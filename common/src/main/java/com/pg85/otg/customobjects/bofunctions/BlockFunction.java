package com.pg85.otg.customobjects.bofunctions;

import java.util.List;
import java.util.Random;

import com.pg85.otg.common.LocalWorldGenRegion;
import com.pg85.otg.common.materials.LocalMaterialData;
import com.pg85.otg.config.customobjects.CustomObjectConfigFile;
import com.pg85.otg.config.customobjects.CustomObjectConfigFunction;
import com.pg85.otg.customobjects.bo3.BO3Loader;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.NamedBinaryTag;

/**
 * Represents a block in a BO3.
 */
public abstract class BlockFunction<T extends CustomObjectConfigFile> extends CustomObjectConfigFunction<T>
{
    public LocalMaterialData material;
    public short y;
    public NamedBinaryTag metaDataTag;
    public String metaDataName;

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(4, args);
        // Those limits are arbitrary, LocalWorld.setBlock will limit it
        // correctly based on what chunks can be accessed
        x = readInt(args.get(0), -100, 100);
        y = (short) readInt(args.get(1), -1000, 1000);
        z = readInt(args.get(2), -100, 100);

        material = readMaterial(args.get(3));
       
        if(material == null)
        {
        	throw new InvalidConfigException("Material \"" + args.get(3) + "\" could not be read.");
        }
        
        if (args.size() == 5)
        {
            metaDataTag = BO3Loader.loadMetadata(args.get(4), getHolder().getFile());
            if (metaDataTag != null)
            {
                metaDataName = args.get(4);
            }
        }
    }

    @Override
    public String makeString()
    {
        String start = "Block(" + x + ',' + y + ',' + z + ',' + material;
        if (metaDataTag != null)
        {
            start += ',' + metaDataName;
        }
        return start + ')';
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
    public abstract void spawn(LocalWorldGenRegion worldGenRegion, Random random, int x, int y, int z, ChunkCoordinate chunkBeingPopulated, boolean replaceBlock);
    
    @Override
    public boolean isAnalogousTo(CustomObjectConfigFunction<T> other)
    {
        if (!getClass().equals(other.getClass()))
        {
            return false;
        }
        BlockFunction<T> block = (BlockFunction<T>) other;
        return block.x == x && block.y == y && block.z == z;
    }
}