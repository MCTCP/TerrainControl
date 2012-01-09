package com.Khorn.TerrainControl.Generator.ResourceGens;

import com.Khorn.TerrainControl.Configuration.Resource;
import net.minecraft.server.Material;
import net.minecraft.server.World;

public class ReedGen extends ResourceGenBase
{
    public ReedGen(World world)
    {
        super(world);
    }

    @Override
    protected void SpawnResource(Resource res, int x, int z)
    {

        int y = this.rand.nextInt(res.MaxAltitude - res.MinAltitude) + res.MinAltitude;

        for (int i = 0; i < 20; i++)
        {
            int j = x + this.rand.nextInt(4) - this.rand.nextInt(4);
            int m = z + this.rand.nextInt(4) - this.rand.nextInt(4);
            if ((!this.isEmpty(j, y, m)) || ((this.getMaterial(j - 1, y - 1, m) != Material.WATER) && (this.getMaterial(j + 1, y - 1, m) != Material.WATER) && (this.getMaterial(j, y - 1, m - 1) != Material.WATER) && (this.getMaterial(j, y - 1, m + 1) != Material.WATER)))
            {
                continue;
            }

            int n = 2 + this.rand.nextInt(this.rand.nextInt(3) + 1);
            for (int i1 = 0; i1 < n; i1++)
            {

                int id = this.GetRawBlockId(j, y + i1 - 1, m);
                if (res.CheckSourceId(id) || id == res.BlockId)
                {
                    this.SetRawBlockId(j, y + i1, m, res.BlockId);
                }

            }

        }


    }
}
