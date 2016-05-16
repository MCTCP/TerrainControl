package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.customobjects.CustomObjectCollection;

/**
 * Provides the configuration objects of a world. This includes:
 *
 * <ul>
 * <li>The world config.</li>
 * <li>The biome configs.</li>
 * <li>The custom objects.</li>
 * </ul>
 */
public interface ConfigProvider
{

    /**
     * Gets the world config, which holds settings that affect the whole world.
     * @return The world config.
     */
    WorldConfig getWorldConfig();

    /**
     * Gets the biome with the given (generation) id, or null if no such biome exists.
     * @param id The id of the biome.
     * @return The biome, or null if not found.
     */
    LocalBiome getBiomeByIdOrNull(int id);

    /**
     * Gets an array with all biomes indexed by the (generation) biome id.
     * Modifying the array is not allowed and may lead to undefined behavior.
     * @return The array.
     */
    LocalBiome[] getBiomeArray();

    /**
     * Reloads all settings. If this implementation doesn't support reloading
     * (for example because it is read once from a network stream), this
     * method does nothing.
     */
    void reload();

    /**
     * Gets all custom objects of this world.
     * @return All custom objects.
     */
    CustomObjectCollection getCustomObjects();

}
