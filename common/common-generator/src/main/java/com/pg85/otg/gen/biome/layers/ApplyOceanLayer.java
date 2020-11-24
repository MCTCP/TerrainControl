package com.pg85.otg.gen.biome.layers;

import com.pg85.otg.gen.biome.layers.type.ParentedLayer;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;
import com.pg85.otg.gen.biome.layers.util.LayerSampler;

public class ApplyOceanLayer implements ParentedLayer
{
	private final BiomeLayerData data;

	public ApplyOceanLayer(BiomeLayerData data) {
		this.data = data;
	}

	@Override
	public int sample(LayerSampleContext<?> context, LayerSampler parent, int x, int z)
	{
		int sample = parent.sample(x, z);

		// If there is no land bit, leave the biome bit at 0.
		// 0 is the world's ocean biome, set by PresetLoader 
		// when registering biomes.

		// TODO: Do we still need this class?
		
		return sample;
	}
}
