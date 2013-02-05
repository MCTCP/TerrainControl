package com.khorn.terraincontrol.customobjects.bo2;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigFile;
import com.khorn.terraincontrol.customobjects.BODefaultValues;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.Rotation;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * The good old BO2.
 */
public class BO2 extends ConfigFile implements CustomObject
{
    public ObjectCoordinate[][] data = new ObjectCoordinate[4][];

    public BO2[] groupObjects = null;

    public String name;

    public HashSet<String> spawnInBiome;

    public String version;
    public HashSet<Integer> spawnOnBlockType;

    public HashSet<Integer> collisionBlockType;

    public boolean spawnWater;
    public boolean spawnLava;
    public boolean spawnAboveGround;
    public boolean spawnUnderGround;

    public boolean spawnSunlight;
    public boolean spawnDarkness;

    public boolean underFill;
    public boolean randomRotation;
    public boolean dig;
    public boolean tree;
    public boolean branch;
    public boolean diggingBranch;
    public boolean needsFoundation;
    public int rarity;
    public double collisionPercentage;
    public int spawnElevationMin;
    public int spawnElevationMax;

    public int groupFrequencyMin;
    public int groupFrequencyMax;
    public int groupSeparationMin;
    public int groupSeparationMax;
    public String groupId;

    public int branchLimit;

    public BO2(File file, String name)
    {
        readSettingsFile(file);
        this.name = name;
    }

    public BO2(Map<String, String> settings, String name)
    {
        settingsCache = settings;
        this.name = name;
        // Initialize immediately
        readConfigSettings();
        correctSettings();
    }
    
