package com.pg85.otg.forge.biomes;

import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.standard.WorldStandardValues;

import net.minecraft.world.biome.Biome.BiomeProperties;

/**
 * Extension of BiomeProperties so that we are able to access the protected
 * methods.
 */
class BiomePropertiesCustom extends BiomeProperties
{
    BiomePropertiesCustom(BiomeConfig biomeConfig)
    {
        super(biomeConfig.getName());
        this.setBaseHeight(biomeConfig.biomeHeight);
        this.setHeightVariation(biomeConfig.biomeVolatility);
        this.setRainfall(biomeConfig.biomeWetness);
        this.setWaterColor(biomeConfig.waterColor);
        float safeTemperature = biomeConfig.biomeTemperature;
        if (safeTemperature >= 0.1 && safeTemperature <= 0.2)
        {
            // Avoid temperatures between 0.1 and 0.2, Minecraft restriction
            safeTemperature = safeTemperature >= 1.5 ? 0.2f : 0.1f;
        }
        this.setTemperature(safeTemperature);
        if (biomeConfig.biomeWetness <= 0.0001)
        {
            this.setRainDisabled();
        }
        if (biomeConfig.biomeTemperature <= WorldStandardValues.SNOW_AND_ICE_MAX_TEMP)
        {
            this.setSnowEnabled();
        }
    }
}