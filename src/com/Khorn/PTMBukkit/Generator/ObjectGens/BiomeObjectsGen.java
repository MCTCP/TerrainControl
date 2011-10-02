package com.Khorn.PTMBukkit.Generator.ObjectGens;

import com.Khorn.PTMBukkit.BiomeConfig;
import com.Khorn.PTMBukkit.Util.WorldWithChunkCheck;
import net.minecraft.server.*;
import org.bukkit.BlockChangeDelegate;

import java.util.Random;

public class BiomeObjectsGen
{

    private WorldGenerator genWaterClay;
    private WorldGenerator genWaterSand;
    private WorldGenerator genWaterGravel;

    private WorldGenerator genYFlower;
    private WorldGenerator genRFlower;
    private WorldGenerator genBMushroom;
    private WorldGenerator genRMushroom;
    private WorldGenerator genReed;
    private WorldGenerator genCactus;
    private WorldGenerator genPumpkin;
    private WorldGenerator genGrass;
    private WorldGenerator genDeadBush;


    private WorldGenerator genWaterLake;
    private WorldGenerator genLavaLake;

    private World world;
    private BiomeConfig biomeConfig;
    private Random rand;
    private BiomeBase Biome;

    private Chunk workingChunk;

    private BlockChangeDelegate WorldDelegate;
    protected WorldGenTrees Tree = new WorldGenTrees();
    protected WorldGenBigTree BigTree = new WorldGenBigTree();
    protected WorldGenForest Forest = new WorldGenForest();
    protected WorldGenSwampTree SwampTree = new WorldGenSwampTree();
    protected WorldGenTaiga1 TaigaTree1 = new WorldGenTaiga1();
    protected WorldGenTaiga2 TaigaTree2 = new WorldGenTaiga2();

    public BiomeObjectsGen(World _world, BiomeConfig config, BiomeBase _biome)
    {
        world = _world;
        Biome = _biome;
        biomeConfig = config;
        WorldDelegate = new WorldWithChunkCheck(world);
        this.InitGenerators();

    }

    public void InitGenerators()
    {
        genWaterClay = new WorldGenClay(biomeConfig.waterClayDepositSize);
        genWaterSand = new WorldGenSand(biomeConfig.waterSandDepositSize, Block.SAND.id);
        genWaterGravel = new WorldGenSand(biomeConfig.waterGravelDepositSize, Block.GRAVEL.id);

        genYFlower = new WorldGenFlowers(Block.YELLOW_FLOWER.id);
        genRFlower = new WorldGenFlowers(Block.YELLOW_FLOWER.id);
        genBMushroom = new WorldGenFlowers(Block.BROWN_MUSHROOM.id);
        genRMushroom = new WorldGenFlowers(Block.RED_MUSHROOM.id);
        genReed = new WorldGenReed();
        genCactus = new WorldGenCactus();
        genPumpkin = new WorldGenPumpkin();
        genGrass = new WorldGenGrass(Block.LONG_GRASS.id, 1);
        genDeadBush = new WorldGenDeadBush(Block.DEAD_BUSH.id);

        genWaterLake = new WorldGenLakes(Block.STATIONARY_WATER.id);
        genLavaLake = new WorldGenLakes(Block.STATIONARY_LAVA.id);


    }

    public void SetChunk(Chunk chunk)
    {
        this.workingChunk = chunk;
    }

    public void ProcessUndergroundObjects(int x, int z, Random rnd)
    {
        this.rand = rnd;

        if (!this.biomeConfig.disableNotchPonds)
        {

            if (this.rand.nextInt(4) == 0)
            {
                int _x = x + this.rand.nextInt(16);
                int _y = this.rand.nextInt(127);
                int _z = z + this.rand.nextInt(16);
                genWaterLake.a(this.world, this.rand, _x, _y, _z);
            }

            if (this.rand.nextInt(8) == 0)
            {
                int _x = x + this.rand.nextInt(16);
                int _y = this.rand.nextInt(this.rand.nextInt(119) + 8);
                int _z = z + this.rand.nextInt(16);
                if ((_y < 64) || (this.rand.nextInt(10) == 0))
                    genLavaLake.a(this.world, this.rand, _x, _y, _z);
            }
        }

        placeMaterial(x, z, this.biomeConfig.dungeonRarity, this.biomeConfig.dungeonFrequency, this.biomeConfig.dungeonMinAltitude, this.biomeConfig.dungeonMaxAltitude, new WorldGenDungeons());

    }

