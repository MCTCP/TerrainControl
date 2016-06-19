package com.khorn.terraincontrol.configuration.io;

import static org.junit.Assert.assertEquals;

import com.khorn.terraincontrol.configuration.settingType.Setting;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import org.junit.Test;

import java.io.IOException;

public class SettingsMapTest
{

    /**
     * Gets a settings reader with no settings present.
     * @return The reader.
     */
    protected SettingsMap getEmptyReader()
    {
        return new SimpleSettingsMap("Test", false);
    }

    /**
     * Gets a settings reader with the given setting and value present.
     *
     * <p>For example, if this is a settings reader that reads from a file, a
     * file is created with the setting, and a settings reader of that file is
     * created.
     * @param setting The setting present.
     * @param value The value.
     * @return The settings reader.
     */
    protected <S> SettingsMap getExistingReader(Setting<S> setting, S value)
    {
        SettingsMap settings = getEmptyReader();
        settings.putSetting(setting, value);
        return settings;
    }

    /**
     * Get an empty config reader, check if it returns the default value for a
     * setting, change that setting, check if it now returns the changed
     * setting.
     */
    @Test
    public void testGetAndPutSetting()
    {
        String defaultValue = "map.png";
        String changedValue = "customfile.png";

        SettingsMap reader = getEmptyReader();

        // Reader is empty, so first we get the default value
        assertEquals(defaultValue, reader.getSetting(WorldStandardValues.IMAGE_FILE, defaultValue));

        // Set it to another value
        reader.putSetting(WorldStandardValues.IMAGE_FILE, changedValue);

        // Make sure that value is returned
        assertEquals(changedValue, reader.getSetting(WorldStandardValues.IMAGE_FILE, defaultValue));
    }

    /**
     * Puts a setting in a file, reads the file, overrides the setting, check
     * if returned value is the overridden setting.
     * @throws IOException If file creation fails.
     */
    @Test
    public void testPutSettingOverridesFileContents() throws IOException
    {
        String imageFileDefault = "map.png";
        String imageFileInConfig = "inConfig.png";
        String imageFileOverridden = "overridden.png";

        // Read it, replace value
        SettingsMap reader = getExistingReader(WorldStandardValues.IMAGE_FILE, imageFileInConfig);
        reader.putSetting(WorldStandardValues.IMAGE_FILE, imageFileOverridden);

        // Check if value is replaced
        assertEquals(imageFileOverridden, reader.getSetting(WorldStandardValues.IMAGE_FILE, imageFileDefault));
    }

    /**
     * Create a simple config file with a few settings using some simple write
     * methods. (This rules out possible errors in FileSettingsWriter). Make
     * sure that the file is read correctly.
     * @throws IOException If creation of the temporary file failed.
     */
    @Test
    public void testReading() throws IOException
    {
        String expectedImageFile = "myMap.png";

        // Read it back
        SettingsMap reader = getExistingReader(WorldStandardValues.IMAGE_FILE, expectedImageFile);
        assertEquals(expectedImageFile, reader.getSetting(WorldStandardValues.IMAGE_FILE, "wrong.png"));
    }
}
