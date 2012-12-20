package com.khorn.terraincontrol.forge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.feature.MapGenScatteredFeature;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenForest;
import net.minecraft.world.gen.feature.WorldGenHugeTrees;
import net.minecraft.world.gen.feature.WorldGenShrub;
import net.minecraft.world.gen.feature.WorldGenSwamp;
import net.minecraft.world.gen.feature.WorldGenTaiga1;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenTrees;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenNetherBridge;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.IBiomeManager;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.Tag;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.forge.util.NBTHelper;
import com.khorn.terraincontrol.generator.resourcegens.TreeType;

public class SingleWorld implements LocalWorld
{
    private ChunkProvider generator;
    private World world;
    private WorldConfig settings;
    private String name;
    private long seed;
    private IBiomeManager biomeManager;
    private BiomeManagerOld oldBiomeManager;

    private static int nextBiomeId = 0;
    private static int maxBiomeCount = 256;
    private static Biome[] biomes = new Biome[maxBiomeCount];
    private static BiomeGenBase[] biomesToRestore = new BiomeGenBase[maxBiomeCount];

    private HashMap<String, LocalBiome> biomeNames = new HashMap<String, LocalBiome>();

    private static ArrayList<LocalBiome> defaultBiomes = new ArrayList<LocalBiome>();

    public MapGenStronghold strongholdGen;
    private MapGenVillage villageGen;
    private MapGenMineshaft mineshaftGen;
    private MapGenScatteredFeature pyramidsGen;
    private MapGenNetherBridge netherFortress;

    private WorldGenDungeons dungeonGen;

    private WorldGenTrees tree;
    private WorldGenTrees cocoaTree;
    private WorldGenBigTree bigTree;
    private WorldGenForest forest;
    private WorldGenSwamp swampTree;
    private WorldGenTaiga1 taigaTree1;
    private WorldGenTaiga2 taigaTree2;
    private WorldGenBigMushroom hugeMushroom;
    private WorldGenHugeTrees jungleTree;
    private WorldGenShrub groundBush;

    private boolean createNewChunks;
    private Chunk[] chunkCache;
    private Chunk cachedChunk;

    private int currentChunkX;
    private int currentChunkZ;

    private BiomeGenBase[] biomeGenBaseArray;
    private int[] biomeIntArray;

    private int worldHeight = 128;
    private int heightBits = 7;

    public static void restoreBiomes()
    {
        for (BiomeGenBase oldBiome : biomesToRestore)
        {
            if (oldBiome == null)
                continue;
            BiomeGenBase.biomeList[oldBiome.biomeID] = oldBiome;
        }
        nextBiomeId = 0;
        defaultBiomes.clear();
        MapGenVillage.villageSpawnBiomes = Arrays.asList(BiomeGenBase.plains, BiomeGenBase.desert);

    }

    public SingleWorld(String _name)
    {
        this.name = _name;

        for (int i = 0; i < DefaultBiome.values().length; i++)
        {
            BiomeGenBase oldBiome = BiomeGenBase.biomeList[i];
            biomesToRestore[i] = oldBiome;
            BiomeGenCustom custom = new BiomeGenCustom(nextBiomeId++, oldBiome.biomeName);
            custom.CopyBiome(oldBiome);
            Biome biome = new Biome(custom);
            biomes[biome.getId()] = biome;
            defaultBiomes.add(biome);
            this.biomeNames.put(biome.getName(), biome);
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
        biomes[biome.getId()] = biome;
        this.biomeNames.put(biome.getName(), biome);
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
        return nextBiomeId++;
    }

    @Override
    public LocalBiome getBiomeById(int id)
    {
        return biomes[id];
    }

    @Override
    public int getBiomeIdByName(String name)
    {
        return this.biomeNames.get(name).getId();
    }

    @Override
    public ArrayList<LocalBiome> getDefaultBiomes()
    {
        return defaultBiomes;
    }

    @Override
    public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int x_size, int z_size)
    {
        if (this.biomeManager != null)
            return this.biomeManager.getBiomesUnZoomedTC(biomeArray, x, z, x_size, z_size);

        biomeGenBaseArray = this.world.provider.worldChunkMgr.getBiomesForGeneration(biomeGenBaseArray, x, z, x_size, z_size);
        if (biomeArray == null || biomeArray.length < x_size * z_size)
            biomeArray = new int[x_size * z_size];
        for (int i = 0; i < x_size * z_size; i++)
            biomeArray[i] = biomeGenBaseArray[i].biomeID;
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

        biomeGenBaseArray = this.world.provider.worldChunkMgr.getBiomeGenAt(biomeGenBaseArray, x, z, x_size, z_size, true);
        if (biomeArray == null || biomeArray.length < x_size * z_size)
            biomeArray = new int[x_size * z_size];
        for (int i = 0; i < x_size * z_size; i++)
            biomeArray[i] = biomeGenBaseArray[i].biomeID;
        return biomeArray;
    }

