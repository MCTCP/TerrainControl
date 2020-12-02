package com.pg85.otg.gen.biome.layers;

import com.pg85.otg.gen.biome.layers.type.CrossSamplingLayer;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;

public class RiverLayer implements CrossSamplingLayer
{
	@Override
	public int sample(LayerSampleContext<?> context, int x, int z, int n, int e, int s, int w, int center)
	{
        int northCheck = n & BiomeLayers.RIVER_BITS;
        int southCheck = s & BiomeLayers.RIVER_BITS;
        int eastCheck = e & BiomeLayers.RIVER_BITS;
        int westCheck = w & BiomeLayers.RIVER_BITS;
        int centerCheck = center & BiomeLayers.RIVER_BITS;
        if (
    		(centerCheck == 0) || 
    		(westCheck == 0) || 
    		(eastCheck == 0) || 
    		(northCheck == 0) || 
    		(southCheck == 0)
		)
        {
        	center |= BiomeLayers.RIVER_BITS;
        }
        else if (
    		(centerCheck != westCheck) || 
    		(centerCheck != northCheck) || 
    		(centerCheck != eastCheck) || 
    		(centerCheck != southCheck)
		)
        {
        	center |= BiomeLayers.RIVER_BITS;
        } else {
        	// Remove any river bits entirely(?)
        	center |= BiomeLayers.RIVER_BITS;
        	center ^= BiomeLayers.RIVER_BITS;
        }
        return center;
	}
}
