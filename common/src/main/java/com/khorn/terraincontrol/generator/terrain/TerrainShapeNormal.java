package com.khorn.terraincontrol.generator.terrain;

import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.ConfigProvider;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.biome.BiomeGenerator;
import com.khorn.terraincontrol.generator.biome.OldBiomeGenerator;
import com.khorn.terraincontrol.generator.biome.OutputType;
import com.khorn.terraincontrol.generator.noise.NoiseGeneratorNewOctaves;
import com.khorn.terraincontrol.generator.noise.NoiseGeneratorPerlinOctaves;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.helpers.MathHelper;

import java.util.Random;

public class TerrainShapeNormal extends TerrainShapeBase
{

    private final Random random;
    private final NoiseGeneratorPerlinOctaves noiseGen1;
    private final NoiseGeneratorPerlinOctaves noiseGen2;
    private final NoiseGeneratorPerlinOctaves noiseGen3;
    private final NoiseGeneratorPerlinOctaves noiseGen5;
    private final NoiseGeneratorPerlinOctaves noiseGen6;

    private final ConfigProvider configProvider;

    private final int heightScale;
    private final int heightCap;

    private final int maxSmoothDiameter;
    private final int maxSmoothRadius;
    private final int maxYSections;
    private final float[] nearBiomeWeightArray;

    private final ChunkSpecific chunkSpecific = new ChunkSpecific();

    /**
     * Used to calculate and store all variables that need to be changed when
     * noise for a new region is created. All the arrays are reused when
     * possible to save memory.
     *
     * @see #open(BiomeGenerator, int, int, int, int, int, int)
     */
    private class ChunkSpecific
    {
        private boolean inUse = false;

        private int[] biomeArray;
        private int[] riverArray;
        // Water level at lower resolution
        private byte[] waterLevelRaw;
        private double[] rawTerrain;

        private double[] noise3;
        private double[] noise1;
        private double[] noise2;
        private double[] noise5;
        private double[] noise6;

        private double riverVol;
        private double riverHeight;
        // Always false if improved rivers disabled
        private boolean riverFound = false;

        private double volatilityFactor;
        private double heightFactor;

        private int xSize;
        private int zSize;
        private BiomeGenerator biomeGenerator;

