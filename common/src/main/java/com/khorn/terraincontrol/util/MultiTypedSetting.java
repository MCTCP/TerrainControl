package com.khorn.terraincontrol.util;

import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

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
        Color,
        Material
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

    public DefaultMaterial materialValue();
}
