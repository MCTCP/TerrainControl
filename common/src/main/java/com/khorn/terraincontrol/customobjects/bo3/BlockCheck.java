package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.MaterialSet;

import java.util.List;

public class BlockCheck extends BO3Check
{
    public MaterialSet toCheck;

    @Override
    public boolean preventsSpawn(LocalWorld world, int x, int y, int z)
    {
        return !toCheck.contains(world.getMaterial(x, y, z));
    }

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(4, args);
        x = readInt(args.get(0), -100, 100);
        y = readInt(args.get(1), -100, 100);
        z = readInt(args.get(2), -100, 100);
        toCheck = readMaterials(args, 3);
    }

    @Override
    public String makeString()
    {
        return makeString("BlockCheck");
    }

    /**
     * Gets the string representation with the given check name.
     * 
     * @param name Name of the check, like BlockCheck.
     * @return The string representation.
     */
    protected String makeString(String name)
    {
        return name + '(' + x + ',' + y + ',' + z + makeMaterials(toCheck) + ')';
    }

    @Override
    public BO3Check rotate()
    {
        BlockCheck rotatedCheck = new BlockCheck();
        rotatedCheck.x = z;
        rotatedCheck.y = y;
        rotatedCheck.z = -x;
        rotatedCheck.toCheck = this.toCheck.rotate();
        return rotatedCheck;
    }

}
