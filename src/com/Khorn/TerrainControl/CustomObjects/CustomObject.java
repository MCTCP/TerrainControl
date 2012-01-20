package com.Khorn.TerrainControl.CustomObjects;

import net.minecraft.server.BiomeBase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;

public class CustomObject
{


    public ArrayList<Coordinate> Data = new ArrayList<Coordinate>();
    public HashSet<Integer> spawnOnBlockType = (new HashSet<Integer>());
    public boolean spawnSunlight = true;
    public boolean spawnDarkness = false;
    public boolean spawnWater = false;
    public boolean spawnLava = false;
    public boolean underFill = true;
    public boolean dig = true;
    public int rarity = 10;
    public int spawnElevationMin = 0;
    public int spawnElevationMax = 128;
    public boolean randomRotation = true;
    public String groupId = "";
    public boolean tree = false;
    public boolean branch = false;
    public boolean diggingBranch = false;
    public int groupFrequencyMin = 1;
    public int groupFrequencyMax = 5;
    public int groupSeperationMin = 0;
    public int groupSeperationMax = 5;
    public HashSet<String> spawnInBiome = new HashSet<String>();
    public double collisionPercentage = 2;
    public int branchLimit = 6;
    public boolean needsFoundation = true;
    public String name = "";
    public String version = "1";
    public boolean IsValid = false;


