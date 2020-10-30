package com.pg85.otg.generator.biome.layers;

import java.util.function.LongFunction;

import com.pg85.otg.generator.biome.layers.util.CachingLayerContext;
import com.pg85.otg.generator.biome.layers.util.CachingLayerSampler;
import com.pg85.otg.generator.biome.layers.util.LayerFactory;
import com.pg85.otg.generator.biome.layers.util.LayerSampleContext;
import com.pg85.otg.generator.biome.layers.util.LayerSampler;

public class BiomeLayers
{
	private static <T extends LayerSampler, C extends LayerSampleContext<T>> LayerFactory<T> build(LongFunction<C> contextProvider)
	{
		LayerFactory<T> factory = new LandLayer(99).create(contextProvider.apply(2L));
		factory = new FuzzyScaleLayer().create(contextProvider.apply(4L), factory);
		for (int i = 0; i < 6; i++)
		{
			factory = new ScaleLayer().create(contextProvider.apply(10L + i), factory);
		}
		return factory;
	}

	public static CachingLayerSampler create(long seed)
	{
		LayerFactory<CachingLayerSampler> factory = build(salt -> new CachingLayerContext(25, seed, salt));
		return factory.make();
	}
}
