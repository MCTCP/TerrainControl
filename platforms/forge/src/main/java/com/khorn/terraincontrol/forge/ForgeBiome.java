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
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ForgeBiome implements LocalBiome
{
    private final BiomeGenBase biomeBase;
    private final BiomeIds biomeIds;
    private final BiomeConfig biomeConfig;

    /**
     * Creates a new biome with the given name and id.
     * 
     * @param biomeConfig    The config of the biome.
     * @param minecraftBiome The Minecraft instance of the biome.
     * @return The registered biome.
     */
    public static ForgeBiome forBiome(BiomeConfig biomeConfig, BiomeGenBase minecraftBiome)
    {
        return new ForgeBiome(biomeConfig, minecraftBiome);
    }

    /**
     * Registers the biome to the biome registry.
     * @param forgeBiome The biome.
     */
    static void registerBiome(ForgeBiome forgeBiome)
    {
        GameRegistry.findRegistry(BiomeGenBase.class);
        FMLControlledNamespacedRegistry<BiomeGenBase> registry = (FMLControlledNamespacedRegistry<BiomeGenBase>) BiomeGenBase.REGISTRY;
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

    private ForgeBiome(BiomeConfig biomeConfig, BiomeGenBase biome)
    {
        this.biomeBase = biome;
        int savedId = BiomeGenBase.getIdForBiome(biome);
        int generationId = (biome instanceof  BiomeGenCustom)? ((BiomeGenCustom) biome).generationId : savedId;
        this.biomeIds = new BiomeIds(generationId, savedId);
        this.biomeConfig = biomeConfig;
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
