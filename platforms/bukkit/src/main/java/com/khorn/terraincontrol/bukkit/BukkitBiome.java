package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.bukkit.util.WorldHelper;
import com.khorn.terraincontrol.configuration.BiomeConfig;
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

    /**
     * Wraps the vanilla biome into a LocalBiome instance.
     *
     * @param biomeConfig Configuration file of the biome.
     * @param biome       The vanilla biome to wrap.
     * @return The wrapped biome.
     */
    public static BukkitBiome forVanillaBiome(BiomeConfig biomeConfig, BiomeBase biome)
    {
        return new BukkitBiome(biomeConfig, biome);
    }

    /**
     * Creates and registers a new custom biome with the config and ids.
     * 
     * @param biomeConfig Config of the custom biome.
     * @param biomeIds    Ids of the custom biome.
     * @return The custom biome.
     */
    public static BukkitBiome forCustomBiome(BiomeConfig biomeConfig, BiomeIds biomeIds)
    {
        return new BukkitBiome(biomeConfig, TXBiomeBase.createInstance(biomeConfig, biomeIds));
    }

    protected BukkitBiome(BiomeConfig biomeConfig, BiomeBase biome)
    {
        this.biomeBase = biome;
        int savedBiomeId =  BiomeBase.a(biomeBase);
        this.biomeIds = new BiomeIds(WorldHelper.getGenerationId(biomeBase), savedBiomeId);
        this.biomeConfig = biomeConfig;
        this.isCustom = biome instanceof TXBiomeBase;
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