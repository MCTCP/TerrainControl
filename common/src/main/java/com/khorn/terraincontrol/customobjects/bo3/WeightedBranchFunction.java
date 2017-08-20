package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.customobjects.Branch;
import com.khorn.terraincontrol.customobjects.CustomObjectCoordinate;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.Rotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

public class WeightedBranchFunction extends BranchFunction implements Branch
{    	
	public double cumulativeChance = 0;
	
    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        branches = new TreeSet<BranchNode>();
        branchesOTGPlus = new ArrayList<BranchNode>();
        cumulativeChance = readArgs(args, true);
    }

    @Override
    public CustomObjectCoordinate toCustomObjectCoordinate(LocalWorld world, Random random, Rotation rotation, int x, int y, int z, String startBO3Name)
    {
    	if(world.getConfigs().getWorldConfig().IsOTGPlus)
    	{
	    	int cumulativeChance = 0;
	    	for (BranchNode branch : branchesOTGPlus)
	    	{
	    		cumulativeChance += branch.getChance();
	    	}    	
	    	if(cumulativeChance > totalChance)
	    	{
	    		totalChance = cumulativeChance;
	    	}
	    	
	        double randomChance = random.nextDouble() * totalChance;
	             
	        for(BranchNode branch : branchesOTGPlus)
	        {
	        	double branchRarity = branch.getChance();
	            if (branchRarity > 0 && branchRarity >= randomChance)
	            {
	            	if(this.getHolder().isOTGPlus)
	            	{
		                CustomObjectCoordinate rotatedCoords = RotateCoords(this.x, this.y, this.z, rotation);               
		                Rotation newRotation = Rotation.getRotation((rotation.getRotationId() + branch.getRotation().getRotationId()) % 4);
		                return new CustomObjectCoordinate(world, branch.getCustomObject(false, world), branch.customObjectName, newRotation, x + rotatedCoords.getX(), y + rotatedCoords.getY(), z + rotatedCoords.getZ(), true, branch.branchDepth, branch.isRequiredBranch);
	            	} else {
	            		return new CustomObjectCoordinate(world, branch.getCustomObject(false, world), branch.customObjectName, branch.getRotation(), x + this.x, y + this.y, z + this.z);
	            	}
	            }
	            randomChance -= branch.getChance();
	            if(randomChance < 0)
	            {
	            	randomChance = 0;
	            }
	        }
    	} else {
            double randomChance = random.nextDouble() * (totalChanceSet
                    ? totalChance
                    : (cumulativeChance >= 100
                       ? cumulativeChance
                       : 100));
			for (BranchNode branch : branches)
			{
				if (branch.getChance() >= randomChance)
				{
					return new CustomObjectCoordinate(world, branch.getCustomObject(false, world), branch.customObjectName, branch.getRotation(), x + this.x, y + this.y, z + this.z);
				}
			}   		
    	}
        return null;
    }
    
    //TODO: The exact same method exists in multiple classes, merge into one method?
    private CustomObjectCoordinate RotateCoords(int x, int y, int z, Rotation newRotation)
    {
        // Assuming initial rotation is always north
    	
    	int newX = 0;
    	int newY = 0;
    	int newZ = 0;
    	int rotations = 0;
    	// How many counter-clockwise rotations have to be applied?
		if(newRotation == Rotation.WEST)
		{
			rotations = 1;
		}
		else if(newRotation == Rotation.SOUTH)
		{
			rotations = 2;    			
		}
		else if(newRotation == Rotation.EAST)
		{
			rotations = 3;    			
		}
    
    	if(rotations == 0)
    	{
    		newX = x;
    		newZ = z;
    	}
    	if(rotations == 1)
    	{
    		newX = z;
    		newZ = -x;    		
    	}
    	if(rotations == 2)
    	{
    		newX = -x; 
    		newZ = -z;
    	}
    	if(rotations == 3)
    	{
    		newX = -z;
    		newZ = x;
    	}    	
    	newY = y;
    	
    	return new CustomObjectCoordinate(null, null, null, newRotation, newX, newY, newZ, false, 0, false);
    }      

    @Override
    protected String getConfigName()
    {
        return "WeightedBranch";
    }

}
