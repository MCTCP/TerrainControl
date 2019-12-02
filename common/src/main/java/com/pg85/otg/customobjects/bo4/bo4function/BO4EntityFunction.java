package com.pg85.otg.customobjects.bo4.bo4function;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

import com.pg85.otg.customobjects.bo4.BO4Config;
import com.pg85.otg.customobjects.bofunctions.EntityFunction;
import com.pg85.otg.customobjects.structures.bo4.BO4CustomStructureCoordinate;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.StreamHelper;

/**
 * Represents an entity in a BO3.
 */
public class BO4EntityFunction extends EntityFunction<BO4Config>
{	
	public BO4EntityFunction() { }
	
	public BO4EntityFunction(BO4Config holder)
	{
		this.holder = holder;
	}
	
    public BO4EntityFunction rotate(Rotation rotation)
    {
    	BO4EntityFunction rotatedBlock = new BO4EntityFunction(this.getHolder());

        BO4CustomStructureCoordinate rotatedCoords = BO4CustomStructureCoordinate.getRotatedBO3CoordsJustified(x, y, z, rotation);

        rotatedBlock.x = rotatedCoords.getX();
        rotatedBlock.y = rotatedCoords.getY();
        rotatedBlock.z = rotatedCoords.getZ();

        rotatedBlock.mobName = mobName;
        rotatedBlock.groupSize = groupSize;
        rotatedBlock.originalNameTagOrNBTFileName = originalNameTagOrNBTFileName;
        rotatedBlock.nameTagOrNBTFileName = nameTagOrNBTFileName;

        return rotatedBlock;
    }
    
    @Override
    public Class<BO4Config> getHolderType()
    {
        return BO4Config.class;
    }

	@Override
	public EntityFunction<BO4Config> createNewInstance()
	{
		return new BO4EntityFunction(this.getHolder());
	}
	
    public void writeToStream(DataOutput stream) throws IOException
    {    	
        stream.writeInt(this.x);
        stream.writeInt(this.y);
        stream.writeInt(this.z);       

        StreamHelper.writeStringToStream(stream, this.mobName);
        stream.writeInt(this.groupSize);
        StreamHelper.writeStringToStream(stream, this.nameTagOrNBTFileName);
        StreamHelper.writeStringToStream(stream, this.originalNameTagOrNBTFileName);        	
    }
    
    public static BO4EntityFunction fromStream(BO4Config holder, ByteBuffer buffer) throws IOException
    {
    	BO4EntityFunction entityFunction = new BO4EntityFunction(holder);
    	   	
    	entityFunction.x = buffer.getInt();
    	entityFunction.y = buffer.getInt();
    	entityFunction.z = buffer.getInt();
    	
    	entityFunction.mobName = StreamHelper.readStringFromBuffer(buffer);
    	entityFunction.groupSize = buffer.getInt();
    	entityFunction.nameTagOrNBTFileName= StreamHelper.readStringFromBuffer(buffer);
    	entityFunction.originalNameTagOrNBTFileName= StreamHelper.readStringFromBuffer(buffer);
    	
    	return entityFunction;
    }
}
