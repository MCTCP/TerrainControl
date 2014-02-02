package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.BiomeIds;

import net.minecraft.block.Block;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.forge.generator.BiomeGenCustom;
import net.minecraft.world.biome.BiomeGenBase;

public class ForgeBiome implements LocalBiome
{
    private BiomeGenCustom biomeBase;
    private BiomeIds biomeIds;

    public ForgeBiome(BiomeGenCustom biome)
    {
        this.biomeBase = biome;
        this.biomeIds = new BiomeIds(biome.biomeID);
    }

    @Override
    public boolean isCustom()
    {
        return true;
    }

    @Override
    public int getCustomId()
    {
        return biomeIds.getSavedId();
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
    public BiomeIds getIds()
    {
        return biomeIds;
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
