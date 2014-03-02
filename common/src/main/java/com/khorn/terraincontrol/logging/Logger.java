package com.khorn.terraincontrol.logging;

import com.khorn.terraincontrol.configuration.PluginConfig;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.util.helpers.StringHelper;
import org.apache.logging.log4j.Marker;

public class Logger
{

    private static org.apache.logging.log4j.Logger baseLogger;
    private static final java.util.Formatter FORMATTER = new java.util.Formatter();
    public static final String PLUGIN_NAME = PluginStandardValues.ChannelName.stringValue();
    private static Marker logLevel;

    protected Logger(org.apache.logging.log4j.Logger logger)
    {
        Logger.logLevel = PluginConfig.LogLevels.Standard.getLevel();
        Logger.baseLogger = logger;
    }

    public static org.apache.logging.log4j.Logger getBaseLogger()
    {
        return Logger.baseLogger;
    }

    public static void setLevel(Marker level)
    {
        Logger.logLevel = level;
    }

    /**
     * Logs the message(s) with the given importance. Message will be
     * prefixed with [TerrainControl], so don't do that yourself.
     * <p/>
     * @param level   The severity of the message
     * @param message The messages to log
     */
    public static void log(Marker level, String... message)
    {
        Logger.log(level, "{}", (Object) StringHelper.join(message, " "));
    }

    /**
     * Logs a format string message with the given importance. Message will
     * be prefixed with [TerrainControl], so don't do that yourself.
     * <p/>
     * @param level   The severity of the message
     * @param message The messages to log formatted similar to Logger.log()
     *                with the same args.
     * @param params  The parameters belonging to {0...} in the message
     *                string
     */
    public static void log(Marker level, String message, Object... params)
    {
        String levelName = level.getName();
        StringBuilder sb = new StringBuilder(50).append('[').append(PLUGIN_NAME).append("] [")
            .append(levelName.substring(levelName.lastIndexOf('.')+1)) //>>	only get the basic name not the FQN
            .append("] ").append(message);
        if (LogMarker.compare(Logger.logLevel, level) >= 0)
        {//>>	Only log messages that we want to see...
            //>>	Log Fatal, Error, and Warn as what they are without Markers.
            if (LogMarker.compare(LogMarker.FATAL, level) == 0)
            {
                Logger.baseLogger.fatal(sb.toString(), params);
            } else if (LogMarker.compare(LogMarker.ERROR, level) == 0)
            {
                Logger.baseLogger.error(sb.toString(), params);
            } else if (LogMarker.compare(LogMarker.WARN, level) == 0)
            {
                Logger.baseLogger.warn(sb.toString(), params);
            } else
            {//>>	Otherwise log the message as info and tag it with a marker
                Logger.baseLogger.info(level, sb.toString(), params);
            }
        }
    }

    /**
     * Logs the message(s) with the given importance <b>ONLY IF</b> logger
     * level matches the level provided. Message will be prefixed with
     * [TerrainControl], so don't do that yourself.
     * <p/>
     * @param ifLevel  the Log level to test for
     * @param messages The messages to log.
     */
    public static void logIfLevel(Marker ifLevel, String... messages)
    {
        if (LogMarker.compare(Logger.logLevel, ifLevel) == 0)
            Logger.log(ifLevel, messages);
    }

    /**
     * Logs the message(s) with the given importance <b>ONLY IF</b> logger
     * level matches the level provided. Message will be prefixed with
     * [TerrainControl], so don't do that yourself.
     * <p/>
     * @param ifLevel the Log level to test for
     * @param message The messages to log formatted similar to
     *                Logger.log() with the same args.
     * @param params  The parameters belonging to {0...} in the message
     *                string
     */
    public static void logIfLevel(Marker ifLevel, String message, Object... params)
    {
        if (LogMarker.compare(Logger.logLevel, ifLevel) == 0)
            Logger.log(ifLevel, message, params);
    }

    /**
     * Logs the message(s) with the given importance <b>ONLY IF</b> logger
     * level is between the min/max provided. Message will be prefixed with
     * [TerrainControl], so don't do that yourself.
     * <p/>
     * @param min      The minimum Log level to test for
     * @param max      The maximum Log level to test for
     * @param messages The messages to log.
     */
    public static void logIfLevel(Marker min, Marker max, String... messages)
    {
        if (LogMarker.compare(Logger.logLevel, max) <= 0 && LogMarker.compare(Logger.logLevel, min) >= 0)
            Logger.log(max, messages);
    }

    /**
     * Logs the message(s) with the given importance <b>ONLY IF</b> logger
     * level is between the min/max provided. Message will be prefixed with
     * [TerrainControl], so don't do that yourself.
     * <p/>
     * @param min     The minimum Log level to test for
     * @param max     The maximum Log level to test for
     * @param message The messages to log formatted similar to
     *                Logger.log() with the same args.
     * @param params  The parameters belonging to {0...} in the message
     *                string
     */
    public static void logIfLevel(Marker min, Marker max, String message, Object... params)
    {
        if (LogMarker.compare(Logger.logLevel, max) <= 0 && LogMarker.compare(Logger.logLevel, min) >= 0)
            Logger.log(max, message, params);
    }

}
