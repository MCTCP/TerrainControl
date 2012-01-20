package com.Khorn.TerrainControl.Util;


import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;

import javax.imageio.stream.FileCacheImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import net.minecraft.server.BiomeBase;
import net.minecraft.server.World;

import org.bukkit.command.CommandSender;

import com.Khorn.TerrainControl.TCPlugin;
import com.Khorn.TerrainControl.Commands.BaseCommand;
import com.Khorn.TerrainControl.Configuration.BiomeConfig;
import com.Khorn.TerrainControl.Configuration.WorldConfig;
import com.sun.imageio.plugins.png.PNGImageWriter;
import com.sun.imageio.plugins.png.PNGImageWriterSpi;

public class MapWriter implements Runnable
{


    private static int[] Default_Colors = {0x3333FF, 0x999900, 0xFFCC33, 0x666600, 0x00FF00, 0x007700, 0x99cc66, 0x00CCCC, 0, 0, 0xFFFFFF, 0x66FFFF, 0xCCCCCC, 0xCC9966, 0xFF33cc, 0xff9999};
    private static boolean isWork = false;

    private TCPlugin plugin;
    private World world;
    private int size;
    private CommandSender sender;
    private Angle angle;

    public enum Angle
    {
        d0, d90, d180, d270
    }

    public MapWriter(TCPlugin _plugin, World _world, int _size, Angle _angle, CommandSender _sender)
    {
        this.plugin = _plugin;
        this.world = _world;
        this.size = _size;
        this.sender = _sender;
        this.angle = _angle;
    }

    public void run()
    {
        if (MapWriter.isWork)
        {
            sender.sendMessage(BaseCommand.ErrorColor + "Another instance of map writer is running");
            return;
        }
        MapWriter.isWork = true;
        int height = size;
        int width = size;
        try
        {
            int[] Colors = Default_Colors;

            WorldConfig config = plugin.worldsSettings.get(world.worldData.name);
            if (config != null)
            {
                Colors = new int[config.biomeConfigs.length];
                for (BiomeConfig biomeConfig : config.biomeConfigs)
                {
                    if (biomeConfig != null)
                    {
                        try
                        {
                            int color = Integer.decode(biomeConfig.BiomeColor);
                            if (color <= 0xFFFFFF)
                                Colors[biomeConfig.Biome.F] = color;
                        } catch (NumberFormatException ex)
                        {
                            System.out.println("TerrainControl: wrong color in " + biomeConfig.Biome.r);
                            sender.sendMessage(BaseCommand.ErrorColor + "Wrong color in " + biomeConfig.Biome.r);
                        }
                    }
                }
            }


            sender.sendMessage(BaseCommand.MessageColor + "Generating map...");
            float[] tempArray = new float[256];
            BiomeBase[] BiomeBuffer = new BiomeBase[256];

            long time = System.currentTimeMillis();

            BufferedImage biomeImage = new BufferedImage(height * 16, width * 16, BufferedImage.TYPE_INT_RGB);
            BufferedImage tempImage = new BufferedImage(height * 16, width * 16, BufferedImage.TYPE_INT_RGB);

            int image_x = 0;
            int image_y = 0;

            for (int x = -height / 2; x < height / 2; x++)
                for (int z = -width / 2; z < width / 2; z++)
                {
                    long time2 = System.currentTimeMillis();

                    if (time2 < time)
                        time = time2;

                    if (time2 > time + 2000L)
                    {
                        sender.sendMessage(BaseCommand.MessageColor + Integer.toString((x + height / 2) * 100 / height) + "%");
                        time = time2;
                    }

                    BiomeBuffer = world.getWorldChunkManager().a(BiomeBuffer, x * 16, z * 16, 16, 16);
                    tempArray = world.getWorldChunkManager().getTemperatures(tempArray, x * 16, z * 16, 16, 16);
                    for (int x1 = 0; x1 < 16; x1++)
                        for (int z1 = 0; z1 < 16; z1++)
                        {

                            switch (this.angle)
                            {
                                case d0:
                                    image_x = width * 16 - ((z + width / 2) * 16 + z1 + 1);
                                    image_y = (x + height / 2) * 16 + x1;
                                    break;
                                case d90:
                                    image_x = height * 16 - ((x + height / 2) * 16 + x1 + 1);
                                    image_y = (z + width / 2) * 16 + z1;
                                    break;
                                case d180:
                                    image_x = width * 16 - ((z + width / 2) * 16 + z1 + 1);
                                    image_y = height * 16 - ((x + height / 2) * 16 + x1 + 1);
                                    break;
                                case d270:
                                    image_x = (x + height / 2) * 16 + x1;
                                    image_y = (z + width / 2) * 16 + z1;
                                    break;
                            }

                            biomeImage.setRGB(image_x, image_y, Colors[BiomeBuffer[x1 + 16 * z1].F]);

                            Color tempColor = Color.getHSBColor(0.7f - tempArray[x1 + 16 * z1] * 0.7f, 0.9f, tempArray[x1 + 16 * z1] * 0.7f + 0.3f);


                            tempImage.setRGB(image_x, image_y, tempColor.getRGB());

                        }
                }

            sender.sendMessage(BaseCommand.MessageColor + "Writing images...");
            PNGImageWriter PngEncoder = new PNGImageWriter(new PNGImageWriterSpi());
            ImageOutputStream imageOutput = new FileCacheImageOutputStream(new FileOutputStream(world.worldData.name + "_biome.png", false), null);
            PngEncoder.setOutput(imageOutput);
            PngEncoder.write(biomeImage);

            imageOutput = new FileCacheImageOutputStream(new FileOutputStream(world.worldData.name + "_temperature.png", false), null);
            PngEncoder.setOutput(imageOutput);
            PngEncoder.write(tempImage);

            PngEncoder.dispose();

            sender.sendMessage(BaseCommand.MessageColor + "Done");

        } catch (Exception e1)
        {
            e1.printStackTrace();
        }
        MapWriter.isWork = false;
    }


}
