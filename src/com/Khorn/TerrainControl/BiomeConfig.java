package com.Khorn.TerrainControl;

import com.Khorn.TerrainControl.Util.ConfigFile;
import com.Khorn.TerrainControl.Util.CustomBiome;
import net.minecraft.server.BiomeBase;
import net.minecraft.server.Block;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BiomeConfig extends ConfigFile
{

    public HashMap<Integer, Byte> replaceBlocks = new HashMap<Integer, Byte>();
    public HashMap<Integer, Integer> replaceHeightMin = new HashMap<Integer, Integer>();
    public HashMap<Integer, Integer> replaceHeightMax = new HashMap<Integer, Integer>();
    public byte[] ReplaceMatrixBlocks = new byte[256];
    public int[] ReplaceMatrixHeightMin = new int[256];
    public int[] ReplaceMatrixHeightMax = new int[256];


    public int BiomeChance;


    //Surface config
    public float BiomeSurface;
    public float BiomeVolatility;

    public byte SurfaceBlock;
    public byte GroundBlock;

    public boolean evenWaterSourceDistribution;
    public boolean evenLavaSourceDistribution;

    public boolean disableNotchPonds;


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

    public int longGrassDepositRarity;
    public int longGrassDepositFrequency;
    public int longGrassDepositMinAltitude;
    public int longGrassDepositMaxAltitude;

    public int deadBushDepositRarity;
    public int deadBushDepositFrequency;
    public int deadBushDepositMinAltitude;
    public int deadBushDepositMaxAltitude;


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

    public int waterClayDepositRarity;
    public int waterClayDepositFrequency;
    public int waterClayDepositSize;

    public int waterSandDepositRarity;
    public int waterSandDepositFrequency;
    public int waterSandDepositSize;

    public int waterGravelDepositRarity;
    public int waterGravelDepositFrequency;
    public int waterGravelDepositSize;

    public int dungeonRarity;
    public int dungeonFrequency;
    public int dungeonMinAltitude;
    public int dungeonMaxAltitude;

    public boolean notchBiomeTrees;
    public int TreeDensity;


    public int cactusDepositRarity;
    public int cactusDepositFrequency;
    public int cactusDepositMinAltitude;
    public int cactusDepositMaxAltitude;


    public BiomeBase Biome;
    private WorldConfig worldConfig;


    public BiomeConfig(File settingsDir, BiomeBase biome, WorldConfig config)
    {

        this.Biome = biome;
        worldConfig = config;
        InitDefaults();

        File settingsFile = new File(settingsDir, this.Biome.l + TCDefaultValues.WorldBiomeConfigName.stringValue());
        this.ReadSettingsFile(settingsFile);
        this.ReadConfigSettings();

        this.CorrectSettings();

        this.WriteSettingsFile(settingsFile);

        BuildReplaceMatrix();


    }

    public BiomeConfig(File settingsDir, String biomeName, int biomeId, WorldConfig config)
    {


        worldConfig = config;
        CustomBiome cBiome = new CustomBiome(biomeId, biomeName);
        this.Biome = cBiome;

        File settingsFile = new File(settingsDir, biomeName + TCDefaultValues.WorldBiomeConfigName.stringValue());
        this.ReadSettingsFile(settingsFile);
        this.ReadConfigSettings();

        this.CorrectSettings();


        this.WriteSettingsFile(settingsFile);

        BuildReplaceMatrix();
        cBiome.SetBiome(this);


    }


    protected void ReadConfigSettings()
    {


        this.BiomeChance = ReadModSettings(TCDefaultValues.biomeChance.name(), this.DefaultBiomeChance);

        this.evenWaterSourceDistribution = this.ReadModSettings(TCDefaultValues.evenWaterSourceDistribution.name(), TCDefaultValues.evenWaterSourceDistribution.booleanValue());
        this.evenLavaSourceDistribution = this.ReadModSettings(TCDefaultValues.evenLavaSourceDistribution.name(), TCDefaultValues.evenLavaSourceDistribution.booleanValue());

        this.BiomeSurface = this.ReadModSettings(TCDefaultValues.BiomeSurfaceAdd.name(), this.DefaultBiomeSurface);
        this.BiomeVolatility = this.ReadModSettings(TCDefaultValues.BiomeVolatilityAdd.name(), this.DefaultBiomeVolatility);

        this.SurfaceBlock = this.ReadModSettings(TCDefaultValues.SurfaceBlock.name(), this.DefaultSurfaceBlock);
        this.GroundBlock = this.ReadModSettings(TCDefaultValues.GroundBlock.name(), this.DefaultGroundBlock);


        this.flowerDepositRarity = this.ReadModSettings(TCDefaultValues.flowerDepositRarity.name(), TCDefaultValues.flowerDepositRarity.intValue());
        this.flowerDepositFrequency = this.ReadModSettings(TCDefaultValues.flowerDepositFrequency.name(), this.DefaultFlowers);
        this.flowerDepositMinAltitude = this.ReadModSettings(TCDefaultValues.flowerDepositMinAltitude.name(), TCDefaultValues.flowerDepositMinAltitude.intValue());
        this.flowerDepositMaxAltitude = this.ReadModSettings(TCDefaultValues.flowerDepositMaxAltitude.name(), TCDefaultValues.flowerDepositMaxAltitude.intValue());

        this.roseDepositRarity = this.ReadModSettings(TCDefaultValues.roseDepositRarity.name(), TCDefaultValues.roseDepositRarity.intValue());
        this.roseDepositFrequency = this.ReadModSettings(TCDefaultValues.roseDepositFrequency.name(), this.DefaultFlowers);
        this.roseDepositMinAltitude = this.ReadModSettings(TCDefaultValues.roseDepositMinAltitude.name(), TCDefaultValues.roseDepositMinAltitude.intValue());
        this.roseDepositMaxAltitude = this.ReadModSettings(TCDefaultValues.roseDepositMaxAltitude.name(), TCDefaultValues.roseDepositMaxAltitude.intValue());

        this.brownMushroomDepositRarity = this.ReadModSettings(TCDefaultValues.brownMushroomDepositRarity.name(), TCDefaultValues.brownMushroomDepositRarity.intValue());
        this.brownMushroomDepositFrequency = this.ReadModSettings(TCDefaultValues.brownMushroomDepositFrequency.name(), this.DefaultMushroom);
        this.brownMushroomDepositMinAltitude = this.ReadModSettings(TCDefaultValues.brownMushroomDepositMinAltitude.name(), TCDefaultValues.brownMushroomDepositMinAltitude.intValue());
        this.brownMushroomDepositMaxAltitude = this.ReadModSettings(TCDefaultValues.brownMushroomDepositMaxAltitude.name(), TCDefaultValues.brownMushroomDepositMaxAltitude.intValue());

        this.redMushroomDepositRarity = this.ReadModSettings(TCDefaultValues.redMushroomDepositRarity.name(), TCDefaultValues.redMushroomDepositRarity.intValue());
        this.redMushroomDepositFrequency = this.ReadModSettings(TCDefaultValues.redMushroomDepositFrequency.name(), this.DefaultMushroom);
        this.redMushroomDepositMinAltitude = this.ReadModSettings(TCDefaultValues.redMushroomDepositMinAltitude.name(), TCDefaultValues.redMushroomDepositMinAltitude.intValue());
        this.redMushroomDepositMaxAltitude = this.ReadModSettings(TCDefaultValues.redMushroomDepositMaxAltitude.name(), TCDefaultValues.redMushroomDepositMaxAltitude.intValue());

        this.reedDepositRarity = this.ReadModSettings(TCDefaultValues.reedDepositRarity.name(), TCDefaultValues.reedDepositRarity.intValue());
        this.reedDepositFrequency = this.ReadModSettings(TCDefaultValues.reedDepositFrequency.name(), this.DefaultReed);
        this.reedDepositMinAltitude = this.ReadModSettings(TCDefaultValues.reedDepositMinAltitude.name(), TCDefaultValues.reedDepositMinAltitude.intValue());
        this.reedDepositMaxAltitude = this.ReadModSettings(TCDefaultValues.reedDepositMaxAltitude.name(), TCDefaultValues.reedDepositMaxAltitude.intValue());

        this.pumpkinDepositRarity = this.ReadModSettings(TCDefaultValues.pumpkinDepositRarity.name(), TCDefaultValues.pumpkinDepositRarity.intValue());
        this.pumpkinDepositFrequency = this.ReadModSettings(TCDefaultValues.pumpkinDepositFrequency.name(), TCDefaultValues.pumpkinDepositFrequency.intValue());
        this.pumpkinDepositMinAltitude = this.ReadModSettings(TCDefaultValues.pumpkinDepositMinAltitude.name(), TCDefaultValues.pumpkinDepositMinAltitude.intValue());
        this.pumpkinDepositMaxAltitude = this.ReadModSettings(TCDefaultValues.pumpkinDepositMaxAltitude.name(), TCDefaultValues.pumpkinDepositMaxAltitude.intValue());

        this.cactusDepositRarity = this.ReadModSettings(TCDefaultValues.cactusDepositRarity.name(), TCDefaultValues.cactusDepositRarity.intValue());
        this.cactusDepositFrequency = this.ReadModSettings(TCDefaultValues.cactusDepositFrequency.name(), this.DefaultCactus);
        this.cactusDepositMinAltitude = this.ReadModSettings(TCDefaultValues.cactusDepositMinAltitude.name(), TCDefaultValues.cactusDepositMinAltitude.intValue());
        this.cactusDepositMaxAltitude = this.ReadModSettings(TCDefaultValues.cactusDepositMaxAltitude.name(), TCDefaultValues.cactusDepositMaxAltitude.intValue());

        this.longGrassDepositRarity = this.ReadModSettings(TCDefaultValues.longGrassDepositRarity.name(), TCDefaultValues.longGrassDepositRarity.intValue());
        this.longGrassDepositFrequency = this.ReadModSettings(TCDefaultValues.longGrassDepositFrequency.name(), this.DefaultGrass);
        this.longGrassDepositMinAltitude = this.ReadModSettings(TCDefaultValues.longGrassDepositMinAltitude.name(), TCDefaultValues.longGrassDepositMinAltitude.intValue());
        this.longGrassDepositMaxAltitude = this.ReadModSettings(TCDefaultValues.longGrassDepositMaxAltitude.name(), TCDefaultValues.longGrassDepositMaxAltitude.intValue());

        this.deadBushDepositRarity = this.ReadModSettings(TCDefaultValues.deadBushDepositRarity.name(), TCDefaultValues.deadBushDepositRarity.intValue());
        this.deadBushDepositFrequency = this.ReadModSettings(TCDefaultValues.deadBushDepositFrequency.name(), this.DefaultDeadBrush);
        this.deadBushDepositMinAltitude = this.ReadModSettings(TCDefaultValues.deadBushDepositMinAltitude.name(), TCDefaultValues.deadBushDepositMinAltitude.intValue());
        this.deadBushDepositMaxAltitude = this.ReadModSettings(TCDefaultValues.deadBushDepositMaxAltitude.name(), TCDefaultValues.deadBushDepositMaxAltitude.intValue());


        this.waterSourceDepositRarity = this.ReadModSettings(TCDefaultValues.waterSourceDepositRarity.name(), TCDefaultValues.waterSourceDepositRarity.intValue());
        this.waterSourceDepositFrequency = this.ReadModSettings(TCDefaultValues.waterSourceDepositFrequency.name(), TCDefaultValues.waterSourceDepositFrequency.intValue());
        this.waterSourceDepositMinAltitude = this.ReadModSettings(TCDefaultValues.waterSourceDepositMinAltitude.name(), TCDefaultValues.waterSourceDepositMinAltitude.intValue());
        this.waterSourceDepositMaxAltitude = this.ReadModSettings(TCDefaultValues.waterSourceDepositMaxAltitude.name(), TCDefaultValues.waterSourceDepositMaxAltitude.intValue());


        this.lavaSourceDepositRarity = this.ReadModSettings(TCDefaultValues.lavaSourceDepositRarity.name(), TCDefaultValues.lavaSourceDepositRarity.intValue());
        this.lavaSourceDepositFrequency = this.ReadModSettings(TCDefaultValues.lavaSourceDepositFrequency.name(), TCDefaultValues.lavaSourceDepositFrequency.intValue());
        this.lavaSourceDepositMinAltitude = this.ReadModSettings(TCDefaultValues.lavaSourceDepositMinAltitude.name(), TCDefaultValues.lavaSourceDepositMinAltitude.intValue());
        this.lavaSourceDepositMaxAltitude = this.ReadModSettings(TCDefaultValues.lavaSourceDepositMaxAltitude.name(), TCDefaultValues.lavaSourceDepositMaxAltitude.intValue());

        this.waterClayDepositRarity = this.ReadModSettings(TCDefaultValues.waterClayDepositRarity.name(), TCDefaultValues.waterClayDepositRarity.intValue());
        this.waterClayDepositFrequency = this.ReadModSettings(TCDefaultValues.waterClayDepositFrequency.name(), this.DefaultClay);
        this.waterClayDepositSize = this.ReadModSettings(TCDefaultValues.waterClayDepositSize.name(), TCDefaultValues.waterClayDepositSize.intValue());

        this.waterSandDepositRarity = this.ReadModSettings(TCDefaultValues.waterSandDepositRarity.name(), TCDefaultValues.waterSandDepositRarity.intValue());
        this.waterSandDepositFrequency = this.ReadModSettings(TCDefaultValues.waterSandDepositFrequency.name(), TCDefaultValues.waterSandDepositFrequency.intValue());
        this.waterSandDepositSize = this.ReadModSettings(TCDefaultValues.waterSandDepositSize.name(), TCDefaultValues.waterSandDepositSize.intValue());

        this.waterGravelDepositRarity = this.ReadModSettings(TCDefaultValues.waterGravelDepositRarity.name(), TCDefaultValues.waterGravelDepositRarity.intValue());
        this.waterGravelDepositFrequency = this.ReadModSettings(TCDefaultValues.waterGravelDepositFrequency.name(), TCDefaultValues.waterGravelDepositFrequency.intValue());
        this.waterGravelDepositSize = this.ReadModSettings(TCDefaultValues.waterGravelDepositSize.name(), TCDefaultValues.waterGravelDepositSize.intValue());

        this.dirtDepositRarity1 = this.ReadModSettings(TCDefaultValues.dirtDepositRarity1.name(), TCDefaultValues.dirtDepositRarity1.intValue());
        this.dirtDepositFrequency1 = this.ReadModSettings(TCDefaultValues.dirtDepositFrequency1.name(), TCDefaultValues.dirtDepositFrequency1.intValue());
        this.dirtDepositSize1 = this.ReadModSettings(TCDefaultValues.dirtDepositSize1.name(), TCDefaultValues.dirtDepositSize1.intValue());
        this.dirtDepositMinAltitude1 = this.ReadModSettings(TCDefaultValues.dirtDepositMinAltitude1.name(), TCDefaultValues.dirtDepositMinAltitude1.intValue());
        this.dirtDepositMaxAltitude1 = this.ReadModSettings(TCDefaultValues.dirtDepositMaxAltitude1.name(), TCDefaultValues.dirtDepositMaxAltitude1.intValue());
        this.dirtDepositRarity2 = this.ReadModSettings(TCDefaultValues.dirtDepositRarity2.name(), TCDefaultValues.dirtDepositRarity2.intValue());
        this.dirtDepositFrequency2 = this.ReadModSettings(TCDefaultValues.dirtDepositFrequency2.name(), TCDefaultValues.dirtDepositFrequency2.intValue());
        this.dirtDepositSize2 = this.ReadModSettings(TCDefaultValues.dirtDepositSize2.name(), TCDefaultValues.dirtDepositSize2.intValue());
        this.dirtDepositMinAltitude2 = this.ReadModSettings(TCDefaultValues.dirtDepositMinAltitude2.name(), TCDefaultValues.dirtDepositMinAltitude2.intValue());
        this.dirtDepositMaxAltitude2 = this.ReadModSettings(TCDefaultValues.dirtDepositMaxAltitude2.name(), TCDefaultValues.dirtDepositMaxAltitude2.intValue());
        this.dirtDepositRarity3 = this.ReadModSettings(TCDefaultValues.dirtDepositRarity3.name(), TCDefaultValues.dirtDepositRarity3.intValue());
        this.dirtDepositFrequency3 = this.ReadModSettings(TCDefaultValues.dirtDepositFrequency3.name(), TCDefaultValues.dirtDepositFrequency3.intValue());
        this.dirtDepositSize3 = this.ReadModSettings(TCDefaultValues.dirtDepositSize3.name(), TCDefaultValues.dirtDepositSize3.intValue());
        this.dirtDepositMinAltitude3 = this.ReadModSettings(TCDefaultValues.dirtDepositMinAltitude3.name(), TCDefaultValues.dirtDepositMinAltitude3.intValue());
        this.dirtDepositMaxAltitude3 = this.ReadModSettings(TCDefaultValues.dirtDepositMaxAltitude3.name(), TCDefaultValues.dirtDepositMaxAltitude3.intValue());
        this.dirtDepositRarity4 = this.ReadModSettings(TCDefaultValues.dirtDepositRarity4.name(), TCDefaultValues.dirtDepositRarity4.intValue());
        this.dirtDepositFrequency4 = this.ReadModSettings(TCDefaultValues.dirtDepositFrequency4.name(), TCDefaultValues.dirtDepositFrequency4.intValue());
        this.dirtDepositSize4 = this.ReadModSettings(TCDefaultValues.dirtDepositSize4.name(), TCDefaultValues.dirtDepositSize4.intValue());
        this.dirtDepositMinAltitude4 = this.ReadModSettings(TCDefaultValues.dirtDepositMinAltitude4.name(), TCDefaultValues.dirtDepositMinAltitude4.intValue());
        this.dirtDepositMaxAltitude4 = this.ReadModSettings(TCDefaultValues.dirtDepositMaxAltitude4.name(), TCDefaultValues.dirtDepositMaxAltitude4.intValue());

        this.gravelDepositRarity1 = this.ReadModSettings(TCDefaultValues.gravelDepositRarity1.name(), TCDefaultValues.gravelDepositRarity1.intValue());
        this.gravelDepositFrequency1 = this.ReadModSettings(TCDefaultValues.gravelDepositFrequency1.name(), TCDefaultValues.gravelDepositFrequency1.intValue());
        this.gravelDepositSize1 = this.ReadModSettings(TCDefaultValues.gravelDepositSize1.name(), TCDefaultValues.gravelDepositSize1.intValue());
        this.gravelDepositMinAltitude1 = this.ReadModSettings(TCDefaultValues.gravelDepositMinAltitude1.name(), TCDefaultValues.gravelDepositMinAltitude1.intValue());
        this.gravelDepositMaxAltitude1 = this.ReadModSettings(TCDefaultValues.gravelDepositMaxAltitude1.name(), TCDefaultValues.gravelDepositMaxAltitude1.intValue());
        this.gravelDepositRarity2 = this.ReadModSettings(TCDefaultValues.gravelDepositRarity2.name(), TCDefaultValues.gravelDepositRarity2.intValue());
        this.gravelDepositFrequency2 = this.ReadModSettings(TCDefaultValues.gravelDepositFrequency2.name(), TCDefaultValues.gravelDepositFrequency2.intValue());
        this.gravelDepositSize2 = this.ReadModSettings(TCDefaultValues.gravelDepositSize2.name(), TCDefaultValues.gravelDepositSize2.intValue());
        this.gravelDepositMinAltitude2 = this.ReadModSettings(TCDefaultValues.gravelDepositMinAltitude2.name(), TCDefaultValues.gravelDepositMinAltitude2.intValue());
        this.gravelDepositMaxAltitude2 = this.ReadModSettings(TCDefaultValues.gravelDepositMaxAltitude2.name(), TCDefaultValues.gravelDepositMaxAltitude2.intValue());
        this.gravelDepositRarity3 = this.ReadModSettings(TCDefaultValues.gravelDepositRarity3.name(), TCDefaultValues.gravelDepositRarity3.intValue());
        this.gravelDepositFrequency3 = this.ReadModSettings(TCDefaultValues.gravelDepositFrequency3.name(), TCDefaultValues.gravelDepositFrequency3.intValue());
        this.gravelDepositSize3 = this.ReadModSettings(TCDefaultValues.gravelDepositSize3.name(), TCDefaultValues.gravelDepositSize3.intValue());
        this.gravelDepositMinAltitude3 = this.ReadModSettings(TCDefaultValues.gravelDepositMinAltitude3.name(), TCDefaultValues.gravelDepositMinAltitude3.intValue());
        this.gravelDepositMaxAltitude3 = this.ReadModSettings(TCDefaultValues.gravelDepositMaxAltitude3.name(), TCDefaultValues.gravelDepositMaxAltitude3.intValue());
        this.gravelDepositRarity4 = this.ReadModSettings(TCDefaultValues.gravelDepositRarity4.name(), TCDefaultValues.gravelDepositRarity4.intValue());
        this.gravelDepositFrequency4 = this.ReadModSettings(TCDefaultValues.gravelDepositFrequency4.name(), TCDefaultValues.gravelDepositFrequency4.intValue());
        this.gravelDepositSize4 = this.ReadModSettings(TCDefaultValues.gravelDepositSize4.name(), TCDefaultValues.gravelDepositSize4.intValue());
        this.gravelDepositMinAltitude4 = this.ReadModSettings(TCDefaultValues.gravelDepositMinAltitude4.name(), TCDefaultValues.gravelDepositMinAltitude4.intValue());
        this.gravelDepositMaxAltitude4 = this.ReadModSettings(TCDefaultValues.gravelDepositMaxAltitude4.name(), TCDefaultValues.gravelDepositMaxAltitude4.intValue());

        this.clayDepositRarity1 = this.ReadModSettings(TCDefaultValues.clayDepositRarity1.name(), TCDefaultValues.clayDepositRarity1.intValue());
        this.clayDepositFrequency1 = this.ReadModSettings(TCDefaultValues.clayDepositFrequency1.name(), TCDefaultValues.clayDepositFrequency1.intValue());
        this.clayDepositSize1 = this.ReadModSettings(TCDefaultValues.clayDepositSize1.name(), TCDefaultValues.clayDepositSize1.intValue());
        this.clayDepositMinAltitude1 = this.ReadModSettings(TCDefaultValues.clayDepositMinAltitude1.name(), TCDefaultValues.clayDepositMinAltitude1.intValue());
        this.clayDepositMaxAltitude1 = this.ReadModSettings(TCDefaultValues.clayDepositMaxAltitude1.name(), TCDefaultValues.clayDepositMaxAltitude1.intValue());
        this.clayDepositRarity2 = this.ReadModSettings(TCDefaultValues.clayDepositRarity2.name(), TCDefaultValues.clayDepositRarity2.intValue());
        this.clayDepositFrequency2 = this.ReadModSettings(TCDefaultValues.clayDepositFrequency2.name(), TCDefaultValues.clayDepositFrequency2.intValue());
        this.clayDepositSize2 = this.ReadModSettings(TCDefaultValues.clayDepositSize2.name(), TCDefaultValues.clayDepositSize2.intValue());
        this.clayDepositMinAltitude2 = this.ReadModSettings(TCDefaultValues.clayDepositMinAltitude2.name(), TCDefaultValues.clayDepositMinAltitude2.intValue());
        this.clayDepositMaxAltitude2 = this.ReadModSettings(TCDefaultValues.clayDepositMaxAltitude2.name(), TCDefaultValues.clayDepositMaxAltitude2.intValue());
        this.clayDepositRarity3 = this.ReadModSettings(TCDefaultValues.clayDepositRarity3.name(), TCDefaultValues.clayDepositRarity3.intValue());
        this.clayDepositFrequency3 = this.ReadModSettings(TCDefaultValues.clayDepositFrequency3.name(), TCDefaultValues.clayDepositFrequency3.intValue());
        this.clayDepositSize3 = this.ReadModSettings(TCDefaultValues.clayDepositSize3.name(), TCDefaultValues.clayDepositSize3.intValue());
        this.clayDepositMinAltitude3 = this.ReadModSettings(TCDefaultValues.clayDepositMinAltitude3.name(), TCDefaultValues.clayDepositMinAltitude3.intValue());
        this.clayDepositMaxAltitude3 = this.ReadModSettings(TCDefaultValues.clayDepositMaxAltitude3.name(), TCDefaultValues.clayDepositMaxAltitude3.intValue());
        this.clayDepositRarity4 = this.ReadModSettings(TCDefaultValues.clayDepositRarity4.name(), TCDefaultValues.clayDepositRarity4.intValue());
        this.clayDepositFrequency4 = this.ReadModSettings(TCDefaultValues.clayDepositFrequency4.name(), TCDefaultValues.clayDepositFrequency4.intValue());
        this.clayDepositSize4 = this.ReadModSettings(TCDefaultValues.clayDepositSize4.name(), TCDefaultValues.clayDepositSize4.intValue());
        this.clayDepositMinAltitude4 = this.ReadModSettings(TCDefaultValues.clayDepositMinAltitude4.name(), TCDefaultValues.clayDepositMinAltitude4.intValue());
        this.clayDepositMaxAltitude4 = this.ReadModSettings(TCDefaultValues.clayDepositMaxAltitude4.name(), TCDefaultValues.clayDepositMaxAltitude4.intValue());

        this.coalDepositRarity1 = this.ReadModSettings(TCDefaultValues.coalDepositRarity1.name(), TCDefaultValues.coalDepositRarity1.intValue());
        this.coalDepositFrequency1 = this.ReadModSettings(TCDefaultValues.coalDepositFrequency1.name(), TCDefaultValues.coalDepositFrequency1.intValue());
        this.coalDepositSize1 = this.ReadModSettings(TCDefaultValues.coalDepositSize1.name(), TCDefaultValues.coalDepositSize1.intValue());
        this.coalDepositMinAltitude1 = this.ReadModSettings(TCDefaultValues.coalDepositMinAltitude1.name(), TCDefaultValues.coalDepositMinAltitude1.intValue());
        this.coalDepositMaxAltitude1 = this.ReadModSettings(TCDefaultValues.coalDepositMaxAltitude1.name(), TCDefaultValues.coalDepositMaxAltitude1.intValue());
        this.coalDepositRarity2 = this.ReadModSettings(TCDefaultValues.coalDepositRarity2.name(), TCDefaultValues.coalDepositRarity2.intValue());
        this.coalDepositFrequency2 = this.ReadModSettings(TCDefaultValues.coalDepositFrequency2.name(), TCDefaultValues.coalDepositFrequency2.intValue());
        this.coalDepositSize2 = this.ReadModSettings(TCDefaultValues.coalDepositSize2.name(), TCDefaultValues.coalDepositSize2.intValue());
        this.coalDepositMinAltitude2 = this.ReadModSettings(TCDefaultValues.coalDepositMinAltitude2.name(), TCDefaultValues.coalDepositMinAltitude2.intValue());
        this.coalDepositMaxAltitude2 = this.ReadModSettings(TCDefaultValues.coalDepositMaxAltitude2.name(), TCDefaultValues.coalDepositMaxAltitude2.intValue());
        this.coalDepositRarity3 = this.ReadModSettings(TCDefaultValues.coalDepositRarity3.name(), TCDefaultValues.coalDepositRarity3.intValue());
        this.coalDepositFrequency3 = this.ReadModSettings(TCDefaultValues.coalDepositFrequency3.name(), TCDefaultValues.coalDepositFrequency3.intValue());
        this.coalDepositSize3 = this.ReadModSettings(TCDefaultValues.coalDepositSize3.name(), TCDefaultValues.coalDepositSize3.intValue());
        this.coalDepositMinAltitude3 = this.ReadModSettings(TCDefaultValues.coalDepositMinAltitude3.name(), TCDefaultValues.coalDepositMinAltitude3.intValue());
        this.coalDepositMaxAltitude3 = this.ReadModSettings(TCDefaultValues.coalDepositMaxAltitude3.name(), TCDefaultValues.coalDepositMaxAltitude3.intValue());
        this.coalDepositRarity4 = this.ReadModSettings(TCDefaultValues.coalDepositRarity4.name(), TCDefaultValues.coalDepositRarity4.intValue());
        this.coalDepositFrequency4 = this.ReadModSettings(TCDefaultValues.coalDepositFrequency4.name(), TCDefaultValues.coalDepositFrequency4.intValue());
        this.coalDepositSize4 = this.ReadModSettings(TCDefaultValues.coalDepositSize4.name(), TCDefaultValues.coalDepositSize4.intValue());
        this.coalDepositMinAltitude4 = this.ReadModSettings(TCDefaultValues.coalDepositMinAltitude4.name(), TCDefaultValues.coalDepositMinAltitude4.intValue());
        this.coalDepositMaxAltitude4 = this.ReadModSettings(TCDefaultValues.coalDepositMaxAltitude4.name(), TCDefaultValues.coalDepositMaxAltitude4.intValue());

        this.ironDepositRarity1 = this.ReadModSettings(TCDefaultValues.ironDepositRarity1.name(), TCDefaultValues.ironDepositRarity1.intValue());
        this.ironDepositFrequency1 = this.ReadModSettings(TCDefaultValues.ironDepositFrequency1.name(), TCDefaultValues.ironDepositFrequency1.intValue());
        this.ironDepositSize1 = this.ReadModSettings(TCDefaultValues.ironDepositSize1.name(), TCDefaultValues.ironDepositSize1.intValue());
        this.ironDepositMinAltitude1 = this.ReadModSettings(TCDefaultValues.ironDepositMinAltitude1.name(), TCDefaultValues.ironDepositMinAltitude1.intValue());
        this.ironDepositMaxAltitude1 = this.ReadModSettings(TCDefaultValues.ironDepositMaxAltitude1.name(), TCDefaultValues.ironDepositMaxAltitude1.intValue());
        this.ironDepositRarity2 = this.ReadModSettings(TCDefaultValues.ironDepositRarity2.name(), TCDefaultValues.ironDepositRarity2.intValue());
        this.ironDepositFrequency2 = this.ReadModSettings(TCDefaultValues.ironDepositFrequency2.name(), TCDefaultValues.ironDepositFrequency2.intValue());
        this.ironDepositSize2 = this.ReadModSettings(TCDefaultValues.ironDepositSize2.name(), TCDefaultValues.ironDepositSize2.intValue());
        this.ironDepositMinAltitude2 = this.ReadModSettings(TCDefaultValues.ironDepositMinAltitude2.name(), TCDefaultValues.ironDepositMinAltitude2.intValue());
        this.ironDepositMaxAltitude2 = this.ReadModSettings(TCDefaultValues.ironDepositMaxAltitude2.name(), TCDefaultValues.ironDepositMaxAltitude2.intValue());
        this.ironDepositRarity3 = this.ReadModSettings(TCDefaultValues.ironDepositRarity3.name(), TCDefaultValues.ironDepositRarity3.intValue());
        this.ironDepositFrequency3 = this.ReadModSettings(TCDefaultValues.ironDepositFrequency3.name(), TCDefaultValues.ironDepositFrequency3.intValue());
        this.ironDepositSize3 = this.ReadModSettings(TCDefaultValues.ironDepositSize3.name(), TCDefaultValues.ironDepositSize3.intValue());
        this.ironDepositMinAltitude3 = this.ReadModSettings(TCDefaultValues.ironDepositMinAltitude3.name(), TCDefaultValues.ironDepositMinAltitude3.intValue());
        this.ironDepositMaxAltitude3 = this.ReadModSettings(TCDefaultValues.ironDepositMaxAltitude3.name(), TCDefaultValues.ironDepositMaxAltitude3.intValue());
        this.ironDepositRarity4 = this.ReadModSettings(TCDefaultValues.ironDepositRarity4.name(), TCDefaultValues.ironDepositRarity4.intValue());
        this.ironDepositFrequency4 = this.ReadModSettings(TCDefaultValues.ironDepositFrequency4.name(), TCDefaultValues.ironDepositFrequency4.intValue());
        this.ironDepositSize4 = this.ReadModSettings(TCDefaultValues.ironDepositSize4.name(), TCDefaultValues.ironDepositSize4.intValue());
        this.ironDepositMinAltitude4 = this.ReadModSettings(TCDefaultValues.ironDepositMinAltitude4.name(), TCDefaultValues.ironDepositMinAltitude4.intValue());
        this.ironDepositMaxAltitude4 = this.ReadModSettings(TCDefaultValues.ironDepositMaxAltitude4.name(), TCDefaultValues.ironDepositMaxAltitude4.intValue());

        this.goldDepositRarity1 = this.ReadModSettings(TCDefaultValues.goldDepositRarity1.name(), TCDefaultValues.goldDepositRarity1.intValue());
        this.goldDepositFrequency1 = this.ReadModSettings(TCDefaultValues.goldDepositFrequency1.name(), TCDefaultValues.goldDepositFrequency1.intValue());
        this.goldDepositSize1 = this.ReadModSettings(TCDefaultValues.goldDepositSize1.name(), TCDefaultValues.goldDepositSize1.intValue());
        this.goldDepositMinAltitude1 = this.ReadModSettings(TCDefaultValues.goldDepositMinAltitude1.name(), TCDefaultValues.goldDepositMinAltitude1.intValue());
        this.goldDepositMaxAltitude1 = this.ReadModSettings(TCDefaultValues.goldDepositMaxAltitude1.name(), TCDefaultValues.goldDepositMaxAltitude1.intValue());
        this.goldDepositRarity2 = this.ReadModSettings(TCDefaultValues.goldDepositRarity2.name(), TCDefaultValues.goldDepositRarity2.intValue());
        this.goldDepositFrequency2 = this.ReadModSettings(TCDefaultValues.goldDepositFrequency2.name(), TCDefaultValues.goldDepositFrequency2.intValue());
        this.goldDepositSize2 = this.ReadModSettings(TCDefaultValues.goldDepositSize2.name(), TCDefaultValues.goldDepositSize2.intValue());
        this.goldDepositMinAltitude2 = this.ReadModSettings(TCDefaultValues.goldDepositMinAltitude2.name(), TCDefaultValues.goldDepositMinAltitude2.intValue());
        this.goldDepositMaxAltitude2 = this.ReadModSettings(TCDefaultValues.goldDepositMaxAltitude2.name(), TCDefaultValues.goldDepositMaxAltitude2.intValue());
        this.goldDepositRarity3 = this.ReadModSettings(TCDefaultValues.goldDepositRarity3.name(), TCDefaultValues.goldDepositRarity3.intValue());
        this.goldDepositFrequency3 = this.ReadModSettings(TCDefaultValues.goldDepositFrequency3.name(), TCDefaultValues.goldDepositFrequency3.intValue());
        this.goldDepositSize3 = this.ReadModSettings(TCDefaultValues.goldDepositSize3.name(), TCDefaultValues.goldDepositSize3.intValue());
        this.goldDepositMinAltitude3 = this.ReadModSettings(TCDefaultValues.goldDepositMinAltitude3.name(), TCDefaultValues.goldDepositMinAltitude3.intValue());
        this.goldDepositMaxAltitude3 = this.ReadModSettings(TCDefaultValues.goldDepositMaxAltitude3.name(), TCDefaultValues.goldDepositMaxAltitude3.intValue());
        this.goldDepositRarity4 = this.ReadModSettings(TCDefaultValues.goldDepositRarity4.name(), TCDefaultValues.goldDepositRarity4.intValue());
        this.goldDepositFrequency4 = this.ReadModSettings(TCDefaultValues.goldDepositFrequency4.name(), TCDefaultValues.goldDepositFrequency4.intValue());
        this.goldDepositSize4 = this.ReadModSettings(TCDefaultValues.goldDepositSize4.name(), TCDefaultValues.goldDepositSize4.intValue());
        this.goldDepositMinAltitude4 = this.ReadModSettings(TCDefaultValues.goldDepositMinAltitude4.name(), TCDefaultValues.goldDepositMinAltitude4.intValue());
        this.goldDepositMaxAltitude4 = this.ReadModSettings(TCDefaultValues.goldDepositMaxAltitude4.name(), TCDefaultValues.goldDepositMaxAltitude4.intValue());

        this.redstoneDepositRarity1 = this.ReadModSettings(TCDefaultValues.redstoneDepositRarity1.name(), TCDefaultValues.redstoneDepositRarity1.intValue());
        this.redstoneDepositFrequency1 = this.ReadModSettings(TCDefaultValues.redstoneDepositFrequency1.name(), TCDefaultValues.redstoneDepositFrequency1.intValue());
        this.redstoneDepositSize1 = this.ReadModSettings(TCDefaultValues.redstoneDepositSize1.name(), TCDefaultValues.redstoneDepositSize1.intValue());
        this.redstoneDepositMinAltitude1 = this.ReadModSettings(TCDefaultValues.redstoneDepositMinAltitude1.name(), TCDefaultValues.redstoneDepositMinAltitude1.intValue());
        this.redstoneDepositMaxAltitude1 = this.ReadModSettings(TCDefaultValues.redstoneDepositMaxAltitude1.name(), TCDefaultValues.redstoneDepositMaxAltitude1.intValue());
        this.redstoneDepositRarity2 = this.ReadModSettings(TCDefaultValues.redstoneDepositRarity2.name(), TCDefaultValues.redstoneDepositRarity2.intValue());
        this.redstoneDepositFrequency2 = this.ReadModSettings(TCDefaultValues.redstoneDepositFrequency2.name(), TCDefaultValues.redstoneDepositFrequency2.intValue());
        this.redstoneDepositSize2 = this.ReadModSettings(TCDefaultValues.redstoneDepositSize2.name(), TCDefaultValues.redstoneDepositSize2.intValue());
        this.redstoneDepositMinAltitude2 = this.ReadModSettings(TCDefaultValues.redstoneDepositMinAltitude2.name(), TCDefaultValues.redstoneDepositMinAltitude2.intValue());
        this.redstoneDepositMaxAltitude2 = this.ReadModSettings(TCDefaultValues.redstoneDepositMaxAltitude2.name(), TCDefaultValues.redstoneDepositMaxAltitude2.intValue());
        this.redstoneDepositRarity3 = this.ReadModSettings(TCDefaultValues.redstoneDepositRarity3.name(), TCDefaultValues.redstoneDepositRarity3.intValue());
        this.redstoneDepositFrequency3 = this.ReadModSettings(TCDefaultValues.redstoneDepositFrequency3.name(), TCDefaultValues.redstoneDepositFrequency3.intValue());
        this.redstoneDepositSize3 = this.ReadModSettings(TCDefaultValues.redstoneDepositSize3.name(), TCDefaultValues.redstoneDepositSize3.intValue());
        this.redstoneDepositMinAltitude3 = this.ReadModSettings(TCDefaultValues.redstoneDepositMinAltitude3.name(), TCDefaultValues.redstoneDepositMinAltitude3.intValue());
        this.redstoneDepositMaxAltitude3 = this.ReadModSettings(TCDefaultValues.redstoneDepositMaxAltitude3.name(), TCDefaultValues.redstoneDepositMaxAltitude3.intValue());
        this.redstoneDepositRarity4 = this.ReadModSettings(TCDefaultValues.redstoneDepositRarity4.name(), TCDefaultValues.redstoneDepositRarity4.intValue());
        this.redstoneDepositFrequency4 = this.ReadModSettings(TCDefaultValues.redstoneDepositFrequency4.name(), TCDefaultValues.redstoneDepositFrequency4.intValue());
        this.redstoneDepositSize4 = this.ReadModSettings(TCDefaultValues.redstoneDepositSize4.name(), TCDefaultValues.redstoneDepositSize4.intValue());
        this.redstoneDepositMinAltitude4 = this.ReadModSettings(TCDefaultValues.redstoneDepositMinAltitude4.name(), TCDefaultValues.redstoneDepositMinAltitude4.intValue());
        this.redstoneDepositMaxAltitude4 = this.ReadModSettings(TCDefaultValues.redstoneDepositMaxAltitude4.name(), TCDefaultValues.redstoneDepositMaxAltitude4.intValue());

        this.diamondDepositRarity1 = this.ReadModSettings(TCDefaultValues.diamondDepositRarity1.name(), TCDefaultValues.diamondDepositRarity1.intValue());
        this.diamondDepositFrequency1 = this.ReadModSettings(TCDefaultValues.diamondDepositFrequency1.name(), TCDefaultValues.diamondDepositFrequency1.intValue());
        this.diamondDepositSize1 = this.ReadModSettings(TCDefaultValues.diamondDepositSize1.name(), TCDefaultValues.diamondDepositSize1.intValue());
        this.diamondDepositMinAltitude1 = this.ReadModSettings(TCDefaultValues.diamondDepositMinAltitude1.name(), TCDefaultValues.diamondDepositMinAltitude1.intValue());
        this.diamondDepositMaxAltitude1 = this.ReadModSettings(TCDefaultValues.diamondDepositMaxAltitude1.name(), TCDefaultValues.diamondDepositMaxAltitude1.intValue());
        this.diamondDepositRarity2 = this.ReadModSettings(TCDefaultValues.diamondDepositRarity2.name(), TCDefaultValues.diamondDepositRarity2.intValue());
        this.diamondDepositFrequency2 = this.ReadModSettings(TCDefaultValues.diamondDepositFrequency2.name(), TCDefaultValues.diamondDepositFrequency2.intValue());
        this.diamondDepositSize2 = this.ReadModSettings(TCDefaultValues.diamondDepositSize2.name(), TCDefaultValues.diamondDepositSize2.intValue());
        this.diamondDepositMinAltitude2 = this.ReadModSettings(TCDefaultValues.diamondDepositMinAltitude2.name(), TCDefaultValues.diamondDepositMinAltitude2.intValue());
        this.diamondDepositMaxAltitude2 = this.ReadModSettings(TCDefaultValues.diamondDepositMaxAltitude2.name(), TCDefaultValues.diamondDepositMaxAltitude2.intValue());
        this.diamondDepositRarity3 = this.ReadModSettings(TCDefaultValues.diamondDepositRarity3.name(), TCDefaultValues.diamondDepositRarity3.intValue());
        this.diamondDepositFrequency3 = this.ReadModSettings(TCDefaultValues.diamondDepositFrequency3.name(), TCDefaultValues.diamondDepositFrequency3.intValue());
        this.diamondDepositSize3 = this.ReadModSettings(TCDefaultValues.diamondDepositSize3.name(), TCDefaultValues.diamondDepositSize3.intValue());
        this.diamondDepositMinAltitude3 = this.ReadModSettings(TCDefaultValues.diamondDepositMinAltitude3.name(), TCDefaultValues.diamondDepositMinAltitude3.intValue());
        this.diamondDepositMaxAltitude3 = this.ReadModSettings(TCDefaultValues.diamondDepositMaxAltitude3.name(), TCDefaultValues.diamondDepositMaxAltitude3.intValue());
        this.diamondDepositRarity4 = this.ReadModSettings(TCDefaultValues.diamondDepositRarity4.name(), TCDefaultValues.diamondDepositRarity4.intValue());
        this.diamondDepositFrequency4 = this.ReadModSettings(TCDefaultValues.diamondDepositFrequency4.name(), TCDefaultValues.diamondDepositFrequency4.intValue());
        this.diamondDepositSize4 = this.ReadModSettings(TCDefaultValues.diamondDepositSize4.name(), TCDefaultValues.diamondDepositSize4.intValue());
        this.diamondDepositMinAltitude4 = this.ReadModSettings(TCDefaultValues.diamondDepositMinAltitude4.name(), TCDefaultValues.diamondDepositMinAltitude4.intValue());
        this.diamondDepositMaxAltitude4 = this.ReadModSettings(TCDefaultValues.diamondDepositMaxAltitude4.name(), TCDefaultValues.diamondDepositMaxAltitude4.intValue());

        this.lapislazuliDepositRarity1 = this.ReadModSettings(TCDefaultValues.lapislazuliDepositRarity1.name(), TCDefaultValues.lapislazuliDepositRarity1.intValue());
        this.lapislazuliDepositFrequency1 = this.ReadModSettings(TCDefaultValues.lapislazuliDepositFrequency1.name(), TCDefaultValues.lapislazuliDepositFrequency1.intValue());
        this.lapislazuliDepositSize1 = this.ReadModSettings(TCDefaultValues.lapislazuliDepositSize1.name(), TCDefaultValues.lapislazuliDepositSize1.intValue());
        this.lapislazuliDepositMinAltitude1 = this.ReadModSettings(TCDefaultValues.lapislazuliDepositMinAltitude1.name(), TCDefaultValues.lapislazuliDepositMinAltitude1.intValue());
        this.lapislazuliDepositMaxAltitude1 = this.ReadModSettings(TCDefaultValues.lapislazuliDepositMaxAltitude1.name(), TCDefaultValues.lapislazuliDepositMaxAltitude1.intValue());
        this.lapislazuliDepositRarity2 = this.ReadModSettings(TCDefaultValues.lapislazuliDepositRarity2.name(), TCDefaultValues.lapislazuliDepositRarity2.intValue());
        this.lapislazuliDepositFrequency2 = this.ReadModSettings(TCDefaultValues.lapislazuliDepositFrequency2.name(), TCDefaultValues.lapislazuliDepositFrequency2.intValue());
        this.lapislazuliDepositSize2 = this.ReadModSettings(TCDefaultValues.lapislazuliDepositSize2.name(), TCDefaultValues.lapislazuliDepositSize2.intValue());
        this.lapislazuliDepositMinAltitude2 = this.ReadModSettings(TCDefaultValues.lapislazuliDepositMinAltitude2.name(), TCDefaultValues.lapislazuliDepositMinAltitude2.intValue());
        this.lapislazuliDepositMaxAltitude2 = this.ReadModSettings(TCDefaultValues.lapislazuliDepositMaxAltitude2.name(), TCDefaultValues.lapislazuliDepositMaxAltitude2.intValue());
        this.lapislazuliDepositRarity3 = this.ReadModSettings(TCDefaultValues.lapislazuliDepositRarity3.name(), TCDefaultValues.lapislazuliDepositRarity3.intValue());
        this.lapislazuliDepositFrequency3 = this.ReadModSettings(TCDefaultValues.lapislazuliDepositFrequency3.name(), TCDefaultValues.lapislazuliDepositFrequency3.intValue());
        this.lapislazuliDepositSize3 = this.ReadModSettings(TCDefaultValues.lapislazuliDepositSize3.name(), TCDefaultValues.lapislazuliDepositSize3.intValue());
        this.lapislazuliDepositMinAltitude3 = this.ReadModSettings(TCDefaultValues.lapislazuliDepositMinAltitude3.name(), TCDefaultValues.lapislazuliDepositMinAltitude3.intValue());
        this.lapislazuliDepositMaxAltitude3 = this.ReadModSettings(TCDefaultValues.lapislazuliDepositMaxAltitude3.name(), TCDefaultValues.lapislazuliDepositMaxAltitude3.intValue());
        this.lapislazuliDepositRarity4 = this.ReadModSettings(TCDefaultValues.lapislazuliDepositRarity4.name(), TCDefaultValues.lapislazuliDepositRarity4.intValue());
        this.lapislazuliDepositFrequency4 = this.ReadModSettings(TCDefaultValues.lapislazuliDepositFrequency4.name(), TCDefaultValues.lapislazuliDepositFrequency4.intValue());
        this.lapislazuliDepositSize4 = this.ReadModSettings(TCDefaultValues.lapislazuliDepositSize4.name(), TCDefaultValues.lapislazuliDepositSize4.intValue());
        this.lapislazuliDepositMinAltitude4 = this.ReadModSettings(TCDefaultValues.lapislazuliDepositMinAltitude4.name(), TCDefaultValues.lapislazuliDepositMinAltitude4.intValue());
        this.lapislazuliDepositMaxAltitude4 = this.ReadModSettings(TCDefaultValues.lapislazuliDepositMaxAltitude4.name(), TCDefaultValues.lapislazuliDepositMaxAltitude4.intValue());


        this.disableNotchPonds = this.ReadModSettings(TCDefaultValues.disableNotchPonds.name(), TCDefaultValues.disableNotchPonds.booleanValue());

        this.notchBiomeTrees = this.ReadModSettings(TCDefaultValues.notchBiomeTrees.name(), TCDefaultValues.notchBiomeTrees.booleanValue());
        this.TreeDensity = this.ReadModSettings(TCDefaultValues.TreeDensity.name(), this.DefaultTrees);

        this.dungeonRarity = this.ReadModSettings(TCDefaultValues.dungeonRarity.name(), TCDefaultValues.dungeonRarity.intValue());
        this.dungeonFrequency = this.ReadModSettings(TCDefaultValues.dungeonFrequency.name(), TCDefaultValues.dungeonFrequency.intValue());
        this.dungeonMinAltitude = this.ReadModSettings(TCDefaultValues.dungeonMinAltitude.name(), TCDefaultValues.dungeonMinAltitude.intValue());
        this.dungeonMaxAltitude = this.ReadModSettings(TCDefaultValues.dungeonMaxAltitude.name(), TCDefaultValues.dungeonMaxAltitude.intValue());


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
                    if (blocks.length != 2)
                        continue;


                    int start = blocks[1].indexOf("(");
                    int end = blocks[1].indexOf(")");
                    if (start != -1 && end != -1)
                    {
                        String[] ranges = blocks[1].substring(start + 1, end).split("-");
                        if (ranges.length != 2)
                            continue;

                        int min = Integer.valueOf(ranges[0]);
                        int max = Integer.valueOf(ranges[1]);
                        min = CheckValue(min, 0, 128);
                        max = CheckValue(max, 0, 128, min);
                        this.replaceHeightMin.put(Integer.valueOf(blocks[0]), min);
                        this.replaceHeightMax.put(Integer.valueOf(blocks[0]), max);
                        this.replaceBlocks.put(Integer.valueOf(blocks[0]), Byte.valueOf(blocks[1].substring(0, start)));
                        continue;


                    }
                    this.replaceHeightMin.put(Integer.valueOf(blocks[0]), 0);
                    this.replaceHeightMax.put(Integer.valueOf(blocks[0]), 128);
                    this.replaceBlocks.put(Integer.valueOf(blocks[0]), Byte.valueOf(blocks[1]));

                }

            } catch (NumberFormatException e)
            {
                System.out.println("Wrong replace settings: '" + this.SettingsCache.get("ReplacedBlocks") + "'");
            }

        }


    }

    private void BuildReplaceMatrix()
    {
        for (int i = 0; i < this.ReplaceMatrixBlocks.length; i++)
        {
            if (this.replaceBlocks.containsKey(i))
            {
                this.ReplaceMatrixBlocks[i] = this.replaceBlocks.get(i);
                this.ReplaceMatrixHeightMin[i] = this.replaceHeightMin.get(i);
                this.ReplaceMatrixHeightMax[i] = this.replaceHeightMax.get(i);
            } else
                this.ReplaceMatrixBlocks[i] = (byte) i;

        }
    }


    protected void WriteConfigSettings() throws IOException
    {
        WriteModTitleSettings(this.Biome.l + " biome config");

        WriteModSettings(TCDefaultValues.biomeChance.name(), this.BiomeChance);

        WriteModSettings(TCDefaultValues.BiomeSurfaceAdd.name(), this.BiomeSurface);
        WriteModSettings(TCDefaultValues.BiomeVolatilityAdd.name(), this.BiomeVolatility);

        WriteModSettings(TCDefaultValues.SurfaceBlock.name(), this.SurfaceBlock);
        WriteModSettings(TCDefaultValues.GroundBlock.name(), this.GroundBlock);


        WriteModTitleSettings("Replace Variable: BlockIdFrom=BlockIdTo(minHeight-maxHeight)");
        WriteModReplaceSettings();


        this.WriteModTitleSettings("Tree Variables");
        this.WriteModSettings(TCDefaultValues.notchBiomeTrees.name(), this.notchBiomeTrees);
        this.WriteModSettings(TCDefaultValues.TreeDensity.name(), this.TreeDensity);


        this.WriteModTitleSettings("Start Deposit Variables :");
        this.WriteModTitleSettings("Above Ground Variables");
        this.WriteModSettings(TCDefaultValues.flowerDepositRarity.name(), this.flowerDepositRarity);
        this.WriteModSettings(TCDefaultValues.flowerDepositFrequency.name(), this.flowerDepositFrequency);
        this.WriteModSettings(TCDefaultValues.flowerDepositMinAltitude.name(), this.flowerDepositMinAltitude);
        this.WriteModSettings(TCDefaultValues.flowerDepositMaxAltitude.name(), this.flowerDepositMaxAltitude);

        this.WriteModSettings(TCDefaultValues.roseDepositRarity.name(), this.roseDepositRarity);
        this.WriteModSettings(TCDefaultValues.roseDepositFrequency.name(), this.roseDepositFrequency);
        this.WriteModSettings(TCDefaultValues.roseDepositMinAltitude.name(), this.roseDepositMinAltitude);
        this.WriteModSettings(TCDefaultValues.roseDepositMaxAltitude.name(), this.roseDepositMaxAltitude);

        this.WriteModSettings(TCDefaultValues.brownMushroomDepositRarity.name(), this.brownMushroomDepositRarity);
        this.WriteModSettings(TCDefaultValues.brownMushroomDepositFrequency.name(), this.brownMushroomDepositFrequency);
        this.WriteModSettings(TCDefaultValues.brownMushroomDepositMinAltitude.name(), this.brownMushroomDepositMinAltitude);
        this.WriteModSettings(TCDefaultValues.brownMushroomDepositMaxAltitude.name(), this.brownMushroomDepositMaxAltitude);

        this.WriteModSettings(TCDefaultValues.redMushroomDepositRarity.name(), this.redMushroomDepositRarity);
        this.WriteModSettings(TCDefaultValues.redMushroomDepositFrequency.name(), this.redMushroomDepositFrequency);
        this.WriteModSettings(TCDefaultValues.redMushroomDepositMinAltitude.name(), this.redMushroomDepositMinAltitude);
        this.WriteModSettings(TCDefaultValues.redMushroomDepositMaxAltitude.name(), this.redMushroomDepositMaxAltitude);

        this.WriteModSettings(TCDefaultValues.reedDepositRarity.name(), this.reedDepositRarity);
        this.WriteModSettings(TCDefaultValues.reedDepositFrequency.name(), this.reedDepositFrequency);
        this.WriteModSettings(TCDefaultValues.reedDepositMinAltitude.name(), this.reedDepositMinAltitude);
        this.WriteModSettings(TCDefaultValues.reedDepositMaxAltitude.name(), this.reedDepositMaxAltitude);

        this.WriteModSettings(TCDefaultValues.pumpkinDepositRarity.name(), this.pumpkinDepositRarity);
        this.WriteModSettings(TCDefaultValues.pumpkinDepositFrequency.name(), this.pumpkinDepositFrequency);
        this.WriteModSettings(TCDefaultValues.pumpkinDepositMinAltitude.name(), this.pumpkinDepositMinAltitude);
        this.WriteModSettings(TCDefaultValues.pumpkinDepositMaxAltitude.name(), this.pumpkinDepositMaxAltitude);

        this.WriteModSettings(TCDefaultValues.cactusDepositRarity.name(), this.cactusDepositRarity);
        this.WriteModSettings(TCDefaultValues.cactusDepositFrequency.name(), this.cactusDepositFrequency);
        this.WriteModSettings(TCDefaultValues.cactusDepositMinAltitude.name(), this.cactusDepositMinAltitude);
        this.WriteModSettings(TCDefaultValues.cactusDepositMaxAltitude.name(), this.cactusDepositMaxAltitude);

        this.WriteModSettings(TCDefaultValues.longGrassDepositRarity.name(), this.longGrassDepositRarity);
        this.WriteModSettings(TCDefaultValues.longGrassDepositFrequency.name(), this.longGrassDepositFrequency);
        this.WriteModSettings(TCDefaultValues.longGrassDepositMinAltitude.name(), this.longGrassDepositMinAltitude);
        this.WriteModSettings(TCDefaultValues.longGrassDepositMaxAltitude.name(), this.longGrassDepositMaxAltitude);

        this.WriteModSettings(TCDefaultValues.deadBushDepositRarity.name(), this.deadBushDepositRarity);
        this.WriteModSettings(TCDefaultValues.deadBushDepositFrequency.name(), this.deadBushDepositFrequency);
        this.WriteModSettings(TCDefaultValues.deadBushDepositMinAltitude.name(), this.deadBushDepositMinAltitude);
        this.WriteModSettings(TCDefaultValues.deadBushDepositMaxAltitude.name(), this.deadBushDepositMaxAltitude);

        this.WriteModTitleSettings("Above/Below Ground Variables");
        this.WriteModSettings(TCDefaultValues.evenWaterSourceDistribution.name(), this.evenWaterSourceDistribution);
        this.WriteModSettings(TCDefaultValues.waterSourceDepositRarity.name(), this.waterSourceDepositRarity);
        this.WriteModSettings(TCDefaultValues.waterSourceDepositFrequency.name(), this.waterSourceDepositFrequency);
        this.WriteModSettings(TCDefaultValues.waterSourceDepositMinAltitude.name(), this.waterSourceDepositMinAltitude);
        this.WriteModSettings(TCDefaultValues.waterSourceDepositMaxAltitude.name(), this.waterSourceDepositMaxAltitude);

        this.WriteModSettings(TCDefaultValues.evenLavaSourceDistribution.name(), this.evenLavaSourceDistribution);
        this.WriteModSettings(TCDefaultValues.lavaSourceDepositRarity.name(), this.lavaSourceDepositRarity);
        this.WriteModSettings(TCDefaultValues.lavaSourceDepositFrequency.name(), this.lavaSourceDepositFrequency);
        this.WriteModSettings(TCDefaultValues.lavaSourceDepositMinAltitude.name(), this.lavaSourceDepositMinAltitude);
        this.WriteModSettings(TCDefaultValues.lavaSourceDepositMaxAltitude.name(), this.lavaSourceDepositMaxAltitude);
        this.WriteModSettings(TCDefaultValues.disableNotchPonds.name(), this.disableNotchPonds);

        this.WriteModTitleSettings("Below Ground Variables");
        this.WriteModSettings(TCDefaultValues.dungeonRarity.name(), this.dungeonRarity);
        this.WriteModSettings(TCDefaultValues.dungeonFrequency.name(), this.dungeonFrequency);
        this.WriteModSettings(TCDefaultValues.dungeonMinAltitude.name(), this.dungeonMinAltitude);
        this.WriteModSettings(TCDefaultValues.dungeonMaxAltitude.name(), this.dungeonMaxAltitude);

        this.WriteModSettings(TCDefaultValues.waterClayDepositRarity.name(), this.waterClayDepositRarity);
        this.WriteModSettings(TCDefaultValues.waterClayDepositFrequency.name(), this.waterClayDepositFrequency);
        this.WriteModSettings(TCDefaultValues.waterClayDepositSize.name(), this.waterClayDepositSize);

        this.WriteModSettings(TCDefaultValues.waterSandDepositRarity.name(), this.waterSandDepositRarity);
        this.WriteModSettings(TCDefaultValues.waterSandDepositFrequency.name(), this.waterSandDepositFrequency);
        this.WriteModSettings(TCDefaultValues.waterSandDepositSize.name(), this.waterSandDepositSize);

        this.WriteModSettings(TCDefaultValues.waterGravelDepositRarity.name(), this.waterGravelDepositRarity);
        this.WriteModSettings(TCDefaultValues.waterGravelDepositFrequency.name(), this.waterGravelDepositFrequency);
        this.WriteModSettings(TCDefaultValues.waterGravelDepositSize.name(), this.waterGravelDepositSize);


        this.WriteModSettings(TCDefaultValues.dirtDepositRarity1.name(), this.dirtDepositRarity1);
        this.WriteModSettings(TCDefaultValues.dirtDepositFrequency1.name(), this.dirtDepositFrequency1);
        this.WriteModSettings(TCDefaultValues.dirtDepositSize1.name(), this.dirtDepositSize1);
        this.WriteModSettings(TCDefaultValues.dirtDepositMinAltitude1.name(), this.dirtDepositMinAltitude1);
        this.WriteModSettings(TCDefaultValues.dirtDepositMaxAltitude1.name(), this.dirtDepositMaxAltitude1);
        this.WriteModSettings(TCDefaultValues.dirtDepositRarity2.name(), this.dirtDepositRarity2);
        this.WriteModSettings(TCDefaultValues.dirtDepositFrequency2.name(), this.dirtDepositFrequency2);
        this.WriteModSettings(TCDefaultValues.dirtDepositSize2.name(), this.dirtDepositSize2);
        this.WriteModSettings(TCDefaultValues.dirtDepositMinAltitude2.name(), this.dirtDepositMinAltitude2);
        this.WriteModSettings(TCDefaultValues.dirtDepositMaxAltitude2.name(), this.dirtDepositMaxAltitude2);
        this.WriteModSettings(TCDefaultValues.dirtDepositRarity3.name(), this.dirtDepositRarity3);
        this.WriteModSettings(TCDefaultValues.dirtDepositFrequency3.name(), this.dirtDepositFrequency3);
        this.WriteModSettings(TCDefaultValues.dirtDepositSize3.name(), this.dirtDepositSize3);
        this.WriteModSettings(TCDefaultValues.dirtDepositMinAltitude3.name(), this.dirtDepositMinAltitude3);
        this.WriteModSettings(TCDefaultValues.dirtDepositMaxAltitude3.name(), this.dirtDepositMaxAltitude3);
        this.WriteModSettings(TCDefaultValues.dirtDepositRarity4.name(), this.dirtDepositRarity4);
        this.WriteModSettings(TCDefaultValues.dirtDepositFrequency4.name(), this.dirtDepositFrequency4);
        this.WriteModSettings(TCDefaultValues.dirtDepositSize4.name(), this.dirtDepositSize4);
        this.WriteModSettings(TCDefaultValues.dirtDepositMinAltitude4.name(), this.dirtDepositMinAltitude4);
        this.WriteModSettings(TCDefaultValues.dirtDepositMaxAltitude4.name(), this.dirtDepositMaxAltitude4);
        this.WriteModSettings(TCDefaultValues.gravelDepositRarity1.name(), this.gravelDepositRarity1);
        this.WriteModSettings(TCDefaultValues.gravelDepositFrequency1.name(), this.gravelDepositFrequency1);
        this.WriteModSettings(TCDefaultValues.gravelDepositSize1.name(), this.gravelDepositSize1);
        this.WriteModSettings(TCDefaultValues.gravelDepositMinAltitude1.name(), this.gravelDepositMinAltitude1);
        this.WriteModSettings(TCDefaultValues.gravelDepositMaxAltitude1.name(), this.gravelDepositMaxAltitude1);
        this.WriteModSettings(TCDefaultValues.gravelDepositRarity2.name(), this.gravelDepositRarity2);
        this.WriteModSettings(TCDefaultValues.gravelDepositFrequency2.name(), this.gravelDepositFrequency2);
        this.WriteModSettings(TCDefaultValues.gravelDepositSize2.name(), this.gravelDepositSize2);
        this.WriteModSettings(TCDefaultValues.gravelDepositMinAltitude2.name(), this.gravelDepositMinAltitude2);
        this.WriteModSettings(TCDefaultValues.gravelDepositMaxAltitude2.name(), this.gravelDepositMaxAltitude2);
        this.WriteModSettings(TCDefaultValues.gravelDepositRarity3.name(), this.gravelDepositRarity3);
        this.WriteModSettings(TCDefaultValues.gravelDepositFrequency3.name(), this.gravelDepositFrequency3);
        this.WriteModSettings(TCDefaultValues.gravelDepositSize3.name(), this.gravelDepositSize3);
        this.WriteModSettings(TCDefaultValues.gravelDepositMinAltitude3.name(), this.gravelDepositMinAltitude3);
        this.WriteModSettings(TCDefaultValues.gravelDepositMaxAltitude3.name(), this.gravelDepositMaxAltitude3);
        this.WriteModSettings(TCDefaultValues.gravelDepositRarity4.name(), this.gravelDepositRarity4);
        this.WriteModSettings(TCDefaultValues.gravelDepositFrequency4.name(), this.gravelDepositFrequency4);
        this.WriteModSettings(TCDefaultValues.gravelDepositSize4.name(), this.gravelDepositSize4);
        this.WriteModSettings(TCDefaultValues.gravelDepositMinAltitude4.name(), this.gravelDepositMinAltitude4);
        this.WriteModSettings(TCDefaultValues.gravelDepositMaxAltitude4.name(), this.gravelDepositMaxAltitude4);
        this.WriteModSettings(TCDefaultValues.clayDepositRarity1.name(), this.clayDepositRarity1);
        this.WriteModSettings(TCDefaultValues.clayDepositFrequency1.name(), this.clayDepositFrequency1);
        this.WriteModSettings(TCDefaultValues.clayDepositSize1.name(), this.clayDepositSize1);
        this.WriteModSettings(TCDefaultValues.clayDepositMinAltitude1.name(), this.clayDepositMinAltitude1);
        this.WriteModSettings(TCDefaultValues.clayDepositMaxAltitude1.name(), this.clayDepositMaxAltitude1);
        this.WriteModSettings(TCDefaultValues.clayDepositRarity2.name(), this.clayDepositRarity2);
        this.WriteModSettings(TCDefaultValues.clayDepositFrequency2.name(), this.clayDepositFrequency2);
        this.WriteModSettings(TCDefaultValues.clayDepositSize2.name(), this.clayDepositSize2);
        this.WriteModSettings(TCDefaultValues.clayDepositMinAltitude2.name(), this.clayDepositMinAltitude2);
        this.WriteModSettings(TCDefaultValues.clayDepositMaxAltitude2.name(), this.clayDepositMaxAltitude2);
        this.WriteModSettings(TCDefaultValues.clayDepositRarity3.name(), this.clayDepositRarity3);
        this.WriteModSettings(TCDefaultValues.clayDepositFrequency3.name(), this.clayDepositFrequency3);
        this.WriteModSettings(TCDefaultValues.clayDepositSize3.name(), this.clayDepositSize3);
        this.WriteModSettings(TCDefaultValues.clayDepositMinAltitude3.name(), this.clayDepositMinAltitude3);
        this.WriteModSettings(TCDefaultValues.clayDepositMaxAltitude3.name(), this.clayDepositMaxAltitude3);
        this.WriteModSettings(TCDefaultValues.clayDepositRarity4.name(), this.clayDepositRarity4);
        this.WriteModSettings(TCDefaultValues.clayDepositFrequency4.name(), this.clayDepositFrequency4);
        this.WriteModSettings(TCDefaultValues.clayDepositSize4.name(), this.clayDepositSize4);
        this.WriteModSettings(TCDefaultValues.clayDepositMinAltitude4.name(), this.clayDepositMinAltitude4);
        this.WriteModSettings(TCDefaultValues.clayDepositMaxAltitude4.name(), this.clayDepositMaxAltitude4);
        this.WriteModSettings(TCDefaultValues.coalDepositRarity1.name(), this.coalDepositRarity1);
        this.WriteModSettings(TCDefaultValues.coalDepositFrequency1.name(), this.coalDepositFrequency1);
        this.WriteModSettings(TCDefaultValues.coalDepositSize1.name(), this.coalDepositSize1);
        this.WriteModSettings(TCDefaultValues.coalDepositMinAltitude1.name(), this.coalDepositMinAltitude1);
        this.WriteModSettings(TCDefaultValues.coalDepositMaxAltitude1.name(), this.coalDepositMaxAltitude1);
        this.WriteModSettings(TCDefaultValues.coalDepositRarity2.name(), this.coalDepositRarity2);
        this.WriteModSettings(TCDefaultValues.coalDepositFrequency2.name(), this.coalDepositFrequency2);
        this.WriteModSettings(TCDefaultValues.coalDepositSize2.name(), this.coalDepositSize2);
        this.WriteModSettings(TCDefaultValues.coalDepositMinAltitude2.name(), this.coalDepositMinAltitude2);
        this.WriteModSettings(TCDefaultValues.coalDepositMaxAltitude2.name(), this.coalDepositMaxAltitude2);
        this.WriteModSettings(TCDefaultValues.coalDepositRarity3.name(), this.coalDepositRarity3);
        this.WriteModSettings(TCDefaultValues.coalDepositFrequency3.name(), this.coalDepositFrequency3);
        this.WriteModSettings(TCDefaultValues.coalDepositSize3.name(), this.coalDepositSize3);
        this.WriteModSettings(TCDefaultValues.coalDepositMinAltitude3.name(), this.coalDepositMinAltitude3);
        this.WriteModSettings(TCDefaultValues.coalDepositMaxAltitude3.name(), this.coalDepositMaxAltitude3);
        this.WriteModSettings(TCDefaultValues.coalDepositRarity4.name(), this.coalDepositRarity4);
        this.WriteModSettings(TCDefaultValues.coalDepositFrequency4.name(), this.coalDepositFrequency4);
        this.WriteModSettings(TCDefaultValues.coalDepositSize4.name(), this.coalDepositSize4);
        this.WriteModSettings(TCDefaultValues.coalDepositMinAltitude4.name(), this.coalDepositMinAltitude4);
        this.WriteModSettings(TCDefaultValues.coalDepositMaxAltitude4.name(), this.coalDepositMaxAltitude4);
        this.WriteModSettings(TCDefaultValues.ironDepositRarity1.name(), this.ironDepositRarity1);
        this.WriteModSettings(TCDefaultValues.ironDepositFrequency1.name(), this.ironDepositFrequency1);
        this.WriteModSettings(TCDefaultValues.ironDepositSize1.name(), this.ironDepositSize1);
        this.WriteModSettings(TCDefaultValues.ironDepositMinAltitude1.name(), this.ironDepositMinAltitude1);
        this.WriteModSettings(TCDefaultValues.ironDepositMaxAltitude1.name(), this.ironDepositMaxAltitude1);
        this.WriteModSettings(TCDefaultValues.ironDepositRarity2.name(), this.ironDepositRarity2);
        this.WriteModSettings(TCDefaultValues.ironDepositFrequency2.name(), this.ironDepositFrequency2);
        this.WriteModSettings(TCDefaultValues.ironDepositSize2.name(), this.ironDepositSize2);
        this.WriteModSettings(TCDefaultValues.ironDepositMinAltitude2.name(), this.ironDepositMinAltitude2);
        this.WriteModSettings(TCDefaultValues.ironDepositMaxAltitude2.name(), this.ironDepositMaxAltitude2);
        this.WriteModSettings(TCDefaultValues.ironDepositRarity3.name(), this.ironDepositRarity3);
        this.WriteModSettings(TCDefaultValues.ironDepositFrequency3.name(), this.ironDepositFrequency3);
        this.WriteModSettings(TCDefaultValues.ironDepositSize3.name(), this.ironDepositSize3);
        this.WriteModSettings(TCDefaultValues.ironDepositMinAltitude3.name(), this.ironDepositMinAltitude3);
        this.WriteModSettings(TCDefaultValues.ironDepositMaxAltitude3.name(), this.ironDepositMaxAltitude3);
        this.WriteModSettings(TCDefaultValues.ironDepositRarity4.name(), this.ironDepositRarity4);
        this.WriteModSettings(TCDefaultValues.ironDepositFrequency4.name(), this.ironDepositFrequency4);
        this.WriteModSettings(TCDefaultValues.ironDepositSize4.name(), this.ironDepositSize4);
        this.WriteModSettings(TCDefaultValues.ironDepositMinAltitude4.name(), this.ironDepositMinAltitude4);
        this.WriteModSettings(TCDefaultValues.ironDepositMaxAltitude4.name(), this.ironDepositMaxAltitude4);
        this.WriteModSettings(TCDefaultValues.goldDepositRarity1.name(), this.goldDepositRarity1);
        this.WriteModSettings(TCDefaultValues.goldDepositFrequency1.name(), this.goldDepositFrequency1);
        this.WriteModSettings(TCDefaultValues.goldDepositSize1.name(), this.goldDepositSize1);
        this.WriteModSettings(TCDefaultValues.goldDepositMinAltitude1.name(), this.goldDepositMinAltitude1);
        this.WriteModSettings(TCDefaultValues.goldDepositMaxAltitude1.name(), this.goldDepositMaxAltitude1);
        this.WriteModSettings(TCDefaultValues.goldDepositRarity2.name(), this.goldDepositRarity2);
        this.WriteModSettings(TCDefaultValues.goldDepositFrequency2.name(), this.goldDepositFrequency2);
        this.WriteModSettings(TCDefaultValues.goldDepositSize2.name(), this.goldDepositSize2);
        this.WriteModSettings(TCDefaultValues.goldDepositMinAltitude2.name(), this.goldDepositMinAltitude2);
        this.WriteModSettings(TCDefaultValues.goldDepositMaxAltitude2.name(), this.goldDepositMaxAltitude2);
        this.WriteModSettings(TCDefaultValues.goldDepositRarity3.name(), this.goldDepositRarity3);
        this.WriteModSettings(TCDefaultValues.goldDepositFrequency3.name(), this.goldDepositFrequency3);
        this.WriteModSettings(TCDefaultValues.goldDepositSize3.name(), this.goldDepositSize3);
        this.WriteModSettings(TCDefaultValues.goldDepositMinAltitude3.name(), this.goldDepositMinAltitude3);
        this.WriteModSettings(TCDefaultValues.goldDepositMaxAltitude3.name(), this.goldDepositMaxAltitude3);
        this.WriteModSettings(TCDefaultValues.goldDepositRarity4.name(), this.goldDepositRarity4);
        this.WriteModSettings(TCDefaultValues.goldDepositFrequency4.name(), this.goldDepositFrequency4);
        this.WriteModSettings(TCDefaultValues.goldDepositSize4.name(), this.goldDepositSize4);
        this.WriteModSettings(TCDefaultValues.goldDepositMinAltitude4.name(), this.goldDepositMinAltitude4);
        this.WriteModSettings(TCDefaultValues.goldDepositMaxAltitude4.name(), this.goldDepositMaxAltitude4);
        this.WriteModSettings(TCDefaultValues.redstoneDepositRarity1.name(), this.redstoneDepositRarity1);
        this.WriteModSettings(TCDefaultValues.redstoneDepositFrequency1.name(), this.redstoneDepositFrequency1);
        this.WriteModSettings(TCDefaultValues.redstoneDepositSize1.name(), this.redstoneDepositSize1);
        this.WriteModSettings(TCDefaultValues.redstoneDepositMinAltitude1.name(), this.redstoneDepositMinAltitude1);
        this.WriteModSettings(TCDefaultValues.redstoneDepositMaxAltitude1.name(), this.redstoneDepositMaxAltitude1);
        this.WriteModSettings(TCDefaultValues.redstoneDepositRarity2.name(), this.redstoneDepositRarity2);
        this.WriteModSettings(TCDefaultValues.redstoneDepositFrequency2.name(), this.redstoneDepositFrequency2);
        this.WriteModSettings(TCDefaultValues.redstoneDepositSize2.name(), this.redstoneDepositSize2);
        this.WriteModSettings(TCDefaultValues.redstoneDepositMinAltitude2.name(), this.redstoneDepositMinAltitude2);
        this.WriteModSettings(TCDefaultValues.redstoneDepositMaxAltitude2.name(), this.redstoneDepositMaxAltitude2);
        this.WriteModSettings(TCDefaultValues.redstoneDepositRarity3.name(), this.redstoneDepositRarity3);
        this.WriteModSettings(TCDefaultValues.redstoneDepositFrequency3.name(), this.redstoneDepositFrequency3);
        this.WriteModSettings(TCDefaultValues.redstoneDepositSize3.name(), this.redstoneDepositSize3);
        this.WriteModSettings(TCDefaultValues.redstoneDepositMinAltitude3.name(), this.redstoneDepositMinAltitude3);
        this.WriteModSettings(TCDefaultValues.redstoneDepositMaxAltitude3.name(), this.redstoneDepositMaxAltitude3);
        this.WriteModSettings(TCDefaultValues.redstoneDepositRarity4.name(), this.redstoneDepositRarity4);
        this.WriteModSettings(TCDefaultValues.redstoneDepositFrequency4.name(), this.redstoneDepositFrequency4);
        this.WriteModSettings(TCDefaultValues.redstoneDepositSize4.name(), this.redstoneDepositSize4);
        this.WriteModSettings(TCDefaultValues.redstoneDepositMinAltitude4.name(), this.redstoneDepositMinAltitude4);
        this.WriteModSettings(TCDefaultValues.redstoneDepositMaxAltitude4.name(), this.redstoneDepositMaxAltitude4);
        this.WriteModSettings(TCDefaultValues.diamondDepositRarity1.name(), this.diamondDepositRarity1);
        this.WriteModSettings(TCDefaultValues.diamondDepositFrequency1.name(), this.diamondDepositFrequency1);
        this.WriteModSettings(TCDefaultValues.diamondDepositSize1.name(), this.diamondDepositSize1);
        this.WriteModSettings(TCDefaultValues.diamondDepositMinAltitude1.name(), this.diamondDepositMinAltitude1);
        this.WriteModSettings(TCDefaultValues.diamondDepositMaxAltitude1.name(), this.diamondDepositMaxAltitude1);
        this.WriteModSettings(TCDefaultValues.diamondDepositRarity2.name(), this.diamondDepositRarity2);
        this.WriteModSettings(TCDefaultValues.diamondDepositFrequency2.name(), this.diamondDepositFrequency2);
        this.WriteModSettings(TCDefaultValues.diamondDepositSize2.name(), this.diamondDepositSize2);
        this.WriteModSettings(TCDefaultValues.diamondDepositMinAltitude2.name(), this.diamondDepositMinAltitude2);
        this.WriteModSettings(TCDefaultValues.diamondDepositMaxAltitude2.name(), this.diamondDepositMaxAltitude2);
        this.WriteModSettings(TCDefaultValues.diamondDepositRarity3.name(), this.diamondDepositRarity3);
        this.WriteModSettings(TCDefaultValues.diamondDepositFrequency3.name(), this.diamondDepositFrequency3);
        this.WriteModSettings(TCDefaultValues.diamondDepositSize3.name(), this.diamondDepositSize3);
        this.WriteModSettings(TCDefaultValues.diamondDepositMinAltitude3.name(), this.diamondDepositMinAltitude3);
        this.WriteModSettings(TCDefaultValues.diamondDepositMaxAltitude3.name(), this.diamondDepositMaxAltitude3);
        this.WriteModSettings(TCDefaultValues.diamondDepositRarity4.name(), this.diamondDepositRarity4);
        this.WriteModSettings(TCDefaultValues.diamondDepositFrequency4.name(), this.diamondDepositFrequency4);
        this.WriteModSettings(TCDefaultValues.diamondDepositSize4.name(), this.diamondDepositSize4);
        this.WriteModSettings(TCDefaultValues.diamondDepositMinAltitude4.name(), this.diamondDepositMinAltitude4);
        this.WriteModSettings(TCDefaultValues.diamondDepositMaxAltitude4.name(), this.diamondDepositMaxAltitude4);
        this.WriteModSettings(TCDefaultValues.lapislazuliDepositRarity1.name(), this.lapislazuliDepositRarity1);
        this.WriteModSettings(TCDefaultValues.lapislazuliDepositFrequency1.name(), this.lapislazuliDepositFrequency1);
        this.WriteModSettings(TCDefaultValues.lapislazuliDepositSize1.name(), this.lapislazuliDepositSize1);
        this.WriteModSettings(TCDefaultValues.lapislazuliDepositMinAltitude1.name(), this.lapislazuliDepositMinAltitude1);
        this.WriteModSettings(TCDefaultValues.lapislazuliDepositMaxAltitude1.name(), this.lapislazuliDepositMaxAltitude1);
        this.WriteModSettings(TCDefaultValues.lapislazuliDepositRarity2.name(), this.lapislazuliDepositRarity2);
        this.WriteModSettings(TCDefaultValues.lapislazuliDepositFrequency2.name(), this.lapislazuliDepositFrequency2);
        this.WriteModSettings(TCDefaultValues.lapislazuliDepositSize2.name(), this.lapislazuliDepositSize2);
        this.WriteModSettings(TCDefaultValues.lapislazuliDepositMinAltitude2.name(), this.lapislazuliDepositMinAltitude2);
        this.WriteModSettings(TCDefaultValues.lapislazuliDepositMaxAltitude2.name(), this.lapislazuliDepositMaxAltitude2);
        this.WriteModSettings(TCDefaultValues.lapislazuliDepositRarity3.name(), this.lapislazuliDepositRarity3);
        this.WriteModSettings(TCDefaultValues.lapislazuliDepositFrequency3.name(), this.lapislazuliDepositFrequency3);
        this.WriteModSettings(TCDefaultValues.lapislazuliDepositSize3.name(), this.lapislazuliDepositSize3);
        this.WriteModSettings(TCDefaultValues.lapislazuliDepositMinAltitude3.name(), this.lapislazuliDepositMinAltitude3);
        this.WriteModSettings(TCDefaultValues.lapislazuliDepositMaxAltitude3.name(), this.lapislazuliDepositMaxAltitude3);
        this.WriteModSettings(TCDefaultValues.lapislazuliDepositRarity4.name(), this.lapislazuliDepositRarity4);
        this.WriteModSettings(TCDefaultValues.lapislazuliDepositFrequency4.name(), this.lapislazuliDepositFrequency4);
        this.WriteModSettings(TCDefaultValues.lapislazuliDepositSize4.name(), this.lapislazuliDepositSize4);
        this.WriteModSettings(TCDefaultValues.lapislazuliDepositMinAltitude4.name(), this.lapislazuliDepositMinAltitude4);
        this.WriteModSettings(TCDefaultValues.lapislazuliDepositMaxAltitude4.name(), this.lapislazuliDepositMaxAltitude4);
    }


    private void WriteModReplaceSettings() throws IOException
    {

        if (this.replaceBlocks.size() == 0)
        {
            this.WriteModSettings("ReplacedBlocks", "None");
            return;
        }
        String output = "";
        Iterator<Map.Entry<Integer, Byte>> i = this.replaceBlocks.entrySet().iterator();
        while (i.hasNext())
        {
            Map.Entry<Integer, Byte> me = i.next();

            output += me.getKey().toString() + "=" + me.getValue().toString();
            int min = this.replaceHeightMin.get(me.getKey());
            int max = this.replaceHeightMax.get(me.getKey());
            if (min != 0 || max != 128)
                output += "(" + min + "-" + max + ")";

            if (i.hasNext())
                output += ",";
        }

        this.WriteModSettings("ReplacedBlocks", output);
    }

    protected void CorrectSettings()
    {
        this.BiomeChance = CheckValue(this.BiomeChance, 0, 20);

        this.dungeonRarity = CheckValue(this.dungeonRarity, 0, 100);
        this.dungeonFrequency = CheckValue(this.dungeonFrequency, 0, 200);
        this.dungeonMinAltitude = CheckValue(this.dungeonMinAltitude, 0, this.worldConfig.ChunkMaxY - 1);
        this.dungeonMaxAltitude = CheckValue(this.dungeonMaxAltitude, 1, this.worldConfig.ChunkMaxY, this.dungeonMinAltitude);

        this.flowerDepositRarity = CheckValue(this.flowerDepositRarity, 0, 100);
        this.flowerDepositFrequency = CheckValue(this.flowerDepositFrequency, 0, 200);
        this.flowerDepositMinAltitude = CheckValue(this.flowerDepositMinAltitude, 0, this.worldConfig.ChunkMaxY - 1);
        this.flowerDepositMaxAltitude = CheckValue(this.flowerDepositMaxAltitude, 1, this.worldConfig.ChunkMaxY, this.flowerDepositMinAltitude);

        this.roseDepositRarity = CheckValue(this.roseDepositRarity, 0, 100);
        this.roseDepositFrequency = CheckValue(this.roseDepositFrequency, 0, 200);
        this.roseDepositMinAltitude = CheckValue(this.roseDepositMinAltitude, 0, this.worldConfig.ChunkMaxY - 1);
        this.roseDepositMaxAltitude = CheckValue(this.roseDepositMaxAltitude, 1, this.worldConfig.ChunkMaxY, this.roseDepositMinAltitude);

        this.brownMushroomDepositRarity = CheckValue(this.brownMushroomDepositRarity, 0, 100);
        this.brownMushroomDepositFrequency = CheckValue(this.brownMushroomDepositFrequency, 0, 200);
        this.brownMushroomDepositMinAltitude = CheckValue(this.brownMushroomDepositMinAltitude, 0, this.worldConfig.ChunkMaxY - 1);
        this.brownMushroomDepositMaxAltitude = CheckValue(this.brownMushroomDepositMaxAltitude, 1, this.worldConfig.ChunkMaxY, this.brownMushroomDepositMinAltitude);

        this.redMushroomDepositRarity = CheckValue(this.redMushroomDepositRarity, 0, 100);
        this.redMushroomDepositFrequency = CheckValue(this.redMushroomDepositFrequency, 0, 200);
        this.redMushroomDepositMinAltitude = CheckValue(this.redMushroomDepositMinAltitude, 0, this.worldConfig.ChunkMaxY - 1);
        this.redMushroomDepositMaxAltitude = CheckValue(this.redMushroomDepositMaxAltitude, 1, this.worldConfig.ChunkMaxY, this.redMushroomDepositMinAltitude);

        this.reedDepositRarity = CheckValue(this.reedDepositRarity, 0, 100);
        this.reedDepositFrequency = CheckValue(this.reedDepositFrequency, 0, 200);
        this.reedDepositMinAltitude = CheckValue(this.reedDepositMinAltitude, 0, this.worldConfig.ChunkMaxY - 1);
        this.reedDepositMaxAltitude = CheckValue(this.reedDepositMaxAltitude, 1, this.worldConfig.ChunkMaxY, this.reedDepositMinAltitude);

        this.pumpkinDepositRarity = CheckValue(this.pumpkinDepositRarity, 0, 100);
        this.pumpkinDepositFrequency = CheckValue(this.pumpkinDepositFrequency, 0, 200);
        this.pumpkinDepositMinAltitude = CheckValue(this.pumpkinDepositMinAltitude, 0, this.worldConfig.ChunkMaxY - 1);
        this.pumpkinDepositMaxAltitude = CheckValue(this.pumpkinDepositMaxAltitude, 1, this.worldConfig.ChunkMaxY, this.pumpkinDepositMinAltitude);

        this.cactusDepositRarity = CheckValue(this.cactusDepositRarity, 0, 100);
        this.cactusDepositFrequency = CheckValue(this.cactusDepositFrequency, 0, 200);
        this.cactusDepositMinAltitude = CheckValue(this.cactusDepositMinAltitude, 0, this.worldConfig.ChunkMaxY - 1);
        this.cactusDepositMaxAltitude = CheckValue(this.cactusDepositMaxAltitude, 1, this.worldConfig.ChunkMaxY, this.cactusDepositMinAltitude);

        this.longGrassDepositRarity = CheckValue(this.longGrassDepositRarity, 0, 100);
        this.longGrassDepositFrequency = CheckValue(this.longGrassDepositFrequency, 0, 200);
        this.longGrassDepositMinAltitude = CheckValue(this.longGrassDepositMinAltitude, 0, this.worldConfig.ChunkMaxY - 1);
        this.longGrassDepositMaxAltitude = CheckValue(this.longGrassDepositMaxAltitude, 1, this.worldConfig.ChunkMaxY, this.longGrassDepositMinAltitude);

        this.deadBushDepositRarity = CheckValue(this.deadBushDepositRarity, 0, 100);
        this.deadBushDepositFrequency = CheckValue(this.deadBushDepositFrequency, 0, 200);
        this.deadBushDepositMinAltitude = CheckValue(this.deadBushDepositMinAltitude, 0, this.worldConfig.ChunkMaxY - 1);
        this.deadBushDepositMaxAltitude = CheckValue(this.deadBushDepositMaxAltitude, 1, this.worldConfig.ChunkMaxY, this.deadBushDepositMinAltitude);

        this.waterSourceDepositRarity = CheckValue(this.waterSourceDepositRarity, 0, 100);
        this.waterSourceDepositFrequency = CheckValue(this.waterSourceDepositFrequency, 0, 200);
        this.waterSourceDepositMinAltitude = CheckValue(this.waterSourceDepositMinAltitude, 0, this.worldConfig.ChunkMaxY - 1);
        this.waterSourceDepositMaxAltitude = CheckValue(this.waterSourceDepositMaxAltitude, 1, this.worldConfig.ChunkMaxY, this.waterSourceDepositMinAltitude);

        this.lavaSourceDepositRarity = CheckValue(this.lavaSourceDepositRarity, 0, 100);
        this.lavaSourceDepositFrequency = CheckValue(this.lavaSourceDepositFrequency, 0, 200);
        this.lavaSourceDepositMinAltitude = CheckValue(this.lavaSourceDepositMinAltitude, 0, this.worldConfig.ChunkMaxY - 1);
        this.lavaSourceDepositMaxAltitude = CheckValue(this.lavaSourceDepositMaxAltitude, 1, this.worldConfig.ChunkMaxY, this.lavaSourceDepositMinAltitude);

        this.waterClayDepositRarity = CheckValue(this.waterClayDepositRarity, 0, 100);
        this.waterClayDepositFrequency = CheckValue(this.waterClayDepositFrequency, 0, 200);

        this.waterSandDepositRarity = CheckValue(this.waterSandDepositRarity, 0, 100);
        this.waterSandDepositFrequency = CheckValue(this.waterSandDepositFrequency, 0, 200);

        this.waterGravelDepositRarity = CheckValue(this.waterGravelDepositRarity, 0, 100);
        this.waterGravelDepositFrequency = CheckValue(this.waterGravelDepositFrequency, 0, 200);


        this.dirtDepositRarity1 = CheckValue(this.dirtDepositRarity1, 0, 100);
        this.dirtDepositFrequency1 = CheckValue(this.dirtDepositFrequency1, 0, 200);
        this.dirtDepositMinAltitude1 = CheckValue(this.dirtDepositMinAltitude1, 0, this.worldConfig.ChunkMaxY - 1);
        this.dirtDepositMaxAltitude1 = CheckValue(this.dirtDepositMaxAltitude1, 1, this.worldConfig.ChunkMaxY, this.dirtDepositMinAltitude1);
        this.dirtDepositRarity2 = CheckValue(this.dirtDepositRarity2, 0, 100);
        this.dirtDepositFrequency2 = CheckValue(this.dirtDepositFrequency2, 0, 200);
        this.dirtDepositMinAltitude2 = CheckValue(this.dirtDepositMinAltitude2, 0, this.worldConfig.ChunkMaxY - 1);
        this.dirtDepositMaxAltitude2 = CheckValue(this.dirtDepositMaxAltitude2, 1, this.worldConfig.ChunkMaxY, this.dirtDepositMinAltitude2);
        this.dirtDepositRarity3 = CheckValue(this.dirtDepositRarity3, 0, 100);
        this.dirtDepositFrequency3 = CheckValue(this.dirtDepositFrequency3, 0, 200);
        this.dirtDepositMinAltitude3 = CheckValue(this.dirtDepositMinAltitude3, 0, this.worldConfig.ChunkMaxY - 1);
        this.dirtDepositMaxAltitude3 = CheckValue(this.dirtDepositMaxAltitude3, 1, this.worldConfig.ChunkMaxY, this.dirtDepositMinAltitude3);
        this.dirtDepositRarity4 = CheckValue(this.dirtDepositRarity4, 0, 100);
        this.dirtDepositFrequency4 = CheckValue(this.dirtDepositFrequency4, 0, 200);
        this.dirtDepositMinAltitude4 = CheckValue(this.dirtDepositMinAltitude4, 0, this.worldConfig.ChunkMaxY - 1);
        this.dirtDepositMaxAltitude4 = CheckValue(this.dirtDepositMaxAltitude4, 1, this.worldConfig.ChunkMaxY, this.dirtDepositMinAltitude4);

        this.gravelDepositRarity1 = CheckValue(this.gravelDepositRarity1, 0, 100);
        this.gravelDepositFrequency1 = CheckValue(this.gravelDepositFrequency1, 0, 200);
        this.gravelDepositMinAltitude1 = CheckValue(this.gravelDepositMinAltitude1, 0, this.worldConfig.ChunkMaxY - 1);
        this.gravelDepositMaxAltitude1 = CheckValue(this.gravelDepositMaxAltitude1, 1, this.worldConfig.ChunkMaxY, this.gravelDepositMinAltitude1);
        this.gravelDepositRarity2 = CheckValue(this.gravelDepositRarity2, 0, 100);
        this.gravelDepositFrequency2 = CheckValue(this.gravelDepositFrequency2, 0, 200);
        this.gravelDepositMinAltitude2 = CheckValue(this.gravelDepositMinAltitude2, 0, this.worldConfig.ChunkMaxY - 1);
        this.gravelDepositMaxAltitude2 = CheckValue(this.gravelDepositMaxAltitude2, 1, this.worldConfig.ChunkMaxY, this.gravelDepositMinAltitude2);
        this.gravelDepositRarity3 = CheckValue(this.gravelDepositRarity3, 0, 100);
        this.gravelDepositFrequency3 = CheckValue(this.gravelDepositFrequency3, 0, 200);
        this.gravelDepositMinAltitude3 = CheckValue(this.gravelDepositMinAltitude3, 0, this.worldConfig.ChunkMaxY - 1);
        this.gravelDepositMaxAltitude3 = CheckValue(this.gravelDepositMaxAltitude3, 1, this.worldConfig.ChunkMaxY, this.gravelDepositMinAltitude3);
        this.gravelDepositRarity4 = CheckValue(this.gravelDepositRarity4, 0, 100);
        this.gravelDepositFrequency4 = CheckValue(this.gravelDepositFrequency4, 0, 200);
        this.gravelDepositMinAltitude4 = CheckValue(this.gravelDepositMinAltitude4, 0, this.worldConfig.ChunkMaxY - 1);
        this.gravelDepositMaxAltitude4 = CheckValue(this.gravelDepositMaxAltitude4, 1, this.worldConfig.ChunkMaxY, this.gravelDepositMinAltitude4);

        this.clayDepositRarity1 = CheckValue(this.clayDepositRarity1, 0, 100);
        this.clayDepositFrequency1 = CheckValue(this.clayDepositFrequency1, 0, 200);
        this.clayDepositMinAltitude1 = CheckValue(this.clayDepositMinAltitude1, 0, this.worldConfig.ChunkMaxY - 1);
        this.clayDepositMaxAltitude1 = CheckValue(this.clayDepositMaxAltitude1, 1, this.worldConfig.ChunkMaxY, this.clayDepositMinAltitude1);
        this.clayDepositRarity2 = CheckValue(this.clayDepositRarity2, 0, 100);
        this.clayDepositFrequency2 = CheckValue(this.clayDepositFrequency2, 0, 200);
        this.clayDepositMinAltitude2 = CheckValue(this.clayDepositMinAltitude2, 0, this.worldConfig.ChunkMaxY - 1);
        this.clayDepositMaxAltitude2 = CheckValue(this.clayDepositMaxAltitude2, 1, this.worldConfig.ChunkMaxY, this.clayDepositMinAltitude2);
        this.clayDepositRarity3 = CheckValue(this.clayDepositRarity3, 0, 100);
        this.clayDepositFrequency3 = CheckValue(this.clayDepositFrequency3, 0, 200);
        this.clayDepositMinAltitude3 = CheckValue(this.clayDepositMinAltitude3, 0, this.worldConfig.ChunkMaxY - 1);
        this.clayDepositMaxAltitude3 = CheckValue(this.clayDepositMaxAltitude3, 1, this.worldConfig.ChunkMaxY, this.clayDepositMinAltitude3);
        this.clayDepositRarity4 = CheckValue(this.clayDepositRarity4, 0, 100);
        this.clayDepositFrequency4 = CheckValue(this.clayDepositFrequency4, 0, 200);
        this.clayDepositMinAltitude4 = CheckValue(this.clayDepositMinAltitude4, 0, this.worldConfig.ChunkMaxY - 1);
        this.clayDepositMaxAltitude4 = CheckValue(this.clayDepositMaxAltitude4, 1, this.worldConfig.ChunkMaxY, this.clayDepositMinAltitude4);

        this.coalDepositRarity1 = CheckValue(this.coalDepositRarity1, 0, 100);
        this.coalDepositFrequency1 = CheckValue(this.coalDepositFrequency1, 0, 200);
        this.coalDepositMinAltitude1 = CheckValue(this.coalDepositMinAltitude1, 0, this.worldConfig.ChunkMaxY - 1);
        this.coalDepositMaxAltitude1 = CheckValue(this.coalDepositMaxAltitude1, 1, this.worldConfig.ChunkMaxY, this.coalDepositMinAltitude1);
        this.coalDepositRarity2 = CheckValue(this.coalDepositRarity2, 0, 100);
        this.coalDepositFrequency2 = CheckValue(this.coalDepositFrequency2, 0, 200);
        this.coalDepositMinAltitude2 = CheckValue(this.coalDepositMinAltitude2, 0, this.worldConfig.ChunkMaxY - 1);
        this.coalDepositMaxAltitude2 = CheckValue(this.coalDepositMaxAltitude2, 1, this.worldConfig.ChunkMaxY, this.coalDepositMinAltitude2);
        this.coalDepositRarity3 = CheckValue(this.coalDepositRarity3, 0, 100);
        this.coalDepositFrequency3 = CheckValue(this.coalDepositFrequency3, 0, 200);
        this.coalDepositMinAltitude3 = CheckValue(this.coalDepositMinAltitude3, 0, this.worldConfig.ChunkMaxY - 1);
        this.coalDepositMaxAltitude3 = CheckValue(this.coalDepositMaxAltitude3, 1, this.worldConfig.ChunkMaxY, this.coalDepositMinAltitude3);
        this.coalDepositRarity4 = CheckValue(this.coalDepositRarity4, 0, 100);
        this.coalDepositFrequency4 = CheckValue(this.coalDepositFrequency4, 0, 200);
        this.coalDepositMinAltitude4 = CheckValue(this.coalDepositMinAltitude4, 0, this.worldConfig.ChunkMaxY - 1);
        this.coalDepositMaxAltitude4 = CheckValue(this.coalDepositMaxAltitude4, 1, this.worldConfig.ChunkMaxY, this.coalDepositMinAltitude4);

        this.ironDepositRarity1 = CheckValue(this.ironDepositRarity1, 0, 100);
        this.ironDepositFrequency1 = CheckValue(this.ironDepositFrequency1, 0, 200);
        this.ironDepositMinAltitude1 = CheckValue(this.ironDepositMinAltitude1, 0, this.worldConfig.ChunkMaxY - 1);
        this.ironDepositMaxAltitude1 = CheckValue(this.ironDepositMaxAltitude1, 1, this.worldConfig.ChunkMaxY, this.ironDepositMinAltitude1);
        this.ironDepositRarity2 = CheckValue(this.ironDepositRarity2, 0, 100);
        this.ironDepositFrequency2 = CheckValue(this.ironDepositFrequency2, 0, 200);
        this.ironDepositMinAltitude2 = CheckValue(this.ironDepositMinAltitude2, 0, this.worldConfig.ChunkMaxY - 1);
        this.ironDepositMaxAltitude2 = CheckValue(this.ironDepositMaxAltitude2, 1, this.worldConfig.ChunkMaxY, this.ironDepositMinAltitude2);
        this.ironDepositRarity3 = CheckValue(this.ironDepositRarity3, 0, 100);
        this.ironDepositFrequency3 = CheckValue(this.ironDepositFrequency3, 0, 200);
        this.ironDepositMinAltitude3 = CheckValue(this.ironDepositMinAltitude3, 0, this.worldConfig.ChunkMaxY - 1);
        this.ironDepositMaxAltitude3 = CheckValue(this.ironDepositMaxAltitude3, 1, this.worldConfig.ChunkMaxY, this.ironDepositMinAltitude3);
        this.ironDepositRarity4 = CheckValue(this.ironDepositRarity4, 0, 100);
        this.ironDepositFrequency4 = CheckValue(this.ironDepositFrequency4, 0, 200);
        this.ironDepositMinAltitude4 = CheckValue(this.ironDepositMinAltitude4, 0, this.worldConfig.ChunkMaxY - 1);
        this.ironDepositMaxAltitude4 = CheckValue(this.ironDepositMaxAltitude4, 1, this.worldConfig.ChunkMaxY, this.ironDepositMinAltitude4);

        this.goldDepositRarity1 = CheckValue(this.goldDepositRarity1, 0, 100);
        this.goldDepositFrequency1 = CheckValue(this.goldDepositFrequency1, 0, 200);
        this.goldDepositMinAltitude1 = CheckValue(this.goldDepositMinAltitude1, 0, this.worldConfig.ChunkMaxY - 1);
        this.goldDepositMaxAltitude1 = CheckValue(this.goldDepositMaxAltitude1, 1, this.worldConfig.ChunkMaxY, this.goldDepositMinAltitude1);
        this.goldDepositRarity2 = CheckValue(this.goldDepositRarity2, 0, 100);
        this.goldDepositFrequency2 = CheckValue(this.goldDepositFrequency2, 0, 200);
        this.goldDepositMinAltitude2 = CheckValue(this.goldDepositMinAltitude2, 0, this.worldConfig.ChunkMaxY - 1);
        this.goldDepositMaxAltitude2 = CheckValue(this.goldDepositMaxAltitude2, 1, this.worldConfig.ChunkMaxY, this.goldDepositMinAltitude2);
        this.goldDepositRarity3 = CheckValue(this.goldDepositRarity3, 0, 100);
        this.goldDepositFrequency3 = CheckValue(this.goldDepositFrequency3, 0, 200);
        this.goldDepositMinAltitude3 = CheckValue(this.goldDepositMinAltitude3, 0, this.worldConfig.ChunkMaxY - 1);
        this.goldDepositMaxAltitude3 = CheckValue(this.goldDepositMaxAltitude3, 1, this.worldConfig.ChunkMaxY, this.goldDepositMinAltitude3);
        this.goldDepositRarity4 = CheckValue(this.goldDepositRarity4, 0, 100);
        this.goldDepositFrequency4 = CheckValue(this.goldDepositFrequency4, 0, 200);
        this.goldDepositMinAltitude4 = CheckValue(this.goldDepositMinAltitude4, 0, this.worldConfig.ChunkMaxY - 1);
        this.goldDepositMaxAltitude4 = CheckValue(this.goldDepositMaxAltitude4, 1, this.worldConfig.ChunkMaxY, this.goldDepositMinAltitude4);

        this.redstoneDepositRarity1 = CheckValue(this.redstoneDepositRarity1, 0, 100);
        this.redstoneDepositFrequency1 = CheckValue(this.redstoneDepositFrequency1, 0, 200);
        this.redstoneDepositMinAltitude1 = CheckValue(this.redstoneDepositMinAltitude1, 0, this.worldConfig.ChunkMaxY - 1);
        this.redstoneDepositMaxAltitude1 = CheckValue(this.redstoneDepositMaxAltitude1, 1, this.worldConfig.ChunkMaxY, this.redstoneDepositMinAltitude1);
        this.redstoneDepositRarity2 = CheckValue(this.redstoneDepositRarity2, 0, 100);
        this.redstoneDepositFrequency2 = CheckValue(this.redstoneDepositFrequency2, 0, 200);
        this.redstoneDepositMinAltitude2 = CheckValue(this.redstoneDepositMinAltitude2, 0, this.worldConfig.ChunkMaxY - 1);
        this.redstoneDepositMaxAltitude2 = CheckValue(this.redstoneDepositMaxAltitude2, 1, this.worldConfig.ChunkMaxY, this.redstoneDepositMinAltitude2);
        this.redstoneDepositRarity3 = CheckValue(this.redstoneDepositRarity3, 0, 100);
        this.redstoneDepositFrequency3 = CheckValue(this.redstoneDepositFrequency3, 0, 200);
        this.redstoneDepositMinAltitude3 = CheckValue(this.redstoneDepositMinAltitude3, 0, this.worldConfig.ChunkMaxY - 1);
        this.redstoneDepositMaxAltitude3 = CheckValue(this.redstoneDepositMaxAltitude3, 1, this.worldConfig.ChunkMaxY, this.redstoneDepositMinAltitude3);
        this.redstoneDepositRarity4 = CheckValue(this.redstoneDepositRarity4, 0, 100);
        this.redstoneDepositFrequency4 = CheckValue(this.redstoneDepositFrequency4, 0, 200);
        this.redstoneDepositMinAltitude4 = CheckValue(this.redstoneDepositMinAltitude4, 0, this.worldConfig.ChunkMaxY - 1);
        this.redstoneDepositMaxAltitude4 = CheckValue(this.redstoneDepositMaxAltitude4, 1, this.worldConfig.ChunkMaxY, this.redstoneDepositMinAltitude4);

        this.diamondDepositRarity1 = CheckValue(this.diamondDepositRarity1, 0, 100);
        this.diamondDepositFrequency1 = CheckValue(this.diamondDepositFrequency1, 0, 200);
        this.diamondDepositMinAltitude1 = CheckValue(this.diamondDepositMinAltitude1, 0, this.worldConfig.ChunkMaxY - 1);
        this.diamondDepositMaxAltitude1 = CheckValue(this.diamondDepositMaxAltitude1, 1, this.worldConfig.ChunkMaxY, this.diamondDepositMinAltitude1);
        this.diamondDepositRarity2 = CheckValue(this.diamondDepositRarity2, 0, 100);
        this.diamondDepositFrequency2 = CheckValue(this.diamondDepositFrequency2, 0, 200);
        this.diamondDepositMinAltitude2 = CheckValue(this.diamondDepositMinAltitude2, 0, this.worldConfig.ChunkMaxY - 1);
        this.diamondDepositMaxAltitude2 = CheckValue(this.diamondDepositMaxAltitude2, 1, this.worldConfig.ChunkMaxY, this.diamondDepositMinAltitude2);
        this.diamondDepositRarity3 = CheckValue(this.diamondDepositRarity3, 0, 100);
        this.diamondDepositFrequency3 = CheckValue(this.diamondDepositFrequency3, 0, 200);
        this.diamondDepositMinAltitude3 = CheckValue(this.diamondDepositMinAltitude3, 0, this.worldConfig.ChunkMaxY - 1);
        this.diamondDepositMaxAltitude3 = CheckValue(this.diamondDepositMaxAltitude3, 1, this.worldConfig.ChunkMaxY, this.diamondDepositMinAltitude3);
        this.diamondDepositRarity4 = CheckValue(this.diamondDepositRarity4, 0, 100);
        this.diamondDepositFrequency4 = CheckValue(this.diamondDepositFrequency4, 0, 200);
        this.diamondDepositMinAltitude4 = CheckValue(this.diamondDepositMinAltitude4, 0, this.worldConfig.ChunkMaxY - 1);
        this.diamondDepositMaxAltitude4 = CheckValue(this.diamondDepositMaxAltitude4, 1, this.worldConfig.ChunkMaxY, this.diamondDepositMinAltitude4);

        this.lapislazuliDepositRarity1 = CheckValue(this.lapislazuliDepositRarity1, 0, 100);
        this.lapislazuliDepositFrequency1 = CheckValue(this.lapislazuliDepositFrequency1, 0, 200);
        this.lapislazuliDepositMinAltitude1 = CheckValue(this.lapislazuliDepositMinAltitude1, 0, this.worldConfig.ChunkMaxY - 1);
        this.lapislazuliDepositMaxAltitude1 = CheckValue(this.lapislazuliDepositMaxAltitude1, 1, this.worldConfig.ChunkMaxY, this.lapislazuliDepositMinAltitude1);
        this.lapislazuliDepositRarity2 = CheckValue(this.lapislazuliDepositRarity2, 0, 100);
        this.lapislazuliDepositFrequency2 = CheckValue(this.lapislazuliDepositFrequency2, 0, 200);
        this.lapislazuliDepositMinAltitude2 = CheckValue(this.lapislazuliDepositMinAltitude2, 0, this.worldConfig.ChunkMaxY - 1);
        this.lapislazuliDepositMaxAltitude2 = CheckValue(this.lapislazuliDepositMaxAltitude2, 1, this.worldConfig.ChunkMaxY, this.lapislazuliDepositMinAltitude2);
        this.lapislazuliDepositRarity3 = CheckValue(this.lapislazuliDepositRarity3, 0, 100);
        this.lapislazuliDepositFrequency3 = CheckValue(this.lapislazuliDepositFrequency3, 0, 200);
        this.lapislazuliDepositMinAltitude3 = CheckValue(this.lapislazuliDepositMinAltitude3, 0, this.worldConfig.ChunkMaxY - 1);
        this.lapislazuliDepositMaxAltitude3 = CheckValue(this.lapislazuliDepositMaxAltitude3, 1, this.worldConfig.ChunkMaxY, this.lapislazuliDepositMinAltitude3);
        this.lapislazuliDepositRarity4 = CheckValue(this.lapislazuliDepositRarity4, 0, 100);
        this.lapislazuliDepositFrequency4 = CheckValue(this.lapislazuliDepositFrequency4, 0, 200);
        this.lapislazuliDepositMinAltitude4 = CheckValue(this.lapislazuliDepositMinAltitude4, 0, this.worldConfig.ChunkMaxY - 1);
        this.lapislazuliDepositMaxAltitude4 = CheckValue(this.lapislazuliDepositMaxAltitude4, 1, this.worldConfig.ChunkMaxY, this.lapislazuliDepositMinAltitude4);


    }


    private int DefaultTrees = 0;
    private int DefaultFlowers = 2;
    private int DefaultGrass = 1;
    private int DefaultDeadBrush = 0;
    private int DefaultMushroom = 0;
    private int DefaultReed = 0;
    private int DefaultCactus = 0;
    private int DefaultClay = 1;
    private float DefaultBiomeSurface = 0.1F;
    private float DefaultBiomeVolatility = 0.3F;
    private byte DefaultSurfaceBlock = (byte) Block.GRASS.id;
    private byte DefaultGroundBlock = (byte) Block.DIRT.id;
    private int DefaultBiomeChance = 1;


    private void InitDefaults()
    {
        this.DefaultBiomeSurface = this.Biome.q;
        this.DefaultBiomeVolatility = this.Biome.r;
        this.DefaultSurfaceBlock = this.Biome.n;
        this.DefaultGroundBlock = this.Biome.o;

        switch (this.Biome.y)
        {
            case 1:
            {
                this.DefaultTrees = -999;
                this.DefaultFlowers = 4;
                this.DefaultGrass = 10;
                break;
            }
            case 2:
            {
                this.DefaultTrees = -999;
                this.DefaultDeadBrush = 2;
                this.DefaultReed = 50;
                this.DefaultCactus = 10;
                break;
            }
            case 4:
            {
                this.DefaultTrees = 10;
                this.DefaultGrass = 2;
                break;
            }
            case 5:
            {
                this.DefaultTrees = 10;
                this.DefaultGrass = 1;
                break;
            }
            case 6:
            {
                this.DefaultTrees = 2;
                this.DefaultFlowers = -999;
                this.DefaultDeadBrush = 1;
                this.DefaultMushroom = 8;
                this.DefaultReed = 10;
                this.DefaultClay = 1;
                break;
            }
            case 7:
            case 8:
            case 9:
            case 0:
            {
                this.DefaultBiomeChance = 0;
                break;
            }

        }

    }

}
