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
import com.pg85.otg.generator.resource.UnderWaterPlantGen;
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
	// Locking objects / checks to prevent populate running on multiple threads,
	// or when the world is waiting for an opportunity to save.
	// TODO: Make this prettier
	public Object lockingObject = new Object();
	public boolean populating;
	public boolean processing = false;
	public boolean saving;
	public boolean saveRequired;
	//
	
    private final LocalWorld world;
    private final Random rand;
    private final WorldConfig worldConfig;
    
    public ObjectSpawner(ConfigProvider configProvider, LocalWorld localWorld)
    {
        this.worldConfig = configProvider.getWorldConfig();
        this.rand = new Random();
        this.world = localWorld;
    }

    public void populate(ChunkCoordinate chunkCoord)
    {
    	boolean unlockWhenDone = false;
		// Wait for another thread running SaveToDisk, then place a lock.
		boolean firstLog = false;
		while(true)
		{
			//OTG.log(LogMarker.INFO, "Populate waiting on SaveToDisk.");
			synchronized(this.lockingObject)
			{
				if(!this.saving)
				{
					// If populating then this method is being called recursively (indicating cascading chunk-gen).
					// This method can be called recursively, but should never be called by two threads at once.
					// TODO: Make sure that's the case.
					if(!this.populating)
					{
						this.populating = true;
						unlockWhenDone = true;
					}
					break;
				} else {
					if(firstLog)
					{
						OTG.log(LogMarker.WARN, "Populate waiting on SaveToDisk. Although other mods could be causing this and there may not be any problem, this can potentially cause an endless loop!");
						firstLog = false;
					}
				}
			}
		}
		synchronized(this.lockingObject)
		{
			this.saveRequired = true;
		}

		if (!this.processing)
		{
			this.processing = true;

			// Cache all biomes in the are being populated (2x2 chunks)
			this.world.cacheBiomesForPopulation(chunkCoord);
			doPopulate(chunkCoord);
			
			this.processing = false;
		} else {

			// Don't use the population chunk biome cache during cascading chunk generation
			this.world.invalidatePopulationBiomeCache();
			doPopulate(chunkCoord);
			
			OTG.log(LogMarker.INFO, "Cascading chunk generation detected.");
			if(OTG.getPluginConfig().developerMode)
			{			
				OTG.log(LogMarker.INFO, Arrays.toString(Thread.currentThread().getStackTrace()));
			}
		}

		// Release the lock
		synchronized(this.lockingObject)
		{
			// This assumes that this method can only be called alone or recursively, never by 2 threads at once.
			// TODO: Make sure that's the case.
			if(unlockWhenDone)
			{
				this.populating = false;
			}
		}

		// Resource spawning may have changed terrain dramatically, update 
		// the spawnpoint so players don't spawn mid-air or underground
		if(chunkCoord.equals(this.world.getSpawnChunk()))
		{
			this.world.updateSpawnPointY();
		}
    }

    private void doPopulate(ChunkCoordinate chunkCoord)
    {
        // Get the corner block coords
        int x = chunkCoord.getChunkX() * 16;
        int z = chunkCoord.getChunkZ() * 16;
     
        LocalBiome biome = this.world.getBiomeForPopulation(x + 8, z + 8, chunkCoord);
        
        // Null check
        if (biome == null)
        {
            OTG.log(LogMarker.WARN, "Unknown biome at {},{}  (chunk {}). Could not populate chunk.", x + 8, z + 8, chunkCoord);
            return;
        }

        BiomeConfig biomeConfig = biome.getBiomeConfig();
        
        // Use BO4 logic for BO4 worlds
		if(this.world.isBo4Enabled())
		{
			// Plot BO4's for all 4 chunks being populated, so we can be sure the chunks have
			// had a chance to be plotted before being populated. We'll spawn BO4's after 
			// ores and lakes, but before any other resources.
			this.world.getStructureCache().plotBo4Structures(this.rand, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ()));
			this.world.getStructureCache().plotBo4Structures(this.rand, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ() + 1));
			this.world.getStructureCache().plotBo4Structures(this.rand, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ() + 1));
			this.world.getStructureCache().plotBo4Structures(this.rand, chunkCoord);

	        ChunkCoordinate spawnChunk = this.world.getSpawnChunk();

	        boolean hasVillage = false;

	        // If a BO3AtSpawn has been defined, spawn it.
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
							if (this.worldConfig.spawnPointSet)
							{
								y = this.worldConfig.spawnPointY;
							}
							else y = (int) (((BO3)customObject).getSettings().minHeight + (Math.random() * (((BO3)customObject).getSettings().maxHeight - ((BO3)customObject).getSettings().minHeight)));
	        			}

	        			y += ((BO3)customObject).getSettings().spawnHeightOffset;
	        			// TODO: This may spawn the structure across chunk borders if its larger than 16 in any direction from its center.
	        			((BO3)customObject).spawnForced(this.world, this.rand, Rotation.NORTH, spawnChunk.getBlockX() + 15, y, spawnChunk.getBlockZ() + 15);
	        		}
	        	}
	        } else {
		        // Generate structures
		        hasVillage = this.world.placeDefaultStructures(this.rand, chunkCoord);
	        }

			// Get the random generator
			long resourcesSeed = this.worldConfig.resourcesSeed != 0L ? this.worldConfig.resourcesSeed : this.world.getSeed();
			this.rand.setSeed(resourcesSeed);
			long l1 = this.rand.nextLong() / 2L * 2L + 1L;
			long l2 = this.rand.nextLong() / 2L * 2L + 1L;
			this.rand.setSeed(chunkCoord.getChunkX() * l1 + chunkCoord.getChunkZ() * l2 ^ resourcesSeed);

			// Mark population started
			OTG.firePopulationStartEvent(this.world, this.rand, hasVillage, chunkCoord);

			// Spawn any resources that should be part of the base terrain
			// Ores, veins, surfacepatch etc.
			processResourcesBeforeBo4s(chunkCoord, biome, biomeConfig);

			spawnBO4s(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ()), chunkCoord);
			
			// Generate structures

			// Spawn any resources that should spawn on top of base terrain and bo4's.
			processResourcesAfterBo4s(chunkCoord, hasVillage, biome, biomeConfig);

			// Mark population ended
			OTG.firePopulationEndEvent(world, rand, hasVillage, chunkCoord);

		} else {

	        // Get the random generator
	        long resourcesSeed = this.worldConfig.resourcesSeed != 0L ? this.worldConfig.resourcesSeed : this.world.getSeed();
	        this.rand.setSeed(resourcesSeed);
	        long l1 = this.rand.nextLong() / 2L * 2L + 1L;
	        long l2 = this.rand.nextLong() / 2L * 2L + 1L;
	        this.rand.setSeed(chunkCoord.getChunkX() * l1 + chunkCoord.getChunkZ() * l2 ^ resourcesSeed);

	        ChunkCoordinate spawnChunk = this.world.getSpawnChunk();

	        boolean hasVillage = false;

	        // If a BO3AtSpawn has been defined, spawn it.
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
							if (this.worldConfig.spawnPointSet)
							{
								y = this.worldConfig.spawnPointY;
							}
							else y = (int) (((BO3)customObject).getSettings().minHeight + (Math.random() * (((BO3)customObject).getSettings().maxHeight - ((BO3)customObject).getSettings().minHeight)));
	        			}

	        			y += ((BO3)customObject).getSettings().spawnHeightOffset;
	        			// TODO: This may spawn the structure across chunk borders if its larger than 16 in any direction from its center.
	        			boolean ret = ((BO3)customObject).spawnForced(this.world, this.rand, Rotation.NORTH, spawnChunk.getBlockX() + 15, y, spawnChunk.getBlockZ() + 15);
	        			if (!ret && OTG.getPluginConfig().spawnLog)
	        			{
	        				OTG.log(LogMarker.WARN, "Failed to spawn bo3AtSpawn object");
						}
	        		}
	        	}
	        } else {
		        // Generate structures
		        hasVillage = this.world.placeDefaultStructures(this.rand, chunkCoord);
	        }

	        // Mark population started
	        OTG.firePopulationStartEvent(this.world, this.rand, hasVillage, chunkCoord);

	        // Resource sequence
	        for (ConfigFunction<BiomeConfig> res : biomeConfig.resourceSequence)
	        {
	            if (res instanceof Resource)
	            {
	                ((Resource)res).process(this.world, this.rand, hasVillage, chunkCoord);
	            }
	        }

	        // Animals
	        this.world.placePopulationMobs(biome, this.rand, chunkCoord);

	        // Snow and ice
			// TODO: Fire PopulateChunkEvent.Populate.EventType.ICE for Forge
	        new FrozenSurfaceHelper(this.world).freezeChunk(chunkCoord);

	        // Replace blocks
	        this.world.replaceBlocks(chunkCoord);

	        // Mark population ended
	        OTG.firePopulationEndEvent(this.world, this.rand, hasVillage, chunkCoord);
		}
    }

	private void processResourcesBeforeBo4s(ChunkCoordinate chunkCoord, LocalBiome biome, BiomeConfig biomeConfig)
	{
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
				(res instanceof SmallLakeGen && !this.world.getStructureCache().isBo4ChunkOccupied(chunkCoord)) ||
				(res instanceof UndergroundLakeGen) || // TODO: look at potential size bug in UnderGroundLakeGen
				(res instanceof UnderWaterOreGen) || // TODO: This seems to be bugged, generate a plains only world with default settings and no sand appears where it does in TC
				(res instanceof VeinGen) || // TODO: Test this
				(res instanceof SurfacePatchGen)
			)
			{
				res.process(this.world, this.rand, false, chunkCoord);
			}
		}
	}

	private void processResourcesAfterBo4s(ChunkCoordinate chunkCoord, boolean hasGeneratedAVillage, LocalBiome biome, BiomeConfig biomeConfig)
	{
		// Get the random generator
		long resourcesSeed = this.worldConfig.resourcesSeed != 0L ? this.worldConfig.resourcesSeed : this.world.getSeed();
		this.rand.setSeed(resourcesSeed);
		long l1 = this.rand.nextLong() / 2L * 2L + 1L;
		long l2 = this.rand.nextLong() / 2L * 2L + 1L;
		this.rand.setSeed(chunkCoord.getChunkX() * l1 + chunkCoord.getChunkZ() * l2 ^ resourcesSeed);

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
			res.process(this.world, this.rand, hasGeneratedAVillage, chunkCoord);
		}
		for (Resource res : miscResources)
		{
			// TODO: Find out if these are always in the same order, trees
			// should spawn first?
			if (
				(res instanceof DungeonGen) ||
				(res instanceof AboveWaterGen) ||
				(res instanceof PlantGen) ||
				(res instanceof UnderWaterPlantGen) ||
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
				res.process(this.world, this.rand, hasGeneratedAVillage, chunkCoord);
			}
		}

		// don't use world.placePopulationMobs, it bypasses EntityLiving.getCanSpawnHere() :(
		//if(!worldConfig.improvedMobSpawning)
		{
			this.world.placePopulationMobs(biome, this.rand, chunkCoord);
		}

		// Snow and ice
		// TODO: Fire PopulateChunkEvent.Populate.EventType.ICE for Forge
		new FrozenSurfaceHelper(this.world).freezeChunk(chunkCoord);

		// Replace blocks 
		this.world.replaceBlocks(chunkCoord);
	}

	private void spawnBO4(ChunkCoordinate chunkCoord, ChunkCoordinate chunkBeingPopulated)
	{
		this.world.getStructureCache().spawnBo4Chunk(chunkCoord, chunkBeingPopulated);
		
		// Complex surface blocks
		// TODO: Reimplement placeComplexSurfaceBlocks?
		//placeComplexSurfaceBlocks(chunkCoord);
	}
	
	// BO4's should always stay within chunk borders, so we can spawn them for all
	// 4 of the chunk in the populated area, ensuring all resources that should be
	// placed afterwards spawn on top of bo4's. 
	private void spawnBO4s(ChunkCoordinate chunkCoord, ChunkCoordinate chunkBeingPopulated)
	{
		spawnBO4(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ()), chunkCoord);
		spawnBO4(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ() + 1), chunkCoord);
		spawnBO4(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ() + 1), chunkCoord);
		spawnBO4(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ()), chunkCoord);
	}
}