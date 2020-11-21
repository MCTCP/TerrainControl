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

		// If there is no land bit set the ocean
		if (!BiomeLayers.isLand(sample)) {
			return sample | this.data.oceanId;
		}

		return sample;
	}
}
