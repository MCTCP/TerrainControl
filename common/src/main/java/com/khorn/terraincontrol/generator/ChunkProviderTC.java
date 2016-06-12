package com.khorn.terraincontrol.generator;

import static com.khorn.terraincontrol.util.ChunkCoordinate.CHUNK_X_SIZE;
import static com.khorn.terraincontrol.util.ChunkCoordinate.CHUNK_Z_SIZE;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.ConfigProvider;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.biome.BiomeGenerator;
import com.khorn.terraincontrol.generator.biome.OutputType;
import com.khorn.terraincontrol.generator.noise.NoiseGeneratorNewOctaves;
import com.khorn.terraincontrol.generator.terrain.*;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import java.util.Random;

// Please don`t remove this. This disable warnings about x+0 arithmetic
// operations in my IDE. Khorn.
@SuppressWarnings("PointlessArithmeticExpression")
public class ChunkProviderTC
{

    public static final int HEIGHT_BITS = 8;
    public static final int HEIGHT_BITS_PLUS_FOUR = HEIGHT_BITS + 4;

    private final LocalMaterialData air = TerrainControl.toLocalMaterialData(DefaultMaterial.AIR, 0);

    private final Random random;
    private final NoiseGeneratorNewOctaves noiseGen4;
    private double[] noise4 = new double[CHUNK_X_SIZE * CHUNK_Z_SIZE];

    private final LocalWorld localWorld;
    private final ConfigProvider configProvider;

    private final TerrainGenBase caveGen;
    private final TerrainGenBase ravineGen;

    private int[] biomeArray;
    // Water level for each column
    private final byte[] waterLevel = new byte[CHUNK_X_SIZE * CHUNK_Z_SIZE];

    private final int heightCap;

    private final TerrainShapeBase terrainShape;

    public ChunkProviderTC(ConfigProvider configs, LocalWorld world)
    {
        this.configProvider = configs;
        this.localWorld = world;
        this.heightCap = world.getHeightCap();

        this.random = new Random(world.getSeed());

        this.terrainShape = new TerrainShapeNormal(configs, heightCap);
        this.noiseGen4 = new NoiseGeneratorNewOctaves(this.random, 4);

        this.caveGen = new CavesGen(configs.getWorldConfig(), this.localWorld);
        this.ravineGen = new RavinesGen(configs.getWorldConfig(), this.localWorld);
    }

    public void generate(ChunkBuffer chunkBuffer)
    {
        ChunkCoordinate chunkCoord = chunkBuffer.getChunkCoordinate();
        int x = chunkCoord.getChunkX();
        int z = chunkCoord.getChunkZ();
        this.random.setSeed(x * 341873128712L + z * 132897987541L);

        generateTerrain(chunkBuffer);

        boolean dry = addBiomeBlocksAndCheckWater(chunkBuffer);

        this.caveGen.generate(chunkBuffer);
        this.ravineGen.generate(chunkBuffer);

        WorldConfig worldConfig = configProvider.getWorldConfig();
        if (worldConfig.ModeTerrain == WorldConfig.TerrainMode.Normal || worldConfig.ModeTerrain == WorldConfig.TerrainMode.OldGenerator)
        {
            this.localWorld.prepareDefaultStructures(x, z, dry);
        }

    }

