package com.Khorn.PTMBukkit;

import net.minecraft.server.Block;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public  class Settings
{
    private BufferedWriter SettingsWriter;
    private HashMap<String, String> ReadedSettings = new HashMap<String, String>();
    
    public HashMap<Integer, Byte> replaceBlocks = new HashMap<Integer, Byte>();
    public byte[] ReplaceBlocksMatrix = new byte[256];


    public ArrayList<CustomObject> Objects = new ArrayList<CustomObject>();
    public ArrayList<CustomObjectLegacy> LegacyObjects = new ArrayList<CustomObjectLegacy>();
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


    private int waterLevel;
    private double maxAverageHeight;
    private double maxAverageDepth;
    private double fractureHorizontal;
    private double fractureVertical;
    private double volatility1;
    private double volatility2;
    private double volatilityWeight1;
    private double volatilityWeight2;
    private boolean disableBedrock;
    private boolean flatBedrock;
    private boolean bedrockobsidian;

    public boolean removeSurfaceStone;



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
    public boolean notchBiomeTrees;
    public boolean denyObjectsUnderFill;

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
        this.caveMinAltitude = (this.caveMinAltitude < 0 ? 0 : this.caveMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.caveMinAltitude);
        this.caveMaxAltitude = (this.caveMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.caveMaxAltitude <= this.caveMinAltitude ? this.caveMinAltitude + 1 : this.caveMaxAltitude);
        this.individualCaveRarity = (this.individualCaveRarity < 0 ? 0 : this.individualCaveRarity);
        this.caveSystemFrequency = (this.caveSystemFrequency < 0 ? 0 : this.caveSystemFrequency);
        this.caveSystemPocketChance = (this.caveSystemPocketChance < 0 ? 0 : this.caveSystemPocketChance > 100 ? 100 : this.caveSystemPocketChance);
        this.caveSystemPocketMinSize = (this.caveSystemPocketMinSize < 0 ? 0 : this.caveSystemPocketMinSize);
        this.caveSystemPocketMaxSize = (this.caveSystemPocketMaxSize <= this.caveSystemPocketMinSize ? this.caveSystemPocketMinSize + 1 : this.caveSystemPocketMaxSize);


        this.waterLevel = (this.waterLevel < 0 ? 0 : this.waterLevel > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.waterLevel);

        this.flowerDepositRarity = (this.flowerDepositRarity < 0 ? 0 : this.flowerDepositRarity > 100 ? 100 : this.flowerDepositRarity);
        this.flowerDepositFrequency = (this.flowerDepositFrequency < 0 ? 0 : this.flowerDepositFrequency);
        this.flowerDepositMinAltitude = (this.flowerDepositMinAltitude < 0 ? 0 : this.flowerDepositMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.flowerDepositMinAltitude);
        this.flowerDepositMaxAltitude = (this.flowerDepositMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.flowerDepositMaxAltitude <= this.flowerDepositMinAltitude ? this.flowerDepositMinAltitude + 1 : this.flowerDepositMaxAltitude);
        this.roseDepositRarity = (this.roseDepositRarity < 0 ? 0 : this.roseDepositRarity > 100 ? 100 : this.roseDepositRarity);
        this.roseDepositFrequency = (this.roseDepositFrequency < 0 ? 0 : this.roseDepositFrequency);
        this.roseDepositMinAltitude = (this.roseDepositMinAltitude < 0 ? 0 : this.roseDepositMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.roseDepositMinAltitude);
        this.roseDepositMaxAltitude = (this.roseDepositMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.roseDepositMaxAltitude <= this.roseDepositMinAltitude ? this.roseDepositMinAltitude + 1 : this.roseDepositMaxAltitude);
        this.brownMushroomDepositRarity = (this.brownMushroomDepositRarity < 0 ? 0 : this.brownMushroomDepositRarity > 100 ? 100 : this.brownMushroomDepositRarity);
        this.brownMushroomDepositFrequency = (this.brownMushroomDepositFrequency < 0 ? 0 : this.brownMushroomDepositFrequency);
        this.brownMushroomDepositMinAltitude = (this.brownMushroomDepositMinAltitude < 0 ? 0 : this.brownMushroomDepositMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.brownMushroomDepositMinAltitude);
        this.brownMushroomDepositMaxAltitude = (this.brownMushroomDepositMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.brownMushroomDepositMaxAltitude <= this.brownMushroomDepositMinAltitude ? this.brownMushroomDepositMinAltitude + 1 : this.brownMushroomDepositMaxAltitude);
        this.redMushroomDepositRarity = (this.redMushroomDepositRarity < 0 ? 0 : this.redMushroomDepositRarity > 100 ? 100 : this.redMushroomDepositRarity);
        this.redMushroomDepositFrequency = (this.redMushroomDepositFrequency < 0 ? 0 : this.redMushroomDepositFrequency);
        this.redMushroomDepositMinAltitude = (this.redMushroomDepositMinAltitude < 0 ? 0 : this.redMushroomDepositMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.redMushroomDepositMinAltitude);
        this.redMushroomDepositMaxAltitude = (this.redMushroomDepositMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.redMushroomDepositMaxAltitude <= this.redMushroomDepositMinAltitude ? this.redMushroomDepositMinAltitude + 1 : this.redMushroomDepositMaxAltitude);
        this.reedDepositRarity = (this.reedDepositRarity < 0 ? 0 : this.reedDepositRarity > 100 ? 100 : this.reedDepositRarity);
        this.reedDepositFrequency = (this.reedDepositFrequency < 0 ? 0 : this.reedDepositFrequency);
        this.reedDepositMinAltitude = (this.reedDepositMinAltitude < 0 ? 0 : this.reedDepositMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.reedDepositMinAltitude);
        this.reedDepositMaxAltitude = (this.reedDepositMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.reedDepositMaxAltitude <= this.reedDepositMinAltitude ? this.reedDepositMinAltitude + 1 : this.reedDepositMaxAltitude);
        this.pumpkinDepositRarity = (this.pumpkinDepositRarity < 0 ? 0 : this.pumpkinDepositRarity > 100 ? 100 : this.pumpkinDepositRarity);
        this.pumpkinDepositFrequency = (this.pumpkinDepositFrequency < 0 ? 0 : this.pumpkinDepositFrequency);
        this.pumpkinDepositMinAltitude = (this.pumpkinDepositMinAltitude < 0 ? 0 : this.pumpkinDepositMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.pumpkinDepositMinAltitude);
        this.pumpkinDepositMaxAltitude = (this.pumpkinDepositMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.pumpkinDepositMaxAltitude <= this.pumpkinDepositMinAltitude ? this.pumpkinDepositMinAltitude + 1 : this.pumpkinDepositMaxAltitude);

        this.waterSourceDepositRarity = (this.waterSourceDepositRarity < 0 ? 0 : this.waterSourceDepositRarity > 100 ? 100 : this.waterSourceDepositRarity);
        this.waterSourceDepositFrequency = (this.waterSourceDepositFrequency < 0 ? 0 : this.waterSourceDepositFrequency);
        this.waterSourceDepositMinAltitude = (this.waterSourceDepositMinAltitude < 0 ? 0 : this.waterSourceDepositMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.waterSourceDepositMinAltitude);
        this.waterSourceDepositMaxAltitude = (this.waterSourceDepositMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.waterSourceDepositMaxAltitude <= this.waterSourceDepositMinAltitude ? this.waterSourceDepositMinAltitude + 1 : this.waterSourceDepositMaxAltitude);
        this.lavaSourceDepositRarity = (this.lavaSourceDepositRarity < 0 ? 0 : this.lavaSourceDepositRarity > 100 ? 100 : this.lavaSourceDepositRarity);
        this.lavaSourceDepositFrequency = (this.lavaSourceDepositFrequency < 0 ? 0 : this.lavaSourceDepositFrequency);
        this.lavaSourceDepositMinAltitude = (this.lavaSourceDepositMinAltitude < 0 ? 0 : this.lavaSourceDepositMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.lavaSourceDepositMinAltitude);
        this.lavaSourceDepositMaxAltitude = (this.lavaSourceDepositMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.lavaSourceDepositMaxAltitude <= this.lavaSourceDepositMinAltitude ? this.lavaSourceDepositMinAltitude + 1 : this.lavaSourceDepositMaxAltitude);

        this.dirtDepositRarity1 = (this.dirtDepositRarity1 < 0 ? 0 : this.dirtDepositRarity1 > 100 ? 100 : this.dirtDepositRarity1);
        this.dirtDepositFrequency1 = (this.dirtDepositFrequency1 < 0 ? 0 : this.dirtDepositFrequency1);
        this.dirtDepositSize1 = (this.dirtDepositSize1 < 0 ? 0 : this.dirtDepositSize1);
        this.dirtDepositMinAltitude1 = (this.dirtDepositMinAltitude1 < 0 ? 0 : this.dirtDepositMinAltitude1 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.dirtDepositMinAltitude1);
        this.dirtDepositMaxAltitude1 = (this.dirtDepositMaxAltitude1 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.dirtDepositMaxAltitude1 <= this.dirtDepositMinAltitude1 ? this.dirtDepositMinAltitude1 + 1 : this.dirtDepositMaxAltitude1);
        this.dirtDepositRarity2 = (this.dirtDepositRarity2 < 0 ? 0 : this.dirtDepositRarity2 > 100 ? 100 : this.dirtDepositRarity2);
        this.dirtDepositFrequency2 = (this.dirtDepositFrequency2 < 0 ? 0 : this.dirtDepositFrequency2);
        this.dirtDepositSize2 = (this.dirtDepositSize2 < 0 ? 0 : this.dirtDepositSize2);
        this.dirtDepositMinAltitude2 = (this.dirtDepositMinAltitude2 < 0 ? 0 : this.dirtDepositMinAltitude2 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.dirtDepositMinAltitude2);
        this.dirtDepositMaxAltitude2 = (this.dirtDepositMaxAltitude2 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.dirtDepositMaxAltitude2 <= this.dirtDepositMinAltitude2 ? this.dirtDepositMinAltitude2 + 1 : this.dirtDepositMaxAltitude2);
        this.dirtDepositRarity3 = (this.dirtDepositRarity3 < 0 ? 0 : this.dirtDepositRarity3 > 100 ? 100 : this.dirtDepositRarity3);
        this.dirtDepositFrequency3 = (this.dirtDepositFrequency3 < 0 ? 0 : this.dirtDepositFrequency3);
        this.dirtDepositSize3 = (this.dirtDepositSize3 < 0 ? 0 : this.dirtDepositSize3);
        this.dirtDepositMinAltitude3 = (this.dirtDepositMinAltitude3 < 0 ? 0 : this.dirtDepositMinAltitude3 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.dirtDepositMinAltitude3);
        this.dirtDepositMaxAltitude3 = (this.dirtDepositMaxAltitude3 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.dirtDepositMaxAltitude3 <= this.dirtDepositMinAltitude3 ? this.dirtDepositMinAltitude3 + 1 : this.dirtDepositMaxAltitude3);
        this.dirtDepositRarity4 = (this.dirtDepositRarity4 < 0 ? 0 : this.dirtDepositRarity4 > 100 ? 100 : this.dirtDepositRarity4);
        this.dirtDepositFrequency4 = (this.dirtDepositFrequency4 < 0 ? 0 : this.dirtDepositFrequency4);
        this.dirtDepositSize4 = (this.dirtDepositSize4 < 0 ? 0 : this.dirtDepositSize4);
        this.dirtDepositMinAltitude4 = (this.dirtDepositMinAltitude4 < 0 ? 0 : this.dirtDepositMinAltitude4 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.dirtDepositMinAltitude4);
        this.dirtDepositMaxAltitude4 = (this.dirtDepositMaxAltitude4 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.dirtDepositMaxAltitude4 <= this.dirtDepositMinAltitude4 ? this.dirtDepositMinAltitude4 + 1 : this.dirtDepositMaxAltitude4);
        this.gravelDepositRarity1 = (this.gravelDepositRarity1 < 0 ? 0 : this.gravelDepositRarity1 > 100 ? 100 : this.gravelDepositRarity1);
        this.gravelDepositFrequency1 = (this.gravelDepositFrequency1 < 0 ? 0 : this.gravelDepositFrequency1);
        this.gravelDepositSize1 = (this.gravelDepositSize1 < 0 ? 0 : this.gravelDepositSize1);
        this.gravelDepositMinAltitude1 = (this.gravelDepositMinAltitude1 < 0 ? 0 : this.gravelDepositMinAltitude1 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.gravelDepositMinAltitude1);
        this.gravelDepositMaxAltitude1 = (this.gravelDepositMaxAltitude1 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.gravelDepositMaxAltitude1 <= this.gravelDepositMinAltitude1 ? this.gravelDepositMinAltitude1 + 1 : this.gravelDepositMaxAltitude1);
        this.gravelDepositRarity2 = (this.gravelDepositRarity2 < 0 ? 0 : this.gravelDepositRarity2 > 100 ? 100 : this.gravelDepositRarity2);
        this.gravelDepositFrequency2 = (this.gravelDepositFrequency2 < 0 ? 0 : this.gravelDepositFrequency2);
        this.gravelDepositSize2 = (this.gravelDepositSize2 < 0 ? 0 : this.gravelDepositSize2);
        this.gravelDepositMinAltitude2 = (this.gravelDepositMinAltitude2 < 0 ? 0 : this.gravelDepositMinAltitude2 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.gravelDepositMinAltitude2);
        this.gravelDepositMaxAltitude2 = (this.gravelDepositMaxAltitude2 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.gravelDepositMaxAltitude2 <= this.gravelDepositMinAltitude2 ? this.gravelDepositMinAltitude2 + 1 : this.gravelDepositMaxAltitude2);
        this.gravelDepositRarity3 = (this.gravelDepositRarity3 < 0 ? 0 : this.gravelDepositRarity3 > 100 ? 100 : this.gravelDepositRarity3);
        this.gravelDepositFrequency3 = (this.gravelDepositFrequency3 < 0 ? 0 : this.gravelDepositFrequency3);
        this.gravelDepositSize3 = (this.gravelDepositSize3 < 0 ? 0 : this.gravelDepositSize3);
        this.gravelDepositMinAltitude3 = (this.gravelDepositMinAltitude3 < 0 ? 0 : this.gravelDepositMinAltitude3 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.gravelDepositMinAltitude3);
        this.gravelDepositMaxAltitude3 = (this.gravelDepositMaxAltitude3 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.gravelDepositMaxAltitude3 <= this.gravelDepositMinAltitude3 ? this.gravelDepositMinAltitude3 + 1 : this.gravelDepositMaxAltitude3);
        this.gravelDepositRarity4 = (this.gravelDepositRarity4 < 0 ? 0 : this.gravelDepositRarity4 > 100 ? 100 : this.gravelDepositRarity4);
        this.gravelDepositFrequency4 = (this.gravelDepositFrequency4 < 0 ? 0 : this.gravelDepositFrequency4);
        this.gravelDepositSize4 = (this.gravelDepositSize4 < 0 ? 0 : this.gravelDepositSize4);
        this.gravelDepositMinAltitude4 = (this.gravelDepositMinAltitude4 < 0 ? 0 : this.gravelDepositMinAltitude4 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.gravelDepositMinAltitude4);
        this.gravelDepositMaxAltitude4 = (this.gravelDepositMaxAltitude4 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.gravelDepositMaxAltitude4 <= this.gravelDepositMinAltitude4 ? this.gravelDepositMinAltitude4 + 1 : this.gravelDepositMaxAltitude4);
        this.clayDepositRarity1 = (this.clayDepositRarity1 < 0 ? 0 : this.clayDepositRarity1 > 100 ? 100 : this.clayDepositRarity1);
        this.clayDepositFrequency1 = (this.clayDepositFrequency1 < 0 ? 0 : this.clayDepositFrequency1);
        this.clayDepositSize1 = (this.clayDepositSize1 < 0 ? 0 : this.clayDepositSize1);
        this.clayDepositMinAltitude1 = (this.clayDepositMinAltitude1 < 0 ? 0 : this.clayDepositMinAltitude1 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.clayDepositMinAltitude1);
        this.clayDepositMaxAltitude1 = (this.clayDepositMaxAltitude1 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.clayDepositMaxAltitude1 <= this.clayDepositMinAltitude1 ? this.clayDepositMinAltitude1 + 1 : this.clayDepositMaxAltitude1);
        this.clayDepositRarity2 = (this.clayDepositRarity2 < 0 ? 0 : this.clayDepositRarity2 > 100 ? 100 : this.clayDepositRarity2);
        this.clayDepositFrequency2 = (this.clayDepositFrequency2 < 0 ? 0 : this.clayDepositFrequency2);
        this.clayDepositSize2 = (this.clayDepositSize2 < 0 ? 0 : this.clayDepositSize2);
        this.clayDepositMinAltitude2 = (this.clayDepositMinAltitude2 < 0 ? 0 : this.clayDepositMinAltitude2 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.clayDepositMinAltitude2);
        this.clayDepositMaxAltitude2 = (this.clayDepositMaxAltitude2 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.clayDepositMaxAltitude2 <= this.clayDepositMinAltitude2 ? this.clayDepositMinAltitude2 + 1 : this.clayDepositMaxAltitude2);
        this.clayDepositRarity3 = (this.clayDepositRarity3 < 0 ? 0 : this.clayDepositRarity3 > 100 ? 100 : this.clayDepositRarity3);
        this.clayDepositFrequency3 = (this.clayDepositFrequency3 < 0 ? 0 : this.clayDepositFrequency3);
        this.clayDepositSize3 = (this.clayDepositSize3 < 0 ? 0 : this.clayDepositSize3);
        this.clayDepositMinAltitude3 = (this.clayDepositMinAltitude3 < 0 ? 0 : this.clayDepositMinAltitude3 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.clayDepositMinAltitude3);
        this.clayDepositMaxAltitude3 = (this.clayDepositMaxAltitude3 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.clayDepositMaxAltitude3 <= this.clayDepositMinAltitude3 ? this.clayDepositMinAltitude3 + 1 : this.clayDepositMaxAltitude3);
        this.clayDepositRarity4 = (this.clayDepositRarity4 < 0 ? 0 : this.clayDepositRarity4 > 100 ? 100 : this.clayDepositRarity4);
        this.clayDepositFrequency4 = (this.clayDepositFrequency4 < 0 ? 0 : this.clayDepositFrequency4);
        this.clayDepositSize4 = (this.clayDepositSize4 < 0 ? 0 : this.clayDepositSize4);
        this.clayDepositMinAltitude4 = (this.clayDepositMinAltitude4 < 0 ? 0 : this.clayDepositMinAltitude4 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.clayDepositMinAltitude4);
        this.clayDepositMaxAltitude4 = (this.clayDepositMaxAltitude4 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.clayDepositMaxAltitude4 <= this.clayDepositMinAltitude4 ? this.clayDepositMinAltitude4 + 1 : this.clayDepositMaxAltitude4);
        this.coalDepositRarity1 = (this.coalDepositRarity1 < 0 ? 0 : this.coalDepositRarity1 > 100 ? 100 : this.coalDepositRarity1);
        this.coalDepositFrequency1 = (this.coalDepositFrequency1 < 0 ? 0 : this.coalDepositFrequency1);
        this.coalDepositSize1 = (this.coalDepositSize1 < 0 ? 0 : this.coalDepositSize1);
        this.coalDepositMinAltitude1 = (this.coalDepositMinAltitude1 < 0 ? 0 : this.coalDepositMinAltitude1 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.coalDepositMinAltitude1);
        this.coalDepositMaxAltitude1 = (this.coalDepositMaxAltitude1 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.coalDepositMaxAltitude1 <= this.coalDepositMinAltitude1 ? this.coalDepositMinAltitude1 + 1 : this.coalDepositMaxAltitude1);
        this.coalDepositRarity2 = (this.coalDepositRarity2 < 0 ? 0 : this.coalDepositRarity2 > 100 ? 100 : this.coalDepositRarity2);
        this.coalDepositFrequency2 = (this.coalDepositFrequency2 < 0 ? 0 : this.coalDepositFrequency2);
        this.coalDepositSize2 = (this.coalDepositSize2 < 0 ? 0 : this.coalDepositSize2);
        this.coalDepositMinAltitude2 = (this.coalDepositMinAltitude2 < 0 ? 0 : this.coalDepositMinAltitude2 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.coalDepositMinAltitude2);
        this.coalDepositMaxAltitude2 = (this.coalDepositMaxAltitude2 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.coalDepositMaxAltitude2 <= this.coalDepositMinAltitude2 ? this.coalDepositMinAltitude2 + 1 : this.coalDepositMaxAltitude2);
        this.coalDepositRarity3 = (this.coalDepositRarity3 < 0 ? 0 : this.coalDepositRarity3 > 100 ? 100 : this.coalDepositRarity3);
        this.coalDepositFrequency3 = (this.coalDepositFrequency3 < 0 ? 0 : this.coalDepositFrequency3);
        this.coalDepositSize3 = (this.coalDepositSize3 < 0 ? 0 : this.coalDepositSize3);
        this.coalDepositMinAltitude3 = (this.coalDepositMinAltitude3 < 0 ? 0 : this.coalDepositMinAltitude3 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.coalDepositMinAltitude3);
        this.coalDepositMaxAltitude3 = (this.coalDepositMaxAltitude3 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.coalDepositMaxAltitude3 <= this.coalDepositMinAltitude3 ? this.coalDepositMinAltitude3 + 1 : this.coalDepositMaxAltitude3);
        this.coalDepositRarity4 = (this.coalDepositRarity4 < 0 ? 0 : this.coalDepositRarity4 > 100 ? 100 : this.coalDepositRarity4);
        this.coalDepositFrequency4 = (this.coalDepositFrequency4 < 0 ? 0 : this.coalDepositFrequency4);
        this.coalDepositSize4 = (this.coalDepositSize4 < 0 ? 0 : this.coalDepositSize4);
        this.coalDepositMinAltitude4 = (this.coalDepositMinAltitude4 < 0 ? 0 : this.coalDepositMinAltitude4 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.coalDepositMinAltitude4);
        this.coalDepositMaxAltitude4 = (this.coalDepositMaxAltitude4 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.coalDepositMaxAltitude4 <= this.coalDepositMinAltitude4 ? this.coalDepositMinAltitude4 + 1 : this.coalDepositMaxAltitude4);
        this.ironDepositRarity1 = (this.ironDepositRarity1 < 0 ? 0 : this.ironDepositRarity1 > 100 ? 100 : this.ironDepositRarity1);
        this.ironDepositFrequency1 = (this.ironDepositFrequency1 < 0 ? 0 : this.ironDepositFrequency1);
        this.ironDepositSize1 = (this.ironDepositSize1 < 0 ? 0 : this.ironDepositSize1);
        this.ironDepositMinAltitude1 = (this.ironDepositMinAltitude1 < 0 ? 0 : this.ironDepositMinAltitude1 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.ironDepositMinAltitude1);
        this.ironDepositMaxAltitude1 = (this.ironDepositMaxAltitude1 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.ironDepositMaxAltitude1 <= this.ironDepositMinAltitude1 ? this.ironDepositMinAltitude1 + 1 : this.ironDepositMaxAltitude1);
        this.ironDepositRarity2 = (this.ironDepositRarity2 < 0 ? 0 : this.ironDepositRarity2 > 100 ? 100 : this.ironDepositRarity2);
        this.ironDepositFrequency2 = (this.ironDepositFrequency2 < 0 ? 0 : this.ironDepositFrequency2);
        this.ironDepositSize2 = (this.ironDepositSize2 < 0 ? 0 : this.ironDepositSize2);
        this.ironDepositMinAltitude2 = (this.ironDepositMinAltitude2 < 0 ? 0 : this.ironDepositMinAltitude2 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.ironDepositMinAltitude2);
        this.ironDepositMaxAltitude2 = (this.ironDepositMaxAltitude2 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.ironDepositMaxAltitude2 <= this.ironDepositMinAltitude2 ? this.ironDepositMinAltitude2 + 1 : this.ironDepositMaxAltitude2);
        this.ironDepositRarity3 = (this.ironDepositRarity3 < 0 ? 0 : this.ironDepositRarity3 > 100 ? 100 : this.ironDepositRarity3);
        this.ironDepositFrequency3 = (this.ironDepositFrequency3 < 0 ? 0 : this.ironDepositFrequency3);
        this.ironDepositSize3 = (this.ironDepositSize3 < 0 ? 0 : this.ironDepositSize3);
        this.ironDepositMinAltitude3 = (this.ironDepositMinAltitude3 < 0 ? 0 : this.ironDepositMinAltitude3 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.ironDepositMinAltitude3);
        this.ironDepositMaxAltitude3 = (this.ironDepositMaxAltitude3 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.ironDepositMaxAltitude3 <= this.ironDepositMinAltitude3 ? this.ironDepositMinAltitude3 + 1 : this.ironDepositMaxAltitude3);
        this.ironDepositRarity4 = (this.ironDepositRarity4 < 0 ? 0 : this.ironDepositRarity4 > 100 ? 100 : this.ironDepositRarity4);
        this.ironDepositFrequency4 = (this.ironDepositFrequency4 < 0 ? 0 : this.ironDepositFrequency4);
        this.ironDepositSize4 = (this.ironDepositSize4 < 0 ? 0 : this.ironDepositSize4);
        this.ironDepositMinAltitude4 = (this.ironDepositMinAltitude4 < 0 ? 0 : this.ironDepositMinAltitude4 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.ironDepositMinAltitude4);
        this.ironDepositMaxAltitude4 = (this.ironDepositMaxAltitude4 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.ironDepositMaxAltitude4 <= this.ironDepositMinAltitude4 ? this.ironDepositMinAltitude4 + 1 : this.ironDepositMaxAltitude4);
        this.goldDepositRarity1 = (this.goldDepositRarity1 < 0 ? 0 : this.goldDepositRarity1 > 100 ? 100 : this.goldDepositRarity1);
        this.goldDepositFrequency1 = (this.goldDepositFrequency1 < 0 ? 0 : this.goldDepositFrequency1);
        this.goldDepositSize1 = (this.goldDepositSize1 < 0 ? 0 : this.goldDepositSize1);
        this.goldDepositMinAltitude1 = (this.goldDepositMinAltitude1 < 0 ? 0 : this.goldDepositMinAltitude1 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.goldDepositMinAltitude1);
        this.goldDepositMaxAltitude1 = (this.goldDepositMaxAltitude1 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.goldDepositMaxAltitude1 <= this.goldDepositMinAltitude1 ? this.goldDepositMinAltitude1 + 1 : this.goldDepositMaxAltitude1);
        this.goldDepositRarity2 = (this.goldDepositRarity2 < 0 ? 0 : this.goldDepositRarity2 > 100 ? 100 : this.goldDepositRarity2);
        this.goldDepositFrequency2 = (this.goldDepositFrequency2 < 0 ? 0 : this.goldDepositFrequency2);
        this.goldDepositSize2 = (this.goldDepositSize2 < 0 ? 0 : this.goldDepositSize2);
        this.goldDepositMinAltitude2 = (this.goldDepositMinAltitude2 < 0 ? 0 : this.goldDepositMinAltitude2 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.goldDepositMinAltitude2);
        this.goldDepositMaxAltitude2 = (this.goldDepositMaxAltitude2 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.goldDepositMaxAltitude2 <= this.goldDepositMinAltitude2 ? this.goldDepositMinAltitude2 + 1 : this.goldDepositMaxAltitude2);
        this.goldDepositRarity3 = (this.goldDepositRarity3 < 0 ? 0 : this.goldDepositRarity3 > 100 ? 100 : this.goldDepositRarity3);
        this.goldDepositFrequency3 = (this.goldDepositFrequency3 < 0 ? 0 : this.goldDepositFrequency3);
        this.goldDepositSize3 = (this.goldDepositSize3 < 0 ? 0 : this.goldDepositSize3);
        this.goldDepositMinAltitude3 = (this.goldDepositMinAltitude3 < 0 ? 0 : this.goldDepositMinAltitude3 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.goldDepositMinAltitude3);
        this.goldDepositMaxAltitude3 = (this.goldDepositMaxAltitude3 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.goldDepositMaxAltitude3 <= this.goldDepositMinAltitude3 ? this.goldDepositMinAltitude3 + 1 : this.goldDepositMaxAltitude3);
        this.goldDepositRarity4 = (this.goldDepositRarity4 < 0 ? 0 : this.goldDepositRarity4 > 100 ? 100 : this.goldDepositRarity4);
        this.goldDepositFrequency4 = (this.goldDepositFrequency4 < 0 ? 0 : this.goldDepositFrequency4);
        this.goldDepositSize4 = (this.goldDepositSize4 < 0 ? 0 : this.goldDepositSize4);
        this.goldDepositMinAltitude4 = (this.goldDepositMinAltitude4 < 0 ? 0 : this.goldDepositMinAltitude4 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.goldDepositMinAltitude4);
        this.goldDepositMaxAltitude4 = (this.goldDepositMaxAltitude4 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.goldDepositMaxAltitude4 <= this.goldDepositMinAltitude4 ? this.goldDepositMinAltitude4 + 1 : this.goldDepositMaxAltitude4);
        this.redstoneDepositRarity1 = (this.redstoneDepositRarity1 < 0 ? 0 : this.redstoneDepositRarity1 > 100 ? 100 : this.redstoneDepositRarity1);
        this.redstoneDepositFrequency1 = (this.redstoneDepositFrequency1 < 0 ? 0 : this.redstoneDepositFrequency1);
        this.redstoneDepositSize1 = (this.redstoneDepositSize1 < 0 ? 0 : this.redstoneDepositSize1);
        this.redstoneDepositMinAltitude1 = (this.redstoneDepositMinAltitude1 < 0 ? 0 : this.redstoneDepositMinAltitude1 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.redstoneDepositMinAltitude1);
        this.redstoneDepositMaxAltitude1 = (this.redstoneDepositMaxAltitude1 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.redstoneDepositMaxAltitude1 <= this.redstoneDepositMinAltitude1 ? this.redstoneDepositMinAltitude1 + 1 : this.redstoneDepositMaxAltitude1);
        this.redstoneDepositRarity2 = (this.redstoneDepositRarity2 < 0 ? 0 : this.redstoneDepositRarity2 > 100 ? 100 : this.redstoneDepositRarity2);
        this.redstoneDepositFrequency2 = (this.redstoneDepositFrequency2 < 0 ? 0 : this.redstoneDepositFrequency2);
        this.redstoneDepositSize2 = (this.redstoneDepositSize2 < 0 ? 0 : this.redstoneDepositSize2);
        this.redstoneDepositMinAltitude2 = (this.redstoneDepositMinAltitude2 < 0 ? 0 : this.redstoneDepositMinAltitude2 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.redstoneDepositMinAltitude2);
        this.redstoneDepositMaxAltitude2 = (this.redstoneDepositMaxAltitude2 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.redstoneDepositMaxAltitude2 <= this.redstoneDepositMinAltitude2 ? this.redstoneDepositMinAltitude2 + 1 : this.redstoneDepositMaxAltitude2);
        this.redstoneDepositRarity3 = (this.redstoneDepositRarity3 < 0 ? 0 : this.redstoneDepositRarity3 > 100 ? 100 : this.redstoneDepositRarity3);
        this.redstoneDepositFrequency3 = (this.redstoneDepositFrequency3 < 0 ? 0 : this.redstoneDepositFrequency3);
        this.redstoneDepositSize3 = (this.redstoneDepositSize3 < 0 ? 0 : this.redstoneDepositSize3);
        this.redstoneDepositMinAltitude3 = (this.redstoneDepositMinAltitude3 < 0 ? 0 : this.redstoneDepositMinAltitude3 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.redstoneDepositMinAltitude3);
        this.redstoneDepositMaxAltitude3 = (this.redstoneDepositMaxAltitude3 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.redstoneDepositMaxAltitude3 <= this.redstoneDepositMinAltitude3 ? this.redstoneDepositMinAltitude3 + 1 : this.redstoneDepositMaxAltitude3);
        this.redstoneDepositRarity4 = (this.redstoneDepositRarity4 < 0 ? 0 : this.redstoneDepositRarity4 > 100 ? 100 : this.redstoneDepositRarity4);
        this.redstoneDepositFrequency4 = (this.redstoneDepositFrequency4 < 0 ? 0 : this.redstoneDepositFrequency4);
        this.redstoneDepositSize4 = (this.redstoneDepositSize4 < 0 ? 0 : this.redstoneDepositSize4);
        this.redstoneDepositMinAltitude4 = (this.redstoneDepositMinAltitude4 < 0 ? 0 : this.redstoneDepositMinAltitude4 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.redstoneDepositMinAltitude4);
        this.redstoneDepositMaxAltitude4 = (this.redstoneDepositMaxAltitude4 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.redstoneDepositMaxAltitude4 <= this.redstoneDepositMinAltitude4 ? this.redstoneDepositMinAltitude4 + 1 : this.redstoneDepositMaxAltitude4);
        this.diamondDepositRarity1 = (this.diamondDepositRarity1 < 0 ? 0 : this.diamondDepositRarity1 > 100 ? 100 : this.diamondDepositRarity1);
        this.diamondDepositFrequency1 = (this.diamondDepositFrequency1 < 0 ? 0 : this.diamondDepositFrequency1);
        this.diamondDepositSize1 = (this.diamondDepositSize1 < 0 ? 0 : this.diamondDepositSize1);
        this.diamondDepositMinAltitude1 = (this.diamondDepositMinAltitude1 < 0 ? 0 : this.diamondDepositMinAltitude1 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.diamondDepositMinAltitude1);
        this.diamondDepositMaxAltitude1 = (this.diamondDepositMaxAltitude1 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.diamondDepositMaxAltitude1 <= this.diamondDepositMinAltitude1 ? this.diamondDepositMinAltitude1 + 1 : this.diamondDepositMaxAltitude1);
        this.diamondDepositRarity2 = (this.diamondDepositRarity2 < 0 ? 0 : this.diamondDepositRarity2 > 100 ? 100 : this.diamondDepositRarity2);
        this.diamondDepositFrequency2 = (this.diamondDepositFrequency2 < 0 ? 0 : this.diamondDepositFrequency2);
        this.diamondDepositSize2 = (this.diamondDepositSize2 < 0 ? 0 : this.diamondDepositSize2);
        this.diamondDepositMinAltitude2 = (this.diamondDepositMinAltitude2 < 0 ? 0 : this.diamondDepositMinAltitude2 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.diamondDepositMinAltitude2);
        this.diamondDepositMaxAltitude2 = (this.diamondDepositMaxAltitude2 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.diamondDepositMaxAltitude2 <= this.diamondDepositMinAltitude2 ? this.diamondDepositMinAltitude2 + 1 : this.diamondDepositMaxAltitude2);
        this.diamondDepositRarity3 = (this.diamondDepositRarity3 < 0 ? 0 : this.diamondDepositRarity3 > 100 ? 100 : this.diamondDepositRarity3);
        this.diamondDepositFrequency3 = (this.diamondDepositFrequency3 < 0 ? 0 : this.diamondDepositFrequency3);
        this.diamondDepositSize3 = (this.diamondDepositSize3 < 0 ? 0 : this.diamondDepositSize3);
        this.diamondDepositMinAltitude3 = (this.diamondDepositMinAltitude3 < 0 ? 0 : this.diamondDepositMinAltitude3 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.diamondDepositMinAltitude3);
        this.diamondDepositMaxAltitude3 = (this.diamondDepositMaxAltitude3 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.diamondDepositMaxAltitude3 <= this.diamondDepositMinAltitude3 ? this.diamondDepositMinAltitude3 + 1 : this.diamondDepositMaxAltitude3);
        this.diamondDepositRarity4 = (this.diamondDepositRarity4 < 0 ? 0 : this.diamondDepositRarity4 > 100 ? 100 : this.diamondDepositRarity4);
        this.diamondDepositFrequency4 = (this.diamondDepositFrequency4 < 0 ? 0 : this.diamondDepositFrequency4);
        this.diamondDepositSize4 = (this.diamondDepositSize4 < 0 ? 0 : this.diamondDepositSize4);
        this.diamondDepositMinAltitude4 = (this.diamondDepositMinAltitude4 < 0 ? 0 : this.diamondDepositMinAltitude4 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.diamondDepositMinAltitude4);
        this.diamondDepositMaxAltitude4 = (this.diamondDepositMaxAltitude4 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.diamondDepositMaxAltitude4 <= this.diamondDepositMinAltitude4 ? this.diamondDepositMinAltitude4 + 1 : this.diamondDepositMaxAltitude4);
        this.lapislazuliDepositRarity1 = (this.lapislazuliDepositRarity1 < 0 ? 0 : this.lapislazuliDepositRarity1 > 100 ? 100 : this.lapislazuliDepositRarity1);
        this.lapislazuliDepositFrequency1 = (this.lapislazuliDepositFrequency1 < 0 ? 0 : this.lapislazuliDepositFrequency1);
        this.lapislazuliDepositSize1 = (this.lapislazuliDepositSize1 < 0 ? 0 : this.lapislazuliDepositSize1);
        this.lapislazuliDepositMinAltitude1 = (this.lapislazuliDepositMinAltitude1 < 0 ? 0 : this.lapislazuliDepositMinAltitude1 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.lapislazuliDepositMinAltitude1);
        this.lapislazuliDepositMaxAltitude1 = (this.lapislazuliDepositMaxAltitude1 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.lapislazuliDepositMaxAltitude1 <= this.lapislazuliDepositMinAltitude1 ? this.lapislazuliDepositMinAltitude1 + 1 : this.lapislazuliDepositMaxAltitude1);
        this.lapislazuliDepositRarity2 = (this.lapislazuliDepositRarity2 < 0 ? 0 : this.lapislazuliDepositRarity2 > 100 ? 100 : this.lapislazuliDepositRarity2);
        this.lapislazuliDepositFrequency2 = (this.lapislazuliDepositFrequency2 < 0 ? 0 : this.lapislazuliDepositFrequency2);
        this.lapislazuliDepositSize2 = (this.lapislazuliDepositSize2 < 0 ? 0 : this.lapislazuliDepositSize2);
        this.lapislazuliDepositMinAltitude2 = (this.lapislazuliDepositMinAltitude2 < 0 ? 0 : this.lapislazuliDepositMinAltitude2 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.lapislazuliDepositMinAltitude2);
        this.lapislazuliDepositMaxAltitude2 = (this.lapislazuliDepositMaxAltitude2 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.lapislazuliDepositMaxAltitude2 <= this.lapislazuliDepositMinAltitude2 ? this.lapislazuliDepositMinAltitude2 + 1 : this.lapislazuliDepositMaxAltitude2);
        this.lapislazuliDepositRarity3 = (this.lapislazuliDepositRarity3 < 0 ? 0 : this.lapislazuliDepositRarity3 > 100 ? 100 : this.lapislazuliDepositRarity3);
        this.lapislazuliDepositFrequency3 = (this.lapislazuliDepositFrequency3 < 0 ? 0 : this.lapislazuliDepositFrequency3);
        this.lapislazuliDepositSize3 = (this.lapislazuliDepositSize3 < 0 ? 0 : this.lapislazuliDepositSize3);
        this.lapislazuliDepositMinAltitude3 = (this.lapislazuliDepositMinAltitude3 < 0 ? 0 : this.lapislazuliDepositMinAltitude3 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.lapislazuliDepositMinAltitude3);
        this.lapislazuliDepositMaxAltitude3 = (this.lapislazuliDepositMaxAltitude3 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.lapislazuliDepositMaxAltitude3 <= this.lapislazuliDepositMinAltitude3 ? this.lapislazuliDepositMinAltitude3 + 1 : this.lapislazuliDepositMaxAltitude3);
        this.lapislazuliDepositRarity4 = (this.lapislazuliDepositRarity4 < 0 ? 0 : this.lapislazuliDepositRarity4 > 100 ? 100 : this.lapislazuliDepositRarity4);
        this.lapislazuliDepositFrequency4 = (this.lapislazuliDepositFrequency4 < 0 ? 0 : this.lapislazuliDepositFrequency4);
        this.lapislazuliDepositSize4 = (this.lapislazuliDepositSize4 < 0 ? 0 : this.lapislazuliDepositSize4);
        this.lapislazuliDepositMinAltitude4 = (this.lapislazuliDepositMinAltitude4 < 0 ? 0 : this.lapislazuliDepositMinAltitude4 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.lapislazuliDepositMinAltitude4);
        this.lapislazuliDepositMaxAltitude4 = (this.lapislazuliDepositMaxAltitude4 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.lapislazuliDepositMaxAltitude4 <= this.lapislazuliDepositMinAltitude4 ? this.lapislazuliDepositMinAltitude4 + 1 : this.lapislazuliDepositMaxAltitude4);

        this.dungeonRarity = (this.dungeonRarity < 0 ? 0 : this.dungeonRarity > 100 ? 100 : this.dungeonRarity);
        this.dungeonFrequency = (this.dungeonFrequency < 0 ? 0 : this.dungeonFrequency);
        this.dungeonMinAltitude = (this.dungeonMinAltitude < 0 ? 0 : this.dungeonMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.dungeonMinAltitude);
        this.dungeonMaxAltitude = (this.dungeonMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.dungeonMaxAltitude <= this.dungeonMinAltitude ? this.dungeonMinAltitude + 1 : this.dungeonMaxAltitude);


        this.lavaLevelMin = (this.lavaLevelMin < 0 ? 0 : this.lavaLevelMin > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.lavaLevelMin);
        this.lavaLevelMax = (this.lavaLevelMax > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.lavaLevelMax < this.lavaLevelMin ? this.lavaLevelMin : this.lavaLevelMax);

        this.undergroundLakeRarity = (this.undergroundLakeRarity < 0 ? 0 : this.undergroundLakeRarity > 100 ? 100 : this.undergroundLakeRarity);
        this.undergroundLakeFrequency = (this.undergroundLakeFrequency < 0 ? 0 : this.undergroundLakeFrequency);
        this.undergroundLakeMinSize = (this.undergroundLakeMinSize < 25 ? 25 : this.undergroundLakeMinSize);
        this.undergroundLakeMaxSize = (this.undergroundLakeMaxSize <= this.undergroundLakeMinSize ? this.undergroundLakeMinSize + 1 : this.undergroundLakeMaxSize);
        this.undergroundLakeMinAltitude = (this.undergroundLakeMinAltitude < 0 ? 0 : this.undergroundLakeMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.undergroundLakeMinAltitude);
        this.undergroundLakeMaxAltitude = (this.undergroundLakeMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.undergroundLakeMaxAltitude <= this.undergroundLakeMinAltitude ? this.undergroundLakeMinAltitude + 1 : this.undergroundLakeMaxAltitude);


    }

    void ReadSettings()
    {
        BufferedReader SettingsReader = null;

        // f = new File(this.worldSaveFolder, BiomeTerrainValues.biomeTerrainSettingsName.stringValue());
        File f = new File(SettingsDir, BiomeTerrainValues.biomeTerrainSettingsName.stringValue());
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


        this.biomeSize = ReadModSettins(BiomeTerrainValues.biomeSize.name(), BiomeTerrainValues.biomeSize.doubleValue());
        this.minMoisture = ReadModSettins(BiomeTerrainValues.minMoisture.name(), BiomeTerrainValues.minMoisture.doubleValue());
        this.maxMoisture = ReadModSettins(BiomeTerrainValues.maxMoisture.name(), BiomeTerrainValues.maxMoisture.doubleValue());
        this.minTemperature = ReadModSettins(BiomeTerrainValues.minTemperature.name(), BiomeTerrainValues.minTemperature.doubleValue());
        this.maxTemperature = ReadModSettins(BiomeTerrainValues.maxTemperature.name(), BiomeTerrainValues.maxTemperature.doubleValue());
        this.snowThreshold = ReadModSettins(BiomeTerrainValues.snowThreshold.name(), BiomeTerrainValues.snowThreshold.doubleValue());
        this.iceThreshold = ReadModSettins(BiomeTerrainValues.iceThreshold.name(), BiomeTerrainValues.iceThreshold.doubleValue());

        this.muddySwamps = ReadModSettins(BiomeTerrainValues.muddySwamps.name(), BiomeTerrainValues.muddySwamps.booleanValue());
        this.claySwamps = ReadModSettins(BiomeTerrainValues.claySwamps.name(), BiomeTerrainValues.claySwamps.booleanValue());
        this.swampSize = ReadModSettins(BiomeTerrainValues.swampSize.name(), BiomeTerrainValues.swampSize.intValue());

        this.waterlessDeserts = ReadModSettins(BiomeTerrainValues.waterlessDeserts.name(), BiomeTerrainValues.waterlessDeserts.booleanValue());
        this.removeSurfaceDirtFromDesert = ReadModSettins(BiomeTerrainValues.removeSurfaceDirtFromDesert.name(), BiomeTerrainValues.removeSurfaceDirtFromDesert.booleanValue());
        this.desertDirt = ReadModSettins(BiomeTerrainValues.desertDirt.name(), BiomeTerrainValues.desertDirt.booleanValue());
        this.desertDirtFrequency = ReadModSettins(BiomeTerrainValues.desertDirtFrequency.name(), BiomeTerrainValues.desertDirtFrequency.intValue());

        this.caveRarity = ReadModSettins(BiomeTerrainValues.caveRarity.name(), BiomeTerrainValues.caveRarity.intValue());
        this.caveFrequency = ReadModSettins(BiomeTerrainValues.caveFrequency.name(), BiomeTerrainValues.caveFrequency.intValue());
        this.caveMinAltitude = ReadModSettins(BiomeTerrainValues.caveMinAltitude.name(), BiomeTerrainValues.caveMinAltitude.intValue());
        this.caveMaxAltitude = ReadModSettins(BiomeTerrainValues.caveMaxAltitude.name(), BiomeTerrainValues.caveMaxAltitude.intValue());
        this.individualCaveRarity = ReadModSettins(BiomeTerrainValues.individualCaveRarity.name(), BiomeTerrainValues.individualCaveRarity.intValue());
        this.caveSystemFrequency = ReadModSettins(BiomeTerrainValues.caveSystemFrequency.name(), BiomeTerrainValues.caveSystemFrequency.intValue());
        this.caveSystemPocketChance = ReadModSettins(BiomeTerrainValues.caveSystemPocketChance.name(), BiomeTerrainValues.caveSystemPocketChance.intValue());
        this.caveSystemPocketMinSize = ReadModSettins(BiomeTerrainValues.caveSystemPocketMinSize.name(), BiomeTerrainValues.caveSystemPocketMinSize.intValue());
        this.caveSystemPocketMaxSize = ReadModSettins(BiomeTerrainValues.caveSystemPocketMaxSize.name(), BiomeTerrainValues.caveSystemPocketMaxSize.intValue());
        this.evenCaveDistribution = ReadModSettins(BiomeTerrainValues.evenCaveDistribution.name(), BiomeTerrainValues.evenCaveDistribution.booleanValue());


        this.waterLevel = ReadModSettins(BiomeTerrainValues.waterLevel.name(), BiomeTerrainValues.waterLevel.intValue());
        this.maxAverageHeight = ReadModSettins(BiomeTerrainValues.maxAverageHeight.name(), BiomeTerrainValues.maxAverageHeight.doubleValue());
        this.maxAverageDepth = ReadModSettins(BiomeTerrainValues.maxAverageDepth.name(), BiomeTerrainValues.maxAverageDepth.doubleValue());
        this.fractureHorizontal = ReadModSettins(BiomeTerrainValues.fractureHorizontal.name(), BiomeTerrainValues.fractureHorizontal.doubleValue());
        this.fractureVertical = ReadModSettins(BiomeTerrainValues.fractureVertical.name(), BiomeTerrainValues.fractureVertical.doubleValue());
        this.volatility1 = ReadModSettins(BiomeTerrainValues.volatility1.name(), BiomeTerrainValues.volatility1.doubleValue());
        this.volatility2 = ReadModSettins(BiomeTerrainValues.volatility2.name(), BiomeTerrainValues.volatility2.doubleValue());
        this.volatilityWeight1 = ReadModSettins(BiomeTerrainValues.volatilityWeight1.name(), BiomeTerrainValues.volatilityWeight1.doubleValue());
        this.volatilityWeight2 = ReadModSettins(BiomeTerrainValues.volatilityWeight2.name(), BiomeTerrainValues.volatilityWeight2.doubleValue());

        this.disableBedrock = ReadModSettins(BiomeTerrainValues.disableBedrock.name(), BiomeTerrainValues.disableBedrock.booleanValue());
        this.flatBedrock = ReadModSettins(BiomeTerrainValues.flatBedrock.name(), BiomeTerrainValues.flatBedrock.booleanValue());
        this.bedrockobsidian = ReadModSettins(BiomeTerrainValues.bedrockobsidian.name(), BiomeTerrainValues.bedrockobsidian.booleanValue());


        this.removeSurfaceStone = ReadModSettins(BiomeTerrainValues.removeSurfaceStone.name(), BiomeTerrainValues.removeSurfaceStone.booleanValue());

        this.flowerDepositRarity = this.ReadModSettins(BiomeTerrainValues.flowerDepositRarity.name(), BiomeTerrainValues.flowerDepositRarity.intValue());
        this.flowerDepositFrequency = this.ReadModSettins(BiomeTerrainValues.flowerDepositFrequency.name(), BiomeTerrainValues.flowerDepositFrequency.intValue());
        this.flowerDepositMinAltitude = this.ReadModSettins(BiomeTerrainValues.flowerDepositMinAltitude.name(), BiomeTerrainValues.flowerDepositMinAltitude.intValue());
        this.flowerDepositMaxAltitude = this.ReadModSettins(BiomeTerrainValues.flowerDepositMaxAltitude.name(), BiomeTerrainValues.flowerDepositMaxAltitude.intValue());
        this.roseDepositRarity = this.ReadModSettins(BiomeTerrainValues.roseDepositRarity.name(), BiomeTerrainValues.roseDepositRarity.intValue());
        this.roseDepositFrequency = this.ReadModSettins(BiomeTerrainValues.roseDepositFrequency.name(), BiomeTerrainValues.roseDepositFrequency.intValue());
        this.roseDepositMinAltitude = this.ReadModSettins(BiomeTerrainValues.roseDepositMinAltitude.name(), BiomeTerrainValues.roseDepositMinAltitude.intValue());
        this.roseDepositMaxAltitude = this.ReadModSettins(BiomeTerrainValues.roseDepositMaxAltitude.name(), BiomeTerrainValues.roseDepositMaxAltitude.intValue());
        this.brownMushroomDepositRarity = this.ReadModSettins(BiomeTerrainValues.brownMushroomDepositRarity.name(), BiomeTerrainValues.brownMushroomDepositRarity.intValue());
        this.brownMushroomDepositFrequency = this.ReadModSettins(BiomeTerrainValues.brownMushroomDepositFrequency.name(), BiomeTerrainValues.brownMushroomDepositFrequency.intValue());
        this.brownMushroomDepositMinAltitude = this.ReadModSettins(BiomeTerrainValues.brownMushroomDepositMinAltitude.name(), BiomeTerrainValues.brownMushroomDepositMinAltitude.intValue());
        this.brownMushroomDepositMaxAltitude = this.ReadModSettins(BiomeTerrainValues.brownMushroomDepositMaxAltitude.name(), BiomeTerrainValues.brownMushroomDepositMaxAltitude.intValue());
        this.redMushroomDepositRarity = this.ReadModSettins(BiomeTerrainValues.redMushroomDepositRarity.name(), BiomeTerrainValues.redMushroomDepositRarity.intValue());
        this.redMushroomDepositFrequency = this.ReadModSettins(BiomeTerrainValues.redMushroomDepositFrequency.name(), BiomeTerrainValues.redMushroomDepositFrequency.intValue());
        this.redMushroomDepositMinAltitude = this.ReadModSettins(BiomeTerrainValues.redMushroomDepositMinAltitude.name(), BiomeTerrainValues.redMushroomDepositMinAltitude.intValue());
        this.redMushroomDepositMaxAltitude = this.ReadModSettins(BiomeTerrainValues.redMushroomDepositMaxAltitude.name(), BiomeTerrainValues.redMushroomDepositMaxAltitude.intValue());
        this.reedDepositRarity = this.ReadModSettins(BiomeTerrainValues.reedDepositRarity.name(), BiomeTerrainValues.reedDepositRarity.intValue());
        this.reedDepositFrequency = this.ReadModSettins(BiomeTerrainValues.reedDepositFrequency.name(), BiomeTerrainValues.reedDepositFrequency.intValue());
        this.reedDepositMinAltitude = this.ReadModSettins(BiomeTerrainValues.reedDepositMinAltitude.name(), BiomeTerrainValues.reedDepositMinAltitude.intValue());
        this.reedDepositMaxAltitude = this.ReadModSettins(BiomeTerrainValues.reedDepositMaxAltitude.name(), BiomeTerrainValues.reedDepositMaxAltitude.intValue());
        this.pumpkinDepositRarity = this.ReadModSettins(BiomeTerrainValues.pumpkinDepositRarity.name(), BiomeTerrainValues.pumpkinDepositRarity.intValue());
        this.pumpkinDepositFrequency = this.ReadModSettins(BiomeTerrainValues.pumpkinDepositFrequency.name(), BiomeTerrainValues.pumpkinDepositFrequency.intValue());
        this.pumpkinDepositMinAltitude = this.ReadModSettins(BiomeTerrainValues.pumpkinDepositMinAltitude.name(), BiomeTerrainValues.pumpkinDepositMinAltitude.intValue());
        this.pumpkinDepositMaxAltitude = this.ReadModSettins(BiomeTerrainValues.pumpkinDepositMaxAltitude.name(), BiomeTerrainValues.pumpkinDepositMaxAltitude.intValue());

        this.evenWaterSourceDistribution = this.ReadModSettins(BiomeTerrainValues.evenWaterSourceDistribution.name(), BiomeTerrainValues.evenWaterSourceDistribution.booleanValue());

        this.waterSourceDepositRarity = this.ReadModSettins(BiomeTerrainValues.waterSourceDepositRarity.name(), BiomeTerrainValues.waterSourceDepositRarity.intValue());
        this.waterSourceDepositFrequency = this.ReadModSettins(BiomeTerrainValues.waterSourceDepositFrequency.name(), BiomeTerrainValues.waterSourceDepositFrequency.intValue());
        this.waterSourceDepositMinAltitude = this.ReadModSettins(BiomeTerrainValues.waterSourceDepositMinAltitude.name(), BiomeTerrainValues.waterSourceDepositMinAltitude.intValue());
        this.waterSourceDepositMaxAltitude = this.ReadModSettins(BiomeTerrainValues.waterSourceDepositMaxAltitude.name(), BiomeTerrainValues.waterSourceDepositMaxAltitude.intValue());

        this.evenWaterSourceDistribution = this.ReadModSettins(BiomeTerrainValues.evenWaterSourceDistribution.name(), BiomeTerrainValues.evenWaterSourceDistribution.booleanValue());
        this.evenLavaSourceDistribution = this.ReadModSettins(BiomeTerrainValues.evenLavaSourceDistribution.name(), BiomeTerrainValues.evenLavaSourceDistribution.booleanValue());

        this.lavaSourceDepositRarity = this.ReadModSettins(BiomeTerrainValues.lavaSourceDepositRarity.name(), BiomeTerrainValues.lavaSourceDepositRarity.intValue());
        this.lavaSourceDepositFrequency = this.ReadModSettins(BiomeTerrainValues.lavaSourceDepositFrequency.name(), BiomeTerrainValues.lavaSourceDepositFrequency.intValue());
        this.lavaSourceDepositMinAltitude = this.ReadModSettins(BiomeTerrainValues.lavaSourceDepositMinAltitude.name(), BiomeTerrainValues.lavaSourceDepositMinAltitude.intValue());
        this.lavaSourceDepositMaxAltitude = this.ReadModSettins(BiomeTerrainValues.lavaSourceDepositMaxAltitude.name(), BiomeTerrainValues.lavaSourceDepositMaxAltitude.intValue());

        this.dirtDepositRarity1 = this.ReadModSettins(BiomeTerrainValues.dirtDepositRarity1.name(), BiomeTerrainValues.dirtDepositRarity1.intValue());
        this.dirtDepositFrequency1 = this.ReadModSettins(BiomeTerrainValues.dirtDepositFrequency1.name(), BiomeTerrainValues.dirtDepositFrequency1.intValue());
        this.dirtDepositSize1 = this.ReadModSettins(BiomeTerrainValues.dirtDepositSize1.name(), BiomeTerrainValues.dirtDepositSize1.intValue());
        this.dirtDepositMinAltitude1 = this.ReadModSettins(BiomeTerrainValues.dirtDepositMinAltitude1.name(), BiomeTerrainValues.dirtDepositMinAltitude1.intValue());
        this.dirtDepositMaxAltitude1 = this.ReadModSettins(BiomeTerrainValues.dirtDepositMaxAltitude1.name(), BiomeTerrainValues.dirtDepositMaxAltitude1.intValue());
        this.dirtDepositRarity2 = this.ReadModSettins(BiomeTerrainValues.dirtDepositRarity2.name(), BiomeTerrainValues.dirtDepositRarity2.intValue());
        this.dirtDepositFrequency2 = this.ReadModSettins(BiomeTerrainValues.dirtDepositFrequency2.name(), BiomeTerrainValues.dirtDepositFrequency2.intValue());
        this.dirtDepositSize2 = this.ReadModSettins(BiomeTerrainValues.dirtDepositSize2.name(), BiomeTerrainValues.dirtDepositSize2.intValue());
        this.dirtDepositMinAltitude2 = this.ReadModSettins(BiomeTerrainValues.dirtDepositMinAltitude2.name(), BiomeTerrainValues.dirtDepositMinAltitude2.intValue());
        this.dirtDepositMaxAltitude2 = this.ReadModSettins(BiomeTerrainValues.dirtDepositMaxAltitude2.name(), BiomeTerrainValues.dirtDepositMaxAltitude2.intValue());
        this.dirtDepositRarity3 = this.ReadModSettins(BiomeTerrainValues.dirtDepositRarity3.name(), BiomeTerrainValues.dirtDepositRarity3.intValue());
        this.dirtDepositFrequency3 = this.ReadModSettins(BiomeTerrainValues.dirtDepositFrequency3.name(), BiomeTerrainValues.dirtDepositFrequency3.intValue());
        this.dirtDepositSize3 = this.ReadModSettins(BiomeTerrainValues.dirtDepositSize3.name(), BiomeTerrainValues.dirtDepositSize3.intValue());
        this.dirtDepositMinAltitude3 = this.ReadModSettins(BiomeTerrainValues.dirtDepositMinAltitude3.name(), BiomeTerrainValues.dirtDepositMinAltitude3.intValue());
        this.dirtDepositMaxAltitude3 = this.ReadModSettins(BiomeTerrainValues.dirtDepositMaxAltitude3.name(), BiomeTerrainValues.dirtDepositMaxAltitude3.intValue());
        this.dirtDepositRarity4 = this.ReadModSettins(BiomeTerrainValues.dirtDepositRarity4.name(), BiomeTerrainValues.dirtDepositRarity4.intValue());
        this.dirtDepositFrequency4 = this.ReadModSettins(BiomeTerrainValues.dirtDepositFrequency4.name(), BiomeTerrainValues.dirtDepositFrequency4.intValue());
        this.dirtDepositSize4 = this.ReadModSettins(BiomeTerrainValues.dirtDepositSize4.name(), BiomeTerrainValues.dirtDepositSize4.intValue());
        this.dirtDepositMinAltitude4 = this.ReadModSettins(BiomeTerrainValues.dirtDepositMinAltitude4.name(), BiomeTerrainValues.dirtDepositMinAltitude4.intValue());
        this.dirtDepositMaxAltitude4 = this.ReadModSettins(BiomeTerrainValues.dirtDepositMaxAltitude4.name(), BiomeTerrainValues.dirtDepositMaxAltitude4.intValue());
        this.gravelDepositRarity1 = this.ReadModSettins(BiomeTerrainValues.gravelDepositRarity1.name(), BiomeTerrainValues.gravelDepositRarity1.intValue());
        this.gravelDepositFrequency1 = this.ReadModSettins(BiomeTerrainValues.gravelDepositFrequency1.name(), BiomeTerrainValues.gravelDepositFrequency1.intValue());
        this.gravelDepositSize1 = this.ReadModSettins(BiomeTerrainValues.gravelDepositSize1.name(), BiomeTerrainValues.gravelDepositSize1.intValue());
        this.gravelDepositMinAltitude1 = this.ReadModSettins(BiomeTerrainValues.gravelDepositMinAltitude1.name(), BiomeTerrainValues.gravelDepositMinAltitude1.intValue());
        this.gravelDepositMaxAltitude1 = this.ReadModSettins(BiomeTerrainValues.gravelDepositMaxAltitude1.name(), BiomeTerrainValues.gravelDepositMaxAltitude1.intValue());
        this.gravelDepositRarity2 = this.ReadModSettins(BiomeTerrainValues.gravelDepositRarity2.name(), BiomeTerrainValues.gravelDepositRarity2.intValue());
        this.gravelDepositFrequency2 = this.ReadModSettins(BiomeTerrainValues.gravelDepositFrequency2.name(), BiomeTerrainValues.gravelDepositFrequency2.intValue());
        this.gravelDepositSize2 = this.ReadModSettins(BiomeTerrainValues.gravelDepositSize2.name(), BiomeTerrainValues.gravelDepositSize2.intValue());
        this.gravelDepositMinAltitude2 = this.ReadModSettins(BiomeTerrainValues.gravelDepositMinAltitude2.name(), BiomeTerrainValues.gravelDepositMinAltitude2.intValue());
        this.gravelDepositMaxAltitude2 = this.ReadModSettins(BiomeTerrainValues.gravelDepositMaxAltitude2.name(), BiomeTerrainValues.gravelDepositMaxAltitude2.intValue());
        this.gravelDepositRarity3 = this.ReadModSettins(BiomeTerrainValues.gravelDepositRarity3.name(), BiomeTerrainValues.gravelDepositRarity3.intValue());
        this.gravelDepositFrequency3 = this.ReadModSettins(BiomeTerrainValues.gravelDepositFrequency3.name(), BiomeTerrainValues.gravelDepositFrequency3.intValue());
        this.gravelDepositSize3 = this.ReadModSettins(BiomeTerrainValues.gravelDepositSize3.name(), BiomeTerrainValues.gravelDepositSize3.intValue());
        this.gravelDepositMinAltitude3 = this.ReadModSettins(BiomeTerrainValues.gravelDepositMinAltitude3.name(), BiomeTerrainValues.gravelDepositMinAltitude3.intValue());
        this.gravelDepositMaxAltitude3 = this.ReadModSettins(BiomeTerrainValues.gravelDepositMaxAltitude3.name(), BiomeTerrainValues.gravelDepositMaxAltitude3.intValue());
        this.gravelDepositRarity4 = this.ReadModSettins(BiomeTerrainValues.gravelDepositRarity4.name(), BiomeTerrainValues.gravelDepositRarity4.intValue());
        this.gravelDepositFrequency4 = this.ReadModSettins(BiomeTerrainValues.gravelDepositFrequency4.name(), BiomeTerrainValues.gravelDepositFrequency4.intValue());
        this.gravelDepositSize4 = this.ReadModSettins(BiomeTerrainValues.gravelDepositSize4.name(), BiomeTerrainValues.gravelDepositSize4.intValue());
        this.gravelDepositMinAltitude4 = this.ReadModSettins(BiomeTerrainValues.gravelDepositMinAltitude4.name(), BiomeTerrainValues.gravelDepositMinAltitude4.intValue());
        this.gravelDepositMaxAltitude4 = this.ReadModSettins(BiomeTerrainValues.gravelDepositMaxAltitude4.name(), BiomeTerrainValues.gravelDepositMaxAltitude4.intValue());
        this.clayDepositRarity1 = this.ReadModSettins(BiomeTerrainValues.clayDepositRarity1.name(), BiomeTerrainValues.clayDepositRarity1.intValue());
        this.clayDepositFrequency1 = this.ReadModSettins(BiomeTerrainValues.clayDepositFrequency1.name(), BiomeTerrainValues.clayDepositFrequency1.intValue());
        this.clayDepositSize1 = this.ReadModSettins(BiomeTerrainValues.clayDepositSize1.name(), BiomeTerrainValues.clayDepositSize1.intValue());
        this.clayDepositMinAltitude1 = this.ReadModSettins(BiomeTerrainValues.clayDepositMinAltitude1.name(), BiomeTerrainValues.clayDepositMinAltitude1.intValue());
        this.clayDepositMaxAltitude1 = this.ReadModSettins(BiomeTerrainValues.clayDepositMaxAltitude1.name(), BiomeTerrainValues.clayDepositMaxAltitude1.intValue());
        this.clayDepositRarity2 = this.ReadModSettins(BiomeTerrainValues.clayDepositRarity2.name(), BiomeTerrainValues.clayDepositRarity2.intValue());
        this.clayDepositFrequency2 = this.ReadModSettins(BiomeTerrainValues.clayDepositFrequency2.name(), BiomeTerrainValues.clayDepositFrequency2.intValue());
        this.clayDepositSize2 = this.ReadModSettins(BiomeTerrainValues.clayDepositSize2.name(), BiomeTerrainValues.clayDepositSize2.intValue());
        this.clayDepositMinAltitude2 = this.ReadModSettins(BiomeTerrainValues.clayDepositMinAltitude2.name(), BiomeTerrainValues.clayDepositMinAltitude2.intValue());
        this.clayDepositMaxAltitude2 = this.ReadModSettins(BiomeTerrainValues.clayDepositMaxAltitude2.name(), BiomeTerrainValues.clayDepositMaxAltitude2.intValue());
        this.clayDepositRarity3 = this.ReadModSettins(BiomeTerrainValues.clayDepositRarity3.name(), BiomeTerrainValues.clayDepositRarity3.intValue());
        this.clayDepositFrequency3 = this.ReadModSettins(BiomeTerrainValues.clayDepositFrequency3.name(), BiomeTerrainValues.clayDepositFrequency3.intValue());
        this.clayDepositSize3 = this.ReadModSettins(BiomeTerrainValues.clayDepositSize3.name(), BiomeTerrainValues.clayDepositSize3.intValue());
        this.clayDepositMinAltitude3 = this.ReadModSettins(BiomeTerrainValues.clayDepositMinAltitude3.name(), BiomeTerrainValues.clayDepositMinAltitude3.intValue());
        this.clayDepositMaxAltitude3 = this.ReadModSettins(BiomeTerrainValues.clayDepositMaxAltitude3.name(), BiomeTerrainValues.clayDepositMaxAltitude3.intValue());
        this.clayDepositRarity4 = this.ReadModSettins(BiomeTerrainValues.clayDepositRarity4.name(), BiomeTerrainValues.clayDepositRarity4.intValue());
        this.clayDepositFrequency4 = this.ReadModSettins(BiomeTerrainValues.clayDepositFrequency4.name(), BiomeTerrainValues.clayDepositFrequency4.intValue());
        this.clayDepositSize4 = this.ReadModSettins(BiomeTerrainValues.clayDepositSize4.name(), BiomeTerrainValues.clayDepositSize4.intValue());
        this.clayDepositMinAltitude4 = this.ReadModSettins(BiomeTerrainValues.clayDepositMinAltitude4.name(), BiomeTerrainValues.clayDepositMinAltitude4.intValue());
        this.clayDepositMaxAltitude4 = this.ReadModSettins(BiomeTerrainValues.clayDepositMaxAltitude4.name(), BiomeTerrainValues.clayDepositMaxAltitude4.intValue());
        this.coalDepositRarity1 = this.ReadModSettins(BiomeTerrainValues.coalDepositRarity1.name(), BiomeTerrainValues.coalDepositRarity1.intValue());
        this.coalDepositFrequency1 = this.ReadModSettins(BiomeTerrainValues.coalDepositFrequency1.name(), BiomeTerrainValues.coalDepositFrequency1.intValue());
        this.coalDepositSize1 = this.ReadModSettins(BiomeTerrainValues.coalDepositSize1.name(), BiomeTerrainValues.coalDepositSize1.intValue());
        this.coalDepositMinAltitude1 = this.ReadModSettins(BiomeTerrainValues.coalDepositMinAltitude1.name(), BiomeTerrainValues.coalDepositMinAltitude1.intValue());
        this.coalDepositMaxAltitude1 = this.ReadModSettins(BiomeTerrainValues.coalDepositMaxAltitude1.name(), BiomeTerrainValues.coalDepositMaxAltitude1.intValue());
        this.coalDepositRarity2 = this.ReadModSettins(BiomeTerrainValues.coalDepositRarity2.name(), BiomeTerrainValues.coalDepositRarity2.intValue());
        this.coalDepositFrequency2 = this.ReadModSettins(BiomeTerrainValues.coalDepositFrequency2.name(), BiomeTerrainValues.coalDepositFrequency2.intValue());
        this.coalDepositSize2 = this.ReadModSettins(BiomeTerrainValues.coalDepositSize2.name(), BiomeTerrainValues.coalDepositSize2.intValue());
        this.coalDepositMinAltitude2 = this.ReadModSettins(BiomeTerrainValues.coalDepositMinAltitude2.name(), BiomeTerrainValues.coalDepositMinAltitude2.intValue());
        this.coalDepositMaxAltitude2 = this.ReadModSettins(BiomeTerrainValues.coalDepositMaxAltitude2.name(), BiomeTerrainValues.coalDepositMaxAltitude2.intValue());
        this.coalDepositRarity3 = this.ReadModSettins(BiomeTerrainValues.coalDepositRarity3.name(), BiomeTerrainValues.coalDepositRarity3.intValue());
        this.coalDepositFrequency3 = this.ReadModSettins(BiomeTerrainValues.coalDepositFrequency3.name(), BiomeTerrainValues.coalDepositFrequency3.intValue());
        this.coalDepositSize3 = this.ReadModSettins(BiomeTerrainValues.coalDepositSize3.name(), BiomeTerrainValues.coalDepositSize3.intValue());
        this.coalDepositMinAltitude3 = this.ReadModSettins(BiomeTerrainValues.coalDepositMinAltitude3.name(), BiomeTerrainValues.coalDepositMinAltitude3.intValue());
        this.coalDepositMaxAltitude3 = this.ReadModSettins(BiomeTerrainValues.coalDepositMaxAltitude3.name(), BiomeTerrainValues.coalDepositMaxAltitude3.intValue());
        this.coalDepositRarity4 = this.ReadModSettins(BiomeTerrainValues.coalDepositRarity4.name(), BiomeTerrainValues.coalDepositRarity4.intValue());
        this.coalDepositFrequency4 = this.ReadModSettins(BiomeTerrainValues.coalDepositFrequency4.name(), BiomeTerrainValues.coalDepositFrequency4.intValue());
        this.coalDepositSize4 = this.ReadModSettins(BiomeTerrainValues.coalDepositSize4.name(), BiomeTerrainValues.coalDepositSize4.intValue());
        this.coalDepositMinAltitude4 = this.ReadModSettins(BiomeTerrainValues.coalDepositMinAltitude4.name(), BiomeTerrainValues.coalDepositMinAltitude4.intValue());
        this.coalDepositMaxAltitude4 = this.ReadModSettins(BiomeTerrainValues.coalDepositMaxAltitude4.name(), BiomeTerrainValues.coalDepositMaxAltitude4.intValue());
        this.ironDepositRarity1 = this.ReadModSettins(BiomeTerrainValues.ironDepositRarity1.name(), BiomeTerrainValues.ironDepositRarity1.intValue());
        this.ironDepositFrequency1 = this.ReadModSettins(BiomeTerrainValues.ironDepositFrequency1.name(), BiomeTerrainValues.ironDepositFrequency1.intValue());
        this.ironDepositSize1 = this.ReadModSettins(BiomeTerrainValues.ironDepositSize1.name(), BiomeTerrainValues.ironDepositSize1.intValue());
        this.ironDepositMinAltitude1 = this.ReadModSettins(BiomeTerrainValues.ironDepositMinAltitude1.name(), BiomeTerrainValues.ironDepositMinAltitude1.intValue());
        this.ironDepositMaxAltitude1 = this.ReadModSettins(BiomeTerrainValues.ironDepositMaxAltitude1.name(), BiomeTerrainValues.ironDepositMaxAltitude1.intValue());
        this.ironDepositRarity2 = this.ReadModSettins(BiomeTerrainValues.ironDepositRarity2.name(), BiomeTerrainValues.ironDepositRarity2.intValue());
        this.ironDepositFrequency2 = this.ReadModSettins(BiomeTerrainValues.ironDepositFrequency2.name(), BiomeTerrainValues.ironDepositFrequency2.intValue());
        this.ironDepositSize2 = this.ReadModSettins(BiomeTerrainValues.ironDepositSize2.name(), BiomeTerrainValues.ironDepositSize2.intValue());
        this.ironDepositMinAltitude2 = this.ReadModSettins(BiomeTerrainValues.ironDepositMinAltitude2.name(), BiomeTerrainValues.ironDepositMinAltitude2.intValue());
        this.ironDepositMaxAltitude2 = this.ReadModSettins(BiomeTerrainValues.ironDepositMaxAltitude2.name(), BiomeTerrainValues.ironDepositMaxAltitude2.intValue());
        this.ironDepositRarity3 = this.ReadModSettins(BiomeTerrainValues.ironDepositRarity3.name(), BiomeTerrainValues.ironDepositRarity3.intValue());
        this.ironDepositFrequency3 = this.ReadModSettins(BiomeTerrainValues.ironDepositFrequency3.name(), BiomeTerrainValues.ironDepositFrequency3.intValue());
        this.ironDepositSize3 = this.ReadModSettins(BiomeTerrainValues.ironDepositSize3.name(), BiomeTerrainValues.ironDepositSize3.intValue());
        this.ironDepositMinAltitude3 = this.ReadModSettins(BiomeTerrainValues.ironDepositMinAltitude3.name(), BiomeTerrainValues.ironDepositMinAltitude3.intValue());
        this.ironDepositMaxAltitude3 = this.ReadModSettins(BiomeTerrainValues.ironDepositMaxAltitude3.name(), BiomeTerrainValues.ironDepositMaxAltitude3.intValue());
        this.ironDepositRarity4 = this.ReadModSettins(BiomeTerrainValues.ironDepositRarity4.name(), BiomeTerrainValues.ironDepositRarity4.intValue());
        this.ironDepositFrequency4 = this.ReadModSettins(BiomeTerrainValues.ironDepositFrequency4.name(), BiomeTerrainValues.ironDepositFrequency4.intValue());
        this.ironDepositSize4 = this.ReadModSettins(BiomeTerrainValues.ironDepositSize4.name(), BiomeTerrainValues.ironDepositSize4.intValue());
        this.ironDepositMinAltitude4 = this.ReadModSettins(BiomeTerrainValues.ironDepositMinAltitude4.name(), BiomeTerrainValues.ironDepositMinAltitude4.intValue());
        this.ironDepositMaxAltitude4 = this.ReadModSettins(BiomeTerrainValues.ironDepositMaxAltitude4.name(), BiomeTerrainValues.ironDepositMaxAltitude4.intValue());
        this.goldDepositRarity1 = this.ReadModSettins(BiomeTerrainValues.goldDepositRarity1.name(), BiomeTerrainValues.goldDepositRarity1.intValue());
        this.goldDepositFrequency1 = this.ReadModSettins(BiomeTerrainValues.goldDepositFrequency1.name(), BiomeTerrainValues.goldDepositFrequency1.intValue());
        this.goldDepositSize1 = this.ReadModSettins(BiomeTerrainValues.goldDepositSize1.name(), BiomeTerrainValues.goldDepositSize1.intValue());
        this.goldDepositMinAltitude1 = this.ReadModSettins(BiomeTerrainValues.goldDepositMinAltitude1.name(), BiomeTerrainValues.goldDepositMinAltitude1.intValue());
        this.goldDepositMaxAltitude1 = this.ReadModSettins(BiomeTerrainValues.goldDepositMaxAltitude1.name(), BiomeTerrainValues.goldDepositMaxAltitude1.intValue());
        this.goldDepositRarity2 = this.ReadModSettins(BiomeTerrainValues.goldDepositRarity2.name(), BiomeTerrainValues.goldDepositRarity2.intValue());
        this.goldDepositFrequency2 = this.ReadModSettins(BiomeTerrainValues.goldDepositFrequency2.name(), BiomeTerrainValues.goldDepositFrequency2.intValue());
        this.goldDepositSize2 = this.ReadModSettins(BiomeTerrainValues.goldDepositSize2.name(), BiomeTerrainValues.goldDepositSize2.intValue());
        this.goldDepositMinAltitude2 = this.ReadModSettins(BiomeTerrainValues.goldDepositMinAltitude2.name(), BiomeTerrainValues.goldDepositMinAltitude2.intValue());
        this.goldDepositMaxAltitude2 = this.ReadModSettins(BiomeTerrainValues.goldDepositMaxAltitude2.name(), BiomeTerrainValues.goldDepositMaxAltitude2.intValue());
        this.goldDepositRarity3 = this.ReadModSettins(BiomeTerrainValues.goldDepositRarity3.name(), BiomeTerrainValues.goldDepositRarity3.intValue());
        this.goldDepositFrequency3 = this.ReadModSettins(BiomeTerrainValues.goldDepositFrequency3.name(), BiomeTerrainValues.goldDepositFrequency3.intValue());
        this.goldDepositSize3 = this.ReadModSettins(BiomeTerrainValues.goldDepositSize3.name(), BiomeTerrainValues.goldDepositSize3.intValue());
        this.goldDepositMinAltitude3 = this.ReadModSettins(BiomeTerrainValues.goldDepositMinAltitude3.name(), BiomeTerrainValues.goldDepositMinAltitude3.intValue());
        this.goldDepositMaxAltitude3 = this.ReadModSettins(BiomeTerrainValues.goldDepositMaxAltitude3.name(), BiomeTerrainValues.goldDepositMaxAltitude3.intValue());
        this.goldDepositRarity4 = this.ReadModSettins(BiomeTerrainValues.goldDepositRarity4.name(), BiomeTerrainValues.goldDepositRarity4.intValue());
        this.goldDepositFrequency4 = this.ReadModSettins(BiomeTerrainValues.goldDepositFrequency4.name(), BiomeTerrainValues.goldDepositFrequency4.intValue());
        this.goldDepositSize4 = this.ReadModSettins(BiomeTerrainValues.goldDepositSize4.name(), BiomeTerrainValues.goldDepositSize4.intValue());
        this.goldDepositMinAltitude4 = this.ReadModSettins(BiomeTerrainValues.goldDepositMinAltitude4.name(), BiomeTerrainValues.goldDepositMinAltitude4.intValue());
        this.goldDepositMaxAltitude4 = this.ReadModSettins(BiomeTerrainValues.goldDepositMaxAltitude4.name(), BiomeTerrainValues.goldDepositMaxAltitude4.intValue());
        this.redstoneDepositRarity1 = this.ReadModSettins(BiomeTerrainValues.redstoneDepositRarity1.name(), BiomeTerrainValues.redstoneDepositRarity1.intValue());
        this.redstoneDepositFrequency1 = this.ReadModSettins(BiomeTerrainValues.redstoneDepositFrequency1.name(), BiomeTerrainValues.redstoneDepositFrequency1.intValue());
        this.redstoneDepositSize1 = this.ReadModSettins(BiomeTerrainValues.redstoneDepositSize1.name(), BiomeTerrainValues.redstoneDepositSize1.intValue());
        this.redstoneDepositMinAltitude1 = this.ReadModSettins(BiomeTerrainValues.redstoneDepositMinAltitude1.name(), BiomeTerrainValues.redstoneDepositMinAltitude1.intValue());
        this.redstoneDepositMaxAltitude1 = this.ReadModSettins(BiomeTerrainValues.redstoneDepositMaxAltitude1.name(), BiomeTerrainValues.redstoneDepositMaxAltitude1.intValue());
        this.redstoneDepositRarity2 = this.ReadModSettins(BiomeTerrainValues.redstoneDepositRarity2.name(), BiomeTerrainValues.redstoneDepositRarity2.intValue());
        this.redstoneDepositFrequency2 = this.ReadModSettins(BiomeTerrainValues.redstoneDepositFrequency2.name(), BiomeTerrainValues.redstoneDepositFrequency2.intValue());
        this.redstoneDepositSize2 = this.ReadModSettins(BiomeTerrainValues.redstoneDepositSize2.name(), BiomeTerrainValues.redstoneDepositSize2.intValue());
        this.redstoneDepositMinAltitude2 = this.ReadModSettins(BiomeTerrainValues.redstoneDepositMinAltitude2.name(), BiomeTerrainValues.redstoneDepositMinAltitude2.intValue());
        this.redstoneDepositMaxAltitude2 = this.ReadModSettins(BiomeTerrainValues.redstoneDepositMaxAltitude2.name(), BiomeTerrainValues.redstoneDepositMaxAltitude2.intValue());
        this.redstoneDepositRarity3 = this.ReadModSettins(BiomeTerrainValues.redstoneDepositRarity3.name(), BiomeTerrainValues.redstoneDepositRarity3.intValue());
        this.redstoneDepositFrequency3 = this.ReadModSettins(BiomeTerrainValues.redstoneDepositFrequency3.name(), BiomeTerrainValues.redstoneDepositFrequency3.intValue());
        this.redstoneDepositSize3 = this.ReadModSettins(BiomeTerrainValues.redstoneDepositSize3.name(), BiomeTerrainValues.redstoneDepositSize3.intValue());
        this.redstoneDepositMinAltitude3 = this.ReadModSettins(BiomeTerrainValues.redstoneDepositMinAltitude3.name(), BiomeTerrainValues.redstoneDepositMinAltitude3.intValue());
        this.redstoneDepositMaxAltitude3 = this.ReadModSettins(BiomeTerrainValues.redstoneDepositMaxAltitude3.name(), BiomeTerrainValues.redstoneDepositMaxAltitude3.intValue());
        this.redstoneDepositRarity4 = this.ReadModSettins(BiomeTerrainValues.redstoneDepositRarity4.name(), BiomeTerrainValues.redstoneDepositRarity4.intValue());
        this.redstoneDepositFrequency4 = this.ReadModSettins(BiomeTerrainValues.redstoneDepositFrequency4.name(), BiomeTerrainValues.redstoneDepositFrequency4.intValue());
        this.redstoneDepositSize4 = this.ReadModSettins(BiomeTerrainValues.redstoneDepositSize4.name(), BiomeTerrainValues.redstoneDepositSize4.intValue());
        this.redstoneDepositMinAltitude4 = this.ReadModSettins(BiomeTerrainValues.redstoneDepositMinAltitude4.name(), BiomeTerrainValues.redstoneDepositMinAltitude4.intValue());
        this.redstoneDepositMaxAltitude4 = this.ReadModSettins(BiomeTerrainValues.redstoneDepositMaxAltitude4.name(), BiomeTerrainValues.redstoneDepositMaxAltitude4.intValue());
        this.diamondDepositRarity1 = this.ReadModSettins(BiomeTerrainValues.diamondDepositRarity1.name(), BiomeTerrainValues.diamondDepositRarity1.intValue());
        this.diamondDepositFrequency1 = this.ReadModSettins(BiomeTerrainValues.diamondDepositFrequency1.name(), BiomeTerrainValues.diamondDepositFrequency1.intValue());
        this.diamondDepositSize1 = this.ReadModSettins(BiomeTerrainValues.diamondDepositSize1.name(), BiomeTerrainValues.diamondDepositSize1.intValue());
        this.diamondDepositMinAltitude1 = this.ReadModSettins(BiomeTerrainValues.diamondDepositMinAltitude1.name(), BiomeTerrainValues.diamondDepositMinAltitude1.intValue());
        this.diamondDepositMaxAltitude1 = this.ReadModSettins(BiomeTerrainValues.diamondDepositMaxAltitude1.name(), BiomeTerrainValues.diamondDepositMaxAltitude1.intValue());
        this.diamondDepositRarity2 = this.ReadModSettins(BiomeTerrainValues.diamondDepositRarity2.name(), BiomeTerrainValues.diamondDepositRarity2.intValue());
        this.diamondDepositFrequency2 = this.ReadModSettins(BiomeTerrainValues.diamondDepositFrequency2.name(), BiomeTerrainValues.diamondDepositFrequency2.intValue());
        this.diamondDepositSize2 = this.ReadModSettins(BiomeTerrainValues.diamondDepositSize2.name(), BiomeTerrainValues.diamondDepositSize2.intValue());
        this.diamondDepositMinAltitude2 = this.ReadModSettins(BiomeTerrainValues.diamondDepositMinAltitude2.name(), BiomeTerrainValues.diamondDepositMinAltitude2.intValue());
        this.diamondDepositMaxAltitude2 = this.ReadModSettins(BiomeTerrainValues.diamondDepositMaxAltitude2.name(), BiomeTerrainValues.diamondDepositMaxAltitude2.intValue());
        this.diamondDepositRarity3 = this.ReadModSettins(BiomeTerrainValues.diamondDepositRarity3.name(), BiomeTerrainValues.diamondDepositRarity3.intValue());
        this.diamondDepositFrequency3 = this.ReadModSettins(BiomeTerrainValues.diamondDepositFrequency3.name(), BiomeTerrainValues.diamondDepositFrequency3.intValue());
        this.diamondDepositSize3 = this.ReadModSettins(BiomeTerrainValues.diamondDepositSize3.name(), BiomeTerrainValues.diamondDepositSize3.intValue());
        this.diamondDepositMinAltitude3 = this.ReadModSettins(BiomeTerrainValues.diamondDepositMinAltitude3.name(), BiomeTerrainValues.diamondDepositMinAltitude3.intValue());
        this.diamondDepositMaxAltitude3 = this.ReadModSettins(BiomeTerrainValues.diamondDepositMaxAltitude3.name(), BiomeTerrainValues.diamondDepositMaxAltitude3.intValue());
        this.diamondDepositRarity4 = this.ReadModSettins(BiomeTerrainValues.diamondDepositRarity4.name(), BiomeTerrainValues.diamondDepositRarity4.intValue());
        this.diamondDepositFrequency4 = this.ReadModSettins(BiomeTerrainValues.diamondDepositFrequency4.name(), BiomeTerrainValues.diamondDepositFrequency4.intValue());
        this.diamondDepositSize4 = this.ReadModSettins(BiomeTerrainValues.diamondDepositSize4.name(), BiomeTerrainValues.diamondDepositSize4.intValue());
        this.diamondDepositMinAltitude4 = this.ReadModSettins(BiomeTerrainValues.diamondDepositMinAltitude4.name(), BiomeTerrainValues.diamondDepositMinAltitude4.intValue());
        this.diamondDepositMaxAltitude4 = this.ReadModSettins(BiomeTerrainValues.diamondDepositMaxAltitude4.name(), BiomeTerrainValues.diamondDepositMaxAltitude4.intValue());
        this.lapislazuliDepositRarity1 = this.ReadModSettins(BiomeTerrainValues.lapislazuliDepositRarity1.name(), BiomeTerrainValues.lapislazuliDepositRarity1.intValue());
        this.lapislazuliDepositFrequency1 = this.ReadModSettins(BiomeTerrainValues.lapislazuliDepositFrequency1.name(), BiomeTerrainValues.lapislazuliDepositFrequency1.intValue());
        this.lapislazuliDepositSize1 = this.ReadModSettins(BiomeTerrainValues.lapislazuliDepositSize1.name(), BiomeTerrainValues.lapislazuliDepositSize1.intValue());
        this.lapislazuliDepositMinAltitude1 = this.ReadModSettins(BiomeTerrainValues.lapislazuliDepositMinAltitude1.name(), BiomeTerrainValues.lapislazuliDepositMinAltitude1.intValue());
        this.lapislazuliDepositMaxAltitude1 = this.ReadModSettins(BiomeTerrainValues.lapislazuliDepositMaxAltitude1.name(), BiomeTerrainValues.lapislazuliDepositMaxAltitude1.intValue());
        this.lapislazuliDepositRarity2 = this.ReadModSettins(BiomeTerrainValues.lapislazuliDepositRarity2.name(), BiomeTerrainValues.lapislazuliDepositRarity2.intValue());
        this.lapislazuliDepositFrequency2 = this.ReadModSettins(BiomeTerrainValues.lapislazuliDepositFrequency2.name(), BiomeTerrainValues.lapislazuliDepositFrequency2.intValue());
        this.lapislazuliDepositSize2 = this.ReadModSettins(BiomeTerrainValues.lapislazuliDepositSize2.name(), BiomeTerrainValues.lapislazuliDepositSize2.intValue());
        this.lapislazuliDepositMinAltitude2 = this.ReadModSettins(BiomeTerrainValues.lapislazuliDepositMinAltitude2.name(), BiomeTerrainValues.lapislazuliDepositMinAltitude2.intValue());
        this.lapislazuliDepositMaxAltitude2 = this.ReadModSettins(BiomeTerrainValues.lapislazuliDepositMaxAltitude2.name(), BiomeTerrainValues.lapislazuliDepositMaxAltitude2.intValue());
        this.lapislazuliDepositRarity3 = this.ReadModSettins(BiomeTerrainValues.lapislazuliDepositRarity3.name(), BiomeTerrainValues.lapislazuliDepositRarity3.intValue());
        this.lapislazuliDepositFrequency3 = this.ReadModSettins(BiomeTerrainValues.lapislazuliDepositFrequency3.name(), BiomeTerrainValues.lapislazuliDepositFrequency3.intValue());
        this.lapislazuliDepositSize3 = this.ReadModSettins(BiomeTerrainValues.lapislazuliDepositSize3.name(), BiomeTerrainValues.lapislazuliDepositSize3.intValue());
        this.lapislazuliDepositMinAltitude3 = this.ReadModSettins(BiomeTerrainValues.lapislazuliDepositMinAltitude3.name(), BiomeTerrainValues.lapislazuliDepositMinAltitude3.intValue());
        this.lapislazuliDepositMaxAltitude3 = this.ReadModSettins(BiomeTerrainValues.lapislazuliDepositMaxAltitude3.name(), BiomeTerrainValues.lapislazuliDepositMaxAltitude3.intValue());
        this.lapislazuliDepositRarity4 = this.ReadModSettins(BiomeTerrainValues.lapislazuliDepositRarity4.name(), BiomeTerrainValues.lapislazuliDepositRarity4.intValue());
        this.lapislazuliDepositFrequency4 = this.ReadModSettins(BiomeTerrainValues.lapislazuliDepositFrequency4.name(), BiomeTerrainValues.lapislazuliDepositFrequency4.intValue());
        this.lapislazuliDepositSize4 = this.ReadModSettins(BiomeTerrainValues.lapislazuliDepositSize4.name(), BiomeTerrainValues.lapislazuliDepositSize4.intValue());
        this.lapislazuliDepositMinAltitude4 = this.ReadModSettins(BiomeTerrainValues.lapislazuliDepositMinAltitude4.name(), BiomeTerrainValues.lapislazuliDepositMinAltitude4.intValue());
        this.lapislazuliDepositMaxAltitude4 = this.ReadModSettins(BiomeTerrainValues.lapislazuliDepositMaxAltitude4.name(), BiomeTerrainValues.lapislazuliDepositMaxAltitude4.intValue());


        this.disableNotchPonds = this.ReadModSettins(BiomeTerrainValues.disableNotchPonds.name(), BiomeTerrainValues.disableNotchPonds.booleanValue());

        this.customObjects = this.ReadModSettins(BiomeTerrainValues.customObjects.name(), BiomeTerrainValues.customObjects.booleanValue());
        this.objectSpawnRatio = this.ReadModSettins(BiomeTerrainValues.objectSpawnRatio.name(), BiomeTerrainValues.objectSpawnRatio.intValue());
        this.notchBiomeTrees = this.ReadModSettins(BiomeTerrainValues.notchBiomeTrees.name(), BiomeTerrainValues.notchBiomeTrees.booleanValue());
        this.denyObjectsUnderFill = this.ReadModSettins(BiomeTerrainValues.denyObjectsUnderFill.name(), BiomeTerrainValues.denyObjectsUnderFill.booleanValue());
        this.globalTreeDensity = this.ReadModSettins(BiomeTerrainValues.globalTreeDensity.name(), BiomeTerrainValues.globalTreeDensity.intValue());
        this.rainforestTreeDensity = this.ReadModSettins(BiomeTerrainValues.rainforestTreeDensity.name(), BiomeTerrainValues.rainforestTreeDensity.intValue());
        this.swamplandTreeDensity = this.ReadModSettins(BiomeTerrainValues.swamplandTreeDensity.name(), BiomeTerrainValues.swamplandTreeDensity.intValue());
        this.seasonalforestTreeDensity = this.ReadModSettins(BiomeTerrainValues.seasonalforestTreeDensity.name(), BiomeTerrainValues.seasonalforestTreeDensity.intValue());
        this.forestTreeDensity = this.ReadModSettins(BiomeTerrainValues.forestTreeDensity.name(), BiomeTerrainValues.forestTreeDensity.intValue());
        this.savannaTreeDensity = this.ReadModSettins(BiomeTerrainValues.savannaTreeDensity.name(), BiomeTerrainValues.savannaTreeDensity.intValue());
        this.shrublandTreeDensity = this.ReadModSettins(BiomeTerrainValues.shrublandTreeDensity.name(), BiomeTerrainValues.shrublandTreeDensity.intValue());
        this.taigaTreeDensity = this.ReadModSettins(BiomeTerrainValues.taigaTreeDensity.name(), BiomeTerrainValues.taigaTreeDensity.intValue());
        this.desertTreeDensity = this.ReadModSettins(BiomeTerrainValues.desertTreeDensity.name(), BiomeTerrainValues.desertTreeDensity.intValue());
        this.plainsTreeDensity = this.ReadModSettins(BiomeTerrainValues.plainsTreeDensity.name(), BiomeTerrainValues.plainsTreeDensity.intValue());
        this.iceDesertTreeDensity = this.ReadModSettins(BiomeTerrainValues.iceDesertTreeDensity.name(), BiomeTerrainValues.iceDesertTreeDensity.intValue());
        this.tundraTreeDensity = this.ReadModSettins(BiomeTerrainValues.tundraTreeDensity.name(), BiomeTerrainValues.tundraTreeDensity.intValue());
        this.globalCactusDensity = this.ReadModSettins(BiomeTerrainValues.globalCactusDensity.name(), BiomeTerrainValues.globalCactusDensity.intValue());
        this.desertCactusDensity = this.ReadModSettins(BiomeTerrainValues.desertCactusDensity.name(), BiomeTerrainValues.desertCactusDensity.intValue());
        this.cactusDepositRarity = this.ReadModSettins(BiomeTerrainValues.cactusDepositRarity.name(), BiomeTerrainValues.cactusDepositRarity.intValue());
        this.cactusDepositMinAltitude = this.ReadModSettins(BiomeTerrainValues.cactusDepositMinAltitude.name(), BiomeTerrainValues.cactusDepositMinAltitude.intValue());
        this.cactusDepositMaxAltitude = this.ReadModSettins(BiomeTerrainValues.cactusDepositMaxAltitude.name(), BiomeTerrainValues.cactusDepositMaxAltitude.intValue());

        this.dungeonRarity = this.ReadModSettins(BiomeTerrainValues.dungeonRarity.name(), BiomeTerrainValues.dungeonRarity.intValue());
        this.dungeonFrequency = this.ReadModSettins(BiomeTerrainValues.dungeonFrequency.name(), BiomeTerrainValues.dungeonFrequency.intValue());
        this.dungeonMinAltitude = this.ReadModSettins(BiomeTerrainValues.dungeonMinAltitude.name(), BiomeTerrainValues.dungeonMinAltitude.intValue());
        this.dungeonMaxAltitude = this.ReadModSettins(BiomeTerrainValues.dungeonMaxAltitude.name(), BiomeTerrainValues.dungeonMaxAltitude.intValue());


        this.lavaLevelMin = this.ReadModSettins(BiomeTerrainValues.lavaLevelMin.name(), BiomeTerrainValues.lavaLevelMin.intValue());
        this.lavaLevelMax = this.ReadModSettins(BiomeTerrainValues.lavaLevelMax.name(), BiomeTerrainValues.lavaLevelMax.intValue());

        this.undergroundLakes = this.ReadModSettins(BiomeTerrainValues.undergroundLakes.name(), BiomeTerrainValues.undergroundLakes.booleanValue());
        this.undergroundLakesInAir = this.ReadModSettins(BiomeTerrainValues.undergroundLakesInAir.name(), BiomeTerrainValues.undergroundLakesInAir.booleanValue());
        this.undergroundLakeFrequency = this.ReadModSettins(BiomeTerrainValues.undergroundLakeFrequency.name(), BiomeTerrainValues.undergroundLakeFrequency.intValue());
        this.undergroundLakeRarity = this.ReadModSettins(BiomeTerrainValues.undergroundLakeRarity.name(), BiomeTerrainValues.undergroundLakeRarity.intValue());
        this.undergroundLakeMinSize = this.ReadModSettins(BiomeTerrainValues.undergroundLakeMinSize.name(), BiomeTerrainValues.undergroundLakeMinSize.intValue());
        this.undergroundLakeMaxSize = this.ReadModSettins(BiomeTerrainValues.undergroundLakeMaxSize.name(), BiomeTerrainValues.undergroundLakeMaxSize.intValue());
        this.undergroundLakeMinAltitude = this.ReadModSettins(BiomeTerrainValues.undergroundLakeMinAltitude.name(), BiomeTerrainValues.undergroundLakeMinAltitude.intValue());
        this.undergroundLakeMaxAltitude = this.ReadModSettins(BiomeTerrainValues.undergroundLakeMaxAltitude.name(), BiomeTerrainValues.undergroundLakeMaxAltitude.intValue());


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
    private void BuildReplaceMatrix()
    {
        for(int i = 0; i<this.ReplaceBlocksMatrix.length;i++)
        {
            if(this.replaceBlocks.containsKey(i))
                this.ReplaceBlocksMatrix[i] = this.replaceBlocks.get(i);
            else
                this.ReplaceBlocksMatrix[i] = (byte)i;

        }
    }

    void WriteSettings()
    {
        //File f = new File(this.worldSaveFolder, BiomeTerrainValues.biomeTerrainSettingsName.stringValue());
        File f = new File(SettingsDir, BiomeTerrainValues.biomeTerrainSettingsName.stringValue());
        try
        {
            this.SettingsWriter = new BufferedWriter(new FileWriter(f, false));

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

        WriteModSettings(BiomeTerrainValues.biomeSize.name(), this.biomeSize);
        WriteModSettings(BiomeTerrainValues.minMoisture.name(), this.minMoisture);
        WriteModSettings(BiomeTerrainValues.maxMoisture.name(), this.maxMoisture);
        WriteModSettings(BiomeTerrainValues.minTemperature.name(), this.minTemperature);
        WriteModSettings(BiomeTerrainValues.maxTemperature.name(), this.maxTemperature);
        WriteModSettings(BiomeTerrainValues.snowThreshold.name(), this.snowThreshold);
        WriteModSettings(BiomeTerrainValues.iceThreshold.name(), this.iceThreshold);

        WriteModTitleSettings("Swamp Biome Variables");
        WriteModSettings(BiomeTerrainValues.muddySwamps.name(), this.muddySwamps);
        WriteModSettings(BiomeTerrainValues.claySwamps.name(), this.claySwamps);
        WriteModSettings(BiomeTerrainValues.swampSize.name(), this.swampSize);

        WriteModTitleSettings("Desert Biome Variables");
        WriteModSettings(BiomeTerrainValues.waterlessDeserts.name(), this.waterlessDeserts);
        WriteModSettings(BiomeTerrainValues.removeSurfaceDirtFromDesert.name(), this.removeSurfaceDirtFromDesert);
        WriteModSettings(BiomeTerrainValues.desertDirt.name(), this.desertDirt);
        WriteModSettings(BiomeTerrainValues.desertDirtFrequency.name(), this.desertDirtFrequency);

        WriteModTitleSettings("Start Underground Variables :");
        WriteModTitleSettings("Cave Variables");
        WriteModSettings(BiomeTerrainValues.caveRarity.name(), this.caveRarity);
        WriteModSettings(BiomeTerrainValues.caveFrequency.name(), this.caveFrequency);
        WriteModSettings(BiomeTerrainValues.caveMinAltitude.name(), this.caveMinAltitude);
        WriteModSettings(BiomeTerrainValues.caveMaxAltitude.name(), this.caveMaxAltitude);
        WriteModSettings(BiomeTerrainValues.individualCaveRarity.name(), this.individualCaveRarity);
        WriteModSettings(BiomeTerrainValues.caveSystemFrequency.name(), this.caveSystemFrequency);
        WriteModSettings(BiomeTerrainValues.caveSystemPocketChance.name(), this.caveSystemPocketChance);
        WriteModSettings(BiomeTerrainValues.caveSystemPocketMinSize.name(), this.caveSystemPocketMinSize);
        WriteModSettings(BiomeTerrainValues.caveSystemPocketMaxSize.name(), this.caveSystemPocketMaxSize);
        WriteModSettings(BiomeTerrainValues.evenCaveDistribution.name(), this.evenCaveDistribution);


        WriteModTitleSettings("Start Terrain Variables :");
        WriteModSettings(BiomeTerrainValues.waterLevel.name(), this.waterLevel);
        WriteModSettings(BiomeTerrainValues.maxAverageHeight.name(), this.maxAverageHeight);
        WriteModSettings(BiomeTerrainValues.maxAverageDepth.name(), this.maxAverageDepth);
        WriteModSettings(BiomeTerrainValues.fractureHorizontal.name(), this.fractureHorizontal);
        WriteModSettings(BiomeTerrainValues.fractureVertical.name(), this.fractureVertical);
        WriteModSettings(BiomeTerrainValues.volatility1.name(), this.volatility1);
        WriteModSettings(BiomeTerrainValues.volatility2.name(), this.volatility2);
        WriteModSettings(BiomeTerrainValues.volatilityWeight1.name(), this.volatilityWeight1);
        WriteModSettings(BiomeTerrainValues.volatilityWeight2.name(), this.volatilityWeight2);
        WriteModSettings(BiomeTerrainValues.disableBedrock.name(), this.disableBedrock);
        WriteModSettings(BiomeTerrainValues.flatBedrock.name(), this.flatBedrock);
        WriteModSettings(BiomeTerrainValues.bedrockobsidian.name(), this.bedrockobsidian);


        WriteModTitleSettings("Replace Variables");
        WriteModSettings(BiomeTerrainValues.removeSurfaceStone.name(), this.removeSurfaceStone);

        WriteModReplaceSettings();


        this.WriteModTitleSettings("Start BOB Objects Variables :");
        this.WriteModSettings(BiomeTerrainValues.customObjects.name(), this.customObjects);
        this.WriteModSettings(BiomeTerrainValues.objectSpawnRatio.name(), Integer.valueOf(this.objectSpawnRatio).intValue());
        this.WriteModSettings(BiomeTerrainValues.denyObjectsUnderFill.name(), this.denyObjectsUnderFill);

        this.WriteModTitleSettings("Start Cactus&Tree Variables :");
        this.WriteModSettings(BiomeTerrainValues.notchBiomeTrees.name(), this.notchBiomeTrees);
        this.WriteModSettings(BiomeTerrainValues.globalTreeDensity.name(), this.globalTreeDensity);
        this.WriteModSettings(BiomeTerrainValues.rainforestTreeDensity.name(), this.rainforestTreeDensity);
        this.WriteModSettings(BiomeTerrainValues.swamplandTreeDensity.name(), this.swamplandTreeDensity);
        this.WriteModSettings(BiomeTerrainValues.seasonalforestTreeDensity.name(), this.seasonalforestTreeDensity);
        this.WriteModSettings(BiomeTerrainValues.forestTreeDensity.name(), this.forestTreeDensity);
        this.WriteModSettings(BiomeTerrainValues.savannaTreeDensity.name(), this.savannaTreeDensity);
        this.WriteModSettings(BiomeTerrainValues.shrublandTreeDensity.name(), this.shrublandTreeDensity);
        this.WriteModSettings(BiomeTerrainValues.taigaTreeDensity.name(), this.taigaTreeDensity);
        this.WriteModSettings(BiomeTerrainValues.desertTreeDensity.name(), this.desertTreeDensity);
        this.WriteModSettings(BiomeTerrainValues.plainsTreeDensity.name(), this.plainsTreeDensity);
        this.WriteModSettings(BiomeTerrainValues.iceDesertTreeDensity.name(), this.iceDesertTreeDensity);
        this.WriteModSettings(BiomeTerrainValues.tundraTreeDensity.name(), this.tundraTreeDensity);
        this.WriteModSettings(BiomeTerrainValues.globalCactusDensity.name(), this.globalCactusDensity);
        this.WriteModSettings(BiomeTerrainValues.desertCactusDensity.name(), this.desertCactusDensity);
        this.WriteModSettings(BiomeTerrainValues.cactusDepositRarity.name(), this.cactusDepositRarity);
        this.WriteModSettings(BiomeTerrainValues.cactusDepositMinAltitude.name(), this.cactusDepositMinAltitude);
        this.WriteModSettings(BiomeTerrainValues.cactusDepositMaxAltitude.name(), this.cactusDepositMaxAltitude);


        this.WriteModTitleSettings("Lava Pool Variables");
        this.WriteModSettings(BiomeTerrainValues.lavaLevelMin.name(), this.lavaLevelMin);
        this.WriteModSettings(BiomeTerrainValues.lavaLevelMax.name(), this.lavaLevelMax);

        this.WriteModTitleSettings("Underground Lake Variables");
        this.WriteModSettings(BiomeTerrainValues.undergroundLakes.name(), this.undergroundLakes);
        this.WriteModSettings(BiomeTerrainValues.undergroundLakesInAir.name(), this.undergroundLakesInAir);
        this.WriteModSettings(BiomeTerrainValues.undergroundLakeFrequency.name(), this.undergroundLakeFrequency);
        this.WriteModSettings(BiomeTerrainValues.undergroundLakeRarity.name(), this.undergroundLakeRarity);
        this.WriteModSettings(BiomeTerrainValues.undergroundLakeMinSize.name(), this.undergroundLakeMinSize);
        this.WriteModSettings(BiomeTerrainValues.undergroundLakeMaxSize.name(), this.undergroundLakeMaxSize);
        this.WriteModSettings(BiomeTerrainValues.undergroundLakeMinAltitude.name(), this.undergroundLakeMinAltitude);
        this.WriteModSettings(BiomeTerrainValues.undergroundLakeMaxAltitude.name(), this.undergroundLakeMaxAltitude);


        this.WriteModTitleSettings("Start Deposit Variables :");
        this.WriteModTitleSettings("Above Ground Variables");
        this.WriteModSettings(BiomeTerrainValues.flowerDepositRarity.name(), this.flowerDepositRarity);
        this.WriteModSettings(BiomeTerrainValues.flowerDepositFrequency.name(), this.flowerDepositFrequency);
        this.WriteModSettings(BiomeTerrainValues.flowerDepositMinAltitude.name(), this.flowerDepositMinAltitude);
        this.WriteModSettings(BiomeTerrainValues.flowerDepositMaxAltitude.name(), this.flowerDepositMaxAltitude);
        this.WriteModSettings(BiomeTerrainValues.roseDepositRarity.name(), this.roseDepositRarity);
        this.WriteModSettings(BiomeTerrainValues.roseDepositFrequency.name(), this.roseDepositFrequency);
        this.WriteModSettings(BiomeTerrainValues.roseDepositMinAltitude.name(), this.roseDepositMinAltitude);
        this.WriteModSettings(BiomeTerrainValues.roseDepositMaxAltitude.name(), this.roseDepositMaxAltitude);
        this.WriteModSettings(BiomeTerrainValues.brownMushroomDepositRarity.name(), this.brownMushroomDepositRarity);
        this.WriteModSettings(BiomeTerrainValues.brownMushroomDepositFrequency.name(), this.brownMushroomDepositFrequency);
        this.WriteModSettings(BiomeTerrainValues.brownMushroomDepositMinAltitude.name(), this.brownMushroomDepositMinAltitude);
        this.WriteModSettings(BiomeTerrainValues.brownMushroomDepositMaxAltitude.name(), this.brownMushroomDepositMaxAltitude);
        this.WriteModSettings(BiomeTerrainValues.redMushroomDepositRarity.name(), this.redMushroomDepositRarity);
        this.WriteModSettings(BiomeTerrainValues.redMushroomDepositFrequency.name(), this.redMushroomDepositFrequency);
        this.WriteModSettings(BiomeTerrainValues.redMushroomDepositMinAltitude.name(), this.redMushroomDepositMinAltitude);
        this.WriteModSettings(BiomeTerrainValues.redMushroomDepositMaxAltitude.name(), this.redMushroomDepositMaxAltitude);
        this.WriteModSettings(BiomeTerrainValues.reedDepositRarity.name(), this.reedDepositRarity);
        this.WriteModSettings(BiomeTerrainValues.reedDepositFrequency.name(), this.reedDepositFrequency);
        this.WriteModSettings(BiomeTerrainValues.reedDepositMinAltitude.name(), this.reedDepositMinAltitude);
        this.WriteModSettings(BiomeTerrainValues.reedDepositMaxAltitude.name(), this.reedDepositMaxAltitude);
        this.WriteModSettings(BiomeTerrainValues.pumpkinDepositRarity.name(), this.pumpkinDepositRarity);
        this.WriteModSettings(BiomeTerrainValues.pumpkinDepositFrequency.name(), this.pumpkinDepositFrequency);
        this.WriteModSettings(BiomeTerrainValues.pumpkinDepositMinAltitude.name(), this.pumpkinDepositMinAltitude);
        this.WriteModSettings(BiomeTerrainValues.pumpkinDepositMaxAltitude.name(), this.pumpkinDepositMaxAltitude);

        this.WriteModTitleSettings("Above/Below Ground Variables");
        this.WriteModSettings(BiomeTerrainValues.evenWaterSourceDistribution.name(), this.evenWaterSourceDistribution);
        this.WriteModSettings(BiomeTerrainValues.waterSourceDepositRarity.name(), this.waterSourceDepositRarity);
        this.WriteModSettings(BiomeTerrainValues.waterSourceDepositFrequency.name(), this.waterSourceDepositFrequency);
        this.WriteModSettings(BiomeTerrainValues.waterSourceDepositMinAltitude.name(), this.waterSourceDepositMinAltitude);
        this.WriteModSettings(BiomeTerrainValues.waterSourceDepositMaxAltitude.name(), this.waterSourceDepositMaxAltitude);
        this.WriteModSettings(BiomeTerrainValues.evenLavaSourceDistribution.name(), this.evenLavaSourceDistribution);
        this.WriteModSettings(BiomeTerrainValues.lavaSourceDepositRarity.name(), this.lavaSourceDepositRarity);
        this.WriteModSettings(BiomeTerrainValues.lavaSourceDepositFrequency.name(), this.lavaSourceDepositFrequency);
        this.WriteModSettings(BiomeTerrainValues.lavaSourceDepositMinAltitude.name(), this.lavaSourceDepositMinAltitude);
        this.WriteModSettings(BiomeTerrainValues.lavaSourceDepositMaxAltitude.name(), this.lavaSourceDepositMaxAltitude);
        this.WriteModSettings(BiomeTerrainValues.disableNotchPonds.name(), this.disableNotchPonds);

        this.WriteModTitleSettings("Below Ground Variables");
        this.WriteModSettings(BiomeTerrainValues.dirtDepositRarity1.name(), this.dirtDepositRarity1);
        this.WriteModSettings(BiomeTerrainValues.dirtDepositFrequency1.name(), this.dirtDepositFrequency1);
        this.WriteModSettings(BiomeTerrainValues.dirtDepositSize1.name(), this.dirtDepositSize1);
        this.WriteModSettings(BiomeTerrainValues.dirtDepositMinAltitude1.name(), this.dirtDepositMinAltitude1);
        this.WriteModSettings(BiomeTerrainValues.dirtDepositMaxAltitude1.name(), this.dirtDepositMaxAltitude1);
        this.WriteModSettings(BiomeTerrainValues.dirtDepositRarity2.name(), this.dirtDepositRarity2);
        this.WriteModSettings(BiomeTerrainValues.dirtDepositFrequency2.name(), this.dirtDepositFrequency2);
        this.WriteModSettings(BiomeTerrainValues.dirtDepositSize2.name(), this.dirtDepositSize2);
        this.WriteModSettings(BiomeTerrainValues.dirtDepositMinAltitude2.name(), this.dirtDepositMinAltitude2);
        this.WriteModSettings(BiomeTerrainValues.dirtDepositMaxAltitude2.name(), this.dirtDepositMaxAltitude2);
        this.WriteModSettings(BiomeTerrainValues.dirtDepositRarity3.name(), this.dirtDepositRarity3);
        this.WriteModSettings(BiomeTerrainValues.dirtDepositFrequency3.name(), this.dirtDepositFrequency3);
        this.WriteModSettings(BiomeTerrainValues.dirtDepositSize3.name(), this.dirtDepositSize3);
        this.WriteModSettings(BiomeTerrainValues.dirtDepositMinAltitude3.name(), this.dirtDepositMinAltitude3);
        this.WriteModSettings(BiomeTerrainValues.dirtDepositMaxAltitude3.name(), this.dirtDepositMaxAltitude3);
        this.WriteModSettings(BiomeTerrainValues.dirtDepositRarity4.name(), this.dirtDepositRarity4);
        this.WriteModSettings(BiomeTerrainValues.dirtDepositFrequency4.name(), this.dirtDepositFrequency4);
        this.WriteModSettings(BiomeTerrainValues.dirtDepositSize4.name(), this.dirtDepositSize4);
        this.WriteModSettings(BiomeTerrainValues.dirtDepositMinAltitude4.name(), this.dirtDepositMinAltitude4);
        this.WriteModSettings(BiomeTerrainValues.dirtDepositMaxAltitude4.name(), this.dirtDepositMaxAltitude4);
        this.WriteModSettings(BiomeTerrainValues.gravelDepositRarity1.name(), this.gravelDepositRarity1);
        this.WriteModSettings(BiomeTerrainValues.gravelDepositFrequency1.name(), this.gravelDepositFrequency1);
        this.WriteModSettings(BiomeTerrainValues.gravelDepositSize1.name(), this.gravelDepositSize1);
        this.WriteModSettings(BiomeTerrainValues.gravelDepositMinAltitude1.name(), this.gravelDepositMinAltitude1);
        this.WriteModSettings(BiomeTerrainValues.gravelDepositMaxAltitude1.name(), this.gravelDepositMaxAltitude1);
        this.WriteModSettings(BiomeTerrainValues.gravelDepositRarity2.name(), this.gravelDepositRarity2);
        this.WriteModSettings(BiomeTerrainValues.gravelDepositFrequency2.name(), this.gravelDepositFrequency2);
        this.WriteModSettings(BiomeTerrainValues.gravelDepositSize2.name(), this.gravelDepositSize2);
        this.WriteModSettings(BiomeTerrainValues.gravelDepositMinAltitude2.name(), this.gravelDepositMinAltitude2);
        this.WriteModSettings(BiomeTerrainValues.gravelDepositMaxAltitude2.name(), this.gravelDepositMaxAltitude2);
        this.WriteModSettings(BiomeTerrainValues.gravelDepositRarity3.name(), this.gravelDepositRarity3);
        this.WriteModSettings(BiomeTerrainValues.gravelDepositFrequency3.name(), this.gravelDepositFrequency3);
        this.WriteModSettings(BiomeTerrainValues.gravelDepositSize3.name(), this.gravelDepositSize3);
        this.WriteModSettings(BiomeTerrainValues.gravelDepositMinAltitude3.name(), this.gravelDepositMinAltitude3);
        this.WriteModSettings(BiomeTerrainValues.gravelDepositMaxAltitude3.name(), this.gravelDepositMaxAltitude3);
        this.WriteModSettings(BiomeTerrainValues.gravelDepositRarity4.name(), this.gravelDepositRarity4);
        this.WriteModSettings(BiomeTerrainValues.gravelDepositFrequency4.name(), this.gravelDepositFrequency4);
        this.WriteModSettings(BiomeTerrainValues.gravelDepositSize4.name(), this.gravelDepositSize4);
        this.WriteModSettings(BiomeTerrainValues.gravelDepositMinAltitude4.name(), this.gravelDepositMinAltitude4);
        this.WriteModSettings(BiomeTerrainValues.gravelDepositMaxAltitude4.name(), this.gravelDepositMaxAltitude4);
        this.WriteModSettings(BiomeTerrainValues.clayDepositRarity1.name(), this.clayDepositRarity1);
        this.WriteModSettings(BiomeTerrainValues.clayDepositFrequency1.name(), this.clayDepositFrequency1);
        this.WriteModSettings(BiomeTerrainValues.clayDepositSize1.name(), this.clayDepositSize1);
        this.WriteModSettings(BiomeTerrainValues.clayDepositMinAltitude1.name(), this.clayDepositMinAltitude1);
        this.WriteModSettings(BiomeTerrainValues.clayDepositMaxAltitude1.name(), this.clayDepositMaxAltitude1);
        this.WriteModSettings(BiomeTerrainValues.clayDepositRarity2.name(), this.clayDepositRarity2);
        this.WriteModSettings(BiomeTerrainValues.clayDepositFrequency2.name(), this.clayDepositFrequency2);
        this.WriteModSettings(BiomeTerrainValues.clayDepositSize2.name(), this.clayDepositSize2);
        this.WriteModSettings(BiomeTerrainValues.clayDepositMinAltitude2.name(), this.clayDepositMinAltitude2);
        this.WriteModSettings(BiomeTerrainValues.clayDepositMaxAltitude2.name(), this.clayDepositMaxAltitude2);
        this.WriteModSettings(BiomeTerrainValues.clayDepositRarity3.name(), this.clayDepositRarity3);
        this.WriteModSettings(BiomeTerrainValues.clayDepositFrequency3.name(), this.clayDepositFrequency3);
        this.WriteModSettings(BiomeTerrainValues.clayDepositSize3.name(), this.clayDepositSize3);
        this.WriteModSettings(BiomeTerrainValues.clayDepositMinAltitude3.name(), this.clayDepositMinAltitude3);
        this.WriteModSettings(BiomeTerrainValues.clayDepositMaxAltitude3.name(), this.clayDepositMaxAltitude3);
        this.WriteModSettings(BiomeTerrainValues.clayDepositRarity4.name(), this.clayDepositRarity4);
        this.WriteModSettings(BiomeTerrainValues.clayDepositFrequency4.name(), this.clayDepositFrequency4);
        this.WriteModSettings(BiomeTerrainValues.clayDepositSize4.name(), this.clayDepositSize4);
        this.WriteModSettings(BiomeTerrainValues.clayDepositMinAltitude4.name(), this.clayDepositMinAltitude4);
        this.WriteModSettings(BiomeTerrainValues.clayDepositMaxAltitude4.name(), this.clayDepositMaxAltitude4);
        this.WriteModSettings(BiomeTerrainValues.coalDepositRarity1.name(), this.coalDepositRarity1);
        this.WriteModSettings(BiomeTerrainValues.coalDepositFrequency1.name(), this.coalDepositFrequency1);
        this.WriteModSettings(BiomeTerrainValues.coalDepositSize1.name(), this.coalDepositSize1);
        this.WriteModSettings(BiomeTerrainValues.coalDepositMinAltitude1.name(), this.coalDepositMinAltitude1);
        this.WriteModSettings(BiomeTerrainValues.coalDepositMaxAltitude1.name(), this.coalDepositMaxAltitude1);
        this.WriteModSettings(BiomeTerrainValues.coalDepositRarity2.name(), this.coalDepositRarity2);
        this.WriteModSettings(BiomeTerrainValues.coalDepositFrequency2.name(), this.coalDepositFrequency2);
        this.WriteModSettings(BiomeTerrainValues.coalDepositSize2.name(), this.coalDepositSize2);
        this.WriteModSettings(BiomeTerrainValues.coalDepositMinAltitude2.name(), this.coalDepositMinAltitude2);
        this.WriteModSettings(BiomeTerrainValues.coalDepositMaxAltitude2.name(), this.coalDepositMaxAltitude2);
        this.WriteModSettings(BiomeTerrainValues.coalDepositRarity3.name(), this.coalDepositRarity3);
        this.WriteModSettings(BiomeTerrainValues.coalDepositFrequency3.name(), this.coalDepositFrequency3);
        this.WriteModSettings(BiomeTerrainValues.coalDepositSize3.name(), this.coalDepositSize3);
        this.WriteModSettings(BiomeTerrainValues.coalDepositMinAltitude3.name(), this.coalDepositMinAltitude3);
        this.WriteModSettings(BiomeTerrainValues.coalDepositMaxAltitude3.name(), this.coalDepositMaxAltitude3);
        this.WriteModSettings(BiomeTerrainValues.coalDepositRarity4.name(), this.coalDepositRarity4);
        this.WriteModSettings(BiomeTerrainValues.coalDepositFrequency4.name(), this.coalDepositFrequency4);
        this.WriteModSettings(BiomeTerrainValues.coalDepositSize4.name(), this.coalDepositSize4);
        this.WriteModSettings(BiomeTerrainValues.coalDepositMinAltitude4.name(), this.coalDepositMinAltitude4);
        this.WriteModSettings(BiomeTerrainValues.coalDepositMaxAltitude4.name(), this.coalDepositMaxAltitude4);
        this.WriteModSettings(BiomeTerrainValues.ironDepositRarity1.name(), this.ironDepositRarity1);
        this.WriteModSettings(BiomeTerrainValues.ironDepositFrequency1.name(), this.ironDepositFrequency1);
        this.WriteModSettings(BiomeTerrainValues.ironDepositSize1.name(), this.ironDepositSize1);
        this.WriteModSettings(BiomeTerrainValues.ironDepositMinAltitude1.name(), this.ironDepositMinAltitude1);
        this.WriteModSettings(BiomeTerrainValues.ironDepositMaxAltitude1.name(), this.ironDepositMaxAltitude1);
        this.WriteModSettings(BiomeTerrainValues.ironDepositRarity2.name(), this.ironDepositRarity2);
        this.WriteModSettings(BiomeTerrainValues.ironDepositFrequency2.name(), this.ironDepositFrequency2);
        this.WriteModSettings(BiomeTerrainValues.ironDepositSize2.name(), this.ironDepositSize2);
        this.WriteModSettings(BiomeTerrainValues.ironDepositMinAltitude2.name(), this.ironDepositMinAltitude2);
        this.WriteModSettings(BiomeTerrainValues.ironDepositMaxAltitude2.name(), this.ironDepositMaxAltitude2);
        this.WriteModSettings(BiomeTerrainValues.ironDepositRarity3.name(), this.ironDepositRarity3);
        this.WriteModSettings(BiomeTerrainValues.ironDepositFrequency3.name(), this.ironDepositFrequency3);
        this.WriteModSettings(BiomeTerrainValues.ironDepositSize3.name(), this.ironDepositSize3);
        this.WriteModSettings(BiomeTerrainValues.ironDepositMinAltitude3.name(), this.ironDepositMinAltitude3);
        this.WriteModSettings(BiomeTerrainValues.ironDepositMaxAltitude3.name(), this.ironDepositMaxAltitude3);
        this.WriteModSettings(BiomeTerrainValues.ironDepositRarity4.name(), this.ironDepositRarity4);
        this.WriteModSettings(BiomeTerrainValues.ironDepositFrequency4.name(), this.ironDepositFrequency4);
        this.WriteModSettings(BiomeTerrainValues.ironDepositSize4.name(), this.ironDepositSize4);
        this.WriteModSettings(BiomeTerrainValues.ironDepositMinAltitude4.name(), this.ironDepositMinAltitude4);
        this.WriteModSettings(BiomeTerrainValues.ironDepositMaxAltitude4.name(), this.ironDepositMaxAltitude4);
        this.WriteModSettings(BiomeTerrainValues.goldDepositRarity1.name(), this.goldDepositRarity1);
        this.WriteModSettings(BiomeTerrainValues.goldDepositFrequency1.name(), this.goldDepositFrequency1);
        this.WriteModSettings(BiomeTerrainValues.goldDepositSize1.name(), this.goldDepositSize1);
        this.WriteModSettings(BiomeTerrainValues.goldDepositMinAltitude1.name(), this.goldDepositMinAltitude1);
        this.WriteModSettings(BiomeTerrainValues.goldDepositMaxAltitude1.name(), this.goldDepositMaxAltitude1);
        this.WriteModSettings(BiomeTerrainValues.goldDepositRarity2.name(), this.goldDepositRarity2);
        this.WriteModSettings(BiomeTerrainValues.goldDepositFrequency2.name(), this.goldDepositFrequency2);
        this.WriteModSettings(BiomeTerrainValues.goldDepositSize2.name(), this.goldDepositSize2);
        this.WriteModSettings(BiomeTerrainValues.goldDepositMinAltitude2.name(), this.goldDepositMinAltitude2);
        this.WriteModSettings(BiomeTerrainValues.goldDepositMaxAltitude2.name(), this.goldDepositMaxAltitude2);
        this.WriteModSettings(BiomeTerrainValues.goldDepositRarity3.name(), this.goldDepositRarity3);
        this.WriteModSettings(BiomeTerrainValues.goldDepositFrequency3.name(), this.goldDepositFrequency3);
        this.WriteModSettings(BiomeTerrainValues.goldDepositSize3.name(), this.goldDepositSize3);
        this.WriteModSettings(BiomeTerrainValues.goldDepositMinAltitude3.name(), this.goldDepositMinAltitude3);
        this.WriteModSettings(BiomeTerrainValues.goldDepositMaxAltitude3.name(), this.goldDepositMaxAltitude3);
        this.WriteModSettings(BiomeTerrainValues.goldDepositRarity4.name(), this.goldDepositRarity4);
        this.WriteModSettings(BiomeTerrainValues.goldDepositFrequency4.name(), this.goldDepositFrequency4);
        this.WriteModSettings(BiomeTerrainValues.goldDepositSize4.name(), this.goldDepositSize4);
        this.WriteModSettings(BiomeTerrainValues.goldDepositMinAltitude4.name(), this.goldDepositMinAltitude4);
        this.WriteModSettings(BiomeTerrainValues.goldDepositMaxAltitude4.name(), this.goldDepositMaxAltitude4);
        this.WriteModSettings(BiomeTerrainValues.redstoneDepositRarity1.name(), this.redstoneDepositRarity1);
        this.WriteModSettings(BiomeTerrainValues.redstoneDepositFrequency1.name(), this.redstoneDepositFrequency1);
        this.WriteModSettings(BiomeTerrainValues.redstoneDepositSize1.name(), this.redstoneDepositSize1);
        this.WriteModSettings(BiomeTerrainValues.redstoneDepositMinAltitude1.name(), this.redstoneDepositMinAltitude1);
        this.WriteModSettings(BiomeTerrainValues.redstoneDepositMaxAltitude1.name(), this.redstoneDepositMaxAltitude1);
        this.WriteModSettings(BiomeTerrainValues.redstoneDepositRarity2.name(), this.redstoneDepositRarity2);
        this.WriteModSettings(BiomeTerrainValues.redstoneDepositFrequency2.name(), this.redstoneDepositFrequency2);
        this.WriteModSettings(BiomeTerrainValues.redstoneDepositSize2.name(), this.redstoneDepositSize2);
        this.WriteModSettings(BiomeTerrainValues.redstoneDepositMinAltitude2.name(), this.redstoneDepositMinAltitude2);
        this.WriteModSettings(BiomeTerrainValues.redstoneDepositMaxAltitude2.name(), this.redstoneDepositMaxAltitude2);
        this.WriteModSettings(BiomeTerrainValues.redstoneDepositRarity3.name(), this.redstoneDepositRarity3);
        this.WriteModSettings(BiomeTerrainValues.redstoneDepositFrequency3.name(), this.redstoneDepositFrequency3);
        this.WriteModSettings(BiomeTerrainValues.redstoneDepositSize3.name(), this.redstoneDepositSize3);
        this.WriteModSettings(BiomeTerrainValues.redstoneDepositMinAltitude3.name(), this.redstoneDepositMinAltitude3);
        this.WriteModSettings(BiomeTerrainValues.redstoneDepositMaxAltitude3.name(), this.redstoneDepositMaxAltitude3);
        this.WriteModSettings(BiomeTerrainValues.redstoneDepositRarity4.name(), this.redstoneDepositRarity4);
        this.WriteModSettings(BiomeTerrainValues.redstoneDepositFrequency4.name(), this.redstoneDepositFrequency4);
        this.WriteModSettings(BiomeTerrainValues.redstoneDepositSize4.name(), this.redstoneDepositSize4);
        this.WriteModSettings(BiomeTerrainValues.redstoneDepositMinAltitude4.name(), this.redstoneDepositMinAltitude4);
        this.WriteModSettings(BiomeTerrainValues.redstoneDepositMaxAltitude4.name(), this.redstoneDepositMaxAltitude4);
        this.WriteModSettings(BiomeTerrainValues.diamondDepositRarity1.name(), this.diamondDepositRarity1);
        this.WriteModSettings(BiomeTerrainValues.diamondDepositFrequency1.name(), this.diamondDepositFrequency1);
        this.WriteModSettings(BiomeTerrainValues.diamondDepositSize1.name(), this.diamondDepositSize1);
        this.WriteModSettings(BiomeTerrainValues.diamondDepositMinAltitude1.name(), this.diamondDepositMinAltitude1);
        this.WriteModSettings(BiomeTerrainValues.diamondDepositMaxAltitude1.name(), this.diamondDepositMaxAltitude1);
        this.WriteModSettings(BiomeTerrainValues.diamondDepositRarity2.name(), this.diamondDepositRarity2);
        this.WriteModSettings(BiomeTerrainValues.diamondDepositFrequency2.name(), this.diamondDepositFrequency2);
        this.WriteModSettings(BiomeTerrainValues.diamondDepositSize2.name(), this.diamondDepositSize2);
        this.WriteModSettings(BiomeTerrainValues.diamondDepositMinAltitude2.name(), this.diamondDepositMinAltitude2);
        this.WriteModSettings(BiomeTerrainValues.diamondDepositMaxAltitude2.name(), this.diamondDepositMaxAltitude2);
        this.WriteModSettings(BiomeTerrainValues.diamondDepositRarity3.name(), this.diamondDepositRarity3);
        this.WriteModSettings(BiomeTerrainValues.diamondDepositFrequency3.name(), this.diamondDepositFrequency3);
        this.WriteModSettings(BiomeTerrainValues.diamondDepositSize3.name(), this.diamondDepositSize3);
        this.WriteModSettings(BiomeTerrainValues.diamondDepositMinAltitude3.name(), this.diamondDepositMinAltitude3);
        this.WriteModSettings(BiomeTerrainValues.diamondDepositMaxAltitude3.name(), this.diamondDepositMaxAltitude3);
        this.WriteModSettings(BiomeTerrainValues.diamondDepositRarity4.name(), this.diamondDepositRarity4);
        this.WriteModSettings(BiomeTerrainValues.diamondDepositFrequency4.name(), this.diamondDepositFrequency4);
        this.WriteModSettings(BiomeTerrainValues.diamondDepositSize4.name(), this.diamondDepositSize4);
        this.WriteModSettings(BiomeTerrainValues.diamondDepositMinAltitude4.name(), this.diamondDepositMinAltitude4);
        this.WriteModSettings(BiomeTerrainValues.diamondDepositMaxAltitude4.name(), this.diamondDepositMaxAltitude4);
        this.WriteModSettings(BiomeTerrainValues.lapislazuliDepositRarity1.name(), this.lapislazuliDepositRarity1);
        this.WriteModSettings(BiomeTerrainValues.lapislazuliDepositFrequency1.name(), this.lapislazuliDepositFrequency1);
        this.WriteModSettings(BiomeTerrainValues.lapislazuliDepositSize1.name(), this.lapislazuliDepositSize1);
        this.WriteModSettings(BiomeTerrainValues.lapislazuliDepositMinAltitude1.name(), this.lapislazuliDepositMinAltitude1);
        this.WriteModSettings(BiomeTerrainValues.lapislazuliDepositMaxAltitude1.name(), this.lapislazuliDepositMaxAltitude1);
        this.WriteModSettings(BiomeTerrainValues.lapislazuliDepositRarity2.name(), this.lapislazuliDepositRarity2);
        this.WriteModSettings(BiomeTerrainValues.lapislazuliDepositFrequency2.name(), this.lapislazuliDepositFrequency2);
        this.WriteModSettings(BiomeTerrainValues.lapislazuliDepositSize2.name(), this.lapislazuliDepositSize2);
        this.WriteModSettings(BiomeTerrainValues.lapislazuliDepositMinAltitude2.name(), this.lapislazuliDepositMinAltitude2);
        this.WriteModSettings(BiomeTerrainValues.lapislazuliDepositMaxAltitude2.name(), this.lapislazuliDepositMaxAltitude2);
        this.WriteModSettings(BiomeTerrainValues.lapislazuliDepositRarity3.name(), this.lapislazuliDepositRarity3);
        this.WriteModSettings(BiomeTerrainValues.lapislazuliDepositFrequency3.name(), this.lapislazuliDepositFrequency3);
        this.WriteModSettings(BiomeTerrainValues.lapislazuliDepositSize3.name(), this.lapislazuliDepositSize3);
        this.WriteModSettings(BiomeTerrainValues.lapislazuliDepositMinAltitude3.name(), this.lapislazuliDepositMinAltitude3);
        this.WriteModSettings(BiomeTerrainValues.lapislazuliDepositMaxAltitude3.name(), this.lapislazuliDepositMaxAltitude3);
        this.WriteModSettings(BiomeTerrainValues.lapislazuliDepositRarity4.name(), this.lapislazuliDepositRarity4);
        this.WriteModSettings(BiomeTerrainValues.lapislazuliDepositFrequency4.name(), this.lapislazuliDepositFrequency4);
        this.WriteModSettings(BiomeTerrainValues.lapislazuliDepositSize4.name(), this.lapislazuliDepositSize4);
        this.WriteModSettings(BiomeTerrainValues.lapislazuliDepositMinAltitude4.name(), this.lapislazuliDepositMinAltitude4);
        this.WriteModSettings(BiomeTerrainValues.lapislazuliDepositMaxAltitude4.name(), this.lapislazuliDepositMaxAltitude4);
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


    private void RegisterBOBPlugins()
    {
        if (this.customObjects)
        {
            try
            {
                File BOBFolder = new File(SettingsDir, "BOBPlugins");
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
                    this.RegisterBOBPlugins(BOBFile);
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

    private void RegisterBOBPlugins(File ObjectPlugin)
    {
        try
        {

            BufferedReader ObjectProps = new BufferedReader(new FileReader(ObjectPlugin));
            // Legacy BOB Loader
            if ((ObjectPlugin.getName().endsWith(".bob")) || (ObjectPlugin.getName().endsWith(".BOB")))
            {

                CustomObjectLegacy workObject = new CustomObjectLegacy();
                String workingString = "";

                while (!((workingString.equals("METBEGIN")) || (workingString.equals("METABEGIN"))))
                {
                    workingString = ObjectProps.readLine();
                    if (!((workingString.equals("METBEGIN") || (workingString.equals("METABEGIN")))))
                    {
                        String[] stringSet = workingString.split(",");
                        int X = Integer.parseInt(stringSet[0]);
                        int Y = Integer.parseInt(stringSet[1]);
                        double Data = Double.valueOf(stringSet[2].split(":")[1]);
                        int Z = Integer.parseInt(stringSet[2].split(":")[0]);
                        workObject.DataValues[X][Y][Z] = Data;
                    }
                }
                workObject.spawnID = Integer.parseInt(ObjectProps.readLine());

                if (ObjectProps.readLine().equals("true"))
                {
                    workObject.underwater = true;
                }
                this.LegacyObjects.add(workObject);

                System.out.println("BOB Plugin Registered: " + ObjectPlugin.getName());
                ObjectProps.close();
                return;

            }
            // BO2 Loader
            if ((ObjectPlugin.getName().endsWith(".bo2")) || (ObjectPlugin.getName().endsWith(".BO2")))
            {
                CustomObject WorkingCustomObject = new CustomObject();

                String workingString = ObjectProps.readLine();

                if (!workingString.equals("[META]"))
                {
                    System.out.println("Invalid BOB Plugin: " + ObjectPlugin.getName());
                    ObjectProps.close();
                    return;
                }

                boolean dataReached = false;
                while ((workingString = ObjectProps.readLine()) != null)
                {

                    if (!dataReached)
                    {
                        if (workingString.contains("="))
                        {
                            String[] stringSet = workingString.split("=");
                            if (stringSet[0].equals("spawnOnBlockType"))
                            {
                                String[] blocks = stringSet[1].split(",");
                                int counter = 0;
                                while (counter < blocks.length)
                                {
                                    WorkingCustomObject.spawnOnBlockType.add(Integer.parseInt(blocks[counter]));
                                    counter++;
                                }
                            }
                            if (stringSet[0].equals("spawnSunlight"))
                            {
                                if (stringSet[1].toLowerCase().equals("false"))
                                {
                                    WorkingCustomObject.spawnSunlight = false;
                                }
                            }
                            if (stringSet[0].equals("spawnDarkness"))
                            {
                                if (stringSet[1].toLowerCase().equals("true"))
                                {
                                    WorkingCustomObject.spawnDarkness = true;
                                }
                            }
                            if (stringSet[0].equals("spawnWater"))
                            {
                                if (stringSet[1].toLowerCase().equals("true"))
                                {
                                    WorkingCustomObject.spawnWater = true;
                                }
                            }
                            if (stringSet[0].equals("spawnLava"))
                            {
                                if (stringSet[1].toLowerCase().equals("true"))
                                {
                                    WorkingCustomObject.spawnLava = true;
                                }
                            }
                            if (stringSet[0].equals("underFill"))
                            {
                                if (stringSet[1].toLowerCase().equals("false"))
                                {
                                    WorkingCustomObject.underFill = false;
                                }
                            }
                            if (stringSet[0].equals("randomRotation"))
                            {
                                if (stringSet[1].toLowerCase().equals("false"))
                                {
                                    WorkingCustomObject.randomRotation = false;
                                }
                            }
                            if (stringSet[0].equals("dig"))
                            {
                                if (stringSet[1].toLowerCase().equals("true"))
                                {
                                    WorkingCustomObject.dig = true;
                                }
                            }
                            if (stringSet[0].equals("rarity"))
                            {
                                WorkingCustomObject.rarity = Integer.parseInt(stringSet[1]);
                            }
                            if (stringSet[0].equals("spawnElevationMin"))
                            {
                                WorkingCustomObject.spawnElevationMin = Integer.parseInt(stringSet[1]);
                            }
                            if (stringSet[0].equals("spawnElevationMax"))
                            {
                                WorkingCustomObject.spawnElevationMax = Integer.parseInt(stringSet[1]);
                            }
                            if (stringSet[0].equals("groupId"))
                            {
                                WorkingCustomObject.groupId = stringSet[1];
                            }
                            if (stringSet[0].equals("tree"))
                            {
                                if (stringSet[1].toLowerCase().equals("true"))
                                {
                                    WorkingCustomObject.tree = true;
                                }
                            }
                            if (stringSet[0].equals("branch"))
                            {
                                if (stringSet[1].toLowerCase().equals("true"))
                                {
                                    WorkingCustomObject.branch = true;
                                }
                            }
                            if (stringSet[0].equals("diggingBranch"))
                            {
                                if (stringSet[1].toLowerCase().equals("true"))
                                {
                                    WorkingCustomObject.diggingBranch = true;
                                }
                            }
                            if (stringSet[0].equals("groupFrequencyMin"))
                            {
                                WorkingCustomObject.groupFrequencyMin = Integer.parseInt(stringSet[1]);
                            }
                            if (stringSet[0].equals("groupFrequencyMax"))
                            {
                                WorkingCustomObject.groupFrequencyMax = Integer.parseInt(stringSet[1]);
                            }
                            if (stringSet[0].equals("groupSeperationMin"))
                            {
                                WorkingCustomObject.groupSeperationMin = Integer.parseInt(stringSet[1]);
                            }
                            if (stringSet[0].equals("groupSeperationMax"))
                            {
                                WorkingCustomObject.groupSeperationMax = Integer.parseInt(stringSet[1]);
                            }
                            if (stringSet[0].equals("collisionPercentage"))
                            {
                                WorkingCustomObject.collisionPercentage = (Integer.parseInt(stringSet[1]) / 100);
                            }
                            if (stringSet[0].equals("spawnInBiome"))
                            {
                                stringSet = stringSet[1].split(",");
                                int counter = 0;
                                while (counter < WorkingCustomObject.spawnInBiome.size())
                                {
                                    if (stringSet[counter].equals("Icedesert"))
                                        WorkingCustomObject.spawnInBiome.add("ice desert");
                                    else if (stringSet[counter].equals("Seasonalforest"))
                                        WorkingCustomObject.spawnInBiome.add("seasonal forest");
                                    else
                                        WorkingCustomObject.spawnInBiome.add(stringSet[counter].toLowerCase());
                                    counter++;
                                }
                            }
                            if (stringSet[0].equals("branchLimit"))
                            {
                                WorkingCustomObject.branchLimit = (Integer.parseInt(stringSet[1]));
                            }
                            if (stringSet[0].equals("needsFoundation"))
                            {
                                if (stringSet[1].toLowerCase().equals("false"))
                                {
                                    WorkingCustomObject.needsFoundation = false;
                                }
                            }
                            if (stringSet[0].equals("version"))
                            {
                                WorkingCustomObject.version = stringSet[1].toLowerCase();
                            }

                        } else if (workingString.equals("[DATA]"))
                            dataReached = true;
                        continue;
                    }

                    String[] CoordinateSet = workingString.split(":")[0].split(",");
                    String BlockString = workingString.split(":")[1];
                    Coordinate Coordinates;
                    if (WorkingCustomObject.dig)
                    {
                        Coordinates = new Coordinate(Integer.parseInt(CoordinateSet[0]), Integer.parseInt(CoordinateSet[2]), Integer.parseInt(CoordinateSet[1]), BlockString, true);
                    } else
                    {
                        Coordinates = new Coordinate(Integer.parseInt(CoordinateSet[0]), Integer.parseInt(CoordinateSet[2]), Integer.parseInt(CoordinateSet[1]), BlockString, false);

                    }
                    Coordinates.RegisterData();
                    WorkingCustomObject.Data.add(Coordinates);

                }

                if (!dataReached)
                {
                    System.out.println("Invalid BOB Plugin: " + ObjectPlugin.getName());
                    ObjectProps.close();
                    return;
                }

                WorkingCustomObject.CorrectSettings();

                WorkingCustomObject.name = ObjectPlugin.getName();

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

                System.out.println("BOB Plugin Registered: " + ObjectPlugin.getName());
                ObjectProps.close();

            }
        } catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Invalid BOB Plugin: " + ObjectPlugin.getName());
        }

    }


    public int getWaterLevel()
    {
        return this.waterLevel;
    }

    public double getMaxAverageHeight()
    {
        return this.maxAverageHeight;
    }

    public double getMaxAverageDepth()
    {
        return this.maxAverageDepth;
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