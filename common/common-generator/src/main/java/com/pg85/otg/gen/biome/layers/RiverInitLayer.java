package com.pg85.otg.gen.biome.layers;

import com.pg85.otg.gen.biome.layers.type.ParentedLayer;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;
import com.pg85.otg.interfaces.ILayerSampler;

public class RiverInitLayer implements ParentedLayer
{
	@Override
	public int sample(LayerSampleContext<?> context, ILayerSampler parent, int x, int z)
	{
		int currentPiece = parent.sample(x,  z);
		// TODO: For 1.12, we initliased the chunkseed here with inverted coordinates,
		// so initChunkSeed(zi + z, xi + x);, do we need to do the same here?		
		if (context.nextInt(2) == 0)
		{
			currentPiece |= BiomeLayers.RIVER_BIT_ONE;
		} else {
			currentPiece |= BiomeLayers.RIVER_BIT_TWO;
		}

		return currentPiece;
	}
}
