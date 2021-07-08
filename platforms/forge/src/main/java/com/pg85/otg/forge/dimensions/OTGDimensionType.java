package com.pg85.otg.forge.dimensions;

import java.util.OptionalLong;
import java.util.Map.Entry;
import java.util.function.Supplier;

import com.mojang.serialization.Lifecycle;
import com.pg85.otg.OTG;
import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.forge.biome.OTGBiomeProvider;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.presets.Preset;

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
		// TODO: Change this to use DimensionConfig, so we can load multiple dimensions/presets.
		Preset preset = OTG.getEngine().getPresetLoader().getPresetByShortNameOrFolderName(generatorSettings);
		String presetFolderName = OTG.getEngine().getPresetLoader().getDefaultPresetFolderName();
		if(preset != null)
		{
			presetFolderName = preset.getFolderName();
		}

		Registry<DimensionType> dimensionTypesRegistry = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
		Registry<Biome> biomesRegistry = dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY);
		Registry<DimensionSettings> dimensionSettingsRegistry = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);

		return OTGDimensionType.createOTGDimensionGeneratorSettings(
			dynamicRegistries, 
			seed, 
			generateStructures, 
			bonusChest, 
			DimensionType.defaultDimensions(dimensionTypesRegistry, biomesRegistry, dimensionSettingsRegistry, seed),
			new DimensionConfig(presetFolderName)
		);
	}	
	
	// Used for SP and MP
	public static DimensionGeneratorSettings createOTGDimensionGeneratorSettings(DynamicRegistries dynamicRegistries, long seed, boolean generateFeatures, boolean generateBonusChest, SimpleRegistry<Dimension> dimensions, DimensionConfig dimensionConfig)
	{
		Registry<DimensionType> dimensionTypesRegistry = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
		Registry<Biome> biomesRegistry = dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY);
		Registry<DimensionSettings> dimensionSettingsRegistry = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);	

		// TODO: Change this to use DimensionConfig, so we can load multiple dimensions/presets.
		Preset preset = OTG.getEngine().getPresetLoader().getPresetByFolderName(dimensionConfig.PresetFolderName);
		IWorldConfig worldConfig = preset.getWorldConfig();
		
		// Register OTG DimensionType with settings from WorldConfig
		RegistryKey<DimensionType> otgOverworldLocation = RegistryKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("otg_overworld"));
		DimensionType otgOverWorld = new OTGDimensionType(
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
		MutableRegistry<DimensionType> mutableregistry = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);		
		mutableregistry.register(otgOverworldLocation, otgOverWorld, Lifecycle.stable());
		
		return new DimensionGeneratorSettings(
			seed,
			generateFeatures,
			generateBonusChest,
			withOverworld(
				otgOverworldLocation,
				dimensionTypesRegistry,
				dimensions,
				new OTGNoiseChunkGenerator(
					dimensionConfig,
					new OTGBiomeProvider(
						dimensionConfig.PresetFolderName,
						seed,
						false,
						false,
						biomesRegistry
					),
					seed,
					() -> dimensionSettingsRegistry.getOrThrow(DimensionSettings.OVERWORLD) // TODO: Add OTG DimensionSettings?
				)
			)
		);
	}

	private static SimpleRegistry<Dimension> withOverworld(RegistryKey<DimensionType> otgOverworldLocation, Registry<DimensionType> dimensionTypeRegistry, SimpleRegistry<Dimension> dimensionRegistry, ChunkGenerator chunkGenerator)
	{
		Supplier<DimensionType> supplier = () -> {
			return dimensionTypeRegistry.getOrThrow(otgOverworldLocation);
		};

		SimpleRegistry<Dimension> simpleregistry = new SimpleRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
		simpleregistry.register(Dimension.OVERWORLD, new Dimension(supplier, chunkGenerator), Lifecycle.stable());

		// Replace nether or end with OTG dimension
		//simpleregistry.register(Dimension.NETHER, new Dimension(supplier, chunkGenerator), Lifecycle.stable());
		//simpleregistry.register(Dimension.END, new Dimension(supplier, chunkGenerator), Lifecycle.stable());

		// Add custom OTG dimensions
		//RegistryKey<Dimension> OTGDim = RegistryKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("otg_dim"));
		//simpleregistry.register(OTGDim, new Dimension(supplier, chunkGenerator), Lifecycle.stable());
		//

		for(Entry<RegistryKey<Dimension>, Dimension> entry : dimensionRegistry.entrySet())
		{
			RegistryKey<Dimension> registrykey = entry.getKey();
			if (registrykey != Dimension.OVERWORLD)
			// When replacing nether and/or end, don't copy them from global registry.
			//if (registrykey != Dimension.OVERWORLD && registrykey != Dimension.NETHER && registrykey != Dimension.END)
			{
				simpleregistry.register(registrykey, entry.getValue(), dimensionRegistry.lifecycle(entry.getValue()));
			}
		}
		return simpleregistry;
	}
}
