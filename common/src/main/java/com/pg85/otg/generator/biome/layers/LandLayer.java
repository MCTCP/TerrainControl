package com.pg85.otg.generator.biome.layers;

import com.pg85.otg.generator.biome.layers.type.InitLayer;
import com.pg85.otg.generator.biome.layers.type.ParentedLayer;
import com.pg85.otg.generator.biome.layers.util.LayerRandomnessSource;
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
		// If we're on the center sample return land to try and make sure that the player doesn't spawn in the ocean.
		if (x == 0 && z == 0)
		{
			return 1;
		}

		// Set land based on the rarity
		// TODO: do we need the land bit here? or can we just use the 1?
		return context.nextInt(this.rarity) == 0 ? 1 : 0;
	}
}
