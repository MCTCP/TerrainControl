package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import net.minecraft.world.biome.BiomeGenBase;

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

    public void setEffects(BiomeConfig config)
    {
        biomeBase.setEffects(config);
    }

    public String getName()
    {
        return biomeBase.biomeName;
    }

    public BiomeGenBase getHandle()
    {
        return biomeBase;
    }

    public int getId()
    {
        return biomeBase.biomeID;
    }

    public float getTemperature()
    {
        return biomeBase.temperature;
    }

    public float getWetness()
    {
        return biomeBase.rainfall;
    }

    public float getSurfaceHeight()
    {
        return biomeBase.minHeight;
    }

    public float getSurfaceVolatility()
    {
        return biomeBase.maxHeight;
    }

    public byte getSurfaceBlock()
    {
        return biomeBase.topBlock;
    }

    public byte getGroundBlock()
    {
        return biomeBase.fillerBlock;
    }
}
