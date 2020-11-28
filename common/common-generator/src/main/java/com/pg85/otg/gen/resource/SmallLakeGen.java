package com.pg85.otg.gen.resource;

import com.pg85.otg.config.biome.Resource;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

import java.util.List;
import java.util.Random;

/**
 * Generates a small lake. The end result will invariably look poor and cause issues so it's recommended to create custom objects for your lakes.
 */
public class SmallLakeGen extends Resource
{
    private int maxAltitude;
    private int minAltitude;

    public SmallLakeGen(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
    {
    	super(biomeConfig, args, logger, materialReader);
        assureSize(5, args);
        material = materialReader.readMaterial(args.get(0));
        frequency = readInt(args.get(1), 1, 100);
        rarity = readRarity(args.get(2));
        minAltitude = readInt(args.get(3), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
        maxAltitude = readInt(args.get(4), minAltitude, Constants.WORLD_HEIGHT - 1);
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
        final SmallLakeGen compare = (SmallLakeGen) other;

        return this.minAltitude == compare.minAltitude
               && this.maxAltitude == compare.maxAltitude;
    }

    @Override
    public int getPriority()
    {
        return 1;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 41 * hash + super.hashCode();
        hash = 41 * hash + this.minAltitude;
        hash = 41 * hash + this.maxAltitude;
        return hash;
    }

    @Override
    public String toString()
    {
        return "SmallLake(" + material + "," + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + ")";
    }

    @Override
    public void spawn(IWorldGenRegion world, Random rand, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
    {
        // Make sure we stay within population bounds, anything outside won't be spawned (unless it's in an existing chunk).

        if (villageInChunk)
        {
            // Lakes and villages don't like each other.
            return;
        }

        x -= 8;
        z -= 8;

        int y = RandomHelper.numberInRange(rand, minAltitude, maxAltitude);

        // Search any free space
        LocalMaterialData worldMaterial;
        while (y > 5 && (worldMaterial = world.getMaterial(x, y, z, chunkBeingPopulated)) != null && worldMaterial.isAir())
        {
            y--;
        }

        if (y <= 4)
        {
            return;
        }

        // y = floor
        y -= 4;

//        parseMaterials(world, material, null);

        LocalMaterialData localMaterialData;
        LocalMaterialData localMaterialData2;
        LocalMaterialData air = LocalMaterials.AIR;
        boolean[] lakeMask = new boolean[2048];

        boolean flag;
        for (int j = 0; j < rand.nextInt(4) + 4; j++)
        {
            double lakeSizeX = rand.nextDouble() * 6.0D + 3.0D;
            double lakeSizeY = rand.nextDouble() * 4.0D + 2.0D;
            double lakeSizeZ = rand.nextDouble() * 6.0D + 3.0D;

            double scaledLakeX = rand.nextDouble() * (16.0D - lakeSizeX - 2.0D) + 1.0D + lakeSizeX / 2.0D;
            double scaledLakeY = rand.nextDouble() * (8.0D - lakeSizeY - 4.0D) + 2.0D + lakeSizeY / 2.0D;
            double scaledLakeZ = rand.nextDouble() * (16.0D - lakeSizeZ - 2.0D) + 1.0D + lakeSizeZ / 2.0D;

            for (int lakeX = 1; lakeX < 15; lakeX++)
            {
                for (int lakeZ = 1; lakeZ < 15; lakeZ++)
                {
                    for (int lakeY = 1; lakeY < 7; lakeY++)
                    {
                        double distX = (lakeX - scaledLakeX) / (lakeSizeX / 2.0D);
                        double distY = (lakeY - scaledLakeY) / (lakeSizeY / 2.0D);
                        double distZ = (lakeZ - scaledLakeZ) / (lakeSizeZ / 2.0D);
                        double distance = distX * distX + distY * distY + distZ * distZ;
                        if (distance >= 1.0D)
                        {
                            continue;
                        }
                        lakeMask[((lakeX * 16 + lakeZ) * 8 + lakeY)] = true;
                    }
                }
            }
        }


        for (int lakeX = 0; lakeX < 16; lakeX++)
        {
            for (int lakeZ = 0; lakeZ < 16; lakeZ++)
            {
                for (int lakeY = 0; lakeY < 8; lakeY++)
                {
                    flag = (!lakeMask[((lakeX * 16 + lakeZ) * 8 + lakeY)])
                            && (((lakeX < 15) && (lakeMask[(((lakeX + 1) * 16 + lakeZ) * 8 + lakeY)]))
                            || ((lakeX > 0) && (lakeMask[(((lakeX - 1) * 16 + lakeZ) * 8 + lakeY)]))
                            || ((lakeZ < 15) && (lakeMask[((lakeX * 16 + (lakeZ + 1)) * 8 + lakeY)]))
                            || ((lakeZ > 0) && (lakeMask[((lakeX * 16 + (lakeZ - 1)) * 8 + lakeY)]))
                            || ((lakeY < 7) && (lakeMask[((lakeX * 16 + lakeZ) * 8 + (lakeY + 1))]))
                            || ((lakeY > 0) && (lakeMask[((lakeX * 16 + lakeZ) * 8 + (lakeY - 1))])));

                    if (flag)
                    {
                        localMaterialData = world.getMaterial(x + lakeX, y + lakeY, z + lakeZ, chunkBeingPopulated);
                        if ((lakeY >= 4) && (localMaterialData == null || localMaterialData.isLiquid()))
                        {
                            return;
                        }
                        localMaterialData2 = world.getMaterial(x + lakeX, y + lakeY, z + lakeZ, chunkBeingPopulated);
                        if ((lakeY < 4) && (localMaterialData == null || !localMaterialData.isSolid()) && (localMaterialData2 == null || !localMaterialData2.equals(material)))
                        {
                            return;
                        }
                    }
                }
            }
        }

        for (int lakeX = 0; lakeX < 16; lakeX++)
        {
            for (int lakeZ = 0; lakeZ < 16; lakeZ++)
            {
                for (int lakeY = 0; lakeY < 8; lakeY++)
                {
                    if (lakeMask[((lakeX * 16 + lakeZ) * 8 + lakeY)])
                    {
                        world.setBlock(x + lakeX, y + lakeY, z + lakeZ, material, null, chunkBeingPopulated, false);
                        lakeMask[((lakeX * 16 + lakeZ) * 8 + lakeY)] = false;
                    }
                }
                for (int lakeY = 4; lakeY < 8; lakeY++)
                {
                    if (lakeMask[((lakeX * 16 + lakeZ) * 8 + lakeY)])
                    {
                        world.setBlock(x + lakeX, y + lakeY, z + lakeZ, air, null, chunkBeingPopulated, false);
                        lakeMask[((lakeX * 16 + lakeZ) * 8 + lakeY)] = false;
                    }
                }
            }
        }
    }
}
