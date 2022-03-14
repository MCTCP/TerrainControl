package com.pg85.otg.interfaces;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

import com.pg85.otg.constants.SettingsEnums.BiomeMode;
import com.pg85.otg.constants.SettingsEnums.ConfigMode;
import com.pg85.otg.constants.SettingsEnums.CustomStructureType;
import com.pg85.otg.constants.SettingsEnums.ImageMode;
import com.pg85.otg.constants.SettingsEnums.ImageOrientation;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.materials.LocalMaterialData;

/**
 * WorldConfig.ini classes
 * 
 * IWorldConfig defines anything that's used/exposed between projects.
 * WorldConfigBase implements anything needed for IWorldConfig. 
 * WorldConfig contains only fields/methods used for io/serialisation/instantiation.
 * 
 * WorldConfig should be used only in common-core and platform-specific layers, when reading/writing settings on app start.
 * IWorldConfig should be used wherever settings are used in code. 
 */
public interface IWorldConfig
{
	// Misc

	public ConfigMode getSettingsMode();
	String getShortPresetName();
	public int getMajorVersion();
	public String getAuthor();
	public String getDescription();

	// Visual settings
	
	public int getFogColor();
	
	// Biome resources

	public boolean isDisableOreGen();
	public boolean getBedrockDisabled();
	public boolean improvedBorderDecoration();

	// Blocks

	public boolean getRemoveSurfaceStone();
	public LocalMaterialData getWaterBlock();
	public LocalMaterialData getBedrockBlockReplaced(ReplaceBlockMatrix replacedBlocks, int y);
	public LocalMaterialData getDefaultBedrockBlock();
	public LocalMaterialData getCooledLavaBlock();
	public LocalMaterialData getIceBlock();
	public LocalMaterialData getCarverLavaBlock();

	// Bedrock

	public boolean getIsCeilingBedrock();
	public boolean getIsFlatBedrock();
	public int getCarverLavaBlockHeight();

	// Biome settings

	public ArrayList<String> getWorldBiomes();
	public List<String> getBlackListedBiomes();
	public int getBiomeRarityScale();
	boolean getOldGroupRarity();
	boolean getOldLandRarity();
	public int getGenerationDepth();
	public int getLandFuzzy();
	public int getLandRarity();
	public int getLandSize();
	public int getOceanBiomeSize();
	public String getDefaultOceanBiome();
	public String getDefaultWarmOceanBiome();
	public String getDefaultLukewarmOceanBiome();
	public String getDefaultColdOceanBiome();
	public String getDefaultFrozenOceanBiome();
	public BiomeMode getBiomeMode();
	double getFrozenOceanTemperature();
	public List<String> getIsleBiomes();
	public List<String> getBorderBiomes();
	public boolean getIsRandomRivers();
	public int getRiverRarity();
	public int getRiverSize();
	public boolean getRiversEnabled();
	public boolean getBiomeConfigsHaveReplacement();
	public boolean setBiomeConfigsHaveReplacement(boolean biomeConfigsHaveReplacement);

	// Terrain settings

	public double getFractureHorizontal();
	public double getFractureVertical();
	public int getWorldHeightCap();
	public int getWorldHeightScale();
	public void setMaxSmoothRadius(int smoothRadius);
	public int getMaxSmoothRadius();
	public boolean isBetterSnowFall();
	public int getWaterLevelMax();
	public int getWaterLevelMin();

	// FromImageMode

	public ImageOrientation getImageOrientation();
	public String getImageFile();
	public String getImageFillBiome();
	public ImageMode getImageMode();
	public int getImageZOffset();
	public int getImageXOffset();

	// Vanilla structures

	public boolean getWoodlandMansionsEnabled();
	public boolean getNetherFortressesEnabled();
	public boolean getBuriedTreasureEnabled();
	public boolean getOceanRuinsEnabled();
	public boolean getPillagerOutpostsEnabled();
	public boolean getBastionRemnantsEnabled();
	public boolean getNetherFossilsEnabled();
	public boolean getEndCitiesEnabled();
	public boolean getRuinedPortalsEnabled();
	public boolean getShipWrecksEnabled();
	public boolean getStrongholdsEnabled();
	public boolean getVillagesEnabled();
	public boolean getMineshaftsEnabled();
	public boolean getOceanMonumentsEnabled();
	public boolean getRareBuildingsEnabled();

