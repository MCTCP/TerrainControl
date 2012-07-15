import com.khorn.terraincontrol.*;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.resourcegens.TreeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class SingleWorld implements LocalWorld
{
    private ChunkProvider generator;
    private xd world;
    private WorldConfig settings;
    private String name;
    private long Seed;
    private IBiomeManager biomeManager;
    private BiomeManagerOld old_biomeManager;

    private static int NextBiomeId = 0;
    private static int maxBiomeCount = 256;
    private static LocalBiome[] Biomes = new LocalBiome[maxBiomeCount];
    private static abn[] BiomesToRestore = new abn[maxBiomeCount];

    private HashMap<String, LocalBiome> BiomeNames = new HashMap<String, LocalBiome>();

    private static ArrayList<LocalBiome> DefaultBiomes = new ArrayList<LocalBiome>();


    private xq strongholdGen;
    private an VillageGen;
    private alo MineshaftGen;

    private dq DungeonGen;

    private sb Tree;
    private zz BigTree;
    private i Forest;
    private bf SwampTree;
    private lo TaigaTree1;
    private rb TaigaTree2;
    private qm HugeMushroom;
    private ajd JungleTree;
    private agm GroundBush;


    private boolean CreateNewChunks;
    private ack[] ChunkCache;
    private ack CachedChunk;

    private int CurrentChunkX;
    private int CurrentChunkZ;

    private abn[] BiomeArray;


    private int worldHeight = 128;
    private int heightBits = 7;


    public static void RestoreBiomes()
    {
        for (abn oldBiome : BiomesToRestore)
        {
            if (oldBiome == null)
                continue;
            abn.a[oldBiome.M] = oldBiome;
        }
        NextBiomeId = 0;
        DefaultBiomes.clear();
        an.a = Arrays.asList(abn.c, abn.d);

    }


    public SingleWorld(String _name)
    {
        this.name = _name;

        for (int i = 0; i < DefaultBiome.values().length; i++)
        {
            abn oldBiome = abn.a[i];
            BiomesToRestore[i] = oldBiome;
            CustomBiome custom = new CustomBiome(NextBiomeId++, oldBiome.y);
            custom.CopyBiome(oldBiome);
            Biome biome = new Biome(custom);
            Biomes[biome.getId()] = biome;
            DefaultBiomes.add(biome);
            this.BiomeNames.put(biome.getName(), biome);
        }
        an.a = Arrays.asList(abn.a[DefaultBiome.PLAINS.Id], abn.a[DefaultBiome.DESERT.Id]);


    }

    public LocalBiome getNullBiome(String name)
    {
        return null;
    }

    public LocalBiome AddBiome(String name, int id)
    {
        LocalBiome biome = new Biome(new CustomBiome(id, name));
        Biomes[biome.getId()] = biome;
        this.BiomeNames.put(biome.getName(), biome);
        return biome;
    }

    public int getMaxBiomesCount()
    {
        return maxBiomeCount;
    }

    public int getFreeBiomeId()
    {
        return NextBiomeId++;
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
        DungeonGen.a(this.world, rand, x, y, z);
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
            case JungleTree:
                JungleTree.a(this.world, rand, x, y, z);
                break;
            case GroundBush:
                GroundBush.a(this.world, rand, x, y, z);
                break;
        }
    }

    public void PlacePonds(int BlockId, Random rand, int x, int y, int z)
    {
        new cs(BlockId).a(this.world, rand, x, y, z);
    }

    public void PlaceIce(int x, int z)
    {
        int i1 = x + 8;
        int i2 = z + 8;
        for (int _x = 0; _x < 16; _x++)
        {
            for (int _z = 0; _z < 16; _z++)
            {
                int i5 = this.world.f(i1 + _x, i2 + _z);

                if (this.world.r(_x + i1, i5 - 1, _z + i2))
                {
                    this.world.g(_x + i1, i5 - 1, _z + i2, DefaultMaterial.ICE.id);
                }
                if (this.world.t(_x + i1, i5, _z + i2))
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

            ack rawChunk = this.ChunkCache[0];


            zg[] sectionsArray = rawChunk.i();

            this.BiomeArray = this.world.i().b(this.BiomeArray, this.CurrentChunkX * 16, this.CurrentChunkZ * 16, 16, 16);

            int x = this.CurrentChunkX * 16;
            int z = this.CurrentChunkZ * 16;

            for (zg section : sectionsArray)
            {
                if (section == null)
                    continue;

                for (int sectionX = 0; sectionX < 16; sectionX++)
                {
                    for (int sectionZ = 0; sectionZ < 16; sectionZ++)
                    {
                        BiomeConfig biomeConfig = this.settings.biomeConfigs[this.BiomeArray[(sectionX + sectionZ * 16)].M];

                        if (biomeConfig.ReplaceCount > 0)
                        {
                            for (int sectionY = 0; sectionY < 16; sectionY++)
                            {
                                int blockId = section.a(sectionX, sectionY, sectionZ);
                                if (biomeConfig.ReplaceMatrixBlocks[blockId] == null)
                                    continue;

                                int replaceTo = biomeConfig.ReplaceMatrixBlocks[blockId][section.c() + sectionY];
                                if (replaceTo == -1)
                                    continue;

                                section.a(sectionX, sectionY, sectionZ, replaceTo >> 4);
                                section.b(sectionX, sectionY, sectionZ, replaceTo & 0xF);
                                world.k((x + sectionX), (section.c() + sectionY), (z + sectionZ));

                            }
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
        this.ChunkCache[0] = this.world.d(x, z);
        this.ChunkCache[1] = this.world.d(x + 1, z);
        this.ChunkCache[2] = this.world.d(x, z + 1);
        this.ChunkCache[3] = this.world.d(x + 1, z + 1);
        this.CreateNewChunks = true;
    }

    private ack getChunk(int x, int y, int z)
    {
        if (y < 0 || y >= this.worldHeight)
            return null;

        x = x >> 4;
        z = z >> 4;
        if (this.CachedChunk != null && this.CachedChunk.g == x && this.CachedChunk.h == z)
            return this.CachedChunk;
        int index = x - this.CurrentChunkX + 2 * (z - this.CurrentChunkZ);
        if (index >= 0 && index < 4)
            return CachedChunk = this.ChunkCache[index];
        else if (this.CreateNewChunks || this.world.z().a(x, z))
            return CachedChunk = this.world.d(x, z);
        else
            return null;


    }

    public int getLiquidHeight(int x, int z)
    {
        ack chunk = this.getChunk(x, 0, z);
        if (chunk == null)
            return -1;
        z = z & 0xF;
        x = x & 0xF;
        for (int y = worldHeight - 1; y > 0; y--)
        {
            int blockId = chunk.a(x, y, z);
            if (blockId != 0 && DefaultMaterial.contains(blockId) && DefaultMaterial.getMaterial(blockId).isLiquid())
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
        ack chunk = this.getChunk(x, y, z);
        if (chunk == null)
            return 0;

        z = z & 0xF;
        x = x & 0xF;

        return chunk.a(x, y, z);
    }

    public void setBlock(final int x, final int y, final int z, final int typeId, final int data, final boolean updateLight, final boolean applyPhysics, final boolean notifyPlayers)
    {
        // If minecraft was updated and obfuscation is off - take a look at these methods:
        // this.world.setRawTypeIdAndData(i, j, k, l, i1)
        // this.world.setTypeIdAndData(i, j, k, l, i1)

        // We fetch the chunk from a custom cache in order to speed things up.
        ack chunk = this.getChunk(x, y, z);
        if (chunk == null)
        {
            return;
        }

        if (applyPhysics)
        {
            int oldTypeId = chunk.a(x & 15, y, z & 15);
            chunk.a(x & 15, y, z & 15, typeId, data);
            this.world.j(x, y, z, typeId == 0 ? oldTypeId : typeId);
        } else
            chunk.a(x & 15, y, z & 15, typeId, data); // Set typeId and Data


        if (updateLight)
        {
            this.world.v(x, y, z);
        }

        if (notifyPlayers)
        {
            this.world.k(x, y, z);
        }
    }

    public void setBlock(final int x, final int y, final int z, final int typeId, final int data)
    {
        this.setBlock(x, y, z, typeId, data, false, false, false);
    }

    public int getHighestBlockYAt(int x, int z)
    {
        ack chunk = this.getChunk(x, 0, z);
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
        if (y < 0 || y >= this.worldHeight)
            return false;
        x = x >> 4;
        z = z >> 4;

        return world.z().a(x, z);
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
        return this.worldHeight;
    }


    public int getHeightBits()
    {
        return heightBits;
    }

    public ChunkProvider getChunkGenerator()
    {
        return this.generator;
    }

    public void setSettings(WorldConfig worldConfig)
    {
        this.settings = worldConfig;
    }

    public void InitM(xd _world)
    {
        this.world = _world;
        this.Seed = world.v();

    }

    public void Init(xd _world)
    {
        this.world = _world;
        this.Seed = world.v();
        //this.world.e = this.settings.waterLevelMax;


        this.DungeonGen = new dq();
        this.strongholdGen = new xq();

        abn[] StrongholdsBiomes = { abn.a[DefaultBiome.DESERT.Id], abn.a[DefaultBiome.FOREST.Id], abn.a[DefaultBiome.EXTREME_HILLS.Id], abn.a[DefaultBiome.SWAMPLAND.Id], abn.a[DefaultBiome.TAIGA.Id], abn.a[DefaultBiome.ICE_PLAINS.Id], abn.a[DefaultBiome.ICE_MOUNTAINS.Id], abn.a[DefaultBiome.DESERT_HILLS.Id], abn.a[DefaultBiome.FOREST_HILLS.Id], abn.a[DefaultBiome.SMALL_MOUNTAINS.Id], abn.a[DefaultBiome.JUNGLE.Id], abn.a[DefaultBiome.JUNGLE_HILLS.Id] };
        try
        {
            ModLoader.setPrivateValue(xq.class,this.strongholdGen,"a",StrongholdsBiomes);

        } catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }

        this.VillageGen = new an(0);
        this.MineshaftGen = new alo();


        this.Tree = new sb(false);
        this.BigTree = new zz(false);
        this.Forest = new i(false);
        this.SwampTree = new bf();
        this.TaigaTree1 = new lo();
        this.TaigaTree2 = new rb(false);
        this.HugeMushroom = new qm();
        this.JungleTree = new ajd(false, 15, 3, 3);
        this.GroundBush = new agm(3, 0);

        this.ChunkCache = new ack[4];
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

    public xd getWorld()
    {
        return this.world;
    }

    public void setHeightBits(int heightBits)
    {
        this.heightBits = heightBits;
        this.worldHeight = 1 << heightBits;
    }
}
