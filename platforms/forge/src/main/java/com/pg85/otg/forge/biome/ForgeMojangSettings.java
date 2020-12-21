package com.pg85.otg.forge.biome;

import com.pg85.otg.config.standard.MojangSettings;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.materials.LocalMaterialData;

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
		RegistryKey<Biome> baseBiomeRegistryKey = BiomeRegistry.getKeyFromID(biomeId);
		if(baseBiomeRegistryKey != null)
		{
			Biome biome = ForgeRegistries.BIOMES.getValue(baseBiomeRegistryKey.getLocation());
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
		// TODO: Implement this?
		return 0.0f;//this.biomeBase.getDefaultTemperature();
	}

	@Override
	public float getWetness()
	{
		// TODO: Implement this?		
		return 0.0f;//this.biomeBase.getRainfall();
	}

	@Override
	public float getSurfaceHeight()
	{
		// TODO: Implement this?	
		return 0.0f;//this.biomeBase.getBaseHeight();
	}

	@Override
	public float getSurfaceVolatility()
	{
		// TODO: Implement this?
		return 0.0f;//this.biomeBase.getHeightVariation();
	}

	@Override
	public LocalMaterialData getSurfaceBlock()
	{
		return ForgeMaterialData.ofMinecraftBlockState(this.biomeBase.getGenerationSettings().getSurfaceBuilderConfig().getTop());
	}

	@Override
	public LocalMaterialData getGroundBlock()
	{
		return ForgeMaterialData.ofMinecraftBlockState(this.biomeBase.getGenerationSettings().getSurfaceBuilderConfig().getUnder());
	}

	@Override
	public List<WeightedMobSpawnGroup> getMobSpawnGroup(EntityCategory entityCategory)
	{
		// TODO: Implement this?
		return null;//MobSpawnGroupHelper.getListFromMinecraftBiome(this.biomeBase, entityCategory);
	}
}
