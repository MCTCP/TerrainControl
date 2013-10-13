package com.khorn.terraincontrol.generator.noise;

import java.util.Random;

public class NoiseGeneratorOld
{
    private static int[][] d = {{1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0}, {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1}, {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}};

    private int[] e = new int[512];
    public double a;
    public double b;
    public double c;
    private static final double f = 0.5D * (Math.sqrt(3.0D) - 1.0D);
    private static final double g = (3.0D - Math.sqrt(3.0D)) / 6.0D;


    public NoiseGeneratorOld(Random paramRandom)
    {
        this.a = (paramRandom.nextDouble() * 256.0D);
        this.b = (paramRandom.nextDouble() * 256.0D);
        this.c = (paramRandom.nextDouble() * 256.0D);
        for (int i = 0; i < 256; i++)
        {
            this.e[i] = i;
        }

        for (int i = 0; i < 256; i++)
        {
            int j = paramRandom.nextInt(256 - i) + i;
            int k = this.e[i];
            this.e[i] = this.e[j];
            this.e[j] = k;

            this.e[(i + 256)] = this.e[i];
        }
    }

    private static int a(double paramDouble)
    {
        return paramDouble > 0.0D ? (int) paramDouble : (int) paramDouble - 1;
    }

    private static double a(int[] paramArrayOfInt, double paramDouble1, double paramDouble2)
    {
        return paramArrayOfInt[0] * paramDouble1 + paramArrayOfInt[1] * paramDouble2;
    }

    public void a(double[] paramArrayOfDouble, double paramDouble1, double paramDouble2, int paramInt1, int paramInt2, double paramDouble3, double paramDouble4, double paramDouble5)
    {
        int i = 0;
        for (int j = 0; j < paramInt1; j++)
        {
            double d2 = (paramDouble2 + j) * paramDouble4 + this.b;
            for (int k = 0; k < paramInt2; k++)
            {
                double d1 = (paramDouble1 + k) * paramDouble3 + this.a;

                double d3 = (d1 + d2) * f;
                int m = a(d1 + d3);
                int n = a(d2 + d3);
                double d4 = (m + n) * g;
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

                double d9 = d7 - i1 + g;
                double d10 = d8 - i2 + g;
                double d11 = d7 - 1.0D + 2.0D * g;
                double d12 = d8 - 1.0D + 2.0D * g;

                int i3 = m & 0xFF;
                int i4 = n & 0xFF;
                int i5 = this.e[(i3 + this.e[i4])] % 12;
                int i6 = this.e[(i3 + i1 + this.e[(i4 + i2)])] % 12;
                int i7 = this.e[(i3 + 1 + this.e[(i4 + 1)])] % 12;

                double d13 = 0.5D - d7 * d7 - d8 * d8;
                double d14;
                if (d13 < 0.0D)
                {
                    d14 = 0.0D;
                } else
                {
                    d13 *= d13;
                    d14 = d13 * d13 * a(d[i5], d7, d8);
                }
                double d15 = 0.5D - d9 * d9 - d10 * d10;
                double d16;
                if (d15 < 0.0D)
                {
                    d16 = 0.0D;
                } else
                {
                    d15 *= d15;
                    d16 = d15 * d15 * a(d[i6], d9, d10);
                }
                double d17 = 0.5D - d11 * d11 - d12 * d12;
                double d18;
                if (d17 < 0.0D)
                {
                    d18 = 0.0D;
                } else
                {
                    d17 *= d17;
                    d18 = d17 * d17 * a(d[i7], d11, d12);
                }

                paramArrayOfDouble[(i++)] += 70.0D * (d14 + d16 + d18) * paramDouble5;
            }
        }
    }
}