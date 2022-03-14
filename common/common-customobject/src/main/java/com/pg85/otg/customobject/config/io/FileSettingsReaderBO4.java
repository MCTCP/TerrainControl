package com.pg85.otg.customobject.config.io;

import com.pg85.otg.config.settingType.Setting;
import com.pg85.otg.customobject.config.CustomObjectConfigFunction;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * A settings reader that reads from a file.
 *
 */
public class FileSettingsReaderBO4 implements SettingsReaderBO4
{  
	private static final <T, C extends CustomObjectConfigFunction<T>> List<C> mergeListsCustomObject(Collection<? extends C> childList, Collection<? extends C> parentList)
	{
		List<C> returnList = new ArrayList<C>(childList);
		for (C parentFunction : parentList)
		{
			if (!hasAnalogousFunction(parentFunction, childList))
			{
				returnList.add(parentFunction);
			}
		}
		return returnList;
	}	
	
	private static final <T, C extends CustomObjectConfigFunction<T>> boolean hasAnalogousFunction(C function, Collection<? extends C> list)
	{
		for (C toCheck : list)
		{
			if (function.isAnalogousTo(toCheck))
			{
				return true;
			}
		}
		return false;
	}
	
	private static final class StringOnLine
	{
		private final String string;
		private final int line;

		private StringOnLine(String string, int line)
		{
			this.string = string;
			this.line = line;
		}
	}

	private static final class Line implements Map.Entry<String, String>
	{
		private final String key;
		private final String value;

		private Line(String key, String value)
		{
			this.key = key;
			this.value = value;
		}

		@Override
		public String getKey()
		{
			return key;
		}

		@Override
		public String getValue()
		{
			return value;
		}

		@Override
		public String setValue(String value)
		{
			throw new UnsupportedOperationException();
		}
	}
	
	private final List<StringOnLine> configFunctions;
	private SettingsReaderBO4 fallback;
	private final File file;
	private final String name;
	
	/**
	 * Stores all the settings. Settings like Name:Value or Name=Value are
	 * stored as name. Because this is a linked hashmap,
	 * you're guaranteed that the lines will be read in order when iterating
	 * over this map.
	 */
	private final Map<String, StringOnLine> settingsCache;
	
	/**
	 * Creates a new settings reader.
	 * @param name Name of the config file, like "WorldConfig" or "Taiga".
	 * @param file File where the settings are stored.
	 */
	public FileSettingsReaderBO4(String name, File file, ILogger logger)
	{
		this.name = name;
		
		this.file = file;
		this.settingsCache = new HashMap<String, StringOnLine>();
		this.configFunctions = new ArrayList<StringOnLine>();

		readSettings(logger);
	}

	public void flushCache()
	{
		this.settingsCache.clear();
		this.configFunctions.clear();
	}

	@Override
	public <T> void addConfigFunction(CustomObjectConfigFunction<T> function)
	{
		configFunctions.add(new StringOnLine(function.write(), -1));
	}

	@Override
	public <T> List<CustomObjectConfigFunction<T>> getConfigFunctions(T holder, boolean useFallback, ILogger logger, IMaterialReader materialReader, CustomObjectResourcesManager manager)
	{
		List<CustomObjectConfigFunction<T>> result = new ArrayList<CustomObjectConfigFunction<T>>(configFunctions.size());
		for (StringOnLine configFunctionLine : configFunctions)
		{
			String configFunctionString = configFunctionLine.string;
			int bracketIndex = configFunctionString.indexOf('(');
			String functionName = configFunctionString.substring(0, bracketIndex);
			String parameters = configFunctionString.substring(bracketIndex + 1, configFunctionString.length() - 1);
			List<String> args = Arrays.asList(StringHelper.readCommaSeperatedString(parameters));
			CustomObjectConfigFunction<T> function = manager.getConfigFunction(functionName, holder, args, logger, materialReader);
			if(function == null)
			{
				function = manager.getConfigFunction(functionName, holder, args, logger, materialReader);	
			}
			result.add(function);
			if (!function.isValid() && logger.getLogCategoryEnabled(LogCategory.CONFIGS))
			{
				logger.log(
					LogLevel.ERROR,
					LogCategory.CONFIGS,
					MessageFormat.format(
						"Invalid resource {0} in {1} on line {2}: {3}", 
						functionName, 
						this.name, 
						configFunctionLine.line, 
						function.getError()
					)
				);
			}
		}

		// Add inherited functions
		if (useFallback && fallback != null)
		{
			return FileSettingsReaderBO4.mergeListsCustomObject(result, fallback.getConfigFunctions(holder, true, logger, materialReader, manager));
		}

		return result;
	}

