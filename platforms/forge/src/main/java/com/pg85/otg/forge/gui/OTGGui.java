package com.pg85.otg.forge.gui;

import java.util.Map;
import java.util.Optional;
import com.google.common.collect.ImmutableMap;
import com.pg85.otg.OTG;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.forge.dimensions.OTGDimensionType;
import com.pg85.otg.forge.gui.screens.CreateOTGDimensionsScreen;
import net.minecraft.client.gui.screen.BiomeGeneratorTypeScreens;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.OverworldBiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.NoiseChunkGenerator;
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
			//if(!OTG.getEngine().getPresetLoader().getAllPresets().isEmpty())
			///{
				//return new OTGNoiseChunkGenerator(new OTGBiomeProvider(OTG.getEngine().getPresetLoader().getDefaultPresetFolderName(), seed, false, false, biomes), seed, () -> dimensionSettings.getOrThrow(DimensionSettings.OVERWORLD));
			//} else {
				// If no presets are installed, return the default chunkgenerator / biomeprovider
				return new NoiseChunkGenerator(new OverworldBiomeProvider(seed, false, false, biomes), seed, () ->
				{
					return dimensionSettings.getOrThrow(DimensionSettings.OVERWORLD);
				});
			//}
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
						/*
						return new CreateOTGWorldScreen(
							createWorldScreen,
							createWorldScreen.worldGenSettingsComponent.registryHolder(),
							// Define apply function, generates updated 
							// settings when leaving customisation menu.
							(dimensionConfig) ->
							{

								MutableRegistry<DimensionType> dimensionTypesRegistry = createWorldScreen.worldGenSettingsComponent.registryHolder().registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
								Registry<Biome> biomesRegistry = createWorldScreen.worldGenSettingsComponent.registryHolder().registryOrThrow(Registry.BIOME_REGISTRY);
								Registry<DimensionSettings> dimensionSettingsRegistry = createWorldScreen.worldGenSettingsComponent.registryHolder().registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
								
								createWorldScreen.worldGenSettingsComponent.updateSettings(
									OTGDimensionType.createOTGDimensionGeneratorSettings(
										dimensionTypesRegistry,
										biomesRegistry,
										dimensionSettingsRegistry,
										dimensionGeneratorSettings.seed(),
										dimensionGeneratorSettings.generateFeatures(),
										dimensionGeneratorSettings.generateBonusChest(),
										dimensionGeneratorSettings.dimensions(),
										dimensionConfig.PresetFolderName
									)
								);
							}
						);
						*/
						return new CreateOTGDimensionsScreen(
							createWorldScreen,
							// Define apply function, generates updated 
							// settings when leaving customisation menu.
							(dimGenSettings) ->
							{
								MutableRegistry<DimensionType> dimensionTypesRegistry = createWorldScreen.worldGenSettingsComponent.registryHolder().registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
								Registry<Biome> biomesRegistry = createWorldScreen.worldGenSettingsComponent.registryHolder().registryOrThrow(Registry.BIOME_REGISTRY);
								Registry<DimensionSettings> dimensionSettingsRegistry = createWorldScreen.worldGenSettingsComponent.registryHolder().registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);

								createWorldScreen.worldGenSettingsComponent.updateSettings(
									OTGDimensionType.createOTGDimensionGeneratorSettings(
										dimGenSettings.dimensionConfig,
										dimensionTypesRegistry,
										biomesRegistry,
										dimensionSettingsRegistry,
										dimensionGeneratorSettings.seed(),
										dimensionGeneratorSettings.generateFeatures(),
										dimensionGeneratorSettings.generateBonusChest(),
										dimensionGeneratorSettings.dimensions()
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
}
