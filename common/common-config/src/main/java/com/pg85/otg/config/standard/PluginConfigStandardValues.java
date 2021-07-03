package com.pg85.otg.config.standard;

import com.pg85.otg.config.settingType.Setting;
import com.pg85.otg.config.settingType.Settings;
import com.pg85.otg.constants.SettingsEnums.LogLevels;

public class PluginConfigStandardValues extends Settings
{
	// Plugin Defaults
	
	public static final Setting<LogLevels> LOG_LEVEL = enumSetting("LogLevel", LogLevels.Standard);	
	public static final Setting<Boolean> DECORATION_ENABLED = booleanSetting("DecorationEnabled", true);
	public static final Setting<Boolean> LOG_CUSTOM_OBJECTS = booleanSetting("LogCustomObjects", false);
	public static final Setting<Boolean> LOG_BO4_PLOTTING = booleanSetting("LogBO4Plotting", false);
	public static final Setting<Boolean> LOG_CONFIGS = booleanSetting("LogConfigs", false);
	public static final Setting<Boolean> LOG_BIOME_REGISTRY = booleanSetting("LogBiomeRegistry", false);
	public static final Setting<Boolean> LOG_DECORATION = booleanSetting("LogDecoration", false);
	public static final Setting<Boolean> LOG_MOBS = booleanSetting("LogMobs", false);
	public static final Setting<Boolean> LOG_PERFORMANCE = booleanSetting("LogPerformance", false);
	public static final Setting<Boolean> DEVELOPER_MODE = booleanSetting("DeveloperMode", false);
	public static final Setting<Integer> WORKER_THREADS = intSetting("WorkerThreads", 0, 0, 10);
}
