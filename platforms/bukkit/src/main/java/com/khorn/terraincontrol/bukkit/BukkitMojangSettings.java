package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.bukkit.util.MobSpawnGroupHelper;
import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;
import com.khorn.terraincontrol.configuration.standard.MojangSettings;
import net.minecraft.server.v1_12_R1.BiomeBase;

import java.util.List;

/**
 * Gets some default settings from the BiomeBase instance. The settings in the
 * BiomeBase instance are provided by Mojang.
 * 
 * @see MojangSettings
 */
public final class BukkitMojangSettings implements MojangSettings
{
    private final BiomeBase biomeBase;

    /**
     * Creates an instance that provides access to the default settings of the
     * vanilla biome with the given id.
     * 
     * @param biomeId The id of the biome.
     * @return The settings.
     */
    public static MojangSettings fromId(int biomeId)
    {
        return fromBiomeBase(BiomeBase.getBiome(biomeId));
    }

    /**
     * Creates an instance that provides access to the default settings of the
     * vanilla biome.
     * 
     * @param biomeBase The biome.
     * @return The settings.
     */
    public static MojangSettings fromBiomeBase(BiomeBase biomeBase)
    {
        return new BukkitMojangSettings(biomeBase);
    }

    private BukkitMojangSettings(BiomeBase biomeBase)
    {
        this.biomeBase = biomeBase;
    }

    @Override
    public float getTemperature()
    {
        return biomeBase.getTemperature();
    }

    @Override
    public float getWetness()
    {
        return biomeBase.getHumidity();
    }

    @Override
    public float getSurfaceHeight()
    {
        return biomeBase.j();
    }

    @Override
    public float getSurfaceVolatility()
    {
        return biomeBase.m();
    }

    @Override
    public LocalMaterialData getSurfaceBlock()
    {
        return BukkitMaterialData.ofMinecraftBlockData(biomeBase.q);
    }

    @Override
    public LocalMaterialData getGroundBlock()
    {
        return BukkitMaterialData.ofMinecraftBlockData(biomeBase.r);
    }

    @Override
    public List<WeightedMobSpawnGroup> getMobSpawnGroup(EntityCategory mobType)
    {
        return MobSpawnGroupHelper.getListFromMinecraftBiome(biomeBase, mobType);
    }

}
