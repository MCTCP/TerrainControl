package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.customobjects.*;
import com.khorn.terraincontrol.customobjects.CustomObjectCoordinate.SpawnHeight;
import com.khorn.terraincontrol.customobjects.bo3.BO3Settings.OutsideSourceBlock;
import com.khorn.terraincontrol.customobjects.bo3.BO3Settings.SpawnHeightSetting;
import com.khorn.terraincontrol.util.MathHelper;

import java.io.File;
import java.util.Map;
import java.util.Random;

public class BO3 implements StructuredCustomObject
{
    private BO3Config settings;
    private String name;
    private File file;

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
        this.settings = new BO3Config(name, file, otherObjectsInDirectory);
    }

    /**
     * Creates a BO3 with the specified settings. Ignores the settings in the
     * settings file.
     *
     * @param oldObject     The object where this object is based on
     * @param extraSettings The settings to override
     */
    public BO3(BO3 oldObject, Map<String, String> extraSettings)
    {
        this.settings = new BO3Config(oldObject, extraSettings);
        this.name = settings.name;
        this.file = settings.file;
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
            if (world.getTypeId(x + block.x, y + block.y, z + block.z) != settings.sourceBlock)
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
            int previousBlock = world.getTypeId(x + block.x, y + block.y, z + block.z);
            if (previousBlock == settings.sourceBlock || settings.outsideSourceBlock == OutsideSourceBlock.placeAnyway)
            {
                block.spawn(world, random, x + block.x, y + block.y, z + block.z);
            }
        }
        return true;
    }

    @Override
    public boolean spawn(LocalWorld world, Random random, int x, int z)
    {
        Rotation rotation = settings.rotateRandomly ? Rotation.getRandomRotation(random) : Rotation.NORTH;
        int y = 0;
        if (settings.spawnHeight == SpawnHeightSetting.randomY)
        {
            y = MathHelper.getRandomNumberInRange(random, settings.minHeight, settings.maxHeight);
        }
        if (settings.spawnHeight == SpawnHeightSetting.highestBlock)
        {
            y = world.getHighestBlockYAt(x, z);
            if (y < settings.minHeight || y > settings.maxHeight)
            {
                return false;
            }
        }
        if (settings.spawnHeight == SpawnHeightSetting.highestSolidBlock)
        {
            y = world.getSolidHeight(x, z);
            if (y < settings.minHeight || y > settings.maxHeight)
            {
                return false;
            }
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
    public boolean process(LocalWorld world, Random random, int chunkX, int chunkZ)
    {
        boolean atLeastOneObjectHasSpawned = false;

        int chunkMiddleX = chunkX * 16 + 8;
        int chunkMiddleZ = chunkZ * 16 + 8;
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
    public CustomObject applySettings(Map<String, String> extraSettings)
    {
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
    public SpawnHeight getSpawnHeight()
    {
        return this.settings.spawnHeight.toSpawnHeight();
    }
}
