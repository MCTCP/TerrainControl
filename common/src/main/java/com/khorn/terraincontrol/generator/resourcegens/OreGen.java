package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OreGen extends Resource
{
    private int minAltitude;
    private int maxAltitude;
    private int maxSize;
    private List<Integer> sourceBlocks;

    @Override
    public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z)
    {
        int y = rand.nextInt(maxAltitude - minAltitude) + minAltitude;

        float f = rand.nextFloat() * 3.141593F;

        double d1 = x + 8 + MathHelper.sin(f) * maxSize / 8.0F;
        double d2 = x + 8 - MathHelper.sin(f) * maxSize / 8.0F;
        double d3 = z + 8 + MathHelper.cos(f) * maxSize / 8.0F;
        double d4 = z + 8 - MathHelper.cos(f) * maxSize / 8.0F;

        double d5 = y + rand.nextInt(3) - 2;
        double d6 = y + rand.nextInt(3) - 2;

        for (int i = 0; i <= maxSize; i++)
        {
            double d7 = d1 + (d2 - d1) * i / maxSize;
            double d8 = d5 + (d6 - d5) * i / maxSize;
            double d9 = d3 + (d4 - d3) * i / maxSize;

            double d10 = rand.nextDouble() * maxSize / 16.0D;
            double d11 = (MathHelper.sin(i * 3.141593F / maxSize) + 1.0F) * d10 + 1.0D;
            double d12 = (MathHelper.sin(i * 3.141593F / maxSize) + 1.0F) * d10 + 1.0D;

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
                                if ((d13 * d13 + d14 * d14 + d15 * d15 < 1.0D) && sourceBlocks.contains(world.getTypeId(i3, i4, i5)))
                                {
                                    world.setBlock(i3, i4, i5, blockId, blockData, false, false, false);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(7, args);

        blockId = readBlockId(args.get(0));
        blockData = readBlockData(args.get(0));
        maxSize = readInt(args.get(1), 1, 128);
        frequency = readInt(args.get(2), 1, 100);
        rarity = readRarity(args.get(3));
        minAltitude = readInt(args.get(4), TerrainControl.worldDepth, TerrainControl.worldHeight);
        maxAltitude = readInt(args.get(5), minAltitude + 1, TerrainControl.worldHeight);
        sourceBlocks = new ArrayList<Integer>();
        for (int i = 6; i < args.size(); i++)
        {
            sourceBlocks.add(readBlockId(args.get(i)));
        }
    }

    @Override
    public String makeString()
    {
        return "Ore(" + makeMaterial(blockId, blockData) + "," + maxSize + "," + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + makeMaterial(sourceBlocks) + ")";
    }
}
