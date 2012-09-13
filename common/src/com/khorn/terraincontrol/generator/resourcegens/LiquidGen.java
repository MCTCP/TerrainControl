package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;

import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.Resource;

import java.util.Random;

public class LiquidGen extends ResourceGenBase
{
    @Override
    protected void SpawnResource(LocalWorld world, Random rand, Resource res, int x, int z)
    {
        int y = rand.nextInt(res.MaxAltitude - res.MinAltitude) + res.MinAltitude;

        if (res.CheckSourceId(world.getTypeId(x, y + 1, z)))
            return;
        if (res.CheckSourceId(world.getTypeId(x, y - 1, z)))
            return;

        if ((world.getTypeId(x, y, z) != 0) && (res.CheckSourceId(world.getTypeId(x, y, z))))
            return;


        int i = 0;
        int j = 0;

        int tempBlock = world.getTypeId(x - 1, y, z);

        i = (res.CheckSourceId(tempBlock)) ? i + 1 : i;
        j = (tempBlock == 0) ? j + 1 : j;

        tempBlock = world.getTypeId(x + 1, y, z);

        i = (res.CheckSourceId(tempBlock)) ? i + 1 : i;
        j = (tempBlock == 0) ? j + 1 : j;

        tempBlock = world.getTypeId(x, y, z - 1);

        i = (res.CheckSourceId(tempBlock)) ? i + 1 : i;
        j = (tempBlock == 0) ? j + 1 : j;

        tempBlock = world.getTypeId(x, y, z + 1);

        i = (res.CheckSourceId(tempBlock)) ? i + 1 : i;
        j = (tempBlock == 0) ? j + 1 : j;


        if ((i == 3) && (j == 1))
        {
            world.setBlock(x, y, z, res.BlockId, 0, true, true, true);
            //this.world.f = true;
            //Block.byId[res.BlockId].a(this.world, x, y, z, this.rand);
            //this.world.f = false;
        }
    }

    @Override
    protected boolean ReadString(Resource res, String[] Props, BiomeConfig biomeConfig) throws NumberFormatException
    {
        res.BlockId = CheckBlock(Props[0]);
        res.Frequency = CheckValue(Props[1], 1, 5000);
        res.Rarity = CheckValue(Props[2], 0, 100);
        res.MinAltitude = CheckValue(Props[3], 0, biomeConfig.worldConfig.WorldHeight);
        res.MaxAltitude = CheckValue(Props[4], 0, biomeConfig.worldConfig.WorldHeight, res.MinAltitude);

        res.SourceBlockId = new int[Props.length - 5];
        for (int i = 5; i < Props.length; i++)
            res.SourceBlockId[i - 5] = CheckBlock(Props[i]);

        return true;
    }

    @Override
    protected String WriteString(Resource res, String blockSources)
    {
        String blockId = res.BlockIdToName(res.BlockId);
        if (res.BlockData > 0)
        {
            blockId += "." + res.BlockData;
        }
        return blockId + "," + res.Frequency + "," + res.Rarity + "," + res.MinAltitude + "," + res.MaxAltitude + blockSources;
    }
}