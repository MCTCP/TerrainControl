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
	
    // Surface config
    protected float biomeHeight;
    protected float biomeVolatility;
    protected int smoothRadius;
    
    protected float biomeTemperature;

    protected int biomeRarity;
    protected int biomeSize;
    
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
    
    protected String replaceToBiomeName;
    
    protected List<ConfigFunction<IBiomeConfig>> resourceSequence = new ArrayList<ConfigFunction<IBiomeConfig>>();
    
    // Structures
    protected boolean woodLandMansionsEnabled;
    protected boolean netherFortressesEnabled;
    protected VillageType villageType;
    protected RareBuildingType rareBuildingType;
    protected MineshaftType mineshaftType = MineshaftType.normal;
	
    protected List<WeightedMobSpawnGroup> spawnMonstersMerged = new ArrayList<WeightedMobSpawnGroup>();
	protected List<WeightedMobSpawnGroup> spawnCreaturesMerged = new ArrayList<WeightedMobSpawnGroup>();
	protected List<WeightedMobSpawnGroup> spawnWaterCreaturesMerged = new ArrayList<WeightedMobSpawnGroup>();
	protected List<WeightedMobSpawnGroup> spawnAmbientCreaturesMerged = new ArrayList<WeightedMobSpawnGroup>();
    
    protected BiomeConfigBase(String configName, BiomeResourceLocation registryKey)
    {
		super(configName);
		this.registryKey = registryKey;
	}
    
	public BiomeResourceLocation getRegistryKey()
	{
		return this.registryKey;
	}   
	
    // Materials
        
	public LocalMaterialData getSurfaceBlockAtHeight(IWorldGenRegion worldGenRegion, int x, int y, int z)
	{
		return this.surfaceAndGroundControl.getSurfaceBlockAtHeight(worldGenRegion, this, x, y, z);
	}
	
	public LocalMaterialData getGroundBlockAtHeight(IWorldGenRegion worldGenRegion, int x, int y, int z)
	{
		return this.surfaceAndGroundControl.getGroundBlockAtHeight(worldGenRegion, this, x, y, z);
	}
	
    // Any blocks spawned/checked during base terrain gen that use the biomeconfig materials
    // call getXXXBlockReplaced to get the replaced blocks.
    // Any blocks spawned during population will have their materials parsed before spawning them
    // via world.setBlock(), so they use the default biomeconfig materials.
	
	public LocalMaterialData getDefaultGroundBlock()
	{
		return this.groundBlock;
	}

    public LocalMaterialData getDefaultSurfaceBlock()
    {
    	return this.surfaceBlock;
    }
    
	// TODO: Optimise BO4, make it use replacedBlocks.replacesStoneBlock
	// instead of replacing stone as a generic block during setBlock.
	public LocalMaterialData getDefaultStoneBlock()
	{
		return this.stoneBlock;
	}
	
	public LocalMaterialData getDefaultWaterBlock()
	{		
		return this.waterBlock;
	}
	
	// TODO: Optimise FrozenSurfaceHelper, make it use replacedBlocks.replacesIce
	// instead of replacing ice as a generic block during setBlock.
	public LocalMaterialData getDefaultIceBlock()
	{
		return this.iceBlock;
	}

	// TODO: Optimise FrozenSurfaceHelper, make it use replacedBlocks.replacesCooledLavaBlock
	// instead of replacing lava as a generic block during setBlock.	
	public LocalMaterialData getDefaultCooledLavaBlock()
	{
		return this.cooledLavaBlock;
	}    
	
	public LocalMaterialData getSurfaceBlockReplaced(int y)
	{
		if(this.replacedBlocks.replacesSurface)
		{
			return this.surfaceBlock.parseWithBiomeAndHeight(this.worldConfig.getBiomeConfigsHaveReplacement(), this.replacedBlocks, y);
		}
		return this.surfaceBlock;
	}
	
	public LocalMaterialData getGroundBlockReplaced(int y)
	{
		if(this.replacedBlocks.replacesGround)
		{
			return this.groundBlock.parseWithBiomeAndHeight(this.worldConfig.getBiomeConfigsHaveReplacement(), this.replacedBlocks, y);
		}
		return this.groundBlock;
	}
	
	public LocalMaterialData getStoneBlockReplaced(int y)
	{
		if(this.replacedBlocks.replacesStone)
		{
			return this.stoneBlock.parseWithBiomeAndHeight(this.worldConfig.getBiomeConfigsHaveReplacement(), this.replacedBlocks, y);
		}
		return this.stoneBlock;
	}

	public LocalMaterialData getBedrockBlockReplaced(int y)
	{
		return this.worldConfig.getBedrockBlockReplaced(this.replacedBlocks, y);
	}
		
	public LocalMaterialData getWaterBlockReplaced(int y)
	{
		if(this.replacedBlocks.replacesWater)
		{
			return this.waterBlock.parseWithBiomeAndHeight(this.worldConfig.getBiomeConfigsHaveReplacement(), this.replacedBlocks, y);
		}
		return this.waterBlock;
	}
	
	public LocalMaterialData getSandStoneBlockReplaced(int y)
	{
		if(this.replacedBlocks.replacesSandStone)
		{
			return this.sandStoneBlock.parseWithBiomeAndHeight(this.worldConfig.getBiomeConfigsHaveReplacement(), this.replacedBlocks, y);
		}
		return this.sandStoneBlock;
	}
	
	public LocalMaterialData getIceBlockReplaced(int y)
	{
		if(this.replacedBlocks.replacesIce)
		{
			return this.iceBlock.parseWithBiomeAndHeight(this.worldConfig.getBiomeConfigsHaveReplacement(), this.replacedBlocks, y);
		}
		return this.iceBlock;
	}
	
	public LocalMaterialData getCooledLavaBlockReplaced(int y)
	{
		if(this.replacedBlocks.replacesCooledLava)
		{
			return this.cooledLavaBlock.parseWithBiomeAndHeight(this.worldConfig.getBiomeConfigsHaveReplacement(), this.replacedBlocks, y);
		}
		return this.cooledLavaBlock;
	}
			
	public LocalMaterialData getRedSandStoneBlockReplaced(int y)
	{
		if(this.replacedBlocks.replacesRedSandStone)
		{
			return this.redSandStoneBlock.parseWithBiomeAndHeight(this.worldConfig.getBiomeConfigsHaveReplacement(), this.replacedBlocks, y);
		}
		return this.redSandStoneBlock;
	}
	
	public boolean replacesDefaultWaterBlock()
	{
		return this.replacedBlocks.replacesWater;
	}
	
	public boolean replacesDefaultStoneBlock()
	{
		return this.replacedBlocks.replacesStone;
	}
	
	public ReplacedBlocksMatrix getReplaceBlocks()
	{
		return this.replacedBlocks;
	}

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
	
	public List<ICustomStructureGen> getCustomStructures()
	{
		return new ArrayList<ICustomStructureGen>(this.customStructures);
	}
	
	public ICustomStructureGen getStructureGen()
	{
		return this.structureGen;
	}
	
	public void setStructureGen(ICustomStructureGen customStructureGen)
	{
		this.structureGen = customStructureGen;
	}
	
	public String getName()
	{
		return this.configName;
	}
			
	public float getBiomeTemperature()
	{
		return this.biomeTemperature;
	}
	
	public float getBiomeHeight()
	{
		return this.biomeHeight;
	}
	
	public float getBiomeVolatility()
	{
		return this.biomeVolatility;
	}
	
	public double getVolatility1()
	{
		return this.volatility1;
	}
	
	public double getVolatility2()
	{
		return this.volatility2;
	}
	
	public int getBiomeColor()
	{
		return this.biomeColor;
	}
	
	public String getBiomeExtends()
	{
		return this.biomeExtends;
	}
	
	public float getBiomeWetness()
	{
		return this.biomeWetness;
	}
	
	public int getCHCSmoothRadius()
	{
		return this.CHCSmoothRadius;
	}
	
	public int getSkyColor()
	{
		return this.skyColor;
	}
	
	public int getFogColor()
	{
		return this.fogColor;
	}

	public int getFoliageColor()
	{
		return this.foliageColor;
	}
	
	public int getGrassColor()
	{
		return this.grassColor;
	}
	
	public int getWaterColor()
	{
		return this.waterColor;
	}

	public boolean getGrassColorIsMultiplier()
	{
		return this.grassColorIsMultiplier;
	}
	
	public MineshaftType getMineShaftType()
	{
		return this.mineshaftType;
	}
	
	public boolean getNetherFortressesEnabled()
	{
		return this.netherFortressesEnabled;
	}
	
	public RareBuildingType getRareBuildingType()
	{
		return this.rareBuildingType;
	}
	
	public VillageType getVillageType()
	{
		return this.villageType;
	}
	
	public boolean getWoodlandMansionsEnabled()
	{
		return this.woodLandMansionsEnabled;
	}
	
	public String getReplaceToBiomeName()
	{
		return this.replaceToBiomeName;
	}
	
	public void setReplaceToBiomeName(String replaceToBiomeName)
	{
		this.replaceToBiomeName = replaceToBiomeName;
	}

	public List<ConfigFunction<IBiomeConfig>> getResourceSequence()
	{
		return this.resourceSequence;
	}
	
	public List<WeightedMobSpawnGroup> getAmbientCreatures()
	{
		return this.spawnAmbientCreaturesMerged;
	}
	
	public List<WeightedMobSpawnGroup> getCreatures()
	{
		return this.spawnCreaturesMerged;
	}
	
	public List<WeightedMobSpawnGroup> getMonsters()
	{
		return this.spawnMonstersMerged;
	}
	
	public List<WeightedMobSpawnGroup> getWaterCreatures()
	{
		return this.spawnWaterCreaturesMerged;
	}
	
	public int getBiomeRarity()
	{
		return this.biomeRarity;
	}
	
	public int getBiomeSize()
	{
		return this.biomeSize;
	}
	
	public double getVolatilityWeight1()
	{
		return this.volatilityWeight1;
	}
	
	public double getVolatilityWeight2()
	{
		return this.volatilityWeight2;
	}
	
	public double getMaxAverageDepth()
	{
		return this.maxAverageDepth;
	}
	
	public double getMaxAverageHeight()
	{
		return this.maxAverageHeight;
	}
	
	public double getCHCData(int y)
	{
		return this.chcData[y];
	}
	
	public int getSmoothRadius()
	{
		return this.smoothRadius;
	}
	
	public int getWaterLevelMax()
	{
		return this.waterLevelMax;
	}
	
	public int getWaterLevelMin()
	{
		return this.waterLevelMin;
	}
	
	public boolean biomeConfigsHaveReplacement()
	{
		return this.worldConfig.getBiomeConfigsHaveReplacement();
	}
	
	public double getFractureHorizontal()
	{
		return this.worldConfig.getFractureHorizontal();
	}
	
	public double getFractureVertical()
	{
		return this.worldConfig.getFractureVertical();
	}
	
	public boolean isFlatBedrock()
	{
		return this.worldConfig.getIsFlatBedrock();
	}
	
	public boolean isCeilingBedrock()
	{
		return this.worldConfig.getIsCeilingBedrock();
	}
	
	public boolean isBedrockDisabled()
	{
		return this.worldConfig.getBedrockDisabled();
	}

	public boolean isRemoveSurfaceStone()
	{
		return this.worldConfig.getRemoveSurfaceStone();
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
