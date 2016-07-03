package com.khorn.terraincontrol.configuration.io;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.configuration.ConfigFunctionsManager;
import com.khorn.terraincontrol.configuration.ErroredFunction;
import com.khorn.terraincontrol.configuration.io.RawSettingValue.ValueType;
import com.khorn.terraincontrol.configuration.settingType.Setting;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.helpers.InheritanceHelper;
import com.khorn.terraincontrol.util.helpers.StringHelper;

import java.util.*;

/**
 * The default implementation of {@link SettingsMap}.
 *
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
    private final boolean isNewConfig;
    private int dummyKeyIndex = 0;

    /**
     * Creates a new settings reader.
     * @param name        Name of the config file, like "WorldConfig" or "Taiga".
     * @param isNewConfig True if this config is newly created.
     */
    public SimpleSettingsMap(String name, boolean isNewConfig)
    {
        this.name = name;
        this.settingsCache = new LinkedHashMap<String, RawSettingValue>();
        this.configFunctions = new ArrayList<RawSettingValue>();
        this.isNewConfig = isNewConfig;
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
            RawSettingValue value = RawSettingValue.create(ValueType.FUNCTION,
                    function.toString());

            configFunctions.add(value);
            settingsCache.put(nextDummyKey(), value);
        }
    }

    @Override
    public <T> List<ConfigFunction<T>> getConfigFunctions(T holder, boolean useFallback)
    {
        List<ConfigFunction<T>> result = new ArrayList<ConfigFunction<T>>(configFunctions.size());
        ConfigFunctionsManager manager = TerrainControl.getConfigFunctionsManager();
        for (RawSettingValue configFunctionLine : configFunctions)
        {
            String configFunctionString = configFunctionLine.getRawValue();
            int bracketIndex = configFunctionString.indexOf('(');
            String functionName = configFunctionString.substring(0, bracketIndex);
            String parameters = configFunctionString.substring(bracketIndex + 1, configFunctionString.length() - 1);
            List<String> args = Arrays.asList(StringHelper.readCommaSeperatedString(parameters));
            ConfigFunction<T> function = manager.getConfigFunction(functionName, holder, args);
            if (function == null)
            {
                // Function is in wrong config file, allowed for config file
                // inheritance
                continue;
            }
            result.add(function);
            if (function instanceof ErroredFunction)
            {
                TerrainControl.log(LogMarker.WARN, "Invalid resource {} in {} on line {}: {}",
                        functionName, this.name,
                        configFunctionLine.getLineNumber(),
                        ((ErroredFunction<?>) function).error);
            }
        }

        // Add inherited functions
        if (useFallback && fallback != null)
        {
            return InheritanceHelper.mergeLists(result, fallback.getConfigFunctions(holder, true));
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
    public <S> S getSetting(Setting<S> setting)
    {
        return getSetting(setting, setting.getDefaultValue());
    }

    @Override
    public <S> S getSetting(Setting<S> setting, S defaultValue)
    {
        // Try reading the setting from the file
        RawSettingValue stringWithLineNumber = this.settingsCache.get(setting.getName().toLowerCase());
        if (stringWithLineNumber != null)
        {
            String stringValue = stringWithLineNumber.getRawValue().split(":", 2)[1].trim();
            try
            {
                return setting.read(stringValue);
            } catch (InvalidConfigException e)
            {
                TerrainControl.log(LogMarker.ERROR, "The value \"{}\" is not valid for the setting {} in {} on line {}: {}",
                        stringValue, setting, name, stringWithLineNumber.getLineNumber(), e.getMessage());
            }
        }

        // Try the fallback
        if (fallback != null)
        {
            return fallback.getSetting(setting, defaultValue);
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
        return isNewConfig;
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
    public void bigTitle(String title, String... comments)
    {
        this.settingsCache.put(nextDummyKey(), RawSettingValue.create(ValueType.BIG_TITLE, title).withComments(comments));
    }

    @Override
    public String toString()
    {
        return "SimpleSettingsMap [name=" + name + ", fallback=" + fallback + "]";
    }

}
