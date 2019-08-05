package com.pg85.otg.customobjects.bo3.bo3function;

import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.customobjects.bofunctions.ParticleFunction;

/**
 * Represents a block in a BO3.
 */
public class BO3ParticleFunction extends ParticleFunction<BO3Config>
{
    public BO3ParticleFunction rotate()
    {
    	BO3ParticleFunction rotatedBlock = new BO3ParticleFunction();
        rotatedBlock.x = z - 1;
        rotatedBlock.y = y;
        rotatedBlock.z = -x;
        rotatedBlock.particleName = particleName;

        rotatedBlock.interval = interval;

        rotatedBlock.velocityX = velocityZ;
        rotatedBlock.velocityY = velocityY;
        rotatedBlock.velocityZ = -velocityX;

        rotatedBlock.velocityXSet = velocityZSet;
        rotatedBlock.velocityYSet = velocityYSet;
        rotatedBlock.velocityZSet = velocityXSet;

        return rotatedBlock;
    }

    @Override
    public Class<BO3Config> getHolderType()
    {
        return BO3Config.class;
    }

	@Override
	public ParticleFunction<BO3Config> getNewInstance()
	{
		return new BO3ParticleFunction();
	}
}
