package com.khorn.terraincontrol.generator.surface;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.generator.ChunkBuffer;
import com.khorn.terraincontrol.generator.GeneratingChunk;
import com.khorn.terraincontrol.generator.noise.NoiseGeneratorNewOctaves;
import com.khorn.terraincontrol.util.helpers.MathHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import java.util.Arrays;
import java.util.Random;

public class MesaSurfaceGenerator implements SurfaceGenerator
{
    public static final String NAME_NORMAL = "Mesa";
    public static final String NAME_FOREST = "MesaForest";
    public static final String NAME_BRYCE = "MesaBryce";

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

    private LocalMaterialData[] blockDataValuesArray;
    private boolean isForestMesa;
    private boolean isBryceMesa;
    private NoiseGeneratorNewOctaves noiseGenBryce1;
    private NoiseGeneratorNewOctaves noiseGenBryce2;
    private NoiseGeneratorNewOctaves noiseGenBlockData;

    private final LocalMaterialData hardenedClay;
    private final LocalMaterialData redSand;
    private final LocalMaterialData whiteStainedClay;
    private final LocalMaterialData orangeStainedClay;
    private final LocalMaterialData yellowStainedClay;
    private final LocalMaterialData brownStainedClay;
    private final LocalMaterialData redStainedClay;
    private final LocalMaterialData silverStainedClay;
    private final LocalMaterialData coarseDirt;

    public MesaSurfaceGenerator(boolean mountainMesa, boolean forestMesa)
    {
        this.isBryceMesa = mountainMesa;
        this.isForestMesa = forestMesa;

        this.hardenedClay = TerrainControl.toLocalMaterialData(DefaultMaterial.HARD_CLAY, 0);
        this.redSand = TerrainControl.toLocalMaterialData(DefaultMaterial.SAND, 1);
        this.coarseDirt = TerrainControl.toLocalMaterialData(DefaultMaterial.DIRT, 1);
        this.whiteStainedClay = TerrainControl.toLocalMaterialData(DefaultMaterial.STAINED_CLAY, 0);
        this.orangeStainedClay = TerrainControl.toLocalMaterialData(DefaultMaterial.STAINED_CLAY, 1);
        this.yellowStainedClay = TerrainControl.toLocalMaterialData(DefaultMaterial.STAINED_CLAY, 4);
        this.brownStainedClay = TerrainControl.toLocalMaterialData(DefaultMaterial.STAINED_CLAY, 12);
        this.redStainedClay = TerrainControl.toLocalMaterialData(DefaultMaterial.STAINED_CLAY, 14);
        this.silverStainedClay = TerrainControl.toLocalMaterialData(DefaultMaterial.STAINED_CLAY, 8);
    }

    private LocalMaterialData getBlockData(int i, int j, int k)
    {
        int l = (int) Math.round(this.noiseGenBlockData.a(i * 1.0D / 512.0D, i * 1.0D / 512.0D) * 2.0D);

        return this.blockDataValuesArray[(j + l + 64) % 64];
    }

    private void initializeSmallByteArray(Random random)
    {
        this.blockDataValuesArray = new LocalMaterialData[64];
        Arrays.fill(this.blockDataValuesArray, this.hardenedClay);

        this.noiseGenBlockData = new NoiseGeneratorNewOctaves(random, 1);

        int j;

        for (j = 0; j < 64; ++j)
        {
            j += random.nextInt(5) + 1;
            if (j < 64)
            {
                this.blockDataValuesArray[j] = this.orangeStainedClay;
            }
        }

        j = random.nextInt(4) + 2;

        int k;
        int l;
        int i1;
        int j1;

        for (k = 0; k < j; ++k)
        {
            l = random.nextInt(3) + 1;
            i1 = random.nextInt(64);

            for (j1 = 0; i1 + j1 < 64 && j1 < l; ++j1)
            {
                this.blockDataValuesArray[i1 + j1] = this.yellowStainedClay;
            }
        }

        k = random.nextInt(4) + 2;

        int k1;

        for (l = 0; l < k; ++l)
        {
            i1 = random.nextInt(3) + 2;
            j1 = random.nextInt(64);

            for (k1 = 0; j1 + k1 < 64 && k1 < i1; ++k1)
            {
                this.blockDataValuesArray[j1 + k1] = this.brownStainedClay;
            }
        }

        l = random.nextInt(4) + 2;

        for (i1 = 0; i1 < l; ++i1)
        {
            j1 = random.nextInt(3) + 1;
            k1 = random.nextInt(64);

            for (int l1 = 0; k1 + l1 < 64 && l1 < j1; ++l1)
            {
                this.blockDataValuesArray[k1 + l1] = this.redStainedClay;
            }
        }

        i1 = random.nextInt(3) + 3;
        j1 = 0;

        for (k1 = 0; k1 < i1; ++k1)
        {
            byte b0 = 1;

            j1 += random.nextInt(16) + 4;

            for (int i2 = 0; j1 + i2 < 64 && i2 < b0; ++i2)
            {
                this.blockDataValuesArray[j1 + i2] = this.whiteStainedClay;
                if (j1 + i2 > 1 && random.nextBoolean())
                {
                    this.blockDataValuesArray[j1 + i2 - 1] = this.silverStainedClay;
                }

                if (j1 + i2 < 63 && random.nextBoolean())
                {
                    this.blockDataValuesArray[j1 + i2 + 1] = this.silverStainedClay;
                }
            }
        }
    }

