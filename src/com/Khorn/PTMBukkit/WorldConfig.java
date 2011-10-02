package com.Khorn.PTMBukkit;

import com.Khorn.PTMBukkit.CustomObjects.CustomObject;
import com.Khorn.PTMBukkit.Generator.ChunkProviderPTM;
import com.Khorn.PTMBukkit.Generator.ObjectSpawner;
import com.Khorn.PTMBukkit.Util.ConfigFile;
import net.minecraft.server.BiomeBase;
import net.minecraft.server.Block;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class WorldConfig extends ConfigFile
{
    public HashMap<Integer, Byte> replaceBlocks = new HashMap<Integer, Byte>();
    public byte[] ReplaceBlocksMatrix = new byte[256];


    public ArrayList<CustomObject> Objects = new ArrayList<CustomObject>();
    public HashMap<String, ArrayList<CustomObject>> ObjectGroups = new HashMap<String, ArrayList<CustomObject>>();
    public HashMap<String, ArrayList<CustomObject>> BranchGroups = new HashMap<String, ArrayList<CustomObject>>();
    public boolean HasCustomTrees = false;

    // public BiomeBase currentBiome;
    // --Commented out by Inspection (17.07.11 1:49):String seedValue;


    // For old biome generator
    public boolean oldBiomeGenerator;
    public double oldBiomeSize;

    public int biomeSize;
    public int landSize;
    public boolean riversEnabled;

    public float minMoisture;
    public float maxMoisture;
    public float minTemperature;
    public float maxTemperature;

    // For 1.9
    public double snowThreshold;
    public double iceThreshold;

    //Specific biome settings
    public boolean muddySwamps;
    public boolean claySwamps;
    public int swampSize;
    public boolean waterlessDeserts;
    public boolean desertDirt;
    public int desertDirtFrequency;

    //Caves
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

    //Canyons

    public int canyonRarity;
    public int canyonMinAltitude;
    public int canyonMaxAltitude;
    public int canyonMinLength;
    public int canyonMaxLength;
    public double canyonDepth;

    //Terrain

    public boolean oldTerrainGenerator;

    public int waterLevel;
    public int waterBlock;
    public double maxAverageHeight;
    public double maxAverageDepth;
    private double fractureHorizontal;
    private double fractureVertical;
    private double volatility1;
    private double volatility2;
    private double volatilityWeight1;
    private double volatilityWeight2;
    private boolean disableBedrock;
    private boolean flatBedrock;
    public boolean ceilingBedrock;
    private boolean bedrockObsidian;


    public boolean removeSurfaceStone;


    public boolean disableNotchHeightControl;
    public double[] heightMatrix = new double[17];


    public boolean customObjects;
    public int objectSpawnRatio;
    public boolean denyObjectsUnderFill;
    public int customTreeMinTime;
    public int customTreeMaxTime;

    public boolean StrongholdsEnabled;
    public boolean MineshaftsEnabled;
    public boolean VillagesEnabled;


    public boolean undergroundLakes;
    public boolean undergroundLakesInAir;
    public int undergroundLakeFrequency;
    public int undergroundLakeRarity;
    public int undergroundLakeMinSize;
    public int undergroundLakeMaxSize;
    public int undergroundLakeMinAltitude;
    public int undergroundLakeMaxAltitude;


    private File SettingsDir;
    public PTMPlugin plugin;
    public ChunkProviderPTM ChunkProvider;
    public ObjectSpawner objectSpawner;

    public boolean isInit = false;

    public boolean isDeprecated = false;
    public WorldConfig newSettings = null;

    public String WorldName;
    public GenMode Mode;

    public static int BiomesCount = 8;

    public BiomeConfig[] biomeConfigs = new BiomeConfig[BiomesCount];
    public boolean BiomeConfigsHaveReplacement = false;


    public int ChunkMaxY = 128;


    public WorldConfig(File settingsDir, PTMPlugin plug, String worldName)
    {
        this.SettingsDir = settingsDir;
        this.WorldName = worldName;

        File settingsFile = new File(this.SettingsDir, PTMDefaultValues.WorldSettingsName.stringValue());

        this.ReadSettingsFile(settingsFile);
        this.ReadConfigSettings();

        this.CorrectSettings();

        this.WriteSettingsFile(settingsFile);

        BuildReplaceMatrix();

        File BiomeFolder = new File(SettingsDir, PTMDefaultValues.WorldBiomeConfigDirectoryName.stringValue());
        if (!BiomeFolder.exists())
        {
            if (!BiomeFolder.mkdir())
            {
                System.out.println("PhoenixTerrainMod: error create biome configs directory, working with defaults");
                return;
            }
        }

        for (int i = 0; i < BiomesCount; i++)
        {
            BiomeConfig config = new BiomeConfig(BiomeFolder, BiomeBase.a[i], this);
            this.biomeConfigs[i] = config;
            if (!this.BiomeConfigsHaveReplacement)
                this.BiomeConfigsHaveReplacement = config.replaceBlocks.size() > 0;
        }

        this.RegisterBOBPlugins();
        this.plugin = plug;
    }

    public WorldConfig()
    {
    }

    public void CreateDefaultSettings(File pluginDir)
    {
        File settingsFile = new File(pluginDir, PTMDefaultValues.DefaultSettingsName.stringValue());
        this.ReadSettingsFile(settingsFile);
        this.ReadConfigSettings();

        this.CorrectSettings();

        this.WriteSettingsFile(settingsFile);

        for (int i = 0; i < BiomesCount; i++)
        {
            this.biomeConfigs[i] = new BiomeConfig(new File(pluginDir, PTMDefaultValues.DefaultBiomeConfigDirectoryName.stringValue()), BiomeBase.a[i], this);
        }


    }

    protected void CorrectSettings()
    {
        this.biomeSize = CheckValue(this.biomeSize, 1, 15);
        this.landSize = CheckValue(this.landSize, 1, 10);

        this.oldBiomeSize = (this.oldBiomeSize <= 0.0D ? 1.5D : this.oldBiomeSize);

        this.minMoisture = (this.minMoisture < 0.0F ? 0.0F : this.minMoisture > 1.0F ? 1.0F : this.minMoisture);
        this.minTemperature = (this.minTemperature < 0.0F ? 0.0F : this.minTemperature > 1.0F ? 1.0F : this.minTemperature);
        this.maxMoisture = (this.maxMoisture > 1.0F ? 1.0F : this.maxMoisture < this.minMoisture ? this.minMoisture : this.maxMoisture);
        this.maxTemperature = (this.maxTemperature > 1.0F ? 1.0F : this.maxTemperature < this.minTemperature ? this.minTemperature : this.maxTemperature);


        this.snowThreshold = (this.snowThreshold < 0.0D ? 0.0D : this.snowThreshold > 1.0D ? 1.0D : this.snowThreshold);
        this.iceThreshold = (this.iceThreshold < -1.0D ? -1.0D : this.iceThreshold > 1.0D ? 1.0D : this.iceThreshold);

        this.caveRarity = (this.caveRarity < 0 ? 0 : this.caveRarity > 100 ? 100 : this.caveRarity);
        this.caveFrequency = (this.caveFrequency < 0 ? 0 : this.caveFrequency);
        this.caveMinAltitude = (this.caveMinAltitude < 0 ? 0 : this.caveMinAltitude > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.caveMinAltitude);
        this.caveMaxAltitude = (this.caveMaxAltitude > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.caveMaxAltitude <= this.caveMinAltitude ? this.caveMinAltitude + 1 : this.caveMaxAltitude);
        this.individualCaveRarity = (this.individualCaveRarity < 0 ? 0 : this.individualCaveRarity);
        this.caveSystemFrequency = (this.caveSystemFrequency < 0 ? 0 : this.caveSystemFrequency);
        this.caveSystemPocketChance = (this.caveSystemPocketChance < 0 ? 0 : this.caveSystemPocketChance > 100 ? 100 : this.caveSystemPocketChance);
        this.caveSystemPocketMinSize = (this.caveSystemPocketMinSize < 0 ? 0 : this.caveSystemPocketMinSize);
        this.caveSystemPocketMaxSize = (this.caveSystemPocketMaxSize <= this.caveSystemPocketMinSize ? this.caveSystemPocketMinSize + 1 : this.caveSystemPocketMaxSize);


        this.canyonRarity = CheckValue(this.canyonRarity, 0, 100);
        this.canyonMinAltitude = CheckValue(this.canyonMinAltitude, 0, this.ChunkMaxY);
        this.canyonMaxAltitude = CheckValue(this.canyonMaxAltitude, 0, this.ChunkMaxY, this.canyonMinAltitude);
        this.canyonMinLength = CheckValue(this.canyonMinLength, 1, 500);
        this.canyonMaxLength = CheckValue(this.canyonMaxLength, 1, 500, this.canyonMinLength);
        this.canyonDepth = CheckValue(this.canyonDepth, 0.1D, 15D);


        this.waterLevel = (this.waterLevel < 0 ? 0 : this.waterLevel > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.waterLevel);

        this.undergroundLakeRarity = (this.undergroundLakeRarity < 0 ? 0 : this.undergroundLakeRarity > 100 ? 100 : this.undergroundLakeRarity);
        this.undergroundLakeFrequency = (this.undergroundLakeFrequency < 0 ? 0 : this.undergroundLakeFrequency);
        this.undergroundLakeMinSize = (this.undergroundLakeMinSize < 25 ? 25 : this.undergroundLakeMinSize);
        this.undergroundLakeMaxSize = (this.undergroundLakeMaxSize <= this.undergroundLakeMinSize ? this.undergroundLakeMinSize + 1 : this.undergroundLakeMaxSize);
        this.undergroundLakeMinAltitude = (this.undergroundLakeMinAltitude < 0 ? 0 : this.undergroundLakeMinAltitude > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.undergroundLakeMinAltitude);
        this.undergroundLakeMaxAltitude = (this.undergroundLakeMaxAltitude > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.undergroundLakeMaxAltitude <= this.undergroundLakeMinAltitude ? this.undergroundLakeMinAltitude + 1 : this.undergroundLakeMaxAltitude);

        this.customTreeMinTime = (this.customTreeMinTime < 1 ? 1 : this.customTreeMinTime);
        this.customTreeMaxTime = ((this.customTreeMaxTime - this.customTreeMinTime) < 1 ? (this.customTreeMinTime + 1) : this.customTreeMaxTime);

        if (this.oldBiomeGenerator && !this.oldTerrainGenerator)
        {
            System.out.println("PhoenixTerrainMod: Old biome generator works only with old terrain generator!");
            this.oldBiomeGenerator = false;

        }


    }


    protected void ReadConfigSettings()
    {
        try
        {
            this.Mode = GenMode.valueOf(ReadModSettings(PTMDefaultValues.Mode.name(), PTMDefaultValues.Mode.stringValue()));
        } catch (IllegalArgumentException e)
        {
            this.Mode = GenMode.Normal;
        }

        this.oldBiomeGenerator = ReadModSettings(PTMDefaultValues.oldBiomeGenerator.name(), PTMDefaultValues.oldBiomeGenerator.booleanValue());
        this.oldBiomeSize = ReadModSettings(PTMDefaultValues.oldBiomeSize.name(), PTMDefaultValues.oldBiomeSize.doubleValue());
        this.biomeSize = ReadModSettings(PTMDefaultValues.biomeSize.name(), PTMDefaultValues.biomeSize.intValue());
        this.landSize = ReadModSettings(PTMDefaultValues.landSize.name(), PTMDefaultValues.landSize.intValue());
        this.riversEnabled = ReadModSettings(PTMDefaultValues.riversEnabled.name(), PTMDefaultValues.riversEnabled.booleanValue());
        this.minMoisture = ReadModSettings(PTMDefaultValues.minMoisture.name(), PTMDefaultValues.minMoisture.floatValue());
        this.maxMoisture = ReadModSettings(PTMDefaultValues.maxMoisture.name(), PTMDefaultValues.maxMoisture.floatValue());
        this.minTemperature = ReadModSettings(PTMDefaultValues.minTemperature.name(), PTMDefaultValues.minTemperature.floatValue());
        this.maxTemperature = ReadModSettings(PTMDefaultValues.maxTemperature.name(), PTMDefaultValues.maxTemperature.floatValue());
        this.snowThreshold = ReadModSettings(PTMDefaultValues.snowThreshold.name(), PTMDefaultValues.snowThreshold.doubleValue());
        this.iceThreshold = ReadModSettings(PTMDefaultValues.iceThreshold.name(), PTMDefaultValues.iceThreshold.doubleValue());

        this.muddySwamps = ReadModSettings(PTMDefaultValues.muddySwamps.name(), PTMDefaultValues.muddySwamps.booleanValue());
        this.claySwamps = ReadModSettings(PTMDefaultValues.claySwamps.name(), PTMDefaultValues.claySwamps.booleanValue());
        this.swampSize = ReadModSettings(PTMDefaultValues.swampSize.name(), PTMDefaultValues.swampSize.intValue());

        this.waterlessDeserts = ReadModSettings(PTMDefaultValues.waterlessDeserts.name(), PTMDefaultValues.waterlessDeserts.booleanValue());
        this.desertDirt = ReadModSettings(PTMDefaultValues.desertDirt.name(), PTMDefaultValues.desertDirt.booleanValue());
        this.desertDirtFrequency = ReadModSettings(PTMDefaultValues.desertDirtFrequency.name(), PTMDefaultValues.desertDirtFrequency.intValue());


        this.StrongholdsEnabled = ReadModSettings(PTMDefaultValues.StrongholdsEnabled.name(), PTMDefaultValues.StrongholdsEnabled.booleanValue());
        this.VillagesEnabled = ReadModSettings(PTMDefaultValues.VillagesEnabled.name(), PTMDefaultValues.VillagesEnabled.booleanValue());
        this.MineshaftsEnabled = ReadModSettings(PTMDefaultValues.MineshaftsEnabled.name(), PTMDefaultValues.MineshaftsEnabled.booleanValue());


        this.caveRarity = ReadModSettings(PTMDefaultValues.caveRarity.name(), PTMDefaultValues.caveRarity.intValue());
        this.caveFrequency = ReadModSettings(PTMDefaultValues.caveFrequency.name(), PTMDefaultValues.caveFrequency.intValue());
        this.caveMinAltitude = ReadModSettings(PTMDefaultValues.caveMinAltitude.name(), PTMDefaultValues.caveMinAltitude.intValue());
        this.caveMaxAltitude = ReadModSettings(PTMDefaultValues.caveMaxAltitude.name(), PTMDefaultValues.caveMaxAltitude.intValue());
        this.individualCaveRarity = ReadModSettings(PTMDefaultValues.individualCaveRarity.name(), PTMDefaultValues.individualCaveRarity.intValue());
        this.caveSystemFrequency = ReadModSettings(PTMDefaultValues.caveSystemFrequency.name(), PTMDefaultValues.caveSystemFrequency.intValue());
        this.caveSystemPocketChance = ReadModSettings(PTMDefaultValues.caveSystemPocketChance.name(), PTMDefaultValues.caveSystemPocketChance.intValue());
        this.caveSystemPocketMinSize = ReadModSettings(PTMDefaultValues.caveSystemPocketMinSize.name(), PTMDefaultValues.caveSystemPocketMinSize.intValue());
        this.caveSystemPocketMaxSize = ReadModSettings(PTMDefaultValues.caveSystemPocketMaxSize.name(), PTMDefaultValues.caveSystemPocketMaxSize.intValue());
        this.evenCaveDistribution = ReadModSettings(PTMDefaultValues.evenCaveDistribution.name(), PTMDefaultValues.evenCaveDistribution.booleanValue());

        this.canyonRarity = ReadModSettings(PTMDefaultValues.canyonRarity.name(), PTMDefaultValues.canyonRarity.intValue());
        this.canyonMinAltitude = ReadModSettings(PTMDefaultValues.canyonMinAltitude.name(), PTMDefaultValues.canyonMinAltitude.intValue());
        this.canyonMaxAltitude = ReadModSettings(PTMDefaultValues.canyonMaxAltitude.name(), PTMDefaultValues.canyonMaxAltitude.intValue());
        this.canyonMinLength = ReadModSettings(PTMDefaultValues.canyonMinLength.name(), PTMDefaultValues.canyonMinLength.intValue());
        this.canyonMinLength = ReadModSettings(PTMDefaultValues.canyonMinLength.name(), PTMDefaultValues.canyonMinLength.intValue());
        this.canyonDepth = ReadModSettings(PTMDefaultValues.canyonDepth.name(), PTMDefaultValues.canyonDepth.doubleValue());


        this.waterLevel = ReadModSettings(PTMDefaultValues.waterLevel.name(), PTMDefaultValues.waterLevel.intValue());
        this.waterBlock = ReadModSettings(PTMDefaultValues.waterBlock.name(), PTMDefaultValues.waterBlock.intValue());
        this.maxAverageHeight = ReadModSettings(PTMDefaultValues.maxAverageHeight.name(), PTMDefaultValues.maxAverageHeight.doubleValue());
        this.maxAverageDepth = ReadModSettings(PTMDefaultValues.maxAverageDepth.name(), PTMDefaultValues.maxAverageDepth.doubleValue());
        this.fractureHorizontal = ReadModSettings(PTMDefaultValues.fractureHorizontal.name(), PTMDefaultValues.fractureHorizontal.doubleValue());
        this.fractureVertical = ReadModSettings(PTMDefaultValues.fractureVertical.name(), PTMDefaultValues.fractureVertical.doubleValue());
        this.volatility1 = ReadModSettings(PTMDefaultValues.volatility1.name(), PTMDefaultValues.volatility1.doubleValue());
        this.volatility2 = ReadModSettings(PTMDefaultValues.volatility2.name(), PTMDefaultValues.volatility2.doubleValue());
        this.volatilityWeight1 = ReadModSettings(PTMDefaultValues.volatilityWeight1.name(), PTMDefaultValues.volatilityWeight1.doubleValue());
        this.volatilityWeight2 = ReadModSettings(PTMDefaultValues.volatilityWeight2.name(), PTMDefaultValues.volatilityWeight2.doubleValue());
        this.disableNotchHeightControl = ReadModSettings(PTMDefaultValues.disableNotchHeightControl.name(), PTMDefaultValues.disableNotchHeightControl.booleanValue());

        this.disableBedrock = ReadModSettings(PTMDefaultValues.disableBedrock.name(), PTMDefaultValues.disableBedrock.booleanValue());
        this.ceilingBedrock = ReadModSettings(PTMDefaultValues.ceilingBedrock.name(), PTMDefaultValues.ceilingBedrock.booleanValue());
        this.flatBedrock = ReadModSettings(PTMDefaultValues.flatBedrock.name(), PTMDefaultValues.flatBedrock.booleanValue());
        this.bedrockObsidian = ReadModSettings(PTMDefaultValues.bedrockobsidian.name(), PTMDefaultValues.bedrockobsidian.booleanValue());

        ReadHeightSettings();

        this.oldTerrainGenerator = ReadModSettings(PTMDefaultValues.oldTerrainGenerator.name(), PTMDefaultValues.oldTerrainGenerator.booleanValue());
        this.removeSurfaceStone = ReadModSettings(PTMDefaultValues.removeSurfaceStone.name(), PTMDefaultValues.removeSurfaceStone.booleanValue());


        this.customObjects = this.ReadModSettings(PTMDefaultValues.customObjects.name(), PTMDefaultValues.customObjects.booleanValue());
        this.objectSpawnRatio = this.ReadModSettings(PTMDefaultValues.objectSpawnRatio.name(), PTMDefaultValues.objectSpawnRatio.intValue());
        this.denyObjectsUnderFill = this.ReadModSettings(PTMDefaultValues.denyObjectsUnderFill.name(), PTMDefaultValues.denyObjectsUnderFill.booleanValue());
        this.customTreeMinTime = this.ReadModSettings(PTMDefaultValues.customTreeMinTime.name(), PTMDefaultValues.customTreeMinTime.intValue());
        this.customTreeMaxTime = this.ReadModSettings(PTMDefaultValues.customTreeMaxTime.name(), PTMDefaultValues.customTreeMaxTime.intValue());


        this.undergroundLakes = this.ReadModSettings(PTMDefaultValues.undergroundLakes.name(), PTMDefaultValues.undergroundLakes.booleanValue());
        this.undergroundLakesInAir = this.ReadModSettings(PTMDefaultValues.undergroundLakesInAir.name(), PTMDefaultValues.undergroundLakesInAir.booleanValue());
        this.undergroundLakeFrequency = this.ReadModSettings(PTMDefaultValues.undergroundLakeFrequency.name(), PTMDefaultValues.undergroundLakeFrequency.intValue());
        this.undergroundLakeRarity = this.ReadModSettings(PTMDefaultValues.undergroundLakeRarity.name(), PTMDefaultValues.undergroundLakeRarity.intValue());
        this.undergroundLakeMinSize = this.ReadModSettings(PTMDefaultValues.undergroundLakeMinSize.name(), PTMDefaultValues.undergroundLakeMinSize.intValue());
        this.undergroundLakeMaxSize = this.ReadModSettings(PTMDefaultValues.undergroundLakeMaxSize.name(), PTMDefaultValues.undergroundLakeMaxSize.intValue());
        this.undergroundLakeMinAltitude = this.ReadModSettings(PTMDefaultValues.undergroundLakeMinAltitude.name(), PTMDefaultValues.undergroundLakeMinAltitude.intValue());
        this.undergroundLakeMaxAltitude = this.ReadModSettings(PTMDefaultValues.undergroundLakeMaxAltitude.name(), PTMDefaultValues.undergroundLakeMaxAltitude.intValue());


        this.ReadModReplaceSettings();


    }


    private void ReadModReplaceSettings()
    {
        if (this.SettingsCache.containsKey("ReplacedBlocks"))
        {
            if (this.SettingsCache.get("ReplacedBlocks").trim().equals("") || this.SettingsCache.get("ReplacedBlocks").equals("None"))
                return;
            String[] keys = this.SettingsCache.get("ReplacedBlocks").split(",");
            try
            {
                for (String key : keys)
                {

                    String[] blocks = key.split("=");

                    this.replaceBlocks.put(Integer.valueOf(blocks[0]), Byte.valueOf(blocks[1]));

                }

            } catch (NumberFormatException e)
            {
                System.out.println("Wrong replace settings: '" + this.SettingsCache.get("ReplacedBlocks") + "'");
            }

        }


    }

    private void ReadHeightSettings()
    {
        if (this.SettingsCache.containsKey("CustomHeightControl"))
        {
            if (this.SettingsCache.get("CustomHeightControl").trim().equals(""))
                return;
            String[] keys = this.SettingsCache.get("CustomHeightControl").split(",");
            try
            {
                if (keys.length != 17)
                    return;
                for (int i = 0; i < 17; i++)
                    this.heightMatrix[i] = Double.valueOf(keys[i]);

            } catch (NumberFormatException e)
            {
                System.out.println("Wrong height settings: '" + this.SettingsCache.get("CustomHeightControl") + "'");
            }

        }


    }

    private void BuildReplaceMatrix()
    {
        for (int i = 0; i < this.ReplaceBlocksMatrix.length; i++)
        {
            if (this.replaceBlocks.containsKey(i))
                this.ReplaceBlocksMatrix[i] = this.replaceBlocks.get(i);
            else
                this.ReplaceBlocksMatrix[i] = (byte) i;

        }
    }


    protected void WriteConfigSettings() throws IOException
    {
        WriteModTitleSettings("Possible modes : Normal, TerrainTest, NotGenerate");
        WriteModSettings(PTMDefaultValues.Mode.name(), this.Mode.name());

        WriteModTitleSettings("Biome Generator Variables");
        WriteModTitleSettings("Old biome generator works only with old terrain generator!");
        WriteModSettings(PTMDefaultValues.oldBiomeGenerator.name(), this.oldBiomeGenerator);
        WriteModSettings(PTMDefaultValues.oldBiomeSize.name(), this.oldBiomeSize);
        WriteModSettings(PTMDefaultValues.biomeSize.name(), this.biomeSize);
        WriteModSettings(PTMDefaultValues.landSize.name(), this.landSize);
        WriteModSettings(PTMDefaultValues.riversEnabled.name(), this.riversEnabled);
        WriteModSettings(PTMDefaultValues.minMoisture.name(), this.minMoisture);
        WriteModSettings(PTMDefaultValues.maxMoisture.name(), this.maxMoisture);
        WriteModSettings(PTMDefaultValues.minTemperature.name(), this.minTemperature);
        WriteModSettings(PTMDefaultValues.maxTemperature.name(), this.maxTemperature);
        WriteModSettings(PTMDefaultValues.snowThreshold.name(), this.snowThreshold);
        WriteModSettings(PTMDefaultValues.iceThreshold.name(), this.iceThreshold);

        WriteModTitleSettings("Swamp Biome Variables");
        WriteModSettings(PTMDefaultValues.muddySwamps.name(), this.muddySwamps);
        WriteModSettings(PTMDefaultValues.claySwamps.name(), this.claySwamps);
        WriteModSettings(PTMDefaultValues.swampSize.name(), this.swampSize);

        WriteModTitleSettings("Desert Biome Variables");
        WriteModSettings(PTMDefaultValues.waterlessDeserts.name(), this.waterlessDeserts);
        WriteModSettings(PTMDefaultValues.desertDirt.name(), this.desertDirt);
        WriteModSettings(PTMDefaultValues.desertDirtFrequency.name(), this.desertDirtFrequency);

        WriteModTitleSettings("Terrain Generator Variables");
        WriteModSettings(PTMDefaultValues.oldTerrainGenerator.name(), this.oldTerrainGenerator);
        WriteModSettings(PTMDefaultValues.waterLevel.name(), this.waterLevel);
        WriteModSettings(PTMDefaultValues.waterBlock.name(), this.waterBlock);
        WriteModSettings(PTMDefaultValues.maxAverageHeight.name(), this.maxAverageHeight);
        WriteModSettings(PTMDefaultValues.maxAverageDepth.name(), this.maxAverageDepth);
        WriteModSettings(PTMDefaultValues.fractureHorizontal.name(), this.fractureHorizontal);
        WriteModSettings(PTMDefaultValues.fractureVertical.name(), this.fractureVertical);
        WriteModSettings(PTMDefaultValues.volatility1.name(), this.volatility1);
        WriteModSettings(PTMDefaultValues.volatility2.name(), this.volatility2);
        WriteModSettings(PTMDefaultValues.volatilityWeight1.name(), this.volatilityWeight1);
        WriteModSettings(PTMDefaultValues.volatilityWeight2.name(), this.volatilityWeight2);
        WriteModSettings(PTMDefaultValues.disableBedrock.name(), this.disableBedrock);
        WriteModSettings(PTMDefaultValues.ceilingBedrock.name(), this.ceilingBedrock);
        WriteModSettings(PTMDefaultValues.flatBedrock.name(), this.flatBedrock);
        WriteModSettings(PTMDefaultValues.bedrockobsidian.name(), this.bedrockObsidian);
        WriteModSettings(PTMDefaultValues.disableNotchHeightControl.name(), this.disableNotchHeightControl);
        WriteHeightSettings();

        WriteModTitleSettings("Map objects");
        WriteModSettings(PTMDefaultValues.StrongholdsEnabled.name(), this.StrongholdsEnabled);
        WriteModSettings(PTMDefaultValues.VillagesEnabled.name(), this.VillagesEnabled);
        WriteModSettings(PTMDefaultValues.MineshaftsEnabled.name(), this.MineshaftsEnabled);

        WriteModTitleSettings("Replace Variables");
        WriteModSettings(PTMDefaultValues.removeSurfaceStone.name(), this.removeSurfaceStone);

        WriteModReplaceSettings();


        this.WriteModTitleSettings("BOB Objects Variables");
        this.WriteModSettings(PTMDefaultValues.customObjects.name(), this.customObjects);
        this.WriteModSettings(PTMDefaultValues.objectSpawnRatio.name(), Integer.valueOf(this.objectSpawnRatio).intValue());
        this.WriteModSettings(PTMDefaultValues.denyObjectsUnderFill.name(), this.denyObjectsUnderFill);
        this.WriteModSettings(PTMDefaultValues.customTreeMinTime.name(), Integer.valueOf(this.customTreeMinTime).intValue());
        this.WriteModSettings(PTMDefaultValues.customTreeMaxTime.name(), Integer.valueOf(this.customTreeMaxTime).intValue());

        this.WriteModTitleSettings("Underground Lake Variables");
        this.WriteModSettings(PTMDefaultValues.undergroundLakes.name(), this.undergroundLakes);
        this.WriteModSettings(PTMDefaultValues.undergroundLakesInAir.name(), this.undergroundLakesInAir);
        this.WriteModSettings(PTMDefaultValues.undergroundLakeFrequency.name(), this.undergroundLakeFrequency);
        this.WriteModSettings(PTMDefaultValues.undergroundLakeRarity.name(), this.undergroundLakeRarity);
        this.WriteModSettings(PTMDefaultValues.undergroundLakeMinSize.name(), this.undergroundLakeMinSize);
        this.WriteModSettings(PTMDefaultValues.undergroundLakeMaxSize.name(), this.undergroundLakeMaxSize);
        this.WriteModSettings(PTMDefaultValues.undergroundLakeMinAltitude.name(), this.undergroundLakeMinAltitude);
        this.WriteModSettings(PTMDefaultValues.undergroundLakeMaxAltitude.name(), this.undergroundLakeMaxAltitude);

        WriteModTitleSettings("Cave Variables");
        WriteModSettings(PTMDefaultValues.caveRarity.name(), this.caveRarity);
        WriteModSettings(PTMDefaultValues.caveFrequency.name(), this.caveFrequency);
        WriteModSettings(PTMDefaultValues.caveMinAltitude.name(), this.caveMinAltitude);
        WriteModSettings(PTMDefaultValues.caveMaxAltitude.name(), this.caveMaxAltitude);
        WriteModSettings(PTMDefaultValues.individualCaveRarity.name(), this.individualCaveRarity);
        WriteModSettings(PTMDefaultValues.caveSystemFrequency.name(), this.caveSystemFrequency);
        WriteModSettings(PTMDefaultValues.caveSystemPocketChance.name(), this.caveSystemPocketChance);
        WriteModSettings(PTMDefaultValues.caveSystemPocketMinSize.name(), this.caveSystemPocketMinSize);
        WriteModSettings(PTMDefaultValues.caveSystemPocketMaxSize.name(), this.caveSystemPocketMaxSize);
        WriteModSettings(PTMDefaultValues.evenCaveDistribution.name(), this.evenCaveDistribution);


        WriteModTitleSettings("Canyon Variables");
        WriteModSettings(PTMDefaultValues.canyonRarity.name(), this.canyonRarity);
        WriteModSettings(PTMDefaultValues.canyonMinAltitude.name(), this.canyonMinAltitude);
        WriteModSettings(PTMDefaultValues.canyonMaxAltitude.name(), this.canyonMaxAltitude);
        WriteModSettings(PTMDefaultValues.canyonMinLength.name(), this.canyonMinLength);
        WriteModSettings(PTMDefaultValues.canyonMaxLength.name(), this.canyonMaxLength);
        WriteModSettings(PTMDefaultValues.canyonDepth.name(), this.canyonDepth);

    }

    private void WriteModReplaceSettings() throws IOException
    {

        if (this.replaceBlocks.size() == 0)
        {
            this.WriteModSettings("ReplacedBlocks", "None");
            return;
        }
        String output = "";
        Iterator<Entry<Integer, Byte>> i = this.replaceBlocks.entrySet().iterator();
        while (i.hasNext())
        {
            Map.Entry<Integer, Byte> me = i.next();

            output += me.getKey().toString() + "=" + me.getValue().toString();
            if (i.hasNext())
                output += ",";
        }
        this.WriteModSettings("ReplacedBlocks", output);
    }

    private void WriteHeightSettings() throws IOException
    {

        String output = Double.toString(this.heightMatrix[0]);
        for (int i = 1; i < this.heightMatrix.length; i++)
            output = output + "," + Double.toString(this.heightMatrix[i]);

        this.WriteModSettings("CustomHeightControl", output);
    }


    private void RegisterBOBPlugins()
    {
        if (this.customObjects)
        {
            try
            {
                File BOBFolder = new File(SettingsDir, PTMDefaultValues.WorldBOBDirectoryName.stringValue());
                if (!BOBFolder.exists())
                {
                    if (!BOBFolder.mkdir())
                    {
                        System.out.println("BOB Plugin system encountered an error, aborting!");
                        return;
                    }
                }
                String[] BOBFolderArray = BOBFolder.list();
                int i = 0;
                while (i < BOBFolderArray.length)
                {
                    File BOBFile = new File(BOBFolder, BOBFolderArray[i]);
                    if ((BOBFile.getName().endsWith(".bo2")) || (BOBFile.getName().endsWith(".BO2")))
                    {
                        CustomObject WorkingCustomObject = new CustomObject(BOBFile);
                        if (WorkingCustomObject.IsValid)
                        {

                            if (!WorkingCustomObject.groupId.equals(""))
                            {
                                if (WorkingCustomObject.branch)
                                {
                                    if (BranchGroups.containsKey(WorkingCustomObject.groupId))
                                        BranchGroups.get(WorkingCustomObject.groupId).add(WorkingCustomObject);
                                    else
                                    {
                                        ArrayList<CustomObject> groupList = new ArrayList<CustomObject>();
                                        groupList.add(WorkingCustomObject);
                                        BranchGroups.put(WorkingCustomObject.groupId, groupList);
                                    }

                                } else
                                {
                                    if (ObjectGroups.containsKey(WorkingCustomObject.groupId))
                                        ObjectGroups.get(WorkingCustomObject.groupId).add(WorkingCustomObject);
                                    else
                                    {
                                        ArrayList<CustomObject> groupList = new ArrayList<CustomObject>();
                                        groupList.add(WorkingCustomObject);
                                        ObjectGroups.put(WorkingCustomObject.groupId, groupList);
                                    }
                                }

                            }

                            this.Objects.add(WorkingCustomObject);

                            System.out.println("BOB Plugin Registered: " + BOBFile.getName());

                        }
                    }
                    i++;
                }
            } catch (Exception e)
            {
                System.out.println("BOB Plugin system encountered an error, aborting!");
            }

            for (CustomObject Object : this.Objects)
            {
                if (Object.tree)
                    this.HasCustomTrees = true;
            }
        }
    }

    public double getFractureHorizontal()
    {
        return this.fractureHorizontal < 0.0D ? 1.0D / (Math.abs(this.fractureHorizontal) + 1.0D) : this.fractureHorizontal + 1.0D;
    }

    public double getFractureVertical()
    {
        return this.fractureVertical < 0.0D ? 1.0D / (Math.abs(this.fractureVertical) + 1.0D) : this.fractureVertical + 1.0D;
    }

    public double getVolatility1()
    {
        return this.volatility1 < 0.0D ? 1.0D / (Math.abs(this.volatility1) + 1.0D) : this.volatility1 + 1.0D;
    }

    public double getVolatility2()
    {
        return this.volatility2 < 0.0D ? 1.0D / (Math.abs(this.volatility2) + 1.0D) : this.volatility2 + 1.0D;
    }

    public double getVolatilityWeight1()
    {
        return (this.volatilityWeight1 - 0.5D) * 24.0D;
    }

    public double getVolatilityWeight2()
    {
        return (0.5D - this.volatilityWeight2) * 24.0D;
    }

    public boolean createAdminium(int y)
    {
        return (!this.disableBedrock) && ((!this.flatBedrock) || (y == 0));
    }

    public byte getAdminium()
    {
        return (byte) (this.bedrockObsidian ? Block.OBSIDIAN.id : Block.BEDROCK.id);
    }

    public enum GenMode
    {
        Normal,
        TerrainTest,
        BiomeTest,
        NotGenerate
    }

}