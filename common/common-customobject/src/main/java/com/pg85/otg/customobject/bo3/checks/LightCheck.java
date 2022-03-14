package com.pg85.otg.customobject.bo3.checks;

import com.pg85.otg.customobject.bo3.BO3Config;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;

import java.util.List;

public class LightCheck extends BO3Check
{
	/** The minimum Light level, inclusive */
	private int minLightLevel;
	/** The maximum Light level, inclusive */
	private int maxLightLevel;

	@Override
	public boolean preventsSpawn(IWorldGenRegion worldGenRegion, int x, int y, int z)
	{
		int lightLevel = worldGenRegion.getLightLevel(x, y, z);
		if (lightLevel < minLightLevel || lightLevel > maxLightLevel)
		{
			// Out of bounds
			return true;
		}
		return false;
	}

	@Override
	public void load(List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
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
	public Class<BO3Config> getHolderType()
	{
		return BO3Config.class;
	}
}