    private void placeMaterial(int _x, int _z, int rarity, int frequency, int minAltitude, int maxAltitude, WorldGenerator worldGen)
    {
        for (int i = 0; i < frequency; i++)
        {
            if (this.rand.nextInt(100) >= rarity)
                continue;
            int x = _x + this.rand.nextInt(16);
            int z = _z + this.rand.nextInt(16);
            int y = this.rand.nextInt(maxAltitude - minAltitude) + minAltitude;
            worldGen.a(this.world, this.rand, x, y, z);
        }

    }

    private void placeMaterialUnderWater(int _x, int _z, int rarity, int frequency, WorldGenerator worldGen)
    {
        for (int i = 0; i < frequency; i++)
        {
            if (this.rand.nextInt(100) >= rarity)
                continue;
            int x = _x + this.rand.nextInt(16);
            int z = _z + this.rand.nextInt(16);
            int y = this.world.f(x, z);
            worldGen.a(this.world, this.rand, x, y, z);
        }

    }

    public void ProcessAboveGround(int x, int z, Random rnd)
    {
        this.rand = rnd;

        placeMaterial(x, z, this.biomeConfig.flowerDepositRarity, this.biomeConfig.flowerDepositFrequency, this.biomeConfig.flowerDepositMinAltitude, this.biomeConfig.flowerDepositMaxAltitude, this.genYFlower);

        placeMaterial(x, z, this.biomeConfig.roseDepositRarity, this.biomeConfig.roseDepositFrequency, this.biomeConfig.roseDepositMinAltitude, this.biomeConfig.roseDepositMaxAltitude, this.genRFlower);

        placeMaterial(x, z, this.biomeConfig.brownMushroomDepositRarity, this.biomeConfig.brownMushroomDepositFrequency, this.biomeConfig.brownMushroomDepositMinAltitude, this.biomeConfig.brownMushroomDepositMaxAltitude, this.genBMushroom);

        placeMaterial(x, z, this.biomeConfig.redMushroomDepositRarity, this.biomeConfig.redMushroomDepositFrequency, this.biomeConfig.redMushroomDepositMinAltitude, this.biomeConfig.redMushroomDepositMaxAltitude, this.genRMushroom);

        placeMaterial(x, z, this.biomeConfig.reedDepositRarity, this.biomeConfig.reedDepositFrequency, this.biomeConfig.reedDepositMinAltitude, this.biomeConfig.reedDepositMaxAltitude, this.genReed);

        placeMaterial(x, z, this.biomeConfig.pumpkinDepositRarity, this.biomeConfig.pumpkinDepositFrequency, this.biomeConfig.pumpkinDepositMinAltitude, this.biomeConfig.pumpkinDepositMaxAltitude, this.genPumpkin);

        placeMaterial(x, z, this.biomeConfig.cactusDepositRarity, this.biomeConfig.cactusDepositFrequency, this.biomeConfig.cactusDepositMinAltitude, this.biomeConfig.cactusDepositMaxAltitude, this.genCactus);

        placeMaterial(x, z, this.biomeConfig.longGrassDepositRarity, this.biomeConfig.longGrassDepositFrequency, this.biomeConfig.longGrassDepositMinAltitude, this.biomeConfig.longGrassDepositMaxAltitude, this.genGrass);

        placeMaterial(x, z, this.biomeConfig.deadBushDepositRarity, this.biomeConfig.deadBushDepositFrequency, this.biomeConfig.deadBushDepositMinAltitude, this.biomeConfig.deadBushDepositMaxAltitude, this.genDeadBush);

        SpawnLiquid(x, z, this.biomeConfig.waterSourceDepositRarity, this.biomeConfig.waterSourceDepositFrequency, this.biomeConfig.waterSourceDepositMinAltitude, this.biomeConfig.waterSourceDepositMaxAltitude, Block.WATER.id);

        SpawnLiquid(x, z, this.biomeConfig.lavaSourceDepositRarity, this.biomeConfig.lavaSourceDepositFrequency, this.biomeConfig.lavaSourceDepositMinAltitude, this.biomeConfig.lavaSourceDepositMaxAltitude, Block.LAVA.id);


        placeMaterialUnderWater(x, z, this.biomeConfig.waterClayDepositRarity, this.biomeConfig.waterClayDepositFrequency, this.genWaterClay);

        placeMaterialUnderWater(x, z, this.biomeConfig.waterSandDepositRarity, this.biomeConfig.waterSandDepositFrequency, this.genWaterSand);

        placeMaterialUnderWater(x, z, this.biomeConfig.waterGravelDepositRarity, this.biomeConfig.waterGravelDepositFrequency, this.genWaterGravel);


    }

