package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.WorldConfig.ConfigMode;
import com.khorn.terraincontrol.configuration.io.SettingsReader;
import com.khorn.terraincontrol.configuration.io.SettingsWriter;
import com.khorn.terraincontrol.configuration.standard.BiomeStandardValues;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.logging.LogMarker;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;

import java.io.IOException;

/**
 * Temporarily pre-configured Class that will eventually represent the file
 * containing any and all plugin-specific settings
 */
public final class PluginConfig extends ConfigFile
{

    public ConfigMode SettingsMode;

    public static enum LogLevels
    {

        Off(LogMarker.ERROR, Level.ERROR.intLevel()),
        Quiet(LogMarker.WARN, Level.WARN.intLevel()),
        Standard(LogMarker.INFO, Level.INFO.intLevel()),
        Debug(LogMarker.DEBUG, Level.DEBUG.intLevel()),
        Trace(LogMarker.TRACE, Level.TRACE.intLevel());
        private final Marker marker;
        private final int value;

        private LogLevels(Marker marker, int value)
        {
            this.marker = marker;
            this.value = value;
        }

        public Marker getLevel()
        {
            return marker;
        }

        public int getValue()
        {
            return value;
        }

    }

    public LogLevels LogLevel = LogLevels.Standard;
    public String biomeConfigExtension;

    public PluginConfig(SettingsReader settingsReader)
    {

        super(settingsReader);

        this.renameOldSettings();
        this.readConfigSettings();

        this.correctSettings();
    }

    @Override
    protected void renameOldSettings()
    {
        // Nothing to rename at the moment
    }

    @Override
    protected void correctSettings()
    {
        if (!BiomeStandardValues.BiomeConfigExtensions.contains(this.biomeConfigExtension))
        {
            String newExtension = BiomeStandardValues.BIOME_CONFIG_EXTENSION.getDefaultValue();
            TerrainControl.log(LogMarker.WARN, "BiomeConfig file extension {} is invalid, changing to {}", new Object[]
            {
                this.biomeConfigExtension, newExtension
            });
            this.biomeConfigExtension = newExtension;
        }

    }

    @Override
    protected void readConfigSettings()
    {
        this.SettingsMode = readSettings(WorldStandardValues.SETTINGS_MODE);
        this.LogLevel = readSettings(PluginStandardValues.LogLevel);
        this.biomeConfigExtension = readSettings(BiomeStandardValues.BIOME_CONFIG_EXTENSION);
    }

    @Override
    protected void writeConfigSettings(SettingsWriter writer) throws IOException
    {
        // The modes
        writer.bigTitle("The TerrainControl Plugin Config File ");

        writer.comment("How this config file will be treated.");
        writer.comment("Possible Write Modes:");
        writer.comment("   WriteAll             - Write config files with help comments");
        writer.comment("   WriteWithoutComments - Write config files without help comments");
        writer.comment("   WriteDisable         - Doesn't write to the config files, it only reads.");
        writer.comment("                          Doesn't auto-update the configs. Use with care!");
        writer.comment("Defaults to: WriteAll");
        writer.setting(WorldStandardValues.SETTINGS_MODE, this.SettingsMode);

        // Custom biomes
        writer.bigTitle("Log Levels");

        writer.smallTitle("Possible Log Levels");
        // writeComment("   Off         - Only warnings and errors are displayed.");
        // // Shows warning when using this
        writer.comment("   Off         - Bare logging; This will only show FATAL and ERROR logs");
        writer.comment("   Quiet       - Minimal logging; This will show FATAL, ERROR, and WARN logs");
        writer.comment("   Standard    - Default logging; This is exactly what you are used to. Quiet + INFO logs");
        writer.comment("   Debug       - Above Normal logging; Standard logs + DEBUG logs");
        writer.comment("   Trace       - Verbose logging; This gets very messy, Debug logs + TRACE logs");
        writer.comment("");

        writer.smallTitle("Logging Level");
        writer.comment("This is the level with which logs will be produced.");
        writer.comment("See ``Possible Log Levels'' if you are lost.");
        writer.comment(" ");
        writer.comment("Defaults to: Standard");
        writer.setting(PluginStandardValues.LogLevel, this.LogLevel);

        writer.bigTitle("File Extension Rules");

        writer.smallTitle("Default Biome File Extension");
        writer.comment("Pre-TC 2.5.0, biome config files were in the form BiomeNameBiomeConfig.ini");
        writer.comment("Now, biome config files are in the form BiomeName.bc.ini");
        writer.comment("You may change this by choosing between the following extensions:");
        writer.comment("BiomeConfig.ini, .biome, .bc, .bc.ini, and .biome.ini");
        writer.comment(" ");
        writer.comment("Defaults to: .bc");
        writer.setting(BiomeStandardValues.BIOME_CONFIG_EXTENSION, this.biomeConfigExtension);
    }

    public LogLevels getLogLevel()
    {
        return LogLevel;
    }

}
