package com.pg85.otg.config.world;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.pg85.otg.OTG;
import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.biome.BiomeGroup;
import com.pg85.otg.config.biome.BiomeGroupManager;
import com.pg85.otg.config.fallbacks.BlockFallback;
import com.pg85.otg.config.io.FileSettingsReader;
import com.pg85.otg.config.io.IConfigFunctionProvider;
import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.io.SimpleSettingsMap;
import com.pg85.otg.config.minecraft.DefaultBiome;
import com.pg85.otg.config.settingType.Setting;
import com.pg85.otg.config.standard.BiomeStandardValues;
import com.pg85.otg.config.standard.WorldStandardValues;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums.ImageMode;
import com.pg85.otg.constants.SettingsEnums.ImageOrientation;
import com.pg85.otg.constants.SettingsEnums.TerrainMode;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.biome.BiomeResourceLocation;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldConfig;

//TODO: Clean this up further, atm WorldConfigBase implements
//anything needed for IWorldConfig, which describes any
//methods used by other projects. WorldConfig only contains
//fields/methods used for io/serialisation/instantiation.
public class WorldConfig extends WorldConfigBase
{
    public static final HashMap<String, Class<? extends ConfigFunction<?>>> CONFIG_FUNCTIONS = new HashMap<>();
    static
    {
    	CONFIG_FUNCTIONS.put("BiomeGroup", BiomeGroup.class);
    	CONFIG_FUNCTIONS.put("BlockFallback", BlockFallback.class);
    }
	
	// TODO: Clean these fields up, move to WorldConfigBase if
	// they need to be exposed, or remove if no longer used.

    // Biome Groups and special biome lists
    public BiomeGroupManager biomeGroupManager;

	private List<String> isleBiomes = new ArrayList<String>();
	private List<String> borderBiomes = new ArrayList<String>();

	private boolean improvedBiomeBorders;
	private boolean improvedBiomeGroups;
	private boolean customHeightControlSmoothing;
    private boolean improvedSmoothing;

    // Dimensions

	// Replace blocks
    //private FallbackConfig fallbacks;
    //private Map<String, LocalMaterialData> fallbackCache = new HashMap<String, LocalMaterialData>();
   
    // For old biome generator

    private double minMoisture;
    private double maxMoisture;
    private double minTemperature;
    private double maxTemperature;

    // Biome generator
    
    private int landRarity;
    private int landSize;
    private int landFuzzy;    

    private boolean frozenOcean;
    private boolean freezeAllColdGroupBiomes;
    private double frozenOceanTemperature;

	private String defaultFrozenOceanBiome;

    // Rivers

	private int riverRarity;
	private int riverSize;
	private boolean riversEnabled;
	private boolean improvedRivers;
	private boolean randomRivers;

    // Biome image

	private String imageFile;
	private ImageOrientation imageOrientation;
	private ImageMode imageMode;
    // public int imageZoom;
	private String imageFillBiome;
    private int imageXOffset;
    private int imageZOffset;

    private HashMap<Integer, BiomeResourceLocation> biomeColorMap;

    // Look settings
    private int worldFog;
    // TODO: Implement this?
    private float worldFogR;
    private float worldFogG;
    private float worldFogB;

    private int worldNightFog;
    // TODO: Implement this?
    private float worldNightFogR;
    private float worldNightFogG;
    private float worldNightFogB;

    // Specific biome settings

    // Strongholds
    private double strongholdDistance;
    private int strongholdCount;
    private int strongholdSpread;

    // Villages
    private int villageSize;
    private int villageDistance; // Has a minimum of 9

    // Pyramids (also swamp huts and jungle temples)
    private int minimumDistanceBetweenRareBuildings; // Minecraft's internal
    // value is 1 chunk lower
    private int maximumDistanceBetweenRareBuildings;

    // Ocean monuments
    private int oceanMonumentGridSize;
    private int oceanMonumentRandomOffset;

    // Other structures
    private boolean netherFortressesEnabled;
    private boolean woodLandMansionsEnabled;

    // Terrain
    
    private boolean populateUsingSavedBiomes;

    private int objectSpawnRatio;
    private TerrainMode modeTerrain;
    //public Class<? extends BiomeGenerator> biomeMode;    

    private int worldHeightScaleBits;
    private int worldHeightCapBits;    
    
    // Settings for console commands
   
    private String bo3AtSpawn;

	//

    private static class DefaulWorldData
    {
    	private WorldConfig worldConfig;
    	private SettingsMap settingsMap;
    	
    	private DefaulWorldData(WorldConfig worldConfig, SettingsMap settingsMap)
    	{
    		this.worldConfig = worldConfig;
    		this.settingsMap = settingsMap;
    	}
    }
        
    public WorldConfig(Path settingsDir, SettingsMap settingsReader, ArrayList<String> biomes, IConfigFunctionProvider biomeResourcesManager, boolean spawnLog, ILogger logger, IMaterialReader materialReader)
    {
        super(settingsReader.getName());

        if(biomes != null) // Can be null when loading the world config for the Forge world creation menu
        {
	        for(String biome : biomes)
	        {
	        	// TODO: Retrieve any already existing id's for the biomes
	        	worldBiomes.add(biome);
	        }
        }
        
        // Fix older names
        this.renameOldSettings(settingsReader, logger, materialReader);
        // Set the local fields based on what was read from the file
        this.readConfigSettings(settingsReader, biomeResourcesManager, spawnLog, logger, materialReader);
        // Clamp Settings to acceptable values.
       	this.correctSettings(biomes != null, OTG.getEngine().getLogger()); // If biomes is null then we're not interested in loading biomes, squelch biome warnings.        
    }
    
    public static DefaulWorldData createDefaultOTGWorldConfig(Path settingsDir, String worldName, IConfigFunctionProvider biomeResourcesManager, boolean spawnLog, ILogger logger, IMaterialReader materialReader)
    {
    	SimpleSettingsMap settingsMap = new SimpleSettingsMap(worldName, true);
    	WorldConfig defaultWorldConfig = new WorldConfig(settingsDir, settingsMap, getDefaultBiomeNames(), biomeResourcesManager, spawnLog, logger, materialReader);
    	defaultWorldConfig.writeConfigSettings(settingsMap);
    	return new DefaulWorldData(defaultWorldConfig, settingsMap);
    }

    private static ArrayList<String> getDefaultBiomeNames()
    {
        // If we're creating a new world with new configs then add the default biomes
        ArrayList<String> defaultBiomes = new ArrayList<String>();
        for (DefaultBiome defaultBiome : DefaultBiome.values())
        {
        	defaultBiomes.add(defaultBiome.Name);
        }
        return defaultBiomes;
    }
    
    /*
    public void addWorldFallbacks(FallbackConfig config)
    {
        this.fallbacks = config;        
    }    

    public LocalMaterialData parseFallback(String raw)
    {
        LocalMaterialData material = fallbackCache.get(raw);

        if (material != null)
        {
            return material;
        }

        for (BlockFallback fallback : fallbacks.getAllFallbacks())
        {
            if (fallback.materialFrom.equals(raw))
            {
                for (String replacement : fallback.materialsTo)
                {
                    try
                    {
                        material = OTG.getEngine().readMaterial(replacement);
                    }
                    catch (InvalidConfigException e)
                    {
                    	if(OTG.getPluginConfig().spawnLog)
                    	{
                    		OTG.log(LogMarker.WARN, "Fallback material could not be parsed: " + replacement);
                    	}
                    }
                    if (material != null)
					{
                        fallbackCache.put(raw, material);
                        return material;
                    }
                }
            }
        }
        return null;
    }
    */
    
    @Override
    protected void renameOldSettings(SettingsMap reader, ILogger logger, IMaterialReader materialReader)
    {
        reader.renameOldSetting("WaterLevel", WorldStandardValues.WATER_LEVEL_MAX);
        reader.renameOldSetting("ModeTerrain", WorldStandardValues.TERRAIN_MODE);
        reader.renameOldSetting("ModeBiome", WorldStandardValues.BIOME_MODE);
        reader.renameOldSetting("NetherFortressEnabled", WorldStandardValues.NETHER_FORTRESSES_ENABLED);
        reader.renameOldSetting("PyramidsEnabled", WorldStandardValues.RARE_BUILDINGS_ENABLED);
        // WorldHeightBits was split into two different settings
        reader.renameOldSetting("WorldHeightBits", WorldStandardValues.WORLD_HEIGHT_SCALE_BITS);
        reader.renameOldSetting("WorldHeightBits", WorldStandardValues.WORLD_HEIGHT_CAP_BITS);

        // Put BiomeMode in compatibility mode when the old setting
        // NormalBiomes is found, and create default groups
        if (reader.hasSetting(WorldStandardValues.NORMAL_BIOMES))
        {
            if (reader.getSetting(WorldStandardValues.BIOME_MODE, logger, null).equals("Normal"))
            {
                reader.putSetting(WorldStandardValues.BIOME_MODE, "BeforeGroups");
            }
            
            int landSize = reader.getSetting(WorldStandardValues.LAND_SIZE, logger, null);
            int landRarity = reader.getSetting(WorldStandardValues.LAND_RARITY, logger, null);
            List<String> normalBiomes = reader.getSetting(WorldStandardValues.NORMAL_BIOMES, logger, null);

            BiomeGroup normalGroup = new BiomeGroup(this, WorldStandardValues.BiomeGroupNames.NORMAL, landSize, landRarity, normalBiomes);

            int iceSize = reader.getSetting(WorldStandardValues.ICE_SIZE, logger, null);
            int iceRarity = reader.getSetting(WorldStandardValues.ICE_RARITY, logger, null);
            List<String> iceBiomes = reader.getSetting(WorldStandardValues.ICE_BIOMES, logger, null);
            BiomeGroup iceGroup = new BiomeGroup(this, WorldStandardValues.BiomeGroupNames.ICE, iceSize, iceRarity, iceBiomes);

            reader.addConfigFunctions(Arrays.asList(normalGroup, iceGroup));
        }

        // Migrate bounds
        if (reader.hasSetting(WorldStandardValues.CANYON_DEPTH))
        {
            reader.renameOldSetting("CanyonDepth", WorldStandardValues.RAVINE_DEPTH);
            reader.renameOldSetting("CanyonRarity", WorldStandardValues.RAVINE_RARITY);
            reader.renameOldSetting("CanyonMinAltitude", WorldStandardValues.RAVINE_MIN_ALTITUDE);
            reader.renameOldSetting("CanyonMaxAltitude", WorldStandardValues.RAVINE_MAX_ALTITUDE);
            reader.renameOldSetting("CanyonMinLength", WorldStandardValues.RAVINE_MIN_LENGTH);
            reader.renameOldSetting("CanyonMaxLength", WorldStandardValues.RAVINE_MAX_LENGTH);

            decrementByOne(reader, WorldStandardValues.CAVE_MAX_ALTITUDE, logger, materialReader);
            decrementByOne(reader, WorldStandardValues.CAVE_SYSTEM_POCKET_MAX_SIZE, logger, materialReader);
            decrementByOne(reader, WorldStandardValues.RAVINE_MAX_ALTITUDE, logger, materialReader);
            decrementByOne(reader, WorldStandardValues.RAVINE_MAX_LENGTH, logger, materialReader);
        }
    }

