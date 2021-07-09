package com.pg85.otg.forge.commands;

import java.util.List;
import java.util.stream.Collectors;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.forge.commands.arguments.StringArrayArgument;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;

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
	private static final String USAGE = "Usage: /otg locate <biome name> [range]";
	private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType(
			object -> new TranslationTextComponent("commands.locatebiome.notFound", object));

	List<String> biomes = ForgeRegistries.BIOMES.getKeys().stream()
			.filter(key -> key.getNamespace().equals(Constants.MOD_ID_SHORT))
			.map(key -> key.getPath()).collect(Collectors.toList());

	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder)
	{
		builder.then(Commands.literal("locate")
			.executes(context -> showHelp(context.getSource()))
				.then(Commands.argument("biome", StringArrayArgument.with(biomes))
					.executes(context -> locateBiome(context.getSource(), StringArgumentType.getString(context, "biome"), 10000))
						.then(Commands.argument("range", IntegerArgumentType.integer(0))
							.executes(context -> locateBiome(context.getSource(), StringArgumentType.getString(context, "biome"), IntegerArgumentType.getInteger(context, "range")))))
		);
	}

	@SuppressWarnings("resource")
	private int locateBiome(CommandSource source, String biome, int range) throws CommandSyntaxException
	{
		ServerWorld world = source.getLevel();
		if (!(world.getChunkSource().generator instanceof OTGNoiseChunkGenerator))
		{
			source.sendSuccess(new StringTextComponent("OTG is not enabled in this world"), false);
			return 0;
		}

		ResourceLocation key = new ResourceLocation(Constants.MOD_ID_SHORT, biome);

		BlockPos pos = world.findNearestBiome(world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).get(key),
				new BlockPos(source.getPosition()), range, 8);

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
