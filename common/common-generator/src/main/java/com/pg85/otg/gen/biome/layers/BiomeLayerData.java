package com.pg85.otg.gen.biome.layers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.pg85.otg.constants.SettingsEnums.BiomeMode;
import com.pg85.otg.gen.biome.NewBiomeData;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IWorldConfig;

/**
 * Class to hold biome layer data until world configs are working. This class will eventually be removed.
 */
public class BiomeLayerData
{
	public final BiomeMode biomeMode;
	public final int generationDepth;
	public final int landSize;
	public final int landFuzzy;
	public final int landRarity;
	public final double frozenOceanTemperature;
	public final int biomeRarityScale;
	public final NewBiomeData oceanBiomeData;
	public final Map<Integer, List<NewBiomeGroup>> groups = new HashMap<>();
	public final List<Integer> biomeDepths = new ArrayList<>(); // Depths with biomes
	public final Map<Integer, NewBiomeGroup> groupRegistry = new HashMap<>();
	public final Map<Integer, List<NewBiomeData>> isleBiomesAtDepth = new HashMap<>();
	public final Map<Integer, List<NewBiomeData>> borderBiomesAtDepth = new HashMap<>();
	public final boolean freezeGroups;
	
	public BiomeLayerData(BiomeLayerData data)
	{
		this.biomeMode = data.biomeMode;
		this.generationDepth = data.generationDepth;
		this.landSize = data.landSize;
		this.landFuzzy = data.landFuzzy;
		this.landRarity = data.landRarity;
		this.frozenOceanTemperature = data.frozenOceanTemperature;
		this.biomeRarityScale = data.biomeRarityScale;
		this.freezeGroups = data.freezeGroups;
		this.oceanBiomeData = data.oceanBiomeData.clone();
		for(Integer integer : data.biomeDepths)
		{
			this.biomeDepths.add(integer.intValue());
		}
		for(Entry<Integer, List<NewBiomeGroup>> entry : data.groups.entrySet())
		{
			if(entry.getValue() != null)
			{
				List<NewBiomeGroup> clonedList = new ArrayList<>();
				for(NewBiomeGroup group : entry.getValue())
				{
					NewBiomeGroup clonedGroup = group.clone();
					this.groupRegistry.put(clonedGroup.id, clonedGroup);
					clonedList.add(clonedGroup);
				}
				this.groups.put(entry.getKey().intValue(), clonedList);
			} else {
				this.groups.put(entry.getKey().intValue(), null);
			}
		}		
		for(Entry<Integer, List<NewBiomeData>> entry : data.isleBiomesAtDepth.entrySet())
		{
			if(entry.getValue() != null)
			{
				List<NewBiomeData> clonedList = new ArrayList<>();
				for(NewBiomeData biome : entry.getValue())
				{
					clonedList.add(biome.clone());
				}
				this.isleBiomesAtDepth.put(entry.getKey().intValue(), clonedList);
			} else {
				this.isleBiomesAtDepth.put(entry.getKey().intValue(), null);
			}
		}
	}
	
	public BiomeLayerData(IWorldConfig worldConfig, IBiomeConfig oceanBiomeConfig)
	{
		this.biomeMode = worldConfig.getBiomeMode();
		this.generationDepth = worldConfig.getGenerationDepth();
		this.landSize = worldConfig.getLandSize();
		this.landFuzzy = worldConfig.getLandFuzzy();
		this.landRarity = worldConfig.getLandRarity();
		this.oceanBiomeData = new NewBiomeData(0, oceanBiomeConfig.getName(), oceanBiomeConfig.getBiomeRarity(), oceanBiomeConfig.getBiomeSize(), oceanBiomeConfig.getBiomeTemperature(), oceanBiomeConfig.getIsleInBiomes());
		this.frozenOceanTemperature = worldConfig.getFrozenOceanTemperature();
		this.biomeRarityScale = worldConfig.getBiomeRarityScale();
		this.freezeGroups = worldConfig.getIsFreezeGroups();
	}

	public void init(Set<Integer> biomeDepths, Map<Integer, List<NewBiomeGroup>> groupDepth, Map<Integer, List<NewBiomeData>> isleBiomesAtDepth, Map<String, Integer> worldBiomes)
	{		
		this.biomeDepths.addAll(biomeDepths);
		this.groups.putAll(groupDepth);
		this.isleBiomesAtDepth.putAll(isleBiomesAtDepth);
				
		for(Entry<Integer, List<NewBiomeGroup>> entry : this.groups.entrySet())
		{
			if(entry.getValue() != null)
			{
				for(NewBiomeGroup group : entry.getValue())
				{
					group.init(worldBiomes);
				}
			}
		}

		for(Entry<Integer, List<NewBiomeData>> entry : this.isleBiomesAtDepth.entrySet())
		{
			if(entry.getValue() != null)
			{
				for(NewBiomeData biome : entry.getValue())
				{
					biome.init(worldBiomes);
				}
			}
		}		
	}
}
