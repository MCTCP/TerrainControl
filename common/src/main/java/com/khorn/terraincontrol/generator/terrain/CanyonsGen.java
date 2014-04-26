package com.khorn.terraincontrol.generator.terrain;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.helpers.MathHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import java.util.Random;

public class CanyonsGen extends TerrainGenBase
{
    private float[] a = new float[1024];
    private WorldConfig worldSettings;

    public CanyonsGen(WorldConfig wrk, LocalWorld world)
    {
        super(world);
        this.worldSettings = wrk;
    }

    protected void a(long paramLong, ChunkCoordinate generatingChunk, byte[] paramArrayOfByte, double paramDouble1, double paramDouble2, double paramDouble3, float paramFloat1, float paramFloat2, float paramFloat3, int size, double paramDouble4)
    {
        Random localRandom = new Random(paramLong);

        double d1 = generatingChunk.getBlockXCenter();
        double d2 = generatingChunk.getBlockZCenter();

        float f1 = 0.0F;
        float f2 = 0.0F;

        int i = 0;

        float f3 = 1.0F;
        for (int j = 0; ; j++)
        {
            if (j >= worldSettings.worldHeightCap)
                break;
            if ((j == 0) || (localRandom.nextInt(3) == 0))
            {
                f3 = 1.0F + localRandom.nextFloat() * localRandom.nextFloat() * 1.0F;
            }
            this.a[j] = (f3 * f3);
        }

        for (int stepCount = 0; stepCount < size; stepCount++)
        {
            double d3 = 1.5D + MathHelper.sin(stepCount * 3.141593F / size) * paramFloat1 * 1.0F;
            double d4 = d3 * paramDouble4;

            d3 *= (localRandom.nextFloat() * 0.25D + 0.75D);
            d4 *= (localRandom.nextFloat() * 0.25D + 0.75D);

            float f4 = MathHelper.cos(paramFloat3);
            float f5 = MathHelper.sin(paramFloat3);
            paramDouble1 += MathHelper.cos(paramFloat2) * f4;
            paramDouble2 += f5;
            paramDouble3 += MathHelper.sin(paramFloat2) * f4;

            paramFloat3 *= 0.7F;

            paramFloat3 += f2 * 0.05F;
            paramFloat2 += f1 * 0.05F;

            f2 *= 0.8F;
            f1 *= 0.5F;
            f2 += (localRandom.nextFloat() - localRandom.nextFloat()) * localRandom.nextFloat() * 2.0F;
            f1 += (localRandom.nextFloat() - localRandom.nextFloat()) * localRandom.nextFloat() * 4.0F;

            if ((i == 0) && (localRandom.nextInt(4) == 0))
            {
                continue;
            }
            double d5 = paramDouble1 - d1;
            double d6 = paramDouble3 - d2;
            double d7 = size - stepCount;
            double d8 = paramFloat1 + 2.0F + 16.0F;
            if (d5 * d5 + d6 * d6 - d7 * d7 > d8 * d8)
            {
                return;
            }

            if ((paramDouble1 < d1 - 16.0D - d3 * 2.0D) || (paramDouble3 < d2 - 16.0D - d3 * 2.0D) || (paramDouble1 > d1 + 16.0D + d3 * 2.0D) || (paramDouble3 > d2 + 16.0D + d3 * 2.0D))
                continue;
            int k = MathHelper.floor(paramDouble1 - d3) - generatingChunk.getBlockX() - 1;
            int m = MathHelper.floor(paramDouble1 + d3) - generatingChunk.getBlockX() + 1;

            int n = MathHelper.floor(paramDouble2 - d4) - 1;
            int i1 = MathHelper.floor(paramDouble2 + d4) + 1;

            int i2 = MathHelper.floor(paramDouble3 - d3) - generatingChunk.getBlockZ() - 1;
            int i3 = MathHelper.floor(paramDouble3 + d3) - generatingChunk.getBlockZ() + 1;

            if (k < 0)
                k = 0;
            if (m > 16)
                m = 16;

            if (n < 1)
                n = 1;
            if (i1 > worldSettings.worldHeightCap - 8)
                i1 = worldSettings.worldHeightCap - 8;

            if (i2 < 0)
                i2 = 0;
            if (i3 > 16)
                i3 = 16;

            int i4 = 0;
            int i8;
            for (int i5 = k; (i4 == 0) && (i5 < m); i5++)
            {
                for (int i6 = i2; (i4 == 0) && (i6 < i3); i6++)
                {
                    for (int i7 = i1 + 1; (i4 == 0) && (i7 >= n - 1); i7--)
                    {
                        i8 = (i5 * 16 + i6) * ChunkCoordinate.CHUNK_Y_SIZE + i7;
                        if (i7 < 0)
                            continue;
                        if (i7 < worldSettings.worldHeightCap)
                        {
                            if ((paramArrayOfByte[i8] == DefaultMaterial.WATER.id) || (paramArrayOfByte[i8] == DefaultMaterial.STATIONARY_WATER.id))
                            {
                                i4 = 1;
                            }
                            if ((i7 != n - 1) && (i5 != k) && (i5 != m - 1) && (i6 != i2) && (i6 != i3 - 1))
                                i7 = n;
                        }
                    }
                }
            }
            if (i4 != 0)
            {
                continue;
            }
            for (int i5 = k; i5 < m; i5++)
            {
                double d9 = (i5 + generatingChunk.getBlockX() + 0.5D - paramDouble1) / d3;
                for (i8 = i2; i8 < i3; i8++)
                {
                    double d10 = (i8 + generatingChunk.getBlockZ() + 0.5D - paramDouble3) / d3;
                    int i9 = (i5 * 16 + i8) * ChunkCoordinate.CHUNK_Y_SIZE + i1;
                    int i10 = 0;
                    if (d9 * d9 + d10 * d10 < 1.0D)
                    {
                        for (int i11 = i1 - 1; i11 >= n; i11--)
                        {
                            double d11 = (i11 + 0.5D - paramDouble2) / d4;
                            if ((d9 * d9 + d10 * d10) * this.a[i11] + d11 * d11 / 6.0D < 1.0D)
                            {
                                int i12 = paramArrayOfByte[i9];
                                if (i12 == DefaultMaterial.GRASS.id)
                                    i10 = 1;
                                if ((i12 == DefaultMaterial.STONE.id) || (i12 == DefaultMaterial.DIRT.id) || (i12 == DefaultMaterial.GRASS.id))
                                {
                                    if (i11 < 10)
                                    {
                                        paramArrayOfByte[i9] = (byte) DefaultMaterial.LAVA.id;
                                    } else
                                    {
                                        paramArrayOfByte[i9] = 0;
                                        if ((i10 != 0) && (paramArrayOfByte[(i9 - 1)] == DefaultMaterial.DIRT.id))
                                            paramArrayOfByte[(i9 - 1)] = (byte) DefaultMaterial.GRASS.id;
                                    }
                                }
                            }
                            i9--;
                        }
                    }
                }
            }
            if (i != 0)
                break;
        }
    }

