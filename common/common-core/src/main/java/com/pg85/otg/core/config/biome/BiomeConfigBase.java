package com.pg85.otg.core.config.biome;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pg85.otg.config.ConfigFile;
import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums;
import com.pg85.otg.constants.SettingsEnums.GrassColorModifier;
import com.pg85.otg.constants.SettingsEnums.MineshaftType;
import com.pg85.otg.constants.SettingsEnums.OceanRuinsType;
import com.pg85.otg.constants.SettingsEnums.RareBuildingType;
import com.pg85.otg.constants.SettingsEnums.RuinedPortalType;
import com.pg85.otg.constants.SettingsEnums.VillageType;
import com.pg85.otg.customobject.resource.CustomStructureResource;
import com.pg85.otg.customobject.resource.SaplingResource;
import com.pg85.otg.gen.surface.SurfaceGenerator;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.IBiomeResourceLocation;
import com.pg85.otg.interfaces.ICustomStructureGen;
import com.pg85.otg.interfaces.ISaplingSpawner;
import com.pg85.otg.interfaces.ISurfaceGeneratorNoiseProvider;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.util.biome.ColorSet;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.GeneratingChunk;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.minecraft.EntityCategory;
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
abstract class BiomeConfigBase extends ConfigFile implements IBiomeConfig
{
	private IBiomeResourceLocation registryKey;
	private int otgBiomeId;

	// Settings container, used so we can copy a biomeconfig while 
	// changing only its id and registry key, used for non-otg 
	// biomes in otg worlds.
	protected SettingsContainer settings = new SettingsContainer();	
	class SettingsContainer
	{
		// Misc
		protected boolean replacedBlocksInited = false;
		
		// TODO: Ideally, don't contain worldConfig within biomeconfig,  
		// use a parent object that holds both, like a worldgenregion.
		protected IWorldConfig worldConfig;
		
		// Identity

		protected boolean isTemplateForBiome;
		protected SettingsEnums.TemplateBiomeType templateBiomeType;
		protected String biomeCategory;
		
		// Inheritance
		
		protected List<String> biomeDictTags;	
		
		// Placement
		
		protected int biomeSize;
		protected int biomeRarity;
		protected int biomeColor;
		protected List<String> isleInBiome;
		protected int biomeSizeWhenIsle;
		protected int biomeRarityWhenIsle;
		protected List<String> biomeIsBorder;
		protected List<String> onlyBorderNear;
		protected List<String> notBorderNear;
		protected int biomeSizeWhenBorder;
	
		// Height / volatility
		
		protected float biomeHeight;
		protected float biomeVolatility;
		protected int smoothRadius;
		protected int CHCSmoothRadius;
		protected double maxAverageHeight;
		protected double maxAverageDepth;
		protected double volatility1;
		protected double volatility2;
		protected double volatilityWeight1;
		protected double volatilityWeight2;	
		protected boolean disableBiomeHeight;
		protected double[] chcData;
		
		// Rivers
		
		protected String riverBiome;
		
		// Blocks
		
		protected LocalMaterialData stoneBlock;
		protected LocalMaterialData surfaceBlock;
		protected LocalMaterialData underWaterSurfaceBlock;
		protected LocalMaterialData groundBlock;
		protected LocalMaterialData sandStoneBlock;
		protected LocalMaterialData redSandStoneBlock;
		protected SurfaceGenerator surfaceAndGroundControl;
		protected ReplaceBlockMatrix replacedBlocks;
		
		// Water / lava / freezing
		
		protected boolean useWorldWaterLevel;
		protected int waterLevelMax;
		protected int waterLevelMin;
		protected LocalMaterialData waterBlock;
		protected LocalMaterialData iceBlock;
		protected LocalMaterialData packedIceBlock;
		protected LocalMaterialData snowBlock;
		protected LocalMaterialData cooledLavaBlock;
	
		// Visuals and weather
		
