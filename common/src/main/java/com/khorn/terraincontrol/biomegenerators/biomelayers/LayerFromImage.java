package com.khorn.terraincontrol.biomegenerators.biomelayers;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.biomegenerators.ArraysCache;
import com.khorn.terraincontrol.configuration.WorldConfig;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class LayerFromImage extends Layer
{

    private int[] biomeMap;
    private int mapHeight;
    private int mapWidth;
    private int fillBiome = 0;
    private int xOffset;
    private int zOffset;
    private WorldConfig.ImageMode imageMode;

    public LayerFromImage(long paramLong, Layer child, WorldConfig config, LocalWorld world)
    {
        super(paramLong);
        this.child = child;
        xOffset = config.imageXOffset;
        zOffset = config.imageZOffset;
        this.imageMode = config.imageMode;
        this.fillBiome = world.getBiomeIdByName(config.imageFillBiome);

        // Read from file
        try
        {
            File image = new File(config.settingsDir, config.imageFile);
            BufferedImage map = ImageIO.read(image);

            // Rotate image if need
            switch(config.imageOrientation)
            {
               case West:
                  // Default TC behavior
                  break;
               case North:
                  // Rotate picture CW
                  BufferedImage rotatedCW = new BufferedImage(map.getHeight(), map.getWidth(), map.getType());
                  for(int y = 0; y < map.getHeight(); y++)
                     for(int x = 0; x < map.getWidth(); x++)
                        rotatedCW.setRGB(map.getHeight() - 1 - y, x, map.getRGB(x, y));
                  map = rotatedCW;
                  break;
               case South:
                  // Rotate picture CCW
                  BufferedImage rotatedCCW = new BufferedImage(map.getHeight(), map.getWidth(), map.getType());
                  for(int y = 0; y < map.getHeight(); y++)
                     for(int x = 0; x < map.getWidth(); x++)
                        rotatedCCW.setRGB(y, map.getWidth() - 1 - x, map.getRGB(x, y));
                  map = rotatedCCW;
                  break;
               case East:
                  // Rotate picture 180 degrees
                  BufferedImage rotated180 = new BufferedImage(map.getWidth(), map.getHeight(), map.getType());
                  for(int y = 0; y < map.getHeight(); y++)
                     for(int x = 0; x < map.getWidth(); x++)
                        rotated180.setRGB(map.getWidth() - 1 - x, map.getHeight() - 1 - y, map.getRGB(x, y));
                  map = rotated180;
                  break;
            }

            this.mapHeight = map.getHeight(null);
            this.mapWidth = map.getWidth(null);
            int[] colorMap = new int[this.mapHeight * this.mapWidth];
            this.biomeMap = new int[this.mapHeight * this.mapWidth];

            map.getRGB(0, 0, this.mapWidth, this.mapHeight, colorMap, 0, this.mapWidth);

            for (int i = 0; i < colorMap.length; i++)
            {
                int color = colorMap[i] & 0xFFFFFF;

                if (config.biomeColorMap.containsKey(color))
                    this.biomeMap[i] = config.biomeColorMap.get(color);
                else
                    this.biomeMap[i] = fillBiome;
            }
        } catch (IOException ioexception) {
            TerrainControl.log(Level.SEVERE, ioexception.getStackTrace().toString());
        }
    }

    @Override
    public int[] GetBiomes(ArraysCache arraysCache, int x, int z, int x_size, int z_size)
    {
        int[] resultBiomes = arraysCache.GetArray(x_size * z_size);
        
        if (this.imageMode == WorldConfig.ImageMode.Repeat)
        {
            for (int iz = 0; iz < z_size; iz++)
            {
                for (int ix = 0; ix < x_size; ix++)
                {
                    int Buffer_x = this.mapWidth - 1 - Math.abs((z + iz - zOffset) % this.mapWidth);
                    int Buffer_z = Math.abs((x + ix - xOffset) % this.mapHeight);
                    resultBiomes[(ix + iz * x_size)] = this.biomeMap[Buffer_x + Buffer_z * this.mapWidth];
                }
            }
        } else {
            int[] childBiomes = null;
            if (this.child != null)
                childBiomes = this.child.GetBiomes(arraysCache, x, z, x_size, z_size);
            for (int iz = 0; iz < z_size; iz++)
            {
                for (int ix = 0; ix < x_size; ix++)
                {
                    int Buffer_x = this.mapWidth - (z + iz - zOffset);
                    int Buffer_z = (x + ix - xOffset);
                    if (Buffer_x < 0 || Buffer_x >= this.mapWidth || Buffer_z < 0 || Buffer_z >= this.mapHeight)
                    {
                        if (childBiomes != null)
                            resultBiomes[(ix + iz * x_size)] = childBiomes[(ix + iz * x_size)];
                        else
                            resultBiomes[(ix + iz * x_size)] = this.fillBiome;
                    } else
                        resultBiomes[(ix + iz * x_size)] = this.biomeMap[Buffer_x + Buffer_z * this.mapWidth];
                }
            }
        }
        return resultBiomes;
    }
}
