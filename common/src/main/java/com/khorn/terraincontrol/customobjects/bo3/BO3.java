package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.io.FileSettingsReader;
import com.khorn.terraincontrol.configuration.io.FileSettingsWriter;
import com.khorn.terraincontrol.configuration.io.SettingsReader;
import com.khorn.terraincontrol.customobjects.*;
import com.khorn.terraincontrol.customobjects.bo3.BO3Settings.OutsideSourceBlock;
import com.khorn.terraincontrol.customobjects.bo3.BO3Settings.SpawnHeightEnum;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.Rotation;
import com.khorn.terraincontrol.util.helpers.MathHelper;

import java.io.File;
import java.util.Map;
import java.util.Random;

public class BO3 implements StructuredCustomObject
{
    private BO3Config settings;
    private final String name;
    private final File file;

    /**
     * Creates a BO3 from a file.
     *
     * @param name
     * @param file
     */
    public BO3(String name, File file)
    {
        this.name = name;
        this.file = file;
    }

    @Override
    public void onEnable(Map<String, CustomObject> otherObjectsInDirectory)
    {
        this.settings = new BO3Config(new FileSettingsReader(name, file), otherObjectsInDirectory);
    }

    /**
     * Creates a BO3 with the specified settings. Ignores the settings in the
     * settings file.
     *
     * @param oldObject     The object where this object is based on
     * @param extraSettings The settings to override
     */
    public BO3(BO3 oldObject, SettingsReader extraSettings)
    {
        this.settings = new BO3Config(extraSettings, oldObject.settings.otherObjectsInDirectory);
        FileSettingsWriter.writeToFile(this.settings, this.settings.settingsMode);
        this.name = settings.getName();
        this.file = settings.getFile();
    }

    @Override
    public String getName()
    {
        return name;
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
        BlockFunction[] blocks = settings.blocks[rotation.getRotationId()];
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
        for (BlockFunction block : blocks)
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
        BlockFunction[] blocks = settings.blocks[rotation.getRotationId()];

        // Spawn
        for (BlockFunction block : blocks)
        {
            if (settings.outsideSourceBlock == OutsideSourceBlock.placeAnyway || settings.sourceBlocks.contains(world.getMaterial(x + block.x, y + block.y, z + block.z)))
            {
                block.spawn(world, random, x + block.x, y + block.y, z + block.z);
            }
        }
        return true;
    }

    protected boolean spawn(LocalWorld world, Random random, int x, int z)
    {
        Rotation rotation = settings.rotateRandomly ? Rotation.getRandomRotation(random) : Rotation.NORTH;
        int y = 0;
        if (settings.spawnHeight == SpawnHeightEnum.randomY)
        {
            y = MathHelper.getRandomNumberInRange(random, settings.minHeight, settings.maxHeight);
        }
        if (settings.spawnHeight == SpawnHeightEnum.highestBlock)
        {
            y = world.getHighestBlockYAt(x, z);
        }
        if (settings.spawnHeight == SpawnHeightEnum.highestSolidBlock)
        {
            y = world.getSolidHeight(x, z);
        }
        if (!canSpawnAt(world, rotation, x, y, z))
        {
            return false;
        }
        return spawnForced(world, random, rotation, x, y, z);
    }

    @Override
    public boolean spawnAsTree(LocalWorld world, Random random, int x, int z)
    {
        if (settings.tree)
        {
            return spawn(world, random, x, z);
        }
        return false;
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
    public CustomObject applySettings(SettingsReader extraSettings)
    {
        extraSettings.setFallbackReader(this.settings.reader);
        return new BO3(this, extraSettings);
    }

    @Override
    public boolean hasPreferenceToSpawnIn(LocalBiome biome)
    {
        if (settings.excludedBiomes.contains("All") || settings.excludedBiomes.contains("all") || settings.excludedBiomes.contains(biome.getName()))
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
            int height = MathHelper.getRandomNumberInRange(random, settings.minHeight, settings.maxHeight);
            return new CustomObjectCoordinate(this, rotation, chunkX * 16 + 8 + random.nextInt(16), height, chunkZ * 16 + 8 + random.nextInt(16));
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
}
