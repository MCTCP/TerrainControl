package com.pg85.otg.forge.gen;

import com.pg85.otg.OTG;
import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.forge.biome.OTGBiomeProvider;
import com.pg85.otg.presets.Preset;

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
		Registry<DimensionType> dimensionTypesRegistry = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
		Registry<Biome> biomesRegistry = dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY);
		Registry<DimensionSettings> dimensionSettingsRegistry = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
		
		// Find the preset defined in generatorSettings, if none use the default preset.
		Preset preset = OTG.getEngine().getPresetLoader().getPresetByShortNameOrFolderName(generatorSettings);
		String presetFolderName = OTG.getEngine().getPresetLoader().getDefaultPresetFolderName();
		if(preset != null)
		{
			presetFolderName = preset.getFolderName();
		}
		
		return new DimensionGeneratorSettings(
			seed,
			generateStructures,
			bonusChest,
			DimensionGeneratorSettings.withOverworld(
				dimensionTypesRegistry,
				DimensionType.defaultDimensions(dimensionTypesRegistry, biomesRegistry, dimensionSettingsRegistry, seed),
				new OTGNoiseChunkGenerator(
					new DimensionConfig(presetFolderName),
					new OTGBiomeProvider(
						presetFolderName,
						seed,
						false,
						false,
						biomesRegistry
					),
					seed,
					() -> dimensionSettingsRegistry.getOrThrow(DimensionSettings.OVERWORLD)
				)
			)
		);
	}
}
