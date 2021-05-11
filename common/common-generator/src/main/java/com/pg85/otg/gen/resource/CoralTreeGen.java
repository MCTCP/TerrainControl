package com.pg85.otg.gen.resource;

import com.pg85.otg.config.biome.Resource;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.gen.resource.util.CoralHelper;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.OTGDirection;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;

import java.util.*;

public class CoralTreeGen extends Resource
{
    private static final OTGDirection[] HORIZONTAL = {OTGDirection.NORTH, OTGDirection.EAST, OTGDirection.SOUTH, OTGDirection.WEST};

    public CoralTreeGen(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
    {
        super(biomeConfig, args, logger, materialReader);
        this.frequency = readInt(args.get(0), 1, 500);
        this.rarity = readRarity(args.get(1));
    }

    @Override
    public String toString()
    {
        return "CoralTree(" + this.frequency + "," + this.rarity + ")";
    }

    @Override
    public void spawn(IWorldGenRegion world, Random random, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
    {
        int y = world.getBlockAboveSolidHeight(x, z, chunkBeingPopulated);
        LocalMaterialData coral = CoralHelper.getRandomCoralBlock(random);

        int height = random.nextInt(3) + 1;
        for (int i = 0; i < height; i++)
        {
            // Return if we don't have enough space to place the rest of the tree
            if (!CoralHelper.placeCoralBlock(world, random, chunkBeingPopulated, x, y + i, z, coral))
            {
                return;
            }
        }

        y += height;

        // 2-4 branch, with a randomized index
        int dirEnd = random.nextInt(3) + 2;
        List<OTGDirection> directions = Arrays.asList(HORIZONTAL);
        Collections.shuffle(directions, random);

        // Iterate 2-4 directions
        for (OTGDirection direction : directions.subList(0, dirEnd))
        {
            // Initial branch out
            int dx = x + direction.getX();
            int dy = y;
            int dz = z + direction.getZ();

            // Branch size
            int count = random.nextInt(5) + 2;
            int placedIndex = 0;

            for (int i = 0; i < count && CoralHelper.placeCoralBlock(world, random, chunkBeingPopulated, dx, dy, dz, coral); i++)
            {
                placedIndex++;
                dy++;

                // Branch out if we're either the first index or if we've placed 2 or more with a 1/4 chance
                if (i == 0 || placedIndex >= 2 && random.nextFloat() < 0.25F) {
                    dx += direction.getX();
                    dz += direction.getZ();

                    placedIndex = 0;
                }
            }
        }
    }
}
