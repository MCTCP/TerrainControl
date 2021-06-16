package com.pg85.otg.customobject.resource;

import com.pg85.otg.config.biome.ResourceBase;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomObjectResource extends ResourceBase implements ICustomObjectResource
{	
	private final List<CustomObject> objects;
	private final List<String> objectNames;

	public CustomObjectResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		if (args.isEmpty() || (args.size() == 1 && args.get(0).trim().isEmpty()))
		{
			// Backwards compatibility
			args = new ArrayList<String>();
			args.add("UseWorld");
		}
		this.objects = new ArrayList<CustomObject>();
		this.objectNames = new ArrayList<String>();
		for (String arg : args)
		{
			this.objectNames.add(arg);
		}
	}
	
	@Override
	public void spawnForChunkDecoration(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, ChunkCoordinate chunkBeingDecorated, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		for (CustomObject object : getObjects(worldGenRegion.getPresetFolderName(), otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker))
		{
			if(object != null) // if null then BO2/BO3 file could not be found
			{
				object.process(structureCache, worldGenRegion, random, chunkBeingDecorated);
			}
		}		
	}	
	
	private List<CustomObject> getObjects(String presetFolderName, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		if(this.objects.isEmpty() && !this.objectNames.isEmpty())
		{
			CustomObject object;
			for (int i = 0; i < this.objectNames.size(); i ++)
			{
				object = customObjectManager.getGlobalObjects().getObjectByName(this.objectNames.get(i), presetFolderName, otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
				this.objects.add(object);				  	
			}
		}
		return this.objects;
	}
	
	@Override
	public String toString()
	{
		return "CustomObject(" + StringHelper.join(this.objectNames, ",") + ")";
	}
}
