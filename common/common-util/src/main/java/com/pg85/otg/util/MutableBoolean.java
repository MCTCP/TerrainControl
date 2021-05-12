package com.pg85.otg.util;

public final class MutableBoolean
{
    private boolean value;

    public MutableBoolean(boolean value) {
        this.value = value;
    }

    public boolean isValue()
    {
        return value;
    }

    public void setValue(boolean value)
    {
        this.value = value;
    }
}
