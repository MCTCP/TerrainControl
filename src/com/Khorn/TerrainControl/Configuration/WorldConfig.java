package com.Khorn.TerrainControl.Configuration;

import com.Khorn.TerrainControl.CustomObjects.CustomObject;
import com.Khorn.TerrainControl.Generator.ChunkProviderTC;
import com.Khorn.TerrainControl.Generator.ObjectSpawner;
import com.Khorn.TerrainControl.TCDefaultValues;
import com.Khorn.TerrainControl.TCPlugin;
import com.Khorn.TerrainControl.Util.CustomBiome;
import net.minecraft.server.BiomeBase;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class WorldConfig extends ConfigFile
{

    public ArrayList<String> CustomBiomes = new ArrayList<String>();

    public ArrayList<String> NormalBiomes = new ArrayList<String>();
    public ArrayList<String> IceBiomes = new ArrayList<String>();
    public ArrayList<String> IsleBiomes = new ArrayList<String>();
    public ArrayList<String> BorderBiomes = new ArrayList<String>();

    public ArrayList<CustomObject> Objects = new ArrayList<CustomObject>();
    public HashMap<String, ArrayList<CustomObject>> ObjectGroups = new HashMap<String, ArrayList<CustomObject>>();
    public HashMap<String, ArrayList<CustomObject>> BranchGroups = new HashMap<String, ArrayList<CustomObject>>();
    public boolean HasCustomTrees = false;

    // public BiomeBase currentBiome;
    // --Commented out by Inspection (17.07.11 1:49):String seedValue;


    // For old biome generator
    public double oldBiomeSize;


    public float minMoisture;
    public float maxMoisture;
    public float minTemperature;
    public float maxTemperature;


    // Biome generator
    public int GenerationDepth;
    public int BiomeRarityScale;

    public int LandRarity;
    public int LandSize;
    public int LandFuzzy;

    public int IceRarity;
    public int IceSize;

    public int RiverRarity;
    public int RiverSize;
    public boolean RiversEnabled;

    public boolean FrozenRivers;
    public boolean FrozenOcean;


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

    public int waterLevelMax;
    public int waterLevelMin;
    public int waterBlock;
    public int iceBlock;
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
    public int customTreeChance;

    public boolean StrongholdsEnabled;
    public boolean MineshaftsEnabled;
    public boolean VillagesEnabled;


    private File SettingsDir;
    public TCPlugin plugin;
    public ChunkProviderTC ChunkProvider;
    public ObjectSpawner objectSpawner;

    public boolean isInit = false;

    public boolean isDeprecated = false;
    public WorldConfig newSettings = null;

    public String WorldName;
    public TerrainMode ModeTerrain;
    public BiomeMode ModeBiome;


    public BiomeConfig[] biomeConfigs;
    public boolean BiomeConfigsHaveReplacement = false;

    public static final int DefaultBiomesCount = 21;
    public static int ExtendedBiomesCount = 0;

    public int normalBiomesRarity;
    public int iceBiomesRarity;


    public int ChunkMaxY = 128;


    public WorldConfig(File settingsDir, TCPlugin plug, String worldName)
    {
        this.SettingsDir = settingsDir;
        this.WorldName = worldName;

        File settingsFile = new File(this.SettingsDir, TCDefaultValues.WorldSettingsName.stringValue());

        this.ReadSettingsFile(settingsFile);
        this.RenameOldSettings();
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

        ArrayList<BiomeBase> biomes = new ArrayList<BiomeBase>();

        biomes.addAll(Arrays.asList(BiomeBase.a).subList(0, DefaultBiomesCount));

        for (String biomeName : this.CustomBiomes)
        {
            boolean isNew = true;
            for (int i = DefaultBiomesCount; i < DefaultBiomesCount + ExtendedBiomesCount; i++)
                if (BiomeBase.a[i].w.equals(biomeName))
                    isNew = false;
            if (isNew)
                new CustomBiome(DefaultBiomesCount + ExtendedBiomesCount++, biomeName);
        }

        for (int i = DefaultBiomesCount; i < DefaultBiomesCount + ExtendedBiomesCount; i++)
            if (this.CustomBiomes.contains(BiomeBase.a[i].w))
                biomes.add(BiomeBase.a[i]);


        for (BiomeBase biome : biomes)
        {
            BukkitBiome bukkitBiome = new BukkitBiome(biome);
            BiomeConfig config = new BiomeConfig(BiomeFolder, bukkitBiome, this);

            if (this.NormalBiomes.contains(config.Name))
                this.normalBiomesRarity += config.BiomeRarity;
            if (this.IceBiomes.contains(config.Name))
                this.iceBiomesRarity += config.BiomeRarity;

            this.biomeConfigs[biome.K] = config;
            if (!this.BiomeConfigsHaveReplacement)
                this.BiomeConfigsHaveReplacement = config.replaceBlocks.size() > 0;


        }


        this.RegisterBOBPlugins();
        this.plugin = plug;
    }

    protected  void RenameOldSettings()
    {

        if(this.SettingsCache.containsKey("WaterLevel"))
        {
            this.SettingsCache.put("WaterLevelMax".toLowerCase(),this.SettingsCache.get("WaterLevel"));
        }

    }

    protected void CorrectSettings()
    {

        this.oldBiomeSize = CheckValue(this.oldBiomeSize, 0.1D, 10.0D);

        this.GenerationDepth = CheckValue(this.GenerationDepth, 1, 20);
        this.BiomeRarityScale = CheckValue(this.BiomeRarityScale, 1, Integer.MAX_VALUE);

        this.LandRarity = CheckValue(this.LandRarity, 1, 100);
        this.LandSize = CheckValue(this.LandSize, 0, this.GenerationDepth);
        this.LandFuzzy = CheckValue(this.LandFuzzy, 0, this.GenerationDepth - this.LandSize);


        this.IceRarity = CheckValue(this.IceRarity, 1, 100);
        this.IceSize = CheckValue(this.IceSize, 0, this.GenerationDepth);

        this.RiverRarity = CheckValue(this.RiverRarity, 0, this.GenerationDepth);
        this.RiverSize = CheckValue(this.RiverSize, 0, this.GenerationDepth - this.RiverRarity);

        this.NormalBiomes = CheckValue(this.NormalBiomes, this.CustomBiomes);
        this.IceBiomes = CheckValue(this.IceBiomes, this.CustomBiomes);
        this.IsleBiomes = CheckValue(this.IsleBiomes, this.CustomBiomes);
        this.BorderBiomes = CheckValue(this.BorderBiomes, this.CustomBiomes);


        this.minMoisture = (this.minMoisture < 0.0F ? 0.0F : this.minMoisture > 1.0F ? 1.0F : this.minMoisture);
        this.minTemperature = (this.minTemperature < 0.0F ? 0.0F : this.minTemperature > 1.0F ? 1.0F : this.minTemperature);
        this.maxMoisture = (this.maxMoisture > 1.0F ? 1.0F : this.maxMoisture < this.minMoisture ? this.minMoisture : this.maxMoisture);
        this.maxTemperature = (this.maxTemperature > 1.0F ? 1.0F : this.maxTemperature < this.minTemperature ? this.minTemperature : this.maxTemperature);


        this.caveRarity = (this.caveRarity < 0 ? 0 : this.caveRarity > 100 ? 100 : this.caveRarity);
        this.caveFrequency = (this.caveFrequency < 0 ? 0 : this.caveFrequency);
        this.caveMinAltitude = (this.caveMinAltitude < 0 ? 0 : this.caveMinAltitude > this.ChunkMaxY - 1 ? this.ChunkMaxY - 1 : this.caveMinAltitude);
        this.caveMaxAltitude = (this.caveMaxAltitude > this.ChunkMaxY ? this.ChunkMaxY : this.caveMaxAltitude <= this.caveMinAltitude ? this.caveMinAltitude + 1 : this.caveMaxAltitude);
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


        this.waterLevelMin = CheckValue(this.waterLevelMin, 0, 128);
        this.waterLevelMax = CheckValue(this.waterLevelMax, 0, 128, this.waterLevelMin);

        this.customTreeChance = CheckValue(this.customTreeChance,0,100);

        if (this.ModeBiome == BiomeMode.OldGenerator && this.ModeTerrain != TerrainMode.OldGenerator)
        {
            System.out.println("TerrainControl: Old biome generator works only with old terrain generator!");
            this.ModeBiome = BiomeMode.Normal;

        }


    }


    protected void ReadConfigSettings()
    {
        try
        {
            this.ModeTerrain = TerrainMode.valueOf(ReadModSettings(TCDefaultValues.ModeTerrain.name(), TCDefaultValues.ModeTerrain.stringValue()));
        } catch (IllegalArgumentException e)
        {
            this.ModeTerrain = TerrainMode.Normal;
        }

        try
        {
            this.ModeBiome = BiomeMode.valueOf(ReadModSettings(TCDefaultValues.ModeBiome.name(), TCDefaultValues.ModeBiome.stringValue()));
        } catch (IllegalArgumentException e)
        {
            this.ModeBiome = BiomeMode.Normal;
        }


        this.oldBiomeSize = ReadModSettings(TCDefaultValues.oldBiomeSize.name(), TCDefaultValues.oldBiomeSize.doubleValue());

        this.GenerationDepth = ReadModSettings(TCDefaultValues.GenerationDepth.name(), TCDefaultValues.GenerationDepth.intValue());
        
        this.BiomeRarityScale = ReadModSettings(TCDefaultValues.BiomeRarityScale.name(), TCDefaultValues.BiomeRarityScale.intValue());
        this.LandRarity = ReadModSettings(TCDefaultValues.LandRarity.name(), TCDefaultValues.LandRarity.intValue());
        this.LandSize = ReadModSettings(TCDefaultValues.LandSize.name(), TCDefaultValues.LandSize.intValue());
        this.LandFuzzy = ReadModSettings(TCDefaultValues.LandFuzzy.name(), TCDefaultValues.LandFuzzy.intValue());

        this.IceRarity = ReadModSettings(TCDefaultValues.IceRarity.name(), TCDefaultValues.IceRarity.intValue());
        this.IceSize = ReadModSettings(TCDefaultValues.IceSize.name(), TCDefaultValues.IceSize.intValue());

        this.RiverRarity = ReadModSettings(TCDefaultValues.RiverRarity.name(), TCDefaultValues.RiverRarity.intValue());
        this.RiverSize = ReadModSettings(TCDefaultValues.RiverSize.name(), TCDefaultValues.RiverSize.intValue());
        this.RiversEnabled = ReadModSettings(TCDefaultValues.RiversEnabled.name(), TCDefaultValues.RiversEnabled.booleanValue());

        this.FrozenRivers = ReadModSettings(TCDefaultValues.FrozenRivers.name(), TCDefaultValues.FrozenRivers.booleanValue());
        this.FrozenOcean = ReadModSettings(TCDefaultValues.FrozenOcean.name(), TCDefaultValues.FrozenOcean.booleanValue());


        this.NormalBiomes = this.ReadModSettings(TCDefaultValues.NormalBiomes.name(), TCDefaultValues.NormalBiomes.StringArrayListValue());
        this.IceBiomes = this.ReadModSettings(TCDefaultValues.IceBiomes.name(), TCDefaultValues.IceBiomes.StringArrayListValue());
        this.IsleBiomes = this.ReadModSettings(TCDefaultValues.IsleBiomes.name(), TCDefaultValues.IsleBiomes.StringArrayListValue());
        this.BorderBiomes = this.ReadModSettings(TCDefaultValues.BorderBiomes.name(), TCDefaultValues.BorderBiomes.StringArrayListValue());

        this.minMoisture = ReadModSettings(TCDefaultValues.minMoisture.name(), TCDefaultValues.minMoisture.floatValue());
        this.maxMoisture = ReadModSettings(TCDefaultValues.maxMoisture.name(), TCDefaultValues.maxMoisture.floatValue());
        this.minTemperature = ReadModSettings(TCDefaultValues.minTemperature.name(), TCDefaultValues.minTemperature.floatValue());
        this.maxTemperature = ReadModSettings(TCDefaultValues.maxTemperature.name(), TCDefaultValues.maxTemperature.floatValue());


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
        this.canyonMaxLength = ReadModSettings(TCDefaultValues.canyonMaxLength.name(), TCDefaultValues.canyonMaxLength.intValue());
        this.canyonDepth = ReadModSettings(TCDefaultValues.canyonDepth.name(), TCDefaultValues.canyonDepth.doubleValue());


        this.waterLevelMax = ReadModSettings(TCDefaultValues.WaterLevelMax.name(), TCDefaultValues.WaterLevelMax.intValue());
        this.waterLevelMin = ReadModSettings(TCDefaultValues.WaterLevelMin.name(), TCDefaultValues.WaterLevelMin.intValue());
        this.waterBlock = ReadModSettings(TCDefaultValues.WaterBlock.name(), TCDefaultValues.WaterBlock.intValue());
        this.iceBlock = ReadModSettings(TCDefaultValues.IceBlock.name(), TCDefaultValues.IceBlock.intValue());
        this.maxAverageHeight = ReadModSettings(TCDefaultValues.MaxAverageHeight.name(), TCDefaultValues.MaxAverageHeight.doubleValue());
        this.maxAverageDepth = ReadModSettings(TCDefaultValues.MaxAverageDepth.name(), TCDefaultValues.MaxAverageDepth.doubleValue());
        this.fractureHorizontal = ReadModSettings(TCDefaultValues.FractureHorizontal.name(), TCDefaultValues.FractureHorizontal.doubleValue());
        this.fractureVertical = ReadModSettings(TCDefaultValues.FractureVertical.name(), TCDefaultValues.FractureVertical.doubleValue());
        this.volatility1 = ReadModSettings(TCDefaultValues.Volatility1.name(), TCDefaultValues.Volatility1.doubleValue());
        this.volatility2 = ReadModSettings(TCDefaultValues.Volatility2.name(), TCDefaultValues.Volatility2.doubleValue());
        this.volatilityWeight1 = ReadModSettings(TCDefaultValues.VolatilityWeight1.name(), TCDefaultValues.VolatilityWeight1.doubleValue());
        this.volatilityWeight2 = ReadModSettings(TCDefaultValues.VolatilityWeight2.name(), TCDefaultValues.VolatilityWeight2.doubleValue());
        this.disableNotchHeightControl = ReadModSettings(TCDefaultValues.DisableBiomeHeight.name(), TCDefaultValues.DisableBiomeHeight.booleanValue());

        this.disableBedrock = ReadModSettings(TCDefaultValues.DisableBedrock.name(), TCDefaultValues.DisableBedrock.booleanValue());
        this.ceilingBedrock = ReadModSettings(TCDefaultValues.CeilingBedrock.name(), TCDefaultValues.CeilingBedrock.booleanValue());
        this.flatBedrock = ReadModSettings(TCDefaultValues.FlatBedrock.name(), TCDefaultValues.FlatBedrock.booleanValue());
        this.bedrockBlock = ReadModSettings(TCDefaultValues.BedrockobBlock.name(), TCDefaultValues.BedrockobBlock.intValue());

        this.removeSurfaceStone = ReadModSettings(TCDefaultValues.RemoveSurfaceStone.name(), TCDefaultValues.RemoveSurfaceStone.booleanValue());

        this.oldTerrainGenerator = this.ModeTerrain == TerrainMode.OldGenerator;

        this.customObjects = this.ReadModSettings(TCDefaultValues.CustomObjects.name(), TCDefaultValues.CustomObjects.booleanValue());
        this.objectSpawnRatio = this.ReadModSettings(TCDefaultValues.objectSpawnRatio.name(), TCDefaultValues.objectSpawnRatio.intValue());
        this.denyObjectsUnderFill = this.ReadModSettings(TCDefaultValues.DenyObjectsUnderFill.name(), TCDefaultValues.DenyObjectsUnderFill.booleanValue());
        this.customTreeChance = this.ReadModSettings(TCDefaultValues.customTreeChance.name(), TCDefaultValues.customTreeChance.intValue());

        this.ReadHeightSettings();
        this.ReadCustomBiomes();


    }


    private void ReadHeightSettings()
    {

        ArrayList<String> keys = this.ReadModSettings(TCDefaultValues.CustomHeightControl.name(), TCDefaultValues.CustomHeightControl.StringArrayListValue());
        try
        {
            if (keys.size() != 17)
                return;
            for (int i = 0; i < 17; i++)
                this.heightMatrix[i] = Double.valueOf(keys.get(i));

        } catch (NumberFormatException e)
        {
            System.out.println("Wrong height settings: '" + this.SettingsCache.get(TCDefaultValues.CustomHeightControl.name()) + "'");
        }


    }


    private void ReadCustomBiomes()
    {

        ArrayList<String> keys = this.ReadModSettings(TCDefaultValues.CustomBiomes.name(), TCDefaultValues.CustomBiomes.StringArrayListValue());

        for (String key : keys)
        {
            boolean isUnique = true;
            for (int i = 0; i < DefaultBiomesCount; i++)
                if (BiomeBase.a[i].w.equals(key))
                    isUnique = false;

            if (isUnique && !this.CustomBiomes.contains(key))
                this.CustomBiomes.add(key);
        }
    }


    protected void WriteConfigSettings() throws IOException
    {
        WriteComment("Possible terrain modes : Normal, OldGenerator, TerrainTest, NotGenerate, Default");
        WriteComment("   Normal - use all features");
        WriteComment("   OldGenerator - generate land like 1.7.3 generator");
        WriteComment("   TerrainTest - generate only terrain without any resources");
        WriteComment("   NotGenerate - generate empty chunks");
        WriteComment("   Default - use default Notch terrain generator");
        WriteValue(TCDefaultValues.ModeTerrain.name(), this.ModeTerrain.name());
        WriteNewLine();
        WriteComment("Possible biome modes : Normal, OldGenerator, Default");
        WriteComment("   Normal - use all features");
        WriteComment("   OldGenerator - generate biome like 1.7.3 generator");
        WriteComment("   Default - use default Notch biome generator");
        WriteValue(TCDefaultValues.ModeBiome.name(), this.ModeBiome.name());

        /* Disabled for 1.9
        WriteValue(TCDefaultValues.snowThreshold.name(), this.snowThreshold);
        WriteValue(TCDefaultValues.iceThreshold.name(), this.iceThreshold);    */

        WriteTitle("Biome Generator Variables");

        /*WriteComment("Integer value from 1 to 15. Affect all biomes except ocean and river");
        WriteValue(TCDefaultValues.BiomeSize.name(), this.biomeSize);
        WriteNewLine();
        WriteComment("Integer value from 0 to 10. This affect how much lands will be generated. LandSize:0 - mean generates only ocean.");
        WriteValue(TCDefaultValues.LandSize.name(), this.landSize);
        WriteNewLine();
        WriteValue(TCDefaultValues.RiversEnabled.name(), this.riversEnabled);  */

        WriteComment("Main value for generation.Bigger value increase zoom. All sizes must be smaller than this.");
        WriteComment("So if you want big biome object you must set size of object near 0 ");
        WriteComment("If you want small object you must set size of object near  GenerationDepth");
        WriteComment("Also small values (about 1-2) and big values (about 20) may affect generator performance");
        WriteValue(TCDefaultValues.GenerationDepth.name(), this.GenerationDepth);
        WriteNewLine();

        WriteComment("Max biome rarity from 1 to infinity. By default this is 100, but you can raise it for");
        WriteComment("fine-grained control, or to create biomes with a chance of occurring smaller than 1/100.");
        WriteValue(TCDefaultValues.BiomeRarityScale.name(), this.BiomeRarityScale);
        WriteNewLine();
        WriteComment("Land rarity from 100 to 1. If you set smaller than 90 and LandSize near 0 beware Big oceans.");
        WriteValue(TCDefaultValues.LandRarity.name(), this.LandRarity);
        WriteNewLine();
        WriteComment("Land size from 0 to GenerationDepth.");
        WriteValue(TCDefaultValues.LandSize.name(), this.LandSize);
        WriteNewLine();
        WriteComment("Make land more fuzzy and make lakes. Must be from 0 to GenerationDepth - LandSize");
        WriteValue(TCDefaultValues.LandFuzzy.name(), this.LandFuzzy);
        WriteNewLine();

        WriteComment("Ice areas rarity from 100 to 1. If you set smaller than 90 and IceSize near 0 beware ice world");
        WriteValue(TCDefaultValues.IceRarity.name(), this.IceRarity);
        WriteNewLine();
        WriteComment("Ice area size from 0 to GenerationDepth.");
        WriteValue(TCDefaultValues.IceSize.name(), this.IceSize);
        WriteNewLine();

        WriteValue(TCDefaultValues.FrozenRivers.name(), this.FrozenRivers);
        WriteNewLine();
        WriteValue(TCDefaultValues.FrozenOcean.name(), this.FrozenOcean);
        WriteNewLine();

        WriteComment("River rarity.Must be from 0 to GenerationDepth.");
        WriteValue(TCDefaultValues.RiverRarity.name(), this.RiverRarity);
        WriteNewLine();
        WriteComment("River size from 0 to GenerationDepth - RiverRarity");
        WriteValue(TCDefaultValues.RiverSize.name(), this.RiverSize);
        WriteNewLine();
        WriteValue(TCDefaultValues.RiversEnabled.name(), this.RiversEnabled);
        WriteNewLine();

        WriteComment("Biomes which used in normal biome algorithm. Biome name is case sensitive.");
        WriteValue(TCDefaultValues.NormalBiomes.name(), this.NormalBiomes);
        WriteNewLine();
        WriteComment("Biomes which used in ice biome algorithm. Biome name is case sensitive.");
        WriteValue(TCDefaultValues.IceBiomes.name(), this.IceBiomes);
        WriteNewLine();
        WriteComment("Biomes which used as isles. You must set IsleInBiome in biome config for each biome here. Biome name is case sensitive.");
        WriteValue(TCDefaultValues.IsleBiomes.name(), this.IsleBiomes);
        WriteNewLine();
        WriteComment("Biomes which used as borders. You must set BiomeIsBorder in biome config for each biome here. Biome name is case sensitive.");
        WriteValue(TCDefaultValues.BorderBiomes.name(), this.BorderBiomes);

        WriteNewLine();
        WriteComment("List of ALL custom biomes.");
        WriteComment("Example: ");
        WriteComment("  CustomBiomes:TestBiome1, BiomeTest2");
        WriteComment("This will add two biomes and generate biome config files");
        WriteComment("Any changes here need server restart.");
        WriteValue(TCDefaultValues.CustomBiomes.name(), this.CustomBiomes);


        /* Removed .. not sure this need someone
        WriteTitle("Swamp Biome Variables");
        WriteValue(TCDefaultValues.muddySwamps.name(), this.muddySwamps);
        WriteValue(TCDefaultValues.claySwamps.name(), this.claySwamps);
        WriteValue(TCDefaultValues.swampSize.name(), this.swampSize);

        WriteTitle("Desert Biome Variables");
        WriteValue(TCDefaultValues.waterlessDeserts.name(), this.waterlessDeserts);
        WriteValue(TCDefaultValues.desertDirt.name(), this.desertDirt);
        WriteValue(TCDefaultValues.desertDirtFrequency.name(), this.desertDirtFrequency);
        */


        WriteTitle("Terrain Generator Variables");
        WriteComment("Set water level. Every empty block under this level will be fill water or another block from WaterBlock ");
        WriteValue(TCDefaultValues.WaterLevelMax.name(), this.waterLevelMax);
        WriteValue(TCDefaultValues.WaterLevelMin.name(), this.waterLevelMin);
        WriteNewLine();
        WriteComment("BlockId used as water in WaterLevel");
        WriteValue(TCDefaultValues.WaterBlock.name(), this.waterBlock);
        WriteNewLine();
        WriteComment("BlockId used as ice");
        WriteValue(TCDefaultValues.IceBlock.name(), this.iceBlock);
        WriteNewLine();
        WriteComment("If this value is greater than 0, then it will affect how much, on average, the terrain will rise before leveling off when it begins to increase in elevation.");
        WriteComment("If the value is less than 0, then it will cause the terrain to either increase to a lower height before leveling out or decrease in height if the value is a large enough negative.");
        WriteValue(TCDefaultValues.MaxAverageHeight.name(), this.maxAverageHeight);

        WriteNewLine();
        WriteComment("If this value is greater than 0, then it will affect how much, on average, the terrain (usually at the ottom of the ocean) will fall before leveling off when it begins to decrease in elevation. ");
        WriteComment("If the value is less than 0, then it will cause the terrain to either fall to a lesser depth before leveling out or increase in height if the value is a large enough negative.");
        WriteValue(TCDefaultValues.MaxAverageDepth.name(), this.maxAverageDepth);

        WriteNewLine();
        WriteComment("Can increase (values greater than 0) or decrease (values less than 0) how much the landscape is fractured horizontally.");
        WriteValue(TCDefaultValues.FractureHorizontal.name(), this.fractureHorizontal);

        WriteNewLine();
        WriteComment("Can increase (values greater than 0) or decrease (values less than 0) how much the landscape is fractured vertically.");
        WriteComment("Positive values will lead to large cliffs/overhangs, floating islands, and/or a cavern world depending on other settings.");
        WriteValue(TCDefaultValues.FractureVertical.name(), this.fractureVertical);

        WriteNewLine();
        WriteComment("Another type of noise. This noise is independent from biomes. The larger the values the more chaotic/volatile landscape generation becomes.");
        WriteComment("Setting the values to negative will have the opposite effect and make landscape generation calmer/gentler.");
        WriteValue(TCDefaultValues.Volatility1.name(), this.volatility1);
        WriteValue(TCDefaultValues.Volatility2.name(), this.volatility2);

        WriteNewLine();
        WriteComment("Adjust the weight of the corresponding volatility settings. This allows you to change how prevalent you want either of the volatility settings to be in the terrain.");
        WriteValue(TCDefaultValues.VolatilityWeight1.name(), this.volatilityWeight1);
        WriteValue(TCDefaultValues.VolatilityWeight2.name(), this.volatilityWeight2);

        WriteNewLine();
        WriteComment("Disable all noises except Volatility1 and Volatility2. Also disable default block chance from height.");
        WriteValue(TCDefaultValues.DisableBiomeHeight.name(), this.disableNotchHeightControl);
        WriteNewLine();
        WriteComment("List of custom height factor, 17 double entries, each entire control of about 7 blocks height from down. Positive entry - better chance of spawn blocks, negative - smaller");
        WriteComment("Values which affect your configuration may be found only experimental. That may be very big, like ~3000.0 depends from height");
        WriteComment("Example:");
        WriteComment("  CustomHeightControl:0.0,-2500.0,0.0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0");
        WriteComment("Make empty layer above bedrock layer. ");
        WriteHeightSettings();

        WriteNewLine();
        WriteComment("Attempts to replace all surface stone with biome surface block");
        WriteValue(TCDefaultValues.RemoveSurfaceStone.name(), this.removeSurfaceStone);

        WriteNewLine();
        WriteComment("Disable bottom of map bedrock generation");
        WriteValue(TCDefaultValues.DisableBedrock.name(), this.disableBedrock);

        WriteNewLine();
        WriteComment("Enable ceiling of map bedrock generation");
        WriteValue(TCDefaultValues.CeilingBedrock.name(), this.ceilingBedrock);

        WriteNewLine();
        WriteComment("Make bottom layer of bedrock flat");
        WriteValue(TCDefaultValues.FlatBedrock.name(), this.flatBedrock);

        WriteNewLine();
        WriteComment("BlockId used as bedrock");
        WriteValue(TCDefaultValues.BedrockobBlock.name(), this.bedrockBlock);

        WriteTitle("Map objects");
        WriteValue(TCDefaultValues.StrongholdsEnabled.name(), this.StrongholdsEnabled);
        WriteValue(TCDefaultValues.VillagesEnabled.name(), this.VillagesEnabled);
        WriteValue(TCDefaultValues.MineshaftsEnabled.name(), this.MineshaftsEnabled);


        this.WriteTitle("BOB Objects Variables");

        WriteNewLine();
        WriteComment("Enable/disable custom objects");
        this.WriteValue(TCDefaultValues.CustomObjects.name(), this.customObjects);

        WriteNewLine();
        WriteComment("Number of attempts for place rep chunk");
        this.WriteValue(TCDefaultValues.objectSpawnRatio.name(), Integer.valueOf(this.objectSpawnRatio).intValue());

        WriteNewLine();
        WriteComment("Deny custom objects underFill even it enabled in objects ");
        this.WriteValue(TCDefaultValues.DenyObjectsUnderFill.name(), this.denyObjectsUnderFill);
        WriteNewLine();
        WriteComment("Chance to grow custom instead normal tree from sapling .");
        this.WriteValue(TCDefaultValues.customTreeChance.name(), this.customTreeChance);


        WriteTitle("Cave Variables");
        WriteValue(TCDefaultValues.caveRarity.name(), this.caveRarity);
        WriteValue(TCDefaultValues.caveFrequency.name(), this.caveFrequency);
        WriteValue(TCDefaultValues.caveMinAltitude.name(), this.caveMinAltitude);
        WriteValue(TCDefaultValues.caveMaxAltitude.name(), this.caveMaxAltitude);
        WriteValue(TCDefaultValues.individualCaveRarity.name(), this.individualCaveRarity);
        WriteValue(TCDefaultValues.caveSystemFrequency.name(), this.caveSystemFrequency);
        WriteValue(TCDefaultValues.caveSystemPocketChance.name(), this.caveSystemPocketChance);
        WriteValue(TCDefaultValues.caveSystemPocketMinSize.name(), this.caveSystemPocketMinSize);
        WriteValue(TCDefaultValues.caveSystemPocketMaxSize.name(), this.caveSystemPocketMaxSize);
        WriteValue(TCDefaultValues.evenCaveDistribution.name(), this.evenCaveDistribution);


        WriteTitle("Canyon Variables");
        WriteValue(TCDefaultValues.canyonRarity.name(), this.canyonRarity);
        WriteValue(TCDefaultValues.canyonMinAltitude.name(), this.canyonMinAltitude);
        WriteValue(TCDefaultValues.canyonMaxAltitude.name(), this.canyonMaxAltitude);
        WriteValue(TCDefaultValues.canyonMinLength.name(), this.canyonMinLength);
        WriteValue(TCDefaultValues.canyonMaxLength.name(), this.canyonMaxLength);
        WriteValue(TCDefaultValues.canyonDepth.name(), this.canyonDepth);

        WriteNewLine();
        WriteTitle("Old Biome Generator Variables");
        WriteComment("This generator works only with old terrain generator!");
        //WriteComment("Since 1.8.3 notch take temperature from biomes, so changing this you can`t affect new biome generation ");
        WriteValue(TCDefaultValues.oldBiomeSize.name(), this.oldBiomeSize);
        WriteValue(TCDefaultValues.minMoisture.name(), this.minMoisture);
        WriteValue(TCDefaultValues.maxMoisture.name(), this.maxMoisture);
        WriteValue(TCDefaultValues.minTemperature.name(), this.minTemperature);
        WriteValue(TCDefaultValues.maxTemperature.name(), this.maxTemperature);

    }


    private void WriteHeightSettings() throws IOException
    {

        String output = Double.toString(this.heightMatrix[0]);
        for (int i = 1; i < this.heightMatrix.length; i++)
            output = output + "," + Double.toString(this.heightMatrix[i]);

        this.WriteValue(TCDefaultValues.CustomHeightControl.name(), output);
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

    public LocalBiome GetBiomeByName(String name)
    {
        for (int i = 0; i < WorldConfig.DefaultBiomesCount + WorldConfig.ExtendedBiomesCount; i++)
            if (BiomeBase.a[i].w.equals(name))
                return new BukkitBiome(BiomeBase.a[i]);
        return null;
    }


    public enum TerrainMode
    {
        Normal,
        OldGenerator,
        TerrainTest,
        NotGenerate,
        Default
    }

    public enum BiomeMode
    {
        Normal,
        OldGenerator,
        Default
    }

}
