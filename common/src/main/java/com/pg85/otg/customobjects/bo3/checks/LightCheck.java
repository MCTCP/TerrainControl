package com.pg85.otg.customobjects.bo3.checks;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.customobjects.customstructure.CustomObjectCoordinate;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.bo3.Rotation;

import java.util.List;

/**
 *
 */
public class LightCheck extends BO3Check
{

    /**
     * The minimum Light level, inclusive
     */
    private int minLightLevel;
    /**
     * The maximum Light level, inclusive
     */
    private int maxLightLevel;

    @Override
    public boolean preventsSpawn(LocalWorld world, int x, int y, int z)
    {
        int lightLevel = world.getLightLevel(x, y, z);
        if (lightLevel < minLightLevel || lightLevel > maxLightLevel)
        {
            // Out of bounds
            return true;
        }

        return false;
    }

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(5, args);
		x = readInt(args.get(0), -100, 100);
        y = readInt(args.get(1), -100, 100);
        z = readInt(args.get(2), -100, 100);
        minLightLevel = readInt(args.get(3), 0, 16);
        maxLightLevel = readInt(args.get(4), minLightLevel, 16);
    }

    @Override
    public String makeString()
    {
        return "LightCheck(" + x + ',' + y + ',' + z + ',' + minLightLevel + ',' + maxLightLevel + ')';
    }

    @Override
    public BO3Check rotate()
    {
        LightCheck rotatedCheck = new LightCheck();
        rotatedCheck.x = z;
        rotatedCheck.y = y;
        rotatedCheck.z = -x;
        rotatedCheck.minLightLevel = minLightLevel;
        rotatedCheck.maxLightLevel = maxLightLevel;

        return rotatedCheck;
    }

    @Override
    public LightCheck rotate(Rotation rotation)
    {
    	LightCheck rotatedBlock = new LightCheck();

        CustomObjectCoordinate rotatedCoords = CustomObjectCoordinate.getRotatedBO3CoordsJustified(x, y, z, rotation);

        rotatedBlock.x = rotatedCoords.getX();
        rotatedBlock.y = rotatedCoords.getY();
        rotatedBlock.z = rotatedCoords.getZ();

    	rotatedBlock.minLightLevel = minLightLevel;
    	rotatedBlock.maxLightLevel = maxLightLevel;

        return rotatedBlock;
    }
}
