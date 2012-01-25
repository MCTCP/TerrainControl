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
        if(DefaultBiome.getBiome(biome.K) == null )
            this.isCustom =true;
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
        return this.biomeBase.w;
    }

    public int getId()
    {
        return this.biomeBase.K;
    }

    public float getTemperature()
    {
        return this.biomeBase.D;
    }

    public float getWetness()
    {
        return this.biomeBase.E;
    }

    public float getSurfaceHeight()
    {
        return this.biomeBase.B;
    }

    public float getSurfaceVolatility()
    {
        return this.biomeBase.C;
    }

    public byte getSurfaceBlock()
    {
        return this.biomeBase.y;
    }

    public byte getGroundBlock()
    {
        return this.biomeBase.z;
    }
}
