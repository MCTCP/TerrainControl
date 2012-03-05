package com.Khorn.TerrainControl.Configuration;

import com.Khorn.TerrainControl.DefaultBiome;
import com.Khorn.TerrainControl.DefaultMaterial;
import com.Khorn.TerrainControl.Generator.ResourceGens.ResourceType;
import com.Khorn.TerrainControl.Generator.ResourceGens.TreeType;

import com.Khorn.TerrainControl.LocalBiome;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BiomeConfig extends ConfigFile
{
    public HashMap<Integer, int[]> replaceBlocks = new HashMap<Integer, int[]>();
    public HashMap<Integer, Integer> replaceHeightMin = new HashMap<Integer, Integer>();
    public HashMap<Integer, Integer> replaceHeightMax = new HashMap<Integer, Integer>();
    public int[][] ReplaceMatrixBlocks = new int[256][2];
    public int[] ReplaceMatrixHeightMin = new int[256];
    public int[] ReplaceMatrixHeightMax = new int[256];


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


    public int ResourceCount = 0;


    public LocalBiome Biome;
    private WorldConfig worldConfig;
    public String Name;


    public BiomeConfig(File settingsDir, LocalBiome biome, WorldConfig config, boolean checkOnly)
    {

        this.Biome = biome;
        this.Name = biome.getName();
        worldConfig = config;
        if (!checkOnly)
            InitDefaults();

        File settingsFile = new File(settingsDir, this.Name + TCDefaultValues.WorldBiomeConfigName.stringValue());

        this.ReadSettingsFile(settingsFile);
        this.ReadConfigSettings();

        this.CorrectSettings();
        if (!settingsFile.exists())
            this.CreateDefaultResources();
        this.WriteSettingsFile(settingsFile);

        BuildReplaceMatrix();

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
        resource = new Resource(ResourceType.Ore, DefaultMaterial.IRON_ORE.id, 0, TCDefaultValues.ironDepositSize.intValue(), TCDefaultValues.ironDepositFrequency.intValue(), TCDefaultValues.ironDepositRarity.intValue(), TCDefaultValues.ironDepositMinAltitude.intValue(), this.worldConfig.WorldHeight/2, new int[]{DefaultMaterial.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Gold
        resource = new Resource(ResourceType.Ore, DefaultMaterial.GOLD_ORE.id, 0, TCDefaultValues.goldDepositSize.intValue(), TCDefaultValues.goldDepositFrequency.intValue(), TCDefaultValues.goldDepositRarity.intValue(), TCDefaultValues.goldDepositMinAltitude.intValue(), this.worldConfig.WorldHeight/4, new int[]{DefaultMaterial.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Redstone
        resource = new Resource(ResourceType.Ore, DefaultMaterial.REDSTONE_ORE.id, 0, TCDefaultValues.redstoneDepositSize.intValue(), TCDefaultValues.redstoneDepositFrequency.intValue(), TCDefaultValues.redstoneDepositRarity.intValue(), TCDefaultValues.redstoneDepositMinAltitude.intValue(), this.worldConfig.WorldHeight/8, new int[]{DefaultMaterial.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Diamond
        resource = new Resource(ResourceType.Ore, DefaultMaterial.DIAMOND_ORE.id, 0, TCDefaultValues.diamondDepositSize.intValue(), TCDefaultValues.diamondDepositFrequency.intValue(), TCDefaultValues.diamondDepositRarity.intValue(), TCDefaultValues.diamondDepositMinAltitude.intValue(), this.worldConfig.WorldHeight/8, new int[]{DefaultMaterial.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Lapislazuli
        resource = new Resource(ResourceType.Ore, DefaultMaterial.LAPIS_ORE.id, 0, TCDefaultValues.lapislazuliDepositSize.intValue(), TCDefaultValues.lapislazuliDepositFrequency.intValue(), TCDefaultValues.lapislazuliDepositRarity.intValue(), TCDefaultValues.lapislazuliDepositMinAltitude.intValue(), this.worldConfig.WorldHeight/8, new int[]{DefaultMaterial.STONE.id});
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

        switch (this.Biome.getId())
        {
            case 0: // Ocean - default
            case 3: // BigHills - default
            case 7: // River - default
            case 20: // SmallHills
                resource = new Resource(ResourceType.Tree, this.DefaultTrees, new TreeType[]{TreeType.BigTree, TreeType.Tree}, new int[]{1, 9});
                this.ResourceSequence[this.ResourceCount++] = resource;
                break;
            case 1: // Plains - no tree
            case 2: // Desert - no tree
            case 17: //HillsDesert
                break;
            case 18: // HillsForest
            case 4: // Forest - forest
                resource = new Resource(ResourceType.Tree, this.DefaultTrees, new TreeType[]{TreeType.Forest, TreeType.BigTree, TreeType.Tree}, new int[]{20, 10, 100});
                this.ResourceSequence[this.ResourceCount++] = resource;
                break;
            case 19: //HillsTaiga
            case 5: // Taiga - taiga
                resource = new Resource(ResourceType.Tree, this.DefaultTrees, new TreeType[]{TreeType.Taiga1, TreeType.Taiga2}, new int[]{35, 100});
                this.ResourceSequence[this.ResourceCount++] = resource;
                break;
            case 6: // Swamp - swamp
                resource = new Resource(ResourceType.Tree, this.DefaultTrees, new TreeType[]{TreeType.SwampTree}, new int[]{100});
                this.ResourceSequence[this.ResourceCount++] = resource;
                break;
            case 14: // Mushroom island
                resource = new Resource(ResourceType.Tree, this.DefaultTrees, new TreeType[]{TreeType.HugeMushroom}, new int[]{100});
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

        this.ReadReplaceSettings();
        this.ReadResourceSettings();
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
                        int[] block = blockIdAndDataFromString(blocks[1].substring(0, start));
                        this.replaceBlocks.put(Integer.valueOf(blocks[0]), block);
                        continue;


                    }
                    this.replaceHeightMin.put(Integer.valueOf(blocks[0]), 0);
                    this.replaceHeightMax.put(Integer.valueOf(blocks[0]), 128);

                    int[] block = blockIdAndDataFromString(blocks[1]);
                    this.replaceBlocks.put(Integer.valueOf(blocks[0]), block);
                }

            } catch (NumberFormatException e)
            {
                System.out.println("Wrong replace settings: '" + this.SettingsCache.get("ReplacedBlocks") + "'");
            }

        }


    }

    private int[] blockIdAndDataFromString(String input)
    {
        int[] block = new int[2]; // [block ID, block data]

        if (input.contains("."))
        {
            String[] parts = input.split("\\.");
            block[0] = Integer.valueOf(parts[0]); // Block ID
            block[1] = Integer.valueOf(parts[1]); // Block data
        } else
        {
            block[0] = Integer.valueOf(input); // Block ID
            block[1] = 0; // Block data
        }

        return block;
    }

    private void BuildReplaceMatrix()
    {
        for (int i = 0; i < this.ReplaceMatrixBlocks.length; i++)
        {
            if (this.replaceBlocks.containsKey(i))
            {
                this.ReplaceMatrixBlocks[i] = replaceBlocks.get(i);
                this.ReplaceMatrixHeightMin[i] = this.replaceHeightMin.get(i);
                this.ReplaceMatrixHeightMax[i] = this.replaceHeightMax.get(i);
            } else
            {
                int[] block = {i, 0};
                this.ReplaceMatrixBlocks[i] = block;
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
                        res.ReadFromString(key.substring(start + 1, end),this.worldConfig.WorldHeight);

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


        this.WriteNewLine();
        WriteComment("BiomeHeight mean how much height will be added in terrain generation");
        WriteComment("It is double value from -10.0 to 10.0");
        WriteComment("Each 1.0 add or remove about 16 blocks height from 64 height with all other values set default.");
        WriteComment("So 0.0 = 64 height, -1.0 = 48 height. Terrain settings from world config may affect how much this works.");
        WriteValue(TCDefaultValues.BiomeHeight.name(), this.BiomeHeight);

        this.WriteNewLine();
        WriteComment("BiomeVolatility similar BiomeHeight, but it adds volatility. Extreme Hills biome made by it.");
        WriteValue(TCDefaultValues.BiomeVolatility.name(), this.BiomeVolatility);

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
        this.WriteComment("AboveWaterRes(Block,Frequency,Rarity,MinAltitude)");
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
        this.WriteComment("TreeType_Chance - similar Rarity. Example:");
        this.WriteComment("  Tree(10,Taiga1,35,Taiga2,100) - plugin trying to 10 attempts, in each attempt he try place Taiga1 ( 35% chance ) if not he place Taiga2 (100% chance)");
        this.WriteComment("Plant resource used for place something like flower, small mushrooms, pumpkins");
        this.WriteComment("Liquid resource is one block water or lava source");
        this.WriteComment("");

        this.WriteResources();

    }


    private void WriteModReplaceSettings() throws IOException
    {

        if (this.replaceBlocks.size() == 0)
        {
            this.WriteValue("ReplacedBlocks", "None");
            return;
        }
        String output = "", value;
        Iterator<Map.Entry<Integer, int[]>> i = this.replaceBlocks.entrySet().iterator();
        while (i.hasNext())
        {
            Map.Entry<Integer, int[]> me = i.next();

            value = String.valueOf(me.getValue()[0]);
            if (me.getValue()[1] > 0)
            {
                value += "." + String.valueOf(me.getValue()[1]);
            }

            output += me.getKey().toString() + "=" + value;
            int min = this.replaceHeightMin.get(me.getKey());
            int max = this.replaceHeightMax.get(me.getKey());
            if (min != 0 || max != 128)
                output += "(" + min + "-" + max + ")";

            if (i.hasNext())
                output += ",";
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


    }

    protected void RenameOldSettings()
    {

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

        }

    }

    public void Serialize(DataOutputStream stream) throws IOException
    {
        WriteStringToStream(stream,this.Name);

        stream.writeInt(this.BiomeSize);
        stream.writeInt(this.BiomeRarity);
        stream.writeBoolean(this.BiomeRivers);

        stream.writeInt(this.IsleInBiome.size());
        for (String biome : this.IsleInBiome)
            WriteStringToStream(stream,biome);

        stream.writeInt(this.BiomeIsBorder.size());
        for (String biome : this.BiomeIsBorder)
            WriteStringToStream(stream,biome);

        stream.writeInt(this.NotBorderNear.size());
        for (String biome : this.NotBorderNear)
            WriteStringToStream(stream,biome);

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