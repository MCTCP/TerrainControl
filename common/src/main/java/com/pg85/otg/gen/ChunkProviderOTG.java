package com.pg85.otg.gen;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorldGenRegion;
import com.pg85.otg.common.materials.LocalMaterialData;
import com.pg85.otg.common.materials.LocalMaterials;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.world.WorldConfig;
import com.pg85.otg.gen.biome.BiomeGenerator;
import com.pg85.otg.gen.biome.OutputType;
import com.pg85.otg.gen.noise.legacy.NoiseGeneratorPerlinMesaBlocks;
import com.pg85.otg.gen.noise.legacy.NoiseGeneratorPerlinOctaves;
import com.pg85.otg.gen.terrain.CavesGen;
import com.pg85.otg.gen.terrain.RavinesGen;
import com.pg85.otg.gen.terrain.TerrainGenBase;
import com.pg85.otg.network.ConfigProvider;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.helpers.MathHelper;

import static com.pg85.otg.util.ChunkCoordinate.CHUNK_SIZE;

import java.util.Random;

public class ChunkProviderOTG
{
    // Several constants describing the chunk size of Minecraft
    private static final int NOISE_MAX_X = CHUNK_SIZE / 4 + 1;
    private static final int NOISE_MAX_Z = CHUNK_SIZE / 4 + 1;

    private final Random random;
    private final NoiseGeneratorPerlinOctaves vol1NoiseGen;
    private final NoiseGeneratorPerlinOctaves vol2NoiseGen;
    private final NoiseGeneratorPerlinOctaves volNoiseGen;
    private final NoiseGeneratorPerlinMesaBlocks biomeBlocksNoiseGen;
    private final NoiseGeneratorPerlinOctaves oldTerrainGeneratorNoiseGen;
    private final NoiseGeneratorPerlinOctaves noiseHeightNoiseGen;

    private double[] biomeBlocksNoise = new double[CHUNK_SIZE * CHUNK_SIZE];

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
    
