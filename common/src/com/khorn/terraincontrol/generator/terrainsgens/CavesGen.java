package com.khorn.terraincontrol.generator.terrainsgens;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.util.MathHelper;

import java.util.Random;

public class CavesGen extends TerrainGenBase
{
    private WorldConfig worldSettings;

    public CavesGen(WorldConfig wrk, LocalWorld world)
    {
        super(world);
        this.worldSettings = wrk;
    }

    protected void a(long paramLong, int paramInt1, int paramInt2, byte[] paramArrayOfByte, double paramDouble1, double paramDouble2, double paramDouble3)
    {
        a(paramLong, paramInt1, paramInt2, paramArrayOfByte, paramDouble1, paramDouble2, paramDouble3, 1.0F + this.c.nextFloat() * 6.0F, 0.0F, 0.0F, -1, -1, 0.5D);
    }

    protected void a(long paramLong, int paramInt1, int paramInt2, byte[] paramArrayOfByte, double paramDouble1, double paramDouble2, double paramDouble3, float paramFloat1, float paramFloat2, float paramFloat3, int paramInt3, int paramInt4, double paramDouble4)
    {
        double d1 = paramInt1 * 16 + 8;
        double d2 = paramInt2 * 16 + 8;

        float f1 = 0.0F;
        float f2 = 0.0F;

        Random localRandom = new Random(paramLong);

        if (paramInt4 <= 0)
        {
            int i = this.b * 16 - 16;
            paramInt4 = i - localRandom.nextInt(i / 4);
        }
        int i = 0;

        if (paramInt3 == -1)
        {
            paramInt3 = paramInt4 / 2;
            i = 1;
        }

        int j = localRandom.nextInt(paramInt4 / 2) + paramInt4 / 4;
        int k = localRandom.nextInt(6) == 0 ? 1 : 0;

        for (; paramInt3 < paramInt4; paramInt3++)
        {
            double d3 = 1.5D + MathHelper.sin(paramInt3 * 3.141593F / paramInt4) * paramFloat1 * 1.0F;
            double d4 = d3 * paramDouble4;

            float f3 = MathHelper.cos(paramFloat3);
            float f4 = MathHelper.sin(paramFloat3);
            paramDouble1 += MathHelper.cos(paramFloat2) * f3;
            paramDouble2 += f4;
            paramDouble3 += MathHelper.sin(paramFloat2) * f3;

            if (k != 0)
                paramFloat3 *= 0.92F;
            else
            {
                paramFloat3 *= 0.7F;
            }
            paramFloat3 += f2 * 0.1F;
            paramFloat2 += f1 * 0.1F;

            f2 *= 0.9F;
            f1 *= 0.75F;
            f2 += (localRandom.nextFloat() - localRandom.nextFloat()) * localRandom.nextFloat() * 2.0F;
            f1 += (localRandom.nextFloat() - localRandom.nextFloat()) * localRandom.nextFloat() * 4.0F;

            if ((i == 0) && (paramInt3 == j) && (paramFloat1 > 1.0F) && (paramInt4 > 0))
            {
                a(localRandom.nextLong(), paramInt1, paramInt2, paramArrayOfByte, paramDouble1, paramDouble2, paramDouble3, localRandom.nextFloat() * 0.5F + 0.5F, paramFloat2 - 1.570796F, paramFloat3 / 3.0F, paramInt3, paramInt4, 1.0D);
                a(localRandom.nextLong(), paramInt1, paramInt2, paramArrayOfByte, paramDouble1, paramDouble2, paramDouble3, localRandom.nextFloat() * 0.5F + 0.5F, paramFloat2 + 1.570796F, paramFloat3 / 3.0F, paramInt3, paramInt4, 1.0D);
                return;
            }
            if ((i == 0) && (localRandom.nextInt(4) == 0))
            {
                continue;
            }
            double d5 = paramDouble1 - d1;
            double d6 = paramDouble3 - d2;
            double d7 = paramInt4 - paramInt3;
            double d8 = paramFloat1 + 2.0F + 16.0F;
            if (d5 * d5 + d6 * d6 - d7 * d7 > d8 * d8)
            {
                return;
            }

            if ((paramDouble1 < d1 - 16.0D - d3 * 2.0D) || (paramDouble3 < d2 - 16.0D - d3 * 2.0D) || (paramDouble1 > d1 + 16.0D + d3 * 2.0D) || (paramDouble3 > d2 + 16.0D + d3 * 2.0D))
                continue;
            int m = MathHelper.floor(paramDouble1 - d3) - paramInt1 * 16 - 1;
            int n = MathHelper.floor(paramDouble1 + d3) - paramInt1 * 16 + 1;

            int i1 = MathHelper.floor(paramDouble2 - d4) - 1;
            int i2 = MathHelper.floor(paramDouble2 + d4) + 1;

            int i3 = MathHelper.floor(paramDouble3 - d3) - paramInt2 * 16 - 1;
            int i4 = MathHelper.floor(paramDouble3 + d3) - paramInt2 * 16 + 1;

            if (m < 0)
                m = 0;
            if (n > 16)
                n = 16;

            if (i1 < 1)
                i1 = 1;
            if (i2 > this.worldSettings.WorldHeight - 8)
            {
                i2 = this.worldSettings.WorldHeight - 8;
            }
            if (i3 < 0)
                i3 = 0;
            if (i4 > 16)
                i4 = 16;

            int i5 = 0;
            int i9;
            for (int i6 = m; (i5 == 0) && (i6 < n); i6++)
            {
                for (int i7 = i3; (i5 == 0) && (i7 < i4); i7++)
                {
                    for (int i8 = i2 + 1; (i5 == 0) && (i8 >= i1 - 1); i8--)
                    {
                        i9 = (i6 * 16 + i7) * this.worldSettings.WorldHeight + i8;
                        if (i8 < 0)
                            continue;

                        if (i8 < this.worldSettings.WorldHeight)
                        {
                            if ((paramArrayOfByte[i9] == DefaultMaterial.WATER.id) || (paramArrayOfByte[i9] == DefaultMaterial.STATIONARY_WATER.id))
                            {
                                i5 = 1;
                            }
                            if ((i8 != i1 - 1) && (i6 != m) && (i6 != n - 1) && (i7 != i3) && (i7 != i4 - 1))
                                i8 = i1;
                        }
                    }
                }
            }
            if (i5 != 0)
                continue;
            for (int i6 = m; i6 < n; i6++)
            {
                double d9 = (i6 + paramInt1 * 16 + 0.5D - paramDouble1) / d3;
                for (i9 = i3; i9 < i4; i9++)
                {
                    double d10 = (i9 + paramInt2 * 16 + 0.5D - paramDouble3) / d3;

                    int i10 = (i6 * 16 + i9) * this.worldSettings.WorldHeight + i2;
                    int i11 = 0;
                    if (d9 * d9 + d10 * d10 < 1.0D)
                    {
                        for (int i12 = i2 - 1; i12 >= i1; i12--)
                        {
                            double d11 = (i12 + 0.5D - paramDouble2) / d4;
                            if ((d11 > -0.7D) && (d9 * d9 + d11 * d11 + d10 * d10 < 1.0D))
                            {
                                int i13 = paramArrayOfByte[i10];
                                if (i13 == DefaultMaterial.GRASS.id)
                                    i11 = 1;
                                if ((i13 == DefaultMaterial.STONE.id) || (i13 == DefaultMaterial.DIRT.id) || (i13 == DefaultMaterial.GRASS.id))
                                {
                                    if (i12 < 10)
                                    {
                                        paramArrayOfByte[i10] = (byte) DefaultMaterial.LAVA.id;
                                    } else
                                    {
                                        paramArrayOfByte[i10] = 0;
                                        if ((i11 != 0) && (paramArrayOfByte[(i10 - 1)] == DefaultMaterial.DIRT.id))
                                            paramArrayOfByte[(i10 - 1)] = (byte) DefaultMaterial.GRASS.id;
                                    }
                                }
                            }
                            i10--;
                        }
                    }
                }
            }
            if (i != 0)
                break;
        }
    }

