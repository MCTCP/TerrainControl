package com.pg85.otg.gen.biome.layers;

import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.gen.biome.layers.util.LayerSampler;

public interface LayerSource
{
	LayerSampler getSampler();

	BiomeConfig getConfig(int x, int z);
}
