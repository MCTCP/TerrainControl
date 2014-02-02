package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.generator.BiomeCacheWrapper;
import com.khorn.terraincontrol.bukkit.generator.TCChunkGenerator;
import com.khorn.terraincontrol.bukkit.generator.TCWorldChunkManager;
import com.khorn.terraincontrol.bukkit.generator.TCWorldProvider;
import com.khorn.terraincontrol.bukkit.generator.structures.*;
import com.khorn.terraincontrol.bukkit.util.NBTHelper;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.configuration.WorldSettings;
import com.khorn.terraincontrol.customobjects.CustomObjectStructureCache;
import com.khorn.terraincontrol.generator.biome.BiomeGenerator;
import com.khorn.terraincontrol.generator.biome.OldBiomeGenerator;
import com.khorn.terraincontrol.generator.biome.OutputType;
import com.khorn.terraincontrol.util.NamedBinaryTag;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;
import com.khorn.terraincontrol.util.minecraftTypes.TreeType;

import net.minecraft.server.v1_7_R1.*;

import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;

public class BukkitWorld implements LocalWorld
{
    // Initially false, set to true when enabled once
    private boolean initialized;

    private TCChunkGenerator generator;
    private WorldServer world;
    private WorldSettings settings;
    private CustomObjectStructureCache structureCache;
    private String name;
    private BiomeGenerator biomeManager;

    private static int nextBiomeId = DefaultBiome.values().length;

    private static final int MAX_BIOMES_COUNT = 1024;
    private LocalBiome[] biomes = new LocalBiome[MAX_BIOMES_COUNT];

    public int[] generationToSavedBiomeIds = new int[MAX_BIOMES_COUNT];
    public boolean haveVirtualBiomes = false;

    private HashMap<String, LocalBiome> biomeNames = new HashMap<String, LocalBiome>();
    private static ArrayList<LocalBiome> defaultBiomes = new ArrayList<LocalBiome>();

    public StrongholdGen strongholdGen;
    public VillageGen villageGen;
    public MineshaftGen mineshaftGen;
    public RareBuildingGen pyramidsGen;
    public NetherFortressGen netherFortress;

    private WorldGenTrees tree;
    private WorldGenAcaciaTree acaciaTree;
    private WorldGenBigTree bigTree;
    private WorldGenForest birchTree;
    private WorldGenTrees cocoaTree;
    private WorldGenForestTree darkOakTree;
    private WorldGenGroundBush groundBush;
    private WorldGenHugeMushroom hugeMushroom;
    private WorldGenMegaTree hugeTaigaTree1;
    private WorldGenMegaTree hugeTaigaTree2;
    private WorldGenJungleTree jungleTree;
    private WorldGenForest longBirchTree;
    private WorldGenSwampTree swampTree;
    private WorldGenTaiga1 taigaTree1;
    private WorldGenTaiga2 taigaTree2;

    private boolean createNewChunks;
    private Chunk[] chunkCache;
    private Chunk cachedChunk;

    private int currentChunkX;
    private int currentChunkZ;

    private BiomeBase[] biomeBaseArray;

    // Need for compatibility with old configs. It is start index for custom
    // biomes count used only for isle biomes.
    private int customBiomesCount = 21;

    static
    {
        for (DefaultBiome defaultBiome : DefaultBiome.values())
        {
            int id = defaultBiome.Id;
            LocalBiome localBiome = BukkitBiome.forVanillaBiome(BiomeBase.getBiome(id));
            defaultBiomes.add(localBiome);
        }
    }

    public BukkitWorld(String _name)
    {
        this.name = _name;
        for (LocalBiome biome : defaultBiomes)
        {
            this.biomeNames.put(biome.getName(), biome);
            this.biomes[biome.getIds().getGenerationId()] = biome;
        }
    }

    @Override
    public LocalBiome getNullBiome(String name)
    {
        return new NullBiome(name);
    }

    @Override
    public LocalBiome addCustomBiome(String name, BiomeIds biomeIds)
    {
        BukkitBiome biome = BukkitBiome.forCustomBiome(new CustomBiome(biomeIds, name), name, biomeIds, customBiomesCount++);

        biomes[biome.getIds().getGenerationId()] = biome;
        this.biomeNames.put(biome.getName(), biome);

        if (biomeIds.isVirtual()) {
            // Update generationToSavedBiomeIds array
            
            if (!this.haveVirtualBiomes)
            {
                // Initialize array first
                this.haveVirtualBiomes = true;
                for (int i = 0; i < MAX_BIOMES_COUNT; i++)
                    this.generationToSavedBiomeIds[i] = i;
            }
            this.generationToSavedBiomeIds[biomeIds.getGenerationId()] = biomeIds.getSavedId();
        }

        return biome;
    }

