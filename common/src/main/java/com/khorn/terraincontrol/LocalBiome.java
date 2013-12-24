package com.khorn.terraincontrol;

import com.khorn.terraincontrol.configuration.BiomeConfig;

/**
 * Class to access the properties of Minecraft's Biome(Gen)Base class.
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


    boolean isVirtual();

    /**
     * Sets the post generator effects. For the client it are things like
     * colors. For the server it are things like mob spawning.
     * 
     * @param config The BiomeConfig of the biome.
     */
    void setEffects(BiomeConfig config);

    /**
     * Gets the name of this biome, like Plains. For vanilla biomes, this is
     * the name Mojang gave to them, for custom biomes the name is decided by
     * the server owner in the WorldConfig.
     * 
     * @return The name.
     */
    String getName();

    /**
     * Gets the id of this biome, saved to the map files.
     * 
     * @return The id.
     */
    int getId();

    /**
     * If this biome is a custom biome, this gets an id for this biome that is
     * unique for this world. This id is decided by Terrain Control and is not
     * saved to the map files.
     * 
     * @return The id.
     */
    int getCustomId();

    /**
     * Gets the temperature of this biome, between 0 and 2 (inclusive). Higher
     * is warmer.
     * 
     * @return The temperature of this biome,
     */
    float getTemperature();

    /**
     * Gets the wetness of this biome, between 0 and 1. Lower is dryer.
     * 
     * @return The wetness of this biome.
     */
    float getWetness();

    /**
     * Gets the default value for the BiomeHeight setting.
     * 
     * @return The default BiomeHeight.
     */
    float getSurfaceHeight();

    /**
     * Gets the default value for the BiomeVolatility setting.
     * 
     * @return The default BiomeVolatility.
     */
    float getSurfaceVolatility();

    /**
     * Gets the default value for the surface block setting.
     * 
     * @return The default SurfaceBlock.
     */
    int getSurfaceBlock();

    /**
     * Gets the default value for the GroundBlock setting.
     * 
     * @return The default GroundBlock.
     */
    int getGroundBlock();

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
}
