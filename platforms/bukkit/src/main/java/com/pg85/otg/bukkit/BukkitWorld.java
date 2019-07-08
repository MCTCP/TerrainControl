package com.pg85.otg.bukkit;

import com.pg85.otg.*;
import com.pg85.otg.bukkit.generator.BukkitVanillaBiomeGenerator;
import com.pg85.otg.bukkit.generator.OTGChunkGenerator;
import com.pg85.otg.bukkit.generator.OTGInternalChunkGenerator;
import com.pg85.otg.bukkit.generator.OTGWorldChunkManager;
import com.pg85.otg.bukkit.generator.OTGWorldProvider;
import com.pg85.otg.bukkit.generator.structures.*;
import com.pg85.otg.bukkit.util.NBTHelper;
import com.pg85.otg.bukkit.util.WorldHelper;
import com.pg85.otg.common.BiomeIds;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.common.WorldSession;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.biome.BiomeLoadInstruction;
import com.pg85.otg.configuration.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.customobjects.SpawnableObject;
import com.pg85.otg.customobjects.bofunctions.EntityFunction;
import com.pg85.otg.customobjects.structures.CustomStructureCache;
import com.pg85.otg.exception.BiomeNotFoundException;
import com.pg85.otg.generator.ObjectSpawner;
import com.pg85.otg.generator.biome.BiomeGenerator;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.network.ConfigProvider;
import com.pg85.otg.network.ServerConfigProvider;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.OTGBlock;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.helpers.ReflectionHelper;
import com.pg85.otg.util.minecraft.defaults.DefaultBiome;
import com.pg85.otg.util.minecraft.defaults.TreeType;

import net.minecraft.server.v1_12_R1.BiomeBase;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockLeaves;
import net.minecraft.server.v1_12_R1.BlockLeaves1;
import net.minecraft.server.v1_12_R1.BlockLog1;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.BlockWood;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_12_R1.ChunkGenerator;
import net.minecraft.server.v1_12_R1.ChunkProviderServer;
import net.minecraft.server.v1_12_R1.ChunkSection;
import net.minecraft.server.v1_12_R1.DataConverter;
import net.minecraft.server.v1_12_R1.DataConverterRegistry;
import net.minecraft.server.v1_12_R1.DataConverterTypes;
import net.minecraft.server.v1_12_R1.DefinedStructure;
import net.minecraft.server.v1_12_R1.DefinedStructureManager;
import net.minecraft.server.v1_12_R1.DimensionManager;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityFallingBlock;
import net.minecraft.server.v1_12_R1.EntityLightning;
import net.minecraft.server.v1_12_R1.EntityOcelot;
import net.minecraft.server.v1_12_R1.EntityTippedArrow;
import net.minecraft.server.v1_12_R1.EnumCreatureType;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.ITileEntity;
import net.minecraft.server.v1_12_R1.Material;
import net.minecraft.server.v1_12_R1.MinecraftKey;
import net.minecraft.server.v1_12_R1.NBTBase;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.SpawnerCreature;
import net.minecraft.server.v1_12_R1.TileEntity;
import net.minecraft.server.v1_12_R1.World;
import net.minecraft.server.v1_12_R1.WorldChunkManager;
import net.minecraft.server.v1_12_R1.WorldGenAcaciaTree;
import net.minecraft.server.v1_12_R1.WorldGenBigTree;
import net.minecraft.server.v1_12_R1.WorldGenDungeons;
import net.minecraft.server.v1_12_R1.WorldGenForest;
import net.minecraft.server.v1_12_R1.WorldGenForestTree;
import net.minecraft.server.v1_12_R1.WorldGenFossils;
import net.minecraft.server.v1_12_R1.WorldGenGroundBush;
import net.minecraft.server.v1_12_R1.WorldGenHugeMushroom;
import net.minecraft.server.v1_12_R1.WorldGenJungleTree;
import net.minecraft.server.v1_12_R1.WorldGenMegaTree;
import net.minecraft.server.v1_12_R1.WorldGenSwampTree;
import net.minecraft.server.v1_12_R1.WorldGenTaiga1;
import net.minecraft.server.v1_12_R1.WorldGenTaiga2;
import net.minecraft.server.v1_12_R1.WorldGenTrees;
import net.minecraft.server.v1_12_R1.WorldServer;
import net.minecraft.server.v1_12_R1.EntityLiving;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftAmbient;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftComplexLivingEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftElderGuardian;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftFlying;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftGolem;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftGuardian;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftMonster;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftSlime;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftWaterMob;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.generator.CustomChunkGenerator;

import java.io.File;
import java.util.*;

public class BukkitWorld implements LocalWorld
{
    private static final int MAX_BIOMES_COUNT = 4096;
    private static final int MAX_SAVED_BIOMES_COUNT = 256;
    private static final int STANDARD_WORLD_HEIGHT = 128;
    
    // Initially false, set to true when enabled once
    private boolean initialized;

    private OTGChunkGenerator generator;
    private WorldServer world;
    private ServerConfigProvider settings;
    private CustomStructureCache structureCache;
    private String name;
    private BiomeGenerator biomeGenerator;
    private DataConverter dataConverter;

    private final Map<String, LocalBiome> biomeNames = new HashMap<String, LocalBiome>();

    public OTGStrongholdGen strongholdGen;
    public OTGVillageGen villageGen;
    public OTGMineshaftGen mineshaftGen;
    public OTGRareBuildingGen rareBuildingGen;
    public OTGNetherFortressGen netherFortressGen;
    public OTGOceanMonumentGen oceanMonumentGen;
    public OTGMansionGen mansionGen;

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
	private BukkitWorldSession worldSession;

