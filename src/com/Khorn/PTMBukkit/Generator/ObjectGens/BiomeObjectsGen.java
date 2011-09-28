package com.Khorn.PTMBukkit.Generator.ObjectGens;

import com.Khorn.PTMBukkit.BiomeConfig;
import net.minecraft.server.*;
import sun.security.krb5.Config;

import java.util.Random;

public class BiomeObjectsGen
{

    private WorldGenerator genWaterClay;
    private WorldGenerator genWaterSand;
    private WorldGenerator genWaterGravel;

    private WorldGenerator genDirt1;
    private WorldGenerator genDirt2;
    private WorldGenerator genDirt3;
    private WorldGenerator genDirt4;

    private WorldGenerator genClay1;
    private WorldGenerator genClay2;
    private WorldGenerator genClay3;
    private WorldGenerator genClay4;

    private WorldGenerator genGravel1;
    private WorldGenerator genGravel2;
    private WorldGenerator genGravel3;
    private WorldGenerator genGravel4;

    private WorldGenerator genCoal1;
    private WorldGenerator genCoal2;
    private WorldGenerator genCoal3;
    private WorldGenerator genCoal4;

    private WorldGenerator genIron1;
    private WorldGenerator genIron2;
    private WorldGenerator genIron3;
    private WorldGenerator genIron4;

    private WorldGenerator genGold1;
    private WorldGenerator genGold2;
    private WorldGenerator genGold3;
    private WorldGenerator genGold4;

    private WorldGenerator genRedstone1;
    private WorldGenerator genRedstone2;
    private WorldGenerator genRedstone3;
    private WorldGenerator genRedstone4;

    private WorldGenerator genDiamond1;
    private WorldGenerator genDiamond2;
    private WorldGenerator genDiamond3;
    private WorldGenerator genDiamond4;

    private WorldGenerator genLapis1;
    private WorldGenerator genLapis2;
    private WorldGenerator genLapis3;
    private WorldGenerator genLapis4;

    private WorldGenerator genYFlower;
    private WorldGenerator genRFlower;
    private WorldGenerator genBMushroom;
    private WorldGenerator genRMushroom;
    private WorldGenerator genReed;
    private WorldGenerator genCactus;
    private WorldGenerator genPumpkin;
    private WorldGenerator genGrass;
    private WorldGenerator genDeadBush;

    private WorldGenerator genWater;
    private WorldGenerator genLava;

    private WorldGenerator genWaterLake;
    private WorldGenerator genLavaLake;

    private World world;
    private BiomeConfig biomeConfig;
    private Random rand;
    private BiomeBase Biome;

    public BiomeObjectsGen(World _world, BiomeConfig config,BiomeBase _biome)
    {
        world = _world;
        Biome = _biome;
        biomeConfig = config;
        this.InitGenerators();

    }

