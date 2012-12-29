package com.khorn.terraincontrol.generator;

import static com.khorn.terraincontrol.events.PopulateEvent.Type.BEGIN;
import static com.khorn.terraincontrol.events.PopulateEvent.Type.END;
import static com.khorn.terraincontrol.events.ResourceEvent.Type.ICE;

import java.util.Random;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.events.PopulateEvent;
import com.khorn.terraincontrol.events.ResourceEvent;
import com.khorn.terraincontrol.generator.resourcegens.Resource;

public class ObjectSpawner
{
    private WorldConfig worldSettings;
    private Random rand;
    private LocalWorld world;

    public ObjectSpawner(WorldConfig wrk, LocalWorld localWorld)
    {
        this.worldSettings = wrk;
        this.rand = new Random();
        this.world = localWorld;
    }

    public void populate(int chunkX, int chunkZ)
    {
        int x = chunkX * 16;
        int z = chunkZ * 16;

        int biomeId = world.getCalculatedBiomeId(x + 16, z + 16);
        BiomeConfig localBiomeConfig = this.worldSettings.biomeConfigs[biomeId];

        this.rand.setSeed(world.getSeed());
        long l1 = this.rand.nextLong() / 2L * 2L + 1L;
        long l2 = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed(chunkX * l1 + chunkZ * l2 ^ world.getSeed());

        TerrainControl.firePopulateEvent(new PopulateEvent(BEGIN, world, rand, chunkX, chunkZ, false));
        
        boolean hasGeneratedAVillage = world.PlaceTerrainObjects(rand, chunkX, chunkZ);

        // Resource sequence
        for (int i = 0; i < localBiomeConfig.ResourceCount; i++)
        {
            world.setChunksCreations(false);
            Resource res = localBiomeConfig.ResourceSequence[i];
            res.process(world, rand, chunkX, chunkZ, hasGeneratedAVillage);
        }

        // Snow and ice
        ResourceEvent event = new ResourceEvent(ICE, world, rand, chunkX, chunkZ, DefaultMaterial.ICE.id, 0, hasGeneratedAVillage);
        TerrainControl.fireResourceEvent(event);
        if (!event.isCancelled())
        	placeSnowAndIce(chunkX, chunkZ);

        world.replaceBlocks();

        world.replaceBiomesLate();

        TerrainControl.firePopulateEvent(new PopulateEvent(END, world, rand, chunkX, chunkZ, hasGeneratedAVillage));
        
        if (this.worldSettings.isDeprecated)
            this.worldSettings = this.worldSettings.newSettings;
    }
    
    protected void placeSnowAndIce(int chunkX, int chunkZ)
    {
        int x = chunkX * 16 + 8;
        int z = chunkZ * 16 + 8;
        for (int i = 0; i < 16; i++)
        {
            for (int j = 0; j < 16; j++)
            {
                int blockToFreezeX = x + i;
                int blockToFreezeZ = z + j;
                BiomeConfig biomeConfig = worldSettings.biomeConfigs[world.getCalculatedBiomeId(blockToFreezeX, blockToFreezeZ)];
                if (biomeConfig.BiomeTemperature < TCDefaultValues.snowAndIceMaxTemp.floatValue())
                {
                    int blockToFreezeY = world.getHighestBlockYAt(blockToFreezeX, blockToFreezeZ);
                    if (blockToFreezeY > 0)
                    {
                        // Ice has to be placed one block in the world
                        if (DefaultMaterial.getMaterial(world.getTypeId(blockToFreezeX, blockToFreezeY - 1, blockToFreezeZ)).isLiquid())
                        {
                            world.setBlock(blockToFreezeX, blockToFreezeY - 1, blockToFreezeZ, biomeConfig.iceBlock, 0);
                        } else
                        {
                            // Snow has to be placed on an empty space on a
                            // solid block in the world
                            if (world.getMaterial(blockToFreezeX, blockToFreezeY, blockToFreezeZ) == DefaultMaterial.AIR)
                            {
                                if (world.getMaterial(blockToFreezeX, blockToFreezeY - 1, blockToFreezeZ).isSolid())
                                {
                                    world.setBlock(blockToFreezeX, blockToFreezeY, blockToFreezeZ, DefaultMaterial.SNOW.id, 0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}