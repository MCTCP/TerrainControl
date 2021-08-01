package com.pg85.otg.spigot.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pg85.otg.OTG;
import com.pg85.otg.customobject.creator.ObjectCreator;
import com.pg85.otg.customobject.creator.ObjectType;
import com.pg85.otg.customobject.structures.StructuredCustomObject;
import com.pg85.otg.customobject.util.Corner;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.spigot.commands.RegionCommand.Region;
import com.pg85.otg.spigot.gen.SpigotWorldGenRegion;
import com.pg85.otg.spigot.materials.SpigotMaterialData;
import com.pg85.otg.spigot.util.SpigotNBTHelper;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.nbt.LocalNBTHelper;
import net.minecraft.server.v1_16_R3.ArgumentTile;
import net.minecraft.server.v1_16_R3.IBlockData;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class ExportCommand extends BaseCommand
{
	protected static final Function<String, String> filterNamesWithSpaces = (name) -> name.contains(" ") ? "\"" + name + "\"" : name;
	private static final List<String> flags = Arrays.asList("-a", "-b", "-o");
	protected static Set<String> presetNames = OTG.getEngine().getPresetLoader().getAllPresetFolderNames().stream().map(filterNamesWithSpaces).collect(Collectors.toSet());
	protected static List<String> objectTypes = Arrays.asList("BO3", "BO4");

	static
	{
		presetNames.add("global");
	}

	public ExportCommand()
	{
		this.name = "export";
		this.helpMessage = "Allows you to export an area as a BO3 or BO4.";
		this.usage = "Please see /otg help export.";
	}

	public List<String> onTabComplete(CommandSender sender, String[] args)
	{
		Map<String, String> strings = CommandUtil.parseArgs(args, true);
		if (strings.size() > 5)
		{
			return flags;
		}
		String str;
		if (strings.size() == 4)
		{
			str = strings.get("4");
			return StringUtil.copyPartialMatches(str == null ? "" : str, objectTypes, new ArrayList<>());
		}
		else if (strings.size() == 5)
		{
			str = strings.get("5");
			if (str == null)
			{
				str = "";
			}

			List<String> list;
			if (str.equalsIgnoreCase("global"))
			{
				list = OTG.getEngine().getCustomObjectManager().getGlobalObjects().getGlobalTemplates(OTG.getEngine().getLogger(), OTG.getEngine().getOTGRootFolder());
			}
			else
			{
				list = OTG.getEngine().getCustomObjectManager().getGlobalObjects().getTemplatesForPreset(str, OTG.getEngine().getLogger(), OTG.getEngine().getOTGRootFolder());
			}

			if (list == null)
			{
				list = new ArrayList<>();
			}

			list = list.stream().map(filterNamesWithSpaces).collect(Collectors.toList());
			list.add("default");
			return StringUtil.copyPartialMatches(str, list, new ArrayList<>());
		}
		else if (strings.size() == 3)
		{
			str = strings.get("3");
			return StringUtil.copyPartialMatches(str == null ? "" : str, presetNames, new ArrayList<>());
		}
		else
		{
			return new ArrayList<>();
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
			this.helpMessage(source);
			return true;
		}
		String objectName = args[0];
		IBlockData centerBlockState = null;

		try
		{
			if (args.length >= 2)
			{
				if (!args[1].equalsIgnoreCase("any"))
					centerBlockState = ArgumentTile.a().parse(new StringReader(args[1])).a();
			}
		}
		catch (CommandSyntaxException var25)
		{
			sender.sendMessage("Could not find material " + args[1]);
			return true;
		}

		String presetName = args.length > 2 && !args[2].equalsIgnoreCase("global") ? args[2] : null;
		boolean isGlobal = presetName == null;
		ObjectType type = args.length >= 4 ? ObjectType.valueOf(args[3]) : ObjectType.BO3;
		String templateName = args.length >= 5 ? args[4] : "default";
		String flags = args.length >= 6 ? String.join(" ", Arrays.copyOfRange(args, 5, args.length)) : "";
		boolean overwrite = flags.contains("-o");
		boolean isStructure = flags.contains("-b");
		boolean includeAir = flags.contains("-a");
		if (type == ObjectType.BO2)
		{
			source.sendMessage("Cannot export BO2 objects");
			return true;
		}
		Region region = RegionCommand.playerSelectionMap.get(source);
		if (region == null || region.getMin() == null || region.getMax() == null)
		{
			source.sendMessage("Please mark two corners with /otg region mark");
			return true;
		}
		if (ObjectUtils.isOutsideBounds(region, type))
		{
			isStructure = true;
		}

		Preset preset = ObjectUtils.getPresetOrDefault(presetName);
		if (preset == null)
		{
			source.sendMessage("Could not find preset " + (presetName == null ? "" : presetName));
			return true;
		}
		Path objectPath = ObjectUtils.getObjectFolderPath(isGlobal ? null : preset.getPresetFolder());
		if (!overwrite && (new File(objectPath.toFile(), objectName + ".bo3")).exists() && (new File(objectPath.toFile(), objectName + "." + type.getType())).exists())
		{
			source.sendMessage("File already exists, run command with flag '-o' to overwrite");
			return true;
		}
		SpigotWorldGenRegion worldGenRegion = ObjectUtils.getWorldGenRegion(preset, (CraftWorld) source.getWorld());
		LocalNBTHelper nbtHelper = new SpigotNBTHelper();
		Corner lowCorner = region.getMin();
		Corner highCorner = region.getMax();
		Corner center = region.getCenter() != null ? region.getCenter() : new Corner((highCorner.x - lowCorner.x) / 2 + lowCorner.x, Math.min(lowCorner.y, highCorner.y), (highCorner.z - lowCorner.z) / 2 + lowCorner.z);

		File templateFile = OTG.getEngine().getCustomObjectManager().getGlobalObjects()
			.getTemplateFileForPreset(presetName, templateName, OTG.getEngine().getLogger(), OTG.getEngine().getOTGRootFolder());
		StructuredCustomObject template = (StructuredCustomObject) OTG.getEngine().getCustomObjectManager().getObjectLoaders().get(type.getType().toLowerCase()).loadFromFile(
			templateName,
			templateFile != null ?
			templateFile :
			new File(type.getFileNameForTemplate(templateName)),
			OTG.getEngine().getLogger());

		if (!template.onEnable(preset.getFolderName(), OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getPresetLoader().getMaterialReader(preset.getFolderName()), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker()))
		{
			source.sendMessage("Failed to load template \"" + templateName + "\"");
			return true;
		}

		LocalMaterialData centerBlock = centerBlockState == null ? null : SpigotMaterialData.ofBlockData(centerBlockState);
		StructuredCustomObject object = ObjectCreator.create(type, lowCorner, highCorner, center, centerBlock, objectName, includeAir, isStructure, objectPath, worldGenRegion, nbtHelper, null, template.getConfig(), preset.getFolderName(), OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getPresetLoader().getMaterialReader(preset.getFolderName()), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());
		if (object != null)
		{
			source.sendMessage("Successfully created " + type.getType() + " " + objectName);
			if (isGlobal)
			{
				OTG.getEngine().getCustomObjectManager().registerGlobalObject(object, object.getConfig().getFile());
			}
			else
			{
				OTG.getEngine().getCustomObjectManager().getGlobalObjects().addObjectToPreset(preset.getFolderName(), object.getName().toLowerCase(Locale.ROOT), object.getConfig().getFile(), object);
			}
		}
		else
		{
			source.sendMessage("Failed to create " + type.getType() + " " + objectName);
		}

		return true;
	}

	public void helpMessage(Player source)
	{
		source.sendMessage(ChatColor.LIGHT_PURPLE + "Usage: /otg export <object name> [center block] [preset] [type] [template] [-a -b -o]");
		source.sendMessage(" - Object name is the only required argument");
		source.sendMessage(" - Center block is optional; if set, the center will be set to the first found instance of the given block.");
		source.sendMessage(" - Preset determines where the object is saved, and where templates are searched for");
		source.sendMessage(" - Type is either BO3 or BO4");
		source.sendMessage(" - Template is a BO3 file whose settings are used for the exported object");
		source.sendMessage("    - Templates have file ending .BO3Template or .BO4Template");
		source.sendMessage("    - Templates are not loaded as objects");
		source.sendMessage(" - There are three flags; -a for Air blocks, -b for Branches, -o for Override");
	}
}
