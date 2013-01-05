package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.MathHelper;

import java.util.List;
import java.util.Random;

public class UndergroundLakeGen extends Resource
{
    private int minSize;
    private int maxSize;
    private int minAltitude;
    private int maxAltitude;

    @Override
    public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z)
    {
        int y = rand.nextInt(maxAltitude - minAltitude) + minAltitude;

        if (y >= world.getHighestBlockYAt(x, z))
            return;
        int size = rand.nextInt(maxSize - minSize) + minSize;

        float mPi = rand.nextFloat() * 3.141593F;

        double x1 = x + 8 + MathHelper.sin(mPi) * size / 8.0F;
        double x2 = x + 8 - MathHelper.sin(mPi) * size / 8.0F;
        double z1 = z + 8 + MathHelper.cos(mPi) * size / 8.0F;
        double z2 = z + 8 - MathHelper.cos(mPi) * size / 8.0F;

        double y1 = y + rand.nextInt(3) + 2;
        double y2 = y + rand.nextInt(3) + 2;

        for (int i = 0; i <= size; i++)
        {
            double xAdjusted = x1 + (x2 - x1) * i / size;
            double yAdjusted = y1 + (y2 - y1) * i / size;
            double zAdjusted = z1 + (z2 - z1) * i / size;

            double horizontalSizeMultiplier = rand.nextDouble() * size / 16.0D;
            double verticalSizeMultiplier = rand.nextDouble() * size / 32.0D;
            double horizontalSize = (MathHelper.sin(i * 3.141593F / size) + 1.0F) * horizontalSizeMultiplier + 1.0D;
            double verticalSize = (MathHelper.sin(i * 3.141593F / size) + 1.0F) * verticalSizeMultiplier + 1.0D;

            for (int xLake = (int) (xAdjusted - horizontalSize / 2.0D); xLake <= (int) (xAdjusted + horizontalSize / 2.0D); xLake++)
                for (int yLake = (int) (yAdjusted - verticalSize / 2.0D); yLake <= (int) (yAdjusted + verticalSize / 2.0D); yLake++)
                    for (int zLake = (int) (zAdjusted - horizontalSize / 2.0D); zLake <= (int) (zAdjusted + horizontalSize / 2.0D); zLake++)
                    {
                        if (world.getTypeId(xLake, yLake, zLake) == 0)
                            continue;
                        double xBounds = (xLake + 0.5D - xAdjusted) / (horizontalSize / 2.0D);
                        double yBounds = (yLake + 0.5D - yAdjusted) / (verticalSize / 2.0D);
                        double zBounds = (zLake + 0.5D - zAdjusted) / (horizontalSize / 2.0D);
                        if (xBounds * xBounds + yBounds * yBounds + zBounds * zBounds >= 1.0D)
                            continue;
                        int uBlock = world.getTypeId(xLake, yLake - 1, zLake);
                        if (uBlock != 0) // not air
                            world.setBlock(xLake, yLake, zLake, DefaultMaterial.WATER.id, 0, false, false, false);
                        else
                            // Air block
                            world.setBlock(xLake, yLake, zLake, 0, 0, false, false, false);
                    }
        }
    }

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        blockId = DefaultMaterial.WATER.id; // Hardcoded for now

        assureSize(6, args);
        minSize = readInt(args.get(0), 1, 25);
        maxSize = readInt(args.get(1), minSize, 60);
        frequency = readInt(args.get(2), 1, 100);
        rarity = readInt(args.get(3), 1, 100);
        minAltitude = readInt(args.get(4), TerrainControl.worldDepth, TerrainControl.worldHeight);
        maxAltitude = readInt(args.get(5), minAltitude + 1, TerrainControl.worldHeight);
    }

    @Override
    public String makeString()
    {
        return "UnderGroundLake(" + minSize + "," + maxSize + "," + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + ")";
    }
}
