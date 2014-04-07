package com.khorn.terraincontrol;

import com.khorn.terraincontrol.configuration.ConfigFunctionsManager;
import com.khorn.terraincontrol.configuration.PluginConfig;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.CustomObjectManager;
import com.khorn.terraincontrol.events.EventHandler;
import com.khorn.terraincontrol.events.EventPriority;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.generator.biome.BiomeModeManager;
import com.khorn.terraincontrol.generator.resource.Resource;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;
import org.apache.logging.log4j.Marker;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Random;

public class TerrainControl
{

    /**
     * The engine that powers Terrain Control.
     */
    private static TerrainControlEngine engine;

    /**
     * The amount of different block ids that are supported. 4096 on Minecraft.
     */
    public static final int SUPPORTED_BLOCK_IDS = 4096;

    /**
     * The world depth that the engine supports. Not the actual depth the
     * world is capped at. 0 in Minecraft.
     */
    public static final int WORLD_DEPTH = 0;

    /**
     * The world height that the engine supports. Not the actual height the
     * world is capped at. 256 in Minecraft.
     */
    public static final int WORLD_HEIGHT = 256;

    /**
     * @see TerrainControlEngine#fireCanCustomObjectSpawnEvent(CustomObject,
     * LocalWorld, int, int, int)
     */
    public static boolean fireCanCustomObjectSpawnEvent(CustomObject object, LocalWorld world, int x, int y, int z)
    {
        return engine.fireCanCustomObjectSpawnEvent(object, world, x, y, z);
    }

    /**
     * @see TerrainControlEngine#firePopulationEndEvent(LocalWorld, Random,
     * boolean, int, int)
     */
    public static void firePopulationEndEvent(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
        engine.firePopulationEndEvent(world, random, villageInChunk, chunkCoord);
    }

    /**
     * @see TerrainControlEngine#firePopulationStartEvent(LocalWorld, Random,
     * boolean, int, int)
     */
    public static void firePopulationStartEvent(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
        engine.firePopulationStartEvent(world, random, villageInChunk, chunkCoord);
    }

    /**
     * @see TerrainControlEngine#fireResourceProcessEvent(Resource,
     * LocalWorld, Random, boolean, int, int)
     */
    public static boolean fireResourceProcessEvent(Resource resource, LocalWorld world, Random random, boolean villageInChunk, int chunkX,
                                                   int chunkZ)
    {
        return engine.fireResourceProcessEvent(resource, world, random, villageInChunk, chunkX, chunkZ);
    }

    /**
     * Returns the biome managers. Register your own biome manager here.
     * <p>
     * @return The biome managers.
     */
    public static BiomeModeManager getBiomeModeManager()
    {
        return engine.getBiomeModeManager();
    }

    /**
     * Convienence method to quickly get the biome name at the given
     * coordinates. Will return null if the world isn't loaded by Terrain
     * Control.
     * <p>
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
     * Returns the Resource manager.
     * <p>
     * @return The Resource manager.
     */
    public static ConfigFunctionsManager getConfigFunctionsManager()
    {
        return engine.getConfigFunctionsManager();
    }

    /**
     * Returns the CustomObject manager, with hooks to spawn CustomObjects.
     * <p>
     * @return The CustomObject manager.
     */
    public static CustomObjectManager getCustomObjectManager()
    {
        return engine.getCustomObjectManager();
    }

    /**
     * Returns the engine, containing the API methods.
     * <p>
     * @return The engine
     */
    public static TerrainControlEngine getEngine()
    {
        return engine;
    }

    /**
     * @see TerrainControlEngine#readMaterial(String)
     */
    public static LocalMaterialData readMaterial(String name) throws InvalidConfigException
    {
        return engine.readMaterial(name);
    }

    /**
     * @see TerrainControlEngine#toLocalMaterialData(DefaultMaterial, int)
     */
    public static LocalMaterialData toLocalMaterialData(DefaultMaterial defaultMaterial, int blockData)
    {
        return engine.toLocalMaterialData(defaultMaterial, blockData);
    }

    /**
     * Returns the global config file.
     * <p>
     * @return The global config file.
     */
    public static PluginConfig getPluginConfig()
    {
        return engine.getPluginConfig();
    }

    /**
     * Returns the world object with the given name.
     * <p>
     * @param name The name of the world.
     * @return The world object.
     */
    public static LocalWorld getWorld(String name)
    {
        return engine.getWorld(name);
    }

