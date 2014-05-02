package com.khorn.terraincontrol.configuration.settingType;

import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.helpers.StringHelper;

/**
 * Reads and writes a single integer.
 *
 * <p>Numbers are limited to the given min and max values.
 */
class IntSetting extends Setting<Integer>
{
    private final int defaultValue;
    private final int minValue;
    private final int maxValue;

    IntSetting(String name, int defaultValue, int minValue, int maxValue)
    {
        super(name);
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public Integer getDefaultValue()
    {
        return defaultValue;
    }

    @Override
    public Integer read(String string) throws InvalidConfigException
    {
        return StringHelper.readInt(string, minValue, maxValue);
    }

}
