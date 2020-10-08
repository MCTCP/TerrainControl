package com.pg85.otg.forge.pregenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.NoSuchElementException;

import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;
import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;

public class Pregenerator
{
	private int currentX;
    private int currentZ;
	private boolean processing = false;
    private long startTime;
    private long timeTaken;
    private int pregenerationRadius;
	private int cycle = 0;
	private int left = 0;
	private int right = 0;
	private int top = 0;
	private int bottom = 0;
	private int iLeft = Integer.MIN_VALUE;
	private int iRight = Integer.MIN_VALUE;
	private int iTop = Integer.MIN_VALUE;
	private int iBottom = Integer.MIN_VALUE;
	private int spawned = 0;
	private double total;
	private int spawnedThisTick = 0;
	private int lastSpawnedWhenSaved = 0;
	private int compressCustomStructureCacheThreshHold = 1000;
	private boolean pregeneratorIsRunning = false;
	private boolean pregeneratorIsInitialised = false;
	private int maxSpawnPerTick;

	// In-game UI
	public String pregenerationWorld = "";
	public String preGeneratorProgressStatus = "";
	public String preGeneratorProgress = "";
	public String progressScreenCycle = "";
	public String progressScreenRadius = "";
	public String progressScreenElapsedTime = "";
	public String progressScreenEstimatedTime = "";
	public int progressScreenWorldSizeInBlocks;
	public long progressScreenServerUsedMbs = 0;
	public long progressScreenServerTotalMbs = 0;
	
	// Logging
	private long lastMessage = System.currentTimeMillis();

	private ForgeWorld world;
	private ChunkCoordinate preGeneratorCenterPoint;
	
	public Pregenerator(LocalWorld world)
	{
		this.world = (ForgeWorld)world;

		// Don't load pregenerator data on MP client
		if(this.world.world != null)
		{
			loadPregeneratorData();
		}

		this.maxSpawnPerTick = OTG.getPluginConfig().pregeneratorMaxChunksPerTick;
		this.pregenerationWorld = world.getConfigs().getWorldConfig().getName();
	}

	public int getPregenerationRadius()
	{
		return this.pregenerationRadius;
	}
	
	/**
	 * Only used when settings pregenerator status for MP clients (that don't actually get run)
	 * @return
	 */
	public void setPregeneratorIsRunning(boolean pregeneratorIsRunning)
	{		
		this.pregeneratorIsRunning = pregeneratorIsRunning;
	}

	// This should never be called asynchronously.
	/**
	 * Sets the pre-generation radius (in chunks) for the world.
	 * Radius cannot be smaller than currently pre-generated area.
	 * @param radius The desired radius.
	 * @param world Used to save pregenerator data to file.
	 * @return The radius value that was set for the pregenerator.
	 */
	public int setPregenerationRadius(int radius)
	{		
		if(radius > -1 && radius != this.pregenerationRadius)
		{
			// Cycle points to the current cycle, which could be: 
			// 2, if pregenerationRadius is 1 and pregeneration is finished.
			// 0-1, if pregenerationRadius is 1 and pregeneration is not yet finished
			// If the radius is smaller than the current cycle, set it to the current cycle (cycle - 1 if pregeneration finished).
			if(this.cycle != 0 && (radius < (this.total == this.spawned ? this.cycle - 1 : this.cycle)))
			{
				this.pregenerationRadius = (this.total == this.spawned ? this.cycle - 1 : this.cycle);
			} else {
				this.pregenerationRadius = radius;
			}
		} else {
			return this.pregenerationRadius;
		}

		// World can be null when creating new worlds on MP client
		if(this.world != null)
		{
			this.savePregeneratorData(true);
		}

		this.total = (this.pregenerationRadius * 2 + 1) * (this.pregenerationRadius * 2 + 1);
    	
		return this.pregenerationRadius;
	}

	public int getPregenerationBorderLeft()
	{
		return this.left;
	}

	public int getPregenerationBorderRight()
	{
		return this.right;
	}

	public int getPregenerationBorderTop()
	{
		return this.top;
	}

	public int getPregenerationBorderBottom()
	{
		return this.bottom;
	}
	
	public void setPreGeneratorCenterPoint(ChunkCoordinate chunkCoord)
	{
		// Don't allow moving of the center point if pregeneration has already started
		if(!this.pregeneratorIsRunning && this.spawned == 0)
		{
			this.preGeneratorCenterPoint = chunkCoord;
			savePregeneratorData(true);
			this.pregeneratorIsInitialised = true;
		}
	}

