package com.pg85.otg.generator.resource;

import com.pg85.otg.LocalWorld;
import com.pg85.otg.OTG;
import com.pg85.otg.configuration.ConfigFunction;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.helpers.StringHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomObjectGen extends Resource
{
	// OTG+
	
    public List<CustomObject> getObjects(String worldName)
    {
    	if(objects.isEmpty() && !objectNames.isEmpty())
    	{
            for (int i = 0; i < objectNames.size(); i ++)
            {
            	CustomObject object = OTG.getCustomObjectManager().getGlobalObjects().getObjectByName(objectNames.get(i), worldName);
            	objects.add(object);	              	
            }
    	}
    	return objects;
    }
	
	//
	
    private List<CustomObject> objects;
    private List<String> objectNames;

    public CustomObjectGen(BiomeConfig biomeConfig, List<String> args) throws InvalidConfigException
    {
        super(biomeConfig);
        if (args.isEmpty() || (args.size() == 1 && args.get(0).trim().isEmpty()))
        {
            // Backwards compatibility
            args = new ArrayList<String>();
            args.add("UseWorld");
        }
        objects = new ArrayList<CustomObject>();
        objectNames = new ArrayList<String>();
        for (String arg : args)
        {
            objectNames.add(arg);
        }
    }

    @Override
    public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z)
    {
        // Left blank, as process(..) already handles this.
    }

    @Override
    protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
        for (CustomObject object : getObjects(world.getName()))
        {
        	if(object != null) // if null then BO2/BO3 file could not be found
        	{
        		object.process(world, random, chunkCoord);
        	}
        }
    }

    @Override
    public String toString()
    {
        return "CustomObject(" + StringHelper.join(objectNames, ",") + ")";
    }

    @Override
    public boolean isAnalogousTo(ConfigFunction<BiomeConfig> other)
    {
        if (getClass() == other.getClass()){
            try {
                CustomObjectGen otherO = (CustomObjectGen) other;
                return otherO.objectNames.size() == this.objectNames.size() && otherO.objectNames.containsAll(this.objectNames);
            } catch (Exception ex){
                OTG.log(LogMarker.WARN, ex.getMessage());
            }
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 41 * hash + super.hashCode();
        hash = 41 * hash + (this.objects != null ? this.objects.hashCode() : 0);
        hash = 41 * hash + (this.objectNames != null ? this.objectNames.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object other)
    {
        if (!super.equals(other))
            return false;
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (getClass() != other.getClass())
            return false;
        final CustomObjectGen compare = (CustomObjectGen) other;
        return (this.objects == null ? this.objects == compare.objects
                : this.objects.equals(compare.objects))
               && (this.objectNames == null ? this.objectNames == compare.objectNames
                   : this.objectNames.equals(compare.objectNames));
    }

    @Override
    public int getPriority()
    {
        return -40;
    }

}
