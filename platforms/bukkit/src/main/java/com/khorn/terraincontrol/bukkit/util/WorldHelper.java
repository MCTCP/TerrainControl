package com.khorn.terraincontrol.bukkit.util;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;

public abstract class WorldHelper
{
    /**
     * Returns the LocalWorld of the Minecraft world. Returns null if TC isn't
     * loaded for that world.
     *
     * @param world
     *            The world.
     * @return The LocalWorld, or null if there is none.
     */
    public static LocalWorld toLocalWorld(net.minecraft.server.v1_7_R1.World world)
    {
        return TerrainControl.getWorld(world.getWorld().getName());
    }

    /**
     * Returns the LocalWorld of the CraftBukkit world. Returns null if TC isn't
     * loaded for that world.
     *
     * @param world
     *            The world.
     * @return The LocalWorld, or null if there is none.
     */
    public static LocalWorld toLocalWorld(org.bukkit.World world)
    {
        return TerrainControl.getWorld(world.getName());
    }

    private WorldHelper()
    {
    }
}
