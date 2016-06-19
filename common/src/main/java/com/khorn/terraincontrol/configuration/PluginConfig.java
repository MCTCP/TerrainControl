package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.WorldConfig.ConfigMode;
import com.khorn.terraincontrol.configuration.io.SettingsMap;
import com.khorn.terraincontrol.configuration.standard.BiomeStandardValues;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.logging.LogMarker;

/**
 * Temporarily pre-configured Class that will eventually represent the file
 * containing any and all plugin-specific settings
 */
public final class PluginConfig extends ConfigFile
{

    public ConfigMode SettingsMode;

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

    private LogLevels LogLevel = LogLevels.Standard;
    public String biomeConfigExtension;

    public PluginConfig(SettingsMap settingsReader)
    {

        super(settingsReader.getName());

        this.renameOldSettings(settingsReader);
        this.readConfigSettings(settingsReader);

        this.correctSettings();
    }

    @Override
    protected void renameOldSettings(SettingsMap reader)
    {
        // Nothing to rename at the moment
    }

    @Override
    protected void correctSettings()
    {
        if (!BiomeStandardValues.BiomeConfigExtensions.contains(this.biomeConfigExtension))
        {
            String newExtension = BiomeStandardValues.BIOME_CONFIG_EXTENSION.getDefaultValue();
            TerrainControl.log(LogMarker.WARN, "BiomeConfig file extension {} is invalid, changing to {}",
                    this.biomeConfigExtension, newExtension);
            this.biomeConfigExtension = newExtension;
        }

    }

    @Override
    protected void readConfigSettings(SettingsMap reader)
    {
        this.SettingsMode = reader.getSetting(WorldStandardValues.SETTINGS_MODE);
        this.LogLevel = reader.getSetting(PluginStandardValues.LogLevel);
        this.biomeConfigExtension = reader.getSetting(BiomeStandardValues.BIOME_CONFIG_EXTENSION);
    }

    @Override
    protected void writeConfigSettings(SettingsMap writer)
    {
        // The modes
        writer.bigTitle("The TerrainControl Plugin Config File ");

        writer.putSetting(WorldStandardValues.SETTINGS_MODE, this.SettingsMode,
                "How this config file will be treated.",
                "Possible Write Modes:",
                "   WriteAll             - Write config files with help comments",
                "   WriteWithoutComments - Write config files without help comments",
                "   WriteDisable         - Doesn't write to the config files, it only reads.",
                "                          Doesn't auto-update the configs. Use with care!",
                "Defaults to: WriteAll");

        // Custom biomes
        writer.bigTitle("Log Levels");

        writer.putSetting(PluginStandardValues.LogLevel, this.LogLevel,
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
    }

    public LogLevels getLogLevel()
    {
        return LogLevel;
    }

}
