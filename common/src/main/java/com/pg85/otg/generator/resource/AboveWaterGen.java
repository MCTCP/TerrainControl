package com.pg85.otg.generator.resource;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.ChunkCoordinate;

import java.util.List;
import java.util.Random;

public class AboveWaterGen extends Resource
{
    public AboveWaterGen(BiomeConfig config, List<String> args) throws InvalidConfigException
    {
        super(config);
        assureSize(3, args);

        material = readMaterial(args.get(0));
        frequency = readInt(args.get(1), 1, 100);
        rarity = readRarity(args.get(2));
    }

    @Override
    public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
    {    	
    	// Make sure we stay within population bounds, anything outside won't be spawned (unless it's in an existing chunk).
    	
        int y = world.getBlockAboveLiquidHeight(x, z, chunkBeingPopulated);
        if (y == -1)
		{
            return;
		}

        parseMaterials(world, material, null);
        
        int j;
        int k;
        int m;
        LocalMaterialData worldMaterial;
        LocalMaterialData worldMaterialBeneath;
        
        for (int i = 0; i < 10; i++)
        {
            j = x + rand.nextInt(8) - rand.nextInt(8);
            k = y + rand.nextInt(4) - rand.nextInt(4);
            m = z + rand.nextInt(8) - rand.nextInt(8);
            
            worldMaterial = world.getMaterial(j, k, m, chunkBeingPopulated);
            if (worldMaterial == null || !worldMaterial.isAir())
            {
            	continue;
            }

            worldMaterialBeneath = world.getMaterial(j, k - 1, m, chunkBeingPopulated);            
            if (
        		worldMaterialBeneath != null &&
				!worldMaterialBeneath.isLiquid()
    		)
            {
                continue;
            }
            
            world.setBlock(j, k, m, material, null, chunkBeingPopulated);
        }
    }

    @Override
    public String toString()
    {
        return "AboveWaterRes(" + material + "," + frequency + "," + rarity + ")";
    }

    @Override
    public int getPriority()
    {
        return -11;
    }
}
