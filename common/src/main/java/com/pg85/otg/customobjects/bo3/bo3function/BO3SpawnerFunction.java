package com.pg85.otg.customobjects.bo3.bo3function;

import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.customobjects.bofunctions.SpawnerFunction;

/**
 * Represents a block in a BO3.
 */
public class BO3SpawnerFunction extends SpawnerFunction<BO3Config>
{
    public BO3SpawnerFunction rotate()
    {
    	BO3SpawnerFunction rotatedBlock = new BO3SpawnerFunction();
        rotatedBlock.x = z;
        rotatedBlock.y = y;
        rotatedBlock.z = -x;
        rotatedBlock.mobName = mobName;

        rotatedBlock.originalnbtFileName = originalnbtFileName;
        rotatedBlock.nbtFileName = nbtFileName;

        rotatedBlock.groupSize = groupSize;
        rotatedBlock.interval = interval;
        rotatedBlock.spawnChance = spawnChance;
        rotatedBlock.maxCount = maxCount;
        rotatedBlock.despawnTime = despawnTime;

        rotatedBlock.velocityX = velocityZ;
        rotatedBlock.velocityY = velocityY;
        rotatedBlock.velocityZ = -velocityX;

        rotatedBlock.velocityXSet = velocityZSet;
        rotatedBlock.velocityYSet = velocityYSet;
        rotatedBlock.velocityZSet = velocityXSet;

        rotatedBlock.yaw = yaw; // TODO: Rotate! +90 or -90?
        rotatedBlock.pitch = pitch;

        return rotatedBlock;
    }
    
    @Override
    public Class<BO3Config> getHolderType()
    {
        return BO3Config.class;
    }

	@Override
	public SpawnerFunction<BO3Config> getNewInstance()
	{
		return new BO3SpawnerFunction();
	}
}
