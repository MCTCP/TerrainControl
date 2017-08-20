package com.khorn.terraincontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.khorn.terraincontrol.customobjects.CustomObjectStructure;
import com.khorn.terraincontrol.customobjects.bo3.BO3;
import com.khorn.terraincontrol.customobjects.bo3.ModDataFunction;
import com.khorn.terraincontrol.customobjects.bo3.ParticleFunction;
import com.khorn.terraincontrol.customobjects.bo3.SpawnerFunction;
import com.khorn.terraincontrol.util.ChunkCoordinate;

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
    	for(Entry<ChunkCoordinate, CustomObjectStructure> entry : world.getStructureCache().worldInfoChunks.entrySet())
    	{   		   		
			// if the player is in range
			if(playerChunk.equals(entry.getKey()))
			{
	    		if(entry.getValue() != null)
	    		{
	    			structureInfo += "-- BO3 Info -- \r\nName: " + ((BO3)entry.getValue().Start.getObject()).getSettings().getName().replace("Start", "") + "\r\nAuthor: " + ((BO3)entry.getValue().Start.getObject()).getSettings().author + "\r\nDescription: " + ((BO3)entry.getValue().Start.getObject()).getSettings().description;
	    			String branchesInChunk = entry.getValue().ObjectsToSpawnInfo.get(entry.getKey());
	    			if(branchesInChunk != null && branchesInChunk.length() > 0)
	    			{
	    				structureInfo += "\r\n" + branchesInChunk;
	    			}
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
	    	for(Entry<ChunkCoordinate, CustomObjectStructure> entry : world.getStructureCache().worldInfoChunks.entrySet())
	    	{
				if(chunkCoord.equals(entry.getKey()))
				{
		    		if(entry.getValue() != null)
		    		{
		    			for(ModDataFunction modData : entry.getValue().modData)
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
		    		}
		    		bFound = true;
				}
	    	}
	    	if(!bFound)
	    	{	    		
	    		if(!world.IsInsidePregeneratedRegion(chunkCoord, true) && (!world.getConfigs().getWorldConfig().IsOTGPlus || !world.getStructureCache().structureCache.containsKey(chunkCoord)))
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
	    	for(Entry<ChunkCoordinate, CustomObjectStructure> entry : world.getStructureCache().worldInfoChunks.entrySet())
	    	{
				if(chunkCoord.equals(entry.getKey()))
				{
		    		if(entry.getValue() != null)
		    		{
		    			for(SpawnerFunction spawnerData : entry.getValue().spawnerData)
		    			{
		    				if(ChunkCoordinate.fromBlockCoords(spawnerData.x, spawnerData.z).equals(chunkCoord)) // spawnerData for all branches of the structure is stored, make sure the modData is in this chunk
		    				{
		    					result.add(spawnerData);
		    				}
		    			}   				
		    		}
		    		bFound = true;
				}
	    	}
	    	if(!bFound)
	    	{	    		
	    		if(!world.IsInsidePregeneratedRegion(chunkCoord, true) && (!world.getConfigs().getWorldConfig().IsOTGPlus || !world.getStructureCache().structureCache.containsKey(chunkCoord)))
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
	    	for(Entry<ChunkCoordinate, CustomObjectStructure> entry : world.getStructureCache().worldInfoChunks.entrySet())
	    	{
				if(chunkCoord.equals(entry.getKey()))
				{
		    		if(entry.getValue() != null)
		    		{
		    			for(ParticleFunction particleData : entry.getValue().particleData)
		    			{
		    				if(ChunkCoordinate.fromBlockCoords(particleData.x, particleData.z).equals(chunkCoord)) // paticleData for all branches of the structure is stored, make sure the modData is in this chunk
		    				{
		    					result.add(particleData);
		    				}
		    			}   				
		    		}
		    		bFound = true;
				}
	    	}
	    	if(!bFound)
	    	{	    		
	    		if(!world.IsInsidePregeneratedRegion(chunkCoord, true) && (!world.getConfigs().getWorldConfig().IsOTGPlus || !world.getStructureCache().structureCache.containsKey(chunkCoord)))
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
    		if(customObject != null && customObject.particleData.contains(particle))
    		{
    			customObject.particleData.remove(particle);
    		}
    	}
    }
}