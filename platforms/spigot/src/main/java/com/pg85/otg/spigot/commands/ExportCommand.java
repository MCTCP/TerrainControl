package com.pg85.otg.spigot.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pg85.otg.OTG;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobject.BOCreator;
import com.pg85.otg.customobject.bo3.BO3;
import com.pg85.otg.customobject.bo3.BO3Creator;
import com.pg85.otg.customobject.util.BoundingBox;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.spigot.gen.SpigotWorldGenRegion;
import com.pg85.otg.spigot.materials.SpigotMaterialData;
import com.pg85.otg.spigot.util.SpigotNBTHelper;
import com.pg85.otg.util.bo3.LocalNBTHelper;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.gen.LocalWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import net.minecraft.server.v1_16_R3.ArgumentTile;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.IBlockData;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExportCommand
{
	protected static final HashMap<Player, Region> playerSelectionMap = new HashMap<>();

	public static boolean execute(CommandSender sender, String[] args)
	{
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players can execute this command"); return true;}
		Player player = (Player) sender;

		if (args.length == 0) {help(player); return true;}
		String objectName =  args[0];
		IBlockData centerBlockState = null;
		try
		{
			 if (args.length >= 2) centerBlockState = ArgumentTile.a().parse(new StringReader(args[1])).a();
		}
		catch (CommandSyntaxException e)
		{
			sender.sendMessage("Could not find material "+args[1]);
			return true;
		}
		String presetName = args.length >= 3 ? args[2] : "global";
		String templateName = args.length >= 4 ? args[3] : "default";
		String flags = args.length >= 5 ? String.join(" ", Arrays.copyOfRange(args, 4, args.length)) : "";
		boolean overwrite = flags.contains("-o");
		boolean branching = flags.contains("-b");
		boolean includeAir = flags.contains("-a");

		// Get region
		Region region = playerSelectionMap.get(player);
		if (region == null || region.getLow() == null) {
			sender.sendMessage("Please mark two corners with /otg region mark"); return true;}

		// Get preset
		Preset preset = getPreset(presetName);
		if (preset == null) {
			sender.sendMessage("Could not find preset "+presetName); return true; }

		// Get object path
		Path objectPath = getObjectPath(preset, presetName);

		// Check for existing file
		if (!overwrite)
			if (new File(objectPath.toFile(), objectName + ".bo3").exists()) {
				sender.sendMessage("File already exists, run command with flag '-o' to overwrite"); return true; }

		// Get required pieces
		LocalWorldGenRegion otgRegion = new SpigotWorldGenRegion(
			preset.getName(), preset.getWorldConfig(), ((CraftWorld) player.getWorld()).getHandle(),
			((CraftWorld) player.getWorld()).getHandle().getChunkProvider().getChunkGenerator()
		);

		LocalNBTHelper nbtHelper = new SpigotNBTHelper();
		BOCreator.Corner lowCorner = region.getLow();
		BOCreator.Corner highCorner = region.getHigh();
		BOCreator.Corner center = new BOCreator.Corner((highCorner.x - lowCorner.x) / 2 + lowCorner.x, lowCorner.y, (highCorner.z - lowCorner.z) / 2 + lowCorner.z);

		// Fetch template or default settings
		BO3 template = (BO3) OTG.getEngine().getCustomObjectManager().getObjectLoaders().get("bo3")
			.loadFromFile(templateName, new File(objectPath.toFile(), templateName + ".BO3Template"), OTG.getEngine().getLogger());

		// Initialize the settings
		template.onEnable(presetName, OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getPluginConfig().getSpawnLogEnabled(),
			OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getMaterialReader(),
			OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());

		BO3 bo3;

		if (branching)
		{
			try
			{
				bo3 = BO3Creator.createStructure(lowCorner, highCorner, center, objectName, includeAir, objectPath, otgRegion,
					nbtHelper, null, template.getSettings(), presetName, OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getPluginConfig().getSpawnLogEnabled(),
					OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getMaterialReader(),
					OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());
			}
			catch (Exception e)
			{
				OTG.log(LogMarker.INFO, e.toString());
				for (StackTraceElement s : e.getStackTrace())
				{
					OTG.log(LogMarker.INFO, s.toString());
				}
				return true;
			}
		}
		else
		{
			// Create a new BO3 from our settings
			LocalMaterialData centerBlock = centerBlockState == null ? null : SpigotMaterialData.ofBlockData(centerBlockState);
			bo3 = BO3Creator.create(lowCorner, highCorner, center, centerBlock, objectName, includeAir,
				objectPath, otgRegion, nbtHelper, null, template.getSettings(), presetName,
				OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getPluginConfig().getSpawnLogEnabled(),
				OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getMaterialReader(),
				OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());
		}

		// Send feedback, and register the BO3 for immediate use
		if (bo3 != null)
		{
			sender.sendMessage("Successfully created BO3 " + objectName);
			if (presetName.equalsIgnoreCase("global"))
			{
				OTG.getEngine().getCustomObjectManager().registerGlobalObject(bo3, bo3.getSettings().getFile());
			} else {
				OTG.getEngine().getCustomObjectManager().getGlobalObjects().addObjectToPreset(presetName, bo3.getName().toLowerCase(Locale.ROOT), bo3.getSettings().getFile(), bo3);
			}
		}
		else
		{
			sender.sendMessage("Failed to create BO3 " + objectName);
		}

		return true;
	}

	public static void help(Player player)
	{
		player.sendMessage("To use the export command:");
		player.sendMessage("/otg export <name> [center block] [preset] [template] [-a, -b, -o]");
		player.sendMessage(" * Name is the name of the object");
		player.sendMessage(" * Center block is where to place 0,0. It chooses the first it finds from below.");
		player.sendMessage(" * Preset is which preset to fetch template from, and to save the BO3 to");
		player.sendMessage(" * Template is what default settings to apply to the BO3");
		player.sendMessage(" * -a flag is to include air blocks, -b is to export branching, and -o is overwrite file");
	}

	protected static Preset getPreset(String presetName)
	{
		if (presetName.equalsIgnoreCase("global"))
			return OTG.getEngine().getPresetLoader().getPresetByName(OTG.getEngine().getPresetLoader().getDefaultPresetName());
		else
			return OTG.getEngine().getPresetLoader().getPresetByName(presetName);
	}

	protected static Path getObjectPath(Preset preset, String presetName)
	{
		Path objectPath;
		if (presetName.equalsIgnoreCase("global"))
		{
			objectPath = OTG.getEngine().getGlobalObjectsFolder();
		}
		else {
			objectPath = preset.getPresetDir().resolve(Constants.WORLD_OBJECTS_FOLDER);

		}

		if (!objectPath.toFile().exists())
		{
			if (objectPath.resolve("..").resolve("WorldObjects").toFile().exists())
			{
				objectPath = objectPath.resolve("..").resolve("WorldObjects");
			}
		}
		return objectPath;
	}

	public static boolean region(CommandSender source, String[] args)
	{
		if (!(source instanceof Player))
		{
			source.sendMessage("Only players can execute this command");
			return false;
		}
		Player player = ((Player) source);
		if (!playerSelectionMap.containsKey(player))
		{
			playerSelectionMap.put(player, new Region());
		}

		if (args.length == 0)
		{
			source.sendMessage("placeholder help message");
			return true;
		}

		Region region = playerSelectionMap.get(player);
		switch (args[0])
		{
			case "mark":
				region.setPos(player.getLocation());
				source.sendMessage("Position marked");
				return true;
			case "clear":
				region.clear();
				player.sendMessage("Position cleared");
				return true;
			case "shrink":
			case "expand":
				if (region.getLow() == null) {
					source.sendMessage("Please mark two positions before modifying or exporting the region");return true; }
				if (args.length < 3) {
					source.sendMessage("Please specify a direction and an amount to expand by"); return true;}
				String direction = args[1];
				int value = Integer.parseInt(args[2]);
				if (args[0].equalsIgnoreCase("shrink")) value = -value;
				expand(player, direction, value);
				return true;
			default:
				return false;
		}
	}

	public static void expand(Player source, String direction, Integer value)
	{
		Region region = playerSelectionMap.get(source);
		if (region.getLow() == null)
		{
			source.sendMessage("Please mark two positions before modifying or exporting the region");
			return;
		}
		switch (direction)
		{
			case "south": // positive Z
				region.setHighCorner(new BOCreator.Corner(region.high.x, region.high.y, region.high.z + value));
				break;
			case "north": // negative Z
				region.setLowCorner(new BOCreator.Corner(region.low.x, region.low.y, region.low.z - value));
				break;
			case "east": // positive X
				region.setHighCorner(new BOCreator.Corner(region.high.x + value, region.high.y, region.high.z));
				break;
			case "west": // negative X
				region.setLowCorner(new BOCreator.Corner(region.low.x - value, region.low.y, region.low.z));
				break;
			case "up": // positive y
				region.setHighCorner(new BOCreator.Corner(region.high.x, region.high.y + value, region.high.z));
				break;
			case "down": // negative y
				region.setLowCorner(new BOCreator.Corner(region.low.x, region.low.y - value, region.low.z));
				break;
			default:
				source.sendMessage("Unrecognized direction " + direction);
				return;
		}
		source.sendMessage("Region modified");
	}

	protected static Region getRegionFromObject(int x, int y, int z, BO3 bo3)
	{
		ExportCommand.Region region = new ExportCommand.Region();
		BoundingBox box = bo3.getBoundingBox(Rotation.NORTH);
		region.setPos(new BlockPosition(x + box.getMinX(), y + box.getMinY(), z + box.getMinZ()));
		region.setPos(new BlockPosition(
			x + box.getMinX() + box.getWidth(),
			y + box.getMinY() + box.getHeight(),
			z + box.getMinZ() + box.getDepth()));
		return region;
	}

	private static final List<String> flags = Arrays.asList("-a", "-b", "-o");

	public static List<String> tabCompleteExport(HashMap<String, String> strings)
	{
		if (strings.size() > 4) {
			// Return flags
			return flags;
		}
		if (strings.size() == 4)
		{ // Template
			String preset = strings.get("3");
			List<String> list;
			if (preset.equalsIgnoreCase("global"))
			{
				list = OTG.getEngine().getCustomObjectManager().getGlobalObjects().getGlobalTemplates();
			} else {
				list = OTG.getEngine().getCustomObjectManager().getGlobalObjects().getTemplatesForPreset(preset);
			}
			if (list == null) list = new ArrayList<>();
			list = list.stream().map(filterNamesWithSpaces).collect(Collectors.toList());
			list.add("default");
			String s = strings.get("4");
			return StringUtil.copyPartialMatches(s == null ? "" : s, list, new ArrayList<>());
		}
		if (strings.size() == 3)
		{ // Preset
			String s = strings.get("3");
			return StringUtil.copyPartialMatches(s == null ? "" : s, presetNames, new ArrayList<>());
		}
		//if (strings.size() == 2)
		{ // Center block
			// TODO: tab complete the block parameter
			//return StringUtil.copyPartialMatches(strings.get("2"), );
		}
		// Name - no suggestions
		return new ArrayList<>();
	}

	// if a name includes a space, we wrap it in quotes
	protected static final Function<String, String> filterNamesWithSpaces = (name -> name.contains(" ") ? "\"" + name + "\"" : name);

	protected static Set<String> presetNames = OTG.getEngine().getPresetLoader().getAllPresetNames().stream()
		.map(filterNamesWithSpaces).collect(Collectors.toSet());

	static {
		presetNames.add("global");
	}

	private static final List<String> directions = Arrays.asList("down", "east", "north", "south", "up", "west");
	private static final List<String> regionSubCommands = Arrays.asList("clear","expand", "mark", "shrink");

	public static List<String> tabCompleteRegion(String[] strings)
	{
		if (strings.length == 2)
		{
			return StringUtil.copyPartialMatches(strings[1], regionSubCommands, new ArrayList<>());
		}

		if (strings[1].equalsIgnoreCase("expand") || strings[1].equalsIgnoreCase("shrink")) {
			if (strings.length == 3)
			{
				return StringUtil.copyPartialMatches(strings[2], directions, new ArrayList<>());
			}
		}

		return new ArrayList<>();
	}

	public static class Region
	{
		private BOCreator.Corner low = null;
		private BOCreator.Corner high = null;
		private final BlockPosition[] posArr = new BlockPosition[2];

		public Region()
		{
			posArr[0] = null;
			posArr[1] = null;
		}

		public void setPos(Location loc)
		{
			setPos(new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
		}

		public void setPos(BlockPosition blockPos)
		{
			if (posArr[0] == null) posArr[0] = blockPos;
			else if (posArr[1] == null)
			{
				posArr[1] = blockPos;
				updateCorners();
			}
			else
			{
				posArr[0] = posArr[1];
				posArr[1] = blockPos;
				updateCorners();
			}
		}

		public void clear()
		{
			posArr[0] = null;
			posArr[1] = null;
			low = null;
			high = null;
		}

		public BOCreator.Corner getLow()
		{
			return low;
		}

		public BOCreator.Corner getHigh()
		{
			return high;
		}

		protected void setLowCorner(BOCreator.Corner newCorner)
		{
			this.low = newCorner;
		}

		protected void setHighCorner(BOCreator.Corner newCorner)
		{
			this.high = newCorner;
		}

		private void updateCorners()
		{
			low = new BOCreator.Corner(
				Math.min(posArr[0].getX(), posArr[1].getX()),
				Math.min(posArr[0].getY(), posArr[1].getY()),
				Math.min(posArr[0].getZ(), posArr[1].getZ())
			);
			high = new BOCreator.Corner(
				Math.max(posArr[0].getX(), posArr[1].getX()),
				Math.max(posArr[0].getY(), posArr[1].getY()),
				Math.max(posArr[0].getZ(), posArr[1].getZ())
			);
		}
	}
}
