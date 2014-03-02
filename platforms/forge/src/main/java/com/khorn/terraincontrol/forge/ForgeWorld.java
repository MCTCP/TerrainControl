package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.LocalMaterialData;

import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import com.khorn.terraincontrol.*;
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
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.NamedBinaryTag;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;
import com.khorn.terraincontrol.util.minecraftTypes.TreeType;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.feature.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

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
        BiomeGenBase[] biomeList = BiomeGenBase.getBiomeGenArray();
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

        // Save all original vanilla biomes, so that they can be restored
        // later on
        for (DefaultBiome defaultBiome : DefaultBiome.values())
        {
            int biomeId = defaultBiome.Id;
            BiomeGenBase oldBiome = BiomeGenBase.getBiome(biomeId);
            biomesToRestore[biomeId] = oldBiome;
            BiomeGenCustom custom = new BiomeGenCustom(new BiomeIds(biomeId), oldBiome.biomeName);
            nextBiomeId++;
            custom.CopyBiome(oldBiome);
            ForgeBiome biome = new ForgeBiome(custom);
            biomes[biome.getIds().getGenerationId()] = biome;
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
    public LocalBiome addCustomBiome(String name, BiomeIds id)
    {
        ForgeBiome biome = new ForgeBiome(new BiomeGenCustom(id, name));
        biomes[biome.getIds().getGenerationId()] = biome;
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

        ExtendedBlockStorage[] sectionsArray = rawChunk.getBlockStorageArray();

        byte[] chunkBiomes = rawChunk.getBiomeArray();

        for (ExtendedBlockStorage section : sectionsArray)
        {
            if (section == null)
                continue;

            for (int sectionX = startXInChunk; sectionX < endXInChunk; sectionX++)
            {
                for (int sectionZ = startZInChunk; sectionZ < endZInChunk; sectionZ++)
                {
                    BiomeConfig biomeConfig = this.settings.biomeConfigs[chunkBiomes[(sectionZ << 4) | sectionX] & 0xFF];
                    if (biomeConfig != null && biomeConfig.replacedBlocks.hasReplaceSettings())
                    {
                        LocalMaterialData[][] replaceArray = biomeConfig.replacedBlocks.compiledInstructions;
                        for (int sectionY = 0; sectionY < 16; sectionY++)
                        {
                            Block block = section.getBlockByExtId(sectionX, sectionY, sectionZ);
                            int blockId = Block.getIdFromBlock(block);
                            if (replaceArray[blockId] == null)
                                continue;

                            int y = section.getYLocation() + sectionY;
                            if (y >= replaceArray[blockId].length)
                                break;

                            ForgeMaterialData replaceTo = (ForgeMaterialData) replaceArray[blockId][y];
                            if (replaceTo == null || replaceTo.getBlockId() == blockId)
                                continue;

                            // section.setBlock(....)
                            section.func_150818_a(sectionX, sectionY, sectionZ, replaceTo.internalBlock());
                            section.setExtBlockMetadata(sectionX, sectionY, sectionZ, replaceTo.getBlockData());
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
            replaceBiomes(this.chunkCache[0].getBiomeArray(), 8, 8);
            replaceBiomes(this.chunkCache[1].getBiomeArray(), 0, 8);
            replaceBiomes(this.chunkCache[2].getBiomeArray(), 8, 0);
            replaceBiomes(this.chunkCache[3].getBiomeArray(), 0, 0);
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
                biomeArray[zInChunkTimes16 | xInChunk] = (byte) (this.settings.replaceToBiomeNameMatrix[biomeArray[zInChunkTimes16
                        | xInChunk] & 0xFF] & 0xFF);
            }
        }
    }

    @Override
    public void placePopulationMobs(BiomeConfig config, Random random, int chunkX, int chunkZ)
    {
        SpawnerAnimals.performWorldGenSpawning(this.getWorld(), ((ForgeBiome) config.Biome).getHandle(), chunkX * 16 + 8, chunkZ * 16 + 8,
                16, 16, random);
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
        if (y < TerrainControl.WORLD_DEPTH || y >= TerrainControl.WORLD_HEIGHT)
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
        return chunk.getBlock(x & 0xF, y, z & 0xF).getMaterial().equals(Material.air);
    }

    @Override
    public LocalMaterialData getMaterial(int x, int y, int z)
    {
        Chunk chunk = this.getChunk(x, y, z);
        if (chunk == null)
        {
            return new ForgeMaterialData(Blocks.air, 0);
        }

        z &= 0xF;
        x &= 0xF;

        return new ForgeMaterialData(chunk.getBlock(x, y, z), chunk.getBlockMetadata(x, y, z));
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

        // Get chunk from (faster) custom cache
        Chunk chunk = this.getChunk(x, y, z);

        if (chunk == null)
        {
            // Chunk is unloaded
            return;
        }

        // Temporarily make remote, so that torches etc. don't pop off
        boolean oldStatic = world.isRemote;
        world.isRemote = true;
        // chunk.setBlockAndMetadata(...)
        chunk.func_150807_a(x & 15, y, z & 15, ((ForgeMaterialData) material).internalBlock(), material.getBlockData());
        world.isRemote = oldStatic;

        // Relight and update players
        world.func_147451_t(x, y, z); // world.updateAllLightTypes
        if (!world.isRemote)
        {
            world.markBlockForUpdate(x, y, z);
        }
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
        int maxSearchY = y + 5; // Don't search too far away
        // while(chunk.getBlock(...) != ...)
        while (chunk.getBlock(x, y, z) != Blocks.air && y <= maxSearchY)
        {
            // Fix for incorrect lightmap
            y += 1;
        }
        return y;
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
            if (biome != null && config.biomeConfigs[biome.getIds().getGenerationId()] != null)
            {
                biome.setEffects(config.biomeConfigs[biome.getIds().getGenerationId()]);
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
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity != null)
        {
            tileEntity.readFromNBT(nmsTag);
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
        tileEntity.writeToNBT(nmsTag);
        nmsTag.removeTag("x");
        nmsTag.removeTag("y");
        nmsTag.removeTag("z");
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
