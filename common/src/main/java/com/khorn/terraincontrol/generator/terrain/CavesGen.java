package com.khorn.terraincontrol.generator.terrain;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.ChunkBuffer;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.helpers.MathHelper;
import com.khorn.terraincontrol.util.helpers.RandomHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import java.util.Random;

public class CavesGen extends TerrainGenBase
{
    private WorldConfig worldSettings;

    public CavesGen(WorldConfig wrk, LocalWorld world)
    {
        super(world);
        this.worldSettings = wrk;
    }

    protected void generateLargeCaveNode(long seed, ChunkBuffer generatingChunkBuffer, double x, double y, double z)
    {
        generateCaveNode(seed, generatingChunkBuffer, x, y, z, 1.0F + this.random.nextFloat() * 6.0F, 0.0F, 0.0F, -1, -1, 0.5D);
    }

    protected void generateCaveNode(long seed, ChunkBuffer generatingChunkBuffer, double x, double y, double z, float paramFloat1,
            float paramFloat2, float paramFloat3, int angle, int maxAngle, double paramDouble4)
    {
        ChunkCoordinate generatingChunk = generatingChunkBuffer.getChunkCoordinate();
        double real_x = generatingChunk.getBlockXCenter();
        double real_z = generatingChunk.getBlockZCenter();

        float f1 = 0.0F;
        float f2 = 0.0F;

        Random localRandom = new Random(seed);

        if (maxAngle <= 0)
        {
            int checkAreaSize = this.checkAreaSize * 16 - 16;
            maxAngle = checkAreaSize - localRandom.nextInt(checkAreaSize / 4);
        }
        boolean isLargeCave = false;

        if (angle == -1)
        {
            angle = maxAngle / 2;
            isLargeCave = true;
        }

        int j = localRandom.nextInt(maxAngle / 2) + maxAngle / 4;
        int k = localRandom.nextInt(6) == 0 ? 1 : 0;

        for (; angle < maxAngle; angle++)
        {
            double d3 = 1.5D + MathHelper.sin(angle * 3.141593F / maxAngle) * paramFloat1 * 1.0F;
            double d4 = d3 * paramDouble4;

            float f3 = MathHelper.cos(paramFloat3);
            float f4 = MathHelper.sin(paramFloat3);
            x += MathHelper.cos(paramFloat2) * f3;
            y += f4;
            z += MathHelper.sin(paramFloat2) * f3;

            if (k != 0)
                paramFloat3 *= 0.92F;
            else
            {
                paramFloat3 *= 0.7F;
            }
            paramFloat3 += f2 * 0.1F;
            paramFloat2 += f1 * 0.1F;

            f2 *= 0.9F;
            f1 *= 0.75F;
            f2 += (localRandom.nextFloat() - localRandom.nextFloat()) * localRandom.nextFloat() * 2.0F;
            f1 += (localRandom.nextFloat() - localRandom.nextFloat()) * localRandom.nextFloat() * 4.0F;

            if ((!isLargeCave) && (angle == j) && (paramFloat1 > 1.0F) && (maxAngle > 0))
            {
                generateCaveNode(localRandom.nextLong(), generatingChunkBuffer, x, y, z, localRandom.nextFloat() * 0.5F + 0.5F,
                        paramFloat2 - 1.570796F, paramFloat3 / 3.0F, angle, maxAngle, 1.0D);
                generateCaveNode(localRandom.nextLong(), generatingChunkBuffer, x, y, z, localRandom.nextFloat() * 0.5F + 0.5F,
                        paramFloat2 + 1.570796F, paramFloat3 / 3.0F, angle, maxAngle, 1.0D);
                return;
            }
            if ((!isLargeCave) && (localRandom.nextInt(4) == 0))
            {
                continue;
            }

            // Check if distance to working point (x and z) too larger than working radius (maybe ??)
            double d5 = x - real_x;
            double d6 = z - real_z;
            double d7 = maxAngle - angle;
            double d8 = paramFloat1 + 2.0F + 16.0F;
            if (d5 * d5 + d6 * d6 - d7 * d7 > d8 * d8)
            {
                return;
            }

            //Boundaries check.
            if ((x < real_x - 16.0D - d3 * 2.0D) || (z < real_z - 16.0D - d3 * 2.0D) || (x > real_x + 16.0D + d3 * 2.0D) || (z > real_z + 16.0D + d3 * 2.0D))
                continue;


            int m = MathHelper.floor(x - d3) - generatingChunk.getBlockX() - 1;
            int n = MathHelper.floor(x + d3) - generatingChunk.getBlockX() + 1;

            int i1 = MathHelper.floor(y - d4) - 1;
            int i2 = MathHelper.floor(y + d4) + 1;

            int i3 = MathHelper.floor(z - d3) - generatingChunk.getBlockZ() - 1;
            int i4 = MathHelper.floor(z + d3) - generatingChunk.getBlockZ() + 1;

            if (m < 0)
                m = 0;
            if (n > 16)
                n = 16;

            if (i1 < 1)
                i1 = 1;
            if (i2 > this.worldSettings.worldHeightCap - 8)
            {
                i2 = this.worldSettings.worldHeightCap - 8;
            }
            if (i3 < 0)
                i3 = 0;
            if (i4 > 16)
                i4 = 16;

            // Search for water
            boolean waterFound = false;
            for (int local_x = m; (!waterFound) && (local_x < n); local_x++)
            {
                for (int local_z = i3; (!waterFound) && (local_z < i4); local_z++)
                {
                    for (int local_y = i2 + 1; (!waterFound) && (local_y >= i1 - 1); local_y--)
                    {
                        if (local_y >= 0 && local_y < this.worldSettings.worldHeightCap)
                        {
                            LocalMaterialData material = generatingChunkBuffer.getBlock(local_x, local_y, local_z);
                            if (material.isMaterial(DefaultMaterial.WATER)
                                    || material.isMaterial(DefaultMaterial.STATIONARY_WATER))
                            {
                                waterFound = true;
                            }
                            if ((local_y != i1 - 1) && (local_x != m) && (local_x != n - 1) && (local_z != i3) && (local_z != i4 - 1))
                                local_y = i1;
                        }
                    }
                }
            }
            if (waterFound)
                continue;

            // Generate cave
            for (int local_x = m; local_x < n; local_x++)
            {
                double d9 = (local_x + generatingChunk.getBlockX() + 0.5D - x) / d3;
                for (int local_z = i3; local_z < i4; local_z++)
                {
                    LocalBiome biome = this.world.getBiome(local_x + generatingChunk.getBlockX(), local_z + generatingChunk.getBlockZ());
                    double d10 = (local_z + generatingChunk.getBlockZ() + 0.5D - z) / d3;

                    boolean grassFound = false;
                    if (d9 * d9 + d10 * d10 < 1.0D)
                    {
                        for (int local_y = i2; local_y > i1; local_y--)
                        {
                            double d11 = ((local_y - 1) + 0.5D - y) / d4;
                            if ((d11 > -0.7D) && (d9 * d9 + d11 * d11 + d10 * d10 < 1.0D))
                            {
                                LocalMaterialData material = generatingChunkBuffer.getBlock(local_x, local_y, local_z);
                                LocalMaterialData materialAbove = generatingChunkBuffer.getBlock(local_x, local_y + 1, local_z);
                                if (material.isMaterial(DefaultMaterial.GRASS) || material.isMaterial(DefaultMaterial.MYCEL))
                                    grassFound = true;
                                if (this.isSuitableBlock(material, materialAbove, biome))
                                {
                                    if (local_y - 1 < 10)
                                    {
                                        generatingChunkBuffer.setBlock(local_x, local_y, local_z, lava);
                                    } else
                                    {
                                        generatingChunkBuffer.setBlock(local_x, local_y, local_z, air);

                                        // If grass was just deleted, try to
                                        // move it down
                                        if (grassFound
                                                && (generatingChunkBuffer.getBlock(local_x, local_y - 1, local_z)
                                                        .isMaterial(DefaultMaterial.DIRT)))
                                        {
                                            generatingChunkBuffer.setBlock(local_x, local_y - 1, local_z,
                                                    biome.getBiomeConfig().surfaceBlock);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (isLargeCave)
                break;
        }
    }

    protected boolean isSuitableBlock(LocalMaterialData material, LocalMaterialData materialAbove, LocalBiome biome)
    {
        BiomeConfig biomeConfig = biome.getBiomeConfig();
        if (material.equals(biomeConfig.stoneBlock))
        {
            return true;
        }
        if (material.canFall())
        {
            return !materialAbove.isLiquid();
        }
        if (material.equals(biomeConfig.groundBlock))
        {
            return true;
        }
        if (material.equals(biomeConfig.surfaceBlock))
        {
            return true;
        }

        // Few hardcoded cases
        if (material.isMaterial(DefaultMaterial.HARD_CLAY))
        {
            return true;
        }
        if (material.isMaterial(DefaultMaterial.SANDSTONE))
        {
            return true;
        }
        // TODO: add red sandstone case in Minecraft 1.8
        if (material.isMaterial(DefaultMaterial.SNOW))
        {
            return true;
        }

        return false;
    }

    @Override
    protected void generateChunk(ChunkCoordinate chunkCoord, ChunkBuffer generatingChunkBuffer)
    {
        int i = this.random.nextInt(this.random.nextInt(this.random.nextInt(this.worldSettings.caveFrequency) + 1) + 1);
        if (this.worldSettings.evenCaveDistribution)
            i = this.worldSettings.caveFrequency;
        if (this.random.nextInt(100) >= this.worldSettings.caveRarity)
            i = 0;

        for (int j = 0; j < i; j++)
        {
            double x = chunkCoord.getBlockX() + this.random.nextInt(ChunkCoordinate.CHUNK_X_SIZE);

            double y;

            if (this.worldSettings.evenCaveDistribution)
                y = RandomHelper.numberInRange(random, this.worldSettings.caveMinAltitude, this.worldSettings.caveMaxAltitude);
            else
                y = this.random.nextInt(this.random.nextInt(this.worldSettings.caveMaxAltitude - this.worldSettings.caveMinAltitude + 1) + 1) + this.worldSettings.caveMinAltitude;

            double z = chunkCoord.getBlockZ() + this.random.nextInt(ChunkCoordinate.CHUNK_Z_SIZE);

            int count = this.worldSettings.caveSystemFrequency;
            boolean largeCaveSpawned = false;
            if (this.random.nextInt(100) <= this.worldSettings.individualCaveRarity)
            {
                generateLargeCaveNode(this.random.nextLong(), generatingChunkBuffer, x, y, z);
                largeCaveSpawned = true;
            }

            if ((largeCaveSpawned) || (this.random.nextInt(100) <= this.worldSettings.caveSystemPocketChance - 1))
            {
                count += RandomHelper.numberInRange(random, this.worldSettings.caveSystemPocketMinSize, this.worldSettings.caveSystemPocketMaxSize);
            }
            while (count > 0)
            {
                count--;
                float f1 = this.random.nextFloat() * 3.141593F * 2.0F;
                float f2 = (this.random.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float f3 = this.random.nextFloat() * 2.0F + this.random.nextFloat();

                generateCaveNode(this.random.nextLong(), generatingChunkBuffer, x, y, z, f3, f1, f2, 0, 0, 1.0D);
            }
        }
    }

}
