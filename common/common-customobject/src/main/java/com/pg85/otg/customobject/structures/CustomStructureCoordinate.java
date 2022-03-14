package com.pg85.otg.customobject.structures;

import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import java.nio.file.Path;

import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.interfaces.IStructuredCustomObject;

/**
 * Represents an object along with its location in the world.
 */
public abstract class CustomStructureCoordinate
{
	public String bo3Name;
	public String presetFolderName;
	
	protected transient IStructuredCustomObject object;
	public Rotation rotation;
	public int x;
	public short y;
	public int z;
	
	protected CustomStructureCoordinate() { } 
			
	public int getX()
	{
		return x;
	}

	public short getY()
	{
		return y;
	}

	public int getZ()
	{
		return z;
	}

	public Rotation getRotation()
	{
		return rotation;
	}
	
	public final int getChunkX()
	{
		return (int)MathHelper.floor(x / (double)16); 
	}
	
	public final int getChunkZ()
	{
		return (int)MathHelper.floor(z / (double)16);
	}
	
	/**
	 * Returns the object of this coordinate.
	 *
	 * @return The object.
	 */
	public IStructuredCustomObject getObject(Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		if(this.object == null)
		{
			CustomObject object = customObjectManager.getGlobalObjects().getObjectByName(this.bo3Name, this.presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);

			if(object == null || !(object instanceof StructuredCustomObject))
			{
				object = null;
				if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
				{
					logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Could not find BO3/BO4 " + this.bo3Name + " in GlobalObjects or WorldObjects directory.");
				}
			}

			this.bo3Name = object != null ? object.getName() : this.bo3Name;
			this.object = (StructuredCustomObject)object;
			return this.object;
		}

		return object;
	}
	
	@Override
	public int hashCode()
	{
		return (x >> 13) ^ (y >> 7) ^ z ^ object.getName().hashCode() ^ rotation.toString().hashCode();
	}
	
	@Override
	public boolean equals(Object otherObject)
	{
		if (otherObject == null)
		{
			return false;
		}
		if (!(otherObject instanceof CustomStructureCoordinate))
		{
			return false;
		}
		CustomStructureCoordinate otherCoord = (CustomStructureCoordinate) otherObject;
		if (otherCoord.x != x)
		{
			return false;
		}
		if (otherCoord.y != y)
		{
			return false;
		}
		if (otherCoord.z != z)
		{
			return false;
		}
		if (!otherCoord.rotation.equals(rotation))
		{
			return false;
		}
		if (!otherCoord.object.getName().equals(object.getName()))
		{
			return false;
		}
		return true;
	}	  
}
