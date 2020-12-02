package com.pg85.otg.config.biome;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pg85.otg.config.ConfigFile;
import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums.MineshaftType;
import com.pg85.otg.constants.SettingsEnums.RareBuildingType;
import com.pg85.otg.constants.SettingsEnums.VillageType;
import com.pg85.otg.customobject.resource.CustomStructureGen;
import com.pg85.otg.customobject.resource.SaplingGen;
import com.pg85.otg.gen.surface.SurfaceGenerator;
import com.pg85.otg.util.biome.BiomeResourceLocation;
import com.pg85.otg.util.biome.ReplacedBlocksMatrix;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.GeneratingChunk;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.ICustomStructureGen;
import com.pg85.otg.util.interfaces.IWorldConfig;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.minecraft.SaplingType;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;

abstract class BiomeConfigBase extends ConfigFile implements IBiomeConfig
{
    private final BiomeResourceLocation registryKey;
	
	// TODO: Ideally, don't contain worldConfig within biomeconfig,  
	// use a parent object that holds both, like a worldgenregion.
    protected IWorldConfig worldConfig;
    
    protected List<String> isleInBiome;
    protected List<String> biomeIsBorder;
    protected List<String> notBorderNear;
    
    protected int biomeSizeWhenIsle;
    protected int biomeRarityWhenIsle;
    protected int biomeSizeWhenBorder;
   
    // Surface config
    protected float biomeHeight;
    protected float biomeVolatility;
    protected int smoothRadius;

    protected float biomeTemperature;

    protected int biomeRarity;
    protected int biomeSize;
    
    protected String riverBiome;

    protected LocalMaterialData stoneBlock;
    protected LocalMaterialData surfaceBlock;
    protected LocalMaterialData groundBlock;
    protected LocalMaterialData sandStoneBlock;
    protected LocalMaterialData redSandStoneBlock;

    protected ReplacedBlocksMatrix replacedBlocks;
    protected SurfaceGenerator surfaceAndGroundControl;

    protected boolean useWorldWaterLevel;
    protected int waterLevelMax;
    protected int waterLevelMin;
    protected LocalMaterialData waterBlock;
    protected LocalMaterialData iceBlock;
    protected LocalMaterialData cooledLavaBlock;

    protected List<CustomStructureGen> customStructures = new ArrayList<CustomStructureGen>(); // Used as a cache for fast querying, not saved
    private ICustomStructureGen structureGen;

    protected double maxAverageHeight;
    protected double maxAverageDepth;
    protected double volatility1;
    protected double volatility2;
    protected double volatilityWeight1;
    protected double volatilityWeight2;

    protected double[] chcData;

    protected Map<SaplingType, SaplingGen> saplingGrowers = new EnumMap<SaplingType, SaplingGen>(SaplingType.class);
    protected Map<LocalMaterialData, SaplingGen> customSaplingGrowers = new HashMap<>();
    protected Map<LocalMaterialData, SaplingGen> customBigSaplingGrowers = new HashMap<>();
    
    protected String biomeExtends;
    protected float biomeWetness;
    protected int CHCSmoothRadius;

    protected int biomeColor;
    protected int grassColor;
    protected boolean grassColorIsMultiplier;
    protected int foliageColor;    
    protected int skyColor;
    protected int waterColor;
    protected int fogColor;

    // TODO: rename this setting
	protected boolean disableNotchHeightControl;
    
    protected String replaceToBiomeName;
    
    protected List<ConfigFunction<IBiomeConfig>> resourceSequence = new ArrayList<ConfigFunction<IBiomeConfig>>();
    
    // Structures
    protected boolean woodLandMansionsEnabled;
    protected boolean netherFortressesEnabled;
    protected VillageType villageType;
    protected RareBuildingType rareBuildingType;
    protected MineshaftType mineshaftType = MineshaftType.normal;	
	protected boolean buriedTreasureEnabled;
	protected boolean oceanRuinsColdEnabled;
	protected boolean oceanRuinsWarmEnabled;
	protected boolean shipWreckEnabled;
	protected boolean shipWreckBeachedEnabled;
	protected boolean pillagerOutpostEnabled;
	protected boolean bastionRemnantEnabled;
	protected boolean netherFossilEnabled;
	protected boolean endCityEnabled;
	protected boolean ruinedPortalEnabled;
	protected boolean ruinedPortalDesertEnabled;
	protected boolean ruinedPortalJungleEnabled;
	protected boolean ruinedPortalSwampEnabled;
	protected boolean ruinedPortalMountainEnabled;
	protected boolean ruinedPortalOceanEnabled;
	protected boolean ruinedPortalNetherEnabled;

