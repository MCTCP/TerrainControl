package com.pg85.otg.forge.commands;

import java.util.List;
import java.util.stream.Collectors;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.forge.commands.arguments.StringArrayArgument;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.biome.OTGBiomeResourceLocation;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

public class LocateCommand implements BaseCommand
{
	private static final String USAGE = "Usage: /otg locate <biome name>";
	private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType(
			object -> new TranslationTextComponent("commands.locatebiome.notFound", new Object[]
			{ object }));

	List<String> biomes = ForgeRegistries.BIOMES.getKeys().stream()
			.filter(key -> key.getNamespace().equals(Constants.MOD_ID_SHORT))
			.map(key -> key.toString().substring(key.toString().indexOf('.') + 1)).collect(Collectors.toList());

	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder)
	{
		builder.then(Commands.literal("locate").executes(context -> showHelp(context.getSource()))
				.then(Commands.argument("biome", StringArrayArgument.with(biomes)).executes(this::locateBiome)));
	}

	@SuppressWarnings("resource")
	private int locateBiome(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		CommandSource source = context.getSource();
		String biome = context.getArgument("biome", String.class);
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
				new BlockPos(source.getPosition()), 6400, 8);

		if (pos == null)
		{
			throw ERROR_BIOME_NOT_FOUND.create(biome);
		} else
		{
			return net.minecraft.command.impl.LocateCommand.showLocateResult(source, biome,
					new BlockPos(source.getPosition()), pos, "commands.locatebiome.success");
		}
	}

	private int showHelp(CommandSource source)
	{
		source.sendSuccess(new StringTextComponent(USAGE), false);
		return 0;
	}
}
