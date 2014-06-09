package com.khorn.terraincontrol.configuration.io;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.configuration.settingType.Setting;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.logging.LogMarker;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

/**
 * Reads settings from a formatted string like:
 * <code>(SettingName=SettingValue;SettingName2=SettingValue2)</code>.
 *
 */
public class BracketSettingsReader implements SettingsReader
{
    private SettingsReader fallback;
    private final String name;
    private final Map<String, String> settingsMap;

    public BracketSettingsReader(String name, String rawSettings)
    {
        this.name = name;
        this.settingsMap = new HashMap<String, String>();

        String[] settings = rawSettings.split(";");
        for (String setting : settings)
        {
            String[] settingParts = setting.split("=", 2);
            String settingName = settingParts[0].toLowerCase().trim();
            if (settingName.isEmpty())
            {
                continue;
            }

            if (settingParts.length == 1)
            {
                // Boolean values
                settingsMap.put(settingName, "true");
            } else if (settingParts.length == 2)
            {
                settingsMap.put(settingName, settingParts[1].trim());
            }
        }
    }

    @Override
    public <T> void addConfigFunction(ConfigFunction<T> function)
    {
        // Empty, doesn't support config functions
    }

    @Override
    public <T> List<ConfigFunction<T>> getConfigFunctions(T holder, boolean useFallback)
    {
        // Add inherited functions
        if (useFallback && fallback != null)
        {
            return fallback.getConfigFunctions(holder, true);
        }
        return Collections.emptyList();
    }

    @Override
    public File getFile()
    {
        return null;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Iterable<Entry<String, String>> getRawSettings()
    {
        return Collections.emptyList();
    }

    @Override
    public <S> S getSetting(Setting<S> setting, S defaultValue)
    {
        // Try reading the setting from the file
        String stringValue = this.settingsMap.get(setting.getName().toLowerCase());
        if (stringValue != null)
        {
            try
            {
                return setting.read(stringValue);
            } catch (InvalidConfigException e)
            {
                TerrainControl.log(LogMarker.ERROR, "The value \"{}\" is not valid for the setting {} in {}: {}",
                        stringValue, setting, name, e.getMessage());
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
        if (settingsMap.containsKey(setting.getName().toLowerCase()))
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
        return true;
    }

    @Override
    public <S> void putSetting(Setting<S> setting, S value)
    {
        settingsMap.put(setting.getName().toLowerCase(), setting.write(value));
    }

    @Override
    public void renameOldSetting(String oldValue, Setting<?> newValue)
    {
        if (this.settingsMap.containsKey(oldValue.toLowerCase()))
        {
            this.settingsMap.put(newValue.getName().toLowerCase(), this.settingsMap.get(oldValue.toLowerCase()));
        }
    }

    @Override
    public void setFallbackReader(SettingsReader reader)
    {
        this.fallback = reader;
    }

}
