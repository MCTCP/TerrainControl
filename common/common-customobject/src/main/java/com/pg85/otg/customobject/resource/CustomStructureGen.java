package com.pg85.otg.customobject.resource;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.constants.SettingsEnums.CustomStructureType;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo3.BO3;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.customobject.structures.StructuredCustomObject;
import com.pg85.otg.customobject.structures.bo3.BO3CustomStructure;
import com.pg85.otg.customobject.structures.bo3.BO3CustomStructureCoordinate;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.ICustomObjectManager;
import com.pg85.otg.util.interfaces.ICustomObjectResourcesManager;
import com.pg85.otg.util.interfaces.ICustomStructureGen;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IStructuredCustomObject;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomStructureGen extends CustomObjectResource implements ICustomStructureGen
{
    private List<Double> objectChances;
    public List<String> objectNames;

    public CustomStructureGen(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
    {
        super(biomeConfig, args, logger, materialReader);
        objectNames = new ArrayList<String>();
        objectChances = new ArrayList<Double>();
        for (int i = 0; i < args.size() - 1; i += 2)
        {
            objectNames.add(args.get(i));
            objectChances.add(readRarity(args.get(i + 1)));
        }

        biomeConfig.setStructureGen(this);
    }

    @Override
	public Double getObjectChance(int i)
    {
    	return this.objectChances.size() < i + 1 ? null : this.objectChances.get(i);
    }
    
    @Override
	public String getObjectName(int i)
	{
    	return this.objectNames.size() < i + 1 ? null : this.objectNames.get(i);
	}
    
    @Override
	public boolean isEmpty()
	{
		return this.objectNames.isEmpty();
	}
    
    @Override
    public List<IStructuredCustomObject> getObjects(String presetFolderName, Path otgRootFolder, boolean spawnLog, ILogger logger, ICustomObjectManager customObjectManager, IMaterialReader materialReader, ICustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
    	List<IStructuredCustomObject> objects = new ArrayList<>();
    	if(!objectNames.isEmpty())
    	{
            for (int i = 0; i < objectNames.size(); i ++)
            {
            	// TODO: Re-wire this so we don't have to cast CustomObjectManager/CustomObjectResourcesManager :(
            	// TODO: Remove any dependency on common-customobjects, interfaces only?
            	CustomObject object = ((CustomObjectManager)customObjectManager).getGlobalObjects().getObjectByName(objectNames.get(i), presetFolderName, otgRootFolder, spawnLog, logger, (CustomObjectManager)customObjectManager, materialReader, (CustomObjectResourcesManager)manager, modLoadedChecker);
            	objects.add((StructuredCustomObject) object);
            }
    	}
    	return objects;
    }

    @Override
    public void spawn(IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
    {
        // Left blank, as spawnInChunk already handles this.
    }
    
    @Override
    public void process(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, ChunkCoordinate chunkBeingPopulated, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
    	// Don't process BO4's, they're plotted and spawned separately from other resources.
    	if(worldGenRegion.getWorldConfig().getCustomStructureType() != CustomStructureType.BO4)
    	{
    		super.process(structureCache, worldGenRegion, random, villageInChunk, chunkBeingPopulated, otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
    	}
    }

    // Only used for BO3 CustomStructure
    @Override
    protected void spawnInChunk(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, ChunkCoordinate chunkBeingPopulated, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
        // Find all structures that reach this chunk, and spawn them
        int searchRadius = worldGenRegion.getWorldConfig().getMaximumCustomStructureRadius();

        int currentChunkX = chunkBeingPopulated.getChunkX();
        int currentChunkZ = chunkBeingPopulated.getChunkZ();
        for (int searchChunkX = currentChunkX - searchRadius; searchChunkX < currentChunkX + searchRadius; searchChunkX++)
        {
            for (int searchChunkZ = currentChunkZ - searchRadius; searchChunkZ < currentChunkZ + searchRadius; searchChunkZ++)
            {
            	BO3CustomStructure structureStart = structureCache.getBo3StructureStart(worldGenRegion, random, searchChunkX, searchChunkZ, otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
                if (structureStart != null)
                {
                	structureStart.spawnInChunk(structureCache, worldGenRegion, chunkBeingPopulated, otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
                }
            }
        }
    }

    public BO3CustomStructureCoordinate getRandomObjectCoordinate(IWorldGenRegion worldGenRegion, Random random, int chunkX, int chunkZ, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
        if (objectNames.isEmpty())
        {
            return null;
        }
        for (int objectNumber = 0; objectNumber < getObjects(worldGenRegion.getPresetFolderName(), otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker).size(); objectNumber++)
        {
            if (random.nextDouble() * 100.0 < objectChances.get(objectNumber))
            {
            	IStructuredCustomObject object = getObjects(worldGenRegion.getPresetFolderName(), otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker).get(objectNumber);
            	if(object != null && object instanceof BO3) // TODO: How could a BO4 end up here? seen it happen once..
            	{
            		return (BO3CustomStructureCoordinate)((BO3)object).makeCustomStructureCoordinate(worldGenRegion.getPresetFolderName(), random, chunkX, chunkZ);
            	} else {
            		if(spawnLog)
            		{
            			IBiomeConfig biomeConfig = worldGenRegion.getBiomeConfig(chunkX * 16 + 15, chunkZ * 16 + 15);
            			logger.log(LogMarker.WARN, "Error: Could not find BO3 for CustomStructure in biome " + biomeConfig.getName() + ". BO3: " + objectNames.get(objectNumber));
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
    public boolean isAnalogousTo(ConfigFunction<IBiomeConfig> other, ILogger logger)
    {
        if (getClass() == other.getClass()){
            try {
                CustomStructureGen otherO = (CustomStructureGen) other;
                return otherO.objectNames.size() == this.objectNames.size() && otherO.objectNames.containsAll(this.objectNames);
            }
            catch (Exception ex)
            {
            	logger.log(LogMarker.WARN, ex.getMessage());
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
		{
			return false;
		}
		if (other == null)
		{
			return false;
		}
		if (other == this)
		{
			return true;
		}
		if (getClass() != other.getClass())
		{
			return false;
		}
		final CustomStructureGen compare = (CustomStructureGen) other;
		return 
			(
				this.objectChances == null ? 
				this.objectChances == compare.objectChances
				: this.objectChances.equals(compare.objectChances)
			) && (
				this.objectNames == null ? 
				this.objectNames == compare.objectNames
				: this.objectNames.equals(compare.objectNames)
			)
		;
    }

    @Override
    public int getPriority()
    {
        return -41;
    }
}