    public void InitGenerators()
    {
        genWaterClay = new WorldGenClay(biomeConfig.waterClayDepositSize);
        genWaterSand = new WorldGenSand(biomeConfig.waterSandDepositSize, Block.SAND.id);
        genWaterGravel = new WorldGenSand(biomeConfig.waterGravelDepositSize, Block.GRAVEL.id);

        genDirt1 = new WorldGenMinable(Block.DIRT.id, biomeConfig.dirtDepositSize1);
        genDirt2 = new WorldGenMinable(Block.DIRT.id, biomeConfig.dirtDepositSize2);
        genDirt3 = new WorldGenMinable(Block.DIRT.id, biomeConfig.dirtDepositSize3);
        genDirt4 = new WorldGenMinable(Block.DIRT.id, biomeConfig.dirtDepositSize4);

        genClay1 = new WorldGenMinable(Block.CLAY.id, biomeConfig.clayDepositSize1);
        genClay2 = new WorldGenMinable(Block.CLAY.id, biomeConfig.clayDepositSize2);
        genClay3 = new WorldGenMinable(Block.CLAY.id, biomeConfig.clayDepositSize3);
        genClay4 = new WorldGenMinable(Block.CLAY.id, biomeConfig.clayDepositSize4);

        genGravel1 = new WorldGenMinable(Block.GRAVEL.id, biomeConfig.gravelDepositSize1);
        genGravel2 = new WorldGenMinable(Block.GRAVEL.id, biomeConfig.gravelDepositSize2);
        genGravel3 = new WorldGenMinable(Block.GRAVEL.id, biomeConfig.gravelDepositSize2);
        genGravel4 = new WorldGenMinable(Block.GRAVEL.id, biomeConfig.gravelDepositSize3);

        genCoal1 = new WorldGenMinable(Block.COAL_ORE.id, biomeConfig.coalDepositSize1);
        genCoal2 = new WorldGenMinable(Block.COAL_ORE.id, biomeConfig.coalDepositSize2);
        genCoal3 = new WorldGenMinable(Block.COAL_ORE.id, biomeConfig.coalDepositSize3);
        genCoal4 = new WorldGenMinable(Block.COAL_ORE.id, biomeConfig.coalDepositSize4);

        genIron1 = new WorldGenMinable(Block.IRON_ORE.id, biomeConfig.ironDepositSize1);
        genIron2 = new WorldGenMinable(Block.IRON_ORE.id, biomeConfig.ironDepositSize2);
        genIron3 = new WorldGenMinable(Block.IRON_ORE.id, biomeConfig.ironDepositSize3);
        genIron4 = new WorldGenMinable(Block.IRON_ORE.id, biomeConfig.ironDepositSize4);

        genGold1 = new WorldGenMinable(Block.GOLD_ORE.id, biomeConfig.goldDepositSize1);
        genGold2 = new WorldGenMinable(Block.GOLD_ORE.id, biomeConfig.goldDepositSize2);
        genGold3 = new WorldGenMinable(Block.GOLD_ORE.id, biomeConfig.goldDepositSize3);
        genGold4 = new WorldGenMinable(Block.GOLD_ORE.id, biomeConfig.goldDepositSize4);

        genRedstone1 = new WorldGenMinable(Block.REDSTONE_ORE.id, biomeConfig.redstoneDepositSize1);
        genRedstone2 = new WorldGenMinable(Block.REDSTONE_ORE.id, biomeConfig.redstoneDepositSize2);
        genRedstone3 = new WorldGenMinable(Block.REDSTONE_ORE.id, biomeConfig.redstoneDepositSize3);
        genRedstone4 = new WorldGenMinable(Block.REDSTONE_ORE.id, biomeConfig.redstoneDepositSize4);

        genDiamond1 = new WorldGenMinable(Block.DIAMOND_ORE.id, biomeConfig.diamondDepositSize1);
        genDiamond2 = new WorldGenMinable(Block.DIAMOND_ORE.id, biomeConfig.diamondDepositSize2);
        genDiamond3 = new WorldGenMinable(Block.DIAMOND_ORE.id, biomeConfig.diamondDepositSize3);
        genDiamond4 = new WorldGenMinable(Block.DIAMOND_ORE.id, biomeConfig.diamondDepositSize4);

        genLapis1 = new WorldGenMinable(Block.LAPIS_ORE.id, biomeConfig.lapislazuliDepositSize1);
        genLapis2 = new WorldGenMinable(Block.LAPIS_ORE.id, biomeConfig.lapislazuliDepositSize2);
        genLapis3 = new WorldGenMinable(Block.LAPIS_ORE.id, biomeConfig.lapislazuliDepositSize3);
        genLapis4 = new WorldGenMinable(Block.LAPIS_ORE.id, biomeConfig.lapislazuliDepositSize4);

        genYFlower = new WorldGenFlowers(Block.YELLOW_FLOWER.id);
        genRFlower = new WorldGenFlowers(Block.YELLOW_FLOWER.id);
        genBMushroom = new WorldGenFlowers(Block.BROWN_MUSHROOM.id);
        genRMushroom = new WorldGenFlowers(Block.RED_MUSHROOM.id);
        genReed = new WorldGenReed();
        genCactus = new WorldGenCactus();
        genPumpkin = new WorldGenPumpkin();
        genGrass = new WorldGenGrass(Block.LONG_GRASS.id, 1);
        genDeadBush = new WorldGenDeadBush(Block.DEAD_BUSH.id);

        genWater = new WorldGenLiquids(Block.WATER.id);
        genLava = new WorldGenLiquids(Block.WATER.id);

        genWaterLake = new WorldGenLakes(Block.STATIONARY_WATER.id);
        genLavaLake = new WorldGenLakes(Block.STATIONARY_LAVA.id);


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

        placeMaterial(x, z, this.biomeConfig.waterSourceDepositRarity, this.biomeConfig.waterSourceDepositFrequency, this.biomeConfig.waterSourceDepositMinAltitude, this.biomeConfig.waterSourceDepositMaxAltitude, this.genWater);

        placeMaterial(x, z, this.biomeConfig.lavaSourceDepositRarity, this.biomeConfig.lavaSourceDepositFrequency, this.biomeConfig.lavaSourceDepositMinAltitude, this.biomeConfig.lavaSourceDepositMaxAltitude, this.genLava);


        placeMaterialUnderWater(x, z, this.biomeConfig.waterClayDepositRarity, this.biomeConfig.waterClayDepositFrequency, this.genWaterClay);

        placeMaterialUnderWater(x, z, this.biomeConfig.waterSandDepositRarity, this.biomeConfig.waterSandDepositFrequency, this.genWaterSand);

        placeMaterialUnderWater(x, z, this.biomeConfig.waterGravelDepositRarity, this.biomeConfig.waterGravelDepositFrequency, this.genWaterGravel);


    }

