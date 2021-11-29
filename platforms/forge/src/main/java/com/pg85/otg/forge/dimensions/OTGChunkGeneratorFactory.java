package com.pg85.otg.forge.dimensions;

import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
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
		return OTGDimensionType.createOTGSettings(dynamicRegistries, seed, generateStructures, bonusChest, generatorSettings);
	}
}
