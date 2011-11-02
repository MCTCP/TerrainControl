package com.Khorn.TerrainControl.Generator.ResourceGens;

import com.Khorn.TerrainControl.Configuration.Resource;
import net.minecraft.server.World;

public class FlowerGen extends ResourceGenBase
{
    public FlowerGen(World world)
    {
        super(world);
    }

    @Override
    protected void SpawnResource(Resource res, int x, int z)
    {

            int y = this.rand.nextInt(res.MaxAltitude - res.MinAltitude) + res.MinAltitude;

            for (int i = 0; i < 64; i++)
            {
                int j = x + this.rand.nextInt(8) - this.rand.nextInt(8);
                int k = y + this.rand.nextInt(4) - this.rand.nextInt(4);
                int m = z + this.rand.nextInt(8) - this.rand.nextInt(8);
                if ((!this.isEmpty(j, k, m)) || (!res.CheckSourceId(this.GetRawBlockId(j, k, m))))
                    continue;
                this.SetRawBlockId(j, k, m, res.BlockId);
            }
        }


}
