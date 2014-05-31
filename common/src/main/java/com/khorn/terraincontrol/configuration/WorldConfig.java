package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.io.SettingsReader;
import com.khorn.terraincontrol.configuration.io.SettingsWriter;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.generator.biome.BiomeGenerator;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class WorldConfig extends ConfigFile
{
    public final File settingsDir;
    private final Comparator<Entry<String, Integer>> CBV = new Comparator<Entry<String, Integer>>()
    {
        @Override
        public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2)
        {
            return o1.getValue() - o2.getValue();
        }
    };

    public Map<String, Integer> customBiomeGenerationIds = new HashMap<String, Integer>();

    // Holds all world CustomObjects.
    public List<CustomObject> customObjects = new ArrayList<CustomObject>();

    public List<String> NormalBiomes = new ArrayList<String>();
    public List<String> IceBiomes = new ArrayList<String>();
    public List<String> IsleBiomes = new ArrayList<String>();
    public List<String> BorderBiomes = new ArrayList<String>();

    public int maxSmoothRadius = 2;

    // For old biome generator
    public double oldBiomeSize;

    public double minMoisture;
    public double maxMoisture;
    public double minTemperature;
    public double maxTemperature;

    // Biome generator
    public int GenerationDepth;
    public int BiomeRarityScale;

    public int LandRarity;
    public int LandSize;
    public int LandFuzzy;

    public int IceRarity;
    public int IceSize;

    public boolean FrozenOcean;

    // Rivers

    public int riverRarity;
    public int riverSize;
    public boolean riversEnabled;
    public boolean improvedRivers;
    public boolean randomRivers;

    // Biome image

    public String imageFile;
    public ImageOrientation imageOrientation;
    public ImageMode imageMode;
    // public int imageZoom;
    public String imageFillBiome;
    public int imageXOffset;
    public int imageZOffset;

    public HashMap<Integer, Integer> biomeColorMap;

    // Look settings
    public int WorldFog;
    public float WorldFogR;
    public float WorldFogG;
    public float WorldFogB;

    public int WorldNightFog;
    public float WorldNightFogR;
    public float WorldNightFogG;
    public float WorldNightFogB;

    // Specific biome settings

    // Caves
    public int caveRarity;
    public int caveFrequency;
    public int caveMinAltitude;
    public int caveMaxAltitude;
    public int individualCaveRarity;
    public int caveSystemFrequency;
    public int caveSystemPocketChance;
    public int caveSystemPocketMinSize;
    public int caveSystemPocketMaxSize;
    public boolean evenCaveDistribution;

    // Canyons
    public int canyonRarity;
    public int canyonMinAltitude;
    public int canyonMaxAltitude;
    public int canyonMinLength;
    public int canyonMaxLength;
    public double canyonDepth;

    // Strongholds
    public boolean strongholdsEnabled;
    public double strongholdDistance;
    public int strongholdCount;
    public int strongholdSpread;

    // Villages
    public boolean villagesEnabled;
    public int villageSize;
    public int villageDistance; // Has a minimum of 9

    // Pyramids (also swamp huts and jungle temples)
    public boolean rareBuildingsEnabled;
    public int minimumDistanceBetweenRareBuildings; // Minecraft's internal
    // value is 1 chunk lower
    public int maximumDistanceBetweenRareBuildings;

    // Other structures
    public boolean mineshaftsEnabled;
    public boolean netherFortressesEnabled;

    // Terrain
    public boolean oldTerrainGenerator;

    public int waterLevelMax;
    public int waterLevelMin;
    public LocalMaterialData waterBlock;
    public LocalMaterialData iceBlock;

    public double fractureHorizontal;
    public double fractureVertical;

    public boolean disableBedrock;
    public boolean flatBedrock;
    public boolean ceilingBedrock;
    public LocalMaterialData bedrockBlock;
    public boolean populationBoundsCheck;

    public boolean removeSurfaceStone;

    public int objectSpawnRatio;
    public File customObjectsDirectory;

    public ConfigMode SettingsMode;
    public TerrainMode ModeTerrain;
    public Class<? extends BiomeGenerator> biomeMode;

    public boolean BiomeConfigsHaveReplacement = false;

    public int normalBiomesRarity;
    public int iceBiomesRarity;

    public int worldHeightScaleBits;
    public int worldHeightScale;
    public int worldHeightCapBits;
    public int worldHeightCap;

    public long resourcesSeed;

    /**
     * Creates a WorldConfig from the WorldConfig.ini file found in the given
     * directory.
     * 
     * @param settingsDir The settings directory where the WorldConfig.ini is
     *            in.
     * @param world The LocalWorld instance of the world.
     */
    public WorldConfig(SettingsReader settingsReader, LocalWorld world)
    {
        super(settingsReader);
        if (settingsReader.getFile() != null) {
            settingsDir = settingsReader.getFile().getParentFile();
        } else {
            settingsDir = new File(".");
        }

        // Read the WorldConfig file
        this.readConfigSettings();
        // Fix older names
        this.renameOldSettings();
        // Set the local fields based on what was read from the file
        this.readConfigSettings();
        // Clamp Settings to acceptable values
        this.correctSettings();

        ReadWorldCustomObjects();

        // Check biome ids, These are the names from the worldConfig file
        // Corrects any instances of incorrect biome id.
        for (String biomeName : customBiomeGenerationIds.keySet())
        {
            if (customBiomeGenerationIds.get(biomeName) == -1)
            {
                customBiomeGenerationIds.put(biomeName, world.getFreeBiomeId());
            }
        }
    }

    private void ReadWorldCustomObjects()
    {
        customObjectsDirectory = new File(this.settingsDir, WorldStandardValues.WORLD_OBJECTS_DIRECTORY_NAME);

        File oldCustomObjectsDirectory = new File(settingsDir, "BOBPlugins");
        if (oldCustomObjectsDirectory.exists())
        {
            if (!oldCustomObjectsDirectory.renameTo(new File(settingsDir, WorldStandardValues.WORLD_OBJECTS_DIRECTORY_NAME)))
            {
                TerrainControl.log(LogMarker.WARN, "Fould old BOBPlugins folder, but it cannot be renamed to WorldObjects.");
                TerrainControl.log(LogMarker.WARN, "Please move the BO2s manually and delete BOBPlugins afterwards.");
            }
        }

        if (!customObjectsDirectory.exists())
        {
            if (!customObjectsDirectory.mkdirs())
            {
                TerrainControl.log(LogMarker.WARN, "Can`t create WorldObjects folder. No write permissions?");
                return;
            }
        }

        customObjects = new ArrayList<CustomObject>(TerrainControl.getCustomObjectManager().loadObjects(customObjectsDirectory).values());

        TerrainControl.log(LogMarker.INFO, "{} world custom objects loaded.", customObjects.size());

    }

    @Override
    protected void renameOldSettings()
    {
        renameOldSetting("WaterLevel", WorldStandardValues.WATER_LEVEL_MAX);
        renameOldSetting("ModeTerrain", WorldStandardValues.TERRAIN_MODE);
        renameOldSetting("ModeBiome", WorldStandardValues.BIOME_MODE);
        renameOldSetting("NetherFortressEnabled", WorldStandardValues.NETHER_FORTRESSES_ENABLED);
        renameOldSetting("PyramidsEnabled", WorldStandardValues.RARE_BUILDINGS_ENABLED);
        // WorldHeightBits was split into two different settings
        renameOldSetting("WorldHeightBits", WorldStandardValues.WORLD_HEIGHT_SCALE_BITS);
        renameOldSetting("WorldHeightBits", WorldStandardValues.WORLD_HEIGHT_CAP_BITS);
    }

    @Override
    protected void correctSettings()
    {
        LandSize = lowerThanOrEqualTo(LandSize, GenerationDepth);
        LandFuzzy = lowerThanOrEqualTo(LandFuzzy, GenerationDepth - LandSize);
        IceSize = lowerThanOrEqualTo(IceSize, GenerationDepth);

        riverRarity = lowerThanOrEqualTo(riverRarity, GenerationDepth);
        riverSize = lowerThanOrEqualTo(riverSize, GenerationDepth - riverRarity);

        NormalBiomes = filterBiomes(NormalBiomes, customBiomeGenerationIds.keySet());
        IceBiomes = filterBiomes(IceBiomes, customBiomeGenerationIds.keySet());
        IsleBiomes = filterBiomes(IsleBiomes, customBiomeGenerationIds.keySet());
        BorderBiomes = filterBiomes(BorderBiomes, customBiomeGenerationIds.keySet());

        if (biomeMode == TerrainControl.getBiomeModeManager().FROM_IMAGE)
        {
            File mapFile = new File(settingsDir, imageFile);
            if (!mapFile.exists())
            {
                TerrainControl.log(LogMarker.WARN, "Biome map file not found. Switching BiomeMode to Normal");
                biomeMode = TerrainControl.getBiomeModeManager().NORMAL;
            }
        }

        imageFillBiome = (DefaultBiome.Contain(imageFillBiome) || customBiomeGenerationIds.keySet().contains(imageFillBiome)) ? imageFillBiome : WorldStandardValues.IMAGE_FILL_BIOME.getDefaultValue();

        maxMoisture = higherThan(maxMoisture, minMoisture);
        maxTemperature = higherThan(maxTemperature, minTemperature);

        caveMaxAltitude = higherThan(caveMaxAltitude, caveMinAltitude);
        caveSystemPocketMaxSize = higherThan(caveSystemPocketMaxSize, caveSystemPocketMinSize);
        canyonMaxAltitude = higherThan(canyonMaxAltitude, canyonMinAltitude);
        canyonMaxLength = higherThan(canyonMaxLength, canyonMinLength);

        waterLevelMax = higherThan(waterLevelMax, waterLevelMin);

        // Remove illegal block data (the chunk generator will ignore block data)
        waterBlock = waterBlock.withBlockData(0);
        iceBlock = iceBlock.withBlockData(0);
        bedrockBlock = bedrockBlock.withBlockData(0);

        maximumDistanceBetweenRareBuildings = higherThan(maximumDistanceBetweenRareBuildings, minimumDistanceBetweenRareBuildings);

        if (biomeMode == TerrainControl.getBiomeModeManager().OLD_GENERATOR && ModeTerrain != TerrainMode.OldGenerator)
        {
            TerrainControl.log(LogMarker.WARN, "Old biome generator works only with old terrain generator!");
            biomeMode = TerrainControl.getBiomeModeManager().NORMAL;

        }
    }

    @Override
    protected void readConfigSettings()
    {
        // Main modes
        this.SettingsMode = readSettings(WorldStandardValues.SETTINGS_MODE);
        this.ModeTerrain = readSettings(WorldStandardValues.TERRAIN_MODE);
        this.biomeMode = TerrainControl.getBiomeModeManager().getBiomeManager((String) readSettings(WorldStandardValues.BIOME_MODE));

        // World and water height
        this.worldHeightCapBits = readSettings(WorldStandardValues.WORLD_HEIGHT_CAP_BITS);
        this.worldHeightCap = 1 << this.worldHeightCapBits;
        this.worldHeightScaleBits = readSettings(WorldStandardValues.WORLD_HEIGHT_SCALE_BITS);
        this.worldHeightScaleBits = lowerThanOrEqualTo(this.worldHeightScaleBits, this.worldHeightCapBits);
        this.worldHeightScale = 1 << this.worldHeightScaleBits;
        this.waterLevelMax = worldHeightCap / 2 - 1;

        // Biome placement
        this.GenerationDepth = readSettings(WorldStandardValues.GENERATION_DEPTH);

        this.BiomeRarityScale = readSettings(WorldStandardValues.BIOME_RARITY_SCALE);
        this.LandRarity = readSettings(WorldStandardValues.LAND_RARITY);
        this.LandSize = readSettings(WorldStandardValues.LAND_SIZE);
        this.LandFuzzy = readSettings(WorldStandardValues.LAND_FUZZY);

        this.IceRarity = readSettings(WorldStandardValues.ICE_RARITY);
        this.IceSize = readSettings(WorldStandardValues.ICE_SIZE);

        this.FrozenOcean = readSettings(WorldStandardValues.FROZEN_OCEAN);

        // Rivers

        this.riverRarity = readSettings(WorldStandardValues.RIVER_RARITY);
        this.riverSize = readSettings(WorldStandardValues.RIVER_SIZE);
        this.riversEnabled = readSettings(WorldStandardValues.RIVERS_ENABLED);
        this.improvedRivers = readSettings(WorldStandardValues.IMPROVED_RIVERS);
        this.randomRivers = readSettings(WorldStandardValues.RANDOM_RIVERS);

        // Biomes
        this.NormalBiomes = readSettings(WorldStandardValues.NORMAL_BIOMES);
        this.IceBiomes = readSettings(WorldStandardValues.ICE_BIOMES);
        this.IsleBiomes = readSettings(WorldStandardValues.ISLE_BIOMES);
        this.BorderBiomes = readSettings(WorldStandardValues.BORDER_BIOMES);
        ReadCustomBiomes();

        // Images
        this.imageMode = readSettings(WorldStandardValues.IMAGE_MODE);
        this.imageFile = this.readSettings(WorldStandardValues.IMAGE_FILE);
        this.imageOrientation = this.readSettings(WorldStandardValues.IMAGE_ORIENTATION);
        this.imageFillBiome = this.readSettings(WorldStandardValues.IMAGE_FILL_BIOME);
        this.imageXOffset = this.readSettings(WorldStandardValues.IMAGE_X_OFFSET);
        this.imageZOffset = this.readSettings(WorldStandardValues.IMAGE_Z_OFFSET);

        // Old biomes
        this.oldBiomeSize = readSettings(WorldStandardValues.OLD_BIOME_SIZE);
        this.minMoisture = readSettings(WorldStandardValues.MIN_MOISTURE);
        this.maxMoisture = readSettings(WorldStandardValues.MAX_MOISTURE);
        this.minTemperature = readSettings(WorldStandardValues.MIN_TEMPERATURE);
        this.maxTemperature = readSettings(WorldStandardValues.MAX_TEMPERATURE);

        // Fog
        this.WorldFog = readSettings(WorldStandardValues.WORLD_FOG);
        this.WorldNightFog = readSettings(WorldStandardValues.WORLD_NIGHT_FOG);

        this.WorldFogR = ((WorldFog & 0xFF0000) >> 16) / 255F;
        this.WorldFogG = ((WorldFog & 0xFF00) >> 8) / 255F;
        this.WorldFogB = (WorldFog & 0xFF) / 255F;

        this.WorldNightFogR = ((WorldNightFog & 0xFF0000) >> 16) / 255F;
        this.WorldNightFogG = ((WorldNightFog & 0xFF00) >> 8) / 255F;
        this.WorldNightFogB = (WorldNightFog & 0xFF) / 255F;

        // Structures
        this.strongholdsEnabled = readSettings(WorldStandardValues.STRONGHOLDS_ENABLED);
        this.strongholdCount = readSettings(WorldStandardValues.STRONGHOLD_COUNT);
        this.strongholdDistance = readSettings(WorldStandardValues.STRONGHOLD_DISTANCE);
        this.strongholdSpread = readSettings(WorldStandardValues.STRONGHOLD_SPREAD);

        this.villagesEnabled = readSettings(WorldStandardValues.VILLAGES_ENABLED);
        this.villageDistance = readSettings(WorldStandardValues.VILLAGE_DISTANCE);
        this.villageSize = readSettings(WorldStandardValues.VILLAGE_SIZE);

        this.rareBuildingsEnabled = readSettings(WorldStandardValues.RARE_BUILDINGS_ENABLED);
        this.minimumDistanceBetweenRareBuildings = readSettings(WorldStandardValues.MINIMUM_DISTANCE_BETWEEN_RARE_BUILDINGS);
        this.maximumDistanceBetweenRareBuildings = readSettings(WorldStandardValues.MAXIMUM_DISTANCE_BETWEEN_RARE_BUILDINGS);

        this.mineshaftsEnabled = readSettings(WorldStandardValues.MINESHAFTS_ENABLED);
        this.netherFortressesEnabled = readSettings(WorldStandardValues.NETHER_FORTRESSES_ENABLED);

        // Caves
        this.caveRarity = readSettings(WorldStandardValues.CAVE_RARITY);
        this.caveFrequency = readSettings(WorldStandardValues.CAVE_FREQUENCY);
        this.caveMinAltitude = readSettings(WorldStandardValues.CAVE_MIN_ALTITUDE);
        this.caveMaxAltitude = readSettings(WorldStandardValues.CAVE_MAX_ALTITUDE);
        this.individualCaveRarity = readSettings(WorldStandardValues.INDIVIDUAL_CAVE_RARITY);
        this.caveSystemFrequency = readSettings(WorldStandardValues.CAVE_SYSTEM_FREQUENCY);
        this.caveSystemPocketChance = readSettings(WorldStandardValues.CAVE_SYSTEM_POCKET_CHANCE);
        this.caveSystemPocketMinSize = readSettings(WorldStandardValues.CAVE_SYSTEM_POCKET_MIN_SIZE);
        this.caveSystemPocketMaxSize = readSettings(WorldStandardValues.CAVE_SYSTEM_POCKET_MAX_SIZE);
        this.evenCaveDistribution = readSettings(WorldStandardValues.EVEN_CAVE_DISTRIBUTION);

        // Canyons
        this.canyonRarity = readSettings(WorldStandardValues.CANYON_RARITY);
        this.canyonMinAltitude = readSettings(WorldStandardValues.CANYON_MIN_ALTITUDE);
        this.canyonMaxAltitude = readSettings(WorldStandardValues.CANYON_MAX_ALTITUDE);
        this.canyonMinLength = readSettings(WorldStandardValues.CANYON_MIN_LENGTH);
        this.canyonMaxLength = readSettings(WorldStandardValues.CANYON_MAX_LENGTH);
        this.canyonDepth = readSettings(WorldStandardValues.CANYON_DEPTH);

        // Water
        this.waterLevelMax = readSettings(WorldStandardValues.WATER_LEVEL_MAX);
        this.waterLevelMin = readSettings(WorldStandardValues.WATER_LEVEL_MIN);
        this.waterBlock = readSettings(WorldStandardValues.WATER_BLOCK);
        this.iceBlock = readSettings(WorldStandardValues.ICE_BLOCK);

        // Fracture
        this.fractureHorizontal = readSettings(WorldStandardValues.FRACTURE_HORIZONTAL);
        this.fractureVertical = readSettings(WorldStandardValues.FRACTURE_VERTICAL);

        // Bedrock
        this.disableBedrock = readSettings(WorldStandardValues.DISABLE_BEDROCK);
        this.ceilingBedrock = readSettings(WorldStandardValues.CEILING_BEDROCK);
        this.flatBedrock = readSettings(WorldStandardValues.FLAT_BEDROCK);
        this.bedrockBlock = readSettings(WorldStandardValues.BEDROCK_BLOCK);

        // Misc
        this.removeSurfaceStone = readSettings(WorldStandardValues.REMOVE_SURFACE_STONE);
        this.objectSpawnRatio = readSettings(WorldStandardValues.OBJECT_SPAWN_RATIO);
        this.resourcesSeed = readSettings(WorldStandardValues.RESOURCES_SEED);
        this.populationBoundsCheck = readSettings(WorldStandardValues.POPULATION_BOUNDS_CHECK);

        this.oldTerrainGenerator = this.ModeTerrain == TerrainMode.OldGenerator;
    }

    private void ReadCustomBiomes()
    {

        List<String> biomes = this.readSettings(WorldStandardValues.CUSTOM_BIOMES);

        for (String biome : biomes)
        {
            try
            {
                String[] keys = biome.split(":");
                if (keys[0].isEmpty())
                {
                    // Don't allow biomes with empty names
                    continue;
                }
                if (keys.length == 2)
                {
                    int generationBiomeId = Integer.parseInt(keys[1]);
                    customBiomeGenerationIds.put(keys[0], generationBiomeId);
                } else
                {
                    customBiomeGenerationIds.put(keys[0], -1);
                }

            } catch (NumberFormatException e)
            {
                System.out.println("Wrong custom biome id settings: '" + biome + "'");
            }

        }

    }

    @Override
    protected void writeConfigSettings(SettingsWriter writer) throws IOException
    {
        // The modes
        writer.bigTitle("The modes");
        writer.comment("What Terrain Control does with the config files.");
        writer.comment("Possible modes: WriteAll, WriteWithoutComments, WriteDisable");
        writer.comment("   WriteAll - default");
        writer.comment("   WriteWithoutComments - write config files without help comments");
        writer.comment("   WriteDisable - doesn't write to the config files, it only reads. Doesn't auto-update the configs. Use with care!");
        writer.setting(WorldStandardValues.SETTINGS_MODE, this.SettingsMode);

        writer.comment("Possible terrain modes: Normal, OldGenerator, TerrainTest, NotGenerate, Default");
        writer.comment("   Normal - use all features");
        writer.comment("   OldGenerator - generate land like Beta 1.7.3 generator");
        writer.comment("   TerrainTest - generate only terrain without any resources");
        writer.comment("   NotGenerate - generate empty chunks");
        writer.comment("   Default - use default terrain generator");
        writer.setting(WorldStandardValues.TERRAIN_MODE, this.ModeTerrain);

        writer.comment("Possible biome modes: Normal, OldGenerator, Default");
        writer.comment("   Normal - use all features");
        writer.comment("   FromImage - get biomes from image file");
        writer.comment("   OldGenerator - generate biome like the Beta 1.7.3 generator");
        writer.comment("   Default - use default Notch biome generator");
        writer.setting(WorldStandardValues.BIOME_MODE, TerrainControl.getBiomeModeManager().getName(biomeMode));

        // Custom biomes
        writer.bigTitle("Custom biomes");
        writer.comment("You need to register your custom biomes here. This setting will make Terrain Control");
        writer.comment("generate setting files for them. However, it won't place them in the world automatically.");
        writer.comment("See the settings for your BiomeMode below on how to add them to the world.");
        writer.comment("");
        writer.comment("Syntax: CustomBiomes:BiomeName:id[,AnotherBiomeName:id[,...]]");
        writer.comment("Example: CustomBiomes:TestBiome1:30,BiomeTest2:31");
        writer.comment("This will add two biomes and generate the BiomeConfigs for them.");
        writer.comment("All changes here need a server restart.");
        writer.comment("");
        writer.comment("Due to the way Mojang's loading code works, all biome ids need to be unique");
        writer.comment("on the server. If you don't do this, the client will display the biomes just fine,");
        writer.comment("but the server can think it is another biome with the same id. This will cause saplings,");
        writer.comment("snowfall and mobs to work as in the other biome.");
        writer.comment("");
        writer.comment("The available ids range from 0 to 1023 and the ids 0-39 and 129-167 are taken by vanilla.");
        writer.comment("The ids 256-1023 cannot be saved to the map files, so use ReplaceToBiomeName in that biome.");

        WriteCustomBiomes(writer);

        // Settings for BiomeMode:Normal
        writer.bigTitle("Settings for BiomeMode:Normal");
        writer.comment("Also applies if you are using BiomeMode:FromImage and ImageMode:ContinueNormal.");

        writer.comment("Important value for generation. Bigger values appear to zoom out. All 'Sizes' must be smaller than this.");
        writer.comment("Large %/total area biomes (Continents) must be set small, (limit=0)");
        writer.comment("Small %/total area biomes (Oasis,Mountain Peaks) must be larger (limit=GenerationDepth)");
        writer.comment("This could also represent \"Total number of biome sizes\" ");
        writer.comment("Small values (about 1-2) and Large values (about 20) may affect generator performance.");
        writer.setting(WorldStandardValues.GENERATION_DEPTH, this.GenerationDepth);

        writer.comment("Max biome rarity from 1 to infinity. By default this is 100, but you can raise it for");
        writer.comment("fine-grained control, or to create biomes with a chance of occurring smaller than 1/100.");
        writer.setting(WorldStandardValues.BIOME_RARITY_SCALE, this.BiomeRarityScale);

        writer.smallTitle("Biome lists");

        writer.comment("Don't forget to register your custom biomes first in CustomBiomes!");

        writer.comment("Biomes generated normal way. Names are case sensitive.");
        writer.setting(WorldStandardValues.NORMAL_BIOMES, this.NormalBiomes);

        writer.comment("Biomes generated in \"ice areas\". Names are case sensitive.");
        writer.setting(WorldStandardValues.ICE_BIOMES, this.IceBiomes);

        writer.comment("Biomes used as isles in other biomes. You must set IsleInBiome in biome config for each biome here. Biome name is case sensitive.");
        writer.setting(WorldStandardValues.ISLE_BIOMES, this.IsleBiomes);

        writer.comment("Biomes used as borders of other biomes. You must set BiomeIsBorder in biome config for each biome here. Biome name is case sensitive.");
        writer.setting(WorldStandardValues.BORDER_BIOMES, this.BorderBiomes);

        writer.smallTitle("Landmass settings (for NormalBiomes)");

        writer.comment("Land rarity from 100 to 1. If you set smaller than 90 and LandSize near 0 beware Big oceans.");
        writer.setting(WorldStandardValues.LAND_RARITY, this.LandRarity);

        writer.comment("Land size from 0 to GenerationDepth.");
        writer.setting(WorldStandardValues.LAND_SIZE, this.LandSize);

        writer.comment("Make land more fuzzy and make lakes. Must be from 0 to GenerationDepth - LandSize");
        writer.setting(WorldStandardValues.LAND_FUZZY, this.LandFuzzy);

        writer.smallTitle("Ice area settings (for IceBiomes)");

        writer.comment("Rarity of the \"ice areas\" from 100 to 1. 100 = ice world, 1 = no IceBiomes");
        writer.setting(WorldStandardValues.ICE_RARITY, this.IceRarity);

        writer.comment("Ice area size from 0 to GenerationDepth.");
        writer.setting(WorldStandardValues.ICE_SIZE, this.IceSize);

        writer.comment("Set this to false to stop the ocean from freezing near when an \"ice area\" intersects with an ocean.");
        writer.setting(WorldStandardValues.FROZEN_OCEAN, this.FrozenOcean);

        writer.smallTitle("Rivers");

        writer.comment("River rarity. Must be from 0 to GenerationDepth.");
        writer.setting(WorldStandardValues.RIVER_RARITY, this.riverRarity);

        writer.comment("River size from 0 to GenerationDepth - RiverRarity");
        writer.setting(WorldStandardValues.RIVER_SIZE, this.riverSize);

        writer.comment("Set this to false to prevent the river generator from doing anything.");
        writer.setting(WorldStandardValues.RIVERS_ENABLED, this.riversEnabled);

        writer.comment("When this is set to false, the standard river generator of Minecraft will be used.");
        writer.comment("This means that a technical biome, determined by the RiverBiome setting of the biome");
        writer.comment("the river is flowing through, will be used to generate the river.");
        writer.comment("");
        writer.comment("When enabled, the rivers won't use a technical biome in your world anymore, instead");
        writer.comment("you can control them using the river settings in the BiomeConfigs.");
        writer.setting(WorldStandardValues.IMPROVED_RIVERS, this.improvedRivers);

        writer.comment("When set to true the rivers will no longer follow biome border most of the time.");
        writer.setting(WorldStandardValues.RANDOM_RIVERS, this.randomRivers);

        // Settings for BiomeMode:FromImage
        writer.bigTitle("Settings for BiomeMode:FromImage");

        writer.comment("Possible modes when generator outside image boundaries: Repeat, ContinueNormal, FillEmpty");
        writer.comment("   Repeat - repeat image");
        writer.comment("   Mirror - advanced repeat image mode");
        writer.comment("   ContinueNormal - continue normal generation");
        writer.comment("   FillEmpty - fill by biome in \"ImageFillBiome settings\" ");
        writer.setting(WorldStandardValues.IMAGE_MODE, this.imageMode);

        writer.comment("Source png file for FromImage biome mode.");
        writer.setting(WorldStandardValues.IMAGE_FILE, this.imageFile);

        writer.comment("Where the png's north is oriented? Possible values: North, East, South, West");
        writer.comment("   North - the top of your picture if north (no any rotation)");
        writer.comment("   West - previous behavior (you should rotate png CCW manually)");
        writer.comment("   East - png should be rotated CW manually");
        writer.comment("   South - rotate png 180 degrees before generating world");
        writer.setting(WorldStandardValues.IMAGE_ORIENTATION, this.imageOrientation);

        writer.comment("Biome name for fill outside image boundaries with FillEmpty mode.");
        writer.setting(WorldStandardValues.IMAGE_FILL_BIOME, this.imageFillBiome);

        writer.comment("Shifts map position from x=0 and z=0 coordinates.");
        writer.setting(WorldStandardValues.IMAGE_X_OFFSET, this.imageXOffset);
        writer.setting(WorldStandardValues.IMAGE_Z_OFFSET, this.imageZOffset);

        // Terrain height and volatility
        writer.bigTitle("Terrain height and volatility");

        writer.comment("Scales the height of the world. Adding 1 to this doubles the");
        writer.comment("height of the terrain, substracting 1 to this halves the height");
        writer.comment("of the terrain. Values must be between 5 and 8, inclusive.");
        writer.setting(WorldStandardValues.WORLD_HEIGHT_SCALE_BITS, this.worldHeightScaleBits);

        writer.comment("Height cap of the base terrain. Setting this to 7 makes no terrain");
        writer.comment("generate above y = 2 ^ 7 = 128. Doesn't affect resources (trees, objects, etc.).");
        writer.comment("Values must be between 5 and 8, inclusive. Values may not be lower");
        writer.comment("than WorldHeightScaleBits.");
        writer.setting(WorldStandardValues.WORLD_HEIGHT_CAP_BITS, this.worldHeightCapBits);

        writer.comment("Can increase (values greater than 0) or decrease (values less than 0) how much the landscape is fractured horizontally.");
        writer.setting(WorldStandardValues.FRACTURE_HORIZONTAL, this.fractureHorizontal);

        writer.comment("Can increase (values greater than 0) or decrease (values less than 0) how much the landscape is fractured vertically.");
        writer.comment("Positive values will lead to large cliffs/overhangs, floating islands, and/or a cavern world depending on other settings.");
        writer.setting(WorldStandardValues.FRACTURE_VERTICAL, this.fractureVertical);

        // Blocks
        writer.bigTitle("Blocks");

        writer.comment("Attempts to replace all surface stone with biome surface block");
        writer.setting(WorldStandardValues.REMOVE_SURFACE_STONE, this.removeSurfaceStone);

        writer.comment("Disable bottom of map bedrock generation");
        writer.setting(WorldStandardValues.DISABLE_BEDROCK, this.disableBedrock);

        writer.comment("Enable ceiling of map bedrock generation");
        writer.setting(WorldStandardValues.CEILING_BEDROCK, this.ceilingBedrock);

        writer.comment("Make bottom layer of bedrock flat");
        writer.setting(WorldStandardValues.FLAT_BEDROCK, this.flatBedrock);

        writer.comment("Block used as bedrock. No block data allowed.");
        writer.setting(WorldStandardValues.BEDROCK_BLOCK, this.bedrockBlock);
        
        writer.comment("Set this to false to disable the bounds check during chunk population.");
        writer.comment("While this allows you to spawn larger objects, it also makes terrain generation");
        writer.comment("dependant on the direction you explored the world in.");
        writer.setting(WorldStandardValues.POPULATION_BOUNDS_CHECK, this.populationBoundsCheck);

        writer.smallTitle("Water and ice");
        writer.comment("Set water level. Every empty block under this level will be fill water or another block from WaterBlock ");
        writer.setting(WorldStandardValues.WATER_LEVEL_MAX, this.waterLevelMax);
        writer.setting(WorldStandardValues.WATER_LEVEL_MIN, this.waterLevelMin);

        writer.comment("Block used as water in WaterLevel. No block data allowed.");
        writer.setting(WorldStandardValues.WATER_BLOCK, this.waterBlock);

        writer.comment("BlockId used as ice. No block data allowed.");
        writer.setting(WorldStandardValues.ICE_BLOCK, this.iceBlock);

        writer.comment("Seed used for the resource generation. Can only be numeric. Set to 0 to use the world seed.");
        writer.setting(WorldStandardValues.RESOURCES_SEED, this.resourcesSeed);

        if (objectSpawnRatio != 1)
        {
            // Write the old objectSpawnRatio

            writer.comment("LEGACY setting for compability with old worlds. This setting should be kept at 1.");
            writer.comment("If the setting is set at 1, the setting will vanish from the config file. Readd it");
            writer.comment("manually with another value and it will be back.");
            writer.comment("");
            writer.comment("When using the UseWorld or UseBiome keyword for spawning custom objects, Terrain Control");
            writer.comment("spawns one of the possible custom objects. There is of course a chance that");
            writer.comment("the chosen object cannot spawn. This setting tells TC how many times it should");
            writer.comment("try to spawn that object.");
            writer.comment("This setting doesn't affect growing saplings anymore.");
            writer.setting(WorldStandardValues.OBJECT_SPAWN_RATIO, this.objectSpawnRatio);
        }

        // Structures
        writer.bigTitle("Structures");
        writer.comment("Generate-structures in the server.properties file is ignored by Terrain Control. Use these settings instead.");
        writer.comment("");

        // Strongholds
        writer.smallTitle("Strongholds");
        writer.comment("Set this to false to prevent the stronghold generator from doing anything.");
        writer.setting(WorldStandardValues.STRONGHOLDS_ENABLED, this.strongholdsEnabled);

        writer.comment("The number of strongholds in the world.");
        writer.setting(WorldStandardValues.STRONGHOLD_COUNT, this.strongholdCount);

        writer.comment("How far strongholds are from the spawn and other strongholds (minimum is 1.0, default is 32.0).");
        writer.setting(WorldStandardValues.STRONGHOLD_DISTANCE, this.strongholdDistance);

        writer.comment("How concentrated strongholds are around the spawn (minimum is 1, default is 3). Lower number, lower concentration.");
        writer.setting(WorldStandardValues.STRONGHOLD_SPREAD, this.strongholdSpread);

        // Villages
        writer.smallTitle("Villages");
        writer.comment("Whether the villages are enabled or not.");
        writer.setting(WorldStandardValues.VILLAGES_ENABLED, this.villagesEnabled);

        writer.comment("The size of the village. Larger is bigger. Normal worlds have 0 as default, superflat worlds 1.");
        writer.setting(WorldStandardValues.VILLAGE_SIZE, this.villageSize);

        writer.comment("The minimum distance between the village centers in chunks. Minimum value is 9.");
        writer.setting(WorldStandardValues.VILLAGE_DISTANCE, this.villageDistance);

        // Rare buildings
        writer.smallTitle("Rare buildings");
        writer.comment("Rare buildings are either desert pyramids, jungle temples or swamp huts.");

        writer.comment("Whether rare buildings are enabled.");
        writer.setting(WorldStandardValues.RARE_BUILDINGS_ENABLED, this.rareBuildingsEnabled);

        writer.comment("The minimum distance between rare buildings in chunks.");
        writer.setting(WorldStandardValues.MINIMUM_DISTANCE_BETWEEN_RARE_BUILDINGS, this.minimumDistanceBetweenRareBuildings);

        writer.comment("The maximum distance between rare buildings in chunks.");
        writer.setting(WorldStandardValues.MAXIMUM_DISTANCE_BETWEEN_RARE_BUILDINGS, this.maximumDistanceBetweenRareBuildings);

        // Other structures
        writer.smallTitle("Other structures");
        writer.setting(WorldStandardValues.MINESHAFTS_ENABLED, this.mineshaftsEnabled);
        writer.setting(WorldStandardValues.NETHER_FORTRESSES_ENABLED, this.netherFortressesEnabled);

        // Visual settings
        writer.bigTitle("Visual settings");
        writer.comment("Warning this section will work only for players with the single version of Terrain Control installed.");

        writer.comment("World fog color");
        writer.setting(WorldStandardValues.WORLD_FOG, this.WorldFog);

        writer.comment("World night fog color");
        writer.setting(WorldStandardValues.WORLD_NIGHT_FOG, this.WorldNightFog);

        // Cave settings (still using code from Bucyruss' BiomeTerrainMod)
        writer.bigTitle("Cave settings");

        writer.comment("This controls the odds that a given chunk will host a single cave and/or the start of a cave system.");
        writer.setting(WorldStandardValues.CAVE_RARITY, this.caveRarity);

        writer.comment("The number of times the cave generation algorithm will attempt to create single caves and cave");
        writer.comment("systems in the given chunk. This value is larger because the likelihood for the cave generation");
        writer.comment("algorithm to bailout is fairly high and it is used in a randomizer that trends towards lower");
        writer.comment("random numbers. With an input of 40 (default) the randomizer will result in an average random");
        writer.comment("result of 5 to 6. This can be turned off by setting evenCaveDistribution (below) to true.");
        writer.setting(WorldStandardValues.CAVE_FREQUENCY, this.caveFrequency);

        writer.comment("Sets the minimum and maximum altitudes at which caves will be generated. These values are");
        writer.comment("used in a randomizer that trends towards lower numbers so that caves become more frequent");
        writer.comment("the closer you get to the bottom of the map. Setting even cave distribution (above) to true");
        writer.comment("will turn off this randomizer and use a flat random number generator that will create an even");
        writer.comment("density of caves at all altitudes.");
        writer.setting(WorldStandardValues.CAVE_MIN_ALTITUDE, this.caveMinAltitude);
        writer.setting(WorldStandardValues.CAVE_MAX_ALTITUDE, this.caveMaxAltitude);

        writer.comment("The odds that the cave generation algorithm will generate a single cavern without an accompanying");
        writer.comment("cave system. Note that whenever the algorithm generates an individual cave it will also attempt to");
        writer.comment("generate a pocket of cave systems in the vicinity (no guarantee of connection or that the cave system");
        writer.comment("will actually be created).");
        writer.setting(WorldStandardValues.INDIVIDUAL_CAVE_RARITY, this.individualCaveRarity);

        writer.comment("The number of times the algorithm will attempt to start a cave system in a given chunk per cycle of");
        writer.comment("the cave generation algorithm (see cave frequency setting above). Note that setting this value too");
        writer.comment("high with an accompanying high cave frequency value can cause extremely long world generation time.");
        writer.setting(WorldStandardValues.CAVE_SYSTEM_FREQUENCY, this.caveSystemFrequency);

        writer.comment("This can be set to create an additional chance that a cave system pocket (a higher than normal");
        writer.comment("density of cave systems) being started in a given chunk. Normally, a cave pocket will only be");
        writer.comment("attempted if an individual cave is generated, but this will allow more cave pockets to be generated");
        writer.comment("in addition to the individual cave trigger.");
        writer.setting(WorldStandardValues.CAVE_SYSTEM_POCKET_CHANCE, this.caveSystemPocketChance);

        writer.comment("The minimum and maximum size that a cave system pocket can be. This modifies/overrides the");
        writer.comment("cave system frequency setting (above) when triggered.");
        writer.setting(WorldStandardValues.CAVE_SYSTEM_POCKET_MIN_SIZE, this.caveSystemPocketMinSize);
        writer.setting(WorldStandardValues.CAVE_SYSTEM_POCKET_MAX_SIZE, this.caveSystemPocketMaxSize);

        writer.comment("Setting this to true will turn off the randomizer for cave frequency (above). Do note that");
        writer.comment("if you turn this on you will probably want to adjust the cave frequency down to avoid long");
        writer.comment("load times at world creation.");
        writer.setting(WorldStandardValues.EVEN_CAVE_DISTRIBUTION, this.evenCaveDistribution);

        // Canyon settings
        writer.bigTitle("Canyon settings");
        writer.setting(WorldStandardValues.CANYON_RARITY, this.canyonRarity);
        writer.setting(WorldStandardValues.CANYON_MIN_ALTITUDE, this.canyonMinAltitude);
        writer.setting(WorldStandardValues.CANYON_MAX_ALTITUDE, this.canyonMaxAltitude);
        writer.setting(WorldStandardValues.CANYON_MIN_LENGTH, this.canyonMinLength);
        writer.setting(WorldStandardValues.CANYON_MAX_LENGTH, this.canyonMaxLength);
        writer.setting(WorldStandardValues.CANYON_DEPTH, this.canyonDepth);

        // Settings for BiomeMode:OldGenerator
        writer.bigTitle("Settings for BiomeMode:OldGenerator");
        writer.comment("This generator works only with old terrain generator!");
        writer.setting(WorldStandardValues.OLD_BIOME_SIZE, this.oldBiomeSize);
        writer.setting(WorldStandardValues.MIN_MOISTURE, this.minMoisture);
        writer.setting(WorldStandardValues.MAX_MOISTURE, this.maxMoisture);
        writer.setting(WorldStandardValues.MIN_TEMPERATURE, this.minTemperature);
        writer.setting(WorldStandardValues.MAX_TEMPERATURE, this.maxTemperature);

    }

    private void WriteCustomBiomes(SettingsWriter writer) throws IOException
    {
        List<String> output = new ArrayList<String>();
        // Custom biome id
        List<Entry<String, Integer>> cbi = new ArrayList<Entry<String, Integer>>(this.customBiomeGenerationIds.entrySet());
        Collections.sort(cbi, CBV);
        // Print all custom biomes
        for (Iterator<Entry<String, Integer>> it = cbi.iterator(); it.hasNext();)
        {
            Entry<String, Integer> entry = it.next();
            output.add(entry.getKey() + ":" + entry.getValue());
        }
        writer.setting(WorldStandardValues.CUSTOM_BIOMES, output);
    }

    public double getFractureHorizontal()
    {
        return this.fractureHorizontal < 0.0D ? 1.0D / (Math.abs(this.fractureHorizontal) + 1.0D) : this.fractureHorizontal + 1.0D;
    }

    public double getFractureVertical()
    {
        return this.fractureVertical < 0.0D ? 1.0D / (Math.abs(this.fractureVertical) + 1.0D) : this.fractureVertical + 1.0D;
    }

    public boolean createAdminium(int y)
    {
        return (!this.disableBedrock) && ((!this.flatBedrock) || (y == 0));
    }

    public enum TerrainMode
    {
        Normal,
        OldGenerator,
        TerrainTest,
        NotGenerate,
        Default
    }

    public enum ImageMode
    {
        Repeat,
        Mirror,
        ContinueNormal,
        FillEmpty,
    }

    public enum ImageOrientation
    {
        North,
        East,
        South,
        West,
    }

    public enum ConfigMode
    {
        WriteAll,
        WriteDisable,
        WriteWithoutComments
    }
}
