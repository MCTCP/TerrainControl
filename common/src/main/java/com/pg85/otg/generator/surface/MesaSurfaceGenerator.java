package com.pg85.otg.generator.surface;

import java.util.Arrays;
import java.util.Random;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.generator.ChunkBuffer;
import com.pg85.otg.generator.GeneratingChunk;
import com.pg85.otg.generator.noise.NoiseGeneratorPerlinMesaBlocks;
import com.pg85.otg.util.materials.MaterialHelper;

public class MesaSurfaceGenerator implements SurfaceGenerator
{
    public static final String NAME_NORMAL = "Mesa";
    public static final String NAME_FOREST = "MesaForest";
    public static final String NAME_BRYCE = "MesaBryce";

    private LocalMaterialData[] clayBands;
    private long worldSeed;
    private boolean hasForest;
    private boolean brycePillars;
    private NoiseGeneratorPerlinMesaBlocks pillarNoise;
    private NoiseGeneratorPerlinMesaBlocks pillarRoofNoise;
    private NoiseGeneratorPerlinMesaBlocks clayBandsOffsetNoise;

    private MesaSurfaceGenerator(boolean mountainMesa, boolean forestMesa)
    {
        this.brycePillars = mountainMesa;
        this.hasForest = forestMesa;
    }

    /**
     * Returns the mesa surface generator representing the setting value. If
     * no mes surface generator represents this setting value, null is
     * returned.
     * 
     * @param settingValue The setting value.
     * @return The mesa surface generator, or null if not found.
     */
    public static MesaSurfaceGenerator getFor(String settingValue)
    {
        if (NAME_NORMAL.equalsIgnoreCase(settingValue))
        {
            return new MesaSurfaceGenerator(false, false);
        }
        if (NAME_FOREST.equalsIgnoreCase(settingValue))
        {
            return new MesaSurfaceGenerator(false, true);
        }
        if (NAME_BRYCE.equalsIgnoreCase(settingValue))
        {
            return new MesaSurfaceGenerator(true, false);
        }
        return null;
    }

    private int lastX = Integer.MAX_VALUE;
    private int lastZ = Integer.MAX_VALUE;
    private int lastNoise = 0;
    private LocalMaterialData getBand(int xInWorld, int yInWorld, int zInWorld)
    {
    	int noise = this.lastNoise;
    	if(this.lastX != xInWorld || this.lastZ != zInWorld)
    	{
    		noise = (int) Math.round(this.clayBandsOffsetNoise.getValue((double) xInWorld / 512.0D, (double) zInWorld / 512.0D) * 2.0D);
    		this.lastX = xInWorld;
    		this.lastZ = zInWorld;
    		this.lastNoise = noise;
    	}
    	//int l = 0; // TODO: Fix the mesa noise pattern (it's broken for vanilla too).
        return this.clayBands[(yInWorld + noise + 64) % 64];
    }

    @Override
    public LocalMaterialData getSurfaceBlockAtHeight(LocalWorld world, BiomeConfig biomeConfig, int xInWorld, int yInWorld, int zInWorld)
    {
    	return getBand(xInWorld, yInWorld, zInWorld);
    }
    
    @Override
    public LocalMaterialData getGroundBlockAtHeight(LocalWorld world, BiomeConfig biomeConfig, int xInWorld, int yInWorld, int zInWorld)
    {
    	return getBand(xInWorld, yInWorld, zInWorld);
    }
    
