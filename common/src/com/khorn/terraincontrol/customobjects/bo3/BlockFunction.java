package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.Tag;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.BlockHelper;

import java.util.List;
import java.util.Random;

/**
 * Represents a block in a BO3.
 */
public class BlockFunction extends BO3Function
{
    public int blockId;
    public int blockData;
    public int x;
    public int y;
    public int z;

    public boolean hasMetaData;
    public Tag metaDataTag;
    public String metaDataName;

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(4, args);
        x = readInt(args.get(0), -100, 100);
        y = readInt(args.get(1), -100, 100);
        z = readInt(args.get(2), -100, 100);
        blockId = readBlockId(args.get(3));
        blockData = readBlockData(args.get(3));
        if (args.size() == 5)
        {
            metaDataTag = BO3Loader.loadMetadata(args.get(4), getHolder().file);
            if (metaDataTag != null)
            {
                hasMetaData = true;
                metaDataName = args.get(4);
            }
        }
    }

    @Override
    public String makeString()
    {
        String start = "Block(" + x + "," + y + "," + z + "," + makeMaterial(blockId, blockData);
        if (hasMetaData)
        {
            start += "," + metaDataName;
        }
        return start + ")";
    }

    @Override
    public BlockFunction rotate()
    {
        BlockFunction rotatedBlock = new BlockFunction();
        rotatedBlock.x = z;
        rotatedBlock.y = y;
        rotatedBlock.z = -x;
        rotatedBlock.blockId = blockId;
        rotatedBlock.blockData = BlockHelper.rotateData(blockId, blockData);
        rotatedBlock.hasMetaData = hasMetaData;
        rotatedBlock.metaDataTag = metaDataTag;
        rotatedBlock.metaDataName = metaDataName;

        return rotatedBlock;
    }

    /**
     * Spawns this block at the position. The saved x, y and z in this block are
     * ignored.
     * 
     * @param world     The world to spawn in.
     * @param random    The random number generator.
     * @param x         The absolute x to spawn. The x-position in this object is ignored.
     * @param y         The absolute y to spawn. The y-position in this object is ignored.
     * @param z         The absolute z to spawn. The z-position in this object is ignored.
     */
    public void spawn(LocalWorld world, Random random, int x, int y, int z)
    {
        world.setBlock(x, y, z, blockId, blockData);
        if (hasMetaData)
        {
            world.attachMetadata(x, y, z, metaDataTag);
        }
    }
}
