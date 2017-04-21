package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.MaterialSet;

import java.util.List;

public final class BlockCheckNot extends BO3Check
{
    public MaterialSet toCheck;

    public BlockCheckNot(BO3Config config, List<String> args) throws InvalidConfigException
    {
        super(config);
        assureSize(4, args);
        this.x = readInt(args.get(0), -100, 100);
        this.y = readInt(args.get(1), -100, 100);
        this.z = readInt(args.get(2), -100, 100);
        this.toCheck = readMaterials(args, 3);
    }

    public BlockCheckNot(BO3Config config, MaterialSet toCheck, int x, int y, int z)
    {
        super(config);
        this.toCheck = toCheck;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean preventsSpawn(LocalWorld world, int x, int y, int z)
    {
        return this.toCheck.contains(world.getMaterial(x, y, z));
    }

    @Override
    public String toString()
    {
        return "BlockCheckNot(" + x + ',' + y + ',' + z + makeMaterials(this.toCheck) + ')';
    }

    @Override
    public BO3Check rotate()
    {
        return new BlockCheckNot(getHolder(), this.toCheck.rotate(), z, y, -x);
    }

}
