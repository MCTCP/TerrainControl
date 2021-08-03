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
import com.pg85.otg.gen.biome.BiomeData;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.IWorldConfig;

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
	public final int oceanBiomeSize;
	public final int[] oceanTemperatures;
	public final double frozenOceanTemperature;
	public final int biomeRarityScale;
	public final BiomeData oceanBiomeData;
	public final Map<Integer, List<NewBiomeGroup>> groups = new HashMap<>();
	public final int[] cumulativeGroupRarities;
	public final int[] groupMaxRarityPerDepth;
	public final boolean oldGroupRarity;
	public final List<Integer> biomeDepths = new ArrayList<>(); // Depths with biomes
	public final Map<Integer, NewBiomeGroup> groupRegistry = new HashMap<>();
	public final Map<Integer, List<BiomeData>> isleBiomesAtDepth = new HashMap<>();
	public final Map<Integer, List<BiomeData>> borderBiomesAtDepth = new HashMap<>();
	public int[] riverBiomes;
	public final boolean riversEnabled;
	public final boolean randomRivers;
	public final int riverDepth;
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
		this.oceanBiomeSize = data.oceanBiomeSize;
		this.frozenOceanTemperature = data.frozenOceanTemperature;
		this.biomeRarityScale = data.biomeRarityScale;
		this.randomRivers = data.randomRivers;
		this.riverDepth = data.riverDepth;
		this.riverSize = data.riverSize;
		this.riversEnabled = data.riversEnabled;
		this.oceanBiomeData = data.oceanBiomeData.clone();
		this.biomeDepths.addAll(data.biomeDepths);
		this.cumulativeGroupRarities = data.cumulativeGroupRarities.clone();
		this.groupMaxRarityPerDepth = data.groupMaxRarityPerDepth.clone();
		this.oldGroupRarity = data.oldGroupRarity;

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
		for(Entry<Integer, List<BiomeData>> entry : data.isleBiomesAtDepth.entrySet())
		{
			if(entry.getValue() != null)
			{
				List<BiomeData> clonedList = new ArrayList<>();
				for(BiomeData biome : entry.getValue())
				{
					clonedList.add(biome.clone());
				}
				this.isleBiomesAtDepth.put(entry.getKey().intValue(), clonedList);
			} else {
				this.isleBiomesAtDepth.put(entry.getKey().intValue(), null);
			}
		}
		for(Entry<Integer, List<BiomeData>> entry : data.borderBiomesAtDepth.entrySet())
		{
			if(entry.getValue() != null)
			{
				List<BiomeData> clonedList = new ArrayList<>();
				for(BiomeData biome : entry.getValue())
				{
					clonedList.add(biome.clone());
				}
				this.borderBiomesAtDepth.put(entry.getKey().intValue(), clonedList);
			} else {
				this.borderBiomesAtDepth.put(entry.getKey().intValue(), null);
			}
		}
		
		this.riverBiomes = data.riverBiomes.clone();

		this.oceanTemperatures = new int[4];
		System.arraycopy(data.oceanTemperatures, 0, this.oceanTemperatures, 0, 4);
	}
	
	public BiomeLayerData(Path presetDir, IWorldConfig worldConfig, IBiomeConfig oceanBiomeConfig, int[] oceanTemperatures)
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
		this.oceanBiomeSize = worldConfig.getOceanBiomeSize();
		this.oceanTemperatures = oceanTemperatures;

		this.cumulativeGroupRarities = new int[this.generationDepth + 1];
		this.groupMaxRarityPerDepth = new int[this.generationDepth + 1];
		this.oldGroupRarity = worldConfig.getOldGroupRarity();

		if (oceanBiomeConfig == null)
		{
			this.oceanBiomeData = new BiomeData(0, 0, 0, 0, ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), ImmutableList.of());
		} else {
			this.oceanBiomeData = new BiomeData(
				0,
				oceanBiomeConfig.getBiomeRarity(), 
				oceanBiomeConfig.getBiomeSize(), 
				oceanBiomeConfig.getBiomeTemperature(), 
				oceanBiomeConfig.getIsleInBiomes(), 
				oceanBiomeConfig.getBorderInBiomes(), 
				oceanBiomeConfig.getOnlyBorderNearBiomes(), 
				oceanBiomeConfig.getNotBorderNearBiomes()
			);
		}

		this.frozenOceanTemperature = worldConfig.getFrozenOceanTemperature();
		this.biomeRarityScale = worldConfig.getBiomeRarityScale();
		this.randomRivers = worldConfig.getIsRandomRivers();
		this.riverDepth = worldConfig.getRiverRarity();
		this.riverSize = worldConfig.getRiverSize();
		this.riversEnabled = worldConfig.getRiversEnabled();
	}

	public void init(Set<Integer> biomeDepths, Map<Integer, List<NewBiomeGroup>> groupDepth, Map<Integer, List<BiomeData>> isleBiomesAtDepth, Map<Integer, List<BiomeData>> borderBiomesAtDepth, Map<String, List<Integer>> biomeIdsByName, HashMap<Integer, Integer> biomeColorMap, IBiome[] biomes)
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

		for(Entry<Integer, List<BiomeData>> entry : this.isleBiomesAtDepth.entrySet())
		{
			if(entry.getValue() != null)
			{
				for(BiomeData biome : entry.getValue())
				{
					biome.init(biomeIdsByName);
				}
			}
		}
		
		for(Entry<Integer, List<BiomeData>> entry : this.borderBiomesAtDepth.entrySet())
		{
			if(entry.getValue() != null)
			{
				for(BiomeData biome : entry.getValue())
				{
					biome.init(biomeIdsByName);
				}
			}
		}

		this.biomeColorMap = biomeColorMap;
		this.riverBiomes = new int[biomes.length];
		for(int i = 0; i < biomes.length; i++)
		{			
			this.riverBiomes[i] = biomeIdsByName.getOrDefault(biomes[i].getBiomeConfig().getRiverBiome(), null).get(0);
		}
	}
}
