package com.Khorn.TerrainControl.Generator.ResourceGens;

import com.Khorn.TerrainControl.Configuration.Resource;
import net.minecraft.server.Block;
import net.minecraft.server.World;

import java.util.Random;

public class GrassGen extends ResourceGenBase
{
    public GrassGen(World world)
    {
        super(world);
    }

    @Override
    protected void SpawnResource(Resource res, int x, int z)
    {
    }

    @Override
    public void Process(Random _rand, Resource res, int _x, int _z)
    {
        this.rand = _rand;


        for (int t = 0; t < res.Frequency; t++)
        {
            if (this.rand.nextInt(100) >= res.Rarity)
                continue;
            int x = _x + this.rand.nextInt(16) + 8;
            int y = 128;
            int z = _z + this.rand.nextInt(16) + 8;

            int i;
            while ((((i = this.GetRawBlockId(x, y, z)) == 0) || (i == Block.LEAVES.id)) && (y > 0))
                y--;

            if ((!this.isEmpty(x, y + 1, z)) || (!res.CheckSourceId(this.GetRawBlockId(x, y , z))))
                continue;
            this.SetRawBlockIdAndData(x, y +1, z, res.BlockId, res.BlockData);
        }
    }
}