	public ChunkCoordinate getPregenerationCenterPoint()
	{
		return this.preGeneratorCenterPoint;
	}

	public boolean isRunning()
	{
		return this.pregeneratorIsRunning;
	}
	
	public boolean isInitialised()
	{
		return this.pregeneratorIsInitialised;
	}

	public void processTick()
	{
		if(!this.processing)
		{
			this.processing = true;
			if(this.spawned < this.total && this.pregenerationRadius > 0)
			{
				// If this is a dimension, not the overworld, it may be unloaded.
				// We can't pregenerate chunks when the world is unloaded, since the
				// world isn't ticked and the ChunkIO doesn't work properly, so the
				// chunks aren't flushed to disk properly. Force dimensions to remain
				// loaded during pregeneration.
				int dimId = this.world.getWorld().provider.getDimension();
				if(dimId != 0)
				{
					// TODO: Assuming this won't cause concurrency problems with
					// net.minecraftforge.server.command.ChunkGenWorker, which
					// also uses keepDimensionLoaded
					DimensionManager.keepDimensionLoaded(dimId, true);
					if(
						((ForgeEngine)OTG.getEngine()).getWorldLoader().isWorldUnloaded(this.world.getName())
					)
					{
						OTGDimensionManager.initDimension(dimId);
					}
				}
				pregenerate();
			}

			this.processing = false;
		}
	}

