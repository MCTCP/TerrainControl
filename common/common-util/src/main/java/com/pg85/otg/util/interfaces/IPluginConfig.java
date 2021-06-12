package com.pg85.otg.util.interfaces;

import com.pg85.otg.constants.SettingsEnums.ConfigMode;
import com.pg85.otg.logging.Logger.LogLevels;

/**
 * OTG.ini / PluginConfig classes
 * 
 * IPluginConfig defines anything that's used/exposed between projects.
 * PluginConfigBase implements anything needed for IWorldConfig. 
 * PluginConfig contains only fields/methods used for io/serialisation/instantiation.
 * 
 * PluginConfig should be used only in common-core and platform-specific layers, when reading/writing settings on app start.
 * IPluginConfig should be used wherever settings are used in code.
 */
public interface IPluginConfig
{
    public LogLevels getLogLevel();
    public int getMaxWorkerThreads();
    public boolean getDeveloperModeEnabled();
    public boolean getSpawnLogEnabled();
    public ConfigMode getSettingsMode();
}
