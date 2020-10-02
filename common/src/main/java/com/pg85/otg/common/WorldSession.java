package com.pg85.otg.common;

import java.util.ArrayList;
import java.util.HashMap;

import com.pg85.otg.customobjects.bo3.BO3;
import com.pg85.otg.customobjects.bo4.BO4;
import com.pg85.otg.customobjects.bofunctions.ModDataFunction;
import com.pg85.otg.customobjects.bofunctions.ParticleFunction;
import com.pg85.otg.customobjects.bofunctions.SpawnerFunction;
import com.pg85.otg.customobjects.structures.CustomStructure;
import com.pg85.otg.customobjects.structures.bo4.BO4CustomStructure;
import com.pg85.otg.util.ChunkCoordinate;

// TODO: Implement this properly for spigot (maybe one day..)
public abstract class WorldSession
{
	protected LocalWorld world;

	public WorldSession(LocalWorld world)
	{
		this.world = world;
	}

	public abstract ArrayList<ParticleFunction<?>> getParticleFunctions();

	public abstract int getPregenerationRadius();
	public abstract int setPregenerationRadius(int value);

	public abstract ChunkCoordinate getPreGeneratorCenterPoint();
	public abstract void setPreGeneratorCenterPoint(ChunkCoordinate chunkCoord);

	public abstract int getPregeneratedBorderLeft();
	public abstract int getPregeneratedBorderRight();
	public abstract int getPregeneratedBorderTop();
	public abstract int getPregeneratedBorderBottom();

	public abstract boolean getPreGeneratorIsRunning();

    public String getStructureInfoAt(double x, double z)
    {
    	String structureInfo = "";
		ChunkCoordinate playerChunk = ChunkCoordinate.fromBlockCoords((int)x, (int)z);
		// if the player is in range
		CustomStructure worldInfoChunk = world.getStructureCache().getChunkData(playerChunk);
		if(worldInfoChunk != null)
		{
			if(worldInfoChunk instanceof BO4CustomStructure)
			{
    			structureInfo += "-- BO4 Info -- \r\nName: " + ((BO4)worldInfoChunk.start.getObject()).getConfig().getName().replace("Start", "") + "\r\nAuthor: " + ((BO4)worldInfoChunk.start.getObject()).getConfig().author + "\r\nDescription: " + ((BO4)worldInfoChunk.start.getObject()).getConfig().description;
    			String branchesInChunk = ((BO4CustomStructure)worldInfoChunk).ObjectsToSpawnInfo.get(playerChunk);
    			if(branchesInChunk != null && branchesInChunk.length() > 0)
    			{
    				structureInfo += "\r\n" + branchesInChunk;
    			}
			} else {
    			structureInfo += "-- BO3 Info -- \r\nName: " + ((BO3)worldInfoChunk.start.getObject()).getSettings().getName().replace("Start", "") + "\r\nAuthor: " + ((BO3)worldInfoChunk.start.getObject()).getSettings().author + "\r\nDescription: " + ((BO3)worldInfoChunk.start.getObject()).getSettings().description;
			}
		}
    	return structureInfo;
    }

    public HashMap<String,ArrayList<ModDataFunction<?>>> getModDataForChunk(ChunkCoordinate chunkCoord)
    {
    	HashMap<String,ArrayList<ModDataFunction<?>>> result = new HashMap<String,ArrayList<ModDataFunction<?>>>();
		CustomStructure worldInfoChunk = world.getStructureCache().getChunkData(chunkCoord);
		if(worldInfoChunk != null)
		{
			for(ModDataFunction<?> modData : worldInfoChunk.modDataManager.modData)
			{
				if(ChunkCoordinate.fromBlockCoords(modData.x, modData.z).equals(chunkCoord)) // modData for all branches of the structure is stored, make sure the modData is in this chunk
				{
    				if(!result.containsKey(modData.modId))
    				{
    					result.put(modData.modId, new ArrayList<ModDataFunction<?>>());
    				}
					result.get(modData.modId).add(modData);
				}
			}
		}
    	return result;
    }

    public ArrayList<SpawnerFunction<?>> getSpawnersForChunk(ChunkCoordinate chunkCoord)
    {
    	ArrayList<SpawnerFunction<?>> result = new ArrayList<SpawnerFunction<?>>();
		CustomStructure worldInfoChunk = world.getStructureCache().getChunkData(chunkCoord);

		if(worldInfoChunk != null)
		{
			for(SpawnerFunction<?> spawnerData : worldInfoChunk.spawnerManager.spawnerData)
			{
				if(ChunkCoordinate.fromBlockCoords(spawnerData.x, spawnerData.z).equals(chunkCoord)) // spawnerData for all branches of the structure is stored, make sure the modData is in this chunk
				{
					result.add(spawnerData);
				}
			}
		}
    	return result;
    }

    public ArrayList<ParticleFunction<?>> getParticlesForChunk(ChunkCoordinate chunkCoord)
    {
    	ArrayList<ParticleFunction<?>> result = new ArrayList<ParticleFunction<?>>();
		CustomStructure worldInfoChunk = world.getStructureCache().getChunkData(chunkCoord);

		if(worldInfoChunk != null)
		{
			for(ParticleFunction<?> particleData : worldInfoChunk.particlesManager.particleData)
			{
				if(ChunkCoordinate.fromBlockCoords(particleData.x, particleData.z).equals(chunkCoord)) // paticleData for all branches of the structure is stored, make sure the modData is in this chunk
				{
					result.add(particleData);
				}
			}
		}
    	return result;
    }

    public void removeParticles(ChunkCoordinate chunkCoord, ParticleFunction<?> particle)
    {
		CustomStructure customObject = world.getStructureCache().getChunkData(chunkCoord);
		if(customObject != null && customObject.particlesManager.particleData.contains(particle))
		{
			customObject.particlesManager.particleData.remove(particle);
		}
    }
    
	public boolean isInsidePregeneratedRegion(ChunkCoordinate chunk)
	{
		return
			!(
				// TODO: Make this prettier.
				// Cycle 0 for the pre-generator can mean 2 things:
				// 1. Nothing has been pre-generated.
				// 2. Only the spawn chunk has been generated.
				// The pre-generator actually skips spawning the center chunk at cycle 0 (is done automatically by MC anyway).
				getPregeneratedBorderLeft() == 0 &&
				getPregeneratedBorderRight() == 0 &&
				getPregeneratedBorderTop() == 0 &&
				getPregeneratedBorderBottom() == 0
			) &&
			(
				getPregenerationRadius() > 0 &&
				chunk.getChunkX() > getPreGeneratorCenterPoint().getChunkX() - getPregeneratedBorderLeft()
				&&
				chunk.getChunkX() <= getPreGeneratorCenterPoint().getChunkX() + getPregeneratedBorderRight()
				&&
				chunk.getChunkZ() > getPreGeneratorCenterPoint().getChunkZ() - getPregeneratedBorderTop()
				&&
				chunk.getChunkZ() <= getPreGeneratorCenterPoint().getChunkZ() + getPregeneratedBorderBottom()
			)
		;
	}

	public abstract boolean getPreGeneratorIsInitialised();
}