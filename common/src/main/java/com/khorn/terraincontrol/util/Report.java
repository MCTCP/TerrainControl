package com.khorn.terraincontrol.util;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Used to add more information to an exception. Example usage:
 *
 * <pre>{@code throw Report.of(e).at(world, x, y, z).with("bo3", bo3.getName())}</pre>
 *
 */
public class Report extends RuntimeException
{

    private static final long serialVersionUID = -3636338164043782942L;

    /**
     * Creates a an exception that wraps the given throwable, such that details can be added.
     * @param t The exception to wrap.
     * @return The wrapped exception.
     */
    public static final Report of(Throwable t)
    {
        if (t instanceof Report)
        {
            // Do not make reports of reports
            return (Report) t;
        }
        if (t instanceof OutOfMemoryError)
        {
            // No more instances can be allocated in this situation, so don't
            // even try
            throw (OutOfMemoryError) t;
        }
        if (t == null)
        {
            // This should never, ever happen. However, we still accept this
            // value here to not miss any important context information that
            // might get added later on.
            t = new NullPointerException("No cause specified - this is a bug in the error reporting system");
        }
        return new Report(t);
    }

    private Map<String, String> details = new HashMap<>();

    private Report(Throwable t)
    {
        super(Objects.requireNonNull(t));
        this.setStackTrace(new StackTraceElement[0]);
    }

    @Override
    public String getMessage()
    {
        StringBuilder message = new StringBuilder("An internal error in " + PluginStandardValues.PLUGIN_NAME + " occured.");
        for (Entry<String, String> detail : details.entrySet())
        {
            message.append('\n').append(detail.getKey()).append(": ").append(detail.getValue());
        }
        return message.toString();
    }

    /**
     * Adds some details to the exception, for example for which BO3 the error occured.
     * @param name Name of the detail, like "bo3".
     * @param value Value of the detail, like the BO3 name.
     * @return This, for chaining.
     */
    public Report with(String name, String value)
    {
        this.details.put(name, value);
        return this;
    }

    /**
     * Adds location details to the exception.
     * @param world The world, should not be null. (But null values are still
     * accepted - they might give an important clue on what is going on.)
     * @param name Location description, like "bo3".
     * @param x Block x.
     * @param y Block y.
     * @param z Block z.
     * @return This, for chaining.
     */
    public Report at(String name, LocalWorld world, int x, int y, int z)
    {
        String worldName = world == null ? "unknown (NULL) world" : world.getName();
        return with("location of " + name, "(" + worldName + ") " + x + "," + y + "," + z);
    }
}
