package com.pg85.otg.forge.gen;

import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.forge.biome.OTGBiomeProvider;

import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraftforge.common.world.ForgeWorldType.IChunkGeneratorFactory;

public class OTGChunkGeneratorFactory implements IChunkGeneratorFactory
{
	// Only used for SP world creation by Forge, we use our own WorldType registration logic for SP (see OTGPlugin/OTGGui).
	@Override
	public ChunkGenerator createChunkGenerator(Registry<Biome> biomeRegistry, Registry<DimensionSettings> dimensionSettingsRegistry, long seed, String generatorSettings)
	{
		return null;
	}

	// Used for MP when starting the server, with settings from server.properties.
	public DimensionGeneratorSettings createSettings(DynamicRegistries dynamicRegistries, long seed, boolean generateStructures, boolean bonusChest, String generatorSettings)
	{
		Registry<DimensionType> dimensionTypesRegistry = dynamicRegistries.getRegistry(Registry.DIMENSION_TYPE_KEY);
		Registry<Biome> biomesRegistry = dynamicRegistries.getRegistry(Registry.BIOME_KEY);
		Registry<DimensionSettings> dimensionSettingsRegistry = dynamicRegistries.getRegistry(Registry.NOISE_SETTINGS_KEY);
		
		return new DimensionGeneratorSettings(
			seed,
			generateStructures,
			bonusChest,
			DimensionGeneratorSettings.func_242749_a(
				dimensionTypesRegistry,
				DimensionType.getDefaultSimpleRegistry(dimensionTypesRegistry, biomesRegistry, dimensionSettingsRegistry, seed),
				new OTGNoiseChunkGenerator(
					new DimensionConfig(generatorSettings),
					new OTGBiomeProvider(
						generatorSettings,
						seed,
						false,
						false,
						biomesRegistry
					),
					seed,
					() -> dimensionSettingsRegistry.getOrThrow(DimensionSettings.field_242734_c)
				)
			)
		);
    }
}
