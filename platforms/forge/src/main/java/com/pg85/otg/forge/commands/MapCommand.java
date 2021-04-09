package com.pg85.otg.forge.commands;

import com.pg85.otg.forge.biome.OTGBiomeProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.StringTextComponent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MapCommand
{
	protected static int mapBiomes(CommandSource source, int width, int height)
	{
		if (!(source.getWorld().getChunkProvider().generator.getBiomeProvider() instanceof OTGBiomeProvider))
		{
			source.sendFeedback(new StringTextComponent("Please run this command in an OTG world."), false);
			return 1;
		}

		OTGBiomeProvider provider = (OTGBiomeProvider) source.getWorld().getChunkProvider().generator.getBiomeProvider();

		//setup image

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		int progressUpdate = img.getHeight() / 8;

		for (int x = 0; x < img.getHeight(); x++)
		{
			for (int z = 0; z < img.getWidth(); z++)
			{
				//set the color
				img.setRGB(x, z, provider.configLookup[provider.getSampler().sample(x, z)].getBiomeColor());
			}

			//send a progress update to let people know the server isn't dying
			if (x % progressUpdate == 0)
			{
				source.sendFeedback(new StringTextComponent((((double) x / img.getHeight()) * 100) + "% Done mapping"), true);
			}
		}

		String fileName = source.getServer().getServerConfiguration().getWorldName() + " biomes.png";

		source.sendFeedback(new StringTextComponent("Finished mapping! The resulting image is located at " + fileName + "."), true);

		//save the biome map
		Path p = Paths.get(fileName);
		try
		{
			ImageIO.write(img, "png", p.toAbsolutePath().toFile());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return 0;
	}
}
