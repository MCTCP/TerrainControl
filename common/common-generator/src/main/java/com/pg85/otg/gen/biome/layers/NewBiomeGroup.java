package com.pg85.otg.gen.biome.layers;

import java.util.ArrayList;
import java.util.List;

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
}
