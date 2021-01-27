package com.pg85.otg.gen.resource;

import com.pg85.otg.config.biome.Resource;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

import java.util.List;
import java.util.Random;

public class VinesGen extends Resource
{
    private static final LocalMaterialData[] FROM_DIRECTION =
    {
            LocalMaterials.VINE_SOUTH, LocalMaterials.VINE_NORTH, LocalMaterials.VINE_EAST, LocalMaterials.VINE_WEST
    };

    private final int maxAltitude;
    private final int minAltitude;

    public VinesGen(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
    {
    	super(biomeConfig, args, logger, materialReader);
        material = LocalMaterials.VINE;

        assureSize(4, args);
        frequency = readInt(args.get(0), 1, 100);
        rarity = readRarity(args.get(1));
        minAltitude = readInt(args.get(2), Constants.WORLD_DEPTH,
    		Constants.WORLD_HEIGHT - 1);
        maxAltitude = readInt(args.get(3), minAltitude,
    		Constants.WORLD_HEIGHT - 1);
    }
    
    private boolean canPlace(IWorldGenRegion worldGenRegion, int x, int y, int z, int direction, ChunkCoordinate chunkBeingPopulated)
    {
        LocalMaterialData sourceBlock;
        switch (direction)
        {
            default:
                return false;
            case 1:
                sourceBlock = worldGenRegion.getMaterial(x, y + 1, z, chunkBeingPopulated);
                break;
            case 2:
                sourceBlock = worldGenRegion.getMaterial(x, y, z + 1, chunkBeingPopulated);
                break;
            case 3:
                sourceBlock = worldGenRegion.getMaterial(x, y, z - 1, chunkBeingPopulated);
                break;
            case 5:
                sourceBlock = worldGenRegion.getMaterial(x - 1, y, z, chunkBeingPopulated);
                break;
            case 4:
                sourceBlock = worldGenRegion.getMaterial(x + 1, y, z, chunkBeingPopulated);
                break;
        }
        return sourceBlock != null && sourceBlock.isSolid();
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
        final VinesGen compare = (VinesGen) other;
        return this.maxAltitude == compare.maxAltitude
               && this.minAltitude == compare.minAltitude;
    }

    @Override
    public int getPriority()
    {
        return -50;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 17 * hash + super.hashCode();
        hash = 17 * hash + this.minAltitude;
        hash = 17 * hash + this.maxAltitude;
        return hash;
    }

    @Override
    public String toString()
    {
        return "Vines(" + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + ")";
    }

    @Override
    public void spawn(IWorldGenRegion worldGenRegion, Random rand, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
    {
    	// Make sure we stay within population bounds, anything outside won't be spawned (unless it's in an existing chunk).
        int _x = x;
        int _z = z;
        int y = minAltitude;

        LocalMaterialData worldMaterial;
        
        while (y <= maxAltitude)
        {
        	worldMaterial = worldGenRegion.getMaterial(_x, y, _z, chunkBeingPopulated);
            if (worldMaterial != null && worldMaterial.isAir())
            {
                // TODO: Refactor to enum
                for (int direction = 2; direction <= 5; direction++)
                {
                    if (canPlace(worldGenRegion, _x, y, _z, direction, chunkBeingPopulated))
                    {
                    	worldGenRegion.setBlock(_x, y, _z, FROM_DIRECTION[direction - 2], null, chunkBeingPopulated, false);
                        break;
                    }
                }
            } else {
                _x = x + rand.nextInt(4) - rand.nextInt(4);
                _z = z + rand.nextInt(4) - rand.nextInt(4);
            }
            y++;
        }
    }
}
