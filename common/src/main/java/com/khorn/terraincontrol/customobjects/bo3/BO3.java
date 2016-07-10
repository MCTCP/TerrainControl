package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.io.FileSettingsReader;
import com.khorn.terraincontrol.configuration.io.FileSettingsWriter;
import com.khorn.terraincontrol.configuration.io.SettingsMap;
import com.khorn.terraincontrol.customobjects.*;
import com.khorn.terraincontrol.customobjects.bo3.BO3Settings.OutsideSourceBlock;
import com.khorn.terraincontrol.customobjects.bo3.BO3Settings.SpawnHeightEnum;
import com.khorn.terraincontrol.util.BoundingBox;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.Rotation;
import com.khorn.terraincontrol.util.helpers.MathHelper;
import com.khorn.terraincontrol.util.helpers.RandomHelper;

import java.io.File;
import java.util.Map;
import java.util.Random;

public class BO3 implements CustomObject
{
    private BO3Config settings;
    private final String name;
    private final File file;

    /**
     * Creates a BO3 from a file.
     *
     * @param name Name of the BO3.
     * @param file File of the BO3. If the file does not exist, a BO3 with the default settings is created.
     */
    public BO3(String name, File file)
    {
        this.name = name;
        this.file = file;
    }

    /**
     * Creates a BO3 with the specified settings. Ignores the settings in the settings file.
     *
     * @param oldObject The object where this object is based on
     * @param settings  The settings to use instead.
     */
    public BO3(BO3 oldObject, SettingsMap settings)
    {
        this.name = oldObject.name;
        this.file = oldObject.file;
        this.settings = new BO3Config(settings, file.getParentFile(), oldObject.settings.otherObjects);
    }

    @Override
    public void onEnable(Map<String, CustomObject> otherObjectsInDirectory)
    {
        this.settings = new BO3Config(FileSettingsReader.read(name, file), file.getParentFile(), otherObjectsInDirectory);
        FileSettingsWriter.writeToFile(this.settings.getSettingsAsMap(), file, this.settings.settingsMode);
    }

    /**
     * Computes the offset and variance for spawning a bo3
     *
     * @param random   Random number generator.
     * @param offset   Base spawn offset.
     * @param variance Max variance from this offset.
     *
     * @return The sum of the offset and variance.
     */
    public int getOffsetAndVariance(Random random, int offset, int variance)
    {
        if (variance == 0)
        {
            return offset;
        } else if (variance < 0)
        {
            variance = -random.nextInt(MathHelper.abs(variance) + 1);
        } else
        {
            variance = random.nextInt(variance + 1);
        }
        return MathHelper.clamp(offset + variance, TerrainControl.WORLD_DEPTH, TerrainControl.WORLD_HEIGHT);
    }

    @Override
    public String getName()
    {
        return name;
    }

    /**
     * Gets the file the BO3 is stored in.
     * @return The file.
     */
    public File getFile()
    {
        return file;
    }

    public BO3Config getSettings()
    {
        return settings;
    }

    @Override
    public boolean canSpawnAsTree()
    {
        return settings.tree;
    }

    @Override
    public boolean canSpawnAsObject()
    {
        return true;
    }

    @Override
    public boolean canSpawnAt(LocalWorld world, Rotation rotation, int x, int y, int z)
    {
        BO3PlaceableFunction[] blocks = settings.blocks[rotation.getRotationId()];
        BO3Check[] checks = settings.bo3Checks[rotation.getRotationId()];

        // Height check
        if (y < settings.minHeight || y > settings.maxHeight)
        {
            return false;
        }

        // Check for spawning
        for (BO3Check check : checks)
        {
            if (check.preventsSpawn(world, x + check.x, y + check.y, z + check.z))
            {
                // A check failed
                return false;
            }
        }

        // Check for source blocks
        int blocksOutsideSourceBlock = 0;
        for (BO3PlaceableFunction block : blocks)
        {
            if (!world.isLoaded(x + block.x, y + block.y, z + block.z))
            {
                // Cannot spawn BO3, part of world is not loaded
                return false;
            }
            if (!settings.sourceBlocks.contains(world.getMaterial(x + block.x, y + block.y, z + block.z)))
            {
                blocksOutsideSourceBlock++;
            }
        }
        if ((((double) blocksOutsideSourceBlock / (double) blocks.length) * 100.0) > settings.maxPercentageOutsideSourceBlock)
        {
            // Too many blocks outside source block
            return false;
        }

        // Call event
        if (!TerrainControl.fireCanCustomObjectSpawnEvent(this, world, x, y, z))
        {
            // Cancelled
            return false;
        }

        // Can most likely spawn here
        return true;
    }