        private void generateTerrainNoise(BiomeGenerator biomeGenerator, int xStart, int yStart, int zStart, int xSize, int ySize, int zSize, int usedYSections)
        {
            this.inUse = true;

            this.xSize = xSize;
            this.zSize = zSize;
            this.biomeGenerator = biomeGenerator;

            if (rawTerrain == null || rawTerrain.length != xSize * ySize * zSize)
            {
                rawTerrain = new double[xSize * ySize * zSize];
            }
            if (waterLevelRaw == null || waterLevelRaw.length != xSize * zSize)
            {
                waterLevelRaw = new byte[xSize * zSize];
            }
            if (biomeGenerator.canGenerateUnZoomed())
            {
                this.biomeArray = biomeGenerator.getBiomesUnZoomed(this.biomeArray, xStart - maxSmoothRadius, zStart - maxSmoothRadius, xSize + maxSmoothDiameter, zSize + maxSmoothDiameter, OutputType.DEFAULT_FOR_WORLD);
            } else
            {
                this.biomeArray = biomeGenerator.getBiomes(this.biomeArray, xStart * TerrainShapeBase.PIECE_X_SIZE, zStart * TerrainShapeBase.PIECE_Z_SIZE, (xSize - 1) * TerrainShapeBase.PIECE_X_SIZE, (zSize - 1) * TerrainShapeBase.PIECE_Z_SIZE, OutputType.DEFAULT_FOR_WORLD);
            }

            WorldConfig worldConfig = configProvider.getWorldConfig();
            if (worldConfig.improvedRivers)
                this.riverArray = biomeGenerator.getBiomesUnZoomed(this.riverArray, xStart - maxSmoothRadius,
                        zStart - maxSmoothRadius, xSize + maxSmoothDiameter, zSize + maxSmoothDiameter, OutputType.ONLY_RIVERS);

            final double xzScale = 684.41200000000003D * worldConfig.getFractureHorizontal();
            final double yScale = 684.41200000000003D * worldConfig.getFractureVertical();

            if (worldConfig.oldTerrainGenerator)
            {
                noise5 = noiseGen5.Noise2D(noise5, xStart, zStart, xSize, zSize, 1.121D, 1.121D);
            }
            noise6 = noiseGen6.Noise2D(noise6, xStart, zStart, xSize, zSize, 200.0D, 200.0D);

            noise3 = noiseGen3.Noise3D(noise3, xStart, yStart, zStart, xSize, ySize, zSize, xzScale / 80.0D, yScale / 160.0D, xzScale / 80.0D);
            noise1 = noiseGen1.Noise3D(noise1, xStart, yStart, zStart, xSize, ySize, zSize, xzScale, yScale, xzScale);
            noise2 = noiseGen2.Noise3D(noise2, xStart, yStart, zStart, xSize, ySize, zSize, xzScale, yScale, xzScale);

            int i3D = 0;
            int i2D = 0;

            for (int x = 0; x < xSize; x++)
            {
                for (int z = 0; z < zSize; z++)
                {

                    final int biomeId = biomeArray[(x + maxSmoothRadius + (z + maxSmoothRadius) * (xSize + maxSmoothDiameter))];
                    final BiomeConfig biomeConfig = configProvider.getBiomeByIdOrNull(biomeId).getBiomeConfig();

                    double noiseHeight = noise6[i2D] / 8000.0D;
                    if (noiseHeight < 0.0D)
                    {
                        noiseHeight = -noiseHeight * 0.3D;
                    }
                    noiseHeight = noiseHeight * 3.0D - 2.0D;

                    if (noiseHeight < 0.0D)
                    {
                        noiseHeight /= 2.0D;
                        if (noiseHeight < -1.0D)
                        {
                            noiseHeight = -1.0D;
                        }
                        noiseHeight -= biomeConfig.maxAverageDepth;
                        noiseHeight /= 1.4D;
                        noiseHeight /= 2.0D;
                    } else
                    {
                        if (noiseHeight > 1.0D)
                        {
                            noiseHeight = 1.0D;
                        }
                        noiseHeight += biomeConfig.maxAverageHeight;
                        noiseHeight /= 8.0D;
                    }

                    if (!worldConfig.oldTerrainGenerator)
                    {
                        if (worldConfig.improvedRivers)
                            biomeFactorWithRivers(x, z, usedYSections, noiseHeight);
                        else
                            biomeFactor(x, z, usedYSections, noiseHeight);
                    } else
                        oldBiomeFactor(x, z, i2D, usedYSections, noiseHeight);

                    i2D++;

                    for (int y = 0; y < ySize; y++)
                    {
                        double output;
                        double d8;

                        if (riverFound)
                        {
                            d8 = (riverHeight - y) * 12.0D * 128.0D / heightCap / riverVol;
                        } else
                        {
                            d8 = (heightFactor - y) * 12.0D * 128.0D / heightCap / volatilityFactor;
                        }

                        if (d8 > 0.0D)
                        {
                            d8 *= 4.0D;
                        }

                        final double vol1 = noise1[i3D] / 512.0D * biomeConfig.volatility1;
                        final double vol2 = noise2[i3D] / 512.0D * biomeConfig.volatility2;

                        final double noise = (noise3[i3D] / 10.0D + 1.0D) / 2.0D;
                        if (noise < biomeConfig.volatilityWeight1)
                        {
                            output = vol1;
                        } else if (noise > biomeConfig.volatilityWeight2)
                        {
                            output = vol2;
                        } else
                        {
                            output = vol1 + (vol2 - vol1) * noise;
                        }

                        if (!biomeConfig.disableNotchHeightControl)
                        {
                            output += d8;

                            if (y > ySize - 4)
                            {
                                final double d12 = (y - (ySize - 4)) / 3.0F;
                                // Reduce last three layers
                                output = output * (1.0D - d12) + -10.0D * d12;
                            }

                        }
                        if (riverFound)
                        {
                            output += biomeConfig.riverHeightMatrix[y];
                        } else
                        {
                            output += biomeConfig.heightMatrix[y];
                        }

                        rawTerrain[i3D] = output;
                        i3D++;
                    }
                }
            }
        }

