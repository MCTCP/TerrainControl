package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.configuration.standard.MojangSettings;
import net.minecraft.server.v1_7_R1.BiomeBase;

/**
 * Gets some default settings from the BiomeBase instance. The settings in the
 * BiomeBase instance are provided by Mojang.
 * 
 * @see MojangSettings
 */
public final class BukkitMojangSettings implements MojangSettings
{
    private final BiomeBase biomeBase;

    /**
     * Creates an instance that provides access to the default settings of the
     * vanilla biome with the given id.
     * 
     * @param biomeId The id of the biome.
     * @return The settings.
     */
    public static MojangSettings fromId(int biomeId)
    {
        return fromBiomeBase(BiomeBase.getBiome(biomeId));
    }

    /**
     * Creates an instance that provides access to the default settings of the
     * vanilla biome.
     * 
     * @param biomeBase The biome.
     * @return The settings.
     */
    public static MojangSettings fromBiomeBase(BiomeBase biomeBase)
    {
        return new BukkitMojangSettings(biomeBase);
    }

    private BukkitMojangSettings(BiomeBase biomeBase)
    {
        this.biomeBase = biomeBase;
    }

    @Override
    public float getTemperature()
    {
        return biomeBase.temperature;
    }

    @Override
    public float getWetness()
    {
        return biomeBase.humidity;
    }

    @Override
    public float getSurfaceHeight()
    {
        return biomeBase.am;
    }

    @Override
    public float getSurfaceVolatility()
    {
        return biomeBase.an;
    }

    @Override
    public LocalMaterialData getSurfaceBlock()
    {
        return new BukkitMaterialData(biomeBase.ai, 0);
    }

    @Override
    public LocalMaterialData getGroundBlock()
    {
        return new BukkitMaterialData(biomeBase.ak, 0);
    }

}