    protected List<WeightedMobSpawnGroup> spawnMonstersMerged = new ArrayList<WeightedMobSpawnGroup>();
	protected List<WeightedMobSpawnGroup> spawnCreaturesMerged = new ArrayList<WeightedMobSpawnGroup>();
	protected List<WeightedMobSpawnGroup> spawnWaterCreaturesMerged = new ArrayList<WeightedMobSpawnGroup>();
	protected List<WeightedMobSpawnGroup> spawnAmbientCreaturesMerged = new ArrayList<WeightedMobSpawnGroup>();
    
    protected BiomeConfigBase(String configName, BiomeResourceLocation registryKey)
    {
		super(configName);
		this.registryKey = registryKey;
	}
    
    @Override
	public BiomeResourceLocation getRegistryKey()
	{
		return this.registryKey;
	}   
	
    // Materials
        
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
	
    // Any blocks spawned/checked during base terrain gen that use the biomeconfig materials
    // call getXXXBlockReplaced to get the replaced blocks.
    // Any blocks spawned during population will have their materials parsed before spawning them
    // via world.setBlock(), so they use the default biomeconfig materials.
	
	@Override
	public LocalMaterialData getDefaultGroundBlock()
	{
		return this.groundBlock;
	}
	
	@Override
	public LocalMaterialData getSurfaceBlockReplaced(int y)
	{
		if(this.replacedBlocks.replacesSurface)
		{
			return this.surfaceBlock.parseWithBiomeAndHeight(this.worldConfig.getBiomeConfigsHaveReplacement(), this.replacedBlocks, y);
		}
		return this.surfaceBlock;
	}
	
	@Override
	public LocalMaterialData getGroundBlockReplaced(int y)
	{
		if(this.replacedBlocks.replacesGround)
		{
			return this.groundBlock.parseWithBiomeAndHeight(this.worldConfig.getBiomeConfigsHaveReplacement(), this.replacedBlocks, y);
		}
		return this.groundBlock;
	}
	
	@Override
	public LocalMaterialData getStoneBlockReplaced(int y)
	{
		if(this.replacedBlocks.replacesStone)
		{
			return this.stoneBlock.parseWithBiomeAndHeight(this.worldConfig.getBiomeConfigsHaveReplacement(), this.replacedBlocks, y);
		}
		return this.stoneBlock;
	}

	@Override
	public LocalMaterialData getBedrockBlockReplaced(int y)
	{
		return this.worldConfig.getBedrockBlockReplaced(this.replacedBlocks, y);
	}
		
	@Override
	public LocalMaterialData getWaterBlockReplaced(int y)
	{
		if(this.replacedBlocks.replacesWater)
		{
			return this.waterBlock.parseWithBiomeAndHeight(this.worldConfig.getBiomeConfigsHaveReplacement(), this.replacedBlocks, y);
		}
		return this.waterBlock;
	}
	
	@Override
	public LocalMaterialData getSandStoneBlockReplaced(int y)
	{
		if(this.replacedBlocks.replacesSandStone)
		{
			return this.sandStoneBlock.parseWithBiomeAndHeight(this.worldConfig.getBiomeConfigsHaveReplacement(), this.replacedBlocks, y);
		}
		return this.sandStoneBlock;
	}
	
	@Override
	public LocalMaterialData getIceBlockReplaced(int y)
	{
		if(this.replacedBlocks.replacesIce)
		{
			return this.iceBlock.parseWithBiomeAndHeight(this.worldConfig.getBiomeConfigsHaveReplacement(), this.replacedBlocks, y);
		}
		return this.iceBlock;
	}
	
	@Override
	public LocalMaterialData getCooledLavaBlockReplaced(int y)
	{
		if(this.replacedBlocks.replacesCooledLava)
		{
			return this.cooledLavaBlock.parseWithBiomeAndHeight(this.worldConfig.getBiomeConfigsHaveReplacement(), this.replacedBlocks, y);
		}
		return this.cooledLavaBlock;
	}
	