	private void pregenerate()
	{
    	// Check if there are chunks that need to be pre-generated
        if(this.spawned < this.total && this.pregenerationRadius > 0)
        {
    		ChunkProviderServer chunkProvider = (ChunkProviderServer) this.world.getWorld().getChunkProvider();
    		if(chunkProvider == null)
    		{
    			// When loading/unloading/reloading dimensions 
    			// the chunkprovider can be null, this will correct
    			// itself automatically when worlds are properly loaded
    			// unloaded(?)
    			this.pregeneratorIsRunning = false;
    			return;
    		}

    		if(!this.pregeneratorIsRunning)
    		{
    			this.startTime = System.currentTimeMillis();
    		}

    		this.pregeneratorIsRunning = true;
    		
   			this.currentX = -this.pregenerationRadius;
   			this.currentZ = -this.pregenerationRadius;

    		// Spawn all chunks within the pre-generation radius
            // Spawn chunks in a rectangle around a center block.
        	// Rectangle grows: Add row to right, add row to left, add row to bottom, add row to top, repeat.
        	// TODO: Rewrite to make use of the way MC groups chunks into region files?

    		boolean leftEdgeFound = this.left >= this.pregenerationRadius;
    		boolean rightEdgeFound = this.right >= this.pregenerationRadius;
    		boolean topEdgeFound = this.top >= this.pregenerationRadius;
    		boolean bottomEdgeFound = this.bottom >= this.pregenerationRadius;

    		this.spawnedThisTick = 0;

    		int spawnChunkX = this.preGeneratorCenterPoint.getChunkX();
    		int spawnChunkZ = this.preGeneratorCenterPoint.getChunkZ();

			// This cycle might be stopped at any point because of the max chunks per server tick.
			// Progress within a cycle is tracked using iTop, iBottom, iLeft, iRight		    	
    		while(!(leftEdgeFound && rightEdgeFound && topEdgeFound && bottomEdgeFound))
			{
	    		// Generate the center block
				if(this.cycle == 0 && this.spawned == 0)
				{
					pregenerateChunk(spawnChunkX, spawnChunkZ);
					this.cycle += 1;
	    			if(this.spawnedThisTick >= this.maxSpawnPerTick)
	    			{
	    				pause();
	    				return;
	    			}
				}
				
	    		if(!rightEdgeFound && this.iBottom == Integer.MIN_VALUE && this.iTop == Integer.MIN_VALUE && this.iRight < this.bottom)
	    		{
	    			for(int i = -this.top; i <= this.bottom; i++)
	    			{
	    				this.currentX = spawnChunkX + this.cycle;
	    				this.currentZ = spawnChunkZ + i;

						if(i > this.iRight) // Check if we haven't generated this chunk in a previous server tick
						{
							this.iRight = i;
							
							pregenerateChunk(this.currentX, this.currentZ);

			    			if(this.spawnedThisTick >= this.maxSpawnPerTick)
			    			{
			    				if(i == this.bottom)
			    				{
			    					this.right++;
			    	        		if(this.right >= this.pregenerationRadius)
			    	        		{
			    	        			rightEdgeFound = true;
			    	        		}
			    				}
			    				pause();
			    				return;
			    			}
						}
	    			}
	    			this.right++;
	        		if(this.right >= this.pregenerationRadius)
	        		{
	        			rightEdgeFound = true;
	        		}
	    		}

	    		if(!leftEdgeFound && this.iBottom == Integer.MIN_VALUE && iTop == Integer.MIN_VALUE && this.iLeft < this.bottom)
	    		{
	    			for(int i = -this.top; i <= this.bottom; i++)
	    			{
	    				this.currentX = spawnChunkX - this.cycle;
	    				this.currentZ = spawnChunkZ + i;

						if(i > this.iLeft) // Check if we haven't generated this chunk in a previous server tick
						{
							this.iLeft = i;
							
							pregenerateChunk(this.currentX, this.currentZ);

			    			if(this.spawnedThisTick >= this.maxSpawnPerTick)
			    			{
			    				if(i == this.bottom)
			    				{
			    					this.left++;
			    		    		if(this.left >= this.pregenerationRadius)
			    		    		{
			    		    			leftEdgeFound = true;
			    		    		}
			    				}
			    				pause();
			    				return;
			    			}
						}
	    			}
	    			this.left++;
    	    		if(this.left >= this.pregenerationRadius)
    	    		{
    	    			leftEdgeFound = true;
    	    		}
	    		}

	    		if(!bottomEdgeFound && this.iBottom < this.right)
	    		{
	    			for(int i = -this.left; i <= this.right; i++)
	    			{
	    				this.currentX = spawnChunkX + i;
	    				this.currentZ = spawnChunkZ + this.cycle;

						if(i > this.iBottom) // Check if we haven't generated this chunk in a previous server tick
						{
							this.iBottom = i;
							
							pregenerateChunk(this.currentX, this.currentZ);

			    			if(this.spawnedThisTick >= this.maxSpawnPerTick)
			    			{
			    				if(i == this.right)
			    				{
			    					this.bottom++;
			    		    		if(this.bottom >= this.pregenerationRadius)
			    		    		{
			    		    			bottomEdgeFound = true;
			    		    		}
			    				}
			    				pause();
			    				return;
			    			}
						}
	    			}
	    			this.bottom++;
    	    		if(this.bottom >= this.pregenerationRadius)
    	    		{
    	    			bottomEdgeFound = true;
    	    		}
	    		}

	    		if(!topEdgeFound && this.iTop < this.right)
	    		{
	    			for(int i = -this.left; i <= this.right; i++)
	    			{
	    				this.currentX = spawnChunkX + i;
	    				this.currentZ = spawnChunkZ - this.cycle;

						if(i > this.iTop) // Check if we haven't generated this chunk in a previous server tick
						{
							this.iTop = i;

							pregenerateChunk(this.currentX, this.currentZ);

			    			if(this.spawnedThisTick >= this.maxSpawnPerTick)
			    			{
			    				if(i == this.right)
			    				{
			    					this.top++;
			    		    		if(this.top >= this.pregenerationRadius)
			    		    		{
			    		    			topEdgeFound = true;
			    		    		}
			    				}
			    				pause();
			    				return;
			    			}
						}
	    			}
	    			this.top++;
    	    		if(this.top >= this.pregenerationRadius)
    	    		{
    	    			topEdgeFound = true;
    	    		}
	    		}

	    		// Cycle completed, update/reset cycle progress
	    		this.iLeft = Integer.MIN_VALUE;
	    		this.iBottom = Integer.MIN_VALUE;
	    		this.iRight = Integer.MIN_VALUE;
	    		this.iTop = Integer.MIN_VALUE;
	    		this.cycle += 1;
			}
			
    		// Pregeneration complete
    		
    		flushChunksToDisk();
    		
			long timeNow = System.currentTimeMillis();
			this.timeTaken += (timeNow - this.startTime);
			savePregeneratorData(false);
			
			// Allow dimensions to be unloaded when pregeneration is complete.
			int dimId = this.world.getWorld().provider.getDimension();
			if(dimId != 0)
			{
				// "keepDimensionsLoaded" shouldn't interfere with canDropChunks/CanUnload, except that we're using it to disallow
				// unloading of dims while they're being pregenerated.
				// TODO: Assuming here that only OTG ever changes keepDimensionLoaded for OTG dims, and we set it to true earlier.
				DimensionManager.keepDimensionLoaded(dimId, false);
			}
        }
        this.pregeneratorIsRunning = false;
	}

