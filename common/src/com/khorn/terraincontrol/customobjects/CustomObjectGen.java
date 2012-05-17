package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.configuration.Resource;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.generator.resourcegens.ResourceGenBase;
import com.khorn.terraincontrol.LocalWorld;

import java.util.ArrayList;
import java.util.Random;

public class CustomObjectGen extends ResourceGenBase
{
    @Override
    public void Process(LocalWorld world, Random rand, Resource res, int _x, int _z, int biomeId)
    {
        SpawnCustomObjects(world, rand, world.getSettings(), _x + 8, _z + 8, biomeId);
    }

    private static boolean ObjectCanSpawn(LocalWorld world, int x, int y, int z, CustomObject obj)
    {
        if ((world.getTypeId(x, y - 5, z) == 0) && (obj.needsFoundation))
            return false;

        boolean abort = false;
        int checkBlock = world.getTypeId(x, y + 2, z);
        if (!obj.spawnWater)
            abort = ((checkBlock == DefaultMaterial.WATER.id) || (checkBlock == DefaultMaterial.STATIONARY_WATER.id));
        if (!obj.spawnLava)
            abort = ((checkBlock == DefaultMaterial.LAVA.id) || (checkBlock == DefaultMaterial.STATIONARY_LAVA.id));

        checkBlock = world.getLightLevel(x, y + 2, z);
        if (!obj.spawnSunlight)
            abort = (checkBlock > 8);
        if (!obj.spawnDarkness)
            abort = (checkBlock < 9);

        if ((y < obj.spawnElevationMin) || (y > obj.spawnElevationMax))
            abort = true;

        if (!obj.spawnOnBlockType.contains(world.getTypeId(x, y - 1, z)))
            abort = true;

        return !abort;
    }

    public static boolean SpawnCustomObjects(LocalWorld world, Random rand, WorldConfig worldSettings, int chunk_x, int chunk_z, int biomeId)
    {
        if (worldSettings.Objects.size() == 0)
            return false;

        boolean objectSpawned = false;
        int spawnattemps = 0;
        while (!objectSpawned)
        {
            if (spawnattemps > worldSettings.objectSpawnRatio)
                return false;

            spawnattemps++;

            CustomObject SelectedObject = worldSettings.Objects.get(rand.nextInt(worldSettings.Objects.size()));

            if (SelectedObject.branch || !SelectedObject.canSpawnInBiome(world.getBiomeById(biomeId).getName()))
                continue;

            int randomRoll = rand.nextInt(100);
            int ObjectRarity = SelectedObject.rarity;

            while (randomRoll < ObjectRarity)
            {
                int x = chunk_x + rand.nextInt(16);
                int z = chunk_z + rand.nextInt(16);
                int y = world.getHighestBlockYAt(x, z);
                ObjectRarity -= 100;

                if (!ObjectCanSpawn(world, x, y, z, SelectedObject))
                    continue;

                objectSpawned = GenerateCustomObject(world, rand, worldSettings, x, y, z, SelectedObject, false);
                // Checked Biome, Branch, Tree - soo try to generate.

                // here we spawn object and check group spawning

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

            }

        }
        return objectSpawned;
    }


    public static boolean SpawnCustomTrees(LocalWorld world, Random rand, WorldConfig worldSettings, int x, int y, int z)
    {

        if (!worldSettings.HasCustomTrees)
            return false;


        String biomeName = world.getLocalBiome(x | 0xF, z | 0xF).getName();

        boolean objectSpawned = false;
        int spawnattemps = 0;
        while (!objectSpawned && spawnattemps < worldSettings.objectSpawnRatio)
        {
            CustomObject SelectedObject = worldSettings.Objects.get(rand.nextInt(worldSettings.Objects.size()));

            spawnattemps++;

            if (SelectedObject.branch || !SelectedObject.canSpawnInBiome(biomeName) || !SelectedObject.tree)
                continue;


            int randomRoll = rand.nextInt(100);

            if (randomRoll < SelectedObject.rarity)
            {
                if (CustomObjectGen.ObjectCanSpawn(world, x, y, z, SelectedObject))
                    objectSpawned = GenerateCustomObject(world, rand, worldSettings, x, y, z, SelectedObject, true);
            }

        }
        return objectSpawned;
    }

