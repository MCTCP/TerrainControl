package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.LocalBiome;
import net.minecraft.server.BiomeBase;

/**
 * The BukkitBiome is basically a wrapper for the BiomeBase.
 * If you look at the constructor and the method you will see that this is the case.
 */
public class BukkitBiome implements LocalBiome
{
    private BiomeBase biomeBase;
    private boolean isCustom;
    private int customID;

    public BukkitBiome(BiomeBase biome)
    {
        this.biomeBase = biome;
        if(DefaultBiome.getBiome(biome.id) == null)
        {
            this.isCustom = true;
        }
        customID = biomeBase.id;
    }

    public boolean isCustom()
    {
        return this.isCustom;
    }
    public int getCustomId()
    {
        return customID;
    }
    public void setCustomID(int id)
    {
        customID = id;
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