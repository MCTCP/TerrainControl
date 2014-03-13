package com.khorn.terraincontrol;

import com.khorn.terraincontrol.configuration.BiomeConfig;

/**
 * Class to access the properties of a biome.
 * 
 */
public interface LocalBiome
{
    /**
     * Returns whether this biome is part of the vanilla biomes created by
     * Mojang, or whether it is a custom biomes.
     * 
     * @return Whether this biome is a custom biome.
     */
    boolean isCustom();

    /**
     * Sets the post generator effects. For the client it are things like
     * colors. For the server it are things like mob spawning.
     */
    void setEffects();

    /**
     * Gets the name of this biome, like Plains. For vanilla biomes, this is
     * the name Mojang gave to them, for custom biomes the name is decided by
     * the server owner in the WorldConfig.
     * 
     * @return The name.
     */
    String getName();

    /**
     * Gets the {@link BiomeIds biome ids} of this biome.
     * 
     * @see BiomeIds
     * @return The id.
     */
    BiomeIds getIds();

    /**
     * Gets the temperature at the given position, if this biome would be
     * there. This temperature is based on the {@link #getTemperature()}
     * value, but it will be lower at higher altitudes.
     * 
     * @param x The x position in the world.
     * @param y The y position in the world.
     * @param z The z position in the world.
     * @return The temperature.
     */
    float getTemperatureAt(int x, int y, int z);
    
    /**
     * Gets the {@link BiomeConfig} of this biome, which holds all settings
     * of this biome.
     *
     * @return The BiomeConfig.
     */
    BiomeConfig getBiomeConfig();
}
