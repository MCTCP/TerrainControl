package com.Khorn.TerrainControl.Generator.ResourceGens;

import com.Khorn.TerrainControl.Configuration.Resource;
import net.minecraft.server.Block;
import net.minecraft.server.World;

public class LiquidGen extends ResourceGenBase 
{
    public LiquidGen(World world)
    {
        super(world);
    }

    @Override
    protected void SpawnResource(Resource res, int _x, int _z)
    {
        for (int t = 0; t < res.Frequency; t++)
        {
            if (this.rand.nextInt(100) >= res.Rarity)
                continue;

            int x = _x + this.rand.nextInt(16)+8;
            int z = _z + this.rand.nextInt(16)+8;
            int y = this.rand.nextInt(res.MaxAltitude - res.MinAltitude) + res.MinAltitude;

            if (res.CheckSourceId(this.GetRawBlockId(x, y + 1, z)))
                continue;
            if (res.CheckSourceId(this.GetRawBlockId(x, y - 1, z)))
                continue;

            if ((this.GetRawBlockId(x, y, z) != 0) && (res.CheckSourceId(this.GetRawBlockId(x, y, z))))
                continue;


            int i = 0;
            int j = 0;

            int tempBlock = this.GetRawBlockId(x - 1, y, z);

            i = (res.CheckSourceId(tempBlock)) ? i + 1 : i;
            j = (tempBlock == 0) ? j + 1 : j;

            tempBlock = this.GetRawBlockId(x + 1, y, z);

            i = (res.CheckSourceId(tempBlock)) ? i + 1 : i;
            j = (tempBlock == 0) ? j + 1 : j;

            tempBlock = this.GetRawBlockId(x, y, z - 1);

            i = (res.CheckSourceId(tempBlock)) ? i + 1 : i;
            j = (tempBlock == 0) ? j + 1 : j;

            tempBlock = this.GetRawBlockId(x, y, z + 1);

            i = (res.CheckSourceId(tempBlock)) ? i + 1 : i;
            j = (tempBlock == 0) ? j + 1 : j;


            if ((i == 3) && (j == 1))
            {
                this.world.setRawTypeId(x, y, z, res.BlockId);

            }
        }
    }
}
