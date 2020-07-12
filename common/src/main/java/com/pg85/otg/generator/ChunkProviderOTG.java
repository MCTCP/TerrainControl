package com.pg85.otg.generator;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.generator.biome.BiomeGenerator;
import com.pg85.otg.generator.biome.OutputType;
import com.pg85.otg.generator.noise.NoiseGeneratorPerlinMesaBlocks;
import com.pg85.otg.generator.noise.NoiseGeneratorPerlinOctaves;
import com.pg85.otg.generator.terrain.CavesGen;
import com.pg85.otg.generator.terrain.RavinesGen;
import com.pg85.otg.generator.terrain.TerrainGenBase;
import com.pg85.otg.network.ConfigProvider;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.materials.MaterialHelper;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import static com.pg85.otg.util.ChunkCoordinate.CHUNK_X_SIZE;
import static com.pg85.otg.util.ChunkCoordinate.CHUNK_Z_SIZE;

import java.util.Random;

public class ChunkProviderOTG
{
    // Several constants describing the chunk size of Minecraft
    private static final int NOISE_MAX_X = CHUNK_X_SIZE / 4 + 1;
    private static final int NOISE_MAX_Z = CHUNK_Z_SIZE / 4 + 1;

    private final LocalMaterialData air = MaterialHelper.toLocalMaterialData(DefaultMaterial.AIR, 0);

    private final Random random;
    private final NoiseGeneratorPerlinOctaves vol1NoiseGen;
    private final NoiseGeneratorPerlinOctaves vol2NoiseGen;
    private final NoiseGeneratorPerlinOctaves volNoiseGen;
    private final NoiseGeneratorPerlinMesaBlocks biomeBlocksNoiseGen;
    private final NoiseGeneratorPerlinOctaves oldTerrainGeneratorNoiseGen;
    private final NoiseGeneratorPerlinOctaves noiseHeightNoiseGen;

    private double[] biomeBlocksNoise = new double[CHUNK_X_SIZE * CHUNK_Z_SIZE];

    private double[] volNoise;
    private double[] vol1Noise;
    private double[] vol2Noise;
    private double[] oldTerrainGeneratorNoise;
    private double[] noiseHeightNoise;
    private float[] nearBiomeWeightArray;

    private double riverVol;
    private double riverHeight;
    // Always false if improved rivers disabled
    private boolean riverFound = false;

    private final LocalWorld localWorld;
    private double volatilityFactor;
    private double heightFactor;

    private final ConfigProvider configProvider;

    private final TerrainGenBase caveGen;
    private final TerrainGenBase canyonGen;

    // Water level at lower resolution
    private final byte[] waterLevelRaw = new byte[25];

    private final int heightScale;
    private final int heightCap;

    private final int maxSmoothDiameter;
    private final int maxSmoothRadius;

    // TODO: Should this really be limited to 1024?
    private BiomeConfig[] biomes = new BiomeConfig[1024];
    
