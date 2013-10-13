package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.customobjects.*;
import com.khorn.terraincontrol.exception.InvalidConfigException;

import java.util.*;
import java.util.logging.Level;

/**
 *
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
        rotatedBranch.branches = branches;
        for (Iterator<BranchNode> it = branches.iterator(); it.hasNext();)
        {
            BranchNode holder = it.next();
            rotatedBranch.branches.add(new BranchNode(Rotation.next(holder.getRotation()), holder.getChance(), holder.getCustomObject()));
        }
        return rotatedBranch;
    }

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        branches = new TreeSet<BranchNode>(BranchNode.getComparator());
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
        TerrainControl.log(Level.FINEST, "Branch:");
        for (Iterator<BranchNode> it = branches.iterator(); it.hasNext();)
        {
            BranchNode branch = it.next();
            double randomChance = random.nextDouble() * (totalChance != -1 ? totalChance : 100);
            TerrainControl.log(Level.FINEST, "  Needed: " + branch.getChance() + " Obtained: " + randomChance);
            if (randomChance < branch.getChance())
            {
                TerrainControl.log(Level.FINEST, "  Successful Spawn");
                return new CustomObjectCoordinate(branch.getCustomObject(), branch.getRotation(), x + this.x, y + this.y, z + this.z);
            }
        }
        TerrainControl.log(Level.FINEST, "  No Spawn");
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
                TerrainControl.log(Level.FINER, object.getName() + " Initialized");
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
        TerrainControl.log(Level.FINEST, args.size() + ":" + i);
        if (i < args.size())
        {
            TerrainControl.log(Level.FINEST, this.getConfigName() + " TotalChance set.");
            totalChance = readDouble(args.get(i), 0, Double.MAX_VALUE);
        }
        return cumulativeChance;

    }

}
