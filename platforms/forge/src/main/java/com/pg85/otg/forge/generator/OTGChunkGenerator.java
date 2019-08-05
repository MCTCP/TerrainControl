package com.pg85.otg.forge.generator;

import static com.pg85.otg.util.ChunkCoordinate.CHUNK_X_SIZE;
import static com.pg85.otg.util.ChunkCoordinate.CHUNK_Z_SIZE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.customobjects.bofunctions.ModDataFunction;
import com.pg85.otg.forge.OTGPlugin;
import com.pg85.otg.forge.util.ForgeMaterialData;
import com.pg85.otg.forge.util.NBTHelper;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.generator.ChunkProviderOTG;
import com.pg85.otg.generator.ObjectSpawner;
import com.pg85.otg.generator.biome.OutputType;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.network.ConfigProvider;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.helpers.MaterialHelper;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGravel;
import net.minecraft.block.BlockSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class OTGChunkGenerator implements IChunkGenerator
{	
	private class LocalCoords2D
	{
		byte x;
		byte z;
		
		LocalCoords2D(byte x, byte z)
		{
			this.x = x;
			this.z = z;
		}
		
		public boolean equals(Object other)
		{
			if(this == other)
			{
				return true;
			}
			if(other instanceof LocalCoords2D)
			{
				if(((LocalCoords2D)other).x == this.x && ((LocalCoords2D)other).z == this.z)
				{
					return true;
				}
			}
			return false;
		}
	}
	
	private class ChunkColumns
	{
		Chunk chunk;
		HashMap<LocalCoords2D, LocalMaterialData[]> blockColumns;
		
		ChunkColumns(Chunk chunk, HashMap<LocalCoords2D, LocalMaterialData[]> blockColumns)
		{
			this.chunk = chunk;
			this.blockColumns = blockColumns;
		}
	}
	
    private boolean testMode = false;
    private ForgeWorld world;
    private ChunkProviderOTG generator;
    public ObjectSpawner spawner;

    // Caches
	private ArrayList<ChunkCoordinate> populatedChunks;
    private FifoMap<ChunkCoordinate, ChunkColumns> unloadedChunkCache = new FifoMap<ChunkCoordinate, ChunkColumns>(128);
    private Map<ChunkCoordinate,Chunk> loadedChunkCache = new HashMap<ChunkCoordinate, Chunk>();
    private Chunk lastUsedChunk;
    private int lastUsedChunkX;
    private int lastUsedChunkZ;
    private ForgeChunkBuffer chunkBuffer;    
    //   
    
    // The first run is used by MC to check for suitable locations for the spawn location. For some reason the spawn location must be on grass.
    private boolean firstRun = true; 
    private ArrayList<LocalMaterialData> originalBlocks = new ArrayList<LocalMaterialData>(); // Don't need to store coords, will place the blocks back in the same order we got them so coords can be inferred    
    private ChunkCoordinate spawnChunk;
    private boolean spawnChunkFixed = false;
    
    private boolean allowSpawningOutsideBounds = false;   
    
    /**
     * Used in {@link #fillBiomeArray(Chunk)}, to avoid creating
     * new short arrays.
     */
    private int[] biomeShortArray;
    private	DataFixer dataFixer = DataFixesManager.createFixer();
    
    public OTGChunkGenerator(ForgeWorld _world)
    {
        this.world = _world;

        this.testMode = this.world.getConfigs().getWorldConfig().modeTerrain == WorldConfig.TerrainMode.TerrainTest;

        this.generator = new ChunkProviderOTG(this.world.getConfigs(), this.world);
        this.spawner = new ObjectSpawner(this.world.getConfigs(), this.world);
        this.populatedChunks = new ArrayList<ChunkCoordinate>();
    }
    
	public void setAllowSpawningOutsideBounds(boolean allowSpawningOutsideBounds)
	{
		this.allowSpawningOutsideBounds = allowSpawningOutsideBounds;
	}
    
	// Chunks
	
	// Called at the end of each chunk generation/population cycle.
	// Also called by pregenerator and /otg flush command to clear memory.
    public void clearChunkCache(boolean onlyLoadedChunks)
    {
    	loadedChunkCache.clear();
    	lastUsedChunk = null;
    	if(!onlyLoadedChunks)
    	{
    		unloadedChunkCache.clear();
    	}
    }

    @Override
    public Chunk generateChunk(int chunkX, int chunkZ)
    {
    	ChunkCoordinate chunkCoords = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
    	boolean bFound = false;
    	synchronized(populatedChunks)
    	{
			if(!populatedChunks.contains(chunkCoords))
			{
				populatedChunks.add(chunkCoords);
			} else {
				bFound = true;
			}
    	}

		if(bFound)
		{
			Chunk chunk = world.getChunk(chunkCoords.getBlockX(), chunkCoords.getBlockZ(), true);
			if(chunk == null)
			{
				// Can happen when chunkExists() in this.world.getChunk() mistakenly returns false
				// This could potentially cause an infinite loop but than't can't be disallowed looping because of async calls
				// to ProvideChunk() by updateBlocks() on server tick.
				chunk = this.world.getWorld().getChunk(chunkX, chunkZ);

				if(chunk == null)
				{
					// TODO: Test this..
					throw new RuntimeException();
				}
				OTG.log(LogMarker.WARN, "Double population prevented");
			}
			if(chunk != null)
			{
				OTG.log(LogMarker.WARN, "Double population prevented");
				return chunk;
			} else {
				OTG.log(LogMarker.WARN, "Double population could not be prevented for chunk X" + chunkX + " Z" + chunkZ);
			}
		}

		Chunk chunk = getBlocks(chunkX, chunkZ, true);
		return chunk;
    }

    @Override
    public void populate(int chunkX, int chunkZ)
    {
        ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
    	if(this.testMode)
        {
    		if(this.testMode)
    		{
    			world.getChunkGenerator().clearChunkCache(false);
    		}
            return;
        }

        BlockSand.fallInstantly = true;
        BlockGravel.fallInstantly = true;

        if(!this.spawner.processing)
        {
	        this.spawner.populatingX = chunkX;
	        this.spawner.populatingZ = chunkZ;
        } else {
			// This happens when:
			// This chunk was populated because of a block being spawned on the
			// other side of the edge of this chunk,
			// the block performed a block check inside this chunk upon being
			// placed (like a torch looking for a wall to stick to)
			// This means that we must place any BO3 queued for this chunk
			// because the block being spawned might need to interact with it
			// (spawn the wall for the torch to stick to).
			// Unfortunately this means that this chunk will not get a call to
			// populate() via the usual population
			// mechanics where we populate 4 BO3's at once in a 2x2 chunks area
			// and then spawn resources (ore, trees, lakes)
			// on top of that. Hopefully the neighbouring chunks do get spawned
			// normally and cover the 2x2 areas this chunk is part of
			// with enough resources that noone notices some are missing...

			// This can also happen when the server decides to provide and/or
			// populate a chunk that has already been provided/populated before,
			// which seems like a bug.
        	//throw new RuntimeException();
        }

        fixSpawnChunk();

        DimensionConfig dimConfig = OTG.getDimensionsConfig().getDimensionConfig(world.getName());
        if(dimConfig.Settings.SpawnPointSet)
        {
    		world.getWorld().provider.setSpawnPoint(new BlockPos(dimConfig.Settings.SpawnPointX, dimConfig.Settings.SpawnPointY, dimConfig.Settings.SpawnPointZ));
    		dimConfig.Settings.SpawnPointSet = false; // This will reset when the world is reloaded, so if users manually reconfigure the spawn point it will be reverted. They will have to set spawnPointSet: false to prevent this.
        }

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
        
        // The chunk generator caches only the chunks being populated, clear them.
        this.clearChunkCache(true);
    }
    
    // If allowOutsidePopulatingArea then normal OTG rules are used:
    // returns any chunk that is inside the area being populated.
    // returns null for chunks outside the populated area if populationBoundsCheck=true
    // returns any loaded chunk or null if populationBoundsCheck=false and chunk is outside the populated area

    // If !allowOutsidePopulatinArea then OTG+ rules are used:
    // returns any chunk that is inside the area being populated. TODO: Or any chunk that is cached, which technically should only be chunks that are in the populated area. Cached chunks could also be from the previously populated area, fix that?
    // returns any loaded chunk outside the populated area
    // throws an exception if any unloaded chunk outside the populated area is requested or if a loaded chunk could not be queried.
    
    public Chunk getChunk(int x, int z, boolean allowOutsidePopulatingArea)
    {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        if(lastUsedChunk != null && lastUsedChunkX == chunkX && lastUsedChunkZ == chunkZ)
        {
        	return lastUsedChunk;
        }

        Chunk chunk = loadedChunkCache.get(ChunkCoordinate.fromChunkCoords(chunkX, chunkZ));
        if(chunk != null)
        {
        	lastUsedChunk = chunk;
        	lastUsedChunkX = chunkX;
        	lastUsedChunkZ = chunkZ;
        	return chunk;
        }

        boolean outsidePopulatingArea =
			(
				chunkX != this.world.getObjectSpawner().populatingX &&
				chunkX != this.world.getObjectSpawner().populatingX + 1
			)
			||
			(
				chunkZ != this.world.getObjectSpawner().populatingZ &&
				chunkZ != this.world.getObjectSpawner().populatingZ + 1
			)
		;

		if(
			(
				outsidePopulatingArea &&
				!allowOutsidePopulatingArea
			) ||
			this.allowSpawningOutsideBounds
		)
		{
			if(!allowOutsidePopulatingArea)
			{
				if(this.world.getConfigs().getWorldConfig().populationBoundsCheck)
				{
					return null;
				}

				// TODO: Does this return only loaded chunks outside the area being populated, or also unloaded ones?
				Chunk loadedChunk = this.getLoadedChunkWithoutMarkingActive(chunkX, chunkZ);
				if(loadedChunk != null)
				{
					lastUsedChunk = loadedChunk;
		        	lastUsedChunkX = chunkX;
		        	lastUsedChunkZ = chunkZ;
					loadedChunkCache.put(ChunkCoordinate.fromChunkCoords(chunkX, chunkZ), loadedChunk);
				}

				if(!this.allowSpawningOutsideBounds || loadedChunk != null)
				{
					return loadedChunk;
				}
			}

			// For BO3AtSpawn we may be forced to populate a chunk outside of the chunks being populated.
			if(this.allowSpawningOutsideBounds)
			{
		        Chunk spawnedChunk = this.world.getWorld().getChunk(chunkX, chunkZ);
		        if(spawnedChunk == null)
		        {
		        	OTG.log(LogMarker.FATAL, "Chunk request failed X" + chunkX + " Z" + chunkZ);
		        	throw new RuntimeException("Chunk request failed X" + chunkX + " Z" + chunkZ);
		        }

		        loadedChunkCache.put(ChunkCoordinate.fromChunkCoords(chunkX, chunkZ), spawnedChunk);
				lastUsedChunk = spawnedChunk;
		    	lastUsedChunkX = chunkX;
		    	lastUsedChunkZ = chunkZ;

				return spawnedChunk;
			}
		}

    	// This never happens when we're spawning stuff on neighbouring BO3's inside the 2x2 population area
    	if(outsidePopulatingArea)
    	{
    		if(!((WorldServer)this.world.getWorld()).isBlockLoaded(new BlockPos(chunkX * 16, 1, chunkZ * 16)))
    		//if(!((WorldServer)this.getWorld()).isChunkGeneratedAt(chunkX, chunkZ))
    		{
    			// Happens when part of a BO3 or smoothing area is spawned and triggers height/material checks in unpopulated chunks.
    			// Also happens when /otg tp requests a block in an unpopulated chunk.
    			return null;
    		} else {
    			// Chunk was provided by chunkprovider
    		}
    	}

        Chunk spawnedChunk = this.world.getWorld().getChunk(chunkX, chunkZ);
        if(spawnedChunk == null)
        {
        	OTG.log(LogMarker.FATAL, "Chunk request failed X" + chunkX + " Z" + chunkZ);
        	throw new RuntimeException("Chunk request failed X" + chunkX + " Z" + chunkZ);
        }

        loadedChunkCache.put(ChunkCoordinate.fromChunkCoords(chunkX, chunkZ), spawnedChunk);
		lastUsedChunk = spawnedChunk;
    	lastUsedChunkX = chunkX;
    	lastUsedChunkZ = chunkZ;

		return spawnedChunk;
    }    
    
    // TODO: This looks interesting, could use it more?
    private Chunk getLoadedChunkWithoutMarkingActive(int chunkX, int chunkZ)
    {
        long i = ChunkPos.asLong(chunkX, chunkZ);
        return (Chunk) ((ChunkProviderServer) this.world.getWorld().getChunkProvider()).loadedChunks.get(i);
    }
    
    // Spawn chunk fix for OTG+    
	// TODO: This should no longer be needed, using the onCreateWorldSpawn event in WorldListener now?.
    private void fixSpawnChunk()
    {
    	if(!firstRun)
    	{
    		// Only required for OTG+ isStructureAtSpawn setting for BO3's.
    		if(!spawnChunkFixed && world.getConfigs().getWorldConfig().isOTGPlus)
			{
	    		// TODO: This shouldn't be necessary, the first chunk spawned should be in the are being populated?
	    		this.setAllowSpawningOutsideBounds(true);
				int i = 0;
				for(int x = 0; x < 15; x++)
				{
					for(int z = 0; z < 15; z++)
					{
						if(!originalBlocks.get(i).toDefaultMaterial().equals(DefaultMaterial.AIR) || !originalBlocks.get(i + 1).toDefaultMaterial().equals(DefaultMaterial.AIR))
						{
							world.setBlock(spawnChunk.getBlockX() + x, 63, spawnChunk.getBlockZ() + z, originalBlocks.get(i), null, true);
							world.setBlock(spawnChunk.getBlockX() + x, 64, spawnChunk.getBlockZ() + z, originalBlocks.get(i + 1), null, true);
						} else {
							for(int h = 62; h > 0; h++)
							{
								if(!world.getMaterial(spawnChunk.getBlockX() + x, h, spawnChunk.getBlockZ() + z, true).toDefaultMaterial().equals(DefaultMaterial.AIR))
								{
									world.setBlock(spawnChunk.getBlockX() + x, 63, spawnChunk.getBlockZ() + z, originalBlocks.get(i), null, true);
									world.setBlock(spawnChunk.getBlockX() + x, 64, spawnChunk.getBlockZ() + z, originalBlocks.get(i + 1), null, true);
									break;
								}
							}
						}
						i += 2;
					}
				}
	
				this.setAllowSpawningOutsideBounds(false);
			}
    		spawnChunkFixed = true;
    	}
    }

    // Blocks
    
    private Chunk getBlocks(int chunkX, int chunkZ, boolean provideChunk)
    {
    	ChunkColumns chunkCacheEntry = unloadedChunkCache.get(ChunkCoordinate.fromChunkCoords(chunkX,chunkZ));
    	Chunk chunk = null;
    	if(chunkCacheEntry != null)
    	{
    		chunk = chunkCacheEntry.chunk;
    	}
    	
    	if(chunk == null)
    	{
    		chunk = new Chunk(this.world.getWorld(), chunkX, chunkZ);

    		ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
    		chunkBuffer = new ForgeChunkBuffer(chunkCoord);
    		this.generator.generate(chunkBuffer);

    		// Before starting terrain generation MC tries to find a suitable spawn point. For some reason it looks for a grass block with an air block above it.
    		// To prevent MC from looking in many chunks (if there is no grass block nearby) and causing them to be populated place grass in the first requested chunk
    		// cache the original blocks so that they can be placed back when proper world generation starts.
    		// Only needed for OTG+ isStructureAtSpawn setting for BO3's.
    		if(firstRun && world.getConfigs().getWorldConfig().isOTGPlus)
    		{
    			spawnChunk = chunkCoord;
    			for(int x = 0; x < 15; x++)
    			{
    				for(int z = 0; z < 15; z++)
    				{
    					originalBlocks.add(chunkBuffer.getBlock(x, 63, z));
    					originalBlocks.add(chunkBuffer.getBlock(x, 64, z));

    					chunkBuffer.setBlock(x, 63, z, MaterialHelper.toLocalMaterialData(DefaultMaterial.GRASS, 0));
    					chunkBuffer.setBlock(x, 64, z, MaterialHelper.toLocalMaterialData(DefaultMaterial.AIR, 0));
    				}
    			}
    		}
			firstRun = false;
    		chunk = chunkBuffer.toChunk(this.world.getWorld());

	        fillBiomeArray(chunk);
	        //if(world.getConfigs().getWorldConfig().ModeTerrain == TerrainMode.TerrainTest)
	        {
	        	chunk.generateSkylightMap(); // Normally chunks are lit in the ObjectSpawner after finishing their population step, TerrainTest skips the population step though so light blocks here.
	        }

	        chunkBuffer = null;
    	} else {
	        fillBiomeArray(chunk);
	        //if(world.getConfigs().getWorldConfig().ModeTerrain == TerrainMode.TerrainTest)
	        {
	        	chunk.generateSkylightMap(); // Normally chunks are lit in the ObjectSpawner after finishing their population step, TerrainTest skips the population step though so light blocks here.
	        }
        	unloadedChunkCache.remove(ChunkCoordinate.fromChunkCoords(chunkX,chunkZ));
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
        this.biomeShortArray = this.world.getBiomeGenerator().getBiomes(this.biomeShortArray, chunk.x * CHUNK_X_SIZE, chunk.z * CHUNK_Z_SIZE, CHUNK_X_SIZE, CHUNK_Z_SIZE, OutputType.DEFAULT_FOR_WORLD);

        for (int i = 0; i < chunkBiomeArray.length; i++)
        {
            int generationId = this.biomeShortArray[i];

            LocalBiome biome = configProvider.getBiomeByOTGIdOrNull(generationId);

        	chunkBiomeArray[i] = (byte) biome.getIds().getSavedId();
        }
    }
    
    public LocalMaterialData[] getBlockColumnInUnloadedChunk(int x, int z)
    {
    	ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);    	
    	int chunkX = chunkCoord.getChunkX();
    	int chunkZ = chunkCoord.getChunkZ();
    	
		// Get internal coordinates for block in chunk
    	byte blockX = (byte)(x &= 0xF);
    	byte blockZ = (byte)(z &= 0xF);    	
    	LocalCoords2D columnLocalCoords = new LocalCoords2D(blockX, blockZ);

    	ChunkColumns chunkCacheEntry = unloadedChunkCache.get(chunkCoord);

    	Chunk chunk = null;
    	HashMap<LocalCoords2D, LocalMaterialData[]> blockColumnCache = null;
    	LocalMaterialData[] cachedColumn = null;
    	if(chunkCacheEntry != null)
    	{
    		chunk = chunkCacheEntry.chunk;
    		blockColumnCache = chunkCacheEntry.blockColumns;
    		cachedColumn = blockColumnCache.get(columnLocalCoords);
    	}
    	if(cachedColumn != null)
    	{
    		return cachedColumn;
    	}

    	if(chunk == null)
    	{
        	chunk = new Chunk(this.world.getWorld(), chunkX, chunkZ);

    		ForgeChunkBuffer chunkBuffer = new ForgeChunkBuffer(chunkCoord);
    		this.generator.generate(chunkBuffer);
    		chunk = chunkBuffer.toChunk(this.world.getWorld());
        	blockColumnCache = new HashMap<LocalCoords2D, LocalMaterialData[]>(256);
        	unloadedChunkCache.put(chunkCoord, new ChunkColumns(chunk, blockColumnCache));
    	}

    	LocalMaterialData[] blocksInColumn = new LocalMaterialData[256];
        for(short y = 0; y < 256; y++)
        {
        	IBlockState blockInChunk = chunk.getBlockState(new BlockPos(blockX, y, blockZ));
        	if(blockInChunk != null)
        	{
	        	blocksInColumn[y] = ForgeMaterialData.ofMinecraftBlockState(blockInChunk);
        	} else {
        		break;
        	}
        }
        blockColumnCache.put(columnLocalCoords, blocksInColumn);

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
        	ForgeMaterialData material = (ForgeMaterialData) blockColumn[y];
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
    
    public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, boolean allowOutsidePopulatingArea)
    {
	    /*
	     * This method usually breaks on every Minecraft update. Always check
	     * whether the names are still correct. Often, you'll also need to
	     * rewrite parts of this method for newer block place logic.
	     */

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
        Chunk chunk = this.getChunk(x, z, allowOutsidePopulatingArea);
        if (chunk == null)
        {
            // Chunk is unloaded
        	throw new RuntimeException("Whatever it is you're trying to do, we didn't write any code for it (sorry). Please contact Team OTG about this crash.");
        }

        IBlockState iblockstate = setBlockState(chunk, pos, newState);

        if (iblockstate == null)
        {
        	return; // Happens when block to place is the same as block being placed? TODO: Is that the only time this happens?
        }

	    if (metaDataTag != null)
	    {
	    	attachMetadata(x, y, z, metaDataTag, allowOutsidePopulatingArea);
	    }

    	this.world.getWorld().markAndNotifyBlock(pos, chunk, iblockstate, newState, 2 | 16);
    }

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
    
    private void attachMetadata(int x, int y, int z, NamedBinaryTag tag, boolean allowOutsidePopulatingArea)
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
        		OTG.log(LogMarker.WARN, "Skipping tile entity with id {}, cannot be placed at {},{},{} on id {}", nmsTag.getString("id"), x, y, z, this.world.getMaterial(x, y, z, allowOutsidePopulatingArea));
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

    public int getHighestBlockInCurrentlyPopulatingChunk(int x, int z)
    {
    	for(int i = PluginStandardValues.WORLD_HEIGHT - 1; i > PluginStandardValues.WORLD_DEPTH; i--)
    	{
    		LocalMaterialData material = chunkBuffer.getBlock(x, i, z);
    		if(material != null && !material.isAir())
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
