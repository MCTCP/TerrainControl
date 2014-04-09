package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.standard.BiomeStandardValues;
import com.khorn.terraincontrol.configuration.standard.StandardBiomeTemplate;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.UseBiome;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.generator.resource.*;
import com.khorn.terraincontrol.generator.surface.MesaSurfaceGenerator;
import com.khorn.terraincontrol.generator.surface.SimpleSurfaceGenerator;
import com.khorn.terraincontrol.generator.surface.SurfaceGenerator;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.MultiTypedSetting;
import com.khorn.terraincontrol.util.helpers.InheritanceHelper;
import com.khorn.terraincontrol.util.helpers.StringHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class BiomeConfig extends ConfigFile
{
    /*
     * Biome Inheritance: String name of the biome to extend
     */

    public String BiomeExtends;
    public boolean BiomeExtendsProcessed = false;

    private boolean processHasRun = false;
    private boolean doResourceInheritance = true;

    public String riverBiome;
    public float riverHeight;
    public float riverVolatility;
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

    public float biomeTemperature;
    public float biomeWetness;

    public LocalMaterialData stoneBlock;
    public LocalMaterialData surfaceBlock;
    public LocalMaterialData groundBlock;
    public ReplacedBlocksMatrix replacedBlocks;
    public SurfaceGenerator surfaceAndGroundControl;

    public String ReplaceBiomeName;

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

    public int SkyColor;
    public int WaterColor;

    public int GrassColor;
    public boolean GrassColorIsMultiplier;
    public int FoliageColor;
    public boolean FoliageColorIsMultiplier;

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


    public BiomeConfig(BiomeLoadInstruction loadInstruction, File biomeFile, WorldConfig worldConfig)
    {
        super(loadInstruction.getBiomeName(), biomeFile);
        this.generationId = loadInstruction.getGenerationId();
        this.worldConfig = worldConfig;
        this.defaultSettings = loadInstruction.getBiomeTemplate();

        this.readSettingsFile(false);

        // Read this setting early, before inheritance is applied
        this.BiomeExtends = readSettings(BiomeStandardValues.BiomeExtends);
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
        if (!this.BiomeExtends.isEmpty())
        {
            // Child Inheritance Biomes
            if (this.worldConfig.SettingsMode != WorldConfig.ConfigMode.WriteDisable)
            {
                this.file = new File(this.file.getParentFile(), this.file.getName() + ".inherited");
                this.writeSettingsFile(this.worldConfig.SettingsMode == WorldConfig.ConfigMode.WriteAll);
            }
        } else
        {
            // Normal config saving
            if (this.worldConfig.SettingsMode != WorldConfig.ConfigMode.WriteDisable)
                this.writeSettingsFile(this.worldConfig.SettingsMode == WorldConfig.ConfigMode.WriteAll);
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
        this.BiomeExtends = readModSettings(BiomeStandardValues.BiomeExtends, defaultSettings.defaultExtends);
        this.BiomeSize = readModSettings(BiomeStandardValues.BiomeSize, defaultSettings.defaultSize);
        this.BiomeRarity = readModSettings(BiomeStandardValues.BiomeRarity, defaultSettings.defaultRarity);

        this.BiomeColor = readModSettings(BiomeStandardValues.BiomeColor, defaultSettings.defaultColor);

        this.riverBiome = readModSettings(BiomeStandardValues.RiverBiome, defaultSettings.defaultRiverBiome);

        this.IsleInBiome = readModSettings(BiomeStandardValues.IsleInBiome, defaultSettings.defaultIsle);
        this.BiomeIsBorder = readModSettings(BiomeStandardValues.BiomeIsBorder, defaultSettings.defaultBorder);
        this.NotBorderNear = readModSettings(BiomeStandardValues.NotBorderNear, defaultSettings.defaultNotBorderNear);

        this.biomeTemperature = readModSettings(BiomeStandardValues.BiomeTemperature, defaultSettings.defaultBiomeTemperature);
        this.biomeWetness = readModSettings(BiomeStandardValues.BiomeWetness, defaultSettings.defaultBiomeWetness);

        if (this.defaultSettings.isCustomBiome)
        {
            this.ReplaceBiomeName = readSettings(BiomeStandardValues.ReplaceToBiomeName);
        } else
        {
            this.ReplaceBiomeName = "";
        }

        this.BiomeHeight = readModSettings(BiomeStandardValues.BiomeHeight, defaultSettings.defaultBiomeSurface);
        this.BiomeVolatility = readModSettings(BiomeStandardValues.BiomeVolatility, defaultSettings.defaultBiomeVolatility);
        this.SmoothRadius = readSettings(BiomeStandardValues.SmoothRadius);

        this.stoneBlock = readSettings(BiomeStandardValues.StoneBlock);
        this.surfaceBlock = readModSettings(BiomeStandardValues.SurfaceBlock, defaultSettings.defaultSurfaceBlock);
        this.groundBlock = readModSettings(BiomeStandardValues.GroundBlock, defaultSettings.defaultGroundBlock);
        this.surfaceAndGroundControl = readSurfaceAndGroundControlSettings();

        this.useWorldWaterLevel = readSettings(BiomeStandardValues.UseWorldWaterLevel);
        this.configWaterLevelMax = readSettings(BiomeStandardValues.WaterLevelMax);
        this.configWaterLevelMin = readSettings(BiomeStandardValues.WaterLevelMin);
        this.configWaterBlock = readSettings(BiomeStandardValues.WaterBlock);
        this.configIceBlock = readSettings(BiomeStandardValues.IceBlock);

        this.SkyColor = readSettings(BiomeStandardValues.SkyColor);
        this.WaterColor = readModSettingsColor(BiomeStandardValues.WaterColor, defaultSettings.defaultWaterColorMultiplier);
        this.GrassColor = readModSettingsColor(BiomeStandardValues.GrassColor, defaultSettings.defaultGrassColor);
        this.GrassColorIsMultiplier = readSettings(BiomeStandardValues.GrassColorIsMultiplier);
        this.FoliageColor = readModSettingsColor(BiomeStandardValues.FoliageColor, defaultSettings.defaultFoliageColor);
        this.FoliageColorIsMultiplier = readSettings(BiomeStandardValues.FoliageColorIsMultiplier);

        this.volatilityRaw1 = readSettings(BiomeStandardValues.Volatility1);
        this.volatilityRaw2 = readSettings(BiomeStandardValues.Volatility2);
        this.volatilityWeightRaw1 = readSettings(BiomeStandardValues.VolatilityWeight1);
        this.volatilityWeightRaw2 = readSettings(BiomeStandardValues.VolatilityWeight2);
        this.disableNotchHeightControl = readSettings(BiomeStandardValues.DisableBiomeHeight);
        this.maxAverageHeight = readSettings(BiomeStandardValues.MaxAverageHeight);
        this.maxAverageDepth = readSettings(BiomeStandardValues.MaxAverageDepth);

        this.riverHeight = readSettings(BiomeStandardValues.RiverHeight);
        this.riverVolatility = readSettings(BiomeStandardValues.RiverVolatility);
        this.configRiverWaterLevel = readSettings(BiomeStandardValues.RiverWaterLevel);

        this.strongholdsEnabled = readModSettings(BiomeStandardValues.StrongholdsEnabled, defaultSettings.defaultStrongholds);
        this.netherFortressesEnabled = readModSettings(BiomeStandardValues.NetherFortressesEnabled, true);
        this.villageType = (VillageType) readModSettings(BiomeStandardValues.VillageType, defaultSettings.defaultVillageType);
        this.mineshaftsRarity = readSettings(BiomeStandardValues.MineshaftRarity);
        this.rareBuildingType = (RareBuildingType) readModSettings(BiomeStandardValues.RareBuildingType,
                                                                   defaultSettings.defaultRareBuildingType);

        if (this.defaultSettings.isCustomBiome)
        {
            // Only for custom biomes
            this.spawnMonstersAddDefaults = readSettings(BiomeStandardValues.SpawnMonstersAddDefaults);
            this.spawnMonsters = readModSettings(BiomeStandardValues.SpawnMonsters, new ArrayList<WeightedMobSpawnGroup>());
            this.spawnCreaturesAddDefaults = readSettings(BiomeStandardValues.SpawnCreaturesAddDefaults);
            this.spawnCreatures = readModSettings(BiomeStandardValues.SpawnCreatures, new ArrayList<WeightedMobSpawnGroup>());
            this.spawnWaterCreaturesAddDefaults = readSettings(BiomeStandardValues.SpawnWaterCreaturesAddDefaults);
            this.spawnWaterCreatures = readModSettings(BiomeStandardValues.SpawnWaterCreatures, new ArrayList<WeightedMobSpawnGroup>());
            this.spawnAmbientCreaturesAddDefaults = readSettings(BiomeStandardValues.SpawnAmbientCreaturesAddDefaults);
            this.spawnAmbientCreatures = readModSettings(BiomeStandardValues.SpawnAmbientCreatures, new ArrayList<WeightedMobSpawnGroup>());
        }

        this.ReadCustomObjectSettings();
        this.readReplaceSettings();
        this.ReadResourceSettings();
        this.heightMatrix = new double[this.worldConfig.worldHeightCap / 8 + 1];
        this.readHeightSettings(this.heightMatrix, BiomeStandardValues.CustomHeightControl);
        this.riverHeightMatrix = new double[this.worldConfig.worldHeightCap / 8 + 1];
        this.readHeightSettings(this.riverHeightMatrix, BiomeStandardValues.RiverCustomHeightControl);
    }

    private void readHeightSettings(double[] heightMatrix, MultiTypedSetting setting)
    {
        ArrayList<String> keys = readSettings(setting);
        try
        {
            for (int i = 0; i < heightMatrix.length && i < keys.size(); i++)
                heightMatrix[i] = Double.parseDouble(keys.get(i));
        } catch (NumberFormatException e)
        {
            logSettingValueInvalid(setting.name(), e);
        }
    }

    private SurfaceGenerator readSurfaceAndGroundControlSettings()
    {
        String defaultValue = this.isNewConfig ? StringHelper.join(defaultSettings.defaultSurfaceSurfaceAndGroundControl, ",") : "";
        String settingValue = readModSettings(BiomeStandardValues.SurfaceAndGroundControl, defaultValue);
        if (settingValue.length() > 0)
        {
            SurfaceGenerator mesa = MesaSurfaceGenerator.getFor(settingValue);
            if (mesa != null)
            {
                return mesa;
            }
            try
            {
                String[] parts = StringHelper.readCommaSeperatedString(settingValue);
                return new SimpleSurfaceGenerator(parts);
            } catch (InvalidConfigException e)
            {
                logSettingValueInvalid(BiomeStandardValues.SurfaceAndGroundControl.name());
            }
        }
        return null;
    }

    private void readReplaceSettings()
    {
        String settingValue = readSettings(BiomeStandardValues.ReplacedBlocks);

        try
        {
            this.replacedBlocks = new ReplacedBlocksMatrix(settingValue, worldConfig.worldHeightCap - 1);
        } catch (InvalidConfigException e)
        {
            // Make sure value is never null
            this.replacedBlocks = ReplacedBlocksMatrix.createEmptyMatrix(worldConfig.worldHeightCap - 1);

            // Print warning
            TerrainControl.log(LogMarker.WARN, "Wrong replace settings '{}': {}", (Object) this.settingsCache.get(settingValue), e.getMessage());
        }
    }

    private ConfigFunction<BiomeConfig> getResource(Map.Entry<String, String> entry)
    {
        String key = entry.getKey();
        int start = key.indexOf('(');
        int end = key.lastIndexOf(')');
        ConfigFunction<BiomeConfig> resource = null;
        if (start != -1 && end != -1)
        {
            String rName = key.substring(0, start);
            String[] props = StringHelper.readCommaSeperatedString(key.substring(start + 1, end));
            if (rName.equalsIgnoreCase("DoResourceInheritance") && props[0].equalsIgnoreCase("false"))
            {
                this.doResourceInheritance = false;
                // make resource non-null so it doesnt get copied as a
                // child,
                // but set the return value of getHolderType to null so we
                // can
                // check for it. Not sure if there is a better way to do
                // this?
                resource = new ConfigFunction<BiomeConfig>()
                {
                    @Override
                    protected void load(List<String> args) throws InvalidConfigException
                    {
                        throw new UnsupportedOperationException("This class is a placeholder");
                    }

                    @Override
                    public String makeString()
                    {
                        throw new UnsupportedOperationException("This class is a placeholder");
                    }

                    @Override
                    public Class<BiomeConfig> getHolderType()
                    {
                        return null;
                    }
                };
            } else
            {
                resource = TerrainControl.getConfigFunctionsManager().getConfigFunction(rName, this,
                                                                                        this.name + " on line " + entry.getValue(), Arrays.asList(props));
            }
        }
        return resource;
    }

    private void ReadResourceSettings()
    {
        this.doResourceInheritance = true;
        for (Map.Entry<String, String> entry : this.settingsCache.entrySet())
        {
            ConfigFunction<BiomeConfig> res = getResource(entry);
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
     * <p>
     * @param parent
     * @param child
     *               <p>
     * @return
     */
    public BiomeConfig merge(BiomeConfig parent)
    {
        // INHERITANCE VARIABLES -- START
        // Run down the list of child settings from config
        for (Map.Entry<String, String> childEntry : this.settingsCache.entrySet())
        { // As long as setting value contains `Interited` and it isnt
            // a resource
            String childValue = childEntry.getValue();
            if (childValue.toLowerCase().contains("inherited") && getResource(childEntry) == null)
            { // if the parent has the setting
                if (parent.settingsCache.containsKey(childEntry.getKey()))
                {
                    String parentValue = parent.settingsCache.get(childEntry.getKey());
                    if (parentValue.toLowerCase().contains("inherited"))
                    {
                        TerrainControl.log(LogMarker.FATAL, "Parent has `Inherited` keyword. Something is wrong. Please report this.");
                        continue;
                    }
                    childEntry.setValue(InheritanceHelper.evaluate(childValue, parentValue));
                    TerrainControl.log(LogMarker.FATAL, "Setting `{}` replaced with `{}`", new Object[] { childEntry.getKey(), InheritanceHelper.evaluate(childValue, parentValue) });
                }
            }
        }

        // INHERITANCE VARIABLES -- END
        // Run down the list of parent settings from config
        for (Map.Entry<String, String> parentEntry : parent.settingsCache.entrySet())
        {
            // As long as the child does not have the setting and it isnt a
            // resource
            if (!this.settingsCache.containsKey(parentEntry.getKey()) && getResource(parentEntry) == null)
            {
                // Give it to the child from the parent
                this.settingsCache.put(parentEntry.getKey(), parentEntry.getValue());
                // And let us know if we are producing FINE logs
                TerrainControl.log(LogMarker.TRACE, "Setting({},{})",
                        new Object[] {parentEntry.getKey(), parent.settingsCache.get(parentEntry.getKey())});
            }
        }
        // Now really process both parent and child so that both have their
        // final non-resource properties
        this.process();
        parent.process();
        // We dont want to merge resources unless the child allows it.
        TerrainControl.log(LogMarker.TRACE, "=====Doing Resource Inheritance====");
        if (this.doResourceInheritance)
        {
            // Then do the resource merge! Start with Resource Sequence.
            ArrayList<Resource> T_ResourceSequence = new ArrayList<Resource>(this.resourceSequence);
            ArrayList<Resource> childRes = new ArrayList<Resource>(this.resourceSequence);
            ArrayList<Resource> parentRes = new ArrayList<Resource>(parent.resourceSequence);
            childRes.removeAll(Collections.singleton(null));
            parentRes.removeAll(Collections.singleton(null));
            for (Resource pr : parentRes)
            {
                boolean analagous = false;
                TerrainControl.log(LogMarker.TRACE, "BASE:: Checking against: {}", new Object[] { pr.makeString() });
                for (Resource cr : childRes)
                {
                    TerrainControl.log(LogMarker.TRACE, "CHCK:: Checking against: {}", new Object[] { cr.makeString() });
                    if (cr.isAnalogousTo(pr))
                    {
                        TerrainControl.log(LogMarker.TRACE, "Adding Child Resource\nC: {}\nP: {}",
                                new Object[] {cr.makeString(), pr.makeString()});
                        T_ResourceSequence.add(cr);
                        childRes.remove(cr);
                        analagous = true;
                        break;
                    }
                }
                if (!analagous)
                {
                    TerrainControl.log(LogMarker.TRACE, "Adding Parent Resource\n{}", new Object[] {pr.makeString()});
                    T_ResourceSequence.add(pr);
                }
            }
            this.resourceSequence = new ArrayList<Resource>(T_ResourceSequence);
            // if the child is using All saplings then we dont need to merge
            if (!this.saplingGrowers.containsKey(SaplingType.All))
            {
                // take all parent sapling types and if child doesn't have
                // them, insert
                for (Entry<SaplingType, SaplingGen> saplingEntry : parent.saplingGrowers.entrySet())
                {
                    SaplingType saplingType = saplingEntry.getKey();
                    SaplingGen saplingGen = saplingEntry.getValue();
                    if (!this.saplingGrowers.containsKey(saplingType))
                    {
                        TerrainControl.log(LogMarker.TRACE, "Sapling added to Child: {}", saplingType);
                        this.saplingGrowers.put(saplingType, saplingGen);
                    }
                }
            } else
            {
                TerrainControl.log(LogMarker.TRACE, "No Sapling merge needed!");
            }
        }

        this.BiomeExtendsProcessed = true;
        TerrainControl.log(LogMarker.TRACE, "=====END Resource Inheritance====");
        return this;
    }

    private void ReadCustomObjectSettings()
    {
        biomeObjects = new ArrayList<CustomObject>();
        biomeObjectStrings = new ArrayList<String>();

        // Read from BiomeObjects setting
        List<String> customObjectStrings = readSettings(BiomeStandardValues.BiomeObjects);
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
        if (this.defaultSettings.isCustomBiome)
        {
            writeComment("This is the biome config file of the " + this.name + " biome, which is a custom biome.");
        } else
        {
            writeComment("This is the biome config file of the " + this.name + " biome, which is one of the vanilla biomes.");
        }

        writeBigTitle("Biome Inheritance");
        writeComment("This should be the value of the biomeConfig you wish to extend.");
        writeComment("The extended config will be loaded, at which point the configs included below");
        writeComment("will overwrite any configs loaded from the extended config.");
        writeValue(BiomeStandardValues.BiomeExtends, this.BiomeExtends);

        // Biome placement
        writeBigTitle("Biome placement");

        writeComment("Biome size from 0 to GenerationDepth. Defines in which biome layer this biome will be generated (see GenerationDepth).");
        writeComment("Higher numbers give a smaller biome, lower numbers a larger biome.");
        writeComment("Oceans and rivers are generated using a dirrerent algorithm in the default settings,");
        writeComment("(they aren't in one of the biome lists), so this setting won't affect them.");
        writeValue(BiomeStandardValues.BiomeSize, this.BiomeSize);

        writeComment("Biome rarity from 100 to 1. If this is normal or ice biome - chance for spawn this biome then others.");
        writeComment("Example for normal biome :");
        writeComment("  100 rarity mean 1/6 chance than other ( with 6 default normal biomes).");
        writeComment("  50 rarity mean 1/11 chance than other");
        writeComment("For isle biome this is chance to spawn isle in good place.");
        writeComment("Don`t work on Ocean and River (frozen versions too) biomes until not added as normal biome.");
        writeValue(BiomeStandardValues.BiomeRarity, this.BiomeRarity);

        writeComment("The hexadecimal color value of this biome. Used in the output of the /tc map command,");
        writeComment("and used in the input of BiomeMode:FromImage.");
        writeValue(BiomeStandardValues.BiomeColor, this.BiomeColor);

        if (this.defaultSettings.isCustomBiome)
        {
            writeComment("Replace this biome to specified after the terrain is generated.");
            writeComment("This will make the world files contain the id of the specified biome, instead of the id of this biome.");
            writeComment("This will cause saplings, colors and mob spawning work as in specified biome.");
            writeValue(BiomeStandardValues.ReplaceToBiomeName, this.ReplaceBiomeName);
        } else
        {
            writeComment("(ReplaceToBiomeName is only available in custom biomes.)");
            writeComment("");
        }

        writeSmallTitle("Isle biomes only");

        writeComment("Biome name list where this biome will be spawned as isle. Like Mushroom isle in Ocean.  This work only if this biome is in IsleBiomes in world config");
        writeValue(BiomeStandardValues.IsleInBiome, this.IsleInBiome);

        writeSmallTitle("Border biomes only");

        writeComment("Biome name list where this biome will be border.Like Mushroom isle shore. Use is compared as IsleInBiome");
        writeValue(BiomeStandardValues.BiomeIsBorder, this.BiomeIsBorder);

        writeComment("Biome name list near border is not applied. ");
        writeValue(BiomeStandardValues.NotBorderNear, this.NotBorderNear);

        // Terrain height and volatility
        writeBigTitle("Terrain height and volatility");

        writeComment("BiomeHeight mean how much height will be added in terrain generation");
        writeComment("It is double value from -10.0 to 10.0");
        writeComment("Value 0.0 equivalent half of map height with all other default settings");
        writeValue(BiomeStandardValues.BiomeHeight, this.BiomeHeight);

        writeComment("Biome volatility.");
        writeValue(BiomeStandardValues.BiomeVolatility, this.BiomeVolatility);

        writeComment("Smooth radius between biomes. Must be between 0 and 32, inclusive. The resulting");
        writeComment("smooth radius seems to be  (thisSmoothRadius + 1 + smoothRadiusOfBiomeOnOtherSide) * 4 .");
        writeComment("So if two biomes next to each other have both a smooth radius of 2, the");
        writeComment("resulting smooth area will be (2 + 1 + 2) * 4 = 20 blocks wide.");
        writeValue(BiomeStandardValues.SmoothRadius, this.SmoothRadius);

        writeComment("If this value is greater than 0, then it will affect how much, on average, the terrain will rise before leveling off when it begins to increase in elevation.");
        writeComment("If the value is less than 0, then it will cause the terrain to either increase to a lower height before leveling out or decrease in height if the value is a large enough negative.");
        writeValue(BiomeStandardValues.MaxAverageHeight, this.maxAverageHeight);

        writeComment("If this value is greater than 0, then it will affect how much, on average, the terrain (usually at the ottom of the ocean) will fall before leveling off when it begins to decrease in elevation. ");
        writeComment("If the value is less than 0, then it will cause the terrain to either fall to a lesser depth before leveling out or increase in height if the value is a large enough negative.");
        writeValue(BiomeStandardValues.MaxAverageDepth, this.maxAverageDepth);

        writeComment("Another type of noise. This noise is independent from biomes. The larger the values the more chaotic/volatile landscape generation becomes.");
        writeComment("Setting the values to negative will have the opposite effect and make landscape generation calmer/gentler.");
        writeValue(BiomeStandardValues.Volatility1, this.volatilityRaw1);
        writeValue(BiomeStandardValues.Volatility2, this.volatilityRaw2);

        writeComment("Adjust the weight of the corresponding volatility settings. This allows you to change how prevalent you want either of the volatility settings to be in the terrain.");
        writeValue(BiomeStandardValues.VolatilityWeight1, this.volatilityWeightRaw1);
        writeValue(BiomeStandardValues.VolatilityWeight2, this.volatilityWeightRaw2);

        writeComment("Disable all noises except Volatility1 and Volatility2. Also disable default block chance from height.");
        writeValue(BiomeStandardValues.DisableBiomeHeight, this.disableNotchHeightControl);

        writeComment("List of custom height factor, 17 double entries, each entire control of about 7 blocks height from down. Positive entry - better chance of spawn blocks, negative - smaller");
        writeComment("Values which affect your configuration may be found only experimental. That may be very big, like ~3000.0 depends from height");
        writeComment("Example:");
        writeComment("  CustomHeightControl:0.0,-2500.0,0.0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0");
        writeComment("Make empty layer above bedrock layer. ");
        writeHeightSettings(this.heightMatrix, BiomeStandardValues.CustomHeightControl);

        writeBigTitle("Rivers");
        writeComment("There are two different river systems - the standard one and the improved one.");
        writeComment("See the ImprovedRivers settting in the WorldConfig. Both modes have different");
        writeComment("river settings, so carefully read the headers to know which settings you can use.");
        writeComment("");

        writeSmallTitle("ImprovedRivers:false");
        writeComment("Only available when ImprovedRivers is set to false in the WorldConfig.");
        writeComment("Sets which biome is used as the river biome.");
        writeValue(BiomeStandardValues.RiverBiome, this.riverBiome);

        writeSmallTitle("ImprovedRivers:true");
        writeComment("Only available when ImprovedRivers is set to true in the WorldConfig.");
        writeComment("");
        writeComment("Works the same as BiomeHeight (scroll up), but is used where a river is generated in this biome");
        writeValue(BiomeStandardValues.RiverHeight, this.riverHeight);

        writeComment("Works the same as BiomeVolatility (scroll up), but is used where a river is generated in this biome");
        writeValue(BiomeStandardValues.RiverVolatility, this.riverVolatility);

        writeComment("Works the same as WaterLevelMax (scroll down), but is used where a river is generated in this biome");
        writeComment("Can be used to create elevated rivers");
        writeValue(BiomeStandardValues.RiverWaterLevel, this.configRiverWaterLevel);

        writeComment("Works the same as CustomHeightControl (scroll up), but is used where a river is generated in this biome");
        writeHeightSettings(this.riverHeightMatrix, BiomeStandardValues.RiverCustomHeightControl);

        this.writeBigTitle("Blocks");

        writeComment("Change this to generate something else than stone in the biome. Doesn't support block data.");
        writeValue(BiomeStandardValues.StoneBlock, this.stoneBlock);

        writeComment("Surface block, usually GRASS. Doesn't support block data.");
        writeValue(BiomeStandardValues.SurfaceBlock, this.surfaceBlock);

        writeComment("Block from stone to surface, like dirt in most biomes. Doesn't support block data.");
        writeValue(BiomeStandardValues.GroundBlock, this.groundBlock);

        writeComment("Setting for biomes with more complex surface and ground blocks.");
        writeComment("Each column in the world has a noise value from what appears to be -7 to 7.");
        writeComment("Values near 0 are more common than values near -7 and 7. This setting is");
        writeComment("used to change the surface block based on the noise value for the column.");
        writeComment("Syntax: SurfaceBlockName,GroundBlockName,MaxNoise,[AnotherSurfaceBlockName,[AnotherGroundBlockName,MaxNoise[,...]]");
        writeComment("Example: " + BiomeStandardValues.SurfaceAndGroundControl + ": STONE,STONE,-0.8,GRAVEL,STONE,0.0,DIRT,DIRT,10.0");
        writeComment("  When the noise is below -0.8, stone is the surface and ground block, between -0.8 and 0");
        writeComment("  gravel with stone just below and between 0.0 and 10.0 there's only dirt.");
        writeComment("  Because 10.0 is higher than the noise can ever get, the normal " + BiomeStandardValues.SurfaceBlock);
        writeComment("  and " + BiomeStandardValues.GroundBlock + " will never appear in this biome.");
        writeComment("");
        writeComment("Alternatively, you can use Mesa, MesaForest or MesaBryce to get blocks");
        writeComment("like the blocks found in the Mesa biomes.");
        writeValue(BiomeStandardValues.SurfaceAndGroundControl,
                   this.surfaceAndGroundControl == null ? "" : this.surfaceAndGroundControl.toString());

        writeComment("Replace Variable: (blockFrom,blockTo[:blockDataTo][,minHeight,maxHeight])");
        writeComment("Example :");
        writeComment("  ReplacedBlocks: (GRASS,DIRT,100,127),(GRAVEL,GLASS)");
        writeComment("Replace grass block to dirt from 100 to 127 height and replace gravel to glass on all height ");
        writeValue(BiomeStandardValues.ReplacedBlocks, replacedBlocks.toString());

        this.writeSmallTitle("Water and ice");

        writeComment("Set this to false to use the water and ice settings of this biome.");
        writeValue(BiomeStandardValues.UseWorldWaterLevel, this.useWorldWaterLevel);

        writeComment("Set water level. Every empty between this levels will be fill water or another block from WaterBlock.");
        writeValue(BiomeStandardValues.WaterLevelMax, this.configWaterLevelMax);
        writeValue(BiomeStandardValues.WaterLevelMin, this.configWaterLevelMin);

        writeComment("Block used as water in WaterLevelMax");
        writeValue(BiomeStandardValues.WaterBlock, this.configWaterBlock);

        writeComment("Block used as ice. Ice only spawns if the BiomeTemperture is low enough.");
        writeValue(BiomeStandardValues.IceBlock, this.configIceBlock);

        this.writeBigTitle("Visuals and weather");
        this.writeComment("Most of the settings here only have an effect on players with the client version of Terrain Control installed.");

        writeComment("Biome temperature. Float value from 0.0 to 2.0.");
        if (this.defaultSettings.isCustomBiome)
        {
            writeComment("When this value is around 0.2, snow will fall on mountain peaks above y=90.");
            writeComment("When this value is around 0.1, the whole biome will be covered in snow and ice.");
        } else
        {
            writeComment("On default biomes, this won't do anything except changing the grass and leaves colors slightly.");
        }
        writeValue(BiomeStandardValues.BiomeTemperature, this.biomeTemperature);

        writeComment("Biome wetness. Float value from 0.0 to 1.0.");
        if (this.defaultSettings.isCustomBiome)
        {
            writeComment("When this is set to 0, no rain will fall.");
        } else
        {
            writeComment("On default biomes, this won't do anything except changing the grass and leaves colors slightly.");
        }
        writeValue(BiomeStandardValues.BiomeWetness, this.biomeWetness);

        this.writeComment("Biome sky color.");
        this.writeColorValue(BiomeStandardValues.SkyColor, this.SkyColor);

        this.writeComment("Biome water color multiplier.");
        this.writeColorValue(BiomeStandardValues.WaterColor, this.WaterColor);

        this.writeComment("Biome grass color.");
        this.writeColorValue(BiomeStandardValues.GrassColor, this.GrassColor);

        this.writeComment("Whether the grass color is a multiplier.");
        this.writeComment("If you set it to true, the color will be based on this value, the BiomeTemperature and the BiomeWetness.");
        this.writeComment("If you set it to false, the grass color will be just this color.");
        this.writeValue(BiomeStandardValues.GrassColorIsMultiplier, this.GrassColorIsMultiplier);

        this.writeComment("Biome foliage color.");
        this.writeColorValue(BiomeStandardValues.FoliageColor, this.FoliageColor);

        this.writeComment("Whether the foliage color is a multiplier. See GrassColorIsMultiplier for details.");
        this.writeValue(BiomeStandardValues.FoliageColorIsMultiplier, this.FoliageColorIsMultiplier);

        this.writeBigTitle("Resource queue");
        this.writeComment("This section control all resources spawning after terrain generation.");
        this.writeComment("The resources will be placed in this order.");
        this.writeComment("");
        this.writeComment("Keep in mind that a high size, frequency or rarity might slow down terrain generation.");
        this.writeComment("");
        this.writeComment("Possible resources:");
        this.writeComment("DoResourceInheritance(true|false)");
        this.writeComment("SmallLake(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude)");
        this.writeComment("Dungeon(Frequency,Rarity,MinAltitude,MaxAltitude)");
        this.writeComment("UnderGroundLake(MinSize,MaxSize,Frequency,Rarity,MinAltitude,MaxAltitude)");
        this.writeComment("Ore(BlockName,Size,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.writeComment("UnderWaterOre(BlockName,Size,Frequency,Rarity,BlockSource[,BlockSource2,BlockSource3.....])");
        this.writeComment("CustomObject(Object[,AnotherObject[,...]])");
        this.writeComment("CustomStructure([Object,Object_Chance[,AnotherObject,Object_Chance[,...]]])");
        this.writeComment("Tree(Frequency,TreeType,TreeTypeChance[,AdditionalTreeType,AdditionalTreeTypeChance.....])");
        this.writeComment("Plant(PlantType,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.writeComment("Grass(PlantType,Grouped/NotGrouped,Frequency,Rarity,BlockSource[,BlockSource2,BlockSource3.....])");
        this.writeComment("Reed(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.writeComment("Cactus(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.writeComment("Liquid(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.writeComment("AboveWaterRes(BlockName,Frequency,Rarity)");
        this.writeComment("Vines(Frequency,Rarity,MinAltitude,MaxAltitude)");
        this.writeComment("Vein(BlockName,MinRadius,MaxRadius,Rarity,OreSize,OreFrequency,OreRarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,..])");
        this.writeComment("Well(BaseBlockName,HalfSlabBlockName,WaterBlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,..])");
        this.writeComment("Boulder(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,..]");
        this.writeComment("IceSpike(BlockName,IceSpikeType,Frequency,Rarity,MinAltitude,MaxAltitude,Blocksource[,BlockSource2,...])");
        this.writeComment("");
        this.writeComment("BlockName:      must be the name of a block. May include block data, like \"WOOL:1\".");
        this.writeComment("BlockSource:    list of blocks the resource can spawn on/in. You can also use \"Solid\" or \"All\".");
        this.writeComment("Frequency:      number of attempts to place this resource in each chunk.");
        this.writeComment("Rarity:         chance for each attempt, Rarity:100 - mean 100% to pass, Rarity:1 - mean 1% to pass.");
        this.writeComment("MinAltitude and MaxAltitude: height limits.");
        this.writeComment("BlockSource:    mean where or whereupon resource will be placed ");
        this.writeComment("TreeType:       Tree (original oak tree) - BigTree - Birch - TallBirch - SwampTree");
        this.writeComment("                HugeMushroom (not a tree but still counts) - Taiga1 - Taiga2 - HugeTaiga1 - HugeTaiga2");
        this.writeComment("                JungleTree (the huge jungle tree) - GroundBush - CocoaTree (smaller jungle tree)");
        this.writeComment("                DarkOak (from the roofed forest biome) - Acacia");
        this.writeComment("                You can also use your own custom objects, as long as they have set Tree to true in their settings.");
        this.writeComment("TreeTypeChance: similar to Rarity. Example:");
        this.writeComment("                Tree(10,Taiga1,35,Taiga2,100) - plugin tries 10 times, for each attempt it tries to place Taiga1 (35% chance),");
        this.writeComment("                if that fails, it attempts to place Taiga2 (100% chance).");
        this.writeComment("PlantType:      one of the plant types: " + StringHelper.join(PlantType.values(), ", "));
        this.writeComment("                or simply a BlockName");
        this.writeComment("IceSpikeType:   one of the ice spike types: " + StringHelper.join(IceSpikeGen.SpikeType.values(), ","));
        this.writeComment("Object:         can be a any kind of custom object (bo2 or bo3) but without the file extension. You can");
        this.writeComment("                also use UseWorld to spawn one of the object in the WorldObjects folder and UseBiome to spawn");
        this.writeComment("                one of the objects in the BiomeObjects setting. When using BO2s for UseWorld, the BO2 must have");
        this.writeComment("                this biome in their spawnInBiome setting.");
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
        this.writeValue(BiomeStandardValues.StrongholdsEnabled, strongholdsEnabled);

        this.writeComment("Whether a Nether Fortress can start in this biome. Might extend to neighbor biomes.");
        this.writeValue(BiomeStandardValues.NetherFortressesEnabled, netherFortressesEnabled);

        this.writeComment("The village type in this biome. Can be wood, sandstone or disabled.");
        this.writeValue(BiomeStandardValues.VillageType, villageType.toString());

        this.writeComment("The mineshaft rarity from 0 to 100. 0 = no mineshafts, 1 = default rarity, 100 = a wooden chaos.");
        this.writeValue(BiomeStandardValues.MineshaftRarity, mineshaftsRarity);

        this.writeComment("The type of the aboveground rare building in this biome. Can be desertPyramid, jungleTemple, swampHut or disabled.");
        this.writeValue(BiomeStandardValues.RareBuildingType, rareBuildingType.toString());

        this.writeBigTitle("Mob spawning");
        if (!this.defaultSettings.isCustomBiome)
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
        writeValue(BiomeStandardValues.SpawnMonstersAddDefaults, this.spawnMonstersAddDefaults);
        this.writeComment("Add extra monster spawn groups here");
        writeValue(BiomeStandardValues.SpawnMonsters, this.spawnMonsters);

        this.writeComment("Should we add the default creature spawn groups?");
        writeValue(BiomeStandardValues.SpawnCreaturesAddDefaults, this.spawnCreaturesAddDefaults);
        this.writeComment("Add extra creature spawn groups here");
        writeValue(BiomeStandardValues.SpawnCreatures, this.spawnCreatures);

        this.writeComment("Should we add the default watercreature spawn groups?");
        writeValue(BiomeStandardValues.SpawnWaterCreaturesAddDefaults, this.spawnWaterCreaturesAddDefaults);
        this.writeComment("Add extra watercreature spawn groups here");
        writeValue(BiomeStandardValues.SpawnWaterCreatures, this.spawnWaterCreatures);

        this.writeComment("Should we add the default ambient creature spawn groups? (Currently only bats)");
        writeValue(BiomeStandardValues.SpawnAmbientCreaturesAddDefaults, this.spawnAmbientCreaturesAddDefaults);
        this.writeComment("Add extra ambient creature spawn groups here");
        writeValue(BiomeStandardValues.SpawnAmbientCreatures, this.spawnAmbientCreatures);

    }

    private void writeHeightSettings(double[] heightMatrix, MultiTypedSetting setting) throws IOException
    {
        String output = Double.toString(heightMatrix[0]);
        for (int i = 1; i < heightMatrix.length; i++)
            output = output + "," + Double.toString(heightMatrix[i]);

        this.writeValue(setting, output);
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
        this.writeValue(BiomeStandardValues.BiomeObjects, objectStrings);
    }

    private void WriteSaplingSettings() throws IOException
    {
        for (SaplingGen res : this.saplingGrowers.values())
        {
            this.writeFunction(res);
        }
    }

    @Override
    protected void correctSettings()
    {
        this.BiomeExtends = (this.BiomeExtends == null || this.BiomeExtends.equals("null")) ? "" : this.BiomeExtends;
        this.BiomeSize = applyBounds(this.BiomeSize, 0, this.worldConfig.GenerationDepth);
        this.BiomeHeight = (float) applyBounds(this.BiomeHeight, -10.0, 10.0);
        this.BiomeRarity = applyBounds(this.BiomeRarity, 1, this.worldConfig.BiomeRarityScale);

        this.SmoothRadius = applyBounds(this.SmoothRadius, 0, 32);

        this.biomeTemperature = applyBounds(this.biomeTemperature, 0.0F, 2.0F);
        this.biomeWetness = applyBounds(this.biomeWetness, 0.0F, 1.0F);

        this.IsleInBiome = filterBiomes(this.IsleInBiome, this.worldConfig.CustomBiomeIds.keySet());
        this.BiomeIsBorder = filterBiomes(this.BiomeIsBorder, this.worldConfig.CustomBiomeIds.keySet());
        this.NotBorderNear = filterBiomes(this.NotBorderNear, this.worldConfig.CustomBiomeIds.keySet());

        this.volatility1 = this.volatilityRaw1 < 0.0D ? 1.0D / (Math.abs(this.volatilityRaw1) + 1.0D) : this.volatilityRaw1 + 1.0D;
        this.volatility2 = this.volatilityRaw2 < 0.0D ? 1.0D / (Math.abs(this.volatilityRaw2) + 1.0D) : this.volatilityRaw2 + 1.0D;

        this.volatilityWeight1 = (this.volatilityWeightRaw1 - 0.5D) * 24.0D;
        this.volatilityWeight2 = (0.5D - this.volatilityWeightRaw2) * 24.0D;

        this.waterLevelMin = applyBounds(this.waterLevelMin, 0, this.worldConfig.worldHeightCap - 1);
        this.waterLevelMax = applyBounds(this.waterLevelMax, 0, this.worldConfig.worldHeightCap - 1, this.waterLevelMin);

        this.ReplaceBiomeName = (DefaultBiome.Contain(this.ReplaceBiomeName) || this.worldConfig.CustomBiomeIds.keySet().contains(
                                 this.ReplaceBiomeName)) ? this.ReplaceBiomeName : "";

        this.riverBiome = (DefaultBiome.Contain(this.riverBiome) || this.worldConfig.CustomBiomeIds.keySet().contains(this.riverBiome)) ? this.riverBiome
                          : "";
    }

    @Override
    protected void renameOldSettings()
    {
        // Old values from WorldConfig
        BiomeStandardValues[] copyFromWorld = {BiomeStandardValues.MaxAverageHeight, BiomeStandardValues.MaxAverageDepth,
            BiomeStandardValues.Volatility1, BiomeStandardValues.Volatility2, BiomeStandardValues.VolatilityWeight1,
                BiomeStandardValues.VolatilityWeight2, BiomeStandardValues.DisableBiomeHeight, BiomeStandardValues.CustomHeightControl};
        for (BiomeStandardValues value : copyFromWorld)
            if (this.worldConfig.settingsCache.containsKey(value.name().toLowerCase()))
            {
                // this.SettingsCache.put(value.name(),
                // this.worldConfig.SettingsCache.get(value.name().toLowerCase()));
                this.settingsCache.put(value.name().toLowerCase(), this.worldConfig.settingsCache.get(value.name().toLowerCase()));
            }

        // disableNotchPonds
        if (this.settingsCache.containsKey("disableNotchPonds".toLowerCase()))
        {
            if (!readModSettings(BiomeStandardValues.DisableNotchPonds, false))
            {
                this.settingsCache.put("SmallLake(WATER,4,7,8," + this.worldConfig.worldHeightCap + ")", "0");
                this.settingsCache.put("SmallLake(LAVA,2,3,8," + (this.worldConfig.worldHeightCap - 8) + ")", "1");
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
        if (!this.worldConfig.readModSettings(WorldStandardValues.FrozenRivers, true))
        {
            // User had disabled frozen rivers in the old WorldConfig
            // So ignore the default value of RiverBiome
            this.settingsCache.put("riverbiome", "River");
        }

        // BiomeRivers
        if (!(Boolean) readSettings(BiomeStandardValues.BiomeRivers))
        {
            // If the rivers were disabled using the old setting, disable them
            // also using the new setting
            // (Overrides FrozenRivers: false)
            this.settingsCache.put("riverbiome", "");
        }

        // ReplacedBlocks
        String replacedBlocksValue = readSettings(BiomeStandardValues.ReplacedBlocks);

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
                    int maxHeight = worldConfig.worldHeightCap;

                    boolean longForm = false;

                    int start = toId.indexOf('(');
                    int end = toId.indexOf(')');
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

    public void writeToStream(DataOutputStream stream) throws IOException
    {
        writeStringToStream(stream, this.name);

        stream.writeFloat(this.biomeTemperature);
        stream.writeFloat(this.biomeWetness);
        stream.writeInt(this.SkyColor);
        stream.writeInt(this.WaterColor);
        stream.writeInt(this.GrassColor);
        stream.writeBoolean(this.GrassColorIsMultiplier);
        stream.writeInt(this.FoliageColor);
        stream.writeBoolean(this.FoliageColorIsMultiplier);
    }

    public BiomeConfig(DataInputStream stream, int generationId, WorldConfig config) throws IOException
    {
        super(readStringFromStream(stream), null);
        this.generationId = generationId;
        this.worldConfig = config;

        this.biomeTemperature = stream.readFloat();
        this.biomeWetness = stream.readFloat();
        this.SkyColor = stream.readInt();
        this.WaterColor = stream.readInt();
        this.GrassColor = stream.readInt();
        this.GrassColorIsMultiplier = stream.readBoolean();
        this.FoliageColor = stream.readInt();
        this.FoliageColorIsMultiplier = stream.readBoolean();
    }

}
