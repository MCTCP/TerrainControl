package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.DefaultMaterial;
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

    public HashSet<String> SpawnInBiome;

    public String Version;
    public HashSet<Integer> SpawnOnBlockType;

    public HashSet<Integer> CollisionBlockType;

    public boolean SpawnWater;
    public boolean SpawnLava;
    public boolean SpawnAboveGround;
    public boolean SpawnUnderGround;

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
    public String GroupId;

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


    public boolean CheckBiome(String biomeName)
    {
        return (SpawnInBiome.contains(BODefaultValues.BO_ALL_KEY.stringValue()) || SpawnInBiome.contains(BODefaultValues.BO_ALL_KEY.stringValue().toLowerCase()) || SpawnInBiome.contains(biomeName));
    }


    @Override
    protected void ReadConfigSettings()
    {
        this.Version = ReadModSettings(BODefaultValues.version.name(), BODefaultValues.version.stringValue());


        this.SpawnOnBlockType = this.ReadBlockList(ReadModSettings(BODefaultValues.spawnOnBlockType.name(), BODefaultValues.spawnOnBlockType.StringArrayListValue()),BODefaultValues.spawnOnBlockType.name());
        this.CollisionBlockType = this.ReadBlockList(ReadModSettings(BODefaultValues.collisionBlockType.name(), BODefaultValues.collisionBlockType.StringArrayListValue()),BODefaultValues.collisionBlockType.name());

        this.SpawnInBiome = new HashSet<String>(ReadModSettings(BODefaultValues.spawnInBiome.name(), BODefaultValues.spawnInBiome.StringArrayListValue()));


        this.SpawnSunlight = ReadModSettings(BODefaultValues.spawnSunlight.name(), BODefaultValues.spawnSunlight.booleanValue());
        this.SpawnDarkness = ReadModSettings(BODefaultValues.spawnDarkness.name(), BODefaultValues.spawnDarkness.booleanValue());
        this.SpawnWater = ReadModSettings(BODefaultValues.spawnWater.name(), BODefaultValues.spawnWater.booleanValue());
        this.SpawnLava = ReadModSettings(BODefaultValues.spawnLava.name(), BODefaultValues.spawnLava.booleanValue());
        this.SpawnAboveGround = ReadModSettings(BODefaultValues.spawnAboveGround.name(), BODefaultValues.spawnAboveGround.booleanValue());
        this.SpawnUnderGround = ReadModSettings(BODefaultValues.spawnUnderGround.name(), BODefaultValues.spawnUnderGround.booleanValue());

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
        this.GroupId = ReadModSettings(BODefaultValues.groupId.name(), BODefaultValues.groupId.stringValue());


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

    private HashSet<Integer> ReadBlockList(ArrayList<String> blocks, String settingName)
    {
        HashSet<Integer> output = new HashSet<Integer>();

        boolean nonIntegerValues = false;
        boolean all = false;
        boolean solid = false;

        for (String block : blocks)
        {

            if (block.equals(BODefaultValues.BO_ALL_KEY.stringValue()))
            {
                all = true;
                continue;
            }
            if (block.equals(BODefaultValues.BO_SolidKey.stringValue()))
            {
                solid = true;
                continue;
            }
            try
            {
                int blockID = Integer.decode(block);
                if (blockID != 0)
                    output.add(blockID);
            } catch (NumberFormatException e)
            {
                nonIntegerValues = true;
            }
        }

        if (all || solid)
            for (DefaultMaterial material : DefaultMaterial.values())
            {
                if(material.id == 0)
                    continue;
                if (solid && !material.isSolid())
                    continue;
                output.add(material.id);

            }
        if (nonIntegerValues)
            System.out.println("TerrainControl: Custom object " + this.Name + " have wrong value " + settingName);

        return output;

    }
}
