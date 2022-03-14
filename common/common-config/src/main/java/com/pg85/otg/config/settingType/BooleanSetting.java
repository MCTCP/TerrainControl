package com.pg85.otg.config.settingType;

import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IMaterialReader;

/**
 * Reads and writes booleans.
 *
 * <p>It can read the values true and false, case insensitive. It will write
 * "true" or "false", always in lowercase.
 */
class BooleanSetting extends Setting<Boolean>
{
	private final boolean defaultValue;

	BooleanSetting(String name, boolean defaultValue)
	{
		super(name);
		this.defaultValue = Boolean.valueOf(defaultValue);
	}

	@Override
	public Boolean getDefaultValue(IMaterialReader materialReader)
	{
		return defaultValue;
	}

	@Override
	public Boolean read(String string, IMaterialReader materialReader) throws InvalidConfigException
	{
		if (string.equalsIgnoreCase("true"))
		{
			return Boolean.TRUE;
		}
		if (string.equalsIgnoreCase("false"))
		{
			return Boolean.FALSE;
		}
		throw new InvalidConfigException(string + " is not a boolean");
	}

}
