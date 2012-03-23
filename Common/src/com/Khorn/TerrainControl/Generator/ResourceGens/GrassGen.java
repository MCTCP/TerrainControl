package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.configuration.Resource;
import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;

import java.util.Random;

public class GrassGen extends ResourceGenBase
{
    @Override
    public void Process(LocalWorld world, Random rand, Resource res, int _x, int _z, int biomeId)
    {

        for (int t = 0; t < res.Frequency; t++)
        {
            if (rand.nextInt(100) >= res.Rarity)
                continue;
            int x = _x + rand.nextInt(16) + 8;
            int y = world.getHeight();
            int z = _z + rand.nextInt(16) + 8;

            int i;
            while ((((i = world.getTypeId(x, y, z)) == 0) || (i == DefaultMaterial.LEAVES.id)) && (y > 0))
                y--;

            if ((!world.isEmpty(x, y + 1, z)) || (!res.CheckSourceId(world.getTypeId(x, y, z))))
                continue;
            world.setBlock(x, y + 1, z, res.BlockId, res.BlockData, false, false, false);
        }
    }

    @Override
    protected void SpawnResource(LocalWorld world, Random rand, Resource res, int x, int z)
    {

    }
}