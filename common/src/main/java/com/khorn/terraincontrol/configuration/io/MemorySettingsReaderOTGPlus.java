package com.khorn.terraincontrol.configuration.io;

import com.khorn.terraincontrol.configuration.CustomObjectConfigFunction;
import com.khorn.terraincontrol.configuration.settingType.Setting;
import com.khorn.terraincontrol.util.helpers.InheritanceHelper;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

public class MemorySettingsReaderOTGPlus implements SettingsReaderOTGPlus
{
    private final List<CustomObjectConfigFunction<?>> configFunctions = new ArrayList<CustomObjectConfigFunction<?>>();
    private SettingsReaderOTGPlus fallback;
    private final String name;
    private final Map<Setting<?>, Object> settings = new HashMap<Setting<?>, Object>();

    public MemorySettingsReaderOTGPlus(String name)
    {
        this.name = name;
    }

    @Override
    public <T> void addConfigFunction(CustomObjectConfigFunction<T> function)
    {
        configFunctions.add(function);
    }

    @Override
    public <T> List<CustomObjectConfigFunction<T>> getConfigFunctions(T holder, boolean useFallback)
    {
        // I wonder if this can be done more elegantly. The functions list has
        // no type parameter. We would need to add T as a type parameters for
        // this class to avoid this ugly cast.
        @SuppressWarnings({"unchecked", "rawtypes"})
        List<CustomObjectConfigFunction<T>> functions = (List<CustomObjectConfigFunction<T>>) (List) configFunctions;

        // Now add all parent resources
        if (useFallback && fallback != null)
        {
            return InheritanceHelper.mergeListsCustomObject(functions, fallback.getConfigFunctions(holder, true));
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
    public void setFallbackReader(SettingsReaderOTGPlus reader)
    {
        this.fallback = reader;
    }

}
