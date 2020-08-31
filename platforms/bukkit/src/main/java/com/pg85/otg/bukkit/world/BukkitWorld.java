package com.pg85.otg.bukkit.world;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
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
import org.bukkit.craftbukkit.v1_12_R1.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.pg85.otg.OTG;
import com.pg85.otg.bukkit.biomes.BukkitBiome;
import com.pg85.otg.bukkit.generator.BukkitVanillaBiomeGenerator;
import com.pg85.otg.bukkit.generator.OTGChunkGenerator;
import com.pg85.otg.bukkit.generator.OTGInternalChunkGenerator;
import com.pg85.otg.bukkit.generator.OTGWorldChunkManager;
import com.pg85.otg.bukkit.generator.OTGWorldProvider;
import com.pg85.otg.bukkit.generator.structures.MojangStructurePart;
import com.pg85.otg.bukkit.generator.structures.OTGMansionGen;
import com.pg85.otg.bukkit.generator.structures.OTGMineshaftGen;
import com.pg85.otg.bukkit.generator.structures.OTGNetherFortressGen;
import com.pg85.otg.bukkit.generator.structures.OTGOceanMonumentGen;
import com.pg85.otg.bukkit.generator.structures.OTGRareBuildingGen;
import com.pg85.otg.bukkit.generator.structures.OTGStrongholdGen;
import com.pg85.otg.bukkit.generator.structures.OTGVillageGen;
import com.pg85.otg.bukkit.materials.BukkitMaterialData;
import com.pg85.otg.bukkit.util.JsonToNBT;
import com.pg85.otg.bukkit.util.NBTException;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.common.WorldSession;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.biome.settings.ReplacedBlocksMatrix.ReplacedBlocksInstruction;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.customobjects.SpawnableObject;
import com.pg85.otg.customobjects.bofunctions.EntityFunction;
import com.pg85.otg.customobjects.structures.CustomStructureCache;

import com.pg85.otg.generator.ChunkBuffer;
import com.pg85.otg.generator.ObjectSpawner;
import com.pg85.otg.generator.biome.BiomeGenerator;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.network.ConfigProvider;
import com.pg85.otg.network.ServerConfigProvider;
import com.pg85.otg.util.BiomeIds;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.helpers.ReflectionHelper;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;
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
import net.minecraft.server.v1_12_R1.DefinedStructure;
import net.minecraft.server.v1_12_R1.DefinedStructureManager;
import net.minecraft.server.v1_12_R1.DimensionManager;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityFallingBlock;
import net.minecraft.server.v1_12_R1.EntityLightning;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.EntityOcelot;
import net.minecraft.server.v1_12_R1.EntityTippedArrow;
import net.minecraft.server.v1_12_R1.EnumCreatureType;
import net.minecraft.server.v1_12_R1.EnumDirection;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.MinecraftKey;
import net.minecraft.server.v1_12_R1.NBTBase;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.SpawnerCreature;
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

public class BukkitWorld implements LocalWorld
{
    private static final int MAX_BIOMES_COUNT = 4096;
    private static final int MAX_SAVED_BIOMES_COUNT = 256;
    public static final int STANDARD_WORLD_HEIGHT = 128; // TODO: Why is this 128, should be 255?
    
    // Initially false, set to true when enabled once
    private boolean initialized;

    private OTGChunkGenerator generator;
    private WorldServer world;
    private ServerConfigProvider settings;
    private CustomStructureCache structureCache;
    private String name;
    private BiomeGenerator biomeGenerator;

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

	private BukkitWorldSession worldSession;

