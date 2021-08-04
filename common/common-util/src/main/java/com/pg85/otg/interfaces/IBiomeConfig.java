package com.pg85.otg.interfaces;

import java.util.List;

import com.pg85.otg.constants.SettingsEnums.GrassColorModifier;
import com.pg85.otg.constants.SettingsEnums.MineshaftType;
import com.pg85.otg.constants.SettingsEnums.OceanRuinsType;
import com.pg85.otg.constants.SettingsEnums.RareBuildingType;
import com.pg85.otg.constants.SettingsEnums.RuinedPortalType;
import com.pg85.otg.constants.SettingsEnums.VillageType;
import com.pg85.otg.util.biome.ColorSet;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.GeneratingChunk;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.minecraft.SaplingType;

/**
 * BiomeConfig (*.bc) classes
 * 
 * IBiomeConfig defines anything that's used/exposed between projects.
 * BiomeConfigBase implements anything needed for IBiomeConfig. 
 * BiomeConfig contains only fields/methods used for io/serialisation/instantiation.
 * 
 * BiomeConfig should be used only in common-core and platform-specific layers, when reading/writing settings on app start.
 * IBiomeConfig should be used wherever settings are used in code. 
 */
public interface IBiomeConfig
{
	// Misc

	String getName();
	IBiomeResourceLocation getRegistryKey();
	void setOTGBiomeId(int id);
	int getOTGBiomeId();
	void setRegistryKey(IBiomeResourceLocation registryKey);

	// WorldConfig getters
	// TODO: Ideally, don't contain worldConfig within biomeconfig,  
	// use a parent object that holds both, like a worldgenregion.

	boolean biomeConfigsHaveReplacement();
	double getFractureHorizontal();
	double getFractureVertical();
	boolean isFlatBedrock();
	boolean isCeilingBedrock();
	boolean isBedrockDisabled();
	boolean isRemoveSurfaceStone();

	// Inheritance

	List<String> getBiomeDictTags();
	String getBiomeCategory();
	boolean getTemplateForBiome();

	// Placement

	int getBiomeSize();
	int getBiomeRarity();
	int getBiomeColor();
	boolean isIsleBiome();
	List<String> getIsleInBiomes();
	int getBiomeSizeWhenIsle();
	int getBiomeRarityWhenIsle();
	boolean isBorderBiome();
	List<String> getBorderInBiomes();
	List<String> getOnlyBorderNearBiomes();
	List<String> getNotBorderNearBiomes();
	int getBiomeSizeWhenBorder();
	
	// Height / volatility
	
	float getBiomeHeight();
	float getBiomeVolatility();
	int getSmoothRadius();	
	int getCHCSmoothRadius();
	double getMaxAverageDepth();
	double getMaxAverageHeight();
	double getVolatility1();
	double getVolatility2();
	double getVolatilityWeight1();
	double getVolatilityWeight2();
	boolean disableBiomeHeight();
	double getCHCData(int y);
	
	// Rivers
	
	String getRiverBiome();
	
	// Blocks

	// Any blocks spawned/checked during base terrain gen that use the biomeconfig materials
	// call getXXXBlockReplaced to get the replaced blocks.
	// Any blocks spawned during decoration will have their materials parsed before spawning
	// them via world.setBlock(), so they use the default biomeconfig materials.	
	
	// Note: getSurfaceBlockReplaced / getGroundBlockReplaced don't take into
	// account SAGC, so they should only be used by surfacegenerators.
	
	LocalMaterialData getSurfaceBlockAtHeight(ISurfaceGeneratorNoiseProvider noiseProvider, int x, int y, int z);
	LocalMaterialData getGroundBlockAtHeight(ISurfaceGeneratorNoiseProvider noiseProvider, int x, int y, int z);
	LocalMaterialData getSurfaceBlockReplaced(int y);
	LocalMaterialData getGroundBlockReplaced(int y);
	LocalMaterialData getStoneBlockReplaced(int y);
	LocalMaterialData getBedrockBlockReplaced(int y);
	LocalMaterialData getSandStoneBlockReplaced(int y);
	LocalMaterialData getDefaultGroundBlock();
	void doSurfaceAndGroundControl(long worldSeed, GeneratingChunk generatingChunk, ChunkBuffer chunkBuffer, int x, int z, IBiome biome);
	boolean hasReplaceBlocksSettings();
	ReplaceBlockMatrix getReplaceBlocks();
	
	// Water / lava / freezing
	
	int getWaterLevelMax();
	int getWaterLevelMin();
	LocalMaterialData getWaterBlockReplaced(int y);
	LocalMaterialData getUnderWaterSurfaceBlockReplaced(int y);	
	LocalMaterialData getIceBlockReplaced(int y);
	LocalMaterialData getPackedIceBlockReplaced(int y);
	LocalMaterialData getSnowBlockReplaced(int y);
	LocalMaterialData getCooledLavaBlockReplaced(int y);
	
	// Visuals / weather
	
	float getBiomeTemperature();
	boolean useFrozenOceanTemperature();
	float getBiomeWetness();
	int getFogColor();
	float getFogDensity();
	int getWaterFogColor();
	int getFoliageColor();
	ColorSet getFoliageColorControl();
	int getGrassColor();
	ColorSet getGrassColorControl();
	GrassColorModifier getGrassColorModifier();
	int getSkyColor();
	int getWaterColor();
	ColorSet getWaterColorControl();
	String getParticleType();
	float getParticleProbability();	
	int getSnowHeight(float tempAtBlockToFreeze);
	String getMusic();
	int getMusicMinDelay();
	int getMusicMaxDelay();
	boolean isReplaceCurrentMusic();
	String getAmbientSound();
	String getMoodSound();
	int getMoodSoundDelay();
	int getMoodSearchRange();
	double getMoodOffset();
	String getAdditionsSound();
	double getAdditionsTickChance();
	
	// OTG Custom structures (BO's)
	
	List<List<String>> getCustomStructureNames();
	List<ICustomStructureGen> getCustomStructures();
	ICustomStructureGen getStructureGen();
	void setStructureGen(ICustomStructureGen customStructureGen);
	
	// Structures
	
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

	// Mob spawning
	
	List<WeightedMobSpawnGroup> getMonsters();
	List<WeightedMobSpawnGroup> getCreatures();
	List<WeightedMobSpawnGroup> getWaterCreatures();
	List<WeightedMobSpawnGroup> getAmbientCreatures();	
	List<WeightedMobSpawnGroup> getWaterAmbientCreatures();
	List<WeightedMobSpawnGroup> getMiscCreatures();
	
	// Saplings
	
	ISaplingSpawner getSaplingGen(SaplingType type);
	ISaplingSpawner getCustomSaplingGen(LocalMaterialData materialData, boolean wideTrunk);
	
	// Misc
	
	public IBiomeConfig createTemplateBiome();
}
