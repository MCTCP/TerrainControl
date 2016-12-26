package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.forge.generator.TXBiome;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class ForgeBiome implements LocalBiome
{
    private final Biome biomeBase;
    private final boolean isCustom;

    private final BiomeIds biomeIds;
    private final BiomeConfig biomeConfig;

    public ForgeBiome(Biome biome, BiomeConfig biomeConfig, BiomeIds biomeIds)
    {
        this.biomeBase = biome;
        this.biomeIds = biomeIds;
        this.biomeConfig = biomeConfig;
        if (biome instanceof TXBiome)
        {
            this.isCustom = true;
        } else
        {
            this.isCustom = false;
        }
    }

    @Override
    public boolean isCustom()
    {
        return this.isCustom;
    }

    @Override
    public String getName()
    {
        return this.biomeBase.getBiomeName();
    }

    public Biome getHandle()
    {
        return this.biomeBase;
    }

    @Override
    public BiomeIds getIds()
    {
        return this.biomeIds;
    }

    @Override
    public float getTemperatureAt(int x, int y, int z)
    {
        return this.biomeBase.getFloatTemperature(new BlockPos(x, y, z));
    }

    @Override
    public BiomeConfig getBiomeConfig()
    {
        return this.biomeConfig;
    }

    @Override
    public String toString()
    {
        return getName() + "[" + getIds() + "]";
    }
}
