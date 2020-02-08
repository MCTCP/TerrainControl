package com.pg85.otg.generator.terrain;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.generator.ChunkBuffer;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import java.util.Random;

public class CavesGen extends TerrainGenBase
{
    private WorldConfig worldSettings;

    public CavesGen(WorldConfig wrk, LocalWorld world)
    {
        super(world);
        this.worldSettings = wrk;
    }

    private void generateLargeCaveNode(long seed, ChunkBuffer generatingChunkBuffer, double x, double y, double z)
    {
        generateCaveNode(seed, generatingChunkBuffer, x, y, z, 1.0F + this.random.nextFloat() * 6.0F, 0.0F, 0.0F, -1, -1, 0.5D);
    }

    private void generateCaveNode(long seed, ChunkBuffer generatingChunkBuffer, double x, double y, double z, float paramFloat1, float paramFloat2, float paramFloat3, int angle, int maxAngle, double paramDouble4)
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

        double d3;
        double d4;
        float f3;
        float f4;
        double d5;
        double d6;
        double d7;
        double d8;
        
        int m;
        int n;
        
        int maxDepth;
        int minDepth;
        int i3;
        int i4;
        
        boolean waterFound;
        LocalMaterialData material;
        
        double d9;
        //LocalBiome biome;
        double d10;

        boolean surfaceBlockFound;
        double d11;
        
        LocalMaterialData materialAbove;
        LocalMaterialData block;
        
    	int surfaceBlockDepth;
    	LocalMaterialData surfaceBlockMaterial;
    	
