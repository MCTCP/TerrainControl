package com.pg85.otg.common;

import java.util.Random;

import com.pg85.otg.common.materials.LocalMaterialData;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.world.WorldConfig;
import com.pg85.otg.customobjects.SpawnableObject;
import com.pg85.otg.customobjects.bofunctions.EntityFunction;
import com.pg85.otg.exception.BiomeNotFoundException;
import com.pg85.otg.gen.ChunkBuffer;
import com.pg85.otg.gen.biome.BiomeGenerator;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.minecraft.defaults.TreeType;

public abstract class LocalWorldGenRegion
{
	private WorldConfig worldConfig;
	
	protected LocalWorldGenRegion(WorldConfig worldConfig, long seed)
	{
		this.worldConfig = worldConfig;
	}
	
	public WorldConfig getWorldConfig()
	{
		return this.worldConfig;
	}

	public abstract String getWorldName();
	public abstract long getSeed();	
	public abstract Random getWorldRandom();
	public abstract LocalBiome getBiome(int x, int z);
	public abstract BiomeConfig getBiomeConfig(int x, int z) throws BiomeNotFoundException;
	public abstract BiomeConfig getBiomeConfigForPopulation(int worldX, int worldZ, ChunkCoordinate chunkBeingPopulated);
	public abstract LocalBiome getBiomeForPopulation(int worldX, int worldZ, ChunkCoordinate chunkBeingPopulated);
	public abstract void cacheBiomesForPopulation(ChunkCoordinate chunkCoord);
	public abstract void invalidatePopulationBiomeCache();
	public abstract double getBiomeBlocksNoiseValue(int xInWorld, int zInWorld);
	public abstract boolean placeTree(TreeType type, Random rand, int x, int y, int z);
	public abstract LocalMaterialData getMaterial(int x, int y, int z, ChunkCoordinate chunkBeingPopulated);
	public abstract int getBlockAboveLiquidHeight(int x, int z, ChunkCoordinate chunkBeingPopulated);
	public abstract int getBlockAboveSolidHeight(int x, int z, ChunkCoordinate chunkBeingPopulated);
	public abstract int getHighestBlockAboveYAt(int x, int z, ChunkCoordinate chunkBeingPopulated);
	public abstract int getHighestBlockYAt(int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, boolean ignoreLeaves, ChunkCoordinate chunkBeingPopulated);
	public abstract int getHeightMapHeight(int x, int z, ChunkCoordinate chunkBeingPopulated);
	public abstract int getLightLevel(int x, int y, int z, ChunkCoordinate chunkBeingPopulated);
	public abstract void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, ChunkCoordinate chunkBeingPopulated, boolean replaceBlocks);
	public abstract void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, ChunkCoordinate chunkBeingPopulated, BiomeConfig biomeConfig, boolean replaceBlocks);
	
	// Not yet implemented
	
	public abstract void spawnEntity(EntityFunction<?> newEntityData, ChunkCoordinate chunkCoordinate);
	public abstract boolean chunkHasDefaultStructure(Random worldRandom, ChunkCoordinate chunkCoordinate);
	public abstract boolean generateModdedCaveGen(long worldSeed, int x, int z, ChunkBuffer chunkBuffer);
	public abstract void placeDungeon(Random random, int x, int y, int z);
	public abstract void placeFossil(Random random, ChunkCoordinate chunkCoord);
	public abstract void prepareDefaultStructures(long worldSeed, int x, int z, boolean dry);
	public abstract boolean isInsideWorldBorder(ChunkCoordinate chunkCoordinate);
	public abstract ChunkCoordinate getSpawnChunk();
		
	// Redundant?
	
	// Do we still need this?
	public abstract SpawnableObject getMojangStructurePart(String path);
		
	// TODO: Do we still need this with the new biome/chunkgen?
	public abstract BiomeGenerator getBiomeGenerator();
}
