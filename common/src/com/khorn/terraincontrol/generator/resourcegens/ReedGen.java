package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.Resource;

import java.util.Random;

public class ReedGen extends ResourceGenBase
{

    @Override
    protected void SpawnResource(LocalWorld world, Random rand, Resource res, int x, int z)
    {
        int y = world.getHighestBlockYAt(x, z);
        if (y > res.MaxAltitude || y < res.MinAltitude || (!world.getMaterial(x - 1, y - 1, z).isLiquid() && !world.getMaterial(x + 1, y - 1, z).isLiquid() && !world.getMaterial(x, y - 1, z - 1).isLiquid() && !world.getMaterial(x, y - 1, z + 1).isLiquid()))
        {
            return;
        }
        if (!res.CheckSourceId(world.getTypeId(x, y - 1, z)))
        {
            return;
        }
        
        int n = 1 + rand.nextInt(2);
        for (int i1 = 0; i1 < n; i1++)
            world.setBlock(x, y + i1, z, res.BlockId, res.BlockData, false, false, false);
    }

    @Override
    protected boolean ReadString(Resource res, String[] Props, BiomeConfig biomeConfig) throws NumberFormatException
    {
        if (Props[0].contains("."))
        {
            String[] block = Props[0].split("\\.");
            res.BlockId = CheckBlock(block[0]);
            res.BlockData = CheckValue(block[1], 0, 16);
        } else
        {
            res.BlockId = CheckBlock(Props[0]);
        }

        res.Frequency = CheckValue(Props[1], 1, 100);
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