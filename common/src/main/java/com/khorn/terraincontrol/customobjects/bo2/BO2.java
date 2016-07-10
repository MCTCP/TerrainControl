package com.khorn.terraincontrol.customobjects.bo2;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigFile;
import com.khorn.terraincontrol.configuration.io.FileSettingsReader;
import com.khorn.terraincontrol.configuration.io.RawSettingValue;
import com.khorn.terraincontrol.configuration.io.SettingsMap;
import com.khorn.terraincontrol.customobjects.Branch;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.CustomObjectCoordinate;
import com.khorn.terraincontrol.customobjects.StructurePartSpawnHeight;
import com.khorn.terraincontrol.util.BoundingBox;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.MaterialSet;
import com.khorn.terraincontrol.util.Rotation;
import com.khorn.terraincontrol.util.helpers.RandomHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The good old BO2.
 */
public class BO2 extends ConfigFile implements CustomObject
{
    /**
     * It's annoying that we have to keep all raw BO2 settings in memory. The
     * reason is that our config writing system doesn't support the BO2 syntax,
     * so we can't simply use the output of
     * {@link #writeConfigSettings(SettingsMap)}, as we do for other configs.
     */
    private final SettingsMap settingsMap;
    private final File file;

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

    /**
     * Creates a BO2 from a file.
     * @param objectName Name of the object.
     * @param file The file to read the settings from.
     */
    public BO2(String objectName, File file)
    {
        super(objectName);
        this.file = file;
        this.settingsMap = FileSettingsReader.read(objectName, file);
    }

    /**
     * Creates a BO2, ignoring the settings in the file.
     * @param settings The actual settings.
     * @param file The file that appears in {@link #getFile()}.
     */
    private BO2(SettingsMap settings, File file)
    {
        super(settings.getName());
        this.file = file;
        this.settingsMap = settings;
    }

    @Override
    public void onEnable(Map<String, CustomObject> otherObjectsInDirectory)
    {
        enable(settingsMap);
    }

    private void enable(SettingsMap settings)
    {
        readConfigSettings(settings);
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

    /**
     * Gets the file that this BO2 was read from.
     * @return The file.
     */
    public File getFile()
    {
        return file;
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
    public CustomObject applySettings(SettingsMap extraSettings)
    {
        extraSettings.setFallback(this.settingsMap);
        BO2 bo2WithSettings = new BO2(extraSettings, file);
        return bo2WithSettings;
    }

    @Override
    protected void writeConfigSettings(SettingsMap writer)
    {
        // It doesn't write.
    }

    @Override
    protected void readConfigSettings(SettingsMap reader)
    {
        this.version = reader.getSetting(BO2Settings.VERSION);

        this.spawnOnBlockType = reader.getSetting(BO2Settings.SPAWN_ON_BLOCK_TYPE);
        this.collisionBlockType = reader.getSetting(BO2Settings.COLLISTION_BLOCK_TYPE);

        this.spawnInBiome = reader.getSetting(BO2Settings.SPAWN_IN_BIOME);

        this.spawnSunlight = reader.getSetting(BO2Settings.SPAWN_SUNLIGHT);
        this.spawnDarkness = reader.getSetting(BO2Settings.SPAWN_DARKNESS);
        this.spawnWater = reader.getSetting(BO2Settings.SPAWN_WATER);
        this.spawnLava = reader.getSetting(BO2Settings.SPAWN_LAVA);
        this.spawnAboveGround = reader.getSetting(BO2Settings.SPAWN_ABOVE_GROUND);
        this.spawnUnderGround = reader.getSetting(BO2Settings.SPAWN_UNDER_GROUND);

        this.underFill = reader.getSetting(BO2Settings.UNDER_FILL);

        this.randomRotation = reader.getSetting(BO2Settings.RANDON_ROTATION);
        this.dig = reader.getSetting(BO2Settings.DIG);
        this.tree = reader.getSetting(BO2Settings.TREE);
        this.branch = reader.getSetting(BO2Settings.BRANCH);
        this.diggingBranch = reader.getSetting(BO2Settings.DIGGING_BRANCH);
        this.needsFoundation = reader.getSetting(BO2Settings.NEEDS_FOUNDATION);
        this.rarity = reader.getSetting(BO2Settings.RARITY);
        this.collisionPercentage = reader.getSetting(BO2Settings.COLLISION_PERCENTAGE);
        this.spawnElevationMin = reader.getSetting(BO2Settings.SPAWN_ELEVATION_MIN);
        this.spawnElevationMax = reader.getSetting(BO2Settings.SPAWN_ELEVATION_MAX);

        this.groupFrequencyMin = reader.getSetting(BO2Settings.GROUP_FREQUENCY_MIN);
        this.groupFrequencyMax = reader.getSetting(BO2Settings.GROUP_FREQUENCY_MAX);
        this.groupSeparationMin = reader.getSetting(BO2Settings.GROUP_SEPERATION_MIN);
        this.groupSeparationMax = reader.getSetting(BO2Settings.GROUP_SEPERATION_MAX);
        // >> Is this not used anymore? Netbeans finds no references to it
        // >> Nothing other than this line references BO2Settings.groupId
        // either...
        this.groupId = reader.getSetting(BO2Settings.GROUP_ID);

        this.branchLimit = reader.getSetting(BO2Settings.BRANCH_LIMIT);

        this.readCoordinates(reader);
    }

    @Override
    protected void correctSettings()
    {
        // Stub method
    }

    @Override
    protected void renameOldSettings(SettingsMap reader)
    {
        // Stub method
    }

    private void readCoordinates(SettingsMap reader)
    {
        ArrayList<ObjectCoordinate> coordinates = new ArrayList<ObjectCoordinate>();

        for (RawSettingValue line : reader.getRawSettings())
        {
            String[] lineSplit = line.getRawValue().split(":", 2);
            if (lineSplit.length != 2)
                continue;

            ObjectCoordinate buffer = ObjectCoordinate.getCoordinateFromString(lineSplit[0], lineSplit[1]);
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

    @Override
    public BoundingBox getBoundingBox(Rotation rotation)
    {
        // We just
        return BoundingBox.newEmptyBox();
    }

    @Override
    public Branch[] getBranches(Rotation rotation)
    {
        return new Branch[0];
    }

    @Override
    public int getMaxBranchDepth()
    {
        return 0;
    }

    @Override
    public StructurePartSpawnHeight getStructurePartSpawnHeight()
    {
        return StructurePartSpawnHeight.PROVIDED;
    }

    @Override
    public boolean hasBranches()
    {
        return false;
    }

    @Override
    public CustomObjectCoordinate makeCustomObjectCoordinate(Random random, int chunkX, int chunkZ)
    {
        if (rarity > random.nextDouble() * 100.0)
        {
            Rotation rotation = Rotation.getRandomRotation(random);
            int height = RandomHelper.numberInRange(random, this.spawnElevationMin, this.spawnElevationMax);
            return new CustomObjectCoordinate(this, rotation, chunkX * 16 + 8 + random.nextInt(16), height, chunkZ * 16 + 8 + random.nextInt(16));
        }
        return null;
    }

}
