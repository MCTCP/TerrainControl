package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.customobjects.Branch;
import com.khorn.terraincontrol.customobjects.CustomObjectCoordinate;
import com.khorn.terraincontrol.exception.InvalidConfigException;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import java.util.logging.Level;

public class WeightedBranchFunction extends BranchFunction implements Branch
{

    /**
     * At the end of the loading process, this value is equal to the sum of
     * the individual branch chances
     */
    public double cumulativeChance = 0;

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        branches = new TreeSet<BranchNode>();
        cumulativeChance = readArgs(args, true);
    }

    @Override
    public CustomObjectCoordinate toCustomObjectCoordinate(LocalWorld world, Random random, int x, int y, int z)
    {
        double randomChance = random.nextDouble() * (totalChance != -1
                                                     ? totalChance
                                                     : (cumulativeChance >= 100
                                                        ? cumulativeChance
                                                        : 100));
        TerrainControl.log(Level.FINEST, "W-Branch: chance_max - " + randomChance);
        for (Iterator<BranchNode> it = branches.iterator(); it.hasNext();)
        {
            BranchNode branch = it.next();
            TerrainControl.log(Level.FINEST, "  " + branch.getCustomObject().getName() + " trying to spawn! #" + branch.getChance());
            if (branch.getChance() >= randomChance)
            {
                TerrainControl.log(Level.FINEST, "  Successful Spawn");
                return new CustomObjectCoordinate(branch.getCustomObject(), branch.getRotation(), x + this.x, y + this.y, z + this.z);
            }
        }
        TerrainControl.log(Level.FINEST, "  No Spawn");
        return null;
    }

    @Override
    protected String getConfigName()
    {
        return "WeightedBranch";
    }

}
