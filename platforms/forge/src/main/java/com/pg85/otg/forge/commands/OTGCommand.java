package com.pg85.otg.forge.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.pg85.otg.OTG;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.BlockStateArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OTGCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(
			Commands.literal("otg").requires(
				(context) -> context.hasPermission(2)
			).executes(
				(context) -> HelpCommand.showHelp(context.getSource())
			).then(
				Commands.literal("help").executes(
					(context) -> HelpCommand.showHelp(context.getSource())
				)
			).then(
				Commands.literal("map").executes(
					(context) -> MapCommand.mapBiomes(context.getSource(), 2048, 2048)
				).then(
					Commands.argument("width", IntegerArgumentType.integer(0)).executes(
						(context) -> MapCommand.mapBiomes(context.getSource(), IntegerArgumentType.getInteger(context, "width"), IntegerArgumentType.getInteger(context, "width"))
					).then(
						Commands.argument("height", IntegerArgumentType.integer(0)).executes(
							(context) -> MapCommand.mapBiomes(context.getSource(), IntegerArgumentType.getInteger(context, "width"), IntegerArgumentType.getInteger(context, "height"))
						)))
			).then(
				Commands.literal("data").then(
					Commands.argument("type", StringArgumentType.word()).executes(
						(context -> DataCommand.execute(context.getSource(), context.getArgument("type", String.class)))
					)
				)
			).then(
				Commands.literal("preset").executes(
					(context -> PresetCommand.showPreset(context.getSource()))
				)
			).then(
				Commands.literal("flush").executes(
					(context -> FlushCommand.flushCache(context.getSource()))
				)
			).then(
				Commands.literal("structure").executes(
					(context -> StructureCommand.showStructureInfo(context.getSource()))
				)				
			).then(					
				Commands.literal("biome").executes(
					(context -> BiomeCommand.showBiome(context.getSource()))
				)
			).then(
				Commands.literal("spawn").then(
					Commands.argument("preset", new PresetArgument()).then(
						Commands.argument("object", new BiomeObjectArgument()).executes(
							context -> SpawnCommand.execute(
								context.getSource(),
								context.getArgument("preset", String.class),
								context.getArgument("object", String.class),
								Objects.requireNonNull(context.getSource().getEntity()).blockPosition())
						).then(
							Commands.argument("location", BlockPosArgument.blockPos())
								.executes(
									(context -> SpawnCommand.execute(
										context.getSource(),
										context.getArgument("preset", String.class),
										context.getArgument("object", String.class),
										BlockPosArgument.getLoadedBlockPos(context, "location")))
								)
						)
					)
				)
			).then(
				Commands.literal("edit").executes(EditCommand::help).then(
					Commands.argument("preset", new PresetArgument()).executes(EditCommand::execute).then(
						Commands.argument("object", new BiomeObjectArgument()).executes(EditCommand::execute).then(
							Commands.argument("flags", FlagsArgument.with("-nofix", "-update")).executes(EditCommand::execute)
						)
					)
				)
			).then(
				Commands.literal("finishedit").executes(EditCommand::finish)
			).then(
				Commands.literal("region").then(
					Commands.literal("mark").executes(
						context -> ExportCommand.mark(context.getSource())
					)
				).then(
					Commands.literal("clear").executes(
						context -> ExportCommand.clear(context.getSource())
					)
				).then(
					Commands.literal("expand").then(
						Commands.argument("direction", new DirectionArgument(true)).then(
							Commands.argument("value", IntegerArgumentType.integer()).executes(
								context -> ExportCommand.expand(context.getSource(),
									context.getArgument("direction", String.class),
									context.getArgument("value", Integer.class))
							)
						)
					)
				).then(
					Commands.literal("shrink").then(
						Commands.argument("direction", new DirectionArgument(true)).then(
							Commands.argument("value", IntegerArgumentType.integer()).executes(
								context -> ExportCommand.shrink(context.getSource(),
									context.getArgument("direction", String.class),
									context.getArgument("value", Integer.class))
							)
						)
					)
				)
			).then(
				Commands.literal("export").executes(ExportCommand::execute).then(
					Commands.argument("name", StringArgumentType.string()).executes(ExportCommand::execute).then(
						Commands.argument("center", BlockStateArgument.block()).executes(ExportCommand::execute).then(
							Commands.argument("preset", new PresetArgument()).executes(ExportCommand::execute).then(
								Commands.argument("template", new TemplateArgument()).executes(ExportCommand::execute).then(
									Commands.argument("flags", FlagsArgument.with("-o", "-a", "-b")).executes(ExportCommand::execute)
								)
							)
						)
					)
				)
			)
		);
	}

	// if a name includes a space, we wrap it in quotes
	private static final Function<String, String> filterNamesWithSpaces = (name -> name.contains(" ") ? "\"" + name + "\"" : name);

	private static class PresetArgument implements ArgumentType<String>
	{
		@Override
		public String parse(StringReader reader) throws CommandSyntaxException
		{
			return reader.readString();
		}

		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
		{
			Set<String> set = OTG.getEngine().getPresetLoader().getAllPresetNames().stream()
				.map(filterNamesWithSpaces).collect(Collectors.toSet());
			set.add("global");
			return ISuggestionProvider.suggest(set, builder);
		}
	}

	private static class BiomeObjectArgument implements ArgumentType<String>
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
				list = OTG.getEngine().getCustomObjectManager().getGlobalObjects().getGlobalObjectNames();
			}
			else
			{
				list = OTG.getEngine().getCustomObjectManager().getGlobalObjects().getAllBONamesForPreset(preset);
			}
			if (list == null) list = new ArrayList<>();
			list = list.stream().map(filterNamesWithSpaces).collect(Collectors.toList());
			return ISuggestionProvider.suggest(list, builder);
		}
	}

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
				list = OTG.getEngine().getCustomObjectManager().getGlobalObjects().getGlobalTemplates();
			}
			else
			{
				list = OTG.getEngine().getCustomObjectManager().getGlobalObjects().getTemplatesForPreset(preset);
			}
			if (list == null) list = new ArrayList<>();
			list = list.stream().map(filterNamesWithSpaces).collect(Collectors.toList());
			list.add("default");
			return ISuggestionProvider.suggest(list.stream(), builder);
		}
	}

	private static class FlagsArgument implements ArgumentType<String>
	{
		private final String[] options;

		private FlagsArgument(String[] options)
		{
			this.options = options;
		}

		public static FlagsArgument with(String... options)
		{
			return new FlagsArgument(options);
		}

		@Override
		public String parse(StringReader reader)
		{
			final String text = reader.getRemaining();
			reader.setCursor(reader.getTotalLength());
			return text;
		}

		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
		{
			return ISuggestionProvider.suggest(options, builder);
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
}