    public void ProcessOres(int x, int z, Random rnd)
    {
        this.rand = rnd;

        SpawnMinable(x, z, this.biomeConfig.dirtDepositRarity1, this.biomeConfig.dirtDepositFrequency1, this.biomeConfig.dirtDepositMinAltitude1, this.biomeConfig.dirtDepositMaxAltitude1, this.biomeConfig.dirtDepositSize1, Block.DIRT.id);

        SpawnMinable(x, z, this.biomeConfig.dirtDepositRarity2, this.biomeConfig.dirtDepositFrequency2, this.biomeConfig.dirtDepositMinAltitude2, this.biomeConfig.dirtDepositMaxAltitude2, this.biomeConfig.dirtDepositSize2, Block.DIRT.id);

        SpawnMinable(x, z, this.biomeConfig.dirtDepositRarity3, this.biomeConfig.dirtDepositFrequency3, this.biomeConfig.dirtDepositMinAltitude3, this.biomeConfig.dirtDepositMaxAltitude3, this.biomeConfig.dirtDepositSize3, Block.DIRT.id);

        SpawnMinable(x, z, this.biomeConfig.dirtDepositRarity4, this.biomeConfig.dirtDepositFrequency4, this.biomeConfig.dirtDepositMinAltitude4, this.biomeConfig.dirtDepositMaxAltitude4, this.biomeConfig.dirtDepositSize4, Block.DIRT.id);

        SpawnMinable(x, z, this.biomeConfig.gravelDepositRarity1, this.biomeConfig.gravelDepositFrequency1, this.biomeConfig.gravelDepositMinAltitude1, this.biomeConfig.gravelDepositMaxAltitude1, this.biomeConfig.gravelDepositSize1, Block.GRAVEL.id);

        SpawnMinable(x, z, this.biomeConfig.gravelDepositRarity2, this.biomeConfig.gravelDepositFrequency2, this.biomeConfig.gravelDepositMinAltitude2, this.biomeConfig.gravelDepositMaxAltitude2, this.biomeConfig.gravelDepositSize2, Block.GRAVEL.id);

        SpawnMinable(x, z, this.biomeConfig.gravelDepositRarity3, this.biomeConfig.gravelDepositFrequency3, this.biomeConfig.gravelDepositMinAltitude3, this.biomeConfig.gravelDepositMaxAltitude3, this.biomeConfig.gravelDepositSize3, Block.GRAVEL.id);

        SpawnMinable(x, z, this.biomeConfig.gravelDepositRarity4, this.biomeConfig.gravelDepositFrequency4, this.biomeConfig.gravelDepositMinAltitude4, this.biomeConfig.gravelDepositMaxAltitude4, this.biomeConfig.gravelDepositSize4, Block.GRAVEL.id);

        SpawnMinable(x, z, this.biomeConfig.clayDepositRarity1, this.biomeConfig.clayDepositFrequency1, this.biomeConfig.clayDepositMinAltitude1, this.biomeConfig.clayDepositMaxAltitude1, this.biomeConfig.clayDepositSize1, Block.CLAY.id);

        SpawnMinable(x, z, this.biomeConfig.clayDepositRarity2, this.biomeConfig.clayDepositFrequency2, this.biomeConfig.clayDepositMinAltitude2, this.biomeConfig.clayDepositMaxAltitude2, this.biomeConfig.clayDepositSize2, Block.CLAY.id);

        SpawnMinable(x, z, this.biomeConfig.clayDepositRarity3, this.biomeConfig.clayDepositFrequency3, this.biomeConfig.clayDepositMinAltitude3, this.biomeConfig.clayDepositMaxAltitude3, this.biomeConfig.clayDepositSize3, Block.CLAY.id);

        SpawnMinable(x, z, this.biomeConfig.clayDepositRarity4, this.biomeConfig.clayDepositFrequency4, this.biomeConfig.clayDepositMinAltitude4, this.biomeConfig.clayDepositMaxAltitude4, this.biomeConfig.clayDepositSize4, Block.CLAY.id);

        SpawnMinable(x, z, this.biomeConfig.coalDepositRarity1, this.biomeConfig.coalDepositFrequency1, this.biomeConfig.coalDepositMinAltitude1, this.biomeConfig.coalDepositMaxAltitude1, this.biomeConfig.coalDepositSize1, Block.COAL_ORE.id);

        SpawnMinable(x, z, this.biomeConfig.coalDepositRarity2, this.biomeConfig.coalDepositFrequency2, this.biomeConfig.coalDepositMinAltitude2, this.biomeConfig.coalDepositMaxAltitude2, this.biomeConfig.coalDepositSize2, Block.COAL_ORE.id);

        SpawnMinable(x, z, this.biomeConfig.coalDepositRarity3, this.biomeConfig.coalDepositFrequency3, this.biomeConfig.coalDepositMinAltitude3, this.biomeConfig.coalDepositMaxAltitude3, this.biomeConfig.coalDepositSize3, Block.COAL_ORE.id);

        SpawnMinable(x, z, this.biomeConfig.coalDepositRarity4, this.biomeConfig.coalDepositFrequency4, this.biomeConfig.coalDepositMinAltitude4, this.biomeConfig.coalDepositMaxAltitude4, this.biomeConfig.coalDepositSize4, Block.COAL_ORE.id);

        SpawnMinable(x, z, this.biomeConfig.ironDepositRarity1, this.biomeConfig.ironDepositFrequency1, this.biomeConfig.ironDepositMinAltitude1, this.biomeConfig.ironDepositMaxAltitude1, this.biomeConfig.ironDepositSize1, Block.IRON_ORE.id);

        SpawnMinable(x, z, this.biomeConfig.ironDepositRarity2, this.biomeConfig.ironDepositFrequency2, this.biomeConfig.ironDepositMinAltitude2, this.biomeConfig.ironDepositMaxAltitude2, this.biomeConfig.ironDepositSize2, Block.IRON_ORE.id);

        SpawnMinable(x, z, this.biomeConfig.ironDepositRarity3, this.biomeConfig.ironDepositFrequency3, this.biomeConfig.ironDepositMinAltitude3, this.biomeConfig.ironDepositMaxAltitude3, this.biomeConfig.ironDepositSize3, Block.IRON_ORE.id);

        SpawnMinable(x, z, this.biomeConfig.ironDepositRarity4, this.biomeConfig.ironDepositFrequency4, this.biomeConfig.ironDepositMinAltitude4, this.biomeConfig.ironDepositMaxAltitude4, this.biomeConfig.ironDepositSize4, Block.IRON_ORE.id);

        SpawnMinable(x, z, this.biomeConfig.goldDepositRarity1, this.biomeConfig.goldDepositFrequency1, this.biomeConfig.goldDepositMinAltitude1, this.biomeConfig.goldDepositMaxAltitude1, this.biomeConfig.goldDepositSize1, Block.GOLD_ORE.id);

        SpawnMinable(x, z, this.biomeConfig.goldDepositRarity2, this.biomeConfig.goldDepositFrequency2, this.biomeConfig.goldDepositMinAltitude2, this.biomeConfig.goldDepositMaxAltitude2, this.biomeConfig.goldDepositSize2, Block.GOLD_ORE.id);

        SpawnMinable(x, z, this.biomeConfig.goldDepositRarity3, this.biomeConfig.goldDepositFrequency3, this.biomeConfig.goldDepositMinAltitude3, this.biomeConfig.goldDepositMaxAltitude3, this.biomeConfig.goldDepositSize3, Block.GOLD_ORE.id);

        SpawnMinable(x, z, this.biomeConfig.goldDepositRarity4, this.biomeConfig.goldDepositFrequency4, this.biomeConfig.goldDepositMinAltitude4, this.biomeConfig.goldDepositMaxAltitude4, this.biomeConfig.goldDepositSize4, Block.GOLD_ORE.id);

        SpawnMinable(x, z, this.biomeConfig.redstoneDepositRarity1, this.biomeConfig.redstoneDepositFrequency1, this.biomeConfig.redstoneDepositMinAltitude1, this.biomeConfig.redstoneDepositMaxAltitude1, this.biomeConfig.redstoneDepositSize1, Block.REDSTONE_ORE.id);

        SpawnMinable(x, z, this.biomeConfig.redstoneDepositRarity2, this.biomeConfig.redstoneDepositFrequency2, this.biomeConfig.redstoneDepositMinAltitude2, this.biomeConfig.redstoneDepositMaxAltitude2, this.biomeConfig.redstoneDepositSize2, Block.REDSTONE_ORE.id);

        SpawnMinable(x, z, this.biomeConfig.redstoneDepositRarity3, this.biomeConfig.redstoneDepositFrequency3, this.biomeConfig.redstoneDepositMinAltitude3, this.biomeConfig.redstoneDepositMaxAltitude3, this.biomeConfig.redstoneDepositSize3, Block.REDSTONE_ORE.id);

        SpawnMinable(x, z, this.biomeConfig.redstoneDepositRarity4, this.biomeConfig.redstoneDepositFrequency4, this.biomeConfig.redstoneDepositMinAltitude4, this.biomeConfig.redstoneDepositMaxAltitude4, this.biomeConfig.redstoneDepositSize4, Block.REDSTONE_ORE.id);

        SpawnMinable(x, z, this.biomeConfig.diamondDepositRarity1, this.biomeConfig.diamondDepositFrequency1, this.biomeConfig.diamondDepositMinAltitude1, this.biomeConfig.diamondDepositMaxAltitude1, this.biomeConfig.diamondDepositSize1, Block.DIAMOND_ORE.id);

        SpawnMinable(x, z, this.biomeConfig.diamondDepositRarity2, this.biomeConfig.diamondDepositFrequency2, this.biomeConfig.diamondDepositMinAltitude2, this.biomeConfig.diamondDepositMaxAltitude2, this.biomeConfig.diamondDepositSize2, Block.DIAMOND_ORE.id);

        SpawnMinable(x, z, this.biomeConfig.diamondDepositRarity3, this.biomeConfig.diamondDepositFrequency3, this.biomeConfig.diamondDepositMinAltitude3, this.biomeConfig.diamondDepositMaxAltitude3, this.biomeConfig.diamondDepositSize3, Block.DIAMOND_ORE.id);

        SpawnMinable(x, z, this.biomeConfig.diamondDepositRarity4, this.biomeConfig.diamondDepositFrequency4, this.biomeConfig.diamondDepositMinAltitude4, this.biomeConfig.diamondDepositMaxAltitude4, this.biomeConfig.diamondDepositSize4, Block.DIAMOND_ORE.id);

        SpawnMinable(x, z, this.biomeConfig.lapislazuliDepositRarity1, this.biomeConfig.lapislazuliDepositFrequency1, this.biomeConfig.lapislazuliDepositMinAltitude1, this.biomeConfig.lapislazuliDepositMaxAltitude1, this.biomeConfig.lapislazuliDepositSize1, Block.LAPIS_ORE.id);

        SpawnMinable(x, z, this.biomeConfig.lapislazuliDepositRarity2, this.biomeConfig.lapislazuliDepositFrequency2, this.biomeConfig.lapislazuliDepositMinAltitude2, this.biomeConfig.lapislazuliDepositMaxAltitude2, this.biomeConfig.lapislazuliDepositSize2, Block.LAPIS_ORE.id);

        SpawnMinable(x, z, this.biomeConfig.lapislazuliDepositRarity3, this.biomeConfig.lapislazuliDepositFrequency3, this.biomeConfig.lapislazuliDepositMinAltitude3, this.biomeConfig.lapislazuliDepositMaxAltitude3, this.biomeConfig.lapislazuliDepositSize3, Block.LAPIS_ORE.id);

        SpawnMinable(x, z, this.biomeConfig.lapislazuliDepositRarity4, this.biomeConfig.lapislazuliDepositFrequency4, this.biomeConfig.lapislazuliDepositMinAltitude4, this.biomeConfig.lapislazuliDepositMaxAltitude4, this.biomeConfig.lapislazuliDepositSize4, Block.LAPIS_ORE.id);

    }

