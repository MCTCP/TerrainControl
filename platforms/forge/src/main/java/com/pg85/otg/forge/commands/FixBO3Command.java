package com.pg85.otg.forge.commands;

import com.pg85.otg.OTG;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobject.BOCreator;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.bo3.BO3;
import com.pg85.otg.customobject.bo3.BO3Creator;
import com.pg85.otg.customobject.bo3.bo3function.BO3BlockFunction;
import com.pg85.otg.customobject.bo3.bo3function.BO3RandomBlockFunction;
import com.pg85.otg.customobject.util.BoundingBox;
import com.pg85.otg.forge.gen.ForgeWorldGenRegion;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.forge.util.ForgeNBTHelper;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.LocalNBTHelper;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.gen.LocalWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterials;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FixBO3Command
{
	private static final HashSet<ResourceLocation> updateMap = Stream.of(
		"oak_fence",
		"birch_fence",
		"nether_brick_fence",
		"spruce_fence",
		"jungle_fence",
		"acacia_fence",
		"dark_oak_fence",
		"iron_door",
		"oak_door",
		"spruce_door",
		"birch_door",
		"jungle_door",
		"acacia_door",
		"dark_oak_door",
		"glass_pane",
		"white_stained_glass_pane",
		"orange_stained_glass_pane",
		"magenta_stained_glass_pane",
		"light_blue_stained_glass_pane",
		"yellow_stained_glass_pane",
		"lime_stained_glass_pane",
		"pink_stained_glass_pane",
		"gray_stained_glass_pane",
		"light_gray_stained_glass_pane",
		"cyan_stained_glass_pane",
		"purple_stained_glass_pane",
		"blue_stained_glass_pane",
		"brown_stained_glass_pane",
		"green_stained_glass_pane",
		"red_stained_glass_pane",
		"black_stained_glass_pane",
		"purpur_stairs",
		"oak_stairs",
		"cobblestone_stairs",
		"brick_stairs",
		"stone_brick_stairs",
		"nether_brick_stairs",
		"spruce_stairs",
		"sandstone_stairs",
		"birch_stairs",
		"jungle_stairs",
		"quartz_stairs",
		"acacia_stairs",
		"dark_oak_stairs",
		"prismarine_stairs",
		"prismarine_brick_stairs",
		"dark_prismarine_stairs",
		"red_sandstone_stairs",
		"polished_granite_stairs",
		"smooth_red_sandstone_stairs",
		"mossy_stone_brick_stairs",
		"polished_diorite_stairs",
		"mossy_cobblestone_stairs",
		"end_stone_brick_stairs",
		"stone_stairs",
		"smooth_sandstone_stairs",
		"smooth_quartz_stairs",
		"granite_stairs",
		"andesite_stairs",
		"red_nether_brick_stairs",
		"polished_andesite_stairs",
		"diorite_stairs",
		"cobblestone_wall",
		"mossy_cobblestone_wall",
		"brick_wall",
		"prismarine_wall",
		"red_sandstone_wall",
		"mossy_stone_brick_wall",
		"granite_wall",
		"stone_brick_wall",
		"nether_brick_wall",
		"andesite_wall",
		"red_nether_brick_wall",
		"sandstone_wall",
		"end_stone_brick_wall",
		"diorite_wall",
		"iron_bars",
		"trapped_chest",
		"chest",
		"redstone_wire")
		.map(ResourceLocation::new)
		.collect(Collectors.toCollection(HashSet::new));

	public static int execute(CommandSource source, String presetName, String object, boolean doCleaning, boolean doOverwrite)
	{
		try
		{
			if (!(source.getWorld().getChunkProvider().getChunkGenerator() instanceof OTGNoiseChunkGenerator))
			{
				source.sendFeedback(new StringTextComponent("Can only run this command in an OTG world"), false);
				return 0;
			}
			if (source.getEntity() == null)
			{
				source.sendFeedback(new StringTextComponent("Only players can run this command"), false);
			}

			CustomObject objectToSpawn = SpawnCommand.getObject(object, presetName);

			if (objectToSpawn == null)
			{
				source.sendFeedback(new StringTextComponent("Could not find an object by the name " + object + " in either " + presetName + " or Global objects"), false);
				return 0;
			}

			if (!(objectToSpawn instanceof BO3))
			{
				source.sendFeedback(new StringTextComponent("Object is not a BO3"), false);
				return 0;
			}

			Preset preset = OTG.getEngine().getPresetLoader().getPresetByName(presetName);
			LocalNBTHelper nbtHelper = new ForgeNBTHelper();
			Path objectPath = preset.getPresetDir().resolve(Constants.WORLD_OBJECTS_FOLDER);

			BO3 bo3 = (BO3) objectToSpawn;
			// Use ForgeWorldGenRegion as a wrapper for the world that BO3Creator can interact with
			ForgeWorldGenRegion genRegion = new ForgeWorldGenRegion(preset.getName(), preset.getWorldConfig(), source.getWorld(),
				(OTGNoiseChunkGenerator) source.getWorld().getChunkProvider().getChunkGenerator());

			BlockPos pos = source.getEntity().getPosition();
			// y = 100 is way simpler than dynamic height
			int y = 100;//15 + region.getBlockAboveSolidHeight(pos.getX(), pos.getZ(), ChunkCoordinate.fromBlockCoords(pos.getX(), pos.getZ()));
			int x = pos.getX() + 15;
			int z = pos.getZ() + 15;

			BoundingBox box = bo3.getBoundingBox(Rotation.NORTH);
			BOCreator.Corner min = new BOCreator.Corner(x + box.getMinX(), y + box.getMinY(), z + box.getMinZ());
			BOCreator.Corner max = new BOCreator.Corner(
				x + box.getMinX() + box.getWidth(),
				y + box.getMinY() + box.getHeight(),
				z + box.getMinZ() + box.getDepth());

			// Prepare area for spawning

			cleanArea(genRegion, min, max);

			// Spawn code, taken and modified from BO3.java :: spawnForced()

			BO3BlockFunction[] blocks = bo3.getSettings().getBlocks(0);
			Random random = new Random();
			HashSet<BlockPos> updates = new HashSet<>();
			HashSet<BlockPos> cleanup = new HashSet<>();
			ArrayList<BO3BlockFunction> extraBlocks = new ArrayList<>();

			for (BO3BlockFunction block : blocks)
			{
				if (block.material != null && updateMap.contains(((ForgeMaterialData) block.material).internalBlock().getBlock().getRegistryName()))
				{
					updates.add(new BlockPos(x + block.x, y + block.y, z + block.z));
				}

				if (block.material != null &&
					(
						block.material.isMaterial(LocalMaterials.AIR)
						|| block.nbt != null
						|| block instanceof BO3RandomBlockFunction
					))
				{
					extraBlocks.add(block);
					continue;
				}
				block.spawn(genRegion, random, x + block.x, y + block.y, z + block.z, null, true);
				//cleanup.add(new BlockPos(x + block.x, y + block.y, z + block.z));
			}

			for (BlockPos blockpos : updates)
			{
				BlockState blockstate = genRegion.getBlockState(blockpos);
				BlockState blockstate1 = Block.getValidBlockForPosition(blockstate, genRegion.getInternal(), blockpos);
				genRegion.setBlockState(blockpos, blockstate1, 20);
			}


			BO3 fixedBO3 = BO3Creator.create(min, max, new BOCreator.Corner(x, y, z), "fixed_" + bo3.getName(), false, objectPath,
				genRegion, nbtHelper, extraBlocks, bo3.getSettings(), presetName,
				OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getPluginConfig().getSpawnLogEnabled(),
				OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getMaterialReader(),
				OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());

			if (fixedBO3 != null)
			{
				source.sendFeedback(new StringTextComponent("Successfully updated BO3 " + bo3.getName()), false);
				OTG.getEngine().getCustomObjectManager().getGlobalObjects().addObjectToPreset(presetName, fixedBO3.getName().toLowerCase(Locale.ROOT), fixedBO3.getSettings().getFile());
			}
			else
			{
				source.sendFeedback(new StringTextComponent("Failed to update BO3 " + bo3.getName()), false);
			}

			// Cleanup - remove all the blocks we placed

			if (doCleaning)
			{
				cleanArea(genRegion, min, max);
			}


		}
		catch (Exception e)
		{
			OTG.log(LogMarker.INFO, e.toString());
			for (StackTraceElement s : e.getStackTrace())
			{
				OTG.log(LogMarker.INFO, s.toString());
			}
		}

		return 0;
	}

	private static void cleanArea(LocalWorldGenRegion region, BOCreator.Corner min, BOCreator.Corner max)
	{
		for (int x1 = min.x; x1 <= max.x; x1++)
		{
			for (int z1 = min.z; z1 <= max.z; z1++)
			{
				for (int y1 = min.y; y1 <= max.y; y1++)
				{
					region.setBlock(x1, y1, z1, LocalMaterials.AIR, null, ChunkCoordinate.fromBlockCoords(x1, z1), false);
				}
			}
		}
	}
}
