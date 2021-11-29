package com.pg85.otg.gen.biome.layers;

import java.util.List;
import java.util.function.LongFunction;

import com.pg85.otg.constants.SettingsEnums.BiomeMode;
import com.pg85.otg.constants.SettingsEnums.ImageMode;
import com.pg85.otg.gen.biome.BiomeData;
import com.pg85.otg.gen.biome.layers.util.CachingLayerContext;
import com.pg85.otg.gen.biome.layers.util.CachingLayerSampler;
import com.pg85.otg.gen.biome.layers.util.LayerFactory;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;
import com.pg85.otg.interfaces.ILayerSampler;
import com.pg85.otg.interfaces.ILogger;

/**
 * Holds the factory and utils needed for OTG's biome layers to work.
 */
public class BiomeLayers
{
	// Bit masks for biome generation
	// TODO: Using columns atm, will need to be changed for 3D biomes.

	// The land bit marks whether a column is land or not. This is used to place biomes.
	protected static final int LAND_BIT = (1 << 31);
	// The ice bit marks which land columns should use the ice biome group.
	protected static final int ICE_BIT = (1 << 30);
	// The island bit marks isle biomes spawned inside other biomes.
	protected static final int ISLAND_BIT = (1 << 29); // TODO: Do we really need this?

	// River bits mark whether there is a river in this column (TODO: Why 2?).
	private static final int RIVER_SHIFT = 27;
	protected static final int RIVER_BITS = (3 << RIVER_SHIFT);
	protected static final int RIVER_BIT_ONE = (1 << RIVER_SHIFT);
	protected static final int RIVER_BIT_TWO = (1 << (RIVER_SHIFT + 1));
	
	// Group bits store the id of the biome group used for a column.
	protected static final int GROUP_SHIFT = 20;
	protected static final int GROUP_BITS = (127 << GROUP_SHIFT); // Max 127 biome groups, 7 bits

	// Biome bits store the id of the biome used for a column.
	protected static final int BIOME_BITS = (1 << GROUP_SHIFT) - 1;
	
	static boolean isLand(int sample)
	{
		return (sample & LAND_BIT) != 0;
	}

	static int getGroupId(int sample)
	{
		return (sample & GROUP_BITS) >> GROUP_SHIFT;
	}
	
	/**
	* Checks for land and when present returns biome data, otherwise returns default ocean.
	*/
	static int getBiomeFromLayer(int sample)
	{
		return 
			(sample & BiomeLayers.LAND_BIT) != 0 ? 
			(sample & BiomeLayers.BIOME_BITS) : 
			0; // 0 should be the default ocean biome otg id, set by the presetloader.
	}

