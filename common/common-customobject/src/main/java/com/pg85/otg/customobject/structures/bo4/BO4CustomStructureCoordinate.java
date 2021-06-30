package com.pg85.otg.customobject.structures.bo4;

import com.pg85.otg.logging.LogCategory;
import com.pg85.otg.logging.LogLevel;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.interfaces.ILogger;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IStructuredCustomObject;

import java.nio.file.Path;

import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructureCoordinate;
import com.pg85.otg.customobject.structures.StructuredCustomObject;

/**
 * Represents an object along with its location in the world.
 */
public class BO4CustomStructureCoordinate extends CustomStructureCoordinate
{
	public boolean isSpawned;
	int branchDepth;
	boolean isRequiredBranch;
	boolean isWeightedBranch;
	String branchGroup;
		
	public BO4CustomStructureCoordinate(String presetFolderName, IStructuredCustomObject object, String customObjectName, Rotation rotation, int x, short y, int z, int branchDepth, boolean isRequiredBranch, boolean isWeightedBranch, String branchGroup)
	{
		this.presetFolderName = presetFolderName;
		this.bo3Name = object != null ? object.getName() : customObjectName != null && customObjectName.length() > 0 ? customObjectName : null;
		this.object = object;
		this.rotation = rotation != null ? rotation : Rotation.NORTH;
		this.x = x;
		this.y = y;
		this.z = z;
		this.branchDepth = branchDepth;
		this.isRequiredBranch = isRequiredBranch;
		this.isWeightedBranch = isWeightedBranch;
		this.branchGroup = branchGroup;
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

		return this.object;
	}
	
	/**
	 * Returns the object of this coordinate, casted to a
	 * StructuredCustomObject. Will throw a ClassCastExcpetion
	 * if the object isn't a StructuredCustomObject
	 *
	 * @return The casted object.
	*/
	StructuredCustomObject getStructuredObject(Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		return (StructuredCustomObject)getObject(otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
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
		if (!(otherObject instanceof BO4CustomStructureCoordinate))
		{
			return false;
		}
		BO4CustomStructureCoordinate otherCoord = (BO4CustomStructureCoordinate) otherObject;
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
	
	/**
	 * Same as getRotatedBO3Coords except it assumes that the minX=-8 maxX=7 minZ=-7 maxZ=8 coordinates have been
	 * centered and justified inside chunk (aligned to fit between 0,0 and 15,15).
	 */
	public static BO4CustomStructureCoordinate getRotatedBO3CoordsJustified(int x, int y, int z, Rotation newRotation)
	{
		int rotations = newRotation.getRotationId();

		int rotatedX = x;
		int rotatedZ = z;

		int newX = x;
		int newZ = z;

		for(int i = 0; i < rotations; i++)
		{
			newX = 15 - rotatedZ;
			newZ = rotatedX;

			rotatedX = newX;
			rotatedZ = newZ;
		}

		return new BO4CustomStructureCoordinate(null, null, null, newRotation, rotatedX, (short)y, rotatedZ, 0, false, false, null);
	}
	
	public static BO4CustomStructureCoordinate getRotatedCoord(int x, int y, int z, Rotation newRotation)
	{
		int rotations = newRotation.getRotationId();

		int rotatedX = x;
		int rotatedZ = z;

		int newX = x;
		int newZ = z;
		for(int i = 0; i < rotations; i++)
		{
			newX = rotatedZ;
			newZ = -rotatedX;
			rotatedX = newX;
			rotatedZ = newZ;
		}

		return new BO4CustomStructureCoordinate(null, null, null, newRotation, rotatedX, (short)y, rotatedZ, 0, false, false, null);
	}	
		
	/**
	 * Rotates a coordinate around its center, assumes the center is at 0,0.
	 * Should only be used for resouces that like Block() that spawn in BO3's and have a -1z offset.
	 * Should not be used for branches.
	 */
	public static BO4CustomStructureCoordinate getRotatedBO3Coords(int x, int y, int z, Rotation newRotation)
	{
		int rotations = newRotation.getRotationId();
		int rotatedX = x;
		int rotatedZ = z;

		int newX = x;
		int newZ = z;
		for(int i = 0; i < rotations; i++)
		{
			// TODO: Bo3's appear to be exported with the center block (0,0) in the top right quadrant of an x,z grid (European style).
			// MC puts 0,0 in the lower right corner though (American style).
			// This makes rotating BO3's (counter-clockwise) confusing.
			// For a 16x16 BO3 minX=-8 maxX=7 minZ=-7 maxZ=8:

			// Rotating block  0 0 0 counter clockwise 1 step should result in: -1 0 0
			// Rotating block -1 0 0 counter clockwise 1 step should result in: -1 0 1
			// Rotating block -1 0 1 counter clockwise 1 step should result in:  0 0 1
			// Rotating block  0 0 1 counter clockwise 1 step should result in:  0 0 0

			// Rotating block  0 0 -7 counter clockwise 1 step should result in: -8 0  0
			// Rotating block -8 0  0 counter clockwise 1 step should result in: -1 0  8
			// Rotating block -1 0  8 counter clockwise 1 step should result in:  7 0  1
			// Rotating block  7 0  1 counter clockwise 1 step should result in:  0 0 -7

			// Rotating block  7 0 -7 counter clockwise 1 step should result in: -8 0 -7
			// Rotating block -8 0 -7 counter clockwise 1 step should result in: -8 0  8
			// Rotating block -8 0  8 counter clockwise 1 step should result in:  7 0  8
			// Rotating block  7 0  8 counter clockwise 1 step should result in:  7 0 -7

			// So basically the center point of a BO3 (0,0) is actually 0,-1 if you'd place a BO3 at 0,0 in the world

			newX = rotatedZ - 1;
			newZ = -rotatedX;

			rotatedX = newX;
			rotatedZ = newZ;
		}

		return new BO4CustomStructureCoordinate(null, null, null, newRotation, rotatedX, (short)y, rotatedZ, 0, false, false, null);
	}
	
	// TODO: Why is this necessary for smoothing areas?
	public static BO4CustomStructureCoordinate getRotatedSmoothingCoords(int x, short y, int z, Rotation newRotation)
	{
		// Assuming initial rotation is always north

		int newX = 0;
		short newY = 0;
		int newZ = 0;
		int rotations = 0;

		// How many counter-clockwise rotations have to be applied?
		if (newRotation == Rotation.WEST)
		{
			rotations = 1;
		}
		else if (newRotation == Rotation.SOUTH)
		{
			rotations = 2;
		}
		else if (newRotation == Rotation.EAST)
		{
			rotations = 3;
		}

		// Apply rotation
		if (rotations == 0)
		{
			newX = x;
			newZ = z;
		}
		if (rotations == 1)
		{
			newX = z;
			newZ = -x + 15;
		}
		if (rotations == 2)
		{
			newX = -x + 15;
			newZ = -z + 15;
		}
		if (rotations == 3)
		{
			newX = -z + 15;
			newZ = x;
		}
		newY = y;

		return new BO4CustomStructureCoordinate(null, null, null, newRotation, newX, newY, newZ, 0, false, false, null);
	}
}
