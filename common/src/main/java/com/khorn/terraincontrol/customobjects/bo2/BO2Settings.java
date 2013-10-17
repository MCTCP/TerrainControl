package com.khorn.terraincontrol.customobjects.bo2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.khorn.terraincontrol.configuration.TCSetting;

public enum BO2Settings implements TCSetting
{

    BO_ALL_KEY("All"),
    BO_SolidKey("Solid"),
    
    // Custom object settings
    version("2.0"),
    spawnOnBlockType("2", SettingsType.StringArray),
    spawnSunlight(true),
    spawnDarkness(true),
    spawnWater(false),
    spawnLava(false),
    spawnAboveGround(false),
    spawnUnderGround(false),
    
    underFill(true),
    randomRotation(true),
    dig(false),
    tree(false),
    branch(false),
    diggingBranch(false),
    needsFoundation(true),
    rarity(100),
    collisionPercentage(2D),
    collisionBlockType("All", SettingsType.StringArray),
    spawnElevationMin(0),
    spawnElevationMax(128),
    
    groupFrequencyMin(1),
    groupFrequencyMax(5),
    groupSeperationMin(0), // Seperation - lol.
    groupSeperationMax(5),
    
    branchLimit(6),
    groupId("", SettingsType.StringArray),
    spawnInBiome("All", SettingsType.StringArray);
    
    private Object value;
    private final SettingsType returnType;

    private BO2Settings(int i)
    {
        value = i;
        returnType = SettingsType.Int;
    }

    private BO2Settings(HashSet<Integer> set, SettingsType type)
    {
        value = set;
        returnType = SettingsType.IntSet;
    }

    private BO2Settings(long i)
    {
        value = i;
        returnType = SettingsType.Long;
    }

    private BO2Settings(double d)
    {
        value = d;
        returnType = SettingsType.Double;
    }

    private BO2Settings(float f)
    {
        value = f;
        returnType = SettingsType.Float;
    }

    private BO2Settings(String s)
    {
        value = s;
        returnType = SettingsType.String;
    }

    private BO2Settings(String s, SettingsType type)
    {
        returnType = type;

        if (type == SettingsType.StringArray)
        {
            ArrayList<String> list = new ArrayList<String>();
            if (s.contains(","))
                Collections.addAll(list, s.split(","));
            else if (!s.equals(""))
                list.add(s);
            value = list;
            return;
        }
        value = s;
    }

    private BO2Settings(Boolean b)
    {
        value = b;
        returnType = SettingsType.Boolean;
    }
    private static Map<String, BO2Settings> lookupName;

    static
    {
        lookupName = new HashMap<String, BO2Settings>();

        for (BO2Settings value : BO2Settings.values())
        {
            lookupName.put(value.name().toLowerCase(), value);
        }
    }

    public static boolean Contains(String name)
    {
        return lookupName.containsKey(name);
    }

    @Override
    public long longValue()
    {
        return (Long) value;
    }

    @Override
    public float floatValue()
    {
        return (Float) value;
    }

    @Override
    public double doubleValue()
    {
        return (Double) value;
    }
    
    @Override
    public Enum<?> enumValue()
    {
        throw new UnsupportedOperationException("Enums are not used in BO2s");
    }

    @Override
    public SettingsType getReturnType()
    {
        return returnType;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<String> stringArrayListValue()
    {
        return (ArrayList<String>) value;
    }
    
     @Override
    public HashSet<Integer> intSetValue()
    {
        throw new UnsupportedOperationException("Int sets are not used in BO2s");
    }   
     
    @Override
    public int intValue()
    {
        return (Integer) value;
    }

    @Override
    public String stringValue()
    {
        return (String) value;
    }

    @Override
    public boolean booleanValue()
    {
        return (Boolean) value;
    }
}