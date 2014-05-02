package com.khorn.terraincontrol.generator;

import static com.khorn.terraincontrol.util.ChunkCoordinate.CHUNK_X_SIZE;
import static com.khorn.terraincontrol.util.ChunkCoordinate.CHUNK_Y_SIZE;
import static com.khorn.terraincontrol.util.ChunkCoordinate.CHUNK_Z_SIZE;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.configuration.WorldSettings;
import com.khorn.terraincontrol.generator.biome.OutputType;
import com.khorn.terraincontrol.generator.noise.NoiseGeneratorNewOctaves;
import com.khorn.terraincontrol.generator.noise.NoiseGeneratorPerlinOctaves;
import com.khorn.terraincontrol.generator.terrain.CanyonsGen;
import com.khorn.terraincontrol.generator.terrain.CavesGen;
import com.khorn.terraincontrol.generator.terrain.TerrainGenBase;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.helpers.MathHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import java.util.Random;

// Please don`t remove this. This disable warnings about x+0 arithmetic
// operations in my IDE. Khorn.
@SuppressWarnings("PointlessArithmeticExpression")
public class ChunkProviderTC
{
    // Several constants describing the chunk size of Minecraft
    private static final int NOISE_MAX_X = CHUNK_X_SIZE / 4 + 1;
    private static final int NOISE_MAX_Z = CHUNK_Z_SIZE / 4 + 1;

    public static final int HEIGHT_BITS = 8;
    public static final int HEIGHT_BITS_PLUS_FOUR = HEIGHT_BITS + 4;

    private final Random random;
    private final NoiseGeneratorPerlinOctaves noiseGen1;
    private final NoiseGeneratorPerlinOctaves noiseGen2;
    private final NoiseGeneratorPerlinOctaves noiseGen3;
    private final NoiseGeneratorNewOctaves noiseGen4;
    private final NoiseGeneratorPerlinOctaves noiseGen5;
    private final NoiseGeneratorPerlinOctaves noiseGen6;
    private double[] rawTerrain;
    private double[] noise4 = new double[CHUNK_X_SIZE * CHUNK_Z_SIZE];

    private double[] noise3;
    private double[] noise1;
    private double[] noise2;
    private double[] noise5;
    private double[] noise6;
    private float[] nearBiomeWeightArray;

    private double riverVol;
    private double riverHeight;
    // Always false if improved rivers disabled
    private boolean riverFound = false;

    private final LocalWorld localWorld;
    private double volatilityFactor;
    private double heightFactor;

    private WorldSettings worldSettings;

    private final TerrainGenBase caveGen;
    private final TerrainGenBase canyonGen;

    private int[] biomeArray;
    private int[] riverArray;
    // Water level at lower resolution
    private final byte[] waterLevelRaw = new byte[25];
    // Water level for each column
    private final byte[] waterLevel = new byte[CHUNK_X_SIZE * CHUNK_Z_SIZE];

    private final int heightScale;
    private final int heightCap;

    private final int maxSmoothDiameter;
    private final int maxSmoothRadius;

    public ChunkProviderTC(WorldSettings configs, LocalWorld world)
    {
        this.worldSettings = configs;
        this.localWorld = world;
        this.heightCap = world.getHeightCap();
        this.heightScale = world.getHeightScale();

        this.random = new Random(world.getSeed());

        this.noiseGen1 = new NoiseGeneratorPerlinOctaves(this.random, 16);
        this.noiseGen2 = new NoiseGeneratorPerlinOctaves(this.random, 16);
        this.noiseGen3 = new NoiseGeneratorPerlinOctaves(this.random, 8);
        this.noiseGen4 = new NoiseGeneratorNewOctaves(this.random, 4);
        this.noiseGen5 = new NoiseGeneratorPerlinOctaves(this.random, 10);
        this.noiseGen6 = new NoiseGeneratorPerlinOctaves(this.random, 16);

        this.caveGen = new CavesGen(configs.worldConfig, this.localWorld);
        this.canyonGen = new CanyonsGen(configs.worldConfig, this.localWorld);

        // Contains 2d array maxSmoothDiameter*maxSmoothDiameter.
        // Maximum weight is in array center.

        this.maxSmoothDiameter = configs.worldConfig.maxSmoothRadius * 2 + 1;
        this.maxSmoothRadius = configs.worldConfig.maxSmoothRadius;

        this.nearBiomeWeightArray = new float[maxSmoothDiameter * maxSmoothDiameter];

        for (int x = -maxSmoothRadius; x <= maxSmoothRadius; x++)
        {
            for (int z = -maxSmoothRadius; z <= maxSmoothRadius; z++)
            {
                final float f1 = 10.0F / MathHelper.sqrt(x * x + z * z + 0.2F);
                this.nearBiomeWeightArray[(x + maxSmoothRadius + (z + maxSmoothRadius) * maxSmoothDiameter)] = f1;
            }
        }

    }