        private void oldBiomeFactor(int x, int z, int i4, int ySections, double noiseHeight)
        {
            BiomeGenerator unwrapped = biomeGenerator.unwrap();
            if (unwrapped instanceof OldBiomeGenerator)
            {
                OldBiomeGenerator oldBiomeGenerator = (OldBiomeGenerator) unwrapped;
                int index = z * 48 + 17 + x * 3;
                double product = oldBiomeGenerator.oldTemperature1[index] * oldBiomeGenerator.oldWetness[index];
                this.volatilityFactor = 1.0 - product;
            } else
            {
                final BiomeConfig biomeConfig = toBiomeConfig(this.biomeArray[(x + maxSmoothRadius + (z + maxSmoothRadius)
                        * (xSize + maxSmoothDiameter))]);
                this.volatilityFactor = (1.0D - Math.min(1, biomeConfig.biomeTemperature) * biomeConfig.biomeWetness);
            }

            volatilityFactor *= volatilityFactor;
            volatilityFactor = 1.0D - volatilityFactor * volatilityFactor;

            volatilityFactor = (noise3[i4] + 256.0D) / 512.0D * volatilityFactor;
            if (volatilityFactor > 1.0D)
            {
                volatilityFactor = 1.0D;
            }
            if (volatilityFactor < 0.0D || noiseHeight < 0.0D)
            {
                volatilityFactor = 0.0D;
            }

            volatilityFactor += 0.5D;
            heightFactor = ySections * (2.0D + noiseHeight) / 4.0D;
        }

        private void biomeFactor(int x, int z, int ySections, double noiseHeight)
        {
            float volatilitySum = 0.0F;
            double heightSum = 0.0F;
            float biomeWeightSum = 0.0F;

            final BiomeConfig centerBiomeConfig = toBiomeConfig(biomeArray[(x + maxSmoothRadius + (z + maxSmoothRadius) * (xSize + maxSmoothDiameter))]);
            final int lookRadius = centerBiomeConfig.smoothRadius;

            float nextBiomeHeight, biomeWeight;

            for (int nextX = -lookRadius; nextX <= lookRadius; nextX++)
            {
                for (int nextZ = -lookRadius; nextZ <= lookRadius; nextZ++)
                {
                    final BiomeConfig nextBiomeConfig = toBiomeConfig(biomeArray[(x + nextX + maxSmoothRadius + (z + nextZ + maxSmoothRadius) * (xSize + maxSmoothDiameter))]);

                    nextBiomeHeight = nextBiomeConfig.biomeHeight;

                    biomeWeight = nearBiomeWeightArray[(nextX + maxSmoothRadius + (nextZ + maxSmoothRadius) * maxSmoothDiameter)] / (nextBiomeHeight + 2.0F);
                    biomeWeight = Math.abs(biomeWeight);
                    if (nextBiomeHeight > centerBiomeConfig.biomeHeight)
                    {
                        biomeWeight /= 2.0F;
                    }
                    volatilitySum += nextBiomeConfig.biomeVolatility * biomeWeight;
                    heightSum += nextBiomeHeight * biomeWeight;
                    biomeWeightSum += biomeWeight;
                }
            }

            volatilitySum /= biomeWeightSum;
            heightSum /= biomeWeightSum;

            waterLevelRaw[x * zSize + z] = (byte) centerBiomeConfig.waterLevelMax;

            volatilitySum = volatilitySum * 0.9F + 0.1F;   // Must be != 0
            heightSum = (heightSum * 4.0F - 1.0F) / 8.0F;  // Silly magic
            // numbers

            volatilityFactor = volatilitySum;
            heightFactor = ySections * (2.0D + heightSum + noiseHeight * 0.2D) / 4.0D;
        }

