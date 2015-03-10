package com.khorn.terraincontrol.generator.noise;

import java.util.Random;

public class NoiseGeneratorOld
{
    private static int[][] grad3 = {{1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0}, {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1}, {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}};

    private int[] permutationTable = new int[512];
    public static final double square3 = Math.sqrt(3.0D);
    public double noiseContributionX;
    public double noiseContributionY;
    public double noiseContributionZ;
    private static final double skewFactor = 0.5D * (square3 - 1.0D);
    private static final double deskewFactor = (3.0D - square3) / 6.0D;


    public NoiseGeneratorOld(Random random)
    {
        this.noiseContributionX = (random.nextDouble() * 256.0D);
        this.noiseContributionY = (random.nextDouble() * 256.0D);
        this.noiseContributionZ = (random.nextDouble() * 256.0D);
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
    public double getYNoise(double x, double z)
    {
        double var11 = 0.5D * (square3 - 1.0D);
        double var13 = (x + z) * var11;
        int var15 = fastFloor(x + var13);
        int var16 = fastFloor(z + var13);
        double var17 = (3.0D - square3) / 6.0D;
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
            var5 = var44 * var44 * dot(grad3[var41], var25, var27);
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
            var7 = var46 * var46 * dot(grad3[var42], var31, var33);
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
            var9 = var48 * var48 * dot(grad3[var43], var35, var37);
        }

        return 70.0D * (var5 + var7 + var9);
    }

    public void a(double[] paramArrayOfDouble, double paramDouble1, double paramDouble2, int paramInt1, int paramInt2, double paramDouble3, double paramDouble4, double paramDouble5)
    {
        int i = 0;
        for (int j = 0; j < paramInt1; j++)
        {
            double d2 = (paramDouble2 + j) * paramDouble4 + this.noiseContributionY;
            for (int k = 0; k < paramInt2; k++)
            {
                double d1 = (paramDouble1 + k) * paramDouble3 + this.noiseContributionX;

                double d3 = (d1 + d2) * skewFactor;
                int m = fastFloor(d1 + d3);
                int n = fastFloor(d2 + d3);
                double d4 = (m + n) * deskewFactor;
                double d5 = m - d4;
                double d6 = n - d4;
                double d7 = d1 - d5;
                double d8 = d2 - d6;
                int i1;
                int i2;
                if (d7 > d8)
                {
                    i1 = 1;
                    i2 = 0;
                } else
                {
                    i1 = 0;
                    i2 = 1;
                }

                double d9 = d7 - i1 + deskewFactor;
                double d10 = d8 - i2 + deskewFactor;
                double d11 = d7 - 1.0D + 2.0D * deskewFactor;
                double d12 = d8 - 1.0D + 2.0D * deskewFactor;

                int i3 = m & 0xFF;
                int i4 = n & 0xFF;
                int i5 = this.permutationTable[(i3 + this.permutationTable[i4])] % 12;
                int i6 = this.permutationTable[(i3 + i1 + this.permutationTable[(i4 + i2)])] % 12;
                int i7 = this.permutationTable[(i3 + 1 + this.permutationTable[(i4 + 1)])] % 12;

                double d13 = 0.5D - d7 * d7 - d8 * d8;
                double d14;
                if (d13 < 0.0D)
                {
                    d14 = 0.0D;
                } else
                {
                    d13 *= d13;
                    d14 = d13 * d13 * dot(grad3[i5], d7, d8);
                }
                double d15 = 0.5D - d9 * d9 - d10 * d10;
                double d16;
                if (d15 < 0.0D)
                {
                    d16 = 0.0D;
                } else
                {
                    d15 *= d15;
                    d16 = d15 * d15 * dot(grad3[i6], d9, d10);
                }
                double d17 = 0.5D - d11 * d11 - d12 * d12;
                double d18;
                if (d17 < 0.0D)
                {
                    d18 = 0.0D;
                } else
                {
                    d17 *= d17;
                    d18 = d17 * d17 * dot(grad3[i7], d11, d12);
                }

                paramArrayOfDouble[(i++)] += 70.0D * (d14 + d16 + d18) * paramDouble5;
            }
        }
    }
}