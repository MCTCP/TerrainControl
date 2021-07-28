package com.pg85.otg.customobject.bo4.bo4function;

import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

import com.pg85.otg.customobject.bo4.BO4Config;
import com.pg85.otg.customobject.bofunctions.BlockFunction;
import com.pg85.otg.customobject.structures.bo4.BO4CustomStructureCoordinate;
import com.pg85.otg.customobject.util.NBTHelper;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.materials.LocalMaterialData;

/**
 * Represents a block in a BO3.
 */
public class BO4BlockFunction extends BlockFunction<BO4Config>
{
	public BO4BlockFunction() { }
	
	public BO4BlockFunction(BO4Config holder)
	{
		this.holder = holder;
	}

	@Override
	public void spawn(IWorldGenRegion worldGenRegion, Random random, int x, int y, int z)
	{
		 worldGenRegion.setBlock(x, y, z, this.material, this.nbt);
	}	
	
	@Override
	public void spawn(IWorldGenRegion worldGenRegion, Random random, int x, int y, int z, ReplaceBlockMatrix replaceBlocks)
	{
		 worldGenRegion.setBlock(x, y, z, this.material, this.nbt, replaceBlocks);			 
	}
 
	public BO4BlockFunction rotate(Rotation rotation)
	{
		BO4BlockFunction rotatedBlock = new BO4BlockFunction(this.getHolder());
		rotatedBlock.material = this.material; // TODO: Make sure this won't cause problems
		
		BO4CustomStructureCoordinate rotatedCoords = BO4CustomStructureCoordinate.getRotatedBO3CoordsJustified(x, y, z, rotation);
		rotatedBlock.x = rotatedCoords.getX();
		rotatedBlock.y = rotatedCoords.getY();
		rotatedBlock.z = rotatedCoords.getZ();
		
		// TODO: This makes no sense, why is rotation inverted??? Should be: NORTH:0,WEST:1,SOUTH:2,EAST:3
		
		// Apply rotation
		if(rotation.getRotationId() == 3)
		{
			rotatedBlock.material = rotatedBlock.material.rotate(1);
		}
		if(rotation.getRotationId() == 2)
		{
			rotatedBlock.material = rotatedBlock.material.rotate(2);
		}
		if(rotation.getRotationId() == 1)
		{
			rotatedBlock.material = rotatedBlock.material.rotate(3);
		}
		
		rotatedBlock.nbt = nbt;
		rotatedBlock.nbtName = nbtName;
		
		return rotatedBlock;
	}

	@Override
	public Class<BO4Config> getHolderType()
	{
		return BO4Config.class;
	}

	public void writeToStream(String[] metaDataNames, LocalMaterialData[] materials, DataOutput stream) throws IOException
	{
		stream.writeShort(this.y);
		boolean bFound = false;
		if(this.material != null)
		{
			for(int i = 0; i < materials.length; i++)
			{
				if(materials[i].equals(this.material))
				{
					stream.writeShort(i);
					bFound = true;
					break;
				}
			}
		}
		if(!bFound)
		{
			stream.writeShort(-1);
		}
		bFound = false;
		if(this.nbtName != null)
		{
			for(int i = 0; i < metaDataNames.length; i++)
			{
				if(metaDataNames[i].equals(this.nbtName))
				{
					stream.writeShort(i);
					bFound = true;
					break;
				}
			}
		}
		if(!bFound)
		{
			stream.writeShort(-1);
		}
	}

	public static BO4BlockFunction fromStream(int x, int z, String[] metaDataNames, LocalMaterialData[] materials, BO4Config holder, ByteBuffer buffer, ILogger logger) throws IOException
	{
		BO4BlockFunction rbf = new BO4BlockFunction(holder);
		
		File file = holder.getFile();
		
		rbf.x = x;
		rbf.y = buffer.getShort();
		rbf.z = z;
		
		short materialId = buffer.getShort();
		if(materialId != -1)
		{
			rbf.material = materials[materialId];
		}
		short metaDataNameId = buffer.getShort();
		if(metaDataNameId != -1)
		{
			rbf.nbtName = metaDataNames[metaDataNameId];
		}
		
		if(rbf.nbtName != null)
		{
			// Get the file
			rbf.nbt = NBTHelper.loadMetadata(rbf.nbtName, file, logger);
			if(rbf.nbt == null)
			{
				rbf.nbtName = null;
			}
		}
		
		return rbf;
	}
}
