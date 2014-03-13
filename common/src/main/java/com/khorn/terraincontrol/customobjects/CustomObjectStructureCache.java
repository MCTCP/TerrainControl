package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.generator.resource.CustomStructureGen;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.helpers.RandomHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Each world has a cache of unfinished structures. This class is the cache.
 *
 */
public class CustomObjectStructureCache
{
    private Map<ChunkCoordinate, CustomObjectStructure> structureCache;
    private LocalWorld world;

    public CustomObjectStructureCache(LocalWorld world)
    {
        this.world = world;
        this.structureCache = new HashMap<ChunkCoordinate, CustomObjectStructure>();
    }

    public void reload(LocalWorld world)
    {
        this.world = world;
        structureCache.clear();
    }

    public CustomObjectStructure getStructureStart(int chunkX, int chunkZ)
    {
        ChunkCoordinate coord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
        CustomObjectStructure structureStart = structureCache.get(coord);

        // Clear cache if needed
        if (structureCache.size() > 400)
        {
            structureCache.clear();
        }

        if (structureStart != null)
        {
            return structureStart;
        }
        // No structure found, create one
        Random random = RandomHelper.getRandomForCoords(chunkX ^ 2, (chunkZ + 1) * 2, world.getSeed());
        CustomStructureGen structureGen = world.getCalculatedBiome(chunkX * 16 + 15, chunkZ * 16 + 15).getBiomeConfig().structureGen;
        if (structureGen != null)
        {
            CustomObjectCoordinate customObject = structureGen.getRandomObjectCoordinate(random, chunkX, chunkZ);
            if (customObject != null)
            {
                structureStart = new CustomObjectStructure(world, customObject);
                structureCache.put(coord, structureStart);
                return structureStart;
            } // TODO Maybe also store that no structure was here?
        }

        return null;
    }
}
