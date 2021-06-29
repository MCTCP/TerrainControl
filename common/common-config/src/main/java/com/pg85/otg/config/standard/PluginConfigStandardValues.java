package com.pg85.otg.config.standard;

import com.pg85.otg.config.settingType.Setting;
import com.pg85.otg.config.settingType.Settings;
import com.pg85.otg.logging.Logger.LogLevels;

public class PluginConfigStandardValues extends Settings
{
	// Plugin Defaults
	
	public static final Setting<LogLevels> LOG_LEVEL = enumSetting("LogLevel", LogLevels.Standard);	
	public static final Setting<Boolean> DECORATION_ENABLED = booleanSetting("DecorationEnabled", true);
	public static final Setting<Boolean> SPAWN_LOG = booleanSetting("SpawnLog", false);
	public static final Setting<Boolean> LOG_BO4_PLOTTING = booleanSetting("LogBO4Plotting", false);
	public static final Setting<Boolean> LOG_CONFIG_ERRORS = booleanSetting("LogConfigErrors", false);
	public static final Setting<Boolean> LOG_DECORATION_ERRORS = booleanSetting("LogDecorationErrors", false);
	public static final Setting<Boolean> DEVELOPER_MODE = booleanSetting("DeveloperMode", false);
	public static final Setting<Integer> WORKER_THREADS = intSetting("WorkerThreads", 0, 0, 10);
}
