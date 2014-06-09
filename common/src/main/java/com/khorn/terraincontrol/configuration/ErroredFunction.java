package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.helpers.StringHelper;

import java.util.List;

final class ErroredFunction<T> extends ConfigFunction<T>
{
    private final Class<T> holder;
    private final String name;
    private final List<String> args;

    ErroredFunction(String name, T holder, List<String> args, String error)
    {
        @SuppressWarnings("unchecked")
        Class<T> holderClass = (Class<T>) holder.getClass();
        this.holder = holderClass;
        this.name = name;
        this.args = args;
        this.invalidate(name, args, error);
    }

    @Override
    public Class<T> getHolderType()
    {
        return holder;
    }

    @Override
    protected void load(List<String> args) throws InvalidConfigException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String makeString()
    {
        return name + "(" + StringHelper.join(args, ",") + ")";
    }

    @Override
    public boolean isAnalogousTo(ConfigFunction<T> other)
    {
        // This function will never do anything, so won't matter how it is
        // inherited
        return this == other;
    }
}