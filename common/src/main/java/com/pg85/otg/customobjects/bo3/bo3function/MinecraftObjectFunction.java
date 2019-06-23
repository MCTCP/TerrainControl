package com.pg85.otg.customobjects.bo3.bo3function;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.customobjects.CustomObjectConfigFunction;
import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.generator.SpawnableObject;
import com.pg85.otg.util.Rotation;
import com.pg85.otg.util.minecraftTypes.DefaultStructurePart;

import java.util.List;
import java.util.Random;

/**
 * Represents a block in a BO3.
 */
public class MinecraftObjectFunction extends BlockFunction
{
    public DefaultStructurePart structurePart;
    public Rotation rotation = Rotation.NORTH;

    public MinecraftObjectFunction()
    {
    	super();
    }

    public MinecraftObjectFunction(BO3Config config, List<String> args) throws InvalidConfigException
    {
        assureSize(4, args);
        // Those limits are arbitrary, LocalWorld.setBlock will limit it
        // correctly based on what chunks can be accessed
        x = readInt(args.get(0), -100, 100);
        y = readInt(args.get(1), -1000, 1000);
        z = readInt(args.get(2), -100, 100);
        structurePart = DefaultStructurePart.getDefaultStructurePart(args.get(3));
    }

    @Override
    public String makeString()
    {
        return "MinecraftObject(" + x + ',' + y + ',' + z + ',' + structurePart + ')';
    }

    @Override
    public MinecraftObjectFunction rotate()
    {
        MinecraftObjectFunction rotatedBlock = new MinecraftObjectFunction();
        rotatedBlock.x = z;
        rotatedBlock.y = y;
        rotatedBlock.z = -x;
        rotatedBlock.rotation = rotation.next();

        return rotatedBlock;
    }

    @Override
    public void spawn(LocalWorld world, Random random, int x, int y, int z, boolean allowOutsidePopulatingArea)
    {
        SpawnableObject object = world.getMojangStructurePart(structurePart.getPath());
        object.spawnForced(world, random, rotation, x, y, z);
    }

    @Override
    public boolean isAnalogousTo(CustomObjectConfigFunction<BO3Config> other)
    {
        if(!getClass().equals(other.getClass()))
        {
            return false;
        }
        MinecraftObjectFunction block = (MinecraftObjectFunction) other;
        return block.x == x && block.y == y && block.z == z;
    }
}
