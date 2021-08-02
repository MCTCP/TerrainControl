package com.pg85.otg.forge.gui;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.pg85.otg.OTG;
import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.forge.biome.OTGBiomeProvider;
import com.pg85.otg.forge.dimensions.OTGDimensionType;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.forge.gui.screens.CreateOTGDimensionsScreen;
import net.minecraft.client.gui.screen.BiomeGeneratorTypeScreens;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.OverworldBiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DebugChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.NoiseChunkGenerator;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.world.ForgeWorldType;
import net.minecraftforge.registries.ForgeRegistries;

@OnlyIn(Dist.CLIENT)
public class OTGGui
{
	// Define a new world type for the world creation screen
	public static final BiomeGeneratorTypeScreens OTG_WORLD_TYPE = new BiomeGeneratorTypeScreens(Constants.MOD_ID_SHORT)
	{
		protected ChunkGenerator generator(Registry<Biome> biomes, Registry<DimensionSettings> dimensionSettings, long seed)
		{
			// Called when selecting the OTG world type in the world creation gui.
			if(!OTG.getEngine().getPresetLoader().getAllPresets().isEmpty())
			{
				return new OTGNoiseChunkGenerator(new OTGBiomeProvider(OTG.getEngine().getPresetLoader().getDefaultPresetFolderName(), seed, false, false, biomes), seed, () -> dimensionSettings.getOrThrow(DimensionSettings.OVERWORLD));
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
		@SuppressWarnings("unchecked")
		Map<Optional<BiomeGeneratorTypeScreens>, BiomeGeneratorTypeScreens.IFactory> otgWorldOptionsScreen =
			ImmutableMap.of(
				Optional.of(OTG_WORLD_TYPE),
				(createWorldScreen, dimensionGeneratorSettings) ->
				{
					if(!OTG.getEngine().getPresetLoader().getAllPresets().isEmpty())
					{
						return new CreateOTGDimensionsScreen(
							createWorldScreen,
							// Define apply function, generates updated 
							// settings when leaving customisation menu.
							(dimGenSettings) ->
							{
								DynamicRegistries.Impl dynamicRegistries = createWorldScreen.worldGenSettingsComponent.registryHolder();							
								MutableRegistry<DimensionType> dimensionTypesRegistry = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
								Registry<Biome> biomesRegistry = dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY);
								Registry<DimensionSettings> dimensionSettingsRegistry = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);

								long seed = dimensionGeneratorSettings.seed();
								boolean generateFeatures = dimensionGeneratorSettings.generateFeatures();
								boolean generateBonusChest = dimensionGeneratorSettings.generateBonusChest();

								// If there is a dimensionconfig for the generatorsettings, use that. Otherwise find a preset by name.
								DimensionConfig dimConfig = dimGenSettings.dimensionConfig;
								SimpleRegistry<Dimension> dimensions = dimensionGeneratorSettings.dimensions();
								if(dimConfig.isModpackConfig())
								{
									// Non-otg overworld, generatorsettings contains non-otg world type.
									ForgeWorldType def = ForgeRegistries.WORLD_TYPES.getValue(new ResourceLocation(dimConfig.Overworld.NonOTGWorldType));
									if(def != null)
									{
										DimensionGeneratorSettings existingDimSetting = def.createSettings(dynamicRegistries, seed, generateFeatures, generateBonusChest, dimConfig.Overworld.NonOTGGeneratorSettings);
										dimensions = existingDimSetting.dimensions();
									} else {
										SimpleRegistry<Dimension> simpleregistry = DimensionType.defaultDimensions(dimensionTypesRegistry, biomesRegistry, dimensionSettingsRegistry, seed);		      
										DimensionGeneratorSettings existingDimSetting = null;
										switch(dimConfig.Overworld.NonOTGWorldType == null ? "" : dimConfig.Overworld.NonOTGWorldType)
										{
											case "flat":
												JsonObject jsonobject = dimConfig.Overworld.NonOTGGeneratorSettings != null && !dimConfig.Overworld.NonOTGGeneratorSettings.isEmpty() ? JSONUtils.parse(dimConfig.Overworld.NonOTGGeneratorSettings) : new JsonObject();
												Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, jsonobject);
												existingDimSetting = new DimensionGeneratorSettings(seed, generateFeatures, generateBonusChest, DimensionGeneratorSettings.withOverworld(dimensionTypesRegistry, simpleregistry, new FlatChunkGenerator(FlatGenerationSettings.CODEC.parse(dynamic).resultOrPartial(LogManager.getLogger()::error).orElseGet(() -> {
												return FlatGenerationSettings.getDefault(biomesRegistry);
												}))));
												break;
											case "debug_all_block_states":
												existingDimSetting = new DimensionGeneratorSettings(seed, generateFeatures, generateBonusChest, DimensionGeneratorSettings.withOverworld(dimensionTypesRegistry, simpleregistry, new DebugChunkGenerator(biomesRegistry)));
												break;
											case "amplified":
												existingDimSetting = new DimensionGeneratorSettings(seed, generateFeatures, generateBonusChest, DimensionGeneratorSettings.withOverworld(dimensionTypesRegistry, simpleregistry, new NoiseChunkGenerator(new OverworldBiomeProvider(seed, false, false, biomesRegistry), seed, () -> {
													return dimensionSettingsRegistry.getOrThrow(DimensionSettings.AMPLIFIED);
												})));
												break;
											case "largebiomes":
												existingDimSetting = new DimensionGeneratorSettings(seed, generateFeatures, generateBonusChest, DimensionGeneratorSettings.withOverworld(dimensionTypesRegistry, simpleregistry, new NoiseChunkGenerator(new OverworldBiomeProvider(seed, false, true, biomesRegistry), seed, () -> {
													return dimensionSettingsRegistry.getOrThrow(DimensionSettings.OVERWORLD);
												})));
												break;
											default:
												existingDimSetting = new DimensionGeneratorSettings(seed, generateFeatures, generateBonusChest, DimensionGeneratorSettings.withOverworld(dimensionTypesRegistry, simpleregistry, DimensionGeneratorSettings.makeDefaultOverworld(biomesRegistry, dimensionSettingsRegistry, seed)));
												break;
										}
										dimensions = existingDimSetting.dimensions();
									}
								}
								else if(dimGenSettings.dimGenSettings != null)
								{
									// Add non-otg overworld configured via customize menu.
									Entry<RegistryKey<Dimension>, Dimension> registryEntry = ((Entry<RegistryKey<Dimension>, Dimension>)dimGenSettings.dimGenSettings.dimensions().entrySet().toArray()[0]);
									if (registryEntry.getKey() == Dimension.OVERWORLD)
									{
										dimensions.register(registryEntry.getKey(), registryEntry.getValue(), dimensions.lifecycle(registryEntry.getValue()));
									}
								}

								createWorldScreen.worldGenSettingsComponent.updateSettings(
									OTGDimensionType.createOTGDimensionGeneratorSettings(
										dimConfig,
										dimensionTypesRegistry,
										biomesRegistry,
										dimensionSettingsRegistry,
										seed,
										generateFeatures,
										generateBonusChest,
										dimensions
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
