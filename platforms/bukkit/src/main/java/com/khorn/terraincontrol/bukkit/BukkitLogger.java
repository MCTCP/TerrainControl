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
        if (this.minimumLevel.compareTo(level) < 0)
        {
            // Only log messages that we want to see...
            return;
        }
        switch (level)
        {
            case FATAL:
                this.logger.fatal(this.logPrefix + message, params);
                break;
            case ERROR:
                this.logger.error(this.logPrefix + message, params);
                break;
            case WARN:
                this.logger.warn(this.logPrefix + message, params);
                break;
            case INFO:
                this.logger.info(this.logPrefix + message, params);
                break;
            case DEBUG:
                this.logger.info(this.logPrefix + "[Debug] " + message, params);
                break;
            case TRACE:
                this.logger.trace(this.logPrefix + "[Trace] " + message, params);
                break;
            default:
                // Unknown log level, should never happen
                this.logger.info(this.logPrefix + message, params); // Still log the message
                throw new RuntimeException("Unknown log marker: " + level);
        }
    }
}