        private void biomeFactorWithRivers(int x, int z, int ySections, double noiseHeight)
        {
            float volatilitySum = 0.0F;
            float heightSum = 0.0F;
            float WeightSum = 0.0F;

            float riverVolatilitySum = 0.0F;
            float riverHeightSum = 0.0F;
            float riverWeightSum = 0.0F;

            final BiomeConfig biomeConfig = toBiomeConfig(biomeArray[(x + maxSmoothRadius + (z + maxSmoothRadius) * (xSize + maxSmoothDiameter))]);

            final int lookRadius = biomeConfig.smoothRadius;

            riverFound = riverArray[(x + maxSmoothRadius + (z + maxSmoothRadius) * (xSize + maxSmoothDiameter))] == 1;

            final float riverCenterHeight = riverFound ? biomeConfig.riverHeight : biomeConfig.biomeHeight;

            BiomeConfig nextBiomeConfig;
            float nextBiomeHeight, biomeWeight, nextRiverHeight, riverWeight;

            for (int nextX = -lookRadius; nextX <= lookRadius; nextX++)
            {
                for (int nextZ = -lookRadius; nextZ <= lookRadius; nextZ++)
                {

                    nextBiomeConfig = toBiomeConfig(biomeArray[(x + nextX + maxSmoothRadius + (z + nextZ + maxSmoothRadius) * (xSize + maxSmoothDiameter))]);
                    nextBiomeHeight = nextBiomeConfig.biomeHeight;
                    biomeWeight = nearBiomeWeightArray[(nextX + maxSmoothRadius + (nextZ + maxSmoothRadius) * maxSmoothDiameter)] / (nextBiomeHeight + 2.0F);

                    biomeWeight = Math.abs(biomeWeight);
                    if (nextBiomeHeight > biomeConfig.biomeHeight)
                    {
                        biomeWeight /= 2.0F;
                    }
                    volatilitySum += nextBiomeConfig.biomeVolatility * biomeWeight;
                    heightSum += nextBiomeHeight * biomeWeight;
                    WeightSum += biomeWeight;

                    // River part

                    boolean isRiver = false;
                    if (riverArray[(x + nextX + maxSmoothRadius + (z + nextZ + maxSmoothRadius) * (xSize + maxSmoothDiameter))] == 1)
                    {
                        riverFound = true;
                        isRiver = true;
                    }

                    nextRiverHeight = (isRiver) ? nextBiomeConfig.riverHeight : nextBiomeHeight;
                    riverWeight = nearBiomeWeightArray[(nextX + maxSmoothRadius + (nextZ + maxSmoothRadius) * maxSmoothDiameter)] / (nextRiverHeight + 2.0F);

                    riverWeight = Math.abs(riverWeight);
                    if (nextRiverHeight > riverCenterHeight)
                    {
                        nextRiverHeight = riverCenterHeight;
                    }
                    riverVolatilitySum += (isRiver ? nextBiomeConfig.riverVolatility : nextBiomeConfig.biomeVolatility) * riverWeight;
                    riverHeightSum += nextRiverHeight * riverWeight;
                    riverWeightSum += riverWeight;
                }
            }

            volatilitySum /= WeightSum;
            heightSum /= WeightSum;

            riverVolatilitySum /= riverWeightSum;
            riverHeightSum /= riverWeightSum;

            int waterLevelSum = riverFound ? biomeConfig.riverWaterLevel : biomeConfig.waterLevelMax;
            waterLevelRaw[x * zSize + z] = (byte) waterLevelSum;

            volatilitySum = volatilitySum * 0.9F + 0.1F;   // Must be != 0
            heightSum = (heightSum * 4.0F - 1.0F) / 8.0F;  // Silly magic
            // numbers

            volatilityFactor = volatilitySum;
            heightFactor = ySections * (2.0D + heightSum + noiseHeight * 0.2D) / 4.0D;

            riverVolatilitySum = riverVolatilitySum * 0.9F + 0.1F; // Must be !=
            // 0
            riverHeightSum = (riverHeightSum * 4.0F - 1.0F) / 8.0F;

            riverVol = riverVolatilitySum;
            riverHeight = ySections * (2.0D + riverHeightSum + noiseHeight * 0.2D) / 4.0D;
        }
    }

