package com.khorn.terraincontrol.logging;

/**
 * Holds the log markers and allows to compare them to each other.
 *
 * <p>Markers for specific Log levels, we use these as internal Log Levels, but can
 * be used to filter logs in a very specific way.</p>
 */
public enum LogMarker
{
    FATAL,
    ERROR,
    WARN,
    INFO,
    DEBUG,
    TRACE
}
