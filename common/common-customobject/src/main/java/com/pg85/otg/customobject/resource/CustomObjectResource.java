package com.pg85.otg.customobject.resource;

import com.pg85.otg.config.biome.BiomeResourceBase;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.helpers.StringHelper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomObjectResource extends BiomeResourceBase implements ICustomObjectResource
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
	public void spawnForChunkDecoration(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, Path otgRootFolder, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		for (CustomObject object : getObjects(worldGenRegion.getPresetFolderName(), otgRootFolder, worldGenRegion.getLogger(), customObjectManager, materialReader, manager, modLoadedChecker))
		{
			if(object != null) // if null then BO2/BO3 file could not be found
			{
				object.process(structureCache, worldGenRegion, random);
			}
		}
	}	
	
	private List<CustomObject> getObjects(String presetFolderName, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		if(this.objects.isEmpty() && !this.objectNames.isEmpty())
		{
			CustomObject object;
			for (int i = 0; i < this.objectNames.size(); i ++)
			{
				object = customObjectManager.getGlobalObjects().getObjectByName(this.objectNames.get(i), presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
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
