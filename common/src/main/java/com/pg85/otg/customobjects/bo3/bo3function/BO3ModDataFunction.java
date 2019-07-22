package com.pg85.otg.customobjects.bo3.bo3function;

import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.customobjects.bofunctions.ModDataFunction;

/**
 * Represents a block in a BO3.
 */
public class BO3ModDataFunction extends ModDataFunction<BO3Config>
{
    public BO3ModDataFunction rotate()
    {
        BO3ModDataFunction rotatedBlock = new BO3ModDataFunction();
        rotatedBlock.x = z - 1;
        rotatedBlock.y = y;
        rotatedBlock.z = -x;
        rotatedBlock.modId = modId;
        rotatedBlock.modData = modData;

        return rotatedBlock;
    }
    
    @Override
    public Class<BO3Config> getHolderType()
    {
        return BO3Config.class;
    }

	@Override
	public ModDataFunction<BO3Config> getNewInstance()
	{
		return new BO3ModDataFunction();
	}
}
