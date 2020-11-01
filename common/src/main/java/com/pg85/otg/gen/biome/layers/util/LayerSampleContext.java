package com.pg85.otg.gen.biome.layers.util;

public interface LayerSampleContext<R extends LayerSampler> extends LayerRandomnessSource
{
   void initSeed(long x, long y);

   R createSampler(LayerOperator operator);

   default R createSampler(LayerOperator operator, R parent)
   {
      return this.createSampler(operator);
   }

   default R createSampler(LayerOperator operator, R layerSampler, R layerSampler2)
   {
      return this.createSampler(operator);
   }

   default int choose(int a, int b)
   {
      return this.nextInt(2) == 0 ? a : b;
   }

   default int choose(int a, int b, int c, int d)
   {
      int i = this.nextInt(4);
      if (i == 0)
      {
         return a;
      }
      else if (i == 1)
      {
         return b;
      } else {
         return i == 2 ? c : d;
      }
   }
}
