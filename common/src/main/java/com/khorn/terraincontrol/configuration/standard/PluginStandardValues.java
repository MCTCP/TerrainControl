package com.khorn.terraincontrol.configuration.standard;

import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import com.khorn.terraincontrol.configuration.PluginConfig;
import com.khorn.terraincontrol.util.MultiTypedSetting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public enum PluginStandardValues implements MultiTypedSetting
{
   
  //>> Files
    //>>	Main Plugin Config
    ConfigFilename("TerrainControl.ini"),
    
  //>> Folders
    BiomeConfigDirectoryName("GlobalBiomes"),
    BO_DirectoryName("GlobalObjects"),
    
  //>>  Network
    ChannelName("TerrainControl"),
    ProtocolVersion(5),
    
  //>>  Plugin Defaults
    LogLevel(PluginConfig.LogLevels.Standard);
    

    private int iValue;
    private long lValue;
    private double dValue;
    private float fValue;
    private String sValue;
    private boolean bValue;
    private Enum<?> eValue;
    private SettingsType returnType;
    private ArrayList<String> sArrayValue;
    private HashSet<Integer> iSetValue;

    private PluginStandardValues(int i)
    {
        this.iValue = i;
        this.returnType = SettingsType.Int;
    }

    @SuppressWarnings("UnusedDeclaration")
    private PluginStandardValues(HashSet<Integer> i)
    {
        this.iSetValue = i;
        this.returnType = SettingsType.IntSet;
    }

    private PluginStandardValues(double d)
    {
        this.dValue = d;
        this.returnType = SettingsType.Double;
    }

    private PluginStandardValues(float f)
    {
        this.fValue = f;
        this.returnType = SettingsType.Float;
    }

    private PluginStandardValues(long l)
    {
        this.lValue = l;
        this.returnType = SettingsType.Long;
    }

    private PluginStandardValues(String s)
    {
        this.sValue = s;
        this.returnType = SettingsType.String;
    }

    private PluginStandardValues(String s, SettingsType type)
    {
        this.returnType = type;

        if (type == SettingsType.StringArray)
        {
            this.sArrayValue = new ArrayList<String>();
            if (s.contains(","))
                Collections.addAll(this.sArrayValue, s.split(","));
            else if (!s.isEmpty())
                this.sArrayValue.add(s);
            return;
        }
        this.sValue = s;

    }

    private PluginStandardValues(Enum<?> e)
    {
        this.eValue = e;
        this.returnType = SettingsType.Enum;

    }

    private PluginStandardValues(boolean b)
    {
        this.bValue = b;
        this.returnType = SettingsType.Boolean;
    }

    @Override
    public int intValue()
    {
        return this.iValue;
    }

    @Override
    public long longValue()
    {
        return this.lValue;
    }

    @Override
    public double doubleValue()
    {
        return this.dValue;
    }

    @Override
    public float floatValue()
    {
        return this.fValue;
    }

    @Override
    public Enum<?> enumValue()
    {
        return this.eValue;
    }

    @Override
    public SettingsType getReturnType()
    {
        return this.returnType;
    }

    @Override
    public String stringValue()
    {
        return this.sValue;
    }

    @Override
    public ArrayList<String> stringArrayListValue()
    {
        return this.sArrayValue;
    }

    @Override
    public boolean booleanValue()
    {
        return this.bValue;
    }

    @Override
    public HashSet<Integer> intSetValue()
    {
        return this.iSetValue;
    }

    @Override
    public DefaultMaterial materialValue()
    {
        throw new UnsupportedOperationException();
    }

}