package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.customobjects.Branch;
import com.khorn.terraincontrol.customobjects.CustomObjectCoordinate;
import com.khorn.terraincontrol.exception.InvalidConfigException;

import java.util.List;
import java.util.Random;
import java.util.TreeSet;

public class WeightedBranchFunction extends BranchFunction implements Branch
{

    /**
     * At the end of the loading process, this value is equal to the sum of
     * the individual branch chances
     */
    public double cumulativeChance = 0;

    public WeightedBranchFunction(BO3Config config, List<String> args) throws InvalidConfigException
    {
        super(config);
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
        for (BranchNode branch : branches)
        {
            if (branch.getChance() >= randomChance)
            {
                return new CustomObjectCoordinate(branch.getCustomObject(), branch.getRotation(), x + this.x, y + this.y, z + this.z);
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
