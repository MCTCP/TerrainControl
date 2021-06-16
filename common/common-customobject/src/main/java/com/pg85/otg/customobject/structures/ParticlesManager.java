package com.pg85.otg.customobject.structures;

import java.util.HashSet;

import com.pg85.otg.customobject.bofunctions.ParticleFunction;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;

public class ParticlesManager
{
	public HashSet<ParticleFunction<?>> particleData = new HashSet<ParticleFunction<?>>();
	
	// TODO: Only used for BO4's, create BO4ParticlesManager?
	public void spawnParticles(ParticleFunction<?>[] particleDataInObject, CustomStructureCoordinate coordObject, ChunkCoordinate chunkCoordinate)
	{
		for(int i = 0; i < particleDataInObject.length; i++)
		{
			ParticleFunction<?> newParticleData = particleDataInObject[i].getNewInstance();
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
					newParticleData.x = particleDataInObject[i].x;
					newParticleData.velocityX = particleDataInObject[i].velocityX;
					newParticleData.z = particleDataInObject[i].z;
					newParticleData.velocityZ = particleDataInObject[i].velocityZ;
					newParticleData.velocityXSet = particleDataInObject[i].velocityXSet;
					newParticleData.velocityZSet = particleDataInObject[i].velocityZSet;
				}
				if(rotations == 1)
				{
					newParticleData.x = particleDataInObject[i].z;
					newParticleData.velocityX = particleDataInObject[i].velocityZ;
					newParticleData.z = -particleDataInObject[i].x + 15;
					newParticleData.velocityZ = -particleDataInObject[i].velocityX;
					newParticleData.velocityXSet = particleDataInObject[i].velocityZSet;
					newParticleData.velocityZSet = particleDataInObject[i].velocityXSet;
				}
				if(rotations == 2)
				{
					newParticleData.x = -particleDataInObject[i].x + 15;
					newParticleData.velocityX = -particleDataInObject[i].velocityX;
					newParticleData.z = -particleDataInObject[i].z + 15;
					newParticleData.velocityZ = -particleDataInObject[i].velocityZ;
					newParticleData.velocityXSet = particleDataInObject[i].velocityXSet;
					newParticleData.velocityZSet = particleDataInObject[i].velocityZSet;
				}
				if(rotations == 3)
				{
					newParticleData.x = -particleDataInObject[i].z + 15;
					newParticleData.velocityX = -particleDataInObject[i].velocityZ;
					newParticleData.z = particleDataInObject[i].x;
					newParticleData.velocityZ = particleDataInObject[i].velocityX;
					newParticleData.velocityXSet = particleDataInObject[i].velocityZSet;
					newParticleData.velocityZSet = particleDataInObject[i].velocityXSet;
				}
				newParticleData.y = coordObject.getY() + particleDataInObject[i].y;

				newParticleData.x = coordObject.getX() + newParticleData.x;
				newParticleData.z = coordObject.getZ() + newParticleData.z;

				newParticleData.particleName = particleDataInObject[i].particleName;

				newParticleData.interval = particleDataInObject[i].interval;

				newParticleData.velocityY = particleDataInObject[i].velocityY;
				newParticleData.velocityYSet = particleDataInObject[i].velocityYSet;

				particleData.add(newParticleData);

				if(!ChunkCoordinate.fromBlockCoords(newParticleData.x, newParticleData.z).equals(chunkCoordinate))
				{
					throw new RuntimeException(); // TODO: Remove after testing
				}
			} else {

				newParticleData.y = coordObject.getY() + particleDataInObject[i].y;

				newParticleData.x = coordObject.getX() + particleDataInObject[i].x;
				newParticleData.z = coordObject.getZ() + particleDataInObject[i].z;

				newParticleData.particleName = particleDataInObject[i].particleName;

				newParticleData.interval = particleDataInObject[i].interval;

				newParticleData.velocityX = particleDataInObject[i].velocityX;
				newParticleData.velocityY = particleDataInObject[i].velocityY;
				newParticleData.velocityZ = particleDataInObject[i].velocityZ;

				newParticleData.velocityXSet = particleDataInObject[i].velocityXSet;
				newParticleData.velocityYSet = particleDataInObject[i].velocityYSet;
				newParticleData.velocityZSet = particleDataInObject[i].velocityZSet;

				particleData.add(newParticleData);

				if(!ChunkCoordinate.fromBlockCoords(newParticleData.x, newParticleData.z).equals(chunkCoordinate))
				{
					throw new RuntimeException(); // TODO: Remove after testing
				}
			}
		}
	}
}
