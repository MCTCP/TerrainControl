package com.pg85.otg.forge.dimensions;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraftforge.common.world.ForgeWorldType.IChunkGeneratorFactory;

public class OTGChunkGeneratorFactory implements IChunkGeneratorFactory
{
	// Only used for SP world creation by Forge, we use our own WorldType registration logic for SP (see OTGPlugin/OTGGui).
	@Override
	public ChunkGenerator createChunkGenerator(Registry<Biome> biomeRegistry, Registry<NoiseGeneratorSettings> dimensionSettingsRegistry, long seed, String generatorSettings)
	{
		return null;
	}

	// Used for MP when starting the server, with settings from server.properties.
	public WorldGenSettings createSettings(RegistryAccess dynamicRegistries, long seed, boolean generateStructures, boolean bonusChest, String generatorSettings)
	{	
		return OTGDimensionTypeHelper.createOTGSettings(dynamicRegistries, seed, generateStructures, bonusChest, generatorSettings);
	}
}