		protected float biomeTemperature;
		protected boolean useFrozenOceanTemperature;
		protected float biomeWetness;
		protected int grassColor;
		protected ColorSet grassColorControl;
		protected GrassColorModifier grassColorModifier;
		protected int foliageColor;	
		protected ColorSet foliageColorControl;
		protected int skyColor;
		protected int waterColor;
		protected ColorSet waterColorControl;
		protected int fogColor;
		protected float fogDensity;
		protected int waterFogColor;
		protected String particleType;
		protected float particleProbability;
	
		// Music and sounds
		
		protected String music;
		protected int musicMinDelay;
		protected int musicMaxDelay;
		protected boolean replaceCurrentMusic;
		protected String ambientSound;
		protected String moodSound;
		protected int moodSoundDelay;
		protected int moodSearchRange;
		protected double moodOffset;
		protected String additionsSound;
		protected double additionsTickChance;
		
		// Custom structures
		
		protected List<CustomStructureResource> customStructures = new ArrayList<CustomStructureResource>(); // Used as a cache for fast querying, not saved
		private ICustomStructureGen structureGen;
	
		// Vanilla structures
		
		protected boolean strongholdsEnabled;
		protected boolean oceanMonumentsEnabled;	
		protected boolean woodLandMansionsEnabled;
		protected boolean netherFortressesEnabled;
		protected int villageSize;
		protected VillageType villageType;
		protected RareBuildingType rareBuildingType;
		protected MineshaftType mineshaftType = MineshaftType.normal;	
		protected boolean buriedTreasureEnabled;
		protected boolean shipWreckEnabled;
		protected boolean shipWreckBeachedEnabled;
		protected boolean pillagerOutpostEnabled;
		protected boolean bastionRemnantEnabled;
		protected boolean netherFossilEnabled;
		protected boolean endCityEnabled;
		protected float mineshaftProbability;
		protected RuinedPortalType ruinedPortalType;
		protected OceanRuinsType oceanRuinsType;
		protected float oceanRuinsLargeProbability;
		protected float oceanRuinsClusterProbability;
		protected float buriedTreasureProbability;
		protected int pillagerOutpostSize;
		protected int bastionRemnantSize;	
		
		// Mob spawning

		protected Map<EntityCategory, List<WeightedMobSpawnGroup>> spawnGroupsMerged = new HashMap<>();
		
		protected String inheritMobsBiomeName;
		
		// Resources
		
		protected List<ConfigFunction<IBiomeConfig>> resourceQueue = new ArrayList<ConfigFunction<IBiomeConfig>>();
		
		// Saplings
		
		protected Map<SaplingType, SaplingResource> saplingGrowers = new EnumMap<SaplingType, SaplingResource>(SaplingType.class);
		protected Map<LocalMaterialData, SaplingResource> customSaplingGrowers = new HashMap<>();
		protected Map<LocalMaterialData, SaplingResource> customBigSaplingGrowers = new HashMap<>();
	}
	
	protected BiomeConfigBase(String configName)
	{
		super(configName);
	}

	public List<ConfigFunction<IBiomeConfig>> getResourceQueue()
	{
		return this.settings.resourceQueue;
	}

	@Override
	public void setRegistryKey(IBiomeResourceLocation registryKey)
	{
		this.registryKey = registryKey;
	}
	
	@Override
	public IBiomeResourceLocation getRegistryKey()
	{
		return this.registryKey;
	}

	@Override
	public void setOTGBiomeId(int id)
	{
		this.otgBiomeId = id;
	}
	
	@Override
	public int getOTGBiomeId()
	{
		return this.otgBiomeId;
	}

	// TODO: Ideally, callers should be aware whether they're fetching a block underwater or not, and call getUnderWaterSurfaceBlock instead.
	@Override
	public LocalMaterialData getSurfaceBlockAtHeight(ISurfaceGeneratorNoiseProvider noiseProvider, int x, int y, int z)
	{
		return this.settings.surfaceAndGroundControl.getSurfaceBlockAtHeight(noiseProvider, this, x, y, z);
	}
	
