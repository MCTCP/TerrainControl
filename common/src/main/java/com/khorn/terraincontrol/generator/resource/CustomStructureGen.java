package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.CustomObjectCoordinate;
import com.khorn.terraincontrol.customobjects.CustomObjectStructure;
import com.khorn.terraincontrol.customobjects.StructuredCustomObject;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.ChunkCoordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomStructureGen extends Resource
{
	// OTG+
	
    public List<StructuredCustomObject> getObjects(String worldName)
    {
    	if(objects.isEmpty() && !objectNames.isEmpty())
    	{
            for (int i = 0; i < objectNames.size(); i ++)
            {
                //CustomObject object = TerrainControl.getCustomObjectManager().getGlobalObjects().parseCustomObject(objectNames.get(i), worldName);
            	CustomObject object = TerrainControl.getCustomObjectManager().getGlobalObjects().getObjectByName(objectNames.get(i), worldName);
            	objects.add((StructuredCustomObject) object);
            }
    	}
    	return objects;
    }

	//
	
    private List<StructuredCustomObject> objects;
    public List<Double> objectChances;
    public List<String> objectNames;

    public CustomStructureGen(BiomeConfig biomeConfig, List<String> args) throws InvalidConfigException
    {
        super(biomeConfig);
        objects = new ArrayList<StructuredCustomObject>();
        objectNames = new ArrayList<String>();
        objectChances = new ArrayList<Double>();
        for (int i = 0; i < args.size() - 1; i += 2)
        {
            objectNames.add(args.get(i));
            objectChances.add(readRarity(args.get(i + 1)));
        }

        getHolder().structureGen = this;
    }

    @Override
    public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z)
    {
        // Left blank, as spawnInChunk(..) already handles this.
    }

    // Only used for OTG CustomStructure
    @Override
    protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
    	if(world.getConfigs().getWorldConfig().IsOTGPlus)
    	{
    		throw new RuntimeException();
    	} else {
	        // Find all structures that reach this chunk, and spawn them
	        int searchRadius = world.getConfigs().getWorldConfig().maximumCustomStructureRadius;
	
	        int currentChunkX = chunkCoord.getChunkX();
	        int currentChunkZ = chunkCoord.getChunkZ();
	        for (int searchChunkX = currentChunkX - searchRadius; searchChunkX < currentChunkX + searchRadius; searchChunkX++)
	        {
	            for (int searchChunkZ = currentChunkZ - searchRadius; searchChunkZ < currentChunkZ + searchRadius; searchChunkZ++)
	            {
	                CustomObjectStructure structureStart = world.getStructureCache().getStructureStart(searchChunkX, searchChunkZ);
	                if (structureStart != null)
	                {
	                    structureStart.spawnForChunk(chunkCoord);
	                }
	            }
	        }
    	}
    }

    @Override
    public String toString()
    {
        if (objectNames.isEmpty())
        {
            return "CustomStructure()";
        }
        String output = "CustomStructure(" + objectNames.get(0) + "," + objectChances.get(0);
        for (int i = 1; i < objectNames.size(); i++)
        {
            output += "," + objectNames.get(i) + "," + objectChances.get(i);
        }
        return output + ")";
    }

    public CustomObjectCoordinate getRandomObjectCoordinate(LocalWorld world, Random random, int chunkX, int chunkZ)
    {
        if (objectNames.isEmpty())
        {
            return null;
        }        
        for (int objectNumber = 0; objectNumber < getObjects(world.getName()).size(); objectNumber++)
        {
            if (random.nextDouble() * 100.0 < objectChances.get(objectNumber))
            {
            	StructuredCustomObject object = getObjects(world.getName()).get(objectNumber);
            	if(object != null)
            	{
            		return object.makeCustomObjectCoordinate(world, random, chunkX, chunkZ);
            	} else {
            		if(TerrainControl.getPluginConfig().SpawnLog)
            		{
            			BiomeConfig biomeConfig = world.getBiome(chunkX * 16 + 15, chunkZ * 16 + 15).getBiomeConfig();
            			TerrainControl.log(LogMarker.WARN, "Error: Could not find BO3 for CustomStructure in biome " + biomeConfig.getName() + ". BO3: " + objectNames.get(objectNumber));
            		}
            	}
            }
        }
        return null;
    }

    @Override
    public boolean isAnalogousTo(ConfigFunction<BiomeConfig> other)
    {
        if (getClass() == other.getClass()){
            try {
                CustomStructureGen otherO = (CustomStructureGen) other;
                return otherO.objectNames.size() == this.objectNames.size() && otherO.objectNames.containsAll(this.objectNames);
            } catch (Exception ex){
                TerrainControl.log(LogMarker.WARN, ex.getMessage());
            }
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 61 * hash + super.hashCode();
        hash = 61 * hash + (this.objects != null ? this.objects.hashCode() : 0);
        hash = 61 * hash + (this.objectChances != null ? this.objectChances.hashCode() : 0);
        hash = 61 * hash + (this.objectNames != null ? this.objectNames.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object other)
    {
        if (!super.equals(other))
            return false;
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (getClass() != other.getClass())
            return false;
        final CustomStructureGen compare = (CustomStructureGen) other;
        return (this.objects == null ? this.objects == compare.objects
                : this.objects.equals(compare.objects))
               && (this.objectChances == null ? this.objectChances == compare.objectChances
                   : this.objectChances.equals(compare.objectChances))
               && (this.objectNames == null ? this.objectNames == compare.objectNames
                   : this.objectNames.equals(compare.objectNames));
    }

    @Override
    public int getPriority()
    {
        return -41;
    }

}
