package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.configuration.Resource;
import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;


import java.util.Random;

public class ReedGen extends ResourceGenBase
{

    @Override
    protected void SpawnResource(LocalWorld world, Random rand, Resource res, int x, int z)
    {

        int y = rand.nextInt(res.MaxAltitude - res.MinAltitude) + res.MinAltitude;

        for (int i = 0; i < 20; i++)
        {
            int j = x + rand.nextInt(4) - rand.nextInt(4);
            int m = z + rand.nextInt(4) - rand.nextInt(4);
            if ((!world.isEmpty(j, y, m)) || ((world.getMaterial(j - 1, y - 1, m) != DefaultMaterial.WATER) && (world.getMaterial(j + 1, y - 1, m) != DefaultMaterial.WATER) && (world.getMaterial(j, y - 1, m - 1) != DefaultMaterial.WATER) && (world.getMaterial(j, y - 1, m + 1) != DefaultMaterial.WATER)))
            {
                continue;
            }

            int n = 2 + rand.nextInt(rand.nextInt(3) + 1);
            for (int i1 = 0; i1 < n; i1++)
            {

                int id = world.getTypeId(j, y + i1 - 1, m);
                if (res.CheckSourceId(id) || id == res.BlockId)
                {
                    world.setBlock(j, y + i1, m, res.BlockId, 0, false, false, false);
                }
            }
        }
    }
}