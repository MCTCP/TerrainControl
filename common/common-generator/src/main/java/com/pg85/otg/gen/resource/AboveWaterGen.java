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

        LocalMaterialData worldMaterial;
        LocalMaterialData worldMaterialBeneath;
        
        for (int i = 0; i < 10; i++)
        {
            int localX = x + rand.nextInt(8) - rand.nextInt(8);
            int localY = y + rand.nextInt(4) - rand.nextInt(4);
            int localZ = z + rand.nextInt(8) - rand.nextInt(8);
            
            worldMaterial = worldGenregion.getMaterial(localX, localY, localZ, chunkBeingPopulated);
            if (worldMaterial == null || !worldMaterial.isAir())
            {
            	continue;
            }

            worldMaterialBeneath = worldGenregion.getMaterial(localX, localY - 1, localZ, chunkBeingPopulated);
            if (
        		worldMaterialBeneath != null &&
				!worldMaterialBeneath.isLiquid()
    		)
            {
                continue;
            }
            
            worldGenregion.setBlock(localX, localY, localZ, material, null, chunkBeingPopulated, false);
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
