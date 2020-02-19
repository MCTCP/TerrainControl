package com.pg85.otg.customobjects.bo3.checks;

import java.util.List;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.ChunkCoordinate;

public class ModCheck extends BO3Check
{
    private String[] mods;

    @Override
    public boolean preventsSpawn(LocalWorld world, int x, int y, int z, ChunkCoordinate chunkBeingPopulated)
    {
        return false;
    }

    @Override
    public BO3Check rotate()
    {
        return this;
    }

    @Override
    protected void load(List<String> args) throws InvalidConfigException
    {
        assureSize(1, args);
        mods = new String[args.size()];
        for (int i = 0; i < args.size(); i++)
            mods[i] = args.get(i);
    }

    @Override
    public String makeString()
    {
        return makeString("ModCheck");
    }

    /**
     * Gets the string representation with the given check name.
     *
     * @param name Name of the check, like BlockCheck.
     * @return The string representation.
     */
    protected String makeString(String name)
    {
        return name + '(' + String.join(",", mods) + ')';
    }

    @Override
    public Class<BO3Config> getHolderType()
    {
        return BO3Config.class;
    }

    public boolean evaluate()
    {
        for (String mod : mods)
        {
            if (!OTG.getEngine().isModLoaded(mod))
                return false;
        }
        return true;
    }
}
