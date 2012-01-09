package com.Khorn.TerrainControl.Generator.ResourceGens;

import com.Khorn.TerrainControl.Configuration.Resource;
import net.minecraft.server.World;

public class CactusGen extends ResourceGenBase
{
    public CactusGen(World world)
    {
        super(world);
    }

    @Override
    protected void SpawnResource(Resource res, int x, int z)
    {

        int y = this.rand.nextInt(res.MaxAltitude - res.MinAltitude) + res.MinAltitude;

        for (int i = 0; i < 10; i++)
        {
            int j = x + this.rand.nextInt(8) - this.rand.nextInt(8);
            int k = y + this.rand.nextInt(4) - this.rand.nextInt(4);
            int m = z + this.rand.nextInt(8) - this.rand.nextInt(8);
            if (this.isEmpty(j, k, m))
            {
                int n = 1 + this.rand.nextInt(this.rand.nextInt(3) + 1);
                for (int i1 = 0; i1 < n; i1++)
                {
                    int id =  this.GetRawBlockId( j, k + i1 - 1, m);
                    if (res.CheckSourceId(id) || id == res.BlockId )
                    {
                        this.SetRawBlockId(j, k + i1, m, res.BlockId);
                    }
                }
            }
        }

    }
}
