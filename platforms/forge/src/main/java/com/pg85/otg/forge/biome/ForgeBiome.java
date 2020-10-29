package com.pg85.otg.forge.biome;

import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.util.BiomeIds;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

public class ForgeBiome
{
	private final ResourceLocation registryName;
    private final BiomeIds biomeIds;
    private final BiomeConfig biomeConfig;

    public ForgeBiome(ResourceLocation registryName, BiomeConfig biomeConfig, BiomeIds biomeIds)
    {
    	this.registryName = registryName;
        this.biomeIds = biomeIds;
        this.biomeConfig = biomeConfig;
    }

    public ResourceLocation getRegistryName()
    {
    	return this.registryName;
    }
    
    public BiomeIds getIds()
    {
        return this.biomeIds;
    }
    
    public BiomeConfig getBiomeConfig()
    {
        return this.biomeConfig;
    }
    
    public String getName()
    {
        return this.getBiomeConfig().getName();
    }

    public Biome getBiome()
    {
        return ForgeRegistries.BIOMES.getValue(getRegistryName());
    }
}
