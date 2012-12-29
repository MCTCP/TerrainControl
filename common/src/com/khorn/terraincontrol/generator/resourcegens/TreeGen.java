package com.khorn.terraincontrol.generator.resourcegens;

import static com.khorn.terraincontrol.events.ResourceEvent.Type.TREE;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.events.ResourceEvent;
import com.khorn.terraincontrol.exception.InvalidResourceException;

public class TreeGen extends Resource
{
    private List<CustomObject> trees;
    private List<String> treeNames;
    private List<Integer> treeChances;

    @Override
    public void process(LocalWorld world, Random random, int chunkX, int chunkZ, boolean hasGeneratedAVillage)
    {
        ResourceEvent event = getResourceEvent(world, random, chunkX, chunkZ, hasGeneratedAVillage);
        TerrainControl.fireResourceEvent(event);
        if (event.isCancelled())
        	return;

        for (int i = 0; i < frequency; i++)
        {
            for (int treeNumber = 0; treeNumber < trees.size(); treeNumber++)
            {
                if (random.nextInt(100) < treeChances.get(treeNumber))
                {
                    int x = chunkX * 16 + random.nextInt(16) + 8;
                    int z = chunkZ * 16 + random.nextInt(16) + 8;
                    if (trees.get(treeNumber).spawnAsTree(world, random, x, z))
                    {
                        // Success, on to the next tree!
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void load(List<String> args) throws InvalidResourceException
    {
        assureSize(3, args);

        frequency = getInt(args.get(0), 1, 100);

        trees = new ArrayList<CustomObject>();
        treeNames = new ArrayList<String>();
        treeChances = new ArrayList<Integer>();

        for (int i = 1; i < args.size() - 1; i += 2)
        {
            CustomObject object = TerrainControl.getCustomObjectManager().getObjectFromString(args.get(i), getHolder());
            if (object == null)
            {
                throw new InvalidResourceException("Custom object " + args.get(i) + " not found!");
            }
            if(!object.canSpawnAsTree())
            {
                throw new InvalidResourceException("Custom object " + args.get(i) + " is not a tree!");
            }
            trees.add(object);
            treeNames.add(args.get(i));
            treeChances.add(getInt(args.get(i + 1), 1, 100));
        }
    }

    @Override
    public String makeString()
    {
        String output = "Tree(" + frequency;
        for (int i = 0; i < treeNames.size(); i++)
        {
            output += "," + treeNames.get(i) + "," + treeChances.get(i);
        }
        return output + ")";
    }

    @Override
    public void spawn(LocalWorld world, Random random, int x, int z)
    {
        // Left blank, as process() already handles this
    }

	@Override
	protected ResourceEvent getResourceEvent(LocalWorld world, Random random,
			int chunkX, int chunkZ, boolean hasGeneratedAVillage) {
		return new ResourceEvent(TREE, world, random, chunkX, chunkZ, 0, 0, hasGeneratedAVillage);
	}
}
