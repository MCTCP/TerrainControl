package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
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
    private final Comparator<Entry<String, BiomeIds>> CBV = new Comparator<Entry<String, BiomeIds>>()
    {
        @Override
        public int compare(Entry<String, BiomeIds> o1, Entry<String, BiomeIds> o2)
        {
            return o1.getValue().getSavedId() - o2.getValue().getSavedId();
        }
    };

    public Map<String, BiomeIds> CustomBiomeIds = new HashMap<String, BiomeIds>();

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
    public WorldConfig(File settingsDir, LocalWorld world)
    {
        super(world.getName(), new File(settingsDir, WorldStandardValues.WORLD_CONFIG_FILE_NAME));
        this.settingsDir = settingsDir;

        // Read the WorldConfig file
        this.readSettingsFile();
        // Fix older names
        this.renameOldSettings();
        // Set the local fields based on what was read from the file
        this.readConfigSettings();
        // Clamp Settings to acceptable values
        this.correctSettings();

        ReadWorldCustomObjects();

        // Check biome ids, These are the names from the worldConfig file
        // Corrects any instances of incorrect biome id.
        for (String biomeName : CustomBiomeIds.keySet())
        {
            if (CustomBiomeIds.get(biomeName).getSavedId() == -1)
            {
                CustomBiomeIds.put(biomeName, new BiomeIds(world.getFreeBiomeId()));
            }
        }

        // Output to file
        if (this.SettingsMode != ConfigMode.WriteDisable)
        {
            this.writeSettingsFile(this.SettingsMode == ConfigMode.WriteAll);
        }
    }

    /**
     * Creates an empty WorldConfig with no settings initialized. Used to read
     * the WorldConfig from the TC network packet.
     * 
     * @param world The LocalWorld instance.
     */
    public WorldConfig(LocalWorld world)
    {
        super(world.getName(), null);
        this.settingsDir = null;
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

        NormalBiomes = filterBiomes(NormalBiomes, CustomBiomeIds.keySet());
        IceBiomes = filterBiomes(IceBiomes, CustomBiomeIds.keySet());
        IsleBiomes = filterBiomes(IsleBiomes, CustomBiomeIds.keySet());
        BorderBiomes = filterBiomes(BorderBiomes, CustomBiomeIds.keySet());

        if (biomeMode == TerrainControl.getBiomeModeManager().FROM_IMAGE)
        {
            File mapFile = new File(settingsDir, imageFile);
            if (!mapFile.exists())
            {
                TerrainControl.log(LogMarker.WARN, "Biome map file not found. Switching BiomeMode to Normal");
                biomeMode = TerrainControl.getBiomeModeManager().NORMAL;
            }
        }

        imageFillBiome = (DefaultBiome.Contain(imageFillBiome) || CustomBiomeIds.keySet().contains(imageFillBiome)) ? imageFillBiome : WorldStandardValues.IMAGE_FILL_BIOME.getDefaultValue();

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
                    CustomBiomeIds.put(keys[0], new BiomeIds(generationBiomeId));
                } else
                {
                    CustomBiomeIds.put(keys[0], new BiomeIds(-1));
                }

            } catch (NumberFormatException e)
            {
                System.out.println("Wrong custom biome id settings: '" + biome + "'");
            }

        }

    }

    @Override
    protected void writeConfigSettings() throws IOException
    {
        // The modes
        writeBigTitle("The modes");
        writeComment("What Terrain Control does with the config files.");
        writeComment("Possible modes: WriteAll, WriteWithoutComments, WriteDisable");
        writeComment("   WriteAll - default");
        writeComment("   WriteWithoutComments - write config files without help comments");
        writeComment("   WriteDisable - doesn't write to the config files, it only reads. Doesn't auto-update the configs. Use with care!");
        writeValue(WorldStandardValues.SETTINGS_MODE, this.SettingsMode);

        writeComment("Possible terrain modes: Normal, OldGenerator, TerrainTest, NotGenerate, Default");
        writeComment("   Normal - use all features");
        writeComment("   OldGenerator - generate land like Beta 1.7.3 generator");
        writeComment("   TerrainTest - generate only terrain without any resources");
        writeComment("   NotGenerate - generate empty chunks");
        writeComment("   Default - use default terrain generator");
        writeValue(WorldStandardValues.TERRAIN_MODE, this.ModeTerrain);

        writeComment("Possible biome modes: Normal, OldGenerator, Default");
        writeComment("   Normal - use all features");
        writeComment("   FromImage - get biomes from image file");
        writeComment("   OldGenerator - generate biome like the Beta 1.7.3 generator");
        writeComment("   Default - use default Notch biome generator");
        writeValue(WorldStandardValues.BIOME_MODE, TerrainControl.getBiomeModeManager().getName(biomeMode));

        // Custom biomes
        writeBigTitle("Custom biomes");
        writeComment("You need to register your custom biomes here. This setting will make Terrain Control");
        writeComment("generate setting files for them. However, it won't place them in the world automatically.");
        writeComment("See the settings for your BiomeMode below on how to add them to the world.");
        writeComment("");
        writeComment("Syntax: CustomBiomes:BiomeName:id[,AnotherBiomeName:id[,...]]");
        writeComment("Example: CustomBiomes:TestBiome1:30,BiomeTest2:31");
        writeComment("This will add two biomes and generate the BiomeConfigs for them.");
        writeComment("All changes here need a server restart.");
        writeComment("");
        writeComment("Due to the way Mojang's loading code works, all biome ids need to be unique");
        writeComment("on the server. If you don't do this, the client will display the biomes just fine,");
        writeComment("but the server can think it is another biome with the same id. This will cause saplings,");
        writeComment("snowfall and mobs to work as in the other biome.");
        writeComment("");
        writeComment("The available ids range from 0 to 1023 and the ids 0-39 and 129-167 are taken by vanilla.");
        writeComment("The ids 256-1023 cannot be saved to the map files, so use ReplaceToBiomeName in that biome.");

        WriteCustomBiomes();

        // Settings for BiomeMode:Normal
        writeBigTitle("Settings for BiomeMode:Normal");
        writeComment("Also applies if you are using BiomeMode:FromImage and ImageMode:ContinueNormal.");

        writeComment("Important value for generation. Bigger values appear to zoom out. All 'Sizes' must be smaller than this.");
        writeComment("Large %/total area biomes (Continents) must be set small, (limit=0)");
        writeComment("Small %/total area biomes (Oasis,Mountain Peaks) must be larger (limit=GenerationDepth)");
        writeComment("This could also represent \"Total number of biome sizes\" ");
        writeComment("Small values (about 1-2) and Large values (about 20) may affect generator performance.");
        writeValue(WorldStandardValues.GENERATION_DEPTH, this.GenerationDepth);

        writeComment("Max biome rarity from 1 to infinity. By default this is 100, but you can raise it for");
        writeComment("fine-grained control, or to create biomes with a chance of occurring smaller than 1/100.");
        writeValue(WorldStandardValues.BIOME_RARITY_SCALE, this.BiomeRarityScale);

        writeSmallTitle("Biome lists");

        writeComment("Don't forget to register your custom biomes first in CustomBiomes!");

        writeComment("Biomes generated normal way. Names are case sensitive.");
        writeValue(WorldStandardValues.NORMAL_BIOMES, this.NormalBiomes);

        writeComment("Biomes generated in \"ice areas\". Names are case sensitive.");
        writeValue(WorldStandardValues.ICE_BIOMES, this.IceBiomes);

        writeComment("Biomes used as isles in other biomes. You must set IsleInBiome in biome config for each biome here. Biome name is case sensitive.");
        writeValue(WorldStandardValues.ISLE_BIOMES, this.IsleBiomes);

        writeComment("Biomes used as borders of other biomes. You must set BiomeIsBorder in biome config for each biome here. Biome name is case sensitive.");
        writeValue(WorldStandardValues.BORDER_BIOMES, this.BorderBiomes);

        writeSmallTitle("Landmass settings (for NormalBiomes)");

        writeComment("Land rarity from 100 to 1. If you set smaller than 90 and LandSize near 0 beware Big oceans.");
        writeValue(WorldStandardValues.LAND_RARITY, this.LandRarity);

        writeComment("Land size from 0 to GenerationDepth.");
        writeValue(WorldStandardValues.LAND_SIZE, this.LandSize);

        writeComment("Make land more fuzzy and make lakes. Must be from 0 to GenerationDepth - LandSize");
        writeValue(WorldStandardValues.LAND_FUZZY, this.LandFuzzy);

        writeSmallTitle("Ice area settings (for IceBiomes)");

        writeComment("Rarity of the \"ice areas\" from 100 to 1. 100 = ice world, 1 = no IceBiomes");
        writeValue(WorldStandardValues.ICE_RARITY, this.IceRarity);

        writeComment("Ice area size from 0 to GenerationDepth.");
        writeValue(WorldStandardValues.ICE_SIZE, this.IceSize);

        writeComment("Set this to false to stop the ocean from freezing near when an \"ice area\" intersects with an ocean.");
        writeValue(WorldStandardValues.FROZEN_OCEAN, this.FrozenOcean);

        writeSmallTitle("Rivers");

        writeComment("River rarity. Must be from 0 to GenerationDepth.");
        writeValue(WorldStandardValues.RIVER_RARITY, this.riverRarity);

        writeComment("River size from 0 to GenerationDepth - RiverRarity");
        writeValue(WorldStandardValues.RIVER_SIZE, this.riverSize);

        writeComment("Set this to false to prevent the river generator from doing anything.");
        writeValue(WorldStandardValues.RIVERS_ENABLED, this.riversEnabled);

        writeComment("When this is set to false, the standard river generator of Minecraft will be used.");
        writeComment("This means that a technical biome, determined by the RiverBiome setting of the biome");
        writeComment("the river is flowing through, will be used to generate the river.");
        writeComment("");
        writeComment("When enabled, the rivers won't use a technical biome in your world anymore, instead");
        writeComment("you can control them using the river settings in the BiomeConfigs.");
        writeValue(WorldStandardValues.IMPROVED_RIVERS, this.improvedRivers);

        writeComment("When set to true the rivers will no longer follow biome border most of the time.");
        writeValue(WorldStandardValues.RANDOM_RIVERS, this.randomRivers);

        // Settings for BiomeMode:FromImage
        writeBigTitle("Settings for BiomeMode:FromImage");

        writeComment("Possible modes when generator outside image boundaries: Repeat, ContinueNormal, FillEmpty");
        writeComment("   Repeat - repeat image");
        writeComment("   Mirror - advanced repeat image mode");
        writeComment("   ContinueNormal - continue normal generation");
        writeComment("   FillEmpty - fill by biome in \"ImageFillBiome settings\" ");
        writeValue(WorldStandardValues.IMAGE_MODE, this.imageMode);

        writeComment("Source png file for FromImage biome mode.");
        writeValue(WorldStandardValues.IMAGE_FILE, this.imageFile);

        writeComment("Where the png's north is oriented? Possible values: North, East, South, West");
        writeComment("   North - the top of your picture if north (no any rotation)");
        writeComment("   West - previous behavior (you should rotate png CCW manually)");
        writeComment("   East - png should be rotated CW manually");
        writeComment("   South - rotate png 180 degrees before generating world");
        writeValue(WorldStandardValues.IMAGE_ORIENTATION, this.imageOrientation);

        writeComment("Biome name for fill outside image boundaries with FillEmpty mode.");
        writeValue(WorldStandardValues.IMAGE_FILL_BIOME, this.imageFillBiome);

        writeComment("Shifts map position from x=0 and z=0 coordinates.");
        writeValue(WorldStandardValues.IMAGE_X_OFFSET, this.imageXOffset);
        writeValue(WorldStandardValues.IMAGE_Z_OFFSET, this.imageZOffset);

        // Terrain height and volatility
        writeBigTitle("Terrain height and volatility");

        writeComment("Scales the height of the world. Adding 1 to this doubles the");
        writeComment("height of the terrain, substracting 1 to this halves the height");
        writeComment("of the terrain. Values must be between 5 and 8, inclusive.");
        writeValue(WorldStandardValues.WORLD_HEIGHT_SCALE_BITS, this.worldHeightScaleBits);

        writeComment("Height cap of the base terrain. Setting this to 7 makes no terrain");
        writeComment("generate above y = 2 ^ 7 = 128. Doesn't affect resources (trees, objects, etc.).");
        writeComment("Values must be between 5 and 8, inclusive. Values may not be lower");
        writeComment("than WorldHeightScaleBits.");
        writeValue(WorldStandardValues.WORLD_HEIGHT_CAP_BITS, this.worldHeightCapBits);

        writeComment("Can increase (values greater than 0) or decrease (values less than 0) how much the landscape is fractured horizontally.");
        writeValue(WorldStandardValues.FRACTURE_HORIZONTAL, this.fractureHorizontal);

        writeComment("Can increase (values greater than 0) or decrease (values less than 0) how much the landscape is fractured vertically.");
        writeComment("Positive values will lead to large cliffs/overhangs, floating islands, and/or a cavern world depending on other settings.");
        writeValue(WorldStandardValues.FRACTURE_VERTICAL, this.fractureVertical);

        // Blocks
        writeBigTitle("Blocks");

        writeComment("Attempts to replace all surface stone with biome surface block");
        writeValue(WorldStandardValues.REMOVE_SURFACE_STONE, this.removeSurfaceStone);

        writeComment("Disable bottom of map bedrock generation");
        writeValue(WorldStandardValues.DISABLE_BEDROCK, this.disableBedrock);

        writeComment("Enable ceiling of map bedrock generation");
        writeValue(WorldStandardValues.CEILING_BEDROCK, this.ceilingBedrock);

        writeComment("Make bottom layer of bedrock flat");
        writeValue(WorldStandardValues.FLAT_BEDROCK, this.flatBedrock);

        writeComment("Block used as bedrock. No block data allowed.");
        writeValue(WorldStandardValues.BEDROCK_BLOCK, this.bedrockBlock);
        
        writeComment("Set this to false to disable the bounds check during chunk population.");
        writeComment("While this allows you to spawn larger objects, it also makes terrain generation");
        writeComment("dependant on the direction you explored the world in.");
        writeValue(WorldStandardValues.POPULATION_BOUNDS_CHECK, this.populationBoundsCheck);

        this.writeSmallTitle("Water and ice");
        writeComment("Set water level. Every empty block under this level will be fill water or another block from WaterBlock ");
        writeValue(WorldStandardValues.WATER_LEVEL_MAX, this.waterLevelMax);
        writeValue(WorldStandardValues.WATER_LEVEL_MIN, this.waterLevelMin);

        writeComment("Block used as water in WaterLevel. No block data allowed.");
        writeValue(WorldStandardValues.WATER_BLOCK, this.waterBlock);

        writeComment("BlockId used as ice. No block data allowed.");
        writeValue(WorldStandardValues.ICE_BLOCK, this.iceBlock);

        writeComment("Seed used for the resource generation. Can only be numeric. Set to 0 to use the world seed.");
        writeValue(WorldStandardValues.RESOURCES_SEED, this.resourcesSeed);

        if (objectSpawnRatio != 1)
        {
            // Write the old objectSpawnRatio

            writeComment("LEGACY setting for compability with old worlds. This setting should be kept at 1.");
            writeComment("If the setting is set at 1, the setting will vanish from the config file. Readd it");
            writeComment("manually with another value and it will be back.");
            writeComment("");
            writeComment("When using the UseWorld or UseBiome keyword for spawning custom objects, Terrain Control");
            writeComment("spawns one of the possible custom objects. There is of course a chance that");
            writeComment("the chosen object cannot spawn. This setting tells TC how many times it should");
            writeComment("try to spawn that object.");
            writeComment("This setting doesn't affect growing saplings anymore.");
            this.writeValue(WorldStandardValues.OBJECT_SPAWN_RATIO, this.objectSpawnRatio);
        }

        // Structures
        writeBigTitle("Structures");
        writeComment("Generate-structures in the server.properties file is ignored by Terrain Control. Use these settings instead.");
        writeComment("");

        // Strongholds
        writeSmallTitle("Strongholds");
        writeComment("Set this to false to prevent the stronghold generator from doing anything.");
        writeValue(WorldStandardValues.STRONGHOLDS_ENABLED, this.strongholdsEnabled);

        writeComment("The number of strongholds in the world.");
        writeValue(WorldStandardValues.STRONGHOLD_COUNT, this.strongholdCount);

        writeComment("How far strongholds are from the spawn and other strongholds (minimum is 1.0, default is 32.0).");
        writeValue(WorldStandardValues.STRONGHOLD_DISTANCE, this.strongholdDistance);

        writeComment("How concentrated strongholds are around the spawn (minimum is 1, default is 3). Lower number, lower concentration.");
        writeValue(WorldStandardValues.STRONGHOLD_SPREAD, this.strongholdSpread);

        // Villages
        writeSmallTitle("Villages");
        writeComment("Whether the villages are enabled or not.");
        writeValue(WorldStandardValues.VILLAGES_ENABLED, this.villagesEnabled);

        writeComment("The size of the village. Larger is bigger. Normal worlds have 0 as default, superflat worlds 1.");
        writeValue(WorldStandardValues.VILLAGE_SIZE, this.villageSize);

        writeComment("The minimum distance between the village centers in chunks. Minimum value is 9.");
        writeValue(WorldStandardValues.VILLAGE_DISTANCE, this.villageDistance);

        // Rare buildings
        writeSmallTitle("Rare buildings");
        writeComment("Rare buildings are either desert pyramids, jungle temples or swamp huts.");

        writeComment("Whether rare buildings are enabled.");
        writeValue(WorldStandardValues.RARE_BUILDINGS_ENABLED, this.rareBuildingsEnabled);

        writeComment("The minimum distance between rare buildings in chunks.");
        writeValue(WorldStandardValues.MINIMUM_DISTANCE_BETWEEN_RARE_BUILDINGS, this.minimumDistanceBetweenRareBuildings);

        writeComment("The maximum distance between rare buildings in chunks.");
        writeValue(WorldStandardValues.MAXIMUM_DISTANCE_BETWEEN_RARE_BUILDINGS, this.maximumDistanceBetweenRareBuildings);

        // Other structures
        writeSmallTitle("Other structures");
        writeValue(WorldStandardValues.MINESHAFTS_ENABLED, this.mineshaftsEnabled);
        writeValue(WorldStandardValues.NETHER_FORTRESSES_ENABLED, this.netherFortressesEnabled);

        // Visual settings
        this.writeBigTitle("Visual settings");
        this.writeComment("Warning this section will work only for players with the single version of Terrain Control installed.");

        writeComment("World fog color");
        writeValue(WorldStandardValues.WORLD_FOG, this.WorldFog);

        writeComment("World night fog color");
        writeValue(WorldStandardValues.WORLD_NIGHT_FOG, this.WorldNightFog);

        // Cave settings (still using code from Bucyruss' BiomeTerrainMod)
        writeBigTitle("Cave settings");

        writeComment("This controls the odds that a given chunk will host a single cave and/or the start of a cave system.");
        writeValue(WorldStandardValues.CAVE_RARITY, this.caveRarity);

        writeComment("The number of times the cave generation algorithm will attempt to create single caves and cave");
        writeComment("systems in the given chunk. This value is larger because the likelihood for the cave generation");
        writeComment("algorithm to bailout is fairly high and it is used in a randomizer that trends towards lower");
        writeComment("random numbers. With an input of 40 (default) the randomizer will result in an average random");
        writeComment("result of 5 to 6. This can be turned off by setting evenCaveDistribution (below) to true.");
        writeValue(WorldStandardValues.CAVE_FREQUENCY, this.caveFrequency);

        writeComment("Sets the minimum and maximum altitudes at which caves will be generated. These values are");
        writeComment("used in a randomizer that trends towards lower numbers so that caves become more frequent");
        writeComment("the closer you get to the bottom of the map. Setting even cave distribution (above) to true");
        writeComment("will turn off this randomizer and use a flat random number generator that will create an even");
        writeComment("density of caves at all altitudes.");
        writeValue(WorldStandardValues.CAVE_MIN_ALTITUDE, this.caveMinAltitude);
        writeValue(WorldStandardValues.CAVE_MAX_ALTITUDE, this.caveMaxAltitude);

        writeComment("The odds that the cave generation algorithm will generate a single cavern without an accompanying");
        writeComment("cave system. Note that whenever the algorithm generates an individual cave it will also attempt to");
        writeComment("generate a pocket of cave systems in the vicinity (no guarantee of connection or that the cave system");
        writeComment("will actually be created).");
        writeValue(WorldStandardValues.INDIVIDUAL_CAVE_RARITY, this.individualCaveRarity);

        writeComment("The number of times the algorithm will attempt to start a cave system in a given chunk per cycle of");
        writeComment("the cave generation algorithm (see cave frequency setting above). Note that setting this value too");
        writeComment("high with an accompanying high cave frequency value can cause extremely long world generation time.");
        writeValue(WorldStandardValues.CAVE_SYSTEM_FREQUENCY, this.caveSystemFrequency);

        writeComment("This can be set to create an additional chance that a cave system pocket (a higher than normal");
        writeComment("density of cave systems) being started in a given chunk. Normally, a cave pocket will only be");
        writeComment("attempted if an individual cave is generated, but this will allow more cave pockets to be generated");
        writeComment("in addition to the individual cave trigger.");
        writeValue(WorldStandardValues.CAVE_SYSTEM_POCKET_CHANCE, this.caveSystemPocketChance);

        writeComment("The minimum and maximum size that a cave system pocket can be. This modifies/overrides the");
        writeComment("cave system frequency setting (above) when triggered.");
        writeValue(WorldStandardValues.CAVE_SYSTEM_POCKET_MIN_SIZE, this.caveSystemPocketMinSize);
        writeValue(WorldStandardValues.CAVE_SYSTEM_POCKET_MAX_SIZE, this.caveSystemPocketMaxSize);

        writeComment("Setting this to true will turn off the randomizer for cave frequency (above). Do note that");
        writeComment("if you turn this on you will probably want to adjust the cave frequency down to avoid long");
        writeComment("load times at world creation.");
        writeValue(WorldStandardValues.EVEN_CAVE_DISTRIBUTION, this.evenCaveDistribution);

        // Canyon settings
        writeBigTitle("Canyon settings");
        writeValue(WorldStandardValues.CANYON_RARITY, this.canyonRarity);
        writeValue(WorldStandardValues.CANYON_MIN_ALTITUDE, this.canyonMinAltitude);
        writeValue(WorldStandardValues.CANYON_MAX_ALTITUDE, this.canyonMaxAltitude);
        writeValue(WorldStandardValues.CANYON_MIN_LENGTH, this.canyonMinLength);
        writeValue(WorldStandardValues.CANYON_MAX_LENGTH, this.canyonMaxLength);
        writeValue(WorldStandardValues.CANYON_DEPTH, this.canyonDepth);

        // Settings for BiomeMode:OldGenerator
        writeBigTitle("Settings for BiomeMode:OldGenerator");
        writeComment("This generator works only with old terrain generator!");
        writeValue(WorldStandardValues.OLD_BIOME_SIZE, this.oldBiomeSize);
        writeValue(WorldStandardValues.MIN_MOISTURE, this.minMoisture);
        writeValue(WorldStandardValues.MAX_MOISTURE, this.maxMoisture);
        writeValue(WorldStandardValues.MIN_TEMPERATURE, this.minTemperature);
        writeValue(WorldStandardValues.MAX_TEMPERATURE, this.maxTemperature);

    }

    private void WriteCustomBiomes() throws IOException
    {
        List<String> output = new ArrayList<String>();
        // Custom biome id
        List<Entry<String, BiomeIds>> cbi = new ArrayList<Entry<String, BiomeIds>>(this.CustomBiomeIds.entrySet());
        Collections.sort(cbi, CBV);
        // Print all custom biomes
        for (Iterator<Entry<String, BiomeIds>> it = cbi.iterator(); it.hasNext();)
        {
            Entry<String, BiomeIds> entry = it.next();
            output.add(entry.getKey() + ":" + entry.getValue().getGenerationId());
        }
        writeValue(WorldStandardValues.CUSTOM_BIOMES, output);
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
