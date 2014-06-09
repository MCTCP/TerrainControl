package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.customobjects.Branch;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.CustomObjectCoordinate;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.Rotation;

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
    public int x;
    /**
     * The base Y coordinate where this branch is expected to spawn
     */
    public int y;
    /**
     * The base Z coordinate where this branch is expected to spawn
     */
    public int z;
    /**
     * holds each CustomObject, its spawn chance and its rotation as a node
     */
    public SortedSet<BranchNode> branches;
    /**
     * This variable was added to allow the following format to be used
     * Branch(x,y,z,branchName,rotation,chance[,anotherBranchName,rotation,chance[,...]][,maxChanceOutOf])
     * maxChanceOutOf changes the upper limit of the random number used to
     * determine if the branch spawns
     */
    public double totalChance = -1;

    @Override
    public BranchFunction rotate()
    {
        BranchFunction rotatedBranch = new BranchFunction();
        rotatedBranch.x = z;
        rotatedBranch.y = y;
        rotatedBranch.z = -x;
        rotatedBranch.branches = new TreeSet<BranchNode>();
        for (BranchNode holder : this.branches)
        {
            rotatedBranch.branches.add(new BranchNode(Rotation.next(holder.getRotation()), holder.getChance(), holder.getCustomObject()));
        }
        return rotatedBranch;
    }

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        branches = new TreeSet<BranchNode>();
        readArgs(args, false);
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
        if (totalChance != -1)
        {
            output.append(',').append(totalChance);
        }
        return output.append(')').toString();
    }

    @Override
    public CustomObjectCoordinate toCustomObjectCoordinate(LocalWorld world, Random random, int x, int y, int z)
    {
        TerrainControl.log(LogMarker.TRACE, "Branch:");
        for (Iterator<BranchNode> it = branches.iterator(); it.hasNext();)
        {
            BranchNode branch = it.next();
            double randomChance = random.nextDouble() * (totalChance != -1 ? totalChance : 100);
            TerrainControl.log(LogMarker.TRACE, "  Needed: {} Obtained: {}", branch.getChance(), randomChance);
            if (randomChance < branch.getChance())
            {
                TerrainControl.log(LogMarker.TRACE, "  Successful Spawn");
                return new CustomObjectCoordinate(branch.getCustomObject(), branch.getRotation(), x + this.x, y + this.y, z + this.z);
            }
        }
        TerrainControl.log(LogMarker.TRACE, "  No Spawn");
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

    protected double readArgs(List<String> args, boolean accumulateChances) throws InvalidConfigException
    {
        assureSize(6, args);
        x = readInt(args.get(0), -32, 32);
        y = readInt(args.get(1), -64, 64);
        z = readInt(args.get(2), -32, 32);
        int i;
        double cumulativeChance = 0;
        for (i = 3; i < args.size() - 2; i += 3)
        {
            CustomObject object = getHolder().otherObjectsInDirectory.get(args.get(i).toLowerCase());
            if (object == null)
            {
                throw new InvalidConfigException("The " + this.getConfigName() + " `" + args.get(i) + "` was not found. Make sure to place it in the same directory.");
            } else
            {
                TerrainControl.log(LogMarker.TRACE, "{} Initialized", (Object) object.getName());
            }
            double branchChance = readDouble(args.get(i + 2), 0, Double.MAX_VALUE);
            if (accumulateChances)
            {
                cumulativeChance += branchChance;
                // CustomObjects are inserted into the Set in ascending chance order with Chance being cumulative.
                branches.add(new BranchNode(Rotation.getRotation(args.get(i + 1)), cumulativeChance, object));
            } else
            {
                branches.add(new BranchNode(Rotation.getRotation(args.get(i + 1)), branchChance, object));
            }
        }
        TerrainControl.log(LogMarker.TRACE, "{}:{}", args.size(), i);
        if (i < args.size())
        {
            TerrainControl.log(LogMarker.TRACE, "{} TotalChance set.", (Object) this.getConfigName());
            totalChance = readDouble(args.get(i), 0, Double.MAX_VALUE);
        }
        return cumulativeChance;

    }

    @Override
    public boolean isAnalogousTo(ConfigFunction<BO3Config> other)
    {
        if(!getClass().equals(other.getClass())) {
            return false;
        }
        BranchFunction branch = (BranchFunction) other;
        return branch.x == x && branch.y == y && branch.z == z;
    }

}
