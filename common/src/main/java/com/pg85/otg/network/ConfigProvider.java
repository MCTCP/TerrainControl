package com.pg85.otg.network;

import java.util.List;

import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.config.world.WorldConfig;

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
    LocalBiome getBiomeByOTGIdOrNull(int id);
   
    /**
     * Gets an array with all biomes indexed by the otg biome id.
     * @return The array.
     */
    LocalBiome[] getBiomeArrayByOTGId();

    /**
     * Gets a list of all biomes ordered by generation id.
     * Only used by the biome generator for backwards compatibility.
     * @return
     */
    List<LocalBiome> getBiomeArrayLegacy();
    
    /**
     * Reloads all settings. If this implementation doesn't support reloading
     * (for example because it is read once from a network stream), this
     * method does nothing.
     */
    void reload();
}
