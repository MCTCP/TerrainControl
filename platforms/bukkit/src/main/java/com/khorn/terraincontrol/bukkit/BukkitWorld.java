package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.*;
import com.khorn.terraincontrol.bukkit.generator.BukkitVanillaBiomeGenerator;
import com.khorn.terraincontrol.bukkit.generator.TCChunkGenerator;
import com.khorn.terraincontrol.bukkit.generator.TCWorldChunkManager;
import com.khorn.terraincontrol.bukkit.generator.TCWorldProvider;
import com.khorn.terraincontrol.bukkit.generator.structures.*;
import com.khorn.terraincontrol.bukkit.util.NBTHelper;
import com.khorn.terraincontrol.configuration.*;
import com.khorn.terraincontrol.configuration.WorldSettings;
import com.khorn.terraincontrol.customobjects.CustomObjectStructureCache;
import com.khorn.terraincontrol.exception.BiomeNotFoundException;
import com.khorn.terraincontrol.generator.biome.BiomeGenerator;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.NamedBinaryTag;
import com.khorn.terraincontrol.util.helpers.ReflectionHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;
import com.khorn.terraincontrol.util.minecraftTypes.TreeType;
import net.minecraft.server.v1_8_R1.*;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;

import java.util.*;

public class BukkitWorld implements LocalWorld
{
    // Initially false, set to true when enabled once
    private boolean initialized;

    private TCChunkGenerator generator;
    private WorldServer world;
    private WorldSettings settings;
    private CustomObjectStructureCache structureCache;
    private String name;
    private BiomeGenerator biomeGenerator;

    private static int nextBiomeId = DefaultBiome.values().length;

    private static final int MAX_BIOMES_COUNT = 1024;
    private static final int MAX_SAVED_BIOMES_COUNT = 256;
    private static final int STANDARD_WORLD_HEIGHT = 128;

    private final Map<String, LocalBiome> biomeNames = new HashMap<String, LocalBiome>();

