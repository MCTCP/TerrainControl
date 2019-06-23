package com.pg85.otg.common;

import java.util.ArrayList;
import java.util.HashMap;

import com.pg85.otg.customobjects.bo3.BO3;
import com.pg85.otg.customobjects.bo3.bo3function.ModDataFunction;
import com.pg85.otg.customobjects.bo3.bo3function.ParticleFunction;
import com.pg85.otg.customobjects.bo3.bo3function.SpawnerFunction;
import com.pg85.otg.customobjects.customstructure.CustomObjectStructure;
import com.pg85.otg.util.ChunkCoordinate;

// TODO: Implement this properly for spigot (maybe one day..)
public abstract class WorldSession
{
	protected LocalWorld world;

	public WorldSession(LocalWorld world)
	{
		this.world = world;
	}

	public abstract ArrayList<ParticleFunction> getParticleFunctions();

	public abstract int getWorldBorderRadius();
	public abstract ChunkCoordinate getWorldBorderCenterPoint();

	public abstract int getPregenerationRadius();
	public abstract int setPregenerationRadius(int value);

	public abstract ChunkCoordinate getPreGeneratorCenterPoint();

	public abstract int getPregeneratedBorderLeft();
	public abstract int getPregeneratedBorderRight();
	public abstract int getPregeneratedBorderTop();
	public abstract int getPregeneratedBorderBottom();

	public abstract boolean getPreGeneratorIsRunning();

    public String GetStructureInfoAt(double x, double z)
    {
    	String structureInfo = "";
		ChunkCoordinate playerChunk = ChunkCoordinate.fromBlockCoords((int)x, (int)z);
		// if the player is in range
		if(world.getStructureCache().worldInfoChunks.containsKey(playerChunk))
		{
			CustomObjectStructure worldInfoChunk = world.getStructureCache().worldInfoChunks.get(playerChunk);

			if(worldInfoChunk != null)
			{
    			structureInfo += "-- BO3 Info -- \r\nName: " + ((BO3)worldInfoChunk.Start.getObject()).getSettings().getName().replace("Start", "") + "\r\nAuthor: " + ((BO3)worldInfoChunk.Start.getObject()).getSettings().author + "\r\nDescription: " + ((BO3)worldInfoChunk.Start.getObject()).getSettings().description;
    			String branchesInChunk = worldInfoChunk.ObjectsToSpawnInfo.get(playerChunk);
    			if(branchesInChunk != null && branchesInChunk.length() > 0)
    			{
    				structureInfo += "\r\n" + branchesInChunk;
    			}
    		}
		}
    	return structureInfo;
    }

    public HashMap<String,ArrayList<ModDataFunction>> GetModDataForChunk(ChunkCoordinate chunkCoord)
    {
    	HashMap<String,ArrayList<ModDataFunction>> result = new HashMap<String,ArrayList<ModDataFunction>>();
    	boolean bFound = false;
    	if(world.IsInsideWorldBorder(chunkCoord, true))
    	{
    		if(world.getStructureCache().worldInfoChunks.containsKey(chunkCoord))
    		{
    			CustomObjectStructure worldInfoChunk = world.getStructureCache().worldInfoChunks.get(chunkCoord);

				if(worldInfoChunk != null)
	    		{
	    			for(ModDataFunction modData : worldInfoChunk.modDataManager.modData)
	    			{
	    				if(ChunkCoordinate.fromBlockCoords(modData.x, modData.z).equals(chunkCoord)) // modData for all branches of the structure is stored, make sure the modData is in this chunk
	    				{
		    				if(!result.containsKey(modData.modId))
		    				{
		    					result.put(modData.modId, new ArrayList<ModDataFunction>());
		    				}
	    					result.get(modData.modId).add(modData);
	    				}
	    			}
		    		bFound = true;
				}
	    	}
	    	if(!bFound)
	    	{
	    		if(!world.IsInsidePregeneratedRegion(chunkCoord) && (!world.getConfigs().getWorldConfig().IsOTGPlus || !world.getStructureCache().structureCache.containsKey(chunkCoord)))
	    		{
	    			result = null;
	    		}
	    	}
    	}
    	return result;
    }

    public ArrayList<SpawnerFunction> GetSpawnersForChunk(ChunkCoordinate chunkCoord)
    {
    	ArrayList<SpawnerFunction> result = new ArrayList<SpawnerFunction>();
    	boolean bFound = false;
    	if(world.IsInsideWorldBorder(chunkCoord, true))
    	{
    		if(world.getStructureCache().worldInfoChunks.containsKey(chunkCoord))
    		{
    			CustomObjectStructure worldInfoChunk = world.getStructureCache().worldInfoChunks.get(chunkCoord);

				if(worldInfoChunk != null)
	    		{
	    			for(SpawnerFunction spawnerData : worldInfoChunk.spawnerManager.spawnerData)
	    			{
	    				if(ChunkCoordinate.fromBlockCoords(spawnerData.x, spawnerData.z).equals(chunkCoord)) // spawnerData for all branches of the structure is stored, make sure the modData is in this chunk
	    				{
	    					result.add(spawnerData);
	    				}
	    			}
	    		}
	    		bFound = true;
			}
	    	if(!bFound)
	    	{
	    		if(!world.IsInsidePregeneratedRegion(chunkCoord) && (!world.getConfigs().getWorldConfig().IsOTGPlus || !world.getStructureCache().structureCache.containsKey(chunkCoord)))
	    		{
	    			result = null;
	    		}
	    	}
    	}
    	return result;
    }

    public ArrayList<ParticleFunction> GetParticlesForChunk(ChunkCoordinate chunkCoord)
    {
    	ArrayList<ParticleFunction> result = new ArrayList<ParticleFunction>();
    	boolean bFound = false;
    	if(world.IsInsideWorldBorder(chunkCoord, true))
    	{
    		if(world.getStructureCache().worldInfoChunks.containsKey(chunkCoord))
    		{
    			CustomObjectStructure worldInfoChunk = world.getStructureCache().worldInfoChunks.get(chunkCoord);

	    		if(worldInfoChunk != null)
	    		{
	    			for(ParticleFunction particleData : worldInfoChunk.particlesManager.particleData)
	    			{
	    				if(ChunkCoordinate.fromBlockCoords(particleData.x, particleData.z).equals(chunkCoord)) // paticleData for all branches of the structure is stored, make sure the modData is in this chunk
	    				{
	    					result.add(particleData);
	    				}
	    			}
	    		}
	    		bFound = true;
	    	}
	    	if(!bFound)
	    	{
	    		if(!world.IsInsidePregeneratedRegion(chunkCoord) && (!world.getConfigs().getWorldConfig().IsOTGPlus || !world.getStructureCache().structureCache.containsKey(chunkCoord)))
	    		{
	    			result = null;
	    		}
	    	}
    	}
    	return result;
    }

    public void RemoveParticles(ChunkCoordinate chunkCoord, ParticleFunction particle)
    {
    	if(world.IsInsideWorldBorder(chunkCoord, true))
    	{
    		CustomObjectStructure customObject = world.getStructureCache().worldInfoChunks.get(chunkCoord);
    		if(customObject != null && customObject.particlesManager.particleData.contains(particle))
    		{
    			customObject.particlesManager.particleData.remove(particle);
    		}
    	}
    }
}