package com.pg85.otg.forge;

import com.pg85.otg.LocalMaterialData;
import com.pg85.otg.configuration.WeightedMobSpawnGroup;
import com.pg85.otg.configuration.standard.MojangSettings;
import com.pg85.otg.forge.util.MobSpawnGroupHelper;

import net.minecraft.world.biome.Biome;

import java.util.List;

/**
 * Gets some default settings from the BiomeBase instance. The settings in the
 * BiomeBase instance are provided by Mojang.
 * 
 * @see MojangSettings
 */
public final class ForgeMojangSettings implements MojangSettings
{
    private final Biome biomeBase;

    /**
     * Creates an instance that provides access to the default settings of the
     * vanilla biome with the given id.
     * 
     * @param biomeId The id of the biome.
     * @return The settings.
     */
    public static MojangSettings fromId(int biomeId)
    {
    	Biome baseBiome = Biome.getBiome(biomeId);
    	if(baseBiome != null)
    	{
    		return fromBiomeBase(baseBiome);
    	}  	
    	return fromBiomeBase(ForgeWorld.vanillaBiomes[biomeId]);
    }

    /**
     * Creates an instance that provides access to the default settings of the
     * vanilla biome.
     * 
     * @param biomeBase The biome.
     * @return The settings.
     */
    public static MojangSettings fromBiomeBase(Biome biomeBase)
    {
        return new ForgeMojangSettings(biomeBase);
    }

    private ForgeMojangSettings(Biome biomeBase)
    {
        this.biomeBase = biomeBase;
    }

    @Override
    public float getTemperature()
    {
        return this.biomeBase.getTemperature();
    }

    @Override
    public float getWetness()
    {
        return this.biomeBase.getRainfall();
    }

    @Override
    public float getSurfaceHeight()
    {
    	return this.biomeBase.getBaseHeight();
    }

    @Override
    public float getSurfaceVolatility()
    {
        return this.biomeBase.getHeightVariation();
    }

    @Override
    public LocalMaterialData getSurfaceBlock()
    {
        return ForgeMaterialData.ofMinecraftBlockState(this.biomeBase.topBlock);
    }

    @Override
    public LocalMaterialData getGroundBlock()
    {
        return ForgeMaterialData.ofMinecraftBlockState(this.biomeBase.fillerBlock);
    }

    @Override
    public List<WeightedMobSpawnGroup> getMobSpawnGroup(EntityCategory entityCategory)
    {
        return MobSpawnGroupHelper.getListFromMinecraftBiome(this.biomeBase, entityCategory);
    }

}
