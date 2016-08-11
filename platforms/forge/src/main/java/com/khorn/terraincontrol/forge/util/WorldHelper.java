package com.khorn.terraincontrol.forge.util;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;

import net.minecraft.world.World;

public abstract class WorldHelper
{

    /**
     * Returns the LocalWorld of the Minecraft world. Returns null if there is
     * no world.
     *
     * @param world The Minecraft world.
     * @return The LocalWorld, or null if there is none.
     */
    public static LocalWorld toLocalWorld(World world)
    {
        return TerrainControl.getWorld(getName(world));
    }

    /**
     * Gets the name of the given world.
     * @param world The world.
     * @return The name.
     */
    public static String getName(World world)
    {
        return world.getWorldInfo().getWorldName();
    }

    private WorldHelper()
    {
    }

}
