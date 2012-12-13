package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.exception.InvalidResourceException;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlantGen extends Resource
{
    private int blockId;
    private int blockData;
    private int minAltitude;
    private int maxAltitude;
    private List<Integer> sourceBlocks;

    @Override
    public void spawn(LocalWorld world, Random rand, int x, int z)
    {
        int y = rand.nextInt(maxAltitude - minAltitude) + minAltitude;

        for (int i = 0; i < 64; i++)
        {
            int j = x + rand.nextInt(8) - rand.nextInt(8);
            int k = y + rand.nextInt(4) - rand.nextInt(4);
            int m = z + rand.nextInt(8) - rand.nextInt(8);
            if ((!world.isEmpty(j, k, m)) || (!sourceBlocks.contains(world.getTypeId(j, k - 1, m))))
                continue;

            world.setBlock(j, k, m, blockId, blockData, false, false, false);
        }
    }

    @Override
    public void load(List<String> args) throws InvalidResourceException
    {
        if (args.size() < 6)
        {
            throw new InvalidResourceException("Too few arguments supplied");
        }
        blockId = getBlockId(args.get(0));
        blockData = getBlockData(args.get(0));
        frequency = getInt(args.get(1), 1, 100);
        rarity = getInt(args.get(2), 1, 100);
        minAltitude = getInt(args.get(3), TerrainControl.worldDepth, TerrainControl.worldHeight);
        maxAltitude = getInt(args.get(4), minAltitude + 1, TerrainControl.worldHeight);
        sourceBlocks = new ArrayList<Integer>();
        for (int i = 5; i < args.size(); i++)
        {
            sourceBlocks.add(getBlockId(args.get(i)));
        }
    }

    @Override
    public String makeString()
    {
        return "Plant(" + makeMaterial(blockId, blockData) + "," + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + makeMaterial(sourceBlocks) + ")";
    }
}