package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.NamedBinaryTag;

import java.util.List;
import java.util.Random;

/**
 * Represents a block in a BO3.
 */
public class BlockFunction extends BO3Function
{

    public LocalMaterialData material;
    public int x;
    public int y;
    public int z;
    public boolean hasMetaData;
    public NamedBinaryTag metaDataTag;
    public String metaDataName;

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(4, args);
        // Those limits are arbitrary, LocalWorld.setBlock will limit it
        // correctly based on what chunks can be accessed
        x = readInt(args.get(0), -100, 100);
        y = readInt(args.get(1), -1000, 1000);
        z = readInt(args.get(2), -100, 100);
        material = readMaterial(args.get(3));
        if (args.size() == 5)
        {
            metaDataTag = BO3Loader.loadMetadata(args.get(4), getHolder().getFile());
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
        String start = "Block(" + x + ',' + y + ',' + z + ',' + material;
        if (hasMetaData)
        {
            start += ',' + metaDataName;
        }
        return start + ')';
    }

    @Override
    public BlockFunction rotate()
    {
        BlockFunction rotatedBlock = new BlockFunction();
        rotatedBlock.x = z;
        rotatedBlock.y = y;
        rotatedBlock.z = -x;
        rotatedBlock.material = material.rotate();
        rotatedBlock.hasMetaData = hasMetaData;
        rotatedBlock.metaDataTag = metaDataTag;
        rotatedBlock.metaDataName = metaDataName;

        return rotatedBlock;
    }

    /**
     * Spawns this block at the position. The saved x, y and z in this block are
     * ignored.
     * <p/>
     * @param world  The world to spawn in.
     * @param random The random number generator.
     * @param x      The absolute x to spawn. The x-position in this object is
     *               ignored.
     * @param y      The absolute y to spawn. The y-position in this object is
     *               ignored.
     * @param z      The absolute z to spawn. The z-position in this object is
     *               ignored.
     */
    public void spawn(LocalWorld world, Random random, int x, int y, int z)
    {
        world.setBlock(x, y, z, material);
        if (hasMetaData)
        {
            world.attachMetadata(x, y, z, metaDataTag);
        }
    }

    @Override
    public boolean isAnalogousTo(ConfigFunction<BO3Config> other)
    {
        if(!getClass().equals(other.getClass())) {
            return false;
        }
        BlockFunction block = (BlockFunction) other;
        return block.x == x && block.y == y && block.z == z;
    }

}