    @Override
    protected void generateChunk(ChunkCoordinate currentChunk, ChunkCoordinate originalChunk, byte[] paramArrayOfByte)
    {
        if (this.random.nextInt(100) >= this.worldSettings.canyonRarity)
            return;
        double d1 = currentChunk.getBlockX() + this.random.nextInt(ChunkCoordinate.CHUNK_X_SIZE);
        double d2 = this.random.nextInt(this.worldSettings.canyonMaxAltitude - this.worldSettings.canyonMinAltitude) + this.worldSettings.canyonMinAltitude;
        double d3 = currentChunk.getBlockZ() + this.random.nextInt(ChunkCoordinate.CHUNK_Z_SIZE);

        int i = 1;

        for (int j = 0; j < i; j++)
        {
            float f1 = this.random.nextFloat() * 3.141593F * 2.0F;
            float f2 = (this.random.nextFloat() - 0.5F) * 2.0F / 8.0F;
            float f3 = (this.random.nextFloat() * 2.0F + this.random.nextFloat()) * 2.0F;

            int size = this.random.nextInt(this.worldSettings.canyonMaxLength - this.worldSettings.canyonMinLength) + this.worldSettings.canyonMinLength;

            a(this.random.nextLong(), originalChunk, paramArrayOfByte, d1, d2, d3, f3, f1, f2, size, this.worldSettings.canyonDepth);
        }
    }
}