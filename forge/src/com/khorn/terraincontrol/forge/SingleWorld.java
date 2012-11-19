package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.*;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.resourcegens.TreeType;

import java.util.*;

import net.minecraft.src.BiomeGenBase;
import net.minecraft.src.Chunk;
import net.minecraft.src.ExtendedBlockStorage;
import net.minecraft.src.MapGenMineshaft;
import net.minecraft.src.MapGenNetherBridge;
import net.minecraft.src.MapGenScatteredFeature;
import net.minecraft.src.MapGenStronghold;
import net.minecraft.src.MapGenVillage;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.minecraft.src.WorldGenBigMushroom;
import net.minecraft.src.WorldGenBigTree;
import net.minecraft.src.WorldGenDungeons;
import net.minecraft.src.WorldGenForest;
import net.minecraft.src.WorldGenHugeTrees;
import net.minecraft.src.WorldGenShrub;
import net.minecraft.src.WorldGenSwamp;
import net.minecraft.src.WorldGenTaiga1;
import net.minecraft.src.WorldGenTaiga2;
import net.minecraft.src.WorldGenTrees;

public class SingleWorld implements LocalWorld
{
    private ChunkProvider generator;
    private World world;
    private WorldConfig settings;
    private String name;
    private long Seed;
    private IBiomeManager biomeManager;
    private BiomeManagerOld old_biomeManager;

    private static int NextBiomeId = 0;
    private static int maxBiomeCount = 256;
    private static Biome[] Biomes = new Biome[maxBiomeCount];
    private static BiomeGenBase[] BiomesToRestore = new BiomeGenBase[maxBiomeCount];

    private HashMap<String, LocalBiome> BiomeNames = new HashMap<String, LocalBiome>();

    private static ArrayList<LocalBiome> DefaultBiomes = new ArrayList<LocalBiome>();

    public MapGenStronghold strongholdGen;
    private MapGenVillage VillageGen;
    private MapGenMineshaft MineshaftGen;
    private MapGenScatteredFeature PyramidsGen;
    private MapGenNetherBridge NetherFortress;

    private WorldGenDungeons DungeonGen;

    private WorldGenTrees Tree;
    private WorldGenTrees CocoaTree;
    private WorldGenBigTree BigTree;
    private WorldGenForest Forest;
    private WorldGenSwamp SwampTree;
    private WorldGenTaiga1 TaigaTree1;
    private WorldGenTaiga2 TaigaTree2;
    private WorldGenBigMushroom HugeMushroom;
    private WorldGenHugeTrees JungleTree;
    private WorldGenShrub GroundBush;

    private boolean CreateNewChunks;
    private Chunk[] ChunkCache;
    private Chunk CachedChunk;

    private int CurrentChunkX;
    private int CurrentChunkZ;

    private BiomeGenBase[] BiomeArray;
    private int[] BiomeIntArray;

    private int worldHeight = 128;
    private int heightBits = 7;

    public static void restoreBiomes()
    {
        for (BiomeGenBase oldBiome : BiomesToRestore)
        {
            if (oldBiome == null)
                continue;
            BiomeGenBase.biomeList[oldBiome.biomeID] = oldBiome;
        }
        NextBiomeId = 0;
        DefaultBiomes.clear();
        MapGenVillage.villageSpawnBiomes = Arrays.asList(BiomeGenBase.plains, BiomeGenBase.desert);

    }

    public SingleWorld(String _name)
    {
        this.name = _name;

        for (int i = 0; i < DefaultBiome.values().length; i++)
        {
            BiomeGenBase oldBiome = BiomeGenBase.biomeList[i];
            BiomesToRestore[i] = oldBiome;
            BiomeGenCustom custom = new BiomeGenCustom(NextBiomeId++, oldBiome.biomeName);
            custom.CopyBiome(oldBiome);
            Biome biome = new Biome(custom);
            Biomes[biome.getId()] = biome;
            DefaultBiomes.add(biome);
            this.BiomeNames.put(biome.getName(), biome);
        }
        MapGenVillage.villageSpawnBiomes = Arrays.asList(BiomeGenBase.biomeList[DefaultBiome.PLAINS.Id], BiomeGenBase.biomeList[DefaultBiome.DESERT.Id]);

    }

