package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.customobjects.*;
import com.khorn.terraincontrol.exception.InvalidConfigException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomStructureGen extends Resource
{
    private List<StructuredCustomObject> objects;
    private List<Integer> objectChances;
    private List<String> objectNames;

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        objects = new ArrayList<StructuredCustomObject>();
        objectNames = new ArrayList<String>();
        objectChances = new ArrayList<Integer>();
        for (int i = 0; i < args.size() - 1; i++)
        {
            CustomObject object = TerrainControl.getCustomObjectManager().getObjectFromString(args.get(i), getHolder().worldConfig);
            if (object == null || !object.canSpawnAsObject())
            {
                throw new InvalidConfigException("No custom object found with the name " + args.get(i));
            }
            if (!(object instanceof StructuredCustomObject) || ((StructuredCustomObject) object).getBranches(Rotation.NORTH).length == 0)
            {
                throw new InvalidConfigException("The object " + args.get(i) + " isn't a structure");
            }
            objects.add((StructuredCustomObject) object);
            objectNames.add(args.get(i));
            objectChances.add(readInt(args.get(i + 1), 1, 100));
        }

        // Inject ourselves in the BiomeConfig
        if (getHolder().structureGen != null)
        {
            throw new InvalidConfigException("There can only be one CustomStructure resource in each BiomeConfig");
        }
        getHolder().structureGen = this;
    }

    @Override
    public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z)
    {
        // Left blank, as spawnInChunk(..) already handles this.
    }

    @Override
    protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, int currentChunkX, int currentChunkZ)
    {
        // Find all structures that reach this chunk, and spawn them
        int searchRadius = 5; // Maybe add a setting for this?

        for (int searchChunkX = currentChunkX - searchRadius; searchChunkX < currentChunkX + searchRadius; searchChunkX++)
        {
            for (int searchChunkZ = currentChunkZ - searchRadius; searchChunkZ < currentChunkZ + searchRadius; searchChunkZ++)
            {
                CustomObjectStructure structureStart = world.getStructureCache().getStructureStart(searchChunkX, searchChunkZ);
                if (structureStart != null)
                {
                    structureStart.spawnForChunk(currentChunkX, currentChunkZ);
                }
            }
        }
    }

    @Override
    public String makeString()
    {
        if (objects.size() == 0)
        {
            return "CustomStructure()";
        }
        String output = "CustomStructure(" + objectNames.get(0) + "," + objectChances.get(0);
        for (int i = 1; i < objectNames.size(); i++)
        {
            output += "," + objectNames.get(i) + "," + objectChances.get(i);
        }
        return output + ")";
    }

    public CustomObjectCoordinate getRandomObjectCoordinate(Random random, int chunkX, int chunkZ)
    {
        if (objects.size() == 0)
        {
            return null;
        }
        for (int objectNumber = 0; objectNumber < objects.size(); objectNumber++)
        {
            if (random.nextInt(100) < objectChances.get(objectNumber))
            {
                return objects.get(objectNumber).makeCustomObjectCoordinate(random, chunkX, chunkZ);
            }
        }
        return null;
    }

}
