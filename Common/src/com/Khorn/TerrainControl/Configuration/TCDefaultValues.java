package com.Khorn.TerrainControl.Configuration;

import java.util.ArrayList;
import java.util.Collections;

public enum TCDefaultValues
{
    WorldSettingsName("WorldConfig.ini"),
    WorldBOBDirectoryName("BOBPlugins"),
    WorldBiomeConfigDirectoryName("BiomeConfigs"),
    WorldBiomeConfigName("BiomeConfig.ini"),
    ChannelName("TerrainControl"),
    ProtocolVersion(2),

    maxChunkBlockValue(32768),

    CustomHeightControl("0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0"),
    CustomBiomes(""),
    NormalBiomes("Desert,Forest,Extreme Hills,Swampland,Plains,Taiga,Jungle"),
    IceBiomes("Ice Plains"),
    IsleBiomes("MushroomIsland,Ice Mountains,DesertHills,ForestHills,TaigaHills,River,JungleHills"),
    BorderBiomes("MushroomIslandShore,Beach,Extreme Hills Edge"),

    ModeTerrain("Normal"),
    ModeBiome("Normal"),

    GenerationDepth(10),
    BiomeRarityScale(100),
    LandRarity(97),
    LandSize(0),
    LandFuzzy(6),
    IceRarity(90),
    IceSize(3),
    RiverRarity(4),
    RiverSize(0),
    RiversEnabled(true),

    FrozenRivers(true),
    FrozenOcean(true),

    oldBiomeSize(1.5D),


    BiomeSize(5),
    BiomeRarity(100),
    BiomeColor(""),
    BiomeRivers(true),
    IsleInBiome("Ocean"),
    BiomeIsBorder(""),
    NotBorderNear(""),

    BiomeTemperature(0.5F),
    BiomeWetness(0.5F),


    SurfaceBlock(2),
    GroundBlock(3),

    minMoisture(0.0D),
    maxMoisture(1.0D),
    minTemperature(0.0D),
    maxTemperature(1.0D),

    snowThreshold(0.5D),
    iceThreshold(0.5D),
    swampSize(2),
    desertDirtFrequency(0),
    muddySwamps(false),
    claySwamps(false),
    waterlessDeserts(false),
    desertDirt(false),

    WaterLevelMax(64),
    WaterLevelMin(0),
    WaterBlock(9),
    IceBlock(79),
    MaxAverageHeight(0.0D),
    MaxAverageDepth(0.0D),
    FractureHorizontal(0.0D),
    FractureVertical(0.0D),
    Volatility1(0.0D),
    Volatility2(0.0D),
    VolatilityWeight1(0.5D),
    VolatilityWeight2(0.45D),
    RemoveSurfaceStone(false),
    DisableBedrock(false),
    CeilingBedrock(false),
    FlatBedrock(false),
    BedrockobBlock(7),
    disableNotchPonds(false),
    WorldHeightBits(7),


    WorldFog("0xC0D8FF"),
    WorldNightFog("0x0B0D17"),
    SkyColor("0x7BA5FF"),
    WaterColor("0xFFFFFF"),
    GrassColor("0x000000"),
    FoliageColor("0x000000"),


    StrongholdsEnabled(true),
    MineshaftsEnabled(true),
    VillagesEnabled(true),

    canyonRarity(2),
    canyonMinAltitude(20),
    canyonMaxAltitude(68),
    canyonMinLength(84),
    canyonMaxLength(112),
    canyonDepth(3.0D),

    caveRarity(7),
    caveFrequency(40),
    caveMinAltitude(8),
    caveMaxAltitude(128),
    individualCaveRarity(25),
    caveSystemFrequency(1),
    caveSystemPocketChance(0),
    caveSystemPocketMinSize(0),
    caveSystemPocketMaxSize(4),
    evenCaveDistribution(false),

    dungeonRarity(100),
    dungeonFrequency(8),
    dungeonMinAltitude(0),

    vinesRarity(100),
    vinesFrequency(50),
    vinesMinAltitude(64),

    DisableBiomeHeight(false),

    undergroundLakeFrequency(2),
    undergroundLakeRarity(5),
    undergroundLakeMinSize(50),
    undergroundLakeMaxSize(60),
    undergroundLakeMinAltitude(0),
    undergroundLakeMaxAltitude(50),


    BiomeHeight(0.1D),
    BiomeVolatility(0.3D),

    CustomObjects(true),
    objectSpawnRatio(2),
    DenyObjectsUnderFill(false),
    customTreeChance(50),


