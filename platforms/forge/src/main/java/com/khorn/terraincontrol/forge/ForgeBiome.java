package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.forge.generator.BiomeGenCustom;
import net.minecraft.world.biome.BiomeGenBase;

public class ForgeBiome implements LocalBiome
{
    private final BiomeGenCustom biomeBase;
    private final BiomeIds biomeIds;
    private final BiomeConfig biomeConfig;

    /**
     * Creates a new biome with the given name and id. Also registers it in
     * Minecraft's biome array, but only when the biome is not virtual.
     * 
     * @param name The name of the biome.
     * @param biomeIds The ids of the biome.
     * @return The registered biome.
     */
    public static ForgeBiome createBiome(BiomeConfig biomeConfig, BiomeIds biomeIds)
    {
        // Save the previous biome
        BiomeGenBase previousBiome = BiomeGenBase.getBiome(biomeIds.getSavedId());

        // Register new biome
        ForgeBiome biome = new ForgeBiome(biomeConfig, new BiomeGenCustom(biomeConfig.name, biomeIds));
        
        // Restore settings of the previous biome
        if (previousBiome != null)
        {
            biome.biomeBase.copyBiome(previousBiome);
        }

        return biome;
    }

    private ForgeBiome(BiomeConfig biomeConfig, BiomeGenCustom biome)
    {
        this.biomeBase = biome;
        this.biomeIds = new BiomeIds(biome.generationId, biome.biomeID);
        this.biomeConfig = biomeConfig;
    }

    @Override
    public boolean isCustom()
    {
        return true;
    }

    @Override
    public void setEffects()
    {
        biomeBase.setEffects(biomeConfig);
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
    public float getTemperatureAt(int x, int y, int z)
    {
        return biomeBase.getFloatTemperature(x, y, z);
    }

    @Override
    public BiomeConfig getBiomeConfig()
    {
        return biomeConfig;
    }
}
