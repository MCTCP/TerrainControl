package com.pg85.otg.presets;

import java.util.ArrayList;
import java.util.HashMap;

import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.world.WorldConfig;

public class Preset
{
	private final String name;
	private final WorldConfig worldConfig;
	private final HashMap<String, BiomeConfig> biomeConfigs = new HashMap<String, BiomeConfig>();

	Preset(String name, WorldConfig worldConfig, ArrayList<BiomeConfig> biomeConfigs)
	{
		this.name = name;
		this.worldConfig = worldConfig;
		for(BiomeConfig biomeConfig : biomeConfigs)
		{
			this.biomeConfigs.put(biomeConfig.getName(), biomeConfig);
		}
	}

	public String getName()
	{
		return this.name;
	}

	public WorldConfig getWorldConfig()
	{
		return this.worldConfig;
	}

	public BiomeConfig getBiomeConfig(String biomeName)
	{
		return this.biomeConfigs.get(biomeName);
	}

	public ArrayList<BiomeConfig> getAllBiomeConfigs()
	{
		return new ArrayList<BiomeConfig>(this.biomeConfigs.values());
	}	
}