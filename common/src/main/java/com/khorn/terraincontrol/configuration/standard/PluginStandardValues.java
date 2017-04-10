package com.khorn.terraincontrol.configuration.standard;

import com.khorn.terraincontrol.configuration.PluginConfig.LogLevels;
import com.khorn.terraincontrol.configuration.settingType.Setting;
import com.khorn.terraincontrol.configuration.settingType.Settings;

public class PluginStandardValues extends Settings
{   
	// Files
	
    // Main Plugin Config
    public static final String ConfigFilename = "OpenTerrainGenerator.ini";
    
    // Folders
    public static final String BiomeConfigDirectoryName = "GlobalBiomes";
    public static final String BO_DirectoryName = "GlobalObjects";
    
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
        
    public static final Setting<Boolean> SPAWN_LOG = booleanSetting("SpawnLog", false);
    
    public static final Setting<Integer> PREGENERATOR_MAX_CHUNKS_PER_TICK = intSetting("PregeneratorMaxChunksPerTick", 1, 1, Integer.MAX_VALUE);   
}