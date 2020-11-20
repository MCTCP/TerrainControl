package com.pg85.otg.gen.biome.layers;

import com.pg85.otg.gen.biome.layers.type.ParentedLayer;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;
import com.pg85.otg.gen.biome.layers.util.LayerSampler;

public class ScaleLayer implements ParentedLayer
{
   public ScaleLayer() { }

   public int transformX(int x)
   {
      return x >> 1;
   }

   public int transformZ(int y)
   {
      return y >> 1;
   }

   public int sample(LayerSampleContext<?> context, LayerSampler parent, int x, int z)
   {
      int i = parent.sample(this.transformX(x), this.transformZ(z));
      context.initSeed((long)(x >> 1 << 1), (long)(z >> 1 << 1));
      int j = x & 1;
      int k = z & 1;
      if (j == 0 && k == 0)
      {
         return i;
      } else {
         int l = parent.sample(this.transformX(x), this.transformZ(z + 1));
         int m = context.choose(i, l);
         if (j == 0 && k == 1)
         {
            return m;
         } else {
            int n = parent.sample(this.transformX(x + 1), this.transformZ(z));
            int o = context.choose(i, n);
            if (j == 1 && k == 0)
            {
               return o;
            } else {
               int p = parent.sample(this.transformX(x + 1), this.transformZ(z + 1));
               return this.sample(context, i, n, l, p);
            }
         }
      }
   }

   protected int sample(LayerSampleContext<?> context, int i, int j, int k, int l)
   {
      if (j == k && k == l)
      {
         return j;
      }
      else if (i == j && i == k)
      {
         return i;
      }
      else if (i == j && i == l)
      {
         return i;
      }
      else if (i == k && i == l)
      {
         return i;
      }
      else if (i == j && k != l)
      {
         return i;
      }
      else if (i == k && j != l)
      {
         return i;
      }
      else if (i == l && j != k)
      {
         return i;
      }
      else if (j == k && i != l)
      {
         return j;
      }
      else if (j == l && i != k)
      {
         return j;
      } else {
         return k == l && i != j ? k : context.choose(i, j, k, l);
      }
   }
}