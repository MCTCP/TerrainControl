package com.pg85.otg.generator.terrain;

import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.generator.ChunkBuffer;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.materials.MaterialHelper;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

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

    private void placeBlocks(long paramLong, ChunkBuffer generatingChunkBuffer, double paramDouble1, double paramDouble2, double paramDouble3, float paramFloat1, float paramFloat2, float paramFloat3, int size, double paramDouble4)
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

        double d3;
        double d4;
        float f4;
        float f5;
        double d5;
        double d6;
        double d7;
        double d8;
        double d9;
        int k;
        int m;
        int maxDepth;
        int minDepth;
        int i2;
        int i3;
        int i4;
        LocalMaterialData materialAtPosition;
        
        LocalBiome biome;
        BiomeConfig biomeConfig;
        LocalMaterialData surfaceblockDefaultMaterial;
        double d10;
        boolean surfaceBlockFound;
        LocalMaterialData surfaceBlockMaterial;
        
    	LocalMaterialData material;
        LocalMaterialData block;
        
        for (int stepCount = 0; stepCount < size; stepCount++)
        {
            d3 = 1.5D + MathHelper.sin(stepCount * 3.141593F / size) * paramFloat1 * 1.0F;
            d4 = d3 * paramDouble4;

            d3 *= (localRandom.nextFloat() * 0.25D + 0.75D);
            d4 *= (localRandom.nextFloat() * 0.25D + 0.75D);

            f4 = MathHelper.cos(paramFloat3);
            f5 = MathHelper.sin(paramFloat3);
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
            d5 = paramDouble1 - d1;
            d6 = paramDouble3 - d2;
            d7 = size - stepCount;
            d8 = paramFloat1 + 2.0F + 16.0F;
            if (d5 * d5 + d6 * d6 - d7 * d7 > d8 * d8)
            {
                return;
            }

            if ((paramDouble1 < d1 - 16.0D - d3 * 2.0D) || (paramDouble3 < d2 - 16.0D - d3 * 2.0D) || (paramDouble1 > d1 + 16.0D + d3 * 2.0D) || (paramDouble3 > d2 + 16.0D + d3 * 2.0D))
            {
                continue;
            }
            k = MathHelper.floor(paramDouble1 - d3) - generatingChunk.getBlockX() - 1;
            m = MathHelper.floor(paramDouble1 + d3) - generatingChunk.getBlockX() + 1;

            maxDepth = MathHelper.floor(paramDouble2 - d4) - 1;
            minDepth = MathHelper.floor(paramDouble2 + d4) + 1;

            i2 = MathHelper.floor(paramDouble3 - d3) - generatingChunk.getBlockZ() - 1;
            i3 = MathHelper.floor(paramDouble3 + d3) - generatingChunk.getBlockZ() + 1;

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

            i4 = 0;
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
                            materialAtPosition = generatingChunkBuffer.getBlock(localX, localY, localZ);
                            if (
                        		materialAtPosition.isMaterial(DefaultMaterial.WATER) || 
                        		materialAtPosition.isMaterial(DefaultMaterial.STATIONARY_WATER)
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
                d9 = (localX + generatingChunk.getBlockX() + 0.5D - paramDouble1) / d3;
                for (int localZ = i2; localZ < i3; localZ++)
                {
                    biome = world.getBiome(localZ + generatingChunk.getBlockX(), localX + generatingChunk.getBlockZ());
                    biomeConfig = biome.getBiomeConfig();
                    surfaceblockDefaultMaterial = biomeConfig.surfaceBlock;
                    d10 = (localZ + generatingChunk.getBlockZ() + 0.5D - paramDouble3) / d3;
                    surfaceBlockFound = false;
                    surfaceBlockMaterial = null;
                    if (d9 * d9 + d10 * d10 < 1.0D)
                    {
                    	// If surfaceBlock is found then replace the bottom block with surfaceblock
                        for (int currentDepth = minDepth; currentDepth >= maxDepth; currentDepth--)
                        {
                            double d11 = ((currentDepth - 1) + 0.5D - paramDouble2) / d4;
                            if ((d9 * d9 + d10 * d10) * this.a[currentDepth - 1] + d11 * d11 / 6.0D < 1.0D)
                            {
                            	material = generatingChunkBuffer.getBlock(localX, currentDepth, localZ);
                                
                                if (!surfaceBlockFound && material.equals(surfaceblockDefaultMaterial))
                                {
                                	surfaceBlockFound = true;
                                	surfaceBlockMaterial = material;
                                }
                                
                                if (
                            		!material.isMaterial(DefaultMaterial.BEDROCK) &&
                    				!material.isAir()
                        		)
                                {                               	
                                    generatingChunkBuffer.setBlock(localX, currentDepth, localZ, MaterialHelper.AIR);
                                }
                                
                                block = generatingChunkBuffer.getBlock(localX, currentDepth - 1, localZ);
                                if (
                            		surfaceBlockFound &&
                            		(
	                                	!block.isMaterial(DefaultMaterial.BEDROCK) &&
										!block.isMaterial(DefaultMaterial.WATER) &&
										!block.isMaterial(DefaultMaterial.STATIONARY_WATER) &&
										!block.isMaterial(DefaultMaterial.LAVA) &&
										!block.isMaterial(DefaultMaterial.STATIONARY_LAVA) &&
										!block.isMaterial(DefaultMaterial.MAGMA) &&
										!block.isAir()
                    				)
                                )
                                {
                                    generatingChunkBuffer.setBlock(localX, currentDepth - 1, localZ, surfaceBlockMaterial);
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