package com.khorn.terraincontrol;

import com.khorn.terraincontrol.biomegenerators.BiomeModeManager;
import com.khorn.terraincontrol.configuration.ConfigFunctionsManager;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.CustomObjectLoader;
import com.khorn.terraincontrol.customobjects.CustomObjectManager;
import com.khorn.terraincontrol.events.EventHandler;
import com.khorn.terraincontrol.events.EventPriority;
import com.khorn.terraincontrol.generator.resourcegens.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class TerrainControl
{
    /**
     * The world height that the engine supports. Not the actual height the
     * world is capped at. 256 in Minecraft.
     */
    public static int worldHeight = 256;

    /**
     * The world depth that the engine supports. Not the actual depth the world
     * is capped at. 0 in Minecraft.
     */
    public static int worldDepth = 0;

    /**
     * The maximum block id that is supported. 255 on CraftBukkit.
     */
    public static int supportedBlockIds = 255;

    private static TerrainControlEngine engine;
    private static ConfigFunctionsManager configFunctionsManager;
    private static CustomObjectManager customObjectManager;
    private static BiomeModeManager biomeManagers;

    private static List<EventHandler> cancelableEventHandlers = new ArrayList<EventHandler>();
    private static List<EventHandler> monitoringEventHandlers = new ArrayList<EventHandler>();

    private TerrainControl()
    {
        // Forbidden to instantiate.
    }

    /**
     * Starts the engine, making all API methods available.
     *
     * @param engine The implementation of the engine.
     */
    public static void startEngine(TerrainControlEngine engine)
    {
        if (TerrainControl.engine != null)
        {
            throw new UnsupportedOperationException("Engine is already set!");
        }

        // Start the engine
        TerrainControl.engine = engine;
        configFunctionsManager = new ConfigFunctionsManager();
        customObjectManager = new CustomObjectManager();
        biomeManagers = new BiomeModeManager();

        // Fire start event
        for (EventHandler handler : cancelableEventHandlers)
        {
            handler.onStart();
        }
        for (EventHandler handler : monitoringEventHandlers)
        {
            handler.onStart();
        }

        // Load global objects after the event has been fired, so that custom
        // object types are also taken into account
        customObjectManager.loadGlobalObjects();
    }

    /**
     * Null out static references to free up memory. Should be called on
     * shutdown.
     */
    public static void stopEngine()
    {
        // Shutdown all loaders
        for (CustomObjectLoader loader : customObjectManager.loaders.values())
        {
            loader.onShutdown();
        }

        engine = null;
        customObjectManager = null;
        configFunctionsManager = null;
        biomeManagers = null;
        cancelableEventHandlers.clear();
        monitoringEventHandlers.clear();
    }

    /**
     * Returns the engine, containing the API methods.
     *
     * @return The engine
     */
    public static TerrainControlEngine getEngine()
    {
        return engine;
    }

    /**
     * Returns the world object with the given name.
     *
     * @param name The name of the world.
     * @return The world object.
     */
    public static LocalWorld getWorld(String name)
    {
        return engine.getWorld(name);
    }

    /**
     * Convienence method to quickly get the biome name at the given
     * coordinates. Will return null if the world isn't loaded by Terrain
     * Control.
     *
     * @param worldName The world name.
     * @param x         The block x in the world.
     * @param z         The block z in the world.
     * @return The biome name, or null if the world isn't managed by Terrain
     *         Control.
     */
    public static String getBiomeName(String worldName, int x, int z)
    {
        LocalWorld world = getWorld(worldName);
        if (world == null)
        {
            // World isn't loaded by Terrain Control
            return null;
        }
        return world.getBiome(x, z).getName();
    }

    /**
     * Logs the message(s) with normal importance. Message will be prefixed with
     * TerrainControl, so don't do that yourself.
     *
     * @param messages The messages to log.
     */
    public static void log(String... messages)
    {
        engine.log(Level.INFO, messages);
    }

    /**
     * Logs the message(s) with the given importance. Message will be prefixed
     * with TerrainControl, so don't do that yourself.
     *
     * @param messages The messages to log.
     */
    public static void log(Level level, String... messages)
    {
        engine.log(level, messages);
    }

    /**
     * Returns the CustomObject manager, with hooks to spawn CustomObjects.
     *
     * @return The CustomObject manager.
     */
    public static CustomObjectManager getCustomObjectManager()
    {
        return customObjectManager;
    }

    /**
     * Returns the Resource manager.
     *
     * @return The Resource manager.
     */
    public static ConfigFunctionsManager getConfigFunctionsManager()
    {
        return configFunctionsManager;
    }
    
    /**
     * Returns the biome managers. Register your own biome manager here.
     * 
     * @return The biome managers.
     */
    public static BiomeModeManager getBiomeModeManager()
    {
        return biomeManagers;
    }

    // Events

    /**
     * Register your event handler here with normal priority. You can do this
     * before TerrainControl is started.
     *
     * @param handler The handler that will receive the events.
     */
    public static void registerEventHandler(EventHandler handler)
    {
        cancelableEventHandlers.add(handler);
    }

    /**
     * Register you event handler here with the given priority. You can do this
     * before TerrainControl is started.
     *
     * @param handler  The handler that will receive the events.
     * @param priority The priority of the event.
     */
    public static void registerEventHandler(EventHandler handler, EventPriority priority)
    {
        if (priority == EventPriority.CANCELABLE)
        {
            cancelableEventHandlers.add(handler);
        } else
        {
            monitoringEventHandlers.add(handler);
        }
    }

    // Firing events
    // All methods first call the cancelableEventHandlers, and then the monitoringEventHandlers.
    // Only cancelableEventHandlers can cancel events.
    // Cancelled events are still fired.

    public static boolean fireCanCustomObjectSpawnEvent(CustomObject object, LocalWorld world, int x, int y, int z)
    {
        boolean success = true;
        for (EventHandler handler : cancelableEventHandlers)
        {
            if (!handler.canCustomObjectSpawn(object, world, x, y, z, !success))
            {
                success = false;
            }
        }
        for (EventHandler handler : monitoringEventHandlers)
        {
            handler.canCustomObjectSpawn(object, world, x, y, z, !success);
        }
        return success;
    }

    public static boolean fireResourceProcessEvent(Resource resource, LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ)
    {
        boolean success = true;
        for (EventHandler handler : cancelableEventHandlers)
        {
            if (!handler.onResourceProcess(resource, world, random, villageInChunk, chunkX, chunkZ, !success))
            {
                success = false;
            }
        }
        for (EventHandler handler : monitoringEventHandlers)
        {
            handler.onResourceProcess(resource, world, random, villageInChunk, chunkX, chunkZ, !success);
        }
        return success;
    }

    public static void firePopulationStartEvent(LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ)
    {
        for (EventHandler handler : cancelableEventHandlers)
            handler.onPopulateStart(world, random, villageInChunk, chunkX, chunkZ);
        for (EventHandler handler : monitoringEventHandlers)
            handler.onPopulateStart(world, random, villageInChunk, chunkX, chunkZ);
    }

    public static void firePopulationEndEvent(LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ)
    {
        for (EventHandler handler : cancelableEventHandlers)
            handler.onPopulateEnd(world, random, villageInChunk, chunkX, chunkZ);
        for (EventHandler handler : monitoringEventHandlers)
            handler.onPopulateEnd(world, random, villageInChunk, chunkX, chunkZ);
    }

}
