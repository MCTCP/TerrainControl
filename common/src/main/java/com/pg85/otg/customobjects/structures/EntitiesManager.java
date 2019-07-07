package com.pg85.otg.customobjects.structures;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.customobjects.bofunctions.EntityFunction;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;

public class EntitiesManager
{
    public void spawnEntities(LocalWorld world, EntityFunction<?>[] entityDataInObject, CustomStructureCoordinate coordObject, ChunkCoordinate chunkCoordinate)
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

            	newEntityData.mobName = entityDataInObject[i].mobName;
            	newEntityData.groupSize = entityDataInObject[i].groupSize;
            	newEntityData.nameTagOrNBTFileName = entityDataInObject[i].nameTagOrNBTFileName;

        		world.spawnEntity(newEntityData);

        		if(!ChunkCoordinate.fromBlockCoords(newEntityData.x, newEntityData.z).equals(chunkCoordinate))
        		{
        			throw new RuntimeException(); // TODO: Remove after testing
        		}
        	} else {

            	newEntityData.y = coordObject.getY() + entityDataInObject[i].y;

            	newEntityData.x = coordObject.getX() + entityDataInObject[i].x;
            	newEntityData.z = coordObject.getZ() + entityDataInObject[i].z;

            	newEntityData.mobName = entityDataInObject[i].mobName;
            	newEntityData.groupSize = entityDataInObject[i].groupSize;
            	newEntityData.nameTagOrNBTFileName = entityDataInObject[i].nameTagOrNBTFileName;

        		world.spawnEntity(newEntityData);

        		if(!ChunkCoordinate.fromBlockCoords(newEntityData.x, newEntityData.z).equals(chunkCoordinate))
        		{
        			throw new RuntimeException(); // TODO: Remove after testing
        		}
        	}
    	}
    }
}
