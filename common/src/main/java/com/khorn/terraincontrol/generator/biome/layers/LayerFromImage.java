package com.khorn.terraincontrol.generator.biome.layers;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.biome.ArraysCache;
import com.khorn.terraincontrol.logging.LogMarker;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class LayerFromImage extends Layer
{

    private int[] biomeMap;
    private int mapHeight;
    private int mapWidth;
    private int fillBiome = 0;
    private int xOffset;
    private int zOffset;
    private WorldConfig.ImageMode imageMode;

    public LayerFromImage(long seed, Layer childLayer, WorldConfig config, LocalWorld world)
    {
        super(seed);
        this.child = childLayer;
        xOffset = config.imageXOffset;
        zOffset = config.imageZOffset;
        this.imageMode = config.imageMode;
        this.fillBiome = world.getBiomeByName(config.imageFillBiome).getIds().getGenerationId();

        // Read from file
        try
        {
            final File image = new File(config.settingsDir, config.imageFile);
            final BufferedImage map = ImageIO.read(image);

            this.mapWidth = map.getWidth(null);
            this.mapHeight = map.getHeight(null);
            int[] colorMap = new int[this.mapHeight * this.mapWidth];

            map.getRGB(0, 0, this.mapWidth, this.mapHeight, colorMap, 0, this.mapWidth);

            // Rotate RGBs if need
            switch (config.imageOrientation)
            {
                case North:
                    // Default behavior - nothing to rotate
                    break;
                case South:
                    // Rotate picture 180 degrees
                    int[] colorMap180 = new int[colorMap.length];
                    for (int y = 0; y < this.mapHeight; y++)
                        for (int x = 0; x < this.mapWidth; x++)
                            colorMap180[(this.mapHeight - 1 - y) * this.mapWidth + this.mapWidth - 1 - x] = colorMap[y * this.mapWidth + x];
                    colorMap = colorMap180;
                    break;
                case West:
                    // Rotate picture CW
                    int[] colorMapCW = new int[colorMap.length];
                    for (int y = 0; y < this.mapHeight; y++)
                        for (int x = 0; x < this.mapWidth; x++)
                            colorMapCW[x * this.mapHeight + this.mapHeight - 1 - y] = colorMap[y * this.mapWidth + x];
                    colorMap = colorMapCW;
                    this.mapWidth = map.getHeight(null);
                    this.mapHeight = map.getWidth(null);
                    break;
                case East:
                    // Rotate picture CCW
                    int[] colorMapCCW = new int[colorMap.length];
                    for (int y = 0; y < this.mapHeight; y++)
                        for (int x = 0; x < this.mapWidth; x++)
                            colorMapCCW[(this.mapWidth - 1 - x) * this.mapHeight + y] = colorMap[y * this.mapWidth + x];
                    colorMap = colorMapCCW;
                    this.mapWidth = map.getHeight(null);
                    this.mapHeight = map.getWidth(null);
                    break;
            }

            this.biomeMap = new int[colorMap.length];

            for (int nColor = 0; nColor < colorMap.length; nColor++)
            {
                int color = colorMap[nColor] & 0x00FFFFFF;

                if (config.biomeColorMap.containsKey(color))
                    this.biomeMap[nColor] = config.biomeColorMap.get(color);
                else
                    this.biomeMap[nColor] = fillBiome;
            }
        } catch (IOException ioexception)
        {
            TerrainControl.log(LogMarker.FATAL, ioexception.getStackTrace().toString());
        }
    }

    @Override
    public int[] getInts(ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int[] resultBiomes = cache.getArray(xSize * zSize);

        switch (this.imageMode)
        {
            case Repeat:
                for (int zi = 0; zi < zSize; zi++)
                    for (int xi = 0; xi < xSize; xi++)
                    {
                        int Buffer_x = (x + xi - xOffset) % this.mapWidth;
                        int Buffer_z = (z + zi - zOffset) % this.mapHeight;

                        // Take care of negatives
                        if (Buffer_x < 0)
                            Buffer_x += this.mapWidth;
                        if (Buffer_z < 0)
                            Buffer_z += this.mapHeight;
                        resultBiomes[(xi + zi * xSize)] = this.biomeMap[Buffer_x + Buffer_z * this.mapWidth];
                    }
                return resultBiomes;
            case Mirror:
                // Improved repeat mode
                for (int zi = 0; zi < zSize; zi++)
                    for (int xi = 0; xi < xSize; xi++)
                    {
                        int Buffer_xq = (x + xi - xOffset) % (2 * this.mapWidth);
                        int Buffer_zq = (z + zi - zOffset) % (2 * this.mapHeight);
                        if (Buffer_xq < 0)
                            Buffer_xq += 2 * this.mapWidth;
                        if (Buffer_zq < 0)
                            Buffer_zq += 2 * this.mapHeight;
                        int Buffer_x = Buffer_xq % this.mapWidth;
                        int Buffer_z = Buffer_zq % this.mapHeight;
                        if (Buffer_xq >= this.mapWidth)
                            Buffer_x = this.mapWidth - 1 - Buffer_x;
                        if (Buffer_zq >= this.mapHeight)
                            Buffer_z = this.mapHeight - 1 - Buffer_z;
                        resultBiomes[(xi + zi * xSize)] = this.biomeMap[Buffer_x + Buffer_z * this.mapWidth];
                    }
                return resultBiomes;
            case ContinueNormal:
                int[] childBiomes = null;
                if (this.child != null)
                    childBiomes = this.child.getInts(cache, x, z, xSize, zSize);
                for (int zi = 0; zi < zSize; zi++)
                    for (int xi = 0; xi < xSize; xi++)
                    {
                        int Buffer_x = x + xi - xOffset;
                        int Buffer_z = z + zi - zOffset;
                        if (Buffer_x < 0 || Buffer_x >= this.mapWidth || Buffer_z < 0 || Buffer_z >= this.mapHeight)
                        {
                            if (childBiomes != null)
                                resultBiomes[(xi + zi * xSize)] = childBiomes[(xi + zi * xSize)];
                            else
                                resultBiomes[(xi + zi * xSize)] = this.fillBiome;
                        } else
                            resultBiomes[(xi + zi * xSize)] = this.biomeMap[Buffer_x + Buffer_z * this.mapWidth];
                    }
                break;
            case FillEmpty:
                // Some fastened version
                for (int zi = 0; zi < zSize; zi++)
                    for (int xi = 0; xi < xSize; xi++)
                    {
                        int Buffer_x = x + xi - xOffset;
                        int Buffer_z = z + zi - zOffset;
                        if (Buffer_x < 0 || Buffer_x >= this.mapWidth || Buffer_z < 0 || Buffer_z >= this.mapHeight)
                            resultBiomes[(xi + zi * xSize)] = this.fillBiome;
                        else
                            resultBiomes[(xi + zi * xSize)] = this.biomeMap[Buffer_x + Buffer_z * this.mapWidth];
                    }
                break;
        }
        return resultBiomes;
    }

}
