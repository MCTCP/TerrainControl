package com.khorn.terraincontrol.generator.terrain;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.ChunkBuffer;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.helpers.MathHelper;
import com.khorn.terraincontrol.util.helpers.RandomHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import java.util.Random;

public class RavinesGen extends TerrainGenBase
{
    private float[] a = new float[1024];
    private WorldConfig worldSettings;

    public RavinesGen(WorldConfig wrk, LocalWorld world)
    {
        super(world);
        this.worldSettings = wrk;
    }

    protected void placeBlocks(long paramLong, ChunkBuffer generatingChunkBuffer, double paramDouble1, double paramDouble2, double paramDouble3, float paramFloat1, float paramFloat2, float paramFloat3, int size, double paramDouble4)
    {
        Random localRandom = new Random(paramLong);

        ChunkCoordinate generatingChunk = generatingChunkBuffer.getChunkCoordinate();
        double d1 = generatingChunk.getBlockXCenter();
        double d2 = generatingChunk.getBlockZCenter();

        float f1 = 0.0F;
        float f2 = 0.0F;

        int i = 0;

        float f3 = 1.0F;
        for (int j = 0; ; j++)
        {
            if (j >= worldSettings.worldHeightCap)
            {
                break;
            }
            if ((j == 0) || (localRandom.nextInt(3) == 0))
            {
                f3 = 1.0F + localRandom.nextFloat() * localRandom.nextFloat() * 1.0F;
            }
            this.a[j] = (f3 * f3);
        }

        for (int stepCount = 0; stepCount < size; stepCount++)
        {
            double d3 = 1.5D + MathHelper.sin(stepCount * 3.141593F / size) * paramFloat1 * 1.0F;
            double d4 = d3 * paramDouble4;

            d3 *= (localRandom.nextFloat() * 0.25D + 0.75D);
            d4 *= (localRandom.nextFloat() * 0.25D + 0.75D);

            float f4 = MathHelper.cos(paramFloat3);
            float f5 = MathHelper.sin(paramFloat3);
            paramDouble1 += MathHelper.cos(paramFloat2) * f4;
            paramDouble2 += f5;
            paramDouble3 += MathHelper.sin(paramFloat2) * f4;

            paramFloat3 *= 0.7F;

            paramFloat3 += f2 * 0.05F;
            paramFloat2 += f1 * 0.05F;

            f2 *= 0.8F;
            f1 *= 0.5F;
            f2 += (localRandom.nextFloat() - localRandom.nextFloat()) * localRandom.nextFloat() * 2.0F;
            f1 += (localRandom.nextFloat() - localRandom.nextFloat()) * localRandom.nextFloat() * 4.0F;

            if ((i == 0) && (localRandom.nextInt(4) == 0))
            {
                continue;
            }
            double d5 = paramDouble1 - d1;
            double d6 = paramDouble3 - d2;
            double d7 = size - stepCount;
            double d8 = paramFloat1 + 2.0F + 16.0F;
            if (d5 * d5 + d6 * d6 - d7 * d7 > d8 * d8)
            {
                return;
            }

            if ((paramDouble1 < d1 - 16.0D - d3 * 2.0D) || (paramDouble3 < d2 - 16.0D - d3 * 2.0D) || (paramDouble1 > d1 + 16.0D + d3 * 2.0D) || (paramDouble3 > d2 + 16.0D + d3 * 2.0D))
            {
                continue;
            }
            int k = MathHelper.floor(paramDouble1 - d3) - generatingChunk.getBlockX() - 1;
            int m = MathHelper.floor(paramDouble1 + d3) - generatingChunk.getBlockX() + 1;

            int maxDepth = MathHelper.floor(paramDouble2 - d4) - 1;
            int minDepth = MathHelper.floor(paramDouble2 + d4) + 1;

            int i2 = MathHelper.floor(paramDouble3 - d3) - generatingChunk.getBlockZ() - 1;
            int i3 = MathHelper.floor(paramDouble3 + d3) - generatingChunk.getBlockZ() + 1;

            if (k < 0)
            {
                k = 0;
            }
            if (m > 16)
            {
                m = 16;
            }
            if (maxDepth < 1)
            {
                maxDepth = 1;
            }
            if (minDepth > worldSettings.worldHeightCap - 8)
            {
                minDepth = worldSettings.worldHeightCap - 8;
            }

            if (i2 < 0)
            {
                i2 = 0;
            }
            if (i3 > 16)
            {
                i3 = 16;
            }

            int i4 = 0;
            for (int localX = k; (i4 == 0) && (localX < m); localX++)
            {
                for (int localZ = i2; (i4 == 0) && (localZ < i3); localZ++)
                {
                    for (int localY = minDepth + 1; (i4 == 0) && (localY >= maxDepth - 1); localY--)
                    {
                        if (localY < 0)
                        {
                            continue;
                        }
                        if (localY < worldSettings.worldHeightCap)
                        {
                            DefaultMaterial materialAtPosition = generatingChunkBuffer.getBlock(localX, localY, localZ).toDefaultMaterial();
                            if (
                        		materialAtPosition == DefaultMaterial.WATER || 
                        		materialAtPosition == DefaultMaterial.STATIONARY_WATER
                    		)
                            {
                                i4 = 1;
                            }
                            if ((localY != maxDepth - 1) && (localX != k) && (localX != m - 1) && (localZ != i2) && (localZ != i3 - 1))
                            {
                                localY = maxDepth;
                            }
                        }
                    }
                }
            }
            if (i4 != 0)
            {
                continue;
            }
            for (int localX = k; localX < m; localX++)
            {
                double d9 = (localX + generatingChunk.getBlockX() + 0.5D - paramDouble1) / d3;
                for (int localZ = i2; localZ < i3; localZ++)
                {
                    LocalBiome biome = world.getBiome(localZ + generatingChunk.getBlockX(), localX + generatingChunk.getBlockZ());
                    BiomeConfig biomeConfig = biome.getBiomeConfig();
                    double d10 = (localZ + generatingChunk.getBlockZ() + 0.5D - paramDouble3) / d3;
                    boolean surfaceBlockFound = false;
                    if (d9 * d9 + d10 * d10 < 1.0D)
                    {
                    	// If surfaceBlock is found then replace the bottom block with surfaceblock
                        for (int currentDepth = minDepth; currentDepth >= maxDepth; currentDepth--)
                        {
                            double d11 = ((currentDepth - 1) + 0.5D - paramDouble2) / d4;
                            if ((d9 * d9 + d10 * d10) * this.a[currentDepth - 1] + d11 * d11 / 6.0D < 1.0D)
                            {
                                DefaultMaterial material = generatingChunkBuffer.getBlock(localX, currentDepth, localZ).toDefaultMaterial();
                                
                                if (!surfaceBlockFound && material == biomeConfig.surfaceBlock.toDefaultMaterial())
                                {
                                	surfaceBlockFound = true;
                                }
                                
                                
                                if (
                                	material != DefaultMaterial.BEDROCK &&
                                	material != DefaultMaterial.AIR
                        		)
                                {                               	
                                    generatingChunkBuffer.setBlock(localX, currentDepth, localZ, air);
                                }
                                
                                DefaultMaterial block = generatingChunkBuffer.getBlock(localX, currentDepth - 1, localZ).toDefaultMaterial();
                                if (
                            		surfaceBlockFound &&
                            		(
	                                	block != DefaultMaterial.BEDROCK &&
										block != DefaultMaterial.WATER &&
										block != DefaultMaterial.STATIONARY_WATER &&
										block != DefaultMaterial.LAVA &&
										block != DefaultMaterial.STATIONARY_LAVA &&
										block != DefaultMaterial.MAGMA &&
										block != DefaultMaterial.AIR
                    				)
                                )
                                {
                                    generatingChunkBuffer.setBlock(localX, currentDepth - 1, localZ, biomeConfig.surfaceBlock);
                                }
                            }
                        }
                    }
                }
            }
            if (i != 0)
            {
                break;
            }
        }
    }

