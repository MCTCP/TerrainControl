package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.commands.BaseCommand;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.sun.imageio.plugins.png.PNGImageWriter;
import com.sun.imageio.plugins.png.PNGImageWriterSpi;
import net.minecraft.server.v1_6_R3.BiomeBase;
import net.minecraft.server.v1_6_R3.World;
import org.bukkit.command.CommandSender;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.util.logging.Level;

import javax.imageio.stream.FileCacheImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

public class MapWriter implements Runnable
{
    public static final int[] defaultColors =
    {
        0x3333FF, 0x999900, 0xFFCC33, 0x333300, 0x00FF00, 0x007700, 0x99cc66, 0x00CCCC, 0, 0, 0xFFFFFF, 0x66FFFF, 0xCCCCCC, 0xCC9966, 0xFF33cc, 0xff9999, 0xFFFF00, 0x996600, 0x009900, 0x003300, 0x666600
    };

    public static boolean isWorking = false;

    private TCPlugin plugin;
    private World world;
    private int size;
    private CommandSender sender;
    private Angle angle;
    private int offsetX;
    private int offsetZ;
    private String label;

    public enum Angle
    {
        d0, d90, d180, d270
    }

    public MapWriter(TCPlugin _plugin, World _world, int _size, Angle _angle, CommandSender _sender, int _offsetX, int _offsetZ, String _label)
    {
        this.plugin = _plugin;
        this.world = _world;
        this.size = _size;
        this.sender = _sender;
        this.angle = _angle;
        this.offsetX = _offsetX;
        this.offsetZ = _offsetZ;
        this.label = _label;
    }


    @Override
    public void run()
    {
        if (MapWriter.isWorking)
        {
            sender.sendMessage(BaseCommand.ERROR_COLOR + "Another instance of map writer is running");
            return;
        }

        MapWriter.isWorking = true;
        int height = size;
        int width = size;

        try
        {
            int[] colors = defaultColors;
            TerrainControl.log(Level.FINER, "BukkitWorld::UUID:: {0}", world.getDataManager().getUUID());
            BukkitWorld bukkitWorld = plugin.worlds.get(world.getDataManager().getUUID());
            if (bukkitWorld != null)
            {
                colors = new int[bukkitWorld.getSettings().biomeConfigs.length];
                TerrainControl.log(Level.FINER, "BukkitWorld settings biomeConfigs.length::{0}", bukkitWorld.getSettings().biomeConfigs.length);

                for (BiomeConfig biomeConfig : bukkitWorld.getSettings().biomeConfigs)
                {
                    if (biomeConfig != null)
                    {
                        try
                        {
                            int color = Integer.decode(biomeConfig.BiomeColor);
                            if (color <= 0xFFFFFF)
                            {
                                colors[biomeConfig.Biome.getId()] = color;
                            }
                        } catch (NumberFormatException ex)
                        {
                            TerrainControl.log(Level.WARNING, "Wrong color in {0}", biomeConfig.Biome.getName());
                            sender.sendMessage(BaseCommand.ERROR_COLOR + "Wrong color in " + biomeConfig.Biome.getName());
                        }
                    }
                }
            } else
            {
                TerrainControl.log(Level.WARNING, "BukkitWorld is null :: Make sure you add `{0}` to bukkit.yml", world.getWorld().getName());
            }

            sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Generating map...");
            float[] tempArray = new float[256];
            BiomeBase[] BiomeBuffer = new BiomeBase[256];

            long time = System.currentTimeMillis();

            BufferedImage biomeImage = new BufferedImage(height * 16, width * 16, BufferedImage.TYPE_INT_RGB);
            BufferedImage tempImage = new BufferedImage(height * 16, width * 16, BufferedImage.TYPE_INT_RGB);

            int image_x = 0;
            int image_y = 0;


            for (int x = -height / 2; x < height / 2; x++)
            {
                for (int z = -width / 2; z < width / 2; z++)
                {
                    long time2 = System.currentTimeMillis();

                    if (time2 < time)
                    {
                        time = time2;
                    }

                    if (time2 > time + 2000L)
                    {
                        sender.sendMessage(BaseCommand.MESSAGE_COLOR + ((x + height / 2) * 100 / height) + "%");
                        time = time2;
                    }

                    BiomeBuffer = world.getWorldChunkManager().getBiomeBlock(BiomeBuffer, offsetX + x * 16, offsetZ + z * 16, 16, 16);
                    tempArray = world.getWorldChunkManager().getTemperatures(tempArray, offsetX + x * 16, offsetZ + z * 16, 16, 16);
                    for (int x1 = 0; x1 < 16; x1++)
                    {
                        for (int z1 = 0; z1 < 16; z1++)
                        {
                            
                            switch (this.angle)
                            {
                                case d0:
                                    image_x = (x + height / 2) * 16 + x1;
                                    image_y = (z + width / 2) * 16 + z1;
                                    break;
                                case d90:
                                    image_x = width * 16 - ((z + width / 2) * 16 + z1 + 1);
                                    image_y = (x + height / 2) * 16 + x1;
                                    break;
                                case d180:
                                    image_x = height * 16 - ((x + height / 2) * 16 + x1 + 1);
                                    image_y = width * 16 - ((z + width / 2) * 16 + z1 + 1);
                                    break;
                                case d270:
                                    image_x = (z + width / 2) * 16 + z1;
                                    image_y = height * 16 - ((x + height / 2) * 16 + x1 + 1);
                                    break;
                            }

                            //>>	This allows TC to map any world, even if it is not configured in the bukkit.yml file
                            int t = x1 + 16 * z1;
                            int bbid = BiomeBuffer[t].id;
                            try
                            {
                                biomeImage.setRGB(image_x, image_y, colors[bbid]);

                                Color tempColor = Color.getHSBColor(0.7f - tempArray[t] * 0.7f, 0.9f, tempArray[t] * 0.7f + 0.3f);

                                tempImage.setRGB(image_x, image_y, tempColor.getRGB());
                            } catch (ArrayIndexOutOfBoundsException ex)
                            {
                                TerrainControl.log(Level.FINEST, "BiomeBuff Idx::{0}<{4}x/{5}z>, Len::{1}, ID::{2} | Colors Len::{3}", new Object[]
                                {
                                    t, BiomeBuffer.length, BiomeBuffer[t].id, colors.length, x1, z1
                                });

                            }
                        }
                    }
                }
            }

            sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Writing images...");
            PNGImageWriter PngEncoder = new PNGImageWriter(new PNGImageWriterSpi());

            // Write biome colors
            FileOutputStream fileOutput = new FileOutputStream(label + world.worldData.getName() + "_biome.png", false);
            ImageOutputStream imageOutput = new FileCacheImageOutputStream(fileOutput, null);
            PngEncoder.setOutput(imageOutput);
            PngEncoder.write(biomeImage);
            imageOutput.close();
            fileOutput.close();

            // Write temperatures
            fileOutput = new FileOutputStream(label + world.worldData.getName() + "_temperature.png", false);
            imageOutput = new FileCacheImageOutputStream(fileOutput, null);
            PngEncoder.setOutput(imageOutput);
            PngEncoder.write(tempImage);
            imageOutput.close();
            fileOutput.close();

            PngEncoder.dispose();

            sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Done");

        } catch (Exception e)
        {
            TerrainControl.log(Level.SEVERE, e.getStackTrace().toString());
        }
        MapWriter.isWorking = false;
    }
    
}