        for (; angle < maxAngle; angle++)
        {
            d3 = 1.5D + MathHelper.sin(angle * 3.141593F / maxAngle) * paramFloat1 * 1.0F;
            d4 = d3 * paramDouble4;

            f3 = MathHelper.cos(paramFloat3);
            f4 = MathHelper.sin(paramFloat3);
            x += MathHelper.cos(paramFloat2) * f3;
            y += f4;
            z += MathHelper.sin(paramFloat2) * f3;

            if (k != 0)
            {
                paramFloat3 *= 0.92F;
            } else {
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
                generateCaveNode(localRandom.nextLong(), generatingChunkBuffer, x, y, z, localRandom.nextFloat() * 0.5F + 0.5F, paramFloat2 - 1.570796F, paramFloat3 / 3.0F, angle, maxAngle, 1.0D);
                generateCaveNode(localRandom.nextLong(), generatingChunkBuffer, x, y, z, localRandom.nextFloat() * 0.5F + 0.5F, paramFloat2 + 1.570796F, paramFloat3 / 3.0F, angle, maxAngle, 1.0D);
                return;
            }
            if ((!isLargeCave) && (localRandom.nextInt(4) == 0))
            {
                continue;
            }

            // Check if distance to working point (x and z) too larger than working radius (maybe ??)
            d5 = x - real_x;
            d6 = z - real_z;
            d7 = maxAngle - angle;
            d8 = paramFloat1 + 2.0F + 16.0F;
            if (d5 * d5 + d6 * d6 - d7 * d7 > d8 * d8)
            {
                return;
            }

            //Boundaries check.
            if ((x < real_x - 16.0D - d3 * 2.0D) || (z < real_z - 16.0D - d3 * 2.0D) || (x > real_x + 16.0D + d3 * 2.0D) || (z > real_z + 16.0D + d3 * 2.0D))
            {
                continue;
            }


            m = MathHelper.floor(x - d3) - generatingChunk.getBlockX() - 1;
            n = MathHelper.floor(x + d3) - generatingChunk.getBlockX() + 1;

            maxDepth = MathHelper.floor(y - d4) - 1;
            minDepth = MathHelper.floor(y + d4) + 1;

            i3 = MathHelper.floor(z - d3) - generatingChunk.getBlockZ() - 1;
            i4 = MathHelper.floor(z + d3) - generatingChunk.getBlockZ() + 1;

            if (m < 0)
            {
                m = 0;
            }
            if (n > 16)
            {
                n = 16;
            }

            if (maxDepth < 1)
            {
                maxDepth = 1;
            }
            if (minDepth > this.worldSettings.worldHeightCap - 8)
            {
                minDepth = this.worldSettings.worldHeightCap - 8;
            }
            if (i3 < 0)
            {
                i3 = 0;
            }
            if (i4 > 16)
            {
                i4 = 16;
            }

            // Search for water
            waterFound = false;
            for (int local_x = m; (!waterFound) && (local_x < n); local_x++)
            {
                for (int local_z = i3; (!waterFound) && (local_z < i4); local_z++)
                {
                    for (int local_y = minDepth + 1; (!waterFound) && (local_y >= maxDepth - 1); local_y--)
                    {
                        if (local_y >= 0 && local_y < this.worldSettings.worldHeightCap)
                        {
                            material = generatingChunkBuffer.getBlock(local_x, local_y, local_z);
                            if (
                        		material.isMaterial(DefaultMaterial.WATER) ||
                        		material.isMaterial(DefaultMaterial.STATIONARY_WATER)
                    		)
                            {
                                waterFound = true;
                            }
                            if ((local_y != maxDepth - 1) && (local_x != m) && (local_x != n - 1) && (local_z != i3) && (local_z != i4 - 1))
                            {
                                local_y = maxDepth;
                            }
                        }
                    }
                }
            }
            if (waterFound)
            {
                continue;
            }

            // Generate cave
            for (int local_x = m; local_x < n; local_x++)
            {
                d9 = (local_x + generatingChunk.getBlockX() + 0.5D - x) / d3;
                for (int local_z = i3; local_z < i4; local_z++)
                {
                    //biome = this.world.getBiome(local_x + generatingChunk.getBlockX(), local_z + generatingChunk.getBlockZ());
                    d10 = (local_z + generatingChunk.getBlockZ() + 0.5D - z) / d3;

                    surfaceBlockFound = false;
                    surfaceBlockMaterial = null;
                    if (d9 * d9 + d10 * d10 < 1.0D)
                    {
                    	surfaceBlockDepth = 0;
                    	for (int currentDepth = minDepth; currentDepth > maxDepth; currentDepth--)
                    	{
                    		material = generatingChunkBuffer.getBlock(local_x, currentDepth, local_z);
                    		materialAbove = generatingChunkBuffer.getBlock(local_x, currentDepth + 1, local_z);
                    		if(
                				(
            						materialAbove.isAir() || 
            						materialAbove.isLiquid()
        						) && !(
    								material.isAir() || 
    								material.isLiquid()
								)
            				)
                    		{
                    			surfaceBlockDepth = currentDepth;
                    			break;
                    		}
                    	}
                    	
                        for (int currentDepth = minDepth; currentDepth > maxDepth; currentDepth--)
                        {
                            d11 = ((currentDepth - 1) + 0.5D - y) / d4;
                            if ((d11 > -0.7D) && (d9 * d9 + d11 * d11 + d10 * d10 < 1.0D))
                            {
                                material = generatingChunkBuffer.getBlock(local_x, currentDepth, local_z);
                                materialAbove = generatingChunkBuffer.getBlock(local_x, currentDepth + 1, local_z);
                                //if (!surfaceBlockFound && material.isMaterial(biome.getBiomeConfig().surfaceBlock.toDefaultMaterial()))
                                if(!surfaceBlockFound && currentDepth == surfaceBlockDepth)
                                {
                                	surfaceBlockFound = true;
                                	surfaceBlockMaterial = material;
                                }
                                if (this.isSuitableBlock(material, materialAbove))//, biome.getBiomeConfig()))
                                {
                                    generatingChunkBuffer.setBlock(local_x, currentDepth, local_z, air);
                                    block = generatingChunkBuffer.getBlock(local_x, currentDepth - 1, local_z);

                                    // If a surface block was just deleted, try to move it down
                                    if (
                                		surfaceBlockFound &&
                                		!block.isLiquid() &&
                                		!block.isMaterial(DefaultMaterial.BEDROCK)
                            		)
                                    {
                                        generatingChunkBuffer.setBlock(local_x, currentDepth - 1, local_z, surfaceBlockMaterial);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (isLargeCave)
            {
                break;
            }
        }
    }

    private boolean isSuitableBlock(LocalMaterialData material, LocalMaterialData materialAbove)//, BiomeConfig biomeConfig)
    {
    	/*
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
        */

        if (material.isSolid())
        {
            return true;
        }
        if (material.canFall())
        {
            return !materialAbove.isLiquid();
        }

    	/*
        // Few hardcoded cases
        if (material.isMaterial(DefaultMaterial.HARD_CLAY))
        {
            return true;
        }
        if (material.isMaterial(DefaultMaterial.SANDSTONE))
        {
            return true;
        }
        if (material.isMaterial(DefaultMaterial.RED_SANDSTONE))
        {
            return true;
        }
        */

        if (material.isMaterial(DefaultMaterial.SNOW))
        {
            return true;
        }

        return false;
    }

    @Override
    protected void generateChunk(ChunkCoordinate chunkCoord, ChunkBuffer generatingChunkBuffer)
    {
    	int i = 0;
    	if(this.worldSettings.caveRarity > 0 && this.worldSettings.caveFrequency > 0)
    	{
	        i = this.random.nextInt(this.random.nextInt(this.random.nextInt(this.worldSettings.caveFrequency) + 1) + 1);
	        if (this.worldSettings.evenCaveDistribution)
	        {
	            i = this.worldSettings.caveFrequency;
	        }
	        if (this.random.nextInt(100) >= this.worldSettings.caveRarity)
	        {
	            i = 0;
	        }
    	}

    	double x;
    	double y;
    	double z;
    	int count;
    	boolean largeCaveSpawned;
    	float f1;
    	float f2;
    	float f3;
        for (int j = 0; j < i; j++)
        {
            x = chunkCoord.getBlockX() + this.random.nextInt(ChunkCoordinate.CHUNK_X_SIZE);

            if (this.worldSettings.evenCaveDistribution)
            {
                y = RandomHelper.numberInRange(random, this.worldSettings.caveMinAltitude, this.worldSettings.caveMaxAltitude);
            } else {
                y = this.random.nextInt(this.random.nextInt(this.worldSettings.caveMaxAltitude - this.worldSettings.caveMinAltitude + 1) + 1) + this.worldSettings.caveMinAltitude;
            }

            z = chunkCoord.getBlockZ() + this.random.nextInt(ChunkCoordinate.CHUNK_Z_SIZE);

            count = this.worldSettings.caveSystemFrequency;
            largeCaveSpawned = false;
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
                f1 = this.random.nextFloat() * 3.141593F * 2.0F;
                f2 = (this.random.nextFloat() - 0.5F) * 2.0F / 8.0F;
                f3 = this.random.nextFloat() * 2.0F + this.random.nextFloat();

                generateCaveNode(this.random.nextLong(), generatingChunkBuffer, x, y, z, f3, f1, f2, 0, 0, 1.0D);
            }
        }
    }

}
