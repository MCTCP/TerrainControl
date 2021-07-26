package com.pg85.otg.forge.commands;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.pg85.otg.OTG;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobject.creator.ObjectCreator;
import com.pg85.otg.customobject.creator.ObjectType;
import com.pg85.otg.customobject.structures.StructuredCustomObject;
import com.pg85.otg.customobject.util.Corner;
import com.pg85.otg.forge.commands.arguments.FlagsArgument;
import com.pg85.otg.forge.commands.arguments.PresetArgument;
import com.pg85.otg.forge.gen.ForgeWorldGenRegion;
import com.pg85.otg.forge.gen.MCWorldGenRegion;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.forge.util.ForgeNBTHelper;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.bo3.LocalNBTHelper;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.materials.LocalMaterialData;

import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class ExportCommand extends BaseCommand
{
	protected static HashMap<Entity, Region> playerSelectionMap = new HashMap<>();
	
	private static final String[] FLAGS = new String[]
	{ "-o", "-a", "-b" };
	
	public ExportCommand() {
		this.name = "export";
		this.helpMessage = "Allows you to export an area as a BO3 or BO4.";
		this.usage = "Please see /otg help export.";
	}
	
	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder)
	{
		builder.then(
			Commands.literal("export").executes(this::execute).then(
				Commands.argument("name", StringArgumentType.string()).executes(this::execute).then(
					// Skip center block
					Commands.argument("preset", StringArgumentType.word()).executes(this::execute)
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
						Commands.argument("preset", StringArgumentType.word()).executes(this::execute)
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
		).then(
			Commands.literal("region").then(
				Commands.literal("mark").executes(
					context -> mark(context.getSource())
				).then(Commands.argument("point",new RegionMarkerArgument()).executes(
					context -> mark(context.getSource(), context.getArgument("point", String.class))
				))
			).then(
				Commands.literal("clear").executes(
					context ->clear(context.getSource())
				)
			).then(
				Commands.literal("expand").then(
					Commands.argument("direction", new DirectionArgument(true)).then(
						Commands.argument("value", IntegerArgumentType.integer()).executes(
							context -> expand(context.getSource(),
								context.getArgument("direction", String.class),
								context.getArgument("value", Integer.class))
						)
					)
				)
			).then(
				Commands.literal("shrink").then(
					Commands.argument("direction", new DirectionArgument(true)).then(
						Commands.argument("value", IntegerArgumentType.integer()).executes(
							context -> shrink(context.getSource(),
								context.getArgument("direction", String.class),
								context.getArgument("value", Integer.class))
						)
					)
				)
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
				templateName = context.getArgument("template", String.class);
				// Flags as a string - easiest and clearest way I've found of adding multiple boolean flags
				String flags = context.getArgument("flags", String.class);
				overwrite = flags.contains("-o");
				isStructure = flags.contains("-b");
				includeAir = flags.contains("-a");
			}
			catch (IllegalArgumentException ignored)
			{} // We can deal with any of these not being there
			
			String raw = context.getArgument("type", String.class);
			try
			{
				type = ObjectType.valueOf(raw);
			}
			catch (IllegalArgumentException ex)
			{
				source.sendFailure(new StringTextComponent("Invalid object type: " + raw));
				return 0;
			}

			if (presetName == null || presetName.equalsIgnoreCase("global"))
			{
				// Set folder name to default, in case we need fallback settings
				presetName = OTG.getEngine().getPresetLoader().getDefaultPresetFolderName();
				isGlobal = true;
			}

			if (objectName.equalsIgnoreCase(""))
			{
				//source.sendSuccess(new StringTextComponent("Please specify a name for the object"), false);
				source.sendSuccess(new StringTextComponent("Usage: /otg export <object name> (center block) [preset] [type] [template] [-a -b -o]").withStyle(TextFormatting.LIGHT_PURPLE), false);
				source.sendSuccess(new StringTextComponent("Do /otg export help for more info"), false);
				return 0;
			}

			if (type == ObjectType.BO2)
			{
				source.sendSuccess(new StringTextComponent("Cannot export BO2 objects"), false);
				return 0;
			}

			Region region = playerSelectionMap.get(source.getEntity());
			if (region == null || region.getMin() == null)
			{
				source.sendSuccess(new StringTextComponent("Please mark two corners with /otg region mark"), false);
				return 0;
			}

			if (EditCommand.isOutsideBounds(region, type))
			{
				isStructure = true;
			}

			Preset preset = getPresetOrDefault(presetName);
			if (preset == null)
			{
				source.sendSuccess(new StringTextComponent("Could not find preset " + (presetName == null ? "" : presetName)), false);
				return 0;
			}

			Path objectPath = getObjectPath(isGlobal ? null : preset.getPresetFolder());

			if (!overwrite)
			{
				if (new File(objectPath.toFile(), objectName + "." + type.getType()).exists())
				{
					source.sendSuccess(new StringTextComponent("File already exists, run command with flag '-o' to overwrite"), false);
					return 0;
				}
			}

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

			LocalNBTHelper nbtHelper = new ForgeNBTHelper();
			Corner lowCorner = region.getMin();
			Corner highCorner = region.getMax();
			Corner center = region.getCenter() != null ? region.getCenter() :
				new Corner(
					(highCorner.x - lowCorner.x) / 2 + lowCorner.x,
					Math.min(lowCorner.y, highCorner.y),
					(highCorner.z - lowCorner.z) / 2 + lowCorner.z);

			// Fetch template or default settings
			StructuredCustomObject template = (StructuredCustomObject) OTG.getEngine().getCustomObjectManager().getObjectLoaders()
				.get(type.getType().toLowerCase())
				.loadFromFile(templateName,
					type.getObjectFilePathFromName(
						type.getFileNameForTemplate(templateName),
						objectPath).toFile(),
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
				objectPath,
				genRegion,
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
			source.sendSuccess(new StringTextComponent("Something went wrong, please check logs"), false);
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, "Error during export command: "+e.getClass().getName());
			for (StackTraceElement s : e.getStackTrace())
			{
				OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, s.toString());
			}
		}

		return 0;
	}

	protected static Path getObjectPath(Path presetFolder)
	{
		Path objectPath;
		if (presetFolder == null)
		{
			objectPath = OTG.getEngine().getGlobalObjectsFolder();
		} else {
			objectPath = presetFolder.resolve(Constants.WORLD_OBJECTS_FOLDER);
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

	protected static Preset getPresetOrDefault(String presetFolderName)
	{
		if (presetFolderName == null)
		{
			return OTG.getEngine().getPresetLoader().getPresetByShortNameOrFolderName(OTG.getEngine().getPresetLoader().getDefaultPresetFolderName());
		} else {
			return OTG.getEngine().getPresetLoader().getPresetByShortNameOrFolderName(presetFolderName);
		}
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

	public int mark(CommandSource source)
	{
		if (checkForNonPlayer(source)) return 0;
		playerSelectionMap.get(source.getEntity()).setPos(source.getEntity().blockPosition());
		source.sendSuccess(new StringTextComponent("Position marked"), false);
		return 0;
	}

	public int mark(CommandSource source, String input)
	{
		if (checkForNonPlayer(source)) return 0;
		Region r = playerSelectionMap.get(source.getEntity());
		switch(input)
		{
			case "min":
			case "pos1":
			case "1":
			{
				r.setPos1(source.getEntity().blockPosition());
				source.sendSuccess(new StringTextComponent("Point 1 marked"), false);
				return 0;
			}
			case "max":
			case "pos2":
			case "2":
			{
				r.setPos2(source.getEntity().blockPosition());
				source.sendSuccess(new StringTextComponent("Point 2 marked"), false);
				return 0;
			}
			case "center":
			{
				r.setCenter(Region.cornerFromBlockPos(source.getEntity().blockPosition()));
				source.sendSuccess(new StringTextComponent("Center marked"), false);
				return 0;
			}
			default:
			{
				source.sendSuccess(new StringTextComponent(input + " is not recognized"), false);
				return 0;
			}
		}
	}

	public int clear(CommandSource source)
	{
		if (checkForNonPlayer(source)) return 0;
		playerSelectionMap.get(source.getEntity()).clear();
		source.sendSuccess(new StringTextComponent("Position cleared"), false);
		return 0;
	}

	public int expand(CommandSource source, String direction, Integer value)
	{
		if (checkForNonPlayer(source)) return 0;
		Region region = playerSelectionMap.get(source.getEntity());
		if (region.getMax() == null)
		{
			source.sendSuccess(new StringTextComponent("Please mark two positions before modifying or exporting the region"), false);
			return 0;
		}

		switch (direction)
		{
			case "south": // positive Z
				if (region.pos2.getZ() >= region.pos1.getZ())
					region.setPos2(region.pos2.south(value));
				else
					region.setPos1(region.pos1.south(value));
				break;
			case "north": // negative Z
				if (region.pos2.getZ() < region.pos1.getZ())
					region.setPos2(region.pos2.north(value));
				else
					region.setPos1(region.pos1.north(value));
				break;
			case "east": // positive X
				if (region.pos2.getX() >= region.pos1.getX())
					region.setPos2(region.pos2.east(value));
				else
					region.setPos1(region.pos1.east(value));
				break;
			case "west": // negative X
				if (region.pos2.getX() < region.pos1.getX())
					region.setPos2(region.pos2.west(value));
				else
					region.setPos1(region.pos1.west(value));
				break;
			case "up": // positive y
				if (region.pos2.getY() >= region.pos1.getY())
					region.setPos2(region.pos2.above(value));
				else
					region.setPos1(region.pos1.above(value));
				break;
			case "down": // negative y
				if (region.pos2.getY() < region.pos1.getY())
					region.setPos2(region.pos2.below(value));
				else
					region.setPos1(region.pos1.below(value));
				break;
			default:
				source.sendSuccess(new StringTextComponent("Unrecognized direction " + direction), false);
				return 0;
		}

		source.sendSuccess(new StringTextComponent("Region modified"), false);
		return 0;
	}

	public int shrink(CommandSource source, String direction, Integer value)
	{
		if (checkForNonPlayer(source)) return 0;

		expand(source, direction, -value);

		return 0;
	}

	private static boolean checkForNonPlayer(CommandSource source)
	{
		if (!(source.getEntity() instanceof ServerPlayerEntity))
		{
			source.sendSuccess(new StringTextComponent("Only players can execute this command"), false);
			return true;
		}
		if (!playerSelectionMap.containsKey(source.getEntity()))
		{
			playerSelectionMap.put(source.getEntity(), new Region());
		}
		return false;
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

	public static class Region
	{
		private BlockPos pos1 = null;
		private BlockPos pos2 = null;
		private Corner center = null;
		private boolean flip = true;

		public void setPos(BlockPos blockPos)
		{
			// alternate between setting min and max
			// Flip initializes as true, meaning we set min first
			if (flip)
				pos1 = blockPos;
			else
				pos2 = blockPos;
			flip = !flip;
		}

		public static Corner cornerFromBlockPos(BlockPos blockPos)
		{
			return new Corner(blockPos.getX(), blockPos.getY(), blockPos.getZ());
		}

		public void clear()
		{
			pos1 = null;
			pos2 = null;
			center = null;
		}

		public Corner getMin()
		{
			return new Corner(
				Math.min(pos1.getX(), pos2.getX()),
				Math.min(pos1.getY(), pos2.getY()),
				Math.min(pos1.getZ(), pos2.getZ())
				);
		}

		public Corner getMax()
		{
			return new Corner(
				Math.max(pos1.getX(), pos2.getX()),
				Math.max(pos1.getY(), pos2.getY()),
				Math.max(pos1.getZ(), pos2.getZ())
			);
		}

		public void setPos1(BlockPos pos)
		{
			flip = false;
			this.pos1 = pos;
		}

		public void setPos2(BlockPos pos)
		{
			flip = true;
			this.pos2 = pos;
		}

		public Corner getCenter()
		{
			return center;
		}

		public void setCenter(Corner center)
		{
			this.center = center;
		}
	}
	
	private static class DirectionArgument implements ArgumentType<String>
	{
		private final String[] options;

		public DirectionArgument(boolean vertical)
		{
			if (vertical) options = new String[]{"north", "south", "east", "west", "up", "down"};
			else options = new String[]{"north", "south", "east", "west"};
		}

		@Override
		public String parse(StringReader reader) throws CommandSyntaxException
		{
			return reader.readString();
		}

		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
		{
			return ISuggestionProvider.suggest(options, builder);
		}
	}

	private static class RegionMarkerArgument implements ArgumentType<String>
	{
		private final String[] options = new String[] {"1", "2", "center"};

		@Override
		public String parse(StringReader reader) throws CommandSyntaxException
		{
			return reader.readString();
		}

		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
		{
			return ISuggestionProvider.suggest(options, builder);
		}
	}
}
