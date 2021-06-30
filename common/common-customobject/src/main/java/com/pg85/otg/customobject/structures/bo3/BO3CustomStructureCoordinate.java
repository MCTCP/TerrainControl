package com.pg85.otg.customobject.structures.bo3;

import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.gen.DecorationArea;
import com.pg85.otg.util.interfaces.ILogger;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IStructuredCustomObject;

import java.nio.file.Path;

import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo3.BO3;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructureCoordinate;
import com.pg85.otg.customobject.util.BoundingBox;
import com.pg85.otg.logging.LogCategory;
import com.pg85.otg.logging.LogLevel;

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
	 * Gets the chunk that should spawn this object during decoration.
	 */
	ChunkCoordinate getDecoratingChunk(Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		// In the past we simply returned the chunk decorating for the origin
		// of the object. However, the origin is not guaranteed to be at the
		// center of the object. We need to know the exact center to choose
		// the appropriate spawning chunk.

		IStructuredCustomObject object = getObject(otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
		if(object == null)
		{
			return null;
		}
		if(!(object instanceof BO3))
		{
			if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
			{
				logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "BO3CustomStructure loaded with non-BO3 object " + object.getName());
			}
			return null;
		}

		BoundingBox box = ((BO3)object).getBoundingBox(this.rotation);
		int centerX = this.x + box.getMinX() + (box.getWidth() / 2);
		int centerZ = this.z + box.getMinZ() + (box.getDepth() / 2);
		// TODO: Remove this offset for 1.16, to align with other resources?
		return ChunkCoordinate.fromBlockCoords(
			centerX - DecorationArea.DECORATION_OFFSET,
			centerZ - DecorationArea.DECORATION_OFFSET
		);
	}
}
