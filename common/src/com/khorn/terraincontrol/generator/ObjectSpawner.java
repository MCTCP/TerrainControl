package com.khorn.terraincontrol.generator;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.Resource;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.resourcegens.ResourceType;

import java.util.Random;

public class ObjectSpawner
{
    private WorldConfig worldSettings;
    private Random rand;
    private LocalWorld world;

    public ObjectSpawner(WorldConfig wrk, LocalWorld localWorld)
    {
        this.worldSettings = wrk;
        this.rand = new Random();
        this.world = localWorld;
    }

    public void populate(int chunk_x, int chunk_z)
    {
        int x = chunk_x * 16;
        int z = chunk_z * 16;

        int biomeId = world.getBiome(x + 16, z + 16);
        BiomeConfig localBiomeConfig = this.worldSettings.biomeConfigs[biomeId];

        this.rand.setSeed(world.getSeed());
        long l1 = this.rand.nextLong() / 2L * 2L + 1L;
        long l2 = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed(chunk_x * l1 + chunk_z * l2 ^ world.getSeed());

        boolean Village = world.PlaceTerrainObjects(rand, chunk_x, chunk_z);


        //Resource sequence
        for (int i = 0; i < localBiomeConfig.ResourceCount; i++)
        {
            Resource res = localBiomeConfig.ResourceSequence[i];
            if(res.Type == ResourceType.SmallLake && Village)
                continue;
            world.setChunksCreations(res.Type.CreateNewChunks);
            res.Type.Generator.Process(world, rand, res, x, z, biomeId);
        }

        // Ice
        world.PlaceIce(x, z);


        world.DoBlockReplace();

        world.DoBiomeReplace();


        if (this.worldSettings.isDeprecated)
            this.worldSettings = this.worldSettings.newSettings;
    }
}