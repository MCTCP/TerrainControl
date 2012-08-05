import com.khorn.terraincontrol.*;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.resourcegens.TreeType;

import java.util.*;

public class SingleWorld implements LocalWorld
{
    private ChunkProvider generator;
    private uo world;
    private WorldConfig settings;
    private String name;
    private long Seed;
    private IBiomeManager biomeManager;
    private BiomeManagerOld old_biomeManager;

    private static int NextBiomeId = 0;
    private static int maxBiomeCount = 256;
    private static LocalBiome[] Biomes = new LocalBiome[maxBiomeCount];
    private static vj[] BiomesToRestore = new vj[maxBiomeCount];

    private HashMap<String, LocalBiome> BiomeNames = new HashMap<String, LocalBiome>();

    private static ArrayList<LocalBiome> DefaultBiomes = new ArrayList<LocalBiome>();


    public aag strongholdGen;
    private abh VillageGen;
    private yx MineshaftGen;
    private zy PyramidsGen;


    private yi DungeonGen;

    private yt Tree;
    private xp BigTree;
    private xq Forest;
    private yr SwampTree;
    private yk TaigaTree1;
    private yq TaigaTree2;
    private ye HugeMushroom;
    private yh JungleTree;
    private xz GroundBush;


    private boolean CreateNewChunks;
    private wk[] ChunkCache;
    private wk CachedChunk;

    private int CurrentChunkX;
    private int CurrentChunkZ;

    private vj[] BiomeArray;
    private int[] BiomeIntArray;


    private int worldHeight = 128;
    private int heightBits = 7;


