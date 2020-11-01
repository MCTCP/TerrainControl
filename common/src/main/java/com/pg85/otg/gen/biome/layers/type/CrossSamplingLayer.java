package com.pg85.otg.gen.biome.layers.type;

import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;
import com.pg85.otg.gen.biome.layers.util.LayerSampler;

/**
 * The type for layers that sample in a cross/plus formation.
 * This is useful for getting all the neighboring samples from a specific x and z coordinate.
 * Unfortunately, this will cause some serious slowdown so it needs to be used with care.
 */
public interface CrossSamplingLayer extends ParentedLayer {
   int sample(LayerSampleContext<?> context, int x, int z, int n, int e, int s, int w, int center);

   default int sample(LayerSampleContext<?> context, LayerSampler parent, int x, int z) {
      return this.sample(context, x, z,
              parent.sample(x, z - 1), // North
              parent.sample(x + 1, z), // East
              parent.sample(x, z + 1), // South
              parent.sample(x - 1, z), // West
              parent.sample(x, z));       // Center
   }
}