    private void decrementByOne(SettingsMap reader, Setting<Integer> setting, ILogger logger, IMaterialReader materialReader)
    {
        if (reader.hasSetting(setting))
        {
            reader.putSetting(setting, reader.getSetting(setting, logger, materialReader) - 1);
        }
    }

    @Override
    protected void correctSettings(boolean logWarnings, ILogger logger)
    {
        landSize = lowerThanOrEqualTo(landSize, generationDepth);
        landFuzzy = lowerThanOrEqualTo(landFuzzy, generationDepth - landSize);

        riverRarity = lowerThanOrEqualTo(riverRarity, generationDepth);
        riverSize = lowerThanOrEqualTo(riverSize, generationDepth - riverRarity);

        biomeGroupManager.filterBiomes(worldBiomes, logWarnings, logger);
        isleBiomes = filterBiomes(isleBiomes, worldBiomes);
        borderBiomes = filterBiomes(borderBiomes, worldBiomes);

        /*
        if (biomeMode == OTG.getBiomeModeManager().FROM_IMAGE)
        {
            File mapFile = new File(settingsDir.toString(), imageFile);
            if (!mapFile.exists())
            {
                OTG.log(LogMarker.WARN, "Biome map file not found. Switching BiomeMode to Normal");
                biomeMode = OTG.getBiomeModeManager().NORMAL;
            }
        }
        */

        imageFillBiome = (DefaultBiome.Contain(imageFillBiome) || worldBiomes.contains(imageFillBiome)) ? imageFillBiome : WorldStandardValues.IMAGE_FILL_BIOME.getDefaultValue(null);

        maxMoisture = higherThan(maxMoisture, minMoisture);
        maxTemperature = higherThan(maxTemperature, minTemperature);

        caveMaxAltitude = higherThanOrEqualTo(caveMaxAltitude, caveMinAltitude);
        caveSystemPocketMaxSize = higherThanOrEqualTo(caveSystemPocketMaxSize, caveSystemPocketMinSize);
        ravineMaxAltitude = higherThanOrEqualTo(ravineMaxAltitude, ravineMinAltitude);
        ravineMaxLength = higherThanOrEqualTo(ravineMaxLength, ravineMinLength);

        waterLevelMax = higherThanOrEqualTo(waterLevelMax, waterLevelMin);

        maximumDistanceBetweenRareBuildings = higherThanOrEqualTo(maximumDistanceBetweenRareBuildings, minimumDistanceBetweenRareBuildings);
        oceanMonumentRandomOffset = lowerThanOrEqualTo(oceanMonumentRandomOffset, oceanMonumentGridSize);
    }