    @Override
    public int getMaxBiomesCount()
    {
        return MAX_BIOMES_COUNT;
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
        return this.biomeNames.get(name).getIds().getGenerationId();
    }

    @Override
    public ArrayList<LocalBiome> getDefaultBiomes()
    {
        return defaultBiomes;
    }

    @Override
    public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int x_size, int z_size, OutputType outputType)
    {
        if (this.biomeManager != null)
            return this.biomeManager.getBiomesUnZoomed(biomeArray, x, z, x_size, z_size, outputType);

        biomeBaseArray = this.world.worldProvider.e.getBiomes(biomeBaseArray, x, z, x_size, z_size);
        if (biomeArray == null || biomeArray.length < x_size * z_size)
            biomeArray = new int[x_size * z_size];
        for (int i = 0; i < x_size * z_size; i++)
            biomeArray[i] = biomeBaseArray[i].id;
        return biomeArray;
    }

    @Override
    public int[] getBiomes(int[] biomeArray, int x, int z, int x_size, int z_size, OutputType outputType)
    {
        if (this.biomeManager != null)
            return this.biomeManager.getBiomes(biomeArray, x, z, x_size, z_size, outputType);

        biomeBaseArray = this.world.worldProvider.e.a(biomeBaseArray, x, z, x_size, z_size, true);
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
        return this.world.worldProvider.e.getBiome(x, z).id;
    }

    @Override
    public double getBiomeFactorForOldBM(int index)
    {
        OldBiomeGenerator oldBiomeGenerator = (OldBiomeGenerator) this.biomeManager;
        return oldBiomeGenerator.oldTemperature1[index] * oldBiomeGenerator.oldWetness[index];
    }

    @Override
    public void prepareDefaultStructures(int chunkX, int chunkZ, boolean dry)
    {
        if (this.settings.worldConfig.strongholdsEnabled)
            this.strongholdGen.prepare(this.world, chunkX, chunkZ);
        if (this.settings.worldConfig.mineshaftsEnabled)
            this.mineshaftGen.prepare(this.world, chunkX, chunkZ);
        if (this.settings.worldConfig.villagesEnabled && dry)
            this.villageGen.prepare(this.world, chunkX, chunkZ);
        if (this.settings.worldConfig.rareBuildingsEnabled)
            this.pyramidsGen.prepare(this.world, chunkX, chunkZ);
        if (this.settings.worldConfig.netherFortressesEnabled)
            this.netherFortress.prepare(this.world, chunkX, chunkZ);
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
            case Birch:
                return birchTree.a(this.world, rand, x, y, z);
            case TallBirch:
                return longBirchTree.a(this.world, rand, x, y, z);
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
            case Acacia:
                return acaciaTree.a(this.world, rand, x, y, z);
            case DarkOak:
                return darkOakTree.a(this.world, rand, x, y, z);
            case HugeTaiga1:
                return hugeTaigaTree1.a(this.world, rand, x, y, z);
            case HugeTaiga2:
                return hugeTaigaTree2.a(this.world, rand, x, y, z);
            default:
                throw new AssertionError("Failed to handle tree of type " + type.toString());
        }
    }

    @Override
    public boolean placeDefaultStructures(Random random, int chunkX, int chunkZ)
    {
        boolean villageGenerated = false;
        if (this.settings.worldConfig.strongholdsEnabled)
            this.strongholdGen.place(this.world, random, chunkX, chunkZ);
        if (this.settings.worldConfig.mineshaftsEnabled)
            this.mineshaftGen.place(this.world, random, chunkX, chunkZ);
        if (this.settings.worldConfig.villagesEnabled)
            villageGenerated = this.villageGen.place(this.world, random, chunkX, chunkZ);
        if (this.settings.worldConfig.rareBuildingsEnabled)
            this.pyramidsGen.place(this.world, random, chunkX, chunkZ);
        if (this.settings.worldConfig.netherFortressesEnabled)
            this.netherFortress.place(this.world, random, chunkX, chunkZ);

        return villageGenerated;
    }

    @Override
    public void replaceBlocks()
    {
        if (this.settings.worldConfig.BiomeConfigsHaveReplacement)
        {
            // See the comment in replaceBiomes for an explanation of this
            replaceBlocks(this.chunkCache[0], 8, 8);
            replaceBlocks(this.chunkCache[1], 0, 8);
            replaceBlocks(this.chunkCache[2], 8, 0);
            replaceBlocks(this.chunkCache[3], 0, 0);
        }
    }

    private void replaceBlocks(Chunk rawChunk, int startXInChunk, int startZInChunk)
    {
        int endXInChunk = startXInChunk + 8;
        int endZInChunk = startZInChunk + 8;

        ChunkSection[] sectionsArray = rawChunk.i();

        byte[] chunkBiomes = rawChunk.m();

        for (ChunkSection section : sectionsArray)
        {
            if (section == null)
                continue;

            for (int sectionX = startXInChunk; sectionX < endXInChunk; sectionX++)
            {
                for (int sectionZ = startZInChunk; sectionZ < endZInChunk; sectionZ++)
                {
                    BiomeConfig biomeConfig = this.settings.biomeConfigs[chunkBiomes[(sectionZ << 4) | sectionX] & 0xFF];
                    if (biomeConfig != null && biomeConfig.ReplaceCount > 0)
                    {
                        for (int sectionY = 0; sectionY < 16; sectionY++)
                        {
                            Block block = section.getTypeId(sectionX, sectionY, sectionZ);
                            int blockId = Block.b(block);
                            if (biomeConfig.replaceMatrixBlocks[blockId] == null)
                                continue;

                            int replaceToId = biomeConfig.replaceMatrixBlocks[blockId][section.getYPosition() + sectionY];
                            if (replaceToId == -1 || (replaceToId >> 4) == blockId)
                                continue;

                            Block replaceTo = Block.e(replaceToId >> 4);

                            section.setTypeId(sectionX, sectionY, sectionZ, replaceTo);
                            section.setData(sectionX, sectionY, sectionZ, replaceToId & 0xF);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void replaceBiomes()
    {
        if (this.settings.worldConfig.HaveBiomeReplace)
        {
            // Like all other populators, this populator uses an offset of 8
            // blocks from the chunk start.
            // This is what happens when the top left chunk has it's biome
            // replaced:
            // +--------+--------+ . = no changes in biome for now
            // |........|........| # = biome is replaced
            // |....####|####....|
            // |....####|####....| The top left chunk is saved as chunk 0
            // +--------+--------+ in the cache, the top right chunk as 1,
            // |....####|####....| the bottom left as 2 and the bottom
            // |....####|####....| right chunk as 3.
            // |........|........|
            // +--------+--------+
            replaceBiomes(this.chunkCache[0].m(), 8, 8);
            replaceBiomes(this.chunkCache[1].m(), 0, 8);
            replaceBiomes(this.chunkCache[2].m(), 8, 0);
            replaceBiomes(this.chunkCache[3].m(), 0, 0);
        }
    }

    private void replaceBiomes(byte[] biomeArray, int startXInChunk, int startZInChunk)
    {
        int endXInChunk = startXInChunk + 8;
        int endZInChunkTimes16 = (startZInChunk + 8) * 16;
        for (int xInChunk = startXInChunk; xInChunk < endXInChunk; xInChunk++)
        {
            for (int zInChunkTimes16 = startZInChunk * 16; zInChunkTimes16 < endZInChunkTimes16; zInChunkTimes16 += 16)
            {
                biomeArray[zInChunkTimes16 | xInChunk] = (byte) (this.settings.replaceToBiomeNameMatrix[biomeArray[zInChunkTimes16 | xInChunk] & 0xFF] & 0xFF);
            }
        }
    }

    @Override
    public void placePopulationMobs(BiomeConfig config, Random random, int chunkX, int chunkZ)
    {
        SpawnerCreature.a(this.world, ((BukkitBiome) config.Biome).getHandle(), chunkX * 16 + 8, chunkZ * 16 + 8, 16, 16, random);
    }

    public void LoadChunk(Chunk chunk)
    {
        this.currentChunkX = chunk.locX;
        this.currentChunkZ = chunk.locZ;
        this.chunkCache[0] = chunk;
        this.chunkCache[1] = this.world.getChunkAt(chunk.locX + 1, chunk.locZ);
        this.chunkCache[2] = this.world.getChunkAt(chunk.locX, chunk.locZ + 1);
        this.chunkCache[3] = this.world.getChunkAt(chunk.locX + 1, chunk.locZ + 1);
        this.createNewChunks = true;
    }

    private Chunk getChunk(int x, int y, int z)
    {
        if (y < TerrainControl.worldDepth || y >= TerrainControl.worldHeight)
            return null;

        x >>= 4;
        z >>= 4;

        if (this.cachedChunk != null && this.cachedChunk.locX == x && this.cachedChunk.locZ == z)
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
        for (int y = getHighestBlockYAt(x, z) - 1; y > 0; y--)
        {
            DefaultMaterial material = getMaterial(x, y, z);
            if (material.isLiquid())
            {
                return y + 1;
            } else if (material.isSolid())
            {
                // Failed to find a liquid
                return -1;
            }
        }
        return -1;
    }

    @Override
    public int getSolidHeight(int x, int z)
    {
        for (int y = getHighestBlockYAt(x, z) - 1; y > 0; y--)
        {
            DefaultMaterial material = getMaterial(x, y, z);
            if (material.isSolid())
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

        z &= 0xF;
        x &= 0xF;

        return Block.b(chunk.getType(x, y, z));
    }

    @Override
    public byte getTypeData(int x, int y, int z)
    {
        Chunk chunk = this.getChunk(x, y, z);
        if (chunk == null)
        {
            return 0;
        }

        z &= 0xF;
        x &= 0xF;

        return (byte) chunk.getData(x, y, z);
    }

    @Override
    public void setBlock(final int x, final int y, final int z, final int typeId, final int data, final boolean updateLight, final boolean applyPhysics, final boolean notifyPlayers)
    {
        /*
         * This method usually breaks on every Minecraft update. Always check
         * whether the names are still correct. Often, you'll also need to
         * rewrite parts of this method for newer block place logic.
         */

        if (y < TerrainControl.worldDepth || y >= TerrainControl.worldHeight)
        {
            return;
        }

        Block block = Block.e(typeId);

        // Get chunk from (faster) custom cache
        Chunk chunk = this.getChunk(x, y, z);

        if (chunk == null)
        {
            // Chunk is unloaded
            return;
        }

        // Get old block (only needed for physics)
        Block oldBlockId = Blocks.AIR;
        if (applyPhysics)
        {
            oldBlockId = chunk.getType(x & 15, y, z & 15);
        }

        // Place block
        if (applyPhysics)
        {
            chunk.a(x & 15, y, z & 15, block, data);
        } else
        {
            // Temporarily make static, so that torches etc. don't pop off
            boolean oldStatic = world.isStatic;
            world.isStatic = true;
            chunk.a(x & 15, y, z & 15, block, data);
            world.isStatic = oldStatic;
        }

        // Relight and update
        if (updateLight)
        {
            world.A(x, y, z);
        }

        if (notifyPlayers && !world.isStatic)
        {
            world.notify(x, y, z);
        }

        if (!world.isStatic && applyPhysics)
        {
            world.update(x, y, z, oldBlockId);
        }
    }

    @Override
    public void setBlock(final int x, final int y, final int z, final int typeId, final int data)
    {
        this.setBlock(x, y, z, typeId, data, true, false, true);
    }

    @Override
    public int getHighestBlockYAt(int x, int z)
    {
        Chunk chunk = this.getChunk(x, 0, z);
        if (chunk == null)
        {
            return -1;
        }
        z &= 0xF;
        x &= 0xF;
        int y = chunk.b(x, z);

        // Fix for incorrect light map
        boolean incorrectHeightMap = false;
        while (y < getHeightCap() && chunk.getType(x, y, z).getMaterial().blocksLight())
        {
            y++;
            incorrectHeightMap = true;
        }
        if (incorrectHeightMap)
        {
            // Let Minecraft know that it made an error
            world.A(x, y, z); // world.relight
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
        return world.j(x, y, z); // world.getBlockAndSkyLightAsItWereDay
    }

    @Override
    public boolean isLoaded(int x, int y, int z)
    {
        return this.getChunk(x, y, z) != null;
    }

    @Override
    public WorldSettings getSettings()
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
    public int getHeightCap()
    {
        return settings.worldConfig.worldHeightCap;
    }

    @Override
    public int getHeightScale()
    {
        return settings.worldConfig.worldHeightScale;
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
     * Sets the new settings and deprecates any references to the old settings,
     * if any.
     * 
     * @param worldConfig
     *            The new settings.
     */
    public void setSettings(WorldSettings newSettings)
    {
        if (this.settings == null)
        {
            this.settings = newSettings;
        }
        else
        {
            // This is an ugly hack. It is a much better idea to give
            // the WorldSettings a proper reload method.
            this.settings.biomeConfigs = newSettings.biomeConfigs;
            this.settings.biomesCount = newSettings.biomesCount;
            this.settings.replaceToBiomeNameMatrix = newSettings.replaceToBiomeNameMatrix;

            // Deprecate old WorldConfig and replace with new
            this.settings.worldConfig.newSettings = newSettings.worldConfig;
            this.settings.worldConfig.isDeprecated = true;
            this.settings.worldConfig = newSettings.worldConfig;
        }
    }

    /**
     * Enables/reloads this BukkitWorld. If you are reloading, don't forget to
     * set the new settings first using {@link #setSettings(WorldConfig)}.
     * 
     * @param world
     *            The world that needs to be enabled.
     */
    public void enable(org.bukkit.World world)
    {
        WorldServer mcWorld = ((CraftWorld) world).getHandle();

        // Do the things that always need to happen, whether we are enabling
        // for the first time or reloading
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
        Class<? extends BiomeGenerator> biomeModeClass = this.settings.worldConfig.biomeMode;
        if (biomeModeClass != TerrainControl.getBiomeModeManager().VANILLA)
        {
            TCWorldChunkManager worldChunkManager = new TCWorldChunkManager(this);
            mcWorld.worldProvider.e = worldChunkManager;

            BiomeGenerator biomeManager = TerrainControl.getBiomeModeManager().create(biomeModeClass, this, new BiomeCacheWrapper(worldChunkManager));
            worldChunkManager.setBiomeManager(biomeManager);
            setBiomeManager(biomeManager);
        }

        if (!initialized)
        {
            // Things that need to be done only when enabling
            // for the first time
            this.structureCache = new CustomObjectStructureCache(this);

            switch (this.settings.worldConfig.ModeTerrain)
            {
                case Normal:
                case OldGenerator:
                    this.strongholdGen = new StrongholdGen(settings);
                    this.villageGen = new VillageGen(settings);
                    this.mineshaftGen = new MineshaftGen();
                    this.pyramidsGen = new RareBuildingGen(settings);
                    this.netherFortress = new NetherFortressGen();
                case NotGenerate:
                case TerrainTest:
                    this.generator.Init(this);
                    break;
                case Default:
                    break;
            }

            this.tree = new WorldGenTrees(false);
            this.acaciaTree = new WorldGenAcaciaTree(false);
            this.cocoaTree = new WorldGenTrees(false, 5, 3, 3, true);
            this.bigTree = new WorldGenBigTree(false);
            this.birchTree = new WorldGenForest(false, false);
            this.darkOakTree = new WorldGenForestTree(false);
            this.longBirchTree = new WorldGenForest(false, true);
            this.swampTree = new WorldGenSwampTree();
            this.taigaTree1 = new WorldGenTaiga1();
            this.taigaTree2 = new WorldGenTaiga2(false);
            this.hugeMushroom = new WorldGenHugeMushroom();
            this.hugeTaigaTree1 = new WorldGenMegaTree(false, false);
            this.hugeTaigaTree2 = new WorldGenMegaTree(false, true);
            this.jungleTree = new WorldGenJungleTree(false, 10, 20, 3, 3);
            this.groundBush = new WorldGenGroundBush(3, 0);

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
    public void attachMetadata(int x, int y, int z, NamedBinaryTag tag)
    {
        // Convert NamedBinaryTag to a native nms tag
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
            TerrainControl.log(Level.CONFIG, "Skipping tile entity with id {0}, cannot be placed at {1},{2},{3} on id {4}", new Object[] { nmsTag.getString("id"), x, y, z, world.getTypeId(x, y, z) });
        }
    }

    @Override
    public NamedBinaryTag getMetadata(int x, int y, int z)
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
        return NBTHelper.getNBTFromNMSTagCompound(null, nmsTag);
    }

    @Override
    public CustomObjectStructureCache getStructureCache()
    {
        return this.structureCache;
    }

    @Override
    public boolean canBiomeManagerGenerateUnzoomed()
    {
        if (this.biomeManager != null)
            return biomeManager.canGenerateUnZoomed();
        return true;
    }

}
