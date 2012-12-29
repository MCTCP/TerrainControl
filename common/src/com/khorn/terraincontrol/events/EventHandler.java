package com.khorn.terraincontrol.events;


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
     */
    public void onCustomObjectSpawn(CustomObjectSpawnEvent event)
    {

    }

    /**
     * Called at the beginning and end of chunk population.
     */
	public void onPopulateEvent(PopulateEvent event)
	{

	}

    /**
     * Called before spawning a resource. Cancelling the event cancels the spawn.
     */
	public void onResourceEvent(ResourceEvent event)
	{

	}
}
