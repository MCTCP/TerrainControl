package com.pg85.otg.gen.biome.layers;

import com.pg85.otg.gen.biome.layers.type.MergingLayer;
import com.pg85.otg.gen.biome.layers.util.LayerRandomnessSource;
import com.pg85.otg.interfaces.ILayerSampler;

public class MergeOceanTemperatureLayer implements MergingLayer
{
	@Override
	public int sample(LayerRandomnessSource context, ILayerSampler mainSampler, ILayerSampler oceanSampler, int x, int z)
	{
		int sample = mainSampler.sample(x, z);

		// If we're not land, return ocean
		if (!BiomeLayers.isLand(sample))
		{
			return oceanSampler.sample(x, z);
		}

		// Return biome sample
		return sample;
	}
}
