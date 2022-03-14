package com.pg85.otg.config.io;

import com.pg85.otg.config.io.RawSettingValue.ValueType;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * A settings reader that reads from a file.
 *
 */
public class FileSettingsReader
{
	/**
	 * Reads a file.
	 * @param configName The name of the config file. For worlds, use the world
	 * name, for biomes, use the biome name, etc.
	 * @param file The file to read from.
	 * @return The settings in the file.
	 */
	public static SettingsMap read(String configName, File file, ILogger logger)
	{
		SettingsMap settingsMap = new SimpleSettingsMap(configName);
		new FileSettingsReader().readIntoMap(settingsMap, file, logger);
		return settingsMap;
	}

	/**
	 * Reads all settings in the file into the given settings map.
	 * @param settings	 The settings map.
	 * @param fileContents The contents of the file. The stream will be fully
	 *					 read, but you'll have to close the stream yourself.
	 * @throws IOException If an IO error occurs.
	 */
	private void readIntoMap(SettingsMap settings, BufferedReader fileContents) throws IOException
	{
		int lineNumber = 0;
		String thisLine;
		while ((thisLine = fileContents.readLine()) != null)
		{
			lineNumber++;
			if (thisLine.trim().isEmpty())
			{
				// Empty line, ignore
			}
			else if (thisLine.startsWith("#") || thisLine.startsWith("<"))
			{
				// Comment, ignore
			}
			else if (thisLine.contains(":") || thisLine.toLowerCase().contains("("))
			{
				// Setting or resource
				if (
						thisLine.contains("(") && 
						(
							!thisLine.contains(":") || 
							thisLine.indexOf('(') < thisLine.indexOf(':')
						)
					)
				{
					// ( is first, so it's a resource
					String configFunction = thisLine.trim();
					settings.addRawSetting(RawSettingValue.create(ValueType.FUNCTION, configFunction).withLineNumber(lineNumber));
				} else {
					// : is first, so it's a setting
					settings.addRawSetting(RawSettingValue.create(ValueType.PLAIN_SETTING, thisLine.trim()).withLineNumber(lineNumber));
				}
			}
			else if (thisLine.contains("="))
			{
				// Setting (old style), split it and add it
				String modifiedLine = thisLine.replaceFirst("=", ":").trim();
				settings.addRawSetting(RawSettingValue.create(ValueType.PLAIN_SETTING, modifiedLine).withLineNumber(lineNumber));
			}
		}
	}

	/**
	 * Reads all settings in the file into the given settings map.
	 * @param settings The settings map.
	 * @param file	 The file.
	 */
	private void readIntoMap(SettingsMap settings, File file, ILogger logger)
	{
		BufferedReader settingsReader = null;

		if (!file.exists())
		{
			return;
		}

		try
		{
			settingsReader = new BufferedReader(new FileReader(file));
			readIntoMap(settings, settingsReader);
		} catch (IOException e) {
			logger.log(LogLevel.ERROR, LogCategory.CONFIGS, String.format("Could not read file, exception: ", (Object[])e.getStackTrace()));
		} finally {
			if (settingsReader != null)
			{
				try
				{
					settingsReader.close();
				}
				catch (IOException localIOException2)
				{
					logger.log(LogLevel.ERROR, LogCategory.CONFIGS, String.format("Could not close file, exception: ", (Object[])localIOException2.getStackTrace()));
				}
			}
		}
	}
}
