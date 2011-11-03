package com.Khorn.TerrainControl.Configuration;

import com.Khorn.TerrainControl.TCDefaultValues;
import com.Khorn.TerrainControl.Util.CustomBiome;
import com.Khorn.TerrainControl.Util.ResourceType;
import net.minecraft.server.BiomeBase;
import net.minecraft.server.Block;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BiomeConfig extends ConfigFile
{

    public HashMap<Integer, Byte> replaceBlocks = new HashMap<Integer, Byte>();
    public HashMap<Integer, Integer> replaceHeightMin = new HashMap<Integer, Integer>();
    public HashMap<Integer, Integer> replaceHeightMax = new HashMap<Integer, Integer>();
    public byte[] ReplaceMatrixBlocks = new byte[256];
    public int[] ReplaceMatrixHeightMin = new int[256];
    public int[] ReplaceMatrixHeightMax = new int[256];


    public int BiomeChance;


    //Surface config
    public float BiomeSurface;
    public float BiomeVolatility;

    public byte SurfaceBlock;
    public byte GroundBlock;

    public boolean evenWaterSourceDistribution;
    public boolean evenLavaSourceDistribution;

    public boolean disableNotchPonds;

    public Resource[] FirstResourceSequence = new Resource[256];
    public Resource[] SecondResourceSequence = new Resource[256];


    public int FirstResourceCount = 0;
    public int SecondResourceCount = 0;


    // Materials

    public int dungeonRarity;
    public int dungeonFrequency;
    public int dungeonMinAltitude;
    public int dungeonMaxAltitude;

    public boolean notchBiomeTrees;
    public int TreeDensity;


    public BiomeBase Biome;
    private WorldConfig worldConfig;


    public BiomeConfig(File settingsDir, BiomeBase biome, WorldConfig config)
    {

        this.Biome = biome;
        worldConfig = config;
        InitDefaults();

        File settingsFile = new File(settingsDir, this.Biome.l + TCDefaultValues.WorldBiomeConfigName.stringValue());

        this.ReadSettingsFile(settingsFile);
        this.ReadConfigSettings();

        this.CorrectSettings();
        if (!settingsFile.exists())
            this.CreateDefaultResources();
        this.WriteSettingsFile(settingsFile);

        BuildReplaceMatrix();


    }

    public BiomeConfig(File settingsDir, String biomeName, int biomeId, WorldConfig config)
    {


        worldConfig = config;
        CustomBiome cBiome = new CustomBiome(biomeId, biomeName);
        this.Biome = cBiome;

        File settingsFile = new File(settingsDir, biomeName + TCDefaultValues.WorldBiomeConfigName.stringValue());
        this.ReadSettingsFile(settingsFile);
        this.ReadConfigSettings();

        this.CorrectSettings();
        if (!settingsFile.exists())
            this.CreateDefaultResources();

        this.WriteSettingsFile(settingsFile);

        BuildReplaceMatrix();
        cBiome.SetBiome(this);


    }

    private void CreateDefaultResources()
    {
        Resource resource;

        //Resource(ResourceType type,int blockId, int blockData, int size,int frequency, int rarity, int minAltitude,int maxAltitude,int[] sourceBlockIds)
        //Dirt
        resource = new Resource(ResourceType.Ore, Block.DIRT.id, 0, TCDefaultValues.dirtDepositSize1.intValue(), TCDefaultValues.dirtDepositFrequency1.intValue(), TCDefaultValues.dirtDepositRarity1.intValue(), TCDefaultValues.dirtDepositMinAltitude1.intValue(), TCDefaultValues.dirtDepositMaxAltitude1.intValue(), new int[]{Block.STONE.id});
        this.FirstResourceSequence[this.FirstResourceCount++] = resource;

        //Gravel
        resource = new Resource(ResourceType.Ore, Block.GRAVEL.id, 0, TCDefaultValues.gravelDepositSize1.intValue(), TCDefaultValues.gravelDepositFrequency1.intValue(), TCDefaultValues.gravelDepositRarity1.intValue(), TCDefaultValues.gravelDepositMinAltitude1.intValue(), TCDefaultValues.gravelDepositMaxAltitude1.intValue(), new int[]{Block.STONE.id});
        this.FirstResourceSequence[this.FirstResourceCount++] = resource;

        //Clay
        resource = new Resource(ResourceType.Ore, Block.CLAY.id, 0, TCDefaultValues.clayDepositSize1.intValue(), TCDefaultValues.clayDepositFrequency1.intValue(), TCDefaultValues.clayDepositRarity1.intValue(), TCDefaultValues.clayDepositMinAltitude1.intValue(), TCDefaultValues.clayDepositMaxAltitude1.intValue(), new int[]{Block.STONE.id});
        this.FirstResourceSequence[this.FirstResourceCount++] = resource;

        //Coal
        resource = new Resource(ResourceType.Ore, Block.COAL_ORE.id, 0, TCDefaultValues.coalDepositSize1.intValue(), TCDefaultValues.coalDepositFrequency1.intValue(), TCDefaultValues.coalDepositRarity1.intValue(), TCDefaultValues.coalDepositMinAltitude1.intValue(), TCDefaultValues.coalDepositMaxAltitude1.intValue(), new int[]{Block.STONE.id});
        this.FirstResourceSequence[this.FirstResourceCount++] = resource;

        //Iron
        resource = new Resource(ResourceType.Ore, Block.IRON_ORE.id, 0, TCDefaultValues.ironDepositSize1.intValue(), TCDefaultValues.ironDepositFrequency1.intValue(), TCDefaultValues.ironDepositRarity1.intValue(), TCDefaultValues.ironDepositMinAltitude1.intValue(), TCDefaultValues.ironDepositMaxAltitude1.intValue(), new int[]{Block.STONE.id});
        this.FirstResourceSequence[this.FirstResourceCount++] = resource;

        //Gold
        resource = new Resource(ResourceType.Ore, Block.GOLD_ORE.id, 0, TCDefaultValues.goldDepositSize1.intValue(), TCDefaultValues.goldDepositFrequency1.intValue(), TCDefaultValues.goldDepositRarity1.intValue(), TCDefaultValues.goldDepositMinAltitude1.intValue(), TCDefaultValues.goldDepositMaxAltitude1.intValue(), new int[]{Block.STONE.id});
        this.FirstResourceSequence[this.FirstResourceCount++] = resource;

        //Redstone
        resource = new Resource(ResourceType.Ore, Block.REDSTONE_ORE.id, 0, TCDefaultValues.redstoneDepositSize1.intValue(), TCDefaultValues.redstoneDepositFrequency1.intValue(), TCDefaultValues.redstoneDepositRarity1.intValue(), TCDefaultValues.redstoneDepositMinAltitude1.intValue(), TCDefaultValues.redstoneDepositMaxAltitude1.intValue(), new int[]{Block.STONE.id});
        this.FirstResourceSequence[this.FirstResourceCount++] = resource;

        //Diamond
        resource = new Resource(ResourceType.Ore, Block.DIAMOND_ORE.id, 0, TCDefaultValues.diamondDepositSize1.intValue(), TCDefaultValues.diamondDepositFrequency1.intValue(), TCDefaultValues.diamondDepositRarity1.intValue(), TCDefaultValues.diamondDepositMinAltitude1.intValue(), TCDefaultValues.diamondDepositMaxAltitude1.intValue(), new int[]{Block.STONE.id});
        this.FirstResourceSequence[this.FirstResourceCount++] = resource;

        //Lapislazuli
        resource = new Resource(ResourceType.Ore, Block.LAPIS_ORE.id, 0, TCDefaultValues.lapislazuliDepositSize1.intValue(), TCDefaultValues.lapislazuliDepositFrequency1.intValue(), TCDefaultValues.lapislazuliDepositRarity1.intValue(), TCDefaultValues.lapislazuliDepositMinAltitude1.intValue(), TCDefaultValues.lapislazuliDepositMaxAltitude1.intValue(), new int[]{Block.STONE.id});
        this.FirstResourceSequence[this.FirstResourceCount++] = resource;


        //Under water sand
        resource = new Resource(ResourceType.UnderWaterOre, Block.SAND.id, 0, TCDefaultValues.waterSandDepositSize.intValue(), TCDefaultValues.waterSandDepositFrequency.intValue(), TCDefaultValues.waterSandDepositRarity.intValue(), 0, 0, new int[]{Block.DIRT.id, Block.GRASS.id});
        this.SecondResourceSequence[this.SecondResourceCount++] = resource;

        //Under water clay
        if (this.DefaultClay > 0)
        {
            resource = new Resource(ResourceType.UnderWaterOre, Block.CLAY.id, 0, TCDefaultValues.waterClayDepositSize.intValue(), this.DefaultClay, TCDefaultValues.waterClayDepositRarity.intValue(), 0, 0, new int[]{Block.DIRT.id, Block.CLAY.id});
            this.SecondResourceSequence[this.SecondResourceCount++] = resource;
        }

        if (this.DefaultFlowers > 0)
        {
            //Red flower
            resource = new Resource(ResourceType.Plant, Block.RED_ROSE.id, 0, 0, this.DefaultFlowers, TCDefaultValues.roseDepositRarity.intValue(), TCDefaultValues.roseDepositMinAltitude.intValue(), TCDefaultValues.roseDepositMaxAltitude.intValue(), new int[]{Block.GRASS.id, Block.DIRT.id, Block.SOIL.id});
            this.SecondResourceSequence[this.SecondResourceCount++] = resource;

            //Yellow flower
            resource = new Resource(ResourceType.Plant, Block.YELLOW_FLOWER.id, 0, 0, this.DefaultFlowers, TCDefaultValues.flowerDepositRarity.intValue(), TCDefaultValues.flowerDepositMinAltitude.intValue(), TCDefaultValues.flowerDepositMaxAltitude.intValue(), new int[]{Block.GRASS.id, Block.DIRT.id, Block.SOIL.id});
            this.SecondResourceSequence[this.SecondResourceCount++] = resource;
        }

        if (this.DefaultMushroom > 0)
        {
            //Red mushroom
            resource = new Resource(ResourceType.Plant, Block.RED_MUSHROOM.id, 0, 0, this.DefaultMushroom, TCDefaultValues.redMushroomDepositRarity.intValue(), TCDefaultValues.redMushroomDepositMinAltitude.intValue(), TCDefaultValues.redMushroomDepositMaxAltitude.intValue(), new int[]{Block.GRASS.id, Block.DIRT.id});
            this.SecondResourceSequence[this.SecondResourceCount++] = resource;

            //Brown mushroom
            resource = new Resource(ResourceType.Plant, Block.BROWN_MUSHROOM.id, 0, 0, this.DefaultMushroom, TCDefaultValues.brownMushroomDepositRarity.intValue(), TCDefaultValues.brownMushroomDepositMinAltitude.intValue(), TCDefaultValues.brownMushroomDepositMaxAltitude.intValue(), new int[]{Block.GRASS.id, Block.DIRT.id});
            this.SecondResourceSequence[this.SecondResourceCount++] = resource;
        }

        if (this.DefaultGrass > 0)
        {
            //Grass
            resource = new Resource(ResourceType.Grass, Block.LONG_GRASS.id, 1, 0, this.DefaultGrass, TCDefaultValues.longGrassDepositRarity.intValue(), TCDefaultValues.longGrassDepositMinAltitude.intValue(), TCDefaultValues.longGrassDepositMaxAltitude.intValue(), new int[]{Block.GRASS.id, Block.DIRT.id});
            this.SecondResourceSequence[this.SecondResourceCount++] = resource;
        }

        if (this.DefaultDeadBrush > 0)
        {
            //Dead Bush
            resource = new Resource(ResourceType.Grass, Block.DEAD_BUSH.id, 0, 0, this.DefaultDeadBrush, TCDefaultValues.deadBushDepositRarity.intValue(), TCDefaultValues.deadBushDepositMinAltitude.intValue(), TCDefaultValues.deadBushDepositMaxAltitude.intValue(), new int[]{Block.SAND.id});
            this.SecondResourceSequence[this.SecondResourceCount++] = resource;
        }

        //Pumpkin
        resource = new Resource(ResourceType.Plant, Block.PUMPKIN.id, 0, 0, TCDefaultValues.pumpkinDepositFrequency.intValue(), TCDefaultValues.pumpkinDepositRarity.intValue(), TCDefaultValues.pumpkinDepositMinAltitude.intValue(), TCDefaultValues.pumpkinDepositMaxAltitude.intValue(), new int[]{Block.GRASS.id});
        this.SecondResourceSequence[this.SecondResourceCount++] = resource;


        if (this.DefaultReed > 0)
        {
            //Reed
            resource = new Resource(ResourceType.Reed, Block.SUGAR_CANE_BLOCK.id, 0, 0, this.DefaultReed, TCDefaultValues.reedDepositRarity.intValue(), TCDefaultValues.reedDepositMinAltitude.intValue(), TCDefaultValues.reedDepositMaxAltitude.intValue(), new int[]{Block.GRASS.id, Block.DIRT.id, Block.SAND.id});
            this.SecondResourceSequence[this.SecondResourceCount++] = resource;
        }


        if (this.DefaultCactus > 0)
        {
            //Cactus
            resource = new Resource(ResourceType.Cactus, Block.CACTUS.id, 0, 0, this.DefaultCactus, TCDefaultValues.cactusDepositRarity.intValue(), TCDefaultValues.cactusDepositMinAltitude.intValue(), TCDefaultValues.cactusDepositMaxAltitude.intValue(), new int[]{Block.SAND.id});
            this.SecondResourceSequence[this.SecondResourceCount++] = resource;
        }

        //Water source
        resource = new Resource(ResourceType.Liquid, Block.WATER.id, 0, 0, TCDefaultValues.waterSourceDepositFrequency.intValue(), TCDefaultValues.waterSourceDepositRarity.intValue(), TCDefaultValues.waterSourceDepositMinAltitude.intValue(), TCDefaultValues.waterSourceDepositMaxAltitude.intValue(), new int[]{Block.STONE.id});
        this.SecondResourceSequence[this.SecondResourceCount++] = resource;

        //Lava source
        resource = new Resource(ResourceType.Liquid, Block.LAVA.id, 0, 0, TCDefaultValues.lavaSourceDepositFrequency.intValue(), TCDefaultValues.lavaSourceDepositRarity.intValue(), TCDefaultValues.lavaSourceDepositMinAltitude.intValue(), TCDefaultValues.lavaSourceDepositMaxAltitude.intValue(), new int[]{Block.STONE.id});
        this.SecondResourceSequence[this.SecondResourceCount++] = resource;


    }

    protected void ReadConfigSettings()
    {


        this.BiomeChance = ReadModSettings(TCDefaultValues.biomeChance.name(), this.DefaultBiomeChance);

        this.evenWaterSourceDistribution = this.ReadModSettings(TCDefaultValues.evenWaterSourceDistribution.name(), TCDefaultValues.evenWaterSourceDistribution.booleanValue());
        this.evenLavaSourceDistribution = this.ReadModSettings(TCDefaultValues.evenLavaSourceDistribution.name(), TCDefaultValues.evenLavaSourceDistribution.booleanValue());

        this.BiomeSurface = this.ReadModSettings(TCDefaultValues.BiomeSurfaceAdd.name(), this.DefaultBiomeSurface);
        this.BiomeVolatility = this.ReadModSettings(TCDefaultValues.BiomeVolatilityAdd.name(), this.DefaultBiomeVolatility);

        this.SurfaceBlock = this.ReadModSettings(TCDefaultValues.SurfaceBlock.name(), this.DefaultSurfaceBlock);
        this.GroundBlock = this.ReadModSettings(TCDefaultValues.GroundBlock.name(), this.DefaultGroundBlock);

        this.disableNotchPonds = this.ReadModSettings(TCDefaultValues.disableNotchPonds.name(), TCDefaultValues.disableNotchPonds.booleanValue());

        this.notchBiomeTrees = this.ReadModSettings(TCDefaultValues.notchBiomeTrees.name(), TCDefaultValues.notchBiomeTrees.booleanValue());
        this.TreeDensity = this.ReadModSettings(TCDefaultValues.TreeDensity.name(), this.DefaultTrees);

        this.dungeonRarity = this.ReadModSettings(TCDefaultValues.dungeonRarity.name(), TCDefaultValues.dungeonRarity.intValue());
        this.dungeonFrequency = this.ReadModSettings(TCDefaultValues.dungeonFrequency.name(), TCDefaultValues.dungeonFrequency.intValue());
        this.dungeonMinAltitude = this.ReadModSettings(TCDefaultValues.dungeonMinAltitude.name(), TCDefaultValues.dungeonMinAltitude.intValue());
        this.dungeonMaxAltitude = this.ReadModSettings(TCDefaultValues.dungeonMaxAltitude.name(), TCDefaultValues.dungeonMaxAltitude.intValue());


        this.ReadModReplaceSettings();
        this.ReadResourceSettings();


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
                        this.replaceBlocks.put(Integer.valueOf(blocks[0]), Byte.valueOf(blocks[1].substring(0, start)));
                        continue;


                    }
                    this.replaceHeightMin.put(Integer.valueOf(blocks[0]), 0);
                    this.replaceHeightMax.put(Integer.valueOf(blocks[0]), 128);
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
        for (int i = 0; i < this.ReplaceMatrixBlocks.length; i++)
        {
            if (this.replaceBlocks.containsKey(i))
            {
                this.ReplaceMatrixBlocks[i] = this.replaceBlocks.get(i);
                this.ReplaceMatrixHeightMin[i] = this.replaceHeightMin.get(i);
                this.ReplaceMatrixHeightMax[i] = this.replaceHeightMax.get(i);
            } else
                this.ReplaceMatrixBlocks[i] = (byte) i;

        }
    }

    private void ReadResourceSettings()
    {
        ArrayList<Integer> first = new ArrayList<Integer>();
        ArrayList<Integer> second = new ArrayList<Integer>();

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
                        res.ReadFromString(key.substring(start + 1, end));

                        if (res.Done)
                            if (res.First)
                            {
                                first.add(Integer.valueOf(entry.getValue()));
                                this.FirstResourceSequence[this.FirstResourceCount++] = res;
                            } else
                            {
                                second.add(Integer.valueOf(entry.getValue()));
                                this.SecondResourceSequence[this.SecondResourceCount++] = res;
                            }

                    }
                }


            }

        }
        Resource buffer;

        for (int i = 0; i < this.FirstResourceCount; i++)
        {
            buffer = this.FirstResourceSequence[i];
            int intBuffer = first.get(i);
            int minimal = i;
            for (int t = i; t < this.FirstResourceCount; t++)
            {
                if (first.get(t) < intBuffer)
                {
                    intBuffer = first.get(t);
                    minimal = t;
                }
            }
            this.FirstResourceSequence[i] = this.FirstResourceSequence[minimal];
            this.FirstResourceSequence[minimal] = buffer;
            first.set(minimal, first.get(i));

        }

        for (int i = 0; i < this.SecondResourceCount; i++)
        {
            buffer = this.SecondResourceSequence[i];
            int intBuffer = second.get(i);
            int minimal = i;
            for (int t = i; t < this.SecondResourceCount; t++)
            {
                if (second.get(t) < intBuffer)
                {
                    intBuffer = second.get(t);
                    minimal = t;
                }
            }
            this.SecondResourceSequence[i] = this.SecondResourceSequence[minimal];
            this.SecondResourceSequence[minimal] = buffer;
            second.set(minimal, second.get(i));

        }

    }

    protected void WriteConfigSettings() throws IOException
    {
        WriteModTitleSettings(this.Biome.l + " biome config");

        WriteModSettings(TCDefaultValues.biomeChance.name(), this.BiomeChance);

        WriteModSettings(TCDefaultValues.BiomeSurfaceAdd.name(), this.BiomeSurface);
        WriteModSettings(TCDefaultValues.BiomeVolatilityAdd.name(), this.BiomeVolatility);

        WriteModSettings(TCDefaultValues.SurfaceBlock.name(), this.SurfaceBlock);
        WriteModSettings(TCDefaultValues.GroundBlock.name(), this.GroundBlock);


        WriteModTitleSettings("Replace Variable: BlockIdFrom=BlockIdTo(minHeight-maxHeight)");
        WriteModReplaceSettings();


        this.WriteModTitleSettings("Tree Variables");
        this.WriteModSettings(TCDefaultValues.notchBiomeTrees.name(), this.notchBiomeTrees);
        this.WriteModSettings(TCDefaultValues.TreeDensity.name(), this.TreeDensity);

        this.WriteModTitleSettings("ResourceSection");
        this.WriteModTitleSettings("Ore(BlockId,BlockIdSource,Size,Frequency,Rarity,MinAltitude,MaxAltitude");
        this.WriteModTitleSettings("UnderWaterOre(BlockId,BlockIdSource,Size,Frequency,Rarity,MinAltitude,MaxAltitude");
        this.WriteModTitleSettings("Plant(BlockId,BlockIdSource,Frequency,Rarity,MinAltitude,MaxAltitude");
        this.WriteResources();

        this.WriteModTitleSettings("Start Deposit Variables :");
        this.WriteModTitleSettings("Above Ground Variables");

        this.WriteModSettings(TCDefaultValues.disableNotchPonds.name(), this.disableNotchPonds);

        this.WriteModTitleSettings("Below Ground Variables");
        this.WriteModSettings(TCDefaultValues.dungeonRarity.name(), this.dungeonRarity);
        this.WriteModSettings(TCDefaultValues.dungeonFrequency.name(), this.dungeonFrequency);
        this.WriteModSettings(TCDefaultValues.dungeonMinAltitude.name(), this.dungeonMinAltitude);
        this.WriteModSettings(TCDefaultValues.dungeonMaxAltitude.name(), this.dungeonMaxAltitude);
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
            int min = this.replaceHeightMin.get(me.getKey());
            int max = this.replaceHeightMax.get(me.getKey());
            if (min != 0 || max != 128)
                output += "(" + min + "-" + max + ")";

            if (i.hasNext())
                output += ",";
        }

        this.WriteModSettings("ReplacedBlocks", output);
    }

    private void WriteResources() throws IOException
    {
        for (int i = 0; i < this.FirstResourceCount; i++)
            this.WriteModSettings(this.FirstResourceSequence[i].WriteToString());
        for (int i = 0; i < this.SecondResourceCount; i++)
            this.WriteModSettings(this.SecondResourceSequence[i].WriteToString());

    }

    protected void CorrectSettings()
    {
        this.BiomeChance = CheckValue(this.BiomeChance, 0, 20);

        this.dungeonRarity = CheckValue(this.dungeonRarity, 0, 100);
        this.dungeonFrequency = CheckValue(this.dungeonFrequency, 0, 200);
        this.dungeonMinAltitude = CheckValue(this.dungeonMinAltitude, 0, this.worldConfig.ChunkMaxY - 1);
        this.dungeonMaxAltitude = CheckValue(this.dungeonMaxAltitude, 1, this.worldConfig.ChunkMaxY, this.dungeonMinAltitude);


    }


    private int DefaultTrees = 0;
    private int DefaultFlowers = 2;
    private int DefaultGrass = 1;
    private int DefaultDeadBrush = 0;
    private int DefaultMushroom = 0;
    private int DefaultReed = 0;
    private int DefaultCactus = 0;
    private int DefaultClay = 1;
    private float DefaultBiomeSurface = 0.1F;
    private float DefaultBiomeVolatility = 0.3F;
    private byte DefaultSurfaceBlock = (byte) Block.GRASS.id;
    private byte DefaultGroundBlock = (byte) Block.DIRT.id;
    private int DefaultBiomeChance = 1;


    private void InitDefaults()
    {
        this.DefaultBiomeSurface = this.Biome.q;
        this.DefaultBiomeVolatility = this.Biome.r;
        this.DefaultSurfaceBlock = this.Biome.n;
        this.DefaultGroundBlock = this.Biome.o;

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
            case 7:
            case 8:
            case 9:
            case 0:
            {
                this.DefaultBiomeChance = 0;
                break;
            }

        }

    }

}
