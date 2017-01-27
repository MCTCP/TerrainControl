package com.khorn.terraincontrol.configuration.standard;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;

import java.util.List;

/**
 * Some default settings are provided by Mojang, so that they don't have to be
 * updated in our code when Mojang changes the default.
 * 
 */
public interface MojangSettings
{
    /**
     * Represents the four mob type groups used by vanilla Minecraft.
     */
    enum EntityCategory
    {
        AMBIENT_CREATURE,
        WATER_CREATURE,
        CREATURE,
        MONSTER
    }

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
    LocalMaterialData getSurfaceBlock();

    /**
     * Gets the default value for the GroundBlock setting.
     * 
     * @return The default GroundBlock.
     */
    LocalMaterialData getGroundBlock();

    /**
     * Gets the default value for the mob spawn list.
     * @param entityCategory The mob type.
     * @return The mob spawn list.
     */
    List<WeightedMobSpawnGroup> getMobSpawnGroup(EntityCategory entityCategory);

}