	private void flushChunksToDisk()
	{
		//if(1 == 1) { return; }
		
        if (this.world.getWorld().getMinecraftServer().getPlayerList() != null)
        {
        	this.world.getWorld().getMinecraftServer().getPlayerList().saveAllPlayerData();
        }

        boolean flag = ((WorldServer)this.world.getWorld()).disableLevelSaving;
        ((WorldServer)this.world.getWorld()).disableLevelSaving = false;
        try {
			((WorldServer)this.world.getWorld()).saveAllChunks(true, (IProgressUpdate)null);
		} catch (MinecraftException e) {
			e.printStackTrace();
		}
        try
        {
        	((WorldServer)this.world.getWorld()).flushToDisk();
        }
        catch(NoSuchElementException ex)
        {
        	// TODO: Happens sometimes during pregeneration, likely a threading issue.
        	// Hopefully aborting and retrying later won't cause problems.
        	OTG.log(LogMarker.INFO, "An error occurred while flushing chunks to disk, aborting flush and continuing pregeneration.");
        }
        ((WorldServer)this.world.getWorld()).disableLevelSaving = flag;        
    }

	private void pause()
	{
		long timeNow = System.currentTimeMillis();
		this.timeTaken += timeNow - this.startTime;
		this.startTime = timeNow;
		if(this.spawned == this.total) // Pregeneration complete
		{
    		// Cycle completed, reset cycle progress
			this.iLeft = Integer.MIN_VALUE;
			this.iBottom = Integer.MIN_VALUE;
			this.iRight = Integer.MIN_VALUE;
			this.iTop = Integer.MIN_VALUE;
			this.cycle += 1;
			
			flushChunksToDisk();
			
			savePregeneratorData(false);
			
			// Allow dimensions to be unloaded when pregeneration is complete.
			int dimId = this.world.getWorld().provider.getDimension();
			if(dimId != 0)
			{
				// "keepDimensionsLoaded" shouldn't interfere with canDropChunks/CanUnload, except that we're using it to disallow
				// unloading of dims while they're being pregenerated.
				// TODO: Assuming here that only OTG ever changes keepDimensionLoaded for OTG dims, and we set it to true earlier.
				DimensionManager.keepDimensionLoaded(dimId, false); 
			}
			
		} else {
			// Pre-generation cycle cannot be completed.
			// Save progress so we can continue and retry on the next server tick.
			this.processing = false;
		}
	}

	private void pregenerateChunk(int currentX, int currentZ)
	{
		this.spawned++;
		this.spawnedThisTick++;
			
		ChunkProviderServer chunkProvider = (ChunkProviderServer) this.world.getWorld().getChunkProvider();
		Chunk chunk1 = chunkProvider.getLoadedChunk(currentX, currentZ);
    	if(chunk1 == null || !chunk1.isPopulated())
    	{
    		// Load the 2x2 chunk area being populated, then populate the target chunk.
    		// This may populate the other 3 chunks, if their surrounding chunks are loaded.
    		if(chunkProvider.getLoadedChunk(currentX + 1, currentZ) == null)
    		{
    			chunkProvider.provideChunk(currentX + 1, currentZ);
    		}
    		if(chunkProvider.getLoadedChunk(currentX, currentZ + 1) == null)
    		{
    			chunkProvider.provideChunk(currentX, currentZ + 1);
    		}
    		if(chunkProvider.getLoadedChunk(currentX + 1, currentZ + 1) == null)
    		{
    			chunkProvider.provideChunk(currentX + 1, currentZ + 1);
    		}
    		chunk1 = chunkProvider.provideChunk(currentX, currentZ);
    	}
		
		if(this.spawned - this.lastSpawnedWhenSaved > this.compressCustomStructureCacheThreshHold)
		{
			this.lastSpawnedWhenSaved = this.spawned;
			flushChunksToDisk();
		}
		
		long timeNow = System.currentTimeMillis();
		this.timeTaken += (timeNow - this.startTime);
		this.startTime = timeNow;
		updateProgressMessage(true);
	}

