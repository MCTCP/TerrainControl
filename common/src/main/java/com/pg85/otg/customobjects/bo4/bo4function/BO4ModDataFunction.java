package com.pg85.otg.customobjects.bo4.bo4function;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.MappedByteBuffer;

import com.pg85.otg.customobjects.bo4.BO4Config;
import com.pg85.otg.customobjects.bofunctions.ModDataFunction;
import com.pg85.otg.customobjects.structures.bo4.BO4CustomStructureCoordinate;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.StreamHelper;

/**
 * Represents a block in a BO3.
 */
public class BO4ModDataFunction extends ModDataFunction<BO4Config>
{
	public BO4ModDataFunction() { }
	
	public BO4ModDataFunction(BO4Config holder)
	{
		this.holder = holder;
	}
	
    public BO4ModDataFunction rotate(Rotation rotation)
    {
    	BO4ModDataFunction rotatedBlock = new BO4ModDataFunction(this.getHolder());

        BO4CustomStructureCoordinate rotatedCoords = BO4CustomStructureCoordinate.getRotatedBO3CoordsJustified(x, y, z, rotation);

        rotatedBlock.x = rotatedCoords.getX();
        rotatedBlock.y = rotatedCoords.getY();
        rotatedBlock.z = rotatedCoords.getZ();

        rotatedBlock.modId = modId;
        rotatedBlock.modData = modData;

        return rotatedBlock;
    }
    
    @Override
    public Class<BO4Config> getHolderType()
    {
        return BO4Config.class;
    }

	@Override
	public ModDataFunction<BO4Config> getNewInstance()
	{
		return new BO4ModDataFunction(this.getHolder());
	}
	
    public void writeToStream(DataOutput stream) throws IOException
    {
        stream.writeInt(this.x);
        stream.writeInt(this.y);
        stream.writeInt(this.z);
        
        StreamHelper.writeStringToStream(stream, this.modId);
        StreamHelper.writeStringToStream(stream, this.modData);
    }
    
    public static BO4ModDataFunction fromStream(BO4Config holder, MappedByteBuffer buffer) throws IOException
    {
    	BO4ModDataFunction modDataFunction = new BO4ModDataFunction(holder);
    	
    	modDataFunction.x = buffer.getInt();
    	modDataFunction.y = buffer.getInt();
    	modDataFunction.z = buffer.getInt();
    	
    	modDataFunction.modId = StreamHelper.readStringFromBuffer(buffer);
    	modDataFunction.modData = StreamHelper.readStringFromBuffer(buffer);
    	
    	return modDataFunction;
    }
}