	private static <T extends ILayerSampler, C extends LayerSampleContext<T>> LayerFactory<T> build(BiomeLayerData data, LongFunction<C> contextProvider, ILogger logger)
	{
		// Create an empty layer to start the biome generation
		LayerFactory<T> factory = new InitializationLayer().create(contextProvider.apply(1L));
		LayerFactory<T> riverFactory = new InitializationLayer().create(contextProvider.apply(1L));
		LayerFactory<T> oceanTemperatureFactory = null;
		boolean riversStarted = false;
		boolean oceanTemperatureStarted = false;

		if(!(data.biomeMode == BiomeMode.FromImage && data.imageMode != ImageMode.ContinueNormal))
		{
			// Iterate through the depth, manipulating the factory at specific points
			for (int depth = 0; depth <= data.generationDepth; depth++)
			{
				// Scale the factory by 2x before adding more transformations
				factory = new ScaleLayer().create(contextProvider.apply(2000L + depth), factory);
				// TODO: probably should add smooth layer here

				// Scale our rivers if they've been started
				if (data.randomRivers && riversStarted)
				{
					riverFactory = new ScaleLayer().create(contextProvider.apply(2000L + depth), riverFactory);
				}

				if (oceanTemperatureStarted)
				{
					oceanTemperatureFactory = new ScaleLayer().create(contextProvider.apply(2000L + depth), oceanTemperatureFactory);
				}

				// If we're at the land size, initialize the land layer with the provided rarity.
				if (depth == data.landSize && data.landRarity > 0)
				{
					factory = new LandLayer(data.landRarity, data.forceLandAtSpawn, data.oldLandRarity).create(contextProvider.apply(1L), factory);
					factory = new FuzzyScaleLayer().create(contextProvider.apply(2000L), factory);
				}

				if (depth == data.oceanBiomeSize)
				{
					// TODO: Process isles/borders for oceanTemperatureFactory too, for gendepths after its been added?
					oceanTemperatureFactory = new OceanTemperatureLayer(data).create(contextProvider.apply(3L));
					oceanTemperatureStarted = true;
				}

				// If the depth is between landSize and landFuzzy, add islands to fuzz the ocean/land border.
				if (depth < (data.landSize + data.landFuzzy))
				{
					factory = new AddIslandsLayer().create(contextProvider.apply(depth), factory);
				}

				if(data.biomeMode == BiomeMode.Normal)
				{
					if (data.groups.containsKey(depth))
					{
						factory = new BiomeGroupLayer(data, depth).create(contextProvider.apply(depth), factory);
					}

					if (data.biomeDepths.contains(depth))
					{
						factory = new BiomeLayer(data, depth).create(contextProvider.apply(depth), factory);
					}
					// TODO: Do we need an IceLayer for Normal?
					if (depth == 3) // TODO: Why 3?
					{
						factory = new IceLayer(data).create(contextProvider.apply(depth), factory);
					}
				}
				else if(data.biomeMode == BiomeMode.NoGroups)
				{
					if (data.biomeDepths.contains(depth))
					{
						factory = new BeforeGroupsLayer(data, depth).create(contextProvider.apply(depth), factory);
					}
					NewBiomeGroup iceGroup = data.groupRegistry.get(2);
					if (iceGroup != null)
					{
						factory = new IceLayer(data).create(contextProvider.apply(depth), factory);
					}
				}

				// Start rivers if we're at the current depth
				if (data.riverDepth == depth)
				{
					if (data.randomRivers)
					{
						riverFactory = new RiverInitLayer().create(contextProvider.apply(depth), riverFactory);
						riversStarted = true;
					} else {
						// TODO: This generates no rivers atm
						factory = new RiverInitLayer().create(contextProvider.apply(depth), factory);
					}
				}

				// If we're at the end of the river size
				if ((data.generationDepth - data.riverSize) == depth)
				{
					if (data.randomRivers)
					{
						riverFactory = new RiverLayer().create(contextProvider.apply(5 + depth), riverFactory);
					} else {
						// TODO: This generates no rivers atm						
						factory = new RiverLayer().create(contextProvider.apply(5 + depth), factory);
					}
				}

				List<BiomeData> isleBiomes = data.isleBiomesAtDepth.get(depth);
				if(isleBiomes != null && isleBiomes.size() > 0)
				{
					BiomeIsleLayer.IslesList islesAtCurrentDepth = new BiomeIsleLayer.IslesList();
					for (BiomeData biome : isleBiomes)
					{
						boolean[] biomeCanSpawnIn = new boolean[1024];
						boolean inOcean = false;
						for (int islandInBiome : biome.isleInBiomes)
						{
							if (islandInBiome == data.oceanBiomeData.id)
							{
								inOcean = true;
							} else {
								biomeCanSpawnIn[islandInBiome] = true;
							}
						}
						int chance = (data.biomeRarityScale + 1) - biome.rarity;
						islesAtCurrentDepth.addIsle(biome.id, chance, biomeCanSpawnIn, inOcean);
					}
					factory = new BiomeIsleLayer(islesAtCurrentDepth).create(contextProvider.apply(depth), factory);				
				}
				
				List<BiomeData> borderBiomes = data.borderBiomesAtDepth.get(depth);
				if(borderBiomes != null && borderBiomes.size() > 0)
				{
					BiomeBorderLayer.BordersList bordersAtCurrentDepth = new BiomeBorderLayer.BordersList();
					for (BiomeData biome : borderBiomes)
					{
						for(int targetBiomeId : biome.borderInBiomes)
						{
							bordersAtCurrentDepth.addBorder(biome.id, targetBiomeId, biome.onlyBorderNearBiomes, biome.notBorderNearBiomes);						
						}
					}
					factory = new BiomeBorderLayer(bordersAtCurrentDepth).create(contextProvider.apply(depth), factory);				
				}
			}

			// Add ocean biomes. This only adds the regular ocean at the moment, soon it will add others.
			// TODO: This does nothing atm, remove?
			//factory = new ApplyOceanLayer(data).create(contextProvider.apply(3L), factory);

			factory = new MergeOceanTemperatureLayer().create(contextProvider.apply(1L), factory, oceanTemperatureFactory);
			
			// Finalize the biome data
			if (data.randomRivers)
			{
				factory = new FinalizeWithRiverLayer(data.riversEnabled, data.riverBiomes).create(contextProvider.apply(1L), factory, riverFactory);
			} else {
				// TODO: This generates no rivers atm
				factory = new FinalizeLayer(data.riversEnabled, data.riverBiomes).create(contextProvider.apply(1L), factory);
			}
		}

		if(data.biomeMode == BiomeMode.FromImage)
		{
			factory = new FromImageLayer(data, logger).create(contextProvider.apply(0), factory);
		}

		return factory;
	}

	// Create a sampler that can get a biome at a position
	public static CachingLayerSampler create(long seed, BiomeLayerData data, ILogger logger)
	{
		LayerFactory<CachingLayerSampler> factory = build(data, salt -> new CachingLayerContext(25, seed, salt), logger);
		return factory.make();
	}
}
