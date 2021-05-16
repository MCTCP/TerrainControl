package com.pg85.otg.forge.gui;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.pg85.otg.OTG;
import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.forge.biome.OTGBiomeProvider;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.forge.gui.screens.CreateOTGWorldScreen;

import net.minecraft.client.gui.screen.BiomeGeneratorTypeScreens;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.OverworldBiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.NoiseChunkGenerator;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OTGGui
{
	// Define a new world type for the world creation screen
	private static final BiomeGeneratorTypeScreens OTG_WORLD_TYPE = new BiomeGeneratorTypeScreens(Constants.MOD_ID_SHORT)
	{
		protected ChunkGenerator generator(Registry<Biome> biomes, Registry<DimensionSettings> dimensionSettings, long seed)
		{
			// Provide our custom chunk generator, biome provider and dimension settings.
			if(!OTG.getEngine().getPresetLoader().getAllPresets().isEmpty())
			{
				return new OTGNoiseChunkGenerator(new OTGBiomeProvider(OTG.getEngine().getPresetLoader().getDefaultPresetName(), seed, false, false, biomes), seed, () -> dimensionSettings.getOrThrow(DimensionSettings.OVERWORLD));
			} else {
				// If no presets are installed, return the default chunkgenerator / biomeprovider
				return new NoiseChunkGenerator(new OverworldBiomeProvider(seed, false, false, biomes), seed, () ->
				{
					return dimensionSettings.getOrThrow(DimensionSettings.OVERWORLD);
				});
			}
		}
	};

	public static void init()
	{
		// Register the otg worldtype for the world creation screen
		BiomeGeneratorTypeScreens.PRESETS.add(OTG_WORLD_TYPE);
		
		// Register world type customisation button / screens
		Map<Optional<BiomeGeneratorTypeScreens>, BiomeGeneratorTypeScreens.IFactory> otgWorldOptionsScreen =
			ImmutableMap.of(
				Optional.of(OTG_WORLD_TYPE),
				(createWorldScreen, dimensionGeneratorSettings) ->
				{
					if(!OTG.getEngine().getPresetLoader().getAllPresets().isEmpty())
					{
						return new CreateOTGWorldScreen(
							createWorldScreen,
							createWorldScreen.worldGenSettingsComponent.registryHolder(),
							// Define apply function, generates updated 
							// settings when leaving customisation menu.
							(dimensionConfig) ->
							{
								createWorldScreen.worldGenSettingsComponent.updateSettings(
									OTGGui.createOTGDimensionGeneratorSettings(
										createWorldScreen.worldGenSettingsComponent.registryHolder(),
										dimensionGeneratorSettings,
										dimensionConfig
									)
								);
							}
						);
					} else {
						// If no preset are installed, do nothing (exits to main menu)
						return null;
					}
				}
			)
		;
		
		BiomeGeneratorTypeScreens.EDITORS = ImmutableMap.<Optional<BiomeGeneratorTypeScreens>, BiomeGeneratorTypeScreens.IFactory>builder()
			.putAll(BiomeGeneratorTypeScreens.EDITORS)
			.putAll(otgWorldOptionsScreen)
			.build()
		;		
	}
	
	private static DimensionGeneratorSettings createOTGDimensionGeneratorSettings(DynamicRegistries dynamicRegistries, DimensionGeneratorSettings dimensionGeneratorSettings, DimensionConfig dimensionConfig)
	{
		Registry<DimensionType> dimensionTypesRegistry = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
		Registry<Biome> biomesRegistry = dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY);
		Registry<DimensionSettings> dimensionSettingsRegistry = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);

		return new DimensionGeneratorSettings(
			dimensionGeneratorSettings.seed(),
			dimensionGeneratorSettings.generateFeatures(),
			dimensionGeneratorSettings.generateBonusChest(),
			DimensionGeneratorSettings.withOverworld(
				dimensionTypesRegistry,
				dimensionGeneratorSettings.dimensions(),
				new OTGNoiseChunkGenerator(
					dimensionConfig,
					new OTGBiomeProvider(
						dimensionConfig.PresetName,
						dimensionGeneratorSettings.seed(),
						false,
						false,
						biomesRegistry
					),
					dimensionGeneratorSettings.seed(),
					() -> dimensionSettingsRegistry.getOrThrow(DimensionSettings.OVERWORLD)
				)
			)
		);
	}
}
