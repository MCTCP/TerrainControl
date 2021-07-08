package com.pg85.otg.forge.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.interfaces.IBiomeConfig;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public class LocateCommand implements BaseCommand
{
	private static final String USAGE = "Usage: /otg locate <biome name>";
	private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType(
			object -> new TranslationTextComponent("commands.locatebiome.notFound", new Object[]
			{ object }));

	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder)
	{
		builder.then(Commands.literal("locate")
				.executes(context -> showHelp(context.getSource()))
					.then(Commands.argument("biome", StringArgumentType.word()).executes(
						context -> locateBiome(context.getSource(), StringArgumentType.getString(context, "biome"))))
		);
	}

	@SuppressWarnings("resource")
	private int locateBiome(CommandSource source, String biome) throws CommandSyntaxException
	{
		ServerWorld world = source.getLevel();
		if (!(world.getChunkSource().generator instanceof OTGNoiseChunkGenerator))
		{
			source.sendSuccess(new StringTextComponent("OTG is not enabled in this world"), false);
			return 0;
		}

		IBiomeConfig config = ((OTGNoiseChunkGenerator) world.getChunkSource().generator).getPreset()
				.getBiomeConfig(biome);

		if (config == null)
		{
			source.sendSuccess(new StringTextComponent("Invalid biome name: " + biome), false);
			return 0;
		}

		ResourceLocation key = new ResourceLocation(config.getRegistryKey().toResourceLocationString());

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
