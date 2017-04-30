package com.khorn.terraincontrol.generator;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.configuration.ConfigProvider;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.bo3.BO3;
import com.khorn.terraincontrol.customobjects.bo3.BO3Settings.SpawnHeightEnum;
import com.khorn.terraincontrol.generator.noise.NoiseGeneratorNewOctaves;
import com.khorn.terraincontrol.generator.resource.Resource;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.Rotation;

import java.util.Arrays;
import java.util.Random;

public class ObjectSpawner
{
    private final ConfigProvider configProvider;
    private final Random rand;
    private final LocalWorld world;

    public ObjectSpawner(ConfigProvider configProvider, LocalWorld localWorld)
    {
        this.configProvider = configProvider;
        this.rand = new Random();
        this.world = localWorld;
        new NoiseGeneratorNewOctaves(new Random(world.getSeed()), 4);
    }

    public boolean processing = false;   
    public void populate(ChunkCoordinate chunkCoord)
    {        
		if (!processing)
		{
			processing = true;
        
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
	        	CustomObject customObject = this.world.getConfigs().getCustomObjects().getObjectByName(this.world.getConfigs().getWorldConfig().BO3AtSpawn);
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
	                ((Resource) res).process(world, rand, hasVillage, chunkCoord);
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
	        
			processing = false;
		} else {			
			TerrainControl.log(LogMarker.TRACE,"Error, minecraft engine attempted to populate two chunks at once! Chunk X" + chunkCoord.getChunkX() + " Z" + chunkCoord.getChunkZ() + ". This is probably caused by a mod spawning blocks in unloaded chunks and can cause lag as well as missing trees, ores and other TC resources. Please try to find out which mod causes this, disable the feature causing it and alert the mod creator. Set the log level to Debug in mods/OpenTerrainGenerator/TerranControl.ini file for a stack trace. (Update: The recently added multi-dimension features may be causing this log message occasionally, will fix a.s.a.p).");
			TerrainControl.log(LogMarker.TRACE, Arrays.toString(Thread.currentThread().getStackTrace()));
		}
    }

}