	@Override
	public ReplacedBlocksMatrix getReplaceBlocks()
	{
		return this.replacedBlocks;
	}

	@Override
	public List<List<String>> getCustomStructureNames()
	{
		List<List<String>> customStructureNamesByGen = new ArrayList<>();
		for(CustomStructureGen structureGens : this.customStructures)
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
	public String getBiomeExtends()
	{
		return this.biomeExtends;
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
	public int getWaterColor()
	{
		return this.waterColor;
	}

	@Override
	public boolean getGrassColorIsMultiplier()
	{
		return this.grassColorIsMultiplier;
	}
	
	// Structures
	
	@Override
	public MineshaftType getMineShaftType()
	{
		return this.mineshaftType;
	}
	
	@Override
	public boolean getNetherFortressesEnabled()
	{
		return this.netherFortressesEnabled;
	}
	
	@Override
	public RareBuildingType getRareBuildingType()
	{
		return this.rareBuildingType;
	}
	
	@Override
	public VillageType getVillageType()
	{
		return this.villageType;
	}
	
	@Override
	public boolean getWoodlandMansionsEnabled()
	{
		return this.woodLandMansionsEnabled;
	}
	
	@Override
	public boolean getBuriedTreasureEnabled()
	{
		return this.buriedTreasureEnabled;
	}

	@Override
	public boolean getOceanRuinsColdEnabled()
	{
		return this.oceanRuinsColdEnabled;
	}

	@Override
	public boolean getOceanRuinsWarmEnabled()
	{
		return this.oceanRuinsWarmEnabled;
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
	public boolean getPillagerOutpostEnabled()
	{
		return this.pillagerOutpostEnabled;
	}

	@Override
	public boolean getBastionRemnantEnabled()
	{
		return this.bastionRemnantEnabled;
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
	public boolean getRuinedPortalEnabled()
	{
		return this.ruinedPortalEnabled;
	}

	@Override
	public boolean getRuinedPortalDesertEnabled()
	{
		return this.ruinedPortalDesertEnabled;
	}

	@Override
	public boolean getRuinedPortalJungleEnabled()
	{
		return this.ruinedPortalJungleEnabled;
	}

	@Override
	public boolean getRuinedPortalSwampEnabled()
	{
		return this.ruinedPortalSwampEnabled;
	}

	@Override
	public boolean getRuinedPortalMountainEnabled()
	{
		return this.ruinedPortalMountainEnabled;
	}

	@Override
	public boolean getRuinedPortalOceanEnabled()
	{
		return this.ruinedPortalOceanEnabled;
	}

	@Override
	public boolean getRuinedPortalNetherEnabled()
	{
		return this.ruinedPortalNetherEnabled;
	}
	
	//
	
	@Override
	public String getReplaceToBiomeName()
	{
		return this.replaceToBiomeName;
	}
	
	@Override
	public void setReplaceToBiomeName(String replaceToBiomeName)
	{
		this.replaceToBiomeName = replaceToBiomeName;
	}

	public List<ConfigFunction<IBiomeConfig>> getResourceSequence()
	{
		return this.resourceSequence;
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
	public boolean disableNotchHeightControl()
	{
		return this.disableNotchHeightControl;
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
    
	//
	
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
	public void doSurfaceAndGroundControl(long worldSeed, GeneratingChunk generatingChunk, ChunkBuffer chunkBuffer, IBiomeConfig biomeConfig, int x, int z)
	{
		this.surfaceAndGroundControl.spawn(worldSeed, generatingChunk, chunkBuffer, biomeConfig, x, z);
	}
	
    public SaplingGen getSaplingGen(SaplingType type)
    {
        SaplingGen gen = saplingGrowers.get(type);
        if (gen == null && type.growsTree())
        {
            gen = saplingGrowers.get(SaplingType.All);
        }
        return gen;
    }

    public SaplingGen getCustomSaplingGen(LocalMaterialData materialData, boolean wideTrunk)
    {
    	// TODO: Re-implement this when block data works
        if (wideTrunk)
        {
        	return customBigSaplingGrowers.get(materialData);
            //return customBigSaplingGrowers.get(materialData.withBlockData(materialData.getBlockData() % 8));
        }
        return customSaplingGrowers.get(materialData);
        //return customSaplingGrowers.get(materialData.withBlockData(materialData.getBlockData() % 8));
    }
}