    public ChunkProviderOTG(ConfigProvider configs, long worldSeed)
    {    	
        WorldConfig worldConfig = configs.getWorldConfig();
    	
        this.configProvider = configs;
        this.heightCap = worldConfig.worldHeightCap;
        this.heightScale = worldConfig.worldHeightScale;

        this.random = new Random(worldSeed);

        this.vol1NoiseGen = new NoiseGeneratorPerlinOctaves(this.random, 16);
        this.vol2NoiseGen = new NoiseGeneratorPerlinOctaves(this.random, 16);
        this.volNoiseGen = new NoiseGeneratorPerlinOctaves(this.random, 8);
        this.biomeBlocksNoiseGen = new NoiseGeneratorPerlinMesaBlocks(this.random, 4);
        this.oldTerrainGeneratorNoiseGen = new NoiseGeneratorPerlinOctaves(this.random, 10);
        this.noiseHeightNoiseGen = new NoiseGeneratorPerlinOctaves(this.random, 16);

        this.caveGen = new CavesGen(configs.getWorldConfig(), worldSeed);
        this.canyonGen = new RavinesGen(configs.getWorldConfig(), worldSeed);

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
    
    public void generate(long worldSeed, ChunkBuffer chunkBuffer, LocalWorldGenRegion worldGenRegion)
    {
        ChunkCoordinate chunkCoord = chunkBuffer.getChunkCoordinate();
        int x = chunkCoord.getChunkX();
        int z = chunkCoord.getChunkZ();
        this.random.setSeed(x * 341873128712L + z * 132897987541L);
              
        boolean dry = generateTerrain(worldSeed, x, z, chunkBuffer, worldGenRegion.getBiomeGenerator());

        if(!worldGenRegion.generateModdedCaveGen(worldSeed, x, z, chunkBuffer))
        {
            this.caveGen.generate(worldGenRegion, chunkBuffer);
        }
        this.canyonGen.generate(worldGenRegion, chunkBuffer);

        WorldConfig worldConfig = configProvider.getWorldConfig();
        if (worldConfig.modeTerrain == WorldConfig.TerrainMode.Normal)// || worldConfig.modeTerrain == WorldConfig.TerrainMode.OldGenerator)
        {
        	worldGenRegion.prepareDefaultStructures(worldSeed, x, z, dry);
        }
    }
    
    private boolean generateTerrain(long worldSeed, int x, int z, ChunkBuffer chunkBuffer, BiomeGenerator biomeGenerator)
    {
        int[] biomeArray = null;
        byte[] waterLevel = new byte[CHUNK_SIZE * CHUNK_SIZE];;
    	
        ChunkCoordinate chunkCoord = chunkBuffer.getChunkCoordinate();
        int chunkX = chunkCoord.getChunkX();
        int chunkZ = chunkCoord.getChunkZ();

        int maxYSections = this.heightCap / 8 + 1;
        int usedYSections = this.heightScale / 8 + 1;

        WorldConfig worldConfig = configProvider.getWorldConfig();
        int[] riverArray = null;
        if (worldConfig.improvedRivers)
        {
            riverArray = biomeGenerator.getBiomesUnZoomed(riverArray, chunkX * 4 - maxSmoothRadius, chunkZ * 4 - maxSmoothRadius, NOISE_MAX_X + maxSmoothDiameter, NOISE_MAX_Z + maxSmoothDiameter, OutputType.ONLY_RIVERS);
        }
        
        if (biomeGenerator.canGenerateUnZoomed())
        {
            biomeArray = biomeGenerator.getBiomesUnZoomed(biomeArray, chunkX * 4 - maxSmoothRadius, chunkZ * 4 - maxSmoothRadius, NOISE_MAX_X + maxSmoothDiameter, NOISE_MAX_Z + maxSmoothDiameter, OutputType.DEFAULT_FOR_WORLD);
        } else {
            biomeArray = biomeGenerator.getBiomes(biomeArray, chunkX * CHUNK_SIZE, chunkZ * CHUNK_SIZE, CHUNK_SIZE, CHUNK_SIZE, OutputType.DEFAULT_FOR_WORLD);
        }

        double[] rawTerrain = generateTerrainNoise(chunkX * 4, chunkZ * 4, maxYSections, usedYSections, biomeArray, riverArray);
       
        // Now that the raw terrain is generated, replace raw biome array (5 x 5) with a higher resolution (16 x 16) one.
        if (biomeGenerator.canGenerateUnZoomed())
        {
            biomeArray = biomeGenerator.getBiomes(biomeArray, chunkX * CHUNK_SIZE, chunkZ * CHUNK_SIZE, CHUNK_SIZE, CHUNK_SIZE, OutputType.DEFAULT_FOR_WORLD);
        }
		
        // The terrain generator takes low resolution noise and upscales it to a full chunk through trilinear interpolation.
        // The generator gets a 5 x 33 x 5 array of noise in the beginning, where 8 units of noise in a cube create a 4x8x4 voxel of terrain.
        // Unfortunately, this method has many issues. The most noticeable of which is the discontinuity along interpolation borders,
        // especially vertical ones every 8 blocks. Unfortunately due to the nature of trilinear interpolation, there's no easy way to solve this.
        // TODO: tricubic interpolation seems like the fix here. Implementing that will take time, effort, and will break existing worldgen so we'll need to proceed with caution.
        // Since the terrain generator needs 5 noise values along the x and z axis, some of that data spills out into neighboring chunks.
        // TODO: Caching the noise data will allow us to re-use noise data and save significant computational time.
        
        double waterLevel_x0z0;
        double waterLevel_x0z1;
        double waterLevel_x1z0;
        double waterLevel_x1z1;
        double waterLevelForArray;
        
        double x0z0;
        double x0z1;
        double x1z0;
        double x1z1;

        double x0z0y1;
        double x0z1y1;
        double x1z0y1;
        double x1z1y1;

        double x0z0y0;
        double x0z1y0;
        double x1z0y0;
        double x1z1y0;
       
		BiomeConfig biomeConfig;
		int waterLevelMax;
		LocalMaterialData block;

        int realY;
		double xLerp;
        double yLerp;
        double zLerp;
		double z0;
		double z1;
        double density;
        
        for (int noiseX = 0; noiseX < 4; noiseX++)
        {
            for (int noiseZ = 0; noiseZ < 4; noiseZ++)
            {
                // Water level (fill final array based on smaller, non-smoothed array)
                //TODO: remove this interpolation and use the raw biome data instead for better results
                waterLevel_x0z0 = this.waterLevelRaw[noiseX * NOISE_MAX_X + noiseZ] & 0xFF;
                waterLevel_x0z1 = this.waterLevelRaw[noiseX * NOISE_MAX_X + noiseZ + 1] & 0xFF;
                waterLevel_x1z0 = this.waterLevelRaw[(noiseX + 1) * NOISE_MAX_X + noiseZ] & 0xFF;
                waterLevel_x1z1 = this.waterLevelRaw[(noiseX + 1) * NOISE_MAX_X + noiseZ + 1] & 0xFF;

                // Use bilinear interpolation to smooth out the water table array.
                for (int pieceX = 0; pieceX < 4; pieceX++)
                {
                    xLerp = pieceX / 4.0;

                    z0 = lerp(xLerp, waterLevel_x0z0, waterLevel_x1z0);
                    z1 = lerp(xLerp, waterLevel_x0z1, waterLevel_x1z1);

                    for (int pieceZ = 0; pieceZ < 4; pieceZ++)
                    {
                        waterLevelForArray = lerp(pieceZ / 8.0, z0, z1);
                        // Fill water level array
                        waterLevel[(noiseZ * 4 + pieceZ) * 16 + (pieceX + noiseX * 4)] = (byte) waterLevelForArray;
                    }
                }

                for (int noiseY = 0; noiseY < this.heightCap / 8; noiseY++)
                {
                    // Lower 4 corners of the cube
                    x0z0y0 = rawTerrain[(noiseX * NOISE_MAX_Z + noiseZ) * maxYSections + noiseY];
                    x0z1y0 = rawTerrain[(noiseX * NOISE_MAX_Z + noiseZ + 1) * maxYSections + noiseY];
                    x1z0y0 = rawTerrain[((noiseX + 1) * NOISE_MAX_Z + noiseZ) * maxYSections + noiseY];
                    x1z1y0 = rawTerrain[((noiseX + 1) * NOISE_MAX_Z + noiseZ + 1) * maxYSections + noiseY];

                    // Upper 4 corners of the cube
                    x0z0y1 = rawTerrain[(noiseX * NOISE_MAX_Z + noiseZ) * maxYSections + noiseY + 1];
                    x0z1y1 = rawTerrain[(noiseX * NOISE_MAX_Z + noiseZ + 1) * maxYSections + noiseY + 1];
                    x1z0y1 = rawTerrain[((noiseX + 1) * NOISE_MAX_Z + noiseZ) * maxYSections + noiseY + 1];
                    x1z1y1 = rawTerrain[((noiseX + 1) * NOISE_MAX_Z + noiseZ + 1) * maxYSections + noiseY + 1];

                    // Now that we've sampled the corners of the cube, we can now interpolate between them to expand the noise into a 4x8x4 voxel.
                    // First, we'll evaluate the current progress of the vertical sides, then move onto the sides on the x axis, then the z axis to
                    // get the noise at a real block position.
                    for (int pieceY = 0; pieceY < 8; pieceY++)
                    {
                        // Progress along the verticals (y axis) of the voxel
                        yLerp = pieceY / 8.0;

                        // Interpolate noise data based on y progress
                        x0z0 = lerp(yLerp, x0z0y0, x0z0y1);
                        x1z0 = lerp(yLerp, x1z0y0, x1z0y1);
                        x0z1 = lerp(yLerp, x0z1y0, x0z1y1);
                        x1z1 = lerp(yLerp, x1z1y0, x1z1y1);

                        for (int pieceX = 0; pieceX < 4; pieceX++)
                        {
                            // Progress along the x axis of the voxel
                            xLerp = pieceX / 4.0;

                            // Interpolate noise based on x progress
                            z0 = lerp(xLerp, x0z0, x1z0);
                            z1 = lerp(xLerp, x0z1, x1z1);

                            for (int pieceZ = 0; pieceZ < 4; pieceZ++)
                            {
                                biomeConfig = toBiomeConfig(biomeArray[(noiseZ * 4 + pieceZ) * 16 + (pieceX + noiseX * 4)]);
                                waterLevelMax = waterLevel[(noiseZ * 4 + pieceZ) * 16 + (pieceX + noiseX * 4)] & 0xFF;

                                // Progress along the z axis of the voxel
                                zLerp = pieceZ / 4.0;

                                // Get the final density based on the z progress
                                density = lerp(zLerp, z0, z1);

                                block = LocalMaterials.AIR;

                                realY = (noiseY * 8) + pieceY;

                                // If the y here is within the water level range, set water                                
                                if (realY < waterLevelMax && realY > biomeConfig.waterLevelMin)
                                {
                                	// TODO: Setting replaced blocks here might cause problems for mods
                                	// that check for biome.waterblock during the replaceBiomeBlocks event.
                                	block = biomeConfig.getWaterBlockReplaced(realY);
                                	chunkBuffer.setHighestBlockForColumn(pieceX + noiseX * 4, noiseZ * 4 + pieceZ, realY);
                                }
                                
                                // Make densities above 0 solid                                
                                if (density > 0.0D)
                                {
                                	// TODO: Setting replaced blocks here might cause problems for mods
                                	// that check for biome.stoneblock during the replaceBiomeBlocks event.
                                	block = biomeConfig.getStoneBlockReplaced(realY);
                                	chunkBuffer.setHighestBlockForColumn(pieceX + noiseX * 4, noiseZ * 4 + pieceZ, realY);
                                }
                                
                                // Set the generating block at this position.
                                chunkBuffer.setBlock(pieceX + noiseX * 4, realY, noiseZ * 4 + pieceZ, block);
                            }
                        }
                    }
                }
            }
        }

        boolean dry = false;
        if(OTG.fireReplaceBiomeBlocksEvent(x, z, chunkBuffer))
		{
        	dry = addBiomeBlocksAndCheckWater(worldSeed, chunkBuffer, biomeArray, waterLevel);
		}
        return dry;
    }

    private int lastX = Integer.MAX_VALUE;
    private int lastZ = Integer.MAX_VALUE;
    private double lastNoise = 0;   
    public double getBiomeBlocksNoiseValue(int blockX, int blockZ)
    {
    	double noise = this.lastNoise;
    	if(this.lastX != blockX || this.lastZ != blockZ)
    	{
        	final double d1 = 0.03125D;
        	noise = this.biomeBlocksNoiseGen.getRegion(new double[1], blockX, blockZ, 1, 1, d1 * 2.0D, d1 * 2.0D, 1.0D)[0];
        	this.lastX = blockX;
        	this.lastZ = blockZ;
        	this.lastNoise = noise;
    	}
    	return noise;
    }
    
    /**
     * Adds the biome blocks like grass, dirt, sand and sandstone. Also adds
     * bedrock at the bottom of the map.
     *
     * @param chunkBuffer The the chunk to add the blocks to.
     * @return Whether there is a lot of water in this chunk. If yes, no
     *         villages will be placed.
     */
    private boolean addBiomeBlocksAndCheckWater(long worldSeed, ChunkBuffer chunkBuffer, int[] biomeArray, byte[] waterLevel)
    {
        ChunkCoordinate chunkCoord = chunkBuffer.getChunkCoordinate();

        int dryBlocksOnSurface = 256;

        final double d1 = 0.03125D;
        this.biomeBlocksNoise = this.biomeBlocksNoiseGen.getRegion(this.biomeBlocksNoise, chunkCoord.getBlockX(), chunkCoord.getBlockZ(), CHUNK_SIZE, CHUNK_SIZE, d1 * 2.0D, d1 * 2.0D, 1.0D);

        GeneratingChunk generatingChunk = new GeneratingChunk(this.random, waterLevel, this.biomeBlocksNoise, this.heightCap);

        for (int x = 0; x < CHUNK_SIZE; x++)
        {
            for (int z = 0; z < CHUNK_SIZE; z++)
            {
                // The following code is executed for each column in the chunk

                // Get the current biome config and some properties
                final BiomeConfig biomeConfig = toBiomeConfig(biomeArray[(x + z * CHUNK_SIZE)]);

                biomeConfig.surfaceAndGroundControl.spawn(worldSeed, generatingChunk, chunkBuffer, biomeConfig, chunkCoord.getBlockX() + x, chunkCoord.getBlockZ() + z);

                // Count liquid blocks
                if (
            		chunkBuffer.getBlock(x, biomeConfig.waterLevelMax, z).isLiquid()                	
            	)
                {
                    dryBlocksOnSurface--;
                }

                // End of code for each column
            }
        }

        return dryBlocksOnSurface > 250;
    }

    private double[] generateTerrainNoise(int xOffset, int zOffset, int maxYSections, int usedYSections, int[] biomeArray, int[] riverArray)
    {
        double[] rawTerrain = new double[NOISE_MAX_X * maxYSections * NOISE_MAX_Z];

        WorldConfig worldConfig = configProvider.getWorldConfig();
        double xzScale = 684.41200000000003D * worldConfig.getFractureHorizontal();
        double yScale = 684.41200000000003D * worldConfig.getFractureVertical();

        // Initialize the noise generators
        if (worldConfig.oldTerrainGenerator)
        {
            this.oldTerrainGeneratorNoise = this.oldTerrainGeneratorNoiseGen.Noise2D(this.oldTerrainGeneratorNoise, xOffset, zOffset, NOISE_MAX_X, NOISE_MAX_Z, 1.121D, 1.121D);
        }
        this.noiseHeightNoise = this.noiseHeightNoiseGen.Noise2D(this.noiseHeightNoise, xOffset, zOffset, NOISE_MAX_X, NOISE_MAX_Z, 200.0D, 200.0D);

        this.volNoise = this.volNoiseGen.Noise3D(this.volNoise, xOffset, 0, zOffset, NOISE_MAX_X, maxYSections, NOISE_MAX_Z, xzScale / 80.0D, yScale / 160.0D, xzScale / 80.0D);
        this.vol1Noise = this.vol1NoiseGen.Noise3D(this.vol1Noise, xOffset, 0, zOffset, NOISE_MAX_X, maxYSections, NOISE_MAX_Z, xzScale, yScale, xzScale);
        this.vol2Noise = this.vol2NoiseGen.Noise3D(this.vol2Noise, xOffset, 0, zOffset, NOISE_MAX_X, maxYSections, NOISE_MAX_Z, xzScale, yScale, xzScale);

        // These variables are used to index 3d and 2d noise arrays quickly, respectively
        int i3D = 0;
        int i2D = 0;

        double topFalloff;
        int biomeId;
        BiomeConfig biomeConfig;

        double volatility1;
        double volatility2;
        double volatilityWeight1;
        double volatilityWeight2;
        double maxAverageDepth;
        double maxAverageHeight;
        double[] data;
        double noiseHeight;
        double[] CHC;
        double[] riverCHC;
        double output;
        double columnHeight;        
        double vol1;
        double vol2;
        double interpolationNoise;
        
        // Fill the noise array with terrain noise
        for (int x = 0; x < NOISE_MAX_X; x++)
        {
            for (int z = 0; z < NOISE_MAX_Z; z++)
            {
                biomeId = biomeArray[(x + this.maxSmoothRadius + (z + this.maxSmoothRadius) * (NOISE_MAX_X + this.maxSmoothDiameter))];

                biomeConfig = toBiomeConfig(biomeId);

                // Data used for noise generation
                volatility1 = biomeConfig.volatility1;
                volatility2 = biomeConfig.volatility2;
                volatilityWeight1 = biomeConfig.volatilityWeight1;
                volatilityWeight2 = biomeConfig.volatilityWeight2;
                maxAverageDepth = biomeConfig.maxAverageDepth;
                maxAverageHeight = biomeConfig.maxAverageHeight;

                // If improved smoothing is enabled, smooth all these values as well
                if (worldConfig.improvedSmoothing)
                {
                    data = smoothRemainingData(x, z, biomeArray);
                    volatility1 = data[0];
                    volatility2 = data[1];
                    volatilityWeight1 = data[2];
                    volatilityWeight2 = data[3];
                    maxAverageDepth = data[4];
                    maxAverageHeight = data[5];
                }

                // Compute noise height additions, to make the terrain height more varied.
                noiseHeight = this.noiseHeightNoise[i2D] / 8000.0D;
                if (noiseHeight < 0.0D)
                {
                	// If the height addition is negative, normalize it to be positive.
                    noiseHeight = -noiseHeight * 0.3D;
                }
                
                noiseHeight = noiseHeight * 3.0D - 2.0D;

                // If the height is negative, then clamp, normalize it and subtract the max average depth.
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
                } else {
                	// Clamp and add the max average height
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
                    } else {
                        this.biomeFactor(x, z, usedYSections, noiseHeight, biomeArray);
                    }
                } else {
                    this.oldBiomeFactor(x, z, i2D, usedYSections, noiseHeight, biomeArray);
            	}

                i2D++;

                // Initialize both CHC for this column
                CHC = new double[biomeConfig.heightMatrix.length];
                riverCHC = new double[biomeConfig.riverHeightMatrix.length];

                // If CHC smoothing is enabled, smooth based on the biome at this column's smoothing radius
                if (worldConfig.customHeightControlSmoothing)
                {
                    // This function modifies the arrays directly, no need to set
                    smoothCHC(CHC, riverCHC, x, z, maxYSections, biomeArray);
                } else {
                    // If smoothing is not enabled, revert to the original CHC data
                    CHC = biomeConfig.heightMatrix;
                    riverCHC = biomeConfig.riverHeightMatrix;
                }

                // Compute and store the noise value for each section
                for (int y = 0; y < maxYSections; y++)
                {
                    // If there's a river here, use the river height and volatility instead
                    if (this.riverFound)
                    {
                        columnHeight = (this.riverHeight - y) * 12.0D * 128.0D / this.heightCap / this.riverVol;
                    } else {
                        columnHeight = (this.heightFactor - y) * 12.0D * 128.0D / this.heightCap / this.volatilityFactor;
                    }

                    // Multiply the height value of this column by 4 if it's above 0
                    if (columnHeight > 0.0D)
                    {
                        columnHeight *= 4.0D;
                    }

                    // The volatility noises create the "actual" terrain and have a range of (-256, 256) * volatility1/2.
                    vol1 = this.vol1Noise[i3D] / 512.0D * volatility1;
                    vol2 = this.vol2Noise[i3D] / 512.0D * volatility2;

                    // To create a more varied landscape, a 3rd noise is used to interpolate between the two other noises.
                    // Due to the nature of Minecraft's noise functions, most of the time this noise chooses either volatility instead of interpolating.
                    interpolationNoise = (this.volNoise[i3D] / 10.0D + 1.0D) / 2.0D;
                    if (interpolationNoise < volatilityWeight1)
                    {
                        output = vol1;
                    }
                    else if (interpolationNoise > volatilityWeight2)
                    {
                        output = vol2;
                    } else {
                        // Linearly interpolate between the two volatility values.
                        output = lerp(interpolationNoise, vol1, vol2);
                    }

                    // Add the column height to the noise if default height control is enabled.
                    // If not, then BiomeHeight and BiomeVolatility will have no effect on the terrain
                    // and everything will depend on CHC and the other volatility controls.
                    if (!biomeConfig.disableNotchHeightControl)
                    {
                        output += columnHeight;

                        if (y > maxYSections - 4)
                        {
                            topFalloff = (y - (maxYSections - 4)) / 3.0F;
                            // Reduce last three layers of noise
                            output = output * (1.0D - topFalloff) + -10.0D * topFalloff;
                        }
                    }
                    if (this.riverFound)
                    {
                    	output += riverCHC[Math.min(biomeConfig.riverHeightMatrix.length - 1, y)];
                    } else {
                    	// CHC directly modifies the terrain noise at a given column, providing for fine access
                    	output += CHC[Math.min(biomeConfig.heightMatrix.length - 1, y)];
                    }

                    // Store the terrain noise for use in the trilinear interpolation
                    rawTerrain[i3D] = output;
                    i3D++;
                }
            }
        }

