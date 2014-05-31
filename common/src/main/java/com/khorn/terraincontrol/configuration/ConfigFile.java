package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.WorldConfig.ConfigMode;
import com.khorn.terraincontrol.configuration.io.FileSettingsWriter;
import com.khorn.terraincontrol.configuration.io.SettingsReader;
import com.khorn.terraincontrol.configuration.io.SettingsWriter;
import com.khorn.terraincontrol.configuration.settingType.Setting;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class ConfigFile
{
    public final SettingsReader reader;
    /**
     * Use {@link #getName()}.
     */
    @Deprecated
    public final String name;
    @Deprecated
    public final File file;

    /**
     * True if the file does not exist yet on disk, false otherwise. Used to
     * provide backwards compatible default settings.
     */
    protected final boolean isNewConfig;

    /**
     * Creates a new configuration file.
     * <p/>
     * @param name   Name of the thing that is being read,
     *               like Plains or MyBO3. May not be null.
     * @param reader Settings reader
     */
    protected ConfigFile(SettingsReader reader) throws IllegalArgumentException
    {
        this.reader = reader;
        this.isNewConfig = reader.isNewConfig();

        this.file = reader.getFile();
        this.name = reader.getName();
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
    protected <T> T readSettings(Setting<T> setting, T defaultValue)
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
    public void write(SettingsWriter writer, ConfigMode configMode) throws IOException
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
    }

    /**
     * @deprecated 29 May 2014
     * @see {@link FileSettingsWriter#writeToFile(ConfigFile, ConfigMode)}
     */
    @Deprecated
    public void writeSettingsFile(boolean comments)
    {
        FileSettingsWriter.writeToFile(this, comments ? ConfigMode.WriteAll : ConfigMode.WriteWithoutComments);
    }

    public void logIOError(IOException e)
    {
        TerrainControl.log(LogMarker.ERROR, "Failed to write to file {}", file);
        TerrainControl.printStackTrace(LogMarker.ERROR, e);
    }

    protected abstract void writeConfigSettings(SettingsWriter writer) throws IOException;

    protected abstract void readConfigSettings();

    protected abstract void correctSettings();

    protected abstract void renameOldSettings();

    /**
     * Renames an old setting. If the old setting isn't found, this does
     * nothing.
     *
     * @param oldValue Name of the old setting.
     * @param newValue The new setting.
     */
    protected void renameOldSetting(String oldValue, Setting<?> newValue)
    {
        reader.renameOldSetting(oldValue, newValue);
    }

    /**
     * Silently corrects the given number so that it is higher than the
     * minimum value.
     * @param currentValue The current value, will be corrected if needed.
     * @param minimumValue The minimum value.
     * @return The corrected value.
     */
    protected int higherThan(int currentValue, int minimumValue)
    {
        if (currentValue <= minimumValue)
        {
            return minimumValue + 1;
        }
        return currentValue;
    }

    /**
     * Silently corrects the given number so that it is higher than or equal
     * to the minimum value.
     * @param currentValue The current value, will be corrected if needed.
     * @param minimumValue The minimum value.
     * @return The corrected value.
     */
    protected double higherThan(double currentValue, double minimumValue)
    {
        if (currentValue < minimumValue)
        {
            return minimumValue;
        }
        return currentValue;
    }

    /**
     * Silently corrects the given number so that it is lower than or equal
     * to the maximum value.
     * @param currentValue The current value, will be corrected if needed.
     * @param maximumValue The maximum value.
     * @return The corrected value.
     */
    protected int lowerThanOrEqualTo(int currentValue, int maximumValue)
    {
        if (currentValue > maximumValue)
        {
            return maximumValue;
        }
        return currentValue;
    }

    protected ArrayList<String> filterBiomes(List<String> biomes, Set<String> customBiomes)
    {
        ArrayList<String> output = new ArrayList<String>();

        for (String key : biomes)
        {
            key = key.trim();
            if (customBiomes.contains(key))
            {
                output.add(key);
                continue;
            }

            if (DefaultBiome.Contain(key))
                output.add(key);

        }
        return output;
    }

    protected static void writeStringToStream(DataOutputStream stream, String value) throws IOException
    {
        byte[] bytes = value.getBytes();
        stream.writeShort(bytes.length);
        stream.write(bytes);
    }

    public static String readStringFromStream(DataInputStream stream) throws IOException
    {
        byte[] chars = new byte[stream.readShort()];
        if (stream.read(chars, 0, chars.length) != chars.length)
            throw new EOFException();

        return new String(chars);
    }

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
