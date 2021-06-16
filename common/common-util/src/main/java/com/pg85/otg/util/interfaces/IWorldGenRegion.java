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
	public String getPresetFolderName();
	public long getSeed();	
	public Random getWorldRandom();
	public void cacheBiomesForDecoration(ChunkCoordinate chunkCoord);
	public void invalidateDecorationBiomeCache();
	public IBiome getBiome(int x, int z);
	public IBiome getBiomeForDecoration(int x, int z, ChunkCoordinate chunkBeingDecorated);
	public IBiomeConfig getBiomeConfig(int x, int z);
	public IBiomeConfig getBiomeConfigForPopulation(int worldX, int worldZ, ChunkCoordinate chunkBeingDecorated);
	public double getBiomeBlocksNoiseValue(int xInWorld, int zInWorld);
	public boolean placeTree(TreeType type, Random rand, int x, int y, int z);
	public LocalMaterialData getMaterial(int x, int y, int z, ChunkCoordinate chunkBeingDecorated);
	public int getBlockAboveLiquidHeight(int x, int z, ChunkCoordinate chunkBeingDecorated);
	public int getBlockAboveSolidHeight(int x, int z, ChunkCoordinate chunkBeingDecorated);
	public int getHighestBlockAboveYAt(int x, int z, ChunkCoordinate chunkBeingDecorated);
	public int getHighestBlockYAt(int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, boolean ignoreLeaves, ChunkCoordinate chunkBeingDecorated);
	public int getHeightMapHeight(int x, int z, ChunkCoordinate chunkBeingDecorated);
	public int getLightLevel(int x, int y, int z, ChunkCoordinate chunkBeingDecorated);
	public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, ChunkCoordinate chunkBeingDecorated, boolean replaceBlocks);
	public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, ChunkCoordinate chunkBeingDecorated, boolean replaceBlocks, boolean useResourceBounds);
	public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, ChunkCoordinate chunkBeingDecorated, ReplacedBlocksMatrix replaceBlocksMatrix, boolean replaceBlocks, boolean useResourceBounds);	
	public void spawnEntity(IEntityFunction<?> newEntityData, ChunkCoordinate chunkCoordinate);
	public boolean chunkHasDefaultStructure(Random worldRandom, ChunkCoordinate chunkCoordinate);
	public void placeDungeon(Random random, int x, int y, int z);
	public void placeFossil(Random random, ChunkCoordinate chunkCoord);
	public void placeFromRegistry(Random random, ChunkCoordinate chunkCoord, String id);
	public boolean isInsideWorldBorder(ChunkCoordinate chunkCoordinate);
}
