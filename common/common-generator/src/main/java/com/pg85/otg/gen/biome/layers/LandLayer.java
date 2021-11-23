package com.pg85.otg.gen.biome.layers;

import static com.pg85.otg.gen.biome.layers.BiomeLayers.LAND_BIT;

import com.pg85.otg.gen.biome.layers.type.ParentedLayer;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;
import com.pg85.otg.interfaces.ILayerSampler;

/**
 * Sets land based on the provided rarity.
 */
class LandLayer implements ParentedLayer
{
	private final int rarity;
	private final boolean forceLandAtSpawn;
	private final boolean spawnLand;
	private final boolean oldLandRarity;

	LandLayer(int landRarity, boolean forceLandAtSpawn, boolean oldLandRarity)
	{
		// Scale rarity from the world config
		if (oldLandRarity) {
			this.rarity = 101 - landRarity;
		} else {
			this.rarity = landRarity;
		}
		this.forceLandAtSpawn = forceLandAtSpawn;
		this.spawnLand = landRarity != 0;
		this.oldLandRarity = oldLandRarity;
	}

	@Override
	public int sample(LayerSampleContext<?> context, ILayerSampler parent, int x, int z)
	{
		int sample = parent.sample(x, z);

		boolean result;
		if (oldLandRarity) {
			// Old land rarity - 100 is all land, 99 is 50% land, etc.
			result = context.nextInt(rarity) == 0;
		} else {
			// New land rarity, where n = % chance for land
			result = context.nextInt(101) <= this.rarity;
		}

		if (result && spawnLand)
		{
			return sample | LAND_BIT;
		} else {
			// If we're on the center sample return land to try and make sure that the player doesn't spawn in the ocean.
			// TODO: This assumes spawn is at 0,0?
			if (x == 0 && z == 0 && forceLandAtSpawn)
			{
				return sample | LAND_BIT;
			}
		}

		return sample;
	}
}
