package com.khorn.terraincontrol.customobjects.bo3;

import java.util.ArrayList;
import java.util.Collections;

import com.khorn.terraincontrol.configuration.TCSetting;

public enum BO3Settings implements TCSetting
{
    author("Unknown"),
    description("No description given"),
    tree(true),
    frequency(1),
    rarity(100.0),
    rotateRandomly(false),
    spawnHeight(SpawnHeight.highestBlock),
    excludedBiomes("All", SettingsType.StringArray);

    // The spawn height
    public enum SpawnHeight{randomY, highestBlock, highestSolidBlock}
    
    private Object value;
    private SettingsType returnType;

    private BO3Settings(int i)
    {
        value = i;
        returnType = SettingsType.Int;
    }

    private BO3Settings(double d)
    {
        value = d;
        returnType = SettingsType.Double;
    }

    private BO3Settings(float f)
    {
        value = f;
        returnType = SettingsType.Float;
    }

    private BO3Settings(String s)
    {
        value = s;
        returnType = SettingsType.String;
    }

    private BO3Settings(String s, SettingsType type)
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

    private BO3Settings(Enum<?> e)
    {
        value = e;
        returnType = SettingsType.Enum;

    }

    private BO3Settings(Boolean b)
    {
        value = b;
        returnType = SettingsType.Boolean;
    }

    public int intValue()
    {
        return (Integer) value;
    }

    public double doubleValue()
    {
        return (Double) value;
    }

    public float floatValue()
    {
        return (Float) value;
    }

    public Enum<?> enumValue()
    {
        return (Enum<?>) value;
    }

    public SettingsType getReturnType()
    {
        return returnType;
    }

    public String stringValue()
    {
        return (String) value;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<String> stringArrayListValue()
    {
        return (ArrayList<String>) value;
    }

    public boolean booleanValue()
    {
        return (Boolean) value;
    }
}
