package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.bo3.BO3Settings.OutsideSourceBlock;
import com.khorn.terraincontrol.customobjects.bo3.BO3Settings.SpawnHeight;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BO3 implements CustomObject
{
    private BO3Config settings;

    /**
     * Creates a BO3 from a file.
     *
     * @param name
     * @param file
     */
    public BO3(String name, File file)
    {
        this.settings = new BO3Config(name, file);
    }

    /**
     * Creates a BO3 with the specified settings. Ignores the settings in the
     * settings file.
     *
     * @param name
     * @param file
     * @param settings
     */
    public BO3(String name, File file, Map<String, String> settings)
    {
        this.settings = new BO3Config(name, file, settings);
    }

    @Override
    public String getName()
    {
        return settings.name;
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
    public boolean spawn(LocalWorld world, Random random, int x, int y, int z)
    {
        BlockFunction[] blocks = settings.blocks[0];
        BO3Check[] checks = settings.bo3Checks[0];
        if (settings.rotateRandomly)
        {
            int rotation = random.nextInt(4);
            blocks = settings.blocks[rotation];
            checks = settings.bo3Checks[rotation];
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
        if (!TerrainControl.fireCustomObjectSpawnEvent(this, world, random, x, y, z))
        {
            // Cancelled
            return false;
        }

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
    public boolean spawnAsTree(LocalWorld world, Random random, int x, int y, int z)
    {
        if (settings.tree)
        {
            return spawn(world, random, x, y, z);
        }
        return false;
    }

    @Override
    public boolean spawn(LocalWorld world, Random random, int x, int z)
    {
        if (settings.spawnHeight == SpawnHeight.randomY)
        {
            return spawn(world, random, x, settings.minHeight + random.nextInt(settings.maxHeight), z);
        }
        if (settings.spawnHeight == SpawnHeight.highestBlock)
        {
            int y = world.getHighestBlockYAt(x, z);
            if (y < settings.minHeight || y > settings.maxHeight)
            {
                return false;
            }
            return spawn(world, random, x, y, z);
        }
        if (settings.spawnHeight == SpawnHeight.highestSolidBlock)
        {
            int y = world.getSolidHeight(x, z);
            if (y < settings.minHeight || y > settings.maxHeight)
            {
                return false;
            }
            return spawn(world, random, x, y, z);
        }
        return false;
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
            double test = random.nextDouble() * 100.0;
            if (settings.rarity > test)
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
    public boolean processAsTree(LocalWorld world, Random random, int chunkX, int chunkZ)
    {
        if (!settings.tree)
        {
            return false;
        }
        return process(world, random, chunkX, chunkZ);
    }

    @Override
    public CustomObject applySettings(Map<String, String> extraSettings)
    {
        Map<String, String> newSettings = new HashMap<String, String>();
        newSettings.putAll(settings.getSettingsCache());
        newSettings.putAll(extraSettings);
        return new BO3(getName(), settings.file, newSettings);
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
}
