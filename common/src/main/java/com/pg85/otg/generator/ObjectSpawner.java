package com.pg85.otg.generator;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.ConfigFunction;
import com.pg85.otg.configuration.ErroredFunction;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.customobjects.bo3.BO3;
import com.pg85.otg.customobjects.bo3.BO3Settings.SpawnHeightEnum;
import com.pg85.otg.customobjects.structures.bo4.BO4CustomStructure;
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
import com.pg85.otg.generator.surface.FrozenSurfaceHelper;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.network.ConfigProvider;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class ObjectSpawner
{
	public boolean populating;
    public boolean processing = false;
	public boolean saving;
	public boolean saveRequired;
    public boolean StructurePlottedAtSpawn = false;
	public int populatingX = 0;
	public int populatingZ = 0;
    private final ConfigProvider configProvider;
    private final Random rand;
    private final LocalWorld world;
	public Object lockingObject = new Object();
    
    public ObjectSpawner(ConfigProvider configProvider, LocalWorld localWorld)
    {
        this.configProvider = configProvider;
        this.rand = new Random();
        this.world = localWorld;
    }

    //int currentlyPopulatingIndex = 0;
    public void populate(ChunkCoordinate chunkCoord)
    {
    	//int myPopulatingIndex;
    	//OTG.log(LogMarker.INFO, "ObjectSpawner populate X" + chunkCoord.getChunkX() + " Z" + chunkCoord.getChunkZ() + " " + world.getName());

    	boolean unlockWhenDone = false;
		// Wait for another thread running SaveToDisk, then place a lock.
		while(true)
		{
			//OTG.log(LogMarker.INFO, "Populate waiting on SaveToDisk.");

			synchronized(lockingObject)
			{
				if(!saving)
				{
					// If populating then this method is being called recursively (indicating cascading chunk-gen).
					// This method can be called recursively, but should never be called by two threads at once.
					// TODO: Make sure that's the case.
					if(!populating)
					{
						populating = true;
						unlockWhenDone = true;
					}
					break;
				}
			}
		}
		synchronized(lockingObject)
		{
			saveRequired = true;
		}

		if(world.isOTGPlus())
		{
			if(!StructurePlottedAtSpawn)
			{
				world.getStructureCache().plotStructures(rand, world.getSpawnChunk(), true);
			}
		}
		StructurePlottedAtSpawn = true;

		if (!processing)
		{
			processing = true;

			doPopulate(chunkCoord);
			
			processing = false;
		} else {
			
			doPopulate(chunkCoord);
			
			OTG.log(LogMarker.INFO, "Cascading chunk generation detected.");
			if(OTG.getPluginConfig().developerMode)
			{			
				OTG.log(LogMarker.INFO, Arrays.toString(Thread.currentThread().getStackTrace()));
			}
		}

		// Release the lock
		synchronized(lockingObject)
		{
			// This assumes that this method can only be called alone or recursively, never by 2 threads at once.
			// TODO: Make sure that's the case.
			if(unlockWhenDone)
			{
				populating = false;
			}
		}

		if(chunkCoord.equals(this.world.getSpawnChunk()))
		{
			this.world.updateSpawnPointY(chunkCoord);
		}
		
		//OTG.log(LogMarker.INFO, "ObjectSpawner DONE populating X" + chunkCoord.getChunkX() + " Z" + chunkCoord.getChunkZ());
    }
    
    private void doPopulate(ChunkCoordinate chunkCoord)
    {
		if(world.isOTGPlus())
		{
			world.getStructureCache().plotStructures(rand, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ()), false);
			world.getStructureCache().plotStructures(rand, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ() + 1), false);
			world.getStructureCache().plotStructures(rand, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ() + 1), false);
			world.getStructureCache().plotStructures(rand, chunkCoord, false);

	        ChunkCoordinate spawnChunk = this.world.getSpawnChunk();

	        boolean hasVillage = false;

	        if(spawnChunk.equals(chunkCoord) && this.world.getConfigs().getWorldConfig().bo3AtSpawn != null && this.world.getConfigs().getWorldConfig().bo3AtSpawn.trim().length() > 0)
	        {
	        	CustomObject customObject = OTG.getCustomObjectManager().getGlobalObjects().getObjectByName(this.world.getConfigs().getWorldConfig().bo3AtSpawn, this.world.getConfigs().getWorldConfig().getName());
	        	if(customObject != null)
	        	{
	        		if(customObject instanceof BO3)
	        		{
	        			int y = 1;

	        			if(((BO3)customObject).getSettings().spawnHeight == SpawnHeightEnum.highestBlock)
	        			{
	        				 y = this.world.getHighestBlockAboveYAt(spawnChunk.getBlockXCenter(), spawnChunk.getBlockZCenter(), chunkCoord) - 1;
	        			}
	        			else if(((BO3)customObject).getSettings().spawnHeight == SpawnHeightEnum.highestSolidBlock)
	        			{
	        				y = this.world.getBlockAboveSolidHeight(spawnChunk.getBlockXCenter(), spawnChunk.getBlockZCenter(), chunkCoord) - 1;
	        			}
	        			else if(((BO3)customObject).getSettings().spawnHeight == SpawnHeightEnum.randomY)
	        			{
	        				y = (int) (((BO3)customObject).getSettings().minHeight + (Math.random() * (((BO3)customObject).getSettings().maxHeight - ((BO3)customObject).getSettings().minHeight)));
	        			}

	        			y += ((BO3)customObject).getSettings().spawnHeightOffset;
	        			// TODO: This may spawn the structure across chunk borders if its larger than 16 in any direction from its center.
	        			((BO3)customObject).spawnForced(this.world, this.rand, Rotation.NORTH, spawnChunk.getBlockX() + 15, y, spawnChunk.getBlockZ() + 15);
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

			spawnBO3s(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ()), chunkCoord);
			spawnBO3s(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ() + 1), chunkCoord);
			spawnBO3s(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ() + 1), chunkCoord);
			spawnBO3s(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ()), chunkCoord);

			// Generate structures

			processResourcesPhase3(chunkCoord, hasVillage);

			// Mark population ended
			OTG.firePopulationEndEvent(world, rand, hasVillage, chunkCoord);

		} else {

	        // Get the corner block coords
	        int x = chunkCoord.getChunkX() * 16;
	        int z = chunkCoord.getChunkZ() * 16;

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

	        if(spawnChunk.equals(chunkCoord) && this.world.getConfigs().getWorldConfig().bo3AtSpawn != null && this.world.getConfigs().getWorldConfig().bo3AtSpawn.trim().length() > 0)
	        {
	        	CustomObject customObject = OTG.getCustomObjectManager().getGlobalObjects().getObjectByName(this.world.getConfigs().getWorldConfig().bo3AtSpawn, this.world.getConfigs().getWorldConfig().getName());
	        	if(customObject != null)
	        	{
	        		if(customObject instanceof BO3)
	        		{
	        			int y = 1;

	        			if(((BO3)customObject).getSettings().spawnHeight == SpawnHeightEnum.highestBlock)
	        			{
	        				 y = this.world.getHighestBlockAboveYAt(spawnChunk.getBlockXCenter(), spawnChunk.getBlockZCenter(), chunkCoord) - 1;
	        			}
	        			else if(((BO3)customObject).getSettings().spawnHeight == SpawnHeightEnum.highestSolidBlock)
	        			{
	        				y = this.world.getBlockAboveSolidHeight(spawnChunk.getBlockXCenter(), spawnChunk.getBlockZCenter(), chunkCoord) - 1;
	        			}
	        			else if(((BO3)customObject).getSettings().spawnHeight == SpawnHeightEnum.randomY)
	        			{
	        				y = (int) (((BO3)customObject).getSettings().minHeight + (Math.random() * (((BO3)customObject).getSettings().maxHeight - ((BO3)customObject).getSettings().minHeight)));
	        			}

	        			y += ((BO3)customObject).getSettings().spawnHeightOffset;
	        			// TODO: This may spawn the structure across chunk borders if its larger than 16 in any direction from its center.
	        			((BO3)customObject).spawnForced(this.world, this.rand, Rotation.NORTH, spawnChunk.getBlockX() + 15, y, spawnChunk.getBlockZ() + 15);
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
			// TODO: Fire PopulateChunkEvent.Populate.EventType.ICE for Forge
	        new FrozenSurfaceHelper(world).freezeChunk(chunkCoord);

	        // Replace blocks
	        world.replaceBlocks(chunkCoord);

	        // Mark population ended
	        OTG.firePopulationEndEvent(world, rand, hasVillage, chunkCoord);
	        world.endPopulation();
		}
    }

	private void processResourcesPhase2(ChunkCoordinate chunkCoord)
	{
		LocalBiome biome = world.getBiome(chunkCoord.getBlockX() + 8, chunkCoord.getBlockZ() + 8);

		if (biome == null)
		{
			OTG.log(LogMarker.FATAL, "Unknown biome at {},{}  (chunk {}). Population failed.", chunkCoord.getBlockX() + 8, chunkCoord.getBlockZ() + 8, chunkCoord);
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
					if(OTG.getPluginConfig().spawnLog)
					{
						OTG.log(LogMarker.WARN, "Could not parse resource \"" + res.toString() + "\" for biome " + biome.getName());
					}
				}
			}
		}

		for (Resource res : miscResources)
		{
			if (
				(res instanceof OreGen) ||
				(res instanceof SmallLakeGen && !this.world.getStructureCache().isChunkOccupied(chunkCoord)) ||
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

	private void processResourcesPhase3(ChunkCoordinate chunkCoord, boolean hasGeneratedAVillage)
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

		LocalBiome biome = world.getBiome(x + 8, z + 8);

		if (biome == null)
		{
			OTG.log(LogMarker.FATAL, "Unknown biome at {},{}  (chunk {}). Population failed.", chunkCoord.getBlockX() + 8, chunkCoord.getBlockZ() + 8, chunkCoord);
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
				if(OTG.getPluginConfig().spawnLog)
				{
					OTG.log(LogMarker.WARN, "Could not parse resource \"" + res.toString() + "\" for biome " + biome.getName());
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
		// TODO: Fire PopulateChunkEvent.Populate.EventType.ICE for Forge
		new FrozenSurfaceHelper(world).freezeChunk(chunkCoord);

		// Replace blocks
		world.replaceBlocks(chunkCoord);
	}

	private void spawnBO3s(ChunkCoordinate chunkCoord, ChunkCoordinate chunkBeingPopulated)
	{
		// Get the corner block coords
		int x = chunkCoord.getChunkX() * 16;
		int z = chunkCoord.getChunkZ() * 16;

		// Get the biome of the other corner
		LocalBiome biome = world.getBiome(x + 15, z + 15);

		// Null check
		if (biome == null)
		{
			if(OTG.getPluginConfig().spawnLog)
			{
				OTG.log(LogMarker.ERROR, "Unknown biome at {},{}  (chunk {}). Population failed.", x + 15, z + 15, chunkCoord);
			}
			return;
		}

		BO4CustomStructure structureStart = world.getStructureCache().bo4StructureCache.get(chunkCoord);
		if (structureStart != null && structureStart.start != null)
		{
			structureStart.spawnInChunk(chunkCoord, world, chunkBeingPopulated);
		} else {
			// Only trees plotted here			
		}
		
		// Complex surface blocks
		// TODO: Reimplement placeComplexSurfaceBlocks?
		//placeComplexSurfaceBlocks(chunkCoord);
		
		// All done spawning structures for this chunk, clean up cache
		if(!world.isInsidePregeneratedRegion(chunkCoord))
		{
			world.getStructureCache().bo4StructureCache.put(chunkCoord, null);
		} else {
			world.getStructureCache().bo4StructureCache.remove(chunkCoord);
		}
	}
}