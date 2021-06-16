package com.pg85.otg.gen.biome.layers.type;

import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;
import com.pg85.otg.gen.biome.layers.util.LayerSampler;

/**
 * The type for layers that sample in a diagonal cross or X formation.
 * This is useful for getting all the corner samples from a specific x and z coordinate.
 * Unfortunately, this will cause some serious slowdown so it needs to be used with care.
 */
public interface DiagonalCrossSamplingLayer extends ParentedLayer {
	int sample(LayerSampleContext<?> context, int x, int z, int sw, int se, int ne, int nw, int center);

	default int sample(LayerSampleContext<?> context, LayerSampler parent, int x, int z) {
	  return this.sample(context, x, z,
			  parent.sample(x - 1, z + 1), // Southwest
			  parent.sample(x + 1, z + 1), // Southeast
			  parent.sample(x + 1, z - 1), // Northeast
			  parent.sample(x - 1, z - 1), // Northwest
			  parent.sample(x, z));		// Center
	}
}
