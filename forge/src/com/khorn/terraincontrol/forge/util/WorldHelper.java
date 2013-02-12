package com.khorn.terraincontrol.forge.util;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;

import java.util.NoSuchElementException;

public abstract class WorldHelper
{

    /**
     * Returns a free world type id. If there is no free id available, it throws
     * a NoSuchElementException.
     *
     * @return A free world type id.
     */
    public static int getNextWorldTypeID()
    {
        for (int i = 0; i < WorldType.worldTypes.length; i++)
            if (WorldType.worldTypes[i] == null)
                return i;
        throw new NoSuchElementException("No more WorldType indexes available.");
    }

    /**
     * Returns the LocalWorld of the Minecraft world. Returns null if there is
     * no world.
     *
     * @param world
     * @return The LocalWorld, or null if there is none.
     */
    public static LocalWorld toLocalWorld(World world)
    {
        String worldName = world.getSaveHandler().getSaveDirectoryName();
        return TerrainControl.getWorld(worldName);
    }

}
