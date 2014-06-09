package com.khorn.terraincontrol.configuration.io;

import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.configuration.settingType.Setting;
import com.khorn.terraincontrol.util.helpers.InheritanceHelper;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

public class MemorySettingsReader implements SettingsReader
{
    private final List<ConfigFunction<?>> configFunctions = new ArrayList<ConfigFunction<?>>();
    private SettingsReader fallback;
    private final String name;
    private final Map<Setting<?>, Object> settings = new HashMap<Setting<?>, Object>();

    public MemorySettingsReader(String name)
    {
        this.name = name;
    }

    @Override
    public <T> void addConfigFunction(ConfigFunction<T> function)
    {
        configFunctions.add(function);
    }

    @Override
    public <T> List<ConfigFunction<T>> getConfigFunctions(T holder, boolean useFallback)
    {
        // I wonder if this can be done more elegantly. The functions list has
        // no type parameter. We would need to add T as a type parameters for
        // this class to avoid this ugly cast.
        @SuppressWarnings({"unchecked", "rawtypes"})
        List<ConfigFunction<T>> functions = (List<ConfigFunction<T>>) (List) configFunctions;

        // Now add all parent resources
        if (useFallback && fallback != null)
        {
            return InheritanceHelper.mergeLists(functions, fallback.getConfigFunctions(holder, true));
        }

        return functions;
    }

    @Override
    public File getFile()
    {
        // Doesn't read from a file
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
        @SuppressWarnings("unchecked")
        // ^ Safety enforced by putSetting
        S value = (S) settings.get(setting);
        if (value != null)
        {
            return value;
        }

        // Add inherited functions
        if (fallback != null)
        {
            return fallback.getSetting(setting, defaultValue);
        }

        // Use default
        return defaultValue;
    }

    @Override
    public boolean hasSetting(Setting<?> setting)
    {
        return settings.containsKey(setting);
    }

    @Override
    public boolean isNewConfig()
    {
        return true;
    }

    @Override
    public <S> void putSetting(Setting<S> setting, S value)
    {
        this.settings.put(setting, value);
    }

    @Override
    public void renameOldSetting(String oldValue, Setting<?> newValue)
    {
        // Does nothing!
    }

    @Override
    public void setFallbackReader(SettingsReader reader)
    {
        this.fallback = reader;
    }

}
