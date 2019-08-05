package com.pg85.otg.customobjects.structures;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.customobjects.bo3.BO3;
import com.pg85.otg.customobjects.bo3.StructurePartSpawnHeight;
import java.util.Random;

/**
 * Represents an object along with its location in the world.
 */
public abstract class CustomStructureCoordinate
{
	public String bo3Name;
	public String worldName;
	
    protected transient StructuredCustomObject object;
    public Rotation rotation;
    public int x;
    public short y;
    public int z;
	
    protected CustomStructureCoordinate() { } 
        
    public abstract boolean spawnWithChecks(CustomStructure structure, LocalWorld world, StructurePartSpawnHeight height, Random random);
    
    public int getX()
    {
        return x;
    }

    public short getY()
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
    
    public final int getChunkX()
    {
    	return (int)MathHelper.floor(x / (double)16); 
    }
    
    public final int getChunkZ()
    {
    	return (int)MathHelper.floor(z / (double)16);
    }
    
    /**
     * Returns the object of this coordinate.
     *
     * @return The object.
     */
    public StructuredCustomObject getObject()
    {
    	if(object == null)
    	{
    		if(worldName == null)
    		{
    			throw new RuntimeException(); // TODO: Remove this after testing
    		}

    		CustomObject object = OTG.getCustomObjectManager().getGlobalObjects().getObjectByName(bo3Name, worldName);

    		if(object == null || !(object instanceof StructuredCustomObject))
    		{
    			object = null;
    			if(OTG.getPluginConfig().spawnLog)
    			{
    				OTG.log(LogMarker.WARN, "Could not find BO2/BO3 " + bo3Name + " in GlobalObjects or WorldObjects directory.");
    			}
    		}
			bo3Name = object != null ? object.getName() : bo3Name;

    		if(object != null && ((BO3)object).getSettings() == null)
    		{
    			throw new RuntimeException(); // TODO: Remove this after testing
    		}

    		this.object = (StructuredCustomObject)object;
    		return this.object;
    	}

        return object;
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
        if (!(otherObject instanceof CustomStructureCoordinate))
        {
            return false;
        }
        CustomStructureCoordinate otherCoord = (CustomStructureCoordinate) otherObject;
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
}
