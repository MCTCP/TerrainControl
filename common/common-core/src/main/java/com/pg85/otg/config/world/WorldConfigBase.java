package com.pg85.otg.config.world;

import java.util.ArrayList;
import java.util.List;
import com.pg85.otg.config.ConfigFile;
import com.pg85.otg.constants.SettingsEnums.BiomeMode;
import com.pg85.otg.constants.SettingsEnums.ConfigMode;
import com.pg85.otg.constants.SettingsEnums.CustomStructureType;
import com.pg85.otg.constants.SettingsEnums.ImageMode;
import com.pg85.otg.constants.SettingsEnums.ImageOrientation;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.interfaces.IWorldConfig;
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
	
	// Bedrock
	
	protected boolean ceilingBedrock;
	protected boolean flatBedrock;
	
	// Biome settings
	
	protected ArrayList<String> worldBiomes = new ArrayList<String>();
	protected int biomeRarityScale;
	protected boolean oldGroupRarity;
	protected int generationDepth;
	protected int landFuzzy;
	protected int landRarity;
	protected int landSize;
	protected int oceanBiomeSize;
	protected String defaultOceanBiome;
	protected String defaultWarmOceanBiome;
	protected String defaultLukewarmOceanBiome;
	protected String defaultColdOceanBiome;
	protected String defaultFrozenOceanBiome;
	protected BiomeMode biomeMode;
	protected double frozenOceanTemperature;
	protected boolean freezeAllColdGroupBiomes;
	protected List<String> isleBiomes = new ArrayList<String>();
	protected List<String> borderBiomes = new ArrayList<String>();	
	protected boolean randomRivers;
	protected int riverRarity;
	protected int riverSize;
	protected boolean riversEnabled;
	protected boolean biomeConfigsHaveReplacement = false;

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

	protected boolean woodlandMansionsEnabled;
	protected boolean netherFortressesEnabled;
	protected boolean buriedTreasureEnabled;
	protected boolean oceanRuinsEnabled;
	protected boolean pillagerOutpostsEnabled;
	protected boolean bastionRemnantsEnabled;
	protected boolean netherFossilsEnabled;
	protected boolean endCitiesEndabled;
	protected boolean ruinedPortalsEndabled;
	protected boolean shipWrecksEndabled;
	protected boolean strongholdsEnabled;
	protected boolean villagesEnabled;	
	protected boolean mineshaftsEnabled;
	protected boolean oceanMonumentsEnabled;
	protected boolean rareBuildingsEnabled;

	// OTG Custom structures
	
	protected CustomStructureType customStructureType;
	protected boolean decorationBoundsCheck;
	protected int maximumCustomStructureRadius;
	
	// Caves & Ravines
	
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

	protected int ravineRarity;
	protected int ravineMinLength;
	protected int ravineMaxLength;
	protected double ravineDepth;	
	protected int ravineMinAltitude;
	protected int ravineMaxAltitude;

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
	public CustomStructureType getCustomStructureType()
	{
		return this.customStructureType;
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
		return this.endCitiesEndabled;
	}

	@Override
	public boolean getRuinedPortalsEnabled()
	{
		return this.ruinedPortalsEndabled;
	}

	@Override
	public boolean getShipWrecksEnabled()
	{
		return this.shipWrecksEndabled;
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
	public boolean getIsFreezeGroups()
	{
		return this.freezeAllColdGroupBiomes;
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
}
