package com.pg85.otg.util.interfaces;

import java.util.List;

import com.pg85.otg.constants.SettingsEnums.MineshaftType;
import com.pg85.otg.constants.SettingsEnums.RareBuildingType;
import com.pg85.otg.constants.SettingsEnums.VillageType;
import com.pg85.otg.util.biome.BiomeResourceLocation;
import com.pg85.otg.util.biome.ReplacedBlocksMatrix;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.GeneratingChunk;
import com.pg85.otg.util.materials.LocalMaterialData;

public interface IBiomeConfig
{
	LocalMaterialData getSurfaceBlockAtHeight(IWorldGenRegion worldGenRegion, int x, int y, int z);
	LocalMaterialData getGroundBlockAtHeight(IWorldGenRegion worldGenRegion, int x, int y, int z);

	LocalMaterialData getSurfaceBlockReplaced(int y);
	LocalMaterialData getGroundBlockReplaced(int y);
	LocalMaterialData getStoneBlockReplaced(int y);
	LocalMaterialData getDefaultGroundBlock();
	LocalMaterialData getBedrockBlockReplaced(int y);
	LocalMaterialData getWaterBlockReplaced(int y);
	LocalMaterialData getSandStoneBlockReplaced(int y);
	LocalMaterialData getIceBlockReplaced(int y);
	LocalMaterialData getCooledLavaBlockReplaced(int y);
	
	ReplacedBlocksMatrix getReplaceBlocks();

	List<List<String>> getCustomStructureNames();
	List<ICustomStructureGen> getCustomStructures();
	ICustomStructureGen getStructureGen();
	void setStructureGen(ICustomStructureGen customStructureGen);
	
	String getName();
	boolean biomeConfigsHaveReplacement();
		
	float getBiomeTemperature();
	float getBiomeHeight();
	float getBiomeVolatility();
	double getVolatility1();
	double getVolatility2();
	double getFractureHorizontal();
	double getFractureVertical();
	double getVolatilityWeight1();
	double getVolatilityWeight2();
	double getMaxAverageDepth();
	double getMaxAverageHeight();
	double getCHCData(int y);
	int getSmoothRadius();
	int getWaterLevelMax();
	int getWaterLevelMin();
	boolean isFlatBedrock();
	boolean isCeilingBedrock();
	boolean isBedrockDisabled();
	boolean isRemoveSurfaceStone();
	
	int getSnowHeight(float tempAtBlockToFreeze);
	void doSurfaceAndGroundControl(long worldSeed, GeneratingChunk generatingChunk, ChunkBuffer chunkBuffer, IBiomeConfig biomeConfig, int x, int z);
	boolean getWoodlandMansionsEnabled();
	VillageType getVillageType();
	MineshaftType getMineShaftType();
	RareBuildingType getRareBuildingType();
	boolean getNetherFortressesEnabled();
	int getFogColor();
	float getBiomeWetness();
	int getFoliageColor();
	int getGrassColor();
	boolean getGrassColorIsMultiplier();
	int getSkyColor();
	int getWaterColor();
	List<WeightedMobSpawnGroup> getWaterCreatures();
	List<WeightedMobSpawnGroup> getAmbientCreatures();
	List<WeightedMobSpawnGroup> getCreatures();
	List<WeightedMobSpawnGroup> getMonsters();
	BiomeResourceLocation getRegistryKey();
	int getBiomeSize();
	int getBiomeRarity();
	boolean isIsleBiome();
	int getBiomeColor();
	String getBiomeExtends();
	int getCHCSmoothRadius();
	String getReplaceToBiomeName();
	void setReplaceToBiomeName(String replaceToBiomeName);
	List<String> getIsleInBiomes();
}
