package com.pg85.otg.forge.generator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import net.minecraft.world.gen.ChunkProviderServer;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;

public class Pregenerator
{
	private int currentX;
    private int currentZ;
	private boolean processing = false;
    private long startTime = System.currentTimeMillis();
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
	private int spawned = 1;
	private double total;
	private int spawnedThisTick = 0;
	private boolean pregeneratorIsRunning;
	private int maxSpawnPerTick;

	// In-game UI
	public String pregenerationWorld = "";
	public String preGeneratorProgressStatus = "";
	public String preGeneratorProgress = "";
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

		maxSpawnPerTick = OTG.getPluginConfig().pregeneratorMaxChunksPerTick;
    	pregenerationWorld = world.getConfigs().getWorldConfig().getName();
	}

	public int getPregenerationRadius()
	{
		return pregenerationRadius;
	}
	
	/**
	 * Only used when settings pregenerator status for MP clients (that don't actually get run)
	 * @return
	 */
	public void setPregeneratorIsRunning(boolean pregeneratorIsRunning)
	{		
		this.pregeneratorIsRunning = pregeneratorIsRunning;
	}

	/**
	 * Sets the pre-generation radius (in chunks) for the world.
	 * Radius cannot be smaller than currently pre-generated area.
	 * @param radius The desired radius.
	 * @param world Used to save pregenerator data to file.
	 * @return The radius value that was set for the pregenerator.
	 */
	public int setPregenerationRadius(int radius)
	{
		if(radius > cycle && radius > 0)
		{
			pregenerationRadius = radius;
		} else {
			pregenerationRadius = cycle;
		}
		// World can be null when creating new worlds on MP client
		if(world != null)
		{
			this.savePregeneratorData(true);
		}

    	total = (pregenerationRadius * 2 + 1) * (pregenerationRadius * 2 + 1);

		return pregenerationRadius;
	}

	public int getPregenerationBorderLeft()
	{
		return left;
	}

	public int getPregenerationBorderRight()
	{
		return right;
	}

	public int getPregenerationBorderTop()
	{
		return top;
	}

	public int getPregenerationBorderBottom()
	{
		return bottom;
	}

	public ChunkCoordinate getPregenerationCenterPoint()
	{
		return preGeneratorCenterPoint;
	}

	public boolean getPregeneratorIsRunning()
	{
		return pregeneratorIsRunning;
	}

	public void processTick()
	{
		if(!processing)
		{
			processing = true;

			pregenerate();

			processing = false;
		}		
	}

	private void pregenerate()
	{
    	// Check if there are chunks that need to be pre-generated
        if(spawned < total && pregenerationRadius > 0)
        {
    		ChunkProviderServer chunkProvider = (ChunkProviderServer) world.getWorld().getChunkProvider();
    		if(chunkProvider == null)
    		{
    			// When loading/unloading/reloading dimensions 
    			// the chunkprovider can be null, this will correct
    			// itself automatically when worlds are properly loaded
    			// unloaded(?)
    			pregeneratorIsRunning = false;
    			return;
    		}
        	
    		pregeneratorIsRunning = true;

	        currentX = -pregenerationRadius;
	        currentZ = -pregenerationRadius;

    		// Spawn all chunks within the pre-generation radius
            // Spawn chunks in a rectangle around a center block.
        	// Rectangle grows: Add row to right, add row to left, add row to bottom, add row to top, repeat.
        	// TODO: Rewrite to make use of the way MC groups chunks into region files?

    		boolean leftEdgeFound = false;
    		boolean rightEdgeFound = false;
    		boolean topEdgeFound = false;
    		boolean bottomEdgeFound = false;

    		spawnedThisTick = 0;

    		int spawnChunkX = preGeneratorCenterPoint.getChunkX();
    		int spawnChunkZ = preGeneratorCenterPoint.getChunkZ();

			// This cycle might be stopped at any point because of the max chunks per server tick.
			// Progress within a cycle is tracked using iTop, iBottom, iLeft, iRight
			while(!(leftEdgeFound && rightEdgeFound && topEdgeFound && bottomEdgeFound))
			{
	    		// Since the first cycle is cycle 1, the centermost chunk (at the spawnpoint) wont be pregenerated.
				if(cycle == 0)
				{
					pregenerateChunk(spawnChunkX, spawnChunkZ);
				}

				cycle += 1;

	    		// Check Right
	    		if(right >= pregenerationRadius)
	    		{
	    			rightEdgeFound = true;
	    		}
	    		if(!rightEdgeFound && iLeft == Integer.MIN_VALUE && iTop == Integer.MIN_VALUE && iBottom == Integer.MIN_VALUE)
	    		{
	    			boolean bSpawned = false;
	    			for(int i = -top; i <= bottom; i++)
	    			{
	    				currentX = spawnChunkX + cycle;
	    				currentZ = spawnChunkZ + i;

						if(i > iRight) // Check if we haven't generated this chunk in a previous server tick
						{
							bSpawned = true;
							iRight = i;
		    				spawned++;

							pregenerateChunk(currentX, currentZ);

			    			if(spawnedThisTick >= maxSpawnPerTick)
			    			{
			    				if(i == bottom)
			    				{
			    					right++;
			    				}
			    				pause();
			    				return;
			    			}
						}
	    			}
	    			if(bSpawned)
	    			{
	    				right++;
	    			}
	    		}

        		// Check Left
	    		if(left >= pregenerationRadius)
	    		{
	    			leftEdgeFound = true;
	    		}
	    		if(!leftEdgeFound && iTop == Integer.MIN_VALUE && iBottom == Integer.MIN_VALUE)
	    		{
	    			boolean bSpawned = false;
	    			for(int i = -top; i <= bottom; i++)
	    			{
	    				currentX = spawnChunkX - cycle;
	    				currentZ = spawnChunkZ + i;

						if(i > iLeft) // Check if we haven't generated this chunk in a previous server tick
						{
							bSpawned = true;
							iLeft = i;
							spawned++;

							pregenerateChunk(currentX, currentZ);

			    			if(spawnedThisTick >= maxSpawnPerTick)
			    			{
			    				if(i == bottom)
			    				{
			    					left++;
			    				}
			    				pause();
			    				return;
			    			}
						}
	    			}
	    			if(bSpawned)
	    			{
	    				left++;
	    			}
	    		}

        		// Check Bottom
	    		if(bottom >= pregenerationRadius)
	    		{
	    			bottomEdgeFound = true;
	    		}
	    		if(!bottomEdgeFound && iTop == Integer.MIN_VALUE)
	    		{
	    			boolean bSpawned = false;
	    			for(int i = -left; i <= right; i++)
	    			{
	    				currentX = spawnChunkX + i;
	    				currentZ = spawnChunkZ + cycle;

						if(i > iBottom) // Check if we haven't generated this chunk in a previous server tick
						{
							bSpawned = true;
							iBottom = i;
							spawned++;

							pregenerateChunk(currentX, currentZ);

			    			if(spawnedThisTick >= maxSpawnPerTick)
			    			{
			    				if(i == right)
			    				{
			    					bottom++;
			    				}
			    				pause();
			    				return;
			    			}
						}
	    			}
	    			if(bSpawned)
	    			{
	    				bottom++;
	    			}
	    		}

        		// Check Top
	    		if(top >= pregenerationRadius)
	    		{
	    			topEdgeFound = true;
	    		}
	    		if(!topEdgeFound)
	    		{
	    			boolean bSpawned = false;
	    			for(int i = -left; i <= right; i++)
	    			{
	    				currentX = spawnChunkX + i;
	    				currentZ = spawnChunkZ - cycle;

						if(i > iTop) // Check if we haven't generated this chunk in a previous server tick
						{
							bSpawned = true;
							iTop = i;
							spawned++;

							pregenerateChunk(currentX, currentZ);

			    			if(spawnedThisTick >= maxSpawnPerTick)
			    			{
			    				if(i == right)
			    				{
			    					top++;
			    				}
			    				pause();
			    				return;
			    			}
						}
	    			}
	    			if(bSpawned)
	    			{
	    				top++;
	    			}
	    		}

	    		// Cycle completed, reset cycle progress
    			iLeft = Integer.MIN_VALUE;
    			iBottom = Integer.MIN_VALUE;
    			iRight = Integer.MIN_VALUE;
    			iTop = Integer.MIN_VALUE;
			}
			
			this.world.getChunkGenerator().clearChunkCache(false);
			savePregeneratorData(false);
        }
        pregeneratorIsRunning = false;
	}

	private void pause()
	{
		if(spawned == total) // Done spawning
		{
    		// Cycle completed, reset cycle progress
			iLeft = Integer.MIN_VALUE;
			iBottom = Integer.MIN_VALUE;
			iRight = Integer.MIN_VALUE;
			iTop = Integer.MIN_VALUE;

			savePregeneratorData(false);
		} else {
			// Pre-generation cycle cannot be completed.
			// Save progress so we can continue and retry on the next server tick.
			cycle -= 1;
			processing = false;
		}		
	}

	private void pregenerateChunk(int currentX, int currentZ)
	{
		updateProgressMessage(true);
		
		// Make sure the 3 surrounding chunks are loaded, or this chunk won't get populated.
        world.getWorld().getChunkProvider().provideChunk(currentX + 1, currentZ);
        world.getWorld().getChunkProvider().provideChunk(currentX, currentZ + 1);
        world.getWorld().getChunkProvider().provideChunk(currentX + 1, currentZ + 1);

        // Provide and populate this chunk
        world.getWorld().getChunkProvider().provideChunk(currentX, currentZ);

		spawnedThisTick++;
	}

	private void updateProgressMessage (boolean loggingCanBeIgnored)
	{
		if(spawned < total)
		{
			boolean dontLog = false;
			// Show progress update max once per second
			if(loggingCanBeIgnored = true && System.currentTimeMillis() - lastMessage < 1000l)
			{
				dontLog = true;
			} else {
				lastMessage = System.currentTimeMillis();
			}

			long elapsedTime = System.currentTimeMillis() - startTime;
			int hours = (int)Math.floor(elapsedTime / 1000d / 60d / 60d);
			int minutes = (int)Math.floor(elapsedTime / 1000d / 60d) - (hours * 60);
			int seconds = (int)Math.floor(elapsedTime / 1000d) - (minutes * 60) - (hours * 60 * 60);
			String sElapsedTime = (hours < 10 ? "0" + hours : hours) + ":" + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);

			double eTA = (total / spawned) * elapsedTime - elapsedTime;

			hours = (int)Math.floor(eTA / 1000d / 60d / 60d);
			minutes = (int)Math.floor(eTA / 1000d / 60d) - (hours * 60);
			seconds = (int)Math.floor(eTA / 1000d) - (minutes * 60) - (hours * 60 * 60);

			String estimatedTime = (hours < 10 ? "0" + hours : hours) + ":" + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);

	        long i = Runtime.getRuntime().maxMemory();
	        long j = Runtime.getRuntime().totalMemory();
	        long k = Runtime.getRuntime().freeMemory();
	        long l = j - k;
	        String memoryUsage = " Mem: " + Long.valueOf(l * 100L / i) + "% " + Long.valueOf(bytesToMb(l)) + " / " +  Long.valueOf(bytesToMb(i)) + " MB ";

	        progressScreenWorldSizeInBlocks = (pregenerationRadius * 2 + 1) * 16;
			preGeneratorProgressStatus = (int)spawned + "/" + (int)total;
			preGeneratorProgress = (int)Math.round(((spawned / (double)(total)) * 100)) + "";
			progressScreenElapsedTime = sElapsedTime;
			progressScreenEstimatedTime = estimatedTime;
			if(!dontLog)
			{
				OTG.log(LogMarker.INFO, "Pre-generating world \"" + pregenerationWorld + "\". Radius: " + pregenerationRadius + " Spawned: " + (int)spawned + "/" + (int)total + " " + (int)Math.round(((spawned / (double)(total)) * 100)) + "% done. Elapsed: " + sElapsedTime + " ETA: " + estimatedTime + memoryUsage);
			}
		} else {

			long elapsedTime = System.currentTimeMillis() - startTime;
			int hours = (int)Math.floor(elapsedTime / 1000d / 60d / 60d);
			int minutes = (int)Math.floor(elapsedTime / 1000d / 60d) - (hours * 60);
			int seconds = (int)Math.floor(elapsedTime / 1000d) - (minutes * 60) - (hours * 60 * 60);
			String sElapsedTime = (hours < 10 ? "0" + hours : hours) + ":" + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);

			preGeneratorProgressStatus = "Done";
			preGeneratorProgress = "";
			progressScreenElapsedTime = "";
			progressScreenEstimatedTime = "";
			progressScreenWorldSizeInBlocks = 0;
			OTG.log(LogMarker.INFO, "Pre-generating chunks done for world " + pregenerationWorld + ", " + ((int)spawned) + " chunks spawned in " + sElapsedTime);		
		}
	}

    private long bytesToMb(long bytes)
    {
        return bytes / 1024L / 1024L;
    }

    public void shutDown()
    {
    	if(pregeneratorIsRunning)
    	{
    		// Don't save pregenerator data on MP client
    		if(this.world.world != null)
    		{
    			savePregeneratorData(false);
    		}	    	
	    	pregeneratorIsRunning = false;
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
		if(pregeneratorIsRunning || forceSave)
		{
			int dimensionId = world.getDimensionId();
			File pregeneratedChunksFile = new File(world.getWorldSaveDir().getAbsolutePath() + "/OpenTerrainGenerator/" + (dimensionId != 0 ? "DIM-" + dimensionId + "/" : "") + WorldStandardValues.PregeneratedChunksFileName);

			if(pregeneratedChunksFile.exists())
			{
				pregeneratedChunksFile.delete();
			}

			StringBuilder stringbuilder = new StringBuilder();
			stringbuilder.append(spawned + "," + left + "," + top + "," + right + "," + bottom + "," + cycle + "," + (System.currentTimeMillis() - startTime) + "," + iTop + "," + iBottom + "," + iLeft + "," + iRight + "," + pregenerationRadius + "," + preGeneratorCenterPoint.getChunkX() + "," + preGeneratorCenterPoint.getChunkZ());

			BufferedWriter writer = null;
	        try
	        {
	        	pregeneratedChunksFile.getParentFile().mkdirs();
	        	writer = new BufferedWriter(new FileWriter(pregeneratedChunksFile));
	            writer.write(stringbuilder.toString());
	            OTG.log(LogMarker.DEBUG, "Pre-generator data saved");
	        }
	        catch (IOException e)
	        {
	        	OTG.log(LogMarker.ERROR, "Could not save pre-generator data.");
	            e.printStackTrace();
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
		int dimensionId = world.getDimensionId();
		File pregeneratedChunksFile = new File(world.getWorldSaveDir().getAbsolutePath() + "/OpenTerrainGenerator/" + (dimensionId != 0 ? "DIM-" + dimensionId + "/" : "") + WorldStandardValues.PregeneratedChunksFileName);

		String[] pregeneratedChunksFileValues = {};
		if(pregeneratedChunksFile.exists())
		{
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
				    }
				    OTG.log(LogMarker.DEBUG, "Pre-generator data loaded");
				} finally {
					reader.close();
				}

			}
			catch (FileNotFoundException e1)
			{
				e1.printStackTrace();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}

		if(pregeneratedChunksFileValues.length > 0)
		{
			spawned = Integer.parseInt(pregeneratedChunksFileValues[0]);

			left = Integer.parseInt(pregeneratedChunksFileValues[1]);
			top = Integer.parseInt(pregeneratedChunksFileValues[2]);
			right = Integer.parseInt(pregeneratedChunksFileValues[3]);
			bottom = Integer.parseInt(pregeneratedChunksFileValues[4]);

			cycle = Integer.parseInt(pregeneratedChunksFileValues[5]);
			startTime = System.currentTimeMillis() - Long.parseLong(pregeneratedChunksFileValues[6]); // Elapsed time

			iTop = Integer.parseInt(pregeneratedChunksFileValues[7]);
			iBottom = Integer.parseInt(pregeneratedChunksFileValues[8]);
			iLeft = Integer.parseInt(pregeneratedChunksFileValues[9]);
			iRight = Integer.parseInt(pregeneratedChunksFileValues[10]);

			pregenerationRadius = Integer.parseInt(pregeneratedChunksFileValues[11]);

			preGeneratorCenterPoint = ChunkCoordinate.fromChunkCoords(Integer.parseInt(pregeneratedChunksFileValues[12]), Integer.parseInt(pregeneratedChunksFileValues[13]));

	    	total = (pregenerationRadius * 2 + 1) * (pregenerationRadius * 2 + 1);
		} else {
			spawned = 1;

			left = 0;
			top = 0;
			right = 0;
			bottom = 0;

			cycle = 0;
			startTime = System.currentTimeMillis();

			iTop = Integer.MIN_VALUE;
			iBottom = Integer.MIN_VALUE;
			iLeft = Integer.MIN_VALUE;
			iRight = Integer.MIN_VALUE;

			preGeneratorCenterPoint = world.getSpawnChunk();

			DimensionConfig dimConfig = OTG.getDimensionsConfig().getDimensionConfig(world.getName());
			this.setPregenerationRadius(dimConfig.PregeneratorRadiusInChunks);

			savePregeneratorData(false);
		}
	}
}
