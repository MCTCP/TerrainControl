package com.pg85.otg.customobject.bo3.bo3function;

import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo3.BO3Config;
import com.pg85.otg.customobject.bofunctions.BranchFunction;
import com.pg85.otg.customobject.bofunctions.BranchNode;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructureCoordinate;
import com.pg85.otg.customobject.structures.bo3.BO3CustomStructureCoordinate;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.util.bo3.Rotation;

import java.nio.file.Path;
import java.util.*;

/**
 * Represents the Branch(..) function in the BO3 files.
 *
 */
public class BO3BranchFunction extends BranchFunction<BO3Config>
{	
	public BO3BranchFunction rotate(String presetFolderName, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		BO3BranchFunction rotatedBranch = new BO3BranchFunction();
		rotatedBranch.x = z;
		rotatedBranch.y = y;
		rotatedBranch.z = -x;
		rotatedBranch.branches = new TreeSet<BranchNode>();
		rotatedBranch.totalChance = totalChance;
		rotatedBranch.totalChanceSet = totalChanceSet;
		for (BranchNode holder : this.branches)
		{
			rotatedBranch.branches.add(new BranchNode(holder.getRotation().next(), holder.getChance(), holder.getCustomObject(false, presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker), holder.customObjectName));
		}
		return rotatedBranch;
	}
	
	// TODO: accumulateChances is only used for weightedbranches, remove from this class (will affect loading..).
	@Override
	protected double readArgs(List<String> args, boolean accumulateChances, ILogger logger) throws InvalidConfigException
	{
		double cumulativeChance = 0;
		assureSize(6, args);
		x = readInt(args.get(0), -10000, 10000);
		y = readInt(args.get(1), -255, 255);
		z = readInt(args.get(2), -10000, 10000);
		int i;
		for (i = 3; i < args.size() - 2; i += 3)
		{
			double branchChance = readDouble(args.get(i + 2), 0, Double.MAX_VALUE);
			if (accumulateChances)
			{
				cumulativeChance += branchChance;
				// CustomObjects are inserted into the Set in ascending chance order with Chance being cumulative.
				branches.add(new BranchNode(Rotation.getRotation(args.get(i + 1)), cumulativeChance, null, args.get(i)));
			} else {
				branches.add(new BranchNode(Rotation.getRotation(args.get(i + 1)), branchChance, null, args.get(i)));
			}
		}
		if (i < args.size())
		{
			totalChanceSet = true;
			totalChance = readDouble(args.get(i), 0, Double.MAX_VALUE);
		}
		return cumulativeChance;
	}
	
	/**
	 * This method iterates all the possible branches in this branchFunction object
	 * and uses a random number and the branch's spawn chance to check if the branch
	 * should spawn. Returns null if no branch passes the check.
	 */
	@Override
	public CustomStructureCoordinate toCustomObjectCoordinate(String presetFolderName, Random random, Rotation rotation, int x, int y, int z, String startBO3Name, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		for (Iterator<BranchNode> it = branches.iterator(); it.hasNext();)
		{
			BranchNode branch = it.next();

			double randomChance = random.nextDouble() * totalChance;
			if (randomChance < branch.getChance())
			{
				return new BO3CustomStructureCoordinate(presetFolderName, branch.getCustomObject(false, presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker), branch.customObjectName, branch.getRotation(), x + this.x, (short)(y + this.y), z + this.z);
			}
		}
		return null;
	}

	@Override
	public Class<BO3Config> getHolderType()
	{
		return BO3Config.class;
	}
}