    public byte[] generate(ChunkCoordinate chunkCoord)
    {
        int x = chunkCoord.getChunkX();
        int z = chunkCoord.getChunkZ();
        this.random.setSeed(x * 341873128712L + z * 132897987541L);

        byte[] blockArray = new byte[CHUNK_X_SIZE * CHUNK_Y_SIZE * CHUNK_Z_SIZE];

        generateTerrain(chunkCoord, blockArray);

        boolean dry = addBiomeBlocksAndCheckWater(chunkCoord, blockArray);

        this.caveGen.generate(chunkCoord, blockArray);
        this.canyonGen.generate(chunkCoord, blockArray);

        if (this.worldSettings.worldConfig.ModeTerrain == WorldConfig.TerrainMode.Normal
                || this.worldSettings.worldConfig.ModeTerrain == WorldConfig.TerrainMode.OldGenerator)
        {
            this.localWorld.prepareDefaultStructures(x, z, dry);
        }

        return blockArray;

    }

    protected void generateTerrain(ChunkCoordinate chunkCoord, byte[] blockArray)
    {
        final int chunkX = chunkCoord.getChunkX();
        final int chunkZ = chunkCoord.getChunkZ();

        final int four = 4;
        final int oneEightOfHeight = this.heightCap / 8;

        final int maxYSections = this.heightCap / 8 + 1;
        final int usedYSections = this.heightScale / 8 + 1;

        if (worldSettings.worldConfig.improvedRivers)
            this.riverArray = this.localWorld.getBiomesUnZoomed(this.riverArray, chunkX * 4 - maxSmoothRadius,
                    chunkZ * 4 - maxSmoothRadius, NOISE_MAX_X + maxSmoothDiameter, NOISE_MAX_Z + maxSmoothDiameter, OutputType.ONLY_RIVERS);

        if (this.localWorld.canBiomeManagerGenerateUnzoomed())
        {
            this.biomeArray = this.localWorld.getBiomesUnZoomed(this.biomeArray, chunkX * 4 - maxSmoothRadius,
                    chunkZ * 4 - maxSmoothRadius, NOISE_MAX_X + maxSmoothDiameter, NOISE_MAX_Z + maxSmoothDiameter,
                    OutputType.DEFAULT_FOR_WORLD);
        } else
        {
            this.biomeArray = this.localWorld.getBiomes(this.biomeArray, chunkX * CHUNK_X_SIZE, chunkZ * CHUNK_Z_SIZE, CHUNK_X_SIZE,
                    CHUNK_Z_SIZE, OutputType.DEFAULT_FOR_WORLD);
        }

        generateTerrainNoise(chunkX * four, 0, chunkZ * four, maxYSections, usedYSections);

        // Now that the raw terrain is generated, replace raw biome array with
        // fine-tuned one.
        this.biomeArray = this.localWorld.getBiomes(this.biomeArray, chunkX * CHUNK_X_SIZE, chunkZ * CHUNK_Z_SIZE, CHUNK_X_SIZE, CHUNK_Z_SIZE,
                OutputType.DEFAULT_FOR_WORLD);

        final double oneEight = 0.125D;
        final int z_step = 1 << HEIGHT_BITS;
        final double oneFourth = 0.25D;

        for (int x = 0; x < four; x++)
        {
            for (int z = 0; z < four; z++)
            {
                // Water level (fill final array based on smaller,
                // non-smoothed
                // array)
                double waterLevel_x0z0 = this.waterLevelRaw[(x + 0) * NOISE_MAX_X + (z + 0)] & 0xFF;
                double waterLevel_x0z1 = this.waterLevelRaw[(x + 0) * NOISE_MAX_X + (z + 1)] & 0xFF;
                final double waterLevel_x1z0 = ((this.waterLevelRaw[(x + 1) * NOISE_MAX_X + (z + 0)] & 0xFF) - waterLevel_x0z0) * oneFourth;
                final double waterLevel_x1z1 = ((this.waterLevelRaw[(x + 1) * NOISE_MAX_X + (z + 1)] & 0xFF) - waterLevel_x0z1) * oneFourth;

                for (int piece_x = 0; piece_x < 4; piece_x++)
                {
                    double waterLevelForArray = waterLevel_x0z0;
                    final double d17_1 = (waterLevel_x0z1 - waterLevel_x0z0) * oneFourth;

                    for (int piece_z = 0; piece_z < 4; piece_z++)
                    {
                        // Fill water level array
                        this.waterLevel[(z * 4 + piece_z) * 16 + (piece_x + x * 4)] = (byte) waterLevelForArray;

                        waterLevelForArray += d17_1;

                    }
                    waterLevel_x0z0 += waterLevel_x1z0;
                    waterLevel_x0z1 += waterLevel_x1z1;

                }

                // Terrain noise
                for (int y = 0; y < oneEightOfHeight; y++)
                {

                    double x0z0 = this.rawTerrain[(((x + 0) * NOISE_MAX_Z + (z + 0)) * maxYSections + (y + 0))];
                    double x0z1 = this.rawTerrain[(((x + 0) * NOISE_MAX_Z + (z + 1)) * maxYSections + (y + 0))];
                    double x1z0 = this.rawTerrain[(((x + 1) * NOISE_MAX_Z + (z + 0)) * maxYSections + (y + 0))];
                    double x1z1 = this.rawTerrain[(((x + 1) * NOISE_MAX_Z + (z + 1)) * maxYSections + (y + 0))];

                    final double x0z0y1 = (this.rawTerrain[(((x + 0) * NOISE_MAX_Z + (z + 0)) * maxYSections + (y + 1))] - x0z0) * oneEight;
                    final double x0z1y1 = (this.rawTerrain[(((x + 0) * NOISE_MAX_Z + (z + 1)) * maxYSections + (y + 1))] - x0z1) * oneEight;
                    final double x1z0y1 = (this.rawTerrain[(((x + 1) * NOISE_MAX_Z + (z + 0)) * maxYSections + (y + 1))] - x1z0) * oneEight;
                    final double x1z1y1 = (this.rawTerrain[(((x + 1) * NOISE_MAX_Z + (z + 1)) * maxYSections + (y + 1))] - x1z1) * oneEight;

                    for (int piece_y = 0; piece_y < 8; piece_y++)
                    {

                        double d11 = x0z0;
                        double d12 = x0z1;
                        final double d13 = (x1z0 - x0z0) * oneFourth;
                        final double d14 = (x1z1 - x0z1) * oneFourth;

                        for (int piece_x = 0; piece_x < 4; piece_x++)
                        {
                            int position = (piece_x + x * 4) << HEIGHT_BITS_PLUS_FOUR | (0 + z * 4) << HEIGHT_BITS | (y * 8 + piece_y);

                            double d16 = d11;
                            final double d17 = (d12 - d11) * oneFourth;
                            for (int piece_z = 0; piece_z < 4; piece_z++)
                            {
                                final BiomeConfig biomeConfig = toBiomeConfig(this.biomeArray[(z * 4 + piece_z) * 16 + (piece_x + x * 4)]);
                                final int waterLevelMax = this.waterLevel[(z * 4 + piece_z) * 16 + (piece_x + x * 4)] & 0xFF;
                                int blockId = 0;
                                if (y * 8 + piece_y < waterLevelMax && y * 8 + piece_y > biomeConfig.waterLevelMin)
                                {
                                    blockId = biomeConfig.waterBlock.getBlockId();
                                }

                                if (d16 > 0.0D)
                                {
                                    blockId = biomeConfig.stoneBlock.getBlockId();
                                }

                                blockArray[position] = (byte) blockId;
                                position += z_step;
                                d16 += d17;
                            }
                            d11 += d13;
                            d12 += d14;
                        }

                        x0z0 += x0z0y1;
                        x0z1 += x0z1y1;
                        x1z0 += x1z0y1;
                        x1z1 += x1z1y1;
                    }
                }
            }
        }

    }

