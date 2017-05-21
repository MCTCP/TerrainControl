package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.*;
import com.khorn.terraincontrol.bukkit.generator.BukkitVanillaBiomeGenerator;
import com.khorn.terraincontrol.bukkit.generator.TXChunkGenerator;
import com.khorn.terraincontrol.bukkit.generator.TXInternalChunkGenerator;
import com.khorn.terraincontrol.bukkit.generator.TXWorldChunkManager;
import com.khorn.terraincontrol.bukkit.generator.structures.*;
import com.khorn.terraincontrol.bukkit.util.NBTHelper;
import com.khorn.terraincontrol.configuration.*;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.customobjects.CustomObjectStructureCache;
import com.khorn.terraincontrol.exception.BiomeNotFoundException;
import com.khorn.terraincontrol.generator.SpawnableObject;
import com.khorn.terraincontrol.generator.biome.BiomeGenerator;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.NamedBinaryTag;
import com.khorn.terraincontrol.util.helpers.ReflectionHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;
import com.khorn.terraincontrol.util.minecraftTypes.TreeType;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.generator.CustomChunkGenerator;

import java.util.*;

public class BukkitWorld implements LocalWorld
{
    // Initially false, set to true when enabled once
    private boolean initialized;

    private TXChunkGenerator generator;
    private WorldServer world;
    private ServerConfigProvider settings;
    private CustomObjectStructureCache structureCache;
    private String name;
    private BiomeGenerator biomeGenerator;
    private DataConverter dataConverter;

    private static int nextBiomeId = DefaultBiome.values().length;

    private static final int MAX_BIOMES_COUNT = 1024;
    private static final int MAX_SAVED_BIOMES_COUNT = 256;
    private static final int STANDARD_WORLD_HEIGHT = 128;

    private final Map<String, LocalBiome> biomeNames = new HashMap<String, LocalBiome>();

    public TXStrongholdGen strongholdGen;
    public TXVillageGen villageGen;
    public TXMineshaftGen mineshaftGen;
    public TXRareBuildingGen rareBuildingGen;
    public TXMansionGen mansionGen;
    public TXNetherFortressGen netherFortressGen;
    public TXOceanMonumentGen oceanMonumentGen;

    private WorldGenDungeons dungeon;
    private WorldGenFossils fossil;

    private WorldGenTrees tree;
    private WorldGenAcaciaTree acaciaTree;
    private WorldGenBigTree bigTree;
    private WorldGenForest birchTree;
    private WorldGenTrees cocoaTree;
    private WorldGenForestTree darkOakTree;
    private WorldGenGroundBush groundBush;
    private WorldGenHugeMushroom hugeBrownMushroom;
    private WorldGenHugeMushroom hugeRedMushroom;
    private WorldGenMegaTree hugeTaigaTree1;
    private WorldGenMegaTree hugeTaigaTree2;
    private WorldGenJungleTree jungleTree;
    private WorldGenForest longBirchTree;
    private WorldGenSwampTree swampTree;
    private WorldGenTaiga1 taigaTree1;
    private WorldGenTaiga2 taigaTree2;

    private Chunk[] chunkCache;

    public BukkitWorld(String _name)
    {
        this.name = _name;
    }

    @Override
    public LocalBiome createBiomeFor(BiomeConfig biomeConfig, BiomeIds biomeIds)
    {
        BukkitBiome biome;
        if (biomeConfig.defaultSettings.isCustomBiome)
        {
            biome = BukkitBiome.forCustomBiome(biomeConfig, biomeIds);
        } else
        {
            biome = BukkitBiome.forVanillaBiome(biomeConfig, BiomeBase.getBiome(biomeIds.getSavedId()));
        }

        this.biomeNames.put(biome.getName(), biome);

        return biome;
    }

    @Override
    public int getMaxBiomesCount()
    {
        return MAX_BIOMES_COUNT;
    }

    @Override
    public int getMaxSavedBiomesCount()
    {
        return MAX_SAVED_BIOMES_COUNT;
    }

    @Override
    public int getFreeBiomeId()
    {
        return nextBiomeId++;
    }

    @Override
    public BukkitBiome getBiomeById(int id) throws BiomeNotFoundException
    {
        LocalBiome biome = settings.getBiomeByIdOrNull(id);
        if (biome == null)
        {
            throw new BiomeNotFoundException(id, Arrays.asList(settings.getBiomeArray()));
        }
        return (BukkitBiome) biome;
    }
    
