package com.khorn.terraincontrol;

import java.io.File;
import java.util.logging.Level;

public interface TerrainControlEngine
{
    /**
     * Returns the world object with the given name.
     *
     * @param name The name of the world.
     * @return The world object.
     */
    public LocalWorld getWorld(String name);

    /**
     * Logs the messages.
     *
     * @param message The messages to log.
     */
    public void log(Level level, String... message);

    /**
     * Returns the folder where the global objects are stored in.
     *
     * @return
     */
    public File getGlobalObjectsDirectory();
    
    /**
     * Returns whether the given id is a valid block id. If the block doesn't exist, the id is considered invalid.
     *
     * @param id The id of the block.
     * @return Whether the given id is a valid block id.
     */
    public boolean isValidBlockId(int id);
}
