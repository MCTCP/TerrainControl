package com.pg85.otg.customobjects.bo3.bo3function;

import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.customobjects.bo4.BO4Config;
import com.pg85.otg.customobjects.bofunctions.BlockFunction;

/**
 * Represents a block in a BO3.
 */
public class BO3BlockFunction extends BlockFunction<BO3Config>
{
	public BO3BlockFunction() { }
	
    public BO3BlockFunction(BO3Config holder)
    {
    	this.holder = holder;
    }
	
    public BO3BlockFunction rotate()
    {
        BO3BlockFunction rotatedBlock = new BO3BlockFunction();
        rotatedBlock.x = z;
        rotatedBlock.y = y;
        rotatedBlock.z = -x;
        rotatedBlock.material = material.rotate();
        rotatedBlock.metaDataTag = metaDataTag;
        rotatedBlock.metaDataName = metaDataName;

        return rotatedBlock;
    }

    @Override
    public Class<BO3Config> getHolderType()
    {
        return BO3Config.class;
    }
}