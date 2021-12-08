package com.pg85.otg.forge.commands;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.pg85.otg.core.OTG;
import com.pg85.otg.core.presets.Preset;
import com.pg85.otg.core.objectcreator.ObjectCreator;
import com.pg85.otg.customobject.util.ObjectType;
import com.pg85.otg.customobject.structures.StructuredCustomObject;
import com.pg85.otg.customobject.util.Corner;
import com.pg85.otg.forge.commands.RegionCommand.Region;
import com.pg85.otg.forge.commands.arguments.PresetArgument;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.forge.util.ForgeNBTHelper;
import com.pg85.otg.util.materials.LocalMaterials;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.materials.LocalMaterialData;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;

public class ExportCommand extends BaseCommand
{
	public static HashMap<Entity, CommandOptions> configMap = new HashMap<>();
	protected static class CommandOptions {
		String preset = OTG.getEngine().getPresetLoader().getDefaultPresetFolderName();
		String template = "default";
		ObjectType objectType = ObjectType.BO3;
		BlockState centerBlock = null;
		boolean overwrite = false;
		boolean exportToGlobal = false;
		HashSet<LocalMaterialData> filter = new HashSet<>(List.of(LocalMaterials.AIR));
	}
	
	public ExportCommand() 
	{
		super("export");
		this.helpMessage = "Allows you to export an area as a BO3 or BO4.";
		this.usage = "Please see /otg export help.";
	}
	
	@Override
	public void build(LiteralArgumentBuilder<CommandSourceStack> builder)
	{
		builder.then(
			Commands.literal("export").executes(this::execute).then(
				Commands.argument("name", StringArgumentType.string()).executes(this::execute).then(
					Commands.argument("type", StringArgumentType.word()).executes(this::execute)
					.suggests((context, suggestionBuilder) -> suggestTypes(context, suggestionBuilder, false)).then(
						Commands.argument("preset", StringArgumentType.string()).executes(this::execute)
						.suggests((context, suggestionBuilder) -> PresetArgument.suggest(context, suggestionBuilder, true)).then(
							Commands.argument("template", StringArgumentType.string())
							.suggests((context, suggestionsBuilder) -> suggestTemplate(
								suggestionsBuilder, context.getArgument("preset", String.class))
							).executes(this::execute)
						)
					)
				)
			).then(
				Commands.literal("help").executes(context -> helpMessage(context.getSource()))
			)
		);
	}

	public int execute(CommandContext<CommandSourceStack> context)
	{
		CommandSourceStack source = context.getSource();
		try
		{
			if (!(source.getEntity() instanceof ServerPlayer playerEntity))
			{
				source.sendSuccess(new TextComponent("Only players can execute this command"), false);
				return 0;
			}

			if (!configMap.containsKey(playerEntity))
			{
				configMap.put(playerEntity, new CommandOptions());
			}
			CommandOptions options = configMap.get(playerEntity);

			// Extract here; this is kinda complex, would be messy in OTGCommand
			String objectName = "";
			BlockState centerBlockState = options.centerBlock;
			String presetName = options.preset;
			ObjectType type = options.objectType;
			String templateName = options.template;
			boolean overwrite = options.overwrite;
			boolean isGlobal = options.exportToGlobal;

			try
			{
				objectName = context.getArgument("name", String.class);
				type = ObjectType.valueOf(context.getArgument("type", String.class).toUpperCase(Locale.ROOT));
				presetName = context.getArgument("preset", String.class);
				templateName = context.getArgument("template", String.class);
			}
			catch (IllegalArgumentException ignored) {} // We can deal with any of these not being there

			if (presetName.equalsIgnoreCase("global"))
			{
				// Set folder name to default, in case we need fallback settings
				presetName = OTG.getEngine().getPresetLoader().getDefaultPresetFolderName();
				isGlobal = true;
			}

			if (objectName.equalsIgnoreCase(""))
			{
				source.sendSuccess(new TextComponent("Usage: /otg export <object name> (center block) [preset] [type] [template] [-a -b -o]").withStyle(ChatFormatting.LIGHT_PURPLE), false);
				source.sendSuccess(new TextComponent("Do /otg export help for more info"), false);
				return 0;
			}

			if (type == ObjectType.BO2)
			{
				source.sendSuccess(new TextComponent("Cannot export BO2 objects"), false);
				return 0;
			}

			Region region = null;
			if (OTG.getEngine().getModLoadedChecker().isModLoaded("worldedit"))
			{
				region = WorldEditUtil.getRegionFromPlayer(playerEntity);
			}

			if (region == null)
			{
				region = RegionCommand.playerSelectionMap.get(source.getEntity());
			}

			if (region == null || region.getMin() == null || region.getMax() == null)
			{
				source.sendSuccess(new TextComponent("Please define a region with /otg region mark, or worldedit"), false);
				return 0;
			}

			Preset preset = ObjectUtils.getPresetOrDefault(presetName);
			if (preset == null)
			{
				source.sendSuccess(new TextComponent("Could not find preset " + presetName), false);
				return 0;
			}

			Path objectPath = ObjectUtils.getObjectFolderPath(isGlobal ? null : preset.getPresetFolder());

			if (!overwrite && new File(objectPath.toFile(), objectName + ".bo3").exists())
			{
				if (new File(objectPath.toFile(), objectName + "." + type.getType()).exists())
				{
					source.sendSuccess(new TextComponent("File already exists, run command with flag '-o' to overwrite"), false);
					return 0;
				}
			}

			Corner lowCorner = region.getMin();
			Corner highCorner = region.getMax();
			Corner center = region.getCenter() != null ? region.getCenter() :
				new Corner(
					(highCorner.x - lowCorner.x) / 2 + lowCorner.x,
					Math.min(lowCorner.y, highCorner.y),
					(highCorner.z - lowCorner.z) / 2 + lowCorner.z);

			// Fetch template or default settings
			File templateFile = OTG.getEngine().getCustomObjectManager().getGlobalObjects().getTemplateFileForPreset(
				presetName, templateName, OTG.getEngine().getLogger(), OTG.getEngine().getOTGRootFolder());
			StructuredCustomObject template = (StructuredCustomObject) OTG.getEngine().getCustomObjectManager().getObjectLoaders()
				.get(type.getType().toLowerCase())
				.loadFromFile(templateName,
					templateFile != null ? templateFile : new File(type.getFileNameForTemplate(templateName)),
					OTG.getEngine().getLogger()
				);

			// Initialize the settings
			if (!template.onEnable(preset.getFolderName(), OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(),
				OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getPresetLoader().getMaterialReader(preset.getFolderName()),
				OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker()))
			{
				source.sendSuccess(new TextComponent("Failed to load template \"" + templateName + "\""), false);
				return 0;
			}

			// Create a new BO from our settings
			LocalMaterialData centerBlock = centerBlockState == null ? null : ForgeMaterialData.ofBlockState(centerBlockState);
			StructuredCustomObject object;

			object = ObjectCreator.create(
					type,
					lowCorner,
					highCorner,
					center,
					centerBlock,
					objectName,
					ObjectUtils.isOutsideBounds(region, type),
					objectPath,
					ObjectUtils.getWorldGenRegion(preset, source.getLevel()),
					new ForgeNBTHelper(),
					null,
					template.getConfig(),
					preset.getFolderName(),
					options.filter
			);

			// Send feedback, and register the BO3 for immediate use
			if (object != null)
			{
				source.sendSuccess(new TextComponent("Successfully created " + type.getType() + " " + objectName), false);
				if (isGlobal)
				{
					OTG.getEngine().getCustomObjectManager().registerGlobalObject(object, object.getConfig().getFile());
				} else {
					OTG.getEngine().getCustomObjectManager().getGlobalObjects()
						.addObjectToPreset(preset.getFolderName(), object.getName().toLowerCase(Locale.ROOT), object.getConfig().getFile(), object);
				}
			} else {
				source.sendSuccess(new TextComponent("Failed to create " + type.getType() + " " + objectName), false);
			}
		} catch (Exception e) {
			source.sendSuccess(new TextComponent("Something went wrong, please check the logs"), false);
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, "Error during export command: "+e.getClass().getName());
			OTG.getEngine().getLogger().printStackTrace(LogLevel.ERROR, LogCategory.MAIN, e);
		}

