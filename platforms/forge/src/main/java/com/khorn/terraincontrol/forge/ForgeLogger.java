package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.logging.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * CImplementation of {@link Logger} for Forge.
 *
 * <p>Note that Forge (unlike Bukkit) automatically adds the TerrainControl
 * prefix, so we don't need to do that ourselves.</p>
 */
final class ForgeLogger extends Logger
{
    private org.apache.logging.log4j.Logger logger = LogManager.getLogger(PluginStandardValues.PLUGIN_NAME);

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
                logger.fatal(message, params);
                break;
            case ERROR:
                logger.error(message, params);
                break;
            case WARN:
                logger.warn(message, params);
                break;
            case INFO:
                logger.info(message, params);
                break;
            case DEBUG:
                logger.debug(message, params);
                break;
            case TRACE:
                logger.trace(message, params);
                break;
            default:
                // Unknown log level, should never happen
                logger.info(message, params); // Still log the message
                throw new RuntimeException("Unknown log marker: " + level);
        }
    }
}
