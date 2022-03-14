package com.pg85.otg.customobject.structures;

import com.pg85.otg.customobject.bofunctions.EntityFunction;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;

public class EntitiesManager
{
	public void spawnEntities(IWorldGenRegion worldGenRegion, EntityFunction<?>[] entityDataInObject, CustomStructureCoordinate coordObject, ChunkCoordinate chunkCoordinate, CustomStructureCache structureCache, ILogger logger)
	{
		for(int i = 0; i < entityDataInObject.length; i++)
		{
			EntityFunction<?> newEntityData = entityDataInObject[i].createNewInstance();

			if(coordObject.getRotation() != Rotation.NORTH)
			{
				int rotations = 0;
				// How many counter-clockwise rotations have to be applied?
				if(coordObject.getRotation() == Rotation.WEST)
				{
					rotations = 1;
				}
				else if(coordObject.getRotation() == Rotation.SOUTH)
				{
					rotations = 2;
				}
				else if(coordObject.getRotation() == Rotation.EAST)
				{
					rotations = 3;
				}

				// Apply rotation
				if(rotations == 0)
				{
					newEntityData.x = entityDataInObject[i].x;
					newEntityData.z = entityDataInObject[i].z;
				}
				if(rotations == 1)
				{
					newEntityData.x = entityDataInObject[i].z;
					newEntityData.z = -entityDataInObject[i].x + 15;
				}
				if(rotations == 2)
				{
					newEntityData.x = -entityDataInObject[i].x + 15;
					newEntityData.z = -entityDataInObject[i].z + 15;
				}
				if(rotations == 3)
				{
					newEntityData.x = -entityDataInObject[i].z + 15;
					newEntityData.z = entityDataInObject[i].x;
				}
				newEntityData.y = coordObject.getY() + entityDataInObject[i].y;

				newEntityData.x = coordObject.getX() + newEntityData.x;
				newEntityData.z = coordObject.getZ() + newEntityData.z;

				newEntityData.name = entityDataInObject[i].name;
				newEntityData.resourceLocation = entityDataInObject[i].resourceLocation;
				newEntityData.groupSize = entityDataInObject[i].groupSize;
				newEntityData.nameTagOrNBTFileName = entityDataInObject[i].nameTagOrNBTFileName;
				newEntityData.namedBinaryTag = entityDataInObject[i].namedBinaryTag;
				newEntityData.rotation = rotations;

				worldGenRegion.spawnEntity(newEntityData);
			} else {

				newEntityData.y = coordObject.getY() + entityDataInObject[i].y;

				newEntityData.x = coordObject.getX() + entityDataInObject[i].x;
				newEntityData.z = coordObject.getZ() + entityDataInObject[i].z;

				newEntityData.name = entityDataInObject[i].name;
				newEntityData.resourceLocation = entityDataInObject[i].resourceLocation;
				newEntityData.groupSize = entityDataInObject[i].groupSize;
				newEntityData.nameTagOrNBTFileName = entityDataInObject[i].nameTagOrNBTFileName;
				newEntityData.namedBinaryTag = entityDataInObject[i].namedBinaryTag;
				newEntityData.rotation = 0;

				worldGenRegion.spawnEntity(newEntityData);
			}
		}
	}
}
