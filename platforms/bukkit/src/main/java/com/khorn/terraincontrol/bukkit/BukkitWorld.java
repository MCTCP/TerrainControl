package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.*;
import com.khorn.terraincontrol.bukkit.generator.BiomeCacheWrapper;
import com.khorn.terraincontrol.bukkit.generator.TCChunkGenerator;
import com.khorn.terraincontrol.bukkit.generator.TCWorldChunkManager;
import com.khorn.terraincontrol.bukkit.generator.TCWorldProvider;
import com.khorn.terraincontrol.bukkit.generator.structures.*;
import com.khorn.terraincontrol.bukkit.util.NBTHelper;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.BiomeLoadInstruction;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.configuration.WorldSettings;
import com.khorn.terraincontrol.customobjects.CustomObjectStructureCache;
import com.khorn.terraincontrol.generator.biome.BiomeGenerator;
import com.khorn.terraincontrol.generator.biome.OldBiomeGenerator;
import com.khorn.terraincontrol.generator.biome.OutputType;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.NamedBinaryTag;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;
import com.khorn.terraincontrol.util.minecraftTypes.TreeType;
import net.minecraft.server.v1_7_R1.*;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;

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
    private BiomeGenerator biomeManager;

    private static int nextBiomeId = DefaultBiome.values().length;

    private static final int MAX_BIOMES_COUNT = 1024;
    private static final int MAX_SAVED_BIOMES_COUNT = 256;
    private static final int STANDARD_WORLD_HEIGHT = 128;

    private final Map<String, LocalBiome> biomeNames = new HashMap<String, LocalBiome>();

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

    private Chunk[] chunkCache;

    private BiomeBase[] biomeBaseArray;

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
    public BukkitBiome getBiomeById(int id)
    {
        return (BukkitBiome) settings.biomes[id];
    }

    @Override
    public LocalBiome getBiomeByName(String name)
    {
        return this.biomeNames.get(name);
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
    public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int x_size, int z_size, OutputType outputType)
    {
        if (this.biomeManager != null)
            return this.biomeManager.getBiomesUnZoomed(biomeArray, x, z, x_size, z_size, outputType);

        // For BiomeMode:Default
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

        // For BiomeMode:Default
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

        // For BiomeMode:Default
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
    public boolean placeDefaultStructures(Random random, ChunkCoordinate chunkCoord)
    {
        int chunkX = chunkCoord.getChunkX();
        int chunkZ = chunkCoord.getChunkZ();

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
        int worldStartX = rawChunk.locX * 16;
        int worldStartZ = rawChunk.locZ * 16;

        ChunkSection[] sectionsArray = rawChunk.i();

        for (ChunkSection section : sectionsArray)
        {
            if (section == null)
                continue;

            for (int sectionX = startXInChunk; sectionX < endXInChunk; sectionX++)
            {
                for (int sectionZ = startZInChunk; sectionZ < endZInChunk; sectionZ++)
                {
                    LocalBiome biome = this.getCalculatedBiome(worldStartX + sectionX, worldStartZ + sectionZ);
                    if (biome != null && biome.getBiomeConfig().replacedBlocks.hasReplaceSettings())
                    {
                        LocalMaterialData[][] replaceArray = biome.getBiomeConfig().replacedBlocks.compiledInstructions;
                        for (int sectionY = 0; sectionY < 16; sectionY++)
                        {
                            Block block = section.getTypeId(sectionX, sectionY, sectionZ);
                            int blockId = Block.b(block);
                            if (replaceArray[blockId] == null)
                                continue;

                            int y = section.getYPosition() + sectionY;
                            if (y >= replaceArray[blockId].length)
                                break;

                            BukkitMaterialData replaceTo = (BukkitMaterialData) replaceArray[blockId][y];
                            if (replaceTo == null || replaceTo.getBlockId() == blockId)
                                continue;

                            section.setTypeId(sectionX, sectionY, sectionZ, replaceTo.internalBlock());
                            section.setData(sectionX, sectionY, sectionZ, replaceTo.getBlockData());
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
            if (world.chunkProvider.isChunkLoaded(chunkX, chunkZ))
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
        return chunk.getType(x & 0xF, y, z & 0xF).getMaterial().equals(Material.AIR);
    }

    @Override
    public LocalMaterialData getMaterial(int x, int y, int z)
    {
        Chunk chunk = this.getChunk(x, y, z);
        if (chunk == null)
        {
            return new BukkitMaterialData(DefaultMaterial.AIR, 0);
        }

        z &= 0xF;
        x &= 0xF;

        return new BukkitMaterialData(chunk.getType(x, y, z), chunk.getData(x, y, z));
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

        Block block = ((BukkitMaterialData) material).internalBlock();

        // Get chunk from (faster) custom cache
        Chunk chunk = this.getChunk(x, y, z);

        if (chunk == null)
        {
            // Chunk is unloaded
            return;
        }

        // Temporarily make static, so that torches etc. don't pop off
        boolean oldStatic = world.isStatic;
        world.isStatic = true;
        chunk.a(x & 15, y, z & 15, block, material.getBlockData());
        world.isStatic = oldStatic;

        // Relight and update players
        world.A(x, y, z);
        if (!world.isStatic)
        {
            world.notify(x, y, z);
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
    public void startPopulation(ChunkCoordinate chunkCoord)
    {
        if (this.chunkCache != null)
        {
            throw new IllegalStateException("Chunk is already being populated");
        }

        // Initialize cache
        this.chunkCache = new Chunk[4];
        for (int indexX = 0; indexX <= 1; indexX++)
        {
            for (int indexZ = 0; indexZ <= 1; indexZ++)
            {
                this.chunkCache[indexX | (indexZ << 1)] = world.getChunkAt(chunkCoord.getChunkX() + indexX, chunkCoord.getChunkZ() + indexZ);
            }
        }
    }

    @Override
    public void endPopulation()
    {
        if (this.chunkCache == null)
        {
            throw new IllegalStateException("Population has already ended");
        }
        this.chunkCache = null;
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
        if (biomeModeClass != TerrainControl.getBiomeModeManager().VANILLA)
        {
            TCWorldChunkManager worldChunkManager = new TCWorldChunkManager(this);
            mcWorld.worldProvider.e = worldChunkManager;

            BiomeGenerator biomeManager = TerrainControl.getBiomeModeManager().create(biomeModeClass, this,
                    new BiomeCacheWrapper(worldChunkManager));
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
    public BukkitBiome getCalculatedBiome(int x, int z)
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
            TerrainControl.log(LogMarker.DEBUG, "Skipping tile entity with id {}, cannot be placed at {},{},{} on id {}", new Object[] {
                    nmsTag.getString("id"), x, y, z, getMaterial(x, y, z)});
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
