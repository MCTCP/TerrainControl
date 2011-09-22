package com.Khorn.PTMBukkit.CustomObjects;

import com.Khorn.PTMBukkit.Settings;
import net.minecraft.server.*;
import org.bukkit.Chunk;

import java.util.ArrayList;
import java.util.Random;

public class CustomObjectGen
{
    private Settings WorldSettings;
    private World world;

    public CustomObjectGen(Settings settings, World _world)
    {
        this.world = _world;
        this.WorldSettings = settings;
    }

    private boolean ObjectCanSpawn(int x, int y, int z, CustomObject obj)
    {
        if ((world.getTypeId(x, y - 5, z) == 0) && (obj.needsFoundation))
            return false;

        boolean abort = false;
        int checkBlock = world.getTypeId(x, y + 2, z);
        if (!obj.spawnWater)
            abort = ((checkBlock == Block.WATER.id) || (checkBlock == Block.STATIONARY_WATER.id));
        if (!obj.spawnLava)
            abort = ((checkBlock == Block.LAVA.id) || (checkBlock == Block.STATIONARY_LAVA.id));

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

    boolean SpawnCustomObjects(Random rand, int chunk_x, int chunk_z, BiomeBase localBiomeBase)
    {

        if (this.WorldSettings.Objects.size() == 0)
            return false;

        boolean objectSpawned = false;
        int spawnattemps = 0;
        while (!objectSpawned)
        {
            if (spawnattemps > this.WorldSettings.objectSpawnRatio)
                return false;

            spawnattemps++;

            CustomObject SelectedObject = this.WorldSettings.Objects.get(rand.nextInt(this.WorldSettings.Objects.size()));

            if (SelectedObject.branch || !SelectedObject.canSpawnInBiome(localBiomeBase))
                continue;

            int randomRoll = rand.nextInt(100);
            int ObjectRarity = SelectedObject.rarity;

            while (randomRoll < ObjectRarity)
            {
                int x = chunk_x + rand.nextInt(16);
                int z = chunk_z + rand.nextInt(16);
                int y = this.world.getHighestBlockYAt(x, z);
                ObjectRarity -= 100;

                if (!this.ObjectCanSpawn(x, y, z, SelectedObject))
                    continue;

                objectSpawned = GenerateCustomObject(world,rand,WorldSettings,x, y, z, SelectedObject, false);
                // Checked Biome, Branch, Tree - soo try to generate.

                // here we spawn object and check group spawning

                if (objectSpawned && !SelectedObject.groupId.endsWith(""))
                {
                    ArrayList<CustomObject> groupList = this.WorldSettings.ObjectGroups.get(SelectedObject.groupId);
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
                        if (ObjectFromGroup.branch || !ObjectFromGroup.canSpawnInBiome(localBiomeBase))
                            continue;

                        x = x + rand.nextInt(SelectedObject.groupSeperationMax - SelectedObject.groupSeperationMin) + SelectedObject.groupSeperationMin;
                        z = z + rand.nextInt(SelectedObject.groupSeperationMax - SelectedObject.groupSeperationMin) + SelectedObject.groupSeperationMin;
                        int _y = this.world.getHighestBlockYAt(x, z);
                        if ((y - _y) > 10 || (_y - y) > 10)
                            continue;

                        if (!this.ObjectCanSpawn(x, y, z, ObjectFromGroup))
                            continue;
                        GenerateCustomObject(world,rand,WorldSettings,x, _y, z, ObjectFromGroup, false);


                    }

                }

            }

        }
        return objectSpawned;
    }


    public boolean SpawnCustomTrees(Random rand, int x, int y, int z)
    {

        if (!this.WorldSettings.HasCustomTrees)
            return false;
        if (this.world.getTypeId(x, y, z) != Block.SAPLING.id)
            return false;

        int oldData = this.world.getData(x, y, z) & 0x3;
        this.world.setRawTypeId(x, y, z, 0);

        Chunk chunk = this.world.getWorld().getChunkAt(x >> 4, z >> 4);

        BiomeBase localBiomeBase = this.world.getWorldChunkManager().getBiome(chunk.getX() * 16 + 16, chunk.getZ() * 16 + 16);

        boolean objectSpawned = false;
        int spawnattemps = 0;
        while (!objectSpawned && spawnattemps < this.WorldSettings.objectSpawnRatio)
        {
            CustomObject SelectedObject = this.WorldSettings.Objects.get(rand.nextInt(this.WorldSettings.Objects.size()));

            spawnattemps++;

            if (SelectedObject.branch || !SelectedObject.canSpawnInBiome(localBiomeBase) || !SelectedObject.tree)
                continue;


            int randomRoll = rand.nextInt(100);

            if (randomRoll < SelectedObject.rarity)
            {
                if (this.ObjectCanSpawn(x, y, z, SelectedObject))
                    objectSpawned = GenerateCustomObject(world,rand,WorldSettings,x, y, z, SelectedObject, true);
            }

        }
        if (!objectSpawned)
            this.world.setRawTypeIdAndData(x, y, z, Block.SAPLING.id, oldData);
        return objectSpawned;
    }

    public static boolean GenerateCustomObject(World world, Random rand, Settings WorldSettings, int x, int y, int z, CustomObject workObject, boolean notify)
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
        ArrayList<CustomObject> branchGroup = WorldSettings.BranchGroups.get(workObject.groupId);
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
                        untranslatedCoordinate.rotateSliceC();
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
            RotationAmount = rand.nextInt(3);
        }
        ChunkProviderServer chunkServer = (ChunkProviderServer) world.chunkProvider;
        net.minecraft.server.Chunk workChunk = chunkServer.chunks.get(workingData.get(0).getChunkX(), workingData.get(0).getChunkZ());
        if (workChunk == null)
            return false;

        while (index < workingData.size())
        {
            int counter = 0;
            Coordinate point = workingData.get(index);
            while (counter < RotationAmount)
            {
                point.rotateSliceC();
                counter++;
            }
            if (workChunk.x != point.getChunkX() || workChunk.z != point.getChunkZ())
                workChunk = chunkServer.chunks.get(point.getChunkX(), point.getChunkZ());

            if (workChunk == null)
                return false;

            if ((y + point.getY() > 127) || (y + point.getY() < 1))
            {
                return false;
            }

            if (!workObject.dig)
            {
                if (workChunk.getTypeId((x + point.getX()) & 0xF, (y + point.getY()) & 0xF, (z + point.getZ()) & 0xF) > 0)
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
                ChangeWorld(world,notify, (x + DataPoint.getX()), y + DataPoint.getY(), z + DataPoint.getZ(), DataPoint.workingData, DataPoint.workingExtra);
            } else if (DataPoint.Digs)
            {
                ChangeWorld(world,notify, (x + DataPoint.getX()), y + DataPoint.getY(), z + DataPoint.getZ(), DataPoint.workingData, DataPoint.workingExtra);
            }
            if ((!WorldSettings.denyObjectsUnderFill) && (workObject.underFill) && (world.getTypeId(x + DataPoint.getX(), y, z + DataPoint.getZ()) > 0))
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
                            ChangeWorld(world,notify, (x + DataPoint.getX()), y + DataPoint.getY() - countdown, z + DataPoint.getZ(), blockForFill, 0);
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


    private  static  boolean ChangeWorld(World world, boolean notify, int x, int y, int z, int type, int data)
    {
        if (notify)
            return world.setTypeIdAndData(x, y, z, type, data);
        else
            return world.setRawTypeIdAndData(x, y, z, type, data);

    }



}
