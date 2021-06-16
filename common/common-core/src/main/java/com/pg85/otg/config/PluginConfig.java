package com.pg85.otg.config;

import java.nio.file.Path;

import com.pg85.otg.config.io.IConfigFunctionProvider;
import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.standard.PluginConfigStandardValues;
import com.pg85.otg.config.standard.WorldStandardValues;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.interfaces.IMaterialReader;

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
public final class PluginConfig extends PluginConfigBase
{
	public PluginConfig(SettingsMap settingsReader, IConfigFunctionProvider biomeResourcesManager, ILogger logger, IMaterialReader materialReader)
	{
		super(settingsReader.getName());
		readConfigSettings(settingsReader, biomeResourcesManager, false, logger, materialReader);
	}

	@Override
	protected void renameOldSettings(SettingsMap reader, ILogger logger, IMaterialReader materialReader) { }

	@Override
	protected void validateAndCorrectSettings(Path settingsDir, boolean logWarnings, ILogger logger) { }

	@Override
	protected void readConfigSettings(SettingsMap reader, IConfigFunctionProvider biomeResourcesManager, boolean spawnLog, ILogger logger, IMaterialReader materialReader)
	{
		this.settingsMode = reader.getSetting(WorldStandardValues.SETTINGS_MODE, logger);
		this.logLevel = reader.getSetting(PluginConfigStandardValues.LOG_LEVEL, logger);
		this.spawnLog = reader.getSetting(PluginConfigStandardValues.SPAWN_LOG, logger);
		this.developerMode = reader.getSetting(PluginConfigStandardValues.DEVELOPER_MODE, logger);
		this.workerThreads = reader.getSetting(PluginConfigStandardValues.WORKER_THREADS, logger);
	}

	@Override
	protected void writeConfigSettings(SettingsMap writer)
	{
		writer.header1("Open Terrain Generator Config");

		writer.putSetting(WorldStandardValues.SETTINGS_MODE, this.settingsMode,
			"Possible Config Write Modes:",
			"	WriteAll			 - Write config files with help comments.",
			"	WriteWithoutComments - Write config files without help comments.",
			"	WriteDisable		 - Don't write config files, read-only.",
			"Defaults to: WriteAll",
			"Writing updates your configs to the currently installed version of OTG."
		);

		writer.putSetting(PluginConfigStandardValues.WORKER_THREADS, this.workerThreads,
			"Forge only, experimental: The amount of OTG worker threads used to speed up ",
			"base terrain and BO4 generation. Higher values may not result in better ",
			"performance, experiment to see what works best for your cpu."
		);
		
		writer.header2("Logging");

		writer.putSetting(PluginConfigStandardValues.LOG_LEVEL, this.logLevel,
			"Possible Log Levels",
			"	Off			- Bare logging; This will only show FATAL and ERROR logs",
			"	Quiet		- Minimal logging; This will show FATAL, ERROR, and WARN logs",
			"	Standard	- Default logging; Logging for normal users. Quiet + INFO logs",
			"	Debug		- Above Normal logging; Standard logs + DEBUG logs",
			"	Trace		- Verbose logging; This gets very messy, Debug logs + TRACE logs",
			"",
			"Defaults to: Standard"
		);

		writer.putSetting(PluginConfigStandardValues.SPAWN_LOG, this.spawnLog,
			"Shows detailed debugging information about BO3 and mob/entity spawning",
			"that is useful for OTG world devs. Use higher log levels to see more",
			"information (TRACE is the highest).",
			"Defaults to: false"
		);

		writer.header2("Developer settings");

		writer.putSetting(PluginConfigStandardValues.DEVELOPER_MODE, this.developerMode,
			"Changes the behaviour of some features to speed up development: Clears the",
			"BO2/BO3 cache whenever a world or dimension is unloaded (similar to using",
			"/otg unloadbo3s and recreating a world).",
			"Defaults to: false"
		);
	}
}
