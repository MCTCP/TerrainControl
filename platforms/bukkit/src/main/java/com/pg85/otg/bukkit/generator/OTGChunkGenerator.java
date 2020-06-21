package com.pg85.otg.bukkit.generator;

import com.pg85.otg.OTG;
import com.pg85.otg.bukkit.OTGPlugin;
import com.pg85.otg.bukkit.materials.BukkitMaterialData;
import com.pg85.otg.bukkit.util.NBTHelper;
import com.pg85.otg.bukkit.world.BukkitWorld;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.generator.ChunkProviderOTG;
import com.pg85.otg.generator.ObjectSpawner;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.BlockPos2D;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.DataConverter;
import net.minecraft.server.v1_12_R1.DataConverterRegistry;
import net.minecraft.server.v1_12_R1.DataConverterTypes;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.ITileEntity;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.TileEntity;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OTGChunkGenerator extends ChunkGenerator
{
    private DataConverter dataConverter;
    //private Chunk[] chunkCache;
    private ChunkProviderOTG chunkProviderOTG;
    // Why does the chunk generator require multiple block populators, each with their own ObjectSpawner instance? For multiple dims?
    private ArrayList<BlockPopulator> BlockPopulator = new ArrayList<BlockPopulator>();
    private boolean NotGenerate = false;
    private OTGPlugin plugin;
    private BukkitWorld world;
    
    // Caches
	private FifoMap<BlockPos2D, LocalMaterialData[]> blockColumnsCache;
	private FifoMap<ChunkCoordinate, ChunkData> unloadedChunksCache;
	private FifoMap<ChunkCoordinate, Chunk> lastUsedChunks;
    //

    
    public OTGChunkGenerator(OTGPlugin _plugin, BukkitWorld world)
    {
        this.plugin = _plugin;
        this.world = world;
        this.dataConverter = DataConverterRegistry.a();
        // TODO: Add a setting to the worldconfig for the size of these caches. 
        // Worlds with lots of BO4's and large smoothing areas may want to increase this. 
        this.blockColumnsCache = new FifoMap<BlockPos2D, LocalMaterialData[]>(1024);
        this.unloadedChunksCache = new FifoMap<ChunkCoordinate, ChunkData>(128);
        this.lastUsedChunks = new FifoMap<ChunkCoordinate, Chunk>(4);
    }
    
	// Called by /otg flush command to clear memory.
    public void clearChunkCache()
    {
    	this.lastUsedChunks.clear();
   		this.blockColumnsCache.clear();
   		this.unloadedChunksCache.clear();
    }

    /**
     * Initializes the world if it hasn't already been initialized.
     * 
     * @param world
     *            The world of this generator.
     */
    private void makeSureWorldIsInitialized(World world)
    {
        if (this.chunkProviderOTG == null)
        {
            // Not yet initialized, do it now
            this.plugin.onWorldInit(world);
        }
    }

    /**
     * Called whenever a BukkitWorld instance becomes available.
     * 
     * @param _world
     *            The BukkitWorld instance.
     */
    public void onInitialize(BukkitWorld _world)
    {
        this.chunkProviderOTG = new ChunkProviderOTG(_world.getConfigs(), _world);

        WorldConfig.TerrainMode mode = _world.getConfigs().getWorldConfig().modeTerrain;

        if (mode == WorldConfig.TerrainMode.Normal)// || mode == WorldConfig.TerrainMode.OldGenerator)
        {
            this.BlockPopulator.add(new OTGBlockPopulator(_world));
        }

        if (mode == WorldConfig.TerrainMode.NotGenerate)
        {
            this.NotGenerate = true;
        }
    }

    public ObjectSpawner getObjectSpawner()
    {
    	if (this.chunkProviderOTG == null)
    	{
    		throw new RuntimeException();
    	}
        return ((OTGBlockPopulator)this.BlockPopulator.get(0)).getObjectSpawner();
    }
    
    @Override
    public List<BlockPopulator> getDefaultPopulators(World world)
    {
        makeSureWorldIsInitialized(world);
        return this.BlockPopulator;
    }

    @Override
    public boolean canSpawn(World world, int x, int z)
    {
        makeSureWorldIsInitialized(world);

        int y = this.getHighestBlockYInUnloadedChunk(x, z, true, false, false, true);
        return  y >  -1;
        //Material material = world.getHighestBlockAt(x, z).getType();
        //return material.isSolid();
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome)
    {
        makeSureWorldIsInitialized(world);
    	
    	ChunkData chunkData = this.NotGenerate ? null : unloadedChunksCache.get(ChunkCoordinate.fromChunkCoords(chunkX,chunkZ));
    	if(chunkData == null)
    	{
            chunkData = createChunkData(world);

            if (this.NotGenerate)
            {
                return chunkData;
            }

            ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
            BukkitChunkBuffer chunkBuffer = new BukkitChunkBuffer(chunkCoord, chunkData);
            this.chunkProviderOTG.generate(chunkBuffer);
            
    	}
    	return chunkData;    	
    }

    public Chunk getChunk(int x, int z)
    {
        ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);
        
        Chunk chunk = this.lastUsedChunks.get(chunkCoord);
        if(chunk == null)
        {
	        // Hopefully this is equal to ChunkProviderServer.getLoadedChunk
        	// So it won't try to populate the chunk.
        	chunk = this.world.getWorld().getChunkProvider().getLoadedChunkAt(x, z);
	        if(chunk == null)
	        {
	        	// Request the chunk with a risk of it being populated..
	        	chunk = this.world.getWorld().getChunkAt(chunkCoord.getChunkX(), chunkCoord.getChunkZ());
	        }
	        if(chunk != null)
	        {
	        	this.lastUsedChunks.put(chunkCoord, chunk);
	        }
        }

    	return chunk;    
    }
   
    public void startPopulation(ChunkCoordinate chunkCoord) { }

    public void endPopulation() { }
    
    public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag)
    {
        if (y < PluginStandardValues.WORLD_DEPTH || y >= PluginStandardValues.WORLD_HEIGHT)
        {
            return;
        }
    	
        try
        {
            IBlockData blockData = ((BukkitMaterialData) material).internalBlock();

            // Get chunk from (faster) custom cache
            Chunk chunk = this.getChunk(x, z);

            if (chunk == null)
            {
            	throw new RuntimeException("Could not provide chunk.");
            }

            BlockPosition blockPos = new BlockPosition(x, y, z);

            // Disable nearby block physics (except for tile entities) and set block
            boolean oldCaptureBlockStates = this.world.getWorld().captureBlockStates;
            this.world.getWorld().captureBlockStates = !(blockData.getBlock() instanceof ITileEntity);
            //this.world.getWorld().captureBlockStates = true;
            IBlockData oldBlockData = chunk.a(blockPos, blockData);
            this.world.getWorld().captureBlockStates = oldCaptureBlockStates;

            if (oldBlockData == null)
            {
            	return; // Happens when block to place is the same as block being placed? TODO: Is that the only time this happens?
            }

            //if (blockData.c() != oldBlockData.c() || blockData.d() != oldBlockData.d())
            //{
                //if (isSafeForLightUpdates(chunk, x, z))
                //{
                    // Relight
                	//this.world.getWorld().methodProfiler.a("checkLight");
                	//this.world.getWorld().w(blockPos);
                	//this.world.getWorld().methodProfiler.b();
                //}
            //}

    	    if (metaDataTag != null)
    	    {
    	    	attachMetadata(x, y, z, metaDataTag);
    	    }

            // Notify world: (2 | 16) == update client, don't update observers
    	    notifyAndUpdatePhysics(this.world.getWorld(), blockPos, chunk, oldBlockData, blockData, 2 | 16);
        } catch (Throwable t) {
        	// TODO: What is this? remove?
        	/*
            String populatingChunkInfo = this.chunkCache == null? "(no chunk)" :
                    this.chunkCache[0].locX + "," + this.chunkCache[0].locZ;
            // Add location info to error
            RuntimeException runtimeException = new RuntimeException("Error setting "
                    + material + " block at " + x + "," + y + "," + z
                    + " while populating chunk " + populatingChunkInfo, t);
            runtimeException.setStackTrace(new StackTraceElement[0]);
            throw runtimeException;
            */
        }
    }
    
    // CraftBukkit start - Split off from above in order to directly send client and physic updates
    public void notifyAndUpdatePhysics(net.minecraft.server.v1_12_R1.World _this, BlockPosition blockposition, Chunk chunk, IBlockData oldBlock, IBlockData newBlock, int i)
    {
    	net.minecraft.server.v1_12_R1.Block block = newBlock.getBlock();
        if (
    		(i & 2) != 0 && 
    		(
				!_this.isClientSide || 
				(i & 4) == 0
			) && (
				chunk == null || 
				//chunk.isReady()
		    	// Replace isReady with Forge's isPopulated to prevent 
				// updates that would cause cascading chunkgen:
		    	// return this.ticked && this.isTerrainPopulated && this.isLightPopulated;				
				(chunk.j() && chunk.isDone() && chunk.v())
			)
		) // allow chunk to be null here as chunk.isReady() is false when we send our notification during block placement
        {
        	_this.notify(blockposition, oldBlock, newBlock, i);
        }

        if (
    		!_this.isClientSide && 
    		(i & 1) != 0
		)
        {
        	_this.update(blockposition, oldBlock.getBlock(), true);
            if (newBlock.n())
            {
            	_this.updateAdjacentComparators(blockposition, block);
            }
        }
        else if (
    		!_this.isClientSide && 
    		(i & 16) == 0
		)
        {
        	_this.c(blockposition, block);
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
        TileEntity tileEntity = this.world.getWorld().getTileEntity(new BlockPosition(x, y, z));
        if (tileEntity != null)
        {
            tileEntity.load(nmsTag);
        } else {
        	if(OTG.getPluginConfig().spawnLog)
        	{
        		OTG.log(LogMarker.WARN, "Skipping tile entity with id {}, cannot be placed at {},{},{}.", nmsTag.getString("id"), x, y, z);
        	}
        }
    }
    
    public LocalMaterialData[] getBlockColumnInUnloadedChunk(int x, int z)
    {
    	BlockPos2D blockPos = new BlockPos2D(x, z);
    	ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);
    	int chunkX = chunkCoord.getChunkX();
    	int chunkZ = chunkCoord.getChunkZ();
    	
		// Get internal coordinates for block in chunk
    	byte blockX = (byte)(x &= 0xF);
    	byte blockZ = (byte)(z &= 0xF);

    	LocalMaterialData[] cachedColumn = this.blockColumnsCache.get(blockPos);

    	if(cachedColumn != null)
    	{
    		return cachedColumn;
    	}
    	
		cachedColumn = new LocalMaterialData[256];
    	LocalMaterialData[] blocksInColumn = new LocalMaterialData[256];
		
    	Chunk chunk = this.world.getWorld().getChunkProvider().getLoadedChunkAt(chunkX, chunkZ);
    	if(chunk == null)
    	{
	    	ChunkData chunkData = this.unloadedChunksCache.get(chunkCoord);
	    	if(chunkData == null)
	    	{
				// Generate a chunk without populating it
	    		chunkData = this.generateChunkData(this.world.getWorld().getWorld(), this.world.getWorld().random, chunkX, chunkZ, (BiomeGrid)null);
	    	}
	        for(short y = 0; y < 256; y++)
	        {
	        	MaterialData blockInChunk = chunkData.getTypeAndData(blockX, y, blockZ);
	        	if(blockInChunk != null)
	        	{
	        		blocksInColumn[y] = BukkitMaterialData.ofIds(blockInChunk.getItemTypeId(), blockInChunk.getData());
	        	} else {       		
	        		break;
	        	}
	        }
			unloadedChunksCache.put(chunkCoord, chunkData);			
    	} else {
	        for(short y = 0; y < 256; y++)
	        {
	        	IBlockData blockInChunk = chunk.getBlockData(new BlockPosition(blockX, y, blockZ));
	        	if(blockInChunk != null)
	        	{
	        		blocksInColumn[y] = BukkitMaterialData.ofMinecraftBlockData(blockInChunk);
	        	} else {       		
	        		break;
	        	}
	        }    		
    	}    	
        blockColumnsCache.put(blockPos, cachedColumn);		
        return blocksInColumn;
    }
    
    public LocalMaterialData getMaterialInUnloadedChunk(int x, int y, int z)
    {
    	LocalMaterialData[] blockColumn = getBlockColumnInUnloadedChunk(x,z);
        return blockColumn[y];
    }

    public int getHighestBlockYInUnloadedChunk(int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow)
    {
    	int height = -1;

    	LocalMaterialData[] blockColumn = getBlockColumnInUnloadedChunk(x,z);

        for(int y = 255; y > -1; y--)
        {
        	BukkitMaterialData material = (BukkitMaterialData) blockColumn[y];
        	boolean isLiquid = material.isLiquid();
        	boolean isSolid = material.isSolid() || (!ignoreSnow && material.toDefaultMaterial().equals(DefaultMaterial.SNOW));
        	if(!(isLiquid && ignoreLiquid))
        	{
            	if((findSolid && isSolid) || (findLiquid && isLiquid))
        		{
            		return y;
        		}
            	if((findSolid && isLiquid) || (findLiquid && isSolid))
            	{
            		return -1;
            	}
        	}
        }
    	return height;
    }
}