package com.pg85.otg.generator.noise;

import java.util.Random;

public class NoiseGeneratorSurfacePatch
{
    private static final double Square3 = Math.sqrt(3.0D);
    private static int[][] Grad3 = {{1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0}, {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1}, {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}};
    
    private int[] permutationTable = new int[512];

    NoiseGeneratorSurfacePatch(Random random)
    {
        for (int i = 0; i < 256; this.permutationTable[i] = i++)
        {
            ;
        }

        for (int i = 0; i < 256; ++i)
        {
            int j = random.nextInt(256 - i) + i;
            int k = this.permutationTable[i];
            this.permutationTable[i] = this.permutationTable[j];
            this.permutationTable[j] = k;
            this.permutationTable[(i + 256)] = this.permutationTable[i];
        }
    }

    private static int fastFloor(double x)
    {
        return x > 0.0D ? (int) x : (int) x - 1;
    }

    private static double dot(int[] matrix, double x, double z)
    {
        return (double) matrix[0] * x + (double) matrix[1] * z;
    }

    /**
     * Convenience Method for getting simple noise at a specific x and z
     * @param x The x coordinate
     * @param z The z coordinate
     * @return the noise value of y
     */
    double getYNoise(double x, double z)
    {
        double var11 = 0.5D * (Square3 - 1.0D);
        double var13 = (x + z) * var11;
        int var15 = fastFloor(x + var13);
        int var16 = fastFloor(z + var13);
        double var17 = (3.0D - Square3) / 6.0D;
        double var19 = (double)(var15 + var16) * var17;
        double var21 = (double)var15 - var19;
        double var23 = (double)var16 - var19;
        double var25 = x - var21;
        double var27 = z - var23;
        byte var29;
        byte var30;

        if (var25 > var27)
        {
            var29 = 1;
            var30 = 0;
        }
        else
        {
            var29 = 0;
            var30 = 1;
        }

        double var31 = var25 - (double)var29 + var17;
        double var33 = var27 - (double)var30 + var17;
        double var35 = var25 - 1.0D + 2.0D * var17;
        double var37 = var27 - 1.0D + 2.0D * var17;
        int var39 = var15 & 255;
        int var40 = var16 & 255;
        int var41 = this.permutationTable[var39 + this.permutationTable[var40]] % 12;
        int var42 = this.permutationTable[var39 + var29 + this.permutationTable[var40 + var30]] % 12;
        int var43 = this.permutationTable[var39 + 1 + this.permutationTable[var40 + 1]] % 12;
        double var44 = 0.5D - var25 * var25 - var27 * var27;
        double var5;

        if (var44 < 0.0D)
        {
            var5 = 0.0D;
        }
        else
        {
            var44 *= var44;
            var5 = var44 * var44 * dot(Grad3[var41], var25, var27);
        }

        double var46 = 0.5D - var31 * var31 - var33 * var33;
        double var7;

        if (var46 < 0.0D)
        {
            var7 = 0.0D;
        }
        else
        {
            var46 *= var46;
            var7 = var46 * var46 * dot(Grad3[var42], var31, var33);
        }

        double var48 = 0.5D - var35 * var35 - var37 * var37;
        double var9;

        if (var48 < 0.0D)
        {
            var9 = 0.0D;
        }
        else
        {
            var48 *= var48;
            var9 = var48 * var48 * dot(Grad3[var43], var35, var37);
        }

        return 70.0D * (var5 + var7 + var9);
    }
}