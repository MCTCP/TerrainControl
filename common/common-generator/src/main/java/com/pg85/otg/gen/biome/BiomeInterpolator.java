package com.pg85.otg.gen.biome;

import com.pg85.otg.gen.biome.layers.LayerSource;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.interfaces.IBiomeConfig;

/**
 * Interpolates the given biome from biome coords (pos >> 2) to real coords.
 * This is required as a vanilla change in 1.15 changed biomes from being stored in real resolution, changing them to be
 * stored in a 4x4x4 cubes instead, allowing for 3d biomes at the cost of resolution. This class interpolates and provides
 * a rough estimation of the correct biome at the given world coords.
 */
public class BiomeInterpolator
{
   public static IBiomeConfig getConfig(long seed, int x, int y, int z, LayerSource generator)
   {
      int i = x - 2;
      int j = y - 2;
      int k = z - 2;
      int l = i >> 2;
      int m = j >> 2;
      int n = k >> 2;
      double d = (double) (i & 3) / 4.0D;
      double e = (double) (j & 3) / 4.0D;
      double f = (double) (k & 3) / 4.0D;
      double[] ds = new double[8];

      int t;
      int aa;
      int ab;
      for (t = 0; t < 8; ++t)
      {
         boolean bl = (t & 4) == 0;
         boolean bl2 = (t & 2) == 0;
         boolean bl3 = (t & 1) == 0;
         aa = bl ? l : l + 1;
         ab = bl2 ? m : m + 1;
         int r = bl3 ? n : n + 1;
         double g = bl ? d : d - 1.0D;
         double h = bl2 ? e : e - 1.0D;
         double s = bl3 ? f : f - 1.0D;
         ds[t] = calcSquaredDistance(seed, aa, ab, r, g, h, s);
      }

      t = 0;
      double u = ds[0];

      int v;
      for (v = 1; v < 8; ++v)
      {
         if (u > ds[v])
         {
            t = v;
            u = ds[v];
         }
      }

      v = (t & 4) == 0 ? l : l + 1;
      aa = (t & 2) == 0 ? m : m + 1;
      ab = (t & 1) == 0 ? n : n + 1;
      return generator.getConfig(v, ab);
   }

   private static double calcSquaredDistance(long seed, int x, int y, int z, double xFraction, double yFraction, double zFraction)
   {
      long l = MathHelper.mixSeed(seed, x);
      l = MathHelper.mixSeed(l, y);
      l = MathHelper.mixSeed(l, z);
      l = MathHelper.mixSeed(l, x);
      l = MathHelper.mixSeed(l, y);
      l = MathHelper.mixSeed(l, z);
      double d = distribute(l);
      l = MathHelper.mixSeed(l, seed);
      double e = distribute(l);
      l = MathHelper.mixSeed(l, seed);
      double f = distribute(l);
      return square(zFraction + f) + square(yFraction + e) + square(xFraction + d);
   }

   private static double distribute(long seed)
   {
      double d = (double) ((int) Math.floorMod(seed >> 24, 1024L)) / 1024.0D;
      return (d - 0.5D) * 0.9D;
   }

   private static double square(double d)
   {
      return d * d;
   }
}
