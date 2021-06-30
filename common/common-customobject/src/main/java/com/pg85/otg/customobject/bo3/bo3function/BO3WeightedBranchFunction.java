package com.pg85.otg.customobject.bo3.bo3function;

import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bofunctions.BranchNode;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructureCoordinate;
import com.pg85.otg.customobject.structures.bo3.BO3CustomStructureCoordinate;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.interfaces.ILogger;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;

import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

public class BO3WeightedBranchFunction extends BO3BranchFunction
{	
	private double cumulativeChance = 0;
		
	@Override
	public void load(List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		branches = new TreeSet<BranchNode>();
		cumulativeChance = readArgs(args, true, logger);
	}

	@Override
	public CustomStructureCoordinate toCustomObjectCoordinate(String presetFolderName, Random random, Rotation rotation, int x, int y, int z, String startBO3Name, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		double randomChance = random.nextDouble() * (totalChanceSet
				? totalChance
				: (cumulativeChance >= 100
					? cumulativeChance
					: 100));
		
		for (BranchNode branch : branches)
		{
			if (branch.getChance() >= randomChance)
			{
				return new BO3CustomStructureCoordinate(presetFolderName, branch.getCustomObject(false, presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker), branch.customObjectName, branch.getRotation(), x + this.x, (short)(y + this.y), z + this.z);
			}
		}
		return null;
	}

	@Override
	protected String getConfigName()
	{
		return "WeightedBranch";
	}
}
