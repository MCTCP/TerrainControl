package com.khorn.terraincontrol.logging;

import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

public class LogFactory
{

    /**
     * This is the name of our Logger, currently pulling from
     * PluginStandardValues for consistency
     */
    public static final String LOGGER_NAME = LogFactory.class.getName();
    

    private static com.khorn.terraincontrol.logging.Logger LOGGER;
    /**
     * Terrain Control Log4j2 Appender name
     */
    private static final String TC_LOG_APPENDER = "TerrainControlFile";

    private LogFactory()
    { //>>	Shouldnt be instantiated
    }

    /**
     *
     * @return a memoized TerrainControl Logger
     */
    public static com.khorn.terraincontrol.logging.Logger getLogger()
    {
        if (LOGGER == null)
        {
            return new com.khorn.terraincontrol.logging.Logger(LogManager.getLogger(LOGGER_NAME));
        } else
        {
            return LOGGER;
        }
    }

    /**
     *
     * @param logger The desired baseLogger of the returned Logger class
     * @return a memoized TerrainControl Logger. If the logger passed in differs
     *         in class from the memoized Logger's baseLogger, a new
     *         TerrainControl logger will be created and returned
     */
    public static com.khorn.terraincontrol.logging.Logger getLogger(Logger logger)
    {
        if (LOGGER != null && LOGGER.getBaseLogger().getClass().getName().equals(logger.getClass().getName()))
        {
            return LOGGER;
        } else
        {
            return new com.khorn.terraincontrol.logging.Logger(logger);
        }
    }

    private static boolean isIndependent(Logger logger)
    {

        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration conf = ctx.getConfiguration();
        LoggerConfig config = conf.getLoggerConfig(logger.getName());
        for (Map.Entry<String, Appender> entry : config.getAppenders().entrySet())
        {
            if (entry.getKey().equalsIgnoreCase(TC_LOG_APPENDER))
            {
                LOGGER.log(LogMarker.INFO, "APPENDER FOUND");
            }
        }
        return false;
    }

}
