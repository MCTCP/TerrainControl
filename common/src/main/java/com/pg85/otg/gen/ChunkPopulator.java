package com.pg85.otg.gen;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorldGenRegion;
import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.customobjects.structures.CustomStructureCache;
import com.pg85.otg.gen.resource.Resource;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;

import java.util.Arrays;
import java.util.Random;

public class ChunkPopulator
{
    private final Random rand;
    private boolean processing = false;
   
    public ChunkPopulator()
    {
        this.rand = new Random();
    }

    public void populate(ChunkCoordinate chunkCoord, CustomStructureCache structureCache, LocalWorldGenRegion worldGenRegion, BiomeConfig biomeConfig, boolean isBO4Enabled)
    {
		if (!this.processing)
		{
			this.processing = true;

			// Cache all biomes in the are being populated (2x2 chunks)
			worldGenRegion.cacheBiomesForPopulation(chunkCoord);
			doPopulate(chunkCoord, structureCache, worldGenRegion, biomeConfig, isBO4Enabled);
			
			this.processing = false;
		} else {

			// Don't use the population chunk biome cache during cascading chunk generation
			worldGenRegion.invalidatePopulationBiomeCache();
			doPopulate(chunkCoord, structureCache, worldGenRegion, biomeConfig, isBO4Enabled);
			
			OTG.log(LogMarker.INFO, "Cascading chunk generation detected.");
			if(OTG.getPluginConfig().developerMode)
			{			
				OTG.log(LogMarker.INFO, Arrays.toString(Thread.currentThread().getStackTrace()));
			}
		}
    }
    	
    public void doPopulate(ChunkCoordinate chunkCoord, CustomStructureCache structureCache, LocalWorldGenRegion worldGenRegion, BiomeConfig biomeConfig, boolean isBO4Enabled)
    {    	
        // Get the corner block coords
        int x = chunkCoord.getChunkX() * 16;
        int z = chunkCoord.getChunkZ() * 16;
     
        //LocalBiome biome = world.getBiomeForPopulation(x + 8, z + 8, chunkCoord);
        
        //BiomeConfig biomeConfig = world.getBiomeConfig(x,z);
        
        // Null check
        if (biomeConfig == null)
        {
            OTG.log(LogMarker.WARN, "Unknown biome at {},{}  (chunk {}). Could not populate chunk.", x + 8, z + 8, chunkCoord);
            return;
        }
        
        // Get the random generator
        long resourcesSeed = worldGenRegion.getWorldConfig().resourcesSeed != 0L ? worldGenRegion.getWorldConfig().resourcesSeed : worldGenRegion.getSeed();
        this.rand.setSeed(resourcesSeed);
        long l1 = this.rand.nextLong() / 2L * 2L + 1L;
        long l2 = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed(chunkCoord.getChunkX() * l1 + chunkCoord.getChunkZ() * l2 ^ resourcesSeed);

        boolean hasVillage = false;

        // Use BO4 logic for BO4 worlds
		if(isBO4Enabled)
		{
			// Plot BO4's for all 4 chunks being populated, so we can be sure the chunks have
			// had a chance to be plotted before being populated. We'll spawn BO4's after 
			// ores and lakes, but before any other resources.
			structureCache.plotBo4Structures(worldGenRegion, this.rand, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ()));
			structureCache.plotBo4Structures(worldGenRegion, this.rand, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ() + 1));
			structureCache.plotBo4Structures(worldGenRegion, this.rand, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ() + 1));
			structureCache.plotBo4Structures(worldGenRegion, this.rand, chunkCoord);
			
			spawnBO4s(structureCache, worldGenRegion, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ()), chunkCoord);
		}
        
        // Generate structures
        //hasVillage = world.placeDefaultStructures(this.rand, chunkCoord);

        // Mark population started
        //OTG.firePopulationStartEvent(world, this.rand, hasVillage, chunkCoord);
		
        // Resource sequence
        for (ConfigFunction<BiomeConfig> res : biomeConfig.resourceSequence)
        {
            if (res instanceof Resource)
            {
                ((Resource)res).process(structureCache, worldGenRegion, this.rand, hasVillage, chunkCoord);
            }
        }

        // Animals
        //world.placePopulationMobs(biome, this.rand, chunkCoord);

        // Snow and ice
		// TODO: Fire PopulateChunkEvent.Populate.EventType.ICE for Forge
        //new FrozenSurfaceHelper(world).freezeChunk(chunkCoord);

        // Replace blocks
        //world.replaceBlocks(chunkCoord);

        // Mark population ended
        //OTG.firePopulationEndEvent(world, this.rand, hasVillage, chunkCoord);
    }
    
	// BO4's should always stay within chunk borders, so we can spawn them for all
	// 4 of the chunk in the populated area, ensuring all resources that should be
	// placed afterwards spawn on top of bo4's. 
	private void spawnBO4s(CustomStructureCache structureCache, LocalWorldGenRegion worldGenRegion, ChunkCoordinate chunkCoord, ChunkCoordinate chunkBeingPopulated)
	{
		spawnBO4(structureCache, worldGenRegion, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ()), chunkCoord);
		spawnBO4(structureCache, worldGenRegion, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ() + 1), chunkCoord);
		spawnBO4(structureCache, worldGenRegion, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ() + 1), chunkCoord);
		spawnBO4(structureCache, worldGenRegion, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ()), chunkCoord);
	}	

	private void spawnBO4(CustomStructureCache structureCache, LocalWorldGenRegion worldGenRegion, ChunkCoordinate chunkCoord, ChunkCoordinate chunkBeingPopulated)
	{
		structureCache.spawnBo4Chunk(worldGenRegion, chunkCoord, chunkBeingPopulated);
	}
}
