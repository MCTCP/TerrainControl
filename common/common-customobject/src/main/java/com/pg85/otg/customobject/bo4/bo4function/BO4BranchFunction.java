package com.pg85.otg.customobject.bo4.bo4function;

import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo4.BO4Config;
import com.pg85.otg.customobject.bofunctions.BranchFunction;
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
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.*;

/**
 * Represents the Branch(..) function in the BO3 files.
 *
 */
public class BO4BranchFunction extends BranchFunction<BO4Config>
{
	ArrayList<BO4BranchNode> branchesBO4;
	String branchGroup = "";
	boolean isRequiredBranch = false;
	
	public BO4BranchFunction() { }
	
	BO4BranchFunction(BO4Config holder)
	{
		this.holder = holder;
	}
	
	public BO4BranchFunction rotate(Rotation rotation, String presetFolderName, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		BO4BranchFunction rotatedBranch = new BO4BranchFunction(this.getHolder());

		rotatedBranch.x = x;
		rotatedBranch.y = y;
		rotatedBranch.z = z;

		rotatedBranch.totalChance = totalChance;
		rotatedBranch.totalChanceSet = totalChanceSet;

		rotatedBranch.branchGroup = branchGroup;
		rotatedBranch.isRequiredBranch = isRequiredBranch;

		rotatedBranch.branchesBO4 = branchesBO4; // TODO: Make sure this won't cause problems

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
			for (BO4BranchNode holder : rotatedBranch.branchesBO4)
			{
				rotatedBranchBranches.add(new BO4BranchNode(holder.branchDepth, holder.isRequiredBranch, holder.isWeightedBranch, holder.getRotation().next(), holder.getChance(), holder.getCustomObject(false, presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker), holder.customObjectName, holder.branchGroup));
			}
			rotatedBranch.branchesBO4 = rotatedBranchBranches;
		}

		return rotatedBranch;
	}

	@Override
	public void load(List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		branchesBO4 = new ArrayList<BO4BranchNode>();
		readArgs(args, false, logger);
	}

	@Override
	protected double readArgs(List<String> args, boolean accumulateChances, ILogger logger) throws InvalidConfigException
	{
		double cumulativeChance = 0;
		// assureSize only returns false if size() < size
		assureSize(8, args);

		this.x = readInt(args.get(0), -10000, 10000);
		this.y = readInt(args.get(1), -255, 255);
		this.z = readInt(args.get(2), -10000, 10000);
		this.isRequiredBranch = readBoolean(args.get(3));

		int i;
		// This for loop allows multiple branches to be defined in a single Branch(x,x,x,x,etc) line in a BO3 file.
		for (i = 4; i < args.size() - 3; i += 4)
		{
			double branchChance = readDouble(args.get(i + 2), 0, Double.MAX_VALUE);
			if(this.isRequiredBranch && args.size() > 9)
			{
				if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
				{
					String branchString = "";
					for(String arg : args)
					{
						branchString += ", " + arg;
					}
					logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "isRequired:true branches cannot have multiple BO4's with a rarity, only one BO4 per isRequired:true branch is allowed and the branch automatically has a 100% chance to spawn. Using only the first BO3 for branch: Branch(" + branchString.substring(0, branchString.length()  - 1) + ")");
				}
				this.branchesBO4.add(new BO4BranchNode(readInt(args.get(i + 3), -32, 32), this.isRequiredBranch, false, Rotation.getRotation(args.get(i + 1)), 100.0, null, args.get(i), null));
				break;
			} else {
				this.branchesBO4.add(new BO4BranchNode(readInt(args.get(i + 3), -32, 32), this.isRequiredBranch, false, Rotation.getRotation(args.get(i + 1)), branchChance, null, args.get(i), null));
			}
		}
		if(!this.isRequiredBranch)
		{
			if (i < args.size())
			{
				String totalChanceOrBranchGroup = args.get(i);
				if(totalChanceOrBranchGroup != null && totalChanceOrBranchGroup.length() > 0)
				{
					try
					{
						Double.parseDouble(totalChanceOrBranchGroup);
						this.totalChanceSet = true;
						this.totalChance = readDouble(args.get(i), 0, Double.MAX_VALUE);
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
				this.branchGroup = args.get(i);

				for(BO4BranchNode branch : this.branchesBO4)
				{
					branch.branchGroup = this.branchGroup;
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
			.append(this.x).append(',')
			.append(this.y).append(',')
			.append(this.z).append(',');

		output.append(this.isRequiredBranch);
		for (Iterator<BO4BranchNode> it = this.branchesBO4.iterator(); it.hasNext();)
		{
			output.append(it.next().toBranchString());
		}
		if (this.totalChanceSet)
		{
			output.append(',').append(this.totalChance);
		}
		if(this.branchGroup != null && this.branchGroup.trim().length() > 0)
		{
			output.append(',').append(this.branchGroup);
		}
		return output.append(')').toString();
	}

	/**
	 * This method iterates all the possible branches in this branchFunction object
	 * and uses a random number and the branch's spawn chance to check if the branch
	 * should spawn. Returns null if no branch passes the check.
	 */
	@Override
	public CustomStructureCoordinate toCustomObjectCoordinate(String presetFolderName, Random random, Rotation rotation, int x, int y, int z, String startBO3Name, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		for (Iterator<BO4BranchNode> it = this.branchesBO4.iterator(); it.hasNext();)
		{
			BO4BranchNode branch = it.next();

			double randomChance = random.nextDouble() * this.totalChance;
			if (randomChance <= branch.getChance())
			{
				BO4CustomStructureCoordinate rotatedCoords = BO4CustomStructureCoordinate.getRotatedCoord(this.x, this.y, this.z, rotation);
				Rotation newRotation = Rotation.getRotation((rotation.getRotationId() + branch.getRotation().getRotationId()) % 4);
				return new BO4CustomStructureCoordinate(presetFolderName, branch.getCustomObject(false, presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker), branch.customObjectName, newRotation, x + rotatedCoords.getX(), (short)(y + rotatedCoords.getY()), z + rotatedCoords.getZ(), branch.branchDepth, branch.isRequiredBranch, branch.isWeightedBranch, branch.branchGroup);
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
	
	public static BO4BranchFunction fromStream(BO4Config holder, ByteBuffer buffer, ILogger logger, IMaterialReader materialReader) throws IOException, InvalidConfigException
	{
		BO4BranchFunction branchFunction = new BO4BranchFunction(holder);		
		String configFunctionString = StreamHelper.readStringFromBuffer(buffer);
		int bracketIndex = configFunctionString.indexOf('(');
		String parameters = configFunctionString.substring(bracketIndex + 1, configFunctionString.length() - 1);
		List<String> args = Arrays.asList(StringHelper.readCommaSeperatedString(parameters));		
		branchFunction.load(args, logger, materialReader);
		return branchFunction;
	}
}
