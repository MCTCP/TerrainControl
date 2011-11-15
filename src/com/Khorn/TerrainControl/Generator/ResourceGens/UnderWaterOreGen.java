package com.Khorn.TerrainControl.Generator.ResourceGens;

import com.Khorn.TerrainControl.Configuration.Resource;
import net.minecraft.server.Material;
import net.minecraft.server.World;


public class UnderWaterOreGen extends ResourceGenBase
{
    public UnderWaterOreGen(World world)
    {
        super(world);
    }

    @Override
    protected void SpawnResource(Resource res, int x, int z)
    {

        int y = this.world.f(x, z);

        if (this.getMaterial(x, y, z) != Material.WATER)
            return;
        int i = this.rand.nextInt(res.MaxSize);
        int j = 2;
        for (int k = x - i; k <= x + i; k++)
        {
            for (int m = z - i; m <= z + i; m++)
            {
                int n = k - x;
                int i1 = m - z;
                if (n * n + i1 * i1 <= i * i)
                {
                    for (int i2 = y - j; i2 <= y + j; i2++)
                    {
                        int i3 = this.GetRawBlockId(k, i2, m);
                        if (res.CheckSourceId(i3))
                        {
                            this.SetRawBlockId(k, i2, m, res.BlockId);
                        }
                    }
                }
            }

        }

    }
}
