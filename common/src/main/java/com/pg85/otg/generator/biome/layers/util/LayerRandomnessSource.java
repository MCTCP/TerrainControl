package com.pg85.otg.generator.biome.layers.util;

import com.pg85.otg.generator.noise.PerlinNoiseSampler;

public interface LayerRandomnessSource
{
   int nextInt(int bound);

   PerlinNoiseSampler getNoiseSampler();
}
