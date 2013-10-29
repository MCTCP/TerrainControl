package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.StringHelper;

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
        if (other.getClass().getName().equals(this.getClass().getName())){
            try {
                CustomObjectGen otherO = (CustomObjectGen) other;
                return otherO.objectNames.size() == this.objectNames.size() && otherO.objectNames.containsAll(this.objectNames);
            } catch (Exception ex){
                TerrainControl.log(Level.WARNING, ex.getMessage());
            }
        }
        return false;
    }

}
