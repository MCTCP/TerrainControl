package com.pg85.otg.customobjects.structures.bo4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.pg85.otg.customobjects.structures.bo4.smoothing.SmoothingAreaLine;
import com.pg85.otg.util.ChunkCoordinate;

public class CustomStructurePlaceHolder extends BO4CustomStructure
{
    public CustomStructurePlaceHolder(long worldSeed, BO4CustomStructureCoordinate structureStart, Map<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>> objectsToSpawn, Map<ChunkCoordinate, ArrayList<SmoothingAreaLine>> smoothingAreasToSpawn, int minY)
    {
    	super(worldSeed, structureStart, objectsToSpawn, smoothingAreasToSpawn, minY);
    }

	public void mergeWithCustomStructure(BO4CustomStructure structure)
	{		
		structure.objectsToSpawn.putAll(this.objectsToSpawn);
		
		Map<ChunkCoordinate, ArrayList<SmoothingAreaLine>> mergedSmoothingAreas = new HashMap<ChunkCoordinate, ArrayList<SmoothingAreaLine>>();
		mergedSmoothingAreas.putAll(structure.smoothingAreaManager.smoothingAreasToSpawn);
		mergedSmoothingAreas.putAll(this.smoothingAreaManager.smoothingAreasToSpawn);
		structure.smoothingAreaManager.fillSmoothingLineCaches(mergedSmoothingAreas);
		
		structure.modDataManager.modData.addAll(this.modDataManager.modData);
		structure.particlesManager.particleData.addAll(this.particlesManager.particleData);
		structure.spawnerManager.spawnerData.addAll(this.spawnerManager.spawnerData);		
	}
}
