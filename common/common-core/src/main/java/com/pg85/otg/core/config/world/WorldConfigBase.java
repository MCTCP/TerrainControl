package com.pg85.otg.core.config.world;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

import com.pg85.otg.config.ConfigFile;
import com.pg85.otg.constants.SettingsEnums.BiomeMode;
import com.pg85.otg.constants.SettingsEnums.ConfigMode;
import com.pg85.otg.constants.SettingsEnums.CustomStructureType;
import com.pg85.otg.constants.SettingsEnums.ImageMode;
import com.pg85.otg.constants.SettingsEnums.ImageOrientation;
import com.pg85.otg.interfaces.IWorldConfig;
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
abstract class WorldConfigBase extends ConfigFile implements IWorldConfig
{
	// Misc
	
	protected ConfigMode settingsMode;

	protected int majorVersion;
	protected int minorVersion;
	protected String author;
	protected String description;
	protected String shortPresetName;

	// Visual settings
	
	protected int worldFogColor;
	
	// Biome resources
	
	protected boolean disableOreGen;
	protected boolean disableBedrock;

	// Blocks
	
	protected boolean removeSurfaceStone;
	protected LocalMaterialData waterBlock;
	protected LocalMaterialData bedrockBlock;
	protected LocalMaterialData cooledLavaBlock;
	protected LocalMaterialData iceBlock;
	protected LocalMaterialData carverLavaBlock;
	
	// Bedrock
	
	protected boolean ceilingBedrock;
	protected boolean flatBedrock;
	protected int carverLavaBlockHeight;
	
	// Biome settings
	
	protected ArrayList<String> worldBiomes = new ArrayList<String>();
	protected List<String> blackListedBiomes = new ArrayList<String>();
	protected int biomeRarityScale;
	protected boolean oldGroupRarity;
	protected boolean oldLandRarity;
	protected int generationDepth;
	protected int landFuzzy;
	protected int landRarity;
	protected int landSize;
	protected boolean forceLandAtSpawn;
	protected int oceanBiomeSize;
	protected String defaultOceanBiome;
	protected String defaultWarmOceanBiome;
	protected String defaultLukewarmOceanBiome;
	protected String defaultColdOceanBiome;
	protected String defaultFrozenOceanBiome;
	protected BiomeMode biomeMode;
	protected double frozenOceanTemperature;
	protected List<String> isleBiomes = new ArrayList<String>();
	protected List<String> borderBiomes = new ArrayList<String>();	
	protected boolean randomRivers;
	protected int riverRarity;
	protected int riverSize;
	protected boolean riversEnabled;
	protected boolean biomeConfigsHaveReplacement = false;
	protected boolean improvedBorderDecoration = false;

	// Terrain settings
	
	protected double fractureHorizontal;
	protected double fractureVertical;
	protected int worldHeightCap;
	protected int worldHeightScale;
	protected int maxSmoothRadius = 2;
	protected boolean betterSnowFall;	
	protected int waterLevelMin;
	protected int waterLevelMax;

	// FromImageMode
	
	protected ImageOrientation imageOrientation;
	protected String imageFile;
	protected String imageFillBiome;	
	protected ImageMode imageMode;
	protected int imageXOffset;
	protected int imageZOffset;
	
	// Vanilla structures

	protected int villageSpacing;
	protected int villageSeparation;
	protected int desertPyramidSpacing;
	protected int desertPyramidSeparation;
	protected int iglooSpacing;
	protected int iglooSeparation;
	protected int jungleTempleSpacing;
	protected int jungleTempleSeparation;
	protected int swampHutSpacing;
	protected int swampHutSeparation;
	protected int pillagerOutpostSpacing;
	protected int pillagerOutpostSeparation;		
	protected int strongholdSpacing;
	protected int strongholdSeparation;
	protected int strongholdDistance;
	protected int strongholdSpread;
	protected int strongholdCount;
	protected int oceanMonumentSpacing;
	protected int oceanMonumentSeparation;		
	protected int endCitySpacing;
	protected int endCitySeparation;
	protected int woodlandMansionSpacing;
	protected int woodlandMansionSeparation;		
	protected int buriedTreasureSpacing;
	protected int buriedTreasureSeparation;		
	protected int mineshaftSpacing;
	protected int mineshaftSeparation;
	protected int ruinedPortalSpacing;
	protected int ruinedPortalSeparation;		
	protected int shipwreckSpacing;
	protected int shipwreckSeparation;
	protected int oceanRuinSpacing;
	protected int oceanRuinSeparation;
	protected int bastionRemnantSpacing;
	protected int bastionRemnantSeparation;
	protected int netherFortressSpacing;
	protected int netherFortressSeparation;
	protected int netherFossilSpacing;
	protected int netherFossilSeparation;
	
