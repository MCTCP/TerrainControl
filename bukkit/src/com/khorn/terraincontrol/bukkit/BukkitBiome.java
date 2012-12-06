package com.khorn.terraincontrol.bukkit;

import java.lang.reflect.Field;

import net.minecraft.server.v1_4_5.BiomeBase;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.BiomeConfig;

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

        try
        {
            Field temp;
            Field humid;
            try
            {
                temp = BiomeBase.class.getField("temperature");
                humid = BiomeBase.class.getField("humidity");

            } catch (NoSuchFieldException e)
            {
                temp = BiomeBase.class.getField("F");
                humid = BiomeBase.class.getField("G");
            }

            this.temperature = temp.getFloat(biome);
            this.humidity = humid.getFloat(biome);
        } catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
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

    public void setVisuals(BiomeConfig config)
    {
        ((CustomBiome) this.biomeBase).SetBiome(config);
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
        return this.temperature;
    }

    public float getWetness()
    {
        return this.humidity;
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