		return 0;
	}

	public int helpMessage(CommandSourceStack source)
	{
		source.sendSuccess(new TextComponent("Usage: /otg export <object name> (center block) [preset] [type] [template] [-a -b -o]").withStyle(ChatFormatting.LIGHT_PURPLE), false);
		source.sendSuccess(new TextComponent(" - Object name is the only required argument"), false);
		source.sendSuccess(new TextComponent(" - Center block is optional; if set, the center will be set to the first found instance of the given block."), false);
		source.sendSuccess(new TextComponent(" - Preset determines where the object is saved, and where templates are searched for"), false);
		source.sendSuccess(new TextComponent(" - Type is either BO3 or BO4"), false);
		source.sendSuccess(new TextComponent(" - Template is a BO3 file whose settings are used for the exported object"), false);
		source.sendSuccess(new TextComponent("    - Templates have file ending .BO3Template or .BO4Template"), false);
		source.sendSuccess(new TextComponent("    - Templates are not loaded as objects"), false);
		source.sendSuccess(new TextComponent(" - There are three flags; -a for Air blocks, -b for Branches, -o for Override"), false);
		return 0;
	}
	
	public static CompletableFuture<Suggestions> suggestTypes(CommandContext<CommandSourceStack> context,
			SuggestionsBuilder builder, boolean includeBO2)
	{
		Set<String> set = Stream.of(ObjectType.values())
				.map(ObjectType::getType)
				.collect(Collectors.toSet());
			if (!includeBO2)
			{
				set.remove("BO2");
			}
			return SharedSuggestionProvider.suggest(set, builder);
	}

	private static final Function<String, String> filterNamesWithSpaces = (name -> name.contains(" ") ? "\"" + name + "\"" : name);

	public static CompletableFuture<Suggestions> suggestTemplate(SuggestionsBuilder builder, String preset)
	{
		List<String> list;
		// Get global objects if global, else fetch based on preset
		if (preset.equalsIgnoreCase("global"))
		{
			list = OTG.getEngine().getCustomObjectManager().getGlobalObjects()
				.getGlobalTemplates(OTG.getEngine().getLogger(), OTG.getEngine().getOTGRootFolder());
		}
		else
		{
			list = OTG.getEngine().getCustomObjectManager().getGlobalObjects()
				.getTemplatesForPreset(preset, OTG.getEngine().getLogger(), OTG.getEngine().getOTGRootFolder());
		}
		if (list == null) list = new ArrayList<>();
		list = list.stream().map(filterNamesWithSpaces).collect(Collectors.toList());
		list.add("default");
		return SharedSuggestionProvider.suggest(list.stream(), builder);
	}
}