	protected boolean woodlandMansionsEnabled;
	protected boolean netherFortressesEnabled;
	protected boolean buriedTreasureEnabled;
	protected boolean oceanRuinsEnabled;
	protected boolean pillagerOutpostsEnabled;
	protected boolean bastionRemnantsEnabled;
	protected boolean netherFossilsEnabled;
	protected boolean endCitiesEnabled;
	protected boolean ruinedPortalsEnabled;
	protected boolean shipWrecksEnabled;
	protected boolean strongholdsEnabled;
	protected boolean villagesEnabled;	
	protected boolean mineshaftsEnabled;
	protected boolean oceanMonumentsEnabled;
	protected boolean rareBuildingsEnabled;

	// OTG Custom structures
	
	protected String bo3AtSpawn;
	protected CustomStructureType customStructureType;
	protected boolean useOldBO3StructureRarity;
	protected boolean decorationBoundsCheck;
	protected int maximumCustomStructureRadius;
	
	// Caves & Ravines	
	
	protected boolean cavesEnabled;
	protected int caveFrequency;
	protected int caveRarity;	
	protected boolean evenCaveDistribution;	
	protected int caveMinAltitude;	
	protected int caveMaxAltitude;	
	protected int caveSystemFrequency;
	protected int individualCaveRarity;
	protected int caveSystemPocketChance;
	protected int caveSystemPocketMinSize;
	protected int caveSystemPocketMaxSize;	

	protected boolean ravinesEnabled;
	protected int ravineRarity;
	protected int ravineMinLength;
	protected int ravineMaxLength;
	protected double ravineDepth;	
	protected int ravineMinAltitude;
	protected int ravineMaxAltitude;

	// Dimension settings

	protected OptionalLong fixedTime;
	protected boolean hasSkyLight;
	protected boolean hasCeiling;
	protected boolean ultraWarm;
	protected boolean natural;
	protected double coordinateScale;
	protected boolean createDragonFight;
	protected boolean piglinSafe;
	protected boolean bedWorks;
	protected boolean respawnAnchorWorks;
	protected boolean hasRaids;
	protected int logicalHeight;
	protected String infiniburn;
	protected String effectsLocation;
	protected float ambientLight;

	// Game rules
	
	protected boolean overrideGameRules;
	protected boolean doFireTick;
	protected boolean mobGriefing;
	protected boolean keepInventory;
	protected boolean doMobSpawning;
	protected boolean doMobLoot;
	protected boolean doTileDrops;
	protected boolean doEntityDrops;
	protected boolean commandBlockOutput;
	protected boolean naturalRegeneration;
	protected boolean doDaylightCycle;
	protected boolean logAdminCommands;
	protected boolean showDeathMessages;
	protected int randomTickSpeed;
	protected boolean sendCommandFeedback;
	protected boolean spectatorsGenerateChunks;
	protected int spawnRadius;
	protected boolean disableElytraMovementCheck;
	protected int maxEntityCramming;
	protected boolean doWeatherCycle;
	protected boolean doLimitedCrafting;
	protected int maxCommandChainLength;
	protected boolean announceAdvancements;
	protected boolean disableRaids;
	protected boolean doInsomnia;
	protected boolean drowningDamage;
	protected boolean fallDamage;
	protected boolean fireDamage;
	protected boolean doPatrolSpawning;
	protected boolean doTraderSpawning;
	protected boolean forgiveDeadPlayers;
	protected boolean universalAnger;
	
	// Portals

	protected ArrayList<LocalMaterialData> portalBlocks;
	protected String portalColor;
	protected String portalMob;
	protected String portalIgnitionSource;

	// Spawn point
	
	protected boolean fixedSpawnPoint;
	protected int spawnPointX;
	protected int spawnPointY;
	protected int spawnPointZ;
	protected float spawnPointAngle;

