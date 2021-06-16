package com.pg85.otg.gen.biome.layers;

import com.pg85.otg.gen.biome.layers.type.MergingLayer;
import com.pg85.otg.gen.biome.layers.util.LayerRandomnessSource;
import com.pg85.otg.gen.biome.layers.util.LayerSampler;

/**
 * Gets the biome id of the sample of this position by removing the extra land and other data.
 */
class FinalizeWithRiverLayer implements MergingLayer
{	
	private final boolean riversEnabled;
	private final int[] riverBiomes;
	
	public FinalizeWithRiverLayer(boolean riversEnabled, int[] riverBiomes)
	{
		this.riversEnabled = riversEnabled;
		this.riverBiomes = riverBiomes;
	}

	@Override
	public int sample(LayerRandomnessSource context, LayerSampler mainSampler, LayerSampler riverSampler, int x, int z)
	{
		int sample = mainSampler.sample(x, z);

		// Remove all the metadata bits from the sample
		sample = sample & BiomeLayers.BIOME_BITS;

		if (this.riversEnabled)
		{
			int currentRiver = riverSampler.sample(x, z);
			if((currentRiver & BiomeLayers.RIVER_BITS) != 0)
			{
				int riverBiomeId = this.riverBiomes[sample];
				if(riverBiomeId >= 0)
				{
					sample = riverBiomeId;
				}
			}
		}

		return sample;
	}
}
