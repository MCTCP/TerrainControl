package com.pg85.otg.forge;

import com.pg85.otg.*;
import com.pg85.otg.common.BiomeIds;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.common.WorldSession;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.biome.BiomeLoadInstruction;
import com.pg85.otg.configuration.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.customobjects.SpawnableObject;
import com.pg85.otg.customobjects.bofunctions.EntityFunction;
import com.pg85.otg.customobjects.structures.CustomStructureCache;
import com.pg85.otg.exception.BiomeNotFoundException;
import com.pg85.otg.forge.biomes.ForgeBiome;
import com.pg85.otg.forge.biomes.ForgeBiomeRegistryManager;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;
import com.pg85.otg.forge.generator.ForgeChunkBuffer;
import com.pg85.otg.forge.generator.OTGChunkGenerator;
import com.pg85.otg.forge.generator.structure.*;
import com.pg85.otg.forge.util.ForgeMaterialData;
import com.pg85.otg.forge.util.IOHelper;
import com.pg85.otg.forge.util.MobSpawnGroupHelper;
import com.pg85.otg.forge.util.NBTHelper;
import com.pg85.otg.forge.util.WorldHelper;
import com.pg85.otg.forge.world.ForgeWorldSession;
import com.pg85.otg.generator.ChunkBuffer;
import com.pg85.otg.generator.ObjectSpawner;
import com.pg85.otg.generator.biome.BiomeGenerator;
import com.pg85.otg.generator.terrain.CavesGen;
import com.pg85.otg.generator.terrain.TerrainGenBase;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.network.ClientConfigProvider;
import com.pg85.otg.network.ConfigProvider;
import com.pg85.otg.network.ServerConfigProvider;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;
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
import net.minecraft.init.Blocks;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
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
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
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
    private static final int MAX_BIOMES_COUNT = 4096;
    private static final int MAX_SAVED_BIOMES_COUNT = 255;
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
        this.cavesGen = net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(new MapGenCaves(), net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.CAVE);
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
    public int getLightLevel(int x, int y, int z)
    {
        // Actually, this calculates the block and skylight as it were day.
        return this.world.getLight(new BlockPos(x, y, z));
    }

    // OTG+ structure cache
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
	public void setAllowSpawningOutsideBounds(boolean allowSpawningOutsideBounds)
	{
		this.getChunkGenerator().setAllowSpawningOutsideBounds(allowSpawningOutsideBounds);
	}
    
    public BlockPos getSpawnPoint()
    {
    	BlockPos spawnPos = world.provider.getSpawnPoint();
    	return new BlockPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
    }
    
    @Override
    public void startPopulation(ChunkCoordinate chunkCoord)
    {
    	// TODO: Only used for Spigot, remove?
    }

    @Override
    public void endPopulation()
    {
    	// TODO: Only used for Spigot, remove?
    }
    
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
			biomes.add(biome);
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

    @Override
    public Collection<BiomeLoadInstruction> getDefaultBiomes()
    {
    	return ForgeBiomeRegistryManager.getDefaultBiomes();
    }
    
    // Chunks

    @Override
    public ChunkCoordinate getSpawnChunk()
    {
    	BlockPos spawnPos = getSpawnPoint();
    	return ChunkCoordinate.fromBlockCoords(spawnPos.getX(), spawnPos.getZ());
    }
    
    public Chunk getChunk(int x, int z, boolean allowOutsidePopulatingArea)
    {
    	return this.getChunkGenerator().getChunk(x, z, allowOutsidePopulatingArea);
    }
    
    @Override
    public boolean isLoaded(int x, int y, int z)
    {
        return getChunk(x, z, false) != null;
    }   
    
    @Override
	public boolean isInsidePregeneratedRegion(ChunkCoordinate chunk)
	{
		return getWorldSession().isInsidePregeneratedRegion(chunk);
	}

    // Blocks
    
    @Override
    public int getLiquidHeight(int x, int z)
    {
        int highestY = getHighestBlockYAt(x, z, false, true, false, false);
        if(highestY > 0)
        {
        	highestY += 1;
        } else {
        	highestY = -1;
        }
		return highestY;
    }

    @Override
    public int getSolidHeight(int x, int z)
    {
        int highestY = getHighestBlockYAt(x, z, true, false, true, true);
        if(highestY > 0)
        {
        	highestY += 1;
        } else {
        	highestY = -1;
        }
		return highestY;
    }
   
    // Not used by OTG+
    @Override
    public int getHighestBlockYAt(int x, int z)
    {
        Chunk chunk = this.getChunk(x, z, false);
        if (chunk == null)
        {
            return -1;
        }

        int y = chunk.getHeightValue(x & 0xf, z & 0xf);

        // Fix for incorrect light map
        // TODO: Fix this properly?
        boolean incorrectHeightMap = false;
        while (y < getHeightCap() && chunk.getBlockState(x, y, z).getMaterial().blocksLight())
        {
            y++;
            incorrectHeightMap = true;
        }
        if (incorrectHeightMap)
        {
            // Let Minecraft know that it made an error
            this.world.checkLight(new BlockPos(x, y, z));
        }

        return y;
    }

    // Only used by OTG+
    @Override
    public int getHighestBlockYAt(int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow)
    {
        Chunk chunk = this.getChunk(x, z, true);
        if (chunk == null)
        {
        	int y = generator.getHighestBlockYInUnloadedChunk(x, z, findSolid, findLiquid, ignoreLiquid, ignoreSnow);
        	return y;
        }

		// Get internal coordinates for block in chunk
        z &= 0xF;
        x &= 0xF;

        // TODO: Get highest block from heightmap?
    	for(int i = 255; i > -1; i--)
        {
    		ForgeMaterialData material = ForgeMaterialData.ofMinecraftBlockState(chunk.getBlockState(x, i, z));
        	if(material == null) throw new RuntimeException();
        	DefaultMaterial defaultMaterial = material.toDefaultMaterial();
        	boolean isLiquid = material.isLiquid();
        	boolean isSolid = (material.isSolid() && !defaultMaterial.equals(DefaultMaterial.LEAVES) && !defaultMaterial.equals(DefaultMaterial.LEAVES_2)) || (!ignoreSnow && defaultMaterial.equals(DefaultMaterial.SNOW));
        	if(!(isLiquid && ignoreLiquid))
        	{
            	if((findSolid && isSolid) || (findLiquid && isLiquid))
        		{
            		return i;
        		}
            	if((findSolid && isLiquid) || (findLiquid && isSolid))
            	{
            		return -1;
            	}
        	}
        }

    	// Can happen if this is a chunk filled with air

        return -1;
    }

    @Override
    public boolean isNullOrAir(int x, int y, int z, boolean isOTGPlus)
    {
    	if (y >= PluginStandardValues.WORLD_HEIGHT || y < PluginStandardValues.WORLD_DEPTH)
    	{
    		return true;
    	}

        Chunk chunk = this.getChunk(x, z, isOTGPlus);
        if (chunk == null)
        {
        	return true;
        }

        return chunk.getBlockState(x & 0xF, y, z & 0xF).getMaterial().equals(Material.AIR);
    }
    
    @Override
    public LocalMaterialData getMaterial(int x, int y, int z, boolean allowOutsidePopulatingArea)
    {
        if (y >= PluginStandardValues.WORLD_HEIGHT || y < PluginStandardValues.WORLD_DEPTH)
        {
        	return null;
        	//throw new RuntimeException();
        }

        // TODO: Return null when attempting to fetch/set blocks outside of populating area && !allowOutsidePopulatingArea?
        Chunk chunk = this.getChunk(x, z, allowOutsidePopulatingArea);

        if(chunk == null && !allowOutsidePopulatingArea)
        {
        	return null;
        }

        // Can happen when requesting a chunk outside the world border
        // or a chunk that has not yet been populated
        if (chunk == null)
		{
        	return generator.getMaterialInUnloadedChunk(x,y,z);
        	//throw new RuntimeException();
		}

		// Get internal coordinates for block in chunk
        z &= 0xF;
        x &= 0xF;

        ForgeMaterialData material = ForgeMaterialData.ofMinecraftBlockState(chunk.getBlockState(x, y, z));

        return material;
    }
    
    @Override
    public LocalMaterialData[] getBlockColumn(int x, int z)
    {
    	//OTG.log(LogMarker.INFO, "getBlockColumn at X" + x + " Z" + z);
    	return generator.getBlockColumnInUnloadedChunk(x,z);
    }

    @Override
    public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, boolean allowOutsidePopulatingArea)
    {
    	this.getChunkGenerator().setBlock(x, y, z, material, metaDataTag, allowOutsidePopulatingArea);
    }

    @Override
    public NamedBinaryTag getMetadata(int x, int y, int z)
    {
       return NBTHelper.getMetadata(world, x, y, z);
    }
    
    // Structures / trees

    @Override
    public boolean placeDungeon(Random rand, int x, int y, int z)
    {
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
        BlockPos blockPos = new BlockPos(x, y, z);
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

    	replaceBlocks(this.getChunk(chunkCoord.getBlockX() + 16, chunkCoord.getBlockZ() + 16, false), 0, 0, 16);
    	replaceBlocks(this.getChunk(chunkCoord.getBlockX(), chunkCoord.getBlockZ() + 16, false), 0, 0, 16);
    	replaceBlocks(this.getChunk(chunkCoord.getBlockX() + 16, chunkCoord.getBlockZ(), false), 0, 0, 16);
    	replaceBlocks(this.getChunk(chunkCoord.getBlockX(), chunkCoord.getBlockZ(), false), 0, 0, 16);
    }

    private void replaceBlocks(Chunk rawChunk, int startXInChunk, int startZInChunk, int size)
    {
        int endXInChunk = startXInChunk + size;
        int endZInChunk = startZInChunk + size;
        int worldStartX = rawChunk.x * 16;
        int worldStartZ = rawChunk.z * 16;

        ExtendedBlockStorage[] sectionsArray = rawChunk.getBlockStorageArray();

        IBlockState block;
        int blockId;
        int y;
        ForgeMaterialData replaceTo;
        LocalBiome biome;
        LocalMaterialData[][] replaceArray;
        
        for (ExtendedBlockStorage section : sectionsArray)
        {
            if (section == null)
            {
                continue;
            }

            for (int sectionX = startXInChunk; sectionX < endXInChunk; sectionX++)
            {
                for (int sectionZ = startZInChunk; sectionZ < endZInChunk; sectionZ++)
                {
                    biome = this.getBiome(worldStartX + sectionX, worldStartZ + sectionZ);
                    if (biome != null && biome.getBiomeConfig().replacedBlocks.hasReplaceSettings())
                    {
                        replaceArray = biome.getBiomeConfig().replacedBlocks.compiledInstructions;
                        for (int sectionY = 0; sectionY < 16; sectionY++)
                        {
                            block = section.getData().get(sectionX, sectionY, sectionZ);
                            blockId = Block.getIdFromBlock(block.getBlock());
                            if (replaceArray[blockId] == null)
                            {
                                continue;
                            }

                            y = section.getYLocation() + sectionY;
                            if (y >= replaceArray[blockId].length)
                            {
                                break;
                            }

                            replaceTo = (ForgeMaterialData) replaceArray[blockId][y];
                            if (replaceTo == null || replaceTo.getBlockId() == blockId)
                            {
                                continue;
                            }

                            section.set(sectionX, sectionY, sectionZ, replaceTo.internalBlock());
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

    /**
     * Used by mob inheritance code. Used to inherit default mob spawning settings (including those added by other mods)
     * @param biomeConfigStub
     */
    @Override
	public void mergeVanillaBiomeMobSpawnSettings(BiomeConfigStub biomeConfigStub, String biomeResourceLocation)
	{
    	ForgeBiomeRegistryManager.mergeVanillaBiomeMobSpawnSettings(biomeConfigStub, biomeResourceLocation);
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
    public void spawnEntity(EntityFunction<?> entityData)
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

    			    	if(OTG.getPluginConfig().spawnLog)
    			    	{
    			    		OTG.log(LogMarker.DEBUG, "Spawned OK");
    			    	}

    					world.spawnEntity(entityliving);
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

	            		world.spawnEntity(entityliving);
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
}
