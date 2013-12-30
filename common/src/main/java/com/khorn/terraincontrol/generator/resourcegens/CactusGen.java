package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CactusGen extends Resource
{
    private int minAltitude;
    private int maxAltitude;
    private List<Integer> sourceBlocks;

    @Override
    public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z)
    {
        int y = rand.nextInt(maxAltitude - minAltitude) + minAltitude;

        for (int i = 0; i < 10; i++)
        {
            int cactusX = x + rand.nextInt(8) - rand.nextInt(8);
            int cactusBaseY = y + rand.nextInt(4) - rand.nextInt(4);
            int cactusZ = z + rand.nextInt(8) - rand.nextInt(8);

            // Check position
            if (!world.isEmpty(cactusX, cactusBaseY, cactusZ))
                continue;

            // Check foundation
            int id = world.getTypeId(cactusX, cactusBaseY - 1, cactusZ);
            if (!sourceBlocks.contains(id))
                continue;

            // Check neighbors
            if (!world.isEmpty(cactusX - 1, cactusBaseY, cactusZ))
                continue;
            if (!world.isEmpty(cactusX + 1, cactusBaseY, cactusZ))
                continue;
            if (!world.isEmpty(cactusX, cactusBaseY, cactusZ + 1))
                continue;
            if (!world.isEmpty(cactusX, cactusBaseY, cactusZ + 1))
                continue;

            // Spawn cactus
            int cactusHeight = 1 + rand.nextInt(rand.nextInt(3) + 1);
            for (int dY = 0; dY < cactusHeight; dY++)
            {
                world.setBlock(cactusX, cactusBaseY + dY, cactusZ, blockId, blockData, false, false, false);
            }
        }
    }

    @Override
    public String makeString()
    {
        return "Cactus(" + makeMaterial(blockId, blockData) + "," + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + makeMaterial(sourceBlocks) + ")";
    }

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(6, args);

        blockId = readBlockId(args.get(0));
        blockData = readBlockData(args.get(0));
        frequency = readInt(args.get(1), 1, 100);
        rarity = readRarity(args.get(2));
        minAltitude = readInt(args.get(3), TerrainControl.worldDepth, TerrainControl.worldHeight);
        maxAltitude = readInt(args.get(4), minAltitude + 1, TerrainControl.worldHeight);
        sourceBlocks = new ArrayList<Integer>();
        for (int i = 5; i < args.size(); i++)
        {
            sourceBlocks.add(readBlockId(args.get(i)));
        }
    }
}