	@Override
	public LocalMaterialData getGroundBlockAtHeight(ISurfaceGeneratorNoiseProvider noiseProvider, int x, int y, int z)
	{
		return this.settings.surfaceAndGroundControl.getGroundBlockAtHeight(noiseProvider, this, x, y, z);
	}
		
	@Override
	public LocalMaterialData getDefaultGroundBlock()
	{
		return this.settings.groundBlock;
	}

	@Override
	public LocalMaterialData getDefaultStoneBlock()
	{
		return this.settings.stoneBlock;
	}

	@Override
	public LocalMaterialData getDefaultWaterBlock()
	{
		return this.settings.waterBlock;
	}

	private void initReplaceBlocks()
	{
		if(!this.settings.replacedBlocksInited)
		{
			// Multiple threads may be working with
			// the same biome configs async, lock.
			synchronized(this)
			{
				if(!this.settings.replacedBlocksInited)
				{
					this.settings.replacedBlocks.init(
						this.settings.useWorldWaterLevel ? this.settings.worldConfig.getCooledLavaBlock() : this.settings.cooledLavaBlock,
						this.settings.useWorldWaterLevel ? this.settings.worldConfig.getIceBlock() : this.settings.iceBlock,
						this.settings.packedIceBlock,
						this.settings.snowBlock,
						this.settings.useWorldWaterLevel ? this.settings.worldConfig.getWaterBlock() : this.settings.waterBlock,
						this.settings.stoneBlock,
						this.settings.groundBlock,
						this.settings.surfaceBlock,
						this.settings.underWaterSurfaceBlock,
						this.settings.worldConfig.getDefaultBedrockBlock(),
						this.settings.sandStoneBlock,
						this.settings.redSandStoneBlock
					);
				}
				this.settings.replacedBlocksInited = true;
			}
		}
	}

	// Note: getSurfaceBlockReplaced / getGroundBlockReplaced don't take into
	// account SAGC, so they should only be used by surfacegenerators.
	
	@Override
	public LocalMaterialData getSurfaceBlockReplaced(int y)
	{
		if(getReplaceBlocks().replacesSurface)
		{
			return this.settings.surfaceBlock.parseWithBiomeAndHeight(this.settings.worldConfig.getBiomeConfigsHaveReplacement(), getReplaceBlocks(), y);
		}
		return this.settings.surfaceBlock;
	}
	
	@Override
	public LocalMaterialData getUnderWaterSurfaceBlockReplaced(int y)
	{
		if(getReplaceBlocks().replacesUnderWaterSurface)
		{
			return this.settings.underWaterSurfaceBlock.parseWithBiomeAndHeight(this.settings.worldConfig.getBiomeConfigsHaveReplacement(), getReplaceBlocks(), y);
		}
		return this.settings.underWaterSurfaceBlock;
	}	
	
	@Override
	public LocalMaterialData getGroundBlockReplaced(int y)
	{
		if(getReplaceBlocks().replacesGround)
		{
			return this.settings.groundBlock.parseWithBiomeAndHeight(this.settings.worldConfig.getBiomeConfigsHaveReplacement(), getReplaceBlocks(), y);
		}
		return this.settings.groundBlock;
	}
	
	@Override
	public LocalMaterialData getStoneBlockReplaced(int y)
	{
		if(getReplaceBlocks().replacesStone)
		{
			return this.settings.stoneBlock.parseWithBiomeAndHeight(this.settings.worldConfig.getBiomeConfigsHaveReplacement(), getReplaceBlocks(), y);
		}
		return this.settings.stoneBlock;
	}

	@Override
	public LocalMaterialData getBedrockBlockReplaced(int y)
	{
		return this.settings.worldConfig.getBedrockBlockReplaced(getReplaceBlocks(), y);
	}
		
	@Override
	public LocalMaterialData getWaterBlockReplaced(int y)
	{
		if(getReplaceBlocks().replacesWater)
		{
			return this.settings.waterBlock.parseWithBiomeAndHeight(this.settings.worldConfig.getBiomeConfigsHaveReplacement(), getReplaceBlocks(), y);
		}
		return this.settings.waterBlock;
	}
	
