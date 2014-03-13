package com.khorn.terraincontrol.generator;

import com.khorn.terraincontrol.LocalMaterialData;

import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldSettings;
import com.khorn.terraincontrol.generator.noise.NoiseGeneratorNewOctaves;
import com.khorn.terraincontrol.generator.resource.Resource;
import com.khorn.terraincontrol.logging.LogMarker;
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

    public void populate(int chunkX, int chunkZ)
    {
        // Get the corner block coords
        int x = chunkX * 16;
        int z = chunkZ * 16;

        // Get the biome of the other corner
        LocalBiome biome = world.getCalculatedBiome(x + 15, z + 15);

        // Null check
        if (biome == null)
        {
            TerrainControl.log(LogMarker.DEBUG, "Unknown biome at {},{}  (chunk {},{}). Population failed.", x + 15, z + 15, chunkX, chunkZ);
            return;
        }

        BiomeConfig biomeConfig = biome.getBiomeConfig();

        // Get the random generator
        long resourcesSeed = worldSettings.worldConfig.resourcesSeed != 0L ? worldSettings.worldConfig.resourcesSeed : world.getSeed();
        this.rand.setSeed(resourcesSeed);
        long l1 = this.rand.nextLong() / 2L * 2L + 1L;
        long l2 = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed(chunkX * l1 + chunkZ * l2 ^ resourcesSeed);

        // Generate structures
        boolean hasGeneratedAVillage = world.placeDefaultStructures(rand, chunkX, chunkZ);

        // Fire event
        TerrainControl.firePopulationStartEvent(world, rand, hasGeneratedAVillage, chunkX, chunkZ);
        
        // Complex surface blocks
        placeComplexSurfaceBlocks(chunkX, chunkZ);

        // Resource sequence
        for (Resource res : biomeConfig.resourceSequence)
        {
            world.setChunksCreations(false);
            res.process(world, rand, hasGeneratedAVillage, chunkX, chunkZ);
        }

        // Animals
        world.placePopulationMobs(biome, rand, chunkX, chunkZ);

        // Snow and ice
        freezeChunk(chunkX, chunkZ);

        // Replace blocks
        world.replaceBlocks();

        // Fire event
        TerrainControl.firePopulationEndEvent(world, rand, hasGeneratedAVillage, chunkX, chunkZ);
    }
    
    protected void placeComplexSurfaceBlocks(int chunkX, int chunkZ)
    {
        this.reusableChunkNoiseArray = this.noiseGen.a(this.reusableChunkNoiseArray, chunkX * 16, chunkZ * 16, 16, 16, 0.0625D, 0.0625D, 1.0D);

        int x = chunkX * 16 + 8;
        int z = chunkZ * 16 + 8;
        for (int i = 0; i < 16; i++)
        {
            for (int j = 0; j < 16; j++)
            {
                int blockToFreezeX = x + i;
                int blockToFreezeZ = z + j;
                // Using the calculated biome id so that ReplaceToBiomeName can't mess up the ids
                LocalBiome biome = this.world.getCalculatedBiome(blockToFreezeX, blockToFreezeZ);
                if (biome != null && biome.getBiomeConfig().surfaceAndGroundControl != null)
                {
                    double noise = this.reusableChunkNoiseArray[i + j * 16];
                    biome.getBiomeConfig().surfaceAndGroundControl.spawn(world, noise, blockToFreezeX, blockToFreezeZ);
                }
            }
        }
    }

    protected void freezeChunk(int chunkX, int chunkZ)
    {
        LocalMaterialData snowMaterial = TerrainControl.toLocalMaterialData(DefaultMaterial.SNOW, 0);
        int x = chunkX * 16 + 8;
        int z = chunkZ * 16 + 8;
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
            if (blockToFreezeY > 0 && biome.getTemperatureAt(x, blockToFreezeY, z) < WorldStandardValues.snowAndIceMaxTemp.floatValue())
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