package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.biomegenerators.BiomeGenerator;
import com.khorn.terraincontrol.customobjects.CustomObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

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

    public BiomeConfig[] biomeConfigs;  // Must be simple array for fast access. Beware! Some ids may contain null values;
    public int biomesCount; // Overall biome count in this world.

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

    public boolean FrozenOcean;

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
    public File CustomObjectsDirectory;

    public File SettingsDir;
    public ConfigMode SettingsMode;

    public boolean isDeprecated = false;
    public WorldConfig newSettings = null;

    public String WorldName;
    public TerrainMode ModeTerrain;
    public Class<? extends BiomeGenerator> biomeMode;

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

        this.readSettingsFile(settingsFile);
        this.renameOldSettings();
        this.readConfigSettings();

        this.correctSettings();

        ReadWorldCustomObjects();

        // Check biome ids

        for (String biomeName : CustomBiomes)
            if (CustomBiomeIds.get(biomeName) == -1)
                CustomBiomeIds.put(biomeName, world.getFreeBiomeId());

        // Need add check to clashes
        if (this.SettingsMode != ConfigMode.WriteDisable)
            this.writeSettingsFile(settingsFile, (this.SettingsMode == ConfigMode.WriteAll));

        world.setHeightBits(this.worldHeightBits);

        File BiomeFolder = new File(SettingsDir, TCDefaultValues.WorldBiomeConfigDirectoryName.stringValue());
        if (!BiomeFolder.exists())
        {
            if (!BiomeFolder.mkdir())
            {
                TerrainControl.log(Level.WARNING, "Error creating biome configs directory, working with defaults");
                return;
            }
        }

        ArrayList<LocalBiome> localBiomes = new ArrayList<LocalBiome>(world.getDefaultBiomes());

        // Add custom biomes to world
        for (String biomeName : this.CustomBiomes)
        {
            if (checkOnly)
                localBiomes.add(world.getNullBiome(biomeName));
            else
                localBiomes.add(world.AddBiome(biomeName, this.CustomBiomeIds.get(biomeName)));
        }

        // Build biome replace matrix
        for (int i = 0; i < this.ReplaceMatrixBiomes.length; i++)
            this.ReplaceMatrixBiomes[i] = (byte) i;

        this.biomeConfigs = new BiomeConfig[world.getMaxBiomesCount()];
        this.biomesCount = 0;

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

            if (this.NormalBiomes.contains(config.name))
                this.normalBiomesRarity += config.BiomeRarity;
            if (this.IceBiomes.contains(config.name))
                this.iceBiomesRarity += config.BiomeRarity;

            if (!this.BiomeConfigsHaveReplacement)
                this.BiomeConfigsHaveReplacement = config.ReplaceCount > 0;
            if (biomesCount != 0)
                LoadedBiomeNames += ", ";
            LoadedBiomeNames += localBiome.getName();
            this.biomeConfigs[localBiome.getId()] = config;
            biomesCount++;

            if (this.biomeMode == TerrainControl.getBiomeModeManager().FROM_IMAGE)
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

        this.RiverRarity = applyBounds(this.RiverRarity, 0, this.GenerationDepth);
        this.RiverSize = applyBounds(this.RiverSize, 0, this.GenerationDepth - this.RiverRarity);

        this.NormalBiomes = filterBiomes(this.NormalBiomes, this.CustomBiomes);
        this.IceBiomes = filterBiomes(this.IceBiomes, this.CustomBiomes);
        this.IsleBiomes = filterBiomes(this.IsleBiomes, this.CustomBiomes);
        this.BorderBiomes = filterBiomes(this.BorderBiomes, this.CustomBiomes);

        if (this.biomeMode == TerrainControl.getBiomeModeManager().FROM_IMAGE)
        {
            File mapFile = new File(SettingsDir, imageFile);
            if (!mapFile.exists())
            {
                TerrainControl.log("Biome map file not found. Switching BiomeMode to Normal");
                this.biomeMode = TerrainControl.getBiomeModeManager().NORMAL;
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
        this.minimumDistanceBetweenRareBuildings = applyBounds(this.minimumDistanceBetweenRareBuildings, 1, Integer.MAX_VALUE);
        this.maximumDistanceBetweenRareBuildings = applyBounds(this.maximumDistanceBetweenRareBuildings, this.minimumDistanceBetweenRareBuildings, Integer.MAX_VALUE);

        if (this.biomeMode == TerrainControl.getBiomeModeManager().OLD_GENERATOR && this.ModeTerrain != TerrainMode.OldGenerator)
        {
            TerrainControl.log("Old biome generator works only with old terrain generator!");
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

        this.RiverRarity = readSettings(TCDefaultValues.RiverRarity);
        this.RiverSize = readSettings(TCDefaultValues.RiverSize);
        this.RiversEnabled = readSettings(TCDefaultValues.RiversEnabled);

        this.FrozenOcean = readSettings(TCDefaultValues.FrozenOcean);

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
        this.objectSpawnRatio = this.readSettings(TCDefaultValues.objectSpawnRatio);

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
        writeValue(TCDefaultValues.SettingsMode.name(), this.SettingsMode.name());
        writeNewLine();

        writeComment("Possible terrain modes: Normal, OldGenerator, TerrainTest, NotGenerate, Default");
        writeComment("   Normal - use all features");
        writeComment("   OldGenerator - generate land like 1.7.3 generator");
        writeComment("   TerrainTest - generate only terrain without any resources");
        writeComment("   NotGenerate - generate empty chunks");
        writeComment("   Default - use default terrain generator");
        writeValue(TCDefaultValues.TerrainMode.name(), this.ModeTerrain.name());
        writeNewLine();

        writeComment("Possible biome modes: Normal, OldGenerator, Default");
        writeComment("   Normal - use all features");
        writeComment("   FromImage - get biomes from image file");
        writeComment("   OldGenerator - generate biome like the Beta 1.7.3 generator");
        writeComment("   Default - use default Notch biome generator");
        writeValue(TCDefaultValues.BiomeMode.name(), TerrainControl.getBiomeModeManager().getName(biomeMode));

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
        writeComment("biomes. To leave room for new vanilla biomes, it is recommend to not use ids below 30.");

        WriteCustomBiomes();

        // Settings for BiomeMode:Normal
        writeBigTitle("Settings for BiomeMode:Normal");
        writeComment("Also applies if you are using BiomeMode:FromImage and ImageMode:ContinueNormal.");
        writeNewLine();

        writeComment("IMPORTANT value for generation. Bigger values appear to zoom out. All 'Sizes' must be smaller than this.");
        writeComment("Large %/total area biomes (Continents) must be set small, (limit=0)");
        writeComment("Small %/total area biomes (Oasis,Mountain Peaks) must be larger (limit=GenerationDepth)");
        writeComment("This could also represent \"Total number of biome sizes\" ");
        writeComment("Small values (about 1-2) and Large values (about 20) may affect generator performance.");
        writeValue(TCDefaultValues.GenerationDepth.name(), this.GenerationDepth);
        writeNewLine();

        writeComment("Max biome rarity from 1 to infinity. By default this is 100, but you can raise it for");
        writeComment("fine-grained control, or to create biomes with a chance of occurring smaller than 1/100.");
        writeValue(TCDefaultValues.BiomeRarityScale.name(), this.BiomeRarityScale);
        writeNewLine();

        writeSmallTitle("Biome lists");

        writeComment("Don't forget to register your custom biomes first in CustomBiomes!");
        writeNewLine();

        writeComment("Biomes which used in normal biome algorithm. Biome name is case sensitive.");
        writeValue(TCDefaultValues.NormalBiomes.name(), this.NormalBiomes);
        writeNewLine();

        writeComment("Biomes which used in ice biome algorithm. Biome name is case sensitive.");
        writeValue(TCDefaultValues.IceBiomes.name(), this.IceBiomes);
        writeNewLine();

        writeComment("Biomes which used as isles. You must set IsleInBiome in biome config for each biome here. Biome name is case sensitive.");
        writeValue(TCDefaultValues.IsleBiomes.name(), this.IsleBiomes);
        writeNewLine();

        writeComment("Biomes which used as borders. You must set BiomeIsBorder in biome config for each biome here. Biome name is case sensitive.");
        writeValue(TCDefaultValues.BorderBiomes.name(), this.BorderBiomes);
        writeNewLine();

        writeSmallTitle("Landmass settings (for NormalBiomes)");

        writeComment("Land rarity from 100 to 1. If you set smaller than 90 and LandSize near 0 beware Big oceans.");
        writeValue(TCDefaultValues.LandRarity.name(), this.LandRarity);
        writeNewLine();

        writeComment("Land size from 0 to GenerationDepth.");
        writeValue(TCDefaultValues.LandSize.name(), this.LandSize);
        writeNewLine();

        writeComment("Make land more fuzzy and make lakes. Must be from 0 to GenerationDepth - LandSize");
        writeValue(TCDefaultValues.LandFuzzy.name(), this.LandFuzzy);
        writeNewLine();

        writeSmallTitle("Ice area settings (for IceBiomes)");

        writeComment("Ice areas rarity from 100 to 1. If you set smaller than 90 and IceSize near 0 beware ice world");
        writeValue(TCDefaultValues.IceRarity.name(), this.IceRarity);
        writeNewLine();

        writeComment("Ice area size from 0 to GenerationDepth.");
        writeValue(TCDefaultValues.IceSize.name(), this.IceSize);
        writeNewLine();

        writeValue(TCDefaultValues.FrozenOcean.name(), this.FrozenOcean);
        writeNewLine();

        writeSmallTitle("River settings");

        writeComment("River rarity.Must be from 0 to GenerationDepth.");
        writeValue(TCDefaultValues.RiverRarity.name(), this.RiverRarity);
        writeNewLine();

        writeComment("River size from 0 to GenerationDepth - RiverRarity");
        writeValue(TCDefaultValues.RiverSize.name(), this.RiverSize);
        writeNewLine();

        writeValue(TCDefaultValues.RiversEnabled.name(), this.RiversEnabled);
        writeNewLine();

        // Settings for BiomeMode:FromImage
        writeBigTitle("Settings for BiomeMode:FromImage");

        writeComment("Possible modes when generator outside image boundaries: Repeat, ContinueNormal, FillEmpty");
        writeComment("   Repeat - repeat image");
        writeComment("   ContinueNormal - continue normal generation");
        writeComment("   FillEmpty - fill by biome in \"ImageFillBiome settings\" ");
        writeValue(TCDefaultValues.ImageMode.name(), this.imageMode.name());
        writeNewLine();

        writeComment("Source png file for FromImage biome mode.");
        writeValue(TCDefaultValues.ImageFile.name(), this.imageFile);
        writeNewLine();

        writeComment("Biome name for fill outside image boundaries with FillEmpty mode.");
        writeValue(TCDefaultValues.ImageFillBiome.name(), this.imageFillBiome);
        writeNewLine();

        writeComment("Shifts map position from x=0 and z=0 coordinates.");
        writeValue(TCDefaultValues.ImageXOffset.name(), this.imageXOffset);
        writeValue(TCDefaultValues.ImageZOffset.name(), this.imageZOffset);

        // Terrain Generator Variables
        writeBigTitle("Terrain Generator Variables");
        writeComment("Height bits determinate generation height. Min 5, max 8");
        writeComment("For example 7 = 128 height, 8 = 256 height");
        writeValue(TCDefaultValues.WorldHeightBits.name(), this.worldHeightBits);
        writeNewLine();

        writeComment("Set water level. Every empty block under this level will be fill water or another block from WaterBlock ");
        writeValue(TCDefaultValues.WaterLevelMax.name(), this.waterLevelMax);
        writeValue(TCDefaultValues.WaterLevelMin.name(), this.waterLevelMin);
        writeNewLine();
        writeComment("BlockId used as water in WaterLevel");
        writeValue(TCDefaultValues.WaterBlock.name(), this.waterBlock);
        writeNewLine();
        writeComment("BlockId used as ice");
        writeValue(TCDefaultValues.IceBlock.name(), this.iceBlock);

        writeNewLine();
        writeComment("Can increase (values greater than 0) or decrease (values less than 0) how much the landscape is fractured horizontally.");
        writeValue(TCDefaultValues.FractureHorizontal.name(), this.fractureHorizontal);

        writeNewLine();
        writeComment("Can increase (values greater than 0) or decrease (values less than 0) how much the landscape is fractured vertically.");
        writeComment("Positive values will lead to large cliffs/overhangs, floating islands, and/or a cavern world depending on other settings.");
        writeValue(TCDefaultValues.FractureVertical.name(), this.fractureVertical);

        writeNewLine();
        writeComment("Attempts to replace all surface stone with biome surface block");
        writeValue(TCDefaultValues.RemoveSurfaceStone.name(), this.removeSurfaceStone);

        writeNewLine();
        writeComment("Disable bottom of map bedrock generation");
        writeValue(TCDefaultValues.DisableBedrock.name(), this.disableBedrock);

        writeNewLine();
        writeComment("Enable ceiling of map bedrock generation");
        writeValue(TCDefaultValues.CeilingBedrock.name(), this.ceilingBedrock);

        writeNewLine();
        writeComment("Make bottom layer of bedrock flat");
        writeValue(TCDefaultValues.FlatBedrock.name(), this.flatBedrock);

        writeNewLine();
        writeComment("BlockId used as bedrock");
        writeValue(TCDefaultValues.BedrockobBlock.name(), this.bedrockBlock);

        if(objectSpawnRatio != 1)
        {
            // Write the old objectSpawnRatio
            writeNewLine();
            writeComment("LEGACY setting for compability with old worlds. This setting should be kept at 1.");
            writeComment("If the setting is set at 1, the setting will vanish from the config file. Readd it");
            writeComment("manually with another value and it will be back.");
            writeComment("");
            writeComment("When using the UseWorld or UseBiome keyword for spawning custom objects, Terrain Control");
            writeComment("spawns one of the possible custom objects. There is of course a chance that");
            writeComment("the chosen object cannot spawn. This setting tells TC how many times it should");
            writeComment("try to spawn that object.");
            writeComment("This setting doesn't affect growing saplings anymore.");
            this.writeValue(TCDefaultValues.objectSpawnRatio.name(), this.objectSpawnRatio);
        }

        // Strongholds
        writeBigTitle("Strongholds");
        writeComment("Not much is known about these settings. They are directly passed to the stronghold generator.");
        writeValue(TCDefaultValues.StrongholdsEnabled.name(), this.strongholdsEnabled);
        writeNewLine();
        writeValue(TCDefaultValues.StrongholdCount.name(), this.strongholdCount);
        writeNewLine();
        writeValue(TCDefaultValues.StrongholdDistance.name(), this.strongholdDistance);
        writeNewLine();
        writeValue(TCDefaultValues.StrongholdSpread.name(), this.strongholdSpread);

        // Villages
        writeBigTitle("Villages");
        writeComment("Whether the villages are enabled or not.");
        writeValue(TCDefaultValues.VillagesEnabled.name(), this.villagesEnabled);
        writeNewLine();
        writeComment("The size of the village. Larger is bigger. Normal worlds have 0 as default, superflat worlds 1.");
        writeValue(TCDefaultValues.VillageSize.name(), this.villageSize);
        writeNewLine();
        writeComment("The minimum distance between the village centers in chunks. Minimum value is 9.");
        writeValue(TCDefaultValues.VillageDistance.name(), this.villageDistance);

        // Rare buildings
        writeBigTitle("Rare buildings");
        writeComment("Rare buildings are either desert pyramids, jungle temples or swamp huts.");
        writeNewLine();
        writeComment("Whether rare buildings are enabled.");
        writeValue(TCDefaultValues.RareBuildingsEnabled.name(), this.rareBuildingsEnabled);
        writeNewLine();
        writeComment("The minimum distance between rare buildings in chunks.");
        writeValue(TCDefaultValues.MinimumDistanceBetweenRareBuildings.name(), this.minimumDistanceBetweenRareBuildings);
        writeNewLine();
        writeComment("The maximum distance between rare buildings in chunks.");
        writeValue(TCDefaultValues.MaximumDistanceBetweenRareBuildings.name(), this.maximumDistanceBetweenRareBuildings);

        // Other structures
        writeBigTitle("Other structures");
        writeValue(TCDefaultValues.MineshaftsEnabled.name(), this.mineshaftsEnabled);
        writeValue(TCDefaultValues.NetherFortressesEnabled.name(), this.netherFortressesEnabled);

        // Visual settings
        this.writeBigTitle("Visual settings");
        this.writeComment("Warning this section will work only for players with the single version of Terrain Control installed.");

        writeComment("World fog color");
        writeColorValue(TCDefaultValues.WorldFog.name(), this.WorldFog);
        this.writeNewLine();

        writeComment("World night fog color");
        writeColorValue(TCDefaultValues.WorldNightFog.name(), this.WorldNightFog);
        this.writeNewLine();

        // Cave settings (still using code from Bucyruss' BiomeTerrainMod)
        writeBigTitle("Cave settings");

        writeComment("This controls the odds that a given chunk will host a single cave and/or the start of a cave system.");
        writeValue(TCDefaultValues.caveRarity.name(), this.caveRarity);
        writeNewLine();

        writeComment("The number of times the cave generation algorithm will attempt to create single caves and cave");
        writeComment("systems in the given chunk. This value is larger because the likelihood for the cave generation");
        writeComment("algorithm to bailout is fairly high and it is used in a randomizer that trends towards lower");
        writeComment("random numbers. With an input of 40 (default) the randomizer will result in an average random");
        writeComment("result of 5 to 6. This can be turned off by setting evenCaveDistribution (below) to true.");
        writeValue(TCDefaultValues.caveFrequency.name(), this.caveFrequency);
        writeNewLine();

        writeComment("Sets the minimum and maximum altitudes at which caves will be generated. These values are");
        writeComment("used in a randomizer that trends towards lower numbers so that caves become more frequent");
        writeComment("the closer you get to the bottom of the map. Setting even cave distribution (above) to true");
        writeComment("will turn off this randomizer and use a flat random number generator that will create an even");
        writeComment("density of caves at all altitudes.");
        writeValue(TCDefaultValues.caveMinAltitude.name(), this.caveMinAltitude);
        writeValue(TCDefaultValues.caveMaxAltitude.name(), this.caveMaxAltitude);
        writeNewLine();

        writeComment("The odds that the cave generation algorithm will generate a single cavern without an accompanying");
        writeComment("cave system. Note that whenever the algorithm generates an individual cave it will also attempt to");
        writeComment("generate a pocket of cave systems in the vicinity (no guarantee of connection or that the cave system");
        writeComment("will actually be created).");
        writeValue(TCDefaultValues.individualCaveRarity.name(), this.individualCaveRarity);
        writeNewLine();

        writeComment("The number of times the algorithm will attempt to start a cave system in a given chunk per cycle of");
        writeComment("the cave generation algorithm (see cave frequency setting above). Note that setting this value too");
        writeComment("high with an accompanying high cave frequency value can cause extremely long world generation time.");
        writeValue(TCDefaultValues.caveSystemFrequency.name(), this.caveSystemFrequency);
        writeNewLine();

        writeComment("This can be set to create an additional chance that a cave system pocket (a higher than normal");
        writeComment("density of cave systems) being started in a given chunk. Normally, a cave pocket will only be");
        writeComment("attempted if an individual cave is generated, but this will allow more cave pockets to be generated");
        writeComment("in addition to the individual cave trigger.");
        writeValue(TCDefaultValues.caveSystemPocketChance.name(), this.caveSystemPocketChance);
        writeNewLine();

        writeComment("The minimum and maximum size that a cave system pocket can be. This modifies/overrides the");
        writeComment("cave system frequency setting (above) when triggered.");
        writeValue(TCDefaultValues.caveSystemPocketMinSize.name(), this.caveSystemPocketMinSize);
        writeValue(TCDefaultValues.caveSystemPocketMaxSize.name(), this.caveSystemPocketMaxSize);
        writeNewLine();

        writeComment("Setting this to true will turn off the randomizer for cave frequency (above). Do note that");
        writeComment("if you turn this on you will probably want to adjust the cave frequency down to avoid long");
        writeComment("load times at world creation.");
        writeValue(TCDefaultValues.evenCaveDistribution.name(), this.evenCaveDistribution);

        // Canyon settings
        writeBigTitle("Canyon settings");
        writeValue(TCDefaultValues.canyonRarity.name(), this.canyonRarity);
        writeValue(TCDefaultValues.canyonMinAltitude.name(), this.canyonMinAltitude);
        writeValue(TCDefaultValues.canyonMaxAltitude.name(), this.canyonMaxAltitude);
        writeValue(TCDefaultValues.canyonMinLength.name(), this.canyonMinLength);
        writeValue(TCDefaultValues.canyonMaxLength.name(), this.canyonMaxLength);
        writeValue(TCDefaultValues.canyonDepth.name(), this.canyonDepth);

        // Settings for BiomeMode:OldGenerator
        writeBigTitle("Settings for BiomeMode:OldGenerator");
        writeComment("This generator works only with old terrain generator!");
        writeValue(TCDefaultValues.oldBiomeSize.name(), this.oldBiomeSize);
        writeValue(TCDefaultValues.minMoisture.name(), this.minMoisture);
        writeValue(TCDefaultValues.maxMoisture.name(), this.maxMoisture);
        writeValue(TCDefaultValues.minTemperature.name(), this.minTemperature);
        writeValue(TCDefaultValues.maxTemperature.name(), this.maxTemperature);

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
        writeValue(TCDefaultValues.CustomBiomes.name(), output);

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
        Normal, OldGenerator, TerrainTest, NotGenerate, Default
    }

    public enum ImageMode
    {
        Repeat, ContinueNormal, FillEmpty,
    }

    public enum ConfigMode
    {
        WriteAll, WriteDisable, WriteWithoutComments
    }

    public void Serialize(DataOutputStream stream) throws IOException
    {
        // General information
        writeStringToStream(stream, this.WorldName);

        stream.writeInt(this.WorldFog);
        stream.writeInt(this.WorldNightFog);

        // Custom biomes + ids
        stream.writeInt(this.CustomBiomes.size());
        for (String name : this.CustomBiomes)
        {
            writeStringToStream(stream, name);
            stream.writeInt(this.CustomBiomeIds.get(name));
        }

        // BiomeConfigs
        stream.writeInt(this.biomesCount);
        for (BiomeConfig config : biomeConfigs)
        {
            if (config == null)
                continue;
            stream.writeInt(config.Biome.getId());
            config.Serialize(stream);
        }
    }

    // Need for create world config from network packet
    public WorldConfig(DataInputStream stream, LocalWorld world) throws IOException
    {
        // General information
        this.WorldName = readStringFromStream(stream);

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
            String name = readStringFromStream(stream);
            int id = stream.readInt();
            world.AddBiome(name, id);
            this.CustomBiomes.add(name);
            this.CustomBiomeIds.put(name, id);
        }

        // BiomeConfigs
        this.biomeConfigs = new BiomeConfig[world.getMaxBiomesCount()];

        count = stream.readInt();
        while (count-- > 0)
        {
            int id = stream.readInt();
            BiomeConfig config = new BiomeConfig(stream, this, world.getBiomeById(id));
            this.biomeConfigs[id] = config;
        }

    }
}
