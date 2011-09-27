package com.Khorn.PTMBukkit;

import com.Khorn.PTMBukkit.Util.ConfigFile;
import net.minecraft.server.BiomeBase;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BiomeConfig extends ConfigFile
{

    public HashMap<Integer, Byte> replaceBlocks = new HashMap<Integer, Byte>();
    public byte[] ReplaceBlocksMatrix = new byte[256];

    public int BiomeChance;


    //Surface config
    public float BiomeSurface;
    public float BiomeVolatility;

    //Todo height control. !!!


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
    private static int ChunkMaxY = 128;


    public BiomeConfig(File settingsDir, BiomeBase biome)
    {

        this.Biome = biome;
        InitDefaults();

        File settingsFile = new File(settingsDir, this.Biome.l + PTMDefaultValues.WorldBiomeConfigName.stringValue());
        this.ReadSettingsFile(settingsFile);
        this.ReadConfigSettings();

        this.CorrectSettings();

        this.WriteSettingsFile(settingsFile);

        BuildReplaceMatrix();


    }
    // Todo file name


    protected void ReadConfigSettings()
    {


        this.BiomeChance = ReadModSettings(PTMDefaultValues.biomeChance.name(), PTMDefaultValues.biomeChance.intValue());

        this.evenWaterSourceDistribution = this.ReadModSettings(PTMDefaultValues.evenWaterSourceDistribution.name(), PTMDefaultValues.evenWaterSourceDistribution.booleanValue());
        this.evenLavaSourceDistribution = this.ReadModSettings(PTMDefaultValues.evenLavaSourceDistribution.name(), PTMDefaultValues.evenLavaSourceDistribution.booleanValue());

        this.BiomeSurface = this.ReadModSettings(PTMDefaultValues.BiomeSurfaceAdd.name(), this.DefaultBiomeSurface);
        this.BiomeVolatility = this.ReadModSettings(PTMDefaultValues.BiomeVolatilityAdd.name(), this.DefaultBiomeVolatility);
        //Todo Add height control
        //ReadHeightSettings();


        this.flowerDepositRarity = this.ReadModSettings(PTMDefaultValues.flowerDepositRarity.name(), PTMDefaultValues.flowerDepositRarity.intValue());
        this.flowerDepositFrequency = this.ReadModSettings(PTMDefaultValues.flowerDepositFrequency.name(), this.DefaultFlowers);
        this.flowerDepositMinAltitude = this.ReadModSettings(PTMDefaultValues.flowerDepositMinAltitude.name(), PTMDefaultValues.flowerDepositMinAltitude.intValue());
        this.flowerDepositMaxAltitude = this.ReadModSettings(PTMDefaultValues.flowerDepositMaxAltitude.name(), PTMDefaultValues.flowerDepositMaxAltitude.intValue());

        this.roseDepositRarity = this.ReadModSettings(PTMDefaultValues.roseDepositRarity.name(), PTMDefaultValues.roseDepositRarity.intValue());
        this.roseDepositFrequency = this.ReadModSettings(PTMDefaultValues.roseDepositFrequency.name(), this.DefaultFlowers);
        this.roseDepositMinAltitude = this.ReadModSettings(PTMDefaultValues.roseDepositMinAltitude.name(), PTMDefaultValues.roseDepositMinAltitude.intValue());
        this.roseDepositMaxAltitude = this.ReadModSettings(PTMDefaultValues.roseDepositMaxAltitude.name(), PTMDefaultValues.roseDepositMaxAltitude.intValue());

        this.brownMushroomDepositRarity = this.ReadModSettings(PTMDefaultValues.brownMushroomDepositRarity.name(), PTMDefaultValues.brownMushroomDepositRarity.intValue());
        this.brownMushroomDepositFrequency = this.ReadModSettings(PTMDefaultValues.brownMushroomDepositFrequency.name(), this.DefaultMushroom);
        this.brownMushroomDepositMinAltitude = this.ReadModSettings(PTMDefaultValues.brownMushroomDepositMinAltitude.name(), PTMDefaultValues.brownMushroomDepositMinAltitude.intValue());
        this.brownMushroomDepositMaxAltitude = this.ReadModSettings(PTMDefaultValues.brownMushroomDepositMaxAltitude.name(), PTMDefaultValues.brownMushroomDepositMaxAltitude.intValue());

        this.redMushroomDepositRarity = this.ReadModSettings(PTMDefaultValues.redMushroomDepositRarity.name(), PTMDefaultValues.redMushroomDepositRarity.intValue());
        this.redMushroomDepositFrequency = this.ReadModSettings(PTMDefaultValues.redMushroomDepositFrequency.name(), this.DefaultMushroom);
        this.redMushroomDepositMinAltitude = this.ReadModSettings(PTMDefaultValues.redMushroomDepositMinAltitude.name(), PTMDefaultValues.redMushroomDepositMinAltitude.intValue());
        this.redMushroomDepositMaxAltitude = this.ReadModSettings(PTMDefaultValues.redMushroomDepositMaxAltitude.name(), PTMDefaultValues.redMushroomDepositMaxAltitude.intValue());

        this.reedDepositRarity = this.ReadModSettings(PTMDefaultValues.reedDepositRarity.name(), PTMDefaultValues.reedDepositRarity.intValue());
        this.reedDepositFrequency = this.ReadModSettings(PTMDefaultValues.reedDepositFrequency.name(), this.DefaultReed);
        this.reedDepositMinAltitude = this.ReadModSettings(PTMDefaultValues.reedDepositMinAltitude.name(), PTMDefaultValues.reedDepositMinAltitude.intValue());
        this.reedDepositMaxAltitude = this.ReadModSettings(PTMDefaultValues.reedDepositMaxAltitude.name(), PTMDefaultValues.reedDepositMaxAltitude.intValue());

        this.pumpkinDepositRarity = this.ReadModSettings(PTMDefaultValues.pumpkinDepositRarity.name(), PTMDefaultValues.pumpkinDepositRarity.intValue());
        this.pumpkinDepositFrequency = this.ReadModSettings(PTMDefaultValues.pumpkinDepositFrequency.name(), PTMDefaultValues.pumpkinDepositFrequency.intValue());
        this.pumpkinDepositMinAltitude = this.ReadModSettings(PTMDefaultValues.pumpkinDepositMinAltitude.name(), PTMDefaultValues.pumpkinDepositMinAltitude.intValue());
        this.pumpkinDepositMaxAltitude = this.ReadModSettings(PTMDefaultValues.pumpkinDepositMaxAltitude.name(), PTMDefaultValues.pumpkinDepositMaxAltitude.intValue());

        this.cactusDepositRarity = this.ReadModSettings(PTMDefaultValues.cactusDepositRarity.name(), PTMDefaultValues.cactusDepositRarity.intValue());
        this.cactusDepositFrequency = this.ReadModSettings(PTMDefaultValues.cactusDepositFrequency.name(), this.DefaultCactus);
        this.cactusDepositMinAltitude = this.ReadModSettings(PTMDefaultValues.cactusDepositMinAltitude.name(), PTMDefaultValues.cactusDepositMinAltitude.intValue());
        this.cactusDepositMaxAltitude = this.ReadModSettings(PTMDefaultValues.cactusDepositMaxAltitude.name(), PTMDefaultValues.cactusDepositMaxAltitude.intValue());

        this.longGrassDepositRarity = this.ReadModSettings(PTMDefaultValues.longGrassDepositRarity.name(), PTMDefaultValues.longGrassDepositRarity.intValue());
        this.longGrassDepositFrequency = this.ReadModSettings(PTMDefaultValues.longGrassDepositFrequency.name(), this.DefaultGrass);
        this.longGrassDepositMinAltitude = this.ReadModSettings(PTMDefaultValues.longGrassDepositMinAltitude.name(), PTMDefaultValues.longGrassDepositMinAltitude.intValue());
        this.longGrassDepositMaxAltitude = this.ReadModSettings(PTMDefaultValues.longGrassDepositMaxAltitude.name(), PTMDefaultValues.longGrassDepositMaxAltitude.intValue());

        this.deadBushDepositRarity = this.ReadModSettings(PTMDefaultValues.deadBushDepositRarity.name(), PTMDefaultValues.deadBushDepositRarity.intValue());
        this.deadBushDepositFrequency = this.ReadModSettings(PTMDefaultValues.deadBushDepositFrequency.name(), this.DefaultDeadBrush);
        this.deadBushDepositMinAltitude = this.ReadModSettings(PTMDefaultValues.deadBushDepositMinAltitude.name(), PTMDefaultValues.deadBushDepositMinAltitude.intValue());
        this.deadBushDepositMaxAltitude = this.ReadModSettings(PTMDefaultValues.deadBushDepositMaxAltitude.name(), PTMDefaultValues.deadBushDepositMaxAltitude.intValue());


        this.waterSourceDepositRarity = this.ReadModSettings(PTMDefaultValues.waterSourceDepositRarity.name(), PTMDefaultValues.waterSourceDepositRarity.intValue());
        this.waterSourceDepositFrequency = this.ReadModSettings(PTMDefaultValues.waterSourceDepositFrequency.name(), PTMDefaultValues.waterSourceDepositFrequency.intValue());
        this.waterSourceDepositMinAltitude = this.ReadModSettings(PTMDefaultValues.waterSourceDepositMinAltitude.name(), PTMDefaultValues.waterSourceDepositMinAltitude.intValue());
        this.waterSourceDepositMaxAltitude = this.ReadModSettings(PTMDefaultValues.waterSourceDepositMaxAltitude.name(), PTMDefaultValues.waterSourceDepositMaxAltitude.intValue());


        this.lavaSourceDepositRarity = this.ReadModSettings(PTMDefaultValues.lavaSourceDepositRarity.name(), PTMDefaultValues.lavaSourceDepositRarity.intValue());
        this.lavaSourceDepositFrequency = this.ReadModSettings(PTMDefaultValues.lavaSourceDepositFrequency.name(), PTMDefaultValues.lavaSourceDepositFrequency.intValue());
        this.lavaSourceDepositMinAltitude = this.ReadModSettings(PTMDefaultValues.lavaSourceDepositMinAltitude.name(), PTMDefaultValues.lavaSourceDepositMinAltitude.intValue());
        this.lavaSourceDepositMaxAltitude = this.ReadModSettings(PTMDefaultValues.lavaSourceDepositMaxAltitude.name(), PTMDefaultValues.lavaSourceDepositMaxAltitude.intValue());

        this.waterClayDepositRarity = this.ReadModSettings(PTMDefaultValues.waterClayDepositRarity.name(), PTMDefaultValues.waterClayDepositRarity.intValue());
        this.waterClayDepositFrequency = this.ReadModSettings(PTMDefaultValues.waterClayDepositFrequency.name(), this.DefaultClay);
        this.waterClayDepositSize = this.ReadModSettings(PTMDefaultValues.waterClayDepositSize.name(), PTMDefaultValues.waterClayDepositSize.intValue());

        this.waterSandDepositRarity = this.ReadModSettings(PTMDefaultValues.waterSandDepositRarity.name(), PTMDefaultValues.waterSandDepositRarity.intValue());
        this.waterSandDepositFrequency = this.ReadModSettings(PTMDefaultValues.waterSandDepositFrequency.name(), this.DefaultSand);
        this.waterSandDepositSize = this.ReadModSettings(PTMDefaultValues.waterSandDepositSize.name(), PTMDefaultValues.waterSandDepositSize.intValue());

        this.waterGravelDepositRarity = this.ReadModSettings(PTMDefaultValues.waterGravelDepositRarity.name(), PTMDefaultValues.waterGravelDepositRarity.intValue());
        this.waterGravelDepositFrequency = this.ReadModSettings(PTMDefaultValues.waterGravelDepositFrequency.name(), this.DefaultGravel);
        this.waterGravelDepositSize = this.ReadModSettings(PTMDefaultValues.waterGravelDepositSize.name(), PTMDefaultValues.waterGravelDepositSize.intValue());

        this.dirtDepositRarity1 = this.ReadModSettings(PTMDefaultValues.dirtDepositRarity1.name(), PTMDefaultValues.dirtDepositRarity1.intValue());
        this.dirtDepositFrequency1 = this.ReadModSettings(PTMDefaultValues.dirtDepositFrequency1.name(), PTMDefaultValues.dirtDepositFrequency1.intValue());
        this.dirtDepositSize1 = this.ReadModSettings(PTMDefaultValues.dirtDepositSize1.name(), PTMDefaultValues.dirtDepositSize1.intValue());
        this.dirtDepositMinAltitude1 = this.ReadModSettings(PTMDefaultValues.dirtDepositMinAltitude1.name(), PTMDefaultValues.dirtDepositMinAltitude1.intValue());
        this.dirtDepositMaxAltitude1 = this.ReadModSettings(PTMDefaultValues.dirtDepositMaxAltitude1.name(), PTMDefaultValues.dirtDepositMaxAltitude1.intValue());
        this.dirtDepositRarity2 = this.ReadModSettings(PTMDefaultValues.dirtDepositRarity2.name(), PTMDefaultValues.dirtDepositRarity2.intValue());
        this.dirtDepositFrequency2 = this.ReadModSettings(PTMDefaultValues.dirtDepositFrequency2.name(), PTMDefaultValues.dirtDepositFrequency2.intValue());
        this.dirtDepositSize2 = this.ReadModSettings(PTMDefaultValues.dirtDepositSize2.name(), PTMDefaultValues.dirtDepositSize2.intValue());
        this.dirtDepositMinAltitude2 = this.ReadModSettings(PTMDefaultValues.dirtDepositMinAltitude2.name(), PTMDefaultValues.dirtDepositMinAltitude2.intValue());
        this.dirtDepositMaxAltitude2 = this.ReadModSettings(PTMDefaultValues.dirtDepositMaxAltitude2.name(), PTMDefaultValues.dirtDepositMaxAltitude2.intValue());
        this.dirtDepositRarity3 = this.ReadModSettings(PTMDefaultValues.dirtDepositRarity3.name(), PTMDefaultValues.dirtDepositRarity3.intValue());
        this.dirtDepositFrequency3 = this.ReadModSettings(PTMDefaultValues.dirtDepositFrequency3.name(), PTMDefaultValues.dirtDepositFrequency3.intValue());
        this.dirtDepositSize3 = this.ReadModSettings(PTMDefaultValues.dirtDepositSize3.name(), PTMDefaultValues.dirtDepositSize3.intValue());
        this.dirtDepositMinAltitude3 = this.ReadModSettings(PTMDefaultValues.dirtDepositMinAltitude3.name(), PTMDefaultValues.dirtDepositMinAltitude3.intValue());
        this.dirtDepositMaxAltitude3 = this.ReadModSettings(PTMDefaultValues.dirtDepositMaxAltitude3.name(), PTMDefaultValues.dirtDepositMaxAltitude3.intValue());
        this.dirtDepositRarity4 = this.ReadModSettings(PTMDefaultValues.dirtDepositRarity4.name(), PTMDefaultValues.dirtDepositRarity4.intValue());
        this.dirtDepositFrequency4 = this.ReadModSettings(PTMDefaultValues.dirtDepositFrequency4.name(), PTMDefaultValues.dirtDepositFrequency4.intValue());
        this.dirtDepositSize4 = this.ReadModSettings(PTMDefaultValues.dirtDepositSize4.name(), PTMDefaultValues.dirtDepositSize4.intValue());
        this.dirtDepositMinAltitude4 = this.ReadModSettings(PTMDefaultValues.dirtDepositMinAltitude4.name(), PTMDefaultValues.dirtDepositMinAltitude4.intValue());
        this.dirtDepositMaxAltitude4 = this.ReadModSettings(PTMDefaultValues.dirtDepositMaxAltitude4.name(), PTMDefaultValues.dirtDepositMaxAltitude4.intValue());

        this.gravelDepositRarity1 = this.ReadModSettings(PTMDefaultValues.gravelDepositRarity1.name(), PTMDefaultValues.gravelDepositRarity1.intValue());
        this.gravelDepositFrequency1 = this.ReadModSettings(PTMDefaultValues.gravelDepositFrequency1.name(), PTMDefaultValues.gravelDepositFrequency1.intValue());
        this.gravelDepositSize1 = this.ReadModSettings(PTMDefaultValues.gravelDepositSize1.name(), PTMDefaultValues.gravelDepositSize1.intValue());
        this.gravelDepositMinAltitude1 = this.ReadModSettings(PTMDefaultValues.gravelDepositMinAltitude1.name(), PTMDefaultValues.gravelDepositMinAltitude1.intValue());
        this.gravelDepositMaxAltitude1 = this.ReadModSettings(PTMDefaultValues.gravelDepositMaxAltitude1.name(), PTMDefaultValues.gravelDepositMaxAltitude1.intValue());
        this.gravelDepositRarity2 = this.ReadModSettings(PTMDefaultValues.gravelDepositRarity2.name(), PTMDefaultValues.gravelDepositRarity2.intValue());
        this.gravelDepositFrequency2 = this.ReadModSettings(PTMDefaultValues.gravelDepositFrequency2.name(), PTMDefaultValues.gravelDepositFrequency2.intValue());
        this.gravelDepositSize2 = this.ReadModSettings(PTMDefaultValues.gravelDepositSize2.name(), PTMDefaultValues.gravelDepositSize2.intValue());
        this.gravelDepositMinAltitude2 = this.ReadModSettings(PTMDefaultValues.gravelDepositMinAltitude2.name(), PTMDefaultValues.gravelDepositMinAltitude2.intValue());
        this.gravelDepositMaxAltitude2 = this.ReadModSettings(PTMDefaultValues.gravelDepositMaxAltitude2.name(), PTMDefaultValues.gravelDepositMaxAltitude2.intValue());
        this.gravelDepositRarity3 = this.ReadModSettings(PTMDefaultValues.gravelDepositRarity3.name(), PTMDefaultValues.gravelDepositRarity3.intValue());
        this.gravelDepositFrequency3 = this.ReadModSettings(PTMDefaultValues.gravelDepositFrequency3.name(), PTMDefaultValues.gravelDepositFrequency3.intValue());
        this.gravelDepositSize3 = this.ReadModSettings(PTMDefaultValues.gravelDepositSize3.name(), PTMDefaultValues.gravelDepositSize3.intValue());
        this.gravelDepositMinAltitude3 = this.ReadModSettings(PTMDefaultValues.gravelDepositMinAltitude3.name(), PTMDefaultValues.gravelDepositMinAltitude3.intValue());
        this.gravelDepositMaxAltitude3 = this.ReadModSettings(PTMDefaultValues.gravelDepositMaxAltitude3.name(), PTMDefaultValues.gravelDepositMaxAltitude3.intValue());
        this.gravelDepositRarity4 = this.ReadModSettings(PTMDefaultValues.gravelDepositRarity4.name(), PTMDefaultValues.gravelDepositRarity4.intValue());
        this.gravelDepositFrequency4 = this.ReadModSettings(PTMDefaultValues.gravelDepositFrequency4.name(), PTMDefaultValues.gravelDepositFrequency4.intValue());
        this.gravelDepositSize4 = this.ReadModSettings(PTMDefaultValues.gravelDepositSize4.name(), PTMDefaultValues.gravelDepositSize4.intValue());
        this.gravelDepositMinAltitude4 = this.ReadModSettings(PTMDefaultValues.gravelDepositMinAltitude4.name(), PTMDefaultValues.gravelDepositMinAltitude4.intValue());
        this.gravelDepositMaxAltitude4 = this.ReadModSettings(PTMDefaultValues.gravelDepositMaxAltitude4.name(), PTMDefaultValues.gravelDepositMaxAltitude4.intValue());

        this.clayDepositRarity1 = this.ReadModSettings(PTMDefaultValues.clayDepositRarity1.name(), PTMDefaultValues.clayDepositRarity1.intValue());
        this.clayDepositFrequency1 = this.ReadModSettings(PTMDefaultValues.clayDepositFrequency1.name(), PTMDefaultValues.clayDepositFrequency1.intValue());
        this.clayDepositSize1 = this.ReadModSettings(PTMDefaultValues.clayDepositSize1.name(), PTMDefaultValues.clayDepositSize1.intValue());
        this.clayDepositMinAltitude1 = this.ReadModSettings(PTMDefaultValues.clayDepositMinAltitude1.name(), PTMDefaultValues.clayDepositMinAltitude1.intValue());
        this.clayDepositMaxAltitude1 = this.ReadModSettings(PTMDefaultValues.clayDepositMaxAltitude1.name(), PTMDefaultValues.clayDepositMaxAltitude1.intValue());
        this.clayDepositRarity2 = this.ReadModSettings(PTMDefaultValues.clayDepositRarity2.name(), PTMDefaultValues.clayDepositRarity2.intValue());
        this.clayDepositFrequency2 = this.ReadModSettings(PTMDefaultValues.clayDepositFrequency2.name(), PTMDefaultValues.clayDepositFrequency2.intValue());
        this.clayDepositSize2 = this.ReadModSettings(PTMDefaultValues.clayDepositSize2.name(), PTMDefaultValues.clayDepositSize2.intValue());
        this.clayDepositMinAltitude2 = this.ReadModSettings(PTMDefaultValues.clayDepositMinAltitude2.name(), PTMDefaultValues.clayDepositMinAltitude2.intValue());
        this.clayDepositMaxAltitude2 = this.ReadModSettings(PTMDefaultValues.clayDepositMaxAltitude2.name(), PTMDefaultValues.clayDepositMaxAltitude2.intValue());
        this.clayDepositRarity3 = this.ReadModSettings(PTMDefaultValues.clayDepositRarity3.name(), PTMDefaultValues.clayDepositRarity3.intValue());
        this.clayDepositFrequency3 = this.ReadModSettings(PTMDefaultValues.clayDepositFrequency3.name(), PTMDefaultValues.clayDepositFrequency3.intValue());
        this.clayDepositSize3 = this.ReadModSettings(PTMDefaultValues.clayDepositSize3.name(), PTMDefaultValues.clayDepositSize3.intValue());
        this.clayDepositMinAltitude3 = this.ReadModSettings(PTMDefaultValues.clayDepositMinAltitude3.name(), PTMDefaultValues.clayDepositMinAltitude3.intValue());
        this.clayDepositMaxAltitude3 = this.ReadModSettings(PTMDefaultValues.clayDepositMaxAltitude3.name(), PTMDefaultValues.clayDepositMaxAltitude3.intValue());
        this.clayDepositRarity4 = this.ReadModSettings(PTMDefaultValues.clayDepositRarity4.name(), PTMDefaultValues.clayDepositRarity4.intValue());
        this.clayDepositFrequency4 = this.ReadModSettings(PTMDefaultValues.clayDepositFrequency4.name(), PTMDefaultValues.clayDepositFrequency4.intValue());
        this.clayDepositSize4 = this.ReadModSettings(PTMDefaultValues.clayDepositSize4.name(), PTMDefaultValues.clayDepositSize4.intValue());
        this.clayDepositMinAltitude4 = this.ReadModSettings(PTMDefaultValues.clayDepositMinAltitude4.name(), PTMDefaultValues.clayDepositMinAltitude4.intValue());
        this.clayDepositMaxAltitude4 = this.ReadModSettings(PTMDefaultValues.clayDepositMaxAltitude4.name(), PTMDefaultValues.clayDepositMaxAltitude4.intValue());

        this.coalDepositRarity1 = this.ReadModSettings(PTMDefaultValues.coalDepositRarity1.name(), PTMDefaultValues.coalDepositRarity1.intValue());
        this.coalDepositFrequency1 = this.ReadModSettings(PTMDefaultValues.coalDepositFrequency1.name(), PTMDefaultValues.coalDepositFrequency1.intValue());
        this.coalDepositSize1 = this.ReadModSettings(PTMDefaultValues.coalDepositSize1.name(), PTMDefaultValues.coalDepositSize1.intValue());
        this.coalDepositMinAltitude1 = this.ReadModSettings(PTMDefaultValues.coalDepositMinAltitude1.name(), PTMDefaultValues.coalDepositMinAltitude1.intValue());
        this.coalDepositMaxAltitude1 = this.ReadModSettings(PTMDefaultValues.coalDepositMaxAltitude1.name(), PTMDefaultValues.coalDepositMaxAltitude1.intValue());
        this.coalDepositRarity2 = this.ReadModSettings(PTMDefaultValues.coalDepositRarity2.name(), PTMDefaultValues.coalDepositRarity2.intValue());
        this.coalDepositFrequency2 = this.ReadModSettings(PTMDefaultValues.coalDepositFrequency2.name(), PTMDefaultValues.coalDepositFrequency2.intValue());
        this.coalDepositSize2 = this.ReadModSettings(PTMDefaultValues.coalDepositSize2.name(), PTMDefaultValues.coalDepositSize2.intValue());
        this.coalDepositMinAltitude2 = this.ReadModSettings(PTMDefaultValues.coalDepositMinAltitude2.name(), PTMDefaultValues.coalDepositMinAltitude2.intValue());
        this.coalDepositMaxAltitude2 = this.ReadModSettings(PTMDefaultValues.coalDepositMaxAltitude2.name(), PTMDefaultValues.coalDepositMaxAltitude2.intValue());
        this.coalDepositRarity3 = this.ReadModSettings(PTMDefaultValues.coalDepositRarity3.name(), PTMDefaultValues.coalDepositRarity3.intValue());
        this.coalDepositFrequency3 = this.ReadModSettings(PTMDefaultValues.coalDepositFrequency3.name(), PTMDefaultValues.coalDepositFrequency3.intValue());
        this.coalDepositSize3 = this.ReadModSettings(PTMDefaultValues.coalDepositSize3.name(), PTMDefaultValues.coalDepositSize3.intValue());
        this.coalDepositMinAltitude3 = this.ReadModSettings(PTMDefaultValues.coalDepositMinAltitude3.name(), PTMDefaultValues.coalDepositMinAltitude3.intValue());
        this.coalDepositMaxAltitude3 = this.ReadModSettings(PTMDefaultValues.coalDepositMaxAltitude3.name(), PTMDefaultValues.coalDepositMaxAltitude3.intValue());
        this.coalDepositRarity4 = this.ReadModSettings(PTMDefaultValues.coalDepositRarity4.name(), PTMDefaultValues.coalDepositRarity4.intValue());
        this.coalDepositFrequency4 = this.ReadModSettings(PTMDefaultValues.coalDepositFrequency4.name(), PTMDefaultValues.coalDepositFrequency4.intValue());
        this.coalDepositSize4 = this.ReadModSettings(PTMDefaultValues.coalDepositSize4.name(), PTMDefaultValues.coalDepositSize4.intValue());
        this.coalDepositMinAltitude4 = this.ReadModSettings(PTMDefaultValues.coalDepositMinAltitude4.name(), PTMDefaultValues.coalDepositMinAltitude4.intValue());
        this.coalDepositMaxAltitude4 = this.ReadModSettings(PTMDefaultValues.coalDepositMaxAltitude4.name(), PTMDefaultValues.coalDepositMaxAltitude4.intValue());

        this.ironDepositRarity1 = this.ReadModSettings(PTMDefaultValues.ironDepositRarity1.name(), PTMDefaultValues.ironDepositRarity1.intValue());
        this.ironDepositFrequency1 = this.ReadModSettings(PTMDefaultValues.ironDepositFrequency1.name(), PTMDefaultValues.ironDepositFrequency1.intValue());
        this.ironDepositSize1 = this.ReadModSettings(PTMDefaultValues.ironDepositSize1.name(), PTMDefaultValues.ironDepositSize1.intValue());
        this.ironDepositMinAltitude1 = this.ReadModSettings(PTMDefaultValues.ironDepositMinAltitude1.name(), PTMDefaultValues.ironDepositMinAltitude1.intValue());
        this.ironDepositMaxAltitude1 = this.ReadModSettings(PTMDefaultValues.ironDepositMaxAltitude1.name(), PTMDefaultValues.ironDepositMaxAltitude1.intValue());
        this.ironDepositRarity2 = this.ReadModSettings(PTMDefaultValues.ironDepositRarity2.name(), PTMDefaultValues.ironDepositRarity2.intValue());
        this.ironDepositFrequency2 = this.ReadModSettings(PTMDefaultValues.ironDepositFrequency2.name(), PTMDefaultValues.ironDepositFrequency2.intValue());
        this.ironDepositSize2 = this.ReadModSettings(PTMDefaultValues.ironDepositSize2.name(), PTMDefaultValues.ironDepositSize2.intValue());
        this.ironDepositMinAltitude2 = this.ReadModSettings(PTMDefaultValues.ironDepositMinAltitude2.name(), PTMDefaultValues.ironDepositMinAltitude2.intValue());
        this.ironDepositMaxAltitude2 = this.ReadModSettings(PTMDefaultValues.ironDepositMaxAltitude2.name(), PTMDefaultValues.ironDepositMaxAltitude2.intValue());
        this.ironDepositRarity3 = this.ReadModSettings(PTMDefaultValues.ironDepositRarity3.name(), PTMDefaultValues.ironDepositRarity3.intValue());
        this.ironDepositFrequency3 = this.ReadModSettings(PTMDefaultValues.ironDepositFrequency3.name(), PTMDefaultValues.ironDepositFrequency3.intValue());
        this.ironDepositSize3 = this.ReadModSettings(PTMDefaultValues.ironDepositSize3.name(), PTMDefaultValues.ironDepositSize3.intValue());
        this.ironDepositMinAltitude3 = this.ReadModSettings(PTMDefaultValues.ironDepositMinAltitude3.name(), PTMDefaultValues.ironDepositMinAltitude3.intValue());
        this.ironDepositMaxAltitude3 = this.ReadModSettings(PTMDefaultValues.ironDepositMaxAltitude3.name(), PTMDefaultValues.ironDepositMaxAltitude3.intValue());
        this.ironDepositRarity4 = this.ReadModSettings(PTMDefaultValues.ironDepositRarity4.name(), PTMDefaultValues.ironDepositRarity4.intValue());
        this.ironDepositFrequency4 = this.ReadModSettings(PTMDefaultValues.ironDepositFrequency4.name(), PTMDefaultValues.ironDepositFrequency4.intValue());
        this.ironDepositSize4 = this.ReadModSettings(PTMDefaultValues.ironDepositSize4.name(), PTMDefaultValues.ironDepositSize4.intValue());
        this.ironDepositMinAltitude4 = this.ReadModSettings(PTMDefaultValues.ironDepositMinAltitude4.name(), PTMDefaultValues.ironDepositMinAltitude4.intValue());
        this.ironDepositMaxAltitude4 = this.ReadModSettings(PTMDefaultValues.ironDepositMaxAltitude4.name(), PTMDefaultValues.ironDepositMaxAltitude4.intValue());

        this.goldDepositRarity1 = this.ReadModSettings(PTMDefaultValues.goldDepositRarity1.name(), PTMDefaultValues.goldDepositRarity1.intValue());
        this.goldDepositFrequency1 = this.ReadModSettings(PTMDefaultValues.goldDepositFrequency1.name(), PTMDefaultValues.goldDepositFrequency1.intValue());
        this.goldDepositSize1 = this.ReadModSettings(PTMDefaultValues.goldDepositSize1.name(), PTMDefaultValues.goldDepositSize1.intValue());
        this.goldDepositMinAltitude1 = this.ReadModSettings(PTMDefaultValues.goldDepositMinAltitude1.name(), PTMDefaultValues.goldDepositMinAltitude1.intValue());
        this.goldDepositMaxAltitude1 = this.ReadModSettings(PTMDefaultValues.goldDepositMaxAltitude1.name(), PTMDefaultValues.goldDepositMaxAltitude1.intValue());
        this.goldDepositRarity2 = this.ReadModSettings(PTMDefaultValues.goldDepositRarity2.name(), PTMDefaultValues.goldDepositRarity2.intValue());
        this.goldDepositFrequency2 = this.ReadModSettings(PTMDefaultValues.goldDepositFrequency2.name(), PTMDefaultValues.goldDepositFrequency2.intValue());
        this.goldDepositSize2 = this.ReadModSettings(PTMDefaultValues.goldDepositSize2.name(), PTMDefaultValues.goldDepositSize2.intValue());
        this.goldDepositMinAltitude2 = this.ReadModSettings(PTMDefaultValues.goldDepositMinAltitude2.name(), PTMDefaultValues.goldDepositMinAltitude2.intValue());
        this.goldDepositMaxAltitude2 = this.ReadModSettings(PTMDefaultValues.goldDepositMaxAltitude2.name(), PTMDefaultValues.goldDepositMaxAltitude2.intValue());
        this.goldDepositRarity3 = this.ReadModSettings(PTMDefaultValues.goldDepositRarity3.name(), PTMDefaultValues.goldDepositRarity3.intValue());
        this.goldDepositFrequency3 = this.ReadModSettings(PTMDefaultValues.goldDepositFrequency3.name(), PTMDefaultValues.goldDepositFrequency3.intValue());
        this.goldDepositSize3 = this.ReadModSettings(PTMDefaultValues.goldDepositSize3.name(), PTMDefaultValues.goldDepositSize3.intValue());
        this.goldDepositMinAltitude3 = this.ReadModSettings(PTMDefaultValues.goldDepositMinAltitude3.name(), PTMDefaultValues.goldDepositMinAltitude3.intValue());
        this.goldDepositMaxAltitude3 = this.ReadModSettings(PTMDefaultValues.goldDepositMaxAltitude3.name(), PTMDefaultValues.goldDepositMaxAltitude3.intValue());
        this.goldDepositRarity4 = this.ReadModSettings(PTMDefaultValues.goldDepositRarity4.name(), PTMDefaultValues.goldDepositRarity4.intValue());
        this.goldDepositFrequency4 = this.ReadModSettings(PTMDefaultValues.goldDepositFrequency4.name(), PTMDefaultValues.goldDepositFrequency4.intValue());
        this.goldDepositSize4 = this.ReadModSettings(PTMDefaultValues.goldDepositSize4.name(), PTMDefaultValues.goldDepositSize4.intValue());
        this.goldDepositMinAltitude4 = this.ReadModSettings(PTMDefaultValues.goldDepositMinAltitude4.name(), PTMDefaultValues.goldDepositMinAltitude4.intValue());
        this.goldDepositMaxAltitude4 = this.ReadModSettings(PTMDefaultValues.goldDepositMaxAltitude4.name(), PTMDefaultValues.goldDepositMaxAltitude4.intValue());

        this.redstoneDepositRarity1 = this.ReadModSettings(PTMDefaultValues.redstoneDepositRarity1.name(), PTMDefaultValues.redstoneDepositRarity1.intValue());
        this.redstoneDepositFrequency1 = this.ReadModSettings(PTMDefaultValues.redstoneDepositFrequency1.name(), PTMDefaultValues.redstoneDepositFrequency1.intValue());
        this.redstoneDepositSize1 = this.ReadModSettings(PTMDefaultValues.redstoneDepositSize1.name(), PTMDefaultValues.redstoneDepositSize1.intValue());
        this.redstoneDepositMinAltitude1 = this.ReadModSettings(PTMDefaultValues.redstoneDepositMinAltitude1.name(), PTMDefaultValues.redstoneDepositMinAltitude1.intValue());
        this.redstoneDepositMaxAltitude1 = this.ReadModSettings(PTMDefaultValues.redstoneDepositMaxAltitude1.name(), PTMDefaultValues.redstoneDepositMaxAltitude1.intValue());
        this.redstoneDepositRarity2 = this.ReadModSettings(PTMDefaultValues.redstoneDepositRarity2.name(), PTMDefaultValues.redstoneDepositRarity2.intValue());
        this.redstoneDepositFrequency2 = this.ReadModSettings(PTMDefaultValues.redstoneDepositFrequency2.name(), PTMDefaultValues.redstoneDepositFrequency2.intValue());
        this.redstoneDepositSize2 = this.ReadModSettings(PTMDefaultValues.redstoneDepositSize2.name(), PTMDefaultValues.redstoneDepositSize2.intValue());
        this.redstoneDepositMinAltitude2 = this.ReadModSettings(PTMDefaultValues.redstoneDepositMinAltitude2.name(), PTMDefaultValues.redstoneDepositMinAltitude2.intValue());
        this.redstoneDepositMaxAltitude2 = this.ReadModSettings(PTMDefaultValues.redstoneDepositMaxAltitude2.name(), PTMDefaultValues.redstoneDepositMaxAltitude2.intValue());
        this.redstoneDepositRarity3 = this.ReadModSettings(PTMDefaultValues.redstoneDepositRarity3.name(), PTMDefaultValues.redstoneDepositRarity3.intValue());
        this.redstoneDepositFrequency3 = this.ReadModSettings(PTMDefaultValues.redstoneDepositFrequency3.name(), PTMDefaultValues.redstoneDepositFrequency3.intValue());
        this.redstoneDepositSize3 = this.ReadModSettings(PTMDefaultValues.redstoneDepositSize3.name(), PTMDefaultValues.redstoneDepositSize3.intValue());
        this.redstoneDepositMinAltitude3 = this.ReadModSettings(PTMDefaultValues.redstoneDepositMinAltitude3.name(), PTMDefaultValues.redstoneDepositMinAltitude3.intValue());
        this.redstoneDepositMaxAltitude3 = this.ReadModSettings(PTMDefaultValues.redstoneDepositMaxAltitude3.name(), PTMDefaultValues.redstoneDepositMaxAltitude3.intValue());
        this.redstoneDepositRarity4 = this.ReadModSettings(PTMDefaultValues.redstoneDepositRarity4.name(), PTMDefaultValues.redstoneDepositRarity4.intValue());
        this.redstoneDepositFrequency4 = this.ReadModSettings(PTMDefaultValues.redstoneDepositFrequency4.name(), PTMDefaultValues.redstoneDepositFrequency4.intValue());
        this.redstoneDepositSize4 = this.ReadModSettings(PTMDefaultValues.redstoneDepositSize4.name(), PTMDefaultValues.redstoneDepositSize4.intValue());
        this.redstoneDepositMinAltitude4 = this.ReadModSettings(PTMDefaultValues.redstoneDepositMinAltitude4.name(), PTMDefaultValues.redstoneDepositMinAltitude4.intValue());
        this.redstoneDepositMaxAltitude4 = this.ReadModSettings(PTMDefaultValues.redstoneDepositMaxAltitude4.name(), PTMDefaultValues.redstoneDepositMaxAltitude4.intValue());

        this.diamondDepositRarity1 = this.ReadModSettings(PTMDefaultValues.diamondDepositRarity1.name(), PTMDefaultValues.diamondDepositRarity1.intValue());
        this.diamondDepositFrequency1 = this.ReadModSettings(PTMDefaultValues.diamondDepositFrequency1.name(), PTMDefaultValues.diamondDepositFrequency1.intValue());
        this.diamondDepositSize1 = this.ReadModSettings(PTMDefaultValues.diamondDepositSize1.name(), PTMDefaultValues.diamondDepositSize1.intValue());
        this.diamondDepositMinAltitude1 = this.ReadModSettings(PTMDefaultValues.diamondDepositMinAltitude1.name(), PTMDefaultValues.diamondDepositMinAltitude1.intValue());
        this.diamondDepositMaxAltitude1 = this.ReadModSettings(PTMDefaultValues.diamondDepositMaxAltitude1.name(), PTMDefaultValues.diamondDepositMaxAltitude1.intValue());
        this.diamondDepositRarity2 = this.ReadModSettings(PTMDefaultValues.diamondDepositRarity2.name(), PTMDefaultValues.diamondDepositRarity2.intValue());
        this.diamondDepositFrequency2 = this.ReadModSettings(PTMDefaultValues.diamondDepositFrequency2.name(), PTMDefaultValues.diamondDepositFrequency2.intValue());
        this.diamondDepositSize2 = this.ReadModSettings(PTMDefaultValues.diamondDepositSize2.name(), PTMDefaultValues.diamondDepositSize2.intValue());
        this.diamondDepositMinAltitude2 = this.ReadModSettings(PTMDefaultValues.diamondDepositMinAltitude2.name(), PTMDefaultValues.diamondDepositMinAltitude2.intValue());
        this.diamondDepositMaxAltitude2 = this.ReadModSettings(PTMDefaultValues.diamondDepositMaxAltitude2.name(), PTMDefaultValues.diamondDepositMaxAltitude2.intValue());
        this.diamondDepositRarity3 = this.ReadModSettings(PTMDefaultValues.diamondDepositRarity3.name(), PTMDefaultValues.diamondDepositRarity3.intValue());
        this.diamondDepositFrequency3 = this.ReadModSettings(PTMDefaultValues.diamondDepositFrequency3.name(), PTMDefaultValues.diamondDepositFrequency3.intValue());
        this.diamondDepositSize3 = this.ReadModSettings(PTMDefaultValues.diamondDepositSize3.name(), PTMDefaultValues.diamondDepositSize3.intValue());
        this.diamondDepositMinAltitude3 = this.ReadModSettings(PTMDefaultValues.diamondDepositMinAltitude3.name(), PTMDefaultValues.diamondDepositMinAltitude3.intValue());
        this.diamondDepositMaxAltitude3 = this.ReadModSettings(PTMDefaultValues.diamondDepositMaxAltitude3.name(), PTMDefaultValues.diamondDepositMaxAltitude3.intValue());
        this.diamondDepositRarity4 = this.ReadModSettings(PTMDefaultValues.diamondDepositRarity4.name(), PTMDefaultValues.diamondDepositRarity4.intValue());
        this.diamondDepositFrequency4 = this.ReadModSettings(PTMDefaultValues.diamondDepositFrequency4.name(), PTMDefaultValues.diamondDepositFrequency4.intValue());
        this.diamondDepositSize4 = this.ReadModSettings(PTMDefaultValues.diamondDepositSize4.name(), PTMDefaultValues.diamondDepositSize4.intValue());
        this.diamondDepositMinAltitude4 = this.ReadModSettings(PTMDefaultValues.diamondDepositMinAltitude4.name(), PTMDefaultValues.diamondDepositMinAltitude4.intValue());
        this.diamondDepositMaxAltitude4 = this.ReadModSettings(PTMDefaultValues.diamondDepositMaxAltitude4.name(), PTMDefaultValues.diamondDepositMaxAltitude4.intValue());

        this.lapislazuliDepositRarity1 = this.ReadModSettings(PTMDefaultValues.lapislazuliDepositRarity1.name(), PTMDefaultValues.lapislazuliDepositRarity1.intValue());
        this.lapislazuliDepositFrequency1 = this.ReadModSettings(PTMDefaultValues.lapislazuliDepositFrequency1.name(), PTMDefaultValues.lapislazuliDepositFrequency1.intValue());
        this.lapislazuliDepositSize1 = this.ReadModSettings(PTMDefaultValues.lapislazuliDepositSize1.name(), PTMDefaultValues.lapislazuliDepositSize1.intValue());
        this.lapislazuliDepositMinAltitude1 = this.ReadModSettings(PTMDefaultValues.lapislazuliDepositMinAltitude1.name(), PTMDefaultValues.lapislazuliDepositMinAltitude1.intValue());
        this.lapislazuliDepositMaxAltitude1 = this.ReadModSettings(PTMDefaultValues.lapislazuliDepositMaxAltitude1.name(), PTMDefaultValues.lapislazuliDepositMaxAltitude1.intValue());
        this.lapislazuliDepositRarity2 = this.ReadModSettings(PTMDefaultValues.lapislazuliDepositRarity2.name(), PTMDefaultValues.lapislazuliDepositRarity2.intValue());
        this.lapislazuliDepositFrequency2 = this.ReadModSettings(PTMDefaultValues.lapislazuliDepositFrequency2.name(), PTMDefaultValues.lapislazuliDepositFrequency2.intValue());
        this.lapislazuliDepositSize2 = this.ReadModSettings(PTMDefaultValues.lapislazuliDepositSize2.name(), PTMDefaultValues.lapislazuliDepositSize2.intValue());
        this.lapislazuliDepositMinAltitude2 = this.ReadModSettings(PTMDefaultValues.lapislazuliDepositMinAltitude2.name(), PTMDefaultValues.lapislazuliDepositMinAltitude2.intValue());
        this.lapislazuliDepositMaxAltitude2 = this.ReadModSettings(PTMDefaultValues.lapislazuliDepositMaxAltitude2.name(), PTMDefaultValues.lapislazuliDepositMaxAltitude2.intValue());
        this.lapislazuliDepositRarity3 = this.ReadModSettings(PTMDefaultValues.lapislazuliDepositRarity3.name(), PTMDefaultValues.lapislazuliDepositRarity3.intValue());
        this.lapislazuliDepositFrequency3 = this.ReadModSettings(PTMDefaultValues.lapislazuliDepositFrequency3.name(), PTMDefaultValues.lapislazuliDepositFrequency3.intValue());
        this.lapislazuliDepositSize3 = this.ReadModSettings(PTMDefaultValues.lapislazuliDepositSize3.name(), PTMDefaultValues.lapislazuliDepositSize3.intValue());
        this.lapislazuliDepositMinAltitude3 = this.ReadModSettings(PTMDefaultValues.lapislazuliDepositMinAltitude3.name(), PTMDefaultValues.lapislazuliDepositMinAltitude3.intValue());
        this.lapislazuliDepositMaxAltitude3 = this.ReadModSettings(PTMDefaultValues.lapislazuliDepositMaxAltitude3.name(), PTMDefaultValues.lapislazuliDepositMaxAltitude3.intValue());
        this.lapislazuliDepositRarity4 = this.ReadModSettings(PTMDefaultValues.lapislazuliDepositRarity4.name(), PTMDefaultValues.lapislazuliDepositRarity4.intValue());
        this.lapislazuliDepositFrequency4 = this.ReadModSettings(PTMDefaultValues.lapislazuliDepositFrequency4.name(), PTMDefaultValues.lapislazuliDepositFrequency4.intValue());
        this.lapislazuliDepositSize4 = this.ReadModSettings(PTMDefaultValues.lapislazuliDepositSize4.name(), PTMDefaultValues.lapislazuliDepositSize4.intValue());
        this.lapislazuliDepositMinAltitude4 = this.ReadModSettings(PTMDefaultValues.lapislazuliDepositMinAltitude4.name(), PTMDefaultValues.lapislazuliDepositMinAltitude4.intValue());
        this.lapislazuliDepositMaxAltitude4 = this.ReadModSettings(PTMDefaultValues.lapislazuliDepositMaxAltitude4.name(), PTMDefaultValues.lapislazuliDepositMaxAltitude4.intValue());


        this.disableNotchPonds = this.ReadModSettings(PTMDefaultValues.disableNotchPonds.name(), PTMDefaultValues.disableNotchPonds.booleanValue());

        // Todo replace wrond defaults in tree and cactus
        this.notchBiomeTrees = this.ReadModSettings(PTMDefaultValues.notchBiomeTrees.name(), PTMDefaultValues.notchBiomeTrees.booleanValue());
        this.TreeDensity = this.ReadModSettings(PTMDefaultValues.TreeDensity.name(), this.DefaultTrees);

        this.dungeonRarity = this.ReadModSettings(PTMDefaultValues.dungeonRarity.name(), PTMDefaultValues.dungeonRarity.intValue());
        this.dungeonFrequency = this.ReadModSettings(PTMDefaultValues.dungeonFrequency.name(), PTMDefaultValues.dungeonFrequency.intValue());
        this.dungeonMinAltitude = this.ReadModSettings(PTMDefaultValues.dungeonMinAltitude.name(), PTMDefaultValues.dungeonMinAltitude.intValue());
        this.dungeonMaxAltitude = this.ReadModSettings(PTMDefaultValues.dungeonMaxAltitude.name(), PTMDefaultValues.dungeonMaxAltitude.intValue());


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
        WriteModTitleSettings(this.Biome.l + " biome config :");

        this.WriteModTitleSettings("Biome chance doe not work on river and ocean biome!");
        WriteModSettings(PTMDefaultValues.biomeChance.name(), this.BiomeChance);

        WriteModSettings(PTMDefaultValues.BiomeSurfaceAdd.name(), this.BiomeSurface);
        WriteModSettings(PTMDefaultValues.BiomeVolatilityAdd.name(), this.BiomeVolatility);
        // Todo height control
        //WriteHeightSettings();


        WriteModTitleSettings("Replace Variables :");

        WriteModReplaceSettings();


        this.WriteModTitleSettings("Start Tree Variables :");
        this.WriteModSettings(PTMDefaultValues.notchBiomeTrees.name(), this.notchBiomeTrees);
        this.WriteModSettings(PTMDefaultValues.TreeDensity.name(), this.TreeDensity);


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

        this.WriteModSettings(PTMDefaultValues.cactusDepositRarity.name(), this.cactusDepositRarity);
        this.WriteModSettings(PTMDefaultValues.cactusDepositFrequency.name(), this.cactusDepositFrequency);
        this.WriteModSettings(PTMDefaultValues.cactusDepositMinAltitude.name(), this.cactusDepositMinAltitude);
        this.WriteModSettings(PTMDefaultValues.cactusDepositMaxAltitude.name(), this.cactusDepositMaxAltitude);

        this.WriteModSettings(PTMDefaultValues.longGrassDepositRarity.name(), this.longGrassDepositRarity);
        this.WriteModSettings(PTMDefaultValues.longGrassDepositFrequency.name(), this.longGrassDepositFrequency);
        this.WriteModSettings(PTMDefaultValues.longGrassDepositMinAltitude.name(), this.longGrassDepositMinAltitude);
        this.WriteModSettings(PTMDefaultValues.longGrassDepositMaxAltitude.name(), this.longGrassDepositMaxAltitude);

        this.WriteModSettings(PTMDefaultValues.deadBushDepositRarity.name(), this.deadBushDepositRarity);
        this.WriteModSettings(PTMDefaultValues.deadBushDepositFrequency.name(), this.deadBushDepositFrequency);
        this.WriteModSettings(PTMDefaultValues.deadBushDepositMinAltitude.name(), this.deadBushDepositMinAltitude);
        this.WriteModSettings(PTMDefaultValues.deadBushDepositMaxAltitude.name(), this.deadBushDepositMaxAltitude);

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

        this.WriteModSettings(PTMDefaultValues.waterClayDepositRarity.name(), this.waterClayDepositRarity);
        this.WriteModSettings(PTMDefaultValues.waterClayDepositFrequency.name(), this.waterClayDepositFrequency);
        this.WriteModSettings(PTMDefaultValues.waterClayDepositSize.name(), this.waterClayDepositSize);

        this.WriteModSettings(PTMDefaultValues.waterSandDepositRarity.name(), this.waterSandDepositRarity);
        this.WriteModSettings(PTMDefaultValues.waterSandDepositFrequency.name(), this.waterSandDepositFrequency);
        this.WriteModSettings(PTMDefaultValues.waterSandDepositSize.name(), this.waterSandDepositSize);

        this.WriteModSettings(PTMDefaultValues.waterGravelDepositRarity.name(), this.waterGravelDepositRarity);
        this.WriteModSettings(PTMDefaultValues.waterGravelDepositFrequency.name(), this.waterGravelDepositFrequency);
        this.WriteModSettings(PTMDefaultValues.waterGravelDepositSize.name(), this.waterGravelDepositSize);


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
            if (i.hasNext())
                output += ",";
        }
        this.WriteModSettings("ReplacedBlocks", output);
    }

    protected void CorrectSettings()
    {
        this.BiomeChance = CheckValue(this.BiomeChance, 0, 20);
        if (this.Biome == BiomeBase.OCEAN || this.Biome == BiomeBase.RIVER)
            this.BiomeChance = 0;

        this.dungeonRarity = CheckValue(this.dungeonRarity, 0, 100);
        this.dungeonFrequency = CheckValue(this.dungeonFrequency, 0, 200);
        this.dungeonMinAltitude = CheckValue(this.dungeonMaxAltitude, 0, ChunkMaxY - 1);
        this.dungeonMaxAltitude = CheckValue(this.dungeonMaxAltitude, 1, ChunkMaxY, this.dungeonMinAltitude);

        this.flowerDepositRarity = CheckValue(this.flowerDepositRarity, 0, 100);
        this.flowerDepositFrequency = CheckValue(this.flowerDepositFrequency, 0, 200);
        this.flowerDepositMinAltitude = CheckValue(this.flowerDepositMaxAltitude, 0, ChunkMaxY - 1);
        this.flowerDepositMaxAltitude = CheckValue(this.flowerDepositMaxAltitude, 1, ChunkMaxY, this.flowerDepositMinAltitude);

        this.roseDepositRarity = CheckValue(this.roseDepositRarity, 0, 100);
        this.roseDepositFrequency = CheckValue(this.roseDepositFrequency, 0, 200);
        this.roseDepositMinAltitude = CheckValue(this.roseDepositMaxAltitude, 0, ChunkMaxY - 1);
        this.roseDepositMaxAltitude = CheckValue(this.roseDepositMaxAltitude, 1, ChunkMaxY, this.roseDepositMinAltitude);

        this.brownMushroomDepositRarity = CheckValue(this.brownMushroomDepositRarity, 0, 100);
        this.brownMushroomDepositFrequency = CheckValue(this.brownMushroomDepositFrequency, 0, 200);
        this.brownMushroomDepositMinAltitude = CheckValue(this.brownMushroomDepositMaxAltitude, 0, ChunkMaxY - 1);
        this.brownMushroomDepositMaxAltitude = CheckValue(this.brownMushroomDepositMaxAltitude, 1, ChunkMaxY, this.brownMushroomDepositMinAltitude);

        this.redMushroomDepositRarity = CheckValue(this.redMushroomDepositRarity, 0, 100);
        this.redMushroomDepositFrequency = CheckValue(this.redMushroomDepositFrequency, 0, 200);
        this.redMushroomDepositMinAltitude = CheckValue(this.redMushroomDepositMaxAltitude, 0, ChunkMaxY - 1);
        this.redMushroomDepositMaxAltitude = CheckValue(this.redMushroomDepositMaxAltitude, 1, ChunkMaxY, this.redMushroomDepositMinAltitude);

        this.reedDepositRarity = CheckValue(this.reedDepositRarity, 0, 100);
        this.reedDepositFrequency = CheckValue(this.reedDepositFrequency, 0, 200);
        this.reedDepositMinAltitude = CheckValue(this.reedDepositMaxAltitude, 0, ChunkMaxY - 1);
        this.reedDepositMaxAltitude = CheckValue(this.reedDepositMaxAltitude, 1, ChunkMaxY, this.reedDepositMinAltitude);

        this.pumpkinDepositRarity = CheckValue(this.pumpkinDepositRarity, 0, 100);
        this.pumpkinDepositFrequency = CheckValue(this.pumpkinDepositFrequency, 0, 200);
        this.pumpkinDepositMinAltitude = CheckValue(this.pumpkinDepositMaxAltitude, 0, ChunkMaxY - 1);
        this.pumpkinDepositMaxAltitude = CheckValue(this.pumpkinDepositMaxAltitude, 1, ChunkMaxY, this.pumpkinDepositMinAltitude);

        this.cactusDepositRarity = CheckValue(this.cactusDepositRarity, 0, 100);
        this.cactusDepositFrequency = CheckValue(this.cactusDepositFrequency, 0, 200);
        this.cactusDepositMinAltitude = CheckValue(this.cactusDepositMaxAltitude, 0, ChunkMaxY - 1);
        this.cactusDepositMaxAltitude = CheckValue(this.cactusDepositMaxAltitude, 1, ChunkMaxY, this.cactusDepositMinAltitude);

        this.longGrassDepositRarity = CheckValue(this.longGrassDepositRarity, 0, 100);
        this.longGrassDepositFrequency = CheckValue(this.longGrassDepositFrequency, 0, 200);
        this.longGrassDepositMinAltitude = CheckValue(this.longGrassDepositMaxAltitude, 0, ChunkMaxY - 1);
        this.longGrassDepositMaxAltitude = CheckValue(this.longGrassDepositMaxAltitude, 1, ChunkMaxY, this.longGrassDepositMinAltitude);

        this.deadBushDepositRarity = CheckValue(this.deadBushDepositRarity, 0, 100);
        this.deadBushDepositFrequency = CheckValue(this.deadBushDepositFrequency, 0, 200);
        this.deadBushDepositMinAltitude = CheckValue(this.deadBushDepositMaxAltitude, 0, ChunkMaxY - 1);
        this.deadBushDepositMaxAltitude = CheckValue(this.deadBushDepositMaxAltitude, 1, ChunkMaxY, this.deadBushDepositMinAltitude);

        this.waterSourceDepositRarity = CheckValue(this.waterSourceDepositRarity, 0, 100);
        this.waterSourceDepositFrequency = CheckValue(this.waterSourceDepositFrequency, 0, 200);
        this.waterSourceDepositMinAltitude = CheckValue(this.waterSourceDepositMaxAltitude, 0, ChunkMaxY - 1);
        this.waterSourceDepositMaxAltitude = CheckValue(this.waterSourceDepositMaxAltitude, 1, ChunkMaxY, this.waterSourceDepositMinAltitude);

        this.lavaSourceDepositRarity = CheckValue(this.lavaSourceDepositRarity, 0, 100);
        this.lavaSourceDepositFrequency = CheckValue(this.lavaSourceDepositFrequency, 0, 200);
        this.lavaSourceDepositMinAltitude = CheckValue(this.lavaSourceDepositMaxAltitude, 0, ChunkMaxY - 1);
        this.lavaSourceDepositMaxAltitude = CheckValue(this.lavaSourceDepositMaxAltitude, 1, ChunkMaxY, this.lavaSourceDepositMinAltitude);

        this.waterClayDepositRarity = CheckValue(this.waterClayDepositRarity, 0, 100);
        this.waterClayDepositFrequency = CheckValue(this.waterClayDepositFrequency, 0, 200);

        this.waterSandDepositRarity = CheckValue(this.waterSandDepositRarity, 0, 100);
        this.waterSandDepositFrequency = CheckValue(this.waterSandDepositFrequency, 0, 200);

        this.waterGravelDepositRarity = CheckValue(this.waterGravelDepositRarity, 0, 100);
        this.waterGravelDepositFrequency = CheckValue(this.waterGravelDepositFrequency, 0, 200);


        this.dirtDepositRarity1 = CheckValue(this.dirtDepositRarity1, 0, 100);
        this.dirtDepositFrequency1 = CheckValue(this.dirtDepositFrequency1, 0, 200);
        this.dirtDepositMinAltitude1 = CheckValue(this.dirtDepositMaxAltitude1, 0, ChunkMaxY - 1);
        this.dirtDepositMaxAltitude1 = CheckValue(this.dirtDepositMaxAltitude1, 1, ChunkMaxY, this.dirtDepositMinAltitude1);
        this.dirtDepositRarity2 = CheckValue(this.dirtDepositRarity2, 0, 100);
        this.dirtDepositFrequency2 = CheckValue(this.dirtDepositFrequency2, 0, 200);
        this.dirtDepositMinAltitude2 = CheckValue(this.dirtDepositMaxAltitude2, 0, ChunkMaxY - 1);
        this.dirtDepositMaxAltitude2 = CheckValue(this.dirtDepositMaxAltitude2, 1, ChunkMaxY, this.dirtDepositMinAltitude2);
        this.dirtDepositRarity3 = CheckValue(this.dirtDepositRarity3, 0, 100);
        this.dirtDepositFrequency3 = CheckValue(this.dirtDepositFrequency3, 0, 200);
        this.dirtDepositMinAltitude3 = CheckValue(this.dirtDepositMaxAltitude3, 0, ChunkMaxY - 1);
        this.dirtDepositMaxAltitude3 = CheckValue(this.dirtDepositMaxAltitude3, 1, ChunkMaxY, this.dirtDepositMinAltitude3);
        this.dirtDepositRarity4 = CheckValue(this.dirtDepositRarity4, 0, 100);
        this.dirtDepositFrequency4 = CheckValue(this.dirtDepositFrequency4, 0, 200);
        this.dirtDepositMinAltitude4 = CheckValue(this.dirtDepositMaxAltitude4, 0, ChunkMaxY - 1);
        this.dirtDepositMaxAltitude4 = CheckValue(this.dirtDepositMaxAltitude4, 1, ChunkMaxY, this.dirtDepositMinAltitude4);

        this.gravelDepositRarity1 = CheckValue(this.gravelDepositRarity1, 0, 100);
        this.gravelDepositFrequency1 = CheckValue(this.gravelDepositFrequency1, 0, 200);
        this.gravelDepositMinAltitude1 = CheckValue(this.gravelDepositMaxAltitude1, 0, ChunkMaxY - 1);
        this.gravelDepositMaxAltitude1 = CheckValue(this.gravelDepositMaxAltitude1, 1, ChunkMaxY, this.gravelDepositMinAltitude1);
        this.gravelDepositRarity2 = CheckValue(this.gravelDepositRarity2, 0, 100);
        this.gravelDepositFrequency2 = CheckValue(this.gravelDepositFrequency2, 0, 200);
        this.gravelDepositMinAltitude2 = CheckValue(this.gravelDepositMaxAltitude2, 0, ChunkMaxY - 1);
        this.gravelDepositMaxAltitude2 = CheckValue(this.gravelDepositMaxAltitude2, 1, ChunkMaxY, this.gravelDepositMinAltitude2);
        this.gravelDepositRarity3 = CheckValue(this.gravelDepositRarity3, 0, 100);
        this.gravelDepositFrequency3 = CheckValue(this.gravelDepositFrequency3, 0, 200);
        this.gravelDepositMinAltitude3 = CheckValue(this.gravelDepositMaxAltitude3, 0, ChunkMaxY - 1);
        this.gravelDepositMaxAltitude3 = CheckValue(this.gravelDepositMaxAltitude3, 1, ChunkMaxY, this.gravelDepositMinAltitude3);
        this.gravelDepositRarity4 = CheckValue(this.gravelDepositRarity4, 0, 100);
        this.gravelDepositFrequency4 = CheckValue(this.gravelDepositFrequency4, 0, 200);
        this.gravelDepositMinAltitude4 = CheckValue(this.gravelDepositMaxAltitude4, 0, ChunkMaxY - 1);
        this.gravelDepositMaxAltitude4 = CheckValue(this.gravelDepositMaxAltitude4, 1, ChunkMaxY, this.gravelDepositMinAltitude4);

        this.clayDepositRarity1 = CheckValue(this.clayDepositRarity1, 0, 100);
        this.clayDepositFrequency1 = CheckValue(this.clayDepositFrequency1, 0, 200);
        this.clayDepositMinAltitude1 = CheckValue(this.clayDepositMaxAltitude1, 0, ChunkMaxY - 1);
        this.clayDepositMaxAltitude1 = CheckValue(this.clayDepositMaxAltitude1, 1, ChunkMaxY, this.clayDepositMinAltitude1);
        this.clayDepositRarity2 = CheckValue(this.clayDepositRarity2, 0, 100);
        this.clayDepositFrequency2 = CheckValue(this.clayDepositFrequency2, 0, 200);
        this.clayDepositMinAltitude2 = CheckValue(this.clayDepositMaxAltitude2, 0, ChunkMaxY - 1);
        this.clayDepositMaxAltitude2 = CheckValue(this.clayDepositMaxAltitude2, 1, ChunkMaxY, this.clayDepositMinAltitude2);
        this.clayDepositRarity3 = CheckValue(this.clayDepositRarity3, 0, 100);
        this.clayDepositFrequency3 = CheckValue(this.clayDepositFrequency3, 0, 200);
        this.clayDepositMinAltitude3 = CheckValue(this.clayDepositMaxAltitude3, 0, ChunkMaxY - 1);
        this.clayDepositMaxAltitude3 = CheckValue(this.clayDepositMaxAltitude3, 1, ChunkMaxY, this.clayDepositMinAltitude3);
        this.clayDepositRarity4 = CheckValue(this.clayDepositRarity4, 0, 100);
        this.clayDepositFrequency4 = CheckValue(this.clayDepositFrequency4, 0, 200);
        this.clayDepositMinAltitude4 = CheckValue(this.clayDepositMaxAltitude4, 0, ChunkMaxY - 1);
        this.clayDepositMaxAltitude4 = CheckValue(this.clayDepositMaxAltitude4, 1, ChunkMaxY, this.clayDepositMinAltitude4);

        this.coalDepositRarity1 = CheckValue(this.coalDepositRarity1, 0, 100);
        this.coalDepositFrequency1 = CheckValue(this.coalDepositFrequency1, 0, 200);
        this.coalDepositMinAltitude1 = CheckValue(this.coalDepositMaxAltitude1, 0, ChunkMaxY - 1);
        this.coalDepositMaxAltitude1 = CheckValue(this.coalDepositMaxAltitude1, 1, ChunkMaxY, this.coalDepositMinAltitude1);
        this.coalDepositRarity2 = CheckValue(this.coalDepositRarity2, 0, 100);
        this.coalDepositFrequency2 = CheckValue(this.coalDepositFrequency2, 0, 200);
        this.coalDepositMinAltitude2 = CheckValue(this.coalDepositMaxAltitude2, 0, ChunkMaxY - 1);
        this.coalDepositMaxAltitude2 = CheckValue(this.coalDepositMaxAltitude2, 1, ChunkMaxY, this.coalDepositMinAltitude2);
        this.coalDepositRarity3 = CheckValue(this.coalDepositRarity3, 0, 100);
        this.coalDepositFrequency3 = CheckValue(this.coalDepositFrequency3, 0, 200);
        this.coalDepositMinAltitude3 = CheckValue(this.coalDepositMaxAltitude3, 0, ChunkMaxY - 1);
        this.coalDepositMaxAltitude3 = CheckValue(this.coalDepositMaxAltitude3, 1, ChunkMaxY, this.coalDepositMinAltitude3);
        this.coalDepositRarity4 = CheckValue(this.coalDepositRarity4, 0, 100);
        this.coalDepositFrequency4 = CheckValue(this.coalDepositFrequency4, 0, 200);
        this.coalDepositMinAltitude4 = CheckValue(this.coalDepositMaxAltitude4, 0, ChunkMaxY - 1);
        this.coalDepositMaxAltitude4 = CheckValue(this.coalDepositMaxAltitude4, 1, ChunkMaxY, this.coalDepositMinAltitude4);

        this.ironDepositRarity1 = CheckValue(this.ironDepositRarity1, 0, 100);
        this.ironDepositFrequency1 = CheckValue(this.ironDepositFrequency1, 0, 200);
        this.ironDepositMinAltitude1 = CheckValue(this.ironDepositMaxAltitude1, 0, ChunkMaxY - 1);
        this.ironDepositMaxAltitude1 = CheckValue(this.ironDepositMaxAltitude1, 1, ChunkMaxY, this.ironDepositMinAltitude1);
        this.ironDepositRarity2 = CheckValue(this.ironDepositRarity2, 0, 100);
        this.ironDepositFrequency2 = CheckValue(this.ironDepositFrequency2, 0, 200);
        this.ironDepositMinAltitude2 = CheckValue(this.ironDepositMaxAltitude2, 0, ChunkMaxY - 1);
        this.ironDepositMaxAltitude2 = CheckValue(this.ironDepositMaxAltitude2, 1, ChunkMaxY, this.ironDepositMinAltitude2);
        this.ironDepositRarity3 = CheckValue(this.ironDepositRarity3, 0, 100);
        this.ironDepositFrequency3 = CheckValue(this.ironDepositFrequency3, 0, 200);
        this.ironDepositMinAltitude3 = CheckValue(this.ironDepositMaxAltitude3, 0, ChunkMaxY - 1);
        this.ironDepositMaxAltitude3 = CheckValue(this.ironDepositMaxAltitude3, 1, ChunkMaxY, this.ironDepositMinAltitude3);
        this.ironDepositRarity4 = CheckValue(this.ironDepositRarity4, 0, 100);
        this.ironDepositFrequency4 = CheckValue(this.ironDepositFrequency4, 0, 200);
        this.ironDepositMinAltitude4 = CheckValue(this.ironDepositMaxAltitude4, 0, ChunkMaxY - 1);
        this.ironDepositMaxAltitude4 = CheckValue(this.ironDepositMaxAltitude4, 1, ChunkMaxY, this.ironDepositMinAltitude4);

        this.goldDepositRarity1 = CheckValue(this.goldDepositRarity1, 0, 100);
        this.goldDepositFrequency1 = CheckValue(this.goldDepositFrequency1, 0, 200);
        this.goldDepositMinAltitude1 = CheckValue(this.goldDepositMaxAltitude1, 0, ChunkMaxY - 1);
        this.goldDepositMaxAltitude1 = CheckValue(this.goldDepositMaxAltitude1, 1, ChunkMaxY, this.goldDepositMinAltitude1);
        this.goldDepositRarity2 = CheckValue(this.goldDepositRarity2, 0, 100);
        this.goldDepositFrequency2 = CheckValue(this.goldDepositFrequency2, 0, 200);
        this.goldDepositMinAltitude2 = CheckValue(this.goldDepositMaxAltitude2, 0, ChunkMaxY - 1);
        this.goldDepositMaxAltitude2 = CheckValue(this.goldDepositMaxAltitude2, 1, ChunkMaxY, this.goldDepositMinAltitude2);
        this.goldDepositRarity3 = CheckValue(this.goldDepositRarity3, 0, 100);
        this.goldDepositFrequency3 = CheckValue(this.goldDepositFrequency3, 0, 200);
        this.goldDepositMinAltitude3 = CheckValue(this.goldDepositMaxAltitude3, 0, ChunkMaxY - 1);
        this.goldDepositMaxAltitude3 = CheckValue(this.goldDepositMaxAltitude3, 1, ChunkMaxY, this.goldDepositMinAltitude3);
        this.goldDepositRarity4 = CheckValue(this.goldDepositRarity4, 0, 100);
        this.goldDepositFrequency4 = CheckValue(this.goldDepositFrequency4, 0, 200);
        this.goldDepositMinAltitude4 = CheckValue(this.goldDepositMaxAltitude4, 0, ChunkMaxY - 1);
        this.goldDepositMaxAltitude4 = CheckValue(this.goldDepositMaxAltitude4, 1, ChunkMaxY, this.goldDepositMinAltitude4);

        this.redstoneDepositRarity1 = CheckValue(this.redstoneDepositRarity1, 0, 100);
        this.redstoneDepositFrequency1 = CheckValue(this.redstoneDepositFrequency1, 0, 200);
        this.redstoneDepositMinAltitude1 = CheckValue(this.redstoneDepositMaxAltitude1, 0, ChunkMaxY - 1);
        this.redstoneDepositMaxAltitude1 = CheckValue(this.redstoneDepositMaxAltitude1, 1, ChunkMaxY, this.redstoneDepositMinAltitude1);
        this.redstoneDepositRarity2 = CheckValue(this.redstoneDepositRarity2, 0, 100);
        this.redstoneDepositFrequency2 = CheckValue(this.redstoneDepositFrequency2, 0, 200);
        this.redstoneDepositMinAltitude2 = CheckValue(this.redstoneDepositMaxAltitude2, 0, ChunkMaxY - 1);
        this.redstoneDepositMaxAltitude2 = CheckValue(this.redstoneDepositMaxAltitude2, 1, ChunkMaxY, this.redstoneDepositMinAltitude2);
        this.redstoneDepositRarity3 = CheckValue(this.redstoneDepositRarity3, 0, 100);
        this.redstoneDepositFrequency3 = CheckValue(this.redstoneDepositFrequency3, 0, 200);
        this.redstoneDepositMinAltitude3 = CheckValue(this.redstoneDepositMaxAltitude3, 0, ChunkMaxY - 1);
        this.redstoneDepositMaxAltitude3 = CheckValue(this.redstoneDepositMaxAltitude3, 1, ChunkMaxY, this.redstoneDepositMinAltitude3);
        this.redstoneDepositRarity4 = CheckValue(this.redstoneDepositRarity4, 0, 100);
        this.redstoneDepositFrequency4 = CheckValue(this.redstoneDepositFrequency4, 0, 200);
        this.redstoneDepositMinAltitude4 = CheckValue(this.redstoneDepositMaxAltitude4, 0, ChunkMaxY - 1);
        this.redstoneDepositMaxAltitude4 = CheckValue(this.redstoneDepositMaxAltitude4, 1, ChunkMaxY, this.redstoneDepositMinAltitude4);

        this.diamondDepositRarity1 = CheckValue(this.diamondDepositRarity1, 0, 100);
        this.diamondDepositFrequency1 = CheckValue(this.diamondDepositFrequency1, 0, 200);
        this.diamondDepositMinAltitude1 = CheckValue(this.diamondDepositMaxAltitude1, 0, ChunkMaxY - 1);
        this.diamondDepositMaxAltitude1 = CheckValue(this.diamondDepositMaxAltitude1, 1, ChunkMaxY, this.diamondDepositMinAltitude1);
        this.diamondDepositRarity2 = CheckValue(this.diamondDepositRarity2, 0, 100);
        this.diamondDepositFrequency2 = CheckValue(this.diamondDepositFrequency2, 0, 200);
        this.diamondDepositMinAltitude2 = CheckValue(this.diamondDepositMaxAltitude2, 0, ChunkMaxY - 1);
        this.diamondDepositMaxAltitude2 = CheckValue(this.diamondDepositMaxAltitude2, 1, ChunkMaxY, this.diamondDepositMinAltitude2);
        this.diamondDepositRarity3 = CheckValue(this.diamondDepositRarity3, 0, 100);
        this.diamondDepositFrequency3 = CheckValue(this.diamondDepositFrequency3, 0, 200);
        this.diamondDepositMinAltitude3 = CheckValue(this.diamondDepositMaxAltitude3, 0, ChunkMaxY - 1);
        this.diamondDepositMaxAltitude3 = CheckValue(this.diamondDepositMaxAltitude3, 1, ChunkMaxY, this.diamondDepositMinAltitude3);
        this.diamondDepositRarity4 = CheckValue(this.diamondDepositRarity4, 0, 100);
        this.diamondDepositFrequency4 = CheckValue(this.diamondDepositFrequency4, 0, 200);
        this.diamondDepositMinAltitude4 = CheckValue(this.diamondDepositMaxAltitude4, 0, ChunkMaxY - 1);
        this.diamondDepositMaxAltitude4 = CheckValue(this.diamondDepositMaxAltitude4, 1, ChunkMaxY, this.diamondDepositMinAltitude4);

        this.lapislazuliDepositRarity1 = CheckValue(this.lapislazuliDepositRarity1, 0, 100);
        this.lapislazuliDepositFrequency1 = CheckValue(this.lapislazuliDepositFrequency1, 0, 200);
        this.lapislazuliDepositMinAltitude1 = CheckValue(this.lapislazuliDepositMaxAltitude1, 0, ChunkMaxY - 1);
        this.lapislazuliDepositMaxAltitude1 = CheckValue(this.lapislazuliDepositMaxAltitude1, 1, ChunkMaxY, this.lapislazuliDepositMinAltitude1);
        this.lapislazuliDepositRarity2 = CheckValue(this.lapislazuliDepositRarity2, 0, 100);
        this.lapislazuliDepositFrequency2 = CheckValue(this.lapislazuliDepositFrequency2, 0, 200);
        this.lapislazuliDepositMinAltitude2 = CheckValue(this.lapislazuliDepositMaxAltitude2, 0, ChunkMaxY - 1);
        this.lapislazuliDepositMaxAltitude2 = CheckValue(this.lapislazuliDepositMaxAltitude2, 1, ChunkMaxY, this.lapislazuliDepositMinAltitude2);
        this.lapislazuliDepositRarity3 = CheckValue(this.lapislazuliDepositRarity3, 0, 100);
        this.lapislazuliDepositFrequency3 = CheckValue(this.lapislazuliDepositFrequency3, 0, 200);
        this.lapislazuliDepositMinAltitude3 = CheckValue(this.lapislazuliDepositMaxAltitude3, 0, ChunkMaxY - 1);
        this.lapislazuliDepositMaxAltitude3 = CheckValue(this.lapislazuliDepositMaxAltitude3, 1, ChunkMaxY, this.lapislazuliDepositMinAltitude3);
        this.lapislazuliDepositRarity4 = CheckValue(this.lapislazuliDepositRarity4, 0, 100);
        this.lapislazuliDepositFrequency4 = CheckValue(this.lapislazuliDepositFrequency4, 0, 200);
        this.lapislazuliDepositMinAltitude4 = CheckValue(this.lapislazuliDepositMaxAltitude4, 0, ChunkMaxY - 1);
        this.lapislazuliDepositMaxAltitude4 = CheckValue(this.lapislazuliDepositMaxAltitude4, 1, ChunkMaxY, this.lapislazuliDepositMinAltitude4);


    }


    private int DefaultTrees = 0;
    private int DefaultFlowers = 2;
    private int DefaultGrass = 1;
    private int DefaultDeadBrush = 0;
    private int DefaultMushroom = 0;
    private int DefaultReed = 0;
    private int DefaultCactus = 0;
    private int DefaultSand = 1;
    private int DefaultGravel = 3;
    private int DefaultClay = 1;
    private float DefaultBiomeSurface = 0.1F;
    private float DefaultBiomeVolatility = 0.3F;


    private void InitDefaults()
    {
        this.DefaultBiomeSurface = this.Biome.q;
        this.DefaultBiomeVolatility = this.Biome.r;

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
        }

    }

}
