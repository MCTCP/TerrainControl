package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.forge.generator.BiomeGenCustom;

public class ForgeBiome implements LocalBiome
{
    private BiomeGenCustom biomeBase;
    private BiomeIds biomeIds;

    /**
     * Creates a new biome with the given name and id. Also registers it in
     * Minecraft's biome array, but only when the biome is not virtual.
     * 
     * @param name The name of the biome.
     * @param biomeIds The ids of the biome.
     * @return The registered biome.
     */
    public static ForgeBiome createBiome(String name, BiomeIds biomeIds)
    {
        // See the BiomeGenCustom constructor for a note
        // about biome registration
        return new ForgeBiome(new BiomeGenCustom(name, biomeIds));
    }

    public ForgeBiome(BiomeGenCustom biome)
    {
        this.biomeBase = biome;
        this.biomeIds = new BiomeIds(biome.generationId, biome.biomeID);
    }

    @Override
    public boolean isCustom()
    {
        return true;
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

    public BiomeGenCustom getHandle()
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
        return biomeBase.rootHeight;
    }

    @Override
    public float getSurfaceVolatility()
    {
        return biomeBase.heightVariation;
    }

    @Override
    public LocalMaterialData getSurfaceBlock()
    {
        return new ForgeMaterialData(biomeBase.topBlock, 0);
    }

    @Override
    public LocalMaterialData getGroundBlock()
    {
        return new ForgeMaterialData(biomeBase.fillerBlock, 0);
    }

    @Override
    public float getTemperatureAt(int x, int y, int z)
    {
        return biomeBase.getFloatTemperature(x, y, z);
    }
}
