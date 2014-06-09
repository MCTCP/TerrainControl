package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.helpers.MathHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

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
                        LocalMaterialData material = world.getMaterial(xLake, yLake, zLake);
                        if (material.isMaterial(DefaultMaterial.AIR) || material.isMaterial(DefaultMaterial.BEDROCK))
                        {
                            // Don't replace air or bedrock
                            continue;
                        }

                        double xBounds = (xLake + 0.5D - xAdjusted) / (horizontalSize / 2.0D);
                        double yBounds = (yLake + 0.5D - yAdjusted) / (verticalSize / 2.0D);
                        double zBounds = (zLake + 0.5D - zAdjusted) / (horizontalSize / 2.0D);
                        if (xBounds * xBounds + yBounds * yBounds + zBounds * zBounds >= 1.0D)
                            continue;
                        LocalMaterialData materialBelow = world.getMaterial(xLake, yLake - 1, zLake);
                        if (materialBelow.isMaterial(DefaultMaterial.AIR))
                        {
                            // Air block, also set position above to air
                            world.setBlock(xLake, yLake, zLake, materialBelow);
                        } else
                        {
                            // Not air, set position above to water
                            world.setBlock(xLake, yLake, zLake, material);
                        }
                    }
        }
    }

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        material = TerrainControl.toLocalMaterialData(DefaultMaterial.STATIONARY_WATER, 0);

        assureSize(6, args);
        minSize = readInt(args.get(0), 1, 25);
        maxSize = readInt(args.get(1), minSize + 1, 60);
        frequency = readInt(args.get(2), 1, 100);
        rarity = readRarity(args.get(3));
        minAltitude = readInt(args.get(4), TerrainControl.WORLD_DEPTH, TerrainControl.WORLD_HEIGHT);
        maxAltitude = readInt(args.get(5), minAltitude + 1, TerrainControl.WORLD_HEIGHT);
    }

    @Override
    public String makeString()
    {
        return "UnderGroundLake(" + minSize + "," + maxSize + "," + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + ")";
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 29 * hash + super.hashCode();
        hash = 29 * hash + this.minSize;
        hash = 29 * hash + this.maxSize;
        hash = 29 * hash + this.minAltitude;
        hash = 29 * hash + this.maxAltitude;
        return hash;
    }

    @Override
    public boolean equals(Object other)
    {
        if (!super.equals(other))
            return false;
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (getClass() != other.getClass())
            return false;
        final UndergroundLakeGen compare = (UndergroundLakeGen) other;
        return this.maxAltitude == compare.maxAltitude
               && this.minAltitude == compare.minAltitude
               && this.minSize == compare.minSize
               && this.maxSize == compare.maxSize;
    }

}