    // net.minecraft.world.biome.BiomeMesa.generateBands
    private void generateBands(long p_150619_1_)
    {
        this.clayBands = new LocalMaterialData[64];
        Arrays.fill(this.clayBands, MaterialHelper.HARDENED_CLAY);
        Random random = new Random(p_150619_1_);

        this.clayBandsOffsetNoise = new NoiseGeneratorPerlinMesaBlocks(random, 1);

        for (int l1 = 0; l1 < 64; ++l1)
        {
            l1 += random.nextInt(5) + 1;

            if (l1 < 64)
            {
                this.clayBands[l1] = MaterialHelper.ORANGE_STAINED_CLAY;
            }
        }

        int i2 = random.nextInt(4) + 2;

        for (int i = 0; i < i2; ++i)
        {
            int j = random.nextInt(3) + 1;
            int k = random.nextInt(64);

            for (int l = 0; k + l < 64 && l < j; ++l)
            {
                this.clayBands[k + l] = MaterialHelper.YELLOW_STAINED_CLAY;
            }
        }

        int j2 = random.nextInt(4) + 2;

        for (int k2 = 0; k2 < j2; ++k2)
        {
            int i3 = random.nextInt(3) + 2;
            int l3 = random.nextInt(64);

            for (int i1 = 0; l3 + i1 < 64 && i1 < i3; ++i1)
            {
                this.clayBands[l3 + i1] = MaterialHelper.BROWN_STAINED_CLAY;
            }
        }

        int l2 = random.nextInt(4) + 2;

        for (int j3 = 0; j3 < l2; ++j3)
        {
            int i4 = random.nextInt(3) + 1;
            int k4 = random.nextInt(64);

            for (int j1 = 0; k4 + j1 < 64 && j1 < i4; ++j1)
            {
                this.clayBands[k4 + j1] = MaterialHelper.RED_STAINED_CLAY;
            }
        }

        int k3 = random.nextInt(3) + 3;
        int j4 = 0;

        for (int l4 = 0; l4 < k3; ++l4)
        {
            int i5 = 1;
            j4 += random.nextInt(16) + 4;

            for (int k1 = 0; j4 + k1 < 64 && k1 < 1; ++k1)
            {
                this.clayBands[j4 + k1] = MaterialHelper.WHITE_STAINED_CLAY;

                if (j4 + k1 > 1 && random.nextBoolean())
                {
                    this.clayBands[j4 + k1 - 1] = MaterialHelper.SILVER_STAINED_CLAY;
                }

                if (j4 + k1 < 63 && random.nextBoolean())
                {
                    this.clayBands[j4 + k1 + 1] = MaterialHelper.SILVER_STAINED_CLAY;
                }
            }
        }
    }

