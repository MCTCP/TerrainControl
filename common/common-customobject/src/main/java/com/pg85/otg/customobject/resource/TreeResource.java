package com.pg85.otg.customobject.resource;

import com.pg85.otg.config.biome.ResourceBase;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TreeResource extends ResourceBase implements ICustomObjectResource
{
	private final int frequency;
	private final List<Integer> treeChances;
	private final List<String> treeNames;
	private CustomObject[] treeObjects;
	private int[] treeObjectMinChances;
	private int[] treeObjectMaxChances;
	private boolean treesLoaded = false;

	public TreeResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(3, args);

		this.frequency = readInt(args.get(0), 1, 100);
		this.treeNames = new ArrayList<String>();
		this.treeChances = new ArrayList<Integer>();

		for (int i = 1; i < args.size() - 1; i += 2)
		{
			this.treeNames.add(args.get(i));
			this.treeChances.add(readInt(args.get(i + 1), 1, 100));
		}
	}	
	
	@Override
	public void spawnForChunkDecoration(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		loadTrees(worldGenRegion.getPresetFolderName(), otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);

		int x;
		int z;
		CustomObject tree;
		for (int i = 0; i < this.frequency; i++)
		{			
			for (int treeNumber = 0; treeNumber < this.treeNames.size(); treeNumber++)
			{							
				if (random.nextInt(100) < this.treeChances.get(treeNumber))
				{
					// TODO: Remove this offset for 1.16?
					x = worldGenRegion.getDecorationArea().getChunkBeingDecoratedCenterX() + random.nextInt(Constants.CHUNK_SIZE);
					z = worldGenRegion.getDecorationArea().getChunkBeingDecoratedCenterZ() + random.nextInt(Constants.CHUNK_SIZE);					
					
					tree = this.treeObjects[treeNumber];
					// Min/Max == -1 means use bo2/bo3 internal min/max height, otherwise use the optional min/max height defined with Tree()
					if(tree != null && tree.spawnAsTree(structureCache, worldGenRegion, random, x, z, this.treeObjectMinChances[treeNumber], this.treeObjectMaxChances[treeNumber]))
					{
						// Success!
						break;
					}
				}
			}
		}
	}
	
	// TODO: Could this cause problems for developer mode / flushcache, trees not updating during a session?
	private void loadTrees(String presetFolderName, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		if(!this.treesLoaded)
		{
			this.treesLoaded = true;
			
			this.treeObjects = new CustomObject[this.treeNames.size()];
			this.treeObjectMinChances = new int[this.treeNames.size()];
			this.treeObjectMaxChances = new int[this.treeNames.size()];
			
			String treeName;
			CustomObject tree;
			int minHeight;
			int maxHeight;
			String[] params;
			String sMinHeight;
			String sMaxHeight;
			for (int treeNumber = 0; treeNumber < this.treeNames.size(); treeNumber++)
			{
				treeName = this.treeNames.get(treeNumber);
				tree = null;
				minHeight = -1;
				maxHeight = -1;
	
				this.treeObjectMinChances[treeNumber] = minHeight;
				this.treeObjectMaxChances[treeNumber] = maxHeight;
				
				if(treeName.contains("("))
				{
					params = treeName.replace(")", "").split("\\(");
					treeName = params[0];
					tree = customObjectManager.getGlobalObjects().getObjectByName(treeName, presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
					this.treeObjects[treeNumber] = tree;				
					if(tree == null)
					{
						if(logger.getSpawnLogEnabled())
						{
							logger.log(LogMarker.WARN, "Error: Could not find BO3 for Tree, BO3: " + this.treeNames.get(treeNumber));
						}
						continue;
					}					
					
					params = params[1].split(";");
					sMinHeight = params[0].toLowerCase().replace("minheight=", "");
					sMaxHeight = params[1].toLowerCase().replace("maxheight=", "");				
					try
					{
						minHeight = Integer.parseInt(sMinHeight);
						maxHeight = Integer.parseInt(sMaxHeight);
						this.treeObjectMinChances[treeNumber] = minHeight;
						this.treeObjectMaxChances[treeNumber] = maxHeight;					
					} catch(NumberFormatException ex) {  }
				} else {
					tree = customObjectManager.getGlobalObjects().getObjectByName(treeName, presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);				
					this.treeObjects[treeNumber] = tree;
					if(tree == null)
					{
						if(logger.getSpawnLogEnabled())
						{
							logger.log(LogMarker.WARN, "Error: Could not find BO3 for Tree, BO3: " + this.treeNames.get(treeNumber));
						}
						continue;
					}
				}
			}
		}
	}

	@Override
	public String toString()
	{
		String output = "Tree(" + this.frequency;
		for (int i = 0; i < this.treeNames.size(); i++)
		{
			output += "," + this.treeNames.get(i) + "," + this.treeChances.get(i);
		}
		return output + ")";
	}	
}
