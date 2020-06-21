package com.pg85.otg.forge.generator;

import static com.pg85.otg.util.ChunkCoordinate.CHUNK_X_SIZE;
import static com.pg85.otg.util.ChunkCoordinate.CHUNK_Z_SIZE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.customobjects.bofunctions.ModDataFunction;
import com.pg85.otg.forge.OTGPlugin;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.forge.util.NBTHelper;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.generator.ChunkProviderOTG;
import com.pg85.otg.generator.ObjectSpawner;
import com.pg85.otg.generator.biome.OutputType;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.network.ConfigProvider;
import com.pg85.otg.util.BlockPos2D;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import net.minecraft.block.BlockGravel;
import net.minecraft.block.BlockSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class OTGChunkGenerator implements IChunkGenerator
{
    private boolean testMode = false;
    private ForgeWorld world;
    private ChunkProviderOTG generator;
    public ObjectSpawner spawner;

    // Caches
	private FifoMap<BlockPos2D, LocalMaterialData[]> unloadedBlockColumnsCache;
	private FifoMap<ChunkCoordinate, Chunk> unloadedChunksCache;
	private FifoMap<ChunkCoordinate, Chunk> lastUsedChunks;
    ForgeChunkBuffer chunkBuffer;
    Object chunkBufferLock = new Object();
    //

    private	DataFixer dataFixer = DataFixesManager.createFixer();
    
    public OTGChunkGenerator(ForgeWorld _world)
    {
        this.world = _world;

        this.testMode = this.world.getConfigs().getWorldConfig().modeTerrain == WorldConfig.TerrainMode.TerrainTest;

        this.generator = new ChunkProviderOTG(this.world.getConfigs(), this.world);
        this.spawner = new ObjectSpawner(this.world.getConfigs(), this.world);
        // TODO: Add a setting to the worldconfig for the size of these caches. 
        // Worlds with lots of BO4's and large smoothing areas may want to increase this. 
        this.unloadedBlockColumnsCache = new FifoMap<BlockPos2D, LocalMaterialData[]>(1024);
        this.unloadedChunksCache = new FifoMap<ChunkCoordinate, Chunk>(128);
        this.lastUsedChunks = new FifoMap<ChunkCoordinate, Chunk>(4);
    }
    
	// Chunks
	
	// Called by /otg flush command to clear memory.
    public void clearChunkCache()
    {
    	this.lastUsedChunks.clear();
   		this.unloadedBlockColumnsCache.clear();
   		this.unloadedChunksCache.clear();
    }
    
    public void clearChunkFromCache(ChunkCoordinate chunkCoordinate)
    {
    	this.lastUsedChunks.remove(chunkCoordinate);
    }

    @Override
    public Chunk generateChunk(int chunkX, int chunkZ)
    {
		return getBlocks(chunkX, chunkZ, true);
    }

    @Override
    public void populate(int chunkX, int chunkZ)
    {
        ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
        this.unloadedChunksCache.remove(chunkCoord);
    	if(this.testMode)
        {
            return;
        }

        BlockSand.fallInstantly = true;
        BlockGravel.fallInstantly = true;

        this.spawner.populate(chunkCoord);

        BlockSand.fallInstantly = false;
        BlockGravel.fallInstantly = false;

        HashMap<String,ArrayList<ModDataFunction<?>>> MessagesPerMod = world.getWorldSession().getModDataForChunk(chunkCoord);
        if(MessagesPerMod != null && MessagesPerMod.entrySet().size() > 0)
        {
        	for(Entry<String, ArrayList<ModDataFunction<?>>> modNameAndData : MessagesPerMod.entrySet())
        	{
        		String messageString = "";
				if(modNameAndData.getKey().equals("OTG"))
				{
	    			for(ModDataFunction<?> modData : modNameAndData.getValue())
	    			{
						String[] paramString2 = modData.modData.split("\\/");

						if(paramString2.length > 1)
						{
							if(paramString2[0].equals("mob"))
							{
								boolean autoSpawn = paramString2.length > 4 ? Boolean.parseBoolean(paramString2[4]) : false;
	    	    				if(autoSpawn)
	    	    				{
	    	    					messageString += "[" + modData.x + "," + modData.y + "," + modData.z + "," + modData.modData + "]";
	    	    				}
							}
						}
	    			}
				} else {
	    			for(ModDataFunction<?> modData : modNameAndData.getValue())
	    			{
    					messageString += "[" + modData.x + "," + modData.y + "," + modData.z + "," + modData.modData + "]";
	    			}
				}
    			if(messageString.length() > 0)
    			{
    				// Send messages to any mods listening
    				FMLInterModComms.sendRuntimeMessage(OTGPlugin.Instance, modNameAndData.getKey(), "ModData", "[" + "[" + world.getName() + "," + chunkX + "," + chunkZ + "]" + messageString + "]");
    			}
        	}
        }
    }
       
    // If allowOutsidePopulatingArea then normal OTG rules are used:
    // returns any chunk that is inside the area being populated.
    // returns null for chunks outside the populated area if populationBoundsCheck=true
    // returns any loaded chunk or null if populationBoundsCheck=false and chunk is outside the populated area

    // If !allowOutsidePopulatinArea then OTG+ rules are used:
    // returns any chunk that is inside the area being populated. TODO: Or any chunk that is cached, which technically should only be chunks that are in the populated area. Cached chunks could also be from the previously populated area, fix that?
    // returns any loaded chunk outside the populated area
    // throws an exception if any unloaded chunk outside the populated area is requested or if a loaded chunk could not be queried.
    
    public Chunk getChunk(int x, int z)
    {
        ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);
        
        Chunk chunk = this.lastUsedChunks.get(chunkCoord);
        if(chunk == null)
        {
        	// Try to fetch the chunk without populating it.
        	chunk = this.world.getWorld().getChunkProvider().getLoadedChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ());
	        if(chunk == null)
	        {
	        	// Request the chunk with a risk of it being populated.
	        	chunk = this.world.getWorld().getChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ());
	        }
	        if(chunk != null)
	        {
	        	this.lastUsedChunks.put(chunkCoord, chunk);
	        }
        }
    	return chunk;
    }

    // Blocks
    
    private Chunk getBlocks(int chunkX, int chunkZ, boolean provideChunk)
    {
    	Chunk chunk = unloadedChunksCache.get(ChunkCoordinate.fromChunkCoords(chunkX,chunkZ));
    	if(chunk == null)
    	{
    		chunk = new Chunk(this.world.getWorld(), chunkX, chunkZ);

    		ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
    		synchronized(chunkBufferLock)
    		{
	    		chunkBuffer = new ForgeChunkBuffer(chunkCoord);
	    		this.generator.generate(chunkBuffer);
	    		chunk = chunkBuffer.toChunk(this.world.getWorld());
		        chunkBuffer = null;
    		}
	        fillBiomeArray(chunk);
	        //if(world.getConfigs().getWorldConfig().ModeTerrain == TerrainMode.TerrainTest)
	        //{
	        	chunk.generateSkylightMap(); // Normally chunks are lit in the ObjectSpawner after finishing their population step, TerrainTest skips the population step though so light blocks here.
	        //}
    	} else {
	        fillBiomeArray(chunk);
	        //if(world.getConfigs().getWorldConfig().ModeTerrain == TerrainMode.TerrainTest)
	        {
	        	chunk.generateSkylightMap(); // Normally chunks are lit in the ObjectSpawner after finishing their population step, TerrainTest skips the population step though so light blocks here.
	        }
    	}

    	return chunk;
    }
    
    /**
     * Fills the biome array of a chunk with the proper saved ids (no
     * generation ids).
     * @param chunk The chunk to fill the biomes of.
     */
    private void fillBiomeArray(Chunk chunk)
    {
        byte[] chunkBiomeArray = chunk.getBiomeArray();
        ConfigProvider configProvider = this.world.getConfigs();
        int[] biomeShortArray = this.world.getBiomeGenerator().getBiomes(null, chunk.x * CHUNK_X_SIZE, chunk.z * CHUNK_Z_SIZE, CHUNK_X_SIZE, CHUNK_Z_SIZE, OutputType.DEFAULT_FOR_WORLD);
        int generationId;
        LocalBiome biome;
        
        for (int i = 0; i < chunkBiomeArray.length; i++)
        {
            generationId = biomeShortArray[i];
            biome = configProvider.getBiomeByOTGIdOrNull(generationId);
        	chunkBiomeArray[i] = (byte) biome.getIds().getSavedId();
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

    	LocalMaterialData[] cachedColumn = this.unloadedBlockColumnsCache.get(blockPos);

    	if(cachedColumn != null)
    	{
    		return cachedColumn;
    	}
    	
    	Chunk chunk = this.world.getWorld().getChunkProvider().getLoadedChunk(chunkX, chunkZ);
    	if(chunk == null)
    	{
    		chunk = this.unloadedChunksCache.get(chunkCoord);
    	} else {
    		this.unloadedChunksCache.remove(chunkCoord);
    	}
    	if(chunk == null)
    	{
			// Generate a chunk without populating it
	    	chunk = new Chunk(this.world.getWorld(), chunkX, chunkZ);
	    	synchronized(chunkBufferLock)
	    	{
				chunkBuffer = new ForgeChunkBuffer(chunkCoord);
				this.generator.generate(chunkBuffer);
				chunk = chunkBuffer.toChunk(this.world.getWorld());
				chunkBuffer = null;
	    	}
			unloadedChunksCache.put(chunkCoord, chunk);
    	}
		
		cachedColumn = new LocalMaterialData[256];

    	LocalMaterialData[] blocksInColumn = new LocalMaterialData[256];
    	IBlockState blockInChunk;
    	for(short y = 0; y < 256; y++)
        {
        	blockInChunk = chunk.getBlockState(new BlockPos(blockX, y, blockZ));
        	if(blockInChunk != null)
        	{
	        	blocksInColumn[y] = ForgeMaterialData.ofMinecraftBlockState(blockInChunk);
        	} else {
        		break;
        	}
        }
		unloadedBlockColumnsCache.put(blockPos, cachedColumn);
		
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
    	ForgeMaterialData material;
    	boolean isLiquid;
    	boolean isSolid;
    	
        for(int y = 255; y > -1; y--)
        {
        	material = (ForgeMaterialData) blockColumn[y];
        	isLiquid = material.isLiquid();
        	isSolid = material.isSolid() || (!ignoreSnow && material.toDefaultMaterial().equals(DefaultMaterial.SNOW));
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
    
    public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag)
    {
        if (y < PluginStandardValues.WORLD_DEPTH || y >= PluginStandardValues.WORLD_HEIGHT)
        {
            return;
        }

        //DefaultMaterial defaultMaterial = material.toDefaultMaterial();

        // TODO: Fix this
        //if(defaultMaterial.equals(DefaultMaterial.DIODE_BLOCK_ON))
        {
        	//material = ForgeMaterialData.ofDefaultMaterial(DefaultMaterial.DIODE_BLOCK_OFF, material.getBlockData());
        }
        //else if(defaultMaterial.equals(DefaultMaterial.REDSTONE_COMPARATOR_ON))
        {
        	//material = ForgeMaterialData.ofDefaultMaterial(DefaultMaterial.REDSTONE_COMPARATOR_OFF, material.getBlockData());
        }

        IBlockState newState = ((ForgeMaterialData) material).internalBlock();

        BlockPos pos = new BlockPos(x, y, z);

        // Get chunk from (faster) custom cache
        Chunk chunk = this.getChunk(x, z);
        if (chunk == null)
        {
        	throw new RuntimeException("Could not provide chunk.");
        }
        
        // Disable nearby block physics
        //IBlockState iblockstate = setBlockState(chunk, pos, newState);
        
        // Disable nearby block physics (except for tile entities) and set block
        boolean oldCaptureBlockStates = this.world.getWorld().captureBlockSnapshots;
        this.world.getWorld().captureBlockSnapshots = !(newState.getBlock().hasTileEntity(newState));
        //this.world.getWorld().captureBlockSnapshots = true;
        IBlockState iblockstate = chunk.setBlockState(pos, newState);
        this.world.getWorld().captureBlockSnapshots = oldCaptureBlockStates;
        
        if (iblockstate == null)
        {
        	return; // Happens when block to place is the same as block being placed? TODO: Is that the only time this happens?
        }

	    if (metaDataTag != null)
	    {
	    	attachMetadata(x, y, z, metaDataTag);
	    }

	    // Notify world: (2 | 16) == update client, don't update observers
    	this.world.getWorld().markAndNotifyBlock(pos, chunk, iblockstate, newState, 2 | 16);
    }
    
    //TODO: Remove this after testing captureBlockSnapshots
    /*
    private IBlockState setBlockState(Chunk _this, BlockPos pos, IBlockState state)
    {
        int i = pos.getX() & 15;
        int j = pos.getY();
        int k = pos.getZ() & 15;
        int l = k << 4 | i;

        if (j >= _this.precipitationHeightMap[l] - 1)
        {
        	_this.precipitationHeightMap[l] = -999;
        }

        int i1 = _this.getHeightMap()[l];
        IBlockState iblockstate = _this.getBlockState(pos);

        if (iblockstate == state)
        {
            return null;
        } else {
            Block block = state.getBlock();
            Block block1 = iblockstate.getBlock();
            int k1 = iblockstate.getLightOpacity(_this.getWorld(), pos); // Relocate old light value lookup here, so that it is called before TE is removed.
            ExtendedBlockStorage extendedblockstorage = _this.getBlockStorageArray()[j >> 4];
            boolean flag = false;

            if (extendedblockstorage == Chunk.NULL_BLOCK_STORAGE)
            {
                if (block == Blocks.AIR)
                {
                    return null;
                }

                extendedblockstorage = new ExtendedBlockStorage(j >> 4 << 4, _this.getWorld().provider.hasSkyLight());
                _this.getBlockStorageArray()[j >> 4] = extendedblockstorage;
                flag = j >= i1;
            }

            extendedblockstorage.set(i, j & 15, k, state);

            //if (block1 != block)
            {
                if (!_this.getWorld().isRemote)
                {
                    if (block1 != block) //Only fire block breaks when the block changes.
                    block1.breakBlock(_this.getWorld(), pos, iblockstate);
                    TileEntity te = _this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
                    if (te != null && te.shouldRefresh(_this.getWorld(), pos, iblockstate, state)) _this.getWorld().removeTileEntity(pos);
                }
                else if (block1.hasTileEntity(iblockstate))
                {
                    TileEntity te = _this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
                    if (te != null && te.shouldRefresh(_this.getWorld(), pos, iblockstate, state))
                    _this.getWorld().removeTileEntity(pos);
                }
            }

            if (extendedblockstorage.get(i, j & 15, k).getBlock() != block)
            {
                return null;
            } else {
                if (flag)
                {
                    _this.generateSkylightMap();
                }
                else
                {
                    int j1 = state.getLightOpacity(_this.getWorld(), pos);

                    if (j1 > 0)
                    {
                        if (j >= i1)
                        {
                            _this.relightBlock(i, j + 1, k);
                        }
                    }
                    else if (j == i1 - 1)
                    {
                    	_this.relightBlock(i, j, k);
                    }

                    if (j1 != k1 && (j1 < k1 || _this.getLightFor(EnumSkyBlock.SKY, pos) > 0 || _this.getLightFor(EnumSkyBlock.BLOCK, pos) > 0))
                    {
                        _this.propagateSkylightOcclusion(i, k);
                    }
                }

                // If capturing blocks, only run block physics for TE's. Non-TE's are handled in ForgeHooks.onPlaceItemIntoWorld
                //if (!_this.getWorld().isRemote && block1 != block && (!_this.getWorld().captureBlockSnapshots || block.hasTileEntity(state)))
                {
                	// Don't do this when spawning resources and BO2's/BO3's, they are considered to be in their intended updated state when spawned
               		//block.onBlockAdded(_this.getWorld(), pos, state);
                }

                if (block.hasTileEntity(state))
                {
                    TileEntity tileentity1 = _this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);

                    if (tileentity1 == null)
                    {
                        tileentity1 = block.createTileEntity(_this.getWorld(), state);
                        _this.getWorld().setTileEntity(pos, tileentity1);
                    }

                    if (tileentity1 != null)
                    {
                        tileentity1.updateContainingBlockInfo();
                    }
                }

                _this.markDirty();
                return iblockstate;
            }
        }
    }   
    */    
    
    private void attachMetadata(int x, int y, int z, NamedBinaryTag tag)
    {
        // Convert Tag to a native nms tag
        NBTTagCompound nmsTag = NBTHelper.getNMSFromNBTTagCompound(tag);
        // Add the x, y and z position to it
        nmsTag.setInteger("x", x);
        nmsTag.setInteger("y", y);
        nmsTag.setInteger("z", z);
        // Update to current Minecraft format (maybe we want to do this at
        // server startup instead, and then save the result?)
        // TODO: Use datawalker instead
        //nmsTag = this.dataFixer.process(FixTypes.BLOCK_ENTITY, nmsTag, -1);
        nmsTag = this.dataFixer.process(FixTypes.BLOCK_ENTITY, nmsTag);

        // Add that data to the current tile entity in the world
        TileEntity tileEntity = this.world.getWorld().getTileEntity(new BlockPos(x, y, z));
        if (tileEntity != null)
        {
            tileEntity.readFromNBT(nmsTag);
        } else {
        	if(OTG.getPluginConfig().spawnLog)
        	{
        		OTG.log(LogMarker.WARN, "Skipping tile entity with id {}, cannot be placed at {},{},{}", nmsTag.getString("id"), x, y, z);
        	}
        }
    }    
    
    // Structures

    @Override
    public void recreateStructures(Chunk chunkIn, int chunkX, int chunkZ)
    {
    	this.world.recreateStructures(chunkIn, chunkX, chunkZ);
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z)
    {
        return false;
    }

	@Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos)
    {
		// TODO: Is it okay to not use worldIn here?
		return this.world.isInsideStructure(structureName, pos);
    }

    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos blockPos, boolean p_180513_4_)
    {
		// TODO: Is it okay to not use worldIn here?
    	return this.world.getNearestStructurePos(structureName, blockPos, p_180513_4_);
    }    

    // Only called during generate by woodlandmansion. Don't call this anywhere else, chunkBuffer is not thread-safe and may be in use.
    public int getHighestBlockInCurrentlyPopulatingChunk(int x, int z)
    {
    	LocalMaterialData material;
    	for(int i = PluginStandardValues.WORLD_HEIGHT - 1; i > PluginStandardValues.WORLD_DEPTH; i--)
    	{
    		material = chunkBuffer.getBlock(x, i, z);
    		if(material != null && !material.isEmptyOrAir())
			{
    			return i;
			};
    	}

    	return 0;
    }    
    
    // Mob spawning
    
    @Override
    public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType paramaca, BlockPos blockPos)
    {
        return this.world.getPossibleCreatures(paramaca, blockPos);
    }
}
