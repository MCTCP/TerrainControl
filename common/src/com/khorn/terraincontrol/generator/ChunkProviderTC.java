package com.khorn.terraincontrol.generator;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.biomegenerators.OutputType;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.noise.NoiseGeneratorPerlinOctaves;
import com.khorn.terraincontrol.generator.terrainsgens.CanyonsGen;
import com.khorn.terraincontrol.generator.terrainsgens.CavesGen;
import com.khorn.terraincontrol.generator.terrainsgens.TerrainGenBase;
import com.khorn.terraincontrol.util.MathHelper;

import java.util.Random;

// Please don`t remove this. This disable warnings about x+0 arithmetic
// operations in my IDE. Khorn.
@SuppressWarnings("PointlessArithmeticExpression")
public class ChunkProviderTC
{
    private final Random random;
    private final NoiseGeneratorPerlinOctaves noiseGen1;
    private final NoiseGeneratorPerlinOctaves noiseGen2;
    private final NoiseGeneratorPerlinOctaves noiseGen3;
    private final NoiseGeneratorPerlinOctaves noiseGen4;
    private final NoiseGeneratorPerlinOctaves noiseGen5;
    private final NoiseGeneratorPerlinOctaves noiseGen6;
    private double[] rawTerrain;
    private double[] noise4 = new double[256];

    double[] noise3;
    double[] noise1;
    double[] noise2;
    double[] noise5;
    double[] noise6;
    float[] nearBiomeWeightArray;

    private static int chunkMaxX = 16;
    private static int chunkMaxZ = 16;

    private double riverVol;
    private double riverHeight;
    // Always false if improved rivers disabled
    private boolean riverFound = false;

    private final LocalWorld localWorld;
    private double volatilityFactor;
    private double heightFactor;

    private double[] heightValues;
    private double[] riverHeightValues;    

    private WorldConfig worldSettings;

    private final TerrainGenBase caveGen;
    private final TerrainGenBase canyonGen;

    private int[] biomeArray;
    private int[] riverArray;
    // Water level at lower resolution
    private final byte[] waterLevelRaw = new byte[25];
    // Water level for each column
    private final byte[] waterLevel = new byte[256];

    private final int height;
    private final int heightBits;
    private final int heightBitsPlusFour;
    private final int heightMinusOne;

    private final int maxSmoothDiameter;
    private final int maxSmoothRadius;

