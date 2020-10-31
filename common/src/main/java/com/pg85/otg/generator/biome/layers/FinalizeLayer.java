package com.pg85.otg.generator.biome.layers;

import static com.pg85.otg.generator.biome.layers.BiomeLayers.BIOME_BITS;

import com.pg85.otg.generator.biome.layers.type.ParentedLayer;
import com.pg85.otg.generator.biome.layers.util.LayerSampleContext;
import com.pg85.otg.generator.biome.layers.util.LayerSampler;

/**
 * Gets the biome id of the sample of this position by removing the extra land and other data.
 */
public class FinalizeLayer implements ParentedLayer
{
	@Override
	public int sample(LayerSampleContext<?> context, LayerSampler parent, int x, int z)
	{
		return parent.sample(x, z) & BIOME_BITS;
	}
}
