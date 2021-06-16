package com.pg85.otg.gen.biome.layers.util;

public interface LayerFactory<A extends LayerSampler>
{
	A make();
}
