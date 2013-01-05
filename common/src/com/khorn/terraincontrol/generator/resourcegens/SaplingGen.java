package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.exception.InvalidConfigException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SaplingGen extends ConfigFunction<WorldConfig>
{
    public List<CustomObject> trees;
    public List<String> treeNames;
    public List<Integer> treeChances;
    public int saplingType;

    @Override
    public Class<WorldConfig> getHolderType()
    {
        return WorldConfig.class;
    }

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(3, args);

        if (args.get(0).equalsIgnoreCase("All"))
        {
            saplingType = -1;
        } else
        {
            saplingType = readInt(args.get(0), -1, 3);
        }

        trees = new ArrayList<CustomObject>();
        treeNames = new ArrayList<String>();
        treeChances = new ArrayList<Integer>();

        for (int i = 1; i < args.size() - 1; i += 2)
        {
            CustomObject object = TerrainControl.getCustomObjectManager().getObjectFromString(args.get(i), getHolder());
            if (object == null)
            {
                throw new InvalidConfigException("Custom object " + args.get(i) + " not found!");
            }
            if (!object.canSpawnAsTree())
            {
                throw new InvalidConfigException("Custom object " + args.get(i) + " is not a tree!");
            }
            trees.add(object);
            treeNames.add(args.get(i));
            treeChances.add(readInt(args.get(i + 1), 1, 100));
        }
    }

    @Override
    public String makeString()
    {
        String output = "Sapling(" + saplingType;
        if (saplingType == -1)
        {
            output = "Sapling(All";
        }
        for (int i = 0; i < treeNames.size(); i++)
        {
            output += "," + treeNames.get(i) + "," + treeChances.get(i);
        }
        return output + ")";
    }

    public boolean growSapling(LocalWorld world, Random random, int x, int y, int z)
    {
        for (int treeNumber = 0; treeNumber < trees.size(); treeNumber++)
        {
            if (random.nextInt(100) < treeChances.get(treeNumber))
            {
                if (trees.get(treeNumber).spawnAsTree(world, random, x, y, z))
                {
                    // Success!
                    return true;
                }
            }
        }
        return false;
    }
}
