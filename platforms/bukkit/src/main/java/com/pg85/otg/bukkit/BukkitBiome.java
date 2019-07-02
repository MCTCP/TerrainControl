package com.pg85.otg.bukkit;

import com.pg85.otg.bukkit.util.WorldHelper;
import com.pg85.otg.common.BiomeIds;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.configuration.biome.BiomeConfig;

import net.minecraft.server.v1_12_R1.BiomeBase;
import net.minecraft.server.v1_12_R1.BlockPosition;

/**
 * The BukkitBiome is basically a wrapper for the BiomeBase. If you look at
 * the constructor and the method you will see that this is the case.
 */
public class BukkitBiome implements LocalBiome
{
    private final BiomeBase biomeBase;
    private final boolean isCustom;
    private final BiomeIds biomeIds;
    private final BiomeConfig biomeConfig;

    private BukkitBiome(BiomeConfig biomeConfig, BiomeBase biome)
    {
        this.biomeBase = biome;
        int savedBiomeId =  BiomeBase.a(biomeBase);
        this.biomeIds = new BiomeIds(WorldHelper.getOTGBiomeId(biomeBase), savedBiomeId);
        this.biomeConfig = biomeConfig;
        this.isCustom = biome instanceof OTGBiomeBase;
    }
    
    /**
     * Creates and registers a new custom biome with the config and ids.
     *
     * @param biomeConfig Config of the custom biome.
     * @param biomeIds    Ids of the custom biome.
     * @return The custom biome.
     */
    static BukkitBiome forCustomBiome(BiomeConfig biomeConfig, BiomeIds biomeIds, String worldName)
    {
        return new BukkitBiome(biomeConfig, OTGBiomeBase.createInstance(biomeConfig, biomeIds, worldName));
    }

    @Override
    public boolean isCustom()
    {
        return this.isCustom;
    }

    public BiomeBase getHandle()
    {
        return biomeBase;
    }

    @Override
    public String getName()
    {
        return this.biomeConfig.getName();
    }

    @Override
    public BiomeIds getIds()
    {
        return this.biomeIds;
    }

    @Override
    public float getTemperatureAt(int x, int y, int z)
    {
        return this.biomeBase.a(new BlockPosition(x, y, z));
    }

    @Override
    public BiomeConfig getBiomeConfig()
    {
        return this.biomeConfig;
    }

    @Override
    public String toString()
    {
        return getName();
    }
}