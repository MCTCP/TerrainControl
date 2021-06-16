package com.pg85.otg.customobject.structures.bo3;

import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IStructuredCustomObject;

import java.nio.file.Path;

import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo3.BO3;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructureCoordinate;
import com.pg85.otg.customobject.util.BoundingBox;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;

/**
 * Represents an object along with its location in the world.
 */
public class BO3CustomStructureCoordinate extends CustomStructureCoordinate
{
	public BO3CustomStructureCoordinate(String presetFolderName, IStructuredCustomObject object, String customObjectName, Rotation rotation, int x, short y, int z)
	{
		this.presetFolderName = presetFolderName;
		this.object = object;

		bo3Name = object != null ? object.getName() : customObjectName != null && customObjectName.length() > 0 ? customObjectName : null;

		this.rotation = rotation;
		this.x = x;
		this.y = y;
		this.z = z;
	}	
		
	/**
	 * Gets the chunk that should populate for this object.
	 * @return The chunk.
	 */
	ChunkCoordinate getPopulatingChunk(Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		// In the past we simply returned the chunk populating for the origin
		// of the object. However, the origin is not guaranteed to be at the
		// center of the object. We need to know the exact center to choose
		// the appropriate spawning chunk.

		IStructuredCustomObject object = getObject(otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
		if(object == null)
		{
			return null;
		}
		if(!(object instanceof BO3))
		{
			if(spawnLog)
			{
				logger.log(LogMarker.WARN, "BO3CustomStructure loaded with non-BO3 object " + object.getName());
			}
			return null;
		}

		BoundingBox box = ((BO3)object).getBoundingBox(rotation);
		int centerX = x + box.getMinX() + (box.getWidth() / 2);
		int centerZ = z + box.getMinZ() + (box.getDepth() / 2);

		return ChunkCoordinate.getPopulatingChunk(centerX, centerZ);
	}
}
