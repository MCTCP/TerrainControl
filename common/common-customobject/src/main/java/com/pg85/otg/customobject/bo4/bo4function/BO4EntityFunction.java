package com.pg85.otg.customobject.bo4.bo4function;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.pg85.otg.customobject.bo4.BO4Config;
import com.pg85.otg.customobject.bofunctions.EntityFunction;
import com.pg85.otg.customobject.structures.bo4.BO4CustomStructureCoordinate;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.StreamHelper;

/**
 * Represents an entity in a BO3.
 */
public class BO4EntityFunction extends EntityFunction<BO4Config>
{	
	public BO4EntityFunction() { }
	
	private BO4EntityFunction(BO4Config holder)
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

		rotatedBlock.name = name;
		rotatedBlock.resourceLocation = resourceLocation;
		rotatedBlock.groupSize = groupSize;
		rotatedBlock.originalNameTagOrNBTFileName = originalNameTagOrNBTFileName;
		rotatedBlock.nameTagOrNBTFileName = nameTagOrNBTFileName;
		rotatedBlock.namedBinaryTag = namedBinaryTag;
		rotatedBlock.rotation = rotation.getRotationId();

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

		StreamHelper.writeStringToStream(stream, this.resourceLocation);
		stream.writeInt(this.groupSize);
		StreamHelper.writeStringToStream(stream, this.nameTagOrNBTFileName);
		StreamHelper.writeStringToStream(stream, this.originalNameTagOrNBTFileName);			
	}
	
	public static BO4EntityFunction fromStream(BO4Config holder, ByteBuffer buffer, boolean spawnLog, ILogger logger) throws IOException
	{
		BO4EntityFunction entityFunction = new BO4EntityFunction(holder);
				
		entityFunction.x = buffer.getInt();
		entityFunction.y = buffer.getInt();
		entityFunction.z = buffer.getInt();

		entityFunction.processEntityName(StreamHelper.readStringFromBuffer(buffer), logger);
		entityFunction.groupSize = buffer.getInt();
		entityFunction.nameTagOrNBTFileName= StreamHelper.readStringFromBuffer(buffer);
		entityFunction.originalNameTagOrNBTFileName= StreamHelper.readStringFromBuffer(buffer);
		if (entityFunction.originalNameTagOrNBTFileName != null)
		{
			entityFunction.processNameTagOrFileName(entityFunction.originalNameTagOrNBTFileName, spawnLog, logger);
		}
		entityFunction.rotation = 0;
		
		return entityFunction;
	}
}
