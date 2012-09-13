package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.Resource;
import com.khorn.terraincontrol.generator.resourcegens.ResourceGenBase;

import java.util.ArrayList;
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
                int x = _x + rand.nextInt(16);
                int z = _z + rand.nextInt(16);
                int y = world.getHighestBlockYAt(x, z);
                ObjectRarity -= 100;

                if (!ObjectCanSpawn(world, x, y, z, SelectedObject))
                    continue;


                objectSpawned = GenerateCustomObject(world, rand, x, y, z, SelectedObject);


                /*
               if (objectSpawned && !SelectedObject.groupId.endsWith(""))
               {
                   ArrayList<CustomObject> groupList = worldSettings.ObjectGroups.get(SelectedObject.groupId);
                   if (groupList == null)
                       return objectSpawned;

                   int attempts = 3;
                   if ((SelectedObject.groupFrequencyMax - SelectedObject.groupFrequencyMin) > 0)
                       attempts = SelectedObject.groupFrequencyMin + rand.nextInt(SelectedObject.groupFrequencyMax - SelectedObject.groupFrequencyMin);

                   while (attempts > 0)
                   {
                       attempts--;

                       int objIndex = rand.nextInt(groupList.size());
                       CustomObject ObjectFromGroup = groupList.get(objIndex);

                       // duno about this check, but maybe it is correct
                       if (ObjectFromGroup.branch || !ObjectFromGroup.canSpawnInBiome(world.getBiomeById(biomeId).getName()))
                           continue;

                       x = x + rand.nextInt(SelectedObject.groupSeperationMax - SelectedObject.groupSeperationMin) + SelectedObject.groupSeperationMin;
                       z = z + rand.nextInt(SelectedObject.groupSeperationMax - SelectedObject.groupSeperationMin) + SelectedObject.groupSeperationMin;
                       int _y = world.getHighestBlockYAt(x, z);
                       if ((y - _y) > 10 || (_y - y) > 10)
                           continue;

                       if (!ObjectCanSpawn(world, x, y, z, ObjectFromGroup))
                           continue;
                       GenerateCustomObject(world, rand, worldSettings, x, _y, z, ObjectFromGroup, true);


                   }

               }
                */
            }
        }


    }

    private boolean GenerateCustomObject(LocalWorld world, Random rand, int x, int y, int z, CustomObjectCompiled workObject)
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
                if (world.getTypeId((x + point.x), (y + point.y), (z + point.z)) > 0)
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

    private static boolean ObjectCanSpawn(LocalWorld world, int x, int y, int z, CustomObjectCompiled obj)
    {
        if ((world.getTypeId(x, y - 5, z) == 0) && (obj.NeedsFoundation))
            return false;

        boolean abort = false;
        int checkBlock = world.getTypeId(x, y + 2, z);
        if (!obj.SpawnWater)
            abort = ((checkBlock == DefaultMaterial.WATER.id) || (checkBlock == DefaultMaterial.STATIONARY_WATER.id));
        if (!obj.SpawnLava)
            abort = ((checkBlock == DefaultMaterial.LAVA.id) || (checkBlock == DefaultMaterial.STATIONARY_LAVA.id));

        checkBlock = world.getLightLevel(x, y + 2, z);
        if (!obj.SpawnSunlight)
            abort = (checkBlock > 8);
        if (!obj.SpawnDarkness)
            abort = (checkBlock < 9);

        if ((y < obj.SpawnElevationMin) || (y > obj.SpawnElevationMax))
            abort = true;

        if (!obj.SpawnOnBlockType.contains(world.getTypeId(x, y - 1, z)))
            abort = true;

        return !abort;
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
            output = output + (first ? "" : ";");
            if (first)
                first = false;

            if (name.equals(BODefaultValues.BO_Use_World.stringValue()) || name.equals(BODefaultValues.BO_Use_Biome.stringValue()))
            {
                output += name;
                continue;
            }

            for (CustomObjectCompiled object : res.CUObjects)
                if (object.Name.equals(name))
                    output += name + "(" + object.ChangedSettings + ")";

        }

        return output;

    }

    @Override
    protected boolean ReadString(Resource res, String[] Props, BiomeConfig biomeConfig) throws NumberFormatException
    {

        ArrayList<CustomObjectCompiled> objects = new ArrayList<CustomObjectCompiled>();
        ArrayList<String> objectsName = new ArrayList<String>();

        if (Props.length == 1 && Props[0].equals(""))
        {
            for (CustomObjectCompiled objectCompiled : biomeConfig.worldConfig.CustomObjectsCompiled)
                if (objectCompiled.parent.CheckBiome(biomeConfig.Name))
                    objects.add(objectCompiled);

            objectsName.add(BODefaultValues.BO_Use_World.stringValue());

        } else
            for (String key : Props)
            {
                if (key.equals(BODefaultValues.BO_Use_World.stringValue()))
                {
                    for (CustomObjectCompiled objectCompiled : biomeConfig.worldConfig.CustomObjectsCompiled)
                        if (objectCompiled.parent.CheckBiome(biomeConfig.Name))
                            objects.add(objectCompiled);

                    objectsName.add(BODefaultValues.BO_Use_World.stringValue());
                    continue;
                }

                if (key.equals(BODefaultValues.BO_Use_Biome.stringValue()))
                {
                    objects.addAll(biomeConfig.CustomObjectsCompiled);
                    objectsName.add(BODefaultValues.BO_Use_Biome.stringValue());
                    continue;
                }

                CustomObjectCompiled obj = ObjectsStore.Compile(key);
                if (obj != null)
                {
                    objects.add(obj);
                    objectsName.add(obj.Name);
                }

            }
        res.CUObjects = objects.toArray(res.CUObjects);
        res.CUObjectsNames = objectsName.toArray(res.CUObjectsNames);

        return true;
    }

}
