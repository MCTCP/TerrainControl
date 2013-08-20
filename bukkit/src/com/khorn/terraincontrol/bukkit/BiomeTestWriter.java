package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.biomegenerators.ArraysCache;
import com.khorn.terraincontrol.biomegenerators.ArraysCacheManager;
import com.khorn.terraincontrol.biomegenerators.NormalBiomeGenerator;
import com.khorn.terraincontrol.biomegenerators.OutputType;
import com.khorn.terraincontrol.biomegenerators.biomelayers.Layer;
import com.khorn.terraincontrol.bukkit.commands.BaseCommand;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.sun.imageio.plugins.png.PNGImageWriter;
import com.sun.imageio.plugins.png.PNGImageWriterSpi;
import org.bukkit.command.CommandSender;

import javax.imageio.stream.FileCacheImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.util.logging.Level;

public class BiomeTestWriter implements Runnable
{
    public static final int[] defaultColors = {0x3333FF, 0x999900, 0xFFCC33, 0x333300, 0x00FF00, 0x007700, 0x99cc66, 0x00CCCC, 0, 0, 0xFFFFFF, 0x66FFFF, 0xCCCCCC, 0xCC9966, 0xFF33cc, 0xff9999, 0xFFFF00, 0x996600, 0x009900, 0x003300, 0x666600};

    public static boolean isWorking = false;

    private TCPlugin plugin;
    private BukkitWorld world;
    private CommandSender sender;


    public BiomeTestWriter(TCPlugin _plugin, BukkitWorld _world, CommandSender _sender)
    {
        this.plugin = _plugin;
        this.world = _world;
        this.sender = _sender;
    }


    public void run()
    {
        if (BiomeTestWriter.isWorking)
        {
            sender.sendMessage(BaseCommand.ERROR_COLOR + "Another instance of map writer is running");
            return;
        }

        BiomeTestWriter.isWorking = true;

        try
        {
            int[] colors = defaultColors;

            BukkitWorld bukkitWorld = world;
            if (bukkitWorld != null)
            {
                colors = new int[bukkitWorld.getSettings().biomeConfigs.length];

                for (BiomeConfig biomeConfig : bukkitWorld.getSettings().biomeConfigs)
                {
                    if (biomeConfig != null)
                    {
                        try
                        {
                            int color = Integer.decode(biomeConfig.BiomeColor);
                            if (color <= 0xFFFFFF)
                                colors[biomeConfig.Biome.getId()] = color;
                        } catch (NumberFormatException ex)
                        {
                            TerrainControl.log(Level.WARNING, "Wrong color in " + biomeConfig.Biome.getName());
                            sender.sendMessage(BaseCommand.ERROR_COLOR + "Wrong color in " + biomeConfig.Biome.getName());
                        }
                    }
                }
            }


            sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Generating test...");
            //float[] tempArray = new float[256];
            //BiomeBase[] BiomeBuffer = new BiomeBase[256];

            //long time = System.currentTimeMillis();

            //BufferedImage biomeImage = new BufferedImage(height * 16, width * 16, BufferedImage.TYPE_INT_RGB);
            //BufferedImage tempImage = new BufferedImage(height * 16, width * 16, BufferedImage.TYPE_INT_RGB);

            // int image_x = 0;
            //int image_y = 0;



            PNGImageWriter PngEncoder = new PNGImageWriter(new PNGImageWriterSpi());

            Layer biomeLayer = ((NormalBiomeGenerator) world.biomeManager).biomeLayer;


            BufferedImage biomeImage[] = new BufferedImage[32];


            for (int z_chunk = 0; z_chunk < 64; z_chunk++)
            {

                for (int x_chunk = 0; x_chunk < 64; x_chunk++)
                {
                    ArraysCache cache = ArraysCacheManager.GetCache();
                    cache.outputType = OutputType.BIOME_TEST;
                    int[] arrayOfInt = biomeLayer.GetBiomes(cache, x_chunk * 64, z_chunk * 64, 16, 16);


                    for (int i = 0; i < cache.BiomeTestOutput.length; i++)
                    {
                        if (cache.BiomeTestOutput[i] == null)
                            continue;

                        int size = cache.BiomeTestSize[i];

                        if (biomeImage[i] == null)
                            biomeImage[i] = new BufferedImage(32 * 64 , 32 * 64 , BufferedImage.TYPE_INT_RGB);

                        for (int z = 0; z < size; z++)
                        {
                            for (int x = 0; x < size; x++)
                            {
                                int biomeId = cache.BiomeTestOutput[i][x + z * size];

                                if (!cache.BiomeTestConverted[i])
                                {

                                    if ((biomeId & Layer.LandBit) == 0)
                                    {
                                        biomeImage[i].setRGB(8 + cache.BiomeTestZ[i] + z, 8 + cache.BiomeTestX[i] + x, colors[0]);
                                    } else
                                    {
                                        if ((biomeId & Layer.BiomeBits) != 0)
                                            biomeImage[i].setRGB(8 + cache.BiomeTestZ[i] + z, 8 + cache.BiomeTestX[i] + x, colors[biomeId & Layer.BiomeBits]);
                                    }
                                } else
                                    biomeImage[i].setRGB(8 + cache.BiomeTestZ[i] + z, 8 + cache.BiomeTestX[i] + x, colors[biomeId]);

                            }

                        }


                    }


                    ArraysCacheManager.ReleaseCache(cache);
                }
            }


            for (int i = 0; i < biomeImage.length; i++)
            {
                if (biomeImage[i] == null)
                    continue;

                FileOutputStream fileOutput = new FileOutputStream(world.getName() + "_" + i + "biomeTest.png", false);
                ImageOutputStream imageOutput = new FileCacheImageOutputStream(fileOutput, null);
                PngEncoder.setOutput(imageOutput);
                PngEncoder.write(biomeImage[i]);
                imageOutput.close();
                fileOutput.close();


            }


            PngEncoder.dispose();


            sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Done");

        } catch (Exception e1)
        {
            e1.printStackTrace();
        }
        BiomeTestWriter.isWorking = false;
    }
}
