package com.pg85.otg.forge.dimensions;

import java.util.OptionalLong;
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
import net.minecraft.world.biome.ColumnFuzzedBiomeMagnifier;
import net.minecraft.world.biome.IBiomeMagnifier;
import net.minecraft.world.biome.provider.OverworldBiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DebugChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.NoiseChunkGenerator;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraftforge.common.world.ForgeWorldType;
import net.minecraftforge.registries.ForgeRegistries;

public class OTGDimensionType extends DimensionType
{
	public OTGDimensionType(OptionalLong fixedTime, boolean hasSkylight, boolean hasCeiling, boolean ultraWarm, boolean natural, double coordinateScale, boolean createDragonFight, boolean piglinSafe, boolean bedWorks, boolean respawnAnchorWorks, boolean hasRaids, int logicalHeight, IBiomeMagnifier biomeZoomer, ResourceLocation infiniburn, ResourceLocation effectsLocation, float ambientLight)
	{
		super(fixedTime, hasSkylight, hasCeiling, ultraWarm, natural, coordinateScale, createDragonFight, piglinSafe, bedWorks, respawnAnchorWorks, hasRaids, logicalHeight, biomeZoomer, infiniburn, effectsLocation, ambientLight);
	}
	
	// Used for MP when starting the server, with settings from server.properties.
	public static DimensionGeneratorSettings createOTGSettings(DynamicRegistries dynamicRegistries, long seed, boolean generateStructures, boolean bonusChest, String generatorSettings)
	{
		MutableRegistry<DimensionType> dimensionTypesRegistry = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
		Registry<Biome> biomesRegistry = dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY);
		Registry<DimensionSettings> dimensionSettingsRegistry = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
		
