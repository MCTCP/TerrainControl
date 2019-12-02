package com.pg85.otg.customobjects.bo4.bo4function;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.customobjects.bo4.BO4Config;
import com.pg85.otg.customobjects.bofunctions.BranchFunction;
import com.pg85.otg.customobjects.structures.CustomStructureCoordinate;
import com.pg85.otg.customobjects.structures.bo4.BO4CustomStructureCoordinate;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.StreamHelper;
import com.pg85.otg.util.helpers.StringHelper;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.*;

/**
 * Represents the Branch(..) function in the BO3 files.
 *
 */
public class BO4BranchFunction extends BranchFunction<BO4Config>
{
    ArrayList<BO4BranchNode> branchesOTGPlus;
    String branchGroup = "";
    boolean isRequiredBranch = false;
   
    public BO4BranchFunction() { }
    
    public BO4BranchFunction(BO4Config holder)
    {
    	this.holder = holder;
    }
    
    public BO4BranchFunction rotate(Rotation rotation)
    {
    	BO4BranchFunction rotatedBranch = new BO4BranchFunction(this.getHolder());

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

            ArrayList<BO4BranchNode> rotatedBranchBranches = new ArrayList<BO4BranchNode>();
            for (BO4BranchNode holder : rotatedBranch.branchesOTGPlus)
            {
            	rotatedBranchBranches.add(new BO4BranchNode(holder.branchDepth, holder.isRequiredBranch, holder.isWeightedBranch, holder.getRotation().next(), holder.getChance(), holder.getCustomObject(false, null), holder.customObjectName, holder.branchGroup));
            }
            rotatedBranch.branchesOTGPlus = rotatedBranchBranches;
    	}

        return rotatedBranch;
    }

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        branchesOTGPlus = new ArrayList<BO4BranchNode>();
        readArgs(args, false);
    }

    @Override
    protected double readArgs(List<String> args, boolean accumulateChances) throws InvalidConfigException
    {
        double cumulativeChance = 0;
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
        		branchesOTGPlus.add(new BO4BranchNode(readInt(args.get(i + 3), -32, 32), isRequiredBranch, false, Rotation.getRotation(args.get(i + 1)), 100.0, null, args.get(i), null));
        		break;
        	} else {
	            branchesOTGPlus.add(new BO4BranchNode(readInt(args.get(i + 3), -32, 32), isRequiredBranch, false, Rotation.getRotation(args.get(i + 1)), branchChance, null, args.get(i), null));
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

        		for(BO4BranchNode branch : branchesOTGPlus)
        		{
        			branch.branchGroup = branchGroup;
        		}
        	}
        }
    	return cumulativeChance;
    }
    
    @Override
    public String makeString()
    {
        StringBuilder output = new StringBuilder(getConfigName())
            .append('(')
            .append(x).append(',')
            .append(y).append(',')
            .append(z).append(',');

    	output.append(isRequiredBranch);
        for (Iterator<BO4BranchNode> it = branchesOTGPlus.iterator(); it.hasNext();)
        {
            output.append(it.next().toBranchString());
        }
        if (totalChanceSet)
        {
            output.append(',').append(totalChance);
        }
        if(branchGroup != null)
        {
        	output.append(',').append(branchGroup);
        }
        return output.append(')').toString();
    }

    /**
     * This method iterates all the possible branches in this branchFunction object
     * and uses a random number and the branch's spawn chance to check if the branch
     * should spawn. Returns null if no branch passes the check.
     */
    @Override
    public CustomStructureCoordinate toCustomObjectCoordinate(LocalWorld world, Random random, Rotation rotation, int x, int y, int z, String startBO3Name)
    {
        for (Iterator<BO4BranchNode> it = branchesOTGPlus.iterator(); it.hasNext();)
        {
            BO4BranchNode branch = it.next();

            double randomChance = random.nextDouble() * totalChance;
            if (randomChance <= branch.getChance())
            {
                BO4CustomStructureCoordinate rotatedCoords = BO4CustomStructureCoordinate.getRotatedCoord(this.x, this.y, this.z, rotation);
                Rotation newRotation = Rotation.getRotation((rotation.getRotationId() + branch.getRotation().getRotationId()) % 4);
                return new BO4CustomStructureCoordinate(world, branch.getCustomObject(false, world), branch.customObjectName, newRotation, x + rotatedCoords.getX(), (short)(y + rotatedCoords.getY()), z + rotatedCoords.getZ(), branch.branchDepth, branch.isRequiredBranch, branch.isWeightedBranch, branch.branchGroup);
            }
        }
        return null;
    }    
    
    @Override
    public Class<BO4Config> getHolderType()
    {
        return BO4Config.class;
    }
    
    public void writeToStream(DataOutput stream) throws IOException
    {
        StreamHelper.writeStringToStream(stream, makeString());
    }
    
    public static BO4BranchFunction fromStream(BO4Config holder, ByteBuffer buffer) throws IOException
    {
    	BO4BranchFunction branchFunction = new BO4BranchFunction(holder);  	
    	
        String configFunctionString = StreamHelper.readStringFromBuffer(buffer);
        int bracketIndex = configFunctionString.indexOf('(');
        String parameters = configFunctionString.substring(bracketIndex + 1, configFunctionString.length() - 1);
        List<String> args = Arrays.asList(StringHelper.readCommaSeperatedString(parameters));
        
        try {
			branchFunction.load(args);
		} catch (InvalidConfigException e) {
			e.printStackTrace();
		}            	           
    	return branchFunction;
    }
}
