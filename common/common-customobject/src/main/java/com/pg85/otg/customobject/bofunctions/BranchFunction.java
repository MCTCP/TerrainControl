package com.pg85.otg.customobject.bofunctions;

import com.pg85.otg.customobject.config.CustomObjectConfigFile;
import com.pg85.otg.customobject.config.CustomObjectConfigFunction;
import com.pg85.otg.customobject.structures.Branch;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.interfaces.ILogger;
import com.pg85.otg.util.interfaces.IMaterialReader;

import java.util.*;

/**
 * Represents the Branch(..) function in the BO3 files.
 *
 */
public abstract class BranchFunction<T extends CustomObjectConfigFile> extends CustomObjectConfigFunction<T> implements Branch
{
	/**
	 * The base Y coordinate where this branch is expected to spawn
	 */
	protected int y;
	/**
	 * holds each CustomObject, its spawn chance and its rotation as a node
	 */
	protected SortedSet<BranchNode> branches; // Warning: Using SortedSet + BranchNode's compare method causes a bug where branches with the same rarity are seen as the same branch, this means only the first branch with the same rarity tries to spawn. This is fixed for OTG+.
	/**
	 * This variable was added to allow the following format to be used
	 * Branch(x,y,z,branchName,rotation,chance[,anotherBranchName,rotation,chance[,...]][,maxChanceOutOf])
	 * maxChanceOutOf changes the upper limit of the random number used to
	 * determine if the branch spawns
	 */
	protected double totalChance = 100;
	protected boolean totalChanceSet = false;

	@Override
	public void load(List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		branches = new TreeSet<BranchNode>();
		readArgs(args, false, logger);
	}

	@Override
	public String makeString()
	{
		StringBuilder output = new StringBuilder(getConfigName())
			.append('(')
			.append(x).append(',')
			.append(y).append(',')
			.append(z);

		for (Iterator<BranchNode> it = branches.iterator(); it.hasNext();)
		{
			output.append(it.next().toBranchString());
		}
		if (totalChanceSet)
		{
			output.append(',').append(totalChance);
		}
		return output.append(')').toString();
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

	protected abstract double readArgs(List<String> args, boolean accumulateChances, ILogger logger) throws InvalidConfigException;

	@Override
	public boolean isAnalogousTo(CustomObjectConfigFunction<T> other)
	{
		if(!getClass().equals(other.getClass())) {
			return false;
		}
		BranchFunction<T> branch = (BranchFunction<T>) other;
		return branch.x == x && branch.y == y && branch.z == z;
	}
}
