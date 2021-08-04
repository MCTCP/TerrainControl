package com.pg85.otg.gen.biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BiomeData
{
	public final int id;
	public final int rarity;
	public final int biomeSize;
	public float biomeTemperature;
	private final List<String> configIsleInBiomes;
	private final List<String> configBorderInBiomes;
	private final List<String> configOnlyBorderNearBiomes;	
	private final List<String> configNotBorderNearBiomes;
	public final List<Integer> isleInBiomes = new ArrayList<Integer>();
	public final List<Integer> borderInBiomes = new ArrayList<Integer>();
	public final List<Integer> onlyBorderNearBiomes = new ArrayList<Integer>();
	public final List<Integer> notBorderNearBiomes = new ArrayList<Integer>();

	public BiomeData(int id, int rarity, int biomeSize, float biomeTemperature, List<String> configIsleInBiomes, List<String> configBorderInBiomes, List<String> configOnlyBorderNearBiomes, List<String> configNotBorderNearBiomes)
	{
		this.id = id;
		this.rarity = rarity;
		this.biomeSize = biomeSize;
		this.biomeTemperature = biomeTemperature;
		this.configIsleInBiomes = configIsleInBiomes;
		this.configBorderInBiomes = configBorderInBiomes;
		this.configOnlyBorderNearBiomes = configOnlyBorderNearBiomes;
		this.configNotBorderNearBiomes = configNotBorderNearBiomes;
	}
	
	public void init(Map<String, List<Integer>> worldBiomes)
	{
		// Note: For template biomes targeting multiple biomes this won't work, 
		// since worldBiomes only contains one id for each biome config name.
		for(String isleInBiomeName : this.configIsleInBiomes)
		{
			List<Integer> isleBiomes = worldBiomes.get(isleInBiomeName);
			if(isleBiomes != null)
			{
				for(Integer isleBiome : isleBiomes)
				{
					if(isleBiome != null && !this.isleInBiomes.contains(isleBiome.intValue()))
					{
						this.isleInBiomes.add(isleBiome.intValue());
					}				
				}
			}
		}
		for(String borderInBiomeName : this.configBorderInBiomes)
		{
			List<Integer> borderBiomes = worldBiomes.get(borderInBiomeName);
			if(borderBiomes != null)
			{
				for(Integer borderBiome : borderBiomes)
				{
					if(borderBiome != null && !this.borderInBiomes.contains(borderBiome.intValue()))
					{
						this.borderInBiomes.add(borderBiome.intValue());
					}
				}
			}
		}
		for(String onlyBorderNearBiomeName : this.configOnlyBorderNearBiomes)
		{
			List<Integer> onlyBorderNearBiomes = worldBiomes.get(onlyBorderNearBiomeName);
			if(onlyBorderNearBiomes != null)
			{
				for(Integer onlyBorderNearBiome : onlyBorderNearBiomes)
				{
					if(onlyBorderNearBiome != null && !this.onlyBorderNearBiomes.contains(onlyBorderNearBiome.intValue()))
					{
						this.onlyBorderNearBiomes.add(onlyBorderNearBiome.intValue());
					}
				}
			}
		}
		for(String notBorderNearBiomeName : this.configNotBorderNearBiomes)
		{
			List<Integer> notBorderNearBiomes = worldBiomes.get(notBorderNearBiomeName);
			if(notBorderNearBiomes != null)
			{
				for(Integer notBorderNearBiome : notBorderNearBiomes)
				{
					if(notBorderNearBiome != null && !this.notBorderNearBiomes.contains(notBorderNearBiome.intValue()))
					{
						this.notBorderNearBiomes.add(notBorderNearBiome.intValue());
					}
				}
			}
		}
	}

	public BiomeData clone()
	{
		BiomeData clone = new BiomeData(this.id, this.rarity, this.biomeSize, this.biomeTemperature, new ArrayList<>(this.configIsleInBiomes), new ArrayList<>(this.configBorderInBiomes), new ArrayList<>(this.configOnlyBorderNearBiomes), new ArrayList<>(this.configNotBorderNearBiomes));		
		for(Integer integer : this.isleInBiomes)
		{
			clone.isleInBiomes.add(integer.intValue());
		}
		for(Integer integer : this.borderInBiomes)
		{
			clone.borderInBiomes.add(integer.intValue());
		}
		for(Integer integer : this.onlyBorderNearBiomes)
		{
			clone.onlyBorderNearBiomes.add(integer.intValue());
		}		
		for(Integer integer : this.notBorderNearBiomes)
		{
			clone.notBorderNearBiomes.add(integer.intValue());
		}
		return clone;
	}
}
