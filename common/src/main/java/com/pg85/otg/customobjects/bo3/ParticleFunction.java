package com.pg85.otg.customobjects.bo3;

import com.pg85.otg.LocalWorld;
import com.pg85.otg.configuration.CustomObjectConfigFunction;
import com.pg85.otg.exception.InvalidConfigException;

import java.util.List;
import java.util.Random;

/**
 * Represents a block in a BO3.
 */
public class ParticleFunction extends BO3Function
{	
    public int x;
    public int y;
    public int z;
    
	public Boolean firstSpawn = true;
    
    public String particleName = "";
    
    public double interval = 1;
    public double intervalOffset = 0;
    
    public double velocityX = 0;
    public double velocityY = 0;
    public double velocityZ = 0;
    
    public boolean velocityXSet = false;
    public boolean velocityYSet = false;
    public boolean velocityZSet = false;
    
    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(5, args);
        // Those limits are arbitrary, LocalWorld.setBlock will limit it
        // correctly based on what chunks can be accessed
    	x = readInt(args.get(0), -100, 100);
		y = readInt(args.get(1), -1000, 1000);
        z = readInt(args.get(2), -100, 100);
        particleName = args.get(3);
        
        interval = readDouble(args.get(4), 0, Integer.MAX_VALUE);
        
        if(args.size() > 5)
        {
        	velocityX = readDouble(args.get(5), Integer.MIN_VALUE, Integer.MAX_VALUE);
        	velocityXSet = true;
        }
        if(args.size() > 6)
        {
        	velocityY = readDouble(args.get(6), Integer.MIN_VALUE, Integer.MAX_VALUE);
        	velocityYSet = true;
        }
        if(args.size() > 7)
        {
        	velocityZ = readDouble(args.get(7), Integer.MIN_VALUE, Integer.MAX_VALUE);
        	velocityZSet = true;
        }			
    }
    
    @Override
    public String makeString()
    {
    	return "Particle(" + x + ',' + y + ',' + z + ',' + particleName + ',' + interval + ',' + velocityX + ',' + velocityY + ',' + velocityZ + ')';
    }

    public String makeStringForPacket()
    {
    	return "Particle(" + x + ',' + y + ',' + z + ',' + particleName + ',' + interval + ',' + velocityX + ',' + velocityY + ',' + velocityZ + ',' + velocityXSet + ',' + velocityYSet + ',' + velocityZSet + ')';
    }
    
    @Override
    public ParticleFunction rotate()
    {
    	ParticleFunction rotatedBlock = new ParticleFunction();
        rotatedBlock.x = z;
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

    /**
     * Spawns this block at the position. The saved x, y and z in this block are
     * ignored.
     * <p/>
     * @param world  The world to spawn in.
     * @param random The random number generator.
     * @param x      The absolute x to spawn. The x-position in this object is
     *               ignored.
     * @param y      The absolute y to spawn. The y-position in this object is
     *               ignored.
     * @param z      The absolute z to spawn. The z-position in this object is
     *               ignored.
     */
    public void spawn(LocalWorld world, Random random, int x, int y, int z, boolean markBlockForUpdate)
    {
    	throw new RuntimeException(); 
    }

    @Override
    public boolean isAnalogousTo(CustomObjectConfigFunction<BO3Config> other)
    {
        if(!getClass().equals(other.getClass()))
        {
            return false;
        }
        ParticleFunction block = (ParticleFunction) other;
        return block.x == x && block.y == y && block.z == z && block.particleName.equalsIgnoreCase(particleName) && block.interval == interval && block.velocityX == velocityX && block.velocityY == velocityY && block.velocityZ == velocityZ;
    }
}
