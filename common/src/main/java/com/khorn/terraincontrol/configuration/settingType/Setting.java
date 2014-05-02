package com.khorn.terraincontrol.configuration.settingType;

import com.khorn.terraincontrol.exception.InvalidConfigException;

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
    public abstract T getDefaultValue();

    /**
     * Gets the name of this setting, like BiomeHeight.
     * @return The name of this setting.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Reads the given setting from a string.
     * @param string The value of the setting.
     * @return The parsed setting.
     * @throws InvalidConfigException If the setting is invalid.
     */
    public abstract T read(String string) throws InvalidConfigException;

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
     * method should accept all possible values returned by this method, and
     * a round trip <code>T value = setting.read(setting.write(oldValue))
     * </code>.
     *
     * <p>The default implementation simply calls <code>String.valueOf(value)
     * </code>, but more sophisticated approaches can be made.
     * @return The value.
     */
    public String write(T value)
    {
        return String.valueOf(value);
    }
}
