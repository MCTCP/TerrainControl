package com.pg85.otg.customobject.structures.bo3;

import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo3.BO3;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.Branch;
import com.pg85.otg.customobject.structures.CustomStructure;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.customobject.structures.CustomStructureCoordinate;
import com.pg85.otg.customobject.structures.StructuredCustomObject;
import com.pg85.otg.customobject.util.BO3Enums.SpawnHeightEnum;
import com.pg85.otg.interfaces.ICustomObject;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.interfaces.IStructuredCustomObject;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import java.nio.file.Path;
import java.util.*;

/**
 * Represents a collection of all {@link CustomObject}s in a structure. It is
 * calculated by finding the branches of one object, then finding the branches
 * of those branches, etc., until
 * {@link BO3#getMaxBranchDepth()} is reached.
 *
 */
public class BO3CustomStructure extends CustomStructure
{
	private SpawnHeightEnum height;
	private int maxBranchDepth;
	
	public BO3CustomStructure(BO3CustomStructureCoordinate start)
	{
		this.start = start;
	}
	
	public BO3CustomStructure(IWorldGenRegion worldGenRegion, BO3CustomStructureCoordinate start, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		StructuredCustomObject object = (StructuredCustomObject)start.getObject(otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);

		if(object == null)
		{
			return;
		}
		if(!(object instanceof BO3))
		{
			if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
			{
				logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "BO3CustomStructure loaded with non-BO3 object " + object.getName());
			}
			return;
		}
		
		this.start = start;
		this.height = ((BO3)object).getStructurePartSpawnHeight();
		this.maxBranchDepth = ((BO3)object).getMaxBranchDepth();
		this.random = RandomHelper.getRandomForCoords(start.getX(), start.getY(), start.getZ(), worldGenRegion.getSeed());

		// Calculate all branches and add them to a list
		this.objectsToSpawn = new LinkedHashMap<ChunkCoordinate, Set<CustomStructureCoordinate>>();

		addToSpawnList((BO3CustomStructureCoordinate)start, object, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker); // Add the object itself
		addBranches((BO3CustomStructureCoordinate)start, 1, worldGenRegion, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
	}

	private void addBranches(BO3CustomStructureCoordinate coordObject, int depth, IWorldGenRegion worldGenRegion, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		IStructuredCustomObject object = coordObject.getObject(otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);

		if(object != null)
		{
			for (Branch branch : getBranches(object, coordObject.getRotation()))
			{
				// TODO: Does passing null as startbo3name work?
				BO3CustomStructureCoordinate childCoordObject = (BO3CustomStructureCoordinate)branch.toCustomObjectCoordinate(worldGenRegion.getPresetFolderName(), this.random, coordObject.getRotation(), coordObject.getX(), coordObject.getY(), coordObject.getZ(), null, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);

				// Don't add null objects
				if (childCoordObject == null)
				{
					continue;
				}

				// Add this object to the chunk
				addToSpawnList(childCoordObject, object, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
				
				// Also add the branches of this object
				if (depth < this.maxBranchDepth)
				{
					addBranches(childCoordObject, depth + 1, worldGenRegion, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
				}
			}
		}
	}

	private Branch[] getBranches(ICustomObject customObject, Rotation rotation)
	{
		return ((BO3)customObject).getBranches(rotation);
	}

	/**
	 * Adds the object to the spawn list of each chunk that the object
	 * touches.
	 * @param coordObject The object.
	 */
	private void addToSpawnList(BO3CustomStructureCoordinate coordObject, ICustomObject parent, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		ChunkCoordinate chunkCoordinate = coordObject.getDecoratingChunk(otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
		if(chunkCoordinate != null)
		{
			Set<CustomStructureCoordinate> objectsInChunk = this.objectsToSpawn.get(chunkCoordinate);
			if (objectsInChunk == null)
			{
				objectsInChunk = new LinkedHashSet<CustomStructureCoordinate>();
				this.objectsToSpawn.put(chunkCoordinate, objectsInChunk);
			}
			objectsInChunk.add(coordObject);
		} else {
			if(logger.getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
			{
				logger.log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Error reading branch in BO3 " + parent.getName()  + " Could not find BO3: " + coordObject.bo3Name);
			}
		}
	}

	public void spawnInChunk(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		Set<CustomStructureCoordinate> objectsInChunk = this.objectsToSpawn.get(worldGenRegion.getDecorationArea().getChunkBeingDecorated());
		if (objectsInChunk != null)
		{
			for (CustomStructureCoordinate coordObject : objectsInChunk)
			{
				BO3 bo3 = ((BO3)((BO3CustomStructureCoordinate)coordObject).getObject(otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker));
				bo3.trySpawnAt(this, structureCache, worldGenRegion, this.random, coordObject.rotation, coordObject.x, getCorrectY(worldGenRegion, coordObject.x, coordObject.y, coordObject.z), coordObject.z, bo3.getSettings().minHeight, bo3.getSettings().maxHeight, coordObject.y);
			}
		}
	}
	
	public int getCorrectY(IWorldGenRegion worldGenRegion, int x, int y, int z)
	{
		switch(this.height)
		{
			case randomY:
				return y;
			case highestBlock:
				return worldGenRegion.getHighestBlockAboveYAt(x, z);
			case highestSolidBlock:
				return worldGenRegion.getBlockAboveSolidHeight(x, z);
			default:
				return -1;
		}		
	}	
}
