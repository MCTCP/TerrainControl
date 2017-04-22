package com.khorn.terraincontrol.forge.generator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.storage.RegionFileCache;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import com.google.common.base.Strings;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigProvider;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.forge.ForgeEngine;
import com.khorn.terraincontrol.forge.TCWorldType;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.ChunkCoordinate;

public class Pregenerator
{    
	private int pregenerationRadius;
	
	public int getPregenerationRadius()
	{
		return pregenerationRadius;
	}

	/**
	 * Sets the pre-generation radius (in chunks) for the world.
	 * Radius cannot be smaller than currently pre-generated area.
	 * @param radius The desired radius.
	 * @return The radius value that was set for the pregenerator.
	 */
	public int setPregenerationRadius(int radius)
	{
		return setPregenerationRadius(radius, null);
	}
	
	/**
	 * Sets the pre-generation radius (in chunks) for the world.
	 * Radius cannot be smaller than currently pre-generated area.
	 * @param radius The desired radius.
	 * @param world Used to save pregenerator data to file.
	 * @return The radius value that was set for the pregenerator.
	 */
	public int setPregenerationRadius(int radius, World world)
	{		
		if(pregenerationRadius == 0)
		{
			// Reset start time in case this method 
			// starts pre-generation in this world
			// for the first time and no start time 
			// info was saved previously.
			resetPregenerator(); 
		}
		if(!preGeneratorIsRunning || (radius > cycle && radius > 0))
		{
			pregenerationRadius = radius;
		} else {
			pregenerationRadius = cycle;
		}
		if(world != null)
		{
			this.SavePreGeneratorData(world);
		}
		
		return pregenerationRadius;
	}

	public void resetPregenerator()
    {
		left = 0;
		right = 0;
		top = 0;
		bottom = 0;
		spawned = 1;
		cycle = 0;
		iLeft = Integer.MIN_VALUE;
		iRight = Integer.MIN_VALUE;
		iTop = Integer.MIN_VALUE;
		iBottom = Integer.MIN_VALUE;
		
		startTime = System.currentTimeMillis();
    }	
	
	boolean preGeneratorIsRunning;
	boolean processing = false;
	
    int radius = 0;
    int currentX;
    int currentZ;    
    
    long startTime;
    
	int spawned = 1;
	double total;

	int left = 0;
	int right = 0;
	int top = 0;
	int bottom = 0;
	int cycle = 0;
	
	int iLeft = Integer.MIN_VALUE;
	int iRight = Integer.MIN_VALUE;
	int iTop = Integer.MIN_VALUE;
	int iBottom = Integer.MIN_VALUE;
	
	int lastWorldHash = 0;
	
	int spawnedThisTick = 0;
	
	// In-game UI
	String pregenerationWorld = "";
	String preGeneratorProgressStatus = "";
	String preGeneratorProgress = "";
	String progressScreenElapsedTime = "";
	String progressScreenEstimatedTime = "";
	int progressScreenWorldSizeInBlocks;
	
	public void ProcessTick()
	{
		if(!processing)
		{
			processing = true;

			MinecraftServer mcServer = FMLCommonHandler.instance().getMinecraftServerInstance();			
			for(WorldServer worldServer : mcServer.worldServers)
			{
				if(worldServer.getWorldInfo().getTerrainType() instanceof TCWorldType && worldServer.provider.getDimension() == 0)
				{					
					LocalWorld world = ((ForgeEngine)TerrainControl.getEngine()).getWorld(worldServer);				
					
					if(world == null)
					{
						//TerrainControl.log(LogMarker.INFO, "Error: Server tick thread failed to load LocalWorld for world \"" + worldServer.getWorldInfo().getWorldName() + "\", cannot start pre-generation.");
						processing = false;
						return; // May be unloading / shutting down
					}
					
					ConfigProvider configProvider = world.getConfigs();
					
					if(configProvider == null)
					{
						//TerrainControl.log(LogMarker.INFO, "Error: Server tick thread failed to load world settings for world \"" + worldServer.getWorldInfo().getWorldName() + "\", cannot start pre-generation.");
						processing = false;
						return; // May be unloading / shutting down (?)
					}
					
					WorldConfig worldConfig = configProvider.getWorldConfig();
					
					if(worldConfig == null)
					{
						//TerrainControl.log(LogMarker.INFO, "Error: Server tick thread failed to load worldConfig for world \"" + worldServer.getWorldInfo().getWorldName() + "\", cannot start pre-generation.");
						processing = false;
						return; // May be unloading / shutting down (?)
					}

					if(worldConfig.PreGenerationRadius > 0)
					{
						Pregenerate(worldServer, worldConfig.PreGenerationRadius);
					} else {
						preGeneratorIsRunning = false;
					}
				}
			}
			processing = false;
		}
	}
	
