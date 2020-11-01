package com.pg85.otg.gen.biome.layers.util;

import com.pg85.otg.gen.noise.PerlinNoiseSampler;

public interface LayerRandomnessSource
{
   int nextInt(int bound);

   PerlinNoiseSampler getNoiseSampler();
}
