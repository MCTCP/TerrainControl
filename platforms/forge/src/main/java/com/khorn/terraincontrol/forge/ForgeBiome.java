package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.forge.generator.BiomeGenCustom;
import com.khorn.terraincontrol.util.helpers.StringHelper;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ForgeBiome implements LocalBiome
{
    private final Biome biomeBase;
    private final BiomeIds biomeIds;
    private final BiomeConfig biomeConfig;

    /**
     * Creates a new biome with the given name and id.
     * 
     * @param biomeConfig The config of the biome.
     * @param minecraftBiome The Minecraft instance of the biome.
     * @param biomeIds The ids of the biome.
     * @return The registered biome.
     */
    public static ForgeBiome forBiome(BiomeConfig biomeConfig, Biome minecraftBiome, BiomeIds biomeIds)
    {
        return new ForgeBiome(biomeConfig, minecraftBiome, biomeIds);
    }

    /**
     * Registers the biome to the biome registry.
     * @param forgeBiome The biome.
     */
    @SuppressWarnings("deprecation") // we must use the specified id
    static void registerBiome(ForgeBiome forgeBiome)
    {
        GameRegistry.findRegistry(Biome.class);
        FMLControlledNamespacedRegistry<Biome> registry = (FMLControlledNamespacedRegistry<Biome>) Biome.REGISTRY;

        String biomeNameForRegistry = StringHelper.toComputerFriendlyName(forgeBiome.getName());
        ResourceLocation name = new ResourceLocation(PluginStandardValues.PLUGIN_NAME, biomeNameForRegistry);
        int idForRegistry = forgeBiome.biomeIds.getSavedId();
        registry.register(idForRegistry, name, forgeBiome.biomeBase);

        int actualId = registry.getId(forgeBiome.biomeBase);
        if (actualId != idForRegistry)
        {
            throw new RuntimeException("Got another id assigned than the desired one");
        }
    }

    private ForgeBiome(BiomeConfig biomeConfig, Biome biome, BiomeIds biomeIds)
    {
        this.biomeBase = biome;
        this.biomeConfig = biomeConfig;
        this.biomeIds = biomeIds;
    }

    @Override
    public boolean isCustom()
    {
        return biomeBase instanceof BiomeGenCustom;
    }

    @Override
    public String getName()
    {
        return biomeBase.getBiomeName();
    }

    public Biome getHandle()
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
        return biomeBase.getFloatTemperature(new BlockPos(x, y, z));
    }

    @Override
    public BiomeConfig getBiomeConfig()
    {
        return biomeConfig;
    }
    
    @Override
    public String toString()
    {
        return getName() + "[" + biomeIds + "]";
    }
}
