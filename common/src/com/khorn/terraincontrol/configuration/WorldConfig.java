package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class WorldConfig extends ConfigFile
{
    public ArrayList<String> CustomBiomes = new ArrayList<String>();
    public HashMap<String, Integer> CustomBiomeIds = new HashMap<String, Integer>();

    public ArrayList<String> NormalBiomes = new ArrayList<String>();
    public ArrayList<String> IceBiomes = new ArrayList<String>();
    public ArrayList<String> IsleBiomes = new ArrayList<String>();
    public ArrayList<String> BorderBiomes = new ArrayList<String>();

    public ArrayList<BiomeConfig> biomes = new ArrayList<BiomeConfig>();

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

    // Biome image

    public String imageFile;
    public ImageMode imageMode;
    //public int imageZoom;
    public String imageFillBiome;
    public int imageXOffset;
    public int imageZOffset;

    public HashMap<Integer,Integer> biomeColorMap;

    // Look settings
    public int WorldFog;
    public float WorldFogR;
    public float WorldFogG;
    public float WorldFogB;

    public int WorldNightFog;
    public float WorldNightFogR;
    public float WorldNightFogG;
    public float WorldNightFogB;


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

    private double fractureHorizontal;
    private double fractureVertical;

    private boolean disableBedrock;
    private boolean flatBedrock;
    public boolean ceilingBedrock;
    public int bedrockBlock;


    public boolean removeSurfaceStone;

    public boolean customObjects;
    public int objectSpawnRatio;
    public boolean denyObjectsUnderFill;
    public int customTreeChance;

    public boolean StrongholdsEnabled;
    public boolean MineshaftsEnabled;
    public boolean VillagesEnabled;

    public File SettingsDir;

    public boolean isDeprecated = false;
    public WorldConfig newSettings = null;

    public String WorldName;
    public TerrainMode ModeTerrain;
    public BiomeMode ModeBiome;

    public BiomeConfig[] biomeConfigs;
    public boolean BiomeConfigsHaveReplacement = false;

    public int normalBiomesRarity;
    public int iceBiomesRarity;

    public int worldHeightBits;
    public int WorldHeight;

    public WorldConfig(File settingsDir, LocalWorld world, boolean checkOnly)
    {
        this.SettingsDir = settingsDir;
        this.WorldName = world.getName();

        File settingsFile = new File(this.SettingsDir, TCDefaultValues.WorldSettingsName.stringValue());

        this.ReadSettingsFile(settingsFile);
        this.RenameOldSettings();
        this.ReadConfigSettings();

        this.CorrectSettings();

        // Check biome ids

        for (String biomeName : CustomBiomes)
            if (CustomBiomeIds.get(biomeName) == -1)
                CustomBiomeIds.put(biomeName, world.getFreeBiomeId());

        // Need add check to clashes

        this.WriteSettingsFile(settingsFile);
        world.setHeightBits(this.worldHeightBits);


        File BiomeFolder = new File(SettingsDir, TCDefaultValues.WorldBiomeConfigDirectoryName.stringValue());
        if (!BiomeFolder.exists())
        {
            if (!BiomeFolder.mkdir())
            {
                System.out.println("TerrainControl: error create biome configs directory, working with defaults");
                return;
            }
        }

        ArrayList<LocalBiome> localBiomes = new ArrayList<LocalBiome>(world.getDefaultBiomes());

        for (String biomeName : this.CustomBiomes)
        {
            if (checkOnly)
                localBiomes.add(world.getNullBiome(biomeName));
            else
                localBiomes.add(world.AddBiome(biomeName, this.CustomBiomeIds.get(biomeName)));
        }

        this.biomeConfigs = new BiomeConfig[world.getMaxBiomesCount()];

        for (LocalBiome localBiome : localBiomes)
        {
            System.out.println("TerrainControl: Loading biome settings for " + localBiome.getName());
            BiomeConfig config = new BiomeConfig(BiomeFolder, localBiome, this);
            if (checkOnly)
                continue;

            if (this.NormalBiomes.contains(config.Name))
                this.normalBiomesRarity += config.BiomeRarity;
            if (this.IceBiomes.contains(config.Name))
                this.iceBiomesRarity += config.BiomeRarity;

            this.biomeConfigs[localBiome.getId()] = config;
            if (!this.BiomeConfigsHaveReplacement)
                this.BiomeConfigsHaveReplacement = config.ReplaceCount > 0;
            this.biomes.add(config);

            if(this.ModeBiome == BiomeMode.FromImage)
            {
                if( this.biomeColorMap == null)
                    this.biomeColorMap = new HashMap<Integer, Integer>();

                try
                {
                    int color = Integer.decode(config.BiomeColor);
                    if (color <= 0xFFFFFF)
                        this.biomeColorMap.put(color,config.Biome.getId());
                } catch (NumberFormatException ex)
                {
                    System.out.println("TerrainControl: wrong color in " + config.Biome.getName());
                }


            }
        }

        this.RegisterBOBPlugins(world);
    }

    protected void RenameOldSettings()
    {
        if (this.SettingsCache.containsKey("WaterLevel"))
        {
            this.SettingsCache.put("WaterLevelMax".toLowerCase(), this.SettingsCache.get("WaterLevel"));
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

        if ( this.ModeBiome == BiomeMode.FromImage)
        {
            File mapFile = new File(SettingsDir, imageFile);
            if(!mapFile.exists())
            {
                System.out.println("TerrainControl: Biome map file not found. Switching ModeBiome to Normal");
                this.ModeBiome = BiomeMode.Normal;
            }
        }

        this.imageFillBiome = (DefaultBiome.Contain(imageFillBiome)|| CustomBiomes.contains(imageFillBiome)) ? imageFillBiome : TCDefaultValues.ImageFillBiome.stringValue();



        this.minMoisture = CheckValue(this.minMoisture, 0, 1.0F);
        this.maxMoisture = CheckValue(this.maxMoisture, 0, 1.0F, this.minMoisture);

        this.minTemperature = CheckValue(this.minTemperature, 0, 1.0F);
        this.maxTemperature = CheckValue(this.maxTemperature, 0, 1.0F, this.minTemperature);


        this.caveRarity = CheckValue(this.caveRarity, 0, 100);
        this.caveFrequency = CheckValue(this.caveFrequency, 0, 200);
        this.caveMinAltitude = CheckValue(this.caveMinAltitude, 0, WorldHeight);
        this.caveMaxAltitude = CheckValue(this.caveMaxAltitude, 0, WorldHeight, this.caveMinAltitude);
        this.individualCaveRarity = CheckValue(this.individualCaveRarity, 0, 100);
        this.caveSystemFrequency = CheckValue(this.caveSystemFrequency, 0, 200);
        this.caveSystemPocketChance = CheckValue(this.caveSystemPocketChance, 0, 100);
        this.caveSystemPocketMinSize = CheckValue(this.caveSystemPocketMinSize, 0, 100);
        this.caveSystemPocketMaxSize = CheckValue(this.caveSystemPocketMaxSize, 0, 100, this.caveSystemPocketMinSize);


        this.canyonRarity = CheckValue(this.canyonRarity, 0, 100);
        this.canyonMinAltitude = CheckValue(this.canyonMinAltitude, 0, WorldHeight);
        this.canyonMaxAltitude = CheckValue(this.canyonMaxAltitude, 0, WorldHeight, this.canyonMinAltitude);
        this.canyonMinLength = CheckValue(this.canyonMinLength, 1, 500);
        this.canyonMaxLength = CheckValue(this.canyonMaxLength, 1, 500, this.canyonMinLength);
        this.canyonDepth = CheckValue(this.canyonDepth, 0.1D, 15D);


        this.waterLevelMin = CheckValue(this.waterLevelMin, 0, WorldHeight - 1);
        this.waterLevelMax = CheckValue(this.waterLevelMax, 0, WorldHeight - 1, this.waterLevelMin);

        this.customTreeChance = CheckValue(this.customTreeChance, 0, 100);

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

        this.worldHeightBits = ReadModSettings(TCDefaultValues.WorldHeightBits.name(), TCDefaultValues.WorldHeightBits.intValue());
        this.worldHeightBits = CheckValue(this.worldHeightBits, 5, 8);

        this.WorldHeight = 1 << worldHeightBits;
        this.waterLevelMax = WorldHeight / 2;


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
        ReadCustomBiomes();

        try
        {
            this.imageMode = ImageMode.valueOf(ReadModSettings(TCDefaultValues.ImageMode.name(), TCDefaultValues.ImageMode.stringValue()));
        } catch (IllegalArgumentException e)
        {
            this.imageMode = ImageMode.Repeat;
        }

        this.imageFile = this.ReadModSettings(TCDefaultValues.ImageFile.name(), TCDefaultValues.ImageFile.stringValue());
        this.imageFillBiome = this.ReadModSettings(TCDefaultValues.ImageFillBiome.name(), TCDefaultValues.ImageFillBiome.stringValue());
        this.imageXOffset =  this.ReadModSettings(TCDefaultValues.ImageXOffset.name(), TCDefaultValues.ImageXOffset.intValue());
        this.imageZOffset =  this.ReadModSettings(TCDefaultValues.ImageZOffset.name(), TCDefaultValues.ImageZOffset.intValue());

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

        this.WorldFog = ReadModSettingsColor(TCDefaultValues.WorldFog.name(), TCDefaultValues.WorldFog.stringValue());
        this.WorldNightFog = ReadModSettingsColor(TCDefaultValues.WorldNightFog.name(), TCDefaultValues.WorldNightFog.stringValue());

        this.WorldFogR = ((WorldFog & 0xFF0000) >> 16) / 255F;
        this.WorldFogG = ((WorldFog & 0xFF00) >> 8) / 255F;
        this.WorldFogB = (WorldFog & 0xFF) / 255F;

        this.WorldNightFogR = ((WorldNightFog & 0xFF0000) >> 16) / 255F;
        this.WorldNightFogG = ((WorldNightFog & 0xFF00) >> 8) / 255F;
        this.WorldNightFogB = (WorldNightFog & 0xFF) / 255F;

        this.StrongholdsEnabled = ReadModSettings(TCDefaultValues.StrongholdsEnabled.name(), TCDefaultValues.StrongholdsEnabled.booleanValue());
        this.VillagesEnabled = ReadModSettings(TCDefaultValues.VillagesEnabled.name(), TCDefaultValues.VillagesEnabled.booleanValue());
        this.MineshaftsEnabled = ReadModSettings(TCDefaultValues.MineshaftsEnabled.name(), TCDefaultValues.MineshaftsEnabled.booleanValue());

        this.caveRarity = ReadModSettings(TCDefaultValues.caveRarity.name(), TCDefaultValues.caveRarity.intValue());
        this.caveFrequency = ReadModSettings(TCDefaultValues.caveFrequency.name(), TCDefaultValues.caveFrequency.intValue());
        this.caveMinAltitude = ReadModSettings(TCDefaultValues.caveMinAltitude.name(), TCDefaultValues.caveMinAltitude.intValue());
        this.caveMaxAltitude = ReadModSettings(TCDefaultValues.caveMaxAltitude.name(), this.WorldHeight);
        this.individualCaveRarity = ReadModSettings(TCDefaultValues.individualCaveRarity.name(), TCDefaultValues.individualCaveRarity.intValue());
        this.caveSystemFrequency = ReadModSettings(TCDefaultValues.caveSystemFrequency.name(), TCDefaultValues.caveSystemFrequency.intValue());
        this.caveSystemPocketChance = ReadModSettings(TCDefaultValues.caveSystemPocketChance.name(), TCDefaultValues.caveSystemPocketChance.intValue());
        this.caveSystemPocketMinSize = ReadModSettings(TCDefaultValues.caveSystemPocketMinSize.name(), TCDefaultValues.caveSystemPocketMinSize.intValue());
        this.caveSystemPocketMaxSize = ReadModSettings(TCDefaultValues.caveSystemPocketMaxSize.name(), TCDefaultValues.caveSystemPocketMaxSize.intValue());
        this.evenCaveDistribution = ReadModSettings(TCDefaultValues.evenCaveDistribution.name(), TCDefaultValues.evenCaveDistribution.booleanValue());

        this.canyonRarity = ReadModSettings(TCDefaultValues.canyonRarity.name(), TCDefaultValues.canyonRarity.intValue());
        this.canyonMinAltitude = ReadModSettings(TCDefaultValues.canyonMinAltitude.name(), TCDefaultValues.canyonMinAltitude.intValue());
        this.canyonMaxAltitude = ReadModSettings(TCDefaultValues.canyonMaxAltitude.name(), this.WorldHeight / 2);
        this.canyonMinLength = ReadModSettings(TCDefaultValues.canyonMinLength.name(), TCDefaultValues.canyonMinLength.intValue());
        this.canyonMaxLength = ReadModSettings(TCDefaultValues.canyonMaxLength.name(), TCDefaultValues.canyonMaxLength.intValue());
        this.canyonDepth = ReadModSettings(TCDefaultValues.canyonDepth.name(), TCDefaultValues.canyonDepth.doubleValue());

        this.waterLevelMax = ReadModSettings(TCDefaultValues.WaterLevelMax.name(), this.waterLevelMax);
        this.waterLevelMin = ReadModSettings(TCDefaultValues.WaterLevelMin.name(), TCDefaultValues.WaterLevelMin.intValue());
        this.waterBlock = ReadModSettings(TCDefaultValues.WaterBlock.name(), TCDefaultValues.WaterBlock.intValue());
        this.iceBlock = ReadModSettings(TCDefaultValues.IceBlock.name(), TCDefaultValues.IceBlock.intValue());
        this.fractureHorizontal = ReadModSettings(TCDefaultValues.FractureHorizontal.name(), TCDefaultValues.FractureHorizontal.doubleValue());
        this.fractureVertical = ReadModSettings(TCDefaultValues.FractureVertical.name(), TCDefaultValues.FractureVertical.doubleValue());

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
    }

    private void ReadCustomBiomes()
    {

        ArrayList<String> biomes = this.ReadModSettings(TCDefaultValues.CustomBiomes.name(), TCDefaultValues.CustomBiomes.StringArrayListValue());

        for (String biome : biomes)
        {
            try
            {
                String[] keys = biome.split(":");
                int id = -1;
                if (keys.length == 2)
                    id = Integer.valueOf(keys[1]);
                CustomBiomes.add(keys[0]);
                CustomBiomeIds.put(keys[0], id);

            } catch (NumberFormatException e)
            {
                System.out.println("Wrong custom biome id settings: '" + biome + "'");
            }

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
        WriteComment("   FromImage - get biomes from image file");
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

        WriteComment("IMPORTANT value for generation. Bigger values appear to zoom out. All 'Sizes' must be smaller than this.");
        WriteComment("Large %/total area biomes (Continents) must be set small, (limit=0)");
        WriteComment("Small %/total area biomes (Oasis,Mountain Peaks) must be larger (limit=GenerationDepth)");
        WriteComment("This could also represent \"Total number of biome sizes\" ");
        WriteComment("Small values (about 1-2) and Large values (about 20) may affect generator performance.");
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

        WriteNewLine();     // TODO Write correct help
        WriteComment("List of ALL custom biomes.");
        WriteComment("Example: ");
        WriteComment("  CustomBiomes:TestBiome1, BiomeTest2");
        WriteComment("This will add two biomes and generate biome config files");
        WriteComment("Any changes here need server restart.");
        /*
               WriteComment("The id of the biome must be unique for all biomes on the server.");
        WriteComment("The available id's range from "+idMin+" to "+idMax+" and the first 0 to "+(DefaultBiome.values().length-1)+" is occupied by vanilla minecraft biomes.");
        WriteComment("To leave room for future vanilla biomes we suggest your custom biomes start at id "+idSuggestedCustomMin+".");
        WriteComment("The id for the biome will be saved to disc together with the chunk data (new feature since the Anvil map format).");
        WriteComment("This means that if you change the biome id after you generated your map, the ids in the map wont change.");
        WriteComment("Orphaned ids are interpreted as id 1 = Plains. Example things that depend on the biome id is mob spawning and growth from saplings.");

         */
        WriteCustomBiomes();

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

        WriteTitle("Biome Image Generator Variables");

        WriteNewLine();
        WriteComment("Possible modes when generator outside image boundaries: Repeat, ContinueNormal, FillEmpty");
        WriteComment("   Repeat - repeat image");
        WriteComment("   ContinueNormal - continue normal generation");
        WriteComment("   FillEmpty - fill by biome in \"ImageFillBiome settings\" ");
        WriteValue(TCDefaultValues.ImageMode.name(), this.imageMode.name());

        WriteNewLine();
        WriteComment("Source png file for FromImage biome mode.");
        WriteValue(TCDefaultValues.ImageFile.name(), this.imageFile);

        WriteNewLine();
        WriteComment("Biome name for fill outside image boundaries with FillEmpty mode.");
        WriteValue(TCDefaultValues.ImageFillBiome.name(), this.imageFillBiome);

        WriteNewLine();
        WriteComment("Shifts map position from x=0 and z=0 coordinates.");
        WriteValue(TCDefaultValues.ImageXOffset.name(), this.imageXOffset);
        WriteValue(TCDefaultValues.ImageZOffset.name(), this.imageZOffset);


        WriteTitle("Terrain Generator Variables");
        WriteComment("Height bits determinate generation height. Min 5, max 8");
        WriteComment("For example 7 = 128 height, 8 = 256 height");
        WriteValue(TCDefaultValues.WorldHeightBits.name(), this.worldHeightBits);
        WriteNewLine();

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
        WriteComment("Can increase (values greater than 0) or decrease (values less than 0) how much the landscape is fractured horizontally.");
        WriteValue(TCDefaultValues.FractureHorizontal.name(), this.fractureHorizontal);

        WriteNewLine();
        WriteComment("Can increase (values greater than 0) or decrease (values less than 0) how much the landscape is fractured vertically.");
        WriteComment("Positive values will lead to large cliffs/overhangs, floating islands, and/or a cavern world depending on other settings.");
        WriteValue(TCDefaultValues.FractureVertical.name(), this.fractureVertical);

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

        this.WriteTitle("World visual settings");
        this.WriteComment("Warning this section will work only for clients with single version of TerrainControl");

        WriteComment("World fog color");
        WriteColorValue(TCDefaultValues.WorldFog.name(), this.WorldFog);
        this.WriteNewLine();

        WriteComment("World night fog color");
        WriteColorValue(TCDefaultValues.WorldNightFog.name(), this.WorldNightFog);
        this.WriteNewLine();

        this.WriteTitle("BOB Objects Variables");

        WriteNewLine();
        WriteComment("Enable/disable custom objects");
        this.WriteValue(TCDefaultValues.CustomObjects.name(), this.customObjects);

        WriteNewLine();
        WriteComment("Number of attempts for place per chunk");
        this.WriteValue(TCDefaultValues.objectSpawnRatio.name(), Integer.valueOf(this.objectSpawnRatio).intValue());

        WriteNewLine();
        WriteComment("Deny custom objects underFill even it enabled in objects ");
        this.WriteValue(TCDefaultValues.DenyObjectsUnderFill.name(), this.denyObjectsUnderFill);
        WriteNewLine();
        WriteComment("Chance to grow custom instead normal tree from sapling .");
        this.WriteValue(TCDefaultValues.customTreeChance.name(), this.customTreeChance);


        WriteTitle("Cave Variables");

        WriteComment("TerrainControl attempts once per chunk to create a cave or cave system.");
        WriteComment("This is chance of success on that attempt.");
        WriteValue(TCDefaultValues.caveRarity.name(), this.caveRarity);
        WriteNewLine();
        WriteComment("If successful, It tries to add this many caves in that chunk but trends towards lower results.");
        WriteComment("Input of 40 tends to result in 5-6 caves or cave systems starting per chunk.");
        WriteValue(TCDefaultValues.caveFrequency.name(), this.caveFrequency);
        WriteNewLine();
        WriteComment("Trends towards lower elevations.");
        WriteValue(TCDefaultValues.caveMinAltitude.name(), this.caveMinAltitude);
        WriteValue(TCDefaultValues.caveMaxAltitude.name(), this.caveMaxAltitude);
        WriteNewLine();
        WriteComment("Chance that any cave made during \" caveFrequency\" will generate without a connecting cave or system.");
        WriteComment("Will also attempt to create a pocket - a higher than normal density of cave systems nearby, however no guarantee of connecting to it.");
        WriteValue(TCDefaultValues.individualCaveRarity.name(), this.individualCaveRarity);
        WriteNewLine();
        WriteComment("Number of attempts during \" caveFreqency\" to start a system instead of continuing a single cave.");
        WriteComment("Warning:High values cause extremely slow world generation and lag.");
        WriteValue(TCDefaultValues.caveSystemFrequency.name(), this.caveSystemFrequency);
        WriteNewLine();
        WriteComment("Adds additional attempts for cave pocket after \"individualCaveRarity\" attempts.");
        WriteValue(TCDefaultValues.caveSystemPocketChance.name(), this.caveSystemPocketChance);
        WriteNewLine();
        WriteComment("When triggered, Overrides \"caveFrequency\"");
        WriteValue(TCDefaultValues.caveSystemPocketMinSize.name(), this.caveSystemPocketMinSize);
        WriteValue(TCDefaultValues.caveSystemPocketMaxSize.name(), this.caveSystemPocketMaxSize);
        WriteNewLine();
        WriteComment("Turns off Randomizer = CAVES EVERYWHERE!");
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

    private void WriteCustomBiomes() throws IOException
    {
        String output = "";
        boolean first = true;
        for (String biome : CustomBiomes)
        {
            if (!first)
                output += ",";
            first = false;
            output += biome + ":" + CustomBiomeIds.get(biome);
        }
        WriteValue(TCDefaultValues.CustomBiomes.name(), output);

    }

    private void RegisterBOBPlugins(LocalWorld world)
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
                        CustomObject WorkingCustomObject = new CustomObject(BOBFile, world);
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


    public boolean createAdminium(int y)
    {
        return (!this.disableBedrock) && ((!this.flatBedrock) || (y == 0));
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
        FromImage,
        OldGenerator,
        Default
    }

    public enum ImageMode
    {
        Repeat,
        ContinueNormal,
        FillEmpty,
    }

    public void Serialize(DataOutputStream stream) throws IOException
    {
        stream.writeInt(TCDefaultValues.ProtocolVersion.intValue());

        WriteStringToStream(stream, this.WorldName);

        stream.writeInt(this.GenerationDepth);
        stream.writeInt(this.BiomeRarityScale);
        stream.writeInt(this.LandRarity);
        stream.writeInt(this.LandSize);
        stream.writeInt(this.LandFuzzy);
        stream.writeInt(this.IceRarity);
        stream.writeInt(this.IceSize);
        stream.writeBoolean(this.FrozenOcean);
        stream.writeBoolean(this.FrozenRivers);
        stream.writeInt(this.RiverRarity);
        stream.writeInt(this.RiverSize);
        stream.writeBoolean(this.RiversEnabled);

        stream.writeDouble(this.oldBiomeSize);

        stream.writeFloat(this.minTemperature);
        stream.writeFloat(this.maxTemperature);
        stream.writeFloat(this.minMoisture);
        stream.writeFloat(this.maxMoisture);

        stream.writeInt(this.WorldFog);
        stream.writeInt(this.WorldNightFog);


        stream.writeInt(this.CustomBiomes.size());
        for (String name : this.CustomBiomes)
        {
            WriteStringToStream(stream, name);
            stream.writeInt(this.CustomBiomeIds.get(name));
        }

        BiomeConfig config = this.getConfigByName(DefaultBiome.OCEAN.Name);
        WriteStringToStream(stream, config.Name);
        config.Serialize(stream);

        config = this.getConfigByName(DefaultBiome.RIVER.Name);
        WriteStringToStream(stream, config.Name);
        config.Serialize(stream);

        config = this.getConfigByName(DefaultBiome.FROZEN_OCEAN.Name);
        WriteStringToStream(stream, config.Name);
        config.Serialize(stream);

        config = this.getConfigByName(DefaultBiome.FROZEN_RIVER.Name);
        WriteStringToStream(stream, config.Name);
        config.Serialize(stream);


        stream.writeInt(this.NormalBiomes.size());
        for (String biome : this.NormalBiomes)
        {
            config = this.getConfigByName(biome);
            WriteStringToStream(stream, config.Name);
            config.Serialize(stream);
        }

        stream.writeInt(this.IceBiomes.size());
        for (String biome : this.IceBiomes)
        {
            config = this.getConfigByName(biome);
            WriteStringToStream(stream, config.Name);
            config.Serialize(stream);
        }

        stream.writeInt(this.IsleBiomes.size());
        for (String biome : this.IsleBiomes)
        {
            config = this.getConfigByName(biome);
            WriteStringToStream(stream, config.Name);
            config.Serialize(stream);
        }

        stream.writeInt(this.BorderBiomes.size());
        for (String biome : this.BorderBiomes)
        {
            config = this.getConfigByName(biome);
            WriteStringToStream(stream, config.Name);
            config.Serialize(stream);
        }
    }


    // Need for create world config from network packet
    public WorldConfig(DataInputStream stream, LocalWorld world) throws IOException
    {
        // Protocol version
        stream.readInt();

        this.WorldName = ReadStringFromStream(stream);

        this.GenerationDepth = stream.readInt();
        this.BiomeRarityScale = stream.readInt();
        this.LandRarity = stream.readInt();
        this.LandSize = stream.readInt();
        this.LandFuzzy = stream.readInt();
        this.IceRarity = stream.readInt();
        this.IceSize = stream.readInt();
        this.FrozenOcean = stream.readBoolean();
        this.FrozenRivers = stream.readBoolean();
        this.RiverRarity = stream.readInt();
        this.RiverSize = stream.readInt();
        this.RiversEnabled = stream.readBoolean();

        this.oldBiomeSize = stream.readDouble();

        this.minTemperature = stream.readFloat();
        this.maxTemperature = stream.readFloat();

        this.minMoisture = stream.readFloat();
        this.maxMoisture = stream.readFloat();

        this.WorldFog = stream.readInt();
        this.WorldNightFog = stream.readInt();

        this.WorldFogR = ((WorldFog & 0xFF0000) >> 16) / 255F;
        this.WorldFogG = ((WorldFog & 0xFF00) >> 8) / 255F;
        this.WorldFogB = (WorldFog & 0xFF) / 255F;

        this.WorldNightFogR = ((WorldNightFog & 0xFF0000) >> 16) / 255F;
        this.WorldNightFogG = ((WorldNightFog & 0xFF00) >> 8) / 255F;
        this.WorldNightFogB = (WorldNightFog & 0xFF) / 255F;

        int count = stream.readInt();
        while (count-- > 0)
        {
            String name = ReadStringFromStream(stream);
            int id = stream.readInt();
            world.AddBiome(name, id);
            this.CustomBiomes.add(name);
            this.CustomBiomeIds.put(name, id);
        }

        this.biomeConfigs = new BiomeConfig[world.getMaxBiomesCount()];

        String name = ReadStringFromStream(stream);
        BiomeConfig config = new BiomeConfig(stream, this, world.getBiomeByName(name));
        this.biomeConfigs[config.Biome.getId()] = config;

        name = ReadStringFromStream(stream);
        config = new BiomeConfig(stream, this, world.getBiomeByName(name));
        this.biomeConfigs[config.Biome.getId()] = config;

        name = ReadStringFromStream(stream);
        config = new BiomeConfig(stream, this, world.getBiomeByName(name));
        this.biomeConfigs[config.Biome.getId()] = config;

        name = ReadStringFromStream(stream);
        config = new BiomeConfig(stream, this, world.getBiomeByName(name));
        this.biomeConfigs[config.Biome.getId()] = config;

        count = stream.readInt();
        while (count-- > 0)
        {
            name = ReadStringFromStream(stream);
            this.NormalBiomes.add(name);
            config = new BiomeConfig(stream, this, world.getBiomeByName(name));
            this.biomeConfigs[config.Biome.getId()] = config;

        }

        count = stream.readInt();
        while (count-- > 0)
        {
            name = ReadStringFromStream(stream);
            this.IceBiomes.add(name);
            config = new BiomeConfig(stream, this, world.getBiomeByName(name));
            this.biomeConfigs[config.Biome.getId()] = config;
        }

        count = stream.readInt();
        while (count-- > 0)
        {
            name = ReadStringFromStream(stream);
            this.IsleBiomes.add(name);
            config = new BiomeConfig(stream, this, world.getBiomeByName(name));
            this.biomeConfigs[config.Biome.getId()] = config;
        }

        count = stream.readInt();
        while (count-- > 0)
        {
            name = ReadStringFromStream(stream);
            this.BorderBiomes.add(name);
            config = new BiomeConfig(stream, this, world.getBiomeByName(name));
            this.biomeConfigs[config.Biome.getId()] = config;
        }

        for (BiomeConfig biomeConfig : this.biomeConfigs)
            if (biomeConfig != null && biomeConfig.Biome.isCustom())
                biomeConfig.Biome.setCustom(biomeConfig);

    }


    private BiomeConfig getConfigByName(String name)
    {
        for (BiomeConfig biome : this.biomeConfigs)
        {
            if (biome.Name.equals(name))
                return biome;
        }
        return null;
    }
}
