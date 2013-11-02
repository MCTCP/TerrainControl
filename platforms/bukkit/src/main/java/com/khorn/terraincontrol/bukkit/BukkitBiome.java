package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import net.minecraft.server.v1_6_R3.BiomeBase;

/**
 * The BukkitBiome is basically a wrapper for the BiomeBase.
 * If you look at the constructor and the method you will see that this is the case.
 */
public class BukkitBiome implements LocalBiome
{
    private BiomeBase biomeBase;
    private boolean isCustom = false;
    private int customID;

    private boolean isVirtual = false;
    private int id;
    private String name;

    private float temperature;
    private float humidity;

    public BukkitBiome(BiomeBase biome)
    {
        this.biomeBase = biome;
        this.id = biomeBase.id;
        this.name = biome.y;

        this.temperature = biome.temperature;
        this.humidity = biome.humidity;
    }

    public BukkitBiome(BiomeBase biome, int customId)
    {
        this.biomeBase = biome;
        this.id = biomeBase.id;
        this.isCustom = true;
        this.customID = customId;
        this.name = biome.y;


        this.temperature = biome.temperature;
        this.humidity = biome.humidity;

    }

    public BukkitBiome(BiomeBase biome,String _name, int customId, int virtualId)
    {
        this.biomeBase = biome;
        this.isVirtual = true;
        this.id = virtualId;
        this.isCustom = true;
        this.customID = customId;
        this.name = _name;


        this.temperature = biome.temperature;
        this.humidity = biome.humidity;

    }

    @Override
    public boolean isCustom()
    {
        return this.isCustom;
    }

    @Override
    public boolean isVirtual()
    {
        return isVirtual;
    }

    @Override
    public int getCustomId()
    {
        return customID;
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
    public int getId()
    {
        return this.id;
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