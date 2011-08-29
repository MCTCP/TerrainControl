package com.Khorn.PTMBukkit;

import com.Khorn.PTMBukkit.CustomObjects.CustomObject;
import com.Khorn.PTMBukkit.Generator.ChunkProviderPTM;
import com.Khorn.PTMBukkit.Generator.ObjectSpawner;
import net.minecraft.server.Block;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class Settings
{
    private BufferedWriter SettingsWriter;
    private HashMap<String, String> ReadedSettings = new HashMap<String, String>();

    public HashMap<Integer, Byte> replaceBlocks = new HashMap<Integer, Byte>();
    public byte[] ReplaceBlocksMatrix = new byte[256];


    public ArrayList<CustomObject> Objects = new ArrayList<CustomObject>();
    public HashMap<String, ArrayList<CustomObject>> ObjectGroups = new HashMap<String, ArrayList<CustomObject>>();
    public HashMap<String, ArrayList<CustomObject>> BranchGroups = new HashMap<String, ArrayList<CustomObject>>();
    public boolean HasCustomTrees = false;

    // public BiomeBase currentBiome;
    // --Commented out by Inspection (17.07.11 1:49):String seedValue;

    public double biomeSize;
    public double minMoisture;
    public double maxMoisture;
    public double minTemperature;
    public double maxTemperature;
    public double snowThreshold;
    public double iceThreshold;
    public boolean muddySwamps;
    public boolean claySwamps;
    public int swampSize;
    public boolean waterlessDeserts;
    public boolean desertDirt;
    public int desertDirtFrequency;
    public boolean removeSurfaceDirtFromDesert;

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
    private boolean bedrockobsidian;

    public boolean removeSurfaceStone;
    public boolean disableNotchHeightControl;
    public double[] heightMatrix = new double[17];


    public boolean evenWaterSourceDistribution;
    public boolean evenLavaSourceDistribution;

    // Materials
    public int flowerDepositRarity;
    public int flowerDepositFrequency;
    public int flowerDepositMinAltitude;
    public int flowerDepositMaxAltitude;
    public int roseDepositRarity;
    public int roseDepositFrequency;
    public int roseDepositMinAltitude;
    public int roseDepositMaxAltitude;
    public int brownMushroomDepositRarity;
    public int brownMushroomDepositFrequency;
    public int brownMushroomDepositMinAltitude;
    public int brownMushroomDepositMaxAltitude;
    public int redMushroomDepositRarity;
    public int redMushroomDepositFrequency;
    public int redMushroomDepositMinAltitude;
    public int redMushroomDepositMaxAltitude;
    public int reedDepositRarity;
    public int reedDepositFrequency;
    public int reedDepositMinAltitude;
    public int reedDepositMaxAltitude;
    public int pumpkinDepositRarity;
    public int pumpkinDepositFrequency;
    public int pumpkinDepositMinAltitude;
    public int pumpkinDepositMaxAltitude;
    public int waterSourceDepositRarity;
    public int waterSourceDepositFrequency;
    public int waterSourceDepositMinAltitude;
    public int waterSourceDepositMaxAltitude;
    public int lavaSourceDepositRarity;
    public int lavaSourceDepositFrequency;
    public int lavaSourceDepositMinAltitude;
    public int lavaSourceDepositMaxAltitude;
    public int dirtDepositRarity1;
    public int dirtDepositFrequency1;
    public int dirtDepositSize1;
    public int dirtDepositMinAltitude1;
    public int dirtDepositMaxAltitude1;
    public int dirtDepositRarity2;
    public int dirtDepositFrequency2;
    public int dirtDepositSize2;
    public int dirtDepositMinAltitude2;
    public int dirtDepositMaxAltitude2;
    public int dirtDepositRarity3;
    public int dirtDepositFrequency3;
    public int dirtDepositSize3;
    public int dirtDepositMinAltitude3;
    public int dirtDepositMaxAltitude3;
    public int dirtDepositRarity4;
    public int dirtDepositFrequency4;
    public int dirtDepositSize4;
    public int dirtDepositMinAltitude4;
    public int dirtDepositMaxAltitude4;
    public int gravelDepositRarity1;
    public int gravelDepositFrequency1;
    public int gravelDepositSize1;
    public int gravelDepositMinAltitude1;
    public int gravelDepositMaxAltitude1;
    public int gravelDepositRarity2;
    public int gravelDepositFrequency2;
    public int gravelDepositSize2;
    public int gravelDepositMinAltitude2;
    public int gravelDepositMaxAltitude2;
    public int gravelDepositRarity3;
    public int gravelDepositFrequency3;
    public int gravelDepositSize3;
    public int gravelDepositMinAltitude3;
    public int gravelDepositMaxAltitude3;
    public int gravelDepositRarity4;
    public int gravelDepositFrequency4;
    public int gravelDepositSize4;
    public int gravelDepositMinAltitude4;
    public int gravelDepositMaxAltitude4;
    public int clayDepositRarity1;
    public int clayDepositFrequency1;
    public int clayDepositSize1;
    public int clayDepositMinAltitude1;
    public int clayDepositMaxAltitude1;
    public int clayDepositRarity2;
    public int clayDepositFrequency2;
    public int clayDepositSize2;
    public int clayDepositMinAltitude2;
    public int clayDepositMaxAltitude2;
    public int clayDepositRarity3;
    public int clayDepositFrequency3;
    public int clayDepositSize3;
    public int clayDepositMinAltitude3;
    public int clayDepositMaxAltitude3;
    public int clayDepositRarity4;
    public int clayDepositFrequency4;
    public int clayDepositSize4;
    public int clayDepositMinAltitude4;
    public int clayDepositMaxAltitude4;
    public int coalDepositRarity1;
    public int coalDepositFrequency1;
    public int coalDepositSize1;
    public int coalDepositMinAltitude1;
    public int coalDepositMaxAltitude1;
    public int coalDepositRarity2;
    public int coalDepositFrequency2;
    public int coalDepositSize2;
    public int coalDepositMinAltitude2;
    public int coalDepositMaxAltitude2;
    public int coalDepositRarity3;
    public int coalDepositFrequency3;
    public int coalDepositSize3;
    public int coalDepositMinAltitude3;
    public int coalDepositMaxAltitude3;
    public int coalDepositRarity4;
    public int coalDepositFrequency4;
    public int coalDepositSize4;
    public int coalDepositMinAltitude4;
    public int coalDepositMaxAltitude4;
    public int ironDepositRarity1;
    public int ironDepositFrequency1;
    public int ironDepositSize1;
    public int ironDepositMinAltitude1;
    public int ironDepositMaxAltitude1;
    public int ironDepositRarity2;
    public int ironDepositFrequency2;
    public int ironDepositSize2;
    public int ironDepositMinAltitude2;
    public int ironDepositMaxAltitude2;
    public int ironDepositRarity3;
    public int ironDepositFrequency3;
    public int ironDepositSize3;
    public int ironDepositMinAltitude3;
    public int ironDepositMaxAltitude3;
    public int ironDepositRarity4;
    public int ironDepositFrequency4;
    public int ironDepositSize4;
    public int ironDepositMinAltitude4;
    public int ironDepositMaxAltitude4;
    public int goldDepositRarity1;
    public int goldDepositFrequency1;
    public int goldDepositSize1;
    public int goldDepositMinAltitude1;
    public int goldDepositMaxAltitude1;
    public int goldDepositRarity2;
    public int goldDepositFrequency2;
    public int goldDepositSize2;
    public int goldDepositMinAltitude2;
    public int goldDepositMaxAltitude2;
    public int goldDepositRarity3;
    public int goldDepositFrequency3;
    public int goldDepositSize3;
    public int goldDepositMinAltitude3;
    public int goldDepositMaxAltitude3;
    public int goldDepositRarity4;
    public int goldDepositFrequency4;
    public int goldDepositSize4;
    public int goldDepositMinAltitude4;
    public int goldDepositMaxAltitude4;
    public int redstoneDepositRarity1;
    public int redstoneDepositFrequency1;
    public int redstoneDepositSize1;
    public int redstoneDepositMinAltitude1;
    public int redstoneDepositMaxAltitude1;
    public int redstoneDepositRarity2;
    public int redstoneDepositFrequency2;
    public int redstoneDepositSize2;
    public int redstoneDepositMinAltitude2;
    public int redstoneDepositMaxAltitude2;
    public int redstoneDepositRarity3;
    public int redstoneDepositFrequency3;
    public int redstoneDepositSize3;
    public int redstoneDepositMinAltitude3;
    public int redstoneDepositMaxAltitude3;
    public int redstoneDepositRarity4;
    public int redstoneDepositFrequency4;
    public int redstoneDepositSize4;
    public int redstoneDepositMinAltitude4;
    public int redstoneDepositMaxAltitude4;
    public int diamondDepositRarity1;
    public int diamondDepositFrequency1;
    public int diamondDepositSize1;
    public int diamondDepositMinAltitude1;
    public int diamondDepositMaxAltitude1;
    public int diamondDepositRarity2;
    public int diamondDepositFrequency2;
    public int diamondDepositSize2;
    public int diamondDepositMinAltitude2;
    public int diamondDepositMaxAltitude2;
    public int diamondDepositRarity3;
    public int diamondDepositFrequency3;
    public int diamondDepositSize3;
    public int diamondDepositMinAltitude3;
    public int diamondDepositMaxAltitude3;
    public int diamondDepositRarity4;
    public int diamondDepositFrequency4;
    public int diamondDepositSize4;
    public int diamondDepositMinAltitude4;
    public int diamondDepositMaxAltitude4;
    public int lapislazuliDepositRarity1;
    public int lapislazuliDepositFrequency1;
    public int lapislazuliDepositSize1;
    public int lapislazuliDepositMinAltitude1;
    public int lapislazuliDepositMaxAltitude1;
    public int lapislazuliDepositRarity2;
    public int lapislazuliDepositFrequency2;
    public int lapislazuliDepositSize2;
    public int lapislazuliDepositMinAltitude2;
    public int lapislazuliDepositMaxAltitude2;
    public int lapislazuliDepositRarity3;
    public int lapislazuliDepositFrequency3;
    public int lapislazuliDepositSize3;
    public int lapislazuliDepositMinAltitude3;
    public int lapislazuliDepositMaxAltitude3;
    public int lapislazuliDepositRarity4;
    public int lapislazuliDepositFrequency4;
    public int lapislazuliDepositSize4;
    public int lapislazuliDepositMinAltitude4;
    public int lapislazuliDepositMaxAltitude4;

    // End Materials

    public boolean disableNotchPonds;

    public int dungeonRarity;
    public int dungeonFrequency;
    public int dungeonMinAltitude;
    public int dungeonMaxAltitude;

    public boolean customObjects;
    public int objectSpawnRatio;
    public boolean denyObjectsUnderFill;
    public int customTreeMinTime;
    public int customTreeMaxTime;


    public boolean notchBiomeTrees;
    public int globalTreeDensity;
    public int rainforestTreeDensity;
    public int swamplandTreeDensity;
    public int seasonalforestTreeDensity;
    public int forestTreeDensity;
    public int savannaTreeDensity;
    public int shrublandTreeDensity;
    public int taigaTreeDensity;
    public int desertTreeDensity;
    public int plainsTreeDensity;
    public int iceDesertTreeDensity;
    public int tundraTreeDensity;
    public int globalCactusDensity;
    public int desertCactusDensity;

    public int cactusDepositRarity;
    public int cactusDepositMinAltitude;
    public int cactusDepositMaxAltitude;

    public boolean undergroundLakes;
    public boolean undergroundLakesInAir;
    public int undergroundLakeFrequency;
    public int undergroundLakeRarity;
    public int undergroundLakeMinSize;
    public int undergroundLakeMaxSize;
    public int undergroundLakeMinAltitude;
    public int undergroundLakeMaxAltitude;

    public int lavaLevelMin;
    public int lavaLevelMax;


    private File SettingsDir;
    public PTMPlugin plugin;
    public ChunkProviderPTM ChunkProvider;
    public ObjectSpawner objectSpawner;

    public boolean isInit = false;

    public boolean isDeprecated = false;
    public Settings newSettings = null;


    public Settings(File settingsDir, PTMPlugin plug)
    {
        this.SettingsDir = settingsDir;

        ReadSettings();
        CorrectSettings();
        WriteSettings();
        BuildReplaceMatrix();
        this.RegisterBOBPlugins();
        this.plugin = plug;
    }

    public Settings()
    {}

    public void CreateDefaultSettings(File settingsDir)
    {
        this.ReadWorldSettings();
        this.CorrectSettings();
        this.WriteSettings(settingsDir);

    }

    void CorrectSettings()
    {
        this.biomeSize = (this.biomeSize <= 0.0D ? 4.9E-324D : this.biomeSize);
        this.minMoisture = (this.minMoisture < 0.0D ? 0.0D : this.minMoisture > 1.0D ? 1.0D : this.minMoisture);
        this.minTemperature = (this.minTemperature < 0.0D ? 0.0D : this.minTemperature > 1.0D ? 1.0D : this.minTemperature);
        this.maxMoisture = (this.maxMoisture > 1.0D ? 1.0D : this.maxMoisture < this.minMoisture ? this.minMoisture : this.maxMoisture);
        this.maxTemperature = (this.maxTemperature > 1.0D ? 1.0D : this.maxTemperature < this.minTemperature ? this.minTemperature : this.maxTemperature);
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


        this.waterLevel = (this.waterLevel < 0 ? 0 : this.waterLevel > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.waterLevel);

        this.flowerDepositRarity = (this.flowerDepositRarity < 0 ? 0 : this.flowerDepositRarity > 100 ? 100 : this.flowerDepositRarity);
        this.flowerDepositFrequency = (this.flowerDepositFrequency < 0 ? 0 : this.flowerDepositFrequency);
        this.flowerDepositMinAltitude = (this.flowerDepositMinAltitude < 0 ? 0 : this.flowerDepositMinAltitude > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.flowerDepositMinAltitude);
        this.flowerDepositMaxAltitude = (this.flowerDepositMaxAltitude > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.flowerDepositMaxAltitude <= this.flowerDepositMinAltitude ? this.flowerDepositMinAltitude + 1 : this.flowerDepositMaxAltitude);
        this.roseDepositRarity = (this.roseDepositRarity < 0 ? 0 : this.roseDepositRarity > 100 ? 100 : this.roseDepositRarity);
        this.roseDepositFrequency = (this.roseDepositFrequency < 0 ? 0 : this.roseDepositFrequency);
        this.roseDepositMinAltitude = (this.roseDepositMinAltitude < 0 ? 0 : this.roseDepositMinAltitude > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.roseDepositMinAltitude);
        this.roseDepositMaxAltitude = (this.roseDepositMaxAltitude > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.roseDepositMaxAltitude <= this.roseDepositMinAltitude ? this.roseDepositMinAltitude + 1 : this.roseDepositMaxAltitude);
        this.brownMushroomDepositRarity = (this.brownMushroomDepositRarity < 0 ? 0 : this.brownMushroomDepositRarity > 100 ? 100 : this.brownMushroomDepositRarity);
        this.brownMushroomDepositFrequency = (this.brownMushroomDepositFrequency < 0 ? 0 : this.brownMushroomDepositFrequency);
        this.brownMushroomDepositMinAltitude = (this.brownMushroomDepositMinAltitude < 0 ? 0 : this.brownMushroomDepositMinAltitude > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.brownMushroomDepositMinAltitude);
        this.brownMushroomDepositMaxAltitude = (this.brownMushroomDepositMaxAltitude > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.brownMushroomDepositMaxAltitude <= this.brownMushroomDepositMinAltitude ? this.brownMushroomDepositMinAltitude + 1 : this.brownMushroomDepositMaxAltitude);
        this.redMushroomDepositRarity = (this.redMushroomDepositRarity < 0 ? 0 : this.redMushroomDepositRarity > 100 ? 100 : this.redMushroomDepositRarity);
        this.redMushroomDepositFrequency = (this.redMushroomDepositFrequency < 0 ? 0 : this.redMushroomDepositFrequency);
        this.redMushroomDepositMinAltitude = (this.redMushroomDepositMinAltitude < 0 ? 0 : this.redMushroomDepositMinAltitude > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.redMushroomDepositMinAltitude);
        this.redMushroomDepositMaxAltitude = (this.redMushroomDepositMaxAltitude > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.redMushroomDepositMaxAltitude <= this.redMushroomDepositMinAltitude ? this.redMushroomDepositMinAltitude + 1 : this.redMushroomDepositMaxAltitude);
        this.reedDepositRarity = (this.reedDepositRarity < 0 ? 0 : this.reedDepositRarity > 100 ? 100 : this.reedDepositRarity);
        this.reedDepositFrequency = (this.reedDepositFrequency < 0 ? 0 : this.reedDepositFrequency);
        this.reedDepositMinAltitude = (this.reedDepositMinAltitude < 0 ? 0 : this.reedDepositMinAltitude > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.reedDepositMinAltitude);
        this.reedDepositMaxAltitude = (this.reedDepositMaxAltitude > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.reedDepositMaxAltitude <= this.reedDepositMinAltitude ? this.reedDepositMinAltitude + 1 : this.reedDepositMaxAltitude);
        this.pumpkinDepositRarity = (this.pumpkinDepositRarity < 0 ? 0 : this.pumpkinDepositRarity > 100 ? 100 : this.pumpkinDepositRarity);
        this.pumpkinDepositFrequency = (this.pumpkinDepositFrequency < 0 ? 0 : this.pumpkinDepositFrequency);
        this.pumpkinDepositMinAltitude = (this.pumpkinDepositMinAltitude < 0 ? 0 : this.pumpkinDepositMinAltitude > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.pumpkinDepositMinAltitude);
        this.pumpkinDepositMaxAltitude = (this.pumpkinDepositMaxAltitude > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.pumpkinDepositMaxAltitude <= this.pumpkinDepositMinAltitude ? this.pumpkinDepositMinAltitude + 1 : this.pumpkinDepositMaxAltitude);

        this.waterSourceDepositRarity = (this.waterSourceDepositRarity < 0 ? 0 : this.waterSourceDepositRarity > 100 ? 100 : this.waterSourceDepositRarity);
        this.waterSourceDepositFrequency = (this.waterSourceDepositFrequency < 0 ? 0 : this.waterSourceDepositFrequency);
        this.waterSourceDepositMinAltitude = (this.waterSourceDepositMinAltitude < 0 ? 0 : this.waterSourceDepositMinAltitude > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.waterSourceDepositMinAltitude);
        this.waterSourceDepositMaxAltitude = (this.waterSourceDepositMaxAltitude > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.waterSourceDepositMaxAltitude <= this.waterSourceDepositMinAltitude ? this.waterSourceDepositMinAltitude + 1 : this.waterSourceDepositMaxAltitude);
        this.lavaSourceDepositRarity = (this.lavaSourceDepositRarity < 0 ? 0 : this.lavaSourceDepositRarity > 100 ? 100 : this.lavaSourceDepositRarity);
        this.lavaSourceDepositFrequency = (this.lavaSourceDepositFrequency < 0 ? 0 : this.lavaSourceDepositFrequency);
        this.lavaSourceDepositMinAltitude = (this.lavaSourceDepositMinAltitude < 0 ? 0 : this.lavaSourceDepositMinAltitude > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.lavaSourceDepositMinAltitude);
        this.lavaSourceDepositMaxAltitude = (this.lavaSourceDepositMaxAltitude > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.lavaSourceDepositMaxAltitude <= this.lavaSourceDepositMinAltitude ? this.lavaSourceDepositMinAltitude + 1 : this.lavaSourceDepositMaxAltitude);

        this.dirtDepositRarity1 = (this.dirtDepositRarity1 < 0 ? 0 : this.dirtDepositRarity1 > 100 ? 100 : this.dirtDepositRarity1);
        this.dirtDepositFrequency1 = (this.dirtDepositFrequency1 < 0 ? 0 : this.dirtDepositFrequency1);
        this.dirtDepositSize1 = (this.dirtDepositSize1 < 0 ? 0 : this.dirtDepositSize1);
        this.dirtDepositMinAltitude1 = (this.dirtDepositMinAltitude1 < 0 ? 0 : this.dirtDepositMinAltitude1 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.dirtDepositMinAltitude1);
        this.dirtDepositMaxAltitude1 = (this.dirtDepositMaxAltitude1 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.dirtDepositMaxAltitude1 <= this.dirtDepositMinAltitude1 ? this.dirtDepositMinAltitude1 + 1 : this.dirtDepositMaxAltitude1);
        this.dirtDepositRarity2 = (this.dirtDepositRarity2 < 0 ? 0 : this.dirtDepositRarity2 > 100 ? 100 : this.dirtDepositRarity2);
        this.dirtDepositFrequency2 = (this.dirtDepositFrequency2 < 0 ? 0 : this.dirtDepositFrequency2);
        this.dirtDepositSize2 = (this.dirtDepositSize2 < 0 ? 0 : this.dirtDepositSize2);
        this.dirtDepositMinAltitude2 = (this.dirtDepositMinAltitude2 < 0 ? 0 : this.dirtDepositMinAltitude2 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.dirtDepositMinAltitude2);
        this.dirtDepositMaxAltitude2 = (this.dirtDepositMaxAltitude2 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.dirtDepositMaxAltitude2 <= this.dirtDepositMinAltitude2 ? this.dirtDepositMinAltitude2 + 1 : this.dirtDepositMaxAltitude2);
        this.dirtDepositRarity3 = (this.dirtDepositRarity3 < 0 ? 0 : this.dirtDepositRarity3 > 100 ? 100 : this.dirtDepositRarity3);
        this.dirtDepositFrequency3 = (this.dirtDepositFrequency3 < 0 ? 0 : this.dirtDepositFrequency3);
        this.dirtDepositSize3 = (this.dirtDepositSize3 < 0 ? 0 : this.dirtDepositSize3);
        this.dirtDepositMinAltitude3 = (this.dirtDepositMinAltitude3 < 0 ? 0 : this.dirtDepositMinAltitude3 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.dirtDepositMinAltitude3);
        this.dirtDepositMaxAltitude3 = (this.dirtDepositMaxAltitude3 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.dirtDepositMaxAltitude3 <= this.dirtDepositMinAltitude3 ? this.dirtDepositMinAltitude3 + 1 : this.dirtDepositMaxAltitude3);
        this.dirtDepositRarity4 = (this.dirtDepositRarity4 < 0 ? 0 : this.dirtDepositRarity4 > 100 ? 100 : this.dirtDepositRarity4);
        this.dirtDepositFrequency4 = (this.dirtDepositFrequency4 < 0 ? 0 : this.dirtDepositFrequency4);
        this.dirtDepositSize4 = (this.dirtDepositSize4 < 0 ? 0 : this.dirtDepositSize4);
        this.dirtDepositMinAltitude4 = (this.dirtDepositMinAltitude4 < 0 ? 0 : this.dirtDepositMinAltitude4 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.dirtDepositMinAltitude4);
        this.dirtDepositMaxAltitude4 = (this.dirtDepositMaxAltitude4 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.dirtDepositMaxAltitude4 <= this.dirtDepositMinAltitude4 ? this.dirtDepositMinAltitude4 + 1 : this.dirtDepositMaxAltitude4);
        this.gravelDepositRarity1 = (this.gravelDepositRarity1 < 0 ? 0 : this.gravelDepositRarity1 > 100 ? 100 : this.gravelDepositRarity1);
        this.gravelDepositFrequency1 = (this.gravelDepositFrequency1 < 0 ? 0 : this.gravelDepositFrequency1);
        this.gravelDepositSize1 = (this.gravelDepositSize1 < 0 ? 0 : this.gravelDepositSize1);
        this.gravelDepositMinAltitude1 = (this.gravelDepositMinAltitude1 < 0 ? 0 : this.gravelDepositMinAltitude1 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.gravelDepositMinAltitude1);
        this.gravelDepositMaxAltitude1 = (this.gravelDepositMaxAltitude1 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.gravelDepositMaxAltitude1 <= this.gravelDepositMinAltitude1 ? this.gravelDepositMinAltitude1 + 1 : this.gravelDepositMaxAltitude1);
        this.gravelDepositRarity2 = (this.gravelDepositRarity2 < 0 ? 0 : this.gravelDepositRarity2 > 100 ? 100 : this.gravelDepositRarity2);
        this.gravelDepositFrequency2 = (this.gravelDepositFrequency2 < 0 ? 0 : this.gravelDepositFrequency2);
        this.gravelDepositSize2 = (this.gravelDepositSize2 < 0 ? 0 : this.gravelDepositSize2);
        this.gravelDepositMinAltitude2 = (this.gravelDepositMinAltitude2 < 0 ? 0 : this.gravelDepositMinAltitude2 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.gravelDepositMinAltitude2);
        this.gravelDepositMaxAltitude2 = (this.gravelDepositMaxAltitude2 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.gravelDepositMaxAltitude2 <= this.gravelDepositMinAltitude2 ? this.gravelDepositMinAltitude2 + 1 : this.gravelDepositMaxAltitude2);
        this.gravelDepositRarity3 = (this.gravelDepositRarity3 < 0 ? 0 : this.gravelDepositRarity3 > 100 ? 100 : this.gravelDepositRarity3);
        this.gravelDepositFrequency3 = (this.gravelDepositFrequency3 < 0 ? 0 : this.gravelDepositFrequency3);
        this.gravelDepositSize3 = (this.gravelDepositSize3 < 0 ? 0 : this.gravelDepositSize3);
        this.gravelDepositMinAltitude3 = (this.gravelDepositMinAltitude3 < 0 ? 0 : this.gravelDepositMinAltitude3 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.gravelDepositMinAltitude3);
        this.gravelDepositMaxAltitude3 = (this.gravelDepositMaxAltitude3 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.gravelDepositMaxAltitude3 <= this.gravelDepositMinAltitude3 ? this.gravelDepositMinAltitude3 + 1 : this.gravelDepositMaxAltitude3);
        this.gravelDepositRarity4 = (this.gravelDepositRarity4 < 0 ? 0 : this.gravelDepositRarity4 > 100 ? 100 : this.gravelDepositRarity4);
        this.gravelDepositFrequency4 = (this.gravelDepositFrequency4 < 0 ? 0 : this.gravelDepositFrequency4);
        this.gravelDepositSize4 = (this.gravelDepositSize4 < 0 ? 0 : this.gravelDepositSize4);
        this.gravelDepositMinAltitude4 = (this.gravelDepositMinAltitude4 < 0 ? 0 : this.gravelDepositMinAltitude4 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.gravelDepositMinAltitude4);
        this.gravelDepositMaxAltitude4 = (this.gravelDepositMaxAltitude4 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.gravelDepositMaxAltitude4 <= this.gravelDepositMinAltitude4 ? this.gravelDepositMinAltitude4 + 1 : this.gravelDepositMaxAltitude4);
        this.clayDepositRarity1 = (this.clayDepositRarity1 < 0 ? 0 : this.clayDepositRarity1 > 100 ? 100 : this.clayDepositRarity1);
        this.clayDepositFrequency1 = (this.clayDepositFrequency1 < 0 ? 0 : this.clayDepositFrequency1);
        this.clayDepositSize1 = (this.clayDepositSize1 < 0 ? 0 : this.clayDepositSize1);
        this.clayDepositMinAltitude1 = (this.clayDepositMinAltitude1 < 0 ? 0 : this.clayDepositMinAltitude1 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.clayDepositMinAltitude1);
        this.clayDepositMaxAltitude1 = (this.clayDepositMaxAltitude1 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.clayDepositMaxAltitude1 <= this.clayDepositMinAltitude1 ? this.clayDepositMinAltitude1 + 1 : this.clayDepositMaxAltitude1);
        this.clayDepositRarity2 = (this.clayDepositRarity2 < 0 ? 0 : this.clayDepositRarity2 > 100 ? 100 : this.clayDepositRarity2);
        this.clayDepositFrequency2 = (this.clayDepositFrequency2 < 0 ? 0 : this.clayDepositFrequency2);
        this.clayDepositSize2 = (this.clayDepositSize2 < 0 ? 0 : this.clayDepositSize2);
        this.clayDepositMinAltitude2 = (this.clayDepositMinAltitude2 < 0 ? 0 : this.clayDepositMinAltitude2 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.clayDepositMinAltitude2);
        this.clayDepositMaxAltitude2 = (this.clayDepositMaxAltitude2 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.clayDepositMaxAltitude2 <= this.clayDepositMinAltitude2 ? this.clayDepositMinAltitude2 + 1 : this.clayDepositMaxAltitude2);
        this.clayDepositRarity3 = (this.clayDepositRarity3 < 0 ? 0 : this.clayDepositRarity3 > 100 ? 100 : this.clayDepositRarity3);
        this.clayDepositFrequency3 = (this.clayDepositFrequency3 < 0 ? 0 : this.clayDepositFrequency3);
        this.clayDepositSize3 = (this.clayDepositSize3 < 0 ? 0 : this.clayDepositSize3);
        this.clayDepositMinAltitude3 = (this.clayDepositMinAltitude3 < 0 ? 0 : this.clayDepositMinAltitude3 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.clayDepositMinAltitude3);
        this.clayDepositMaxAltitude3 = (this.clayDepositMaxAltitude3 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.clayDepositMaxAltitude3 <= this.clayDepositMinAltitude3 ? this.clayDepositMinAltitude3 + 1 : this.clayDepositMaxAltitude3);
        this.clayDepositRarity4 = (this.clayDepositRarity4 < 0 ? 0 : this.clayDepositRarity4 > 100 ? 100 : this.clayDepositRarity4);
        this.clayDepositFrequency4 = (this.clayDepositFrequency4 < 0 ? 0 : this.clayDepositFrequency4);
        this.clayDepositSize4 = (this.clayDepositSize4 < 0 ? 0 : this.clayDepositSize4);
        this.clayDepositMinAltitude4 = (this.clayDepositMinAltitude4 < 0 ? 0 : this.clayDepositMinAltitude4 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.clayDepositMinAltitude4);
        this.clayDepositMaxAltitude4 = (this.clayDepositMaxAltitude4 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.clayDepositMaxAltitude4 <= this.clayDepositMinAltitude4 ? this.clayDepositMinAltitude4 + 1 : this.clayDepositMaxAltitude4);
        this.coalDepositRarity1 = (this.coalDepositRarity1 < 0 ? 0 : this.coalDepositRarity1 > 100 ? 100 : this.coalDepositRarity1);
        this.coalDepositFrequency1 = (this.coalDepositFrequency1 < 0 ? 0 : this.coalDepositFrequency1);
        this.coalDepositSize1 = (this.coalDepositSize1 < 0 ? 0 : this.coalDepositSize1);
        this.coalDepositMinAltitude1 = (this.coalDepositMinAltitude1 < 0 ? 0 : this.coalDepositMinAltitude1 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.coalDepositMinAltitude1);
        this.coalDepositMaxAltitude1 = (this.coalDepositMaxAltitude1 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.coalDepositMaxAltitude1 <= this.coalDepositMinAltitude1 ? this.coalDepositMinAltitude1 + 1 : this.coalDepositMaxAltitude1);
        this.coalDepositRarity2 = (this.coalDepositRarity2 < 0 ? 0 : this.coalDepositRarity2 > 100 ? 100 : this.coalDepositRarity2);
        this.coalDepositFrequency2 = (this.coalDepositFrequency2 < 0 ? 0 : this.coalDepositFrequency2);
        this.coalDepositSize2 = (this.coalDepositSize2 < 0 ? 0 : this.coalDepositSize2);
        this.coalDepositMinAltitude2 = (this.coalDepositMinAltitude2 < 0 ? 0 : this.coalDepositMinAltitude2 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.coalDepositMinAltitude2);
        this.coalDepositMaxAltitude2 = (this.coalDepositMaxAltitude2 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.coalDepositMaxAltitude2 <= this.coalDepositMinAltitude2 ? this.coalDepositMinAltitude2 + 1 : this.coalDepositMaxAltitude2);
        this.coalDepositRarity3 = (this.coalDepositRarity3 < 0 ? 0 : this.coalDepositRarity3 > 100 ? 100 : this.coalDepositRarity3);
        this.coalDepositFrequency3 = (this.coalDepositFrequency3 < 0 ? 0 : this.coalDepositFrequency3);
        this.coalDepositSize3 = (this.coalDepositSize3 < 0 ? 0 : this.coalDepositSize3);
        this.coalDepositMinAltitude3 = (this.coalDepositMinAltitude3 < 0 ? 0 : this.coalDepositMinAltitude3 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.coalDepositMinAltitude3);
        this.coalDepositMaxAltitude3 = (this.coalDepositMaxAltitude3 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.coalDepositMaxAltitude3 <= this.coalDepositMinAltitude3 ? this.coalDepositMinAltitude3 + 1 : this.coalDepositMaxAltitude3);
        this.coalDepositRarity4 = (this.coalDepositRarity4 < 0 ? 0 : this.coalDepositRarity4 > 100 ? 100 : this.coalDepositRarity4);
        this.coalDepositFrequency4 = (this.coalDepositFrequency4 < 0 ? 0 : this.coalDepositFrequency4);
        this.coalDepositSize4 = (this.coalDepositSize4 < 0 ? 0 : this.coalDepositSize4);
        this.coalDepositMinAltitude4 = (this.coalDepositMinAltitude4 < 0 ? 0 : this.coalDepositMinAltitude4 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.coalDepositMinAltitude4);
        this.coalDepositMaxAltitude4 = (this.coalDepositMaxAltitude4 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.coalDepositMaxAltitude4 <= this.coalDepositMinAltitude4 ? this.coalDepositMinAltitude4 + 1 : this.coalDepositMaxAltitude4);
        this.ironDepositRarity1 = (this.ironDepositRarity1 < 0 ? 0 : this.ironDepositRarity1 > 100 ? 100 : this.ironDepositRarity1);
        this.ironDepositFrequency1 = (this.ironDepositFrequency1 < 0 ? 0 : this.ironDepositFrequency1);
        this.ironDepositSize1 = (this.ironDepositSize1 < 0 ? 0 : this.ironDepositSize1);
        this.ironDepositMinAltitude1 = (this.ironDepositMinAltitude1 < 0 ? 0 : this.ironDepositMinAltitude1 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.ironDepositMinAltitude1);
        this.ironDepositMaxAltitude1 = (this.ironDepositMaxAltitude1 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.ironDepositMaxAltitude1 <= this.ironDepositMinAltitude1 ? this.ironDepositMinAltitude1 + 1 : this.ironDepositMaxAltitude1);
        this.ironDepositRarity2 = (this.ironDepositRarity2 < 0 ? 0 : this.ironDepositRarity2 > 100 ? 100 : this.ironDepositRarity2);
        this.ironDepositFrequency2 = (this.ironDepositFrequency2 < 0 ? 0 : this.ironDepositFrequency2);
        this.ironDepositSize2 = (this.ironDepositSize2 < 0 ? 0 : this.ironDepositSize2);
        this.ironDepositMinAltitude2 = (this.ironDepositMinAltitude2 < 0 ? 0 : this.ironDepositMinAltitude2 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.ironDepositMinAltitude2);
        this.ironDepositMaxAltitude2 = (this.ironDepositMaxAltitude2 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.ironDepositMaxAltitude2 <= this.ironDepositMinAltitude2 ? this.ironDepositMinAltitude2 + 1 : this.ironDepositMaxAltitude2);
        this.ironDepositRarity3 = (this.ironDepositRarity3 < 0 ? 0 : this.ironDepositRarity3 > 100 ? 100 : this.ironDepositRarity3);
        this.ironDepositFrequency3 = (this.ironDepositFrequency3 < 0 ? 0 : this.ironDepositFrequency3);
        this.ironDepositSize3 = (this.ironDepositSize3 < 0 ? 0 : this.ironDepositSize3);
        this.ironDepositMinAltitude3 = (this.ironDepositMinAltitude3 < 0 ? 0 : this.ironDepositMinAltitude3 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.ironDepositMinAltitude3);
        this.ironDepositMaxAltitude3 = (this.ironDepositMaxAltitude3 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.ironDepositMaxAltitude3 <= this.ironDepositMinAltitude3 ? this.ironDepositMinAltitude3 + 1 : this.ironDepositMaxAltitude3);
        this.ironDepositRarity4 = (this.ironDepositRarity4 < 0 ? 0 : this.ironDepositRarity4 > 100 ? 100 : this.ironDepositRarity4);
        this.ironDepositFrequency4 = (this.ironDepositFrequency4 < 0 ? 0 : this.ironDepositFrequency4);
        this.ironDepositSize4 = (this.ironDepositSize4 < 0 ? 0 : this.ironDepositSize4);
        this.ironDepositMinAltitude4 = (this.ironDepositMinAltitude4 < 0 ? 0 : this.ironDepositMinAltitude4 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.ironDepositMinAltitude4);
        this.ironDepositMaxAltitude4 = (this.ironDepositMaxAltitude4 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.ironDepositMaxAltitude4 <= this.ironDepositMinAltitude4 ? this.ironDepositMinAltitude4 + 1 : this.ironDepositMaxAltitude4);
        this.goldDepositRarity1 = (this.goldDepositRarity1 < 0 ? 0 : this.goldDepositRarity1 > 100 ? 100 : this.goldDepositRarity1);
        this.goldDepositFrequency1 = (this.goldDepositFrequency1 < 0 ? 0 : this.goldDepositFrequency1);
        this.goldDepositSize1 = (this.goldDepositSize1 < 0 ? 0 : this.goldDepositSize1);
        this.goldDepositMinAltitude1 = (this.goldDepositMinAltitude1 < 0 ? 0 : this.goldDepositMinAltitude1 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.goldDepositMinAltitude1);
        this.goldDepositMaxAltitude1 = (this.goldDepositMaxAltitude1 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.goldDepositMaxAltitude1 <= this.goldDepositMinAltitude1 ? this.goldDepositMinAltitude1 + 1 : this.goldDepositMaxAltitude1);
        this.goldDepositRarity2 = (this.goldDepositRarity2 < 0 ? 0 : this.goldDepositRarity2 > 100 ? 100 : this.goldDepositRarity2);
        this.goldDepositFrequency2 = (this.goldDepositFrequency2 < 0 ? 0 : this.goldDepositFrequency2);
        this.goldDepositSize2 = (this.goldDepositSize2 < 0 ? 0 : this.goldDepositSize2);
        this.goldDepositMinAltitude2 = (this.goldDepositMinAltitude2 < 0 ? 0 : this.goldDepositMinAltitude2 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.goldDepositMinAltitude2);
        this.goldDepositMaxAltitude2 = (this.goldDepositMaxAltitude2 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.goldDepositMaxAltitude2 <= this.goldDepositMinAltitude2 ? this.goldDepositMinAltitude2 + 1 : this.goldDepositMaxAltitude2);
        this.goldDepositRarity3 = (this.goldDepositRarity3 < 0 ? 0 : this.goldDepositRarity3 > 100 ? 100 : this.goldDepositRarity3);
        this.goldDepositFrequency3 = (this.goldDepositFrequency3 < 0 ? 0 : this.goldDepositFrequency3);
        this.goldDepositSize3 = (this.goldDepositSize3 < 0 ? 0 : this.goldDepositSize3);
        this.goldDepositMinAltitude3 = (this.goldDepositMinAltitude3 < 0 ? 0 : this.goldDepositMinAltitude3 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.goldDepositMinAltitude3);
        this.goldDepositMaxAltitude3 = (this.goldDepositMaxAltitude3 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.goldDepositMaxAltitude3 <= this.goldDepositMinAltitude3 ? this.goldDepositMinAltitude3 + 1 : this.goldDepositMaxAltitude3);
        this.goldDepositRarity4 = (this.goldDepositRarity4 < 0 ? 0 : this.goldDepositRarity4 > 100 ? 100 : this.goldDepositRarity4);
        this.goldDepositFrequency4 = (this.goldDepositFrequency4 < 0 ? 0 : this.goldDepositFrequency4);
        this.goldDepositSize4 = (this.goldDepositSize4 < 0 ? 0 : this.goldDepositSize4);
        this.goldDepositMinAltitude4 = (this.goldDepositMinAltitude4 < 0 ? 0 : this.goldDepositMinAltitude4 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.goldDepositMinAltitude4);
        this.goldDepositMaxAltitude4 = (this.goldDepositMaxAltitude4 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.goldDepositMaxAltitude4 <= this.goldDepositMinAltitude4 ? this.goldDepositMinAltitude4 + 1 : this.goldDepositMaxAltitude4);
        this.redstoneDepositRarity1 = (this.redstoneDepositRarity1 < 0 ? 0 : this.redstoneDepositRarity1 > 100 ? 100 : this.redstoneDepositRarity1);
        this.redstoneDepositFrequency1 = (this.redstoneDepositFrequency1 < 0 ? 0 : this.redstoneDepositFrequency1);
        this.redstoneDepositSize1 = (this.redstoneDepositSize1 < 0 ? 0 : this.redstoneDepositSize1);
        this.redstoneDepositMinAltitude1 = (this.redstoneDepositMinAltitude1 < 0 ? 0 : this.redstoneDepositMinAltitude1 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.redstoneDepositMinAltitude1);
        this.redstoneDepositMaxAltitude1 = (this.redstoneDepositMaxAltitude1 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.redstoneDepositMaxAltitude1 <= this.redstoneDepositMinAltitude1 ? this.redstoneDepositMinAltitude1 + 1 : this.redstoneDepositMaxAltitude1);
        this.redstoneDepositRarity2 = (this.redstoneDepositRarity2 < 0 ? 0 : this.redstoneDepositRarity2 > 100 ? 100 : this.redstoneDepositRarity2);
        this.redstoneDepositFrequency2 = (this.redstoneDepositFrequency2 < 0 ? 0 : this.redstoneDepositFrequency2);
        this.redstoneDepositSize2 = (this.redstoneDepositSize2 < 0 ? 0 : this.redstoneDepositSize2);
        this.redstoneDepositMinAltitude2 = (this.redstoneDepositMinAltitude2 < 0 ? 0 : this.redstoneDepositMinAltitude2 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.redstoneDepositMinAltitude2);
        this.redstoneDepositMaxAltitude2 = (this.redstoneDepositMaxAltitude2 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.redstoneDepositMaxAltitude2 <= this.redstoneDepositMinAltitude2 ? this.redstoneDepositMinAltitude2 + 1 : this.redstoneDepositMaxAltitude2);
        this.redstoneDepositRarity3 = (this.redstoneDepositRarity3 < 0 ? 0 : this.redstoneDepositRarity3 > 100 ? 100 : this.redstoneDepositRarity3);
        this.redstoneDepositFrequency3 = (this.redstoneDepositFrequency3 < 0 ? 0 : this.redstoneDepositFrequency3);
        this.redstoneDepositSize3 = (this.redstoneDepositSize3 < 0 ? 0 : this.redstoneDepositSize3);
        this.redstoneDepositMinAltitude3 = (this.redstoneDepositMinAltitude3 < 0 ? 0 : this.redstoneDepositMinAltitude3 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.redstoneDepositMinAltitude3);
        this.redstoneDepositMaxAltitude3 = (this.redstoneDepositMaxAltitude3 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.redstoneDepositMaxAltitude3 <= this.redstoneDepositMinAltitude3 ? this.redstoneDepositMinAltitude3 + 1 : this.redstoneDepositMaxAltitude3);
        this.redstoneDepositRarity4 = (this.redstoneDepositRarity4 < 0 ? 0 : this.redstoneDepositRarity4 > 100 ? 100 : this.redstoneDepositRarity4);
        this.redstoneDepositFrequency4 = (this.redstoneDepositFrequency4 < 0 ? 0 : this.redstoneDepositFrequency4);
        this.redstoneDepositSize4 = (this.redstoneDepositSize4 < 0 ? 0 : this.redstoneDepositSize4);
        this.redstoneDepositMinAltitude4 = (this.redstoneDepositMinAltitude4 < 0 ? 0 : this.redstoneDepositMinAltitude4 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.redstoneDepositMinAltitude4);
        this.redstoneDepositMaxAltitude4 = (this.redstoneDepositMaxAltitude4 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.redstoneDepositMaxAltitude4 <= this.redstoneDepositMinAltitude4 ? this.redstoneDepositMinAltitude4 + 1 : this.redstoneDepositMaxAltitude4);
        this.diamondDepositRarity1 = (this.diamondDepositRarity1 < 0 ? 0 : this.diamondDepositRarity1 > 100 ? 100 : this.diamondDepositRarity1);
        this.diamondDepositFrequency1 = (this.diamondDepositFrequency1 < 0 ? 0 : this.diamondDepositFrequency1);
        this.diamondDepositSize1 = (this.diamondDepositSize1 < 0 ? 0 : this.diamondDepositSize1);
        this.diamondDepositMinAltitude1 = (this.diamondDepositMinAltitude1 < 0 ? 0 : this.diamondDepositMinAltitude1 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.diamondDepositMinAltitude1);
        this.diamondDepositMaxAltitude1 = (this.diamondDepositMaxAltitude1 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.diamondDepositMaxAltitude1 <= this.diamondDepositMinAltitude1 ? this.diamondDepositMinAltitude1 + 1 : this.diamondDepositMaxAltitude1);
        this.diamondDepositRarity2 = (this.diamondDepositRarity2 < 0 ? 0 : this.diamondDepositRarity2 > 100 ? 100 : this.diamondDepositRarity2);
        this.diamondDepositFrequency2 = (this.diamondDepositFrequency2 < 0 ? 0 : this.diamondDepositFrequency2);
        this.diamondDepositSize2 = (this.diamondDepositSize2 < 0 ? 0 : this.diamondDepositSize2);
        this.diamondDepositMinAltitude2 = (this.diamondDepositMinAltitude2 < 0 ? 0 : this.diamondDepositMinAltitude2 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.diamondDepositMinAltitude2);
        this.diamondDepositMaxAltitude2 = (this.diamondDepositMaxAltitude2 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.diamondDepositMaxAltitude2 <= this.diamondDepositMinAltitude2 ? this.diamondDepositMinAltitude2 + 1 : this.diamondDepositMaxAltitude2);
        this.diamondDepositRarity3 = (this.diamondDepositRarity3 < 0 ? 0 : this.diamondDepositRarity3 > 100 ? 100 : this.diamondDepositRarity3);
        this.diamondDepositFrequency3 = (this.diamondDepositFrequency3 < 0 ? 0 : this.diamondDepositFrequency3);
        this.diamondDepositSize3 = (this.diamondDepositSize3 < 0 ? 0 : this.diamondDepositSize3);
        this.diamondDepositMinAltitude3 = (this.diamondDepositMinAltitude3 < 0 ? 0 : this.diamondDepositMinAltitude3 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.diamondDepositMinAltitude3);
        this.diamondDepositMaxAltitude3 = (this.diamondDepositMaxAltitude3 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.diamondDepositMaxAltitude3 <= this.diamondDepositMinAltitude3 ? this.diamondDepositMinAltitude3 + 1 : this.diamondDepositMaxAltitude3);
        this.diamondDepositRarity4 = (this.diamondDepositRarity4 < 0 ? 0 : this.diamondDepositRarity4 > 100 ? 100 : this.diamondDepositRarity4);
        this.diamondDepositFrequency4 = (this.diamondDepositFrequency4 < 0 ? 0 : this.diamondDepositFrequency4);
        this.diamondDepositSize4 = (this.diamondDepositSize4 < 0 ? 0 : this.diamondDepositSize4);
        this.diamondDepositMinAltitude4 = (this.diamondDepositMinAltitude4 < 0 ? 0 : this.diamondDepositMinAltitude4 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.diamondDepositMinAltitude4);
        this.diamondDepositMaxAltitude4 = (this.diamondDepositMaxAltitude4 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.diamondDepositMaxAltitude4 <= this.diamondDepositMinAltitude4 ? this.diamondDepositMinAltitude4 + 1 : this.diamondDepositMaxAltitude4);
        this.lapislazuliDepositRarity1 = (this.lapislazuliDepositRarity1 < 0 ? 0 : this.lapislazuliDepositRarity1 > 100 ? 100 : this.lapislazuliDepositRarity1);
        this.lapislazuliDepositFrequency1 = (this.lapislazuliDepositFrequency1 < 0 ? 0 : this.lapislazuliDepositFrequency1);
        this.lapislazuliDepositSize1 = (this.lapislazuliDepositSize1 < 0 ? 0 : this.lapislazuliDepositSize1);
        this.lapislazuliDepositMinAltitude1 = (this.lapislazuliDepositMinAltitude1 < 0 ? 0 : this.lapislazuliDepositMinAltitude1 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.lapislazuliDepositMinAltitude1);
        this.lapislazuliDepositMaxAltitude1 = (this.lapislazuliDepositMaxAltitude1 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.lapislazuliDepositMaxAltitude1 <= this.lapislazuliDepositMinAltitude1 ? this.lapislazuliDepositMinAltitude1 + 1 : this.lapislazuliDepositMaxAltitude1);
        this.lapislazuliDepositRarity2 = (this.lapislazuliDepositRarity2 < 0 ? 0 : this.lapislazuliDepositRarity2 > 100 ? 100 : this.lapislazuliDepositRarity2);
        this.lapislazuliDepositFrequency2 = (this.lapislazuliDepositFrequency2 < 0 ? 0 : this.lapislazuliDepositFrequency2);
        this.lapislazuliDepositSize2 = (this.lapislazuliDepositSize2 < 0 ? 0 : this.lapislazuliDepositSize2);
        this.lapislazuliDepositMinAltitude2 = (this.lapislazuliDepositMinAltitude2 < 0 ? 0 : this.lapislazuliDepositMinAltitude2 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.lapislazuliDepositMinAltitude2);
        this.lapislazuliDepositMaxAltitude2 = (this.lapislazuliDepositMaxAltitude2 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.lapislazuliDepositMaxAltitude2 <= this.lapislazuliDepositMinAltitude2 ? this.lapislazuliDepositMinAltitude2 + 1 : this.lapislazuliDepositMaxAltitude2);
        this.lapislazuliDepositRarity3 = (this.lapislazuliDepositRarity3 < 0 ? 0 : this.lapislazuliDepositRarity3 > 100 ? 100 : this.lapislazuliDepositRarity3);
        this.lapislazuliDepositFrequency3 = (this.lapislazuliDepositFrequency3 < 0 ? 0 : this.lapislazuliDepositFrequency3);
        this.lapislazuliDepositSize3 = (this.lapislazuliDepositSize3 < 0 ? 0 : this.lapislazuliDepositSize3);
        this.lapislazuliDepositMinAltitude3 = (this.lapislazuliDepositMinAltitude3 < 0 ? 0 : this.lapislazuliDepositMinAltitude3 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.lapislazuliDepositMinAltitude3);
        this.lapislazuliDepositMaxAltitude3 = (this.lapislazuliDepositMaxAltitude3 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.lapislazuliDepositMaxAltitude3 <= this.lapislazuliDepositMinAltitude3 ? this.lapislazuliDepositMinAltitude3 + 1 : this.lapislazuliDepositMaxAltitude3);
        this.lapislazuliDepositRarity4 = (this.lapislazuliDepositRarity4 < 0 ? 0 : this.lapislazuliDepositRarity4 > 100 ? 100 : this.lapislazuliDepositRarity4);
        this.lapislazuliDepositFrequency4 = (this.lapislazuliDepositFrequency4 < 0 ? 0 : this.lapislazuliDepositFrequency4);
        this.lapislazuliDepositSize4 = (this.lapislazuliDepositSize4 < 0 ? 0 : this.lapislazuliDepositSize4);
        this.lapislazuliDepositMinAltitude4 = (this.lapislazuliDepositMinAltitude4 < 0 ? 0 : this.lapislazuliDepositMinAltitude4 > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.lapislazuliDepositMinAltitude4);
        this.lapislazuliDepositMaxAltitude4 = (this.lapislazuliDepositMaxAltitude4 > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.lapislazuliDepositMaxAltitude4 <= this.lapislazuliDepositMinAltitude4 ? this.lapislazuliDepositMinAltitude4 + 1 : this.lapislazuliDepositMaxAltitude4);

        this.dungeonRarity = (this.dungeonRarity < 0 ? 0 : this.dungeonRarity > 100 ? 100 : this.dungeonRarity);
        this.dungeonFrequency = (this.dungeonFrequency < 0 ? 0 : this.dungeonFrequency);
        this.dungeonMinAltitude = (this.dungeonMinAltitude < 0 ? 0 : this.dungeonMinAltitude > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.dungeonMinAltitude);
        this.dungeonMaxAltitude = (this.dungeonMaxAltitude > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.dungeonMaxAltitude <= this.dungeonMinAltitude ? this.dungeonMinAltitude + 1 : this.dungeonMaxAltitude);


        this.lavaLevelMin = (this.lavaLevelMin < 0 ? 0 : this.lavaLevelMin > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.lavaLevelMin);
        this.lavaLevelMax = (this.lavaLevelMax > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.lavaLevelMax < this.lavaLevelMin ? this.lavaLevelMin : this.lavaLevelMax);

        this.undergroundLakeRarity = (this.undergroundLakeRarity < 0 ? 0 : this.undergroundLakeRarity > 100 ? 100 : this.undergroundLakeRarity);
        this.undergroundLakeFrequency = (this.undergroundLakeFrequency < 0 ? 0 : this.undergroundLakeFrequency);
        this.undergroundLakeMinSize = (this.undergroundLakeMinSize < 25 ? 25 : this.undergroundLakeMinSize);
        this.undergroundLakeMaxSize = (this.undergroundLakeMaxSize <= this.undergroundLakeMinSize ? this.undergroundLakeMinSize + 1 : this.undergroundLakeMaxSize);
        this.undergroundLakeMinAltitude = (this.undergroundLakeMinAltitude < 0 ? 0 : this.undergroundLakeMinAltitude > PTMDefaultValues.yLimit.intValue() - 1 ? PTMDefaultValues.yLimit.intValue() - 1 : this.undergroundLakeMinAltitude);
        this.undergroundLakeMaxAltitude = (this.undergroundLakeMaxAltitude > PTMDefaultValues.yLimit.intValue() ? PTMDefaultValues.yLimit.intValue() : this.undergroundLakeMaxAltitude <= this.undergroundLakeMinAltitude ? this.undergroundLakeMinAltitude + 1 : this.undergroundLakeMaxAltitude);

        this.customTreeMinTime = (this.customTreeMinTime < 1 ? 1 : this.customTreeMinTime);
        this.customTreeMaxTime = ((this.customTreeMaxTime - this.customTreeMinTime) < 1 ? (this.customTreeMinTime + 1) : this.customTreeMaxTime);

    }

    void ReadSettings()
    {
        BufferedReader SettingsReader = null;

        // f = new File(this.worldSaveFolder, PTMDefaultValues.WorldSettingsName.stringValue());
        File f = new File(SettingsDir, PTMDefaultValues.WorldSettingsName.stringValue());
        if (f.exists())
        {

            try
            {
                SettingsReader = new BufferedReader(new FileReader(f));
                String thisLine;
                while ((thisLine = SettingsReader.readLine()) != null)
                {
                    if (thisLine.toLowerCase().contains(":"))
                    {
                        String[] splitsettings = thisLine.split(":");
                        if (splitsettings.length == 2)
                            this.ReadedSettings.put(splitsettings[0].trim(), splitsettings[1].trim());
                    }
                }
            } catch (IOException e)
            {
                e.printStackTrace();

                if (SettingsReader != null)
                    try
                    {
                        SettingsReader.close();
                    } catch (IOException localIOException1)
                    {
                        localIOException1.printStackTrace();
                    }
            } finally
            {
                if (SettingsReader != null)
                    try
                    {
                        SettingsReader.close();
                    } catch (IOException localIOException2)
                    {
                        localIOException2.printStackTrace();
                    }
            }
        }
        ReadWorldSettings();
    }

    private void ReadWorldSettings()
    {


        this.biomeSize = ReadModSettins(PTMDefaultValues.biomeSize.name(), PTMDefaultValues.biomeSize.doubleValue());
        this.minMoisture = ReadModSettins(PTMDefaultValues.minMoisture.name(), PTMDefaultValues.minMoisture.doubleValue());
        this.maxMoisture = ReadModSettins(PTMDefaultValues.maxMoisture.name(), PTMDefaultValues.maxMoisture.doubleValue());
        this.minTemperature = ReadModSettins(PTMDefaultValues.minTemperature.name(), PTMDefaultValues.minTemperature.doubleValue());
        this.maxTemperature = ReadModSettins(PTMDefaultValues.maxTemperature.name(), PTMDefaultValues.maxTemperature.doubleValue());
        this.snowThreshold = ReadModSettins(PTMDefaultValues.snowThreshold.name(), PTMDefaultValues.snowThreshold.doubleValue());
        this.iceThreshold = ReadModSettins(PTMDefaultValues.iceThreshold.name(), PTMDefaultValues.iceThreshold.doubleValue());

        this.muddySwamps = ReadModSettins(PTMDefaultValues.muddySwamps.name(), PTMDefaultValues.muddySwamps.booleanValue());
        this.claySwamps = ReadModSettins(PTMDefaultValues.claySwamps.name(), PTMDefaultValues.claySwamps.booleanValue());
        this.swampSize = ReadModSettins(PTMDefaultValues.swampSize.name(), PTMDefaultValues.swampSize.intValue());

        this.waterlessDeserts = ReadModSettins(PTMDefaultValues.waterlessDeserts.name(), PTMDefaultValues.waterlessDeserts.booleanValue());
        this.removeSurfaceDirtFromDesert = ReadModSettins(PTMDefaultValues.removeSurfaceDirtFromDesert.name(), PTMDefaultValues.removeSurfaceDirtFromDesert.booleanValue());
        this.desertDirt = ReadModSettins(PTMDefaultValues.desertDirt.name(), PTMDefaultValues.desertDirt.booleanValue());
        this.desertDirtFrequency = ReadModSettins(PTMDefaultValues.desertDirtFrequency.name(), PTMDefaultValues.desertDirtFrequency.intValue());

        this.caveRarity = ReadModSettins(PTMDefaultValues.caveRarity.name(), PTMDefaultValues.caveRarity.intValue());
        this.caveFrequency = ReadModSettins(PTMDefaultValues.caveFrequency.name(), PTMDefaultValues.caveFrequency.intValue());
        this.caveMinAltitude = ReadModSettins(PTMDefaultValues.caveMinAltitude.name(), PTMDefaultValues.caveMinAltitude.intValue());
        this.caveMaxAltitude = ReadModSettins(PTMDefaultValues.caveMaxAltitude.name(), PTMDefaultValues.caveMaxAltitude.intValue());
        this.individualCaveRarity = ReadModSettins(PTMDefaultValues.individualCaveRarity.name(), PTMDefaultValues.individualCaveRarity.intValue());
        this.caveSystemFrequency = ReadModSettins(PTMDefaultValues.caveSystemFrequency.name(), PTMDefaultValues.caveSystemFrequency.intValue());
        this.caveSystemPocketChance = ReadModSettins(PTMDefaultValues.caveSystemPocketChance.name(), PTMDefaultValues.caveSystemPocketChance.intValue());
        this.caveSystemPocketMinSize = ReadModSettins(PTMDefaultValues.caveSystemPocketMinSize.name(), PTMDefaultValues.caveSystemPocketMinSize.intValue());
        this.caveSystemPocketMaxSize = ReadModSettins(PTMDefaultValues.caveSystemPocketMaxSize.name(), PTMDefaultValues.caveSystemPocketMaxSize.intValue());
        this.evenCaveDistribution = ReadModSettins(PTMDefaultValues.evenCaveDistribution.name(), PTMDefaultValues.evenCaveDistribution.booleanValue());


        this.waterLevel = ReadModSettins(PTMDefaultValues.waterLevel.name(), PTMDefaultValues.waterLevel.intValue());
        this.waterBlock = ReadModSettins(PTMDefaultValues.waterBlock.name(), PTMDefaultValues.waterBlock.intValue());
        this.maxAverageHeight = ReadModSettins(PTMDefaultValues.maxAverageHeight.name(), PTMDefaultValues.maxAverageHeight.doubleValue());
        this.maxAverageDepth = ReadModSettins(PTMDefaultValues.maxAverageDepth.name(), PTMDefaultValues.maxAverageDepth.doubleValue());
        this.fractureHorizontal = ReadModSettins(PTMDefaultValues.fractureHorizontal.name(), PTMDefaultValues.fractureHorizontal.doubleValue());
        this.fractureVertical = ReadModSettins(PTMDefaultValues.fractureVertical.name(), PTMDefaultValues.fractureVertical.doubleValue());
        this.volatility1 = ReadModSettins(PTMDefaultValues.volatility1.name(), PTMDefaultValues.volatility1.doubleValue());
        this.volatility2 = ReadModSettins(PTMDefaultValues.volatility2.name(), PTMDefaultValues.volatility2.doubleValue());
        this.volatilityWeight1 = ReadModSettins(PTMDefaultValues.volatilityWeight1.name(), PTMDefaultValues.volatilityWeight1.doubleValue());
        this.volatilityWeight2 = ReadModSettins(PTMDefaultValues.volatilityWeight2.name(), PTMDefaultValues.volatilityWeight2.doubleValue());
        this.disableNotchHeightControl = ReadModSettins(PTMDefaultValues.disableNotchHeightControl.name(), PTMDefaultValues.disableNotchHeightControl.booleanValue());

        this.disableBedrock = ReadModSettins(PTMDefaultValues.disableBedrock.name(), PTMDefaultValues.disableBedrock.booleanValue());
        this.ceilingBedrock = ReadModSettins(PTMDefaultValues.ceilingBedrock.name(), PTMDefaultValues.ceilingBedrock.booleanValue());
        this.flatBedrock = ReadModSettins(PTMDefaultValues.flatBedrock.name(), PTMDefaultValues.flatBedrock.booleanValue());
        this.bedrockobsidian = ReadModSettins(PTMDefaultValues.bedrockobsidian.name(), PTMDefaultValues.bedrockobsidian.booleanValue());

        ReadHeightSettings();


        this.removeSurfaceStone = ReadModSettins(PTMDefaultValues.removeSurfaceStone.name(), PTMDefaultValues.removeSurfaceStone.booleanValue());

        this.flowerDepositRarity = this.ReadModSettins(PTMDefaultValues.flowerDepositRarity.name(), PTMDefaultValues.flowerDepositRarity.intValue());
        this.flowerDepositFrequency = this.ReadModSettins(PTMDefaultValues.flowerDepositFrequency.name(), PTMDefaultValues.flowerDepositFrequency.intValue());
        this.flowerDepositMinAltitude = this.ReadModSettins(PTMDefaultValues.flowerDepositMinAltitude.name(), PTMDefaultValues.flowerDepositMinAltitude.intValue());
        this.flowerDepositMaxAltitude = this.ReadModSettins(PTMDefaultValues.flowerDepositMaxAltitude.name(), PTMDefaultValues.flowerDepositMaxAltitude.intValue());
        this.roseDepositRarity = this.ReadModSettins(PTMDefaultValues.roseDepositRarity.name(), PTMDefaultValues.roseDepositRarity.intValue());
        this.roseDepositFrequency = this.ReadModSettins(PTMDefaultValues.roseDepositFrequency.name(), PTMDefaultValues.roseDepositFrequency.intValue());
        this.roseDepositMinAltitude = this.ReadModSettins(PTMDefaultValues.roseDepositMinAltitude.name(), PTMDefaultValues.roseDepositMinAltitude.intValue());
        this.roseDepositMaxAltitude = this.ReadModSettins(PTMDefaultValues.roseDepositMaxAltitude.name(), PTMDefaultValues.roseDepositMaxAltitude.intValue());
        this.brownMushroomDepositRarity = this.ReadModSettins(PTMDefaultValues.brownMushroomDepositRarity.name(), PTMDefaultValues.brownMushroomDepositRarity.intValue());
        this.brownMushroomDepositFrequency = this.ReadModSettins(PTMDefaultValues.brownMushroomDepositFrequency.name(), PTMDefaultValues.brownMushroomDepositFrequency.intValue());
        this.brownMushroomDepositMinAltitude = this.ReadModSettins(PTMDefaultValues.brownMushroomDepositMinAltitude.name(), PTMDefaultValues.brownMushroomDepositMinAltitude.intValue());
        this.brownMushroomDepositMaxAltitude = this.ReadModSettins(PTMDefaultValues.brownMushroomDepositMaxAltitude.name(), PTMDefaultValues.brownMushroomDepositMaxAltitude.intValue());
        this.redMushroomDepositRarity = this.ReadModSettins(PTMDefaultValues.redMushroomDepositRarity.name(), PTMDefaultValues.redMushroomDepositRarity.intValue());
        this.redMushroomDepositFrequency = this.ReadModSettins(PTMDefaultValues.redMushroomDepositFrequency.name(), PTMDefaultValues.redMushroomDepositFrequency.intValue());
        this.redMushroomDepositMinAltitude = this.ReadModSettins(PTMDefaultValues.redMushroomDepositMinAltitude.name(), PTMDefaultValues.redMushroomDepositMinAltitude.intValue());
        this.redMushroomDepositMaxAltitude = this.ReadModSettins(PTMDefaultValues.redMushroomDepositMaxAltitude.name(), PTMDefaultValues.redMushroomDepositMaxAltitude.intValue());
        this.reedDepositRarity = this.ReadModSettins(PTMDefaultValues.reedDepositRarity.name(), PTMDefaultValues.reedDepositRarity.intValue());
        this.reedDepositFrequency = this.ReadModSettins(PTMDefaultValues.reedDepositFrequency.name(), PTMDefaultValues.reedDepositFrequency.intValue());
        this.reedDepositMinAltitude = this.ReadModSettins(PTMDefaultValues.reedDepositMinAltitude.name(), PTMDefaultValues.reedDepositMinAltitude.intValue());
        this.reedDepositMaxAltitude = this.ReadModSettins(PTMDefaultValues.reedDepositMaxAltitude.name(), PTMDefaultValues.reedDepositMaxAltitude.intValue());
        this.pumpkinDepositRarity = this.ReadModSettins(PTMDefaultValues.pumpkinDepositRarity.name(), PTMDefaultValues.pumpkinDepositRarity.intValue());
        this.pumpkinDepositFrequency = this.ReadModSettins(PTMDefaultValues.pumpkinDepositFrequency.name(), PTMDefaultValues.pumpkinDepositFrequency.intValue());
        this.pumpkinDepositMinAltitude = this.ReadModSettins(PTMDefaultValues.pumpkinDepositMinAltitude.name(), PTMDefaultValues.pumpkinDepositMinAltitude.intValue());
        this.pumpkinDepositMaxAltitude = this.ReadModSettins(PTMDefaultValues.pumpkinDepositMaxAltitude.name(), PTMDefaultValues.pumpkinDepositMaxAltitude.intValue());

        this.evenWaterSourceDistribution = this.ReadModSettins(PTMDefaultValues.evenWaterSourceDistribution.name(), PTMDefaultValues.evenWaterSourceDistribution.booleanValue());

        this.waterSourceDepositRarity = this.ReadModSettins(PTMDefaultValues.waterSourceDepositRarity.name(), PTMDefaultValues.waterSourceDepositRarity.intValue());
        this.waterSourceDepositFrequency = this.ReadModSettins(PTMDefaultValues.waterSourceDepositFrequency.name(), PTMDefaultValues.waterSourceDepositFrequency.intValue());
        this.waterSourceDepositMinAltitude = this.ReadModSettins(PTMDefaultValues.waterSourceDepositMinAltitude.name(), PTMDefaultValues.waterSourceDepositMinAltitude.intValue());
        this.waterSourceDepositMaxAltitude = this.ReadModSettins(PTMDefaultValues.waterSourceDepositMaxAltitude.name(), PTMDefaultValues.waterSourceDepositMaxAltitude.intValue());

        this.evenWaterSourceDistribution = this.ReadModSettins(PTMDefaultValues.evenWaterSourceDistribution.name(), PTMDefaultValues.evenWaterSourceDistribution.booleanValue());
        this.evenLavaSourceDistribution = this.ReadModSettins(PTMDefaultValues.evenLavaSourceDistribution.name(), PTMDefaultValues.evenLavaSourceDistribution.booleanValue());

        this.lavaSourceDepositRarity = this.ReadModSettins(PTMDefaultValues.lavaSourceDepositRarity.name(), PTMDefaultValues.lavaSourceDepositRarity.intValue());
        this.lavaSourceDepositFrequency = this.ReadModSettins(PTMDefaultValues.lavaSourceDepositFrequency.name(), PTMDefaultValues.lavaSourceDepositFrequency.intValue());
        this.lavaSourceDepositMinAltitude = this.ReadModSettins(PTMDefaultValues.lavaSourceDepositMinAltitude.name(), PTMDefaultValues.lavaSourceDepositMinAltitude.intValue());
        this.lavaSourceDepositMaxAltitude = this.ReadModSettins(PTMDefaultValues.lavaSourceDepositMaxAltitude.name(), PTMDefaultValues.lavaSourceDepositMaxAltitude.intValue());

        this.dirtDepositRarity1 = this.ReadModSettins(PTMDefaultValues.dirtDepositRarity1.name(), PTMDefaultValues.dirtDepositRarity1.intValue());
        this.dirtDepositFrequency1 = this.ReadModSettins(PTMDefaultValues.dirtDepositFrequency1.name(), PTMDefaultValues.dirtDepositFrequency1.intValue());
        this.dirtDepositSize1 = this.ReadModSettins(PTMDefaultValues.dirtDepositSize1.name(), PTMDefaultValues.dirtDepositSize1.intValue());
        this.dirtDepositMinAltitude1 = this.ReadModSettins(PTMDefaultValues.dirtDepositMinAltitude1.name(), PTMDefaultValues.dirtDepositMinAltitude1.intValue());
        this.dirtDepositMaxAltitude1 = this.ReadModSettins(PTMDefaultValues.dirtDepositMaxAltitude1.name(), PTMDefaultValues.dirtDepositMaxAltitude1.intValue());
        this.dirtDepositRarity2 = this.ReadModSettins(PTMDefaultValues.dirtDepositRarity2.name(), PTMDefaultValues.dirtDepositRarity2.intValue());
        this.dirtDepositFrequency2 = this.ReadModSettins(PTMDefaultValues.dirtDepositFrequency2.name(), PTMDefaultValues.dirtDepositFrequency2.intValue());
        this.dirtDepositSize2 = this.ReadModSettins(PTMDefaultValues.dirtDepositSize2.name(), PTMDefaultValues.dirtDepositSize2.intValue());
        this.dirtDepositMinAltitude2 = this.ReadModSettins(PTMDefaultValues.dirtDepositMinAltitude2.name(), PTMDefaultValues.dirtDepositMinAltitude2.intValue());
        this.dirtDepositMaxAltitude2 = this.ReadModSettins(PTMDefaultValues.dirtDepositMaxAltitude2.name(), PTMDefaultValues.dirtDepositMaxAltitude2.intValue());
        this.dirtDepositRarity3 = this.ReadModSettins(PTMDefaultValues.dirtDepositRarity3.name(), PTMDefaultValues.dirtDepositRarity3.intValue());
        this.dirtDepositFrequency3 = this.ReadModSettins(PTMDefaultValues.dirtDepositFrequency3.name(), PTMDefaultValues.dirtDepositFrequency3.intValue());
        this.dirtDepositSize3 = this.ReadModSettins(PTMDefaultValues.dirtDepositSize3.name(), PTMDefaultValues.dirtDepositSize3.intValue());
        this.dirtDepositMinAltitude3 = this.ReadModSettins(PTMDefaultValues.dirtDepositMinAltitude3.name(), PTMDefaultValues.dirtDepositMinAltitude3.intValue());
        this.dirtDepositMaxAltitude3 = this.ReadModSettins(PTMDefaultValues.dirtDepositMaxAltitude3.name(), PTMDefaultValues.dirtDepositMaxAltitude3.intValue());
        this.dirtDepositRarity4 = this.ReadModSettins(PTMDefaultValues.dirtDepositRarity4.name(), PTMDefaultValues.dirtDepositRarity4.intValue());
        this.dirtDepositFrequency4 = this.ReadModSettins(PTMDefaultValues.dirtDepositFrequency4.name(), PTMDefaultValues.dirtDepositFrequency4.intValue());
        this.dirtDepositSize4 = this.ReadModSettins(PTMDefaultValues.dirtDepositSize4.name(), PTMDefaultValues.dirtDepositSize4.intValue());
        this.dirtDepositMinAltitude4 = this.ReadModSettins(PTMDefaultValues.dirtDepositMinAltitude4.name(), PTMDefaultValues.dirtDepositMinAltitude4.intValue());
        this.dirtDepositMaxAltitude4 = this.ReadModSettins(PTMDefaultValues.dirtDepositMaxAltitude4.name(), PTMDefaultValues.dirtDepositMaxAltitude4.intValue());
        this.gravelDepositRarity1 = this.ReadModSettins(PTMDefaultValues.gravelDepositRarity1.name(), PTMDefaultValues.gravelDepositRarity1.intValue());
        this.gravelDepositFrequency1 = this.ReadModSettins(PTMDefaultValues.gravelDepositFrequency1.name(), PTMDefaultValues.gravelDepositFrequency1.intValue());
        this.gravelDepositSize1 = this.ReadModSettins(PTMDefaultValues.gravelDepositSize1.name(), PTMDefaultValues.gravelDepositSize1.intValue());
        this.gravelDepositMinAltitude1 = this.ReadModSettins(PTMDefaultValues.gravelDepositMinAltitude1.name(), PTMDefaultValues.gravelDepositMinAltitude1.intValue());
        this.gravelDepositMaxAltitude1 = this.ReadModSettins(PTMDefaultValues.gravelDepositMaxAltitude1.name(), PTMDefaultValues.gravelDepositMaxAltitude1.intValue());
        this.gravelDepositRarity2 = this.ReadModSettins(PTMDefaultValues.gravelDepositRarity2.name(), PTMDefaultValues.gravelDepositRarity2.intValue());
        this.gravelDepositFrequency2 = this.ReadModSettins(PTMDefaultValues.gravelDepositFrequency2.name(), PTMDefaultValues.gravelDepositFrequency2.intValue());
        this.gravelDepositSize2 = this.ReadModSettins(PTMDefaultValues.gravelDepositSize2.name(), PTMDefaultValues.gravelDepositSize2.intValue());
        this.gravelDepositMinAltitude2 = this.ReadModSettins(PTMDefaultValues.gravelDepositMinAltitude2.name(), PTMDefaultValues.gravelDepositMinAltitude2.intValue());
        this.gravelDepositMaxAltitude2 = this.ReadModSettins(PTMDefaultValues.gravelDepositMaxAltitude2.name(), PTMDefaultValues.gravelDepositMaxAltitude2.intValue());
        this.gravelDepositRarity3 = this.ReadModSettins(PTMDefaultValues.gravelDepositRarity3.name(), PTMDefaultValues.gravelDepositRarity3.intValue());
        this.gravelDepositFrequency3 = this.ReadModSettins(PTMDefaultValues.gravelDepositFrequency3.name(), PTMDefaultValues.gravelDepositFrequency3.intValue());
        this.gravelDepositSize3 = this.ReadModSettins(PTMDefaultValues.gravelDepositSize3.name(), PTMDefaultValues.gravelDepositSize3.intValue());
        this.gravelDepositMinAltitude3 = this.ReadModSettins(PTMDefaultValues.gravelDepositMinAltitude3.name(), PTMDefaultValues.gravelDepositMinAltitude3.intValue());
        this.gravelDepositMaxAltitude3 = this.ReadModSettins(PTMDefaultValues.gravelDepositMaxAltitude3.name(), PTMDefaultValues.gravelDepositMaxAltitude3.intValue());
        this.gravelDepositRarity4 = this.ReadModSettins(PTMDefaultValues.gravelDepositRarity4.name(), PTMDefaultValues.gravelDepositRarity4.intValue());
        this.gravelDepositFrequency4 = this.ReadModSettins(PTMDefaultValues.gravelDepositFrequency4.name(), PTMDefaultValues.gravelDepositFrequency4.intValue());
        this.gravelDepositSize4 = this.ReadModSettins(PTMDefaultValues.gravelDepositSize4.name(), PTMDefaultValues.gravelDepositSize4.intValue());
        this.gravelDepositMinAltitude4 = this.ReadModSettins(PTMDefaultValues.gravelDepositMinAltitude4.name(), PTMDefaultValues.gravelDepositMinAltitude4.intValue());
        this.gravelDepositMaxAltitude4 = this.ReadModSettins(PTMDefaultValues.gravelDepositMaxAltitude4.name(), PTMDefaultValues.gravelDepositMaxAltitude4.intValue());
        this.clayDepositRarity1 = this.ReadModSettins(PTMDefaultValues.clayDepositRarity1.name(), PTMDefaultValues.clayDepositRarity1.intValue());
        this.clayDepositFrequency1 = this.ReadModSettins(PTMDefaultValues.clayDepositFrequency1.name(), PTMDefaultValues.clayDepositFrequency1.intValue());
        this.clayDepositSize1 = this.ReadModSettins(PTMDefaultValues.clayDepositSize1.name(), PTMDefaultValues.clayDepositSize1.intValue());
        this.clayDepositMinAltitude1 = this.ReadModSettins(PTMDefaultValues.clayDepositMinAltitude1.name(), PTMDefaultValues.clayDepositMinAltitude1.intValue());
        this.clayDepositMaxAltitude1 = this.ReadModSettins(PTMDefaultValues.clayDepositMaxAltitude1.name(), PTMDefaultValues.clayDepositMaxAltitude1.intValue());
        this.clayDepositRarity2 = this.ReadModSettins(PTMDefaultValues.clayDepositRarity2.name(), PTMDefaultValues.clayDepositRarity2.intValue());
        this.clayDepositFrequency2 = this.ReadModSettins(PTMDefaultValues.clayDepositFrequency2.name(), PTMDefaultValues.clayDepositFrequency2.intValue());
        this.clayDepositSize2 = this.ReadModSettins(PTMDefaultValues.clayDepositSize2.name(), PTMDefaultValues.clayDepositSize2.intValue());
        this.clayDepositMinAltitude2 = this.ReadModSettins(PTMDefaultValues.clayDepositMinAltitude2.name(), PTMDefaultValues.clayDepositMinAltitude2.intValue());
        this.clayDepositMaxAltitude2 = this.ReadModSettins(PTMDefaultValues.clayDepositMaxAltitude2.name(), PTMDefaultValues.clayDepositMaxAltitude2.intValue());
        this.clayDepositRarity3 = this.ReadModSettins(PTMDefaultValues.clayDepositRarity3.name(), PTMDefaultValues.clayDepositRarity3.intValue());
        this.clayDepositFrequency3 = this.ReadModSettins(PTMDefaultValues.clayDepositFrequency3.name(), PTMDefaultValues.clayDepositFrequency3.intValue());
        this.clayDepositSize3 = this.ReadModSettins(PTMDefaultValues.clayDepositSize3.name(), PTMDefaultValues.clayDepositSize3.intValue());
        this.clayDepositMinAltitude3 = this.ReadModSettins(PTMDefaultValues.clayDepositMinAltitude3.name(), PTMDefaultValues.clayDepositMinAltitude3.intValue());
        this.clayDepositMaxAltitude3 = this.ReadModSettins(PTMDefaultValues.clayDepositMaxAltitude3.name(), PTMDefaultValues.clayDepositMaxAltitude3.intValue());
        this.clayDepositRarity4 = this.ReadModSettins(PTMDefaultValues.clayDepositRarity4.name(), PTMDefaultValues.clayDepositRarity4.intValue());
        this.clayDepositFrequency4 = this.ReadModSettins(PTMDefaultValues.clayDepositFrequency4.name(), PTMDefaultValues.clayDepositFrequency4.intValue());
        this.clayDepositSize4 = this.ReadModSettins(PTMDefaultValues.clayDepositSize4.name(), PTMDefaultValues.clayDepositSize4.intValue());
        this.clayDepositMinAltitude4 = this.ReadModSettins(PTMDefaultValues.clayDepositMinAltitude4.name(), PTMDefaultValues.clayDepositMinAltitude4.intValue());
        this.clayDepositMaxAltitude4 = this.ReadModSettins(PTMDefaultValues.clayDepositMaxAltitude4.name(), PTMDefaultValues.clayDepositMaxAltitude4.intValue());
        this.coalDepositRarity1 = this.ReadModSettins(PTMDefaultValues.coalDepositRarity1.name(), PTMDefaultValues.coalDepositRarity1.intValue());
        this.coalDepositFrequency1 = this.ReadModSettins(PTMDefaultValues.coalDepositFrequency1.name(), PTMDefaultValues.coalDepositFrequency1.intValue());
        this.coalDepositSize1 = this.ReadModSettins(PTMDefaultValues.coalDepositSize1.name(), PTMDefaultValues.coalDepositSize1.intValue());
        this.coalDepositMinAltitude1 = this.ReadModSettins(PTMDefaultValues.coalDepositMinAltitude1.name(), PTMDefaultValues.coalDepositMinAltitude1.intValue());
        this.coalDepositMaxAltitude1 = this.ReadModSettins(PTMDefaultValues.coalDepositMaxAltitude1.name(), PTMDefaultValues.coalDepositMaxAltitude1.intValue());
        this.coalDepositRarity2 = this.ReadModSettins(PTMDefaultValues.coalDepositRarity2.name(), PTMDefaultValues.coalDepositRarity2.intValue());
        this.coalDepositFrequency2 = this.ReadModSettins(PTMDefaultValues.coalDepositFrequency2.name(), PTMDefaultValues.coalDepositFrequency2.intValue());
        this.coalDepositSize2 = this.ReadModSettins(PTMDefaultValues.coalDepositSize2.name(), PTMDefaultValues.coalDepositSize2.intValue());
        this.coalDepositMinAltitude2 = this.ReadModSettins(PTMDefaultValues.coalDepositMinAltitude2.name(), PTMDefaultValues.coalDepositMinAltitude2.intValue());
        this.coalDepositMaxAltitude2 = this.ReadModSettins(PTMDefaultValues.coalDepositMaxAltitude2.name(), PTMDefaultValues.coalDepositMaxAltitude2.intValue());
        this.coalDepositRarity3 = this.ReadModSettins(PTMDefaultValues.coalDepositRarity3.name(), PTMDefaultValues.coalDepositRarity3.intValue());
        this.coalDepositFrequency3 = this.ReadModSettins(PTMDefaultValues.coalDepositFrequency3.name(), PTMDefaultValues.coalDepositFrequency3.intValue());
        this.coalDepositSize3 = this.ReadModSettins(PTMDefaultValues.coalDepositSize3.name(), PTMDefaultValues.coalDepositSize3.intValue());
        this.coalDepositMinAltitude3 = this.ReadModSettins(PTMDefaultValues.coalDepositMinAltitude3.name(), PTMDefaultValues.coalDepositMinAltitude3.intValue());
        this.coalDepositMaxAltitude3 = this.ReadModSettins(PTMDefaultValues.coalDepositMaxAltitude3.name(), PTMDefaultValues.coalDepositMaxAltitude3.intValue());
        this.coalDepositRarity4 = this.ReadModSettins(PTMDefaultValues.coalDepositRarity4.name(), PTMDefaultValues.coalDepositRarity4.intValue());
        this.coalDepositFrequency4 = this.ReadModSettins(PTMDefaultValues.coalDepositFrequency4.name(), PTMDefaultValues.coalDepositFrequency4.intValue());
        this.coalDepositSize4 = this.ReadModSettins(PTMDefaultValues.coalDepositSize4.name(), PTMDefaultValues.coalDepositSize4.intValue());
        this.coalDepositMinAltitude4 = this.ReadModSettins(PTMDefaultValues.coalDepositMinAltitude4.name(), PTMDefaultValues.coalDepositMinAltitude4.intValue());
        this.coalDepositMaxAltitude4 = this.ReadModSettins(PTMDefaultValues.coalDepositMaxAltitude4.name(), PTMDefaultValues.coalDepositMaxAltitude4.intValue());
        this.ironDepositRarity1 = this.ReadModSettins(PTMDefaultValues.ironDepositRarity1.name(), PTMDefaultValues.ironDepositRarity1.intValue());
        this.ironDepositFrequency1 = this.ReadModSettins(PTMDefaultValues.ironDepositFrequency1.name(), PTMDefaultValues.ironDepositFrequency1.intValue());
        this.ironDepositSize1 = this.ReadModSettins(PTMDefaultValues.ironDepositSize1.name(), PTMDefaultValues.ironDepositSize1.intValue());
        this.ironDepositMinAltitude1 = this.ReadModSettins(PTMDefaultValues.ironDepositMinAltitude1.name(), PTMDefaultValues.ironDepositMinAltitude1.intValue());
        this.ironDepositMaxAltitude1 = this.ReadModSettins(PTMDefaultValues.ironDepositMaxAltitude1.name(), PTMDefaultValues.ironDepositMaxAltitude1.intValue());
        this.ironDepositRarity2 = this.ReadModSettins(PTMDefaultValues.ironDepositRarity2.name(), PTMDefaultValues.ironDepositRarity2.intValue());
        this.ironDepositFrequency2 = this.ReadModSettins(PTMDefaultValues.ironDepositFrequency2.name(), PTMDefaultValues.ironDepositFrequency2.intValue());
        this.ironDepositSize2 = this.ReadModSettins(PTMDefaultValues.ironDepositSize2.name(), PTMDefaultValues.ironDepositSize2.intValue());
        this.ironDepositMinAltitude2 = this.ReadModSettins(PTMDefaultValues.ironDepositMinAltitude2.name(), PTMDefaultValues.ironDepositMinAltitude2.intValue());
        this.ironDepositMaxAltitude2 = this.ReadModSettins(PTMDefaultValues.ironDepositMaxAltitude2.name(), PTMDefaultValues.ironDepositMaxAltitude2.intValue());
        this.ironDepositRarity3 = this.ReadModSettins(PTMDefaultValues.ironDepositRarity3.name(), PTMDefaultValues.ironDepositRarity3.intValue());
        this.ironDepositFrequency3 = this.ReadModSettins(PTMDefaultValues.ironDepositFrequency3.name(), PTMDefaultValues.ironDepositFrequency3.intValue());
        this.ironDepositSize3 = this.ReadModSettins(PTMDefaultValues.ironDepositSize3.name(), PTMDefaultValues.ironDepositSize3.intValue());
        this.ironDepositMinAltitude3 = this.ReadModSettins(PTMDefaultValues.ironDepositMinAltitude3.name(), PTMDefaultValues.ironDepositMinAltitude3.intValue());
        this.ironDepositMaxAltitude3 = this.ReadModSettins(PTMDefaultValues.ironDepositMaxAltitude3.name(), PTMDefaultValues.ironDepositMaxAltitude3.intValue());
        this.ironDepositRarity4 = this.ReadModSettins(PTMDefaultValues.ironDepositRarity4.name(), PTMDefaultValues.ironDepositRarity4.intValue());
        this.ironDepositFrequency4 = this.ReadModSettins(PTMDefaultValues.ironDepositFrequency4.name(), PTMDefaultValues.ironDepositFrequency4.intValue());
        this.ironDepositSize4 = this.ReadModSettins(PTMDefaultValues.ironDepositSize4.name(), PTMDefaultValues.ironDepositSize4.intValue());
        this.ironDepositMinAltitude4 = this.ReadModSettins(PTMDefaultValues.ironDepositMinAltitude4.name(), PTMDefaultValues.ironDepositMinAltitude4.intValue());
        this.ironDepositMaxAltitude4 = this.ReadModSettins(PTMDefaultValues.ironDepositMaxAltitude4.name(), PTMDefaultValues.ironDepositMaxAltitude4.intValue());
        this.goldDepositRarity1 = this.ReadModSettins(PTMDefaultValues.goldDepositRarity1.name(), PTMDefaultValues.goldDepositRarity1.intValue());
        this.goldDepositFrequency1 = this.ReadModSettins(PTMDefaultValues.goldDepositFrequency1.name(), PTMDefaultValues.goldDepositFrequency1.intValue());
        this.goldDepositSize1 = this.ReadModSettins(PTMDefaultValues.goldDepositSize1.name(), PTMDefaultValues.goldDepositSize1.intValue());
        this.goldDepositMinAltitude1 = this.ReadModSettins(PTMDefaultValues.goldDepositMinAltitude1.name(), PTMDefaultValues.goldDepositMinAltitude1.intValue());
        this.goldDepositMaxAltitude1 = this.ReadModSettins(PTMDefaultValues.goldDepositMaxAltitude1.name(), PTMDefaultValues.goldDepositMaxAltitude1.intValue());
        this.goldDepositRarity2 = this.ReadModSettins(PTMDefaultValues.goldDepositRarity2.name(), PTMDefaultValues.goldDepositRarity2.intValue());
        this.goldDepositFrequency2 = this.ReadModSettins(PTMDefaultValues.goldDepositFrequency2.name(), PTMDefaultValues.goldDepositFrequency2.intValue());
        this.goldDepositSize2 = this.ReadModSettins(PTMDefaultValues.goldDepositSize2.name(), PTMDefaultValues.goldDepositSize2.intValue());
        this.goldDepositMinAltitude2 = this.ReadModSettins(PTMDefaultValues.goldDepositMinAltitude2.name(), PTMDefaultValues.goldDepositMinAltitude2.intValue());
        this.goldDepositMaxAltitude2 = this.ReadModSettins(PTMDefaultValues.goldDepositMaxAltitude2.name(), PTMDefaultValues.goldDepositMaxAltitude2.intValue());
        this.goldDepositRarity3 = this.ReadModSettins(PTMDefaultValues.goldDepositRarity3.name(), PTMDefaultValues.goldDepositRarity3.intValue());
        this.goldDepositFrequency3 = this.ReadModSettins(PTMDefaultValues.goldDepositFrequency3.name(), PTMDefaultValues.goldDepositFrequency3.intValue());
        this.goldDepositSize3 = this.ReadModSettins(PTMDefaultValues.goldDepositSize3.name(), PTMDefaultValues.goldDepositSize3.intValue());
        this.goldDepositMinAltitude3 = this.ReadModSettins(PTMDefaultValues.goldDepositMinAltitude3.name(), PTMDefaultValues.goldDepositMinAltitude3.intValue());
        this.goldDepositMaxAltitude3 = this.ReadModSettins(PTMDefaultValues.goldDepositMaxAltitude3.name(), PTMDefaultValues.goldDepositMaxAltitude3.intValue());
        this.goldDepositRarity4 = this.ReadModSettins(PTMDefaultValues.goldDepositRarity4.name(), PTMDefaultValues.goldDepositRarity4.intValue());
        this.goldDepositFrequency4 = this.ReadModSettins(PTMDefaultValues.goldDepositFrequency4.name(), PTMDefaultValues.goldDepositFrequency4.intValue());
        this.goldDepositSize4 = this.ReadModSettins(PTMDefaultValues.goldDepositSize4.name(), PTMDefaultValues.goldDepositSize4.intValue());
        this.goldDepositMinAltitude4 = this.ReadModSettins(PTMDefaultValues.goldDepositMinAltitude4.name(), PTMDefaultValues.goldDepositMinAltitude4.intValue());
        this.goldDepositMaxAltitude4 = this.ReadModSettins(PTMDefaultValues.goldDepositMaxAltitude4.name(), PTMDefaultValues.goldDepositMaxAltitude4.intValue());
        this.redstoneDepositRarity1 = this.ReadModSettins(PTMDefaultValues.redstoneDepositRarity1.name(), PTMDefaultValues.redstoneDepositRarity1.intValue());
        this.redstoneDepositFrequency1 = this.ReadModSettins(PTMDefaultValues.redstoneDepositFrequency1.name(), PTMDefaultValues.redstoneDepositFrequency1.intValue());
        this.redstoneDepositSize1 = this.ReadModSettins(PTMDefaultValues.redstoneDepositSize1.name(), PTMDefaultValues.redstoneDepositSize1.intValue());
        this.redstoneDepositMinAltitude1 = this.ReadModSettins(PTMDefaultValues.redstoneDepositMinAltitude1.name(), PTMDefaultValues.redstoneDepositMinAltitude1.intValue());
        this.redstoneDepositMaxAltitude1 = this.ReadModSettins(PTMDefaultValues.redstoneDepositMaxAltitude1.name(), PTMDefaultValues.redstoneDepositMaxAltitude1.intValue());
        this.redstoneDepositRarity2 = this.ReadModSettins(PTMDefaultValues.redstoneDepositRarity2.name(), PTMDefaultValues.redstoneDepositRarity2.intValue());
        this.redstoneDepositFrequency2 = this.ReadModSettins(PTMDefaultValues.redstoneDepositFrequency2.name(), PTMDefaultValues.redstoneDepositFrequency2.intValue());
        this.redstoneDepositSize2 = this.ReadModSettins(PTMDefaultValues.redstoneDepositSize2.name(), PTMDefaultValues.redstoneDepositSize2.intValue());
        this.redstoneDepositMinAltitude2 = this.ReadModSettins(PTMDefaultValues.redstoneDepositMinAltitude2.name(), PTMDefaultValues.redstoneDepositMinAltitude2.intValue());
        this.redstoneDepositMaxAltitude2 = this.ReadModSettins(PTMDefaultValues.redstoneDepositMaxAltitude2.name(), PTMDefaultValues.redstoneDepositMaxAltitude2.intValue());
        this.redstoneDepositRarity3 = this.ReadModSettins(PTMDefaultValues.redstoneDepositRarity3.name(), PTMDefaultValues.redstoneDepositRarity3.intValue());
        this.redstoneDepositFrequency3 = this.ReadModSettins(PTMDefaultValues.redstoneDepositFrequency3.name(), PTMDefaultValues.redstoneDepositFrequency3.intValue());
        this.redstoneDepositSize3 = this.ReadModSettins(PTMDefaultValues.redstoneDepositSize3.name(), PTMDefaultValues.redstoneDepositSize3.intValue());
        this.redstoneDepositMinAltitude3 = this.ReadModSettins(PTMDefaultValues.redstoneDepositMinAltitude3.name(), PTMDefaultValues.redstoneDepositMinAltitude3.intValue());
        this.redstoneDepositMaxAltitude3 = this.ReadModSettins(PTMDefaultValues.redstoneDepositMaxAltitude3.name(), PTMDefaultValues.redstoneDepositMaxAltitude3.intValue());
        this.redstoneDepositRarity4 = this.ReadModSettins(PTMDefaultValues.redstoneDepositRarity4.name(), PTMDefaultValues.redstoneDepositRarity4.intValue());
        this.redstoneDepositFrequency4 = this.ReadModSettins(PTMDefaultValues.redstoneDepositFrequency4.name(), PTMDefaultValues.redstoneDepositFrequency4.intValue());
        this.redstoneDepositSize4 = this.ReadModSettins(PTMDefaultValues.redstoneDepositSize4.name(), PTMDefaultValues.redstoneDepositSize4.intValue());
        this.redstoneDepositMinAltitude4 = this.ReadModSettins(PTMDefaultValues.redstoneDepositMinAltitude4.name(), PTMDefaultValues.redstoneDepositMinAltitude4.intValue());
        this.redstoneDepositMaxAltitude4 = this.ReadModSettins(PTMDefaultValues.redstoneDepositMaxAltitude4.name(), PTMDefaultValues.redstoneDepositMaxAltitude4.intValue());
        this.diamondDepositRarity1 = this.ReadModSettins(PTMDefaultValues.diamondDepositRarity1.name(), PTMDefaultValues.diamondDepositRarity1.intValue());
        this.diamondDepositFrequency1 = this.ReadModSettins(PTMDefaultValues.diamondDepositFrequency1.name(), PTMDefaultValues.diamondDepositFrequency1.intValue());
        this.diamondDepositSize1 = this.ReadModSettins(PTMDefaultValues.diamondDepositSize1.name(), PTMDefaultValues.diamondDepositSize1.intValue());
        this.diamondDepositMinAltitude1 = this.ReadModSettins(PTMDefaultValues.diamondDepositMinAltitude1.name(), PTMDefaultValues.diamondDepositMinAltitude1.intValue());
        this.diamondDepositMaxAltitude1 = this.ReadModSettins(PTMDefaultValues.diamondDepositMaxAltitude1.name(), PTMDefaultValues.diamondDepositMaxAltitude1.intValue());
        this.diamondDepositRarity2 = this.ReadModSettins(PTMDefaultValues.diamondDepositRarity2.name(), PTMDefaultValues.diamondDepositRarity2.intValue());
        this.diamondDepositFrequency2 = this.ReadModSettins(PTMDefaultValues.diamondDepositFrequency2.name(), PTMDefaultValues.diamondDepositFrequency2.intValue());
        this.diamondDepositSize2 = this.ReadModSettins(PTMDefaultValues.diamondDepositSize2.name(), PTMDefaultValues.diamondDepositSize2.intValue());
        this.diamondDepositMinAltitude2 = this.ReadModSettins(PTMDefaultValues.diamondDepositMinAltitude2.name(), PTMDefaultValues.diamondDepositMinAltitude2.intValue());
        this.diamondDepositMaxAltitude2 = this.ReadModSettins(PTMDefaultValues.diamondDepositMaxAltitude2.name(), PTMDefaultValues.diamondDepositMaxAltitude2.intValue());
        this.diamondDepositRarity3 = this.ReadModSettins(PTMDefaultValues.diamondDepositRarity3.name(), PTMDefaultValues.diamondDepositRarity3.intValue());
        this.diamondDepositFrequency3 = this.ReadModSettins(PTMDefaultValues.diamondDepositFrequency3.name(), PTMDefaultValues.diamondDepositFrequency3.intValue());
        this.diamondDepositSize3 = this.ReadModSettins(PTMDefaultValues.diamondDepositSize3.name(), PTMDefaultValues.diamondDepositSize3.intValue());
        this.diamondDepositMinAltitude3 = this.ReadModSettins(PTMDefaultValues.diamondDepositMinAltitude3.name(), PTMDefaultValues.diamondDepositMinAltitude3.intValue());
        this.diamondDepositMaxAltitude3 = this.ReadModSettins(PTMDefaultValues.diamondDepositMaxAltitude3.name(), PTMDefaultValues.diamondDepositMaxAltitude3.intValue());
        this.diamondDepositRarity4 = this.ReadModSettins(PTMDefaultValues.diamondDepositRarity4.name(), PTMDefaultValues.diamondDepositRarity4.intValue());
        this.diamondDepositFrequency4 = this.ReadModSettins(PTMDefaultValues.diamondDepositFrequency4.name(), PTMDefaultValues.diamondDepositFrequency4.intValue());
        this.diamondDepositSize4 = this.ReadModSettins(PTMDefaultValues.diamondDepositSize4.name(), PTMDefaultValues.diamondDepositSize4.intValue());
        this.diamondDepositMinAltitude4 = this.ReadModSettins(PTMDefaultValues.diamondDepositMinAltitude4.name(), PTMDefaultValues.diamondDepositMinAltitude4.intValue());
        this.diamondDepositMaxAltitude4 = this.ReadModSettins(PTMDefaultValues.diamondDepositMaxAltitude4.name(), PTMDefaultValues.diamondDepositMaxAltitude4.intValue());
        this.lapislazuliDepositRarity1 = this.ReadModSettins(PTMDefaultValues.lapislazuliDepositRarity1.name(), PTMDefaultValues.lapislazuliDepositRarity1.intValue());
        this.lapislazuliDepositFrequency1 = this.ReadModSettins(PTMDefaultValues.lapislazuliDepositFrequency1.name(), PTMDefaultValues.lapislazuliDepositFrequency1.intValue());
        this.lapislazuliDepositSize1 = this.ReadModSettins(PTMDefaultValues.lapislazuliDepositSize1.name(), PTMDefaultValues.lapislazuliDepositSize1.intValue());
        this.lapislazuliDepositMinAltitude1 = this.ReadModSettins(PTMDefaultValues.lapislazuliDepositMinAltitude1.name(), PTMDefaultValues.lapislazuliDepositMinAltitude1.intValue());
        this.lapislazuliDepositMaxAltitude1 = this.ReadModSettins(PTMDefaultValues.lapislazuliDepositMaxAltitude1.name(), PTMDefaultValues.lapislazuliDepositMaxAltitude1.intValue());
        this.lapislazuliDepositRarity2 = this.ReadModSettins(PTMDefaultValues.lapislazuliDepositRarity2.name(), PTMDefaultValues.lapislazuliDepositRarity2.intValue());
        this.lapislazuliDepositFrequency2 = this.ReadModSettins(PTMDefaultValues.lapislazuliDepositFrequency2.name(), PTMDefaultValues.lapislazuliDepositFrequency2.intValue());
        this.lapislazuliDepositSize2 = this.ReadModSettins(PTMDefaultValues.lapislazuliDepositSize2.name(), PTMDefaultValues.lapislazuliDepositSize2.intValue());
        this.lapislazuliDepositMinAltitude2 = this.ReadModSettins(PTMDefaultValues.lapislazuliDepositMinAltitude2.name(), PTMDefaultValues.lapislazuliDepositMinAltitude2.intValue());
        this.lapislazuliDepositMaxAltitude2 = this.ReadModSettins(PTMDefaultValues.lapislazuliDepositMaxAltitude2.name(), PTMDefaultValues.lapislazuliDepositMaxAltitude2.intValue());
        this.lapislazuliDepositRarity3 = this.ReadModSettins(PTMDefaultValues.lapislazuliDepositRarity3.name(), PTMDefaultValues.lapislazuliDepositRarity3.intValue());
        this.lapislazuliDepositFrequency3 = this.ReadModSettins(PTMDefaultValues.lapislazuliDepositFrequency3.name(), PTMDefaultValues.lapislazuliDepositFrequency3.intValue());
        this.lapislazuliDepositSize3 = this.ReadModSettins(PTMDefaultValues.lapislazuliDepositSize3.name(), PTMDefaultValues.lapislazuliDepositSize3.intValue());
        this.lapislazuliDepositMinAltitude3 = this.ReadModSettins(PTMDefaultValues.lapislazuliDepositMinAltitude3.name(), PTMDefaultValues.lapislazuliDepositMinAltitude3.intValue());
        this.lapislazuliDepositMaxAltitude3 = this.ReadModSettins(PTMDefaultValues.lapislazuliDepositMaxAltitude3.name(), PTMDefaultValues.lapislazuliDepositMaxAltitude3.intValue());
        this.lapislazuliDepositRarity4 = this.ReadModSettins(PTMDefaultValues.lapislazuliDepositRarity4.name(), PTMDefaultValues.lapislazuliDepositRarity4.intValue());
        this.lapislazuliDepositFrequency4 = this.ReadModSettins(PTMDefaultValues.lapislazuliDepositFrequency4.name(), PTMDefaultValues.lapislazuliDepositFrequency4.intValue());
        this.lapislazuliDepositSize4 = this.ReadModSettins(PTMDefaultValues.lapislazuliDepositSize4.name(), PTMDefaultValues.lapislazuliDepositSize4.intValue());
        this.lapislazuliDepositMinAltitude4 = this.ReadModSettins(PTMDefaultValues.lapislazuliDepositMinAltitude4.name(), PTMDefaultValues.lapislazuliDepositMinAltitude4.intValue());
        this.lapislazuliDepositMaxAltitude4 = this.ReadModSettins(PTMDefaultValues.lapislazuliDepositMaxAltitude4.name(), PTMDefaultValues.lapislazuliDepositMaxAltitude4.intValue());


        this.disableNotchPonds = this.ReadModSettins(PTMDefaultValues.disableNotchPonds.name(), PTMDefaultValues.disableNotchPonds.booleanValue());

        this.customObjects = this.ReadModSettins(PTMDefaultValues.customObjects.name(), PTMDefaultValues.customObjects.booleanValue());
        this.objectSpawnRatio = this.ReadModSettins(PTMDefaultValues.objectSpawnRatio.name(), PTMDefaultValues.objectSpawnRatio.intValue());
        this.denyObjectsUnderFill = this.ReadModSettins(PTMDefaultValues.denyObjectsUnderFill.name(), PTMDefaultValues.denyObjectsUnderFill.booleanValue());
        this.customTreeMinTime = this.ReadModSettins(PTMDefaultValues.customTreeMinTime.name(), PTMDefaultValues.customTreeMinTime.intValue());
        this.customTreeMaxTime = this.ReadModSettins(PTMDefaultValues.customTreeMaxTime.name(), PTMDefaultValues.customTreeMaxTime.intValue());


        this.notchBiomeTrees = this.ReadModSettins(PTMDefaultValues.notchBiomeTrees.name(), PTMDefaultValues.notchBiomeTrees.booleanValue());
        this.globalTreeDensity = this.ReadModSettins(PTMDefaultValues.globalTreeDensity.name(), PTMDefaultValues.globalTreeDensity.intValue());
        this.rainforestTreeDensity = this.ReadModSettins(PTMDefaultValues.rainforestTreeDensity.name(), PTMDefaultValues.rainforestTreeDensity.intValue());
        this.swamplandTreeDensity = this.ReadModSettins(PTMDefaultValues.swamplandTreeDensity.name(), PTMDefaultValues.swamplandTreeDensity.intValue());
        this.seasonalforestTreeDensity = this.ReadModSettins(PTMDefaultValues.seasonalforestTreeDensity.name(), PTMDefaultValues.seasonalforestTreeDensity.intValue());
        this.forestTreeDensity = this.ReadModSettins(PTMDefaultValues.forestTreeDensity.name(), PTMDefaultValues.forestTreeDensity.intValue());
        this.savannaTreeDensity = this.ReadModSettins(PTMDefaultValues.savannaTreeDensity.name(), PTMDefaultValues.savannaTreeDensity.intValue());
        this.shrublandTreeDensity = this.ReadModSettins(PTMDefaultValues.shrublandTreeDensity.name(), PTMDefaultValues.shrublandTreeDensity.intValue());
        this.taigaTreeDensity = this.ReadModSettins(PTMDefaultValues.taigaTreeDensity.name(), PTMDefaultValues.taigaTreeDensity.intValue());
        this.desertTreeDensity = this.ReadModSettins(PTMDefaultValues.desertTreeDensity.name(), PTMDefaultValues.desertTreeDensity.intValue());
        this.plainsTreeDensity = this.ReadModSettins(PTMDefaultValues.plainsTreeDensity.name(), PTMDefaultValues.plainsTreeDensity.intValue());
        this.iceDesertTreeDensity = this.ReadModSettins(PTMDefaultValues.iceDesertTreeDensity.name(), PTMDefaultValues.iceDesertTreeDensity.intValue());
        this.tundraTreeDensity = this.ReadModSettins(PTMDefaultValues.tundraTreeDensity.name(), PTMDefaultValues.tundraTreeDensity.intValue());
        this.globalCactusDensity = this.ReadModSettins(PTMDefaultValues.globalCactusDensity.name(), PTMDefaultValues.globalCactusDensity.intValue());
        this.desertCactusDensity = this.ReadModSettins(PTMDefaultValues.desertCactusDensity.name(), PTMDefaultValues.desertCactusDensity.intValue());
        this.cactusDepositRarity = this.ReadModSettins(PTMDefaultValues.cactusDepositRarity.name(), PTMDefaultValues.cactusDepositRarity.intValue());
        this.cactusDepositMinAltitude = this.ReadModSettins(PTMDefaultValues.cactusDepositMinAltitude.name(), PTMDefaultValues.cactusDepositMinAltitude.intValue());
        this.cactusDepositMaxAltitude = this.ReadModSettins(PTMDefaultValues.cactusDepositMaxAltitude.name(), PTMDefaultValues.cactusDepositMaxAltitude.intValue());

        this.dungeonRarity = this.ReadModSettins(PTMDefaultValues.dungeonRarity.name(), PTMDefaultValues.dungeonRarity.intValue());
        this.dungeonFrequency = this.ReadModSettins(PTMDefaultValues.dungeonFrequency.name(), PTMDefaultValues.dungeonFrequency.intValue());
        this.dungeonMinAltitude = this.ReadModSettins(PTMDefaultValues.dungeonMinAltitude.name(), PTMDefaultValues.dungeonMinAltitude.intValue());
        this.dungeonMaxAltitude = this.ReadModSettins(PTMDefaultValues.dungeonMaxAltitude.name(), PTMDefaultValues.dungeonMaxAltitude.intValue());


        this.lavaLevelMin = this.ReadModSettins(PTMDefaultValues.lavaLevelMin.name(), PTMDefaultValues.lavaLevelMin.intValue());
        this.lavaLevelMax = this.ReadModSettins(PTMDefaultValues.lavaLevelMax.name(), PTMDefaultValues.lavaLevelMax.intValue());

        this.undergroundLakes = this.ReadModSettins(PTMDefaultValues.undergroundLakes.name(), PTMDefaultValues.undergroundLakes.booleanValue());
        this.undergroundLakesInAir = this.ReadModSettins(PTMDefaultValues.undergroundLakesInAir.name(), PTMDefaultValues.undergroundLakesInAir.booleanValue());
        this.undergroundLakeFrequency = this.ReadModSettins(PTMDefaultValues.undergroundLakeFrequency.name(), PTMDefaultValues.undergroundLakeFrequency.intValue());
        this.undergroundLakeRarity = this.ReadModSettins(PTMDefaultValues.undergroundLakeRarity.name(), PTMDefaultValues.undergroundLakeRarity.intValue());
        this.undergroundLakeMinSize = this.ReadModSettins(PTMDefaultValues.undergroundLakeMinSize.name(), PTMDefaultValues.undergroundLakeMinSize.intValue());
        this.undergroundLakeMaxSize = this.ReadModSettins(PTMDefaultValues.undergroundLakeMaxSize.name(), PTMDefaultValues.undergroundLakeMaxSize.intValue());
        this.undergroundLakeMinAltitude = this.ReadModSettins(PTMDefaultValues.undergroundLakeMinAltitude.name(), PTMDefaultValues.undergroundLakeMinAltitude.intValue());
        this.undergroundLakeMaxAltitude = this.ReadModSettins(PTMDefaultValues.undergroundLakeMaxAltitude.name(), PTMDefaultValues.undergroundLakeMaxAltitude.intValue());


        this.ReadModReplaceSettings();


    }


    private int ReadModSettins(String settingsName, int defaultValue)
    {
        if (this.ReadedSettings.containsKey(settingsName))
        {
            return Integer.valueOf(this.ReadedSettings.get(settingsName));
        }
        return defaultValue;
    }

    private double ReadModSettins(String settingsName, double defaultValue)
    {
        if (this.ReadedSettings.containsKey(settingsName))
        {
            return Double.valueOf(this.ReadedSettings.get(settingsName));
        }
        return defaultValue;
    }

    private boolean ReadModSettins(String settingsName, boolean defaultValue)
    {
        if (this.ReadedSettings.containsKey(settingsName))
        {
            return Boolean.valueOf(this.ReadedSettings.get(settingsName));
        }
        return defaultValue;
    }

    private void ReadModReplaceSettings()
    {
        if (this.ReadedSettings.containsKey("ReplacedBlocks"))
        {
            if (this.ReadedSettings.get("ReplacedBlocks").trim().equals("") || this.ReadedSettings.get("ReplacedBlocks").equals("None"))
                return;
            String[] keys = this.ReadedSettings.get("ReplacedBlocks").split(",");
            try
            {
                for (String key : keys)
                {

                    String[] blocks = key.split("=");

                    this.replaceBlocks.put(Integer.valueOf(blocks[0]), Byte.valueOf(blocks[1]));

                }

            } catch (NumberFormatException e)
            {
                System.out.println("Wrong replace settings: '" + this.ReadedSettings.get("ReplacedBlocks") + "'");
            }

        }


    }

    private void ReadHeightSettings()
    {
        if (this.ReadedSettings.containsKey("CustomHeightControl"))
        {
            if (this.ReadedSettings.get("CustomHeightControl").trim().equals(""))
                return;
            String[] keys = this.ReadedSettings.get("CustomHeightControl").split(",");
            try
            {
                if (keys.length != 17)
                    return;
                for (int i = 0; i < 17; i++)
                    this.heightMatrix[i] = Double.valueOf(keys[i]);

            } catch (NumberFormatException e)
            {
                System.out.println("Wrong height settings: '" + this.ReadedSettings.get("CustomHeightControl") + "'");
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

    void WriteSettings()
    {
      this.WriteSettings(new File(this.SettingsDir, PTMDefaultValues.WorldSettingsName.stringValue()));
    }

    void WriteSettings(File settingsFile)
    {
        try
        {
            this.SettingsWriter = new BufferedWriter(new FileWriter(settingsFile, false));

            WriteWorldSettings();
        } catch (IOException e)
        {
            e.printStackTrace();

            if (this.SettingsWriter != null)
                try
                {
                    this.SettingsWriter.close();
                } catch (IOException localIOException1)
                {
                    localIOException1.printStackTrace();
                }
        } finally
        {
            if (this.SettingsWriter != null)
                try
                {
                    this.SettingsWriter.close();
                } catch (IOException localIOException2)
                {
                    localIOException2.printStackTrace();
                }
        }
    }

    private void WriteWorldSettings() throws IOException
    {
        WriteModTitleSettings("Start Biome Variables :");
        WriteModTitleSettings("All Biome Variables");

        WriteModSettings(PTMDefaultValues.biomeSize.name(), this.biomeSize);
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
        WriteModSettings(PTMDefaultValues.removeSurfaceDirtFromDesert.name(), this.removeSurfaceDirtFromDesert);
        WriteModSettings(PTMDefaultValues.desertDirt.name(), this.desertDirt);
        WriteModSettings(PTMDefaultValues.desertDirtFrequency.name(), this.desertDirtFrequency);

        WriteModTitleSettings("Start Underground Variables :");
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


        WriteModTitleSettings("Start Terrain Variables :");
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
        WriteModSettings(PTMDefaultValues.bedrockobsidian.name(), this.bedrockobsidian);
        WriteModSettings(PTMDefaultValues.disableNotchHeightControl.name(), this.disableNotchHeightControl);
        WriteHeightSettings();


        WriteModTitleSettings("Replace Variables");
        WriteModSettings(PTMDefaultValues.removeSurfaceStone.name(), this.removeSurfaceStone);

        WriteModReplaceSettings();


        this.WriteModTitleSettings("Start BOB Objects Variables :");
        this.WriteModSettings(PTMDefaultValues.customObjects.name(), this.customObjects);
        this.WriteModSettings(PTMDefaultValues.objectSpawnRatio.name(), Integer.valueOf(this.objectSpawnRatio).intValue());
        this.WriteModSettings(PTMDefaultValues.denyObjectsUnderFill.name(), this.denyObjectsUnderFill);
        this.WriteModSettings(PTMDefaultValues.customTreeMinTime.name(), Integer.valueOf(this.customTreeMinTime).intValue());
        this.WriteModSettings(PTMDefaultValues.customTreeMaxTime.name(), Integer.valueOf(this.customTreeMaxTime).intValue());

        this.WriteModTitleSettings("Start Cactus&Tree Variables :");
        this.WriteModSettings(PTMDefaultValues.notchBiomeTrees.name(), this.notchBiomeTrees);
        this.WriteModSettings(PTMDefaultValues.globalTreeDensity.name(), this.globalTreeDensity);
        this.WriteModSettings(PTMDefaultValues.rainforestTreeDensity.name(), this.rainforestTreeDensity);
        this.WriteModSettings(PTMDefaultValues.swamplandTreeDensity.name(), this.swamplandTreeDensity);
        this.WriteModSettings(PTMDefaultValues.seasonalforestTreeDensity.name(), this.seasonalforestTreeDensity);
        this.WriteModSettings(PTMDefaultValues.forestTreeDensity.name(), this.forestTreeDensity);
        this.WriteModSettings(PTMDefaultValues.savannaTreeDensity.name(), this.savannaTreeDensity);
        this.WriteModSettings(PTMDefaultValues.shrublandTreeDensity.name(), this.shrublandTreeDensity);
        this.WriteModSettings(PTMDefaultValues.taigaTreeDensity.name(), this.taigaTreeDensity);
        this.WriteModSettings(PTMDefaultValues.desertTreeDensity.name(), this.desertTreeDensity);
        this.WriteModSettings(PTMDefaultValues.plainsTreeDensity.name(), this.plainsTreeDensity);
        this.WriteModSettings(PTMDefaultValues.iceDesertTreeDensity.name(), this.iceDesertTreeDensity);
        this.WriteModSettings(PTMDefaultValues.tundraTreeDensity.name(), this.tundraTreeDensity);
        this.WriteModSettings(PTMDefaultValues.globalCactusDensity.name(), this.globalCactusDensity);
        this.WriteModSettings(PTMDefaultValues.desertCactusDensity.name(), this.desertCactusDensity);
        this.WriteModSettings(PTMDefaultValues.cactusDepositRarity.name(), this.cactusDepositRarity);
        this.WriteModSettings(PTMDefaultValues.cactusDepositMinAltitude.name(), this.cactusDepositMinAltitude);
        this.WriteModSettings(PTMDefaultValues.cactusDepositMaxAltitude.name(), this.cactusDepositMaxAltitude);


        this.WriteModTitleSettings("Lava Pool Variables");
        this.WriteModSettings(PTMDefaultValues.lavaLevelMin.name(), this.lavaLevelMin);
        this.WriteModSettings(PTMDefaultValues.lavaLevelMax.name(), this.lavaLevelMax);

        this.WriteModTitleSettings("Underground Lake Variables");
        this.WriteModSettings(PTMDefaultValues.undergroundLakes.name(), this.undergroundLakes);
        this.WriteModSettings(PTMDefaultValues.undergroundLakesInAir.name(), this.undergroundLakesInAir);
        this.WriteModSettings(PTMDefaultValues.undergroundLakeFrequency.name(), this.undergroundLakeFrequency);
        this.WriteModSettings(PTMDefaultValues.undergroundLakeRarity.name(), this.undergroundLakeRarity);
        this.WriteModSettings(PTMDefaultValues.undergroundLakeMinSize.name(), this.undergroundLakeMinSize);
        this.WriteModSettings(PTMDefaultValues.undergroundLakeMaxSize.name(), this.undergroundLakeMaxSize);
        this.WriteModSettings(PTMDefaultValues.undergroundLakeMinAltitude.name(), this.undergroundLakeMinAltitude);
        this.WriteModSettings(PTMDefaultValues.undergroundLakeMaxAltitude.name(), this.undergroundLakeMaxAltitude);


        this.WriteModTitleSettings("Start Deposit Variables :");
        this.WriteModTitleSettings("Above Ground Variables");
        this.WriteModSettings(PTMDefaultValues.flowerDepositRarity.name(), this.flowerDepositRarity);
        this.WriteModSettings(PTMDefaultValues.flowerDepositFrequency.name(), this.flowerDepositFrequency);
        this.WriteModSettings(PTMDefaultValues.flowerDepositMinAltitude.name(), this.flowerDepositMinAltitude);
        this.WriteModSettings(PTMDefaultValues.flowerDepositMaxAltitude.name(), this.flowerDepositMaxAltitude);
        this.WriteModSettings(PTMDefaultValues.roseDepositRarity.name(), this.roseDepositRarity);
        this.WriteModSettings(PTMDefaultValues.roseDepositFrequency.name(), this.roseDepositFrequency);
        this.WriteModSettings(PTMDefaultValues.roseDepositMinAltitude.name(), this.roseDepositMinAltitude);
        this.WriteModSettings(PTMDefaultValues.roseDepositMaxAltitude.name(), this.roseDepositMaxAltitude);
        this.WriteModSettings(PTMDefaultValues.brownMushroomDepositRarity.name(), this.brownMushroomDepositRarity);
        this.WriteModSettings(PTMDefaultValues.brownMushroomDepositFrequency.name(), this.brownMushroomDepositFrequency);
        this.WriteModSettings(PTMDefaultValues.brownMushroomDepositMinAltitude.name(), this.brownMushroomDepositMinAltitude);
        this.WriteModSettings(PTMDefaultValues.brownMushroomDepositMaxAltitude.name(), this.brownMushroomDepositMaxAltitude);
        this.WriteModSettings(PTMDefaultValues.redMushroomDepositRarity.name(), this.redMushroomDepositRarity);
        this.WriteModSettings(PTMDefaultValues.redMushroomDepositFrequency.name(), this.redMushroomDepositFrequency);
        this.WriteModSettings(PTMDefaultValues.redMushroomDepositMinAltitude.name(), this.redMushroomDepositMinAltitude);
        this.WriteModSettings(PTMDefaultValues.redMushroomDepositMaxAltitude.name(), this.redMushroomDepositMaxAltitude);
        this.WriteModSettings(PTMDefaultValues.reedDepositRarity.name(), this.reedDepositRarity);
        this.WriteModSettings(PTMDefaultValues.reedDepositFrequency.name(), this.reedDepositFrequency);
        this.WriteModSettings(PTMDefaultValues.reedDepositMinAltitude.name(), this.reedDepositMinAltitude);
        this.WriteModSettings(PTMDefaultValues.reedDepositMaxAltitude.name(), this.reedDepositMaxAltitude);
        this.WriteModSettings(PTMDefaultValues.pumpkinDepositRarity.name(), this.pumpkinDepositRarity);
        this.WriteModSettings(PTMDefaultValues.pumpkinDepositFrequency.name(), this.pumpkinDepositFrequency);
        this.WriteModSettings(PTMDefaultValues.pumpkinDepositMinAltitude.name(), this.pumpkinDepositMinAltitude);
        this.WriteModSettings(PTMDefaultValues.pumpkinDepositMaxAltitude.name(), this.pumpkinDepositMaxAltitude);

        this.WriteModTitleSettings("Above/Below Ground Variables");
        this.WriteModSettings(PTMDefaultValues.evenWaterSourceDistribution.name(), this.evenWaterSourceDistribution);
        this.WriteModSettings(PTMDefaultValues.waterSourceDepositRarity.name(), this.waterSourceDepositRarity);
        this.WriteModSettings(PTMDefaultValues.waterSourceDepositFrequency.name(), this.waterSourceDepositFrequency);
        this.WriteModSettings(PTMDefaultValues.waterSourceDepositMinAltitude.name(), this.waterSourceDepositMinAltitude);
        this.WriteModSettings(PTMDefaultValues.waterSourceDepositMaxAltitude.name(), this.waterSourceDepositMaxAltitude);
        this.WriteModSettings(PTMDefaultValues.evenLavaSourceDistribution.name(), this.evenLavaSourceDistribution);
        this.WriteModSettings(PTMDefaultValues.lavaSourceDepositRarity.name(), this.lavaSourceDepositRarity);
        this.WriteModSettings(PTMDefaultValues.lavaSourceDepositFrequency.name(), this.lavaSourceDepositFrequency);
        this.WriteModSettings(PTMDefaultValues.lavaSourceDepositMinAltitude.name(), this.lavaSourceDepositMinAltitude);
        this.WriteModSettings(PTMDefaultValues.lavaSourceDepositMaxAltitude.name(), this.lavaSourceDepositMaxAltitude);
        this.WriteModSettings(PTMDefaultValues.disableNotchPonds.name(), this.disableNotchPonds);

        this.WriteModTitleSettings("Below Ground Variables");
        this.WriteModSettings(PTMDefaultValues.dungeonRarity.name(), this.dungeonRarity);
        this.WriteModSettings(PTMDefaultValues.dungeonFrequency.name(), this.dungeonFrequency);
        this.WriteModSettings(PTMDefaultValues.dungeonMinAltitude.name(), this.dungeonMinAltitude);
        this.WriteModSettings(PTMDefaultValues.dungeonMaxAltitude.name(), this.dungeonMaxAltitude);

        this.WriteModSettings(PTMDefaultValues.dirtDepositRarity1.name(), this.dirtDepositRarity1);
        this.WriteModSettings(PTMDefaultValues.dirtDepositFrequency1.name(), this.dirtDepositFrequency1);
        this.WriteModSettings(PTMDefaultValues.dirtDepositSize1.name(), this.dirtDepositSize1);
        this.WriteModSettings(PTMDefaultValues.dirtDepositMinAltitude1.name(), this.dirtDepositMinAltitude1);
        this.WriteModSettings(PTMDefaultValues.dirtDepositMaxAltitude1.name(), this.dirtDepositMaxAltitude1);
        this.WriteModSettings(PTMDefaultValues.dirtDepositRarity2.name(), this.dirtDepositRarity2);
        this.WriteModSettings(PTMDefaultValues.dirtDepositFrequency2.name(), this.dirtDepositFrequency2);
        this.WriteModSettings(PTMDefaultValues.dirtDepositSize2.name(), this.dirtDepositSize2);
        this.WriteModSettings(PTMDefaultValues.dirtDepositMinAltitude2.name(), this.dirtDepositMinAltitude2);
        this.WriteModSettings(PTMDefaultValues.dirtDepositMaxAltitude2.name(), this.dirtDepositMaxAltitude2);
        this.WriteModSettings(PTMDefaultValues.dirtDepositRarity3.name(), this.dirtDepositRarity3);
        this.WriteModSettings(PTMDefaultValues.dirtDepositFrequency3.name(), this.dirtDepositFrequency3);
        this.WriteModSettings(PTMDefaultValues.dirtDepositSize3.name(), this.dirtDepositSize3);
        this.WriteModSettings(PTMDefaultValues.dirtDepositMinAltitude3.name(), this.dirtDepositMinAltitude3);
        this.WriteModSettings(PTMDefaultValues.dirtDepositMaxAltitude3.name(), this.dirtDepositMaxAltitude3);
        this.WriteModSettings(PTMDefaultValues.dirtDepositRarity4.name(), this.dirtDepositRarity4);
        this.WriteModSettings(PTMDefaultValues.dirtDepositFrequency4.name(), this.dirtDepositFrequency4);
        this.WriteModSettings(PTMDefaultValues.dirtDepositSize4.name(), this.dirtDepositSize4);
        this.WriteModSettings(PTMDefaultValues.dirtDepositMinAltitude4.name(), this.dirtDepositMinAltitude4);
        this.WriteModSettings(PTMDefaultValues.dirtDepositMaxAltitude4.name(), this.dirtDepositMaxAltitude4);
        this.WriteModSettings(PTMDefaultValues.gravelDepositRarity1.name(), this.gravelDepositRarity1);
        this.WriteModSettings(PTMDefaultValues.gravelDepositFrequency1.name(), this.gravelDepositFrequency1);
        this.WriteModSettings(PTMDefaultValues.gravelDepositSize1.name(), this.gravelDepositSize1);
        this.WriteModSettings(PTMDefaultValues.gravelDepositMinAltitude1.name(), this.gravelDepositMinAltitude1);
        this.WriteModSettings(PTMDefaultValues.gravelDepositMaxAltitude1.name(), this.gravelDepositMaxAltitude1);
        this.WriteModSettings(PTMDefaultValues.gravelDepositRarity2.name(), this.gravelDepositRarity2);
        this.WriteModSettings(PTMDefaultValues.gravelDepositFrequency2.name(), this.gravelDepositFrequency2);
        this.WriteModSettings(PTMDefaultValues.gravelDepositSize2.name(), this.gravelDepositSize2);
        this.WriteModSettings(PTMDefaultValues.gravelDepositMinAltitude2.name(), this.gravelDepositMinAltitude2);
        this.WriteModSettings(PTMDefaultValues.gravelDepositMaxAltitude2.name(), this.gravelDepositMaxAltitude2);
        this.WriteModSettings(PTMDefaultValues.gravelDepositRarity3.name(), this.gravelDepositRarity3);
        this.WriteModSettings(PTMDefaultValues.gravelDepositFrequency3.name(), this.gravelDepositFrequency3);
        this.WriteModSettings(PTMDefaultValues.gravelDepositSize3.name(), this.gravelDepositSize3);
        this.WriteModSettings(PTMDefaultValues.gravelDepositMinAltitude3.name(), this.gravelDepositMinAltitude3);
        this.WriteModSettings(PTMDefaultValues.gravelDepositMaxAltitude3.name(), this.gravelDepositMaxAltitude3);
        this.WriteModSettings(PTMDefaultValues.gravelDepositRarity4.name(), this.gravelDepositRarity4);
        this.WriteModSettings(PTMDefaultValues.gravelDepositFrequency4.name(), this.gravelDepositFrequency4);
        this.WriteModSettings(PTMDefaultValues.gravelDepositSize4.name(), this.gravelDepositSize4);
        this.WriteModSettings(PTMDefaultValues.gravelDepositMinAltitude4.name(), this.gravelDepositMinAltitude4);
        this.WriteModSettings(PTMDefaultValues.gravelDepositMaxAltitude4.name(), this.gravelDepositMaxAltitude4);
        this.WriteModSettings(PTMDefaultValues.clayDepositRarity1.name(), this.clayDepositRarity1);
        this.WriteModSettings(PTMDefaultValues.clayDepositFrequency1.name(), this.clayDepositFrequency1);
        this.WriteModSettings(PTMDefaultValues.clayDepositSize1.name(), this.clayDepositSize1);
        this.WriteModSettings(PTMDefaultValues.clayDepositMinAltitude1.name(), this.clayDepositMinAltitude1);
        this.WriteModSettings(PTMDefaultValues.clayDepositMaxAltitude1.name(), this.clayDepositMaxAltitude1);
        this.WriteModSettings(PTMDefaultValues.clayDepositRarity2.name(), this.clayDepositRarity2);
        this.WriteModSettings(PTMDefaultValues.clayDepositFrequency2.name(), this.clayDepositFrequency2);
        this.WriteModSettings(PTMDefaultValues.clayDepositSize2.name(), this.clayDepositSize2);
        this.WriteModSettings(PTMDefaultValues.clayDepositMinAltitude2.name(), this.clayDepositMinAltitude2);
        this.WriteModSettings(PTMDefaultValues.clayDepositMaxAltitude2.name(), this.clayDepositMaxAltitude2);
        this.WriteModSettings(PTMDefaultValues.clayDepositRarity3.name(), this.clayDepositRarity3);
        this.WriteModSettings(PTMDefaultValues.clayDepositFrequency3.name(), this.clayDepositFrequency3);
        this.WriteModSettings(PTMDefaultValues.clayDepositSize3.name(), this.clayDepositSize3);
        this.WriteModSettings(PTMDefaultValues.clayDepositMinAltitude3.name(), this.clayDepositMinAltitude3);
        this.WriteModSettings(PTMDefaultValues.clayDepositMaxAltitude3.name(), this.clayDepositMaxAltitude3);
        this.WriteModSettings(PTMDefaultValues.clayDepositRarity4.name(), this.clayDepositRarity4);
        this.WriteModSettings(PTMDefaultValues.clayDepositFrequency4.name(), this.clayDepositFrequency4);
        this.WriteModSettings(PTMDefaultValues.clayDepositSize4.name(), this.clayDepositSize4);
        this.WriteModSettings(PTMDefaultValues.clayDepositMinAltitude4.name(), this.clayDepositMinAltitude4);
        this.WriteModSettings(PTMDefaultValues.clayDepositMaxAltitude4.name(), this.clayDepositMaxAltitude4);
        this.WriteModSettings(PTMDefaultValues.coalDepositRarity1.name(), this.coalDepositRarity1);
        this.WriteModSettings(PTMDefaultValues.coalDepositFrequency1.name(), this.coalDepositFrequency1);
        this.WriteModSettings(PTMDefaultValues.coalDepositSize1.name(), this.coalDepositSize1);
        this.WriteModSettings(PTMDefaultValues.coalDepositMinAltitude1.name(), this.coalDepositMinAltitude1);
        this.WriteModSettings(PTMDefaultValues.coalDepositMaxAltitude1.name(), this.coalDepositMaxAltitude1);
        this.WriteModSettings(PTMDefaultValues.coalDepositRarity2.name(), this.coalDepositRarity2);
        this.WriteModSettings(PTMDefaultValues.coalDepositFrequency2.name(), this.coalDepositFrequency2);
        this.WriteModSettings(PTMDefaultValues.coalDepositSize2.name(), this.coalDepositSize2);
        this.WriteModSettings(PTMDefaultValues.coalDepositMinAltitude2.name(), this.coalDepositMinAltitude2);
        this.WriteModSettings(PTMDefaultValues.coalDepositMaxAltitude2.name(), this.coalDepositMaxAltitude2);
        this.WriteModSettings(PTMDefaultValues.coalDepositRarity3.name(), this.coalDepositRarity3);
        this.WriteModSettings(PTMDefaultValues.coalDepositFrequency3.name(), this.coalDepositFrequency3);
        this.WriteModSettings(PTMDefaultValues.coalDepositSize3.name(), this.coalDepositSize3);
        this.WriteModSettings(PTMDefaultValues.coalDepositMinAltitude3.name(), this.coalDepositMinAltitude3);
        this.WriteModSettings(PTMDefaultValues.coalDepositMaxAltitude3.name(), this.coalDepositMaxAltitude3);
        this.WriteModSettings(PTMDefaultValues.coalDepositRarity4.name(), this.coalDepositRarity4);
        this.WriteModSettings(PTMDefaultValues.coalDepositFrequency4.name(), this.coalDepositFrequency4);
        this.WriteModSettings(PTMDefaultValues.coalDepositSize4.name(), this.coalDepositSize4);
        this.WriteModSettings(PTMDefaultValues.coalDepositMinAltitude4.name(), this.coalDepositMinAltitude4);
        this.WriteModSettings(PTMDefaultValues.coalDepositMaxAltitude4.name(), this.coalDepositMaxAltitude4);
        this.WriteModSettings(PTMDefaultValues.ironDepositRarity1.name(), this.ironDepositRarity1);
        this.WriteModSettings(PTMDefaultValues.ironDepositFrequency1.name(), this.ironDepositFrequency1);
        this.WriteModSettings(PTMDefaultValues.ironDepositSize1.name(), this.ironDepositSize1);
        this.WriteModSettings(PTMDefaultValues.ironDepositMinAltitude1.name(), this.ironDepositMinAltitude1);
        this.WriteModSettings(PTMDefaultValues.ironDepositMaxAltitude1.name(), this.ironDepositMaxAltitude1);
        this.WriteModSettings(PTMDefaultValues.ironDepositRarity2.name(), this.ironDepositRarity2);
        this.WriteModSettings(PTMDefaultValues.ironDepositFrequency2.name(), this.ironDepositFrequency2);
        this.WriteModSettings(PTMDefaultValues.ironDepositSize2.name(), this.ironDepositSize2);
        this.WriteModSettings(PTMDefaultValues.ironDepositMinAltitude2.name(), this.ironDepositMinAltitude2);
        this.WriteModSettings(PTMDefaultValues.ironDepositMaxAltitude2.name(), this.ironDepositMaxAltitude2);
        this.WriteModSettings(PTMDefaultValues.ironDepositRarity3.name(), this.ironDepositRarity3);
        this.WriteModSettings(PTMDefaultValues.ironDepositFrequency3.name(), this.ironDepositFrequency3);
        this.WriteModSettings(PTMDefaultValues.ironDepositSize3.name(), this.ironDepositSize3);
        this.WriteModSettings(PTMDefaultValues.ironDepositMinAltitude3.name(), this.ironDepositMinAltitude3);
        this.WriteModSettings(PTMDefaultValues.ironDepositMaxAltitude3.name(), this.ironDepositMaxAltitude3);
        this.WriteModSettings(PTMDefaultValues.ironDepositRarity4.name(), this.ironDepositRarity4);
        this.WriteModSettings(PTMDefaultValues.ironDepositFrequency4.name(), this.ironDepositFrequency4);
        this.WriteModSettings(PTMDefaultValues.ironDepositSize4.name(), this.ironDepositSize4);
        this.WriteModSettings(PTMDefaultValues.ironDepositMinAltitude4.name(), this.ironDepositMinAltitude4);
        this.WriteModSettings(PTMDefaultValues.ironDepositMaxAltitude4.name(), this.ironDepositMaxAltitude4);
        this.WriteModSettings(PTMDefaultValues.goldDepositRarity1.name(), this.goldDepositRarity1);
        this.WriteModSettings(PTMDefaultValues.goldDepositFrequency1.name(), this.goldDepositFrequency1);
        this.WriteModSettings(PTMDefaultValues.goldDepositSize1.name(), this.goldDepositSize1);
        this.WriteModSettings(PTMDefaultValues.goldDepositMinAltitude1.name(), this.goldDepositMinAltitude1);
        this.WriteModSettings(PTMDefaultValues.goldDepositMaxAltitude1.name(), this.goldDepositMaxAltitude1);
        this.WriteModSettings(PTMDefaultValues.goldDepositRarity2.name(), this.goldDepositRarity2);
        this.WriteModSettings(PTMDefaultValues.goldDepositFrequency2.name(), this.goldDepositFrequency2);
        this.WriteModSettings(PTMDefaultValues.goldDepositSize2.name(), this.goldDepositSize2);
        this.WriteModSettings(PTMDefaultValues.goldDepositMinAltitude2.name(), this.goldDepositMinAltitude2);
        this.WriteModSettings(PTMDefaultValues.goldDepositMaxAltitude2.name(), this.goldDepositMaxAltitude2);
        this.WriteModSettings(PTMDefaultValues.goldDepositRarity3.name(), this.goldDepositRarity3);
        this.WriteModSettings(PTMDefaultValues.goldDepositFrequency3.name(), this.goldDepositFrequency3);
        this.WriteModSettings(PTMDefaultValues.goldDepositSize3.name(), this.goldDepositSize3);
        this.WriteModSettings(PTMDefaultValues.goldDepositMinAltitude3.name(), this.goldDepositMinAltitude3);
        this.WriteModSettings(PTMDefaultValues.goldDepositMaxAltitude3.name(), this.goldDepositMaxAltitude3);
        this.WriteModSettings(PTMDefaultValues.goldDepositRarity4.name(), this.goldDepositRarity4);
        this.WriteModSettings(PTMDefaultValues.goldDepositFrequency4.name(), this.goldDepositFrequency4);
        this.WriteModSettings(PTMDefaultValues.goldDepositSize4.name(), this.goldDepositSize4);
        this.WriteModSettings(PTMDefaultValues.goldDepositMinAltitude4.name(), this.goldDepositMinAltitude4);
        this.WriteModSettings(PTMDefaultValues.goldDepositMaxAltitude4.name(), this.goldDepositMaxAltitude4);
        this.WriteModSettings(PTMDefaultValues.redstoneDepositRarity1.name(), this.redstoneDepositRarity1);
        this.WriteModSettings(PTMDefaultValues.redstoneDepositFrequency1.name(), this.redstoneDepositFrequency1);
        this.WriteModSettings(PTMDefaultValues.redstoneDepositSize1.name(), this.redstoneDepositSize1);
        this.WriteModSettings(PTMDefaultValues.redstoneDepositMinAltitude1.name(), this.redstoneDepositMinAltitude1);
        this.WriteModSettings(PTMDefaultValues.redstoneDepositMaxAltitude1.name(), this.redstoneDepositMaxAltitude1);
        this.WriteModSettings(PTMDefaultValues.redstoneDepositRarity2.name(), this.redstoneDepositRarity2);
        this.WriteModSettings(PTMDefaultValues.redstoneDepositFrequency2.name(), this.redstoneDepositFrequency2);
        this.WriteModSettings(PTMDefaultValues.redstoneDepositSize2.name(), this.redstoneDepositSize2);
        this.WriteModSettings(PTMDefaultValues.redstoneDepositMinAltitude2.name(), this.redstoneDepositMinAltitude2);
        this.WriteModSettings(PTMDefaultValues.redstoneDepositMaxAltitude2.name(), this.redstoneDepositMaxAltitude2);
        this.WriteModSettings(PTMDefaultValues.redstoneDepositRarity3.name(), this.redstoneDepositRarity3);
        this.WriteModSettings(PTMDefaultValues.redstoneDepositFrequency3.name(), this.redstoneDepositFrequency3);
        this.WriteModSettings(PTMDefaultValues.redstoneDepositSize3.name(), this.redstoneDepositSize3);
        this.WriteModSettings(PTMDefaultValues.redstoneDepositMinAltitude3.name(), this.redstoneDepositMinAltitude3);
        this.WriteModSettings(PTMDefaultValues.redstoneDepositMaxAltitude3.name(), this.redstoneDepositMaxAltitude3);
        this.WriteModSettings(PTMDefaultValues.redstoneDepositRarity4.name(), this.redstoneDepositRarity4);
        this.WriteModSettings(PTMDefaultValues.redstoneDepositFrequency4.name(), this.redstoneDepositFrequency4);
        this.WriteModSettings(PTMDefaultValues.redstoneDepositSize4.name(), this.redstoneDepositSize4);
        this.WriteModSettings(PTMDefaultValues.redstoneDepositMinAltitude4.name(), this.redstoneDepositMinAltitude4);
        this.WriteModSettings(PTMDefaultValues.redstoneDepositMaxAltitude4.name(), this.redstoneDepositMaxAltitude4);
        this.WriteModSettings(PTMDefaultValues.diamondDepositRarity1.name(), this.diamondDepositRarity1);
        this.WriteModSettings(PTMDefaultValues.diamondDepositFrequency1.name(), this.diamondDepositFrequency1);
        this.WriteModSettings(PTMDefaultValues.diamondDepositSize1.name(), this.diamondDepositSize1);
        this.WriteModSettings(PTMDefaultValues.diamondDepositMinAltitude1.name(), this.diamondDepositMinAltitude1);
        this.WriteModSettings(PTMDefaultValues.diamondDepositMaxAltitude1.name(), this.diamondDepositMaxAltitude1);
        this.WriteModSettings(PTMDefaultValues.diamondDepositRarity2.name(), this.diamondDepositRarity2);
        this.WriteModSettings(PTMDefaultValues.diamondDepositFrequency2.name(), this.diamondDepositFrequency2);
        this.WriteModSettings(PTMDefaultValues.diamondDepositSize2.name(), this.diamondDepositSize2);
        this.WriteModSettings(PTMDefaultValues.diamondDepositMinAltitude2.name(), this.diamondDepositMinAltitude2);
        this.WriteModSettings(PTMDefaultValues.diamondDepositMaxAltitude2.name(), this.diamondDepositMaxAltitude2);
        this.WriteModSettings(PTMDefaultValues.diamondDepositRarity3.name(), this.diamondDepositRarity3);
        this.WriteModSettings(PTMDefaultValues.diamondDepositFrequency3.name(), this.diamondDepositFrequency3);
        this.WriteModSettings(PTMDefaultValues.diamondDepositSize3.name(), this.diamondDepositSize3);
        this.WriteModSettings(PTMDefaultValues.diamondDepositMinAltitude3.name(), this.diamondDepositMinAltitude3);
        this.WriteModSettings(PTMDefaultValues.diamondDepositMaxAltitude3.name(), this.diamondDepositMaxAltitude3);
        this.WriteModSettings(PTMDefaultValues.diamondDepositRarity4.name(), this.diamondDepositRarity4);
        this.WriteModSettings(PTMDefaultValues.diamondDepositFrequency4.name(), this.diamondDepositFrequency4);
        this.WriteModSettings(PTMDefaultValues.diamondDepositSize4.name(), this.diamondDepositSize4);
        this.WriteModSettings(PTMDefaultValues.diamondDepositMinAltitude4.name(), this.diamondDepositMinAltitude4);
        this.WriteModSettings(PTMDefaultValues.diamondDepositMaxAltitude4.name(), this.diamondDepositMaxAltitude4);
        this.WriteModSettings(PTMDefaultValues.lapislazuliDepositRarity1.name(), this.lapislazuliDepositRarity1);
        this.WriteModSettings(PTMDefaultValues.lapislazuliDepositFrequency1.name(), this.lapislazuliDepositFrequency1);
        this.WriteModSettings(PTMDefaultValues.lapislazuliDepositSize1.name(), this.lapislazuliDepositSize1);
        this.WriteModSettings(PTMDefaultValues.lapislazuliDepositMinAltitude1.name(), this.lapislazuliDepositMinAltitude1);
        this.WriteModSettings(PTMDefaultValues.lapislazuliDepositMaxAltitude1.name(), this.lapislazuliDepositMaxAltitude1);
        this.WriteModSettings(PTMDefaultValues.lapislazuliDepositRarity2.name(), this.lapislazuliDepositRarity2);
        this.WriteModSettings(PTMDefaultValues.lapislazuliDepositFrequency2.name(), this.lapislazuliDepositFrequency2);
        this.WriteModSettings(PTMDefaultValues.lapislazuliDepositSize2.name(), this.lapislazuliDepositSize2);
        this.WriteModSettings(PTMDefaultValues.lapislazuliDepositMinAltitude2.name(), this.lapislazuliDepositMinAltitude2);
        this.WriteModSettings(PTMDefaultValues.lapislazuliDepositMaxAltitude2.name(), this.lapislazuliDepositMaxAltitude2);
        this.WriteModSettings(PTMDefaultValues.lapislazuliDepositRarity3.name(), this.lapislazuliDepositRarity3);
        this.WriteModSettings(PTMDefaultValues.lapislazuliDepositFrequency3.name(), this.lapislazuliDepositFrequency3);
        this.WriteModSettings(PTMDefaultValues.lapislazuliDepositSize3.name(), this.lapislazuliDepositSize3);
        this.WriteModSettings(PTMDefaultValues.lapislazuliDepositMinAltitude3.name(), this.lapislazuliDepositMinAltitude3);
        this.WriteModSettings(PTMDefaultValues.lapislazuliDepositMaxAltitude3.name(), this.lapislazuliDepositMaxAltitude3);
        this.WriteModSettings(PTMDefaultValues.lapislazuliDepositRarity4.name(), this.lapislazuliDepositRarity4);
        this.WriteModSettings(PTMDefaultValues.lapislazuliDepositFrequency4.name(), this.lapislazuliDepositFrequency4);
        this.WriteModSettings(PTMDefaultValues.lapislazuliDepositSize4.name(), this.lapislazuliDepositSize4);
        this.WriteModSettings(PTMDefaultValues.lapislazuliDepositMinAltitude4.name(), this.lapislazuliDepositMinAltitude4);
        this.WriteModSettings(PTMDefaultValues.lapislazuliDepositMaxAltitude4.name(), this.lapislazuliDepositMaxAltitude4);
    }

    private void WriteModSettings(String settingsName, int settingsValue) throws IOException
    {
        this.SettingsWriter.write(settingsName + ":" + Integer.toString(settingsValue));
        this.SettingsWriter.newLine();
    }

    private void WriteModSettings(String settingsName, double settingsValue) throws IOException
    {
        this.SettingsWriter.write(settingsName + ":" + Double.toString(settingsValue));
        this.SettingsWriter.newLine();
    }

    private void WriteModSettings(String settingsName, boolean settingsValue) throws IOException
    {
        this.SettingsWriter.write(settingsName + ":" + Boolean.toString(settingsValue));
        this.SettingsWriter.newLine();
    }

    private void WriteModSettings(String settingsName, String settingsValue) throws IOException
    {
        this.SettingsWriter.write(settingsName + ":" + settingsValue);
        this.SettingsWriter.newLine();
    }

    private void WriteModTitleSettings(String title) throws IOException
    {
        this.SettingsWriter.newLine();
        this.SettingsWriter.write("<" + title + ">");
        this.SettingsWriter.newLine();
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

    public boolean createadminium(int y)
    {
        return (!this.disableBedrock) && ((!this.flatBedrock) || (y == 0));
    }

    public byte getadminium()
    {
        return (byte) (this.bedrockobsidian ? Block.OBSIDIAN.id : Block.BEDROCK.id);
    }


}