    BukkitWorld(String _name)
    {
        this.name = _name;
        this.worldSession = new BukkitWorldSession(this);
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

    public World getWorld()
    {
        return this.world;
    }
    
    @Override
    public ConfigProvider getConfigs()
    {
        return this.settings;
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
    
    public OTGChunkGenerator getChunkGenerator()
    {
        return this.generator;
    }
    
    public void setChunkGenerator(OTGChunkGenerator _generator)
    {
        this.generator = _generator;
    }
		
    @Override
    public CustomStructureCache getStructureCache()
    {
        return this.structureCache;
    }

    @Override
    public BiomeGenerator getBiomeGenerator() {
        return biomeGenerator;
    }
	
	@Override
	public ObjectSpawner getObjectSpawner()
	{
		return this.generator.getObjectSpawner();
	}

    @Override
	public WorldSession getWorldSession()
	{
    	// TODO Implement this properly (for particles)
		return worldSession;
	}
    	
	@Override
	public String getWorldSettingsName()
	{
		// TODO: Make sure this returns the correct name
		return this.getWorld().getWorldData().getName();
	}

	@Override
	public File getWorldSaveDir()
	{
		// TODO: Make sure this returns the correct directory
		return this.getWorld().getDataManager().getDirectory();
	}

	@Override
	public int getDimensionId()
	{
		return this.getWorld().worldProvider.getDimensionManager().getDimensionID();
	}
	
	@Override
	public void deleteWorldSessionData()
	{
		// TODO Implement this (for spawners and particles)
		throw new RuntimeException();
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
    
    // World

    /**
     * Enables/reloads this BukkitWorld. If you are reloading, don't forget to
     * set the new settings first using {@link #setSettings(ServerConfigProvider)}.
     *
     * @param world The world that needs to be enabled.
     */
    void enable(org.bukkit.World world)
    {
        WorldServer mcWorld = ((CraftWorld) world).getHandle();

        // Do the things that always need to happen, whether we are enabling
        // for the first time or reloading
        this.world = mcWorld;

        // Inject our own WorldProvider
        if (mcWorld.worldProvider.getDimensionManager().equals(DimensionManager.OVERWORLD))
        {
            // Only replace the worldProvider if it's the overworld
            // Replacing other dimensions causes a lot of glitches
            mcWorld.worldProvider = new OTGWorldProvider(this, this.world.worldProvider);
        }

        // Inject our own BiomeManager (called WorldChunkManager)
        Class<? extends BiomeGenerator> biomeModeClass = this.settings.getWorldConfig().biomeMode;
        biomeGenerator = OTG.getBiomeModeManager().createCached(biomeModeClass, this);
        injectWorldChunkManager(biomeGenerator);

        // Set sea level
        mcWorld.b(this.settings.getWorldConfig().waterLevelMax);

        if (!initialized)
        {
            // Things that need to be done only when enabling
            // for the first time
            this.structureCache = new CustomStructureCache(this);
            this.dataConverter = DataConverterRegistry.a();

            switch (this.settings.getWorldConfig().modeTerrain)
            {
                case Normal:
                case OldGenerator:
                    this.strongholdGen = new OTGStrongholdGen(settings);
                    this.villageGen = new OTGVillageGen(settings);
                    this.mineshaftGen = new OTGMineshaftGen();
                    this.rareBuildingGen = new OTGRareBuildingGen(settings);
                    this.mansionGen = new OTGMansionGen(settings);
                    this.netherFortressGen = new OTGNetherFortressGen();
                    this.oceanMonumentGen = new OTGOceanMonumentGen(settings);

                    // Inject our own ChunkGenerator
                    injectInternalChunkGenerator(new OTGInternalChunkGenerator(this, generator));
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
    
    /**
     * Cleans up references of itself in Minecraft's native code.
     */
    void disable()
    {
        // Restore old world provider if replaced
        if (world.worldProvider instanceof OTGWorldProvider)
        {
            world.worldProvider = ((OTGWorldProvider) world.worldProvider).getOldWorldProvider();
        }

        // Restore vanilla chunk generator
        this.injectInternalChunkGenerator(new CustomChunkGenerator(world, getSeed(), generator));
    }
    
    private void injectWorldChunkManager(BiomeGenerator biomeGenerator)
    {
        if (biomeGenerator instanceof BukkitVanillaBiomeGenerator)
        {
            // Let our biome generator depend on Minecraft's
            ((BukkitVanillaBiomeGenerator) biomeGenerator).setWorldChunkManager(this.world.worldProvider.k());
        } else {
            // Let Minecraft's biome generator depend on ours
            ReflectionHelper.setValueInFieldOfType(this.world.worldProvider,
                    WorldChunkManager.class, new OTGWorldChunkManager(this, biomeGenerator));
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

    @Override
    public void endPopulation()
    {
        if (this.chunkCache == null && settings.getWorldConfig().populationBoundsCheck)
        {
            throw new IllegalStateException("Chunk is not being populated."
                    + " This may be a bug in Open Terrain Generator, but it may also be"
                    + " another mod that is poking in unloaded chunks. Set"
                    + " PopulationBoundsCheck to false in the WorldConfig to"
                    + " disable this error.");
        }
        this.chunkCache = null;
    }
    
    // Biomes
    
    @Override
    public LocalBiome createBiomeFor(BiomeConfig biomeConfig, BiomeIds biomeIds, ConfigProvider configProvider)
    {
    	return createBiomeFor(biomeConfig, biomeIds);
	}
    
	@Override
	public int getRegisteredBiomeId(String resourceLocationString)
	{
		if(resourceLocationString != null && !resourceLocationString.trim().isEmpty())
		{
			String[] resourceLocationStringArr = resourceLocationString.split(":");
			if(resourceLocationStringArr.length == 1) // When querying for biome name without domain search the local world's biomes 
			{
				MinecraftKey resourceLocation = new MinecraftKey(PluginStandardValues.MOD_ID.toLowerCase(), this.getName() + "_" + resourceLocationStringArr[0].replaceAll(" ", "_"));
				BiomeBase biome = BiomeBase.REGISTRY_ID.get(resourceLocation);
				return WorldHelper.getSavedId(biome);
			}
			if(resourceLocationStringArr.length == 2)
			{
				MinecraftKey resourceLocation = new MinecraftKey(resourceLocationStringArr[0],resourceLocationStringArr[1]);
				BiomeBase biome = BiomeBase.REGISTRY_ID.get(resourceLocation);
				return WorldHelper.getSavedId(biome);
			}
		}
		return -1;
	}
    
    @Override
    public BukkitBiome getCalculatedBiome(int x, int z)
    {
        return (BukkitBiome)getBiomeByOTGIdOrNull(this.biomeGenerator.getBiome(x, z));
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
    	BiomeBase biome = world.getBiome(new BlockPosition(x, 0, z));        		
    	int biomeId = BiomeBase.a(biome);
    	return this.settings.getBiomeBySavedIdOrNull(biomeId);
    }
    
    private LocalBiome createBiomeFor(BiomeConfig biomeConfig, BiomeIds biomeIds)
    {
        BukkitBiome biome = BukkitBiome.forCustomBiome(biomeConfig, biomeIds, this.getName());
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
    public ArrayList<LocalBiome> getAllBiomes()
    {
    	ArrayList<LocalBiome> biomes = new ArrayList<LocalBiome>();
		for(LocalBiome biome : this.settings.getBiomeArrayByOTGId())
		{
			biomes.add(biome);
		}
    	return biomes;
    }

    @Override
    public LocalBiome getBiomeByOTGIdOrNull(int id)
    {
        return settings.getBiomeByOTGIdOrNull(id);
    }
    
	@Override
	public LocalBiome getFirstBiomeOrNull() {
		return biomeNames.size() > 0 ? (LocalBiome)biomeNames.values().toArray()[0] : null;
	}

    @Override
    public LocalBiome getBiomeByNameOrNull(String name)
    {
        return biomeNames.get(name);
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

    // Structures / trees
    
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
        if (worldConfig.woodLandMansionsEnabled)
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
	public boolean chunkHasDefaultStructure(Random random, ChunkCoordinate chunk) {
		// TODO Auto-generated method stub
		return false;
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
        if (worldConfig.woodLandMansionsEnabled)
        	this.mansionGen.a(this.world, random, chunkIntPair);

        return villageGenerated;
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
    
    // Replace blocks

    @Override
    public void replaceBlocks(ChunkCoordinate chunkCoord)
    {
        if (!this.settings.getWorldConfig().biomeConfigsHaveReplacement)
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

    // Mob / entity spawning
    
    @Override
    public void placePopulationMobs(LocalBiome biome, Random random, ChunkCoordinate chunkCoord)
    {
        SpawnerCreature.a(this.world, ((BukkitBiome) biome).getHandle(), chunkCoord.getChunkX() * 16 + 8, chunkCoord.getChunkZ() * 16 + 8, 16, 16, random);
    }

	private Entity getEntity(Class<? extends org.bukkit.entity.Entity> clazz)
	{
		// TODO: Clean up and optimise this

		double x = 0;
		double y = 0;
		double z = 0;

		if (org.bukkit.entity.Boat.class.isAssignableFrom(clazz))
		{
			return new net.minecraft.server.v1_12_R1.EntityBoat(world, x, y, z);
		}
		else if (FallingBlock.class.isAssignableFrom(clazz))
		{
			return new EntityFallingBlock(world, x, y, z, world.getType(new BlockPosition(x, y, z)));
		}
		else if (org.bukkit.entity.Projectile.class.isAssignableFrom(clazz))
		{
			if (org.bukkit.entity.Snowball.class.isAssignableFrom(clazz))
			{
				return new net.minecraft.server.v1_12_R1.EntitySnowball(world, x, y, z);
			}
			else if (org.bukkit.entity.Egg.class.isAssignableFrom(clazz))
			{
				return new net.minecraft.server.v1_12_R1.EntityEgg(world, x, y, z);
			}
			else if (Arrow.class.isAssignableFrom(clazz))
			{
				if (org.bukkit.entity.TippedArrow.class.isAssignableFrom(clazz))
				{
					EntityTippedArrow entity = new EntityTippedArrow(world);
					entity.setType(org.bukkit.craftbukkit.v1_12_R1.potion.CraftPotionUtil.fromBukkit(new org.bukkit.potion.PotionData(org.bukkit.potion.PotionType.WATER, false, false)));
					return entity;
				}
				else if (org.bukkit.entity.SpectralArrow.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntitySpectralArrow(world);
				} else {
					return new EntityTippedArrow(world);
				}
			}
			else if (org.bukkit.entity.ThrownExpBottle.class.isAssignableFrom(clazz))
			{
				return new net.minecraft.server.v1_12_R1.EntityThrownExpBottle(world);
			}
			else if (org.bukkit.entity.EnderPearl.class.isAssignableFrom(clazz))
			{
				return new net.minecraft.server.v1_12_R1.EntityEnderPearl(world);
			}
			else if (org.bukkit.entity.ThrownPotion.class.isAssignableFrom(clazz))
			{
				if (org.bukkit.entity.LingeringPotion.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntityPotion(world, x, y, z, CraftItemStack.asNMSCopy(new ItemStack(org.bukkit.Material.LINGERING_POTION, 1)));
				} else {
					return new net.minecraft.server.v1_12_R1.EntityPotion(world, x, y, z, CraftItemStack.asNMSCopy(new ItemStack(org.bukkit.Material.SPLASH_POTION, 1)));
				}
			}
			else if (org.bukkit.entity.Fireball.class.isAssignableFrom(clazz))
			{
				if (org.bukkit.entity.SmallFireball.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntitySmallFireball(world);
				}
				else if (org.bukkit.entity.WitherSkull.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntityWitherSkull(world);
				}
				else if (org.bukkit.entity.DragonFireball.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntityDragonFireball(world);
				} else {
					return new net.minecraft.server.v1_12_R1.EntityLargeFireball(world);
				}
			}
			else if (org.bukkit.entity.ShulkerBullet.class.isAssignableFrom(clazz))
			{
				return new net.minecraft.server.v1_12_R1.EntityShulkerBullet(world);
			}
		}
		else if (org.bukkit.entity.Minecart.class.isAssignableFrom(clazz))
		{
			if (org.bukkit.entity.minecart.PoweredMinecart.class.isAssignableFrom(clazz))
			{
				return new net.minecraft.server.v1_12_R1.EntityMinecartFurnace(world, x, y, z);
			}
			else if (org.bukkit.entity.minecart.StorageMinecart.class.isAssignableFrom(clazz))
			{
				return new net.minecraft.server.v1_12_R1.EntityMinecartChest(world, x, y, z);
			}
			else if (org.bukkit.entity.minecart.ExplosiveMinecart.class.isAssignableFrom(clazz))
			{
				return new net.minecraft.server.v1_12_R1.EntityMinecartTNT(world, x, y, z);
			}
			else if (org.bukkit.entity.minecart.HopperMinecart.class.isAssignableFrom(clazz))
			{
				return new net.minecraft.server.v1_12_R1.EntityMinecartHopper(world, x, y, z);
			}
			else if (org.bukkit.entity.minecart.SpawnerMinecart.class.isAssignableFrom(clazz))
			{
				return new net.minecraft.server.v1_12_R1.EntityMinecartMobSpawner(world, x, y, z);
			}
			else if (org.bukkit.entity.minecart.CommandMinecart.class.isAssignableFrom(clazz))
			{
				return new net.minecraft.server.v1_12_R1.EntityMinecartCommandBlock(world, x, y, z);
			} else {
				return new net.minecraft.server.v1_12_R1.EntityMinecartRideable(world, x, y, z);
			}
		}
		else if (org.bukkit.entity.EnderSignal.class.isAssignableFrom(clazz))
		{
			return new net.minecraft.server.v1_12_R1.EntityEnderSignal(world, x, y, z);
		}
		else if (org.bukkit.entity.EnderCrystal.class.isAssignableFrom(clazz))
		{
			return new net.minecraft.server.v1_12_R1.EntityEnderCrystal(world);
		}
		else if (LivingEntity.class.isAssignableFrom(clazz))
		{
			if (org.bukkit.entity.Chicken.class.isAssignableFrom(clazz))
			{
				return new net.minecraft.server.v1_12_R1.EntityChicken(world);
			}
			else if (org.bukkit.entity.Cow.class.isAssignableFrom(clazz))
			{
				if (org.bukkit.entity.MushroomCow.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntityMushroomCow(world);
				} else {
					return new net.minecraft.server.v1_12_R1.EntityCow(world);
				}
			}
			else if (org.bukkit.entity.Golem.class.isAssignableFrom(clazz))
			{
				if (org.bukkit.entity.Snowman.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntitySnowman(world);
				}
				else if (org.bukkit.entity.IronGolem.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntityIronGolem(world);
				}
				else if (org.bukkit.entity.Shulker.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntityShulker(world);
				}
			}
			else if (org.bukkit.entity.Creeper.class.isAssignableFrom(clazz))
			{
				return new net.minecraft.server.v1_12_R1.EntityCreeper(world);
			}
			else if (org.bukkit.entity.Ghast.class.isAssignableFrom(clazz))
			{
				return new net.minecraft.server.v1_12_R1.EntityGhast(world);
			}
			else if (org.bukkit.entity.Pig.class.isAssignableFrom(clazz))
			{
				return new net.minecraft.server.v1_12_R1.EntityPig(world);
			}
			else if (!Player.class.isAssignableFrom(clazz))
			{
				if (org.bukkit.entity.Sheep.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntitySheep(world);
				}
				else if (org.bukkit.entity.Horse.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntityHorse(world);
				}
				else if (org.bukkit.entity.Skeleton.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntitySkeleton(world);
				}
				else if (org.bukkit.entity.Slime.class.isAssignableFrom(clazz))
				{
					if (org.bukkit.entity.MagmaCube.class.isAssignableFrom(clazz))
					{
						return new net.minecraft.server.v1_12_R1.EntityMagmaCube(world);
					} else {
						return new net.minecraft.server.v1_12_R1.EntitySlime(world);
					}
				}
				else if (org.bukkit.entity.Spider.class.isAssignableFrom(clazz))
				{
					if (org.bukkit.entity.CaveSpider.class.isAssignableFrom(clazz))
					{
						return new net.minecraft.server.v1_12_R1.EntityCaveSpider(world);
					} else {
						return new net.minecraft.server.v1_12_R1.EntitySpider(world);
					}
				}
				else if (org.bukkit.entity.Squid.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntitySquid(world);
				}
				else if (org.bukkit.entity.Tameable.class.isAssignableFrom(clazz))
				{
					if (org.bukkit.entity.Wolf.class.isAssignableFrom(clazz))
					{
						return new net.minecraft.server.v1_12_R1.EntityWolf(world);
					}
					else if (org.bukkit.entity.Ocelot.class.isAssignableFrom(clazz))
					{
						return new EntityOcelot(world);
					}
				}
				else if (org.bukkit.entity.PigZombie.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntityPigZombie(world);
				}
				else if (org.bukkit.entity.Zombie.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntityZombie(world);
				}
				else if (org.bukkit.entity.Giant.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntityGiantZombie(world);
				}
				else if (org.bukkit.entity.Silverfish.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntitySilverfish(world);
				}
				else if (org.bukkit.entity.Enderman.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntityEnderman(world);
				}
				else if (org.bukkit.entity.Blaze.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntityBlaze(world);
				}
				else if (org.bukkit.entity.Villager.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntityVillager(world);
				}
				else if (org.bukkit.entity.Witch.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntityWitch(world);
				}
				else if (org.bukkit.entity.Wither.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntityWither(world);
				}
				else if (org.bukkit.entity.ComplexLivingEntity.class.isAssignableFrom(clazz))
				{
					if (org.bukkit.entity.EnderDragon.class.isAssignableFrom(clazz))
					{
						return new net.minecraft.server.v1_12_R1.EntityEnderDragon(world);
					}
				}
				else if (org.bukkit.entity.Ambient.class.isAssignableFrom(clazz))
				{
					if (org.bukkit.entity.Bat.class.isAssignableFrom(clazz))
					{
						return new net.minecraft.server.v1_12_R1.EntityBat(world);
					}
				}
				else if (org.bukkit.entity.Rabbit.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntityRabbit(world);
				}
				else if (org.bukkit.entity.Endermite.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntityEndermite(world);
				}
				else if (org.bukkit.entity.Guardian.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntityGuardian(world);
				}
				else if (org.bukkit.entity.ArmorStand.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntityArmorStand(world, x, y, z);
				}
				else if (org.bukkit.entity.PolarBear.class.isAssignableFrom(clazz))
				{
					return new net.minecraft.server.v1_12_R1.EntityPolarBear(world);
				}
			}
		}

		//TODO: Does this need to be re-enabled??
		/*
		else if (org.bukkit.entity.Hanging.class.isAssignableFrom(clazz))
		{
			org.bukkit.block.Block block = getBlockAt(location);
			BlockFace face = BlockFace.SELF;

			int width = 16;
			int height = 16;

			if (org.bukkit.entity.ItemFrame.class.isAssignableFrom(clazz))
			{
				width = 12;
				height = 12;
			}
			else if (org.bukkit.entity.LeashHitch.class.isAssignableFrom(clazz))
			{
				width = 9;
				height = 9;
			}

			BlockFace[] faces = { BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH };
			BlockPosition pos = new BlockPosition((int)x, (int)y, (int)z);
			for (BlockFace dir : faces)
			{
				net.minecraft.server.v1_10_R1.Block nmsBlock = org.bukkit.craftbukkit.v1_10_R1.util.CraftMagicNumbers.getBlock(block.getRelative(dir));
				if ((nmsBlock.getBlockData().getMaterial().isBuildable()) || (net.minecraft.server.v1_10_R1.BlockDiodeAbstract.isDiode(nmsBlock.getBlockData())))
				{
					boolean taken = false;
					AxisAlignedBB bb = EntityHanging.calculateBoundingBox(null, pos, CraftBlock.blockFaceToNotch(dir).opposite(), width, height);
					List<net.minecraft.server.v1_10_R1.Entity> list = world.getEntities(null, bb);
					for (Iterator<net.minecraft.server.v1_10_R1.Entity> it = list.iterator(); (!taken) && (it.hasNext());)
					{
						net.minecraft.server.v1_10_R1.Entity e = (net.minecraft.server.v1_10_R1.Entity)it.next();
						if ((e instanceof EntityHanging))
						{
							taken = true;
						}
					}

					if (!taken)
					{
						face = dir;
						break;
					}
			    }
		  	}

			if (org.bukkit.entity.LeashHitch.class.isAssignableFrom(clazz))
			{
			    return new net.minecraft.server.v1_10_R1.EntityLeash(world, new BlockPosition((int)x, (int)y, (int)z));
			    attachedToPlayer = true;
			} else {
			    com.google.common.base.Preconditions.checkArgument(face != BlockFace.SELF, "Cannot spawn hanging entity for %s at %s (no free face)", new Object[] { clazz.getName(), location });

			    EnumDirection dir = CraftBlock.blockFaceToNotch(face).opposite();
			    if (org.bukkit.entity.Painting.class.isAssignableFrom(clazz))
			    {
			      return new net.minecraft.server.v1_10_R1.EntityPainting(world, new BlockPosition((int)x, (int)y, (int)z), dir);
			    }
			    else if (org.bukkit.entity.ItemFrame.class.isAssignableFrom(clazz))
			    {
			      return new net.minecraft.server.v1_10_R1.EntityItemFrame(world, new BlockPosition((int)x, (int)y, (int)z), dir);
			    }
			}

			if ((entity != null) && (!((EntityHanging)entity).survives()))
			{
			    throw new IllegalArgumentException("Cannot spawn hanging entity for " + clazz.getName() + " at " + location);
			}
		}
		*/
		else if (org.bukkit.entity.TNTPrimed.class.isAssignableFrom(clazz))
		{
			return new net.minecraft.server.v1_12_R1.EntityTNTPrimed(world, x, y, z, null);
		}
		else if (org.bukkit.entity.ExperienceOrb.class.isAssignableFrom(clazz))
		{
			return new net.minecraft.server.v1_12_R1.EntityExperienceOrb(world, x, y, z, 0);
		}
		else if (org.bukkit.entity.Weather.class.isAssignableFrom(clazz))
		{
			if (LightningStrike.class.isAssignableFrom(clazz))
			{
				return new EntityLightning(world, x, y, z, false);
			}
		}
		else if (org.bukkit.entity.Firework.class.isAssignableFrom(clazz))
		{
			return new net.minecraft.server.v1_12_R1.EntityFireworks(world, x, y, z, null);
		}
		else if (org.bukkit.entity.AreaEffectCloud.class.isAssignableFrom(clazz))
		{
			return new net.minecraft.server.v1_12_R1.EntityAreaEffectCloud(world, x, y, z);
		}

		if(org.bukkit.entity.Donkey.class.isAssignableFrom(clazz))
		{
			return new net.minecraft.server.v1_12_R1.EntityHorseDonkey(world);
		}
		if(org.bukkit.entity.ElderGuardian.class.isAssignableFrom(clazz))
		{
			return new net.minecraft.server.v1_12_R1.EntityGuardianElder(world);
		}
		if(org.bukkit.entity.Evoker.class.isAssignableFrom(clazz))
		{
			return new net.minecraft.server.v1_12_R1.EntityEvoker(world);
		}
		if(org.bukkit.entity.EvokerFangs.class.isAssignableFrom(clazz))
		{
			return new net.minecraft.server.v1_12_R1.EntityEvokerFangs(world);
		}
		if(org.bukkit.entity.Husk.class.isAssignableFrom(clazz))
		{
			return new net.minecraft.server.v1_12_R1.EntityZombieHusk(world);
		}
		if(org.bukkit.entity.Llama.class.isAssignableFrom(clazz))
		{
			return new net.minecraft.server.v1_12_R1.EntityLlama(world);
		}
		if(org.bukkit.entity.LlamaSpit.class.isAssignableFrom(clazz))
		{
			return new net.minecraft.server.v1_12_R1.EntityLlamaSpit(world);
		}
		if(org.bukkit.entity.Mule.class.isAssignableFrom(clazz))
		{
			return new net.minecraft.server.v1_12_R1.EntityHorseMule(world);
		}
		if(org.bukkit.entity.SkeletonHorse.class.isAssignableFrom(clazz))
		{
			return new net.minecraft.server.v1_12_R1.EntityHorseSkeleton(world);
		}
		if(org.bukkit.entity.Stray.class.isAssignableFrom(clazz))
		{
			return new net.minecraft.server.v1_12_R1.EntitySkeletonStray(world);
		}
		if(org.bukkit.entity.Vex.class.isAssignableFrom(clazz))
		{
			return new net.minecraft.server.v1_12_R1.EntityVex(world);
		}
		if(org.bukkit.entity.Vindicator.class.isAssignableFrom(clazz))
		{
			return new net.minecraft.server.v1_12_R1.EntityVindicator(world);
		}
		if(org.bukkit.entity.WitherSkeleton.class.isAssignableFrom(clazz))
		{
			return new net.minecraft.server.v1_12_R1.EntitySkeletonWither(world);
		}
		if(org.bukkit.entity.ZombieHorse.class.isAssignableFrom(clazz))
		{
			return new net.minecraft.server.v1_12_R1.EntityHorseZombie(world);
		}
		if(org.bukkit.entity.ZombieVillager.class.isAssignableFrom(clazz))
		{
			return new net.minecraft.server.v1_12_R1.EntityZombieVillager(world);
		}

		return null;
	}
	
    @Override
    public void mergeVanillaBiomeMobSpawnSettings(BiomeConfigStub biomeConfigStub, String biomeResourceLocation) { }
    
	@Override
	public void spawnEntity(EntityFunction<?> entityData)
	{
    	Random rand = new Random();

		String mobTypeName = entityData.mobName;
		int groupSize = entityData.groupSize;
		String nameTag = entityData.nameTagOrNBTFileName;

		EntityType entityType = null;

		for(EntityType entityType1 : EntityType.values())
		{
			if(entityType1.name() != null && mobTypeName.toLowerCase().replace("_", "").replace(" ", "").replace("entity","").equals(entityType1.name().toLowerCase().replace("_", "").replace(" ", "").replace("entity","")))
			{
				entityType = entityType1;
				break;
			}
		}

		// Make sure all mob names that forge accepts also work here

    	if(mobTypeName.toLowerCase().replace("entity", "").replace("_", "").replace(" ", "").equals("evocationillager"))
    	{
    		entityType = org.bukkit.entity.EntityType.EVOKER;
    	}
    	if(mobTypeName.toLowerCase().replace("entity", "").replace("_", "").replace(" ", "").equals("vindicationillager"))
    	{
    		entityType = org.bukkit.entity.EntityType.VINDICATOR;
    	}
    	if(mobTypeName.toLowerCase().replace("entity", "").replace("_", "").replace(" ", "").equals("zombiepigman"))
    	{
    		entityType = org.bukkit.entity.EntityType.PIG_ZOMBIE;
    	}

    	//

		if(entityType == null)
		{
			if(OTG.getPluginConfig().spawnLog)
			{
				OTG.log(LogMarker.WARN, "Could not find entity: " + mobTypeName);
			}
			return;
		}

		if (entityType == EntityType.PLAYER)
		{
			return;
		}

		Entity entityLiving = getEntity(entityType.getEntityClass());
		org.bukkit.entity.Entity bukkitEntityLiving = entityLiving.getBukkitEntity();

		boolean isWaterMob = bukkitEntityLiving instanceof CraftGuardian || bukkitEntityLiving instanceof CraftElderGuardian;

        if(entityLiving != null)
        {
			EnumCreatureType creatureType = EnumCreatureType.CREATURE;

			// MONSTER
			if(
				bukkitEntityLiving instanceof CraftComplexLivingEntity || // Dragon
				bukkitEntityLiving instanceof CraftSlime || // Slime/Magma
				bukkitEntityLiving instanceof CraftMonster ||
				bukkitEntityLiving instanceof CraftFlying // Ghast
			)
			{
				creatureType = EnumCreatureType.MONSTER;
			}

			// AMBIENT
			if(
				bukkitEntityLiving instanceof CraftAmbient // Bat
			)
			{
				creatureType = EnumCreatureType.AMBIENT;
			}

			// CREATURE
			if(
				bukkitEntityLiving instanceof CraftAnimals || // Creature
				bukkitEntityLiving instanceof CraftVillager ||
				bukkitEntityLiving instanceof CraftGolem
			)
			{
				creatureType = EnumCreatureType.CREATURE;
			}

			// WATERCREATURE
			if(
				bukkitEntityLiving instanceof CraftWaterMob
			)
			{
				creatureType = EnumCreatureType.WATER_CREATURE;
			}

            int j1 = entityData.x;
            int k1 = entityData.y;
            int l1 = entityData.z;

            int x = entityData.x;
            int y = entityData.y;
            int z = entityData.z;

            CraftBlock block = (CraftBlock) world.getWorld().getBlockAt(new Location(world.getWorld(), x, y, z));
            org.bukkit.Material material = block.getType();

            boolean isOutsideBuildHeight = y < 0 || y >= 256;
            boolean isOpaque = material.isTransparent() ? false : material.isSolid();
            boolean isFullCube = material != org.bukkit.Material.STEP;
            boolean canProvidePower = block.isBlockPowered();
            boolean isBlockNormalCube = !isOutsideBuildHeight && isOpaque && isFullCube && !canProvidePower;

            if (!isBlockNormalCube && (((creatureType == EnumCreatureType.WATER_CREATURE || isWaterMob) && (material == org.bukkit.Material.WATER || material == org.bukkit.Material.STATIONARY_WATER)) || material == org.bukkit.Material.AIR))
            {
	            float f = (float)j1 + 0.5F;
	            float f1 = (float)k1;
	            float f2 = (float)l1 + 0.5F;

	            if(entityLiving instanceof EntityLiving)
	            {
	            	for(int r = 0; r < groupSize; r++)
	            	{
            			CraftEntity entity = (CraftEntity) world.getWorld().spawn(new Location(world.getWorld(), (double)f, (double)f1, (double)f2, rand.nextFloat() * 360.0F, 0.0F), entityType.getEntityClass());

	            		if(entityData.nameTagOrNBTFileName != null && (entityData.nameTagOrNBTFileName.toLowerCase().trim().endsWith(".txt") || entityData.nameTagOrNBTFileName.toLowerCase().trim().endsWith(".nbt")))
	           			{
	           				applyMetaData(entity, entityData.mobName, entityData.getMetaData());
	           			}

	            		if(entityData.nameTagOrNBTFileName != null && !entityData.nameTagOrNBTFileName.toLowerCase().trim().endsWith(".txt") && !entityData.nameTagOrNBTFileName.toLowerCase().trim().endsWith(".nbt"))
	            		{
	            			if(nameTag != null && nameTag.length() > 0)
	        				{
	            				entity.setCustomName(nameTag);
	        				}
	            		}

	            		if(entity instanceof CraftLivingEntity)
	            		{
	            			((CraftLivingEntity) entity).setRemoveWhenFarAway(false); // <- makes sure mobs don't de-spawn
	            		}
	            	}
	            } else {
	            	for(int r = 0; r < groupSize; r++)
	            	{
	            		CraftEntity entity = (CraftEntity) world.getWorld().spawn(new Location(world.getWorld(), (double)f, (double)f1, (double)f2, rand.nextFloat() * 360.0F, 0.0F), entityType.getEntityClass());

	            		if(entityData.nameTagOrNBTFileName != null && (entityData.nameTagOrNBTFileName.toLowerCase().trim().endsWith(".txt") || entityData.nameTagOrNBTFileName.toLowerCase().trim().endsWith(".nbt")))
	           			{
	           				applyMetaData(entity, entityData.mobName, entityData.getMetaData());
	           			}
	            	}
	            }
            }
		}
	}
    
    private void applyMetaData(CraftEntity entity, String mobName, String metaDataString)
	{
    	NBTTagCompound nbttagcompound = new NBTTagCompound();

        try
        {
            NBTBase nbtbase = JsonToNBT.getTagFromJson(metaDataString);

            if (!(nbtbase instanceof NBTTagCompound))
            {
            	if(OTG.getPluginConfig().spawnLog)
            	{
            		OTG.log(LogMarker.WARN, "Invalid NBT tag for mob in EntityFunction: " + metaDataString + ". Skipping mob.");
            	}
            	return;
            }

            nbttagcompound = (NBTTagCompound)nbtbase;
        }
        catch (NBTException nbtexception)
        {
        	if(OTG.getPluginConfig().spawnLog)
        	{
        		OTG.log(LogMarker.WARN, "Invalid NBT tag for mob in EntityFunction: " + metaDataString + ". Skipping mob.");
        	}
        	return;
        }

        nbttagcompound.setString("id", mobName);

		Entity nmsEntity = ((CraftEntity)entity).getHandle();

		NBTTagCompound originalTag = new NBTTagCompound();
		nmsEntity.c(originalTag);

		NBTBase originalPos = originalTag.get("Pos");
		NBTBase originalRot = originalTag.get("Rotation");

		NBTBase originalUUIDLeast = originalTag.get("UUIDLeast");
		NBTBase originalUUIDMost = originalTag.get("UUIDMost");

		nbttagcompound.set("Pos", originalPos);
		nbttagcompound.set("Rotation", originalRot);
		nbttagcompound.set("UUIDLeast", originalUUIDLeast);
		nbttagcompound.set("UUIDMost", originalUUIDMost);

		nmsEntity.f(nbttagcompound);
		nmsEntity.recalcPosition();
	}
	
    // Chunks
    
    private Chunk getChunk(int x, int y, int z)
    {
        if (y < PluginStandardValues.WORLD_DEPTH || y >= PluginStandardValues.WORLD_HEIGHT)
            return null;

        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        if (this.chunkCache == null)
        {
            // Blocks requested outside population step
            // (Tree growing, /otg spawn, etc.)
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
    public ChunkCoordinate getSpawnChunk()
    {
    	BlockPosition spawnPos = world.getSpawn();

    	return ChunkCoordinate.fromBlockCoords(spawnPos.getX(), spawnPos.getZ());
    }
	
	@Override
	public boolean isInsidePregeneratedRegion(ChunkCoordinate chunk)
	{
		// TODO Implement this
		return false;
	}

	@Override
	public boolean isInsideWorldBorder(ChunkCoordinate chunk, boolean spawningResources)
	{
		// TODO Implement this
		return true;
	}
    
    // Blocks / materials
    	
	@Override
	public boolean isNullOrAir(int x, int y, int z, boolean allowOutsidePopulatingArea)
	{
    	if (y >= PluginStandardValues.WORLD_HEIGHT || y < PluginStandardValues.WORLD_DEPTH)
    	{
        	return true;
    	}

        Chunk chunk = this.getChunk(x, y, z);
        if (chunk == null)
        {
            return true;
        }

        return chunk.a(x & 0xF, y, z & 0xF).getMaterial().equals(Material.AIR);
	}
    
    @Override
    public int getLiquidHeight(int x, int z)
    {
        for (int y = getHighestBlockYAt(x, z) - 1; y > 0; y--)
        {
            LocalMaterialData material = getMaterial(x, y, z, false);
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
            LocalMaterialData material = getMaterial(x, y, z, false);
            if (material.isSolid())
            {
                return y + 1;
            }
        }
        return -1;
    }

    @Override
    public LocalMaterialData getMaterial(int x, int y, int z, boolean allowOutsidePopulatingArea)
    {
        Chunk chunk = this.getChunk(x, y, z);
        if (chunk == null || y < PluginStandardValues.WORLD_DEPTH || y >= PluginStandardValues.WORLD_HEIGHT)
        {
            return BukkitMaterialData.ofMinecraftBlock(Blocks.AIR);
        }

        return BukkitMaterialData.ofMinecraftBlockData(chunk.a(x, y, z));
    }

	@Override
	public int getHighestBlockYAt(int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow)
	{
		// TODO Implement this
		throw new RuntimeException();
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
	public OTGBlock[] getBlockColumn(int x, int z)
	{
		// TODO Implement this
		throw new RuntimeException();
	}
    
    @Override
    public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, boolean allowOutsidePopulatingArea)
    {
        /*
         * This method usually breaks on every Minecraft update. Always check
         * whether the names are still correct. Often, you'll also need to
         * rewrite parts of this method for newer block place logic.
         */

        try
        {
            if (y < PluginStandardValues.WORLD_DEPTH || y >= PluginStandardValues.WORLD_HEIGHT)
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

    	    if (metaDataTag != null)
    	    {
    	    	attachMetadata(x, y, z, metaDataTag);
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
    
    private void attachMetadata(int x, int y, int z, NamedBinaryTag tag)
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
            tileEntity.load(nmsTag);
        } else {
        	if(OTG.getPluginConfig().spawnLog)
        	{
        		OTG.log(LogMarker.WARN, "Skipping tile entity with id {}, cannot be placed at {},{},{} on id {}", nmsTag.getString("id"), x, y, z, getMaterial(x, y, z, false));
        	}
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
	public void setAllowSpawningOutsideBounds(boolean allowSpawningOutsideBounds)
	{
		// TODO: Implement this?
	}
}