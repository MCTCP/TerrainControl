package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalWorld;

import java.util.Random;

public class UseBiomeAll extends UseBiome
{
    @Override
    public String getName()
    {
        return "UseBiomeAll";
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
        boolean spawnedAtLeastOneObject = false;

        for (CustomObject object : getPossibleObjectsAt(world, chunkX * 16 + 8, chunkZ * 16 + 8))
        {
            if (object.process(world, random, chunkX, chunkZ))
            {
                spawnedAtLeastOneObject = true;
            }
        }

        return spawnedAtLeastOneObject;
    }
}
