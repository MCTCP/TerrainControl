package com.khorn.terraincontrol.generator.noise;

import java.util.Random;

public class NoiseGeneratorOldOctaves
{
    private NoiseGeneratorOld[] a;
    private int b;

    public NoiseGeneratorOldOctaves(Random paramRandom, int paramInt)
    {
        this.b = paramInt;
        this.a = new NoiseGeneratorOld[paramInt];
        for (int i = 0; i < paramInt; i++)
            this.a[i] = new NoiseGeneratorOld(paramRandom);
    }

    public double[] a(double[] paramArrayOfDouble, double paramDouble1, double paramDouble2, int paramInt1, int paramInt2, double paramDouble3, double paramDouble4, double paramDouble5)
    {
        return a(paramArrayOfDouble, paramDouble1, paramDouble2, paramInt1, paramInt2, paramDouble3, paramDouble4, paramDouble5, 0.5D);
    }

    public double[] a(double[] paramArrayOfDouble, double paramDouble1, double paramDouble2, int paramInt1, int paramInt2, double paramDouble3, double paramDouble4, double paramDouble5, double paramDouble6)
    {
        paramDouble3 /= 1.5D;
        paramDouble4 /= 1.5D;

        if ((paramArrayOfDouble == null) || (paramArrayOfDouble.length < paramInt1 * paramInt2))
            paramArrayOfDouble = new double[paramInt1 * paramInt2];
        else
        {
            for (int i = 0; i < paramArrayOfDouble.length; i++)
            {
                paramArrayOfDouble[i] = 0.0D;
            }
        }
        double d1 = 1.0D;
        double d2 = 1.0D;
        for (int j = 0; j < this.b; j++)
        {
            this.a[j].a(paramArrayOfDouble, paramDouble1, paramDouble2, paramInt1, paramInt2, paramDouble3 * d2, paramDouble4 * d2, 0.55D / d1);
            d2 *= paramDouble5;
            d1 *= paramDouble6;
        }

        return paramArrayOfDouble;
    }
}