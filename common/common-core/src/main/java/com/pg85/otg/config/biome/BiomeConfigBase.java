package com.pg85.otg.config.biome;

import java.util.ArrayList;
import java.util.List;

import com.pg85.otg.config.ConfigFile;
import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums.GrassColorModifier;
import com.pg85.otg.constants.SettingsEnums.MineshaftType;
import com.pg85.otg.constants.SettingsEnums.OceanRuinsType;
import com.pg85.otg.constants.SettingsEnums.RareBuildingType;
import com.pg85.otg.constants.SettingsEnums.RuinedPortalType;
import com.pg85.otg.constants.SettingsEnums.VillageType;
import com.pg85.otg.customobject.resource.CustomStructureResource;
import com.pg85.otg.gen.surface.SurfaceGenerator;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.GeneratingChunk;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IBiomeResourceLocation;
import com.pg85.otg.util.interfaces.ICustomStructureGen;
import com.pg85.otg.util.interfaces.IWorldConfig;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;

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
	// Misc
	
	private IBiomeResourceLocation registryKey;
	private boolean replacedBlocksInited = false;
	
	// TODO: Ideally, don't contain worldConfig within biomeconfig,  
	// use a parent object that holds both, like a worldgenregion.
	protected IWorldConfig worldConfig;
	
	// Identity
	
	protected String templateForBiome;
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
	protected LocalMaterialData cooledLavaBlock;

	// Visuals and weather
	
	protected float biomeTemperature;
	protected float biomeWetness;
	protected int grassColor;
	protected GrassColorModifier grassColorModifier;
	protected int foliageColor;	
	protected int skyColor;
	protected int waterColor;
	protected int fogColor;
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
	
	protected List<WeightedMobSpawnGroup> spawnMonstersMerged = new ArrayList<WeightedMobSpawnGroup>();
	protected List<WeightedMobSpawnGroup> spawnCreaturesMerged = new ArrayList<WeightedMobSpawnGroup>();
	protected List<WeightedMobSpawnGroup> spawnWaterCreaturesMerged = new ArrayList<WeightedMobSpawnGroup>();
	protected List<WeightedMobSpawnGroup> spawnAmbientCreaturesMerged = new ArrayList<WeightedMobSpawnGroup>();
	protected List<WeightedMobSpawnGroup> spawnWaterAmbientCreaturesMerged = new ArrayList<WeightedMobSpawnGroup>();
	protected List<WeightedMobSpawnGroup> spawnMiscCreaturesMerged = new ArrayList<WeightedMobSpawnGroup>();	
	
	// Resources
	
	protected List<ConfigFunction<IBiomeConfig>> resourceQueue = new ArrayList<ConfigFunction<IBiomeConfig>>();
	
	protected BiomeConfigBase(String configName)
	{
		super(configName);
	}

	public List<ConfigFunction<IBiomeConfig>> getResourceQueue()
	{
		return this.resourceQueue;
	}
	
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
	public LocalMaterialData getSurfaceBlockAtHeight(IWorldGenRegion worldGenRegion, int x, int y, int z)
	{
		return this.surfaceAndGroundControl.getSurfaceBlockAtHeight(worldGenRegion, this, x, y, z);
	}
	
	@Override
	public LocalMaterialData getGroundBlockAtHeight(IWorldGenRegion worldGenRegion, int x, int y, int z)
	{
		return this.surfaceAndGroundControl.getGroundBlockAtHeight(worldGenRegion, this, x, y, z);
	}
		
	@Override
	public LocalMaterialData getDefaultGroundBlock()
	{
		return this.groundBlock;
	}
	
	private void initReplaceBlocks()
	{
		if(!this.replacedBlocksInited)
		{
			// Multiple threads may be working with
			// the same biome configs async, lock.
			synchronized(this)
			{
				if(!this.replacedBlocksInited)
				{					
					this.replacedBlocks.init(
						this.useWorldWaterLevel ? worldConfig.getCooledLavaBlock() : this.cooledLavaBlock,
						this.useWorldWaterLevel ? worldConfig.getIceBlock() : this.iceBlock,
						this.useWorldWaterLevel ? worldConfig.getWaterBlock() : this.waterBlock,
						this.stoneBlock,
						this.groundBlock,
						this.surfaceBlock,
						this.worldConfig.getDefaultBedrockBlock(),
						this.sandStoneBlock,
						this.redSandStoneBlock
					);					
				}
				this.replacedBlocksInited = true;				
			}
		}
	}
	
	@Override
	public LocalMaterialData getSurfaceBlockReplaced(int y)
	{
		if(getReplaceBlocks().replacesSurface)
		{
			return this.surfaceBlock.parseWithBiomeAndHeight(this.worldConfig.getBiomeConfigsHaveReplacement(), getReplaceBlocks(), y);
		}
		return this.surfaceBlock;
	}
	
	@Override
	public LocalMaterialData getGroundBlockReplaced(int y)
	{
		if(getReplaceBlocks().replacesGround)
		{
			return this.groundBlock.parseWithBiomeAndHeight(this.worldConfig.getBiomeConfigsHaveReplacement(), getReplaceBlocks(), y);
		}
		return this.groundBlock;
	}
	
	@Override
	public LocalMaterialData getStoneBlockReplaced(int y)
	{
		if(getReplaceBlocks().replacesStone)
		{
			return this.stoneBlock.parseWithBiomeAndHeight(this.worldConfig.getBiomeConfigsHaveReplacement(), getReplaceBlocks(), y);
		}
		return this.stoneBlock;
	}

	@Override
	public LocalMaterialData getBedrockBlockReplaced(int y)
	{
		return this.worldConfig.getBedrockBlockReplaced(getReplaceBlocks(), y);
	}
		
	@Override
	public LocalMaterialData getWaterBlockReplaced(int y)
	{
		if(getReplaceBlocks().replacesWater)
		{
			return this.waterBlock.parseWithBiomeAndHeight(this.worldConfig.getBiomeConfigsHaveReplacement(), getReplaceBlocks(), y);
		}
		return this.waterBlock;
	}
	
	@Override
	public LocalMaterialData getSandStoneBlockReplaced(int y)
	{
		if(getReplaceBlocks().replacesSandStone)
		{
			return this.sandStoneBlock.parseWithBiomeAndHeight(this.worldConfig.getBiomeConfigsHaveReplacement(), getReplaceBlocks(), y);
		}
		return this.sandStoneBlock;
	}
	
	@Override
	public LocalMaterialData getIceBlockReplaced(int y)
	{
		if(getReplaceBlocks().replacesIce)
		{
			return this.iceBlock.parseWithBiomeAndHeight(this.worldConfig.getBiomeConfigsHaveReplacement(), getReplaceBlocks(), y);
		}
		return this.iceBlock;
	}
	
	@Override
	public LocalMaterialData getCooledLavaBlockReplaced(int y)
	{
		if(getReplaceBlocks().replacesCooledLava)
		{
			return this.cooledLavaBlock.parseWithBiomeAndHeight(this.worldConfig.getBiomeConfigsHaveReplacement(), getReplaceBlocks(), y);
		}
		return this.cooledLavaBlock;
	}
	
	@Override
	public boolean hasReplaceBlocksSettings()
	{
		return this.replacedBlocks.hasReplaceSettings();
	}

	@Override
	public ReplaceBlockMatrix getReplaceBlocks()
	{
		initReplaceBlocks();
		return this.replacedBlocks;
	}

	@Override
	public List<List<String>> getCustomStructureNames()
	{
		List<List<String>> customStructureNamesByGen = new ArrayList<>();
		for(CustomStructureResource structureGens : this.customStructures)
		{
			List<String> customStructureNames = new ArrayList<>();
			for(String objectName : structureGens.objectNames)
			{
				customStructureNames.add(objectName);
			}
			customStructureNamesByGen.add(customStructureNames);
		}
		return customStructureNamesByGen; 
	}
	
	@Override
	public List<ICustomStructureGen> getCustomStructures()
	{
		return new ArrayList<ICustomStructureGen>(this.customStructures);
	}
	
	@Override
	public ICustomStructureGen getStructureGen()
	{
		return this.structureGen;
	}
	
	@Override
	public void setStructureGen(ICustomStructureGen customStructureGen)
	{
		this.structureGen = customStructureGen;
	}
	
	@Override
	public String getName()
	{
		return this.configName;
	}
	
	@Override
	public String getTemplateForBiome()
	{
		return this.templateForBiome;
	}

	@Override
	public String getBiomeCategory()
	{
		return this.biomeCategory;
	}	
			
	@Override
	public float getBiomeTemperature()
	{
		return this.biomeTemperature;
	}
	
	@Override
	public float getBiomeHeight()
	{
		return this.biomeHeight;
	}
	
	@Override
	public float getBiomeVolatility()
	{
		return this.biomeVolatility;
	}
	
	@Override
	public double getVolatility1()
	{
		return this.volatility1;
	}
	
	@Override
	public double getVolatility2()
	{
		return this.volatility2;
	}
	
	@Override
	public int getBiomeColor()
	{
		return this.biomeColor;
	}
	
	@Override
	public List<String> getBiomeDictTags()
	{
		return this.biomeDictTags;
	}	
	
	@Override
	public float getBiomeWetness()
	{
		return this.biomeWetness;
	}
	
	@Override
	public int getCHCSmoothRadius()
	{
		return this.CHCSmoothRadius;
	}
	
	@Override
	public int getSkyColor()
	{
		return this.skyColor;
	}
	
	@Override
	public int getFogColor()
	{
		return this.fogColor;
	}
	
	@Override
	public int getWaterFogColor()
	{
		return this.waterFogColor;
	}

	@Override
	public int getFoliageColor()
	{
		return this.foliageColor;
	}
	
	@Override
	public int getGrassColor()
	{
		return this.grassColor;
	}

	@Override
	public GrassColorModifier getGrassColorModifier()
	{
		return this.grassColorModifier;
	}	
	
	@Override
	public int getWaterColor()
	{
		return this.waterColor;
	}
	
	@Override
	public String getParticleType()
	{
		return this.particleType;
	}
	
	@Override
	public float getParticleProbability()
	{
		return this.particleProbability;
	}

	@Override
	public String getMusic()
	{
		return music;
	}

	@Override
	public int getMusicMinDelay()
	{
		return musicMinDelay;
	}

	@Override
	public int getMusicMaxDelay()
	{
		return musicMaxDelay;
	}

	@Override
	public boolean isReplaceCurrentMusic()
	{
		return replaceCurrentMusic;
	}

	@Override
	public String getAmbientSound()
	{
		return ambientSound;
	}

	@Override
	public String getMoodSound()
	{
		return moodSound;
	}

	@Override
	public int getMoodSoundDelay()
	{
		return moodSoundDelay;
	}

	@Override
	public int getMoodSearchRange()
	{
		return moodSearchRange;
	}

	@Override
	public double getMoodOffset()
	{
		return moodOffset;
	}

	@Override
	public String getAdditionsSound()
	{
		return additionsSound;
	}

	@Override
	public double getAdditionsTickChance()
	{
		return additionsTickChance;
	}
	
	@Override
	public VillageType getVillageType()
	{
		return this.villageType;
	}
	
	@Override
	public int getVillageSize()
	{
		return this.villageSize;
	}
	
	@Override
	public MineshaftType getMineShaftType()
	{
		return this.mineshaftType;
	}
	
	@Override
	public float getMineShaftProbability()
	{
		return this.mineshaftProbability;
	}
	
	@Override
	public OceanRuinsType getOceanRuinsType()
	{
		return this.oceanRuinsType;
	}
	
	@Override
	public float getOceanRuinsLargeProbability()
	{
		return this.oceanRuinsLargeProbability;
	}
	
	@Override
	public float getOceanRuinsClusterProbability()
	{
		return this.oceanRuinsClusterProbability;
	}
	
	@Override
	public boolean getBuriedTreasureEnabled()
	{
		return this.buriedTreasureEnabled;
	}
	
	@Override
	public float getBuriedTreasureProbability()
	{
		return this.buriedTreasureProbability;
	}

	@Override
	public boolean getPillagerOutpostEnabled()
	{
		return this.pillagerOutpostEnabled;
	}
	
	@Override
	public int getPillagerOutPostSize()
	{
		return this.pillagerOutpostSize;
	}

	@Override
	public boolean getBastionRemnantEnabled()
	{
		return this.bastionRemnantEnabled;
	}
	
	@Override
	public int getBastionRemnantSize()
	{
		return this.bastionRemnantSize;
	}
		
	@Override
	public RareBuildingType getRareBuildingType()
	{
		return this.rareBuildingType;
	}
	
	@Override
	public RuinedPortalType getRuinedPortalType()
	{
		return this.ruinedPortalType;
	}
	
	@Override
	public boolean getWoodlandMansionsEnabled()
	{
		return this.woodLandMansionsEnabled;
	}
	
	@Override
	public boolean getNetherFortressesEnabled()
	{
		return this.netherFortressesEnabled;
	}
	
	@Override
	public boolean getShipWreckEnabled()
	{
		return this.shipWreckEnabled;
	}

	@Override
	public boolean getShipWreckBeachedEnabled()
	{
		return this.shipWreckBeachedEnabled;
	}
	
	@Override
	public boolean getNetherFossilEnabled()
	{
		return this.netherFossilEnabled;
	}
	
	@Override
	public boolean getEndCityEnabled()
	{
		return this.endCityEnabled;
	}
	
	@Override
	public boolean getStrongholdsEnabled()
	{
		return this.strongholdsEnabled;
	}
	
	@Override
	public boolean getOceanMonumentsEnabled()
	{
		return this.oceanMonumentsEnabled;
	}
	
	@Override
	public List<WeightedMobSpawnGroup> getAmbientCreatures()
	{
		return this.spawnAmbientCreaturesMerged;
	}
	
	@Override
	public List<WeightedMobSpawnGroup> getCreatures()
	{
		return this.spawnCreaturesMerged;
	}
	
	@Override
	public List<WeightedMobSpawnGroup> getMonsters()
	{
		return this.spawnMonstersMerged;
	}
	
	@Override
	public List<WeightedMobSpawnGroup> getWaterCreatures()
	{
		return this.spawnWaterCreaturesMerged;
	}
	
	@Override
	public List<WeightedMobSpawnGroup> getWaterAmbientCreatures()
	{
		return this.spawnWaterAmbientCreaturesMerged;
	}

	@Override
	public List<WeightedMobSpawnGroup> getMiscCreatures()
	{
		return this.spawnMiscCreaturesMerged;
	}
	
	@Override
	public int getBiomeRarity()
	{
		return this.biomeRarity;
	}
	
	@Override
	public int getBiomeSize()
	{
		return this.biomeSize;
	}
	
	@Override
	public double getVolatilityWeight1()
	{
		return this.volatilityWeight1;
	}
	
	@Override
	public double getVolatilityWeight2()
	{
		return this.volatilityWeight2;
	}
	
	@Override
	public double getMaxAverageDepth()
	{
		return this.maxAverageDepth;
	}
	
	@Override
	public double getMaxAverageHeight()
	{
		return this.maxAverageHeight;
	}
	
	@Override
	public double getCHCData(int y)
	{
		return this.chcData[y];
	}
	
	@Override
	public int getSmoothRadius()
	{
		return this.smoothRadius;
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
	public boolean biomeConfigsHaveReplacement()
	{
		return this.worldConfig.getBiomeConfigsHaveReplacement();
	}
	
	@Override
	public double getFractureHorizontal()
	{
		return this.worldConfig.getFractureHorizontal();
	}
	
	@Override
	public double getFractureVertical()
	{
		return this.worldConfig.getFractureVertical();
	}
	
	@Override
	public boolean isFlatBedrock()
	{
		return this.worldConfig.getIsFlatBedrock();
	}
	
	@Override
	public boolean isCeilingBedrock()
	{
		return this.worldConfig.getIsCeilingBedrock();
	}
	
	@Override
	public boolean isBedrockDisabled()
	{
		return this.worldConfig.getBedrockDisabled();
	}

	@Override
	public boolean isRemoveSurfaceStone()
	{
		return this.worldConfig.getRemoveSurfaceStone();
	}

	@Override
	public boolean disableBiomeHeight()
	{
		return this.disableBiomeHeight;
	}

	@Override
	public boolean isIsleBiome()
	{
		return
			this.isleInBiome != null && 
			this.isleInBiome.size() > 0 &&
			this.worldConfig.getIsleBiomes().contains(this.getName())
		;
	}

	@Override
	public boolean isBorderBiome()
	{
		return
			this.biomeIsBorder != null && 
			this.biomeIsBorder.size() > 0 &&
			this.worldConfig.getBorderBiomes().contains(this.getName())
		;
	}
	
	@Override
	public List<String> getIsleInBiomes()
	{
		return this.isleInBiome;
	}
	
	@Override
	public List<String> getBorderInBiomes()
	{
		return this.biomeIsBorder;
	}
	
	@Override
	public List<String> getNotBorderNearBiomes()
	{
		return this.notBorderNear;
	}

	@Override
	public int getBiomeSizeWhenIsle()
	{
		return this.biomeSizeWhenIsle;
	}
	
	@Override
	public int getBiomeRarityWhenIsle()
	{
		return this.biomeRarityWhenIsle;
	}
	
	@Override
	public int getBiomeSizeWhenBorder()
	{
		return this.biomeSizeWhenBorder;
	}

	@Override
	public String getRiverBiome()
	{
		return this.riverBiome;
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
	public void doSurfaceAndGroundControl(long worldSeed, GeneratingChunk generatingChunk, ChunkBuffer chunkBuffer, int x, int z)
	{
		this.surfaceAndGroundControl.spawn(worldSeed, generatingChunk, chunkBuffer, this, x, z);
	}
}