    public static boolean GenerateCustomObject(LocalWorld world, Random rand, WorldConfig worldSettings, int x, int y, int z, CustomObject workObject, boolean notify)
    {
        /*
         * 1) ground check (moved to ObjectCanSpawn)
         * 2) add branches and copy all data to work array (dont change default CustomObject object)
         * 3) collision check and rotation
         * 4) spawn
         */

        // 1)


        // 2)

        int index = 0;
        int branchLimit = 0;
        ArrayList<CustomObject> branchGroup = worldSettings.BranchGroups.get(workObject.groupId);
        ArrayList<Coordinate> workingData = new ArrayList<Coordinate>();
        while (index < workObject.Data.size())
        {
            Coordinate DataPoint = workObject.Data.get(index);
            workingData.add(DataPoint.GetCopy());

            if ((DataPoint.branchDirection != -1) && (branchGroup != null) && (branchLimit < workObject.branchLimit))
            {
                CustomObject workingBranch = branchGroup.get(rand.nextInt(branchGroup.size()));
                int counter = 0;
                while (counter < workingBranch.Data.size())
                {
                    Coordinate untranslatedCoordinate = workingBranch.Data.get(counter).GetCopy();
                    int directionCounter = 0;
                    while (directionCounter < (DataPoint.branchDirection))
                    {
                        untranslatedCoordinate.Rotate();
                        directionCounter++;
                    }

                    workingData.add(untranslatedCoordinate.GetSumm(DataPoint));
                    counter++;
                }

            }
            index++;
        }

        // 3)
        int RotationAmount = 0;
        index = 0;
        int faultCounter = 0;

        if (workObject.randomRotation)
        {
            RotationAmount = rand.nextInt(4);
        }


        while (index < workingData.size())
        {
            int counter = 0;
            Coordinate point = workingData.get(index);
            while (counter < RotationAmount)
            {
                point.Rotate();
                counter++;
            }
            if (!world.isLoaded(point.getX() + x,point.getY() + y, point.getZ() + z))
                return false;


            if (!workObject.dig)
            {
                if (world.getTypeId((x + point.getX()), (y + point.getY()), (z + point.getZ())) > 0)
                {
                    faultCounter++;
                    if (faultCounter > (workingData.size() * (workObject.collisionPercentage / 100)))
                    {
                        return false;
                    }
                }
            }
            index++;
        }

        // 4)

        index = 0;
        while (index < workingData.size())
        {
            Coordinate DataPoint = workingData.get(index);
            if (world.getTypeId(x + DataPoint.getX(), y + DataPoint.getY(), z + DataPoint.getZ()) == 0)
            {
                ChangeWorld(world, notify, (x + DataPoint.getX()), y + DataPoint.getY(), z + DataPoint.getZ(), DataPoint.workingData, DataPoint.workingExtra);
            } else if (DataPoint.Digs)
            {
                ChangeWorld(world, notify, (x + DataPoint.getX()), y + DataPoint.getY(), z + DataPoint.getZ(), DataPoint.workingData, DataPoint.workingExtra);
            }
            if ((!worldSettings.denyObjectsUnderFill) && (workObject.underFill) && (world.getTypeId(x + DataPoint.getX(), y, z + DataPoint.getZ()) > 0))
            {
                int depthScanner = 0;
                int blockForFill = world.getTypeId(x, y - 1, z);
                while (depthScanner < 64)
                {
                    if (DataPoint.getY() < depthScanner)
                    {
                        int countdown = depthScanner;
                        while ((world.getTypeId(x + DataPoint.getX(), y + DataPoint.getY() - countdown, z + DataPoint.getZ()) == 0) && (countdown < 64))
                        {
                            ChangeWorld(world, notify, (x + DataPoint.getX()), y + DataPoint.getY() - countdown, z + DataPoint.getZ(), blockForFill, 0);
                            countdown++;
                        }
                    }
                    depthScanner++;
                }
            }
            index++;
        }
        return true;

    }


    private static void ChangeWorld(LocalWorld world, boolean notify, int x, int y, int z, int type, int data)
    {
        world.setBlock(x, y, z, type, data, true, false, notify);
    }


    @Override
    protected void SpawnResource(LocalWorld world, Random rand, Resource res, int x, int z)
    {

    }
}
