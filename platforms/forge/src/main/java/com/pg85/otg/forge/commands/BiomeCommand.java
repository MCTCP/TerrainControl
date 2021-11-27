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

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraftforge.common.BiomeDictionary;

public class BiomeCommand extends BaseCommand
{
	private static final String[] OPTIONS = new String[]
	{ "info", "spawns" };

	public BiomeCommand()
	{
		super("biome");
		this.helpMessage = "Displays information about the biome you are in.";
		this.usage = "/otg biome";
	}

	@Override
	public void build(LiteralArgumentBuilder<CommandSourceStack> builder)
	{
		builder.then(Commands.literal("biome").executes(context -> showBiome(context.getSource(), ""))
				.then(Commands.argument("option", StringArgumentType.word()).suggests(this::suggestTypes).executes(
						context -> showBiome(context.getSource(), StringArgumentType.getString(context, "option")))));
	}

	private int showBiome(CommandSourceStack source, String option)
	{
		if (!(source.getLevel().getChunkSource().generator instanceof OTGNoiseChunkGenerator))
		{
			source.sendSuccess(new TextComponent("OTG is not enabled in this world"), false);
			return 0;
		}

		Biome biome = source.getLevel()
				.getBiome(new BlockPos(source.getPosition().x, source.getPosition().y, source.getPosition().z));
		IBiomeConfig config = ((OTGNoiseChunkGenerator) source.getLevel().getChunkSource().generator)
				.getCachedBiomeProvider().getBiomeConfig((int) source.getPosition().x, (int) source.getPosition().z);

		source.sendSuccess(new TextComponent("====================================================="), false);
		if (config.getTemplateForBiome())
		{
			source.sendSuccess(
					new TextComponent("According to OTG, this biome uses the ").withStyle(ChatFormatting.GOLD)
							.append(new TextComponent(config.getName()).withStyle(ChatFormatting.GREEN))
							.append(new TextComponent(" template.").withStyle(ChatFormatting.GOLD)),
					false);
			source.sendSuccess(new TextComponent("This biome belongs to either another mod or the vanilla game.")
					.withStyle(ChatFormatting.GRAY), false);
		} else
		{
			source.sendSuccess(
					new TextComponent("According to OTG, you are in the ").withStyle(ChatFormatting.GOLD)
							.append(new TextComponent(config.getName()).withStyle(ChatFormatting.GREEN))
							.append(new TextComponent(" biome.").withStyle(ChatFormatting.GOLD)),
					false);
		}
		source.sendSuccess(createComponent("Biome registry name: ", biome.getRegistryName().toString(),
				ChatFormatting.GOLD, ChatFormatting.GREEN), false);

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

	private void showBiomeInfo(CommandSourceStack source, Biome biome, IBiomeConfig config)
	{
		Set<String> types = BiomeDictionary
				.getTypes(ResourceKey.create(Registry.BIOME_REGISTRY, biome.getRegistryName())).stream()
				.map(BiomeDictionary.Type::getName).collect(Collectors.toSet());

		source.sendSuccess(createComponent("Biome Category: ", biome.getBiomeCategory().toString(), ChatFormatting.GOLD,
				ChatFormatting.GREEN), false);
		source.sendSuccess(
				createComponent("Biome Tags: ", String.join(", ", types), ChatFormatting.GOLD, ChatFormatting.GREEN),
				false);
		source.sendSuccess(createComponent("Inherit Mobs: ", config.getInheritMobsBiomeName(), ChatFormatting.GOLD,
				ChatFormatting.GREEN), false);

		source.sendSuccess(createComponent("Base Size: ", Integer.toString(config.getBiomeSize()), ChatFormatting.GOLD,
				ChatFormatting.GREEN)
						.append(createComponent(" Biome Rarity: ", Integer.toString(config.getBiomeRarity()),
								ChatFormatting.GOLD, ChatFormatting.GREEN)),
				false);

		source.sendSuccess(createComponent("Biome Height: ", String.format("%.2f", config.getBiomeHeight()),
				ChatFormatting.GOLD, ChatFormatting.GREEN), false);

		source.sendSuccess(createComponent("Volatility: ", String.format("%.2f", config.getBiomeVolatility()),
				ChatFormatting.GOLD, ChatFormatting.GREEN)
						.append(createComponent(" Volatility1: ", String.format("%.2f", config.getVolatility1()),
								ChatFormatting.GOLD, ChatFormatting.GREEN))
						.append(createComponent(" Volatility2: ", String.format("%.2f", config.getVolatility2()),
								ChatFormatting.GOLD, ChatFormatting.GREEN)),
				false);

		source.sendSuccess(createComponent("Base Temperature: ", String.format("%.2f", biome.getBaseTemperature()),
				ChatFormatting.GOLD, ChatFormatting.GREEN)
						.append(createComponent(" Current Temperature: ",
								String.format("%.2f", biome.getTemperature(new BlockPos(source.getPosition()))),
								ChatFormatting.GOLD, ChatFormatting.GREEN)),
				false);
	}

	private void showBiomeMobs(CommandSourceStack source, Biome biome, IBiomeConfig config)
	{
		source.sendSuccess(new TextComponent("Spawns:").withStyle(ChatFormatting.GOLD), false);
		source.sendSuccess(new TextComponent("  Monsters:").withStyle(ChatFormatting.GOLD), false);
		showSpawns(source, biome.getMobSettings().getMobs(MobCategory.MONSTER));
		source.sendSuccess(new TextComponent("  Creatures:").withStyle(ChatFormatting.GOLD), false);
		showSpawns(source, biome.getMobSettings().getMobs(MobCategory.CREATURE));
		source.sendSuccess(new TextComponent("  Water Creatures:").withStyle(ChatFormatting.GOLD), false);
		showSpawns(source, biome.getMobSettings().getMobs(MobCategory.WATER_CREATURE));
		source.sendSuccess(new TextComponent("  Ambient Creatures:").withStyle(ChatFormatting.GOLD), false);
		showSpawns(source, biome.getMobSettings().getMobs(MobCategory.AMBIENT));
		source.sendSuccess(new TextComponent("  Water Ambient:").withStyle(ChatFormatting.GOLD), false);
		showSpawns(source, biome.getMobSettings().getMobs(MobCategory.WATER_AMBIENT));
		source.sendSuccess(new TextComponent("  Misc:").withStyle(ChatFormatting.GOLD), false);
		showSpawns(source, biome.getMobSettings().getMobs(MobCategory.MISC));

	}

	public void showSpawns(CommandSourceStack source, WeightedRandomList<SpawnerData> spawns)
	{
		spawns.unwrap().forEach(spawn -> source
				.sendSuccess(createComponent("   - Entity: ", spawn.type.getRegistryName().toString(), ChatFormatting.GOLD, ChatFormatting.GREEN)
						.append(createComponent(", Weight: ", Integer.toString(spawn.getWeight().asInt()), ChatFormatting.GOLD,
								ChatFormatting.GREEN))
						.append(createComponent(", Min: ", Integer.toString(spawn.minCount), ChatFormatting.GOLD,
								ChatFormatting.GREEN))
						.append(createComponent(", Max: ", Integer.toString(spawn.maxCount), ChatFormatting.GOLD,
								ChatFormatting.GREEN)),
						false));

	}

	private CompletableFuture<Suggestions> suggestTypes(CommandContext<CommandSourceStack> context,
			SuggestionsBuilder builder)
	{
		return SharedSuggestionProvider.suggest(OPTIONS, builder);
	}
}