        return rawTerrain;
    }

    private void oldBiomeFactor(int x, int z, int i2D, int ySections, double noiseHeight, int[] biomeArray)
    {
        BiomeConfig biomeConfig = toBiomeConfig(biomeArray[(x + this.maxSmoothRadius + (z + this.maxSmoothRadius) * (NOISE_MAX_X + this.maxSmoothDiameter))]);
        this.volatilityFactor = (1.0D - Math.min(1, biomeConfig.biomeTemperature) * biomeConfig.biomeWetness);

        this.volatilityFactor *= this.volatilityFactor;
        this.volatilityFactor = 1.0D - this.volatilityFactor * this.volatilityFactor;

        this.volatilityFactor = (this.volNoise[i2D] + 256.0D) / 512.0D * this.volatilityFactor;
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

        BiomeConfig centerBiomeConfig = toBiomeConfig(biomeArray[(x + this.maxSmoothRadius + (z + this.maxSmoothRadius) * (NOISE_MAX_X + this.maxSmoothDiameter))]);
        int lookRadius = centerBiomeConfig.smoothRadius;

        float nextBiomeHeight, biomeWeight;
        BiomeConfig nextBiomeConfig;
        
        for (int nextX = -lookRadius; nextX <= lookRadius; nextX++)
        {
            for (int nextZ = -lookRadius; nextZ <= lookRadius; nextZ++)
            {
                nextBiomeConfig = toBiomeConfig(biomeArray[(x + nextX + this.maxSmoothRadius + (z + nextZ + this.maxSmoothRadius) * (NOISE_MAX_X + this.maxSmoothDiameter))]);

                nextBiomeHeight = nextBiomeConfig.biomeHeight;
                biomeWeight = this.nearBiomeWeightArray[(nextX + this.maxSmoothRadius + (nextZ + this.maxSmoothRadius) * this.maxSmoothDiameter)] / (nextBiomeHeight + 2.0F);
                biomeWeight = Math.abs(biomeWeight); // Shouldnt be necessary, should always be positive?
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
        boolean isRiver;
        
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

                isRiver = false;
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
        BiomeConfig centerBiomeConfig = toBiomeConfig(biomeArray[(x + this.maxSmoothRadius + (z + this.maxSmoothRadius) * (NOISE_MAX_X + this.maxSmoothDiameter))]);
        int lookRadius = centerBiomeConfig.CHCSmoothRadius;
        BiomeConfig nextBiomeConfig;
        
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
                    nextBiomeConfig = toBiomeConfig(biomeArray[(x + nextX + this.maxSmoothRadius + (z + nextZ + this.maxSmoothRadius) * (NOISE_MAX_X + this.maxSmoothDiameter))]);

                    // Add the custom height for both river and normal CHC
                    riverData[y] += nextBiomeConfig.riverHeightMatrix[Math.min(nextBiomeConfig.riverHeightMatrix.length - 1, y)];
                    chcData[y] += nextBiomeConfig.heightMatrix[Math.min(nextBiomeConfig.heightMatrix.length - 1, y)];
                }
            }
        }

        // average the custom height for each column for both arrays
        for (int y = 0; y < chcData.length; y++)
        {
            chcData[y] = chcData[y] / weightSum;
            riverData[y] = riverData[y] / weightSum;
        }
    }

    private double[] smoothRemainingData(int x, int z, int[] biomeArray)
    {
        float weightSum = 0.0F;

        double vol1Sum = 0;
        double vol2Sum = 0;
        double vol1WeightSum = 0;
        double vol2WeightSum = 0;
        double maxAverageDepthSum = 0;
        double maxAverageHeightSum = 0;
        BiomeConfig nextBiomeConfig;

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
                nextBiomeConfig = toBiomeConfig(biomeArray[(x + nextX + this.maxSmoothRadius + (z + nextZ + this.maxSmoothRadius) * (NOISE_MAX_X + this.maxSmoothDiameter))]);

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
    
    // Performs a linear interpolation along one axis.
    private static double lerp(double delta, double start, double end)
    {
        return start + delta * (end - start);
    }
}