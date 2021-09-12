package com.pg85.otg.paper.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.pg85.otg.paper.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.biome.OTGBiomeResourceLocation;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public class TpCommand extends BaseCommand
{
	private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType(
			object -> new TranslatableComponent("commands.locatebiome.notFound", object));

	public TpCommand()
	{
		super("tp");
		this.helpMessage = "Teleports you to a specific OTG biome.";
		this.usage = "/otg tp <biome> [range]";
		this.detailedHelp = new String[]
		{ "<biome>: The name of the biome to teleport to.",
				"[range]: The radius in blocks to search for the target biome, defaults to 10000.",
				"Note: large numbers will make the command take a long time." };
	}

	@Override
	public void build(LiteralArgumentBuilder<CommandSourceStack> builder)
	{
		builder.then(
				Commands.literal("tp").executes(context -> showHelp(context.getSource()))
						.then(Commands.argument("biome", StringArgumentType.word())
								.executes(context -> locateBiome(context.getSource(),
										StringArgumentType.getString(context, "biome"), 10000))
								.suggests(this::suggestBiomes)
								.then(Commands.argument("range", IntegerArgumentType.integer(0))
										.executes(context -> locateBiome(context.getSource(),
												StringArgumentType.getString(context, "biome"),
												IntegerArgumentType.getInteger(context, "range"))))));
	}

	@SuppressWarnings("resource")
	private int locateBiome(CommandSourceStack source, String biome, int range) throws CommandSyntaxException
	{
		biome = biome.toLowerCase();

		ServerLevel world = source.getLevel();
		if (!(world.getChunkSource().generator instanceof OTGNoiseChunkGenerator))
		{
			source.sendSuccess(new TextComponent("OTG is not enabled in this world"), false);
			return 0;
		}

		Preset preset = ((OTGNoiseChunkGenerator) world.getChunkSource().generator).getPreset();

		ResourceLocation key = new ResourceLocation(new OTGBiomeResourceLocation(preset.getPresetFolder(),
				preset.getShortPresetName(), preset.getMajorVersion(), biome).toResourceLocationString());

		BlockPos pos = world.findNearestBiome(world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).get(key),
				new BlockPos(source.getPosition()), range, 8);

		if (pos == null)
		{
			throw ERROR_BIOME_NOT_FOUND.create(biome);
		} else
		{
			int y = world.getChunkSource().generator.getBaseHeight(pos.getX(), pos.getZ(),
					Types.MOTION_BLOCKING_NO_LEAVES, world);
			source.getPlayerOrException().teleportTo(pos.getX(), y, pos.getZ());
			source.sendSuccess(new TextComponent("Teleporting you to the nearest " + biome + "."), false);
			return 0;
		}
	}

	private CompletableFuture<Suggestions> suggestBiomes(CommandContext<CommandSourceStack> context,
			SuggestionsBuilder builder)
	{
		List<String> biomes = new ArrayList<>();

		if (context.getSource().getLevel().getChunkSource().generator instanceof OTGNoiseChunkGenerator)
		{
			biomes = ((OTGNoiseChunkGenerator) context.getSource().getLevel().getChunkSource().generator).getPreset()
					.getAllBiomeNames().stream().map(name -> name.replace(' ', '_')).collect(Collectors.toList());
		}

		return SharedSuggestionProvider.suggest(biomes, builder);
	}

	private int showHelp(CommandSourceStack source)
	{
		source.sendSuccess(new TextComponent(getUsage()), false);
		return 0;
	}
}