    @Override
    protected void generateChunk(ChunkCoordinate currentChunk, ChunkBuffer generatingChunkBuffer)
    {
        if (this.random.nextInt(100) >= this.worldSettings.ravineRarity)
        {
            return;
        }
        double d1 = currentChunk.getBlockX() + this.random.nextInt(ChunkCoordinate.CHUNK_X_SIZE);
        double d2 = RandomHelper.numberInRange(random, this.worldSettings.ravineMinAltitude, this.worldSettings.ravineMaxAltitude);
        double d3 = currentChunk.getBlockZ() + this.random.nextInt(ChunkCoordinate.CHUNK_Z_SIZE);

        int i = 1;

        for (int j = 0; j < i; j++)
        {
            float f1 = this.random.nextFloat() * 3.141593F * 2.0F;
            float f2 = (this.random.nextFloat() - 0.5F) * 2.0F / 8.0F;
            float f3 = (this.random.nextFloat() * 2.0F + this.random.nextFloat()) * 2.0F;

            int size = RandomHelper.numberInRange(random, this.worldSettings.ravineMinLength, this.worldSettings.ravineMaxLength);

            placeBlocks(this.random.nextLong(), generatingChunkBuffer, d1, d2, d3, f3, f1, f2, size, this.worldSettings.ravineDepth);
        }
    }
}