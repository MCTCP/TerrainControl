package com.pg85.otg.generator.biome.layers.util;

public interface LayerFactory<A extends LayerSampler>
{
   A make();
}