package com.Khorn.PTMBukkit;


import net.minecraft.server.BiomeBase;
import net.minecraft.server.NoiseGeneratorOctaves2;
import net.minecraft.server.World;
import net.minecraft.server.WorldChunkManager;

import java.util.Random;

class BiomeManagerPTM extends WorldChunkManager
{

    private Settings localWrk;

    private NoiseGeneratorOctaves2 e;
    private NoiseGeneratorOctaves2 f;
    private NoiseGeneratorOctaves2 g;

    public BiomeManagerPTM(World paramWorld, Settings worker)
    {

        this.localWrk = worker;
        this.e = new NoiseGeneratorOctaves2(new Random(paramWorld.getSeed() * 9871L), 4);
        this.f = new NoiseGeneratorOctaves2(new Random(paramWorld.getSeed() * 39811L), 4);
        this.g = new NoiseGeneratorOctaves2(new Random(paramWorld.getSeed() * 543321L), 2);

    }

    @Override
    public double[] a(double[] paramArrayOfDouble, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        if ((paramArrayOfDouble == null) || (paramArrayOfDouble.length < paramInt3 * paramInt4))
        {
            paramArrayOfDouble = new double[paramInt3 * paramInt4];
        }

        paramArrayOfDouble = this.e.a(paramArrayOfDouble, paramInt1, paramInt2, paramInt3, paramInt4, 0.025000000372529D / this.localWrk.biomeSize, 0.025000000372529D / this.localWrk.biomeSize, 0.25D);
        this.c = this.g.a(this.c, paramInt1, paramInt2, paramInt3, paramInt4, 0.25D / this.localWrk.biomeSize, 0.25D / this.localWrk.biomeSize, 0.5882352941176471D);

        int i = 0;
        for (int j = 0; j < paramInt3; j++)
        {
            for (int k = 0; k < paramInt4; k++)
            {
                double d1 = this.c[i] * 1.1D + 0.5D;

                double d2 = 0.01D;
                double d3 = 1.0D - d2;
                double d4 = (paramArrayOfDouble[i] * 0.15D + 0.7D) * d3 + d1 * d2;
                d4 = 1.0D - (1.0D - d4) * (1.0D - d4);

                if (d4 < this.localWrk.minTemperature)
                    d4 = this.localWrk.minTemperature;
                if (d4 > this.localWrk.maxTemperature)
                {
                    d4 = this.localWrk.maxTemperature;
                }
                paramArrayOfDouble[i] = d4;
                i++;
            }

        }

        return paramArrayOfDouble;
    }

    @Override
    public BiomeBase[] a(BiomeBase[] paramArrayOfBiomeBase, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        if ((paramArrayOfBiomeBase == null) || (paramArrayOfBiomeBase.length < paramInt3 * paramInt4))
        {
            paramArrayOfBiomeBase = new BiomeBase[paramInt3 * paramInt4];
        }

        this.temperature = this.e.a(this.temperature, paramInt1, paramInt2, paramInt3, paramInt3, 0.025000000372529D / this.localWrk.biomeSize, 0.025000000372529D / this.localWrk.biomeSize, 0.25D);
        this.rain = this.f.a(this.rain, paramInt1, paramInt2, paramInt3, paramInt3, 0.0500000007450581D / this.localWrk.biomeSize, 0.0500000007450581D / this.localWrk.biomeSize, 0.3333333333333333D);
        this.c = this.g.a(this.c, paramInt1, paramInt2, paramInt3, paramInt3, 0.25D / this.localWrk.biomeSize, 0.25D / this.localWrk.biomeSize, 0.5882352941176471D);

        int i = 0;
        for (int j = 0; j < paramInt3; j++)
        {
            for (int k = 0; k < paramInt4; k++)
            {
                double d1 = this.c[i] * 1.1D + 0.5D;

                double d2 = 0.01D;
                double d3 = 1.0D - d2;
                double d4 = (this.temperature[i] * 0.15D + 0.7D) * d3 + d1 * d2;
                d2 = 0.002D;
                d3 = 1.0D - d2;
                double d5 = (this.rain[i] * 0.15D + 0.5D) * d3 + d1 * d2;
                d4 = 1.0D - (1.0D - d4) * (1.0D - d4);

                if (d4 < this.localWrk.minTemperature)
                    d4 = this.localWrk.minTemperature;
                if (d5 < this.localWrk.minMoisture)
                    d5 = this.localWrk.minMoisture;
                if (d4 > this.localWrk.maxTemperature)
                    d4 = this.localWrk.maxTemperature;
                if (d5 > this.localWrk.maxMoisture)
                {
                    d5 = this.localWrk.maxMoisture;
                }
                this.temperature[i] = d4;
                this.rain[i] = d5;

                paramArrayOfBiomeBase[(i++)] = BiomeBase.a(d4, d5);
            }

        }

        return paramArrayOfBiomeBase;
    }

}
