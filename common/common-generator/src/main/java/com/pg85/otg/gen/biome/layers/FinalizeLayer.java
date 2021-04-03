package com.pg85.otg.gen.biome.layers;

import com.pg85.otg.gen.biome.layers.type.ParentedLayer;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;
import com.pg85.otg.gen.biome.layers.util.LayerSampler;

/**
 * Gets the biome id of the sample of this position by removing the extra land and other data.
 */
class FinalizeLayer implements ParentedLayer
{
	private final boolean riversEnabled;
	private int[] riverBiomes;
	
	public FinalizeLayer(boolean riversEnabled, int[] riverBiomes)
	{
		this.riversEnabled = riversEnabled;
		this.riverBiomes = riverBiomes;
	}
	
	@Override
	public int sample(LayerSampleContext<?> context, LayerSampler parent, int x, int z)
	{
		int sample = parent.sample(x, z);

		// Remove all the metadata bits from the sample
		sample = sample & BiomeLayers.BIOME_BITS;
		
        if (this.riversEnabled && (sample & BiomeLayers.RIVER_BITS) != 0)
		{
    		int riverBiomeId = this.riverBiomes[sample];
    		if(riverBiomeId >= 0)
    		{
    			sample = riverBiomeId;
    		}
		}
        
		return sample;
	}
}
