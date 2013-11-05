package com.khorn.terraincontrol.logging;

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

    /**
     * Logs the message(s) with the given importance <b>ONLY IF</b> logger
     * level matches the level provided. Message will be prefixed with
     * [TerrainControl], so don't do that yourself.
     * <p/>
     * @param ifLevel  the Log level to test for
     * @param messages The messages to log.
     */
    public void logIfLevel(Level ifLevel, String... messages);

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
    public void logIfLevel(Level ifLevel, String message, Object[] params);

    /**
     * Logs the message(s) with the given importance <b>ONLY IF</b> logger
     * level is between the min/max provided. Message will be prefixed with
     * [TerrainControl], so don't do that yourself.
     * <p/>
     * @param min      The minimum Log level to test for
     * @param max      The maximum Log level to test for
     * @param messages The messages to log.
     */
    public void logIfLevel(Level min, Level max, String... messages);

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
    public void logIfLevel(Level min, Level max, String message, Object[] params);

}