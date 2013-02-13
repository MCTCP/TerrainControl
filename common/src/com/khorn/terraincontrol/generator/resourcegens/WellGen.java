package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;

import java.util.List;
import java.util.Random;

public class WellGen extends Resource
{
    private int minAltitude;
    private int maxAltitude;

    private int slabId;
    private int slabData; // 1
    private int waterId;
    private int waterData;

    private List<Integer> sourceBlocks;

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(8, args);

        blockId = readBlockId(args.get(0));
        blockData = readBlockData(args.get(0));
        slabId = readBlockId(args.get(1));
        slabData = readBlockData(args.get(1));
        waterId = readBlockId(args.get(2));
        waterData = readBlockData(args.get(2));
        frequency = readInt(args.get(3), 1, 100);
        rarity = readRarity(args.get(4));
        minAltitude = readInt(args.get(5), TerrainControl.worldDepth, TerrainControl.worldHeight);
        maxAltitude = readInt(args.get(6), minAltitude + 1, TerrainControl.worldHeight);
        sourceBlocks = this.readBlockIds(args, 7);
    }

    @Override
    public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z)
    {
        int y = random.nextInt(maxAltitude - minAltitude) + minAltitude;

        while (world.isEmpty(x, y, z) && y > minAltitude)
        {
            --y;
        }

        int sourceBlock = world.getTypeId(x, y, z);

        if (!sourceBlocks.contains(sourceBlock))
        {
            return;
        }
        int i;
        int j;

        for (i = -2; i <= 2; ++i)
        {
            for (j = -2; j <= 2; ++j)
            {
                if (world.isEmpty(x + i, y - 1, z + j) && world.isEmpty(x + i, y - 2, z + j))
                {
                    return;
                }
            }
        }

        for (i = -1; i <= 0; ++i)
        {
            for (j = -2; j <= 2; ++j)
            {
                for (int var9 = -2; var9 <= 2; ++var9)
                {
                    world.setBlock(x + j, y + i, z + var9, blockId, blockData);
                }
            }
        }

        world.setBlock(x, y, z, waterId, waterData);
        world.setBlock(x - 1, y, z, waterId, waterData);
        world.setBlock(x + 1, y, z, waterId, waterData);
        world.setBlock(x, y, z - 1, waterId, waterData);
        world.setBlock(x, y, z + 1, waterId, waterData);

        for (i = -2; i <= 2; ++i)
        {
            for (j = -2; j <= 2; ++j)
            {
                if (i == -2 || i == 2 || j == -2 || j == 2)
                {
                    world.setBlock(x + i, y + 1, z + j, blockId, blockData);
                }
            }
        }

        world.setBlock(x + 2, y + 1, z, slabId, slabData);
        world.setBlock(x - 2, y + 1, z, slabId, slabData);
        world.setBlock(x, y + 1, z + 2, slabId, slabData);
        world.setBlock(x, y + 1, z - 2, slabId, slabData);

        for (i = -1; i <= 1; ++i)
        {
            for (j = -1; j <= 1; ++j)
            {
                if (i == 0 && j == 0)
                {
                    world.setBlock(x + i, y + 4, z + j, blockId, blockData);
                } else
                {
                    world.setBlock(x + i, y + 4, z + j, slabId, slabData);
                }
            }
        }

        for (i = 1; i <= 3; ++i)
        {
            world.setBlock(x - 1, y + i, z - 1, blockId, blockData);
            world.setBlock(x - 1, y + i, z + 1, blockId, blockData);
            world.setBlock(x + 1, y + i, z - 1, blockId, blockData);
            world.setBlock(x + 1, y + i, z + 1, blockId, blockData);
        }
    }

    @Override
    public String makeString()
    {
        String output = "Well(" + makeMaterial(blockId, blockData) + "," + makeMaterial(slabId, slabData) + "," + makeMaterial(waterId, waterData) + ",";
        output += frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + this.makeMaterial(sourceBlocks) + ")";
        return output;
    }
}