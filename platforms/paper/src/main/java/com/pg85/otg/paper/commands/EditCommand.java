package com.pg85.otg.paper.commands;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.pg85.otg.OTG;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.bo2.BO2;
import com.pg85.otg.customobject.bo3.BO3;
import com.pg85.otg.customobject.bo3.bo3function.BO3RandomBlockFunction;
import com.pg85.otg.customobject.bo4.BO4;
import com.pg85.otg.customobject.bo4.BO4Config;
import com.pg85.otg.customobject.bo4.bo4function.BO4RandomBlockFunction;
import com.pg85.otg.customobject.bofunctions.BlockFunction;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.config.io.FileSettingsReaderBO4;
import com.pg85.otg.customobject.creator.ObjectCreator;
import com.pg85.otg.customobject.creator.ObjectType;
import com.pg85.otg.customobject.structures.StructuredCustomObject;
import com.pg85.otg.customobject.util.Corner;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.ICustomObjectManager;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.paper.commands.RegionCommand.Region;
import com.pg85.otg.paper.gen.PaperWorldGenRegion;
import com.pg85.otg.paper.materials.PaperMaterialData;
import com.pg85.otg.paper.util.PaperNBTHelper;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;

public class EditCommand extends BaseCommand
{
	private static final HashMap<Player, EditCommand.EditSession> sessionsMap = new HashMap<>();
	private static final List<String> FLAGS = Arrays.asList("-nofix", "-update", "-wrongleaves");
	private static final HashSet<LocalMaterialData> gravityBlocksSet;

	static
	{
		gravityBlocksSet = Stream.of(LocalMaterials.SAND, LocalMaterials.RED_SAND, LocalMaterials.GRAVEL).collect(Collectors.toCollection(HashSet::new));
	}

	public EditCommand()
	{
	super("edit");
		this.helpMessage = "Allows you to edit existing BO3 and BO4 files.";
		this.usage = "Please see /otg help edit.";
	}

	protected static StructuredCustomObject getStructuredObject(String objectName, String presetFolderName) throws InvalidConfigException
	{
		CustomObject objectToSpawn = ObjectUtils.getObject(objectName, presetFolderName);
		if (objectToSpawn instanceof BO3)
		{
			return (StructuredCustomObject) objectToSpawn;
		}

		if (presetFolderName == null)
		{
			presetFolderName = OTG.getEngine().getPresetLoader().getDefaultPresetFolderName();
		}

		if (objectToSpawn instanceof BO4)
		{
			File file = ((BO4) objectToSpawn).getConfig().getFile();
			return new BO4(
				objectName,
				file,
				new BO4Config(
					new FileSettingsReaderBO4(objectName, file, OTG.getEngine().getLogger()),
					true,
					presetFolderName,
					OTG.getEngine().getOTGRootFolder(),
					OTG.getEngine().getLogger(),
					OTG.getEngine().getCustomObjectManager(),
					OTG.getEngine().getPresetLoader().getMaterialReader(presetFolderName),
					OTG.getEngine().getCustomObjectResourcesManager(),
					OTG.getEngine().getModLoadedChecker()));
		}

		else if (objectToSpawn instanceof BO2)
		{
			try
			{
				return new BO3(objectToSpawn.getName(), ObjectType.BO3.getObjectFilePathFromName(objectToSpawn.getName(), ((BO2) objectToSpawn).getFile().getParentFile().toPath()).toFile(), ((BO2) objectToSpawn).getConvertedConfig(presetFolderName, OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getPresetLoader().getMaterialReader(presetFolderName), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker()));
			}
			catch (InvalidConfigException var4)
			{
				OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, "Failed to convert BO2 " + objectName);
				OTG.getEngine().getLogger().printStackTrace(LogLevel.ERROR, LogCategory.MAIN, var4);
			}
		}

