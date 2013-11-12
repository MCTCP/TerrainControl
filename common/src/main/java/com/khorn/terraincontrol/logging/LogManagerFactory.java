package com.khorn.terraincontrol.logging;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.PluginConfig;
import com.khorn.terraincontrol.configuration.PluginConfig.LogLevels;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;

import java.util.logging.*;

/**
 * `Static` Class that provides a centralized way of obtaining a properly
 * configured LogManager class based on a provided PluginConfig
 */
public class LogManagerFactory
{

    /**
     * Holds the name of the Plugin to be prefixed to all logs records
     */
    public static final String prefix = PluginStandardValues.ChannelName.stringValue();
    /**
     * The default log level of the Plugin
     */
    public static final LogLevels defaultLogLevel = PluginConfig.LogLevels.Standard;

    private LogManagerFactory()
    { //>>	Shouldnt be instantiated
    }

    public static LogManager makeLogManager(Logger logger)
    {
        return setLogLevels(logger, TerrainControl.getPluginConfig().getConsoleHandlerLevel(), TerrainControl.getPluginConfig().getFileHandlerLevel());
    }

    public static LogManager setLogLevels(Logger logger, LogLevels consoleLogLevel, LogLevels fileLogLevel)
    {

        /* The logger is ultimately limited to the level of its handlers,
         * so if the user specifies a log level, set it to that, otherwise
         * determine the larger of the two alternate Log Levels and set the
         * log level to that.
         * //t>>
         * It looks like alot of what I was trying to do here might be possible with
         * the new Log4j 2 logging used by MC 1.7+, still waiting for MCP so Bukkit and Forge and update :\
         * //t>>
         */
        logger.setLevel((consoleLogLevel.getLevel().intValue() <= fileLogLevel.getLevel().intValue())
                        ? consoleLogLevel.getLevel() : fileLogLevel.getLevel());
        consoleLogLevel = clampLevel(consoleLogLevel);
        fileLogLevel = clampLevel(fileLogLevel);

        String handlerName, handlerParentName;
        for (Handler h : logger.getParent().getHandlers())
        {
            handlerName = h.getClass().getSimpleName();
            if (handlerName != null)
            {
                handlerParentName = h.getClass().getSuperclass().getSimpleName();
                if (handlerName.contains(ConsoleHandler.class.getSimpleName()) || handlerParentName.contains(ConsoleHandler.class.getSimpleName()))
                {
                    h.setLevel(consoleLogLevel.getLevel());
                    logHandlerLevelSet(logger, h, consoleLogLevel);
                } else if (handlerName.equals(FileHandler.class.getSimpleName()))
                {
                    h.setLevel(fileLogLevel.getLevel());
                    logHandlerLevelSet(logger, h, fileLogLevel);
                }
            }
        }
        return new LogManager(logger);
    }

    private static void logHandlerLevelSet(Logger logger, Handler h, LogLevels level)
    {
        logger.log(Level.FINE, "   [{0}] Handler::{1} Level Set to {2}", new Object[]
        {
            PluginStandardValues.ChannelName.stringValue(),
            h.getClass().getSimpleName(),
            level.getLevel().getLocalizedName()
        });
    }

    //t>>	Potential future site of addFileHandler(); to be configured by plugin config for plugin-specific Log file and level
    public static LogLevels clampLevel(LogLevels level)
    {
        if (level != PluginConfig.LogLevels.Off && level.getLevel().intValue() > defaultLogLevel.getLevel().intValue())
        {
            return defaultLogLevel;
        }
        return level;
    }
    
}