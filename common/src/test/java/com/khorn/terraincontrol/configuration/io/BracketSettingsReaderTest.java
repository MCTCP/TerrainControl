package com.khorn.terraincontrol.configuration.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import org.junit.Test;

public class BracketSettingsReaderTest
{

    @Test
    public void testBasics()
    {
        SettingsMap settingsMap = new SimpleSettingsMap("Test", false);

        BracketSettingsReader.readInto(settingsMap, "Author=Some Author;Description=Some description");

        assertEquals("Some Author", settingsMap.getSetting(WorldStandardValues.AUTHOR));
        assertEquals("Some description", settingsMap.getSetting(WorldStandardValues.DESCRIPTION));
    }

    @Test
    public void testBooleanSettings()
    {
        SettingsMap settingsMap = new SimpleSettingsMap("Test", false);

        BracketSettingsReader.readInto(settingsMap, "VillagesEnabled;NetherFortressesEnabled");

        assertTrue(settingsMap.getSetting(WorldStandardValues.VILLAGES_ENABLED));
        assertTrue(settingsMap.getSetting(WorldStandardValues.NETHER_FORTRESSES_ENABLED));
    }
}