		return null;
	}

	protected static ArrayList<BlockFunction<?>> spawnAndFixObject(int x, int y, int z, StructuredCustomObject object, PaperWorldGenRegion worldGenRegion, boolean fixObject, String presetFolderName, Path otgRootFolder, ILogger logger, ICustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		BlockFunction<?>[] blocks = object.getConfig().getBlockFunctions(presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
		HashSet<BlockPos> updates = new HashSet<>();
		HashSet<BlockPos> gravityBlocksToCheck = new HashSet<>();
		Random random = new Random();
		ArrayList<BlockFunction<?>> unspawnedBlocks = new ArrayList<>();

		for (BlockFunction<?> block : blocks)
		{
			if (
				block.material == null
				|| (block.nbt != null
					|| block instanceof BO3RandomBlockFunction
					|| block instanceof BO4RandomBlockFunction
					|| block.material.isBlank())
			)
			{
				unspawnedBlocks.add(block);
				continue;
			}

			// Queue the block coordinate for processing
			if (fixObject && updateMap.contains(ResourceLocation.tryParse(block.material.getRegistryName())))
			{
				updates.add(new BlockPos(x + block.x, y + block.y, z + block.z));
			}

			// Make sure gravel and sand blocks don't fall down
			if (gravityBlocksSet.contains(block.material))
			{
				gravityBlocksToCheck.add(new BlockPos(x + block.x, y + block.y, z + block.z));
			}

			// We set all leaves to be persistent so they don't disappear in the process
			// They lose persistence on export if they are legal
			if (block.material.isLeaves())
			{
				block.material = PaperMaterialData.ofBlockData(((PaperMaterialData) block.material)
					.internalBlock().setValue(LeavesBlock.PERSISTENT, true).setValue(LeavesBlock.DISTANCE, 7));
			}

			block.spawn(worldGenRegion, random, x + block.x, y + block.y, z + block.z);
		}

		for (BlockPos pos : gravityBlocksToCheck)
		{
			if (worldGenRegion.getMaterial(pos.getX(), pos.getY() - 1, pos.getZ()).isMaterial(LocalMaterials.STRUCTURE_VOID))
			{
				worldGenRegion.setBlock(pos.getX(), pos.getY() - 1, pos.getZ(), LocalMaterials.STRUCTURE_BLOCK);
			}
		}

		if (fixObject)
		{
			for (BlockPos pos : updates)
			{
				BlockState blockstate = worldGenRegion.getBlockData(pos);
				if (blockstate.is(BlockTags.LEAVES))
				{
					worldGenRegion.getInternal().getBlockTicks().scheduleTick(pos, blockstate.getBlock(), 1);
				} else {
					BlockState blockstate1 = Block.updateFromNeighbourShapes(blockstate, worldGenRegion.getInternal(), pos);
					worldGenRegion.setBlockState(pos, blockstate1, 20);
				}
			}
		}

		return unspawnedBlocks;
	}

	public static void finishSession(Player source)
	{
		try
		{
			EditCommand.EditSession session = sessionsMap.get(source);
			Region region = RegionCommand.playerSelectionMap.get(source);
			if (session == null)
			{
				source.sendMessage("No active session, do '/otg edit' to start one");
				return;
			}

			if (ObjectUtils.isOutsideBounds(region, session.type))
			{
				source.sendMessage("Selection is too big! Maximum size is 16x16 for BO4 and 32x32 for BO3");
				return;
			}

			source.sendMessage("Cleaning up...");
			StructuredCustomObject object = exportFromSession(session, region);
			if (object != null)
			{
				source.sendMessage("Successfully edited " + session.type.getType() + " " + object.getName());
				OTG.getEngine().getCustomObjectManager().getGlobalObjects().addObjectToPreset(session.presetFolderName, object.getName(), object.getConfig().getFile(), object);
			}
			else
			{
				source.sendMessage("Failed to edit " + session.type.getType() + " " + session.object.getName());
			}

			ObjectUtils.cleanArea(session.genRegion, region.getMin(), region.getMax(), false);
			sessionsMap.put(source, null);
		}
		catch (Exception var4)
		{
			source.sendMessage("Edit command encountered an error, please check logs.");
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, "Edit command encountered an error: ");
			OTG.getEngine().getLogger().printStackTrace(LogLevel.ERROR, LogCategory.MAIN, var4);
		}

	}

	public static void cancelSession(Player player)
	{
		EditCommand.EditSession session = sessionsMap.get(player);
		Region region = RegionCommand.playerSelectionMap.get(player);
		if (session != null && region != null)
		{
			ObjectUtils.cleanArea(session.genRegion, region.getMin(), region.getMax(), false);
			sessionsMap.put(player, null);
			player.sendMessage("Edit session cancelled");
		}
		else
		{
			player.sendMessage("No active edit session to cancel");
		}

	}

	public static StructuredCustomObject exportFromSession(EditCommand.EditSession session, Region region)
	{
		return ObjectCreator.createObject(session.type, region.getMin(), region.getMax(), session.extraBlocks.isEmpty() ? region.getCenter() : session.originalCenterPoint, null, session.object.getName(), false, session.leaveIllegalLeaves, session.objectPath, session.genRegion, new PaperNBTHelper(), session.extraBlocks, session.object.getConfig(), session.presetFolderName, OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getPresetLoader().getMaterialReader(session.presetFolderName), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());
	}

	public List<String> onTabComplete(CommandSender sender, String[] args)
	{
		Map<String, String> strings = CommandUtil.parseArgs(args, true);
		if (strings.size() >= 3)
		{
			return FLAGS;
		}
		else
		{
			Set<String> presetFolderNames = OTG.getEngine().getPresetLoader().getAllPresetFolderNames().stream().map(ExportCommand.filterNamesWithSpaces).collect(Collectors.toSet());
			presetFolderNames.add("global");
			String presetFolderName = strings.get("1");
			String objectName = strings.get("2");
			if (presetFolderName == null)
			{
				return new ArrayList<>(presetFolderNames);
			}
			else
			{
				// This was what the decompiler gave me back, and it works so I'm leaving it -auth
				return objectName != null && presetFolderNames.contains(presetFolderName)
					   ? StringUtil.copyPartialMatches(objectName, OTG.getEngine().getCustomObjectManager().getGlobalObjects().getAllBONamesForPreset(presetFolderName, OTG.getEngine().getLogger(), OTG.getEngine().getOTGRootFolder()), new ArrayList<>())
					   : StringUtil.copyPartialMatches(presetFolderName, presetFolderNames, new ArrayList<>());
			}
		}
	}

	public boolean execute(CommandSender sender, String[] args)
	{
		if (!(sender instanceof Player source))
		{
			sender.sendMessage("Only players can execute this command");
			return true;
		}
		if (args.length == 0)
		{
			this.help(source);
			return true;
		}
		else if (args.length < 2)
		{
			sender.sendMessage("Please supply an object name to export");
			return true;
		}
		String presetFolderName = args[0];
		String objectName = args[1];
		String flags = args.length >= 3 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "";
		boolean immediate = flags.contains("-update");
		boolean doFixing = !flags.contains("-nofix");
		boolean leaveIllegalLeaves = flags.contains("-wrongleaves");
		presetFolderName = presetFolderName != null && presetFolderName.equalsIgnoreCase("global") ? null : presetFolderName;
		boolean isGlobal = presetFolderName == null;
		StructuredCustomObject inputObject;
		try
		{
			inputObject = getStructuredObject(objectName, presetFolderName);
		}
		catch (InvalidConfigException e)
		{
			source.sendMessage("Failed to load object " + objectName);
			return true;
		}
		if (inputObject == null)
		{
			source.sendMessage("Could not find " + objectName);
			return true;
		}
		ObjectType type = inputObject.getType();
		Preset preset = ObjectUtils.getPresetOrDefault(presetFolderName);
		if (preset == null)
		{
			source.sendMessage("Could not find preset " + (presetFolderName == null ? "" : presetFolderName));
			return true;
		}
		PaperWorldGenRegion worldGenRegion = ObjectUtils.getWorldGenRegion(preset, (CraftWorld) source.getWorld());
		Region region = ObjectUtils.getRegionFromObject(source.getLocation(), inputObject);
		Corner center = region.getCenter();
		ObjectUtils.cleanArea(worldGenRegion, region.getMin(), region.getMax(), true);
		ArrayList<BlockFunction<?>> extraBlocks = spawnAndFixObject(center.x, center.y, center.z, inputObject, worldGenRegion, doFixing, presetFolderName, OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getPresetLoader().getMaterialReader(presetFolderName), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());
		Path path = ObjectUtils.getObjectFolderPath(isGlobal ? null : preset.getPresetFolder()).resolve(ObjectUtils.getFoldersFromObject(inputObject));
		if (immediate)
		{
			(new Thread(ObjectUtils.getExportRunnable(type, region, center, inputObject, path, extraBlocks, presetFolderName, true, leaveIllegalLeaves, source, worldGenRegion))).start();
			return true;
		}
		sessionsMap.put(source, new EditCommand.EditSession(type, worldGenRegion, inputObject, extraBlocks, path, preset.getFolderName(), center, leaveIllegalLeaves));
		source.sendMessage("You can now edit the object");
		source.sendMessage("To change the area of the object, use /otg region");
		source.sendMessage("When you are done editing, do /otg finishedit");
		source.sendMessage("To cancel, do /otg canceledit");
		if (!extraBlocks.isEmpty())
		{
			source.sendMessage("This object's center cannot be moved");
		}

		RegionCommand.playerSelectionMap.put(source, region);
		return true;
	}

	public void help(Player source)
	{
		source.sendMessage(ChatColor.LIGHT_PURPLE + "To use the edit command:");
		source.sendMessage("/otg edit <preset> <object> [-nofix, -update]");
		source.sendMessage(" - Preset is which preset to fetch the object from, and save it back to");
		source.sendMessage(" - Object is the object you want to edit");
		source.sendMessage(" - The -nofix flag disables block state fixing");
		source.sendMessage(" - The -update flag immediately exports and cleans after fixing");
		source.sendMessage(" - Complex objects cannot have their center moved");
		source.sendMessage(" - An object is \"complex\" if it contains NBT or RandomBlock");
	}

	private record EditSession(
		ObjectType type,
		PaperWorldGenRegion genRegion,
		StructuredCustomObject object,
		ArrayList<BlockFunction<?>> extraBlocks,
		Path objectPath,
		String presetFolderName,
		Corner originalCenterPoint,
		boolean leaveIllegalLeaves
	) {}


	private static final HashSet<ResourceLocation> updateMap = Stream.of(
		"blocks/oak_fence",
		"blocks/birch_fence",
		"blocks/nether_brick_fence",
		"blocks/spruce_fence",
		"blocks/jungle_fence",
		"blocks/acacia_fence",
		"blocks/dark_oak_fence",
		"blocks/iron_door",
		"blocks/oak_door",
		"blocks/spruce_door",
		"blocks/birch_door",
		"blocks/jungle_door",
		"blocks/acacia_door",
		"blocks/dark_oak_door",
		"blocks/glass_pane",
		"blocks/white_stained_glass_pane",
		"blocks/orange_stained_glass_pane",
		"blocks/magenta_stained_glass_pane",
		"blocks/light_blue_stained_glass_pane",
		"blocks/yellow_stained_glass_pane",
		"blocks/lime_stained_glass_pane",
		"blocks/pink_stained_glass_pane",
		"blocks/gray_stained_glass_pane",
		"blocks/light_gray_stained_glass_pane",
		"blocks/cyan_stained_glass_pane",
		"blocks/purple_stained_glass_pane",
		"blocks/blue_stained_glass_pane",
		"blocks/brown_stained_glass_pane",
		"blocks/green_stained_glass_pane",
		"blocks/red_stained_glass_pane",
		"blocks/black_stained_glass_pane",
		"blocks/purpur_stairs",
		"blocks/oak_stairs",
		"blocks/cobblestone_stairs",
		"blocks/brick_stairs",
		"blocks/stone_brick_stairs",
		"blocks/nether_brick_stairs",
		"blocks/spruce_stairs",
		"blocks/sandstone_stairs",
		"blocks/birch_stairs",
		"blocks/jungle_stairs",
		"blocks/quartz_stairs",
		"blocks/acacia_stairs",
		"blocks/dark_oak_stairs",
		"blocks/prismarine_stairs",
		"blocks/prismarine_brick_stairs",
		"blocks/dark_prismarine_stairs",
		"blocks/red_sandstone_stairs",
		"blocks/polished_granite_stairs",
		"blocks/smooth_red_sandstone_stairs",
		"blocks/mossy_stone_brick_stairs",
		"blocks/polished_diorite_stairs",
		"blocks/mossy_cobblestone_stairs",
		"blocks/end_stone_brick_stairs",
		"blocks/stone_stairs",
		"blocks/smooth_sandstone_stairs",
		"blocks/smooth_quartz_stairs",
		"blocks/granite_stairs",
		"blocks/andesite_stairs",
		"blocks/red_nether_brick_stairs",
		"blocks/polished_andesite_stairs",
		"blocks/diorite_stairs",
		"blocks/cobblestone_wall",
		"blocks/mossy_cobblestone_wall",
		"blocks/brick_wall",
		"blocks/prismarine_wall",
		"blocks/red_sandstone_wall",
		"blocks/mossy_stone_brick_wall",
		"blocks/granite_wall",
		"blocks/stone_brick_wall",
		"blocks/nether_brick_wall",
		"blocks/andesite_wall",
		"blocks/red_nether_brick_wall",
		"blocks/sandstone_wall",
		"blocks/end_stone_brick_wall",
		"blocks/diorite_wall",
		"blocks/iron_bars",
		"blocks/trapped_chest",
		"blocks/chest",
		"blocks/redstone_wire",
		"blocks/oak_leaves",
		"blocks/spruce_leaves",
		"blocks/birch_leaves",
		"blocks/jungle_leaves",
		"blocks/acacia_leaves",
		"blocks/dark_oak_leaves",
		"blocks/vine")
		.map(ResourceLocation::new)
		.collect(Collectors.toCollection(HashSet::new));
}
