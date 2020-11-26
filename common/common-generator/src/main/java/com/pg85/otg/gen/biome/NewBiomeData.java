package com.pg85.otg.gen.biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NewBiomeData
{
	public final int id;
	public final String name;
	public final int rarity;
	public final int biomeSize;
	public float biomeTemperature;
	private final List<String> configIslesInBiome;
	public final List<Integer> islesInBiome = new ArrayList<Integer>();

	public NewBiomeData(int id, String name, int rarity, int biomeSize, float biomeTemperature, List<String> configIslesInBiome)
	{
		this.id = id;
		this.name = name;
		this.rarity = rarity;
		this.biomeSize = biomeSize;
		this.biomeTemperature = biomeTemperature;
		this.configIslesInBiome = configIslesInBiome;
	}
	
	public void init(Map<String, Integer> worldBiomes)
	{
		for(String isleInBiomeName : this.configIslesInBiome)
		{
			Integer isleBiome = worldBiomes.get(isleInBiomeName);
			if(isleBiome != null)
			{
				this.islesInBiome.add(isleBiome.intValue());
			}
		}
	}
	
	public NewBiomeData clone()
	{
		NewBiomeData clone = new NewBiomeData(this.id, this.name, this.rarity, this.biomeSize, this.biomeTemperature, new ArrayList<>(this.configIslesInBiome));		
		for(Integer integer : this.islesInBiome)
		{
			clone.islesInBiome.add(integer.intValue());
		}
		return clone;
	}
}
