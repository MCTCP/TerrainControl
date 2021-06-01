package com.pg85.otg.customobject.structures.bo4;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.bo4.smoothing.SmoothingAreaLine;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;

public class CustomStructurePlaceHolder extends BO4CustomStructure
{
    public CustomStructurePlaceHolder(long worldSeed, BO4CustomStructureCoordinate structureStart, Map<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>> objectsToSpawn, Map<ChunkCoordinate, ArrayList<SmoothingAreaLine>> smoothingAreasToSpawn, int minY, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
    	super(worldSeed, structureStart, objectsToSpawn, smoothingAreasToSpawn, minY, otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
    }

	public void mergeWithCustomStructure(BO4CustomStructure structure)
	{		
		structure.getObjectsToSpawn().putAll(this.getObjectsToSpawn());
		
		Map<ChunkCoordinate, ArrayList<SmoothingAreaLine>> mergedSmoothingAreas = new HashMap<ChunkCoordinate, ArrayList<SmoothingAreaLine>>();
		mergedSmoothingAreas.putAll(structure.getSmoothingAreaManager().smoothingAreasToSpawn);
		mergedSmoothingAreas.putAll(this.getSmoothingAreaManager().smoothingAreasToSpawn);
		structure.getSmoothingAreaManager().fillSmoothingLineCaches(mergedSmoothingAreas);
		
		structure.modDataManager.modData.addAll(this.modDataManager.modData);
		structure.particlesManager.particleData.addAll(this.particlesManager.particleData);
		structure.spawnerManager.spawnerData.addAll(this.spawnerManager.spawnerData);		
	}
}
