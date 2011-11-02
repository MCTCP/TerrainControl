package com.Khorn.TerrainControl.Generator;

import com.Khorn.TerrainControl.Configuration.BiomeConfig;
import com.Khorn.TerrainControl.Configuration.Resource;
import com.Khorn.TerrainControl.Configuration.WorldConfig;
import com.Khorn.TerrainControl.CustomObjects.CustomObjectGen;
import com.Khorn.TerrainControl.Generator.ResourceGens.LiquidGen;
import com.Khorn.TerrainControl.Generator.ResourceGens.OreGen;
import com.Khorn.TerrainControl.Generator.TerrainsGens.BiomeObjectsGen;
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
    private BiomeBase[] BiomeArray;

    private OreGen oreGen;
    private LiquidGen liquidGen;

    private BiomeObjectsGen[] BiomeGenerators;

    public ObjectSpawner(WorldConfig wrk)
    {
        this.worldSettings = wrk;
        this.rand = new Random();
        this.worldSettings.objectSpawner = this;
    }

    public void Init(World world)
    {
        this.world = world;
        this.oreGen = new OreGen(this.world);
        this.liquidGen = new LiquidGen(this.world);
        InitGenerators();
    }

    private void InitGenerators()
    {
        this.BiomeGenerators = new BiomeObjectsGen[this.worldSettings.biomeConfigs.length];
        for (int i = 0; i < this.worldSettings.biomeConfigs.length; i++)
            this.BiomeGenerators[i] = new BiomeObjectsGen(this.world, this.worldSettings.biomeConfigs[i], BiomeBase.a[i]);
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

    private void ProcessResource(Resource res, int x,int z)
    {
        switch (res.Type)
        {
            case Ore:
                this.oreGen.Process(this.rand,res,x,z);
                break;
            case UnderWaterOre:
                break;
            case Flower:
                break;
            case Liquid:
                this.liquidGen.Process(this.rand,res,x,z);
                break;
        }

    }

    @Override
    public void populate(org.bukkit.World _world, Random random, Chunk chunk)
    {

        int chunk_x = chunk.getX();
        int chunk_z = chunk.getZ();

        int x = chunk_x * 16;
        int z = chunk_z * 16;

        BiomeBase localBiomeBase = world.getWorldChunkManager().getBiome(x + 16, z + 16);


        this.rand.setSeed(world.getSeed());
        long l1 = this.rand.nextLong() / 2L * 2L + 1L;
        long l2 = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed(chunk_x * l1 + chunk_z * l2 ^ world.getSeed());


        boolean Village = false;
        if (this.worldSettings.StrongholdsEnabled)
            this.worldSettings.ChunkProvider.strongholdGen.a(this.world, this.rand, chunk_x, chunk_z);
        if (this.worldSettings.MineshaftsEnabled)
            this.worldSettings.ChunkProvider.MineshaftGen.a(this.world, this.rand, chunk_x, chunk_z);
        if (this.worldSettings.VillagesEnabled)
            Village = this.worldSettings.ChunkProvider.VillageGen.a(this.world, this.rand, chunk_x, chunk_z);


        BiomeObjectsGen biomeGen = this.BiomeGenerators[localBiomeBase.y];
        biomeGen.SetChunk(((CraftChunk) chunk).getHandle());

        if (!Village)
            biomeGen.ProcessUndergroundObjects(x, z, this.rand);

        if (this.worldSettings.undergroundLakes)
            this.processUndergroundLakes(x, z);

        BiomeConfig localBiomeConfig = this.worldSettings.biomeConfigs[localBiomeBase.y];


        //First resource sequence
        for(int i = 0; i<localBiomeConfig.FirstResourceCount; i++)
            this.ProcessResource(localBiomeConfig.FirstResourceSequence[i],x,z);

        biomeGen.ProcessOres(x, z, this.rand);

        CustomObjectGen.SpawnCustomObjects(this.world, this.rand, this.worldSettings, x + 8, z + 8, localBiomeBase);

        biomeGen.ProcessTrees(x, z, this.rand);

        biomeGen.ProcessAboveGround(x, z, this.rand);


        if (this.worldSettings.BiomeConfigsHaveReplacement)
        {
            byte[] blocks = ((CraftChunk) chunk).getHandle().b;

            for (int _x = 0; _x < 16; _x++)
                for (int _z = 0; _z < 16; _z++)
                {
                    this.BiomeArray = this.world.getWorldChunkManager().a(this.BiomeArray, chunk_x * 16, chunk_z * 16, 16, 16);
                    BiomeConfig biomeConfig = this.worldSettings.biomeConfigs[this.BiomeArray[(_z + _x * 16)].y];
                    if (biomeConfig.replaceBlocks.size() > 0)
                    {
                        for (int _y = 127; _y >= 0; _y--)
                        {
                            int i = (_z * 16 + _x) * 128 + _y;
                            if (blocks[i] != biomeConfig.ReplaceMatrixBlocks[blocks[i]])
                                if (_y >= biomeConfig.ReplaceMatrixHeightMin[i] && _y <= biomeConfig.ReplaceMatrixHeightMax[i])
                                    blocks[i] = biomeConfig.ReplaceMatrixBlocks[blocks[i]];

                        }
                    }
                }
        }

        SpawnerCreature.a(this.world, localBiomeBase, x + 8, z + 8, 16, 16, this.rand);


        if (this.worldSettings.isDeprecated)
        {
            this.worldSettings = this.worldSettings.newSettings;
            InitGenerators();
        }
    }

}