    public void ProcessTrees(int x, int z, Random rnd)
    {
        this.rand = rnd;
        int localDensity = this.biomeConfig.TreeDensity;
        if (rnd.nextInt(10) == 0)
            localDensity++;
        for (int i = 0; i < localDensity; i++)
        {

            int _x = x + this.rand.nextInt(16);
            int _z = z + this.rand.nextInt(16);

            this.SpawnTree(_x, this.world.getHighestBlockYAt(_x, _z), _z);


        }

    }

    private void SpawnLiquid(int _x, int _z, int rarity, int frequency, int minAltitude, int maxAltitude, int type)
    {
        for (int t = 0; t < frequency; t++)
        {
            if (this.rand.nextInt(100) >= rarity)
                continue;

            int x = _x + this.rand.nextInt(16);
            int z = _z + this.rand.nextInt(16);
            int y = this.rand.nextInt(maxAltitude - minAltitude) + minAltitude;

            if (this.world.getTypeId(x, y + 1, z) != Block.STONE.id)
                return;
            if (this.world.getTypeId(x, y - 1, z) != Block.STONE.id)
                return;

            if ((this.world.getTypeId(x, y, z) != 0) && (this.world.getTypeId(x, y, z) != Block.STONE.id))
                return;


            int i = 0;
            int j = 0;

            int tempBlock = this.GetRawBlock(x - 1, y, z);

            i = (tempBlock == Block.STONE.id) ? i + 1 : i;
            j = (tempBlock == 0) ? j + 1 : j;

            tempBlock = this.GetRawBlock(x + 1, y, z);

            i = (tempBlock == Block.STONE.id) ? i + 1 : i;
            j = (tempBlock == 0) ? j + 1 : j;

            tempBlock = this.GetRawBlock(x, y, z - 1);

            i = (tempBlock == Block.STONE.id) ? i + 1 : i;
            j = (tempBlock == 0) ? j + 1 : j;

            tempBlock = this.GetRawBlock(x, y, z + 1);

            i = (tempBlock == Block.STONE.id) ? i + 1 : i;
            j = (tempBlock == 0) ? j + 1 : j;


            if ((i == 3) && (j == 1))
            {
                this.world.setRawTypeId(x, y, z, type);

            }
        }

    }

