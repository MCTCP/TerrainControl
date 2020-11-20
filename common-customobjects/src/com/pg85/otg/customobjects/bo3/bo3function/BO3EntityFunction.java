package com.pg85.otg.customobjects.bo3.bo3function;

import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.customobjects.bofunctions.EntityFunction;
import com.pg85.otg.util.interfaces.IEntityFunction;

/**
 * Represents an entity in a BO3.
 */
public class BO3EntityFunction extends EntityFunction<BO3Config> implements IEntityFunction<BO3Config>
{
    public BO3EntityFunction rotate()
    {
    	BO3EntityFunction rotatedBlock = new BO3EntityFunction();
        rotatedBlock.x = z;
        rotatedBlock.y = y;
        rotatedBlock.z = -x;
        rotatedBlock.name = name;
        rotatedBlock.resourceLocation = resourceLocation;
        rotatedBlock.groupSize = groupSize;
        rotatedBlock.originalNameTagOrNBTFileName = originalNameTagOrNBTFileName;
        rotatedBlock.nameTagOrNBTFileName = nameTagOrNBTFileName;
        rotatedBlock.namedBinaryTag = namedBinaryTag;
        rotatedBlock.rotation = (rotation + 1) % 4;

        return rotatedBlock;
    }

    @Override
    public Class<BO3Config> getHolderType()
    {
        return BO3Config.class;
    }

	@Override
	public EntityFunction<BO3Config> createNewInstance()
	{
		return new BO3EntityFunction();
	}
}
