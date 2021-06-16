package com.pg85.otg.config.settingType;

import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.interfaces.IMaterialReader;

/**
 * Represents a setting. Can parse from a string and save to a string.
 *
 * @param <T> The type of the setting. Int/String/Etc.
 */
public abstract class Setting<T>
{	
	private final String name;
	
	protected Setting(String name)
	{
		this.name = name;
	}

	/**
	 * Gets the default value of the setting.
	 * @return The default value.
	 */
	public abstract T getDefaultValue(IMaterialReader materialReader);
	
	public T getDefaultValue()
	{
		return getDefaultValue(null);
	}
	
	/**
	 * Gets the name of this setting, like BiomeHeight.
	 * @return The name of this setting.
	 */
	public String getName()
	{
		return name;
	}
	
	// TODO: After cleaning up this class, create read 
	// without materialReader param for any callers passing hardcoded null.
	/**
	 * Reads the given setting from a string.
	 * @param string The value of the setting.
	 * @return The parsed setting.
	 * @throws InvalidConfigException If the setting is invalid.
	 */
	public abstract T read(String string, IMaterialReader materialReader) throws InvalidConfigException;

	/**
	 * Returns the {@link #getName() name}.
	 */
	@Override
	public final String toString()
	{
		return getName();
	}

	/**
	 * Gets the value of this setting as a string. The {@link #read(String)}
	 * method must accept all possible values returned by this method, and
	 * a round trip <code>T value = setting.read(setting.write(oldValue))
	 * </code> must produce a a value that is equal to oldValue.
	 *
	 * <p>The default implementation simply calls <code>String.valueOf(value)
	 * </code>, but more sophisticated approaches can be made.
	 * @param value The setting.
	 * @return The value.
	 */
	public String write(T value)
	{
		return String.valueOf(value);
	}
}
