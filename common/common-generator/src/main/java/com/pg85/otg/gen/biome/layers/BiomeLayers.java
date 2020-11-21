package com.pg85.otg.gen.biome.layers;

import java.util.function.LongFunction;

import com.pg85.otg.gen.biome.layers.util.CachingLayerContext;
import com.pg85.otg.gen.biome.layers.util.CachingLayerSampler;
import com.pg85.otg.gen.biome.layers.util.LayerFactory;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;
import com.pg85.otg.gen.biome.layers.util.LayerSampler;

/**
 * Holds the factory and utils needed for OTG's biome layers to work.
 */
public class BiomeLayers
{
	// Bit masks for biome generation

	// The land bit marks whether a sample is land or not. This is used to place biomes.
	public static final int LAND_BIT = (1 << 30);

	public static final int GROUP_SHIFT = 23;

	// The marker for biome groups
	public static final int GROUP_BIT = (127 << GROUP_SHIFT);

	// This is the amount of bits we & the sample at the end to get the correct biome id.
	public static final int BIOME_BITS = (1 << GROUP_SHIFT) - 1;

	public static boolean isLand(int sample) {
		return (sample & LAND_BIT) == 0;
	}

	public static int getGroupId(int sample)
	{
		return (sample & GROUP_BIT) >> GROUP_SHIFT;
	}

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


			if (data.groups.containsKey(depth)) {
				factory = new BiomeGroupLayer(data.groups.get(depth)).create(contextProvider.apply(depth), factory);
			}

			if (data.biomeDepths.contains(depth)) {
				factory = new BiomeLayer(data, depth).create(contextProvider.apply(depth), factory);
			}
		}

		// Finalize the biome data
		factory = new FinalizeLayer().create(contextProvider.apply(1L), factory);

		return factory;
	}

	// Create a sampler that can get a biome at a position
	public static CachingLayerSampler create(long seed)
	{
		LayerFactory<CachingLayerSampler> factory = build(BiomeLayerData.INSTANCE, salt -> new CachingLayerContext(25, seed, salt));
		return factory.make();
	}
}
