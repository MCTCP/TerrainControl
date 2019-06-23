package com.pg85.otg.customobjects.bo3.checks;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.customobjects.customstructure.CustomObjectCoordinate;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.MaterialSet;
import com.pg85.otg.util.Rotation;

import java.util.List;

public class BlockCheck extends BO3Check
{
    public MaterialSet toCheck;

    @Override
    public boolean preventsSpawn(LocalWorld world, int x, int y, int z)
    {
        return y > OTG.WORLD_DEPTH && y < OTG.WORLD_HEIGHT && !toCheck.contains(world.getMaterial(x, y, z, false));
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

    @Override
    public BO3Check rotate(Rotation rotation)
    {
    	BlockCheck rotatedBlock = new BlockCheck();

        CustomObjectCoordinate rotatedCoords = CustomObjectCoordinate.getRotatedBO3CoordsJustified(x, y, z, rotation);

        rotatedBlock.x = rotatedCoords.getX();
        rotatedBlock.y = rotatedCoords.getY();
        rotatedBlock.z = rotatedCoords.getZ();

        rotatedBlock.toCheck = this.toCheck;

    	for(int i = 0; i < rotation.getRotationId(); i++)
    	{
            rotatedBlock.toCheck = rotatedBlock.toCheck.rotate();
    	}

        return rotatedBlock;
    }
}
