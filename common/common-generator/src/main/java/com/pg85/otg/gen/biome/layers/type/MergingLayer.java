package com.pg85.otg.gen.biome.layers.type;

import com.pg85.otg.gen.biome.layers.util.LayerFactory;
import com.pg85.otg.gen.biome.layers.util.LayerRandomnessSource;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;
import com.pg85.otg.interfaces.ILayerSampler;

public interface MergingLayer
{
	default <R extends ILayerSampler> LayerFactory<R> create(LayerSampleContext<R> context, LayerFactory<R> layer1, LayerFactory<R> layer2)
	{
	  return () -> {
		 R layerSampler = layer1.make();
		 R layerSampler2 = layer2.make();
		 return context.createSampler((x, z) -> {
			context.initSeed(x, z);
			return this.sample(context, layerSampler, layerSampler2, x, z);
		 }, layerSampler, layerSampler2);
	  };
	}

	int sample(LayerRandomnessSource context, ILayerSampler sampler1, ILayerSampler sampler2, int x, int z);
}
