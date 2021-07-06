package com.pg85.otg.gen.biome.layers.util;

import com.pg85.otg.interfaces.ILayerSampler;

public interface LayerFactory<A extends ILayerSampler>
{
	A make();
}
