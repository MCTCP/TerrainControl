package com.khorn.terraincontrol.logging;

import org.apache.logging.log4j.LogManager;

public class LogFactory
{

    /**
     * This is the name of our Logger, currently pulling from
     * PluginStandardValues for consistency
     */
    public static final String LOGGER_NAME = LogFactory.class.getName();

    private static Logger LOGGER;

    private LogFactory()
    { //>>	Shouldnt be instantiated
    }

    /**
     *
     * @return a memoized TerrainControl Logger
     */
    public static Logger getLogger()
    {
        if (LOGGER == null)
        {
            return new Logger(LogManager.getLogger(LOGGER_NAME));
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
    public static Logger getLogger(org.apache.logging.log4j.Logger logger)
    {
        if (LOGGER != null && Logger.getBaseLogger().getClass().getName().equals(logger.getClass().getName()))
        {
            return LOGGER;
        } else
        {
            return new Logger(logger);
        }
    }

}
