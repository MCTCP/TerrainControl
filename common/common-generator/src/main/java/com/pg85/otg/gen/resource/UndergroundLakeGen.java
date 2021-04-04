package com.pg85.otg.gen.resource;

import com.pg85.otg.config.biome.Resource;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

import java.util.List;
import java.util.Random;

public class UndergroundLakeGen extends Resource
{
    private final int maxAltitude;
    private final int maxSize;
    private final int minAltitude;
    private final int minSize;

    public UndergroundLakeGen(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
    {
    	super(biomeConfig, args, logger, materialReader);
        material = LocalMaterials.WATER;
        assureSize(6, args);
        minSize = readInt(args.get(0), 1, 25);
        maxSize = readInt(args.get(1), minSize, 60);
        frequency = readInt(args.get(2), 1, 100);
        rarity = readRarity(args.get(3));
        minAltitude = readInt(args.get(4), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
        maxAltitude = readInt(args.get(5), minAltitude, Constants.WORLD_HEIGHT - 1);
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

    @Override
    public int getPriority()
    {
        return 3;
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
    public String toString()
    {
        return "UnderGroundLake(" + minSize + "," + maxSize + "," + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + ")";
    }

    @Override
    public void spawn(IWorldGenRegion worldGenRegion, Random rand, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
    {
    	// Make sure we stay within population bounds, anything outside won't be spawned (unless it's in an existing chunk).
    	
        int y = RandomHelper.numberInRange(rand, minAltitude, maxAltitude);
        if (y >= worldGenRegion.getHighestBlockAboveYAt(x, z, chunkBeingPopulated))
        {
            return;
        }
        
        int size = RandomHelper.numberInRange(rand, minSize, maxSize);

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
            {
                for (int yLake = (int) (yAdjusted - verticalSize / 2.0D); yLake <= (int) (yAdjusted + verticalSize / 2.0D); yLake++)
                {
                    for (int zLake = (int) (zAdjusted - horizontalSize / 2.0D); zLake <= (int) (zAdjusted + horizontalSize / 2.0D); zLake++)
                    {
                        LocalMaterialData material = worldGenRegion.getMaterial(xLake, yLake, zLake, chunkBeingPopulated);
                        if (material == null || material.isEmptyOrAir() || material.isMaterial(LocalMaterials.BEDROCK))
                        {
                            // Don't replace air or bedrock
                            continue;
                        }

                        double xBounds = (xLake + 0.5D - xAdjusted) / (horizontalSize / 2.0D);
                        double yBounds = (yLake + 0.5D - yAdjusted) / (verticalSize / 2.0D);
                        double zBounds = (zLake + 0.5D - zAdjusted) / (horizontalSize / 2.0D);
                        if (xBounds * xBounds + yBounds * yBounds + zBounds * zBounds >= 1.0D)
                        {
                            continue;
                        }

                        LocalMaterialData materialBelow = worldGenRegion.getMaterial(xLake, yLake - 1, zLake, chunkBeingPopulated);
                        if (materialBelow != null && materialBelow.isAir())
                        {
                            // Air block, also set position above to air
                        	worldGenRegion.setBlock(xLake, yLake, zLake, materialBelow, null, chunkBeingPopulated, false);
                        } else {
                            // Not air, set position above to water
                        	worldGenRegion.setBlock(xLake, yLake, zLake, material, null, chunkBeingPopulated, false);
                        }
                    }
                }
            }
        }
    }
}
