package com.khorn.terraincontrol;

import java.util.logging.Level;

public class TerrainControl
{
    private static TerrainControlEngine engine;

    private TerrainControl()
    {
        // Forbidden to instantiate.
    }

    /**
     * Starts the engine, making all API methods availible.
     * 
     * @param engine
     *            The implementation of the engine.
     */
    public static void startEngine(TerrainControlEngine engine)
    {
        if (TerrainControl.engine != null)
        {
            throw new UnsupportedOperationException("Engine is already set!");
        }
        TerrainControl.engine = engine;
    }

    /**
     * Null out static references to free up memory. Should be called on
     * shutdown.
     */
    public static void stopEngine()
    {
        engine = null;
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
     * @param name
     *            The name of the world.
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
     * @param world
     *            The world name.
     * @param x
     *            The block x in the world.
     * @param z
     *            The block z in the world.
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
     * @param messages
     *            The messages to log.
     */
    public static void log(String... messages)
    {
        engine.log(Level.INFO, messages);
    }

    /**
     * Logs the message(s) with the given importance. Message will be prefixed
     * with TerrainControl, so don't do that yourself.
     * 
     * @param messages
     *            The messages to log.
     */
    public static void log(Level level, String... messages)
    {
        engine.log(level, messages);
    }
}
