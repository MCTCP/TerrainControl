package com.khorn.terraincontrol.logging;

import com.khorn.terraincontrol.configuration.PluginConfig;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.util.helpers.StringHelper;
import org.apache.logging.log4j.Marker;

import java.util.List;

public class Logger
{

    private org.apache.logging.log4j.Logger baseLogger;
    public static final String PLUGIN_NAME = PluginStandardValues.ChannelName;
    private Marker logLevel;

    protected Logger(org.apache.logging.log4j.Logger logger)
    {
        logLevel = PluginConfig.LogLevels.Standard.getLevel();
        baseLogger = logger;
    }

    public org.apache.logging.log4j.Logger getBaseLogger()
    {
        return baseLogger;
    }

    public void setLevel(Marker level)
    {
        logLevel = level;
    }

    /**
     * Logs the message(s) with the given importance. Message will be
     * prefixed with [TerrainControl], so don't do that yourself.
     * <p/>
     * @param level   The severity of the message
     * @param message The messages to log
     */
    public void log(Marker level, List<String> message)
    {
        log(level, "{}", (Object) StringHelper.join(message, " "));
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
    public void log(Marker level, String message, Object... params)
    {
        String levelName = level.getName();
        StringBuilder sb = new StringBuilder(50).append('[').append(PLUGIN_NAME).append("] [")
            .append(levelName.substring(levelName.lastIndexOf('.')+1)) //>>	only get the basic name not the FQN
            .append("] ").append(message);
        if (LogMarker.compare(logLevel, level) >= 0)
        {//>>	Only log messages that we want to see...
            //>>	Log Fatal, Error, and Warn as what they are without Markers.
            if (LogMarker.compare(LogMarker.FATAL, level) == 0)
            {
                baseLogger.fatal(sb.toString(), params);
            } else if (LogMarker.compare(LogMarker.ERROR, level) == 0)
            {
                baseLogger.error(sb.toString(), params);
            } else if (LogMarker.compare(LogMarker.WARN, level) == 0)
            {
                baseLogger.warn(sb.toString(), params);
            } else
            {//>>	Otherwise log the message as info and tag it with a marker
                baseLogger.info(level, sb.toString(), params);
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
    public void logIfLevel(Marker ifLevel, List<String> messages)
    {
        if (LogMarker.compare(logLevel, ifLevel) == 0)
            log(ifLevel, messages);
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
    public void logIfLevel(Marker ifLevel, String message, Object... params)
    {
        if (LogMarker.compare(logLevel, ifLevel) == 0)
            log(ifLevel, message, params);
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
    public void logIfLevel(Marker min, Marker max, List<String> messages)
    {
        if (LogMarker.compare(logLevel, max) <= 0 && LogMarker.compare(logLevel, min) >= 0)
            log(max, messages);
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
    public void logIfLevel(Marker min, Marker max, String message, Object... params)
    {
        if (LogMarker.compare(logLevel, max) <= 0 && LogMarker.compare(logLevel, min) >= 0)
            log(max, message, params);
    }

}
