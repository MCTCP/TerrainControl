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
    
    @Override
    public boolean equals(Object other)
    {
    	return 
    		other != null &&
    		(
				this == other ||
				(
					other instanceof CustomStructure &&
					this.start != null &&
					((CustomStructure)other).start != null &&
					((CustomStructure)other).start.bo3Name.equals(this.start.bo3Name) &&
					((CustomStructure)other).start.getX() == this.start.getX() &&
					((CustomStructure)other).start.getY() == this.start.getY() &&
					((CustomStructure)other).start.getZ() == this.start.getZ()
				)
			)
		;
    }
    
    @Override
    public int hashCode()
    {    	
        final int prime = 31;
	    int result = 1;
	    result = prime * result + ((this.start.bo3Name == null) ? 0 : this.start.bo3Name.hashCode());
	    result = prime * result + (int) (this.start.getX() ^ (this.start.getX() >>> 32));
	    result = prime * result + (int) (this.start.getY() ^ (this.start.getY() >>> 32));
	    result = prime * result + (int) (this.start.getZ() ^ (this.start.getZ() >>> 32));
	    return result;
    }
}
