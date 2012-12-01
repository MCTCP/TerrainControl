package com.khorn.terraincontrol.customobjects;

import java.util.Map;
import java.util.Random;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;

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
        for(CustomObject object: world.getSettings().customObjects.values())
        {
            if(object.hasPreferenceToSpawnIn(world.getBiome(x, z)) && object.spawn(world, random, x, y, z))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean spawnAsTree(LocalWorld world, Random random, int x, int y, int z)
    {
        for(CustomObject object: world.getSettings().customObjects.values())
        {
            if(object.spawnAsTree(world, random, x, y, z))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean spawn(LocalWorld world, Random random, int x, int z)
    {
        for(CustomObject object: world.getSettings().customObjects.values())
        {
            if(object.spawn(world, random, x, z))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean spawnAsTree(LocalWorld world, Random random, int x, int z)
    {
        for(CustomObject object: world.getSettings().customObjects.values())
        {
            if(object.spawnAsTree(world, random, x, z))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void process(LocalWorld world, Random random, int chunkX, int chunkZ)
    {
        for(CustomObject object: world.getSettings().customObjects.values())
        {
            object.process(world, random, chunkX, chunkZ);
        }
    }

    @Override
    public void processAsTree(LocalWorld world, Random random, int chunkX, int chunkZ)
    {
        for(CustomObject object: world.getSettings().customObjects.values())
        {
            object.processAsTree(world, random, chunkX, chunkZ);
        }
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