	void Pregenerate(WorldServer worldServer, int pregenerationRadius)
	{	
		// Load any saved pre-generator data and/or set the default values
		// Don't load the same world each tick
		if(worldServer.hashCode() != lastWorldHash)
		{
			LoadPreGeneratorData(worldServer);
		}
		lastWorldHash = worldServer.hashCode();	
		
    	radius = pregenerationRadius;
    	total = (radius * 2 + 1) * (radius * 2 + 1);
		
    	// Check if there are chunks that need to be pre-generated
        if(spawned < total && radius > 0)
        {			    	
	        currentX = -radius;
	        currentZ = -radius;				    	
        	
			preGeneratorIsRunning = true;
	    	pregenerationWorld = worldServer.getWorldInfo().getWorldName();	    	
	    	      	
    		// Spawn all chunks within the pre-generation radius 
            // Spawn chunks in a rectangle around a center block. 
        	// Rectangle grows: Add row to right, add row to left, add row to bottom, add row to top, repeat.
        	// TODO: Rewrite to make use of the way MC groups chunks into region files?
	        
    		boolean leftEdgeFound = false;
    		boolean rightEdgeFound = false;
    		boolean topEdgeFound = false;
    		boolean bottomEdgeFound = false;
    			
    		int maxSpawnPerTick = TerrainControl.getPluginConfig().PregeneratorMaxChunksPerTick;
    		spawnedThisTick = 0;

    		BlockPos spawnPoint = worldServer.getSpawnPoint();
    		ChunkCoordinate spawnChunk = ChunkCoordinate.fromBlockCoords(spawnPoint.getX(), spawnPoint.getZ());
    		int spawnChunkX = spawnChunk.getChunkX();
    		int spawnChunkZ = spawnChunk.getChunkZ();
    		
			// This cycle might be stopped at any point because of the max chunks per server tick.
			// Progress within a cycle is tracked using iTop, iBottom, iLeft, iRight
			while(!(leftEdgeFound && rightEdgeFound && topEdgeFound && bottomEdgeFound))
			{ 				
	    		// TODO: Since the first cycle is cycle 1, the centermost chunk (at the spawnpoint) wont be pregenerated.
	    		// Probably doesnt matter though since that chunk shouldve been populated automatically when the server started
				cycle += 1;			
				
	    		// Check Right
	    		if(right >= radius)
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
		    				
							PreGenerateChunk(currentX, currentZ, worldServer);
							
			    			if(spawnedThisTick >= maxSpawnPerTick)
			    			{
			    				if(i == bottom)
			    				{
			    					right++;
			    				}
			    				Pause(worldServer);
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
	    		if(left >= radius)
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
							
							PreGenerateChunk(currentX, currentZ, worldServer);
						
			    			if(spawnedThisTick >= maxSpawnPerTick)
			    			{
			    				if(i == bottom)
			    				{
			    					left++;
			    				}
			    				Pause(worldServer);
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
	    		if(bottom >= radius)
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
							
							PreGenerateChunk(currentX, currentZ, worldServer);
							
			    			if(spawnedThisTick >= maxSpawnPerTick)
			    			{
			    				if(i == right)
			    				{
			    					bottom++;
			    				}
			    				Pause(worldServer);
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
	    		if(top >= radius)
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
							
							PreGenerateChunk(currentX, currentZ, worldServer);
							
			    			if(spawnedThisTick >= maxSpawnPerTick)
			    			{
			    				if(i == right)
			    				{
			    					top++;
			    				}
			    				Pause(worldServer);
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

			UpdateProgressMessage(false);
			
			SavePreGeneratorData(worldServer);
        }
        preGeneratorIsRunning = false;
	}
	
	void Pause(WorldServer worldServer)
	{
		if(spawned == total) // Done spawning
		{
    		// Cycle completed, reset cycle progress
			iLeft = Integer.MIN_VALUE;
			iBottom = Integer.MIN_VALUE;
			iRight = Integer.MIN_VALUE;
			iTop = Integer.MIN_VALUE;
			
			UpdateProgressMessage(false);
			
			SavePreGeneratorData(worldServer);
		} else {		
			// Pre-generation cycle cannot be completed.
			// Save progress so we can continue and retry on the next server tick.
			cycle -= 1;
			processing = false;
		}
	}
	
	void PreGenerateChunk(int currentX, int currentZ, WorldServer worldServer)
	{
		UpdateProgressMessage(true);
		
		ChunkProviderServer chunkProvider = worldServer.getChunkProvider();
		
        if (
        	!(
	    		(
    				chunkProvider.chunkExists(currentX, currentZ) || 
					RegionFileCache.createOrLoadRegionFile(worldServer.getChunkSaveLocation(), currentX, currentZ).chunkExists(currentX & 0x1F, currentZ & 0x1F)
				) && 
				chunkProvider.provideChunk(currentX, currentZ).isPopulated()
			)
		)
		{
			spawnedThisTick++;
        	chunkProvider.provideChunk(currentX, currentZ).needsSaving(true);
        	chunkProvider.provideChunk(currentX, currentZ + 1).needsSaving(true);
        	chunkProvider.provideChunk(currentX + 1, currentZ).needsSaving(true);
        	chunkProvider.provideChunk(currentX + 1, currentZ + 1).needsSaving(true);
		}
	}
	
	static long lastMessage = System.currentTimeMillis();
	void UpdateProgressMessage (boolean loggingCanBeIgnored)	
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

		if(spawned < total)
		{
	        long i = Runtime.getRuntime().maxMemory();
	        long j = Runtime.getRuntime().totalMemory();
	        long k = Runtime.getRuntime().freeMemory();
	        long l = j - k;
	        String memoryUsage = " Mem: " + Long.valueOf(l * 100L / i) + "% " + Long.valueOf(BytesToMb(l)) + " / " +  Long.valueOf(BytesToMb(i)) + " MB ";
	        
	        progressScreenWorldSizeInBlocks = (radius * 2 + 1) * 16;
			preGeneratorProgressStatus = (int)spawned + "/" + (int)total; 
			preGeneratorProgress = (int)Math.round(((spawned / (double)(total)) * 100)) + "";
			progressScreenElapsedTime = sElapsedTime;
			progressScreenEstimatedTime = estimatedTime;
			if(!dontLog)
			{
				TerrainControl.log(LogMarker.INFO, "Pre-generating chunk X" + currentX + " Z" + currentZ + ". Radius: " + radius + " Spawned: " + (int)spawned + "/" + (int)total + " " + (int)Math.round(((spawned / (double)(total)) * 100)) + "% done. Elapsed: " + sElapsedTime + " ETA: " + estimatedTime + memoryUsage);
			}
		} else {
			preGeneratorProgressStatus = "Done";
			preGeneratorProgress = "";
			progressScreenElapsedTime = "";
			progressScreenEstimatedTime = "";
			progressScreenWorldSizeInBlocks = 0;
			if(!dontLog)
			{
				TerrainControl.log(LogMarker.INFO, "Pre-generating chunks done for world " + pregenerationWorld + ", " + ((int)spawned) + " chunks spawned in " + sElapsedTime);
			}
		}
	}
	
	public boolean menuOpen = true;
	public void ShowInGameUI()
	{
		if(menuOpen)
		{
	    	Minecraft mc = Minecraft.getMinecraft();
	    	mc.gameSettings.showDebugInfo = false;
	    	
	    	if(preGeneratorIsRunning && preGeneratorProgressStatus != "Done")
	    	{
		    	FontRenderer fontRenderer = mc.fontRendererObj;
		    	
		        GlStateManager.pushMatrix();
		        
		        List<String> list = new ArrayList<String>();       
		        
		        list.add("Generating \"" + pregenerationWorld + "\" " + (progressScreenWorldSizeInBlocks > 0 ? "(" + progressScreenWorldSizeInBlocks + "x" + progressScreenWorldSizeInBlocks  + " blocks)" : ""));        
				list.add("Progress: " + preGeneratorProgress + "%");
				list.add("Chunks: " + preGeneratorProgressStatus);
				list.add("Elapsed: " + progressScreenElapsedTime);
				list.add("Estimated: " + progressScreenEstimatedTime);
		        
		        long i = Runtime.getRuntime().maxMemory();
		        long j = Runtime.getRuntime().totalMemory();
		        long k = Runtime.getRuntime().freeMemory();
		        long l = j - k;
		        list.add("Memory: " + Long.valueOf(BytesToMb(l)) + "/" +  Long.valueOf(BytesToMb(i)) + " MB");
		
		        for (int zi = 0; zi < list.size(); ++zi)
		        {
		            String s = (String)list.get(zi);
		
		            if (!Strings.isNullOrEmpty(s))
		            {
		                int zj = fontRenderer.FONT_HEIGHT;
		                int zk = fontRenderer.getStringWidth(s);
		                int zi1 = 2 + zj * zi;
		                Gui.drawRect(1, zi1 - 1, 2 + zk + 1, zi1 + zj - 1, -1873784752); // TODO: Make semi-transparent
		                fontRenderer.drawString(s, 2, zi1, 14737632);
		            }
		        }
		        GlStateManager.popMatrix();
	    	}
		}
	}	

	public void ToggleIngameUI()
	{ 
		if(preGeneratorIsRunning && preGeneratorProgressStatus != "Done" || menuOpen)
		{
			if(menuOpen)
			{
				Minecraft.getMinecraft().gameSettings.showDebugInfo = false;
				menuOpen = false;
			}
			else if(!Minecraft.getMinecraft().gameSettings.showDebugInfo)
			{			
				menuOpen = true;
			}
		}
	}
	
    private long BytesToMb(long bytes)
    {
        return bytes / 1024L / 1024L;
    }
	
    public void shutDown(World world)
    {
    	if(preGeneratorIsRunning)
    	{
	    	SavePreGeneratorData(world);
	    	preGeneratorIsRunning = false;
    	}
    }
    
    // Saving / Loading
    // TODO: It's crude but it works, can improve later
    
	public void SavePreGeneratorData(World world)
	{			
		if(preGeneratorIsRunning)
		{
			File pregeneratedChunksFile = new File(world.getSaveHandler().getWorldDirectory() + "/OpenTerrainGenerator/PregeneratedChunks.txt");		
			if(pregeneratedChunksFile.exists())
			{
				pregeneratedChunksFile.delete();
			}		
			
			StringBuilder stringbuilder = new StringBuilder();
			stringbuilder.append(spawned + "," + left + "," + top + "," + right + "," + bottom + "," + cycle + "," + (System.currentTimeMillis() - startTime) + "," + iTop + "," + iBottom + "," + iLeft + "," + iRight);		
			
			BufferedWriter writer = null;
	        try
	        {
	        	pregeneratedChunksFile.getParentFile().mkdirs();
	        	writer = new BufferedWriter(new FileWriter(pregeneratedChunksFile));
	            writer.write(stringbuilder.toString());
	            TerrainControl.log(LogMarker.DEBUG, "Pre-generator data saved");
	        }
	        catch (IOException e)
	        {
	        	TerrainControl.log(LogMarker.ERROR, "Could not save pre-generator data.");
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

	void LoadPreGeneratorData(WorldServer worldServer)
	{				
		File pregeneratedChunksFile = new File(worldServer.getSaveHandler().getWorldDirectory() + "/OpenTerrainGenerator/PregeneratedChunks.txt");				
		String[] pregeneratedChunksFileValues = {};
		if(pregeneratedChunksFile.exists())
		{
			try {
				StringBuilder stringbuilder = new StringBuilder();
				BufferedReader reader = new BufferedReader(new FileReader(pregeneratedChunksFile));
				try {
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
				    TerrainControl.log(LogMarker.DEBUG, "Pre-generator data loaded");
				} finally {
					reader.close();
				}
				
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			catch (IOException e1) {
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
		}
	}
}