    boolean clayBandsGenerated = false;
    // net.minecraft.world.biome.BiomeMesa.genTerrainBlocks
    @Override
    public void spawn(LocalWorld world, GeneratingChunk generatingChunk, ChunkBuffer chunkBuffer, BiomeConfig biomeConfig, int xInWorld, int zInWorld)
    {    	
        long worldSeed = world.getSeed();
        if (this.clayBands == null || !clayBandsGenerated)
        {
            this.generateBands(worldSeed);
        }

        if (this.pillarNoise == null || this.pillarRoofNoise == null || !clayBandsGenerated)
        {
            Random random = new Random(this.worldSeed);
            this.pillarNoise = new NoiseGeneratorPerlinMesaBlocks(random, 4);
            this.pillarRoofNoise = new NoiseGeneratorPerlinMesaBlocks(random, 1);
        }
        
        clayBandsGenerated = true;
        
        int x = xInWorld & 15;
        int z = zInWorld & 15;
        double noise = generatingChunk.getNoise(x, z);
        
    	this.worldSeed = worldSeed;
        // Bryce spike calculations
        double bryceHeight = 0.0D;
        if (this.brycePillars)
        {
            int k = (xInWorld & -16) + (zInWorld & 15);
            int l = (zInWorld & -16) + (xInWorld & 15);
            double bryceNoiseValue = Math.min(Math.abs(noise), this.pillarNoise.getValue((double)k * 0.25D, (double)l * 0.25D));

            if (bryceNoiseValue > 0.0D)
            {
                double d3 = 0.001953125D;
                double d4 = Math.abs(this.pillarRoofNoise.getValue((double)k * d3, (double)l * d3));
                bryceHeight = bryceNoiseValue * bryceNoiseValue * 2.5D;
                double d5 = Math.ceil(d4 * 50.0D) + 14.0D;

                if (bryceHeight > d5)
                {
                    bryceHeight = d5;
                }

                bryceHeight += 64.0D;
            }
        }
        
        int waterLevel = generatingChunk.getWaterLevel(x, z);
        
        boolean useDefaultGroundBlock = true;
        LocalMaterialData currentGroundBlock = null;
        boolean groundIsStainedClay = true;
        
        int noisePlusRandomFactor = (int) (noise / 3.0D + 3.0D + generatingChunk.random.nextDouble() * 0.25D);
        
        int k1 = -1;
        boolean belowSand = false;
    	boolean useGroundBlockGround = false;
    	boolean useGroundBlockStone = false;
        int i1 = 0;

        int maxHeight = generatingChunk.heightCap - 1;
        
        int minHeight = 0;
        LocalMaterialData worldMaterial;
        
        for (int y = maxHeight; y >= minHeight; y--)
        {
        	worldMaterial = chunkBuffer.getBlock(x, y, z);
            if (y < (int) bryceHeight && worldMaterial.isAir())
            {
                chunkBuffer.setBlock(x, y, z, biomeConfig.getStoneBlockReplaced(world, y));
            }

            if (generatingChunk.mustCreateBedrockAt(biomeConfig.worldConfig, y))
            {
                chunkBuffer.setBlock(x, y, z, biomeConfig.worldConfig.getBedrockBlockReplaced(world, biomeConfig, y));
            }
            else if (i1 < 15 || this.brycePillars)
            {
                if (worldMaterial.isEmptyOrAir())
                {
                    k1 = -1;
                }
                else if (worldMaterial.isSolid())
                {
                    if (k1 == -1)
                    {
                        belowSand = false;

                        if (noisePlusRandomFactor <= 0)
                        {
                        	useDefaultGroundBlock = false;
                        	useGroundBlockGround = false;
                        	useGroundBlockStone = true;
                        }
                        else if (y >= waterLevel - 4 && y <= waterLevel + 1)
                        {
                        	useDefaultGroundBlock = false;
                        	useGroundBlockGround = true;
                        	useGroundBlockStone = false;
                        }
                        
                        k1 = noisePlusRandomFactor + Math.max(0, y - waterLevel);
                        if (y >= waterLevel - 1)
                        {
                            if (this.hasForest && y > 86 + noisePlusRandomFactor * 2)
                            {
                                chunkBuffer.setBlock(x, y, z, biomeConfig.getSurfaceBlockReplaced(world, y));
                            }
                            else if (y > waterLevel + 3 + noisePlusRandomFactor)
                            {
                                if (y >= 64 && y <= 127)
                                {
                                	worldMaterial = this.getBand(xInWorld, y, zInWorld).parseWithBiomeAndHeight(world, biomeConfig, y);
                                } else {
                                	worldMaterial = MaterialHelper.ORANGE_STAINED_CLAY.parseWithBiomeAndHeight(world, biomeConfig, y);
                                }

                                chunkBuffer.setBlock(x, y, z, worldMaterial);
                            } else {
                                chunkBuffer.setBlock(x, y, z, MaterialHelper.RED_SAND.parseWithBiomeAndHeight(world, biomeConfig, y));
                                belowSand = true;
                            }
                        } else {                        	
                            if (useGroundBlockGround)
                            {
                                currentGroundBlock = biomeConfig.getStoneBlockReplaced(world, y);
                            }
                            else if (useGroundBlockStone)
                            {
                                currentGroundBlock = biomeConfig.getGroundBlockReplaced(world, y);
                            }                        	
                            else if(useDefaultGroundBlock)
                        	{
                        		currentGroundBlock = MaterialHelper.WHITE_STAINED_CLAY.parseWithBiomeAndHeight(world, biomeConfig, y);
                        	}
                            
                            if (groundIsStainedClay)
                            {
                                chunkBuffer.setBlock(x, y, z, MaterialHelper.ORANGE_STAINED_CLAY.parseWithBiomeAndHeight(world, biomeConfig, y));
                            } else {
                            	chunkBuffer.setBlock(x, y, z, currentGroundBlock);
                            }
                        }
                    }
                    else if (k1 > 0)
                    {
                        --k1;
                        if (belowSand)
                        {
                            chunkBuffer.setBlock(x, y, z, MaterialHelper.ORANGE_STAINED_CLAY.parseWithBiomeAndHeight(world, biomeConfig, y));
                        } else {
                        	worldMaterial = this.getBand(xInWorld, y, zInWorld).parseWithBiomeAndHeight(world, biomeConfig, y);
                            chunkBuffer.setBlock(x, y, z, worldMaterial);
                        }
                    }
                    ++i1;
                }
            }
        }
    }

    @Override
    public String toString()
    {
        if (this.hasForest)
        {
            return NAME_FOREST;
        }
        if (this.brycePillars)
        {
            return NAME_BRYCE;
        }
        return NAME_NORMAL;
    }
}
