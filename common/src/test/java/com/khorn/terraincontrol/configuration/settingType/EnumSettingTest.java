package com.khorn.terraincontrol.configuration.settingType;

import static org.junit.Assert.assertEquals;

import com.khorn.terraincontrol.configuration.WorldConfig.ConfigMode;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class EnumSettingTest extends AbstractSettingTest
{
    @Test
    public void testCaseInsensitivity() throws InvalidConfigException
    {
        EnumSetting<ConfigMode> setting = new EnumSetting<ConfigMode>("TestSetting", ConfigMode.WriteAll);
        for (ConfigMode configMode : ConfigMode.values())
        {
            // .toUpperCase to make sure that it is case insensitive
            ConfigMode readBack = setting.read(configMode.name().toUpperCase());

            // Make sure we actually have got the correct config mode
            assertEquals(configMode, readBack);
        }
    }

    @Test
    public void testEnumSettings() throws InvalidConfigException
    {
        EnumSetting<ConfigMode> setting = new EnumSetting<ConfigMode>("TestSetting", ConfigMode.WriteAll);
        for (ConfigMode configMode : ConfigMode.values())
        {
            testValue(setting, configMode);
        }
    }

    @Test(expected = InvalidConfigException.class)
    public void testInvalidEnumSetting() throws InvalidConfigException
    {
        new EnumSetting<ConfigMode>("TestSetting", ConfigMode.WriteAll).read("InvalidOption");
    }
}
