package com.pg85.otg.forge.commands;

import com.mojang.brigadier.context.CommandContext;
import com.pg85.otg.OTG;
import com.pg85.otg.customobject.BOCreator;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.bo3.BO3;
import com.pg85.otg.customobject.bo3.BO3Creator;
import com.pg85.otg.customobject.bo3.bo3function.BO3BlockFunction;
import com.pg85.otg.customobject.bo3.bo3function.BO3RandomBlockFunction;
import com.pg85.otg.customobject.util.BoundingBox;
import com.pg85.otg.forge.gen.ForgeWorldGenRegion;
import com.pg85.otg.forge.gen.MCWorldGenRegion;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.forge.util.ForgeNBTHelper;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.gen.LocalWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterials;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EditCommand
{
	private static final HashMap<Entity, EditSession> sessionsMap = new HashMap<>();

	public static int execute(CommandContext<CommandSource> context)
	{
		CommandSource source = context.getSource();
		try {
			String presetName = context.getArgument("preset", String.class);
			String objectName = "";
			boolean immediate = false, doFixing = true;

			try
			{
				objectName = context.getArgument("object", String.class);
				String flags = context.getArgument("flags", String.class);
				immediate = flags.contains("-update");
				doFixing = !flags.contains("-nofix");
			}
			catch (IllegalArgumentException ignored) {}
			presetName = presetName != null && presetName.equalsIgnoreCase("global") ? null : presetName;
			boolean isGlobal = presetName == null;
				
			if (objectName.equals("")) { source.sendSuccess(new StringTextComponent("Please supply an object name"), false); return 0; }

			if (source.getEntity() == null) { source.sendSuccess(new StringTextComponent("Only players can run this command"), false); return 0; }

			CustomObject objectToSpawn = SpawnCommand.getObject(objectName, presetName);

			if (!(objectToSpawn instanceof BO3)) { source.sendSuccess(new StringTextComponent("Could not find BO3 " + objectName), false); return 0; }

			Preset preset = ExportCommand.getPresetOrDefault(presetName);
			if (preset == null)
			{
				source.sendSuccess(new StringTextComponent("Could not find preset " + (presetName == null ? "" : presetName)), false); return 0;
			}

			Path objectPath = ExportCommand.getObjectPath(isGlobal ? null : preset.getPresetFolder());

			BO3 bo3 = (BO3) objectToSpawn;

			// Use ForgeWorldGenRegion as a wrapper for the world that BO3Creator can interact with
			ForgeWorldGenRegion genRegion;
			if(source.getLevel().getChunkSource().getGenerator() instanceof OTGNoiseChunkGenerator)
			{
				genRegion = new ForgeWorldGenRegion(
					preset.getFolderName(), 
					preset.getWorldConfig(), 
					source.getLevel(), 
					(OTGNoiseChunkGenerator)source.getLevel().getChunkSource().getGenerator()
				);
			} else {
				genRegion = new MCWorldGenRegion(
					preset.getFolderName(), 
					preset.getWorldConfig(), 
					source.getLevel()
				);
			}

			BlockPos pos = source.getEntity().blockPosition();

			BoundingBox box = bo3.getBoundingBox(Rotation.NORTH);
			BOCreator.Corner center = new BOCreator.Corner(pos.getX() + 2 + (box.getWidth() / 2), pos.getY(), pos.getZ() + 2 + (box.getDepth() / 2));
			ExportCommand.Region region = ExportCommand.getRegionFromObject(center.x, center.y, center.z, bo3);

			// Prepare area for spawning

			cleanArea(genRegion, region.getLow(), region.getHigh());

			// -- Spawn and update --

			// Spawn code, taken and modified from BO3.java :: spawnForced()
			ArrayList<BO3BlockFunction> extraBlocks = new ArrayList<>();

			spawnAndFixObject(center.x, center.y, center.z, bo3, extraBlocks, genRegion, doFixing);

			// Cleanup - remove all the blocks we placed

			if (immediate)
			{
				BO3 fixedBO3 = BO3Creator.create(region.getLow(), region.getHigh(), center, null, "fixed_" + bo3.getName(), false, objectPath,
					genRegion, new ForgeNBTHelper(), extraBlocks, bo3.getSettings(), preset.getFolderName(),
					OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getPluginConfig().getSpawnLogEnabled(),
					OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getPresetLoader().getMaterialReader(preset.getFolderName()),
					OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());

				if (fixedBO3 != null)
				{
					source.sendSuccess(new StringTextComponent("Successfully updated BO3 " + bo3.getName()), false);
					OTG.getEngine().getCustomObjectManager().getGlobalObjects().addObjectToPreset(preset.getFolderName(), fixedBO3.getName().toLowerCase(Locale.ROOT), fixedBO3.getSettings().getFile(), bo3);
				}
				else
				{
					source.sendSuccess(new StringTextComponent("Failed to update BO3 " + bo3.getName()), false);
				}
				cleanArea(genRegion, region.getLow(), region.getHigh());
			} else {
				// Store the info, wait for /otg finishedit
				sessionsMap.put(source.getEntity(), new EditSession(genRegion, bo3, extraBlocks, objectPath, preset.getFolderName(), center));
				source.sendSuccess(new StringTextComponent("You can now edit the bo3"), false);
				source.sendSuccess(new StringTextComponent("To change the area of the bo3, use /otg region"), false);
				source.sendSuccess(new StringTextComponent("When you are done editing, do /otg finishedit"), false);
				ExportCommand.playerSelectionMap.put(source.getEntity(), region);
			}
		}
		catch (Exception e)
		{
			source.sendSuccess(new StringTextComponent("Something went wrong, please check logs"), false);
			OTG.log(LogMarker.INFO, e.toString());
			for (StackTraceElement s : e.getStackTrace())
			{
				OTG.log(LogMarker.INFO, s.toString());
			}
		}

		return 0;
	}

	public static int finish(CommandContext<CommandSource> context)
	{
		CommandSource source = context.getSource();
		try
		{
			EditSession session = sessionsMap.get(source.getEntity());
			if (session != null) source.sendSuccess(new StringTextComponent("Cleaning up..."), false);
			else {source.sendSuccess(new StringTextComponent("No active session, do '/otg edit' to start one"), false); return 0;}

			ExportCommand.Region region = ExportCommand.playerSelectionMap.get(source.getEntity());

			BO3 bo3 = BO3Creator.create(region.getLow(), region.getHigh(), session.center, null, session.bo3.getName(), false, session.objectPath,
				session.genRegion, new ForgeNBTHelper(), session.extraBlocks, session.bo3.getSettings(), session.presetFolderName,
				OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getPluginConfig().getSpawnLogEnabled(),
				OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getPresetLoader().getMaterialReader(session.presetFolderName),
				OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());

			if (bo3 != null)
			{
				source.sendSuccess(new StringTextComponent("Successfully edited BO3 " + bo3.getName()), false);
				OTG.getEngine().getCustomObjectManager().getGlobalObjects().addObjectToPreset(session.presetFolderName,  bo3.getName().toLowerCase(Locale.ROOT), bo3.getSettings().getFile(), bo3);
			} else {
				source.sendSuccess(new StringTextComponent("Failed to edit BO3 " + session.bo3.getName()), false);
			}
			cleanArea(session.genRegion, region.getLow(), region.getHigh());
			sessionsMap.put(source.getEntity(), null);
		}
		catch (Exception e)
		{
			source.sendSuccess(new StringTextComponent("Something went wrong, please check logs"), false);
			OTG.log(LogMarker.INFO, e.toString());
			for (StackTraceElement s : e.getStackTrace())
			{
				OTG.log(LogMarker.INFO, s.toString());
			}
		}
		return 0;
	}


	private static class EditSession {
		private final ForgeWorldGenRegion genRegion;
		private final BO3 bo3;
		private final ArrayList<BO3BlockFunction> extraBlocks;
		private final Path objectPath;
		private final String presetFolderName;
		private final BOCreator.Corner center;

		public EditSession(ForgeWorldGenRegion genRegion, BO3 bo3, ArrayList<BO3BlockFunction> extraBlocks, Path objectPath, String presetFolderName, BOCreator.Corner center)
		{
			this.genRegion = genRegion;
			this.bo3 = bo3;
			this.extraBlocks = extraBlocks;
			this.objectPath = objectPath;
			this.presetFolderName = presetFolderName;
			this.center = center;
		}
	}

	protected static void spawnAndFixObject(int x, int y, int z, BO3 bo3, ArrayList<BO3BlockFunction> extraBlocks, ForgeWorldGenRegion worldGenRegion, boolean fixObject)
	{
		BO3BlockFunction[] blocks = bo3.getSettings().getBlocks(0);
		Random random = new Random();
		HashSet<BlockPos> updates = new HashSet<>();

		ReplaceBlockMatrix replaceBlocks = null;
		int lastX = Integer.MIN_VALUE;
		int lastZ = Integer.MIN_VALUE;
		for (BO3BlockFunction block : blocks)
		{
			if (fixObject && block.material != null && updateMap.contains(((ForgeMaterialData) block.material).internalBlock().getBlock().getRegistryName()))
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
			if(
				bo3.doReplaceBlocks() && 
				(lastX != x + block.x || lastZ != z + block.z))
			{
				replaceBlocks = worldGenRegion.getBiomeConfig(x + block.x, z + block.z).getReplaceBlocks();
				lastX = x + block.x;
				lastZ = z + block.z;
			}
			if(bo3.doReplaceBlocks())
			{
				block.spawn(worldGenRegion, random, x + block.x, y + block.y, z + block.z, replaceBlocks);
			} else {
				block.spawn(worldGenRegion, random, x + block.x, y + block.y, z + block.z);
			}
		}

		if (fixObject)
		{
			for (BlockPos blockpos : updates)
			{
				BlockState blockstate = worldGenRegion.getBlockState(blockpos);
				BlockState blockstate1 = Block.updateFromNeighbourShapes(blockstate, worldGenRegion.getInternal(), blockpos);
				worldGenRegion.setBlockState(blockpos, blockstate1, 20);
			}
		}
	}

	protected static void cleanArea(LocalWorldGenRegion region, BOCreator.Corner min, BOCreator.Corner max)
	{
		for (int x1 = min.x-1; x1 <= max.x+1; x1++)
		{
			for (int z1 = min.z-1; z1 <= max.z+1; z1++)
			{
				for (int y1 = min.y-1; y1 <= max.y+1; y1++)
				{
					region.setBlock(x1, y1, z1, LocalMaterials.AIR);
				}
			}
		}
	}

	public static int help(CommandContext<CommandSource> context)
	{
		context.getSource().sendSuccess(new StringTextComponent("To use the edit command:"), false);
		context.getSource().sendSuccess(new StringTextComponent("/otg edit <preset> <object> [-fix, -clean]"), false);
		context.getSource().sendSuccess(new StringTextComponent(" * Preset is which preset to fetch the object from, and save it back to"), false);
		context.getSource().sendSuccess(new StringTextComponent(" * Object is the object you want to edit"), false);
		context.getSource().sendSuccess(new StringTextComponent(" * The -nofix flag disables block state fixing"), false);
		context.getSource().sendSuccess(new StringTextComponent(" * The -update flag immediately exports and cleans after fixing"), false);
		return 0;
	}

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
}