    @Override
    public LocalBiome getBiomeByIdOrNull(int id)
    {
        return settings.getBiomeByIdOrNull(id);
    }

    @Override
    public LocalBiome getBiomeByName(String name) throws BiomeNotFoundException
    {
        LocalBiome biome = biomeNames.get(name);
        if (biome == null)
        {
            throw new BiomeNotFoundException(name, biomeNames.keySet());
        }
        return biome;
    }

    @Override
    public Collection<? extends BiomeLoadInstruction> getDefaultBiomes()
    {
        // Loop through all default biomes and create the default
        // settings for them
        List<BiomeLoadInstruction> standardBiomes = new ArrayList<BiomeLoadInstruction>();
        for (DefaultBiome defaultBiome : DefaultBiome.values())
        {
            int id = defaultBiome.Id;
            BiomeLoadInstruction instruction = defaultBiome.getLoadInstructions(BukkitMojangSettings.fromId(id), STANDARD_WORLD_HEIGHT);
            standardBiomes.add(instruction);
        }

        return standardBiomes;
    }

    @Override
    public void prepareDefaultStructures(int chunkX, int chunkZ, boolean dry)
    {
        WorldConfig worldConfig = this.settings.getWorldConfig();

        if (worldConfig.strongholdsEnabled)
            this.strongholdGen.a(this.world, chunkX, chunkZ, null);
        if (worldConfig.mineshaftsEnabled)
            this.mineshaftGen.a(this.world, chunkX, chunkZ, null);
        if (worldConfig.villagesEnabled && dry)
            this.villageGen.a(this.world, chunkX, chunkZ, null);
        if (worldConfig.rareBuildingsEnabled)
            this.rareBuildingGen.a(this.world, chunkX, chunkZ, null);
        if (worldConfig.netherFortressesEnabled)
            this.netherFortressGen.a(this.world, chunkX, chunkZ, null);
        if (worldConfig.oceanMonumentsEnabled)
            this.oceanMonumentGen.a(this.world, chunkX, chunkZ, null);
        if (worldConfig.mansionsEnabled)
            this.mansionGen.a(this.world, chunkX, chunkZ, null);
    }

    @Override
    public boolean placeDungeon(Random rand, int x, int y, int z)
    {
        return dungeon.generate(world, rand, new BlockPosition(x, y, z));
    }

    @Override
    public boolean placeFossil(Random rand, ChunkCoordinate chunkCoord)
    {
        return fossil.generate(world, rand, new BlockPosition(chunkCoord.getBlockX(), 0, chunkCoord.getBlockZ()));
    }

    @Override
    public boolean placeTree(TreeType type, Random rand, int x, int y, int z)
    {
        BlockPosition blockPos = new BlockPosition(x, y, z);
        switch (type)
        {
            case Tree:
                return tree.generate(this.world, rand, blockPos);
            case BigTree:
                return bigTree.generate(this.world, rand, blockPos);
            case Forest:
            case Birch:
                return birchTree.generate(this.world, rand, blockPos);
            case TallBirch:
                return longBirchTree.generate(this.world, rand, blockPos);
            case HugeMushroom:
                if (rand.nextBoolean())
                {
                    return hugeBrownMushroom.generate(this.world, rand, blockPos);
                } else
                {
                    return hugeRedMushroom.generate(this.world, rand, blockPos);
                }
            case HugeRedMushroom:
                return hugeRedMushroom.generate(this.world, rand, blockPos);
            case HugeBrownMushroom:
                return hugeBrownMushroom.generate(this.world, rand, blockPos);
            case SwampTree:
                return swampTree.generate(this.world, rand, blockPos);
            case Taiga1:
                return taigaTree1.generate(this.world, rand, blockPos);
            case Taiga2:
                return taigaTree2.generate(this.world, rand, blockPos);
            case JungleTree:
                return jungleTree.generate(this.world, rand, blockPos);
            case GroundBush:
                return groundBush.generate(this.world, rand, blockPos);
            case CocoaTree:
                return cocoaTree.generate(this.world, rand, blockPos);
            case Acacia:
                return acaciaTree.generate(this.world, rand, blockPos);
            case DarkOak:
                return darkOakTree.generate(this.world, rand, blockPos);
            case HugeTaiga1:
                return hugeTaigaTree1.generate(this.world, rand, blockPos);
            case HugeTaiga2:
                return hugeTaigaTree2.generate(this.world, rand, blockPos);
            default:
                throw new RuntimeException("Failed to handle tree of type " + type.toString());
        }
    }

