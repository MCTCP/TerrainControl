package com.pg85.otg.config;

import com.pg85.otg.config.io.IConfigFunctionProvider;
import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.standard.BiomeStandardValues;
import com.pg85.otg.config.standard.PluginConfigStandardValues;
import com.pg85.otg.config.standard.WorldStandardValues;
import com.pg85.otg.constants.SettingsEnums.ConfigMode;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.logging.Logger.LogLevels;
import com.pg85.otg.util.interfaces.IMaterialReader;

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
	public boolean spawnLog = PluginConfigStandardValues.SPAWN_LOG.getDefaultValue(null);    

	/**
	 * Having developermode enabled means BO3's are reloaded when rejoining an SP world. 
	 */
    public boolean developerMode = PluginConfigStandardValues.DEVELOPER_MODE.getDefaultValue(null);
    
	/**
	 * Forge only: This is the number of chunks the pre-generator generates each server tick.
	 * Higher values make pre-generation faster but can cause lag and increased memory usage.
	 */
	private int pregeneratorMaxChunksPerTick = PluginConfigStandardValues.PREGENERATOR_MAX_CHUNKS_PER_TICK.getDefaultValue(null);
    
    public PluginConfig(SettingsMap settingsReader, IConfigFunctionProvider biomeResourcesManager, ILogger logger, IMaterialReader materialReader)
    {
        super(settingsReader.getName());

        this.renameOldSettings(settingsReader, logger, materialReader);
        this.readConfigSettings(settingsReader, biomeResourcesManager, false, logger, materialReader);

        this.correctSettings(true, logger);
    }

    @Override
    protected void renameOldSettings(SettingsMap reader, ILogger logger, IMaterialReader materialReader)
    {
        // Nothing to rename at the moment
    }

    @Override
    protected void correctSettings(boolean logWarnings, ILogger logger)
    {
        if (!BiomeStandardValues.BiomeConfigExtensions.contains(this.biomeConfigExtension))
        {
            String newExtension = BiomeStandardValues.BIOME_CONFIG_EXTENSION.getDefaultValue(null);
            logger.log(LogMarker.WARN, "BiomeConfig file extension {} is invalid, changing to {}",
                    this.biomeConfigExtension, newExtension);
            this.biomeConfigExtension = newExtension;
        }
    }

    @Override
    protected void readConfigSettings(SettingsMap reader, IConfigFunctionProvider biomeResourcesManager, boolean spawnLog, ILogger logger, IMaterialReader materialReader)
    {
        this.settingsMode = reader.getSetting(WorldStandardValues.SETTINGS_MODE, logger, null);
        this.logLevel = reader.getSetting(PluginConfigStandardValues.LogLevel, logger, null);
        this.biomeConfigExtension = reader.getSetting(BiomeStandardValues.BIOME_CONFIG_EXTENSION, logger, null);
        this.spawnLog = reader.getSetting(PluginConfigStandardValues.SPAWN_LOG, logger, null);
        this.pregeneratorMaxChunksPerTick = reader.getSetting(PluginConfigStandardValues.PREGENERATOR_MAX_CHUNKS_PER_TICK, logger, null);
        
        this.developerMode = reader.getSetting(PluginConfigStandardValues.DEVELOPER_MODE, logger, null);
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

        writer.putSetting(PluginConfigStandardValues.LogLevel, this.logLevel,
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
        
        writer.putSetting(PluginConfigStandardValues.SPAWN_LOG, this.spawnLog,
		        "Shows detailed information about BO3 and mob/entity spawning that is useful for OTG world devs. Use higher log levels to see more information (TRACE is the highest).",
		        "Defaults to: false");
        
        writer.smallTitle("Developer mode");
        writer.putSetting(PluginConfigStandardValues.DEVELOPER_MODE, this.developerMode,
        		"Changes the behaviour of some features to speed up development: Clears the BO2/BO3 cache whenever a world or dimension is unloaded (similar to using /otg unloadbo3s ",
        		" and recreating a world), makes the pregenerator skip light population for faster pregeneration.", 
        		"Defaults to: false"
		);         
       
        writer.putSetting(PluginConfigStandardValues.PREGENERATOR_MAX_CHUNKS_PER_TICK, this.pregeneratorMaxChunksPerTick,
		        "The number of chunks the pre-generator is allowed to generate for each server tick, shoul be between 1-5.",
		        "Higher numbers make pre-generation faster but increase memory usage and will cause lag.");
    }

    public LogLevels getLogLevel()
    {
        return logLevel;
    }	
}
