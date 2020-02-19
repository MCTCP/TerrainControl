package com.pg85.otg.forge.world;

import com.pg85.otg.*;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.common.WorldSession;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.biome.settings.ReplacedBlocksMatrix.ReplacedBlocksInstruction;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.customobjects.SpawnableObject;
import com.pg85.otg.customobjects.bofunctions.EntityFunction;
import com.pg85.otg.customobjects.structures.CustomStructureCache;
import com.pg85.otg.forge.biomes.ForgeBiome;
import com.pg85.otg.forge.biomes.ForgeBiomeRegistryManager;
import com.pg85.otg.forge.biomes.OTGBiome;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;
import com.pg85.otg.forge.generator.ForgeChunkBuffer;
import com.pg85.otg.forge.generator.OTGChunkGenerator;
import com.pg85.otg.forge.generator.structure.*;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.forge.util.IOHelper;
import com.pg85.otg.forge.util.MobSpawnGroupHelper;
import com.pg85.otg.generator.ChunkBuffer;
import com.pg85.otg.generator.ObjectSpawner;
import com.pg85.otg.generator.biome.BiomeGenerator;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.network.ClientConfigProvider;
import com.pg85.otg.network.ConfigProvider;
import com.pg85.otg.network.ServerConfigProvider;
import com.pg85.otg.util.BiomeIds;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.minecraft.defaults.StructureNames;
import com.pg85.otg.util.minecraft.defaults.TreeType;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureOceanMonument;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.util.*;

// TODO: Move this to com.pg85.otg.forge.world for 1.13. Has to be in com.pg85.otg.forge for Streams 1.12, which depends on it. 
public class ForgeWorld implements LocalWorld
{
    public static final int MAX_BIOMES_COUNT = 4096;
    private static final int MAX_SAVED_BIOMES_COUNT = 256;
    public static final int STANDARD_WORLD_HEIGHT = 128; // TODO: Why is this 128, should be 255?
    
	private ForgeWorldSession worldSession;
	public boolean isLoadedOnServer;	
	public int clientDimensionId = 0;
    private OTGChunkGenerator generator;
    public World world;
    private ConfigProvider settings;
    private CustomStructureCache structureCache;
    private String name;
    private long seed;
    private BiomeGenerator biomeGenerator;
    public HashMap<String, LocalBiome> biomeNames = new HashMap<String, LocalBiome>();
    private MapGenStructure strongholdGen;
    public MapGenStructure villageGen;
    private MapGenStructure mineshaftGen;
    private MapGenStructure rareBuildingGen;
    private MapGenBase cavesGen;
    private OTGNetherFortressGen netherFortressGen;
    private MapGenStructure oceanMonumentGen;
    private MapGenStructure woodLandMansionGen;
    private WorldGenDungeons dungeonGen;
    private WorldGenFossils fossilGen;
    private WorldGenTrees tree;
    private WorldGenSavannaTree acaciaTree;
    private WorldGenBigTree bigTree;
    private WorldGenBirchTree birchTree;
    private WorldGenTrees cocoaTree;
    private WorldGenCanopyTree darkOakTree;
    private WorldGenShrub groundBush;
    private WorldGenBigMushroom hugeRedMushroom;
    private WorldGenBigMushroom hugeBrownMushroom;
    private WorldGenMegaPineTree hugeTaigaTree1;
    private WorldGenMegaPineTree hugeTaigaTree2;
    private WorldGenMegaJungle jungleTree;
    private WorldGenBirchTree longBirchTree;
    private WorldGenSwamp swampTree;
    private WorldGenTaiga1 taigaTree1;
    private WorldGenTaiga2 taigaTree2;
    
    public ForgeWorld(String _name)
    {
		OTG.log(LogMarker.INFO, "Creating world \"" + _name + "\"");
        this.name = _name;
    }
    
