package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.configuration.WorldConfig.ConfigMode;
import com.khorn.terraincontrol.configuration.io.SettingsReader;
import com.khorn.terraincontrol.configuration.io.SettingsWriter;
import com.khorn.terraincontrol.configuration.settingType.Setting;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Abstract base class for all configuration files. Configuration files read
 * the desired settings from a {@link SettingsReader}, and write them on
 * request to a {@link SettingsWriter}.
 *
 */
public abstract class ConfigFile
{
    protected final SettingsReader reader;

    /**
     * True if the file does not exist yet on disk, false otherwise. Used to
     * provide backwards compatible default settings.
     */
    protected final boolean isNewConfig;

    /**
     * Creates a new configuration file.
     * @param name   Name of the thing that is being read,
     *               like Plains or MyBO3. May not be null.
     * @param reader Settings reader
     */
    protected ConfigFile(SettingsReader reader)
    {
        this.reader = reader;
        this.isNewConfig = reader.isNewConfig();
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
     * Writes all settings of this configuration file to the provided writer.
     * This method can be called at any time after the settings are fully read.
     * @param writer     The writer to write the setings to.
     * @param configMode The mode to use while writing the settings. May not
     * be {@link ConfigMode#WriteDisable}.
     * @throws IOException If one of the methods on the writer throws an
     * {@link IOException}, or if the {@code configMode} is
     * {@link ConfigMode#WriteDisable}.
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
     * Methods that subclasses must override to write the actual settings.
     * @param writer The writer to write the setings to.
     * @throws IOException If one of the methods on the writer throws an
     * {@link IOException}.
     */
    protected abstract void writeConfigSettings(SettingsWriter writer) throws IOException;

    /**
     * Called once to read all configuration settings from the
     * {@link SettingsReader} provided to the constructor.
     */
    protected abstract void readConfigSettings();

    /**
     * Called directly after {@link #readConfigSettings()} to fix impossible
     * combinations of settings.
     */
    protected abstract void correctSettings();

    /**
     * Called before {@link #readConfigSettings()} to rewrite configs in old
     * formats to the modern format, so that they can be read.
     */
    protected abstract void renameOldSettings();

    /**
     * Renames an old setting. If the old setting isn't found, this does
     * nothing.
     *
     * @param oldValue Name of the old setting.
     * @param newValue The new setting.
     */
    protected final void renameOldSetting(String oldValue, Setting<?> newValue)
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
    protected final int higherThan(int currentValue, int minimumValue)
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
    protected final double higherThan(double currentValue, double minimumValue)
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
    protected final int lowerThanOrEqualTo(int currentValue, int maximumValue)
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

    protected static void writeStringToStream(DataOutput stream, String value) throws IOException
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