    private void SetRawBlock(int x, int y, int z, int BlockId)
    {
        if (y >= 128 || y < 0)
            return;

        this.workingChunk.b[((z & 0xF) * 16 + (x & 0xF)) * 128 + y] = (byte) BlockId;
    }

    private int GetRawBlock(int x, int y, int z)
    {
        z = z & 0xF;
        x = x & 0xF;
        if (y >= 128 || y < 0)
            return 0;

        return (int) this.workingChunk.b[(z * 16 + x) * 128 + y];
    }

    private void SpawnMinable(int _x, int _z, int rarity, int frequency, int minAltitude, int maxAltitude, int size, int BlockId)
    {

        for (int t = 0; t < frequency; t++)
        {
            if (this.rand.nextInt(100) >= rarity)
                continue;
            int x = _x + this.rand.nextInt(16);
            int z = _z + this.rand.nextInt(16);
            int y = this.rand.nextInt(maxAltitude - minAltitude) + minAltitude;

            float f = this.rand.nextFloat() * 3.141593F;

            double d1 = x + 8 + MathHelper.sin(f) * size / 8.0F;
            double d2 = x + 8 - MathHelper.sin(f) * size / 8.0F;
            double d3 = z + 8 + MathHelper.cos(f) * size / 8.0F;
            double d4 = z + 8 - MathHelper.cos(f) * size / 8.0F;

            double d5 = y + this.rand.nextInt(3) - 2;
            double d6 = y + this.rand.nextInt(3) - 2;

            for (int i = 0; i <= size; i++)
            {
                double d7 = d1 + (d2 - d1) * i / size;
                double d8 = d5 + (d6 - d5) * i / size;
                double d9 = d3 + (d4 - d3) * i / size;

                double d10 = this.rand.nextDouble() * size / 16.0D;
                double d11 = (MathHelper.sin(i * 3.141593F / size) + 1.0F) * d10 + 1.0D;
                double d12 = (MathHelper.sin(i * 3.141593F / size) + 1.0F) * d10 + 1.0D;

                int j = MathHelper.floor(d7 - d11 / 2.0D);
                int k = MathHelper.floor(d8 - d12 / 2.0D);
                int m = MathHelper.floor(d9 - d11 / 2.0D);

                int n = MathHelper.floor(d7 + d11 / 2.0D);
                int i1 = MathHelper.floor(d8 + d12 / 2.0D);
                int i2 = MathHelper.floor(d9 + d11 / 2.0D);

                for (int i3 = j; i3 <= n; i3++)
                {
                    double d13 = (i3 + 0.5D - d7) / (d11 / 2.0D);
                    if (d13 * d13 < 1.0D)
                    {
                        for (int i4 = k; i4 <= i1; i4++)
                        {
                            double d14 = (i4 + 0.5D - d8) / (d12 / 2.0D);
                            if (d13 * d13 + d14 * d14 < 1.0D)
                            {
                                for (int i5 = m; i5 <= i2; i5++)
                                {
                                    double d15 = (i5 + 0.5D - d9) / (d11 / 2.0D);
                                    if ((d13 * d13 + d14 * d14 + d15 * d15 < 1.0D) && (this.GetRawBlock(i3, i4, i5) == Block.STONE.id))
                                        this.SetRawBlock(i3, i4, i5, BlockId);
                                }
                            }
                        }
                    }

                }

            }
        }

    }

