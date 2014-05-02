package com.khorn.terraincontrol.configuration.settingType;

import com.khorn.terraincontrol.exception.InvalidConfigException;

/**
 * Reads and writes a string. Surrounding whitespace is stripped using
 * {@link String#trim()}.
 *
 */
class StringSetting extends Setting<String>
{
    private final String defaultValue;

    StringSetting(String name, String defaultValue)
    {
        super(name);
        this.defaultValue = defaultValue;
    }

    @Override
    public String getDefaultValue()
    {
        return defaultValue;
    }

    @Override
    public String read(String string) throws InvalidConfigException
    {
        return string.trim();
    }

}
