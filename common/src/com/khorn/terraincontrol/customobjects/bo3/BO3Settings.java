package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.customobjects.CustomObjectCoordinate.SpawnHeight;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.configuration.TCSetting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public enum BO3Settings implements TCSetting
{
    // BO3
    author("Unknown"),
    description("No description given"),

    // Main settings
    tree(true),
    frequency(1),
    rarity(100.0),
    rotateRandomly(false),
    spawnHeight(SpawnHeightSetting.highestBlock),
    minHeight(0),
    maxHeight(256),
    maxBranchDepth(10),
    excludedBiomes("All", SettingsType.StringArray),

    // Source block settings
    sourceBlock(DefaultMaterial.AIR.id, SettingsType.IntSet),
    outsideSourceBlock(OutsideSourceBlock.placeAnyway),
    maxPercentageOutsideSourceBlock(100);

    // The spawn height
    public enum SpawnHeightSetting
    {
        randomY(SpawnHeight.PROVIDED),
        highestBlock(SpawnHeight.HIGHEST_BLOCK),
        highestSolidBlock(SpawnHeight.HIGHEST_SOLID_BLOCK);

        private SpawnHeight height;

        private SpawnHeightSetting(SpawnHeight height)
        {
            this.height = height;
        }

        public SpawnHeight toSpawnHeight()
        {
            return height;
        }
    }

    // What to do when outside the source block
    public enum OutsideSourceBlock
    {
        dontPlace,
        placeAnyway
    }

    private Object value;
    private SettingsType returnType;

    private BO3Settings(int i)
    {
        value = i;
        returnType = SettingsType.Int;
    }
    
    private BO3Settings(int i, SettingsType type)
    {
        if (type == SettingsType.IntSet){
            HashSet<Integer> x = new HashSet<Integer>();
            x.add(i);
            value = x;
            returnType = SettingsType.IntSet;
        } else {
            value = i;
            returnType = SettingsType.Int;
        }
    }
    
    private BO3Settings(HashSet<Integer> set, SettingsType type)
    {
        value = set;
        returnType = SettingsType.IntSet;
    }
    
    private BO3Settings(long i)
    {
        value = i;
        returnType = SettingsType.Long;
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

    @Override
    public int intValue()
    {
        return (Integer) value;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public HashSet<Integer> intSetValue() {
        return (HashSet<Integer>) value;
    }
    
    @Override
    public double doubleValue()
    {
        return (Double) value;
    }

    @Override
    public float floatValue()
    {
        return (Float) value;
    }

    @Override
    public Enum<?> enumValue()
    {
        return (Enum<?>) value;
    }

    @Override
    public SettingsType getReturnType()
    {
        return returnType;
    }

    @Override
    public String stringValue()
    {
        return (String) value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<String> stringArrayListValue()
    {
        return (ArrayList<String>) value;
    }

    @Override
    public boolean booleanValue()
    {
        return (Boolean) value;
    }

    @Override
    public long longValue()
    {
        return (Long) value;
    }
}
