package com.Khorn.TerrainControl.BiomeManager.Layers;

import net.minecraft.server.BiomeBase;
import net.minecraft.server.IntCache;

public class LayerTemperatureMix extends Layer
{
  private Layer b;
  private int c;

  public LayerTemperatureMix(Layer paramLayer1, Layer paramLayer2, int paramInt)
  {
    super(0L);
    this.a = paramLayer2;
    this.b = paramLayer1;
    this.c = paramInt;
  }

  public int[] a(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
    int[] arrayOfInt1 = this.a.a(paramInt1, paramInt2, paramInt3, paramInt4);
    int[] arrayOfInt2 = this.b.a(paramInt1, paramInt2, paramInt3, paramInt4);

    int[] arrayOfInt3 = IntCache.a(paramInt3 * paramInt4);
    for (int i = 0; i < paramInt3 * paramInt4; i++) {
      arrayOfInt2[i] += (BiomeBase.a[arrayOfInt1[i]].f() - arrayOfInt2[i]) / (this.c * 2 + 1);
    }

    return arrayOfInt3;
  }
}