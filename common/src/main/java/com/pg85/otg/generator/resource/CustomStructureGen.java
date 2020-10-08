package com.pg85.otg.generator.resource;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.ConfigFunction;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.customobjects.bo3.BO3;
import com.pg85.otg.customobjects.structures.StructuredCustomObject;
import com.pg85.otg.customobjects.structures.bo3.BO3CustomStructure;
import com.pg85.otg.customobjects.structures.bo3.BO3CustomStructureCoordinate;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomStructureGen extends Resource
{
    public List<Double> objectChances;
    public List<String> objectNames;

    public CustomStructureGen(BiomeConfig biomeConfig, List<String> args) throws InvalidConfigException
    {
        super(biomeConfig);
        objectNames = new ArrayList<String>();
        objectChances = new ArrayList<Double>();
        for (int i = 0; i < args.size() - 1; i += 2)
        {
            objectNames.add(args.get(i));
            objectChances.add(readRarity(args.get(i + 1)));
        }

        getHolder().structureGen = this;
    }
    
    public List<StructuredCustomObject> getObjects(String worldName)
    {
    	List<StructuredCustomObject> objects = new ArrayList<StructuredCustomObject>();
    	if(!objectNames.isEmpty())
    	{
            for (int i = 0; i < objectNames.size(); i ++)
            {
            	CustomObject object = OTG.getCustomObjectManager().getGlobalObjects().getObjectByName(objectNames.get(i), worldName);
            	objects.add((StructuredCustomObject) object);
            }
    	}
    	return objects;
    }

    @Override
    public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
    {
        // Left blank, as spawnInChunk already handles this.
    }

    // Only used for BO3 CustomStructure
    @Override
    protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
        // Find all structures that reach this chunk, and spawn them
        int searchRadius = world.getConfigs().getWorldConfig().maximumCustomStructureRadius;

        int currentChunkX = chunkCoord.getChunkX();
        int currentChunkZ = chunkCoord.getChunkZ();
        for (int searchChunkX = currentChunkX - searchRadius; searchChunkX < currentChunkX + searchRadius; searchChunkX++)
        {
            for (int searchChunkZ = currentChunkZ - searchRadius; searchChunkZ < currentChunkZ + searchRadius; searchChunkZ++)
            {
            	BO3CustomStructure structureStart = world.getStructureCache().getBo3StructureStart(random, searchChunkX, searchChunkZ);
                if (structureStart != null)
                {
                	structureStart.spawnInChunk(chunkCoord, world);
                }
            }
        }
    }

    public BO3CustomStructureCoordinate getRandomObjectCoordinate(LocalWorld world, Random random, int chunkX, int chunkZ)
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
            	if(object != null && object instanceof BO3) // TODO: How could a BO4 end up here? seen it happen once..
            	{
            		return (BO3CustomStructureCoordinate)((BO3)object).makeCustomStructureCoordinate(world, random, chunkX, chunkZ);
            	} else {
            		if(OTG.getPluginConfig().spawnLog)
            		{
            			BiomeConfig biomeConfig = world.getBiome(chunkX * 16 + 15, chunkZ * 16 + 15).getBiomeConfig();
            			OTG.log(LogMarker.WARN, "Error: Could not find BO3 for CustomStructure in biome " + biomeConfig.getName() + ". BO3: " + objectNames.get(objectNumber));
            		}
            	}
            }
        }
        return null;
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

    @Override
    public boolean isAnalogousTo(ConfigFunction<BiomeConfig> other)
    {
        if (getClass() == other.getClass()){
            try {
                CustomStructureGen otherO = (CustomStructureGen) other;
                return otherO.objectNames.size() == this.objectNames.size() && otherO.objectNames.containsAll(this.objectNames);
            }
            catch (Exception ex)
            {
                OTG.log(LogMarker.WARN, ex.getMessage());
            }
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 61 * hash + super.hashCode();
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
        return (this.objectChances == null ? this.objectChances == compare.objectChances
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
