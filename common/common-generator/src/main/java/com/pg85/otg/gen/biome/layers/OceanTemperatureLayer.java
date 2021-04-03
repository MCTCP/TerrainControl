package com.pg85.otg.gen.biome.layers;

import com.pg85.otg.gen.biome.layers.type.InitLayer;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;

public class OceanTemperatureLayer implements InitLayer
{
	private final BiomeLayerData data;

	public OceanTemperatureLayer(BiomeLayerData data) {
		this.data = data;
	}

	@Override
	public int sample(LayerSampleContext<?> context, int x, int z)
	{
		double noise = context.getNoiseSampler().sample(x / 8.0, z / 8.0, 0, 0, 0);
		if (noise > 0.4) {
			return this.data.oceanTemperatures[0]; // Warm ocean
		} else if (noise > 0.2) {
			return this.data.oceanTemperatures[1]; // Lukewarm ocean
		} else if (noise < -0.4) {
			return this.data.oceanTemperatures[2]; // Frozen ocean
		} else if (noise < -0.2) {
			return this.data.oceanTemperatures[3]; // Cold ocean
		}

		return 0;
	}
}