    @Override
    protected void readConfigSettings(SettingsMap reader, IConfigFunctionProvider biomeResourcesManager, boolean spawnLog, ILogger logger, IMaterialReader materialReader)
    {
    	// OTG+
    	this.isOTGPlus = reader.getSetting(WorldStandardValues.IS_OTG_PLUS, logger, null);
    	this.replaceBlocksList = reader.getSetting(WorldStandardValues.REPLACE_BLOCKS_LIST, logger, null);
    	//

        // Main modes
        this.settingsMode = reader.getSetting(WorldStandardValues.SETTINGS_MODE, logger, null);
        this.modeTerrain = reader.getSetting(WorldStandardValues.TERRAIN_MODE, logger, null);
        //this.biomeMode = OTG.getBiomeModeManager().getBiomeManager(reader.getSetting(WorldStandardValues.BIOME_MODE));

        // World and water height
        this.worldHeightCapBits = reader.getSetting(WorldStandardValues.WORLD_HEIGHT_CAP_BITS, logger, null);
        this.worldHeightCap = 1 << this.worldHeightCapBits;
        this.worldHeightScaleBits = reader.getSetting(WorldStandardValues.WORLD_HEIGHT_SCALE_BITS, logger, null);
        this.worldHeightScaleBits = lowerThanOrEqualTo(this.worldHeightScaleBits, this.worldHeightCapBits);
        this.worldHeightScale = 1 << this.worldHeightScaleBits;
        this.waterLevelMax = worldHeightCap / 2 - 1;

        // Biome placement
        this.generationDepth = reader.getSetting(WorldStandardValues.GENERATION_DEPTH, logger, null);

        this.biomeRarityScale = reader.getSetting(WorldStandardValues.BIOME_RARITY_SCALE, logger, null);
        this.landRarity = reader.getSetting(WorldStandardValues.LAND_RARITY, logger, null);
        this.landSize = reader.getSetting(WorldStandardValues.LAND_SIZE, logger, null);
        this.landFuzzy = reader.getSetting(WorldStandardValues.LAND_FUZZY, logger, null);

    	this.defaultOceanBiome = reader.getSetting(WorldStandardValues.DEFAULT_OCEAN_BIOME, logger, null);
        
        // Ice Area Settings
        this.frozenOcean = reader.getSetting(WorldStandardValues.FROZEN_OCEAN, logger, null);
        this.frozenOceanTemperature = reader.getSetting(WorldStandardValues.FROZEN_OCEAN_TEMPERATURE, logger, null);
        this.freezeAllColdGroupBiomes = reader.getSetting(WorldStandardValues.GROUP_FREEZE_ENABLED, logger, null);

    	this.defaultFrozenOceanBiome = reader.getSetting(WorldStandardValues.DEFAULT_FROZEN_OCEAN_BIOME, logger, null);
    	
        // Freeze & Snow Settings
    	this.betterSnowFall = reader.getSetting(WorldStandardValues.BETTER_SNOW_FALL, logger, null);
        this.fullyFreezeLakes = reader.getSetting(WorldStandardValues.FULLY_FREEZE_LAKES, logger, null);

        // Rivers
        this.riverRarity = reader.getSetting(WorldStandardValues.RIVER_RARITY, logger, null);
        this.riverSize = reader.getSetting(WorldStandardValues.RIVER_SIZE, logger, null);
        this.riversEnabled = reader.getSetting(WorldStandardValues.RIVERS_ENABLED, logger, null);
        this.improvedRivers = reader.getSetting(WorldStandardValues.IMPROVED_RIVERS, logger, null);
        this.randomRivers = reader.getSetting(WorldStandardValues.RANDOM_RIVERS, logger, null);
        
		// Biome Groups
		readBiomeGroups(reader, biomeResourcesManager, spawnLog, logger, materialReader);
    
        // Specialized Biomes
        this.isleBiomes = reader.getSetting(WorldStandardValues.ISLE_BIOMES, logger, null);
        this.borderBiomes = reader.getSetting(WorldStandardValues.BORDER_BIOMES, logger, null);

        this.improvedBiomeBorders = reader.getSetting(WorldStandardValues.IMPROVED_BIOME_BORDERS, logger, null);
        this.improvedBiomeGroups = reader.getSetting(WorldStandardValues.IMPROVED_BIOME_GROUPS, logger, null);
        this.customHeightControlSmoothing = reader.getSetting(WorldStandardValues.CUSTOM_HEIGHT_CONTROL_SMOOTHING, logger, null);
        this.improvedSmoothing = reader.getSetting(WorldStandardValues.IMPROVED_SMOOTHING, logger, null);
        
        // Images
        this.imageMode = reader.getSetting(WorldStandardValues.IMAGE_MODE, logger, null);
        this.imageFile = reader.getSetting(WorldStandardValues.IMAGE_FILE, logger, null);
        this.imageOrientation = reader.getSetting(WorldStandardValues.IMAGE_ORIENTATION, logger, null);
        this.imageFillBiome = reader.getSetting(WorldStandardValues.IMAGE_FILL_BIOME, logger, null);
        this.imageXOffset = reader.getSetting(WorldStandardValues.IMAGE_X_OFFSET, logger, null);
        this.imageZOffset = reader.getSetting(WorldStandardValues.IMAGE_Z_OFFSET, logger, null);

        // Fog
        this.worldFog = reader.getSetting(WorldStandardValues.WORLD_FOG, logger, null);
        this.worldNightFog = reader.getSetting(WorldStandardValues.WORLD_NIGHT_FOG, logger, null);

        this.worldFogR = ((worldFog & 0xFF0000) >> 16) / 255F;
        this.worldFogG = ((worldFog & 0xFF00) >> 8) / 255F;
        this.worldFogB = (worldFog & 0xFF) / 255F;

        this.worldNightFogR = ((worldNightFog & 0xFF0000) >> 16) / 255F;
        this.worldNightFogG = ((worldNightFog & 0xFF00) >> 8) / 255F;
        this.worldNightFogB = (worldNightFog & 0xFF) / 255F;

        // Structures
        this.strongholdsEnabled = reader.getSetting(WorldStandardValues.STRONGHOLDS_ENABLED, logger, null);
        this.strongholdCount = reader.getSetting(WorldStandardValues.STRONGHOLD_COUNT, logger, null);
        this.strongholdDistance = reader.getSetting(WorldStandardValues.STRONGHOLD_DISTANCE, logger, null);
        this.strongholdSpread = reader.getSetting(WorldStandardValues.STRONGHOLD_SPREAD, logger, null);

        this.villagesEnabled = reader.getSetting(WorldStandardValues.VILLAGES_ENABLED, logger, null);
        this.villageDistance = reader.getSetting(WorldStandardValues.VILLAGE_DISTANCE, logger, null);
        this.villageSize = reader.getSetting(WorldStandardValues.VILLAGE_SIZE, logger, null);

        this.rareBuildingsEnabled = reader.getSetting(WorldStandardValues.RARE_BUILDINGS_ENABLED, logger, null);
        this.minimumDistanceBetweenRareBuildings = reader.getSetting(WorldStandardValues.MINIMUM_DISTANCE_BETWEEN_RARE_BUILDINGS, logger, null);
        this.maximumDistanceBetweenRareBuildings = reader.getSetting(WorldStandardValues.MAXIMUM_DISTANCE_BETWEEN_RARE_BUILDINGS, logger, null);

        this.woodLandMansionsEnabled = reader.getSetting(WorldStandardValues.WOODLAND_MANSIONS_ENABLED, logger, null);

        this.oceanMonumentsEnabled = reader.getSetting(WorldStandardValues.OCEAN_MONUMENTS_ENABLED, logger, null);
        this.oceanMonumentRandomOffset = reader.getSetting(WorldStandardValues.OCEAN_MONUMENT_RANDOM_OFFSET, logger, null);
        this.oceanMonumentGridSize = reader.getSetting(WorldStandardValues.OCEAN_MONUMENT_GRID_SIZE, logger, null);

        this.maximumCustomStructureRadius = reader.getSetting(WorldStandardValues.MAXIMUM_CUSTOM_STRUCTURE_RADIUS, logger, null);
        this.mineshaftsEnabled = reader.getSetting(WorldStandardValues.MINESHAFTS_ENABLED, logger, null);
        this.netherFortressesEnabled = reader.getSetting(WorldStandardValues.NETHER_FORTRESSES_ENABLED, logger, null);

        // Caves
        this.caveRarity = reader.getSetting(WorldStandardValues.CAVE_RARITY, logger, null);
        this.caveFrequency = reader.getSetting(WorldStandardValues.CAVE_FREQUENCY, logger, null);
        this.caveMinAltitude = reader.getSetting(WorldStandardValues.CAVE_MIN_ALTITUDE, logger, null);
        this.caveMaxAltitude = reader.getSetting(WorldStandardValues.CAVE_MAX_ALTITUDE, logger, null);
        this.individualCaveRarity = reader.getSetting(WorldStandardValues.INDIVIDUAL_CAVE_RARITY, logger, null);
        this.caveSystemFrequency = reader.getSetting(WorldStandardValues.CAVE_SYSTEM_FREQUENCY, logger, null);
        this.caveSystemPocketChance = reader.getSetting(WorldStandardValues.CAVE_SYSTEM_POCKET_CHANCE, logger, null);
        this.caveSystemPocketMinSize = reader.getSetting(WorldStandardValues.CAVE_SYSTEM_POCKET_MIN_SIZE, logger, null);
        this.caveSystemPocketMaxSize = reader.getSetting(WorldStandardValues.CAVE_SYSTEM_POCKET_MAX_SIZE, logger, null);
        this.evenCaveDistribution = reader.getSetting(WorldStandardValues.EVEN_CAVE_DISTRIBUTION, logger, null);

        // Ravines
        this.ravineRarity = reader.getSetting(WorldStandardValues.RAVINE_RARITY, logger, null);
        this.ravineMinAltitude = reader.getSetting(WorldStandardValues.RAVINE_MIN_ALTITUDE, logger, null);
        this.ravineMaxAltitude = reader.getSetting(WorldStandardValues.RAVINE_MAX_ALTITUDE, logger, null);
        this.ravineMinLength = reader.getSetting(WorldStandardValues.RAVINE_MIN_LENGTH, logger, null);
        this.ravineMaxLength = reader.getSetting(WorldStandardValues.RAVINE_MAX_LENGTH, logger, null);
        this.ravineDepth = reader.getSetting(WorldStandardValues.RAVINE_DEPTH, logger, null);

        // Water
        this.waterLevelMax = reader.getSetting(WorldStandardValues.WATER_LEVEL_MAX, logger, null);
        this.waterLevelMin = reader.getSetting(WorldStandardValues.WATER_LEVEL_MIN, logger, null);
        this.waterBlock = reader.getSetting(WorldStandardValues.WATER_BLOCK, logger, materialReader);
        this.iceBlock = reader.getSetting(WorldStandardValues.ICE_BLOCK, logger, materialReader);

        // Lava
        this.cooledLavaBlock = reader.getSetting(WorldStandardValues.COOLED_LAVA_BLOCK, logger, materialReader);

        // Fracture
        this.fractureHorizontal = reader.getSetting(WorldStandardValues.FRACTURE_HORIZONTAL, logger, null);
        this.fractureVertical = reader.getSetting(WorldStandardValues.FRACTURE_VERTICAL, logger, null);

        // Bedrock
        this.disableBedrock = reader.getSetting(WorldStandardValues.DISABLE_BEDROCK, logger, null);
        this.ceilingBedrock = reader.getSetting(WorldStandardValues.CEILING_BEDROCK, logger, null);
        this.flatBedrock = reader.getSetting(WorldStandardValues.FLAT_BEDROCK, logger, null);
        this.bedrockBlock = reader.getSetting(WorldStandardValues.BEDROCK_BLOCK, logger, materialReader);

        // Misc
        this.removeSurfaceStone = reader.getSetting(WorldStandardValues.REMOVE_SURFACE_STONE, logger, null);
        this.objectSpawnRatio = reader.getSetting(WorldStandardValues.OBJECT_SPAWN_RATIO, logger, null);
        this.resourcesSeed = reader.getSetting(WorldStandardValues.RESOURCES_SEED, logger, null);
        this.populationBoundsCheck = reader.getSetting(WorldStandardValues.POPULATION_BOUNDS_CHECK, logger, null);
        this.populateUsingSavedBiomes = reader.getSetting(WorldStandardValues.POPULATE_USING_SAVED_BIOMES, logger, null);
        this.disableOreGen = reader.getSetting(WorldStandardValues.DISABLE_OREGEN, logger, null);
        
        this.author = reader.getSetting(WorldStandardValues.AUTHOR, logger, null);
        this.description = reader.getSetting(WorldStandardValues.DESCRIPTION, logger, null);
        this.worldPackerModName = reader.getSetting(WorldStandardValues.WORLDPACKER_MODNAME, logger, null);
        
        this.preGenerationRadius = reader.getSetting(WorldStandardValues.PREGENERATION_RADIUS, logger, null);
        this.worldBorderRadius = reader.getSetting(WorldStandardValues.WORLD_BORDER_RADIUS, logger, null);

        this.worldSeed = reader.getSetting(WorldStandardValues.WORLD_SEED, logger, null);
        this.bo3AtSpawn = reader.getSetting(WorldStandardValues.BO3_AT_SPAWN, logger, null);
        this.dimensionPortalMaterials = reader.getSetting(WorldStandardValues.DIMENSION_PORTAL_MATERIALS, logger, materialReader);

        this.portalColor = reader.getSetting(WorldStandardValues.PORTAL_COLOR, logger, null);
        this.portalParticleType = reader.getSetting(WorldStandardValues.PORTAL_PARTICLE_TYPE, logger, null);
        this.portalMobType = reader.getSetting(WorldStandardValues.PORTAL_MOB_TYPE, logger, null);
        this.portalMobSpawnChance = reader.getSetting(WorldStandardValues.PORTAL_MOB_SPAWN_CHANCE, logger, null);

        // Dimensions
        this.dimensions = reader.getSetting(WorldStandardValues.DIMENSIONS, logger, null);

        this.dimensionBelow = reader.getSetting(WorldStandardValues.DIMENSIONBELOW, logger, null);
        this.dimensionAbove = reader.getSetting(WorldStandardValues.DIMENSIONABOVE, logger, null);

        this.dimensionBelowHeight = reader.getSetting(WorldStandardValues.DIMENSIONBELOWHEIGHT, logger, null);
        this.dimensionAboveHeight = reader.getSetting(WorldStandardValues.DIMENSIONABOVEHEIGHT, logger, null);

        if(this.dimensionBelowHeight >= this.dimensionAboveHeight)
        {
        	this.dimensionBelowHeight = WorldStandardValues.DIMENSIONBELOWHEIGHT.getDefaultValue(null);
        	this.dimensionAboveHeight = WorldStandardValues.DIMENSIONABOVEHEIGHT.getDefaultValue(null);
    		OTG.log(LogMarker.WARN, "World " + this.getName() + " WorldConfig setting dimensionBelowHeight was higher than dimensionAboveHeight, using default values instead.");
        }

        this.teleportToSpawnOnly = reader.getSetting(WorldStandardValues.TeleportToSpawnOnly, logger, null);
        this.commandBlockOutput = reader.getSetting(WorldStandardValues.CommandBlockOutput, logger, null).toString();
        this.disableElytraMovementCheck = reader.getSetting(WorldStandardValues.DisableElytraMovementCheck, logger, null).toString();
        this.doDaylightCycle = reader.getSetting(WorldStandardValues.DoDaylightCycle, logger, null).toString();
        this.doEntityDrops = reader.getSetting(WorldStandardValues.DoEntityDrops, logger, null).toString();
        this.doFireTick = reader.getSetting(WorldStandardValues.DoFireTick, logger, null).toString();
        this.doLimitedCrafting = reader.getSetting(WorldStandardValues.DoLimitedCrafting, logger, null).toString();
        this.doMobLoot = reader.getSetting(WorldStandardValues.DoMobLoot, logger, null).toString();
        this.doMobSpawning = reader.getSetting(WorldStandardValues.DoMobSpawning, logger, null).toString();
        this.doTileDrops = reader.getSetting(WorldStandardValues.DoTileDrops, logger, null).toString();
        this.doWeatherCycle = reader.getSetting(WorldStandardValues.DoWeatherCycle, logger, null).toString();
        this.gameLoopFunction = reader.getSetting(WorldStandardValues.GameLoopFunction, logger, null).toString();
        this.keepInventory = reader.getSetting(WorldStandardValues.KeepInventory, logger, null).toString();
        this.logAdminCommands = reader.getSetting(WorldStandardValues.LogAdminCommands, logger, null).toString();
        this.maxCommandChainLength = reader.getSetting(WorldStandardValues.MaxCommandChainLength, logger, null).toString();
        this.maxEntityCramming = reader.getSetting(WorldStandardValues.MaxEntityCramming, logger, null).toString();
        this.mobGriefing = reader.getSetting(WorldStandardValues.MobGriefing, logger, null).toString();
        this.naturalRegeneration = reader.getSetting(WorldStandardValues.NaturalRegeneration, logger, null).toString();
        this.randomTickSpeed = reader.getSetting(WorldStandardValues.RandomTickSpeed, logger, null).toString();
        this.reducedDebugInfo = reader.getSetting(WorldStandardValues.ReducedDebugInfo, logger, null).toString();
        this.sendCommandFeedback = reader.getSetting(WorldStandardValues.SendCommandFeedback, logger, null).toString();
        this.showDeathMessages = reader.getSetting(WorldStandardValues.ShowDeathMessages, logger, null).toString();
        this.spawnRadius = reader.getSetting(WorldStandardValues.SpawnRadius, logger, null).toString(); // "10";
        this.spectatorsGenerateChunks = reader.getSetting(WorldStandardValues.SpectatorsGenerateChunks, logger, null).toString();

        // World provider settings for Forge OTG worlds

        this.welcomeMessage = reader.getSetting(WorldStandardValues.WelcomeMessage, logger, null);
        this.departMessage = reader.getSetting(WorldStandardValues.DepartMessage, logger, null);
        this.hasSkyLight = reader.getSetting(WorldStandardValues.HasSkyLight, logger, null);
        this.isSurfaceWorld = reader.getSetting(WorldStandardValues.IsSurfaceWorld, logger, null);
        this.canRespawnHere = reader.getSetting(WorldStandardValues.CanRespawnHere, logger, null);
        this.doesWaterVaporize = reader.getSetting(WorldStandardValues.DoesWaterVaporize, logger, null);
        this.doesXZShowFog = reader.getSetting(WorldStandardValues.DoesXZShowFog, logger, null);
        this.useCustomFogColor = reader.getSetting(WorldStandardValues.UseCustomFogColor, logger, null);
        this.fogColorRed = reader.getSetting(WorldStandardValues.FogColorRed, logger, null);
        this.fogColorGreen = reader.getSetting(WorldStandardValues.FogColorGreen, logger, null);
        this.fogColorBlue = reader.getSetting(WorldStandardValues.FogColorBlue, logger, null);
        this.isSkyColored = reader.getSetting(WorldStandardValues.IsSkyColored, logger, null);
        //this.averageGroundlevel = reader.getSetting(WorldStandardValues.averageGroundlevel);
        //this.horizonHeight = reader.getSetting(WorldStandardValues.horizonHeight);
        this.cloudHeight = reader.getSetting(WorldStandardValues.CloudHeight, logger, null);
        this.canDoLightning = reader.getSetting(WorldStandardValues.CanDoLightning, logger, null);
        this.canDoRainSnowIce = reader.getSetting(WorldStandardValues.CanDoRainSnowIce, logger, null);
        this.isNightWorld = reader.getSetting(WorldStandardValues.IsNightWorld, logger, null);
        this.voidFogYFactor = reader.getSetting(WorldStandardValues.VoidFogYFactor, logger, null);
        this.gravityFactor = reader.getSetting(WorldStandardValues.GravityFactor, logger, null);
        this.shouldMapSpin = reader.getSetting(WorldStandardValues.ShouldMapSpin, logger, null);
        this.canDropChunk = reader.getSetting(WorldStandardValues.CanDropChunk, logger, null);
        this.respawnDimension = reader.getSetting(WorldStandardValues.RESPAWN_DIMENSION, logger, null);
        this.movementFactor = reader.getSetting(WorldStandardValues.MOVEMENT_FACTOR, logger, null);

        this.itemsToAddOnJoinDimension = reader.getSetting(WorldStandardValues.ITEMS_TO_ADD_ON_JOIN_DIMENSION, logger, null);
        this.itemsToRemoveOnJoinDimension = reader.getSetting(WorldStandardValues.ITEMS_TO_REMOVE_ON_JOIN_DIMENSION, logger, null);
        this.itemsToAddOnLeaveDimension = reader.getSetting(WorldStandardValues.ITEMS_TO_ADD_ON_LEAVE_DIMENSION, logger, null);
        this.itemsToRemoveOnLeaveDimension = reader.getSetting(WorldStandardValues.ITEMS_TO_REMOVE_ON_LEAVE_DIMENSION, logger, null);
        this.itemsToAddOnRespawn = reader.getSetting(WorldStandardValues.ITEMS_TO_ADD_ON_RESPAWN, logger, null);

        this.spawnPointSet = reader.getSetting(WorldStandardValues.SPAWN_POINT_SET, logger, null);
        this.spawnPointX = reader.getSetting(WorldStandardValues.SPAWN_POINT_X, logger, null);
        this.spawnPointY = reader.getSetting(WorldStandardValues.SPAWN_POINT_Y, logger, null);
        this.spawnPointZ = reader.getSetting(WorldStandardValues.SPAWN_POINT_Z, logger, null);

        this.playersCanBreakBlocks = reader.getSetting(WorldStandardValues.PLAYERS_CAN_BREAK_BLOCKS, logger, null);
        this.explosionsCanBreakBlocks = reader.getSetting(WorldStandardValues.EXPLOSIONS_CAN_BREAK_BLOCKS, logger, null);
        this.playersCanPlaceBlocks = reader.getSetting(WorldStandardValues.PLAYERS_CAN_PLACE_BLOCKS, logger, null);
    }

