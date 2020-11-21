package com.pg85.otg.customobject.resource;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IPresetNameProvider;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomObjectGen extends CustomObjectResource
{	
    private List<CustomObject> objects;
    private List<String> objectNames;

    public CustomObjectGen(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
    {
        super(biomeConfig, args, logger, materialReader);
        if (args.isEmpty() || (args.size() == 1 && args.get(0).trim().isEmpty()))
        {
            // Backwards compatibility
            args = new ArrayList<String>();
            args.add("UseWorld");
        }
        objects = new ArrayList<CustomObject>();
        objectNames = new ArrayList<String>();
        for (String arg : args)
        {
            objectNames.add(arg);
        }
    }
    
    private List<CustomObject> getObjects(String worldName, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IPresetNameProvider presetNameProvider, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
    	if(objects.isEmpty() && !objectNames.isEmpty())
    	{
            for (int i = 0; i < objectNames.size(); i ++)
            {
            	CustomObject object = customObjectManager.getGlobalObjects().getObjectByName(objectNames.get(i), worldName, otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker);
            	objects.add(object);	              	
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
    protected void spawnInChunk(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, ChunkCoordinate chunkBeingPopulated, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IPresetNameProvider presetNameProvider, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
        for (CustomObject object : getObjects(worldGenRegion.getWorldName(), otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker))
        {
        	if(object != null) // if null then BO2/BO3 file could not be found
        	{
        		object.process(structureCache, worldGenRegion, random, chunkBeingPopulated);
        	}
        }
    }

    @Override
    public String toString()
    {
        return "CustomObject(" + StringHelper.join(objectNames, ",") + ")";
    }

    @Override
    public boolean isAnalogousTo(ConfigFunction<IBiomeConfig> other, ILogger logger)
    {
        if (getClass() == other.getClass())
        {
            try
            {
                CustomObjectGen otherO = (CustomObjectGen) other;
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
        int hash = 5;
        hash = 41 * hash + super.hashCode();
        hash = 41 * hash + (this.objects != null ? this.objects.hashCode() : 0);
        hash = 41 * hash + (this.objectNames != null ? this.objectNames.hashCode() : 0);
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
    	final CustomObjectGen compare = (CustomObjectGen) other;
    	return 
			(
				this.objects == null ? 
				this.objects == compare.objects
				: this.objects.equals(compare.objects)
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
        return -40;
    }    
}
