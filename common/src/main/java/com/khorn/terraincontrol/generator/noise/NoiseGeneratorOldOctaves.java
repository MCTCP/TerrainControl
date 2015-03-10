package com.khorn.terraincontrol.generator.noise;

import java.util.Random;

public class NoiseGeneratorOldOctaves
{
    private NoiseGeneratorOld[] noiseArray;
    private int numOctaves;

    public NoiseGeneratorOldOctaves(Random random, int numOctaves)
    {
        this.numOctaves = numOctaves;
        this.noiseArray = new NoiseGeneratorOld[numOctaves];
        for (int i = 0; i < numOctaves; i++)
            this.noiseArray[i] = new NoiseGeneratorOld(random);
    }

    /**
     * Convenience Method for getting simple noise at a specific x and z, composites noise from
     * {@code numOctaves} octaves.
     * @param x The x coordinate
     * @param z The z coordinate
     * @return the noise value of y
     */
    public double getYNoise(double x, double z)
    {
        double resultingY = 0.0D;
        double octaveAmplitude = 1.0D;

        for (int var9 = 0; var9 < this.numOctaves; ++var9)
        {
            resultingY += this.noiseArray[var9].getYNoise(x * octaveAmplitude, z * octaveAmplitude) / octaveAmplitude;
            octaveAmplitude /= 2.0D;
        }

        return resultingY;
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
        for (int j = 0; j < this.numOctaves; j++)
        {
            this.noiseArray[j].a(paramArrayOfDouble, paramDouble1, paramDouble2, paramInt1, paramInt2, paramDouble3 * d2, paramDouble4 * d2, 0.55D / d1);
            d2 *= paramDouble5;
            d1 *= paramDouble6;
        }

        return paramArrayOfDouble;
    }
}