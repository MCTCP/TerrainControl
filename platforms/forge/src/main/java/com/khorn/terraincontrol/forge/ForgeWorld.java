package com.khorn.terraincontrol.forge;


import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldSettings;
import com.khorn.terraincontrol.customobjects.CustomObjectStructureCache;
import com.khorn.terraincontrol.forge.generator.BiomeGenCustom;
import com.khorn.terraincontrol.forge.generator.ChunkProvider;
import com.khorn.terraincontrol.forge.generator.structure.*;
import com.khorn.terraincontrol.forge.util.NBTHelper;
import com.khorn.terraincontrol.generator.biome.BiomeGenerator;
import com.khorn.terraincontrol.generator.biome.OldBiomeGenerator;
import com.khorn.terraincontrol.generator.biome.OutputType;
import com.khorn.terraincontrol.util.NamedBinaryTag;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;
import com.khorn.terraincontrol.util.minecraftTypes.TreeType;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.feature.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;

public class ForgeWorld implements LocalWorld
{

    private ChunkProvider generator;
    private World world;
    private WorldSettings settings;
    private CustomObjectStructureCache structureCache;
    private String name;
    private long seed;
    private BiomeGenerator biomeManager;

    private static int nextBiomeId = 0;

    // >> This will likely change in 1.7
    private static final int maxBiomeCount = (Byte.MIN_VALUE * -2);
    private static ForgeBiome[] biomes = new ForgeBiome[maxBiomeCount];
    private static BiomeGenBase[] biomesToRestore = new BiomeGenBase[maxBiomeCount];

    private HashMap<String, LocalBiome> biomeNames = new HashMap<String, LocalBiome>();

    private static ArrayList<LocalBiome> defaultBiomes = new ArrayList<LocalBiome>();

    public StrongholdGen strongholdGen;
    public VillageGen villageGen;
    public MineshaftGen mineshaftGen;
    public RareBuildingGen rareBuildingGen;
    public NetherFortressGen netherFortressGen;

    private WorldGenDungeons dungeonGen;

    private WorldGenTrees tree;
    private WorldGenSavannaTree acaciaTree;
    private WorldGenBigTree bigTree;
    private WorldGenForest birchTree;
    private WorldGenTrees cocoaTree;
    private WorldGenCanopyTree darkOakTree;
    private WorldGenShrub groundBush;
    private WorldGenBigMushroom hugeMushroom;
    private WorldGenMegaPineTree hugeTaigaTree1;
    private WorldGenMegaPineTree hugeTaigaTree2;
    private WorldGenMegaJungle jungleTree;
    private WorldGenForest longBirchTree;
    private WorldGenSwamp swampTree;
    private WorldGenTaiga1 taigaTree1;
    private WorldGenTaiga2 taigaTree2;

    private boolean createNewChunks;
    private Chunk[] chunkCache;
    private Chunk cachedChunk;

    private int currentChunkX;
    private int currentChunkZ;

    private BiomeGenBase[] biomeGenBaseArray;
    private int[] biomeIntArray;

    public static void restoreBiomes()
    {
        BiomeGenBase[] biomeList = BiomeGenBase.func_150565_n();
        for (BiomeGenBase oldBiome : biomesToRestore)
        {
            if (oldBiome == null)
                continue;
            biomeList[oldBiome.biomeID] = oldBiome;
        }
        nextBiomeId = 0;
        defaultBiomes.clear();
    }

    public ForgeWorld(String _name)
    {
        this.name = _name;

        for (int i = 0; i < DefaultBiome.values().length; i++)
        {
            BiomeGenBase oldBiome = BiomeGenBase.func_150568_d(i);
            biomesToRestore[i] = oldBiome;
            BiomeGenCustom custom = new BiomeGenCustom(nextBiomeId++, oldBiome.biomeName);
            custom.CopyBiome(oldBiome);
            ForgeBiome biome = new ForgeBiome(custom);
            biomes[biome.getId()] = biome;
            defaultBiomes.add(biome);
            this.biomeNames.put(biome.getName(), biome);
        }
    }

    @Override
    public LocalBiome getNullBiome(String name)
    {
        return null;
    }

