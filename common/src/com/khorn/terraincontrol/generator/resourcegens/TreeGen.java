package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.configuration.Resource;
import com.khorn.terraincontrol.LocalWorld;

import java.util.ArrayList;
import java.util.Random;

public class TreeGen extends ResourceGenBase
{
    @Override
    protected void SpawnResource(LocalWorld world, Random rand, Resource res, int x, int z)
    {

    }

    @Override
    public void Process(LocalWorld world, Random rand, Resource res, int x, int z, int biomeId)
    {
        for (int i = 0; i < res.Frequency; i++)
        {

            int _x = x + rand.nextInt(16) + 8;
            int _z = z + rand.nextInt(16) + 8;
            int _y = world.getHighestBlockYAt(_x, _z);

            for (int t = 0; t < res.TreeTypes.length; t++)
                if (rand.nextInt(100) < res.TreeChances[t])
                    world.PlaceTree(res.TreeTypes[t], rand, _x, _y, _z);


        }
    }

    @Override
    protected boolean ReadString(Resource res, String[] Props, int worldHeight) throws NumberFormatException
    {
        res.Frequency = CheckValue(Props[0], 1, 100);

        ArrayList<TreeType> treeTypes = new ArrayList<TreeType>();
        ArrayList<Integer> treeChances = new ArrayList<Integer>();

        for (int i = 1; i < Props.length && (i + 1) < Props.length; i += 2)
        {
            String tree = Props[i];
            for (TreeType type : TreeType.values())
                if (type.name().equals(tree))
                {
                    treeTypes.add(type);
                    treeChances.add(CheckValue(Props[i + 1], 0, 100));
                }
        }
        if (treeChances.size() == 0)
            return false;

        res.TreeTypes = new TreeType[treeChances.size()];
        res.TreeChances = new int[treeChances.size()];
        for (int t = 0; t < treeTypes.size(); t++)
        {
            res.TreeTypes[t] = treeTypes.get(t);
            res.TreeChances[t] = treeChances.get(t);
        }

        return true;
    }

    @Override
    protected String WriteString(Resource res, String blockSources)
    {
        String output = String.valueOf(res.Frequency);
        for (int i = 0; i < res.TreeChances.length; i++)
            output += "," + res.TreeTypes[i].name() + "," + res.TreeChances[i];
        return output;
    }
}
