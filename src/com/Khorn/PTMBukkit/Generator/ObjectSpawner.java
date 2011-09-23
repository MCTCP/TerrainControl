package com.Khorn.PTMBukkit.Generator;

import com.Khorn.PTMBukkit.CustomObjects.CustomObjectGen;
import com.Khorn.PTMBukkit.WorldConfig;
import net.minecraft.server.*;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.generator.BlockPopulator;
import java.util.Random;

public class ObjectSpawner extends BlockPopulator
{


    private NoiseGeneratorOctaves treeNoise;


    private WorldConfig worldSettings;
    private Random rand;
    private World world;

    public ObjectSpawner(WorldConfig wrk)
    {
        this.worldSettings = wrk;
        this.rand = new Random();
        this.worldSettings.objectSpawner = this;
    }

    public void Init(World world)
    {
        this.world = world;
        this.treeNoise = new NoiseGeneratorOctaves(new Random(world.getSeed()), 8);
    }


    void processAboveGroundMaterials(int x, int z, BiomeBase currentBiome)
    {
        processDepositMaterial(x, z, this.worldSettings.flowerDepositRarity, this.worldSettings.flowerDepositFrequency, this.worldSettings.flowerDepositMinAltitude, this.worldSettings.flowerDepositMaxAltitude, -1, Block.YELLOW_FLOWER.id);

        processDepositMaterial(x, z, this.worldSettings.roseDepositRarity, this.worldSettings.roseDepositFrequency, this.worldSettings.roseDepositMinAltitude, this.worldSettings.roseDepositMaxAltitude, -1, Block.RED_ROSE.id);

        processDepositMaterial(x, z, this.worldSettings.brownMushroomDepositRarity, this.worldSettings.brownMushroomDepositFrequency, this.worldSettings.brownMushroomDepositMinAltitude, this.worldSettings.brownMushroomDepositMaxAltitude, -1, Block.BROWN_MUSHROOM.id);

        processDepositMaterial(x, z, this.worldSettings.redMushroomDepositRarity, this.worldSettings.redMushroomDepositFrequency, this.worldSettings.redMushroomDepositMinAltitude, this.worldSettings.redMushroomDepositMaxAltitude, -1, Block.RED_MUSHROOM.id);

        processDepositMaterial(x, z, this.worldSettings.reedDepositRarity, this.worldSettings.reedDepositFrequency, this.worldSettings.reedDepositMinAltitude, this.worldSettings.reedDepositMaxAltitude, -1, Block.SUGAR_CANE_BLOCK.id);

        processDepositMaterial(x, z, this.worldSettings.pumpkinDepositRarity, this.worldSettings.pumpkinDepositFrequency, this.worldSettings.pumpkinDepositMinAltitude, this.worldSettings.pumpkinDepositMaxAltitude, -1, Block.PUMPKIN.id);

        processDepositMaterial(x, z, this.worldSettings.waterSourceDepositRarity, this.worldSettings.waterSourceDepositFrequency, this.worldSettings.waterSourceDepositMinAltitude, this.worldSettings.waterSourceDepositMaxAltitude, -1, Block.WATER.id);

        processDepositMaterial(x, z, this.worldSettings.lavaSourceDepositRarity, this.worldSettings.lavaSourceDepositFrequency, this.worldSettings.lavaSourceDepositMinAltitude, this.worldSettings.lavaSourceDepositMaxAltitude, -1, Block.LAVA.id);

        processDepositMaterial(x, z, this.worldSettings.cactusDepositRarity, this.worldSettings.globalCactusDensity + (currentBiome == BiomeBase.DESERT ? this.worldSettings.desertCactusDensity : 0), this.worldSettings.cactusDepositMinAltitude, this.worldSettings.cactusDepositMaxAltitude, -1, Block.CACTUS.id);
    }


