package com.pg85.otg.customobjects.bo3.bo3function;

import com.pg85.otg.configuration.customobjects.CustomObjectConfigFunction;
import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.customobjects.customstructure.CustomObjectCoordinate;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.bo3.Rotation;

import java.util.List;

/**
 * Represents a block in a BO3.
 */
public class ModDataFunction extends BO3Function
{
    public int x;
    public int y;
    public int z;
    public String modId;
    public String modData;

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(5, args);
        // Those limits are arbitrary, LocalWorld.setBlock will limit it
        // correctly based on what chunks can be accessed
		x = readInt(args.get(0), -100, 100);
        y = readInt(args.get(1), -1000, 1000);
        z = readInt(args.get(2), -100, 100);
        modId = args.get(3);
        modData = args.get(4);
    }

    @Override
    public String makeString()
    {
        return "ModData(" + x + ',' + y + ',' + z + ',' + modId + ',' + modData + ')';
    }

    @Override
    public ModDataFunction rotate()
    {
        ModDataFunction rotatedBlock = new ModDataFunction();
        rotatedBlock.x = z;
        rotatedBlock.y = y;
        rotatedBlock.z = -x;
        rotatedBlock.modId = modId;
        rotatedBlock.modData = modData;

        return rotatedBlock;
    }

    public ModDataFunction rotate(Rotation rotation)
    {
    	ModDataFunction rotatedBlock = new ModDataFunction();

        CustomObjectCoordinate rotatedCoords = CustomObjectCoordinate.getRotatedBO3CoordsJustified(x, y, z, rotation);

        rotatedBlock.x = rotatedCoords.getX();
        rotatedBlock.y = rotatedCoords.getY();
        rotatedBlock.z = rotatedCoords.getZ();

        rotatedBlock.modId = modId;
        rotatedBlock.modData = modData;

        return rotatedBlock;
    }

    @Override
    public boolean isAnalogousTo(CustomObjectConfigFunction<BO3Config> other)
    {
        if(!getClass().equals(other.getClass()))
        {
            return false;
        }
        ModDataFunction block = (ModDataFunction) other;
        return block.x == x && block.y == y && block.z == z && block.modId.equalsIgnoreCase(modId) && block.modData.equalsIgnoreCase(modData);
    }
}
