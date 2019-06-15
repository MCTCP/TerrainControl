package com.pg85.otg.configuration.io;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.customobjects.CustomObjectConfigFunction;
import com.pg85.otg.configuration.settingType.Setting;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;

public class BracketSettingsReaderOTGPlus implements SettingsReaderOTGPlus
{
    private SettingsReaderOTGPlus fallback;
    private final String name;
    private final Map<String, String> settingsMap;

    public BracketSettingsReaderOTGPlus(String name, String rawSettings)
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
    public <T> void addConfigFunction(CustomObjectConfigFunction<T> function)
    {
        // Empty, doesn't support config functions
    }

    @Override
    public <T> List<CustomObjectConfigFunction<T>> getConfigFunctions(T holder, boolean useFallback)
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
            }
            catch (InvalidConfigException e)
            {
                OTG.log(LogMarker.WARN, "The value \"{}\" is not valid for the setting {} in {}: {}", stringValue, setting, name, e.getMessage());
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
    public void setFallbackReader(SettingsReaderOTGPlus reader)
    {
        this.fallback = reader;
    }
}
