package com.khorn.terraincontrol.configuration.settingType;

import static org.junit.Assert.assertEquals;

import com.khorn.terraincontrol.exception.InvalidConfigException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class LongSettingTest extends AbstractSettingTest
{
    @Test
    public void testRoundTrips() throws InvalidConfigException
    {
        LongSetting setting = new LongSetting("TestSetting", 0, Long.MIN_VALUE, Long.MAX_VALUE);
        testValue(setting, Long.MIN_VALUE);
        testValue(setting, 0L);
        testValue(setting, Long.MAX_VALUE);
    }

    @Test(expected = InvalidConfigException.class)
    public void testInvalidSetting() throws InvalidConfigException
    {
        new LongSetting("TestSetting", 0, -1, 1).read("NotANumber");
    }

    @Test
    public void testOutOfBoundsSetting() throws InvalidConfigException
    {
        long min = -1;
        long max = 1;

        assertEquals(max, new LongSetting("TestSetting", 0, min, max).read("2").intValue());
        assertEquals(min, new LongSetting("TestSetting", 0, min, max).read("-2").intValue());
    }
}
