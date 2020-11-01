package com.pg85.otg.config.settingType;

import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.bo3.Rotation;

/**
 * Reads and writes a single integer.
 *
 * <p>Numbers are limited to the given min and max values.
 */
class RotationSetting extends Setting<Rotation>
{
    private final Rotation defaultValue;

    RotationSetting(String name, Rotation defaultValue)
    {
        super(name);
        this.defaultValue = defaultValue;
    }

    @Override
    public Rotation getDefaultValue()
    {
        return defaultValue;
    }

    @Override
    public Rotation read(String string) throws InvalidConfigException
    {
        return
    		string == null || string.length() == 0 ? defaultValue :
			string.toUpperCase().trim().equals("NORTH") ? Rotation.NORTH :
			string.toUpperCase().trim().equals("EAST") ? Rotation.EAST :
			string.toUpperCase().trim().equals("SOUTH") ? Rotation.SOUTH :
			string.toUpperCase().trim().equals("WEST") ? Rotation.WEST :
			defaultValue;
    }
}