    /**
     * Adds the biome blocks like grass, dirt, sand and sandstone. Also adds
     * bedrock at the bottom of the map.
     * 
     * @param chunkX The chunk X.
     * @param chunkZ The chunk Z.
     * @param blocksArray The blocks in the chunk. Will be modified.
     * @return Whether there is a lot of water in this chunk. If yes, no
     *         villages will be placed.
     */
    protected boolean addBiomeBlocksAndCheckWater(ChunkCoordinate chunkCoord, byte[] blocksArray)
    {
        int dryBlocksOnSurface = 256;

        final double d1 = 0.03125D;
        this.noise4 = this.noiseGen4.a(this.noise4, chunkCoord.getBlockX(), chunkCoord.getBlockZ(), CHUNK_X_SIZE, CHUNK_Z_SIZE, d1 * 2.0D,
                d1 * 2.0D, 1.0D);

        WorldConfig worldConfig = this.worldSettings.worldConfig;

        for (int x = 0; x < CHUNK_X_SIZE; x++)
        {
            for (int z = 0; z < CHUNK_Z_SIZE; z++)
            {
                // The following code is executed for each column in the chunk

                // Get the current biome config and some properties
                final BiomeConfig biomeConfig = this.worldSettings.biomes[this.biomeArray[(z + x * CHUNK_Z_SIZE)]].getBiomeConfig();
                final float currentTemperature = biomeConfig.biomeTemperature;
                final int surfaceBlocksNoise = (int) (this.noise4[(x + z * CHUNK_X_SIZE)] / 3.0D + 3.0D + this.random.nextDouble() * 0.25D);

                // Bedrock on the ceiling
                if (worldConfig.ceilingBedrock)
                {
                    // Moved one block lower to fix lighting issues
                    blocksArray[(z * 16 + x) * CHUNK_Y_SIZE + this.heightCap - 2] = (byte) worldConfig.bedrockBlock.getBlockId();
                }

                // Loop from map height to zero to place bedrock and surface
                // blocks
                int currentSurfaceBlock = biomeConfig.surfaceBlock.getBlockId();
                int currentGroundBlock = biomeConfig.groundBlock.getBlockId();
                int surfaceBlocksCount = -1;
                final int currentWaterLevel = this.waterLevel[z + x * 16];
                for (int y = CHUNK_Y_SIZE - 1; y >= 0; y--)
                {
                    final int currentPos = (z * CHUNK_Z_SIZE + x) * CHUNK_Y_SIZE + y;

                    if (y < 5 && (worldConfig.createAdminium(y)) && y <= this.random.nextInt(5))
                    {
                        // Place bottom bedrock
                        blocksArray[currentPos] = (byte) worldConfig.bedrockBlock.getBlockId();
                    } else
                    {
                        // Surface blocks logic (grass, dirt, sand, sandstone)
                        final int blockOnCurrentPos = blocksArray[currentPos] & 0xff;

                        if (blockOnCurrentPos == 0)
                        {
                            // Reset when air is found
                            surfaceBlocksCount = -1;
                        } else if (blockOnCurrentPos == biomeConfig.stoneBlock.getBlockId())
                        {
                            if (surfaceBlocksCount == -1)
                            {
                                // Set when variable was reset
                                if (surfaceBlocksNoise <= 0 && !worldConfig.removeSurfaceStone)
                                {
                                    currentSurfaceBlock = 0;
                                    currentGroundBlock = biomeConfig.stoneBlock.getBlockId();
                                } else if ((y >= currentWaterLevel - 4) && (y <= currentWaterLevel + 1))
                                {
                                    currentSurfaceBlock = biomeConfig.surfaceBlock.getBlockId();
                                    currentGroundBlock = biomeConfig.groundBlock.getBlockId();
                                }

                                // Use blocks for the top of the water instead
                                // when on water
                                if ((y < currentWaterLevel) && (y > worldConfig.waterLevelMin) && (currentSurfaceBlock == 0))
                                {
                                    if (currentTemperature < 0.15F)
                                    {
                                        currentSurfaceBlock = (byte) biomeConfig.iceBlock.getBlockId();
                                    } else
                                    {
                                        currentSurfaceBlock = (byte) biomeConfig.waterBlock.getBlockId();
                                    }
                                }

                                // Place surface block
                                surfaceBlocksCount = surfaceBlocksNoise;
                                if (y >= currentWaterLevel - 1)
                                {
                                    blocksArray[currentPos] = (byte) currentSurfaceBlock;
                                } else
                                {
                                    blocksArray[currentPos] = (byte) currentGroundBlock;
                                }

                            } else if (surfaceBlocksCount > 0)
                            {
                                // Place ground block
                                surfaceBlocksCount--;
                                blocksArray[currentPos] = (byte) currentGroundBlock;

                                // Place sandstone under stand
                                if ((surfaceBlocksCount == 0) && (currentGroundBlock == DefaultMaterial.SAND.id))
                                {
                                    surfaceBlocksCount = this.random.nextInt(4);
                                    currentGroundBlock = (byte) DefaultMaterial.SANDSTONE.id;
                                }
                            }
                        }
                    }
                }

                // Count how many water there is
                if (blocksArray[(z * CHUNK_X_SIZE + x) * CHUNK_Y_SIZE + biomeConfig.waterLevelMax] == biomeConfig.waterBlock.getBlockId())
                {
                    dryBlocksOnSurface--;
                }

                // End of code for each column
            }
        }

        return dryBlocksOnSurface > 250;
    }

