package com.khorn.terraincontrol.customobjects.bo2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.ConfigFile;
import com.khorn.terraincontrol.customobjects.BODefaultValues;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.ObjectCoordinate;

/**
 * The good old BO2.
 * 
 */
public class BO2 extends ConfigFile implements CustomObject
{
    public ObjectCoordinate[][] Data = new ObjectCoordinate[4][];

    public BO2[] GroupObjects = null;

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

    public BO2(File file, String name)
    {
        ReadSettingsFile(file);
        this.Name = name;

        ReadConfigSettings();
        CorrectSettings();
    }

    public BO2(Map<String, String> settings, String name)
    {
        SettingsCache = settings;
        this.Name = name;

        ReadConfigSettings();
        CorrectSettings();
    }

    @Override
    public String getName()
    {
        return this.Name;
    }

    @Override
    public boolean canSpawnAsTree()
    {
        return Tree;
    }

    @Override
    public boolean canSpawnAsObject()
    {
        return true;
    }

    @Override
    public boolean spawn(LocalWorld world, Random random, int x, int y, int z)
    {
        ObjectCoordinate[] data = Data[0];
        if (RandomRotation)
            data = Data[random.nextInt(4)];

        int faultCounter = 0;

        for (ObjectCoordinate point : data)
        {
            if (!world.isLoaded((x + point.x), (y + point.y), (z + point.z)))
                return false;

            if (!Dig)
            {
                if (CollisionBlockType.contains(world.getTypeId((x + point.x), (y + point.y), (z + point.z))))
                {
                    faultCounter++;
                    if (faultCounter > (data.length * (CollisionPercentage / 100)))
                    {
                        return false;
                    }
                }
            }

        }

        for (ObjectCoordinate point : data)
        {

            if (world.getTypeId(x + point.x, y + point.y, z + point.z) == 0)
            {
                world.setBlock((x + point.x), y + point.y, z + point.z, point.BlockId, point.BlockData, true, false, true);
            } else if (Dig)
            {
                world.setBlock((x + point.x), y + point.y, z + point.z, point.BlockId, point.BlockData, true, false, true);
            }

        }
        return true;
    }

    @Override
    public boolean spawnAsTree(LocalWorld world, Random random, int x, int y, int z)
    {
        if (!Tree)
        {
            // Can only spawn as a tree if this is a tree.
            return false;
        }
        return spawn(world, random, x, y, z);
    }

    @Override
    public boolean spawn(LocalWorld world, Random random, int x, int z)
    {
        int y;
        if (SpawnAboveGround)
            y = world.getSolidHeight(x, z);
        else if (SpawnUnderGround)
        {
            int solidHeight = world.getSolidHeight(x, z);
            if (solidHeight < 1 || solidHeight <= SpawnElevationMin)
                return false;
            if (solidHeight > SpawnElevationMax)
                solidHeight = SpawnElevationMax;
            y = random.nextInt(solidHeight - SpawnElevationMin) + SpawnElevationMin;
        } else
            y = world.getHighestBlockYAt(x, z);

        if (y < 0)
            return false;

        if (!ObjectCanSpawn(world, x, y, z))
            return false;

        boolean objectSpawned = this.spawn(world, random, x, y, z);

        if (objectSpawned)
            GenerateCustomObjectFromGroup(world, random, x, y, z);

        return objectSpawned;
    }

    @Override
    public boolean spawnAsTree(LocalWorld world, Random random, int x, int z)
    {
        return spawn(world, random, x, z);
    }

    @Override
    public void process(LocalWorld world, Random random, int chunkX, int chunkZ)
    {
        int randomRoll = random.nextInt(100);
        int ObjectRarity = Rarity;

        while (randomRoll < ObjectRarity)
        {
            ObjectRarity -= 100;

            int x = chunkX * 16 + random.nextInt(16) + 8;
            int z = chunkZ * 16 + random.nextInt(16) + 8;

            spawn(world, random, x, z);
        }
    }

    @Override
    public void processAsTree(LocalWorld world, Random random, int chunkX, int chunkZ)
    {
        if (!Tree)
        {
            return;
        }

        process(world, random, chunkX, chunkZ);
    }

