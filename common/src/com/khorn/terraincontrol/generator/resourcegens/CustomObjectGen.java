package com.khorn.terraincontrol.generator.resourcegens;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.exception.InvalidResourceException;
import com.khorn.terraincontrol.util.Txt;

public class CustomObjectGen extends Resource
{
    private List<CustomObject> objects;
    private List<String> objectNames;

    @Override
    public void load(List<String> args) throws InvalidResourceException
    {
        if (args.size() == 0 || (args.size() == 1 && args.get(0).trim().equals("")))
        {
            // Backwards compability
            args.set(0, "UseWorld");
        }
        objects = new ArrayList<CustomObject>();
        objectNames = new ArrayList<String>();
        for (String arg : args)
        {
            CustomObject object = TerrainControl.getCustomObjectManager().getObjectFromString(arg, getHolder());
            if (object == null || !object.canSpawnAsObject())
            {
                throw new InvalidResourceException("No custom object found with the name " + arg);
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
    public void process(LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ)
    {
        // Fire event
        if(!TerrainControl.fireResourceProcessEvent(this, world, random, villageInChunk, chunkX, chunkZ))
        {
            return;
        }
        
        for (CustomObject object : objects)
        {
            object.process(world, random, chunkX, chunkZ);
        }
    }

    @Override
    public String makeString()
    {
        return "CustomObject(" + Txt.implode(objectNames, ",") + ")";
    }

}
