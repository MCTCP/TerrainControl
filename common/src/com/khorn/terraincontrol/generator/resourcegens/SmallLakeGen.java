package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.Resource;

import java.util.Random;

public class SmallLakeGen extends ResourceGenBase
{

    private final boolean[] BooleanBuffer = new boolean[2048];


    @Override
    protected void SpawnResource(LocalWorld world, Random rand, Resource res, int x, int z)
    {
        x -= 8;
        z -= 8;

        int y = rand.nextInt(res.MaxAltitude - res.MinAltitude) + res.MinAltitude;

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
                        boolean flag = (!BooleanBuffer[((j * 16 + i1) * 8 + i2)]) && (((j < 15) && (BooleanBuffer[(((j + 1) * 16 + i1) * 8 + i2)])) || ((j > 0) && (BooleanBuffer[(((j - 1) * 16 + i1) * 8 + i2)])) || ((i1 < 15) && (BooleanBuffer[((j * 16 + (i1 + 1)) * 8 + i2)])) || ((i1 > 0) && (BooleanBuffer[((j * 16 + (i1 - 1)) * 8 + i2)])) || ((i2 < 7) && (BooleanBuffer[((j * 16 + i1) * 8 + (i2 + 1))])) || ((i2 > 0) && (BooleanBuffer[((j * 16 + i1) * 8 + (i2 - 1))])));

                        if (flag)
                        {
                            DefaultMaterial localMaterial = world.getMaterial(x + j, y + i2, z + i1);
                            if ((i2 >= 4) && (localMaterial.isLiquid()))
                                return;
                            if ((i2 < 4) && (!localMaterial.isSolid()) && (world.getTypeId(x + j, y + i2, z + i1) != res.BlockId))
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
                            world.setBlock(x + j, y + i2, z + i1, res.BlockId, res.BlockData);
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
            /*
           for (j = 0; j < 16; j++) {
               for (i1 = 0; i1 < 16; i1++) {
                   for (i2 = 4; i2 < 8; i2++) {
                       if ((BooleanBuffer[((j * 16 + i1) * 8 + i2)] == 0) ||
                               (paramWorld.getTypeId(x + j, y + i2 - 1, z + i1) != Block.DIRT.id) || (paramWorld.a(EnumSkyBlock.SKY, x + j, y + i2, z + i1) <= 0)) continue;
                       BiomeBase localBiomeBase = paramWorld.getBiome(x + j, z + i1);
                       if (localBiomeBase.A == Block.MYCEL.id) paramWorld.setRawTypeId(x + j, y + i2 - 1, z + i1, Block.MYCEL.id); else {
                           paramWorld.setRawTypeId(x + j, y + i2 - 1, z + i1, Block.GRASS.id);
                       }
                   }
               }

           }

           if (Block.byId[this.a].material == Material.LAVA) {
               for (j = 0; j < 16; j++) {
                   for (i1 = 0; i1 < 16; i1++) {
                       for (i2 = 0; i2 < 8; i2++) {
                           int i4 = (BooleanBuffer[((j * 16 + i1) * 8 + i2)] == 0) && (((j < 15) && (BooleanBuffer[(((j + 1) * 16 + i1) * 8 + i2)] != 0)) || ((j > 0) && (BooleanBuffer[(((j - 1) * 16 + i1) * 8 + i2)] != 0)) || ((i1 < 15) && (BooleanBuffer[((j * 16 + (i1 + 1)) * 8 + i2)] != 0)) || ((i1 > 0) && (BooleanBuffer[((j * 16 + (i1 - 1)) * 8 + i2)] != 0)) || ((i2 < 7) && (BooleanBuffer[((j * 16 + i1) * 8 + (i2 + 1))] != 0)) || ((i2 > 0) && (BooleanBuffer[((j * 16 + i1) * 8 + (i2 - 1))] != 0))) ? 1 : 0;

                           if ((i4 == 0) ||
                                   ((i2 >= 4) && (rand.nextInt(2) == 0)) || (!paramWorld.getMaterial(x + j, y + i2, z + i1).isBuildable())) continue;
                           paramWorld.setRawTypeId(x + j, y + i2, z + i1, Block.STONE.id);
                       }
                   }

               }

           }

           if (Block.byId[this.a].material == Material.WATER) {
               for (j = 0; j < 16; j++) {
                   for (i1 = 0; i1 < 16; i1++) {
                       i2 = 4;
                       if (!paramWorld.s(x + j, y + i2, z + i1)) continue; paramWorld.setRawTypeId(x + j, y + i2, z + i1, Block.ICE.id);
                   }
               }
           } */

        }
    }

    @Override
    protected boolean ReadString(Resource res, String[] Props, BiomeConfig biomeConfig) throws NumberFormatException
    {
        if (Props[0].contains("."))
        {
            String[] block = Props[0].split("\\.");
            res.BlockId = CheckBlock(block[0]);
            res.BlockData = CheckValue(block[1], 0, 16);
        } else
        {
            res.BlockId = CheckBlock(Props[0]);
        }

        res.Frequency = CheckValue(Props[1], 1, 100);
        res.Rarity = CheckValue(Props[2], 0, 100);
        res.MinAltitude = CheckValue(Props[3], 0, biomeConfig.worldConfig.WorldHeight);
        res.MaxAltitude = CheckValue(Props[4], 0, biomeConfig.worldConfig.WorldHeight, res.MinAltitude);
        return true;
    }

    @Override
    protected String WriteString(Resource res, String blockSources)
    {
        String blockId = res.BlockIdToName(res.BlockId);
        if (res.BlockData > 0)
        {
            blockId += "." + res.BlockData;
        }
        return blockId + "," + res.Frequency + "," + res.Rarity + "," + res.MinAltitude + "," + res.MaxAltitude;
    }
}
