package com.Khorn.TerrainControl.Generator.ResourceGens;

import com.Khorn.TerrainControl.Configuration.Resource;
import com.Khorn.TerrainControl.LocalWorld;

import java.util.Random;

public class CactusGen extends ResourceGenBase
{
    @Override
    protected void SpawnResource(LocalWorld world, Random rand, Resource res, int x, int z)
    {
        int y = rand.nextInt(res.MaxAltitude - res.MinAltitude) + res.MinAltitude;

        for (int i = 0; i < 10; i++)
        {
            int j = x + rand.nextInt(8) - rand.nextInt(8);
            int k = y + rand.nextInt(4) - rand.nextInt(4);
            int m = z + rand.nextInt(8) - rand.nextInt(8);
            if (world.isEmpty(j, k, m))
            {
                int n = 1 + rand.nextInt(rand.nextInt(3) + 1);
                for (int i1 = 0; i1 < n; i1++)
                {
                    int id = world.getRawBlockId(j, k + i1 - 1, m);
                    if (res.CheckSourceId(id) || id == res.BlockId)
                    {
                        world.setRawBlockId(j, k + i1, m, res.BlockId);
                    }
                }
            }
        }
    }
}