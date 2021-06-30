package com.pg85.otg.customobject.bo3.checks;

import java.util.List;

import com.pg85.otg.customobject.bo3.BO3Config;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.interfaces.IWorldGenRegion;

public class ModCheck extends BO3Check
{
	private String[] mods;

	@Override
	public boolean preventsSpawn(IWorldGenRegion worldGenRegion, int x, int y, int z)
	{
		return false;
	}

	@Override
	public BO3Check rotate()
	{
		return this;
	}

	@Override
	protected void load(List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		assureSize(1, args);
		mods = new String[args.size()];
		for (int i = 0; i < args.size(); i++)
		{
			mods[i] = args.get(i);
		}
	}

	@Override
	public String makeString()
	{
		return makeString("ModCheck");
	}

	/**
	 * Gets the string representation with the given check name.
	 *
	 * @param name Name of the check, like BlockCheck.
	 * @return The string representation.
	 */
	protected String makeString(String name)
	{
		return name + '(' + String.join(",", mods) + ')';
	}

	@Override
	public Class<BO3Config> getHolderType()
	{
		return BO3Config.class;
	}

	public boolean evaluate(IModLoadedChecker modLoadedChecker)
	{
		for (String mod : mods)
		{
			if (!modLoadedChecker.isModLoaded(mod))
			{
				return false;
			}
		}
		return true;
	}
}
