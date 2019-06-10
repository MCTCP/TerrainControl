package com.pg85.otg.configuration.customobjects;

import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.helpers.StringHelper;

import java.util.List;

public final class CustomObjectErroredFunction<T> extends CustomObjectConfigFunction<T>
{
    private final Class<T> holder;
    private final String name;
    private final List<String> args;

    CustomObjectErroredFunction(String name, T holder, List<String> args, String error)
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
    public boolean isAnalogousTo(CustomObjectConfigFunction<T> other)
    {
        // This function will never do anything, so won't matter how it is
        // inherited
        return this == other;
    }
}