	protected boolean largeOreVeins;

	protected WorldConfigBase(String configName)
	{
		super(configName);
	}

	@Override
	public int getFogColor()
	{
		return this.worldFogColor;
	}
	
	@Override
	public LocalMaterialData getDefaultBedrockBlock()
	{
		return this.bedrockBlock;
	}

	@Override
	public LocalMaterialData getBedrockBlockReplaced(ReplaceBlockMatrix replaceBlocks, int y)
	{		
		if(replaceBlocks.replacesBedrock)
		{
			return this.bedrockBlock.parseWithBiomeAndHeight(this.biomeConfigsHaveReplacement, replaceBlocks, y);
		}
		return this.bedrockBlock;
	}
	
	@Override
	public double getFractureHorizontal()
	{
		return this.fractureHorizontal < 0.0D ? 1.0D / (Math.abs(this.fractureHorizontal) + 1.0D) : this.fractureHorizontal + 1.0D;
	}

	@Override
	public boolean improvedBorderDecoration() {
		return improvedBorderDecoration;
	}

	@Override
	public double getFractureVertical()
	{
		return this.fractureVertical < 0.0D ? 1.0D / (Math.abs(this.fractureVertical) + 1.0D) : this.fractureVertical + 1.0D;
	}

	@Override
	public boolean doPopulationBoundsCheck()
	{
		return this.decorationBoundsCheck;
	}
	
	@Override
	public boolean getBedrockDisabled()
	{
		return this.disableBedrock;
	}	
	
	@Override
	public int getWorldHeightCap()
	{
		return this.worldHeightCap;
	}
	
	@Override
	public boolean getRavinesEnabled()
	{
		return this.ravinesEnabled;
	}
	
	@Override
	public int getRavineRarity()
	{
		return this.ravineRarity;
	}
	
	@Override
	public int getRavineMinLength()
	{
		return this.ravineMinLength;
	}
	
	@Override
	public int getRavineMaxLength()
	{
		return this.ravineMaxLength;
	}
	
	@Override
	public double getRavineDepth()
	{
		return this.ravineDepth;
	}

	@Override
	public int getRavineMinAltitude()
	{
		return this.ravineMinAltitude;
	}
	
	@Override
	public int getRavineMaxAltitude()
	{
		return this.ravineMaxAltitude;
	}
	
	@Override
	public boolean getCavesEnabled()
	{
		return this.cavesEnabled;
	}
	
	@Override
	public int getCaveFrequency()
	{
		return this.caveFrequency;
	}
	
	@Override
	public int getCaveRarity()
	{
		return this.caveRarity;
	}
	
	@Override
	public boolean isEvenCaveDistribution()
	{
		return this.evenCaveDistribution;
	}
	
	@Override
	public int getCaveMinAltitude()
	{
		return this.caveMinAltitude;
	}
	
	@Override
	public int getCaveMaxAltitude()
	{
		return this.caveMaxAltitude;
	}
	
	@Override
	public int getCaveSystemFrequency()
	{
		return this.caveSystemFrequency;
	}
	
	@Override
	public int getIndividualCaveRarity()
	{
		return this.individualCaveRarity;
	}
	
	@Override
	public int getCaveSystemPocketMinSize()
	{
		return this.caveSystemPocketMinSize;
	}
	
	@Override
	public int getCaveSystemPocketChance()
	{
		return this.caveSystemPocketChance;
	}

	@Override
	public int getCaveSystemPocketMaxSize()
	{
		return this.caveSystemPocketMaxSize;
	}

	@Override
	public boolean isBetterSnowFall()
	{
		return this.betterSnowFall;
	}
	
	@Override
	public String getBO3AtSpawn()
	{
		return this.bo3AtSpawn;
	}
	
	@Override
	public CustomStructureType getCustomStructureType()
	{
		return this.customStructureType;
	}

	@Override
	public boolean getUseOldBO3StructureRarity()
	{
		return this.useOldBO3StructureRarity;
	}

	@Override
	public boolean isDisableOreGen()
	{
		return this.disableOreGen;
	}
	
	@Override
	public int getMaximumCustomStructureRadius()
	{
		return this.maximumCustomStructureRadius;
	}
	
