package com.pg85.otg.customobject.bo3.bo3function;

import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo3.BO3Config;
import com.pg85.otg.customobject.bofunctions.BranchFunction;
import com.pg85.otg.customobject.bofunctions.BranchNode;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructureCoordinate;
import com.pg85.otg.customobject.structures.bo3.BO3CustomStructureCoordinate;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IPresetNameProvider;

import java.nio.file.Path;
import java.util.*;

/**
 * Represents the Branch(..) function in the BO3 files.
 *
 */
public class BO3BranchFunction extends BranchFunction<BO3Config>
{	
    public BO3BranchFunction rotate(Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IPresetNameProvider presetNameProvider, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
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
            rotatedBranch.branches.add(new BranchNode(holder.getRotation().next(), holder.getChance(), holder.getCustomObject(false, null, otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker), holder.customObjectName));
        }
        return rotatedBranch;
    }
	
    // TODO: accumulateChances is only used for weightedbranches, remove from this class (will affect loading..).
	@Override
    protected double readArgs(List<String> args, boolean accumulateChances, boolean spawnLog, ILogger logger) throws InvalidConfigException
    {
        double cumulativeChance = 0;
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
    public CustomStructureCoordinate toCustomObjectCoordinate(String worldName, Random random, Rotation rotation, int x, int y, int z, String startBO3Name, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IPresetNameProvider presetNameProvider, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
        for (Iterator<BranchNode> it = branches.iterator(); it.hasNext();)
        {
            BranchNode branch = it.next();

            double randomChance = random.nextDouble() * totalChance;
            if (randomChance < branch.getChance())
            {
                return new BO3CustomStructureCoordinate(worldName, branch.getCustomObject(false, worldName, otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker), branch.customObjectName, branch.getRotation(), x + this.x, (short)(y + this.y), z + this.z);
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