	public int getVillageSpacing();
	public int getVillageSeparation();
	public int getDesertPyramidSpacing();
	public int getDesertPyramidSeparation();
	public int getIglooSpacing();
	public int getIglooSeparation();
	public int getJungleTempleSpacing();
	public int getJungleTempleSeparation();
	public int getSwampHutSpacing();
	public int getSwampHutSeparation();
	public int getPillagerOutpostSpacing();
	public int getPillagerOutpostSeparation();
	public int getStrongholdSpacing();
	public int getStrongholdSeparation();
	public int getStrongHoldDistance();
	public int getStrongHoldSpread();
	public int getStrongHoldCount();
	public int getOceanMonumentSpacing();
	public int getOceanMonumentSeparation();	
	public int getWoodlandMansionSpacing();
	public int getWoodlandMansionSeparation();
	public int getBuriedTreasureSpacing();
	public int getBuriedTreasureSeparation();
	public int getMineshaftSpacing();
	public int getMineshaftSeparation();
	public int getRuinedPortalSpacing();
	public int getRuinedPortalSeparation();
	public int getShipwreckSpacing();
	public int getShipwreckSeparation();
	public int getOceanRuinSpacing();
	public int getOceanRuinSeparation();	
	public int getEndCitySpacing();
	public int getEndCitySeparation();
	public int getBastionRemnantSpacing();
	public int getBastionRemnantSeparation();
	public int getNetherFortressSpacing();
	public int getNetherFortressSeparation();
	public int getNetherFossilSpacing();
	public int getNetherFossilSeparation();
	
	// OTG Custom structures

	public String getBO3AtSpawn();
	public CustomStructureType getCustomStructureType();
	public boolean getUseOldBO3StructureRarity();
	// TODO: Reimplement this, or forbid any spawning outside of decoration for 1.16.
	public boolean doPopulationBoundsCheck();
	public int getMaximumCustomStructureRadius();

	// Caves & Ravines

	public boolean getCavesEnabled();
	public int getCaveFrequency();
	public int getCaveRarity();
	public boolean isEvenCaveDistribution();
	public int getCaveMinAltitude();
	public int getCaveMaxAltitude();
	public int getCaveSystemFrequency();
	public int getIndividualCaveRarity();
	public int getCaveSystemPocketMinSize();
	public int getCaveSystemPocketChance();
	public int getCaveSystemPocketMaxSize();
	public boolean getRavinesEnabled();
	public int getRavineRarity();
	public int getRavineMinLength();
	public int getRavineMaxLength();
	public double getRavineDepth();
	public int getRavineMinAltitude();
	public int getRavineMaxAltitude();
	
	// Dimension settings

	public OptionalLong getFixedTime();
	public boolean getHasSkyLight();
	public boolean getHasCeiling();
	public boolean getUltraWarm();
	public boolean getNatural();
	public double getCoordinateScale();
	public boolean getCreateDragonFight();
	public boolean getPiglinSafe();
	public boolean getBedWorks();
	public boolean getRespawnAnchorWorks();
	public boolean getHasRaids();
	public int getLogicalHeight();
	public String getInfiniburn();
	public String getEffectsLocation();
	public float getAmbientLight();
	
	// Portal settings
	
	public List<LocalMaterialData> getPortalBlocks();
	public String getPortalColor();
	public String getPortalMob();
	public String getPortalIgnitionSource();

	// Spawn point settings
	
	public boolean getSpawnPointSet();
	public int getSpawnPointX();
	public int getSpawnPointY();
	public int getSpawnPointZ();
	public float getSpawnPointAngle();	
	
	// Game rules
	
	public boolean getOverrideGameRules();	
	public boolean getDoFireTick();
	public boolean getMobGriefing();
	public boolean getKeepInventory();
	public boolean getDoMobSpawning();
	public boolean getDoMobLoot();
	public boolean getDoTileDrops();
	public boolean getDoEntityDrops();
	public boolean getCommandBlockOutput();
	public boolean getNaturalRegeneration();
	public boolean getDoDaylightCycle();
	public boolean getLogAdminCommands();
	public boolean getShowDeathMessages();
	public int getRandomTickSpeed();
	public boolean getSendCommandFeedback();
	public boolean getSpectatorsGenerateChunks();
	public int getSpawnRadius();
	public boolean getDisableElytraMovementCheck();
	public int getMaxEntityCramming();
	public boolean getDoWeatherCycle();
	public boolean getDoLimitedCrafting();
	public int getMaxCommandChainLength();
	public boolean getAnnounceAdvancements();
	public boolean getDisableRaids();
	public boolean getDoInsomnia();
	public boolean getDrowningDamage();
	public boolean getFallDamage();
	public boolean getFireDamage();
	public boolean getDoPatrolSpawning();
	public boolean getDoTraderSpawning();
	public boolean getForgiveDeadPlayers();
	public boolean getUniversalAnger();
	boolean getForceLandAtSpawn();

	boolean getLargeOreVeins();
}
