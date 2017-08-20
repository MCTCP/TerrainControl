package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.BoundingBox;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.Rotation;
import com.khorn.terraincontrol.util.helpers.MathHelper;
import com.khorn.terraincontrol.customobjects.bo3.BO3;

import java.util.Random;

/**
 * Represents an object along with its location in the world.
 */
public class CustomObjectCoordinate
{
	// OTG+
	
	public boolean isSpawned;
	public boolean isBranch;
	public int branchDepth;
	public boolean isRequiredBranch;
	String BO3Name;
	String StartBO3Name; // Is filled in when its needed after the caller calls the constructor (todo, change that, ugly!)
	
    public final int getChunkX(){ return (int)MathHelper.floor(x / (double)16); }
    public final int getChunkZ(){ return (int)MathHelper.floor(z / (double)16); }
	
    public LocalWorld World;
    
    public CustomObjectCoordinate(LocalWorld world, CustomObject object, String customObjectName, Rotation rotation, int x, int y, int z, boolean isBranch, int branchDepth, boolean isRequiredBranch)
    {    	  
    	World = world;
    	
    	BO3Name = object != null ? object.getName() : customObjectName != null && customObjectName.length() > 0 ? customObjectName : null;
    	
		if(object != null && ((BO3)object).getSettings() == null)
		{
			throw new RuntimeException();
		}
    	
        this.object = object;
                
        this.rotation = rotation != null ? rotation : Rotation.NORTH;
        this.x = x;        
        this.y = y;
        if(y >= TerrainControl.WORLD_HEIGHT)
        {
        	throw new RuntimeException();
        }
        
        this.z = z;
        this.isBranch = isBranch;
        this.branchDepth = branchDepth;
        this.isRequiredBranch = isRequiredBranch;
    }    
    
    /**
     * Returns the object of this coordinate.
     * 
     * @return The object.
     */
    public CustomObject getObject()
    {    	
    	if(object == null)
    	{   		
    		if(World == null)
    		{
    			throw new RuntimeException();
    		}
    		    		
    		object = TerrainControl.getCustomObjectManager().getGlobalObjects().getObjectByName(BO3Name, World.getName());
    		
    		if(object == null)
    		{
    			TerrainControl.log(LogMarker.ERROR, "Could not find BO2/BO3 " + BO3Name + " in GlobalObjects or WorldObjects directory.");
    			//throw new NotImplementedException();
    		}
			BO3Name = object != null ? object.getName() : BO3Name;
    		
    		if(object != null && ((BO3)object).getSettings() == null)
    		{
    			throw new RuntimeException();
    		}   	
    		
    		return object;
    	}
    	
        return object;
    }    
    
    // This should only be used for OTG+ CustomStructure
    public boolean spawnWithChecks(ChunkCoordinate chunkCoord, LocalWorld world, Random random, String replaceAbove, String replaceBelow, boolean replaceWithBiomeBlocks, String replaceWithSurfaceBlock, String replaceWithGroundBlock, boolean spawnUnderWater, int waterLevel, boolean isStructureAtSpawn, boolean doReplaceAboveBelowOnly)
    {
    	if(getObject() == null)
    	{
    		throw new RuntimeException();
    	}
        if(getObject() instanceof BO3)
        {        	
        	return ((BO3)getObject()).trySpawnAt(world, random, rotation, chunkCoord, x, y, z, replaceAbove, replaceBelow, replaceWithBiomeBlocks, replaceWithSurfaceBlock, replaceWithGroundBlock, spawnUnderWater, waterLevel, isStructureAtSpawn, doReplaceAboveBelowOnly);
        } else {
        	throw new RuntimeException();
        }   	
    }    
    
	/**
	 * Returns the object of this coordinate, casted to a
	 * StructuredCustomObject. Will throw a ClassCastExcpetion
	 * if the object isn't a StructuredCustomObject
	 * 
	 * @return The casted object.
	*/
    public StructuredCustomObject getStructuredObject()
    {
    	return (StructuredCustomObject)getObject();
    }
    
	//
	
    private transient CustomObject object;
    Rotation rotation;
    int x;
    int y;
    int z;

    public CustomObjectCoordinate(LocalWorld world, CustomObject object, String customObjectName, Rotation rotation, int x, int y, int z)
    {
    	this.World = world;
        this.object = object;
        
        BO3Name = object != null ? object.getName() : customObjectName != null && customObjectName.length() > 0 ? customObjectName : null;
        
        if(BO3Name == null)
        {
        	throw new RuntimeException();
        }
        
        this.rotation = rotation;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getZ()
    {
        return z;
    }

    public Rotation getRotation()
    {
        return rotation;
    }

    // This should only be used for OTG CustomStructure
    boolean spawnWithChecks(CustomObjectStructure structure, LocalWorld world, StructurePartSpawnHeight height, Random random)
    {
        return ((BO3)object).trySpawnAt(false, structure, world, random, rotation, x, height.getCorrectY(world, x, this.y, z), z);
    }

    @Override
    public boolean equals(Object otherObject)
    {
        if (otherObject == null)
        {
            return false;
        }
        if (!(otherObject instanceof CustomObjectCoordinate))
        {
            return false;
        }
        CustomObjectCoordinate otherCoord = (CustomObjectCoordinate) otherObject;
        if (otherCoord.x != x)
        {
            return false;
        }
        if (otherCoord.y != y)
        {
            return false;
        }
        if (otherCoord.z != z)
        {
            return false;
        }
        if (!otherCoord.rotation.equals(rotation))
        {
            return false;
        }
        if (!otherCoord.object.getName().equals(object.getName()))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        return (x >> 13) ^ (y >> 7) ^ z ^ object.getName().hashCode() ^ rotation.toString().hashCode();
    }

    /**
     * Gets the chunk that should populate for this object.
     * @return The chunk.
     */
    public ChunkCoordinate getPopulatingChunk()
    {
        // In the past we simply returned the chunk populating for the origin
        // of the object. However, the origin is not guaranteed to be at the
        // center of the object. We need to know the exact center to choose
        // the appropriate spawning chunk.

    	CustomObject object = getObject();
    	if(object == null)
    	{
    		return null;
    	}
    	
        BoundingBox box = object.getBoundingBox(rotation);
        int centerX = x + box.getMinX() + (box.getWidth() / 2);
        int centerZ = z + box.getMinZ() + (box.getDepth() / 2);

        return ChunkCoordinate.getPopulatingChunk(centerX, centerZ);
    }
}
