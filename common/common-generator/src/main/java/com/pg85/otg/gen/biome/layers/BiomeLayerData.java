package com.pg85.otg.gen.biome.layers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.pg85.otg.constants.SettingsEnums.BiomeMode;
import com.pg85.otg.gen.biome.NewBiomeData;

/**
 * Class to hold biome layer data until world configs are working. This class will eventually be removed.
 */
public class BiomeLayerData
{
	public BiomeMode biomeMode = BiomeMode.Normal;
	public int generationDepth = 10;
	public int landSize = 0;
	public int landFuzzy = 5;
	public int landRarity = 99;
	public double frozenOceanTemperature;
	public NewBiomeData oceanBiomeData;
	public Map<Integer, List<NewBiomeGroup>> groups = new HashMap<>();
	public List<Integer> biomeDepths = new ArrayList<>(); // Depths with biomes
	public Map<Integer, NewBiomeGroup> groupRegistry = new HashMap<>();
	public boolean freezeGroups;
	
	public BiomeLayerData clone()
	{
		BiomeLayerData clone = new BiomeLayerData();
		clone.biomeMode = this.biomeMode;
		clone.generationDepth = this.generationDepth;
		clone.landSize = this.landSize;
		clone.landFuzzy = this.landFuzzy;
		clone.landRarity = this.landRarity;
		clone.frozenOceanTemperature = this.frozenOceanTemperature;
		clone.freezeGroups = this.freezeGroups;
		clone.oceanBiomeData = this.oceanBiomeData.clone();
		clone.biomeDepths = new ArrayList<>();
		for(Integer integer : this.biomeDepths)
		{
			clone.biomeDepths.add(integer.intValue());
		}
		clone.groups = new HashMap<>();
		clone.groupRegistry = new HashMap<>();
		for(Entry<Integer, List<NewBiomeGroup>> entry : this.groups.entrySet())
		{
			if(entry.getValue() != null)
			{
				List<NewBiomeGroup> clonedList = new ArrayList<>();
				for(NewBiomeGroup group : entry.getValue())
				{
					NewBiomeGroup clonedGroup = group.clone();
					clone.groupRegistry.put(clonedGroup.id, clonedGroup);
					clonedList.add(clonedGroup);
				}
				clone.groups.put(entry.getKey().intValue(), clonedList);
			} else {
				clone.groups.put(entry.getKey().intValue(), null);
			}
		}
		
		return clone;
	}
}
