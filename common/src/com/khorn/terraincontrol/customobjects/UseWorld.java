package com.khorn.terraincontrol.customobjects;

import java.util.Map;
import java.util.Random;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.WorldConfig;

/**
 * UseWorld is a keyword that spawns the objects in the WorldObjects folder.
 * 
 */
public class UseWorld implements CustomObject
{

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
    public boolean spawn(LocalWorld world, Random random, int x, int y, int z)
    {
        for (CustomObject object : world.getSettings().customObjects)
        {
            if (object.hasPreferenceToSpawnIn(world.getBiome(x, z)))
            {
                if (object.spawn(world, random, x, y, z))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean spawnAsTree(LocalWorld world, Random random, int x, int y, int z)
    {
        for (CustomObject object : world.getSettings().customObjects)
        {
            if (object.hasPreferenceToSpawnIn(world.getBiome(x, z)))
            {
                if (object.spawnAsTree(world, random, x, y, z))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean spawn(LocalWorld world, Random random, int x, int z)
    {
        for (CustomObject object : world.getSettings().customObjects)
        {
            if (object.hasPreferenceToSpawnIn(world.getBiome(x, z)))
            {

                if (object.spawn(world, random, x, z))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean spawnAsTree(LocalWorld world, Random random, int x, int z)
    {
        for (CustomObject object : world.getSettings().customObjects)
        {
            if (object.hasPreferenceToSpawnIn(world.getBiome(x, z)))
            {
                if (object.spawnAsTree(world, random, x, z))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean process(LocalWorld world, Random rand, int chunk_x, int chunk_z)
    {
        
        
        WorldConfig worldSettings = world.getSettings();

        if (worldSettings.customObjects.size() == 0)
            return false;
        
        boolean objectSpawned = false;
        int spawnattemps = 0;
        while (!objectSpawned)
        {
            if (spawnattemps > worldSettings.objectSpawnRatio)
                return false;

            spawnattemps++;

            CustomObject SelectedObject = worldSettings.customObjects.get(rand.nextInt(worldSettings.customObjects.size()));
            
            if (!SelectedObject.hasPreferenceToSpawnIn(world.getBiome(chunk_x * 16 + 8, chunk_z * 16 + 8)))
                continue;

            
            // Process the object
            
            objectSpawned = SelectedObject.process(world, rand, chunk_x, chunk_z);

        }
        return objectSpawned;
    }

    @Override
    public boolean processAsTree(LocalWorld world, Random rand, int chunk_x, int chunk_z)
    {
        WorldConfig worldSettings = world.getSettings();

        if (worldSettings.customObjects.size() == 0)
            return false;

        boolean objectSpawned = false;
        int spawnattemps = 0;
        while (!objectSpawned)
        {
            if (spawnattemps > worldSettings.objectSpawnRatio)
                return false;

            spawnattemps++;

            CustomObject SelectedObject = worldSettings.customObjects.get(rand.nextInt(worldSettings.customObjects.size()));

            if (!SelectedObject.hasPreferenceToSpawnIn(world.getBiome(chunk_x * 16 + 8, chunk_z * 16 + 8)))
                continue;

            // Process the object
            objectSpawned = SelectedObject.processAsTree(world, rand, chunk_x, chunk_z);

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
        // Never, ever spawn this in UseWorld. It would cause an infinite loop.
        return false;
    }

}
