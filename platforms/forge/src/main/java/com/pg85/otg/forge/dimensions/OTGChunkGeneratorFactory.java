package com.pg85.otg.forge.dimensions;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraftforge.common.world.ForgeWorldPreset.IChunkGeneratorFactory;

public class OTGChunkGeneratorFactory implements IChunkGeneratorFactory
{
	// Only used for SP world creation by Forge, we use our own WorldType registration logic for SP (see OTGPlugin/OTGGui).
	@Override
	public ChunkGenerator createChunkGenerator(RegistryAccess dynamicRegistries, long seed, String generatorSettings)
	{
		return null;
	}

	// Used for MP when starting the server, with settings from server.properties.
	public WorldGenSettings createSettings(RegistryAccess dynamicRegistries, long seed, boolean generateStructures, boolean bonusChest, String generatorSettings)
	{	
		return OTGDimensionTypeHelper.createOTGSettings(dynamicRegistries, seed, generateStructures, bonusChest, generatorSettings);
	}
}
