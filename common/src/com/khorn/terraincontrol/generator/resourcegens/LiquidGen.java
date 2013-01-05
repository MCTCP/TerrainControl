package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LiquidGen extends Resource
{
    private List<Integer> sourceBlocks;
    private int minAltitude;
    private int maxAltitude;

    @Override
    public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z)
    {
        int y = rand.nextInt(maxAltitude - minAltitude) + minAltitude;

        if (sourceBlocks.contains(world.getTypeId(x, y + 1, z)))
            return;
        if (sourceBlocks.contains(world.getTypeId(x, y - 1, z)))
            return;

        if ((world.getTypeId(x, y, z) != 0) && (sourceBlocks.contains(world.getTypeId(x, y, z))))
            return;

        int i = 0;
        int j = 0;

        int tempBlock = world.getTypeId(x - 1, y, z);

        i = (sourceBlocks.contains(tempBlock)) ? i + 1 : i;
        j = (tempBlock == 0) ? j + 1 : j;

        tempBlock = world.getTypeId(x + 1, y, z);

        i = (sourceBlocks.contains(tempBlock)) ? i + 1 : i;
        j = (tempBlock == 0) ? j + 1 : j;

        tempBlock = world.getTypeId(x, y, z - 1);

        i = (sourceBlocks.contains(tempBlock)) ? i + 1 : i;
        j = (tempBlock == 0) ? j + 1 : j;

        tempBlock = world.getTypeId(x, y, z + 1);

        i = (sourceBlocks.contains(tempBlock)) ? i + 1 : i;
        j = (tempBlock == 0) ? j + 1 : j;

        if ((i == 3) && (j == 1))
        {
            world.setBlock(x, y, z, blockId, 0, true, true, true);
            // this.world.f = true;
            // Block.byId[res.BlockId].a(this.world, x, y, z, this.rand);
            // this.world.f = false;
        }
    }

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(6, args);

        blockId = readBlockId(args.get(0));
        blockData = readBlockData(args.get(0));
        frequency = readInt(args.get(1), 1, 5000);
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
        return "Liquid(" + makeMaterial(blockId, blockData) + "," + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + makeMaterial(sourceBlocks) + ")";
    }
}