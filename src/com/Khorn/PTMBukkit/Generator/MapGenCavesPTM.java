package com.Khorn.PTMBukkit.Generator;

import com.Khorn.PTMBukkit.Settings;
import net.minecraft.server.Block;
import net.minecraft.server.MathHelper;
import org.bukkit.World;

import java.util.Random;

class MapGenCavesPTM
{
    private int a = 8;
    private Random b = new Random();

    private Settings WorldSettings;

    public MapGenCavesPTM(Settings wrk)
    {
        this.WorldSettings = wrk;
    }

    void a(int paramInt1, int paramInt2, byte[] paramArrayOfByte, double paramDouble1, double paramDouble2, double paramDouble3)
    {
        a(paramInt1, paramInt2, paramArrayOfByte, paramDouble1, paramDouble2, paramDouble3, 1.0F + this.b.nextFloat() * 6.0F, 0.0F, 0.0F, -1, -1, 0.5D);
    }

    void a(int paramInt1, int paramInt2, byte[] paramArrayOfByte, double paramDouble1, double paramDouble2, double paramDouble3, float paramFloat1, float paramFloat2, float paramFloat3, int paramInt3, int paramInt4, double paramDouble4)
    {
        double d1 = paramInt1 * 16 + 8;
        double d2 = paramInt2 * 16 + 8;

        float f1 = 0.0F;
        float f2 = 0.0F;

        Random localRandom = new Random(this.b.nextLong());

        if (paramInt4 <= 0)
        {
            int i = this.a * 16 - 16;
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

            if ((i == 0) && (paramInt3 == j) && (paramFloat1 > 1.0F))
            {
                a(paramInt1, paramInt2, paramArrayOfByte, paramDouble1, paramDouble2, paramDouble3, localRandom.nextFloat() * 0.5F + 0.5F, paramFloat2 - 1.570796F, paramFloat3 / 3.0F, paramInt3, paramInt4, 1.0D);
                a(paramInt1, paramInt2, paramArrayOfByte, paramDouble1, paramDouble2, paramDouble3, localRandom.nextFloat() * 0.5F + 0.5F, paramFloat2 + 1.570796F, paramFloat3 / 3.0F, paramInt3, paramInt4, 1.0D);
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
            if (i2 > 120)
                i2 = 120;

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
                        i9 = (i6 * 16 + i7) * 128 + i8;
                        if ((i8 >= 0) && (i8 < 128))
                        {
                            if ((paramArrayOfByte[i9] == Block.WATER.id) || (paramArrayOfByte[i9] == Block.STATIONARY_WATER.id))
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
                    int i10 = (i6 * 16 + i9) * 128 + i2;
                    int i11 = 0;
                    if (d9 * d9 + d10 * d10 < 1.0D)
                    {
                        for (int i12 = i2 - 1; i12 >= i1; i12--)
                        {
                            double d11 = (i12 + 0.5D - paramDouble2) / d4;
                            if ((d11 > -0.7D) && (d9 * d9 + d11 * d11 + d10 * d10 < 1.0D))
                            {
                                int i13 = paramArrayOfByte[i10];
                                if (i13 == Block.GRASS.id)
                                    i11 = 1;
                                if ((i13 == Block.STONE.id) || (i13 == Block.DIRT.id) || (i13 == Block.GRASS.id))
                                {
                                    if (i12 < 10)
                                    {
                                        paramArrayOfByte[i10] = (byte) Block.LAVA.id;
                                    } else
                                    {
                                        paramArrayOfByte[i10] = 0;
                                        if ((i11 != 0) && (paramArrayOfByte[(i10 - 1)] == Block.DIRT.id))
                                            paramArrayOfByte[(i10 - 1)] = (byte) Block.GRASS.id;
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

    void a( int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte)
    {
        int i = this.b.nextInt(this.b.nextInt(this.b.nextInt(this.WorldSettings.caveFrequency) + 1) + 1);
        if (this.WorldSettings.evenCaveDistribution)
            i = this.WorldSettings.caveFrequency;
        if (this.b.nextInt(100) > this.WorldSettings.caveRarity)
            i = 0;

        for (int j = 0; j < i; j++)
        {
            double d1 = paramInt1 * 16 + this.b.nextInt(16);

            double d2;

            if (this.WorldSettings.evenCaveDistribution)
                d2 = this.b.nextInt(this.WorldSettings.caveMaxAltitude - this.WorldSettings.caveMinAltitude) + this.WorldSettings.caveMinAltitude;
            else
                d2 = this.b.nextInt(this.b.nextInt(this.WorldSettings.caveMaxAltitude - this.WorldSettings.caveMinAltitude) + this.WorldSettings.caveMinAltitude + 1);
            double d3 = paramInt2 * 16 + this.b.nextInt(16);

            int k = this.WorldSettings.caveSystemFrequency;
            boolean l = false;
            if (this.b.nextInt(100) <= this.WorldSettings.individualCaveRarity)
            {
                a(paramInt3, paramInt4, paramArrayOfByte, d1, d2, d3);
                l = true;
            }

            if ((l) || (this.b.nextInt(100) <= this.WorldSettings.caveSystemPocketChance - 1))
            {
                k += this.b.nextInt(this.WorldSettings.caveSystemPocketMaxSize - this.WorldSettings.caveSystemPocketMinSize) + this.WorldSettings.caveSystemPocketMinSize;
            }
            for (int m = 0; m < k; m++)
            {
                float f1 = this.b.nextFloat() * 3.141593F * 2.0F;
                float f2 = (this.b.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float f3 = this.b.nextFloat() * 2.0F + this.b.nextFloat();

                a(paramInt3, paramInt4, paramArrayOfByte, d1, d2, d3, f3, f1, f2, 0, 0, 1.0D);
            }
        }
    }

    public void a(World paramWorld, int paramInt1, int paramInt2, byte[] paramArrayOfByte) {
    int i = this.a;

    this.b.setSeed(paramWorld.getSeed());
    long l1 = this.b.nextLong() / 2L * 2L + 1L;
    long l2 = this.b.nextLong() / 2L * 2L + 1L;

    for (int j = paramInt1 - i; j <= paramInt1 + i; j++)
      for (int k = paramInt2 - i; k <= paramInt2 + i; k++) {
        this.b.setSeed(j * l1 + k * l2 ^ paramWorld.getSeed());
        a( j, k, paramInt1, paramInt2, paramArrayOfByte);
      }
  }
}