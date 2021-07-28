package com.pg85.otg.config;

import com.pg85.otg.constants.SettingsEnums.ConfigMode;
import com.pg85.otg.constants.SettingsEnums.LogLevels;
import com.pg85.otg.interfaces.IPluginConfig;

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
public abstract class PluginConfigBase extends ConfigFile implements IPluginConfig
{
	protected LogLevels logLevel;
	protected ConfigMode settingsMode;
	protected int workerThreads;
	protected boolean developerMode;
	protected boolean logCustomObjects;
	protected boolean logStructurePlotting;
	protected boolean logConfigs;
	protected boolean logPerformance;	
	protected boolean logDecoration;
	protected boolean logBiomeRegistry;
	protected boolean decorationEnabled;
	protected boolean logMobs;
	
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
	public int getMaxWorkerThreads()
	{
		return this.workerThreads;
	}

	@Override
	public boolean getDeveloperModeEnabled()
	{
		return this.developerMode;
	}

	@Override
	public boolean logCustomObjects()
	{
		return this.logCustomObjects;
	}
	
	@Override
	public boolean logStructurePlotting()
	{
		return this.logStructurePlotting;
	}
	
	@Override
	public boolean logConfigs()
	{
		return this.logConfigs;
	}	

	@Override
	public boolean logDecoration()
	{
		return this.logDecoration;
	}

	@Override
	public boolean logBiomeRegistry()
	{
		return this.logBiomeRegistry;
	}
	
	@Override
	public boolean logPerformance()
	{
		return this.logPerformance;
	}	
	
	@Override
	public boolean getDecorationEnabled()
	{
		return this.decorationEnabled;
	}

	@Override
	public boolean logMobs()
	{
		return this.logMobs;
	}

	@Override
	public ConfigMode getSettingsMode()
	{
		return this.settingsMode;
	}
}
