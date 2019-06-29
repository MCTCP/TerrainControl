package com.pg85.otg.customobjects.bo3.bo3function;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.customobjects.customstructure.Branch;
import com.pg85.otg.customobjects.customstructure.CustomObjectCoordinate;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.bo3.Rotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

public class WeightedBranchFunction extends BranchFunction implements Branch
{
	public double cumulativeChance = 0;

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
	            branchesOTGPlus.add(new BranchNode(readInt(args.get(i + 3), -32, 32), isRequiredBranch, true, Rotation.getRotation(args.get(i + 1)), branchChance, null, args.get(i), null));
	        }

	        if (i < args.size())
	        {
	        	String totalChanceOrBranchGroup = args.get(i);
	        	if(totalChanceOrBranchGroup != null && totalChanceOrBranchGroup.length() > 0)
	        	{
	        		try
	        		{
	        			Double.parseDouble(totalChanceOrBranchGroup);
			        	totalChanceSet = true;
			            totalChance = readDouble(args.get(i), 0, Double.MAX_VALUE);
			            i++;
	        		}
	        		catch(NumberFormatException ex) { }
	        	}
	        }

	        if (i < args.size())
	        {
	        	String totalChanceOrBranchGroup = args.get(i);
	        	if(totalChanceOrBranchGroup != null && totalChanceOrBranchGroup.length() > 0)
	        	{
	        		branchGroup = args.get(i);

	        		for(BranchNode branch : branchesOTGPlus)
	        		{
	        			branch.branchGroup = branchGroup;
	        		}
	        	}
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
    	if(world.getConfigs().getWorldConfig().isOTGPlus)
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
		                CustomObjectCoordinate rotatedCoords = CustomObjectCoordinate.getRotatedCoord(this.x, this.y, this.z, rotation);
		                Rotation newRotation = Rotation.getRotation((rotation.getRotationId() + branch.getRotation().getRotationId()) % 4);
		                return new CustomObjectCoordinate(world, branch.getCustomObject(false, world), branch.customObjectName, newRotation, x + rotatedCoords.getX(), y + rotatedCoords.getY(), z + rotatedCoords.getZ(), true, branch.branchDepth, branch.isRequiredBranch, true, branch.branchGroup);
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

    @Override
    protected String getConfigName()
    {
        return "WeightedBranch";
    }

    @Override
    public WeightedBranchFunction rotate(Rotation rotation)
    {
    	if(!isOTGPlus)
    	{
    		throw new RuntimeException();
    	}

    	WeightedBranchFunction rotatedBranch = new WeightedBranchFunction();

    	rotatedBranch.isOTGPlus = isOTGPlus;

    	rotatedBranch.x = x;
    	rotatedBranch.y = y;
    	rotatedBranch.z = z;

        rotatedBranch.totalChance = totalChance;
        rotatedBranch.totalChanceSet = totalChanceSet;

        rotatedBranch.branchGroup = branchGroup;
        rotatedBranch.isRequiredBranch = isRequiredBranch;
        rotatedBranch.cumulativeChance = cumulativeChance;
        rotatedBranch.isOTGPlus = isOTGPlus;

        rotatedBranch.holder = holder;
        rotatedBranch.valid = valid;
        rotatedBranch.inputName = inputName;
        rotatedBranch.inputArgs = inputArgs;
        rotatedBranch.error = error;

        rotatedBranch.branchesOTGPlus = this.branchesOTGPlus; // TODO: Make sure this won't cause problems

        int newX = rotatedBranch.x;
        int newZ = rotatedBranch.z;

    	for(int i = 0; i < rotation.getRotationId(); i++)
    	{
            newX = rotatedBranch.z;
            newZ = -rotatedBranch.x;

    		rotatedBranch.x = newX;
            rotatedBranch.y = rotatedBranch.y;
            rotatedBranch.z = newZ;

            ArrayList<BranchNode> rotatedBranchBranches = new ArrayList<BranchNode>();
            for (BranchNode holder : rotatedBranch.branchesOTGPlus)
            {
            	rotatedBranchBranches.add(new BranchNode(holder.branchDepth, holder.isRequiredBranch, holder.isWeightedBranch, holder.getRotation().next(), holder.getChance(), holder.getCustomObject(false, null), holder.customObjectName, holder.branchGroup));
            }
            rotatedBranch.branchesOTGPlus = rotatedBranchBranches;
    	}

        return rotatedBranch;
    }
}
