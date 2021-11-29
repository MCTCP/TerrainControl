package com.pg85.otg.forge.dimensions;

import java.util.OptionalLong;
import java.util.Random;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.OptionalInt;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.pg85.otg.OTG;
import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.config.dimensions.DimensionConfig.OTGDimension;
import com.pg85.otg.config.dimensions.DimensionConfig.OTGOverWorld;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.forge.biome.OTGBiomeProvider;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.MappedRegistry;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FuzzyOffsetConstantColumnBiomeZoomer;
import net.minecraft.world.level.biome.BiomeZoomer;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraftforge.common.world.ForgeWorldType;
import net.minecraftforge.registries.ForgeRegistries;

public class OTGDimensionTypeHelper
{
	public static DimensionType make(OptionalLong fixedTime, boolean hasSkylight, boolean hasCeiling, boolean ultraWarm, boolean natural, double coordinateScale, boolean createDragonFight, boolean piglinSafe, boolean bedWorks, boolean respawnAnchorWorks, boolean hasRaids, int minY, int height, int logicalHeight, BiomeZoomer biomeZoomer, ResourceLocation infiniburn, ResourceLocation effectsLocation, float ambientLight)
	{
		return DimensionType.create(fixedTime, hasSkylight, hasCeiling, ultraWarm, natural, coordinateScale, createDragonFight, piglinSafe, bedWorks, respawnAnchorWorks, hasRaids, minY, height, logicalHeight, biomeZoomer, infiniburn, effectsLocation, ambientLight);
	}
	
