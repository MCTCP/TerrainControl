package com.Khorn.TerrainControl.Bukkit;

import com.Khorn.TerrainControl.Configuration.BiomeConfig;
import com.Khorn.TerrainControl.DefaultBiome;
import com.Khorn.TerrainControl.LocalBiome;
import net.minecraft.server.BiomeBase;

public class BukkitBiome implements LocalBiome
{
    private BiomeBase biomeBase;
    private boolean isCustom;

    public BukkitBiome(BiomeBase biome)
    {
        this.biomeBase = biome;
        if(DefaultBiome.getBiome(biome.id) == null)
        {
            this.isCustom = true;
        }
    }

    public boolean isCustom()
    {
        return this.isCustom;
    }

    public void setCustom(BiomeConfig config)
    {
        ((CustomBiome)this.biomeBase).SetBiome(config);
    }

    public String getName()
    {
        return this.biomeBase.y;
    }

    public int getId()
    {
        return this.biomeBase.id;
    }

    public float getTemperature()
    {
        return this.biomeBase.F;
    }

    public float getWetness()
    {
        return this.biomeBase.G;
    }

    public float getSurfaceHeight()
    {
        return this.biomeBase.D;
    }

    public float getSurfaceVolatility()
    {
        return this.biomeBase.E;
    }

    public byte getSurfaceBlock()
    {
        return this.biomeBase.A;
    }

    public byte getGroundBlock()
    {
        return this.biomeBase.B;
    }
}