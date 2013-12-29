package com.khorn.terraincontrol.forge;

import net.minecraft.block.Block;

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

    @Override
    public boolean isCustom()
    {
        return true;
    }

    @Override
    public int getCustomId()
    {
        return getId();
    }

    @Override
    public void setEffects(BiomeConfig config)
    {
        biomeBase.setEffects(config);
    }

    @Override
    public String getName()
    {
        return biomeBase.biomeName;
    }

    public BiomeGenBase getHandle()
    {
        return biomeBase;
    }

    @Override
    public int getId()
    {
        return biomeBase.biomeID;
    }

    @Override
    public float getTemperature()
    {
        return biomeBase.temperature;
    }

    @Override
    public float getWetness()
    {
        return biomeBase.rainfall;
    }

    @Override
    public float getSurfaceHeight()
    {
        return biomeBase.minHeight;
    }

    @Override
    public float getSurfaceVolatility()
    {
        return biomeBase.maxHeight;
    }

    @Override
    public int getSurfaceBlock()
    {
        return Block.func_149682_b(biomeBase.topBlock);
    }

    @Override
    public int getGroundBlock()
    {
        return Block.func_149682_b(biomeBase.fillerBlock);
    }

    @Override
    public float getTemperatureAt(int x, int y, int z) {
        return biomeBase.func_150564_a(x, y, z);
    }
}
