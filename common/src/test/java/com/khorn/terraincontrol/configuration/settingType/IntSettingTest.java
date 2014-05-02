package com.khorn.terraincontrol.configuration.settingType;

import static org.junit.Assert.assertEquals;

import com.khorn.terraincontrol.exception.InvalidConfigException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class IntSettingTest extends AbstractSettingTest
{
    @Test
    public void testRoundtrips() throws InvalidConfigException
    {
        IntSetting setting = new IntSetting("TestSetting", 0, -10, 10);
        testValue(setting, -10);
        testValue(setting, 0);
        testValue(setting, 10);
    }

    @Test(expected = InvalidConfigException.class)
    public void testInvalidSetting() throws InvalidConfigException
    {
        new IntSetting("TestSetting", 0, -1, 1).read("NotANumber");
    }

    @Test
    public void testOutOfBoundsSetting() throws InvalidConfigException
    {
        int min = -1;
        int max = 1;

        assertEquals(max, new IntSetting("TestSetting", 0, min, max).read("2").intValue());
        assertEquals(min, new IntSetting("TestSetting", 0, min, max).read("-2").intValue());
    }
}
