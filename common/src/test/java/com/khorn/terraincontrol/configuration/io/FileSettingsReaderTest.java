package com.khorn.terraincontrol.configuration.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

public class FileSettingsReaderTest
{

    public String fileContentsString = "Author: TerrainControl\n"
            + "\n"
            + "Description:    File to test setting reading\n";

    @Test
    public void testReading() throws IOException
    {
        FileSettingsReader reader = new FileSettingsReader();
        SettingsMap settingsMap = new SimpleSettingsMap("Test", false);
        BufferedReader fileContents = new BufferedReader(new StringReader(fileContentsString));

        reader.readIntoMap(settingsMap, fileContents);

        // Test normat retrieval
        assertEquals("TerrainControl", settingsMap.getSetting(WorldStandardValues.AUTHOR));
        assertEquals("File to test setting reading", settingsMap.getSetting(WorldStandardValues.DESCRIPTION));

        // Test iterating over raw settings
        Iterator<RawSettingValue> it = settingsMap.getRawSettings().iterator();
        assertEquals("Author: TerrainControl", it.next().getRawValue());
        assertEquals("Description:    File to test setting reading", it.next().getRawValue());
    }

    @Test
    public void testNewFile()
    {
        SettingsMap settings = FileSettingsReader.read("Test", new File("./non-existing-file.ini"));

        assertTrue(settings.isNewConfig());
        assertEquals("New settings are empty", 0, settings.getRawSettings().size());
    }
}
