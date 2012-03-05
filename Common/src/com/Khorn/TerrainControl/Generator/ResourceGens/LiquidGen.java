package com.Khorn.TerrainControl.Generator.ResourceGens;

import com.Khorn.TerrainControl.LocalWorld;

import com.Khorn.TerrainControl.Configuration.Resource;

import java.util.Random;

public class LiquidGen extends ResourceGenBase
{
    @Override
    protected void SpawnResource(LocalWorld world, Random rand, Resource res, int x, int z)
    {
        int y = rand.nextInt(res.MaxAltitude - res.MinAltitude) + res.MinAltitude;

        if (res.CheckSourceId(world.getRawBlockId(x, y + 1, z)))
            return;
        if (res.CheckSourceId(world.getRawBlockId(x, y - 1, z)))
            return;

        if ((world.getRawBlockId(x, y, z) != 0) && (res.CheckSourceId(world.getRawBlockId(x, y, z))))
            return;


        int i = 0;
        int j = 0;

        int tempBlock = world.getRawBlockId(x - 1, y, z);

        i = (res.CheckSourceId(tempBlock)) ? i + 1 : i;
        j = (tempBlock == 0) ? j + 1 : j;

        tempBlock = world.getRawBlockId(x + 1, y, z);

        i = (res.CheckSourceId(tempBlock)) ? i + 1 : i;
        j = (tempBlock == 0) ? j + 1 : j;

        tempBlock = world.getRawBlockId(x, y, z - 1);

        i = (res.CheckSourceId(tempBlock)) ? i + 1 : i;
        j = (tempBlock == 0) ? j + 1 : j;

        tempBlock = world.getRawBlockId(x, y, z + 1);

        i = (res.CheckSourceId(tempBlock)) ? i + 1 : i;
        j = (tempBlock == 0) ? j + 1 : j;


        if ((i == 3) && (j == 1))
        {

            world.setBlockId(x, y, z, res.BlockId);
            //this.world.f = true;
            //Block.byId[res.BlockId].a(this.world, x, y, z, this.rand);
            //this.world.f = false;
        }
    }
}