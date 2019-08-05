package com.pg85.otg.customobjects.bo4.bo4function;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.customobjects.bo4.BO4Config;
import com.pg85.otg.customobjects.structures.CustomStructureCoordinate;
import com.pg85.otg.customobjects.structures.bo4.BO4CustomStructureCoordinate;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.StreamHelper;
import com.pg85.otg.util.helpers.StringHelper;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BO4WeightedBranchFunction extends BO4BranchFunction
{
	private double cumulativeChance = 0;
	
	public BO4WeightedBranchFunction() { }
	
	public BO4WeightedBranchFunction(BO4Config holder)
	{
		super(holder);
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
            branchesOTGPlus.add(new BO4BranchNode(readInt(args.get(i + 3), -32, 32), isRequiredBranch, true, Rotation.getRotation(args.get(i + 1)), branchChance, null, args.get(i), null));
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

        		for(BO4BranchNode branch : branchesOTGPlus)
        		{
        			branch.branchGroup = branchGroup;
        		}
        	}
        }
    	return cumulativeChance;
    }

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        branchesOTGPlus = new ArrayList<BO4BranchNode>();
        cumulativeChance = readArgs(args, true);
    }

    @Override
    public CustomStructureCoordinate toCustomObjectCoordinate(LocalWorld world, Random random, Rotation rotation, int x, int y, int z, String startBO3Name)
    {
    	int cumulativeChance = 0;
    	for (BO4BranchNode branch : branchesOTGPlus)
    	{
    		cumulativeChance += branch.getChance();
    	}
    	if(cumulativeChance > totalChance)
    	{
    		totalChance = cumulativeChance;
    	}

        double randomChance = random.nextDouble() * totalChance;

        for(BO4BranchNode branch : branchesOTGPlus)
        {
        	double branchRarity = branch.getChance();
            if (branchRarity > 0 && branchRarity >= randomChance)
            {
                BO4CustomStructureCoordinate rotatedCoords = BO4CustomStructureCoordinate.getRotatedCoord(this.x, this.y, this.z, rotation);
                Rotation newRotation = Rotation.getRotation((rotation.getRotationId() + branch.getRotation().getRotationId()) % 4);
                return new BO4CustomStructureCoordinate(world, branch.getCustomObject(false, world), branch.customObjectName, newRotation, x + rotatedCoords.getX(), (short)(y + rotatedCoords.getY()), z + rotatedCoords.getZ(), branch.branchDepth, branch.isRequiredBranch, true, branch.branchGroup);
            }
            randomChance -= branch.getChance();
            if(randomChance < 0)
            {
            	randomChance = 0;
            }
        }
        return null;
    }

    public BO4WeightedBranchFunction rotate(Rotation rotation)
    {
    	BO4WeightedBranchFunction rotatedBranch = new BO4WeightedBranchFunction(this.getHolder());

    	rotatedBranch.x = x;
    	rotatedBranch.y = y;
    	rotatedBranch.z = z;

        rotatedBranch.totalChance = totalChance;
        rotatedBranch.totalChanceSet = totalChanceSet;

        rotatedBranch.branchGroup = branchGroup;
        rotatedBranch.isRequiredBranch = isRequiredBranch;
        rotatedBranch.cumulativeChance = cumulativeChance;

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
    protected String getConfigName()
    {
        return "WeightedBranch";
    }
    
    @Override
    public Class<BO4Config> getHolderType()
    {
        return BO4Config.class;
    }
    
    @Override
    public void writeToStream(DataOutput stream) throws IOException
    {    	
        StreamHelper.writeStringToStream(stream, makeString());
    }
    
    public static BO4WeightedBranchFunction fromStream(BO4Config holder, MappedByteBuffer buffer) throws IOException
    {
    	BO4WeightedBranchFunction branchFunction = new BO4WeightedBranchFunction(holder);  	
    	
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
