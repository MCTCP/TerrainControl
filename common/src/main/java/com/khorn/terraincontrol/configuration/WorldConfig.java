package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.biomegenerators.BiomeGenerator;
import com.khorn.terraincontrol.customobjects.CustomObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

public class WorldConfig extends ConfigFile
{
    public final File settingsDir;
    private final Comparator<Entry<String,Integer>> CBV = new Comparator<Entry<String, Integer>>() {

        @Override
        public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2)
        {
            return o1.getValue().compareTo(o2.getValue());
        }
    };
    public Map<String, Integer> CustomBiomeIds = new HashMap<String, Integer>();

    // Holds all world CustomObjects.
    public List<CustomObject> customObjects = new ArrayList<CustomObject>();

    public ArrayList<String> NormalBiomes = new ArrayList<String>();
    public ArrayList<String> IceBiomes = new ArrayList<String>();
    public ArrayList<String> IsleBiomes = new ArrayList<String>();
    public ArrayList<String> BorderBiomes = new ArrayList<String>();

    public byte[] ReplaceMatrixBiomes = new byte[256];
    public boolean HaveBiomeReplace = false;

    public int maxSmoothRadius = 2;

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

    public boolean FrozenOcean;

    // Rivers

    public int riverRarity;
    public int riverSize;
    public boolean riversEnabled;
    public boolean improvedRivers;
    public boolean randomRivers;

    // Biome image

    public String imageFile;
    public ImageMode imageMode;
    // public int imageZoom;
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

    // Specific biome settings

    // Caves
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

    // Canyons
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

    // Pyramids (also swamp huts and jungle temples)
    public boolean rareBuildingsEnabled;
    public int minimumDistanceBetweenRareBuildings; // Minecraft's internal
    // value is 1 chunk lower
    public int maximumDistanceBetweenRareBuildings;

    // Other structures
    public boolean mineshaftsEnabled;
    public boolean netherFortressesEnabled;

    // Terrain
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
    public File customObjectsDirectory;

    public ConfigMode SettingsMode;

    public boolean isDeprecated = false;
    public WorldConfig newSettings = null;

    public TerrainMode ModeTerrain;
    public Class<? extends BiomeGenerator> biomeMode;

    public boolean BiomeConfigsHaveReplacement = false;

    public int normalBiomesRarity;
    public int iceBiomesRarity;

    public int worldHeightBits;
    public int WorldHeight;

    public long resourcesSeed;

    public BiomeConfigManager biomeConfigManager;
    
    public WorldConfig(File settingsDir, LocalWorld world, boolean checkOnly)
    {
        super(world.getName(), new File(settingsDir, TCDefaultValues.WorldSettingsName.stringValue()));
        this.settingsDir = settingsDir;

        //>>	Read the WorldConfig file
        this.readSettingsFile();
        //>>	Fix older names 
        this.renameOldSettings();
        //>>	Set the local fields based on what was read from the file
        this.readConfigSettings();
        //>>	Clamp Settings to acceptable values
        this.correctSettings();

        ReadWorldCustomObjects();

        // Need add check to clashes ( what? )
        if (this.SettingsMode != ConfigMode.WriteDisable)
            this.writeSettingsFile(this.SettingsMode == ConfigMode.WriteAll);

        world.setHeightBits(this.worldHeightBits);

        this.biomeConfigManager = new BiomeConfigManager(settingsDir, world, this, CustomBiomeIds, checkOnly);
        
    }

    /**
     * @return the biomeConfigs
     * @deprecated 
     */
    public BiomeConfig[] getBiomeConfigs()
    {
        return biomeConfigManager.getBiomeConfigs();
    }

    /**
     * @param biomeConfigs the biomeConfigs to set
     * @deprecated 
     */
    public void setBiomeConfigs(BiomeConfig[] biomeConfigs)
    {
        biomeConfigManager.setBiomeConfigs(biomeConfigs);
    }

    /**
     * @return the biomesCount
     * @deprecated 
     */
    public int getBiomesCount()
    {
        return biomeConfigManager.getBiomesCount();
    }

    /**
     * @param biomesCount the biomesCount to set
     * @deprecated 
     */
    public void setBiomesCount(int biomesCount)
    {
        biomeConfigManager.setBiomesCount(biomesCount);
    }

    private void ReadWorldCustomObjects()
    {
        customObjectsDirectory = new File(this.settingsDir, TCDefaultValues.BO_WorldDirectoryName.stringValue());

        File oldCustomObjectsDirectory = new File(settingsDir, "BOBPlugins");
        if (oldCustomObjectsDirectory.exists())
        {
            if (!oldCustomObjectsDirectory.renameTo(new File(settingsDir, TCDefaultValues.BO_WorldDirectoryName.stringValue())))
            {
                TerrainControl.log(Level.WARNING, "Fould old BOBPlugins folder, but it cannot be renamed to WorldObjects.");
                TerrainControl.log(Level.WARNING, "Please move the BO2s manually and delete BOBPlugins afterwards.");
            }
        }

        if (!customObjectsDirectory.exists())
        {
            if (!customObjectsDirectory.mkdirs())
            {
                TerrainControl.log(Level.WARNING, "Can`t create WorldObjects folder. No write permissions?");
                return;
            }
        }

        customObjects = new ArrayList<CustomObject>(TerrainControl.getCustomObjectManager().loadObjects(customObjectsDirectory).values());

        TerrainControl.log(Level.INFO, "{0} world custom objects loaded.", customObjects.size());

    }

    @Override
    protected void renameOldSettings()
    {
        renameOldSetting("WaterLevel", TCDefaultValues.WaterLevelMax);
        renameOldSetting("ModeTerrain", TCDefaultValues.TerrainMode);
        renameOldSetting("ModeBiome", TCDefaultValues.BiomeMode);
        renameOldSetting("NetherFortressEnabled", TCDefaultValues.NetherFortressesEnabled);
        renameOldSetting("PyramidsEnabled", TCDefaultValues.RareBuildingsEnabled);
    }

    @Override
    protected void correctSettings()
    {
        this.oldBiomeSize = applyBounds(this.oldBiomeSize, 0.1D, 10.0D);

        this.GenerationDepth = applyBounds(this.GenerationDepth, 1, 20);
        this.BiomeRarityScale = applyBounds(this.BiomeRarityScale, 1, Integer.MAX_VALUE);

        this.LandRarity = applyBounds(this.LandRarity, 1, 100);
        this.LandSize = applyBounds(this.LandSize, 0, this.GenerationDepth);
        this.LandFuzzy = applyBounds(this.LandFuzzy, 0, this.GenerationDepth - this.LandSize);

        this.IceRarity = applyBounds(this.IceRarity, 1, 100);
        this.IceSize = applyBounds(this.IceSize, 0, this.GenerationDepth);

        this.riverRarity = applyBounds(this.riverRarity, 0, this.GenerationDepth);
        this.riverSize = applyBounds(this.riverSize, 0, this.GenerationDepth - this.riverRarity);

        this.NormalBiomes = filterBiomes(this.NormalBiomes, this.CustomBiomeIds.keySet());
        this.IceBiomes = filterBiomes(this.IceBiomes, this.CustomBiomeIds.keySet());
        this.IsleBiomes = filterBiomes(this.IsleBiomes, this.CustomBiomeIds.keySet());
        this.BorderBiomes = filterBiomes(this.BorderBiomes, this.CustomBiomeIds.keySet());

        if (this.biomeMode == TerrainControl.getBiomeModeManager().FROM_IMAGE)
        {
            File mapFile = new File(settingsDir, imageFile);
            if (!mapFile.exists())
            {
                TerrainControl.log(Level.WARNING, "Biome map file not found. Switching BiomeMode to Normal");
                this.biomeMode = TerrainControl.getBiomeModeManager().NORMAL;
            }
        }

        this.imageFillBiome = (DefaultBiome.Contain(imageFillBiome) || CustomBiomeIds.keySet().contains(imageFillBiome)) ? imageFillBiome : TCDefaultValues.ImageFillBiome.stringValue();

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
        this.minimumDistanceBetweenRareBuildings = applyBounds(this.minimumDistanceBetweenRareBuildings, 1, Integer.MAX_VALUE);
        this.maximumDistanceBetweenRareBuildings = applyBounds(this.maximumDistanceBetweenRareBuildings, this.minimumDistanceBetweenRareBuildings, Integer.MAX_VALUE);

        if (this.biomeMode == TerrainControl.getBiomeModeManager().OLD_GENERATOR && this.ModeTerrain != TerrainMode.OldGenerator)
        {
            TerrainControl.log(Level.WARNING, "Old biome generator works only with old terrain generator!");
            this.biomeMode = TerrainControl.getBiomeModeManager().NORMAL;

        }
    }

    @Override
    protected void readConfigSettings()
    {
        // Main modes
        this.SettingsMode = readSettings(TCDefaultValues.SettingsMode);
        this.ModeTerrain = readSettings(TCDefaultValues.TerrainMode);
        this.biomeMode = TerrainControl.getBiomeModeManager().getBiomeManager((String) readSettings(TCDefaultValues.BiomeMode));

        // World and water height
        this.worldHeightBits = readSettings(TCDefaultValues.WorldHeightBits);
        this.worldHeightBits = applyBounds(this.worldHeightBits, 5, 8);
        this.WorldHeight = 1 << worldHeightBits;
        this.waterLevelMax = WorldHeight / 2 - 1;

        // Biome placement
        this.GenerationDepth = readSettings(TCDefaultValues.GenerationDepth);

        this.BiomeRarityScale = readSettings(TCDefaultValues.BiomeRarityScale);
        this.LandRarity = readSettings(TCDefaultValues.LandRarity);
        this.LandSize = readSettings(TCDefaultValues.LandSize);
        this.LandFuzzy = readSettings(TCDefaultValues.LandFuzzy);

        this.IceRarity = readSettings(TCDefaultValues.IceRarity);
        this.IceSize = readSettings(TCDefaultValues.IceSize);

        this.FrozenOcean = readSettings(TCDefaultValues.FrozenOcean);

        // Rivers

        this.riverRarity = readSettings(TCDefaultValues.RiverRarity);
        this.riverSize = readSettings(TCDefaultValues.RiverSize);
        this.riversEnabled = readSettings(TCDefaultValues.RiversEnabled);
        this.improvedRivers = readSettings(TCDefaultValues.ImprovedRivers);
        this.randomRivers = readSettings(TCDefaultValues.RandomRivers);

        // Biomes
        this.NormalBiomes = readSettings(TCDefaultValues.NormalBiomes);
        this.IceBiomes = readSettings(TCDefaultValues.IceBiomes);
        this.IsleBiomes = readSettings(TCDefaultValues.IsleBiomes);
        this.BorderBiomes = readSettings(TCDefaultValues.BorderBiomes);
        ReadCustomBiomes();

        // Images
        this.imageMode = readSettings(TCDefaultValues.ImageMode);
        this.imageFile = this.readSettings(TCDefaultValues.ImageFile);
        this.imageFillBiome = this.readSettings(TCDefaultValues.ImageFillBiome);
        this.imageXOffset = this.readSettings(TCDefaultValues.ImageXOffset);
        this.imageZOffset = this.readSettings(TCDefaultValues.ImageZOffset);

        // Old biomes
        this.oldBiomeSize = readSettings(TCDefaultValues.oldBiomeSize);
        this.minMoisture = readSettings(TCDefaultValues.minMoisture);
        this.maxMoisture = readSettings(TCDefaultValues.maxMoisture);
        this.minTemperature = readSettings(TCDefaultValues.minTemperature);
        this.maxTemperature = readSettings(TCDefaultValues.maxTemperature);

        // Fog
        this.WorldFog = readSettings(TCDefaultValues.WorldFog);
        this.WorldNightFog = readSettings(TCDefaultValues.WorldNightFog);

        this.WorldFogR = ((WorldFog & 0xFF0000) >> 16) / 255F;
        this.WorldFogG = ((WorldFog & 0xFF00) >> 8) / 255F;
        this.WorldFogB = (WorldFog & 0xFF) / 255F;

        this.WorldNightFogR = ((WorldNightFog & 0xFF0000) >> 16) / 255F;
        this.WorldNightFogG = ((WorldNightFog & 0xFF00) >> 8) / 255F;
        this.WorldNightFogB = (WorldNightFog & 0xFF) / 255F;

        // Structures
        this.strongholdsEnabled = readSettings(TCDefaultValues.StrongholdsEnabled);
        this.strongholdCount = readSettings(TCDefaultValues.StrongholdCount);
        this.strongholdDistance = readSettings(TCDefaultValues.StrongholdDistance);
        this.strongholdSpread = readSettings(TCDefaultValues.StrongholdSpread);

        this.villagesEnabled = readSettings(TCDefaultValues.VillagesEnabled);
        this.villageDistance = readSettings(TCDefaultValues.VillageDistance);
        this.villageSize = readSettings(TCDefaultValues.VillageSize);

        this.rareBuildingsEnabled = readSettings(TCDefaultValues.RareBuildingsEnabled);
        this.minimumDistanceBetweenRareBuildings = readSettings(TCDefaultValues.MinimumDistanceBetweenRareBuildings);
        this.maximumDistanceBetweenRareBuildings = readSettings(TCDefaultValues.MaximumDistanceBetweenRareBuildings);

        this.mineshaftsEnabled = readSettings(TCDefaultValues.MineshaftsEnabled);
        this.netherFortressesEnabled = readSettings(TCDefaultValues.NetherFortressesEnabled);

        // Caves
        this.caveRarity = readSettings(TCDefaultValues.caveRarity);
        this.caveFrequency = readSettings(TCDefaultValues.caveFrequency);
        this.caveMinAltitude = readSettings(TCDefaultValues.caveMinAltitude);
        this.caveMaxAltitude = readSettings(TCDefaultValues.caveMaxAltitude);
        this.individualCaveRarity = readSettings(TCDefaultValues.individualCaveRarity);
        this.caveSystemFrequency = readSettings(TCDefaultValues.caveSystemFrequency);
        this.caveSystemPocketChance = readSettings(TCDefaultValues.caveSystemPocketChance);
        this.caveSystemPocketMinSize = readSettings(TCDefaultValues.caveSystemPocketMinSize);
        this.caveSystemPocketMaxSize = readSettings(TCDefaultValues.caveSystemPocketMaxSize);
        this.evenCaveDistribution = readSettings(TCDefaultValues.evenCaveDistribution);

        // Canyons
        this.canyonRarity = readSettings(TCDefaultValues.canyonRarity);
        this.canyonMinAltitude = readSettings(TCDefaultValues.canyonMinAltitude);
        this.canyonMaxAltitude = readSettings(TCDefaultValues.canyonMaxAltitude);
        this.canyonMinLength = readSettings(TCDefaultValues.canyonMinLength);
        this.canyonMaxLength = readSettings(TCDefaultValues.canyonMaxLength);
        this.canyonDepth = readSettings(TCDefaultValues.canyonDepth);

        // Water
        this.waterLevelMax = readSettings(TCDefaultValues.WaterLevelMax);
        this.waterLevelMin = readSettings(TCDefaultValues.WaterLevelMin);
        this.waterBlock = readSettings(TCDefaultValues.WaterBlock);
        this.iceBlock = readSettings(TCDefaultValues.IceBlock);

        // Fracture
        this.fractureHorizontal = readSettings(TCDefaultValues.FractureHorizontal);
        this.fractureVertical = readSettings(TCDefaultValues.FractureVertical);

        // Bedrock
        this.disableBedrock = readSettings(TCDefaultValues.DisableBedrock);
        this.ceilingBedrock = readSettings(TCDefaultValues.CeilingBedrock);
        this.flatBedrock = readSettings(TCDefaultValues.FlatBedrock);
        this.bedrockBlock = readSettings(TCDefaultValues.BedrockobBlock);

        // Misc
        this.removeSurfaceStone = readSettings(TCDefaultValues.RemoveSurfaceStone);
        this.objectSpawnRatio = readSettings(TCDefaultValues.objectSpawnRatio);
        this.resourcesSeed = readSettings(TCDefaultValues.ResourcesSeed);

        this.oldTerrainGenerator = this.ModeTerrain == TerrainMode.OldGenerator;
    }

    private void ReadCustomBiomes()
    {

        ArrayList<String> biomes = this.readSettings(TCDefaultValues.CustomBiomes);

        for (String biome : biomes)
        {
            try
            {
                String[] keys = biome.split(":");
                CustomBiomeIds.put(keys[0], (keys.length == 2) ? Integer.valueOf(keys[1]) : -1);
            } catch (NumberFormatException e)
            {
                System.out.println("Wrong custom biome id settings: '" + biome + "'");
            }

        }

    }

    @Override
    protected void writeConfigSettings() throws IOException
    {
        // The modes
        writeBigTitle("The modes");
        writeComment("What Terrain Control does with the config files.");
        writeComment("Possible modes: WriteAll, WriteWithoutComments, WriteDisable");
        writeComment("   WriteAll - default");
        writeComment("   WriteWithoutComments - write config files without help comments");
        writeComment("   WriteDisable - doesn't write to the config files, it only reads. Doesn't auto-update the configs. Use with care!");
        writeValue(TCDefaultValues.SettingsMode, this.SettingsMode.name());

        writeComment("Possible terrain modes: Normal, OldGenerator, TerrainTest, NotGenerate, Default");
        writeComment("   Normal - use all features");
        writeComment("   OldGenerator - generate land like Beta 1.7.3 generator");
        writeComment("   TerrainTest - generate only terrain without any resources");
        writeComment("   NotGenerate - generate empty chunks");
        writeComment("   Default - use default terrain generator");
        writeValue(TCDefaultValues.TerrainMode, this.ModeTerrain.name());

        writeComment("Possible biome modes: Normal, OldGenerator, Default");
        writeComment("   Normal - use all features");
        writeComment("   FromImage - get biomes from image file");
        writeComment("   OldGenerator - generate biome like the Beta 1.7.3 generator");
        writeComment("   Default - use default Notch biome generator");
        writeValue(TCDefaultValues.BiomeMode, TerrainControl.getBiomeModeManager().getName(biomeMode));

        // Custom biomes
        writeBigTitle("Custom biomes");
        writeComment("You need to register your custom biomes here. This setting will make Terrain Control");
        writeComment("generate setting files for them. However, it won't place them in the world automatically.");
        writeComment("See the settings for your BiomeMode below on how to add them to the world.");
        writeComment("");
        writeComment("Syntax: CustomBiomes:BiomeName:id[,AnotherBiomeName:id[,...]]");
        writeComment("Example: CustomBiomes:TestBiome1:30,BiomeTest2:31");
        writeComment("This will add two biomes and generate the BiomeConfigs for them.");
        writeComment("All changes here need a server restart.");
        writeComment("");
        writeComment("Due to the way Mojang's loading code works, all biome ids need to be unique");
        writeComment("on the server. If you don't do this, the client will display the biomes just fine,");
        writeComment("but the server can think it is another biome with the same id. This will cause saplings,");
        writeComment("snowfall and mobs to work as in the other biome.");
        writeComment("");
        writeComment("The available ids range from 0 to 255 and the ids 0 to " + (DefaultBiome.values().length - 1) + " are occupied by vanilla minecraft");
        writeComment("biomes. Minecraft 1.7 will take most ids in the range 23-39 and 129-167.");
        // TODO: update above message when MC 1.7 is out

        WriteCustomBiomes();

        // Settings for BiomeMode:Normal
        writeBigTitle("Settings for BiomeMode:Normal");
        writeComment("Also applies if you are using BiomeMode:FromImage and ImageMode:ContinueNormal.");

        writeComment("Important value for generation. Bigger values appear to zoom out. All 'Sizes' must be smaller than this.");
        writeComment("Large %/total area biomes (Continents) must be set small, (limit=0)");
        writeComment("Small %/total area biomes (Oasis,Mountain Peaks) must be larger (limit=GenerationDepth)");
        writeComment("This could also represent \"Total number of biome sizes\" ");
        writeComment("Small values (about 1-2) and Large values (about 20) may affect generator performance.");
        writeValue(TCDefaultValues.GenerationDepth, this.GenerationDepth);

        writeComment("Max biome rarity from 1 to infinity. By default this is 100, but you can raise it for");
        writeComment("fine-grained control, or to create biomes with a chance of occurring smaller than 1/100.");
        writeValue(TCDefaultValues.BiomeRarityScale, this.BiomeRarityScale);

        writeSmallTitle("Biome lists");

        writeComment("Don't forget to register your custom biomes first in CustomBiomes!");

        writeComment("Biomes generated normal way. Names are case sensitive.");
        writeValue(TCDefaultValues.NormalBiomes, this.NormalBiomes);

        writeComment("Biomes generated in \"ice areas\". Names are case sensitive.");
        writeValue(TCDefaultValues.IceBiomes, this.IceBiomes);

        writeComment("Biomes used as isles in other biomes. You must set IsleInBiome in biome config for each biome here. Biome name is case sensitive.");
        writeValue(TCDefaultValues.IsleBiomes, this.IsleBiomes);

        writeComment("Biomes used as borders of other biomes. You must set BiomeIsBorder in biome config for each biome here. Biome name is case sensitive.");
        writeValue(TCDefaultValues.BorderBiomes, this.BorderBiomes);

        writeSmallTitle("Landmass settings (for NormalBiomes)");

        writeComment("Land rarity from 100 to 1. If you set smaller than 90 and LandSize near 0 beware Big oceans.");
        writeValue(TCDefaultValues.LandRarity, this.LandRarity);

        writeComment("Land size from 0 to GenerationDepth.");
        writeValue(TCDefaultValues.LandSize, this.LandSize);

        writeComment("Make land more fuzzy and make lakes. Must be from 0 to GenerationDepth - LandSize");
        writeValue(TCDefaultValues.LandFuzzy, this.LandFuzzy);

        writeSmallTitle("Ice area settings (for IceBiomes)");

        writeComment("Rarity of the \"ice areas\" from 100 to 1. 100 = ice world, 1 = no IceBiomes");
        writeValue(TCDefaultValues.IceRarity, this.IceRarity);

        writeComment("Ice area size from 0 to GenerationDepth.");
        writeValue(TCDefaultValues.IceSize, this.IceSize);

        writeComment("Set this to false to stop the ocean from freezing near when an \"ice area\" intersects with an ocean.");
        writeValue(TCDefaultValues.FrozenOcean, this.FrozenOcean);

        writeSmallTitle("Rivers");

        writeComment("River rarity. Must be from 0 to GenerationDepth.");
        writeValue(TCDefaultValues.RiverRarity, this.riverRarity);

        writeComment("River size from 0 to GenerationDepth - RiverRarity");
        writeValue(TCDefaultValues.RiverSize, this.riverSize);

        writeComment("Set this to false to prevent the river generator from doing anything.");
        writeValue(TCDefaultValues.RiversEnabled, this.riversEnabled);

        writeComment("When this is set to false, the standard river generator of Minecraft will be used.");
        writeComment("This means that a technical biome, determined by the RiverBiome setting of the biome");
        writeComment("the river is flowing through, will be used to generate the river.");
        writeComment("");
        writeComment("When enabled, the rivers won't use a technical biome in your world anymore, instead");
        writeComment("you can control them using the river settings in the BiomeConfigs.");
        writeValue(TCDefaultValues.ImprovedRivers, this.improvedRivers);

        writeComment("When set to true the rivers will no longer follow biome border most of the time.");
        writeValue(TCDefaultValues.RandomRivers, this.randomRivers);

        // Settings for BiomeMode:FromImage
        writeBigTitle("Settings for BiomeMode:FromImage");

        writeComment("Possible modes when generator outside image boundaries: Repeat, ContinueNormal, FillEmpty");
        writeComment("   Repeat - repeat image");
        writeComment("   ContinueNormal - continue normal generation");
        writeComment("   FillEmpty - fill by biome in \"ImageFillBiome settings\" ");
        writeValue(TCDefaultValues.ImageMode, this.imageMode.name());

        writeComment("Source png file for FromImage biome mode.");
        writeValue(TCDefaultValues.ImageFile, this.imageFile);

        writeComment("Biome name for fill outside image boundaries with FillEmpty mode.");
        writeValue(TCDefaultValues.ImageFillBiome, this.imageFillBiome);

        writeComment("Shifts map position from x=0 and z=0 coordinates.");
        writeValue(TCDefaultValues.ImageXOffset, this.imageXOffset);
        writeValue(TCDefaultValues.ImageZOffset, this.imageZOffset);

        // Terrain height and volatility
        writeBigTitle("Terrain height and volatility");

        writeComment("How many bits the generator uses for the height, from 5 to 8, inclusive.");
        writeComment("This setting allows you to generate terrain above y=128.");
        writeComment("Affects the height of the whole world.");
        writeComment("7 bits gives 2^7 = 128 blocks and 8 gives 2^8 = 256 blocks.");
        writeValue(TCDefaultValues.WorldHeightBits, this.worldHeightBits);

        writeComment("Can increase (values greater than 0) or decrease (values less than 0) how much the landscape is fractured horizontally.");
        writeValue(TCDefaultValues.FractureHorizontal, this.fractureHorizontal);

        writeComment("Can increase (values greater than 0) or decrease (values less than 0) how much the landscape is fractured vertically.");
        writeComment("Positive values will lead to large cliffs/overhangs, floating islands, and/or a cavern world depending on other settings.");
        writeValue(TCDefaultValues.FractureVertical, this.fractureVertical);

        // Blocks
        writeBigTitle("Blocks");

        writeComment("Attempts to replace all surface stone with biome surface block");
        writeValue(TCDefaultValues.RemoveSurfaceStone, this.removeSurfaceStone);

        writeComment("Disable bottom of map bedrock generation");
        writeValue(TCDefaultValues.DisableBedrock, this.disableBedrock);

        writeComment("Enable ceiling of map bedrock generation");
        writeValue(TCDefaultValues.CeilingBedrock, this.ceilingBedrock);

        writeComment("Make bottom layer of bedrock flat");
        writeValue(TCDefaultValues.FlatBedrock, this.flatBedrock);

        writeComment("BlockId used as bedrock");
        writeValue(TCDefaultValues.BedrockobBlock, this.bedrockBlock);

        this.writeSmallTitle("Water and ice");
        writeComment("Set water level. Every empty block under this level will be fill water or another block from WaterBlock ");
        writeValue(TCDefaultValues.WaterLevelMax, this.waterLevelMax);
        writeValue(TCDefaultValues.WaterLevelMin, this.waterLevelMin);

        writeComment("BlockId used as water in WaterLevel");
        writeValue(TCDefaultValues.WaterBlock, this.waterBlock);

        writeComment("BlockId used as ice");
        writeValue(TCDefaultValues.IceBlock, this.iceBlock);

        writeComment("Seed used for the resource generation. Can only be numeric. Leave blank to use the world seed.");
        if (this.resourcesSeed == 0)
        {   // It's zero, so leave it blank, we're using the world seed
            writeValue(TCDefaultValues.ResourcesSeed, "");
        } else
        {
            writeValue(TCDefaultValues.ResourcesSeed, this.resourcesSeed);
        }

        if (objectSpawnRatio != 1)
        {
            // Write the old objectSpawnRatio

            writeComment("LEGACY setting for compability with old worlds. This setting should be kept at 1.");
            writeComment("If the setting is set at 1, the setting will vanish from the config file. Readd it");
            writeComment("manually with another value and it will be back.");
            writeComment("");
            writeComment("When using the UseWorld or UseBiome keyword for spawning custom objects, Terrain Control");
            writeComment("spawns one of the possible custom objects. There is of course a chance that");
            writeComment("the chosen object cannot spawn. This setting tells TC how many times it should");
            writeComment("try to spawn that object.");
            writeComment("This setting doesn't affect growing saplings anymore.");
            this.writeValue(TCDefaultValues.objectSpawnRatio, this.objectSpawnRatio);
        }

        // Structures
        writeBigTitle("Structures");
        writeComment("Generate-structures in the server.properties file is ignored by Terrain Control. Use these settings instead.");
        writeComment("");

        // Strongholds
        writeSmallTitle("Strongholds");
        writeComment("Set this to false to prevent the stronghold generator from doing anything.");
        writeValue(TCDefaultValues.StrongholdsEnabled, this.strongholdsEnabled);

        writeComment("The number of strongholds in the world.");
        writeValue(TCDefaultValues.StrongholdCount, this.strongholdCount);

        writeComment("How far strongholds are from the spawn and other strongholds (minimum is 1.0, default is 32.0).");
        writeValue(TCDefaultValues.StrongholdDistance, this.strongholdDistance);

        writeComment("How concentrated strongholds are around the spawn (minimum is 1, default is 3). Lower number, lower concentration.");
        writeValue(TCDefaultValues.StrongholdSpread, this.strongholdSpread);

        // Villages
        writeSmallTitle("Villages");
        writeComment("Whether the villages are enabled or not.");
        writeValue(TCDefaultValues.VillagesEnabled, this.villagesEnabled);

        writeComment("The size of the village. Larger is bigger. Normal worlds have 0 as default, superflat worlds 1.");
        writeValue(TCDefaultValues.VillageSize, this.villageSize);

        writeComment("The minimum distance between the village centers in chunks. Minimum value is 9.");
        writeValue(TCDefaultValues.VillageDistance, this.villageDistance);

        // Rare buildings
        writeSmallTitle("Rare buildings");
        writeComment("Rare buildings are either desert pyramids, jungle temples or swamp huts.");

        writeComment("Whether rare buildings are enabled.");
        writeValue(TCDefaultValues.RareBuildingsEnabled, this.rareBuildingsEnabled);

        writeComment("The minimum distance between rare buildings in chunks.");
        writeValue(TCDefaultValues.MinimumDistanceBetweenRareBuildings, this.minimumDistanceBetweenRareBuildings);

        writeComment("The maximum distance between rare buildings in chunks.");
        writeValue(TCDefaultValues.MaximumDistanceBetweenRareBuildings, this.maximumDistanceBetweenRareBuildings);

        // Other structures
        writeSmallTitle("Other structures");
        writeValue(TCDefaultValues.MineshaftsEnabled, this.mineshaftsEnabled);
        writeValue(TCDefaultValues.NetherFortressesEnabled, this.netherFortressesEnabled);

        // Visual settings
        this.writeBigTitle("Visual settings");
        this.writeComment("Warning this section will work only for players with the single version of Terrain Control installed.");

        writeComment("World fog color");
        writeColorValue(TCDefaultValues.WorldFog, this.WorldFog);

        writeComment("World night fog color");
        writeColorValue(TCDefaultValues.WorldNightFog, this.WorldNightFog);

        // Cave settings (still using code from Bucyruss' BiomeTerrainMod)
        writeBigTitle("Cave settings");

        writeComment("This controls the odds that a given chunk will host a single cave and/or the start of a cave system.");
        writeValue(TCDefaultValues.caveRarity, this.caveRarity);

        writeComment("The number of times the cave generation algorithm will attempt to create single caves and cave");
        writeComment("systems in the given chunk. This value is larger because the likelihood for the cave generation");
        writeComment("algorithm to bailout is fairly high and it is used in a randomizer that trends towards lower");
        writeComment("random numbers. With an input of 40 (default) the randomizer will result in an average random");
        writeComment("result of 5 to 6. This can be turned off by setting evenCaveDistribution (below) to true.");
        writeValue(TCDefaultValues.caveFrequency, this.caveFrequency);

        writeComment("Sets the minimum and maximum altitudes at which caves will be generated. These values are");
        writeComment("used in a randomizer that trends towards lower numbers so that caves become more frequent");
        writeComment("the closer you get to the bottom of the map. Setting even cave distribution (above) to true");
        writeComment("will turn off this randomizer and use a flat random number generator that will create an even");
        writeComment("density of caves at all altitudes.");
        writeValue(TCDefaultValues.caveMinAltitude, this.caveMinAltitude);
        writeValue(TCDefaultValues.caveMaxAltitude, this.caveMaxAltitude);

        writeComment("The odds that the cave generation algorithm will generate a single cavern without an accompanying");
        writeComment("cave system. Note that whenever the algorithm generates an individual cave it will also attempt to");
        writeComment("generate a pocket of cave systems in the vicinity (no guarantee of connection or that the cave system");
        writeComment("will actually be created).");
        writeValue(TCDefaultValues.individualCaveRarity, this.individualCaveRarity);

        writeComment("The number of times the algorithm will attempt to start a cave system in a given chunk per cycle of");
        writeComment("the cave generation algorithm (see cave frequency setting above). Note that setting this value too");
        writeComment("high with an accompanying high cave frequency value can cause extremely long world generation time.");
        writeValue(TCDefaultValues.caveSystemFrequency, this.caveSystemFrequency);

        writeComment("This can be set to create an additional chance that a cave system pocket (a higher than normal");
        writeComment("density of cave systems) being started in a given chunk. Normally, a cave pocket will only be");
        writeComment("attempted if an individual cave is generated, but this will allow more cave pockets to be generated");
        writeComment("in addition to the individual cave trigger.");
        writeValue(TCDefaultValues.caveSystemPocketChance, this.caveSystemPocketChance);

        writeComment("The minimum and maximum size that a cave system pocket can be. This modifies/overrides the");
        writeComment("cave system frequency setting (above) when triggered.");
        writeValue(TCDefaultValues.caveSystemPocketMinSize, this.caveSystemPocketMinSize);
        writeValue(TCDefaultValues.caveSystemPocketMaxSize, this.caveSystemPocketMaxSize);

        writeComment("Setting this to true will turn off the randomizer for cave frequency (above). Do note that");
        writeComment("if you turn this on you will probably want to adjust the cave frequency down to avoid long");
        writeComment("load times at world creation.");
        writeValue(TCDefaultValues.evenCaveDistribution, this.evenCaveDistribution);

        // Canyon settings
        writeBigTitle("Canyon settings");
        writeValue(TCDefaultValues.canyonRarity, this.canyonRarity);
        writeValue(TCDefaultValues.canyonMinAltitude, this.canyonMinAltitude);
        writeValue(TCDefaultValues.canyonMaxAltitude, this.canyonMaxAltitude);
        writeValue(TCDefaultValues.canyonMinLength, this.canyonMinLength);
        writeValue(TCDefaultValues.canyonMaxLength, this.canyonMaxLength);
        writeValue(TCDefaultValues.canyonDepth, this.canyonDepth);

        // Settings for BiomeMode:OldGenerator
        writeBigTitle("Settings for BiomeMode:OldGenerator");
        writeComment("This generator works only with old terrain generator!");
        writeValue(TCDefaultValues.oldBiomeSize, this.oldBiomeSize);
        writeValue(TCDefaultValues.minMoisture, this.minMoisture);
        writeValue(TCDefaultValues.maxMoisture, this.maxMoisture);
        writeValue(TCDefaultValues.minTemperature, this.minTemperature);
        writeValue(TCDefaultValues.maxTemperature, this.maxTemperature);

    }

    private void WriteCustomBiomes() throws IOException
    {
        String output = "";
        boolean first = true;
        List<Entry<String, Integer>> a = new ArrayList<Entry<String, Integer>>(this.CustomBiomeIds.entrySet());
        Collections.sort(a, CBV);
        for (Iterator<Entry<String, Integer>> it = a.iterator(); it.hasNext();)
        {
            Entry<String, Integer> entry = it.next();
            if (!first)
                output += ",";
            first = false;
            output += entry.getKey() + ":" + String.valueOf(entry.getValue());
        }
        writeValue(TCDefaultValues.CustomBiomes, output);
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
        writeStringToStream(stream, this.name);

        stream.writeInt(this.WorldFog);
        stream.writeInt(this.WorldNightFog);

        // Custom biomes + ids
        stream.writeInt(this.CustomBiomeIds.size());
        for (Iterator<Entry<String, Integer>> it = this.CustomBiomeIds.entrySet().iterator(); it.hasNext();)
        {
            Entry<String, Integer> entry = it.next();
            writeStringToStream(stream, entry.getKey());
            stream.writeInt(entry.getValue());
        }

        // BiomeConfigs
        stream.writeInt(this.getBiomesCount());
        for (BiomeConfig config : biomeConfigManager.getBiomeConfigs())
        {
            if (config == null)
                continue;
            stream.writeInt(config.Biome.getId());
            config.Serialize(stream);
        }
    }

    // Needed for creating world config from network packet
    public WorldConfig(DataInputStream stream, LocalWorld world) throws IOException
    {
        // General information
        super(readStringFromStream(stream), null);
        this.settingsDir = null;

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
            String biomeName = readStringFromStream(stream);
            int id = stream.readInt();
            world.AddBiome(biomeName, id);
            this.CustomBiomeIds.put(biomeName, id);
        }

        // BiomeConfigs
        biomeConfigManager.setBiomeConfigs(new BiomeConfig[world.getMaxBiomesCount()]);

        count = stream.readInt();
        while (count-- > 0)
        {
            int id = stream.readInt();
            BiomeConfig config = new BiomeConfig(stream, this, world.getBiomeById(id));
            biomeConfigManager.addBiomeConfig(id,config);
        }

    }
}
