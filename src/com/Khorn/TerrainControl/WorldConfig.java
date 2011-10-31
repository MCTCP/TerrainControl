package com.Khorn.TerrainControl;

import com.Khorn.TerrainControl.CustomObjects.CustomObject;
import com.Khorn.TerrainControl.Generator.ChunkProviderTC;
import com.Khorn.TerrainControl.Generator.ObjectSpawner;
import com.Khorn.TerrainControl.Util.ConfigFile;
import net.minecraft.server.BiomeBase;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class WorldConfig extends ConfigFile
{

    public ArrayList<String> CustomBiomes = new ArrayList<String>();

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
    public int bedrockBlock;


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
    public TCPlugin plugin;
    public ChunkProviderTC ChunkProvider;
    public ObjectSpawner objectSpawner;

    public boolean isInit = false;

    public boolean isDeprecated = false;
    public WorldConfig newSettings = null;

    public String WorldName;
    public GenMode Mode;


    public BiomeConfig[] biomeConfigs;
    public boolean BiomeConfigsHaveReplacement = false;

    public static final int DefaultBiomesCount = 10;


    public int ChunkMaxY = 128;


    public WorldConfig(File settingsDir, TCPlugin plug, String worldName)
    {
        this.SettingsDir = settingsDir;
        this.WorldName = worldName;

        File settingsFile = new File(this.SettingsDir, TCDefaultValues.WorldSettingsName.stringValue());

        this.ReadSettingsFile(settingsFile);
        this.ReadConfigSettings();

        this.CorrectSettings();

        this.WriteSettingsFile(settingsFile);


        File BiomeFolder = new File(SettingsDir, TCDefaultValues.WorldBiomeConfigDirectoryName.stringValue());
        if (!BiomeFolder.exists())
        {
            if (!BiomeFolder.mkdir())
            {
                System.out.println("TerrainControl: error create biome configs directory, working with defaults");
                return;
            }
        }
        this.biomeConfigs = new BiomeConfig[DefaultBiomesCount + this.CustomBiomes.size()];

        int i = 0;
        while (i < DefaultBiomesCount)
        {

            BiomeConfig config = new BiomeConfig(BiomeFolder, BiomeBase.a[i], this);
            this.biomeConfigs[i] = config;
            if (!this.BiomeConfigsHaveReplacement)
                this.BiomeConfigsHaveReplacement = config.replaceBlocks.size() > 0;
            i++;
        }
        for (String biomeName : this.CustomBiomes)
        {
            BiomeConfig config = new BiomeConfig(BiomeFolder, biomeName, i, this);
            this.biomeConfigs[i] = config;
            if (!this.BiomeConfigsHaveReplacement)
                this.BiomeConfigsHaveReplacement = config.replaceBlocks.size() > 0;
            i++;
        }

        this.RegisterBOBPlugins();
        this.plugin = plug;
    }

    protected void CorrectSettings()
    {
        this.biomeSize = CheckValue(this.biomeSize, 1, 15);
        this.landSize = CheckValue(this.landSize, 0, 10);

        this.oldBiomeSize = (this.oldBiomeSize <= 0.0D ? 1.5D : this.oldBiomeSize);

        this.minMoisture = (this.minMoisture < 0.0F ? 0.0F : this.minMoisture > 1.0F ? 1.0F : this.minMoisture);
        this.minTemperature = (this.minTemperature < 0.0F ? 0.0F : this.minTemperature > 1.0F ? 1.0F : this.minTemperature);
        this.maxMoisture = (this.maxMoisture > 1.0F ? 1.0F : this.maxMoisture < this.minMoisture ? this.minMoisture : this.maxMoisture);
        this.maxTemperature = (this.maxTemperature > 1.0F ? 1.0F : this.maxTemperature < this.minTemperature ? this.minTemperature : this.maxTemperature);


        this.snowThreshold = (this.snowThreshold < 0.0D ? 0.0D : this.snowThreshold > 1.0D ? 1.0D : this.snowThreshold);
        this.iceThreshold = (this.iceThreshold < -1.0D ? -1.0D : this.iceThreshold > 1.0D ? 1.0D : this.iceThreshold);

        this.caveRarity = (this.caveRarity < 0 ? 0 : this.caveRarity > 100 ? 100 : this.caveRarity);
        this.caveFrequency = (this.caveFrequency < 0 ? 0 : this.caveFrequency);
        this.caveMinAltitude = (this.caveMinAltitude < 0 ? 0 : this.caveMinAltitude > TCDefaultValues.yLimit.intValue() - 1 ? TCDefaultValues.yLimit.intValue() - 1 : this.caveMinAltitude);
        this.caveMaxAltitude = (this.caveMaxAltitude > TCDefaultValues.yLimit.intValue() ? TCDefaultValues.yLimit.intValue() : this.caveMaxAltitude <= this.caveMinAltitude ? this.caveMinAltitude + 1 : this.caveMaxAltitude);
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


        this.waterLevel = (this.waterLevel < 0 ? 0 : this.waterLevel > TCDefaultValues.yLimit.intValue() - 1 ? TCDefaultValues.yLimit.intValue() - 1 : this.waterLevel);

        this.undergroundLakeRarity = (this.undergroundLakeRarity < 0 ? 0 : this.undergroundLakeRarity > 100 ? 100 : this.undergroundLakeRarity);
        this.undergroundLakeFrequency = (this.undergroundLakeFrequency < 0 ? 0 : this.undergroundLakeFrequency);
        this.undergroundLakeMinSize = (this.undergroundLakeMinSize < 25 ? 25 : this.undergroundLakeMinSize);
        this.undergroundLakeMaxSize = (this.undergroundLakeMaxSize <= this.undergroundLakeMinSize ? this.undergroundLakeMinSize + 1 : this.undergroundLakeMaxSize);
        this.undergroundLakeMinAltitude = (this.undergroundLakeMinAltitude < 0 ? 0 : this.undergroundLakeMinAltitude > TCDefaultValues.yLimit.intValue() - 1 ? TCDefaultValues.yLimit.intValue() - 1 : this.undergroundLakeMinAltitude);
        this.undergroundLakeMaxAltitude = (this.undergroundLakeMaxAltitude > TCDefaultValues.yLimit.intValue() ? TCDefaultValues.yLimit.intValue() : this.undergroundLakeMaxAltitude <= this.undergroundLakeMinAltitude ? this.undergroundLakeMinAltitude + 1 : this.undergroundLakeMaxAltitude);

        this.customTreeMinTime = (this.customTreeMinTime < 1 ? 1 : this.customTreeMinTime);
        this.customTreeMaxTime = ((this.customTreeMaxTime - this.customTreeMinTime) < 1 ? (this.customTreeMinTime + 1) : this.customTreeMaxTime);

        if (this.oldBiomeGenerator && !this.oldTerrainGenerator)
        {
            System.out.println("TerrainControl: Old biome generator works only with old terrain generator!");
            this.oldBiomeGenerator = false;

        }


    }


    protected void ReadConfigSettings()
    {
        try
        {
            this.Mode = GenMode.valueOf(ReadModSettings(TCDefaultValues.Mode.name(), TCDefaultValues.Mode.stringValue()));
        } catch (IllegalArgumentException e)
        {
            this.Mode = GenMode.Normal;
        }

        this.oldBiomeGenerator = ReadModSettings(TCDefaultValues.oldBiomeGenerator.name(), TCDefaultValues.oldBiomeGenerator.booleanValue());
        this.oldBiomeSize = ReadModSettings(TCDefaultValues.oldBiomeSize.name(), TCDefaultValues.oldBiomeSize.doubleValue());
        this.biomeSize = ReadModSettings(TCDefaultValues.biomeSize.name(), TCDefaultValues.biomeSize.intValue());
        this.landSize = ReadModSettings(TCDefaultValues.landSize.name(), TCDefaultValues.landSize.intValue());
        this.riversEnabled = ReadModSettings(TCDefaultValues.riversEnabled.name(), TCDefaultValues.riversEnabled.booleanValue());
        this.minMoisture = ReadModSettings(TCDefaultValues.minMoisture.name(), TCDefaultValues.minMoisture.floatValue());
        this.maxMoisture = ReadModSettings(TCDefaultValues.maxMoisture.name(), TCDefaultValues.maxMoisture.floatValue());
        this.minTemperature = ReadModSettings(TCDefaultValues.minTemperature.name(), TCDefaultValues.minTemperature.floatValue());
        this.maxTemperature = ReadModSettings(TCDefaultValues.maxTemperature.name(), TCDefaultValues.maxTemperature.floatValue());
        this.snowThreshold = ReadModSettings(TCDefaultValues.snowThreshold.name(), TCDefaultValues.snowThreshold.doubleValue());
        this.iceThreshold = ReadModSettings(TCDefaultValues.iceThreshold.name(), TCDefaultValues.iceThreshold.doubleValue());

        this.muddySwamps = ReadModSettings(TCDefaultValues.muddySwamps.name(), TCDefaultValues.muddySwamps.booleanValue());
        this.claySwamps = ReadModSettings(TCDefaultValues.claySwamps.name(), TCDefaultValues.claySwamps.booleanValue());
        this.swampSize = ReadModSettings(TCDefaultValues.swampSize.name(), TCDefaultValues.swampSize.intValue());

        this.waterlessDeserts = ReadModSettings(TCDefaultValues.waterlessDeserts.name(), TCDefaultValues.waterlessDeserts.booleanValue());
        this.desertDirt = ReadModSettings(TCDefaultValues.desertDirt.name(), TCDefaultValues.desertDirt.booleanValue());
        this.desertDirtFrequency = ReadModSettings(TCDefaultValues.desertDirtFrequency.name(), TCDefaultValues.desertDirtFrequency.intValue());


        this.StrongholdsEnabled = ReadModSettings(TCDefaultValues.StrongholdsEnabled.name(), TCDefaultValues.StrongholdsEnabled.booleanValue());
        this.VillagesEnabled = ReadModSettings(TCDefaultValues.VillagesEnabled.name(), TCDefaultValues.VillagesEnabled.booleanValue());
        this.MineshaftsEnabled = ReadModSettings(TCDefaultValues.MineshaftsEnabled.name(), TCDefaultValues.MineshaftsEnabled.booleanValue());


        this.caveRarity = ReadModSettings(TCDefaultValues.caveRarity.name(), TCDefaultValues.caveRarity.intValue());
        this.caveFrequency = ReadModSettings(TCDefaultValues.caveFrequency.name(), TCDefaultValues.caveFrequency.intValue());
        this.caveMinAltitude = ReadModSettings(TCDefaultValues.caveMinAltitude.name(), TCDefaultValues.caveMinAltitude.intValue());
        this.caveMaxAltitude = ReadModSettings(TCDefaultValues.caveMaxAltitude.name(), TCDefaultValues.caveMaxAltitude.intValue());
        this.individualCaveRarity = ReadModSettings(TCDefaultValues.individualCaveRarity.name(), TCDefaultValues.individualCaveRarity.intValue());
        this.caveSystemFrequency = ReadModSettings(TCDefaultValues.caveSystemFrequency.name(), TCDefaultValues.caveSystemFrequency.intValue());
        this.caveSystemPocketChance = ReadModSettings(TCDefaultValues.caveSystemPocketChance.name(), TCDefaultValues.caveSystemPocketChance.intValue());
        this.caveSystemPocketMinSize = ReadModSettings(TCDefaultValues.caveSystemPocketMinSize.name(), TCDefaultValues.caveSystemPocketMinSize.intValue());
        this.caveSystemPocketMaxSize = ReadModSettings(TCDefaultValues.caveSystemPocketMaxSize.name(), TCDefaultValues.caveSystemPocketMaxSize.intValue());
        this.evenCaveDistribution = ReadModSettings(TCDefaultValues.evenCaveDistribution.name(), TCDefaultValues.evenCaveDistribution.booleanValue());

        this.canyonRarity = ReadModSettings(TCDefaultValues.canyonRarity.name(), TCDefaultValues.canyonRarity.intValue());
        this.canyonMinAltitude = ReadModSettings(TCDefaultValues.canyonMinAltitude.name(), TCDefaultValues.canyonMinAltitude.intValue());
        this.canyonMaxAltitude = ReadModSettings(TCDefaultValues.canyonMaxAltitude.name(), TCDefaultValues.canyonMaxAltitude.intValue());
        this.canyonMinLength = ReadModSettings(TCDefaultValues.canyonMinLength.name(), TCDefaultValues.canyonMinLength.intValue());
        this.canyonMinLength = ReadModSettings(TCDefaultValues.canyonMinLength.name(), TCDefaultValues.canyonMinLength.intValue());
        this.canyonDepth = ReadModSettings(TCDefaultValues.canyonDepth.name(), TCDefaultValues.canyonDepth.doubleValue());


        this.waterLevel = ReadModSettings(TCDefaultValues.waterLevel.name(), TCDefaultValues.waterLevel.intValue());
        this.waterBlock = ReadModSettings(TCDefaultValues.waterBlock.name(), TCDefaultValues.waterBlock.intValue());
        this.maxAverageHeight = ReadModSettings(TCDefaultValues.maxAverageHeight.name(), TCDefaultValues.maxAverageHeight.doubleValue());
        this.maxAverageDepth = ReadModSettings(TCDefaultValues.maxAverageDepth.name(), TCDefaultValues.maxAverageDepth.doubleValue());
        this.fractureHorizontal = ReadModSettings(TCDefaultValues.fractureHorizontal.name(), TCDefaultValues.fractureHorizontal.doubleValue());
        this.fractureVertical = ReadModSettings(TCDefaultValues.fractureVertical.name(), TCDefaultValues.fractureVertical.doubleValue());
        this.volatility1 = ReadModSettings(TCDefaultValues.volatility1.name(), TCDefaultValues.volatility1.doubleValue());
        this.volatility2 = ReadModSettings(TCDefaultValues.volatility2.name(), TCDefaultValues.volatility2.doubleValue());
        this.volatilityWeight1 = ReadModSettings(TCDefaultValues.volatilityWeight1.name(), TCDefaultValues.volatilityWeight1.doubleValue());
        this.volatilityWeight2 = ReadModSettings(TCDefaultValues.volatilityWeight2.name(), TCDefaultValues.volatilityWeight2.doubleValue());
        this.disableNotchHeightControl = ReadModSettings(TCDefaultValues.disableNotchHeightControl.name(), TCDefaultValues.disableNotchHeightControl.booleanValue());

        this.disableBedrock = ReadModSettings(TCDefaultValues.disableBedrock.name(), TCDefaultValues.disableBedrock.booleanValue());
        this.ceilingBedrock = ReadModSettings(TCDefaultValues.ceilingBedrock.name(), TCDefaultValues.ceilingBedrock.booleanValue());
        this.flatBedrock = ReadModSettings(TCDefaultValues.flatBedrock.name(), TCDefaultValues.flatBedrock.booleanValue());
        this.bedrockBlock = ReadModSettings(TCDefaultValues.BedrockobBlock.name(), TCDefaultValues.BedrockobBlock.intValue());

        ReadHeightSettings();

        this.oldTerrainGenerator = ReadModSettings(TCDefaultValues.oldTerrainGenerator.name(), TCDefaultValues.oldTerrainGenerator.booleanValue());
        this.removeSurfaceStone = ReadModSettings(TCDefaultValues.removeSurfaceStone.name(), TCDefaultValues.removeSurfaceStone.booleanValue());


        this.customObjects = this.ReadModSettings(TCDefaultValues.customObjects.name(), TCDefaultValues.customObjects.booleanValue());
        this.objectSpawnRatio = this.ReadModSettings(TCDefaultValues.objectSpawnRatio.name(), TCDefaultValues.objectSpawnRatio.intValue());
        this.denyObjectsUnderFill = this.ReadModSettings(TCDefaultValues.denyObjectsUnderFill.name(), TCDefaultValues.denyObjectsUnderFill.booleanValue());
        this.customTreeMinTime = this.ReadModSettings(TCDefaultValues.customTreeMinTime.name(), TCDefaultValues.customTreeMinTime.intValue());
        this.customTreeMaxTime = this.ReadModSettings(TCDefaultValues.customTreeMaxTime.name(), TCDefaultValues.customTreeMaxTime.intValue());


        this.undergroundLakes = this.ReadModSettings(TCDefaultValues.undergroundLakes.name(), TCDefaultValues.undergroundLakes.booleanValue());
        this.undergroundLakesInAir = this.ReadModSettings(TCDefaultValues.undergroundLakesInAir.name(), TCDefaultValues.undergroundLakesInAir.booleanValue());
        this.undergroundLakeFrequency = this.ReadModSettings(TCDefaultValues.undergroundLakeFrequency.name(), TCDefaultValues.undergroundLakeFrequency.intValue());
        this.undergroundLakeRarity = this.ReadModSettings(TCDefaultValues.undergroundLakeRarity.name(), TCDefaultValues.undergroundLakeRarity.intValue());
        this.undergroundLakeMinSize = this.ReadModSettings(TCDefaultValues.undergroundLakeMinSize.name(), TCDefaultValues.undergroundLakeMinSize.intValue());
        this.undergroundLakeMaxSize = this.ReadModSettings(TCDefaultValues.undergroundLakeMaxSize.name(), TCDefaultValues.undergroundLakeMaxSize.intValue());
        this.undergroundLakeMinAltitude = this.ReadModSettings(TCDefaultValues.undergroundLakeMinAltitude.name(), TCDefaultValues.undergroundLakeMinAltitude.intValue());
        this.undergroundLakeMaxAltitude = this.ReadModSettings(TCDefaultValues.undergroundLakeMaxAltitude.name(), TCDefaultValues.undergroundLakeMaxAltitude.intValue());



        this.ReadCustomBiomes();


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



    private void ReadCustomBiomes()
    {
        if (this.SettingsCache.containsKey("CustomBiomes"))
        {
            if (this.SettingsCache.get("CustomBiomes").trim().equals(""))
                return;
            String[] keys = this.SettingsCache.get("CustomBiomes").split(",");

            for (String key : keys)
            {
                boolean isUnique = true;
                for (int i = 0; i < DefaultBiomesCount; i++)
                    if (BiomeBase.a[i].l.equals(key))
                        isUnique = false;

                if (isUnique && !this.CustomBiomes.contains(key))
                    this.CustomBiomes.add(key);
            }
        }
    }


    protected void WriteConfigSettings() throws IOException
    {
        WriteModTitleSettings("Possible modes : Normal, TerrainTest, NotGenerate, OnlyBiome");
        WriteModSettings(TCDefaultValues.Mode.name(), this.Mode.name());

        WriteModTitleSettings("Old Biome Generator Variables");
        WriteModTitleSettings("This generator works only with old terrain generator!");
        WriteModSettings(TCDefaultValues.oldBiomeGenerator.name(), this.oldBiomeGenerator);
        WriteModSettings(TCDefaultValues.oldBiomeSize.name(), this.oldBiomeSize);
        WriteModSettings(TCDefaultValues.minMoisture.name(), this.minMoisture);
        WriteModSettings(TCDefaultValues.maxMoisture.name(), this.maxMoisture);
        WriteModSettings(TCDefaultValues.minTemperature.name(), this.minTemperature);
        WriteModSettings(TCDefaultValues.maxTemperature.name(), this.maxTemperature);
        WriteModSettings(TCDefaultValues.snowThreshold.name(), this.snowThreshold);
        WriteModSettings(TCDefaultValues.iceThreshold.name(), this.iceThreshold);

        WriteModTitleSettings("Biome Generator Variables");
        WriteModSettings(TCDefaultValues.biomeSize.name(), this.biomeSize);
        WriteModSettings(TCDefaultValues.landSize.name(), this.landSize);
        WriteModSettings(TCDefaultValues.riversEnabled.name(), this.riversEnabled);

        this.WriteCustomBiomesSettings();

        WriteModTitleSettings("Swamp Biome Variables");
        WriteModSettings(TCDefaultValues.muddySwamps.name(), this.muddySwamps);
        WriteModSettings(TCDefaultValues.claySwamps.name(), this.claySwamps);
        WriteModSettings(TCDefaultValues.swampSize.name(), this.swampSize);

        WriteModTitleSettings("Desert Biome Variables");
        WriteModSettings(TCDefaultValues.waterlessDeserts.name(), this.waterlessDeserts);
        WriteModSettings(TCDefaultValues.desertDirt.name(), this.desertDirt);
        WriteModSettings(TCDefaultValues.desertDirtFrequency.name(), this.desertDirtFrequency);

        WriteModTitleSettings("Terrain Generator Variables");
        WriteModSettings(TCDefaultValues.oldTerrainGenerator.name(), this.oldTerrainGenerator);
        WriteModSettings(TCDefaultValues.waterLevel.name(), this.waterLevel);
        WriteModSettings(TCDefaultValues.waterBlock.name(), this.waterBlock);
        WriteModSettings(TCDefaultValues.removeSurfaceStone.name(), this.removeSurfaceStone);
        WriteModSettings(TCDefaultValues.maxAverageHeight.name(), this.maxAverageHeight);
        WriteModSettings(TCDefaultValues.maxAverageDepth.name(), this.maxAverageDepth);
        WriteModSettings(TCDefaultValues.fractureHorizontal.name(), this.fractureHorizontal);
        WriteModSettings(TCDefaultValues.fractureVertical.name(), this.fractureVertical);
        WriteModSettings(TCDefaultValues.volatility1.name(), this.volatility1);
        WriteModSettings(TCDefaultValues.volatility2.name(), this.volatility2);
        WriteModSettings(TCDefaultValues.volatilityWeight1.name(), this.volatilityWeight1);
        WriteModSettings(TCDefaultValues.volatilityWeight2.name(), this.volatilityWeight2);
        WriteModSettings(TCDefaultValues.disableBedrock.name(), this.disableBedrock);
        WriteModSettings(TCDefaultValues.ceilingBedrock.name(), this.ceilingBedrock);
        WriteModSettings(TCDefaultValues.flatBedrock.name(), this.flatBedrock);
        WriteModSettings(TCDefaultValues.BedrockobBlock.name(), this.bedrockBlock);
        WriteModSettings(TCDefaultValues.disableNotchHeightControl.name(), this.disableNotchHeightControl);
        WriteHeightSettings();

        WriteModTitleSettings("Map objects");
        WriteModSettings(TCDefaultValues.StrongholdsEnabled.name(), this.StrongholdsEnabled);
        WriteModSettings(TCDefaultValues.VillagesEnabled.name(), this.VillagesEnabled);
        WriteModSettings(TCDefaultValues.MineshaftsEnabled.name(), this.MineshaftsEnabled);


        this.WriteModTitleSettings("BOB Objects Variables");
        this.WriteModSettings(TCDefaultValues.customObjects.name(), this.customObjects);
        this.WriteModSettings(TCDefaultValues.objectSpawnRatio.name(), Integer.valueOf(this.objectSpawnRatio).intValue());
        this.WriteModSettings(TCDefaultValues.denyObjectsUnderFill.name(), this.denyObjectsUnderFill);
        this.WriteModSettings(TCDefaultValues.customTreeMinTime.name(), Integer.valueOf(this.customTreeMinTime).intValue());
        this.WriteModSettings(TCDefaultValues.customTreeMaxTime.name(), Integer.valueOf(this.customTreeMaxTime).intValue());

        this.WriteModTitleSettings("Underground Lake Variables");
        this.WriteModSettings(TCDefaultValues.undergroundLakes.name(), this.undergroundLakes);
        this.WriteModSettings(TCDefaultValues.undergroundLakesInAir.name(), this.undergroundLakesInAir);
        this.WriteModSettings(TCDefaultValues.undergroundLakeFrequency.name(), this.undergroundLakeFrequency);
        this.WriteModSettings(TCDefaultValues.undergroundLakeRarity.name(), this.undergroundLakeRarity);
        this.WriteModSettings(TCDefaultValues.undergroundLakeMinSize.name(), this.undergroundLakeMinSize);
        this.WriteModSettings(TCDefaultValues.undergroundLakeMaxSize.name(), this.undergroundLakeMaxSize);
        this.WriteModSettings(TCDefaultValues.undergroundLakeMinAltitude.name(), this.undergroundLakeMinAltitude);
        this.WriteModSettings(TCDefaultValues.undergroundLakeMaxAltitude.name(), this.undergroundLakeMaxAltitude);

        WriteModTitleSettings("Cave Variables");
        WriteModSettings(TCDefaultValues.caveRarity.name(), this.caveRarity);
        WriteModSettings(TCDefaultValues.caveFrequency.name(), this.caveFrequency);
        WriteModSettings(TCDefaultValues.caveMinAltitude.name(), this.caveMinAltitude);
        WriteModSettings(TCDefaultValues.caveMaxAltitude.name(), this.caveMaxAltitude);
        WriteModSettings(TCDefaultValues.individualCaveRarity.name(), this.individualCaveRarity);
        WriteModSettings(TCDefaultValues.caveSystemFrequency.name(), this.caveSystemFrequency);
        WriteModSettings(TCDefaultValues.caveSystemPocketChance.name(), this.caveSystemPocketChance);
        WriteModSettings(TCDefaultValues.caveSystemPocketMinSize.name(), this.caveSystemPocketMinSize);
        WriteModSettings(TCDefaultValues.caveSystemPocketMaxSize.name(), this.caveSystemPocketMaxSize);
        WriteModSettings(TCDefaultValues.evenCaveDistribution.name(), this.evenCaveDistribution);


        WriteModTitleSettings("Canyon Variables");
        WriteModSettings(TCDefaultValues.canyonRarity.name(), this.canyonRarity);
        WriteModSettings(TCDefaultValues.canyonMinAltitude.name(), this.canyonMinAltitude);
        WriteModSettings(TCDefaultValues.canyonMaxAltitude.name(), this.canyonMaxAltitude);
        WriteModSettings(TCDefaultValues.canyonMinLength.name(), this.canyonMinLength);
        WriteModSettings(TCDefaultValues.canyonMaxLength.name(), this.canyonMaxLength);
        WriteModSettings(TCDefaultValues.canyonDepth.name(), this.canyonDepth);

    }


    private void WriteHeightSettings() throws IOException
    {

        String output = Double.toString(this.heightMatrix[0]);
        for (int i = 1; i < this.heightMatrix.length; i++)
            output = output + "," + Double.toString(this.heightMatrix[i]);

        this.WriteModSettings("CustomHeightControl", output);
    }

    private void WriteCustomBiomesSettings() throws IOException
    {

        if (this.CustomBiomes.size() == 0)
        {
            this.WriteModSettings("CustomBiomes", "");
            return;
        }
        String output = this.CustomBiomes.get(0);
        for (int i = 1; i < this.CustomBiomes.size(); i++)
            output = output + "," + this.CustomBiomes.get(i);

        this.WriteModSettings("CustomBiomes", output);
    }


    private void RegisterBOBPlugins()
    {
        if (this.customObjects)
        {
            try
            {
                File BOBFolder = new File(SettingsDir, TCDefaultValues.WorldBOBDirectoryName.stringValue());
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


    public enum GenMode
    {
        Normal,
        TerrainTest,
        OnlyBiome,
        NotGenerate,

    }

}