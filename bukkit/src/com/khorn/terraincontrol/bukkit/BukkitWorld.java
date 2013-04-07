package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.*;
import com.khorn.terraincontrol.biomegenerators.BiomeGenerator;
import com.khorn.terraincontrol.biomegenerators.OldBiomeGenerator;
import com.khorn.terraincontrol.bukkit.structuregens.*;
import com.khorn.terraincontrol.bukkit.util.NBTHelper;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.Tag;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.customobjects.CustomObjectStructureCache;
import com.khorn.terraincontrol.generator.resourcegens.TreeType;
import net.minecraft.server.v1_5_R2.*;
import org.bukkit.craftbukkit.v1_5_R2.CraftWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class BukkitWorld implements LocalWorld
{
    // Initially false, set to true when enabled once
    private boolean initialized;

    private TCChunkGenerator generator;
    private WorldServer world;
    private WorldConfig settings;
    private CustomObjectStructureCache structureCache;
    private String name;
    private BiomeGenerator biomeManager;

    private static int nextBiomeId = DefaultBiome.values().length;
    private static int maxBiomeCount = 256;
    private static LocalBiome[] biomes = new LocalBiome[maxBiomeCount];

    private HashMap<String, LocalBiome> biomeNames = new HashMap<String, LocalBiome>();
    private static ArrayList<LocalBiome> defaultBiomes = new ArrayList<LocalBiome>();

    public StrongholdGen strongholdGen;
    public VillageGen villageGen;
    public MineshaftGen mineshaftGen;
    public RareBuildingGen pyramidsGen;
    public NetherFortressGen netherFortress;

    private WorldGenTrees tree;
    private WorldGenTrees cocoaTree;
    private WorldGenBigTree bigTree;
    private WorldGenForest forest;
    private WorldGenSwampTree swampTree;
    private WorldGenTaiga1 taigaTree1;
    private WorldGenTaiga2 taigaTree2;
    private WorldGenHugeMushroom hugeMushroom;
    private WorldGenMegaTree jungleTree;
    private WorldGenGroundBush groundBush;

    private boolean createNewChunks;
    private Chunk[] chunkCache;
    private Chunk cachedChunk;

    private int currentChunkX;
    private int currentChunkZ;

    private BiomeBase[] biomeBaseArray;

    // TODO do something with that when bukkit allow custom world height.
    private int worldHeight = 256;
    private int heightBits = 8;

    private int customBiomesCount = 21;

    static
    {
        for (int i = 0; i < DefaultBiome.values().length; i++)
        {
            biomes[i] = new BukkitBiome(BiomeBase.biomes[i]);
            defaultBiomes.add(biomes[i]);
        }
    }

    public BukkitWorld(String _name)
    {
        this.name = _name;
        for (LocalBiome biome : defaultBiomes)
        {
            this.biomeNames.put(biome.getName(), biome);
        }
    }

    @Override
    public LocalBiome getNullBiome(String name)
    {
        return new NullBiome(name);
    }

    @Override
    public LocalBiome AddBiome(String name, int id)
    {
        BukkitBiome biome = new BukkitBiome(new CustomBiome(id, name));
        biome.setCustomID(customBiomesCount++);
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
            return this.biomeManager.getBiomesUnZoomed(biomeArray, x, z, x_size, z_size);

        biomeBaseArray = this.world.worldProvider.d.getBiomes(biomeBaseArray, x, z, x_size, z_size);
        if (biomeArray == null || biomeArray.length < x_size * z_size)
            biomeArray = new int[x_size * z_size];
        for (int i = 0; i < x_size * z_size; i++)
            biomeArray[i] = biomeBaseArray[i].id;
        return biomeArray;
    }

    @Override
    public float[] getTemperatures(int x, int z, int x_size, int z_size)
    {
        if (this.biomeManager != null)
            return this.biomeManager.getTemperatures(null, x, z, x_size, z_size);
        return this.world.worldProvider.d.getTemperatures(null, x, z, x_size, z_size);
    }

    @Override
    public int[] getBiomes(int[] biomeArray, int x, int z, int x_size, int z_size)
    {
        if (this.biomeManager != null)
            return this.biomeManager.getBiomes(biomeArray, x, z, x_size, z_size);

        biomeBaseArray = this.world.worldProvider.d.a(biomeBaseArray, x, z, x_size, z_size, true);
        if (biomeArray == null || biomeArray.length < x_size * z_size)
            biomeArray = new int[x_size * z_size];
        for (int i = 0; i < x_size * z_size; i++)
            biomeArray[i] = biomeBaseArray[i].id;
        return biomeArray;
    }

    @Override
    public int getCalculatedBiomeId(int x, int z)
    {
        if (this.biomeManager != null)
            return this.biomeManager.getBiome(x, z);
        return this.world.worldProvider.d.getBiome(x, z).id;
    }

    @Override
    public double getBiomeFactorForOldBM(int index)
    {
        OldBiomeGenerator oldBiomeGenerator = (OldBiomeGenerator) this.biomeManager;
        return oldBiomeGenerator.oldTemperature1[index] * oldBiomeGenerator.oldWetness[index];
    }

    @Override
    public void PrepareTerrainObjects(int x, int z, byte[] chunkArray, boolean dry)
    {
        if (this.settings.strongholdsEnabled)
            this.strongholdGen.a(null, this.world, x, z, chunkArray);
        if (this.settings.mineshaftsEnabled)
            this.mineshaftGen.a(null, this.world, x, z, chunkArray);
        if (this.settings.villagesEnabled && dry)
            this.villageGen.a(null, this.world, x, z, chunkArray);
        if (this.settings.rareBuildingsEnabled)
            this.pyramidsGen.a(null, this.world, x, z, chunkArray);
        if (this.settings.netherFortressesEnabled)
            this.netherFortress.a(null, this.world, x, z, chunkArray);
    }

    @Override
    public void PlaceDungeons(Random rand, int x, int y, int z)
    {
        new WorldGenDungeons().a(this.world, rand, x, y, z);
    }

    @Override
    public boolean PlaceTree(TreeType type, Random rand, int x, int y, int z)
    {
        switch (type)
        {
            case Tree:
                return tree.a(this.world, rand, x, y, z);
            case BigTree:
                bigTree.a(1.0D, 1.0D, 1.0D);
                return bigTree.a(this.world, rand, x, y, z);
            case Forest:
                return forest.a(this.world, rand, x, y, z);
            case HugeMushroom:
                hugeMushroom.a(1.0D, 1.0D, 1.0D);
                return hugeMushroom.a(this.world, rand, x, y, z);
            case SwampTree:
                return swampTree.a(this.world, rand, x, y, z);
            case Taiga1:
                return taigaTree1.a(this.world, rand, x, y, z);
            case Taiga2:
                return taigaTree2.a(this.world, rand, x, y, z);
            case JungleTree:
                return jungleTree.a(this.world, rand, x, y, z);
            case GroundBush:
                return groundBush.a(this.world, rand, x, y, z);
            case CocoaTree:
                return cocoaTree.a(this.world, rand, x, y, z);
        }
        return false;
    }

    @Override
    public boolean PlaceTerrainObjects(Random rand, int chunk_x, int chunk_z)
    {
        boolean villageGenerated = false;
        if (this.settings.strongholdsEnabled)
            this.strongholdGen.a(this.world, rand, chunk_x, chunk_z);
        if (this.settings.mineshaftsEnabled)
            this.mineshaftGen.a(this.world, rand, chunk_x, chunk_z);
        if (this.settings.villagesEnabled)
            villageGenerated = this.villageGen.a(this.world, rand, chunk_x, chunk_z);
        if (this.settings.rareBuildingsEnabled)
            this.pyramidsGen.a(this.world, rand, chunk_x, chunk_z);
        if (this.settings.netherFortressesEnabled)
            this.netherFortress.a(this.world, rand, chunk_x, chunk_z);

        return villageGenerated;
    }

    // This part work with ReplacedBlocks after all spawns
    // TODO: check how its work.
    @Override
    public void replaceBlocks()
    {
        if (this.settings.BiomeConfigsHaveReplacement)
        {
            Chunk rawChunk = this.chunkCache[0];

            ChunkSection[] sectionsArray = rawChunk.i();

            byte[] ChunkBiomes = rawChunk.m();

            int x = this.currentChunkX * 16;
            int z = this.currentChunkZ * 16;

            for (ChunkSection section : sectionsArray)
            {
                if (section == null)
                    continue;

                for (int sectionX = 0; sectionX < 16; sectionX++)
                {
                    for (int sectionZ = 0; sectionZ < 16; sectionZ++)
                    {
                        BiomeConfig biomeConfig = this.settings.biomeConfigs[ChunkBiomes[(sectionZ << 4) | sectionX] & 0xFF];
                        if (biomeConfig != null && biomeConfig.ReplaceCount > 0)
                        {
                            for (int sectionY = 0; sectionY < 16; sectionY++)
                            {
                                int blockId = section.a(sectionX, sectionY, sectionZ);
                                if (biomeConfig.replaceMatrixBlocks[blockId] == null)
                                    continue;

                                int replaceTo = biomeConfig.replaceMatrixBlocks[blockId][section.d() + sectionY];
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

    @Override
    public void replaceBiomes()
    {
        if (this.settings.HaveBiomeReplace)
        {
            byte[] ChunkBiomes = this.chunkCache[0].m();

            for (int i = 0; i < ChunkBiomes.length; i++)
                ChunkBiomes[i] = (byte) (this.settings.ReplaceMatrixBiomes[ChunkBiomes[i] & 0xFF] & 0xFF);
        }

    }

    @Override
    public void placePopulationMobs(BiomeConfig config, Random random, int chunkX, int chunkZ)
    {
        SpawnerCreature.a(this.world, ((BukkitBiome) config.Biome).getHandle(), chunkX * 16 + 8, chunkZ * 16 + 8, 16, 16, random);
    }

    public void LoadChunk(Chunk chunk)
    {
        this.currentChunkX = chunk.x;
        this.currentChunkZ = chunk.z;
        this.chunkCache[0] = chunk;
        this.chunkCache[1] = this.world.getChunkAt(chunk.x + 1, chunk.z);
        this.chunkCache[2] = this.world.getChunkAt(chunk.x, chunk.z + 1);
        this.chunkCache[3] = this.world.getChunkAt(chunk.x + 1, chunk.z + 1);
        this.createNewChunks = true;
    }

    private Chunk getChunk(int x, int y, int z)
    {
        if (y < 0 || y >= worldHeight)
            return null;

        x = x >> 4;
        z = z >> 4;

        if (this.cachedChunk != null && this.cachedChunk.x == x && this.cachedChunk.z == z)
            return this.cachedChunk;

        int index_x = (x - this.currentChunkX);
        int index_z = (z - this.currentChunkZ);
        if ((index_x == 0 || index_x == 1) && (index_z == 0 || index_z == 1))
            return cachedChunk = this.chunkCache[index_x | (index_z << 1)];
        else if (this.createNewChunks || this.world.chunkProvider.isChunkLoaded(x, z))
            return cachedChunk = this.world.getChunkAt(x, z);
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
            int id = chunk.getTypeId(x, y, z);
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

        for (int y = getHighestBlockYAt(x, z) - 1; y > 0; y--)
        {
            int id = chunk.getTypeId(x & 0xF, y, z & 0xF);
            if (DefaultMaterial.getMaterial(id).isSolid())
            {
                return y + 1;
            }
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
        {
            return 0;
        }

        z = z & 0xF;
        x = x & 0xF;

        return chunk.getTypeId(x, y, z);
    }

    @Override
    public byte getTypeData(int x, int y, int z)
    {
        Chunk chunk = this.getChunk(x, y, z);
        if (chunk == null)
        {
            return 0;
        }

        z = z & 0xF;
        x = x & 0xF;

        return (byte) chunk.getData(x, y, z);
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
            int oldTypeId = chunk.getTypeId(x & 15, y, z & 15);
            chunk.a(x & 15, y, z & 15, typeId, data); // chunk.setTypeIdAndData(....)
            // Workaround for (bug in?) CraftBukkit
            if (chunk.i()[y >> 4] != null)
                chunk.i()[y >> 4].j().a(x & 15, y & 15, z & 15, data); // chunk.getSections()[..].getData().set(....)
            this.world.applyPhysics(x, y, z, typeId == 0 ? oldTypeId : typeId);
        } else
        {
            chunk.a(x & 15, y, z & 15, typeId, data); // chunk.setTypeIdAndData(....)
            // Workaround for (bug in?) Minecraft
            if (chunk.i()[y >> 4] != null)
                chunk.i()[y >> 4].j().a(x & 15, y & 15, z & 15, data); // chunk.getSections()[..].getData().set(....)
        }

        if (updateLight)
        {
            this.world.v(x, y, z);
        }

        if (notifyPlayers && chunk.seenByPlayer)
        {
            this.world.notify(x, y, z);
        }

    }

    @Override
    public void setBlock(final int x, final int y, final int z, final int typeId, final int data)
    {
        this.setBlock(x, y, z, typeId, data, false, false, true);
    }

    @Override
    public int getHighestBlockYAt(int x, int z)
    {
        Chunk chunk = this.getChunk(x, 0, z);
        if (chunk == null)
        {
            return -1;
        }
        z = z & 0xF;
        x = x & 0xF;
        int y = chunk.b(x, z);
        while (chunk.getTypeId(x, y, z) != DefaultMaterial.AIR.id && y <= worldHeight)
        {
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
        return world.m(x, y, z); // world.getBlockAndSkyLightAsItWereDay
    }

    @Override
    public boolean isLoaded(int x, int y, int z)
    {
        return world.isLoaded(x, y, z);
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
        return world.getSeed();
    }

    @Override
    public int getHeight()
    {
        return worldHeight;
    }

    @Override
    public int getHeightBits()
    {
        return heightBits;
    }

    public TCChunkGenerator getChunkGenerator()
    {
        return this.generator;
    }

    public World getWorld()
    {
        return this.world;
    }

    /**
     * Sets the new settings and deprecates any references to the old settings, if any.
     * @param worldConfig The new settings.
     */
    public void setSettings(WorldConfig worldConfig)
    {
        if (this.settings != null)
        {
            this.settings.newSettings = worldConfig;
            this.settings.isDeprecated = true;
        }
        this.settings = worldConfig;
    }

    /**
     * Enables/reloads this BukkitWorld. If you are reloading, 
     * don't forget to set the new settings first using 
     * {@link #setSettings(WorldConfig)}.
     * @param world The world that needs to be enabled.
     */
    public void enable(org.bukkit.World world)
    {
        WorldServer mcWorld = ((CraftWorld) world).getHandle();

        // Do the things that always need to happen, whether we are enabling for
        // the first time or reloading
        this.world = mcWorld;
        this.chunkCache = new Chunk[4];

        // Inject our own WorldProvider
        if (mcWorld.worldProvider.getName().equals("Overworld"))
        {
            // Only replace the worldProvider if it's the overworld
            // Replacing other dimensions causes a lot of glitches
            mcWorld.worldProvider = new TCWorldProvider(this, this.world.worldProvider);
        }
        
        // Inject our own BiomeManager (called WorldChunkManager)
        Class<? extends BiomeGenerator> biomeModeClass = this.settings.biomeMode;
        if (biomeModeClass != TerrainControl.getBiomeModeManager().VANILLA)
        {
            TCWorldChunkManager worldChunkManager = new TCWorldChunkManager(this);
            mcWorld.worldProvider.d = worldChunkManager;

            BiomeGenerator biomeManager = TerrainControl.getBiomeModeManager().create(biomeModeClass, this, new BiomeCacheWrapper(worldChunkManager));
            worldChunkManager.setBiomeManager(biomeManager);
            setBiomeManager(biomeManager);
        }

        if (!initialized)
        {
            // Things that need to be done only when enabling for the first time
            this.structureCache = new CustomObjectStructureCache(this);

            switch (this.settings.ModeTerrain)
            {
                case Normal:
                case OldGenerator:
                    this.strongholdGen = new StrongholdGen(settings);
                    this.villageGen = new VillageGen(settings);
                    this.mineshaftGen = new MineshaftGen();
                    this.pyramidsGen = new RareBuildingGen(settings);
                    this.netherFortress = new NetherFortressGen();
                case NotGenerate:
                    this.tree = new WorldGenTrees(false);
                    this.cocoaTree = new WorldGenTrees(false, 5, 3, 3, true);
                    this.bigTree = new WorldGenBigTree(false);
                    this.forest = new WorldGenForest(false);
                    this.swampTree = new WorldGenSwampTree();
                    this.taigaTree1 = new WorldGenTaiga1();
                    this.taigaTree2 = new WorldGenTaiga2(false);
                    this.hugeMushroom = new WorldGenHugeMushroom();
                    this.jungleTree = new WorldGenMegaTree(false, 15, 3, 3);
                    this.groundBush = new WorldGenGroundBush(3, 0);
                case TerrainTest:
                    this.generator.Init(this);
                    break;
                case Default:
                    break;
            }

            this.initialized = true;
        } else
        {
            // Things that need to be done only on reloading
            this.structureCache.reload(this);
        }
    }

    /**
     * Cleans up references of itself in Minecraft's native code.
     */
    public void disable()
    {
        // Restore old world provider if replaced
        if (world.worldProvider instanceof TCWorldProvider)
        {
            world.worldProvider = ((TCWorldProvider) world.worldProvider).getOldWorldProvider();
        }
    }

    public void setChunkGenerator(TCChunkGenerator _generator)
    {
        this.generator = _generator;
    }

    public void setBiomeManager(BiomeGenerator manager)
    {
        this.biomeManager = manager;
    }

    @Override
    public void setHeightBits(int heightBits)
    {
        this.heightBits = heightBits;
        this.worldHeight = 1 << heightBits;
    }

    @Override
    public LocalBiome getCalculatedBiome(int x, int z)
    {
        return getBiomeById(getCalculatedBiomeId(x, z));
    }

    @Override
    public int getBiomeId(int x, int z)
    {
        return world.getBiome(x, z).id;
    }

    @Override
    public LocalBiome getBiome(int x, int z)
    {
        return getBiomeById(world.getBiome(x, z).id);
    }

    @Override
    public void attachMetadata(int x, int y, int z, Tag tag)
    {
        // Convert Tag to a native nms tag
        NBTTagCompound nmsTag = NBTHelper.getNMSFromNBTTagCompound(tag);
        // Add the x, y and z position to it
        nmsTag.setInt("x", x);
        nmsTag.setInt("y", y);
        nmsTag.setInt("z", z);
        // Add that data to the current tile entity in the world
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity != null)
        {
            tileEntity.a(nmsTag); // tileEntity.load
        } else
        {
            TerrainControl.log("Skipping tile entity with id " + nmsTag.getString("id") + ", cannot be placed at " + x + "," + y + "," + z + " on id " + world.getTypeId(x, y, z));
        }
    }

    @Override
    public Tag getMetadata(int x, int y, int z)
    {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity == null)
        {
            return null;
        }
        NBTTagCompound nmsTag = new NBTTagCompound();
        tileEntity.b(nmsTag); // tileEntity.save
        nmsTag.remove("x");
        nmsTag.remove("y");
        nmsTag.remove("z");
        return NBTHelper.getNBTFromNMSTagCompound(nmsTag);
    }

    @Override
    public CustomObjectStructureCache getStructureCache()
    {
        return this.structureCache;
    }
}
