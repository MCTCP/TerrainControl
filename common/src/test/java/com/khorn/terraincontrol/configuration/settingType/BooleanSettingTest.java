package com.khorn.terraincontrol.configuration.settingType;

import com.khorn.terraincontrol.exception.InvalidConfigException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BooleanSettingTest
{
    @Test
    public void testValidValues() throws InvalidConfigException
    {
        BooleanSetting setting = setting();
        setting.read("true");
        setting.read("false");
        setting.read("TRUE");
        setting.read("True");
        setting.read("FALSE");
        setting.read("False");
    }

    @Test(expected = InvalidConfigException.class)
    public void testInvalidValue() throws InvalidConfigException
    {
        BooleanSetting setting = setting();
        setting.read("invalid");
    }

    private BooleanSetting setting()
    {
        return new BooleanSetting("TestSetting", false);
    }
}
