package com.khorn.terraincontrol.generator;

import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.Resource;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;


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

        if (!Village)
        {
            if (!localBiomeConfig.disableNotchPonds)
            {

                if (this.rand.nextInt(4) == 0)
                {
                    int _x = x + this.rand.nextInt(16) + 8;
                    int _y = this.rand.nextInt(this.worldSettings.WorldHeight - 1);
                    int _z = z + this.rand.nextInt(16) + 8;
                    world.PlacePonds(DefaultMaterial.STATIONARY_WATER.id, this.rand, _x, _y, _z);
                }

                if (this.rand.nextInt(8) == 0)
                {
                    int _x = x + this.rand.nextInt(16) + 8;
                    int _y = this.rand.nextInt(this.rand.nextInt(this.worldSettings.WorldHeight - 9) + 8);
                    int _z = z + this.rand.nextInt(16) + 8;
                    if ((_y < this.worldSettings.waterLevelMax) || (this.rand.nextInt(10) == 0))
                        world.PlacePonds(DefaultMaterial.STATIONARY_LAVA.id, this.rand, _x, _y, _z);
                }
            }
        }


        //Resource sequence
        for (int i = 0; i < localBiomeConfig.ResourceCount; i++)
        {
            Resource res = localBiomeConfig.ResourceSequence[i];
            world.setChunksCreations(res.Type.CreateNewChunks);
            res.Type.Generator.Process(world, rand, res, x, z, biomeId);
        }

        // Ice
        world.PlaceIce(x, z);


        world.DoReplace();


        if (this.worldSettings.isDeprecated)
            this.worldSettings = this.worldSettings.newSettings;
    }
}