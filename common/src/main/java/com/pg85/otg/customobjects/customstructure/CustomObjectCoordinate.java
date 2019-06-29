package com.pg85.otg.customobjects.customstructure;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.BoundingBox;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.customobjects.bo3.BO3;
import com.pg85.otg.customobjects.bo3.StructurePartSpawnHeight;

import java.util.Random;

/**
 * Represents an object along with its location in the world.
 */
public class CustomObjectCoordinate
{
	// OTG
	
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
        	throw new RuntimeException(); // TODO: Remove this after testing
        }

        this.rotation = rotation;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
	// OTG+

	public boolean isSpawned;
	public boolean isBranch;
	public int branchDepth;
	public boolean isRequiredBranch;
	public boolean isWeightedBranch;
	public String branchGroup;
	String BO3Name;
	String StartBO3Name; // Is filled in when its needed after the caller calls the constructor (todo, change that, ugly!)

    public LocalWorld World;
	
    public final int getChunkX(){ return (int)MathHelper.floor(x / (double)16); }
    public final int getChunkZ(){ return (int)MathHelper.floor(z / (double)16); }

    public CustomObjectCoordinate(LocalWorld world, CustomObject object, String customObjectName, Rotation rotation, int x, int y, int z, boolean isBranch, int branchDepth, boolean isRequiredBranch, boolean isWeightedBranch, String branchGroup)
    {
    	World = world;

    	BO3Name = object != null ? object.getName() : customObjectName != null && customObjectName.length() > 0 ? customObjectName : null;

		if(object != null && ((BO3)object).getSettings() == null)
		{
			throw new RuntimeException(); // TODO: Remove this after testing
		}

        this.object = object;

        this.rotation = rotation != null ? rotation : Rotation.NORTH;
        this.x = x;
        this.y = y;
        if(y >= PluginStandardValues.WORLD_HEIGHT)
        {
        	throw new RuntimeException(); // TODO: Remove this after testing
        }

        this.z = z;
        this.isBranch = isBranch;
        this.branchDepth = branchDepth;

        this.isRequiredBranch = isRequiredBranch;
        this.isWeightedBranch = isWeightedBranch;
        this.branchGroup = branchGroup;
    }
 
    // Shared
    
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
    			throw new RuntimeException(); // TODO: Remove this after testing
    		}

    		object = OTG.getCustomObjectManager().getGlobalObjects().getObjectByName(BO3Name, World.getName());

    		if(object == null)
    		{
    			if(OTG.getPluginConfig().SpawnLog)
    			{
    				OTG.log(LogMarker.WARN, "Could not find BO2/BO3 " + BO3Name + " in GlobalObjects or WorldObjects directory.");
    			}
    		}
			BO3Name = object != null ? object.getName() : BO3Name;

    		if(object != null && ((BO3)object).getSettings() == null)
    		{
    			throw new RuntimeException(); // TODO: Remove this after testing
    		}

    		return object;
    	}

        return object;
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
    
    @Override
    public int hashCode()
    {
        return (x >> 13) ^ (y >> 7) ^ z ^ object.getName().hashCode() ^ rotation.toString().hashCode();
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
    
    /**
     * Same as getRotatedBO3Coords except it assumes that the minX=-8 maxX=7 minZ=-7 maxZ=8 coordinates have been
     * centered and justified inside chunk (aligned to fit between 0,0 and 15,15).
     */
    public static CustomObjectCoordinate getRotatedBO3CoordsJustified(int x, int y, int z, Rotation newRotation)
    {
    	int rotations = newRotation.getRotationId();
    	if(rotations < 0)
    	{
    		throw new RuntimeException(); // TODO: Remove this after testing
    		//rotations += 4;
    	}

    	int rotatedX = x;
    	int rotatedZ = z;

    	int newX = x;
    	int newZ = z;

    	for(int i = 0; i < rotations; i++)
    	{
    		newX = 15 - rotatedZ;
    		newZ = rotatedX;

    		rotatedX = newX;
    		rotatedZ = newZ;
    	}

    	return new CustomObjectCoordinate(null, null, null, newRotation, rotatedX, y, rotatedZ, false, 0, false, false, null);
    }
    
    public static CustomObjectCoordinate getRotatedCoord(int x, int y, int z, Rotation newRotation)
    {
    	int rotations = newRotation.getRotationId();
    	if(rotations < 0)
    	{
    		throw new RuntimeException(); // TODO: Remove this after testing
    		//rotations += 4;
    	}

    	int rotatedX = x;
    	int rotatedZ = z;

    	int newX = x;
    	int newZ = z;
    	for(int i = 0; i < rotations; i++)
    	{
    		newX = rotatedZ;
    		newZ = -rotatedX;
    		rotatedX = newX;
    		rotatedZ = newZ;
    	}

    	return new CustomObjectCoordinate(null, null, null, newRotation, rotatedX, y, rotatedZ, false, 0, false, false, null);
    }    
    
    // OTG

    boolean spawnWithChecks(CustomObjectStructure structure, LocalWorld world, StructurePartSpawnHeight height, Random random)
    {
        return ((BO3)object).trySpawnAt(false, structure, world, random, rotation, x, height.getCorrectY(world, x, this.y, z), z);
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

    // OTG+
    
    public boolean spawnWithChecks(ChunkCoordinate chunkCoord, LocalWorld world, Random random, String replaceAbove, String replaceBelow, boolean replaceWithBiomeBlocks, String replaceWithSurfaceBlock, String replaceWithGroundBlock, boolean spawnUnderWater, int waterLevel, boolean isStructureAtSpawn, boolean doReplaceAboveBelowOnly)
    {
    	if(getObject() == null)
    	{
    		throw new RuntimeException(); // TODO: Remove this after testing
    	}
        if(getObject() instanceof BO3)
        {
        	return ((BO3)getObject()).trySpawnAt(world, random, rotation, chunkCoord, x, y, z, replaceAbove, replaceBelow, replaceWithBiomeBlocks, replaceWithSurfaceBlock, replaceWithGroundBlock, spawnUnderWater, waterLevel, isStructureAtSpawn, doReplaceAboveBelowOnly);
        } else {
        	throw new RuntimeException(); // TODO: Remove this after testing
        }
    }
    
    /**
     * Rotates a coordinate around its center, assumes the center is at 0,0.
     * Should only be used for resouces that like Block() that spawn in BO3's and have a -1z offset.
     * Should not be used for branches.
     */
    public static CustomObjectCoordinate getRotatedBO3Coords(int x, int y, int z, Rotation newRotation)
    {
    	int rotations = newRotation.getRotationId();
    	if(rotations < 0)
    	{
    		throw new RuntimeException(); // TODO: Remove this after testing
    		//rotations += 4;
    	}

    	int rotatedX = x;
    	int rotatedZ = z;

    	int newX = x;
    	int newZ = z;
    	for(int i = 0; i < rotations; i++)
    	{
    		// TODO: Bo3's appear to be exported with the center block (0,0) in the top right quadrant of an x,z grid (European style).
    		// MC puts 0,0 in the lower right corner though (American style).
    		// This makes rotating BO3's (counter-clockwise) confusing.
    		// For a 16x16 BO3 minX=-8 maxX=7 minZ=-7 maxZ=8:

    		// Rotating block  0 0 0 counter clockwise 1 step should result in: -1 0 0
    		// Rotating block -1 0 0 counter clockwise 1 step should result in: -1 0 1
    		// Rotating block -1 0 1 counter clockwise 1 step should result in:  0 0 1
    		// Rotating block  0 0 1 counter clockwise 1 step should result in:  0 0 0

    		// Rotating block  0 0 -7 counter clockwise 1 step should result in: -8 0  0
    		// Rotating block -8 0  0 counter clockwise 1 step should result in: -1 0  8
    		// Rotating block -1 0  8 counter clockwise 1 step should result in:  7 0  1
    		// Rotating block  7 0  1 counter clockwise 1 step should result in:  0 0 -7

    		// Rotating block  7 0 -7 counter clockwise 1 step should result in: -8 0 -7
    		// Rotating block -8 0 -7 counter clockwise 1 step should result in: -8 0  8
    		// Rotating block -8 0  8 counter clockwise 1 step should result in:  7 0  8
    		// Rotating block  7 0  8 counter clockwise 1 step should result in:  7 0 -7

    		// So basically the center point of a BO3 (0,0) is actually 0,-1 if you'd place a BO3 at 0,0 in the world

    		newX = rotatedZ - 1;
    		newZ = -rotatedX;

    		rotatedX = newX;
    		rotatedZ = newZ;
    	}

    	return new CustomObjectCoordinate(null, null, null, newRotation, rotatedX, y, rotatedZ, false, 0, false, false, null);
    }
    
    // TODO: Why is this necessary for smoothing areas?
    public static CustomObjectCoordinate getRotatedSmoothingCoords(int x, int y, int z, Rotation newRotation)
    {
        // Assuming initial rotation is always north

        int newX = 0;
        int newY = 0;
        int newZ = 0;
        int rotations = 0;

        // How many counter-clockwise rotations have to be applied?
        if (newRotation == Rotation.WEST)
        {
            rotations = 1;
        }
        else if (newRotation == Rotation.SOUTH)
        {
            rotations = 2;
        }
        else if (newRotation == Rotation.EAST)
        {
            rotations = 3;
        }

        // Apply rotation
        if (rotations == 0)
        {
            newX = x;
            newZ = z;
        }
        if (rotations == 1)
        {
            newX = z;
            newZ = -x + 15;
        }
        if (rotations == 2)
        {
            newX = -x + 15;
            newZ = -z + 15;
        }
        if (rotations == 3)
        {
            newX = -z + 15;
            newZ = x;
        }
        newY = y;

        return new CustomObjectCoordinate(null, null, null, newRotation, newX, newY, newZ, false, 0, false, false, null);
    }
}
