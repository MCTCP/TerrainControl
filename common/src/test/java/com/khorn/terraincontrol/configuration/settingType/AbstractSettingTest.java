package com.khorn.terraincontrol.configuration.settingType;

import static org.junit.Assert.assertEquals;

import com.khorn.terraincontrol.exception.InvalidConfigException;

class AbstractSettingTest
{

    private <T> T roundTrip(Setting<T> setting, T value) throws InvalidConfigException
    {
        return setting.read(setting.write(value));
    }

    /**
     * Asserts that the given value survives a round trip of being serialized
     * to a string and deserialized to a value.
     * @param setting The setting to check.
     * @param value   The value to check.
     * @throws InvalidConfigException If the setting couldn't be read back.
     */
    protected <T> void testValue(Setting<T> setting, T value) throws InvalidConfigException
    {
        assertEquals(value, roundTrip(setting, value));
    }

}