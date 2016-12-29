package com.khorn.terraincontrol.forge.util;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.forge.generator.TXBiome;

import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;

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

    public static boolean isVanillaWorld(World world)
    {
        int dimensionId = world.provider.getDimension();
        // If vanilla, return overworld
        if (dimensionId == 0 || dimensionId == 1 || dimensionId == -1)
        {
            return true;
        }

        return false;
    }

    /**
     * Gets the generation id of the given biome. This is usually equal to the
     * id of the BiomeBase, but when using virtual biomes it may be different.
     * 
     * @param biomeBase The biome to check.
     * @return The generation id.
     */
    public static int getGenerationId(Biome biomeBase)
    {
        if (biomeBase instanceof TXBiome)
        {
            return ((TXBiome) biomeBase).generationId;
        }
        return Biome.getIdForBiome(biomeBase);
    }

    /**
     * Gets the name of the given world.
     * @param world The world.
     * @return The name.
     */
    public static String getName(World world)
    {
        World defaultWorld = DimensionManager.getWorld(0);
        // If vanilla or we are dealing with an implementation that supports
        // multiple save handlers, return the world name
        if (isVanillaWorld(world) || (defaultWorld != null && world.getWorldInfo() != null && world.getSaveHandler() != defaultWorld.getSaveHandler()))
        {
            return world.getWorldInfo().getWorldName();
        }

        // This is the best we can do for Forge
        return world.provider.getSaveFolder();
    }

    private WorldHelper()
    {
    }

}
