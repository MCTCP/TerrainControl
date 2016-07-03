package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.exception.InvalidConfigException;

import java.util.List;
import java.util.Random;

public class AboveWaterGen extends Resource
{
    public AboveWaterGen(BiomeConfig config, List<String> args) throws InvalidConfigException
    {
        super(config);
        assureSize(3, args);

        material = readMaterial(args.get(0));
        frequency = readInt(args.get(1), 1, 100);
        rarity = readRarity(args.get(2));
    }

    @Override
    public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z)
    {
        int y = world.getLiquidHeight(x, z);
        if (y == -1)
            return;

        for (int i = 0; i < 10; i++)
        {
            int j = x + rand.nextInt(8) - rand.nextInt(8);
            int k = y + rand.nextInt(4) - rand.nextInt(4);
            int m = z + rand.nextInt(8) - rand.nextInt(8);
            if (!world.isEmpty(j, k, m) || !world.getMaterial(j, k - 1, m).isLiquid())
                continue;
            world.setBlock(j, k, m, material);
        }
    }

    @Override
    public String toString()
    {
        return "AboveWaterRes(" + material + "," + frequency + "," + rarity + ")";
    }

    @Override
    public int getPriority()
    {
        return -11;
    }
}
