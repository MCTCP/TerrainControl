package com.pg85.otg.gen.resource;

import com.pg85.otg.config.biome.Resource;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;

import java.util.List;
import java.util.Random;

public class AboveWaterGen extends Resource
{
    public AboveWaterGen(IBiomeConfig config, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
    {
        super(config, args, logger, materialReader);
        assureSize(3, args);

        material = materialReader.readMaterial(args.get(0));
        frequency = readInt(args.get(1), 1, 100);
        rarity = readRarity(args.get(2));
    }
    
    @Override
    public void spawn(IWorldGenRegion worldGenregion, Random rand, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
    {    	
    	// Make sure we stay within population bounds, anything outside won't be spawned (unless it's in an existing chunk).
    	
        int y = worldGenregion.getBlockAboveLiquidHeight(x, z, chunkBeingPopulated);
        if (y == -1)
		{
            return;
		}

        //parseMaterials(worldGenregion.getWorldConfig(), material, null);
        
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
            
            worldMaterial = worldGenregion.getMaterial(j, k, m, chunkBeingPopulated);
            if (worldMaterial == null || !worldMaterial.isAir())
            {
            	continue;
            }

            worldMaterialBeneath = worldGenregion.getMaterial(j, k - 1, m, chunkBeingPopulated);            
            if (
        		worldMaterialBeneath != null &&
				!worldMaterialBeneath.isLiquid()
    		)
            {
                continue;
            }
            
            worldGenregion.setBlock(j, k, m, material, null, chunkBeingPopulated, false);
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
