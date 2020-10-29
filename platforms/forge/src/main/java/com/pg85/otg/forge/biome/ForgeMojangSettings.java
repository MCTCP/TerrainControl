package com.pg85.otg.forge.biome;

import com.pg85.otg.common.materials.LocalMaterialData;
import com.pg85.otg.configuration.biome.settings.WeightedMobSpawnGroup;
import com.pg85.otg.configuration.standard.MojangSettings;
import com.pg85.otg.forge.materials.ForgeMaterialData;

import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeRegistry;
import net.minecraftforge.registries.ForgeRegistries;

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
     * Creates an instance that provides access to the default 
     * settings of the vanilla biome with the given id.
     */
    public static MojangSettings fromId(int biomeId)
    {
    	RegistryKey<Biome> baseBiomeRegistryKey = BiomeRegistry.func_244203_a(biomeId);
    	if(baseBiomeRegistryKey != null)
    	{
    		Biome biome = ForgeRegistries.BIOMES.getValue(baseBiomeRegistryKey.func_240901_a_());    		
    		return fromBiomeBase(biome);
    	}
    	return null;
    }

    private static MojangSettings fromBiomeBase(Biome biomeBase)
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
        return 0.0f;//this.biomeBase.getDefaultTemperature();
    }

    @Override
    public float getWetness()
    {
        return 0.0f;//this.biomeBase.getRainfall();
    }

    @Override
    public float getSurfaceHeight()
    {
    	return 0.0f;//this.biomeBase.getBaseHeight();
    }

    @Override
    public float getSurfaceVolatility()
    {
        return 0.0f;//this.biomeBase.getHeightVariation();
    }

    @Override
    public LocalMaterialData getSurfaceBlock()
    {
        return ForgeMaterialData.ofMinecraftBlockState(this.biomeBase.func_242440_e().func_242502_e().getTop());
    }

    @Override
    public LocalMaterialData getGroundBlock()
    {
        return ForgeMaterialData.ofMinecraftBlockState(this.biomeBase.func_242440_e().func_242502_e().getUnder());
    }

    @Override
    public List<WeightedMobSpawnGroup> getMobSpawnGroup(EntityCategory entityCategory)
    {
        return null;//MobSpawnGroupHelper.getListFromMinecraftBiome(this.biomeBase, entityCategory);
    }
}
