package com.khorn.terraincontrol.customobjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum BODefaultValues
{

    BO_GlobalDirectoryName("GlobalObjects"),
    BO_Use_World("UseWorld"),
    BO_Use_Biome("UseBiome"),
    BO_ALL_KEY("All"),
    BO_SolidKey("Solid"),
    BO_Extension("bo2"),

    // Custom object settings
    version("2.0"),
    spawnOnBlockType("2"),
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
    collisionPercentage(2),
    collisionBlockType("All"),
    spawnElevationMin(0),
    spawnElevationMax(128),

    groupFrequencyMin(1),
    groupFrequencyMax(5),
    groupSeperationMin(0),     // Seperation - lol.
    groupSeperationMax(5),

    branchLimit(6),

    groupId(""),

    spawnInBiome("All");

    private int iValue;
    private String sValue;
    private boolean bValue;

    private BODefaultValues(int i)
    {
        this.iValue = i;
    }

    private BODefaultValues(String s)
    {
        this.sValue = s;
    }

    private BODefaultValues(Boolean b)
    {
        this.bValue = b;
    }

    public int intValue()
    {
        return this.iValue;
    }

    public String stringValue()
    {
        return this.sValue;
    }

    public ArrayList<String> StringArrayListValue()
    {
        ArrayList<String> out = new ArrayList<String>();
        if (this.sValue.contains(","))
            Collections.addAll(out, this.sValue.split(","));
        else if (!this.sValue.equals(""))
            out.add(this.sValue);
        return out;
    }

    public Boolean booleanValue()
    {
        return this.bValue;
    }

    private static Map<String, BODefaultValues> lookupName;

    static
    {
        lookupName = new HashMap<String, BODefaultValues>();

        for (BODefaultValues value : BODefaultValues.values())
        {
            lookupName.put(value.name().toLowerCase(), value);
        }
    }

    public static boolean Contains(String name)
    {
        return lookupName.containsKey(name);
    }
}
