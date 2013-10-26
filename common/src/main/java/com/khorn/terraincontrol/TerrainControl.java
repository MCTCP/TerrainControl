package com.khorn.terraincontrol;

import com.khorn.terraincontrol.biomegenerators.BiomeModeManager;
import com.khorn.terraincontrol.configuration.ConfigFunctionsManager;
import com.khorn.terraincontrol.configuration.PluginConfig;
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
     * The world depth that the engine supports. Not the actual depth the 
     * world is capped at. 0 in Minecraft.
     */
    public static int worldDepth = 0;

    /**
     * The maximum block id that is supported. 255 on CraftBukkit.
     */
    public static int supportedBlockIds = 255;

    /**
     * Global TC plugin configs
     */
    private static PluginConfig pluginConfig;
    
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
     * {@link #setEngine(TerrainControlEngine)} needs to be called first.
     */
    public static void startEngine()
    {
        if (TerrainControl.engine == null)
        {
            throw new IllegalStateException("Engine is not set! Call setEngine first");
        }

        // Start the engine
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
     * shutdown. Engine can be restarted after this.
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
        pluginConfig = null;
        cancelableEventHandlers.clear();
        monitoringEventHandlers.clear();
    }

    /**
     * Returns the engine, containing the API methods.
     * <p/>
     * @return The engine
     */
    public static TerrainControlEngine getEngine()
    {
        return engine;
    }

    /**
     * Sets the engine and initializes the root TerrainControl directory
     * and global config. This is done to prevent logging NPE's later in
     * the plugin start sequence
     * <p/>
     * @param engine The engine.
     */
    public static void setEngine(TerrainControlEngine engine)
    {
        if (TerrainControl.engine != null)
        {
            throw new IllegalStateException("Engine is already set");
        }
        
        TerrainControl.engine = engine;
        TerrainControl.pluginConfig = new PluginConfig(engine.getTCDataFolder());
    }

    /**
     * Returns the world object with the given name.
     * <p/>
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
     * <p/>
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
     * Logs the message(s) with the given importance. Message will be
     * prefixed with [TerrainControl], so don't do that yourself.
     * <p/>
     * @param message The messages to log.
     * @param level   The severity of the message
     */
    public static void log(Level level, String... message)
    {
        engine.log(level, message);
    }

    /**
     * Logs a format string message with the given importance. Message will
     * be prefixed with [TerrainControl], so don't do that yourself.
     * <p/>
     * @param message The messages to log formatted similar to Logger.log()
     *                with the same args.
     * @param level   The severity of the message
     * @param param   The parameter belonging to {0} in the message string
     */
    public static void log(Level level, String message, Object param)
    {
        engine.log(level, message, param);
    }

    /**
     * Logs a format string message with the given importance. Message will
     * be prefixed with [TerrainControl], so don't do that yourself.
     * <p/>
     * @param message The messages to log formatted similar to Logger.log()
     *                with the same args.
     * @param level   The severity of the message
     * @param params  The parameters belonging to {0...} in the message
     *                string
     */
    public static void log(Level level, String message, Object[] params)
    {
        engine.log(level, message, params);
    }

    /**
     * Logs the message(s) with the given importance <b>ONLY IF</b> logger
     * level matches the level provided. Message will be prefixed with
     * [TerrainControl], so don't do that yourself.
     * <p/>
     * @param ifLevel  the Log level to test for
     * @param declared The Log level that will be shown to the user
     * @param messages The messages to log.
     */
    public static void logIfLevel(Level ifLevel, String... messages)
    {
        engine.logIfLevel(ifLevel, messages);
    }

    /**
     * Logs the message(s) with the given importance <b>ONLY IF</b> logger
     * level matches the level provided. Message will be prefixed with
     * [TerrainControl], so don't do that yourself.
     * <p/>
     * @param ifLevel  the Log level to test for
     * @param declared The Log level that will be shown to the user
     * @param message  The messages to log formatted similar to
     *                 Logger.log() with the same args.
     * @param params   The parameters belonging to {0...} in the message
     *                 string
     */
    public static void logIfLevel(Level ifLevel, String message, Object[] params)
    {
        engine.logIfLevel(ifLevel, message, params);
    }

    /**
     * Logs the message(s) with the given importance <b>ONLY IF</b> logger
     * level is between the min/max provided. Message will be prefixed with
     * [TerrainControl], so don't do that yourself.
     * <p/>
     * @param min      The minimum Log level to test for
     * @param max      The maximum Log level to test for
     * @param declared The Log level that will be shown to the user
     * @param messages The messages to log.
     */
    public static void logIfLevel(Level min, Level max, String... messages)
    {
        engine.logIfLevel(min, max, messages);
    }

    /**
     * Logs the message(s) with the given importance <b>ONLY IF</b> logger
     * level is between the min/max provided. Message will be prefixed with
     * [TerrainControl], so don't do that yourself.
     * <p/>
     * @param min      The minimum Log level to test for
     * @param max      The maximum Log level to test for
     * @param declared The Log level that will be shown to the user
     * @param message  The messages to log formatted similar to
     *                 Logger.log() with the same args.
     * @param params   The parameters belonging to {0...} in the message
     *                 string
     */
    public static void logIfLevel(Level min, Level max, String message, Object[] params)
    {
        engine.logIfLevel(min, max, message, params);
    }
    
    /**
     * Prints the stackTrace of the provided Throwable object
     * @param level The log level to log this stack trace at
     * @param e The Throwable object to obtain stack trace information from
     */
    public static void printStackTrace(Level level, Throwable e)
    {
        printStackTrace(level, e, Integer.MAX_VALUE);
    }

    /**
     * Prints the stackTrace of the provided Throwable object to a certain depth
     * @param level The log level to log this stack trace at
     * @param e The Throwable object to obtain stack trace information from
     * @param maxDepth The max number of trace elements to print
     */
    public static void printStackTrace(Level level, Throwable e, int maxDepth)
    {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (StackTraceElement element : e.getStackTrace())
        {
            sb.append(element.toString());
            sb.append("\n");
            if (++count > maxDepth)
            {
                break;
            }
        }
        TerrainControl.log(level, sb.toString());
    }

    /**
     * Returns the CustomObject manager, with hooks to spawn CustomObjects.
     * <p/>
     * @return The CustomObject manager.
     */
    public static CustomObjectManager getCustomObjectManager()
    {
        return customObjectManager;
    }

    /**
     * Returns the Resource manager.
     * <p/>
     * @return The Resource manager.
     */
    public static ConfigFunctionsManager getConfigFunctionsManager()
    {
        return configFunctionsManager;
    }
    
    /**
     * Returns the biome managers. Register your own biome manager here.
     * <p/>
     * @return The biome managers.
     */
    public static BiomeModeManager getBiomeModeManager()
    {
        return biomeManagers;
    }
    
    /**
     * Returns the global config file.
     * @return The global config file.
     */
    public static PluginConfig getPluginConfig()
    {
        return pluginConfig;
    }

    // Events

    /**
     * Register your event handler here with normal priority. You can do 
     * this before TerrainControl is started.
     * <p/>
     * @param handler The handler that will receive the events.
     */
    public static void registerEventHandler(EventHandler handler)
    {
        cancelableEventHandlers.add(handler);
    }

    /**
     * Register you event handler here with the given priority. You can do 
     * this before TerrainControl is started.
     * <p/>
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

    /** Firing events
     * All methods first call the cancelableEventHandlers, and then the
     * monitoringEventHandlers. Only cancelableEventHandlers can cancel
     * events. Cancelled events are still fired.
     */
    
    //t>>	
    /**
     * 
     * @param object
     * @param world
     * @param x
     * @param y
     * @param z
     * <p/>
     * @return
     */
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

    //t>>	
    /**
     *
     * @param resource
     * @param world
     * @param random
     * @param villageInChunk
     * @param chunkX
     * @param chunkZ
     * <p/>
     * @return
     */
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

    //t>>	
    /**
     *
     * @param world
     * @param random
     * @param villageInChunk
     * @param chunkX
     * @param chunkZ
     */
    public static void firePopulationStartEvent(LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ)
    {
        for (EventHandler handler : cancelableEventHandlers)
            handler.onPopulateStart(world, random, villageInChunk, chunkX, chunkZ);
        for (EventHandler handler : monitoringEventHandlers)
            handler.onPopulateStart(world, random, villageInChunk, chunkX, chunkZ);
    }

    //t>>	
    /**
     *
     * @param world
     * @param random
     * @param villageInChunk
     * @param chunkX
     * @param chunkZ
     */
    public static void firePopulationEndEvent(LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ)
    {
        for (EventHandler handler : cancelableEventHandlers)
            handler.onPopulateEnd(world, random, villageInChunk, chunkX, chunkZ);
        for (EventHandler handler : monitoringEventHandlers)
            handler.onPopulateEnd(world, random, villageInChunk, chunkX, chunkZ);
    }

}
