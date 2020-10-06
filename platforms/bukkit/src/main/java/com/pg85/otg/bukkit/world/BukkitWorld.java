package com.pg85.otg.bukkit.world;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.server.v1_12_R1.*;
import net.minecraft.server.v1_12_R1.Entity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftGuardian;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_12_R1.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
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
import com.pg85.otg.util.minecraft.defaults.TreeType;

// TODO: Change localworld into abstract class and implement common logic there
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
    public OTGMansionGen woodLandMansionGen;

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

    // 32x32 biomes cache for fast lookups during population
	private LocalBiome[][] cachedBiomes;
    private boolean cacheIsValid;

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
                    this.woodLandMansionGen = new OTGMansionGen(settings);
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
            this.structureCache.reloadBo3StructureCache(this);
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
    public LocalBiome getBiomeForPopulation(int worldX, int worldZ, ChunkCoordinate chunkBeingPopulated)
    {
    	// Cache is invalidated when cascading chunkgen happens.
    	return !cacheIsValid ? getBiome(worldZ, worldX) : this.cachedBiomes[worldX - chunkBeingPopulated.getBlockX()][worldZ - chunkBeingPopulated.getBlockZ()];
    }

	@Override
	public void cacheBiomesForPopulation(ChunkCoordinate chunkCoord)
	{
		this.cachedBiomes = new LocalBiome[32][32];

		int areaSize = 32;
		for(int x = 0; x < areaSize; x++)
		{
			for(int z = 0; z < areaSize; z++)
			{
				this.cachedBiomes[x][z] = getBiome(chunkCoord.getBlockX() + x, chunkCoord.getBlockZ() + z);
			}
		}
		this.cacheIsValid = true;
	}

	// Population biome cache is invalidated when cascading chunkgen happens
	@Override
	public void invalidatePopulationBiomeCache()
	{
		this.cacheIsValid = false;
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
        	this.woodLandMansionGen.a(this.world, chunkX, chunkZ, null);
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
	
	// Used to make sure OTG+ structures don't spawn on top of default structures
	@Override
	public boolean chunkHasDefaultStructure(Random rand, ChunkCoordinate chunkCoord)
	{
        WorldConfig worldConfig = this.settings.getWorldConfig();
        // Allow OTG structures to spawn on top of strongholds
        // Allow OTG structures to spawn on top of mine shafts
        // isInsideStructure only detects structures that have structure starts saved to world, using custom method ><.
        return         		
    		(worldConfig.villagesEnabled && isStructureInRadius(chunkCoord, this.villageGen, 4)) || // TODO: Extra large villages aren't working?
    		(worldConfig.rareBuildingsEnabled && isStructureInRadius(chunkCoord, this.rareBuildingGen, 4)) ||
    		(worldConfig.netherFortressesEnabled && isStructureInRadius(chunkCoord, this.netherFortressGen, 4)) ||
    		(worldConfig.oceanMonumentsEnabled && isStructureInRadius(chunkCoord, this.oceanMonumentGen, 4)) ||
    		(worldConfig.woodLandMansionsEnabled && isStructureInRadius(chunkCoord, this.woodLandMansionGen, 4))
		;
	}
	
	static Method canSpawnStructureAtCoordsMethod;
    public boolean isStructureInRadius(ChunkCoordinate startChunk, StructureGenerator structure, int radiusInChunks)
    {    	
        if(canSpawnStructureAtCoordsMethod == null)
        {
	        try
	        {
	        	canSpawnStructureAtCoordsMethod = StructureGenerator.class.getDeclaredMethod("a", int.class, int.class);
	        	canSpawnStructureAtCoordsMethod.setAccessible(true);
	        } catch (NoSuchMethodException | SecurityException e) {
	        	OTG.log(LogMarker.ERROR, "Error, could not reflect canSpawnStructureAtCoords, BO4's may not be able to detect default/modded structures. OTG may not fully support your Spigot/Bukkit version.");
	        	e.printStackTrace();
	        }
        }
    	    	
        int chunkX = startChunk.getChunkX();
        int chunkZ = startChunk.getChunkZ();        
        for (int cycle = 0; cycle <= radiusInChunks; ++cycle)
        {
            for (int xRadius = -cycle; xRadius <= cycle; ++xRadius)
            {
                for (int zRadius = -cycle; zRadius <= cycle; ++zRadius)
                {
                    int distance = (int)Math.floor(Math.sqrt(Math.pow (chunkX-chunkX + xRadius, 2) + Math.pow (chunkZ-chunkZ + zRadius, 2)));                    
                    if (distance == cycle)
                    {
                    	boolean canSpawnStructureAtCoords = false;
						try
						{
							canSpawnStructureAtCoords = (boolean) canSpawnStructureAtCoordsMethod.invoke(structure, chunkX + xRadius, chunkZ + zRadius);
						}
						catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
						{
							OTG.log(LogMarker.ERROR, "Error, could not reflect canSpawnStructureAtCoords, BO4's may not be able to detect default/modded structures. OTG may not fully support your Spigot/Bukkit version.");
							e.printStackTrace();
						}
                    	if(canSpawnStructureAtCoords)
                    	{
                    		return true;
                    	}
                    }
                }
            }
        }
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
        	this.woodLandMansionGen.a(this.world, random, chunkIntPair);

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
    
    // Replace blocks / chc

	@Override
	public double getBiomeBlocksNoiseValue(int xInWorld, int zInWorld)
	{
		return this.getChunkGenerator().getBiomeBlocksNoiseValue(xInWorld, zInWorld);
	}

	// TODO: No longer needed, we're replacing blocks when placing them now.
	// Remove this after doing some profiling to compare performance.
    @Override
    public void replaceBlocks(ChunkCoordinate chunkCoord)
    {
    	if(1 == 1) { return; }

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
                    		biome.getBiomeConfig().replacedBlocks.parseForWorld(this);
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
        if(OTG.getPluginConfig().spawnLog)
        {
            OTG.log(LogMarker.DEBUG, "Attempting to spawn BO3 Entity() " + entityData.groupSize + " x " + entityData.name + " at " + entityData.x + " " + entityData.y + " " + entityData.z);
        }
        if (chunkBeingPopulated != null && !OTG.IsInAreaBeingPopulated((int) Math.floor(entityData.x), (int) Math.floor(entityData.z), chunkBeingPopulated)) {
            // If outside area being populated, abort and remove entity
            if(OTG.getPluginConfig().spawnLog)
            {
                OTG.log(LogMarker.DEBUG, "Tried to spawn entity "+ entityData.resourceLocation +"outside population bounds, aborting");
            }
            return;
        }
        if (entityData.y < 0 || entityData.y >= 256) {
            if(OTG.getPluginConfig().spawnLog)
            {
                OTG.log(LogMarker.ERROR, "Failed to spawn mob "+entityData.name +", spawn position out of bounds");
            }
            return;
        }

		String nameTag = entityData.nameTagOrNBTFileName;

		Entity entity = createEntityFromData(entityData);
        if(entity == null) return;

        // If either the block is a full block, or entity is a fish out of water, then we cancel
        org.bukkit.Material material = world.getWorld().getBlockAt(new Location(world.getWorld(), entityData.x, entityData.y, entityData.z)).getType();
        if (!material.isTransparent() || material.isSolid() ||
                ((entity.getBukkitEntity() instanceof CraftGuardian || EnumCreatureType.WATER_CREATURE.a().isAssignableFrom(entity.getClass())
                        && (material != org.bukkit.Material.WATER && material != org.bukkit.Material.STATIONARY_WATER))))
        {
            world.removeEntity(entity);
            return;
        }

        if(entity instanceof EntityLiving)
        {
            for (int r = 0; r < entityData.groupSize; r++)
            {
                if (r != 0) {
                    entity = createEntityFromData(entityData);
                    if (entity == null) {
                        return;
                    }
                }

                if(nameTag != null && !nameTag.toLowerCase().trim().endsWith(".txt") && !nameTag.toLowerCase().trim().endsWith(".nbt"))
                    entity.setCustomName(nameTag);

                ((CraftLivingEntity) CraftLivingEntity.getEntity(world.getServer(), entity))
                        .setRemoveWhenFarAway(false); // <- makes sure mobs don't de-spawn
            }
        } else {
            for (int r = 0; r < entityData.groupSize; r++)
            {
                if(r != 0)
                {
                    entity = createEntityFromData(entityData);
                    if (entity == null) continue;
                }

                if (entity instanceof EntityItemFrame)
                    if (((EntityItemFrame)entity).direction == null)
                        ((EntityItemFrame)entity).direction = EnumDirection.SOUTH;
            }
		}
	}

	private Entity createEntityFromData(EntityFunction<?> entityData) {
        Entity entity = null;
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        if(entityData.getMetaData() != null && entityData.getMetaData().trim().length() > 0)
        {
            try
            {
                nbttagcompound = JsonToNBT.getTagFromJson(entityData.getMetaData());
            }
            catch (NBTException nbtexception)
            {
                if(OTG.getPluginConfig().spawnLog)
                {
                    OTG.log(LogMarker.WARN, "Invalid NBT tag for mob in EntityFunction: " + entityData.getMetaData() + ". Skipping mob.");
                }
                return null;
            }
            // Specify which type of entity to spawn
            nbttagcompound.setString("id", entityData.resourceLocation);

            // Spawn entity, with potential passengers
            entity = ChunkRegionLoader.spawnEntity(nbttagcompound, world, entityData.x+0.5, entityData.y, entityData.z+0.5, true, CreatureSpawnEvent.SpawnReason.CUSTOM);
            if (entity == null) return null;

            if(nbttagcompound.hasKey("Facing"))
            {
                entity.setHeadRotation(EnumDirection.fromType1(nbttagcompound.getByte("Facing")).get2DRotationValue() * 90);
            }
            else if(nbttagcompound.hasKey("Rotation"))
            {
                entity.setHeadRotation(nbttagcompound.getByte("Rotation"));
            }
        } else {
            try
            {
                org.bukkit.entity.Entity e = world.getWorld().spawn(new Location(world.getWorld(), entityData.x+0.5, entityData.y+0.0, entityData.z+0.5), EntityType.fromName(entityData.name).getEntityClass());
                //entity = (Entity) EntityType.fromName(entityData.name).getEntityClass().getConstructor(new Class[] {World.class}).newInstance(world);
                entity = world.getEntity( e.getUniqueId());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        if(OTG.getPluginConfig().spawnLog)
        {
            OTG.log(LogMarker.DEBUG, "Spawned OK");
        }
        return entity;
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
    public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, ChunkCoordinate chunkBeingPopulated, boolean replaceBlocks)
    {
    	setBlock(x, y, z, material, metaDataTag, chunkBeingPopulated, null, replaceBlocks);
    }

    @Override
    public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, ChunkCoordinate chunkBeingPopulated, BiomeConfig biomeConfig, boolean replaceBlocks)
    {
    	if(y < PluginStandardValues.WORLD_DEPTH || y >= PluginStandardValues.WORLD_HEIGHT)
    	{
    		return;
    	}

    	if(material.isEmpty())
    	{
    		// Happens when configs contain blocks that don't exist.
    		// TODO: Catch this earlier up the chain, avoid doing work?
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
    		if(replaceBlocks)
    		{
        		if(biomeConfig == null)
        		{
        			if(chunkBeingPopulated == null)
        			{
        				biomeConfig = this.getBiome(x, z).getBiomeConfig();
        			} else {
        				biomeConfig = this.getBiomeForPopulation(x, z, chunkBeingPopulated).getBiomeConfig();
        			}
        		}
    			material = material.parseWithBiomeAndHeight(this, biomeConfig, y);
    		}
    		this.getChunkGenerator().setBlock(x, y, z, material, metaDataTag, biomeConfig);
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
	public boolean isBo4Enabled()
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