    protected void generateTerrain(ChunkBuffer chunkBuffer)
    {
        ChunkCoordinate chunkCoord = chunkBuffer.getChunkCoordinate();
        int chunkX = chunkCoord.getChunkX();
        int chunkZ = chunkCoord.getChunkZ();

        final int four = 4;
        final int oneEightOfHeight = this.heightCap / 8;

        BiomeGenerator biomeGenerator = this.localWorld.getBiomeGenerator();

        try
        {
            this.terrainShape.open(biomeGenerator, chunkCoord);

            this.biomeArray = biomeGenerator.getBiomes(this.biomeArray, chunkX * CHUNK_X_SIZE, chunkZ * CHUNK_Z_SIZE, CHUNK_X_SIZE, CHUNK_Z_SIZE, OutputType.DEFAULT_FOR_WORLD);

            final double oneEight = 0.125D;
            final double oneFourth = 0.25D;

            for (int noisePieceX = 0; noisePieceX < four; noisePieceX++)
            {
                for (int noisePieceZ = 0; noisePieceZ < four; noisePieceZ++)
                {
                    // Water level (fill final array based on smaller,
                    // non-smoothed array using linear interpolation)
                    double waterLevel_x0z0 = terrainShape.getWaterLevel(noisePieceX, noisePieceZ);
                    double waterLevel_x0z1 = terrainShape.getWaterLevel(noisePieceX, noisePieceZ + 1);
                    final double waterLevel_x1z0 = (terrainShape.getWaterLevel(noisePieceX + 1, noisePieceZ) - waterLevel_x0z0) * oneFourth;
                    final double waterLevel_x1z1 = (terrainShape.getWaterLevel(noisePieceX + 1, noisePieceZ + 1) - waterLevel_x0z1) * oneFourth;

                    for (int inNoisePieceX = 0; inNoisePieceX < 4; inNoisePieceX++)
                    {
                        double waterLevelForArray = waterLevel_x0z0;
                        final double d17_1 = (waterLevel_x0z1 - waterLevel_x0z0) * oneFourth;

                        for (int inNoisePieceZ = 0; inNoisePieceZ < 4; inNoisePieceZ++)
                        {
                            // Fill water level array
                            this.waterLevel[(noisePieceZ * 4 + inNoisePieceZ) * 16 + (inNoisePieceX + noisePieceX * 4)] = (byte) waterLevelForArray;

                            waterLevelForArray += d17_1;

                        }
                        waterLevel_x0z0 += waterLevel_x1z0;
                        waterLevel_x0z1 += waterLevel_x1z1;

                    }

                    // Terrain noise (linear interpolation)
                    for (int noisePieceY = 0; noisePieceY < oneEightOfHeight; noisePieceY++)
                    {

                        double x0z0 = terrainShape.getNoise(noisePieceX, noisePieceY, noisePieceZ);
                        double x0z1 = terrainShape.getNoise(noisePieceX, noisePieceY, noisePieceZ + 1);
                        double x1z0 = terrainShape.getNoise(noisePieceX + 1, noisePieceY, noisePieceZ);
                        double x1z1 = terrainShape.getNoise(noisePieceX + 1, noisePieceY, noisePieceZ + 1);

                        final double x0z0y1 = (terrainShape.getNoise(noisePieceX, noisePieceY + 1, noisePieceZ) - x0z0) * oneEight;
                        final double x0z1y1 = (terrainShape.getNoise(noisePieceX, noisePieceY + 1, noisePieceZ + 1) - x0z1) * oneEight;
                        final double x1z0y1 = (terrainShape.getNoise(noisePieceX + 1, noisePieceY + 1, noisePieceZ) - x1z0) * oneEight;
                        final double x1z1y1 = (terrainShape.getNoise(noisePieceX + 1, noisePieceY + 1, noisePieceZ + 1) - x1z1) * oneEight;

                        for (int inNoisePieceY = 0; inNoisePieceY < 8; inNoisePieceY++)
                        {

                            double d11 = x0z0;
                            double d12 = x0z1;
                            final double d13 = (x1z0 - x0z0) * oneFourth;
                            final double d14 = (x1z1 - x0z1) * oneFourth;

                            for (int inNoisePieceX = 0; inNoisePieceX < 4; inNoisePieceX++)
                            {
                                double blockNoise = d11;
                                final double d17 = (d12 - d11) * oneFourth;
                                for (int inNoisePieceZ = 0; inNoisePieceZ < 4; inNoisePieceZ++)
                                {
                                    final BiomeConfig biomeConfig = toBiomeConfig(this.biomeArray[(noisePieceZ * TerrainShapeBase.PIECE_Z_SIZE + inNoisePieceZ) * CHUNK_X_SIZE + (inNoisePieceX + noisePieceX * TerrainShapeBase.PIECE_X_SIZE)]);
                                    final int waterLevelMax = this.waterLevel[(noisePieceZ * TerrainShapeBase.PIECE_Z_SIZE + inNoisePieceZ) * CHUNK_X_SIZE + (inNoisePieceX + noisePieceX * TerrainShapeBase.PIECE_X_SIZE)] & 0xFF;
                                    LocalMaterialData block = air;
                                    if (noisePieceY * 8 + inNoisePieceY < waterLevelMax && noisePieceY * 8 + inNoisePieceY > biomeConfig.waterLevelMin)
                                    {
                                        block = biomeConfig.waterBlock;
                                    }

                                    if (blockNoise > 0.0D)
                                    {
                                        block = biomeConfig.stoneBlock;
                                    }

                                    chunkBuffer.setBlock(inNoisePieceX + noisePieceX * TerrainShapeBase.PIECE_X_SIZE, noisePieceY * TerrainShapeBase.PIECE_Y_SIZE + inNoisePieceY, noisePieceZ * TerrainShapeBase.PIECE_Z_SIZE + inNoisePieceZ, block);
                                    blockNoise += d17;
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
        } finally
        {
            this.terrainShape.close();
        }
    }

    /**
     * Adds the biome blocks like grass, dirt, sand and sandstone. Also adds
     * bedrock at the bottom of the map.
     * 
     * @param chunkBuffer The the chunk to add the blocks to.
     * @return Whether there is a lot of water in this chunk. If yes, no
     *         villages will be placed.
     */
    protected boolean addBiomeBlocksAndCheckWater(ChunkBuffer chunkBuffer)
    {
        ChunkCoordinate chunkCoord = chunkBuffer.getChunkCoordinate();

        int dryBlocksOnSurface = 256;

        final double d1 = 0.03125D;
        this.noise4 = this.noiseGen4.a(this.noise4, chunkCoord.getBlockX(), chunkCoord.getBlockZ(), CHUNK_X_SIZE, CHUNK_Z_SIZE, d1 * 2.0D, d1 * 2.0D, 1.0D);

        GeneratingChunk generatingChunk = new GeneratingChunk(random, waterLevel, noise4, heightCap);

        for (int x = 0; x < CHUNK_X_SIZE; x++)
        {
            for (int z = 0; z < CHUNK_Z_SIZE; z++)
            {
                // The following code is executed for each column in the chunk

                // Get the current biome config and some properties
                final BiomeConfig biomeConfig = this.configProvider.getBiomeByIdOrNull(this.biomeArray[(x + z * CHUNK_X_SIZE)]).getBiomeConfig();

                biomeConfig.surfaceAndGroundControl.spawn(generatingChunk, chunkBuffer, biomeConfig, chunkCoord.getBlockX() + x, chunkCoord.getBlockZ() + z);

                // Count how many water there is
                if (chunkBuffer.getBlock(x, biomeConfig.waterLevelMax, z).equals(biomeConfig.waterBlock))
                {
                    dryBlocksOnSurface--;
                }

                // End of code for each column
            }
        }

        return dryBlocksOnSurface > 250;
    }

    /**
     * Gets the BiomeConfig with the given id.
     * 
     * @param id The generation id of the biome.
     * @return The BiomeConfig.
     */
    private BiomeConfig toBiomeConfig(int id)
    {
        return this.configProvider.getBiomeByIdOrNull(id).getBiomeConfig();
    }

}
