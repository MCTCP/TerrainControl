package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.configuration.io.SettingsMap;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.Rotation;

import java.util.Map;
import java.util.Random;

/**
 * UseWorld is a keyword that spawns the objects in the WorldObjects folder.
 */
public class UseWorld extends SimpleObject
{
    @Override
    public void onEnable(Map<String, CustomObject> otherObjectsInDirectory)
    {
        // Stub method
    }

    @Override
    public String getName()
    {
        return "UseWorld";
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
        for (CustomObject object : world.getConfigs().getCustomObjects())
        {
            if (object.hasPreferenceToSpawnIn(world.getBiome(x, z)))
            {
                if (object.spawnForced(world, random, rotation, x, y, z))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean process(LocalWorld world, Random rand, ChunkCoordinate chunkCoord)
    {
        // Pick one object, try to spawn that, if that fails, try with another
        // object, as long as the objectSpawnRatio cap isn't reached.

        WorldConfig worldConfig = world.getConfigs().getWorldConfig();
        CustomObjectCollection customObjects = world.getConfigs().getCustomObjects();

        if (customObjects.isEmpty())
            return false;

        boolean objectSpawned = false;
        int spawnattemps = 0;
        while (!objectSpawned)
        {
            if (spawnattemps > worldConfig.objectSpawnRatio)
                return false;

            spawnattemps++;

            CustomObject selectedObject = customObjects.getRandomObject(rand);

            if (!selectedObject.hasPreferenceToSpawnIn(world.getBiome(chunkCoord.getBlockXCenter(), chunkCoord.getBlockZCenter())))
                continue;

            // Process the object
            objectSpawned = selectedObject.process(world, rand, chunkCoord);

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
        // Never, ever spawn this in UseWorld. It would cause an infinite loop.
        return false;
    }

    @Override
    public boolean canSpawnAt(LocalWorld world, Rotation rotation, int x, int y, int z)
    {
        return true; // We can only guess
    }

    @Override
    public boolean canRotateRandomly()
    {
        return true;
    }

}
