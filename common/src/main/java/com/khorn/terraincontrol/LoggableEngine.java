package com.khorn.terraincontrol;

import java.util.logging.Level;

/**
 * When implemented, the implementing class will have a suite of available
 * options for detailed, configurable logging
 */
public interface LoggableEngine
{

    /**
     * Logs the message(s) with the given importance. Message will be
     * prefixed with [TerrainControl], so don't do that yourself.
     * <p/>
     * @param level   The severity of the message
     * @param message The messages to log
     */
    public void log(Level level, String... message);

    /**
     * Logs a format string message with the given importance. Message will
     * be prefixed with [TerrainControl], so don't do that yourself.
     * <p/>
     * @param level   The severity of the message
     * @param message The messages to log formatted similar to Logger.log()
     *                with the same args.
     * @param param   The parameter belonging to {0} in the message string
     */
    public void log(Level level, String message, Object param);

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
    public void log(Level level, String message, Object[] params);

}