	private void updateProgressMessage (boolean loggingCanBeIgnored)
	{
		if(this.spawned < this.total)
		{
			boolean dontLog = false;
			// Show progress update max once per second
			if(loggingCanBeIgnored = true && System.currentTimeMillis() - this.lastMessage < 1000l)
			{
				dontLog = true;
			} else {
				this.lastMessage = System.currentTimeMillis();
			}

			int hours = (int)Math.floor(this.timeTaken / 1000d / 60d / 60d);
			int minutes = (int)Math.floor(this.timeTaken / 1000d / 60d) - (hours * 60);
			int seconds = (int)Math.floor(this.timeTaken / 1000d) - (minutes * 60) - (hours * 60 * 60);
			String sElapsedTime = (hours < 10 ? "0" + hours : hours) + ":" + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);

			double eTA = (this.total / this.spawned) * this.timeTaken - this.timeTaken;

			hours = (int)Math.floor(eTA / 1000d / 60d / 60d);
			minutes = (int)Math.floor(eTA / 1000d / 60d) - (hours * 60);
			seconds = (int)Math.floor(eTA / 1000d) - (minutes * 60) - (hours * 60 * 60);

			String estimatedTime = (hours < 10 ? "0" + hours : hours) + ":" + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);

	        long i = Runtime.getRuntime().maxMemory();
	        long j = Runtime.getRuntime().totalMemory();
	        long k = Runtime.getRuntime().freeMemory();
	        long l = j - k;
	        String memoryUsage = " Mem: " + Long.valueOf(l * 100L / i) + "% " + Long.valueOf(bytesToMb(l)) + " / " +  Long.valueOf(bytesToMb(i)) + " MB ";

