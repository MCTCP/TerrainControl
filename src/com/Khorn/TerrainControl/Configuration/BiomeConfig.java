package com.Khorn.TerrainControl.Configuration;

import com.Khorn.TerrainControl.Generator.ResourceGens.ResourceType;
import com.Khorn.TerrainControl.Generator.ResourceGens.TreeType;
import com.Khorn.TerrainControl.TCDefaultValues;
import com.Khorn.TerrainControl.Util.CustomBiome;
import net.minecraft.server.BiomeBase;
import net.minecraft.server.Block;

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

    public Resource[] ResourceSequence = new Resource[256];


    public int ResourceCount = 0;


    public BiomeBase Biome;
    private WorldConfig worldConfig;
    public String Name;


    public BiomeConfig(File settingsDir, BiomeBase biome, WorldConfig config)
    {

        this.Biome = biome;
        this.Name = biome.r;
        worldConfig = config;
        InitDefaults();

        File settingsFile = new File(settingsDir, this.Biome.r + TCDefaultValues.WorldBiomeConfigName.stringValue());

        this.ReadSettingsFile(settingsFile);
        this.ReadConfigSettings();

        this.CorrectSettings();
        if (!settingsFile.exists())
            this.CreateDefaultResources();
        this.WriteSettingsFile(settingsFile);

        BuildReplaceMatrix();

        if (this.Biome.F > WorldConfig.DefaultBiomesCount)
            ((CustomBiome) this.Biome).SetBiome(this);


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
        resource = new Resource(ResourceType.Dungeon, 0, 0, 0, TCDefaultValues.dungeonFrequency.intValue(), TCDefaultValues.dungeonRarity.intValue(), TCDefaultValues.dungeonMinAltitude.intValue(), TCDefaultValues.dungeonMaxAltitude.intValue(), new int[]{Block.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Resource(ResourceType type,int blockId, int blockData, int size,int frequency, int rarity, int minAltitude,int maxAltitude,int[] sourceBlockIds)
        //Dirt
        resource = new Resource(ResourceType.Ore, Block.DIRT.id, 0, TCDefaultValues.dirtDepositSize.intValue(), TCDefaultValues.dirtDepositFrequency.intValue(), TCDefaultValues.dirtDepositRarity.intValue(), TCDefaultValues.dirtDepositMinAltitude.intValue(), TCDefaultValues.dirtDepositMaxAltitude.intValue(), new int[]{Block.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Gravel
        resource = new Resource(ResourceType.Ore, Block.GRAVEL.id, 0, TCDefaultValues.gravelDepositSize.intValue(), TCDefaultValues.gravelDepositFrequency.intValue(), TCDefaultValues.gravelDepositRarity.intValue(), TCDefaultValues.gravelDepositMinAltitude.intValue(), TCDefaultValues.gravelDepositMaxAltitude.intValue(), new int[]{Block.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Clay
        resource = new Resource(ResourceType.Ore, Block.CLAY.id, 0, TCDefaultValues.clayDepositSize.intValue(), TCDefaultValues.clayDepositFrequency.intValue(), TCDefaultValues.clayDepositRarity.intValue(), TCDefaultValues.clayDepositMinAltitude.intValue(), TCDefaultValues.clayDepositMaxAltitude.intValue(), new int[]{Block.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Coal
        resource = new Resource(ResourceType.Ore, Block.COAL_ORE.id, 0, TCDefaultValues.coalDepositSize.intValue(), TCDefaultValues.coalDepositFrequency.intValue(), TCDefaultValues.coalDepositRarity.intValue(), TCDefaultValues.coalDepositMinAltitude.intValue(), TCDefaultValues.coalDepositMaxAltitude.intValue(), new int[]{Block.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Iron
        resource = new Resource(ResourceType.Ore, Block.IRON_ORE.id, 0, TCDefaultValues.ironDepositSize.intValue(), TCDefaultValues.ironDepositFrequency.intValue(), TCDefaultValues.ironDepositRarity.intValue(), TCDefaultValues.ironDepositMinAltitude.intValue(), TCDefaultValues.ironDepositMaxAltitude.intValue(), new int[]{Block.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Gold
        resource = new Resource(ResourceType.Ore, Block.GOLD_ORE.id, 0, TCDefaultValues.goldDepositSize.intValue(), TCDefaultValues.goldDepositFrequency.intValue(), TCDefaultValues.goldDepositRarity.intValue(), TCDefaultValues.goldDepositMinAltitude.intValue(), TCDefaultValues.goldDepositMaxAltitude.intValue(), new int[]{Block.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Redstone
        resource = new Resource(ResourceType.Ore, Block.REDSTONE_ORE.id, 0, TCDefaultValues.redstoneDepositSize.intValue(), TCDefaultValues.redstoneDepositFrequency.intValue(), TCDefaultValues.redstoneDepositRarity.intValue(), TCDefaultValues.redstoneDepositMinAltitude.intValue(), TCDefaultValues.redstoneDepositMaxAltitude.intValue(), new int[]{Block.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Diamond
        resource = new Resource(ResourceType.Ore, Block.DIAMOND_ORE.id, 0, TCDefaultValues.diamondDepositSize.intValue(), TCDefaultValues.diamondDepositFrequency.intValue(), TCDefaultValues.diamondDepositRarity.intValue(), TCDefaultValues.diamondDepositMinAltitude.intValue(), TCDefaultValues.diamondDepositMaxAltitude.intValue(), new int[]{Block.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Lapislazuli
        resource = new Resource(ResourceType.Ore, Block.LAPIS_ORE.id, 0, TCDefaultValues.lapislazuliDepositSize.intValue(), TCDefaultValues.lapislazuliDepositFrequency.intValue(), TCDefaultValues.lapislazuliDepositRarity.intValue(), TCDefaultValues.lapislazuliDepositMinAltitude.intValue(), TCDefaultValues.lapislazuliDepositMaxAltitude.intValue(), new int[]{Block.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;


        //Under water sand
        resource = new Resource(ResourceType.UnderWaterOre, Block.SAND.id, 0, TCDefaultValues.waterSandDepositSize.intValue(), TCDefaultValues.waterSandDepositFrequency.intValue(), TCDefaultValues.waterSandDepositRarity.intValue(), 0, 0, new int[]{Block.DIRT.id, Block.GRASS.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Under water clay
        if (this.DefaultClay > 0)
        {
            resource = new Resource(ResourceType.UnderWaterOre, Block.CLAY.id, 0, TCDefaultValues.waterClayDepositSize.intValue(), this.DefaultClay, TCDefaultValues.waterClayDepositRarity.intValue(), 0, 0, new int[]{Block.DIRT.id, Block.CLAY.id});
            this.ResourceSequence[this.ResourceCount++] = resource;
        }
        //Custom objects
        resource = new Resource(ResourceType.CustomObject);
        this.ResourceSequence[this.ResourceCount++] = resource;

        switch (this.Biome.F)
        {
            case 0: // Ocean - default
            case 3: // BigHills - default
            case 7: // River - default
                resource = new Resource(ResourceType.Tree, this.DefaultTrees, new TreeType[]{TreeType.BigTree, TreeType.Tree}, new int[]{1, 9});
                this.ResourceSequence[this.ResourceCount++] = resource;
                break;
            case 1: // Plains - no tree
            case 2: // Desert - no tree
                break;

            case 4: // Forest - forest
                resource = new Resource(ResourceType.Tree, this.DefaultTrees, new TreeType[]{TreeType.Forest, TreeType.BigTree, TreeType.Tree}, new int[]{20, 10, 100});
                this.ResourceSequence[this.ResourceCount++] = resource;
                break;
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
            resource = new Resource(ResourceType.AboveWaterRes, Block.WATER_LILY.id, 0, 0, this.DefaultWaterLily, 100, 0, 0, new int[0]);
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        if (this.DefaultFlowers > 0)
        {
            //Red flower
            resource = new Resource(ResourceType.Plant, Block.RED_ROSE.id, 0, 0, this.DefaultFlowers, TCDefaultValues.roseDepositRarity.intValue(), TCDefaultValues.roseDepositMinAltitude.intValue(), TCDefaultValues.roseDepositMaxAltitude.intValue(), new int[]{Block.GRASS.id, Block.DIRT.id, Block.SOIL.id});
            this.ResourceSequence[this.ResourceCount++] = resource;

            //Yellow flower
            resource = new Resource(ResourceType.Plant, Block.YELLOW_FLOWER.id, 0, 0, this.DefaultFlowers, TCDefaultValues.flowerDepositRarity.intValue(), TCDefaultValues.flowerDepositMinAltitude.intValue(), TCDefaultValues.flowerDepositMaxAltitude.intValue(), new int[]{Block.GRASS.id, Block.DIRT.id, Block.SOIL.id});
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        if (this.DefaultMushroom > 0)
        {
            //Red mushroom
            resource = new Resource(ResourceType.Plant, Block.RED_MUSHROOM.id, 0, 0, this.DefaultMushroom, TCDefaultValues.redMushroomDepositRarity.intValue(), TCDefaultValues.redMushroomDepositMinAltitude.intValue(), TCDefaultValues.redMushroomDepositMaxAltitude.intValue(), new int[]{Block.GRASS.id, Block.DIRT.id});
            this.ResourceSequence[this.ResourceCount++] = resource;

            //Brown mushroom
            resource = new Resource(ResourceType.Plant, Block.BROWN_MUSHROOM.id, 0, 0, this.DefaultMushroom, TCDefaultValues.brownMushroomDepositRarity.intValue(), TCDefaultValues.brownMushroomDepositMinAltitude.intValue(), TCDefaultValues.brownMushroomDepositMaxAltitude.intValue(), new int[]{Block.GRASS.id, Block.DIRT.id});
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        if (this.DefaultGrass > 0)
        {
            //Grass
            resource = new Resource(ResourceType.Grass, Block.LONG_GRASS.id, 1, 0, this.DefaultGrass, TCDefaultValues.longGrassDepositRarity.intValue(), 0, 0, new int[]{Block.GRASS.id, Block.DIRT.id});
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        if (this.DefaultDeadBrush > 0)
        {
            //Dead Bush
            resource = new Resource(ResourceType.Grass, Block.DEAD_BUSH.id, 0, 0, this.DefaultDeadBrush, TCDefaultValues.deadBushDepositRarity.intValue(), 0, 0, new int[]{Block.SAND.id});
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        //Pumpkin
        resource = new Resource(ResourceType.Plant, Block.PUMPKIN.id, 0, 0, TCDefaultValues.pumpkinDepositFrequency.intValue(), TCDefaultValues.pumpkinDepositRarity.intValue(), TCDefaultValues.pumpkinDepositMinAltitude.intValue(), TCDefaultValues.pumpkinDepositMaxAltitude.intValue(), new int[]{Block.GRASS.id});
        this.ResourceSequence[this.ResourceCount++] = resource;


        if (this.DefaultReed > 0)
        {
            //Reed
            resource = new Resource(ResourceType.Reed, Block.SUGAR_CANE_BLOCK.id, 0, 0, this.DefaultReed, TCDefaultValues.reedDepositRarity.intValue(), TCDefaultValues.reedDepositMinAltitude.intValue(), TCDefaultValues.reedDepositMaxAltitude.intValue(), new int[]{Block.GRASS.id, Block.DIRT.id, Block.SAND.id});
            this.ResourceSequence[this.ResourceCount++] = resource;
        }


        if (this.DefaultCactus > 0)
        {
            //Cactus
            resource = new Resource(ResourceType.Cactus, Block.CACTUS.id, 0, 0, this.DefaultCactus, TCDefaultValues.cactusDepositRarity.intValue(), TCDefaultValues.cactusDepositMinAltitude.intValue(), TCDefaultValues.cactusDepositMaxAltitude.intValue(), new int[]{Block.SAND.id});
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        //Water source
        resource = new Resource(ResourceType.Liquid, Block.WATER.id, 0, 0, TCDefaultValues.waterSourceDepositFrequency.intValue(), TCDefaultValues.waterSourceDepositRarity.intValue(), TCDefaultValues.waterSourceDepositMinAltitude.intValue(), TCDefaultValues.waterSourceDepositMaxAltitude.intValue(), new int[]{Block.STONE.id});
        this.ResourceSequence[this.ResourceCount++] = resource;

        //Lava source
        resource = new Resource(ResourceType.Liquid, Block.LAVA.id, 0, 0, TCDefaultValues.lavaSourceDepositFrequency.intValue(), TCDefaultValues.lavaSourceDepositRarity.intValue(), TCDefaultValues.lavaSourceDepositMinAltitude.intValue(), TCDefaultValues.lavaSourceDepositMaxAltitude.intValue(), new int[]{Block.STONE.id});
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

        this.BiomeTemperature = this.ReadModSettings(TCDefaultValues.BiomeTemperature.name(), this.DefaultBiomeTemperature);
        this.BiomeWetness = this.ReadModSettings(TCDefaultValues.BiomeWetness.name(), this.DefaultBiomeWetness);


        this.evenWaterSourceDistribution = this.ReadModSettings(TCDefaultValues.evenWaterSourceDistribution.name(), TCDefaultValues.evenWaterSourceDistribution.booleanValue());
        this.evenLavaSourceDistribution = this.ReadModSettings(TCDefaultValues.evenLavaSourceDistribution.name(), TCDefaultValues.evenLavaSourceDistribution.booleanValue());

        this.BiomeHeight = this.ReadModSettings(TCDefaultValues.BiomeHeight.name(), this.DefaultBiomeSurface);
        this.BiomeVolatility = this.ReadModSettings(TCDefaultValues.BiomeVolatility.name(), this.DefaultBiomeVolatility);

        this.SurfaceBlock = this.ReadModSettings(TCDefaultValues.SurfaceBlock.name(), this.DefaultSurfaceBlock);
        this.GroundBlock = this.ReadModSettings(TCDefaultValues.GroundBlock.name(), this.DefaultGroundBlock);

        this.disableNotchPonds = this.ReadModSettings(TCDefaultValues.disableNotchPonds.name(), TCDefaultValues.disableNotchPonds.booleanValue());


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
    
    private int[] blockIdAndDataFromString(String input) {
    	int[] block = new int[2]; // [block ID, block data]
    	
    	if (input.indexOf(".") > -1) {
    		String[] parts = input.split("\\.");
    		block[0] = Integer.valueOf(parts[0]); // Block ID
    		block[1] = Integer.valueOf(parts[1]); // Block data
    	} else {
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
            } else {
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
                        res.ReadFromString(key.substring(start + 1, end));

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
        WriteTitle(this.Biome.r + " biome config");

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

        WriteComment("Biome temperature. Float value from 0.0 to 1.0");
        WriteValue(TCDefaultValues.BiomeTemperature.name(), this.BiomeTemperature);
        this.WriteNewLine();
        WriteComment("Biome wetness. Float value from 0.0 to 1.0");
        WriteValue(TCDefaultValues.BiomeWetness.name(), this.BiomeWetness);
        this.WriteNewLine();


        this.WriteNewLine();
        WriteComment("BiomeHeight mean how much height will be added in terrain generation");
        WriteComment("It is double value from -10.0 to 10.0");
        WriteComment("BiomeHeight:0.0 - mean height controlled only by world config and near 64 if it default");
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

        this.WriteTitle("Resource queue");
        this.WriteComment("This section control all resources spawning after terrain generation");
        this.WriteComment("So first line is first resource which will be placed. Second line - second resource.");
        this.WriteComment("By default this set to be near notch settings.");
        this.WriteComment("");
        this.WriteComment("Possible resources:");
        this.WriteComment("Dungeon(Frequency,Rarity,MinAltitude,MaxAltitude)");
        this.WriteComment("UnderGroundLake(MinSize,MaxSize,Frequency,Rarity,MinAltitude,MaxAltitude)");
        this.WriteComment("Ore(Block,Size,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.WriteComment("UnderWaterOre(Block,Size,Frequency,Rarity,BlockSource[,BlockSource2,BlockSource3.....])");
        this.WriteComment("CustomObject()");
        this.WriteComment("Tree(Frequency,TreeType,TreeType_Chance[,Additional_TreeType,Additional_TreeType_Chance.....])");
        this.WriteComment("Plant(Block,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
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
            if (me.getValue()[1] > 0) {
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
    private byte DefaultSurfaceBlock = (byte) Block.GRASS.id;
    private byte DefaultGroundBlock = (byte) Block.DIRT.id;
    private float DefaultBiomeTemperature = 0.5F;
    private float DefaultBiomeWetness = 0.5F;
    private ArrayList<String> DefaultIsle = new ArrayList<String>();
    private ArrayList<String> DefaultBorder = new ArrayList<String>();
    private boolean DefaultRiver = true;
    private int DefaultSize = 4;
    private int DefaultRarity = 100;
    private String DefaultColor = "0x000000";
    private int DefaultWaterLily = 0;


    private void InitDefaults()
    {
        this.DefaultBiomeSurface = this.Biome.w;
        this.DefaultBiomeVolatility = this.Biome.x;
        this.DefaultSurfaceBlock = this.Biome.t;
        this.DefaultGroundBlock = this.Biome.u;
        this.DefaultBiomeTemperature = this.Biome.y;
        this.DefaultBiomeWetness = this.Biome.z;


        switch (this.Biome.F)
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
                this.DefaultColor = "0x666600";
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
                break;
            }
            case 7:
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
                this.DefaultSurfaceBlock = (byte) Block.MYCEL.id;
                this.DefaultMushroom = 1;
                this.DefaultGrass = 0;
                this.DefaultFlowers = 0;
                this.DefaultTrees = 0;
                this.DefaultRarity = 1;
                this.DefaultRiver = false;
                this.DefaultSize = 6;
                this.DefaultIsle.add(BiomeBase.OCEAN.r);
                this.DefaultColor = "0xFF33CC";
                this.DefaultWaterLily = 1;
                break;
            }
            case 15:
            {
                this.DefaultRiver = false;
                this.DefaultSize = 9;
                this.DefaultBorder.add(BiomeBase.MUSHROOM_ISLAND.r);
                this.DefaultColor = "0xFF9999";
                break;
            }

        }

    }

}
