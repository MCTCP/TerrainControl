package com.khorn.terraincontrol.configuration.standard;

import com.khorn.terraincontrol.configuration.PluginConfig.LogLevels;
import com.khorn.terraincontrol.configuration.settingType.Setting;
import com.khorn.terraincontrol.configuration.settingType.Settings;

public class PluginStandardValues extends Settings
{
   
  //>> Files
    //>>	Main Plugin Config
    public static final String ConfigFilename = "TerrainControl.ini";
    
  //>> Folders
    public static final String BiomeConfigDirectoryName = "GlobalBiomes";
    public static final String BO_DirectoryName = "GlobalObjects";
    
  //>>  Network
    public static final String ChannelName = "TerrainControl";
    public static final int ProtocolVersion = 5;
    
  //>>  Plugin Defaults
    public static final Setting<LogLevels> LogLevel = enumSetting("LogLevel", LogLevels.Standard);

    /**
     * Name of the plugin, "Terrain Control".
     */
    public static final String PLUGIN_NAME = "Terrain Control";

}