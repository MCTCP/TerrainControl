package com.pg85.otg.configuration.standard;

import com.pg85.otg.configuration.PluginConfig.LogLevels;
import com.pg85.otg.configuration.settingType.Setting;
import com.pg85.otg.configuration.settingType.Settings;

public class PluginStandardValues extends Settings
{   
	// Files
	
    // Main Plugin Config
    public static final String ConfigFilename = "OTG.ini";
    
    // Folders
    public static final String BiomeConfigDirectoryName = "GlobalBiomes";
    public static final String BO_DirectoryName = "GlobalObjects";
    public static final String PresetsDirectoryName = "worlds";
    
    // Network
    public static final String ChannelName = "OpenTerrainGenerator";
    public static final int ProtocolVersion = 5;
    
    // Plugin Defaults
    public static final Setting<LogLevels> LogLevel = enumSetting("LogLevel", LogLevels.Standard);

    /**
     * Name of the plugin, "OpenTerrainGenerator".
     */
    public static final String PLUGIN_NAME = "OpenTerrainGenerator";
    
    public static final String PLUGIN_NAME_SHORT = "OTG";
        
    public static final Setting<String> REPLACE_UNKNOWN_BLOCK_WITH_MATERIAL = stringSetting("ReplaceUnknownBlockWithMaterial", "LOG");
    
    public static final Setting<Boolean> SPAWN_LOG = booleanSetting("SpawnLog", false);
    
    public static final Setting<Boolean> DEVELOPER_MODE = booleanSetting("DeveloperMode", false);
    
    public static final Setting<Integer> PREGENERATOR_MAX_CHUNKS_PER_TICK = intSetting("PregeneratorMaxChunksPerTick", 1, 1, Integer.MAX_VALUE);   
}