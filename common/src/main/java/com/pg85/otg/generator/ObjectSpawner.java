package com.pg85.otg.generator;

import com.pg85.otg.LocalBiome;
import com.pg85.otg.LocalWorld;
import com.pg85.otg.OTG;
import com.pg85.otg.configuration.ConfigFunction;
import com.pg85.otg.configuration.ErroredFunction;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.customobjects.CustomObjectStructure;
import com.pg85.otg.customobjects.bo3.BO3;
import com.pg85.otg.customobjects.bo3.BO3Settings.SpawnHeightEnum;
import com.pg85.otg.generator.resource.AboveWaterGen;
import com.pg85.otg.generator.resource.BoulderGen;
import com.pg85.otg.generator.resource.CactusGen;
import com.pg85.otg.generator.resource.CustomObjectGen;
import com.pg85.otg.generator.resource.CustomStructureGen;
import com.pg85.otg.generator.resource.DungeonGen;
import com.pg85.otg.generator.resource.FossilGen;
import com.pg85.otg.generator.resource.GrassGen;
import com.pg85.otg.generator.resource.IceSpikeGen;
import com.pg85.otg.generator.resource.LiquidGen;
import com.pg85.otg.generator.resource.OreGen;
import com.pg85.otg.generator.resource.PlantGen;
import com.pg85.otg.generator.resource.ReedGen;
import com.pg85.otg.generator.resource.Resource;
import com.pg85.otg.generator.resource.SmallLakeGen;
import com.pg85.otg.generator.resource.SurfacePatchGen;
import com.pg85.otg.generator.resource.TreeGen;
import com.pg85.otg.generator.resource.UnderWaterOreGen;
import com.pg85.otg.generator.resource.UndergroundLakeGen;
import com.pg85.otg.generator.resource.VeinGen;
import com.pg85.otg.generator.resource.VinesGen;
import com.pg85.otg.generator.resource.WellGen;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.network.ConfigProvider;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.Rotation;

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
    	//OTG.log(LogMarker.INFO, "ObjectSpawner populate X" + chunkCoord.getChunkX() + " Z" + chunkCoord.getChunkZ());

		// Wait for another thread running SaveToDisk, then place a lock.
		while(true)
		{
			//OTG.log(LogMarker.INFO, "Populate waiting on SaveToDisk.");

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
				world.getStructureCache().PlotStructures(rand, world.getSpawnChunk(), true);
			}
		}
		StructurePlottedAtSpawn = true;

		if (!processing)
		{
			processing = true;

			if(world.getConfigs().getWorldConfig().IsOTGPlus)
			{
				world.getStructureCache().PlotStructures(rand, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ()), false);
				world.getStructureCache().PlotStructures(rand, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ() + 1), false);
				world.getStructureCache().PlotStructures(rand, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ() + 1), false);
				world.getStructureCache().PlotStructures(rand, chunkCoord, false);

		        ChunkCoordinate spawnChunk = this.world.getSpawnChunk();

		        boolean hasVillage = false;

		        if(spawnChunk.equals(chunkCoord) && this.world.getConfigs().getWorldConfig().BO3AtSpawn != null && this.world.getConfigs().getWorldConfig().BO3AtSpawn.trim().length() > 0)
		        {
		        	CustomObject customObject = OTG.getCustomObjectManager().getGlobalObjects().getObjectByName(this.world.getConfigs().getWorldConfig().BO3AtSpawn, this.world.getConfigs().getWorldConfig().getName());
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

		        			boolean populationBoundsCheck = world.getConfigs().getWorldConfig().populationBoundsCheck;
		        			world.getConfigs().getWorldConfig().populationBoundsCheck = false;
		        			world.setAllowSpawningOutsideBounds(true);
		        			((BO3)customObject).spawnForced(this.world, this.rand, Rotation.NORTH, spawnChunk.getBlockXCenter(), y, spawnChunk.getBlockZCenter());
		        			world.setAllowSpawningOutsideBounds(false);
		        			world.getConfigs().getWorldConfig().populationBoundsCheck = populationBoundsCheck;
		        		}
		        	}
		        } else {
			        // Generate structures
			        hasVillage = world.placeDefaultStructures(rand, chunkCoord);
		        }

				// Get the random generator
				WorldConfig worldConfig = configProvider.getWorldConfig();
				long resourcesSeed = worldConfig.resourcesSeed != 0L ? worldConfig.resourcesSeed : world.getSeed();
				this.rand.setSeed(resourcesSeed);
				long l1 = this.rand.nextLong() / 2L * 2L + 1L;
				long l2 = this.rand.nextLong() / 2L * 2L + 1L;
				this.rand.setSeed(chunkCoord.getChunkX() * l1 + chunkCoord.getChunkZ() * l2 ^ resourcesSeed);

				// Mark population started
				OTG.firePopulationStartEvent(world, rand, hasVillage, chunkCoord);

				processResourcesPhase2(chunkCoord);

				SpawnBO3s(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ()));
				SpawnBO3s(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ() + 1));
				SpawnBO3s(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ() + 1));
				SpawnBO3s(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ()));

				// Generate structures

				processResourcesPhase3(chunkCoord, hasVillage);

				// Mark population ended
				OTG.firePopulationEndEvent(world, rand, hasVillage, chunkCoord);

			} else {

		        // Get the corner block coords
		        int x = chunkCoord.getChunkX() * 16;
		        int z = chunkCoord.getChunkZ() * 16;

		        // Get the biome of the other corner
		        LocalBiome biome = world.getBiome(x + 15, z + 15);

		        // Null check
		        if (biome == null)
		        {
		            OTG.log(LogMarker.WARN, "Unknown biome at {},{}  (chunk {}). Could not populate chunk.", x + 15, z + 15, chunkCoord);
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
		        	CustomObject customObject = OTG.getCustomObjectManager().getGlobalObjects().getObjectByName(this.world.getConfigs().getWorldConfig().BO3AtSpawn, this.world.getConfigs().getWorldConfig().getName());
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

		        			boolean populationBoundsCheck = world.getConfigs().getWorldConfig().populationBoundsCheck;
		        			world.getConfigs().getWorldConfig().populationBoundsCheck = false;
		        			world.setAllowSpawningOutsideBounds(true);
		        			((BO3)customObject).spawnForced(this.world, this.rand, Rotation.NORTH, spawnChunk.getBlockXCenter(), y, spawnChunk.getBlockZCenter());
		        			world.setAllowSpawningOutsideBounds(false);
		        			world.getConfigs().getWorldConfig().populationBoundsCheck = populationBoundsCheck;
		        		}
		        	}
		        } else {
			        // Generate structures
			        hasVillage = world.placeDefaultStructures(rand, chunkCoord);
		        }

		        // Mark population started
		        world.startPopulation(chunkCoord);
		        OTG.firePopulationStartEvent(world, rand, hasVillage, chunkCoord);

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
		        OTG.firePopulationEndEvent(world, rand, hasVillage, chunkCoord);
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

				world.getStructureCache().PlotStructures(rand, chunkCoord, false);

				SpawnBO3s(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ()));

				// Get the random generator
				WorldConfig worldConfig = configProvider.getWorldConfig();
				long resourcesSeed = worldConfig.resourcesSeed != 0L ? worldConfig.resourcesSeed : world.getSeed();
				this.rand.setSeed(resourcesSeed);
				long l1 = this.rand.nextLong() / 2L * 2L + 1L;
				long l2 = this.rand.nextLong() / 2L * 2L + 1L;
				this.rand.setSeed(chunkCoord.getChunkX() * l1 + chunkCoord.getChunkZ() * l2 ^ resourcesSeed);

		        // Mark population started
		        world.startPopulation(chunkCoord);
		        OTG.firePopulationStartEvent(world, rand, false, chunkCoord);

				// Get the corner block coords
				int x = chunkCoord.getChunkX() * 16;
				int z = chunkCoord.getChunkZ() * 16;

				// Get the biome of the other corner TODO: explain why?
				LocalBiome biome = world.getBiome(x + 8, z + 8);

				// Default structures can cause a bigger cascade but we have to spawn them to prevent holes in villages etc
				boolean hasVillage = world.placeDefaultStructures(rand, chunkCoord);

				// TODO: Reimplement this
				//if(!worldConfig.improvedMobSpawning)
				{
					world.placePopulationMobs(biome, rand, chunkCoord);
				}

				// Snow and ice
				//freezeChunk(chunkCoord);

				// Replace blocks
				//world.replaceBlocks(chunkCoord); // <-- causes nullreference exception when getChunk returns null

		        // Mark population ended
		        OTG.firePopulationEndEvent(world, rand, false, chunkCoord);
		        world.endPopulation();

				OTG.log(LogMarker.INFO,"Error, minecraft engine attempted to populate two chunks at once. Chunk X" + chunkCoord.getChunkX() + " Z" + chunkCoord.getChunkZ() + ". This is probably caused by a mod spawning blocks in unloaded chunks and can cause lag as well as missing trees, ores and other TC/OTG resources. Please try to find out which mod causes this, disable the feature causing it and alert the mod creator. Set the log level to TRACE in mods/OpenTerrainGenerator/OTG.ini file for a stack trace.");
				OTG.log(LogMarker.TRACE, Arrays.toString(Thread.currentThread().getStackTrace()));
			} else {
				OTG.log(LogMarker.INFO,"Error, minecraft engine attempted to populate two chunks at once. Chunk X" + chunkCoord.getChunkX() + " Z" + chunkCoord.getChunkZ() + ". This is probably caused by a mod spawning blocks in unloaded chunks. Set the log level to Trace in mods/OpenTerrainGenerator/OTG.ini file for a stack trace. Update: Using OTG multi-dimension features may cause this log message occasionally, still need to investigate.");
				OTG.log(LogMarker.TRACE, Arrays.toString(Thread.currentThread().getStackTrace()));

				// Get the random generator
				WorldConfig worldConfig = configProvider.getWorldConfig();
				long resourcesSeed = worldConfig.resourcesSeed != 0L ? worldConfig.resourcesSeed : world.getSeed();
				this.rand.setSeed(resourcesSeed);
				long l1 = this.rand.nextLong() / 2L * 2L + 1L;
				long l2 = this.rand.nextLong() / 2L * 2L + 1L;
				this.rand.setSeed(chunkCoord.getChunkX() * l1 + chunkCoord.getChunkZ() * l2 ^ resourcesSeed);

		        // Mark population started
		        world.startPopulation(chunkCoord);
		        OTG.firePopulationStartEvent(world, rand, false, chunkCoord);

				// Default structures can cause a bigger cascade but we have to spawn them to prevent holes in villages etc
				boolean hasVillage = world.placeDefaultStructures(rand, chunkCoord);

		        // Mark population ended
		        OTG.firePopulationEndEvent(world, rand, false, chunkCoord);
		        world.endPopulation();
			}
		}

		// Release the lock
		synchronized(lockingObject)
		{
			populating = false;
		}

		//OTG.log(LogMarker.INFO, "ObjectSpawner DONE populating X" + chunkCoord.getChunkX() + " Z" + chunkCoord.getChunkZ());
    }

	public void processResourcesPhase2(ChunkCoordinate chunkCoord)
	{
		// Get the biome of the other corner TODO: explain why?
		LocalBiome biome = world.getBiome(chunkCoord.getBlockX() + 8, chunkCoord.getBlockZ() + 8);

		if (biome == null)
		{
			OTG.log(LogMarker.DEBUG, "Unknown biome at {},{}  (chunk {}). Population failed.", chunkCoord.getBlockX() + 8, chunkCoord.getBlockZ() + 8, chunkCoord);
			throw new RuntimeException();
		}

		BiomeConfig biomeConfig = biome.getBiomeConfig();

		ArrayList<Resource> miscResources = new ArrayList<Resource>();
		for (ConfigFunction<BiomeConfig> res : biomeConfig.resourceSequence)
		{
			if (!(res instanceof CustomObjectGen) && !(res instanceof CustomStructureGen))
			{
				if(!(res instanceof ErroredFunction))
				{
					miscResources.add((Resource) res);
				} else {
					if(OTG.getPluginConfig().SpawnLog)
					{
						OTG.log(LogMarker.INFO, "Could not parse resource \"" + res.toString() + "\" for biome " + biome.getName());
					}
				}
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
			OTG.log(LogMarker.DEBUG, "Unknown biome at {},{}  (chunk {}). Population failed.", chunkCoord.getBlockX() + 8, chunkCoord.getBlockZ() + 8, chunkCoord);
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
			if(!(res instanceof ErroredFunction))
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
			} else {
				if(OTG.getPluginConfig().SpawnLog)
				{
					OTG.log(LogMarker.INFO, "Could not parse resource \"" + res.toString() + "\" for biome " + biome.getName());
				}
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
			OTG.log(LogMarker.DEBUG, "Unknown biome at {},{}  (chunk {}). Population failed.", x + 15, z + 15, chunkCoord);
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