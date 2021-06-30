package com.pg85.otg.config;

import com.pg85.otg.config.io.IConfigFunctionProvider;
import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.io.SimpleSettingsMap;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.minecraft.BiomeRegistryNames;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all configuration files. Configuration files read
 * the desired settings from a {@link SettingsMap}, and can then write the
 * settings back to such a map.
 *
 */
public abstract class ConfigFile
{
	protected final String configName;

	/**
	 * Creates a new config file.
	 *
	 * @param configName The name of the config. For worlds, this is the world
	 *					name, for biomes this is the biome name, etc.
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
		SettingsMap settingsMap = new SimpleSettingsMap(configName);
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
	protected abstract void readConfigSettings(SettingsMap reader, IConfigFunctionProvider biomeResourcesManager, ILogger logger, IMaterialReader materialReader);

	/**
	 * Called directly after {@link #readConfigSettings(SettingsMap)} to fix
	 * impossible combinations of settings.
	 */
	protected abstract void validateAndCorrectSettings(Path settingsDir, ILogger logger);

	/**
	 * Called before {@link #readConfigSettings(SettingsMap)} to rewrite
	 * configs in old formats to the modern format, so that they can be read.
	 * @param reader The settings reader.
	 */
	protected abstract void renameOldSettings(SettingsMap reader, ILogger logger, IMaterialReader materialReader);

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

	protected ArrayList<String> filterBiomes(List<String> biomes, ArrayList<String> customBiomes)
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

			if (BiomeRegistryNames.Contain(key))
			{
				output.add(key);
			}
		}
		return output;
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
