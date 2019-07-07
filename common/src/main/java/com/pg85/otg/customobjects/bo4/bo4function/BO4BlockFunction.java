package com.pg85.otg.customobjects.bo4.bo4function;

import com.pg85.otg.customobjects.bo4.BO4Config;
import com.pg85.otg.customobjects.bofunctions.BlockFunction;
import com.pg85.otg.customobjects.structures.bo4.BO4CustomStructureCoordinate;
import com.pg85.otg.util.bo3.Rotation;

/**
 * Represents a block in a BO3.
 */
public class BO4BlockFunction extends BlockFunction<BO4Config>
{
    public BO4BlockFunction rotate(Rotation rotation)
    {
        BO4BlockFunction rotatedBlock = new BO4BlockFunction();

        rotatedBlock.material = material; // TODO: Make sure this won't cause problems

        BO4CustomStructureCoordinate rotatedCoords = BO4CustomStructureCoordinate.getRotatedBO3CoordsJustified(x, y, z, rotation);

        rotatedBlock.x = rotatedCoords.getX();
        rotatedBlock.y = rotatedCoords.getY();
        rotatedBlock.z = rotatedCoords.getZ();

    	// TODO: This makes no sense, why is rotation inverted??? Should be: NORTH:0,WEST:1,SOUTH:2,EAST:3

        // Apply rotation
    	if(rotation.getRotationId() == 3)
    	{
    		rotatedBlock.material = rotatedBlock.material.rotate();
    	}
    	if(rotation.getRotationId() == 2)
    	{
    		rotatedBlock.material = rotatedBlock.material.rotate();
    		rotatedBlock.material = rotatedBlock.material.rotate();
    	}
    	if(rotation.getRotationId() == 1)
    	{
    		rotatedBlock.material = rotatedBlock.material.rotate();
    		rotatedBlock.material = rotatedBlock.material.rotate();
    		rotatedBlock.material = rotatedBlock.material.rotate();
    	}

        rotatedBlock.metaDataTag = metaDataTag;
        rotatedBlock.metaDataName = metaDataName;

        return rotatedBlock;
    }   
    
    @Override
    public Class<BO4Config> getHolderType()
    {
        return BO4Config.class;
    }
}