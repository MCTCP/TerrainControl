package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.configuration.Resource;
import com.khorn.terraincontrol.LocalWorld;

import java.util.Random;

public abstract class ResourceGenBase
{
    public void Process(LocalWorld world, Random rand, Resource res, int _x, int _z, int biomeId)
    {
        for (int t = 0; t < res.Frequency; t++)
        {
            if (rand.nextInt(100) > res.Rarity)
                continue;
            int x = _x + rand.nextInt(16) + 8;
            int z = _z + rand.nextInt(16) + 8;
            SpawnResource(world, rand, res, x, z);
        }

    }

    protected abstract void SpawnResource(LocalWorld world, Random rand, Resource res, int x, int z);
}