    @Override
    public boolean placeDefaultStructures(Random random, ChunkCoordinate chunkCoord)
    {
        ChunkCoordIntPair chunkIntPair = new ChunkCoordIntPair(chunkCoord.getChunkX(), chunkCoord.getChunkZ());
        WorldConfig worldConfig = this.settings.getWorldConfig();
        boolean villageGenerated = false;

        if (worldConfig.strongholdsEnabled)
            this.strongholdGen.a(this.world, random, chunkIntPair);
        if (worldConfig.mineshaftsEnabled)
            this.mineshaftGen.a(this.world, random, chunkIntPair);
        if (worldConfig.villagesEnabled)
            villageGenerated = this.villageGen.a(this.world, random, chunkIntPair);
        if (worldConfig.rareBuildingsEnabled)
            this.rareBuildingGen.a(this.world, random, chunkIntPair);
        if (worldConfig.netherFortressesEnabled)
            this.netherFortressGen.a(this.world, random, chunkIntPair);
        if (worldConfig.oceanMonumentsEnabled)
            this.oceanMonumentGen.a(this.world, random, chunkIntPair);
        if (worldConfig.mansionsEnabled)
            this.mansionGen.a(this.world, random, chunkIntPair);

        return villageGenerated;
    }

    @Override
    public void replaceBlocks(ChunkCoordinate chunkCoord)
    {
        if (!this.settings.getWorldConfig().BiomeConfigsHaveReplacement)
        {
            // Don't waste time here, ReplacedBlocks is empty everywhere
            return;
        }

        // Get cache
        Chunk[] cache = getChunkCache(chunkCoord);

        // Replace the blocks
        for(int i = 0; i < 4; i++) {
            replaceBlocks(cache[i], 0, 0, 16);
        }
    }