	@Override
	public boolean setBiomeConfigsHaveReplacement(boolean biomeConfigsHaveReplacement)
	{
		return this.biomeConfigsHaveReplacement = biomeConfigsHaveReplacement;
	}
	
	@Override
	public boolean getBiomeConfigsHaveReplacement()
	{
		return this.biomeConfigsHaveReplacement;
	}

	@Override
	public int getGenerationDepth()
	{
		return this.generationDepth;
	}
	
	@Override
	public int getBiomeRarityScale()
	{
		return this.biomeRarityScale;
	}

	@Override
	public boolean getOldGroupRarity()
	{
		return this.oldGroupRarity;
	}

	@Override
	public boolean getOldLandRarity() {
		return this.oldLandRarity;
	}

	@Override
	public LocalMaterialData getCooledLavaBlock()
	{
		return this.cooledLavaBlock;
	}
		
	@Override
	public LocalMaterialData getIceBlock()
	{
		return this.iceBlock;
	}

	@Override
	public LocalMaterialData getCarverLavaBlock()
	{
		return this.carverLavaBlock;
	}

	@Override
	public boolean getIsCeilingBedrock()
	{
		return this.ceilingBedrock;
	}

	@Override
	public boolean getIsFlatBedrock()
	{
		return this.flatBedrock;
	}

	@Override
	public int getCarverLavaBlockHeight()
	{
		return this.carverLavaBlockHeight;
	}
	
	@Override
	public int getMaxSmoothRadius()
	{
		return this.maxSmoothRadius;
	}
	
	@Override
	public boolean getWoodlandMansionsEnabled()
	{
		return this.woodlandMansionsEnabled;
	}

	@Override
	public boolean getNetherFortressesEnabled()
	{
		return this.netherFortressesEnabled;
	}

	@Override
	public boolean getBuriedTreasureEnabled()
	{
		return this.buriedTreasureEnabled;
	}

	@Override
	public boolean getOceanRuinsEnabled()
	{
		return this.oceanRuinsEnabled;
	}

	@Override
	public boolean getPillagerOutpostsEnabled()
	{
		return this.pillagerOutpostsEnabled;
	}

	@Override
	public boolean getBastionRemnantsEnabled()
	{
		return this.bastionRemnantsEnabled;
	}

	@Override
	public boolean getNetherFossilsEnabled()
	{
		return this.netherFossilsEnabled;
	}

	@Override
	public boolean getEndCitiesEnabled()
	{
		return this.endCitiesEnabled;
	}

	@Override
	public boolean getRuinedPortalsEnabled()
	{
		return this.ruinedPortalsEnabled;
	}

	@Override
	public boolean getShipWrecksEnabled()
	{
		return this.shipWrecksEnabled;
	}

	@Override
	public boolean getStrongholdsEnabled()
	{
		return this.strongholdsEnabled;
	}

	@Override
	public boolean getVillagesEnabled()
	{
		return this.villagesEnabled;
	}

	@Override
	public boolean getMineshaftsEnabled()
	{
		return this.mineshaftsEnabled;
	}

	@Override
	public boolean getOceanMonumentsEnabled()
	{
		return this.oceanMonumentsEnabled;
	}

	@Override
	public boolean getRareBuildingsEnabled()
	{
		return this.rareBuildingsEnabled;
	}
	
	@Override
	public int getVillageSpacing()
	{
		return this.villageSpacing;
	}
	
	@Override
	public int getVillageSeparation()
	{
		return this.villageSeparation;
	}
	
	@Override
	public int getDesertPyramidSpacing()
	{
		return this.desertPyramidSpacing;
	}
	
	@Override
	public int getDesertPyramidSeparation()
	{
		return this.desertPyramidSeparation;
	}
	
	@Override
	public int getIglooSpacing()
	{
		return this.iglooSpacing;
	}

	@Override
	public int getIglooSeparation()
	{
		return this.iglooSeparation;
	}
	
	@Override
	public int getJungleTempleSpacing()
	{
		return this.jungleTempleSpacing;
	}
	
	@Override
	public int getJungleTempleSeparation()
	{
		return this.jungleTempleSeparation;
	}
	
	@Override
	public int getSwampHutSpacing()
	{
		return this.swampHutSpacing;
	}
	
