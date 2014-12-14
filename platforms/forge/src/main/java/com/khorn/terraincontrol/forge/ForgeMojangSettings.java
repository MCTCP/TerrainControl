package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;
import com.khorn.terraincontrol.configuration.standard.MojangSettings;
import com.khorn.terraincontrol.forge.util.MobSpawnGroupHelper;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.List;

/**
 * Gets some default settings from the BiomeBase instance. The settings in the
 * BiomeBase instance are provided by Mojang.
 * 
 * @see MojangSettings
 */
public final class ForgeMojangSettings implements MojangSettings
{
    private final BiomeGenBase biomeBase;

    /**
     * Creates an instance that provides access to the default settings of the
     * vanilla biome with the given id.
     * 
     * @param biomeId The id of the biome.
     * @return The settings.
     */
    public static MojangSettings fromId(int biomeId)
    {
        return fromBiomeBase(BiomeGenBase.getBiome(biomeId));
    }

    /**
     * Creates an instance that provides access to the default settings of the
     * vanilla biome.
     * 
     * @param biomeBase The biome.
     * @return The settings.
     */
    public static MojangSettings fromBiomeBase(BiomeGenBase biomeBase)
    {
        return new ForgeMojangSettings(biomeBase);
    }

    private ForgeMojangSettings(BiomeGenBase biomeBase)
    {
        this.biomeBase = biomeBase;
    }

    @Override
    public float getTemperature()
    {
        return biomeBase.temperature;
    }

    @Override
    public float getWetness()
    {
        return biomeBase.rainfall;
    }

    @Override
    public float getSurfaceHeight()
    {
        return biomeBase.minHeight;
    }

    @Override
    public float getSurfaceVolatility()
    {
        return biomeBase.maxHeight;
    }

    @Override
    public LocalMaterialData getSurfaceBlock()
    {
        return ForgeMaterialData.ofMinecraftBlockState(biomeBase.topBlock);
    }

    @Override
    public LocalMaterialData getGroundBlock()
    {
        return ForgeMaterialData.ofMinecraftBlockState(biomeBase.fillerBlock);
    }

    @Override
    public List<WeightedMobSpawnGroup> getMobSpawnGroup(EntityCategory entityCategory)
    {
        return MobSpawnGroupHelper.getListFromMinecraftBiome(biomeBase, entityCategory);
    }

}
