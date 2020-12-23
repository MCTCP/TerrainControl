package com.pg85.otg.spigot.biome;

import com.pg85.otg.config.standard.MojangSettings;
import com.pg85.otg.spigot.materials.SpigotMaterialData;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.materials.LocalMaterialData;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;

import java.util.List;

public class SpigotMojangSettings implements MojangSettings
{
	private final BiomeBase biomeBase;

	/**
	 * Creates an instance that provides access to the default
	 * settings of the vanilla biome with the given id.
	 */
	public static MojangSettings fromId (int biomeId)
	{
		// getKeyFromId() -> a()
		ResourceKey<BiomeBase> baseBiomeRegistryKey = BiomeRegistry.a(biomeId);
		if (baseBiomeRegistryKey != null)
		{
			//TODO: Check that this works...
			IRegistryWritable<BiomeBase> biome_registry = ((CraftServer) Bukkit.getServer()).getServer().customRegistry.b(IRegistry.ay);
			BiomeBase biomeBase = biome_registry.a(baseBiomeRegistryKey);
			return fromBiomeBase(biomeBase);
		}
		return null;
	}

	private static MojangSettings fromBiomeBase (BiomeBase biomeBase)
	{
		return new SpigotMojangSettings(biomeBase);
	}

	private SpigotMojangSettings (BiomeBase biomeBase)
	{
		this.biomeBase = biomeBase;
	}

	@Override
	public float getTemperature ()
	{
		// TODO: Implement this?
		return 0;
	}

	@Override
	public float getWetness ()
	{
		// TODO: Implement this?
		//biomeBase.getHumidity();
		return 0;
	}

	@Override
	public float getSurfaceHeight ()
	{
		// TODO: Implement this?
		return 0;
	}

	@Override
	public float getSurfaceVolatility ()
	{
		// TODO: Implement this?
		return 0;
	}

	@Override
	public LocalMaterialData getSurfaceBlock ()
	{
		// Forge: this.biomeBase.getGenerationSettings().getSurfaceBuilderConfig().getTop()
		return SpigotMaterialData.ofBlockData(this.biomeBase.e().e().a());
	}

	@Override
	public LocalMaterialData getGroundBlock ()
	{
		// Forge: this.biomeBase.getGenerationSettings().getSurfaceBuilderConfig().getUnder()
		return SpigotMaterialData.ofBlockData(this.biomeBase.e().e().b());
	}

	@Override
	public List<WeightedMobSpawnGroup> getMobSpawnGroup (EntityCategory entityCategory)
	{
		// TODO: Implement this?
		return null;
	}
}