	@Override
	public int getSwampHutSeparation()
	{
		return this.swampHutSeparation;
	}
	
	@Override
	public int getPillagerOutpostSpacing()
	{
		return this.pillagerOutpostSpacing;
	}
	
	@Override
	public int getPillagerOutpostSeparation()
	{
		return this.pillagerOutpostSeparation;
	}
	
	@Override
	public int getStrongholdSpacing()
	{
		return this.strongholdSpacing;
	}
	
	@Override
	public int getStrongholdSeparation()
	{
		return this.strongholdSeparation;
	}
	
	@Override
	public int getStrongHoldDistance()
	{
		return this.strongholdDistance;
	}
	
	@Override
	public int getStrongHoldSpread()
	{
		return this.strongholdSpread;
	}	
	
	@Override
	public int getStrongHoldCount()
	{
		return this.strongholdCount;
	}	
	
	@Override
	public int getOceanMonumentSpacing()
	{
		return this.oceanMonumentSpacing;
	}
	
	@Override
	public int getOceanMonumentSeparation()
	{
		return this.oceanMonumentSeparation;
	}
	
	@Override
	public int getEndCitySpacing()
	{
		return this.endCitySpacing;
	}

	@Override
	public int getEndCitySeparation()
	{
		return this.endCitySeparation;
	}
	
	@Override
	public int getWoodlandMansionSpacing()
	{
		return this.woodlandMansionSpacing;
	}
	
	@Override
	public int getWoodlandMansionSeparation()
	{
		return this.woodlandMansionSeparation;
	}
	
	@Override
	public int getBuriedTreasureSpacing()
	{
		return this.buriedTreasureSpacing;
	}
	
	@Override
	public int getBuriedTreasureSeparation()
	{
		return this.buriedTreasureSeparation;	
	}
		
	@Override
	public int getMineshaftSpacing()
	{
		return this.mineshaftSpacing;
	}
	
	@Override
	public int getMineshaftSeparation()
	{
		return this.mineshaftSeparation;
	}
	
	@Override
	public int getRuinedPortalSpacing()
	{
		return this.ruinedPortalSpacing;
	}
	
	@Override
	public int getRuinedPortalSeparation()
	{
		return this.ruinedPortalSeparation;
	}
	
	@Override
	public int getShipwreckSpacing()
	{
		return this.shipwreckSpacing;
	}
	
	@Override
	public int getShipwreckSeparation()
	{
		return this.shipwreckSeparation;
	}
	
	@Override
	public int getOceanRuinSpacing()
	{
		return this.oceanRuinSpacing;
	}
	
	@Override
	public int getOceanRuinSeparation()
	{
		return this.oceanRuinSeparation;
	}
	
	@Override
	public int getBastionRemnantSpacing()
	{
		return this.bastionRemnantSpacing;
	}
	
	@Override
	public int getBastionRemnantSeparation()
	{
		return this.bastionRemnantSeparation;
	}
	
	@Override
	public int getNetherFortressSpacing()
	{
		return this.netherFortressSpacing;
	}
	
	@Override
	public int getNetherFortressSeparation()
	{
		return this.netherFortressSeparation;
	}
	
	@Override
	public int getNetherFossilSpacing()
	{
		return this.netherFossilSpacing;
	}
	
	@Override
	public int getNetherFossilSeparation()
	{
		return this.netherFossilSeparation;
	}
	
	//

	@Override
	public boolean getRemoveSurfaceStone()
	{
		return this.removeSurfaceStone;
	}
	
	@Override
	public ConfigMode getSettingsMode()
	{
		return this.settingsMode;
	}

	@Override
	public String getShortPresetName()
	{
		return this.shortPresetName;
	}

	@Override
	public int getMajorVersion()
	{
		return this.majorVersion;
	}
	
	@Override
	public String getAuthor()
	{
		return this.author;
	}

	@Override
	public String getDescription()
	{
		return this.description;
	}

	@Override
	public LocalMaterialData getWaterBlock()
	{
		return this.waterBlock;
	}
	
	@Override
	public int getWaterLevelMax()
	{
		return this.waterLevelMax;
	}
	
	@Override
	public int getWaterLevelMin()
	{
		return this.waterLevelMin;
	}
	
	@Override
	public ArrayList<String> getWorldBiomes()
	{
		return this.worldBiomes;
	}
	
