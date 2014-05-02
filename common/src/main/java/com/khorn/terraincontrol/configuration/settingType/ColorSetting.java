package com.khorn.terraincontrol.configuration.settingType;

import com.khorn.terraincontrol.exception.InvalidConfigException;

/**
 * Reads and writes colors. The colors are represented as integers internally,
 * but are written as hexadecimal colors in upper case, starting with a #.
 *
 * <p>Color reading allows multiple formats. Colors starting with 0x or
 * # are interpreted as hexadecimal numbers, colors starting with 0 as octal
 * numbers and other colors as decimal numbers. Colors are case insensitive.
 *
 */
class ColorSetting extends Setting<Integer>
{
    private int defaultValue;

    ColorSetting(String name, String defaultValue)
    {
        super(name);
        this.defaultValue = Integer.decode(defaultValue);
    }

    @Override
    public Integer getDefaultValue()
    {
        return defaultValue;
    }

    @Override
    public Integer read(String string) throws InvalidConfigException
    {
        try
        {
            Integer integer = Integer.decode(string);
            if (integer.intValue() > 0xffffff || integer.intValue() < 0)
            {
                throw new InvalidConfigException("Color must have 6 hexadecimal digits");
            }
            return integer;
        } catch (NumberFormatException e)
        {
            throw new InvalidConfigException("Invalid color " + string);
        }
    }

    @Override
    public String write(Integer value)
    {
        return "#" + Integer.toHexString(value.intValue() | 0x1000000).substring(1).toUpperCase();
    }

}
