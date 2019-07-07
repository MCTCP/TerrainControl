package com.pg85.otg.customobjects.bo4.bo4function;

import com.pg85.otg.customobjects.bo4.BO4Config;
import com.pg85.otg.customobjects.bofunctions.ModDataFunction;
import com.pg85.otg.customobjects.structures.bo4.BO4CustomStructureCoordinate;
import com.pg85.otg.util.bo3.Rotation;

/**
 * Represents a block in a BO3.
 */
public class BO4ModDataFunction extends ModDataFunction<BO4Config>
{
    public BO4ModDataFunction rotate(Rotation rotation)
    {
    	BO4ModDataFunction rotatedBlock = new BO4ModDataFunction();

        BO4CustomStructureCoordinate rotatedCoords = BO4CustomStructureCoordinate.getRotatedBO3CoordsJustified(x, y, z, rotation);

        rotatedBlock.x = rotatedCoords.getX();
        rotatedBlock.y = rotatedCoords.getY();
        rotatedBlock.z = rotatedCoords.getZ();

        rotatedBlock.modId = modId;
        rotatedBlock.modData = modData;

        return rotatedBlock;
    }
    
    @Override
    public Class<BO4Config> getHolderType()
    {
        return BO4Config.class;
    }

	@Override
	public ModDataFunction<BO4Config> getNewInstance()
	{
		return new BO4ModDataFunction();
	}
}
