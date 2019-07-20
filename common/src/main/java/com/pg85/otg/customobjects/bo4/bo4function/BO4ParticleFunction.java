package com.pg85.otg.customobjects.bo4.bo4function;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

import com.pg85.otg.customobjects.bo4.BO4Config;
import com.pg85.otg.customobjects.bofunctions.ParticleFunction;
import com.pg85.otg.customobjects.structures.bo4.BO4CustomStructureCoordinate;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.StreamHelper;

/**
 * Represents a block in a BO3.
 */
public class BO4ParticleFunction extends ParticleFunction<BO4Config>
{
	public BO4ParticleFunction() { }	
	
	public BO4ParticleFunction(BO4Config holder)
	{
		this.holder = holder;
	}
	
    public BO4ParticleFunction rotate(Rotation rotation)
    {
    	BO4ParticleFunction rotatedBlock = new BO4ParticleFunction(this.getHolder());

        BO4CustomStructureCoordinate rotatedCoords = BO4CustomStructureCoordinate.getRotatedBO3CoordsJustified(x, y, z, rotation);

        rotatedBlock.x = rotatedCoords.getX();
        rotatedBlock.y = rotatedCoords.getY();
        rotatedBlock.z = rotatedCoords.getZ();

        rotatedBlock.velocityX = velocityX;
        rotatedBlock.velocityY = velocityY;
        rotatedBlock.velocityZ = velocityZ;

        rotatedBlock.velocityXSet = velocityXSet;
        rotatedBlock.velocityYSet = velocityYSet;
        rotatedBlock.velocityZSet = velocityZSet;

        double newVelocityX = rotatedBlock.velocityX;
        double newVelocityZ = rotatedBlock.velocityZ;

        boolean newVelocityXSet = rotatedBlock.velocityXSet;
        boolean newVelocityZSet = rotatedBlock.velocityZSet;

    	for(int i = 0; i < rotation.getRotationId(); i++)
    	{
            newVelocityX = rotatedBlock.velocityZ;
            newVelocityZ = -rotatedBlock.velocityX;

            rotatedBlock.velocityX = newVelocityX;
            rotatedBlock.velocityY = rotatedBlock.velocityY;
            rotatedBlock.velocityZ = newVelocityZ;

            newVelocityXSet = rotatedBlock.velocityZSet;
            newVelocityZSet = rotatedBlock.velocityXSet;

            rotatedBlock.velocityXSet = newVelocityXSet;
            rotatedBlock.velocityYSet = rotatedBlock.velocityYSet;
            rotatedBlock.velocityZSet = newVelocityZSet;
    	}

    	rotatedBlock.particleName = particleName;
    	rotatedBlock.interval = interval;

        return rotatedBlock;
    }
    
    @Override
    public Class<BO4Config> getHolderType()
    {
        return BO4Config.class;
    }

	@Override
	public ParticleFunction<BO4Config> getNewInstance()
	{
		return new BO4ParticleFunction(this.getHolder());
	}
	
    public void writeToStream(DataOutput stream) throws IOException
    {
        stream.writeInt(this.x);
        stream.writeInt(this.y);
        stream.writeInt(this.z);       

        stream.writeBoolean(this.firstSpawn);

    	StreamHelper.writeStringToStream(stream, this.particleName);

        stream.writeDouble(this.interval);
        stream.writeDouble(this.intervalOffset);

        stream.writeDouble(this.velocityX);
        stream.writeDouble(this.velocityY);
        stream.writeDouble(this.velocityZ);

        stream.writeBoolean(this.velocityXSet);
        stream.writeBoolean(this.velocityYSet);
        stream.writeBoolean(this.velocityZSet);
    }
    
    public static BO4ParticleFunction fromStream(BO4Config holder, DataInputStream stream) throws IOException
    {
    	BO4ParticleFunction particleFunction = new BO4ParticleFunction(holder);
    	
    	particleFunction.x = stream.readInt();
    	particleFunction.y = stream.readInt();
    	particleFunction.z = stream.readInt();
    	
    	particleFunction.firstSpawn = stream.readBoolean();
    	particleFunction.particleName = StreamHelper.readStringFromStream(stream);
    	particleFunction.interval = stream.readDouble();
    	particleFunction.intervalOffset = stream.readDouble();
    	particleFunction.velocityX = stream.readDouble();
    	particleFunction.velocityY = stream.readDouble();
    	particleFunction.velocityZ = stream.readDouble();
    	
    	particleFunction.velocityXSet = stream.readBoolean();
    	particleFunction.velocityYSet = stream.readBoolean();
    	particleFunction.velocityZSet = stream.readBoolean();
    	
    	return particleFunction;
    }
}
