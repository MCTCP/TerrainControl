package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.customobjects.StructurePartSpawnHeight;
import com.khorn.terraincontrol.util.MultiTypedSetting;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public enum BO3Settings implements MultiTypedSetting
{
    // BO3
    Author("Unknown"),
    Description("No description given"),
    Version(3),

    // Main settings
    Tree(true),
    Frequency(1),
    Rarity(100.0),
    RotateRandomly(false),
    SpawnHeight(SpawnHeightEnum.highestBlock),
    MinHeight(0),
    MaxHeight(256),
    MaxBranchDepth(10),
    ExcludedBiomes("All", SettingsType.StringArray),

    // Source block settings
    SourceBlock(DefaultMaterial.AIR.id, SettingsType.IntSet),
    OutsideSourceBlock(OutsideSourceBlockEnum.placeAnyway),
    MaxPercentageOutsideSourceBlock(100);

    // The spawn height
    public enum SpawnHeightEnum
    {
        randomY(StructurePartSpawnHeight.PROVIDED),
        highestBlock(StructurePartSpawnHeight.HIGHEST_BLOCK),
        highestSolidBlock(StructurePartSpawnHeight.HIGHEST_SOLID_BLOCK);

        private StructurePartSpawnHeight height;

        private SpawnHeightEnum(StructurePartSpawnHeight height)
        {
            this.height = height;
        }

        public StructurePartSpawnHeight toStructurePartSpawnHeight()
        {
            return height;
        }
    }

    // What to do when outside the source block
    public enum OutsideSourceBlockEnum
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
            else if (!s.isEmpty())
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
