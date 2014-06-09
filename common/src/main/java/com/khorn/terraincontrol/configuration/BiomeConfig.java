package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ReplacedBlocksMatrix.ReplacedBlocksInstruction;
import com.khorn.terraincontrol.configuration.WorldConfig.ConfigMode;
import com.khorn.terraincontrol.configuration.io.FileSettingsWriter;
import com.khorn.terraincontrol.configuration.io.SettingsReader;
import com.khorn.terraincontrol.configuration.io.SettingsWriter;
import com.khorn.terraincontrol.configuration.settingType.Setting;
import com.khorn.terraincontrol.configuration.standard.BiomeStandardValues;
import com.khorn.terraincontrol.configuration.standard.StandardBiomeTemplate;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.UseBiome;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.generator.resource.*;
import com.khorn.terraincontrol.generator.surface.NullSurfaceGenerator;
import com.khorn.terraincontrol.generator.surface.SurfaceGenerator;
import com.khorn.terraincontrol.util.helpers.StringHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;
import com.khorn.terraincontrol.util.minecraftTypes.TreeType;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class BiomeConfig extends ConfigFile
{
    /**
     * Biome Inheritance: String name of the biome to extend
     */
    public String biomeExtends;
    public boolean biomeExtendsProcessed = false;

    private boolean processHasRun = false;
    private boolean doResourceInheritance = true;

    public String riverBiome;
    public float riverHeight;
    public float riverVolatility;
    public double[] riverHeightMatrix;

    public int biomeSize;
    public int biomeRarity;

    public int biomeColor;

    public List<String> biomeIsBorder;
    public List<String> isleInBiome;
    public List<String> notBorderNear;

    // Surface config
    public float biomeHeight;
    public float biomeVolatility;
    public int smoothRadius;

    public float biomeTemperature;
    public float biomeWetness;

    public LocalMaterialData stoneBlock;
    public LocalMaterialData surfaceBlock;
    public LocalMaterialData groundBlock;
    public ReplacedBlocksMatrix replacedBlocks;
    public SurfaceGenerator surfaceAndGroundControl;

    public String replaceToBiomeName;

    public boolean useWorldWaterLevel;
    public int waterLevelMax;
    public int waterLevelMin;
    public LocalMaterialData waterBlock;
    public LocalMaterialData iceBlock;
    public int riverWaterLevel;

    private int configWaterLevelMax;
    private int configWaterLevelMin;
    private LocalMaterialData configWaterBlock;
    private LocalMaterialData configIceBlock;
    private int configRiverWaterLevel;

    public int skyColor;
    public int waterColor;

    public int grassColor;
    public boolean grassColorIsMultiplier;
    public int foliageColor;
    public boolean foliageColorIsMultiplier;

    public List<Resource> resourceSequence = new ArrayList<Resource>();

    private Map<SaplingType, SaplingGen> saplingGrowers = new EnumMap<SaplingType, SaplingGen>(SaplingType.class);

    public ArrayList<CustomObject> biomeObjects;
    public CustomStructureGen structureGen;
    public ArrayList<String> biomeObjectStrings;

    public double maxAverageHeight;
    public double maxAverageDepth;
    public double volatility1;
    public double volatility2;
    public double volatilityWeight1;
    public double volatilityWeight2;
    private double volatilityRaw1;
    private double volatilityRaw2;
    private double volatilityWeightRaw1;
    private double volatilityWeightRaw2;
    public boolean disableNotchHeightControl;
    public double[] heightMatrix;

    // Structures
    public boolean strongholdsEnabled;
    public boolean netherFortressesEnabled;

    public enum VillageType
    {

        disabled,
        wood,
        sandstone

    }

    public VillageType villageType;
    public double mineshaftsRarity;

    public enum RareBuildingType
    {

        disabled,
        desertPyramid,
        jungleTemple,
        swampHut

    }

    public RareBuildingType rareBuildingType;

    public final int generationId;
    public StandardBiomeTemplate defaultSettings;
    public WorldConfig worldConfig;

    // Spawn Config
    public boolean spawnMonstersAddDefaults = true;
    public List<WeightedMobSpawnGroup> spawnMonsters = new ArrayList<WeightedMobSpawnGroup>();
    public boolean spawnCreaturesAddDefaults = true;
    public List<WeightedMobSpawnGroup> spawnCreatures = new ArrayList<WeightedMobSpawnGroup>();
    public boolean spawnWaterCreaturesAddDefaults = true;
    public List<WeightedMobSpawnGroup> spawnWaterCreatures = new ArrayList<WeightedMobSpawnGroup>();
    public boolean spawnAmbientCreaturesAddDefaults = true;
    public List<WeightedMobSpawnGroup> spawnAmbientCreatures = new ArrayList<WeightedMobSpawnGroup>();

    public BiomeConfig(SettingsReader reader, BiomeLoadInstruction loadInstruction, WorldConfig worldConfig)
    {
        super(reader);
        this.generationId = loadInstruction.getGenerationId();
        this.worldConfig = worldConfig;
        this.defaultSettings = loadInstruction.getBiomeTemplate();

        // Read this setting early, before inheritance is applied
        this.biomeExtends = readSettings(BiomeStandardValues.BIOME_EXTENDS, defaultSettings.defaultExtends);
    }

    public void process()
    {
        if (!processHasRun)
        {
            this.processHasRun = true;
            this.renameOldSettings();
            this.readConfigSettings();

            this.correctSettings();

            // Add default resources when needed
            if (this.isNewConfig)
            {
                this.resourceSequence.addAll(defaultSettings.createDefaultResources(this));
            }

            // Set water level
            if (this.useWorldWaterLevel)
            {
                this.waterLevelMax = worldConfig.waterLevelMax;
                this.waterLevelMin = worldConfig.waterLevelMin;
                this.waterBlock = worldConfig.waterBlock;
                this.iceBlock = worldConfig.iceBlock;
                this.riverWaterLevel = worldConfig.waterLevelMax;
            } else
            {
                this.waterLevelMax = this.configWaterLevelMax;
                this.waterLevelMin = this.configWaterLevelMin;
                this.waterBlock = this.configWaterBlock;
                this.iceBlock = this.configIceBlock;
                this.riverWaterLevel = this.configRiverWaterLevel;
            }
        }
    }

    public void outputToFile()
    {
        if (!processHasRun)
        {
            throw new IllegalStateException("Run process() first!");
        }
        if (!this.biomeExtends.isEmpty())
        {
            // Child Inheritance Biomes
            File inheritedFile = new File(getFile().getAbsolutePath() + ".inherited");
            FileSettingsWriter.writeToFile(this, inheritedFile, ConfigMode.WriteAll);
        } else
        {
            // Normal config saving
            FileSettingsWriter.writeToFile(this, worldConfig.SettingsMode);
        }
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

    @Override
    protected void readConfigSettings()
    {
        this.doResourceInheritance = readSettings(BiomeStandardValues.RESOURCE_INHERITANCE);
        this.biomeSize = readSettings(BiomeStandardValues.BIOME_SIZE, defaultSettings.defaultSize);
        this.biomeRarity = readSettings(BiomeStandardValues.BIOME_RARITY, defaultSettings.defaultRarity);

        this.biomeColor = readSettings(BiomeStandardValues.BIOME_COLOR, defaultSettings.defaultColor);

        this.riverBiome = readSettings(BiomeStandardValues.RIVER_BIOME, defaultSettings.defaultRiverBiome);

        this.isleInBiome = readSettings(BiomeStandardValues.ISLE_IN_BIOME, defaultSettings.defaultIsle);
        this.biomeIsBorder = readSettings(BiomeStandardValues.BIOME_IS_BORDER, defaultSettings.defaultBorder);
        this.notBorderNear = readSettings(BiomeStandardValues.NOT_BORDER_NEAR, defaultSettings.defaultNotBorderNear);

        this.biomeTemperature = readSettings(BiomeStandardValues.BIOME_TEMPERATURE, defaultSettings.defaultBiomeTemperature);
        this.biomeWetness = readSettings(BiomeStandardValues.BIOME_WETNESS, defaultSettings.defaultBiomeWetness);

        if (this.defaultSettings.isCustomBiome)
        {
            this.replaceToBiomeName = readSettings(BiomeStandardValues.REPLACE_TO_BIOME_NAME);
        } else
        {
            this.replaceToBiomeName = "";
        }

        this.biomeHeight = readSettings(BiomeStandardValues.BIOME_HEIGHT, defaultSettings.defaultBiomeSurface);
        this.biomeVolatility = readSettings(BiomeStandardValues.BIOME_VOLATILITY, defaultSettings.defaultBiomeVolatility);
        this.smoothRadius = readSettings(BiomeStandardValues.SMOOTH_RADIUS);

        this.stoneBlock = readSettings(BiomeStandardValues.STONE_BLOCK);
        this.surfaceBlock = readSettings(BiomeStandardValues.SURFACE_BLOCK,
                TerrainControl.toLocalMaterialData(defaultSettings.defaultSurfaceBlock, 0));
        this.groundBlock = readSettings(BiomeStandardValues.GROUND_BLOCK,
                TerrainControl.toLocalMaterialData(defaultSettings.defaultGroundBlock, 0));
        this.replacedBlocks = readSettings(BiomeStandardValues.REPLACED_BLOCKS);
        this.surfaceAndGroundControl = readSurfaceAndGroundControlSettings();

        this.useWorldWaterLevel = readSettings(BiomeStandardValues.USE_WORLD_WATER_LEVEL);
        this.configWaterLevelMax = readSettings(BiomeStandardValues.WATER_LEVEL_MAX);
        this.configWaterLevelMin = readSettings(BiomeStandardValues.WATER_LEVEL_MIN);
        this.configWaterBlock = readSettings(BiomeStandardValues.WATER_BLOCK);
        this.configIceBlock = readSettings(BiomeStandardValues.ICE_BLOCK);

        this.skyColor = readSettings(BiomeStandardValues.SKY_COLOR);
        this.waterColor = readSettings(BiomeStandardValues.WATER_COLOR, defaultSettings.defaultWaterColorMultiplier);
        this.grassColor = readSettings(BiomeStandardValues.GRASS_COLOR, defaultSettings.defaultGrassColor);
        this.grassColorIsMultiplier = readSettings(BiomeStandardValues.GRASS_COLOR_IS_MULTIPLIER);
        this.foliageColor = readSettings(BiomeStandardValues.FOLIAGE_COLOR, defaultSettings.defaultFoliageColor);
        this.foliageColorIsMultiplier = readSettings(BiomeStandardValues.FOLIAGE_COLOR_IS_MULTIPLIER);

        this.volatilityRaw1 = readSettings(BiomeStandardValues.VOLATILITY_1);
        this.volatilityRaw2 = readSettings(BiomeStandardValues.VOLATILITY_2);
        this.volatilityWeightRaw1 = readSettings(BiomeStandardValues.VOLATILITY_WEIGHT_1);
        this.volatilityWeightRaw2 = readSettings(BiomeStandardValues.VOLATILITY_WEIGHT_2);
        this.disableNotchHeightControl = readSettings(BiomeStandardValues.DISABLE_BIOME_HEIGHT);
        this.maxAverageHeight = readSettings(BiomeStandardValues.MAX_AVERAGE_HEIGHT);
        this.maxAverageDepth = readSettings(BiomeStandardValues.MAX_AVERAGE_DEPTH);

        this.riverHeight = readSettings(BiomeStandardValues.RIVER_HEIGHT);
        this.riverVolatility = readSettings(BiomeStandardValues.RIVER_VOLATILITY);
        this.configRiverWaterLevel = readSettings(BiomeStandardValues.RIVER_WATER_LEVEL);

        this.strongholdsEnabled = readSettings(BiomeStandardValues.STRONGHOLDS_ENABLED, defaultSettings.defaultStrongholds);
        this.netherFortressesEnabled = readSettings(BiomeStandardValues.NETHER_FORTRESSES_ENABLED, true);
        this.villageType = readSettings(BiomeStandardValues.VILLAGE_TYPE, defaultSettings.defaultVillageType);
        this.mineshaftsRarity = readSettings(BiomeStandardValues.MINESHAFT_RARITY);
        this.rareBuildingType = readSettings(BiomeStandardValues.RARE_BUILDING_TYPE, defaultSettings.defaultRareBuildingType);

        if (this.defaultSettings.isCustomBiome)
        {
            // Only for custom biomes
            this.spawnMonstersAddDefaults = readSettings(BiomeStandardValues.SPAWN_MONSTERS_ADD_DEFAULTS);
            this.spawnMonsters = readSettings(BiomeStandardValues.SPAWN_MONSTERS);
            this.spawnCreaturesAddDefaults = readSettings(BiomeStandardValues.SPAWN_CREATURES_ADD_DEFAULTS);
            this.spawnCreatures = readSettings(BiomeStandardValues.SPAWN_CREATURES);
            this.spawnWaterCreaturesAddDefaults = readSettings(BiomeStandardValues.SPAWN_WATER_CREATURES_ADD_DEFAULTS);
            this.spawnWaterCreatures = readSettings(BiomeStandardValues.SPAWN_WATER_CREATURES);
            this.spawnAmbientCreaturesAddDefaults = readSettings(BiomeStandardValues.SPAWN_AMBIENT_CREATURES_ADD_DEFAULTS);
            this.spawnAmbientCreatures = readSettings(BiomeStandardValues.SPAWN_AMBIENT_CREATURES);
        }

        this.ReadCustomObjectSettings();
        this.ReadResourceSettings();
        this.heightMatrix = new double[this.worldConfig.worldHeightCap / 8 + 1];
        this.readHeightSettings(this.heightMatrix, BiomeStandardValues.CUSTOM_HEIGHT_CONTROL);
        this.riverHeightMatrix = new double[this.worldConfig.worldHeightCap / 8 + 1];
        this.readHeightSettings(this.riverHeightMatrix, BiomeStandardValues.RIVER_CUSTOM_HEIGHT_CONTROL);
    }

    private void readHeightSettings(double[] heightMatrix, Setting<double[]> setting)
    {
        double[] keys = readSettings(setting);
        for (int i = 0; i < heightMatrix.length && i < keys.length; i++)
                heightMatrix[i] = keys[i];
    }

    private SurfaceGenerator readSurfaceAndGroundControlSettings()
    {
        // Get default value
        SurfaceGenerator defaultSetting;
        if (isNewConfig)
        {
            String defaultString = StringHelper.join(defaultSettings.defaultSurfaceSurfaceAndGroundControl, ",");
            try
            {
                defaultSetting = BiomeStandardValues.SURFACE_AND_GROUND_CONTROL.read(defaultString);
            } catch (InvalidConfigException e)
            {
                throw new AssertionError(e);
            }
        } else
        {
            defaultSetting = new NullSurfaceGenerator();
        }

        return readSettings(BiomeStandardValues.SURFACE_AND_GROUND_CONTROL, defaultSetting);
    }

    private void ReadResourceSettings()
    {
        for (ConfigFunction<BiomeConfig> res : reader.getConfigFunctions(this, this.doResourceInheritance))
        {
            // Do not include DoResourceInheritance() as a resource
            if (res != null && res.getHolderType() != null)
            {
                if (res instanceof SaplingGen && res.isValid())
                {
                    SaplingGen sapling = (SaplingGen) res;
                    this.saplingGrowers.put(sapling.saplingType, sapling);
                } else if (res instanceof Resource)
                {
                    this.resourceSequence.add((Resource) res);
                }
            }
        }
    }

    /**
     * Merges two sets of resources. The child set will override any element
     * of the parent that it can.
     *
     * @param parent
     * @param child
     */
    public void merge(BiomeConfig parent)
    {
        // Merging settings is easy
        this.reader.setFallbackReader(parent.reader);

        this.biomeExtendsProcessed = true;
    }

    private void ReadCustomObjectSettings()
    {
        biomeObjects = new ArrayList<CustomObject>();
        biomeObjectStrings = new ArrayList<String>();

        // Read from BiomeObjects setting
        List<String> customObjectStrings = readSettings(BiomeStandardValues.BIOME_OBJECTS);
        for (String customObjectString : customObjectStrings)
        {
            CustomObject object = TerrainControl.getCustomObjectManager().getObjectFromString(customObjectString, worldConfig);
            if (object != null && !(object instanceof UseBiome))
            {
                biomeObjects.add(object);
                biomeObjectStrings.add(customObjectString);
            }
        }
    }

    @Override
    protected void writeConfigSettings(SettingsWriter writer) throws IOException
    {
        if (this.defaultSettings.isCustomBiome)
        {
            writer.comment("This is the biome config file of the " + getName() + " biome, which is a custom biome.");
        } else
        {
            writer.comment("This is the biome config file of the " + getName() + " biome, which is one of the vanilla biomes.");
        }

        writer.bigTitle("Biome Inheritance");
        writer.comment("This should be the value of the biomeConfig you wish to extend.");
        writer.comment("The extended config will be loaded, at which point the configs included below");
        writer.comment("will overwrite any configs loaded from the extended config.");
        writer.setting(BiomeStandardValues.BIOME_EXTENDS, this.biomeExtends);
        
        writer.comment("When set to true, all resources of the parent biome (if any) will be copied");
        writer.comment("to the resources queue of this biome. If a resource in the parent biome looks");
        writer.comment("very similar to that of a child biome (for example, two ores of the same type)");
        writer.comment("it won't be copied.");
        writer.setting(BiomeStandardValues.RESOURCE_INHERITANCE, this.doResourceInheritance);

        // Biome placement
        writer.bigTitle("Biome placement");

        writer.comment("Biome size from 0 to GenerationDepth. Defines in which biome layer this biome will be generated (see GenerationDepth).");
        writer.comment("Higher numbers give a smaller biome, lower numbers a larger biome.");
        writer.comment("Oceans and rivers are generated using a dirrerent algorithm in the default settings,");
        writer.comment("(they aren't in one of the biome lists), so this setting won't affect them.");
        writer.setting(BiomeStandardValues.BIOME_SIZE, this.biomeSize);

        writer.comment("Biome rarity from 100 to 1. If this is normal or ice biome - chance for spawn this biome then others.");
        writer.comment("Example for normal biome :");
        writer.comment("  100 rarity mean 1/6 chance than other ( with 6 default normal biomes).");
        writer.comment("  50 rarity mean 1/11 chance than other");
        writer.comment("For isle biome this is chance to spawn isle in good place.");
        writer.comment("Don`t work on Ocean and River (frozen versions too) biomes until not added as normal biome.");
        writer.setting(BiomeStandardValues.BIOME_RARITY, this.biomeRarity);

        writer.comment("The hexadecimal color value of this biome. Used in the output of the /tc map command,");
        writer.comment("and used in the input of BiomeMode:FromImage.");
        writer.setting(BiomeStandardValues.BIOME_COLOR, this.biomeColor);

        if (this.defaultSettings.isCustomBiome)
        {
            writer.comment("Replace this biome to specified after the terrain is generated.");
            writer.comment("This will make the world files contain the id of the specified biome, instead of the id of this biome.");
            writer.comment("This will cause saplings, colors and mob spawning work as in specified biome.");
            writer.setting(BiomeStandardValues.REPLACE_TO_BIOME_NAME, this.replaceToBiomeName);
        } else
        {
            writer.comment("(ReplaceToBiomeName is only available in custom biomes.)");
            writer.comment("");
        }

        writer.smallTitle("Isle biomes only");

        writer.comment("Biome name list where this biome will be spawned as isle. Like Mushroom isle in Ocean.  This work only if this biome is in IsleBiomes in world config");
        writer.setting(BiomeStandardValues.ISLE_IN_BIOME, this.isleInBiome);

        writer.smallTitle("Border biomes only");

        writer.comment("Biome name list where this biome will be border.Like Mushroom isle shore. Use is compared as IsleInBiome");
        writer.setting(BiomeStandardValues.BIOME_IS_BORDER, this.biomeIsBorder);

        writer.comment("Biome name list near border is not applied. ");
        writer.setting(BiomeStandardValues.NOT_BORDER_NEAR, this.notBorderNear);

        // Terrain height and volatility
        writer.bigTitle("Terrain height and volatility");

        writer.comment("BiomeHeight mean how much height will be added in terrain generation");
        writer.comment("It is double value from -10.0 to 10.0");
        writer.comment("Value 0.0 equivalent half of map height with all other default settings");
        writer.setting(BiomeStandardValues.BIOME_HEIGHT, this.biomeHeight);

        writer.comment("Biome volatility.");
        writer.setting(BiomeStandardValues.BIOME_VOLATILITY, this.biomeVolatility);

        writer.comment("Smooth radius between biomes. Must be between 0 and 32, inclusive. The resulting");
        writer.comment("smooth radius seems to be  (thisSmoothRadius + 1 + smoothRadiusOfBiomeOnOtherSide) * 4 .");
        writer.comment("So if two biomes next to each other have both a smooth radius of 2, the");
        writer.comment("resulting smooth area will be (2 + 1 + 2) * 4 = 20 blocks wide.");
        writer.setting(BiomeStandardValues.SMOOTH_RADIUS, this.smoothRadius);

        writer.comment("If this value is greater than 0, then it will affect how much, on average, the terrain will rise before leveling off when it begins to increase in elevation.");
        writer.comment("If the value is less than 0, then it will cause the terrain to either increase to a lower height before leveling out or decrease in height if the value is a large enough negative.");
        writer.setting(BiomeStandardValues.MAX_AVERAGE_HEIGHT, this.maxAverageHeight);

        writer.comment("If this value is greater than 0, then it will affect how much, on average, the terrain (usually at the ottom of the ocean) will fall before leveling off when it begins to decrease in elevation. ");
        writer.comment("If the value is less than 0, then it will cause the terrain to either fall to a lesser depth before leveling out or increase in height if the value is a large enough negative.");
        writer.setting(BiomeStandardValues.MAX_AVERAGE_DEPTH, this.maxAverageDepth);

        writer.comment("Another type of noise. This noise is independent from biomes. The larger the values the more chaotic/volatile landscape generation becomes.");
        writer.comment("Setting the values to negative will have the opposite effect and make landscape generation calmer/gentler.");
        writer.setting(BiomeStandardValues.VOLATILITY_1, this.volatilityRaw1);
        writer.setting(BiomeStandardValues.VOLATILITY_2, this.volatilityRaw2);

        writer.comment("Adjust the weight of the corresponding volatility settings. This allows you to change how prevalent you want either of the volatility settings to be in the terrain.");
        writer.setting(BiomeStandardValues.VOLATILITY_WEIGHT_1, this.volatilityWeightRaw1);
        writer.setting(BiomeStandardValues.VOLATILITY_WEIGHT_2, this.volatilityWeightRaw2);

        writer.comment("Disable all noises except Volatility1 and Volatility2. Also disable default block chance from height.");
        writer.setting(BiomeStandardValues.DISABLE_BIOME_HEIGHT, this.disableNotchHeightControl);

        writer.comment("List of custom height factor, 17 double entries, each entire control of about 7 blocks height from down. Positive entry - better chance of spawn blocks, negative - smaller");
        writer.comment("Values which affect your configuration may be found only experimental. That may be very big, like ~3000.0 depends from height");
        writer.comment("Example:");
        writer.comment("  CustomHeightControl:0.0,-2500.0,0.0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0");
        writer.comment("Make empty layer above bedrock layer. ");
        writer.setting(BiomeStandardValues.CUSTOM_HEIGHT_CONTROL, this.heightMatrix);

        writer.bigTitle("Rivers");
        writer.comment("There are two different river systems - the standard one and the improved one.");
        writer.comment("See the ImprovedRivers settting in the WorldConfig. Both modes have different");
        writer.comment("river settings, so carefully read the headers to know which settings you can use.");
        writer.comment("");

        writer.smallTitle("ImprovedRivers:false");
        writer.comment("Only available when ImprovedRivers is set to false in the WorldConfig.");
        writer.comment("Sets which biome is used as the river biome.");
        writer.setting(BiomeStandardValues.RIVER_BIOME, this.riverBiome);

        writer.smallTitle("ImprovedRivers:true");
        writer.comment("Only available when ImprovedRivers is set to true in the WorldConfig.");
        writer.comment("");
        writer.comment("Works the same as BiomeHeight (scroll up), but is used where a river is generated in this biome");
        writer.setting(BiomeStandardValues.RIVER_HEIGHT, this.riverHeight);

        writer.comment("Works the same as BiomeVolatility (scroll up), but is used where a river is generated in this biome");
        writer.setting(BiomeStandardValues.RIVER_VOLATILITY, this.riverVolatility);

        writer.comment("Works the same as WaterLevelMax (scroll down), but is used where a river is generated in this biome");
        writer.comment("Can be used to create elevated rivers");
        writer.setting(BiomeStandardValues.RIVER_WATER_LEVEL, this.configRiverWaterLevel);

        writer.comment("Works the same as CustomHeightControl (scroll up), but is used where a river is generated in this biome");
        writer.setting(BiomeStandardValues.RIVER_CUSTOM_HEIGHT_CONTROL, this.riverHeightMatrix);

        writer.bigTitle("Blocks");

        writer.comment("Change this to generate something else than stone in the biome. Doesn't support block data.");
        writer.setting(BiomeStandardValues.STONE_BLOCK, this.stoneBlock);

        writer.comment("Surface block, usually GRASS. Doesn't support block data.");
        writer.setting(BiomeStandardValues.SURFACE_BLOCK, this.surfaceBlock);

        writer.comment("Block from stone to surface, like dirt in most biomes. Doesn't support block data.");
        writer.setting(BiomeStandardValues.GROUND_BLOCK, this.groundBlock);

        writer.comment("Setting for biomes with more complex surface and ground blocks.");
        writer.comment("Each column in the world has a noise value from what appears to be -7 to 7.");
        writer.comment("Values near 0 are more common than values near -7 and 7. This setting is");
        writer.comment("used to change the surface block based on the noise value for the column.");
        writer.comment("Syntax: SurfaceBlockName,GroundBlockName,MaxNoise,[AnotherSurfaceBlockName,[AnotherGroundBlockName,MaxNoise[,...]]");
        writer.comment("Example: " + BiomeStandardValues.SURFACE_AND_GROUND_CONTROL + ": STONE,STONE,-0.8,GRAVEL,STONE,0.0,DIRT,DIRT,10.0");
        writer.comment("  When the noise is below -0.8, stone is the surface and ground block, between -0.8 and 0");
        writer.comment("  gravel with stone just below and between 0.0 and 10.0 there's only dirt.");
        writer.comment("  Because 10.0 is higher than the noise can ever get, the normal " + BiomeStandardValues.SURFACE_BLOCK);
        writer.comment("  and " + BiomeStandardValues.GROUND_BLOCK + " will never appear in this biome.");
        writer.comment("");
        writer.comment("Alternatively, you can use Mesa, MesaForest or MesaBryce to get blocks");
        writer.comment("like the blocks found in the Mesa biomes.");
        writer.setting(BiomeStandardValues.SURFACE_AND_GROUND_CONTROL, this.surfaceAndGroundControl);

        writer.comment("Replace Variable: (blockFrom,blockTo[:blockDataTo][,minHeight,maxHeight])");
        writer.comment("Example :");
        writer.comment("  ReplacedBlocks: (GRASS,DIRT,100,127),(GRAVEL,GLASS)");
        writer.comment("Replace grass block to dirt from 100 to 127 height and replace gravel to glass on all height ");
        writer.setting(BiomeStandardValues.REPLACED_BLOCKS, replacedBlocks);

        writer.smallTitle("Water and ice");

        writer.comment("Set this to false to use the water and ice settings of this biome.");
        writer.setting(BiomeStandardValues.USE_WORLD_WATER_LEVEL, this.useWorldWaterLevel);

        writer.comment("Set water level. Every empty between this levels will be fill water or another block from WaterBlock.");
        writer.setting(BiomeStandardValues.WATER_LEVEL_MAX, this.configWaterLevelMax);
        writer.setting(BiomeStandardValues.WATER_LEVEL_MIN, this.configWaterLevelMin);

        writer.comment("Block used as water in WaterLevelMax");
        writer.setting(BiomeStandardValues.WATER_BLOCK, this.configWaterBlock);

        writer.comment("Block used as ice. Ice only spawns if the BiomeTemperture is low enough.");
        writer.setting(BiomeStandardValues.ICE_BLOCK, this.configIceBlock);

        writer.bigTitle("Visuals and weather");
        writer.comment("Most of the settings here only have an effect on players with the client version of Terrain Control installed.");

        writer.comment("Biome temperature. Float value from 0.0 to 2.0.");
        if (this.defaultSettings.isCustomBiome)
        {
            writer.comment("When this value is around 0.2, snow will fall on mountain peaks above y=90.");
            writer.comment("When this value is around 0.1, the whole biome will be covered in snow and ice.");
        } else
        {
            writer.comment("On default biomes, this won't do anything except changing the grass and leaves colors slightly.");
        }
        writer.setting(BiomeStandardValues.BIOME_TEMPERATURE, this.biomeTemperature);

        writer.comment("Biome wetness. Float value from 0.0 to 1.0.");
        if (this.defaultSettings.isCustomBiome)
        {
            writer.comment("When this is set to 0, no rain will fall.");
        } else
        {
            writer.comment("On default biomes, this won't do anything except changing the grass and leaves colors slightly.");
        }
        writer.setting(BiomeStandardValues.BIOME_WETNESS, this.biomeWetness);

        writer.comment("Biome sky color.");
        writer.setting(BiomeStandardValues.SKY_COLOR, this.skyColor);

        writer.comment("Biome water color multiplier.");
        writer.setting(BiomeStandardValues.WATER_COLOR, this.waterColor);

        writer.comment("Biome grass color.");
        writer.setting(BiomeStandardValues.GRASS_COLOR, this.grassColor);

        writer.comment("Whether the grass color is a multiplier.");
        writer.comment("If you set it to true, the color will be based on this value, the BiomeTemperature and the BiomeWetness.");
        writer.comment("If you set it to false, the grass color will be just this color.");
        writer.setting(BiomeStandardValues.GRASS_COLOR_IS_MULTIPLIER, this.grassColorIsMultiplier);

        writer.comment("Biome foliage color.");
        writer.setting(BiomeStandardValues.FOLIAGE_COLOR, this.foliageColor);

        writer.comment("Whether the foliage color is a multiplier. See GrassColorIsMultiplier for details.");
        writer.setting(BiomeStandardValues.FOLIAGE_COLOR_IS_MULTIPLIER, this.foliageColorIsMultiplier);

        writer.bigTitle("Resource queue");
        writer.comment("This section control all resources spawning after terrain generation.");
        writer.comment("The resources will be placed in this order.");
        writer.comment("");
        writer.comment("Keep in mind that a high size, frequency or rarity might slow down terrain generation.");
        writer.comment("");
        writer.comment("Possible resources:");
        writer.comment("DoResourceInheritance(true|false)");
        writer.comment("SmallLake(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude)");
        writer.comment("Dungeon(Frequency,Rarity,MinAltitude,MaxAltitude)");
        writer.comment("UnderGroundLake(MinSize,MaxSize,Frequency,Rarity,MinAltitude,MaxAltitude)");
        writer.comment("Ore(BlockName,Size,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        writer.comment("UnderWaterOre(BlockName,Size,Frequency,Rarity,BlockSource[,BlockSource2,BlockSource3.....])");
        writer.comment("CustomObject(Object[,AnotherObject[,...]])");
        writer.comment("CustomStructure([Object,Object_Chance[,AnotherObject,Object_Chance[,...]]])");
        writer.comment("Tree(Frequency,TreeType,TreeTypeChance[,AdditionalTreeType,AdditionalTreeTypeChance.....])");
        writer.comment("Plant(PlantType,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        writer.comment("Grass(PlantType,Grouped/NotGrouped,Frequency,Rarity,BlockSource[,BlockSource2,BlockSource3.....])");
        writer.comment("Reed(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        writer.comment("Cactus(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        writer.comment("Liquid(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        writer.comment("AboveWaterRes(BlockName,Frequency,Rarity)");
        writer.comment("Vines(Frequency,Rarity,MinAltitude,MaxAltitude)");
        writer.comment("Vein(BlockName,MinRadius,MaxRadius,Rarity,OreSize,OreFrequency,OreRarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,..])");
        writer.comment("Well(BaseBlockName,HalfSlabBlockName,WaterBlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,..])");
        writer.comment("Boulder(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,..]");
        writer.comment("IceSpike(BlockName,IceSpikeType,Frequency,Rarity,MinAltitude,MaxAltitude,Blocksource[,BlockSource2,...])");
        writer.comment("");
        writer.comment("BlockName:      must be the name of a block. May include block data, like \"WOOL:1\".");
        writer.comment("BlockSource:    list of blocks the resource can spawn on/in. You can also use \"Solid\" or \"All\".");
        writer.comment("Frequency:      number of attempts to place this resource in each chunk.");
        writer.comment("Rarity:         chance for each attempt, Rarity:100 - mean 100% to pass, Rarity:1 - mean 1% to pass.");
        writer.comment("MinAltitude and MaxAltitude: height limits.");
        writer.comment("BlockSource:    mean where or whereupon resource will be placed ");
        writer.comment("TreeType:       Tree (original oak tree) - BigTree - Birch - TallBirch - SwampTree");
        writer.comment("                HugeMushroom (not a tree but still counts) - Taiga1 - Taiga2 - HugeTaiga1 - HugeTaiga2");
        writer.comment("                JungleTree (the huge jungle tree) - GroundBush - CocoaTree (smaller jungle tree)");
        writer.comment("                DarkOak (from the roofed forest biome) - Acacia");
        writer.comment("                You can also use your own custom objects, as long as they have set Tree to true in their settings.");
        writer.comment("TreeTypeChance: similar to Rarity. Example:");
        writer.comment("                Tree(10,Taiga1,35,Taiga2,100) - plugin tries 10 times, for each attempt it tries to place Taiga1 (35% chance),");
        writer.comment("                if that fails, it attempts to place Taiga2 (100% chance).");
        writer.comment("PlantType:      one of the plant types: " + StringHelper.join(PlantType.values(), ", "));
        writer.comment("                or simply a BlockName");
        writer.comment("IceSpikeType:   one of the ice spike types: " + StringHelper.join(IceSpikeGen.SpikeType.values(), ","));
        writer.comment("Object:         can be a any kind of custom object (bo2 or bo3) but without the file extension. You can");
        writer.comment("                also use UseWorld to spawn one of the object in the WorldObjects folder and UseBiome to spawn");
        writer.comment("                one of the objects in the BiomeObjects setting. When using BO2s for UseWorld, the BO2 must have");
        writer.comment("                this biome in their spawnInBiome setting.");
        writer.comment("");
        writer.comment("Plant and Grass resource: both a resource of one block. Plant can place blocks underground, Grass cannot.");
        writer.comment("Liquid resource: a one-block water or lava source");
        writer.comment("SmallLake and UnderGroundLake resources: small lakes of about 8x8 blocks");
        writer.comment("Vein resource: not in vanilla. Starts an area where ores will spawn. Can be slow, so use a low Rarity (smaller than 1).");
        writer.comment("CustomStructure resource: starts a BO3 structure in the chunk.");
        writer.comment("");

        this.WriteResources(writer);

        writer.bigTitle("Sapling resource");
        writer.comment("Terrain Control allows you to grow your custom objects from saplings, instead");
        writer.comment("of the vanilla trees. Add one or more Sapling functions here to override vanilla");
        writer.comment("spawning for that sapling.");
        writer.comment("");
        writer.comment("The syntax is: Sapling(SaplingType,TreeType,TreeType_Chance[,Additional_TreeType,Additional_TreeType_Chance.....])");
        writer.comment("Works like Tree resource instead first parameter.");
        writer.comment("");
        writer.comment("Sapling types: " + StringHelper.join(SaplingType.values(), ", "));
        writer.comment("All - will make the tree spawn from all saplings, but not from mushrooms.");
        writer.comment("BigJungle - for when 4 jungle saplings grow at once.");
        writer.comment("RedMushroom/BrownMushroom - will only grow when bonemeal is used.");
        this.WriteSaplingSettings(writer);

        writer.bigTitle("Custom objects");
        writer.comment("These objects will spawn when using the UseBiome keyword.");
        this.WriteCustomObjects(writer);

        writer.bigTitle("Structures");
        writer.comment("Here you can change, enable or disable the stuctures.");
        writer.comment("If you have disabled the structure in the WorldConfig, it won't spawn,");
        writer.comment("regardless of these settings.");

        writer.comment("Disables strongholds for this biome. If there is no suitable biome nearby,");
        writer.comment("Minecraft will ignore this setting.");
        writer.setting(BiomeStandardValues.STRONGHOLDS_ENABLED, strongholdsEnabled);

        writer.comment("Whether a Nether Fortress can start in this biome. Might extend to neighbor biomes.");
        writer.setting(BiomeStandardValues.NETHER_FORTRESSES_ENABLED, netherFortressesEnabled);

        writer.comment("The village type in this biome. Can be wood, sandstone or disabled.");
        writer.setting(BiomeStandardValues.VILLAGE_TYPE, villageType);

        writer.comment("The mineshaft rarity from 0 to 100. 0 = no mineshafts, 1 = default rarity, 100 = a wooden chaos.");
        writer.setting(BiomeStandardValues.MINESHAFT_RARITY, mineshaftsRarity);

        writer.comment("The type of the aboveground rare building in this biome. Can be desertPyramid, jungleTemple, swampHut or disabled.");
        writer.setting(BiomeStandardValues.RARE_BUILDING_TYPE, rareBuildingType);

        writer.bigTitle("Mob spawning");
        if (!this.defaultSettings.isCustomBiome)
        {
            // Stop in the default biomes
            writer.comment("Mob spawning control doesn't work in default biomes.");
            return;
        }

        writer.comment("========<TUTORIAL>========");
        writer.comment("This is where you configure mob spawning. Changing this section is optional.");
        writer.comment("");
        writer.comment("#STEP1: Understanding what a mobgroup is.");
        writer.comment("A mobgroups is made of four parts. They are mob, weight, min and max.");
        writer.comment("The mob is one of the Minecraft internal mob names.");
        writer.comment("See http://www.minecraftwiki.net/wiki/Chunk_format#Mobs");
        writer.comment("The weight is used for a random selection. This is a positive integer.");
        writer.comment("The min is the minimum amount of mobs spawning as a group. This is a positive integer.");
        writer.comment("The max is the maximum amount of mobs spawning as a group. This is a positive integer.");
        writer.comment("");
        writer.comment("#STEP2: Understanding how write a mobgroup as JSON as well as lists of them.");
        writer.comment("Json is a tree document format: http://en.wikipedia.org/wiki/JSON");
        writer.comment("Write a mobgroup like this: {\"mob\": \"mobname\", \"weight\": integer, \"min\": integer, \"max\": integer}");
        writer.comment("For example: {\"mob\": \"Ocelot\", \"weight\": 10, \"min\": 2, \"max\": 6}");
        writer.comment("For example: {\"mob\": \"MushroomCow\", \"weight\": 5, \"min\": 2, \"max\": 2}");
        writer.comment("A json list of mobgroups looks like this: [mobgroup, mobgroup, mobgroup...]");
        writer.comment("This would be an ampty list: []");
        writer.comment("You can validate your json here: http://jsonlint.com/");
        writer.comment("");
        writer.comment("#STEP3: Understanding what to do with all this info");
        writer.comment("There are three categories of mobs: monsters, creatures and watercreatures.");
        writer.comment("These list may be populated with Default values if thee booleans bellow is set to true");
        writer.comment("You may also add your own mobgroups in the lists below");
        writer.comment("");
        writer.comment("#STEP4: What is in the default mob groups?");
        writer.comment("The default mob groups are controlled by vanilla minecraft.");
        writer.comment("At 2012-03-24 you could find them here: https://github.com/Bukkit/mc-dev/blob/master/net/minecraft/server/BiomeBase.java#L75");
        writer.comment("In simple terms:");
        writer.comment("default creatures: [{\"mob\": \"Sheep\", \"weight\": 12, \"min\": 4, \"max\": 4}, {\"mob\": \"Pig\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Chicken\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Cow\", \"weight\": 8, \"min\": 4, \"max\": 4}]");
        writer.comment("default monsters: [{\"mob\": \"Spider\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Zombie\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Skeleton\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Creeper\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Slime\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Enderman\", \"weight\": 1, \"min\": 1, \"max\": 4}]");
        writer.comment("default watercreatures: [{\"mob\": \"Squid\", \"weight\": 10, \"min\": 4, \"max\": 4}]");
        writer.comment("");
        writer.comment("So for example ocelots wont spawn unless you add them below.");

        writer.comment("========<CONFIGURATION>========");

        writer.comment("Should we add the default monster spawn groups?");
        writer.setting(BiomeStandardValues.SPAWN_MONSTERS_ADD_DEFAULTS, this.spawnMonstersAddDefaults);
        writer.comment("Add extra monster spawn groups here");
        writer.setting(BiomeStandardValues.SPAWN_MONSTERS, this.spawnMonsters);

        writer.comment("Should we add the default creature spawn groups?");
        writer.setting(BiomeStandardValues.SPAWN_CREATURES_ADD_DEFAULTS, this.spawnCreaturesAddDefaults);
        writer.comment("Add extra creature spawn groups here");
        writer.setting(BiomeStandardValues.SPAWN_CREATURES, this.spawnCreatures);

        writer.comment("Should we add the default watercreature spawn groups?");
        writer.setting(BiomeStandardValues.SPAWN_WATER_CREATURES_ADD_DEFAULTS, this.spawnWaterCreaturesAddDefaults);
        writer.comment("Add extra watercreature spawn groups here");
        writer.setting(BiomeStandardValues.SPAWN_WATER_CREATURES, this.spawnWaterCreatures);

        writer.comment("Should we add the default ambient creature spawn groups? (Currently only bats)");
        writer.setting(BiomeStandardValues.SPAWN_AMBIENT_CREATURES_ADD_DEFAULTS, this.spawnAmbientCreaturesAddDefaults);
        writer.comment("Add extra ambient creature spawn groups here");
        writer.setting(BiomeStandardValues.SPAWN_AMBIENT_CREATURES, this.spawnAmbientCreatures);

    }

    private void WriteResources(SettingsWriter writer) throws IOException
    {
        for (Resource resource : this.resourceSequence)
        {
            writer.function(resource);
        }
    }

    private void WriteCustomObjects(SettingsWriter writer) throws IOException
    {
        ArrayList<String> objectStrings = new ArrayList<String>(biomeObjectStrings.size());
        for (String objectString : biomeObjectStrings)
        {
            objectStrings.add(objectString);
        }
        writer.setting(BiomeStandardValues.BIOME_OBJECTS, objectStrings);
    }

    private void WriteSaplingSettings(SettingsWriter writer) throws IOException
    {
        for (SaplingGen res : this.saplingGrowers.values())
        {
            writer.function(res);
        }
    }

    @Override
    protected void correctSettings()
    {
        this.biomeExtends = (this.biomeExtends == null || this.biomeExtends.equals("null")) ? "" : this.biomeExtends;
        this.biomeSize = lowerThanOrEqualTo(biomeSize, worldConfig.GenerationDepth);
        this.biomeRarity = lowerThanOrEqualTo(biomeRarity, worldConfig.BiomeRarityScale);

        this.isleInBiome = filterBiomes(this.isleInBiome, this.worldConfig.customBiomeGenerationIds.keySet());
        this.biomeIsBorder = filterBiomes(this.biomeIsBorder, this.worldConfig.customBiomeGenerationIds.keySet());
        this.notBorderNear = filterBiomes(this.notBorderNear, this.worldConfig.customBiomeGenerationIds.keySet());

        this.volatility1 = this.volatilityRaw1 < 0.0D ? 1.0D / (Math.abs(this.volatilityRaw1) + 1.0D) : this.volatilityRaw1 + 1.0D;
        this.volatility2 = this.volatilityRaw2 < 0.0D ? 1.0D / (Math.abs(this.volatilityRaw2) + 1.0D) : this.volatilityRaw2 + 1.0D;

        this.volatilityWeight1 = (this.volatilityWeightRaw1 - 0.5D) * 24.0D;
        this.volatilityWeight2 = (0.5D - this.volatilityWeightRaw2) * 24.0D;

        this.waterLevelMax = lowerThanOrEqualTo(waterLevelMax, this.waterLevelMin);

        this.replaceToBiomeName = (DefaultBiome.Contain(this.replaceToBiomeName) || this.worldConfig.customBiomeGenerationIds.keySet().contains(
                this.replaceToBiomeName)) ? this.replaceToBiomeName : "";

        this.riverBiome = (DefaultBiome.Contain(this.riverBiome) || this.worldConfig.customBiomeGenerationIds.keySet().contains(this.riverBiome)) ? this.riverBiome
                : "";
    }
    
    /**
     * Some settings used to be in the WorldConfig but are now in the
     * BiomeConfig. This method moves such a setting over.
     * @param setting The setting to move over.
     */
    private <T> void moveSettingFromWorld(Setting<T> setting)
    {
        T value = worldConfig.reader.getSetting(setting, setting.getDefaultValue());
        if (!value.equals(setting.getDefaultValue()))
        {
            this.reader.putSetting(setting, value);
        }
    }

    @Override
    protected void renameOldSettings()
    {
        // Old values from WorldConfig
        Setting<?>[] copyFromWorld = {BiomeStandardValues.MAX_AVERAGE_HEIGHT, BiomeStandardValues.MAX_AVERAGE_DEPTH,
                BiomeStandardValues.VOLATILITY_1, BiomeStandardValues.VOLATILITY_2, BiomeStandardValues.VOLATILITY_WEIGHT_1,
                BiomeStandardValues.VOLATILITY_WEIGHT_2, BiomeStandardValues.DISABLE_BIOME_HEIGHT, BiomeStandardValues.CUSTOM_HEIGHT_CONTROL};
        for (Setting<?> setting : copyFromWorld)
        {
            moveSettingFromWorld(setting);
        }

        // disableNotchPonds
        if (reader.hasSetting(BiomeStandardValues.DISABLE_NOTCH_PONDS))
        {
            // Found disableNotchPonds, so add SmallLake resource if it wasn't set to true
            if (!readSettings(BiomeStandardValues.DISABLE_NOTCH_PONDS, false))
            {
                reader.addConfigFunction(ConfigFunction.create(this, SmallLakeGen.class, DefaultMaterial.WATER, 4, 7, 8, worldConfig.worldHeightCap));
                reader.addConfigFunction(ConfigFunction.create(this, SmallLakeGen.class, DefaultMaterial.LAVA, 2, 3, 8, worldConfig.worldHeightCap - 8));
            }
        }

        // CustomTreeChance
        int customTreeChance = worldConfig.reader.getSetting(WorldStandardValues.CUSTOM_TREE_CHANCE, 0);
        if (customTreeChance == 100)
        {
            reader.addConfigFunction(ConfigFunction.create(this, SaplingGen.class, "All", "UseWorld", 100));
        }
        if (customTreeChance > 0 && customTreeChance < 100)
        {
            reader.addConfigFunction(ConfigFunction.create(this, SaplingGen.class,
                    SaplingType.Oak, "UseWorld", customTreeChance, TreeType.BigTree, 10, TreeType.Tree, 100));
            reader.addConfigFunction(ConfigFunction.create(this, SaplingGen.class,
                    SaplingType.Redwood, "UseWorld", customTreeChance, TreeType.Taiga2, 100));
            reader.addConfigFunction(ConfigFunction.create(this, SaplingGen.class,
                    SaplingType.Birch, "UseWorld", customTreeChance, TreeType.Birch, 100));
            reader.addConfigFunction(ConfigFunction.create(this, SaplingGen.class,
                    SaplingType.SmallJungle, "UseWorld", customTreeChance, TreeType.CocoaTree, 100));
        }

        // FrozenRivers
        if (!this.worldConfig.readSettings(WorldStandardValues.FROZEN_RIVERS))
        {
            // User had disabled frozen rivers in the old WorldConfig
            // So ignore the default value of RiverBiome
            this.reader.putSetting(BiomeStandardValues.RIVER_BIOME, "River");
        }

        // BiomeRivers
        if (!readSettings(BiomeStandardValues.BIOME_RIVERS))
        {
            // If the rivers were disabled using the old setting, disable them
            // also using the new setting
            // (Overrides FrozenRivers: false)
            this.reader.putSetting(BiomeStandardValues.RIVER_BIOME, "");
        }

        // ReplacedBlocks in format fromId=toId.data(minHeight-maxHeight)
        String replacedBlocksValue = readSettings(BiomeStandardValues.REPLACED_BLOCKS_OLD);

        if (replacedBlocksValue.contains("="))
        {
            String[] values = replacedBlocksValue.split(",");
            List<ReplacedBlocksInstruction> output = new ArrayList<ReplacedBlocksInstruction>();

            for (String replacedBlock : values)
            {
                try
                {
                    LocalMaterialData fromId = TerrainControl.readMaterial(replacedBlock.split("=")[0]);
                    String rest = replacedBlock.split("=")[1];
                    LocalMaterialData to;
                    int minHeight = 0;
                    int maxHeight = worldConfig.worldHeightCap;

                    int start = rest.indexOf('(');
                    int end = rest.indexOf(')');
                    if (start != -1 && end != -1)
                    {   // Found height settings
                        String[] ranges = rest.substring(start + 1, end).split("-");
                        to = TerrainControl.readMaterial(rest.substring(0, start));
                        minHeight = StringHelper.readInt(ranges[0], minHeight, maxHeight);
                        maxHeight = StringHelper.readInt(ranges[1], minHeight, maxHeight);
                    } else
                    {   // No height settings
                        to = TerrainControl.readMaterial(rest);
                    }

                    output.add(new ReplacedBlocksInstruction(fromId, to, minHeight, maxHeight));
                } catch (InvalidConfigException ignored)
                {
                }
            }

            ReplacedBlocksMatrix replacedBlocks = ReplacedBlocksMatrix.createEmptyMatrix(worldConfig.worldHeightCap);
            replacedBlocks.setInstructions(output);
            this.reader.putSetting(BiomeStandardValues.REPLACED_BLOCKS, replacedBlocks);
        }
    }

    public void writeToStream(DataOutputStream stream) throws IOException
    {
        writeStringToStream(stream, getName());

        stream.writeFloat(this.biomeTemperature);
        stream.writeFloat(this.biomeWetness);
        stream.writeInt(this.skyColor);
        stream.writeInt(this.waterColor);
        stream.writeInt(this.grassColor);
        stream.writeBoolean(this.grassColorIsMultiplier);
        stream.writeInt(this.foliageColor);
        stream.writeBoolean(this.foliageColorIsMultiplier);
    }

}