    void processTrees(int x, int z, BiomeBase currentBiome)
    {
        if (!this.worldSettings.notchBiomeTrees)
            return;
        double d1 = 0.5D;
        int treeDensity = 0;
        double temp = this.treeNoise.a(x * d1, z * d1);
        int treeDensityVariation = (int) ((temp / 8.0D + this.rand.nextDouble() * 4.0D + 4.0D) / 3.0D);

        if (this.rand.nextInt(10) == 0)
            treeDensity++;

        if (currentBiome == BiomeBase.SWAMPLAND)
            treeDensity += treeDensityVariation + this.worldSettings.swamplandTreeDensity;
        if (currentBiome == BiomeBase.FOREST)
            treeDensity += treeDensityVariation + this.worldSettings.forestTreeDensity;
        if (currentBiome == BiomeBase.TAIGA)
            treeDensity += treeDensityVariation + this.worldSettings.taigaTreeDensity;
        if (currentBiome == BiomeBase.DESERT)
            treeDensity += treeDensityVariation + this.worldSettings.desertTreeDensity;
        if (currentBiome == BiomeBase.PLAINS)
            treeDensity += treeDensityVariation + this.worldSettings.plainsTreeDensity;


        for (int i = 0; i < treeDensity; i++)
        {

            int _x = x + this.rand.nextInt(16);
            int _z = z + this.rand.nextInt(16);

            WorldGenerator localWorldGenerator = currentBiome.a(this.rand);
            localWorldGenerator.a(1.0D, 1.0D, 1.0D);
            localWorldGenerator.a(this.world, this.rand, _x, this.world.getHighestBlockYAt(_x, _z), _z);


        }

    }

    void processDepositMaterial(int _x, int _z, int rarity, int frequency, int minAltitude, int maxAltitude, int size, int type)
    {

        if ((type == Block.FIRE.id))
            frequency = this.rand.nextInt(this.rand.nextInt(frequency) + 1) + 1;
        else if ((type == Block.GLOWSTONE.id) && (size == -1))
        {
            frequency = this.rand.nextInt(this.rand.nextInt(frequency) + 1);
        }
        for (int i = 0; i < frequency; i++)
        {
            if (this.rand.nextInt(100) >= rarity)
                continue;
            int x = _x + this.rand.nextInt(16);
            int z = _z + this.rand.nextInt(16);
            int y = this.rand.nextInt(maxAltitude - minAltitude) + minAltitude;

            if ((type == Block.YELLOW_FLOWER.id) || (type == Block.RED_ROSE.id || (type == Block.BROWN_MUSHROOM.id) || (type == Block.RED_MUSHROOM.id)))
                new WorldGenFlowers(type).a(this.world, this.rand, x, y, z);
            else if (type == Block.CACTUS.id)
                new WorldGenCactus().a(this.world, this.rand, x, y, z);
            else if (type == Block.SUGAR_CANE_BLOCK.id)
                new WorldGenReed().a(this.world, this.rand, x, y, z);
            else if (type == Block.PUMPKIN.id)
                new WorldGenPumpkin().a(this.world, this.rand, x, y, z);
            else if (type == Block.CLAY.id)
                new WorldGenClay(size).a(this.world, this.rand, x, y, z);
            else if (type == Block.WATER.id)
            {
                if (!this.worldSettings.evenWaterSourceDistribution)
                    y = this.rand.nextInt(this.rand.nextInt(maxAltitude - minAltitude) + minAltitude + 1);
                SpawnLiquid(x, y, z, type);
            } else if (type == Block.LAVA.id)
            {
                if (!this.worldSettings.evenLavaSourceDistribution)
                    y = this.rand.nextInt(this.rand.nextInt(maxAltitude - minAltitude) + minAltitude + 1);
                SpawnLiquid(x, y, z, type);
            } else if (type == Block.MOB_SPAWNER.id)
                new WorldGenDungeons().a(this.world, this.rand, x, y, z);
            else
                new WorldGenMinable(type, size).a(this.world, this.rand, x, y, z);
        }
    }

