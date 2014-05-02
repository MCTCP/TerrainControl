package com.khorn.terraincontrol.configuration.settingType;

import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.helpers.StringHelper;

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
    public Long getDefaultValue()
    {
        return defaultValue;
    }

    @Override
    public Long read(String string) throws InvalidConfigException
    {
        return StringHelper.readLong(string, minValue, maxValue);
    }

}
