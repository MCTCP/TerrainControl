package com.pg85.otg.gen.biome.layers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.pg85.otg.gen.biome.BiomeData;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;
import com.pg85.otg.interfaces.ILayerSampler;

/**
 * Places the biomes at a specific depth, given the biome groups.
 */
class BeforeGroupsLayer extends BiomeLayerBase
{
	private final NewBiomeGroup normalGroup;
	private final NewBiomeGroup iceGroup;
	
	BeforeGroupsLayer(BiomeLayerData data, int depth)
	{
		super(data, depth);

		NewBiomeGroup configNormalGroup = this.data.groupRegistry.get(1);
		NewBiomeGroup configIceGroup = this.data.groupRegistry.get(2);
		this.normalGroup = new NewBiomeGroup();
		this.iceGroup = new NewBiomeGroup();
		
		List<BiomeData> normalBiomes = new ArrayList<BiomeData>();
		List<BiomeData> iceBiomes= new ArrayList<BiomeData>();
		
		for (BiomeData biome : configNormalGroup.biomes)
		{
			if (biome.biomeSize != depth)
			{
				continue;
			}
			for (int t = 0; t < biome.rarity; t++)
			{
				normalBiomes.add(biome);
			}
			configNormalGroup.totalGroupRarity -= biome.rarity;
		}
		if (!normalBiomes.isEmpty())
		{
			this.normalGroup.biomes = Arrays.asList(normalBiomes.toArray(new BiomeData[normalBiomes.size() + configNormalGroup.totalGroupRarity]));
		}
		
		for (BiomeData biome : configIceGroup.biomes)
		{
			if (biome.biomeSize != depth)
			{
				continue;
			}
			for (int t = 0; t < biome.rarity; t++)
			{
				iceBiomes.add(biome);
			}
			configIceGroup.totalGroupRarity -= biome.rarity;
		}
		if (!iceBiomes.isEmpty())
		{
			this.iceGroup.biomes = Arrays.asList(iceBiomes.toArray(new BiomeData[iceBiomes.size() + configIceGroup.totalGroupRarity]));
		}
	}

	@Override
	public int sample(LayerSampleContext<?> context, ILayerSampler parent, int x, int z)
	{
		int sample = parent.sample(x, z);
		
		if (
			// If biome bits have not yet been set (this column has not been cached), do so now.
			// TODO: We don't check for LAND_BIT here, but should? For 1.12 LayerMix fixed the 
			// problem by just ignoring non-land columns with biome data, and placed 
			// ocean/frozenocean based on ICE_BIT.
			(sample & BiomeLayers.BIOME_BITS) == 0
		)
		{
			BiomeData biomeData = null;
			if (this.normalGroup.biomes.size() > 0 && (sample & BiomeLayers.ICE_BIT) == 0)
			{
				biomeData = this.normalGroup.biomes.get(context.nextInt(this.normalGroup.biomes.size()));
			}
			if (this.iceGroup.biomes.size() > 0 && (sample & BiomeLayers.ICE_BIT) != 0)
			{
				biomeData = this.iceGroup.biomes.get(context.nextInt(this.iceGroup.biomes.size()));
			}
			if (biomeData != null)
			{
				return sample | biomeData.id |
					// Set IceBit based on Biome Temperature
					(biomeData.biomeTemperature <= this.data.frozenOceanTemperature ? BiomeLayers.ICE_BIT : 0)
				;
			}
		}
		return sample;
	}
}
