package com.pg85.otg.generator.biome.layers;

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
		// If the center is land and the corners are not, set it to land 2/3 of the time.
		if (center == 0 && ((nw != 0) || (ne != 0) || (sw != 0) || (se != 0)))
		{
			if (context.nextInt(3) != 0)
			{
				return 1; // set land
			}
		// If the center is land and the corners are not, there's a 1/5 chance to set it to ocean
		} else if ((center != 0) && ((nw == 0) || (ne == 0) || (sw == 0) || (se == 0)))
		{
			if (context.nextInt(5) == 0)
			{
				return 0; // set ocean
			}
		}

		// TODO: if center == 0, xor land

		return center;
	}
}