    public BukkitWorld(String _name)
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
        } else {
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
    public void enable(org.bukkit.World world)
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
            
            switch (this.settings.getWorldConfig().modeTerrain)
            {
                case Normal:
            	/*
                case OldGenerator:
                */
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
                //case Default:
                    //break;
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
        } else {
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
    	this.getChunkGenerator().startPopulation(chunkCoord);
    }

    @Override
    public void endPopulation()
    {
    	this.getChunkGenerator().endPopulation();
    }
    
    // Biomes
    
    @Override
    public LocalBiome createBiomeFor(BiomeConfig biomeConfig, BiomeIds biomeIds, ConfigProvider configProvider, boolean isReload)
    {
    	return createBiomeFor(biomeConfig, biomeIds, isReload);
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
    	// TODO: Fix populateUsingSavedBiomes, just fixing this method may not work though, as the biome gen now uses otg biome id's everywhere.
        //if (this.settings.getWorldConfig().populateUsingSavedBiomes)
        //{       	

        //} else {
            return getCalculatedBiome(x, z);
        //}
    }

    @Override
    public String getSavedBiomeName(int x, int z)
    {
        // TODO: Should this return resourcelocation?
    	// TODO: Fetch name from registry instead of replacetobiomename?
        BiomeConfig biomeConfig = getBiome(x, z).getBiomeConfig();
        if(biomeConfig.replaceToBiomeName == null || biomeConfig.replaceToBiomeName.trim().length() == 0)
        {
     	   return biomeConfig.getName();
        } else {
     	   return biomeConfig.replaceToBiomeName;
        }
    }
    
    private LocalBiome createBiomeFor(BiomeConfig biomeConfig, BiomeIds biomeIds, boolean isReload)
    {
        BukkitBiome biome = BukkitBiome.forCustomBiome(biomeConfig, biomeIds, this.getName(), isReload);
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

    // Structures / trees
    
    @Override
    public void prepareDefaultStructures(int chunkX, int chunkZ, boolean dry)
    {
        WorldConfig worldConfig = this.settings.getWorldConfig();

        if (worldConfig.strongholdsEnabled)
        {
            this.strongholdGen.a(this.world, chunkX, chunkZ, null);
        }
        if (worldConfig.mineshaftsEnabled)
        {
            this.mineshaftGen.a(this.world, chunkX, chunkZ, null);
        }
        if (worldConfig.villagesEnabled && dry)
        {
            this.villageGen.a(this.world, chunkX, chunkZ, null);
        }
        if (worldConfig.rareBuildingsEnabled)
        {
        	this.rareBuildingGen.a(this.world, chunkX, chunkZ, null);
        }
        if (worldConfig.netherFortressesEnabled)
        {
            this.netherFortressGen.a(this.world, chunkX, chunkZ, null);
        }
        if (worldConfig.oceanMonumentsEnabled)
        {
            this.oceanMonumentGen.a(this.world, chunkX, chunkZ, null);
        }
        if (worldConfig.woodLandMansionsEnabled)
        {
        	this.mansionGen.a(this.world, chunkX, chunkZ, null);
        }
    }

    @Override
    public boolean placeDungeon(Random rand, int x, int y, int z)
    {
    	if(y < PluginStandardValues.WORLD_DEPTH || y >= PluginStandardValues.WORLD_HEIGHT)
    	{
    		return false;
    	}
    	
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
    	if(y < PluginStandardValues.WORLD_DEPTH || y >= PluginStandardValues.WORLD_HEIGHT)
    	{
    		return false;
    	}
    	
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
	public boolean chunkHasDefaultStructure(Random random, ChunkCoordinate chunk)
	{
        WorldConfig worldConfig = this.settings.getWorldConfig();
        BlockPosition blockPos = new BlockPosition(chunk.getBlockXCenter(), 0, chunk.getBlockZCenter());
        // Allow OTG structures to spawn on top of strongholds
        // Allow OTG structures to spawn on top of mine shafts
        // TODO: VIllage gen detection doesn't appear to be working too well, fix..
        return 
		(worldConfig.villagesEnabled && this.villageGen instanceof OTGVillageGen && ((OTGVillageGen)this.villageGen).b(blockPos)) ||
		(worldConfig.villagesEnabled && !(this.villageGen instanceof OTGVillageGen) && this.villageGen.b(blockPos)) ||
        (worldConfig.rareBuildingsEnabled && this.rareBuildingGen instanceof OTGRareBuildingGen && ((OTGRareBuildingGen)this.rareBuildingGen).b(blockPos)) ||
        (worldConfig.rareBuildingsEnabled && !(this.rareBuildingGen instanceof OTGRareBuildingGen) && this.rareBuildingGen.b(blockPos)) ||
        (worldConfig.netherFortressesEnabled && this.netherFortressGen.b(blockPos)) ||
        (worldConfig.oceanMonumentsEnabled && this.oceanMonumentGen instanceof OTGOceanMonumentGen && ((OTGOceanMonumentGen)this.oceanMonumentGen).b(blockPos)) ||
        (worldConfig.oceanMonumentsEnabled && !(this.oceanMonumentGen instanceof OTGOceanMonumentGen) && this.oceanMonumentGen.b(blockPos)) ||
        (worldConfig.woodLandMansionsEnabled && this.mansionGen instanceof OTGMansionGen && ((OTGMansionGen)this.mansionGen).b(blockPos)) ||
        (worldConfig.woodLandMansionsEnabled && !(this.mansionGen instanceof OTGMansionGen) && this.mansionGen.b(blockPos))
        ;
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

    	replaceBlocks(getChunkGenerator().getChunk(chunkCoord.getBlockX() + 16, chunkCoord.getBlockZ() + 16));
    	replaceBlocks(getChunkGenerator().getChunk(chunkCoord.getBlockX(), chunkCoord.getBlockZ() + 16));
    	replaceBlocks(getChunkGenerator().getChunk(chunkCoord.getBlockX() + 16, chunkCoord.getBlockZ()));
    	replaceBlocks(getChunkGenerator().getChunk(chunkCoord.getBlockX(), chunkCoord.getBlockZ()));
    }

    private void replaceBlocks(Chunk rawChunk)
    {
        int worldStartX = rawChunk.locX * 16;
        int worldStartZ = rawChunk.locZ * 16;

        ChunkSection[] sectionsArray = rawChunk.getSections();
        ReplacedBlocksInstruction[] replaceArray;
        IBlockData block;
        int blockId = 0;
        int minHeight;
    	int maxHeight;
    	LocalBiome biome;
    	int y;
    	ReplacedBlocksInstruction[][][] replaceInstructionsCache = new ReplacedBlocksInstruction[16][16][];
    	
        for (ChunkSection section : sectionsArray)
        {
            if (section == null)
            {
                continue;
            }

            for (int sectionX = 0; sectionX < 16; sectionX++)
            {
                for (int sectionZ = 0; sectionZ < 16; sectionZ++)
                {
                	replaceArray = replaceInstructionsCache[sectionX][sectionZ];
                    if(replaceArray == null)
                    {
                    	biome = this.getBiome(worldStartX + sectionX, worldStartZ + sectionZ);
                    	if (biome == null || !biome.getBiomeConfig().replacedBlocks.hasReplaceSettings())
                    	{
                    		replaceArray = new ReplacedBlocksInstruction[0];
                    	} else {
                    		replaceArray = new ReplacedBlocksInstruction[biome.getBiomeConfig().replacedBlocks.getInstructions().size()];
                    		replaceArray = (ReplacedBlocksInstruction[])biome.getBiomeConfig().replacedBlocks.getInstructions().toArray(replaceArray);
                    	}
                    	replaceInstructionsCache[sectionX][sectionZ] = replaceArray;
                    }
                    if (replaceArray != null && replaceArray.length > 0)
                    {
                    	minHeight = PluginStandardValues.WORLD_HEIGHT;
                    	maxHeight = PluginStandardValues.WORLD_DEPTH;
                        for(ReplacedBlocksInstruction instruction : replaceArray)
                        {
                        	if(instruction.getFrom() != null && instruction.getTo() != null)
                        	{
	                        	if(instruction.getMinHeight() < minHeight)
	                        	{
	                        		minHeight = instruction.getMinHeight();
	                        	}
	                        	if(instruction.getMaxHeight() > maxHeight)
	                        	{
	                        		maxHeight = instruction.getMaxHeight();
	                    		}
                        	}
                        }
                        for (int sectionY = 0; sectionY < 16; sectionY++)
                        {
                        	block = null;
                        	y = section.getYPosition() + sectionY;                    
                        	if(y >= minHeight && y <= maxHeight)
                        	{
	                            for(ReplacedBlocksInstruction instruction : replaceArray)
	                            {
	                            	if(instruction.getFrom() != null && instruction.getTo() != null)
	                            	{
	                            		if(y >= instruction.getMinHeight() && y <= instruction.getMaxHeight())
	                            		{
	                            			if(block == null)
	                            			{
		                                    	block = section.getType(sectionX, sectionY, sectionZ);
		                                    	blockId = Block.getId(block.getBlock());
	                            			}
			                            	if(instruction.getFrom().getBlockId() == blockId)
			                            	{
			                                    section.setType(sectionX, sectionY, sectionZ, ((BukkitMaterialData)instruction.getTo()).internalBlock());                            		
			                            	}
	                            		}
	                            	}
	                            }
                        	}
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

	private Entity getEntity(Class<? extends org.bukkit.entity.Entity> clazz, EnumDirection direction)
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
		// TODO: Improve this to look for blocks to attach to (without causing cascading chunk gen..)
		else if (org.bukkit.entity.Hanging.class.isAssignableFrom(clazz))
		{
			if (org.bukkit.entity.LeashHitch.class.isAssignableFrom(clazz))
			{
			    return new net.minecraft.server.v1_12_R1.EntityLeash(world, new BlockPosition((int)x, (int)y, (int)z));
			} else {
			    if (org.bukkit.entity.Painting.class.isAssignableFrom(clazz))
			    {
			      return new net.minecraft.server.v1_12_R1.EntityPainting(world, new BlockPosition((int)x, (int)y, (int)z), direction); // TODO: Use Facing from nbt data
			    }
			    else if (org.bukkit.entity.ItemFrame.class.isAssignableFrom(clazz))
			    {
			      return new net.minecraft.server.v1_12_R1.EntityItemFrame(world, new BlockPosition((int)x, (int)y, (int)z), direction); // TODO: Use Facing from nbt data
			    }
			}
		}
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
	public void spawnEntity(EntityFunction<?> entityData, ChunkCoordinate chunkBeingPopulated)
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

		EnumDirection direction = EnumDirection.SOUTH;
        float rotationFromNbt = 0;
        boolean rotationFromNbtSet = false;
        
		String nbtData = entityData.getMetaData();
		if(nbtData != null && nbtData.trim().length() > 0)
		{
			NBTTagCompound nbttagcompound = new NBTTagCompound();
	        try
	        {
	            NBTBase nbtbase = JsonToNBT.getTagFromJson(nbtData);

	            if (!(nbtbase instanceof NBTTagCompound))
	            {
	            	if(OTG.getPluginConfig().spawnLog)
	            	{
	            		OTG.log(LogMarker.WARN, "Invalid NBT tag for mob in EntityFunction: " + nbtData + ". Skipping mob.");
	            	}
	            	return;
	            }
	            nbttagcompound = (NBTTagCompound)nbtbase;
	        }
	        catch (NBTException nbtexception)
	        {
	        	if(OTG.getPluginConfig().spawnLog)
	        	{
	        		OTG.log(LogMarker.WARN, "Invalid NBT tag for mob in EntityFunction: " + nbtData + ". Skipping mob.");
	        	}
	        	return;
	        }
	        if(nbttagcompound.hasKey("Facing"))
	        {
	        	int facing = nbttagcompound.getByte("Facing");
	        	switch(facing)
	        	{
	        		case 0:
	        			direction = EnumDirection.DOWN;
        			break;
	        		case 1:
	        			direction = EnumDirection.UP;
        			break;
	        		case 2:
	        			direction = EnumDirection.NORTH;
        			break;
	        		case 3:
	        			direction = EnumDirection.SOUTH;
        			break;
	        		case 4:
	        			direction = EnumDirection.WEST;
        			break;
	        		case 5:
	        			direction = EnumDirection.EAST;
        			break;        			        			
	        	}
    			rotationFromNbt = direction.get2DRotationValue() * 90;
	        	rotationFromNbtSet = true;
	        }
	        else if(nbttagcompound.hasKey("Rotation"))
	        {
	        	rotationFromNbt = nbttagcompound.getByte("Rotation");
	        	rotationFromNbtSet = true;
	        }
		}
		
		Entity entityLiving = getEntity(entityType.getEntityClass(), direction);
        if(entityLiving != null)
        {
    		org.bukkit.entity.Entity bukkitEntityLiving = entityLiving.getBukkitEntity();
    		boolean isWaterMob = bukkitEntityLiving instanceof CraftGuardian || bukkitEntityLiving instanceof CraftElderGuardian;        	
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
    					if(
							chunkBeingPopulated == null || 
							(
								OTG.IsInAreaBeingPopulated((int)Math.floor(f), (int)Math.floor(f2), chunkBeingPopulated)// || 
								//getChunkGenerator().chunkExists((int)Math.floor(entityliving.posX), (int)Math.floor(entityliving.posZ))
							)
						)
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
	            	}
	            } else {
	            	for(int r = 0; r < groupSize; r++)
	            	{
    					if(
							chunkBeingPopulated == null || 
							(
								OTG.IsInAreaBeingPopulated((int)Math.floor(f), (int)Math.floor(f2), chunkBeingPopulated)// || 
								//getChunkGenerator().chunkExists((int)Math.floor(entityliving.posX), (int)Math.floor(entityliving.posZ))
							)
						)
    					{
		            		try
		            		{
			            		CraftEntity entity = (CraftEntity) world.getWorld().spawn(new Location(world.getWorld(), (double)f, (double)f1, (double)f2, rotationFromNbtSet ? rotationFromNbt : rand.nextFloat() * 360.0F, 0.0F), entityType.getEntityClass());	            		
			            		if(entityData.nameTagOrNBTFileName != null && (entityData.nameTagOrNBTFileName.toLowerCase().trim().endsWith(".txt") || entityData.nameTagOrNBTFileName.toLowerCase().trim().endsWith(".nbt")))
			           			{
			           				applyMetaData(entity, entityData.mobName, entityData.getMetaData());
			           			}
		            		}
		            		catch(IllegalArgumentException ex)
		            		{
		            			if(OTG.getPluginConfig().spawnLog)
		            			{
		            				OTG.log(LogMarker.WARN, "Could not spawn entity " + entityData.mobName + " in world. Please note that hanging entities such as item frames may cause problems, this will be fixed in a future update. Exception: ");
		            				ex.printStackTrace();
		            			}
		            		}
	    			    	
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
		nbttagcompound.set("Pos", originalPos);
		
		NBTBase originalRot = originalTag.get("Rotation");
		nbttagcompound.set("Rotation", originalRot);
	
		NBTBase originalUUIDLeast = originalTag.get("UUIDLeast");
		NBTBase originalUUIDMost = originalTag.get("UUIDMost");

		nbttagcompound.set("UUIDLeast", originalUUIDLeast);
		nbttagcompound.set("UUIDMost", originalUUIDMost);

		nmsEntity.f(nbttagcompound);
		nmsEntity.recalcPosition();
	}
	
    // Chunks
        
    @Override
    public ChunkCoordinate getSpawnChunk()
    {
    	BlockPosition spawnPos = world.getSpawn();

    	return ChunkCoordinate.fromBlockCoords(spawnPos.getX(), spawnPos.getZ());
    }
	
	@Override
	public boolean isInsidePregeneratedRegion(ChunkCoordinate chunk)
	{
		return false;
	}
    
    // Blocks / materials

    @Override
    public LocalMaterialData getMaterial(int x, int y, int z, ChunkCoordinate chunkBeingPopulated)
    {
        if (y >= PluginStandardValues.WORLD_HEIGHT || y < PluginStandardValues.WORLD_DEPTH)
        {
        	return null;
        }

        // If the chunk exists or is inside the area being populated, fetch it normally.
        Chunk chunk = null;
    	if(
			(chunkBeingPopulated != null && OTG.IsInAreaBeingPopulated(x, z, chunkBeingPopulated)) 
			//|| getChunkGenerator().chunkExists(x, z)			
		)
    	{
    		chunk = getChunkGenerator().getChunk(x, z);
    	}
    	
		// If the chunk doesn't exist and we're doing something outside the
    	// population sequence, return the material without loading the chunk.
    	if(chunk == null && chunkBeingPopulated == null)
		{
			ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);
    		// If the chunk has already been loaded, no need to use fake chunks.
    		if(world.getChunkProviderServer().isLoaded(chunkCoord.getChunkX(), chunkCoord.getChunkZ()))
    		{
    			chunk = getChunkGenerator().getChunk(x, z);
    		} else {
    			// Calculate the material without loading the chunk.
    			return getChunkGenerator().getMaterialInUnloadedChunk(x,y,z);
    		}
    	}
    	
		// Tried to query an unloaded chunk outside the area being populated
    	if(chunk == null)
    	{
            return null;
    	}
    	
		// Get internal coordinates for block in chunk
        int internalX = x & 0xF;
        int internalZ = z & 0xF;
        return BukkitMaterialData.ofMinecraftBlockData(chunk.a(internalX, y, internalZ));
    }
	
    @Override
    public int getBlockAboveLiquidHeight(int x, int z, ChunkCoordinate chunkBeingPopulated)
    {
        int highestY = getHighestBlockYAt(x, z, false, true, false, false, false, chunkBeingPopulated);
        if(highestY > 0)
        {
        	highestY += 1;
        } else {
        	highestY = -1;
        }
		return highestY;
    }

    @Override
    public int getBlockAboveSolidHeight(int x, int z, ChunkCoordinate chunkBeingPopulated)
    {
        int highestY = getHighestBlockYAt(x, z, true, false, true, true, false, chunkBeingPopulated);
        if(highestY > 0)
        {
        	highestY += 1;
        } else {
        	highestY = -1;
        }
		return highestY;
    }
    
    @Override
    public int getHighestBlockAboveYAt(int x, int z, ChunkCoordinate chunkBeingPopulated)
    {
    	return getHighestBlockYAt(x, z, true, true, false, true, false, chunkBeingPopulated) + 1;
    }
    
	@Override
	public int getHighestBlockYAt(int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, boolean ignoreLeaves, ChunkCoordinate chunkBeingPopulated)
	{
        // If the chunk exists or is inside the area being populated, fetch it normally.
        Chunk chunk = null;
    	if(
			(chunkBeingPopulated != null && OTG.IsInAreaBeingPopulated(x, z, chunkBeingPopulated)) 
			//|| getChunkGenerator().chunkExists(x, z)			
		)
    	{
    		chunk = getChunkGenerator().getChunk(x, z);
    	}
    	
		// If the chunk doesn't exist and we're doing something outside the
    	// population sequence, return the material without loading the chunk.
    	if(chunk == null && chunkBeingPopulated == null)
		{
			ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);
    		// If the chunk has already been loaded, no need to use fake chunks.
    		if(world.getChunkProviderServer().isLoaded(chunkCoord.getChunkX(), chunkCoord.getChunkZ()))
    		{
    			chunk = getChunkGenerator().getChunk(x, z);
    		} else {
    			// Calculate the height without loading the chunk.
    			return getChunkGenerator().getHighestBlockYInUnloadedChunk(x, z, findSolid, findLiquid, ignoreLiquid, ignoreSnow);
    		}
    	}
    	
		// Tried to query an unloaded chunk outside the area being populated
    	if(chunk == null)
    	{
            return -1;
    	}
    	
		// Get internal coordinates for block in chunk
        int internalX = x & 0xF;
        int internalZ = z & 0xF;

        int heightMapy = chunk.b(internalX, internalZ);
        
        // Fix for incorrect light map
        // TODO: Fix this properly?
        boolean incorrectHeightMap = false;
        while (heightMapy < getHeightCap() && chunk.a(internalX, heightMapy, internalZ).getMaterial().blocksLight())
        {
        	heightMapy++;
            incorrectHeightMap = true;
        }
        if (incorrectHeightMap)// && isSafeForLightUpdates(chunk, x, z))
        {
            // Let Minecraft know that it made an error
            world.w(new BlockPosition(x, heightMapy, z)); // world.relight
        }

		BukkitMaterialData material;
    	boolean isLiquid;
    	boolean isSolid;
    	IBlockData blockData;
    	Block block;
    	
    	for(int i = heightMapy; i >= 0; i--)
        {
    		blockData = chunk.getBlockData(new BlockPosition(internalX, i, internalZ));
    		block = blockData.getBlock();
    		material = BukkitMaterialData.ofMinecraftBlockData(blockData);
        	isLiquid = material.isLiquid();
        	isSolid =
			(
    			material.isSolid() ||
    			(
					!ignoreLeaves && 
					(
						block == Blocks.LEAVES || 
						block == Blocks.LEAVES2
					)
				) || (						
					!ignoreSnow && 
					block == Blocks.SNOW_LAYER
				)
			);
        	if(!(ignoreLiquid && isLiquid))
        	{
            	if((findSolid && isSolid) || (findLiquid && isLiquid))
        		{
            		return i;
        		}
            	if((findSolid && isLiquid) || (findLiquid && isSolid))
            	{
            		// Found an illegal block (liquid when looking for solid, or vice-versa)
            		return -1;
            	}
        	}
        }

    	// Can happen if this is a chunk filled with air
        return -1;
	}
	
    // Faster than getHighestBlockYAt, but offers less precision. Used for resources
    // like oregen that need to find a starting point at the surface very often.
	@Override
	public int getHeightMapHeight(int x, int z, ChunkCoordinate chunkBeingPopulated)
	{
        // If the chunk exists or is inside the area being populated, fetch it normally.
        Chunk chunk = null;
    	if(
			(chunkBeingPopulated != null && OTG.IsInAreaBeingPopulated(x, z, chunkBeingPopulated))
			//|| getChunkGenerator().chunkExists(x, z)
		)
    	{
    		chunk = getChunkGenerator().getChunk(x, z);
    	}
		// If the chunk doesn't exist and we're doing something outside the
    	// population sequence, return the material without loading the chunk.
    	if(chunk == null && chunkBeingPopulated == null)
		{
    		// If the chunk has already been loaded, no need to use fake chunks.
			ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);
    		// If the chunk has already been loaded, no need to use fake chunks.
    		if(world.getChunkProviderServer().isLoaded(chunkCoord.getChunkX(), chunkCoord.getChunkZ()))    		
    		{
    			chunk = getChunkGenerator().getChunk(x, z);
    		} else {
    			// Calculate the height without loading the chunk.
    			//return generator.getHighestBlockYInUnloadedChunk(x, z, findSolid, findLiquid, ignoreLiquid, ignoreSnow);
    		}
    	}
		// Tried to query an unloaded chunk outside the area being populated
    	if(chunk == null)
    	{
            return -1;
    	}
    	
		// Get internal coordinates for block in chunk
        int internalX = x & 0xF;
        int internalZ = z & 0xF;

        int heightMapy = chunk.b(internalX, internalZ);
        
        // Fix for incorrect light map
        // TODO: Fix this properly?
        boolean incorrectHeightMap = false;
        while (heightMapy < getHeightCap() && chunk.a(internalX, heightMapy, internalZ).getMaterial().blocksLight())
        {
        	heightMapy++;
            incorrectHeightMap = true;
        }
        if (incorrectHeightMap)// && isSafeForLightUpdates(chunk, x, z))
        {
            // Let Minecraft know that it made an error
            world.w(new BlockPosition(x, heightMapy, z)); // world.relight
        }
        
        return heightMapy;
	}
        
    @Override
    public int getLightLevel(int x, int y, int z, ChunkCoordinate chunkBeingPopulated)
    {
    	if(y < PluginStandardValues.WORLD_DEPTH || y >= PluginStandardValues.WORLD_HEIGHT)
    	{
    		return -1;
    	}
    	
    	ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);
    	// We can't check light without loading the chunk, so never allow getLightLevel to load unloaded chunks.
    	// TODO: Check if this doesn't cause problems with BO3 LightChecks.
    	// TODO: Make a getLight method based on world.getLight that uses unloaded chunks.
    	if(
			(chunkBeingPopulated != null && OTG.IsInAreaBeingPopulated(x, z, chunkBeingPopulated))
			//|| getChunkGenerator().chunkExists(x, z)
    		|| (chunkBeingPopulated == null && world.getChunkProviderServer().isLoaded(chunkCoord.getChunkX(), chunkCoord.getChunkZ()))
		)
    	{
	        // This calculates the block and skylight as if it were day.
	        return world.j(new BlockPosition(x, y, z)); // world.getBlockAndSkyLightAsItWereDay
    	}
		return -1;
    }
    
	@Override
	public LocalMaterialData[] getBlockColumnInUnloadedChunk(int x, int z)
	{
    	//OTG.log(LogMarker.INFO, "getBlockColumn at X" + x + " Z" + z);
    	return generator.getBlockColumnInUnloadedChunk(x,z);
	}
    
    @Override
    public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, ChunkCoordinate chunkBeingPopulated)
    {
    	if(y < PluginStandardValues.WORLD_DEPTH || y >= PluginStandardValues.WORLD_HEIGHT)
    	{
    		return;
    	}

    	// If no chunk was passed, we're doing something outside of the population cycle.
    	// If a chunk was passed, only spawn in the area being populated, or existing chunks.
    	if(
			chunkBeingPopulated == null || 
			(
				OTG.IsInAreaBeingPopulated(x, z, chunkBeingPopulated) //|| 
				//getChunkGenerator().chunkExists(x, z)
			)
		)
    	{
    		this.getChunkGenerator().setBlock(x, y, z, material, metaDataTag);
    	}
    }   

	@Override
	public boolean generateModdedCaveGen(int x, int z, ChunkBuffer chunkBuffer)
	{
		return false;
	}

	@Override
	public boolean isInsideWorldBorder(ChunkCoordinate chunkCoordinate)
	{
		return true;
	}

	@Override
	public boolean isOTGPlus()
	{
		return this.getConfigs().getWorldConfig().isOTGPlus;
	}

	// Forge only atm, used to update the spawn point after populating the spawn chunk.
	@Override
	public void updateSpawnPointY(ChunkCoordinate chunkBeingPopulated)
	{
		// TODO: Implement this for spigot.
	}
}