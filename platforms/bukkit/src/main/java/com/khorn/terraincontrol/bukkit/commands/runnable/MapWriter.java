package com.khorn.terraincontrol.bukkit.commands.runnable;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.commands.BaseCommand;
import com.khorn.terraincontrol.bukkit.util.WorldHelper;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.logging.LogMarker;
import net.minecraft.server.v1_7_R1.BiomeBase;
import net.minecraft.server.v1_7_R1.World;
import org.bukkit.command.CommandSender;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class MapWriter implements Runnable
{
    public static final int[] defaultColors = {0x3333FF, 0x999900, 0xFFCC33, 0x333300, 0x00FF00, 0x007700, 0x99cc66, 0x00CCCC, 0, 0,
            0xFFFFFF, 0x66FFFF, 0xCCCCCC, 0xCC9966, 0xFF33cc, 0xff9999, 0xFFFF00, 0x996600, 0x009900, 0x003300, 0x666600};

    public static boolean isWorking = false;

    private World world;
    private int size;
    private CommandSender sender;
    private Angle angle;
    private int offsetX;
    private int offsetZ;
    private String label;

    public enum Angle
    {
        d0,
        d90,
        d180,
        d270
    }

    public MapWriter(World _world, int _size, Angle _angle, CommandSender _sender, int _offsetX, int _offsetZ, String _label)
    {
        this.world = _world;
        this.size = _size;
        this.sender = _sender;
        this.angle = _angle;
        this.offsetX = _offsetX;
        this.offsetZ = _offsetZ;
        this.label = _label;
    }

    /**
     * Gets the colors of all biomes, indexed by biome id.
     * 
     * @param world The world to get the colors from. Doesn't have to be
     *            managed by Terrain Control.
     * @return The colors, indexed by biome id.
     */
    private int[] getColors(World world)
    {
        TerrainControl.log(LogMarker.TRACE, "BukkitWorld::UUID:: {}", world.getDataManager().getUUID());
        LocalWorld bukkitWorld = WorldHelper.toLocalWorld(world);
        if (bukkitWorld == null)
        {
            TerrainControl.log(LogMarker.WARN, "BukkitWorld is null :: Make sure you add `{}` to bukkit.yml", (Object) world.getWorld()
                    .getName());
            return defaultColors;
        }

        int[] colors = defaultColors;
        colors = new int[bukkitWorld.getSettings().biomes.length];
        TerrainControl.log(LogMarker.TRACE, "BukkitWorld settings biomes.length::{}", bukkitWorld.getSettings().biomes.length);

        for (LocalBiome biome : bukkitWorld.getSettings().biomes)
        {
            if (biome != null)
            {
                BiomeConfig biomeConfig = biome.getBiomeConfig();
                int color = biomeConfig.biomeColor;
                colors[biome.getIds().getGenerationId()] = color;
            }
        }
        return colors;
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
        LocalWorld localWorld = WorldHelper.toLocalWorld(world);

        int[] colors = this.getColors(world);

        sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Generating map...");

        BiomeBase[] biomeBuffer = new BiomeBase[256];
        long time = System.currentTimeMillis();

        BufferedImage biomeImage = new BufferedImage(height * 16, width * 16, BufferedImage.TYPE_INT_RGB);
        BufferedImage temperatureImage = new BufferedImage(height * 16, width * 16, BufferedImage.TYPE_INT_RGB);

        int imageX = 0;
        int imageY = 0;

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

                biomeBuffer = world.getWorldChunkManager().getBiomeBlock(biomeBuffer, offsetX + x * 16, offsetZ + z * 16, 16, 16);
                for (int x1 = 0; x1 < 16; x1++)
                {
                    for (int z1 = 0; z1 < 16; z1++)
                    {

                        switch (this.angle)
                        {
                            case d0:
                                imageX = (x + height / 2) * 16 + x1;
                                imageY = (z + width / 2) * 16 + z1;
                                break;
                            case d90:
                                imageX = width * 16 - ((z + width / 2) * 16 + z1 + 1);
                                imageY = (x + height / 2) * 16 + x1;
                                break;
                            case d180:
                                imageX = height * 16 - ((x + height / 2) * 16 + x1 + 1);
                                imageY = width * 16 - ((z + width / 2) * 16 + z1 + 1);
                                break;
                            case d270:
                                imageX = (z + width / 2) * 16 + z1;
                                imageY = height * 16 - ((x + height / 2) * 16 + x1 + 1);
                                break;
                        }

                        int arrayPosition = x1 + 16 * z1;
                        int biomeId = WorldHelper.getGenerationId(biomeBuffer[arrayPosition]);
                        try
                        {
                            // Biome color
                            biomeImage.setRGB(imageX, imageY, colors[biomeId]);

                            // Temperature
                            Color temperatureColor = getBiomeTemperatureColor(biomeBuffer[arrayPosition], localWorld);
                            temperatureImage.setRGB(imageX, imageY, temperatureColor.getRGB());
                        } catch (ArrayIndexOutOfBoundsException ex)
                        {
                            TerrainControl.log(LogMarker.TRACE, "BiomeBuff Idx::{}<{}x/{}z>, Len::{}, ID::{} | Colors Len::{}",
                                    new Object[] {arrayPosition, x1, z1, biomeBuffer.length, WorldHelper.getGenerationId(biomeBuffer[arrayPosition]), colors.length});
                        }
                    }
                }
            }
        }

        sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Writing images...");

        try
        {
            // Write biome colors
            ImageIO.write(biomeImage, "png", new File(label + world.worldData.getName() + "_biome.png"));

            // Write temperatures
            ImageIO.write(temperatureImage, "png", new File(label + world.worldData.getName() + "_temperature.png"));

            sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Done");
        } catch (IOException e)
        {
            sender.sendMessage(BaseCommand.ERROR_COLOR + "Exception while writing images: " + e.getLocalizedMessage());
            TerrainControl.log(LogMarker.ERROR, "Failed to write image.");
            TerrainControl.printStackTrace(LogMarker.ERROR, e);
        }

        MapWriter.isWorking = false;
    }

    /**
     * Gets the temperature color of a single biome. Starts at blue, goes to
     * green, red and darker red for increasing temperatures.
     * 
     * @param biome The biome to get the temperature from.
     * @param world The world the biome is in. May be null if the world isn't
     *            managed by Terrain Control.
     * @return The temperature color.
     */
    private Color getBiomeTemperatureColor(BiomeBase biome, LocalWorld world)
    {
        float temperature;
        if (world != null)
        {
            temperature = world.getBiomeById(WorldHelper.getGenerationId(biome)).getBiomeConfig().biomeTemperature;
        } else
        {
            temperature = biome.temperature;
        }

        // Prevents us from going around the color wheel twice or getting into
        // the purple colors
        float cappedTemperature = Math.min(1.0f, temperature);

        return Color.getHSBColor(0.7f - cappedTemperature * 0.7f, 0.9f, temperature * 0.7f + 0.3f);
    }

}
