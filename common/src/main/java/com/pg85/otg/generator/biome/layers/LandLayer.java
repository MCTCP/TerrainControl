package com.pg85.otg.generator.biome.layers;

import static com.pg85.otg.generator.biome.layers.BiomeLayers.LAND_BIT;

import com.pg85.otg.generator.biome.layers.type.ParentedLayer;
import com.pg85.otg.generator.biome.layers.util.LayerSampleContext;
import com.pg85.otg.generator.biome.layers.util.LayerSampler;

/**
 * Sets land based on the provided rarity.
 */
public class LandLayer implements ParentedLayer
{
	public final int rarity;
	public LandLayer(int landRarity)
	{
		// Scale rarity from the world config
		this.rarity = 101 - landRarity;
	}

	@Override
	public int sample(LayerSampleContext<?> context, LayerSampler parent, int x, int z)
	{
		int sample = parent.sample(x, z);
		// If we're on the center sample return land to try and make sure that the player doesn't spawn in the ocean.
		if (x == 0 && z == 0)
		{
			return sample | LAND_BIT;
		}

		// Set land based on the rarity
		if (context.nextInt(this.rarity) == 0) {
			return sample | LAND_BIT;
		}

		return sample;
	}
}
