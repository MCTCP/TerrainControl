package com.khorn.terraincontrol.generator;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldSettings;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.generator.noise.NoiseGeneratorNewOctaves;
import com.khorn.terraincontrol.generator.resource.Resource;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import java.util.Random;

public class ObjectSpawner
{

    private WorldSettings worldSettings;
    private Random rand;
    private LocalWorld world;
    private NoiseGeneratorNewOctaves noiseGen;
    private double[] reusableChunkNoiseArray;

    public ObjectSpawner(WorldSettings configs, LocalWorld localWorld)
    {
        this.worldSettings = configs;
        this.rand = new Random();
        this.world = localWorld;
        this.noiseGen = new NoiseGeneratorNewOctaves(new Random(world.getSeed()), 4);
    }

    public void populate(ChunkCoordinate chunkCoord)
    {
        // Get the corner block coords
        int x = chunkCoord.getChunkX() * 16;
        int z = chunkCoord.getChunkZ() * 16;

        // Get the biome of the other corner
        LocalBiome biome = world.getCalculatedBiome(x + 15, z + 15);

        // Null check
        if (biome == null)
        {
            TerrainControl.log(LogMarker.DEBUG, "Unknown biome at {},{}  (chunk {}). Population failed.", x + 15, z + 15, chunkCoord);
            return;
        }

        BiomeConfig biomeConfig = biome.getBiomeConfig();

        // Get the random generator
        long resourcesSeed = worldSettings.worldConfig.resourcesSeed != 0L ? worldSettings.worldConfig.resourcesSeed : world.getSeed();
        this.rand.setSeed(resourcesSeed);
        long l1 = this.rand.nextLong() / 2L * 2L + 1L;
        long l2 = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed(chunkCoord.getChunkX() * l1 + chunkCoord.getChunkZ() * l2 ^ resourcesSeed);

        // Generate structures
        boolean hasGeneratedAVillage = world.placeDefaultStructures(rand, chunkCoord);

        // Mark population started
        world.startPopulation(chunkCoord);
        TerrainControl.firePopulationStartEvent(world, rand, hasGeneratedAVillage, chunkCoord);
        
        // Complex surface blocks
        placeComplexSurfaceBlocks(chunkCoord);

        // Resource sequence
        for (Resource res : biomeConfig.resourceSequence)
        {
            res.process(world, rand, hasGeneratedAVillage, chunkCoord);
        }

        // Animals
        world.placePopulationMobs(biome, rand, chunkCoord);

        // Snow and ice
        freezeChunk(chunkCoord);

        // Replace blocks
        world.replaceBlocks();

        // Mark population ended
        TerrainControl.firePopulationEndEvent(world, rand, hasGeneratedAVillage, chunkCoord);
        world.endPopulation();
    }
    
    protected void placeComplexSurfaceBlocks(ChunkCoordinate chunkCoord)
    {
        this.reusableChunkNoiseArray = this.noiseGen.a(this.reusableChunkNoiseArray, chunkCoord.getChunkX() * 16, chunkCoord.getChunkZ() * 16, 16, 16, 0.0625D, 0.0625D, 1.0D);

        int x = chunkCoord.getChunkX() * 16 + 8;
        int z = chunkCoord.getChunkZ() * 16 + 8;
        for (int i = 0; i < 16; i++)
        {
            for (int j = 0; j < 16; j++)
            {
                int blockToFreezeX = x + i;
                int blockToFreezeZ = z + j;
                // Using the calculated biome id so that ReplaceToBiomeName can't mess up the ids
                LocalBiome biome = this.world.getCalculatedBiome(blockToFreezeX, blockToFreezeZ);
                if (biome != null)
                {
                    double noise = this.reusableChunkNoiseArray[i + j * 16];
                    biome.getBiomeConfig().surfaceAndGroundControl.spawn(world, noise, blockToFreezeX, blockToFreezeZ);
                }
            }
        }
    }

    protected void freezeChunk(ChunkCoordinate chunkCoord)
    {
        LocalMaterialData snowMaterial = TerrainControl.toLocalMaterialData(DefaultMaterial.SNOW, 0);
        int x = chunkCoord.getChunkX() * 16 + 8;
        int z = chunkCoord.getChunkZ() * 16 + 8;
        for (int i = 0; i < 16; i++)
        {
            for (int j = 0; j < 16; j++)
            {
                int blockToFreezeX = x + i;
                int blockToFreezeZ = z + j;
                freezeColumn(blockToFreezeX, blockToFreezeZ, snowMaterial);
            }
        }
    }

    protected void freezeColumn(int x, int z, LocalMaterialData snowMaterial)
    {
        // Using the calculated biome id so that ReplaceToBiomeName can't mess up the ids
        LocalBiome biome = world.getCalculatedBiome(x, z);
        if (biome != null)
        {
            BiomeConfig biomeConfig = biome.getBiomeConfig();
            int blockToFreezeY = world.getHighestBlockYAt(x, z);
            if (blockToFreezeY > 0 && biome.getTemperatureAt(x, blockToFreezeY, z) < WorldStandardValues.SNOW_AND_ICE_MAX_TEMP)
            {
                // Ice has to be placed one block in the world
                if (world.getMaterial(x, blockToFreezeY - 1, z).isLiquid())
                {
                    world.setBlock(x, blockToFreezeY - 1, z, biomeConfig.iceBlock);
                } else
                {
                    // Snow has to be placed on an empty space on a
                    // block that accepts snow in the world
                    if (world.isEmpty(x, blockToFreezeY, z))
                    {
                        if (world.getMaterial(x, blockToFreezeY - 1, z).canSnowFallOn())
                        {
                            world.setBlock(x, blockToFreezeY, z, snowMaterial);
                        }
                    }
                }
            }
        }
    }

}