    @Override
    public LocalBiome AddCustomBiome(String name, int id)
    {
        ForgeBiome biome = new ForgeBiome(new BiomeGenCustom(id, name));
        biomes[biome.getId()] = biome;
        this.biomeNames.put(biome.getName(), biome);
        return biome;
    }

    @Override
    public LocalBiome AddVirtualBiome(String name, int id, int virtualId)
    {
        return null;
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
    public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int x_size, int z_size, OutputType outputType)
    {
        if (this.biomeManager != null)
            return this.biomeManager.getBiomesUnZoomed(biomeArray, x, z, x_size, z_size, outputType);

        biomeGenBaseArray = this.world.provider.worldChunkMgr.getBiomesForGeneration(biomeGenBaseArray, x, z, x_size, z_size);
        if (biomeArray == null || biomeArray.length < x_size * z_size)
            biomeArray = new int[x_size * z_size];
        for (int i = 0; i < x_size * z_size; i++)
            biomeArray[i] = biomeGenBaseArray[i].biomeID;
        return biomeArray;
    }

    @Override
    public int[] getBiomes(int[] biomeArray, int x, int z, int x_size, int z_size, OutputType outputType)
    {
        if (this.biomeManager != null)
            return this.biomeManager.getBiomes(biomeArray, x, z, x_size, z_size, outputType);

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
            return this.biomeManager.getBiome(x, z);
        return this.world.provider.worldChunkMgr.getBiomeGenAt(x, z).biomeID;
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
            this.strongholdGen.func_151539_a(null, this.world, chunkX, chunkZ, null);
        if (this.settings.worldConfig.mineshaftsEnabled)
            this.mineshaftGen.func_151539_a(null, this.world, chunkX, chunkZ, null);
        if (this.settings.worldConfig.villagesEnabled && dry)
            this.villageGen.func_151539_a(null, this.world, chunkX, chunkZ, null);
        if (this.settings.worldConfig.rareBuildingsEnabled)
            this.rareBuildingGen.func_151539_a(null, this.world, chunkX, chunkZ, null);
        if (this.settings.worldConfig.netherFortressesEnabled)
            this.netherFortressGen.func_151539_a(null, this.world, chunkX, chunkZ, null);
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
            case Birch:
                return birchTree.generate(this.world, rand, x, y, z);
            case TallBirch:
                return longBirchTree.generate(this.world, rand, x, y, z);
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
            case Acacia:
                return acaciaTree.generate(this.world, rand, x, y, z);
            case DarkOak:
                return darkOakTree.generate(this.world, rand, x, y, z);
            case HugeTaiga1:
                return hugeTaigaTree1.generate(this.world, rand, x, y, z);
            case HugeTaiga2:
                return hugeTaigaTree2.generate(this.world, rand, x, y, z);
            default:
                throw new AssertionError("Failed to handle tree of type " + type.toString());
        }
    }

    @Override
    public boolean placeDefaultStructures(Random rand, int chunkX, int chunkZ)
    {
        boolean isVillagePlaced = false;
        if (this.settings.worldConfig.strongholdsEnabled)
            this.strongholdGen.generateStructuresInChunk(this.world, rand, chunkX, chunkZ);
        if (this.settings.worldConfig.mineshaftsEnabled)
            this.mineshaftGen.generateStructuresInChunk(this.world, rand, chunkX, chunkZ);
        if (this.settings.worldConfig.villagesEnabled)
            isVillagePlaced = this.villageGen.generateStructuresInChunk(this.world, rand, chunkX, chunkZ);
        if (this.settings.worldConfig.rareBuildingsEnabled)
            this.rareBuildingGen.generateStructuresInChunk(this.world, rand, chunkX, chunkZ);
        if (this.settings.worldConfig.netherFortressesEnabled)
            this.netherFortressGen.generateStructuresInChunk(this.world, rand, chunkX, chunkZ);

        return isVillagePlaced;
    }

    @Override
    public void replaceBlocks()
    {
        if (this.settings.worldConfig.BiomeConfigsHaveReplacement)
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
                        BiomeConfig biomeConfig = this.settings.biomeConfigs[ChunkBiomes[(sectionZ << 4) | sectionX] & 0xFF];

                        if (biomeConfig.ReplaceCount > 0)
                        {
                            for (int sectionY = 0; sectionY < 16; sectionY++)
                            {
                                Block block = section.func_150819_a(sectionX, sectionY, sectionZ);
                                int blockId = Block.func_149682_b(block);
                                if (biomeConfig.replaceMatrixBlocks[blockId] == null)
                                    continue;

                                int replaceTo = biomeConfig.replaceMatrixBlocks[blockId][section.getYLocation() + sectionY];
                                if (replaceTo == -1)
                                    continue;

                                section.func_150818_a(sectionX, sectionY, sectionZ, Block.func_149729_e(replaceTo >> 4));
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
    public void replaceBiomes()
    {
        if (this.settings.worldConfig.HaveBiomeReplace)
        {
            byte[] ChunkBiomes = this.chunkCache[0].getBiomeArray();

            for (int i = 0; i < ChunkBiomes.length; i++)
                ChunkBiomes[i] = (byte) (this.settings.ReplaceBiomesMatrix[ChunkBiomes[i] & 0xFF] & 0xFF);
        }

    }

    @Override
    public void placePopulationMobs(BiomeConfig config, Random random, int chunkX, int chunkZ)
    {
        SpawnerAnimals.performWorldGenSpawning(this.getWorld(), ((ForgeBiome) config.Biome).getHandle(), chunkX * 16 + 8, chunkZ * 16 + 8, 16, 16, random);
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
        if (y < TerrainControl.worldDepth || y >= TerrainControl.worldHeight)
            return null;

        x >>= 4;
        z >>= 4;
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

        Block block = chunk.func_150810_a(x, y, z);
        return Block.func_149682_b(block);
    }

    @Override
    public byte getTypeData(int x, int y, int z)
    {
        Chunk chunk = this.getChunk(x, y, z);
        if (chunk == null)
            return 0;

        z &= 0xF;
        x &= 0xF;

        return (byte) chunk.getBlockMetadata(x, y, z);
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

        // Get chunk from (faster) custom cache
        Chunk chunk = this.getChunk(x, y, z);

        if (chunk == null)
        {
            // Chunk is unloaded
            return;
        }

        // Get old block id (only needed for physics)
        Block oldBlock = Blocks.air;
        if (applyPhysics)
        {
            // oldBlock = chunk.getBlock(...)
            oldBlock = chunk.func_150810_a(x & 15, y, z & 15);
        }

        // Place block
        if (applyPhysics)
        {
            // chunk.setBlockAndMetadata(..., Block.getBlock(typeId), ..)
            chunk.func_150807_a(x & 15, y, z & 15, Block.func_149729_e(typeId), data);
        } else
        {
            // Temporarily make remote, so that torches etc. don't pop off
            boolean oldStatic = world.isRemote;
            world.isRemote = true;
            // chunk.setBlockAndMetadata(..., Block.getBlock(typeId), ..)
            chunk.func_150807_a(x & 15, y, z & 15, Block.func_149729_e(typeId), data);
            world.isRemote = oldStatic;
        }

        // Relight and update
        if (updateLight)
        {
            world.updateAllLightTypes(x, y, z);
        }

        if (notifyPlayers && !world.isRemote)
        {
            world.markBlockForUpdate(x, y, z);
        }

        if (!world.isRemote && applyPhysics)
        {
            world.notifyBlockChange(x, y, z, oldBlock);
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
            return -1;
        z &= 0xF;
        x &= 0xF;
        int y = chunk.getHeightValue(x, z);
<<<<<<< HEAD
        while (chunk.getBlockID(x, y, z) != DefaultMaterial.AIR.id && y <= 256)
=======
        int maxSearchY = y + 5; // Don't search too far away
        // while(chunk.getBlock(...) != ...)
        while (chunk.func_150810_a(x, y, z) != Blocks.air && y <= maxSearchY)
>>>>>>> origin/master
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
        // Actually, this calculates the block and skylight as it were day.
        return world.getFullBlockLightValue(x, y, z);
    }

    @Override
    public boolean isLoaded(int x, int y, int z)
    {
        return getChunk(x, y, z) != null;
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
        return this.seed;
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

    public ChunkProvider getChunkGenerator()
    {
        return this.generator;
    }

    public void InitM(World world, WorldSettings config)
    {
        this.settings = config;
        this.world = world;
        this.seed = world.getSeed();
        for (ForgeBiome biome : biomes)
        {
            // Apply settings for biomes
            if (biome != null && config.biomeConfigs[biome.getId()] != null)
            {
                biome.setEffects(config.biomeConfigs[biome.getId()]);
            }
        }
    }

    public void Init(World world, WorldSettings configs)
    {
        this.settings = configs;

        this.world = world;
        this.seed = world.getSeed();
        this.structureCache = new CustomObjectStructureCache(this);

        this.dungeonGen = new WorldGenDungeons();
        this.strongholdGen = new StrongholdGen(configs);

        this.villageGen = new VillageGen(configs);
        this.mineshaftGen = new MineshaftGen();
        this.rareBuildingGen = new RareBuildingGen(configs);
        this.netherFortressGen = new NetherFortressGen();

        this.tree = new WorldGenTrees(false);
        this.acaciaTree = new WorldGenSavannaTree(false);
        this.cocoaTree = new WorldGenTrees(false, 5, 3, 3, true);
        this.bigTree = new WorldGenBigTree(false);
        this.birchTree = new WorldGenForest(false, false);
        this.darkOakTree = new WorldGenCanopyTree(false);
        this.longBirchTree = new WorldGenForest(false, true);
        this.swampTree = new WorldGenSwamp();
        this.taigaTree1 = new WorldGenTaiga1();
        this.taigaTree2 = new WorldGenTaiga2(false);
        this.hugeMushroom = new WorldGenBigMushroom();
        this.hugeTaigaTree1 = new WorldGenMegaPineTree(false, false);
        this.hugeTaigaTree2 = new WorldGenMegaPineTree(false, true);
        this.jungleTree = new WorldGenMegaJungle(false, 10, 20, 3, 3);
        this.groundBush = new WorldGenShrub(3, 0);

        this.chunkCache = new Chunk[4];
        this.generator = new ChunkProvider(this);
    }

    public void setBiomeManager(BiomeGenerator manager)
    {
        this.biomeManager = manager;
    }

    public World getWorld()
    {
        return this.world;
    }

    public void FillChunkForBiomes(Chunk chunk, int x, int z)
    {

        byte[] arrayOfByte2 = chunk.getBiomeArray();
        biomeIntArray = this.getBiomes(biomeIntArray, x * 16, z * 16, 16, 16, OutputType.DEFAULT_FOR_WORLD);

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
    public int getBiomeId(int x, int z)
    {
        return world.getBiomeGenForCoords(x, z).biomeID;
    }

    @Override
    public LocalBiome getBiome(int x, int z)
    {
        return getBiomeById(world.getBiomeGenForCoords(x, z).biomeID);
    }

    @Override
    public void attachMetadata(int x, int y, int z, NamedBinaryTag tag)
    {
        // Convert Tag to a native nms tag
        NBTTagCompound nmsTag = NBTHelper.getNMSFromNBTTagCompound(tag);
        // Add the x, y and z position to it
        nmsTag.setInteger("x", x);
        nmsTag.setInteger("y", y);
        nmsTag.setInteger("z", z);
        // Add that data to the current tile entity in the world
        // TileEntity tileEntity = world.getTileEntity(x, y, z);
        TileEntity tileEntity = world.func_147438_o(x, y, z);
        if (tileEntity != null)
        {
            tileEntity.readFromNBT(nmsTag);
        } else
        {
            TerrainControl.log(Level.CONFIG, "Skipping tile entity with id {0}, cannot be placed at {1},{2},{3} on id {4}", new Object[] {nmsTag.getString("id"), x, y, z, world.getBlockId(x, y, z)});
        }
    }

    @Override
    public NamedBinaryTag getMetadata(int x, int y, int z)
    {
        // TileEntity tileEntity = world.getTileEntity(x, y, z);
        TileEntity tileEntity = world.func_147438_o(x, y, z);
        if (tileEntity == null)
        {
            return null;
        }
        NBTTagCompound nmsTag = new NBTTagCompound();
        tileEntity.writeToNBT(nmsTag);
        nmsTag.removeTag("x");
        nmsTag.removeTag("y");
        nmsTag.removeTag("z");
        return NBTHelper.getNBTFromNMSTagCompound(nmsTag);
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
