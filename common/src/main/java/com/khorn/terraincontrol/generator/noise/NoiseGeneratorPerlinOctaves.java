package com.khorn.terraincontrol.generator.noise;

import com.khorn.terraincontrol.util.helpers.MathHelper;

import java.util.Random;

public class NoiseGeneratorPerlinOctaves
{

    private NoiseGeneratorPerlin[] a;
    private int b;

    public NoiseGeneratorPerlinOctaves(Random random, int i)
    {
        this.b = i;
        this.a = new NoiseGeneratorPerlin[i];

        for (int j = 0; j < i; ++j)
        {
            this.a[j] = new NoiseGeneratorPerlin(random);
        }
    }

    public double[] Noise3D(double[] doubleArray, int xOffset, int yOffset, int zOffset, int xSize, int ySize, int zSize, double xScale, double yScale, double zScale)
    {
        if (doubleArray == null)
        {
            doubleArray = new double[xSize * ySize * zSize];
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
            double d4 = (double) xOffset * d3 * xScale;
            double d5 = (double) yOffset * d3 * yScale;
            double d6 = (double) zOffset * d3 * zScale;
            long i2 = MathHelper.floor_double_long(d4);
            long j2 = MathHelper.floor_double_long(d6);

            d4 -= (double) i2;
            d6 -= (double) j2;
            i2 %= 16777216L;
            j2 %= 16777216L;
            d4 += (double) i2;
            d6 += (double) j2;
            this.a[l1].populateNoiseArray3D(doubleArray, d4, d5, d6, xSize, ySize, zSize, xScale * d3, yScale * d3, zScale * d3, d3);
            d3 /= 2.0D;
        }

        return doubleArray;
    }


    public double[] Noise2D(double[] doubleArray, int xOffset, int zOffset, int xSize, int zSize, double xScale, double zScale)
    {
       // return this.Noise3D(doubleArray, xOffset, 10, zOffset, xSize, 1, zSize, xScale, 1.0D, zScale);

        if (doubleArray == null)
        {
            doubleArray = new double[xSize * zSize];
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
            double d4 = (double) xOffset * d3 * xScale;
            double d6 = (double) zOffset * d3 * zScale;
            long i2 = MathHelper.floor_double_long(d4);
            long j2 = MathHelper.floor_double_long(d6);

            d4 -= (double) i2;
            d6 -= (double) j2;
            i2 %= 16777216L;
            j2 %= 16777216L;
            d4 += (double) i2;
            d6 += (double) j2;
            this.a[l1].populateNoiseArray2D(doubleArray, d4, d6, xSize, zSize, xScale * d3, zScale * d3, d3);
            d3 /= 2.0D;
        }

        return doubleArray;
    }
}
