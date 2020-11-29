package com.pg85.otg.gen.biome.layers;

import com.pg85.otg.gen.biome.layers.type.ParentedLayer;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;
import com.pg85.otg.gen.biome.layers.util.LayerSampler;

/**
 * Gets the biome id of the sample of this position by removing the extra land and other data.
 */
class FinalizeLayer implements ParentedLayer
{
	@Override
	public int sample(LayerSampleContext<?> context, LayerSampler parent, int x, int z)
	{
		int sample = parent.sample(x, z);
        if ((sample & BiomeLayers.LAND_BIT) != 0)
        {
       		sample = sample & BiomeLayers.BIOME_BITS;	
        } else {
        	// TODO: Ocean/FrozenOcean based on ICE_BIT and worldConfig.frozenOcean.
        	// This will work for backwards compatibility, but will need to be 
        	// re-designed for the new ocean biomes?
        	sample = 0;
        }
		
		return sample;
	}
}