    public StrongholdGen strongholdGen;
    public VillageGen villageGen;
    public MineshaftGen mineshaftGen;
    public RareBuildingGen pyramidsGen;
    public NetherFortressGen netherFortressGen;
    public OceanMonumentGen oceanMonumentGen;

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
        LocalBiome biome = settings.biomes[id];
        if (biome == null)
        {
            throw new BiomeNotFoundException(id, Arrays.asList(settings.biomes));
        }
        return (BukkitBiome) biome;
    }
    
    @Override
    public LocalBiome getBiomeByIdOrNull(int id)
    {
        return settings.biomes[id];
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
        if (this.settings.worldConfig.strongholdsEnabled)
            this.strongholdGen.prepare(this.world, chunkX, chunkZ);
        if (this.settings.worldConfig.mineshaftsEnabled)
            this.mineshaftGen.prepare(this.world, chunkX, chunkZ);
        if (this.settings.worldConfig.villagesEnabled && dry)
            this.villageGen.prepare(this.world, chunkX, chunkZ);
        if (this.settings.worldConfig.rareBuildingsEnabled)
            this.pyramidsGen.prepare(this.world, chunkX, chunkZ);
        if (this.settings.worldConfig.netherFortressesEnabled)
            this.netherFortressGen.prepare(this.world, chunkX, chunkZ);
        if (this.settings.worldConfig.oceanMonumentsEnabled)
            this.oceanMonumentGen.prepare(this.world, chunkX, chunkZ);
    }

    @Override
    public void PlaceDungeons(Random rand, int x, int y, int z)
    {
        new WorldGenDungeons().generate(this.world, rand, new BlockPosition(x, y, z));
    }

    @Override
    public boolean PlaceTree(TreeType type, Random rand, int x, int y, int z)
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
                return hugeMushroom.generate(this.world, rand, blockPos);
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
                throw new AssertionError("Failed to handle tree of type " + type.toString());
        }
    }

    @Override
    public boolean placeDefaultStructures(Random random, ChunkCoordinate chunkCoord)
    {
        ChunkCoordIntPair chunkIntPair = new ChunkCoordIntPair(chunkCoord.getChunkX(), chunkCoord.getChunkZ());
        boolean villageGenerated = false;

        if (this.settings.worldConfig.strongholdsEnabled)
            this.strongholdGen.place(this.world, random, chunkIntPair);
        if (this.settings.worldConfig.mineshaftsEnabled)
            this.mineshaftGen.place(this.world, random, chunkIntPair);
        if (this.settings.worldConfig.villagesEnabled)
            villageGenerated = this.villageGen.place(this.world, random, chunkIntPair);
        if (this.settings.worldConfig.rareBuildingsEnabled)
            this.pyramidsGen.place(this.world, random, chunkIntPair);
        if (this.settings.worldConfig.netherFortressesEnabled)
            this.netherFortressGen.place(this.world, random, chunkIntPair);
        if (this.settings.worldConfig.oceanMonumentsEnabled)
            this.oceanMonumentGen.place(this.world, random, chunkIntPair);

        return villageGenerated;
    }

    @Override
    public void replaceBlocks(ChunkCoordinate chunkCoord)
    {
        if (!this.settings.worldConfig.BiomeConfigsHaveReplacement)
        {
            // Don't waste time here, ReplacedBlocks is empty everywhere
            return;
        }

        // Get cache
        Chunk[] cache = getChunkCache(chunkCoord);

        // Replace the blocks
        replaceBlocks(cache[0], 8, 8);
        replaceBlocks(cache[1], 0, 8);
        replaceBlocks(cache[2], 8, 0);
        replaceBlocks(cache[3], 0, 0);
    }

    private void replaceBlocks(Chunk rawChunk, int startXInChunk, int startZInChunk)
    {
        int endXInChunk = startXInChunk + 8;
        int endZInChunk = startZInChunk + 8;
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
            if (this.settings.worldConfig.populationBoundsCheck)
            {
                return null;
            }
            if (world.chunkProviderServer.isChunkLoaded(chunkX, chunkZ))
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
        return chunk.getTypeAbs(x & 0xF, y, z & 0xF).getMaterial().equals(Material.AIR);
    }

    @Override
    public LocalMaterialData getMaterial(int x, int y, int z)
    {
        Chunk chunk = this.getChunk(x, y, z);
        if (chunk == null || y < TerrainControl.WORLD_DEPTH || y >= TerrainControl.WORLD_HEIGHT)
        {
            return BukkitMaterialData.ofMinecraftBlock(Blocks.AIR);
        }

        // There's no chunk.getType(x,y,z), only chunk.getType(BlockPosition)
        // so we use this little hack.
        // Creating a block position for every block lookup is expensive and
        // a major cause of Minecraft 1.8's performance degradation:
        // http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/1272953-optifine?comment=43757
        ChunkSection section = chunk.getSections()[y >> 4];
        if (section == null)
        {
            return BukkitMaterialData.ofMinecraftBlock(Blocks.AIR);
        }

        IBlockData blockData = section.getType(x & 0xF, y & 0xF, z & 0xF);
        return BukkitMaterialData.ofMinecraftBlockData(blockData);
    }

    @Override
    public void setBlock(int x, int y, int z, LocalMaterialData material)
    {
        /*
         * This method usually breaks on every Minecraft update. Always check
         * whether the names are still correct. Often, you'll also need to
         * rewrite parts of this method for newer block place logic.
         */

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

        chunk.a(blockPos, blockData);

        // Relight and update players
        world.x(blockPos);
        if (!world.isStatic)
        {
            world.notify(blockPos);
        }
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
        while (y < getHeightCap() && chunk.getTypeAbs(x, y, z).getMaterial().blocksLight())
        {
            y++;
            incorrectHeightMap = true;
        }
        if (incorrectHeightMap)
        {
            // Let Minecraft know that it made an error
            world.x(new BlockPosition(x, y, z)); // world.relight
        }

        return y;
    }

    @Override
    public void startPopulation(ChunkCoordinate chunkCoord)
    {
        if (this.chunkCache != null && settings.worldConfig.populationBoundsCheck)
        {
            throw new IllegalStateException("Chunk is already being populated."
                    + " This may be a bug in Terrain Control, but it may also be"
                    + " another mod that is poking in unloaded chunks. Set"
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
            // Cache is invalid, most likely because two chunks are being
            // populated at once
            if (this.settings.worldConfig.populationBoundsCheck)
            {
                // ... but this can never happen, as startPopulation() checks
                // for this if populationBoundsCheck is set to true
                // So we have a bug
                throw new IllegalStateException("chunkCache is null");
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
                chunkCache[indexX | (indexZ << 1)] = world.getChunkAt(topLeft.getChunkX() + indexX, topLeft.getChunkZ() + indexZ);
            }
        }
        return chunkCache;
    }

    @Override
    public void endPopulation()
    {
        if (this.chunkCache == null && settings.worldConfig.populationBoundsCheck)
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
        return world.k(new BlockPosition(x, y, z)); // world.getBlockAndSkyLightAsItWereDay
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
     * Sets the new settings and deprecates any references to the old
     * settings, if any.
     * 
     * @param worldConfig The new settings.
     */
    public void setSettings(WorldSettings newSettings)
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
     * set the new settings first using {@link #setSettings(WorldConfig)}.
     * 
     * @param world The world that needs to be enabled.
     */
    public void enable(org.bukkit.World world)
    {
        WorldServer mcWorld = ((CraftWorld) world).getHandle();

        // Do the things that always need to happen, whether we are enabling
        // for the first time or reloading
        this.world = mcWorld;

        // Inject our own WorldProvider
        if (mcWorld.worldProvider.getName().equals("Overworld"))
        {
            // Only replace the worldProvider if it's the overworld
            // Replacing other dimensions causes a lot of glitches
            mcWorld.worldProvider = new TCWorldProvider(this, this.world.worldProvider);
        }

        // Inject our own BiomeManager (called WorldChunkManager)
        Class<? extends BiomeGenerator> biomeModeClass = this.settings.worldConfig.biomeMode;
        biomeGenerator = TerrainControl.getBiomeModeManager().createCached(biomeModeClass, this);
        injectWorldChunkManager(biomeGenerator);

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
                    this.netherFortressGen = new NetherFortressGen();
                    this.oceanMonumentGen = new OceanMonumentGen(settings);
                case NotGenerate:
                case TerrainTest:
                    this.generator.onInitialize(this);
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

    private void injectWorldChunkManager(BiomeGenerator biomeGenerator)
    {
        if (biomeGenerator instanceof BukkitVanillaBiomeGenerator)
        {
            // Let our biome generator depend on Minecraft's
            ((BukkitVanillaBiomeGenerator) biomeGenerator).setWorldChunkManager(this.world.worldProvider.m());
        } else
        {
            // Let Minecraft's biome generator depend on ours
            ReflectionHelper.setFirstFieldOfType(this.world.worldProvider,
                    WorldChunkManager.class, new TCWorldChunkManager(this, biomeGenerator));
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

    @Override
    public BukkitBiome getCalculatedBiome(int x, int z)
    {
        return getBiomeById(this.biomeGenerator.getBiome(x, z));
    }

    @Override
    public LocalBiome getBiome(int x, int z)
    {
        if (this.settings.worldConfig.populateUsingSavedBiomes)
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
        return getBiomeById(world.getBiome(new BlockPosition(x, 0, z)).id);
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
        TileEntity tileEntity = world.getTileEntity(new BlockPosition(x, y, z));
        if (tileEntity != null)
        {
            tileEntity.a(nmsTag); // tileEntity.load
        } else
        {
            TerrainControl.log(LogMarker.DEBUG, "Skipping tile entity with id {}, cannot be placed at {},{},{} on id {}", new Object[] {
                    nmsTag.getString("id"), x, y, z, getMaterial(x, y, z)});
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
    public BiomeGenerator getBiomeGenerator() {
        return biomeGenerator;
    }

}
