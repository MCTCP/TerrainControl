package com.khorn.terraincontrol.customobjects.bo2;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigFile;
import com.khorn.terraincontrol.configuration.io.SettingsReader;
import com.khorn.terraincontrol.configuration.io.SettingsWriter;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.MaterialSet;
import com.khorn.terraincontrol.util.Rotation;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * The good old BO2.
 */
public class BO2 extends ConfigFile implements CustomObject
{

    public ObjectCoordinate[][] data = new ObjectCoordinate[4][];

    public BO2[] groupObjects = null;

    public List<String> spawnInBiome;

    public String version;
    public MaterialSet spawnOnBlockType;
    public MaterialSet collisionBlockType;

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
    public List<String> groupId;

    public int branchLimit;

    public BO2(SettingsReader reader)
    {
        super(reader);
    }

    @Override
    public void onEnable(Map<String, CustomObject> otherObjectsInDirectory)
    {
        enable();
    }

    private void enable()
    {
        readConfigSettings();
        correctSettings();
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
            if (world.isEmpty(x + point.x, y + point.y, z + point.z))
            {
                world.setBlock((x + point.x), y + point.y, z + point.z, point.material);
            } else if (dig)
            {
                world.setBlock((x + point.x), y + point.y, z + point.z, point.material);
            }
        }
        return true;
    }

    @Override
    public boolean canSpawnAt(LocalWorld world, Rotation rotation, int x, int y, int z)
    {
        // Basic checks
        if (world.isEmpty(x, y - 5, z) && (needsFoundation))
            return false;

        LocalMaterialData checkBlock = world.getMaterial(x, y + 2, z);
        if (!spawnWater)
        {
            if (checkBlock.equals(DefaultMaterial.WATER) || checkBlock.equals(DefaultMaterial.STATIONARY_WATER))
                return false;
        }
        if (!spawnLava)
        {
            if (checkBlock.equals(DefaultMaterial.LAVA) || checkBlock.equals(DefaultMaterial.STATIONARY_LAVA))
                return false;
        }

        int checkLight = world.getLightLevel(x, y + 2, z);
        if (!spawnSunlight)
        {
            if (checkLight > 8)
                return false;
        }
        if (!spawnDarkness)
        {
            if (checkLight < 9)
                return false;
        }

        if ((y < spawnElevationMin) || (y > spawnElevationMax))
            return false;

        if (!spawnOnBlockType.contains(world.getMaterial(x, y - 1, z)))
            return false;

        ObjectCoordinate[] objData = this.data[rotation.getRotationId()];

        // Check all blocks
        int faultCounter = 0;

        for (ObjectCoordinate point : objData)
        {
            if (!world.isLoaded((x + point.x), (y + point.y), (z + point.z)))
                return false;

            if (!dig)
            {
                if (collisionBlockType.contains(world.getMaterial((x + point.x), (y + point.y), (z + point.z))))
                {
                    faultCounter++;
                    if (faultCounter > (objData.length * (collisionPercentage / 100)))
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

    protected boolean spawn(LocalWorld world, Random random, int x, int z)
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
    public boolean process(LocalWorld world, Random rand, ChunkCoordinate chunkCoord)
    {

        if (branch)
            return false;

        int randomRoll = rand.nextInt(100);
        int ObjectRarity = rarity;
        boolean objectSpawned = false;

        while (randomRoll < ObjectRarity)
        {
            ObjectRarity -= 100;

            int x = chunkCoord.getBlockX() + rand.nextInt(ChunkCoordinate.CHUNK_X_SIZE);
            int z = chunkCoord.getBlockZ() + rand.nextInt(ChunkCoordinate.CHUNK_Z_SIZE);

            objectSpawned = spawn(world, rand, x, z);
        }

        return objectSpawned;
    }

    @Override
    public CustomObject applySettings(SettingsReader extraSettings)
    {
        extraSettings.setFallbackReader(this.reader);
        BO2 bo2WithSettings = new BO2(extraSettings);
        bo2WithSettings.enable();
        return bo2WithSettings;
    }

    @Override
    protected void writeConfigSettings(SettingsWriter writer) throws IOException
    {
        // It doesn't write.
    }

    @Override
    protected void readConfigSettings()
    {
        this.version = readSettings(BO2Settings.VERSION);

        this.spawnOnBlockType = readSettings(BO2Settings.SPAWN_ON_BLOCK_TYPE);
        this.collisionBlockType = readSettings(BO2Settings.COLLISTION_BLOCK_TYPE);

        this.spawnInBiome = readSettings(BO2Settings.SPAWN_IN_BIOME);

        this.spawnSunlight = readSettings(BO2Settings.SPAWN_SUNLIGHT);
        this.spawnDarkness = readSettings(BO2Settings.SPAWN_DARKNESS);
        this.spawnWater = readSettings(BO2Settings.SPAWN_WATER);
        this.spawnLava = readSettings(BO2Settings.SPAWN_LAVA);
        this.spawnAboveGround = readSettings(BO2Settings.SPAWN_ABOVE_GROUND);
        this.spawnUnderGround = readSettings(BO2Settings.SPAWN_UNDER_GROUND);

        this.underFill = readSettings(BO2Settings.UNDER_FILL);

        this.randomRotation = readSettings(BO2Settings.RANDON_ROTATION);
        this.dig = readSettings(BO2Settings.DIG);
        this.tree = readSettings(BO2Settings.TREE);
        this.branch = readSettings(BO2Settings.BRANCH);
        this.diggingBranch = readSettings(BO2Settings.DIGGING_BRANCH);
        this.needsFoundation = readSettings(BO2Settings.NEEDS_FOUNDATION);
        this.rarity = readSettings(BO2Settings.RARITY);
        this.collisionPercentage = readSettings(BO2Settings.COLLISION_PERCENTAGE);
        this.spawnElevationMin = readSettings(BO2Settings.SPAWN_ELEVATION_MIN);
        this.spawnElevationMax = readSettings(BO2Settings.SPAWN_ELEVATION_MAX);

        this.groupFrequencyMin = readSettings(BO2Settings.GROUP_FREQUENCY_MIN);
        this.groupFrequencyMax = readSettings(BO2Settings.GROUP_FREQUENCY_MAX);
        this.groupSeparationMin = readSettings(BO2Settings.GROUP_SEPERATION_MIN);
        this.groupSeparationMax = readSettings(BO2Settings.GROUP_SEPERATION_MAX);
        // >> Is this not used anymore? Netbeans finds no references to it
        // >> Nothing other than this line references BO2Settings.groupId
        // either...
        this.groupId = readSettings(BO2Settings.GROUP_ID);

        this.branchLimit = readSettings(BO2Settings.BRANCH_LIMIT);

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

        for (Entry<String, String> line : reader.getRawSettings())
        {
            ObjectCoordinate buffer = ObjectCoordinate.getCoordinateFromString(line.getKey(), line.getValue());
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

    @Override
    public boolean hasPreferenceToSpawnIn(LocalBiome biome)
    {
        return spawnInBiome.contains(biome.getName()) || spawnInBiome.contains("All");
    }

}
