package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * UseBiome is a keyword that spawns the objects in the BiomeConfig/BiomeObjects
 * setting.
 */
public class UseBiome implements CustomObject
{
    public ArrayList<CustomObject> getPossibleObjectsAt(LocalWorld world, int x, int z)
    {
        return world.getSettings().biomeConfigs[world.getBiome(x, z).getId()].biomeObjects;
    }

    @Override
    public String getName()
    {
        return "UseBiome";
    }

    @Override
    public boolean canSpawnAsTree()
    {
        return true;
    }

    @Override
    public boolean canSpawnAsObject()
    {
        return true;
    }

    @Override
    public boolean spawn(LocalWorld world, Random random, int x, int y, int z)
    {
        for (CustomObject object : getPossibleObjectsAt(world, x, z))
        {
            if (object.spawn(world, random, x, y, z))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean spawnAsTree(LocalWorld world, Random random, int x, int y, int z)
    {
        for (CustomObject object : getPossibleObjectsAt(world, x, z))
        {
            if (object.spawnAsTree(world, random, x, y, z))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean spawn(LocalWorld world, Random random, int x, int z)
    {
        for (CustomObject object : getPossibleObjectsAt(world, x, z))
        {
            if (object.spawn(world, random, x, z))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean spawnAsTree(LocalWorld world, Random random, int x, int z)
    {
        for (CustomObject object : getPossibleObjectsAt(world, x, z))
        {
            if (object.spawnAsTree(world, random, x, z))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean process(LocalWorld world, Random random, int chunkX, int chunkZ)
    {
        List<CustomObject> possibleObjects = getPossibleObjectsAt(world, chunkX * 16 + 8, chunkZ * 16 + 8);
        if (possibleObjects.size() == 0)
        {
            return false;
        }
        CustomObject object = possibleObjects.get(random.nextInt(possibleObjects.size()));
        return object.process(world, random, chunkX, chunkZ);
    }

    @Override
    public boolean processAsTree(LocalWorld world, Random random, int chunkX, int chunkZ)
    {
        List<CustomObject> possibleObjects = getPossibleObjectsAt(world, chunkX * 16 + 8, chunkZ * 16 + 8);
        if (possibleObjects.size() == 0)
        {
            return false;
        }
        CustomObject object = possibleObjects.get(random.nextInt(possibleObjects.size()));
        return object.processAsTree(world, random, chunkX, chunkZ);
    }

    @Override
    public CustomObject applySettings(Map<String, String> settings)
    {
        // Not supported
        return this;
    }

    @Override
    public boolean hasPreferenceToSpawnIn(LocalBiome biome)
    {
        // Never, ever spawn this with UseWorld.
        return false;
    }

}