    public TerrainShapeNormal(ConfigProvider configs, long seed)
    {
        WorldConfig worldConfig = configs.getWorldConfig();

        configProvider = configs;
        heightCap = worldConfig.worldHeightCap;
        heightScale = worldConfig.worldHeightScale;

        random = new Random(seed);

        noiseGen1 = new NoiseGeneratorPerlinOctaves(random, 16);
        noiseGen2 = new NoiseGeneratorPerlinOctaves(random, 16);
        noiseGen3 = new NoiseGeneratorPerlinOctaves(random, 8);
        new NoiseGeneratorNewOctaves(this.random, 4);
        noiseGen5 = new NoiseGeneratorPerlinOctaves(random, 10);
        noiseGen6 = new NoiseGeneratorPerlinOctaves(random, 16);

        // Contains 2d array maxSmoothDiameter*maxSmoothDiameter.
        // Maximum weight is in array center.

        maxSmoothDiameter = worldConfig.maxSmoothRadius * 2 + 1;
        maxSmoothRadius = worldConfig.maxSmoothRadius;

        nearBiomeWeightArray = new float[maxSmoothDiameter * maxSmoothDiameter];

        for (int x = -maxSmoothRadius; x <= maxSmoothRadius; x++)
        {
            for (int z = -maxSmoothRadius; z <= maxSmoothRadius; z++)
            {
                final float f1 = 10.0F / MathHelper.sqrt(x * x + z * z + 0.2F);
                nearBiomeWeightArray[(x + maxSmoothRadius + (z + maxSmoothRadius) * maxSmoothDiameter)] = f1;
            }
        }

        maxYSections = heightCap / TerrainShapeBase.PIECE_Y_SIZE + 1;

    }

    @Override
    public void open(BiomeGenerator biomeGenerator, ChunkCoordinate chunkCoord)
    {
        int chunkX = chunkCoord.getChunkX();
        int chunkZ = chunkCoord.getChunkZ();
        open(biomeGenerator, chunkX * TerrainShapeBase.PIECES_PER_CHUNK_X, 0, chunkZ * TerrainShapeBase.PIECES_PER_CHUNK_Z, TerrainShapeBase.PIECES_PER_CHUNK_X + 1, maxYSections, TerrainShapeBase.PIECES_PER_CHUNK_Z + 1);
    }

    @Override
    public void open(BiomeGenerator biomeGenerator, int xStart, int yStart, int zStart, int xSize, int ySize, int zSize)
    {
        if (chunkSpecific.inUse)
        {
            throw new IllegalStateException("Already generating for another chunk");
        }
        int usedYSections = heightScale / TerrainShapeBase.PIECE_Y_SIZE + 1;
        chunkSpecific.generateTerrainNoise(biomeGenerator, xStart, yStart, zStart, xSize, ySize, zSize, usedYSections);
    }

    /**
     * Gets the BiomeConfig with the given id.
     *
     * @param id The generation id of the biome.
     * @return The BiomeConfig.
     */
    private BiomeConfig toBiomeConfig(int id)
    {
        return configProvider.getBiomeByIdOrNull(id).getBiomeConfig();
    }

    @Override
    public int getWaterLevel(int noisePieceX, int noisePieceZ)
    {
        return chunkSpecific.waterLevelRaw[noisePieceX * chunkSpecific.zSize + noisePieceZ] & 0xFF;
    }

    @Override
    public double getNoise(int noisePieceX, int noisePieceY, int noisePieceZ)
    {
        return chunkSpecific.rawTerrain[(noisePieceX * chunkSpecific.zSize + noisePieceZ) * maxYSections + (noisePieceY)];
    }

    @Override
    public void close()
    {
        chunkSpecific.inUse = false;
    }


}
