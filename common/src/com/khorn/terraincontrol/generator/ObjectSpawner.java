package com.khorn.terraincontrol.generator;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.Resource;
import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.resourcegens.ResourceType;

import java.util.Random;

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

        int biomeId = world.getBiome(x + 16, z + 16);
        BiomeConfig localBiomeConfig = this.worldSettings.biomeConfigs[biomeId];

        this.rand.setSeed(world.getSeed());
        long l1 = this.rand.nextLong() / 2L * 2L + 1L;
        long l2 = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed(chunkX * l1 + chunkZ * l2 ^ world.getSeed());

        boolean Village = world.PlaceTerrainObjects(rand, chunkX, chunkZ);

        // Resource sequence
        for (int i = 0; i < localBiomeConfig.ResourceCount; i++)
        {
            Resource res = localBiomeConfig.ResourceSequence[i];
            if (res.Type == ResourceType.SmallLake && Village)
                continue;
            world.setChunksCreations(res.Type.CreateNewChunks);
            res.Type.Generator.Process(world, rand, res, x, z);
        }

        // Snow and ice
        for (int i = 0; i < 16; i++)
        {
            for (int j = 0; j < 16; j++)
            {
                int blockToFreezeX = x + 8 + i;
                int blockToFreezeZ = z + 8 + j;
                BiomeConfig biomeConfig = worldSettings.biomeConfigs[world.getBiome(blockToFreezeX, blockToFreezeZ)];
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
                            // Snow has to be placed on the world
                            if (DefaultMaterial.getMaterial(world.getTypeId(blockToFreezeX, blockToFreezeY, blockToFreezeZ)) == DefaultMaterial.AIR)
                            {
                                world.setBlock(blockToFreezeX, blockToFreezeY, blockToFreezeZ, DefaultMaterial.SNOW.id, 0);
                            }
                        }
                    }
                }
            }
        }

        world.DoBlockReplace();

        world.DoBiomeReplace();

        if (this.worldSettings.isDeprecated)
            this.worldSettings = this.worldSettings.newSettings;
    }
}