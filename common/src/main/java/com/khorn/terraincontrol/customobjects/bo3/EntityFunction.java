package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.NamedBinaryTag;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Represents an entity in a BO3.
 */
public class EntityFunction extends BO3PlaceableFunction
{
    public String mobName = "";
    public int groupSize = 1;
    public String metaDataName = "";
    public NamedBinaryTag metaDataTag = null;

    private EntityFunction(BO3Config holder)
    {
        super(holder);
    }

    public EntityFunction(BO3Config config, List<String> args) throws InvalidConfigException
    {
        super(config);
        assureSize(5, args);
        // Those limits are arbitrary, LocalWorld.setBlock will limit it
        // correctly based on what chunks can be accessed
        x = readInt(args.get(0), -100, 100);
        y = readInt(args.get(1), -1000, 1000);
        z = readInt(args.get(2), -100, 100);
        mobName = args.get(3);
        groupSize = readInt(args.get(4), 0, Integer.MAX_VALUE);

        if(args.size() > 5)
        {
            metaDataName = args.get(5).trim();
            metaDataTag = BO3Loader.loadMetadata(args.get(5), getHolder().directory);
        }
    }

    @Override
    public String toString()
    {
        return "Entity(" + x + ',' + y + ',' + z + ',' + mobName + ',' + groupSize + (metaDataName.length() > 0 ? ',' + metaDataName : "") + ')';
    }

    @Override
    public EntityFunction rotate()
    {
        EntityFunction rotatedBlock = new EntityFunction(getHolder());
        rotatedBlock.x = z;
        rotatedBlock.y = y;
        rotatedBlock.z = -x;
        rotatedBlock.mobName = mobName;
        rotatedBlock.groupSize = groupSize;
        rotatedBlock.metaDataName = metaDataName;
        rotatedBlock.metaDataTag = metaDataTag;

        return rotatedBlock;
    }


    @Override
    public boolean isAnalogousTo(ConfigFunction<BO3Config> other)
    {
        if(!getClass().equals(other.getClass()))
        {
            return false;
        }
        EntityFunction block = (EntityFunction) other;
        return block.x == x && block.y == y && block.z == z && block.mobName.equalsIgnoreCase(
                mobName) && block.groupSize == groupSize && Objects.equals(block.metaDataName, metaDataName);
    }

    @Override
    public void spawn(LocalWorld world, Random random, int x, int y, int z)
    {
        world.spawnEntity(mobName, this.x + x, this.y + y, this.z + z, groupSize, metaDataTag);
    }
}