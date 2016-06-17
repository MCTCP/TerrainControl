package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.util.ChunkCoordinate;

import java.util.Random;

public class UseWorldAll extends UseWorld
{
    @Override
    public String getName()
    {
        return "UseWorldAll";
    }

    @Override
    public boolean process(LocalWorld world, Random rand, ChunkCoordinate chunkCoord)
    {
        boolean spawnedAtLeastOneObject = false;

        for (CustomObject selectedObject : world.getConfigs().getCustomObjects())
        {
            if (!selectedObject.hasPreferenceToSpawnIn(world.getBiome(chunkCoord.getBlockXCenter(), chunkCoord.getBlockZCenter())))
                continue;

            // Process the object
            if (selectedObject.process(world, rand, chunkCoord))
            {
                spawnedAtLeastOneObject = true;
            }
        }
        return spawnedAtLeastOneObject;
    }
}
