package com.pg85.otg.forge.commands;

import java.util.Set;
import java.util.stream.Collectors;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.interfaces.IBiomeConfig;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

public class BiomeCommand extends BaseCommand
{
	public BiomeCommand()
	{
		this.name = "biome";
		this.helpMessage = "Displays information about the biome you are in.";
		this.usage = "/otg biome";
	}

	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder)
	{
		builder.then(Commands.literal("biome").executes(context -> showBiome(context.getSource())));
	}

	private int showBiome(CommandSource source)
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

		if (config.getTemplateForBiome() != null && config.getTemplateForBiome().trim().length() > 0)
		{
			source.sendSuccess(new StringTextComponent(
					"According to OTG, this biome uses the " + config.getName() + " template.").append(
							new StringTextComponent("\nThis biome belongs to either another mod or the vanilla game.")
									.withStyle(TextFormatting.GRAY)),
					false);
		} else
		{
			source.sendSuccess(
					new StringTextComponent("According to OTG, you are in the " + config.getName() + " biome."), false);
			source.sendSuccess(new StringTextComponent("Biome registry name: ").withStyle(TextFormatting.GOLD).append(
					new StringTextComponent(biome.getRegistryName().toString()).withStyle(TextFormatting.GREEN)),
					false);
		}
		Set<String> types = BiomeDictionary
				.getTypes(RegistryKey.create(Registry.BIOME_REGISTRY, biome.getRegistryName())).stream()
				.map(BiomeDictionary.Type::getName).collect(Collectors.toSet());
		
		source.sendSuccess(
				new StringTextComponent("\nBiome Category: ").withStyle(TextFormatting.GOLD).append(
						new StringTextComponent(biome.getBiomeCategory().toString()).withStyle(TextFormatting.GREEN)),
				false);
		source.sendSuccess(new StringTextComponent("Biome Tags: ").withStyle(TextFormatting.GOLD)
				.append(new StringTextComponent(String.join(", ", types)).withStyle(TextFormatting.GREEN)), false);
		source.sendSuccess(new StringTextComponent("Current Temperature: ").withStyle(TextFormatting.GOLD)
				.append(new StringTextComponent(String.format("%.2f", biome.getTemperature(new BlockPos(source.getPosition())))).withStyle(TextFormatting.GREEN)), false);
		return 0;
	}
}
