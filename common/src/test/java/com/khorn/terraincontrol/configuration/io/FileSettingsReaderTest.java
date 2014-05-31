package com.khorn.terraincontrol.configuration.io;

import static org.junit.Assert.assertEquals;

import com.khorn.terraincontrol.configuration.settingType.Setting;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@RunWith(JUnit4.class)
public class FileSettingsReaderTest extends AbstractSettingsReaderTest
{

    @Override
    protected void cleanupCrumbs(SettingsReader reader)
    {
        reader.getFile().delete();
    }

    @Override
    protected FileSettingsReader getEmptyReader()
    {
        return new FileSettingsReader("Test", new File("nonExistantTestFile.txt"));
    }

    @Override
    protected <S> SettingsReader getExistingReader(Setting<S> setting, S value) throws IOException
    {
        File file = File.createTempFile("tcTestFile", ".ini");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(setting.getName() + ": " + setting.write(value));
        writer.flush();
        writer.close();

        return new FileSettingsReader("TestConfig", file);
    }

    /**
     * Create two readers: one from an non-existent file and one from an
     * existing file. Make sure that isNewConfig returns the appropriate
     * values.
     * @throws IOException If the temporary file can't be created
     */
    @Test
    public void testIsNewConfig() throws IOException
    {
        // Test with non-existant file
        SettingsReader emptyReader = getEmptyReader();
        assertEquals(true, emptyReader.isNewConfig());

        // Test with existing file
        File file = File.createTempFile("tcTestIsNewConfig", ".ini");
        file.createNewFile();
        SettingsReader readingExistingFile = new FileSettingsReader("Test", file);
        assertEquals(false, readingExistingFile.isNewConfig());
        file.delete();
    }
}
