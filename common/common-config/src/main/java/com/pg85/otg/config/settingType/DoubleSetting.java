package com.pg85.otg.config.settingType;

import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.helpers.StringHelper;

/**
 * Reads and writes a single double number.
 *
 * <p>Numbers are limited to the given min and max values.
 */
class DoubleSetting extends Setting<Double>
{
	private final double defaultValue;
	private final double minValue;
	private final double maxValue;

	DoubleSetting(String name, double defaultValue, double minValue, double maxValue)
	{
		super(name);
		this.defaultValue = defaultValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	@Override
	public Double getDefaultValue(IMaterialReader materialReader)
	{
		return defaultValue;
	}

	@Override
	public Double read(String string, IMaterialReader materialReader) throws InvalidConfigException
	{
		return StringHelper.readDouble(string, minValue, maxValue);
	}

	public Double getMinValue()
	{
		return minValue;
	}
	
	public Double getMaxValue()
	{
		return maxValue;
	}	
}