	@Override
	public LocalMaterialData getSandStoneBlockReplaced(int y)
	{
		if(getReplaceBlocks().replacesSandStone)
		{
			return this.settings.sandStoneBlock.parseWithBiomeAndHeight(this.settings.worldConfig.getBiomeConfigsHaveReplacement(), getReplaceBlocks(), y);
		}
		return this.settings.sandStoneBlock;
	}
	
	@Override
	public LocalMaterialData getIceBlockReplaced(int y)
	{
		if(getReplaceBlocks().replacesIce)
		{
			return this.settings.iceBlock.parseWithBiomeAndHeight(this.settings.worldConfig.getBiomeConfigsHaveReplacement(), getReplaceBlocks(), y);
		}
		return this.settings.iceBlock;
	}
	
	@Override
	public LocalMaterialData getPackedIceBlockReplaced(int y)
	{
		if(getReplaceBlocks().replacesPackedIce)
		{
			return this.settings.packedIceBlock.parseWithBiomeAndHeight(this.settings.worldConfig.getBiomeConfigsHaveReplacement(), getReplaceBlocks(), y);
		}
		return this.settings.packedIceBlock;
	}

	@Override
	public LocalMaterialData getSnowBlockReplaced(int y)
	{
		if(getReplaceBlocks().replacesSnow)
		{
			return this.settings.snowBlock.parseWithBiomeAndHeight(this.settings.worldConfig.getBiomeConfigsHaveReplacement(), getReplaceBlocks(), y);
		}
		return this.settings.snowBlock;
	}
	
	@Override
	public LocalMaterialData getCooledLavaBlockReplaced(int y)
	{
		if(getReplaceBlocks().replacesCooledLava)
		{
			return this.settings.cooledLavaBlock.parseWithBiomeAndHeight(this.settings.worldConfig.getBiomeConfigsHaveReplacement(), getReplaceBlocks(), y);
		}
		return this.settings.cooledLavaBlock;
	}
	
	@Override
	public boolean hasReplaceBlocksSettings()
	{
		return this.settings.replacedBlocks.hasReplaceSettings();
	}

	@Override
	public ReplaceBlockMatrix getReplaceBlocks()
	{
		initReplaceBlocks();
		return this.settings.replacedBlocks;
	}

	@Override
	public List<List<String>> getCustomStructureNames()
	{
		List<List<String>> customStructureNamesByGen = new ArrayList<>();
		for(CustomStructureResource structureGens : this.settings.customStructures)
		{
			List<String> customStructureNames = new ArrayList<>(structureGens.objectNames);
			customStructureNamesByGen.add(customStructureNames);
		}
		return customStructureNamesByGen; 
	}
	
	@Override
	public List<ICustomStructureGen> getCustomStructures()
	{
		return new ArrayList<ICustomStructureGen>(this.settings.customStructures);
	}
	
	@Override
	public ICustomStructureGen getStructureGen()
	{
		return this.settings.structureGen;
	}
	
	@Override
	public void setStructureGen(ICustomStructureGen customStructureGen)
	{
		this.settings.structureGen = customStructureGen;
	}
	
	@Override
	public String getName()
	{
		return this.configName;
	}

	@Override
	public boolean getIsTemplateForBiome()
	{
		return this.settings.isTemplateForBiome;
	}

	@Override
	public String getBiomeCategory()
	{
		return this.settings.biomeCategory;
	}	
			
	@Override
	public float getBiomeTemperature()
	{
		return this.settings.biomeTemperature;
	}
	
	@Override
	public boolean useFrozenOceanTemperature()
	{
		return this.settings.useFrozenOceanTemperature;
	}
	
	@Override
	public float getBiomeHeight()
	{
		return this.settings.biomeHeight;
	}
	
	@Override
	public float getBiomeVolatility()
	{
		return this.settings.biomeVolatility;
	}
	
	@Override
	public double getVolatility1()
	{
		return this.settings.volatility1;
	}
	
	@Override
	public double getVolatility2()
	{
		return this.settings.volatility2;
	}
	
