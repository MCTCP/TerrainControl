package com.pg85.otg.util.interfaces;

import java.util.Random;

import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.biome.ReplacedBlocksMatrix;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.minecraft.TreeType;

public interface IWorldGenRegion
{
	public IWorldConfig getWorldConfig();
	public String getWorldName();
	public long getSeed();	
	public Random getWorldRandom();
	public void cacheBiomesForPopulation(ChunkCoordinate chunkCoord);
	public void invalidatePopulationBiomeCache();
	public IBiome getBiome(int x, int z);
	public IBiome getBiomeForPopulation(int x, int z, ChunkCoordinate chunkBeingPopulated);
	public IBiomeConfig getBiomeConfig(int x, int z);
	public IBiomeConfig getBiomeConfigForPopulation(int worldX, int worldZ, ChunkCoordinate chunkBeingPopulated);
	public double getBiomeBlocksNoiseValue(int xInWorld, int zInWorld);
	public boolean placeTree(TreeType type, Random rand, int x, int y, int z);
	public LocalMaterialData getMaterial(int x, int y, int z, ChunkCoordinate chunkBeingPopulated);
	public int getBlockAboveLiquidHeight(int x, int z, ChunkCoordinate chunkBeingPopulated);
	public int getBlockAboveSolidHeight(int x, int z, ChunkCoordinate chunkBeingPopulated);
	public int getHighestBlockAboveYAt(int x, int z, ChunkCoordinate chunkBeingPopulated);
	public int getHighestBlockYAt(int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, boolean ignoreLeaves, ChunkCoordinate chunkBeingPopulated);
	public int getHeightMapHeight(int x, int z, ChunkCoordinate chunkBeingPopulated);
	public int getLightLevel(int x, int y, int z, ChunkCoordinate chunkBeingPopulated);
	public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, ChunkCoordinate chunkBeingPopulated, boolean replaceBlocks);
	public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, ChunkCoordinate chunkBeingPopulated, ReplacedBlocksMatrix replaceBlocksMatrix, boolean replaceBlocks);	
	public void spawnEntity(IEntityFunction<?> newEntityData, ChunkCoordinate chunkCoordinate);
	public boolean chunkHasDefaultStructure(Random worldRandom, ChunkCoordinate chunkCoordinate);
	public void placeDungeon(Random random, int x, int y, int z);
	public void placeFossil(Random random, ChunkCoordinate chunkCoord);
	public void prepareDefaultStructures(long worldSeed, int x, int z, boolean dry);
	public boolean isInsideWorldBorder(ChunkCoordinate chunkCoordinate);
	public ChunkCoordinate getSpawnChunk();
}
