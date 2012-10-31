package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.*;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.resourcegens.TreeType;
import net.minecraft.server.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class BukkitWorld implements LocalWorld
{
    private TCChunkGenerator generator;
    private World world;
    private WorldConfig settings;
    private String name;
    private long Seed;
    private IBiomeManager biomeManager;
    private TCWorldChunkManagerOld old_biomeManager;

    // TODO: We must refactor so fields start with lowercase chars.
    // TODO: It is bad practice to use a big char as start of a field name.

    private static int NextBiomeId = DefaultBiome.values().length;
    private static int maxBiomeCount = 256;
    private static LocalBiome[] Biomes = new LocalBiome[maxBiomeCount];

    private HashMap<String, LocalBiome> BiomeNames = new HashMap<String, LocalBiome>();
    private static ArrayList<LocalBiome> DefaultBiomes = new ArrayList<LocalBiome>();

    private WorldGenStronghold strongholdGen;
    private WorldGenVillage VillageGen;
    private WorldGenMineshaft MineshaftGen;
    private WorldGenLargeFeature PyramidsGen;
    private WorldGenNether NetherFortress;

    private WorldGenTrees Tree;
    private WorldGenTrees CocoaTree;
    private WorldGenBigTree BigTree;
    private WorldGenForest Forest;
    private WorldGenSwampTree SwampTree;
    private WorldGenTaiga1 TaigaTree1;
    private WorldGenTaiga2 TaigaTree2;
    private WorldGenHugeMushroom HugeMushroom;
    private WorldGenMegaTree JungleTree;
    private WorldGenGroundBush GroundBush;

    private boolean CreateNewChunks;
    private Chunk[] ChunkCache;
    private Chunk CachedChunk;

    private int CurrentChunkX;
    private int CurrentChunkZ;

    private BiomeBase[] BiomeArray;

    //TODO do something with that when bukkit allow custom world height.
    private int worldHeight = 256;
    private int heightBits = 8;

    private int CustomBiomesCount = 21;

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

    public LocalBiome AddBiome(String name, int id)
    {
        BukkitBiome biome = new BukkitBiome(new CustomBiome(id, name));
        biome.setCustomID(CustomBiomesCount++);
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

        BiomeArray = this.world.worldProvider.d.getBiomes(BiomeArray, x, z, x_size, z_size);
        if (biomeArray == null || biomeArray.length < x_size * z_size)
            biomeArray = new int[x_size * z_size];
        for (int i = 0; i < x_size * z_size; i++)
            biomeArray[i] = BiomeArray[i].id;
        return biomeArray;
    }

    public float[] getTemperatures(int x, int z, int x_size, int z_size)
    {
        if (this.biomeManager != null)
            return this.biomeManager.getTemperaturesTC(x, z, x_size, z_size);
        return this.world.worldProvider.d.getTemperatures(null, x, z, x_size, z_size);
    }

    public int[] getBiomes(int[] biomeArray, int x, int z, int x_size, int z_size)
    {
        if (this.biomeManager != null)
            return this.biomeManager.getBiomesTC(biomeArray, x, z, x_size, z_size);

        BiomeArray = this.world.worldProvider.d.a(BiomeArray, x, z, x_size, z_size, true);
        if (biomeArray == null || biomeArray.length < x_size * z_size)
            biomeArray = new int[x_size * z_size];
        for (int i = 0; i < x_size * z_size; i++)
            biomeArray[i] = BiomeArray[i].id;
        return biomeArray;
    }

    public int getBiome(int x, int z)
    {
        if (this.biomeManager != null)
            return this.biomeManager.getBiomeTC(x, z);
        return this.world.worldProvider.d.getBiome(x, z).id;
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
        if (this.settings.NetherFortress)
            this.NetherFortress.a(null, this.world, x, z, chunkArray);
    }

    public void PlaceDungeons(Random rand, int x, int y, int z)
    {
        new WorldGenDungeons().a(this.world, rand, x, y, z);
    }

    public boolean PlaceTree(TreeType type, Random rand, int x, int y, int z)
    {
        switch (type)
        {
            case Tree:
                return Tree.a(this.world, rand, x, y, z);
            case BigTree:
                BigTree.a(1.0D, 1.0D, 1.0D);
                return BigTree.a(this.world, rand, x, y, z);
            case Forest:
                return Forest.a(this.world, rand, x, y, z);
            case HugeMushroom:
                HugeMushroom.a(1.0D, 1.0D, 1.0D);
                return HugeMushroom.a(this.world, rand, x, y, z);
            case SwampTree:
                return SwampTree.a(this.world, rand, x, y, z);
            case Taiga1:
                return TaigaTree1.a(this.world, rand, x, y, z);
            case Taiga2:
                return TaigaTree2.a(this.world, rand, x, y, z);
            case JungleTree:
                return JungleTree.a(this.world, rand, x, y, z);
            case GroundBush:
                return GroundBush.a(this.world, rand, x, y, z);
            case CocoaTree:
                return CocoaTree.a(this.world, rand, x, y, z);
        }
        return false;
    }


    // There is raw code from ChunkProviderGenerate, check each x,z coordinate in chunk  i5 - some thing like getHighestBlockYAt
    // TODO: I think we may optimise this.
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
                    this.world.setTypeId(_x + i1, i5 - 1, _z + i2, Block.ICE.id);
                }
                if (this.world.w(_x + i1, i5, _z + i2))
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
            this.strongholdGen.a(this.world, rand, chunk_x, chunk_z);
        if (this.settings.MineshaftsEnabled)
            this.MineshaftGen.a(this.world, rand, chunk_x, chunk_z);
        if (this.settings.VillagesEnabled)
            Village = this.VillageGen.a(this.world, rand, chunk_x, chunk_z);
        if (this.settings.PyramidsEnabled)
            this.PyramidsGen.a(this.world, rand, chunk_x, chunk_z);
        if (this.settings.NetherFortress)
            this.NetherFortress.a(this.world, rand, chunk_x, chunk_z);

        return Village;
    }

    // This part work with ReplacedBlocks after all spawns
    // TODO: check how its work.
    public void DoBlockReplace()
    {
        if (this.settings.BiomeConfigsHaveReplacement)
        {
            Chunk rawChunk = this.ChunkCache[0];

            ChunkSection[] sectionsArray = rawChunk.i();

            byte[] ChunkBiomes = rawChunk.m();

            int x = this.CurrentChunkX * 16;
            int z = this.CurrentChunkZ * 16;

            for (ChunkSection section : sectionsArray)
            {
                if (section == null)
                    continue;

                for (int sectionX = 0; sectionX < 16; sectionX++)
                {
                    for (int sectionZ = 0; sectionZ < 16; sectionZ++)
                    {
                        BiomeConfig biomeConfig = this.settings.biomeConfigs[ChunkBiomes[(sectionZ << 4) | sectionX]];
                        if (biomeConfig != null && biomeConfig.ReplaceCount > 0)
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
                                world.notify((x + sectionX), (section.d() + sectionY), (z + sectionZ));

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
        if (y < 0 || y >= worldHeight)
            return null;

        x = x >> 4;
        z = z >> 4;

        if (this.CachedChunk != null && this.CachedChunk.x == x && this.CachedChunk.z == z)
            return this.CachedChunk;

        int index_x = (x - this.CurrentChunkX);
        int index_z = (z - this.CurrentChunkZ);
        if ((index_x == 0 || index_x == 1) && (index_z == 0 || index_z == 1))
            return CachedChunk = this.ChunkCache[index_x | (index_z << 1)];
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
        for (int y = worldHeight - 1; y > 0; y--)
        {
            int id = chunk.getTypeId(x, y, z);
            if (DefaultMaterial.getMaterial(id).isLiquid())
                return y;
        }
        return -1;
    }
    
    public int getSolidHeight(int x, int z)
    {
        Chunk chunk = this.getChunk(x, 0, z);
        if (chunk == null)
            return -1;
        z = z & 0xF;
        x = x & 0xF;
        for (int y = worldHeight - 1; y > 0; y--)
        {
            int id = chunk.getTypeId(x, y, z);
            if (DefaultMaterial.getMaterial(id).isSolid())
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
        Chunk chunk = this.getChunk(x, y, z);
        if (chunk == null)
        {
            return 0;
        }

        z = z & 0xF;
        x = x & 0xF;

        return chunk.getTypeId(x, y, z);
    }

    public void setBlock(final int x, final int y, final int z, final int typeId, final int data, final boolean updateLight, final boolean applyPhysics, final boolean notifyPlayers)
    {
        // If minecraft was updated and obfuscation is off - take a look at these methods:
        // this.world.setRawTypeIdAndData(i, j, k, l, i1)
        // this.world.setTypeIdAndData(i, j, k, l, i1)

        // We fetch the chunk from a custom cache in order to speed things up.
        Chunk chunk = this.getChunk(x, y, z);

        if (chunk == null)
        {
            return;
        }
        if (applyPhysics)
        {
            int oldTypeId = chunk.getTypeId(x & 15, y, z & 15);
            chunk.a(x & 15, y, z & 15, typeId, data);
            this.world.applyPhysics(x, y, z, typeId == 0 ? oldTypeId : typeId);
        } else
            chunk.a(x & 15, y, z & 15, typeId, data); // Set typeId and Data


        if (updateLight)
        {
            this.world.v(x, y, z);
        }

        if (notifyPlayers && chunk.seenByPlayer)
        {
            this.world.notify(x, y, z);
        }
    }

    public void setBlock(final int x, final int y, final int z, final int typeId, final int data)
    {
        this.setBlock(x, y, z, typeId, data, false, false, true);
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
        return worldHeight;
    }

    public int getHeightBits()
    {
        return heightBits;
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

        // TODO: Should WorldProviderTC extend even more? For example for spawn point etc?
        // this.world.worldProvider = new TCWorldProvider().setSeaLevel(this.settings.waterLevelMax); // cause errors with entity burn, disabled temporary.

        this.ChunkCache = new Chunk[4];

        switch (this.settings.ModeTerrain)
        {

            case Normal:
            case OldGenerator:
                this.Tree = new WorldGenTrees(false);
                this.CocoaTree = new WorldGenTrees(false, 5, 3, 3, true);
                this.BigTree = new WorldGenBigTree(false);
                this.Forest = new WorldGenForest(false);
                this.SwampTree = new WorldGenSwampTree();
                this.TaigaTree1 = new WorldGenTaiga1();
                this.TaigaTree2 = new WorldGenTaiga2(false);
                this.HugeMushroom = new WorldGenHugeMushroom();
                this.JungleTree = new WorldGenMegaTree(false, 15, 3, 3);
                this.GroundBush = new WorldGenGroundBush(3, 0);

                this.strongholdGen = new WorldGenStronghold();
                this.VillageGen = new WorldGenVillage();
                this.MineshaftGen = new WorldGenMineshaft();
                this.PyramidsGen = new WorldGenLargeFeature();
                this.NetherFortress = new WorldGenNether();
            case TerrainTest:
            case NotGenerate:
                this.generator.Init(this);
                break;
            case Default:
                break;
        }
    }

    public void setChunkGenerator(TCChunkGenerator _generator)
    {
        this.generator = _generator;
    }

    public void setBiomeManager(IBiomeManager manager)
    {
        this.biomeManager = manager;
    }

    public void setOldBiomeManager(TCWorldChunkManagerOld manager)
    {
        this.old_biomeManager = manager;
        this.biomeManager = manager;
    }

    public void setHeightBits(int heightBits)
    {
        this.heightBits = heightBits;
        this.worldHeight = 1 << heightBits;
    }
}