	@Override
	public int getBiomeColor()
	{
		return this.settings.biomeColor;
	}
	
	@Override
	public List<String> getBiomeDictTags()
	{
		return this.settings.biomeDictTags;
	}	
	
	@Override
	public float getBiomeWetness()
	{
		return this.settings.biomeWetness;
	}
	
	@Override
	public int getCHCSmoothRadius()
	{
		return this.settings.CHCSmoothRadius;
	}
	
	@Override
	public int getSkyColor()
	{
		return this.settings.skyColor;
	}
	
	@Override
	public int getFogColor()
	{
		return this.settings.fogColor;
	}
	
	@Override
	public float getFogDensity()
	{
		return this.settings.fogDensity;
	}
	
	@Override
	public int getWaterFogColor()
	{
		return this.settings.waterFogColor;
	}

	@Override
	public int getFoliageColor()
	{
		return this.settings.foliageColor;
	}
	
	@Override
	public ColorSet getFoliageColorControl()
	{
		return this.settings.foliageColorControl;
	}
	
	@Override
	public int getGrassColor()
	{
		return this.settings.grassColor;
	}
	
	@Override
	public ColorSet getGrassColorControl()
	{
		return this.settings.grassColorControl;
	}

	@Override
	public GrassColorModifier getGrassColorModifier()
	{
		return this.settings.grassColorModifier;
	}	
	
	@Override
	public int getWaterColor()
	{
		return this.settings.waterColor;
	}
	
	@Override
	public ColorSet getWaterColorControl()
	{
		return this.settings.waterColorControl;
	}
	
	@Override
	public String getParticleType()
	{
		return this.settings.particleType;
	}
	
	@Override
	public float getParticleProbability()
	{
		return this.settings.particleProbability;
	}

	@Override
	public String getMusic()
	{
		return this.settings.music;
	}

	@Override
	public int getMusicMinDelay()
	{
		return this.settings.musicMinDelay;
	}

	@Override
	public int getMusicMaxDelay()
	{
		return this.settings.musicMaxDelay;
	}

	@Override
	public boolean isReplaceCurrentMusic()
	{
		return this.settings.replaceCurrentMusic;
	}

	@Override
	public String getAmbientSound()
	{
		return this.settings.ambientSound;
	}

	@Override
	public String getMoodSound()
	{
		return this.settings.moodSound;
	}

	@Override
	public int getMoodSoundDelay()
	{
		return this.settings.moodSoundDelay;
	}

	@Override
	public int getMoodSearchRange()
	{
		return this.settings.moodSearchRange;
	}

	@Override
	public double getMoodOffset()
	{
		return this.settings.moodOffset;
	}

	@Override
	public String getAdditionsSound()
	{
		return this.settings.additionsSound;
	}

	@Override
	public double getAdditionsTickChance()
	{
		return this.settings.additionsTickChance;
	}
	
	@Override
	public VillageType getVillageType()
	{
		return this.settings.villageType;
	}
	
	@Override
	public int getVillageSize()
	{
		return this.settings.villageSize;
	}
	
	@Override
	public MineshaftType getMineShaftType()
	{
		return this.settings.mineshaftType;
	}
	
	@Override
	public float getMineShaftProbability()
	{
		return this.settings.mineshaftProbability;
	}
	
	@Override
	public OceanRuinsType getOceanRuinsType()
	{
		return this.settings.oceanRuinsType;
	}
	
	@Override
	public float getOceanRuinsLargeProbability()
	{
		return this.settings.oceanRuinsLargeProbability;
	}
	
	@Override
	public float getOceanRuinsClusterProbability()
	{
		return this.settings.oceanRuinsClusterProbability;
	}
	
	@Override
	public boolean getBuriedTreasureEnabled()
	{
		return this.settings.buriedTreasureEnabled;
	}
	
	@Override
	public float getBuriedTreasureProbability()
	{
		return this.settings.buriedTreasureProbability;
	}

	@Override
	public boolean getPillagerOutpostEnabled()
	{
		return this.settings.pillagerOutpostEnabled;
	}
	
