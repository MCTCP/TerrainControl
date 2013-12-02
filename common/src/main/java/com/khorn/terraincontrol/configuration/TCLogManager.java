package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.PluginConfig.LogLevels;
import java.util.logging.*;

/**
 * `Static` Class that provides a centralized way of obtaining a properly
 * configured logger class based on the static TerrainControl PluginConfig
 */
public class TCLogManager
{

    public static final String prefix = TCDefaultValues.ChannelName.stringValue();
    public static Formatter formatter;
    public static final LogLevels defaultLogLevel = PluginConfig.LogLevels.Standard;

    private TCLogManager()
    { //>>	Shouldnt be instantiated
    }

    static
    {
        formatter = new Formatter()
        {
            @Override
            public String format(LogRecord record)
            {
                return new StringBuilder()
                    .append("[").append(prefix).append("] ")
                    .append(formatMessage(record)).toString();
            }
        };
    }

    public static Logger prepLogger(Logger logger)
    {
        return setLogLevels(logger, TerrainControl.getPluginConfig().getConsoleHandlerLevel(), TerrainControl.getPluginConfig().getFileHandlerLevel());
    }

    public static Logger setLogLevels(Logger logger, LogLevels consoleLogLevel, LogLevels fileLogLevel)
    {

        /* The logger is ultimately limited to the level of its handlers,
         * so if the user specifies a log level, set it to that, otherwise
         * determine the larger of the two alternate Log Levels and set the
         * log level to that.
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
        return logger;
    }

    private static void logHandlerLevelSet(Logger logger, Handler h, LogLevels level)
    {
        logger.log(Level.FINE, "   [{0}] Handler::{1} Level Set to {2}", new Object[]
        {
            TCDefaultValues.ChannelName.stringValue(),
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