    @Override
    public boolean canRotateRandomly()
    {
        return settings.rotateRandomly;
    }

    @Override
    public boolean spawnForced(LocalWorld world, Random random, Rotation rotation, int x, int y, int z)
    {
        BO3PlaceableFunction[] blocks = settings.blocks[rotation.getRotationId()];
        ObjectExtrusionHelper oeh = new ObjectExtrusionHelper(settings.extrudeMode, settings.extrudeThroughBlocks);
        // Spawn

        for (BO3PlaceableFunction block : blocks)
        {
            if (settings.outsideSourceBlock == OutsideSourceBlock.placeAnyway || settings.sourceBlocks.contains(world.getMaterial(x + block.x, y + block.y, z + block.z)))
            {
                block.spawn(world, random, x + block.x, y + block.y, z + block.z);
            }
            if (block instanceof BlockFunction)
            {
                oeh.addBlock((BlockFunction) block);
            }
        }
        oeh.extrude(world, random, x, y, z);
        return true;
    }

    protected boolean spawn(LocalWorld world, Random random, int x, int z)
    {
        Rotation rotation = settings.rotateRandomly ? Rotation.getRandomRotation(random) : Rotation.NORTH;
        int y = 0;
        if (settings.spawnHeight == SpawnHeightEnum.atMinY)
        {
            y = settings.minHeight;
        }
        if (settings.spawnHeight == SpawnHeightEnum.randomY)
        {
            y = RandomHelper.numberInRange(random, settings.minHeight, settings.maxHeight);
        }
        if (settings.spawnHeight == SpawnHeightEnum.highestBlock)
        {
            y = world.getHighestBlockYAt(x, z);
        }
        if (settings.spawnHeight == SpawnHeightEnum.highestSolidBlock)
        {
            y = world.getSolidHeight(x, z);
        }
        // Offset by static and random settings values
        y += this.getOffsetAndVariance(random, settings.spawnHeightOffset, settings.spawnHeightVariance);
        if (!canSpawnAt(world, rotation, x, y, z))
        {
            return false;
        }
        return spawnForced(world, random, rotation, x, y, z);
    }

    @Override
    public boolean process(LocalWorld world, Random random, ChunkCoordinate chunkCoord)
    {
        boolean atLeastOneObjectHasSpawned = false;

        int chunkMiddleX = chunkCoord.getBlockXCenter();
        int chunkMiddleZ = chunkCoord.getBlockZCenter();
        for (int i = 0; i < settings.frequency; i++)
        {
            if (settings.rarity > random.nextDouble() * 100.0)
            {
                if (spawn(world, random, chunkMiddleX + random.nextInt(16), chunkMiddleZ + random.nextInt(16)))
                {
                    atLeastOneObjectHasSpawned = true;
                }
            }
        }

        return atLeastOneObjectHasSpawned;
    }

    @Override
    public CustomObject applySettings(SettingsMap extraSettings)
    {
        extraSettings.setFallback(this.settings.getSettingsAsMap());
        return new BO3(this, extraSettings);
    }

    @Override
    public boolean hasPreferenceToSpawnIn(LocalBiome biome)
    {
        if (settings.excludedBiomes.contains("All") || settings.excludedBiomes.contains("all") || settings.excludedBiomes
                .contains(biome.getName()))
        {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasBranches()
    {
        return settings.branches[0].length != 0;
    }

    @Override
    public Branch[] getBranches(Rotation rotation)
    {
        return settings.branches[rotation.getRotationId()];
    }

    @Override
    public CustomObjectCoordinate makeCustomObjectCoordinate(Random random, int chunkX, int chunkZ)
    {
        if (settings.rarity > random.nextDouble() * 100.0)
        {
            Rotation rotation = settings.rotateRandomly ? Rotation.getRandomRotation(random) : Rotation.NORTH;
            int height = RandomHelper.numberInRange(random, settings.minHeight, settings.maxHeight);
            return new CustomObjectCoordinate(
                    this, rotation, chunkX * 16 + 8 + random.nextInt(16), height, chunkZ * 16 + 8 + random.nextInt(16)
            );
        }
        return null;
    }

    @Override
    public int getMaxBranchDepth()
    {
        return settings.maxBranchDepth;
    }

    @Override
    public StructurePartSpawnHeight getStructurePartSpawnHeight()
    {
        return this.settings.spawnHeight.toStructurePartSpawnHeight();
    }

    @Override
    public BoundingBox getBoundingBox(Rotation rotation)
    {
        return this.settings.boundingBoxes[rotation.getRotationId()];
    }
}
