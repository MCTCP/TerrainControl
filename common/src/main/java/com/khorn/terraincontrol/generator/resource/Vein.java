package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.MaterialSet;
import com.khorn.terraincontrol.util.helpers.MathHelper;

import java.util.Random;

/**
 * Represents a single ore vein.
 *
 */
class Vein
{
    private int x, y, z, size;

    Vein(int blockX, int blockY, int blockZ, int size)
    {
        this.x = blockX;
        this.y = blockY;
        this.z = blockZ;
        this.size = size;
    }

    public int getChunkSize()
    {
        return (size + 15) / 16;
    }

    public boolean reachesChunk(int otherChunkX, int otherChunkZ)
    {
        // Calculate the nearest chunk x and z
        int chunkX = (x + 8) / 16;
        int chunkZ = (z + 8) / 16;
        // Calculate the ceiled chunk size
        int chunkSize = getChunkSize();

        if (MathHelper.abs(otherChunkX - chunkX) > chunkSize || MathHelper.abs(otherChunkZ - chunkZ) > chunkSize)
        {
            return false;
        }

        return true;
    }

    public void spawn(LocalWorld world, Random random, ChunkCoordinate chunkCoord, VeinGen gen)
    {
        int sizeSquared = size * size;

        for (int i = 0; i < gen.oreFrequency; i++)
        {
            if (random.nextInt(100) < gen.oreRarity)
            {
                int oreX = chunkCoord.getBlockXCenter() + random.nextInt(ChunkCoordinate.CHUNK_X_SIZE);
                int oreY = MathHelper.getRandomNumberInRange(random, gen.minAltitude, gen.maxAltitude);
                int oreZ = chunkCoord.getBlockZCenter() + random.nextInt(ChunkCoordinate.CHUNK_Z_SIZE);

                if ((oreX - x) * (oreX - x) + (oreY - y) * (oreY - y) + (oreZ - z) * (oreZ - z) < sizeSquared)
                {
                    spawnOre(world, random, oreX, oreY, oreZ, gen);
                }
            }
        }
    }

    protected void spawnOre(LocalWorld world, Random rand, int x, int y, int z, VeinGen gen)
    {
        int maxSize = gen.oreSize;
        LocalMaterialData material = gen.material;
        MaterialSet sourceBlocks = gen.sourceBlocks;

        float f = rand.nextFloat() * 3.141593F;

        double d1 = x + 8 + MathHelper.sin(f) * maxSize / 8.0F;
        double d2 = x + 8 - MathHelper.sin(f) * maxSize / 8.0F;
        double d3 = z + 8 + MathHelper.cos(f) * maxSize / 8.0F;
        double d4 = z + 8 - MathHelper.cos(f) * maxSize / 8.0F;

        double d5 = y + rand.nextInt(3) - 2;
        double d6 = y + rand.nextInt(3) - 2;

        for (int i = 0; i <= maxSize; i++)
        {
            double d7 = d1 + (d2 - d1) * i / maxSize;
            double d8 = d5 + (d6 - d5) * i / maxSize;
            double d9 = d3 + (d4 - d3) * i / maxSize;

            double d10 = rand.nextDouble() * maxSize / 16.0D;
            double d11 = (MathHelper.sin(i * 3.141593F / maxSize) + 1.0F) * d10 + 1.0D;
            double d12 = (MathHelper.sin(i * 3.141593F / maxSize) + 1.0F) * d10 + 1.0D;

            int j = MathHelper.floor(d7 - d11 / 2.0D);
            int k = MathHelper.floor(d8 - d12 / 2.0D);
            int m = MathHelper.floor(d9 - d11 / 2.0D);

            int n = MathHelper.floor(d7 + d11 / 2.0D);
            int i1 = MathHelper.floor(d8 + d12 / 2.0D);
            int i2 = MathHelper.floor(d9 + d11 / 2.0D);

            for (int i3 = j; i3 <= n; i3++)
            {
                double d13 = (i3 + 0.5D - d7) / (d11 / 2.0D);
                if (d13 * d13 < 1.0D)
                {
                    for (int i4 = k; i4 <= i1; i4++)
                    {
                        double d14 = (i4 + 0.5D - d8) / (d12 / 2.0D);
                        if (d13 * d13 + d14 * d14 < 1.0D)
                        {
                            for (int i5 = m; i5 <= i2; i5++)
                            {
                                double d15 = (i5 + 0.5D - d9) / (d11 / 2.0D);
                                if ((d13 * d13 + d14 * d14 + d15 * d15 < 1.0D) && sourceBlocks.contains(world.getMaterial(i3, i4, i5)))
                                {
                                    world.setBlock(i3, i4, i5, material);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}