    @Override
    public CustomObject applySettings(Map<String, String> extraSettings)
    {
        Map<String, String> newSettings = new HashMap<String, String>();
        newSettings.putAll(SettingsCache);
        newSettings.putAll(extraSettings);
        return new BO2(newSettings, getName());
    }

    @Override
    protected void WriteConfigSettings() throws IOException
    {
        // It doesn't write.
    }

    @Override
    protected void ReadConfigSettings()
    {
        this.Version = ReadModSettings(BODefaultValues.version.name(), BODefaultValues.version.stringValue());

        this.SpawnOnBlockType = this.ReadBlockList(ReadModSettings(BODefaultValues.spawnOnBlockType.name(), BODefaultValues.spawnOnBlockType.StringArrayListValue()), BODefaultValues.spawnOnBlockType.name());
        this.CollisionBlockType = this.ReadBlockList(ReadModSettings(BODefaultValues.collisionBlockType.name(), BODefaultValues.collisionBlockType.StringArrayListValue()), BODefaultValues.collisionBlockType.name());

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
        // Stub method
    }

    @Override
    protected void RenameOldSettings()
    {
        // Stub method
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
                if (material.id == 0)
                    continue;
                if (solid && !material.isSolid())
                    continue;
                output.add(material.id);

            }
        if (nonIntegerValues)
            System.out.println("TerrainControl: Custom object " + this.Name + " have wrong value " + settingName);

        return output;

    }

    public boolean ObjectCanSpawn(LocalWorld world, int x, int y, int z)
    {
        if ((world.getTypeId(x, y - 5, z) == 0) && (NeedsFoundation))
            return false;

        boolean output = true;
        int checkBlock = world.getTypeId(x, y + 2, z);
        if (!SpawnWater)
            output = !((checkBlock == DefaultMaterial.WATER.id) || (checkBlock == DefaultMaterial.STATIONARY_WATER.id));
        if (!SpawnLava)
            output = !((checkBlock == DefaultMaterial.LAVA.id) || (checkBlock == DefaultMaterial.STATIONARY_LAVA.id));

        checkBlock = world.getLightLevel(x, y + 2, z);
        if (!SpawnSunlight)
            output = !(checkBlock > 8);
        if (!SpawnDarkness)
            output = !(checkBlock < 9);

        if ((y < SpawnElevationMin) || (y > SpawnElevationMax))
            output = false;

        if (!SpawnOnBlockType.contains(world.getTypeId(x, y - 1, z)))
            output = false;

        return output;
    }

    public void GenerateCustomObjectFromGroup(LocalWorld world, Random random, int x, int y, int z)
    {
        if (GroupObjects == null)
            return;

        int attempts = 3;
        if ((GroupFrequencyMax - GroupFrequencyMin) > 0)
            attempts = GroupFrequencyMin + random.nextInt(GroupFrequencyMax - GroupFrequencyMin);

        while (attempts > 0)
        {
            attempts--;

            int objIndex = random.nextInt(GroupObjects.length);
            BO2 ObjectFromGroup = GroupObjects[objIndex];

            if (Branch)
                continue;

            x = x + random.nextInt(GroupSeparationMax - GroupSeparationMin) + GroupSeparationMin;
            z = z + random.nextInt(GroupSeparationMax - GroupSeparationMin) + GroupSeparationMin;
            int _y;

            if (SpawnAboveGround)
                _y = world.getSolidHeight(x, z);
            else if (SpawnUnderGround)
            {
                int solidHeight = world.getSolidHeight(x, z);
                if (solidHeight < 1 || solidHeight <= SpawnElevationMin)
                    continue;
                if (solidHeight > SpawnElevationMax)
                    solidHeight = SpawnElevationMax;
                _y = random.nextInt(solidHeight - SpawnElevationMin) + SpawnElevationMin;
            } else
                _y = world.getHighestBlockYAt(x, z);

            if (y < 0)
                continue;

            if ((y - _y) > 10 || (_y - y) > 10)
                continue;

            ObjectFromGroup.spawn(world, random, x, _y, z);
        }

    }

    @Override
    public boolean hasPreferenceToSpawnIn(LocalBiome biome)
    {
        return SpawnInBiome.contains(biome.getName()) || SpawnInBiome.contains("All");
    }

}
