package com.khorn.terraincontrol.events;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.generator.resource.Resource;

import java.util.Random;

/**
 * Inherit this class, override methods as necessary and register it with
 * TerrainControl.registerEventHander(..). If you want to use the onStart(),
 * make sure that it is registered before TerrainControl is started.
 */
public abstract class EventHandler
{
    /**
     * Called when it's time to register the custom resources and objects.
     */
    public void onStart()
    {

    }

    /**
     * Called whenever a check needs to be run to spawn an object. Always 
     * called as the last check. You can be (almost) sure that an object
     * will spawn after this, so it can be used to log CustomObject spawn.
     * <p/>
     * It is up to the implementation of the CustomObject to fire this event.
     * UseWorld and UseBiome won't fire this event, but BO2 and BO3 will.
     * <p/>
     * This event should NOT be used to modify data in the object or the world.
     * However, you can cancel the event by returning false.
     *
     * @param object The object that is about to spawn.
     * @param world  The world where it will spawn.
     * @param x      The x of the object origin.
     * @param y      The y of the object origin.
     * @param z      The z of the object origin.
     * @return Whether the event should be cancelled. You cannot "uncancel"
     *         events, so returning true when the event is already cancelled
     *         does nothing.
     */
    public boolean canCustomObjectSpawn(CustomObject object, LocalWorld world, int x, int y, int z, boolean isCancelled)
    {
        return true;
    }

    /**
     * Called whenever a resource is processed for the chunk.
     *
     * @param resource       The resource that is about to spawn.
     * @param world          The world the resource will spawn in.
     * @param random         The random generator.
     * @param villageInChunk Whether there is a village in the current chunk.
     * @param chunkX         The x coordinate of the chunk the resource will spawn in.
     * @param chunkZ         The z coordinate of the chunk the resource will spawn in.
     * @return Whether the event should be cancelled. You cannot "uncancel"
     *         events, so returning true when the event is already cancelled
     *         does nothing.
     */
    public boolean onResourceProcess(Resource resource, LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ, boolean isCancelled)
    {
        return true;
    }

    /**
     * Called whenever Terrain Control starts to populate a chunk. Cannot be
     * cancelled.
     *
     * @param world          The world where the population occurred.
     * @param random         The random generator.
     * @param villageInChunk Whether there is a village in the current chunk.
     * @param chunkX         The x coordinate of the chunk that is being populated.
     * @param chunkZ         The z coordinate of the chunk that is being populated.
     */
    public void onPopulateStart(LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ)
    {

    }

    /**
     * Called whenever Terrain Control is done populating a chunk. Cannot be
     * cancelled.
     *
     * @param world          The world where the population occurred.
     * @param random         The random generator.
     * @param villageInChunk Whether there is a village in the current chunk.
     * @param chunkX         The x coordinate of the chunk that is being populated.
     * @param chunkZ         The z coordinate of the chunk that is being populated.
     */
    public void onPopulateEnd(LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ)
    {

    }
}