    @Override
    public void onEnable(Map<String,CustomObject> otherObjectsInDirectory) {
        readConfigSettings();
        correctSettings();
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public boolean canSpawnAsTree()
    {
        return tree;
    }

    @Override
    public boolean canSpawnAsObject()
    {
        return true;
    }
    
    @Override
    public boolean canRotateRandomly()
    {
        return randomRotation;
    }

    @Override
    public boolean spawnForced(LocalWorld world, Random random, Rotation rotation, int x, int y, int z)
    {
        ObjectCoordinate[] data = this.data[rotation.getRotationId()];

        // Spawn
        for (ObjectCoordinate point : data)
        {
            if (world.getTypeId(x + point.x, y + point.y, z + point.z) == 0)
            {
                world.setBlock((x + point.x), y + point.y, z + point.z, point.blockId, point.blockData, true, false, true);
            } else if (dig)
            {
                world.setBlock((x + point.x), y + point.y, z + point.z, point.blockId, point.blockData, true, false, true);
            }
        }
        return true;
    }

    public boolean canSpawnAt(LocalWorld world, Rotation rotation, int x, int y, int z)
    {
        // Basic checks
        if ((world.getTypeId(x, y - 5, z) == 0) && (needsFoundation))
            return false;

        int checkBlock = world.getTypeId(x, y + 2, z);
        if (!spawnWater)
        {
            if ((checkBlock == DefaultMaterial.WATER.id) || (checkBlock == DefaultMaterial.STATIONARY_WATER.id))
                return false;
        }
        if (!spawnLava)
        {
            if ((checkBlock == DefaultMaterial.LAVA.id) || (checkBlock == DefaultMaterial.STATIONARY_LAVA.id))
                return false;
        }

        checkBlock = world.getLightLevel(x, y + 2, z);
        if (!spawnSunlight)
        {
            if (checkBlock > 8)
                return false;
        }
        if (!spawnDarkness)
        {
            if (checkBlock < 9)
                return false;
        }

        if ((y < spawnElevationMin) || (y > spawnElevationMax))
            return false;

        if (!spawnOnBlockType.contains(world.getTypeId(x, y - 1, z)))
            return false;

        ObjectCoordinate[] data = this.data[rotation.getRotationId()];

        // Check all blocks
        int faultCounter = 0;

        for (ObjectCoordinate point : data)
        {
            if (!world.isLoaded((x + point.x), (y + point.y), (z + point.z)))
                return false;

            if (!dig)
            {
                if (collisionBlockType.contains(world.getTypeId((x + point.x), (y + point.y), (z + point.z))))
                {
                    faultCounter++;
                    if (faultCounter > (data.length * (collisionPercentage / 100)))
                    {
                        return false;
                    }
                }
            }
        }

        // Call event
        if (!TerrainControl.fireCanCustomObjectSpawnEvent(this, world, x, y, z))
        {
            // Cancelled
            return false;
        }

        return true;
    }

    @Override
    public boolean spawn(LocalWorld world, Random random, int x, int z)
    {
        int y;
        if (spawnAboveGround)
            y = world.getSolidHeight(x, z);
        else if (spawnUnderGround)
        {
            int solidHeight = world.getSolidHeight(x, z);
            if (solidHeight < 1 || solidHeight <= spawnElevationMin)
                return false;
            if (solidHeight > spawnElevationMax)
                solidHeight = spawnElevationMax;
            y = random.nextInt(solidHeight - spawnElevationMin) + spawnElevationMin;
        } else
            y = world.getHighestBlockYAt(x, z);

        if (y < 0)
            return false;

        Rotation rotation = randomRotation ? Rotation.getRandomRotation(random) : Rotation.NORTH;

        if (!canSpawnAt(world, rotation, x, y, z))
            return false;

        boolean objectSpawned = spawnForced(world, random, rotation, x, y, z);

//        if (objectSpawned)
//            GenerateCustomObjectFromGroup(world, random, x, y, z);

        return objectSpawned;
    }

    @Override
    public boolean spawnAsTree(LocalWorld world, Random random, int x, int z)
    {
        return spawn(world, random, x, z);
    }

    @Override
    public boolean process(LocalWorld world, Random rand, int chunkX, int chunkZ)
    {

        if (branch)
            return false;

        int randomRoll = rand.nextInt(100);
        int ObjectRarity = rarity;
        boolean objectSpawned = false;

        while (randomRoll < ObjectRarity)
        {
            ObjectRarity -= 100;

            int x = chunkX * 16 + rand.nextInt(16);
            int z = chunkZ * 16 + rand.nextInt(16);

            objectSpawned = spawn(world, rand, x, z);
        }

        return objectSpawned;
    }

    @Override
    public CustomObject applySettings(Map<String, String> extraSettings)
    {
        Map<String, String> newSettings = new HashMap<String, String>();
        newSettings.putAll(settingsCache);
        newSettings.putAll(extraSettings);
        return new BO2(newSettings, getName());
    }

    @Override
    protected void writeConfigSettings() throws IOException
    {
        // It doesn't write.
    }

    @Override
    protected void readConfigSettings()
    {
        this.version = readModSettings(BODefaultValues.version.name(), BODefaultValues.version.stringValue());

        this.spawnOnBlockType = this.ReadBlockList(readModSettings(BODefaultValues.spawnOnBlockType.name(), BODefaultValues.spawnOnBlockType.StringArrayListValue()), BODefaultValues.spawnOnBlockType.name());
        this.collisionBlockType = this.ReadBlockList(readModSettings(BODefaultValues.collisionBlockType.name(), BODefaultValues.collisionBlockType.StringArrayListValue()), BODefaultValues.collisionBlockType.name());

        this.spawnInBiome = new HashSet<String>(readModSettings(BODefaultValues.spawnInBiome.name(), BODefaultValues.spawnInBiome.StringArrayListValue()));

        this.spawnSunlight = readModSettings(BODefaultValues.spawnSunlight.name(), BODefaultValues.spawnSunlight.booleanValue());
        this.spawnDarkness = readModSettings(BODefaultValues.spawnDarkness.name(), BODefaultValues.spawnDarkness.booleanValue());
        this.spawnWater = readModSettings(BODefaultValues.spawnWater.name(), BODefaultValues.spawnWater.booleanValue());
        this.spawnLava = readModSettings(BODefaultValues.spawnLava.name(), BODefaultValues.spawnLava.booleanValue());
        this.spawnAboveGround = readModSettings(BODefaultValues.spawnAboveGround.name(), BODefaultValues.spawnAboveGround.booleanValue());
        this.spawnUnderGround = readModSettings(BODefaultValues.spawnUnderGround.name(), BODefaultValues.spawnUnderGround.booleanValue());

        this.underFill = readModSettings(BODefaultValues.underFill.name(), BODefaultValues.underFill.booleanValue());

        this.randomRotation = readModSettings(BODefaultValues.randomRotation.name(), BODefaultValues.randomRotation.booleanValue());
        this.dig = readModSettings(BODefaultValues.dig.name(), BODefaultValues.dig.booleanValue());
        this.tree = readModSettings(BODefaultValues.tree.name(), BODefaultValues.tree.booleanValue());
        this.branch = readModSettings(BODefaultValues.branch.name(), BODefaultValues.branch.booleanValue());
        this.diggingBranch = readModSettings(BODefaultValues.diggingBranch.name(), BODefaultValues.diggingBranch.booleanValue());
        this.needsFoundation = readModSettings(BODefaultValues.needsFoundation.name(), BODefaultValues.needsFoundation.booleanValue());
        this.rarity = readModSettings(BODefaultValues.rarity.name(), BODefaultValues.rarity.intValue());
        this.collisionPercentage = readModSettings(BODefaultValues.collisionPercentage.name(), BODefaultValues.collisionPercentage.intValue());
        this.spawnElevationMin = readModSettings(BODefaultValues.spawnElevationMin.name(), BODefaultValues.spawnElevationMin.intValue());
        this.spawnElevationMax = readModSettings(BODefaultValues.spawnElevationMax.name(), BODefaultValues.spawnElevationMax.intValue());

        this.groupFrequencyMin = readModSettings(BODefaultValues.groupFrequencyMin.name(), BODefaultValues.groupFrequencyMin.intValue());
        this.groupFrequencyMax = readModSettings(BODefaultValues.groupFrequencyMax.name(), BODefaultValues.groupFrequencyMax.intValue());
        this.groupSeparationMin = readModSettings(BODefaultValues.groupSeperationMin.name(), BODefaultValues.groupSeperationMin.intValue());
        this.groupSeparationMax = readModSettings(BODefaultValues.groupSeperationMax.name(), BODefaultValues.groupSeperationMax.intValue());
        this.groupId = readModSettings(BODefaultValues.groupId.name(), BODefaultValues.groupId.stringValue());

        this.branchLimit = readModSettings(BODefaultValues.branchLimit.name(), BODefaultValues.branchLimit.intValue());

        this.ReadCoordinates();
    }

    @Override
    protected void correctSettings()
    {
        // Stub method
    }

    @Override
    protected void renameOldSettings()
    {
        // Stub method
    }

    private void ReadCoordinates()
    {
        ArrayList<ObjectCoordinate> coordinates = new ArrayList<ObjectCoordinate>();

        for (String key : settingsCache.keySet())
        {
            ObjectCoordinate buffer = ObjectCoordinate.getCoordinateFromString(key, settingsCache.get(key));
            if (buffer != null)
                coordinates.add(buffer);
        }

        data[0] = new ObjectCoordinate[coordinates.size()];
        data[1] = new ObjectCoordinate[coordinates.size()];
        data[2] = new ObjectCoordinate[coordinates.size()];
        data[3] = new ObjectCoordinate[coordinates.size()];

        for (int i = 0; i < coordinates.size(); i++)
        {
            ObjectCoordinate coordinate = coordinates.get(i);

            data[0][i] = coordinate;
            coordinate = coordinate.Rotate();
            data[1][i] = coordinate;
            coordinate = coordinate.Rotate();
            data[2][i] = coordinate;
            coordinate = coordinate.Rotate();
            data[3][i] = coordinate;
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
            System.out.println("TerrainControl: Custom object " + this.name + " has wrong value " + settingName);

        return output;

    }

    // Old branch code - is being rewritten
/*    public void GenerateCustomObjectFromGroup(LocalWorld world, Random random, int x, int y, int z)
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

    }*/

    @Override
    public boolean hasPreferenceToSpawnIn(LocalBiome biome)
    {
        return spawnInBiome.contains(biome.getName()) || spawnInBiome.contains("All");
    }
}
