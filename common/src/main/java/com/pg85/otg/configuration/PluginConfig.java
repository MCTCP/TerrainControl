package com.pg85.otg.configuration;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.io.SettingsMap;
import com.pg85.otg.configuration.standard.BiomeStandardValues;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.configuration.world.WorldConfig.ConfigMode;
import com.pg85.otg.logging.LogMarker;

/**
 * Temporarily pre-configured Class that will eventually represent the file
 * containing any and all plugin-specific settings
 */
public final class PluginConfig extends ConfigFile
{
    private LogLevels logLevel = LogLevels.Standard;
    public String biomeConfigExtension;	
    public ConfigMode settingsMode;
    
	/**
	 * Shows detailed information about mob and BO3 spawning that is useful for TC world devs.
	 */
	public boolean spawnLog = false;    

	/**
	 * Having developermode enabled means BO3's are reloaded when rejoining an SP world. 
	 */
    public boolean developerMode = false;
    
	/**
	 * Forge only: This is the number of chunks the pre-generator generates each server tick.
	 * Higher values make pre-generation faster but can cause lag and increased memory usage.
	 */
	public int pregeneratorMaxChunksPerTick = 1;
    
    public enum LogLevels
    {
        Off(LogMarker.ERROR),
        Quiet(LogMarker.WARN),
        Standard(LogMarker.INFO),
        Debug(LogMarker.DEBUG),
        Trace(LogMarker.TRACE);
        private final LogMarker marker;

        LogLevels(LogMarker marker)
        {
            this.marker = marker;
        }

        public LogMarker getLevel()
        {
            return marker;
        }

    }

    public PluginConfig(SettingsMap settingsReader)
    {

        super(settingsReader.getName());

        this.renameOldSettings(settingsReader);
        this.readConfigSettings(settingsReader);

        this.correctSettings(true);
    }

    @Override
    protected void renameOldSettings(SettingsMap reader)
    {
        // Nothing to rename at the moment
    }

    @Override
    protected void correctSettings(boolean logWarnings)
    {
        if (!BiomeStandardValues.BiomeConfigExtensions.contains(this.biomeConfigExtension))
        {
            String newExtension = BiomeStandardValues.BIOME_CONFIG_EXTENSION.getDefaultValue();
            OTG.log(LogMarker.WARN, "BiomeConfig file extension {} is invalid, changing to {}",
                    this.biomeConfigExtension, newExtension);
            this.biomeConfigExtension = newExtension;
        }
    }

    @Override
    protected void readConfigSettings(SettingsMap reader)
    {
        this.settingsMode = reader.getSetting(WorldStandardValues.SETTINGS_MODE);
        this.logLevel = reader.getSetting(PluginStandardValues.LogLevel);
        this.biomeConfigExtension = reader.getSetting(BiomeStandardValues.BIOME_CONFIG_EXTENSION);
        this.spawnLog = reader.getSetting(PluginStandardValues.SPAWN_LOG);
        this.pregeneratorMaxChunksPerTick = reader.getSetting(PluginStandardValues.PREGENERATOR_MAX_CHUNKS_PER_TICK);
        
        this.developerMode = reader.getSetting(PluginStandardValues.DEVELOPER_MODE);
    }

    @Override
    protected void writeConfigSettings(SettingsMap writer)
    {
        // The modes
        writer.bigTitle("The Open Terrain Generator Config File ");

        writer.putSetting(WorldStandardValues.SETTINGS_MODE, this.settingsMode,
                "How this config file will be treated.",
                "Possible Write Modes:",
                "   WriteAll             - Write config files with help comments",
                "   WriteWithoutComments - Write config files without help comments",
                "   WriteDisable         - Doesn't write to the config files, it only reads.",
                "                          Doesn't auto-update the configs. Use with care!",
                "Defaults to: WriteAll");

        // Custom biomes
        writer.bigTitle("Log Levels");

        writer.putSetting(PluginStandardValues.LogLevel, this.logLevel,
                "This is the level with which logs will be produced.",
                "Possible Log Levels",
                "   Off         - Bare logging; This will only show FATAL and ERROR logs",
                "   Quiet       - Minimal logging; This will show FATAL, ERROR, and WARN logs",
                "   Standard    - Default logging; This is exactly what you are used to. Quiet + INFO logs",
                "   Debug       - Above Normal logging; Standard logs + DEBUG logs",
                "   Trace       - Verbose logging; This gets very messy, Debug logs + TRACE logs",
                " ",
                "Defaults to: Standard");

        writer.bigTitle("File Extension Rules");

        writer.smallTitle("Default Biome File Extension");

        writer.putSetting(BiomeStandardValues.BIOME_CONFIG_EXTENSION, this.biomeConfigExtension,
                "Pre-TC 2.5.0, biome config files were in the form BiomeNameBiomeConfig.ini",
                "Now, biome config files are in the form BiomeName.bc.ini",
                "You may change this by choosing between the following extensions:",
                "BiomeConfig.ini, .biome, .bc, .bc.ini, and .biome.ini",
                " ",
                "Defaults to: .bc");         
        
        writer.putSetting(PluginStandardValues.SPAWN_LOG, this.spawnLog,
		        "Shows detailed information about BO3 and mob/entity spawning that is useful for OTG world devs. Use higher log levels to see more information (TRACE is the highest).",
		        "Defaults to: false");
        
        writer.smallTitle("Developer mode");
        writer.putSetting(PluginStandardValues.DEVELOPER_MODE, this.developerMode,
        		"Clears the BO2/BO3 cache whenever a world or dimension is unloaded (similar to using /otg unloadbo3s and recreating a world).",
        		"Defaults to: false"
		);         
       
        writer.putSetting(PluginStandardValues.PREGENERATOR_MAX_CHUNKS_PER_TICK, this.pregeneratorMaxChunksPerTick,
		        "The number of chunks the pre-generator is allowed to generate for each server tick, shoul be between 1-5.",
		        "Higher numbers make pre-generation faster but increase memory usage and will cause lag.");
    }

    public LogLevels getLogLevel()
    {
        return logLevel;
    }	
}
