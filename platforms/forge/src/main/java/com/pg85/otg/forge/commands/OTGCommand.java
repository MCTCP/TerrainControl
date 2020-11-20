package com.pg85.otg.forge.commands;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import com.mojang.brigadier.CommandDispatcher;
import com.pg85.otg.forge.biome.OTGBiomeProvider;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class OTGCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(
			Commands.literal("otg").requires(
				(context) -> context.hasPermissionLevel(2)
			).executes(
				(context) -> showHelp(context.getSource())
			).then(
				Commands.literal("help").executes(
					(context) -> showHelp(context.getSource())
				)
			).then(
				Commands.literal("map").executes(
					(context) -> mapBiomes(context.getSource())
				)
			)
		);
	}

	private static int showHelp(CommandSource source)
	{
		source.sendFeedback(new StringTextComponent("OTG Help"), true);
		source.sendFeedback(new StringTextComponent("/otg map -> Creates a 2048x2048 biome map of the world."), true);
		return 0;
	}

	private static int mapBiomes(CommandSource source)
	{
		if (!(source.getWorld().getChunkProvider().generator.getBiomeProvider() instanceof OTGBiomeProvider))
		{
			source.sendFeedback(new StringTextComponent("Please run this command in an OTG world."), false);
			return 1;
		}

		OTGBiomeProvider provider = (OTGBiomeProvider) source.getWorld().getChunkProvider().generator.getBiomeProvider();

		//setup image

		BufferedImage img = new BufferedImage(2048, 2048, BufferedImage.TYPE_INT_RGB);

		int progressUpdate = img.getHeight() / 8;

		for (int x = 0; x < img.getHeight(); x++)
		{
			for (int z = 0; z < img.getWidth(); z++)
			{
				//set the color
				img.setRGB(x, z, OTGBiomeProvider.LOOKUP[provider.getSampler().sample(x, z)].getBiomeColor());
			}

			//send a progress update to let people know the server isn't dying
			if (x % progressUpdate == 0)
			{
				source.sendFeedback(new StringTextComponent((((double) x / img.getHeight()) * 100) + "% Done mapping"), true);
			}
		}

		String fileName = source.getServer().func_240793_aU_().getWorldName() + " biomes.png";

		source.sendFeedback(new StringTextComponent("Finished mapping! The resulting image is located at " + fileName + "."), true);

		//save the biome map
		Path p = Paths.get(fileName);
		try
		{
			ImageIO.write(img, "png", p.toAbsolutePath().toFile());
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		return 0;
	}
}
