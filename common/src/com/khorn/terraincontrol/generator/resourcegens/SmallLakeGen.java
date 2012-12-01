package com.khorn.terraincontrol.generator.resourcegens;

import java.util.List;
import java.util.Random;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.Resource;
import com.khorn.terraincontrol.exception.InvalidResourceException;

public class SmallLakeGen extends Resource
{

    private final boolean[] BooleanBuffer = new boolean[2048];
    private int blockId;
    private int blockData;
    private int minAltitude;
    private int maxAltitude;

    @Override
    public void spawn(LocalWorld world, Random rand, int x, int z)
    {
        x -= 8;
        z -= 8;

        int y = rand.nextInt(maxAltitude - minAltitude) + minAltitude;

        // Search any free space
        while ((y > 5) && (world.isEmpty(x, y, z)))
            y--;

        if (y <= 4)
            return;

        // y = floor
        y -= 4;

        synchronized (BooleanBuffer)
        {
            boolean[] BooleanBuffer = new boolean[2048];
            int i = rand.nextInt(4) + 4;
            for (int j = 0; j < i; j++)
            {
                double d1 = rand.nextDouble() * 6.0D + 3.0D;
                double d2 = rand.nextDouble() * 4.0D + 2.0D;
                double d3 = rand.nextDouble() * 6.0D + 3.0D;

                double d4 = rand.nextDouble() * (16.0D - d1 - 2.0D) + 1.0D + d1 / 2.0D;
                double d5 = rand.nextDouble() * (8.0D - d2 - 4.0D) + 2.0D + d2 / 2.0D;
                double d6 = rand.nextDouble() * (16.0D - d3 - 2.0D) + 1.0D + d3 / 2.0D;

                for (int k = 1; k < 15; k++)
                    for (int m = 1; m < 15; m++)
                        for (int n = 1; n < 7; n++)
                        {
                            double d7 = (k - d4) / (d1 / 2.0D);
                            double d8 = (n - d5) / (d2 / 2.0D);
                            double d9 = (m - d6) / (d3 / 2.0D);
                            double d10 = d7 * d7 + d8 * d8 + d9 * d9;
                            if (d10 >= 1.0D)
                                continue;
                            BooleanBuffer[((k * 16 + m) * 8 + n)] = true;
                        }
            }
            int i1;
            int i2;
            for (int j = 0; j < 16; j++)
            {
                for (i1 = 0; i1 < 16; i1++)
                {
                    for (i2 = 0; i2 < 8; i2++)
                    {
                        boolean flag = (!BooleanBuffer[((j * 16 + i1) * 8 + i2)])
                                && (((j < 15) && (BooleanBuffer[(((j + 1) * 16 + i1) * 8 + i2)])) || ((j > 0) && (BooleanBuffer[(((j - 1) * 16 + i1) * 8 + i2)]))
                                        || ((i1 < 15) && (BooleanBuffer[((j * 16 + (i1 + 1)) * 8 + i2)])) || ((i1 > 0) && (BooleanBuffer[((j * 16 + (i1 - 1)) * 8 + i2)]))
                                        || ((i2 < 7) && (BooleanBuffer[((j * 16 + i1) * 8 + (i2 + 1))])) || ((i2 > 0) && (BooleanBuffer[((j * 16 + i1) * 8 + (i2 - 1))])));

                        if (flag)
                        {
                            DefaultMaterial localMaterial = world.getMaterial(x + j, y + i2, z + i1);
                            if ((i2 >= 4) && (localMaterial.isLiquid()))
                                return;
                            if ((i2 < 4) && (!localMaterial.isSolid()) && (world.getTypeId(x + j, y + i2, z + i1) != blockId))
                                return;
                        }
                    }
                }

            }

            for (int j = 0; j < 16; j++)
            {
                for (i1 = 0; i1 < 16; i1++)
                {
                    for (i2 = 0; i2 < 4; i2++)
                    {
                        if (BooleanBuffer[((j * 16 + i1) * 8 + i2)])
                        {
                            world.setBlock(x + j, y + i2, z + i1, blockId, blockData);
                            BooleanBuffer[((j * 16 + i1) * 8 + i2)] = false;
                        }
                    }
                    for (i2 = 4; i2 < 8; i2++)
                    {
                        if (BooleanBuffer[((j * 16 + i1) * 8 + i2)])
                        {
                            world.setBlock(x + j, y + i2, z + i1, 0, 0);
                            BooleanBuffer[((j * 16 + i1) * 8 + i2)] = false;
                        }
                    }
                }
            }

        }
    }

    @Override
    public void load(List<String> args) throws InvalidResourceException
    {
        assureSize(5, args);
        blockId = getBlockId(args.get(0));
        blockData = getBlockData(args.get(0));
        frequency = getInt(args.get(1), 1, 100);
        rarity = getInt(args.get(2), 1, 100);
        minAltitude = getInt(args.get(3), TerrainControl.worldDepth, TerrainControl.worldHeight);
        maxAltitude = getInt(args.get(4), minAltitude + 1, TerrainControl.worldHeight);
    }

    @Override
    public ResourceType getType()
    {
        return ResourceType.biomeConfigResource;
    }

    @Override
    public String makeString()
    {
        return "SmallLake(" + makeMaterial(blockId, blockData) + "," + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + ")";
    }
}
