package com.Khorn.PTMBukkit;

import net.minecraft.server.*;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.generator.BlockPopulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ObjectSpawner extends BlockPopulator
{

    private ArrayList<CustomObject> Objects = new ArrayList<CustomObject>();
    private ArrayList<CustomObjectLegacy> LegacyObjects = new ArrayList<CustomObjectLegacy>();
    private HashMap<String, ArrayList<CustomObject>> ObjectGroups = new HashMap<String, ArrayList<CustomObject>>();
    private HashMap<String, ArrayList<CustomObject>> BranchGroups = new HashMap<String, ArrayList<CustomObject>>();
    private boolean HasCustomTrees = false;

    public NoiseGeneratorOctaves c;

    private boolean evenWaterSourceDistribution;
    private boolean evenLavaSourceDistribution;

    // Materials
    private int flowerDepositRarity;
    private int flowerDepositFrequency;
    private int flowerDepositMinAltitude;
    private int flowerDepositMaxAltitude;
    private int roseDepositRarity;
    private int roseDepositFrequency;
    private int roseDepositMinAltitude;
    private int roseDepositMaxAltitude;
    private int brownMushroomDepositRarity;
    private int brownMushroomDepositFrequency;
    private int brownMushroomDepositMinAltitude;
    private int brownMushroomDepositMaxAltitude;
    private int redMushroomDepositRarity;
    private int redMushroomDepositFrequency;
    private int redMushroomDepositMinAltitude;
    private int redMushroomDepositMaxAltitude;
    private int reedDepositRarity;
    private int reedDepositFrequency;
    private int reedDepositMinAltitude;
    private int reedDepositMaxAltitude;
    private int pumpkinDepositRarity;
    private int pumpkinDepositFrequency;
    private int pumpkinDepositMinAltitude;
    private int pumpkinDepositMaxAltitude;
    private int waterSourceDepositRarity;
    private int waterSourceDepositFrequency;
    private int waterSourceDepositMinAltitude;
    private int waterSourceDepositMaxAltitude;
    private int lavaSourceDepositRarity;
    private int lavaSourceDepositFrequency;
    private int lavaSourceDepositMinAltitude;
    private int lavaSourceDepositMaxAltitude;
    private int dirtDepositRarity1;
    private int dirtDepositFrequency1;
    private int dirtDepositSize1;
    private int dirtDepositMinAltitude1;
    private int dirtDepositMaxAltitude1;
    private int dirtDepositRarity2;
    private int dirtDepositFrequency2;
    private int dirtDepositSize2;
    private int dirtDepositMinAltitude2;
    private int dirtDepositMaxAltitude2;
    private int dirtDepositRarity3;
    private int dirtDepositFrequency3;
    private int dirtDepositSize3;
    private int dirtDepositMinAltitude3;
    private int dirtDepositMaxAltitude3;
    private int dirtDepositRarity4;
    private int dirtDepositFrequency4;
    private int dirtDepositSize4;
    private int dirtDepositMinAltitude4;
    private int dirtDepositMaxAltitude4;
    private int gravelDepositRarity1;
    private int gravelDepositFrequency1;
    private int gravelDepositSize1;
    private int gravelDepositMinAltitude1;
    private int gravelDepositMaxAltitude1;
    private int gravelDepositRarity2;
    private int gravelDepositFrequency2;
    private int gravelDepositSize2;
    private int gravelDepositMinAltitude2;
    private int gravelDepositMaxAltitude2;
    private int gravelDepositRarity3;
    private int gravelDepositFrequency3;
    private int gravelDepositSize3;
    private int gravelDepositMinAltitude3;
    private int gravelDepositMaxAltitude3;
    private int gravelDepositRarity4;
    private int gravelDepositFrequency4;
    private int gravelDepositSize4;
    private int gravelDepositMinAltitude4;
    private int gravelDepositMaxAltitude4;
    private int clayDepositRarity1;
    private int clayDepositFrequency1;
    private int clayDepositSize1;
    private int clayDepositMinAltitude1;
    private int clayDepositMaxAltitude1;
    private int clayDepositRarity2;
    private int clayDepositFrequency2;
    private int clayDepositSize2;
    private int clayDepositMinAltitude2;
    private int clayDepositMaxAltitude2;
    private int clayDepositRarity3;
    private int clayDepositFrequency3;
    private int clayDepositSize3;
    private int clayDepositMinAltitude3;
    private int clayDepositMaxAltitude3;
    private int clayDepositRarity4;
    private int clayDepositFrequency4;
    private int clayDepositSize4;
    private int clayDepositMinAltitude4;
    private int clayDepositMaxAltitude4;
    private int coalDepositRarity1;
    private int coalDepositFrequency1;
    private int coalDepositSize1;
    private int coalDepositMinAltitude1;
    private int coalDepositMaxAltitude1;
    private int coalDepositRarity2;
    private int coalDepositFrequency2;
    private int coalDepositSize2;
    private int coalDepositMinAltitude2;
    private int coalDepositMaxAltitude2;
    private int coalDepositRarity3;
    private int coalDepositFrequency3;
    private int coalDepositSize3;
    private int coalDepositMinAltitude3;
    private int coalDepositMaxAltitude3;
    private int coalDepositRarity4;
    private int coalDepositFrequency4;
    private int coalDepositSize4;
    private int coalDepositMinAltitude4;
    private int coalDepositMaxAltitude4;
    private int ironDepositRarity1;
    private int ironDepositFrequency1;
    private int ironDepositSize1;
    private int ironDepositMinAltitude1;
    private int ironDepositMaxAltitude1;
    private int ironDepositRarity2;
    private int ironDepositFrequency2;
    private int ironDepositSize2;
    private int ironDepositMinAltitude2;
    private int ironDepositMaxAltitude2;
    private int ironDepositRarity3;
    private int ironDepositFrequency3;
    private int ironDepositSize3;
    private int ironDepositMinAltitude3;
    private int ironDepositMaxAltitude3;
    private int ironDepositRarity4;
    private int ironDepositFrequency4;
    private int ironDepositSize4;
    private int ironDepositMinAltitude4;
    private int ironDepositMaxAltitude4;
    private int goldDepositRarity1;
    private int goldDepositFrequency1;
    private int goldDepositSize1;
    private int goldDepositMinAltitude1;
    private int goldDepositMaxAltitude1;
    private int goldDepositRarity2;
    private int goldDepositFrequency2;
    private int goldDepositSize2;
    private int goldDepositMinAltitude2;
    private int goldDepositMaxAltitude2;
    private int goldDepositRarity3;
    private int goldDepositFrequency3;
    private int goldDepositSize3;
    private int goldDepositMinAltitude3;
    private int goldDepositMaxAltitude3;
    private int goldDepositRarity4;
    private int goldDepositFrequency4;
    private int goldDepositSize4;
    private int goldDepositMinAltitude4;
    private int goldDepositMaxAltitude4;
    private int redstoneDepositRarity1;
    private int redstoneDepositFrequency1;
    private int redstoneDepositSize1;
    private int redstoneDepositMinAltitude1;
    private int redstoneDepositMaxAltitude1;
    private int redstoneDepositRarity2;
    private int redstoneDepositFrequency2;
    private int redstoneDepositSize2;
    private int redstoneDepositMinAltitude2;
    private int redstoneDepositMaxAltitude2;
    private int redstoneDepositRarity3;
    private int redstoneDepositFrequency3;
    private int redstoneDepositSize3;
    private int redstoneDepositMinAltitude3;
    private int redstoneDepositMaxAltitude3;
    private int redstoneDepositRarity4;
    private int redstoneDepositFrequency4;
    private int redstoneDepositSize4;
    private int redstoneDepositMinAltitude4;
    private int redstoneDepositMaxAltitude4;
    private int diamondDepositRarity1;
    private int diamondDepositFrequency1;
    private int diamondDepositSize1;
    private int diamondDepositMinAltitude1;
    private int diamondDepositMaxAltitude1;
    private int diamondDepositRarity2;
    private int diamondDepositFrequency2;
    private int diamondDepositSize2;
    private int diamondDepositMinAltitude2;
    private int diamondDepositMaxAltitude2;
    private int diamondDepositRarity3;
    private int diamondDepositFrequency3;
    private int diamondDepositSize3;
    private int diamondDepositMinAltitude3;
    private int diamondDepositMaxAltitude3;
    private int diamondDepositRarity4;
    private int diamondDepositFrequency4;
    private int diamondDepositSize4;
    private int diamondDepositMinAltitude4;
    private int diamondDepositMaxAltitude4;
    private int lapislazuliDepositRarity1;
    private int lapislazuliDepositFrequency1;
    private int lapislazuliDepositSize1;
    private int lapislazuliDepositMinAltitude1;
    private int lapislazuliDepositMaxAltitude1;
    private int lapislazuliDepositRarity2;
    private int lapislazuliDepositFrequency2;
    private int lapislazuliDepositSize2;
    private int lapislazuliDepositMinAltitude2;
    private int lapislazuliDepositMaxAltitude2;
    private int lapislazuliDepositRarity3;
    private int lapislazuliDepositFrequency3;
    private int lapislazuliDepositSize3;
    private int lapislazuliDepositMinAltitude3;
    private int lapislazuliDepositMaxAltitude3;
    private int lapislazuliDepositRarity4;
    private int lapislazuliDepositFrequency4;
    private int lapislazuliDepositSize4;
    private int lapislazuliDepositMinAltitude4;
    private int lapislazuliDepositMaxAltitude4;
    // private boolean evenFireHellDepositDistribution;
    // private boolean evenLightstoneHellDepositDistribution;
    private int lavaSourceHellDepositRarity;
    private int lavaSourceHellDepositFrequency;
    private int lavaSourceHellDepositMinAltitude;
    private int lavaSourceHellDepositMaxAltitude;
    private int fireHellDepositRarity;
    private int fireHellDepositFrequency;
    private int fireHellDepositMinAltitude;
    private int fireHellDepositMaxAltitude;
    private int lightstoneHellDepositRarity1;
    private int lightstoneHellDepositFrequency1;
    private int lightstoneHellDepositMinAltitude1;
    private int lightstoneHellDepositMaxAltitude1;
    private int lightstoneHellDepositRarity2;
    private int lightstoneHellDepositFrequency2;
    private int lightstoneHellDepositMinAltitude2;
    private int lightstoneHellDepositMaxAltitude2;
    private int brownMushroomHellDepositRarity;
    private int brownMushroomHellDepositFrequency;
    private int brownMushroomHellDepositMinAltitude;
    private int brownMushroomHellDepositMaxAltitude;
    private int redMushroomHellDepositRarity;
    private int redMushroomHellDepositFrequency;
    private int redMushroomHellDepositMinAltitude;
    private int redMushroomHellDepositMaxAltitude;
    // End Materials

    private boolean disableNotchPonds;

    private int dungeonRarity;
    private int dungeonFrequency;
    private int dungeonMinAltitude;
    private int dungeonMaxAltitude;

    private boolean customObjects;
    private int objectSpawnRatio;
    private boolean notchBiomeTrees;

    private int globalTreeDensity;
    private int rainforestTreeDensity;
    private int swamplandTreeDensity;
    private int seasonalforestTreeDensity;
    private int forestTreeDensity;
    private int savannaTreeDensity;
    private int shrublandTreeDensity;
    private int taigaTreeDensity;
    private int desertTreeDensity;
    private int plainsTreeDensity;
    private int iceDesertTreeDensity;
    private int tundraTreeDensity;
    private int globalCactusDensity;
    private int desertCactusDensity;

    private int cactusDepositRarity;
    private int cactusDepositMinAltitude;
    private int cactusDepositMaxAltitude;

    private boolean undergroundLakes;
    private boolean undergroundLakesInAir;
    private int undergroundLakeFrequency;
    private int undergroundLakeRarity;
    private int undergroundLakeMinSize;
    private int undergroundLakeMaxSize;
    private int undergroundLakeMinAltitude;
    private int undergroundLakeMaxAltitude;

    private int lavaLevelMin;
    private int lavaLevelMax;

    private WorldWorker worldWrk;
    private Random rand;
    public World world;

    public ObjectSpawner(WorldWorker wrk)
    {
        this.worldWrk = wrk;


    }

    public void RegisterBOBPlugins()
    {
        if (this.customObjects)
        {
            try
            {
                File BOBFolder = new File(worldWrk.SettingsDir.concat(PTMPlugin.FILE_SEPARATOR), "BOBPlugins");
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
                    this.RegisterBOBPlugins(BOBFile);
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

    private void RegisterBOBPlugins(File ObjectPlugin)
    {
        try
        {

            BufferedReader ObjectProps = new BufferedReader(new FileReader(ObjectPlugin));
            // Legacy BOB Loader
            if ((ObjectPlugin.getName().endsWith(".bob")) || (ObjectPlugin.getName().endsWith(".BOB")))
            {

                CustomObjectLegacy workObject = new CustomObjectLegacy();
                String workingString = "";

                while (!((workingString.equals("METBEGIN")) || (workingString.equals("METABEGIN"))))
                {
                    workingString = ObjectProps.readLine();
                    if (!((workingString.equals("METBEGIN") || (workingString.equals("METABEGIN")))))
                    {
                        String[] stringSet = workingString.split(",");
                        int X = Integer.parseInt(stringSet[0]);
                        int Y = Integer.parseInt(stringSet[1]);
                        double Data = Double.valueOf(stringSet[2].split(":")[1]);
                        int Z = Integer.parseInt(stringSet[2].split(":")[0]);
                        workObject.DataValues[X][Y][Z] = Data;
                    }
                }
                workObject.spawnID = Integer.parseInt(ObjectProps.readLine());

                if (ObjectProps.readLine().equals("true"))
                {
                    workObject.underwater = true;
                }
                this.LegacyObjects.add(workObject);

                System.out.println("BOB Plugin Registered: " + ObjectPlugin.getName());
                ObjectProps.close();
                return;

            }
            // BO2 Loader
            if ((ObjectPlugin.getName().endsWith(".bo2")) || (ObjectPlugin.getName().endsWith(".BO2")))
            {
                CustomObject WorkingCustomObject = new CustomObject();

                String workingString = ObjectProps.readLine();

                if (!workingString.equals("[META]"))
                {
                    System.out.println("Invalid BOB Plugin: " + ObjectPlugin.getName());
                    ObjectProps.close();
                    return;
                }

                boolean dataReached = false;
                while ((workingString = ObjectProps.readLine()) != null)
                {

                    if (!dataReached)
                    {
                        if (workingString.contains("="))
                        {
                            String[] stringSet = workingString.split("=");
                            if (stringSet[0].equals("spawnOnBlockType"))
                            {
                                String[] blocks = stringSet[1].split(",");
                                int counter = 0;
                                while (counter < blocks.length)
                                {
                                    WorkingCustomObject.spawnOnBlockType.add(Integer.parseInt(blocks[counter]));
                                    counter++;
                                }
                            }
                            if (stringSet[0].equals("spawnSunlight"))
                            {
                                if (stringSet[1].toLowerCase().equals("false"))
                                {
                                    WorkingCustomObject.spawnSunlight = false;
                                }
                            }
                            if (stringSet[0].equals("spawnDarkness"))
                            {
                                if (stringSet[1].toLowerCase().equals("true"))
                                {
                                    WorkingCustomObject.spawnDarkness = true;
                                }
                            }
                            if (stringSet[0].equals("spawnWater"))
                            {
                                if (stringSet[1].toLowerCase().equals("true"))
                                {
                                    WorkingCustomObject.spawnWater = true;
                                }
                            }
                            if (stringSet[0].equals("spawnLava"))
                            {
                                if (stringSet[1].toLowerCase().equals("true"))
                                {
                                    WorkingCustomObject.spawnLava = true;
                                }
                            }
                            if (stringSet[0].equals("underFill"))
                            {
                                if (stringSet[1].toLowerCase().equals("false"))
                                {
                                    WorkingCustomObject.underFill = false;
                                }
                            }
                            if (stringSet[0].equals("randomRotation"))
                            {
                                if (stringSet[1].toLowerCase().equals("false"))
                                {
                                    WorkingCustomObject.randomRotation = false;
                                }
                            }
                            if (stringSet[0].equals("dig"))
                            {
                                if (stringSet[1].toLowerCase().equals("true"))
                                {
                                    WorkingCustomObject.dig = true;
                                }
                            }
                            if (stringSet[0].equals("rarity"))
                            {
                                WorkingCustomObject.rarity = Integer.parseInt(stringSet[1]);
                            }
                            if (stringSet[0].equals("spawnElevationMin"))
                            {
                                WorkingCustomObject.spawnElevationMin = Integer.parseInt(stringSet[1]);
                            }
                            if (stringSet[0].equals("spawnElevationMax"))
                            {
                                WorkingCustomObject.spawnElevationMax = Integer.parseInt(stringSet[1]);
                            }
                            if (stringSet[0].equals("groupId"))
                            {
                                WorkingCustomObject.groupId = stringSet[1];
                            }
                            if (stringSet[0].equals("tree"))
                            {
                                if (stringSet[1].toLowerCase().equals("true"))
                                {
                                    WorkingCustomObject.tree = true;
                                }
                            }
                            if (stringSet[0].equals("branch"))
                            {
                                if (stringSet[1].toLowerCase().equals("true"))
                                {
                                    WorkingCustomObject.branch = true;
                                }
                            }
                            if (stringSet[0].equals("diggingBranch"))
                            {
                                if (stringSet[1].toLowerCase().equals("true"))
                                {
                                    WorkingCustomObject.diggingBranch = true;
                                }
                            }
                            if (stringSet[0].equals("groupFrequencyMin"))
                            {
                                WorkingCustomObject.groupFrequencyMin = Integer.parseInt(stringSet[1]);
                            }
                            if (stringSet[0].equals("groupFrequencyMax"))
                            {
                                WorkingCustomObject.groupFrequencyMax = Integer.parseInt(stringSet[1]);
                            }
                            if (stringSet[0].equals("groupSeperationMin"))
                            {
                                WorkingCustomObject.groupSeperationMin = Integer.parseInt(stringSet[1]);
                            }
                            if (stringSet[0].equals("groupSeperationMax"))
                            {
                                WorkingCustomObject.groupSeperationMax = Integer.parseInt(stringSet[1]);
                            }
                            if (stringSet[0].equals("collisionPercentage"))
                            {
                                WorkingCustomObject.collisionPercentage = (Integer.parseInt(stringSet[1]) / 100);
                            }
                            if (stringSet[0].equals("spawnInBiome"))
                            {
                                stringSet = stringSet[1].split(",");
                                int counter = 0;
                                while (counter < WorkingCustomObject.spawnInBiome.size())
                                {
                                    if (stringSet[counter].equals("Icedesert"))
                                        WorkingCustomObject.spawnInBiome.add("ice desert");
                                    else if (stringSet[counter].equals("Seasonalforest"))
                                        WorkingCustomObject.spawnInBiome.add("seasonal forest");
                                    else
                                        WorkingCustomObject.spawnInBiome.add(stringSet[counter].toLowerCase());
                                    counter++;
                                }
                            }
                            if (stringSet[0].equals("branchLimit"))
                            {
                                WorkingCustomObject.branchLimit = (Integer.parseInt(stringSet[1]));
                            }
                            if (stringSet[0].equals("needsFoundation"))
                            {
                                if (stringSet[1].toLowerCase().equals("false"))
                                {
                                    WorkingCustomObject.needsFoundation = false;
                                }
                            }
                            if (stringSet[0].equals("version"))
                            {
                                WorkingCustomObject.version = stringSet[1].toLowerCase();
                            }

                        } else if (workingString.equals("[DATA]"))
                            dataReached = true;
                        continue;
                    }

                    String[] CoordinateSet = workingString.split(":")[0].split(",");
                    String BlockString = workingString.split(":")[1];
                    Coordinate Coordinates;
                    if (WorkingCustomObject.dig)
                    {
                        Coordinates = new Coordinate(Integer.parseInt(CoordinateSet[0]), Integer.parseInt(CoordinateSet[2]), Integer.parseInt(CoordinateSet[1]), BlockString, true);
                    } else
                    {
                        Coordinates = new Coordinate(Integer.parseInt(CoordinateSet[0]), Integer.parseInt(CoordinateSet[2]), Integer.parseInt(CoordinateSet[1]), BlockString, false);

                    }
                    Coordinates.RegisterData();
                    WorkingCustomObject.Data.add(Coordinates);

                }

                if (!dataReached)
                {
                    System.out.println("Invalid BOB Plugin: " + ObjectPlugin.getName());
                    ObjectProps.close();
                    return;
                }

                WorkingCustomObject.CorrectSettings();

                WorkingCustomObject.name = ObjectPlugin.getName();

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

                System.out.println("BOB Plugin Registered: " + ObjectPlugin.getName());
                ObjectProps.close();

            }
        } catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Invalid BOB Plugin: " + ObjectPlugin.getName());
        }

    }


    public void readSettings()
    {
        this.flowerDepositRarity = this.worldWrk.ReadModSettins(BiomeTerrainValues.flowerDepositRarity.name(), BiomeTerrainValues.flowerDepositRarity.intValue());
        this.flowerDepositFrequency = this.worldWrk.ReadModSettins(BiomeTerrainValues.flowerDepositFrequency.name(), BiomeTerrainValues.flowerDepositFrequency.intValue());
        this.flowerDepositMinAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.flowerDepositMinAltitude.name(), BiomeTerrainValues.flowerDepositMinAltitude.intValue());
        this.flowerDepositMaxAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.flowerDepositMaxAltitude.name(), BiomeTerrainValues.flowerDepositMaxAltitude.intValue());
        this.roseDepositRarity = this.worldWrk.ReadModSettins(BiomeTerrainValues.roseDepositRarity.name(), BiomeTerrainValues.roseDepositRarity.intValue());
        this.roseDepositFrequency = this.worldWrk.ReadModSettins(BiomeTerrainValues.roseDepositFrequency.name(), BiomeTerrainValues.roseDepositFrequency.intValue());
        this.roseDepositMinAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.roseDepositMinAltitude.name(), BiomeTerrainValues.roseDepositMinAltitude.intValue());
        this.roseDepositMaxAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.roseDepositMaxAltitude.name(), BiomeTerrainValues.roseDepositMaxAltitude.intValue());
        this.brownMushroomDepositRarity = this.worldWrk.ReadModSettins(BiomeTerrainValues.brownMushroomDepositRarity.name(), BiomeTerrainValues.brownMushroomDepositRarity.intValue());
        this.brownMushroomDepositFrequency = this.worldWrk.ReadModSettins(BiomeTerrainValues.brownMushroomDepositFrequency.name(), BiomeTerrainValues.brownMushroomDepositFrequency.intValue());
        this.brownMushroomDepositMinAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.brownMushroomDepositMinAltitude.name(), BiomeTerrainValues.brownMushroomDepositMinAltitude.intValue());
        this.brownMushroomDepositMaxAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.brownMushroomDepositMaxAltitude.name(), BiomeTerrainValues.brownMushroomDepositMaxAltitude.intValue());
        this.redMushroomDepositRarity = this.worldWrk.ReadModSettins(BiomeTerrainValues.redMushroomDepositRarity.name(), BiomeTerrainValues.redMushroomDepositRarity.intValue());
        this.redMushroomDepositFrequency = this.worldWrk.ReadModSettins(BiomeTerrainValues.redMushroomDepositFrequency.name(), BiomeTerrainValues.redMushroomDepositFrequency.intValue());
        this.redMushroomDepositMinAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.redMushroomDepositMinAltitude.name(), BiomeTerrainValues.redMushroomDepositMinAltitude.intValue());
        this.redMushroomDepositMaxAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.redMushroomDepositMaxAltitude.name(), BiomeTerrainValues.redMushroomDepositMaxAltitude.intValue());
        this.reedDepositRarity = this.worldWrk.ReadModSettins(BiomeTerrainValues.reedDepositRarity.name(), BiomeTerrainValues.reedDepositRarity.intValue());
        this.reedDepositFrequency = this.worldWrk.ReadModSettins(BiomeTerrainValues.reedDepositFrequency.name(), BiomeTerrainValues.reedDepositFrequency.intValue());
        this.reedDepositMinAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.reedDepositMinAltitude.name(), BiomeTerrainValues.reedDepositMinAltitude.intValue());
        this.reedDepositMaxAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.reedDepositMaxAltitude.name(), BiomeTerrainValues.reedDepositMaxAltitude.intValue());
        this.pumpkinDepositRarity = this.worldWrk.ReadModSettins(BiomeTerrainValues.pumpkinDepositRarity.name(), BiomeTerrainValues.pumpkinDepositRarity.intValue());
        this.pumpkinDepositFrequency = this.worldWrk.ReadModSettins(BiomeTerrainValues.pumpkinDepositFrequency.name(), BiomeTerrainValues.pumpkinDepositFrequency.intValue());
        this.pumpkinDepositMinAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.pumpkinDepositMinAltitude.name(), BiomeTerrainValues.pumpkinDepositMinAltitude.intValue());
        this.pumpkinDepositMaxAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.pumpkinDepositMaxAltitude.name(), BiomeTerrainValues.pumpkinDepositMaxAltitude.intValue());

        this.evenWaterSourceDistribution = this.worldWrk.ReadModSettins(BiomeTerrainValues.evenWaterSourceDistribution.name(), BiomeTerrainValues.evenWaterSourceDistribution.booleanValue());

        this.waterSourceDepositRarity = this.worldWrk.ReadModSettins(BiomeTerrainValues.waterSourceDepositRarity.name(), BiomeTerrainValues.waterSourceDepositRarity.intValue());
        this.waterSourceDepositFrequency = this.worldWrk.ReadModSettins(BiomeTerrainValues.waterSourceDepositFrequency.name(), BiomeTerrainValues.waterSourceDepositFrequency.intValue());
        this.waterSourceDepositMinAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.waterSourceDepositMinAltitude.name(), BiomeTerrainValues.waterSourceDepositMinAltitude.intValue());
        this.waterSourceDepositMaxAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.waterSourceDepositMaxAltitude.name(), BiomeTerrainValues.waterSourceDepositMaxAltitude.intValue());

        this.evenWaterSourceDistribution = this.worldWrk.ReadModSettins(BiomeTerrainValues.evenWaterSourceDistribution.name(), BiomeTerrainValues.evenWaterSourceDistribution.booleanValue());
        this.evenLavaSourceDistribution = this.worldWrk.ReadModSettins(BiomeTerrainValues.evenLavaSourceDistribution.name(), BiomeTerrainValues.evenLavaSourceDistribution.booleanValue());

        this.lavaSourceDepositRarity = this.worldWrk.ReadModSettins(BiomeTerrainValues.lavaSourceDepositRarity.name(), BiomeTerrainValues.lavaSourceDepositRarity.intValue());
        this.lavaSourceDepositFrequency = this.worldWrk.ReadModSettins(BiomeTerrainValues.lavaSourceDepositFrequency.name(), BiomeTerrainValues.lavaSourceDepositFrequency.intValue());
        this.lavaSourceDepositMinAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.lavaSourceDepositMinAltitude.name(), BiomeTerrainValues.lavaSourceDepositMinAltitude.intValue());
        this.lavaSourceDepositMaxAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.lavaSourceDepositMaxAltitude.name(), BiomeTerrainValues.lavaSourceDepositMaxAltitude.intValue());

        this.dirtDepositRarity1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.dirtDepositRarity1.name(), BiomeTerrainValues.dirtDepositRarity1.intValue());
        this.dirtDepositFrequency1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.dirtDepositFrequency1.name(), BiomeTerrainValues.dirtDepositFrequency1.intValue());
        this.dirtDepositSize1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.dirtDepositSize1.name(), BiomeTerrainValues.dirtDepositSize1.intValue());
        this.dirtDepositMinAltitude1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.dirtDepositMinAltitude1.name(), BiomeTerrainValues.dirtDepositMinAltitude1.intValue());
        this.dirtDepositMaxAltitude1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.dirtDepositMaxAltitude1.name(), BiomeTerrainValues.dirtDepositMaxAltitude1.intValue());
        this.dirtDepositRarity2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.dirtDepositRarity2.name(), BiomeTerrainValues.dirtDepositRarity2.intValue());
        this.dirtDepositFrequency2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.dirtDepositFrequency2.name(), BiomeTerrainValues.dirtDepositFrequency2.intValue());
        this.dirtDepositSize2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.dirtDepositSize2.name(), BiomeTerrainValues.dirtDepositSize2.intValue());
        this.dirtDepositMinAltitude2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.dirtDepositMinAltitude2.name(), BiomeTerrainValues.dirtDepositMinAltitude2.intValue());
        this.dirtDepositMaxAltitude2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.dirtDepositMaxAltitude2.name(), BiomeTerrainValues.dirtDepositMaxAltitude2.intValue());
        this.dirtDepositRarity3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.dirtDepositRarity3.name(), BiomeTerrainValues.dirtDepositRarity3.intValue());
        this.dirtDepositFrequency3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.dirtDepositFrequency3.name(), BiomeTerrainValues.dirtDepositFrequency3.intValue());
        this.dirtDepositSize3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.dirtDepositSize3.name(), BiomeTerrainValues.dirtDepositSize3.intValue());
        this.dirtDepositMinAltitude3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.dirtDepositMinAltitude3.name(), BiomeTerrainValues.dirtDepositMinAltitude3.intValue());
        this.dirtDepositMaxAltitude3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.dirtDepositMaxAltitude3.name(), BiomeTerrainValues.dirtDepositMaxAltitude3.intValue());
        this.dirtDepositRarity4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.dirtDepositRarity4.name(), BiomeTerrainValues.dirtDepositRarity4.intValue());
        this.dirtDepositFrequency4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.dirtDepositFrequency4.name(), BiomeTerrainValues.dirtDepositFrequency4.intValue());
        this.dirtDepositSize4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.dirtDepositSize4.name(), BiomeTerrainValues.dirtDepositSize4.intValue());
        this.dirtDepositMinAltitude4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.dirtDepositMinAltitude4.name(), BiomeTerrainValues.dirtDepositMinAltitude4.intValue());
        this.dirtDepositMaxAltitude4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.dirtDepositMaxAltitude4.name(), BiomeTerrainValues.dirtDepositMaxAltitude4.intValue());
        this.gravelDepositRarity1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.gravelDepositRarity1.name(), BiomeTerrainValues.gravelDepositRarity1.intValue());
        this.gravelDepositFrequency1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.gravelDepositFrequency1.name(), BiomeTerrainValues.gravelDepositFrequency1.intValue());
        this.gravelDepositSize1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.gravelDepositSize1.name(), BiomeTerrainValues.gravelDepositSize1.intValue());
        this.gravelDepositMinAltitude1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.gravelDepositMinAltitude1.name(), BiomeTerrainValues.gravelDepositMinAltitude1.intValue());
        this.gravelDepositMaxAltitude1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.gravelDepositMaxAltitude1.name(), BiomeTerrainValues.gravelDepositMaxAltitude1.intValue());
        this.gravelDepositRarity2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.gravelDepositRarity2.name(), BiomeTerrainValues.gravelDepositRarity2.intValue());
        this.gravelDepositFrequency2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.gravelDepositFrequency2.name(), BiomeTerrainValues.gravelDepositFrequency2.intValue());
        this.gravelDepositSize2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.gravelDepositSize2.name(), BiomeTerrainValues.gravelDepositSize2.intValue());
        this.gravelDepositMinAltitude2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.gravelDepositMinAltitude2.name(), BiomeTerrainValues.gravelDepositMinAltitude2.intValue());
        this.gravelDepositMaxAltitude2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.gravelDepositMaxAltitude2.name(), BiomeTerrainValues.gravelDepositMaxAltitude2.intValue());
        this.gravelDepositRarity3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.gravelDepositRarity3.name(), BiomeTerrainValues.gravelDepositRarity3.intValue());
        this.gravelDepositFrequency3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.gravelDepositFrequency3.name(), BiomeTerrainValues.gravelDepositFrequency3.intValue());
        this.gravelDepositSize3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.gravelDepositSize3.name(), BiomeTerrainValues.gravelDepositSize3.intValue());
        this.gravelDepositMinAltitude3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.gravelDepositMinAltitude3.name(), BiomeTerrainValues.gravelDepositMinAltitude3.intValue());
        this.gravelDepositMaxAltitude3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.gravelDepositMaxAltitude3.name(), BiomeTerrainValues.gravelDepositMaxAltitude3.intValue());
        this.gravelDepositRarity4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.gravelDepositRarity4.name(), BiomeTerrainValues.gravelDepositRarity4.intValue());
        this.gravelDepositFrequency4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.gravelDepositFrequency4.name(), BiomeTerrainValues.gravelDepositFrequency4.intValue());
        this.gravelDepositSize4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.gravelDepositSize4.name(), BiomeTerrainValues.gravelDepositSize4.intValue());
        this.gravelDepositMinAltitude4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.gravelDepositMinAltitude4.name(), BiomeTerrainValues.gravelDepositMinAltitude4.intValue());
        this.gravelDepositMaxAltitude4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.gravelDepositMaxAltitude4.name(), BiomeTerrainValues.gravelDepositMaxAltitude4.intValue());
        this.clayDepositRarity1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.clayDepositRarity1.name(), BiomeTerrainValues.clayDepositRarity1.intValue());
        this.clayDepositFrequency1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.clayDepositFrequency1.name(), BiomeTerrainValues.clayDepositFrequency1.intValue());
        this.clayDepositSize1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.clayDepositSize1.name(), BiomeTerrainValues.clayDepositSize1.intValue());
        this.clayDepositMinAltitude1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.clayDepositMinAltitude1.name(), BiomeTerrainValues.clayDepositMinAltitude1.intValue());
        this.clayDepositMaxAltitude1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.clayDepositMaxAltitude1.name(), BiomeTerrainValues.clayDepositMaxAltitude1.intValue());
        this.clayDepositRarity2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.clayDepositRarity2.name(), BiomeTerrainValues.clayDepositRarity2.intValue());
        this.clayDepositFrequency2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.clayDepositFrequency2.name(), BiomeTerrainValues.clayDepositFrequency2.intValue());
        this.clayDepositSize2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.clayDepositSize2.name(), BiomeTerrainValues.clayDepositSize2.intValue());
        this.clayDepositMinAltitude2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.clayDepositMinAltitude2.name(), BiomeTerrainValues.clayDepositMinAltitude2.intValue());
        this.clayDepositMaxAltitude2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.clayDepositMaxAltitude2.name(), BiomeTerrainValues.clayDepositMaxAltitude2.intValue());
        this.clayDepositRarity3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.clayDepositRarity3.name(), BiomeTerrainValues.clayDepositRarity3.intValue());
        this.clayDepositFrequency3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.clayDepositFrequency3.name(), BiomeTerrainValues.clayDepositFrequency3.intValue());
        this.clayDepositSize3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.clayDepositSize3.name(), BiomeTerrainValues.clayDepositSize3.intValue());
        this.clayDepositMinAltitude3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.clayDepositMinAltitude3.name(), BiomeTerrainValues.clayDepositMinAltitude3.intValue());
        this.clayDepositMaxAltitude3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.clayDepositMaxAltitude3.name(), BiomeTerrainValues.clayDepositMaxAltitude3.intValue());
        this.clayDepositRarity4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.clayDepositRarity4.name(), BiomeTerrainValues.clayDepositRarity4.intValue());
        this.clayDepositFrequency4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.clayDepositFrequency4.name(), BiomeTerrainValues.clayDepositFrequency4.intValue());
        this.clayDepositSize4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.clayDepositSize4.name(), BiomeTerrainValues.clayDepositSize4.intValue());
        this.clayDepositMinAltitude4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.clayDepositMinAltitude4.name(), BiomeTerrainValues.clayDepositMinAltitude4.intValue());
        this.clayDepositMaxAltitude4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.clayDepositMaxAltitude4.name(), BiomeTerrainValues.clayDepositMaxAltitude4.intValue());
        this.coalDepositRarity1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.coalDepositRarity1.name(), BiomeTerrainValues.coalDepositRarity1.intValue());
        this.coalDepositFrequency1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.coalDepositFrequency1.name(), BiomeTerrainValues.coalDepositFrequency1.intValue());
        this.coalDepositSize1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.coalDepositSize1.name(), BiomeTerrainValues.coalDepositSize1.intValue());
        this.coalDepositMinAltitude1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.coalDepositMinAltitude1.name(), BiomeTerrainValues.coalDepositMinAltitude1.intValue());
        this.coalDepositMaxAltitude1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.coalDepositMaxAltitude1.name(), BiomeTerrainValues.coalDepositMaxAltitude1.intValue());
        this.coalDepositRarity2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.coalDepositRarity2.name(), BiomeTerrainValues.coalDepositRarity2.intValue());
        this.coalDepositFrequency2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.coalDepositFrequency2.name(), BiomeTerrainValues.coalDepositFrequency2.intValue());
        this.coalDepositSize2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.coalDepositSize2.name(), BiomeTerrainValues.coalDepositSize2.intValue());
        this.coalDepositMinAltitude2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.coalDepositMinAltitude2.name(), BiomeTerrainValues.coalDepositMinAltitude2.intValue());
        this.coalDepositMaxAltitude2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.coalDepositMaxAltitude2.name(), BiomeTerrainValues.coalDepositMaxAltitude2.intValue());
        this.coalDepositRarity3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.coalDepositRarity3.name(), BiomeTerrainValues.coalDepositRarity3.intValue());
        this.coalDepositFrequency3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.coalDepositFrequency3.name(), BiomeTerrainValues.coalDepositFrequency3.intValue());
        this.coalDepositSize3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.coalDepositSize3.name(), BiomeTerrainValues.coalDepositSize3.intValue());
        this.coalDepositMinAltitude3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.coalDepositMinAltitude3.name(), BiomeTerrainValues.coalDepositMinAltitude3.intValue());
        this.coalDepositMaxAltitude3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.coalDepositMaxAltitude3.name(), BiomeTerrainValues.coalDepositMaxAltitude3.intValue());
        this.coalDepositRarity4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.coalDepositRarity4.name(), BiomeTerrainValues.coalDepositRarity4.intValue());
        this.coalDepositFrequency4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.coalDepositFrequency4.name(), BiomeTerrainValues.coalDepositFrequency4.intValue());
        this.coalDepositSize4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.coalDepositSize4.name(), BiomeTerrainValues.coalDepositSize4.intValue());
        this.coalDepositMinAltitude4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.coalDepositMinAltitude4.name(), BiomeTerrainValues.coalDepositMinAltitude4.intValue());
        this.coalDepositMaxAltitude4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.coalDepositMaxAltitude4.name(), BiomeTerrainValues.coalDepositMaxAltitude4.intValue());
        this.ironDepositRarity1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.ironDepositRarity1.name(), BiomeTerrainValues.ironDepositRarity1.intValue());
        this.ironDepositFrequency1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.ironDepositFrequency1.name(), BiomeTerrainValues.ironDepositFrequency1.intValue());
        this.ironDepositSize1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.ironDepositSize1.name(), BiomeTerrainValues.ironDepositSize1.intValue());
        this.ironDepositMinAltitude1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.ironDepositMinAltitude1.name(), BiomeTerrainValues.ironDepositMinAltitude1.intValue());
        this.ironDepositMaxAltitude1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.ironDepositMaxAltitude1.name(), BiomeTerrainValues.ironDepositMaxAltitude1.intValue());
        this.ironDepositRarity2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.ironDepositRarity2.name(), BiomeTerrainValues.ironDepositRarity2.intValue());
        this.ironDepositFrequency2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.ironDepositFrequency2.name(), BiomeTerrainValues.ironDepositFrequency2.intValue());
        this.ironDepositSize2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.ironDepositSize2.name(), BiomeTerrainValues.ironDepositSize2.intValue());
        this.ironDepositMinAltitude2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.ironDepositMinAltitude2.name(), BiomeTerrainValues.ironDepositMinAltitude2.intValue());
        this.ironDepositMaxAltitude2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.ironDepositMaxAltitude2.name(), BiomeTerrainValues.ironDepositMaxAltitude2.intValue());
        this.ironDepositRarity3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.ironDepositRarity3.name(), BiomeTerrainValues.ironDepositRarity3.intValue());
        this.ironDepositFrequency3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.ironDepositFrequency3.name(), BiomeTerrainValues.ironDepositFrequency3.intValue());
        this.ironDepositSize3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.ironDepositSize3.name(), BiomeTerrainValues.ironDepositSize3.intValue());
        this.ironDepositMinAltitude3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.ironDepositMinAltitude3.name(), BiomeTerrainValues.ironDepositMinAltitude3.intValue());
        this.ironDepositMaxAltitude3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.ironDepositMaxAltitude3.name(), BiomeTerrainValues.ironDepositMaxAltitude3.intValue());
        this.ironDepositRarity4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.ironDepositRarity4.name(), BiomeTerrainValues.ironDepositRarity4.intValue());
        this.ironDepositFrequency4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.ironDepositFrequency4.name(), BiomeTerrainValues.ironDepositFrequency4.intValue());
        this.ironDepositSize4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.ironDepositSize4.name(), BiomeTerrainValues.ironDepositSize4.intValue());
        this.ironDepositMinAltitude4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.ironDepositMinAltitude4.name(), BiomeTerrainValues.ironDepositMinAltitude4.intValue());
        this.ironDepositMaxAltitude4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.ironDepositMaxAltitude4.name(), BiomeTerrainValues.ironDepositMaxAltitude4.intValue());
        this.goldDepositRarity1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.goldDepositRarity1.name(), BiomeTerrainValues.goldDepositRarity1.intValue());
        this.goldDepositFrequency1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.goldDepositFrequency1.name(), BiomeTerrainValues.goldDepositFrequency1.intValue());
        this.goldDepositSize1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.goldDepositSize1.name(), BiomeTerrainValues.goldDepositSize1.intValue());
        this.goldDepositMinAltitude1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.goldDepositMinAltitude1.name(), BiomeTerrainValues.goldDepositMinAltitude1.intValue());
        this.goldDepositMaxAltitude1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.goldDepositMaxAltitude1.name(), BiomeTerrainValues.goldDepositMaxAltitude1.intValue());
        this.goldDepositRarity2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.goldDepositRarity2.name(), BiomeTerrainValues.goldDepositRarity2.intValue());
        this.goldDepositFrequency2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.goldDepositFrequency2.name(), BiomeTerrainValues.goldDepositFrequency2.intValue());
        this.goldDepositSize2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.goldDepositSize2.name(), BiomeTerrainValues.goldDepositSize2.intValue());
        this.goldDepositMinAltitude2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.goldDepositMinAltitude2.name(), BiomeTerrainValues.goldDepositMinAltitude2.intValue());
        this.goldDepositMaxAltitude2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.goldDepositMaxAltitude2.name(), BiomeTerrainValues.goldDepositMaxAltitude2.intValue());
        this.goldDepositRarity3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.goldDepositRarity3.name(), BiomeTerrainValues.goldDepositRarity3.intValue());
        this.goldDepositFrequency3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.goldDepositFrequency3.name(), BiomeTerrainValues.goldDepositFrequency3.intValue());
        this.goldDepositSize3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.goldDepositSize3.name(), BiomeTerrainValues.goldDepositSize3.intValue());
        this.goldDepositMinAltitude3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.goldDepositMinAltitude3.name(), BiomeTerrainValues.goldDepositMinAltitude3.intValue());
        this.goldDepositMaxAltitude3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.goldDepositMaxAltitude3.name(), BiomeTerrainValues.goldDepositMaxAltitude3.intValue());
        this.goldDepositRarity4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.goldDepositRarity4.name(), BiomeTerrainValues.goldDepositRarity4.intValue());
        this.goldDepositFrequency4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.goldDepositFrequency4.name(), BiomeTerrainValues.goldDepositFrequency4.intValue());
        this.goldDepositSize4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.goldDepositSize4.name(), BiomeTerrainValues.goldDepositSize4.intValue());
        this.goldDepositMinAltitude4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.goldDepositMinAltitude4.name(), BiomeTerrainValues.goldDepositMinAltitude4.intValue());
        this.goldDepositMaxAltitude4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.goldDepositMaxAltitude4.name(), BiomeTerrainValues.goldDepositMaxAltitude4.intValue());
        this.redstoneDepositRarity1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.redstoneDepositRarity1.name(), BiomeTerrainValues.redstoneDepositRarity1.intValue());
        this.redstoneDepositFrequency1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.redstoneDepositFrequency1.name(), BiomeTerrainValues.redstoneDepositFrequency1.intValue());
        this.redstoneDepositSize1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.redstoneDepositSize1.name(), BiomeTerrainValues.redstoneDepositSize1.intValue());
        this.redstoneDepositMinAltitude1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.redstoneDepositMinAltitude1.name(), BiomeTerrainValues.redstoneDepositMinAltitude1.intValue());
        this.redstoneDepositMaxAltitude1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.redstoneDepositMaxAltitude1.name(), BiomeTerrainValues.redstoneDepositMaxAltitude1.intValue());
        this.redstoneDepositRarity2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.redstoneDepositRarity2.name(), BiomeTerrainValues.redstoneDepositRarity2.intValue());
        this.redstoneDepositFrequency2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.redstoneDepositFrequency2.name(), BiomeTerrainValues.redstoneDepositFrequency2.intValue());
        this.redstoneDepositSize2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.redstoneDepositSize2.name(), BiomeTerrainValues.redstoneDepositSize2.intValue());
        this.redstoneDepositMinAltitude2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.redstoneDepositMinAltitude2.name(), BiomeTerrainValues.redstoneDepositMinAltitude2.intValue());
        this.redstoneDepositMaxAltitude2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.redstoneDepositMaxAltitude2.name(), BiomeTerrainValues.redstoneDepositMaxAltitude2.intValue());
        this.redstoneDepositRarity3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.redstoneDepositRarity3.name(), BiomeTerrainValues.redstoneDepositRarity3.intValue());
        this.redstoneDepositFrequency3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.redstoneDepositFrequency3.name(), BiomeTerrainValues.redstoneDepositFrequency3.intValue());
        this.redstoneDepositSize3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.redstoneDepositSize3.name(), BiomeTerrainValues.redstoneDepositSize3.intValue());
        this.redstoneDepositMinAltitude3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.redstoneDepositMinAltitude3.name(), BiomeTerrainValues.redstoneDepositMinAltitude3.intValue());
        this.redstoneDepositMaxAltitude3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.redstoneDepositMaxAltitude3.name(), BiomeTerrainValues.redstoneDepositMaxAltitude3.intValue());
        this.redstoneDepositRarity4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.redstoneDepositRarity4.name(), BiomeTerrainValues.redstoneDepositRarity4.intValue());
        this.redstoneDepositFrequency4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.redstoneDepositFrequency4.name(), BiomeTerrainValues.redstoneDepositFrequency4.intValue());
        this.redstoneDepositSize4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.redstoneDepositSize4.name(), BiomeTerrainValues.redstoneDepositSize4.intValue());
        this.redstoneDepositMinAltitude4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.redstoneDepositMinAltitude4.name(), BiomeTerrainValues.redstoneDepositMinAltitude4.intValue());
        this.redstoneDepositMaxAltitude4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.redstoneDepositMaxAltitude4.name(), BiomeTerrainValues.redstoneDepositMaxAltitude4.intValue());
        this.diamondDepositRarity1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.diamondDepositRarity1.name(), BiomeTerrainValues.diamondDepositRarity1.intValue());
        this.diamondDepositFrequency1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.diamondDepositFrequency1.name(), BiomeTerrainValues.diamondDepositFrequency1.intValue());
        this.diamondDepositSize1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.diamondDepositSize1.name(), BiomeTerrainValues.diamondDepositSize1.intValue());
        this.diamondDepositMinAltitude1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.diamondDepositMinAltitude1.name(), BiomeTerrainValues.diamondDepositMinAltitude1.intValue());
        this.diamondDepositMaxAltitude1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.diamondDepositMaxAltitude1.name(), BiomeTerrainValues.diamondDepositMaxAltitude1.intValue());
        this.diamondDepositRarity2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.diamondDepositRarity2.name(), BiomeTerrainValues.diamondDepositRarity2.intValue());
        this.diamondDepositFrequency2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.diamondDepositFrequency2.name(), BiomeTerrainValues.diamondDepositFrequency2.intValue());
        this.diamondDepositSize2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.diamondDepositSize2.name(), BiomeTerrainValues.diamondDepositSize2.intValue());
        this.diamondDepositMinAltitude2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.diamondDepositMinAltitude2.name(), BiomeTerrainValues.diamondDepositMinAltitude2.intValue());
        this.diamondDepositMaxAltitude2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.diamondDepositMaxAltitude2.name(), BiomeTerrainValues.diamondDepositMaxAltitude2.intValue());
        this.diamondDepositRarity3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.diamondDepositRarity3.name(), BiomeTerrainValues.diamondDepositRarity3.intValue());
        this.diamondDepositFrequency3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.diamondDepositFrequency3.name(), BiomeTerrainValues.diamondDepositFrequency3.intValue());
        this.diamondDepositSize3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.diamondDepositSize3.name(), BiomeTerrainValues.diamondDepositSize3.intValue());
        this.diamondDepositMinAltitude3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.diamondDepositMinAltitude3.name(), BiomeTerrainValues.diamondDepositMinAltitude3.intValue());
        this.diamondDepositMaxAltitude3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.diamondDepositMaxAltitude3.name(), BiomeTerrainValues.diamondDepositMaxAltitude3.intValue());
        this.diamondDepositRarity4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.diamondDepositRarity4.name(), BiomeTerrainValues.diamondDepositRarity4.intValue());
        this.diamondDepositFrequency4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.diamondDepositFrequency4.name(), BiomeTerrainValues.diamondDepositFrequency4.intValue());
        this.diamondDepositSize4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.diamondDepositSize4.name(), BiomeTerrainValues.diamondDepositSize4.intValue());
        this.diamondDepositMinAltitude4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.diamondDepositMinAltitude4.name(), BiomeTerrainValues.diamondDepositMinAltitude4.intValue());
        this.diamondDepositMaxAltitude4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.diamondDepositMaxAltitude4.name(), BiomeTerrainValues.diamondDepositMaxAltitude4.intValue());
        this.lapislazuliDepositRarity1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lapislazuliDepositRarity1.name(), BiomeTerrainValues.lapislazuliDepositRarity1.intValue());
        this.lapislazuliDepositFrequency1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lapislazuliDepositFrequency1.name(), BiomeTerrainValues.lapislazuliDepositFrequency1.intValue());
        this.lapislazuliDepositSize1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lapislazuliDepositSize1.name(), BiomeTerrainValues.lapislazuliDepositSize1.intValue());
        this.lapislazuliDepositMinAltitude1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lapislazuliDepositMinAltitude1.name(), BiomeTerrainValues.lapislazuliDepositMinAltitude1.intValue());
        this.lapislazuliDepositMaxAltitude1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lapislazuliDepositMaxAltitude1.name(), BiomeTerrainValues.lapislazuliDepositMaxAltitude1.intValue());
        this.lapislazuliDepositRarity2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lapislazuliDepositRarity2.name(), BiomeTerrainValues.lapislazuliDepositRarity2.intValue());
        this.lapislazuliDepositFrequency2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lapislazuliDepositFrequency2.name(), BiomeTerrainValues.lapislazuliDepositFrequency2.intValue());
        this.lapislazuliDepositSize2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lapislazuliDepositSize2.name(), BiomeTerrainValues.lapislazuliDepositSize2.intValue());
        this.lapislazuliDepositMinAltitude2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lapislazuliDepositMinAltitude2.name(), BiomeTerrainValues.lapislazuliDepositMinAltitude2.intValue());
        this.lapislazuliDepositMaxAltitude2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lapislazuliDepositMaxAltitude2.name(), BiomeTerrainValues.lapislazuliDepositMaxAltitude2.intValue());
        this.lapislazuliDepositRarity3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lapislazuliDepositRarity3.name(), BiomeTerrainValues.lapislazuliDepositRarity3.intValue());
        this.lapislazuliDepositFrequency3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lapislazuliDepositFrequency3.name(), BiomeTerrainValues.lapislazuliDepositFrequency3.intValue());
        this.lapislazuliDepositSize3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lapislazuliDepositSize3.name(), BiomeTerrainValues.lapislazuliDepositSize3.intValue());
        this.lapislazuliDepositMinAltitude3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lapislazuliDepositMinAltitude3.name(), BiomeTerrainValues.lapislazuliDepositMinAltitude3.intValue());
        this.lapislazuliDepositMaxAltitude3 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lapislazuliDepositMaxAltitude3.name(), BiomeTerrainValues.lapislazuliDepositMaxAltitude3.intValue());
        this.lapislazuliDepositRarity4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lapislazuliDepositRarity4.name(), BiomeTerrainValues.lapislazuliDepositRarity4.intValue());
        this.lapislazuliDepositFrequency4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lapislazuliDepositFrequency4.name(), BiomeTerrainValues.lapislazuliDepositFrequency4.intValue());
        this.lapislazuliDepositSize4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lapislazuliDepositSize4.name(), BiomeTerrainValues.lapislazuliDepositSize4.intValue());
        this.lapislazuliDepositMinAltitude4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lapislazuliDepositMinAltitude4.name(), BiomeTerrainValues.lapislazuliDepositMinAltitude4.intValue());
        this.lapislazuliDepositMaxAltitude4 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lapislazuliDepositMaxAltitude4.name(), BiomeTerrainValues.lapislazuliDepositMaxAltitude4.intValue());

        // this.evenFireHellDepositDistribution =
        // this.worldWrk.ReadModSettins(BiomeTerrainValues.evenFireHellDepositDistribution.name(),
        // BiomeTerrainValues.evenFireHellDepositDistribution.booleanValue().booleanValue());

        // this.evenLightstoneHellDepositDistribution =
        // this.worldWrk.ReadModSettins(BiomeTerrainValues.evenLightstoneHellDepositDistribution.name(),
        // BiomeTerrainValues.evenLightstoneHellDepositDistribution.booleanValue().booleanValue());

        this.lavaSourceHellDepositRarity = this.worldWrk.ReadModSettins(BiomeTerrainValues.lavaSourceHellDepositRarity.name(), BiomeTerrainValues.lavaSourceHellDepositRarity.intValue());
        this.lavaSourceHellDepositFrequency = this.worldWrk.ReadModSettins(BiomeTerrainValues.lavaSourceHellDepositFrequency.name(), BiomeTerrainValues.lavaSourceHellDepositFrequency.intValue());
        this.lavaSourceHellDepositMinAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.lavaSourceHellDepositMinAltitude.name(), BiomeTerrainValues.lavaSourceHellDepositMinAltitude.intValue());
        this.lavaSourceHellDepositMaxAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.lavaSourceHellDepositMaxAltitude.name(), BiomeTerrainValues.lavaSourceHellDepositMaxAltitude.intValue());
        this.fireHellDepositRarity = this.worldWrk.ReadModSettins(BiomeTerrainValues.fireHellDepositRarity.name(), BiomeTerrainValues.fireHellDepositRarity.intValue());
        this.fireHellDepositFrequency = this.worldWrk.ReadModSettins(BiomeTerrainValues.fireHellDepositFrequency.name(), BiomeTerrainValues.fireHellDepositFrequency.intValue());
        this.fireHellDepositMinAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.fireHellDepositMinAltitude.name(), BiomeTerrainValues.fireHellDepositMinAltitude.intValue());
        this.fireHellDepositMaxAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.fireHellDepositMaxAltitude.name(), BiomeTerrainValues.fireHellDepositMaxAltitude.intValue());
        this.lightstoneHellDepositRarity1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lightstoneHellDepositRarity1.name(), BiomeTerrainValues.lightstoneHellDepositRarity1.intValue());
        this.lightstoneHellDepositFrequency1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lightstoneHellDepositFrequency1.name(), BiomeTerrainValues.lightstoneHellDepositFrequency1.intValue());
        this.lightstoneHellDepositMinAltitude1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lightstoneHellDepositMinAltitude1.name(), BiomeTerrainValues.lightstoneHellDepositMinAltitude1.intValue());
        this.lightstoneHellDepositMaxAltitude1 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lightstoneHellDepositMaxAltitude1.name(), BiomeTerrainValues.lightstoneHellDepositMaxAltitude1.intValue());
        this.lightstoneHellDepositRarity2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lightstoneHellDepositRarity2.name(), BiomeTerrainValues.lightstoneHellDepositRarity2.intValue());
        this.lightstoneHellDepositFrequency2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lightstoneHellDepositFrequency2.name(), BiomeTerrainValues.lightstoneHellDepositFrequency2.intValue());
        this.lightstoneHellDepositMinAltitude2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lightstoneHellDepositMinAltitude2.name(), BiomeTerrainValues.lightstoneHellDepositMinAltitude2.intValue());
        this.lightstoneHellDepositMaxAltitude2 = this.worldWrk.ReadModSettins(BiomeTerrainValues.lightstoneHellDepositMaxAltitude2.name(), BiomeTerrainValues.lightstoneHellDepositMaxAltitude2.intValue());
        this.brownMushroomHellDepositRarity = this.worldWrk.ReadModSettins(BiomeTerrainValues.brownMushroomHellDepositRarity.name(), BiomeTerrainValues.brownMushroomHellDepositRarity.intValue());
        this.brownMushroomHellDepositFrequency = this.worldWrk.ReadModSettins(BiomeTerrainValues.brownMushroomHellDepositFrequency.name(), BiomeTerrainValues.brownMushroomHellDepositFrequency.intValue());
        this.brownMushroomHellDepositMinAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.brownMushroomHellDepositMinAltitude.name(), BiomeTerrainValues.brownMushroomHellDepositMinAltitude.intValue());
        this.brownMushroomHellDepositMaxAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.brownMushroomHellDepositMaxAltitude.name(), BiomeTerrainValues.brownMushroomHellDepositMaxAltitude.intValue());
        this.redMushroomHellDepositRarity = this.worldWrk.ReadModSettins(BiomeTerrainValues.redMushroomHellDepositRarity.name(), BiomeTerrainValues.redMushroomHellDepositRarity.intValue());
        this.redMushroomHellDepositFrequency = this.worldWrk.ReadModSettins(BiomeTerrainValues.redMushroomHellDepositFrequency.name(), BiomeTerrainValues.redMushroomHellDepositFrequency.intValue());
        this.redMushroomHellDepositMinAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.redMushroomHellDepositMinAltitude.name(), BiomeTerrainValues.redMushroomHellDepositMinAltitude.intValue());
        this.redMushroomHellDepositMaxAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.redMushroomHellDepositMaxAltitude.name(), BiomeTerrainValues.redMushroomHellDepositMaxAltitude.intValue());

        this.disableNotchPonds = this.worldWrk.ReadModSettins(BiomeTerrainValues.disableNotchPonds.name(), BiomeTerrainValues.disableNotchPonds.booleanValue());

        this.customObjects = this.worldWrk.ReadModSettins(BiomeTerrainValues.customObjects.name(), BiomeTerrainValues.customObjects.booleanValue());
        this.objectSpawnRatio = this.worldWrk.ReadModSettins(BiomeTerrainValues.objectSpawnRatio.name(), BiomeTerrainValues.objectSpawnRatio.intValue());
        this.notchBiomeTrees = this.worldWrk.ReadModSettins(BiomeTerrainValues.notchBiomeTrees.name(), BiomeTerrainValues.notchBiomeTrees.booleanValue());
        this.globalTreeDensity = this.worldWrk.ReadModSettins(BiomeTerrainValues.globalTreeDensity.name(), BiomeTerrainValues.globalTreeDensity.intValue());
        this.rainforestTreeDensity = this.worldWrk.ReadModSettins(BiomeTerrainValues.rainforestTreeDensity.name(), BiomeTerrainValues.rainforestTreeDensity.intValue());
        this.swamplandTreeDensity = this.worldWrk.ReadModSettins(BiomeTerrainValues.swamplandTreeDensity.name(), BiomeTerrainValues.swamplandTreeDensity.intValue());
        this.seasonalforestTreeDensity = this.worldWrk.ReadModSettins(BiomeTerrainValues.seasonalforestTreeDensity.name(), BiomeTerrainValues.seasonalforestTreeDensity.intValue());
        this.forestTreeDensity = this.worldWrk.ReadModSettins(BiomeTerrainValues.forestTreeDensity.name(), BiomeTerrainValues.forestTreeDensity.intValue());
        this.savannaTreeDensity = this.worldWrk.ReadModSettins(BiomeTerrainValues.savannaTreeDensity.name(), BiomeTerrainValues.savannaTreeDensity.intValue());
        this.shrublandTreeDensity = this.worldWrk.ReadModSettins(BiomeTerrainValues.shrublandTreeDensity.name(), BiomeTerrainValues.shrublandTreeDensity.intValue());
        this.taigaTreeDensity = this.worldWrk.ReadModSettins(BiomeTerrainValues.taigaTreeDensity.name(), BiomeTerrainValues.taigaTreeDensity.intValue());
        this.desertTreeDensity = this.worldWrk.ReadModSettins(BiomeTerrainValues.desertTreeDensity.name(), BiomeTerrainValues.desertTreeDensity.intValue());
        this.plainsTreeDensity = this.worldWrk.ReadModSettins(BiomeTerrainValues.plainsTreeDensity.name(), BiomeTerrainValues.plainsTreeDensity.intValue());
        this.iceDesertTreeDensity = this.worldWrk.ReadModSettins(BiomeTerrainValues.iceDesertTreeDensity.name(), BiomeTerrainValues.iceDesertTreeDensity.intValue());
        this.tundraTreeDensity = this.worldWrk.ReadModSettins(BiomeTerrainValues.tundraTreeDensity.name(), BiomeTerrainValues.tundraTreeDensity.intValue());
        this.globalCactusDensity = this.worldWrk.ReadModSettins(BiomeTerrainValues.globalCactusDensity.name(), BiomeTerrainValues.globalCactusDensity.intValue());
        this.desertCactusDensity = this.worldWrk.ReadModSettins(BiomeTerrainValues.desertCactusDensity.name(), BiomeTerrainValues.desertCactusDensity.intValue());
        this.cactusDepositRarity = this.worldWrk.ReadModSettins(BiomeTerrainValues.cactusDepositRarity.name(), BiomeTerrainValues.cactusDepositRarity.intValue());
        this.cactusDepositMinAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.cactusDepositMinAltitude.name(), BiomeTerrainValues.cactusDepositMinAltitude.intValue());
        this.cactusDepositMaxAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.cactusDepositMaxAltitude.name(), BiomeTerrainValues.cactusDepositMaxAltitude.intValue());

        this.dungeonRarity = this.worldWrk.ReadModSettins(BiomeTerrainValues.dungeonRarity.name(), BiomeTerrainValues.dungeonRarity.intValue());
        this.dungeonFrequency = this.worldWrk.ReadModSettins(BiomeTerrainValues.dungeonFrequency.name(), BiomeTerrainValues.dungeonFrequency.intValue());
        this.dungeonMinAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.dungeonMinAltitude.name(), BiomeTerrainValues.dungeonMinAltitude.intValue());
        this.dungeonMaxAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.dungeonMaxAltitude.name(), BiomeTerrainValues.dungeonMaxAltitude.intValue());


        this.lavaLevelMin = this.worldWrk.ReadModSettins(BiomeTerrainValues.lavaLevelMin.name(), BiomeTerrainValues.lavaLevelMin.intValue());
        this.lavaLevelMax = this.worldWrk.ReadModSettins(BiomeTerrainValues.lavaLevelMax.name(), BiomeTerrainValues.lavaLevelMax.intValue());

        this.undergroundLakes = this.worldWrk.ReadModSettins(BiomeTerrainValues.undergroundLakes.name(), BiomeTerrainValues.undergroundLakes.booleanValue());
        this.undergroundLakesInAir = this.worldWrk.ReadModSettins(BiomeTerrainValues.undergroundLakesInAir.name(), BiomeTerrainValues.undergroundLakesInAir.booleanValue());
        this.undergroundLakeFrequency = this.worldWrk.ReadModSettins(BiomeTerrainValues.undergroundLakeFrequency.name(), BiomeTerrainValues.undergroundLakeFrequency.intValue());
        this.undergroundLakeRarity = this.worldWrk.ReadModSettins(BiomeTerrainValues.undergroundLakeRarity.name(), BiomeTerrainValues.undergroundLakeRarity.intValue());
        this.undergroundLakeMinSize = this.worldWrk.ReadModSettins(BiomeTerrainValues.undergroundLakeMinSize.name(), BiomeTerrainValues.undergroundLakeMinSize.intValue());
        this.undergroundLakeMaxSize = this.worldWrk.ReadModSettins(BiomeTerrainValues.undergroundLakeMaxSize.name(), BiomeTerrainValues.undergroundLakeMaxSize.intValue());
        this.undergroundLakeMinAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.undergroundLakeMinAltitude.name(), BiomeTerrainValues.undergroundLakeMinAltitude.intValue());
        this.undergroundLakeMaxAltitude = this.worldWrk.ReadModSettins(BiomeTerrainValues.undergroundLakeMaxAltitude.name(), BiomeTerrainValues.undergroundLakeMaxAltitude.intValue());

    }

    public void fixSettingsValues()
    {
        this.flowerDepositRarity = (this.flowerDepositRarity < 0 ? 0 : this.flowerDepositRarity > 100 ? 100 : this.flowerDepositRarity);
        this.flowerDepositFrequency = (this.flowerDepositFrequency < 0 ? 0 : this.flowerDepositFrequency);
        this.flowerDepositMinAltitude = (this.flowerDepositMinAltitude < 0 ? 0 : this.flowerDepositMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.flowerDepositMinAltitude);
        this.flowerDepositMaxAltitude = (this.flowerDepositMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.flowerDepositMaxAltitude <= this.flowerDepositMinAltitude ? this.flowerDepositMinAltitude + 1 : this.flowerDepositMaxAltitude);
        this.roseDepositRarity = (this.roseDepositRarity < 0 ? 0 : this.roseDepositRarity > 100 ? 100 : this.roseDepositRarity);
        this.roseDepositFrequency = (this.roseDepositFrequency < 0 ? 0 : this.roseDepositFrequency);
        this.roseDepositMinAltitude = (this.roseDepositMinAltitude < 0 ? 0 : this.roseDepositMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.roseDepositMinAltitude);
        this.roseDepositMaxAltitude = (this.roseDepositMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.roseDepositMaxAltitude <= this.roseDepositMinAltitude ? this.roseDepositMinAltitude + 1 : this.roseDepositMaxAltitude);
        this.brownMushroomDepositRarity = (this.brownMushroomDepositRarity < 0 ? 0 : this.brownMushroomDepositRarity > 100 ? 100 : this.brownMushroomDepositRarity);
        this.brownMushroomDepositFrequency = (this.brownMushroomDepositFrequency < 0 ? 0 : this.brownMushroomDepositFrequency);
        this.brownMushroomDepositMinAltitude = (this.brownMushroomDepositMinAltitude < 0 ? 0 : this.brownMushroomDepositMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.brownMushroomDepositMinAltitude);
        this.brownMushroomDepositMaxAltitude = (this.brownMushroomDepositMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.brownMushroomDepositMaxAltitude <= this.brownMushroomDepositMinAltitude ? this.brownMushroomDepositMinAltitude + 1 : this.brownMushroomDepositMaxAltitude);
        this.redMushroomDepositRarity = (this.redMushroomDepositRarity < 0 ? 0 : this.redMushroomDepositRarity > 100 ? 100 : this.redMushroomDepositRarity);
        this.redMushroomDepositFrequency = (this.redMushroomDepositFrequency < 0 ? 0 : this.redMushroomDepositFrequency);
        this.redMushroomDepositMinAltitude = (this.redMushroomDepositMinAltitude < 0 ? 0 : this.redMushroomDepositMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.redMushroomDepositMinAltitude);
        this.redMushroomDepositMaxAltitude = (this.redMushroomDepositMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.redMushroomDepositMaxAltitude <= this.redMushroomDepositMinAltitude ? this.redMushroomDepositMinAltitude + 1 : this.redMushroomDepositMaxAltitude);
        this.reedDepositRarity = (this.reedDepositRarity < 0 ? 0 : this.reedDepositRarity > 100 ? 100 : this.reedDepositRarity);
        this.reedDepositFrequency = (this.reedDepositFrequency < 0 ? 0 : this.reedDepositFrequency);
        this.reedDepositMinAltitude = (this.reedDepositMinAltitude < 0 ? 0 : this.reedDepositMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.reedDepositMinAltitude);
        this.reedDepositMaxAltitude = (this.reedDepositMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.reedDepositMaxAltitude <= this.reedDepositMinAltitude ? this.reedDepositMinAltitude + 1 : this.reedDepositMaxAltitude);
        this.pumpkinDepositRarity = (this.pumpkinDepositRarity < 0 ? 0 : this.pumpkinDepositRarity > 100 ? 100 : this.pumpkinDepositRarity);
        this.pumpkinDepositFrequency = (this.pumpkinDepositFrequency < 0 ? 0 : this.pumpkinDepositFrequency);
        this.pumpkinDepositMinAltitude = (this.pumpkinDepositMinAltitude < 0 ? 0 : this.pumpkinDepositMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.pumpkinDepositMinAltitude);
        this.pumpkinDepositMaxAltitude = (this.pumpkinDepositMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.pumpkinDepositMaxAltitude <= this.pumpkinDepositMinAltitude ? this.pumpkinDepositMinAltitude + 1 : this.pumpkinDepositMaxAltitude);

        this.waterSourceDepositRarity = (this.waterSourceDepositRarity < 0 ? 0 : this.waterSourceDepositRarity > 100 ? 100 : this.waterSourceDepositRarity);
        this.waterSourceDepositFrequency = (this.waterSourceDepositFrequency < 0 ? 0 : this.waterSourceDepositFrequency);
        this.waterSourceDepositMinAltitude = (this.waterSourceDepositMinAltitude < 0 ? 0 : this.waterSourceDepositMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.waterSourceDepositMinAltitude);
        this.waterSourceDepositMaxAltitude = (this.waterSourceDepositMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.waterSourceDepositMaxAltitude <= this.waterSourceDepositMinAltitude ? this.waterSourceDepositMinAltitude + 1 : this.waterSourceDepositMaxAltitude);
        this.lavaSourceDepositRarity = (this.lavaSourceDepositRarity < 0 ? 0 : this.lavaSourceDepositRarity > 100 ? 100 : this.lavaSourceDepositRarity);
        this.lavaSourceDepositFrequency = (this.lavaSourceDepositFrequency < 0 ? 0 : this.lavaSourceDepositFrequency);
        this.lavaSourceDepositMinAltitude = (this.lavaSourceDepositMinAltitude < 0 ? 0 : this.lavaSourceDepositMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.lavaSourceDepositMinAltitude);
        this.lavaSourceDepositMaxAltitude = (this.lavaSourceDepositMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.lavaSourceDepositMaxAltitude <= this.lavaSourceDepositMinAltitude ? this.lavaSourceDepositMinAltitude + 1 : this.lavaSourceDepositMaxAltitude);

        this.dirtDepositRarity1 = (this.dirtDepositRarity1 < 0 ? 0 : this.dirtDepositRarity1 > 100 ? 100 : this.dirtDepositRarity1);
        this.dirtDepositFrequency1 = (this.dirtDepositFrequency1 < 0 ? 0 : this.dirtDepositFrequency1);
        this.dirtDepositSize1 = (this.dirtDepositSize1 < 0 ? 0 : this.dirtDepositSize1);
        this.dirtDepositMinAltitude1 = (this.dirtDepositMinAltitude1 < 0 ? 0 : this.dirtDepositMinAltitude1 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.dirtDepositMinAltitude1);
        this.dirtDepositMaxAltitude1 = (this.dirtDepositMaxAltitude1 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.dirtDepositMaxAltitude1 <= this.dirtDepositMinAltitude1 ? this.dirtDepositMinAltitude1 + 1 : this.dirtDepositMaxAltitude1);
        this.dirtDepositRarity2 = (this.dirtDepositRarity2 < 0 ? 0 : this.dirtDepositRarity2 > 100 ? 100 : this.dirtDepositRarity2);
        this.dirtDepositFrequency2 = (this.dirtDepositFrequency2 < 0 ? 0 : this.dirtDepositFrequency2);
        this.dirtDepositSize2 = (this.dirtDepositSize2 < 0 ? 0 : this.dirtDepositSize2);
        this.dirtDepositMinAltitude2 = (this.dirtDepositMinAltitude2 < 0 ? 0 : this.dirtDepositMinAltitude2 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.dirtDepositMinAltitude2);
        this.dirtDepositMaxAltitude2 = (this.dirtDepositMaxAltitude2 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.dirtDepositMaxAltitude2 <= this.dirtDepositMinAltitude2 ? this.dirtDepositMinAltitude2 + 1 : this.dirtDepositMaxAltitude2);
        this.dirtDepositRarity3 = (this.dirtDepositRarity3 < 0 ? 0 : this.dirtDepositRarity3 > 100 ? 100 : this.dirtDepositRarity3);
        this.dirtDepositFrequency3 = (this.dirtDepositFrequency3 < 0 ? 0 : this.dirtDepositFrequency3);
        this.dirtDepositSize3 = (this.dirtDepositSize3 < 0 ? 0 : this.dirtDepositSize3);
        this.dirtDepositMinAltitude3 = (this.dirtDepositMinAltitude3 < 0 ? 0 : this.dirtDepositMinAltitude3 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.dirtDepositMinAltitude3);
        this.dirtDepositMaxAltitude3 = (this.dirtDepositMaxAltitude3 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.dirtDepositMaxAltitude3 <= this.dirtDepositMinAltitude3 ? this.dirtDepositMinAltitude3 + 1 : this.dirtDepositMaxAltitude3);
        this.dirtDepositRarity4 = (this.dirtDepositRarity4 < 0 ? 0 : this.dirtDepositRarity4 > 100 ? 100 : this.dirtDepositRarity4);
        this.dirtDepositFrequency4 = (this.dirtDepositFrequency4 < 0 ? 0 : this.dirtDepositFrequency4);
        this.dirtDepositSize4 = (this.dirtDepositSize4 < 0 ? 0 : this.dirtDepositSize4);
        this.dirtDepositMinAltitude4 = (this.dirtDepositMinAltitude4 < 0 ? 0 : this.dirtDepositMinAltitude4 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.dirtDepositMinAltitude4);
        this.dirtDepositMaxAltitude4 = (this.dirtDepositMaxAltitude4 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.dirtDepositMaxAltitude4 <= this.dirtDepositMinAltitude4 ? this.dirtDepositMinAltitude4 + 1 : this.dirtDepositMaxAltitude4);
        this.gravelDepositRarity1 = (this.gravelDepositRarity1 < 0 ? 0 : this.gravelDepositRarity1 > 100 ? 100 : this.gravelDepositRarity1);
        this.gravelDepositFrequency1 = (this.gravelDepositFrequency1 < 0 ? 0 : this.gravelDepositFrequency1);
        this.gravelDepositSize1 = (this.gravelDepositSize1 < 0 ? 0 : this.gravelDepositSize1);
        this.gravelDepositMinAltitude1 = (this.gravelDepositMinAltitude1 < 0 ? 0 : this.gravelDepositMinAltitude1 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.gravelDepositMinAltitude1);
        this.gravelDepositMaxAltitude1 = (this.gravelDepositMaxAltitude1 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.gravelDepositMaxAltitude1 <= this.gravelDepositMinAltitude1 ? this.gravelDepositMinAltitude1 + 1 : this.gravelDepositMaxAltitude1);
        this.gravelDepositRarity2 = (this.gravelDepositRarity2 < 0 ? 0 : this.gravelDepositRarity2 > 100 ? 100 : this.gravelDepositRarity2);
        this.gravelDepositFrequency2 = (this.gravelDepositFrequency2 < 0 ? 0 : this.gravelDepositFrequency2);
        this.gravelDepositSize2 = (this.gravelDepositSize2 < 0 ? 0 : this.gravelDepositSize2);
        this.gravelDepositMinAltitude2 = (this.gravelDepositMinAltitude2 < 0 ? 0 : this.gravelDepositMinAltitude2 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.gravelDepositMinAltitude2);
        this.gravelDepositMaxAltitude2 = (this.gravelDepositMaxAltitude2 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.gravelDepositMaxAltitude2 <= this.gravelDepositMinAltitude2 ? this.gravelDepositMinAltitude2 + 1 : this.gravelDepositMaxAltitude2);
        this.gravelDepositRarity3 = (this.gravelDepositRarity3 < 0 ? 0 : this.gravelDepositRarity3 > 100 ? 100 : this.gravelDepositRarity3);
        this.gravelDepositFrequency3 = (this.gravelDepositFrequency3 < 0 ? 0 : this.gravelDepositFrequency3);
        this.gravelDepositSize3 = (this.gravelDepositSize3 < 0 ? 0 : this.gravelDepositSize3);
        this.gravelDepositMinAltitude3 = (this.gravelDepositMinAltitude3 < 0 ? 0 : this.gravelDepositMinAltitude3 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.gravelDepositMinAltitude3);
        this.gravelDepositMaxAltitude3 = (this.gravelDepositMaxAltitude3 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.gravelDepositMaxAltitude3 <= this.gravelDepositMinAltitude3 ? this.gravelDepositMinAltitude3 + 1 : this.gravelDepositMaxAltitude3);
        this.gravelDepositRarity4 = (this.gravelDepositRarity4 < 0 ? 0 : this.gravelDepositRarity4 > 100 ? 100 : this.gravelDepositRarity4);
        this.gravelDepositFrequency4 = (this.gravelDepositFrequency4 < 0 ? 0 : this.gravelDepositFrequency4);
        this.gravelDepositSize4 = (this.gravelDepositSize4 < 0 ? 0 : this.gravelDepositSize4);
        this.gravelDepositMinAltitude4 = (this.gravelDepositMinAltitude4 < 0 ? 0 : this.gravelDepositMinAltitude4 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.gravelDepositMinAltitude4);
        this.gravelDepositMaxAltitude4 = (this.gravelDepositMaxAltitude4 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.gravelDepositMaxAltitude4 <= this.gravelDepositMinAltitude4 ? this.gravelDepositMinAltitude4 + 1 : this.gravelDepositMaxAltitude4);
        this.clayDepositRarity1 = (this.clayDepositRarity1 < 0 ? 0 : this.clayDepositRarity1 > 100 ? 100 : this.clayDepositRarity1);
        this.clayDepositFrequency1 = (this.clayDepositFrequency1 < 0 ? 0 : this.clayDepositFrequency1);
        this.clayDepositSize1 = (this.clayDepositSize1 < 0 ? 0 : this.clayDepositSize1);
        this.clayDepositMinAltitude1 = (this.clayDepositMinAltitude1 < 0 ? 0 : this.clayDepositMinAltitude1 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.clayDepositMinAltitude1);
        this.clayDepositMaxAltitude1 = (this.clayDepositMaxAltitude1 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.clayDepositMaxAltitude1 <= this.clayDepositMinAltitude1 ? this.clayDepositMinAltitude1 + 1 : this.clayDepositMaxAltitude1);
        this.clayDepositRarity2 = (this.clayDepositRarity2 < 0 ? 0 : this.clayDepositRarity2 > 100 ? 100 : this.clayDepositRarity2);
        this.clayDepositFrequency2 = (this.clayDepositFrequency2 < 0 ? 0 : this.clayDepositFrequency2);
        this.clayDepositSize2 = (this.clayDepositSize2 < 0 ? 0 : this.clayDepositSize2);
        this.clayDepositMinAltitude2 = (this.clayDepositMinAltitude2 < 0 ? 0 : this.clayDepositMinAltitude2 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.clayDepositMinAltitude2);
        this.clayDepositMaxAltitude2 = (this.clayDepositMaxAltitude2 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.clayDepositMaxAltitude2 <= this.clayDepositMinAltitude2 ? this.clayDepositMinAltitude2 + 1 : this.clayDepositMaxAltitude2);
        this.clayDepositRarity3 = (this.clayDepositRarity3 < 0 ? 0 : this.clayDepositRarity3 > 100 ? 100 : this.clayDepositRarity3);
        this.clayDepositFrequency3 = (this.clayDepositFrequency3 < 0 ? 0 : this.clayDepositFrequency3);
        this.clayDepositSize3 = (this.clayDepositSize3 < 0 ? 0 : this.clayDepositSize3);
        this.clayDepositMinAltitude3 = (this.clayDepositMinAltitude3 < 0 ? 0 : this.clayDepositMinAltitude3 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.clayDepositMinAltitude3);
        this.clayDepositMaxAltitude3 = (this.clayDepositMaxAltitude3 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.clayDepositMaxAltitude3 <= this.clayDepositMinAltitude3 ? this.clayDepositMinAltitude3 + 1 : this.clayDepositMaxAltitude3);
        this.clayDepositRarity4 = (this.clayDepositRarity4 < 0 ? 0 : this.clayDepositRarity4 > 100 ? 100 : this.clayDepositRarity4);
        this.clayDepositFrequency4 = (this.clayDepositFrequency4 < 0 ? 0 : this.clayDepositFrequency4);
        this.clayDepositSize4 = (this.clayDepositSize4 < 0 ? 0 : this.clayDepositSize4);
        this.clayDepositMinAltitude4 = (this.clayDepositMinAltitude4 < 0 ? 0 : this.clayDepositMinAltitude4 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.clayDepositMinAltitude4);
        this.clayDepositMaxAltitude4 = (this.clayDepositMaxAltitude4 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.clayDepositMaxAltitude4 <= this.clayDepositMinAltitude4 ? this.clayDepositMinAltitude4 + 1 : this.clayDepositMaxAltitude4);
        this.coalDepositRarity1 = (this.coalDepositRarity1 < 0 ? 0 : this.coalDepositRarity1 > 100 ? 100 : this.coalDepositRarity1);
        this.coalDepositFrequency1 = (this.coalDepositFrequency1 < 0 ? 0 : this.coalDepositFrequency1);
        this.coalDepositSize1 = (this.coalDepositSize1 < 0 ? 0 : this.coalDepositSize1);
        this.coalDepositMinAltitude1 = (this.coalDepositMinAltitude1 < 0 ? 0 : this.coalDepositMinAltitude1 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.coalDepositMinAltitude1);
        this.coalDepositMaxAltitude1 = (this.coalDepositMaxAltitude1 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.coalDepositMaxAltitude1 <= this.coalDepositMinAltitude1 ? this.coalDepositMinAltitude1 + 1 : this.coalDepositMaxAltitude1);
        this.coalDepositRarity2 = (this.coalDepositRarity2 < 0 ? 0 : this.coalDepositRarity2 > 100 ? 100 : this.coalDepositRarity2);
        this.coalDepositFrequency2 = (this.coalDepositFrequency2 < 0 ? 0 : this.coalDepositFrequency2);
        this.coalDepositSize2 = (this.coalDepositSize2 < 0 ? 0 : this.coalDepositSize2);
        this.coalDepositMinAltitude2 = (this.coalDepositMinAltitude2 < 0 ? 0 : this.coalDepositMinAltitude2 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.coalDepositMinAltitude2);
        this.coalDepositMaxAltitude2 = (this.coalDepositMaxAltitude2 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.coalDepositMaxAltitude2 <= this.coalDepositMinAltitude2 ? this.coalDepositMinAltitude2 + 1 : this.coalDepositMaxAltitude2);
        this.coalDepositRarity3 = (this.coalDepositRarity3 < 0 ? 0 : this.coalDepositRarity3 > 100 ? 100 : this.coalDepositRarity3);
        this.coalDepositFrequency3 = (this.coalDepositFrequency3 < 0 ? 0 : this.coalDepositFrequency3);
        this.coalDepositSize3 = (this.coalDepositSize3 < 0 ? 0 : this.coalDepositSize3);
        this.coalDepositMinAltitude3 = (this.coalDepositMinAltitude3 < 0 ? 0 : this.coalDepositMinAltitude3 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.coalDepositMinAltitude3);
        this.coalDepositMaxAltitude3 = (this.coalDepositMaxAltitude3 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.coalDepositMaxAltitude3 <= this.coalDepositMinAltitude3 ? this.coalDepositMinAltitude3 + 1 : this.coalDepositMaxAltitude3);
        this.coalDepositRarity4 = (this.coalDepositRarity4 < 0 ? 0 : this.coalDepositRarity4 > 100 ? 100 : this.coalDepositRarity4);
        this.coalDepositFrequency4 = (this.coalDepositFrequency4 < 0 ? 0 : this.coalDepositFrequency4);
        this.coalDepositSize4 = (this.coalDepositSize4 < 0 ? 0 : this.coalDepositSize4);
        this.coalDepositMinAltitude4 = (this.coalDepositMinAltitude4 < 0 ? 0 : this.coalDepositMinAltitude4 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.coalDepositMinAltitude4);
        this.coalDepositMaxAltitude4 = (this.coalDepositMaxAltitude4 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.coalDepositMaxAltitude4 <= this.coalDepositMinAltitude4 ? this.coalDepositMinAltitude4 + 1 : this.coalDepositMaxAltitude4);
        this.ironDepositRarity1 = (this.ironDepositRarity1 < 0 ? 0 : this.ironDepositRarity1 > 100 ? 100 : this.ironDepositRarity1);
        this.ironDepositFrequency1 = (this.ironDepositFrequency1 < 0 ? 0 : this.ironDepositFrequency1);
        this.ironDepositSize1 = (this.ironDepositSize1 < 0 ? 0 : this.ironDepositSize1);
        this.ironDepositMinAltitude1 = (this.ironDepositMinAltitude1 < 0 ? 0 : this.ironDepositMinAltitude1 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.ironDepositMinAltitude1);
        this.ironDepositMaxAltitude1 = (this.ironDepositMaxAltitude1 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.ironDepositMaxAltitude1 <= this.ironDepositMinAltitude1 ? this.ironDepositMinAltitude1 + 1 : this.ironDepositMaxAltitude1);
        this.ironDepositRarity2 = (this.ironDepositRarity2 < 0 ? 0 : this.ironDepositRarity2 > 100 ? 100 : this.ironDepositRarity2);
        this.ironDepositFrequency2 = (this.ironDepositFrequency2 < 0 ? 0 : this.ironDepositFrequency2);
        this.ironDepositSize2 = (this.ironDepositSize2 < 0 ? 0 : this.ironDepositSize2);
        this.ironDepositMinAltitude2 = (this.ironDepositMinAltitude2 < 0 ? 0 : this.ironDepositMinAltitude2 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.ironDepositMinAltitude2);
        this.ironDepositMaxAltitude2 = (this.ironDepositMaxAltitude2 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.ironDepositMaxAltitude2 <= this.ironDepositMinAltitude2 ? this.ironDepositMinAltitude2 + 1 : this.ironDepositMaxAltitude2);
        this.ironDepositRarity3 = (this.ironDepositRarity3 < 0 ? 0 : this.ironDepositRarity3 > 100 ? 100 : this.ironDepositRarity3);
        this.ironDepositFrequency3 = (this.ironDepositFrequency3 < 0 ? 0 : this.ironDepositFrequency3);
        this.ironDepositSize3 = (this.ironDepositSize3 < 0 ? 0 : this.ironDepositSize3);
        this.ironDepositMinAltitude3 = (this.ironDepositMinAltitude3 < 0 ? 0 : this.ironDepositMinAltitude3 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.ironDepositMinAltitude3);
        this.ironDepositMaxAltitude3 = (this.ironDepositMaxAltitude3 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.ironDepositMaxAltitude3 <= this.ironDepositMinAltitude3 ? this.ironDepositMinAltitude3 + 1 : this.ironDepositMaxAltitude3);
        this.ironDepositRarity4 = (this.ironDepositRarity4 < 0 ? 0 : this.ironDepositRarity4 > 100 ? 100 : this.ironDepositRarity4);
        this.ironDepositFrequency4 = (this.ironDepositFrequency4 < 0 ? 0 : this.ironDepositFrequency4);
        this.ironDepositSize4 = (this.ironDepositSize4 < 0 ? 0 : this.ironDepositSize4);
        this.ironDepositMinAltitude4 = (this.ironDepositMinAltitude4 < 0 ? 0 : this.ironDepositMinAltitude4 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.ironDepositMinAltitude4);
        this.ironDepositMaxAltitude4 = (this.ironDepositMaxAltitude4 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.ironDepositMaxAltitude4 <= this.ironDepositMinAltitude4 ? this.ironDepositMinAltitude4 + 1 : this.ironDepositMaxAltitude4);
        this.goldDepositRarity1 = (this.goldDepositRarity1 < 0 ? 0 : this.goldDepositRarity1 > 100 ? 100 : this.goldDepositRarity1);
        this.goldDepositFrequency1 = (this.goldDepositFrequency1 < 0 ? 0 : this.goldDepositFrequency1);
        this.goldDepositSize1 = (this.goldDepositSize1 < 0 ? 0 : this.goldDepositSize1);
        this.goldDepositMinAltitude1 = (this.goldDepositMinAltitude1 < 0 ? 0 : this.goldDepositMinAltitude1 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.goldDepositMinAltitude1);
        this.goldDepositMaxAltitude1 = (this.goldDepositMaxAltitude1 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.goldDepositMaxAltitude1 <= this.goldDepositMinAltitude1 ? this.goldDepositMinAltitude1 + 1 : this.goldDepositMaxAltitude1);
        this.goldDepositRarity2 = (this.goldDepositRarity2 < 0 ? 0 : this.goldDepositRarity2 > 100 ? 100 : this.goldDepositRarity2);
        this.goldDepositFrequency2 = (this.goldDepositFrequency2 < 0 ? 0 : this.goldDepositFrequency2);
        this.goldDepositSize2 = (this.goldDepositSize2 < 0 ? 0 : this.goldDepositSize2);
        this.goldDepositMinAltitude2 = (this.goldDepositMinAltitude2 < 0 ? 0 : this.goldDepositMinAltitude2 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.goldDepositMinAltitude2);
        this.goldDepositMaxAltitude2 = (this.goldDepositMaxAltitude2 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.goldDepositMaxAltitude2 <= this.goldDepositMinAltitude2 ? this.goldDepositMinAltitude2 + 1 : this.goldDepositMaxAltitude2);
        this.goldDepositRarity3 = (this.goldDepositRarity3 < 0 ? 0 : this.goldDepositRarity3 > 100 ? 100 : this.goldDepositRarity3);
        this.goldDepositFrequency3 = (this.goldDepositFrequency3 < 0 ? 0 : this.goldDepositFrequency3);
        this.goldDepositSize3 = (this.goldDepositSize3 < 0 ? 0 : this.goldDepositSize3);
        this.goldDepositMinAltitude3 = (this.goldDepositMinAltitude3 < 0 ? 0 : this.goldDepositMinAltitude3 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.goldDepositMinAltitude3);
        this.goldDepositMaxAltitude3 = (this.goldDepositMaxAltitude3 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.goldDepositMaxAltitude3 <= this.goldDepositMinAltitude3 ? this.goldDepositMinAltitude3 + 1 : this.goldDepositMaxAltitude3);
        this.goldDepositRarity4 = (this.goldDepositRarity4 < 0 ? 0 : this.goldDepositRarity4 > 100 ? 100 : this.goldDepositRarity4);
        this.goldDepositFrequency4 = (this.goldDepositFrequency4 < 0 ? 0 : this.goldDepositFrequency4);
        this.goldDepositSize4 = (this.goldDepositSize4 < 0 ? 0 : this.goldDepositSize4);
        this.goldDepositMinAltitude4 = (this.goldDepositMinAltitude4 < 0 ? 0 : this.goldDepositMinAltitude4 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.goldDepositMinAltitude4);
        this.goldDepositMaxAltitude4 = (this.goldDepositMaxAltitude4 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.goldDepositMaxAltitude4 <= this.goldDepositMinAltitude4 ? this.goldDepositMinAltitude4 + 1 : this.goldDepositMaxAltitude4);
        this.redstoneDepositRarity1 = (this.redstoneDepositRarity1 < 0 ? 0 : this.redstoneDepositRarity1 > 100 ? 100 : this.redstoneDepositRarity1);
        this.redstoneDepositFrequency1 = (this.redstoneDepositFrequency1 < 0 ? 0 : this.redstoneDepositFrequency1);
        this.redstoneDepositSize1 = (this.redstoneDepositSize1 < 0 ? 0 : this.redstoneDepositSize1);
        this.redstoneDepositMinAltitude1 = (this.redstoneDepositMinAltitude1 < 0 ? 0 : this.redstoneDepositMinAltitude1 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.redstoneDepositMinAltitude1);
        this.redstoneDepositMaxAltitude1 = (this.redstoneDepositMaxAltitude1 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.redstoneDepositMaxAltitude1 <= this.redstoneDepositMinAltitude1 ? this.redstoneDepositMinAltitude1 + 1 : this.redstoneDepositMaxAltitude1);
        this.redstoneDepositRarity2 = (this.redstoneDepositRarity2 < 0 ? 0 : this.redstoneDepositRarity2 > 100 ? 100 : this.redstoneDepositRarity2);
        this.redstoneDepositFrequency2 = (this.redstoneDepositFrequency2 < 0 ? 0 : this.redstoneDepositFrequency2);
        this.redstoneDepositSize2 = (this.redstoneDepositSize2 < 0 ? 0 : this.redstoneDepositSize2);
        this.redstoneDepositMinAltitude2 = (this.redstoneDepositMinAltitude2 < 0 ? 0 : this.redstoneDepositMinAltitude2 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.redstoneDepositMinAltitude2);
        this.redstoneDepositMaxAltitude2 = (this.redstoneDepositMaxAltitude2 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.redstoneDepositMaxAltitude2 <= this.redstoneDepositMinAltitude2 ? this.redstoneDepositMinAltitude2 + 1 : this.redstoneDepositMaxAltitude2);
        this.redstoneDepositRarity3 = (this.redstoneDepositRarity3 < 0 ? 0 : this.redstoneDepositRarity3 > 100 ? 100 : this.redstoneDepositRarity3);
        this.redstoneDepositFrequency3 = (this.redstoneDepositFrequency3 < 0 ? 0 : this.redstoneDepositFrequency3);
        this.redstoneDepositSize3 = (this.redstoneDepositSize3 < 0 ? 0 : this.redstoneDepositSize3);
        this.redstoneDepositMinAltitude3 = (this.redstoneDepositMinAltitude3 < 0 ? 0 : this.redstoneDepositMinAltitude3 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.redstoneDepositMinAltitude3);
        this.redstoneDepositMaxAltitude3 = (this.redstoneDepositMaxAltitude3 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.redstoneDepositMaxAltitude3 <= this.redstoneDepositMinAltitude3 ? this.redstoneDepositMinAltitude3 + 1 : this.redstoneDepositMaxAltitude3);
        this.redstoneDepositRarity4 = (this.redstoneDepositRarity4 < 0 ? 0 : this.redstoneDepositRarity4 > 100 ? 100 : this.redstoneDepositRarity4);
        this.redstoneDepositFrequency4 = (this.redstoneDepositFrequency4 < 0 ? 0 : this.redstoneDepositFrequency4);
        this.redstoneDepositSize4 = (this.redstoneDepositSize4 < 0 ? 0 : this.redstoneDepositSize4);
        this.redstoneDepositMinAltitude4 = (this.redstoneDepositMinAltitude4 < 0 ? 0 : this.redstoneDepositMinAltitude4 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.redstoneDepositMinAltitude4);
        this.redstoneDepositMaxAltitude4 = (this.redstoneDepositMaxAltitude4 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.redstoneDepositMaxAltitude4 <= this.redstoneDepositMinAltitude4 ? this.redstoneDepositMinAltitude4 + 1 : this.redstoneDepositMaxAltitude4);
        this.diamondDepositRarity1 = (this.diamondDepositRarity1 < 0 ? 0 : this.diamondDepositRarity1 > 100 ? 100 : this.diamondDepositRarity1);
        this.diamondDepositFrequency1 = (this.diamondDepositFrequency1 < 0 ? 0 : this.diamondDepositFrequency1);
        this.diamondDepositSize1 = (this.diamondDepositSize1 < 0 ? 0 : this.diamondDepositSize1);
        this.diamondDepositMinAltitude1 = (this.diamondDepositMinAltitude1 < 0 ? 0 : this.diamondDepositMinAltitude1 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.diamondDepositMinAltitude1);
        this.diamondDepositMaxAltitude1 = (this.diamondDepositMaxAltitude1 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.diamondDepositMaxAltitude1 <= this.diamondDepositMinAltitude1 ? this.diamondDepositMinAltitude1 + 1 : this.diamondDepositMaxAltitude1);
        this.diamondDepositRarity2 = (this.diamondDepositRarity2 < 0 ? 0 : this.diamondDepositRarity2 > 100 ? 100 : this.diamondDepositRarity2);
        this.diamondDepositFrequency2 = (this.diamondDepositFrequency2 < 0 ? 0 : this.diamondDepositFrequency2);
        this.diamondDepositSize2 = (this.diamondDepositSize2 < 0 ? 0 : this.diamondDepositSize2);
        this.diamondDepositMinAltitude2 = (this.diamondDepositMinAltitude2 < 0 ? 0 : this.diamondDepositMinAltitude2 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.diamondDepositMinAltitude2);
        this.diamondDepositMaxAltitude2 = (this.diamondDepositMaxAltitude2 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.diamondDepositMaxAltitude2 <= this.diamondDepositMinAltitude2 ? this.diamondDepositMinAltitude2 + 1 : this.diamondDepositMaxAltitude2);
        this.diamondDepositRarity3 = (this.diamondDepositRarity3 < 0 ? 0 : this.diamondDepositRarity3 > 100 ? 100 : this.diamondDepositRarity3);
        this.diamondDepositFrequency3 = (this.diamondDepositFrequency3 < 0 ? 0 : this.diamondDepositFrequency3);
        this.diamondDepositSize3 = (this.diamondDepositSize3 < 0 ? 0 : this.diamondDepositSize3);
        this.diamondDepositMinAltitude3 = (this.diamondDepositMinAltitude3 < 0 ? 0 : this.diamondDepositMinAltitude3 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.diamondDepositMinAltitude3);
        this.diamondDepositMaxAltitude3 = (this.diamondDepositMaxAltitude3 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.diamondDepositMaxAltitude3 <= this.diamondDepositMinAltitude3 ? this.diamondDepositMinAltitude3 + 1 : this.diamondDepositMaxAltitude3);
        this.diamondDepositRarity4 = (this.diamondDepositRarity4 < 0 ? 0 : this.diamondDepositRarity4 > 100 ? 100 : this.diamondDepositRarity4);
        this.diamondDepositFrequency4 = (this.diamondDepositFrequency4 < 0 ? 0 : this.diamondDepositFrequency4);
        this.diamondDepositSize4 = (this.diamondDepositSize4 < 0 ? 0 : this.diamondDepositSize4);
        this.diamondDepositMinAltitude4 = (this.diamondDepositMinAltitude4 < 0 ? 0 : this.diamondDepositMinAltitude4 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.diamondDepositMinAltitude4);
        this.diamondDepositMaxAltitude4 = (this.diamondDepositMaxAltitude4 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.diamondDepositMaxAltitude4 <= this.diamondDepositMinAltitude4 ? this.diamondDepositMinAltitude4 + 1 : this.diamondDepositMaxAltitude4);
        this.lapislazuliDepositRarity1 = (this.lapislazuliDepositRarity1 < 0 ? 0 : this.lapislazuliDepositRarity1 > 100 ? 100 : this.lapislazuliDepositRarity1);
        this.lapislazuliDepositFrequency1 = (this.lapislazuliDepositFrequency1 < 0 ? 0 : this.lapislazuliDepositFrequency1);
        this.lapislazuliDepositSize1 = (this.lapislazuliDepositSize1 < 0 ? 0 : this.lapislazuliDepositSize1);
        this.lapislazuliDepositMinAltitude1 = (this.lapislazuliDepositMinAltitude1 < 0 ? 0 : this.lapislazuliDepositMinAltitude1 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.lapislazuliDepositMinAltitude1);
        this.lapislazuliDepositMaxAltitude1 = (this.lapislazuliDepositMaxAltitude1 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.lapislazuliDepositMaxAltitude1 <= this.lapislazuliDepositMinAltitude1 ? this.lapislazuliDepositMinAltitude1 + 1 : this.lapislazuliDepositMaxAltitude1);
        this.lapislazuliDepositRarity2 = (this.lapislazuliDepositRarity2 < 0 ? 0 : this.lapislazuliDepositRarity2 > 100 ? 100 : this.lapislazuliDepositRarity2);
        this.lapislazuliDepositFrequency2 = (this.lapislazuliDepositFrequency2 < 0 ? 0 : this.lapislazuliDepositFrequency2);
        this.lapislazuliDepositSize2 = (this.lapislazuliDepositSize2 < 0 ? 0 : this.lapislazuliDepositSize2);
        this.lapislazuliDepositMinAltitude2 = (this.lapislazuliDepositMinAltitude2 < 0 ? 0 : this.lapislazuliDepositMinAltitude2 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.lapislazuliDepositMinAltitude2);
        this.lapislazuliDepositMaxAltitude2 = (this.lapislazuliDepositMaxAltitude2 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.lapislazuliDepositMaxAltitude2 <= this.lapislazuliDepositMinAltitude2 ? this.lapislazuliDepositMinAltitude2 + 1 : this.lapislazuliDepositMaxAltitude2);
        this.lapislazuliDepositRarity3 = (this.lapislazuliDepositRarity3 < 0 ? 0 : this.lapislazuliDepositRarity3 > 100 ? 100 : this.lapislazuliDepositRarity3);
        this.lapislazuliDepositFrequency3 = (this.lapislazuliDepositFrequency3 < 0 ? 0 : this.lapislazuliDepositFrequency3);
        this.lapislazuliDepositSize3 = (this.lapislazuliDepositSize3 < 0 ? 0 : this.lapislazuliDepositSize3);
        this.lapislazuliDepositMinAltitude3 = (this.lapislazuliDepositMinAltitude3 < 0 ? 0 : this.lapislazuliDepositMinAltitude3 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.lapislazuliDepositMinAltitude3);
        this.lapislazuliDepositMaxAltitude3 = (this.lapislazuliDepositMaxAltitude3 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.lapislazuliDepositMaxAltitude3 <= this.lapislazuliDepositMinAltitude3 ? this.lapislazuliDepositMinAltitude3 + 1 : this.lapislazuliDepositMaxAltitude3);
        this.lapislazuliDepositRarity4 = (this.lapislazuliDepositRarity4 < 0 ? 0 : this.lapislazuliDepositRarity4 > 100 ? 100 : this.lapislazuliDepositRarity4);
        this.lapislazuliDepositFrequency4 = (this.lapislazuliDepositFrequency4 < 0 ? 0 : this.lapislazuliDepositFrequency4);
        this.lapislazuliDepositSize4 = (this.lapislazuliDepositSize4 < 0 ? 0 : this.lapislazuliDepositSize4);
        this.lapislazuliDepositMinAltitude4 = (this.lapislazuliDepositMinAltitude4 < 0 ? 0 : this.lapislazuliDepositMinAltitude4 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.lapislazuliDepositMinAltitude4);
        this.lapislazuliDepositMaxAltitude4 = (this.lapislazuliDepositMaxAltitude4 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.lapislazuliDepositMaxAltitude4 <= this.lapislazuliDepositMinAltitude4 ? this.lapislazuliDepositMinAltitude4 + 1 : this.lapislazuliDepositMaxAltitude4);

        this.lavaSourceHellDepositRarity = (this.lavaSourceHellDepositRarity < 0 ? 0 : this.lavaSourceHellDepositRarity > 100 ? 100 : this.lavaSourceHellDepositRarity);
        this.lavaSourceHellDepositFrequency = (this.lavaSourceHellDepositFrequency < 0 ? 0 : this.lavaSourceHellDepositFrequency);
        this.lavaSourceHellDepositMinAltitude = (this.lavaSourceHellDepositMinAltitude < 0 ? 0 : this.lavaSourceHellDepositMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.lavaSourceHellDepositMinAltitude);
        this.lavaSourceHellDepositMaxAltitude = (this.lavaSourceHellDepositMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.lavaSourceHellDepositMaxAltitude <= this.lavaSourceHellDepositMinAltitude ? this.lavaSourceHellDepositMinAltitude + 1 : this.lavaSourceHellDepositMaxAltitude);
        this.fireHellDepositRarity = (this.fireHellDepositRarity < 0 ? 0 : this.fireHellDepositRarity > 100 ? 100 : this.fireHellDepositRarity);
        this.fireHellDepositFrequency = (this.fireHellDepositFrequency < 0 ? 0 : this.fireHellDepositFrequency);
        this.fireHellDepositMinAltitude = (this.fireHellDepositMinAltitude < 0 ? 0 : this.fireHellDepositMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.fireHellDepositMinAltitude);
        this.fireHellDepositMaxAltitude = (this.fireHellDepositMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.fireHellDepositMaxAltitude <= this.fireHellDepositMinAltitude ? this.fireHellDepositMinAltitude + 1 : this.fireHellDepositMaxAltitude);
        this.lightstoneHellDepositRarity1 = (this.lightstoneHellDepositRarity1 < 0 ? 0 : this.lightstoneHellDepositRarity1 > 100 ? 100 : this.lightstoneHellDepositRarity1);
        this.lightstoneHellDepositFrequency1 = (this.lightstoneHellDepositFrequency1 < 0 ? 0 : this.lightstoneHellDepositFrequency1);
        this.lightstoneHellDepositMinAltitude1 = (this.lightstoneHellDepositMinAltitude1 < 0 ? 0 : this.lightstoneHellDepositMinAltitude1 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.lightstoneHellDepositMinAltitude1);
        this.lightstoneHellDepositMaxAltitude1 = (this.lightstoneHellDepositMaxAltitude1 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.lightstoneHellDepositMaxAltitude1 <= this.lightstoneHellDepositMinAltitude1 ? this.lightstoneHellDepositMinAltitude1 + 1 : this.lightstoneHellDepositMaxAltitude1);
        this.lightstoneHellDepositRarity2 = (this.lightstoneHellDepositRarity2 < 0 ? 0 : this.lightstoneHellDepositRarity2 > 100 ? 100 : this.lightstoneHellDepositRarity2);
        this.lightstoneHellDepositFrequency2 = (this.lightstoneHellDepositFrequency2 < 0 ? 0 : this.lightstoneHellDepositFrequency2);
        this.lightstoneHellDepositMinAltitude2 = (this.lightstoneHellDepositMinAltitude2 < 0 ? 0 : this.lightstoneHellDepositMinAltitude2 > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.lightstoneHellDepositMinAltitude2);
        this.lightstoneHellDepositMaxAltitude2 = (this.lightstoneHellDepositMaxAltitude2 > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.lightstoneHellDepositMaxAltitude2 <= this.lightstoneHellDepositMinAltitude2 ? this.lightstoneHellDepositMinAltitude2 + 1 : this.lightstoneHellDepositMaxAltitude2);
        this.brownMushroomHellDepositRarity = (this.brownMushroomHellDepositRarity < 0 ? 0 : this.brownMushroomHellDepositRarity > 100 ? 100 : this.brownMushroomHellDepositRarity);
        this.brownMushroomHellDepositFrequency = (this.brownMushroomHellDepositFrequency < 0 ? 0 : this.brownMushroomHellDepositFrequency);
        this.brownMushroomHellDepositMinAltitude = (this.brownMushroomHellDepositMinAltitude < 0 ? 0 : this.brownMushroomHellDepositMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.brownMushroomHellDepositMinAltitude);
        this.brownMushroomHellDepositMaxAltitude = (this.brownMushroomHellDepositMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.brownMushroomHellDepositMaxAltitude <= this.brownMushroomHellDepositMinAltitude ? this.brownMushroomHellDepositMinAltitude + 1 : this.brownMushroomHellDepositMaxAltitude);
        this.redMushroomHellDepositRarity = (this.redMushroomHellDepositRarity < 0 ? 0 : this.redMushroomHellDepositRarity > 100 ? 100 : this.redMushroomHellDepositRarity);
        this.redMushroomHellDepositFrequency = (this.redMushroomHellDepositFrequency < 0 ? 0 : this.redMushroomHellDepositFrequency);
        this.redMushroomHellDepositMinAltitude = (this.redMushroomHellDepositMinAltitude < 0 ? 0 : this.redMushroomHellDepositMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.redMushroomHellDepositMinAltitude);
        this.redMushroomHellDepositMaxAltitude = (this.redMushroomHellDepositMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.redMushroomHellDepositMaxAltitude <= this.redMushroomHellDepositMinAltitude ? this.redMushroomHellDepositMinAltitude + 1 : this.redMushroomHellDepositMaxAltitude);

        this.dungeonRarity = (this.dungeonRarity < 0 ? 0 : this.dungeonRarity > 100 ? 100 : this.dungeonRarity);
        this.dungeonFrequency = (this.dungeonFrequency < 0 ? 0 : this.dungeonFrequency);
        this.dungeonMinAltitude = (this.dungeonMinAltitude < 0 ? 0 : this.dungeonMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.dungeonMinAltitude);
        this.dungeonMaxAltitude = (this.dungeonMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.dungeonMaxAltitude <= this.dungeonMinAltitude ? this.dungeonMinAltitude + 1 : this.dungeonMaxAltitude);


        this.lavaLevelMin = (this.lavaLevelMin < 0 ? 0 : this.lavaLevelMin > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.lavaLevelMin);
        this.lavaLevelMax = (this.lavaLevelMax > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.lavaLevelMax < this.lavaLevelMin ? this.lavaLevelMin : this.lavaLevelMax);

        this.undergroundLakeRarity = (this.undergroundLakeRarity < 0 ? 0 : this.undergroundLakeRarity > 100 ? 100 : this.undergroundLakeRarity);
        this.undergroundLakeFrequency = (this.undergroundLakeFrequency < 0 ? 0 : this.undergroundLakeFrequency);
        this.undergroundLakeMinSize = (this.undergroundLakeMinSize < 25 ? 25 : this.undergroundLakeMinSize);
        this.undergroundLakeMaxSize = (this.undergroundLakeMaxSize <= this.undergroundLakeMinSize ? this.undergroundLakeMinSize + 1 : this.undergroundLakeMaxSize);
        this.undergroundLakeMinAltitude = (this.undergroundLakeMinAltitude < 0 ? 0 : this.undergroundLakeMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.undergroundLakeMinAltitude);
        this.undergroundLakeMaxAltitude = (this.undergroundLakeMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.undergroundLakeMaxAltitude <= this.undergroundLakeMinAltitude ? this.undergroundLakeMinAltitude + 1 : this.undergroundLakeMaxAltitude);


    }

    public void writeSettings() throws IOException
    {

        this.worldWrk.WriteModTitleSettings("Start BOB Objects Variables :");
        this.worldWrk.WriteModSettings(BiomeTerrainValues.customObjects.name(), this.customObjects);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.objectSpawnRatio.name(), Integer.valueOf(this.objectSpawnRatio).intValue());

        this.worldWrk.WriteModTitleSettings("Start Cactus&Tree Variables :");
        this.worldWrk.WriteModSettings(BiomeTerrainValues.notchBiomeTrees.name(), this.notchBiomeTrees);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.globalTreeDensity.name(), this.globalTreeDensity);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.rainforestTreeDensity.name(), this.rainforestTreeDensity);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.swamplandTreeDensity.name(), this.swamplandTreeDensity);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.seasonalforestTreeDensity.name(), this.seasonalforestTreeDensity);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.forestTreeDensity.name(), this.forestTreeDensity);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.savannaTreeDensity.name(), this.savannaTreeDensity);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.shrublandTreeDensity.name(), this.shrublandTreeDensity);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.taigaTreeDensity.name(), this.taigaTreeDensity);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.desertTreeDensity.name(), this.desertTreeDensity);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.plainsTreeDensity.name(), this.plainsTreeDensity);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.iceDesertTreeDensity.name(), this.iceDesertTreeDensity);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.tundraTreeDensity.name(), this.tundraTreeDensity);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.globalCactusDensity.name(), this.globalCactusDensity);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.desertCactusDensity.name(), this.desertCactusDensity);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.cactusDepositRarity.name(), this.cactusDepositRarity);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.cactusDepositMinAltitude.name(), this.cactusDepositMinAltitude);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.cactusDepositMaxAltitude.name(), this.cactusDepositMaxAltitude);


        this.worldWrk.WriteModTitleSettings("Lava Pool Variables");
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lavaLevelMin.name(), this.lavaLevelMin);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lavaLevelMax.name(), this.lavaLevelMax);

        this.worldWrk.WriteModTitleSettings("Underground Lake Variables");
        this.worldWrk.WriteModSettings(BiomeTerrainValues.undergroundLakes.name(), this.undergroundLakes);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.undergroundLakesInAir.name(), this.undergroundLakesInAir);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.undergroundLakeFrequency.name(), this.undergroundLakeFrequency);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.undergroundLakeRarity.name(), this.undergroundLakeRarity);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.undergroundLakeMinSize.name(), this.undergroundLakeMinSize);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.undergroundLakeMaxSize.name(), this.undergroundLakeMaxSize);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.undergroundLakeMinAltitude.name(), this.undergroundLakeMinAltitude);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.undergroundLakeMaxAltitude.name(), this.undergroundLakeMaxAltitude);


        this.worldWrk.WriteModTitleSettings("Start Deposit Variables :");
        this.worldWrk.WriteModTitleSettings("Above Ground Variables");
        this.worldWrk.WriteModSettings(BiomeTerrainValues.flowerDepositRarity.name(), this.flowerDepositRarity);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.flowerDepositFrequency.name(), this.flowerDepositFrequency);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.flowerDepositMinAltitude.name(), this.flowerDepositMinAltitude);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.flowerDepositMaxAltitude.name(), this.flowerDepositMaxAltitude);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.roseDepositRarity.name(), this.roseDepositRarity);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.roseDepositFrequency.name(), this.roseDepositFrequency);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.roseDepositMinAltitude.name(), this.roseDepositMinAltitude);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.roseDepositMaxAltitude.name(), this.roseDepositMaxAltitude);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.brownMushroomDepositRarity.name(), this.brownMushroomDepositRarity);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.brownMushroomDepositFrequency.name(), this.brownMushroomDepositFrequency);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.brownMushroomDepositMinAltitude.name(), this.brownMushroomDepositMinAltitude);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.brownMushroomDepositMaxAltitude.name(), this.brownMushroomDepositMaxAltitude);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.redMushroomDepositRarity.name(), this.redMushroomDepositRarity);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.redMushroomDepositFrequency.name(), this.redMushroomDepositFrequency);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.redMushroomDepositMinAltitude.name(), this.redMushroomDepositMinAltitude);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.redMushroomDepositMaxAltitude.name(), this.redMushroomDepositMaxAltitude);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.reedDepositRarity.name(), this.reedDepositRarity);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.reedDepositFrequency.name(), this.reedDepositFrequency);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.reedDepositMinAltitude.name(), this.reedDepositMinAltitude);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.reedDepositMaxAltitude.name(), this.reedDepositMaxAltitude);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.pumpkinDepositRarity.name(), this.pumpkinDepositRarity);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.pumpkinDepositFrequency.name(), this.pumpkinDepositFrequency);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.pumpkinDepositMinAltitude.name(), this.pumpkinDepositMinAltitude);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.pumpkinDepositMaxAltitude.name(), this.pumpkinDepositMaxAltitude);

        this.worldWrk.WriteModTitleSettings("Above/Below Ground Variables");
        this.worldWrk.WriteModSettings(BiomeTerrainValues.evenWaterSourceDistribution.name(), this.evenWaterSourceDistribution);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.waterSourceDepositRarity.name(), this.waterSourceDepositRarity);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.waterSourceDepositFrequency.name(), this.waterSourceDepositFrequency);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.waterSourceDepositMinAltitude.name(), this.waterSourceDepositMinAltitude);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.waterSourceDepositMaxAltitude.name(), this.waterSourceDepositMaxAltitude);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.evenLavaSourceDistribution.name(), this.evenLavaSourceDistribution);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lavaSourceDepositRarity.name(), this.lavaSourceDepositRarity);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lavaSourceDepositFrequency.name(), this.lavaSourceDepositFrequency);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lavaSourceDepositMinAltitude.name(), this.lavaSourceDepositMinAltitude);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lavaSourceDepositMaxAltitude.name(), this.lavaSourceDepositMaxAltitude);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.disableNotchPonds.name(), this.disableNotchPonds);

        this.worldWrk.WriteModTitleSettings("Below Ground Variables");
        this.worldWrk.WriteModSettings(BiomeTerrainValues.dirtDepositRarity1.name(), this.dirtDepositRarity1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.dirtDepositFrequency1.name(), this.dirtDepositFrequency1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.dirtDepositSize1.name(), this.dirtDepositSize1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.dirtDepositMinAltitude1.name(), this.dirtDepositMinAltitude1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.dirtDepositMaxAltitude1.name(), this.dirtDepositMaxAltitude1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.dirtDepositRarity2.name(), this.dirtDepositRarity2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.dirtDepositFrequency2.name(), this.dirtDepositFrequency2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.dirtDepositSize2.name(), this.dirtDepositSize2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.dirtDepositMinAltitude2.name(), this.dirtDepositMinAltitude2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.dirtDepositMaxAltitude2.name(), this.dirtDepositMaxAltitude2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.dirtDepositRarity3.name(), this.dirtDepositRarity3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.dirtDepositFrequency3.name(), this.dirtDepositFrequency3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.dirtDepositSize3.name(), this.dirtDepositSize3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.dirtDepositMinAltitude3.name(), this.dirtDepositMinAltitude3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.dirtDepositMaxAltitude3.name(), this.dirtDepositMaxAltitude3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.dirtDepositRarity4.name(), this.dirtDepositRarity4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.dirtDepositFrequency4.name(), this.dirtDepositFrequency4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.dirtDepositSize4.name(), this.dirtDepositSize4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.dirtDepositMinAltitude4.name(), this.dirtDepositMinAltitude4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.dirtDepositMaxAltitude4.name(), this.dirtDepositMaxAltitude4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.gravelDepositRarity1.name(), this.gravelDepositRarity1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.gravelDepositFrequency1.name(), this.gravelDepositFrequency1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.gravelDepositSize1.name(), this.gravelDepositSize1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.gravelDepositMinAltitude1.name(), this.gravelDepositMinAltitude1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.gravelDepositMaxAltitude1.name(), this.gravelDepositMaxAltitude1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.gravelDepositRarity2.name(), this.gravelDepositRarity2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.gravelDepositFrequency2.name(), this.gravelDepositFrequency2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.gravelDepositSize2.name(), this.gravelDepositSize2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.gravelDepositMinAltitude2.name(), this.gravelDepositMinAltitude2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.gravelDepositMaxAltitude2.name(), this.gravelDepositMaxAltitude2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.gravelDepositRarity3.name(), this.gravelDepositRarity3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.gravelDepositFrequency3.name(), this.gravelDepositFrequency3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.gravelDepositSize3.name(), this.gravelDepositSize3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.gravelDepositMinAltitude3.name(), this.gravelDepositMinAltitude3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.gravelDepositMaxAltitude3.name(), this.gravelDepositMaxAltitude3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.gravelDepositRarity4.name(), this.gravelDepositRarity4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.gravelDepositFrequency4.name(), this.gravelDepositFrequency4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.gravelDepositSize4.name(), this.gravelDepositSize4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.gravelDepositMinAltitude4.name(), this.gravelDepositMinAltitude4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.gravelDepositMaxAltitude4.name(), this.gravelDepositMaxAltitude4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.clayDepositRarity1.name(), this.clayDepositRarity1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.clayDepositFrequency1.name(), this.clayDepositFrequency1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.clayDepositSize1.name(), this.clayDepositSize1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.clayDepositMinAltitude1.name(), this.clayDepositMinAltitude1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.clayDepositMaxAltitude1.name(), this.clayDepositMaxAltitude1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.clayDepositRarity2.name(), this.clayDepositRarity2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.clayDepositFrequency2.name(), this.clayDepositFrequency2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.clayDepositSize2.name(), this.clayDepositSize2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.clayDepositMinAltitude2.name(), this.clayDepositMinAltitude2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.clayDepositMaxAltitude2.name(), this.clayDepositMaxAltitude2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.clayDepositRarity3.name(), this.clayDepositRarity3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.clayDepositFrequency3.name(), this.clayDepositFrequency3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.clayDepositSize3.name(), this.clayDepositSize3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.clayDepositMinAltitude3.name(), this.clayDepositMinAltitude3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.clayDepositMaxAltitude3.name(), this.clayDepositMaxAltitude3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.clayDepositRarity4.name(), this.clayDepositRarity4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.clayDepositFrequency4.name(), this.clayDepositFrequency4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.clayDepositSize4.name(), this.clayDepositSize4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.clayDepositMinAltitude4.name(), this.clayDepositMinAltitude4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.clayDepositMaxAltitude4.name(), this.clayDepositMaxAltitude4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.coalDepositRarity1.name(), this.coalDepositRarity1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.coalDepositFrequency1.name(), this.coalDepositFrequency1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.coalDepositSize1.name(), this.coalDepositSize1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.coalDepositMinAltitude1.name(), this.coalDepositMinAltitude1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.coalDepositMaxAltitude1.name(), this.coalDepositMaxAltitude1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.coalDepositRarity2.name(), this.coalDepositRarity2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.coalDepositFrequency2.name(), this.coalDepositFrequency2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.coalDepositSize2.name(), this.coalDepositSize2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.coalDepositMinAltitude2.name(), this.coalDepositMinAltitude2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.coalDepositMaxAltitude2.name(), this.coalDepositMaxAltitude2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.coalDepositRarity3.name(), this.coalDepositRarity3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.coalDepositFrequency3.name(), this.coalDepositFrequency3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.coalDepositSize3.name(), this.coalDepositSize3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.coalDepositMinAltitude3.name(), this.coalDepositMinAltitude3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.coalDepositMaxAltitude3.name(), this.coalDepositMaxAltitude3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.coalDepositRarity4.name(), this.coalDepositRarity4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.coalDepositFrequency4.name(), this.coalDepositFrequency4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.coalDepositSize4.name(), this.coalDepositSize4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.coalDepositMinAltitude4.name(), this.coalDepositMinAltitude4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.coalDepositMaxAltitude4.name(), this.coalDepositMaxAltitude4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.ironDepositRarity1.name(), this.ironDepositRarity1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.ironDepositFrequency1.name(), this.ironDepositFrequency1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.ironDepositSize1.name(), this.ironDepositSize1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.ironDepositMinAltitude1.name(), this.ironDepositMinAltitude1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.ironDepositMaxAltitude1.name(), this.ironDepositMaxAltitude1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.ironDepositRarity2.name(), this.ironDepositRarity2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.ironDepositFrequency2.name(), this.ironDepositFrequency2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.ironDepositSize2.name(), this.ironDepositSize2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.ironDepositMinAltitude2.name(), this.ironDepositMinAltitude2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.ironDepositMaxAltitude2.name(), this.ironDepositMaxAltitude2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.ironDepositRarity3.name(), this.ironDepositRarity3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.ironDepositFrequency3.name(), this.ironDepositFrequency3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.ironDepositSize3.name(), this.ironDepositSize3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.ironDepositMinAltitude3.name(), this.ironDepositMinAltitude3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.ironDepositMaxAltitude3.name(), this.ironDepositMaxAltitude3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.ironDepositRarity4.name(), this.ironDepositRarity4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.ironDepositFrequency4.name(), this.ironDepositFrequency4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.ironDepositSize4.name(), this.ironDepositSize4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.ironDepositMinAltitude4.name(), this.ironDepositMinAltitude4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.ironDepositMaxAltitude4.name(), this.ironDepositMaxAltitude4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.goldDepositRarity1.name(), this.goldDepositRarity1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.goldDepositFrequency1.name(), this.goldDepositFrequency1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.goldDepositSize1.name(), this.goldDepositSize1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.goldDepositMinAltitude1.name(), this.goldDepositMinAltitude1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.goldDepositMaxAltitude1.name(), this.goldDepositMaxAltitude1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.goldDepositRarity2.name(), this.goldDepositRarity2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.goldDepositFrequency2.name(), this.goldDepositFrequency2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.goldDepositSize2.name(), this.goldDepositSize2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.goldDepositMinAltitude2.name(), this.goldDepositMinAltitude2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.goldDepositMaxAltitude2.name(), this.goldDepositMaxAltitude2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.goldDepositRarity3.name(), this.goldDepositRarity3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.goldDepositFrequency3.name(), this.goldDepositFrequency3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.goldDepositSize3.name(), this.goldDepositSize3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.goldDepositMinAltitude3.name(), this.goldDepositMinAltitude3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.goldDepositMaxAltitude3.name(), this.goldDepositMaxAltitude3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.goldDepositRarity4.name(), this.goldDepositRarity4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.goldDepositFrequency4.name(), this.goldDepositFrequency4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.goldDepositSize4.name(), this.goldDepositSize4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.goldDepositMinAltitude4.name(), this.goldDepositMinAltitude4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.goldDepositMaxAltitude4.name(), this.goldDepositMaxAltitude4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.redstoneDepositRarity1.name(), this.redstoneDepositRarity1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.redstoneDepositFrequency1.name(), this.redstoneDepositFrequency1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.redstoneDepositSize1.name(), this.redstoneDepositSize1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.redstoneDepositMinAltitude1.name(), this.redstoneDepositMinAltitude1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.redstoneDepositMaxAltitude1.name(), this.redstoneDepositMaxAltitude1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.redstoneDepositRarity2.name(), this.redstoneDepositRarity2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.redstoneDepositFrequency2.name(), this.redstoneDepositFrequency2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.redstoneDepositSize2.name(), this.redstoneDepositSize2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.redstoneDepositMinAltitude2.name(), this.redstoneDepositMinAltitude2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.redstoneDepositMaxAltitude2.name(), this.redstoneDepositMaxAltitude2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.redstoneDepositRarity3.name(), this.redstoneDepositRarity3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.redstoneDepositFrequency3.name(), this.redstoneDepositFrequency3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.redstoneDepositSize3.name(), this.redstoneDepositSize3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.redstoneDepositMinAltitude3.name(), this.redstoneDepositMinAltitude3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.redstoneDepositMaxAltitude3.name(), this.redstoneDepositMaxAltitude3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.redstoneDepositRarity4.name(), this.redstoneDepositRarity4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.redstoneDepositFrequency4.name(), this.redstoneDepositFrequency4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.redstoneDepositSize4.name(), this.redstoneDepositSize4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.redstoneDepositMinAltitude4.name(), this.redstoneDepositMinAltitude4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.redstoneDepositMaxAltitude4.name(), this.redstoneDepositMaxAltitude4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.diamondDepositRarity1.name(), this.diamondDepositRarity1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.diamondDepositFrequency1.name(), this.diamondDepositFrequency1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.diamondDepositSize1.name(), this.diamondDepositSize1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.diamondDepositMinAltitude1.name(), this.diamondDepositMinAltitude1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.diamondDepositMaxAltitude1.name(), this.diamondDepositMaxAltitude1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.diamondDepositRarity2.name(), this.diamondDepositRarity2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.diamondDepositFrequency2.name(), this.diamondDepositFrequency2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.diamondDepositSize2.name(), this.diamondDepositSize2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.diamondDepositMinAltitude2.name(), this.diamondDepositMinAltitude2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.diamondDepositMaxAltitude2.name(), this.diamondDepositMaxAltitude2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.diamondDepositRarity3.name(), this.diamondDepositRarity3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.diamondDepositFrequency3.name(), this.diamondDepositFrequency3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.diamondDepositSize3.name(), this.diamondDepositSize3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.diamondDepositMinAltitude3.name(), this.diamondDepositMinAltitude3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.diamondDepositMaxAltitude3.name(), this.diamondDepositMaxAltitude3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.diamondDepositRarity4.name(), this.diamondDepositRarity4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.diamondDepositFrequency4.name(), this.diamondDepositFrequency4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.diamondDepositSize4.name(), this.diamondDepositSize4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.diamondDepositMinAltitude4.name(), this.diamondDepositMinAltitude4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.diamondDepositMaxAltitude4.name(), this.diamondDepositMaxAltitude4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lapislazuliDepositRarity1.name(), this.lapislazuliDepositRarity1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lapislazuliDepositFrequency1.name(), this.lapislazuliDepositFrequency1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lapislazuliDepositSize1.name(), this.lapislazuliDepositSize1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lapislazuliDepositMinAltitude1.name(), this.lapislazuliDepositMinAltitude1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lapislazuliDepositMaxAltitude1.name(), this.lapislazuliDepositMaxAltitude1);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lapislazuliDepositRarity2.name(), this.lapislazuliDepositRarity2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lapislazuliDepositFrequency2.name(), this.lapislazuliDepositFrequency2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lapislazuliDepositSize2.name(), this.lapislazuliDepositSize2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lapislazuliDepositMinAltitude2.name(), this.lapislazuliDepositMinAltitude2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lapislazuliDepositMaxAltitude2.name(), this.lapislazuliDepositMaxAltitude2);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lapislazuliDepositRarity3.name(), this.lapislazuliDepositRarity3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lapislazuliDepositFrequency3.name(), this.lapislazuliDepositFrequency3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lapislazuliDepositSize3.name(), this.lapislazuliDepositSize3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lapislazuliDepositMinAltitude3.name(), this.lapislazuliDepositMinAltitude3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lapislazuliDepositMaxAltitude3.name(), this.lapislazuliDepositMaxAltitude3);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lapislazuliDepositRarity4.name(), this.lapislazuliDepositRarity4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lapislazuliDepositFrequency4.name(), this.lapislazuliDepositFrequency4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lapislazuliDepositSize4.name(), this.lapislazuliDepositSize4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lapislazuliDepositMinAltitude4.name(), this.lapislazuliDepositMinAltitude4);
        this.worldWrk.WriteModSettings(BiomeTerrainValues.lapislazuliDepositMaxAltitude4.name(), this.lapislazuliDepositMaxAltitude4);

        /*
         * this.worldWrk.WriteModTitleSettings("Deposit Mod - Hell Variables");
         * this.worldWrk.WriteModSettings(BiomeTerrainValues. evenFireHellDepositDistribution.name(),
         * Boolean.valueOf(this.evenFireHellDepositDistribution ).booleanValue());
         * 
         * this.worldWrk.WriteModSettings(BiomeTerrainValues. evenLightstoneHellDepositDistribution.name(),
         * Boolean.valueOf(this.evenLightstoneHellDepositDistribution ).booleanValue());
         * this.worldWrk.WriteModSettings(BiomeTerrainValues. lavaSourceHellDepositRarity.name(),
         * this.lavaSourceHellDepositRarity); this.worldWrk.WriteModSettings(BiomeTerrainValues
         * .lavaSourceHellDepositFrequency.name(), this.lavaSourceHellDepositFrequency);
         * this.worldWrk.WriteModSettings(BiomeTerrainValues .lavaSourceHellDepositMinAltitude.name(),
         * this.lavaSourceHellDepositMinAltitude); this.worldWrk.WriteModSettings
         * (BiomeTerrainValues.lavaSourceHellDepositMaxAltitude.name(), this.lavaSourceHellDepositMaxAltitude);
         * this.worldWrk.WriteModSettings (BiomeTerrainValues.fireHellDepositRarity.name(), this.fireHellDepositRarity);
         * this.worldWrk.WriteModSettings(BiomeTerrainValues .fireHellDepositFrequency.name(),
         * this.fireHellDepositFrequency); this .worldWrk.WriteModSettings(BiomeTerrainValues.fireHellDepositMinAltitude
         * .name(), this.fireHellDepositMinAltitude); this.worldWrk.WriteModSettings
         * (BiomeTerrainValues.fireHellDepositMaxAltitude.name(), this.fireHellDepositMaxAltitude);
         * this.worldWrk.WriteModSettings(BiomeTerrainValues .lightstoneHellDepositRarity1.name(),
         * this.lightstoneHellDepositRarity1); this.worldWrk.WriteModSettings(BiomeTerrainValues
         * .lightstoneHellDepositFrequency1.name(), this.lightstoneHellDepositFrequency1);
         * this.worldWrk.WriteModSettings( BiomeTerrainValues.lightstoneHellDepositMinAltitude1.name(),
         * this.lightstoneHellDepositMinAltitude1); this.worldWrk.WriteModSettings
         * (BiomeTerrainValues.lightstoneHellDepositMaxAltitude1.name(), this.lightstoneHellDepositMaxAltitude1);
         * this.worldWrk.WriteModSettings (BiomeTerrainValues.lightstoneHellDepositRarity2.name(),
         * this.lightstoneHellDepositRarity2); this.worldWrk.WriteModSettings(BiomeTerrainValues
         * .lightstoneHellDepositFrequency2.name(), this.lightstoneHellDepositFrequency2);
         * this.worldWrk.WriteModSettings( BiomeTerrainValues.lightstoneHellDepositMinAltitude2.name(),
         * this.lightstoneHellDepositMinAltitude2); this.worldWrk.WriteModSettings
         * (BiomeTerrainValues.lightstoneHellDepositMaxAltitude2.name(), this.lightstoneHellDepositMaxAltitude2);
         * this.worldWrk.WriteModSettings (BiomeTerrainValues.brownMushroomHellDepositRarity.name(),
         * this.brownMushroomHellDepositRarity); this.worldWrk.WriteModSettings(BiomeTerrainValues
         * .brownMushroomHellDepositFrequency.name(), this.brownMushroomHellDepositFrequency);
         * this.worldWrk.WriteModSettings (BiomeTerrainValues.brownMushroomHellDepositMinAltitude.name(),
         * this.brownMushroomHellDepositMinAltitude); this.worldWrk.WriteModSettings
         * (BiomeTerrainValues.brownMushroomHellDepositMaxAltitude.name(), this.brownMushroomHellDepositMaxAltitude);
         * this.worldWrk.WriteModSettings (BiomeTerrainValues.redMushroomHellDepositRarity.name(),
         * this.redMushroomHellDepositRarity); this.worldWrk.WriteModSettings(BiomeTerrainValues
         * .redMushroomHellDepositFrequency.name(), this.redMushroomHellDepositFrequency);
         * this.worldWrk.WriteModSettings( BiomeTerrainValues.redMushroomHellDepositMinAltitude.name(),
         * this.redMushroomHellDepositMinAltitude); this.worldWrk.WriteModSettings
         * (BiomeTerrainValues.redMushroomHellDepositMaxAltitude.name(), this.redMushroomHellDepositMaxAltitude);
         */

    }


    private boolean ObjectCantSpawn(CustomObject obj, BiomeBase localBiomeBase, boolean isTree)
    {
        boolean attemptSpawn = false;

        if ((obj.spawnInBiome.size() == 0) || (obj.spawnInBiome.contains("All")))
        {
            attemptSpawn = true;
        } else if (obj.spawnInBiome.contains(localBiomeBase.n.toLowerCase()))
            attemptSpawn = true;

        return obj.branch || !attemptSpawn || (isTree && !obj.tree);
    }

    private void SpawnLegacyObject(int x, int y, int z)
    {

        int SelectedObject = this.rand.nextInt(LegacyObjects.size());
        CustomObjectLegacy legacyObject = LegacyObjects.get(SelectedObject);

        int i1 = world.getTypeId(x, y - 1, z);
        if ((y < 128 - 1))
        {

            if ((i1 == Block.GRASS.id))
            {
                ChangeWorld(x, y - 1, z, Block.DIRT.id);
            }
            if ((world.getTypeId(x, y + 2, z) != Block.WATER.id) || (legacyObject.underwater))
            {

                int j1 = world.getTypeId(x, y - 1, z);
                if ((j1 == legacyObject.spawnID))
                {
                    int DataX = 0;
                    int DataY = 0;
                    int DataZ = 0;
                    while (DataZ <= 100)
                    {
                        while (DataY <= 14)
                        {
                            while (DataX <= 14)
                            {
                                if (legacyObject.DataValues[DataX][DataY][DataZ] != 0)
                                {

                                    int Data;
                                    Data = (int) legacyObject.DataValues[DataX][DataY][DataZ];
                                    int eData = (int) Math.round(((legacyObject.DataValues[DataX][DataY][DataZ] - Data) * 100));
                                    if (!(Data == 0))
                                    {
                                        ChangeWorld(false, x + (DataX - 4), (y - 1) + (DataZ), z + (DataY - 4), Data, eData);
                                        if (DataZ == 0)
                                        {
                                            int DownChecker = 1;
                                            while (world.getTypeId(x + (DataX - 4), (y - DownChecker), z + (DataY - 4)) == 0)
                                            {
                                                ChangeWorld(x + (DataX - 4), (y - DownChecker), z + (DataY - 4), (byte) legacyObject.spawnID);
                                            }
                                        }
                                    }
                                }
                                DataX = DataX + 1;
                            }
                            DataX = 0;
                            DataY = DataY + 1;
                        }
                        DataY = 0;
                        DataZ = DataZ + 1;
                    }
                }

            }

        }

    }


    boolean SpawnCustomObjects(int chunk_x, int chunk_z, BiomeBase localBiomeBase)
    {


        if (LegacyObjects.size() > 0 && this.rand.nextInt(2) == 1)
        {
            int x = chunk_x + this.rand.nextInt(16) + 8;
            int z = chunk_z + this.rand.nextInt(16) + 8;
            int y = this.world.getHighestBlockYAt(x, z);
            SpawnLegacyObject(x, y, z);
            return true;
        }

        if (this.Objects.size() == 0)
            return false;

        boolean objectSpawned = false;
        int spawnattemps = 0;
        while (!objectSpawned)
        {
            if (spawnattemps > this.objectSpawnRatio)
                return false;

            spawnattemps++;

            CustomObject SelectedObject = Objects.get(this.rand.nextInt(Objects.size()));

            if (ObjectCantSpawn(SelectedObject, localBiomeBase, false))
                continue;

            int randomRoll = this.rand.nextInt(100);
            int ObjectRarity = SelectedObject.rarity;

            while (randomRoll < ObjectRarity)
            {
                int x = chunk_x + this.rand.nextInt(16) + 8;
                int z = chunk_z + this.rand.nextInt(16) + 8;
                int y = this.world.getHighestBlockYAt(x, z);
                ObjectRarity -= 100;

                objectSpawned = GenerateCustomObject(x, y, z, SelectedObject, false);
                // Checked Biome, Branch, Tree - soo try to generate.

                // here we spawn object and check group spawning

                if (objectSpawned && !SelectedObject.groupId.endsWith(""))
                {
                    ArrayList<CustomObject> groupList = ObjectGroups.get(SelectedObject.groupId);
                    if (groupList == null)
                        return objectSpawned;

                    int attempts = 3;
                    if ((SelectedObject.groupFrequencyMax - SelectedObject.groupFrequencyMin) > 0)
                        attempts = SelectedObject.groupFrequencyMin + this.rand.nextInt(SelectedObject.groupFrequencyMax - SelectedObject.groupFrequencyMin);

                    while (attempts > 0)
                    {
                        attempts--;

                        int objIndex = this.rand.nextInt(groupList.size());
                        CustomObject ObjectfromGroup = groupList.get(objIndex);

                        // duno about this check, but maybe it is correct
                        if (ObjectCantSpawn(ObjectfromGroup, localBiomeBase, false))
                            continue;

                        x = x + this.rand.nextInt(SelectedObject.groupSeperationMax - SelectedObject.groupSeperationMin) + SelectedObject.groupSeperationMin;
                        z = z + this.rand.nextInt(SelectedObject.groupSeperationMax - SelectedObject.groupSeperationMin) + SelectedObject.groupSeperationMin;
                        int _y = this.world.getHighestBlockYAt(x, z);
                        if ((y - _y) > 10 || (_y - y) > 10)
                            continue;
                        GenerateCustomObject(x, _y, z, ObjectfromGroup, false);


                    }

                }

            }

        }
        return objectSpawned;
    }


    public boolean SpawnCustomTrees(int x, int y, int z)
    {

        if (!this.HasCustomTrees)
            return false;

        BiomeBase localBiomeBase = this.world.getWorldChunkManager().getBiome(x, z);

        boolean objectSpawned = false;
        int spawnattemps = 0;
        while (!objectSpawned)
        {
            if (spawnattemps > this.objectSpawnRatio)
                return false;


            CustomObject SelectedObject = Objects.get(this.rand.nextInt(Objects.size()));

            if (ObjectCantSpawn(SelectedObject, localBiomeBase, true))
                continue;

            spawnattemps++;

            int randomRoll = this.rand.nextInt(100);

            if (randomRoll < SelectedObject.rarity)
            {

                objectSpawned = GenerateCustomObject(x, y, z, SelectedObject, true);
            }

        }
        return objectSpawned;
    }

    private boolean GenerateCustomObject(int x, int y, int z, CustomObject workObject, boolean notify)
    {
        /*
         * 1) ground check 
         * 2) add branches and copy all data to work array (dont change default CustomObject object) 
         * 3) collision check and rotation 
         * 4) spawn
         */

        // 1)
        if ((world.getTypeId(x, y - 5, z) == 0) && (workObject.needsFoundation))
            return false;

        boolean abort = false;
        int checkblock = world.getTypeId(x, y + 2, z);
        if (!workObject.spawnWater)
            abort = ((checkblock == Block.WATER.id) || (checkblock == Block.STATIONARY_WATER.id));
        if (!workObject.spawnLava)
            abort = ((checkblock == Block.LAVA.id) || (checkblock == Block.STATIONARY_LAVA.id));

        checkblock = world.getLightLevel(x, y + 2, z);
        if (!workObject.spawnSunlight)
            abort = (checkblock > 8);
        if (!workObject.spawnDarkness)
            abort = (checkblock < 9);

        if ((y < workObject.spawnElevationMin) || (y > workObject.spawnElevationMax))
            abort = true;

        if (!workObject.spawnOnBlockType.contains(world.getTypeId(x, y - 1, z)))
            abort = true;

        if (abort)
            return false;

        // 2)

        int index = 0;
        int branchLimit = 0;
        ArrayList<CustomObject> branchGroup = BranchGroups.get(workObject.groupId);
        ArrayList<Coordinate> workingData = new ArrayList<Coordinate>();
        while (index < workObject.Data.size())
        {
            Coordinate DataPoint = workObject.Data.get(index);
            workingData.add(DataPoint.GetCopy());

            if ((DataPoint.branchDirection != -1) && (branchGroup != null) && (branchLimit < workObject.branchLimit))
            {
                CustomObject workingBranch = branchGroup.get(this.rand.nextInt(branchGroup.size()));
                int counter = 0;
                while (counter < workingBranch.Data.size())
                {
                    Coordinate untranslatedCoordinate = workingBranch.Data.get(counter).GetCopy();
                    int directionCounter = 0;
                    while (directionCounter < (DataPoint.branchDirection))
                    {
                        untranslatedCoordinate.rotateSliceC();
                        directionCounter++;
                    }

                    workingData.add(untranslatedCoordinate.GetSumm(DataPoint));
                    counter++;
                }

            }
            index++;
        }

        // 3)
        int RotationAmount = 0;
        index = 0;
        int faultCounter = 0;

        if (workObject.randomRotation)
        {
            RotationAmount = this.rand.nextInt(3);
        }

        while (index < workingData.size())
        {
            int counter = 0;
            while (counter < RotationAmount)
            {
                workingData.get(index).rotateSliceC();
                counter++;
            }
            if (!workObject.dig)
            {
                if (world.getTypeId(x + workingData.get(index).getX(), y + workingData.get(index).getY(), z + workingData.get(index).getZ()) > 0)
                {
                    faultCounter++;
                    if (faultCounter > (workingData.size() * (workObject.collisionPercentage / 100)))
                    {
                        return false;
                    }
                }
            }
            if ((y + workingData.get(index).getY() > 127) || (y + workingData.get(index).getY() < 1))
            {
                return false;
            }
            index++;
        }

        // 4)

        index = 0;
        while (index < workingData.size())
        {
            Coordinate DataPoint = workingData.get(index);
            if (world.getTypeId(x + DataPoint.getX(), y + DataPoint.getY(), z + DataPoint.getZ()) == 0)
            {
                ChangeWorld(notify, (x + DataPoint.getX()), y + DataPoint.getY(), z + DataPoint.getZ(), DataPoint.workingData, DataPoint.workingExtra);
            } else if (DataPoint.Digs)
            {
                ChangeWorld(notify, (x + DataPoint.getX()), y + DataPoint.getY(), z + DataPoint.getZ(), DataPoint.workingData, DataPoint.workingExtra);
            }
            if ((workObject.underFill) && (world.getTypeId(x + DataPoint.getX(), y, z + DataPoint.getZ()) > 0))
            {
                int depthScanner = 0;
                int blockForFill = world.getTypeId(x, y - 1, z);
                while (depthScanner < 64)
                {
                    if (DataPoint.getY() < depthScanner)
                    {
                        int countdown = depthScanner;
                        while ((world.getTypeId(x + DataPoint.getX(), y + DataPoint.getY() - countdown, z + DataPoint.getZ()) == 0) && (countdown < 64))
                        {
                            ChangeWorld(notify, (x + DataPoint.getX()), y + DataPoint.getY() - countdown, z + DataPoint.getZ(), blockForFill, 0);
                            countdown++;
                        }
                    }
                    depthScanner++;
                }
            }
            index++;
        }
        return true;

    }


    private boolean ChangeWorld(int x, int y, int z, int type)
    {
        return world.setRawTypeId(x, y, z, type);

    }

    private boolean ChangeWorld(boolean notify, int x, int y, int z, int type, int data)
    {
        if (notify)
            return world.setTypeIdAndData(x, y, z, type, data);
        else
            return world.setRawTypeIdAndData(x, y, z, type, data);

    }

    void processAboveGroundMaterials(int x, int z, BiomeBase currentBiome)
    {
        processDepositMaterial(x, z, this.flowerDepositRarity, this.flowerDepositFrequency, this.flowerDepositMinAltitude, this.flowerDepositMaxAltitude, -1, Block.YELLOW_FLOWER.id);

        processDepositMaterial(x, z, this.roseDepositRarity, this.roseDepositFrequency, this.roseDepositMinAltitude, this.roseDepositMaxAltitude, -1, Block.RED_ROSE.id);

        processDepositMaterial(x, z, this.brownMushroomDepositRarity, this.brownMushroomDepositFrequency, this.brownMushroomDepositMinAltitude, this.brownMushroomDepositMaxAltitude, -1, Block.BROWN_MUSHROOM.id);

        processDepositMaterial(x, z, this.redMushroomDepositRarity, this.redMushroomDepositFrequency, this.redMushroomDepositMinAltitude, this.redMushroomDepositMaxAltitude, -1, Block.RED_MUSHROOM.id);

        processDepositMaterial(x, z, this.reedDepositRarity, this.reedDepositFrequency, this.reedDepositMinAltitude, this.reedDepositMaxAltitude, -1, Block.SUGAR_CANE_BLOCK.id);

        processDepositMaterial(x, z, this.pumpkinDepositRarity, this.pumpkinDepositFrequency, this.pumpkinDepositMinAltitude, this.pumpkinDepositMaxAltitude, -1, Block.PUMPKIN.id);

        processDepositMaterial(x, z, this.waterSourceDepositRarity, this.waterSourceDepositFrequency, this.waterSourceDepositMinAltitude, this.waterSourceDepositMaxAltitude, -1, Block.WATER.id);

        processDepositMaterial(x, z, this.lavaSourceDepositRarity, this.lavaSourceDepositFrequency, this.lavaSourceDepositMinAltitude, this.lavaSourceDepositMaxAltitude, -1, Block.LAVA.id);

        processDepositMaterial(x, z, this.cactusDepositRarity, this.globalCactusDensity + (currentBiome == BiomeBase.DESERT ? this.desertCactusDensity : 0), this.cactusDepositMinAltitude, this.cactusDepositMaxAltitude, -1, Block.CACTUS.id);
    }


    void processTrees(int x, int z, BiomeBase currentBiome)
    {
        if (!this.notchBiomeTrees)
            return;
        double d1 = 0.5D;
        int treeDensity = 0;
        int treeDensityVariation = (int) ((this.c.a(x * d1, z * d1) / 8.0D + this.rand.nextDouble() * 4.0D + 4.0D) / 3.0D);

        if (this.rand.nextInt(10) == 0)
            treeDensity++;

        if (currentBiome == BiomeBase.RAINFOREST)
            treeDensity += treeDensityVariation + this.rainforestTreeDensity;
        if (currentBiome == BiomeBase.SWAMPLAND)
            treeDensity += treeDensityVariation + this.swamplandTreeDensity;
        if (currentBiome == BiomeBase.SEASONAL_FOREST)
            treeDensity += treeDensityVariation + this.seasonalforestTreeDensity;
        if (currentBiome == BiomeBase.FOREST)
            treeDensity += treeDensityVariation + this.forestTreeDensity;
        if (currentBiome == BiomeBase.SAVANNA)
            treeDensity += treeDensityVariation + this.savannaTreeDensity;
        if (currentBiome == BiomeBase.SHRUBLAND)
            treeDensity += treeDensityVariation + this.shrublandTreeDensity;
        if (currentBiome == BiomeBase.TAIGA)
            treeDensity += treeDensityVariation + this.taigaTreeDensity;
        if (currentBiome == BiomeBase.DESERT)
            treeDensity += treeDensityVariation + this.desertTreeDensity;
        if (currentBiome == BiomeBase.PLAINS)
            treeDensity += treeDensityVariation + this.plainsTreeDensity;
        if (currentBiome == BiomeBase.ICE_DESERT)
            treeDensity += treeDensityVariation + this.iceDesertTreeDensity;
        if (currentBiome == BiomeBase.TUNDRA)
            treeDensity += treeDensityVariation + this.tundraTreeDensity;


        for (int i = 0; i < treeDensity; i++)
        {

            int _x = x + this.rand.nextInt(16) + 8;
            int _z = z + this.rand.nextInt(16) + 8;

            WorldGenerator localWorldGenerator = currentBiome.a(this.rand);
            localWorldGenerator.a(1.0D, 1.0D, 1.0D);
            localWorldGenerator.a(this.world, this.rand, _x, this.world.getHighestBlockYAt(_x, _z), _z);


        }

    }

    void processDepositMaterial(int _x, int _z, int rarity, int frequency, int minAltitude, int maxAltitude, int size, int type)
    {
        Random rand = this.rand;
        int xyPosMod = (type == Block.LONG_GRASS.id) || (type == Block.YELLOW_FLOWER.id) || (type == Block.RED_ROSE.id) || (type == Block.BROWN_MUSHROOM.id) || (type == Block.RED_MUSHROOM.id) || (type == Block.WATER.id) || (type == Block.LAVA.id) || (type == Block.FIRE.id) || (type == Block.CACTUS.id) || (type == Block.MOB_SPAWNER.id) || (type == Block.SUGAR_CANE_BLOCK.id) || (type == Block.PUMPKIN.id) || (type == Block.GLOWSTONE.id) ? 8 : 0;

        if ((type == Block.FIRE.id))
            frequency = rand.nextInt(rand.nextInt(frequency) + 1) + 1;
        else if ((type == Block.GLOWSTONE.id) && (size == -1))
        {
            frequency = rand.nextInt(rand.nextInt(frequency) + 1);
        }
        for (int i = 0; i < frequency; i++)
        {
            if (rand.nextInt(100) >= rarity)
                continue;
            int x = _x + rand.nextInt(16) + xyPosMod;
            int z = _z + rand.nextInt(16) + xyPosMod;
            int y = rand.nextInt(maxAltitude - minAltitude) + minAltitude;

            if ((type == Block.YELLOW_FLOWER.id) || (type == Block.RED_ROSE.id || (type == Block.BROWN_MUSHROOM.id) || (type == Block.RED_MUSHROOM.id)))
                new WorldGenFlowers(type).a(this.world, rand, x, y, z);
            else if (type == Block.CACTUS.id)
                new WorldGenCactus().a(this.world, rand, x, y, z);
            else if (type == Block.SUGAR_CANE_BLOCK.id)
                new WorldGenReed().a(this.world, rand, x, y, z);
            else if (type == Block.PUMPKIN.id)
                new WorldGenPumpkin().a(this.world, rand, x, y, z);
            else if (type == Block.CLAY.id)
                new WorldGenClay(size).a(this.world, rand, x, y, z);
            else if (type == Block.WATER.id)
            {
                if (!this.evenWaterSourceDistribution)
                    y = rand.nextInt(rand.nextInt(maxAltitude - minAltitude) + minAltitude + 1);
                new WorldGenLiquids(type).a(this.world, rand, x, y, z);
            } else if (type == Block.LAVA.id)
            {
                if (!this.evenLavaSourceDistribution)
                    y = rand.nextInt(rand.nextInt(maxAltitude - minAltitude) + minAltitude + 1);
                new WorldGenLiquids(type).a(this.world, rand, x, y, z);
            } else if (type == Block.MOB_SPAWNER.id)
                new WorldGenDungeons().a(this.world, rand, x, y, z);
            else
                new WorldGenMinable(type, size).a(this.world, rand, x, y, z);
        }
    }

    void processGrass(int x, int z, BiomeBase currentBiome)
    {
        int grassDensity = 0;
        if (currentBiome == BiomeBase.FOREST)
            grassDensity = 2;
        if (currentBiome == BiomeBase.RAINFOREST)
            grassDensity = 10;
        if (currentBiome == BiomeBase.SEASONAL_FOREST)
            grassDensity = 2;
        if (currentBiome == BiomeBase.TAIGA)
            grassDensity = 1;
        if (currentBiome == BiomeBase.PLAINS)
            grassDensity = 10;

        int _x;
        int _y;
        int _z;

        for (int i = 0; i < grassDensity; i++)
        {
            int grassType = 1;

            if ((currentBiome == BiomeBase.RAINFOREST) && (this.rand.nextInt(3) != 0))
                grassType = 2;

            _x = x + this.rand.nextInt(16) + 8;
            _y = this.rand.nextInt(128);
            _z = z + this.rand.nextInt(16) + 8;
            new WorldGenGrass(Block.LONG_GRASS.id, grassType).a(this.world, this.rand, _x, _y, _z);
        }

        if (currentBiome == BiomeBase.DESERT)
        {

            for (int i = 0; i < 2; i++)
            {
                _x = x + this.rand.nextInt(16) + 8;
                _y = this.rand.nextInt(128);
                _z = z + this.rand.nextInt(16) + 8;
                new WorldGenDeadBush(Block.DEAD_BUSH.id).a(this.world, this.rand, _x, _y, _z);
            }
        }
    }

    void processUndergroundDeposits(int x, int z, BiomeBase currentBiome)
    {

        processDepositMaterial(x, z, this.dungeonRarity, this.dungeonFrequency, this.dungeonMinAltitude, this.dungeonMaxAltitude, -1, Block.MOB_SPAWNER.id);

        processDepositMaterial(x, z, this.dirtDepositRarity1, this.dirtDepositFrequency1, this.dirtDepositMinAltitude1, this.dirtDepositMaxAltitude1, this.dirtDepositSize1, Block.DIRT.id);

        processDepositMaterial(x, z, this.dirtDepositRarity2, this.dirtDepositFrequency2, this.dirtDepositMinAltitude2, this.dirtDepositMaxAltitude2, this.dirtDepositSize2, Block.DIRT.id);

        processDepositMaterial(x, z, this.dirtDepositRarity3, this.dirtDepositFrequency3, this.dirtDepositMinAltitude3, this.dirtDepositMaxAltitude3, this.dirtDepositSize3, Block.DIRT.id);

        processDepositMaterial(x, z, this.dirtDepositRarity4, this.dirtDepositFrequency4, this.dirtDepositMinAltitude4, this.dirtDepositMaxAltitude4, this.dirtDepositSize4, Block.DIRT.id);

        processDepositMaterial(x, z, this.gravelDepositRarity1, this.gravelDepositFrequency1, this.gravelDepositMinAltitude1, this.gravelDepositMaxAltitude1, this.gravelDepositSize1, Block.GRAVEL.id);

        processDepositMaterial(x, z, this.gravelDepositRarity2, this.gravelDepositFrequency2, this.gravelDepositMinAltitude2, this.gravelDepositMaxAltitude2, this.gravelDepositSize2, Block.GRAVEL.id);

        processDepositMaterial(x, z, this.gravelDepositRarity3, this.gravelDepositFrequency3, this.gravelDepositMinAltitude3, this.gravelDepositMaxAltitude3, this.gravelDepositSize3, Block.GRAVEL.id);

        processDepositMaterial(x, z, this.gravelDepositRarity4, this.gravelDepositFrequency4, this.gravelDepositMinAltitude4, this.gravelDepositMaxAltitude4, this.gravelDepositSize4, Block.GRAVEL.id);

        processDepositMaterial(x, z, this.clayDepositRarity1, this.clayDepositFrequency1, this.clayDepositMinAltitude1, this.clayDepositMaxAltitude1, this.clayDepositSize1, Block.CLAY.id);

        processDepositMaterial(x, z, this.clayDepositRarity2, this.clayDepositFrequency2, this.clayDepositMinAltitude2, this.clayDepositMaxAltitude2, this.clayDepositSize2, Block.CLAY.id);

        processDepositMaterial(x, z, this.clayDepositRarity3, this.clayDepositFrequency3, this.clayDepositMinAltitude3, this.clayDepositMaxAltitude3, this.clayDepositSize3, Block.CLAY.id);

        processDepositMaterial(x, z, this.clayDepositRarity4, this.clayDepositFrequency4, this.clayDepositMinAltitude4, this.clayDepositMaxAltitude4, this.clayDepositSize4, Block.CLAY.id);

        processDepositMaterial(x, z, this.coalDepositRarity1, this.coalDepositFrequency1, this.coalDepositMinAltitude1, this.coalDepositMaxAltitude1, this.coalDepositSize1, Block.COAL_ORE.id);

        processDepositMaterial(x, z, this.coalDepositRarity2, this.coalDepositFrequency2, this.coalDepositMinAltitude2, this.coalDepositMaxAltitude2, this.coalDepositSize2, Block.COAL_ORE.id);

        processDepositMaterial(x, z, this.coalDepositRarity3, this.coalDepositFrequency3, this.coalDepositMinAltitude3, this.coalDepositMaxAltitude3, this.coalDepositSize3, Block.COAL_ORE.id);

        processDepositMaterial(x, z, this.coalDepositRarity4, this.coalDepositFrequency4, this.coalDepositMinAltitude4, this.coalDepositMaxAltitude4, this.coalDepositSize4, Block.COAL_ORE.id);

        processDepositMaterial(x, z, this.ironDepositRarity1, this.ironDepositFrequency1, this.ironDepositMinAltitude1, this.ironDepositMaxAltitude1, this.ironDepositSize1, Block.IRON_ORE.id);

        processDepositMaterial(x, z, this.ironDepositRarity2, this.ironDepositFrequency2, this.ironDepositMinAltitude2, this.ironDepositMaxAltitude2, this.ironDepositSize2, Block.IRON_ORE.id);

        processDepositMaterial(x, z, this.ironDepositRarity3, this.ironDepositFrequency3, this.ironDepositMinAltitude3, this.ironDepositMaxAltitude3, this.ironDepositSize3, Block.IRON_ORE.id);

        processDepositMaterial(x, z, this.ironDepositRarity4, this.ironDepositFrequency4, this.ironDepositMinAltitude4, this.ironDepositMaxAltitude4, this.ironDepositSize4, Block.IRON_ORE.id);

        processDepositMaterial(x, z, this.goldDepositRarity1, this.goldDepositFrequency1, this.goldDepositMinAltitude1, this.goldDepositMaxAltitude1, this.goldDepositSize1, Block.GOLD_ORE.id);

        processDepositMaterial(x, z, this.goldDepositRarity2, this.goldDepositFrequency2, this.goldDepositMinAltitude2, this.goldDepositMaxAltitude2, this.goldDepositSize2, Block.GOLD_ORE.id);

        processDepositMaterial(x, z, this.goldDepositRarity3, this.goldDepositFrequency3, this.goldDepositMinAltitude3, this.goldDepositMaxAltitude3, this.goldDepositSize3, Block.GOLD_ORE.id);

        processDepositMaterial(x, z, this.goldDepositRarity4, this.goldDepositFrequency4, this.goldDepositMinAltitude4, this.goldDepositMaxAltitude4, this.goldDepositSize4, Block.GOLD_ORE.id);

        processDepositMaterial(x, z, this.redstoneDepositRarity1, this.redstoneDepositFrequency1, this.redstoneDepositMinAltitude1, this.redstoneDepositMaxAltitude1, this.redstoneDepositSize1, Block.REDSTONE_ORE.id);

        processDepositMaterial(x, z, this.redstoneDepositRarity2, this.redstoneDepositFrequency2, this.redstoneDepositMinAltitude2, this.redstoneDepositMaxAltitude2, this.redstoneDepositSize2, Block.REDSTONE_ORE.id);

        processDepositMaterial(x, z, this.redstoneDepositRarity3, this.redstoneDepositFrequency3, this.redstoneDepositMinAltitude3, this.redstoneDepositMaxAltitude3, this.redstoneDepositSize3, Block.REDSTONE_ORE.id);

        processDepositMaterial(x, z, this.redstoneDepositRarity4, this.redstoneDepositFrequency4, this.redstoneDepositMinAltitude4, this.redstoneDepositMaxAltitude4, this.redstoneDepositSize4, Block.REDSTONE_ORE.id);

        processDepositMaterial(x, z, this.diamondDepositRarity1, this.diamondDepositFrequency1, this.diamondDepositMinAltitude1, this.diamondDepositMaxAltitude1, this.diamondDepositSize1, Block.DIAMOND_ORE.id);

        processDepositMaterial(x, z, this.diamondDepositRarity2, this.diamondDepositFrequency2, this.diamondDepositMinAltitude2, this.diamondDepositMaxAltitude2, this.diamondDepositSize2, Block.DIAMOND_ORE.id);

        processDepositMaterial(x, z, this.diamondDepositRarity3, this.diamondDepositFrequency3, this.diamondDepositMinAltitude3, this.diamondDepositMaxAltitude3, this.diamondDepositSize3, Block.DIAMOND_ORE.id);

        processDepositMaterial(x, z, this.diamondDepositRarity4, this.diamondDepositFrequency4, this.diamondDepositMinAltitude4, this.diamondDepositMaxAltitude4, this.diamondDepositSize4, Block.DIAMOND_ORE.id);

        processDepositMaterial(x, z, this.lapislazuliDepositRarity1, this.lapislazuliDepositFrequency1, this.lapislazuliDepositMinAltitude1, this.lapislazuliDepositMaxAltitude1, this.lapislazuliDepositSize1, Block.LAPIS_ORE.id);

        processDepositMaterial(x, z, this.lapislazuliDepositRarity2, this.lapislazuliDepositFrequency2, this.lapislazuliDepositMinAltitude2, this.lapislazuliDepositMaxAltitude2, this.lapislazuliDepositSize2, Block.LAPIS_ORE.id);

        processDepositMaterial(x, z, this.lapislazuliDepositRarity3, this.lapislazuliDepositFrequency3, this.lapislazuliDepositMinAltitude3, this.lapislazuliDepositMaxAltitude3, this.lapislazuliDepositSize3, Block.LAPIS_ORE.id);

        processDepositMaterial(x, z, this.lapislazuliDepositRarity4, this.lapislazuliDepositFrequency4, this.lapislazuliDepositMinAltitude4, this.lapislazuliDepositMaxAltitude4, this.lapislazuliDepositSize4, Block.LAPIS_ORE.id);


        if (this.undergroundLakes)
            processUndergroundLakes(x, z);
    }


    void processUndergroundLakes(int x, int z)
    {
        for (int i = 0; i < this.undergroundLakeFrequency; i++)
        {
            if (this.rand.nextInt(100) >= this.undergroundLakeRarity)
                continue;
            int xR = x + this.rand.nextInt(16);
            int yR = this.rand.nextInt(this.undergroundLakeMaxAltitude - this.undergroundLakeMinAltitude) + this.undergroundLakeMinAltitude;
            int zR = z + this.rand.nextInt(16);
            createUndergroundLake(this.rand.nextInt(this.undergroundLakeMaxSize - this.undergroundLakeMinSize) + this.undergroundLakeMinSize, xR, yR, zR);
        }
    }

    private void createUndergroundLake(int size, int x, int y, int z)
    {
        float mPi = this.rand.nextFloat() * 3.141593F;

        double x1 = x + 8 + MathHelper.sin(mPi) * size / 8.0F;
        double x2 = x + 8 - MathHelper.sin(mPi) * size / 8.0F;
        double z1 = z + 8 + MathHelper.cos(mPi) * size / 8.0F;
        double z2 = z + 8 - MathHelper.cos(mPi) * size / 8.0F;

        double y1 = y + this.rand.nextInt(3) + 2;
        double y2 = y + this.rand.nextInt(3) + 2;

        for (int i = 0; i <= size; i++)
        {
            double xAdjusted = x1 + (x2 - x1) * i / size;
            double yAdjusted = y1 + (y2 - y1) * i / size;
            double zAdjusted = z1 + (z2 - z1) * i / size;

            double horizontalSizeMultiplier = this.rand.nextDouble() * size / 16.0D;
            double verticalSizeMultiplier = this.rand.nextDouble() * size / 32.0D;
            double horizontalSize = (MathHelper.sin(i * 3.141593F / size) + 1.0F) * horizontalSizeMultiplier + 1.0D;
            double verticalSize = (MathHelper.sin(i * 3.141593F / size) + 1.0F) * verticalSizeMultiplier + 1.0D;

            for (int xLake = (int) (xAdjusted - horizontalSize / 2.0D); xLake <= (int) (xAdjusted + horizontalSize / 2.0D); xLake++)
                for (int yLake = (int) (yAdjusted - verticalSize / 2.0D); yLake <= (int) (yAdjusted + verticalSize / 2.0D); yLake++)
                    for (int zLake = (int) (zAdjusted - horizontalSize / 2.0D); zLake <= (int) (zAdjusted + horizontalSize / 2.0D); zLake++)
                    {
                        double xBounds = (xLake + 0.5D - xAdjusted) / (horizontalSize / 2.0D);
                        double yBounds = (yLake + 0.5D - yAdjusted) / (verticalSize / 2.0D);
                        double zBounds = (zLake + 0.5D - zAdjusted) / (horizontalSize / 2.0D);
                        if (xBounds * xBounds + yBounds * yBounds + zBounds * zBounds >= 1.0D)
                            continue;
                        int uBlock = world.getTypeId(xLake, yLake - 1, zLake);
                        if ((yLake < y + 2) && ((this.undergroundLakesInAir) || (uBlock != 0))) // not air
                            this.ChangeWorld(xLake, yLake, zLake, Block.WATER.id);
                        else
                            this.ChangeWorld(xLake, yLake, zLake, 0); // Air block
                    }
        }
    }

    @Override
    public void populate(org.bukkit.World wrld, Random random, Chunk chunk)
    {
        this.world = ((CraftWorld)wrld).getHandle();
        this.rand = random;
        if(this.c == null)
            this.c = new NoiseGeneratorOctaves(this.rand, 8);
        int x = chunk.getX();
        int z = chunk.getZ();

        int i1 = x * 16;
        int i2 = z * 16;

        BiomeBase localBiomeBase = world.getWorldChunkManager().getBiome(i1 + 16, i2 + 16);


        this.rand.setSeed(world.getSeed());
        long l1 = this.rand.nextLong() / 2L * 2L + 1L;
        long l2 = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed(x * l1 + z * l2 ^ world.getSeed());


        this.processUndergroundDeposits(i1, i2, localBiomeBase);
        // ToDo add lavaLevelMin and lavaLevelMax here
        if (this.notchBiomeTrees)
        {
            if (this.rand.nextInt(4) == 0)
            {
                int i3 = i1 + this.rand.nextInt(16) + 8;
                int i4 = this.rand.nextInt(128);
                int i5 = i2 + this.rand.nextInt(16) + 8;
                new WorldGenLakes(Block.STATIONARY_WATER.id).a(this.world, this.rand, i3, i4, i5);
            }

            if (this.rand.nextInt(8) == 0)
            {
                int i3 = i1 + this.rand.nextInt(16) + 8;
                int i4 = this.rand.nextInt(this.rand.nextInt(120) + 8);
                int i5 = i2 + this.rand.nextInt(16) + 8;
                if ((i4 < 64) || (this.rand.nextInt(10) == 0))
                    new WorldGenLakes(Block.STATIONARY_LAVA.id).a(this.world, this.rand, i3, i4, i5);
            }
        }
        int i3;
        int i4 = 0;
        int i5;
        int i6;
        int i7;

        this.processAboveGroundMaterials(i1, i2, localBiomeBase);


        this.SpawnCustomObjects(i1, i2, localBiomeBase);


        this.processTrees(i1, i2, localBiomeBase);

        this.processGrass(i1, i2, localBiomeBase);


        i6 = 0;

        int i8;
        int i9;
        int i10;
        int i11;


        double[] TemperatureArray = new double[256];
        TemperatureArray = this.world.getWorldChunkManager().a(TemperatureArray, i1 + 8, i2 + 8, 16, 16);
        for (i6 = i1 + 8; i6 < i1 + 8 + 16; i6++)
        {
            for (i7 = i2 + 8; i7 < i2 + 8 + 16; i7++)
            {
                i8 = i6 - (i1 + 8);
                i9 = i7 - (i2 + 8);
                i10 = this.world.e(i6, i7);
                double d2 = TemperatureArray[(i8 * 16 + i9)] - (i10 - 64) / 64.0D * 0.3D;
                if ((d2 >= this.worldWrk.getSnowThreshold()) || (i10 <= 0) || (i10 >= 128) || (!this.world.isEmpty(i6, i10, i7)) || (!this.world.getMaterial(i6, i10 - 1, i7).isSolid()) || (this.world.getMaterial(i6, i10 - 1, i7) == Material.ICE))
                    continue;
                this.world.setTypeId(i6, i10, i7, Block.SNOW.id);
            }

        }

        this.worldWrk.ReplaceBlocks(x, z);
    }
}