    /**
     * Logs the messages with the given importance. Message will be
     * prefixed with [TerrainControl], so don't do that yourself.
     * <p>
     * @param message The messages to log.
     * @param level   The severity of the message
     */
    public static void log(Marker level, List<String> messages)
    {
        engine.getLogger().log(level, messages);
    }

    /**
     * Logs a format string message with the given importance. Message will
     * be prefixed with [TerrainControl], so don't do that yourself.
     * <p>
     * @param message The messages to log formatted similar to Logger.log()
     *                with the same args.
     * @param level   The severity of the message
     * @param params  The parameters belonging to {0...} in the message
     *                string
     */
    public static void log(Marker level, String message, Object... params)
    {
        engine.getLogger().log(level, message, params);
    }

    /**
     * Logs the message(s) with the given importance <b>ONLY IF</b> logger
     * level matches the level provided. Message will be prefixed with
     * [TerrainControl], so don't do that yourself.
     * <p>
     * @param ifLevel  the Log level to test for
     * @param messages The messages to log.
     */
    public static void logIfLevel(Marker ifLevel, List<String> messages)
    {
        engine.getLogger().logIfLevel(ifLevel, messages);
    }

    /**
     * Logs the message(s) with the given importance <b>ONLY IF</b> logger
     * level matches the level provided. Message will be prefixed with
     * [TerrainControl], so don't do that yourself.
     * <p>
     * @param ifLevel the Log level to test for
     * @param message The messages to log formatted similar to
     *                Logger.log() with the same args.
     * @param params  The parameters belonging to {0...} in the message
     *                string
     */
    public static void logIfLevel(Marker ifLevel, String message, Object... params)
    {
        engine.getLogger().logIfLevel(ifLevel, message, params);
    }

    /**
     * Logs the message(s) with the given importance <b>ONLY IF</b> logger
     * level is between the min/max provided. Message will be prefixed with
     * [TerrainControl], so don't do that yourself.
     * <p>
     * @param min      The minimum Log level to test for
     * @param max      The maximum Log level to test for
     * @param messages The messages to log.
     */
    public static void logIfLevel(Marker min, Marker max, List<String> messages)
    {
        engine.getLogger().logIfLevel(min, max, messages);
    }

    /**
     * Logs the message(s) with the given importance <b>ONLY IF</b> logger
     * level is between the min/max provided. Message will be prefixed with
     * [TerrainControl], so don't do that yourself.
     * <p>
     * @param min     The minimum Log level to test for
     * @param max     The maximum Log level to test for
     * @param message The messages to log formatted similar to
     *                Logger.log() with the same args.
     * @param params  The parameters belonging to {0...} in the message
     *                string
     */
    public static void logIfLevel(Marker min, Marker max, String message, Object... params)
    {
        engine.getLogger().logIfLevel(min, max, message, params);
    }

    /**
     * Prints the stackTrace of the provided Throwable object
     * <p>
     * @param level The log level to log this stack trace at
     * @param e     The Throwable object to obtain stack trace information from
     */
    public static void printStackTrace(Marker level, Throwable e)
    {
        printStackTrace(level, e, Integer.MAX_VALUE);
    }

    /**
     * Prints the stackTrace of the provided Throwable object to a certain
     * depth
     * <p>
     * @param level    The log level to log this stack trace at
     * @param e        The Throwable object to obtain stack trace information
     *                 from
     * @param maxDepth The max number of trace elements to print
     */
    public static void printStackTrace(Marker level, Throwable e, int maxDepth)
    {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        engine.getLogger().log(level, stringWriter.toString());
    }

    /**
     * @see TerrainControlEngine#registerEventHandler(EventHandler)
     */
    public static void registerEventHandler(EventHandler handler)
    {
        engine.registerEventHandler(handler);
    }

    /**
     * @see TerrainControlEngine#registerEventHandler(EventHandler,
     * EventPriority)
     */
    public static void registerEventHandler(EventHandler handler, EventPriority priority)
    {
        engine.registerEventHandler(handler, priority);
    }

    /**
     * Sets the engine and calls its {@link TerrainControlEngine#onStart()
     * onStart()} method.
     * <p>
     * @param engine The engine.
     */
    public static void setEngine(TerrainControlEngine engine)
    {
        if (TerrainControl.engine != null)
        {
            throw new IllegalStateException("Engine is already set.");
        }

        TerrainControl.engine = engine;
        engine.onStart();
    }

    /**
     * Nulls out static references to free up memory. Should be called on
     * shutdown. Engine can be restarted after this.
     */
    public static void stopEngine()
    {
        engine.onShutdown();
        engine = null;
    }

    private TerrainControl()
    {
        // Forbidden to instantiate.
    }

}
