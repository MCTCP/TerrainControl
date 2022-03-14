package com.pg85.otg.customobject.resource;

import com.pg85.otg.config.biome.BiomeResourceBase;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.StructuredCustomObject;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ICustomObjectManager;
import com.pg85.otg.interfaces.ICustomObjectResourcesManager;
import com.pg85.otg.interfaces.ICustomStructureGen;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.interfaces.IStructuredCustomObject;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CustomStructureResource extends BiomeResourceBase implements ICustomStructureResource, ICustomStructureGen
{
	private final List<Double> objectChances;
	public final List<String> objectNames;

	public CustomStructureResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		this.objectNames = new ArrayList<String>();
		this.objectChances = new ArrayList<Double>();
		for (int i = 0; i < args.size() - 1; i += 2)
		{
			this.objectNames.add(args.get(i));
			this.objectChances.add(readRarity(args.get(i + 1)));
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
	public List<IStructuredCustomObject> getObjects(String presetFolderName, Path otgRootFolder, ILogger logger, ICustomObjectManager customObjectManager, IMaterialReader materialReader, ICustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		List<IStructuredCustomObject> objects = new ArrayList<>();
		if(!this.objectNames.isEmpty())
		{
			CustomObject object;
			for (int i = 0; i < this.objectNames.size(); i ++)
			{
				// TODO: Refactor this so we don't have to cast CustomObjectManager/CustomObjectResourcesManager :(
				// TODO: Remove any dependency on common-customobjects, interfaces only?
				object = ((CustomObjectManager)customObjectManager).getGlobalObjects().getObjectByName(objectNames.get(i), presetFolderName, otgRootFolder, logger, (CustomObjectManager)customObjectManager, materialReader, (CustomObjectResourcesManager)manager, modLoadedChecker);
				objects.add((StructuredCustomObject) object);
			}
		}
		return objects;
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
}
