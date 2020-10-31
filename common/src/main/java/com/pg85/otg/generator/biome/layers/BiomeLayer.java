package com.pg85.otg.generator.biome.layers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.pg85.otg.generator.biome.NewBiomeData;
import com.pg85.otg.generator.biome.layers.type.ParentedLayer;
import com.pg85.otg.generator.biome.layers.util.LayerRandomnessSource;
import com.pg85.otg.generator.biome.layers.util.LayerSampleContext;
import com.pg85.otg.generator.biome.layers.util.LayerSampler;

/**
 * Places the biomes at a specific depth, given the biome groups.
 */
public class BiomeLayer implements ParentedLayer
{
	private final BiomeLayerData data;
	private final int depth;

	private final Map<NewBiomeGroup, Integer> groupToMaxRarity = new HashMap<>();
	private final Map<NewBiomeGroup, Map<Integer, NewBiomeData>> groupBiomes = new HashMap<>();
	public BiomeLayer(BiomeLayerData data, int depth) {
		this.data = data;
		this.depth = depth;

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

		if (BiomeLayers.isLand(sample)) {
			int biomeGroupId = BiomeLayers.getGroupId(sample);
			if (biomeGroupId > 0)
			{
				NewBiomeGroup group = data.groupRegistry.get(biomeGroupId);

				if (groupToMaxRarity.containsKey(group) && groupBiomes.containsKey(group))
				{
					int biome = getBiomeFromGroup(context, groupToMaxRarity.get(group), groupBiomes.get(group));

					return sample | biome;
				}
			}
		}

		return sample;
	}

	private int getBiomeFromGroup(LayerRandomnessSource random, int maxRarity, Map<Integer, NewBiomeData> rarityMap)
	{
		// Get a random rarity number from our max rarity
		int chosenRarity = random.nextInt(maxRarity);

		// Iterate through the rarity map and see if the chosen rarity is less than the rarity for each group, if it is then return.
		for (Map.Entry<Integer, NewBiomeData> entry : rarityMap.entrySet())
		{
			if (chosenRarity < entry.getKey()) {
				return entry.getValue().id;
			}
		}

		// Fallback
		return 0;
	}

}
