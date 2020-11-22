package com.pg85.otg.gen.resource;

import com.pg85.otg.config.biome.Resource;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.MaterialSet;

import java.util.List;
import java.util.Random;

/**
 * Generates a disk-alike structure for sand, gravel, and clay.
 * TODO: This needs to be renamed to DiskGen()
 */
public class UnderWaterOreGen extends Resource
{
    private final int size;
    private final MaterialSet sourceBlocks;

    public UnderWaterOreGen(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
    {
    	super(biomeConfig, args, logger, materialReader);
        assureSize(5, args);
        material = materialReader.readMaterial(args.get(0));
        size = readInt(args.get(1), 1, 8);
        frequency = readInt(args.get(2), 1, 100);
        rarity = readRarity(args.get(3));
        sourceBlocks = readMaterials(args, 4, materialReader);
    }

    @Override
    public boolean equals(Object other)
    {
        if (!super.equals(other))
            return false;
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (getClass() != other.getClass())
            return false;
        final UnderWaterOreGen compare = (UnderWaterOreGen) other;
        return (this.sourceBlocks == null ? this.sourceBlocks == compare.sourceBlocks
                : this.sourceBlocks.equals(compare.sourceBlocks))
               && this.size == compare.size;
    }

    @Override
    public int getPriority()
    {
        return -12;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 47 * hash + super.hashCode();
        hash = 47 * hash + (this.sourceBlocks != null ? this.sourceBlocks.hashCode() : 0);
        hash = 47 * hash + this.size;
        return hash;
    }

    @Override
    public String toString()
    {
        return "UnderWaterOre(" + material + "," + size + "," + frequency + "," + rarity + makeMaterials(sourceBlocks) + ")";
    }

    @Override
    public void spawn(IWorldGenRegion worldGenRegion, Random rand, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
    {
    	// Make sure we stay within population bounds, anything outside won't be spawned (unless it's in an existing chunk).
    	
        int firstSolidBlock = worldGenRegion.getBlockAboveSolidHeight(x, z, chunkBeingPopulated) - 1;
        if (worldGenRegion.getBlockAboveLiquidHeight(x, z, chunkBeingPopulated) < firstSolidBlock || firstSolidBlock == -1)
        {
            return;
        }
        
        //parseMaterials(worldGenRegion.getWorldConfig(), this.material, this.sourceBlocks);

        if(worldGenRegion.getWorldConfig().isDisableOreGen())
        {
    		if(this.material.isOre())
        	{
        		return;
        	}
        }
        
        int currentSize = rand.nextInt(this.size);
        int deltaX;
        int deltaZ;
        LocalMaterialData sourceBlock;
        for (int currentX = x - currentSize; currentX <= x + currentSize; currentX++)
        {
            for (int currentZ = z - currentSize; currentZ <= z + currentSize; currentZ++)
            {
                deltaX = currentX - x;
                deltaZ = currentZ - z;
                if (deltaX * deltaX + deltaZ * deltaZ <= currentSize * currentSize)
                {
                    for (int y = firstSolidBlock - 2; y <= firstSolidBlock + 2; y++)
                    {
                        sourceBlock = worldGenRegion.getMaterial(currentX, y, currentZ, chunkBeingPopulated);
                        if (this.sourceBlocks.contains(sourceBlock))
                        {
                        	worldGenRegion.setBlock(currentX, y, currentZ, this.material, null, chunkBeingPopulated, true);
                        }
                    }
                }
            }
        }
    }
}
