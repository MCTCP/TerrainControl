package com.pg85.otg.customobject.structures;

import java.util.HashSet;

import com.pg85.otg.customobject.bofunctions.SpawnerFunction;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;

public class SpawnerManager
{
	public HashSet<SpawnerFunction<?>> spawnerData = new HashSet<SpawnerFunction<?>>();
	
	// TODO: Only used for BO4's, create BO4SpawnerManager?
	public void spawnSpawners(SpawnerFunction<?>[] spawnerDataInObject, CustomStructureCoordinate coordObject, ChunkCoordinate chunkCoordinate)
	{
		for(int i = 0; i < spawnerDataInObject.length; i++)
		{
			SpawnerFunction<?> newSpawnerData = spawnerDataInObject[i].getNewInstance();
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
					newSpawnerData.x = spawnerDataInObject[i].x;
					newSpawnerData.velocityX = spawnerDataInObject[i].velocityX;
					newSpawnerData.z = spawnerDataInObject[i].z;
					newSpawnerData.velocityZ = spawnerDataInObject[i].velocityZ;
					newSpawnerData.velocityXSet = spawnerDataInObject[i].velocityXSet;
					newSpawnerData.velocityZSet = spawnerDataInObject[i].velocityZSet;
				}
				if(rotations == 1)
				{
					newSpawnerData.x = spawnerDataInObject[i].z;
					newSpawnerData.velocityX = spawnerDataInObject[i].velocityZ;
					newSpawnerData.z = -spawnerDataInObject[i].x + 15;
					newSpawnerData.velocityZ = -spawnerDataInObject[i].velocityX;
					newSpawnerData.velocityXSet = spawnerDataInObject[i].velocityZSet;
					newSpawnerData.velocityZSet = spawnerDataInObject[i].velocityXSet;
				}
				if(rotations == 2)
				{
					newSpawnerData.x = -spawnerDataInObject[i].x + 15;
					newSpawnerData.velocityX = -spawnerDataInObject[i].velocityX;
					newSpawnerData.z = -spawnerDataInObject[i].z + 15;
					newSpawnerData.velocityZ = -spawnerDataInObject[i].velocityZ;
					newSpawnerData.velocityXSet = spawnerDataInObject[i].velocityXSet;
					newSpawnerData.velocityZSet = spawnerDataInObject[i].velocityZSet;
				}
				if(rotations == 3)
				{
					newSpawnerData.x = -spawnerDataInObject[i].z + 15;
					newSpawnerData.velocityX = -spawnerDataInObject[i].velocityZ;
					newSpawnerData.z = spawnerDataInObject[i].x;
					newSpawnerData.velocityZ = spawnerDataInObject[i].velocityX;
					newSpawnerData.velocityXSet = spawnerDataInObject[i].velocityZSet;
					newSpawnerData.velocityZSet = spawnerDataInObject[i].velocityXSet;
				}
				newSpawnerData.y = coordObject.getY() + spawnerDataInObject[i].y;

				newSpawnerData.x = coordObject.getX() + newSpawnerData.x;
				newSpawnerData.z = coordObject.getZ() + newSpawnerData.z;

				newSpawnerData.mobName = spawnerDataInObject[i].mobName;
				newSpawnerData.originalnbtFileName = spawnerDataInObject[i].originalnbtFileName;
				newSpawnerData.nbtFileName = spawnerDataInObject[i].nbtFileName;
				newSpawnerData.groupSize = spawnerDataInObject[i].groupSize;
				newSpawnerData.interval = spawnerDataInObject[i].interval;
				newSpawnerData.spawnChance = spawnerDataInObject[i].spawnChance;
				newSpawnerData.maxCount= spawnerDataInObject[i].maxCount;

				newSpawnerData.despawnTime = spawnerDataInObject[i].despawnTime;

				newSpawnerData.velocityY = spawnerDataInObject[i].velocityY;
				newSpawnerData.velocityYSet = spawnerDataInObject[i].velocityYSet;

				newSpawnerData.yaw = spawnerDataInObject[i].yaw;
				newSpawnerData.pitch = spawnerDataInObject[i].pitch;

				spawnerData.add(newSpawnerData);

				if(!ChunkCoordinate.fromBlockCoords(newSpawnerData.x, newSpawnerData.z).equals(chunkCoordinate))
				{
					throw new RuntimeException(); // TODO: Remove after testing
				}
			} else {

				newSpawnerData.y = coordObject.getY() + spawnerDataInObject[i].y;

				newSpawnerData.x = coordObject.getX() + spawnerDataInObject[i].x;
				newSpawnerData.z = coordObject.getZ() + spawnerDataInObject[i].z;

				newSpawnerData.mobName = spawnerDataInObject[i].mobName;
				newSpawnerData.originalnbtFileName = spawnerDataInObject[i].originalnbtFileName;
				newSpawnerData.nbtFileName = spawnerDataInObject[i].nbtFileName;
				newSpawnerData.groupSize = spawnerDataInObject[i].groupSize;
				newSpawnerData.interval = spawnerDataInObject[i].interval;
				newSpawnerData.spawnChance = spawnerDataInObject[i].spawnChance;
				newSpawnerData.maxCount= spawnerDataInObject[i].maxCount;

				newSpawnerData.despawnTime = spawnerDataInObject[i].despawnTime;

				newSpawnerData.velocityX = spawnerDataInObject[i].velocityX;
				newSpawnerData.velocityY = spawnerDataInObject[i].velocityY;
				newSpawnerData.velocityZ = spawnerDataInObject[i].velocityZ;

				newSpawnerData.velocityXSet = spawnerDataInObject[i].velocityXSet;
				newSpawnerData.velocityYSet = spawnerDataInObject[i].velocityYSet;
				newSpawnerData.velocityZSet = spawnerDataInObject[i].velocityZSet;

				newSpawnerData.yaw = spawnerDataInObject[i].yaw;
				newSpawnerData.pitch = spawnerDataInObject[i].pitch;

				spawnerData.add(newSpawnerData);

				if(!ChunkCoordinate.fromBlockCoords(newSpawnerData.x, newSpawnerData.z).equals(chunkCoordinate))
				{
					throw new RuntimeException(); // TODO: Remove after testing
				}
			}
		}
	}
}
