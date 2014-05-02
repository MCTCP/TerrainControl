package com.khorn.terraincontrol.configuration.settingType;

import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.helpers.StringHelper;

/**
 * Reads and writes a single float number.
 *
 * <p>Numbers are limited to the given min and max values.
 */
class FloatSetting extends Setting<Float>
{
    private final float defaultValue;
    private final float minValue;
    private final float maxValue;

    FloatSetting(String name, float defaultValue, float minValue, float maxValue)
    {
        super(name);
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public Float getDefaultValue()
    {
        return defaultValue;
    }

    @Override
    public Float read(String string) throws InvalidConfigException
    {
        return (float) StringHelper.readDouble(string, minValue, maxValue);
    }

}
