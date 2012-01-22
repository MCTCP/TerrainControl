package com.Khorn.TerrainControl.Generator;

import com.Khorn.TerrainControl.Configuration.BiomeConfig;
import com.Khorn.TerrainControl.Configuration.Resource;
import com.Khorn.TerrainControl.Configuration.WorldConfig;
import com.Khorn.TerrainControl.CustomObjects.CustomObjectGen;
import com.Khorn.TerrainControl.Generator.ResourceGens.*;
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
    private UnderWaterOreGen underWaterOreGen;
    private PlantGen plantGen;
    private GrassGen grassGen;
    private ReedGen reedGen;
    private CactusGen cactusGen;
    private DungeonGen dungeonGen;
    private TreeGen treeGen;
    private UndergroundLakeGen undergroundLakeGen;
    private AboveWaterGen aboveWaterGen;


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
        this.underWaterOreGen = new UnderWaterOreGen(this.world);
        this.plantGen = new PlantGen(this.world);
        this.grassGen = new GrassGen(this.world);
        this.reedGen = new ReedGen(this.world);
        this.cactusGen = new CactusGen(this.world);
        this.dungeonGen = new DungeonGen(this.world);
        this.treeGen = new TreeGen(this.world);
        this.undergroundLakeGen = new UndergroundLakeGen(this.world);
        this.aboveWaterGen = new AboveWaterGen(this.world);
    }


    private void ProcessResource(Resource res, int x, int z, BiomeBase localBiomeBase)
    {
        switch (res.Type)
        {
            case Ore:
                this.oreGen.Process(this.rand, res, x, z);
                break;
            case UnderWaterOre:
                this.underWaterOreGen.Process(this.rand, res, x, z);
                break;
            case Plant:
                this.plantGen.Process(this.rand, res, x, z);
                break;
            case Liquid:
                this.liquidGen.Process(this.rand, res, x, z);
                break;
            case Grass:
                this.grassGen.Process(this.rand, res, x, z);
                break;
            case Reed:
                this.reedGen.Process(this.rand, res, x, z);
                break;
            case Cactus:
                this.cactusGen.Process(this.rand, res, x, z);
                break;
            case Dungeon:
                this.dungeonGen.Process(this.rand, res, x, z);
                break;
            case Tree:
                this.treeGen.Process(this.rand, res, x, z);
                break;
            case CustomObject:
                CustomObjectGen.SpawnCustomObjects(this.world, this.rand, this.worldSettings, x + 8, z + 8, localBiomeBase);
                break;
            case UnderGroundLake:
                this.undergroundLakeGen.Process(this.rand, res, x, z);
                break;
            case AboveWaterRes:
                this.aboveWaterGen.Process(this.rand, res, x, z);
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
        BiomeConfig localBiomeConfig = this.worldSettings.biomeConfigs[localBiomeBase.F];


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


        if (!Village)
        {
            if (!localBiomeConfig.disableNotchPonds)
            {

                if (this.rand.nextInt(4) == 0)
                {
                    int _x = x + this.rand.nextInt(16) + 8;
                    int _y = this.rand.nextInt(127);
                    int _z = z + this.rand.nextInt(16) + 8;
                    new WorldGenLakes(Block.STATIONARY_WATER.id).a(this.world, this.rand, _x, _y, _z);
                }

                if (this.rand.nextInt(8) == 0)
                {
                    int _x = x + this.rand.nextInt(16) + 8;
                    int _y = this.rand.nextInt(this.rand.nextInt(119) + 8);
                    int _z = z + this.rand.nextInt(16) + 8;
                    if ((_y < this.worldSettings.waterLevelMax) || (this.rand.nextInt(10) == 0))
                        new WorldGenLakes(Block.STATIONARY_LAVA.id).a(this.world, this.rand, _x, _y, _z);
                }
            }
        }


        //Resource sequence
        for (int i = 0; i < localBiomeConfig.ResourceCount; i++)
            this.ProcessResource(localBiomeConfig.ResourceSequence[i], x, z, localBiomeBase);

        int i1 = x + 8;
        int i2 = z + 8;
        for (int _x = 0; _x < 16; _x++)
        {
            for (int _z = 0; _z < 16; _z++)
            {
                int i5 = this.world.e(i1 + _x, i2 + _z);

                if (this.world.p(_x + i1, i5 - 1, _z + i2))
                {
                    this.world.setTypeId(_x + i1, i5 - 1, _z + i2, Block.ICE.id);
                }
                if (this.world.r(_x + i1, i5, _z + i2))
                {
                    this.world.setTypeId(_x + i1, i5, _z + i2, Block.SNOW.id);
                }
            }

        }


        if (this.worldSettings.BiomeConfigsHaveReplacement)
        {
            byte[] blocks = ((CraftChunk) chunk).getHandle().b;
            this.BiomeArray = this.world.getWorldChunkManager().a(this.BiomeArray, chunk_x * 16, chunk_z * 16, 16, 16);

            for (int _x = 0; _x < 16; _x++)
                for (int _z = 0; _z < 16; _z++)
                {
                    BiomeConfig biomeConfig = this.worldSettings.biomeConfigs[this.BiomeArray[(_z + _x * 16)].F];
                    if (biomeConfig.replaceBlocks.size() > 0)
                    {
                        for (int _y = 127; _y >= 0; _y--)
                        {
                            int i = (_z * 16 + _x) * 128 + _y;
                            int blockId = blocks[i] & 0xFF;  // Fuck java with signed bytes;
                            int[] replacement = biomeConfig.ReplaceMatrixBlocks[blockId]; // [block ID, block data]
                            if (_y >= biomeConfig.ReplaceMatrixHeightMin[blockId] && _y <= biomeConfig.ReplaceMatrixHeightMax[blockId])
                            {
                            	world.setRawTypeIdAndData((x + _z), _y, (z + _x), replacement[0], replacement[1]);
                            	world.notify((x + _x), _y, (z + _z));
                            }
                        }
                    }
                }
        }

        SpawnerCreature.a(this.world, localBiomeBase, x + 8, z + 8, 16, 16, this.rand);


        if (this.worldSettings.isDeprecated)
            this.worldSettings = this.worldSettings.newSettings;
    }

}
