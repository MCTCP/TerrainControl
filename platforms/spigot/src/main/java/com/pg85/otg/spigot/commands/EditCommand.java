package com.pg85.otg.spigot.commands;

import com.pg85.otg.OTG;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.bo2.BO2;
import com.pg85.otg.customobject.bo3.BO3;
import com.pg85.otg.customobject.bo3.bo3function.BO3RandomBlockFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4RandomBlockFunction;
import com.pg85.otg.customobject.bofunctions.BlockFunction;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.creator.ObjectCreator;
import com.pg85.otg.customobject.creator.ObjectType;
import com.pg85.otg.customobject.structures.StructuredCustomObject;
import com.pg85.otg.customobject.util.Corner;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.ICustomObjectManager;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.spigot.commands.RegionCommand.Region;
import com.pg85.otg.spigot.gen.SpigotWorldGenRegion;
import com.pg85.otg.spigot.materials.SpigotMaterialData;
import com.pg85.otg.spigot.util.SpigotNBTHelper;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EditCommand extends BaseCommand
{
	private static final HashMap<Player, EditCommand.EditSession> sessionsMap = new HashMap();
	private static final List<String> FLAGS = Arrays.asList("-nofix", "-update");
	private static final HashSet<LocalMaterialData> gravityBlocksSet;
	private static final HashSet<MinecraftKey> updateMap;

	static
	{
		gravityBlocksSet = Stream.of(LocalMaterials.SAND, LocalMaterials.RED_SAND, LocalMaterials.GRAVEL).collect(Collectors.toCollection(HashSet::new));
		updateMap = Stream.of("oak_fence", "birch_fence", "nether_brick_fence", "spruce_fence", "jungle_fence", "acacia_fence", "dark_oak_fence", "iron_door", "oak_door", "spruce_door", "birch_door", "jungle_door", "acacia_door", "dark_oak_door", "glass_pane", "white_stained_glass_pane", "orange_stained_glass_pane", "magenta_stained_glass_pane", "light_blue_stained_glass_pane", "yellow_stained_glass_pane", "lime_stained_glass_pane", "pink_stained_glass_pane", "gray_stained_glass_pane", "light_gray_stained_glass_pane", "cyan_stained_glass_pane", "purple_stained_glass_pane", "blue_stained_glass_pane", "brown_stained_glass_pane", "green_stained_glass_pane", "red_stained_glass_pane", "black_stained_glass_pane", "purpur_stairs", "oak_stairs", "cobblestone_stairs", "brick_stairs", "stone_brick_stairs", "nether_brick_stairs", "spruce_stairs", "sandstone_stairs", "birch_stairs", "jungle_stairs", "quartz_stairs", "acacia_stairs", "dark_oak_stairs", "prismarine_stairs", "prismarine_brick_stairs", "dark_prismarine_stairs", "red_sandstone_stairs", "polished_granite_stairs", "smooth_red_sandstone_stairs", "mossy_stone_brick_stairs", "polished_diorite_stairs", "mossy_cobblestone_stairs", "end_stone_brick_stairs", "stone_stairs", "smooth_sandstone_stairs", "smooth_quartz_stairs", "granite_stairs", "andesite_stairs", "red_nether_brick_stairs", "polished_andesite_stairs", "diorite_stairs", "cobblestone_wall", "mossy_cobblestone_wall", "brick_wall", "prismarine_wall", "red_sandstone_wall", "mossy_stone_brick_wall", "granite_wall", "stone_brick_wall", "nether_brick_wall", "andesite_wall", "red_nether_brick_wall", "sandstone_wall", "end_stone_brick_wall", "diorite_wall", "iron_bars", "trapped_chest", "chest", "redstone_wire", "oak_leaves", "spruce_leaves", "birch_leaves", "jungle_leaves", "acacia_leaves", "dark_oak_leaves").map(MinecraftKey::new).collect(Collectors.toCollection(HashSet::new));
	}

	public EditCommand()
	{
		this.name = "edit";
		this.helpMessage = "Allows you to edit existing BO3 and BO4 files.";
		this.usage = "Please see /otg help edit.";
	}

	protected static StructuredCustomObject getStructuredObject(String objectName, String presetFolderName)
	{
		CustomObject objectToSpawn = ObjectUtils.getObject(objectName, presetFolderName);
		if (objectToSpawn instanceof StructuredCustomObject)
		{
			return (StructuredCustomObject) objectToSpawn;
		}
		if (objectToSpawn instanceof BO2)
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

	protected static ArrayList<BlockFunction<?>> spawnAndFixObject(int x, int y, int z, StructuredCustomObject object, SpigotWorldGenRegion worldGenRegion, boolean fixObject, String presetFolderName, Path otgRootFolder, ILogger logger, ICustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		BlockFunction<?>[] blocks = object.getConfig().getBlockFunctions(presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
		HashSet<BlockPosition> updates = new HashSet<>();
		HashSet<BlockPosition> gravityBlocksToCheck = new HashSet<>();
		Random random = new Random();
		ArrayList<BlockFunction<?>> unspawnedBlocks = new ArrayList<>();

		for (BlockFunction<?> block : blocks)
		{
			if (fixObject && block.material != null && updateMap.contains(((SpigotMaterialData) block.material).internalBlock().getBlock().r()))
			{
				updates.add(new BlockPosition(x + block.x, y + block.y, z + block.z));
			}

			if (block.material != null && block.nbt == null && !(block instanceof BO3RandomBlockFunction) && !(block instanceof BO4RandomBlockFunction))
			{
				if (gravityBlocksSet.contains(block.material))
				{
					gravityBlocksToCheck.add(new BlockPosition(x + block.x, y + block.y, z + block.z));
				}

				if (block.material.isLeaves())
				{
					block.material = SpigotMaterialData.ofBlockData(((SpigotMaterialData) block.material).internalBlock().set(BlockLeaves.PERSISTENT, true).set(BlockLeaves.DISTANCE, 7));
				}

				block.spawn(worldGenRegion, random, x + block.x, y + block.y, z + block.z);
			}
			else
			{
				unspawnedBlocks.add(block);
			}
		}

		for (BlockPosition pos : gravityBlocksToCheck)
		{
			if (worldGenRegion.getMaterial(pos.getX(), pos.getY() - 1, pos.getZ()).isMaterial(LocalMaterials.STRUCTURE_VOID))
			{
				worldGenRegion.setBlock(pos.getX(), pos.getY() - 1, pos.getZ(), LocalMaterials.STRUCTURE_BLOCK);
			}
		}

		if (fixObject)
		{
			for (BlockPosition pos : updates)
			{
				IBlockData blockstate = worldGenRegion.getBlockData(pos);
				if (blockstate.a(TagsBlock.LEAVES))
				{
					worldGenRegion.getInternal().getBlockTickList().a(pos, blockstate.getBlock(), 1);
				}
				else
				{
					IBlockData blockstate1 = Block.b(blockstate, worldGenRegion.getInternal(), pos);
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
				OTG.getEngine().getCustomObjectManager().getGlobalObjects().addObjectToPreset(session.presetFolderName, object.getName().toLowerCase(Locale.ROOT), object.getConfig().getFile(), object);
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
		return ObjectCreator.createObject(session.type, region.getMin(), region.getMax(), session.extraBlocks.isEmpty() ? region.getCenter() : session.originalCenterPoint, null, session.object.getName(), false, session.objectPath, session.genRegion, new SpigotNBTHelper(), session.extraBlocks, session.object.getConfig(), session.presetFolderName, OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getPresetLoader().getMaterialReader(session.presetFolderName), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());
	}

	public List<String> onTabComplete(CommandSender sender, String[] args)
	{
		Map<String, String> strings = CommandUtil.parseArgs(args, true);
		if (strings.size() > 3)
		{
			return FLAGS;
		}
		else
		{
			Set<String> presetFolderNames = OTG.getEngine().getPresetLoader().getAllPresetFolderNames().stream().map(OldExportCommand.filterNamesWithSpaces).collect(Collectors.toSet());
			presetFolderNames.add("global");
			String presetFolderName = strings.get("1");
			String objectName = strings.get("2");
			if (presetFolderName == null)
			{
				return new ArrayList(presetFolderNames);
			}
			else
			{
				return objectName != null && presetFolderNames.contains(presetFolderName) ? (List) StringUtil.copyPartialMatches(objectName, OTG.getEngine().getCustomObjectManager().getGlobalObjects().getAllBONamesForPreset(presetFolderName, OTG.getEngine().getLogger(), OTG.getEngine().getOTGRootFolder()), new ArrayList()) : (List) StringUtil.copyPartialMatches(presetFolderName, presetFolderNames, new ArrayList());
			}
		}
	}

	public boolean execute(CommandSender sender, String[] args)
	{
		if (!(sender instanceof Player))
		{
			sender.sendMessage("Only players can execute this command");
			return true;
		}
		Player source = (Player) sender;
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
		presetFolderName = presetFolderName != null && presetFolderName.equalsIgnoreCase("global") ? null : presetFolderName;
		boolean isGlobal = presetFolderName == null;
		StructuredCustomObject inputObject = getStructuredObject(objectName, presetFolderName);
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
		SpigotWorldGenRegion worldGenRegion = ObjectUtils.getWorldGenRegion(preset, (CraftWorld) source.getWorld());
		Region region = ObjectUtils.getRegionFromObject(source.getLocation(), inputObject);
		Corner center = region.getCenter();
		ObjectUtils.cleanArea(worldGenRegion, region.getMin(), region.getMax(), true);
		ArrayList<BlockFunction<?>> extraBlocks = spawnAndFixObject(center.x, center.y, center.z, inputObject, worldGenRegion, doFixing, presetFolderName, OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getPresetLoader().getMaterialReader(presetFolderName), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());
		Path path = ObjectUtils.getObjectFolderPath(isGlobal ? null : preset.getPresetFolder()).resolve(ObjectUtils.getFoldersFromObject(inputObject));
		if (immediate)
		{
			(new Thread(ObjectUtils.getExportRunnable(type, region, center, inputObject, path, extraBlocks, presetFolderName, true, source, worldGenRegion))).start();
			return true;
		}
		sessionsMap.put(source, new EditCommand.EditSession(type, worldGenRegion, inputObject, extraBlocks, path, preset.getFolderName(), center));
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

	private static class EditSession
	{
		private final ObjectType type;
		private final SpigotWorldGenRegion genRegion;
		private final StructuredCustomObject object;
		private final ArrayList<BlockFunction<?>> extraBlocks;
		private final Path objectPath;
		private final String presetFolderName;
		private final Corner originalCenterPoint;

		public EditSession(ObjectType type, SpigotWorldGenRegion genRegion, StructuredCustomObject object, ArrayList<BlockFunction<?>> extraBlocks, Path objectPath, String presetFolderName, Corner originalCenterPoint)
		{
			this.type = type;
			this.genRegion = genRegion;
			this.object = object;
			this.extraBlocks = extraBlocks;
			this.objectPath = objectPath;
			this.presetFolderName = presetFolderName;
			this.originalCenterPoint = originalCenterPoint;
		}
	}
}
