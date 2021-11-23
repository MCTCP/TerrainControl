package com.pg85.otg.interfaces;

import java.util.Random;

import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.nbt.NamedBinaryTag;
import com.pg85.otg.util.gen.DecorationArea;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.minecraft.TreeType;

public interface IWorldGenRegion extends ISurfaceGeneratorNoiseProvider
{
	ILogger getLogger();
	IPluginConfig getPluginConfig();
	IWorldConfig getWorldConfig();
	String getPresetFolderName();
	long getSeed();
	Random getWorldRandom();
	ChunkCoordinate getSpawnChunk();
	
	// Any getBiome/getBiomeConfig requests done as a part
	// of chunk decoration should call getBiomeForDecoration(),
	// unless they are intentionally querying outside of
	// the area being decorated, in which case use getBiome().

	ICachedBiomeProvider getCachedBiomeProvider();
	IBiome getBiomeForDecoration(int x, int z);
	IBiomeConfig getBiomeConfigForDecoration(int worldX, int worldZ);
	boolean placeTree(TreeType type, Random rand, int x, int y, int z);
	LocalMaterialData getMaterial(int x, int y, int z);
	LocalMaterialData getMaterialDirect(int x, int y, int z);
	int getBlockAboveLiquidHeight(int x, int z);
	int getBlockAboveSolidHeight(int x, int z);
	int getHighestBlockAboveYAt(int x, int z);
	int getHighestBlockYAt(int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, boolean ignoreLeaves);
	int getHeightMapHeight(int x, int z);
	int getLightLevel(int x, int y, int z);
	void setBlockDirect(int x, int y, int z, LocalMaterialData material);
	void setBlock(int x, int y, int z, LocalMaterialData material);
	void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag);
	void setBlock(int x, int y, int z, LocalMaterialData material, ReplaceBlockMatrix replaceBlocksMatrix);
	void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, ReplaceBlockMatrix replaceBlocksMatrix);
	void spawnEntity(IEntityFunction newEntityData);
	void placeDungeon(Random random, int x, int y, int z);
	void placeFossil(Random random, int x, int y, int z);
	boolean isInsideWorldBorder(ChunkCoordinate chunkCoordinate);
	DecorationArea getDecorationArea();

	// Shadowgen

	LocalMaterialData getMaterialWithoutLoading(int x, int y, int z);
	int getHighestBlockYAtWithoutLoading(int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, boolean ignoreLeaves);
	boolean chunkHasDefaultStructure(Random worldRandom, ChunkCoordinate chunkCoordinate);	
}
