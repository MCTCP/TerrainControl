package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.Resource;
import com.khorn.terraincontrol.generator.resourcegens.ResourceGenBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class CustomObjectGen extends ResourceGenBase
{


    @Override
    public void Process(LocalWorld world, Random rand, Resource res, int _x, int _z)
    {

        if (res.CUObjects.length == 0)
            return;

        _x = _x + 8;
        _z = _z + 8;

        boolean objectSpawned = false;
        int spawnAttempts = 0;
        while (!objectSpawned)
        {
            if (spawnAttempts > world.getSettings().objectSpawnRatio)
                return;

            spawnAttempts++;

            CustomObjectCompiled SelectedObject = res.CUObjects[rand.nextInt(res.CUObjects.length)];
            if (SelectedObject.Branch)
                continue;

            int randomRoll = rand.nextInt(100);
            int ObjectRarity = SelectedObject.Rarity;

            while (randomRoll < ObjectRarity)
            {
                ObjectRarity -= 100;
                
                int x = _x + rand.nextInt(16);
                int z = _z + rand.nextInt(16);
                int y;

                if (SelectedObject.SpawnAboveGround)
                    y = world.getSolidHeight(x, z);
                else if (SelectedObject.SpawnUnderGround)
                {
                    int solidHeight = world.getSolidHeight(x, z);
                    if (solidHeight < 1 || solidHeight <= SelectedObject.SpawnElevationMin)
                        continue;
                    if (solidHeight > SelectedObject.SpawnElevationMax)
                        solidHeight = SelectedObject.SpawnElevationMax;
                    y = rand.nextInt(solidHeight - SelectedObject.SpawnElevationMin) + SelectedObject.SpawnElevationMin;
                } else
                    y = world.getHighestBlockYAt(x, z);

                if (y < 0)
                    continue;

                if (!ObjectCanSpawn(world, x, y, z, SelectedObject))
                    continue;


                objectSpawned = GenerateCustomObject(world, rand, x, y, z, SelectedObject);

                if (objectSpawned)
                    GenerateCustomObjectFromGroup(world, rand, x, y, z, SelectedObject);
            }
        }


    }

    public static void GenerateCustomObjectFromGroup(LocalWorld world, Random rand, int x, int y, int z, CustomObjectCompiled workObject)
    {
        if (workObject.GroupObjects == null)
            return;

        int attempts = 3;
        if ((workObject.GroupFrequencyMax - workObject.GroupFrequencyMin) > 0)
            attempts = workObject.GroupFrequencyMin + rand.nextInt(workObject.GroupFrequencyMax - workObject.GroupFrequencyMin);

        while (attempts > 0)
        {
            attempts--;

            int objIndex = rand.nextInt(workObject.GroupObjects.length);
            CustomObjectCompiled ObjectFromGroup = workObject.GroupObjects[objIndex];

            if (ObjectFromGroup.Branch)
                continue;

            x = x + rand.nextInt(workObject.GroupSeparationMax - workObject.GroupSeparationMin) + workObject.GroupSeparationMin;
            z = z + rand.nextInt(workObject.GroupSeparationMax - workObject.GroupSeparationMin) + workObject.GroupSeparationMin;
            int _y;

            if (workObject.SpawnAboveGround)
                _y = world.getSolidHeight(x, z);
            else if (workObject.SpawnUnderGround)
            {
                int solidHeight = world.getSolidHeight(x, z);
                if (solidHeight < 1 || solidHeight <= workObject.SpawnElevationMin)
                    continue;
                if (solidHeight > workObject.SpawnElevationMax)
                    solidHeight = workObject.SpawnElevationMax;
                _y = rand.nextInt(solidHeight - workObject.SpawnElevationMin) + workObject.SpawnElevationMin;
            } else
                _y = world.getHighestBlockYAt(x, z);

            if (y < 0)
                continue;

            if ((y - _y) > 10 || (_y - y) > 10)
                continue;

            if (!ObjectCanSpawn(world, x, y, z, ObjectFromGroup))
                continue;
            GenerateCustomObject(world, rand, x, _y, z, ObjectFromGroup);
        }


    }


    public static boolean GenerateCustomObject(LocalWorld world, Random rand, int x, int y, int z, CustomObjectCompiled workObject)
    {

        ObjectCoordinate[] data = workObject.Data[0];
        if (workObject.RandomRotation)
            data = workObject.Data[rand.nextInt(4)];


        int faultCounter = 0;

        for (ObjectCoordinate point : data)
        {
            if (!world.isLoaded((x + point.x), (y + point.y), (z + point.z)))
                return false;

            if (!workObject.Dig)
            {
                if (workObject.CollisionBlockType.contains(world.getTypeId((x + point.x), (y + point.y), (z + point.z))))
                {
                    faultCounter++;
                    if (faultCounter > (data.length * (workObject.CollisionPercentage / 100)))
                    {
                        return false;
                    }
                }
            }


        }

        for (ObjectCoordinate point : data)
        {

            if (world.getTypeId(x + point.x, y + point.y, z + point.z) == 0)
            {
                world.setBlock((x + point.x), y + point.y, z + point.z, point.BlockId, point.BlockData, true, false, true);
            } else if (workObject.Dig)
            {
                world.setBlock((x + point.x), y + point.y, z + point.z, point.BlockId, point.BlockData, true, false, true);
            }

        }
        return true;

    }

    public static boolean ObjectCanSpawn(LocalWorld world, int x, int y, int z, CustomObjectCompiled obj)
    {
        if ((world.getTypeId(x, y - 5, z) == 0) && (obj.NeedsFoundation))
            return false;

        boolean output = true;
        int checkBlock = world.getTypeId(x, y + 2, z);
        if (!obj.SpawnWater)
            output = !((checkBlock == DefaultMaterial.WATER.id) || (checkBlock == DefaultMaterial.STATIONARY_WATER.id));
        if (!obj.SpawnLava)
            output = !((checkBlock == DefaultMaterial.LAVA.id) || (checkBlock == DefaultMaterial.STATIONARY_LAVA.id));

        checkBlock = world.getLightLevel(x, y + 2, z);
        if (!obj.SpawnSunlight)
            output = !(checkBlock > 8);
        if (!obj.SpawnDarkness)
            output = !(checkBlock < 9);

        if ((y < obj.SpawnElevationMin) || (y > obj.SpawnElevationMax))
            output = false;

        if (!obj.SpawnOnBlockType.contains(world.getTypeId(x, y - 1, z)))
            output = false;

        return output;
    }

    @Override
    protected void SpawnResource(LocalWorld world, Random rand, Resource res, int x, int z)
    {


    }

    @Override
    protected String WriteString(Resource res, String blockSources)
    {
        String output = "";
        boolean first = true;

        for (String name : res.CUObjectsNames)
        {
            output = output + (first ? "" : ",");
            if (first)
                first = false;

            if (name.equals(BODefaultValues.BO_Use_World.stringValue()) || name.equals(BODefaultValues.BO_Use_Biome.stringValue()))
            {
                output += name;
                continue;
            }

            for (CustomObjectCompiled object : res.CUObjects)
                if (object.Name.equals(name))
                    output += name + (object.ChangedSettings.equals("") ? "" : ("(" + object.ChangedSettings + ")"));

        }

        return output;

    }

    @Override
    protected boolean ReadString(Resource res, String[] Props, BiomeConfig biomeConfig) throws NumberFormatException
    {

        ArrayList<CustomObjectCompiled> objects = new ArrayList<CustomObjectCompiled>();
        ArrayList<String> objectsName = new ArrayList<String>();
        HashMap<String, ArrayList<CustomObjectCompiled>> Groups = new HashMap<String, ArrayList<CustomObjectCompiled>>();

        if (Props.length == 1 && Props[0].equals(""))
        {
            AddCompiledObjectsFromWorld(biomeConfig, objects, Groups);
            objectsName.add(BODefaultValues.BO_Use_World.stringValue());

        } else
            for (String key : Props)
            {
                if (key.equals(BODefaultValues.BO_Use_World.stringValue()))
                {
                    AddCompiledObjectsFromWorld(biomeConfig, objects, Groups);
                    objectsName.add(BODefaultValues.BO_Use_World.stringValue());
                    continue;
                }

                if (key.equals(BODefaultValues.BO_Use_Biome.stringValue()))
                {
                    AddCompiledObjectsFromBiome(biomeConfig, objects, Groups);
                    objectsName.add(BODefaultValues.BO_Use_Biome.stringValue());
                    continue;
                }

                CustomObjectCompiled obj = ObjectsStore.CompileString(key, biomeConfig.worldConfig.CustomObjectsDirectory);
                if (obj == null)
                    obj = ObjectsStore.CompileString(key, ObjectsStore.GlobalDirectory);
                if (obj != null)
                {
                    objects.add(obj);
                    objectsName.add(obj.Name);

                    if (!obj.GroupId.equals(""))
                    {
                        if (!Groups.containsKey(obj.GroupId))
                            Groups.put(obj.GroupId, new ArrayList<CustomObjectCompiled>());

                        Groups.get(obj.GroupId).add(obj);

                    }


                }

            }

        for (CustomObjectCompiled objectCompiled : objects)
        {
            if (Groups.containsKey(objectCompiled.GroupId))
            {
                objectCompiled.GroupObjects = Groups.get(objectCompiled.GroupId).toArray(new CustomObjectCompiled[0]);
            }
        }

        res.CUObjects = objects.toArray(res.CUObjects);
        res.CUObjectsNames = objectsName.toArray(res.CUObjectsNames);

        return true;
    }


    private void AddCompiledObjectsFromWorld(BiomeConfig biomeConfig, ArrayList<CustomObjectCompiled> output, HashMap<String, ArrayList<CustomObjectCompiled>> groups)
    {
        for (CustomObjectCompiled objectCompiled : biomeConfig.worldConfig.CustomObjectsCompiled)
            if (objectCompiled.CheckBiome(biomeConfig.Name))
            {
                output.add(objectCompiled);
                if (!objectCompiled.GroupId.equals(""))
                {
                    if (!groups.containsKey(objectCompiled.GroupId))
                        groups.put(objectCompiled.GroupId, new ArrayList<CustomObjectCompiled>());

                    groups.get(objectCompiled.GroupId).add(objectCompiled);

                }

            }

    }

    private void AddCompiledObjectsFromBiome(BiomeConfig biomeConfig, ArrayList<CustomObjectCompiled> output, HashMap<String, ArrayList<CustomObjectCompiled>> groups)
    {
        for (CustomObjectCompiled objectCompiled : biomeConfig.CustomObjectsCompiled)
        {
            output.add(objectCompiled);
            if (!objectCompiled.GroupId.equals(""))
            {
                if (!groups.containsKey(objectCompiled.GroupId))
                    groups.put(objectCompiled.GroupId, new ArrayList<CustomObjectCompiled>());

                groups.get(objectCompiled.GroupId).add(objectCompiled);

            }

        }

    }

}
