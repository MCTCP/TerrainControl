package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.util.helpers.StringHelper;

import java.util.List;

public final class ErroredFunction<T> extends ConfigFunction<T>
{
    private final String name;
    private final List<String> args;
    public final String error;

    ErroredFunction(String name, T holder, List<String> args, String error)
    {
        super(holder);
        this.name = name;
        this.args = args;
        this.error = error;
    }

    @Override
    public String toString()
    {
        return "## INVALID " + name.toUpperCase() + " - " + error + " ##" + System.getProperty(
                "line.separator") + name + "(" + StringHelper.join(args,
                        ",") + ")";
    }

    @Override
    public boolean isAnalogousTo(ConfigFunction<T> other)
    {
        // This function will never do anything, so won't matter how it is
        // inherited
        return this == other;
    }
}