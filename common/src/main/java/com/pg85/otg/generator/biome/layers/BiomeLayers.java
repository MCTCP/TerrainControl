package com.pg85.otg.generator.biome.layers;

import java.util.function.LongFunction;

import com.pg85.otg.generator.biome.layers.util.CachingLayerContext;
import com.pg85.otg.generator.biome.layers.util.CachingLayerSampler;
import com.pg85.otg.generator.biome.layers.util.LayerFactory;
import com.pg85.otg.generator.biome.layers.util.LayerSampleContext;
import com.pg85.otg.generator.biome.layers.util.LayerSampler;

/**
 * Holds the factory and utils needed for OTG's biome layers to work.
 */
public class BiomeLayers
{
	private static <T extends LayerSampler, C extends LayerSampleContext<T>> LayerFactory<T> build(BiomeLayerData data, LongFunction<C> contextProvider)
	{
		// Create an empty layer to start the biome generation
		LayerFactory<T> factory = new InitializationLayer().create(contextProvider.apply(1L));

		// Iterate through the depth, manipulating the factory at specific points
		for (int depth = 0; depth < data.generationDepth; depth++)
		{
			// Scale the factory by 2x before adding more transformations
			factory = new ScaleLayer().create(contextProvider.apply(2000L + depth), factory);
			// TODO: probably should add smooth layer here

			// If we're at the land size, initialize the land layer with the provided rarity.
			if (depth == data.landSize) {
				factory = new LandLayer(data.landRarity).create(contextProvider.apply(1L), factory);
				factory = new FuzzyScaleLayer().create(contextProvider.apply(2000L), factory);
			}

			// If the depth is between landSize and landFuzzy, add islands to fuzz the ocean/land border.
			if (depth < (data.landSize + data.landFuzzy))
			{
				factory = new AddIslandsLayer().create(contextProvider.apply(depth), factory);
			}
		}

		return factory;
	}

	// Create a
	public static CachingLayerSampler create(long seed)
	{
		LayerFactory<CachingLayerSampler> factory = build(new BiomeLayerData(), salt -> new CachingLayerContext(25, seed, salt));
		return factory.make();
	}
}
