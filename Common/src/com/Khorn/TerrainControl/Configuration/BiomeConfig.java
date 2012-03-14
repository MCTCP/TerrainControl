package com.Khorn.TerrainControl.Configuration;

import com.Khorn.TerrainControl.DefaultBiome;
import com.Khorn.TerrainControl.DefaultMaterial;
import com.Khorn.TerrainControl.Generator.ResourceGens.ResourceType;
import com.Khorn.TerrainControl.Generator.ResourceGens.TreeType;

import com.Khorn.TerrainControl.LocalBiome;


import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class BiomeConfig extends ConfigFile
{
    public short[][] ReplaceMatrixBlocks = new short[256][];
    public int ReplaceCount = 0;


    public int BiomeSize;
    public int BiomeRarity;

    public String BiomeColor;

    public ArrayList<String> BiomeIsBorder;
    public ArrayList<String> IsleInBiome;
    public ArrayList<String> NotBorderNear;


    //Surface config
    public float BiomeHeight;
    public float BiomeVolatility;

    public float BiomeTemperature;
    public float BiomeWetness;

    public boolean BiomeRivers;


    public byte SurfaceBlock;
    public byte GroundBlock;

    public boolean evenWaterSourceDistribution;
    public boolean evenLavaSourceDistribution;

    public boolean disableNotchPonds;

    public int SkyColor;
    public int WaterColor;

    public int GrassColor;
    public int FoliageColor;

    public Resource[] ResourceSequence = new Resource[256];

    public double maxAverageHeight;
    public double maxAverageDepth;
    public double volatility1;
    public double volatility2;
    public double volatilityWeight1;
    public double volatilityWeight2;
    private double volatilityRaw1;
    private double volatilityRaw2;
    private double volatilityWeightRaw1;
    private double volatilityWeightRaw2;
    public boolean disableNotchHeightControl;
    public double[] heightMatrix;


    public int ResourceCount = 0;


    public LocalBiome Biome;
    private WorldConfig worldConfig;
    public String Name;


    public BiomeConfig(File settingsDir, LocalBiome biome, WorldConfig config)
    {

        this.Biome = biome;
        this.Name = biome.getName();
        worldConfig = config;
        InitDefaults();

        File settingsFile = new File(settingsDir, this.Name + TCDefaultValues.WorldBiomeConfigName.stringValue());

        this.ReadSettingsFile(settingsFile);
        this.RenameOldSettings();
        this.ReadConfigSettings();

        this.CorrectSettings();
        if (!settingsFile.exists())
            this.CreateDefaultResources();
        this.WriteSettingsFile(settingsFile);

        if (biome.isCustom())
            biome.setCustom(this);


    }


    public int getTemperature()
    {
        return (int) (this.BiomeTemperature * 65536.0F);
    }

    public int getWetness()
    {
        return (int) (this.BiomeWetness * 65536.0F);
    }

    private void CreateDefaultResources()
    {
        Resource resource;

        //Underground lakes
        resource = new Resource(ResourceType.UnderGroundLake, TCDefaultValues.undergroundLakeMinSize.intValue(), TCDefaultValues.undergroundLakeMaxSize.intValue(), TCDefaultValues.undergroundLakeFrequency.intValue(), TCDefaultValues.undergroundLakeRarity.intValue(), TCDefaultValues.undergroundLakeMinAltitude.intValue(), TCDefaultValues.undergroundLakeMaxAltitude.intValue());
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Dungeon
        resource = new Resource(ResourceType.Dungeon, 0, 0, 0, TCDefaultValues.dungeonFrequency.intValue(), TCDefaultValues.dungeonRarity.intValue(), TCDefaultValues.dungeonMinAltitude.intValue(), this.worldConfig.WorldHeight, new int[]{DefaultMaterial.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Resource(ResourceType type,int blockId, int blockData, int size,int frequency, int rarity, int minAltitude,int maxAltitude,int[] sourceBlockIds)
        //Dirt
        resource = new Resource(ResourceType.Ore, DefaultMaterial.DIRT.id, 0, TCDefaultValues.dirtDepositSize.intValue(), TCDefaultValues.dirtDepositFrequency.intValue(), TCDefaultValues.dirtDepositRarity.intValue(), TCDefaultValues.dirtDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, new int[]{DefaultMaterial.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Gravel
        resource = new Resource(ResourceType.Ore, DefaultMaterial.GRAVEL.id, 0, TCDefaultValues.gravelDepositSize.intValue(), TCDefaultValues.gravelDepositFrequency.intValue(), TCDefaultValues.gravelDepositRarity.intValue(), TCDefaultValues.gravelDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, new int[]{DefaultMaterial.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Clay
        resource = new Resource(ResourceType.Ore, DefaultMaterial.CLAY.id, 0, TCDefaultValues.clayDepositSize.intValue(), TCDefaultValues.clayDepositFrequency.intValue(), TCDefaultValues.clayDepositRarity.intValue(), TCDefaultValues.clayDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, new int[]{DefaultMaterial.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Coal
        resource = new Resource(ResourceType.Ore, DefaultMaterial.COAL_ORE.id, 0, TCDefaultValues.coalDepositSize.intValue(), TCDefaultValues.coalDepositFrequency.intValue(), TCDefaultValues.coalDepositRarity.intValue(), TCDefaultValues.coalDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, new int[]{DefaultMaterial.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Iron
        resource = new Resource(ResourceType.Ore, DefaultMaterial.IRON_ORE.id, 0, TCDefaultValues.ironDepositSize.intValue(), TCDefaultValues.ironDepositFrequency.intValue(), TCDefaultValues.ironDepositRarity.intValue(), TCDefaultValues.ironDepositMinAltitude.intValue(), this.worldConfig.WorldHeight / 2, new int[]{DefaultMaterial.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Gold
        resource = new Resource(ResourceType.Ore, DefaultMaterial.GOLD_ORE.id, 0, TCDefaultValues.goldDepositSize.intValue(), TCDefaultValues.goldDepositFrequency.intValue(), TCDefaultValues.goldDepositRarity.intValue(), TCDefaultValues.goldDepositMinAltitude.intValue(), this.worldConfig.WorldHeight / 4, new int[]{DefaultMaterial.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Redstone
        resource = new Resource(ResourceType.Ore, DefaultMaterial.REDSTONE_ORE.id, 0, TCDefaultValues.redstoneDepositSize.intValue(), TCDefaultValues.redstoneDepositFrequency.intValue(), TCDefaultValues.redstoneDepositRarity.intValue(), TCDefaultValues.redstoneDepositMinAltitude.intValue(), this.worldConfig.WorldHeight / 8, new int[]{DefaultMaterial.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Diamond
        resource = new Resource(ResourceType.Ore, DefaultMaterial.DIAMOND_ORE.id, 0, TCDefaultValues.diamondDepositSize.intValue(), TCDefaultValues.diamondDepositFrequency.intValue(), TCDefaultValues.diamondDepositRarity.intValue(), TCDefaultValues.diamondDepositMinAltitude.intValue(), this.worldConfig.WorldHeight / 8, new int[]{DefaultMaterial.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Lapislazuli
        resource = new Resource(ResourceType.Ore, DefaultMaterial.LAPIS_ORE.id, 0, TCDefaultValues.lapislazuliDepositSize.intValue(), TCDefaultValues.lapislazuliDepositFrequency.intValue(), TCDefaultValues.lapislazuliDepositRarity.intValue(), TCDefaultValues.lapislazuliDepositMinAltitude.intValue(), this.worldConfig.WorldHeight / 8, new int[]{DefaultMaterial.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;


        //Under water sand
        resource = new Resource(ResourceType.UnderWaterOre, DefaultMaterial.SAND.id, 0, TCDefaultValues.waterSandDepositSize.intValue(), TCDefaultValues.waterSandDepositFrequency.intValue(), TCDefaultValues.waterSandDepositRarity.intValue(), 0, 0, new int[]{DefaultMaterial.DIRT.id, DefaultMaterial.GRASS.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Under water clay
        if (this.DefaultClay > 0)
        {
            resource = new Resource(ResourceType.UnderWaterOre, DefaultMaterial.CLAY.id, 0, TCDefaultValues.waterClayDepositSize.intValue(), this.DefaultClay, TCDefaultValues.waterClayDepositRarity.intValue(), 0, 0, new int[]{DefaultMaterial.DIRT.id, DefaultMaterial.CLAY.id});
            this.ResourceSequence[this.ResourceCount++] = resource;
        }
        //Custom objects
        resource = new Resource(ResourceType.CustomObject);
        this.ResourceSequence[this.ResourceCount++] = resource;

        DefaultBiome biome = DefaultBiome.getBiome(this.Biome.getId());
        if ( biome != null)
        switch (biome)
        {
            case OCEAN: // Ocean - default
            case EXTREME_HILLS: // BigHills - default
            case RIVER: // River - default
            case SMALL_MOUNTAINS: // SmallHills
                resource = new Resource(ResourceType.Tree, this.DefaultTrees, new TreeType[]{TreeType.BigTree, TreeType.Tree}, new int[]{1, 9});
                this.ResourceSequence[this.ResourceCount++] = resource;
                break;
            case PLAINS: // Plains - no tree
            case DESERT: // Desert - no tree
            case DESERT_HILLS: //HillsDesert
                break;
            case FOREST_HILLS: // HillsForest
            case FOREST: // Forest - forest
                resource = new Resource(ResourceType.Tree, this.DefaultTrees, new TreeType[]{TreeType.Forest, TreeType.BigTree, TreeType.Tree}, new int[]{20, 10, 100});
                this.ResourceSequence[this.ResourceCount++] = resource;
                break;
            case TAIGA_HILLS: //HillsTaiga
            case TAIGA: // Taiga - taiga
                resource = new Resource(ResourceType.Tree, this.DefaultTrees, new TreeType[]{TreeType.Taiga1, TreeType.Taiga2}, new int[]{35, 100});
                this.ResourceSequence[this.ResourceCount++] = resource;
                break;
            case SWAMPLAND: // Swamp - swamp
                resource = new Resource(ResourceType.Tree, this.DefaultTrees, new TreeType[]{TreeType.SwampTree}, new int[]{100});
                this.ResourceSequence[this.ResourceCount++] = resource;
                break;
            case MUSHROOM_ISLAND: // Mushroom island
                resource = new Resource(ResourceType.Tree, this.DefaultTrees, new TreeType[]{TreeType.HugeMushroom}, new int[]{100});
                this.ResourceSequence[this.ResourceCount++] = resource;
                break;
            case JUNGLE:// Jungle
            case JUNGLE_HILLS:
                resource = new Resource(ResourceType.Tree, this.DefaultTrees, new TreeType[]{TreeType.BigTree, TreeType.GroundBush, TreeType.JungleTree, TreeType.Tree}, new int[]{10, 50, 35, 100});
                this.ResourceSequence[this.ResourceCount++] = resource;
                break;


        }
        if (this.DefaultWaterLily > 0)
        {
            resource = new Resource(ResourceType.AboveWaterRes, DefaultMaterial.WATER_LILY.id, 0, 0, this.DefaultWaterLily, 100, 0, 0, new int[0]);
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        if (this.DefaultFlowers > 0)
        {
            //Red flower
            resource = new Resource(ResourceType.Plant, DefaultMaterial.RED_ROSE.id, 0, 0, this.DefaultFlowers, TCDefaultValues.roseDepositRarity.intValue(), TCDefaultValues.roseDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, new int[]{DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id, DefaultMaterial.SOIL.id});
            this.ResourceSequence[this.ResourceCount++] = resource;

            //Yellow flower
            resource = new Resource(ResourceType.Plant, DefaultMaterial.YELLOW_FLOWER.id, 0, 0, this.DefaultFlowers, TCDefaultValues.flowerDepositRarity.intValue(), TCDefaultValues.flowerDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, new int[]{DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id, DefaultMaterial.SOIL.id});
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        if (this.DefaultMushroom > 0)
        {
            //Red mushroom
            resource = new Resource(ResourceType.Plant, DefaultMaterial.RED_MUSHROOM.id, 0, 0, this.DefaultMushroom, TCDefaultValues.redMushroomDepositRarity.intValue(), TCDefaultValues.redMushroomDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, new int[]{DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id});
            this.ResourceSequence[this.ResourceCount++] = resource;

            //Brown mushroom
            resource = new Resource(ResourceType.Plant, DefaultMaterial.BROWN_MUSHROOM.id, 0, 0, this.DefaultMushroom, TCDefaultValues.brownMushroomDepositRarity.intValue(), TCDefaultValues.brownMushroomDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, new int[]{DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id});
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        if (this.DefaultGrass > 0)
        {
            //Grass
            resource = new Resource(ResourceType.Grass, DefaultMaterial.LONG_GRASS.id, 1, 0, this.DefaultGrass, TCDefaultValues.longGrassDepositRarity.intValue(), 0, 0, new int[]{DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id});
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        if (this.DefaultDeadBrush > 0)
        {
            //Dead Bush
            resource = new Resource(ResourceType.Grass, DefaultMaterial.DEAD_BUSH.id, 0, 0, this.DefaultDeadBrush, TCDefaultValues.deadBushDepositRarity.intValue(), 0, 0, new int[]{DefaultMaterial.SAND.id});
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        //Pumpkin
        resource = new Resource(ResourceType.Plant, DefaultMaterial.PUMPKIN.id, 0, 0, TCDefaultValues.pumpkinDepositFrequency.intValue(), TCDefaultValues.pumpkinDepositRarity.intValue(), TCDefaultValues.pumpkinDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, new int[]{DefaultMaterial.GRASS.id});
        this.ResourceSequence[this.ResourceCount++] = resource;


        if (this.DefaultReed > 0)
        {
            //Reed
            resource = new Resource(ResourceType.Reed, DefaultMaterial.SUGAR_CANE_BLOCK.id, 0, 0, this.DefaultReed, TCDefaultValues.reedDepositRarity.intValue(), TCDefaultValues.reedDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, new int[]{DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id, DefaultMaterial.SAND.id});
            this.ResourceSequence[this.ResourceCount++] = resource;
        }


        if (this.DefaultCactus > 0)
        {
            //Cactus
            resource = new Resource(ResourceType.Cactus, DefaultMaterial.CACTUS.id, 0, 0, this.DefaultCactus, TCDefaultValues.cactusDepositRarity.intValue(), TCDefaultValues.cactusDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, new int[]{DefaultMaterial.SAND.id});
            this.ResourceSequence[this.ResourceCount++] = resource;
        }
        if( biome == DefaultBiome.JUNGLE || biome == DefaultBiome.JUNGLE_HILLS) // Jungle and Jungle Hills
        {
            resource = new Resource(ResourceType.Vines, 0, 0, 0, TCDefaultValues.vinesFrequency.intValue(), TCDefaultValues.vinesRarity.intValue(), TCDefaultValues.vinesMinAltitude.intValue(), this.worldConfig.WorldHeight, new int[]{DefaultMaterial.VINE.id});
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        //Water source
        resource = new Resource(ResourceType.Liquid, DefaultMaterial.WATER.id, 0, 0, TCDefaultValues.waterSourceDepositFrequency.intValue(), TCDefaultValues.waterSourceDepositRarity.intValue(), TCDefaultValues.waterSourceDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, new int[]{DefaultMaterial.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Lava source
        resource = new Resource(ResourceType.Liquid, DefaultMaterial.LAVA.id, 0, 0, TCDefaultValues.lavaSourceDepositFrequency.intValue(), TCDefaultValues.lavaSourceDepositRarity.intValue(), TCDefaultValues.lavaSourceDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, new int[]{DefaultMaterial.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;


    }

    protected void ReadConfigSettings()
    {
        this.BiomeSize = ReadModSettings(TCDefaultValues.BiomeSize.name(), this.DefaultSize);
        this.BiomeRarity = ReadModSettings(TCDefaultValues.BiomeRarity.name(), this.DefaultRarity);

        this.BiomeColor = ReadModSettings(TCDefaultValues.BiomeColor.name(), this.DefaultColor);

        this.BiomeRivers = ReadModSettings(TCDefaultValues.BiomeRivers.name(), this.DefaultRiver);


        this.IsleInBiome = ReadModSettings(TCDefaultValues.IsleInBiome.name(), this.DefaultIsle);
        this.BiomeIsBorder = ReadModSettings(TCDefaultValues.BiomeIsBorder.name(), this.DefaultBorder);
        this.NotBorderNear = ReadModSettings(TCDefaultValues.NotBorderNear.name(), this.DefaultNotBorderNear);

        this.BiomeTemperature = this.ReadModSettings(TCDefaultValues.BiomeTemperature.name(), this.DefaultBiomeTemperature);
        this.BiomeWetness = this.ReadModSettings(TCDefaultValues.BiomeWetness.name(), this.DefaultBiomeWetness);


        this.evenWaterSourceDistribution = this.ReadModSettings(TCDefaultValues.evenWaterSourceDistribution.name(), TCDefaultValues.evenWaterSourceDistribution.booleanValue());
        this.evenLavaSourceDistribution = this.ReadModSettings(TCDefaultValues.evenLavaSourceDistribution.name(), TCDefaultValues.evenLavaSourceDistribution.booleanValue());

        this.BiomeHeight = this.ReadModSettings(TCDefaultValues.BiomeHeight.name(), this.DefaultBiomeSurface);
        this.BiomeVolatility = this.ReadModSettings(TCDefaultValues.BiomeVolatility.name(), this.DefaultBiomeVolatility);

        this.SurfaceBlock = this.ReadModSettings(TCDefaultValues.SurfaceBlock.name(), this.DefaultSurfaceBlock);
        this.GroundBlock = this.ReadModSettings(TCDefaultValues.GroundBlock.name(), this.DefaultGroundBlock);

        this.disableNotchPonds = this.ReadModSettings(TCDefaultValues.disableNotchPonds.name(), TCDefaultValues.disableNotchPonds.booleanValue());

        this.SkyColor = this.ReadModSettingsColor(TCDefaultValues.SkyColor.name(), TCDefaultValues.SkyColor.stringValue());
        this.WaterColor = this.ReadModSettingsColor(TCDefaultValues.WaterColor.name(), this.DefaultWaterColorMultiplier);
        this.GrassColor = this.ReadModSettingsColor(TCDefaultValues.GrassColor.name(), this.DefaultGrassColor);
        this.FoliageColor = this.ReadModSettingsColor(TCDefaultValues.FoliageColor.name(), this.DefaultFoliageColor);

        this.volatilityRaw1 = ReadModSettings(TCDefaultValues.Volatility1.name(), TCDefaultValues.Volatility1.doubleValue());
        this.volatilityRaw2 = ReadModSettings(TCDefaultValues.Volatility2.name(), TCDefaultValues.Volatility2.doubleValue());
        this.volatilityWeightRaw1 = ReadModSettings(TCDefaultValues.VolatilityWeight1.name(), TCDefaultValues.VolatilityWeight1.doubleValue());
        this.volatilityWeightRaw2 = ReadModSettings(TCDefaultValues.VolatilityWeight2.name(), TCDefaultValues.VolatilityWeight2.doubleValue());
        this.disableNotchHeightControl = ReadModSettings(TCDefaultValues.DisableBiomeHeight.name(), TCDefaultValues.DisableBiomeHeight.booleanValue());
        this.maxAverageHeight = ReadModSettings(TCDefaultValues.MaxAverageHeight.name(), TCDefaultValues.MaxAverageHeight.doubleValue());
        this.maxAverageDepth = ReadModSettings(TCDefaultValues.MaxAverageDepth.name(), TCDefaultValues.MaxAverageDepth.doubleValue());


        this.ReadReplaceSettings();
        this.ReadResourceSettings();
        this.ReadHeightSettings();
    }

    private void ReadHeightSettings()
    {
        this.heightMatrix = new double[this.worldConfig.WorldHeight / 8 + 1];

        ArrayList<String> keys = this.ReadModSettings(TCDefaultValues.CustomHeightControl.name(), TCDefaultValues.CustomHeightControl.StringArrayListValue());
        try
        {
            if (keys.size() != (this.worldConfig.WorldHeight / 8 + 1))
                return;
            for (int i = 0; i < this.worldConfig.WorldHeight / 8 + 1; i++)
                this.heightMatrix[i] = Double.valueOf(keys.get(i));

        } catch (NumberFormatException e)
        {
            System.out.println("Wrong height settings: '" + this.SettingsCache.get(TCDefaultValues.CustomHeightControl.name()) + "'");
        }


    }


    private void ReadReplaceSettings()
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

                    int min_y = 0;
                    int max_y = worldConfig.WorldHeight;

                    int start = blocks[1].indexOf("(");
                    int end = blocks[1].indexOf(")");
                    if (start != -1 && end != -1)
                    {
                        String[] ranges = blocks[1].substring(start + 1, end).split("-");
                        if (ranges.length != 2)
                            continue;

                        min_y = Integer.valueOf(ranges[0]);
                        max_y = Integer.valueOf(ranges[1]);
                        min_y = CheckValue(min_y, 0, worldConfig.WorldHeight);
                        max_y = CheckValue(max_y, 0, worldConfig.WorldHeight, min_y);
                        blocks[1] = blocks[1].substring(0, start);
                    }

                    short blockId;
                    short blockData;
                    short fromBlockId = Short.valueOf(blocks[0]);

                    if (blocks[1].contains("."))
                    {
                        String[] parts = blocks[1].split("\\.");
                        blockId = Short.valueOf(parts[0]); // Block ID
                        blockData = Short.valueOf(parts[1]); // Block data
                    } else
                    {
                        blockId = Short.valueOf(blocks[1]); // Block ID
                        blockData = 0; // Block data
                    }
                    if (this.ReplaceMatrixBlocks[fromBlockId] == null)
                    {
                        this.ReplaceMatrixBlocks[fromBlockId] = new short[worldConfig.WorldHeight];
                        for (int i = 0; i < worldConfig.WorldHeight; i++)
                            this.ReplaceMatrixBlocks[fromBlockId][i] = -1;
                    }
                    for (int y = min_y; y < max_y; y++)
                        this.ReplaceMatrixBlocks[fromBlockId][y] = (short) (blockId << 4 | blockData);
                    ReplaceCount++;

                }

            } catch (NumberFormatException e)
            {
                System.out.println("Wrong replace settings: '" + this.SettingsCache.get("ReplacedBlocks") + "'");
            }

        }


    }

    private void ReadResourceSettings()
    {
        ArrayList<Integer> LineNumbers = new ArrayList<Integer>();

        for (Map.Entry<String, String> entry : this.SettingsCache.entrySet())
        {
            String key = entry.getKey();
            for (ResourceType type : ResourceType.values())
            {
                if (key.startsWith(type.name()))
                {
                    int start = key.indexOf("(");
                    int end = key.indexOf(")");
                    if (start != -1 && end != -1)
                    {
                        Resource res = new Resource(type);
                        res.ReadFromString(key.substring(start + 1, end), this.worldConfig.WorldHeight);

                        if (res.Done)
                        {
                            LineNumbers.add(Integer.valueOf(entry.getValue()));
                            this.ResourceSequence[this.ResourceCount++] = res;
                        }


                    }
                }


            }

        }
        Resource buffer;

        for (int i = 0; i < this.ResourceCount; i++)
        {
            buffer = this.ResourceSequence[i];
            int intBuffer = LineNumbers.get(i);
            int minimal = i;
            for (int t = i; t < this.ResourceCount; t++)
            {
                if (LineNumbers.get(t) < intBuffer)
                {
                    intBuffer = LineNumbers.get(t);
                    minimal = t;
                }
            }
            this.ResourceSequence[i] = this.ResourceSequence[minimal];
            this.ResourceSequence[minimal] = buffer;
            LineNumbers.set(minimal, LineNumbers.get(i));

        }

    }

    protected void WriteConfigSettings() throws IOException
    {
        WriteTitle(this.Name + " biome config");

        this.WriteNewLine();

        WriteComment("Biome size from 0 to GenerationDepth. Show in what zoom level biome will be generated ( see GenerationDepth)");
        WriteComment("Higher numbers = less zoom this biome, lower numbers = more zoom");
        WriteComment("Don`t work on Ocean and River (frozen versions too) biomes until not added as normal biome.");
        WriteValue(TCDefaultValues.BiomeSize.name(), this.BiomeSize);
        this.WriteNewLine();

        WriteComment("Biome rarity from 100 to 1. If this is normal or ice biome - chance for spawn this biome then others.");
        WriteComment("Example for normal biome :");
        WriteComment("  100 rarity mean 1/6 chance than other ( with 6 default normal biomes).");
        WriteComment("  50 rarity mean 1/11 chance than other");
        WriteComment("For isle biome this is chance to spawn isle in good place.");
        WriteComment("Don`t work on Ocean and River (frozen versions too) biomes until not added as normal biome.");
        WriteValue(TCDefaultValues.BiomeRarity.name(), this.BiomeRarity);
        this.WriteNewLine();

        WriteComment("Biome color in hex value. Biome color in /tc map command");
        WriteValue(TCDefaultValues.BiomeColor.name(), this.BiomeColor);
        this.WriteNewLine();

        WriteComment("True if biome can pass rivers.");
        WriteValue(TCDefaultValues.BiomeRivers.name(), this.BiomeRivers);
        this.WriteNewLine();


        WriteComment("Biome name list where this biome will be spawned as isle. Like Mushroom isle in Ocean.  This work only if this biome is in IsleBiomes in world config");
        WriteValue(TCDefaultValues.IsleInBiome.name(), this.IsleInBiome);
        this.WriteNewLine();

        WriteComment("Biome name list where this biome will be border.Like Mushroom isle shore. Use is compared as IsleInBiome");
        WriteValue(TCDefaultValues.BiomeIsBorder.name(), this.BiomeIsBorder);
        this.WriteNewLine();

        WriteComment("Biome name list near border is not applied. ");
        WriteValue(TCDefaultValues.NotBorderNear.name(), this.NotBorderNear);
        this.WriteNewLine();

        WriteComment("Biome temperature. Float value from 0.0 to 1.0");
        WriteValue(TCDefaultValues.BiomeTemperature.name(), this.BiomeTemperature);
        this.WriteNewLine();
        WriteComment("Biome wetness. Float value from 0.0 to 1.0");
        WriteValue(TCDefaultValues.BiomeWetness.name(), this.BiomeWetness);
        this.WriteNewLine();


        WriteTitle("Terrain Generator Variables");
        WriteComment("BiomeHeight mean how much height will be added in terrain generation");
        WriteComment("It is double value from -10.0 to 10.0");
        WriteComment("Value 0.0 equivalent half of map height with all other default settings");
        WriteValue(TCDefaultValues.BiomeHeight.name(), this.BiomeHeight);

        this.WriteNewLine();
        WriteComment("Biome volatility.");
        WriteValue(TCDefaultValues.BiomeVolatility.name(), this.BiomeVolatility);

        WriteNewLine();
        WriteComment("If this value is greater than 0, then it will affect how much, on average, the terrain will rise before leveling off when it begins to increase in elevation.");
        WriteComment("If the value is less than 0, then it will cause the terrain to either increase to a lower height before leveling out or decrease in height if the value is a large enough negative.");
        WriteValue(TCDefaultValues.MaxAverageHeight.name(), this.maxAverageHeight);

        WriteNewLine();
        WriteComment("If this value is greater than 0, then it will affect how much, on average, the terrain (usually at the ottom of the ocean) will fall before leveling off when it begins to decrease in elevation. ");
        WriteComment("If the value is less than 0, then it will cause the terrain to either fall to a lesser depth before leveling out or increase in height if the value is a large enough negative.");
        WriteValue(TCDefaultValues.MaxAverageDepth.name(), this.maxAverageDepth);

        WriteNewLine();
        WriteComment("Another type of noise. This noise is independent from biomes. The larger the values the more chaotic/volatile landscape generation becomes.");
        WriteComment("Setting the values to negative will have the opposite effect and make landscape generation calmer/gentler.");
        WriteValue(TCDefaultValues.Volatility1.name(), this.volatilityRaw1);
        WriteValue(TCDefaultValues.Volatility2.name(), this.volatilityRaw2);

        WriteNewLine();
        WriteComment("Adjust the weight of the corresponding volatility settings. This allows you to change how prevalent you want either of the volatility settings to be in the terrain.");
        WriteValue(TCDefaultValues.VolatilityWeight1.name(), this.volatilityWeightRaw1);
        WriteValue(TCDefaultValues.VolatilityWeight2.name(), this.volatilityWeightRaw2);

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


        this.WriteNewLine();
        WriteComment("Surface block id");
        WriteValue(TCDefaultValues.SurfaceBlock.name(), this.SurfaceBlock);

        this.WriteNewLine();
        WriteComment("Block id from stone to surface, like dirt in plain biome ");
        WriteValue(TCDefaultValues.GroundBlock.name(), this.GroundBlock);

        this.WriteNewLine();
        WriteComment("Replace Variable: BlockIdFrom=BlockIdTo[(minHeight-maxHeight)]");
        WriteComment("Example :");
        WriteComment("  ReplacedBlocks:2=3(100-128),13=20");
        WriteComment("Replace grass block to dirt from 100 to 128 height and replace gravel to glass on all height ");
        WriteModReplaceSettings();
        this.WriteNewLine();

        WriteComment("Disable or enable small lava and water lakes on surface");
        this.WriteValue(TCDefaultValues.disableNotchPonds.name(), this.disableNotchPonds);

        this.WriteTitle("Biome visual settings");
        this.WriteComment("Warning this section will work only for clients with single version of TerrainControl");

        this.WriteNewLine();
        this.WriteComment("Biome sky color");
        this.WriteColorValue(TCDefaultValues.SkyColor.name(), this.SkyColor);

        this.WriteNewLine();
        this.WriteComment("Biome water color multiplier ");
        this.WriteColorValue(TCDefaultValues.WaterColor.name(), this.WaterColor);

        this.WriteNewLine();
        this.WriteComment("Biome grass color");
        this.WriteColorValue(TCDefaultValues.GrassColor.name(), this.GrassColor);

        this.WriteNewLine();
        this.WriteComment("Biome foliage color");
        this.WriteColorValue(TCDefaultValues.FoliageColor.name(), this.FoliageColor);


        this.WriteNewLine();
        this.WriteTitle("Resource queue");
        this.WriteComment("This section control all resources spawning after terrain generation");
        this.WriteComment("So first line is first resource which will be placed. Second line - second resource.");
        this.WriteComment("By default this set to be near notch settings.");
        this.WriteComment("");
        this.WriteComment("Possible resources:");
        this.WriteComment("Dungeon(Frequency,Rarity,MinAltitude,MaxAltitude)");
        this.WriteComment("UnderGroundLake(MinSize,MaxSize,Frequency,Rarity,MinAltitude,MaxAltitude)");
        this.WriteComment("Ore(Block[.BlockData],Size,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.WriteComment("UnderWaterOre(Block,Size,Frequency,Rarity,BlockSource[,BlockSource2,BlockSource3.....])");
        this.WriteComment("CustomObject()");
        this.WriteComment("Tree(Frequency,TreeType,TreeType_Chance[,Additional_TreeType,Additional_TreeType_Chance.....])");
        this.WriteComment("Plant(Block[.BlockData],Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.WriteComment("Grass(Block,BlockData,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.WriteComment("Reed(Block,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.WriteComment("Cactus(Block,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.WriteComment("Liquid(Block,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.WriteComment("AboveWaterRes(Block,Frequency,Rarity)");
        this.WriteComment("Vines(Frequency,Rarity,MinAltitude,MaxAltitude)");
        this.WriteComment("");
        this.WriteComment("Some comments:  ");
        this.WriteComment("Block and BlockSource - can be id or name, Frequency - is count of attempts for place resource");
        this.WriteComment("Rarity - chance for each attempt, Rarity:100 - mean 100% to pass, Rarity:1 - mean 1% to pass");
        this.WriteComment("MinAltitude and MaxAltitude - height limits");
        this.WriteComment("BlockSource - mean where or whereupon resource will be placed ");
        this.WriteComment("CustomObject resource - mean where in queue custom object will be trying to place");
        this.WriteComment("Tree types: ");
        this.WriteComment("     Tree");
        this.WriteComment("     BigTree");
        this.WriteComment("     Forest");
        this.WriteComment("     HugeMushroom");
        this.WriteComment("     SwampTree");
        this.WriteComment("     Taiga1");
        this.WriteComment("     Taiga2");
        this.WriteComment("     JungleTree");
        this.WriteComment("     GroundBush");
        this.WriteComment("TreeType_Chance - similar Rarity. Example:");
        this.WriteComment("  Tree(10,Taiga1,35,Taiga2,100) - plugin trying to 10 attempts, in each attempt he try place Taiga1 ( 35% chance ) if not he place Taiga2 (100% chance)");
        this.WriteComment("Plant resource used for place something like flower, small mushrooms, pumpkins");
        this.WriteComment("Liquid resource is one block water or lava source");
        this.WriteComment("");

        this.WriteResources();

    }

    private void WriteHeightSettings() throws IOException
    {

        String output = Double.toString(this.heightMatrix[0]);
        for (int i = 1; i < this.heightMatrix.length; i++)
            output = output + "," + Double.toString(this.heightMatrix[i]);

        this.WriteValue(TCDefaultValues.CustomHeightControl.name(), output);
    }


    private void WriteModReplaceSettings() throws IOException
    {

        if (this.ReplaceCount == 0)
        {
            this.WriteValue("ReplacedBlocks", "None");
            return;
        }
        String output = "";
        boolean first = true;


        for (int id = 0; id < ReplaceMatrixBlocks.length; id++)
        {
            if (ReplaceMatrixBlocks[id] == null)
                continue;

            int replaceTo = -1;
            int y_start = 0;

            for (int y = 0; y < ReplaceMatrixBlocks[id].length; y++)
            {
                if (ReplaceMatrixBlocks[id][y] == replaceTo)
                    continue;

                if (replaceTo == -1)
                {
                    y_start = y;
                    replaceTo = ReplaceMatrixBlocks[id][y];
                    continue;
                }
                if (!first)
                    output += ",";

                output += id + "=" + (replaceTo >> 4);
                if ((replaceTo & 0xF) > 0)
                    output += "." + (replaceTo & 0xF);
                if (y_start != 0 || y != (ReplaceMatrixBlocks[id].length - 1))
                {
                    output += "(" + y_start + "-" + y + ")";
                }
                first = false;
                replaceTo = -1;
            }
            if (replaceTo != -1)
            {
                if (!first)
                    output += ",";

                output += id + "=" + (replaceTo >> 4);
                if ((replaceTo & 0xF) > 0)
                    output += "." + (replaceTo & 0xF);
                if (y_start != 0)
                {
                    output += "(" + y_start + "-" + (ReplaceMatrixBlocks[id].length - 1) + ")";
                }
                first = false;

            }

        }

        this.WriteValue("ReplacedBlocks", output);
    }

    private void WriteResources() throws IOException
    {
        for (int i = 0; i < this.ResourceCount; i++)
            this.WriteValue(this.ResourceSequence[i].WriteToString());

    }

    protected void CorrectSettings()
    {
        this.BiomeSize = CheckValue(this.BiomeSize, 0, this.worldConfig.GenerationDepth);
        this.BiomeHeight = (float) CheckValue(this.BiomeHeight, -10.0, 10.0);
        this.BiomeRarity = CheckValue(this.BiomeRarity, 1, this.worldConfig.BiomeRarityScale);

        this.BiomeTemperature = CheckValue(this.BiomeTemperature, 0.0F, 1.0F);
        this.BiomeWetness = CheckValue(this.BiomeWetness, 0.0F, 1.0F);

        this.IsleInBiome = CheckValue(this.IsleInBiome, this.worldConfig.CustomBiomes);
        this.BiomeIsBorder = CheckValue(this.BiomeIsBorder, this.worldConfig.CustomBiomes);
        this.NotBorderNear = CheckValue(this.NotBorderNear, this.worldConfig.CustomBiomes);

        this.volatility1 = this.volatilityRaw1 < 0.0D ? 1.0D / (Math.abs(this.volatilityRaw1) + 1.0D) : this.volatilityRaw1 + 1.0D;
        this.volatility2 = this.volatilityRaw2 < 0.0D ? 1.0D / (Math.abs(this.volatilityRaw2) + 1.0D) : this.volatilityRaw2 + 1.0D;

        this.volatilityWeight1 = (this.volatilityWeightRaw1 - 0.5D) * 24.0D;
        this.volatilityWeight2 = (0.5D - this.volatilityWeightRaw2) * 24.0D;

    }

    protected void RenameOldSettings()
    {
        TCDefaultValues[] copyFromWorld = {TCDefaultValues.MaxAverageHeight, TCDefaultValues.MaxAverageDepth, TCDefaultValues.Volatility1, TCDefaultValues.Volatility2, TCDefaultValues.VolatilityWeight1, TCDefaultValues.VolatilityWeight2, TCDefaultValues.DisableBiomeHeight, TCDefaultValues.CustomHeightControl};
        for (TCDefaultValues value : copyFromWorld)
            if (this.worldConfig.SettingsCache.containsKey(value.name()))
                this.SettingsCache.put(value.name(), this.worldConfig.SettingsCache.get(value.name()));

    }


    private int DefaultTrees = 1;
    private int DefaultFlowers = 2;
    private int DefaultGrass = 10;
    private int DefaultDeadBrush = 0;
    private int DefaultMushroom = 0;
    private int DefaultReed = 0;
    private int DefaultCactus = 0;
    private int DefaultClay = 1;
    private float DefaultBiomeSurface = 0.1F;
    private float DefaultBiomeVolatility = 0.3F;
    private byte DefaultSurfaceBlock = (byte) DefaultMaterial.GRASS.id;
    private byte DefaultGroundBlock = (byte) DefaultMaterial.DIRT.id;
    private float DefaultBiomeTemperature = 0.5F;
    private float DefaultBiomeWetness = 0.5F;
    private ArrayList<String> DefaultIsle = new ArrayList<String>();
    private ArrayList<String> DefaultBorder = new ArrayList<String>();
    private ArrayList<String> DefaultNotBorderNear = new ArrayList<String>();
    private boolean DefaultRiver = true;
    private int DefaultSize = 4;
    private int DefaultRarity = 100;
    private String DefaultColor = "0x000000";
    private int DefaultWaterLily = 0;
    private String DefaultWaterColorMultiplier = "0xFFFFFF";
    private String DefaultGrassColor = "0xFFFFFF";
    private String DefaultFoliageColor = "0xFFFFFF";


    private void InitDefaults()
    {
        this.DefaultBiomeSurface = this.Biome.getSurfaceHeight();
        this.DefaultBiomeVolatility = this.Biome.getSurfaceVolatility();
        this.DefaultSurfaceBlock = this.Biome.getSurfaceBlock();
        this.DefaultGroundBlock = this.Biome.getGroundBlock();
        this.DefaultBiomeTemperature = this.Biome.getTemperature();
        this.DefaultBiomeWetness = this.Biome.getWetness();


        switch (this.Biome.getId())
        {
            case 0:
                this.DefaultColor = "0x3333FF";
                break;
            case 1:
            {
                this.DefaultTrees = 0;
                this.DefaultFlowers = 4;
                this.DefaultGrass = 20;
                this.DefaultColor = "0x999900";
                break;
            }
            case 2:
            {
                this.DefaultTrees = 0;
                this.DefaultDeadBrush = 4;
                this.DefaultGrass = 0;
                this.DefaultReed = 50;
                this.DefaultCactus = 10;
                this.DefaultColor = "0xFFCC33";
                break;
            }
            case 3:
                this.DefaultColor = "0x333300";
                break;
            case 4:
            {
                this.DefaultTrees = 10;
                this.DefaultGrass = 15;
                this.DefaultColor = "0x00FF00";
                break;
            }
            case 5:
            {
                this.DefaultTrees = 10;
                this.DefaultGrass = 10;
                this.DefaultColor = "0x007700";
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
                this.DefaultColor = "0x99CC66";
                this.DefaultWaterColorMultiplier = "0xe0ffae";
                this.DefaultGrassColor = "0x7E6E7E";
                this.DefaultFoliageColor = "0x7E6E7E";
                break;
            }
            case 7:
                this.DefaultSize = 8;
                this.DefaultRarity = 95;
                this.DefaultIsle.add(DefaultBiome.SWAMPLAND.Name);
                this.DefaultColor = "0x00CCCC";
            case 8:
            case 9:

                break;
            case 10:
                this.DefaultColor = "0xFFFFFF";
                break;
            case 11:
                this.DefaultColor = "0x66FFFF";
                break;
            case 12:
                this.DefaultColor = "0xCCCCCC";
                break;
            case 13:
                this.DefaultColor = "0xCC9966";
                break;
            case 14:
            {
                this.DefaultSurfaceBlock = (byte) DefaultMaterial.MYCEL.id;
                this.DefaultMushroom = 1;
                this.DefaultGrass = 0;
                this.DefaultFlowers = 0;
                this.DefaultTrees = 0;
                this.DefaultRarity = 1;
                this.DefaultRiver = false;
                this.DefaultSize = 6;
                this.DefaultIsle.add(DefaultBiome.OCEAN.Name);
                this.DefaultColor = "0xFF33CC";
                this.DefaultWaterLily = 1;
                break;
            }
            case 15:
            {
                this.DefaultRiver = false;
                this.DefaultSize = 9;
                this.DefaultBorder.add(DefaultBiome.MUSHROOM_ISLAND.Name);
                this.DefaultColor = "0xFF9999";
                break;
            }
            case 16:
                this.DefaultTrees = 0;
                this.DefaultSize = 8;
                this.DefaultBorder.add(DefaultBiome.OCEAN.Name);
                this.DefaultNotBorderNear.add(DefaultBiome.RIVER.Name);
                this.DefaultNotBorderNear.add(DefaultBiome.SWAMPLAND.Name);
                this.DefaultNotBorderNear.add(DefaultBiome.EXTREME_HILLS.Name);
                this.DefaultNotBorderNear.add(DefaultBiome.MUSHROOM_ISLAND.Name);
                this.DefaultColor = "0xFFFF00";
                break;
            case 17:
                this.DefaultSize = 6;
                this.DefaultRarity = 97;
                this.DefaultIsle.add(DefaultBiome.DESERT.Name);
                this.DefaultTrees = 0;
                this.DefaultDeadBrush = 4;
                this.DefaultGrass = 0;
                this.DefaultReed = 50;
                this.DefaultCactus = 10;
                this.DefaultColor = "0x996600";
                break;
            case 18:
                this.DefaultSize = 6;
                this.DefaultRarity = 97;
                this.DefaultIsle.add(DefaultBiome.FOREST.Name);
                this.DefaultTrees = 10;
                this.DefaultGrass = 15;
                this.DefaultColor = "0x009900";
                break;
            case 19:
                this.DefaultSize = 6;
                this.DefaultRarity = 97;
                this.DefaultIsle.add(DefaultBiome.TAIGA.Name);
                this.DefaultTrees = 10;
                this.DefaultGrass = 10;
                this.DefaultColor = "0x003300";
                break;
            case 20:
                this.DefaultSize = 8;
                this.DefaultBorder.add(DefaultBiome.EXTREME_HILLS.Name);
                this.DefaultColor = "0x666600";
                break;
            case 21:
                this.DefaultTrees = 50;
                this.DefaultGrass = 25;
                this.DefaultFlowers = 4;
                this.DefaultColor = "0xCC6600";
                break;
            case 22:
                this.DefaultTrees = 50;
                this.DefaultGrass = 25;
                this.DefaultFlowers = 4;
                this.DefaultColor = "0x663300";
                this.DefaultIsle.add(DefaultBiome.JUNGLE.Name);
                break;

        }

    }

    public void Serialize(DataOutputStream stream) throws IOException
    {
        WriteStringToStream(stream, this.Name);

        stream.writeInt(this.BiomeSize);
        stream.writeInt(this.BiomeRarity);
        stream.writeBoolean(this.BiomeRivers);

        stream.writeInt(this.IsleInBiome.size());
        for (String biome : this.IsleInBiome)
            WriteStringToStream(stream, biome);

        stream.writeInt(this.BiomeIsBorder.size());
        for (String biome : this.BiomeIsBorder)
            WriteStringToStream(stream, biome);

        stream.writeInt(this.NotBorderNear.size());
        for (String biome : this.NotBorderNear)
            WriteStringToStream(stream, biome);

        stream.writeFloat(this.BiomeTemperature);
        stream.writeFloat(this.BiomeWetness);
        stream.writeInt(this.SkyColor);
        stream.writeInt(this.WaterColor);
        stream.writeInt(this.GrassColor);
        stream.writeInt(this.FoliageColor);

    }

    public BiomeConfig(DataInputStream stream, WorldConfig config, LocalBiome biome) throws IOException
    {
        this.Name = ReadStringFromStream(stream);
        this.Biome = biome;
        this.worldConfig = config;

        this.BiomeSize = stream.readInt();
        this.BiomeRarity = stream.readInt();
        this.BiomeRivers = stream.readBoolean();

        int count = stream.readInt();
        this.IsleInBiome = new ArrayList<String>();
        while (count-- > 0)
            this.IsleInBiome.add(ReadStringFromStream(stream));

        count = stream.readInt();
        this.BiomeIsBorder = new ArrayList<String>();
        while (count-- > 0)
            this.BiomeIsBorder.add(ReadStringFromStream(stream));

        count = stream.readInt();
        this.NotBorderNear = new ArrayList<String>();
        while (count-- > 0)
            this.NotBorderNear.add(ReadStringFromStream(stream));
        this.BiomeTemperature = stream.readFloat();
        this.BiomeWetness = stream.readFloat();

        this.SkyColor = stream.readInt();
        this.WaterColor = stream.readInt();
        this.GrassColor = stream.readInt();
        this.FoliageColor = stream.readInt();

    }
}