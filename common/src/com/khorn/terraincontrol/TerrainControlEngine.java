package com.khorn.terraincontrol;

import java.util.logging.Level;

public interface TerrainControlEngine
{
    /**
     * Returns the world object with the given name.
     * @param name The name of the world.
     * @return The world object.
     */
    public abstract LocalWorld getWorld(String name);
    
    /**
     * Logs the messages.
     * @param message The messages to log.
     */
    public abstract void log(Level level, String... message);
}