	@Override
	public List<String> getBlackListedBiomes()
	{
		return this.blackListedBiomes;
	}
	
	@Override
	public int getWorldHeightScale()
	{
		return this.worldHeightScale;
	}
	
	@Override
	public void setMaxSmoothRadius(int smoothRadius)
	{
		this.maxSmoothRadius = smoothRadius;
	}
	
	@Override
	public String getDefaultOceanBiome()
	{
		return this.defaultOceanBiome;
	}

	@Override
	public String getDefaultWarmOceanBiome()
	{
		return this.defaultWarmOceanBiome;
	}

	@Override
	public String getDefaultLukewarmOceanBiome()
	{
		return this.defaultLukewarmOceanBiome;
	}

	@Override
	public String getDefaultColdOceanBiome()
	{
		return this.defaultColdOceanBiome;
	}

	@Override
	public String getDefaultFrozenOceanBiome()
	{
		return this.defaultFrozenOceanBiome;
	}

	@Override
	public int getLandFuzzy()
	{
		return this.landFuzzy;
	}

	@Override
	public int getLandRarity()
	{
		return this.landRarity;
	}

	@Override
	public int getLandSize()
	{
		return this.landSize;
	}

	@Override
	public int getOceanBiomeSize()
	{
		return this.oceanBiomeSize;
	}

	@Override
	public BiomeMode getBiomeMode()
	{
		return this.biomeMode;
	}
	
	@Override
	public double getFrozenOceanTemperature()
	{
		return this.frozenOceanTemperature;
	}

	@Override
	public List<String> getIsleBiomes()
	{
		return this.isleBiomes;
	}

	@Override
	public List<String> getBorderBiomes()
	{
		return this.borderBiomes;
	}
		
	@Override
	public ImageOrientation getImageOrientation()
	{
		return this.imageOrientation;
	}

	@Override
	public String getImageFile()
	{
		return this.imageFile;
	}

	@Override
	public String getImageFillBiome()
	{
		return this.imageFillBiome;
	}

	@Override
	public ImageMode getImageMode()
	{
		return this.imageMode;
	}

	@Override
	public int getImageZOffset()
	{
		return this.imageZOffset;
	}

	@Override
	public int getImageXOffset()
	{
		return this.imageXOffset;
	}
	
	@Override
	public boolean getIsRandomRivers()
	{
		return this.randomRivers;
	}
	
	@Override
	public int getRiverRarity()
	{
		return this.riverRarity;
	}
	
	@Override
	public int getRiverSize()
	{
		return this.riverSize;
	}

	@Override
	public boolean getRiversEnabled()
	{
		return this.riversEnabled;
	}
	
	// Dimension settings
	
	@Override
	public OptionalLong getFixedTime()
	{
		return this.fixedTime;
	}
	
	@Override
	public boolean getHasSkyLight()
	{
		return this.hasSkyLight;
	}

	@Override
	public boolean getHasCeiling()
	{
		return this.hasCeiling;
	}

	@Override
	public boolean getUltraWarm()
	{
		return this.ultraWarm;
	}

	@Override
	public boolean getNatural()
	{
		return this.natural;
	}

	@Override
	public double getCoordinateScale()
	{
		return this.coordinateScale;
	}

	@Override
	public boolean getCreateDragonFight()
	{
		return this.createDragonFight;
	}

	@Override
	public boolean getPiglinSafe()
	{
		return this.piglinSafe;
	}

	@Override
	public boolean getBedWorks()
	{
		return this.bedWorks;
	}

	@Override
	public boolean getRespawnAnchorWorks()
	{
		return this.respawnAnchorWorks;
	}

	@Override
	public boolean getHasRaids()
	{
		return this.hasRaids;
	}

	@Override
	public int getLogicalHeight()
	{
		return this.logicalHeight;
	}

	@Override
	public String getInfiniburn()
	{
		return this.infiniburn;
	}

	@Override
	public String getEffectsLocation()
	{
		return this.effectsLocation;
	}

	@Override
	public float getAmbientLight()
	{
		return this.ambientLight;
	}

	// Portal settings
	
	@Override
	public ArrayList<LocalMaterialData> getPortalBlocks()
	{
		return this.portalBlocks;
	}

