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
import com.pg85.otg.forge.biome.OTGBiomeProvider;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.presets.Preset;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
				(context) -> context.hasPermissionLevel(2)
			).executes(
				(context) -> showHelp(context.getSource())
			).then(
				Commands.literal("help").executes(
					(context) -> showHelp(context.getSource())
				)
			).then(
				Commands.literal("map").executes(
					(context) -> mapBiomes(context.getSource(), 2048, 2048)
				).then(
					Commands.argument("width", IntegerArgumentType.integer(0)).executes(
							(context) -> mapBiomes(context.getSource(), IntegerArgumentType.getInteger(context, "width"), IntegerArgumentType.getInteger(context, "width"))
					).then(
						Commands.argument("height", IntegerArgumentType.integer(0)).executes(
							(context) -> mapBiomes(context.getSource(), IntegerArgumentType.getInteger(context, "width"), IntegerArgumentType.getInteger(context, "height"))
				)))
			).then(
				Commands.literal("data").then(
					Commands.argument("type", StringArgumentType.word()).executes(
						(context -> DataCommand.execute(context.getSource(), context.getArgument("type", String.class)))
					)
				)
			).then(
				Commands.literal("preset").executes(
					(context -> showPreset(context.getSource()))
				)
			).then(
				Commands.literal("biome").executes(
						(context -> showBiome(context.getSource()))
				)
			).then(
				Commands.literal("spawn").then(
					Commands.argument("preset", new PresetArgument()).then(
						Commands.argument("object", new BiomeObjectArgument()).then(
							Commands.argument("location", new BlockPosArgument())
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
		));
	}

	private static int showBiome(CommandSource source)
	{
		if (!(source.getWorld().getChunkProvider().generator instanceof OTGNoiseChunkGenerator))
		{
			source.sendFeedback(new StringTextComponent("OTG is not enabled in this world"), false);
			return 0;
		}
		source.sendFeedback(new StringTextComponent(
				source.getWorld().getBiome(
						new BlockPos(source.getPos().x, source.getPos().y, source.getPos().z)).toString())
				, false);
		return 0;
	}

	private static int showPreset(CommandSource source)
	{
		if (!(source.getWorld().getChunkProvider().generator instanceof OTGNoiseChunkGenerator))
		{
			source.sendFeedback(new StringTextComponent("OTG is not enabled in this world"), false);
			return 0;
		}
		Preset preset = ((OTGNoiseChunkGenerator) source.getWorld().getChunkProvider().generator).getPreset();
		source.sendFeedback(new StringTextComponent
				("Preset: "+preset.getName()
				 + "\nDescription: " + preset.getDescription()
				 + "\nVersion: " + preset.getVersion()
		), false);
		return 0;
	}

	private static int showHelp(CommandSource source)
	{
		source.sendFeedback(new StringTextComponent("OTG Help"), false);
		source.sendFeedback(new StringTextComponent("/otg map -> Creates a 2048x2048 biome map of the world."), false);
		source.sendFeedback(new StringTextComponent("/otg data <dataType>"), false);
		source.sendFeedback(new StringTextComponent("/otg spawn <preset name> <object name> <location>"), false);
		return 0;
	}

	private static int mapBiomes(CommandSource source, int width, int height)
	{
		if (!(source.getWorld().getChunkProvider().generator.getBiomeProvider() instanceof OTGBiomeProvider))
		{
			source.sendFeedback(new StringTextComponent("Please run this command in an OTG world."), false);
			return 1;
		}

		OTGBiomeProvider provider = (OTGBiomeProvider) source.getWorld().getChunkProvider().generator.getBiomeProvider();

		//setup image

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		int progressUpdate = img.getHeight() / 8;

		for (int x = 0; x < img.getHeight(); x++)
		{
			for (int z = 0; z < img.getWidth(); z++)
			{
				//set the color
				img.setRGB(x, z, provider.configLookup[provider.getSampler().sample(x, z)].getBiomeColor());
			}

			//send a progress update to let people know the server isn't dying
			if (x % progressUpdate == 0)
			{
				source.sendFeedback(new StringTextComponent((((double) x / img.getHeight()) * 100) + "% Done mapping"), true);
			}
		}

		String fileName = source.getServer().getServerConfiguration().getWorldName() + " biomes.png";

		source.sendFeedback(new StringTextComponent("Finished mapping! The resulting image is located at " + fileName + "."), true);

		//save the biome map
		Path p = Paths.get(fileName);
		try
		{
			ImageIO.write(img, "png", p.toAbsolutePath().toFile());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return 0;
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
			List<String> list = OTG.getEngine().getCustomObjectManager().getGlobalObjects().getAllBONamesForPreset(
				context.getArgument("preset", String.class)).stream()
				.map(filterNamesWithSpaces).collect(Collectors.toList());
			return ISuggestionProvider.suggest(
				list,
				builder);
		}
	}
}
