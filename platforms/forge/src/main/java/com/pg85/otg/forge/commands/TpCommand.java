package com.pg85.otg.forge.commands;

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
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.biome.OTGBiomeResourceLocation;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.server.ServerWorld;

public class TpCommand extends BaseCommand
{
	private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType(
			object -> new TranslationTextComponent("commands.locatebiome.notFound", object));

	public TpCommand()
	{
		this.name = "tp";
		this.helpMessage = "Teleports you to a specific OTG biome.";
		this.usage = "/otg tp <biome> [range]";
		this.detailedHelp = new String[]
		{ "<biome>: The name of the biome to teleport to.",
				"[range]: The radius in blocks to search for the target biome, defaults to 10000.",
				"Note: large numbers will make the command take a long time." };
	}

	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder)
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
	private int locateBiome(CommandSource source, String biome, int range) throws CommandSyntaxException
	{
		biome = biome.toLowerCase();

		ServerWorld world = source.getLevel();
		if (!(world.getChunkSource().generator instanceof OTGNoiseChunkGenerator))
		{
			source.sendSuccess(new StringTextComponent("OTG is not enabled in this world"), false);
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
					Type.MOTION_BLOCKING_NO_LEAVES);
			source.getPlayerOrException().teleportTo(pos.getX(), y, pos.getZ());
			source.sendSuccess(new StringTextComponent("Teleporting you to the nearest " + biome + "."), false);
			return 0;
		}
	}

	private CompletableFuture<Suggestions> suggestBiomes(CommandContext<CommandSource> context,
			SuggestionsBuilder builder)
	{
		List<String> biomes = new ArrayList<>();

		if (context.getSource().getLevel().getChunkSource().generator instanceof OTGNoiseChunkGenerator)
		{
			biomes = ((OTGNoiseChunkGenerator) context.getSource().getLevel().getChunkSource().generator).getPreset()
					.getAllBiomeNames().stream().map(name -> name.replace(' ', '_')).collect(Collectors.toList());
		}

		return ISuggestionProvider.suggest(biomes, builder);
	}

	private int showHelp(CommandSource source)
	{
		source.sendSuccess(new StringTextComponent(getUsage()), false);
		return 0;
	}
}
