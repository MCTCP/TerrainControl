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
import com.pg85.otg.constants.Constants;
import com.pg85.otg.core.OTG;
import com.pg85.otg.core.config.dimensions.DimensionConfig;
import com.pg85.otg.core.config.dimensions.DimensionConfig.OTGOverWorld;
import com.pg85.otg.forge.biome.OTGBiomeProvider;
import com.pg85.otg.forge.dimensions.OTGDimensionTypeHelper;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.forge.gui.screens.CreateOTGDimensionsScreen;

import net.minecraft.client.gui.screens.worldselection.WorldPreset;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraftforge.common.world.ForgeWorldType;
import net.minecraftforge.registries.ForgeRegistries;

public class OTGGui
{
	public static DimensionConfig currentSelection;

	// Define a new world type for the world creation screen
	public static final WorldPreset OTG_WORLD_TYPE = new WorldPreset(Constants.MOD_ID_SHORT)
	{
		protected ChunkGenerator generator(Registry<Biome> biomes, Registry<NoiseGeneratorSettings> dimensionSettings, long seed)
		{
			// Called when selecting the OTG world type in the world creation gui.
			currentSelection = DimensionConfig.createDefaultConfig();
			if(!OTG.getEngine().getPresetLoader().getAllPresets().isEmpty())
			{
				currentSelection.Overworld = new OTGOverWorld(OTG.getEngine().getPresetLoader().getDefaultPresetFolderName(), seed, null, null);
				return new OTGNoiseChunkGenerator(new OTGBiomeProvider(OTG.getEngine().getPresetLoader().getDefaultPresetFolderName(), seed, false, false, biomes), seed, () -> dimensionSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD));
			} else {
				// If no presets are installed, return the default chunkgenerator / biomeprovider
				return new NoiseBasedChunkGenerator(new OverworldBiomeSource(seed, false, false, biomes), seed, () ->
				{
					return dimensionSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
				});
			}
		}
	};
	
	public static void init()
	{
		// Register the otg worldtype for the world creation screen
		WorldPreset.PRESETS.add(OTG_WORLD_TYPE);
		
		// Register world type customisation button / screens
		@SuppressWarnings("unchecked")
		Map<Optional<WorldPreset>, WorldPreset.PresetEditor> otgWorldOptionsScreen =
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
								RegistryAccess.RegistryHolder dynamicRegistries = createWorldScreen.worldGenSettingsComponent.registryHolder();
								WritableRegistry<DimensionType> dimensionTypesRegistry = dynamicRegistries.ownedRegistryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
								Registry<Biome> biomesRegistry = dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY);
								Registry<NoiseGeneratorSettings> dimensionSettingsRegistry = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);

								long seed = dimensionGeneratorSettings.seed();
								boolean generateFeatures = dimensionGeneratorSettings.generateFeatures();
								boolean generateBonusChest = dimensionGeneratorSettings.generateBonusChest();

								// If there is a dimensionconfig for the generatorsettings, use that. Otherwise find a preset by name.
								DimensionConfig dimConfig = dimGenSettings.dimensionConfig;
								currentSelection = dimConfig;
								MappedRegistry<LevelStem> dimensions = dimensionGeneratorSettings.dimensions();
								if(dimConfig.isModpackConfig())
								{
									// Non-otg overworld, generatorsettings contains non-otg world type.
									ForgeWorldType def = dimConfig.Overworld.NonOTGWorldType == null ? null : ForgeRegistries.WORLD_TYPES.getValue(new ResourceLocation(dimConfig.Overworld.NonOTGWorldType));
									if(def != null)
									{
										WorldGenSettings existingDimSetting = def.createSettings(dynamicRegistries, seed, generateFeatures, generateBonusChest, dimConfig.Overworld.NonOTGGeneratorSettings);
										dimensions = existingDimSetting.dimensions();
									} else {
										MappedRegistry<LevelStem> simpleregistry = DimensionType.defaultDimensions(dimensionTypesRegistry, biomesRegistry, dimensionSettingsRegistry, seed);		      
										WorldGenSettings existingDimSetting = null;
										switch(dimConfig.Overworld.NonOTGWorldType == null ? "" : dimConfig.Overworld.NonOTGWorldType)
										{
											case "flat":
												JsonObject jsonobject = dimConfig.Overworld.NonOTGGeneratorSettings != null && !dimConfig.Overworld.NonOTGGeneratorSettings.isEmpty() ? GsonHelper.parse(dimConfig.Overworld.NonOTGGeneratorSettings) : new JsonObject();
												Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, jsonobject);
												existingDimSetting = new WorldGenSettings(seed, generateFeatures, generateBonusChest, WorldGenSettings.withOverworld(dimensionTypesRegistry, simpleregistry, new FlatLevelSource(FlatLevelGeneratorSettings.CODEC.parse(dynamic).resultOrPartial(LogManager.getLogger()::error).orElseGet(() -> {
												return FlatLevelGeneratorSettings.getDefault(biomesRegistry);
												}))));
												break;
											case "debug_all_block_states":
												existingDimSetting = new WorldGenSettings(seed, generateFeatures, generateBonusChest, WorldGenSettings.withOverworld(dimensionTypesRegistry, simpleregistry, new DebugLevelSource(biomesRegistry)));
												break;
											case "amplified":
												existingDimSetting = new WorldGenSettings(seed, generateFeatures, generateBonusChest, WorldGenSettings.withOverworld(dimensionTypesRegistry, simpleregistry, new NoiseBasedChunkGenerator(new OverworldBiomeSource(seed, false, false, biomesRegistry), seed, () -> {
													return dimensionSettingsRegistry.getOrThrow(NoiseGeneratorSettings.AMPLIFIED);
												})));
												break;
											case "largebiomes":
												existingDimSetting = new WorldGenSettings(seed, generateFeatures, generateBonusChest, WorldGenSettings.withOverworld(dimensionTypesRegistry, simpleregistry, new NoiseBasedChunkGenerator(new OverworldBiomeSource(seed, false, true, biomesRegistry), seed, () -> {
													return dimensionSettingsRegistry.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
												})));
												break;
											default:
												existingDimSetting = new WorldGenSettings(seed, generateFeatures, generateBonusChest, WorldGenSettings.withOverworld(dimensionTypesRegistry, simpleregistry, WorldGenSettings.makeDefaultOverworld(biomesRegistry, dimensionSettingsRegistry, seed)));
												break;
										}
										dimensions = existingDimSetting.dimensions();
									}
								}
								else if(dimGenSettings.dimGenSettings != null)
								{
									// Add non-otg overworld configured via customize menu.
									Entry<ResourceKey<LevelStem>, LevelStem> registryEntry = ((Entry<ResourceKey<LevelStem>, LevelStem>)dimGenSettings.dimGenSettings.dimensions().entrySet().toArray()[0]);
									if (registryEntry.getKey() == LevelStem.OVERWORLD)
									{
										dimensions.register(registryEntry.getKey(), registryEntry.getValue(), dimensions.lifecycle(registryEntry.getValue()));
									}
								}

								createWorldScreen.worldGenSettingsComponent.updateSettings(
									OTGDimensionTypeHelper.createOTGDimensionGeneratorSettings(
										dimConfig.isModpackConfig() ? Constants.MODPACK_CONFIG_NAME : null,											
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
		
		WorldPreset.EDITORS = ImmutableMap.<Optional<WorldPreset>, WorldPreset.PresetEditor>builder()
			.putAll(WorldPreset.EDITORS)
			.putAll(otgWorldOptionsScreen)
			.build()
		;			
	}
}
