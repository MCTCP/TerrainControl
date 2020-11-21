package com.pg85.otg.gen.biome.layers;

import static com.pg85.otg.gen.biome.layers.BiomeLayers.GROUP_SHIFT;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.pg85.otg.gen.biome.layers.type.ParentedLayer;
import com.pg85.otg.gen.biome.layers.util.LayerRandomnessSource;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;
import com.pg85.otg.gen.biome.layers.util.LayerSampler;

/**
 * Places a biome group at a certain depth.
 */
class BiomeGroupLayer implements ParentedLayer
{
	// The sorted map of rarities to biome groups
	private final TreeMap<Integer, NewBiomeGroup> rarityMap = new TreeMap<>();
	// The rarity sum of all the groups. This is used to choose the biome group.
	private final int maxRarity;
	BiomeGroupLayer(List<NewBiomeGroup> groups) {
		int maxRarity = 0;

		// Iterate through groups and keep a tally of the rarity of each group.
		// The order doesn't matter all that much, the margin between the values dictates the rarity.
		for (NewBiomeGroup group : groups)
		{
			maxRarity += group.rarity;
			this.rarityMap.put(maxRarity, group);
		}

		this.maxRarity = maxRarity;
	}

	@Override
	public int sample(LayerSampleContext<?> context, LayerSampler parent, int x, int z)
	{
		int sample = parent.sample(x, z);

		// Check if it's land and then check if there is no group already here
		if (BiomeLayers.isLand(sample) && BiomeLayers.getGroupId(sample) == 0)
		{
			int biomeGroup = getGroup(context);

			// Encode the biome group id into the sample for later use
			return sample | biomeGroup << GROUP_SHIFT;
		}

		return sample;
	}

	private int getGroup(LayerRandomnessSource random)
	{
		// Get a random rarity number from our max rarity
		int chosenRarity = random.nextInt(maxRarity);

		// Iterate through the rarity map and see if the chosen rarity is less than the rarity for each group, if it is then return.
		for (Map.Entry<Integer, NewBiomeGroup> entry : rarityMap.entrySet())
		{
			if (chosenRarity < entry.getKey()) {
				return entry.getValue().id;
			}
		}

		// Fallback
		return 0;
	}
}
