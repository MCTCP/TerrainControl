package com.Khorn.TerrainControl.Generator.ResourceGens;

import com.Khorn.TerrainControl.Configuration.Resource;
import com.Khorn.TerrainControl.LocalWorld;

import java.util.Random;

public class DungeonGen extends ResourceGenBase
{
    @Override
    protected void SpawnResource(LocalWorld world, Random rand, Resource res, int x, int z)
    {
        int _y = rand.nextInt(res.MaxAltitude - res.MinAltitude) + res.MinAltitude;
        world.PlaceDungeons(rand, x, _y, z);
    }
}