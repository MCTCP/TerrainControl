package com.pg85.otg.gen.biome.layers;

import com.pg85.otg.gen.biome.layers.util.LayerSampler;
import com.pg85.otg.interfaces.IBiomeConfig;

public interface LayerSource
{
	LayerSampler getSampler();

	IBiomeConfig getConfig(int x, int z);

	String getBiomeRegistryName(int biomeX, int biomeY, int biomeZ);
}
