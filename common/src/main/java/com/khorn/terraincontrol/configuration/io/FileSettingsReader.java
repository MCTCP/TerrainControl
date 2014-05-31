package com.khorn.terraincontrol.configuration.io;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.configuration.ConfigFunctionsManager;
import com.khorn.terraincontrol.configuration.settingType.Setting;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.helpers.StringHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class FileSettingsReader implements SettingsReader
{
    private final List<String> configFunctions;
    private SettingsReader fallback;
    private final File file;

    private final String name;

    /**
     * Stores all the settings. Settings like Name:Value or Name=Value are
     * stored as name. Because this is a linked hashmap,
     * you're guaranteed that the lines will be read in order when iterating
     * over this map.
     */
    private final Map<String, String> settingsCache;

    /**
     * Creates a new settings reader.
     * @param name Name of the config file, like "WorldConfig" or "Taiga".
     * @param file File where the settings are stored.
     */
    public FileSettingsReader(String name, File file)
    {
        this.name = name;
        this.file = file;
        this.settingsCache = new HashMap<String, String>();
        this.configFunctions = new ArrayList<String>();

        readSettings();
    }

    @Override
    public <T> void addConfigFunction(ConfigFunction<T> function)
    {
        configFunctions.add(function.write());
    }

    @Override
    public <T> List<ConfigFunction<T>> getConfigFunctions(T holder)
    {
        List<ConfigFunction<T>> result = new ArrayList<ConfigFunction<T>>(configFunctions.size());
        ConfigFunctionsManager manager = TerrainControl.getConfigFunctionsManager();
        for (String configFunctionString : configFunctions)
        {
            int bracketIndex = configFunctionString.indexOf('(');
            String functionName = configFunctionString.substring(0, bracketIndex);
            String parameters = configFunctionString.substring(bracketIndex + 1, configFunctionString.length() - 1);
            List<String> args = Arrays.asList(StringHelper.readCommaSeperatedString(parameters));
            try
            {
                result.add(manager.getConfigFunction(functionName, holder, args));
            } catch (InvalidConfigException e)
            {
                TerrainControl.log(LogMarker.WARN, "Invalid resource {} in {}: {}", functionName, this.name, e.getMessage());
                // TODO add replacement resource with correct write(), so that
                // no information gets lost
            }
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
        return Collections.unmodifiableCollection(settingsCache.entrySet());
    }

    @Override
    public <S> S getSetting(Setting<S> setting, S defaultValue)
    {
        // Try reading the setting from the file
        String stringValue = this.settingsCache.get(setting.getName().toLowerCase());
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
        this.settingsCache.put(setting.getName().toLowerCase(), setting.write(value));
    }

    private void readSettings()
    {
        BufferedReader settingsReader = null;

        if (!file.exists())
        {
            return;
        }

        try
        {
            settingsReader = new BufferedReader(new FileReader(file));
            String thisLine;
            while ((thisLine = settingsReader.readLine()) != null)
            {
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
                        this.configFunctions.add(thisLine.trim());
                    } else
                    {
                        // : is first, so it's a setting
                        String[] splitSettings = thisLine.split(":", 2);
                        this.settingsCache.put(splitSettings[0].trim().toLowerCase(), splitSettings[1].trim());
                    }
                } else if (thisLine.contains("="))
                {
                    // Setting (old style), split it and add it
                    String[] splitSettings = thisLine.split("=", 2);
                    this.settingsCache.put(splitSettings[0].trim().toLowerCase(), splitSettings[1].trim());
                }
            }
        } catch (IOException e)
        {
            TerrainControl.printStackTrace(LogMarker.FATAL, e);

            if (settingsReader != null)
            {
                try
                {
                    settingsReader.close();
                } catch (IOException localIOException1)
                {
                    TerrainControl.printStackTrace(LogMarker.FATAL, localIOException1);
                }
            }
        } finally
        {
            if (settingsReader != null)
            {
                try
                {
                    settingsReader.close();
                } catch (IOException localIOException2)
                {
                    TerrainControl.printStackTrace(LogMarker.FATAL, localIOException2);
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
    public void setFallbackReader(SettingsReader reader)
    {
        this.fallback = reader;
    }

}
