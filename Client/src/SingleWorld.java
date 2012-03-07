import com.Khorn.TerrainControl.*;
import com.Khorn.TerrainControl.Configuration.BiomeConfig;
import com.Khorn.TerrainControl.Configuration.WorldConfig;
import com.Khorn.TerrainControl.Generator.ResourceGens.TreeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class SingleWorld implements LocalWorld
{
    private ChunkProvider generator;
    private wz world;
    private WorldConfig settings;
    private String name;
    private long Seed;
    private IBiomeManager biomeManager;
    private BiomeManagerOld old_biomeManager;

    private static int NextBiomeId = 0;
    private static LocalBiome[] Biomes = new LocalBiome[64];
    private static abi[] BiomesToRestore = new abi[64];
    private HashMap<String, LocalBiome> BiomeNames = new HashMap<String, LocalBiome>();
    private static ArrayList<LocalBiome> DefaultBiomes = new ArrayList<LocalBiome>();


    private wa strongholdGen;
    private al VillageGen;
    private ajb MineshaftGen;

    private re Tree;
    private yd BigTree;
    private j Forest;
    private bc SwampTree;
    private kw TaigaTree1;
    private qe TaigaTree2;
    private pp HugeMushroom;


    private boolean CreateNewChunks;
    private aal[] ChunkCache;
    private aal CachedChunk;

    private int CurrentChunkX;
    private int CurrentChunkZ;

    private zp[] BiomeArray;

    private int height;
    private int DefaultWaterLevel;


    public static void RestoreBiomes()
    {
        for (abi oldBiome : BiomesToRestore)
        {
            if (oldBiome == null)
                continue;
            abi.a[oldBiome.M] = oldBiome;
        }
        NextBiomeId = 0;
        DefaultBiomes.clear();

    }


    public SingleWorld(String _name)
    {
        this.name = _name;

        for (int i = 0; i < DefaultBiome.values().length; i++)
        {
            abi oldBiome = abi.a[i];
            BiomesToRestore[i] = oldBiome;
            CustomBiome custom = new CustomBiome(NextBiomeId++, oldBiome.y);
            custom.CopyBiome(oldBiome);
            Biome biome = new Biome(custom);
            Biomes[biome.getId()] = biome;
            DefaultBiomes.add(biome);
            this.BiomeNames.put(biome.getName(), biome);
        }


    }

    public LocalBiome getNullBiome(String name)
    {
        return null;
    }

    public LocalBiome AddBiome(String name)
    {
        LocalBiome biome = new Biome(new CustomBiome(NextBiomeId++, name));
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
            this.strongholdGen.a(null, this.world, x, z, chunkArray);

        if (this.settings.MineshaftsEnabled)
            this.MineshaftGen.a(null, this.world, x, z, chunkArray);
        if (this.settings.VillagesEnabled && dry)
            this.VillageGen.a(null, this.world, x, z, chunkArray);

    }

    public void PlaceDungeons(Random rand, int x, int y, int z)
    {
        new dl().a(this.world, rand, x, y, z);
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
        new cl(BlockId).a(this.world, rand, x, y, z);
    }

    public void PlaceIce(int x, int z)
    {
        int i1 = x + 8;
        int i2 = z + 8;
        for (int _x = 0; _x < 16; _x++)
        {
            for (int _z = 0; _z < 16; _z++)
            {
                int i5 = this.world.e(i1 + _x, i2 + _z);

                if (this.world.p(_x + i1, i5 - 1, _z + i2))
                {
                    this.world.g(_x + i1, i5 - 1, _z + i2, DefaultMaterial.ICE.id);
                }
                if (this.world.r(_x + i1, i5, _z + i2))
                {
                    this.world.g(_x + i1, i5, _z + i2, DefaultMaterial.SNOW.id);
                }
            }

        }
    }

    public boolean PlaceTerrainObjects(Random rand, int chunk_x, int chunk_z)
    {
        boolean Village = false;
        if (this.settings.StrongholdsEnabled)
            this.strongholdGen.a(this.world, rand, chunk_x, chunk_z);
        if (this.settings.MineshaftsEnabled)
            this.MineshaftGen.a(this.world, rand, chunk_x, chunk_z);
        if (this.settings.VillagesEnabled)
            Village = this.VillageGen.a(this.world, rand, chunk_x, chunk_z);

        return Village;
    }

    public void DoReplace()
    {
        if (this.settings.BiomeConfigsHaveReplacement)
        {

            aal rawaal = this.ChunkCache[0];
            byte[] blocks = rawaal.b;
            this.BiomeArray = this.world.a().b(this.BiomeArray, this.CurrentChunkX * 16, this.CurrentChunkZ * 16, 16, 16);

            int x = this.CurrentChunkX * 16;
            int z = this.CurrentChunkZ * 16;

            for (int _x = 0; _x < 16; _x++)
                for (int _z = 0; _z < 16; _z++)
                {
                    BiomeConfig biomeConfig = this.settings.biomeConfigs[this.BiomeArray[(_x + _z * 16)].K];
                    if (biomeConfig.replaceBlocks.size() > 0)
                    {
                        for (int _y = 127; _y >= 0; _y--)
                        {
                            int i = _x << 11 | _z << 7 | _y;
                            int blockId = blocks[i] & 0xFF;  // Fuck java with signed bytes;
                            int[] replacement = biomeConfig.ReplaceMatrixBlocks[blockId]; // [block ID, block data]
                            if (blockId != replacement[0] || (blockId == replacement[0] && rawaal.g.a(_x, _y, _z) != replacement[1]))
                                if (_y >= biomeConfig.ReplaceMatrixHeightMin[blockId] && _y <= biomeConfig.ReplaceMatrixHeightMax[blockId])
                                {
                                    blocks[i] = (byte) replacement[0];
                                    rawaal.g.a(_x, _y, _z, replacement[1]);
                                    world.j((x + _x), _y, (z + _z));
                                }

                        }
                    }
                }
        }
    }

    public void LoadChunk(int x, int z)
    {
        this.CurrentChunkX = x;
        this.CurrentChunkZ = z;
        this.ChunkCache[0] = this.world.c(x, z);
        this.ChunkCache[1] = this.world.c(x + 1, z);
        this.ChunkCache[2] = this.world.c(x, z + 1);
        this.ChunkCache[3] = this.world.c(x + 1, z + 1);
        this.CreateNewChunks = true;
    }

    private aal getChunk(int x, int y, int z)
    {
        if (y < 0 || y >= this.world.c)
            return null;

        x = x >> 4;
        z = z >> 4;
        if (this.CachedChunk != null && this.CachedChunk.l == x && this.CachedChunk.m == z)
            return this.CachedChunk;
        int index = x - this.CurrentChunkX + 2 * (z - this.CurrentChunkZ);
        if (index >= 0 && index < 4)
            return CachedChunk = this.ChunkCache[index];
        else if (this.CreateNewChunks || this.world.A.a(x, z))
            return CachedChunk = this.world.c(x, z);
        else
            return null;


    }

    public int getLiquidHeight(int x, int z)
    {
        aal chunk = this.getChunk(x, 0, z);
        if (chunk == null)
            return -1;
        z = z & 0xF;
        x = x & 0xF;
        for (int y = world.d; y > 0; y--)
        {
            int blockId = chunk.b[x << 11 | z << 7 | y] & 0xFF;
            if (blockId != 0 && oe.m[blockId].cb.d())
                return y;
        }
        return -1;
    }

    public boolean isEmpty(int x, int y, int z)
    {
        return this.getTypeId(x, y, z) == 0;
    }

    public int getTypeId(int x, int y, int z)
    {
        aal chunk = this.getChunk(x, y, z);
        if (chunk == null)
            return 0;

        z = z & 0xF;
        x = x & 0xF;

        return chunk.b[x << world.b | z << world.a | y] & 0xFF;  //Fuck java !!
    }

    public void setRawBlockIdAndData(int x, int y, int z, int BlockId, int Data)
    {

        aal chunk = this.getChunk(x, y, z);
        if (chunk == null)
            return;
        z = z & 0xF;
        x = x & 0xF;


        chunk.b[x << world.b | z << world.a | y] = (byte) BlockId;
        chunk.g.a(x, y, z, Data);
    }

    public void setRawBlockId(int x, int y, int z, int BlockId)
    {
        aal chunk = this.getChunk(x, y, z);
        if (chunk == null)
            return;
        z = z & 0xF;
        x = x & 0xF;


        chunk.b[x << world.b | z << world.a | y] = (byte) BlockId;
    }

    public void setBlockId(int x, int y, int z, int BlockId)
    {
        this.world.g(x, y, z, BlockId);
    }

    public void setBlockIdAndData(int x, int y, int z, int BlockId, int Data)
    {
        this.world.d(x, y, z, BlockId, Data);
    }

    public int getHighestBlockYAt(int x, int z)
    {
        aal chunk = this.getChunk(x, 0, z);
        if (chunk == null)
            return -1;
        z = z & 0xF;
        x = x & 0xF;
        return chunk.b(x, z);
    }

    public DefaultMaterial getMaterial(int x, int y, int z)
    {
        int id = this.getTypeId(x, y, z);
        return DefaultMaterial.getMaterial(id);
    }

    public void setChunksCreations(boolean createNew)
    {
        this.CreateNewChunks = createNew;
    }

    public int getLightLevel(int x, int y, int z)
    {
        return world.n(x, y, z);
    }

    public boolean isLoaded(int x, int y, int z)
    {
        if (y < 0 || y >= this.world.c)
            return false;
        x = x >> 4;
        z = z >> 4;

        return world.A.a(x, z);
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
        return this.height;
    }

    public int getWaterLevel()
    {
        return this.DefaultWaterLevel;
    }

    public int getHeightBits()
    {
        return world.a;
    }

    public ChunkProvider getChunkGenerator()
    {
        return this.generator;
    }

    public void setSettings(WorldConfig worldConfig)
    {
        this.settings = worldConfig;
    }

    public void InitM(wz _world)
    {
        this.world = _world;
        this.Seed = world.t();
        this.world.e = this.settings.waterLevelMax;

    }

    public void Init(wz _world)
    {
        this.world = _world;
        this.Seed = world.t();
        this.world.e = this.settings.waterLevelMax;
        this.DefaultWaterLevel = this.settings.waterLevelMax;


        this.strongholdGen = new wa();
        this.VillageGen = new al(0);
        this.MineshaftGen = new ajb();

        this.Tree = new re(false);
        this.BigTree = new yd(false);
        this.Forest = new j(false);
        this.SwampTree = new bc();
        this.TaigaTree1 = new kw();
        this.TaigaTree2 = new qe(false);
        this.HugeMushroom = new pp();

        this.ChunkCache = new aal[4];
        this.generator = new ChunkProvider(this);
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

    public wz getWorld()
    {
        return this.world;
    }

    public wa getStrongholdGen()
    {
        return this.strongholdGen;
    }

    public void setHeight(int _height)
    {
        this.height = _height;
    }

    public void setWaterLevel(int waterLevel)
    {
        this.DefaultWaterLevel = waterLevel;
    }
}
