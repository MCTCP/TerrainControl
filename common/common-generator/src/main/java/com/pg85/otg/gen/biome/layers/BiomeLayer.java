package com.pg85.otg.gen.biome.layers;

import static com.pg85.otg.gen.biome.layers.BiomeLayers.BIOME_BITS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.pg85.otg.gen.biome.NewBiomeData;
import com.pg85.otg.gen.biome.layers.util.LayerRandomnessSource;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;
import com.pg85.otg.gen.biome.layers.util.LayerSampler;

/**
 * Places the biomes at a specific depth, given the biome groups.
 */
class BiomeLayer extends BiomeLayerBase
{
	protected final Map<NewBiomeGroup, Integer> groupToMaxRarity = new HashMap<>();
	protected final Map<NewBiomeGroup, Map<Integer, NewBiomeData>> groupBiomes = new HashMap<>();

	BiomeLayer(BiomeLayerData data, int depth)
	{
		super(data, depth);

		// Iterate through all of the groups
		for (Map.Entry<Integer, List<NewBiomeGroup>> entry : data.groups.entrySet())
		{
			for (NewBiomeGroup group : entry.getValue())
			{
				int maxRarity = 0;

				Map<Integer, NewBiomeData> biomes = new TreeMap<>();

				for (NewBiomeData biome : group.biomes)
				{
					if (depth == biome.biomeSize)
					{
						maxRarity += biome.rarity;
						biomes.put(maxRarity, biome);
					}
				}

				if (maxRarity > 0)
				{
					this.groupToMaxRarity.put(group, maxRarity);
					this.groupBiomes.put(group, biomes);
				}
			}
		}
	}

	@Override
	public int sample(LayerSampleContext<?> context, LayerSampler parent, int x, int z)
	{
		int sample = parent.sample(x, z);

		if (
			// If biome bits have not yet been set (this column has not been cached), do so now.
			(sample & BIOME_BITS) == 0 && 
			BiomeLayers.isLand(sample)
		)
		{
			int biomeGroupId = BiomeLayers.getGroupId(sample);
			if (biomeGroupId > 0)
			{
				NewBiomeGroup group = this.data.groupRegistry.get(biomeGroupId);	
				if (this.groupToMaxRarity.containsKey(group) && this.groupBiomes.containsKey(group))
				{
					NewBiomeData biomeData = getBiomeFromGroup(context, this.groupToMaxRarity.get(group), this.groupBiomes.get(group));
					return sample | biomeData.id |
                        // Set IceBit based on Biome Temperature
                        (biomeData.biomeTemperature <= this.data.frozenOceanTemperature ? BiomeLayers.ICE_BIT : 0)
					;
				}
			}
		}

		return sample;
	}

	private NewBiomeData getBiomeFromGroup(LayerRandomnessSource random, int maxRarity, Map<Integer, NewBiomeData> rarityMap)
	{
		// Get a random rarity number from our max rarity
		int chosenRarity = random.nextInt(maxRarity);

		// Iterate through the rarity map and see if the chosen rarity is less than the rarity for each group, if it is then return.
		for (Map.Entry<Integer, NewBiomeData> entry : rarityMap.entrySet())
		{
			if (chosenRarity < entry.getKey())
			{
				return entry.getValue();
			}
		}

		// Fallback
		return this.data.oceanBiomeData;
	}
}
