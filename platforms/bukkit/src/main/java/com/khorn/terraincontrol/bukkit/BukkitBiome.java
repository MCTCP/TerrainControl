package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import net.minecraft.server.v1_7_R1.BiomeBase;

/**
 * The BukkitBiome is basically a wrapper for the BiomeBase. If you look at
 * the constructor and the method you will see that this is the case.
 */
public class BukkitBiome implements LocalBiome
{
    private BiomeBase biomeBase;
    private boolean isCustom = false;

    private BiomeIds biomeIds;
    private String name;

    private float temperature;
    private float humidity;

    /**
     * Wraps the vanilla biome into a LocalBiome instance.
     * 
     * @param biome The vanilla biome to wrap.
     * @return The wrapped biome.
     */
    public static BukkitBiome forVanillaBiome(BiomeBase biome)
    {
        return new BukkitBiome(biome);
    }

    /**
     * Creates a new custom biome with the given name and ids.
     * 
     * @param name Name of the custom biome.
     * @param biomeIds Ids of the custom biome.
     * @return The custom biome.
     */
    public static BukkitBiome forCustomBiome(String name, BiomeIds biomeIds)
    {
        return new BukkitBiome(new CustomBiome(name, biomeIds));
    }

    // For vanilla biomes
    protected BukkitBiome(BiomeBase biome)
    {
        this.biomeBase = biome;
        this.biomeIds = new BiomeIds(biomeBase.id);
        this.name = biome.af;

        this.temperature = biome.temperature;
        this.humidity = biome.humidity;
    }

    // For custom biomes
    private BukkitBiome(CustomBiome biomeBase)
    {
        this.biomeBase = biomeBase;
        this.biomeIds = new BiomeIds(biomeBase.generationId, biomeBase.id);
        this.isCustom = true;
        this.name = biomeBase.af;

        this.temperature = biomeBase.temperature;
        this.humidity = biomeBase.humidity;
    }

    @Override
    public boolean isCustom()
    {
        return this.isCustom;
    }

    public BiomeBase getHandle()
    {
        return biomeBase;
    }

    @Override
    public void setEffects(BiomeConfig config)
    {
        ((CustomBiome) this.biomeBase).setEffects(config);
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public BiomeIds getIds()
    {
        return this.biomeIds;
    }

    @Override
    public float getTemperature()
    {
        return this.temperature;
    }

    @Override
    public float getWetness()
    {
        return this.humidity;
    }

    @Override
    public float getSurfaceHeight()
    {
        return this.biomeBase.am;
    }

    @Override
    public float getSurfaceVolatility()
    {
        return this.biomeBase.an;
    }

    @Override
    public LocalMaterialData getSurfaceBlock()
    {
        return new BukkitMaterialData(this.biomeBase.ai, 0);
    }

    @Override
    public LocalMaterialData getGroundBlock()
    {
        return new BukkitMaterialData(this.biomeBase.ak, 0);
    }

    @Override
    public float getTemperatureAt(int x, int y, int z)
    {
        return this.biomeBase.a(x, y, z);
    }
}