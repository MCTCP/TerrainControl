package com.pg85.otg.util.interfaces;

import com.pg85.otg.constants.SettingsEnums.ConfigMode;
import com.pg85.otg.logging.Logger.LogLevels;

public interface IPluginConfig
{
    public LogLevels getLogLevel();
    public boolean getDeveloperModeEnabled();
    public boolean getSpawnLogEnabled();
    public ConfigMode getSettingsMode();
}
