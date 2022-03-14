package com.pg85.otg.gen.biome.layers;

import com.pg85.otg.constants.SettingsEnums.BiomeMode;
import com.pg85.otg.gen.biome.layers.type.ParentedLayer;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;
import com.pg85.otg.interfaces.ILayerSampler;

/**
 * Sets ice based on the provided rarity.
 */
class IceLayer implements ParentedLayer
{
	private final int rarity;
	
	IceLayer(BiomeLayerData data)
	{
		if(data.biomeMode == BiomeMode.NoGroups)
		{
			NewBiomeGroup iceGroup = data.groupRegistry.get(2);			
			// Scale rarity from the world config
			this.rarity = 101 - (iceGroup == null ? 0 : iceGroup.rarity);
		} else {		
			this.rarity = 10;
		}
	}

	@Override
	public int sample(LayerSampleContext<?> context, ILayerSampler parent, int x, int z)
	{
		int sample = parent.sample(x, z);

		// Set ice based on the rarity
		// TODO: For 1.12, we initliased the chunkseed here with inverted coordinates,
		// so initChunkSeed(z + zi, x + xi);, do we need to do the same here?
		if (context.nextInt(this.rarity) == 0)
		{
			return sample | BiomeLayers.ICE_BIT;
		}

		return sample;
	}
}
