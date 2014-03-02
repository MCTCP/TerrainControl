package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.WorldConfig.ConfigMode;
import com.khorn.terraincontrol.configuration.standard.BiomeStandardValues;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.logging.LogMarker;

import java.io.File;
import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;

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

    public PluginConfig(File settingsDir)
    {

        super(PluginStandardValues.ChannelName.stringValue(), new File(settingsDir, PluginStandardValues.ConfigFilename.stringValue()));
        if (!settingsDir.exists())
            settingsDir.mkdirs();
        init();
    }

    private void init()
    {
        if (this.file.exists())
        {
            this.readSettingsFile();
        }
        this.renameOldSettings();
        this.readConfigSettings();

        this.correctSettings();

        if (this.SettingsMode != ConfigMode.WriteDisable)
            this.writeSettingsFile(this.SettingsMode == ConfigMode.WriteAll);
    }

    @Override
    protected void renameOldSettings()
    {
        // Nothing to rename at the moment
    }

    @Override
    protected void correctSettings()
    {
        if (!BiomeStandardValues.BiomeConfigExtensions.stringArrayListValue().contains(this.biomeConfigExtension))
        {
            String newExtension = BiomeStandardValues.DefaultBiomeConfigExtension.stringValue();
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
        this.SettingsMode = readSettings(WorldStandardValues.SettingsMode);
        this.LogLevel = readSettings(PluginStandardValues.LogLevel);
        this.biomeConfigExtension = readSettings(BiomeStandardValues.DefaultBiomeConfigExtension);
    }

    @Override
    protected void writeConfigSettings() throws IOException
    {
        // The modes
        writeBigTitle("The TerrainControl Plugin Config File ");

        writeComment("How this config file will be treated.");
        writeComment("Possible Write Modes:");
        writeComment("   WriteAll             - Write config files with help comments");
        writeComment("   WriteWithoutComments - Write config files without help comments");
        writeComment("   WriteDisable         - Doesn't write to the config files, it only reads.");
        writeComment("                          Doesn't auto-update the configs. Use with care!");
        writeComment("Defaults to: WriteAll");
        writeValue(WorldStandardValues.SettingsMode, this.SettingsMode.name());

        // Custom biomes
        writeBigTitle("Log Levels");

        writeSmallTitle("Possible Log Levels");
        // writeComment("   Off         - Only warnings and errors are displayed.");
        // // Shows warning when using this
        writeComment("   Off         - Bare logging; This will only show FATAL and ERROR logs");
        writeComment("   Quiet       - Minimal logging; This will show FATAL, ERROR, and WARN logs");
        writeComment("   Standard    - Default logging; This is exactly what you are used to. Quiet + INFO logs");
        writeComment("   Debug       - Above Normal logging; Standard logs + DEBUG logs");
        writeComment("   Trace       - Verbose logging; This gets very messy, Debug logs + TRACE logs");
        writeComment("");

        writeSmallTitle("Logging Level");
        writeComment("This is the level with which logs will be produced.");
        writeComment("See ``Possible Log Levels'' if you are lost.");
        writeComment(" ");
        writeComment("Defaults to: Standard");
        writeValue(PluginStandardValues.LogLevel, this.LogLevel.name());

        writeBigTitle("File Extension Rules");

        writeSmallTitle("Default Biome File Extension");
        writeComment("Pre-TC 2.5.0, biome config files were in the form BiomeNameBiomeConfig.ini");
        writeComment("Now, biome config files are in the form BiomeName.bc.ini");
        writeComment("You may change this by choosing between the following extensions:");
        writeComment("BiomeConfig.ini, .biome, .bc, .bc.ini, and .biome.ini");
        writeComment(" ");
        writeComment("Defaults to: .bc");
        writeValue(BiomeStandardValues.DefaultBiomeConfigExtension, this.biomeConfigExtension);
    }

    public LogLevels getLogLevel()
    {
        return LogLevel;
    }

}
