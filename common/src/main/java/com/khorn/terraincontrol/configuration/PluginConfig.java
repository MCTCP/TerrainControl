package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.logging.LogManager;
import com.khorn.terraincontrol.configuration.WorldConfig.ConfigMode;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Temporarily pre-configured Class that will eventually represent the file
 * containing any and all plugin-specific settings
 */
public final class PluginConfig extends ConfigFile
{

    public ConfigMode SettingsMode;

    public static enum LogLevels
    {

        Off(Level.WARNING),
        Standard(Level.INFO),
        Debug(Level.CONFIG),
        XDebug(Level.FINE),
        XXDebug(Level.FINER),
        Trace(Level.FINEST);
        private Level level;

        private LogLevels(Level level)
        {
            this.level = level;
        }

        public Level getLevel()
        {
            return level;
        }

    }
    public LogLevels fileHandlerLevel;
    public LogLevels consoleHandlerLevel;
    public String worldDefaultBiomeConfigExtension;
    private static final Logger l = LogManager.getLogger();

    public PluginConfig(File settingsDir)
    {

        super(TCDefaultValues.ChannelName.stringValue(), new File(settingsDir, TCDefaultValues.pluginSettingsName.stringValue()));
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
        // Need add check to clashes
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
        boolean hasOffLevel = false;
        if (this.consoleHandlerLevel == PluginConfig.LogLevels.Off)
        {
            hasOffLevel = true;
        }
        if (this.fileHandlerLevel == PluginConfig.LogLevels.Off)
        {
            hasOffLevel = true;
        }
        if (hasOffLevel)
        {
            l.log(Level.WARNING, LogManager.formatter.format(new LogRecord(Level.WARNING, "Quiet Mode: You will no longer see INFO messages FOR ANY PLUGIN.")));
            l.log(Level.WARNING, LogManager.formatter.format(new LogRecord(Level.WARNING, "WARNING AND SEVERE level logs will still show.")));
        }
    }

    @Override
    protected void readConfigSettings()
    {
        this.SettingsMode = readSettings(TCDefaultValues.SettingsMode);
        this.consoleHandlerLevel = readSettings(TCDefaultValues.ConsoleLogLevel);
        this.fileHandlerLevel = readSettings(TCDefaultValues.FileLogLevel);
        this.worldDefaultBiomeConfigExtension = readSettings(TCDefaultValues.DefaultBiomeConfigExtension);
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
        writeValue(TCDefaultValues.SettingsMode, this.SettingsMode.name());

        // Custom biomes
        writeBigTitle("Log Levels");

        writeSmallTitle("Possible Log Levels");
        // writeComment("   Off         - Only warnings and errors are displayed."); // Shows warning when using this
        writeComment("   Standard    - Default Level, minimal logging; This is exactly what you are used to.");
        writeComment("   Debug       - Slightly more detail, this one is not too noisy.");
        writeComment("   XDebug      - Slightly even more detail, can be slightly noisy.");
        writeComment("   XXDebug     - Use with caution, some large logs are possible.");
        writeComment("   Trace       - Only use this in dire circumstances and only for short periods of time, huge logs incoming.");
        writeComment("");
        
        writeSmallTitle("Console Logging Level");
        writeComment("This is the level with which logs will be produced on the console. i.e. That black screen thing you see in windows.");
        writeComment("See ``Possible Log Levels'' if you are lost.");
        writeComment(" ");
        writeComment("Defaults to: Standard");
        writeValue(TCDefaultValues.ConsoleLogLevel, this.consoleHandlerLevel.name());

        writeSmallTitle("File Logging Level");
        writeComment("This is the level with which logs will be produced in the log file. i.e. server.log");
        writeComment("See ``Possible Log Levels'' if you are lost.");
        writeComment(" ");
        writeComment("Defaults to: Standard");
        writeValue(TCDefaultValues.FileLogLevel, this.fileHandlerLevel.name());


        writeBigTitle("File Extension Rules");

        writeSmallTitle("Default Biome File Extension");
        writeComment("Pre-TC 2.5.0, biome config files were in the form BiomeNameBiomeConfig.ini");
        writeComment("Now, biome config files are in the form BiomeName.bc.ini");
        writeComment("You may change this by choosing between the following extensions:");
        writeComment("BiomeConfig.ini, .biome, .bc, .bc.ini, and .biome.ini");
        writeComment(" ");
        writeComment("Defaults to: .bc");
        writeValue(TCDefaultValues.DefaultBiomeConfigExtension, this.worldDefaultBiomeConfigExtension);
    }

    public LogLevels getFileHandlerLevel()
    {
        return fileHandlerLevel;
    }

    public LogLevels getConsoleHandlerLevel()
    {
        return consoleHandlerLevel;
    }

}