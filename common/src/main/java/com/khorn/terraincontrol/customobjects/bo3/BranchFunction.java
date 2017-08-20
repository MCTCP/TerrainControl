package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.CustomObjectConfigFunction;
import com.khorn.terraincontrol.customobjects.Branch;
import com.khorn.terraincontrol.customobjects.CustomObjectCoordinate;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.Rotation;

import java.util.*;

/**
 * Represents the Branch(..) function in the BO3 files.
 *
 */
public class BranchFunction extends BO3Function implements Branch
{
    /**
     * The base X coordinate where this branch is expected to spawn
     */
    public int x;
    /**
     * The base Y coordinate where this branch is expected to spawn
     */
    public int y;
    /**
     * The base Z coordinate where this branch is expected to spawn
     */
    public int z;
    /**
     * holds each CustomObject, its spawn chance and its rotation as a node
     */
    public SortedSet<BranchNode> branches; // Warning: Using SortedSet + BranchNode's compare method causes a bug where branches with the same rarity are seen as the same branch, this means only the first branch with the same rarity tries to spawn. This is fixed for OTG+.
    public ArrayList<BranchNode> branchesOTGPlus;
    /**
     * This variable was added to allow the following format to be used
     * Branch(x,y,z,branchName,rotation,chance[,anotherBranchName,rotation,chance[,...]][,maxChanceOutOf])
     * maxChanceOutOf changes the upper limit of the random number used to
     * determine if the branch spawns
     */
    public double totalChance = 100;
    public boolean totalChanceSet = false;
    
    public boolean isRequiredBranch = false;
            
    @Override
    public BranchFunction rotate()
    {
        BranchFunction rotatedBranch = new BranchFunction();
        rotatedBranch.x = z;
        rotatedBranch.y = y;
        rotatedBranch.z = -x;
        rotatedBranch.branches = new TreeSet<BranchNode>();
        for (BranchNode holder : this.branches)
        {
            rotatedBranch.branches.add(new BranchNode(holder.getRotation().next(), holder.getChance(), holder.getCustomObject(false, null), holder.customObjectName));
        }
        return rotatedBranch;
    }
    
    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        branches = new TreeSet<BranchNode>();
        branchesOTGPlus = new ArrayList<BranchNode>();
        readArgs(args, false);
    }

    @Override
    public String makeString()
    {
        StringBuilder output = new StringBuilder(getConfigName())
            .append('(')
            .append(x).append(',')
            .append(y).append(',')
            .append(z).append(',');
        
        if(isOTGPlus)
        {
        	output.append(isRequiredBranch);
            for (Iterator<BranchNode> it = branchesOTGPlus.iterator(); it.hasNext();)
            {
                output.append(it.next().toBranchString());
            } 
        } else {        
	        for (Iterator<BranchNode> it = branches.iterator(); it.hasNext();)
	        {
	            output.append(it.next().toBranchString());
	        }
        }
        if (totalChanceSet)
        {
            output.append(',').append(totalChance);
        }
        return output.append(')').toString();
    }
   
    /**
     * This method iterates all the possible branches in this branchFunction object
     * and uses a random number and the branch's spawn chance to check if the branch
     * should spawn. Returns null if no branch passes the check.
     */
    @Override
    public CustomObjectCoordinate toCustomObjectCoordinate(LocalWorld world, Random random, Rotation rotation, int x, int y, int z, String startBO3Name)
    {   	
    	if(world.getConfigs().getWorldConfig().IsOTGPlus)
    	{
	        for (Iterator<BranchNode> it = branchesOTGPlus.iterator(); it.hasNext();)
	        {
	            BranchNode branch = it.next();
	            double randomChance = random.nextDouble() * totalChance;
	            if (randomChance <= branch.getChance())
	            {               
	                CustomObjectCoordinate rotatedCoords = RotateCoords(this.x, this.y, this.z, rotation);
	                Rotation newRotation = Rotation.getRotation((rotation.getRotationId() + branch.getRotation().getRotationId()) % 4);
	                return new CustomObjectCoordinate(world, branch.getCustomObject(false, world), branch.customObjectName, newRotation, x + rotatedCoords.getX(), y + rotatedCoords.getY(), z + rotatedCoords.getZ(), true, branch.branchDepth, branch.isRequiredBranch);
	            }
	        }
    	} else {
            for (Iterator<BranchNode> it = branches.iterator(); it.hasNext();)
            {            	
                BranchNode branch = it.next();
                                
                double randomChance = random.nextDouble() * totalChance;                
                if (randomChance < branch.getChance())
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

    /**
     * Returns the name of the function used in the config file;
     * <p/>
     * @return The name of the function used in the config file;
     */
    protected String getConfigName()
    {
        return "Branch";
    }    
    
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
	        assureSize(6, args);
	        x = readInt(args.get(0), -32, 32);
	        y = readInt(args.get(1), -64, 64);
	        z = readInt(args.get(2), -32, 32);
	        int i;
	        for (i = 3; i < args.size() - 2; i += 3)
	        {
	            double branchChance = readDouble(args.get(i + 2), 0, Double.MAX_VALUE);
	            if (accumulateChances)
	            {
	                cumulativeChance += branchChance;
	                // CustomObjects are inserted into the Set in ascending chance order with Chance being cumulative.
	                branches.add(new BranchNode(0, isRequiredBranch, Rotation.getRotation(args.get(i + 1)), cumulativeChance, null, args.get(i)));
	            } else {
	                branches.add(new BranchNode(0, isRequiredBranch, Rotation.getRotation(args.get(i + 1)), branchChance, null, args.get(i)));
	            }
	        }
	        if (i < args.size())
	        {
	        	totalChanceSet = true;
	            totalChance = readDouble(args.get(i), 0, Double.MAX_VALUE);
	        }
    	}
    	return cumulativeChance;
    }

    @Override
    public boolean isAnalogousTo(CustomObjectConfigFunction<BO3Config> other)
    {
        if(!getClass().equals(other.getClass())) {
            return false;
        }
        BranchFunction branch = (BranchFunction) other;
        return branch.x == x && branch.y == y && branch.z == z;
    }

}
