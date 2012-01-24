package com.Khorn.TerrainControl.Generator.ResourceGens;

import com.Khorn.TerrainControl.Configuration.Resource;
import com.Khorn.TerrainControl.LocalWorld;

import java.util.Random;

public class PlantGen extends ResourceGenBase
{
    @Override
    protected void SpawnResource(LocalWorld world, Random rand, Resource res, int x, int z)
    {

        int y = rand.nextInt(res.MaxAltitude - res.MinAltitude) + res.MinAltitude;

        for (int i = 0; i < 64; i++)
        {
            int j = x + rand.nextInt(8) - rand.nextInt(8);
            int k = y + rand.nextInt(4) - rand.nextInt(4);
            int m = z + rand.nextInt(8) - rand.nextInt(8);
            if ((!world.isEmpty(j, k, m)) || (!res.CheckSourceId(world.getRawBlockId(j, k - 1, m))))
                continue;

            if (res.BlockData > 0)
            {
                world.setRawBlockIdAndData(j, k, m, res.BlockId, res.BlockData);
            } else
            {
                world.setRawBlockId(j, k, m, res.BlockId);
            }
        }
    }


}
