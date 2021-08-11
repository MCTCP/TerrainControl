package com.pg85.otg.forge.commands;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.interfaces.IBiomeConfig;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.MobSpawnInfo.Spawners;
import net.minecraftforge.common.BiomeDictionary;

public class BiomeCommand extends BaseCommand
{
	private static final String[] OPTIONS = new String[]
	{ "info", "spawns" };

	public BiomeCommand()
	{
		this.name = "biome";
		this.helpMessage = "Displays information about the biome you are in.";
		this.usage = "/otg biome";
	}

	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder)
	{
		builder.then(Commands.literal("biome").executes(context -> showBiome(context.getSource(), ""))
				.then(Commands.argument("option", StringArgumentType.word()).suggests(this::suggestTypes).executes(
						context -> showBiome(context.getSource(), StringArgumentType.getString(context, "option")))));
	}

	private int showBiome(CommandSource source, String option)
	{
		if (!(source.getLevel().getChunkSource().generator instanceof OTGNoiseChunkGenerator))
		{
			source.sendSuccess(new StringTextComponent("OTG is not enabled in this world"), false);
			return 0;
		}

		Biome biome = source.getLevel()
				.getBiome(new BlockPos(source.getPosition().x, source.getPosition().y, source.getPosition().z));
		IBiomeConfig config = ((OTGNoiseChunkGenerator) source.getLevel().getChunkSource().generator)
				.getCachedBiomeProvider().getBiomeConfig((int) source.getPosition().x, (int) source.getPosition().z);

		source.sendSuccess(new StringTextComponent("====================================================="), false);
		if (config.getTemplateForBiome())
		{
			source.sendSuccess(
					new StringTextComponent("According to OTG, this biome uses the ").withStyle(TextFormatting.GOLD)
							.append(new StringTextComponent(config.getName()).withStyle(TextFormatting.GREEN))
							.append(new StringTextComponent(" template.").withStyle(TextFormatting.GOLD)),
					false);
			source.sendSuccess(new StringTextComponent("This biome belongs to either another mod or the vanilla game.")
					.withStyle(TextFormatting.GRAY), false);
		} else
		{
			source.sendSuccess(
					new StringTextComponent("According to OTG, you are in the ").withStyle(TextFormatting.GOLD)
							.append(new StringTextComponent(config.getName()).withStyle(TextFormatting.GREEN))
							.append(new StringTextComponent(" biome.").withStyle(TextFormatting.GOLD)),
					false);
		}
		source.sendSuccess(createComponent("Biome registry name: ", biome.getRegistryName().toString(),
				TextFormatting.GOLD, TextFormatting.GREEN), false);

		switch (option)
		{
		case "info":
			showBiomeInfo(source, biome, config);
			break;
		case "spawns":
			showBiomeMobs(source, biome, config);
			break;
		default:
			break;
		}
		return 0;
	}

	private void showBiomeInfo(CommandSource source, Biome biome, IBiomeConfig config)
	{
		Set<String> types = BiomeDictionary
				.getTypes(RegistryKey.create(Registry.BIOME_REGISTRY, biome.getRegistryName())).stream()
				.map(BiomeDictionary.Type::getName).collect(Collectors.toSet());

		source.sendSuccess(createComponent("Biome Category: ", biome.getBiomeCategory().toString(), TextFormatting.GOLD,
				TextFormatting.GREEN), false);
		source.sendSuccess(
				createComponent("Biome Tags: ", String.join(", ", types), TextFormatting.GOLD, TextFormatting.GREEN),
				false);
		source.sendSuccess(createComponent("Inherit Mobs: ", config.getInheritMobsBiomeName(), TextFormatting.GOLD,
				TextFormatting.GREEN), false);

		source.sendSuccess(createComponent("Base Size: ", Integer.toString(config.getBiomeSize()), TextFormatting.GOLD,
				TextFormatting.GREEN)
						.append(createComponent(" Biome Rarity: ", Integer.toString(config.getBiomeRarity()),
								TextFormatting.GOLD, TextFormatting.GREEN)),
				false);

		source.sendSuccess(createComponent("Biome Height: ", String.format("%.2f", config.getBiomeHeight()),
				TextFormatting.GOLD, TextFormatting.GREEN), false);

		source.sendSuccess(createComponent("Volatility: ", String.format("%.2f", config.getBiomeVolatility()),
				TextFormatting.GOLD, TextFormatting.GREEN)
						.append(createComponent(" Volatility1: ", String.format("%.2f", config.getVolatility1()),
								TextFormatting.GOLD, TextFormatting.GREEN))
						.append(createComponent(" Volatility2: ", String.format("%.2f", config.getVolatility2()),
								TextFormatting.GOLD, TextFormatting.GREEN)),
				false);

		source.sendSuccess(createComponent("Base Temperature: ", String.format("%.2f", biome.getBaseTemperature()),
				TextFormatting.GOLD, TextFormatting.GREEN)
						.append(createComponent(" Current Temperature: ",
								String.format("%.2f", biome.getTemperature(new BlockPos(source.getPosition()))),
								TextFormatting.GOLD, TextFormatting.GREEN)),
				false);
	}

	private void showBiomeMobs(CommandSource source, Biome biome, IBiomeConfig config)
	{
		source.sendSuccess(new StringTextComponent("Spawns:").withStyle(TextFormatting.GOLD), false);
		source.sendSuccess(new StringTextComponent("  Monsters:").withStyle(TextFormatting.GOLD), false);
		showSpawns(source, biome.getMobSettings().getMobs(EntityClassification.MONSTER));
		source.sendSuccess(new StringTextComponent("  Creatures:").withStyle(TextFormatting.GOLD), false);
		showSpawns(source, biome.getMobSettings().getMobs(EntityClassification.CREATURE));
		source.sendSuccess(new StringTextComponent("  Water Creatures:").withStyle(TextFormatting.GOLD), false);
		showSpawns(source, biome.getMobSettings().getMobs(EntityClassification.WATER_CREATURE));
		source.sendSuccess(new StringTextComponent("  Ambient Creatures:").withStyle(TextFormatting.GOLD), false);
		showSpawns(source, biome.getMobSettings().getMobs(EntityClassification.AMBIENT));
		source.sendSuccess(new StringTextComponent("  Water Ambient:").withStyle(TextFormatting.GOLD), false);
		showSpawns(source, biome.getMobSettings().getMobs(EntityClassification.WATER_AMBIENT));
		source.sendSuccess(new StringTextComponent("  Misc:").withStyle(TextFormatting.GOLD), false);
		showSpawns(source, biome.getMobSettings().getMobs(EntityClassification.MISC));

	}

	public void showSpawns(CommandSource source, List<Spawners> spawns)
	{
		spawns.forEach(spawn -> source
				.sendSuccess(createComponent("   - Entity: ", spawn.type.getRegistryName().toString(), TextFormatting.GOLD, TextFormatting.GREEN)
						.append(createComponent(", Weight: ", Integer.toString(spawn.weight), TextFormatting.GOLD,
								TextFormatting.GREEN))
						.append(createComponent(", Min: ", Integer.toString(spawn.minCount), TextFormatting.GOLD,
								TextFormatting.GREEN))
						.append(createComponent(", Max: ", Integer.toString(spawn.maxCount), TextFormatting.GOLD,
								TextFormatting.GREEN)),
						false));

	}

	private CompletableFuture<Suggestions> suggestTypes(CommandContext<CommandSource> context,
			SuggestionsBuilder builder)
	{
		return ISuggestionProvider.suggest(OPTIONS, builder);
	}
}