    private void replaceBlocks(Chunk rawChunk, int startXInChunk, int startZInChunk, int size)
    {
        int endXInChunk = startXInChunk + size;
        int endZInChunk = startZInChunk + size;
        int worldStartX = rawChunk.locX * 16;
        int worldStartZ = rawChunk.locZ * 16;

        ChunkSection[] sectionsArray = rawChunk.getSections();

        for (ChunkSection section : sectionsArray)
        {
            if (section == null)
                continue;

            for (int sectionX = startXInChunk; sectionX < endXInChunk; sectionX++)
            {
                for (int sectionZ = startZInChunk; sectionZ < endZInChunk; sectionZ++)
                {
                    LocalBiome biome = this.getBiome(worldStartX + sectionX, worldStartZ + sectionZ);
                    if (biome != null && biome.getBiomeConfig().replacedBlocks.hasReplaceSettings())
                    {
                        LocalMaterialData[][] replaceArray = biome.getBiomeConfig().replacedBlocks.compiledInstructions;
                        for (int sectionY = 0; sectionY < 16; sectionY++)
                        {
                            IBlockData block = section.getType(sectionX, sectionY, sectionZ);
                            int blockId = Block.getId(block.getBlock());
                            if (replaceArray[blockId] == null)
                                continue;

                            int y = section.getYPosition() + sectionY;
                            if (y >= replaceArray[blockId].length)
                                break;

                            BukkitMaterialData replaceTo = (BukkitMaterialData) replaceArray[blockId][y];
                            if (replaceTo == null || replaceTo.getBlockId() == blockId)
                                continue;

                            section.setType(sectionX, sectionY, sectionZ, replaceTo.internalBlock());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void placePopulationMobs(LocalBiome biome, Random random, ChunkCoordinate chunkCoord)
    {
        SpawnerCreature.a(this.world, ((BukkitBiome) biome).getHandle(), chunkCoord.getChunkX() * 16 + 8, chunkCoord.getChunkZ() * 16 + 8, 16, 16, random);
    }

    private Chunk getChunk(int x, int y, int z)
    {
        if (y < TerrainControl.WORLD_DEPTH || y >= TerrainControl.WORLD_HEIGHT)
            return null;

        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        if (this.chunkCache == null)
        {
            // Blocks requested outside population step
            // (Tree growing, /tc spawn, etc.)
           return world.getChunkAt(chunkX, chunkZ); 
        }

        // Restrict to chunks we are currently populating
        Chunk topLeftCachedChunk = this.chunkCache[0];
        int indexX = (chunkX - topLeftCachedChunk.locX);
        int indexZ = (chunkZ - topLeftCachedChunk.locZ);
        if ((indexX == 0 || indexX == 1) && (indexZ == 0 || indexZ == 1))
        {
            return this.chunkCache[indexX | (indexZ << 1)];
        } else
        {
            // Outside area
            if (this.settings.getWorldConfig().populationBoundsCheck)
            {
                return null;
            }
            if (world.getChunkProviderServer().isLoaded(chunkX, chunkZ))
            {
                return world.getChunkAt(chunkX, chunkZ);
            }
            return null;
        }
    }

    @Override
    public int getLiquidHeight(int x, int z)
    {
        for (int y = getHighestBlockYAt(x, z) - 1; y > 0; y--)
        {
            LocalMaterialData material = getMaterial(x, y, z);
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
            LocalMaterialData material = getMaterial(x, y, z);
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
        Chunk chunk = this.getChunk(x, y, z);
        if (chunk == null)
        {
            return true;
        }
        return chunk.a(x & 0xF, y, z & 0xF).getMaterial().equals(Material.AIR);
    }

    @Override
    public LocalMaterialData getMaterial(int x, int y, int z)
    {
        Chunk chunk = this.getChunk(x, y, z);
        if (chunk == null || y < TerrainControl.WORLD_DEPTH || y >= TerrainControl.WORLD_HEIGHT)
        {
            return BukkitMaterialData.ofMinecraftBlock(Blocks.AIR);
        }

        return BukkitMaterialData.ofMinecraftBlockData(chunk.a(x, y, z));
    }

    @Override
    public void setBlock(int x, int y, int z, LocalMaterialData material)
    {
        /*
         * This method usually breaks on every Minecraft update. Always check
         * whether the names are still correct. Often, you'll also need to
         * rewrite parts of this method for newer block place logic.
         */

        try
        {
            if (y < TerrainControl.WORLD_DEPTH || y >= TerrainControl.WORLD_HEIGHT)
            {
                return;
            }

            IBlockData blockData = ((BukkitMaterialData) material).internalBlock();

            // Get chunk from (faster) custom cache
            Chunk chunk = this.getChunk(x, y, z);

            if (chunk == null)
            {
                // Chunk is unloaded
                return;
            }

            BlockPosition blockPos = new BlockPosition(x, y, z);

            // Disable nearby block physics (except for tile entities) and set block
            boolean oldCaptureBlockStates = this.world.captureBlockStates;
            this.world.captureBlockStates = !(blockData.getBlock() instanceof ITileEntity);
            IBlockData oldBlockData = chunk.a(blockPos, blockData);
            this.world.captureBlockStates = oldCaptureBlockStates;

            if (oldBlockData == null)
            {
                return;
            }

            if (blockData.c() != oldBlockData.c() || blockData.d() != oldBlockData.d())
            {
                if (isSafeForLightUpdates(chunk, x, z))
                {
                    // Relight
                    world.methodProfiler.a("checkLight");
                    world.w(blockPos);
                    world.methodProfiler.b();
                }
            }

            // Notify world: (2 | 16) == update client, don't update observers
            world.notifyAndUpdatePhysics(blockPos, chunk, oldBlockData, blockData, 2 | 16);
        } catch (Throwable t)
        {
            String populatingChunkInfo = this.chunkCache == null? "(no chunk)" :
                    this.chunkCache[0].locX + "," + this.chunkCache[0].locZ;
            // Add location info to error
            RuntimeException runtimeException = new RuntimeException("Error setting "
                    + material + " block at " + x + "," + y + "," + z
                    + " while populating chunk " + populatingChunkInfo, t);
            runtimeException.setStackTrace(new StackTraceElement[0]);
            throw runtimeException;
        }
    }

    /**
     * When a light update hits an unloaded chunk, Minecraft unfortunately
     * attempts to generate this chunk. When this happens, two chunks will be
     * populated at the same time, which crashes the server. We must prevent
     * this by checking beforehand whether a light update will touch unloaded
     * chunks. If this is the case, the light update must be skipped.
     * @param currentChunk Current chunk (contains the following x and z)
     * @param x Block x in the world.
     * @param z Block z in the world.
     * @return True if it is safe to perform a light update at this location.
     */
    private boolean isSafeForLightUpdates(Chunk currentChunk, int x, int z)
    {
        int xInChunk = x & 0xf;
        int zInChunk = z & 0xf;
        if (xInChunk == 0 || xInChunk == 15 || zInChunk == 0 || zInChunk == 15)
        {
            // We're at the edge of a chunk
            // Ensure a larger region is loaded
            return currentChunk.areNeighborsLoaded(2);
        }
        return currentChunk.areNeighborsLoaded(1);
    }

    @Override
    public int getHighestBlockYAt(int x, int z)
    {
        Chunk chunk = this.getChunk(x, 0, z);
        if (chunk == null)
        {
            return -1;
        }

        int y = chunk.b(x & 0xf, z & 0xf);

        // Fix for incorrect light map
        boolean incorrectHeightMap = false;
        while (y < getHeightCap() && chunk.a(x, y, z).getMaterial().blocksLight())
        {
            y++;
            incorrectHeightMap = true;
        }
        if (incorrectHeightMap && isSafeForLightUpdates(chunk, x, z))
        {
            // Let Minecraft know that it made an error
            world.w(new BlockPosition(x, y, z)); // world.relight
        }

        return y;
    }

    @Override
    public void startPopulation(ChunkCoordinate chunkCoord)
    {
        if (this.chunkCache != null && settings.getWorldConfig().populationBoundsCheck)
        {
            throw new IllegalStateException("Chunk is already being populated."
                    + " This may be a bug in " + PluginStandardValues.PLUGIN_NAME + ", but it may also be"
                    + " another mod that is poking in unloaded chunks.\nSet"
                    + " PopulationBoundsCheck to false in the WorldConfig to"
                    + " disable this error.");
        }

        // Initialize cache
        this.chunkCache = loadFourChunks(chunkCoord);
    }

    private Chunk[] getChunkCache(ChunkCoordinate topLeft)
    {
        if (this.chunkCache == null || !topLeft.coordsMatch(this.chunkCache[0].locX, this.chunkCache[0].locZ))
        {
            // Cache is invalid, most likely because two chunks are being populated at once
            if (this.settings.getWorldConfig().populationBoundsCheck)
            {
                // ... but this can never happen, as startPopulation() checks for this if populationBoundsCheck is set
                // to true. So we must have a bug.
                throw new IllegalStateException("chunkCache is null! You've got a bug!");
            } else
            {
                // Use a temporary cache, best we can do
                return this.loadFourChunks(topLeft);
            }
        }
        return this.chunkCache;
    }

    private Chunk[] loadFourChunks(ChunkCoordinate topLeft)
    {
        Chunk[] chunkCache = new Chunk[4];
        for (int indexX = 0; indexX <= 1; indexX++)
        {
            for (int indexZ = 0; indexZ <= 1; indexZ++)
            {
                chunkCache[indexX | (indexZ << 1)] = world.getChunkAt(
                        topLeft.getChunkX() + indexX,
                        topLeft.getChunkZ() + indexZ
                );
            }
        }
        return chunkCache;
    }

    @Override
    public void endPopulation()
    {
        if (this.chunkCache == null && settings.getWorldConfig().populationBoundsCheck)
        {
            throw new IllegalStateException("Chunk is not being populated."
                    + " This may be a bug in Terrain Control, but it may also be"
                    + " another mod that is poking in unloaded chunks. Set"
                    + " PopulationBoundsCheck to false in the WorldConfig to"
                    + " disable this error.");
        }
        this.chunkCache = null;
    }

    @Override
    public int getLightLevel(int x, int y, int z)
    {
        return world.j(new BlockPosition(x, y, z)); // world.getBlockAndSkyLightAsItWereDay
    }

    @Override
    public boolean isLoaded(int x, int y, int z)
    {
        return this.getChunk(x, y, z) != null;
    }

    @Override
    public ConfigProvider getConfigs()
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
        return settings.getWorldConfig().worldHeightCap;
    }

    @Override
    public int getHeightScale()
    {
        return settings.getWorldConfig().worldHeightScale;
    }

    public TXChunkGenerator getChunkGenerator()
    {
        return this.generator;
    }

    public World getWorld()
    {
        return this.world;
    }

    /**
     * Sets the new settings and deprecates any references to the old
     * settings, if any.
     * 
     * @param newSettings The new settings.
     */
    public void setSettings(ServerConfigProvider newSettings)
    {
        if (this.settings == null)
        {
            this.settings = newSettings;
        } else
        {
            throw new IllegalStateException("Settings are already set");
        }
    }

    /**
     * Loads all settings again from disk.
     */
    public void reloadSettings()
    {
        this.biomeNames.clear();
        this.settings.reload();
    }

    /**
     * Enables/reloads this BukkitWorld. If you are reloading, don't forget to
     * set the new settings first using {@link #setSettings(ServerConfigProvider)}.
     *
     * @param world The world that needs to be enabled.
     */
    public void enable(org.bukkit.World world)
    {
        WorldServer mcWorld = ((CraftWorld) world).getHandle();

        // Do the things that always need to happen, whether we are enabling
        // for the first time or reloading
        this.world = mcWorld;

        // Inject our own BiomeManager (called WorldChunkManager)
        Class<? extends BiomeGenerator> biomeModeClass = this.settings.getWorldConfig().biomeMode;
        biomeGenerator = TerrainControl.getBiomeModeManager().createCached(biomeModeClass, this);
        injectWorldChunkManager(biomeGenerator);

        // Set sea level
        mcWorld.b(this.settings.getWorldConfig().waterLevelMax);

        if (!initialized)
        {
            // Things that need to be done only when enabling
            // for the first time
            this.structureCache = new CustomObjectStructureCache(this);
            this.dataConverter = DataConverterRegistry.a();

            switch (this.settings.getWorldConfig().ModeTerrain)
            {
                case Normal:
                case OldGenerator:
                    this.strongholdGen = new TXStrongholdGen(settings);
                    this.villageGen = new TXVillageGen(settings);
                    this.mineshaftGen = new TXMineshaftGen();
                    this.rareBuildingGen = new TXRareBuildingGen(settings);
                    this.mansionGen = new TXMansionGen(settings);
                    this.netherFortressGen = new TXNetherFortressGen();
                    this.oceanMonumentGen = new TXOceanMonumentGen(settings);

                    // Inject our own ChunkGenerator
                    injectInternalChunkGenerator(new TXInternalChunkGenerator(this, generator));
                case NotGenerate:
                case TerrainTest:
                    this.generator.onInitialize(this);
                    break;
                case Default:
                    break;
            }

            this.dungeon = new WorldGenDungeons();
            this.fossil = new WorldGenFossils();

            // Initialize trees
            IBlockData jungleLog = Blocks.LOG.getBlockData()
                    .set(BlockLog1.VARIANT, BlockWood.EnumLogVariant.JUNGLE);
            IBlockData jungleLeaves = Blocks.LEAVES.getBlockData()
                    .set(BlockLeaves1.VARIANT, BlockWood.EnumLogVariant.JUNGLE)
                    .set(BlockLeaves.CHECK_DECAY, false);
            IBlockData oakLeaves = Blocks.LEAVES.getBlockData()
                    .set(BlockLeaves1.VARIANT, BlockWood.EnumLogVariant.OAK)
                    .set(BlockLeaves.CHECK_DECAY, false);

            this.tree = new WorldGenTrees(false);
            this.acaciaTree = new WorldGenAcaciaTree(false);
            this.cocoaTree = new WorldGenTrees(false, 5, jungleLog, jungleLeaves, true);
            this.bigTree = new WorldGenBigTree(false);
            this.birchTree = new WorldGenForest(false, false);
            this.darkOakTree = new WorldGenForestTree(false);
            this.longBirchTree = new WorldGenForest(false, true);
            this.swampTree = new WorldGenSwampTree();
            this.taigaTree1 = new WorldGenTaiga1();
            this.taigaTree2 = new WorldGenTaiga2(false);
            this.hugeBrownMushroom = new WorldGenHugeMushroom(Blocks.BROWN_MUSHROOM_BLOCK);
            this.hugeRedMushroom = new WorldGenHugeMushroom(Blocks.RED_MUSHROOM_BLOCK);
            this.hugeTaigaTree1 = new WorldGenMegaTree(false, false);
            this.hugeTaigaTree2 = new WorldGenMegaTree(false, true);
            this.jungleTree = new WorldGenJungleTree(false, 10, 20, jungleLog, jungleLeaves);
            this.groundBush = new WorldGenGroundBush(jungleLog, oakLeaves);

            this.initialized = true;
        } else
        {
            // Things that need to be done only on reloading
            this.structureCache.reload(this);
        }
    }

    private void injectWorldChunkManager(BiomeGenerator biomeGenerator)
    {
        if (biomeGenerator instanceof BukkitVanillaBiomeGenerator)
        {
            // Let our biome generator depend on Minecraft's
            ((BukkitVanillaBiomeGenerator) biomeGenerator).setWorldChunkManager(this.world.worldProvider.k());
        } else
        {
            // Let Minecraft's biome generator depend on ours
            ReflectionHelper.setValueInFieldOfType(this.world.worldProvider,
                    WorldChunkManager.class, new TXWorldChunkManager(this, biomeGenerator));
        }
    }

    private void injectInternalChunkGenerator(CustomChunkGenerator chunkGenerator)
    {
        ChunkProviderServer chunkProvider = this.world.getChunkProviderServer();
        ChunkGenerator oldChunkGenerator = chunkProvider.chunkGenerator;

        if (oldChunkGenerator instanceof CustomChunkGenerator)
        {
            ReflectionHelper.setValueInFieldOfType(chunkProvider, ChunkGenerator.class, chunkGenerator);
        }
    }

    /**
     * Cleans up references of itself in Minecraft's native code.
     */
    public void disable()
    {
        // Restore vanilla chunk generator
        this.injectInternalChunkGenerator(new CustomChunkGenerator(world, getSeed(), generator));
    }

    public void setChunkGenerator(TXChunkGenerator _generator)
    {
        this.generator = _generator;
    }

    @Override
    public BukkitBiome getCalculatedBiome(int x, int z)
    {
        return getBiomeById(this.biomeGenerator.getBiome(x, z));
    }

    @Override
    public LocalBiome getBiome(int x, int z)
    {
        if (this.settings.getWorldConfig().populateUsingSavedBiomes)
        {
            return getSavedBiome(x, z);
        } else
        {
            return getCalculatedBiome(x, z);
        }
    }

    @Override
    public LocalBiome getSavedBiome(int x, int z) throws BiomeNotFoundException
    {
        int savedId = BiomeBase.a(world.getBiome(new BlockPosition(x, 0, z)));
        return getBiomeById(savedId);
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
        // Update to current Minecraft format (maybe we want to do this at
        // server startup instead, and then save the result?)
        nmsTag = this.dataConverter.a(DataConverterTypes.BLOCK_ENTITY, nmsTag, -1);
        // Add that data to the current tile entity in the world
        TileEntity tileEntity = world.getTileEntity(new BlockPosition(x, y, z));
        if (tileEntity != null)
        {
            tileEntity.a(nmsTag); // tileEntity.load
        } else
        {
            TerrainControl.log(LogMarker.DEBUG, "Skipping tile entity with id {}, cannot be placed at {},{},{} on id {}",
                    nmsTag.getString("id"), x, y, z, getMaterial(x, y, z));
        }
    }

    @Override
    public NamedBinaryTag getMetadata(int x, int y, int z)
    {
        TileEntity tileEntity = world.getTileEntity(new BlockPosition(x, y, z));
        if (tileEntity == null)
        {
            return null;
        }
        NBTTagCompound nmsTag = new NBTTagCompound();
        tileEntity.save(nmsTag);
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
    public BiomeGenerator getBiomeGenerator() {
        return biomeGenerator;
    }

    @Override
    public SpawnableObject getMojangStructurePart(String name)
    {
        MinecraftKey minecraftKey = new MinecraftKey(name);
        DefinedStructureManager mojangStructureParts = world.getDataManager().h();
        DefinedStructure mojangStructurePart = mojangStructureParts.a(world.getMinecraftServer(), minecraftKey);
        if (mojangStructurePart == null)
        {
            return null;
        }
        return new MojangStructurePart(name, mojangStructurePart);
    }

}