    void processGrass(int x, int z, BiomeBase currentBiome)
    {
        int grassDensity = 0;
        if (currentBiome == BiomeBase.FOREST)
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

            _x = x + this.rand.nextInt(16);
            _y = this.rand.nextInt(128);
            _z = z + this.rand.nextInt(16);
            new WorldGenGrass(Block.LONG_GRASS.id, grassType).a(this.world, this.rand, _x, _y, _z);
        }

        if (currentBiome == BiomeBase.DESERT)
        {

            for (int i = 0; i < 2; i++)
            {
                _x = x + this.rand.nextInt(16);
                _y = this.rand.nextInt(128);
                _z = z + this.rand.nextInt(16);
                new WorldGenDeadBush(Block.DEAD_BUSH.id).a(this.world, this.rand, _x, _y, _z);
            }
        }
    }

    void processUndergroundDeposits(int x, int z)
    {

        processDepositMaterial(x, z, this.worldSettings.dungeonRarity, this.worldSettings.dungeonFrequency, this.worldSettings.dungeonMinAltitude, this.worldSettings.dungeonMaxAltitude, -1, Block.MOB_SPAWNER.id);

        processDepositMaterial(x, z, this.worldSettings.dirtDepositRarity1, this.worldSettings.dirtDepositFrequency1, this.worldSettings.dirtDepositMinAltitude1, this.worldSettings.dirtDepositMaxAltitude1, this.worldSettings.dirtDepositSize1, Block.DIRT.id);

        processDepositMaterial(x, z, this.worldSettings.dirtDepositRarity2, this.worldSettings.dirtDepositFrequency2, this.worldSettings.dirtDepositMinAltitude2, this.worldSettings.dirtDepositMaxAltitude2, this.worldSettings.dirtDepositSize2, Block.DIRT.id);

        processDepositMaterial(x, z, this.worldSettings.dirtDepositRarity3, this.worldSettings.dirtDepositFrequency3, this.worldSettings.dirtDepositMinAltitude3, this.worldSettings.dirtDepositMaxAltitude3, this.worldSettings.dirtDepositSize3, Block.DIRT.id);

        processDepositMaterial(x, z, this.worldSettings.dirtDepositRarity4, this.worldSettings.dirtDepositFrequency4, this.worldSettings.dirtDepositMinAltitude4, this.worldSettings.dirtDepositMaxAltitude4, this.worldSettings.dirtDepositSize4, Block.DIRT.id);

        processDepositMaterial(x, z, this.worldSettings.gravelDepositRarity1, this.worldSettings.gravelDepositFrequency1, this.worldSettings.gravelDepositMinAltitude1, this.worldSettings.gravelDepositMaxAltitude1, this.worldSettings.gravelDepositSize1, Block.GRAVEL.id);

        processDepositMaterial(x, z, this.worldSettings.gravelDepositRarity2, this.worldSettings.gravelDepositFrequency2, this.worldSettings.gravelDepositMinAltitude2, this.worldSettings.gravelDepositMaxAltitude2, this.worldSettings.gravelDepositSize2, Block.GRAVEL.id);

        processDepositMaterial(x, z, this.worldSettings.gravelDepositRarity3, this.worldSettings.gravelDepositFrequency3, this.worldSettings.gravelDepositMinAltitude3, this.worldSettings.gravelDepositMaxAltitude3, this.worldSettings.gravelDepositSize3, Block.GRAVEL.id);

        processDepositMaterial(x, z, this.worldSettings.gravelDepositRarity4, this.worldSettings.gravelDepositFrequency4, this.worldSettings.gravelDepositMinAltitude4, this.worldSettings.gravelDepositMaxAltitude4, this.worldSettings.gravelDepositSize4, Block.GRAVEL.id);

        processDepositMaterial(x, z, this.worldSettings.clayDepositRarity1, this.worldSettings.clayDepositFrequency1, this.worldSettings.clayDepositMinAltitude1, this.worldSettings.clayDepositMaxAltitude1, this.worldSettings.clayDepositSize1, Block.CLAY.id);

        processDepositMaterial(x, z, this.worldSettings.clayDepositRarity2, this.worldSettings.clayDepositFrequency2, this.worldSettings.clayDepositMinAltitude2, this.worldSettings.clayDepositMaxAltitude2, this.worldSettings.clayDepositSize2, Block.CLAY.id);

        processDepositMaterial(x, z, this.worldSettings.clayDepositRarity3, this.worldSettings.clayDepositFrequency3, this.worldSettings.clayDepositMinAltitude3, this.worldSettings.clayDepositMaxAltitude3, this.worldSettings.clayDepositSize3, Block.CLAY.id);

        processDepositMaterial(x, z, this.worldSettings.clayDepositRarity4, this.worldSettings.clayDepositFrequency4, this.worldSettings.clayDepositMinAltitude4, this.worldSettings.clayDepositMaxAltitude4, this.worldSettings.clayDepositSize4, Block.CLAY.id);

        processDepositMaterial(x, z, this.worldSettings.coalDepositRarity1, this.worldSettings.coalDepositFrequency1, this.worldSettings.coalDepositMinAltitude1, this.worldSettings.coalDepositMaxAltitude1, this.worldSettings.coalDepositSize1, Block.COAL_ORE.id);

        processDepositMaterial(x, z, this.worldSettings.coalDepositRarity2, this.worldSettings.coalDepositFrequency2, this.worldSettings.coalDepositMinAltitude2, this.worldSettings.coalDepositMaxAltitude2, this.worldSettings.coalDepositSize2, Block.COAL_ORE.id);

        processDepositMaterial(x, z, this.worldSettings.coalDepositRarity3, this.worldSettings.coalDepositFrequency3, this.worldSettings.coalDepositMinAltitude3, this.worldSettings.coalDepositMaxAltitude3, this.worldSettings.coalDepositSize3, Block.COAL_ORE.id);

        processDepositMaterial(x, z, this.worldSettings.coalDepositRarity4, this.worldSettings.coalDepositFrequency4, this.worldSettings.coalDepositMinAltitude4, this.worldSettings.coalDepositMaxAltitude4, this.worldSettings.coalDepositSize4, Block.COAL_ORE.id);

        processDepositMaterial(x, z, this.worldSettings.ironDepositRarity1, this.worldSettings.ironDepositFrequency1, this.worldSettings.ironDepositMinAltitude1, this.worldSettings.ironDepositMaxAltitude1, this.worldSettings.ironDepositSize1, Block.IRON_ORE.id);

        processDepositMaterial(x, z, this.worldSettings.ironDepositRarity2, this.worldSettings.ironDepositFrequency2, this.worldSettings.ironDepositMinAltitude2, this.worldSettings.ironDepositMaxAltitude2, this.worldSettings.ironDepositSize2, Block.IRON_ORE.id);

        processDepositMaterial(x, z, this.worldSettings.ironDepositRarity3, this.worldSettings.ironDepositFrequency3, this.worldSettings.ironDepositMinAltitude3, this.worldSettings.ironDepositMaxAltitude3, this.worldSettings.ironDepositSize3, Block.IRON_ORE.id);

        processDepositMaterial(x, z, this.worldSettings.ironDepositRarity4, this.worldSettings.ironDepositFrequency4, this.worldSettings.ironDepositMinAltitude4, this.worldSettings.ironDepositMaxAltitude4, this.worldSettings.ironDepositSize4, Block.IRON_ORE.id);

        processDepositMaterial(x, z, this.worldSettings.goldDepositRarity1, this.worldSettings.goldDepositFrequency1, this.worldSettings.goldDepositMinAltitude1, this.worldSettings.goldDepositMaxAltitude1, this.worldSettings.goldDepositSize1, Block.GOLD_ORE.id);

        processDepositMaterial(x, z, this.worldSettings.goldDepositRarity2, this.worldSettings.goldDepositFrequency2, this.worldSettings.goldDepositMinAltitude2, this.worldSettings.goldDepositMaxAltitude2, this.worldSettings.goldDepositSize2, Block.GOLD_ORE.id);

        processDepositMaterial(x, z, this.worldSettings.goldDepositRarity3, this.worldSettings.goldDepositFrequency3, this.worldSettings.goldDepositMinAltitude3, this.worldSettings.goldDepositMaxAltitude3, this.worldSettings.goldDepositSize3, Block.GOLD_ORE.id);

        processDepositMaterial(x, z, this.worldSettings.goldDepositRarity4, this.worldSettings.goldDepositFrequency4, this.worldSettings.goldDepositMinAltitude4, this.worldSettings.goldDepositMaxAltitude4, this.worldSettings.goldDepositSize4, Block.GOLD_ORE.id);

        processDepositMaterial(x, z, this.worldSettings.redstoneDepositRarity1, this.worldSettings.redstoneDepositFrequency1, this.worldSettings.redstoneDepositMinAltitude1, this.worldSettings.redstoneDepositMaxAltitude1, this.worldSettings.redstoneDepositSize1, Block.REDSTONE_ORE.id);

        processDepositMaterial(x, z, this.worldSettings.redstoneDepositRarity2, this.worldSettings.redstoneDepositFrequency2, this.worldSettings.redstoneDepositMinAltitude2, this.worldSettings.redstoneDepositMaxAltitude2, this.worldSettings.redstoneDepositSize2, Block.REDSTONE_ORE.id);

        processDepositMaterial(x, z, this.worldSettings.redstoneDepositRarity3, this.worldSettings.redstoneDepositFrequency3, this.worldSettings.redstoneDepositMinAltitude3, this.worldSettings.redstoneDepositMaxAltitude3, this.worldSettings.redstoneDepositSize3, Block.REDSTONE_ORE.id);

        processDepositMaterial(x, z, this.worldSettings.redstoneDepositRarity4, this.worldSettings.redstoneDepositFrequency4, this.worldSettings.redstoneDepositMinAltitude4, this.worldSettings.redstoneDepositMaxAltitude4, this.worldSettings.redstoneDepositSize4, Block.REDSTONE_ORE.id);

        processDepositMaterial(x, z, this.worldSettings.diamondDepositRarity1, this.worldSettings.diamondDepositFrequency1, this.worldSettings.diamondDepositMinAltitude1, this.worldSettings.diamondDepositMaxAltitude1, this.worldSettings.diamondDepositSize1, Block.DIAMOND_ORE.id);

        processDepositMaterial(x, z, this.worldSettings.diamondDepositRarity2, this.worldSettings.diamondDepositFrequency2, this.worldSettings.diamondDepositMinAltitude2, this.worldSettings.diamondDepositMaxAltitude2, this.worldSettings.diamondDepositSize2, Block.DIAMOND_ORE.id);

        processDepositMaterial(x, z, this.worldSettings.diamondDepositRarity3, this.worldSettings.diamondDepositFrequency3, this.worldSettings.diamondDepositMinAltitude3, this.worldSettings.diamondDepositMaxAltitude3, this.worldSettings.diamondDepositSize3, Block.DIAMOND_ORE.id);

        processDepositMaterial(x, z, this.worldSettings.diamondDepositRarity4, this.worldSettings.diamondDepositFrequency4, this.worldSettings.diamondDepositMinAltitude4, this.worldSettings.diamondDepositMaxAltitude4, this.worldSettings.diamondDepositSize4, Block.DIAMOND_ORE.id);

        processDepositMaterial(x, z, this.worldSettings.lapislazuliDepositRarity1, this.worldSettings.lapislazuliDepositFrequency1, this.worldSettings.lapislazuliDepositMinAltitude1, this.worldSettings.lapislazuliDepositMaxAltitude1, this.worldSettings.lapislazuliDepositSize1, Block.LAPIS_ORE.id);

        processDepositMaterial(x, z, this.worldSettings.lapislazuliDepositRarity2, this.worldSettings.lapislazuliDepositFrequency2, this.worldSettings.lapislazuliDepositMinAltitude2, this.worldSettings.lapislazuliDepositMaxAltitude2, this.worldSettings.lapislazuliDepositSize2, Block.LAPIS_ORE.id);

        processDepositMaterial(x, z, this.worldSettings.lapislazuliDepositRarity3, this.worldSettings.lapislazuliDepositFrequency3, this.worldSettings.lapislazuliDepositMinAltitude3, this.worldSettings.lapislazuliDepositMaxAltitude3, this.worldSettings.lapislazuliDepositSize3, Block.LAPIS_ORE.id);

        processDepositMaterial(x, z, this.worldSettings.lapislazuliDepositRarity4, this.worldSettings.lapislazuliDepositFrequency4, this.worldSettings.lapislazuliDepositMinAltitude4, this.worldSettings.lapislazuliDepositMaxAltitude4, this.worldSettings.lapislazuliDepositSize4, Block.LAPIS_ORE.id);


        if (this.worldSettings.undergroundLakes)
            processUndergroundLakes(x, z);
    }


    void processUndergroundLakes(int x, int z)
    {
        for (int i = 0; i < this.worldSettings.undergroundLakeFrequency; i++)
        {
            if (this.rand.nextInt(100) >= this.worldSettings.undergroundLakeRarity)
                continue;
            int xR = x + this.rand.nextInt(16);
            int yR = this.rand.nextInt(this.worldSettings.undergroundLakeMaxAltitude - this.worldSettings.undergroundLakeMinAltitude) + this.worldSettings.undergroundLakeMinAltitude;
            int zR = z + this.rand.nextInt(16);
            if (yR < this.world.getHighestBlockYAt(xR, zR))
                createUndergroundLake(this.rand.nextInt(this.worldSettings.undergroundLakeMaxSize - this.worldSettings.undergroundLakeMinSize) + this.worldSettings.undergroundLakeMinSize, xR, yR, zR);
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
                        if (world.getTypeId(xLake, yLake, zLake) == 0)
                            continue;
                        double xBounds = (xLake + 0.5D - xAdjusted) / (horizontalSize / 2.0D);
                        double yBounds = (yLake + 0.5D - yAdjusted) / (verticalSize / 2.0D);
                        double zBounds = (zLake + 0.5D - zAdjusted) / (horizontalSize / 2.0D);
                        if (xBounds * xBounds + yBounds * yBounds + zBounds * zBounds >= 1.0D)
                            continue;
                        int uBlock = world.getTypeId(xLake, yLake - 1, zLake);
                        if ((yLake < y + 2) && ((this.worldSettings.undergroundLakesInAir) || (uBlock != 0))) // not air
                            this.world.setRawTypeId(xLake, yLake, zLake, Block.WATER.id);
                        else
                            this.world.setRawTypeId(xLake, yLake, zLake, 0); // Air block
                    }
        }
    }

    private boolean SpawnLiquid(int x, int y, int z, int type)
    {

        if (this.world.getTypeId(x, y + 1, z) != Block.STONE.id)
            return false;
        if (this.world.getTypeId(x, y - 1, z) != Block.STONE.id)
            return false;

        if ((this.world.getTypeId(x, y, z) != 0) && (this.world.getTypeId(x, y, z) != Block.STONE.id))
            return false;

        int i = 0;
        if (this.world.getTypeId(x - 1, y, z) == Block.STONE.id)
            i++;
        if (this.world.getTypeId(x + 1, y, z) == Block.STONE.id)
            i++;
        if (this.world.getTypeId(x, y, z - 1) == Block.STONE.id)
            i++;
        if (this.world.getTypeId(x, y, z + 1) == Block.STONE.id)
            i++;

        int j = 0;
        if (this.world.isEmpty(x - 1, y, z))
            j++;
        if (this.world.isEmpty(x + 1, y, z))
            j++;
        if (this.world.isEmpty(x, y, z - 1))
            j++;
        if (this.world.isEmpty(x, y, z + 1))
            j++;

        if ((i == 3) && (j == 1))
        {
            this.world.setRawTypeId(x, y, z, type);

        }

        return true;
    }

    @Override
    public void populate(org.bukkit.World wrld, Random random, Chunk chunk)
    {
        BlockSand.instaFall = false;

        int chunk_x = chunk.getX();
        int chunk_z = chunk.getZ();

        int x = chunk_x * 16;
        int z = chunk_z * 16;

        BiomeBase localBiomeBase = world.getWorldChunkManager().getBiome(x + 16, z + 16);


        this.rand.setSeed(world.getSeed());
        long l1 = this.rand.nextLong() / 2L * 2L + 1L;
        long l2 = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed(chunk_x * l1 + chunk_z * l2 ^ world.getSeed());


        //ToDo create custom WorldGen objects

        this.processUndergroundDeposits(x, z);
        //System.out.println("Under ground debug: " + x  + " " + z + " " + rand.nextDouble());
        // ToDo add lavaLevelMin and lavaLevelMax here


        if (!this.worldSettings.disableNotchPonds)
        {
            if (this.rand.nextInt(4) == 0)
            {
                int i3 = x + this.rand.nextInt(16);
                int i4 = this.rand.nextInt(127);
                int i5 = z + this.rand.nextInt(16);
                //if (i4 <= this.world.getHighestBlockYAt(i3, i5))
                new WorldGenLakes(Block.STATIONARY_WATER.id).a(this.world, this.rand, i3, i4, i5);
                //System.out.println("Lake debug: " + i3  + " " +i4 + " " + i5 + " " +lake.a(this.world, this.rand, i3, i4, i5) + " " + rand.nextDouble());
            }

            if (this.rand.nextInt(8) == 0)
            {
                int i3 = x + this.rand.nextInt(16);
                int i4 = this.rand.nextInt(this.rand.nextInt(119) + 8);
                int i5 = z + this.rand.nextInt(16);
                if ((i4 < 64) || (this.rand.nextInt(10) == 0))
                    new WorldGenLakes(Block.STATIONARY_LAVA.id).a(this.world, this.rand, i3, i4, i5);
            }
        }


        this.processAboveGroundMaterials(x, z, localBiomeBase);

        //System.out.println("Above ground debug: " + x  + " " + z + " " + rand.nextDouble());

        CustomObjectGen.SpawnCustomObjects(this.world, this.rand, this.worldSettings, x, z, localBiomeBase);

        this.processTrees(x, z, localBiomeBase);

        this.processGrass(x, z, localBiomeBase);


        /*
        int i = 0;

        double[] TemperatureArray = new double[256];
        TemperatureArray = this.world.getWorldChunkManager().a(TemperatureArray, x, z, 16, 16);
        for (int _x = x; _x < x + 16; _x++)
        {
            for (int _z = z; _z < z + 16; _z++)
            {


                int _y = this.world.e(_x, _z);
                double d2 = TemperatureArray[i] - (_y - 64) / 64.0D * 0.3D;
                i++;
                if (!((d2 >= this.worldSettings.snowThreshold) || (_y <= 0) || (_y >= 128) || (!this.world.isEmpty(_x, _y, _z)) || (!this.world.getMaterial(_x, _y - 1, _z).isSolid()) || (this.world.getMaterial(_x, _y - 1, _z) == Material.ICE)))
                    this.world.setRawTypeId(_x, _y, _z, Block.SNOW.id);


            }
            i = 0;
        } */
        if (this.worldSettings.replaceBlocks.size() != 0)
        {
            byte[] blocks = ((CraftChunk) chunk).getHandle().b;

            for (int i = 0; i < blocks.length; i++)
            {
                if (blocks[i] != this.worldSettings.ReplaceBlocksMatrix[blocks[i]])
                    blocks[i] = this.worldSettings.ReplaceBlocksMatrix[blocks[i]];
            }
        }

        ((CraftChunk) chunk).getHandle().initLighting();


        BlockSand.instaFall = true;

        if (this.worldSettings.isDeprecated)
            this.worldSettings = this.worldSettings.newSettings;
    }

}