    @Override
    public int getCalculatedBiomeId(int x, int z)
    {
        if (this.biomeManager != null)
            return this.biomeManager.getBiomeTC(x, z);
        return this.world.provider.worldChunkMgr.getBiomeGenAt(x, z).biomeID;
    }

    @Override
    public double getBiomeFactorForOldBM(int index)
    {
        return this.oldBiomeManager.oldTemperature[index] * this.oldBiomeManager.oldWetness[index];
    }

    @Override
    public void PrepareTerrainObjects(int x, int z, byte[] chunkArray, boolean dry)
    {
        if (this.settings.StrongholdsEnabled)
            this.strongholdGen.generate(null, this.world, x, z, chunkArray);

        if (this.settings.MineshaftsEnabled)
            this.mineshaftGen.generate(null, this.world, x, z, chunkArray);
        if (this.settings.VillagesEnabled && dry)
            this.villageGen.generate(null, this.world, x, z, chunkArray);
        if (this.settings.PyramidsEnabled)
            this.pyramidsGen.generate(null, this.world, x, z, chunkArray);
        if (this.settings.NetherFortress)
            this.netherFortress.generate(null, this.world, x, z, chunkArray);

    }

    @Override
    public void PlaceDungeons(Random rand, int x, int y, int z)
    {
        dungeonGen.generate(this.world, rand, x, y, z);
    }

    @Override
    public boolean PlaceTree(TreeType type, Random rand, int x, int y, int z)
    {
        switch (type)
        {
        case Tree:
            return tree.generate(this.world, rand, x, y, z);
        case BigTree:
            bigTree.setScale(1.0D, 1.0D, 1.0D);
            return bigTree.generate(this.world, rand, x, y, z);
        case Forest:
            return forest.generate(this.world, rand, x, y, z);
        case HugeMushroom:
            hugeMushroom.setScale(1.0D, 1.0D, 1.0D);
            return hugeMushroom.generate(this.world, rand, x, y, z);
        case SwampTree:
            return swampTree.generate(this.world, rand, x, y, z);
        case Taiga1:
            return taigaTree1.generate(this.world, rand, x, y, z);
        case Taiga2:
            return taigaTree2.generate(this.world, rand, x, y, z);
        case JungleTree:
            return jungleTree.generate(this.world, rand, x, y, z);
        case GroundBush:
            return groundBush.generate(this.world, rand, x, y, z);
        case CocoaTree:
            return cocoaTree.generate(this.world, rand, x, y, z);
        default:
            break;
        }
        return false;
    }

    @Override
    public boolean PlaceTerrainObjects(Random rand, int chunk_x, int chunk_z)
    {
        boolean isVillagePlaced = false;
        if (this.settings.StrongholdsEnabled)
            this.strongholdGen.generateStructuresInChunk(this.world, rand, chunk_x, chunk_z);
        if (this.settings.MineshaftsEnabled)
            this.mineshaftGen.generateStructuresInChunk(this.world, rand, chunk_x, chunk_z);
        if (this.settings.VillagesEnabled)
            isVillagePlaced = this.villageGen.generateStructuresInChunk(this.world, rand, chunk_x, chunk_z);
        if (this.settings.PyramidsEnabled)
            this.pyramidsGen.generateStructuresInChunk(this.world, rand, chunk_x, chunk_z);
        if (this.settings.NetherFortress)
            this.netherFortress.generateStructuresInChunk(this.world, rand, chunk_x, chunk_z);

        return isVillagePlaced;
    }

