package com.pg85.otg.forge.commands;

import com.pg85.otg.OTG;
import net.minecraft.command.CommandSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
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
	public static final String usage = "Usage: /otg data <type>";
	public static final List<String> dataTypes = new ArrayList<>(Arrays.asList(
			"biome",
			"block",
			"entity",
			"sound",
			"particle"
	));

	public static int execute(CommandSource source, String type)
	{
		// /otg data music
		// /otg data sound

		IForgeRegistry<?> registry;

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
			default:
				source.sendFeedback(new StringTextComponent(usage), false);
				source.sendFeedback(new StringTextComponent("Data types: "+String.join(", ", dataTypes)), false);
				return 0;
		}
		Set<ResourceLocation> set = registry.getKeys();
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
