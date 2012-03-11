package com.Khorn.TerrainControl.Util;

import java.util.Random;

public class NoiseGeneratorOctaves
{

    private NoiseGeneratorPerlin[] a;
    private int b;

    public NoiseGeneratorOctaves(Random random, int i)
    {
        this.b = i;
        this.a = new NoiseGeneratorPerlin[i];

        for (int j = 0; j < i; ++j)
        {
            this.a[j] = new NoiseGeneratorPerlin(random);
        }
    }

    public double[] Noise3D(double[] doubleArray, int i, int j, int k, int l, int i1, int j1, double d0, double d1, double d2)
    {
        if (doubleArray == null)
        {
            doubleArray = new double[l * i1 * j1];
        } else
        {
            for (int k1 = 0; k1 < doubleArray.length; ++k1)
            {
                doubleArray[k1] = 0.0D;
            }
        }

        double d3 = 1.0D;

        for (int l1 = 0; l1 < this.b; ++l1)
        {
            double d4 = (double) i * d3 * d0;
            double d5 = (double) j * d3 * d1;
            double d6 = (double) k * d3 * d2;
            long i2 = MathHelper.floor_double_long(d4);
            long j2 = MathHelper.floor_double_long(d6);

            d4 -= (double) i2;
            d6 -= (double) j2;
            i2 %= 16777216L;
            j2 %= 16777216L;
            d4 += (double) i2;
            d6 += (double) j2;
            this.a[l1].a(doubleArray, d4, d5, d6, l, i1, j1, d0 * d3, d1 * d3, d2 * d3, d3);
            d3 /= 2.0D;
        }

        return doubleArray;
    }


    public double[] Noise2D(double[] doubleArray, int i, int j, int k, int l, double d0, double d1)
    {
        return this.Noise3D(doubleArray, i, 10, j, k, 1, l, d0, 1.0D, d1);
    }
}
