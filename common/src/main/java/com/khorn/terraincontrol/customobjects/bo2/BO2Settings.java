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
    collisionPercentage(2.0D),
    collisionBlockType("All", SettingsType.StringArray),
    spawnElevationMin(0),
    spawnElevationMax(128),

    groupFrequencyMin(1),
    groupFrequencyMax(5),
    groupSeperationMin(0), // Seperation - lol.
    groupSeperationMax(5),

    branchLimit(6),

    groupId(""),

    spawnInBiome("All", SettingsType.StringArray);

    private int iValue;
    private String sValue;
    private boolean bValue;
    private double dValue;
    private final SettingsType type;

    private BO2Settings(int i)
    {
        this.iValue = i;
        this.type = SettingsType.Int;
    }

    private BO2Settings(String s)
    {
        this.sValue = s;
        this.type = SettingsType.String;
    }

    private BO2Settings(boolean b)
    {
        this.bValue = b;
        this.type = SettingsType.Boolean;
    }
    
    private BO2Settings(double d)
    {
        this.dValue = d;
        this.type = SettingsType.Double;
    }
    
    private BO2Settings(String s, SettingsType type)
    {
        this.sValue = s;
        this.type = type;
    }

    public int intValue()
    {
        return this.iValue;
    }

    public String stringValue()
    {
        return this.sValue;
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
        return this.iValue;
    }

    @Override
    public float floatValue()
    {
        return (float) this.dValue;
    }

    @Override
    public double doubleValue()
    {
        return this.dValue;
    }

    @Override
    public Enum<?> enumValue()
    {
        throw new UnsupportedOperationException("Enums are not used in BO2s");
    }

    @Override
    public SettingsType getReturnType()
    {
        return type;
    }

    @Override
    public ArrayList<String> stringArrayListValue()
    {
        ArrayList<String> out = new ArrayList<String>();
        if (this.sValue.contains(","))
            Collections.addAll(out, this.sValue.split(","));
        else if (!this.sValue.equals(""))
            out.add(this.sValue);
        return out;
    }

    @Override
    public HashSet<Integer> intSetValue()
    {
        throw new UnsupportedOperationException("Int sets are not used in BO2s");
    }

    @Override
    public boolean booleanValue()
    {
        return this.bValue;
    }
}
