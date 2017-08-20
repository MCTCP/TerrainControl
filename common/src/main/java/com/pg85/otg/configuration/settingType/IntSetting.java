package com.pg85.otg.configuration.settingType;

import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.helpers.StringHelper;

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
