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
    public void onEnable(Map<String, CustomObject> otherObjectsInDirectory)
    {
        // Stub method
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
    public boolean spawnForced(LocalWorld world, Random random, Rotation rotation, int x, int y, int z)
    {
        for (CustomObject object : getPossibleObjectsAt(world, x, z))
        {
            if (object.spawnForced(world, random, rotation, x, y, z))
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

        // Pick one object, try to spawn that, if that fails, try with another
        // object, as long as the objectSpawnRatio cap isn't reached.
        int objectSpawnRatio = world.getSettings().objectSpawnRatio;

        if (possibleObjects.size() == 0)
            return false;

        boolean objectSpawned = false;
        int spawnattemps = 0;
        while (!objectSpawned)
        {
            if (spawnattemps > objectSpawnRatio)
                return false;

            spawnattemps++;

            CustomObject selectedObject = possibleObjects.get(random.nextInt(possibleObjects.size()));

            // Process the object
            objectSpawned = selectedObject.process(world, random, chunkX, chunkZ);

        }
        return objectSpawned;
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

    @Override
    public boolean canSpawnAt(LocalWorld world, Rotation rotation, int x, int y, int z)
    {
        List<CustomObject> objects = getPossibleObjectsAt(world, x, z);
        if (objects.size() == 0)
        {
            // No objects to spawn
            return false;
        }
        // Check for all the object
        for (CustomObject object : objects)
        {
            if (!object.canSpawnAt(world, rotation, x, y, z))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canRotateRandomly()
    {
        return true;
    }
}
