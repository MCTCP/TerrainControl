package com.pg85.otg.config.standard;

import com.pg85.otg.util.materials.LocalMaterialData;

/**
 * Some default settings are provided by Mojang, so that they don't have to be
 * updated in our code when Mojang changes the default.
 */
public interface MojangSettings
{
    /**
     * Represents the four mob type groups used by vanilla Minecraft.
     */
    public static enum EntityCategory
    {
        AMBIENT_CREATURE,
        WATER_CREATURE,
        CREATURE,
        MONSTER,
        WATER_AMBIENT,
        MISC
    }

    /**
     * Gets the temperature of this biome, between 0 and 2 (inclusive).
     * Higher is warmer.
     *  
     * @return The temperature of this biome,
     */
    float getTemperature();

    /**
     * Gets the wetness of this biome, between 0 and 1. 
     * Lower is dryer.
     * 
     * @return The wetness of this biome.
     */
    float getWetness();

    float getSurfaceHeight();

    float getSurfaceVolatility();

    LocalMaterialData getSurfaceBlock();

    LocalMaterialData getGroundBlock();
}
