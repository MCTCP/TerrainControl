package com.pg85.otg.gen;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorldGenRegion;
import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.customobjects.structures.CustomStructureCache;
import com.pg85.otg.gen.resource.Resource;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;

import java.util.Random;

public class ChunkPopulator
{
    private final Random rand;
   
    public ChunkPopulator()
    {
        this.rand = new Random();
    }

    public void populate(ChunkCoordinate chunkCoord, CustomStructureCache structureCache, LocalWorldGenRegion worldGenRegion, BiomeConfig biomeConfig)
    {
		// Cache all biomes in the are being populated (2x2 chunks)
		//world.cacheBiomesForPopulation(chunkCoord);
		
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
}