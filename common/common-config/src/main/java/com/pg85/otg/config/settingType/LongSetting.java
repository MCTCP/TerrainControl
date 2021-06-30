package com.pg85.otg.config.settingType;

import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.helpers.StringHelper;

/**
 * Reads and writes a single long.
 *
 * <p>Numbers are limited to the given min and max values.
 */
class LongSetting extends Setting<Long>
{
	private final long defaultValue;
	private final long minValue;
	private final long maxValue;

	LongSetting(String name, long defaultValue, long minValue, long maxValue)
	{
		super(name);
		this.defaultValue = defaultValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	@Override
	public Long getDefaultValue(IMaterialReader materialReader)
	{
		return defaultValue;
	}

	@Override
	public Long read(String string, IMaterialReader materialReader) throws InvalidConfigException
	{
		return StringHelper.readLong(string, minValue, maxValue);
	}

	public Long getMinValue()
	{
		return minValue;
	}
	
	public Long getMaxValue()
	{
		return maxValue;
	}	
}