    public ChunkProviderTC(WorldConfig config, LocalWorld world)
    {
        this.worldSettings = config;
        this.localWorld = world;
        this.height = world.getHeight();
        this.heightBits = world.getHeightBits();
        this.heightBitsPlusFour = this.heightBits + 4;
        this.heightMinusOne = this.height - 1;

        this.random = new Random(world.getSeed());

        this.noiseGen1 = new NoiseGeneratorPerlinOctaves(this.random, 16);
        this.noiseGen2 = new NoiseGeneratorPerlinOctaves(this.random, 16);
        this.noiseGen3 = new NoiseGeneratorPerlinOctaves(this.random, 8);
        this.noiseGen4 = new NoiseGeneratorPerlinOctaves(this.random, 4);
        this.noiseGen5 = new NoiseGeneratorPerlinOctaves(this.random, 10);
        this.noiseGen6 = new NoiseGeneratorPerlinOctaves(this.random, 16);

        this.caveGen = new CavesGen(this.worldSettings, this.localWorld);
        this.canyonGen = new CanyonsGen(this.worldSettings, this.localWorld);

        // Contains 2d array maxSmoothDiameter*maxSmoothDiameter. Maximum weight is in array center.

        this.maxSmoothDiameter = config.maxSmoothRadius * 2 + 1;
        this.maxSmoothRadius = config.maxSmoothRadius;

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

    public byte[] generate(int x, int z)
    {

        this.random.setSeed(x * 341873128712L + z * 132897987541L);

        final byte[] blockArray = new byte[chunkMaxX * this.height * chunkMaxZ];

        generateTerrain(x, z, blockArray);

        final boolean dry = addBiomeBlocksAndCheckWater(x, z, blockArray);

        this.caveGen.a(x, z, blockArray);
        this.canyonGen.a(x, z, blockArray);

        if (this.worldSettings.ModeTerrain == WorldConfig.TerrainMode.Normal || this.worldSettings.ModeTerrain == WorldConfig.TerrainMode.OldGenerator)
        {
            this.localWorld.PrepareTerrainObjects(x, z, blockArray, dry);
        }

        if (this.worldSettings.isDeprecated)
        {
            this.worldSettings = this.worldSettings.newSettings;
        }

        return blockArray;

    }

    protected void generateTerrain(int chunkX, int chunkZ, byte[] blockArray)
    {
        final int four = 4;
        final int oneEightOfHeight = this.height / 8;

        final int noise_xSize = four + 1;
        final int noise_ySize = this.height / 8 + 1;
        final int noise_zSize = four + 1;

        if (worldSettings.improvedRivers)
            this.riverArray = this.localWorld.getBiomesUnZoomed(this.riverArray, chunkX * 4 - maxSmoothRadius, chunkZ * 4 - maxSmoothRadius, noise_xSize + maxSmoothDiameter, noise_zSize + maxSmoothDiameter, OutputType.ONLY_RIVERS);

        if (this.localWorld.canBiomeManagerGenerateUnzoomed())
        {
            this.biomeArray = this.localWorld.getBiomesUnZoomed(this.biomeArray, chunkX * 4 - maxSmoothRadius, chunkZ * 4 - maxSmoothRadius, noise_xSize + maxSmoothDiameter, noise_zSize + maxSmoothDiameter, OutputType.DEFAULT_FOR_WORLD);
        } else
        {
            this.biomeArray = this.localWorld.getBiomes(this.biomeArray, chunkX * 16, chunkZ * 16, 16, 16, OutputType.DEFAULT_FOR_WORLD);
        }

        this.rawTerrain = generateTerrainNoise(this.rawTerrain, chunkX * four, 0, chunkZ * four, noise_xSize, noise_ySize, noise_zSize);

        // Now that the raw terrain is generated, replace raw biome array with
        // fine-tuned one.
        this.biomeArray = this.localWorld.getBiomes(this.biomeArray, chunkX * 16, chunkZ * 16, chunkMaxX, chunkMaxZ, OutputType.DEFAULT_FOR_WORLD);

        final double oneEight = 0.125D;
        final int z_step = 1 << this.heightBits;
        final double oneFourth = 0.25D;

        for (int x = 0; x < four; x++)
        {
            for (int z = 0; z < four; z++)
            {
                // Water level (fill final array based on smaller, non-smoothed
                // array)
                double waterLevel_x0z0 = this.waterLevelRaw[(x + 0) * noise_xSize + (z + 0)];
                double waterLevel_x0z1 = this.waterLevelRaw[(x + 0) * noise_xSize + (z + 1)];
                final double waterLevel_x1z0 = (this.waterLevelRaw[(x + 1) * noise_xSize + (z + 0)] - waterLevel_x0z0) * oneFourth;
                final double waterLevel_x1z1 = (this.waterLevelRaw[(x + 1) * noise_xSize + (z + 1)] - waterLevel_x0z1) * oneFourth;

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

                    double x0z0 = this.rawTerrain[(((x + 0) * noise_zSize + (z + 0)) * noise_ySize + (y + 0))];
                    double x0z1 = this.rawTerrain[(((x + 0) * noise_zSize + (z + 1)) * noise_ySize + (y + 0))];
                    double x1z0 = this.rawTerrain[(((x + 1) * noise_zSize + (z + 0)) * noise_ySize + (y + 0))];
                    double x1z1 = this.rawTerrain[(((x + 1) * noise_zSize + (z + 1)) * noise_ySize + (y + 0))];

                    final double x0z0y1 = (this.rawTerrain[(((x + 0) * noise_zSize + (z + 0)) * noise_ySize + (y + 1))] - x0z0) * oneEight;
                    final double x0z1y1 = (this.rawTerrain[(((x + 0) * noise_zSize + (z + 1)) * noise_ySize + (y + 1))] - x0z1) * oneEight;
                    final double x1z0y1 = (this.rawTerrain[(((x + 1) * noise_zSize + (z + 0)) * noise_ySize + (y + 1))] - x1z0) * oneEight;
                    final double x1z1y1 = (this.rawTerrain[(((x + 1) * noise_zSize + (z + 1)) * noise_ySize + (y + 1))] - x1z1) * oneEight;

                    for (int piece_y = 0; piece_y < 8; piece_y++)
                    {

                        double d11 = x0z0;
                        double d12 = x0z1;
                        final double d13 = (x1z0 - x0z0) * oneFourth;
                        final double d14 = (x1z1 - x0z1) * oneFourth;

                        for (int piece_x = 0; piece_x < 4; piece_x++)
                        {
                            int position = (piece_x + x * 4) << this.heightBitsPlusFour | (0 + z * 4) << this.heightBits | (y * 8 + piece_y);

                            double d16 = d11;
                            final double d17 = (d12 - d11) * oneFourth;
                            for (int piece_z = 0; piece_z < 4; piece_z++)
                            {
                                final int biomeId = this.biomeArray[(z * 4 + piece_z) * 16 + (piece_x + x * 4)];
                                final int waterLevelMax = this.waterLevel[(z * 4 + piece_z) * 16 + (piece_x + x * 4)] & 0xFF;
                                int blockId = 0;
                                if (y * 8 + piece_y < waterLevelMax && y * 8 + piece_y > this.worldSettings.biomeConfigs[biomeId].waterLevelMin)
                                {
                                    blockId = this.worldSettings.biomeConfigs[biomeId].waterBlock;
                                }

                                if (d16 > 0.0D)
                                {
                                    blockId = this.worldSettings.biomeConfigs[biomeId].StoneBlock;
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
     * Adds the biome blocks like grass, dirt, sand and sandstone.
     * Also adds bedrock at the bottom of the map.
     *
     * @param chunkX      The chunk X.
     * @param chunkZ      The chunk Z.
     * @param blocksArray The blocks in the chunk. Will be modified.
     * @return Whether there is a lot of water in this chunk. If yes, no villages will be placed.
     */
    protected boolean addBiomeBlocksAndCheckWater(int chunkX, int chunkZ, byte[] blocksArray)
    {
        int dryBlocksOnSurface = 256;

        final double d1 = 0.03125D;
        this.noise4 = this.noiseGen4.Noise3D(this.noise4, chunkX * 16, chunkZ * 16, 0, 16, 16, 1, d1 * 2.0D, d1 * 2.0D, d1 * 2.0D);
        final float[] temperatureArray = this.localWorld.getTemperatures(chunkX * 16, chunkZ * 16, 16, 16);

        for (int x = 0; x < 16; x++)
        {
            for (int z = 0; z < 16; z++)
            {
                // The following code is executed for each column in the chunk
                final float currentTemperature = temperatureArray[(z + x * 16)];
                final int surfaceBlocksNoise = (int) (this.noise4[(x + z * 16)] / 3.0D + 3.0D + this.random.nextDouble() * 0.25D);

                // Get the current biome config
                final BiomeConfig biomeConfig = this.worldSettings.biomeConfigs[this.biomeArray[(z + x * 16)]];

                // Bedrock on the ceiling
                if (this.worldSettings.ceilingBedrock)
                {
                    // Moved one block lower to fix lighting issues
                    blocksArray[(z * 16 + x) * this.height + this.heightMinusOne - 1] = (byte) this.worldSettings.bedrockBlock;
                }

                // Loop from map height to zero to place bedrock and surface
                // blocks
                int currentSurfaceBlock = biomeConfig.SurfaceBlock;
                int currentGroundBlock = biomeConfig.GroundBlock;
                int surfaceBlocksCount = -1;
                final int currentWaterLevel = this.waterLevel[z + x * 16];
                for (int y = this.heightMinusOne; y >= 0; y--)
                {
                    final int currentPos = (z * 16 + x) * this.height + y;

                    if (y < 5 && (this.worldSettings.createAdminium(y)) && y <= this.random.nextInt(5))
                    {
                        // Place bottom bedrock
                        blocksArray[currentPos] = (byte) this.worldSettings.bedrockBlock;
                    } else
                    {
                        // Surface blocks logic (grass, dirt, sand, sandstone)
                        final int blockOnCurrentPos = blocksArray[currentPos] & 0xff;

                        if (blockOnCurrentPos == 0)
                        {
                            // Reset when air is found
                            surfaceBlocksCount = -1;
                        } else if (blockOnCurrentPos == biomeConfig.StoneBlock)
                        {
                            if (surfaceBlocksCount == -1)
                            {
                                // Set when variable was reset
                                if (surfaceBlocksNoise <= 0 && !this.worldSettings.removeSurfaceStone)
                                {
                                    currentSurfaceBlock = 0;
                                    currentGroundBlock = biomeConfig.StoneBlock;
                                } else if ((y >= currentWaterLevel - 4) && (y <= currentWaterLevel + 1))
                                {
                                    currentSurfaceBlock = biomeConfig.SurfaceBlock;
                                    currentGroundBlock = biomeConfig.GroundBlock;
                                }

                                // Use blocks for the top of the water instead
                                // when on water
                                if ((y < currentWaterLevel) && (y > this.worldSettings.waterLevelMin) && (currentSurfaceBlock == 0))
                                {
                                    if (currentTemperature < 0.15F)
                                    {
                                        currentSurfaceBlock = (byte) biomeConfig.iceBlock;
                                    } else
                                    {
                                        currentSurfaceBlock = (byte) biomeConfig.waterBlock;
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
                if (blocksArray[(z * 16 + x) * this.height + biomeConfig.waterLevelMax] == biomeConfig.waterBlock)
                {
                    dryBlocksOnSurface--;
                }

                // End of code for each column
            }
        }

        return dryBlocksOnSurface > 250;
    }

    private double[] generateTerrainNoise(double[] outArray, int xOffset, int yOffset, int zOffset, int max_X, int max_Y, int max_Z)
    {
        if (outArray == null)
        {
            outArray = new double[max_X * max_Y * max_Z];
        }

        final double xzScale = 684.41200000000003D * this.worldSettings.getFractureHorizontal();
        final double yScale = 684.41200000000003D * this.worldSettings.getFractureVertical();

        if (this.worldSettings.oldTerrainGenerator)
        {
            this.noise5 = this.noiseGen5.Noise2D(this.noise5, xOffset, zOffset, max_X, max_Z, 1.121D, 1.121D);
        }
        this.noise6 = this.noiseGen6.Noise2D(this.noise6, xOffset, zOffset, max_X, max_Z, 200.0D, 200.0D);

        this.noise3 = this.noiseGen3.Noise3D(this.noise3, xOffset, yOffset, zOffset, max_X, max_Y, max_Z, xzScale / 80.0D, yScale / 160.0D, xzScale / 80.0D);
        this.noise1 = this.noiseGen1.Noise3D(this.noise1, xOffset, yOffset, zOffset, max_X, max_Y, max_Z, xzScale, yScale, xzScale);
        this.noise2 = this.noiseGen2.Noise3D(this.noise2, xOffset, yOffset, zOffset, max_X, max_Y, max_Z, xzScale, yScale, xzScale);

        int i3D = 0;
        int i2D = 0;

        for (int x = 0; x < max_X; x++)
        {
            for (int z = 0; z < max_Z; z++)
            {

                final int biomeId = this.biomeArray[(x + this.maxSmoothRadius + (z + this.maxSmoothRadius) * (max_X + this.maxSmoothDiameter))];
                final BiomeConfig biomeConfig = this.worldSettings.biomeConfigs[biomeId];

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

                if (!this.worldSettings.oldTerrainGenerator)
                {
                    if (this.worldSettings.improvedRivers)
                        this.biomeFactorWithRivers(x, z, max_X, max_Y, noiseHeight);
                    else
                        this.biomeFactor(x, z, max_X, max_Y, noiseHeight);
                } else
                    this.oldBiomeFactor(x, z, i2D, max_X, max_Y, noiseHeight);

                i2D++;

                for (int y = 0; y < max_Y; y++)
                {
                    double output;

                    double d8;
                    if(this.riverFound)
                    	d8 = this.riverHeight - y;
                    else
                    	d8 = this.heightFactor - y;
                    
                    //if(d8 >= 0.0) {d8 = d8;}	//We are good
                    /*else*/if(d8 < -biomeConfig.ExtraBiomeHeight)
                    {
                    	d8 += biomeConfig.ExtraBiomeHeight;
                    }
                    else if(d8 < 0.0)
                    {//-ExtraBiomeHeight, d8, 0
                    	if(d8 * 2.0 < -biomeConfig.ExtraBiomeHeight)	//if (neg)d8 is outside of  
                    	{//-ExtraBiomeHeight, d8, -ExtraBiomeHeight/2, 0
                    		d8 += (d8 * 2.0) / biomeConfig.ExtraBiomeHeight * biomeConfig.ExtraHeightConstrictWaist;	//We
                    	}
                    	else
                    	{//-ExtraBiomeHeight, -ExtraBiomeHeight/2, d8, 0
                    		d8 += (2.0 - d8 * 2.0) / biomeConfig.ExtraBiomeHeight * biomeConfig.ExtraHeightConstrictWaist;	//We
                    	}
                    }

                    if (this.riverFound)
                    {
                        d8 = riverHeightValues[y] * 12.0D * 128.0D / this.height / this.riverVol;
                    } else
                    {
                        d8 = heightValues[y] * 12.0D * 128.0D / this.height / this.volatilityFactor;
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

                        if (y > max_Y - 4)
                        {
                            final double d12 = (y - (max_Y - 4)) / 3.0F;
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

                    outArray[i3D] = output;
                    i3D++;
                }
            }
        }
        return outArray;

    }

    private void oldBiomeFactor(int x, int z, int i4, int max_X, int max_Y, double noiseHeight)
    {
        if (this.worldSettings.biomeMode == TerrainControl.getBiomeModeManager().OLD_GENERATOR)
        {
            this.volatilityFactor = (1.0D - this.localWorld.getBiomeFactorForOldBM(z * 48 + 17 + x * 3));

        } else
        {
            final int biomeId = this.biomeArray[(x + this.maxSmoothRadius + (z + this.maxSmoothRadius) * (max_X + this.maxSmoothDiameter))];
            this.volatilityFactor = (1.0D - this.worldSettings.biomeConfigs[biomeId].BiomeTemperature * this.worldSettings.biomeConfigs[biomeId].BiomeWetness);
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
        this.heightFactor = max_Y * (2.0D + noiseHeight) / 4.0D;
    }

    private void biomeFactor(int x, int z, int max_X, int max_Y, double noiseHeight)
    {
        float volatilitySum = 0.0F;
        float heightSum = 0.0F;
        float biomeWeightSum = 0.0F;
        heightValues = new double[max_Y];
        
        double phi, alpha, beta;
        alpha = max_Y * 0.25;
        beta = 2 + 0.2*noiseHeight;
        
        for(int i = 0; i < max_Y; i++)
        {
        	heightValues[i] = 0.0;
        }


        final int biomeId = this.biomeArray[(x + this.maxSmoothRadius + (z + this.maxSmoothRadius) * (max_X + this.maxSmoothDiameter))];

        final int lookRadius = this.worldSettings.biomeConfigs[biomeId].SmoothRadius;

        BiomeConfig nextBiomeConfig;
        float nextBiomeHeight, biomeWeight;

        for (int nextX = -lookRadius; nextX <= lookRadius; nextX++)
        {
            for (int nextZ = -lookRadius; nextZ <= lookRadius; nextZ++)
            {
                final int nextBiomeId = this.biomeArray[(x + nextX + this.maxSmoothRadius + (z + nextZ + this.maxSmoothRadius) * (max_X + this.maxSmoothDiameter))];

                nextBiomeConfig = this.worldSettings.biomeConfigs[nextBiomeId];
                nextBiomeHeight = nextBiomeConfig.BiomeHeight;

                biomeWeight = this.nearBiomeWeightArray[(nextX + this.maxSmoothRadius + (nextZ + this.maxSmoothRadius) * this.maxSmoothDiameter)] / (nextBiomeHeight + 2.0F);
                biomeWeight = Math.abs(biomeWeight);
                if (nextBiomeHeight > this.worldSettings.biomeConfigs[biomeId].BiomeHeight)
                {
                    biomeWeight /= 2.0F;
                }
                volatilitySum += nextBiomeConfig.BiomeVolatility * biomeWeight;
                heightSum += nextBiomeHeight * biomeWeight;
                biomeWeightSum += biomeWeight;
                
                phi = (4 * nextBiomeHeight* biomeWeight + biomeWeight * (8 * beta - 1)) * alpha;
                double tempBiomeHeight = phi / (8 * biomeWeight);  
                
                for(int nextY = 0; nextY < max_Y; nextY++)
                {
                	if(nextY < tempBiomeHeight)
                	{
                		heightValues[nextY] += phi - 8 * biomeWeight * nextY;
                	}
                	else if(nextY > tempBiomeHeight + nextBiomeConfig.ExtraBiomeHeight)
                	{
                		heightValues[nextY] += phi + 8 * biomeWeight * (nextBiomeConfig.ExtraBiomeHeight - nextY);
                	}
                	else if(nextY < tempBiomeHeight + 0.5 * nextBiomeConfig.ExtraBiomeHeight)
                	{
                		heightValues[nextY] += 2 * nextBiomeConfig.ExtraHeightConstrictWaist / nextBiomeConfig.ExtraBiomeHeight * (phi - 8 * biomeWeight * nextY);
                	}
                	else
                	{
                		heightValues[nextY] += 2 * nextBiomeConfig.ExtraHeightConstrictWaist / nextBiomeConfig.ExtraBiomeHeight * ( 8 * biomeWeight * (nextY - nextBiomeConfig.ExtraBiomeHeight) - phi);
                	}
                }

            }
        }

        volatilitySum /= biomeWeightSum;
        heightSum /= biomeWeightSum;
        
        for(int i = 0; i < max_Y; i++)
        {
        	heightValues[i] /= 8 * biomeWeightSum;
        }

        this.waterLevelRaw[x * max_X + z] = (byte) this.worldSettings.biomeConfigs[biomeId].waterLevelMax;

        volatilitySum = volatilitySum * 0.9F + 0.1F;   // Must be != 0
        heightSum = (heightSum * 4.0F - 1.0F) / 8.0F;  // Silly magic numbers

        this.volatilityFactor = volatilitySum;
        this.heightFactor = max_Y * (2.0D + heightSum + noiseHeight * 0.2D) / 4.0D;

    }

    private void biomeFactorWithRivers(int x, int z, int max_X, int max_Y, double noiseHeight)
    {
        float volatilitySum = 0.0F;
        float heightSum = 0.0F;
        float WeightSum = 0.0F;

        float riverVolatilitySum = 0.0F;
        float riverHeightSum = 0.0F;
        float riverWeightSum = 0.0F;
        riverHeightValues = new double[max_Y];
        heightValues = new double[max_Y];
        
        double phi, alpha, beta;
        alpha = max_Y * 0.25;
        beta = 2 + 0.2*noiseHeight;
        
        for(int i = 0; i < max_Y; i++)
        {
        	riverHeightValues[i] = heightValues[i] = 0.0;
        }


        final int biomeId = this.biomeArray[(x + this.maxSmoothRadius + (z + this.maxSmoothRadius) * (max_X + this.maxSmoothDiameter))];

        final int lookRadius = this.worldSettings.biomeConfigs[biomeId].SmoothRadius;

        this.riverFound = this.riverArray[(x + this.maxSmoothRadius + (z + this.maxSmoothRadius) * (max_X + this.maxSmoothDiameter))] == 1;

        final float riverCenterHeight = this.riverFound ? this.worldSettings.biomeConfigs[biomeId].riverHeight : this.worldSettings.biomeConfigs[biomeId].BiomeHeight;

        BiomeConfig nextBiomeConfig;
        float nextBiomeHeight, biomeWeight, nextRiverHeight, riverWeight;

        for (int nextX = -lookRadius; nextX <= lookRadius; nextX++)
        {
            for (int nextZ = -lookRadius; nextZ <= lookRadius; nextZ++)
            {

                nextBiomeConfig = this.worldSettings.biomeConfigs[this.biomeArray[(x + nextX + this.maxSmoothRadius + (z + nextZ + this.maxSmoothRadius) * (max_X + this.maxSmoothDiameter))]];
                nextBiomeHeight = nextBiomeConfig.BiomeHeight;
                biomeWeight = this.nearBiomeWeightArray[(nextX + this.maxSmoothRadius + (nextZ + this.maxSmoothRadius) * this.maxSmoothDiameter)] / (nextBiomeHeight + 2.0F);

                biomeWeight = Math.abs(biomeWeight);
                if (nextBiomeHeight > this.worldSettings.biomeConfigs[biomeId].BiomeHeight)
                {
                    biomeWeight /= 2.0F;
                }
                volatilitySum += nextBiomeConfig.BiomeVolatility * biomeWeight;
                heightSum += nextBiomeHeight * biomeWeight;
                WeightSum += biomeWeight;
                
                phi = (4 * nextBiomeHeight* biomeWeight + biomeWeight * (8 * beta - 1)) * alpha;
                double tempBiomeHeight = phi / (8 * biomeWeight);  
                
                for(int nextY = 0; nextY < max_Y; nextY++)
                {
                	if(nextY < tempBiomeHeight)
                	{
                		heightValues[nextY] += phi - 8 * biomeWeight * nextY;
                	}
                	else if(nextY > tempBiomeHeight + nextBiomeConfig.ExtraBiomeHeight)
                	{
                		heightValues[nextY] += phi + 8 * biomeWeight * (nextBiomeConfig.ExtraBiomeHeight - nextY);
                	}
                	else if(nextY < tempBiomeHeight + 0.5 * nextBiomeConfig.ExtraBiomeHeight)
                	{
                		heightValues[nextY] += 2 * nextBiomeConfig.ExtraHeightConstrictWaist / nextBiomeConfig.ExtraBiomeHeight * (phi - 8 * biomeWeight * nextY);
                	}
                	else
                	{
                		heightValues[nextY] += 2 * nextBiomeConfig.ExtraHeightConstrictWaist / nextBiomeConfig.ExtraBiomeHeight * ( 8 * biomeWeight * (nextY - nextBiomeConfig.ExtraBiomeHeight) - phi);
                	}
                }

                // River part

                boolean isRiver = false;
                if (this.riverArray[(x + nextX + this.maxSmoothRadius + (z + nextZ + this.maxSmoothRadius) * (max_X + this.maxSmoothDiameter))] == 1)
                {
                    this.riverFound = true;
                    isRiver = true;
                }

                nextRiverHeight = (isRiver) ? nextBiomeConfig.riverHeight : nextBiomeHeight;
                riverWeight = this.nearBiomeWeightArray[(nextX + this.maxSmoothRadius + (nextZ + this.maxSmoothRadius) * this.maxSmoothDiameter)] / (nextRiverHeight + 2.0F);

                riverWeight = Math.abs(riverWeight);
                if (nextRiverHeight > riverCenterHeight)
                {
                    nextRiverHeight = riverCenterHeight;
                }
                riverVolatilitySum += (isRiver ? nextBiomeConfig.riverVolatility : nextBiomeConfig.BiomeVolatility) * riverWeight;
                riverHeightSum += nextRiverHeight * riverWeight;
                riverWeightSum += riverWeight;
                
                phi = (4 * nextRiverHeight* riverWeight + riverWeight * (8 * beta - 1)) * alpha;
                tempBiomeHeight = phi / (8 * riverWeight);  
                
                for(int nextY = 0; nextY < max_Y; nextY++)
                {
                	if(nextY < tempBiomeHeight)
                	{
                		riverHeightValues[nextY] += phi - 8 * riverWeight * nextY;
                	}
                	else if(nextY > tempBiomeHeight + nextBiomeConfig.ExtraBiomeHeight)
                	{
                		riverHeightValues[nextY] += phi + 8 * riverWeight * (nextBiomeConfig.ExtraBiomeHeight - nextY);
                	}
                	else if(nextY < tempBiomeHeight + 0.5 * nextBiomeConfig.ExtraBiomeHeight)
                	{
                		riverHeightValues[nextY] += 2 * nextBiomeConfig.ExtraHeightConstrictWaist / nextBiomeConfig.ExtraBiomeHeight * (phi - 8 * riverWeight * nextY);
                	}
                	else
                	{
                		riverHeightValues[nextY] += 2 * nextBiomeConfig.ExtraHeightConstrictWaist / nextBiomeConfig.ExtraBiomeHeight * ( 8 * riverWeight * (nextY - nextBiomeConfig.ExtraBiomeHeight) - phi);
                	}
                }
            }
        }

        volatilitySum /= WeightSum;
        heightSum /= WeightSum;

        riverVolatilitySum /= riverWeightSum;
        riverHeightSum /= riverWeightSum;
        
        for(int i = 0; i < max_Y; i++)
        {
        	riverHeightValues[i] /= 8 * riverWeightSum;
        	heightValues[i] /= 8 * WeightSum;
        }

        float waterLevelSum = this.riverFound ? this.worldSettings.biomeConfigs[biomeId].riverWaterLevel : this.worldSettings.biomeConfigs[biomeId].waterLevelMax;
        this.waterLevelRaw[x * max_X + z] = (byte) waterLevelSum;

        volatilitySum = volatilitySum * 0.9F + 0.1F;   // Must be != 0
        heightSum = (heightSum * 4.0F - 1.0F) / 8.0F;  // Silly magic numbers

        this.volatilityFactor = volatilitySum;
        this.heightFactor = max_Y * (2.0D + heightSum + noiseHeight * 0.2D) / 4.0D;

        riverVolatilitySum = riverVolatilitySum * 0.9F + 0.1F; // Must be != 0
        riverHeightSum = (riverHeightSum * 4.0F - 1.0F) / 8.0F; // Silly magic
        // numbers

        this.riverVol = riverVolatilitySum;
        this.riverHeight = max_Y * (2.0D + riverHeightSum + noiseHeight * 0.2D) / 4.0D;

    }

}
