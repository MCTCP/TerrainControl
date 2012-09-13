package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.Resource;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.util.MathHelper;

import java.util.Random;

public class OreGen extends ResourceGenBase
{
    @Override
    protected void SpawnResource(LocalWorld world, Random rand, Resource res, int x, int z)
    {
        int y = rand.nextInt(res.MaxAltitude - res.MinAltitude) + res.MinAltitude;

        float f = rand.nextFloat() * 3.141593F;

        double d1 = x + 8 + MathHelper.sin(f) * res.MaxSize / 8.0F;
        double d2 = x + 8 - MathHelper.sin(f) * res.MaxSize / 8.0F;
        double d3 = z + 8 + MathHelper.cos(f) * res.MaxSize / 8.0F;
        double d4 = z + 8 - MathHelper.cos(f) * res.MaxSize / 8.0F;

        double d5 = y + rand.nextInt(3) - 2;
        double d6 = y + rand.nextInt(3) - 2;

        for (int i = 0; i <= res.MaxSize; i++)
        {
            double d7 = d1 + (d2 - d1) * i / res.MaxSize;
            double d8 = d5 + (d6 - d5) * i / res.MaxSize;
            double d9 = d3 + (d4 - d3) * i / res.MaxSize;

            double d10 = rand.nextDouble() * res.MaxSize / 16.0D;
            double d11 = (MathHelper.sin(i * 3.141593F / res.MaxSize) + 1.0F) * d10 + 1.0D;
            double d12 = (MathHelper.sin(i * 3.141593F / res.MaxSize) + 1.0F) * d10 + 1.0D;

            int j = MathHelper.floor(d7 - d11 / 2.0D);
            int k = MathHelper.floor(d8 - d12 / 2.0D);
            int m = MathHelper.floor(d9 - d11 / 2.0D);

            int n = MathHelper.floor(d7 + d11 / 2.0D);
            int i1 = MathHelper.floor(d8 + d12 / 2.0D);
            int i2 = MathHelper.floor(d9 + d11 / 2.0D);

            for (int i3 = j; i3 <= n; i3++)
            {
                double d13 = (i3 + 0.5D - d7) / (d11 / 2.0D);
                if (d13 * d13 < 1.0D)
                {
                    for (int i4 = k; i4 <= i1; i4++)
                    {
                        double d14 = (i4 + 0.5D - d8) / (d12 / 2.0D);
                        if (d13 * d13 + d14 * d14 < 1.0D)
                        {
                            for (int i5 = m; i5 <= i2; i5++)
                            {
                                double d15 = (i5 + 0.5D - d9) / (d11 / 2.0D);
                                if ((d13 * d13 + d14 * d14 + d15 * d15 < 1.0D) && res.CheckSourceId(world.getTypeId(i3, i4, i5)))
                                {
                                    if (res.BlockData > 0)
                                    {
                                        world.setBlock(i3, i4, i5, res.BlockId, res.BlockData, false, false, false);
                                    } else
                                    {
                                        world.setBlock(i3, i4, i5, res.BlockId, 0, false, false, false);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
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

        res.MaxSize = CheckValue(Props[1], 1, 32);
        res.Frequency = CheckValue(Props[2], 1, 100);
        res.Rarity = CheckValue(Props[3], 0, 100);
        res.MinAltitude = CheckValue(Props[4], 0, biomeConfig.worldConfig.WorldHeight);
        res.MaxAltitude = CheckValue(Props[5], 0, biomeConfig.worldConfig.WorldHeight, res.MinAltitude);

        res.SourceBlockId = new int[Props.length - 6];
        for (int i = 6; i < Props.length; i++)
            res.SourceBlockId[i - 6] = CheckBlock(Props[i]);

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
        return blockId + "," + res.MaxSize + "," + res.Frequency + "," + res.Rarity + "," + res.MinAltitude + "," + res.MaxAltitude + blockSources;
    }
}
