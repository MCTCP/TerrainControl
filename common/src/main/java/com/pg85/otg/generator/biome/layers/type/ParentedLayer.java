package com.pg85.otg.generator.biome.layers.type;

import com.pg85.otg.generator.biome.layers.util.LayerFactory;
import com.pg85.otg.generator.biome.layers.util.LayerSampleContext;
import com.pg85.otg.generator.biome.layers.util.LayerSampler;

/**
 * They type for layers that modify the output based on the previous layer.
 */
public interface ParentedLayer
{
   default <R extends LayerSampler> LayerFactory<R> create(LayerSampleContext<R> context, LayerFactory<R> parent)
   {
      return () -> {
         R layerSampler = parent.make();
         return context.createSampler((i, j) -> {
            context.initSeed(i, j);
            return this.sample(context, layerSampler, i, j);
         }, layerSampler);
      };
   }

   int sample(LayerSampleContext<?> context, LayerSampler parent, int x, int z);
}
