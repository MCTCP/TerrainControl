package com.pg85.otg.customobject.bo3.bo3function;

import com.pg85.otg.customobject.bo3.BO3Config;
import com.pg85.otg.customobject.bofunctions.MinecraftObjectFunction;

/**
 * Represents a block in a BO3.
 */
public class BO3MinecraftObjectFunction extends MinecraftObjectFunction<BO3Config>
{
    public BO3MinecraftObjectFunction rotate()
    {
        BO3MinecraftObjectFunction rotatedBlock = new BO3MinecraftObjectFunction();
        rotatedBlock.x = z;
        rotatedBlock.y = y;
        rotatedBlock.z = -x;
        rotatedBlock.rotation = rotation.next();

        return rotatedBlock;
    }
    
    @Override
    public Class<BO3Config> getHolderType()
    {
        return BO3Config.class;
    }
}
