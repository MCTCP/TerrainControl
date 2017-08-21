package com.pg85.otg.customobjects.bo3;

import com.pg85.otg.LocalWorld;
import com.pg85.otg.OTG;
import com.pg85.otg.customobjects.Branch;
import com.pg85.otg.customobjects.CustomObjectCoordinate;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.Rotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

public class WeightedBranchFunction extends BranchFunction implements Branch
{    	
	public double cumulativeChance = 0;

    boolean isOTGPlus;
    protected double readArgs(List<String> args, boolean accumulateChances) throws InvalidConfigException
    {   
        double cumulativeChance = 0;
        isOTGPlus = this.getHolder().isOTGPlus;
    	if(isOTGPlus)
    	{
    		// assureSize only returns false if size() < size 
    		assureSize(8, args);			    	
    		
	        x = readInt(args.get(0), -32, 32);
	        y = readInt(args.get(1), -255, 255);
	        z = readInt(args.get(2), -32, 32);        
	        isRequiredBranch = readBoolean(args.get(3));

	        if(isRequiredBranch)
	        {
    			String branchString = "";
    			for(String arg : args)
    			{
    				branchString += ",";	
    			}
    			
	    		if(OTG.getPluginConfig().SpawnLog)
	    		{
	    			OTG.log(LogMarker.WARN, "isRequired:true branches cannot have multiple BO3's with a rarity, only one BO3 per isRequired:true branch is allowed and the branch automatically has a 100% chance to spawn. WeightedBranch() can only be used with isRequired:false. Branch: WeightedBranch(" + branchString.substring(0, branchString.length()  - 1) + ")");    		
	    		}
	    		throw new InvalidConfigException("isRequired:true branches cannot have multiple BO3's with a rarity, only one BO3 per isRequired:true branch is allowed and the branch automatically has a 100% chance to spawn. WeightedBranch() can only be used with isRequired:false. Branch: WeightedBranch(" + branchString.substring(0, branchString.length()  - 1) + ")");
	        }
	        
	        int i;
	        // This for loop allows multiple branches to be defined in a single Branch(x,x,x,x,etc) line in a BO3 file.
	        for (i = 4; i < args.size() - 3; i += 4)
	        {
	            double branchChance = readDouble(args.get(i + 2), 0, Double.MAX_VALUE);
	            branchesOTGPlus.add(new BranchNode(readInt(args.get(i + 3), -32, 32), isRequiredBranch, Rotation.getRotation(args.get(i + 1)), branchChance, null, args.get(i)));
	        }
	        if (i < args.size())
	        {
	        	totalChanceSet = true;
	            totalChance = readDouble(args.get(i), 0, Double.MAX_VALUE);
	        }
    	} else {
    		return super.readArgs(args, accumulateChances);    		
    	}
    	return cumulativeChance;
    }
	
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
		                return new CustomObjectCoordinate(world, branch.getCustomObject(false, world), branch.customObjectName, newRotation, x + rotatedCoords.getX(), y + rotatedCoords.getY(), z + rotatedCoords.getZ(), true, branch.branchDepth, false);
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
