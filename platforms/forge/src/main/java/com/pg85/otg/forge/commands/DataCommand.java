package com.pg85.otg.forge.commands;

import com.pg85.otg.OTG;
import net.minecraft.command.CommandSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class DataCommand
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

	public static int execute(CommandSource source, String type)
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
				worldGenRegistry = source.getServer().getDynamicRegistries().getRegistry(Registry.CONFIGURED_FEATURE_KEY);
				break;
			default:
				source.sendFeedback(new StringTextComponent(USAGE), false);
				source.sendFeedback(new StringTextComponent("Data types: " + String.join(", ", DATA_TYPES)), false);
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
				source.sendFeedback(new StringTextComponent("File exported as "+fileName), true);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}).start();
		return 0;
	}
}
