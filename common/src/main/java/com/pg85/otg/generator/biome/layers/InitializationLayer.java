package com.pg85.otg.generator.biome.layers;

import com.pg85.otg.generator.biome.layers.type.InitLayer;
import com.pg85.otg.generator.biome.layers.util.LayerRandomnessSource;
import com.pg85.otg.generator.biome.layers.util.LayerSampleContext;

/**
 * Initializes the biome sample for biome generation.
 */
public class InitializationLayer implements InitLayer
{
	@Override
	public int sample(LayerSampleContext<?> context, int x, int y)
	{
		return 0;
	}
}