    @Override
    public void replaceBlocks()
    {
        if (this.settings.BiomeConfigsHaveReplacement)
        {

            Chunk rawChunk = this.chunkCache[0];

            ExtendedBlockStorage[] sectionsArray = rawChunk.getBlockStorageArray();

            byte[] ChunkBiomes = rawChunk.getBiomeArray();

            int x = this.currentChunkX * 16;
            int z = this.currentChunkZ * 16;

            for (ExtendedBlockStorage section : sectionsArray)
            {
                if (section == null)
                    continue;

                for (int sectionX = 0; sectionX < 16; sectionX++)
                {
                    for (int sectionZ = 0; sectionZ < 16; sectionZ++)
                    {
                        BiomeConfig biomeConfig = this.settings.biomeConfigs[ChunkBiomes[(sectionZ << 4) | sectionX] & 0xFF ];

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
    public void replaceBiomesLate()
    {
        if (this.settings.HaveBiomeReplace)
        {
            byte[] ChunkBiomes = this.chunkCache[0].getBiomeArray();

            for (int i = 0; i < ChunkBiomes.length; i++)
                ChunkBiomes[i] = this.settings.ReplaceMatrixBiomes[ChunkBiomes[i]];
        }

    }

    public void LoadChunk(int x, int z)
    {
        this.currentChunkX = x;
        this.currentChunkZ = z;
        this.chunkCache[0] = this.world.getChunkFromChunkCoords(x, z);
        this.chunkCache[1] = this.world.getChunkFromChunkCoords(x + 1, z);
        this.chunkCache[2] = this.world.getChunkFromChunkCoords(x, z + 1);
        this.chunkCache[3] = this.world.getChunkFromChunkCoords(x + 1, z + 1);
        this.createNewChunks = true;
    }

    private Chunk getChunk(int x, int y, int z)
    {
        if (y < 0 || y >= this.worldHeight)
            return null;

        x = x >> 4;
        z = z >> 4;
        if (this.cachedChunk != null && this.cachedChunk.xPosition == x && this.cachedChunk.zPosition == z)
            return this.cachedChunk;

        int index_x = (x - this.currentChunkX);
        int index_z = (z - this.currentChunkZ);
        if ((index_x == 0 || index_x == 1) && (index_z == 0 || index_z == 1))
            return cachedChunk = this.chunkCache[index_x | (index_z << 1)];
        else if (this.createNewChunks || this.world.getChunkProvider().chunkExists(x, z))
            return cachedChunk = this.world.getChunkFromBlockCoords(x, z);
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
    public byte getTypeData(int x, int y, int z)
    {
        Chunk chunk = this.getChunk(x, y, z);
        if (chunk == null)
            return 0;
        
        z = z & 0xF;
        x = x & 0xF;

        return (byte) chunk.getBlockMetadata(x, y, z);
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
        int y = chunk.getHeightValue(x, z);
        while(chunk.getBlockID(x, y, z) != DefaultMaterial.AIR.id && y <= worldHeight) {
            // Fix for incorrect lightmap
            y += 1;
        }
        return y;
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
        this.createNewChunks = createNew;
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
        return this.seed;
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
        this.seed = world.getSeed();
        for (Biome biome : biomes)
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
        this.seed = world.getSeed();
        // this.world.e = this.settings.waterLevelMax;

        this.dungeonGen = new WorldGenDungeons();
        this.strongholdGen = new MapGenStronghold();

        this.villageGen = new MapGenVillage();
        this.mineshaftGen = new MapGenMineshaft();
        this.pyramidsGen = new MapGenScatteredFeature();
        this.netherFortress = new MapGenNetherBridge();

        this.tree = new WorldGenTrees(false);
        this.cocoaTree = new WorldGenTrees(false, 5, 3, 3, true);
        this.bigTree = new WorldGenBigTree(false);
        this.forest = new WorldGenForest(false);
        this.swampTree = new WorldGenSwamp();
        this.taigaTree1 = new WorldGenTaiga1();
        this.taigaTree2 = new WorldGenTaiga2(false);
        this.hugeMushroom = new WorldGenBigMushroom();
        this.jungleTree = new WorldGenHugeTrees(false, 15, 3, 3);
        this.groundBush = new WorldGenShrub(3, 0);

        this.chunkCache = new Chunk[4];
        this.generator = new ChunkProvider(this);
    }

    public void setBiomeManager(IBiomeManager manager)
    {
        this.biomeManager = manager;
    }

    public void setOldBiomeManager(BiomeManagerOld manager)
    {
        this.oldBiomeManager = manager;
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
        biomeIntArray = this.getBiomes(biomeIntArray, x * 16, z * 16, 16, 16);

        for (int i1 = 0; i1 < arrayOfByte2.length; i1++)
        {
            arrayOfByte2[i1] = (byte) biomeIntArray[i1];
        }
    }

    @Override
    public LocalBiome getCalculatedBiome(int x, int z)
    {
        return getBiomeById(this.getCalculatedBiomeId(x, z));
    }

    @Override
    public LocalBiome getBiome(int x, int z)
    {
        return getBiomeById(world.getBiomeGenForCoords(x, z).biomeID);
    }

    @Override
    public void attachMetadata(int x, int y, int z, Tag tag)
    {
        // Convert Tag to a native nms tag
        NBTTagCompound nmsTag = NBTHelper.getNMSFromNBTTagCompound(tag);
        // Add the x, y and z position to it
        nmsTag.setInteger("x", x);
        nmsTag.setInteger("y", y);
        nmsTag.setInteger("z", z);
        // Create a Tile Entity of it and add it to the world
        TileEntity tileEntity = TileEntity.createAndLoadEntity(nmsTag);
        world.setBlockTileEntity(x, y, z, tileEntity);
    }
}
