package com.pg85.otg.configuration.customobjects;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.io.SettingsReaderOTGPlus;
import com.pg85.otg.configuration.io.SettingsWriterOTGPlus;
import com.pg85.otg.configuration.settingType.Setting;
import com.pg85.otg.configuration.world.WorldConfig.ConfigMode;
import com.pg85.otg.customobjects.bo4.BO4Config;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;

import java.io.*;

public abstract class CustomObjectConfigFile
{
    public SettingsReaderOTGPlus reader;

    /**
     * Creates a new configuration file.
     * <p/>
     * @param name   Name of the thing that is being read,
     *               like Plains or MyBO3. May not be null.
     * @param reader Settings reader
     */
    protected CustomObjectConfigFile(SettingsReaderOTGPlus reader) throws IllegalArgumentException
    {
        this.reader = reader;
    }

    /**
     * Reads a setting. If the setting has an invalid value,
     * a message is logged and the default value is returned.
     * @param setting The setting to read.
     * @return The value of the setting.
     */
    protected <T> T readSettings(Setting<T> setting)
    {
        return readSettings(setting, setting.getDefaultValue());
    }

    /**
     * Reads a setting. This method allows you to provide another default
     * value. If the setting has an invalid value, a message is logged and
     * the default value is returned.
     * @param setting      The setting to read.
     * @param defaultValue Default value for the setting.
     * @return The value of the setting.
     */
    private <T> T readSettings(Setting<T> setting, T defaultValue)
    {
        return reader.getSetting(setting, defaultValue);
    }

    /**
     * Sets the config mode and opens the writer for writing. After writing is
     * done, the writer is closed.
     * @param writer The writer.
     * @param configMode
     * @throws IOException
     */
    public void write(SettingsWriterOTGPlus writer, ConfigMode configMode) throws IOException
    {
        if (configMode == ConfigMode.WriteDisable)
        {
            throw new IOException("ConfigMode is " + ConfigMode.WriteDisable);
        }
        writer.setConfigMode(configMode);
        try
        {
            writer.open();
            writeConfigSettings(writer);
        } finally
        {
            writer.close();
        }
        
        if(writer.getFile().getName().toLowerCase().trim().endsWith(".bo3") && this instanceof BO4Config)
        {
	        // Rename BO3 to BO4
	        File newFile = new File(writer.getFile().getParentFile(), this.getName() + ".BO4");
	        if (!writer.getFile().renameTo(newFile))
	        {
	        	OTG.log(LogMarker.INFO, "Could not rename file " + newFile.getName() + " to BO4, the file may be in use.");
	        }
        }
    }

    protected abstract void writeConfigSettings(SettingsWriterOTGPlus writer) throws IOException;

    protected abstract void readConfigSettings() throws InvalidConfigException;

    protected abstract void correctSettings();

    protected abstract void renameOldSettings();

    /**
     * Gets the name of this config file. For worlds, this is the world name,
     * for biomes this is the biome name, etc.
     * @return The name of this config file.
     */
    public String getName()
    {
        return reader.getName();
    }

    /**
     * Gets the file this config will be written to. May be null if the config
     * will never be written.
     * @return The file.
     */
    public File getFile()
    {
        return reader.getFile();
    }
}