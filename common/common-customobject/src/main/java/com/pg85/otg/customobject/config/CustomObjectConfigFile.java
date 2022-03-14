package com.pg85.otg.customobject.config;

import com.pg85.otg.config.settingType.Setting;
import com.pg85.otg.constants.SettingsEnums.ConfigMode;
import com.pg85.otg.customobject.bofunctions.BlockFunction;
import com.pg85.otg.customobject.config.io.SettingsReaderBO4;
import com.pg85.otg.customobject.config.io.SettingsWriterBO4;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.ICustomObjectManager;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;

import java.io.*;
import java.nio.file.Path;

public abstract class CustomObjectConfigFile
{
	public SettingsReaderBO4 reader;
	public ConfigMode settingsMode;

	/**
	 * Creates a new configuration file.
	 * <p/>
	 * @param reader Settings reader
	 */
	protected CustomObjectConfigFile(SettingsReaderBO4 reader) throws IllegalArgumentException
	{
		this.reader = reader;
	}

	/**
	 * Reads a setting. If the setting has an invalid value,
	 * a message is logged and the default value is returned.
	 * @param setting The setting to read.
	 * @return The value of the setting.
	 */
	protected <T> T readSettings(Setting<T> setting, ILogger logger, IMaterialReader materialReader, CustomObjectResourcesManager manager)
	{
		return readSettings(setting, setting.getDefaultValue(materialReader), logger, materialReader, manager);
	}

	/**
	 * Reads a setting. This method allows you to provide another default
	 * value. If the setting has an invalid value, a message is logged and
	 * the default value is returned.
	 * @param setting	  The setting to read.
	 * @param defaultValue Default value for the setting.
	 * @return The value of the setting.
	 */
	private <T> T readSettings(Setting<T> setting, T defaultValue, ILogger logger, IMaterialReader materialReader, CustomObjectResourcesManager manager)
	{
		return reader.getSetting(setting, defaultValue, logger, materialReader, manager);
	}

	/**
	 * Sets the config mode and opens the writer for writing. After writing is
	 * done, the writer is closed.
	 * @param writer The writer.
	 * @param configMode
	 * @throws IOException
	 */
	public void write(SettingsWriterBO4 writer, ConfigMode configMode, ILogger logger, IMaterialReader materialReader, CustomObjectResourcesManager manager) throws IOException
	{
		if (configMode == ConfigMode.WriteDisable)
		{
			throw new IOException("ConfigMode is " + ConfigMode.WriteDisable);
		}
		writer.setConfigMode(configMode);
		try
		{
			writer.open();
			writeConfigSettings(writer, logger, materialReader, manager);
		} finally
		{
			writer.close(logger);
		}

		/*
		if(writer.getFile().getName().toLowerCase().trim().endsWith(".bo3") && this instanceof BO4Config)
		{
			// Rename BO3 to BO4
			File newFile = new File(writer.getFile().getParentFile(), this.getName() + ".BO4");
			if (!writer.getFile().renameTo(newFile))
			{
				logger.log(LogMarker.INFO, "Could not rename file " + newFile.getName() + " to BO4, the file may be in use.");
			}
		}
		*/
	}

	protected abstract void writeConfigSettings(SettingsWriterBO4 writer, ILogger logger, IMaterialReader materialReader, CustomObjectResourcesManager manager) throws IOException;

	protected abstract void readConfigSettings(String presetFolderName, Path otgRootFolder, ILogger logger, ICustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker) throws InvalidConfigException;
	
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

	public abstract BlockFunction<?>[] getBlockFunctions(String presetFolderName, Path otgRootFolder, ILogger logger, ICustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker);
}