    private void readBiomeGroups(SettingsMap reader, IConfigFunctionProvider biomeResourcesManager, boolean spawnLog, ILogger logger, IMaterialReader materialReader)
    {
        this.biomeGroupManager = new BiomeGroupManager();
        if (reader.isNewConfig())
        {
            createDefaultBiomeGroups(logger);
        }
        for (ConfigFunction<IWorldConfig> res : new ArrayList<ConfigFunction<IWorldConfig>>(reader.getConfigFunctions(this, false, biomeResourcesManager, spawnLog, logger, materialReader)))
        {
            if (res != null)
            {
                if (res instanceof BiomeGroup)
                {
                    biomeGroupManager.registerGroup((BiomeGroup) res, logger);
                }
            }
        }
    }

    private void createDefaultBiomeGroups(ILogger logger)
    {
        BiomeGroup normalGroup = new BiomeGroup(this, WorldStandardValues.BiomeGroupNames.NORMAL, 0, 98,
                Arrays.asList("Forest", "Roofed Forest", "Extreme Hills", "Plains",
                        "Birch Forest", "Swampland", "Flower Forest", "Roofed Forest M",
                        "Extreme Hills+", "Sunflower Plains", "Birch Forest M", "Swampland M"));
        this.biomeGroupManager.registerGroup(normalGroup, logger);

        BiomeGroup iceGroup = new BiomeGroup(this, WorldStandardValues.BiomeGroupNames.ICE, 2, 40,
                Arrays.asList("Ice Plains", "Cold Taiga", "Ice Plains Spikes", "Cold Taiga M"));
        this.biomeGroupManager.registerGroup(iceGroup, logger);

        BiomeGroup hotGroup = new BiomeGroup(this, WorldStandardValues.BiomeGroupNames.HOT, 1, 98,
                Arrays.asList("Desert", "Savanna", "Plains", "Desert M", "Savanna M", "Sunflower Plains"));
        this.biomeGroupManager.registerGroup(hotGroup, logger);

        BiomeGroup coldGroup = new BiomeGroup(this, WorldStandardValues.BiomeGroupNames.COLD, 0, 98,
                Arrays.asList("Forest", "Extreme Hills", "Taiga", "Plains",
                        "Flower Forest", "Extreme Hills+", "Taiga M", "Sunflower Plains"));
        this.biomeGroupManager.registerGroup(coldGroup, logger);

        BiomeGroup mesaGroup = new BiomeGroup(this, WorldStandardValues.BiomeGroupNames.MESA, 2, 40,
                Arrays.asList("Mesa"));
        this.biomeGroupManager.registerGroup(mesaGroup, logger);

        BiomeGroup jungleGroup = new BiomeGroup(this, WorldStandardValues.BiomeGroupNames.JUNGLE, 1, 40,
                Arrays.asList("Jungle", "Jungle M"));
        this.biomeGroupManager.registerGroup(jungleGroup, logger);

        BiomeGroup megaTaigaGroup = new BiomeGroup(this, WorldStandardValues.BiomeGroupNames.MEGA_TAIGA, 1, 40,
                Arrays.asList("Mega Taiga", "Mega Spruce Taiga"));
        this.biomeGroupManager.registerGroup(megaTaigaGroup, logger);
    }

