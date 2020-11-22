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

/**
 * Generates a cactus.
 */
public class CactusGen extends Resource
{
    private int minAltitude;
    private int maxAltitude;
    private MaterialSet sourceBlocks;

    public CactusGen(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
    {
        super(biomeConfig, args, logger, materialReader);
        assureSize(6, args);

        material = materialReader.readMaterial(args.get(0));
        frequency = readInt(args.get(1), 1, 100);
        rarity = readRarity(args.get(2));
        minAltitude = readInt(args.get(3), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
        maxAltitude = readInt(args.get(4), minAltitude, Constants.WORLD_HEIGHT - 1);
        sourceBlocks = readMaterials(args, 5, materialReader);
    }

    @Override
    public void spawn(IWorldGenRegion worldGenregion, Random rand, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
    {
    	// Make sure we stay within population bounds, anything outside won't be spawned (unless it's in an existing chunk).
    	
        int y = RandomHelper.numberInRange(rand, minAltitude, maxAltitude);
        
        //parseMaterials(worldGenregion.getWorldConfig(), material, sourceBlocks);
        LocalMaterialData worldMaterial;
        int cactusX;
        int cactusBaseY;
        int cactusZ;
        
        for (int i = 0; i < 10; i++)
        {
            cactusX = x + rand.nextInt(8) - rand.nextInt(8);
            cactusBaseY = y + rand.nextInt(4) - rand.nextInt(4);
            cactusZ = z + rand.nextInt(8) - rand.nextInt(8);

            worldMaterial = worldGenregion.getMaterial(cactusX, cactusBaseY, cactusZ,  chunkBeingPopulated);
            if(worldMaterial == null || !worldMaterial.isAir())
            {
            	continue;
            }
            
            // Check foundation
            worldMaterial = worldGenregion.getMaterial(cactusX, cactusBaseY - 1, cactusZ, chunkBeingPopulated);
            if (worldMaterial == null || !sourceBlocks.contains(worldMaterial))
            {
                continue;
            }

            // Check neighbors
            worldMaterial = worldGenregion.getMaterial(cactusX - 1, cactusBaseY, cactusZ, chunkBeingPopulated);
            if (worldMaterial == null || !worldMaterial.isAir())
            {
                continue;
            }
            
            worldMaterial = worldGenregion.getMaterial(cactusX + 1, cactusBaseY, cactusZ, chunkBeingPopulated);
            if (worldMaterial == null || !worldMaterial.isAir())
            {
                continue;
            }
            
            worldMaterial = worldGenregion.getMaterial(cactusX, cactusBaseY, cactusZ - 1, chunkBeingPopulated);
            if (worldMaterial == null || !worldMaterial.isAir())
            {
                continue;
            }
            
            worldMaterial = worldGenregion.getMaterial(cactusX, cactusBaseY, cactusZ + 1, chunkBeingPopulated);
            if (worldMaterial == null || !worldMaterial.isAir())
            {
                continue;
            }

            // Spawn cactus
            int cactusHeight = 1 + rand.nextInt(rand.nextInt(3) + 1);
            for (int dY = 0; dY < cactusHeight; dY++)
            {
            	worldGenregion.setBlock(cactusX, cactusBaseY + dY, cactusZ, material, null, chunkBeingPopulated, false);
            }
        }
    }

    @Override
    public String toString()
    {
        return "Cactus(" + material + "," + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + makeMaterials(sourceBlocks) + ")";
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 79 * hash + super.hashCode();
        hash = 79 * hash + this.minAltitude;
        hash = 79 * hash + this.maxAltitude;
        hash = 79 * hash + (this.sourceBlocks != null ? this.sourceBlocks.hashCode() : 0);
        return hash;
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
        final CactusGen compare = (CactusGen) other;
        return this.minAltitude == compare.minAltitude
               && this.maxAltitude == compare.maxAltitude
               && (this.sourceBlocks == null ? this.sourceBlocks == compare.sourceBlocks
                   : this.sourceBlocks.equals(compare.sourceBlocks));
    }

    @Override
    public int getPriority()
    {
        return -35;
    }
}
