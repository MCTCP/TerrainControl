package com.khorn.terraincontrol.bukkit.util;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.TXBiomeBase;
import net.minecraft.server.v1_12_R1.BiomeBase;

public abstract class WorldHelper
{
    /**
     * Returns the LocalWorld of the Minecraft world. Returns null if TC isn't
     * loaded for that world.
     * 
     * @param world The world.
     * @return The LocalWorld, or null if there is none.
     */
    public static LocalWorld toLocalWorld(net.minecraft.server.v1_12_R1.World world)
    {
        return TerrainControl.getWorld(world.getWorld().getName());
    }

    /**
     * Returns the LocalWorld of the CraftBukkit world. Returns null if TC
     * isn't loaded for that world.
     * 
     * @param world The world.
     * @return The LocalWorld, or null if there is none.
     */
    public static LocalWorld toLocalWorld(org.bukkit.World world)
    {
        return TerrainControl.getWorld(world.getName());
    }

    /**
     * Gets the generation id of the given biome. This is usually equal to the
     * id of the BiomeBase, but when using virtual biomes it may be different.
     * 
     * @param biomeBase The biome to check.
     * @return The generation id.
     */
    public static int getGenerationId(BiomeBase biomeBase)
    {
        if (biomeBase instanceof TXBiomeBase)
        {
            return ((TXBiomeBase) biomeBase).generationId;
        }
        return BiomeBase.a(biomeBase);
    }

    /**
     * Gets the saved id of the given biome.
     *
     * @param biomeBase The biome.
     * @return The id.
     */
    public static int getSavedId(BiomeBase biomeBase)
    {
        return BiomeBase.a(biomeBase);
    }

    private WorldHelper()
    {
    }
}
