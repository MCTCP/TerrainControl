package com.khorn.terraincontrol.util;

import java.util.ArrayList;
import java.util.HashSet;

public interface MultiTypedSetting
{
    public enum SettingsType
    {
        String,
        Boolean,
        Int,
        IntSet,
        Long,
        Enum,
        Double,
        Float,
        StringArray,
        Color
    }

    public String name();

    public int intValue();
    
    public long longValue();

    public float floatValue();

    public double doubleValue();

    public Enum<?> enumValue();

    public SettingsType getReturnType();

    public String stringValue();

    public ArrayList<String> stringArrayListValue();
    
    public HashSet<Integer> intSetValue();

    public boolean booleanValue();
}
