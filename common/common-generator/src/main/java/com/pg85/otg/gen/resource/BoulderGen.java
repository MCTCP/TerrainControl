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
import com.pg85.otg.util.materials.MaterialSet;

import java.util.List;
import java.util.Random;

/**
 * Generates a mossy rock structure by placing multiple spheres together.
 */
public class BoulderGen extends Resource
{
    private MaterialSet sourceBlocks;
    private int minAltitude;
    private int maxAltitude;

    public BoulderGen(IBiomeConfig config, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
    {
        super(config, args, logger, materialReader);
        assureSize(6, args);

        material = materialReader.readMaterial(args.get(0));
        frequency = readInt(args.get(1), 1, 5000);
        rarity = readRarity(args.get(2));
        minAltitude = readInt(args.get(3), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
        maxAltitude = readInt(args.get(4), minAltitude, Constants.WORLD_HEIGHT - 1);
        sourceBlocks = readMaterials(args, 5, materialReader);
    }

    @Override
    public void spawn(IWorldGenRegion worldGenregion, Random random, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
    {
    	// Make sure we stay within population bounds, anything outside won't be spawned (unless it's in an existing chunk).
    	
    	int y = worldGenregion.getHighestBlockAboveYAt(x, z, chunkBeingPopulated);
        if (y < this.minAltitude || y > this.maxAltitude)
        {
            return;
        }
        
        //parseMaterials(worldGenregion.getWorldConfig(), material, sourceBlocks);

        while (y > 3)
        {
            LocalMaterialData material = worldGenregion.getMaterial(x, y - 1, z, chunkBeingPopulated);
            if (sourceBlocks.contains(material))
            {
                break;
            }
            y--;
        }
        if (y <= 3)
        {
            return;
        }

        int i = 0;
        int j = 0;
        while ((i >= 0) && (j < 3))
        {
            int k = i + random.nextInt(2);
            int m = i + random.nextInt(2);
            int n = i + random.nextInt(2);
            float f1 = (k + m + n) * 0.333F + 0.5F;
            for (int i1 = x - k; i1 <= x + k; i1++)
            {
                for (int i2 = z - n; i2 <= z + n; i2++)
                {
                    for (int i3 = y - m; i3 <= y + m; i3++)
                    {
                        float f2 = i1 - x;
                        float f3 = i2 - z;
                        float f4 = i3 - y;
                        if (f2 * f2 + f3 * f3 + f4 * f4 <= f1 * f1)
                        {
                        	worldGenregion.setBlock(i1, i3, i2, this.material, null, chunkBeingPopulated, true);
                        }
                    }
                }
            }
            x += random.nextInt(2 + i * 2) - 1 - i;
            z += random.nextInt(2 + i * 2) - 1 - i;
            y -= random.nextInt(2);
            j++;
        }
    }

    @Override
    public String toString()
    {
        return "Boulder(" + material + "," + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + makeMaterials(sourceBlocks) + ")";
    }

    @Override
    public int getPriority()
    {
        return -22;
    }
}
