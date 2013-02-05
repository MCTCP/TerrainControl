package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.WorldConfig;

import java.util.Random;

public class UseWorldAll extends UseWorld
{
    @Override
    public String getName()
    {
        return "UseWorldAll";
    }

    @Override
    public boolean process(LocalWorld world, Random rand, int chunkX, int chunkZ)
    {
        WorldConfig worldSettings = world.getSettings();
        boolean spawnedAtLeastOneObject = false;

        if (worldSettings.customObjects.size() == 0)
            return false;

        for (CustomObject selectedObject : worldSettings.customObjects)
        {
            if (!selectedObject.hasPreferenceToSpawnIn(world.getBiome(chunkX * 16 + 8, chunkZ * 16 + 8)))
                continue;

            // Process the object
            if (selectedObject.process(world, rand, chunkX, chunkZ))
            {
                spawnedAtLeastOneObject = true;
            }
        }
        return spawnedAtLeastOneObject;
    }
}
