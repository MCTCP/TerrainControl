package com.pg85.otg.generator.biome.layers;

import com.pg85.otg.generator.biome.layers.type.InitLayer;
import com.pg85.otg.generator.biome.layers.util.LayerRandomnessSource;

public class LandLayer implements InitLayer
{
	public final int rarity;
	public LandLayer(int landRarity)
	{
		// Scale rarity from the world config
		this.rarity = 101 - landRarity;
	}

	@Override
	public int sample(LayerRandomnessSource context, int x, int y)
	{
		// Set land based on the rarity
		// TODO: do we need the land bit here? or can we just use the 1?
		return context.nextInt(this.rarity) == 0 ? 1 : 0;
	}
}