    private void SpawnTree(int x, int y, int z)
    {
        switch (this.Biome.y)
        {
            case 0:
            case 1:
            case 2:
            case 3:
            case 7:
            {
                if (this.rand.nextInt(10) == 0)
                {
                    BigTree.a(1.0D, 1.0D, 1.0D);
                    BigTree.generate(this.WorldDelegate, this.rand, x, y, z);
                } else
                    Tree.generate(this.WorldDelegate, this.rand, x, y, z);
                break;
            }
            case 4:
            {
                if (this.rand.nextInt(10) == 0)
                {
                    BigTree.a(1.0D, 1.0D, 1.0D);
                    BigTree.generate(this.WorldDelegate, this.rand, x, y, z);
                } else if (this.rand.nextInt(5) == 0)
                    Forest.generate(this.WorldDelegate, this.rand, x, y, z);
                else
                    Tree.generate(this.WorldDelegate, this.rand, x, y, z);
                break;
            }
            case 5:
            {
                if (this.rand.nextInt(3) == 0)
                    TaigaTree1.generate(this.WorldDelegate, this.rand, x, y, z);
                else
                    TaigaTree2.generate(this.WorldDelegate, this.rand, x, y, z);
                break;
            }
            case 6:
            {
                SwampTree.a(this.world, this.rand, x, y, z);
                break;
            }


        }

    }
}
