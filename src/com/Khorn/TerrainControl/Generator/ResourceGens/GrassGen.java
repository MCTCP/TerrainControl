package com.Khorn.TerrainControl.Generator.ResourceGens;

import com.Khorn.TerrainControl.Configuration.Resource;
import net.minecraft.server.Block;
import net.minecraft.server.World;

public class GrassGen extends ResourceGenBase
{
    public GrassGen(World world)
    {
        super(world);
    }

    @Override
    protected void SpawnResource(Resource res, int x, int z)
    {

        int y = this.rand.nextInt(res.MaxAltitude - res.MinAltitude) + res.MinAltitude;

        int i;
        while ((((i = this.GetRawBlockId(x, y, z)) == 0) || (i == Block.LEAVES.id)) && (y > 0))
        {
            y--;
        }
        for (int j = 0; j < 128; j++)
        {
            int k = x + this.rand.nextInt(8) - this.rand.nextInt(8);
            int m = y + this.rand.nextInt(4) - this.rand.nextInt(4);
            int n = z + this.rand.nextInt(8) - this.rand.nextInt(8);
            if ((!this.isEmpty(k, m, n)) || (!res.CheckSourceId(this.GetRawBlockId(k, m - 1, n))))
                continue;
            this.SetRawBlockIdAndData(k, m, n, res.BlockId, res.BlockData);
        }

    }


}
