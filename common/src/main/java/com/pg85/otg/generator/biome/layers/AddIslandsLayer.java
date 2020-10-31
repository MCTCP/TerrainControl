package com.pg85.otg.generator.biome.layers;

import static com.pg85.otg.generator.biome.layers.BiomeLayers.LAND_BIT;

import com.pg85.otg.generator.biome.layers.type.DiagonalCrossSamplingLayer;
import com.pg85.otg.generator.biome.layers.util.LayerSampleContext;

/**
 * Fuzzes the edge of land and ocean by adding islands and adding ocean.
 */
public class AddIslandsLayer implements DiagonalCrossSamplingLayer
{
	@Override
	public int sample(LayerSampleContext<?> context, int x, int z, int sw, int se, int ne, int nw, int center)
	{
		// If the center is land and the corners are not, set it to ocean 2/3 of the time.
		if (BiomeLayers.isLand(center) && (!BiomeLayers.isLand(nw) || !BiomeLayers.isLand(ne) || !BiomeLayers.isLand(sw) || !BiomeLayers.isLand(se)))
		{
			if (context.nextInt(3) != 0)
			{
				return center & ~LAND_BIT;
			}
		// If the center isn't land and the corners are, there's a 1/5 chance to set it to land
		} else if (!BiomeLayers.isLand(center) && (BiomeLayers.isLand(nw) || BiomeLayers.isLand(ne) || BiomeLayers.isLand(sw) || BiomeLayers.isLand(se)))
		{
			if (context.nextInt(5) == 0)
			{
				return center | LAND_BIT;
			}
		}

		// TODO: if center == 0, xor land

		return center;
	}
}