	@Override
	public File getFile()
	{
		return file;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Iterable<Entry<String, String>> getRawSettings()
	{
		List<Entry<String, String>> lines = new ArrayList<Entry<String, String>>(this.settingsCache.size());
		for (Entry<String, StringOnLine> rawSetting : this.settingsCache.entrySet())
		{
			lines.add(new Line(rawSetting.getKey(), rawSetting.getValue().string));
		}
		return lines;
	}

	@Override
	public <S> S getSetting(Setting<S> setting, S defaultValue, ILogger logger, IMaterialReader materialReader, CustomObjectResourcesManager manager)
	{
		// Try reading the setting from the file
		StringOnLine stringWithLineNumber = this.settingsCache.get(setting.getName().toLowerCase());
		if (stringWithLineNumber != null)
		{
			String stringValue = stringWithLineNumber.string;
			try
			{
				return setting.read(stringValue, materialReader);
			}
			catch (InvalidConfigException e)
			{
				if(logger.getLogCategoryEnabled(LogCategory.CONFIGS))
				{
					logger.log(
						LogLevel.ERROR, 
						LogCategory.CONFIGS,
						MessageFormat.format(
							"The value \"{0}\" is not valid for the setting {1} in {2} on line {3}: {4}", 
							stringValue, 
							setting, 
							this.name, 
							stringWithLineNumber.line, 
							e.getMessage()
						)
					);
				}
			}
		}

		// Try the fallback
		if (this.fallback != null)
		{
			return this.fallback.getSetting(setting, defaultValue, logger, materialReader, manager);
		}

		// Return default value
		return defaultValue;
	}

	@Override
	public boolean hasSetting(Setting<?> setting)
	{
		if (settingsCache.containsKey(setting.getName().toLowerCase()))
		{
			return true;
		}
		if (fallback != null)
		{
			return fallback.hasSetting(setting);
		}
		return false;
	}
		
	@Override
	public boolean isNewConfig()
	{
		return !file.exists();
	}

	@Override
	public <S> void putSetting(Setting<S> setting, S value)
	{
		this.settingsCache.put(setting.getName().toLowerCase(), new StringOnLine(setting.write(value), -1));
	}

	private void readSettings(ILogger logger)
	{
		BufferedReader settingsReader = null;

		if (!file.exists())
		{
			return;
		}

		try
		{
			settingsReader = new BufferedReader(new FileReader(file));
			int lineNumber = 0;
			String thisLine;
			while ((thisLine = settingsReader.readLine()) != null)
			{
				lineNumber++;
				if (thisLine.trim().isEmpty())
				{
					// Empty line, ignore
				} else if (thisLine.startsWith("#") || thisLine.startsWith("<"))
				{
					// Comment, ignore
				} else if (thisLine.contains(":") || thisLine.toLowerCase().contains("("))
				{
					// Setting or resource
					if (thisLine.contains("(") && (!thisLine.contains(":") || thisLine.indexOf('(') < thisLine.indexOf(':')))
					{
						// ( is first, so it's a resource
						this.configFunctions.add(new StringOnLine(thisLine.trim(), lineNumber));
					} else
					{
						// : is first, so it's a setting
						String[] splitSettings = thisLine.split(":", 2);
						this.settingsCache
								.put(splitSettings[0].trim().toLowerCase(), new StringOnLine(splitSettings[1].trim(), lineNumber));
					}
				} else if (thisLine.contains("="))
				{
					// Setting (old style), split it and add it
					String[] splitSettings = thisLine.split("=", 2);
					this.settingsCache.put(splitSettings[0].trim().toLowerCase(), new StringOnLine(splitSettings[1].trim(), lineNumber));
				}
			}
		}
		catch (IOException e)
		{
			logger.log(LogLevel.ERROR, LogCategory.CONFIGS, String.format("Exception when reading file: ", (Object[])e.getStackTrace()));
		} finally {
			if (settingsReader != null)
			{
				try
				{
					settingsReader.close();
				}
				catch (IOException localIOException2)
				{
					logger.log(LogLevel.ERROR, LogCategory.CONFIGS, String.format("Exception when closing file: ", (Object[])localIOException2.getStackTrace()));
				}
			}
		}
	}

	@Override
	public void renameOldSetting(String oldValue, Setting<?> newValue)
	{
		if (this.settingsCache.containsKey(oldValue.toLowerCase()))
		{
			this.settingsCache.put(newValue.getName().toLowerCase(), this.settingsCache.get(oldValue.toLowerCase()));
		}
	}

	@Override
	public void setFallbackReader(SettingsReaderBO4 reader)
	{
		this.fallback = reader;
	}
}
