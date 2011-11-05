package com.Khorn.TerrainControl.Generator.ResourceGens;

import com.Khorn.TerrainControl.Configuration.Resource;
import net.minecraft.server.World;
import net.minecraft.server.WorldGenDungeons;

public class DungeonGen extends ResourceGenBase
{
    public DungeonGen(World world)
    {
        super(world);
    }

    @Override
    protected void SpawnResource(Resource res, int _x, int _z)
    {
        int _y = this.rand.nextInt(res.MaxAltitude - res.MinAltitude) + res.MinAltitude;
        new WorldGenDungeons().a(this.world, this.rand, _x, _y, _z);
    }
}
