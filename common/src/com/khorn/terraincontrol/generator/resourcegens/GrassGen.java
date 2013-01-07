package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.exception.InvalidConfigException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GrassGen extends Resource
{
    private List<Integer> sourceBlocks;

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(5, args);

        blockId = readBlockId(args.get(0));
        blockData = readInt(args.get(1), 0, 16);
        frequency = readInt(args.get(2), 1, 500);
        rarity = readInt(args.get(3), 1, 100);
        sourceBlocks = new ArrayList<Integer>();
        for (int i = 4; i < args.size(); i++)
        {
            sourceBlocks.add(readBlockId(args.get(i)));
        }
    }

    @Override
    public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z)
    {
        // Handled by process().
    }

    @Override
    protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ)
    {
        for (int t = 0; t < frequency; t++)
        {
            if (random.nextInt(100) >= rarity)
                continue;
            int x = chunkX * 16 + random.nextInt(16) + 8;
            int z = chunkZ * 16 + random.nextInt(16) + 8;
            int y = world.getHighestBlockYAt(x, z);

            int i;
            while ((((i = world.getTypeId(x, y, z)) == 0) || (i == DefaultMaterial.LEAVES.id)) && (y > 0))
                y--;

            if ((!world.isEmpty(x, y + 1, z)) || (!sourceBlocks.contains(world.getTypeId(x, y, z))))
                continue;
            world.setBlock(x, y + 1, z, blockId, blockData, false, false, false);
        }
    }

    @Override
    public String makeString()
    {
        return "Grass(" + makeMaterial(blockId) + "," + blockData + "," + frequency + "," + rarity + makeMaterial(sourceBlocks) + ")";
    }
}