    cactusDepositRarity(100),
    cactusDepositMinAltitude(0),
    cactusDepositMaxAltitude(128),

    flowerDepositRarity(100),
    flowerDepositMinAltitude(0),
    flowerDepositMaxAltitude(128),

    roseDepositRarity(100),
    roseDepositMinAltitude(0),
    roseDepositMaxAltitude(128),

    brownMushroomDepositRarity(100),
    brownMushroomDepositMinAltitude(0),
    brownMushroomDepositMaxAltitude(128),

    redMushroomDepositRarity(100),
    redMushroomDepositMinAltitude(0),
    redMushroomDepositMaxAltitude(128),

    reedDepositRarity(100),
    reedDepositMinAltitude(0),
    reedDepositMaxAltitude(128),

    pumpkinDepositRarity(3),
    pumpkinDepositFrequency(1),
    pumpkinDepositMinAltitude(0),
    pumpkinDepositMaxAltitude(128),

    longGrassDepositRarity(100),

    deadBushDepositRarity(100),


    waterSourceDepositRarity(100),
    waterSourceDepositFrequency(20),
    waterSourceDepositMinAltitude(8),
    waterSourceDepositMaxAltitude(128),

    lavaSourceDepositRarity(100),
    lavaSourceDepositFrequency(10),
    lavaSourceDepositMinAltitude(8),
    lavaSourceDepositMaxAltitude(128),

    waterClayDepositRarity(100),
    waterClayDepositSize(4),

    waterSandDepositRarity(100),
    waterSandDepositFrequency(4),
    waterSandDepositSize(7),

    dirtDepositRarity(100),
    dirtDepositFrequency(20),
    dirtDepositSize(32),
    dirtDepositMinAltitude(0),
    dirtDepositMaxAltitude(128),

    gravelDepositRarity(100),
    gravelDepositFrequency(10),
    gravelDepositSize(32),
    gravelDepositMinAltitude(0),
    gravelDepositMaxAltitude(128),

    clayDepositRarity(100),
    clayDepositFrequency(1),
    clayDepositSize(32),
    clayDepositMinAltitude(0),
    clayDepositMaxAltitude(128),

    coalDepositRarity(100),
    coalDepositFrequency(20),
    coalDepositSize(16),
    coalDepositMinAltitude(0),
    coalDepositMaxAltitude(128),

    ironDepositRarity(100),
    ironDepositFrequency(20),
    ironDepositSize(8),
    ironDepositMinAltitude(0),
    ironDepositMaxAltitude(64),

    goldDepositRarity(100),
    goldDepositFrequency(2),
    goldDepositSize(8),
    goldDepositMinAltitude(0),
    goldDepositMaxAltitude(32),

    redstoneDepositRarity(100),
    redstoneDepositFrequency(8),
    redstoneDepositSize(7),
    redstoneDepositMinAltitude(0),
    redstoneDepositMaxAltitude(16),

    diamondDepositRarity(100),
    diamondDepositFrequency(1),
    diamondDepositSize(7),
    diamondDepositMinAltitude(0),
    diamondDepositMaxAltitude(16),

    lapislazuliDepositRarity(100),
    lapislazuliDepositFrequency(1),
    lapislazuliDepositSize(7),
    lapislazuliDepositMinAltitude(0),
    lapislazuliDepositMaxAltitude(16),


    evenWaterSourceDistribution(false),
    evenLavaSourceDistribution(false);


    private int iValue;
    private double dValue;
    private String sValue;
    private boolean bValue;

    private TCDefaultValues(int i)
    {
        this.iValue = i;
    }

    private TCDefaultValues(double d)
    {
        this.dValue = d;
    }

    private TCDefaultValues(String s)
    {
        this.sValue = s;
    }

    private TCDefaultValues(Boolean b)
    {
        this.bValue = b;
    }


    public int intValue()
    {
        return this.iValue;
    }

    public double doubleValue()
    {
        return this.dValue;
    }

    public float floatValue()
    {
        return (float) this.dValue;
    }

    public String stringValue()
    {
        return this.sValue;
    }

    public ArrayList<String> StringArrayListValue()
    {
        ArrayList<String> out = new ArrayList<String>();
        if (this.sValue.contains(","))
            Collections.addAll(out, this.sValue.split(","));
        else if (!this.sValue.equals(""))
            out.add(this.sValue);
        return out;
    }


    public Boolean booleanValue()
    {
        return this.bValue;
    }
}