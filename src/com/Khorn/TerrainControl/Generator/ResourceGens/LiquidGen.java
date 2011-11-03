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
    protected void SpawnResource(Resource res, int x, int z)
    {

        int y = this.rand.nextInt(res.MaxAltitude - res.MinAltitude) + res.MinAltitude;

        if (res.CheckSourceId(this.GetRawBlockId(x, y + 1, z)))
            return;
        if (res.CheckSourceId(this.GetRawBlockId(x, y - 1, z)))
            return;

        if ((this.GetRawBlockId(x, y, z) != 0) && (res.CheckSourceId(this.GetRawBlockId(x, y, z))))
            return;


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

            this.world.setTypeId(x, y, z, res.BlockId);
            //this.world.f = true;
            //Block.byId[res.BlockId].a(this.world, x, y, z, this.rand);
            //this.world.f = false;


        }
    }
}