	@Override
	public int getPillagerOutPostSize()
	{
		return this.settings.pillagerOutpostSize;
	}

	@Override
	public boolean getBastionRemnantEnabled()
	{
		return this.settings.bastionRemnantEnabled;
	}
	
	@Override
	public int getBastionRemnantSize()
	{
		return this.settings.bastionRemnantSize;
	}
		
	@Override
	public RareBuildingType getRareBuildingType()
	{
		return this.settings.rareBuildingType;
	}
	
	@Override
	public RuinedPortalType getRuinedPortalType()
	{
		return this.settings.ruinedPortalType;
	}
	
	@Override
	public boolean getWoodlandMansionsEnabled()
	{
		return this.settings.woodLandMansionsEnabled;
	}
	
	@Override
	public boolean getNetherFortressesEnabled()
	{
		return this.settings.netherFortressesEnabled;
	}
	
	@Override
	public boolean getShipWreckEnabled()
	{
		return this.settings.shipWreckEnabled;
	}

	@Override
	public boolean getShipWreckBeachedEnabled()
	{
		return this.settings.shipWreckBeachedEnabled;
	}
	
	@Override
	public boolean getNetherFossilEnabled()
	{
		return this.settings.netherFossilEnabled;
	}
	
	@Override
	public boolean getEndCityEnabled()
	{
		return this.settings.endCityEnabled;
	}
	
	@Override
	public boolean getStrongholdsEnabled()
	{
		return this.settings.strongholdsEnabled;
	}
	
	@Override
	public boolean getOceanMonumentsEnabled()
	{
		return this.settings.oceanMonumentsEnabled;
	}
	
	@Override
	public String getInheritMobsBiomeName()
	{
		return this.settings.inheritMobsBiomeName;
	}

	@Override
	public List<WeightedMobSpawnGroup> getSpawnList(EntityCategory category)
	{
		return this.settings.spawnGroupsMerged.get(category);
	}
	
	@Override
	public int getBiomeRarity()
	{
		return this.settings.biomeRarity;
	}
	
	@Override
	public int getBiomeSize()
	{
		return this.settings.biomeSize;
	}
	
	@Override
	public double getVolatilityWeight1()
	{
		return this.settings.volatilityWeight1;
	}
	
	@Override
	public double getVolatilityWeight2()
	{
		return this.settings.volatilityWeight2;
	}
	
	@Override
	public double getMaxAverageDepth()
	{
		return this.settings.maxAverageDepth;
	}
	
	@Override
	public double getMaxAverageHeight()
	{
		return this.settings.maxAverageHeight;
	}
	
	@Override
	public double getCHCData(int y)
	{
		return this.settings.chcData[y];
	}
	
	@Override
	public int getSmoothRadius()
	{
		return this.settings.smoothRadius;
	}
	
	@Override
	public int getWaterLevelMax()
	{
		return this.settings.waterLevelMax;
	}
	
	@Override
	public int getWaterLevelMin()
	{
		return this.settings.waterLevelMin;
	}
	
	@Override
	public boolean biomeConfigsHaveReplacement()
	{
		return this.settings.worldConfig.getBiomeConfigsHaveReplacement();
	}
	
	@Override
	public double getFractureHorizontal()
	{
		return this.settings.worldConfig.getFractureHorizontal();
	}
	
	@Override
	public double getFractureVertical()
	{
		return this.settings.worldConfig.getFractureVertical();
	}
	
	@Override
	public boolean isFlatBedrock()
	{
		return this.settings.worldConfig.getIsFlatBedrock();
	}
	
	@Override
	public boolean isCeilingBedrock()
	{
		return this.settings.worldConfig.getIsCeilingBedrock();
	}
	
	@Override
	public boolean isBedrockDisabled()
	{
		return this.settings.worldConfig.getBedrockDisabled();
	}

	@Override
	public boolean isRemoveSurfaceStone()
	{
		return this.settings.worldConfig.getRemoveSurfaceStone();
	}

