package com.khorn.terraincontrol.generator;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.configuration.ConfigProvider;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.CustomObjectStructure;
import com.khorn.terraincontrol.customobjects.bo3.BO3;
import com.khorn.terraincontrol.customobjects.bo3.BO3Settings.SpawnHeightEnum;
import com.khorn.terraincontrol.generator.resource.AboveWaterGen;
import com.khorn.terraincontrol.generator.resource.BoulderGen;
import com.khorn.terraincontrol.generator.resource.CactusGen;
import com.khorn.terraincontrol.generator.resource.CustomObjectGen;
import com.khorn.terraincontrol.generator.resource.CustomStructureGen;
import com.khorn.terraincontrol.generator.resource.DungeonGen;
import com.khorn.terraincontrol.generator.resource.FossilGen;
import com.khorn.terraincontrol.generator.resource.GrassGen;
import com.khorn.terraincontrol.generator.resource.IceSpikeGen;
import com.khorn.terraincontrol.generator.resource.LiquidGen;
import com.khorn.terraincontrol.generator.resource.OreGen;
import com.khorn.terraincontrol.generator.resource.PlantGen;
import com.khorn.terraincontrol.generator.resource.ReedGen;
import com.khorn.terraincontrol.generator.resource.Resource;
import com.khorn.terraincontrol.generator.resource.SmallLakeGen;
import com.khorn.terraincontrol.generator.resource.SurfacePatchGen;
import com.khorn.terraincontrol.generator.resource.TreeGen;
import com.khorn.terraincontrol.generator.resource.UnderWaterOreGen;
import com.khorn.terraincontrol.generator.resource.UndergroundLakeGen;
import com.khorn.terraincontrol.generator.resource.VeinGen;
import com.khorn.terraincontrol.generator.resource.VinesGen;
import com.khorn.terraincontrol.generator.resource.WellGen;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.Rotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class ObjectSpawner
{
	// OTG+
	
	public boolean populating;
    public boolean saving;
	public boolean saveRequired;
	
	public int populatingX = 0;
	public int populatingZ = 0;
	
	public Object lockingObject = new Object();
	
	//
	
    private final ConfigProvider configProvider;
    private final Random rand;
    private final LocalWorld world;

    public ObjectSpawner(ConfigProvider configProvider, LocalWorld localWorld)
    {
        this.configProvider = configProvider;
        this.rand = new Random();
        this.world = localWorld;
    }  
    
    public boolean StructurePlottedAtSpawn = false;
        
    public boolean processing = false;   
    public void populate(ChunkCoordinate chunkCoord)
    {       	
		//TerrainControl.log(LogMarker.INFO, "ObjectSpawner populate X" + chunkCoord.getChunkX() + " Z" + chunkCoord.getChunkZ());
		
		// Wait for another thread running SaveToDisk, then place a lock.
		while(true)
		{
			//TerrainControl.log(LogMarker.INFO, "Populate waiting on SaveToDisk.");
			
			synchronized(lockingObject)
			{
				if(!saving)
				{
					populating = true;
					break;
				}
			}
		}	
		synchronized(lockingObject)
		{
			saveRequired = true;
		}    	

		if(world.getConfigs().getWorldConfig().IsOTGPlus)
		{
			if(!StructurePlottedAtSpawn)
			{
				world.getStructureCache().PlotStructures(world.getSpawnChunk(), true);
			}
		}
		StructurePlottedAtSpawn = true;
		
		if (!processing)
		{
			processing = true;
		
			if(world.getConfigs().getWorldConfig().IsOTGPlus)
			{						
				world.getStructureCache().PlotStructures(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ()), false);			
				world.getStructureCache().PlotStructures(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ() + 1), false);			
				world.getStructureCache().PlotStructures(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ() + 1), false);
				world.getStructureCache().PlotStructures(chunkCoord, false);
				
		        ChunkCoordinate spawnChunk = this.world.getSpawnChunk();
		        
		        boolean hasVillage = false;
		        
		        if(spawnChunk.equals(chunkCoord) && this.world.getConfigs().getWorldConfig().BO3AtSpawn != null && this.world.getConfigs().getWorldConfig().BO3AtSpawn.trim().length() > 0)
		        {
		        	CustomObject customObject = TerrainControl.getCustomObjectManager().getGlobalObjects().getObjectByName(this.world.getConfigs().getWorldConfig().BO3AtSpawn, this.world.getConfigs().getWorldConfig().getName());
		        	if(customObject != null)
		        	{
		        		if(customObject instanceof BO3)
		        		{
		        			int y = 1;
		        			
		        			if(((BO3)customObject).getSettings().spawnHeight == SpawnHeightEnum.highestBlock)
		        			{
		        				 y = this.world.getHighestBlockYAt(spawnChunk.getBlockXCenter(), spawnChunk.getBlockZCenter()) - 1;
		        			}
		        			else if(((BO3)customObject).getSettings().spawnHeight == SpawnHeightEnum.highestSolidBlock)
		        			{
		        				y = this.world.getSolidHeight(spawnChunk.getBlockXCenter(), spawnChunk.getBlockZCenter()) - 1;
		        			}
		        			else if(((BO3)customObject).getSettings().spawnHeight == SpawnHeightEnum.randomY)
		        			{
		        				y = (int) (((BO3)customObject).getSettings().minHeight + (Math.random() * (((BO3)customObject).getSettings().maxHeight - ((BO3)customObject).getSettings().minHeight)));
		        			}
	
		        			y += ((BO3)customObject).getSettings().spawnHeightOffset;
		        			
		        			((BO3)customObject).spawnForced(this.world, this.rand, Rotation.NORTH, spawnChunk.getBlockXCenter(), y, spawnChunk.getBlockZCenter());	
		        		}	        			        	
		        	}
		        } else {	       
			        // Generate structures
			        hasVillage = world.placeDefaultStructures(rand, chunkCoord);	
		        }				
				
				// Mark population started
				TerrainControl.firePopulationStartEvent(world, rand, hasVillage, chunkCoord);
				
				processResourcesPhase2(chunkCoord);
							
				SpawnBO3s(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ()));
				SpawnBO3s(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ() + 1));
				SpawnBO3s(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ() + 1));
				SpawnBO3s(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ()));
									
				// Get the random generator
				WorldConfig worldConfig = configProvider.getWorldConfig();
				long resourcesSeed = worldConfig.resourcesSeed != 0L ? worldConfig.resourcesSeed : world.getSeed();
				this.rand.setSeed(resourcesSeed);
				long l1 = this.rand.nextLong() / 2L * 2L + 1L;
				long l2 = this.rand.nextLong() / 2L * 2L + 1L;
				this.rand.setSeed(chunkCoord.getChunkX() * l1 + chunkCoord.getChunkZ() * l2 ^ resourcesSeed);

				// Generate structures

				processResourcesPhase3(chunkCoord, hasVillage);
								
				// Mark population ended
				TerrainControl.firePopulationEndEvent(world, rand, hasVillage, chunkCoord);
								
			} else {
				
		        // Get the corner block coords
		        int x = chunkCoord.getChunkX() * 16;
		        int z = chunkCoord.getChunkZ() * 16;
				
		        // Get the biome of the other corner
		        LocalBiome biome = world.getBiome(x + 15, z + 15);
		
		        // Null check
		        if (biome == null)
		        {
		            TerrainControl.log(LogMarker.WARN, "Unknown biome at {},{}  (chunk {}). Could not populate chunk.", x + 15, z + 15, chunkCoord);
		            return;
		        }
		        
		        BiomeConfig biomeConfig = biome.getBiomeConfig();
		        
		        // Get the random generator
		        WorldConfig worldConfig = configProvider.getWorldConfig();
		        long resourcesSeed = worldConfig.resourcesSeed != 0L ? worldConfig.resourcesSeed : world.getSeed();
		        this.rand.setSeed(resourcesSeed);
		        long l1 = this.rand.nextLong() / 2L * 2L + 1L;
		        long l2 = this.rand.nextLong() / 2L * 2L + 1L;
		        this.rand.setSeed(chunkCoord.getChunkX() * l1 + chunkCoord.getChunkZ() * l2 ^ resourcesSeed);	
		        
		        ChunkCoordinate spawnChunk = this.world.getSpawnChunk();
		        
		        boolean hasVillage = false;
		        
		        if(spawnChunk.equals(chunkCoord) && this.world.getConfigs().getWorldConfig().BO3AtSpawn != null && this.world.getConfigs().getWorldConfig().BO3AtSpawn.trim().length() > 0)
		        {
		        	CustomObject customObject = TerrainControl.getCustomObjectManager().getGlobalObjects().getObjectByName(this.world.getConfigs().getWorldConfig().BO3AtSpawn, this.world.getConfigs().getWorldConfig().getName());
		        	if(customObject != null)
		        	{
		        		if(customObject instanceof BO3)
		        		{
		        			int y = 1;
		        			
		        			if(((BO3)customObject).getSettings().spawnHeight == SpawnHeightEnum.highestBlock)
		        			{
		        				 y = this.world.getHighestBlockYAt(spawnChunk.getBlockXCenter(), spawnChunk.getBlockZCenter()) - 1;
		        			}
		        			else if(((BO3)customObject).getSettings().spawnHeight == SpawnHeightEnum.highestSolidBlock)
		        			{
		        				y = this.world.getSolidHeight(spawnChunk.getBlockXCenter(), spawnChunk.getBlockZCenter()) - 1;
		        			}
		        			else if(((BO3)customObject).getSettings().spawnHeight == SpawnHeightEnum.randomY)
		        			{
		        				y = (int) (((BO3)customObject).getSettings().minHeight + (Math.random() * (((BO3)customObject).getSettings().maxHeight - ((BO3)customObject).getSettings().minHeight)));
		        			}
	
		        			y += ((BO3)customObject).getSettings().spawnHeightOffset;
		        			
		        			((BO3)customObject).spawnForced(this.world, this.rand, Rotation.NORTH, spawnChunk.getBlockXCenter(), y, spawnChunk.getBlockZCenter());	
		        		}	        			        	
		        	}
		        } else {	       
			        // Generate structures
			        hasVillage = world.placeDefaultStructures(rand, chunkCoord);	
		        }
		        		        
		        // Mark population started
		        world.startPopulation(chunkCoord);
		        TerrainControl.firePopulationStartEvent(world, rand, hasVillage, chunkCoord);
		        		        
		        // Resource sequence
		        for (ConfigFunction<BiomeConfig> res : biomeConfig.resourceSequence)
		        {
		            if (res instanceof Resource)
		            {
		                ((Resource)res).process(world, rand, hasVillage, chunkCoord);
		            }
		        }
		        		        
		        // Animals
		        world.placePopulationMobs(biome, rand, chunkCoord);       
		        
		        // Snow and ice
		        new FrozenSurfaceHelper(world).freezeChunk(chunkCoord);
		
		        // Replace blocks
		        world.replaceBlocks(chunkCoord);
		        
		        // Mark population ended
		        TerrainControl.firePopulationEndEvent(world, rand, hasVillage, chunkCoord);
		        world.endPopulation();		        
			}
	        
			processing = false;
		} else {
			if(world.getConfigs().getWorldConfig().IsOTGPlus)
			{
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

				world.getStructureCache().PlotStructures(chunkCoord, false);
				
				SpawnBO3s(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ()));

				// Get the random generator
				WorldConfig worldConfig = configProvider.getWorldConfig();
				long resourcesSeed = worldConfig.resourcesSeed != 0L ? worldConfig.resourcesSeed : world.getSeed();
				this.rand.setSeed(resourcesSeed);
				long l1 = this.rand.nextLong() / 2L * 2L + 1L;
				long l2 = this.rand.nextLong() / 2L * 2L + 1L;
				this.rand.setSeed(chunkCoord.getChunkX() * l1 + chunkCoord.getChunkZ() * l2 ^ resourcesSeed);

				// Get the corner block coords
				int x = chunkCoord.getChunkX() * 16;
				int z = chunkCoord.getChunkZ() * 16;

				// Get the biome of the other corner TODO: explain why?
				LocalBiome biome = world.getBiome(x + 8, z + 8);
			
				// TODO: Reimplement this				
				//if(!worldConfig.improvedMobSpawning)
				{
					world.placePopulationMobs(biome, rand, chunkCoord);
				}
				
				// Snow and ice
				//freezeChunk(chunkCoord);
				
				// Replace blocks
				//world.replaceBlocks(chunkCoord); // <-- causes nullreference exception when getChunk returns null
				
				TerrainControl.log(LogMarker.INFO,"Error, minecraft engine attempted to populate two chunks at once! Chunk X" + chunkCoord.getChunkX() + " Z" + chunkCoord.getChunkZ() + ". This is probably caused by a mod spawning blocks in unloaded chunks and can cause lag as well as missing trees, ores and other TC/OTG resources. Please try to find out which mod causes this, disable the feature causing it and alert the mod creator. Set the log level to TRACE in mods/TerrainControl/TerranControl.ini file for a stack trace.");
				TerrainControl.log(LogMarker.TRACE, Arrays.toString(Thread.currentThread().getStackTrace()));
				//throw new NotImplementedException(); // Enable this when debugging				
			} else {
				TerrainControl.log(LogMarker.TRACE,"Error, minecraft engine attempted to populate two chunks at once! Chunk X" + chunkCoord.getChunkX() + " Z" + chunkCoord.getChunkZ() + ". This is probably caused by a mod spawning blocks in unloaded chunks and can cause lag as well as missing trees, ores and other OTG resources. Please try to find out which mod causes this, disable the feature causing it and alert the mod creator. Set the log level to Trace in mods/OpenTerrainGenerator/TerranControl.ini file for a stack trace. (Update: The recently added multi-dimension features may be causing this log message occasionally, will fix a.s.a.p).");
				TerrainControl.log(LogMarker.TRACE, Arrays.toString(Thread.currentThread().getStackTrace()));
			}
		}

		// Release the lock
		synchronized(lockingObject)
		{
			populating = false;
		}
		
		//TerrainControl.log(LogMarker.INFO, "ObjectSpawner DONE populating X" + chunkCoord.getChunkX() + " Z" + chunkCoord.getChunkZ());
    }      
    
	public void processResourcesPhase2(ChunkCoordinate chunkCoord)
	{	        
		// Get the biome of the other corner TODO: explain why?
		LocalBiome biome = world.getBiome(chunkCoord.getBlockX() + 8, chunkCoord.getBlockZ() + 8);

		if (biome == null)
		{
			TerrainControl.log(LogMarker.DEBUG, "Unknown biome at {},{}  (chunk {}). Population failed.", chunkCoord.getBlockX() + 8, chunkCoord.getBlockZ() + 8, chunkCoord);
			throw new RuntimeException();
		}

		BiomeConfig biomeConfig = biome.getBiomeConfig();
        
		ArrayList<Resource> miscResources = new ArrayList<Resource>();
		for (ConfigFunction<BiomeConfig> res : biomeConfig.resourceSequence)
		{
			if (!(res instanceof CustomObjectGen) && !(res instanceof CustomStructureGen))
			{
				miscResources.add((Resource) res);
			}
		}
		
		for (Resource res : miscResources)
		{
			if (
				(res instanceof OreGen) || 
				(res instanceof SmallLakeGen) || 
				(res instanceof UndergroundLakeGen) || // TODO: look at potential size bug in UnderGroundLakeGen
				(res instanceof UnderWaterOreGen) || // TODO: This seems to be bugged, generate a plains only world with default settings and no sand appears where it does in TC
				(res instanceof VeinGen) || // TODO: Test this
				(res instanceof SurfacePatchGen)
			)
			{
				res.process(world, rand, false, chunkCoord);
			}
		}
	}

	public void processResourcesPhase3(ChunkCoordinate chunkCoord, boolean hasGeneratedAVillage)
	{			
		// Get the random generator
		WorldConfig worldConfig = configProvider.getWorldConfig();
		long resourcesSeed = worldConfig.resourcesSeed != 0L ? worldConfig.resourcesSeed : world.getSeed();
		this.rand.setSeed(resourcesSeed);
		long l1 = this.rand.nextLong() / 2L * 2L + 1L;
		long l2 = this.rand.nextLong() / 2L * 2L + 1L;
		this.rand.setSeed(chunkCoord.getChunkX() * l1 + chunkCoord.getChunkZ() * l2 ^ resourcesSeed);
	
		// Get the corner block coords
		int x = chunkCoord.getChunkX() * 16;
		int z = chunkCoord.getChunkZ() * 16;

		// Get the biome of the other corner TODO: explain why?
		LocalBiome biome = world.getBiome(x + 8, z + 8);

		if (biome == null)
		{
			TerrainControl.log(LogMarker.DEBUG, "Unknown biome at {},{}  (chunk {}). Population failed.", chunkCoord.getBlockX() + 8, chunkCoord.getBlockZ() + 8, chunkCoord);
			throw new RuntimeException();
		}

		BiomeConfig biomeConfig = biome.getBiomeConfig();

		// Resource sequence
		// Processes all resources including bo2 and bo3's but also trees and such

		// Bo2's
		ArrayList<Resource> customObjects = new ArrayList<Resource>();
		// Every other type of resource
		ArrayList<Resource> miscResources = new ArrayList<Resource>();
		for (ConfigFunction<BiomeConfig> res : biomeConfig.resourceSequence)
		{		
			if (res instanceof CustomObjectGen)
			{
				// Small (<32x32) custom objects like trees and rocks.
				customObjects.add((Resource) res);
			}
			else if (!(res instanceof CustomStructureGen))
			{
				miscResources.add((Resource) res);
			}
		}
	
		for (Resource res : customObjects)
		{
			res.process(world, rand, hasGeneratedAVillage, chunkCoord);
		}
		for (Resource res : miscResources)
		{
			// TODO: Find out if these are always in the same order, trees
			// should spawn first?
			if (
				(res instanceof DungeonGen) ||				
				(res instanceof AboveWaterGen) || 
				(res instanceof PlantGen) || 
				(res instanceof GrassGen) || 
				(res instanceof TreeGen) || 
				(res instanceof ReedGen) || 
				(res instanceof LiquidGen) || 
				(res instanceof BoulderGen) || 
				(res instanceof CactusGen) || 
				(res instanceof IceSpikeGen) ||
				(res instanceof WellGen) || 
				(res instanceof VinesGen) ||
				(res instanceof FossilGen)
			)
			{				
				res.process(world, rand, hasGeneratedAVillage, chunkCoord);
			}
		}
				
		// don't use world.placePopulationMobs, it bypasses EntityLiving.getCanSpawnHere() :(		
		//if(!worldConfig.improvedMobSpawning)
		{
			world.placePopulationMobs(biome, rand, chunkCoord);
		}

		// Snow and ice
        new FrozenSurfaceHelper(world).freezeChunk(chunkCoord);

		// Replace blocks
		world.replaceBlocks(chunkCoord);
	}
    
	public void SpawnBO3s(ChunkCoordinate chunkCoord)
	{			
		// Get the corner block coords
		int x = chunkCoord.getChunkX() * 16;
		int z = chunkCoord.getChunkZ() * 16;

		// Get the biome of the other corner
		LocalBiome biome = world.getBiome(x + 15, z + 15);

		// Null check
		if (biome == null)
		{
			TerrainControl.log(LogMarker.DEBUG, "Unknown biome at {},{}  (chunk {}). Population failed.", x + 15, z + 15, chunkCoord);
			return;
		}
		
		CustomObjectStructure structureStart = world.getStructureCache().structureCache.get(chunkCoord);
		if (structureStart != null && structureStart.Start != null)
		{
			// SpawnForChunk will call placeComplexSurfaceBlocks for this
			// chunk (after spawning smooth area but before spawning structure)
			structureStart.SpawnForChunk(chunkCoord);

			// All done spawning structures for this chunk, clean up cache
			if(!world.IsInsidePregeneratedRegion(chunkCoord, true))
			{
				world.getStructureCache().structureCache.put(chunkCoord, null);	
			} else {
				world.getStructureCache().structureCache.remove(chunkCoord);
			}			
		}
		// Only trees plotted here
		else if (structureStart != null)
		{				
			// Complex surface blocks
			//placeComplexSurfaceBlocks(chunkCoord);
			
			if(!world.IsInsidePregeneratedRegion(chunkCoord, true))
			{
				world.getStructureCache().structureCache.put(chunkCoord, null);	
			} else {
				world.getStructureCache().structureCache.remove(chunkCoord);
			}
		}
	}	
}