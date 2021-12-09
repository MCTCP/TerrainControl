package com.pg85.otg.forge.dimensions;

import java.util.OptionalLong;
import java.util.Random;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.core.OTG;
import com.pg85.otg.core.config.dimensions.DimensionConfig;
import com.pg85.otg.core.config.dimensions.DimensionConfig.OTGDimension;
import com.pg85.otg.core.config.dimensions.DimensionConfig.OTGOverWorld;
import com.pg85.otg.core.presets.Preset;
import com.pg85.otg.forge.biome.OTGBiomeProvider;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.MappedRegistry;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.Climate.ParameterList;
import net.minecraft.world.level.biome.Climate.ParameterPoint;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraftforge.common.world.ForgeWorldPreset;
import net.minecraftforge.registries.ForgeRegistries;

public class OTGDimensionTypeHelper
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	public static DimensionType make(OptionalLong fixedTime, boolean hasSkylight, boolean hasCeiling, boolean ultraWarm, boolean natural, double coordinateScale, boolean createDragonFight, boolean piglinSafe, boolean bedWorks, boolean respawnAnchorWorks, boolean hasRaids, int minY, int height, int logicalHeight, ResourceLocation infiniburn, ResourceLocation effectsLocation, float ambientLight)
	{
		return DimensionType.create(fixedTime, hasSkylight, hasCeiling, ultraWarm, natural, coordinateScale, createDragonFight, piglinSafe, bedWorks, respawnAnchorWorks, hasRaids, minY, height, logicalHeight, infiniburn, effectsLocation, ambientLight);
	}
	
	// Used for MP when starting the server, with settings from server.properties.
	public static WorldGenSettings createOTGSettings(RegistryAccess dynamicRegistries, long seed, boolean generateFeatures, boolean bonusChest, String generatorSettings)
	{
		WritableRegistry<DimensionType> dimensionTypesRegistry = dynamicRegistries.ownedRegistryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
		Registry<Biome> biomesRegistry = dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY);
		Registry<NoiseGeneratorSettings> dimensionSettingsRegistry = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
		Registry<NormalNoise.NoiseParameters> noiseParamsRegistry = dynamicRegistries.registryOrThrow(Registry.NOISE_REGISTRY);
		
		// If there is a dimensionconfig for the generatorsettings, use that. Otherwise find a preset by name.
		DimensionConfig dimConfig = DimensionConfig.fromDisk(generatorSettings);
		MappedRegistry<LevelStem> dimensions = null;
		Preset preset = null;
		String dimConfigName = null;
		if(dimConfig == null)
		{
			// Find the preset defined in generatorSettings, if none use the default preset.
			preset = OTG.getEngine().getPresetLoader().getPresetByShortNameOrFolderName(generatorSettings);
			if(preset == null)
			{
				OTG.getEngine().getLogger().log(LogLevel.FATAL, LogCategory.MAIN, "DimensionConfig or preset name \"" + generatorSettings +"\", provided as generator-settings in server.properties, does not exist.");
				throw new RuntimeException("DimensionConfig or preset name \"" + generatorSettings +"\", provided as generator-settings in server.properties, does not exist.");
			} else {
				dimConfig = new DimensionConfig();
				dimConfig.Overworld = new OTGOverWorld(preset.getFolderName(), seed, null, null);
				dimensions = DimensionType.defaultDimensions(dynamicRegistries, seed);
			}
		} else {
			dimConfigName = generatorSettings;
			// Non-otg overworld, generatorsettings contains non-otg world type.
			ForgeWorldPreset def = dimConfig.Overworld.NonOTGWorldType == null || dimConfig.Overworld.NonOTGWorldType.trim().length() == 0 ? null : ForgeRegistries.WORLD_TYPES.getValue(new ResourceLocation(dimConfig.Overworld.NonOTGWorldType));
			if(def != null)
			{
				WorldGenSettings existingDimSetting = def.createSettings(dynamicRegistries, seed, generateFeatures, bonusChest, dimConfig.Overworld.NonOTGGeneratorSettings);
				dimensions = existingDimSetting.dimensions();
			} else {
				WorldGenSettings existingDimSetting = null;
				Registry<DimensionType> registry2 = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
				Registry<Biome> registry = dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY);
				MappedRegistry<LevelStem> mappedregistry = DimensionType.defaultDimensions(dynamicRegistries, seed);
				String nonOTGGeneratorSettings = dimConfig.Overworld.NonOTGGeneratorSettings;
				if(dimConfig.Overworld.NonOTGWorldType != null)
				{
					net.minecraftforge.common.world.ForgeWorldPreset type = net.minecraftforge.registries.ForgeRegistries.WORLD_TYPES.getValue(new net.minecraft.resources.ResourceLocation(dimConfig.Overworld.NonOTGWorldType));
					if (type != null)
					{
						existingDimSetting = type.createSettings(dynamicRegistries, seed, generateFeatures, false, nonOTGGeneratorSettings);
					}
				}
				
				switch(dimConfig.Overworld.NonOTGWorldType == null ? "" : dimConfig.Overworld.NonOTGWorldType)
				{
					case "flat":
						JsonObject jsonobject = nonOTGGeneratorSettings != null && !nonOTGGeneratorSettings.isEmpty() ? GsonHelper.parse(nonOTGGeneratorSettings) : new JsonObject();
						Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, jsonobject);
						existingDimSetting = new WorldGenSettings(
							seed,
							generateFeatures, 
							bonusChest, 
							WorldGenSettings.withOverworld(
								registry2, 
								mappedregistry, 
								new FlatLevelSource(
									FlatLevelGeneratorSettings.CODEC.parse(dynamic).resultOrPartial(LOGGER::error)
									.orElseGet(
										() -> {
											return FlatLevelGeneratorSettings.getDefault(registry);
										}
									)
								)
							)
						);
					case "debug_all_block_states":
						existingDimSetting = new WorldGenSettings(seed, generateFeatures, bonusChest, WorldGenSettings.withOverworld(registry2, mappedregistry, new DebugLevelSource(registry)));
					case "amplified":
						existingDimSetting = new WorldGenSettings(seed, generateFeatures, bonusChest, WorldGenSettings.withOverworld(registry2, mappedregistry, WorldGenSettings.makeOverworld(dynamicRegistries, seed, NoiseGeneratorSettings.AMPLIFIED)));
					case "largebiomes":
						existingDimSetting = new WorldGenSettings(seed, generateFeatures, bonusChest, WorldGenSettings.withOverworld(registry2, mappedregistry, WorldGenSettings.makeOverworld(dynamicRegistries, seed, NoiseGeneratorSettings.LARGE_BIOMES)));
					default:
						existingDimSetting = new WorldGenSettings(seed, generateFeatures, bonusChest, WorldGenSettings.withOverworld(registry2, mappedregistry, WorldGenSettings.makeDefaultOverworld(dynamicRegistries, seed)));
				}
				dimensions = existingDimSetting.dimensions();
			}
		}

		return OTGDimensionTypeHelper.createOTGDimensionGeneratorSettings(
			dimConfigName,				
			dimConfig,
			dimensionTypesRegistry,
			biomesRegistry,
			dimensionSettingsRegistry,
			noiseParamsRegistry,
			seed,
			generateFeatures,
			bonusChest,
			dimensions
		);
	}
	
	// Used for SP and MP
	public static WorldGenSettings createOTGDimensionGeneratorSettings(String dimConfigName, DimensionConfig dimConfig, WritableRegistry<DimensionType> dimensionTypesRegistry, Registry<Biome> biomesRegistry, Registry<NoiseGeneratorSettings> dimensionSettingsRegistry, Registry<NormalNoise.NoiseParameters> noiseParamsRegistry, long seed, boolean generateFeatures, boolean generateBonusChest, MappedRegistry<LevelStem> defaultDimensions)
	{
		// Create a new registry object and register dimensions to it.
		MappedRegistry<LevelStem> dimensions = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
		boolean nonOTGOverWorld = dimConfig.Overworld.PresetFolderName == null;

		// Dummy list
		ParameterList<Supplier<Biome>> paramList = new Climate.ParameterList<Supplier<Biome>>(
			ImmutableList.of(
				Pair.of(
					(ParameterPoint)Climate.parameters(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), 
					(Supplier<Biome>)() -> { return biomesRegistry.getOrThrow(Biomes.PLAINS); }
				)
			)
		);
		
		if(dimConfig.Overworld != null && dimConfig.Overworld.PresetFolderName != null && !nonOTGOverWorld)
		{
			ChunkGenerator chunkGenerator = new OTGNoiseChunkGenerator(
				dimConfig.Overworld.PresetFolderName, 
				dimConfigName,
				noiseParamsRegistry,
				new OTGBiomeProvider(
					dimConfig.Overworld.PresetFolderName,
					paramList,
					Optional.of(new OTGBiomeProvider.PresetInstance(dimConfig.Overworld.PresetFolderName, OTGBiomeProvider.Preset.DEFAULT, biomesRegistry))				
				),
				seed,
				() -> dimensionSettingsRegistry.getOrThrow(NoiseGeneratorSettings.OVERWORLD) // TODO: Add OTG DimensionSettings?
			);
			addDimension(dimConfig.Overworld.PresetFolderName, dimensions, dimensionTypesRegistry, LevelStem.OVERWORLD, chunkGenerator, DimensionType.OVERWORLD_LOCATION);
		}
		if(dimConfig.Nether != null && dimConfig.Nether.PresetFolderName != null)
		{
			long dimSeed = dimConfig.Nether.Seed != -1l ? dimConfig.Nether.Seed : new Random().nextLong();
			ChunkGenerator chunkGenerator = new OTGNoiseChunkGenerator(
					dimConfig.Nether.PresetFolderName, 
					dimConfigName,
					noiseParamsRegistry,
					new OTGBiomeProvider(
						dimConfig.Nether.PresetFolderName,
						paramList,
						Optional.of(new OTGBiomeProvider.PresetInstance(dimConfig.Nether.PresetFolderName, OTGBiomeProvider.Preset.DEFAULT, biomesRegistry))				
					),					
					dimSeed,
				() -> dimensionSettingsRegistry.getOrThrow(NoiseGeneratorSettings.NETHER) // TODO: Add OTG DimensionSettings?
			);
			addDimension(dimConfig.Nether.PresetFolderName, dimensions, dimensionTypesRegistry, LevelStem.NETHER, chunkGenerator, DimensionType.NETHER_LOCATION);
		}
		if(dimConfig.End != null && dimConfig.End.PresetFolderName != null)
		{
			long dimSeed = dimConfig.End.Seed != -1l ? dimConfig.End.Seed : new Random().nextLong();
			ChunkGenerator chunkGenerator = new OTGNoiseChunkGenerator(
					dimConfig.End.PresetFolderName, 
					dimConfigName, 
					noiseParamsRegistry,
					new OTGBiomeProvider(
						dimConfig.End.PresetFolderName,
						paramList,
						Optional.of(new OTGBiomeProvider.PresetInstance(dimConfig.Nether.PresetFolderName, OTGBiomeProvider.Preset.DEFAULT, biomesRegistry))				
					),					
					dimSeed,
				() -> dimensionSettingsRegistry.getOrThrow(NoiseGeneratorSettings.END) // TODO: Add OTG DimensionSettings?
			);
			addDimension(dimConfig.End.PresetFolderName, dimensions, dimensionTypesRegistry, LevelStem.END, chunkGenerator, DimensionType.END_LOCATION);
		}
		if(dimConfig.Dimensions != null)
		{
			for(OTGDimension otgDim : dimConfig.Dimensions)
			{
				if(otgDim.PresetFolderName != null)
				{
					long dimSeed = otgDim.Seed != -1l ? otgDim.Seed : new Random().nextLong();
					ChunkGenerator chunkGenerator = new OTGNoiseChunkGenerator(
						otgDim.PresetFolderName,
						dimConfigName,
						noiseParamsRegistry,
						new OTGBiomeProvider(
							otgDim.PresetFolderName,
							paramList,
							Optional.of(new OTGBiomeProvider.PresetInstance(otgDim.PresetFolderName, OTGBiomeProvider.Preset.DEFAULT, biomesRegistry))				
						),
						dimSeed,
						() -> dimensionSettingsRegistry.getOrThrow(NoiseGeneratorSettings.OVERWORLD) // TODO: Add OTG DimensionSettings?
					);
					ResourceKey<LevelStem> dimRegistryKey = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation(Constants.MOD_ID_SHORT, otgDim.PresetFolderName.trim().replace(" ", "_").toLowerCase()));
					ResourceKey<DimensionType> dimTypeRegistryKey = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation(Constants.MOD_ID_SHORT, otgDim.PresetFolderName.trim().replace(" ", "_").toLowerCase()));
					addDimension(otgDim.PresetFolderName, dimensions, dimensionTypesRegistry, dimRegistryKey, chunkGenerator, dimTypeRegistryKey);
				}
			}
		}

		// Register default dimensions (if we're not overriding them with otg dimensions)
		for(Entry<ResourceKey<LevelStem>, LevelStem> entry : defaultDimensions.entrySet())
		{
			ResourceKey<LevelStem> registrykey = entry.getKey();
			if (
				(dimConfig.Overworld == null || dimConfig.Overworld.PresetFolderName == null || registrykey != LevelStem.OVERWORLD) &&
				(dimConfig.Nether == null || dimConfig.Nether.PresetFolderName == null || registrykey != LevelStem.NETHER) && 
				(dimConfig.End == null || dimConfig.End.PresetFolderName == null || registrykey != LevelStem.END)
			)
			{
				dimensions.register(registrykey, entry.getValue(), defaultDimensions.lifecycle(entry.getValue()));
			}
		}

		return new WorldGenSettings(
			seed,
			generateFeatures,
			generateBonusChest,
			dimensions
		);
	}

	private static void addDimension(String presetFolderName, MappedRegistry<LevelStem> dimensions, WritableRegistry<DimensionType> dimensionTypeRegistry, ResourceKey<LevelStem> dimRegistryKey, ChunkGenerator chunkGenerator, ResourceKey<DimensionType> dimTypeRegistryKey)
	{
		Preset preset = OTG.getEngine().getPresetLoader().getPresetByFolderName(presetFolderName);
		IWorldConfig worldConfig = preset.getWorldConfig();
		
		// Register OTG DimensionType with settings from WorldConfig
		DimensionType otgOverWorld = DimensionType.create(
			worldConfig.getFixedTime(),
			worldConfig.getHasSkyLight(),
			worldConfig.getHasCeiling(),
			worldConfig.getUltraWarm(),
			worldConfig.getNatural(),
			worldConfig.getCoordinateScale(),
			worldConfig.getCreateDragonFight(),
			worldConfig.getPiglinSafe(),
			worldConfig.getBedWorks(),
			worldConfig.getRespawnAnchorWorks(),
			worldConfig.getHasRaids(),
			Constants.WORLD_DEPTH,
			Constants.WORLD_HEIGHT,
			worldConfig.getLogicalHeight(),
			new ResourceLocation(worldConfig.getInfiniburn()),
			new ResourceLocation(worldConfig.getEffectsLocation()),
			worldConfig.getAmbientLight()
		);
		dimensionTypeRegistry.registerOrOverride(OptionalInt.empty(), dimTypeRegistryKey, otgOverWorld, Lifecycle.stable());
		
		LevelStem dimension = dimensions.get(dimRegistryKey);
		Supplier<DimensionType> supplier = () -> {
			return dimension == null ? dimensionTypeRegistry.getOrThrow(dimTypeRegistryKey) : dimension.type();
		};
		dimensions.register(dimRegistryKey, new LevelStem(supplier, chunkGenerator), Lifecycle.stable());		
	}
	
	// Writes OTG DimensionTypes to world save folder as datapack json files so they're picked up on world load.
	// Unfortunately there doesn't appear to be a way to persist them via code. Silly, but it works.
	public static void saveDataPackFile(Path datapackFolder, String dimName, IWorldConfig worldConfig, String presetFolderName)
	{
		File folder = new File(datapackFolder + File.separator + Constants.MOD_ID_SHORT + File.separator);
		File file = new File(datapackFolder + File.separator + Constants.MOD_ID_SHORT + File.separator + "pack.mcmeta");
		if(!folder.exists())
		{
			folder.mkdirs();
		}
		String data;
		if(!file.exists())
		{
			data = "{ \"pack\": { \"pack_format\":6, \"description\":\"OTG Dimension settings\" } }";	
	        try(
	    		FileOutputStream fos = new FileOutputStream(file);
	    		BufferedOutputStream bos = new BufferedOutputStream(fos)
			) {
	            byte[] bytes = data.getBytes();
	            bos.write(bytes);
	            bos.close();
	            fos.close();
	        } catch (IOException e) { e.printStackTrace(); }
		}

		if(dimName.equals("overworld") || dimName.equals("the_end") || dimName.equals("the_nether"))
		{		
			folder = new File(datapackFolder + File.separator + Constants.MOD_ID_SHORT + File.separator + "data" + File.separator + "minecraft" + File.separator + "dimension_type" + File.separator);
			file = new File(datapackFolder + File.separator + Constants.MOD_ID_SHORT + File.separator + "data" + File.separator + "minecraft" + File.separator + "dimension_type" + File.separator + dimName + ".json");
		} else {
			folder = new File(datapackFolder + File.separator + Constants.MOD_ID_SHORT + File.separator + "data" + File.separator + Constants.MOD_ID_SHORT + File.separator + "dimension_type" + File.separator);
			file = new File(datapackFolder + File.separator + Constants.MOD_ID_SHORT + File.separator + "data" + File.separator + Constants.MOD_ID_SHORT + File.separator + "dimension_type" + File.separator + dimName + ".json");			
		}
		
		if(!folder.exists())
		{
			folder.mkdirs();
		}
		// TODO: Make height/min_y configurable? Add name?
		data = "{ \"name\": \"\", \"height\":256, \"min_y\": 0, \"ultrawarm\": " + worldConfig.getUltraWarm() + ", \"infiniburn\": \"" + worldConfig.getInfiniburn() + "\", \"logical_height\": " + worldConfig.getLogicalHeight() + ", \"has_raids\": " + worldConfig.getHasRaids() + ", \"respawn_anchor_works\": " + worldConfig.getRespawnAnchorWorks() + ", \"bed_works\": " + worldConfig.getBedWorks() + ", \"piglin_safe\": " + worldConfig.getPiglinSafe() + ", \"natural\": " + worldConfig.getNatural() + ", \"coordinate_scale\": " + worldConfig.getCoordinateScale() + ", \"ambient_light\": " + worldConfig.getAmbientLight() + ", \"has_skylight\": " + worldConfig.getHasSkyLight() + ", \"has_ceiling\": " + worldConfig.getHasCeiling() + ", \"effects\": \"" + worldConfig.getEffectsLocation() + "\"" + (worldConfig.getFixedTime().isPresent() ? ", \"fixed_time\": " + worldConfig.getFixedTime().getAsLong() : "") + " }";
        try(    		        	
    		FileOutputStream fos = new FileOutputStream(file);
    		BufferedOutputStream bos = new BufferedOutputStream(fos)
		) {
            byte[] bytes = data.getBytes();
            bos.write(bytes);
            bos.close();
            fos.close();
        } catch (IOException e) { e.printStackTrace(); }
	}
}
