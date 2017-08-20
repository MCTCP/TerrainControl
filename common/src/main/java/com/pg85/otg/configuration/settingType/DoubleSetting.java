package com.pg85.otg.configuration.settingType;

import com.pg85.otg.exception.InvalidConfigException;
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
    public Double getDefaultValue()
    {
        return defaultValue;
    }

    @Override
    public Double read(String string) throws InvalidConfigException
    {
        return StringHelper.readDouble(string, minValue, maxValue);
    }

}
