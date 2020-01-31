package com.pg85.otg.generator.surface;

import java.util.Arrays;
import java.util.Random;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.generator.ChunkBuffer;
import com.pg85.otg.generator.GeneratingChunk;
import com.pg85.otg.generator.noise.NoiseGeneratorPerlinMesaBlocks;
import com.pg85.otg.util.helpers.MaterialHelper;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

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

    private final LocalMaterialData hardenedClay;
    private final LocalMaterialData redSand;
    private final LocalMaterialData whiteStainedClay;
    private final LocalMaterialData orangeStainedClay;
    private final LocalMaterialData yellowStainedClay;
    private final LocalMaterialData brownStainedClay;
    private final LocalMaterialData redStainedClay;
    private final LocalMaterialData silverStainedClay;
    private final LocalMaterialData coarseDirt;

    private MesaSurfaceGenerator(boolean mountainMesa, boolean forestMesa)
    {
        this.brycePillars = mountainMesa;
        this.hasForest = forestMesa;

        this.hardenedClay = MaterialHelper.toLocalMaterialData(DefaultMaterial.HARD_CLAY, 0);
        this.redSand = MaterialHelper.toLocalMaterialData(DefaultMaterial.SAND, 1);
        this.coarseDirt = MaterialHelper.toLocalMaterialData(DefaultMaterial.DIRT, 1);
        this.whiteStainedClay = MaterialHelper.toLocalMaterialData(DefaultMaterial.STAINED_CLAY, 0);
        this.orangeStainedClay = MaterialHelper.toLocalMaterialData(DefaultMaterial.STAINED_CLAY, 1);
        this.yellowStainedClay = MaterialHelper.toLocalMaterialData(DefaultMaterial.STAINED_CLAY, 4);
        this.brownStainedClay = MaterialHelper.toLocalMaterialData(DefaultMaterial.STAINED_CLAY, 12);
        this.redStainedClay = MaterialHelper.toLocalMaterialData(DefaultMaterial.STAINED_CLAY, 14);
        this.silverStainedClay = MaterialHelper.toLocalMaterialData(DefaultMaterial.STAINED_CLAY, 8);
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

    private LocalMaterialData getBand(int i, int j, int k)
    {
        int l = (int) Math.round(this.clayBandsOffsetNoise.getValue((double) i / 512.0D, (double) i / 512.0D) * 2.0D);
        return this.clayBands[(j + l + 64) % 64];
    }

    // net.minecraft.world.biome.BiomeMesa.generateBands
    private void generateBands(long p_150619_1_)
    {
        this.clayBands = new LocalMaterialData[64];
        Arrays.fill(this.clayBands, this.hardenedClay);
        Random random = new Random(p_150619_1_);

        this.clayBandsOffsetNoise = new NoiseGeneratorPerlinMesaBlocks(random, 1);

        for (int l1 = 0; l1 < 64; ++l1)
        {
            l1 += random.nextInt(5) + 1;

            if (l1 < 64)
            {
                this.clayBands[l1] = this.orangeStainedClay;
            }
        }

        int i2 = random.nextInt(4) + 2;

        for (int i = 0; i < i2; ++i)
        {
            int j = random.nextInt(3) + 1;
            int k = random.nextInt(64);

            for (int l = 0; k + l < 64 && l < j; ++l)
            {
                this.clayBands[k + l] = this.yellowStainedClay;
            }
        }

        int j2 = random.nextInt(4) + 2;

        for (int k2 = 0; k2 < j2; ++k2)
        {
            int i3 = random.nextInt(3) + 2;
            int l3 = random.nextInt(64);

            for (int i1 = 0; l3 + i1 < 64 && i1 < i3; ++i1)
            {
                this.clayBands[l3 + i1] = this.brownStainedClay;
            }
        }

        int l2 = random.nextInt(4) + 2;

        for (int j3 = 0; j3 < l2; ++j3)
        {
            int i4 = random.nextInt(3) + 1;
            int k4 = random.nextInt(64);

            for (int j1 = 0; k4 + j1 < 64 && j1 < i4; ++j1)
            {
                this.clayBands[k4 + j1] = this.redStainedClay;
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
                this.clayBands[j4 + k1] = this.whiteStainedClay;

                if (j4 + k1 > 1 && random.nextBoolean())
                {
                    this.clayBands[j4 + k1 - 1] = this.silverStainedClay;
                }

                if (j4 + k1 < 63 && random.nextBoolean())
                {
                    this.clayBands[j4 + k1 + 1] = this.silverStainedClay;
                }
            }
        }
    }

    @Override
    public LocalMaterialData getCustomBlockData(LocalWorld world, BiomeConfig biomeConfig, int xInWorld, int yInWorld, int zInWorld)
    {
        int l = (int) Math.round(
                this.clayBandsOffsetNoise.getValue((double) xInWorld / 512.0D, (double) xInWorld / 512.0D) * 2.0D);
        return this.clayBands[(yInWorld + l + 64) % 64];
    }

    // net.minecraft.world.biome.BiomeMesa.genTerrainBlocks
    @Override
    public void spawn(LocalWorld world, GeneratingChunk generatingChunk, ChunkBuffer chunkBuffer, BiomeConfig biomeConfig, int xInWorld, int zInWorld)
    {    	
        long worldSeed = world.getSeed();
        if (this.clayBands == null || this.worldSeed != worldSeed)
        {
            this.generateBands(worldSeed);
        }

        if (this.pillarNoise == null || this.pillarRoofNoise == null || this.worldSeed != worldSeed)
        {
            Random random = new Random(this.worldSeed);
            this.pillarNoise = new NoiseGeneratorPerlinMesaBlocks(random, 4);
            this.pillarRoofNoise = new NoiseGeneratorPerlinMesaBlocks(random, 1);
        }
        
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
        
        LocalMaterialData currentSurfaceBlock = whiteStainedClay;
        LocalMaterialData currentGroundBlock = whiteStainedClay;
        
        LocalMaterialData surfaceBlock = biomeConfig.surfaceBlock.parseForWorld(world);
        LocalMaterialData groundBlock = biomeConfig.groundBlock.parseForWorld(world);
        LocalMaterialData stoneBlock = biomeConfig.stoneBlock.parseForWorld(world);
        LocalMaterialData bedrockBlock = biomeConfig.worldConfig.bedrockBlock.parseForWorld(world);
        LocalMaterialData waterBlock = biomeConfig.waterBlock.parseForWorld(world);
        
        int noisePlusRandomFactor = (int) (noise / 3.0D + 3.0D + generatingChunk.random.nextDouble() * 0.25D);
                
        boolean cosNoiseIsLargerThanZero = Math.cos(noise / 3.0D * Math.PI) > 0.0D;
        
        int k1 = -1;
        boolean belowSand = false;
        int i1 = 0;

        int maxHeight = generatingChunk.heightCap - 1;
        
        int minHeight = 0;
        LocalMaterialData worldMaterial;
        
        for (int y = maxHeight; y >= minHeight; y--)
        {
        	worldMaterial = chunkBuffer.getBlock(x, y, z);
            if (y < (int) bryceHeight && worldMaterial.isAir())
            {
                chunkBuffer.setBlock(x, y, z, stoneBlock);
            }

            if (generatingChunk.mustCreateBedrockAt(biomeConfig.worldConfig, y))
            {
                chunkBuffer.setBlock(x, y, z, bedrockBlock);
            }
            else if (i1 < 15 || this.brycePillars)
            {
            	worldMaterial = chunkBuffer.getBlock(x, y, z);

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
                            currentSurfaceBlock = null;
                            currentGroundBlock = stoneBlock;
                        }
                        else if (y >= waterLevel - 4 && y <= waterLevel + 1)
                        {
                            currentSurfaceBlock = this.whiteStainedClay;
                            currentGroundBlock = groundBlock;
                        }

                        if (y < waterLevel && (currentSurfaceBlock == null || currentSurfaceBlock.isEmptyOrAir()))
                        {
                            currentSurfaceBlock = waterBlock;
                        }

                        k1 = noisePlusRandomFactor + Math.max(0, y - waterLevel);
                        if (y >= waterLevel - 1)
                        {
                            if (this.hasForest && y > 86 + noisePlusRandomFactor * 2)
                            {
                                if (cosNoiseIsLargerThanZero)
                                {
                                    chunkBuffer.setBlock(x, y, z, this.coarseDirt);
                                } else {
                                    chunkBuffer.setBlock(x, y, z, surfaceBlock);
                                }
                            }
                            else if (y > waterLevel + 3 + noisePlusRandomFactor)
                            {
                                if (y >= 64 && y <= 127)
                                {
                                    if (cosNoiseIsLargerThanZero)
                                    {
                                    	worldMaterial = this.hardenedClay;
                                    } else {
                                    	worldMaterial = this.getBand(xInWorld, y, zInWorld);
                                    }
                                } else {
                                	worldMaterial = this.orangeStainedClay;
                                }

                                chunkBuffer.setBlock(x, y, z, worldMaterial);
                            } else {
                                chunkBuffer.setBlock(x, y, z, redSand);
                                belowSand = true;
                            }
                        } else {
                            chunkBuffer.setBlock(x, y, z, currentGroundBlock);
                            if (currentGroundBlock.isMaterial(DefaultMaterial.STAINED_CLAY))
                            {
                                chunkBuffer.setBlock(x, y, z, this.orangeStainedClay);
                            }
                        }
                    }
                    else if (k1 > 0)
                    {
                        --k1;
                        if (belowSand)
                        {
                            chunkBuffer.setBlock(x, y, z, this.orangeStainedClay);
                        } else {
                        	worldMaterial = this.getBand(xInWorld, y, zInWorld);
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
