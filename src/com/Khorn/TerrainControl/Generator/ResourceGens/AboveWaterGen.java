package com.Khorn.TerrainControl.Generator.ResourceGens;

import com.Khorn.TerrainControl.Configuration.Resource;
import net.minecraft.server.Material;
import net.minecraft.server.World;


public class AboveWaterGen extends ResourceGenBase
{
    public AboveWaterGen(World world)
    {
        super(world);
    }

    @Override
    protected void SpawnResource(Resource res, int x, int z)
    {
        int y = this.world.getHighestBlockYAt(x, z);

        for (int i = 0; i < 10; i++)
        {
            int j = x + this.rand.nextInt(8) - this.rand.nextInt(8);
            int k = y + this.rand.nextInt(4) - this.rand.nextInt(4);
            int m = z + this.rand.nextInt(8) - this.rand.nextInt(8);
            if ((!this.isEmpty(j, k, m)) || (this.getMaterial(j, k - 1, m) != Material.WATER))
                continue;
            this.SetRawBlockId(j, k, m, res.BlockId);
        }
    }
}
