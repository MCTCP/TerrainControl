package com.pg85.otg.customobjects.bo3.bo3function;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.customobjects.CustomObjectConfigFunction;
import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.customobjects.customstructure.Branch;
import com.pg85.otg.customobjects.customstructure.CustomObjectCoordinate;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.bo3.Rotation;

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
    int x;
    /**
     * The base Y coordinate where this branch is expected to spawn
     */
    int y;
    /**
     * The base Z coordinate where this branch is expected to spawn
     */
    int z;
    /**
     * holds each CustomObject, its spawn chance and its rotation as a node
     */
    SortedSet<BranchNode> branches; // Warning: Using SortedSet + BranchNode's compare method causes a bug where branches with the same rarity are seen as the same branch, this means only the first branch with the same rarity tries to spawn. This is fixed for OTG+.
    ArrayList<BranchNode> branchesOTGPlus;
    /**
     * This variable was added to allow the following format to be used
     * Branch(x,y,z,branchName,rotation,chance[,anotherBranchName,rotation,chance[,...]][,maxChanceOutOf])
     * maxChanceOutOf changes the upper limit of the random number used to
     * determine if the branch spawns
     */
    double totalChance = 100;
    boolean totalChanceSet = false;

    String branchGroup = "";

    boolean isRequiredBranch = false;

    @Override
    public BranchFunction rotate()
    {
    	if(isOTGPlus)
    	{
    		throw new RuntimeException();
    	}

        BranchFunction rotatedBranch = new BranchFunction();
        rotatedBranch.x = z;
        rotatedBranch.y = y;
        rotatedBranch.z = -x;
        rotatedBranch.branches = new TreeSet<BranchNode>();
        rotatedBranch.totalChance = totalChance;
        rotatedBranch.totalChanceSet = totalChanceSet;
        for (BranchNode holder : this.branches)
        {
            rotatedBranch.branches.add(new BranchNode(holder.getRotation().next(), holder.getChance(), holder.getCustomObject(false, null), holder.customObjectName));
        }
        return rotatedBranch;
    }

    public BranchFunction rotate(Rotation rotation)
    {
    	if(!isOTGPlus)
    	{
    		throw new RuntimeException();
    	}

    	BranchFunction rotatedBranch = new BranchFunction();

    	rotatedBranch.isOTGPlus = isOTGPlus;

    	rotatedBranch.x = x;
    	rotatedBranch.y = y;
    	rotatedBranch.z = z;

        rotatedBranch.totalChance = totalChance;
        rotatedBranch.totalChanceSet = totalChanceSet;

        rotatedBranch.branchGroup = branchGroup;
        rotatedBranch.isRequiredBranch = isRequiredBranch;

        rotatedBranch.branchesOTGPlus = branchesOTGPlus; // TODO: Make sure this won't cause problems

        rotatedBranch.holder = holder;
        rotatedBranch.valid = valid;
        rotatedBranch.inputName = inputName;
        rotatedBranch.inputArgs = inputArgs;
        rotatedBranch.error = error;

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
    	if(world.getConfigs().getWorldConfig().isOTGPlus)
    	{
	        for (Iterator<BranchNode> it = branchesOTGPlus.iterator(); it.hasNext();)
	        {
	            BranchNode branch = it.next();

	            double randomChance = random.nextDouble() * totalChance;
	            if (randomChance <= branch.getChance())
	            {
	                CustomObjectCoordinate rotatedCoords = CustomObjectCoordinate.getRotatedCoord(this.x, this.y, this.z, rotation);
	                Rotation newRotation = Rotation.getRotation((rotation.getRotationId() + branch.getRotation().getRotationId()) % 4);
	                return new CustomObjectCoordinate(world, branch.getCustomObject(false, world), branch.customObjectName, newRotation, x + rotatedCoords.getX(), y + rotatedCoords.getY(), z + rotatedCoords.getZ(), branch.branchDepth, branch.isRequiredBranch, branch.isWeightedBranch, branch.branchGroup);
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
	        	if(isRequiredBranch && args.size() > 9)
	        	{
	        		if(OTG.getPluginConfig().spawnLog)
	        		{
	        			String branchString = "";
	        			for(String arg : args)
	        			{
	        				branchString += ", " + arg;
	        			}
	        			OTG.log(LogMarker.WARN, "isRequired:true branches cannot have multiple BO3's with a rarity, only one BO3 per isRequired:true branch is allowed and the branch automatically has a 100% chance to spawn. Using only the first BO3 for branch: Branch(" + branchString.substring(0, branchString.length()  - 1) + ")");
	        		}
	        		branchesOTGPlus.add(new BranchNode(readInt(args.get(i + 3), -32, 32), isRequiredBranch, false, Rotation.getRotation(args.get(i + 1)), 100.0, null, args.get(i), null));
	        		break;
	        	} else {
		            branchesOTGPlus.add(new BranchNode(readInt(args.get(i + 3), -32, 32), isRequiredBranch, false, Rotation.getRotation(args.get(i + 1)), branchChance, null, args.get(i), null));
	        	}
	        }
	        if(!isRequiredBranch)
	        {
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
	                branches.add(new BranchNode(0, isRequiredBranch, false, Rotation.getRotation(args.get(i + 1)), cumulativeChance, null, args.get(i), null));
	            } else {
	                branches.add(new BranchNode(0, isRequiredBranch, false, Rotation.getRotation(args.get(i + 1)), branchChance, null, args.get(i), null));
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
