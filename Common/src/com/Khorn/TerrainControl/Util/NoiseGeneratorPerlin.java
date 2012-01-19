package com.Khorn.TerrainControl.Util;

import java.util.Random;

public class NoiseGeneratorPerlin
{
    private int[] d = new int[512];
    public double a;
    public double b;
    public double c;

    public NoiseGeneratorPerlin()
    {
        this(new Random());
    }

    public NoiseGeneratorPerlin(Random paramRandom)
    {
        this.a = (paramRandom.nextDouble() * 256.0D);
        this.b = (paramRandom.nextDouble() * 256.0D);
        this.c = (paramRandom.nextDouble() * 256.0D);
        for (int i = 0; i < 256; i++)
        {
            this.d[i] = i;
        }

        for (int i = 0; i < 256; i++)
        {
            int j = paramRandom.nextInt(256 - i) + i;
            int k = this.d[i];
            this.d[i] = this.d[j];
            this.d[j] = k;

            this.d[(i + 256)] = this.d[i];
        }
    }

    public final double a(double paramDouble1, double paramDouble2, double paramDouble3)
    {
        return paramDouble2 + paramDouble1 * (paramDouble3 - paramDouble2);
    }

    public final double a(int paramInt, double paramDouble1, double paramDouble2)
    {
        int i = paramInt & 0xF;

        double d1 = (1 - ((i & 0x8) >> 3)) * paramDouble1;
        double d2 = (i == 12) || (i == 14) ? paramDouble1 : i < 4 ? 0.0D : paramDouble2;

        return ((i & 0x1) == 0 ? d1 : -d1) + ((i & 0x2) == 0 ? d2 : -d2);
    }

    public final double a(int paramInt, double paramDouble1, double paramDouble2, double paramDouble3)
    {
        int i = paramInt & 0xF;

        double d1 = i < 8 ? paramDouble1 : paramDouble2;
        double d2 = (i == 12) || (i == 14) ? paramDouble1 : i < 4 ? paramDouble2 : paramDouble3;

        return ((i & 0x1) == 0 ? d1 : -d1) + ((i & 0x2) == 0 ? d2 : -d2);
    }

    public void a(double[] paramArrayOfDouble, double paramDouble1, double paramDouble2, double paramDouble3, int paramInt1, int paramInt2, int paramInt3, double paramDouble4, double paramDouble5, double paramDouble6, double paramDouble7)
    {
        double d6;
        int i5;
        int i6;
        int i;
        int m;
        double d7;
        int n;
        if (paramInt2 == 1)
        {
            n = 0;
            double d1;
            double d2;
            double d3 = 1.0D / paramDouble7;
            for (int i1 = 0; i1 < paramInt1; i1++)
            {
                double d4 = paramDouble1 + i1 * paramDouble4 + this.a;
                int i2 = (int) d4;
                if (d4 < i2)
                    i2--;
                int i3 = i2 & 0xFF;
                d4 -= i2;
                double d5 = d4 * d4 * d4 * (d4 * (d4 * 6.0D - 15.0D) + 10.0D);

                for (int i4 = 0; i4 < paramInt3; i4++)
                {
                    d6 = paramDouble3 + i4 * paramDouble6 + this.c;
                    i5 = (int) d6;
                    if (d6 < i5)
                        i5--;
                    i6 = i5 & 0xFF;
                    d6 -= i5;
                    d7 = d6 * d6 * d6 * (d6 * (d6 * 6.0D - 15.0D) + 10.0D);

                    i = this.d[i3];
                    int j = this.d[i] + i6;
                    int k = this.d[(i3 + 1)];
                    m = this.d[k] + i6;
                    d1 = a(d5, a(this.d[j], d4, d6), a(this.d[m], d4 - 1.0D, 0.0D, d6));
                    d2 = a(d5, a(this.d[(j + 1)], d4, 0.0D, d6 - 1.0D), a(this.d[(m + 1)], d4 - 1.0D, 0.0D, d6 - 1.0D));

                    double d8 = a(d7, d1, d2);

                    paramArrayOfDouble[(n++)] += d8 * d3;
                }
            }
            return;
        }
        i = 0;
        double d9 = 1.0D / paramDouble7;
        m = -1;
        int i7;
        int i8;
        int i9;
        int i10;
        int i11;
        double d10 = 0.0D;
        double d4 = 0.0D;
        double d11 = 0.0D;
        double d5 = 0.0D;

        for (int i4 = 0; i4 < paramInt1; i4++)
        {
            d6 = paramDouble1 + i4 * paramDouble4 + this.a;
            i5 = (int) d6;
            if (d6 < i5)
                i5--;
            i6 = i5 & 0xFF;
            d6 -= i5;
            d7 = d6 * d6 * d6 * (d6 * (d6 * 6.0D - 15.0D) + 10.0D);

            for (int i12 = 0; i12 < paramInt3; i12++)
            {
                double d12 = paramDouble3 + i12 * paramDouble6 + this.c;
                int i13 = (int) d12;
                if (d12 < i13)
                    i13--;
                int i14 = i13 & 0xFF;
                d12 -= i13;
                double d13 = d12 * d12 * d12 * (d12 * (d12 * 6.0D - 15.0D) + 10.0D);

                for (int i15 = 0; i15 < paramInt2; i15++)
                {
                    double d14 = paramDouble2 + i15 * paramDouble5 + this.b;
                    int i16 = (int) d14;
                    if (d14 < i16)
                        i16--;
                    int i17 = i16 & 0xFF;
                    d14 -= i16;
                    double d15 = d14 * d14 * d14 * (d14 * (d14 * 6.0D - 15.0D) + 10.0D);

                    if ((i15 == 0) || (i17 != m))
                    {
                        m = i17;
                        i7 = this.d[i6] + i17;
                        i8 = this.d[i7] + i14;
                        i9 = this.d[(i7 + 1)] + i14;
                        i10 = this.d[(i6 + 1)] + i17;
                        n = this.d[i10] + i14;
                        i11 = this.d[(i10 + 1)] + i14;
                        d10 = a(d7, a(this.d[i8], d6, d14, d12), a(this.d[n], d6 - 1.0D, d14, d12));
                        d4 = a(d7, a(this.d[i9], d6, d14 - 1.0D, d12), a(this.d[i11], d6 - 1.0D, d14 - 1.0D, d12));
                        d11 = a(d7, a(this.d[(i8 + 1)], d6, d14, d12 - 1.0D), a(this.d[(n + 1)], d6 - 1.0D, d14, d12 - 1.0D));
                        d5 = a(d7, a(this.d[(i9 + 1)], d6, d14 - 1.0D, d12 - 1.0D), a(this.d[(i11 + 1)], d6 - 1.0D, d14 - 1.0D, d12 - 1.0D));
                    }

                    double d16 = a(d15, d10, d4);
                    double d17 = a(d15, d11, d5);
                    double d18 = a(d13, d16, d17);

                    paramArrayOfDouble[(i++)] += d18 * d9;
                }
            }
        }
    }
}