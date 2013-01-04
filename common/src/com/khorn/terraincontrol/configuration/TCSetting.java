package com.khorn.terraincontrol.configuration;

import java.util.ArrayList;

public interface TCSetting
{
    public enum SettingsType
    {
        String,
        Boolean,
        Int,
        Enum,
        Double,
        Float,
        StringArray,
        Color
    }

    public String name();

    public int intValue();

    public float floatValue();

    public double doubleValue();

    public Enum<?> enumValue();

    public SettingsType getReturnType();

    public String stringValue();

    public ArrayList<String> stringArrayListValue();

    public boolean booleanValue();
}
