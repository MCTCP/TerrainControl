package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReedGen extends Resource
{
    private int minAltitude;
    private int maxAltitude;
    private List<Integer> sourceBlocks;

    @Override
    public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z)
    {
        int y = world.getHighestBlockYAt(x, z);
        if (y > maxAltitude || y < minAltitude || (!world.getMaterial(x - 1, y - 1, z).isLiquid() && !world.getMaterial(x + 1, y - 1, z).isLiquid() && !world.getMaterial(x, y - 1, z - 1).isLiquid() && !world.getMaterial(x, y - 1, z + 1).isLiquid()))
        {
            return;
        }
        if (!sourceBlocks.contains(world.getTypeId(x, y - 1, z)))
        {
            return;
        }

        int n = 1 + rand.nextInt(2);
        for (int i1 = 0; i1 < n; i1++)
            world.setBlock(x, y + i1, z, blockId, blockData, false, false, false);
    }

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(6, args);

        blockId = readBlockId(args.get(0));
        blockData = readBlockData(args.get(0));
        frequency = readInt(args.get(1), 1, 100);
        rarity = readInt(args.get(2), 1, 100);
        minAltitude = readInt(args.get(3), TerrainControl.worldDepth, TerrainControl.worldHeight);
        maxAltitude = readInt(args.get(4), minAltitude + 1, TerrainControl.worldHeight);
        sourceBlocks = new ArrayList<Integer>();
        for (int i = 5; i < args.size(); i++)
        {
            sourceBlocks.add(readBlockId(args.get(i)));
        }
    }

    @Override
    public String makeString()
    {
        return "Reed(" + makeMaterial(blockId, blockData) + "," + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + makeMaterial(sourceBlocks) + ")";
    }
}