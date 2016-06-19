package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.configuration.io.SettingsMap;
import com.khorn.terraincontrol.configuration.io.SimpleSettingsMap;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Abstract base class for all configuration files. Configuration files read
 * the desired settings from a {@link SettingsMap}, and can then write the
 * settings back to such a map.
 *
 */
public abstract class ConfigFile
{
    private final String configName;

    /**
     * Creates a new config file.
     *
     * @param configName The name of the config. For worlds, this is the world
     *                   name, for biomes this is the biome name, etc.
     */
    protected ConfigFile(String configName)
    {
        this.configName = configName;
    }

    /**
     * Gets all settings of this config file.
     * @return All settings.
     */
    public SettingsMap getSettingsAsMap()
    {
        SettingsMap settingsMap = new SimpleSettingsMap(configName, false);
        writeConfigSettings(settingsMap);
        return settingsMap;
    }

    /**
     * Methods that subclasses must override to write the actual settings.
     * @param settingsMap The map to write the settings to.
     */
    protected abstract void writeConfigSettings(SettingsMap settingsMap);

    /**
     * Called once to read all configuration settings from the
     * {@link SettingsMap} provided to the constructor.
     * @param reader The settings reader.
     */
    protected abstract void readConfigSettings(SettingsMap reader);

    /**
     * Called directly after {@link #readConfigSettings(SettingsMap)} to fix
     * impossible combinations of settings.
     */
    protected abstract void correctSettings();

    /**
     * Called before {@link #readConfigSettings(SettingsMap)} to rewrite
     * configs in old formats to the modern format, so that they can be read.
     * @param reader The settings reader.
     */
    protected abstract void renameOldSettings(SettingsMap reader);

    /**
     * Silently corrects the given number so that it is higher than or equal to
     * the minimum value.
     * @param currentValue The current value, will be corrected if needed.
     * @param minimumValue The minimum value.
     * @return The corrected value.
     */
    protected final int higherThanOrEqualTo(int currentValue, int minimumValue)
    {
        if (currentValue < minimumValue)
        {
            return minimumValue;
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

    public static void writeStringToStream(DataOutput stream, String value) throws IOException
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
        return configName;
    }

}