	        this.progressScreenWorldSizeInBlocks = (this.pregenerationRadius * 2 + 1) * 16;
	        this.preGeneratorProgressStatus = (int)this.spawned + "/" + (int)this.total;
	        this.preGeneratorProgress = (int)Math.round(((this.spawned / (double)(this.total)) * 100)) + "";
	        this.progressScreenElapsedTime = sElapsedTime;
	        this.progressScreenEstimatedTime = estimatedTime;
	        this.progressScreenCycle = this.cycle + "";
	        this.progressScreenRadius = this.pregenerationRadius + "";
			if(!dontLog)
			{
				OTG.log(LogMarker.INFO, "Pre-generating world \"" + this.pregenerationWorld + "\". Radius: " + this.cycle + "/" + this.pregenerationRadius + " Spawned: " + (int)this.spawned + "/" + (int)this.total + " " + (int)Math.round(((this.spawned / (double)(this.total)) * 100)) + "% done. Elapsed: " + sElapsedTime + " ETA: " + estimatedTime + memoryUsage);
			}
		} else {

			int hours = (int)Math.floor(this.timeTaken / 1000d / 60d / 60d);
			int minutes = (int)Math.floor(this.timeTaken / 1000d / 60d) - (hours * 60);
			int seconds = (int)Math.floor(this.timeTaken / 1000d) - (minutes * 60) - (hours * 60 * 60);
			String sElapsedTime = (hours < 10 ? "0" + hours : hours) + ":" + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);

			this.preGeneratorProgressStatus = "Done";
			this.preGeneratorProgress = "";
			this.progressScreenElapsedTime = "";
			this.progressScreenEstimatedTime = "";
			this.progressScreenWorldSizeInBlocks = 0;
			this.progressScreenCycle = this.pregenerationRadius + "";
			this.progressScreenRadius = this.pregenerationRadius + "";
			OTG.log(LogMarker.INFO, "Pre-generating chunks done for world " + this.pregenerationWorld + ", " + ((int)this.spawned) + " chunks spawned in " + sElapsedTime);		
		}
	}

    private long bytesToMb(long bytes)
    {
        return bytes / 1024L / 1024L;
    }

    public void shutDown()
    {
    	if(this.pregeneratorIsRunning)
    	{
    		// Don't save pregenerator data on MP client
    		if(this.world.world != null)
    		{
    			savePregeneratorData(false);
    		}	    	
    		this.pregeneratorIsRunning = false;
    	}
    }

    // Saving / Loading
    // TODO: It's crude but it works, can improve later

    public void savePregeneratorData()
    {
    	// Don't save pregenerator data on MP client
    	if(this.world.world != null)
    	{
    		savePregeneratorData(false);
    	}
    }

	private void savePregeneratorData(boolean forceSave)
	{
		if(this.pregeneratorIsRunning || forceSave)
		{
			int dimensionId = this.world.getDimensionId();
			File pregeneratedChunksFile = new File(this.world.getWorldSaveDir().getAbsolutePath() + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + (dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") + WorldStandardValues.PregeneratedChunksFileName);
			File pregeneratedChunksBackupFile = new File(this.world.getWorldSaveDir().getAbsolutePath() + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + (dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") + WorldStandardValues.PregeneratedChunksBackupFileName);

			StringBuilder stringbuilder = new StringBuilder();
			stringbuilder.append(this.spawned + "," + this.left + "," + this.top + "," + this.right + "," + this.bottom + "," + this.cycle + "," + this.timeTaken + "," + this.iTop + "," + this.iBottom + "," + this.iLeft + "," + this.iRight + "," + this.pregenerationRadius + "," + (this.preGeneratorCenterPoint != null ? this.preGeneratorCenterPoint.getChunkX() : "null") + "," + (this.preGeneratorCenterPoint != null ? this.preGeneratorCenterPoint.getChunkZ() : "null"));

			BufferedWriter writer = null;
	        try
	        {
	    		if(!pregeneratedChunksFile.exists())
	    		{
	    			pregeneratedChunksFile.getParentFile().mkdirs();
	    		} else {
	    			Files.move(pregeneratedChunksFile.toPath(), pregeneratedChunksBackupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	    		}
	    		
	        	writer = new BufferedWriter(new FileWriter(pregeneratedChunksFile));
	            writer.write(stringbuilder.toString());
	            OTG.log(LogMarker.DEBUG, "Pre-generator data saved");
	        }
	        catch (IOException e)
	        {
				e.printStackTrace();
				throw new RuntimeException(
					"OTG encountered a critical error writing " + pregeneratedChunksFile.getAbsolutePath() + ", exiting. "
					+ "OTG automatically backs up files before writing and will try to use the backup when loading. "					
					+ "If your world's " + WorldStandardValues.PregeneratedChunksFileName + " and its backup have been corrupted, you can "
					+ "replace it with a backup.");
	        }
	        finally
	        {
	            try
	            {
	                writer.close();
	            } catch (Exception e) { }
	        }
		}
	}

	private void loadPregeneratorData()
	{
		int dimensionId = this.world.getDimensionId();
		File pregeneratedChunksFile = new File(this.world.getWorldSaveDir().getAbsolutePath() + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + (dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") + WorldStandardValues.PregeneratedChunksFileName);
		File pregeneratedChunksBackupFile = new File(this.world.getWorldSaveDir().getAbsolutePath() + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + (dimensionId != 0 ? "DIM-" + dimensionId + File.separator : "") + WorldStandardValues.PregeneratedChunksBackupFileName);

		if(!pregeneratedChunksFile.exists() && !pregeneratedChunksBackupFile.exists())
		{
			saveDefaults();
			return;
		}
		
		if(pregeneratedChunksFile.exists())
		{
			String[] pregeneratedChunksFileValues = {};
			boolean bSuccess = false;			
			try
			{
				StringBuilder stringbuilder = new StringBuilder();
				BufferedReader reader = new BufferedReader(new FileReader(pregeneratedChunksFile));
				try
				{
					String line = reader.readLine();
				    while (line != null)
				    {
				    	stringbuilder.append(line);
				        line = reader.readLine();
				    }
				    if(stringbuilder.length() > 0)
				    {
				    	pregeneratedChunksFileValues = stringbuilder.toString().split(",");
					    bSuccess = true;
					    OTG.log(LogMarker.DEBUG, "Pre-generator data loaded");				    	
				    }
				} finally {
					reader.close();
				}

			}
			catch (IOException e)
			{
				e.printStackTrace();
				OTG.log(LogMarker.WARN, "Failed to load " + pregeneratedChunksFile.getAbsolutePath() + ", trying to load backup.");
			}
			
			if(bSuccess)
			{
				try
				{
					parsePregeneratorData(pregeneratedChunksFileValues);
					return;
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
					OTG.log(LogMarker.WARN, "Failed to load " + pregeneratedChunksFile.getAbsolutePath() + ", trying to load backup.");
				}
			}			
		}
		
		if(pregeneratedChunksBackupFile.exists())
		{
			String[] pregeneratedChunksFileValues = {};
			boolean bSuccess = false;			
			try
			{
				StringBuilder stringbuilder = new StringBuilder();
				BufferedReader reader = new BufferedReader(new FileReader(pregeneratedChunksBackupFile));
				try
				{
					String line = reader.readLine();
				    while (line != null)
				    {
				    	stringbuilder.append(line);
				        line = reader.readLine();
				    }
				    if(stringbuilder.length() > 0)
				    {
				    	pregeneratedChunksFileValues = stringbuilder.toString().split(",");
					    bSuccess = true;
					    OTG.log(LogMarker.DEBUG, "Pre-generator data loaded");				    	
				    }
				} finally {
					reader.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			if(bSuccess)
			{
				try
				{
					parsePregeneratorData(pregeneratedChunksFileValues);
					return;
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}

		throw new RuntimeException(
			"OTG encountered a critical error loading " + pregeneratedChunksFile.getAbsolutePath() + " and could not load a backup, exiting. "
			+ "OTG automatically backs up files before writing and will try to use the backup when loading. "
			+ "If your dimension's " + WorldStandardValues.PregeneratedChunksFileName + " and its backup have been corrupted, you can "
			+ "replace it with a backup.");
	}
	
	private void parsePregeneratorData(String[] pregeneratedChunksFileValues)
	{
		if(pregeneratedChunksFileValues.length > 0)
		{
			this.spawned = Integer.parseInt(pregeneratedChunksFileValues[0]);
			this.lastSpawnedWhenSaved = this.spawned;
			
			this.left = Integer.parseInt(pregeneratedChunksFileValues[1]);
			this.top = Integer.parseInt(pregeneratedChunksFileValues[2]);
			this.right = Integer.parseInt(pregeneratedChunksFileValues[3]);
			this.bottom = Integer.parseInt(pregeneratedChunksFileValues[4]);

			if(this.spawned > 0)
			{
				// This should hopefully fix corrupted pregendata saves for > v6 < v8.3_r4
				this.cycle = Math.min(Math.min(Math.min(this.left, this.top), this.right), this.bottom) + 1;
			} else {
				this.cycle = 0;
			}
			
			this.timeTaken = Long.parseLong(pregeneratedChunksFileValues[6]); // Elapsed time
			
			this.iTop = Integer.parseInt(pregeneratedChunksFileValues[7]);
			this.iBottom = Integer.parseInt(pregeneratedChunksFileValues[8]);
			this.iLeft = Integer.parseInt(pregeneratedChunksFileValues[9]);
			this.iRight = Integer.parseInt(pregeneratedChunksFileValues[10]);

			this.pregenerationRadius = Integer.parseInt(pregeneratedChunksFileValues[11]);

			if(pregeneratedChunksFileValues[12] != null && pregeneratedChunksFileValues[13] != null)
			{
				this.preGeneratorCenterPoint = ChunkCoordinate.fromChunkCoords(Integer.parseInt(pregeneratedChunksFileValues[12]), Integer.parseInt(pregeneratedChunksFileValues[13]));
				this.pregeneratorIsInitialised = true;
			} else {
				this.preGeneratorCenterPoint = null;
			}

			this.total = (this.pregenerationRadius * 2 + 1) * (this.pregenerationRadius * 2 + 1);	    	
		} else {
			saveDefaults();
		}
	}
	
	private void saveDefaults()
	{
		this.spawned = 0;

		this.left = 0;
		this.top = 0;
		this.right = 0;
		this.bottom = 0;

		this.cycle = 0;
		this.timeTaken = 0;

		this.iTop = Integer.MIN_VALUE;
		this.iBottom = Integer.MIN_VALUE;
		this.iLeft = Integer.MIN_VALUE;
		this.iRight = Integer.MIN_VALUE;

		this.preGeneratorCenterPoint = null; // Will be set after world spawn point has been determined
		
		DimensionConfig dimConfig = OTG.getDimensionsConfig().getDimensionConfig(this.world.getName());
		this.setPregenerationRadius(dimConfig.PregeneratorRadiusInChunks);

		savePregeneratorData(false);
	}
}
