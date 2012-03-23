package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.configuration.Resource;
import com.khorn.terraincontrol.LocalWorld;

import java.util.Random;

public class TreeGen extends ResourceGenBase
{
    @Override
    protected void SpawnResource(LocalWorld world, Random rand, Resource res, int x, int z)
    {

    }

    @Override
    public void Process(LocalWorld world, Random rand, Resource res, int x, int z, int biomeId)
    {
        for (int i = 0; i < res.Frequency; i++)
        {

            int _x = x + rand.nextInt(16) + 8;
            int _z = z + rand.nextInt(16) + 8;
            int _y = world.getHighestBlockYAt(_x, _z);

            for (int t = 0; t < res.TreeTypes.length; t++)
                if (rand.nextInt(100) < res.TreeChances[t])
                    world.PlaceTree(res.TreeTypes[t], rand, _x, _y, _z);


        }
    }
}
