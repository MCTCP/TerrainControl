package com.Khorn.TerrainControl.BiomeLayers.Layers;

import com.Khorn.TerrainControl.Bukkit.BiomeManager.ArraysCache;
import com.Khorn.TerrainControl.Configuration.WorldConfig;

public class LayerTemperature extends Layer
{
    public LayerTemperature(Layer paramLayer, WorldConfig config)
    {
        super(0L);
        this.child = paramLayer;
        this.worldConfig = config;
    }

    private WorldConfig worldConfig;

    public int[] GetBiomes(int cacheId,int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        int[] arrayOfInt1 = this.child.GetBiomes(cacheId,paramInt1, paramInt2, paramInt3, paramInt4);

        int[] arrayOfInt2 = ArraysCache.GetArray(cacheId, paramInt3 * paramInt4);
        for (int i = 0; i < paramInt3 * paramInt4; i++)
        {
            arrayOfInt2[i] = worldConfig.biomeConfigs[arrayOfInt1[i]].getTemperature();
        }
        return arrayOfInt2;
    }
}