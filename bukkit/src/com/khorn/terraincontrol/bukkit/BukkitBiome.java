package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import net.minecraft.server.v1_6_R2.BiomeBase;

/**
 * The BukkitBiome is basically a wrapper for the BiomeBase.
 * If you look at the constructor and the method you will see that this is the case.
 */
public class BukkitBiome implements LocalBiome
{
    private BiomeBase biomeBase;
    private boolean isCustom;
    private int customID;

    private float temperature;
    private float humidity;

    public BukkitBiome(BiomeBase biome)
    {
        this.biomeBase = biome;
        if (DefaultBiome.getBiome(biome.id) == null)
        {
            this.isCustom = true;
        }
        customID = biomeBase.id;

        this.temperature = biome.temperature;
        this.humidity = biome.humidity;
    }

    @Override
    public boolean isCustom()
    {
        return this.isCustom;
    }

    @Override
    public int getCustomId()
    {
        return customID;
    }

    public void setCustomID(int id)
    {
        customID = id;
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
        return this.biomeBase.y;
    }

    @Override
    public int getId()
    {
        return this.biomeBase.id;
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
        return this.biomeBase.D;
    }

    @Override
    public float getSurfaceVolatility()
    {
        return this.biomeBase.E;
    }

    @Override
    public byte getSurfaceBlock()
    {
        return this.biomeBase.A;
    }

    @Override
    public byte getGroundBlock()
    {
        return this.biomeBase.B;
    }
}