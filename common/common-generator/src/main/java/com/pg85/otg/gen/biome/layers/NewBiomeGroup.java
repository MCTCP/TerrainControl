package com.pg85.otg.gen.biome.layers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.gen.biome.NewBiomeData;

/**
 * New biome group data for testing purposes
 */
public class NewBiomeGroup
{
	public int id = 0;
	public int rarity = 0;
	public List<NewBiomeData> biomes = new ArrayList<>();	
	
	public float avgTemp = 0;
	public boolean isColdGroup() 
	{
		return this.avgTemp < Constants.ICE_GROUP_MAX_TEMP;
	}
	
	 // Used for BeforeGroups
	public int totalGroupRarity;

	// Used for NormalMode. Both of these arrays should be initialized to genDepth
	// int array with the total depth rarity per depth of this biome group
	public int[] totalDepthRarity;

	// int array of the max rarity at a given depth in the biome group
	// max rarity is a sum of the total rarity of this depth and all subsequent depths
	public int[] maxRarityPerDepth;
	
	public void init(Map<String, Integer> worldIsleBiomes)
	{
		for(NewBiomeData biomeData : this.biomes)
		{
			biomeData.init(worldIsleBiomes);
		}
	}
	
	public NewBiomeGroup clone()
	{
		NewBiomeGroup clone = new NewBiomeGroup();
		clone.id = this.id;
		clone.rarity = this.rarity;
		clone.biomes = new ArrayList<>();
		for(NewBiomeData biomeData : this.biomes)
		{
			clone.biomes.add(biomeData.clone());
		}
		clone.avgTemp = this.avgTemp;
		clone.totalGroupRarity = this.totalGroupRarity;
		clone.maxRarityPerDepth = maxRarityPerDepth.clone();
		clone.totalDepthRarity = totalDepthRarity.clone();
		return clone;
	}
}