	// Used for MP when starting the server, with settings from server.properties.
	public static WorldGenSettings createOTGSettings(RegistryAccess dynamicRegistries, long seed, boolean generateStructures, boolean bonusChest, String generatorSettings)
	{
		WritableRegistry<DimensionType> dimensionTypesRegistry = dynamicRegistries.ownedRegistryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
		Registry<Biome> biomesRegistry = dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY);
		Registry<NoiseGeneratorSettings> dimensionSettingsRegistry = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
		
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
				dimensions = DimensionType.defaultDimensions(dimensionTypesRegistry, biomesRegistry, dimensionSettingsRegistry, seed);
			}
		} else {
			dimConfigName = generatorSettings;
			// Non-otg overworld, generatorsettings contains non-otg world type.
			ForgeWorldType def = dimConfig.Overworld.NonOTGWorldType == null || dimConfig.Overworld.NonOTGWorldType.trim().length() == 0 ? null : ForgeRegistries.WORLD_TYPES.getValue(new ResourceLocation(dimConfig.Overworld.NonOTGWorldType));
			if(def != null)
			{
				WorldGenSettings existingDimSetting = def.createSettings(dynamicRegistries, seed, generateStructures, bonusChest, dimConfig.Overworld.NonOTGGeneratorSettings);
				dimensions = existingDimSetting.dimensions();
			} else {
				Registry<DimensionType> registry2 = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
				Registry<Biome> registry = dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY);
				Registry<NoiseGeneratorSettings> registry1 = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
				MappedRegistry<LevelStem> simpleregistry = DimensionType.defaultDimensions(registry2, registry, registry1, seed);		      
				WorldGenSettings existingDimSetting = null;
				switch(dimConfig.Overworld.NonOTGWorldType == null ? "" : dimConfig.Overworld.NonOTGWorldType)
				{
					case "flat":
						JsonObject jsonobject = dimConfig.Overworld.NonOTGGeneratorSettings != null && !dimConfig.Overworld.NonOTGGeneratorSettings.isEmpty() ? GsonHelper.parse(dimConfig.Overworld.NonOTGGeneratorSettings) : new JsonObject();
						Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, jsonobject);
						existingDimSetting = new WorldGenSettings(seed, generateStructures, bonusChest, WorldGenSettings.withOverworld(registry2, simpleregistry, new FlatLevelSource(FlatLevelGeneratorSettings.CODEC.parse(dynamic).resultOrPartial(LogManager.getLogger()::error).orElseGet(() -> {
						return FlatLevelGeneratorSettings.getDefault(registry);
						}))));
						break;
					case "debug_all_block_states":
						existingDimSetting = new WorldGenSettings(seed, generateStructures, bonusChest, WorldGenSettings.withOverworld(registry2, simpleregistry, new DebugLevelSource(registry)));
						break;
					case "amplified":
						existingDimSetting = new WorldGenSettings(seed, generateStructures, bonusChest, WorldGenSettings.withOverworld(registry2, simpleregistry, new NoiseBasedChunkGenerator(new OverworldBiomeSource(seed, false, false, registry), seed, () -> {
							return registry1.getOrThrow(NoiseGeneratorSettings.AMPLIFIED);
						})));
						break;
					case "largebiomes":
						existingDimSetting = new WorldGenSettings(seed, generateStructures, bonusChest, WorldGenSettings.withOverworld(registry2, simpleregistry, new NoiseBasedChunkGenerator(new OverworldBiomeSource(seed, false, true, registry), seed, () -> {
							return registry1.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
						})));
						break;
					default:
						existingDimSetting = new WorldGenSettings(seed, generateStructures, bonusChest, WorldGenSettings.withOverworld(registry2, simpleregistry, WorldGenSettings.makeDefaultOverworld(registry, registry1, seed)));
						break;
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
			seed,
			generateStructures,
			bonusChest,
			dimensions
		);
	}
	
	// Used for SP and MP
	public static WorldGenSettings createOTGDimensionGeneratorSettings(String dimConfigName, DimensionConfig dimConfig, WritableRegistry<DimensionType> dimensionTypesRegistry, Registry<Biome> biomesRegistry, Registry<NoiseGeneratorSettings> dimensionSettingsRegistry, long seed, boolean generateFeatures, boolean generateBonusChest, MappedRegistry<LevelStem> defaultDimensions)
	{
		// Create a new registry object and register dimensions to it.
		MappedRegistry<LevelStem> dimensions = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
		boolean nonOTGOverWorld = dimConfig.Overworld.PresetFolderName == null;

		if(dimConfig.Overworld != null && dimConfig.Overworld.PresetFolderName != null && !nonOTGOverWorld)
		{
			ChunkGenerator chunkGenerator = new OTGNoiseChunkGenerator(
				dimConfig.Overworld.PresetFolderName, dimConfigName, new OTGBiomeProvider(dimConfig.Overworld.PresetFolderName, seed, false, false, biomesRegistry), seed,
				() -> dimensionSettingsRegistry.getOrThrow(NoiseGeneratorSettings.OVERWORLD) // TODO: Add OTG DimensionSettings?
			);
			addDimension(dimConfig.Overworld.PresetFolderName, dimensions, dimensionTypesRegistry, LevelStem.OVERWORLD, chunkGenerator, DimensionType.OVERWORLD_LOCATION);
		}
		if(dimConfig.Nether != null && dimConfig.Nether.PresetFolderName != null)
		{
			long dimSeed = dimConfig.Nether.Seed != -1l ? dimConfig.Nether.Seed : new Random().nextLong();
			ChunkGenerator chunkGenerator = new OTGNoiseChunkGenerator(
					dimConfig.Nether.PresetFolderName, dimConfigName, new OTGBiomeProvider(dimConfig.Nether.PresetFolderName, dimSeed, false, false, biomesRegistry), dimSeed,
				() -> dimensionSettingsRegistry.getOrThrow(NoiseGeneratorSettings.NETHER) // TODO: Add OTG DimensionSettings?
			);
			addDimension(dimConfig.Nether.PresetFolderName, dimensions, dimensionTypesRegistry, LevelStem.NETHER, chunkGenerator, DimensionType.NETHER_LOCATION);
		}
		if(dimConfig.End != null && dimConfig.End.PresetFolderName != null)
		{
			long dimSeed = dimConfig.End.Seed != -1l ? dimConfig.End.Seed : new Random().nextLong();
			ChunkGenerator chunkGenerator = new OTGNoiseChunkGenerator(
					dimConfig.End.PresetFolderName, dimConfigName, new OTGBiomeProvider(dimConfig.End.PresetFolderName, dimSeed, false, false, biomesRegistry), dimSeed,
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
						otgDim.PresetFolderName, dimConfigName, new OTGBiomeProvider(otgDim.PresetFolderName, dimSeed, false, false, biomesRegistry), dimSeed,
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
			FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE,
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
		data = "{ \"ultrawarm\": " + worldConfig.getUltraWarm() + ", \"natural\": " + worldConfig.getNatural() + ", \"piglin_safe\": " + worldConfig.getPiglinSafe() + ", \"respawn_anchor_works\": " + worldConfig.getRespawnAnchorWorks() + ", \"bed_works\": " + worldConfig.getBedWorks() + ", \"has_raids\": " + worldConfig.getHasRaids() + ", \"has_skylight\": " + worldConfig.getHasSkyLight() + ", \"has_ceiling\": " + worldConfig.getHasCeiling() + ", \"coordinate_scale\": " + worldConfig.getCoordinateScale() + ", \"ambient_light\": " + worldConfig.getAmbientLight() + ", \"logical_height\": " + worldConfig.getLogicalHeight() + ", \"effects\": \"" + worldConfig.getEffectsLocation() + "\", \"infiniburn\": \"" + worldConfig.getInfiniburn() + "\" }";
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
