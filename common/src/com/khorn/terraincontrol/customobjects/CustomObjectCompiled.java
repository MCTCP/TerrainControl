package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.configuration.ConfigFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CustomObjectCompiled extends ConfigFile
{
    public ObjectCoordinate[][] Data = new ObjectCoordinate[4][];

    public CustomObjectCompiled[] GroupObjects = null;

    public String Name;

    public String Version;
    public HashSet<Integer> SpawnOnBlockType;

    public boolean SpawnWater;
    public boolean SpawnLava;
    public boolean SpawnSunlight;
    public boolean SpawnDarkness;

    public boolean UnderFill;
    public boolean RandomRotation;
    public boolean Dig;
    public boolean Tree;
    public boolean Branch;
    public boolean DiggingBranch;
    public boolean NeedsFoundation;
    public int Rarity;
    public double CollisionPercentage;
    public int SpawnElevationMin;
    public int SpawnElevationMax;

    public int GroupFrequencyMin;
    public int GroupFrequencyMax;
    public int GroupSeparationMin;
    public int GroupSeparationMax;

    public int BranchLimit;

    public String ChangedSettings;

    public CustomObject parent;

    public CustomObjectCompiled(HashMap<String, String> settings, String name, String changedSettings, CustomObject parent)
    {
        SettingsCache = settings;
        this.Name = name;
        this.ChangedSettings = changedSettings;
        this.parent = parent;

        ReadConfigSettings();
        CorrectSettings();

        SettingsCache.clear();
        SettingsCache = null;


    }


    @Override
    protected void ReadConfigSettings()
    {
        this.Version = ReadModSettings(BODefaultValues.version.name(), BODefaultValues.version.stringValue());
        this.SpawnOnBlockType = new HashSet<Integer>();
        ArrayList<String> blocks = ReadModSettings(BODefaultValues.spawnOnBlockType.name(), BODefaultValues.spawnOnBlockType.StringArrayListValue());
        try
        {
            for (String block : blocks)
            {
                int blockID = Integer.decode(block);
                if (blockID != 0)
                    this.SpawnOnBlockType.add(blockID);
            }
        } catch (NumberFormatException e)
        {
            System.out.println("TerrainControl: Custom object " + Name + " have wrong value " + BODefaultValues.spawnOnBlockType.name());
        }

        this.SpawnSunlight = ReadModSettings(BODefaultValues.spawnSunlight.name(), BODefaultValues.spawnSunlight.booleanValue());
        this.SpawnDarkness = ReadModSettings(BODefaultValues.spawnDarkness.name(), BODefaultValues.spawnDarkness.booleanValue());
        this.SpawnWater = ReadModSettings(BODefaultValues.spawnWater.name(), BODefaultValues.spawnWater.booleanValue());
        this.SpawnLava = ReadModSettings(BODefaultValues.spawnLava.name(), BODefaultValues.spawnLava.booleanValue());
        this.UnderFill = ReadModSettings(BODefaultValues.underFill.name(), BODefaultValues.underFill.booleanValue());

        this.RandomRotation = ReadModSettings(BODefaultValues.randomRotation.name(), BODefaultValues.randomRotation.booleanValue());
        this.Dig = ReadModSettings(BODefaultValues.dig.name(), BODefaultValues.dig.booleanValue());
        this.Tree = ReadModSettings(BODefaultValues.tree.name(), BODefaultValues.tree.booleanValue());
        this.Branch = ReadModSettings(BODefaultValues.branch.name(), BODefaultValues.branch.booleanValue());
        this.DiggingBranch = ReadModSettings(BODefaultValues.diggingBranch.name(), BODefaultValues.diggingBranch.booleanValue());
        this.NeedsFoundation = ReadModSettings(BODefaultValues.needsFoundation.name(), BODefaultValues.needsFoundation.booleanValue());
        this.Rarity = ReadModSettings(BODefaultValues.rarity.name(), BODefaultValues.rarity.intValue());
        this.CollisionPercentage = ReadModSettings(BODefaultValues.collisionPercentage.name(), BODefaultValues.collisionPercentage.intValue());
        this.SpawnElevationMin = ReadModSettings(BODefaultValues.spawnElevationMin.name(), BODefaultValues.spawnElevationMin.intValue());
        this.SpawnElevationMax = ReadModSettings(BODefaultValues.spawnElevationMax.name(), BODefaultValues.spawnElevationMax.intValue());

        this.GroupFrequencyMin = ReadModSettings(BODefaultValues.groupFrequencyMin.name(), BODefaultValues.groupFrequencyMin.intValue());
        this.GroupFrequencyMax = ReadModSettings(BODefaultValues.groupFrequencyMax.name(), BODefaultValues.groupFrequencyMax.intValue());
        this.GroupSeparationMin = ReadModSettings(BODefaultValues.groupSeperationMin.name(), BODefaultValues.groupSeperationMin.intValue());
        this.GroupSeparationMax = ReadModSettings(BODefaultValues.groupSeperationMax.name(), BODefaultValues.groupSeperationMax.intValue());

        this.BranchLimit = ReadModSettings(BODefaultValues.branchLimit.name(), BODefaultValues.branchLimit.intValue());

        this.ReadCoordinates();
    }

    @Override
    protected void CorrectSettings()
    {


    }

    @Override
    protected void WriteConfigSettings() throws IOException
    {

    }

    @Override
    protected void RenameOldSettings()
    {

    }


    private void ReadCoordinates()
    {
        ArrayList<ObjectCoordinate> coordinates = new ArrayList<ObjectCoordinate>();

        for (String key : SettingsCache.keySet())
        {
            ObjectCoordinate buffer = ObjectCoordinate.getCoordinateFromString(key, SettingsCache.get(key));
            if (buffer != null)
                coordinates.add(buffer);
        }

        Data[0] = new ObjectCoordinate[coordinates.size()];
        Data[1] = new ObjectCoordinate[coordinates.size()];
        Data[2] = new ObjectCoordinate[coordinates.size()];
        Data[3] = new ObjectCoordinate[coordinates.size()];

        for (int i = 0; i < coordinates.size(); i++)
        {
            ObjectCoordinate coordinate = coordinates.get(i);

            Data[0][i] = coordinate;
            coordinate = coordinate.Rotate();
            Data[1][i] = coordinate;
            coordinate = coordinate.Rotate();
            Data[2][i] = coordinate;
            coordinate = coordinate.Rotate();
            Data[3][i] = coordinate;
        }


    }
}
