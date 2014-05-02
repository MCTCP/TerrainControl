package com.khorn.terraincontrol.configuration.settingType;

import static org.junit.Assert.assertEquals;

import com.khorn.terraincontrol.exception.InvalidConfigException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DoubleSettingTest extends AbstractSettingTest
{
    @Test
    public void testRoundTrips() throws InvalidConfigException
    {
        DoubleSetting setting = new DoubleSetting("TestSetting", 0, -1000, 1000);
        testValue(setting, 1000.0);
        testValue(setting, 0.0);
        testValue(setting, -1000.0);
    }

    @Test(expected = InvalidConfigException.class)
    public void testInvalidSetting() throws InvalidConfigException
    {
        new DoubleSetting("TestSetting", 0, -1, 1).read("NotANumber");
    }

    @Test
    public void testOutOfBoundsSetting() throws InvalidConfigException
    {
        double min = -1.1;
        double max = 1.1;

        assertEquals(max, new DoubleSetting("TestSetting", 0, min, max).read("2").doubleValue(), 0.0000001);
        assertEquals(min, new DoubleSetting("TestSetting", 0, min, max).read("-2").doubleValue(), 0.0000001);
    }
}
