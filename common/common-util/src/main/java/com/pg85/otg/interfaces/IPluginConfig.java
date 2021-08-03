package com.pg85.otg.interfaces;

import com.pg85.otg.constants.SettingsEnums.ConfigMode;
import com.pg85.otg.constants.SettingsEnums.LogLevels;

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
	public boolean logCustomObjects();
	public boolean logStructurePlotting();
	public boolean logConfigs();
	public boolean logPerformance();	
	public boolean logDecoration();	
	public boolean logBiomeRegistry();
	public boolean getDecorationEnabled();
	public boolean logMobs();
	public boolean canLogForPreset(String presetFolderName);
	public ConfigMode getSettingsMode();
}
