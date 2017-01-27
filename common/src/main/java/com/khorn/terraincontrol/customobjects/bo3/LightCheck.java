package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.exception.InvalidConfigException;

import java.util.List;

/**
 *
 */
public class LightCheck extends BO3Check
{

    /**
     * The minimum Light level, inclusive
     */
    public int minLightLevel;
    /**
     * The maximum Light level, inclusive
     */
    public int maxLightLevel;

    public LightCheck(BO3Config config, List<String> args) throws InvalidConfigException
    {
        super(config);
        assureSize(5, args);
        this.x = readInt(args.get(0), -100, 100);
        this.y = readInt(args.get(1), -100, 100);
        this.z = readInt(args.get(2), -100, 100);
        this.minLightLevel = readInt(args.get(3), 0, 16);
        this.maxLightLevel = readInt(args.get(4), this.minLightLevel, 16);
    }

    private LightCheck(BO3Config config)
    {
        super(config);
    }

    @Override
    public boolean preventsSpawn(LocalWorld world, int x, int y, int z)
    {
        int lightLevel = world.getLightLevel(x, y, z);
        if (lightLevel < this.minLightLevel || lightLevel > this.maxLightLevel)
        {
            // Out of bounds
            return true;
        }

        return false;
    }

    @Override
    public String toString()
    {
        return "LightCheck(" + x + ',' + y + ',' + z + ',' + this.minLightLevel + ',' + this.maxLightLevel + ')';
    }

    @Override
    public BO3Check rotate()
    {
        LightCheck rotatedCheck = new LightCheck(getHolder());
        rotatedCheck.x = z;
        rotatedCheck.y = y;
        rotatedCheck.z = -x;
        rotatedCheck.minLightLevel = this.minLightLevel;
        rotatedCheck.maxLightLevel = this.maxLightLevel;

        return rotatedCheck;
    }

}
