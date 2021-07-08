package com.pg85.otg.forge.commands;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.pg85.otg.OTG;
import com.pg85.otg.forge.commands.arguments.StringArrayArgument;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public class DataCommand implements BaseCommand
{
	private static final String USAGE = "Usage: /otg data <type>";
	private static final List<String> DATA_TYPES = new ArrayList<>(Arrays.asList(
			"biome",
			"block",
			"entity",
			"sound",
			"particle",
			"configured_feature"
	));
	
	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder)
	{
		builder.then(Commands.literal("data")
			.executes(context -> execute(context.getSource(), ""))
				.then(Commands.argument("type", StringArrayArgument.with(DATA_TYPES))
					.executes((context -> execute(context.getSource(), context.getArgument("type", String.class)))
				)
			)
		);
	}

	public int execute(CommandSource source, String type)
	{
		// /otg data music
		// /otg data sound

		// worldgen registries aren't wrapped by forge so we need to use another object here
		IForgeRegistry<?> registry = null;
		Registry<?> worldGenRegistry = null;

		switch (type.toLowerCase())
		{
			case "biome":
				registry = ForgeRegistries.BIOMES;
				break;
			case "block":
				registry = ForgeRegistries.BLOCKS;
				break;
			case "entity":
				registry = ForgeRegistries.ENTITIES;
				break;
			case "sound":
				registry = ForgeRegistries.SOUND_EVENTS;
				break;
			case "particle":
				registry = ForgeRegistries.PARTICLE_TYPES;
				break;
			case "configured_feature":
				worldGenRegistry = source.getServer().registryAccess().registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY);
				break;
			default:
				source.sendSuccess(new StringTextComponent(USAGE), false);
				return 0;
		}

		Set<ResourceLocation> set;
		if (registry != null) {
			set = registry.getKeys();
		} else {
			set = worldGenRegistry.keySet();
		}

		new Thread(() -> {
			try
			{
				Path root = OTG.getEngine().getOTGRootFolder();
				String fileName = root.toString() + File.separator + "data-output-" + type + ".txt".toLowerCase();
				File output = new File(fileName);
				FileWriter writer = new FileWriter(output);
				for (ResourceLocation key : set)
				{
					writer.write(key.toString()+"\n");
				}
				writer.close();
				source.sendSuccess(new StringTextComponent("File exported as "+fileName), true);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}).start();
		return 0;
	}
}
