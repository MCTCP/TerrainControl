package com.Khorn.TerrainControl.BiomeManager.Layers;

import com.Khorn.TerrainControl.Configuration.WorldConfig;
import net.minecraft.server.BiomeBase;
import net.minecraft.server.IntCache;

public class LayerTemperatureMix extends Layer
{
    private Layer b;
    private int c;

    public LayerTemperatureMix(Layer paramLayer1, Layer paramLayer2, int paramInt, WorldConfig config)
    {
        super(0L);
        this.a = paramLayer2;
        this.b = paramLayer1;
        this.c = paramInt;
        this.worldConfig = config;
    }

    private WorldConfig worldConfig;

    public int[] a(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        int[] arrayOfInt1 = this.a.a(paramInt1, paramInt2, paramInt3, paramInt4);
        int[] arrayOfInt2 = this.b.a(paramInt1, paramInt2, paramInt3, paramInt4);

        int[] arrayOfInt3 = IntCache.a(paramInt3 * paramInt4);
        for (int i = 0; i < paramInt3 * paramInt4; i++)
        {
            arrayOfInt2[i] += (this.worldConfig.biomeConfigs[arrayOfInt1[i]].getTemperature() - arrayOfInt2[i]) / (this.c * 2 + 1);
        }

        return arrayOfInt3;
    }
}