package com.Khorn.PTMBukkit;

import net.minecraft.server.*;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.generator.BlockPopulator;

import java.util.ArrayList;
import java.util.Random;

public class ObjectSpawner extends BlockPopulator
{



    public NoiseGeneratorOctaves c;



    private WorldWorker WorldSettings;
    private Random rand;
    public World world;

    public ObjectSpawner(WorldWorker wrk)
    {
        this.WorldSettings = wrk;


    }



    private boolean ObjectCantSpawn(CustomObject obj, BiomeBase localBiomeBase, boolean isTree)
    {
        boolean attemptSpawn = false;

        if ((obj.spawnInBiome.size() == 0) || (obj.spawnInBiome.contains("All")))
        {
            attemptSpawn = true;
        } else if (obj.spawnInBiome.contains(localBiomeBase.n.toLowerCase()))
            attemptSpawn = true;

        return obj.branch || !attemptSpawn || (isTree && !obj.tree);
    }

    private void SpawnLegacyObject(int x, int y, int z)
    {

        int SelectedObject = this.rand.nextInt(this.WorldSettings.LegacyObjects.size());
        CustomObjectLegacy legacyObject = this.WorldSettings.LegacyObjects.get(SelectedObject);

        int i1 = world.getTypeId(x, y - 1, z);
        if ((y < 128 - 1))
        {

            if ((i1 == Block.GRASS.id))
            {
                ChangeWorld(x, y - 1, z, Block.DIRT.id);
            }
            if ((world.getTypeId(x, y + 2, z) != Block.WATER.id) || (legacyObject.underwater))
            {

                int j1 = world.getTypeId(x, y - 1, z);
                if ((j1 == legacyObject.spawnID))
                {
                    int DataX = 0;
                    int DataY = 0;
                    int DataZ = 0;
                    while (DataZ <= 100)
                    {
                        while (DataY <= 14)
                        {
                            while (DataX <= 14)
                            {
                                if (legacyObject.DataValues[DataX][DataY][DataZ] != 0)
                                {

                                    int Data;
                                    Data = (int) legacyObject.DataValues[DataX][DataY][DataZ];
                                    int eData = (int) Math.round(((legacyObject.DataValues[DataX][DataY][DataZ] - Data) * 100));
                                    if (!(Data == 0))
                                    {
                                        ChangeWorld(false, x + (DataX - 4), (y - 1) + (DataZ), z + (DataY - 4), Data, eData);
                                        if (DataZ == 0)
                                        {
                                            int DownChecker = 1;
                                            while (world.getTypeId(x + (DataX - 4), (y - DownChecker), z + (DataY - 4)) == 0)
                                            {
                                                ChangeWorld(x + (DataX - 4), (y - DownChecker), z + (DataY - 4), (byte) legacyObject.spawnID);
                                            }
                                        }
                                    }
                                }
                                DataX = DataX + 1;
                            }
                            DataX = 0;
                            DataY = DataY + 1;
                        }
                        DataY = 0;
                        DataZ = DataZ + 1;
                    }
                }

            }

        }

    }


    boolean SpawnCustomObjects(int chunk_x, int chunk_z, BiomeBase localBiomeBase)
    {


        if (this.WorldSettings.LegacyObjects.size() > 0 && this.rand.nextInt(2) == 1)
        {
            int x = chunk_x + this.rand.nextInt(16) + 8;
            int z = chunk_z + this.rand.nextInt(16) + 8;
            int y = this.world.getHighestBlockYAt(x, z);
            SpawnLegacyObject(x, y, z);
            return true;
        }

        if (this.WorldSettings.Objects.size() == 0)
            return false;

        boolean objectSpawned = false;
        int spawnattemps = 0;
        while (!objectSpawned)
        {
            if (spawnattemps > this.WorldSettings.objectSpawnRatio)
                return false;

            spawnattemps++;

            CustomObject SelectedObject = this.WorldSettings.Objects.get(this.rand.nextInt(this.WorldSettings.Objects.size()));

            if (ObjectCantSpawn(SelectedObject, localBiomeBase, false))
                continue;

            int randomRoll = this.rand.nextInt(100);
            int ObjectRarity = SelectedObject.rarity;

            while (randomRoll < ObjectRarity)
            {
                int x = chunk_x + this.rand.nextInt(16) + 8;
                int z = chunk_z + this.rand.nextInt(16) + 8;
                int y = this.world.getHighestBlockYAt(x, z);
                ObjectRarity -= 100;

                objectSpawned = GenerateCustomObject(x, y, z, SelectedObject, false);
                // Checked Biome, Branch, Tree - soo try to generate.

                // here we spawn object and check group spawning

                if (objectSpawned && !SelectedObject.groupId.endsWith(""))
                {
                    ArrayList<CustomObject> groupList = this.WorldSettings.ObjectGroups.get(SelectedObject.groupId);
                    if (groupList == null)
                        return objectSpawned;

                    int attempts = 3;
                    if ((SelectedObject.groupFrequencyMax - SelectedObject.groupFrequencyMin) > 0)
                        attempts = SelectedObject.groupFrequencyMin + this.rand.nextInt(SelectedObject.groupFrequencyMax - SelectedObject.groupFrequencyMin);

                    while (attempts > 0)
                    {
                        attempts--;

                        int objIndex = this.rand.nextInt(groupList.size());
                        CustomObject ObjectfromGroup = groupList.get(objIndex);

                        // duno about this check, but maybe it is correct
                        if (ObjectCantSpawn(ObjectfromGroup, localBiomeBase, false))
                            continue;

                        x = x + this.rand.nextInt(SelectedObject.groupSeperationMax - SelectedObject.groupSeperationMin) + SelectedObject.groupSeperationMin;
                        z = z + this.rand.nextInt(SelectedObject.groupSeperationMax - SelectedObject.groupSeperationMin) + SelectedObject.groupSeperationMin;
                        int _y = this.world.getHighestBlockYAt(x, z);
                        if ((y - _y) > 10 || (_y - y) > 10)
                            continue;
                        GenerateCustomObject(x, _y, z, ObjectfromGroup, false);


                    }

                }

            }

        }
        return objectSpawned;
    }


