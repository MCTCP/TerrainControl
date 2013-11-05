package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.helpers.StringHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class CustomObjectGen extends Resource
{
    private List<CustomObject> objects;
    private List<String> objectNames;

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        if (args.isEmpty() || (args.size() == 1 && args.get(0).trim().equals("")))
        {
            // Backwards compability
            args = new ArrayList<String>();
            args.add("UseWorld");
        }
        objects = new ArrayList<CustomObject>();
        objectNames = new ArrayList<String>();
        for (String arg : args)
        {
            CustomObject object = TerrainControl.getCustomObjectManager().getObjectFromString(arg, getHolder().worldConfig);
            if (object == null || !object.canSpawnAsObject())
            {
                throw new InvalidConfigException("No custom object found with the name " + arg);
            }
            objects.add(object);
            objectNames.add(arg);
        }
    }

    @Override
    public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z)
    {
        // Left blank, as process(..) already handles this.
    }

    @Override
    protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ)
    {
        for (CustomObject object : objects)
        {
            object.process(world, random, chunkX, chunkZ);
        }
    }

    @Override
    public String makeString()
    {
        return "CustomObject(" + StringHelper.join(objectNames, ",") + ")";
    }

    @Override
    public boolean isAnalogousTo(Resource other)
    {
        if (getClass() == other.getClass()){
            try {
                CustomObjectGen otherO = (CustomObjectGen) other;
                return otherO.objectNames.size() == this.objectNames.size() && otherO.objectNames.containsAll(this.objectNames);
            } catch (Exception ex){
                TerrainControl.log(Level.WARNING, ex.getMessage());
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

}