    public ChunkProviderOTG(ConfigProvider configs, LocalWorld world)
    {    	
        this.configProvider = configs;
        this.localWorld = world;
        this.heightCap = world.getHeightCap();
        this.heightScale = world.getHeightScale();

        this.random = new Random(world.getSeed());

        this.vol1NoiseGen = new NoiseGeneratorPerlinOctaves(this.random, 16);
        this.vol2NoiseGen = new NoiseGeneratorPerlinOctaves(this.random, 16);
        this.volNoiseGen = new NoiseGeneratorPerlinOctaves(this.random, 8);
        this.biomeBlocksNoiseGen = new NoiseGeneratorPerlinMesaBlocks(this.random, 4);
        this.oldTerrainGeneratorNoiseGen = new NoiseGeneratorPerlinOctaves(this.random, 10);
        this.noiseHeightNoiseGen = new NoiseGeneratorPerlinOctaves(this.random, 16);

        this.caveGen = new CavesGen(configs.getWorldConfig(), this.localWorld);
        this.canyonGen = new RavinesGen(configs.getWorldConfig(), this.localWorld);

        WorldConfig worldConfig = configs.getWorldConfig();

        // Contains 2d array maxSmoothDiameter*maxSmoothDiameter.
        // Maximum weight is in array center.

        this.maxSmoothDiameter = worldConfig.maxSmoothRadius * 2 + 1;
        this.maxSmoothRadius = worldConfig.maxSmoothRadius;

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
    
    public void generate(ChunkBuffer chunkBuffer)
    {
        ChunkCoordinate chunkCoord = chunkBuffer.getChunkCoordinate();
        int x = chunkCoord.getChunkX();
        int z = chunkCoord.getChunkZ();
        this.random.setSeed(x * 341873128712L + z * 132897987541L);
              
        boolean dry = generateTerrainA(x, z, chunkBuffer);
                
        if(!this.localWorld.generateModdedCaveGen(x, z, chunkBuffer))
        {
            this.caveGen.generate(chunkBuffer);        	
        }
        this.canyonGen.generate(chunkBuffer);

        WorldConfig worldConfig = configProvider.getWorldConfig();
        if (worldConfig.modeTerrain == WorldConfig.TerrainMode.Normal)// || worldConfig.modeTerrain == WorldConfig.TerrainMode.OldGenerator)
        {
            this.localWorld.prepareDefaultStructures(x, z, dry);
        }
    }

    // Used by Streams
    private void generateTerrain(ChunkBuffer chunkBuffer)
    {
    	
    }
    
    // Renamed this to generateTerrainA for v8, since Streams injects code into generateTerrain for v6
    // to make it fire the ReplaceBiomeBlocks event. V8 fires the event itself, so just to make sure 
    // Streams doesnt crash when trying to inject code, have an empty generateTerrain method.
    private boolean generateTerrainA(int x, int z, ChunkBuffer chunkBuffer)
    {
        int[] biomeArray = null;
        byte[] waterLevel = new byte[CHUNK_X_SIZE * CHUNK_Z_SIZE];;
    	
        ChunkCoordinate chunkCoord = chunkBuffer.getChunkCoordinate();
        int chunkX = chunkCoord.getChunkX();
        int chunkZ = chunkCoord.getChunkZ();

        final int four = 4;
        final int oneEightOfHeight = this.heightCap / 8;

        final int maxYSections = this.heightCap / 8 + 1;
        final int usedYSections = this.heightScale / 8 + 1;

        WorldConfig worldConfig = configProvider.getWorldConfig();
        BiomeGenerator biomeGenerator = this.localWorld.getBiomeGenerator();
        int[] riverArray = null;
        if (worldConfig.improvedRivers)
        {
            riverArray = biomeGenerator.getBiomesUnZoomed(riverArray, chunkX * 4 - maxSmoothRadius, chunkZ * 4 - maxSmoothRadius, NOISE_MAX_X + maxSmoothDiameter, NOISE_MAX_Z + maxSmoothDiameter, OutputType.ONLY_RIVERS);
        }
        
        if (biomeGenerator.canGenerateUnZoomed())
        {
            biomeArray = biomeGenerator.getBiomesUnZoomed(biomeArray, chunkX * 4 - maxSmoothRadius, chunkZ * 4 - maxSmoothRadius, NOISE_MAX_X + maxSmoothDiameter, NOISE_MAX_Z + maxSmoothDiameter, OutputType.DEFAULT_FOR_WORLD);
        } else {
            biomeArray = biomeGenerator.getBiomes(biomeArray, chunkX * CHUNK_X_SIZE, chunkZ * CHUNK_Z_SIZE, CHUNK_X_SIZE, CHUNK_Z_SIZE, OutputType.DEFAULT_FOR_WORLD);
        }

        double[] rawTerrain = generateTerrainNoise(chunkX * four, 0, chunkZ * four, maxYSections, usedYSections, biomeArray, riverArray);

        // Now that the raw terrain is generated, replace raw biome array with
        // fine-tuned one.
        if (biomeGenerator.canGenerateUnZoomed())
        {
            biomeArray = biomeGenerator.getBiomes(biomeArray, chunkX * CHUNK_X_SIZE, chunkZ * CHUNK_Z_SIZE, CHUNK_X_SIZE, CHUNK_Z_SIZE, OutputType.DEFAULT_FOR_WORLD);
        }

        final double oneEight = 0.125D;
        final double oneFourth = 0.25D;

        for (int x1 = 0; x1 < four; x1++)
        {
            for (int z1 = 0; z1 < four; z1++)
            {
                // Water level (fill final array based on smaller,
                // non-smoothed
                // array)
                double waterLevel_x0z0 = this.waterLevelRaw[(x1 + 0) * NOISE_MAX_X + (z1 + 0)] & 0xFF;
                double waterLevel_x0z1 = this.waterLevelRaw[(x1 + 0) * NOISE_MAX_X + (z1 + 1)] & 0xFF;
                final double waterLevel_x1z0 = ((this.waterLevelRaw[(x1 + 1) * NOISE_MAX_X + (z1 + 0)] & 0xFF) - waterLevel_x0z0) * oneFourth;
                final double waterLevel_x1z1 = ((this.waterLevelRaw[(x1 + 1) * NOISE_MAX_X + (z1 + 1)] & 0xFF) - waterLevel_x0z1) * oneFourth;

                for (int piece_x = 0; piece_x < 4; piece_x++)
                {
                    double waterLevelForArray = waterLevel_x0z0;
                    final double d17_1 = (waterLevel_x0z1 - waterLevel_x0z0) * oneFourth;

                    for (int piece_z = 0; piece_z < 4; piece_z++)
                    {
                        // Fill water level array
                        waterLevel[(z1 * 4 + piece_z) * 16 + (piece_x + x1 * 4)] = (byte) waterLevelForArray;
                        waterLevelForArray += d17_1;
                    }
                    waterLevel_x0z0 += waterLevel_x1z0;
                    waterLevel_x0z1 += waterLevel_x1z1;

                }

                // Terrain noise
                for (int y = 0; y < oneEightOfHeight; y++)
                {
                    double x0z0 = rawTerrain[(((x1 + 0) * NOISE_MAX_Z + (z1 + 0)) * maxYSections + (y + 0))];
                    double x0z1 = rawTerrain[(((x1 + 0) * NOISE_MAX_Z + (z1 + 1)) * maxYSections + (y + 0))];
                    double x1z0 = rawTerrain[(((x1 + 1) * NOISE_MAX_Z + (z1 + 0)) * maxYSections + (y + 0))];
                    double x1z1 = rawTerrain[(((x1 + 1) * NOISE_MAX_Z + (z1 + 1)) * maxYSections + (y + 0))];

                    final double x0z0y1 = (rawTerrain[(((x1 + 0) * NOISE_MAX_Z + (z1 + 0)) * maxYSections + (y + 1))] - x0z0) * oneEight;
                    final double x0z1y1 = (rawTerrain[(((x1 + 0) * NOISE_MAX_Z + (z1 + 1)) * maxYSections + (y + 1))] - x0z1) * oneEight;
                    final double x1z0y1 = (rawTerrain[(((x1 + 1) * NOISE_MAX_Z + (z1 + 0)) * maxYSections + (y + 1))] - x1z0) * oneEight;
                    final double x1z1y1 = (rawTerrain[(((x1 + 1) * NOISE_MAX_Z + (z1 + 1)) * maxYSections + (y + 1))] - x1z1) * oneEight;

                    for (int piece_y = 0; piece_y < 8; piece_y++)
                    {
                        double d11 = x0z0;
                        double d12 = x0z1;
                        final double d13 = (x1z0 - x0z0) * oneFourth;
                        final double d14 = (x1z1 - x0z1) * oneFourth;

                        for (int piece_x = 0; piece_x < 4; piece_x++)
                        {
                            double d16 = d11;
                            final double d17 = (d12 - d11) * oneFourth;
                            for (int piece_z = 0; piece_z < 4; piece_z++)
                            {
                                final BiomeConfig biomeConfig = toBiomeConfig(biomeArray[(z1 * 4 + piece_z) * 16 + (piece_x + x1 * 4)]);
                                final int waterLevelMax = waterLevel[(z1 * 4 + piece_z) * 16 + (piece_x + x1 * 4)] & 0xFF;
                                LocalMaterialData block = air;
                                if (y * 8 + piece_y < waterLevelMax && y * 8 + piece_y > biomeConfig.waterLevelMin)
                                {
                                    block = biomeConfig.waterBlock;
                                }

                                if (d16 > 0.0D)
                                {
                                    block = biomeConfig.stoneBlock;
                                }

                                chunkBuffer.setBlock(piece_x + x1 * 4, y * 8 + piece_y, z1 * 4 + piece_z, block);
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
        
        boolean dry = false;
        if(OTG.fireReplaceBiomeBlocksEvent(x, z, chunkBuffer, localWorld))
		{
        	dry = addBiomeBlocksAndCheckWater(chunkBuffer, biomeArray, waterLevel);
		}
        return dry;
    }

    /**
     * Adds the biome blocks like grass, dirt, sand and sandstone. Also adds
     * bedrock at the bottom of the map.
     *
     * @param chunkBuffer The the chunk to add the blocks to.
     * @return Whether there is a lot of water in this chunk. If yes, no
     *         villages will be placed.
     */
    private boolean addBiomeBlocksAndCheckWater(ChunkBuffer chunkBuffer, int[] biomeArray, byte[] waterLevel)
    {
        ChunkCoordinate chunkCoord = chunkBuffer.getChunkCoordinate();

        int dryBlocksOnSurface = 256;

        final double d1 = 0.03125D;
        this.biomeBlocksNoise = this.biomeBlocksNoiseGen.getRegion(this.biomeBlocksNoise, chunkCoord.getBlockX(), chunkCoord.getBlockZ(), CHUNK_X_SIZE, CHUNK_Z_SIZE, d1 * 2.0D, d1 * 2.0D, 1.0D);

        GeneratingChunk generatingChunk = new GeneratingChunk(this.random, waterLevel, this.biomeBlocksNoise, this.heightCap);

        for (int x = 0; x < CHUNK_X_SIZE; x++)
        {
            for (int z = 0; z < CHUNK_Z_SIZE; z++)
            {
                // The following code is executed for each column in the chunk

                // Get the current biome config and some properties
                final BiomeConfig biomeConfig = toBiomeConfig(biomeArray[(x + z * CHUNK_X_SIZE)]);

                biomeConfig.surfaceAndGroundControl.spawn(localWorld, generatingChunk, chunkBuffer, biomeConfig, chunkCoord.getBlockX() + x, chunkCoord.getBlockZ() + z);

                // Count water blocks
                if (chunkBuffer.getBlock(x, biomeConfig.waterLevelMax, z).equals(biomeConfig.waterBlock))
                {
                    dryBlocksOnSurface--;
                }

                // End of code for each column
            }
        }

        return dryBlocksOnSurface > 250;
    }

    private double[] generateTerrainNoise(int xOffset, int yOffset, int zOffset, int maxYSections, int usedYSections, int[] biomeArray, int[] riverArray)
    {
        double[] rawTerrain = new double[NOISE_MAX_X * maxYSections * NOISE_MAX_Z];

        WorldConfig worldConfig = configProvider.getWorldConfig();
        final double xzScale = 684.41200000000003D * worldConfig.getFractureHorizontal();
        final double yScale = 684.41200000000003D * worldConfig.getFractureVertical();

        if (worldConfig.oldTerrainGenerator)
        {
            this.oldTerrainGeneratorNoise = this.oldTerrainGeneratorNoiseGen.Noise2D(this.oldTerrainGeneratorNoise, xOffset, zOffset, NOISE_MAX_X, NOISE_MAX_Z, 1.121D, 1.121D);
        }
        this.noiseHeightNoise = this.noiseHeightNoiseGen.Noise2D(this.noiseHeightNoise, xOffset, zOffset, NOISE_MAX_X, NOISE_MAX_Z, 200.0D, 200.0D);

        this.volNoise = this.volNoiseGen.Noise3D(this.volNoise, xOffset, yOffset, zOffset, NOISE_MAX_X, maxYSections, NOISE_MAX_Z, xzScale / 80.0D, yScale / 160.0D, xzScale / 80.0D);
        this.vol1Noise = this.vol1NoiseGen.Noise3D(this.vol1Noise, xOffset, yOffset, zOffset, NOISE_MAX_X, maxYSections, NOISE_MAX_Z, xzScale, yScale, xzScale);
        this.vol2Noise = this.vol2NoiseGen.Noise3D(this.vol2Noise, xOffset, yOffset, zOffset, NOISE_MAX_X, maxYSections, NOISE_MAX_Z, xzScale, yScale, xzScale);

        int i3D = 0;
        int i2D = 0;

        for (int x = 0; x < NOISE_MAX_X; x++)
        {
            for (int z = 0; z < NOISE_MAX_Z; z++)
            {
                final int biomeId = biomeArray[(x + this.maxSmoothRadius + (z + this.maxSmoothRadius) * (NOISE_MAX_X + this.maxSmoothDiameter))];

                BiomeConfig biomeConfig = toBiomeConfig(biomeId);

                // Data used for noise generation
                double volatility1 = biomeConfig.volatility1;
                double volatility2 = biomeConfig.volatility2;
                double volatilityWeight1 = biomeConfig.volatilityWeight1;
                double volatilityWeight2 = biomeConfig.volatilityWeight2;
                double maxAverageDepth = biomeConfig.maxAverageDepth;
                double maxAverageHeight = biomeConfig.maxAverageHeight;

                if (worldConfig.improvedSmoothing)
                {
                    double[] data = smoothRemainingData(x, z, biomeArray);
                    volatility1 = data[0];
                    volatility2 = data[1];
                    volatilityWeight1 = data[2];
                    volatilityWeight2 = data[3];
                    maxAverageDepth = data[4];
                    maxAverageHeight = data[5];
                }

                double noiseHeight = this.noiseHeightNoise[i2D] / 8000.0D;
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
                    noiseHeight -= maxAverageDepth;
                    noiseHeight /= 1.4D;
                    noiseHeight /= 2.0D;
                }
                else
                {
                    if (noiseHeight > 1.0D)
                    {
                        noiseHeight = 1.0D;
                    }
                    noiseHeight += maxAverageHeight;
                    noiseHeight /= 8.0D;
                }

                // Compute biome height and volatility for this column
                if (!worldConfig.oldTerrainGenerator)
                {
                    if (worldConfig.improvedRivers)
                    {
                        this.biomeFactorWithRivers(x, z, usedYSections, noiseHeight, biomeArray, riverArray);
                    }
                    else
                    {
                        this.biomeFactor(x, z, usedYSections, noiseHeight, biomeArray);
                    }
                }
                else
                {
                    this.oldBiomeFactor(x, z, i2D, usedYSections, noiseHeight, biomeArray);
            	}

                i2D++;

                // Initialize both CHC for this column
                double[] CHC = new double[biomeConfig.heightMatrix.length];
                double[] riverCHC = new double[biomeConfig.riverHeightMatrix.length];

                // If CHC smoothing is enabled, smooth based on the biome at this column's smoothing radius
                if (worldConfig.customHeightControlSmoothing)
                {
                    // This function modifies the arrays directly, no need to set
                    smoothCHC(CHC, riverCHC, x, z, maxYSections, biomeArray);
                }
                else {
                    // If smoothing is not enabled, revert to the original CHC data
                    CHC = biomeConfig.heightMatrix;
                    riverCHC = biomeConfig.riverHeightMatrix;
                }

                // Compute and store the noise value for each section
                for (int y = 0; y < maxYSections; y++)
                {
                    double output;
                    double columnHeight;

                    // If there's a river here, use the river height and volatility instead
                    if (this.riverFound)
                    {
                        columnHeight = (this.riverHeight - y) * 12.0D * 128.0D / this.heightCap / this.riverVol;
                    } else
                    {
                        columnHeight = (this.heightFactor - y) * 12.0D * 128.0D / this.heightCap / this.volatilityFactor;
                    }

                    // Multiply the height value of this column by 4 if it's above 0
                    if (columnHeight > 0.0D)
                    {
                        columnHeight *= 4.0D;
                    }

                    final double vol1 = this.vol1Noise[i3D] / 512.0D * volatility1;
                    final double vol2 = this.vol2Noise[i3D] / 512.0D * volatility2;

                    final double noise = (this.volNoise[i3D] / 10.0D + 1.0D) / 2.0D;
                    if (noise < volatilityWeight1)
                    {
                        output = vol1;
                    }
                    else if (noise > volatilityWeight2)
                    {
                        output = vol2;
                    } else
                    {
                        output = vol1 + (vol2 - vol1) * noise;
                    }

                    if (!biomeConfig.disableNotchHeightControl)
                    {
                        output += columnHeight;

                        if (y > maxYSections - 4)
                        {
                            final double d12 = (y - (maxYSections - 4)) / 3.0F;
                            // Reduce last three layers
                            output = output * (1.0D - d12) + -10.0D * d12;
                        }
                    }
                    if (this.riverFound)
                    {
                    	output += riverCHC[Math.min(biomeConfig.riverHeightMatrix.length - 1, y)];
                    } else
                    {
                    	output += CHC[Math.min(biomeConfig.heightMatrix.length - 1, y)];
                    }

                    rawTerrain[i3D] = output;
                    i3D++;
                }
            }
        }

        return rawTerrain;
    }

    private void oldBiomeFactor(int x, int z, int i4, int ySections, double noiseHeight, int[] biomeArray)
    {
        final BiomeConfig biomeConfig = toBiomeConfig(biomeArray[(x + this.maxSmoothRadius + (z + this.maxSmoothRadius) * (NOISE_MAX_X + this.maxSmoothDiameter))]);
        this.volatilityFactor = (1.0D - Math.min(1, biomeConfig.biomeTemperature) * biomeConfig.biomeWetness);

        this.volatilityFactor *= this.volatilityFactor;
        this.volatilityFactor = 1.0D - this.volatilityFactor * this.volatilityFactor;

        this.volatilityFactor = (this.volNoise[i4] + 256.0D) / 512.0D * this.volatilityFactor;
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

    private void biomeFactor(int x, int z, int ySections, double noiseHeight, int[] biomeArray)
    {
        float volatilitySum = 0.0F;
        double heightSum = 0.0F;
        float biomeWeightSum = 0.0F;

        final BiomeConfig centerBiomeConfig = toBiomeConfig(biomeArray[(x + this.maxSmoothRadius + (z + this.maxSmoothRadius) * (NOISE_MAX_X + this.maxSmoothDiameter))]);
        final int lookRadius = centerBiomeConfig.smoothRadius;

        float nextBiomeHeight, biomeWeight;

        for (int nextX = -lookRadius; nextX <= lookRadius; nextX++)
        {
            for (int nextZ = -lookRadius; nextZ <= lookRadius; nextZ++)
            {
                final BiomeConfig nextBiomeConfig = toBiomeConfig(biomeArray[(x + nextX + this.maxSmoothRadius + (z + nextZ + this.maxSmoothRadius) * (NOISE_MAX_X + this.maxSmoothDiameter))]);

                nextBiomeHeight = nextBiomeConfig.biomeHeight;

                // TODO: Potential divide by zero, not sure what the outcome or the proper solution would be. Uses floats so won't necessarily cause exceptions.
                biomeWeight = this.nearBiomeWeightArray[(nextX + this.maxSmoothRadius + (nextZ + this.maxSmoothRadius) * this.maxSmoothDiameter)] / (nextBiomeHeight + 2.0F);
                biomeWeight = Math.abs(biomeWeight);
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

    private void biomeFactorWithRivers(int x, int z, int ySections, double noiseHeight, int[] biomeArray, int[] riverArray)
    {
        float volatilitySum = 0.0F;
        float heightSum = 0.0F;
        float WeightSum = 0.0F;

        float riverVolatilitySum = 0.0F;
        float riverHeightSum = 0.0F;
        float riverWeightSum = 0.0F;

        final BiomeConfig biomeConfig = toBiomeConfig(biomeArray[(x + this.maxSmoothRadius + (z + this.maxSmoothRadius) * (NOISE_MAX_X + this.maxSmoothDiameter))]);

        final int lookRadius = biomeConfig.smoothRadius;

        this.riverFound = riverArray[(x + this.maxSmoothRadius + (z + this.maxSmoothRadius) * (NOISE_MAX_X + this.maxSmoothDiameter))] == 1;

        final float riverCenterHeight = this.riverFound ? biomeConfig.riverHeight : biomeConfig.biomeHeight;

        BiomeConfig nextBiomeConfig;
        float nextBiomeHeight, biomeWeight, nextRiverHeight, riverWeight;

        for (int nextX = -lookRadius; nextX <= lookRadius; nextX++)
        {
            for (int nextZ = -lookRadius; nextZ <= lookRadius; nextZ++)
            {
                nextBiomeConfig = toBiomeConfig(biomeArray[(x + nextX + this.maxSmoothRadius + (z + nextZ + this.maxSmoothRadius) * (NOISE_MAX_X + this.maxSmoothDiameter))]);
                nextBiomeHeight = nextBiomeConfig.biomeHeight;
                biomeWeight = this.nearBiomeWeightArray[(nextX + this.maxSmoothRadius + (nextZ + this.maxSmoothRadius) * this.maxSmoothDiameter)] / (nextBiomeHeight + 2.0F);

                biomeWeight = Math.abs(biomeWeight);
                volatilitySum += nextBiomeConfig.biomeVolatility * biomeWeight;
                heightSum += nextBiomeHeight * biomeWeight;
                WeightSum += biomeWeight;

                // River part

                boolean isRiver = false;
                if (riverArray[(x + nextX + this.maxSmoothRadius + (z + nextZ + this.maxSmoothRadius) * (NOISE_MAX_X + this.maxSmoothDiameter))] == 1)
                {
                    this.riverFound = true;
                    isRiver = true;
                }

                nextRiverHeight = (isRiver) ? nextBiomeConfig.riverHeight : nextBiomeHeight;
                // TODO: Potential divide by zero, not sure what the outcome or the proper solution would be. Uses floats so won't necessarily cause exceptions.
                riverWeight = this.nearBiomeWeightArray[(nextX + this.maxSmoothRadius + (nextZ + this.maxSmoothRadius) * this.maxSmoothDiameter)] / (nextRiverHeight + 2.0F);

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

    private void smoothCHC(double[] chcData, double[] riverData, int x, int z, int maxYSections, int[] biomeArray)
    {
        float weightSum = 0.0F;

        // Gather center data
        final BiomeConfig centerBiomeConfig = toBiomeConfig(biomeArray[(x + this.maxSmoothRadius + (z + this.maxSmoothRadius) * (NOISE_MAX_X + this.maxSmoothDiameter))]);
        final int lookRadius = centerBiomeConfig.CHCSmoothRadius;

        // Iterate through and add the chc
        for (int nextX = -lookRadius; nextX <= lookRadius; nextX++)
        {
            for (int nextZ = -lookRadius; nextZ <= lookRadius; nextZ++)
            {
                // Weight is done on a 2d scale, so add that outside of the loop
                weightSum += 1;

                // Compute the data for each column
                for (int y = 0; y < maxYSections; y++)
                {
                    // Get the biome config at this position
                    final BiomeConfig nextBiomeConfig = toBiomeConfig(biomeArray[(x + nextX + this.maxSmoothRadius + (z + nextZ + this.maxSmoothRadius) * (NOISE_MAX_X + this.maxSmoothDiameter))]);

                    // Add the custom height for both river and normal CHC
                    riverData[y] += nextBiomeConfig.riverHeightMatrix[Math.min(nextBiomeConfig.riverHeightMatrix.length - 1, y)];
                    chcData[y] += nextBiomeConfig.heightMatrix[Math.min(nextBiomeConfig.heightMatrix.length - 1, y)];
                }
            }
        }

        // average the custom height for each column for both arrays
        for (int y = 0; y < chcData.length; y++) {
            chcData[y] = chcData[y] / weightSum;
            riverData[y] = riverData[y] / weightSum;
        }
    }

    private double[] smoothRemainingData(int x, int z, int[] biomeArray) {
        float weightSum = 0.0F;

        double vol1Sum = 0;
        double vol2Sum = 0;
        double vol1WeightSum = 0;
        double vol2WeightSum = 0;
        double maxAverageDepthSum = 0;
        double maxAverageHeightSum = 0;

        // Gather center data
        final BiomeConfig centerBiomeConfig = toBiomeConfig(biomeArray[(x + this.maxSmoothRadius + (z + this.maxSmoothRadius) * (NOISE_MAX_X + this.maxSmoothDiameter))]);
        final int lookRadius = centerBiomeConfig.smoothRadius;

        // Iterate through and add the values
        for (int nextX = -lookRadius; nextX <= lookRadius; nextX++)
        {
            for (int nextZ = -lookRadius; nextZ <= lookRadius; nextZ++)
            {
                // Add weight
                weightSum += 1;

                // Get biome
                final BiomeConfig nextBiomeConfig = toBiomeConfig(biomeArray[(x + nextX + this.maxSmoothRadius + (z + nextZ + this.maxSmoothRadius) * (NOISE_MAX_X + this.maxSmoothDiameter))]);

                // Add other data
                vol1Sum += nextBiomeConfig.volatility1;
                vol2Sum += nextBiomeConfig.volatility2;
                vol1WeightSum += nextBiomeConfig.volatilityWeight1;
                vol2WeightSum += nextBiomeConfig.volatilityWeight2;
                maxAverageDepthSum += nextBiomeConfig.maxAverageDepth;
                maxAverageHeightSum += nextBiomeConfig.maxAverageHeight;
            }
        }

        // Divide by weight to get the final data
        vol1Sum /= weightSum;
        vol2Sum /= weightSum;
        vol1WeightSum /= weightSum;
        vol2WeightSum /= weightSum;
        maxAverageDepthSum /= weightSum;
        maxAverageHeightSum /= weightSum;

        // Return everything in a double array to avoid extraneous fields in the class
        return new double[]{vol1Sum, vol2Sum, vol1WeightSum, vol2WeightSum, maxAverageDepthSum, maxAverageHeightSum};
    }

    /**
     * Gets the BiomeConfig with the given id.
     *
     * @param id The generation id of the biome.
     * @return The BiomeConfig.
     */
    private BiomeConfig toBiomeConfig(int id)
    {
        BiomeConfig biomeConfig = biomes[id];

        if(biomeConfig == null)
        {
            LocalBiome biome = this.configProvider.getBiomeByOTGIdOrNull(id);
            
            biomeConfig = biome.getBiomeConfig();
            biomes[id] = biomeConfig;
        }

        return biomeConfig;
    }
}