    public static void RestoreBiomes()
    {
        for (vj oldBiome : BiomesToRestore)
        {
            if (oldBiome == null)
                continue;
            vj.a[oldBiome.M] = oldBiome;
        }
        NextBiomeId = 0;
        DefaultBiomes.clear();
        abh.e = Arrays.asList(vj.c, vj.d);

        List biomes = Arrays.asList(vj.d, vj.s, vj.w);
        try
        {
            ModLoader.setPrivateValue(zy.class, null, "e", biomes);

        } catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }

    }


    public SingleWorld(String _name)
    {
        this.name = _name;

        for (int i = 0; i < DefaultBiome.values().length; i++)
        {
            vj oldBiome = vj.a[i];
            BiomesToRestore[i] = oldBiome;
            CustomBiome custom = new CustomBiome(NextBiomeId++, oldBiome.y);
            custom.CopyBiome(oldBiome);
            Biome biome = new Biome(custom);
            Biomes[biome.getId()] = biome;
            DefaultBiomes.add(biome);
            this.BiomeNames.put(biome.getName(), biome);
        }
        abh.e = Arrays.asList(vj.a[DefaultBiome.PLAINS.Id], vj.a[DefaultBiome.DESERT.Id]);

        List PyramidsBiomes = Arrays.asList(vj.a[DefaultBiome.DESERT.Id], vj.a[DefaultBiome.DESERT_HILLS.Id], vj.a[DefaultBiome.JUNGLE.Id]);
        try
        {
            ModLoader.setPrivateValue(zy.class, null, "e", PyramidsBiomes);

        } catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }

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
        if (this.biomeManager != null)
            return this.biomeManager.getBiomesUnZoomedTC(biomeArray, x, z, x_size, z_size);

        BiomeArray = this.world.w.c.a(BiomeArray, x, z, x_size, z_size);
        if (biomeArray == null || biomeArray.length < x_size * z_size)
            biomeArray = new int[x_size * z_size];
        for (int i = 0; i < x_size * z_size; i++)
            biomeArray[i] = BiomeArray[i].M;
        return biomeArray;
    }

    public float[] getTemperatures(int x, int z, int x_size, int z_size)
    {
        if (this.biomeManager != null)
            return this.biomeManager.getTemperaturesTC(x, z, x_size, z_size);
        return this.world.w.c.b(new float[0], x, z, x_size, z_size);
    }

    public int[] getBiomes(int[] biomeArray, int x, int z, int x_size, int z_size)
    {
        if (this.biomeManager != null)
            return this.biomeManager.getBiomesTC(biomeArray, x, z, x_size, z_size);

        BiomeArray = this.world.w.c.a(BiomeArray, x, z, x_size, z_size, true);
        if (biomeArray == null || biomeArray.length < x_size * z_size)
            biomeArray = new int[x_size * z_size];
        for (int i = 0; i < x_size * z_size; i++)
            biomeArray[i] = BiomeArray[i].M;
        return biomeArray;
    }

    public int getBiome(int x, int z)
    {
        if (this.biomeManager != null)
            return this.biomeManager.getBiomeTC(x, z);
        return this.world.w.c.a(x, z).M;
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
        if (this.settings.PyramidsEnabled)
            this.PyramidsGen.a(null, this.world, x, z, chunkArray);


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


    public void PlaceIce(int x, int z)
    {
        int i1 = x + 8;
        int i2 = z + 8;
        for (int _x = 0; _x < 16; _x++)
        {
            for (int _z = 0; _z < 16; _z++)
            {
                int i5 = this.world.g(i1 + _x, i2 + _z);

                if (this.world.u(_x + i1, i5 - 1, _z + i2))
                {
                    this.world.e(_x + i1, i5 - 1, _z + i2, DefaultMaterial.ICE.id);
                }
                if (this.world.w(_x + i1, i5, _z + i2))
                {
                    this.world.e(_x + i1, i5, _z + i2, DefaultMaterial.SNOW.id);
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
        if (this.settings.PyramidsEnabled)
            this.PyramidsGen.a(this.world, rand, chunk_x, chunk_z);

        return Village;
    }

    public void DoBlockReplace()
    {
        if (this.settings.BiomeConfigsHaveReplacement)
        {

            wk rawChunk = this.ChunkCache[0];

            wl[] sectionsArray = rawChunk.i();

            byte[] ChunkBiomes = rawChunk.m();

            int x = this.CurrentChunkX * 16;
            int z = this.CurrentChunkZ * 16;

            for (wl section : sectionsArray)
            {
                if (section == null)
                    continue;

                for (int sectionX = 0; sectionX < 16; sectionX++)
                {
                    for (int sectionZ = 0; sectionZ < 16; sectionZ++)
                    {
                        BiomeConfig biomeConfig = this.settings.biomeConfigs[ChunkBiomes[(sectionZ << 4) | sectionX]];

                        if (biomeConfig.ReplaceCount > 0)
                        {
                            for (int sectionY = 0; sectionY < 16; sectionY++)
                            {
                                int blockId = section.a(sectionX, sectionY, sectionZ);
                                if (biomeConfig.ReplaceMatrixBlocks[blockId] == null)
                                    continue;

                                int replaceTo = biomeConfig.ReplaceMatrixBlocks[blockId][section.d() + sectionY];
                                if (replaceTo == -1)
                                    continue;

                                section.a(sectionX, sectionY, sectionZ, replaceTo >> 4);
                                section.b(sectionX, sectionY, sectionZ, replaceTo & 0xF);
                                world.k((x + sectionX), (section.d() + sectionY), (z + sectionZ));

                            }
                        }
                    }
                }
            }
        }
    }

    public void DoBiomeReplace()
    {
        if (this.settings.HaveBiomeReplace)
        {
            byte[] ChunkBiomes = this.ChunkCache[0].m();

            for (int i = 0; i < ChunkBiomes.length; i++)
                ChunkBiomes[i] = this.settings.ReplaceMatrixBiomes[ChunkBiomes[i]];
        }

    }

    public void LoadChunk(int x, int z)
    {
        this.CurrentChunkX = x;
        this.CurrentChunkZ = z;
        this.ChunkCache[0] = this.world.e(x, z);
        this.ChunkCache[1] = this.world.e(x + 1, z);
        this.ChunkCache[2] = this.world.e(x, z + 1);
        this.ChunkCache[3] = this.world.e(x + 1, z + 1);
        this.CreateNewChunks = true;
    }

    private wk getChunk(int x, int y, int z)
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
        else if (this.CreateNewChunks || this.world.F().a(x, z))
            return CachedChunk = this.world.d(x, z);
        else
            return null;


    }

    public int getLiquidHeight(int x, int z)
    {
        wk chunk = this.getChunk(x, 0, z);
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
        wk chunk = this.getChunk(x, y, z);
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
        wk chunk = this.getChunk(x, y, z);
        if (chunk == null)
        {
            return;
        }

        if (applyPhysics)
        {
            int oldTypeId = chunk.a(x & 15, y, z & 15);
            chunk.a(x & 15, y, z & 15, typeId, data);
            this.world.h(x, y, z, typeId == 0 ? oldTypeId : typeId);
        } else
            chunk.a(x & 15, y, z & 15, typeId, data); // Set typeId and Data


        if (updateLight)
        {
            this.world.x(x, y, z);
        }

        if (notifyPlayers)
        {
            this.world.h(x, y, z);
        }
    }

    public void setBlock(final int x, final int y, final int z, final int typeId, final int data)
    {
        this.setBlock(x, y, z, typeId, data, false, false, false);
    }

    public int getHighestBlockYAt(int x, int z)
    {
        wk chunk = this.getChunk(x, 0, z);
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
        return world.l(x, y, z);
    }

    public boolean isLoaded(int x, int y, int z)
    {
        if (y < 0 || y >= this.worldHeight)
            return false;
        x = x >> 4;
        z = z >> 4;

        return world.F().a(x, z);
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

    public void InitM(uo _world)
    {
        this.world = _world;
        this.Seed = world.C();

    }

    public void Init(uo _world)
    {
        this.world = _world;
        this.Seed = world.C();
        //this.world.e = this.settings.waterLevelMax;


        this.DungeonGen = new yi();
        this.strongholdGen = new aag();

        vj[] StrongholdsBiomes = {vj.a[DefaultBiome.DESERT.Id], vj.a[DefaultBiome.FOREST.Id], vj.a[DefaultBiome.EXTREME_HILLS.Id], vj.a[DefaultBiome.SWAMPLAND.Id], vj.a[DefaultBiome.TAIGA.Id], vj.a[DefaultBiome.ICE_PLAINS.Id], vj.a[DefaultBiome.ICE_MOUNTAINS.Id], vj.a[DefaultBiome.DESERT_HILLS.Id], vj.a[DefaultBiome.FOREST_HILLS.Id], vj.a[DefaultBiome.SMALL_MOUNTAINS.Id], vj.a[DefaultBiome.JUNGLE.Id], vj.a[DefaultBiome.JUNGLE_HILLS.Id]};

        try
        {
            ModLoader.setPrivateValue(aag.class, this.strongholdGen, "e", StrongholdsBiomes);

        } catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }

        this.VillageGen = new abh(0);
        this.MineshaftGen = new yx();
        this.PyramidsGen = new zy();


        this.Tree = new yt(false);
        this.BigTree = new xp(false);
        this.Forest = new xq(false);
        this.SwampTree = new yr();
        this.TaigaTree1 = new yk();
        this.TaigaTree2 = new yq(false);
        this.HugeMushroom = new ye();
        this.JungleTree = new yh(false, 15, 3, 3);
        this.GroundBush = new xz(3, 0);

        this.ChunkCache = new wk[4];
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

    public uo getWorld()
    {
        return this.world;
    }

    public void setHeightBits(int heightBits)
    {
        this.heightBits = heightBits;
        this.worldHeight = 1 << heightBits;
    }

    public void FillChunkForBiomes(wk chunk, int x, int z)
    {

        byte[] arrayOfByte2 = chunk.m();
        BiomeIntArray = this.getBiomes(BiomeIntArray, x *16 , z *16 , 16, 16);

        for (int i1 = 0; i1 < arrayOfByte2.length; i1++)
        {
            arrayOfByte2[i1] = (byte) BiomeIntArray[i1];
        }
    }
}