	@Override
	public String getPortalColor()
	{
		return this.portalColor;
	}

	@Override
	public String getPortalMob()
	{
		return this.portalMob;
	}

	@Override
	public String getPortalIgnitionSource()
	{
		return this.portalIgnitionSource;
	}
	
	// Spawn point
	
	@Override
	public boolean getSpawnPointSet()
	{
		return this.fixedSpawnPoint;
	}

	@Override
	public int getSpawnPointX()
	{
		return this.spawnPointX;
	}

	@Override
	public int getSpawnPointY()
	{
		return this.spawnPointY;
	}

	@Override
	public int getSpawnPointZ()
	{
		return this.spawnPointZ;
	}

	@Override
	public float getSpawnPointAngle()
	{
		return this.spawnPointAngle;
	}
	
	// Game rules
	
	@Override
	public boolean getOverrideGameRules()
	{
		return this.overrideGameRules;
	}
	
	@Override
	public boolean getDoFireTick()
	{
		return this.doFireTick;
	}

	@Override
	public boolean getMobGriefing()
	{
		return this.mobGriefing;
	}

	@Override
	public boolean getKeepInventory()
	{
		return this.keepInventory;
	}

	@Override
	public boolean getDoMobSpawning()
	{
		return this.doMobSpawning;
	}

	@Override
	public boolean getDoMobLoot()
	{
		return this.doMobLoot;
	}

	@Override
	public boolean getDoTileDrops()
	{
		return this.doTileDrops;
	}

	@Override
	public boolean getDoEntityDrops()
	{
		return this.doEntityDrops;
	}

	@Override
	public boolean getCommandBlockOutput()
	{
		return this.commandBlockOutput;
	}

	@Override
	public boolean getNaturalRegeneration()
	{
		return this.naturalRegeneration;
	}

	@Override
	public boolean getDoDaylightCycle()
	{
		return this.doDaylightCycle;
	}

	@Override
	public boolean getLogAdminCommands()
	{
		return this.logAdminCommands;
	}
	
	@Override
	public boolean getShowDeathMessages()
	{
		return this.showDeathMessages;
	}

	@Override
	public int getRandomTickSpeed()
	{
		return this.randomTickSpeed;
	}

	@Override
	public boolean getSendCommandFeedback()
	{
		return this.sendCommandFeedback;
	}

	@Override
	public boolean getSpectatorsGenerateChunks()
	{
		return this.spectatorsGenerateChunks;
	}

	@Override
	public int getSpawnRadius()
	{
		return this.spawnRadius;
	}

	@Override
	public boolean getDisableElytraMovementCheck()
	{
		return this.disableElytraMovementCheck;
	}

	@Override
	public int getMaxEntityCramming()
	{
		return this.maxEntityCramming;
	}

	@Override
	public boolean getDoWeatherCycle()
	{
		return this.doWeatherCycle;
	}

	@Override
	public boolean getDoLimitedCrafting()
	{
		return this.doLimitedCrafting;
	}

	@Override
	public int getMaxCommandChainLength()
	{
		return this.maxCommandChainLength;
	}

	@Override
	public boolean getAnnounceAdvancements()
	{
		return this.announceAdvancements;
	}

	@Override
	public boolean getDisableRaids()
	{
		return this.disableRaids;
	}

	@Override
	public boolean getDoInsomnia()
	{
		return this.doInsomnia;
	}

	@Override
	public boolean getDrowningDamage()
	{
		return this.drowningDamage;
	}

	@Override
	public boolean getFallDamage()
	{
		return this.fallDamage;
	}

	@Override
	public boolean getFireDamage()
	{
		return this.fireDamage;
	}

	@Override
	public boolean getDoPatrolSpawning()
	{
		return this.doPatrolSpawning;
	}

	@Override
	public boolean getDoTraderSpawning()
	{
		return this.doTraderSpawning;
	}

	@Override
	public boolean getForgiveDeadPlayers()
	{
		return this.forgiveDeadPlayers;
	}

	@Override
	public boolean getForceLandAtSpawn()
	{
		return this.forceLandAtSpawn;
	}

	@Override
	public boolean getUniversalAnger()
	{
		return this.universalAnger;
	}


	public boolean getLargeOreVeins()
	{
		return this.largeOreVeins;
	}
}
