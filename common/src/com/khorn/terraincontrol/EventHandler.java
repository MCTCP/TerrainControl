package com.khorn.terraincontrol;

import com.khorn.terraincontrol.customobjects.CustomObject;

/**
 * Inherit this class, override methods as necessary and register it with
 * TerrainControl.registerEventHander(..). If you want to use the onStart(),
 * make sure that it is registered before TerrainControl is started.
 * 
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
     * Called whenever a custom object successfully spawns.
     * 
     * It is up to the implementation of the CustomObject to fire this
     * event. UseWorld and UseBiome won't fire this event, but BO2 and BO3 will.
     * 
     * This event should NOT be used to modify data in the object or the world.
     * 
     * @param object
     *            The object that is about to spawn.
     * @param world
     *            The world where it will spawn.
     * @param x
     *            The x of the object origin.
     * @param y
     *            The y of the object origin.
     * @param z
     *            The z of the object origin.
     */
    public void onCustomObjectSpawn(CustomObject object, LocalWorld world, int x, int y, int z)
    {

    }
}
