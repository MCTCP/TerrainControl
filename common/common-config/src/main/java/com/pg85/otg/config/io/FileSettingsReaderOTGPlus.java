package com.pg85.otg.config.io;

import com.pg85.otg.config.customobjects.CustomObjectConfigFunction;
import com.pg85.otg.config.customobjects.CustomObjectResourcesManager;
import com.pg85.otg.config.helpers.InheritanceHelper;
import com.pg85.otg.config.settingType.Setting;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.interfaces.IMaterialReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * A settings reader that reads from a file.
 *
 */
public class FileSettingsReaderOTGPlus implements SettingsReaderOTGPlus
{  
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
    private SettingsReaderOTGPlus fallback;
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
    public FileSettingsReaderOTGPlus(String name, File file, ILogger logger)
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
    public <T> List<CustomObjectConfigFunction<T>> getConfigFunctions(T holder, boolean useFallback, boolean spawnLog, ILogger logger, IMaterialReader materialReader, CustomObjectResourcesManager manager)
    {
        List<CustomObjectConfigFunction<T>> result = new ArrayList<CustomObjectConfigFunction<T>>(configFunctions.size());
        for (StringOnLine configFunctionLine : configFunctions)
        {
            String configFunctionString = configFunctionLine.string;
            int bracketIndex = configFunctionString.indexOf('(');
            String functionName = configFunctionString.substring(0, bracketIndex);
            String parameters = configFunctionString.substring(bracketIndex + 1, configFunctionString.length() - 1);
            List<String> args = Arrays.asList(StringHelper.readCommaSeperatedString(parameters));
            CustomObjectConfigFunction<T> function = manager.getConfigFunction(functionName, holder, args, spawnLog, logger, materialReader);
            if(function == null)
            {
            	function = manager.getConfigFunction(functionName, holder, args, spawnLog, logger, materialReader);	
            }
            result.add(function);
            if (!function.isValid() && spawnLog)
            {
            	logger.log(LogMarker.WARN, "Invalid resource {} in {} on line {}: {}", functionName, this.name, configFunctionLine.line, function.getError());
            }
        }

        // Add inherited functions
        if (useFallback && fallback != null)
        {
            return InheritanceHelper.mergeListsCustomObject(result, fallback.getConfigFunctions(holder, true, spawnLog, logger, materialReader, manager));
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
    public <S> S getSetting(Setting<S> setting, S defaultValue, boolean spawnLog, ILogger logger, IMaterialReader materialReader, CustomObjectResourcesManager manager)
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
            	logger.log(LogMarker.WARN, "The value \"{}\" is not valid for the setting {} in {} on line {}: {}", stringValue, setting, name, stringWithLineNumber.line, e.getMessage());
            }
        }

        // Try the fallback
        if (fallback != null)
        {
            return fallback.getSetting(setting, defaultValue, spawnLog, logger, materialReader, manager);
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

    public void readSettings(ILogger logger)
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
        	logger.printStackTrace(LogMarker.FATAL, e);
        } finally {
            if (settingsReader != null)
            {
                try
                {
                    settingsReader.close();
                } catch (IOException localIOException2)
                {
                	logger.printStackTrace(LogMarker.FATAL, localIOException2);
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
    public void setFallbackReader(SettingsReaderOTGPlus reader)
    {
        this.fallback = reader;
    }
}
