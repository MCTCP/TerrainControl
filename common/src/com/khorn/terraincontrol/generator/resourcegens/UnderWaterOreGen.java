package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.Resource;

import java.util.Random;

public class UnderWaterOreGen extends ResourceGenBase
{
    @Override
    protected void SpawnResource(LocalWorld world, Random rand, Resource res, int x, int z)
    {

        int y = world.getLiquidHeight(x, z);

        int i = rand.nextInt(res.MaxSize);
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
                        int i3 = world.getTypeId(k, i2, m);
                        if (res.CheckSourceId(i3))
                        {
                            world.setBlock(k, i2, m, res.BlockId, 0, false, false, false);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected boolean ReadString(Resource res, String[] Props, int worldHeight) throws NumberFormatException
    {

        res.BlockId = CheckBlock(Props[0]);
        res.MaxSize = CheckValue(Props[1], 1, 8);
        res.Frequency = CheckValue(Props[2], 1, 100);
        res.Rarity = CheckValue(Props[3], 0, 100);


        res.SourceBlockId = new int[Props.length - 4];
        for (int i = 4; i < Props.length; i++)
            res.SourceBlockId[i - 4] = CheckBlock(Props[i]);

        return true;
    }

    @Override
    protected String WriteString(Resource res, String blockSources)
    {
        return res.BlockIdToName(res.BlockId) + "," + res.MaxSize + "," + res.Frequency + "," + res.Rarity + blockSources;
    }
}
