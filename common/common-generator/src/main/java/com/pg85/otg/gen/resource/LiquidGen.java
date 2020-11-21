package com.pg85.otg.gen.resource;

import com.pg85.otg.config.biome.Resource;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.MaterialSet;

import java.util.List;
import java.util.Random;

public class LiquidGen extends Resource
{
    private final int maxAltitude;
    private final int minAltitude;
    private final MaterialSet sourceBlocks;

    public LiquidGen(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
    {
        super(biomeConfig, args, logger, materialReader);
        assureSize(6, args);

        material = materialReader.readMaterial(args.get(0));
        frequency = readInt(args.get(1), 1, 5000);
        rarity = readRarity(args.get(2));
        minAltitude = readInt(args.get(3), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
        maxAltitude = readInt(args.get(4), minAltitude, Constants.WORLD_HEIGHT - 1);
        sourceBlocks = readMaterials(args, 5, materialReader);
    }

    @Override
    public boolean equals(Object other)
    {
		if (!super.equals(other))
		{
			return false;
		}
		if (other == null)
		{
			return false;
		}
		if (other == this)
		{
			return true;
		}
		if (getClass() != other.getClass())
		{
			return false;
		}
		final LiquidGen compare = (LiquidGen) other;
		return 
			this.minAltitude == compare.minAltitude 
			&& this.maxAltitude == compare.maxAltitude
			&& (
				this.sourceBlocks == null ? 
				this.sourceBlocks == compare.sourceBlocks
				: this.sourceBlocks.equals(compare.sourceBlocks)
			)
		;
	}

    @Override
    public int getPriority()
    {
        return 2;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 17 * hash + super.hashCode();
        hash = 17 * hash + this.minAltitude;
        hash = 17 * hash + this.maxAltitude;
        hash = 17 * hash + (this.sourceBlocks != null ? this.sourceBlocks.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString()
    {
        return "Liquid(" + material + "," + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + makeMaterials(sourceBlocks) + ")";
    }

    @Override
    public void spawn(IWorldGenRegion worldGenregion, Random rand, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
    {
    	// Make sure we stay within population bounds, anything outside won't be spawned (unless it's in an existing chunk).
    	
        int y = RandomHelper.numberInRange(rand, minAltitude, maxAltitude);
        
        //parseMaterials(worldGenregion.getWorldConfig(), material, sourceBlocks);

        LocalMaterialData worldMaterial = worldGenregion.getMaterial(x, y + 1, z, chunkBeingPopulated);
        if (worldMaterial == null || !sourceBlocks.contains(worldMaterial))
        {
            return;
        }
        
        worldMaterial = worldGenregion.getMaterial(x, y - 1, z, chunkBeingPopulated);
        if (worldMaterial == null || !sourceBlocks.contains(worldMaterial))
        {
            return;
        }

        worldMaterial = worldGenregion.getMaterial(x, y, z, chunkBeingPopulated);
        if (worldMaterial == null || !worldMaterial.isAir() || !sourceBlocks.contains(worldMaterial))
        {
            return;
        }

        int i = 0;
        int j = 0;

        worldMaterial = worldGenregion.getMaterial(x - 1, y, z, chunkBeingPopulated);
        i = (worldMaterial != null && sourceBlocks.contains(worldMaterial)) ? i + 1 : i;
        j = (worldMaterial != null && worldMaterial.isAir()) ? j + 1 : j;

        worldMaterial = worldGenregion.getMaterial(x + 1, y, z, chunkBeingPopulated);
        i = (worldMaterial != null && sourceBlocks.contains(worldMaterial)) ? i + 1 : i;
        j = (worldMaterial != null && worldMaterial.isAir()) ? j + 1 : j;

        worldMaterial = worldGenregion.getMaterial(x, y, z - 1, chunkBeingPopulated);
        i = (worldMaterial != null && sourceBlocks.contains(worldMaterial)) ? i + 1 : i;
        j = (worldMaterial != null && worldMaterial.isAir()) ? j + 1 : j;

        worldMaterial = worldGenregion.getMaterial(x, y, z + 1, chunkBeingPopulated);
        i = (worldMaterial != null && sourceBlocks.contains(worldMaterial)) ? i + 1 : i;
        j = (worldMaterial != null && worldMaterial.isAir()) ? j + 1 : j;

        if ((i == 3) && (j == 1))
        {
        	worldGenregion.setBlock(x, y, z, material, null, chunkBeingPopulated, false);
        }
    }
}
