package com.khorn.terraincontrol.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class TCLogManager
{

    private static final String PREFIX = "TerrainControl";
    /**
     * Formats all log records being published
     */
    public static final Formatter FORMATTER = new Formatter()
    {
        @Override
        public String format(LogRecord record)
        {
            return new StringBuilder().append('[').append(PREFIX).append(']').append(' ').append(formatMessage(record)).toString();
        }
    };

}
