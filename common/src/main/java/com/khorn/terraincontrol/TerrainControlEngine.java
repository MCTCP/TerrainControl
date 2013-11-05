package com.khorn.terraincontrol;

import com.khorn.terraincontrol.logging.LoggableEngine;
import java.io.File;

public interface TerrainControlEngine extends LoggableEngine
{

    /**
     * Returns the world object with the given name.
     * <p/>
     * @param name The name of the world.
     * <p/>
     * @return The world object.
     */
    public LocalWorld getWorld(String name);

    /**
     * Returns the root data folder for TerrainControl.
     * <p/>
     * @return The root data folder for TerrainControl.
     */
    public File getTCDataFolder();

    /**
     * Returns the folder where the global objects are stored in.
     * <p/>
     * @return Folder where the global objects are stored.
     */
    public File getGlobalObjectsDirectory();

    /**
     * Returns whether the given id is a valid block id. If the block
     * doesn't exist, the id is considered invalid.
     * <p/>
     * @param id The id of the block.
     * <p/>
     * @return Whether the given id is a valid block id.
     */
    public boolean isValidBlockId(int id);

}
