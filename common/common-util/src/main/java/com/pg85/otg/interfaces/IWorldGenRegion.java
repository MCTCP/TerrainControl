package com.pg85.otg.interfaces;

import java.util.Random;

import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.gen.DecorationArea;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.minecraft.TreeType;

public interface IWorldGenRegion
{
	public IPluginConfig getPluginConfig();
	public IWorldConfig getWorldConfig();
	public String getPresetFolderName();
	public long getSeed();
	public Random getWorldRandom();
	public ChunkCoordinate getSpawnChunk();
	
	// Any getBiome/getBiomeConfig requests done as a part
	// of chunk decoration should call getBiomeForDecoration(),
	// unless they are intentionally querying outside of
	// the area being decorated, in which case use getBiome().

	public ICachedBiomeProvider getCachedBiomeProvider();
	public IBiome getBiomeForDecoration(int x, int z);
	public IBiomeConfig getBiomeConfigForDecoration(int worldX, int worldZ);
	public double getBiomeBlocksNoiseValue(int xInWorld, int zInWorld);
	public boolean placeTree(TreeType type, Random rand, int x, int y, int z);
	public LocalMaterialData getMaterial(int x, int y, int z);
	public LocalMaterialData getMaterialDirect(int x, int y, int z);
	public int getBlockAboveLiquidHeight(int x, int z);
	public int getBlockAboveSolidHeight(int x, int z);
	public int getHighestBlockAboveYAt(int x, int z);
	public int getHighestBlockYAt(int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, boolean ignoreLeaves);
	public int getHeightMapHeight(int x, int z);
	public int getLightLevel(int x, int y, int z);
	public void setBlockDirect(int x, int y, int z, LocalMaterialData material);
	public void setBlock(int x, int y, int z, LocalMaterialData material);
	public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag);
	public void setBlock(int x, int y, int z, LocalMaterialData material, ReplaceBlockMatrix replaceBlocksMatrix);
	public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, ReplaceBlockMatrix replaceBlocksMatrix);
	public void spawnEntity(IEntityFunction<?> newEntityData);
	public void placeDungeon(Random random, int x, int y, int z, int range, int count);
	public void placeFossil(Random random, int x, int y, int z, int chance);
	public void placeFromRegistry(Random random, ChunkCoordinate chunkCoord, String id);
	public boolean isInsideWorldBorder(ChunkCoordinate chunkCoordinate);
	public DecorationArea getDecorationArea();

	// Shadowgen

	public LocalMaterialData getMaterialWithoutLoading(int x, int y, int z);
	public int getHighestBlockYAtWithoutLoading(int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, boolean ignoreLeaves);
	public boolean chunkHasDefaultStructure(Random worldRandom, ChunkCoordinate chunkCoordinate);	
}
