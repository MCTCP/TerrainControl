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
    public static Logger stdLogger;

    private TCLogManager()
    { //>>	Shouldnt be instantiated
    }

    static
    {
        formatter = new Formatter()
        {
            String spacer;

            @Override
            public String format(LogRecord record)
            {
                spacer = "";
                String levelName = record.getLevel().getName();
                if (levelName.equals(Level.ALL.getName()) || levelName.equals(Level.OFF.getName()))
                {
                    spacer += "    ";
                } else if (levelName.equals(Level.FINE.getName()) || levelName.equals(Level.INFO.getName()))
                {
                    spacer += "   ";
                } else if (levelName.equals(Level.FINER.getName()))
                {
                    spacer += "  ";
                } else if (levelName.equals(Level.CONFIG.getName()) || levelName.equals(Level.FINEST.getName()) || levelName.equals(Level.SEVERE.getName()))
                {
                    spacer += " ";
                }
                return new StringBuilder()
                    .append(spacer).append("[").append(prefix).append("] ")
                    .append(formatMessage(record)).toString();
            }
        };
    }

    public static Logger getLogger()
    {
        if (stdLogger == null)
        {
            return Logger.getLogger("Minecraft");
        } else
        {
            return stdLogger;
        }
    }

    public static Logger getLogger(Object context)
    {
        stdLogger = prepLogger(Logger.getLogger(context.getClass().getCanonicalName()));
        return stdLogger;
    }

    public static Logger prepLogger(Logger logger)
    {
        return setLogLevels(logger, TerrainControl.TCPluginConfig.getConsoleHandlerLevel(), TerrainControl.TCPluginConfig.getFileHandlerLevel());
    }

    public static Logger setLogLevels(Logger logger, LogLevels consoleLogLevel, LogLevels fileLogLevel)
    {

        /* The logger is ultimately limited to the level of its handlers,
         * so if the user specifies a log level, set it to that, otherwise
         * determine the larger of the two alternate Log Levels and set the
         * log level to that.
         */
        if (consoleLogLevel == LogLevels.Off || fileLogLevel == LogLevels.Off)
        {
            logger.setLevel(Level.WARNING);
        } else
        {
            logger.setLevel((consoleLogLevel.getLevel().intValue() <= fileLogLevel.getLevel().intValue())
                            ? consoleLogLevel.getLevel() : fileLogLevel.getLevel());
        }
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
        if (level.getLevel().intValue() > defaultLogLevel.getLevel().intValue())
        {
            TCLogManager.getLogger().log(Level.WARNING, " [{0}] FileLoglevel `{1}` is not a safe level, reverting to default.", new Object[]
            {
                TCDefaultValues.ChannelName.stringValue(), level.getLevel().getLocalizedName()
            });
            return defaultLogLevel;
        }
        return level;
    }

}