package com.Khorn.TerrainControl.Bukkit;

import com.Khorn.TerrainControl.*;
import com.Khorn.TerrainControl.Bukkit.BiomeManager.BiomeManagerOld;
import com.Khorn.TerrainControl.IBiomeManager;
import com.Khorn.TerrainControl.Configuration.BiomeConfig;
import com.Khorn.TerrainControl.Configuration.WorldConfig;
import com.Khorn.TerrainControl.Generator.ResourceGens.TreeType;
import net.minecraft.server.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.craftbukkit.CraftWorld;

public class BukkitWorld implements LocalWorld
{
    private TCChunkGenerator generator;
    private World world;
    private WorldConfig settings;
    private String name;
    private long Seed;
    private IBiomeManager biomeManager;
    private BiomeManagerOld old_biomeManager;

    private static int NextBiomeId = DefaultBiome.values().length;
    private static LocalBiome[] Biomes = new LocalBiome[64];
    private HashMap<String, LocalBiome> BiomeNames = new HashMap<String, LocalBiome>();
    private static ArrayList<LocalBiome> DefaultBiomes = new ArrayList<LocalBiome>();


    private WorldGenStronghold strongholdGen;
    private WorldGenVillage VillageGen;
    private WorldGenMineshaft MineshaftGen;

    private WorldGenTrees Tree;
    private WorldGenBigTree BigTree;
    private WorldGenForest Forest;
    private WorldGenSwampTree SwampTree;
    private WorldGenTaiga1 TaigaTree1;
    private WorldGenTaiga2 TaigaTree2;
    private WorldGenHugeMushroom HugeMushroom;


    private boolean CreateNewChunks;
    private Chunk[] ChunkCache;
    private Chunk CachedChunk;
    
    private int CurrentChunkX;
    private int CurrentChunkZ;

    private BiomeBase[] BiomeArray;

    static
    {
        for (int i = 0; i < DefaultBiome.values().length; i++)
        {
            Biomes[i] = new BukkitBiome(BiomeBase.biomes[i]);
            DefaultBiomes.add(Biomes[i]);
        }
    }

    public BukkitWorld(String _name)
    {
        this.name = _name;
        for (LocalBiome biome : DefaultBiomes)
        {
            this.BiomeNames.put(biome.getName(), biome);
        }
    }

    public LocalBiome getNullBiome(String name)
    {
        return new NullBiome(name);
    }

    public LocalBiome AddBiome(String name)
    {
        LocalBiome biome = new BukkitBiome(new CustomBiome(NextBiomeId++, name));
        Biomes[biome.getId()] = biome;
        this.BiomeNames.put(biome.getName(), biome);
        return biome;
    }

    public int getBiomesCount()
    {
        return NextBiomeId;
    }

    public LocalBiome getBiomeById(int id)
    {
        return Biomes[id];
    }

    public LocalBiome getBiomeByName(String name)
    {
        return this.BiomeNames.get(name);
    }

    public int getBiomeIdByName(String name)
    {
        return this.BiomeNames.get(name).getId();
    }

    public ArrayList<LocalBiome> getDefaultBiomes()
    {
        return DefaultBiomes;
    }

