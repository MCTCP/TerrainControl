package com.khorn.terraincontrol.forge;

import net.minecraft.src.BiomeGenBase;

import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.LocalBiome;

public class Biome implements LocalBiome
{
    private BiomeGenCustom biomeBase;

    public Biome(BiomeGenCustom biome)
    {
        this.biomeBase = biome;
    }

    public boolean isCustom()
    {
        return true;
    }

    public int getCustomId()
    {
        return getId();
    }

    public void setCustom(BiomeConfig config)
    {
        ((BiomeGenCustom) this.biomeBase).setVisuals(config);
    }

    public String getName()
    {
        return this.biomeBase.biomeName;
    }

    public int getId()
    {
        return this.biomeBase.biomeID;
    }

    public float getTemperature()
    {
        return this.biomeBase.temperature;
    }

    public float getWetness()
    {
        return this.biomeBase.rainfall;
    }

    public float getSurfaceHeight()
    {
        return this.biomeBase.minHeight;
    }

    public float getSurfaceVolatility()
    {
        return this.biomeBase.maxHeight;
    }

    public byte getSurfaceBlock()
    {
        return this.biomeBase.topBlock;
    }

    public byte getGroundBlock()
    {
        return this.biomeBase.fillerBlock;
    }
}
