package com.Khorn.PTMBukkit;

import java.util.ArrayList;
import java.util.HashSet;

class CustomObject
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
    
    public void CorrectSettings()
    {
		for (int blockid : spawnOnBlockType)
		{
			if (blockid > 96 || blockid == 0)
				spawnOnBlockType.remove(blockid);
		}
		
    	if(spawnOnBlockType.size() == 0)
    		spawnOnBlockType.add(2);

    	
    	if( rarity == 0 || rarity > 1000)
    		rarity = 10;
    	
    	if( collisionPercentage == 0 || collisionPercentage > 100)
    		collisionPercentage = 2;
    	
    	if( spawnElevationMin > 128)
    		spawnElevationMin = 0;
    	
    	if(  spawnElevationMax > 128)
    		spawnElevationMax = 128;
    	
    	if( spawnElevationMax < spawnElevationMin)
    	{
    		spawnElevationMax = 128;
    		spawnElevationMin = 0;
    	}
    	if( branchLimit == 0 || branchLimit> 16 )
    		branchLimit =6;
    	
    	if (groupFrequencyMin == 0 || groupFrequencyMin > 100)
    		groupFrequencyMin = 1;
    	if (groupFrequencyMax == 0 || groupFrequencyMax >100)
    		groupFrequencyMax = 5;
    	
    	if( groupFrequencyMax < groupFrequencyMin)
    	{
    		groupFrequencyMin = 1;
    		groupFrequencyMax =5;
    	}
    	
    	if (groupSeperationMin > 16)
    		groupSeperationMin = 0;
    	if (groupSeperationMax > 16)
    		groupSeperationMax = 5;
    	if(groupSeperationMax < groupSeperationMin)
    	{
    		groupSeperationMin = 0;
    		groupSeperationMax = 5;
    	}
    	
    	if(spawnInBiome.size() == 0)
    		spawnInBiome.add("All");
    	
    }
}