    public void ProcessOres(int x, int z, Random rnd)
    {
        this.rand = rnd;

        placeMaterial(x, z, this.biomeConfig.dirtDepositRarity1, this.biomeConfig.dirtDepositFrequency1, this.biomeConfig.dirtDepositMinAltitude1, this.biomeConfig.dirtDepositMaxAltitude1, this.genDirt1);

        placeMaterial(x, z, this.biomeConfig.dirtDepositRarity2, this.biomeConfig.dirtDepositFrequency2, this.biomeConfig.dirtDepositMinAltitude2, this.biomeConfig.dirtDepositMaxAltitude2, this.genDirt2);

        placeMaterial(x, z, this.biomeConfig.dirtDepositRarity3, this.biomeConfig.dirtDepositFrequency3, this.biomeConfig.dirtDepositMinAltitude3, this.biomeConfig.dirtDepositMaxAltitude3, this.genDirt3);

        placeMaterial(x, z, this.biomeConfig.dirtDepositRarity4, this.biomeConfig.dirtDepositFrequency4, this.biomeConfig.dirtDepositMinAltitude4, this.biomeConfig.dirtDepositMaxAltitude4, this.genDirt4);

        placeMaterial(x, z, this.biomeConfig.gravelDepositRarity1, this.biomeConfig.gravelDepositFrequency1, this.biomeConfig.gravelDepositMinAltitude1, this.biomeConfig.gravelDepositMaxAltitude1, this.genGravel1);

        placeMaterial(x, z, this.biomeConfig.gravelDepositRarity2, this.biomeConfig.gravelDepositFrequency2, this.biomeConfig.gravelDepositMinAltitude2, this.biomeConfig.gravelDepositMaxAltitude2, this.genGravel2);

        placeMaterial(x, z, this.biomeConfig.gravelDepositRarity3, this.biomeConfig.gravelDepositFrequency3, this.biomeConfig.gravelDepositMinAltitude3, this.biomeConfig.gravelDepositMaxAltitude3, this.genGravel3);

        placeMaterial(x, z, this.biomeConfig.gravelDepositRarity4, this.biomeConfig.gravelDepositFrequency4, this.biomeConfig.gravelDepositMinAltitude4, this.biomeConfig.gravelDepositMaxAltitude4, this.genGravel4);

        placeMaterial(x, z, this.biomeConfig.clayDepositRarity1, this.biomeConfig.clayDepositFrequency1, this.biomeConfig.clayDepositMinAltitude1, this.biomeConfig.clayDepositMaxAltitude1, this.genClay1);

        placeMaterial(x, z, this.biomeConfig.clayDepositRarity2, this.biomeConfig.clayDepositFrequency2, this.biomeConfig.clayDepositMinAltitude2, this.biomeConfig.clayDepositMaxAltitude2, this.genClay2);

        placeMaterial(x, z, this.biomeConfig.clayDepositRarity3, this.biomeConfig.clayDepositFrequency3, this.biomeConfig.clayDepositMinAltitude3, this.biomeConfig.clayDepositMaxAltitude3, this.genClay3);

        placeMaterial(x, z, this.biomeConfig.clayDepositRarity4, this.biomeConfig.clayDepositFrequency4, this.biomeConfig.clayDepositMinAltitude4, this.biomeConfig.clayDepositMaxAltitude4, this.genClay4);

        placeMaterial(x, z, this.biomeConfig.coalDepositRarity1, this.biomeConfig.coalDepositFrequency1, this.biomeConfig.coalDepositMinAltitude1, this.biomeConfig.coalDepositMaxAltitude1, this.genCoal1);

        placeMaterial(x, z, this.biomeConfig.coalDepositRarity2, this.biomeConfig.coalDepositFrequency2, this.biomeConfig.coalDepositMinAltitude2, this.biomeConfig.coalDepositMaxAltitude2, this.genCoal2);

        placeMaterial(x, z, this.biomeConfig.coalDepositRarity3, this.biomeConfig.coalDepositFrequency3, this.biomeConfig.coalDepositMinAltitude3, this.biomeConfig.coalDepositMaxAltitude3, this.genCoal3);

        placeMaterial(x, z, this.biomeConfig.coalDepositRarity4, this.biomeConfig.coalDepositFrequency4, this.biomeConfig.coalDepositMinAltitude4, this.biomeConfig.coalDepositMaxAltitude4, this.genCoal4);

        placeMaterial(x, z, this.biomeConfig.ironDepositRarity1, this.biomeConfig.ironDepositFrequency1, this.biomeConfig.ironDepositMinAltitude1, this.biomeConfig.ironDepositMaxAltitude1, this.genIron1);

        placeMaterial(x, z, this.biomeConfig.ironDepositRarity2, this.biomeConfig.ironDepositFrequency2, this.biomeConfig.ironDepositMinAltitude2, this.biomeConfig.ironDepositMaxAltitude2, this.genIron2);

        placeMaterial(x, z, this.biomeConfig.ironDepositRarity3, this.biomeConfig.ironDepositFrequency3, this.biomeConfig.ironDepositMinAltitude3, this.biomeConfig.ironDepositMaxAltitude3, this.genIron3);

        placeMaterial(x, z, this.biomeConfig.ironDepositRarity4, this.biomeConfig.ironDepositFrequency4, this.biomeConfig.ironDepositMinAltitude4, this.biomeConfig.ironDepositMaxAltitude4, this.genIron4);

        placeMaterial(x, z, this.biomeConfig.goldDepositRarity1, this.biomeConfig.goldDepositFrequency1, this.biomeConfig.goldDepositMinAltitude1, this.biomeConfig.goldDepositMaxAltitude1, this.genGold1);

        placeMaterial(x, z, this.biomeConfig.goldDepositRarity2, this.biomeConfig.goldDepositFrequency2, this.biomeConfig.goldDepositMinAltitude2, this.biomeConfig.goldDepositMaxAltitude2, this.genGold2);

        placeMaterial(x, z, this.biomeConfig.goldDepositRarity3, this.biomeConfig.goldDepositFrequency3, this.biomeConfig.goldDepositMinAltitude3, this.biomeConfig.goldDepositMaxAltitude3, this.genGold3);

        placeMaterial(x, z, this.biomeConfig.goldDepositRarity4, this.biomeConfig.goldDepositFrequency4, this.biomeConfig.goldDepositMinAltitude4, this.biomeConfig.goldDepositMaxAltitude4, this.genGold4);

        placeMaterial(x, z, this.biomeConfig.redstoneDepositRarity1, this.biomeConfig.redstoneDepositFrequency1, this.biomeConfig.redstoneDepositMinAltitude1, this.biomeConfig.redstoneDepositMaxAltitude1, this.genRedstone1);

        placeMaterial(x, z, this.biomeConfig.redstoneDepositRarity2, this.biomeConfig.redstoneDepositFrequency2, this.biomeConfig.redstoneDepositMinAltitude2, this.biomeConfig.redstoneDepositMaxAltitude2, this.genRedstone2);

        placeMaterial(x, z, this.biomeConfig.redstoneDepositRarity3, this.biomeConfig.redstoneDepositFrequency3, this.biomeConfig.redstoneDepositMinAltitude3, this.biomeConfig.redstoneDepositMaxAltitude3, this.genRedstone3);

        placeMaterial(x, z, this.biomeConfig.redstoneDepositRarity4, this.biomeConfig.redstoneDepositFrequency4, this.biomeConfig.redstoneDepositMinAltitude4, this.biomeConfig.redstoneDepositMaxAltitude4, this.genRedstone4);

        placeMaterial(x, z, this.biomeConfig.diamondDepositRarity1, this.biomeConfig.diamondDepositFrequency1, this.biomeConfig.diamondDepositMinAltitude1, this.biomeConfig.diamondDepositMaxAltitude1, this.genDiamond1);

        placeMaterial(x, z, this.biomeConfig.diamondDepositRarity2, this.biomeConfig.diamondDepositFrequency2, this.biomeConfig.diamondDepositMinAltitude2, this.biomeConfig.diamondDepositMaxAltitude2, this.genDiamond2);

        placeMaterial(x, z, this.biomeConfig.diamondDepositRarity3, this.biomeConfig.diamondDepositFrequency3, this.biomeConfig.diamondDepositMinAltitude3, this.biomeConfig.diamondDepositMaxAltitude3, this.genDiamond3);

        placeMaterial(x, z, this.biomeConfig.diamondDepositRarity4, this.biomeConfig.diamondDepositFrequency4, this.biomeConfig.diamondDepositMinAltitude4, this.biomeConfig.diamondDepositMaxAltitude4, this.genDiamond4);

        placeMaterial(x, z, this.biomeConfig.lapislazuliDepositRarity1, this.biomeConfig.lapislazuliDepositFrequency1, this.biomeConfig.lapislazuliDepositMinAltitude1, this.biomeConfig.lapislazuliDepositMaxAltitude1, this.genLapis1);

        placeMaterial(x, z, this.biomeConfig.lapislazuliDepositRarity2, this.biomeConfig.lapislazuliDepositFrequency2, this.biomeConfig.lapislazuliDepositMinAltitude2, this.biomeConfig.lapislazuliDepositMaxAltitude2, this.genLapis2);

        placeMaterial(x, z, this.biomeConfig.lapislazuliDepositRarity3, this.biomeConfig.lapislazuliDepositFrequency3, this.biomeConfig.lapislazuliDepositMinAltitude3, this.biomeConfig.lapislazuliDepositMaxAltitude3, this.genLapis3);

        placeMaterial(x, z, this.biomeConfig.lapislazuliDepositRarity4, this.biomeConfig.lapislazuliDepositFrequency4, this.biomeConfig.lapislazuliDepositMinAltitude4, this.biomeConfig.lapislazuliDepositMaxAltitude4, this.genLapis4);

    }

    public void ProcessTrees(int x, int z,Random rnd)
    {
        int localDensity = this.biomeConfig.TreeDensity;
        if(rnd.nextInt(10) == 0) localDensity++;
         for (int i = 0; i < localDensity; i++)
        {

            int _x = x + this.rand.nextInt(16);
            int _z = z + this.rand.nextInt(16);

            WorldGenerator localWorldGenerator = this.Biome.a(this.rand);
            localWorldGenerator.a(1.0D, 1.0D, 1.0D);
            localWorldGenerator.a(this.world, this.rand, _x, this.world.getHighestBlockYAt(_x, _z), _z);


        }

    }
}
