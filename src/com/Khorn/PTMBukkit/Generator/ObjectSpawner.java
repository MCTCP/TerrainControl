package com.Khorn.PTMBukkit.Generator;

import com.Khorn.PTMBukkit.BiomeConfig;
import com.Khorn.PTMBukkit.CustomObjects.CustomObjectGen;
import com.Khorn.PTMBukkit.WorldConfig;
import net.minecraft.server.*;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class ObjectSpawner extends BlockPopulator
{



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
    }



   /*
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

    }*/


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
    public void populate(org.bukkit.World _world, Random random, Chunk chunk)
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
        //Todo use world.f for sand and clay
        //this.processUndergroundDeposits(x, z);
        //System.out.println("Under ground debug: " + x  + " " + z + " " + rand.nextDouble());
        // ToDo add lavaLevelMin and lavaLevelMax here




        //this.processAboveGroundMaterials(x, z, localBiomeBase);

        //System.out.println("Above ground debug: " + x  + " " + z + " " + rand.nextDouble());

        CustomObjectGen.SpawnCustomObjects(this.world, this.rand, this.worldSettings, x, z, localBiomeBase);

        //this.processTrees(x, z, localBiomeBase);

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