    private void generateTerrainNoise(int xOffset, int yOffset, int zOffset, int maxYSections, int usedYSections)
    {
        if (this.rawTerrain == null || this.rawTerrain.length != NOISE_MAX_X * maxYSections * NOISE_MAX_Z)
        {
            this.rawTerrain = new double[NOISE_MAX_X * maxYSections * NOISE_MAX_Z];
        }

        WorldConfig worldConfig = this.worldSettings.worldConfig;

        final double xzScale = 684.41200000000003D * worldConfig.getFractureHorizontal();
        final double yScale = 684.41200000000003D * worldConfig.getFractureVertical();

        if (worldConfig.oldTerrainGenerator)
        {
            this.noise5 = this.noiseGen5.Noise2D(this.noise5, xOffset, zOffset, NOISE_MAX_X, NOISE_MAX_Z, 1.121D, 1.121D);
        }
        this.noise6 = this.noiseGen6.Noise2D(this.noise6, xOffset, zOffset, NOISE_MAX_X, NOISE_MAX_Z, 200.0D, 200.0D);

        this.noise3 = this.noiseGen3.Noise3D(this.noise3, xOffset, yOffset, zOffset, NOISE_MAX_X, maxYSections, NOISE_MAX_Z,
                xzScale / 80.0D, yScale / 160.0D, xzScale / 80.0D);
        this.noise1 = this.noiseGen1.Noise3D(this.noise1, xOffset, yOffset, zOffset, NOISE_MAX_X, maxYSections, NOISE_MAX_Z, xzScale,
                yScale, xzScale);
        this.noise2 = this.noiseGen2.Noise3D(this.noise2, xOffset, yOffset, zOffset, NOISE_MAX_X, maxYSections, NOISE_MAX_Z, xzScale,
                yScale, xzScale);

        int i3D = 0;
        int i2D = 0;

        for (int x = 0; x < NOISE_MAX_X; x++)
        {
            for (int z = 0; z < NOISE_MAX_Z; z++)
            {

                final int biomeId = this.biomeArray[(x + this.maxSmoothRadius + (z + this.maxSmoothRadius)
                        * (NOISE_MAX_X + this.maxSmoothDiameter))];
                final BiomeConfig biomeConfig = this.worldSettings.biomes[biomeId].getBiomeConfig();

                double noiseHeight = this.noise6[i2D] / 8000.0D;
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
                        this.biomeFactorWithRivers(x, z, usedYSections, noiseHeight);
                    else
                        this.biomeFactor(x, z, usedYSections, noiseHeight);
                } else
                    this.oldBiomeFactor(x, z, i2D, usedYSections, noiseHeight);

                i2D++;

                for (int y = 0; y < maxYSections; y++)
                {
                    double output;
                    double d8;

                    if (this.riverFound)
                    {
                        d8 = (this.riverHeight - y) * 12.0D * 128.0D / this.heightCap / this.riverVol;
                    } else
                    {
                        d8 = (this.heightFactor - y) * 12.0D * 128.0D / this.heightCap / this.volatilityFactor;
                    }

                    if (d8 > 0.0D)
                    {
                        d8 *= 4.0D;
                    }

                    final double vol1 = this.noise1[i3D] / 512.0D * biomeConfig.volatility1;
                    final double vol2 = this.noise2[i3D] / 512.0D * biomeConfig.volatility2;

                    final double noise = (this.noise3[i3D] / 10.0D + 1.0D) / 2.0D;
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

                        if (y > maxYSections - 4)
                        {
                            final double d12 = (y - (maxYSections - 4)) / 3.0F;
                            // Reduce last three layers
                            output = output * (1.0D - d12) + -10.0D * d12;
                        }

                    }
                    if (this.riverFound)
                    {
                        output += biomeConfig.riverHeightMatrix[y];
                    } else
                    {
                        output += biomeConfig.heightMatrix[y];
                    }

                    this.rawTerrain[i3D] = output;
                    i3D++;
                }
            }
        }
    }

    private void oldBiomeFactor(int x, int z, int i4, int ySections, double noiseHeight)
    {
        if (this.worldSettings.worldConfig.biomeMode == TerrainControl.getBiomeModeManager().OLD_GENERATOR)
        {
            this.volatilityFactor = (1.0D - this.localWorld.getBiomeFactorForOldBM(z * 48 + 17 + x * 3));

        } else
        {
            final BiomeConfig biomeConfig = toBiomeConfig(this.biomeArray[(x + this.maxSmoothRadius + (z + this.maxSmoothRadius)
                    * (NOISE_MAX_X + this.maxSmoothDiameter))]);
            this.volatilityFactor = (1.0D - Math.min(1, biomeConfig.biomeTemperature) * biomeConfig.biomeWetness);
        }
        this.volatilityFactor *= this.volatilityFactor;
        this.volatilityFactor = 1.0D - this.volatilityFactor * this.volatilityFactor;

        this.volatilityFactor = (this.noise3[i4] + 256.0D) / 512.0D * this.volatilityFactor;
        if (this.volatilityFactor > 1.0D)
        {
            this.volatilityFactor = 1.0D;
        }
        if (this.volatilityFactor < 0.0D || noiseHeight < 0.0D)
        {
            this.volatilityFactor = 0.0D;
        }

        this.volatilityFactor += 0.5D;
        this.heightFactor = ySections * (2.0D + noiseHeight) / 4.0D;
    }

    private void biomeFactor(int x, int z, int ySections, double noiseHeight)
    {
        float volatilitySum = 0.0F;
        double heightSum = 0.0F;
        float biomeWeightSum = 0.0F;

        final BiomeConfig centerBiomeConfig = toBiomeConfig(this.biomeArray[(x + this.maxSmoothRadius + (z + this.maxSmoothRadius)
                * (NOISE_MAX_X + this.maxSmoothDiameter))]);
        final int lookRadius = centerBiomeConfig.smoothRadius;

        float nextBiomeHeight, biomeWeight;

        for (int nextX = -lookRadius; nextX <= lookRadius; nextX++)
        {
            for (int nextZ = -lookRadius; nextZ <= lookRadius; nextZ++)
            {
                final BiomeConfig nextBiomeConfig = toBiomeConfig(this.biomeArray[(x + nextX + this.maxSmoothRadius + (z + nextZ + this.maxSmoothRadius)
                        * (NOISE_MAX_X + this.maxSmoothDiameter))]);

                nextBiomeHeight = nextBiomeConfig.biomeHeight;

                biomeWeight = this.nearBiomeWeightArray[(nextX + this.maxSmoothRadius + (nextZ + this.maxSmoothRadius)
                        * this.maxSmoothDiameter)]
                        / (nextBiomeHeight + 2.0F);
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

        this.waterLevelRaw[x * NOISE_MAX_X + z] = (byte) centerBiomeConfig.waterLevelMax;

        volatilitySum = volatilitySum * 0.9F + 0.1F;   // Must be != 0
        heightSum = (heightSum * 4.0F - 1.0F) / 8.0F;  // Silly magic numbers

        this.volatilityFactor = volatilitySum;
        this.heightFactor = ySections * (2.0D + heightSum + noiseHeight * 0.2D) / 4.0D;
    }

    private void biomeFactorWithRivers(int x, int z, int ySections, double noiseHeight)
    {
        float volatilitySum = 0.0F;
        float heightSum = 0.0F;
        float WeightSum = 0.0F;

        float riverVolatilitySum = 0.0F;
        float riverHeightSum = 0.0F;
        float riverWeightSum = 0.0F;

        final BiomeConfig biomeConfig = toBiomeConfig(this.biomeArray[(x + this.maxSmoothRadius + (z + this.maxSmoothRadius)
                * (NOISE_MAX_X + this.maxSmoothDiameter))]);

        final int lookRadius = biomeConfig.smoothRadius;

        this.riverFound = this.riverArray[(x + this.maxSmoothRadius + (z + this.maxSmoothRadius) * (NOISE_MAX_X + this.maxSmoothDiameter))] == 1;

        final float riverCenterHeight = this.riverFound ? biomeConfig.riverHeight : biomeConfig.biomeHeight;

        BiomeConfig nextBiomeConfig;
        float nextBiomeHeight, biomeWeight, nextRiverHeight, riverWeight;

        for (int nextX = -lookRadius; nextX <= lookRadius; nextX++)
        {
            for (int nextZ = -lookRadius; nextZ <= lookRadius; nextZ++)
            {

                nextBiomeConfig = toBiomeConfig(this.biomeArray[(x + nextX + this.maxSmoothRadius + (z + nextZ + this.maxSmoothRadius)
                        * (NOISE_MAX_X + this.maxSmoothDiameter))]);
                nextBiomeHeight = nextBiomeConfig.biomeHeight;
                biomeWeight = this.nearBiomeWeightArray[(nextX + this.maxSmoothRadius + (nextZ + this.maxSmoothRadius)
                        * this.maxSmoothDiameter)]
                        / (nextBiomeHeight + 2.0F);

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
                if (this.riverArray[(x + nextX + this.maxSmoothRadius + (z + nextZ + this.maxSmoothRadius)
                        * (NOISE_MAX_X + this.maxSmoothDiameter))] == 1)
                {
                    this.riverFound = true;
                    isRiver = true;
                }

                nextRiverHeight = (isRiver) ? nextBiomeConfig.riverHeight : nextBiomeHeight;
                riverWeight = this.nearBiomeWeightArray[(nextX + this.maxSmoothRadius + (nextZ + this.maxSmoothRadius)
                        * this.maxSmoothDiameter)]
                        / (nextRiverHeight + 2.0F);

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

        int waterLevelSum = this.riverFound ? biomeConfig.riverWaterLevel : biomeConfig.waterLevelMax;
        this.waterLevelRaw[x * NOISE_MAX_X + z] = (byte) waterLevelSum;

        volatilitySum = volatilitySum * 0.9F + 0.1F;   // Must be != 0
        heightSum = (heightSum * 4.0F - 1.0F) / 8.0F;  // Silly magic numbers

        this.volatilityFactor = volatilitySum;
        this.heightFactor = ySections * (2.0D + heightSum + noiseHeight * 0.2D) / 4.0D;

        riverVolatilitySum = riverVolatilitySum * 0.9F + 0.1F; // Must be != 0
        riverHeightSum = (riverHeightSum * 4.0F - 1.0F) / 8.0F;

        this.riverVol = riverVolatilitySum;
        this.riverHeight = ySections * (2.0D + riverHeightSum + noiseHeight * 0.2D) / 4.0D;
    }

    /**
     * Gets the BiomeConfig with the given id.
     * 
     * @param id The generation id of the biome.
     * @return The BiomeConfig.
     */
    private BiomeConfig toBiomeConfig(int id)
    {
        return this.worldSettings.biomes[id].getBiomeConfig();
    }

}
