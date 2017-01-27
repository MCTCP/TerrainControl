package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.customobjects.Branch;
import com.khorn.terraincontrol.customobjects.CustomObjectCoordinate;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.logging.LogMarker;

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
        this.branches = new TreeSet<BranchNode>();
        this.cumulativeChance = readArgs(args, true);
    }

    @Override
    public CustomObjectCoordinate toCustomObjectCoordinate(LocalWorld world, Random random, int x, int y, int z)
    {
        double randomChance = random.nextDouble() * (this.totalChance != -1
                                                     ? this.totalChance
                                                     : (this.cumulativeChance >= 100
                                                        ? this.cumulativeChance
                                                        : 100));
        TerrainControl.log(LogMarker.TRACE, "W-Branch: chance_max - {}", randomChance);
        for (BranchNode branch : this.branches)
        {
            TerrainControl.log(LogMarker.TRACE, "  {} trying to spawn! #{}", branch.getCustomObject().getName(), branch.getChance());
            if (branch.getChance() >= randomChance)
            {
                TerrainControl.log(LogMarker.TRACE, "  Successful Spawn");
                return new CustomObjectCoordinate(branch.getCustomObject(), branch.getRotation(), x + this.x, y + this.y, z + this.z);
            }
        }
        TerrainControl.log(LogMarker.TRACE, "  No Spawn");
        return null;
    }

    @Override
    protected String getConfigName()
    {
        return "WeightedBranch";
    }

}
