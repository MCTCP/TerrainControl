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
public class BlockFunction extends BO3PlaceableFunction
{

    public final LocalMaterialData material;
    public NamedBinaryTag metaDataTag;
    public String metaDataName;

    public BlockFunction(BO3Config config, List<String> args) throws InvalidConfigException
    {
        super(config);
        assureSize(4, args);
        // Those limits are arbitrary, LocalWorld.setBlock will limit it
        // correctly based on what chunks can be accessed
        x = readInt(args.get(0), -100, 100);
        y = readInt(args.get(1), -1000, 1000);
        z = readInt(args.get(2), -100, 100);
        material = readMaterial(args.get(3));
        if (args.size() == 5)
        {
            metaDataTag = BO3Loader.loadMetadata(args.get(4), getHolder().directory);
            if (metaDataTag != null)
            {
                metaDataName = args.get(4);
            }
        }
    }

    public BlockFunction(BO3Config config, int x, int y, int z, LocalMaterialData material)
    {
        super(config);
        this.x = x;
        this.y = y;
        this.z = z;
        this.material = material;
    }

    @Override
    public String toString()
    {
        String start = "Block(" + x + ',' + y + ',' + z + ',' + material;
        if (metaDataName != null)
        {
            start += ',' + metaDataName;
        }
        return start + ')';
    }

    @Override
    public BlockFunction rotate()
    {
        BlockFunction rotatedBlock = new BlockFunction(getHolder(), z, y, -x, material.rotate());
        rotatedBlock.metaDataTag = metaDataTag;
        rotatedBlock.metaDataName = metaDataName;

        return rotatedBlock;
    }

    @Override
    public void spawn(LocalWorld world, Random random, int x, int y, int z)
    {
        world.setBlock(x, y, z, material);
        if (metaDataTag != null)
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