    @Override
    public void spawn(GeneratingChunk generatingChunk, ChunkBuffer chunkBuffer, BiomeConfig biomeConfig, int xInWorld, int zInWorld)
    {
        int x = xInWorld & 0xf;
        int z = zInWorld & 0xf;
        double noise = generatingChunk.getNoise(x, z);
        if (this.blockDataValuesArray == null)
        {
            this.initializeSmallByteArray(generatingChunk.random);
        }

        // Bryce spike calculations
        double bryceHeight = 0.0D;
        if (this.isBryceMesa)
        {
            if (this.noiseGenBryce1 == null || this.noiseGenBryce2 == null)
            {
                Random newRandom = new Random(generatingChunk.random.nextLong());

                this.noiseGenBryce1 = new NoiseGeneratorNewOctaves(newRandom, 4);
                this.noiseGenBryce2 = new NoiseGeneratorNewOctaves(newRandom, 1);
            }

            int k = (xInWorld & -16) + (zInWorld & 15);
            int l = (zInWorld & -16) + (xInWorld & 15);
            double bryceNoiseValue = Math.min(Math.abs(noise), this.noiseGenBryce1.a(k * 0.25D, l * 0.25D));

            if (bryceNoiseValue > 0.0D)
            {
                double d3 = 0.001953125D;
                double d4 = Math.abs(this.noiseGenBryce2.a(k * d3, l * d3));

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
        int noisePlusRandomFactor = (int) (noise / 3.0D + 3.0D + generatingChunk.random.nextDouble() * 0.25D);
        boolean cosNoiseIsLargerThanZero = MathHelper.cos((float) (noise / 3.0D * Math.PI)) > 0.0D;
        int k1 = -1;
        boolean belowSand = false;

        int maxHeight = generatingChunk.heightCap - 1;
        int minHeight = 0;

        for (int y = maxHeight; y >= minHeight; y--)
        {
            if (chunkBuffer.getBlock(x, y, z).isAir() && y < (int) bryceHeight)
            {
                chunkBuffer.setBlock(x, y, z, biomeConfig.stoneBlock);
            }

            if (generatingChunk.mustCreateBedrockAt(biomeConfig.worldConfig, y))
            {
                chunkBuffer.setBlock(x, y, z, biomeConfig.worldConfig.bedrockBlock);
            } else
            {
                LocalMaterialData blockAtPosition = chunkBuffer.getBlock(x, y, z);

                if (blockAtPosition.isAir())
                {
                    k1 = -1;
                } else if (blockAtPosition.isSolid())
                {
                    LocalMaterialData iblockdata3;

                    if (k1 == -1)
                    {
                        belowSand = false;
                        if (noisePlusRandomFactor <= 0)
                        {
                            currentSurfaceBlock = null;
                            currentGroundBlock = biomeConfig.stoneBlock;
                        } else if (y >= waterLevel - 4 && y <= waterLevel + 1)
                        {
                            currentSurfaceBlock = this.whiteStainedClay;
                            currentGroundBlock = biomeConfig.groundBlock;
                        }

                        if (y < waterLevel && (currentSurfaceBlock == null || currentSurfaceBlock.isAir()))
                        {
                            currentSurfaceBlock = biomeConfig.waterBlock;
                        }

                        k1 = noisePlusRandomFactor + Math.max(0, y - waterLevel);
                        if (y >= waterLevel - 1)
                        {
                            if (this.isForestMesa && y > 86 + noisePlusRandomFactor * 2)
                            {
                                if (cosNoiseIsLargerThanZero)
                                {
                                    chunkBuffer.setBlock(x, y, z, this.coarseDirt);
                                } else
                                {
                                    chunkBuffer.setBlock(x, y, z, biomeConfig.surfaceBlock);
                                }
                            } else if (y > waterLevel + 3 + noisePlusRandomFactor)
                            {
                                if (y >= 64 && y <= 127)
                                {
                                    if (cosNoiseIsLargerThanZero)
                                    {
                                        iblockdata3 = this.hardenedClay;
                                    } else
                                    {
                                        iblockdata3 = this.getBlockData(xInWorld, y, zInWorld);
                                    }
                                } else
                                {
                                    iblockdata3 = this.orangeStainedClay;
                                }

                                chunkBuffer.setBlock(x, y, z, iblockdata3);
                            } else
                            {
                                chunkBuffer.setBlock(x, y, z, redSand);
                                belowSand = true;
                            }
                        } else
                        {
                            chunkBuffer.setBlock(x, y, z, currentGroundBlock);
                            if (currentGroundBlock.isMaterial(DefaultMaterial.STAINED_CLAY))
                            {
                                chunkBuffer.setBlock(x, y, z, this.orangeStainedClay);
                            }
                        }
                    } else if (k1 > 0)
                    {
                        --k1;
                        if (belowSand)
                        {
                            chunkBuffer.setBlock(x, y, z, this.orangeStainedClay);
                        } else
                        {
                            iblockdata3 = this.getBlockData(xInWorld, y, zInWorld);
                            chunkBuffer.setBlock(x, y, z, iblockdata3);
                        }
                    }
                }
            }
        }

    }

    @Override
    public String toString()
    {
        if (this.isForestMesa)
        {
            return NAME_FOREST;
        }
        if (this.isBryceMesa)
        {
            return NAME_BRYCE;
        }
        return NAME_NORMAL;
    }

}
