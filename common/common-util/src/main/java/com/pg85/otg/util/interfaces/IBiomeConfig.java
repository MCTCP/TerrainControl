package com.pg85.otg.util.interfaces;

import java.util.List;

import com.pg85.otg.constants.SettingsEnums.MineshaftType;
import com.pg85.otg.constants.SettingsEnums.OceanRuinsType;
import com.pg85.otg.constants.SettingsEnums.RareBuildingType;
import com.pg85.otg.constants.SettingsEnums.RuinedPortalType;
import com.pg85.otg.constants.SettingsEnums.VillageType;
import com.pg85.otg.util.biome.BiomeResourceLocation;
import com.pg85.otg.util.biome.ReplacedBlocksMatrix;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.GeneratingChunk;
import com.pg85.otg.util.materials.LocalMaterialData;

public interface IBiomeConfig
{
	String getName();
	BiomeResourceLocation getRegistryKey();
	
	float getBiomeTemperature();
	float getBiomeWetness();
	
	// WorldConfig getters (TODO: don't access worldconfig via biomeconfig)
	
	boolean biomeConfigsHaveReplacement();
	
	// Blocks

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

	boolean isRemoveSurfaceStone();
	
	ReplacedBlocksMatrix getReplaceBlocks();
	void doSurfaceAndGroundControl(long worldSeed, GeneratingChunk generatingChunk, ChunkBuffer chunkBuffer, IBiomeConfig biomeConfig, int x, int z);
	
	int getSnowHeight(float tempAtBlockToFreeze);
	
	// Bedrock
	
	boolean isFlatBedrock();
	boolean isCeilingBedrock();
	boolean isBedrockDisabled();
	
	// Colors

	int getFogColor();
	int getFoliageColor();
	int getGrassColor();
	boolean getGrassColorIsMultiplier();
	int getSkyColor();
	int getWaterColor();
	
	// Mob spawning
	
	List<WeightedMobSpawnGroup> getWaterCreatures();
	List<WeightedMobSpawnGroup> getAmbientCreatures();
	List<WeightedMobSpawnGroup> getCreatures();
	List<WeightedMobSpawnGroup> getMonsters();
	
	// Biomegen / terrain settings

	String getReplaceToBiomeName();
	void setReplaceToBiomeName(String replaceToBiomeName);
	List<String> getIsleInBiomes();
	List<String> getBorderInBiomes();
	List<String> getNotBorderNearBiomes();
	int getBiomeSizeWhenIsle();
	int getBiomeSizeWhenBorder();
	int getBiomeRarityWhenIsle();
	String getRiverBiome();
	int getBiomeSize();
	int getBiomeRarity();
	boolean isIsleBiome();
	boolean isBorderBiome();
	int getBiomeColor();
	String getBiomeExtends();
	int getCHCSmoothRadius();
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
	boolean disableNotchHeightControl();
	int getWaterLevelMax();
	int getWaterLevelMin();
	
	// Vanilla structures
	
	VillageType getVillageType();
	int getVillageSize();
	
	MineshaftType getMineShaftType();
	float getMineShaftProbability();
	
	OceanRuinsType getOceanRuinsType();
	float getOceanRuinsLargeProbability();
	float getOceanRuinsClusterProbability();
	
	boolean getBuriedTreasureEnabled();
	float getBuriedTreasureProbability();

	boolean getPillagerOutpostEnabled();
	int getPillagerOutPostSize();

	boolean getBastionRemnantEnabled();
	int getBastionRemnantSize();
	
	RareBuildingType getRareBuildingType();
	RuinedPortalType getRuinedPortalType();
	
	boolean getWoodlandMansionsEnabled();
	boolean getNetherFortressesEnabled();	
	boolean getShipWreckEnabled();
	boolean getShipWreckBeachedEnabled();
	boolean getNetherFossilEnabled();
	boolean getEndCityEnabled();
	boolean getStrongholdsEnabled();
	boolean getOceanMonumentsEnabled();
	
	// OTG Custom structures (BO's)
	
	List<List<String>> getCustomStructureNames();
	List<ICustomStructureGen> getCustomStructures();
	ICustomStructureGen getStructureGen();
	void setStructureGen(ICustomStructureGen customStructureGen);
}
