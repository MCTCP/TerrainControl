package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.io.SettingsMap;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.Rotation;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * UseBiome is a keyword that spawns the objects in the BiomeConfig/BiomeObjects
 * setting.
 */
public class UseBiome extends SimpleObject
{
    public List<CustomObject> getPossibleObjectsAt(LocalWorld world, int x, int z)
    {
        return world.getBiome(x, z).getBiomeConfig().biomeObjects;
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
    public boolean process(LocalWorld world, Random random, ChunkCoordinate chunkCoord)
    {
        List<CustomObject> possibleObjects = getPossibleObjectsAt(world, chunkCoord.getBlockXCenter(), chunkCoord.getBlockZCenter());

        // Pick one object, try to spawn that, if that fails, try with another
        // object, as long as the objectSpawnRatio cap isn't reached.
        int objectSpawnRatio = world.getConfigs().getWorldConfig().objectSpawnRatio;

        if (possibleObjects.isEmpty())
            return false;

        boolean objectSpawned = false;
        int spawnAttempts = 0;
        while (!objectSpawned)
        {
            if (spawnAttempts > objectSpawnRatio)
                return false;

            spawnAttempts++;

            CustomObject selectedObject = possibleObjects.get(random.nextInt(possibleObjects.size()));

            // Process the object
            objectSpawned = selectedObject.process(world, random, chunkCoord);

        }
        return objectSpawned;
    }

    @Override
    public CustomObject applySettings(SettingsMap settings)
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
        if (objects.isEmpty())
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
