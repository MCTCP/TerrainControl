package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.generator.resourcegens.PlantType;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.UseBiome;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.generator.resourcegens.CustomStructureGen;
import com.khorn.terraincontrol.generator.resourcegens.Resource;
import com.khorn.terraincontrol.generator.resourcegens.SaplingGen;
import com.khorn.terraincontrol.generator.resourcegens.SaplingType;
import com.khorn.terraincontrol.util.StringHelper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class BiomeConfig extends ConfigFile
{

    public short[][] replaceMatrixBlocks = new short[TerrainControl.supportedBlockIds][];
    public int ReplaceCount = 0;

    public String riverBiome;
    public float riverHeight;
    public float riverVolatility;
    public int riverWaterLevel;
    public double[] riverHeightMatrix;

    public int BiomeSize;
    public int BiomeRarity;

    public String BiomeColor;

    public ArrayList<String> BiomeIsBorder;
    public ArrayList<String> IsleInBiome;
    public ArrayList<String> NotBorderNear;

    // Surface config
    public float BiomeHeight;
    public float BiomeVolatility;
    public int SmoothRadius;

    public float BiomeTemperature;
    public float BiomeWetness;

    public int StoneBlock;
    public int SurfaceBlock;
    public int GroundBlock;

    public String ReplaceBiomeName;

    public boolean UseWorldWaterLevel;
    public int waterLevelMax;
    public int waterLevelMin;
    public int waterBlock;
    public int iceBlock;

    public int SkyColor;
    public int WaterColor;

    public int GrassColor;
    public boolean GrassColorIsMultiplier;
    public int FoliageColor;
    public boolean FoliageColorIsMultiplier;

    public List<Resource> resourceSequence = new ArrayList<Resource>();
    private SaplingGen[] saplingTypes = new SaplingGen[20];
    private SaplingGen saplingResource = null;

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

    public LocalBiome Biome;
    public DefaultBiomeSettings defaultSettings;

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

    public BiomeConfig(File settingsDir, LocalBiome biome, WorldConfig config)
    {
        super(biome.getName(), new File(settingsDir, biome.getName() + TCDefaultValues.WorldBiomeConfigName.stringValue()));
        this.Biome = biome;
        this.worldConfig = config;
        this.defaultSettings = DefaultBiomeSettings.getDefaultSettings(biome, config.WorldHeight);

        this.readSettingsFile();
        this.renameOldSettings();
        this.readConfigSettings();

        this.correctSettings();
        
        // Add default resources when needed
        if (!file.exists())
        {
            this.resourceSequence.addAll(defaultSettings.createDefaultResources(this));
        }

        if (config.SettingsMode != WorldConfig.ConfigMode.WriteDisable)
            this.writeSettingsFile(config.SettingsMode == WorldConfig.ConfigMode.WriteAll);

        if (this.UseWorldWaterLevel)
        {
            this.waterLevelMax = worldConfig.waterLevelMax;
            this.waterLevelMin = worldConfig.waterLevelMin;
            this.waterBlock = worldConfig.waterBlock;
            this.iceBlock = worldConfig.iceBlock;
        }

        if (biome.isCustom())
            biome.setEffects(this);
    }

    public int getTemperature()
    {
        return (int) (this.BiomeTemperature * 65536.0F);
    }

    public int getWetness()
    {
        return (int) (this.BiomeWetness * 65536.0F);
    }

    public SaplingGen getSaplingGen(SaplingType type)
    {
        SaplingGen gen = this.saplingTypes[type.getSaplingId()];
        if (gen == null && type.growsTree())
        {
            gen = this.saplingResource;
        }
        return gen;
    }

    @Override
    protected void readConfigSettings()
    {
        this.BiomeSize = readModSettings(TCDefaultValues.BiomeSize, defaultSettings.defaultSize);
        this.BiomeRarity = readModSettings(TCDefaultValues.BiomeRarity, defaultSettings.defaultRarity);

        this.BiomeColor = readModSettings(TCDefaultValues.BiomeColor, defaultSettings.defaultColor);

        this.riverBiome = readModSettings(TCDefaultValues.RiverBiome, defaultSettings.defaultRiverBiome);

        this.IsleInBiome = readModSettings(TCDefaultValues.IsleInBiome, defaultSettings.defaultIsle);
        this.BiomeIsBorder = readModSettings(TCDefaultValues.BiomeIsBorder, defaultSettings.defaultBorder);
        this.NotBorderNear = readModSettings(TCDefaultValues.NotBorderNear, defaultSettings.defaultNotBorderNear);

        this.BiomeTemperature = readModSettings(TCDefaultValues.BiomeTemperature, defaultSettings.defaultBiomeTemperature);
        this.BiomeWetness = readModSettings(TCDefaultValues.BiomeWetness, defaultSettings.defaultBiomeWetness);

        this.ReplaceBiomeName = readSettings(TCDefaultValues.ReplaceToBiomeName);

        this.BiomeHeight = readModSettings(TCDefaultValues.BiomeHeight, defaultSettings.defaultBiomeSurface);
        this.BiomeVolatility = readModSettings(TCDefaultValues.BiomeVolatility, defaultSettings.defaultBiomeVolatility);
        this.SmoothRadius = readSettings(TCDefaultValues.SmoothRadius);

        this.StoneBlock = readSettings(TCDefaultValues.StoneBlock);
        this.SurfaceBlock = readModSettings(TCDefaultValues.SurfaceBlock, defaultSettings.defaultSurfaceBlock);
        this.GroundBlock = readModSettings(TCDefaultValues.GroundBlock, defaultSettings.defaultGroundBlock);

        this.UseWorldWaterLevel = readSettings(TCDefaultValues.UseWorldWaterLevel);
        this.waterLevelMax = readSettings(TCDefaultValues.WaterLevelMax);
        this.waterLevelMin = readSettings(TCDefaultValues.WaterLevelMin);
        this.waterBlock = readSettings(TCDefaultValues.WaterBlock);
        this.iceBlock = readSettings(TCDefaultValues.IceBlock);

        this.SkyColor = readSettings(TCDefaultValues.SkyColor);
        this.WaterColor = readModSettingsColor(TCDefaultValues.WaterColor, defaultSettings.defaultWaterColorMultiplier);
        this.GrassColor = readModSettingsColor(TCDefaultValues.GrassColor, defaultSettings.defaultGrassColor);
        this.GrassColorIsMultiplier = readSettings(TCDefaultValues.GrassColorIsMultiplier);
        this.FoliageColor = readModSettingsColor(TCDefaultValues.FoliageColor, defaultSettings.defaultFoliageColor);
        this.FoliageColorIsMultiplier = readSettings(TCDefaultValues.FoliageColorIsMultiplier);

        this.volatilityRaw1 = readSettings(TCDefaultValues.Volatility1);
        this.volatilityRaw2 = readSettings(TCDefaultValues.Volatility2);
        this.volatilityWeightRaw1 = readSettings(TCDefaultValues.VolatilityWeight1);
        this.volatilityWeightRaw2 = readSettings(TCDefaultValues.VolatilityWeight2);
        this.disableNotchHeightControl = readSettings(TCDefaultValues.DisableBiomeHeight);
        this.maxAverageHeight = readSettings(TCDefaultValues.MaxAverageHeight);
        this.maxAverageDepth = readSettings(TCDefaultValues.MaxAverageDepth);

        this.riverHeight = readSettings(TCDefaultValues.RiverHeight);
        this.riverVolatility = readSettings(TCDefaultValues.RiverVolatility);
        this.riverWaterLevel = readSettings(TCDefaultValues.RiverWaterLevel);

        this.strongholdsEnabled = readModSettings(TCDefaultValues.StrongholdsEnabled, defaultSettings.defaultStrongholds);
        this.netherFortressesEnabled = readModSettings(TCDefaultValues.NetherFortressesEnabled, true);
        this.villageType = (VillageType) readModSettings(TCDefaultValues.VillageType, defaultSettings.defaultVillageType);
        this.mineshaftsRarity = readSettings(TCDefaultValues.MineshaftRarity);
        this.rareBuildingType = (RareBuildingType) readModSettings(TCDefaultValues.RareBuildingType, defaultSettings.defaultRareBuildingType);

        if (this.Biome.isCustom())
        {
            // Only for custom biomes
            this.spawnMonstersAddDefaults = readSettings(TCDefaultValues.SpawnMonstersAddDefaults);
            this.spawnMonsters = readModSettings(TCDefaultValues.SpawnMonsters, new ArrayList<WeightedMobSpawnGroup>());
            this.spawnCreaturesAddDefaults = readSettings(TCDefaultValues.SpawnCreaturesAddDefaults);
            this.spawnCreatures = readModSettings(TCDefaultValues.SpawnCreatures, new ArrayList<WeightedMobSpawnGroup>());
            this.spawnWaterCreaturesAddDefaults = readSettings(TCDefaultValues.SpawnWaterCreaturesAddDefaults);
            this.spawnWaterCreatures = readModSettings(TCDefaultValues.SpawnWaterCreatures, new ArrayList<WeightedMobSpawnGroup>());
            this.spawnAmbientCreaturesAddDefaults = readSettings(TCDefaultValues.SpawnAmbientCreaturesAddDefaults);
            this.spawnAmbientCreatures = readModSettings(TCDefaultValues.SpawnAmbientCreatures, new ArrayList<WeightedMobSpawnGroup>());
        }

        this.ReadCustomObjectSettings();
        this.ReadReplaceSettings();
        this.ReadResourceSettings();
        this.heightMatrix = new double[this.worldConfig.WorldHeight / 8 + 1];
        this.readHeightSettings(this.heightMatrix, TCDefaultValues.CustomHeightControl);
        this.riverHeightMatrix = new double[this.worldConfig.WorldHeight / 8 + 1];
        this.readHeightSettings(this.riverHeightMatrix, TCDefaultValues.RiverCustomHeightControl);
    }

    private void readHeightSettings(double[] heightMatrix, TCSetting setting)
    {
        ArrayList<String> keys = readSettings(setting);
        try
        {
            for (int i = 0; i < heightMatrix.length && i < keys.size(); i++)
            {
                heightMatrix[i] = Double.parseDouble(keys.get(i));
            }
        } catch (NumberFormatException e)
        {
            logSettingValueInvalid(setting.name(), e);
        }
    }

    private void ReadReplaceSettings()
    {
        String settingValue = readSettings(TCDefaultValues.ReplacedBlocks);

        if (settingValue.equals("") || settingValue.equals("None"))
            return;

        String[] keys = readComplexString(settingValue);
        try
        {
            for (String key : keys)
            {

                int start = key.indexOf("(");
                int end = key.lastIndexOf(")");
                if (start != -1 && end != -1)
                {
                    key = key.substring(start + 1, end);
                    String[] values = key.split(",");
                    if (values.length == 5)
                    {
                        // Replace in TC 2.3 style found
                        values = new String[] {values[0], values[1] + ":" + values[2], values[3], "" + (Integer.parseInt(values[4]) - 1)};
                    }

                    if (values.length != 2 && values.length != 4)
                        continue;

                    short fromBlockId = (short) StringHelper.readBlockId(values[0]);
                    short toBlockId = (short) StringHelper.readBlockId(values[1]);
                    short blockData = (short) StringHelper.readBlockData(values[1]);

                    int minY = 0;
                    int maxY = worldConfig.WorldHeight - 1;

                    if (values.length == 4)
                    {
                        minY = Integer.valueOf(values[2]);
                        maxY = Integer.valueOf(values[3]);
                        minY = applyBounds(minY, 0, worldConfig.WorldHeight - 1);
                        maxY = applyBounds(maxY, minY, worldConfig.WorldHeight - 1);
                    }

                    if (this.replaceMatrixBlocks[fromBlockId] == null)
                    {
                        this.replaceMatrixBlocks[fromBlockId] = new short[worldConfig.WorldHeight];
                        for (int i = 0; i < worldConfig.WorldHeight; i++)
                            this.replaceMatrixBlocks[fromBlockId][i] = -1;
                    }
                    for (int y = minY; y <= maxY; y++)
                        this.replaceMatrixBlocks[fromBlockId][y] = (short) (toBlockId << 4 | blockData);
                    ReplaceCount++;

                }

            }

        } catch (NumberFormatException e)
        {
            TerrainControl.log(Level.WARNING, "Wrong replace settings: ''{0}''", this.settingsCache.get(settingValue));
        } catch (InvalidConfigException e)
        {
            TerrainControl.log(Level.WARNING, "Wrong replace settings: ''{0}''", this.settingsCache.get(settingValue));
        }

    }

    private void ReadResourceSettings()
    {
        for (Map.Entry<String, String> entry : this.settingsCache.entrySet())
        {
            String key = entry.getKey();
            int start = key.indexOf("(");
            int end = key.lastIndexOf(")");
            if (start != -1 && end != -1)
            {
                String name = key.substring(0, start);
                String[] props = readComplexString(key.substring(start + 1, end));

                ConfigFunction<BiomeConfig> res = TerrainControl.getConfigFunctionsManager().getConfigFunction(name, this, this.name + " on line " + entry.getValue(), Arrays.asList(props));

                if (res != null)
                {

                    if (res instanceof SaplingGen && res.isValid())
                    {
                        SaplingGen sapling = (SaplingGen) res;
                        if (sapling.saplingType == SaplingType.All)
                            this.saplingResource = sapling;
                        else
                            this.saplingTypes[sapling.saplingType.getSaplingId()] = sapling;

                    } else if (res instanceof Resource)
                    {
                        this.resourceSequence.add((Resource) res);
                    }
                }
            }
        }
    }

    private void ReadCustomObjectSettings()
    {
        biomeObjects = new ArrayList<CustomObject>();
        biomeObjectStrings = new ArrayList<String>();

        // Read from BiomeObjects setting
        List<String> customObjectStrings = readSettings(TCDefaultValues.BiomeObjects);
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
    protected void writeConfigSettings() throws IOException
    {
        if (this.Biome.isCustom())
        {
            writeComment("This is the biome config file of the " + this.name + " biome, which is a custom biome.");
        } else
        {
            writeComment("This is the biome config file of the " + this.name + " biome, which is one of the vanilla biomes.");
        }

        // Biome placement
        writeBigTitle("Biome placement");

        writeComment("Biome size from 0 to GenerationDepth. Defines in which biome layer this biome will be generated (see GenerationDepth).");
        writeComment("Higher numbers give a smaller biome, lower numbers a larger biome.");
        writeComment("Oceans and rivers are generated using a dirrerent algorithm in the default settings,");
        writeComment("(they aren't in one of the biome lists), so this setting won't affect them.");
        writeValue(TCDefaultValues.BiomeSize, this.BiomeSize);

        writeComment("Biome rarity from 100 to 1. If this is normal or ice biome - chance for spawn this biome then others.");
        writeComment("Example for normal biome :");
        writeComment("  100 rarity mean 1/6 chance than other ( with 6 default normal biomes).");
        writeComment("  50 rarity mean 1/11 chance than other");
        writeComment("For isle biome this is chance to spawn isle in good place.");
        writeComment("Don`t work on Ocean and River (frozen versions too) biomes until not added as normal biome.");
        writeValue(TCDefaultValues.BiomeRarity, this.BiomeRarity);

        writeComment("The hexadecimal color value of this biome. Used in the output of the /tc map command,");
        writeComment("and used in the input of BiomeMode:FromImage.");
        writeValue(TCDefaultValues.BiomeColor, this.BiomeColor);

        writeComment("Replace this biome to specified after all generations. Warning this will cause saplings and mob spawning work as in specified biome");
        writeValue(TCDefaultValues.ReplaceToBiomeName, this.ReplaceBiomeName);

        writeSmallTitle("Isle biomes only");

        writeComment("Biome name list where this biome will be spawned as isle. Like Mushroom isle in Ocean.  This work only if this biome is in IsleBiomes in world config");
        writeValue(TCDefaultValues.IsleInBiome, this.IsleInBiome);

        writeSmallTitle("Border biomes only");

        writeComment("Biome name list where this biome will be border.Like Mushroom isle shore. Use is compared as IsleInBiome");
        writeValue(TCDefaultValues.BiomeIsBorder, this.BiomeIsBorder);

        writeComment("Biome name list near border is not applied. ");
        writeValue(TCDefaultValues.NotBorderNear, this.NotBorderNear);

        // Terrain height and volatility
        writeBigTitle("Terrain height and volatility");

        writeComment("BiomeHeight mean how much height will be added in terrain generation");
        writeComment("It is double value from -10.0 to 10.0");
        writeComment("Value 0.0 equivalent half of map height with all other default settings");
        writeValue(TCDefaultValues.BiomeHeight, this.BiomeHeight);

        writeComment("Biome volatility.");
        writeValue(TCDefaultValues.BiomeVolatility, this.BiomeVolatility);

        writeComment("Smooth radius between biomes. Must be between 0 and 32, inclusive. The resulting");
        writeComment("smooth radius seems to be  (thisSmoothRadius + 1 + smoothRadiusOfBiomeOnOtherSide) * 4 .");
        writeComment("So if two biomes next to each other have both a smooth radius of 2, the");
        writeComment("resulting smooth area will be (2 + 1 + 2) * 4 = 20 blocks wide.");
        writeValue(TCDefaultValues.SmoothRadius, this.SmoothRadius);

        writeComment("If this value is greater than 0, then it will affect how much, on average, the terrain will rise before leveling off when it begins to increase in elevation.");
        writeComment("If the value is less than 0, then it will cause the terrain to either increase to a lower height before leveling out or decrease in height if the value is a large enough negative.");
        writeValue(TCDefaultValues.MaxAverageHeight, this.maxAverageHeight);

        writeComment("If this value is greater than 0, then it will affect how much, on average, the terrain (usually at the ottom of the ocean) will fall before leveling off when it begins to decrease in elevation. ");
        writeComment("If the value is less than 0, then it will cause the terrain to either fall to a lesser depth before leveling out or increase in height if the value is a large enough negative.");
        writeValue(TCDefaultValues.MaxAverageDepth, this.maxAverageDepth);

        writeComment("Another type of noise. This noise is independent from biomes. The larger the values the more chaotic/volatile landscape generation becomes.");
        writeComment("Setting the values to negative will have the opposite effect and make landscape generation calmer/gentler.");
        writeValue(TCDefaultValues.Volatility1, this.volatilityRaw1);
        writeValue(TCDefaultValues.Volatility2, this.volatilityRaw2);

        writeComment("Adjust the weight of the corresponding volatility settings. This allows you to change how prevalent you want either of the volatility settings to be in the terrain.");
        writeValue(TCDefaultValues.VolatilityWeight1, this.volatilityWeightRaw1);
        writeValue(TCDefaultValues.VolatilityWeight2, this.volatilityWeightRaw2);

        writeComment("Disable all noises except Volatility1 and Volatility2. Also disable default block chance from height.");
        writeValue(TCDefaultValues.DisableBiomeHeight, this.disableNotchHeightControl);

        writeComment("List of custom height factor, 17 double entries, each entire control of about 7 blocks height from down. Positive entry - better chance of spawn blocks, negative - smaller");
        writeComment("Values which affect your configuration may be found only experimental. That may be very big, like ~3000.0 depends from height");
        writeComment("Example:");
        writeComment("  CustomHeightControl:0.0,-2500.0,0.0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0");
        writeComment("Make empty layer above bedrock layer. ");
        writeHeightSettings(this.heightMatrix, TCDefaultValues.CustomHeightControl);

        writeBigTitle("Rivers");
        writeComment("There are two different river systems - the standard one and the improved one.");
        writeComment("See the ImprovedRivers settting in the WorldConfig. Both modes have different");
        writeComment("river settings, so carefully read the headers to know which settings you can use.");
        writeComment("");

        writeSmallTitle("ImprovedRivers:false");
        writeComment("Only available when ImprovedRivers is set to false in the WorldConfig.");
        writeComment("Sets which biome is used as the river biome.");
        writeValue(TCDefaultValues.RiverBiome, this.riverBiome);

        writeSmallTitle("ImprovedRivers:true");
        writeComment("Only available when ImprovedRivers is set to true in the WorldConfig.");
        writeComment("");
        writeComment("Works the same as BiomeHeight (scroll up), but is used where a river is generated in this biome");
        writeValue(TCDefaultValues.RiverHeight, this.riverHeight);

        writeComment("Works the same as BiomeVolatility (scroll up), but is used where a river is generated in this biome");
        writeValue(TCDefaultValues.RiverVolatility, this.riverVolatility);

        writeComment("Works the same as WaterLevelMax (scroll down), but is used where a river is generated in this biome");
        writeComment("Can be used to create elevated rivers");
        writeValue(TCDefaultValues.RiverWaterLevel, this.riverWaterLevel);

        writeComment("Works the same as CustomHeightControl (scroll up), but is used where a river is generated in this biome");
        writeHeightSettings(this.riverHeightMatrix, TCDefaultValues.RiverCustomHeightControl);

        this.writeBigTitle("Blocks");

        writeComment("Stone block id");
        writeValue(TCDefaultValues.StoneBlock, this.StoneBlock);

        writeComment("Surface block id");
        writeValue(TCDefaultValues.SurfaceBlock, this.SurfaceBlock);

        writeComment("Block id from stone to surface, like dirt in plain biome ");
        writeValue(TCDefaultValues.GroundBlock, this.GroundBlock);

        writeComment("Replace Variable: (blockFrom,blockTo[:blockDataTo][,minHeight,maxHeight])");
        writeComment("Example :");
        writeComment("  ReplacedBlocks:(GRASS,DIRT,100,127),(GRAVEL,GLASS)");
        writeComment("Replace grass block to dirt from 100 to 127 height and replace gravel to glass on all height ");
        WriteModReplaceSettings();

        this.writeSmallTitle("Water and ice");

        writeComment("Set this to false to use the water and ice settings of this biome.");
        writeValue(TCDefaultValues.UseWorldWaterLevel, this.UseWorldWaterLevel);

        writeComment("Set water level. Every empty between this levels will be fill water or another block from WaterBlock.");
        writeValue(TCDefaultValues.WaterLevelMax, this.waterLevelMax);
        writeValue(TCDefaultValues.WaterLevelMin, this.waterLevelMin);

        writeComment("BlockId used as water in WaterLevelMax");
        writeValue(TCDefaultValues.WaterBlock, this.waterBlock);

        writeComment("BlockId used as ice. Ice only spawns if the BiomeTemperture is low enough.");
        writeValue(TCDefaultValues.IceBlock, this.iceBlock);

        this.writeBigTitle("Visuals and weather");
        this.writeComment("Most of the settings here only have an effect on players with the client version of Terrain Control installed.");

        writeComment("Biome temperature. Float value from 0.0 to 1.0.");
        writeValue(TCDefaultValues.BiomeTemperature, this.BiomeTemperature);

        writeComment("Biome wetness. Float value from 0.0 to 1.0.");
        writeValue(TCDefaultValues.BiomeWetness, this.BiomeWetness);

        this.writeComment("Biome sky color.");
        this.writeColorValue(TCDefaultValues.SkyColor, this.SkyColor);

        this.writeComment("Biome water color multiplier.");
        this.writeColorValue(TCDefaultValues.WaterColor, this.WaterColor);

        this.writeComment("Biome grass color.");
        this.writeColorValue(TCDefaultValues.GrassColor, this.GrassColor);

        this.writeComment("Whether the grass color is a multiplier.");
        this.writeComment("If you set it to true, the color will be based on this value, the BiomeTemperature and the BiomeWetness.");
        this.writeComment("If you set it to false, the grass color will be just this color.");
        this.writeValue(TCDefaultValues.GrassColorIsMultiplier, this.GrassColorIsMultiplier);

        this.writeComment("Biome foliage color.");
        this.writeColorValue(TCDefaultValues.FoliageColor, this.FoliageColor);

        this.writeComment("Whether the foliage color is a multiplier. See GrassColorIsMultiplier for details.");
        this.writeValue(TCDefaultValues.FoliageColorIsMultiplier, this.FoliageColorIsMultiplier);

        this.writeBigTitle("Resource queue");
        this.writeComment("This section control all resources spawning after terrain generation.");
        this.writeComment("The resources will be placed in this order.");
        this.writeComment("");
        this.writeComment("Keep in mind that a high size, frequency or rarity might slow down terrain generation.");
        this.writeComment("");
        this.writeComment("Possible resources:");
        this.writeComment("SmallLake(Block[:Data],Frequency,Rarity,MinAltitude,MaxAltitude)");
        this.writeComment("Dungeon(Frequency,Rarity,MinAltitude,MaxAltitude)");
        this.writeComment("UnderGroundLake(MinSize,MaxSize,Frequency,Rarity,MinAltitude,MaxAltitude)");
        this.writeComment("Ore(Block[:Data],Size,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.writeComment("UnderWaterOre(Block[:Data],Size,Frequency,Rarity,BlockSource[,BlockSource2,BlockSource3.....])");
        this.writeComment("CustomObject(Object[,AnotherObject[,...]])");
        this.writeComment("CustomStructure([Object,Object_Chance[,AnotherObject,Object_Chance[,...]]])");
        this.writeComment("Tree(Frequency,TreeType,TreeType_Chance[,Additional_TreeType,Additional_TreeType_Chance.....])");
        this.writeComment("Plant(PlantType,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.writeComment("Grass(PlantType,Unused,Frequency,Rarity,BlockSource[,BlockSource2,BlockSource3.....])");
        this.writeComment("Reed(Block[:Data],Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.writeComment("Cactus(Block[:Data],Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.writeComment("Liquid(Block[:Data],Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.writeComment("AboveWaterRes(Block[:Data],Frequency,Rarity)");
        this.writeComment("Vines(Frequency,Rarity,MinAltitude,MaxAltitude)");
        this.writeComment("Vein(Block[:Data],MinRadius,MaxRadius,Rarity,OreSize,OreFrequency,OreRarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,..])");
        this.writeComment("Well(BaseBlock[:Data],HalfSlabBlock[:Data],WaterBlock[:Data],Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,..])");
        this.writeComment("");
        this.writeComment("Block and BlockSource: can be id or name, Frequency - is count of attempts for place resource");
        this.writeComment("Rarity: chance for each attempt, Rarity:100 - mean 100% to pass, Rarity:1 - mean 1% to pass");
        this.writeComment("MinAltitude and MaxAltitude: height limits");
        this.writeComment("BlockSource: mean where or whereupon resource will be placed ");
        this.writeComment("TreeType: Tree (original oak tree) - BigTree - Birch - TallBirch - SwampTree");
        this.writeComment("   HugeMushroom (not a tree but still counts) - Taiga1 - Taiga2 - HugeTaiga1 - HugeTaiga2");
        this.writeComment("   JungleTree (the huge jungle tree) - GroundBush - CocoaTree (smaller jungle tree)");
        this.writeComment("   DarkOak (from the roofed forest biome) - Acacia");
        this.writeComment("   You can also use your own custom objects, as long as they have set Tree to true in their settings.");
        this.writeComment("TreeType_Chance: similar Rarity. Example:");
        this.writeComment("  Tree(10,Taiga1,35,Taiga2,100) - plugin tries 10 times, for each attempt it tries to place Taiga1 (35% chance),");
        this.writeComment("  if that fails, it attempts to place Taiga2 (100% chance).");
        this.writeComment("PlantType: one of the plant types: " + StringHelper.join(PlantType.values(), ", "));
        this.writeComment("           or simply Block[:Data]");
        this.writeComment("Object: can be a any kind of custom object (bo2 or bo3) but without the file extension. You can");
        this.writeComment("also use UseWorld to spawn one of the object in the WorldObjects folder and UseBiome to spawn");
        this.writeComment("one of the objects in the BiomeObjects setting. When using BO2s for UseWorld, the BO2 must have");
        this.writeComment("this biome in their spawnInBiome setting.");
        this.writeComment("Object_Chance: Like TreeType_Chance.");
        this.writeComment("Unused: Not used anymore. Ignore it.");
        this.writeComment("");
        this.writeComment("Plant and Grass resource: both a resource of one block. Plant can place blocks underground, Grass cannot.");
        this.writeComment("Liquid resource: a one-block water or lava source");
        this.writeComment("SmallLake and UnderGroundLake resources: small lakes of about 8x8 blocks");
        this.writeComment("Vein resource: not in vanilla. Starts an area where ores will spawn. Can be slow, so use a low Rarity (smaller than 1).");
        this.writeComment("CustomStructure resource: starts a BO3 structure in the chunk.");
        this.writeComment("");

        this.WriteResources();

        this.writeBigTitle("Sapling resource");
        this.writeComment("Terrain Control allows you to grow your custom objects from saplings, instead");
        this.writeComment("of the vanilla trees. Add one or more Sapling functions here to override vanilla");
        this.writeComment("spawning for that sapling.");
        this.writeComment("");
        this.writeComment("The syntax is: Sapling(SaplingType,TreeType,TreeType_Chance[,Additional_TreeType,Additional_TreeType_Chance.....])");
        this.writeComment("Works like Tree resource instead first parameter.");
        this.writeComment("");
        this.writeComment("Sapling types: " + StringHelper.join(SaplingType.values(), ", "));
        this.writeComment("All - will make the tree spawn from all saplings, but not from mushrooms.");
        this.writeComment("BigJungle - for when 4 jungle saplings grow at once.");
        this.writeComment("RedMushroom/BrownMushroom - will only grow when bonemeal is used.");
        this.WriteSaplingSettings();

        this.writeBigTitle("Custom objects");
        this.writeComment("These objects will spawn when using the UseBiome keyword.");
        this.WriteCustomObjects();

        this.writeBigTitle("Structures");
        this.writeComment("Here you can change, enable or disable the stuctures.");
        this.writeComment("If you have disabled the structure in the WorldConfig, it won't spawn,");
        this.writeComment("regardless of these settings.");

        this.writeComment("Disables strongholds for this biome. If there is no suitable biome nearby,");
        this.writeComment("Minecraft will ignore this setting.");
        this.writeValue(TCDefaultValues.StrongholdsEnabled, strongholdsEnabled);

        this.writeComment("Whether a Nether Fortress can start in this biome. Might extend to neighbor biomes.");
        this.writeValue(TCDefaultValues.NetherFortressesEnabled, netherFortressesEnabled);

        this.writeComment("The village type in this biome. Can be wood, sandstone or disabled.");
        this.writeValue(TCDefaultValues.VillageType, villageType.toString());

        this.writeComment("The mineshaft rarity from 0 to 100. 0 = no mineshafts, 1 = default rarity, 100 = a wooden chaos.");
        this.writeValue(TCDefaultValues.MineshaftRarity, mineshaftsRarity);

        this.writeComment("The type of the aboveground rare building in this biome. Can be desertPyramid, jungleTemple, swampHut or disabled.");
        this.writeValue(TCDefaultValues.RareBuildingType, rareBuildingType.toString());

        this.writeBigTitle("Mob spawning");
        if (DefaultBiome.getBiome(this.Biome.getId()) != null)
        {
            // Stop in the default biomes
            this.writeComment("Mob spawning control doesn't work in default biomes.");
            return;
        }

        this.writeComment("========<TUTORIAL>========");
        this.writeComment("This is where you configure mob spawning. Changing this section is optional.");
        this.writeComment("");
        this.writeComment("#STEP1: Understanding what a mobgroup is.");
        this.writeComment("A mobgroups is made of four parts. They are mob, weight, min and max.");
        this.writeComment("The mob is one of the Minecraft internal mob names.");
        this.writeComment("See http://www.minecraftwiki.net/wiki/Chunk_format#Mobs");
        this.writeComment("The weight is used for a random selection. This is a positive integer.");
        this.writeComment("The min is the minimum amount of mobs spawning as a group. This is a positive integer.");
        this.writeComment("The max is the maximum amount of mobs spawning as a group. This is a positive integer.");
        this.writeComment("");
        this.writeComment("#STEP2: Understanding how write a mobgroup as JSON as well as lists of them.");
        this.writeComment("Json is a tree document format: http://en.wikipedia.org/wiki/JSON");
        this.writeComment("Write a mobgroup like this: {\"mob\": \"mobname\", \"weight\": integer, \"min\": integer, \"max\": integer}");
        this.writeComment("For example: {\"mob\": \"Ocelot\", \"weight\": 10, \"min\": 2, \"max\": 6}");
        this.writeComment("For example: {\"mob\": \"MushroomCow\", \"weight\": 5, \"min\": 2, \"max\": 2}");
        this.writeComment("A json list of mobgroups looks like this: [mobgroup, mobgroup, mobgroup...]");
        this.writeComment("This would be an ampty list: []");
        this.writeComment("You can validate your json here: http://jsonlint.com/");
        this.writeComment("");
        this.writeComment("#STEP3: Understanding what to do with all this info");
        this.writeComment("There are three categories of mobs: monsters, creatures and watercreatures.");
        this.writeComment("These list may be populated with Default values if thee booleans bellow is set to true");
        this.writeComment("You may also add your own mobgroups in the lists below");
        this.writeComment("");
        this.writeComment("#STEP4: What is in the default mob groups?");
        this.writeComment("The default mob groups are controlled by vanilla minecraft.");
        this.writeComment("At 2012-03-24 you could find them here: https://github.com/Bukkit/mc-dev/blob/master/net/minecraft/server/BiomeBase.java#L75");
        this.writeComment("In simple terms:");
        this.writeComment("default creatures: [{\"mob\": \"Sheep\", \"weight\": 12, \"min\": 4, \"max\": 4}, {\"mob\": \"Pig\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Chicken\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Cow\", \"weight\": 8, \"min\": 4, \"max\": 4}]");
        this.writeComment("default monsters: [{\"mob\": \"Spider\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Zombie\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Skeleton\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Creeper\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Slime\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Enderman\", \"weight\": 1, \"min\": 1, \"max\": 4}]");
        this.writeComment("default watercreatures: [{\"mob\": \"Squid\", \"weight\": 10, \"min\": 4, \"max\": 4}]");
        this.writeComment("");
        this.writeComment("So for example ocelots wont spawn unless you add them below.");

        this.writeComment("========<CONFIGURATION>========");

        this.writeComment("Should we add the default monster spawn groups?");
        writeValue(TCDefaultValues.SpawnMonstersAddDefaults, this.spawnMonstersAddDefaults);
        this.writeComment("Add extra monster spawn groups here");
        writeValue(TCDefaultValues.SpawnMonsters, this.spawnMonsters);

        this.writeComment("Should we add the default creature spawn groups?");
        writeValue(TCDefaultValues.SpawnCreaturesAddDefaults, this.spawnCreaturesAddDefaults);
        this.writeComment("Add extra creature spawn groups here");
        writeValue(TCDefaultValues.SpawnCreatures, this.spawnCreatures);

        this.writeComment("Should we add the default watercreature spawn groups?");
        writeValue(TCDefaultValues.SpawnWaterCreaturesAddDefaults, this.spawnWaterCreaturesAddDefaults);
        this.writeComment("Add extra watercreature spawn groups here");
        writeValue(TCDefaultValues.SpawnWaterCreatures, this.spawnWaterCreatures);

        this.writeComment("Should we add the default ambient creature spawn groups? (Currently only bats)");
        writeValue(TCDefaultValues.SpawnAmbientCreaturesAddDefaults, this.spawnAmbientCreaturesAddDefaults);
        this.writeComment("Add extra ambient creature spawn groups here");
        writeValue(TCDefaultValues.SpawnAmbientCreatures, this.spawnAmbientCreatures);

    }

    private void writeHeightSettings(double[] heightMatrix, TCSetting setting) throws IOException
    {
        String output = Double.toString(heightMatrix[0]);
        for (int i = 1; i < heightMatrix.length; i++)
            output = output + "," + Double.toString(heightMatrix[i]);

        this.writeValue(setting, output);
    }

    private void WriteModReplaceSettings() throws IOException
    {
        if (this.ReplaceCount == 0)
        {
            this.writeValue(TCDefaultValues.ReplacedBlocks, "None");
            return;
        }
        String output = "";

        // Read all block ids
        for (int blockIdFrom = 0; blockIdFrom < replaceMatrixBlocks.length; blockIdFrom++)
        {
            if (replaceMatrixBlocks[blockIdFrom] == null)
                continue;

            int previousReplaceTo = -1; // What the y coord just below had it's
                                        // replace setting set to
            int yStart = 0;

            for (int y = 0; y <= replaceMatrixBlocks[blockIdFrom].length; y++)
            {
                int currentReplaceTo = (y == replaceMatrixBlocks[blockIdFrom].length) ? -1 : replaceMatrixBlocks[blockIdFrom][y];

                if (currentReplaceTo == previousReplaceTo)
                {
                    // Same as the previous entry, do nothing
                    continue;
                }

                // Not the same as the previous entry, previous entry wasn't -1
                // So we have found the end of a replace setting
                if (previousReplaceTo != -1)
                {
                    output += "(" + StringHelper.makeMaterial(blockIdFrom) + ",";
                    if (yStart != 0 || y != (replaceMatrixBlocks[blockIdFrom].length))
                    {
                        // Long form
                        output += StringHelper.makeMaterial(previousReplaceTo >> 4, previousReplaceTo & 0xF) + "," + yStart + "," + (y - 1);
                    } else
                    {
                        // Short form
                        output += StringHelper.makeMaterial(previousReplaceTo >> 4, previousReplaceTo & 0xF);
                    }
                    output += "),";

                    // Reset the previousReplaceTo
                    previousReplaceTo = -1;
                }

                if (previousReplaceTo == -1)
                {
                    // Not the same as the previous entry, previous entry was -1
                    // So we have found the start of a new replace setting
                    yStart = y;
                    previousReplaceTo = currentReplaceTo;
                }
            }
        }
        this.writeValue(TCDefaultValues.ReplacedBlocks, output.substring(0, output.length() - 1));
    }

    private void WriteResources() throws IOException
    {
        for (Resource resource : this.resourceSequence)
        {
            this.writeFunction(resource);
        }
    }

    private void WriteCustomObjects() throws IOException
    {
        ArrayList<String> objectStrings = new ArrayList<String>(biomeObjectStrings.size());
        for (String objectString : biomeObjectStrings)
        {
            objectStrings.add(objectString);
        }
        this.writeValue(TCDefaultValues.BiomeObjects, objectStrings);
    }

    private void WriteSaplingSettings() throws IOException
    {
        if (this.saplingResource != null)
            this.writeFunction(saplingResource);

        for (SaplingGen res : this.saplingTypes)
            if (res != null)
                this.writeFunction(res);

    }

    protected void correctSettings()
    {
        this.BiomeSize = applyBounds(this.BiomeSize, 0, this.worldConfig.GenerationDepth);
        this.BiomeHeight = (float) applyBounds(this.BiomeHeight, -10.0, 10.0);
        this.BiomeRarity = applyBounds(this.BiomeRarity, 1, this.worldConfig.BiomeRarityScale);

        this.SmoothRadius = applyBounds(this.SmoothRadius, 0, 32);

        this.BiomeTemperature = applyBounds(this.BiomeTemperature, 0.0F, 1.0F);
        this.BiomeWetness = applyBounds(this.BiomeWetness, 0.0F, 1.0F);

        this.IsleInBiome = filterBiomes(this.IsleInBiome, this.worldConfig.CustomBiomes);
        this.BiomeIsBorder = filterBiomes(this.BiomeIsBorder, this.worldConfig.CustomBiomes);
        this.NotBorderNear = filterBiomes(this.NotBorderNear, this.worldConfig.CustomBiomes);

        this.volatility1 = this.volatilityRaw1 < 0.0D ? 1.0D / (Math.abs(this.volatilityRaw1) + 1.0D) : this.volatilityRaw1 + 1.0D;
        this.volatility2 = this.volatilityRaw2 < 0.0D ? 1.0D / (Math.abs(this.volatilityRaw2) + 1.0D) : this.volatilityRaw2 + 1.0D;

        this.volatilityWeight1 = (this.volatilityWeightRaw1 - 0.5D) * 24.0D;
        this.volatilityWeight2 = (0.5D - this.volatilityWeightRaw2) * 24.0D;

        this.waterLevelMin = applyBounds(this.waterLevelMin, 0, this.worldConfig.WorldHeight - 1);
        this.waterLevelMax = applyBounds(this.waterLevelMax, 0, this.worldConfig.WorldHeight - 1, this.waterLevelMin);

        this.ReplaceBiomeName = (DefaultBiome.Contain(this.ReplaceBiomeName) || this.worldConfig.CustomBiomes.contains(this.ReplaceBiomeName)) ? this.ReplaceBiomeName : "";

        this.riverBiome = (DefaultBiome.Contain(this.riverBiome) || this.worldConfig.CustomBiomes.contains(this.riverBiome)) ? this.riverBiome : "";
    }

    protected void renameOldSettings()
    {
        // Old values from WorldConfig
        TCDefaultValues[] copyFromWorld = {TCDefaultValues.MaxAverageHeight, TCDefaultValues.MaxAverageDepth, TCDefaultValues.Volatility1, TCDefaultValues.Volatility2, TCDefaultValues.VolatilityWeight1, TCDefaultValues.VolatilityWeight2, TCDefaultValues.DisableBiomeHeight, TCDefaultValues.CustomHeightControl};
        for (TCDefaultValues value : copyFromWorld)
            if (this.worldConfig.settingsCache.containsKey(value.name().toLowerCase()))
            {
                // this.SettingsCache.put(value.name(),
                // this.worldConfig.SettingsCache.get(value.name().toLowerCase()));
                this.settingsCache.put(value.name().toLowerCase(), this.worldConfig.settingsCache.get(value.name().toLowerCase()));
            }

        // disableNotchPonds
        if (this.settingsCache.containsKey("disableNotchPonds".toLowerCase()))
        {
            if (!readModSettings(TCDefaultValues.DisableNotchPonds, false))
            {
                this.settingsCache.put("SmallLake(WATER,4,7,8," + this.worldConfig.WorldHeight + ")", "0");
                this.settingsCache.put("SmallLake(LAVA,2,3,8," + (this.worldConfig.WorldHeight - 8) + ")", "1");
            }

        }

        // CustomTreeChance
        int customTreeChance = 0; // Default value
        if (worldConfig.settingsCache.containsKey("customtreechance"))
        {
            try
            {
                customTreeChance = Integer.parseInt(worldConfig.settingsCache.get("customtreechance"));
            } catch (NumberFormatException e)
            {
                // Ignore, so leave customTreeChance at 0
            }
        }
        if (customTreeChance == 100)
        {
            this.settingsCache.put("Sapling(All,UseWorld,100)", "-");
        }
        if (customTreeChance > 0 && customTreeChance < 100)
        {
            this.settingsCache.put("Sapling(0,UseWorld," + customTreeChance + ",BigTree,10,Tree,100)", "-"); // Oak
            this.settingsCache.put("Sapling(1,UseWorld," + customTreeChance + ",Taiga2,100)", "-"); // Redwood
            this.settingsCache.put("Sapling(2,UseWorld," + customTreeChance + ",Forest,100)", "-"); // Birch
            this.settingsCache.put("Sapling(3,UseWorld," + customTreeChance + ",CocoaTree,100)", "-"); // Jungle
        }

        // FrozenRivers
        if (this.worldConfig.readModSettings(TCDefaultValues.FrozenRivers, true) == false)
        {
            // User had disabled frozen rivers in the old WorldConfig
            // So ignore the default value of RiverBiome
            this.settingsCache.put("riverbiome", "River");
        }

        // BiomeRivers
        if (!(Boolean) readSettings(TCDefaultValues.BiomeRivers))
        {
            // If the rivers were disabled using the old setting, disable them
            // also using the new setting
            // (Overrides FrozenRivers: false)
            this.settingsCache.put("riverbiome", "");
        }

        // ReplacedBlocks
        String replacedBlocksValue = readSettings(TCDefaultValues.ReplacedBlocks);

        if (replacedBlocksValue.contains("="))
        {
            String[] values = replacedBlocksValue.split(",");
            String output = "";

            for (String replacedBlock : values)
            {
                try
                {
                    String fromId = replacedBlock.split("=")[0];
                    String toId = replacedBlock.split("=")[1];

                    String toData = "0";
                    String minHeight = "0";
                    int maxHeight = worldConfig.WorldHeight;

                    boolean longForm = false;

                    int start = toId.indexOf("(");
                    int end = toId.indexOf(")");
                    if (start != -1 && end != -1)
                    {
                        String[] ranges = toId.substring(start + 1, end).split("-");
                        toId = toId.substring(0, start);
                        minHeight = ranges[0];
                        maxHeight = Integer.parseInt(ranges[1]);
                        longForm = true;
                    }
                    if (toId.contains("."))
                    {
                        String[] temp = toId.split("\\.");
                        toId = temp[0];
                        toData = temp[1];
                        longForm = true;
                    }

                    if (longForm)
                        output = output + "(" + fromId + "," + toId + ":" + toData + "," + minHeight + "," + (maxHeight - 1) + "),";
                    else
                        output = output + "(" + fromId + "," + toId + "),";
                } catch (Exception ignored)
                {
                }

            }

            this.settingsCache.put("replacedblocks", output.substring(0, output.length() - 1));
        }
    }

    public void Serialize(DataOutputStream stream) throws IOException
    {
        writeStringToStream(stream, this.name);

        stream.writeFloat(this.BiomeTemperature);
        stream.writeFloat(this.BiomeWetness);
        stream.writeInt(this.SkyColor);
        stream.writeInt(this.WaterColor);
        stream.writeInt(this.GrassColor);
        stream.writeBoolean(this.GrassColorIsMultiplier);
        stream.writeInt(this.FoliageColor);
        stream.writeBoolean(this.FoliageColorIsMultiplier);
    }

    public BiomeConfig(DataInputStream stream, WorldConfig config, LocalBiome biome) throws IOException
    {
        super(readStringFromStream(stream), null);
        this.Biome = biome;
        this.worldConfig = config;

        this.BiomeTemperature = stream.readFloat();
        this.BiomeWetness = stream.readFloat();
        this.SkyColor = stream.readInt();
        this.WaterColor = stream.readInt();
        this.GrassColor = stream.readInt();
        this.GrassColorIsMultiplier = stream.readBoolean();
        this.FoliageColor = stream.readInt();
        this.FoliageColorIsMultiplier = stream.readBoolean();
    }

}