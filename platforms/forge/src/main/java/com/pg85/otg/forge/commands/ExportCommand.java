package com.pg85.otg.forge.commands;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.pg85.otg.OTG;
import com.pg85.otg.customobject.creator.ObjectCreator;
import com.pg85.otg.customobject.creator.ObjectType;
import com.pg85.otg.customobject.structures.StructuredCustomObject;
import com.pg85.otg.customobject.util.Corner;
import com.pg85.otg.forge.commands.RegionCommand.Region;
import com.pg85.otg.forge.commands.arguments.FlagsArgument;
import com.pg85.otg.forge.commands.arguments.PresetArgument;
import com.pg85.otg.forge.gen.ForgeWorldGenRegion;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.forge.util.ForgeNBTHelper;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.nbt.LocalNBTHelper;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.materials.LocalMaterialData;

import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class ExportCommand extends BaseCommand
{
	private static final String[] FLAGS = new String[]
	{ "-o", "-a", "-b" };
	
	public ExportCommand() 
	{
		super("export");
		this.helpMessage = "Allows you to export an area as a BO3 or BO4.";
		this.usage = "Please see /otg export help.";
	}
	
	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder)
	{
		builder.then(
			Commands.literal("export").executes(this::execute).then(
				Commands.argument("name", StringArgumentType.string()).executes(this::execute).then(
					// Skip center block
					Commands.argument("preset", StringArgumentType.string()).executes(this::execute)
					.suggests((context, suggestionBuilder) -> PresetArgument.suggest(context, suggestionBuilder, true)).then(
						Commands.argument("type", StringArgumentType.word()).executes(this::execute)
						.suggests((context, suggestionBuilder) -> suggestTypes(context, suggestionBuilder, false)).then(
							Commands.argument("template", new TemplateArgument()).executes(this::execute).then(
								Commands.argument("flags", FlagsArgument.create()).executes(this::execute).suggests(this::suggestFlags)
							)
						)
					)
				).then(
					Commands.argument("center", BlockStateArgument.block()).executes(this::execute).then(
						Commands.argument("preset", StringArgumentType.string()).executes(this::execute)
						.suggests((context, suggestionBuilder) -> PresetArgument.suggest(context, suggestionBuilder, true)).then(
							Commands.argument("type", StringArgumentType.word()).executes(this::execute)
							.suggests((context, suggestionBuilder) -> suggestTypes(context, suggestionBuilder, false)).then(
								Commands.argument("template", new TemplateArgument()).executes(this::execute).then(
									Commands.argument("flags", FlagsArgument.create()).executes(this::execute).suggests(this::suggestFlags)
								)
							)
						)
					)
				)
			).then(
				Commands.literal("help").executes(context -> helpMessage(context.getSource()))
			)
		);
	}

	public int execute(CommandContext<CommandSource> context)
	{
		CommandSource source = context.getSource();
		try
		{
			if (!(source.getEntity() instanceof ServerPlayerEntity))
			{
				source.sendSuccess(new StringTextComponent("Only players can execute this command"), false);
				return 0;
			}
			ServerPlayerEntity playerEntity = (ServerPlayerEntity) source.getEntity();

			// Extract here; this is kinda complex, would be messy in OTGCommand
			String objectName = "";
			BlockState centerBlockState;
			String presetName = null;
			ObjectType type = ObjectType.BO3; // Defaults to BO3 for simplicity
			String templateName = "default";
			boolean overwrite = false, isStructure = false, includeAir = false, isGlobal = false;

			try
			{
				centerBlockState = context.getArgument("center", BlockStateInput.class).getState();
			}
			catch (IllegalArgumentException ex)
			{
				centerBlockState = null;
			}

			try
			{
				objectName = context.getArgument("name", String.class);
				presetName = context.getArgument("preset", String.class);
				String raw = context.getArgument("type", String.class);
				type = ObjectType.valueOf(raw.toUpperCase(Locale.ROOT));
				templateName = context.getArgument("template", String.class);
				// Flags as a string - easiest and clearest way I've found of adding multiple boolean flags
				String flags = context.getArgument("flags", String.class);

				overwrite = flags.contains("-o");
				isStructure = flags.contains("-b");
				includeAir = flags.contains("-a");
			}
			catch (IllegalArgumentException ignored)
			{} // We can deal with any of these not being there

			if (presetName == null || presetName.equalsIgnoreCase("global"))
			{
				// Set folder name to default, in case we need fallback settings
				presetName = OTG.getEngine().getPresetLoader().getDefaultPresetFolderName();
				isGlobal = true;
			}

			if (objectName.equalsIgnoreCase(""))
			{
				source.sendSuccess(new StringTextComponent("Usage: /otg export <object name> (center block) [preset] [type] [template] [-a -b -o]").withStyle(TextFormatting.LIGHT_PURPLE), false);
				source.sendSuccess(new StringTextComponent("Do /otg export help for more info"), false);
				return 0;
			}

			if (type == ObjectType.BO2)
			{
				source.sendSuccess(new StringTextComponent("Cannot export BO2 objects"), false);
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
				source.sendSuccess(new StringTextComponent("Please define a region with /otg region mark, or worldedit"), false);
				return 0;
			}

			if (ObjectUtils.isOutsideBounds(region, type))
			{
				isStructure = true;
			}

			Preset preset = ObjectUtils.getPresetOrDefault(presetName);
			if (preset == null)
			{
				source.sendSuccess(new StringTextComponent("Could not find preset " + (presetName == null ? "" : presetName)), false);
				return 0;
			}

			Path objectPath = ObjectUtils.getObjectFolderPath(isGlobal ? null : preset.getPresetFolder());

			if (!overwrite && new File(objectPath.toFile(), objectName + ".bo3").exists())
			{
				if (new File(objectPath.toFile(), objectName + "." + type.getType()).exists())
				{
					source.sendSuccess(new StringTextComponent("File already exists, run command with flag '-o' to overwrite"), false);
					return 0;
				}
			}

			ForgeWorldGenRegion worldGenRegion = ObjectUtils.getWorldGenRegion(preset, source.getLevel());

			LocalNBTHelper nbtHelper = new ForgeNBTHelper();
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
				source.sendSuccess(new StringTextComponent("Failed to load template \"" + templateName + "\""), false);
				return 0;
			}

			// Create a new BO from our settings
			LocalMaterialData centerBlock = centerBlockState == null ? null : ForgeMaterialData.ofBlockState(centerBlockState);

			StructuredCustomObject object = ObjectCreator.create(
				type,
				lowCorner,
				highCorner,
				center,
				centerBlock,
				objectName,
				includeAir,
				isStructure,
				false,
				objectPath,
				worldGenRegion,
				nbtHelper,
				null,
				template.getConfig(),
				preset.getFolderName(),
				OTG.getEngine().getOTGRootFolder(),
				OTG.getEngine().getLogger(),
				OTG.getEngine().getCustomObjectManager(),
				OTG.getEngine().getPresetLoader().getMaterialReader(preset.getFolderName()),
				OTG.getEngine().getCustomObjectResourcesManager(),
				OTG.getEngine().getModLoadedChecker()
			);

			// Send feedback, and register the BO3 for immediate use
			if (object != null)
			{
				source.sendSuccess(new StringTextComponent("Successfully created " + type.getType() + " " + objectName), false);
				if (isGlobal)
				{
					OTG.getEngine().getCustomObjectManager().registerGlobalObject(object, object.getConfig().getFile());
				} else {
					OTG.getEngine().getCustomObjectManager().getGlobalObjects()
						.addObjectToPreset(preset.getFolderName(), object.getName().toLowerCase(Locale.ROOT), object.getConfig().getFile(), object);
				}
			} else {
				source.sendSuccess(new StringTextComponent("Failed to create " + type.getType() + " " + objectName), false);
			}
		} catch (Exception e) {
			source.sendSuccess(new StringTextComponent("Something went wrong, please check the logs"), false);
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, "Error during export command: "+e.getClass().getName());
			OTG.getEngine().getLogger().printStackTrace(LogLevel.ERROR, LogCategory.MAIN, e);
		}

		return 0;
	}

	public int helpMessage(CommandSource source)
	{
		source.sendSuccess(new StringTextComponent("Usage: /otg export <object name> (center block) [preset] [type] [template] [-a -b -o]").withStyle(TextFormatting.LIGHT_PURPLE), false);
		source.sendSuccess(new StringTextComponent(" - Object name is the only required argument"), false);
		source.sendSuccess(new StringTextComponent(" - Center block is optional; if set, the center will be set to the first found instance of the given block."), false);
		source.sendSuccess(new StringTextComponent(" - Preset determines where the object is saved, and where templates are searched for"), false);
		source.sendSuccess(new StringTextComponent(" - Type is either BO3 or BO4"), false);
		source.sendSuccess(new StringTextComponent(" - Template is a BO3 file whose settings are used for the exported object"), false);
		source.sendSuccess(new StringTextComponent("    - Templates have file ending .BO3Template or .BO4Template"), false);
		source.sendSuccess(new StringTextComponent("    - Templates are not loaded as objects"), false);
		source.sendSuccess(new StringTextComponent(" - There are three flags; -a for Air blocks, -b for Branches, -o for Override"), false);
		return 0;
	}
	
	private CompletableFuture<Suggestions> suggestTypes(CommandContext<CommandSource> context,
			SuggestionsBuilder builder, boolean includeBO2)
	{
		Set<String> set = Stream.of(ObjectType.values())
				.map(ObjectType::getType)
				.collect(Collectors.toSet());
			if (!includeBO2)
			{
				set.remove("BO2");
			}
			return ISuggestionProvider.suggest(set, builder);
	}
	
	private CompletableFuture<Suggestions> suggestFlags(CommandContext<CommandSource> context,
			SuggestionsBuilder builder)
	{
		return ISuggestionProvider.suggest(FLAGS, builder);
	}
	
	private static final Function<String, String> filterNamesWithSpaces = (name -> name.contains(" ") ? "\"" + name + "\"" : name);
	
	private static class TemplateArgument implements ArgumentType<String>
	{
		@Override
		public String parse(StringReader reader) throws CommandSyntaxException
		{
			return reader.readString();
		}

		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
		{
			String preset = context.getArgument("preset", String.class);
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
			return ISuggestionProvider.suggest(list.stream(), builder);
		}
	}
}
