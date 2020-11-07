package com.pg85.otg.gen.resource;

import com.pg85.otg.common.LocalWorldGenRegion;
import com.pg85.otg.common.materials.LocalMaterialData;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.standard.PluginStandardValues;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.materials.MaterialSet;

import java.util.List;
import java.util.Random;

public class ReedGen extends Resource
{
    private final int maxAltitude;
    private final int minAltitude;
    private final MaterialSet sourceBlocks;
	
    public ReedGen(BiomeConfig biomeConfig, List<String> args) throws InvalidConfigException
    {
        super(biomeConfig);
        assureSize(6, args);

        material = readMaterial(args.get(0));
        frequency = readInt(args.get(1), 1, 100);
        rarity = readRarity(args.get(2));
        minAltitude = readInt(args.get(3), PluginStandardValues.WORLD_DEPTH,
                PluginStandardValues.WORLD_HEIGHT - 1);
        maxAltitude = readInt(args.get(4), minAltitude,
                PluginStandardValues.WORLD_HEIGHT - 1);
        sourceBlocks = readMaterials(args, 5);
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
        final ReedGen compare = (ReedGen) other;
        return this.minAltitude == compare.minAltitude
               && this.maxAltitude == compare.maxAltitude
               && (this.sourceBlocks == null ? this.sourceBlocks == compare.sourceBlocks
                   : this.sourceBlocks.equals(compare.sourceBlocks));
    }

    @Override
    public int getPriority()
    {
        return -34;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 23 * hash + super.hashCode();
        hash = 23 * hash + this.minAltitude;
        hash = 23 * hash + this.maxAltitude;
        hash = 23 * hash + (this.sourceBlocks != null ? this.sourceBlocks.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString()
    {
        return "Reed(" + material + "," + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + makeMaterials(sourceBlocks) + ")";
    }

    @Override
    public void spawn(LocalWorldGenRegion worldGenRegion, Random rand, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
    {
    	// Make sure we stay within population bounds, anything outside won't be spawned (unless it's in an existing chunk).
    	
        int y = worldGenRegion.getHighestBlockAboveYAt(x, z, chunkBeingPopulated);
        LocalMaterialData materialA = worldGenRegion.getMaterial(x - 1, y - 1, z, chunkBeingPopulated);
        LocalMaterialData materialB = worldGenRegion.getMaterial(x + 1, y - 1, z, chunkBeingPopulated);
        LocalMaterialData materialC = worldGenRegion.getMaterial(x, y - 1, z - 1, chunkBeingPopulated);
        LocalMaterialData materialD = worldGenRegion.getMaterial(x, y - 1, z + 1, chunkBeingPopulated);
        if (
    		y > this.maxAltitude || 
    		y < this.minAltitude || 
    		(
				materialA != null && !materialA.isLiquid() &&
				materialB != null && !materialB.isLiquid() &&
				materialC != null && !materialC.isLiquid() &&
				materialD != null && !materialD.isLiquid()
			)
		)
        {
            return;
        }
        
        parseMaterials(worldGenRegion.getWorldConfig(), material, sourceBlocks);
        
        LocalMaterialData worldMaterial = worldGenRegion.getMaterial(x, y - 1, z, chunkBeingPopulated);        
        if (worldMaterial == null || !this.sourceBlocks.contains(worldMaterial))
        {
            return;
        }  

        int n = 1 + rand.nextInt(2);
        for (int i1 = 0; i1 < n; i1++)
        {
        	worldGenRegion.setBlock(x, y + i1, z, this.material, null, chunkBeingPopulated, false);
        }
    }
}
