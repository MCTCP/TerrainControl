package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.logging.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Implementation of {@link Logger} for Bukkit.
 */
final class BukkitLogger extends Logger
{
    private final String logPrefix = "[" + PluginStandardValues.PLUGIN_NAME + "] ";
    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(PluginStandardValues.PLUGIN_NAME);

    BukkitLogger(java.util.logging.Logger logger)
    {

    }

    @Override
    public void log(LogMarker level, String message, Object... params)
    {
        if (minimumLevel.compareTo(level) < 0)
        {
            // Only log messages that we want to see...
            return;
        }
        switch (level)
        {
            case FATAL:
                logger.fatal(logPrefix + message, params);
                break;
            case ERROR:
                logger.error(logPrefix + message, params);
                break;
            case WARN:
                logger.warn(logPrefix + message, params);
                break;
            case INFO:
                logger.info(logPrefix + message, params);
                break;
            case DEBUG:
                logger.info(logPrefix + "[Debug] " + message, params);
                break;
            case TRACE:
                logger.trace(logPrefix + "[Trace] " + message, params);
                break;
            default:
                // Unknown log level, should never happen
                logger.info(logPrefix + message, params); // Still log the message
                throw new RuntimeException("Unknown log marker: " + level);
        }
    }
}
