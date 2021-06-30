package com.pg85.otg.customobject.bo4.bo4function;

import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo4.BO4Config;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructureCoordinate;
import com.pg85.otg.customobject.structures.bo4.BO4CustomStructureCoordinate;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.StreamHelper;
import com.pg85.otg.util.helpers.StringHelper;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BO4WeightedBranchFunction extends BO4BranchFunction
{
	private double cumulativeChance = 0;
	
	public BO4WeightedBranchFunction() { }
	
	private BO4WeightedBranchFunction(BO4Config holder)
	{
		super(holder);
	}
	
	@Override
	protected double readArgs(List<String> args, boolean accumulateChances, ILogger logger) throws InvalidConfigException
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
			branchesBO4.add(new BO4BranchNode(readInt(args.get(i + 3), -32, 32), isRequiredBranch, true, Rotation.getRotation(args.get(i + 1)), branchChance, null, args.get(i), null));
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

				for(BO4BranchNode branch : branchesBO4)
				{
					branch.branchGroup = branchGroup;
				}
			}
		}
		return cumulativeChance;
	}

	@Override
	public void load(List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		branchesBO4 = new ArrayList<BO4BranchNode>();
		cumulativeChance = readArgs(args, true, logger);
	}

	@Override
	public CustomStructureCoordinate toCustomObjectCoordinate(String presetFolderName, Random random, Rotation rotation, int x, int y, int z, String startBO3Name, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		int cumulativeChance = 0;
		for (BO4BranchNode branch : branchesBO4)
		{
			cumulativeChance += branch.getChance();
		}
		if(cumulativeChance > totalChance)
		{
			totalChance = cumulativeChance;
		}

		double randomChance = random.nextDouble() * totalChance;

		for(BO4BranchNode branch : branchesBO4)
		{
			double branchRarity = branch.getChance();
			if (branchRarity > 0 && branchRarity >= randomChance)
			{
				BO4CustomStructureCoordinate rotatedCoords = BO4CustomStructureCoordinate.getRotatedCoord(this.x, this.y, this.z, rotation);
				Rotation newRotation = Rotation.getRotation((rotation.getRotationId() + branch.getRotation().getRotationId()) % 4);
				return new BO4CustomStructureCoordinate(presetFolderName, branch.getCustomObject(false, presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker), branch.customObjectName, newRotation, x + rotatedCoords.getX(), (short)(y + rotatedCoords.getY()), z + rotatedCoords.getZ(), branch.branchDepth, branch.isRequiredBranch, true, branch.branchGroup);
			}
			randomChance -= branch.getChance();
			if(randomChance < 0)
			{
				randomChance = 0;
			}
		}
		return null;
	}

	public BO4WeightedBranchFunction rotate(Rotation rotation, String presetFolderName, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
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

		rotatedBranch.branchesBO4 = this.branchesBO4; // TODO: Make sure this won't cause problems

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
			for (BO4BranchNode holder : rotatedBranch.branchesBO4)
			{
				rotatedBranchBranches.add(new BO4BranchNode(holder.branchDepth, holder.isRequiredBranch, holder.isWeightedBranch, holder.getRotation().next(), holder.getChance(), holder.getCustomObject(false, presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker), holder.customObjectName, holder.branchGroup));
			}
			rotatedBranch.branchesBO4 = rotatedBranchBranches;
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

	public static BO4WeightedBranchFunction fromStream(BO4Config holder, ByteBuffer buffer, ILogger logger, IMaterialReader materialReader) throws IOException, InvalidConfigException
	{
		BO4WeightedBranchFunction branchFunction = new BO4WeightedBranchFunction(holder);
		String configFunctionString = StreamHelper.readStringFromBuffer(buffer);
		int bracketIndex = configFunctionString.indexOf('(');
		String parameters = configFunctionString.substring(bracketIndex + 1, configFunctionString.length() - 1);
		List<String> args = Arrays.asList(StringHelper.readCommaSeperatedString(parameters));
		branchFunction.load(args, logger, materialReader);
		return branchFunction;
	}
}
