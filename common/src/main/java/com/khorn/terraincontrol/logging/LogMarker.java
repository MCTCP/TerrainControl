package com.khorn.terraincontrol.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds the log markers and allows to compare them to each other.
 */
public class LogMarker
{

    /**
     * Markers for specific Log levels, we use these as internal Log Levels, but
     * can be used to filter logs in a very specific way
     */
    public static final Marker FATAL = MarkerManager.getMarker("com.khorn.terraincontrol.FATAL");
    public static final Marker ERROR = MarkerManager.getMarker("com.khorn.terraincontrol.ERROR", LogMarker.FATAL);
    public static final Marker WARN = MarkerManager.getMarker("com.khorn.terraincontrol.WARN", LogMarker.ERROR);
    public static final Marker INFO = MarkerManager.getMarker("com.khorn.terraincontrol.INFO", LogMarker.WARN);
    public static final Marker DEBUG = MarkerManager.getMarker("com.khorn.terraincontrol.DEBUG", LogMarker.INFO);
    public static final Marker TRACE = MarkerManager.getMarker("com.khorn.terraincontrol.TRACE", LogMarker.DEBUG);

    private static Map<Marker, Integer> standardLevels = new LinkedHashMap<Marker, Integer>(6);

    private LogMarker()
    {
    }

    static
    {
        standardLevels.put(FATAL, Level.FATAL.intLevel());
        standardLevels.put(ERROR, Level.ERROR.intLevel());
        standardLevels.put(WARN, Level.WARN.intLevel());
        standardLevels.put(INFO, Level.INFO.intLevel());
        standardLevels.put(DEBUG, Level.DEBUG.intLevel());
        standardLevels.put(TRACE, Level.TRACE.intLevel());
    }

    public static int compare(Marker first, Marker second)
    {
        Integer firstInt = standardLevels.get(first);
        if (firstInt != null)
        {
            return standardLevels.get(first).compareTo(standardLevels.get(second));
        } else
        {
            return 0;
        }
    }

}
