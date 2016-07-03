package com.khorn.terraincontrol.generator;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.configuration.ConfigProvider;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.noise.NoiseGeneratorNewOctaves;
import com.khorn.terraincontrol.generator.resource.Resource;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.ChunkCoordinate;

import java.util.Random;

public class ObjectSpawner
{

    private final ConfigProvider configProvider;
    private final Random rand;
    private final LocalWorld world;

    public ObjectSpawner(ConfigProvider configProvider, LocalWorld localWorld)
    {
        this.configProvider = configProvider;
        this.rand = new Random();
        this.world = localWorld;
        new NoiseGeneratorNewOctaves(new Random(world.getSeed()), 4);
    }

    public void populate(ChunkCoordinate chunkCoord)
    {
        // Get the corner block coords
        int x = chunkCoord.getChunkX() * 16;
        int z = chunkCoord.getChunkZ() * 16;

        // Get the biome of the other corner
        LocalBiome biome = world.getBiome(x + 15, z + 15);

        // Null check
        if (biome == null)
        {
            TerrainControl.log(LogMarker.DEBUG, "Unknown biome at {},{}  (chunk {}). Population failed.", x + 15, z + 15, chunkCoord);
            return;
        }

        BiomeConfig biomeConfig = biome.getBiomeConfig();

        // Get the random generator
        WorldConfig worldConfig = configProvider.getWorldConfig();
        long resourcesSeed = worldConfig.resourcesSeed != 0L ? worldConfig.resourcesSeed : world.getSeed();
        this.rand.setSeed(resourcesSeed);
        long l1 = this.rand.nextLong() / 2L * 2L + 1L;
        long l2 = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed(chunkCoord.getChunkX() * l1 + chunkCoord.getChunkZ() * l2 ^ resourcesSeed);

        // Generate structures
        boolean hasVillage = world.placeDefaultStructures(rand, chunkCoord);

        // Mark population started
        world.startPopulation(chunkCoord);
        TerrainControl.firePopulationStartEvent(world, rand, hasVillage,
                chunkCoord);

        // Resource sequence
        for (ConfigFunction<BiomeConfig> res : biomeConfig.resourceSequence)
        {
            if (res instanceof Resource)
                ((Resource) res).process(world, rand, hasVillage, chunkCoord);
        }

        // Animals
        world.placePopulationMobs(biome, rand, chunkCoord);

        // Snow and ice
        new FrozenSurfaceHelper(world).freezeChunk(chunkCoord);

        // Replace blocks
        world.replaceBlocks(chunkCoord);

        // Mark population ended
        TerrainControl.firePopulationEndEvent(world, rand, hasVillage, chunkCoord);
        world.endPopulation();
    }

}