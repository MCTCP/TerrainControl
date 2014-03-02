package com.khorn.terraincontrol.logging;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * @author Timethor
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

    public static Map<Marker, Integer> StandardLevels = new LinkedHashMap<Marker, Integer>(6);

    private LogMarker()
    {
    }

    static
    {
        StandardLevels.put(FATAL, Level.FATAL.intLevel());
        StandardLevels.put(ERROR, Level.ERROR.intLevel());
        StandardLevels.put(WARN, Level.WARN.intLevel());
        StandardLevels.put(INFO, Level.INFO.intLevel());
        StandardLevels.put(DEBUG, Level.DEBUG.intLevel());
        StandardLevels.put(TRACE, Level.TRACE.intLevel());
    }

    public static int compare(Marker first, Marker second)
    {
        Integer firstInt = StandardLevels.get(first);
        if (firstInt != null)
        {
            return StandardLevels.get(first).compareTo(StandardLevels.get(second));
        } else
        {
            return 0;
        }
    }

}