    @Override
    public LocalBiome getNullBiome(String name)
    {
        return null;
    }

    @Override
    public LocalBiome AddBiome(String name, int id)
    {
        Biome biome = new Biome(new BiomeGenCustom(id, name));
        Biomes[biome.getId()] = biome;
        this.BiomeNames.put(biome.getName(), biome);
        return biome;
    }

    @Override
    public int getMaxBiomesCount()
    {
        return maxBiomeCount;
    }

    @Override
    public int getFreeBiomeId()
    {
        return NextBiomeId++;
    }

    @Override
    public LocalBiome getBiomeById(int id)
    {
        return Biomes[id];
    }

    @Override
    public int getBiomeIdByName(String name)
    {
        return this.BiomeNames.get(name).getId();
    }

    @Override
    public ArrayList<LocalBiome> getDefaultBiomes()
    {
        return DefaultBiomes;
    }

    @Override
    public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int x_size, int z_size)
    {
        if (this.biomeManager != null)
            return this.biomeManager.getBiomesUnZoomedTC(biomeArray, x, z, x_size, z_size);

        BiomeArray = this.world.provider.worldChunkMgr.getBiomesForGeneration(BiomeArray, x, z, x_size, z_size);
        if (biomeArray == null || biomeArray.length < x_size * z_size)
            biomeArray = new int[x_size * z_size];
        for (int i = 0; i < x_size * z_size; i++)
            biomeArray[i] = BiomeArray[i].biomeID;
        return biomeArray;
    }

    @Override
    public float[] getTemperatures(int x, int z, int x_size, int z_size)
    {
        if (this.biomeManager != null)
            return this.biomeManager.getTemperaturesTC(x, z, x_size, z_size);
        return this.world.provider.worldChunkMgr.getTemperatures(new float[0], x, z, x_size, z_size);
    }

    @Override
    public int[] getBiomes(int[] biomeArray, int x, int z, int x_size, int z_size)
    {
        if (this.biomeManager != null)
            return this.biomeManager.getBiomesTC(biomeArray, x, z, x_size, z_size);

        BiomeArray = this.world.provider.worldChunkMgr.getBiomeGenAt(BiomeArray, x, z, x_size, z_size, true);
        if (biomeArray == null || biomeArray.length < x_size * z_size)
            biomeArray = new int[x_size * z_size];
        for (int i = 0; i < x_size * z_size; i++)
            biomeArray[i] = BiomeArray[i].biomeID;
        return biomeArray;
    }

    @Override
    public int getBiome(int x, int z)
    {
        if (this.biomeManager != null)
            return this.biomeManager.getBiomeTC(x, z);
        return this.world.provider.worldChunkMgr.getBiomeGenAt(x, z).biomeID;
    }

    @Override
    public double getBiomeFactorForOldBM(int index)
    {
        return this.old_biomeManager.oldTemperature[index] * this.old_biomeManager.oldWetness[index];
    }

    @Override
    public void PrepareTerrainObjects(int x, int z, byte[] chunkArray, boolean dry)
    {
        if (this.settings.StrongholdsEnabled)
            this.strongholdGen.generate(null, this.world, x, z, chunkArray);

        if (this.settings.MineshaftsEnabled)
            this.MineshaftGen.generate(null, this.world, x, z, chunkArray);
        if (this.settings.VillagesEnabled && dry)
            this.VillageGen.generate(null, this.world, x, z, chunkArray);
        if (this.settings.PyramidsEnabled)
            this.PyramidsGen.generate(null, this.world, x, z, chunkArray);
        if (this.settings.NetherFortress)
            this.NetherFortress.generate(null, this.world, x, z, chunkArray);

    }

    @Override
    public void PlaceDungeons(Random rand, int x, int y, int z)
    {
        DungeonGen.generate(this.world, rand, x, y, z);
    }

    @Override
    public boolean PlaceTree(TreeType type, Random rand, int x, int y, int z)
    {
        switch (type)
        {
        case Tree:
            return Tree.generate(this.world, rand, x, y, z);
        case BigTree:
            BigTree.setScale(1.0D, 1.0D, 1.0D);
            return BigTree.generate(this.world, rand, x, y, z);
        case Forest:
            return Forest.generate(this.world, rand, x, y, z);
        case HugeMushroom:
            HugeMushroom.setScale(1.0D, 1.0D, 1.0D);
            return HugeMushroom.generate(this.world, rand, x, y, z);
        case SwampTree:
            return SwampTree.generate(this.world, rand, x, y, z);
        case Taiga1:
            return TaigaTree1.generate(this.world, rand, x, y, z);
        case Taiga2:
            return TaigaTree2.generate(this.world, rand, x, y, z);
        case JungleTree:
            return JungleTree.generate(this.world, rand, x, y, z);
        case GroundBush:
            return GroundBush.generate(this.world, rand, x, y, z);
        case CocoaTree:
            return CocoaTree.generate(this.world, rand, x, y, z);
        default:
            break;
        }
        return false;
    }

    @Override
    public void PlaceIce(int x, int z)
    {
        int i1 = x + 8;
        int i2 = z + 8;
        for (int _x = 0; _x < 16; _x++)
        {
            for (int _z = 0; _z < 16; _z++)
            {
                int i5 = this.world.getFirstUncoveredBlock(i1 + _x, i2 + _z);

                if (this.world.isBlockFreezable(_x + i1, i5 - 1, _z + i2))
                {
                    this.world.setBlock(_x + i1, i5 - 1, _z + i2, DefaultMaterial.ICE.id);
                }
                if (this.world.canSnowAt(_x + i1, i5, _z + i2))
                {
                    this.world.setBlock(_x + i1, i5, _z + i2, DefaultMaterial.SNOW.id);
                }
            }

        }
    }

    @Override
    public boolean PlaceTerrainObjects(Random rand, int chunk_x, int chunk_z)
    {
        boolean isVillagePlaced = false;
        if (this.settings.StrongholdsEnabled)
            this.strongholdGen.generateStructuresInChunk(this.world, rand, chunk_x, chunk_z);
        if (this.settings.MineshaftsEnabled)
            this.MineshaftGen.generateStructuresInChunk(this.world, rand, chunk_x, chunk_z);
        if (this.settings.VillagesEnabled)
            isVillagePlaced = this.VillageGen.generateStructuresInChunk(this.world, rand, chunk_x, chunk_z);
        if (this.settings.PyramidsEnabled)
            this.PyramidsGen.generateStructuresInChunk(this.world, rand, chunk_x, chunk_z);
        if (this.settings.NetherFortress)
            this.NetherFortress.generateStructuresInChunk(this.world, rand, chunk_x, chunk_z);

        return isVillagePlaced;
    }

    @Override
    public void DoBlockReplace()
    {
        if (this.settings.BiomeConfigsHaveReplacement)
        {

            Chunk rawChunk = this.ChunkCache[0];

            ExtendedBlockStorage[] sectionsArray = rawChunk.getBlockStorageArray();

            byte[] ChunkBiomes = rawChunk.getBiomeArray();

            int x = this.CurrentChunkX * 16;
            int z = this.CurrentChunkZ * 16;

            for (ExtendedBlockStorage section : sectionsArray)
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
                                int blockId = section.getExtBlockID(sectionX, sectionY, sectionZ);
                                if (biomeConfig.ReplaceMatrixBlocks[blockId] == null)
                                    continue;

                                int replaceTo = biomeConfig.ReplaceMatrixBlocks[blockId][section.getYLocation() + sectionY];
                                if (replaceTo == -1)
                                    continue;

                                section.setExtBlockID(sectionX, sectionY, sectionZ, replaceTo >> 4);
                                section.setExtBlockMetadata(sectionX, sectionY, sectionZ, replaceTo & 0xF);
                                world.getFullBlockLightValue((x + sectionX), (section.getYLocation() + sectionY), (z + sectionZ));

                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void DoBiomeReplace()
    {
        if (this.settings.HaveBiomeReplace)
        {
            byte[] ChunkBiomes = this.ChunkCache[0].getBiomeArray();

            for (int i = 0; i < ChunkBiomes.length; i++)
                ChunkBiomes[i] = this.settings.ReplaceMatrixBiomes[ChunkBiomes[i]];
        }

    }

    public void LoadChunk(int x, int z)
    {
        this.CurrentChunkX = x;
        this.CurrentChunkZ = z;
        this.ChunkCache[0] = this.world.getChunkFromChunkCoords(x, z);
        this.ChunkCache[1] = this.world.getChunkFromChunkCoords(x + 1, z);
        this.ChunkCache[2] = this.world.getChunkFromChunkCoords(x, z + 1);
        this.ChunkCache[3] = this.world.getChunkFromChunkCoords(x + 1, z + 1);
        this.CreateNewChunks = true;
    }

    private Chunk getChunk(int x, int y, int z)
    {
        if (y < 0 || y >= this.worldHeight)
            return null;

        x = x >> 4;
        z = z >> 4;
        if (this.CachedChunk != null && this.CachedChunk.xPosition == x && this.CachedChunk.zPosition == z)
            return this.CachedChunk;

        int index_x = (x - this.CurrentChunkX);
        int index_z = (z - this.CurrentChunkZ);
        if ((index_x == 0 || index_x == 1) && (index_z == 0 || index_z == 1))
            return CachedChunk = this.ChunkCache[index_x | (index_z << 1)];
        else if (this.CreateNewChunks || this.world.getChunkProvider().chunkExists(x, z))
            return CachedChunk = this.world.getChunkFromBlockCoords(x, z);
        else
            return null;

    }

    @Override
    public int getLiquidHeight(int x, int z)
    {
        Chunk chunk = this.getChunk(x, 0, z);
        if (chunk == null)
            return -1;
        z = z & 0xF;
        x = x & 0xF;
        for (int y = worldHeight - 1; y > 0; y--)
        {
            int id = chunk.getBlockID(x, y, z);
            if (DefaultMaterial.getMaterial(id).isLiquid())
                return y;
        }
        return -1;
    }

    @Override
    public int getSolidHeight(int x, int z)
    {
        Chunk chunk = this.getChunk(x, 0, z);
        if (chunk == null)
            return -1;
        z = z & 0xF;
        x = x & 0xF;
        for (int y = worldHeight - 1; y > 0; y--)
        {
            int id = chunk.getBlockID(x, y, z);
            if (DefaultMaterial.getMaterial(id).isSolid())
                return y;
        }
        return -1;
    }

    @Override
    public boolean isEmpty(int x, int y, int z)
    {
        return this.getTypeId(x, y, z) == 0;
    }

    @Override
    public int getTypeId(int x, int y, int z)
    {
        Chunk chunk = this.getChunk(x, y, z);
        if (chunk == null)
            return 0;

        z = z & 0xF;
        x = x & 0xF;

        return chunk.getBlockID(x, y, z);
    }

    @Override
    public void setBlock(final int x, final int y, final int z, final int typeId, final int data, final boolean updateLight, final boolean applyPhysics, final boolean notifyPlayers)
    {
        // If minecraft was updated and obfuscation is off - take a look at
        // these methods:
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
            int oldTypeId = chunk.getBlockID(x & 15, y, z & 15);
            chunk.setBlockIDWithMetadata(x & 15, y, z & 15, typeId, data);
            this.world.notifyBlocksOfNeighborChange(x, y, z, typeId == 0 ? oldTypeId : typeId);
        } else
            chunk.setBlockIDWithMetadata(x & 15, y, z & 15, typeId, data); // Set
                                                                           // typeId
                                                                           // and
                                                                           // Data

        if (updateLight)
        {
            this.world.updateAllLightTypes(x, y, z);
        }

        if (notifyPlayers)
        {
            // this.world.notifyPlayers(x, y, z) // TODO find method to notify
            // players
        }
    }

    @Override
    public void setBlock(final int x, final int y, final int z, final int typeId, final int data)
    {
        this.setBlock(x, y, z, typeId, data, false, false, false);
    }

    @Override
    public int getHighestBlockYAt(int x, int z)
    {
        Chunk chunk = this.getChunk(x, 0, z);
        if (chunk == null)
            return -1;
        z = z & 0xF;
        x = x & 0xF;
        return chunk.getHeightValue(x, z);
    }

    @Override
    public DefaultMaterial getMaterial(int x, int y, int z)
    {
        int id = this.getTypeId(x, y, z);
        return DefaultMaterial.getMaterial(id);
    }

    @Override
    public void setChunksCreations(boolean createNew)
    {
        this.CreateNewChunks = createNew;
    }

    @Override
    public int getLightLevel(int x, int y, int z)
    {
        return world.getBlockLightValue(x, y, z);
    }

    @Override
    public boolean isLoaded(int x, int y, int z)
    {
        if (y < 0 || y >= this.worldHeight)
            return false;
        x = x >> 4;
        z = z >> 4;

        return world.getChunkProvider().chunkExists(x, z);
    }

    @Override
    public WorldConfig getSettings()
    {
        return this.settings;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public long getSeed()
    {
        return this.Seed;
    }

    @Override
    public int getHeight()
    {
        return this.worldHeight;
    }

    @Override
    public int getHeightBits()
    {
        return heightBits;
    }

    public ChunkProvider getChunkGenerator()
    {
        return this.generator;
    }

    public void InitM(World world, WorldConfig config)
    {
        this.settings = config;
        this.world = world;
        this.Seed = world.getSeed();
        for (Biome biome : this.Biomes)
        {
            // Apply settings for biomes
            if (biome != null && config.biomeConfigs[biome.getId()] != null)
            {
                biome.setVisuals(config.biomeConfigs[biome.getId()]);
            }
        }
    }

    public void Init(World world, WorldConfig config)
    {
        this.settings = config;

        this.world = world;
        this.Seed = world.getSeed();
        // this.world.e = this.settings.waterLevelMax;

        this.DungeonGen = new WorldGenDungeons();
        this.strongholdGen = new MapGenStronghold();

        this.VillageGen = new MapGenVillage();
        this.MineshaftGen = new MapGenMineshaft();
        this.PyramidsGen = new MapGenScatteredFeature();
        this.NetherFortress = new MapGenNetherBridge();

        this.Tree = new WorldGenTrees(false);
        this.CocoaTree = new WorldGenTrees(false, 5, 3, 3, true);
        this.BigTree = new WorldGenBigTree(false);
        this.Forest = new WorldGenForest(false);
        this.SwampTree = new WorldGenSwamp();
        this.TaigaTree1 = new WorldGenTaiga1();
        this.TaigaTree2 = new WorldGenTaiga2(false);
        this.HugeMushroom = new WorldGenBigMushroom();
        this.JungleTree = new WorldGenHugeTrees(false, 15, 3, 3);
        this.GroundBush = new WorldGenShrub(3, 0);

        this.ChunkCache = new Chunk[4];
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

    public World getWorld()
    {
        return this.world;
    }

    @Override
    public void setHeightBits(int heightBits)
    {
        this.heightBits = heightBits;
        this.worldHeight = 1 << heightBits;
    }

    public void FillChunkForBiomes(Chunk chunk, int x, int z)
    {

        byte[] arrayOfByte2 = chunk.getBiomeArray();
        BiomeIntArray = this.getBiomes(BiomeIntArray, x * 16, z * 16, 16, 16);

        for (int i1 = 0; i1 < arrayOfByte2.length; i1++)
        {
            arrayOfByte2[i1] = (byte) BiomeIntArray[i1];
        }
    }
}