    /**
     * Call this method when the Minecraft world is loaded. Call this method
     * after {@link #provideConfigs(ServerConfigProvider)} has been called.
     * @param world The Minecraft world.
     */
    public void provideWorldInstance(WorldServer world)
    {
        ServerConfigProvider configs = (ServerConfigProvider) this.settings;
        DimensionConfig dimConfig = OTG.getDimensionsConfig().getDimensionConfig(WorldHelper.getName(world));        

        this.world = world;
        OTGDimensionManager.ApplyGameRulesToWorld(world, dimConfig);
        this.seed = world.getWorldInfo().getSeed();
        world.setSeaLevel(configs.getWorldConfig().waterLevelMax);

        this.dungeonGen = new WorldGenDungeons();
        this.fossilGen = new WorldGenFossils();
        this.netherFortressGen = new OTGNetherFortressGen(this);
        
        this.cavesGen = net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(null, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.CAVE);
        this.strongholdGen = (MapGenStructure)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(new OTGStrongholdGen(configs, world), net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.STRONGHOLD);
        this.villageGen = (MapGenStructure)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(new OTGVillageGen(configs, this), net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.VILLAGE);
        this.mineshaftGen = (MapGenStructure)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(new OTGMineshaftGen(this), net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.MINESHAFT);        
        this.rareBuildingGen = (MapGenStructure)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(new OTGRareBuildingGen(configs, this), net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.SCATTERED_FEATURE);
        this.oceanMonumentGen = (MapGenStructure)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(new OTGOceanMonumentGen(configs, this), net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.OCEAN_MONUMENT);
        this.woodLandMansionGen = (MapGenStructure)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(new OTGWoodLandMansionGen(configs, this), net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.WOODLAND_MANSION);
        
        IBlockState jungleLog = Blocks.LOG.getDefaultState()
                .withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);
        IBlockState jungleLeaves = Blocks.LEAVES.getDefaultState()
                .withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE)
                .withProperty(BlockLeaves.CHECK_DECAY, false);

        this.tree = new WorldGenTrees(false);
        this.acaciaTree = new WorldGenSavannaTree(false);
        this.cocoaTree = new WorldGenTrees(false, 5, jungleLog, jungleLeaves, true);
        this.bigTree = new WorldGenBigTree(false);
        this.birchTree = new WorldGenBirchTree(false, false);
        this.darkOakTree = new WorldGenCanopyTree(false);
        this.longBirchTree = new WorldGenBirchTree(false, true);
        this.swampTree = new WorldGenSwamp();
        this.taigaTree1 = new WorldGenTaiga1();
        this.taigaTree2 = new WorldGenTaiga2(false);
        this.hugeRedMushroom = new WorldGenBigMushroom(Blocks.RED_MUSHROOM_BLOCK);
        this.hugeBrownMushroom = new WorldGenBigMushroom(Blocks.BROWN_MUSHROOM_BLOCK);
        this.hugeTaigaTree1 = new WorldGenMegaPineTree(false, false);
        this.hugeTaigaTree2 = new WorldGenMegaPineTree(false, true);
        this.jungleTree = new WorldGenMegaJungle(false, 10, 20, jungleLog, jungleLeaves);
        this.groundBush = new WorldGenShrub(jungleLog, jungleLeaves);

        this.generator = new OTGChunkGenerator(this);

        this.worldSession = new ForgeWorldSession(this);
        this.structureCache = new CustomStructureCache(this);
    }

    @SideOnly(Side.CLIENT)
    public void provideClientConfigsBukkit(WorldClient world, ClientConfigProvider config)
    {
        this.settings = config;
        this.world = world;
        this.seed = world.getSeed();
    }

    @SideOnly(Side.CLIENT)
    public void provideClientConfigs(ClientConfigProvider config)
    {
        this.settings = config;
        this.worldSession = new ForgeWorldSession(this);
    }

    @SideOnly(Side.CLIENT)
    public void provideClientWorld(World world)
    {
        this.world = world;
        this.seed = world.getSeed();
    }

    /**
     * Call this method when the configs are loaded.
     * @param configs The configs.
     */
    public void provideConfigs(ServerConfigProvider configs)
    {
        this.settings = configs;
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
    public String getWorldSettingsName()
    {
        return this.getWorld().getWorldInfo().getWorldName();
    }

    @Override
    public long getSeed()
    {
        return this.seed;
    }

    @Override
    public int getHeightCap()
    {
        return this.settings.getWorldConfig().worldHeightCap;
    }

    @Override
    public int getHeightScale()
    {
        return this.settings.getWorldConfig().worldHeightScale;
    }

    public OTGChunkGenerator getChunkGenerator()
    {
        return this.generator;
    }

    public void setBiomeGenerator(BiomeGenerator generator)
    {
        this.biomeGenerator = generator;
    }

    public World getWorld()
    {
        return this.world;
    }
        
    @Override
    public CustomStructureCache getStructureCache()
    {
        return this.structureCache;
    }

    @Override
    public ObjectSpawner getObjectSpawner()
    {
        return this.generator.spawner;
    }

    @Override
    public BiomeGenerator getBiomeGenerator()
    {
        return this.biomeGenerator;
    }
       
	@Override
	public File getWorldSaveDir()
	{
		return this.getWorld().getSaveHandler().getWorldDirectory();
	}

	@Override
	public int getDimensionId()
	{
		return getWorld().provider.getDimension();
	}
    
	@Override
	public void updateSpawnPointY(ChunkCoordinate chunkBeingPopulated)
	{		
        // Spawn point is only saved for overworld by MC, 
        // so we have to save it ourselves for dimensions.
        // Use the dimensionconfig
		DimensionConfig dimConfig = OTG.getDimensionsConfig().getDimensionConfig(this.getName());
		if(!dimConfig.Settings.SpawnPointSet)
		{
			BlockPos spawnPos = this.getSpawnPoint();
			int highestY = this.getHighestBlockAboveYAt(spawnPos.getX(), spawnPos.getZ(), chunkBeingPopulated);
			this.world.getWorldInfo().setSpawn(new BlockPos(spawnPos.getX(), highestY, spawnPos.getZ()));
	        dimConfig.Settings.SpawnPointSet = true; // TODO: This breaks getrandomisedspawnpoint and spawns the player at the exact same coords each time.
	        dimConfig.Settings.SpawnPointX = spawnPos.getX();
	        dimConfig.Settings.SpawnPointY = highestY;
	        dimConfig.Settings.SpawnPointZ = spawnPos.getZ();
	        OTG.getDimensionsConfig().save();
		}
	}
	
    public BlockPos getSpawnPoint()
    {
    	return world.provider.getSpawnPoint();
    }
    
    @Override
    public void startPopulation(ChunkCoordinate chunkCoord) { }

    @Override
    public void endPopulation() { }
    
    // World session
    
    @Override
	public WorldSession getWorldSession()
	{
		return worldSession;
	}

	@Override
	public void deleteWorldSessionData()
	{
		// getWorld == null can happen for MP clients when deleting dimensions that were never entered. 
		// No files need to be deleted on the client though.
		// TODO: Make this method Server side only (adding annotation causes bug ><).
		if(getWorld() != null)
		{
			int dimensionId = getWorld().provider.getDimension();
			File worldDataDir = new File(getWorld().getSaveHandler().getWorldDirectory() + File.separator + "OpenTerrainGenerator" + File.separator + (dimensionId != 0 ? "DIM-" + dimensionId + File.separator : ""));
			if(worldDataDir.exists())
			{
				IOHelper.deleteRecursive(worldDataDir);
			}
		}
	}
    
    // Biomes
    
	public Biome getBiomeFromChunk(int blockX, int blockZ)
	{
		if(this.getWorld().isBlockLoaded(new BlockPos(blockX,255,blockZ)))
		{
			Chunk chunk = this.getWorld().getChunk(new BlockPos(blockX, 0, blockZ));
			if(chunk != null && !(chunk instanceof EmptyChunk))
			{
				byte[] blockBiomeArray = chunk.getBiomeArray();
		        int i = blockX & 15;
		        int j = blockZ & 15;
		        int biomeId = blockBiomeArray[j << 4 | i] & 255;
		        return Biome.getBiome(biomeId);			
			}
		}
		return null;
	}
	
    @Override
    public LocalBiome getCalculatedBiome(int x, int z)
    {
    	return getBiomeByOTGIdOrNull(this.biomeGenerator.getBiome(x, z));
    }
    
    @Override
    public LocalBiome getBiome(int x, int z)
    {
    	// TODO: Fix populateUsingSavedBiomes, just fixing this method may not work though, as the biome gen now uses otg biome id's everywhere.
        //if (this.settings.getWorldConfig().populateUsingSavedBiomes)
        //{
        	//return getSavedBiome(x, z);
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
    
    @Override
    public ArrayList<LocalBiome> getAllBiomes()
    {
    	ArrayList<LocalBiome> biomes = new ArrayList<LocalBiome>();
		for(LocalBiome biome : this.settings.getBiomeArrayByOTGId())
		{
			if(biome != null)
			{
				biomes.add(biome);
			}
		}
    	return biomes;
    }

	@Override
	public LocalBiome getFirstBiomeOrNull() {
		return this.biomeNames.size() > 0 ? (LocalBiome) this.biomeNames.values().toArray()[0] : null;
	}
    
    @Override
    public ForgeBiome getBiomeByOTGIdOrNull(int id)
    {
        return (ForgeBiome) this.settings.getBiomeByOTGIdOrNull(id);
    }

    @Override
    public LocalBiome getBiomeByNameOrNull(String name)
    {
        return this.biomeNames.get(name);
    }
    
    @Override
    public LocalBiome createBiomeFor(BiomeConfig biomeConfig, BiomeIds biomeIds, ConfigProvider configProvider, boolean isReload)
    {
    	ForgeBiome forgeBiome = ForgeBiomeRegistryManager.getOrCreateBiome(biomeConfig, biomeIds, this.getName(), configProvider);
        this.biomeNames.put(forgeBiome.getName(), forgeBiome);
        return forgeBiome;
    }
    
	@Override
	public int getRegisteredBiomeId(String resourceLocationString)
	{
		return ForgeBiomeRegistryManager.getRegisteredBiomeId(resourceLocationString, this.getName());
	}
    
	public void unRegisterBiomes()
	{
		ForgeBiomeRegistryManager.unregisterBiomes(this.biomeNames, this);
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
    
    // Chunks

    @Override
    public ChunkCoordinate getSpawnChunk()
    {
    	BlockPos spawnPos = getSpawnPoint();
    	return ChunkCoordinate.fromBlockCoords(spawnPos.getX(), spawnPos.getZ());
    }
    
    @Override
	public boolean isInsidePregeneratedRegion(ChunkCoordinate chunk)
	{
		return getWorldSession().isInsidePregeneratedRegion(chunk);
	}

    // Blocks

    @Override
    public int getLightLevel(int x, int y, int z, ChunkCoordinate chunkBeingPopulated)
    {   	
    	if(y < PluginStandardValues.WORLD_DEPTH || y >= PluginStandardValues.WORLD_HEIGHT)
    	{
    		return -1;
    	}
    	
    	// We can't check light without loading the chunk, so never allow getLightLevel to load unloaded chunks.
    	// TODO: Check if this doesn't cause problems with BO3 LightChecks.
    	// TODO: Make a getLight method based on world.getLight that uses unloaded chunks.
    	if(
			(chunkBeingPopulated != null && OTG.IsInAreaBeingPopulated(x, z, chunkBeingPopulated))
			//|| getChunkGenerator().chunkExists(x, z)
    		|| (chunkBeingPopulated == null && world.isBlockLoaded(new BlockPos(x,255,z)))
		)
    	{
	        // This calculates the block and skylight as if it were day.
	        return this.world.getLight(new BlockPos(x, y, z));
    	}
		return -1;
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
    	return getHighestBlockYAt(x, z, true, true, false, false, false, chunkBeingPopulated) + 1;
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
    		// If the chunk has already been loaded, no need to use fake chunks.
    		if(world.isBlockLoaded(new BlockPos(x,255,z)))
    		{
    			chunk = getChunkGenerator().getChunk(x, z);
    		} else {
    			// Calculate the height without loading the chunk.
    			return generator.getHighestBlockYInUnloadedChunk(x, z, findSolid, findLiquid, ignoreLiquid, ignoreSnow);
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

        int heightMapy = chunk.getHeightValue(internalX, internalZ);

        // Fix for incorrect light map
        // TODO: Fix this properly?
        boolean incorrectHeightMap = false;
        while (heightMapy < getHeightCap() && chunk.getBlockState(internalX, heightMapy, internalZ).getMaterial().blocksLight())
        {
        	heightMapy++;
            incorrectHeightMap = true;
        }
        if (incorrectHeightMap)
        {
            // Let Minecraft know that it made an error
            this.world.checkLight(new BlockPos(x, heightMapy, z));
        }
         
        ForgeMaterialData material;
        boolean isSolid;
        boolean isLiquid;
        IBlockState blockState;
        Block block;
        
        for(int i = heightMapy; i >= 0; i--)
        {
        	blockState = chunk.getBlockState(internalX, i, internalZ);
        	block = blockState.getBlock();
    		material = ForgeMaterialData.ofMinecraftBlockState(blockState);
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
    		if(world.isBlockLoaded(new BlockPos(x,255,z)))
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

        int heightMapy = chunk.getHeightValue(internalX, internalZ);

        // Fix for incorrect light map
        // TODO: Fix this properly?
        boolean incorrectHeightMap = false;
        while (heightMapy < getHeightCap() && chunk.getBlockState(internalX, heightMapy, internalZ).getMaterial().blocksLight())
        {
        	heightMapy++;
            incorrectHeightMap = true;
        }
        if (incorrectHeightMap)
        {
            // Let Minecraft know that it made an error
            this.world.checkLight(new BlockPos(x, heightMapy, z));
        }
        
        return heightMapy;
	}
    
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
    		// If the chunk has already been loaded, no need to use fake chunks.
    		if(world.isBlockLoaded(new BlockPos(x,255,z)))
    		{
    			chunk = getChunkGenerator().getChunk(x, z);
    		} else {
    			// Calculate the material without loading the chunk.
    			return generator.getMaterialInUnloadedChunk(x,y,z);
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
        return ForgeMaterialData.ofMinecraftBlockState(chunk.getBlockState(internalX, y, internalZ));		               
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

    // Structures / trees

    @Override
    public boolean placeDungeon(Random rand, int x, int y, int z)
    {
    	if(y < PluginStandardValues.WORLD_DEPTH || y >= PluginStandardValues.WORLD_HEIGHT)
    	{
    		return false;
    	}
    	
   		return this.dungeonGen.generate(this.world, rand, new BlockPos(x, y, z));
    }

    @Override
    public boolean placeFossil(Random rand, ChunkCoordinate chunkCoord)
    {
        return this.fossilGen.generate(this.world, rand, new BlockPos(chunkCoord.getBlockX(), 0, chunkCoord.getBlockZ()));
    }

    @Override
    public boolean placeTree(TreeType type, Random rand, int x, int y, int z)
    {
    	if(y < PluginStandardValues.WORLD_DEPTH || y >= PluginStandardValues.WORLD_HEIGHT)
    	{
    		return false;
    	}
        BlockPos blockPos = new BlockPos(x, y, z);
        try
        {
	        switch (type)
	        {
	            case Tree:
	                return this.tree.generate(this.world, rand, blockPos);
	            case BigTree:
	                return this.bigTree.generate(this.world, rand, blockPos);
	            case Forest:
	            case Birch:
	                return this.birchTree.generate(this.world, rand, blockPos);
	            case TallBirch:
	                return this.longBirchTree.generate(this.world, rand, blockPos);
	            case HugeMushroom:
	                if (rand.nextBoolean())
	                {
	                    return this.hugeBrownMushroom.generate(this.world, rand, blockPos);
	                } else {
	                    return this.hugeRedMushroom.generate(this.world, rand, blockPos);
	                }
	            case HugeRedMushroom:
	                return this.hugeRedMushroom.generate(this.world, rand, blockPos);
	            case HugeBrownMushroom:
	                return this.hugeBrownMushroom.generate(this.world, rand, blockPos);
	            case SwampTree:
	                return this.swampTree.generate(this.world, rand, blockPos);
	            case Taiga1:
	                return this.taigaTree1.generate(this.world, rand, blockPos);
	            case Taiga2:
	                return this.taigaTree2.generate(this.world, rand, blockPos);
	            case JungleTree:
	                return this.jungleTree.generate(this.world, rand, blockPos);
	            case GroundBush:
	                return this.groundBush.generate(this.world, rand, blockPos);
	            case CocoaTree:
	                return this.cocoaTree.generate(this.world, rand, blockPos);
	            case Acacia:
	                return this.acaciaTree.generate(this.world, rand, blockPos);
	            case DarkOak:
	                return this.darkOakTree.generate(this.world, rand, blockPos);
	            case HugeTaiga1:
	                return this.hugeTaigaTree1.generate(this.world, rand, blockPos);
	            case HugeTaiga2:
	                return this.hugeTaigaTree2.generate(this.world, rand, blockPos);
	            default:
	                throw new RuntimeException("Failed to handle tree of type " + type.toString());
	        }
        }
        catch(NullPointerException ex)
        {
        	// Have only seen this happen once while flying backwards, tree spawning causes cascading chunkgen, 
        	// nullreference when trying to query world for blockstate at pos.
        	OTG.log(LogMarker.WARN, "Treegen caused a non-fatal exception, likely due to cascading chunkgen: ");
        	ex.printStackTrace();
        	return true; // Return true to prevent further attempts.
        	// TODO: Fix this properly, somewhere either coords are outside of bounds or we're making an 
        	// incorrect assumption about which chunks are available.
        }
    }
    
    @Override
    public void prepareDefaultStructures(int chunkX, int chunkZ, boolean dry)
    {
        WorldConfig worldConfig = this.settings.getWorldConfig();
        if (worldConfig.strongholdsEnabled)
            this.strongholdGen.generate(this.world, chunkX, chunkZ, null);
        if (worldConfig.mineshaftsEnabled)
            this.mineshaftGen.generate(this.world, chunkX, chunkZ, null);
        if (worldConfig.villagesEnabled && dry)
            this.villageGen.generate(this.world, chunkX, chunkZ, null);
        if (worldConfig.rareBuildingsEnabled)
            this.rareBuildingGen.generate(this.world, chunkX, chunkZ, null);
        if (worldConfig.netherFortressesEnabled)
            this.netherFortressGen.generate(this.world, chunkX, chunkZ, null);
        if (worldConfig.oceanMonumentsEnabled)
           this.oceanMonumentGen.generate(this.world, chunkX, chunkZ, null);
        if (worldConfig.woodLandMansionsEnabled)
            this.woodLandMansionGen.generate(this.world, chunkX, chunkZ, null);
    }

    @Override
    public boolean placeDefaultStructures(Random rand, ChunkCoordinate chunkCoord)
    {
        ChunkPos chunkCoordIntPair = new ChunkPos(chunkCoord.getChunkX(), chunkCoord.getChunkZ());
        WorldConfig worldConfig = this.settings.getWorldConfig();

        boolean isVillagePlaced = false;
        if (worldConfig.strongholdsEnabled)
            this.strongholdGen.generateStructure(this.world, rand, chunkCoordIntPair);
        if (worldConfig.mineshaftsEnabled)
            this.mineshaftGen.generateStructure(this.world, rand, chunkCoordIntPair);
        if (worldConfig.villagesEnabled)
            isVillagePlaced = this.villageGen.generateStructure(this.world, rand, chunkCoordIntPair);
        if (worldConfig.rareBuildingsEnabled)
            this.rareBuildingGen.generateStructure(this.world, rand, chunkCoordIntPair);
        if (worldConfig.netherFortressesEnabled)
            this.netherFortressGen.generateStructure(this.world, rand, chunkCoordIntPair);
        if (worldConfig.oceanMonumentsEnabled)
            this.oceanMonumentGen.generateStructure(this.world, rand, chunkCoordIntPair);
        if (worldConfig.woodLandMansionsEnabled)
            this.woodLandMansionGen.generateStructure(this.world, rand, chunkCoordIntPair);

        return isVillagePlaced;
    }
        
    /**
     * Recreates data about structures intersecting given chunk (used for example by getPossibleCreatures), without
     * placing any blocks. When called for the first time before any chunk is generated - also initializes the internal
     * state needed by getPossibleCreatures.
     */
    public void recreateStructures(Chunk chunkIn, int chunkX, int chunkZ)
    {
        // recreateStructures
        WorldConfig worldConfig = this.getConfigs().getWorldConfig();

        if (worldConfig.mineshaftsEnabled)
        {
            this.mineshaftGen.generate(this.getWorld(), chunkX, chunkZ, null);
        }
        if (worldConfig.villagesEnabled)
        {
            this.villageGen.generate(this.getWorld(), chunkX, chunkZ, null);
        }
        if (worldConfig.strongholdsEnabled)
        {
            this.strongholdGen.generate(this.getWorld(), chunkX, chunkZ, null);
        }
        if (worldConfig.rareBuildingsEnabled)
        {
            this.rareBuildingGen.generate(this.getWorld(), chunkX, chunkZ, null);
        }
        if (worldConfig.netherFortressesEnabled)
        {
            this.netherFortressGen.generate(this.getWorld(), chunkX, chunkZ, null);
        }
        if (worldConfig.oceanMonumentsEnabled)
        {
            this.oceanMonumentGen.generate(this.getWorld(), chunkX, chunkZ, null);
        }
        if (worldConfig.woodLandMansionsEnabled)
        {
            this.woodLandMansionGen.generate(this.getWorld(), chunkX, chunkZ, null);
        }
    }
    
    @Override
    public SpawnableObject getMojangStructurePart(String name)
    {
        ResourceLocation resourceLocation = new ResourceLocation(name);
        TemplateManager mojangStructureParts = this.world.getSaveHandler().getStructureTemplateManager();
        Template mojangStructurePart = mojangStructureParts.getTemplate(this.world.getMinecraftServer(), resourceLocation);
        if (mojangStructurePart == null)
        {
            return null;
        }
        return new MojangStructurePart(name, mojangStructurePart);
    }
    
	// Used to make sure OTG+ structures don't spawn on top of default structures
	@Override
	public boolean chunkHasDefaultStructure(Random rand, ChunkCoordinate chunk)
	{
        WorldConfig worldConfig = this.settings.getWorldConfig();
        BlockPos blockPos = new BlockPos(chunk.getBlockXCenter(), 0, chunk.getBlockZCenter());
        // Allow OTG structures to spawn on top of strongholds
        // Allow OTG structures to spawn on top of mine shafts
        // TODO: VIllage gen detection doesn't appear to be working too well, fix..
        return 
		(worldConfig.villagesEnabled && this.villageGen instanceof OTGVillageGen && ((OTGVillageGen)this.villageGen).isInsideStructure(blockPos)) ||
		(worldConfig.villagesEnabled && !(this.villageGen instanceof OTGVillageGen) && this.villageGen.isInsideStructure(blockPos)) ||
        (worldConfig.rareBuildingsEnabled && this.rareBuildingGen instanceof OTGRareBuildingGen && ((OTGRareBuildingGen)this.rareBuildingGen).isInsideStructure(blockPos)) ||
        (worldConfig.rareBuildingsEnabled && !(this.rareBuildingGen instanceof OTGRareBuildingGen) && this.rareBuildingGen.isInsideStructure(blockPos)) ||
        (worldConfig.netherFortressesEnabled && this.netherFortressGen.isInsideStructure(blockPos)) ||
        (worldConfig.oceanMonumentsEnabled && this.oceanMonumentGen instanceof OTGOceanMonumentGen && ((OTGOceanMonumentGen)this.oceanMonumentGen).isInsideStructure(blockPos)) ||
        (worldConfig.oceanMonumentsEnabled && !(this.oceanMonumentGen instanceof OTGOceanMonumentGen) && this.oceanMonumentGen.isInsideStructure(blockPos)) ||
        (worldConfig.woodLandMansionsEnabled && this.woodLandMansionGen instanceof OTGWoodLandMansionGen && ((OTGWoodLandMansionGen)this.woodLandMansionGen).isInsideStructure(blockPos)) ||
        (worldConfig.woodLandMansionsEnabled && !(this.woodLandMansionGen instanceof OTGWoodLandMansionGen) && this.woodLandMansionGen.isInsideStructure(blockPos))
        ;
	}
	
	// TODO: No clue what this is used for, leads to some MC advancements "test" code. 
    public boolean isInsideStructure(String structureName, BlockPos pos)
    {
        //if (!this.mapFeaturesEnabled)
        {
            //return false;
        }
        //else
        if ((StructureNames.STRONGHOLD.equals(structureName)) && (this.strongholdGen != null))
        {
        	// TODO: Override and implement isInsideStructure?
            return this.strongholdGen.isInsideStructure(pos);
        }
        else if ((StructureNames.WOODLAND_MANSION.equals(structureName)) && (this.woodLandMansionGen != null))
        {
        	// TODO: Override and implement isInsideStructure?
            return this.woodLandMansionGen.isInsideStructure(pos);
        }
        else if ((StructureNames.OCEAN_MONUMENT.equals(structureName)) && (this.oceanMonumentGen != null))
        {
        	// TODO: Override and implement isInsideStructure?
            return this.oceanMonumentGen.isInsideStructure(pos);
        }
        else if (((StructureNames.VILLAGE.equals(structureName)) || ("Village".equals(structureName))) && (this.villageGen != null))
        {
        	// TODO: Override and implement isInsideStructure?
            return this.villageGen.isInsideStructure(pos);
        }
        else if ((StructureNames.MINESHAFT.equals(structureName)) && (this.mineshaftGen != null))
        {
        	// TODO: Override and implement isInsideStructure?
            return this.mineshaftGen.isInsideStructure(pos);
        }
        else if (((StructureNames.RARE_BUILDING.equals(structureName))|| ("Temple".equals(structureName))) && (this.rareBuildingGen != null))
        {
        	// TODO: Override and implement isInsideStructure?
            return this.rareBuildingGen.isInsideStructure(pos);
        }

    	return false;
    }

    public BlockPos getNearestStructurePos(String structureName, BlockPos blockPos, boolean p_180513_4_)
    {
    	//if(!this.mapFeaturesEnabled == null)
    	{
	        // Gets the nearest stronghold
	        if ((StructureNames.STRONGHOLD.equals(structureName)) && (this.strongholdGen != null))
	        {
	            return this.strongholdGen.getNearestStructurePos(this.getWorld(), blockPos, p_180513_4_);
	        }
	        if ((StructureNames.WOODLAND_MANSION.equals(structureName)) && (this.woodLandMansionGen != null))
	        {
	            return this.woodLandMansionGen.getNearestStructurePos(this.getWorld(), blockPos, p_180513_4_);
	        }
	        if ((StructureNames.OCEAN_MONUMENT.equals(structureName)) && (this.oceanMonumentGen != null))
	        {
	            return this.oceanMonumentGen.getNearestStructurePos(this.getWorld(), blockPos, p_180513_4_);
	        }
	        if (((StructureNames.VILLAGE.equals(structureName)) || ("Village".equals(structureName))) && (this.villageGen != null))
	        {
	            return this.villageGen.getNearestStructurePos(this.getWorld(), blockPos, p_180513_4_);
	        }
	        if ((StructureNames.MINESHAFT.equals(structureName)) && (this.mineshaftGen != null))
	        {
	            return this.mineshaftGen.getNearestStructurePos(this.getWorld(), blockPos, p_180513_4_);
	        }
	        if (((StructureNames.RARE_BUILDING.equals(structureName))|| ("Temple".equals(structureName))) && (this.rareBuildingGen != null))
	        {
	            return this.rareBuildingGen.getNearestStructurePos(this.getWorld(), blockPos, p_180513_4_);
	        }
    	}

        return null;
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
        int worldStartX = rawChunk.x * 16;
        int worldStartZ = rawChunk.z * 16;

        ExtendedBlockStorage[] sectionsArray = rawChunk.getBlockStorageArray();
        ReplacedBlocksInstruction[] replaceArray;
        IBlockState block;
        int blockId = 0;
    	int minHeight;
    	int maxHeight;
    	LocalBiome biome;
    	int y;
    	ReplacedBlocksInstruction[][][] replaceInstructionsCache = new ReplacedBlocksInstruction[16][16][];
        
        for (ExtendedBlockStorage section : sectionsArray)
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
                        	y = section.getYLocation() + sectionY;                    
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
		                                    	block = section.getData().get(sectionX, sectionY, sectionZ);
		                                    	blockId = Block.getIdFromBlock(block.getBlock());
	                            			}
			                            	if(instruction.getFrom().getBlockId() == blockId)
			                            	{	                            		
			                                    section.set(sectionX, sectionY, sectionZ, ((ForgeMaterialData)instruction.getTo()).internalBlock());                            		
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

    // Mob spawning
    
    @Override
    public void placePopulationMobs(LocalBiome biome, Random random, ChunkCoordinate chunkCoord)
    {
        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this.getChunkGenerator(), this.world, random, chunkCoord.getChunkX(), chunkCoord.getChunkZ(), false, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.ANIMALS))
        {
	        WorldEntitySpawner.performWorldGenSpawning(this.getWorld(), ((ForgeBiome) biome).getHandle(), chunkCoord.getBlockXCenter(), chunkCoord.getBlockZCenter(), ChunkCoordinate.CHUNK_X_SIZE, ChunkCoordinate.CHUNK_Z_SIZE, random);
        }
    }

    public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType paramaca, BlockPos blockPos)
    {
        WorldConfig worldConfig = this.getConfigs().getWorldConfig();
        Biome biomeBaseOTG = ((ForgeBiome)this.getBiome(blockPos.getX(), blockPos.getZ())).biomeBase;
        
        if (worldConfig.rareBuildingsEnabled)
        {
            if (
        		paramaca == EnumCreatureType.MONSTER && 
            	(
	        		(
	        			this.rareBuildingGen instanceof OTGRareBuildingGen && 
	        			((OTGRareBuildingGen)this.rareBuildingGen).isSwampHutAtLocation(blockPos)
	    			) ||
	        		(
	        			!(this.rareBuildingGen instanceof OTGRareBuildingGen) && 
	        			((MapGenScatteredFeature)this.rareBuildingGen).isSwampHut(blockPos)	        				
					)
        		)
        	)
            {
                return (this.rareBuildingGen instanceof OTGRareBuildingGen) ? ((OTGRareBuildingGen)this.rareBuildingGen).getMonsterSpawnList() : ((MapGenScatteredFeature)this.rareBuildingGen).getMonsters();
            }
        }
        if (worldConfig.oceanMonumentsEnabled)
        {
            if (paramaca == EnumCreatureType.MONSTER && this.oceanMonumentGen.isPositionInStructure(this.getWorld(), blockPos))
            {
                return (this.oceanMonumentGen instanceof OTGOceanMonumentGen) ? ((OTGOceanMonumentGen)this.oceanMonumentGen).getMonsterSpawnList() : ((StructureOceanMonument)this.oceanMonumentGen).getMonsters();
            }
        }

        return biomeBaseOTG.getSpawnableList(paramaca);
    }    
    
    // Entity spawning
    	
    @Override
    public void spawnEntity(EntityFunction<?> entityData, ChunkCoordinate chunkBeingPopulated)
    { 	
    	if(OTG.getPluginConfig().spawnLog)
    	{
    		OTG.log(LogMarker.DEBUG, "Attempting to spawn BO3 Entity() " + entityData.groupSize + " x " + entityData.mobName + " at " + entityData.x + " " + entityData.y + " " + entityData.z);
    	}

    	Random rand = new Random();

		String mobTypeName = entityData.mobName;
		int groupSize = entityData.groupSize;
		String nameTag = entityData.nameTagOrNBTFileName;
        Class<? extends Entity> entityClass = MobSpawnGroupHelper.toMinecraftClass(mobTypeName);
    	
        if(entityClass == null)
        {
        	if(OTG.getPluginConfig().spawnLog)
        	{
        		OTG.log(LogMarker.WARN, "Could not find entity: " + mobTypeName);
        	}
        	return;
        }
        
		ResourceLocation entityResourceLocation = MobSpawnGroupHelper.resourceLocationFromMinecraftClass(entityClass);

        Entity entityliving = null;
        float rotationFromNbt = 0;
        boolean rotationFromNbtSet = false;

        if(entityData.nameTagOrNBTFileName != null && (entityData.nameTagOrNBTFileName.toLowerCase().trim().endsWith(".txt") || entityData.nameTagOrNBTFileName.toLowerCase().trim().endsWith(".nbt")))
        {
        	NBTTagCompound nbttagcompound = new NBTTagCompound();

	        try
	        {
	            NBTBase nbtbase = JsonToNBT.getTagFromJson(entityData.getMetaData());

	            if (!(nbtbase instanceof NBTTagCompound))
	            {
	            	if(OTG.getPluginConfig().spawnLog)
	            	{
	            		OTG.log(LogMarker.WARN, "Invalid NBT tag for mob in EntityFunction: " + entityData.getMetaData() + ". Skipping mob.");
	            	}
		        	return;
	            }

	            nbttagcompound = (NBTTagCompound)nbtbase;
	        }
	        catch (NBTException nbtexception)
	        {
	        	if(OTG.getPluginConfig().spawnLog)
	        	{
	        		OTG.log(LogMarker.WARN, "Invalid NBT tag for mob in EntityFunction: " + entityData.getMetaData() + ". Skipping mob.");
	        	}
	        	return;
	        }

	        nbttagcompound.setString("id", entityResourceLocation.toString());
	        entityliving = EntityList.createEntityFromNBT(nbttagcompound, world);
	        // TODO: Rotated item frames don't stick to walls correctly or don't pop off when their support block is removed.
	        if(nbttagcompound.hasKey("Facing"))
	        {
	        	EnumFacing facing = EnumFacing.byIndex(nbttagcompound.getByte("Facing"));
	        	rotationFromNbt = facing.getHorizontalIndex() * 90;
	        	rotationFromNbtSet = true;
	        }
	        
        } else {
	        try
	        {
	            entityliving = (Entity) entityClass.getConstructor(new Class[] {World.class}).newInstance(new Object[] { world });
	        }
	        catch (Exception exception)
	        {
	            exception.printStackTrace();
	            return;
	        }
        }

        if(entityliving != null)
        {
            EnumCreatureType creatureType = EnumCreatureType.MONSTER;
            if(!entityliving.isCreatureType(creatureType, false))
            {
            	creatureType = EnumCreatureType.CREATURE;
            	if(!entityliving.isCreatureType(creatureType, false))
            	{
            		creatureType = EnumCreatureType.AMBIENT;
            		if(!entityliving.isCreatureType(creatureType, false))
            		{
                		creatureType = EnumCreatureType.WATER_CREATURE;
                		if(!entityliving.isCreatureType(creatureType, false))
                		{
                        	creatureType = EnumCreatureType.CREATURE;
                		}
            		}
            	}
            }

            int j1 = entityData.x;
            int k1 = entityData.y;
            int l1 = entityData.z;

            boolean isWaterMob = entityliving instanceof EntityGuardian;

            Material material = world.getBlockState(new BlockPos(j1, k1, l1)).getMaterial();
            if (!world.isBlockNormalCube(new BlockPos(j1, k1, l1), false) && (((creatureType == EnumCreatureType.WATER_CREATURE || isWaterMob) && material == Material.WATER) || material == Material.AIR))
            {
	            float f = (float)j1 + 0.5F;
	            float f1 = (float)k1;
	            float f2 = (float)l1 + 0.5F;

	            entityliving.setLocationAndAngles((double)f, (double)f1, (double)f2, rotationFromNbtSet ? rotationFromNbt : rand.nextFloat() * 360.0F, 0.0F);

	            if(entityliving instanceof EntityLiving)
	            {
	            	for(int r = 0; r < groupSize; r++)
	            	{
	            		if(r != 0)
	            		{
	            	        if(entityData.nameTagOrNBTFileName != null && (entityData.nameTagOrNBTFileName.toLowerCase().trim().endsWith(".txt") || entityData.nameTagOrNBTFileName.toLowerCase().trim().endsWith(".nbt")))
	            	        {
	            	        	NBTTagCompound nbttagcompound = new NBTTagCompound();

	            		        try
	            		        {
	            		            NBTBase nbtbase = JsonToNBT.getTagFromJson(entityData.getMetaData());

	            		            if (!(nbtbase instanceof NBTTagCompound))
	            		            {
	            		            	if(OTG.getPluginConfig().spawnLog)
	            		            	{
	            		            		OTG.log(LogMarker.WARN, "Invalid NBT tag for mob in EntityFunction: " + entityData.getMetaData() + ". Skipping mob.");
	            		            	}
		            		        	return;
	            		            }

	            		            nbttagcompound = (NBTTagCompound)nbtbase;
	            		        }
	            		        catch (NBTException nbtexception)
	            		        {
	            		        	if(OTG.getPluginConfig().spawnLog)
	            		        	{
	            		        		OTG.log(LogMarker.WARN, "Invalid NBT tag for mob in EntityFunction: " + entityData.getMetaData() + ". Skipping mob.");
	            		        	}
	            		        	return;
	            		        }

	            		        nbttagcompound.setString("id", entityResourceLocation.toString());
	            		        entityliving = EntityList.createEntityFromNBT(nbttagcompound, world);
	            	        } else {
	            		        try
	            		        {
	            		            entityliving = (Entity) entityClass.getConstructor(new Class[] {World.class}).newInstance(new Object[] { world });
	            		        }
	            		        catch (Exception exception)
	            		        {
	            		            exception.printStackTrace();
	            		            return;
	            		        }
	            	        }
	                        entityliving.setLocationAndAngles((double)f, (double)f1, (double)f2, rotationFromNbtSet ? rotationFromNbt : rand.nextFloat() * 360.0F, 0.0F);
	            		}

	            		if(entityData.nameTagOrNBTFileName != null && !entityData.nameTagOrNBTFileName.toLowerCase().trim().endsWith(".txt") && !entityData.nameTagOrNBTFileName.toLowerCase().trim().endsWith(".nbt"))
	            		{
	            			if(nameTag != null && nameTag.length() > 0)
	        				{
	        					((EntityLiving) entityliving).setCustomNameTag(nameTag);
	        				}
	            		}

    					((EntityLiving) entityliving).enablePersistence(); // <- makes sure mobs don't de-spawn

    					if(
							chunkBeingPopulated == null || 
							(
								OTG.IsInAreaBeingPopulated((int)Math.floor(entityliving.posX), (int)Math.floor(entityliving.posZ), chunkBeingPopulated)// || 
								//getChunkGenerator().chunkExists((int)Math.floor(entityliving.posX), (int)Math.floor(entityliving.posZ))
							)
						)
    					{
	    			    	if(OTG.getPluginConfig().spawnLog)
	    			    	{
	    			    		OTG.log(LogMarker.DEBUG, "Spawned OK");
	    			    	}
	
	    					world.spawnEntity(entityliving);
    					}
	            	}
	            } else {
	            	for(int r = 0; r < groupSize; r++)
	            	{
	            		if(r != 0)
	            		{
	            	        if(entityData.nameTagOrNBTFileName != null && (entityData.nameTagOrNBTFileName.toLowerCase().trim().endsWith(".txt") || entityData.nameTagOrNBTFileName.toLowerCase().trim().endsWith(".nbt")))
	            	        {
	            	        	NBTTagCompound nbttagcompound = new NBTTagCompound();

	            		        try
	            		        {
	            		            NBTBase nbtbase = JsonToNBT.getTagFromJson(entityData.getMetaData());

	            		            if (!(nbtbase instanceof NBTTagCompound))
	            		            {
	            		            	if(OTG.getPluginConfig().spawnLog)
	            		            	{
	            		            		OTG.log(LogMarker.WARN, "Invalid NBT tag for mob in EntityFunction: " + entityData.getMetaData() + ". Skipping mob.");
	            		            	}
	            			        	return;
	            		            }

	            		            nbttagcompound = (NBTTagCompound)nbtbase;
	            		        }
	            		        catch (NBTException nbtexception)
	            		        {
	            		        	if(OTG.getPluginConfig().spawnLog)
	            		        	{
	            		        		OTG.log(LogMarker.WARN, "Invalid NBT tag for mob in EntityFunction: " + entityData.getMetaData() + ". Skipping mob.");
	            		        	}
	            		        	return;
	            		        }

	            		        nbttagcompound.setString("id", entityResourceLocation.toString());
	            		        entityliving = EntityList.createEntityFromNBT(nbttagcompound, world);
	            		        // TODO: Rotated item frames don't stick to walls correctly or don't pop off when their support block is removed.
	            		        if(nbttagcompound.hasKey("Facing"))
	            		        {
	            		        	EnumFacing facing = EnumFacing.byIndex(nbttagcompound.getByte("Facing"));
	            		        	rotationFromNbt = facing.getHorizontalIndex() * 90;
	            		        	rotationFromNbtSet = true;
	            		        }
	            		        
	            	        } else {
	            		        try
	            		        {
	            		            entityliving = (Entity) entityClass.getConstructor(new Class[] {World.class}).newInstance(new Object[] { world });
	            		        }
	            		        catch (Exception exception)
	            		        {
	            		            exception.printStackTrace();
	            		            return;
	            		        }
	            	        }
	                        entityliving.setLocationAndAngles((double)f, (double)f1, (double)f2, rotationFromNbtSet ? rotationFromNbt : rand.nextFloat() * 360.0F, 0.0F);
	            		}

    			    	if(OTG.getPluginConfig().spawnLog)
    			    	{
    			    		OTG.log(LogMarker.DEBUG, "Spawned OK");
    			    	}
    			    	
    			    	if(entityliving instanceof EntityItemFrame)
    			    	{
    			    		((EntityItemFrame)entityliving).facingDirection = ((EntityItemFrame)entityliving).facingDirection == null ? EnumFacing.SOUTH : ((EntityItemFrame)entityliving).facingDirection;  
    			    	}

    					if(
							chunkBeingPopulated == null || 
							(
								OTG.IsInAreaBeingPopulated((int)Math.floor(entityliving.posX), (int)Math.floor(entityliving.posZ), chunkBeingPopulated)// || 
								//getChunkGenerator().chunkExists((int)Math.floor(entityliving.posX), (int)Math.floor(entityliving.posZ))
							)
						)
    					{
	    			    	if(OTG.getPluginConfig().spawnLog)
	    			    	{
	    			    		OTG.log(LogMarker.DEBUG, "Spawned OK");
	    			    	}
	
	    					world.spawnEntity(entityliving);
    					}
	            	}
	            }
            }
		}
    }

	@Override
	public boolean generateModdedCaveGen(int x, int z, ChunkBuffer chunkBuffer)
	{
		if(this.cavesGen == null)
		{
			return false;	
		}

		ChunkPrimer primer = ((ForgeChunkBuffer)chunkBuffer).getChunkPrimer();
		this.cavesGen.generate(this.world, x, z, primer);
		
		return true;
	}

	@Override
	public boolean isInsideWorldBorder(ChunkCoordinate chunkCoordinate)
	{
		return this.world.getWorldBorder().contains(new BlockPos(chunkCoordinate.getBlockXCenter(), 0, chunkCoordinate.getBlockZCenter()));
	}
	
	private boolean isOTGPlusLoaded = false;
	private boolean isOTGPlus = false;
	@Override
	public boolean isOTGPlus()
	{
		if(!isOTGPlusLoaded)
		{
			isOTGPlusLoaded = true;
			DimensionConfig dimConfig = OTG.getDimensionsConfig().getDimensionConfig(this.getName());
			if(dimConfig != null && dimConfig.Settings.IsOTGPlus)		
			{
				isOTGPlus = true;
			}
		}
		return isOTGPlus;
	}
}
