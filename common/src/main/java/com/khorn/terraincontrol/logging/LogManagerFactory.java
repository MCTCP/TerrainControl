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

    public static final String prefix = PluginStandardValues.ChannelName.stringValue();
    public static final LogLevels defaultLogLevel = PluginConfig.LogLevels.Standard;
    public static Formatter formatter;

    private LogManagerFactory()
    { //>>	Shouldnt be instantiated
    }

    static
    {
        formatter = new Formatter()
        {
            @Override
            public String format(LogRecord record)
            {
                //>>	It would be cool to align all TC logs in the future
                //>>	And switch the formatting up a bit...
                //>>	Ex: [WARNING] [TerrainControl] This is a Log
                //>>	    [SEVERE] [TerrainControl] This is another Log
                //>>	    [INFO] [TerrainControl] More Logs!
                //>>	    [CONFIG] [TerrainControl] This is not very readable
                //>>	doesnt look as nice as:
                //>>	Ex: [TerrainControl] [WARNING] This is a Log
                //>>	    [TerrainControl]  [SEVERE] This is another Log
                //>>	    [TerrainControl]    [INFO] More Logs!
                //>>	    [TerrainControl]  [CONFIG] This is so much more readable
                
                //>>	Personally, I dont see why all logs arent written like that...
                return new StringBuilder()
                    .append('[').append(prefix).append(']').append(' ')
                    .append(formatMessage(record)).toString();
            }
        };
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