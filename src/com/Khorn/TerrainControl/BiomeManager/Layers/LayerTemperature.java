package com.Khorn.TerrainControl.BiomeManager.Layers;

import com.Khorn.TerrainControl.Configuration.WorldConfig;
import net.minecraft.server.BiomeBase;
import net.minecraft.server.IntCache;

public class LayerTemperature extends Layer
{
    public LayerTemperature(Layer paramLayer, WorldConfig config)
    {
        super(0L);
        this.a = paramLayer;
        this.worldConfig = config;
    }

    private WorldConfig worldConfig;

    public int[] a(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        int[] arrayOfInt1 = this.a.a(paramInt1, paramInt2, paramInt3, paramInt4);

        int[] arrayOfInt2 = IntCache.a(paramInt3 * paramInt4);
        for (int i = 0; i < paramInt3 * paramInt4; i++)
        {
            arrayOfInt2[i] = worldConfig.biomeConfigs[arrayOfInt1[i]].getTemperature();
        }
        return arrayOfInt2;
    }
}