package com.khorn.terraincontrol.bukkit.util;

import net.minecraft.server.v1_7_R1.World;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.TCPlugin;

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
        return ((TCPlugin) TerrainControl.getEngine()).worlds.get(world.getWorld().getUID());
    }
}