		// If there is a dimensionconfig for the generatorsettings, use that. Otherwise find a preset by name.
		DimensionConfig dimConfig = DimensionConfig.fromDisk(generatorSettings);
		SimpleRegistry<Dimension> dimensions = null;
		Preset preset = null;
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
				dimConfig.OverWorld = new OTGOverWorld(preset.getFolderName(), seed, null, null);
				dimensions = DimensionType.defaultDimensions(dimensionTypesRegistry, biomesRegistry, dimensionSettingsRegistry, seed);
			}
		} else {
			// Non-otg overworld, generatorsettings contains non-otg world type.
			ForgeWorldType def = dimConfig.OverWorld.NonOTGWorldType == null || dimConfig.OverWorld.NonOTGWorldType.trim().length() == 0 ? null : ForgeRegistries.WORLD_TYPES.getValue(new ResourceLocation(dimConfig.OverWorld.NonOTGWorldType));
			if(def != null)
			{
				DimensionGeneratorSettings existingDimSetting = def.createSettings(dynamicRegistries, seed, generateStructures, bonusChest, dimConfig.OverWorld.NonOTGGeneratorSettings);
				dimensions = existingDimSetting.dimensions();
			} else {				
				Registry<DimensionType> registry2 = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
				Registry<Biome> registry = dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY);
				Registry<DimensionSettings> registry1 = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
				SimpleRegistry<Dimension> simpleregistry = DimensionType.defaultDimensions(registry2, registry, registry1, seed);		      
				DimensionGeneratorSettings existingDimSetting = null;
				switch(dimConfig.OverWorld.NonOTGWorldType == null ? "" : dimConfig.OverWorld.NonOTGWorldType)
				{
					case "flat":
						JsonObject jsonobject = dimConfig.OverWorld.NonOTGGeneratorSettings != null && !dimConfig.OverWorld.NonOTGGeneratorSettings.isEmpty() ? JSONUtils.parse(dimConfig.OverWorld.NonOTGGeneratorSettings) : new JsonObject();
						Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, jsonobject);
						existingDimSetting = new DimensionGeneratorSettings(seed, generateStructures, bonusChest, DimensionGeneratorSettings.withOverworld(registry2, simpleregistry, new FlatChunkGenerator(FlatGenerationSettings.CODEC.parse(dynamic).resultOrPartial(LogManager.getLogger()::error).orElseGet(() -> {
						return FlatGenerationSettings.getDefault(registry);
						}))));
					case "debug_all_block_states":
						existingDimSetting = new DimensionGeneratorSettings(seed, generateStructures, bonusChest, DimensionGeneratorSettings.withOverworld(registry2, simpleregistry, new DebugChunkGenerator(registry)));
					case "amplified":
						existingDimSetting = new DimensionGeneratorSettings(seed, generateStructures, bonusChest, DimensionGeneratorSettings.withOverworld(registry2, simpleregistry, new NoiseChunkGenerator(new OverworldBiomeProvider(seed, false, false, registry), seed, () -> {
							return registry1.getOrThrow(DimensionSettings.AMPLIFIED);
						})));
					case "largebiomes":
						existingDimSetting = new DimensionGeneratorSettings(seed, generateStructures, bonusChest, DimensionGeneratorSettings.withOverworld(registry2, simpleregistry, new NoiseChunkGenerator(new OverworldBiomeProvider(seed, false, true, registry), seed, () -> {
							return registry1.getOrThrow(DimensionSettings.OVERWORLD);
						})));
					default:
						existingDimSetting = new DimensionGeneratorSettings(seed, generateStructures, bonusChest, DimensionGeneratorSettings.withOverworld(registry2, simpleregistry, DimensionGeneratorSettings.makeDefaultOverworld(registry, registry1, seed)));
				}
				dimensions = existingDimSetting.dimensions();
			}
		}

		return OTGDimensionType.createOTGDimensionGeneratorSettings(
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
	public static DimensionGeneratorSettings createOTGDimensionGeneratorSettings(DimensionConfig dimConfig, MutableRegistry<DimensionType> dimensionTypesRegistry, Registry<Biome> biomesRegistry, Registry<DimensionSettings> dimensionSettingsRegistry, long seed, boolean generateFeatures, boolean generateBonusChest, SimpleRegistry<Dimension> defaultDimensions)
	{
		// Create a new registry object and register dimensions to it.
		SimpleRegistry<Dimension> dimensions = new SimpleRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
		boolean nonOTGOverWorld = dimConfig.OverWorld.PresetFolderName == null;

		if(dimConfig.OverWorld != null && dimConfig.OverWorld.PresetFolderName != null && !nonOTGOverWorld)
		{
			ChunkGenerator chunkGenerator = new OTGNoiseChunkGenerator(
				dimConfig.OverWorld.PresetFolderName, new OTGBiomeProvider(dimConfig.OverWorld.PresetFolderName, seed, false, false, biomesRegistry), seed,
				() -> dimensionSettingsRegistry.getOrThrow(DimensionSettings.OVERWORLD) // TODO: Add OTG DimensionSettings?
			);
			RegistryKey<DimensionType> dimTypeRegistryKey = RegistryKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation(Constants.MOD_ID_SHORT, "overworld"));
			addDimension(dimConfig.OverWorld.PresetFolderName, dimensions, dimensionTypesRegistry, Dimension.OVERWORLD, chunkGenerator, dimTypeRegistryKey);
		}
		if(dimConfig.Nether != null && dimConfig.Nether.PresetFolderName != null)
		{
			ChunkGenerator chunkGenerator = new OTGNoiseChunkGenerator(
					dimConfig.Nether.PresetFolderName, new OTGBiomeProvider(dimConfig.Nether.PresetFolderName, dimConfig.Nether.Seed != -1l ? dimConfig.Nether.Seed : seed, false, false, biomesRegistry), dimConfig.Nether.Seed != -1l ? dimConfig.Nether.Seed : seed,
				() -> dimensionSettingsRegistry.getOrThrow(DimensionSettings.NETHER) // TODO: Add OTG DimensionSettings?
			);
			RegistryKey<DimensionType> dimTypeRegistryKey = RegistryKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation(Constants.MOD_ID_SHORT, "the_nether"));
			addDimension(dimConfig.Nether.PresetFolderName, dimensions, dimensionTypesRegistry, Dimension.NETHER, chunkGenerator, dimTypeRegistryKey);
		}
		if(dimConfig.End != null && dimConfig.End.PresetFolderName != null)
		{
			ChunkGenerator chunkGenerator = new OTGNoiseChunkGenerator(
					dimConfig.End.PresetFolderName, new OTGBiomeProvider(dimConfig.End.PresetFolderName, dimConfig.End.Seed != 1l ? dimConfig.End.Seed : seed, false, false, biomesRegistry), dimConfig.End.Seed != 1l ? dimConfig.End.Seed : seed,
				() -> dimensionSettingsRegistry.getOrThrow(DimensionSettings.END) // TODO: Add OTG DimensionSettings?
			);
			RegistryKey<DimensionType> dimTypeRegistryKey = RegistryKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation(Constants.MOD_ID_SHORT, "the_end"));
			addDimension(dimConfig.End.PresetFolderName, dimensions, dimensionTypesRegistry, Dimension.END, chunkGenerator, dimTypeRegistryKey);
		}
		if(dimConfig.Dimensions != null)
		{
			for(OTGDimension otgDim : dimConfig.Dimensions)
			{
				if(otgDim.PresetFolderName != null)
				{
					ChunkGenerator chunkGenerator = new OTGNoiseChunkGenerator(
						otgDim.PresetFolderName, new OTGBiomeProvider(otgDim.PresetFolderName, otgDim.Seed != -1l ? otgDim.Seed : seed, false, false, biomesRegistry), otgDim.Seed != -1l ? otgDim.Seed : seed,
						() -> dimensionSettingsRegistry.getOrThrow(DimensionSettings.OVERWORLD) // TODO: Add OTG DimensionSettings?
					);
					RegistryKey<Dimension> dimRegistryKey = RegistryKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation(Constants.MOD_ID_SHORT, otgDim.PresetFolderName.trim().replace(" ", "_").toLowerCase()));
					RegistryKey<DimensionType> dimTypeRegistryKey = RegistryKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation(Constants.MOD_ID_SHORT, otgDim.PresetFolderName.trim().replace(" ", "_").toLowerCase()));
					addDimension(otgDim.PresetFolderName, dimensions, dimensionTypesRegistry, dimRegistryKey, chunkGenerator, dimTypeRegistryKey);
				}
			}
		}

		// Register default dimensions (if we're not overriding them with otg dimensions)
		for(Entry<RegistryKey<Dimension>, Dimension> entry : defaultDimensions.entrySet())
		{
			RegistryKey<Dimension> registrykey = entry.getKey();
			if (
				(dimConfig.OverWorld == null || dimConfig.OverWorld.PresetFolderName == null || registrykey != Dimension.OVERWORLD) &&
				(dimConfig.Nether == null || dimConfig.Nether.PresetFolderName == null || registrykey != Dimension.NETHER) && 
				(dimConfig.End == null || dimConfig.End.PresetFolderName == null || registrykey != Dimension.END)
			)
			{
				dimensions.register(registrykey, entry.getValue(), defaultDimensions.lifecycle(entry.getValue()));
			}
		}

		return new DimensionGeneratorSettings(
			seed,
			generateFeatures,
			generateBonusChest,
			dimensions
		);
	}

	private static void addDimension(String presetFolderName, SimpleRegistry<Dimension> dimensions, MutableRegistry<DimensionType> dimensionTypeRegistry, RegistryKey<Dimension> dimRegistryKey, ChunkGenerator chunkGenerator, RegistryKey<DimensionType> dimTypeRegistryKey)
	{
		Preset preset = OTG.getEngine().getPresetLoader().getPresetByFolderName(presetFolderName);
		IWorldConfig worldConfig = preset.getWorldConfig();
		
		// Register OTG DimensionType with settings from WorldConfig
		DimensionType otgOverWorld = new DimensionType(
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
			worldConfig.getLogicalHeight(),
			ColumnFuzzedBiomeMagnifier.INSTANCE,
			new ResourceLocation(worldConfig.getInfiniburn()),
			new ResourceLocation(worldConfig.getEffectsLocation()),
			worldConfig.getAmbientLight()
		);
		dimensionTypeRegistry.registerOrOverride(OptionalInt.empty(), dimTypeRegistryKey, otgOverWorld, Lifecycle.stable());
		
		Dimension dimension = dimensions.get(dimRegistryKey);
		Supplier<DimensionType> supplier = () -> {
			// TODO: Is this supposed to be a fallback? Normally falls back to dimensionTypeRegistry.getOrThrow(DimensionType.OVERWORLD_LOCATION)
			return dimension == null ? dimensionTypeRegistry.getOrThrow(dimTypeRegistryKey) : dimension.type();
		};
		dimensions.register(dimRegistryKey, new Dimension(supplier, chunkGenerator), Lifecycle.stable());		
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

		folder = new File(datapackFolder + File.separator + "otg" + File.separator + "data" + File.separator + Constants.MOD_ID_SHORT + File.separator + "dimension_type" + File.separator);
		file = new File(datapackFolder + File.separator + "otg" + File.separator + "data" + File.separator + Constants.MOD_ID_SHORT + File.separator + "dimension_type" + File.separator + dimName + ".json");
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