    public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int x_size, int z_size)
    {
        return this.biomeManager.getBiomesUnZoomedTC(biomeArray, x, z, x_size, z_size);
    }

    public float[] getTemperatures(int x, int z, int x_size, int z_size)
    {
        return this.biomeManager.getTemperaturesTC(x, z, x_size, z_size);
    }

    public int[] getBiomes(int[] biomeArray, int x, int z, int x_size, int z_size)
    {
        return this.biomeManager.getBiomesTC(biomeArray, x, z, x_size, z_size);
    }

    public int getBiome(int x, int z)
    {
        return this.biomeManager.getBiomeTC(x, z);
    }

    public LocalBiome getLocalBiome(int x, int z)
    {
        return Biomes[this.getBiome(x, z)];
    }

    public double getBiomeFactorForOldBM(int index)
    {
        return this.old_biomeManager.old_temperature[index] * this.old_biomeManager.old_rain[index];
    }

    public void PrepareTerrainObjects(int x, int z, byte[] chunkArray, boolean dry)
    {
        if (this.settings.StrongholdsEnabled)
        {
            this.strongholdGen.a(null, this.world, x, z, chunkArray);
        }

        if (this.settings.MineshaftsEnabled)
        {
            this.MineshaftGen.a(null, this.world, x, z, chunkArray);
        }
            
        if (this.settings.VillagesEnabled && dry)
        {
            this.VillageGen.a(null, this.world, x, z, chunkArray);
        }
    }

    public void PlaceDungeons(Random rand, int x, int y, int z)
    {
        new WorldGenDungeons().a(this.world, rand, x, y, z);
    }

    public void PlaceTree(TreeType type, Random rand, int x, int y, int z)
    {
        switch (type)
        {
            case Tree:
                Tree.a(this.world, rand, x, y, z);
                break;
            case BigTree:
                BigTree.a(1.0D, 1.0D, 1.0D);
                BigTree.a(this.world, rand, x, y, z);
                break;
            case Forest:
                Forest.a(this.world, rand, x, y, z);
                break;
            case HugeMushroom:
                HugeMushroom.a(1.0D, 1.0D, 1.0D);
                HugeMushroom.a(this.world, rand, x, y, z);
                break;
            case SwampTree:
                SwampTree.a(this.world, rand, x, y, z);
                break;
            case Taiga1:
                TaigaTree1.a(this.world, rand, x, y, z);
                break;
            case Taiga2:
                TaigaTree2.a(this.world, rand, x, y, z);
                break;
        }
    }

    public void PlacePonds(int BlockId, Random rand, int x, int y, int z)
    {
        new WorldGenLakes(BlockId).a(this.world, rand, x, y, z);
    }

    // TODO: Some comments here would be nice. is this chunk or block coordinates?
    public void PlaceIce(int x, int z)
    {
        int i1 = x + 8;
        int i2 = z + 8;
        for (int _x = 0; _x < 16; _x++)
        {
            for (int _z = 0; _z < 16; _z++)
            {
                int i5 = this.world.e(i1 + _x, i2 + _z); // TODO - Fix obfuscation

                if (this.world.p(_x + i1, i5 - 1, _z + i2)) // TODO - Fix obfuscation
                {
                    this.world.setTypeId(_x + i1, i5 - 1, _z + i2, Block.ICE.id);
                }
                if (this.world.r(_x + i1, i5, _z + i2)) // TODO - Fix obfuscation
                {
                    this.world.setTypeId(_x + i1, i5, _z + i2, Block.SNOW.id);
                }
            }
        }
    }

    public boolean PlaceTerrainObjects(Random rand, int chunk_x, int chunk_z)
    {
        boolean Village = false;
        if (this.settings.StrongholdsEnabled)
            this.strongholdGen.a(this.world, rand, chunk_x, chunk_z); // TODO - Fix obfuscation
        if (this.settings.MineshaftsEnabled)
            this.MineshaftGen.a(this.world, rand, chunk_x, chunk_z); // TODO - Fix obfuscation
        if (this.settings.VillagesEnabled)
            Village = this.VillageGen.a(this.world, rand, chunk_x, chunk_z); // TODO - Fix obfuscation

        return Village;
    }

    // TODO: Do replace what with what????
    public void DoReplace()
    {
        if (this.settings.BiomeConfigsHaveReplacement)
        {
            Chunk rawChunk = this.ChunkCache[0];
            byte[] blocks = rawChunk.b; // TODO - Fix obfuscation
            this.BiomeArray = this.world.getWorldChunkManager().getBiomeBlock(this.BiomeArray, this.CurrentChunkX * 16, this.CurrentChunkZ * 16, 16, 16);

            int x = this.CurrentChunkX * 16;
            int z = this.CurrentChunkZ * 16;

            for (int _x = 0; _x < 16; _x++)
            {
                for (int _z = 0; _z < 16; _z++)
                {
                    BiomeConfig biomeConfig = this.settings.biomeConfigs[this.BiomeArray[(_x + _z * 16)].id];
                    if (biomeConfig.replaceBlocks.size() > 0)
                    {
                        for (int _y = 127; _y >= 0; _y--)
                        {
                            int i = _x << 11 | _z << 7 | _y;
                            int blockId = blocks[i] & 0xFF;  // Fuck java with signed bytes;
                            int[] replacement = biomeConfig.ReplaceMatrixBlocks[blockId]; // [block ID, block data]
                            if (blockId != replacement[0] || (blockId == replacement[0] && rawChunk.g.a(_x, _y, _z) != replacement[1])) // TODO - Fix obfuscation
                            {
                                if (_y >= biomeConfig.ReplaceMatrixHeightMin[blockId] && _y <= biomeConfig.ReplaceMatrixHeightMax[blockId])
                                {
                                    blocks[i] = (byte) replacement[0];
                                    rawChunk.g.a(_x, _y, _z, replacement[1]); // TODO - Fix obfuscation
                                    world.notify((x + _x), _y, (z + _z));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void LoadChunk(Chunk chunk)
    {
        this.CurrentChunkX = chunk.x;
        this.CurrentChunkZ = chunk.z;
        this.ChunkCache[0] = chunk;
        this.ChunkCache[1] = this.world.getChunkAt(chunk.x + 1, chunk.z);
        this.ChunkCache[2] = this.world.getChunkAt(chunk.x, chunk.z + 1);
        this.ChunkCache[3] = this.world.getChunkAt(chunk.x + 1, chunk.z + 1);
        this.CreateNewChunks = true;
    }

    private Chunk getChunk(int x, int y, int z)
    {
        if (y < 0 || y >= 256) // TODO: Should this be hard-coded like this?
            return null;

        x = x >> 4;
        z = z >> 4;
        if(this.CachedChunk != null && this.CachedChunk.x == x && this.CachedChunk.z == z)
            return this.CachedChunk;
        int index = x - this.CurrentChunkX + 2 * (z - this.CurrentChunkZ);
        if (index >= 0 && index < 4)
            return CachedChunk = this.ChunkCache[index];
        else if (this.CreateNewChunks || this.world.chunkProvider.isChunkLoaded(x, z))
            return CachedChunk = this.world.getChunkAt(x, z);
        else
            return null;

    }

    public int getLiquidHeight(int x, int z)
    {
        Chunk chunk = this.getChunk(x, 0, z);
        if (chunk == null)
            return -1;
        z = z & 0xF;
        x = x & 0xF;
        for (int y = 255; y > 0; y--) // TODO: Should this be hard-coded like this?
        {
            int blockId = chunk.b[x << 11 | z << 7 | y] & 0xFF; // TODO - Fix obfuscation - Could also be ".heightMap"
            if (blockId != 0 && Block.byId[blockId].material.isLiquid())
                return y;
        }
        return -1;
    }

    public boolean isEmpty(int x, int y, int z)
    {
        return this.getRawBlockId(x, y, z) == 0;
    }

    public int getRawBlockId(int x, int y, int z)
    {
        return this.world.getTypeId(x, y, z); // Would this be ok?
        /*
        Chunk chunk = this.getChunk(x, y, z);
        if (chunk == null)
        {
            return 0;
        }

        z = z & 0xF;
        x = x & 0xF;

        return chunk.b[x << 11 | z << 7 | y] & 0xFF;  //Fuck java !! // TODO - Fix obfuscation - Could also be ".heightMap"
        */
    }

    public void setRawBlockIdAndData(int x, int y, int z, int BlockId, int Data)
    {
        this.world.setRawTypeIdAndData(x, y, z, BlockId, Data); // Would this be ok? - Note it will check light! Do we want that?
        /*
        Chunk chunk = this.getChunk(x, y, z);
        if (chunk == null)
            return;
        z = z & 0xF;
        x = x & 0xF;

        chunk.b[x << 11 | z << 7 | y] = (byte) BlockId; // TODO - Fix obfuscation
        chunk.g.a(x, y, z, Data); // TODO - Fix obfuscation
        */
    }

    public void setRawBlockId(int x, int y, int z, int BlockId)
    {
        this.world.setRawTypeId(x, y, z, BlockId); // Would this be ok? - Note it will check light! Do we want that?
        /*
        Chunk chunk = this.getChunk(x, y, z);
        if (chunk == null)
            return;
        z = z & 0xF;
        x = x & 0xF;

        chunk.b[x << 11 | z << 7 | y] = (byte) BlockId; // TODO - Fix obfuscation
        */
    }

    public void setBlockId(int x, int y, int z, int BlockId)
    {
        this.world.setTypeId(x, y, z, BlockId);
    }

    public void setBlockIdAndData(int x, int y, int z, int BlockId, int Data)
    {
        this.world.setTypeIdAndData(x, y, z, BlockId, Data);
    }

    public int getHighestBlockYAt(int x, int z)
    {
        Chunk chunk = this.getChunk(x, 0, z);
        if (chunk == null)
        {
            return -1;
        }
        z = z & 0xF;
        x = x & 0xF;
        return chunk.b(x, z); // TODO - Fix obfuscation
    }

    public DefaultMaterial getMaterial(int x, int y, int z)
    {
        int id = this.getRawBlockId(x, y, z);
        return DefaultMaterial.getMaterial(id);
    }

    public void setChunksCreations(boolean createNew)
    {
        this.CreateNewChunks = createNew;
    }

    public int getLightLevel(int x, int y, int z)
    {
        return world.getLightLevel(x, y, z);
    }

    public boolean isLoaded(int x, int y, int z)
    {
        return world.isLoaded(x, y, z);
    }

    public WorldConfig getSettings()
    {
        return this.settings;
    }

    public String getName()
    {
        return this.name;
    }

    public long getSeed()
    {
        return this.Seed;
    }

    public int getHeight()
    {
        return 256; //TODO do something with that when bukkit allow custom world height.
    }
    public int getWaterLevel()
    {
        return 63; // TODO same as height
    }

    public int getHeightBits()
    {
        return world.heightBits; // TODO - Fix obfuscation
    }

    public TCChunkGenerator getChunkGenerator()
    {
        return this.generator;
    }

    public void setSettings(WorldConfig worldConfig)
    {
        this.settings = worldConfig;
    }

    public void Init(World _world)
    {
        this.world = _world;
        this.Seed = world.getSeed();
        this.world.seaLevel = this.settings.waterLevelMax; // TODO - Fix obfuscation

        this.generator.Init(this);

        this.strongholdGen = new WorldGenStronghold();
        this.VillageGen = new WorldGenVillage(0);
        this.MineshaftGen = new WorldGenMineshaft();

        this.Tree = new WorldGenTrees(false);
        this.BigTree = new WorldGenBigTree(false);
        this.Forest = new WorldGenForest(false);
        this.SwampTree = new WorldGenSwampTree();
        this.TaigaTree1 = new WorldGenTaiga1();
        this.TaigaTree2 = new WorldGenTaiga2(false);
        this.HugeMushroom = new WorldGenHugeMushroom();

        this.ChunkCache = new Chunk[4];
    }

    public void setChunkGenerator(TCChunkGenerator _generator)
    {
        this.generator = _generator;
    }

    public void setBiomeManager(IBiomeManager manager)
    {
        this.biomeManager = manager;
    }

    public void setOldBiomeManager(BiomeManagerOld manager)
    {
        this.old_biomeManager = manager;
        this.biomeManager = manager;
    }
}