package com.pg85.otg.forge.biomes;

import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.util.BiomeIds;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class ForgeBiome implements LocalBiome
{
    public final Biome biomeBase;
    private final boolean isCustom;

    private final BiomeIds biomeIds;
    private final BiomeConfig biomeConfig;

    public ForgeBiome(Biome biome, BiomeConfig biomeConfig, BiomeIds biomeIds)
    {
        this.biomeBase = biome;
        this.biomeIds = biomeIds;
        this.biomeConfig = biomeConfig;
        if (biome instanceof OTGBiome)
        {
            this.isCustom = true;
        } else
        {
            this.isCustom = false;
        }
        
        if(biomeConfig == null)
        {
        	throw new RuntimeException("Was machst du!?!");
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
        return this.biomeBase.biomeName;
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
        return this.biomeBase.getTemperature(new BlockPos(x, y, z));
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
