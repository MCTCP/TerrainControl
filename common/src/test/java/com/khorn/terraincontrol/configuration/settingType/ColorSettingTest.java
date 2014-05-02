package com.khorn.terraincontrol.configuration.settingType;

import static org.junit.Assert.assertEquals;

import com.khorn.terraincontrol.exception.InvalidConfigException;
import org.junit.Test;

public class ColorSettingTest extends AbstractSettingTest
{

    @Test
    public void testColorSettings() throws InvalidConfigException
    {
        ColorSetting setting = new ColorSetting("TestSetting", "0xffffff");

        // Test some numbers
        testValue(setting, 0xffffff);
        testValue(setting, 0x000000);

        // Tests leading zeroes
        assertEquals("#000040", setting.write(0x000040));
    }

    @Test(expected = InvalidConfigException.class)
    public void testTooHigh() throws InvalidConfigException
    {
        ColorSetting setting = new ColorSetting("TestSetting", "0xffffff");
        setting.read("0x1000000");
    }

    @Test(expected = InvalidConfigException.class)
    public void testNegative() throws InvalidConfigException
    {
        ColorSetting setting = new ColorSetting("TestSetting", "0xffffff");
        setting.read("-1");
    }
}
