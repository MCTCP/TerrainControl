package com.khorn.terraincontrol.bukkit.util;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import net.minecraft.server.v1_7_R1.World;

public abstract class WorldHelper
{
    /**
     * Returns the LocalWorld of the Minecraft world. Returns null if there is
     * no world.
     *
     * @param world
     * @return The LocalWorld, or null if there is none.
     */
    public static LocalWorld toLocalWorld(World world)
    {
        return TerrainControl.getWorld(world.getWorld().getName());
    }

    private WorldHelper()
    {
    }
}