    @Override
    protected void writeConfigSettings(SettingsMap writer)
    {
        // About the world
        writer.bigTitle("WorldConfig");
        writer.putSetting(WorldStandardValues.AUTHOR, this.author,
            "The author of this world");

        writer.putSetting(WorldStandardValues.DESCRIPTION, this.description,
            "A short description of this world");

        writer.putSetting(WorldStandardValues.WORLDPACKER_MODNAME, this.worldPackerModName,
            "The mod name of a WorldPacker jar. The WorldPacker jar's mod image is shown in the world creation UI for this world.");
        
        writer.putSetting(WorldStandardValues.SETTINGS_MODE, this.settingsMode,
            "What " + Constants.MOD_ID + " does with the config files.",
            "Possible modes:",
            "   WriteAll - default",
            "   WriteWithoutComments - write config files without help comments",
            "   WriteDisable - doesn't write to the config files, it only reads. Doesn't auto-update the configs. Use with care!");

        // The modes
        writer.bigTitle("The modes");

        writer.putSetting(WorldStandardValues.TERRAIN_MODE, this.modeTerrain,
            "Possible terrain modes:",
            "   Normal - use all features",
            "   TerrainTest - generate only terrain without any resources",
            "   NotGenerate - generate empty chunks");//,
            //"   Default - use default terrain generator",
            //"   OldGenerator - Minecraft Beta 1.7.3-like land generator");

        /*
        writer.putSetting(WorldStandardValues.BIOME_MODE, OTG.getBiomeModeManager().getName(biomeMode),
            "Possible biome modes:",
            "   Normal - use all features",
            "   FromImage - get biomes from image file",
            //"   Default - use default Notch biome generator",
            "For old maps two more modes are available:",
            "   BeforeGroups - Minecraft 1.0 - 1.6.4 biome generator, only supports the biome groups NormalBiomes and IceBiomes");//,
            //"   OldGenerator - Minecraft Beta 1.7.3 biome generator");
        */

        // Settings for BiomeMode:Normal
        writer.bigTitle("Settings for BiomeMode: Normal",
            "Also used in BiomeMode:FromImage when ImageMode is set to ContinueNormal");

        writer.putSetting(WorldStandardValues.GENERATION_DEPTH, this.generationDepth,
            "Important value for generation. Bigger values appear to zoom out. All 'Sizes' must be smaller than this.",
            "Large %/total area biomes (Continents) must be set small, (limit=0)",
            "Small %/total area biomes (Oasis,Mountain Peaks) must be larger (limit=GenerationDepth)",
            "This could also represent \"Total number of biome sizes\" ",
            "Small values (about 1-2) and Large values (about 20) may affect generator performance.");

        writer.putSetting(WorldStandardValues.BIOME_RARITY_SCALE, this.biomeRarityScale,
            "Max biome rarity from 1 to infinity. By default this is 100, but you can raise it for",
            "fine-grained control, or to create biomes with a chance of occurring smaller than 1/100.");

        // Biome groups
        /*
        writer.smallTitle("Biome Groups",
            "Minecraft groups similar biomes together, so that they spawn next to each other.",
            "",
            "Syntax: BiomeGroup(Name, Size, Rarity, BiomeName[, AnotherName[, ...]])",
            "Name - just for clarity, choose something descriptive",
            "Size - layer to generate on, from 0 to GenerationDepth. All biomes in the group must have a BiomeSize",
            "       larger than or equal to this value.",
            "Rarity - relative spawn chances.",
            "BiomeName... - names of the biome that spawn in the group. Case sensitive.",
            "",
            "Note: if you're using BiomeMode: BeforeGroups, only the biome names of the groups named NormalBiomes",
            "and IceBiomes and the size and rarity of the group named IceBiomes will be used. Other groups are",
            "ignored. The size and rarity of the NormalBiomes group is ignored as well, use LandSize and",
            "LandRarity instead.",
            "");
        */

        //writer.addConfigFunctions(this.biomeGroupManager.getGroups());

        // Biome lists
        writer.smallTitle("Biome lists",
            //"Don't forget to register your custom biomes first in CustomBiomes!",
            "");

        writer.putSetting(WorldStandardValues.ISLE_BIOMES, this.isleBiomes,
            "Biomes used as isles in other biomes. You must set IsleInBiome in biome config for each biome here. Biome name is case sensitive.");

        writer.putSetting(WorldStandardValues.BORDER_BIOMES, this.borderBiomes,
            "Biomes used as borders of other biomes. You must set BiomeIsBorder in biome config for each biome here. Biome name is case sensitive.");

        writer.putSetting(WorldStandardValues.IMPROVED_BIOME_BORDERS, this.improvedBiomeBorders,
    		"Spawns more precise borders that never spill over into neighbouring biomes. Disabled by default for legacy worlds.");

        writer.putSetting(WorldStandardValues.IMPROVED_BIOME_GROUPS, this.improvedBiomeGroups,
    		"Fixes biome groups not changing with seeds. Disabled by default for legacy worlds.");

        writer.putSetting(WorldStandardValues.CUSTOM_HEIGHT_CONTROL_SMOOTHING, this.customHeightControlSmoothing,
            "Smooths biome CustomHeightControl data. Disabled by default for legacy worlds.");

        writer.putSetting(WorldStandardValues.IMPROVED_SMOOTHING, this.improvedSmoothing,
            "Smooths volatility and max average data. Disabled by default for legacy worlds.");

        writer.smallTitle("Landmass settings (for NormalBiomes)");

        writer.putSetting(WorldStandardValues.LAND_RARITY, this.landRarity,
            "Land rarity from 100 to 1. If you set smaller than 90 and LandSize near 0 beware Big oceans.");

        writer.putSetting(WorldStandardValues.LAND_SIZE, this.landSize,
            "Land size from 0 to GenerationDepth. Biome groups are placed on this.");

        writer.putSetting(WorldStandardValues.LAND_FUZZY, this.landFuzzy,
            "Make land more fuzzy and make lakes. Must be from 0 to GenerationDepth - LandSize");

        writer.putSetting(WorldStandardValues.DEFAULT_OCEAN_BIOME, this.defaultOceanBiome,
    		"The default Ocean biome.");
        
        writer.smallTitle("Ice area settings");

        writer.putSetting(WorldStandardValues.FROZEN_OCEAN, this.frozenOcean,
            "Set this to false to stop the ocean from freezing near when an \"ice area\" intersects with an ocean.");

        writer.putSetting(WorldStandardValues.FROZEN_OCEAN_TEMPERATURE, this.frozenOceanTemperature,
            "This is the biome temperature when water freezes if \"FrozenOcean\" is set to true.",
            "This used to be the case for all biomes in the \"IceBiomes\" list. Default: 0.15; Min: 0.0; Max: 2.0",
            "Temperature Reference from Vanilla: <0.15 for snow, 0.15 - 0.95 for rain, or >1.0 for dry");

        writer.putSetting(WorldStandardValues.GROUP_FREEZE_ENABLED, this.freezeAllColdGroupBiomes,
            "If the average of all biome temperatures in a biome group is less than \"OceanFreezingTemperature\", then:",
            " - When this setting is true, all biomes in the group will have frozen oceans",
            " - When this setting is false, only biomes with a temperature below \"OceanFreezingTemperature\" will have frozen oceans",
            "Default: false");

        writer.putSetting(WorldStandardValues.DEFAULT_FROZEN_OCEAN_BIOME, this.defaultFrozenOceanBiome,
    		"The default frozen ocean biome.");
        
        writer.smallTitle("Rivers");

        writer.putSetting(WorldStandardValues.RIVER_RARITY, this.riverRarity,
            "River rarity. Must be from 0 to GenerationDepth.");

        writer.putSetting(WorldStandardValues.RIVER_SIZE, this.riverSize,
            "River size from 0 to GenerationDepth - RiverRarity");

        writer.putSetting(WorldStandardValues.RIVERS_ENABLED, this.riversEnabled,
            "Set this to false to prevent the river generator from doing anything.");

        writer.putSetting(WorldStandardValues.IMPROVED_RIVERS, this.improvedRivers,
            "When this is set to false, the standard river generator of Minecraft will be used.",
            "This means that a technical biome, determined by the RiverBiome setting of the biome",
            "the river is flowing through, will be used to generate the river.",
            "",
            "When enabled, the rivers won't use a technical biome in your world anymore, instead",
            "you can control them using the river settings in the BiomeConfigs.");

        writer.putSetting(WorldStandardValues.RANDOM_RIVERS, this.randomRivers,
            "When set to true the rivers will no longer follow biome border most of the time.");
        
        // Settings for BiomeMode:FromImage
        writer.bigTitle("Settings for BiomeMode:FromImage");

        writer.putSetting(WorldStandardValues.IMAGE_MODE, this.imageMode,
            "Possible modes when generator outside image boundaries: Repeat, ContinueNormal, FillEmpty",
            "   Repeat - repeat image",
            "   Mirror - advanced repeat image mode",
            "   ContinueNormal - continue normal generation",
            "   FillEmpty - fill by biome in \"ImageFillBiome settings\" ");

        writer.putSetting(WorldStandardValues.IMAGE_FILE, this.imageFile,
            "Source png file for FromImage biome mode.");

        writer.putSetting(WorldStandardValues.IMAGE_ORIENTATION, this.imageOrientation,
            "Where the png's north is oriented? Possible values: North, East, South, West",
            "   North - the top of your picture if north (no any rotation)",
            "   West - previous behavior (you should rotate png CCW manually)",
            "   East - png should be rotated CW manually",
            "   South - rotate png 180 degrees before generating world");

        writer.putSetting(WorldStandardValues.IMAGE_FILL_BIOME, this.imageFillBiome,
            "Biome name for fill outside image boundaries with FillEmpty mode.");

        writer.putSetting(WorldStandardValues.IMAGE_X_OFFSET, this.imageXOffset,
            "Shifts map position from x=0 and z=0 coordinates.");
        writer.putSetting(WorldStandardValues.IMAGE_Z_OFFSET, this.imageZOffset);

        // Terrain height and volatility
        writer.bigTitle("Terrain height and volatility");

        writer.putSetting(WorldStandardValues.WORLD_HEIGHT_SCALE_BITS, this.worldHeightScaleBits,
            "Scales the height of the world. Adding 1 to this doubles the",
            "height of the terrain, substracting 1 to this halves the height",
            "of the terrain. Values must be between 5 and 8, inclusive.");

        writer.putSetting(WorldStandardValues.WORLD_HEIGHT_CAP_BITS, this.worldHeightCapBits,
            "Height cap of the base terrain. Setting this to 7 makes no terrain",
            "generate above y = 2 ^ 7 = 128. Doesn't affect resources (trees, objects, etc.).",
            "Values must be between 5 and 8, inclusive. Values may not be lower",
            "than WorldHeightScaleBits.");

        writer.putSetting(WorldStandardValues.FRACTURE_HORIZONTAL, this.fractureHorizontal,
            "Can increase (values greater than 0) or decrease (values less than 0) how much the landscape is fractured horizontally.");

        writer.putSetting(WorldStandardValues.FRACTURE_VERTICAL, this.fractureVertical,
            "Can increase (values greater than 0) or decrease (values less than 0) how much the landscape is fractured vertically.",
            "Positive values will lead to large cliffs/overhangs, floating islands, and/or a cavern world depending on other settings.");

        // Blocks
        writer.bigTitle("Blocks");

        writer.putSetting(WorldStandardValues.REMOVE_SURFACE_STONE, this.removeSurfaceStone,
            "Attempts to replace all surface stone with biome surface block.");

        writer.putSetting(WorldStandardValues.DISABLE_BEDROCK, this.disableBedrock,
            "Disable bottom of map bedrock generation. Doesn't affect bedrock on the ceiling of the map.");

        writer.putSetting(WorldStandardValues.CEILING_BEDROCK, this.ceilingBedrock,
            "Enable ceiling of map bedrock generation.");

        writer.putSetting(WorldStandardValues.FLAT_BEDROCK, this.flatBedrock,
            "Make layer of bedrock flat.");

        writer.putSetting(WorldStandardValues.BEDROCK_BLOCK, this.bedrockBlock,
            "Block used as bedrock.");

        writer.putSetting(WorldStandardValues.POPULATION_BOUNDS_CHECK, this.populationBoundsCheck,
            "Set this to false to disable the bounds check during chunk population.",
            "While this allows you to spawn larger objects, it also makes terrain generation",
            "dependant on the direction you explored the world in.");

        if (this.populateUsingSavedBiomes)
        {
            writer.putSetting(WorldStandardValues.POPULATE_USING_SAVED_BIOMES, this.populateUsingSavedBiomes,
        		"Update: Broken since 1.12.2 v7, still need to update this to work with changes.",
                "Advanced setting, only written to this file when set to true.",
                "If it is set to true the biome populator will use the biome ids present in the",
                "chunk data, ignoring the biome generator. This is useful if you have a premade",
                "map made with for example WorldPainter, but still want to populate it using "
                + Constants.MOD_ID + ".",
                "Using this together with " + BiomeStandardValues.REPLACE_TO_BIOME_NAME + " is discouraged: it uses the biome",
                "specified in " + BiomeStandardValues.REPLACE_TO_BIOME_NAME
                + " to populate the chunk, instead of the biome itself.");
        }

        writer.smallTitle("Water / Lava & Frozen States");

        writer.putSetting(WorldStandardValues.WATER_LEVEL_MAX, this.waterLevelMax,
            "Set water level. Every empty block under this level will be fill water or another block from WaterBlock ");
        writer.putSetting(WorldStandardValues.WATER_LEVEL_MIN, this.waterLevelMin);

        writer.putSetting(WorldStandardValues.WATER_BLOCK, this.waterBlock,
            "Block used as water in WaterLevel.");

        writer.putSetting(WorldStandardValues.ICE_BLOCK, this.iceBlock,
            "Block used as ice.");

        writer.putSetting(WorldStandardValues.COOLED_LAVA_BLOCK, this.cooledLavaBlock,
            "Block used as cooled or frozen lava.",
            "Set this to OBSIDIAN for \"frozen\" lava lakes in cold biomes");

        writer.smallTitle("World only");

        writer.putSetting(WorldStandardValues.FULLY_FREEZE_LAKES, this.fullyFreezeLakes,
            "By Default in cold biomes, lakes freeze but only water exposed to sky is frozen.",
            "Setting this to true causes any lake in a cold biome with at least one block exposed to sky to completely freeze");

        writer.putSetting(WorldStandardValues.BETTER_SNOW_FALL, this.betterSnowFall,
            "By Default, 1 layer of snow falls on the highest block only.",
            "Set this to true to make the amount of layers (1-8) dependent on biome temperatur.",
            "Higher altitudes have lower temperatures, so snow is more thick high up in the mountains.",        
    		"Also causes snow to fall through leaves, leaves can carry 3 layers, the rest falls through.");
        
        writer.bigTitle("Resources");

        writer.putSetting(WorldStandardValues.RESOURCES_SEED, this.resourcesSeed,
            "Seed used for the resource generation. Can only be numeric. Set to 0 to use the world seed.");

        if (objectSpawnRatio != 1)
        {
            // Write the old objectSpawnRatio

            writer.putSetting(WorldStandardValues.OBJECT_SPAWN_RATIO, this.objectSpawnRatio,
                "LEGACY setting for compability with old worlds. This setting should be kept at 1.",
                "If the setting is set at 1, the setting will vanish from the config file. Readd it",
                "manually with another value and it will be back.",
                "",
                "When using the UseWorld or UseBiome keyword for spawning custom objects, Open Terrain Generator",
                "spawns one of the possible custom objects. There is of course a chance that",
                "the chosen object cannot spawn. This setting tells TC how many times it should",
                "try to spawn that object.",
                "This setting doesn't affect growing saplings anymore.");
        }

        // OreGen
        writer.putSetting(WorldStandardValues.DISABLE_OREGEN, this.disableOreGen,
            "Disables Ore(), UnderWaterOre() and Vein() biome resources that use any type of ore block.");
        
        // Structures
        writer.bigTitle("Structures",
            "Generate-structures in the server.properties file is ignored by Open Terrain Generator. Use these settings instead.",
            "");

        // Strongholds
        writer.smallTitle("Strongholds");

        writer.putSetting(WorldStandardValues.STRONGHOLDS_ENABLED, this.strongholdsEnabled,
            "Set this to false to prevent the stronghold generator from doing anything.");

        writer.putSetting(WorldStandardValues.STRONGHOLD_COUNT, this.strongholdCount,
            "The number of strongholds in the world.");

        writer.putSetting(WorldStandardValues.STRONGHOLD_DISTANCE, this.strongholdDistance,
            "How far strongholds are from the spawn and other strongholds (minimum is 1.0, default is 32.0).");

        writer.putSetting(WorldStandardValues.STRONGHOLD_SPREAD, this.strongholdSpread,
            "How concentrated strongholds are around the spawn (minimum is 1, default is 3). Lower number, lower concentration.");

        // Villages
        writer.smallTitle("Villages");

        writer.putSetting(WorldStandardValues.VILLAGES_ENABLED, this.villagesEnabled,
            "Whether the villages are enabled or not.");

        writer.putSetting(WorldStandardValues.VILLAGE_SIZE, this.villageSize,
            "The size of the village. Larger is bigger. Normal worlds have 0 as default, superflat worlds 1.");

        writer.putSetting(WorldStandardValues.VILLAGE_DISTANCE, this.villageDistance,
            "The minimum distance between the village centers in chunks. Minimum value is 9.");

        // Rare buildings
        writer.smallTitle("Rare buildings",
            "Rare buildings are either desert pyramids, jungle temples or swamp huts.");

        writer.putSetting(WorldStandardValues.RARE_BUILDINGS_ENABLED, this.rareBuildingsEnabled,
            "Whether rare buildings are enabled.");

        writer.putSetting(WorldStandardValues.MINIMUM_DISTANCE_BETWEEN_RARE_BUILDINGS, this.minimumDistanceBetweenRareBuildings,
            "The minimum distance between rare buildings in chunks.");

        writer.putSetting(WorldStandardValues.MAXIMUM_DISTANCE_BETWEEN_RARE_BUILDINGS, this.maximumDistanceBetweenRareBuildings,
            "The maximum distance between rare buildings in chunks.");

        // Woodland Mansions
        writer.smallTitle("Woodland Mansions");

        writer.putSetting(WorldStandardValues.WOODLAND_MANSIONS_ENABLED, this.woodLandMansionsEnabled,
            "Whether woodland mansions are enabled.");

        // Ocean monuments
        writer.smallTitle("Ocean monuments");

        writer.putSetting(WorldStandardValues.OCEAN_MONUMENTS_ENABLED, this.oceanMonumentsEnabled,
            "Whether ocean monuments are enabled.");

        writer.putSetting(WorldStandardValues.OCEAN_MONUMENT_GRID_SIZE, this.oceanMonumentGridSize,
            "Ocean monuments are placed on the corners of a grid, with a random offset added to each corner.",
            "The first variable is the size of the grid in chunks.",
            "Setting this to 8 will give a grid with cells of 8x8 chunks.");

        writer.putSetting(WorldStandardValues.OCEAN_MONUMENT_RANDOM_OFFSET, this.oceanMonumentRandomOffset,
            "Random offset from each corner in chunks, on both the x and z axis.",
            "May not be smaller than 0, and may not be larger than " + WorldStandardValues.OCEAN_MONUMENT_GRID_SIZE + ".");

        // Custom structures
        writer.smallTitle("Custom structures and objects");

        writer.putSetting(WorldStandardValues.IS_OTG_PLUS, this.isOTGPlus,
            "Set this to true to use BO4's for CustomStructure().",
            "BO4's allow for collision detection, fine control over structure distribution, advanced branching mechanics for",
            "procedurally generated structures, smoothing areas, extremely large structures, settings for blending structures",
            "with surrounding terrain, etc."
		);

        writer.putSetting(WorldStandardValues.REPLACE_BLOCKS_LIST, this.replaceBlocksList,
	        "A list of blocks that will be replaced in all BO2's/BO3's",
	        "For instance: [{\"BEACON\":\"AIR\"},{\"DIAMOND_BLOCK\":\"AIR\"}]",
	        "Defaults to: []"
		);

        //

        writer.putSetting(WorldStandardValues.MAXIMUM_CUSTOM_STRUCTURE_RADIUS, this.maximumCustomStructureRadius,
            "Maximum radius of custom structures in chunks. Custom structures are spawned by",
            "the CustomStructure resource in the biome configuration files. Not used for BO4's.");

        // Other structures
        writer.smallTitle("Other structures");
        writer.putSetting(WorldStandardValues.MINESHAFTS_ENABLED, this.mineshaftsEnabled);
        writer.putSetting(WorldStandardValues.NETHER_FORTRESSES_ENABLED, this.netherFortressesEnabled);

        // Visual settings
        writer.bigTitle("Visual settings",
            "Warning: this section will work only for players with the single version of Open Terrain Generator installed.");

        writer.putSetting(WorldStandardValues.WORLD_FOG, this.worldFog,
            "World fog color");

        writer.putSetting(WorldStandardValues.WORLD_NIGHT_FOG, this.worldNightFog,
            "World night fog color");

        // Cave settings (still using code from Bucyruss' BiomeTerrainMod)
        writer.bigTitle("Cave settings");

        writer.putSetting(WorldStandardValues.CAVE_RARITY, this.caveRarity,
            "This controls the odds that a given chunk will host a single cave and/or the start of a cave system.");

        writer.putSetting(WorldStandardValues.CAVE_FREQUENCY, this.caveFrequency,
            "The number of times the cave generation algorithm will attempt to create single caves and cave",
            "systems in the given chunk. This value is larger because the likelihood for the cave generation",
            "algorithm to bailout is fairly high and it is used in a randomizer that trends towards lower",
            "random numbers. With an input of 40 (default) the randomizer will result in an average random",
            "result of 5 to 6. This can be turned off by setting evenCaveDistribution (below) to true.");

        writer.putSetting(WorldStandardValues.CAVE_MIN_ALTITUDE, this.caveMinAltitude,
            "Sets the minimum and maximum altitudes at which caves will be generated. These values are",
            "used in a randomizer that trends towards lower numbers so that caves become more frequent",
            "the closer you get to the bottom of the map. Setting even cave distribution (above) to true",
            "will turn off this randomizer and use a flat random number generator that will create an even",
            "density of caves at all altitudes.");
        writer.putSetting(WorldStandardValues.CAVE_MAX_ALTITUDE, this.caveMaxAltitude);

        writer.putSetting(WorldStandardValues.INDIVIDUAL_CAVE_RARITY, this.individualCaveRarity,
            "The odds that the cave generation algorithm will generate a single cavern without an accompanying",
            "cave system. Note that whenever the algorithm generates an individual cave it will also attempt to",
            "generate a pocket of cave systems in the vicinity (no guarantee of connection or that the cave system",
            "will actually be created).");

        writer.putSetting(WorldStandardValues.CAVE_SYSTEM_FREQUENCY, this.caveSystemFrequency,
            "The number of times the algorithm will attempt to start a cave system in a given chunk per cycle of",
            "the cave generation algorithm (see cave frequency setting above). Note that setting this value too",
            "high with an accompanying high cave frequency value can cause extremely long world generation time.");

        writer.putSetting(WorldStandardValues.CAVE_SYSTEM_POCKET_CHANCE, this.caveSystemPocketChance,
            "This can be set to create an additional chance that a cave system pocket (a higher than normal",
            "density of cave systems) being started in a given chunk. Normally, a cave pocket will only be",
            "attempted if an individual cave is generated, but this will allow more cave pockets to be generated",
            "in addition to the individual cave trigger.");

        writer.putSetting(WorldStandardValues.CAVE_SYSTEM_POCKET_MIN_SIZE, this.caveSystemPocketMinSize,
            "The minimum and maximum size that a cave system pocket can be. This modifies/overrides the",
            "cave system frequency setting (above) when triggered.");
        writer.putSetting(WorldStandardValues.CAVE_SYSTEM_POCKET_MAX_SIZE, this.caveSystemPocketMaxSize);

        writer.putSetting(WorldStandardValues.EVEN_CAVE_DISTRIBUTION, this.evenCaveDistribution,
            "Setting this to true will turn off the randomizer for cave frequency (above). Do note that",
            "if you turn this on you will probably want to adjust the cave frequency down to avoid long",
            "load times at world creation.");

        // Ravine settings
        writer.bigTitle("Ravine settings");
        writer.putSetting(WorldStandardValues.RAVINE_RARITY, this.ravineRarity);
        writer.putSetting(WorldStandardValues.RAVINE_MIN_ALTITUDE, this.ravineMinAltitude);
        writer.putSetting(WorldStandardValues.RAVINE_MAX_ALTITUDE, this.ravineMaxAltitude);
        writer.putSetting(WorldStandardValues.RAVINE_MIN_LENGTH, this.ravineMinLength);
        writer.putSetting(WorldStandardValues.RAVINE_MAX_LENGTH, this.ravineMaxLength);
        writer.putSetting(WorldStandardValues.RAVINE_DEPTH, this.ravineDepth);

        writer.bigTitle("World Seed");
        writer.putSetting(WorldStandardValues.WORLD_SEED, this.worldSeed,
	        "The seed that will be used for this world unless it is overriden in the world creation menu.",
	        "Leave blank for a random seed."
    	);

        writer.bigTitle("BO3 at spawn");
        writer.putSetting(WorldStandardValues.BO3_AT_SPAWN, this.bo3AtSpawn,
	        "This BO3 will be spawned at the world's spawn point as a CustomObject."
    	);

        writer.bigTitle("Pre-generation radius");
        writer.putSetting(WorldStandardValues.PREGENERATION_RADIUS, this.preGenerationRadius,
	        "This is the radius in chunks around the spawn chunk within which chunks will automatically be spawned (uses a rectangle, not a circle around the spawn location!",
			"Defaults to: 0 (disabled)"
		);

        writer.bigTitle("World border radius");
        writer.putSetting(WorldStandardValues.WORLD_BORDER_RADIUS, this.worldBorderRadius,
	        "This is the radius in chunks around the spawn chunk within which chunks will have blocks spawned (uses a rectangle, not a circle around the spawn location!)",
			"Defaults to: 0 (disabled)"
		);

        // Dimensions
        writer.bigTitle("Dimension");
        writer.putSetting(WorldStandardValues.DIMENSIONS, this.dimensions,
    		"Dimensions that should be loaded for this world at world creation. A world directory of the same name must be present in mods/OpenTerrainGenerator/"+ Constants.PRESETS_FOLDER + "/");
        writer.putSetting(WorldStandardValues.DIMENSIONBELOW, this.dimensionBelow,
            "When a player goes below Y 0, they will be teleported to this dimension. The dimension must be registered either via Dimensions in the worldconfig or via the /otg dim -c <dimname> console command.");
        writer.putSetting(WorldStandardValues.DIMENSIONABOVE, this.dimensionAbove,
    		"When a player goes above Y 255, they will be teleported to this dimension. The dimension must be registered either via Dimensions in the worldconfig or via the /otg dim -c <dimname> console command.");
        writer.putSetting(WorldStandardValues.DIMENSIONBELOWHEIGHT, this.dimensionBelowHeight,
            "The Y coordinate (height) for the DimensionBelow setting.");
        writer.putSetting(WorldStandardValues.DIMENSIONABOVEHEIGHT, this.dimensionAboveHeight,
    		"The Y coordinate (height) for the DimensionAbove setting.");
        writer.putSetting(WorldStandardValues.DIMENSION_PORTAL_MATERIALS, this.dimensionPortalMaterials,
            "A comma seperated list of blocks, dimension portals made of one or more of these blocks will lead to this world.",
            "For blocks that have rotation such as stairs be sure to add all rotations (0,1,2,3,4,5,6,7), for instance: QUARTZ_STAIRS:0, QUARTZ_STAIRS:1, QUARTZ_STAIRS:2 etc.",
            "For blocks that have rotation such as QUARTZ_STAIRS, \"QUARTZ_STAIRS\" is the same as \"QUARTZ_STAIRS:3\"."
		);

        writer.putSetting(WorldStandardValues.PORTAL_COLOR, this.portalColor,
    		"The color of OTG portal blocks, \"" + WorldStandardValues.PORTAL_COLOR + "\" by default." + 
    		"Colors: beige, black, blue, crystalblue, darkblue, darkgreen, darkred, emerald, flame," +
			"gold, green, grey, lightblue, lightgreen, orange, pink, red, white, yellow, default.");         
        writer.putSetting(WorldStandardValues.PORTAL_PARTICLE_TYPE, this.portalParticleType,
    		"The type of particles spawned by OTG portal blocks, \"" + WorldStandardValues.PORTAL_PARTICLE_TYPE + "\" by default. For a list of particles, use /otg particles"); 
        writer.putSetting(WorldStandardValues.PORTAL_MOB_TYPE, this.portalMobType,        		
    		"The type of mobs spawned by OTG portal blocks, \"" + WorldStandardValues.PORTAL_MOB_TYPE + "\" by default. For a list of mobs, use /otg entities");
        writer.putSetting(WorldStandardValues.PORTAL_MOB_SPAWN_CHANCE, this.portalMobSpawnChance,        		
    		"The mob spawn chance for OTG portal blocks, \"" + WorldStandardValues.PORTAL_MOB_SPAWN_CHANCE + "\" by default, lower value means higher chance. Mob spawn chance also depends on difficulty level.");        

        writer.putSetting(WorldStandardValues.TeleportToSpawnOnly, this.teleportToSpawnOnly,
    		"If this is set to true then portals to this dimension will always teleport players to the world's spawn point.");

        writer.smallTitle("Game rules", "Game rules for this world. These settings are still in development, may not all work (please submit an issue on the git) and may be subject to change in upcoming releases.", "");

        writer.putSetting(WorldStandardValues.CommandBlockOutput, Boolean.parseBoolean(this.commandBlockOutput),
    		"Whether command blocks should notify admins when they perform commands");
        writer.putSetting(WorldStandardValues.DisableElytraMovementCheck, Boolean.parseBoolean(this.disableElytraMovementCheck),
    		"Whether the server should skip checking player speed when the player is wearing elytra. Often helps with jittering due to lag in multiplayer, but may also be used to travel unfairly long distances in survival mode (cheating).");
        writer.putSetting(WorldStandardValues.DoDaylightCycle, Boolean.parseBoolean(this.doDaylightCycle),
    		"Whether the day-night cycle and moon phases progress");
        writer.putSetting(WorldStandardValues.DoEntityDrops, Boolean.parseBoolean(this.doEntityDrops),
    		"Whether entities that are not mobs should have drops");
        writer.putSetting(WorldStandardValues.DoFireTick, Boolean.parseBoolean(this.doFireTick),
    		"Whether fire should spread and naturally extinguish");
        writer.putSetting(WorldStandardValues.DoLimitedCrafting, Boolean.parseBoolean(this.doLimitedCrafting),
    		"Whether players should only be able to craft recipes that they've unlocked first");
        writer.putSetting(WorldStandardValues.DoMobLoot, Boolean.parseBoolean(this.doMobLoot),
    		"Whether mobs should drop items");
        writer.putSetting(WorldStandardValues.DoMobSpawning, Boolean.parseBoolean(this.doMobSpawning),
    		"Whether mobs should naturally spawn. Does not affect monster spawners.");
        writer.putSetting(WorldStandardValues.DoTileDrops, Boolean.parseBoolean(this.doTileDrops),
    		"Whether blocks should have drops");
        writer.putSetting(WorldStandardValues.DoWeatherCycle, Boolean.parseBoolean(this.doWeatherCycle),
    		"Whether the weather will change");
        writer.putSetting(WorldStandardValues.GameLoopFunction, Boolean.parseBoolean(this.gameLoopFunction),
    		"The function to run every game tick");        
        writer.putSetting(WorldStandardValues.KeepInventory, Boolean.parseBoolean(this.keepInventory),
    		"Whether the player should keep items in their inventory after death");
        writer.putSetting(WorldStandardValues.LogAdminCommands, Boolean.parseBoolean(this.logAdminCommands),
    		"Whether to log admin commands to server log");
        writer.putSetting(WorldStandardValues.MaxCommandChainLength, Integer.parseInt(this.maxCommandChainLength),
    		"Determines the number at which the chain command block acts as a \"chain\"");        
        writer.putSetting(WorldStandardValues.MaxEntityCramming, Integer.parseInt(this.maxEntityCramming),
    		"The maximum number of other pushable entities a mob or player can push, before taking 3 doublehearts suffocation damage per half-second. Setting to 0 disables the rule. Damage affects survival-mode or adventure-mode players, and all mobs but bats. Pushable entities include non-spectator-mode players, any mob except bats, as well as boats and minecarts.");
        writer.putSetting(WorldStandardValues.MobGriefing, Boolean.parseBoolean(this.mobGriefing),
    		"Whether creepers, zombies, endermen, ghasts, withers, ender dragons, rabbits, sheep, and villagers should be able to change blocks and whether villagers, zombies, skeletons, and zombie pigmen can pick up items");
        writer.putSetting(WorldStandardValues.NaturalRegeneration, Boolean.parseBoolean(this.naturalRegeneration),
    		"Whether the player can regenerate health naturally if their hunger is full enough (doesn't affect external healing, such as golden apples, the Regeneration effect, etc.)");
        writer.putSetting(WorldStandardValues.RandomTickSpeed, Integer.parseInt(this.randomTickSpeed),
    		"How often a random block tick occurs (such as plant growth, leaf decay, etc.) per chunk section per game tick. 0 will disable random ticks, higher numbers will increase random ticks");
        writer.putSetting(WorldStandardValues.ReducedDebugInfo, Boolean.parseBoolean(this.reducedDebugInfo),
    		"Whether the debug screen shows all or reduced information; and whether the effects of F3+B (entity hitboxes) and F3+G (chunk boundaries) are shown.");
        writer.putSetting(WorldStandardValues.SendCommandFeedback, Boolean.parseBoolean(this.sendCommandFeedback),
    		"Whether the feedback from commands executed by a player should show up in chat. Also affects the default behavior of whether command blocks store their output text");
        writer.putSetting(WorldStandardValues.ShowDeathMessages, Boolean.parseBoolean(this.showDeathMessages),
    		"Whether death messages are put into chat when a player dies. Also affects whether a message is sent to the pet's owner when the pet dies");
        writer.putSetting(WorldStandardValues.SpawnRadius, Integer.parseInt(this.spawnRadius),
    		"The number of blocks outward from the world spawn coordinates that a player will spawn in when first joining a server or when dying without a spawnpoint.");
        writer.putSetting(WorldStandardValues.SpectatorsGenerateChunks, Boolean.parseBoolean(this.spectatorsGenerateChunks),
    		"Whether players in spectator mode can generate chunks");

        // World provider settings for worlds used as dimensions with Forge
        writer.smallTitle("World provider settings", "World provider settings for this world. These settings are still in development, may not all work (please submit an issue on the git) and may be subject to change in upcoming releases.", "");

        writer.putSetting(WorldStandardValues.WelcomeMessage, this.welcomeMessage,
    		"A message to display to the user when they transfer to this dimension.");
        writer.putSetting(WorldStandardValues.DepartMessage, this.departMessage,
    		"A Message to display to the user when they transfer out of this dimension.");
        writer.putSetting(WorldStandardValues.HasSkyLight, this.hasSkyLight,
    		"A boolean that tells if a world has a sky or not. Used for calculating weather and skylight. Also affects GetActualHeight(), hasSkyLight = false worlds are seen as 128 height worlds, which affects nether portal placement/detection.");
        writer.putSetting(WorldStandardValues.IsSurfaceWorld, this.isSurfaceWorld,
    		"Returns 'true' if in the \"main surface world\", but 'false' if in the Nether or End dimensions. Affects: Clock, Compass, sky/cloud rendering, allowed to sleep here, zombie pigmen spawning in portal frames.");
        writer.putSetting(WorldStandardValues.CanRespawnHere, this.canRespawnHere,
    		"True if the player can respawn in this dimension (true = overworld, false = nether).");
        writer.putSetting(WorldStandardValues.DoesWaterVaporize, this.doesWaterVaporize,
    		"True for nether, any water that is placed vaporises.");
        writer.putSetting(WorldStandardValues.DoesXZShowFog, this.doesXZShowFog,
    		"Returns true if the given X,Z coordinate should show environmental fog. True for Nether.");
        writer.putSetting(WorldStandardValues.UseCustomFogColor, this.useCustomFogColor,
    		"Set this to true if you want to use the fog color settings below.");
        writer.putSetting(WorldStandardValues.FogColorRed, this.fogColorRed);
        writer.putSetting(WorldStandardValues.FogColorGreen, this.fogColorGreen);
        writer.putSetting(WorldStandardValues.FogColorBlue, this.fogColorBlue);
        writer.putSetting(WorldStandardValues.IsSkyColored, this.isSkyColored,
    		"Is set to false for End (black sky?)");
        //writer.putSetting(WorldStandardValues.averageGroundlevel, this.averageGroundlevel,
        		//"Affects spawn point location and village spawning. Should be equal to sea level + 1(?)");
        //writer.putSetting(WorldStandardValues.horizonHeight, this.horizonHeight,
        		//"Returns horizon height for use in rendering the sky. Should be equal to sea level(?)");
        writer.putSetting(WorldStandardValues.CloudHeight, this.cloudHeight);
        writer.putSetting(WorldStandardValues.CanDoLightning, this.canDoLightning);
        writer.putSetting(WorldStandardValues.CanDoRainSnowIce, this.canDoRainSnowIce);
        writer.putSetting(WorldStandardValues.IsNightWorld, this.isNightWorld,
    		"If true then the sky will be locked at midnight with the moon and stars above but the world will be lit as if it were day time. Useful for space dimensions.");
        writer.putSetting(WorldStandardValues.VoidFogYFactor, this.voidFogYFactor,
    		"A double value representing the Y value relative to the top of the map at which void fog is at its maximum. The default factor of 0.03125 relative to 256, for example, means the void fog will be at its maximum at (256*0.03125), or 8.");

        writer.putSetting(WorldStandardValues.GravityFactor, this.gravityFactor,
    		"A value above 0.0, defaults to 0.08. Affects entities jumping and falling. 0.04 would result in half the gravity and falling damage.",
    		"NOTE: Broken for Forge 1.11.2 MP atm, should work fine for SP and 1.12.2 SP & MP.");

        writer.putSetting(WorldStandardValues.ShouldMapSpin, this.shouldMapSpin,
    		"Determine if the cursor on the map should 'spin' when rendered, like it does for the player in the nether.");
        writer.putSetting(WorldStandardValues.CanDropChunk, this.canDropChunk,
    		"Used for dimensions, when set to false, no chunks are ever unloaded in the dimension. Used for The End.");
        writer.putSetting(WorldStandardValues.RESPAWN_DIMENSION, this.respawnDimension,
    		"Dimension that players respawn in when dying in this dimension, defaults to 0, only applies when canRespawnHere = false.");
        writer.putSetting(WorldStandardValues.MOVEMENT_FACTOR, this.movementFactor,
    		"The dimension's movement factor. Whenever a player or entity changes dimension from world A to world B, their coordinates are multiplied by worldA.provider.getMovementFactor() / worldB.provider.getMovementFactor(). Example: Overworld factor is 1, nether factor is 8. Traveling from overworld to nether multiplies coordinates by 1/8.");

        writer.putSetting(WorldStandardValues.ITEMS_TO_ADD_ON_JOIN_DIMENSION, this.itemsToAddOnJoinDimension,
    		"Similar to the /give command, gives players items when they enter a dimension/world.",
    		"Example (single): { \"flint_and_steel\", \"1\" }",
    		"Example (single): { \"diamond_sword\", \"1\", \"0\", \"{ench:[{id:16,lvl:5}]}\" } // 0 is metaDataString",
    		"Example (multiple): [{ \"diamond_sword\", \"1\", \"0\", \"{ench:[{id:16,lvl:5}]}\" }, { \"diamond_helmet\", \"1\", \"0\", \"{ench:[{id:16,lvl:5}]}\" }]",
    		"Use -1 as amount to remove all matching items.");
        writer.putSetting(WorldStandardValues.ITEMS_TO_REMOVE_ON_JOIN_DIMENSION, this.itemsToRemoveOnJoinDimension,
    		"The opposite of the /give command, removes items from players inventories when they enter a dimension/world.",
    		"Example (single): { \"flint_and_steel\", \"1\" }",
    		"Example (single): { \"diamond_sword\", \"1\", \"0\", \"{ench:[{id:16,lvl:5}]}\" } // 0 is metaDataString",        		
    		"Example (multiple): [{ \"diamond_sword\", \"1\", \"0\", \"{ench:[{id:16,lvl:5}]}\" }, { \"diamond_helmet\", \"1\", \"0\", \"{ench:[{id:16,lvl:5}]}\" }]",
    		"Use -1 as amount to remove all matching items.");
        writer.putSetting(WorldStandardValues.ITEMS_TO_ADD_ON_LEAVE_DIMENSION, this.itemsToAddOnLeaveDimension,
    		"Similar to the /give command, gives players items when they leave a dimension/world.",
    		"Example (single): { \"flint_and_steel\", \"1\" }",
    		"Example (single): { \"diamond_sword\", \"1\", \"0\", \"{ench:[{id:16,lvl:5}]}\" } // 0 is metaDataString",        		
    		"Example (multiple): [{ \"diamond_sword\", \"1\", \"0\", \"{ench:[{id:16,lvl:5}]}\" }, { \"diamond_helmet\", \"1\", \"0\", \"{ench:[{id:16,lvl:5}]}\" }]",
			"Use -1 as amount to remove all matching items.");
        writer.putSetting(WorldStandardValues.ITEMS_TO_REMOVE_ON_LEAVE_DIMENSION, this.itemsToRemoveOnLeaveDimension,
    		"The opposite of the /give command, removes items from players inventories when they leave a dimension/world.",
    		"Example (single): { \"flint_and_steel\", \"1\" }",
    		"Example (single): { \"diamond_sword\", \"1\", \"0\", \"{ench:[{id:16,lvl:5}]}\" } // 0 is metaDataString",        		
    		"Example (multiple): [{ \"diamond_sword\", \"1\", \"0\", \"{ench:[{id:16,lvl:5}]}\" }, { \"diamond_helmet\", \"1\", \"0\", \"{ench:[{id:16,lvl:5}]}\" }]",
			"Use -1 as amount to remove all matching items.");
        writer.putSetting(WorldStandardValues.ITEMS_TO_ADD_ON_RESPAWN, this.itemsToAddOnRespawn,
    		"Similar to the /give command, gives players items when they respawn in a dimension/world.",
    		"Example (single): { \"flint_and_steel\", \"1\" }",
    		"Example (single): { \"diamond_sword\", \"1\", \"0\", \"{ench:[{id:16,lvl:5}]}\" } // 0 is metaDataString",
    		"Example (multiple): [{ \"diamond_sword\", \"1\", \"0\", \"{ench:[{id:16,lvl:5}]}\" }, { \"diamond_helmet\", \"1\", \"0\", \"{ench:[{id:16,lvl:5}]}\" }]",
			"Use -1 as amount to remove all matching items.");

		writer.putSetting(WorldStandardValues.SPAWN_POINT_SET, this.spawnPointSet,
			"Set this to true to set the server spawn point to SpawnPointX, SpawnPointY, SpawnPointZ");

		writer.putSetting(WorldStandardValues.SPAWN_POINT_X, this.spawnPointX,
			"Use this with SpawnPointSet: true to set a spawn coordinate.");

		writer.putSetting(WorldStandardValues.SPAWN_POINT_Y, this.spawnPointY,
			"Use this with SpawnPointSet: true to set a spawn coordinate.");

		writer.putSetting(WorldStandardValues.SPAWN_POINT_Z, this.spawnPointZ,
			"Use this with SpawnPointSet: true to set a spawn coordinate.");

		writer.putSetting(WorldStandardValues.PLAYERS_CAN_BREAK_BLOCKS, this.playersCanBreakBlocks,
			"When set to false players cannot break blocks in this world. Defaults to: true");

		writer.putSetting(WorldStandardValues.EXPLOSIONS_CAN_BREAK_BLOCKS, this.explosionsCanBreakBlocks,
			"When set to false explosions cannot break blocks in this world. Defaults to: true");

		writer.putSetting(WorldStandardValues.PLAYERS_CAN_PLACE_BLOCKS, this.playersCanPlaceBlocks,
			"When set to false players cannot place blocks in this world. Defaults to: true");
    }
    
    private final Comparator<Entry<String, Integer>> CBV = new Comparator<Entry<String, Integer>>()
    {
        @Override
        public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2)
        {
            return o1.getValue() - o2.getValue();
        }
    };    
    
	public static WorldConfig fromDisk(Path worldDir, IConfigFunctionProvider biomeResourcesManager, boolean spawnLog, ILogger logger, IMaterialReader materialReader)
	{
		File worldConfigFile = Paths.get(worldDir.toString(), Constants.WORLD_CONFIG_FILE).toFile();
        if(!worldConfigFile.exists())
        {
        	return null;
        }
        SettingsMap settingsMap = FileSettingsReader.read(worldDir.toFile().getName(), worldConfigFile, logger);
        return new WorldConfig(worldDir, settingsMap, null, biomeResourcesManager, spawnLog, logger, materialReader);
	}
}