    protected void a(int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte)
    {
        int i = this.c.nextInt(this.c.nextInt(this.c.nextInt(this.worldSettings.caveFrequency) + 1) + 1);
        if (this.worldSettings.evenCaveDistribution)
            i = this.worldSettings.caveFrequency;
        if (this.c.nextInt(100) >= this.worldSettings.caveRarity)
            i = 0;

        for (int j = 0; j < i; j++)
        {
            double d1 = paramInt1 * 16 + this.c.nextInt(16);

            double d2;

            if (this.worldSettings.evenCaveDistribution)
                d2 = this.c.nextInt(this.worldSettings.caveMaxAltitude - this.worldSettings.caveMinAltitude) + this.worldSettings.caveMinAltitude;
            else
                d2 = this.c.nextInt(this.c.nextInt(this.worldSettings.caveMaxAltitude - this.worldSettings.caveMinAltitude) + this.worldSettings.caveMinAltitude + 1);
            double d3 = paramInt2 * 16 + this.c.nextInt(16);

            int k = this.worldSettings.caveSystemFrequency;
            boolean l = false;
            if (this.c.nextInt(100) <= this.worldSettings.individualCaveRarity)
            {
                a(this.c.nextLong(), paramInt3, paramInt4, paramArrayOfByte, d1, d2, d3);
                l = true;
            }

            if ((l) || (this.c.nextInt(100) <= this.worldSettings.caveSystemPocketChance - 1))
            {
                k += this.c.nextInt(this.worldSettings.caveSystemPocketMaxSize - this.worldSettings.caveSystemPocketMinSize) + this.worldSettings.caveSystemPocketMinSize;
            }
            for (int m = 0; m < k; m++)
            {
                float f1 = this.c.nextFloat() * 3.141593F * 2.0F;
                float f2 = (this.c.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float f3 = this.c.nextFloat() * 2.0F + this.c.nextFloat();

                a(this.c.nextLong(), paramInt3, paramInt4, paramArrayOfByte, d1, d2, d3, f3, f1, f2, 0, 0, 1.0D);
            }
        }
    }

}