    public boolean SpawnCustomTrees(int x, int y, int z)
    {

        if (!this.WorldSettings.HasCustomTrees)
            return false;

        BiomeBase localBiomeBase = this.world.getWorldChunkManager().getBiome(x, z);

        boolean objectSpawned = false;
        int spawnattemps = 0;
        while (!objectSpawned)
        {
            if (spawnattemps > this.WorldSettings.objectSpawnRatio)
                return false;


            CustomObject SelectedObject = this.WorldSettings.Objects.get(this.rand.nextInt(this.WorldSettings.Objects.size()));

            if (ObjectCantSpawn(SelectedObject, localBiomeBase, true))
                continue;

            spawnattemps++;

            int randomRoll = this.rand.nextInt(100);

            if (randomRoll < SelectedObject.rarity)
            {

                objectSpawned = GenerateCustomObject(x, y, z, SelectedObject, true);
            }

        }
        return objectSpawned;
    }

    private boolean GenerateCustomObject(int x, int y, int z, CustomObject workObject, boolean notify)
    {
        /*
         * 1) ground check 
         * 2) add branches and copy all data to work array (dont change default CustomObject object) 
         * 3) collision check and rotation 
         * 4) spawn
         */

        // 1)
        if ((world.getTypeId(x, y - 5, z) == 0) && (workObject.needsFoundation))
            return false;

        boolean abort = false;
        int checkblock = world.getTypeId(x, y + 2, z);
        if (!workObject.spawnWater)
            abort = ((checkblock == Block.WATER.id) || (checkblock == Block.STATIONARY_WATER.id));
        if (!workObject.spawnLava)
            abort = ((checkblock == Block.LAVA.id) || (checkblock == Block.STATIONARY_LAVA.id));

        checkblock = world.getLightLevel(x, y + 2, z);
        if (!workObject.spawnSunlight)
            abort = (checkblock > 8);
        if (!workObject.spawnDarkness)
            abort = (checkblock < 9);

        if ((y < workObject.spawnElevationMin) || (y > workObject.spawnElevationMax))
            abort = true;

        if (!workObject.spawnOnBlockType.contains(world.getTypeId(x, y - 1, z)))
            abort = true;

        if (abort)
            return false;

        // 2)

        int index = 0;
        int branchLimit = 0;
        ArrayList<CustomObject> branchGroup = this.WorldSettings.BranchGroups.get(workObject.groupId);
        ArrayList<Coordinate> workingData = new ArrayList<Coordinate>();
        while (index < workObject.Data.size())
        {
            Coordinate DataPoint = workObject.Data.get(index);
            workingData.add(DataPoint.GetCopy());

            if ((DataPoint.branchDirection != -1) && (branchGroup != null) && (branchLimit < workObject.branchLimit))
            {
                CustomObject workingBranch = branchGroup.get(this.rand.nextInt(branchGroup.size()));
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
            RotationAmount = this.rand.nextInt(3);
        }

        while (index < workingData.size())
        {
            int counter = 0;
            while (counter < RotationAmount)
            {
                workingData.get(index).rotateSliceC();
                counter++;
            }
            if (!workObject.dig)
            {
                if (world.getTypeId(x + workingData.get(index).getX(), y + workingData.get(index).getY(), z + workingData.get(index).getZ()) > 0)
                {
                    faultCounter++;
                    if (faultCounter > (workingData.size() * (workObject.collisionPercentage / 100)))
                    {
                        return false;
                    }
                }
            }
            if ((y + workingData.get(index).getY() > 127) || (y + workingData.get(index).getY() < 1))
            {
                return false;
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
                ChangeWorld(notify, (x + DataPoint.getX()), y + DataPoint.getY(), z + DataPoint.getZ(), DataPoint.workingData, DataPoint.workingExtra);
            } else if (DataPoint.Digs)
            {
                ChangeWorld(notify, (x + DataPoint.getX()), y + DataPoint.getY(), z + DataPoint.getZ(), DataPoint.workingData, DataPoint.workingExtra);
            }
            if ((!this.WorldSettings.denyObjectsUnderFill) && (workObject.underFill) && (world.getTypeId(x + DataPoint.getX(), y, z + DataPoint.getZ()) > 0))
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
                            ChangeWorld(notify, (x + DataPoint.getX()), y + DataPoint.getY() - countdown, z + DataPoint.getZ(), blockForFill, 0);
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


    private boolean ChangeWorld(int x, int y, int z, int type)
    {
        return world.setRawTypeId(x, y, z, type);

    }

    private boolean ChangeWorld(boolean notify, int x, int y, int z, int type, int data)
    {
        if (notify)
            return world.setTypeIdAndData(x, y, z, type, data);
        else
            return world.setRawTypeIdAndData(x, y, z, type, data);

    }

    void processAboveGroundMaterials(int x, int z, BiomeBase currentBiome)
    {
        processDepositMaterial(x, z, this.WorldSettings.flowerDepositRarity, this.WorldSettings.flowerDepositFrequency, this.WorldSettings.flowerDepositMinAltitude, this.WorldSettings.flowerDepositMaxAltitude, -1, Block.YELLOW_FLOWER.id);

        processDepositMaterial(x, z, this.WorldSettings.roseDepositRarity, this.WorldSettings.roseDepositFrequency, this.WorldSettings.roseDepositMinAltitude, this.WorldSettings.roseDepositMaxAltitude, -1, Block.RED_ROSE.id);

        processDepositMaterial(x, z, this.WorldSettings.brownMushroomDepositRarity, this.WorldSettings.brownMushroomDepositFrequency, this.WorldSettings.brownMushroomDepositMinAltitude, this.WorldSettings.brownMushroomDepositMaxAltitude, -1, Block.BROWN_MUSHROOM.id);

        processDepositMaterial(x, z, this.WorldSettings.redMushroomDepositRarity, this.WorldSettings.redMushroomDepositFrequency, this.WorldSettings.redMushroomDepositMinAltitude, this.WorldSettings.redMushroomDepositMaxAltitude, -1, Block.RED_MUSHROOM.id);

        processDepositMaterial(x, z, this.WorldSettings.reedDepositRarity, this.WorldSettings.reedDepositFrequency, this.WorldSettings.reedDepositMinAltitude, this.WorldSettings.reedDepositMaxAltitude, -1, Block.SUGAR_CANE_BLOCK.id);

        processDepositMaterial(x, z, this.WorldSettings.pumpkinDepositRarity, this.WorldSettings.pumpkinDepositFrequency, this.WorldSettings.pumpkinDepositMinAltitude, this.WorldSettings.pumpkinDepositMaxAltitude, -1, Block.PUMPKIN.id);

        processDepositMaterial(x, z, this.WorldSettings.waterSourceDepositRarity, this.WorldSettings.waterSourceDepositFrequency, this.WorldSettings.waterSourceDepositMinAltitude, this.WorldSettings.waterSourceDepositMaxAltitude, -1, Block.WATER.id);

        processDepositMaterial(x, z, this.WorldSettings.lavaSourceDepositRarity, this.WorldSettings.lavaSourceDepositFrequency, this.WorldSettings.lavaSourceDepositMinAltitude, this.WorldSettings.lavaSourceDepositMaxAltitude, -1, Block.LAVA.id);

        processDepositMaterial(x, z, this.WorldSettings.cactusDepositRarity, this.WorldSettings.globalCactusDensity + (currentBiome == BiomeBase.DESERT ? this.WorldSettings.desertCactusDensity : 0), this.WorldSettings.cactusDepositMinAltitude, this.WorldSettings.cactusDepositMaxAltitude, -1, Block.CACTUS.id);
    }


    void processTrees(int x, int z, BiomeBase currentBiome)
    {
        if (!this.WorldSettings.notchBiomeTrees)
            return;
        double d1 = 0.5D;
        int treeDensity = 0;
        int treeDensityVariation = (int) ((this.c.a(x * d1, z * d1) / 8.0D + this.rand.nextDouble() * 4.0D + 4.0D) / 3.0D);

        if (this.rand.nextInt(10) == 0)
            treeDensity++;

        if (currentBiome == BiomeBase.RAINFOREST)
            treeDensity += treeDensityVariation + this.WorldSettings.rainforestTreeDensity;
        if (currentBiome == BiomeBase.SWAMPLAND)
            treeDensity += treeDensityVariation + this.WorldSettings.swamplandTreeDensity;
        if (currentBiome == BiomeBase.SEASONAL_FOREST)
            treeDensity += treeDensityVariation + this.WorldSettings.seasonalforestTreeDensity;
        if (currentBiome == BiomeBase.FOREST)
            treeDensity += treeDensityVariation + this.WorldSettings.forestTreeDensity;
        if (currentBiome == BiomeBase.SAVANNA)
            treeDensity += treeDensityVariation + this.WorldSettings.savannaTreeDensity;
        if (currentBiome == BiomeBase.SHRUBLAND)
            treeDensity += treeDensityVariation + this.WorldSettings.shrublandTreeDensity;
        if (currentBiome == BiomeBase.TAIGA)
            treeDensity += treeDensityVariation + this.WorldSettings.taigaTreeDensity;
        if (currentBiome == BiomeBase.DESERT)
            treeDensity += treeDensityVariation + this.WorldSettings.desertTreeDensity;
        if (currentBiome == BiomeBase.PLAINS)
            treeDensity += treeDensityVariation + this.WorldSettings.plainsTreeDensity;
        if (currentBiome == BiomeBase.ICE_DESERT)
            treeDensity += treeDensityVariation + this.WorldSettings.iceDesertTreeDensity;
        if (currentBiome == BiomeBase.TUNDRA)
            treeDensity += treeDensityVariation + this.WorldSettings.tundraTreeDensity;


        for (int i = 0; i < treeDensity; i++)
        {

            int _x = x + this.rand.nextInt(16) + 8;
            int _z = z + this.rand.nextInt(16) + 8;

            WorldGenerator localWorldGenerator = currentBiome.a(this.rand);
            localWorldGenerator.a(1.0D, 1.0D, 1.0D);
            localWorldGenerator.a(this.world, this.rand, _x, this.world.getHighestBlockYAt(_x, _z), _z);


        }

    }

    void processDepositMaterial(int _x, int _z, int rarity, int frequency, int minAltitude, int maxAltitude, int size, int type)
    {
        Random rand = this.rand;
        int xyPosMod = (type == Block.LONG_GRASS.id) || (type == Block.YELLOW_FLOWER.id) || (type == Block.RED_ROSE.id) || (type == Block.BROWN_MUSHROOM.id) || (type == Block.RED_MUSHROOM.id) || (type == Block.WATER.id) || (type == Block.LAVA.id) || (type == Block.FIRE.id) || (type == Block.CACTUS.id) || (type == Block.MOB_SPAWNER.id) || (type == Block.SUGAR_CANE_BLOCK.id) || (type == Block.PUMPKIN.id) || (type == Block.GLOWSTONE.id) ? 8 : 0;

        if ((type == Block.FIRE.id))
            frequency = rand.nextInt(rand.nextInt(frequency) + 1) + 1;
        else if ((type == Block.GLOWSTONE.id) && (size == -1))
        {
            frequency = rand.nextInt(rand.nextInt(frequency) + 1);
        }
        for (int i = 0; i < frequency; i++)
        {
            if (rand.nextInt(100) >= rarity)
                continue;
            int x = _x + rand.nextInt(16) + xyPosMod;
            int z = _z + rand.nextInt(16) + xyPosMod;
            int y = rand.nextInt(maxAltitude - minAltitude) + minAltitude;

            if ((type == Block.YELLOW_FLOWER.id) || (type == Block.RED_ROSE.id || (type == Block.BROWN_MUSHROOM.id) || (type == Block.RED_MUSHROOM.id)))
                new WorldGenFlowers(type).a(this.world, rand, x, y, z);
            else if (type == Block.CACTUS.id)
                new WorldGenCactus().a(this.world, rand, x, y, z);
            else if (type == Block.SUGAR_CANE_BLOCK.id)
                new WorldGenReed().a(this.world, rand, x, y, z);
            else if (type == Block.PUMPKIN.id)
                new WorldGenPumpkin().a(this.world, rand, x, y, z);
            else if (type == Block.CLAY.id)
                new WorldGenClay(size).a(this.world, rand, x, y, z);
            else if (type == Block.WATER.id)
            {
                if (!this.WorldSettings.evenWaterSourceDistribution)
                    y = rand.nextInt(rand.nextInt(maxAltitude - minAltitude) + minAltitude + 1);
                new WorldGenLiquids(type).a(this.world, rand, x, y, z);
            } else if (type == Block.LAVA.id)
            {
                if (!this.WorldSettings.evenLavaSourceDistribution)
                    y = rand.nextInt(rand.nextInt(maxAltitude - minAltitude) + minAltitude + 1);
                new WorldGenLiquids(type).a(this.world, rand, x, y, z);
            } else if (type == Block.MOB_SPAWNER.id)
                new WorldGenDungeons().a(this.world, rand, x, y, z);
            else
                new WorldGenMinable(type, size).a(this.world, rand, x, y, z);
        }
    }

    void processGrass(int x, int z, BiomeBase currentBiome)
    {
        int grassDensity = 0;
        if (currentBiome == BiomeBase.FOREST)
            grassDensity = 2;
        if (currentBiome == BiomeBase.RAINFOREST)
            grassDensity = 10;
        if (currentBiome == BiomeBase.SEASONAL_FOREST)
            grassDensity = 2;
        if (currentBiome == BiomeBase.TAIGA)
            grassDensity = 1;
        if (currentBiome == BiomeBase.PLAINS)
            grassDensity = 10;

        int _x;
        int _y;
        int _z;

        for (int i = 0; i < grassDensity; i++)
        {
            int grassType = 1;

            if ((currentBiome == BiomeBase.RAINFOREST) && (this.rand.nextInt(3) != 0))
                grassType = 2;

            _x = x + this.rand.nextInt(16) + 8;
            _y = this.rand.nextInt(128);
            _z = z + this.rand.nextInt(16) + 8;
            new WorldGenGrass(Block.LONG_GRASS.id, grassType).a(this.world, this.rand, _x, _y, _z);
        }

        if (currentBiome == BiomeBase.DESERT)
        {

            for (int i = 0; i < 2; i++)
            {
                _x = x + this.rand.nextInt(16) + 8;
                _y = this.rand.nextInt(128);
                _z = z + this.rand.nextInt(16) + 8;
                new WorldGenDeadBush(Block.DEAD_BUSH.id).a(this.world, this.rand, _x, _y, _z);
            }
        }
    }

    void processUndergroundDeposits(int x, int z, BiomeBase currentBiome)
    {

        processDepositMaterial(x, z, this.WorldSettings.dungeonRarity, this.WorldSettings.dungeonFrequency, this.WorldSettings.dungeonMinAltitude, this.WorldSettings.dungeonMaxAltitude, -1, Block.MOB_SPAWNER.id);

        processDepositMaterial(x, z, this.WorldSettings.dirtDepositRarity1, this.WorldSettings.dirtDepositFrequency1, this.WorldSettings.dirtDepositMinAltitude1, this.WorldSettings.dirtDepositMaxAltitude1, this.WorldSettings.dirtDepositSize1, Block.DIRT.id);

        processDepositMaterial(x, z, this.WorldSettings.dirtDepositRarity2, this.WorldSettings.dirtDepositFrequency2, this.WorldSettings.dirtDepositMinAltitude2, this.WorldSettings.dirtDepositMaxAltitude2, this.WorldSettings.dirtDepositSize2, Block.DIRT.id);

        processDepositMaterial(x, z, this.WorldSettings.dirtDepositRarity3, this.WorldSettings.dirtDepositFrequency3, this.WorldSettings.dirtDepositMinAltitude3, this.WorldSettings.dirtDepositMaxAltitude3, this.WorldSettings.dirtDepositSize3, Block.DIRT.id);

        processDepositMaterial(x, z, this.WorldSettings.dirtDepositRarity4, this.WorldSettings.dirtDepositFrequency4, this.WorldSettings.dirtDepositMinAltitude4, this.WorldSettings.dirtDepositMaxAltitude4, this.WorldSettings.dirtDepositSize4, Block.DIRT.id);

        processDepositMaterial(x, z, this.WorldSettings.gravelDepositRarity1, this.WorldSettings.gravelDepositFrequency1, this.WorldSettings.gravelDepositMinAltitude1, this.WorldSettings.gravelDepositMaxAltitude1, this.WorldSettings.gravelDepositSize1, Block.GRAVEL.id);

        processDepositMaterial(x, z, this.WorldSettings.gravelDepositRarity2, this.WorldSettings.gravelDepositFrequency2, this.WorldSettings.gravelDepositMinAltitude2, this.WorldSettings.gravelDepositMaxAltitude2, this.WorldSettings.gravelDepositSize2, Block.GRAVEL.id);

        processDepositMaterial(x, z, this.WorldSettings.gravelDepositRarity3, this.WorldSettings.gravelDepositFrequency3, this.WorldSettings.gravelDepositMinAltitude3, this.WorldSettings.gravelDepositMaxAltitude3, this.WorldSettings.gravelDepositSize3, Block.GRAVEL.id);

        processDepositMaterial(x, z, this.WorldSettings.gravelDepositRarity4, this.WorldSettings.gravelDepositFrequency4, this.WorldSettings.gravelDepositMinAltitude4, this.WorldSettings.gravelDepositMaxAltitude4, this.WorldSettings.gravelDepositSize4, Block.GRAVEL.id);

        processDepositMaterial(x, z, this.WorldSettings.clayDepositRarity1, this.WorldSettings.clayDepositFrequency1, this.WorldSettings.clayDepositMinAltitude1, this.WorldSettings.clayDepositMaxAltitude1, this.WorldSettings.clayDepositSize1, Block.CLAY.id);

        processDepositMaterial(x, z, this.WorldSettings.clayDepositRarity2, this.WorldSettings.clayDepositFrequency2, this.WorldSettings.clayDepositMinAltitude2, this.WorldSettings.clayDepositMaxAltitude2, this.WorldSettings.clayDepositSize2, Block.CLAY.id);

        processDepositMaterial(x, z, this.WorldSettings.clayDepositRarity3, this.WorldSettings.clayDepositFrequency3, this.WorldSettings.clayDepositMinAltitude3, this.WorldSettings.clayDepositMaxAltitude3, this.WorldSettings.clayDepositSize3, Block.CLAY.id);

        processDepositMaterial(x, z, this.WorldSettings.clayDepositRarity4, this.WorldSettings.clayDepositFrequency4, this.WorldSettings.clayDepositMinAltitude4, this.WorldSettings.clayDepositMaxAltitude4, this.WorldSettings.clayDepositSize4, Block.CLAY.id);

        processDepositMaterial(x, z, this.WorldSettings.coalDepositRarity1, this.WorldSettings.coalDepositFrequency1, this.WorldSettings.coalDepositMinAltitude1, this.WorldSettings.coalDepositMaxAltitude1, this.WorldSettings.coalDepositSize1, Block.COAL_ORE.id);

        processDepositMaterial(x, z, this.WorldSettings.coalDepositRarity2, this.WorldSettings.coalDepositFrequency2, this.WorldSettings.coalDepositMinAltitude2, this.WorldSettings.coalDepositMaxAltitude2, this.WorldSettings.coalDepositSize2, Block.COAL_ORE.id);

        processDepositMaterial(x, z, this.WorldSettings.coalDepositRarity3, this.WorldSettings.coalDepositFrequency3, this.WorldSettings.coalDepositMinAltitude3, this.WorldSettings.coalDepositMaxAltitude3, this.WorldSettings.coalDepositSize3, Block.COAL_ORE.id);

        processDepositMaterial(x, z, this.WorldSettings.coalDepositRarity4, this.WorldSettings.coalDepositFrequency4, this.WorldSettings.coalDepositMinAltitude4, this.WorldSettings.coalDepositMaxAltitude4, this.WorldSettings.coalDepositSize4, Block.COAL_ORE.id);

        processDepositMaterial(x, z, this.WorldSettings.ironDepositRarity1, this.WorldSettings.ironDepositFrequency1, this.WorldSettings.ironDepositMinAltitude1, this.WorldSettings.ironDepositMaxAltitude1, this.WorldSettings.ironDepositSize1, Block.IRON_ORE.id);

        processDepositMaterial(x, z, this.WorldSettings.ironDepositRarity2, this.WorldSettings.ironDepositFrequency2, this.WorldSettings.ironDepositMinAltitude2, this.WorldSettings.ironDepositMaxAltitude2, this.WorldSettings.ironDepositSize2, Block.IRON_ORE.id);

        processDepositMaterial(x, z, this.WorldSettings.ironDepositRarity3, this.WorldSettings.ironDepositFrequency3, this.WorldSettings.ironDepositMinAltitude3, this.WorldSettings.ironDepositMaxAltitude3, this.WorldSettings.ironDepositSize3, Block.IRON_ORE.id);

        processDepositMaterial(x, z, this.WorldSettings.ironDepositRarity4, this.WorldSettings.ironDepositFrequency4, this.WorldSettings.ironDepositMinAltitude4, this.WorldSettings.ironDepositMaxAltitude4, this.WorldSettings.ironDepositSize4, Block.IRON_ORE.id);

        processDepositMaterial(x, z, this.WorldSettings.goldDepositRarity1, this.WorldSettings.goldDepositFrequency1, this.WorldSettings.goldDepositMinAltitude1, this.WorldSettings.goldDepositMaxAltitude1, this.WorldSettings.goldDepositSize1, Block.GOLD_ORE.id);

        processDepositMaterial(x, z, this.WorldSettings.goldDepositRarity2, this.WorldSettings.goldDepositFrequency2, this.WorldSettings.goldDepositMinAltitude2, this.WorldSettings.goldDepositMaxAltitude2, this.WorldSettings.goldDepositSize2, Block.GOLD_ORE.id);

        processDepositMaterial(x, z, this.WorldSettings.goldDepositRarity3, this.WorldSettings.goldDepositFrequency3, this.WorldSettings.goldDepositMinAltitude3, this.WorldSettings.goldDepositMaxAltitude3, this.WorldSettings.goldDepositSize3, Block.GOLD_ORE.id);

        processDepositMaterial(x, z, this.WorldSettings.goldDepositRarity4, this.WorldSettings.goldDepositFrequency4, this.WorldSettings.goldDepositMinAltitude4, this.WorldSettings.goldDepositMaxAltitude4, this.WorldSettings.goldDepositSize4, Block.GOLD_ORE.id);

        processDepositMaterial(x, z, this.WorldSettings.redstoneDepositRarity1, this.WorldSettings.redstoneDepositFrequency1, this.WorldSettings.redstoneDepositMinAltitude1, this.WorldSettings.redstoneDepositMaxAltitude1, this.WorldSettings.redstoneDepositSize1, Block.REDSTONE_ORE.id);

        processDepositMaterial(x, z, this.WorldSettings.redstoneDepositRarity2, this.WorldSettings.redstoneDepositFrequency2, this.WorldSettings.redstoneDepositMinAltitude2, this.WorldSettings.redstoneDepositMaxAltitude2, this.WorldSettings.redstoneDepositSize2, Block.REDSTONE_ORE.id);

        processDepositMaterial(x, z, this.WorldSettings.redstoneDepositRarity3, this.WorldSettings.redstoneDepositFrequency3, this.WorldSettings.redstoneDepositMinAltitude3, this.WorldSettings.redstoneDepositMaxAltitude3, this.WorldSettings.redstoneDepositSize3, Block.REDSTONE_ORE.id);

        processDepositMaterial(x, z, this.WorldSettings.redstoneDepositRarity4, this.WorldSettings.redstoneDepositFrequency4, this.WorldSettings.redstoneDepositMinAltitude4, this.WorldSettings.redstoneDepositMaxAltitude4, this.WorldSettings.redstoneDepositSize4, Block.REDSTONE_ORE.id);

        processDepositMaterial(x, z, this.WorldSettings.diamondDepositRarity1, this.WorldSettings.diamondDepositFrequency1, this.WorldSettings.diamondDepositMinAltitude1, this.WorldSettings.diamondDepositMaxAltitude1, this.WorldSettings.diamondDepositSize1, Block.DIAMOND_ORE.id);

        processDepositMaterial(x, z, this.WorldSettings.diamondDepositRarity2, this.WorldSettings.diamondDepositFrequency2, this.WorldSettings.diamondDepositMinAltitude2, this.WorldSettings.diamondDepositMaxAltitude2, this.WorldSettings.diamondDepositSize2, Block.DIAMOND_ORE.id);

        processDepositMaterial(x, z, this.WorldSettings.diamondDepositRarity3, this.WorldSettings.diamondDepositFrequency3, this.WorldSettings.diamondDepositMinAltitude3, this.WorldSettings.diamondDepositMaxAltitude3, this.WorldSettings.diamondDepositSize3, Block.DIAMOND_ORE.id);

        processDepositMaterial(x, z, this.WorldSettings.diamondDepositRarity4, this.WorldSettings.diamondDepositFrequency4, this.WorldSettings.diamondDepositMinAltitude4, this.WorldSettings.diamondDepositMaxAltitude4, this.WorldSettings.diamondDepositSize4, Block.DIAMOND_ORE.id);

        processDepositMaterial(x, z, this.WorldSettings.lapislazuliDepositRarity1, this.WorldSettings.lapislazuliDepositFrequency1, this.WorldSettings.lapislazuliDepositMinAltitude1, this.WorldSettings.lapislazuliDepositMaxAltitude1, this.WorldSettings.lapislazuliDepositSize1, Block.LAPIS_ORE.id);

        processDepositMaterial(x, z, this.WorldSettings.lapislazuliDepositRarity2, this.WorldSettings.lapislazuliDepositFrequency2, this.WorldSettings.lapislazuliDepositMinAltitude2, this.WorldSettings.lapislazuliDepositMaxAltitude2, this.WorldSettings.lapislazuliDepositSize2, Block.LAPIS_ORE.id);

        processDepositMaterial(x, z, this.WorldSettings.lapislazuliDepositRarity3, this.WorldSettings.lapislazuliDepositFrequency3, this.WorldSettings.lapislazuliDepositMinAltitude3, this.WorldSettings.lapislazuliDepositMaxAltitude3, this.WorldSettings.lapislazuliDepositSize3, Block.LAPIS_ORE.id);

        processDepositMaterial(x, z, this.WorldSettings.lapislazuliDepositRarity4, this.WorldSettings.lapislazuliDepositFrequency4, this.WorldSettings.lapislazuliDepositMinAltitude4, this.WorldSettings.lapislazuliDepositMaxAltitude4, this.WorldSettings.lapislazuliDepositSize4, Block.LAPIS_ORE.id);


        if (this.WorldSettings.undergroundLakes)
            processUndergroundLakes(x, z);
    }


    void processUndergroundLakes(int x, int z)
    {
        for (int i = 0; i < this.WorldSettings.undergroundLakeFrequency; i++)
        {
            if (this.rand.nextInt(100) >= this.WorldSettings.undergroundLakeRarity)
                continue;
            int xR = x + this.rand.nextInt(16);
            int yR = this.rand.nextInt(this.WorldSettings.undergroundLakeMaxAltitude - this.WorldSettings.undergroundLakeMinAltitude) + this.WorldSettings.undergroundLakeMinAltitude;
            int zR = z + this.rand.nextInt(16);
            createUndergroundLake(this.rand.nextInt(this.WorldSettings.undergroundLakeMaxSize - this.WorldSettings.undergroundLakeMinSize) + this.WorldSettings.undergroundLakeMinSize, xR, yR, zR);
        }
    }

    private void createUndergroundLake(int size, int x, int y, int z)
    {
        float mPi = this.rand.nextFloat() * 3.141593F;

        double x1 = x + 8 + MathHelper.sin(mPi) * size / 8.0F;
        double x2 = x + 8 - MathHelper.sin(mPi) * size / 8.0F;
        double z1 = z + 8 + MathHelper.cos(mPi) * size / 8.0F;
        double z2 = z + 8 - MathHelper.cos(mPi) * size / 8.0F;

        double y1 = y + this.rand.nextInt(3) + 2;
        double y2 = y + this.rand.nextInt(3) + 2;

        for (int i = 0; i <= size; i++)
        {
            double xAdjusted = x1 + (x2 - x1) * i / size;
            double yAdjusted = y1 + (y2 - y1) * i / size;
            double zAdjusted = z1 + (z2 - z1) * i / size;

            double horizontalSizeMultiplier = this.rand.nextDouble() * size / 16.0D;
            double verticalSizeMultiplier = this.rand.nextDouble() * size / 32.0D;
            double horizontalSize = (MathHelper.sin(i * 3.141593F / size) + 1.0F) * horizontalSizeMultiplier + 1.0D;
            double verticalSize = (MathHelper.sin(i * 3.141593F / size) + 1.0F) * verticalSizeMultiplier + 1.0D;

            for (int xLake = (int) (xAdjusted - horizontalSize / 2.0D); xLake <= (int) (xAdjusted + horizontalSize / 2.0D); xLake++)
                for (int yLake = (int) (yAdjusted - verticalSize / 2.0D); yLake <= (int) (yAdjusted + verticalSize / 2.0D); yLake++)
                    for (int zLake = (int) (zAdjusted - horizontalSize / 2.0D); zLake <= (int) (zAdjusted + horizontalSize / 2.0D); zLake++)
                    {
                        double xBounds = (xLake + 0.5D - xAdjusted) / (horizontalSize / 2.0D);
                        double yBounds = (yLake + 0.5D - yAdjusted) / (verticalSize / 2.0D);
                        double zBounds = (zLake + 0.5D - zAdjusted) / (horizontalSize / 2.0D);
                        if (xBounds * xBounds + yBounds * yBounds + zBounds * zBounds >= 1.0D)
                            continue;
                        int uBlock = world.getTypeId(xLake, yLake - 1, zLake);
                        if ((yLake < y + 2) && ((this.WorldSettings.undergroundLakesInAir) || (uBlock != 0))) // not air
                            this.ChangeWorld(xLake, yLake, zLake, Block.WATER.id);
                        else
                            this.ChangeWorld(xLake, yLake, zLake, 0); // Air block
                    }
        }
    }

    @Override
    public void populate(org.bukkit.World wrld, Random random, Chunk chunk)
    {
        this.world = ((CraftWorld)wrld).getHandle();
        this.rand = random;
        this.WorldSettings.InitWorld(((CraftWorld)wrld).getHandle(),random);

        if(this.c == null)
            this.c = new NoiseGeneratorOctaves(this.rand, 8);
        int x = chunk.getX();
        int z = chunk.getZ();

        int i1 = x * 16;
        int i2 = z * 16;

        BiomeBase localBiomeBase = world.getWorldChunkManager().getBiome(i1 + 16, i2 + 16);


        this.rand.setSeed(world.getSeed());
        long l1 = this.rand.nextLong() / 2L * 2L + 1L;
        long l2 = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed(x * l1 + z * l2 ^ world.getSeed());


        this.processUndergroundDeposits(i1, i2, localBiomeBase);
        // ToDo add lavaLevelMin and lavaLevelMax here
        if (this.WorldSettings.notchBiomeTrees)
        {
            if (this.rand.nextInt(4) == 0)
            {
                int i3 = i1 + this.rand.nextInt(16) + 8;
                int i4 = this.rand.nextInt(128);
                int i5 = i2 + this.rand.nextInt(16) + 8;
                new WorldGenLakes(Block.STATIONARY_WATER.id).a(this.world, this.rand, i3, i4, i5);
            }

            if (this.rand.nextInt(8) == 0)
            {
                int i3 = i1 + this.rand.nextInt(16) + 8;
                int i4 = this.rand.nextInt(this.rand.nextInt(120) + 8);
                int i5 = i2 + this.rand.nextInt(16) + 8;
                if ((i4 < 64) || (this.rand.nextInt(10) == 0))
                    new WorldGenLakes(Block.STATIONARY_LAVA.id).a(this.world, this.rand, i3, i4, i5);
            }
        }

        int i6;
        int i7;

        this.processAboveGroundMaterials(i1, i2, localBiomeBase);


        this.SpawnCustomObjects(i1, i2, localBiomeBase);


        this.processTrees(i1, i2, localBiomeBase);

        this.processGrass(i1, i2, localBiomeBase);


        i6 = 0;

        int i8;
        int i9;
        int i10;



        double[] TemperatureArray = new double[256];
        TemperatureArray = this.world.getWorldChunkManager().a(TemperatureArray, i1 + 8, i2 + 8, 16, 16);
        for (i6 = i1 + 8; i6 < i1 + 8 + 16; i6++)
        {
            for (i7 = i2 + 8; i7 < i2 + 8 + 16; i7++)
            {
                i8 = i6 - (i1 + 8);
                i9 = i7 - (i2 + 8);
                i10 = this.world.e(i6, i7);
                double d2 = TemperatureArray[(i8 * 16 + i9)] - (i10 - 64) / 64.0D * 0.3D;
                if ((d2 >= this.WorldSettings.snowThreshold) || (i10 <= 0) || (i10 >= 128) || (!this.world.isEmpty(i6, i10, i7)) || (!this.world.getMaterial(i6, i10 - 1, i7).isSolid()) || (this.world.getMaterial(i6, i10 - 1, i7) == Material.ICE))
                    continue;
                this.world.setTypeId(i6, i10, i7, Block.SNOW.id);
            }

        }

        if (this.WorldSettings.replaceBlocks.size() <= 0)
            return;

        byte[] blocks = this.world.getChunkAt(x, z).b;

        for (int i = 0; i < blocks.length; i++)
        {
            if (this.WorldSettings.replaceBlocks.containsKey(blocks[i]))
            {
                blocks[i] = this.WorldSettings.replaceBlocks.get(blocks[i]);
            }
        }
    }

}