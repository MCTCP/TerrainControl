package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.forge.generator.BiomeGenCustom;
import com.khorn.terraincontrol.util.helpers.StringHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.GameData;

public class ForgeBiome implements LocalBiome
{
    private final BiomeGenCustom biomeBase;
    private final BiomeIds biomeIds;
    private final BiomeConfig biomeConfig;

    /**
     * Creates a new biome with the given name and id.
     * 
     * @param biomeConfig The config of the biome.
     * @param biomeIds The ids of the biome.
     * @return The registered biome.
     */
    public static ForgeBiome createBiome(BiomeConfig biomeConfig, BiomeIds biomeIds)
    {
        // Register new biome
        ForgeBiome biome = new ForgeBiome(biomeConfig, new BiomeGenCustom(biomeConfig, biomeIds));
        return biome;
    }

    /**
     * Registers the biome to the biome registry.
     * @param forgeBiome The biome.
     */
    public static void registerBiome(ForgeBiome forgeBiome)
    {
        FMLControlledNamespacedRegistry<BiomeGenBase> registry = GameData.getBiomeRegistry();
        ResourceLocation name;
        if (forgeBiome.isCustom())
        {
            name = new ResourceLocation(PluginStandardValues.PLUGIN_NAME, StringHelper.toComputerFriendlyName(forgeBiome.getName()));
        } else {
            BiomeGenBase currentlyRegistered = registry.getObjectById(forgeBiome.biomeIds.getSavedId());
            name = registry.getNameForObject(currentlyRegistered);
        }
        registry.register(forgeBiome.biomeIds.getSavedId(), name, forgeBiome.biomeBase);
    }

    private ForgeBiome(BiomeConfig biomeConfig, BiomeGenCustom biome)
    {
        this.biomeBase = biome;
        this.biomeIds = new BiomeIds(biome.generationId, BiomeGenBase.getIdForBiome(biome));
        this.biomeConfig = biomeConfig;
    }

    @Override
    public boolean isCustom()
    {
        return true;
    }

    @Override
    public String getName()
    {
        return biomeBase.getBiomeName();
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
