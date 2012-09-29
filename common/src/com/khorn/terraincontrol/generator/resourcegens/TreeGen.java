package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.Resource;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.customobjects.BODefaultValues;
import com.khorn.terraincontrol.customobjects.CustomObjectCompiled;
import com.khorn.terraincontrol.customobjects.CustomObjectGen;
import com.khorn.terraincontrol.customobjects.ObjectsStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class TreeGen extends ResourceGenBase
{
    @Override
    protected void SpawnResource(LocalWorld world, Random rand, Resource res, int x, int z)
    {

    }

    @Override
    public void Process(LocalWorld world, Random rand, Resource res, int x, int z)
    {
        for (int i = 0; i < res.Frequency; i++)
        {

            int _x = x + rand.nextInt(16) + 8;
            int _z = z + rand.nextInt(16) + 8;
            int _y = world.getHighestBlockYAt(_x, _z);
            SpawnTree(world, rand, res, _x, _y, _z);
        }
    }

    public boolean SpawnTree(LocalWorld world, Random rand, Resource res, int x, int y, int z)
    {
        boolean treeSpawned = false;

        for (int t = 0; t < res.TreeTypes.length && !treeSpawned; t++)
        {
            if (rand.nextInt(100) < res.TreeChances[t])
            {
                switch (res.TreeTypes[t])
                {
                    case CustomTree:
                        CustomObjectCompiled SelectedObject = res.CUObjects[t];
                        // TODO Branch check ?!!!

                        if (!CustomObjectGen.ObjectCanSpawn(world, x, y, z, SelectedObject))
                            continue;

                        treeSpawned = CustomObjectGen.GenerateCustomObject(world, rand, x, y, z, SelectedObject);

                        if (treeSpawned)
                            CustomObjectGen.GenerateCustomObjectFromGroup(world, rand, x, y, z, SelectedObject);
                        break;
                    case CustomTreeWorld:
                        treeSpawned = SpawnCustomTreeFromArray(world, rand, x, y, z, res.CUObjectsWorld);
                        break;
                    case CustomTreeBiome:
                        treeSpawned = SpawnCustomTreeFromArray(world, rand, x, y, z, res.CUObjectsBiome);
                        break;
                    default:
                        treeSpawned = world.PlaceTree(res.TreeTypes[t], rand, x, y, z);
                        break;
                }
            }
        }
        return treeSpawned;

    }

    private boolean SpawnCustomTreeFromArray(LocalWorld world, Random rand, int x, int y, int z, CustomObjectCompiled[] CUObjects)
    {

        if (CUObjects.length == 0)
            return false;

        boolean objectSpawned = false;
        int spawnAttempts = 0;
        while (!objectSpawned)
        {
            if (spawnAttempts > world.getSettings().objectSpawnRatio)
                return false;

            spawnAttempts++;

            CustomObjectCompiled SelectedObject = CUObjects[rand.nextInt(CUObjects.length)];
            if (SelectedObject.Branch)
                continue;

            if (rand.nextInt(100) < SelectedObject.Rarity)
            {

                if (!CustomObjectGen.ObjectCanSpawn(world, x, y, z, SelectedObject))
                    continue;

                objectSpawned = CustomObjectGen.GenerateCustomObject(world, rand, x, y, z, SelectedObject);

                if (objectSpawned)
                    CustomObjectGen.GenerateCustomObjectFromGroup(world, rand, x, y, z, SelectedObject);
            }
        }

        return objectSpawned;
    }

    @Override
    protected boolean ReadString(Resource res, String[] Props, BiomeConfig biomeConfig) throws NumberFormatException
    {
        if (res.Type == ResourceType.Sapling)
        {
            if(Props[0].equals("All"))
                res.BlockData = -1;
            else
                res.BlockData = CheckValue(Props[0], 0, 4);

        } else
            res.Frequency = CheckValue(Props[0], 1, 100);

        ArrayList<TreeType> treeTypes = new ArrayList<TreeType>();
        ArrayList<Integer> treeChances = new ArrayList<Integer>();

        ArrayList<CustomObjectCompiled> customTrees = new ArrayList<CustomObjectCompiled>();
        HashMap<String, ArrayList<CustomObjectCompiled>> Groups = new HashMap<String, ArrayList<CustomObjectCompiled>>();

        boolean hasCustomTreeWorld = false;
        boolean hasCustomTreeBiome = false;

        for (int index = 1; (index + 1) < Props.length; index += 2)
        {
            String tree = Props[index];
            boolean defaultTreeFound = false;
            for (TreeType type : TreeType.values())
            {
                if (type == TreeType.CustomTree || type == TreeType.CustomTreeWorld || type == TreeType.CustomTreeBiome)
                    continue;

                if (type.name().equals(tree))
                {
                    defaultTreeFound = true;

                    treeTypes.add(type);
                    treeChances.add(CheckValue(Props[index + 1], 0, 100));
                    break;
                }
            }
            if (defaultTreeFound)
                continue;

            // Check custom objects

            if (tree.equals(BODefaultValues.BO_Use_World.stringValue()))
            {
                treeTypes.add(TreeType.CustomTreeWorld);
                treeChances.add(CheckValue(Props[index + 1], 0, 100));
                hasCustomTreeWorld = true;
                continue;
            }

            if (tree.equals(BODefaultValues.BO_Use_Biome.stringValue()))
            {
                treeTypes.add(TreeType.CustomTreeBiome);
                treeChances.add(CheckValue(Props[index + 1], 0, 100));
                hasCustomTreeBiome = true;
                continue;
            }

            CustomObjectCompiled obj = ObjectsStore.Compile(tree);
            if (obj != null)
            {
                customTrees.add(obj);
                treeTypes.add(TreeType.CustomTree);
                treeChances.add(CheckValue(Props[index + 1], 0, 100));

                if (!obj.GroupId.equals(""))
                {
                    if (!Groups.containsKey(obj.GroupId))
                        Groups.put(obj.GroupId, new ArrayList<CustomObjectCompiled>());

                    Groups.get(obj.GroupId).add(obj);

                }
            }


        }
        if (treeChances.size() == 0)
            return false;


        if (hasCustomTreeBiome)
        {
            ArrayList<CustomObjectCompiled> customTreesBiome = new ArrayList<CustomObjectCompiled>();
            for (CustomObjectCompiled objectCompiled : biomeConfig.CustomObjectsCompiled)
            {
                if (!objectCompiled.Tree)
                    continue;
                customTreesBiome.add(objectCompiled);
                if (!objectCompiled.GroupId.equals(""))
                {
                    if (!Groups.containsKey(objectCompiled.GroupId))
                        Groups.put(objectCompiled.GroupId, new ArrayList<CustomObjectCompiled>());

                    Groups.get(objectCompiled.GroupId).add(objectCompiled);

                }

            }

            res.CUObjectsBiome = customTreesBiome.toArray(res.CUObjectsBiome);

        }

        if (hasCustomTreeWorld)
        {
            ArrayList<CustomObjectCompiled> customTreesWorld = new ArrayList<CustomObjectCompiled>();
            for (CustomObjectCompiled objectCompiled : biomeConfig.worldConfig.CustomObjectsCompiled)
            {
                if (objectCompiled.CheckBiome(biomeConfig.Name))
                {
                    if (!objectCompiled.Tree)
                        continue;
                    customTreesWorld.add(objectCompiled);
                    if (!objectCompiled.GroupId.equals(""))
                    {
                        if (!Groups.containsKey(objectCompiled.GroupId))
                            Groups.put(objectCompiled.GroupId, new ArrayList<CustomObjectCompiled>());

                        Groups.get(objectCompiled.GroupId).add(objectCompiled);

                    }
                }

            }

            res.CUObjectsWorld = customTreesWorld.toArray(res.CUObjectsBiome);

        }

        for (CustomObjectCompiled objectCompiled : res.CUObjectsBiome)
            if (Groups.containsKey(objectCompiled.GroupId))
                objectCompiled.GroupObjects = Groups.get(objectCompiled.GroupId).toArray(objectCompiled.GroupObjects);

        for (CustomObjectCompiled objectCompiled : res.CUObjectsWorld)
            if (Groups.containsKey(objectCompiled.GroupId))
                objectCompiled.GroupObjects = Groups.get(objectCompiled.GroupId).toArray(objectCompiled.GroupObjects);

        for (CustomObjectCompiled objectCompiled : customTrees)
            if (Groups.containsKey(objectCompiled.GroupId))
                objectCompiled.GroupObjects = Groups.get(objectCompiled.GroupId).toArray(objectCompiled.GroupObjects);

        Groups.clear();


        res.TreeTypes = new TreeType[treeChances.size()];
        res.TreeChances = new int[treeChances.size()];
        res.CUObjects = new CustomObjectCompiled[treeChances.size()];
        res.CUObjectsNames = new String[treeChances.size()];

        int customIndex = 0;
        for (int t = 0; t < treeTypes.size(); t++)
        {
            res.TreeTypes[t] = treeTypes.get(t);
            res.TreeChances[t] = treeChances.get(t);
            if (treeTypes.get(t) == TreeType.CustomTree)
                res.CUObjects[t] = customTrees.get(customIndex++);
        }

        return true;
    }

    @Override
    protected String WriteString(Resource res, String blockSources)
    {
        String output;
        if(res.Type == ResourceType.Sapling)
        {
           if(res.BlockData == -1)
               output = "All";
            else
               output = "" + res.BlockData;

        } else
        output = String.valueOf(res.Frequency);
        for (int i = 0; i < res.TreeChances.length; i++)
        {
            output += ",";

            if (res.TreeTypes[i] == TreeType.CustomTreeWorld)
                output += BODefaultValues.BO_Use_World.stringValue() + "," + res.TreeChances[i];
            else if (res.TreeTypes[i] == TreeType.CustomTreeBiome)
                output += BODefaultValues.BO_Use_Biome.stringValue() + "," + res.TreeChances[i];
            else if (res.TreeTypes[i] == TreeType.CustomTree)
                output += res.CUObjects[i].Name + (res.CUObjects[i].ChangedSettings.equals("") ? "" : ("(" + res.CUObjects[i].ChangedSettings + ")")) + "," + res.TreeChances[i];
            else
                output += res.TreeTypes[i].name() + "," + res.TreeChances[i];
        }
        return output;
    }
}
