package com.pg85.otg.gen.resource;

import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;

import java.util.List;
import java.util.Random;

public class AboveWaterResource extends FrequencyResourceBase
{
	private final LocalMaterialData material;
	
    public AboveWaterResource(IBiomeConfig config, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
    {
        super(config, args, logger, materialReader);
        assureSize(3, args);

        this.material = materialReader.readMaterial(args.get(0));
        this.frequency = readInt(args.get(1), 1, 100);
        this.rarity = readRarity(args.get(2));
    }
    
    @Override
    public void spawn(IWorldGenRegion worldGenregion, Random rand, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
    {
        int y = worldGenregion.getBlockAboveLiquidHeight(x, z, chunkBeingPopulated);
        if (y == -1)
		{
            return;
		}

        LocalMaterialData worldMaterial;
        LocalMaterialData worldMaterialBeneath;
        int localX;
        int localY;
        int localZ;        
        for (int i = 0; i < 10; i++)
        {
            localX = x + rand.nextInt(8) - rand.nextInt(8);
            localY = y + rand.nextInt(4) - rand.nextInt(4);
            localZ = z + rand.nextInt(8) - rand.nextInt(8);
            
            worldMaterial = worldGenregion.getMaterial(localX, localY, localZ, chunkBeingPopulated);
            if (worldMaterial == null || !worldMaterial.isAir())
            {
            	continue;
            }

            worldMaterialBeneath = worldGenregion.getMaterial(localX, localY - 1, localZ, chunkBeingPopulated);
            if (worldMaterialBeneath != null && !worldMaterialBeneath.isLiquid())
            {
                continue;
            }
            
            worldGenregion.setBlock(localX, localY, localZ, this.material, null, chunkBeingPopulated, false);
        }
    }

    @Override
    public String toString()
    {
        return "AboveWaterRes(" + this.material + "," + this.frequency + "," + this.rarity + ")";
    }
}
