package com.pg85.otg.config;

import com.pg85.otg.constants.SettingsEnums.ConfigMode;
import com.pg85.otg.logging.Logger.LogLevels;
import com.pg85.otg.util.interfaces.IPluginConfig;

public abstract class PluginConfigBase extends ConfigFile implements IPluginConfig
{
    protected LogLevels logLevel;
    protected boolean developerMode;
    protected boolean spawnLog;
    protected ConfigMode settingsMode;

    public PluginConfigBase(String configName)
    {
        super(configName);
    }

    @Override
    public LogLevels getLogLevel()
    {
        return this.logLevel;
    }
    
    @Override
    public boolean getDeveloperModeEnabled()
    {
        return this.developerMode;
    }
    
    @Override
    public boolean getSpawnLogEnabled()
    {
        return this.spawnLog;
    }

    @Override
    public ConfigMode getSettingsMode()
    {
        return this.settingsMode;
    }
}
