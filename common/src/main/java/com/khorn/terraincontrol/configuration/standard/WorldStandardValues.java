package com.khorn.terraincontrol.configuration.standard;

import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.util.MultiTypedSetting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public enum WorldStandardValues implements MultiTypedSetting
{        
//>>	WorldConfig Name
    ConfigFilename("WorldConfig.ini"),
    //>> Folders
    BiomeConfigDirectoryName("WorldBiomes"),
    BO_DirectoryName("WorldObjects"),
  //>>  World Defaults
    SettingsMode(WorldConfig.ConfigMode.WriteAll),
    TerrainMode(WorldConfig.TerrainMode.Normal),
    
    BiomeMode("Normal"),
    
    snowAndIceMaxTemp(0.15F),
    WorldHeightScaleBits(7),
    WorldHeightCapBits(8),

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
    RandomRivers(false),
    ImprovedRivers(false),

    WaterLevelMax(63),
    WaterLevelMin(0),
    WaterBlock(DefaultMaterial.STATIONARY_WATER),
    IceBlock(DefaultMaterial.ICE),
    
    FrozenOcean(true),

    NormalBiomes("Desert,Forest,Extreme Hills,Swampland,Plains,Taiga,Jungle", SettingsType.StringArray),
    IceBiomes("Ice Plains", SettingsType.StringArray),
    IsleBiomes("MushroomIsland,Ice Mountains,DesertHills,ForestHills,TaigaHills,River,JungleHills", SettingsType.StringArray),
    BorderBiomes("MushroomIslandShore,Beach,Extreme Hills Edge", SettingsType.StringArray),

    CustomBiomes("", SettingsType.StringArray),

    ImageMode(WorldConfig.ImageMode.Mirror),
    ImageFile("map.png"),
    ImageOrientation(WorldConfig.ImageOrientation.West),
    ImageFillBiome("Ocean"),
    ImageXOffset(0),
    ImageZOffset(0),

    oldBiomeSize(1.5D),
    minMoisture(0.0f),
    maxMoisture(1.0f),
    minTemperature(0.0f),
    maxTemperature(1.0f),

    WorldFog("0xC0D8FF", SettingsType.Color),
    WorldNightFog("0x0B0D17", SettingsType.Color),

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

    canyonRarity(2),
    canyonMinAltitude(20),
    canyonMaxAltitude(68),
    canyonMinLength(84),
    canyonMaxLength(112),
    canyonDepth(3.0D),

    FractureHorizontal(0.0D),
    FractureVertical(0.0D),

    DisableBedrock(false),
    CeilingBedrock(false),
    FlatBedrock(false),
    BedrockobBlock(DefaultMaterial.BEDROCK),
    RemoveSurfaceStone(false),
    objectSpawnRatio(1),
    ResourcesSeed(0L), // "L" means that it is a long instead of an int
    PopulationBoundsCheck(true),

    //>>	Some settings here are similar to BiomeStandardValues
    NetherFortressesEnabled(false),

    StrongholdsEnabled(true),
    StrongholdCount(3),
    StrongholdDistance(32.0),
    StrongholdSpread(3),

    VillagesEnabled(true),
    VillageDistance(32),
    VillageSize(0),
    VillageType(BiomeConfig.VillageType.disabled),

    MineshaftsEnabled(true),
    MineshaftRarity(1D),

    RareBuildingsEnabled(true),
    MinimumDistanceBetweenRareBuildings(9),
    MaximumDistanceBetweenRareBuildings(32),
    RareBuildingType(BiomeConfig.RareBuildingType.disabled),
    // End world settings

    // Begin deprecated settings
    FrozenRivers(true);
    // End deprecated settings

    private int iValue;
    private long lValue;
    private double dValue;
    private float fValue;
    private String sValue;
    private boolean bValue;
    private Enum<?> eValue;
    private SettingsType returnType;
    private ArrayList<String> sArrayValue;
    private HashSet<Integer> iSetValue;
    private DefaultMaterial mValue;

    private WorldStandardValues(int i)
    {
        this.iValue = i;
        this.returnType = SettingsType.Int;
    }

    @SuppressWarnings("UnusedDeclaration")
    private WorldStandardValues(HashSet<Integer> i)
    {
        this.iSetValue = i;
        this.returnType = SettingsType.IntSet;
    }

    private WorldStandardValues(double d)
    {
        this.dValue = d;
        this.returnType = SettingsType.Double;
    }

    private WorldStandardValues(float f)
    {
        this.fValue = f;
        this.returnType = SettingsType.Float;
    }

    private WorldStandardValues(long l)
    {
        this.lValue = l;
        this.returnType = SettingsType.Long;
    }

    private WorldStandardValues(String s)
    {
        this.sValue = s;
        this.returnType = SettingsType.String;
    }

    private WorldStandardValues(String s, SettingsType type)
    {
        this.returnType = type;

        if (type == SettingsType.StringArray)
        {
            this.sArrayValue = new ArrayList<String>();
            if (s.contains(","))
                Collections.addAll(this.sArrayValue, s.split(","));
            else if (!s.isEmpty())
                this.sArrayValue.add(s);
            return;
        }
        this.sValue = s;

    }

    private WorldStandardValues(Enum<?> e)
    {
        this.eValue = e;
        this.returnType = SettingsType.Enum;

    }

    private WorldStandardValues(boolean b)
    {
        this.bValue = b;
        this.returnType = SettingsType.Boolean;
    }

    private WorldStandardValues(DefaultMaterial material)
    {
        this.mValue = material;
        this.returnType = SettingsType.Material;
    }

    @Override
    public int intValue()
    {
        return this.iValue;
    }

    @Override
    public long longValue()
    {
        return this.lValue;
    }

    @Override
    public double doubleValue()
    {
        return this.dValue;
    }

    @Override
    public float floatValue()
    {
        return this.fValue;
    }

    @Override
    public Enum<?> enumValue()
    {
        return this.eValue;
    }

    @Override
    public SettingsType getReturnType()
    {
        return this.returnType;
    }

    @Override
    public String stringValue()
    {
        return this.sValue;
    }

    @Override
    public ArrayList<String> stringArrayListValue()
    {
        return this.sArrayValue;
    }

    @Override
    public boolean booleanValue()
    {
        return this.bValue;
    }

    @Override
    public HashSet<Integer> intSetValue()
    {
        return this.iSetValue;
    }

    @Override
    public DefaultMaterial materialValue()
    {
        return this.mValue;
    }

}