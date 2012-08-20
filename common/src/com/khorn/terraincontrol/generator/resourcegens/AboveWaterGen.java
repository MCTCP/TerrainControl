package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.Resource;

import java.util.Random;

public class AboveWaterGen extends ResourceGenBase
{
    @Override
    protected void SpawnResource(LocalWorld world, Random rand, Resource res, int x, int z)
    {
        int y = world.getLiquidHeight(x, z);
        if (y == -1)
            return;
        y++;

        for (int i = 0; i < 10; i++)
        {
            int j = x + rand.nextInt(8) - rand.nextInt(8);
            //int k = y + rand.nextInt(4) - rand.nextInt(4);
            int m = z + rand.nextInt(8) - rand.nextInt(8);
            if (!world.isEmpty(j, y, m) || !world.getMaterial(j, y - 1, m).isLiquid())
                continue;
            world.setBlock(j, y, m, res.BlockId, 0, false, false, false);
        }
    }

    @Override
    protected boolean ReadString(Resource res, String[] Props, int worldHeight) throws NumberFormatException
    {
        res.BlockId = CheckBlock(Props[0]);
        res.Frequency = CheckValue(Props[1], 1, 100);
        res.Rarity = CheckValue(Props[2], 0, 100);
        return true;
    }

    @Override
    protected String WriteString(Resource res, String blockSources)
    {
        return res.BlockIdToName(res.BlockId) + "," + res.Frequency + "," + res.Rarity;
    }
}
