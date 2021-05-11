package com.pg85.otg.spigot.commands;

import com.pg85.otg.OTG;
import com.pg85.otg.customobject.BOCreator;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.bo3.BO3;
import com.pg85.otg.customobject.bo3.BO3Creator;
import com.pg85.otg.customobject.bo3.bo3function.BO3BlockFunction;
import com.pg85.otg.customobject.bo3.bo3function.BO3RandomBlockFunction;
import com.pg85.otg.customobject.util.BoundingBox;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.spigot.gen.SpigotWorldGenRegion;
import com.pg85.otg.spigot.materials.SpigotMaterialData;
import com.pg85.otg.spigot.util.SpigotNBTHelper;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.gen.LocalWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterials;
import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EditCommand
{
	private static final HashMap<Player, EditSession> sessionsMap = new HashMap<>();

	public static boolean execute(CommandSender sender, String[] args)
	{
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players can execute this command"); return true;}
		Player player = (Player) sender;

		if (args.length == 0) { help(player); return true;}

		if (args.length < 2) { sender.sendMessage("Please supply an object name to export"); return true;}

		String presetName = args[0];
		String objectName = args[1];
		String flags = args.length >= 3 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "";
		boolean immediate = flags.contains("-update");
		boolean doFixing = !flags.contains("-nofix");

		CustomObject objectToSpawn = SpawnCommand.getObject(objectName, presetName);

		if (!(objectToSpawn instanceof BO3)) { sender.sendMessage("Could not find BO3 " + objectName); return true; }

		Preset preset = ExportCommand.getPreset(presetName);
		if (preset == null) { sender.sendMessage("Could not find preset "+presetName); return true; }

		Path objectPath = ExportCommand.getObjectPath(preset, presetName);

		BO3 bo3 = (BO3) objectToSpawn;

		SpigotWorldGenRegion genRegion = new SpigotWorldGenRegion(preset.getName(), preset.getWorldConfig(),
			((CraftWorld) player.getWorld()).getHandle(), ((CraftWorld) player.getWorld()).getHandle().getChunkProvider().getChunkGenerator());

		Location pos = player.getLocation();

		BoundingBox box = bo3.getBoundingBox(Rotation.NORTH);
		BOCreator.Corner center = new BOCreator.Corner(pos.getBlockX() + 2 + (box.getWidth() / 2), pos.getBlockY(), pos.getBlockZ() + 2 + (box.getDepth() / 2));
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
				genRegion, new SpigotNBTHelper(), extraBlocks, bo3.getSettings(), presetName,
				OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getPluginConfig().getSpawnLogEnabled(),
				OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getMaterialReader(),
				OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());

			if (fixedBO3 != null)
			{
				player.sendMessage("Successfully updated BO3 " + bo3.getName());
				OTG.getEngine().getCustomObjectManager().getGlobalObjects().addObjectToPreset(presetName, fixedBO3.getName().toLowerCase(Locale.ROOT), fixedBO3.getSettings().getFile(), bo3);
			}
			else
			{
				player.sendMessage("Failed to update BO3 " + bo3.getName());
			}
			cleanArea(genRegion, region.getLow(), region.getHigh());
		} else {
			// Store the info, wait for /otg finishedit
			sessionsMap.put(player, new EditSession(genRegion, bo3, extraBlocks, objectPath, presetName, center));
			player.sendMessage("You can now edit the bo3");
			player.sendMessage("To change the area of the bo3, use /otg region");
			player.sendMessage("When you are done editing, do /otg finishedit");
			ExportCommand.playerSelectionMap.put(player, region);
		}

		return true;
	}

	protected static void cleanArea(LocalWorldGenRegion region, BOCreator.Corner min, BOCreator.Corner max)
	{
		for (int x1 = min.x-1; x1 <= max.x+1; x1++)
		{
			for (int z1 = min.z-1; z1 <= max.z+1; z1++)
			{
				for (int y1 = min.y-1; y1 <= max.y+1; y1++)
				{
					region.setBlock(x1, y1, z1, LocalMaterials.AIR, null, ChunkCoordinate.fromBlockCoords(x1, z1), false);
				}
			}
		}
	}

	public static boolean finish(CommandSender sender)
	{
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players can execute this command"); return true;}
		Player source = (Player) sender;

		EditSession session = sessionsMap.get(source);
		if (session != null) source.sendMessage("Cleaning up...");
		else {source.sendMessage("No active session, do '/otg edit' to start one"); return true;}

		ExportCommand.Region region = ExportCommand.playerSelectionMap.get(source);

		BO3 bo3 = BO3Creator.create(region.getLow(), region.getHigh(), session.center, null, session.bo3.getName(), false, session.objectPath,
			session.genRegion, new SpigotNBTHelper(), session.extraBlocks, session.bo3.getSettings(), session.presetName,
			OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getPluginConfig().getSpawnLogEnabled(),
			OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getMaterialReader(),
			OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());

		if (bo3 != null)
		{
			source.sendMessage("Successfully edited BO3 " + bo3.getName());
			OTG.getEngine().getCustomObjectManager().getGlobalObjects().addObjectToPreset(session.presetName,  bo3.getName().toLowerCase(Locale.ROOT), bo3.getSettings().getFile(), bo3);
		}
		else
		{
			source.sendMessage("Failed to edit BO3 " + bo3.getName());
		}
		cleanArea(session.genRegion, region.getLow(), region.getHigh());
		sessionsMap.put(source, null);

		return true;
	}

	private static final List<String> flags = Arrays.asList("-nofix", "-update");

	public static List<String> tabComplete(HashMap<String, String> strings, boolean doFlags)
	{
		if (strings.size() > 3 && doFlags) {
			// This means we have flags, so we gotta autocomplete the flags
			return flags;
		}
		Set<String> presetNames = OTG.getEngine().getPresetLoader().getAllPresetNames().stream()
			.map(ExportCommand.filterNamesWithSpaces).collect(Collectors.toSet());
		presetNames.add("global");

		String presetName = strings.get("1");
		String objectName = strings.get("2");
		if (presetName == null)
		{
			return new ArrayList<>(presetNames);
		}

		if (objectName != null && presetNames.contains(presetName)) // We have a complete first argument, suggest object name
		{
			return StringUtil.copyPartialMatches(objectName, OTG.getEngine().getCustomObjectManager().getGlobalObjects().getAllBONamesForPreset(presetName), new ArrayList<>());
		} else { // Suggest preset name
			return StringUtil.copyPartialMatches(presetName, presetNames, new ArrayList<>());
		}
	}

	private static class EditSession
	{
		private final SpigotWorldGenRegion genRegion;
		private final BO3 bo3;
		private final ArrayList<BO3BlockFunction> extraBlocks;
		private final Path objectPath;
		private final String presetName;
		private final BOCreator.Corner center;

		public EditSession(SpigotWorldGenRegion genRegion, BO3 bo3, ArrayList<BO3BlockFunction> extraBlocks, Path objectPath, String presetName, BOCreator.Corner center)
		{

			this.genRegion = genRegion;
			this.bo3 = bo3;
			this.extraBlocks = extraBlocks;
			this.objectPath = objectPath;
			this.presetName = presetName;
			this.center = center;
		}
	}

	protected static void spawnAndFixObject(int x, int y, int z, BO3 bo3, ArrayList<BO3BlockFunction> extraBlocks, SpigotWorldGenRegion genRegion, boolean fixObject)
	{
		BO3BlockFunction[] blocks = bo3.getSettings().getBlocks(0);
		Random random = new Random();
		HashSet<BlockPosition> updates = new HashSet<>();

		for (BO3BlockFunction block : blocks)
		{
			if (fixObject && block.material != null && updateMap.contains(((SpigotMaterialData) block.material).internalBlock().getBlock().r()))
			{
				updates.add(new BlockPosition(x + block.x, y + block.y, z + block.z));
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
		}

		if (fixObject)
		{
			for (BlockPosition blockpos : updates)
			{
				IBlockData blockstate = genRegion.getBlockData(blockpos);
				IBlockData blockstate1 = Block.b(blockstate, genRegion.getInternal(), blockpos);
				genRegion.setBlockState(blockpos, blockstate1, 20);
			}
		}
	}



	public static void help(Player player)
	{
		player.sendMessage("To use the edit command:");
		player.sendMessage("/otg edit <preset> <object> [-fix, -clean]");
		player.sendMessage(" * Preset is which preset to fetch the object from, and save it back to");
		player.sendMessage(" * Object is the object you want to edit");
		player.sendMessage(" * The -nofix flag disables block state fixing");
		player.sendMessage(" * The -update flag immediately exports and cleans after fixing");
	}

	private static final HashSet<MinecraftKey> updateMap = Stream.of(
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
		.map(MinecraftKey::new)
		.collect(Collectors.toCollection(HashSet::new));
}
