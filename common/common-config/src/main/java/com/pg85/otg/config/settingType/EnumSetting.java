package com.pg85.otg.config.settingType;

import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IMaterialReader;

/**
 * Reads and writes values of the given enum type.
 *
 * <p>Valid values are those defined by the given enum type. Values are case
 * insensitive, all {@link Enum#name() names} of the values in the enum are
 * read and compared to the given value. Values are written using the
 * {@link Enum#toString() toString method on the Enum class}.
 *
 * @param <T> The enum type.
 */
class EnumSetting<T extends Enum<T>> extends Setting<T>
{
	private final T defaultValue;
	private final T[] enumValues;

	public EnumSetting(String name, T defaultValue)
	{
		super(name);
		this.defaultValue = defaultValue;
		this.enumValues = defaultValue.getDeclaringClass().getEnumConstants();
	}

	@Override
	public T getDefaultValue(IMaterialReader materialReader)
	{
		return defaultValue;
	}

	@Override
	public T read(String string, IMaterialReader materialReader) throws InvalidConfigException
	{
		for (T enumValue : enumValues)
		{
			if (enumValue.name().equalsIgnoreCase(string))
			{
				return enumValue;
			}
		}
		throw new InvalidConfigException(string + " is not an acceptable value");
	}

}
