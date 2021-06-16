package com.pg85.otg.gen.biome.layers.type;

import com.pg85.otg.gen.biome.layers.util.LayerFactory;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;
import com.pg85.otg.gen.biome.layers.util.LayerSampler;

/**
 * The type for layers that modify the output based on the previous layer.
 */
public interface ParentedLayer
{
	default <R extends LayerSampler> LayerFactory<R> create(LayerSampleContext<R> context, LayerFactory<R> parent)
	{
	  return () -> {
		 R layerSampler = parent.make();
		 return context.createSampler((x, z) -> {
			context.initSeed(x, z);
			return this.sample(context, layerSampler, x, z);
		 }, layerSampler);
	  };
	}

	int sample(LayerSampleContext<?> context, LayerSampler parent, int x, int z);
}
