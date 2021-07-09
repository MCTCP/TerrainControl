package com.pg85.otg.forge.dimensions;

import java.util.OptionalLong;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.OptionalInt;
import java.util.function.Supplier;

import com.mojang.serialization.Lifecycle;
import com.pg85.otg.OTG;
import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.config.dimensions.DimensionConfig.OTGDimension;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.forge.biome.OTGBiomeProvider;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.presets.Preset;

import net.minecraft.client.Minecraft;
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
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;

public class OTGDimensionType extends DimensionType
{
	public OTGDimensionType(OptionalLong fixedTime, boolean hasSkylight, boolean hasCeiling, boolean ultraWarm, boolean natural, double coordinateScale, boolean createDragonFight, boolean piglinSafe, boolean bedWorks, boolean respawnAnchorWorks, boolean hasRaids, int logicalHeight, IBiomeMagnifier biomeZoomer, ResourceLocation infiniburn, ResourceLocation effectsLocation, float ambientLight)
	{
		super(fixedTime, hasSkylight, hasCeiling, ultraWarm, natural, coordinateScale, createDragonFight, piglinSafe, bedWorks, respawnAnchorWorks, hasRaids, logicalHeight, biomeZoomer, infiniburn, effectsLocation, ambientLight);
	}
	
	// Used for MP when starting the server, with settings from server.properties.
	public static DimensionGeneratorSettings createOTGSettings(DynamicRegistries dynamicRegistries, long seed, boolean generateStructures, boolean bonusChest, String generatorSettings)
	{
		// Find the preset defined in generatorSettings, if none use the default preset.
		Preset preset = OTG.getEngine().getPresetLoader().getPresetByShortNameOrFolderName(generatorSettings);
		String presetFolderName = OTG.getEngine().getPresetLoader().getDefaultPresetFolderName();
		if(preset != null)
		{
			presetFolderName = preset.getFolderName();
		}

		MutableRegistry<DimensionType> dimensionTypesRegistry = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
		Registry<Biome> biomesRegistry = dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY);
		Registry<DimensionSettings> dimensionSettingsRegistry = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);

		return OTGDimensionType.createOTGDimensionGeneratorSettings(
			dimensionTypesRegistry,
			biomesRegistry,
			dimensionSettingsRegistry,
			seed,
			generateStructures,
			bonusChest,
			DimensionType.defaultDimensions(dimensionTypesRegistry, biomesRegistry, dimensionSettingsRegistry, seed),
			presetFolderName
		);
	}
	
	// Used for SP and MP
	public static DimensionGeneratorSettings createOTGDimensionGeneratorSettings(MutableRegistry<DimensionType> dimensionTypesRegistry, Registry<Biome> biomesRegistry, Registry<DimensionSettings> dimensionSettingsRegistry, long seed, boolean generateFeatures, boolean generateBonusChest, SimpleRegistry<Dimension> dimensions, String presetFolderName)
	{
		SimpleRegistry<Dimension> simpleRegistry = new SimpleRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());

		DimensionConfig dimConfig = DimensionConfig.fromDisk(presetFolderName);
		if(dimConfig == null)
		{
			dimConfig = new DimensionConfig(presetFolderName);
			dimConfig.OverWorld = new OTGDimension(presetFolderName, seed);
		}
		if(dimConfig.OverWorld != null)
		{
			ChunkGenerator chunkGenerator = new OTGNoiseChunkGenerator(
				dimConfig.OverWorld.PresetFolderName, new OTGBiomeProvider(dimConfig.OverWorld.PresetFolderName, dimConfig.OverWorld.Seed, false, false, biomesRegistry), dimConfig.OverWorld.Seed,
				() -> dimensionSettingsRegistry.getOrThrow(DimensionSettings.OVERWORLD) // TODO: Add OTG DimensionSettings?
			);
			RegistryKey<DimensionType> dimTypeRegistryKey = RegistryKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation(Constants.MOD_ID_SHORT, "overworld"));
			addDimension(dimConfig.OverWorld.PresetFolderName, simpleRegistry, dimensionTypesRegistry, Dimension.OVERWORLD, chunkGenerator, dimTypeRegistryKey);
		}
		if(dimConfig.Nether != null)
		{
			ChunkGenerator chunkGenerator = new OTGNoiseChunkGenerator(
					dimConfig.Nether.PresetFolderName, new OTGBiomeProvider(dimConfig.Nether.PresetFolderName, dimConfig.Nether.Seed, false, false, biomesRegistry), dimConfig.Nether.Seed,
				() -> dimensionSettingsRegistry.getOrThrow(DimensionSettings.NETHER) // TODO: Add OTG DimensionSettings?
			);
			RegistryKey<DimensionType> dimTypeRegistryKey = RegistryKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation(Constants.MOD_ID_SHORT, "the_nether"));
			addDimension(dimConfig.Nether.PresetFolderName, simpleRegistry, dimensionTypesRegistry, Dimension.NETHER, chunkGenerator, dimTypeRegistryKey);
		}
		if(dimConfig.End != null)
		{
			ChunkGenerator chunkGenerator = new OTGNoiseChunkGenerator(
					dimConfig.End.PresetFolderName, new OTGBiomeProvider(dimConfig.End.PresetFolderName, dimConfig.End.Seed, false, false, biomesRegistry), dimConfig.End.Seed,
				() -> dimensionSettingsRegistry.getOrThrow(DimensionSettings.END) // TODO: Add OTG DimensionSettings?
			);
			RegistryKey<DimensionType> dimTypeRegistryKey = RegistryKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation(Constants.MOD_ID_SHORT, "the_end"));
			addDimension(dimConfig.End.PresetFolderName, simpleRegistry, dimensionTypesRegistry, Dimension.END, chunkGenerator, dimTypeRegistryKey);
		}
		if(dimConfig.Dimensions != null)
		{
			for(OTGDimension otgDim : dimConfig.Dimensions)
			{
				ChunkGenerator chunkGenerator = new OTGNoiseChunkGenerator(
					otgDim.PresetFolderName, new OTGBiomeProvider(otgDim.PresetFolderName, otgDim.Seed, false, false, biomesRegistry), otgDim.Seed,
					() -> dimensionSettingsRegistry.getOrThrow(DimensionSettings.OVERWORLD) // TODO: Add OTG DimensionSettings?
				);
				RegistryKey<Dimension> dimRegistryKey = RegistryKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation(Constants.MOD_ID_SHORT, otgDim.PresetFolderName.trim().replace(" ", "_").toLowerCase()));
				RegistryKey<DimensionType> dimTypeRegistryKey = RegistryKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation(Constants.MOD_ID_SHORT, otgDim.PresetFolderName.trim().replace(" ", "_").toLowerCase()));
				addDimension(otgDim.PresetFolderName, simpleRegistry, dimensionTypesRegistry, dimRegistryKey, chunkGenerator, dimTypeRegistryKey);
			}
		}

		for(Entry<RegistryKey<Dimension>, Dimension> entry : dimensions.entrySet())
		{
			RegistryKey<Dimension> registrykey = entry.getKey();
			// When replacing nether and/or end, don't copy them from global registry.
			if (
				(dimConfig.OverWorld == null || registrykey != Dimension.OVERWORLD) &&
				(dimConfig.Nether == null || registrykey != Dimension.NETHER) && 
				(dimConfig.End == null || registrykey != Dimension.END)
			)
			{
				simpleRegistry.register(registrykey, entry.getValue(), dimensions.lifecycle(entry.getValue()));
			}
		}
		
		return new DimensionGeneratorSettings(
			seed,
			generateFeatures,
			generateBonusChest,
			simpleRegistry
		);
	}

	private static void addDimension(String presetFolderName, SimpleRegistry<Dimension> simpleRegistry, MutableRegistry<DimensionType> dimensionTypeRegistry, RegistryKey<Dimension> dimRegistryKey, ChunkGenerator chunkGenerator, RegistryKey<DimensionType> dimTypeRegistryKey)
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
		
		Dimension dimension = simpleRegistry.get(dimRegistryKey);
		Supplier<DimensionType> supplier = () -> {
			// TODO: Is this supposed to be a fallback? Normally falls back to dimensionTypeRegistry.getOrThrow(DimensionType.OVERWORLD_LOCATION)
			return dimension == null ? dimensionTypeRegistry.getOrThrow(dimTypeRegistryKey) : dimension.type();
		};
		simpleRegistry.register(dimRegistryKey, new Dimension(supplier, chunkGenerator), Lifecycle.stable());		
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
