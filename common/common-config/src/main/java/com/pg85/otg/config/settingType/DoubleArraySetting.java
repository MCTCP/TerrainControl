package com.pg85.otg.config.settingType;

import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.helpers.StringHelper;

/**
 * Reads and writes arrays of doubles, used for settings like
 * CustomHeightControl.
 *
 * <p>Numbers are separated with a ",". Numbers may have spaces around them.
 * The numbers must be within the bounds of the Java double type. Numbers
 * are written separated with a ", " (comma and space).
 */
public class DoubleArraySetting extends Setting<double[]>
{
	public DoubleArraySetting(String name)
	{
		super(name);
	}

	@Override
	public double[] getDefaultValue(IMaterialReader materialReader)
	{
		return new double[0];
	}

	@Override
	public double[] read(String string, IMaterialReader materialReader) throws InvalidConfigException
	{
		if (string.isEmpty())
		{
			return new double[0];
		}
		String[] split = StringHelper.readCommaSeperatedString(string);
		double[] values = new double[split.length];
		for (int i = 0; i < split.length; i++)
		{
			// Trimming the values allows "Value1, Value2"
			values[i] = StringHelper.readDouble(split[i], -Double.MAX_VALUE, Double.MAX_VALUE);
		}
		return values;
	}

	@Override
	public String write(double[] values)
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < values.length; i++)
		{
			if (i != 0)
			{
				builder.append(", ");
			}
			builder.append(values[i]);
		}
		return builder.toString();
	}

}
