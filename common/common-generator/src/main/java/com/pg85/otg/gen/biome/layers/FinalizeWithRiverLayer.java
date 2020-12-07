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
	private int[] riverBiomes;
	
	public FinalizeWithRiverLayer(boolean riversEnabled, int[] riverBiomes)
	{
		this.riversEnabled = riversEnabled;
		this.riverBiomes = riverBiomes;
	}

	@Override
	public int sample(LayerRandomnessSource context, LayerSampler samplerMain, LayerSampler samplerRiver, int x, int z)
	{
		int sample = samplerMain.sample(x, z);
        if ((sample & BiomeLayers.LAND_BIT) != 0)
        {
       		sample = sample & BiomeLayers.BIOME_BITS;
        } else {
        	// TODO: Ocean/FrozenOcean based on ICE_BIT and worldConfig.frozenOcean.
        	// This will work for backwards compatibility, but will need to be 
        	// re-designed for the new ocean biomes?
        	sample = 0;
        }

        if (this.riversEnabled)
        {
        	int currentRiver = samplerRiver.sample(x, z);
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