	@Override
	public boolean disableBiomeHeight()
	{
		return this.settings.disableBiomeHeight;
	}

	@Override
	public boolean isIsleBiome()
	{
		return
			this.settings.isleInBiome != null && 
			this.settings.isleInBiome.size() > 0 &&
			this.settings.worldConfig.getIsleBiomes().contains(this.getName())
		;
	}

	@Override
	public boolean isBorderBiome()
	{
		return
			this.settings.biomeIsBorder != null && 
			this.settings.biomeIsBorder.size() > 0 &&
			this.settings.worldConfig.getBorderBiomes().contains(this.getName())
		;
	}

	@Override
	public List<String> getIsleInBiomes()
	{
		return this.settings.isleInBiome;
	}
	
	@Override
	public List<String> getBorderInBiomes()
	{
		return this.settings.biomeIsBorder;
	}
	
	@Override
	public List<String> getOnlyBorderNearBiomes()
	{
		return this.settings.onlyBorderNear;
	}	
	
	@Override
	public List<String> getNotBorderNearBiomes()
	{
		return this.settings.notBorderNear;
	}

	@Override
	public int getBiomeSizeWhenIsle()
	{
		return this.settings.biomeSizeWhenIsle;
	}
	
	@Override
	public int getBiomeRarityWhenIsle()
	{
		return this.settings.biomeRarityWhenIsle;
	}
	
	@Override
	public int getBiomeSizeWhenBorder()
	{
		return this.settings.biomeSizeWhenBorder;
	}

	@Override
	public String getRiverBiome()
	{
		return this.settings.riverBiome;
	}		  

	/**
	 * This is a pretty weak map from -0.5 to ~-0.8 (min vanilla temperature)
	 *
	 * TODO: We should probably make this more configurable in the future?
	 *
	 * @param temp The temp to get snow height for
	 * @return A value from 0 to 7 to be used for snow height
	 */
	@Override
	public int getSnowHeight(float temp)
	{
		// OTG biome temperature is between 0.0 and 2.0.
		// Judging by WorldStandardValues.SNOW_AND_ICE_MAX_TEMP, snow should appear below 0.15.
		// According to the configs, snow and ice should appear between 0.2 (at y > 90) and 0.1 (entire biome covered in ice).
		// Let's make sure that at 0.2, snow layers start with thickness 0 at y 90 and thickness 7 around y 255.
		// In a 0.2 temp biome, y90 temp is 0.156, y255 temp is -0.12
				
		float snowTemp = Constants.SNOW_AND_ICE_TEMP;
		if(temp <= snowTemp)
		{
			float maxColdTemp = Constants.SNOW_AND_ICE_MAX_TEMP;
			float maxThickness = 7.0f;
			if(temp < maxColdTemp)
			{
				return (int)maxThickness;
			}
			float range = Math.abs(maxColdTemp - snowTemp);
			float fraction = Math.abs(maxColdTemp - temp);
			return (int)Math.floor((1.0f - (fraction / range)) * maxThickness);
		}

		return  0;
	}
	
	@Override
	public void doSurfaceAndGroundControl(long worldSeed, GeneratingChunk generatingChunk, ChunkBuffer chunkBuffer, int x, int z, IBiome biome)
	{
		this.settings.surfaceAndGroundControl.spawn(worldSeed, generatingChunk, chunkBuffer, biome, x, z);
	}
	
	@Override
	public ISaplingSpawner getSaplingGen(SaplingType type)
	{
		SaplingResource gen = this.settings.saplingGrowers.get(type);
		if (gen == null && type.growsTree())
		{
			gen = this.settings.saplingGrowers.get(SaplingType.All);
		}
		return gen;
	}

	@Override
	public ISaplingSpawner getCustomSaplingGen(LocalMaterialData materialData, boolean wideTrunk)
	{
		if (wideTrunk)
		{
			ISaplingSpawner spawner = this.settings.customBigSaplingGrowers.get(materialData);
			if(spawner != null)
			{
				return spawner;
			}
		}
		return this.settings.customSaplingGrowers.get(materialData);
	}	
}
