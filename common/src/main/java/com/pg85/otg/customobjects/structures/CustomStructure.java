package com.pg85.otg.customobjects.structures;

import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.util.ChunkCoordinate;
import java.util.*;

/**
 * Represents a collection of all {@link CustomObject}s in a structure. It is
 * calculated by finding the branches of one object, then finding the branches
 * of those branches, etc., until
 * {@link CustomObject#getMaxBranchDepth()} is reached.
 *
 */
public abstract class CustomStructure
{
    // The origin BO3 for this branching structure
    public CustomStructureCoordinate start;
    
    public EntitiesManager entitiesManager = new EntitiesManager();
	public ParticlesManager particlesManager = new ParticlesManager();
	public ModDataManager modDataManager = new ModDataManager();
	public SpawnerManager spawnerManager = new SpawnerManager();
	
    protected Map<ChunkCoordinate, Set<CustomStructureCoordinate>> objectsToSpawn;
    protected Random random;
    
    protected CustomStructure() { }    
}
