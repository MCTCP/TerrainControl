package com.pg85.otg.config.io;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.ErroredFunction;
import com.pg85.otg.config.io.RawSettingValue.ValueType;
import com.pg85.otg.config.settingType.Setting;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IPluginConfig;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import java.text.MessageFormat;
import java.util.*;

/**
 * The default implementation of {@link SettingsMap}.
 */
public final class SimpleSettingsMap implements SettingsMap
{
	private final List<RawSettingValue> configFunctions;
	private SettingsMap fallback;
	private final String name;

	/**
	 * Stores all the settings. Settings like Name:Value or Name=Value are
	 * stored as name. Because this is a linked hashmap,
	 * you're guaranteed that the lines will be read in order when iterating
	 * over this map.
	 */
	private final Map<String, RawSettingValue> settingsCache;
	private int dummyKeyIndex = 0;

	/**
	 * Creates a new settings reader.
	 * @param name		Name of the config file, like "WorldConfig" or "Taiga".
	 * //@param isNewConfig True if this config is newly created.
	 */
	public SimpleSettingsMap(String name)
	{
		this.name = name;
		this.settingsCache = new LinkedHashMap<String, RawSettingValue>();
		this.configFunctions = new ArrayList<RawSettingValue>();
	}

	/**
	 * Returns a dummy key suitable as a key for {@link #settingsCache}.
	 *
	 * <p>Settings are indexed by their name in {@link #settingsCache}, but what
	 * are functions and titles indexed by? There are no useful options here,
	 * so we just use a dummy key.
	 * @return A dummy key.
	 */
	private String nextDummyKey()
	{
		dummyKeyIndex++;
		return "__key" + dummyKeyIndex;
	}

	@Override
	public void addConfigFunctions(Collection<? extends ConfigFunction<?>> functions)
	{
		for (ConfigFunction<?> function : functions)
		{
			RawSettingValue value = RawSettingValue.create(ValueType.FUNCTION, function.toString());

			configFunctions.add(value);
			settingsCache.put(nextDummyKey(), value);
		}
	}

	@Override
	public <T> List<ConfigFunction<T>> getConfigFunctions(T holder, IConfigFunctionProvider biomeResourcesManager, ILogger logger, IMaterialReader materialReader)
	{
		return this.getConfigFunctions(holder, biomeResourcesManager, logger, materialReader, null, null);
	}

	@Override
	public <T> List<ConfigFunction<T>> getConfigFunctions(T holder, IConfigFunctionProvider biomeResourcesManager, ILogger logger, IMaterialReader materialReader, String presetFolderName, IPluginConfig conf)
	{
		List<ConfigFunction<T>> result = new ArrayList<ConfigFunction<T>>(configFunctions.size());
		for (RawSettingValue configFunctionLine : configFunctions)
		{
			String configFunctionString = configFunctionLine.getRawValue();
			int bracketIndex = configFunctionString.indexOf('(');
			String functionName = configFunctionString.substring(0, bracketIndex);
			String parameters = configFunctionString.substring(bracketIndex + 1, configFunctionString.length() - 1);
			List<String> args = Arrays.asList(StringHelper.readCommaSeperatedString(parameters));
			ConfigFunction<T> function = biomeResourcesManager.getConfigFunction(functionName, holder, args, logger, materialReader);
			if (function == null)
			{
				// Function is in wrong config file,
				// allowed for config file inheritance.
				continue;
			}
			result.add(function);
			if (conf == null || presetFolderName == null) {
				if (logger.getLogCategoryEnabled(LogCategory.CONFIGS) && function instanceof ErroredFunction)
				{
					logger.log(
							LogLevel.ERROR,
							LogCategory.CONFIGS,
							MessageFormat.format(
									"Invalid resource {0} in {1} on line {2}: {3}",
									functionName,
									this.name,
									configFunctionLine.getLineNumber(),
									((ErroredFunction<?>)function).error
							)
					);
				}
			} else {
				if (logger.getLogCategoryEnabled(LogCategory.CONFIGS) && function instanceof ErroredFunction && logger.canLogForPreset(presetFolderName))
				{
					logger.log(
							LogLevel.ERROR,
							LogCategory.CONFIGS,
							MessageFormat.format(
									"Invalid resource {0} in {1} on line {2}: {3}",
									functionName,
									this.name,
									configFunctionLine.getLineNumber(),
									((ErroredFunction<?>)function).error
							)
					);
				}
			}
		}

		return result;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Collection<RawSettingValue> getRawSettings()
	{
		return Collections.unmodifiableCollection(this.settingsCache.values());
	}

	@Override
	public <S> S getSetting(Setting<S> setting, ILogger logger)
	{
		return getSetting(setting, logger, null);
	}
	
	@Override
	public <S> S getSetting(Setting<S> setting, ILogger logger, IMaterialReader materialReader)
	{
		return getSetting(setting, setting.getDefaultValue(materialReader), logger, materialReader);
	}

	@Override
	public <S> S getSetting(Setting<S> setting, S defaultValue, ILogger logger)
	{
		return getSetting(setting, defaultValue, logger, null);
	}
	
	@Override
	public <S> S getSetting(Setting<S> setting, S defaultValue, ILogger logger, IMaterialReader materialReader)
	{
		// Try reading the setting from the file
		RawSettingValue stringWithLineNumber = this.settingsCache.get(setting.getName().toLowerCase());
		if (stringWithLineNumber != null)
		{
			String stringValue = stringWithLineNumber.getRawValue().split(":", 2)[1].trim();
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
							name, 
							stringWithLineNumber.getLineNumber(), 
							e.getMessage()
						)
					);
				}
			}
		}

		// Try the fallback
		if (fallback != null)
		{
			return fallback.getSetting(setting, defaultValue, logger, materialReader);
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
	public <S> void putSetting(Setting<S> setting, S value, String... comments)
	{
		RawSettingValue settingValue = RawSettingValue.ofPlainSetting(setting, value).withComments(comments);
		this.settingsCache.put(setting.getName().toLowerCase(), settingValue);
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
	public void setFallback(SettingsMap reader)
	{
		this.fallback = reader;
	}

	@Override
	public void addRawSetting(RawSettingValue value)
	{
		switch (value.getType())
		{
			case PLAIN_SETTING:
				String[] split = value.getRawValue().split(":", 2);
				String settingName = split[0].toLowerCase().trim();
				this.settingsCache.put(settingName, value);
				break;
			case FUNCTION:
				this.configFunctions.add(value);
				this.settingsCache.put(nextDummyKey(), value);
				break;
			default:
				this.settingsCache.put(nextDummyKey(), value);
				break;
		}
	}

	@Override
	public void smallTitle(String title, String... comments)
	{
		this.settingsCache.put(nextDummyKey(), RawSettingValue.create(ValueType.SMALL_TITLE, title).withComments(comments));
	}
	
	@Override
	public void header1(String title, String... comments)
	{
		this.settingsCache.put(nextDummyKey(), RawSettingValue.create(ValueType.BIG_TITLE, title).withComments(comments));
	}

	@Override
	public void header2(String title, String... comments)
	{
		this.settingsCache.put(nextDummyKey(), RawSettingValue.create(ValueType.BIG_TITLE_2, title).withComments(comments));
	}

	@Override
	public String toString()
	{
		return "SimpleSettingsMap [name=" + name + ", fallback=" + fallback + "]";
	}

}
