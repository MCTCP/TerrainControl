package com.pg85.otg.gen.biome.layers;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.pg85.otg.constants.SettingsEnums.BiomeMode;
import com.pg85.otg.constants.SettingsEnums.ImageMode;
import com.pg85.otg.constants.SettingsEnums.ImageOrientation;
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
	public final int[] cumulativeGroupRarities;
	public final int[] groupMaxRarityPerDepth;
	public final List<Integer> biomeDepths = new ArrayList<>(); // Depths with biomes
	public final Map<Integer, NewBiomeGroup> groupRegistry = new HashMap<>();
	public final Map<Integer, List<NewBiomeData>> isleBiomesAtDepth = new HashMap<>();
	public final Map<Integer, List<NewBiomeData>> borderBiomesAtDepth = new HashMap<>();
	public int[] riverBiomes;
	public final boolean freezeGroups;
	public final boolean riversEnabled;
	public final boolean randomRivers;
	public final int riverRarity;
	public final int riverSize;

	// FromImageMode
	public HashMap<Integer, Integer> biomeColorMap;
	public final int imageXOffset;
	public final int imageZOffset;
	public final ImageMode imageMode;
	public final String configImageFillBiome;
	public int imageFillBiome;
	public final Path presetDir;
	public final String imageFile;
	public final ImageOrientation imageOrientation;

	// TODO: The only reason we're cloning BiomeLayerData and NewBiomeData
	// is because NewBiomeData.totalGroupRarity is used and modified across 
	// all generation depths, and we want loaded presets to remain unmodified.
	// totalGroupRarity is the only setting affected though, so technically 
	// speaking we don't have to clone the others, just doing it for completeness. 
	// Re-design this? (any solution will have some warts)
	public BiomeLayerData(BiomeLayerData data)
	{
		this.biomeColorMap = new HashMap<>();
		for(Entry<Integer, Integer> entry : data.biomeColorMap.entrySet())
		{
			this.biomeColorMap.put(entry.getKey().intValue(), entry.getValue().intValue());
		}
		this.imageXOffset = data.imageXOffset;
		this.imageZOffset = data.imageZOffset;
		this.imageMode = data.imageMode;
		this.configImageFillBiome = data.configImageFillBiome;
		this.imageFillBiome = data.imageFillBiome;
		this.presetDir = data.presetDir;
		this.imageFile = data.imageFile;
		this.imageOrientation = data.imageOrientation;
		
		this.biomeMode = data.biomeMode;
		this.generationDepth = data.generationDepth;
		this.landSize = data.landSize;
		this.landFuzzy = data.landFuzzy;
		this.landRarity = data.landRarity;
		this.frozenOceanTemperature = data.frozenOceanTemperature;
		this.biomeRarityScale = data.biomeRarityScale;
		this.freezeGroups = data.freezeGroups;
		this.randomRivers = data.randomRivers;
		this.riverRarity = data.riverRarity;
		this.riverSize = data.riverSize;
		this.riversEnabled = data.riversEnabled;
		this.oceanBiomeData = data.oceanBiomeData.clone();
		this.biomeDepths.addAll(data.biomeDepths);
		this.cumulativeGroupRarities = data.cumulativeGroupRarities.clone();
		this.groupMaxRarityPerDepth = data.groupMaxRarityPerDepth.clone();
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
		for(Entry<Integer, List<NewBiomeData>> entry : data.borderBiomesAtDepth.entrySet())
		{
			if(entry.getValue() != null)
			{
				List<NewBiomeData> clonedList = new ArrayList<>();
				for(NewBiomeData biome : entry.getValue())
				{
					clonedList.add(biome.clone());
				}
				this.borderBiomesAtDepth.put(entry.getKey().intValue(), clonedList);
			} else {
				this.borderBiomesAtDepth.put(entry.getKey().intValue(), null);
			}
		}
		
		this.riverBiomes = data.riverBiomes.clone();
	}
	
	public BiomeLayerData(Path presetDir, IWorldConfig worldConfig, IBiomeConfig oceanBiomeConfig)
	{
		this.imageXOffset = worldConfig.getImageXOffset();
		this.imageZOffset = worldConfig.getImageZOffset();
		this.imageMode = worldConfig.getImageMode();
		this.configImageFillBiome = worldConfig.getImageFillBiome();
		this.presetDir = presetDir;
		this.imageFile = worldConfig.getImageFile();
		this.imageOrientation = worldConfig.getImageOrientation();
		
		this.biomeMode = worldConfig.getBiomeMode();
		this.generationDepth = worldConfig.getGenerationDepth();
		this.landSize = worldConfig.getLandSize();
		this.landFuzzy = worldConfig.getLandFuzzy();
		this.landRarity = worldConfig.getLandRarity();

		this.cumulativeGroupRarities = new int[this.generationDepth];
		this.groupMaxRarityPerDepth = new int[this.generationDepth];

		if (oceanBiomeConfig == null)
		{
			this.oceanBiomeData = new NewBiomeData(0, "none", 0, 0, 0, ImmutableList.of(), ImmutableList.of(), ImmutableList.of());
		} else
		{
			this.oceanBiomeData = new NewBiomeData(0, oceanBiomeConfig.getName(), oceanBiomeConfig.getBiomeRarity(), oceanBiomeConfig.getBiomeSize(), oceanBiomeConfig.getBiomeTemperature(), oceanBiomeConfig.getIsleInBiomes(), oceanBiomeConfig.getBorderInBiomes(), oceanBiomeConfig.getNotBorderNearBiomes());
		}

		this.frozenOceanTemperature = worldConfig.getFrozenOceanTemperature();
		this.biomeRarityScale = worldConfig.getBiomeRarityScale();
		this.freezeGroups = worldConfig.getIsFreezeGroups();
		this.randomRivers = worldConfig.getIsRandomRivers();
		this.riverRarity = worldConfig.getRiverRarity();
		this.riverSize = worldConfig.getRiverSize();
		this.riversEnabled = worldConfig.getRiversEnabled();
	}

	public void init(Set<Integer> biomeDepths, Map<Integer, List<NewBiomeGroup>> groupDepth, Map<Integer, List<NewBiomeData>> isleBiomesAtDepth, Map<Integer, List<NewBiomeData>> borderBiomesAtDepth, Map<String, Integer> biomeIdsByName, HashMap<Integer, Integer> biomeColorMap, IBiomeConfig[] biomes)
	{		
		this.biomeDepths.addAll(biomeDepths);
		this.groups.putAll(groupDepth);
		this.isleBiomesAtDepth.putAll(isleBiomesAtDepth);
		this.borderBiomesAtDepth.putAll(borderBiomesAtDepth);
				
		for(Entry<Integer, List<NewBiomeGroup>> entry : this.groups.entrySet())
		{
			if(entry.getValue() != null)
			{
				int cumulativeRarity = 0;
				for(NewBiomeGroup group : entry.getValue())
				{
					group.init(biomeIdsByName);
					cumulativeRarity += group.rarity;
				}
				this.cumulativeGroupRarities[entry.getKey()] = cumulativeRarity;
			}
		}
		for (int depth = 0; depth < this.cumulativeGroupRarities.length; depth++)
		{
			for (int j = depth; j < this.cumulativeGroupRarities.length; j++)
			{
				this.groupMaxRarityPerDepth[depth] += this.cumulativeGroupRarities[j];
			}
		}

		for(Entry<Integer, List<NewBiomeData>> entry : this.isleBiomesAtDepth.entrySet())
		{
			if(entry.getValue() != null)
			{
				for(NewBiomeData biome : entry.getValue())
				{
					biome.init(biomeIdsByName);
				}
			}
		}
		
		for(Entry<Integer, List<NewBiomeData>> entry : this.borderBiomesAtDepth.entrySet())
		{
			if(entry.getValue() != null)
			{
				for(NewBiomeData biome : entry.getValue())
				{
					biome.init(biomeIdsByName);
				}
			}
		}
		
		this.biomeColorMap = biomeColorMap;		
		this.riverBiomes = new int[biomes.length];
		for(IBiomeConfig biomeConfig : biomes)
		{
			this.riverBiomes[biomeIdsByName.get(biomeConfig.getName())] = biomeIdsByName.getOrDefault(biomeConfig.getRiverBiome(), -1);
		}
	}
}