    public CustomObject(File objectFile)
    {

        try
        {

            BufferedReader ObjectProps = new BufferedReader(new FileReader(objectFile));

            String workingString = ObjectProps.readLine();
            if (!workingString.equals("[META]"))
            {
                System.out.println("Invalid BOB Plugin: " + objectFile.getName());
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
                                this.spawnOnBlockType.add(Integer.parseInt(blocks[counter]));
                                counter++;
                            }
                        }
                        if (stringSet[0].equals("spawnSunlight"))
                        {
                            if (stringSet[1].toLowerCase().equals("false"))
                            {
                                this.spawnSunlight = false;
                            }
                        }
                        if (stringSet[0].equals("spawnDarkness"))
                        {
                            if (stringSet[1].toLowerCase().equals("true"))
                            {
                                this.spawnDarkness = true;
                            }
                        }
                        if (stringSet[0].equals("spawnWater"))
                        {
                            if (stringSet[1].toLowerCase().equals("true"))
                            {
                                this.spawnWater = true;
                            }
                        }
                        if (stringSet[0].equals("spawnLava"))
                        {
                            if (stringSet[1].toLowerCase().equals("true"))
                            {
                                this.spawnLava = true;
                            }
                        }
                        if (stringSet[0].equals("underFill"))
                        {
                            if (stringSet[1].toLowerCase().equals("false"))
                            {
                                this.underFill = false;
                            }
                        }
                        if (stringSet[0].equals("randomRotation"))
                        {
                            if (stringSet[1].toLowerCase().equals("false"))
                            {
                                this.randomRotation = false;
                            }
                        }
                        if (stringSet[0].equals("dig"))
                        {
                            if (stringSet[1].toLowerCase().equals("true"))
                            {
                                this.dig = true;
                            }
                        }
                        if (stringSet[0].equals("rarity"))
                        {
                            this.rarity = Integer.parseInt(stringSet[1]);
                        }
                        if (stringSet[0].equals("spawnElevationMin"))
                        {
                            this.spawnElevationMin = Integer.parseInt(stringSet[1]);
                        }
                        if (stringSet[0].equals("spawnElevationMax"))
                        {
                            this.spawnElevationMax = Integer.parseInt(stringSet[1]);
                        }
                        if (stringSet[0].equals("groupId"))
                        {
                            this.groupId = stringSet[1];
                        }
                        if (stringSet[0].equals("tree"))
                        {
                            if (stringSet[1].toLowerCase().equals("true"))
                            {
                                this.tree = true;
                            }
                        }
                        if (stringSet[0].equals("branch"))
                        {
                            if (stringSet[1].toLowerCase().equals("true"))
                            {
                                this.branch = true;
                            }
                        }
                        if (stringSet[0].equals("diggingBranch"))
                        {
                            if (stringSet[1].toLowerCase().equals("true"))
                            {
                                this.diggingBranch = true;
                            }
                        }
                        if (stringSet[0].equals("groupFrequencyMin"))
                        {
                            this.groupFrequencyMin = Integer.parseInt(stringSet[1]);
                        }
                        if (stringSet[0].equals("groupFrequencyMax"))
                        {
                            this.groupFrequencyMax = Integer.parseInt(stringSet[1]);
                        }
                        if (stringSet[0].equals("groupSeperationMin"))
                        {
                            this.groupSeperationMin = Integer.parseInt(stringSet[1]);
                        }
                        if (stringSet[0].equals("groupSeperationMax"))
                        {
                            this.groupSeperationMax = Integer.parseInt(stringSet[1]);
                        }
                        if (stringSet[0].equals("collisionPercentage"))
                        {
                            this.collisionPercentage = (Integer.parseInt(stringSet[1]) / 100);
                        }
                        if (stringSet[0].equals("spawnInBiome"))
                        {
                            stringSet = stringSet[1].split(",");
                            int counter = 0;
                            while (counter < stringSet.length)
                            {
                                if (stringSet[counter].equals("Icedesert"))
                                    this.spawnInBiome.add("ice desert");
                                if (stringSet[counter].equals("Rain Forest"))
                                    this.spawnInBiome.add("rainforest");
                                else if (stringSet[counter].equals("Seasonalforest"))
                                    this.spawnInBiome.add("seasonal forest");
                                else
                                    this.spawnInBiome.add(stringSet[counter].toLowerCase());
                                counter++;
                            }
                        }
                        if (stringSet[0].equals("branchLimit"))
                        {
                            this.branchLimit = (Integer.parseInt(stringSet[1]));
                        }
                        if (stringSet[0].equals("needsFoundation"))
                        {
                            if (stringSet[1].toLowerCase().equals("false"))
                            {
                                this.needsFoundation = false;
                            }
                        }
                        if (stringSet[0].equals("version"))
                        {
                            this.version = stringSet[1].toLowerCase();
                        }

                    } else if (workingString.equals("[DATA]"))
                        dataReached = true;
                    continue;
                }

                String[] CoordinateSet = workingString.split(":")[0].split(",");
                String BlockString = workingString.split(":")[1];
                Coordinate Coordinates;
                if (this.dig)
                {
                    Coordinates = new Coordinate(Integer.parseInt(CoordinateSet[0]), Integer.parseInt(CoordinateSet[2]), Integer.parseInt(CoordinateSet[1]), BlockString, true);
                } else
                {
                    Coordinates = new Coordinate(Integer.parseInt(CoordinateSet[0]), Integer.parseInt(CoordinateSet[2]), Integer.parseInt(CoordinateSet[1]), BlockString, false);

                }
                Coordinates.RegisterData();
                this.Data.add(Coordinates);

            }

            if (!dataReached)
            {
                System.out.println("Invalid BOB Plugin: " + objectFile.getName());
                ObjectProps.close();
                return;
            }

            this.name = objectFile.getName().substring(0,objectFile.getName().length()-4);
            this.CorrectSettings();
            this.IsValid = true;

        } catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Invalid BOB Plugin: " + objectFile.getName());
        }

    }

    public void CorrectSettings()
    {
        for (int blockid : spawnOnBlockType)
        {
            if (blockid > 96 || blockid == 0)
                spawnOnBlockType.remove(blockid);
        }

        if (spawnOnBlockType.size() == 0)
        {
            spawnOnBlockType.add(2);
            spawnOnBlockType.add(3);
        }


        if (rarity == 0 || rarity > 1000)
            rarity = 10;

        if (collisionPercentage == 0 || collisionPercentage > 100)
            collisionPercentage = 2;

        if (spawnElevationMin > 128)
            spawnElevationMin = 0;

        if (spawnElevationMax > 128)
            spawnElevationMax = 128;

        if (spawnElevationMax < spawnElevationMin)
        {
            spawnElevationMax = 128;
            spawnElevationMin = 0;
        }
        if (branchLimit == 0 || branchLimit > 16)
            branchLimit = 6;

        if (groupFrequencyMin == 0 || groupFrequencyMin > 100)
            groupFrequencyMin = 1;
        if (groupFrequencyMax == 0 || groupFrequencyMax > 100)
            groupFrequencyMax = 5;

        if (groupFrequencyMax < groupFrequencyMin)
        {
            groupFrequencyMin = 1;
            groupFrequencyMax = 5;
        }

        if (groupSeperationMin > 16)
            groupSeperationMin = 0;
        if (groupSeperationMax > 16)
            groupSeperationMax = 5;
        if (groupSeperationMax < groupSeperationMin)
        {
            groupSeperationMin = 0;
            groupSeperationMax = 5;
        }

        if (spawnInBiome.size() == 0)
            spawnInBiome.add("all");

    }

    public boolean canSpawnInBiome(BiomeBase localBiome)
    {
        return this.spawnInBiome.contains("all") || this.spawnInBiome.contains(localBiome.r.toLowerCase());

    }


}