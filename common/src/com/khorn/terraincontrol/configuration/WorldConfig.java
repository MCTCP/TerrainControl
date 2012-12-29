package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.customobjects.CustomObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldConfig extends ConfigFile
{   
    public ArrayList<String> CustomBiomes = new ArrayList<String>();
    public HashMap<String, Integer> CustomBiomeIds = new HashMap<String, Integer>();

    // Holds all world CustomObjects.
    public List<CustomObject> customObjects = new ArrayList<CustomObject>();
    

    public ArrayList<String> NormalBiomes = new ArrayList<String>();
    public ArrayList<String> IceBiomes = new ArrayList<String>();
    public ArrayList<String> IsleBiomes = new ArrayList<String>();
    public ArrayList<String> BorderBiomes = new ArrayList<String>();

    public Map<Integer, BiomeConfig> biomeConfigs;

    public byte[] ReplaceMatrixBiomes = new byte[256];
    public boolean HaveBiomeReplace = false;

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

    public HashMap<Integer, Integer> biomeColorMap;

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

    // Strongholds
    public boolean strongholdsEnabled;
    public double strongholdDistance;
    public int strongholdCount;
    public int strongholdSpread;
    
    // Villages
    public boolean villagesEnabled;
    public int villageSize;
    public int villageDistance; // Has a minimum of 9

    //Terrain
    public boolean oldTerrainGenerator;

    public int waterLevelMax;
    public int waterLevelMin;
    public int waterBlock;
    public int iceBlock;

    public double fractureHorizontal;
    public double fractureVertical;

    public boolean disableBedrock;
    public boolean flatBedrock;
    public boolean ceilingBedrock;
    public int bedrockBlock;


    public boolean removeSurfaceStone;

    public int objectSpawnRatio;
    public File CustomObjectsDirectory;

    // Structures
    public boolean mineshaftsEnabled;
    
    public boolean PyramidsEnabled;
    public boolean netherFortressesEnabled;

    public File SettingsDir;
    public ConfigMode SettingsMode;

    public boolean isDeprecated = false;
    public WorldConfig newSettings = null;

    public String WorldName;
    public TerrainMode ModeTerrain;
    public BiomeMode ModeBiome;

    
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

        ReadWorldCustomObjects();

        // Check biome ids

        for (String biomeName : CustomBiomes)
            if (CustomBiomeIds.get(biomeName) == -1)
                CustomBiomeIds.put(biomeName, world.getFreeBiomeId());

        // Need add check to clashes
        if (this.SettingsMode != ConfigMode.WriteDisable)
            this.WriteSettingsFile(settingsFile, (this.SettingsMode == ConfigMode.WriteAll));

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

        //Add custom biomes to world
        for (String biomeName : this.CustomBiomes)
        {
            if (checkOnly)
                localBiomes.add(world.getNullBiome(biomeName));
            else
                localBiomes.add(world.AddBiome(biomeName, this.CustomBiomeIds.get(biomeName)));
        }

        //Build biome replace matrix
        for (int i = 0; i < this.ReplaceMatrixBiomes.length; i++)
            this.ReplaceMatrixBiomes[i] = (byte) i;

        this.biomeConfigs = new HashMap<Integer, BiomeConfig>();

        String LoadedBiomeNames = "";


        for (LocalBiome localBiome : localBiomes)
        {
            BiomeConfig config = new BiomeConfig(BiomeFolder, localBiome, this);
            if (checkOnly)
                continue;

            if (!config.ReplaceBiomeName.equals(""))
            {
                this.HaveBiomeReplace = true;
                this.ReplaceMatrixBiomes[config.Biome.getId()] = (byte) world.getBiomeIdByName(config.ReplaceBiomeName);
            }

            if (this.NormalBiomes.contains(config.Name))
                this.normalBiomesRarity += config.BiomeRarity;
            if (this.IceBiomes.contains(config.Name))
                this.iceBiomesRarity += config.BiomeRarity;

            if (!this.BiomeConfigsHaveReplacement)
                this.BiomeConfigsHaveReplacement = config.ReplaceCount > 0;
            if (this.biomeConfigs.size() != 0)
                LoadedBiomeNames += ", ";
            LoadedBiomeNames += localBiome.getName();
            this.biomeConfigs.put(localBiome.getId(), config);

            if (this.ModeBiome == BiomeMode.FromImage)
            {
                if (this.biomeColorMap == null)
                    this.biomeColorMap = new HashMap<Integer, Integer>();

                try
                {
                    int color = Integer.decode(config.BiomeColor);
                    if (color <= 0xFFFFFF)
                        this.biomeColorMap.put(color, config.Biome.getId());
                } catch (NumberFormatException ex)
                {
                    System.out.println("TerrainControl: wrong color in " + config.Biome.getName());
                }


            }
        }

        System.out.println("TerrainControl: Loaded biomes - " + LoadedBiomeNames);


    }


    private void ReadWorldCustomObjects()
    {
        CustomObjectsDirectory = new File(SettingsDir, "BOBPlugins");
        if (CustomObjectsDirectory.exists())
            if (!CustomObjectsDirectory.renameTo(new File(SettingsDir, TCDefaultValues.BO_WorldDirectoryName.stringValue())))
            {
                System.out.println("TerrainControl: Can`t rename old custom objects folder");
            }

        CustomObjectsDirectory = new File(this.SettingsDir, TCDefaultValues.BO_WorldDirectoryName.stringValue());

        if (!CustomObjectsDirectory.exists())
        {
            if (!CustomObjectsDirectory.mkdirs())
            {
                System.out.println("TerrainControl: can`t create WorldObjects CustomObjectsDirectory");
                return;
            }
        }

        customObjects = new ArrayList<CustomObject>(TerrainControl.getCustomObjectManager().loadObjects(CustomObjectsDirectory).values());

        TerrainControl.log(customObjects.size() + " world custom objects loaded");

    }

    protected void RenameOldSettings()
    {
        if (this.SettingsCache.containsKey("WaterLevel".toLowerCase()))
        {
            this.SettingsCache.put("WaterLevelMax".toLowerCase(), this.SettingsCache.get("WaterLevel".toLowerCase()));
        }
        if (this.SettingsCache.containsKey("ModeTerrain".toLowerCase()))
        {
            this.SettingsCache.put(TCDefaultValues.TerrainMode.name().toLowerCase(), this.SettingsCache.get("ModeTerrain".toLowerCase()));
        }
        if (this.SettingsCache.containsKey("ModeBiome".toLowerCase()))
        {
            this.SettingsCache.put(TCDefaultValues.BiomeMode.name().toLowerCase(), this.SettingsCache.get("ModeBiome".toLowerCase()));
        }
        if(this.SettingsCache.containsKey("NetherFortressEnabled".toLowerCase()))
        {
            this.SettingsCache.put(TCDefaultValues.NetherFortressesEnabled.name().toLowerCase(), this.SettingsCache.get("ModeBiome".toLowerCase()));
        }

    }

    protected void CorrectSettings()
    {
        this.oldBiomeSize = applyBounds(this.oldBiomeSize, 0.1D, 10.0D);

        this.GenerationDepth = applyBounds(this.GenerationDepth, 1, 20);
        this.BiomeRarityScale = applyBounds(this.BiomeRarityScale, 1, Integer.MAX_VALUE);

        this.LandRarity = applyBounds(this.LandRarity, 1, 100);
        this.LandSize = applyBounds(this.LandSize, 0, this.GenerationDepth);
        this.LandFuzzy = applyBounds(this.LandFuzzy, 0, this.GenerationDepth - this.LandSize);


        this.IceRarity = applyBounds(this.IceRarity, 1, 100);
        this.IceSize = applyBounds(this.IceSize, 0, this.GenerationDepth);

        this.RiverRarity = applyBounds(this.RiverRarity, 0, this.GenerationDepth);
        this.RiverSize = applyBounds(this.RiverSize, 0, this.GenerationDepth - this.RiverRarity);

        this.NormalBiomes = filterBiomes(this.NormalBiomes, this.CustomBiomes);
        this.IceBiomes = filterBiomes(this.IceBiomes, this.CustomBiomes);
        this.IsleBiomes = filterBiomes(this.IsleBiomes, this.CustomBiomes);
        this.BorderBiomes = filterBiomes(this.BorderBiomes, this.CustomBiomes);

        if (this.ModeBiome == BiomeMode.FromImage)
        {
            File mapFile = new File(SettingsDir, imageFile);
            if (!mapFile.exists())
            {
                TerrainControl.log("Biome map file not found. Switching BiomeMode to Normal");
                this.ModeBiome = BiomeMode.Normal;
            }
        }

        this.imageFillBiome = (DefaultBiome.Contain(imageFillBiome) || CustomBiomes.contains(imageFillBiome)) ? imageFillBiome : TCDefaultValues.ImageFillBiome.stringValue();


        this.minMoisture = applyBounds(this.minMoisture, 0, 1.0F);
        this.maxMoisture = applyBounds(this.maxMoisture, 0, 1.0F, this.minMoisture);

        this.minTemperature = applyBounds(this.minTemperature, 0, 1.0F);
        this.maxTemperature = applyBounds(this.maxTemperature, 0, 1.0F, this.minTemperature);


        this.caveRarity = applyBounds(this.caveRarity, 0, 100);
        this.caveFrequency = applyBounds(this.caveFrequency, 0, 200);
        this.caveMinAltitude = applyBounds(this.caveMinAltitude, 0, WorldHeight);
        this.caveMaxAltitude = applyBounds(this.caveMaxAltitude, 0, WorldHeight, this.caveMinAltitude);
        this.individualCaveRarity = applyBounds(this.individualCaveRarity, 0, 100);
        this.caveSystemFrequency = applyBounds(this.caveSystemFrequency, 0, 200);
        this.caveSystemPocketChance = applyBounds(this.caveSystemPocketChance, 0, 100);
        this.caveSystemPocketMinSize = applyBounds(this.caveSystemPocketMinSize, 0, 100);
        this.caveSystemPocketMaxSize = applyBounds(this.caveSystemPocketMaxSize, 0, 100, this.caveSystemPocketMinSize);


        this.canyonRarity = applyBounds(this.canyonRarity, 0, 100);
        this.canyonMinAltitude = applyBounds(this.canyonMinAltitude, 0, WorldHeight);
        this.canyonMaxAltitude = applyBounds(this.canyonMaxAltitude, 0, WorldHeight, this.canyonMinAltitude);
        this.canyonMinLength = applyBounds(this.canyonMinLength, 1, 500);
        this.canyonMaxLength = applyBounds(this.canyonMaxLength, 1, 500, this.canyonMinLength);
        this.canyonDepth = applyBounds(this.canyonDepth, 0.1D, 15D);


        this.waterLevelMin = applyBounds(this.waterLevelMin, 0, WorldHeight - 1);
        this.waterLevelMax = applyBounds(this.waterLevelMax, 0, WorldHeight - 1, this.waterLevelMin);
        
        this.villageDistance = applyBounds(this.villageDistance, 9, Integer.MAX_VALUE);

        if (this.ModeBiome == BiomeMode.OldGenerator && this.ModeTerrain != TerrainMode.OldGenerator)
        {
            TerrainControl.log("Old biome generator works only with old terrain generator!");
            this.ModeBiome = BiomeMode.Normal;

        }
    }


    protected void ReadConfigSettings()
    {
        // Main modes
        this.SettingsMode = ReadSettings(TCDefaultValues.SettingsMode);
        this.ModeTerrain = ReadSettings(TCDefaultValues.TerrainMode);
        this.ModeBiome = ReadSettings(TCDefaultValues.BiomeMode);

        // World and water height
        this.worldHeightBits = ReadSettings(TCDefaultValues.WorldHeightBits);
        this.worldHeightBits = applyBounds(this.worldHeightBits, 5, 8);
        this.WorldHeight = 1 << worldHeightBits;
        this.waterLevelMax = WorldHeight / 2 - 1;

        // Biome placement
        this.GenerationDepth = ReadSettings(TCDefaultValues.GenerationDepth);

        this.BiomeRarityScale = ReadSettings(TCDefaultValues.BiomeRarityScale);
        this.LandRarity = ReadSettings(TCDefaultValues.LandRarity);
        this.LandSize = ReadSettings(TCDefaultValues.LandSize);
        this.LandFuzzy = ReadSettings(TCDefaultValues.LandFuzzy);

        this.IceRarity = ReadSettings(TCDefaultValues.IceRarity);
        this.IceSize = ReadSettings(TCDefaultValues.IceSize);

        this.RiverRarity = ReadSettings(TCDefaultValues.RiverRarity);
        this.RiverSize = ReadSettings(TCDefaultValues.RiverSize);
        this.RiversEnabled = ReadSettings(TCDefaultValues.RiversEnabled);

        this.FrozenRivers = ReadSettings(TCDefaultValues.FrozenRivers);
        this.FrozenOcean = ReadSettings(TCDefaultValues.FrozenOcean);

        // Biomes
        this.NormalBiomes = ReadSettings(TCDefaultValues.NormalBiomes);
        this.IceBiomes = ReadSettings(TCDefaultValues.IceBiomes);
        this.IsleBiomes = ReadSettings(TCDefaultValues.IsleBiomes);
        this.BorderBiomes = ReadSettings(TCDefaultValues.BorderBiomes);
        ReadCustomBiomes();

        // Images
        this.imageMode =  ReadSettings(TCDefaultValues.ImageMode);
        this.imageFile = this.ReadSettings(TCDefaultValues.ImageFile);
        this.imageFillBiome = this.ReadSettings(TCDefaultValues.ImageFillBiome);
        this.imageXOffset = this.ReadSettings(TCDefaultValues.ImageXOffset);
        this.imageZOffset = this.ReadSettings(TCDefaultValues.ImageZOffset);

        // Old biomes
        this.oldBiomeSize = ReadSettings(TCDefaultValues.oldBiomeSize);
        this.minMoisture = ReadSettings(TCDefaultValues.minMoisture);
        this.maxMoisture = ReadSettings(TCDefaultValues.maxMoisture);
        this.minTemperature = ReadSettings(TCDefaultValues.minTemperature);
        this.maxTemperature = ReadSettings(TCDefaultValues.maxTemperature);

        // Fog
        this.WorldFog = ReadSettings(TCDefaultValues.WorldFog);
        this.WorldNightFog = ReadSettings(TCDefaultValues.WorldNightFog);

        this.WorldFogR = ((WorldFog & 0xFF0000) >> 16) / 255F;
        this.WorldFogG = ((WorldFog & 0xFF00) >> 8) / 255F;
        this.WorldFogB = (WorldFog & 0xFF) / 255F;

        this.WorldNightFogR = ((WorldNightFog & 0xFF0000) >> 16) / 255F;
        this.WorldNightFogG = ((WorldNightFog & 0xFF00) >> 8) / 255F;
        this.WorldNightFogB = (WorldNightFog & 0xFF) / 255F;

        // Structures
        this.strongholdsEnabled = ReadSettings(TCDefaultValues.StrongholdsEnabled);
        this.strongholdCount = ReadSettings(TCDefaultValues.StrongholdCount);
        this.strongholdDistance = ReadSettings(TCDefaultValues.StrongholdDistance);
        this.strongholdSpread = ReadSettings(TCDefaultValues.StrongholdSpread);
        
        this.villagesEnabled = ReadSettings(TCDefaultValues.VillagesEnabled);
        this.villageDistance = ReadSettings(TCDefaultValues.VillageDistance);
        this.villageSize = ReadSettings(TCDefaultValues.VillageSize);
        
        this.mineshaftsEnabled = ReadSettings(TCDefaultValues.MineshaftsEnabled);
        this.PyramidsEnabled = ReadSettings(TCDefaultValues.PyramidsEnabled);
        this.netherFortressesEnabled = ReadSettings(TCDefaultValues.NetherFortressesEnabled);

        // Caves
        this.caveRarity = ReadSettings(TCDefaultValues.caveRarity);
        this.caveFrequency = ReadSettings(TCDefaultValues.caveFrequency);
        this.caveMinAltitude = ReadSettings(TCDefaultValues.caveMinAltitude);
        this.caveMaxAltitude = ReadSettings(TCDefaultValues.caveMaxAltitude);
        this.individualCaveRarity = ReadSettings(TCDefaultValues.individualCaveRarity);
        this.caveSystemFrequency = ReadSettings(TCDefaultValues.caveSystemFrequency);
        this.caveSystemPocketChance = ReadSettings(TCDefaultValues.caveSystemPocketChance);
        this.caveSystemPocketMinSize = ReadSettings(TCDefaultValues.caveSystemPocketMinSize);
        this.caveSystemPocketMaxSize = ReadSettings(TCDefaultValues.caveSystemPocketMaxSize);
        this.evenCaveDistribution = ReadSettings(TCDefaultValues.evenCaveDistribution);

        // Canyons
        this.canyonRarity = ReadSettings(TCDefaultValues.canyonRarity);
        this.canyonMinAltitude = ReadSettings(TCDefaultValues.canyonMinAltitude);
        this.canyonMaxAltitude = ReadSettings(TCDefaultValues.canyonMaxAltitude);
        this.canyonMinLength = ReadSettings(TCDefaultValues.canyonMinLength);
        this.canyonMaxLength = ReadSettings(TCDefaultValues.canyonMaxLength);
        this.canyonDepth = ReadSettings(TCDefaultValues.canyonDepth);

        // Water
        this.waterLevelMax = ReadSettings(TCDefaultValues.WaterLevelMax);
        this.waterLevelMin = ReadSettings(TCDefaultValues.WaterLevelMin);
        this.waterBlock = ReadSettings(TCDefaultValues.WaterBlock);
        this.iceBlock = ReadSettings(TCDefaultValues.IceBlock);

        // Fracture
        this.fractureHorizontal = ReadSettings(TCDefaultValues.FractureHorizontal);
        this.fractureVertical = ReadSettings(TCDefaultValues.FractureVertical);

        // Bedrock
        this.disableBedrock = ReadSettings(TCDefaultValues.DisableBedrock);
        this.ceilingBedrock = ReadSettings(TCDefaultValues.CeilingBedrock);
        this.flatBedrock = ReadSettings(TCDefaultValues.FlatBedrock);
        this.bedrockBlock = ReadSettings(TCDefaultValues.BedrockobBlock);

        // Misc
        this.removeSurfaceStone = ReadSettings(TCDefaultValues.RemoveSurfaceStone);
        this.objectSpawnRatio = this.ReadSettings(TCDefaultValues.objectSpawnRatio);

        this.oldTerrainGenerator = this.ModeTerrain == TerrainMode.OldGenerator;
    }

    private void ReadCustomBiomes()
    {

        ArrayList<String> biomes = this.ReadSettings(TCDefaultValues.CustomBiomes);

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
        WriteTitle("Configuration settings");
        WriteComment("Possible configurations modes : WriteAll, WriteWithoutComments, WriteDisable");
        WriteComment("   WriteAll - default");
        WriteComment("   WriteWithoutComments - write config files without help comments");
        WriteComment("   WriteDisable - did not write anything, only read. Use with caution!");
        WriteValue(TCDefaultValues.SettingsMode.name(), this.SettingsMode.name());

        WriteTitle("World modes");
        WriteComment("Possible terrain modes : Normal, OldGenerator, TerrainTest, NotGenerate, Default");
        WriteComment("   Normal - use all features");
        WriteComment("   OldGenerator - generate land like 1.7.3 generator");
        WriteComment("   TerrainTest - generate only terrain without any resources");
        WriteComment("   NotGenerate - generate empty chunks");
        WriteComment("   Default - use default Notch terrain generator");
        WriteValue(TCDefaultValues.TerrainMode.name(), this.ModeTerrain.name());
        WriteNewLine();
        WriteComment("Possible biome modes : Normal, OldGenerator, Default");
        WriteComment("   Normal - use all features");
        WriteComment("   FromImage - get biomes from image file");
        WriteComment("   OldGenerator - generate biome like 1.7.3 generator");
        WriteComment("   Default - use default Notch biome generator");
        WriteValue(TCDefaultValues.BiomeMode.name(), this.ModeBiome.name());


        WriteTitle("Biome Generator Variables");


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
        WriteComment("  CustomBiomes:TestBiome1,BiomeTest2");
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
        
        WriteTitle("Strongholds");
        WriteComment("Not much is known about these settings. They are directly passed to the stronghold generator.");
        WriteValue(TCDefaultValues.StrongholdsEnabled.name(), this.strongholdsEnabled);
        WriteNewLine();
        WriteValue(TCDefaultValues.StrongholdCount.name(), this.strongholdCount);
        WriteNewLine();
        WriteValue(TCDefaultValues.StrongholdDistance.name(), this.strongholdDistance);
        WriteNewLine();
        WriteValue(TCDefaultValues.StrongholdSpread.name(), this.strongholdSpread);
        
        WriteTitle("Villages");
        WriteComment("Whether the villages are enabled or not.");
        WriteValue(TCDefaultValues.VillagesEnabled.name(), this.villagesEnabled);
        WriteNewLine();
        WriteComment("The size of the village. Larger is bigger. Normal worlds have 0 as default, superflat worlds 1.");
        WriteValue(TCDefaultValues.VillageSize.name(), this.villageSize);
        WriteNewLine();
        WriteComment("The minimum distance between the village centers in chunks. Minimum value is 9.");
        WriteValue(TCDefaultValues.VillageDistance.name(), this.villageDistance);
        
        WriteTitle("Other structures");
        WriteValue(TCDefaultValues.MineshaftsEnabled.name(), this.mineshaftsEnabled);
        WriteValue(TCDefaultValues.PyramidsEnabled.name(), this.PyramidsEnabled);
        WriteValue(TCDefaultValues.NetherFortressesEnabled.name(), this.netherFortressesEnabled);

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
        WriteComment("Terrain Control tries <objectSpawnRatio> times to spawn a BO2.");
        WriteComment("A high value makes BO2s with a low rarity spawn more.");
        WriteComment("Doesn't affect BO3s. It also doesn't affect growing saplings anymore.");
        this.WriteValue(TCDefaultValues.objectSpawnRatio.name(), this.objectSpawnRatio);


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

    public enum ConfigMode
    {
        WriteAll,
        WriteDisable,
        WriteWithoutComments
    }

    public void Serialize(DataOutputStream stream) throws IOException
    {
    	// General information
        WriteStringToStream(stream, this.WorldName);

        stream.writeInt(this.WorldFog);
        stream.writeInt(this.WorldNightFog);

        // Custom biomes + ids
        stream.writeInt(this.CustomBiomes.size());
        for (String name : this.CustomBiomes)
        {
            WriteStringToStream(stream, name);
            stream.writeInt(this.CustomBiomeIds.get(name));
        }

        // BiomeConfigs
        stream.writeInt(this.biomeConfigs.size());
        for (BiomeConfig config : biomeConfigs.values())
        {
            stream.writeInt(config.Biome.getId());
            config.Serialize(stream);
        }
    }


    // Need for create world config from network packet
    public WorldConfig(DataInputStream stream, LocalWorld world) throws IOException
    {
        // General information
        this.WorldName = ReadStringFromStream(stream);

        this.WorldFog = stream.readInt();
        this.WorldNightFog = stream.readInt();

        this.WorldFogR = ((WorldFog & 0xFF0000) >> 16) / 255F;
        this.WorldFogG = ((WorldFog & 0xFF00) >> 8) / 255F;
        this.WorldFogB = (WorldFog & 0xFF) / 255F;

        this.WorldNightFogR = ((WorldNightFog & 0xFF0000) >> 16) / 255F;
        this.WorldNightFogG = ((WorldNightFog & 0xFF00) >> 8) / 255F;
        this.WorldNightFogB = (WorldNightFog & 0xFF) / 255F;

        // Custom biomes + ids
        int count = stream.readInt();
        while (count-- > 0)
        {
            String name = ReadStringFromStream(stream);
            int id = stream.readInt();
            world.AddBiome(name, id);
            this.CustomBiomes.add(name);
            this.CustomBiomeIds.put(name, id);
        }

        // BiomeConfigs
        this.biomeConfigs = new HashMap<Integer, BiomeConfig>();

        count = stream.readInt();
        while (count-- > 0)
        {
            int id = stream.readInt();
            BiomeConfig config = new BiomeConfig(stream, this, world.getBiomeById(id));
            this.biomeConfigs.put(id, config);
        }

    }
}
