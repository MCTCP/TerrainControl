package com.pg85.otg.gen.biome.layers;

import static com.pg85.otg.gen.biome.layers.BiomeLayers.LAND_BIT;

import com.pg85.otg.gen.biome.layers.type.DiagonalCrossSamplingLayer;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;

/**
 * Fuzzes the edge of land and ocean by adding islands and adding ocean.
 */
class AddIslandsLayer implements DiagonalCrossSamplingLayer
{
	@Override
	public int sample(LayerSampleContext<?> context, int x, int z, int sw, int se, int ne, int nw, int center)
	{
		// Initialize the sample as always land... for some reason.
		int sample = center | LAND_BIT;

		// If the center is not land and one of the corners is, set it to ocean 2/3 of the time.
		if (!BiomeLayers.isLand(center) && (BiomeLayers.isLand(nw) || BiomeLayers.isLand(ne) || BiomeLayers.isLand(sw) || BiomeLayers.isLand(se)))
		{
			if (context.nextInt(3) != 0)
			{
				sample ^= LAND_BIT;
			}
		// If the center is land and one of the corners isn't, there's a 1/5 chance to set it to ocean.
		} else if (BiomeLayers.isLand(center) && (!BiomeLayers.isLand(nw) || !BiomeLayers.isLand(ne) || !BiomeLayers.isLand(sw) || !BiomeLayers.isLand(se)))
		{
			if (context.nextInt(5) == 0)
			{
				sample ^= LAND_BIT;
			}
		}
		else if (!BiomeLayers.isLand(center))
		{
			sample ^= LAND_BIT;
		}

		return sample;
	}
}
